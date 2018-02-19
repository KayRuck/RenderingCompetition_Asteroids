package CGA.Framework;
/**
 * Created by Fabian on 16.09.2017.
 */

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.nio.*;
import org.lwjgl.system.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

/**
 * Base class for GameWindows using OpenGL for rendering
 */
abstract public class GameWindow
{
    //inner types

    /**
     * Simple class that holds screen space x and y coordinates of the current mouse position
     */
    static public class MousePosition
    {
        public MousePosition(double x, double y)
        {
            xpos = x;
            ypos = y;
        }
        public double xpos;
        public double ypos;
    }


    //private data
    private long m_window;
    private int m_width;
    private int m_height;
    private int m_fbwidth;
    private int m_fbheight;
    private boolean m_fullscreen;
    private boolean m_vsync;
    private String m_title;
    private int m_msaasamples;
    private float m_updatefrequency;
    private int m_cvmaj;
    private int m_cvmin;

    //GLFW callbacks
    private GLCapabilities m_caps;
    private GLFWKeyCallback m_keyCallback;
    private GLFWCursorPosCallback m_cpCallback;
    private GLFWMouseButtonCallback m_mbCallback;
    private GLFWFramebufferSizeCallback m_fbCallback;
    private GLFWWindowSizeCallback m_wsCallback;
    private Callback m_debugProc;

    private long m_currentTime;


