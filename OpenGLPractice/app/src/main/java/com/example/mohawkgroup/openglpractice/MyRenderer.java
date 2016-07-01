package com.example.mohawkgroup.openglpractice;

import android.content.Context;
import android.opengl.EGLConfig;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import org.j3d.loaders.stl.STLFileReader;

/**
 * Created by Mohawk Group on 6/24/2016.
 * Sets up GLES20 environment, handles view transformations
 */
public class MyRenderer implements GLSurfaceView.Renderer {
    private Context context;
    public volatile float xAngle = 0.0f;
    public volatile float yAngle = 0.0f;

    private LinkedList<Triangle> triangles = new LinkedList<Triangle>();
    private double[] bounding_box = {Double.MAX_VALUE, -Double.MAX_VALUE,
                                        Double.MAX_VALUE, -Double.MAX_VALUE,
                                        Double.MAX_VALUE, -Double.MAX_VALUE};
                                        // = {x_min, x_max, y_min, y_max, z_min, z_max)
    private double[] box_size = {0.0, 0.0, 0.0}; // = {delta_x, delta_y, delta_z}
    private float[] box_middle = {0.0f, 0.0f, 0.0f}; // = {x_avg, y_avg, z_avg}

    // my shader source code
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}";
//            "uniform mat4 u_MVPMatrix;      \n"     // A constant representing the combined model/view/projection matrix.
//
//                    + "attribute vec4 a_Position;     \n"     // Per-vertex position information we will pass in.
//
//                    + "void main()                    \n"     // The entry point for our vertex shader.
//                    + "{                              \n"
//                    // It will be interpolated across the triangle.
//                    + "   gl_Position = u_MVPMatrix   \n"     // gl_Position is a special variable used to store the final position.
//                    + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in
//                    + "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    // my handles
    private int myProgram;
    private int myMVPMatrixHandle;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] myMVPMatrix = new float[16];
    private final float[] myProjectionMatrix = new float[16];
    private final float[] myViewMatrix = new float[16];
    private final float[] transThenRotMatrix = new float[16];
    private final float[] myTranslationMatrix = new float[16];
    float[] xRotationMatrix = new float[16]; // rotates about x axis, caused by y movement
    float[] yRotationMatrix = new float[16]; // rotates about y axis, caused by x movement
    float[] totRotationMatrix = new float[16];
    float[] scratch = new float[16];

    // for storing vertices
    private int number_of_triangles;
    private FloatBuffer allTrianglesVertexBuffer;

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

        List<Triangle> triangles = new LinkedList<Triangle>();
//        // load up the model from file (exclude file extension)
//        ModelLoader input_loader = new ModelLoader(context, "cube"); // name of stl goes here
//        // populate triangles list
//        FloatBuffer vertexBuffer;
//        double[] normal = new double[3];
//        double[][] vertices = new double[3][3];
//        while (input_loader.getNextFacet(normal, vertices)) {
//            Triangle temp_tri = new Triangle(vertices);
//            triangles.add(temp_tri);
//            update_bounding_box(vertices);
//        }
//        numer_of_triangles = triangles.size();

        // set triangle manually
        Vertex v0 = new Vertex(0.0f, 0.0f, 0.0f);
        Vertex v1 = new Vertex(1.0f, 0.0f, 0.0f);
        Vertex v2 = new Vertex(0.0f, 1.0f, 0.0f);
        triangles.add(new Triangle(v0, v1, v2));
        bounding_box[0] = 0.0f;
        bounding_box[1] = 1.0f;
        bounding_box[2] = 0.0f;
        bounding_box[3] = 1.0f;
        bounding_box[4] = 0.0f;
        bounding_box[5] = 0.0f;
        number_of_triangles = triangles.size();

        box_size[0] = bounding_box[1] - bounding_box[0];
        box_size[1] = bounding_box[3] - bounding_box[2];
        box_size[2] = bounding_box[5] - bounding_box[4];

        box_middle[0] = (float) (bounding_box[0] + bounding_box[1]) / 2;
        box_middle[1] = (float) (bounding_box[2] + bounding_box[3]) / 2;
        box_middle[2] = (float) (bounding_box[4] + bounding_box[5]) / 2;
