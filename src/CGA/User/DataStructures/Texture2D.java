package CGA.User.DataStructures;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBImage;
import CGA.Framework.*;
import static CGA.Framework.GLError.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Created by Fabian on 16.09.2017.
 */
public class Texture2D
{
    private int texID;

    //create texture from file
    //don't support compressed textures for now
    //instead stick to pngs



    public Texture2D(String path, boolean genMipMaps) throws Exception
    {
        IntBuffer x = BufferUtils.createIntBuffer(1);
        IntBuffer y = BufferUtils.createIntBuffer(1);
        IntBuffer readChannels = BufferUtils.createIntBuffer(1);
        //flip y coordinate to make OpenGL happy
        stbi_set_flip_vertically_on_load(true);
        ByteBuffer imageData = stbi_load(path, x, y, readChannels, 4);
        if(imageData == null)
            throw new Exception("Image file \"" + path + "\" couldn't be read:\n" + stbi_failure_reason());

        int tex = glGenTextures();
        if(tex == 0)
        {
            stbi_image_free(imageData);
            throw new Exception("OpenGL texture object creation failed.");
        }

        glBindTexture(GL_TEXTURE_2D, tex);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, x.get(), y.get(), 0, GL_RGBA , GL_UNSIGNED_BYTE, imageData);
        //should be done but explained before
        if(checkEx())
        {
            stbi_image_free(imageData);
            glDeleteTextures(tex);
            throw new Exception("glTexImage2D call failed.");
        }

        //explain mipmaps and their creation
        if(genMipMaps)
        {
            glGenerateMipmap(GL_TEXTURE_2D);
            if(checkEx())
            {
                stbi_image_free(imageData);
                glDeleteTextures(tex);
                throw new Exception("Mipmap creation failed.");
            }
        }

        stbi_image_free(imageData);

        glBindTexture(GL_TEXTURE_2D, 0);

        texID = tex;
    }

    public void setTexParams(int wrapS, int wrapT, int minFilter, int magFilter) throws Exception
    {
        //talk to martin
        bind(0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapS);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);
        if(checkEx())
        {
            unbind();
            throw new Exception("Setting texture params failed.");
        }
        unbind();
    }

    //maybe load 2 textures and mix them in shader
    //maybe explain that 0 means invalid texture object
    public void bind(int textureUnit)
    {
        if(texID != 0)
        {
            glActiveTexture(GL_TEXTURE0 + textureUnit);
            glBindTexture(GL_TEXTURE_2D, texID);
        }
    }

    public void unbind()
    {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public int getTexID()
    {
        return texID;
    }

    public void cleanup()
    {
        unbind();
        if(texID != 0)
        {
            glDeleteTextures(texID);
            texID = 0;
        }

    }
}
