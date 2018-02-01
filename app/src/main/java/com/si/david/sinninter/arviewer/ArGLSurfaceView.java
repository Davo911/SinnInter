package com.si.david.sinninter.arviewer;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.SizeF;
import android.view.MotionEvent;

import com.google.android.gms.maps.model.LatLng;
import com.si.david.sinninter.R;
import com.si.david.sinninter.arviewer.renderer.Model;
import com.si.david.sinninter.arviewer.renderer.Shader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.content.Context.CAMERA_SERVICE;
import static android.opengl.GLES20.GL_BACK;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glCullFace;


public class ArGLSurfaceView extends GLSurfaceView
{
    private final MyGLRenderer mRenderer;

    private LatLng currentLocation = null;

    private float[] projectionMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] cameraMatrix = new float[16];

    private ArrayList<ARObject> arObjects = new ArrayList<>();
    private final ConcurrentLinkedQueue<Model.ModelData> modelsToLoad = new ConcurrentLinkedQueue<Model.ModelData>();
    private final ConcurrentLinkedQueue<Double[]> arObjectPositions = new ConcurrentLinkedQueue<Double[]>();

    Shader shader;


    public ArGLSurfaceView(Context context)
    {
        super(context);

        setEGLContextClientVersion(3);

        mRenderer = new MyGLRenderer();

        setEGLConfigChooser(8, 8, 8, 8, 24, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {

        if(e.getAction() == MotionEvent.ACTION_DOWN ||
                (e.getAction() == MotionEvent.ACTION_MOVE && e.getPointerCount() == 1))
        {
            float[] ray = screenCoordsToRay(e.getX(), e.getY());

            //assume the phone is held 1.5m above ground
            float x = 1.5f / ray[1];

            if (currentLocation != null)
            {
                if (!arObjects.isEmpty())
                {
                    arObjects.get(0).setPosition(currentLocation.latitude + ray[0] * x / 111111.1,
                            currentLocation.longitude + ray[2] * x / (111111.1 * Math.cos(currentLocation.latitude)),
                            -1.5);
                }
            }
        }

        if(e.getPointerCount() > 1)
        {
            if (!arObjects.isEmpty())
                arObjects.get(0).rotation += 1f;
        }

        return true;
    }

    private class ARObject extends Model
    {
        double[] position;
        double rotation;
        double scale;


        public ARObject(ModelData data, Double[] position, double scale)
        {
            super(data);

            this.position = new double[3];
            this.position[0] = position[0];     //latitude
            this.position[1] = position[2];     //height
            this.position[2] = position[1];     //longitude
            this.rotation = position[4];
            this.scale = scale;
        }

        public void render(Shader shader)
        {
            if (currentLocation == null)
                return;

            double deltaLatitude = position[0] - currentLocation.latitude;
            //double deltaAltitude = position[1] - currentLocation.getAltitude();
            double deltaLongitude = position[2] - currentLocation.longitude;


            double latitudeCircumference = 40075160d * Math.cos(Math.toRadians(position[0]));
            double locationDelta[] = new double[3];
            locationDelta[0] = deltaLatitude * 40008000d / 360d;
            //locationDelta[1] = deltaAltitude;
            locationDelta[2] = deltaLongitude * latitudeCircumference / 360d;

            float[] transformationMatrix = new float[16];
            Matrix.setIdentityM(transformationMatrix, 0);
            Matrix.translateM(transformationMatrix, 0, (float) (-locationDelta[0]), (float) position[1], (float) (-locationDelta[2]));
            Matrix.rotateM(transformationMatrix, 0, transformationMatrix, 0, (float)rotation, 0, 1, 0);


            shader.start();
            shader.loadModel(this);

            if (getTexture() != null)
            {
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE0, getTexture().getID());

                shader.loadInt("uTexture", 0);
                shader.loadInt("useTexture", 1);
            } else
            {
                shader.loadInt("useTexture", 0);
            }

            shader.loadFloat("scale", (float) scale);
            shader.loadTransformationMatrix(transformationMatrix);
            shader.renderModel(this);
            shader.stop();
        }

        public void setPosition(double latitude, double longitude, double altitude)
        {
            position[0] = latitude;
            position[1] = altitude;
            position[2] = longitude;
        }
    }

    public class MyGLRenderer implements Renderer
    {

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config)
        {
            GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GLES30.glEnable(GLES20.GL_DEPTH_TEST);
            GLES30.glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);

            shader = new Shader(resourceToString(R.raw.vertex_shader), resourceToString(R.raw.fragment_shader),
                    new String[]{"position", "textureCoords", "normal"});
            shader.start();
            GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GLES30.glEnable(GLES20.GL_DEPTH_TEST);
            GLES30.glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
            shader.stop();
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height)
        {
            GLES30.glViewport(0, 0, width, height);

            float fov = 65f;
            try
            {
                CameraManager cManager = (CameraManager) getContext().getSystemService(CAMERA_SERVICE);
                if(cManager != null)
                    fov = getHFOV(cManager.getCameraCharacteristics(cManager.getCameraIdList()[0]));
            }catch(CameraAccessException ex){ex.printStackTrace();}

            Matrix.perspectiveM(projectionMatrix, 0, fov, (float)width/(float)height, 0.2f, 200f);
            shader.start();
            shader.loadProjectionMatrix(projectionMatrix);
            shader.stop();
        }

        @Override
        public void onDrawFrame(GL10 unused)
        {
            while(!modelsToLoad.isEmpty())
            {
                Double[] pos = arObjectPositions.poll();
                arObjects.add(new ARObject(modelsToLoad.poll(), pos, pos[3]));
            }

            if (currentLocation == null)
                return;

            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
            shader.start();

            float[] tmpM = new float[16];
            float[] tmpM2 = new float[16];
            Matrix.setIdentityM(viewMatrix, 0);
            Matrix.setIdentityM(tmpM, 0);
            Matrix.setIdentityM(tmpM2, 0);

            Matrix.multiplyMM(tmpM2, 0, viewMatrix, 0, cameraMatrix, 0);
            Matrix.setRotateEulerM(tmpM, 0, 90, 180, -90f);
            Matrix.multiplyMM(viewMatrix, 0, tmpM2, 0, tmpM, 0);
            shader.loadViewMatrix(viewMatrix);

            for (ARObject obj : arObjects)
            {
                obj.render(shader);
            }

            shader.stop();
        }
    }
    public void setCameraMatrix(float[] newCameraMatrix)
    {
        System.arraycopy(newCameraMatrix, 0, cameraMatrix, 0, 16);
    }

    public void updateLocation(LatLng newLocation)
    {
        currentLocation = newLocation;
    }

    public void addArObject(Model.ModelData modelData, Double[] location)
    {
        modelsToLoad.add(modelData);
        arObjectPositions.add(location);
    }


    //PRIVATE HELPER FUNCTIONS
    private String resourceToString(int resource)
    {
        StringBuilder result = new StringBuilder();

        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getResources().openRawResource(resource)));
            String line;
            while ((line = reader.readLine()) != null)
            {
                result.append(line).append('\n');
            }
            reader.close();

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return result.toString();
    }

    //returns the fov of the camera
    private float getHFOV(CameraCharacteristics info)
    {
        SizeF sensorSize = info.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
        float[] focalLengths = info.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);

        if (sensorSize != null && focalLengths != null && focalLengths.length > 0) {
            return (float)Math.toDegrees((2.0f * Math.atan(sensorSize.getWidth() / (2.0f * focalLengths[0]))));
        }

        return 65f;
    }

    //returns the fov of the camera
    private float getVFOV(CameraCharacteristics info)
    {
        SizeF sensorSize = info.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
        float[] focalLengths = info.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);

        if (sensorSize != null && focalLengths != null && focalLengths.length > 0) {
            return (float)Math.toDegrees((2.0f * Math.atan(sensorSize.getHeight() / (2.0f * focalLengths[0]))));
        }

        return 65f;
    }

    private float[] screenCoordsToRay(float screenX, float screenY)
    {
        //convert to normalized coordinates
        float normX =   (float)(2.0 * screenX) / getWidth() - 1;
        float normY = (float)-((2.0 * screenY) / getHeight() - 1);

        float[] ray = {normX, normY, -1, 1};

        //convert to eye-coordinates
        float[] invertedProjMat = new float[16];
        Matrix.invertM(invertedProjMat, 0, projectionMatrix, 0);
        Matrix.multiplyMV(ray, 0, invertedProjMat, 0, ray, 0);
        ray[2] = -1;
        ray[3] = 0;

        //convert to world-coordinates
        float[] invertedViewMat = new float[16];
        Matrix.invertM(invertedViewMat, 0, viewMatrix, 0);
        Matrix.multiplyMV(ray, 0, invertedViewMat, 0, ray, 0);

        //normalize vector
        float l = Matrix.length(ray[0], ray[1], ray[2]);
        return new float[]{ray[0] / l, ray[1] / l, ray[2] / l};
    }

}
