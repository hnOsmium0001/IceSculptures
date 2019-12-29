package powerlessri.icesculptures.geometry;

import javax.vecmath.Vector3f;

public class Triangle {

    public static Triangle zero() {
        Triangle triangle = new Triangle();
        triangle.v0 = new Vector3f();
        triangle.v1 = new Vector3f();
        triangle.v2 = new Vector3f();
        return triangle;
    }

    public static Triangle empty() {
        return new Triangle();
    }

    public Vector3f v0, v1, v2;

    private Triangle() {
    }
}
