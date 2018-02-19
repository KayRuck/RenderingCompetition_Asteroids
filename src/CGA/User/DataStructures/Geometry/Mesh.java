package CGA.User.DataStructures.Geometry;

import CGA.User.DataStructures.Shader;
import CGA.User.DataStructures.Texture2D;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Created by Fabian on 16.09.2017.
 */
public class Mesh {

    //private data
    private int vao = 0;
    private int vbo = 0;
    private int ibo = 0;

    private int indexcount = 0;

    //Material
    private Texture2D diff = null;
    private Texture2D spec = null;
    private Texture2D emit = null;

    private float shininess;

    /**
     * Creates a Mesh object from vertexdata, intexdata and a given set of vertex attributes
     *
     * @param vertexdata plain float array of vertex data
     * @param indexdata  index data
     * @param attributes vertex attributes contained in vertex data
     * @throws Exception If the creation of the required OpenGL objects fails, an exception is thrown
     */
    private Mesh(float[] vertexdata, int[] indexdata, VertexAttribute[] attributes) throws Exception {
        vao = glGenVertexArrays();
        if (vao == 0) {
            throw new Exception("Vertex array object creation failed.");
        }
        vbo = glGenBuffers();
        if (vbo == 0) {
            glDeleteVertexArrays(vao);
            throw new Exception("Vertex buffer creation failed.");
        }
        ibo = glGenBuffers();
        if (ibo == 0) {
            glDeleteVertexArrays(vao);
            glDeleteBuffers(vbo);
            throw new Exception("Index buffer creation failed.");
        }

        glBindVertexArray(vao);
        //---------------------- VAO state setup start ----------------------
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);

        //buffer data
        glBufferData(GL_ARRAY_BUFFER, vertexdata, GL_STATIC_DRAW);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexdata, GL_STATIC_DRAW);

        int attid = 0;
        for (int i = 0; i < attributes.length; i++) {
            glEnableVertexAttribArray(i);
            glVertexAttribPointer(
                    i,
                    attributes[i].n,
                    attributes[i].type,
                    false,
                    attributes[i].stride,
                    attributes[i].offset
            );
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        //--------------------- VAO state setup end --------------------
        glBindVertexArray(0);

        indexcount = indexdata.length;
    }

    public Mesh(float[] vertexdata, int[] indexdata, VertexAttribute[] attributes, Texture2D difftex, Texture2D spectex, Texture2D emittex, float shininess) throws Exception {
        this(vertexdata, indexdata, attributes);
        diff = difftex;
        spec = spectex;
        emit = emittex;
        this.shininess = shininess;
    }

    //Only send the geometry to the gpu

    /**
     * renders the mesh
     */
    private void render() {
        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, indexcount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public void render(Shader shader) {
        diff.bind(0);
        shader.setUniform("diff", 0);
        spec.bind(1);
        shader.setUniform("spec", 1);
        emit.bind(2);
        shader.setUniform("emit", 2);
        shader.setUniform("shininess", shininess);
        render();
    }

    /**
     * Deletes the previously allocated OpenGL objects for this mesh
     */
    public void cleanup() {
        if (ibo != 0)
            glDeleteBuffers(ibo);
        if (vbo != 0)
            glDeleteBuffers(vbo);
        if (vao != 0)
            glDeleteVertexArrays(vao);
    }
}
