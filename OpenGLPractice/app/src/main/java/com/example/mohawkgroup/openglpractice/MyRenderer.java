package com.example.mohawkgroup.openglpractice;

import android.content.Context;
import android.opengl.EGLConfig;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import org.j3d.geom.particle.MaxTimeParticleFunction;
import org.j3d.loaders.stl.STLFileReader;

/**
 * Created by Mohawk Group on 6/24/2016.
 * Sets up GLES20 environment, handles view transformations
 */
public class MyRenderer implements GLSurfaceView.Renderer {
    private Context context;
    public volatile float xAngle = 0.0f;
    public volatile float yAngle = 0.0f;
    // up direction from perspective of model, last coord indicates that it's a direction
    public volatile float[] upDirection = {0.0f, 1.0f, 0.0f, 0.0f};
    // direction model is facing, initialized towards camera, last coord indicates that it's a direction
    public volatile float[] facingDirection = {0.0f, 0.0f, 1.0f, 0.0f};

    private double[] bounding_box = {Double.MAX_VALUE, -Double.MAX_VALUE,
                                        Double.MAX_VALUE, -Double.MAX_VALUE,
                                        Double.MAX_VALUE, -Double.MAX_VALUE};
                                        // = {x_min, x_max, y_min, y_max, z_min, z_max)
    private double[] box_size = {0.0, 0.0, 0.0}; // = {delta_x, delta_y, delta_z}
    private float[] box_middle = {0.0f, 0.0f, 0.0f}; // = {x_avg, y_avg, z_avg}
    private float max_box_size;

    // my shader source code
    private final String vertexShaderCode =
            "uniform mat4 normalTransformMatrix;" + // model view (no projection)
            "uniform mat4 uMVPMatrix;" +

                    "uniform vec4 aColor;" + // color of triangle before we apply lambertian shading
                    "uniform vec3 light_dir; " + // direction of incident light, should be normalized

                    "attribute vec4 vPosition;" +
                    "attribute vec4 vNormal;" +
                    "varying vec4 vColor;" + // color of triangle after we apply lambertian shading

                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" + // update gl_position

