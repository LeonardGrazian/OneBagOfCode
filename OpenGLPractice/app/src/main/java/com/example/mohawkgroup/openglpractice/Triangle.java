package com.example.mohawkgroup.openglpractice;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Mohawk Group on 6/24/2016.
 * Represents a triangle, with three Vertex objects representing its vertices.
 * Contains color information and shaders for the triangle
 * Most importantly, it handles rendering of itself in the draw method
 * TODO: should draw() be moved to MyRenderer?
 */
class Triangle {
    static final int COORDS_PER_VERTEX = 3; // number of coordinates per vertex in this array
    private Vertex[] triangleCoords;
    private Vertex normal;
    private FloatBuffer vertexBuffer;

    private int myProgram;

    private int myPositionHandle;
    private int myColorHandle;
    private int myMVPMatrixHandle; // Use to access and set the view transformation

    private final int vertexCount = 3; // 3 vertics per triangle
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per coord (float)

    // Set color with red, green, blue and alpha (opacity) values
    float base_color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
    float[] color = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f};

    public Triangle(Vertex v0, Vertex v1, Vertex v2) {
        triangleCoords = new Vertex[3];
        triangleCoords[0] = v0;
        triangleCoords[1] = v1;
        triangleCoords[2] = v2;
        normal = Vertex.cross(Vertex.subtract(v1, v0),
                                Vertex.subtract(v2, v1));
        normal.normalize();
    }

    public Triangle(double[][] vertices) {
        this(new Vertex(vertices[0][0], vertices[0][1], vertices[0][2]),
                new Vertex(vertices[1][0], vertices[1][1], vertices[1][2]),
                new Vertex(vertices[2][0], vertices[2][1], vertices[2][2]));
    }

    public void init() {


        // initialize vertex byte buffer for shape coordinates
        // (vertices per triangle * floats per vertex * 4 bytes per float)
        ByteBuffer bb = ByteBuffer.allocateDirect(triangleCoords.length * COORDS_PER_VERTEX * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        float[] vertexDataArray = VertexArrayToFloatArray(triangleCoords);
        vertexBuffer.put(vertexDataArray);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);



        //



        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(myPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(myPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer); // loads in position to GPU memory
    }

    // pass mvp matrix for positioning, pass normalized_light_dir for lambertian shading
    public void set_color(int myProgram, float[] light_dir_array) {

//        // change color according to lambert factor
//        Vertex normalized_light_dir = new Vertex(light_dir_array[0],
//                                                    light_dir_array[1],
//                                                    light_dir_array[2]);
//        normalized_light_dir.normalize();
//        float lambert_factor = Math.max(-Vertex.dot(normal, normalized_light_dir), 0); // ranges from 0 to 1
////        lambert_factor = 1.0f;
//        for (int i = 0; i < 3; i++) {
//            color[i] = base_color[i] * lambert_factor;
//        }



        // get handle to fragment shader's vColor member
        myColorHandle = GLES20.glGetUniformLocation(myProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(myColorHandle, 1, color, 0); // load color into GPU memory

//        // Disable vertex array
//        GLES20.glDisableVertexAttribArray(myPositionHandle);
    }

    public void translate(Vertex v) {
        for (int i = 0; i < 3; i++) {
            triangleCoords[i].translate(v);
        }
    }

    public float[] getCoordsAsArray() {
        return VertexArrayToFloatArray(this.triangleCoords);
    }

    // for each vertex, copy vertex coords into array
    static private float[] VertexArrayToFloatArray(Vertex[] vArray) {
        int number_of_vertices = vArray.length;
        float[] fArray = new float[number_of_vertices * 3];
        for (int i = 0; i < number_of_vertices; i++) {
            fArray[(3 * i)] = vArray[i].getX();
            fArray[(3 * i) + 1] = vArray[i].getY();
            fArray[(3 * i) + 2] = vArray[i].getZ();
        }
        return fArray;
    }
}
