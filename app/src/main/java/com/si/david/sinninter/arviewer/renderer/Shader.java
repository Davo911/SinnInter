package com.si.david.sinninter.arviewer.renderer;


import android.opengl.Matrix;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES30.GL_ACTIVE_ATTRIBUTES;
import static android.opengl.GLES30.GL_ACTIVE_UNIFORMS;
import static android.opengl.GLES30.GL_COMPILE_STATUS;
import static android.opengl.GLES30.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES30.GL_FALSE;
import static android.opengl.GLES30.GL_FRAGMENT_SHADER;
import static android.opengl.GLES30.GL_TEXTURE0;
import static android.opengl.GLES30.GL_TEXTURE_2D;
import static android.opengl.GLES30.GL_TRIANGLES;
import static android.opengl.GLES30.GL_UNSIGNED_INT;
import static android.opengl.GLES30.GL_VERTEX_SHADER;
import static android.opengl.GLES30.glActiveTexture;
import static android.opengl.GLES30.glAttachShader;
import static android.opengl.GLES30.glBindAttribLocation;
import static android.opengl.GLES30.glBindBuffer;
import static android.opengl.GLES30.glBindTexture;
import static android.opengl.GLES30.glBindVertexArray;
import static android.opengl.GLES30.glCompileShader;
import static android.opengl.GLES30.glCreateProgram;
import static android.opengl.GLES30.glCreateShader;
import static android.opengl.GLES30.glDeleteProgram;
import static android.opengl.GLES30.glDeleteShader;
import static android.opengl.GLES30.glDetachShader;
import static android.opengl.GLES30.glEnableVertexAttribArray;
import static android.opengl.GLES30.glGetActiveAttrib;
import static android.opengl.GLES30.glGetActiveUniform;
import static android.opengl.GLES30.glGetAttribLocation;
import static android.opengl.GLES30.glGetProgramiv;
import static android.opengl.GLES30.glGetShaderInfoLog;
import static android.opengl.GLES30.glGetShaderiv;
import static android.opengl.GLES30.glGetUniformLocation;
import static android.opengl.GLES30.glLinkProgram;
import static android.opengl.GLES30.glShaderSource;
import static android.opengl.GLES30.glUniform1f;
import static android.opengl.GLES30.glUniform1i;
import static android.opengl.GLES30.glUniform3f;
import static android.opengl.GLES30.glUniformMatrix4fv;
import static android.opengl.GLES30.glUseProgram;
import static android.opengl.GLES30.glValidateProgram;

public class Shader
{
    private int programID;
    private int vertexShaderID;
    private int fragmentShaderID;
    private Map<String, Integer> uniforms = new HashMap<>();
    private Map<String, Integer> attributes = new HashMap<>();


    private static FloatBuffer matrixBuffer = ByteBuffer.allocateDirect(4 * 16)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();

    public Shader(String vertexShader, String fragmentShader, String[] attribs)
    {
        vertexShaderID = generateShader(vertexShader, GL_VERTEX_SHADER);
        fragmentShaderID = generateShader(fragmentShader, GL_FRAGMENT_SHADER);

        programID = glCreateProgram();
        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);

        int i = 0;
        for(String attribName : attribs)
            glBindAttribLocation(programID, i++, attribName);

        glLinkProgram(programID);
        glValidateProgram(programID);

        int[] count = new int[1];
        int[] length = new int[1];
        int[] size = new int[1];
        int[] type = new int[1];
        byte[] nameBuffer = new byte[100];
        String name;

        glGetProgramiv(programID, GL_ACTIVE_ATTRIBUTES, count, 0);
        try
        {
            for (i = 0; i < count[0]; i++)
            {
                glGetActiveAttrib(programID, i, 100, length, 0, size, 0, type, 0, nameBuffer, 0);

                name = new String(nameBuffer, 0, length[0], "UTF-8");
                attributes.put(name, glGetAttribLocation(programID, name));
            }
        }catch(UnsupportedEncodingException e) {Log.d("shader", "unsupported Encoding");}


