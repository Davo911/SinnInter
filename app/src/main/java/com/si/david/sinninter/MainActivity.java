package com.si.david.sinninter;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
        locationCardPageFragment.OnArRequestListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LoginFragment.LoginPageListener
{
    GoogleApiClient googleApiClient;

    MapFragment mapFragment;
    CalendarFragment calendarFragment;
    ProfileFragment profileFragment;
    LoginFragment loginFragment;
    JSONArray locations;

    String activeMarker = "";
    boolean menuExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
*/

        //init locationServices
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //parse the json and add the_locations to the mapView
        loadLocations("locations");
        mapFragment = MapFragment.newInstance();
        calendarFragment = CalendarFragment.newInstance();
        profileFragment = ProfileFragment.newInstance();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mapFragment);
        fragmentTransaction.commit();

        loginFragment = LoginFragment.newInstance();
        FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
        fragmentTransaction2.replace(R.id.login_container, loginFragment);
        fragmentTransaction2.commit();

        try
        {
            for (int i = 0; i < locations.length(); i++)
            {
                JSONObject location = (JSONObject)locations.get(i);
                mapFragment.addLocation(location);
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
        saveLocations("_locationss.json");
        super.onPause();
    }

    private void setUpButtons()
    {
        ImageButton hamburgerButton = (ImageButton)findViewById(R.id.hamburgerButton);
        ImageButton mapButton = (ImageButton)findViewById(R.id.mapButton);
        ImageButton calendarButton = (ImageButton)findViewById(R.id.calendarButton);
        ImageButton profileButton = (ImageButton)findViewById(R.id.profileButton);
        ImageButton locationButton = (ImageButton)findViewById(R.id.arButton);
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

        locationButton.setOnClickListener(view -> {
            mapFragment.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if(mapFragment.unlockSpot())
            {
                displayDialog("Kategorien freigeschaltet", 1500,
                        R.drawable.si_geschichte_kalender, R.drawable.si_architekt_profil, R.drawable.si_bilder,
                        null);
            }
            else
            {
                displayDialog("nichts neues freigeschaltet", 2000, 0,0,0,
                        "Begib dich zu unbesuchten Spots, um mehr Informationen freizuschalten");
            }
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

    //loads the _locations-json and initializes the _locations-Object
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

    //saves the _locations-json to cache
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

    void displayDialog(String title,int closeTimer, int res1, int res2, int res3, String subtitle)
    {
        if (mapFragment.bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED)
        {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_layout, null);
            ((TextView) dialogView.findViewById(R.id.dialog_title)).setText(title);

            ((ImageView) dialogView.findViewById(R.id.dialogImage1)).setImageResource(res1);
            ((ImageView) dialogView.findViewById(R.id.dialogImage2)).setImageResource(res2);
            ((ImageView) dialogView.findViewById(R.id.dialogImage3)).setImageResource(res3);

            if(subtitle != null)
            {
                ((TextView)dialogView.findViewById(R.id.subtitle)).setText(subtitle);
            }

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            lp.y = displayMetrics.heightPixels / 2 + (int) (150f / displayMetrics.density);
            dialog.getWindow().setAttributes(lp);
            dialog.show();

            new CountDownTimer(closeTimer, closeTimer)
            {

                @Override
                public void onTick(long millisUntilFinished)
                {
                }

                @Override
                public void onFinish()
                {
                    dialog.dismiss();
                }
            }.start();
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


    //locationCardListeners
    @Override
    public void onArRequest()
    {
        //request permission to use camera
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, 2);

        //if permission is granted, the new Activity is started in the RequestPermissionResultCallback
    }

    @Override
    public void onClickArrow()
    {
        if(mapFragment.bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
        {
            mapFragment.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }else if(mapFragment.bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
        {
            mapFragment.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
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

    @Override
    public void onLoginSuccessful()
    {
        new CountDownTimer(100, 100)
        {
            public void onTick(long millisUntilFinished)
            {
            }

            public void onFinish()
            {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_out, R.anim.slide_out);
                fragmentTransaction.remove(loginFragment);
                fragmentTransaction.commit();
            }
        }.start();

        new CountDownTimer(400, 400)
        {
            public void onTick(long millisUntilFinished)
            {
            }

            public void onFinish()
            {
                mapFragment.animateCamera();
            }
        }.start();
    }
}
