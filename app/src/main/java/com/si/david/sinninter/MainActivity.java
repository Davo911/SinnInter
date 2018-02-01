package com.si.david.sinninter;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.si.david.sinninter.arviewer.ArActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity
        implements MapFragment.OnFragmentInteractionListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        locationCardPageFragment.OnLocationCardInteractionListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{
    GoogleApiClient googleApiClient;

    MapFragment mapFragment;
    CalendarFragment calendarFragment;
    ProfileFragment profileFragment;
    JSONArray locations;

    String activeMarker = "";
    boolean menuExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        //init locationServices
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        //parse the json and add the locations to the mapView
        loadLocations("locations");
        mapFragment = MapFragment.newInstance();
        calendarFragment = CalendarFragment.newInstance();
        profileFragment = ProfileFragment.newInstance();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mapFragment);
        fragmentTransaction.commit();

        try
        {
            for (int i = 0; i < locations.length(); i++)
            {
                JSONObject parent = (JSONObject) ((JSONObject) locations.get(i)).get("parent");
                JSONArray children = (JSONArray) ((JSONObject) locations.get(i)).get("children");

                mapFragment.addLocation(parent.getString("name"),
                        parent.getString("subtitle"),
                        parent.getString("description"),
                        new LatLng(parent.getDouble("lat"), parent.getDouble("lng")),
                        null);

                for (int j = 0; j < children.length(); j++)
                {
                    JSONObject child = (JSONObject) children.get(j);

                    mapFragment.addLocation(child.getString("name"),
                            child.getString("subtitle"),
                            child.getString("description"),
                            new LatLng(child.getDouble("lat"), child.getDouble("lng")),
                            parent.getString("name"));
                }

            }
        }catch (Exception e){e.printStackTrace();}

        setUpButtons();

        //switch to MapView
        ImageButton mapButton = (ImageButton)findViewById(R.id.mapButton);
        mapButton.callOnClick();

    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onPause()
    {
        saveLocations("locations.json");
        super.onPause();
    }

    private void setUpButtons()
    {
        ImageButton hamburgerButton = (ImageButton)findViewById(R.id.hamburgerButton);
        ImageButton mapButton = (ImageButton)findViewById(R.id.mapButton);
        ImageButton calendarButton = (ImageButton)findViewById(R.id.calendarButton);
        ImageButton profileButton = (ImageButton)findViewById(R.id.profileButton);
        Drawable menuBackground = ((ImageView)findViewById(R.id.menuBackground)).getDrawable();

        ValueAnimator growBackground = ObjectAnimator.ofInt(menuBackground.getIntrinsicWidth(), (int)(menuBackground.getIntrinsicWidth() * 4.75));
        growBackground.addUpdateListener(animation ->
        {
            menuBackground.setBounds(0, 0, (Integer)animation.getAnimatedValue(), menuBackground.getIntrinsicHeight());
        });
        growBackground.setDuration(150);

        ValueAnimator shrinkBackground = ObjectAnimator.ofInt((int)(menuBackground.getIntrinsicWidth() * 4.75), menuBackground.getIntrinsicWidth());
        shrinkBackground.addUpdateListener(animation ->
        {
            menuBackground.setBounds(0, 0, (Integer)animation.getAnimatedValue(), menuBackground.getIntrinsicHeight());
        });
        shrinkBackground.setDuration(250);

        final Animation popupAnim1 = AnimationUtils.loadAnimation(this, R.anim.popup_button_anim);
        final Animation popupAnim2 = AnimationUtils.loadAnimation(this, R.anim.popup_button_anim);
        final Animation popupAnim3 = AnimationUtils.loadAnimation(this, R.anim.popup_button_anim);
        popupAnim1.setStartOffset(0);
        popupAnim2.setStartOffset(75);
        popupAnim3.setStartOffset(150);

        final Animation hideAnim1 = AnimationUtils.loadAnimation(this, R.anim.hide_button_anim);
        hideAnim1.setStartOffset(100);
        hideAnim1.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                mapButton.setVisibility(ImageButton.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });
        final Animation hideAnim2 = AnimationUtils.loadAnimation(this, R.anim.hide_button_anim);
        hideAnim2.setStartOffset(50);
        hideAnim2.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                calendarButton.setVisibility(ImageButton.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });
        final Animation hideAnim3 = AnimationUtils.loadAnimation(this, R.anim.hide_button_anim);
        hideAnim3.setStartOffset(0);
        hideAnim3.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                profileButton.setVisibility(ImageButton.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });

        hamburgerButton.setOnClickListener(view ->
        {
            if(menuExpanded)
            {
                hamburgerButton.setImageResource(R.drawable.si_menu);

                mapButton.startAnimation(hideAnim1);
                calendarButton.startAnimation(hideAnim2);
                profileButton.startAnimation(hideAnim3);

                shrinkBackground.start();

            }else
            {
                hamburgerButton.setImageResource(R.drawable.si_kreuz_menu);

                mapButton.setVisibility(ImageButton.VISIBLE);
                mapButton.startAnimation(popupAnim1);

                calendarButton.setVisibility(ImageButton.VISIBLE);
                calendarButton.startAnimation(popupAnim2);

                profileButton.setVisibility(ImageButton.VISIBLE);
                profileButton.startAnimation(popupAnim3);
                growBackground.start();
            }
            menuExpanded = !menuExpanded;
        });

        //open map:
        mapButton.setOnClickListener(view ->
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            mapFragment.displayLocationInfo();

            //close the menu
            if(menuExpanded)
                hamburgerButton.callOnClick();
        });

        calendarButton.setOnClickListener(view ->
        {
            mapFragment.displayCalendar();

            //close the menu
            if(menuExpanded)
                hamburgerButton.callOnClick();
        });

        profileButton.setOnClickListener(view ->
        {
            mapFragment.displayProfile();

            //close the menu
            if(menuExpanded)
                hamburgerButton.callOnClick();
        });
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == 1)
        {
            if (permissions.length == 1 &&
                    Objects.equals(permissions[0], Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                googleApiClient.connect();
                mapFragment.enableUserLocation();
            }
        }else if(requestCode == 2)
        {
            if (permissions.length == 2 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {
                Intent myIntent = new Intent(MainActivity.this, ArActivity.class);
                myIntent.putExtra("locations", locations.toString());
                myIntent.putExtra("activeMarker", activeMarker);

                startActivity(myIntent);
            }
        }
    }

    //loads the locations-json and initializes the locations-Object
    private void loadLocations(String resourceName)
    {

        InputStream is = null;
        File cachedFile = new File(getCacheDir(), resourceName + ".json");
        //try loading from cache
        if(false && cachedFile.exists())
            try
            {
                is = (InputStream)new FileInputStream(cachedFile);
            } catch (FileNotFoundException e){e.printStackTrace();}
        else
        {
            //the file doesnt exist in the cache, load from resources
            is = getResources().openRawResource(getResources().getIdentifier(resourceName,
                    "raw", getApplicationContext().getPackageName()));
        }

        Scanner s = new Scanner(is).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";

        try{
            locations = new JSONArray(result);
        }catch (Exception e){e.printStackTrace();}

    }

    //saves the locations-json to cache
    private void saveLocations(String fileName)
    {
        File file;
        FileOutputStream outputStream;

        try {
            file = new File(getCacheDir() + "//" + fileName);
            outputStream = new FileOutputStream(file);
            outputStream.write(locations.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMarkerPositionChanged(String name, LatLng position)
    {
        try
        {
            for (int i = 0; i < locations.length(); i++)
            {
                JSONObject parent = (JSONObject) ((JSONObject) locations.get(i)).get("parent");
                JSONArray children = (JSONArray) ((JSONObject) locations.get(i)).get("children");

                if(parent.getString("name").equals(name))
                {
                    parent.put("lat", position.latitude);
                    parent.put("lng", position.longitude);
                    return;
                }

                for (int j = 0; j < children.length(); j++)
                {
                    JSONObject child = (JSONObject) children.get(j);

                    if(child.getString("name").equals(name))
                    {
                        child.put("lat", position.latitude);
                        child.put("lng", position.longitude);
                        return;
                    }
                }

            }
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public void onActiveMarkerChanged(String name)
    {
        activeMarker = name;
    }

    @Override
    public void onArRequest()
    {
        //request permission to use camera
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, 2);

        //if permission is granted, the new Activity is started in the RequestPermissionResultCallback
    }

    private void updateLocation(Location location)
    {
        mapFragment.updateMyLocation(location);
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

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this::updateLocation);

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
}
