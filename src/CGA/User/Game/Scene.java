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

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Fabian on 16.09.2017.
 * Edited by Team A (Kay Ruck, Philipp Schmeier, Merle Struckmann)
 */
public class Scene {

    private Shader shader;
    private GameWindow window;

    //Renderables
    private Renderable ufoRend, ringRend, bgRend, winRend, loseRend, lifeRend, timeRend;
    private ArrayList<Renderable> cometRend = new ArrayList<>();
    public static final int COMETCOUNT = 15;

    //Lights
    private PointLight ufo_light;
    private PointLight ring_light;

    //Textures
    private Texture2D ufo_diff, ufo_spec, ufo_emit;
    private Texture2D ringTex, cometTex, backgoundTex, alienTex, winTex, loseTex, lifeTex, timeTex;

    //camera
    private FlyCamera camera;

    //alieneffekt
    private float alien;

    //game logic
    private boolean gameOver    = false;
    private boolean gameOverMsg = false;
    private boolean win         = false;
    private boolean winMsg      = false;
    private int lifeCount = 3;
    private float invulnTime = 0;
    public static boolean CONSOLE_LOG = false;

    //time
    private float time = 0.0f;
    private final float MAXTIME = 60;

    public Scene(GameWindow window) {
        this.window = window;
    }

    //scene setup
    public boolean init() {
        try {
            //Load shader
            shader = new Shader("assets/shaders/vertex.glsl", "assets/shaders/fragment.glsl");

            //load Orb textures
            ufo_diff = new Texture2D("assets/textures/ufo_diff.jpg", true);
            ufo_spec = new Texture2D("assets/textures/ufo_spec.png", true);
            ufo_emit = new Texture2D("assets/textures/orb_emit.png", true);

            ufo_diff.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
            ufo_spec.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
            ufo_emit.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);

            //Load Ring textures
            ringTex = new Texture2D("assets/models/ringDingDing.jpg", true);
            ringTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);

