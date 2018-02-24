package CGA.User.Game;

import CGA.Framework.GameWindow;
import CGA.Framework.OBJLoader;
import CGA.User.DataStructures.*;
import CGA.User.DataStructures.Camera.FlyCamera;
import CGA.User.DataStructures.Geometry.Mesh;
import CGA.User.DataStructures.Geometry.Renderable;
import CGA.User.DataStructures.Geometry.VertexAttribute;
import CGA.User.DataStructures.Light.PointLight;
import org.joml.*;
import org.joml.Math;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Fabian on 16.09.2017.
 */
public class Scene {

    private Shader shader;
    private GameWindow window;

    //Renderables
    private Renderable orbRend, ringRend;
    private ArrayList<Renderable> cometRend = new ArrayList<>();
    public static final int COMETCOUNT = 15 ;

    //Lights
    private PointLight orb_light;
    private PointLight ring_light;

    //Textures
    private Texture2D orb_diff, orb_spec, orb_emit;
    private Texture2D ring_diff, ring_spec, ring_emit;
    private Texture2D comet_diff, comet_spec, comet_emit;
    private Texture2D flashlighttex;

    //camera
    private FlyCamera camera;

    //flashlight
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

            //Load Ring textures
            ring_diff = new Texture2D("assets/textures/ruin_diff.png", true);
            ring_spec = new Texture2D("assets/textures/ruin_spec.png", true);
            ring_emit = new Texture2D("assets/textures/ruin_emit.png", true);

            ring_diff.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
            ring_spec.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
            ring_emit.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);

            //Load Comet textures
            comet_diff = new Texture2D("assets/textures/comet.jpg", true);

            flashlighttex = new Texture2D("assets/textures/flashlight.png", true);
            flashlighttex.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
            flashlight = 0.0f;

            //load an object and create a mesh
            OBJLoader.OBJResult resOrb    = OBJLoader.loadOBJ("assets/models/sphere.obj", true, true);
            OBJLoader.OBJResult resRing   = OBJLoader.loadOBJ("assets/models/ring.obj", true, true);
            OBJLoader.OBJResult resCom    = OBJLoader.loadOBJ("assets/models/sphere.obj", true, true);

            //Create the mesh
            VertexAttribute[] vertexAttributes = new VertexAttribute[3];
            int stride = 8 * 4;
            vertexAttributes[0] = new VertexAttribute(3, GL_FLOAT, stride, 0);      //position attribute
            vertexAttributes[1] = new VertexAttribute(2, GL_FLOAT, stride, 3 * 4);  //texture coordinate attribut
            vertexAttributes[2] = new VertexAttribute(3, GL_FLOAT, stride, 5 * 4);  //normal attribute

            //create renderable (orb)
            orbRend = new Renderable();

            for (OBJLoader.OBJMesh m : resOrb.objects.get(0).meshes) {
                Mesh mesh = new Mesh(m.getVertexData(), m.getIndexData(), vertexAttributes, orb_diff, orb_spec, orb_emit, 20.0f);
                orbRend.meshes.add(mesh);
            }

            orbRend.scaleLocal(new Vector3f(0.2f));

            //create renderable (ring)
            ringRend = new Renderable();

            for (OBJLoader.OBJMesh m : resRing.objects.get(0).meshes) {
                Mesh mesh = new Mesh(m.getVertexData(), m.getIndexData(), vertexAttributes, ring_diff, ring_spec, ring_emit, 5.0f);
                ringRend.meshes.add(mesh);
            }

            //create renderables (comets)
            for (int i = 0; i < COMETCOUNT; i++) {
                cometRend.add(new Renderable());
            }

            for (OBJLoader.OBJMesh m : resCom.objects.get(0).meshes) {
                Mesh mesh = new Mesh(m.getVertexData(), m.getIndexData(), vertexAttributes, comet_diff, comet_diff, comet_diff, 5.0f);
                for (Renderable r : cometRend) {
                    r.meshes.add(mesh);
                }
            }

            // TODO cometen größe und position festlegen
            //comet size
            for (Renderable r : cometRend)
                r.scaleLocal(new Vector3f(0.5f, 0.5f, 0.5f));

            // randwerte: x: +/- 3.3f
            //            y: +/- 1.9f
            cometRend.get(0).translateGlobal(new Vector3f( -3.5f,2.0f,  -3.0f));
            cometRend.get(1).translateGlobal(new Vector3f( 3.8f, 1.6f,  -2.4f));
            cometRend.get(2).translateGlobal(new Vector3f( 0.9f, -1.7f, -2.0f));
            cometRend.get(3).translateGlobal(new Vector3f( 0.4f, -2.5f, -105.0f));
            cometRend.get(4).translateGlobal(new Vector3f( 1.0f, 3.4f, -10.0f));
            cometRend.get(5).translateGlobal(new Vector3f( 4.5f, 4.8f, -20.0f));
            cometRend.get(6).translateGlobal(new Vector3f( -1.4f, -3.2f, -1.0f));

            cometRend.get(7).translateGlobal(new Vector3f( -3.5f,2.0f,  -125.0f));
            cometRend.get(8).translateGlobal(new Vector3f( 3.8f, 1.6f,  -15.4f));
            cometRend.get(9).translateGlobal(new Vector3f( 0.9f, -1.7f, -20.0f));
            cometRend.get(10).translateGlobal(new Vector3f( 0.4f, -2.5f, -15.0f));
            cometRend.get(11).translateGlobal(new Vector3f( 1.0f, 3.4f, -10.0f));
            cometRend.get(12).translateGlobal(new Vector3f( 4.5f, 4.8f, -20.0f));
            cometRend.get(13).translateGlobal(new Vector3f( -1.4f, -3.2f, -5.0f));
            cometRend.get(14).translateGlobal(new Vector3f( -1.4f, -3.2f, -15.0f));

            //light setup
            // TODO Kugel- und Ringfarbe ändern, mit Paint die Textur ändern -> neue Farbe (orb_emitt.png)
            orb_light  = new PointLight(new Vector3f(1.0f, 1.0f, 160.0f / 255.0f), new Vector3f(0.3f, 1.7f, 1.6f));
            // TODO: später Farbe Anpassen            \/ RBG Wert von 0 - 1
            ring_light = new PointLight(new Vector3f(0.5f, 0.5f, 0.5f ), new Vector3f(0.3f, 1.7f, 1.6f));

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

        //render camera
        shader.setUniform("view_matrix", camera.getViewMatrix(), false);
        shader.setUniform("proj_matrix", camera.getProjectionMatrix(), false);

        //render objects
        orbRend.render(shader);  // zweites Objekt (ring) wird nicht angezeigt
        ringRend.render(shader); // TODO Transformation abhängig machen
        for (Renderable r : cometRend) {
            r.render(shader);
        }


        orb_light.bind(shader, "light");
        ring_light.bind(shader, "light");
        shader.setUniform("uvMultiplier", 1.0f);


        // TODO warum funktioniert dies nicht?
