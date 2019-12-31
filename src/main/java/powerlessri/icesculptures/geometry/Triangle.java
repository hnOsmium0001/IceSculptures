package powerlessri.icesculptures.geometry;

import javax.vecmath.Vector3f;
import java.util.Objects;

public class Triangle {

    public static Triangle zero() {
        return new Triangle(new Vector3f(), new Vector3f(), new Vector3f());
    }

    public static Triangle of(Vector3f v0, Vector3f v1, Vector3f v2) {
        Triangle triangle = new Triangle(v0, v1, v2);
        triangle.calcNormals();
        return triangle;
    }

    public final Vector3f v0;
    public final Vector3f v1;
    public final Vector3f v2;
    public final Vector3f n0 = new Vector3f();
    public final Vector3f n1 = new Vector3f();
    public final Vector3f n2 = new Vector3f();

    private Triangle(Vector3f v0, Vector3f v1, Vector3f v2) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
    }

    public void calcNormals() {
        calcNormal(n0, v0, v1, v2);
        calcNormal(n1, v1, v0, v2);
        calcNormal(n2, v2, v0, v1);
    }

    // Internal normal calculation use only
    private static Vector3f e1 = new Vector3f();
    private static Vector3f e2 = new Vector3f();

    private void calcNormal(Vector3f res, Vector3f base, Vector3f neighbor1, Vector3f neighbor2) {
        e1.sub(neighbor1, base);
        e2.sub(neighbor2, base);
        res.cross(e2, e1);
        res.normalize();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triangle triangle = (Triangle) o;
        return v0.equals(triangle.v0) &&
                v1.equals(triangle.v1) &&
                v2.equals(triangle.v2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(v0, v1, v2);
    }
}
