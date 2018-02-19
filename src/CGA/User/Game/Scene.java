package CGA.User.Game;

import CGA.Framework.GameWindow;
import CGA.Framework.OBJLoader;
import CGA.User.DataStructures.*;
import CGA.User.DataStructures.Camera.FlyCamera;
import CGA.User.DataStructures.Geometry.Mesh;
import CGA.User.DataStructures.Geometry.Renderable;
import CGA.User.DataStructures.Geometry.Transformable;
import CGA.User.DataStructures.Geometry.VertexAttribute;
import CGA.User.DataStructures.Light.PointLight;
import org.joml.*;
import org.joml.Math;

import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Fabian on 16.09.2017.
 */
public class Scene {
    //Mesh mesh;
    private Shader shader;
    private GameWindow window;

    private Renderable orbRend, ringRend;

    private Mesh orbMesh, ringMesh;

    //Lights
    private PointLight orb_light;
    private PointLight ring_light;

    private Texture2D orb_diff, orb_spec, orb_emit;
    private Texture2D ring_diff, ring_spec, ring_emit;
    private Texture2D flashlighttex;
    private MatrixStackf m_orb;

    //camera

    private FlyCamera camera;

    private float flashlight;

    public Scene(GameWindow window) {
        this.window = window;
    }

    //scene setup
    public boolean init() {
        try {
            //Load shader
            shader = new Shader("assets/shaders/vertex.glsl", "assets/shaders/fragment.glsl");

            //load Orb textures
            orb_diff = new Texture2D("assets/textures/orb_diff.png", true);
            orb_spec = new Texture2D("assets/textures/orb_spec.png", true);
            orb_emit = new Texture2D("assets/textures/orb_emit.png", true);

            orb_diff.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
            orb_spec.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
            orb_emit.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);

            ring_diff = new Texture2D("assets/textures/ruin_diff.png", true);
            ring_spec = new Texture2D("assets/textures/ruin_spec.png", true);
            ring_emit = new Texture2D("assets/textures/ruin_emit.png", true);

            ring_diff.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
            ring_spec.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
            ring_emit.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);

            flashlighttex = new Texture2D("assets/textures/flashlight.png", true);
            flashlighttex.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
            flashlight = 0.0f;

            //load an object and create a mesh
            OBJLoader.OBJResult resOrb  = OBJLoader.loadOBJ("assets/models/sphere.obj", true, true);
            OBJLoader.OBJResult resRing = OBJLoader.loadOBJ("assets/models/ring.obj", true, true);

            //Create the mesh
            VertexAttribute[] vertexAttributes = new VertexAttribute[3];
            int stride = 8 * 4;
            vertexAttributes[0] = new VertexAttribute(3, GL_FLOAT, stride, 0);      //position attribute
            vertexAttributes[1] = new VertexAttribute(2, GL_FLOAT, stride, 3 * 4);  //texture coordinate attribut
            vertexAttributes[2] = new VertexAttribute(3, GL_FLOAT, stride, 5 * 4);  //normal attribute

            //Create renderable
            orbRend = new Renderable();

            for (OBJLoader.OBJMesh m : resOrb.objects.get(0).meshes) {
                Mesh mesh = new Mesh(m.getVertexData(), m.getIndexData(), vertexAttributes, orb_diff, orb_spec, orb_emit, 20.0f);
                orbRend.meshes.add(mesh);
            }

            orbRend.scaleLocal(new Vector3f(0.3f));

            ringRend = new Renderable();

            for (OBJLoader.OBJMesh m : resRing.objects.get(0).meshes) {
                Mesh mesh = new Mesh(m.getVertexData(), m.getIndexData(), vertexAttributes, ring_diff, ring_spec, ring_emit, 5.0f);
                ringRend.meshes.add(mesh);
            }

            //light setup

            orb_light  = new PointLight(new Vector3f(1.0f, 1.0f, 160.0f / 255.0f), new Vector3f(0.3f, 1.7f, 1.6f));
            ring_light = new PointLight(new Vector3f(1.0f, 1.0f, 160.0f / 255.0f), new Vector3f(0.3f, 1.7f, 1.6f)); // TODO: Anpassen


