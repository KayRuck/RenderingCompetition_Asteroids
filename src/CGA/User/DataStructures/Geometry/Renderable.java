package CGA.User.DataStructures.Geometry;

import CGA.User.DataStructures.Shader;
import org.joml.Matrix4f;

import java.util.ArrayList;

/**
 * Created by Fabian on 19.09.2017.
 */

/**
 * Extends Transformable such that the object can render Mesh objects transformed by Transformable
 */
public class Renderable extends Transformable
{
    /**
     * List of meshes attached to this renderable object
     */
    public ArrayList<Mesh> meshes;

    /**
     * creates an empty renderable object with an empty mesh list
     */
    public Renderable()
    {
        super();
        meshes = new ArrayList<>();
    }

    public Renderable(ArrayList<Mesh> meshes)
    {
        super();
        meshes = new ArrayList<>();
        meshes.addAll(meshes);
    }

    /**
     * Renders all meshes attached to this Renderable, applying the transformation matrix to
     * each of them
     * @param shader    The shader used to render the meshes. Must contain a mat4 uniform with name "model_matrix"
     */
    public void render(Shader shader)
    {
        shader.setUniform("model_matrix", transformMat, false);
        for(Mesh m : meshes)
        {
            m.render(shader);
        }
    }

    public void render(Shader shader, Transformable[] parents)
    {
        Matrix4f mat = new Matrix4f();
        for (Transformable t : parents)
        {
            mat.mul(t.transformMat);
        }

        shader.setUniform("model_matrix", mat.mul(transformMat), false);
        for(Mesh m : meshes)
        {
            m.render(shader);
        }
    }
}