//        Transformable[] t = {orbRend};
//        ringRend.render(shader, t);

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

        //orb update // TODO Bewegung auf sinnvollen radius einschränken
        if (window.getKeyState(GLFW_KEY_UP)) {
            orbRend.translateGlobal(new Vector3f(0.0f, 1.0f * dt, 0.0f));
            ringRend.translateGlobal(new Vector3f(0.0f, 1.0f * dt, 0.0f));
        }
        if (window.getKeyState(GLFW_KEY_DOWN)) {
            orbRend.translateGlobal(new Vector3f(0.0f, -1.0f * dt, 0.0f));
            ringRend.translateGlobal(new Vector3f(0.0f, -1.0f * dt, 0.0f));
        }
        if (window.getKeyState(GLFW_KEY_LEFT)) {
            orbRend.translateGlobal(new Vector3f(-1.0f * dt, 0.0f, 0.0f));
            ringRend.translateGlobal(new Vector3f(-1.0f * dt, 0.0f, 0.0f));
        }
        if (window.getKeyState(GLFW_KEY_RIGHT)) {
            orbRend.translateGlobal(new Vector3f(1.0f * dt, 0.0f, 0.0f));
            ringRend.translateGlobal(new Vector3f(1.0f * dt, 0.0f, 0.0f));
        }


        //comet update/movement //TODO
        boolean hit = false;
        for (Renderable r : cometRend) {
            if (r.getPosition().z < 3.0f) {
                r.translateGlobal(new Vector3f(0.0f, 0.0f, 1.0f * dt)); // TODO Geschwindigkeit anpassen (über Zeit schneller werden)
                hit = hitDetection(r.getPosition());
            }
            else {
                //reset comet position
                r.translateGlobal(new Vector3f(-r.getPosition().x, -r.getPosition().y, -r.getPosition().z));

                //comet new position
                float x = (float) (java.lang.Math.random() * 3.4f);// randwerte: x: +/- 3.3f
                float y = (float) (java.lang.Math.random() * 1.9f);//            y: +/- 1.9f
                float z = ((float) (java.lang.Math.random() * 7.0f) + 2.0f) * -1;

                if ((int) (java.lang.Math.random() * 2) >= 1) x *= -1;
                if ((int) (java.lang.Math.random() * 2) >= 1) y *= -1;

                r.translateGlobal(new Vector3f(x, y, z));
            }
        }
        if (!hit) {
            System.out.println("-------");
        }
    }

    public void onKey(int key, int scancode, int action, int mode) {
        if (key == GLFW_KEY_L && action == GLFW_PRESS) {
            flashlight = flashlight == 0.0f ? 1.0f : 0.0f;
        }
    }


    public void cleanup() {}

    private boolean hitDetection(Vector3f comet){

        Vector3f ufo = orbRend.getPosition();
        float ufoRadius = 0.8f;
        float comRadius = 0.5f;

        float dx = ufo.x - comet.x;
        float dy = ufo.y - comet.y;
        float dz = ufo.z - comet.z;

        float distance = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);

        if (distance <= (ufoRadius + comRadius)) {
            // hit --> game over
            System.out.println("HIT");
            // TODO Programm beenden
            return true;
        }
        return false;
    }

}
