package com.si.david.sinninter.arviewer.renderer;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

import static android.opengl.GLES30.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES30.GL_LINEAR;
import static android.opengl.GLES30.GL_TEXTURE_2D;
import static android.opengl.GLES30.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES30.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES30.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES30.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES30.glBindTexture;
import static android.opengl.GLES30.glDeleteTextures;
import static android.opengl.GLES30.glGenTextures;
import static android.opengl.GLES30.glTexParameteri;


public class Texture
{

    private final int[] textureID = new int[1];

    public Texture(Bitmap image)
    {
        glGenTextures(1, textureID, 0);

        glBindTexture(GL_TEXTURE_2D, textureID[0]);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, image, 0);
    }

    public void delete()
    {
        glDeleteTextures(1, new int[]{textureID[0]}, 0);
    }

    public int getID()
    {
        return textureID[0];
    }

}
