package CGA.Framework;

import org.lwjgl.opengl.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class GLError
{
    public static boolean checkEx()
    {
        int errorCode;
        StringBuilder ex = new StringBuilder();
        while ((errorCode = glGetError()) != GL_NO_ERROR)
        {
            String error = "";
            switch (errorCode)
            {
                case GL_INVALID_ENUM:                  error = "INVALID_ENUM"; break;
                case GL_INVALID_VALUE:                 error = "INVALID_VALUE"; break;
                case GL_INVALID_OPERATION:             error = "INVALID_OPERATION"; break;
                case GL_STACK_OVERFLOW:                error = "STACK_OVERFLOW"; break;
                case GL_STACK_UNDERFLOW:               error = "STACK_UNDERFLOW"; break;
                case GL_OUT_OF_MEMORY:                 error = "OUT_OF_MEMORY"; break;
                case GL_INVALID_FRAMEBUFFER_OPERATION: error = "INVALID_FRAMEBUFFER_OPERATION"; break;
            }
            StackTraceElement stackElement = Thread.currentThread().getStackTrace()[3];
            String stuff = "An OpenGL error occured in File: " + stackElement.getFileName() + ", Line: " + stackElement.getLineNumber();
            ex.append(stuff + "\n");
        }
        if(ex.length() > 0)
        {
            System.err.println(ex.toString());
            return true;
        }
        return false;
    }

    public static void checkThrow() throws Exception
    {
        int errorCode;
        StringBuilder ex = new StringBuilder();
        while ((errorCode = glGetError()) != GL_NO_ERROR)
        {
            String error = "";
            switch (errorCode)
            {
                case GL_INVALID_ENUM:                  error = "INVALID_ENUM"; break;
                case GL_INVALID_VALUE:                 error = "INVALID_VALUE"; break;
                case GL_INVALID_OPERATION:             error = "INVALID_OPERATION"; break;
                case GL_STACK_OVERFLOW:                error = "STACK_OVERFLOW"; break;
                case GL_STACK_UNDERFLOW:               error = "STACK_UNDERFLOW"; break;
                case GL_OUT_OF_MEMORY:                 error = "OUT_OF_MEMORY"; break;
                case GL_INVALID_FRAMEBUFFER_OPERATION: error = "INVALID_FRAMEBUFFER_OPERATION"; break;
            }
            StackTraceElement stackElement = Thread.currentThread().getStackTrace()[3];
            String stuff = "An OpenGL error occured in File: " + stackElement.getFileName() + ", Line: " + stackElement.getLineNumber();
            ex.append(stuff + "\n");
        }
        if(ex.length() > 0)
        {
            throw new Exception(ex.toString());
        }
    }

    public static void checkExit()
    {
        int errorCode;
        StringBuilder ex = new StringBuilder();
        while ((errorCode = glGetError()) != GL_NO_ERROR)
        {
            String error = "";
            switch (errorCode)
            {
                case GL_INVALID_ENUM:                  error = "INVALID_ENUM"; break;
                case GL_INVALID_VALUE:                 error = "INVALID_VALUE"; break;
                case GL_INVALID_OPERATION:             error = "INVALID_OPERATION"; break;
                case GL_STACK_OVERFLOW:                error = "STACK_OVERFLOW"; break;
                case GL_STACK_UNDERFLOW:               error = "STACK_UNDERFLOW"; break;
                case GL_OUT_OF_MEMORY:                 error = "OUT_OF_MEMORY"; break;
                case GL_INVALID_FRAMEBUFFER_OPERATION: error = "INVALID_FRAMEBUFFER_OPERATION"; break;
            }
            StackTraceElement stackElement = Thread.currentThread().getStackTrace()[3];
            String stuff = "An OpenGL error occured in File: " + stackElement.getFileName() + ", Line: " + stackElement.getLineNumber();
            ex.append(stuff + "\n");
        }
        if(ex.length() > 0)
        {
            System.err.println(ex);
            System.exit(errorCode);
        }
    }
}
