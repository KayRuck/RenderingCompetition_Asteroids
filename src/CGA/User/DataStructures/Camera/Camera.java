package CGA.User.DataStructures.Camera;

import CGA.User.DataStructures.Geometry.Transformable;
import org.joml.Matrix4f;

/**
 * Created by Fabian on 19.09.2017.
 */
public abstract class Camera extends Transformable {
    protected Matrix4f viewMat;
    protected Matrix4f projMat;

    protected Camera() {
        super();
        viewMat = new Matrix4f();
        projMat = new Matrix4f();
    }

    public Matrix4f getViewMatrix() {
        return viewMat;
    }

    public Matrix4f getProjectionMatrix() {
        return projMat;
    }

    public abstract void left(float amount);

    public abstract void right(float amount);

    public abstract void up(float amount);

    public abstract void down(float amount);

    public abstract void forward(float amount);

    public abstract void backward(float amount);
}
