package com.si.david.sinninter.arviewer.renderer;

import android.graphics.Bitmap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import static android.opengl.GLES30.GL_ARRAY_BUFFER;
import static android.opengl.GLES30.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES30.GL_FLOAT;
import static android.opengl.GLES30.GL_STATIC_DRAW;
import static android.opengl.GLES30.glBindBuffer;
import static android.opengl.GLES30.glBindVertexArray;
import static android.opengl.GLES30.glBufferData;
import static android.opengl.GLES30.glDeleteBuffers;
import static android.opengl.GLES30.glDeleteVertexArrays;
import static android.opengl.GLES30.glGenBuffers;
import static android.opengl.GLES30.glGenVertexArrays;
import static android.opengl.GLES30.glVertexAttribPointer;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;


public class Model
{
    int vaoID, verticesVBO, indicesVBO, textureCoordVBO, normalsVBO, vertexCount;
    private Texture texture = null;

    public static class ModelData
    {
        ByteBuffer vertices, textureCoords, normals, indices;
        int verticeCount, indiceCount;
        Bitmap texture = null;

        ModelData(float[] vertices, int[] indices, float[] textureCoords, float[] normals)
        {
            verticeCount = vertices.length / 3;
            indiceCount = indices.length;

            this.vertices = byteBufferFromArray(vertices);
            this.indices = byteBufferFromArray(indices);
            this.textureCoords = byteBufferFromArray(textureCoords);
            this.normals = byteBufferFromArray(normals);
        }



        public static ModelData fromOBJ(InputStream objFileStream)
        {
            BufferedReader reader;
            try
            {
                reader = new BufferedReader(new InputStreamReader(objFileStream));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            String line;

            ArrayList<Float[]> vertexList = new ArrayList<>();
            ArrayList<Float[]> textureCoordList = new ArrayList<>();
            ArrayList<Float[]> normalsList = new ArrayList<>();
            ArrayList<Integer> indiceList = new ArrayList<>();

            float[] correspondingTexture = new float[2];
            float[] correspondingNormal = new float[3];
            Float[] tmp;

            float[] vertices;
            float[] textureCoords = null;
            float[] normals = null;
            int[] indices;

            try
            {
                while ((line = reader.readLine()) != null)
                {
                    String[] splitLine = line.split(" ");

                    if (line.startsWith("v "))
                    {
                        vertexList.add(new Float[]{parseFloat(splitLine[1]), parseFloat(splitLine[2]), parseFloat(splitLine[3])});
                    }
                    else if(line.startsWith("vt "))
                    {
                        textureCoordList.add(new Float[]{parseFloat(splitLine[1]), parseFloat(splitLine[2])});

                    }
                    else if(line.startsWith("vn "))
                    {
                        normalsList.add(new Float[]{parseFloat(splitLine[1]), parseFloat(splitLine[2]), parseFloat(splitLine[3])});

                    }
                    else if(line.startsWith("f "))
                    {
                        normals = new float[vertexList.size() * 3];
                        textureCoords = new float[vertexList.size() * 2];

                        do
                        {
                            if (line.startsWith("f "))
                            {
                                for (String indexes : line.split(" "))
                                {
                                    if (!indexes.startsWith("f"))
                                    {
                                        int vertexIndex = parseInt(indexes.split("/")[0]) - 1;

                                        tmp = textureCoordList.get(parseInt(indexes.split("/")[1]) - 1);
                                        correspondingTexture[0] = tmp[0];
                                        correspondingTexture[1] = tmp[1];

                                        tmp = normalsList.get(parseInt(indexes.split("/")[2]) - 1);
                                        correspondingNormal[0] = tmp[0];
                                        correspondingNormal[1] = tmp[1];
                                        correspondingNormal[2] = tmp[2];

                                        indiceList.add(vertexIndex);

                                        textureCoords[vertexIndex * 2] = correspondingTexture[0];
                                        textureCoords[vertexIndex * 2 + 1] = 1 - correspondingTexture[1];

                                        normals[vertexIndex * 3] = correspondingNormal[0];
                                        normals[vertexIndex * 3 + 1] = correspondingNormal[1];
                                        normals[vertexIndex * 3 + 2] = correspondingNormal[2];
                                    }
                                }
                            }
                        }while((line = reader.readLine()) != null);
                        break;
                    }
                }

                reader.close();

            } catch (IOException e) { e.printStackTrace(); }


            vertices = new float[vertexList.size() * 3];
            int i = 0;
            for(Float[] v : vertexList)
            {
                vertices[i++] = v[0];
                vertices[i++] = v[1];
                vertices[i++] = v[2];
            }

            indices = new int[indiceList.size()];
            i = 0;
            for(Integer index : indiceList)
            {
                indices[i++] = index;
            }

            return new ModelData(vertices, indices, textureCoords, normals);
        }

        void writeToFile(String filePath)
        {
            File outFile = new File(filePath);
            try
            {
                FileChannel channel = new FileOutputStream(outFile).getChannel();

                channel.write(byteBufferFromArray(new int[]{verticeCount, indiceCount}));

                channel.write(vertices);
                channel.write(indices);
                channel.write(textureCoords);
                channel.write(normals);

                channel.close();

            }catch(Exception e){e.printStackTrace();System.exit(-1);}


        }

        ModelData(FileOutputStream ins)
        {
            try
            {
                byte[] tmp = new byte[8];
                ByteBuffer tmpBuffer = ByteBuffer.wrap(tmp);
                FileChannel channel = ins.getChannel();


                channel.read(tmpBuffer);
                tmpBuffer.flip();
                verticeCount = tmpBuffer.getInt();
                indiceCount = tmpBuffer.getInt();

                vertices = ByteBuffer.allocateDirect(verticeCount * 3);
                indices = ByteBuffer.allocateDirect(indiceCount);
                textureCoords = ByteBuffer.allocateDirect(verticeCount * 2);
                normals = ByteBuffer.allocateDirect(verticeCount * 3);

                channel.read(vertices);
                vertices.flip();

                channel.read(indices);
                vertices.flip();

                channel.read(textureCoords);
                vertices.flip();

                channel.read(normals);
                vertices.flip();

                channel.close();

            }catch(Exception e){e.printStackTrace();System.exit(-1);}
        }

        public void setTexture(Bitmap texture)
        {
            this.texture = texture;
        }

        public Bitmap getTexture()
        {
            return texture;
        }
    }