            //Load Comet textures
            cometTex = new Texture2D("assets/textures/comet.jpg", true);
            cometTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);

            //Load background textures
            backgoundTex = new Texture2D("assets/textures/star.jpg", true);
            backgoundTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);

            //Alien texture
            alienTex = new Texture2D("assets/textures/alien.png", true);
            alienTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
            alien = 0.5f;

            //Win Texture
            winTex = new Texture2D("assets/textures/winScreen.png", true);
            winTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);

            //Lose Texture
            loseTex = new Texture2D("assets/textures/loseScreen.png", true);
            loseTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);

            //Life Texture
            lifeTex = new Texture2D("assets/textures/lifes.png", true);
            lifeTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);

            //Time Texture
            timeTex = new Texture2D("assets/textures/time.png", true);
            timeTex.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);

            //load an object via OBJLoader
            OBJLoader.OBJResult resOrb, resRing, resCom, resBG, resWin, resLose, resLife, resTime;

            resOrb  = OBJLoader.loadOBJ("assets/models/sphere.obj", true, true);
            resRing = OBJLoader.loadOBJ("assets/models/ringDingDing.obj", true, true);
            resCom  = OBJLoader.loadOBJ("assets/models/texComet.obj", true, true);
            resBG   = OBJLoader.loadOBJ("assets/models/background.obj", true, true);
            resWin  = OBJLoader.loadOBJ("assets/models/background.obj", true, true);
            resLose = OBJLoader.loadOBJ("assets/models/background.obj", true, true);
            resLife = OBJLoader.loadOBJ("assets/models/background.obj", true, true);
            resTime = OBJLoader.loadOBJ("assets/models/background.obj", true, true);

            //Create the meshes

            //define vertex attributes
            VertexAttribute[] vertexAttributes = new VertexAttribute[3];
            int stride = 8 * 4;
            vertexAttributes[0] = new VertexAttribute(3, GL_FLOAT, stride, 0);      //position attribute
            vertexAttributes[1] = new VertexAttribute(2, GL_FLOAT, stride, 3 * 4);  //texture coordinate attribut
            vertexAttributes[2] = new VertexAttribute(3, GL_FLOAT, stride, 5 * 4);  //normal attribute

            //create renderable (orb)
            ufoRend = new Renderable();

            for (OBJLoader.OBJMesh m : resOrb.objects.get(0).meshes) {
                Mesh mesh = new Mesh(m.getVertexData(), m.getIndexData(), vertexAttributes, ufo_diff, ufo_spec, ufo_emit, 20.0f);
                ufoRend.meshes.add(mesh);
            }

            ufoRend.scaleLocal(new Vector3f(0.35f));

            //create renderable (ring)
            ringRend = new Renderable();

            for (OBJLoader.OBJMesh m : resRing.objects.get(0).meshes) {
                Mesh mesh = new Mesh(m.getVertexData(), m.getIndexData(), vertexAttributes, ringTex, ringTex, ringTex, 5.0f);
                ringRend.meshes.add(mesh);
            }

            ringRend.scaleLocal(new Vector3f(3f));

            //create renderables (comets)
            for (int i = 0; i < COMETCOUNT; i++) {
                cometRend.add(new Renderable());
            }

            for (OBJLoader.OBJMesh m : resCom.objects.get(0).meshes) {
                Mesh mesh = new Mesh(m.getVertexData(), m.getIndexData(), vertexAttributes, cometTex, cometTex, cometTex, 5.0f);
                for (Renderable r : cometRend) {
                    r.meshes.add(mesh);
                }
            }

            //comet size
            for (Renderable r : cometRend)
                r.scaleLocal(new Vector3f(0.5f, 0.5f, 0.5f));

            //init comet positions
            // randwerte: x: +/- 3.3f
            //            y: +/- 1.9f
            cometRend.get(0).translateGlobal(new Vector3f( -3.5f,2.0f,  -3.0f));
            cometRend.get(1).translateGlobal(new Vector3f( 3.8f, 1.6f,  -2.4f));
            cometRend.get(2).translateGlobal(new Vector3f( 0.9f, -1.7f, -2.0f));
            cometRend.get(3).translateGlobal(new Vector3f( 0.4f, -2.5f, -105.0f));
            cometRend.get(4).translateGlobal(new Vector3f( 1.0f, 1.2f, -10.0f));
            cometRend.get(5).translateGlobal(new Vector3f( 2.5f, 1.8f, -20.0f));
            cometRend.get(6).translateGlobal(new Vector3f( -1.4f, -3.2f, -1.0f));

            cometRend.get(7).translateGlobal(new Vector3f( -3.5f,2.0f,  -125.0f));
            cometRend.get(8).translateGlobal(new Vector3f( 3.8f, 1.6f,  -15.4f));
            cometRend.get(9).translateGlobal(new Vector3f( 0.9f, -1.7f, -20.0f));
            cometRend.get(10).translateGlobal(new Vector3f( 0.4f, -2.5f, -15.0f));
            cometRend.get(11).translateGlobal(new Vector3f( 1.0f, 3.4f, -10.0f));
            cometRend.get(12).translateGlobal(new Vector3f( 1.5f, 4.8f, -20.0f));
            cometRend.get(13).translateGlobal(new Vector3f( -1.4f, -3.2f, -5.0f));
            cometRend.get(14).translateGlobal(new Vector3f( -1.4f, -3.2f, -15.0f));

            //create renderables (background)
            bgRend = new Renderable();

            for (OBJLoader.OBJMesh m : resBG.objects.get(0).meshes) {
                Mesh mesh = new Mesh(
                        m.getVertexData(),
                        m.getIndexData(),
                        vertexAttributes,
                        backgoundTex,
                        backgoundTex,
                        backgoundTex,
                        1.0f
                );
                bgRend.meshes.add(mesh);
            }

            //init background position
            bgRend.translateGlobal(new Vector3f(0f, 0f, -90f));
            bgRend.rotateLocal(new Vector3f((float) Math.PI * 3/2, 0f, 0f));
            bgRend.scaleLocal(new Vector3f(200f, 0f, 100f));

            //create renderables (winScreen)
            winRend = new Renderable();
            for (OBJLoader.OBJMesh m : resWin.objects.get(0).meshes) {
                Mesh mesh = new Mesh(m.getVertexData(), m.getIndexData(), vertexAttributes, winTex, winTex, winTex, 5.0f);
                winRend.meshes.add(mesh);
            }

            //init win screens position
            winRend.translateGlobal(new Vector3f(20.0f, 0f, -95f));
            winRend.rotateLocal(new Vector3f((float) Math.PI * 3/2, (float) Math.PI, 0f));
            winRend.scaleLocal(new Vector3f(200f, 0f, 100f));

            //create renderables (loseScreen)
            loseRend = new Renderable();
            for (OBJLoader.OBJMesh m : resLose.objects.get(0).meshes) {
                Mesh mesh = new Mesh(m.getVertexData(), m.getIndexData(), vertexAttributes, loseTex, loseTex, loseTex, 5.0f);
                loseRend.meshes.add(mesh);
            }

            //init lose screen position
            loseRend.translateGlobal(new Vector3f(7.0f, 0f, -95f));
            loseRend.rotateLocal(new Vector3f((float) Math.PI * 3/2, (float) Math.PI, 0f));
            loseRend.scaleLocal(new Vector3f(200f, 0f, 100f));


            //create renderables (lifes)
            lifeRend = new Renderable();
            for (OBJLoader.OBJMesh m : resLife.objects.get(0).meshes) {
                Mesh mesh = new Mesh(m.getVertexData(), m.getIndexData(), vertexAttributes, lifeTex, lifeTex, lifeTex, 5.0f);
                lifeRend.meshes.add(mesh);
            }

            //init life position
            lifeRend.translateGlobal(new Vector3f(130.0f, 75.0f, -87f));
            lifeRend.rotateLocal(new Vector3f((float) Math.PI * 3/2, (float) Math.PI, 0f));
            lifeRend.scaleLocal(new Vector3f(20f, 0f, 6f));

            //create renderables (time)
            timeRend = new Renderable();
            for (OBJLoader.OBJMesh m : resTime.objects.get(0).meshes) {
                Mesh mesh = new Mesh(m.getVertexData(), m.getIndexData(), vertexAttributes, timeTex, timeTex, timeTex, 5.0f);
                timeRend.meshes.add(mesh);
            }

            //init time position
            //start value x = 360
            timeRend.translateGlobal(new Vector3f(360.0f, 85f, -87f));
            timeRend.rotateLocal(new Vector3f((float) Math.PI * 3/2, 0, 0f));
            timeRend.scaleLocal(new Vector3f(200f, 0f, 2.5f));


            //light setup
            ufo_light  = new PointLight(new Vector3f(1.0f, 1.0f, 160.0f / 255.0f), new Vector3f(0.3f, 1.7f, 1.6f));
            ring_light = new PointLight(new Vector3f(0.3f, 0.3f, 0.3f ), new Vector3f(0.3f, 1.7f, 1.6f));

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
        alienTex.bind(3);
        shader.setUniform("alienTex", 3);
        shader.setUniform("alienFactor", alien);
        shader.setUniform("screensize", new Vector2f((float) window.getFramebufferWidth(), (float) window.getFramebufferHeight()));

        //render camera
        shader.setUniform("view_matrix", camera.getViewMatrix(), false);
        shader.setUniform("proj_matrix", camera.getProjectionMatrix(), false);

        //render objects
        ufoRend.render(shader);
        Transformable[] t = {ufoRend};
        ringRend.render(shader, t);
        for (Renderable r : cometRend) {
            r.render(shader);
        }
        bgRend.render(shader);
        winRend.render(shader);
        loseRend.render(shader);
        lifeRend.render(shader);
        timeRend.render(shader);

        ufo_light.bind(shader, "light");
        ring_light.bind(shader, "light");
        shader.setUniform("uvMultiplier", 1.0f);
    }

    public void update(float dt) {
        //camera update
        float movemul = 0.75f;

        // Rotation Ufo und Ring
        if(gameOver){
            ufoRend.rotateLocal(new Vector3f(0.0f, 0.0f, 0.0f));
            ringRend.rotateLocal(new Vector3f(0.0f, 0.0f, 0.0f));
        }
        else {
            ufoRend.rotateLocal(new Vector3f(0.0f, 1.0f * dt * 2, 0.0f));
            ringRend.rotateLocal(new Vector3f(0.0f, 1.0f * -dt * 2.5f, 0.0f));
        }

        if (window.getKeyState(GLFW_KEY_C)) {
            camera.down(movemul * dt);
        }
        if (window.getKeyState(GLFW_KEY_SPACE)) {
            camera.up(movemul * dt);
        }

        // Admin cheat
        if (window.getKeyState(GLFW_KEY_F5)) {
            gameOver = false;
        }

        if (win) {
            if (!winMsg) {
                winMsg = true;

                //console msg
                System.out.println("---------------------------------");
                System.out.println("- Winner Winner Chicken Dinner! -");
                System.out.println("---------------------------------");
                System.out.println("            __//      ");
                System.out.println("           /.__.\\    ");
                System.out.println("           \\ \\/ /   ");
                System.out.println("        '__/    \\    ");
                System.out.println("         \\-      )   ");
                System.out.println("          \\_____/    ");
                System.out.println("       _____|_|____   ");
                System.out.println("            \" \"     ");
                System.out.println("---------------------------------");

                //UI msg
                winRend.translateGlobal(new Vector3f(0f, 0f, 10f));
            }

            return;
        }

        //Game Over
        if (gameOver) {
            if (!gameOverMsg) {
                gameOverMsg = true;

                //console msg
                System.out.println("Leider verloren, versuchs doch noch einmal :)");

                //UI msg
                loseRend.translateGlobal(new Vector3f(0f, 0f, 10f));
            }
            return;
        }

        //time
        time += dt;
        invulnTime -= dt;
        if (time > MAXTIME) {
            win = true;
            return;
        }

        //time UI
        timeRend.translateGlobal(new Vector3f((-320.0f / MAXTIME) * dt, 0f, 0f));

        //alien-effekt
        int s =  (int) time;
        //calc effect time --> after 30 sec's, for 3 sec
        boolean effectTime = s % 30 == 0 || s % 31 == 0 || s % 32 == 0 || s % 33 == 0;
        if (effectTime && s != 0) alien = 1.0f;
        else alien = 0.5f;

        //ufo update
        if (window.getKeyState(GLFW_KEY_UP)) {
            if (ufoRend.getPosition().y <= 2.3f)
                ufoRend.translateGlobal(new Vector3f(0.0f, 1.0f * dt, 0.0f));
        }
        if (window.getKeyState(GLFW_KEY_DOWN)) {
            if (ufoRend.getPosition().y >= -2.3f)
                ufoRend.translateGlobal(new Vector3f(0.0f, -1.0f * dt, 0.0f));
        }
        if (window.getKeyState(GLFW_KEY_LEFT)) {
            if (ufoRend.getPosition().x >= -2.3f)
                ufoRend.translateGlobal(new Vector3f(-1.0f * dt, 0.0f, 0.0f));
        }
        if (window.getKeyState(GLFW_KEY_RIGHT)) {
            if (ufoRend.getPosition().x <= 2.3f)
                ufoRend.translateGlobal(new Vector3f(1.0f * dt, 0.0f, 0.0f));
        }

        //comet update/movement
        for (Renderable r : cometRend) {
            if (r.getPosition().z < 3.0f) {
                r.translateGlobal(new Vector3f(0.0f, 0.0f, 1.0f * dt * (time / 10)));
                if (ufoHitDetection(r.getPosition())) gameOver = true;

                if (invulnTime < 0 && gameOver && lifeCount > 1) {
                    if (CONSOLE_LOG) System.out.println("Ein Leben weniger: " + lifeCount);
                    lifeCount--;
                    invulnTime = 3;
                    gameOver = false;
                    lifeRend.translateGlobal(new Vector3f(19f, 0f, 0f));
                }
            }
            else {
                //reset comet position
                r.translateGlobal(new Vector3f(-r.getPosition().x, -r.getPosition().y, -r.getPosition().z));

                //comet new position
                float x =  (float) (java.lang.Math.random() * 3.4f);// randwerte: x: +/- 3.3f
                float y =  (float) (java.lang.Math.random() * 1.9f);//            y: +/- 1.9f
                float z = ((float) (java.lang.Math.random() * 7.0f) + 6.0f) * -1;

                if ((int) (java.lang.Math.random() * 2) >= 1) x *= -1;
                if ((int) (java.lang.Math.random() * 2) >= 1) y *= -1;

                r.translateGlobal(new Vector3f(x, y, z));
            }
        }

        if (invulnTime > 0) {
            gameOver = false;
            alien = 0.1f;
        }
    }

    public void cleanup() {}

    private boolean ufoHitDetection(Vector3f comet){
        boolean hit;
        Vector3f ufo = ufoRend.getPosition(); // center point of the orb and ring
        float ufoOrbRadius  = 0.2f;
        float comRadius     = 0.5f;
        float ufoRingWidth  = 2.3f;
        float ufoRingHeight = 0.25f;
        float ufoRingDepth  = 0.4f;
        Vector3f ufoRingP1 = new Vector3f(
                ufo.x - ufoRingWidth / 2,  //left
                ufo.y - ufoRingHeight / 2, //bottom
                ufo.z - ufoRingDepth / 2); //front
        Vector3f ufoRingP2 = new Vector3f(
                ufo.x + ufoRingWidth / 2,  //right
                ufo.y + ufoRingHeight / 2, //up
                ufo.z + ufoRingDepth / 2); //back

        // hit detection orb
        hit = collisionSphere(ufo, ufoOrbRadius, comet, comRadius);
        if (hit) return true;

        // hit detection ring
        hit = collisionRectSpehere(comet, comRadius, ufoRingP1, ufoRingP2);
        if (hit) return true;

        return false;
    }

    /**
     * Calc collision between a rectangle and a sphere
     * @param sphere center point of the sphere
     * @param radius radius of the sphere
     * @param p1 left-bottom-front point of the rectangle
     * @param p2 right-top-back point of the rectangle
     * @return true if both objects collide
     */
    private boolean collisionRectSpehere(Vector3f sphere, float radius, Vector3f p1, Vector3f p2) {

        //case 1 - sphere center in rectangle
        if (sphere.x > p1.x && sphere.x < p2.x
                && sphere.y > p1.y && sphere.y < p2.y
                && sphere.z > p1.z && sphere.z < p2.z) {
            if (CONSOLE_LOG) System.out.println("Ring Hit: Center");
            return true;
        }

        //case 2 - one corner of the rectangle is in the sphere
        // ggf. weglassen, da wir miteinem Ring arbeiten und dieser Fall daher
        // zwar für ein Rechteck richtig wäre aber hier zu unschönen Kollisionen führen würde


        //case 3 - the sphere interleaves one edge of the rectangle

        //x-Edges
        boolean yzArea   = sphere.y > p1.y && sphere.y < p2.y && sphere.z > p1.z && sphere.z < p2.z;
        boolean leftHit  = Math.abs(sphere.x - p1.x) < radius;
        boolean rightHit = Math.abs(sphere.x - p2.x) < radius;
        if (yzArea && (leftHit || rightHit)) {
            if (CONSOLE_LOG) System.out.println("Rint Hit: X-Edge");
            return true;
        }

        //y-Edges
        boolean xzArea = sphere.x > p1.x && sphere.x < p2.x && sphere.z > p1.z && sphere.z < p2.z;
        boolean topHit = Math.abs(sphere.y - p1.y) < radius;
        boolean botHit = Math.abs(sphere.y - p2.y) < radius;
        if (xzArea && (topHit ||botHit)) {
           if (CONSOLE_LOG) System.out.println("Ring Hit: Y-Edge");
           return true;
        }

        //z-Edges
        boolean xyArea   = sphere.x > p1.x && sphere.x < p2.x && sphere.y > p1.y && sphere.y < p2.y;
        boolean frontHit = Math.abs(sphere.z - p1.z) < radius;
        boolean backHit  = Math.abs(sphere.z - p2.z) < radius;
        if (xyArea && (frontHit || backHit)) {
            if (CONSOLE_LOG) System.out.println("Ring Hit: Z-Edge");
            return true;
        }

        return false;
    }

    /**
     * Calc collision between two spheres
     * @param p1 center point of sphere 1
     * @param rad1 radius of sphere 1
     * @param p2 center point of sphere 2
     * @param rad2 radius of sphere 2
     * @return true if both spheres collide
     */
    private boolean collisionSphere(Vector3f p1, float rad1, Vector3f p2, float rad2) {
    //calc deltas
        float dx = p1.x - p2.x;
        float dy = p1.y - p2.y;
        float dz = p1.z - p2.z;

        float distance = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);

        if (distance <= (rad1 + rad2)) {
            if (CONSOLE_LOG) System.out.println("Sphere Hit");
            return true;
        }

        return false;
    }

}
