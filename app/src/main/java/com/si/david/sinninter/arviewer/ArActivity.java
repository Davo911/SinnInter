package com.si.david.sinninter.arviewer;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.si.david.sinninter.R;
import com.si.david.sinninter.arviewer.renderer.Model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;

public class ArActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SensorEventListener
{
    private GoogleApiClient googleApiClient;

    private Camera camera;
    private CameraView cameraPreview;

    private ArGLSurfaceView glSurfaceView;

    private float[] rotationMatrix = new float[16];      //deviceRotation

    private String activeMarker = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getSupportActionBar().hide();

        setContentView(R.layout.ar_layout);

        //init googleApiClient for location updates
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //init camera preview
        camera = Camera.open();
        camera.setDisplayOrientation(90);
        cameraPreview = new CameraView(getBaseContext(), camera);
        FrameLayout previewContainer = (FrameLayout) findViewById(R.id.camera_preview);
        previewContainer.addView(cameraPreview);

        //init openGLView
        glSurfaceView = new ArGLSurfaceView(getApplicationContext());
        FrameLayout glFrame = (FrameLayout) findViewById(R.id.gl_frame);
        glFrame.addView(glSurfaceView);
        glSurfaceView.setZOrderMediaOverlay(true);

        //parse the json containting the diffenent _locations
        Bundle extras = getIntent().getExtras();
        activeMarker = extras.getString("activeMarker");
        String json = extras.getString("locations");
        JSONArray jsonObjects;

        try
        {
            jsonObjects = new JSONArray(json);

            for(int i = 0; i < jsonObjects.length(); i++)
            {
                JSONObject object = (JSONObject)jsonObjects.get(i);
                if(object.getString("name").equals(activeMarker))
                {
                    addObject(object);
                    break;
                }
            }

            for(int i = 0; i < jsonObjects.length(); i++)
            {
                JSONObject object = (JSONObject)jsonObjects.get(i);
                if(!object.getString("name").equals(activeMarker))
                {
                    addObject(object);
                }
            }

        }catch(Exception e){e.printStackTrace();}

        //set up orientation sensor
        SensorManager sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        if(sensorManager == null)
            onBackPressed();

        Sensor rotationVecSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (rotationVecSensor ==  null)
            onBackPressed();

        sensorManager.registerListener(this, rotationVecSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void addObject(JSONObject object)
    {
        try
        {
            Model.ModelData modelData = Model.ModelData.fromOBJ(
                    getResources().openRawResource(getResources().getIdentifier(object.getString("model"),
                            "raw", getApplicationContext().getPackageName())));

            if(object.has("texture"))
            {
                InputStream is = getResources().openRawResource(getResources().getIdentifier(object.getString("texture"), "raw", getApplicationContext().getPackageName()));
                modelData.setTexture(BitmapFactory.decodeStream(is));
            }

            addObject(modelData, new LatLng(object.getDouble("lat"), object.getDouble("lng")),
                    object.getDouble("alt"), object.getDouble("scale"), object.getDouble("rot"));

        }catch(Exception e){e.printStackTrace();}
    }


    public void addObject(Model.ModelData model, LatLng position, double altitude, double scale, double rotation)
    {
        glSurfaceView.addArObject(model, new Double[]{position.latitude, position.longitude, altitude, scale, rotation});
    }

    @Override
    public void onStart()
    {
        googleApiClient.connect();
        super.onStart();
    }

    //GOOGLEAPICLIENT CONNECTION EVENTS
    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(10);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, location ->
        {
            glSurfaceView.updateLocation(new LatLng(location.getLatitude(), location.getLongitude()));
        });

    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        //TODO: notify user
    }

    //SENSOR EVENTS
    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
        {
            //retrieve orientation vector
            float[] tmpRotMatrix = new float[16];
            SensorManager.getRotationMatrixFromVector(tmpRotMatrix, sensorEvent.values);

            //smooth out sensorData
            for (int i = 0; i < 16; i++)
            {
                rotationMatrix[i] = rotationMatrix[i] * 0.75f + tmpRotMatrix[i] * 0.25f;
            }
            glSurfaceView.setCameraMatrix(rotationMatrix);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {

    }
}