//        print_bounding_box();
//
//        // put model at origin TODO: find a way to do this with matrix transformations
//        for (Triangle triangle : triangles) {
//            triangle.translate(new Vertex(-box_middle[0], -box_middle[1], -box_middle[2]));
//        }
//
//        // (# of triangles) * (vertices per triangle) * (floats per vertice)
//        float[] vertex_data_array = new float[number_of_triangles * 3 * 3];
//        int position_index = 0;
//        for (Triangle triangle : triangles) {
//            // add the coordinates to the FloatBuffer
//            float[] temp_array = triangle.getCoordsAsArray();
//            for (int i = 0; i < temp_array.length; i++) {
//                vertex_data_array[position_index] = temp_array[i];
//                position_index++;
//            }
//        }
//
//        // Create buffer containing all of our vertex data
//        int number_of_bytes = vertex_data_array.length * 4; // (# of floats) * (bytes per float)
//        ByteBuffer bb = ByteBuffer.allocateDirect(number_of_bytes);
//        // use the device hardware's native byte order
//        bb.order(ByteOrder.nativeOrder());
//
//        // create a floating point buffer from the ByteBuffer
//        FloatBuffer cVertexBuffer = bb.asFloatBuffer();
//        // add the coordinates to the FloatBuffer
//        cVertexBuffer.put(vertex_data_array);
//        // set the buffer to read the first coordinate
//        cVertexBuffer.position(0);
//
//        int gVertexBuffer[] = new int[1];
//        GLES20.glGenBuffers(number_of_triangles * 3, gVertexBuffer, 0);
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, gVertexBuffer[0]);
//        // Give our vertices to OpenGL.
//        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
//                            number_of_bytes,
//                            cVertexBuffer,
//                            GLES20.GL_STATIC_DRAW // we will NOT update positions dynamically
//        );
//
//        // IMPORTANT: Unbind from the buffer when we're done with it.
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
//
//
//
//        GLES20.glEnableVertexAttribArray(0);
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, gVertexBuffer[0]);
//        GLES20.glVertexAttribPointer(
//                1,                  // attribute 1. No particular reason for 1, but must match the layout in the shader.
//                3,                  // size
//                GLES20.GL_FLOAT,    // type
//                false,              // normalized?
//                0,                  // stride
//                0                   // array buffer offset
//        );


    }

    @Override
    public void onDrawFrame(GL10 unused) {
//        // Redraw background color
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//
//        // Set light position
//        float[] normalized_light_dir_before_rotation = {0.0f, 0.0f, 1.0f, 0.0f};
//        // Set the camera position (View matrix)
//        Matrix.setLookAtM(myViewMatrix, 0, 0.0f, 0.0f,
//                            -4.0f, // = z_min - (z_min + z_max)/2
//                            0.0f, 0.0f, 0.0f, 0f, 1.0f, 0.0f);
//
//        // Calculate the model transformation (translates the model so it sits at the origin
//        // this method translates the matrix, it does not generate a translation matrix, DO NOT USE
////        Matrix.translateM(myTranslationMatrix, 0, -box_middle[0], -box_middle[1], -box_middle[2]);
//        // Calculate the projection and view transformation
//        Matrix.multiplyMM(myMVPMatrix, 0, myProjectionMatrix, 0, myViewMatrix, 0);
//
//        // Calculate rotation matrix
//        Matrix.setRotateM(xRotationMatrix, 0, xAngle, 1.0f, 0, 0);
//        Matrix.setRotateM(yRotationMatrix, 0, yAngle, 0, 1.0f, 0);
//        Matrix.multiplyMM(totRotationMatrix, 0, xRotationMatrix, 0, yRotationMatrix, 0);
//
//        // Concatenate translation and rotation matrices
////        Matrix.multiplyMM(transThenRotMatrix, 0, totRotationMatrix, 0, myTranslationMatrix, 0);
//
//        // Combine the rotation matrix with the projection and camera view
//        // Note that the mMVPMatrix factor *must be first* in order
//        // for the matrix multiplication product to be correct.
//        Matrix.multiplyMM(scratch, 0, myMVPMatrix, 0, totRotationMatrix, 0);
//
//        // update light position based on rotation
//        float[] new_light_dir = new float[4];
//        Matrix.multiplyMV(new_light_dir, 0, totRotationMatrix, 0, normalized_light_dir_before_rotation, 0);
//
//        for (Triangle triangle : triangles) {
//            triangle.set_color(myProgram, new_light_dir);
//        }
//
//        // get handle to shape's transformation matrix
//        myMVPMatrixHandle = GLES20.glGetUniformLocation(myProgram, "uMVPMatrix"); // should definitely be outside triangle class
//
//        // Pass the projection and view transformation to the shader
//        GLES20.glUniformMatrix4fv(myMVPMatrixHandle, 1, false, scratch, 0); //mvpMatrix, 0);
//
//        // Draw the triangle
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3 * number_of_triangles);

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(myProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(myProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(myProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(myProjectionMatrix, 0, -ratio, ratio, -1, 1, 3.0f, 7.0f);
//                (float) (bounding_box[4] + bounding_box[5]) / 4,
//                (float) (bounding_box[4] + bounding_box[5]) * 7.0f / 4);
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
}