            //setup camera
            camera = new FlyCamera(
                    window.getFramebufferWidth(),
                    window.getFramebufferHeight(),
                    (float) Math.toRadians(90.0),
                    0.1f,
                    100.0f
            );

            //move camera a little bit in z direction
            camera.translateGlobal(new Vector3f(0.0f, 2.0f, 6.0f));
            camera.forward(3.0f);
            camera.down(0.5f);


            //initial opengl state
            shader.use();
            glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

            glEnable(GL_CULL_FACE);
            glFrontFace(GL_CCW);
            glCullFace(GL_BACK);

            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LESS);

            return true;
        }   catch (Exception ex) {
            System.err.println("Scene initialization failed:\n" + ex.getMessage() + "\n");
            ex.printStackTrace();
            return false;
        }

    }

    public void render(float dt) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        shader.use();
        flashlighttex.bind(3);
        shader.setUniform("flashlightTex", 3);
        shader.setUniform("flashlightFactor", flashlight);
        shader.setUniform("screensize", new Vector2f((float) window.getFramebufferWidth(), (float) window.getFramebufferHeight()));

        // render Camera
        shader.setUniform("view_matrix", camera.getViewMatrix(), false);
        shader.setUniform("proj_matrix", camera.getProjectionMatrix(), false);

        orbRend.render(shader);  // zweites Objekt (ring) wird nihct angezeigt

        orb_light.bind(shader, "light");
        shader.setUniform("uvMultiplier", 1.0f);

        // TODO richtig machen
//        ringRend.render(shader);

        Transformable[] t = {orbRend}; // TODO Orb oder Orb_light? Muss ein Rendable sein
        ringRend.render(shader, t);
//        ringRend.translateGlobal( new Vector3f(-ringRend.getPosition().x, -ringRend.getPosition().y, -ringRend.getPosition().z ));

    }

    public void update(float dt) {
        //camera update
        float movemul = 0.75f;
        if (window.getKeyState(GLFW_KEY_C)) {
            camera.down(movemul * dt);
        }
        if (window.getKeyState(GLFW_KEY_SPACE)) {
            camera.up(movemul * dt);
        }
//        if (window.getKeyState(GLFW_KEY_W)) {
//            camera.forward(movemul * dt);
//        }
//        if (window.getKeyState(GLFW_KEY_A)) {
//            camera.left(movemul * dt);
//        }
//        if (window.getKeyState(GLFW_KEY_S)) {
//            camera.backward(movemul * dt);
//        }
//        if (window.getKeyState(GLFW_KEY_D)) {
//            camera.right(movemul * dt);
//        }

        if (window.getKeyState(GLFW_KEY_UP)) {
            orbRend.translateGlobal(new Vector3f(0.0f, 1.0f * dt, 0.0f));
        }
        if (window.getKeyState(GLFW_KEY_DOWN)) {
            orbRend.translateGlobal(new Vector3f(0.0f, -1.0f * dt, 0.0f));
        }


        //TODO Ist das mit Orb light richtig?

//        if (window.getKeyState(GLFW_KEY_UP)) {
//            orb_light.translateGlobal(new Vector3f(0.0f, 0.0f, -1.0f * dt));
//        }
//        if (window.getKeyState(GLFW_KEY_DOWN)) {
//            orb_light.translateGlobal(new Vector3f(0.0f, 0.0f, 1.0f * dt));
//        }
        if (window.getKeyState(GLFW_KEY_LEFT)) {
            orb_light.translateGlobal(new Vector3f(-1.0f * dt, 0.0f, 0.0f));
        }
        if (window.getKeyState(GLFW_KEY_RIGHT)) {
            orb_light.translateGlobal(new Vector3f(1.0f * dt, 0.0f, 0.0f));
        }

        if (window.getKeyState(GLFW_KEY_I)) {
            orb_light.translateGlobal(new Vector3f(0.0f, 1.0f * dt, 0.0f));
        }
        if (window.getKeyState(GLFW_KEY_K)) {
            orb_light.translateGlobal(new Vector3f(0.0f, -1.0f * dt, 0.0f));
        }
    }

    public void onKey(int key, int scancode, int action, int mode) {
        if (key == GLFW_KEY_L && action == GLFW_PRESS) {
            flashlight = flashlight == 0.0f ? 1.0f : 0.0f;
        }
    }


    public void cleanup() {}
}