                    "  vec3 modelViewNormal = normalize(vec3(normalTransformMatrix * vNormal));" +
                    "  float lambert_factor = max(-dot(vec3(modelViewNormal), light_dir), 0.1);" +
                    "  vColor = lambert_factor * aColor;" +
                    "  vColor.w = 1.0;" +

//                    "  vColor = aColor;" +

//                    "  float normalX = max(vNormal.x, -vNormal.x);" +
//                    "  float normalY = max(vNormal.y, -vNormal.y);" +
//                    "  float normalZ = max(vNormal.z, -vNormal.z);" +
//                    "  if ((normalX > normalY) && (normalX > normalZ)) {" +
//                    "    vColor = vec4(1.0, 0.0, 0.0, 1.0);" +
//                    "  } else if ((normalY > normalX) && (normalY > normalZ)) {" +
//                    "    vColor = vec4(0.0, 1.0, 0.0, 1.0);" +
//                    "  } else if ((normalZ > normalX) && (normalZ > normalY)) {" +
//                    "    vColor = vec4(0.0, 0.0, 1.0, 1.0);" +
//                    "  } else {" +
//                    "    vColor = vec4(0.0, 0.0, 0.0, 1.0);" +
//                    "  }" +

                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    // my handles
    private int myProgram;
    private int myPositionHandle;
    private int myNormalHandle;
    private int myColorHandle;
    private int myLightDirHandle;
    float[] color = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f};
    private int myMVPMatrixHandle;
    private int normalTransformMatrixHandle;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] myMVPMatrix = new float[16];
    private final float[] myViewProjectionMatrix = new float[16];
    private final float[] myProjectionMatrix = new float[16];
    private final float[] myViewMatrix = new float[16];
    private final float[] normalTransformMatrix = new float[16];
    private final float[] myModelMatrix = new float[16];
    float[] xRotationMatrix = new float[16]; // rotates about x axis, caused by y movement
    float[] yRotationMatrix = new float[16]; // rotates about y axis, caused by x movement
    float[] totRotationMatrix = new float[16];
    float[] scratch = new float[16];

    // for storing vertices
    private int number_of_triangles;

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, javax.microedition.khronos.egl.EGLConfig eglConfig) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        // enable face culling feature
        GLES20.glEnable(GL10.GL_CULL_FACE);
        // specify which faces to not draw
        GLES20.glCullFace(GL10.GL_BACK);

        // Enable depth test
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        // Accept fragment if it closer to the camera than the former one
        GLES20.glDepthFunc(GLES20.GL_LESS);

        // set up compile and link shaders
        int vertexShader = MyRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // get a program
        myProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(myProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(myProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(myProgram);

        // load up the model from file (exclude file extension)
        ModelLoader input_loader = new ModelLoader(context, "cube"); // name of stl goes here

        List<Triangle> triangles = new LinkedList<Triangle>();
        List<Vertex> normals = new LinkedList<Vertex>();
//        List<Float> packed_data_list = new LinkedList<Float>(); // XYZ XYZ XYZ NxNyNz ...
        number_of_triangles = 0;
        double[] normal = new double[3];
        double[][] vertices = new double[3][3];
        while (input_loader.getNextFacet(normal, vertices)) {
            Triangle temp_tri = new Triangle(vertices);
            triangles.add(temp_tri);
            normals.add(new Vertex(normal[0], normal[1], normal[2]));

            update_bounding_box(vertices);
        }

        // set triangle manually
//        Vertex v0 = new Vertex(0.0f, 0.0f, 0.0f);
//        Vertex v1 = new Vertex(1.0f, 0.0f, 0.0f);
//        Vertex v2 = new Vertex(0.0f, 1.0f, 0.0f);
//        triangles.add(new Triangle(v0, v1, v2));
//        bounding_box[0] = 0.0f;
//        bounding_box[1] = 1.0f;
//        bounding_box[2] = 0.0f;
//        bounding_box[3] = 1.0f;
//        bounding_box[4] = 0.0f;
//        bounding_box[5] = 0.0f;

        number_of_triangles = triangles.size();

        box_size[0] = bounding_box[1] - bounding_box[0];
        box_size[1] = bounding_box[3] - bounding_box[2];
        box_size[2] = bounding_box[5] - bounding_box[4];

        box_middle[0] = (float) (bounding_box[0] + bounding_box[1]) / 2;
        box_middle[1] = (float) (bounding_box[2] + bounding_box[3]) / 2;
        box_middle[2] = (float) (bounding_box[4] + bounding_box[5]) / 2;

        max_box_size = (float) Math.max(Math.max(box_size[0], box_size[1]), box_size[2]);
        print_bounding_box();

        // put model at origin TODO: find a way to do this with matrix transformations
//        for (Triangle triangle : triangles) {
//            triangle.translate(new Vertex(-box_middle[0], -box_middle[1], -box_middle[2]));
//        }

        // load vertex positions into a float array
        // (# of triangles) * (vertices per triangle + normals per triangle) * (floats per vertex or normal)
        float[] packed_data_array = new float[(number_of_triangles) * (3 + 3) * 4];
        int step_size = (3 + 3) * 4; // (vertices per triangle + normals per triangle) * (floats per vertex or normal)
        int position_index = 0;
        for (int i = 0; i < number_of_triangles; i++) {
            Triangle temp_tri = triangles.get(i);
            Vertex temp_vertex =  normals.get(i);

            // Add vertex 1 and the normal for this tri
            Vertex firstVertex = temp_tri.getFirstVertex();
            packed_data_array[(step_size * i)] = firstVertex.getX();
            packed_data_array[(step_size * i) + 1] = firstVertex.getY();
            packed_data_array[(step_size * i) + 2] = firstVertex.getZ();
            packed_data_array[(step_size * i) + 3] = 1.0f; // this is a position
            packed_data_array[(step_size * i) + 4] = temp_vertex.getX();
            packed_data_array[(step_size * i) + 5] = temp_vertex.getY();
            packed_data_array[(step_size * i) + 6] = temp_vertex.getZ();
            packed_data_array[(step_size * i) + 7] = 0.0f; // this is a direction

            // Add vertex 2 and the normal for this tri
            Vertex secondVertex = temp_tri.getSecondVertex();
            packed_data_array[(step_size * i) + 8] = secondVertex.getX();
            packed_data_array[(step_size * i) + 9] = secondVertex.getY();
            packed_data_array[(step_size * i) + 10] = secondVertex.getZ();
            packed_data_array[(step_size * i) + 11] = 1.0f; // this is a position
            packed_data_array[(step_size * i) + 12] = temp_vertex.getX();
            packed_data_array[(step_size * i) + 13] = temp_vertex.getY();
            packed_data_array[(step_size * i) + 14] = temp_vertex.getZ();
            packed_data_array[(step_size * i) + 15] = 0.0f; // this is a direction

            // Add vertex 3 and the normal for this tri
            Vertex thirdVertex = temp_tri.getThirdVertex();
            packed_data_array[(step_size * i) + 16] = thirdVertex.getX();
            packed_data_array[(step_size * i) + 17] = thirdVertex.getY();
            packed_data_array[(step_size * i) + 18] = thirdVertex.getZ();
            packed_data_array[(step_size * i) + 19] = 1.0f; // this is a position
            packed_data_array[(step_size * i) + 20] = temp_vertex.getX();
            packed_data_array[(step_size * i) + 21] = temp_vertex.getY();
            packed_data_array[(step_size * i) + 22] = temp_vertex.getZ();
            packed_data_array[(step_size * i) + 23] = 0.0f; // this is a direction
        }

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(myProgram);

        // create handles to important stuff
        // get handle to vertex shader's vPosition member
        myPositionHandle = GLES20.glGetAttribLocation(myProgram, "vPosition");

//         get handle to vertex shader's vNormal member
        myNormalHandle = GLES20.glGetAttribLocation(myProgram, "vNormal");

        // get handle to fragment shader's vColor member
        myColorHandle = GLES20.glGetUniformLocation(myProgram, "aColor");
        GLES20.glUniform4fv(myColorHandle, 1, color, 0); // load color into GPU memory

        myLightDirHandle = GLES20.glGetUniformLocation(myProgram, "light_dir");
        GLES20.glUniform3f(myLightDirHandle, 0.0f, 0.0f, -1.0f);

        // get handle to shape's transformation matrix
        myMVPMatrixHandle = GLES20.glGetUniformLocation(myProgram, "uMVPMatrix");

        normalTransformMatrixHandle = GLES20.glGetUniformLocation(myProgram, "normalTransformMatrix");

        // set light direction
//        myLightDirHandle = GLES20.glGetAttribLocation(myProgram, "light_dir");
//        GLES20.glUniform3f(myLightDirHandle, 0.0f, 0.0f, 1.0f);

        // Create buffer containing all of our vertex data
        int number_of_bytes = packed_data_array.length * 4; // (# of floats) * (bytes per float)
        ByteBuffer bb = ByteBuffer.allocateDirect(number_of_bytes);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        FloatBuffer cDataBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        cDataBuffer.put(packed_data_array);
        // set the buffer to read the first coordinate
        cDataBuffer.position(0);

        int[] gBuffers = new int[3];
        GLES20.glGenBuffers(3, gBuffers, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, gBuffers[0]);

        // Give our data to OpenGL.
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
                            number_of_bytes,
                            cDataBuffer,
                            GLES20.GL_STATIC_DRAW // we will NOT update positions dynamically
        );

//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, gBuffers[1]);

        // IMPORTANT: Unbind from the buffer when we're done with it.

        // (floats_per_vertex * vertices_per_triangle + floats_per_normal * normals_per_triangle) * bytes_per_float
        int stride = (4 + 4) * 4;
        // attach our vertex info to myPositionHandle
        GLES20.glEnableVertexAttribArray(myPositionHandle);
        GLES20.glVertexAttribPointer(
                myPositionHandle,   // maps the vertex coords to vPosition in shader code
                4,                  // size
                GLES20.GL_FLOAT,    // type
                false,              // normalized?
                stride,                  // stride
                0                   // array buffer offset
        );

        GLES20.glEnableVertexAttribArray(myNormalHandle);
        GLES20.glVertexAttribPointer(
                myNormalHandle,
                4,
                GLES20.GL_FLOAT,
                false,
                stride,
                4 * 4); // (4 floats per vertex) * (4 bytes per float)

        // IMPORTANT: Unbind from the buffer when we're done with it.
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // get view matrix
        Matrix.setLookAtM(myViewMatrix, 0, 0.0f, 0.0f,
                10.0f * max_box_size, //25.0f, // = z_min - (z_min + z_max)/2
                0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        // for transitioning to model space
        Matrix.setIdentityM(scratch, 0);
        Matrix.translateM(myModelMatrix, 0,scratch, 0, -box_middle[0], -box_middle[1], -box_middle[2]);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Calculate rotation matrix
        Matrix.setRotateM(xRotationMatrix, 0, xAngle, 1.0f, 0, 0);
        Matrix.setRotateM(yRotationMatrix, 0, yAngle, 0, 1.0f, 0);
        Matrix.multiplyMM(totRotationMatrix, 0, xRotationMatrix, 0, yRotationMatrix, 0);

        // [projection] * [view] * [rotation] * [translation]
        Matrix.multiplyMM(scratch, 0, totRotationMatrix, 0, myModelMatrix, 0);
        Matrix.multiplyMM(myMVPMatrix, 0, myViewProjectionMatrix, 0, scratch, 0); // scratch
//        for (int i = 0; i < 16; i++) {
//            System.out.println(totRotationMatrix[i]);
//        }

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(myMVPMatrixHandle, 1, false, myMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(normalTransformMatrixHandle, 1, false, totRotationMatrix, 0);
        //

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, number_of_triangles * 3);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(myProjectionMatrix, 0, -ratio, ratio, -1, 1, 18.0f * Math.min(ratio, 1.0f), 20.0f * max_box_size); //5.0f, 145.0f); //3.0f, 47.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(myViewProjectionMatrix, 0, myProjectionMatrix, 0, myViewMatrix, 0);
    }

    public static int loadShader(int type, String shaderCode) {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
//        // Get the compilation status.
//        final int[] compileStatus = new int[1];
//        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
//
//        // If the compilation failed, delete the shader.
//        if (compileStatus[0] == 0)
//        {
//            GLES20.glDeleteShader(shader);
//            throw new RuntimeException("Error creating vertex shader.");
//        }

        return shader;
    }

    public void update_bounding_box(double[][] vertices) {
        for (int i = 0; i < 3; i++) {
            if (vertices[i][0] < bounding_box[0]) {
                bounding_box[0] = vertices[i][0];
            }
            if (vertices[i][0] > bounding_box[1]) {
                bounding_box[1] = vertices[i][0];
            }
            if (vertices[i][1] < bounding_box[2]) {
                bounding_box[2] = vertices[i][1];
            }
            if (vertices[i][1] > bounding_box[3]) {
                bounding_box[3] = vertices[i][1];
            }
            if (vertices[i][2] < bounding_box[4]) {
                bounding_box[4] = vertices[i][2];
            }
            if (vertices[i][2] > bounding_box[5]) {
                bounding_box[5] = vertices[i][2];
            }
        }
    }

    public void print_bounding_box() {
        System.out.println();
        System.out.println("x_min = " + Double.toString(bounding_box[0]));
        System.out.println("x_max = " + Double.toString(bounding_box[1]));
        System.out.println("y_min = " + Double.toString(bounding_box[2]));
        System.out.println("y_max = " + Double.toString(bounding_box[3]));
        System.out.println("z_min = " + Double.toString(bounding_box[4]));
        System.out.println("z_max = " + Double.toString(bounding_box[5]));
        System.out.println();
    }

    public float getYAngle() {
        return yAngle;
    }

    public void setYAngle(float angle) {
        yAngle = angle;
    }
    public float getXAngle() {
        return xAngle;
    }

    public void setXAngle(float angle) {
        xAngle = angle;
    }

    // updates orientation according to direction screen is being swiped
    // also updates orientation matrix
    // the axis of rotation is perpendicular both the swipe direction and z
    public void updateOrientation(float dx, float dy) {
        float[] rotation_axis = {-dy, dx, 0.0f};
        float rotation_angle = (float) Math.sqrt((double) (dx * dx + dy * dy));
        float[] temp_rotation_matrix = new float[16];
        Matrix.setRotateM(temp_rotation_matrix,
                            0,
                            rotation_angle,
                            rotation_axis[0],
                            rotation_axis[1],
                            rotation_axis[2]);

        float[] scratch_direction = new float[4];
        Matrix.multiplyMV(scratch_direction, 0, temp_rotation_matrix, 0, upDirection, 0);
//        scratch_direction = normalizeVector(scratch_direction); // normalize scratch_direction
        float[] rotation_axis_A = normalizeVector(cross(upDirection, scratch_direction));
        Matrix.setRotateM(xRotationMatrix,
                            0,
                            (float) Math.acos((double) dot(upDirection, scratch_direction)),
                            rotation_axis_A[0],
                            rotation_axis_A[1],
                            rotation_axis_A[2]);
//        upDirection = scratch_direction;
        System.arraycopy(scratch_direction, 0, upDirection, 0, 4);

        Matrix.multiplyMV(scratch_direction, 0, temp_rotation_matrix, 0, facingDirection, 0);
//        scratch_direction = normalizeVector(scratch_direction); // normalize scratch_direction
        Matrix.setRotateM(yRotationMatrix,
                            0,
                            (float) Math.acos((double) dot(facingDirection, scratch_direction)),
                            upDirection[0],
                            upDirection[1],
                            upDirection[2]);
//        facingDirection = scratch_direction;
        System.arraycopy(scratch_direction, 0, facingDirection, 0, 4);

        Matrix.multiplyMM(totRotationMatrix, 0, yRotationMatrix, 0, xRotationMatrix, 0);
    }

    // only normalizes first 3 components
    public static float[] normalizeVector(float[] input_vector) {
        int number_of_elements = 3; // input_vector.length;
        float square_accumulator = 0;
        for (int i = 0; i < number_of_elements; i++) {
            square_accumulator += (float) Math.pow(input_vector[i], 2);
        }

        float[] output_vector = new float[number_of_elements];
        for (int i = 0; i < number_of_elements; i++) {
            output_vector[i] = input_vector[i] / (float) Math.sqrt(square_accumulator);
        }
//        output_vector[3] = input_vector[3];
        return output_vector;
    }

    // only cares about first 3 elements
    public static float dot(float[] v1, float[] v2) {
        int number_of_elements = 3;
        float accumulator = 0;
        for (int i = 0; i < number_of_elements; i++) {
            accumulator += v1[i] * v2[i];
        }
        return accumulator;
    }

    // assumes input vectors have 4 elements
    // last element of returned matrix is the same as last element of first argument
    public static float[] cross(float[] v1, float[] v2) {
        float[] output_vector = new float[4];
        output_vector[0] = v1[1] * v2[2] - v1[2] * v2[1];
        output_vector[1] = -(v1[0] * v2[2] - v1[2] * v1[0]);
        output_vector[2] = v1[0] * v2[1] - v1[1] * v2[0];
        output_vector[3] = v1[3];
        return output_vector;
    }
}
