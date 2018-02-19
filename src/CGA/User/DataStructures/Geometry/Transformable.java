package CGA.User.DataStructures.Geometry;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Fabian on 16.09.2017.
 */
public class Transformable {
    public Matrix4f transformMat = new Matrix4f();

    public Transformable() {
        transformMat = new Matrix4f();
    }

    public void rotateLocal(Vector3f rotation) {
        transformMat.rotateXYZ(rotation);
    }

    public void rotateLocal(float pitch, float yaw, float roll) {
        transformMat.rotateXYZ(pitch, yaw, roll);
    }

    public void rotateLocal(Vector3f axis, float angle) {
        transformMat.rotate(angle, axis);
    }

    public void rotateAroundPoint(Vector3f rotation, Vector3f altMidpoint) {
        Matrix4f tmp = new Matrix4f();
        tmp.translate(altMidpoint);
        tmp.rotateXYZ(rotation);
        tmp.translate(new Vector3f(altMidpoint).negate());
        transformMat = tmp.mul(transformMat);
    }

    public void rotateAroundPoint(float pitch, float yaw, float roll, Vector3f altMidpoint) {
        Matrix4f tmp = new Matrix4f();
        tmp.translate(altMidpoint);
        tmp.rotateXYZ(pitch, yaw, roll);
        tmp.translate(new Vector3f(altMidpoint).negate());
        transformMat = tmp.mul(transformMat);
    }

    public void rotateAroundPoint(Vector3f axis, float angle, Vector3f altMidpoint) {
        Matrix4f tmp = new Matrix4f();
        tmp.translate(altMidpoint);
        tmp.rotate(angle, axis);
        tmp.translate(new Vector3f(altMidpoint).negate());
        tmp.mul(transformMat, transformMat);
    }

    public void translateLocal(Vector3f deltaPos) {
        transformMat.translate(deltaPos);
    }

    public void translateGlobal(Vector3f deltaPos) {
        transformMat = (new Matrix4f().translate(deltaPos)).mul(transformMat);
    }

    public void scaleLocal(Vector3f scale) {
        transformMat.scale(scale);
    }

    public void scaleAroundPoint(Vector3f scale, Vector3f point) {
        Matrix4f tmp = new Matrix4f();
        tmp.translate(point);
        tmp.scale(scale);
        tmp.translate(new Vector3f(point).negate());
        transformMat = tmp.mul(transformMat);
    }

    public Vector3f getPosition() {
        return new Vector3f(transformMat.m30(), transformMat.m31(), transformMat.m32());
    }

    public Vector3f getXAxis() {
        return new Vector3f(
                transformMat.m00(), transformMat.m01(), transformMat.m02()
        ).normalize();
    }

    public Vector3f getYAxis() {
        return new Vector3f(
                transformMat.m10(), transformMat.m11(), transformMat.m12()
        ).normalize();
    }

    public Vector3f getZAxis() {
        return new Vector3f(
                transformMat.m20(), transformMat.m21(), transformMat.m22()
        ).normalize();
    }
}