    //private methods
    private void initialize()
    {
        if(!glfwInit())
            throw new IllegalStateException("GLFW initialization failed.");

        glfwSetErrorCallback(new GLFWErrorCallback()
        {
            @Override
            public void invoke(int error,
                               long description)
            {
                String msg = MemoryUtil.memUTF8(description);
                System.out.println(msg);
            }
        });

        glfwDefaultWindowHints();
        if(m_msaasamples > 0)
            glfwWindowHint(GLFW_SAMPLES, m_msaasamples);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, m_cvmaj);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, m_cvmin);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        m_window = glfwCreateWindow(m_width, m_height, m_title, (m_fullscreen ? glfwGetPrimaryMonitor() : 0L), 0);
        if(m_window == 0)
            throw new IllegalStateException("GLFW window couldn't be created.");

        glfwSetKeyCallback(m_window, new GLFWKeyCallback()
        {
            @Override
            public void invoke(long window,
                               int key,
                               int scancode,
                               int action,
                               int mods)
            {
                if(key == GLFW_KEY_ESCAPE && action == GLFW_PRESS)
                    quit();
                onKey(key, scancode, action, mods);
            }
        });

        glfwSetMouseButtonCallback(m_window, new GLFWMouseButtonCallback()
        {
            @Override
            public void invoke(long window,
                               int button,
                               int action,
                               int mods)
            {
                onMouseButton(button, action, mods);
            }
        });

        glfwSetCursorPosCallback(m_window, new GLFWCursorPosCallback()
        {
            @Override
            public void invoke(long window,
                               double xpos,
                               double ypos)
            {
                onMouseMove(xpos, ypos);
            }
        });

        glfwSetScrollCallback(m_window, new GLFWScrollCallback()
        {
            @Override
            public void invoke(long window,
                               double xoffset,
                               double yoffset)
            {
                onMouseScroll(xoffset, yoffset);
            }
        });

        glfwSetFramebufferSizeCallback(m_window, new GLFWFramebufferSizeCallback()
        {
            @Override
            public void invoke(long window,
                               int width,
                               int height)
            {
                m_fbwidth = width;
                m_fbheight = height;
                glViewport(0, 0, width, height);
                onFrameBufferSize(width, height);
            }
        });

        glfwSetWindowSizeCallback(m_window, new GLFWWindowSizeCallback()
        {
            @Override
            public void invoke(long window,
                               int width,
                               int height)
            {
                m_width = width;
                m_height = height;
                onWindowSize(width, height);
            }
        });



        glfwMakeContextCurrent(m_window);
        glfwSwapInterval((m_vsync ? 1 : 0));

        glfwShowWindow(m_window);
    }

    //Methods to be called by child classes

    /**
     * Tells the application to quit.
     * shutdown() is called after the last simulation step has completed.
     */
    protected void quit()
    {
        glfwSetWindowShouldClose(m_window, true);
    }

    /**
     * Returns the current mouse position in screen coordinates
     * @return Current mouse position
     */
    public MousePosition getMousePos()
    {
        DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer y = BufferUtils.createDoubleBuffer(1);
        glfwGetCursorPos(m_window, x, y);
        return new MousePosition(x.get(0), y.get(0));
    }

    /**
     * Queries the state of a given key
     * @param key The GLFW key name
     * @return false, if the key is released; true, if the key is pressed
     */
    public boolean getKeyState(int key)
    {
        return glfwGetKey(m_window, key) == GLFW_PRESS;
    }

    /**
     * Toggles cursor capture mode
     * @param visible if false, the cursor becomes invisible and is captured by the window.
     */
    public void setCursorVisible(boolean visible)
    {
        glfwSetInputMode(m_window, GLFW_CURSOR, visible ? GLFW_CURSOR_NORMAL : GLFW_CURSOR_DISABLED);
    }

    /**
     * Returns the current width of the default frame buffer
     * @return width of the default frame buffer
     */
    public int getFramebufferWidth()
    {
        return m_fbwidth;
    }

    /**
     * Returns the current height of the default frame buffer
     * @return height of the default frame buffer
     */
    public int getFramebufferHeight()
    {
        return m_fbheight;
    }

    /**
     * Returns the current width of the window
     * @return width of the window
     */
    public int getWindowWidth()
    {
        return m_width;
    }

    /**
     * Returns the current height of the window
     * @return height of the window
     */
    public int getWindowHeight()
    {
        return m_height;
    }

    public float getCurrentTime()
    {
        return (float)m_currentTime * 1e-9f;
    }

    //Methods to override by child classes

    /**
     * Is called once when the application starts
     */
    protected void init(){}

    /**
     * is called when the application quits
     */
    protected void shutdown(){}

    /**
     * Is called for every game state update.
     * The method is called in fixed time steps if possible.
     * Make sure that one update call takes no longer than 1/updatefrequency seconds, otherwise the
     * game slows down.
     *
     * This method should be used for physics simulations, where explicit solvers need small and constant
     * time steps to stay stable.
     *
     * @param dt Time delta to advance the game state simulation. dt is 1/updatefrequency seconds, constant.
     */
    protected void update(float dt){}

    /**
     * Is called for each frame to be rendered.
     *
     * @param dt Time in seconds the last frame needed to complete
     */
    protected void render(float dt){}

    /**
     * Is called when a mouse move event occurs
     * @param xpos  screen coordinate x value
     * @param ypos  screen coordinate y value
     */
    protected void onMouseMove(double xpos, double ypos){}

    /**
     * Is called when a mouse button is pressed or released
     * @param button    GLFW mouse button name
     * @param action    GLFW action name
     * @param mode      GLFW modifiers
     */
    protected void onMouseButton(int button, int action, int mode){}

    /**
     * Is called when a scroll event occurs
     * @param xoffset   x offset of the mouse wheel
     * @param yoffset   y offset of the mouse wheel
     */
    protected void onMouseScroll(double xoffset, double yoffset){}

    /**
     * Is called when a key is pressed or released
     * @param key       GLFW key name
     * @param scancode  scancode of the key
     * @param action    GLFW action name
     * @param mode      GLFW modifiers
     */
    protected void onKey(int key, int scancode, int action, int mode){}

    /**
     * Is called when the default frame buffer size changes (i.e. through resizing the window)
     * @param width     new frame buffer width
     * @param height    new frame buffer height
     */
    protected  void onFrameBufferSize(int width, int height)
    {
        m_fbwidth = width;
        m_fbheight = height;
    }

    /**
     * Is called when the window size changes
     * @param width     new window width
     * @param height    new window height
     */
    protected void onWindowSize(int width, int height)
    {
        m_width = width;
        m_height = height;
    }

    //Public interface
    //Constructors

    /**
     * Initializes a game window object
     * @param width         Desired window width
     * @param height        Desired window height
     * @param fullscreen    Fullscreen mode
     * @param vsync         Use vsync
     * @param cvmaj         Desired major OpenGL version
     * @param cvmin         Desired minor OpenGL version
     * @param title         Window title
     * @param msaasamples       Desired of multisampling samples to use when displaying the default frame buffer
     * @param updatefrequency   Frequency the update method should be called with. 2x the expected frame rate is a good rule of thumb
     */
    public GameWindow(int width,
                      int height,
                      boolean fullscreen,
                      boolean vsync,
                      int cvmaj,
                      int cvmin,
                      String title,
                      int msaasamples,
                      float updatefrequency
                      )
    {
        m_width = width;
        m_height = height;
        m_fbwidth = width;
        m_fbheight = height;
        m_fullscreen = fullscreen;
        m_vsync = vsync;
        m_cvmaj = cvmaj;
        m_cvmin = cvmin;
        m_title = title;
        m_msaasamples = msaasamples;
        m_updatefrequency = updatefrequency;
        initialize();
    }

    //public methods

    /**
     * Enters the game loop and loops until an error occurs or quit() is called
     */
    public void run()
    {
        m_caps = GL.createCapabilities(true);
        if(m_msaasamples > 0)
            glEnable(GL_MULTISAMPLE);
        init();
        long timedelta = (long)((1.0d / m_updatefrequency) * 1000000000.0d);
        long currenttime = 0;
        long frametime = 0;
        long newtime = 0;
        long accum = 0;
        m_currentTime = 0;
        currenttime = System.nanoTime();

        while(!glfwWindowShouldClose(m_window))
        {
            newtime = System.nanoTime();
            frametime = newtime - currenttime;
            m_currentTime += frametime;
            currenttime = newtime;
            accum += frametime;

            glfwPollEvents();
            while(accum >= timedelta)
            {
                update((float)((double)timedelta * 1e-9d));
                accum -= timedelta;
            }
            render((float)((double)frametime * 1e-9d));
            glfwSwapBuffers(m_window);
        }
        shutdown();
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(m_window);
        glfwDestroyWindow(m_window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
