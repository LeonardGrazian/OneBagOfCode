package com.example.mohawkgroup.openglpractice;

/**
 * Created by Mohawk Group on 6/24/2016.
 * This class was originally intended to hold an ordered triple (to represent a vertex)
 * It has since been expanded to represent a float vector with 3 elements
 * It implements several vector operations like addition, subtraction, dot and cross products,
 * and normalization
 */
public class Vertex {
    private float[] vertex_coords;

    public Vertex(float x, float y, float z) {
        this.vertex_coords = new float[3];
        vertex_coords[0] = x;
        vertex_coords[1] = y;
        vertex_coords[2] = z;
    }

    public float getX() { return vertex_coords[0]; }
    public float getY() { return vertex_coords[1]; }
    public float getZ() { return vertex_coords[2]; }

    public void normalize() {
        double size = Math.sqrt(Math.pow(vertex_coords[0], 2)
                        + Math.pow(vertex_coords[1], 2)
                        + Math.pow(vertex_coords[2], 2));
        for (int i = 0; i < 3; i++) {
            vertex_coords[i] /= size;
        }
    }

    public Vertex(double x, double y, double z) {
        this((float) x, (float) y, (float) z);
    }

    public void translate(Vertex v) {
        vertex_coords[0] += v.getX();
        vertex_coords[1] += v.getY();
        vertex_coords[2] += v.getZ();
    }

    static public float dot(Vertex v0, Vertex v1) {
        return v0.getX() * v1.getX() + v0.getY() * v1.getY() + v0.getZ() * v1.getZ();
    }

    static public Vertex cross(Vertex v0, Vertex v1) {
        return new Vertex(v0.getY() * v1.getZ() - v0.getZ() * v1.getY(),
                            v0.getZ() * v1.getX() - v0.getX() * v1.getZ(),
                            v0.getX() * v1.getY() - v0.getY() * v1.getX());
    }

    static public Vertex add(Vertex v0, Vertex v1) {
        return new Vertex(v0.getX() + v1.getX(),
                v0.getY() + v1.getY(),
                v0.getZ() + v1.getZ());
    }

    // returns v0 - v1
    static public Vertex subtract(Vertex v0, Vertex v1) {
        return Vertex.add(v0, Vertex.minus(v1));
    }

    static public Vertex minus(Vertex v) {
        return new Vertex(-v.getX(), -v.getY(), -v.getZ());
    }
}