        glGetProgramiv(programID, GL_ACTIVE_UNIFORMS, count, 0);
        try
        {
            for (i = 0; i < count[0]; i++)
            {
                glGetActiveUniform(programID, i, 100, length, 0, size, 0, type, 0, nameBuffer, 0);

                name = new String(nameBuffer, 0, length[0], "UTF-8");
                uniforms.put(name, glGetUniformLocation(programID, name));
            }
        }catch(UnsupportedEncodingException e){Log.d("shader", "unsupported Encoding");}
    }

    public void loadModel(Model model)
    {
        glBindVertexArray(model.getVAO());
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, model.indicesVBO);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        if(model.getTexture() != null)
        {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, model.getTexture().getID());
        }
    }

    public void bindTexture2D(int targetTextureUnit, int textureID)
    {
        glActiveTexture(targetTextureUnit);
        glBindTexture(GL_TEXTURE_2D, textureID);
    }

    public void start()
    {
        glUseProgram(programID);
    }

    public void stop()
    {
        glUseProgram(0);
    }

    public void delete()
    {
        stop();

        glDetachShader(programID, vertexShaderID);
        glDetachShader(programID, fragmentShaderID);

        glDeleteShader(vertexShaderID);
        glDeleteShader(fragmentShaderID);

        glDeleteProgram(programID);
    }

    private static int generateShader(String shaderCode, int type)
    {

        int shaderID = glCreateShader(type);
        glShaderSource(shaderID, shaderCode);
        glCompileShader(shaderID);

        int[] compiled = new int[1];
        glGetShaderiv(shaderID, GL_COMPILE_STATUS, compiled, 0);
        if(compiled[0] == GL_FALSE)
        {
            String infoLog = glGetShaderInfoLog(shaderID);
            Log.e("shader", infoLog);
            System.exit(-1);
        }

        return shaderID;
    }

    public void loadMatrix(String location, float[] matrix)
    {
        matrixBuffer.put(matrix);
        matrixBuffer.flip();
        glUniformMatrix4fv(uniforms.get(location), 1, false, matrixBuffer);
    }

    public void loadInt(String location, int value)
    {
        try
        {
            glUniform1i(uniforms.get(location), value);
        }catch(Exception e ){Log.d("shader", "uniform not found");} //TODO: repeat for all glUniform calls
    }

    public void loadFloat(String location, float value)
    {
        glUniform1f(uniforms.get(location), value);
    }

    protected void loadVector(String location, float[] vector)
    {
        glUniform3f(uniforms.get(location), vector[0], vector[1], vector[2]);
    }

    public void loadViewMatrix(float[] viewMatrix)
    {
        loadMatrix("viewMatrix", viewMatrix);
    }

    public void loadTransformationMatrix(float[] transformationMatrix)
    {
        loadMatrix("transformationMatrix", transformationMatrix);
    }

    public void loadProjectionMatrix(float[] projection)
    {
        loadMatrix("projectionMatrix", projection);
    }

    @Deprecated
    //use Matrix.perspectiveM()
    public float[] getProjectionMatrix(int width, int height, float fov, float nearPlane, float farPlane)
    {
        float aspectRatio = (float) width / (float)height;
        float yScale = (float) ((1.0f / Math.tan(Math.toRadians(fov / 2.0f))) * aspectRatio);
        float xScale = yScale / aspectRatio;
        float frustumLength = farPlane - nearPlane;

        float[] projectionMatrix = new float[16];
        projectionMatrix[0] = xScale;
        projectionMatrix[5] = yScale;
        projectionMatrix[10] = -((farPlane + nearPlane) / frustumLength);
        projectionMatrix[11] = -1;
        projectionMatrix[14] = -((2 + nearPlane + farPlane) / frustumLength);
        projectionMatrix[15] = 1;

        return projectionMatrix;
    }

    public void renderModel(Model model)
    {
        glDrawElements(GL_TRIANGLES, model.getVertexCount(), GL_UNSIGNED_INT, 0);
    }
}