    public Model(ModelData data)
    {
        this.vertexCount = data.indiceCount;

        this.vaoID = createVAO();

        int buffers[] = new int[4];
        glGenBuffers(4, buffers, 0);

        //create VBO and put it in the VAO
        verticesVBO = buffers[0];
        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        //put the vertices into the vbo
        glBufferData(GL_ARRAY_BUFFER, data.vertices.remaining(), data.vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        //put the indices into the vbo
        indicesVBO = buffers[1];
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesVBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, data.indices.remaining(), data.indices, GL_STATIC_DRAW);

        //put the textureCoords into the vbo
        textureCoordVBO = buffers[2];
        glBindBuffer(GL_ARRAY_BUFFER, textureCoordVBO);
        glBufferData(GL_ARRAY_BUFFER, data.textureCoords.remaining(), data.textureCoords, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        //put the normals into the vbo
        normalsVBO = buffers[3];
        glBindBuffer(GL_ARRAY_BUFFER, normalsVBO);
        glBufferData(GL_ARRAY_BUFFER, data.normals.remaining(), data.normals, GL_STATIC_DRAW);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        //unbind current VAO
        glBindVertexArray(0);

        if(data.texture != null)
        {
            setTexture(new Texture(data.texture));
        }
    }


    public static Model fromOBJ(InputStream objFileStream)
    {
        return new Model(ModelData.fromOBJ(objFileStream));
    }

    private int createVAO()
    {
        int[] arrays = new int[1];
        glGenVertexArrays(1, arrays, 0);
        glBindVertexArray(arrays[0]);
        return arrays[0];
    }

    public void unload()
    {
        glDeleteBuffers(4, new int[]{verticesVBO, indicesVBO, textureCoordVBO, normalsVBO}, 0);
        glDeleteVertexArrays(1, new int[]{vaoID}, 0);
    }

    public int getVAO()
    {
        return vaoID;
    }

    public int getVertexCount()
    {
        return vertexCount;
    }

    public static ByteBuffer byteBufferFromArray(float[] array)
    {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * array.length)
                .order(ByteOrder.nativeOrder());

        buffer.asFloatBuffer().put(array);
        buffer.rewind();
        return buffer;
    }

    public static ByteBuffer byteBufferFromArray(int[] array)
    {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * array.length)
                .order(ByteOrder.nativeOrder());

        buffer.asIntBuffer().put(array);
        buffer.rewind();
        return buffer;
    }

    public Texture getTexture()
    {
        return texture;
    }

    public void setTexture(Texture texture)
    {
        this.texture = texture;
    }
}
