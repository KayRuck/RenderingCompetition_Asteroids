package CGA.User.DataStructures.Camera;


import org.joml.*;

import java.lang.Math;

/**
 * Created by Fabian on 16.09.2017.
 */

public class FlyCamera extends Camera {
    private float aspectratio;
    private float fov;
    private float near;
    private float far;

    public FlyCamera() {
        super();
        aspectratio = 16.0f / 9.0f;
        fov = (float)Math.toRadians(90.0f);
        near = 0.1f;
        far = 100.0f;
    }

    public FlyCamera(int width,
                     int height,
                     float fov,
                     float near,
                     float far) {
        super();
        aspectratio = (float) width / (float) height;
        this.fov = fov;
        this.near = near;
        this.far = far;
    }

    @Override
    public Matrix4f getViewMatrix() {
        //we do a view matrix update only when needed
        viewMat.identity();
        viewMat = new Matrix4f().lookAt(getPosition(), getPosition().sub(getZAxis()), getYAxis());
        return super.getViewMatrix();
    }


    @Override
    public Matrix4f getProjectionMatrix() {
        projMat.identity();
        projMat.perspective(fov, aspectratio, near, far);
        return super.getProjectionMatrix();
    }

    public void setProjection(int width, int height, float fov, float near, float far) {
        aspectratio = (float) width / (float) height;
        this.near = near;
        this.far = far;
        this.fov = fov;
    }

    //movement

    public void rotateView(float yaw, float pitch) {

        //translate*pitch*oldrot*yaw
        //extract the current rotation matrix
        Matrix4f oldrot = new Matrix4f(
                new Vector4f(getXAxis(), 0.0f),
                new Vector4f(getYAxis(), 0.0f),
                new Vector4f(getZAxis(), 0.0f),
                new Vector4f(0.0f, 0.0f, 0.0f, 1.0f)
        );

        Matrix4f yawrot = new Matrix4f();
        yawrot.rotateY(yaw);

        Matrix4f pitchrot = new Matrix4f();
        pitchrot.rotateX(pitch);

        Matrix4f translate = new Matrix4f();
        translate.translate(getPosition());

        //we have 2 rotation options: Pitch = P and Yaw = Y, Translation is denoted as T
        //goal is to stack the transformations for the camera as follows
        //cameratransformation = ...TTTTTT...*...YYYYYY...*...PPPPPP...
        //first we collect all pitches, then apply the yaws.
        //translation is applied at the very end
        transformMat = translate.mul(yawrot).mul(oldrot).mul(pitchrot);
    }

    @Override
    public void left(float amount) {
        translateLocal(new Vector3f(-1.0f, 0.0f, 0.0f).mul(amount));
    }

    public void right(float amount) {
        translateLocal(new Vector3f(1.0f, 0.0f, 0.0f).mul(amount));
    }

    public void up(float amount) {
        translateGlobal(new Vector3f(0.0f, 1.0f, 0.0f).mul(amount));
    }

    public void down(float amount) {
        translateGlobal(new Vector3f(0.0f, -1.0f, 0.0f).mul(amount));
    }

    public void forward(float amount) {
        translateLocal(new Vector3f(0.0f, 0.0f, -1.0f).mul(amount));
    }

    public void backward(float amount) {
        translateLocal(new Vector3f(0.0f, 0.0f, 1.0f).mul(amount));
    }
}
