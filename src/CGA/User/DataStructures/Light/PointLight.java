package CGA.User.DataStructures.Light;

import CGA.User.DataStructures.Camera.Camera;
import CGA.User.DataStructures.Geometry.Transformable;
import CGA.User.DataStructures.Shader;
import org.joml.Vector3f;
import org.joml.Vector4f;


public class PointLight extends Transformable implements ILight {
    public Vector3f lightColor;

    public PointLight(Vector3f lightColor, Vector3f position) {
        super();
        this.lightColor = lightColor;
        translateGlobal(position);
    }

    @Override
    public void bind(Shader shader, String name) {
        shader.setUniform(name + "Color", lightColor);
        shader.setUniform(name + "Position", getPosition());
    }
}
