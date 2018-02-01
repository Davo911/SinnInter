package com.si.david.sinninter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static android.content.ContentValues.TAG;
import static android.content.Context.SENSOR_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements
        SensorEventListener
{
    //sensordata:
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;
    private float[] valuesAccelerometer;
    private float[] valuesMagneticField;
    private float[] matrixR;
    private float[] matrixI;
    private float[] matrixValues;

    MapView mapView;
    GoogleMap googleMap;
    CameraPosition cameraPosition;
    OnFragmentInteractionListener mListener;
    BottomSheetBehavior bottomSheetBehavior;

    LocationPagerFragment locationPagerFragment;
    CalendarFragment calendarFragment;
    ProfileFragment profileFragment;

    LinkedHashMap<String, Location> locations = new LinkedHashMap<>();
    ArrayList<Polyline> polyLines = new ArrayList<>();

    Bitmap customMarkerBitmap;
    Bitmap parentMarkerBitmap;
    Bitmap myLocationBitmap;

    Marker myLocation;

    class Location{
        LatLng position;
        String parent;
        Marker marker;

        private Location(LatLng position, String parent)
        {
            this.position = position;
            this.parent = parent;
        }
    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... url)
        {
            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e)
            {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>>
    {
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData)
        {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DataParser parser = new DataParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result)
        {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++)
            {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++)
                {

                    if(i==0 && j == 0)
                        j++;

                    if(i == (result.size() - 1) && j == (path.size() - 1))
                        break;


                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.WHITE);
            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null)
            {
                Polyline line = googleMap.addPolyline(lineOptions);
                line.setStartCap(new RoundCap());
                line.setEndCap(new RoundCap());
                polyLines.add(line);
            }
            else
            {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }

    public class DataParser
    {
        /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
        public List<List<HashMap<String,String>>> parse(JSONObject jObject){

            List<List<HashMap<String, String>>> routes = new ArrayList<>() ;
            JSONArray jRoutes;
            JSONArray jLegs;
            JSONArray jSteps;

            try {

                jRoutes = jObject.getJSONArray("routes");

                /** Traversing all routes */
                for(int i=0;i<jRoutes.length();i++){
                    jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                    List path = new ArrayList<>();

                    /** Traversing all legs */
                    for(int j=0;j<jLegs.length();j++){
                        jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");

                        /** Traversing all steps */
                        for(int k=0;k<jSteps.length();k++){
                            String polyline = "";
                            polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);

                            /** Traversing all points */
                            for(int l=0;l<list.size();l++){
                                HashMap<String, String> hm = new HashMap<>();
                                hm.put("lat", Double.toString((list.get(l)).latitude) );
                                hm.put("lng", Double.toString((list.get(l)).longitude) );
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }catch (Exception e){
            }

            return routes;
        }

        /**
         * Method to decode polyline points
         * Courtesy : https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
         * */
        private List<LatLng> decodePoly(String encoded)
        {
            List<LatLng> poly = new ArrayList<>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }

            return poly;
        }
    }

    public MapFragment()
    {
        // Required empty public constructor
    }

    public static MapFragment newInstance()
    {
        MapFragment fragment = new MapFragment();
        fragment.locationPagerFragment = LocationPagerFragment.newInstance();
        fragment.calendarFragment = CalendarFragment.newInstance();
        fragment.profileFragment = ProfileFragment.newInstance();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        sensorManager = (SensorManager)getContext().getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        valuesMagneticField = new float[3];
        valuesAccelerometer = new float[3];
        matrixR = new float[9];
        matrixI = new float[9];
        matrixValues = new float[3];

        Bitmap markerBitmap = BitmapFactory.decodeResource(getResources(),R.raw.si_marker);
        customMarkerBitmap = Bitmap.createScaledBitmap(markerBitmap, 64, 64, false);

        Bitmap parentMarkerBitmap = BitmapFactory.decodeResource(getResources(), R.raw.si_spot);
        this.parentMarkerBitmap = Bitmap.createScaledBitmap(parentMarkerBitmap, 128, 128, false);

        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        Bitmap markerBitmap2 = BitmapFactory.decodeResource(getResources(), R.raw.si_gps_button);
        myLocationBitmap = Bitmap.createScaledBitmap(
                Bitmap.createBitmap(markerBitmap2, 0, 0, markerBitmap2.getWidth(), markerBitmap2.getHeight(), matrix, true),
                100, 100, false);

        super.onCreate(savedInstanceState);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        locationPagerFragment.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {

            }

            @Override
            public void onPageSelected(int position)
            {
                if(googleMap != null)
                {
                    for (Polyline line : polyLines)
                    {
                        line.remove();
                    }

                    Fragment f = locationPagerFragment.locationCards.get(position);

                    if (f.getView() == null)
                        return;

                    String title = ((TextView) (f.getView().findViewById(R.id.title))).getText().toString();
                    String parent = locations.get(title).parent;
                    if (parent == null)
                        parent = title;

                    //draw new directions and update markers
                    for (Map.Entry<String, Location> location : locations.entrySet())
                    {
                        if (location.getValue().parent != null &&
                                location.getValue().parent.equals(parent))
                        {
                            new FetchUrl().execute(buildUrl(location.getValue().position,
                                    locations.get(location.getValue().parent).position));

                            locations.get(location.getValue().parent).marker.setIcon(
                                    BitmapDescriptorFactory.fromBitmap(parentMarkerBitmap));
                            locations.get(location.getValue().parent).marker.setAnchor(0.5f, 0.9f);
                        } else
                        {
                            location.getValue().marker.setIcon(
                                    BitmapDescriptorFactory.fromBitmap(customMarkerBitmap));
                            location.getValue().marker.setAnchor(0.5f, 0.5f);
                        }
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });

        final View bottomSheet = view.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback(){
            @Override
            public void onStateChanged(View bottomSheet, int newState)
            {
                if (newState == BottomSheetBehavior.STATE_EXPANDED)
                {
                    locationPagerFragment.setCollapsed();
                }
                else if (newState == BottomSheetBehavior.STATE_COLLAPSED)
                {
                    locationPagerFragment.setExpanded();
                }
                else if (newState == BottomSheetBehavior.STATE_HIDDEN)
                {
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset)
            {
            }
        });

        displayLocationInfo();

        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(mMap -> {
            googleMap = mMap;

            //add style to Map
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),R.raw.map_style));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }

            googleMap.setIndoorEnabled(false);
            googleMap.getUiSettings().setCompassEnabled(false);

            myLocation = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(90,0))
                    .title("myLocation")
                    .icon(BitmapDescriptorFactory.fromBitmap(myLocationBitmap))
                    .anchor(0.5f, 0.5f)
                    .visible(false)
                    .flat(true));

            for (Map.Entry<String, Location> location : locations.entrySet())
            {
                location.getValue().marker = googleMap.addMarker(new MarkerOptions()
                        .position(location.getValue().position)
                        .title(location.getKey())
                        .icon(BitmapDescriptorFactory.fromBitmap(customMarkerBitmap))
                        .anchor(0.5f, 0.5f)
                        .draggable(true));
            }

            googleMap.setOnMarkerClickListener(marker ->
            {
                mListener.onActiveMarkerChanged(marker.getTitle());

                return false;
            });

            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener()
            {
                @Override
                public void onMarkerDragStart(Marker marker)
                {

                }

                @Override
                public void onMarkerDrag(Marker marker)
                {

                }

                @Override
                public void onMarkerDragEnd(Marker marker)
                {
                    mListener.onMarkerPositionChanged(marker.getTitle(), marker.getPosition());
                    locations.get(marker.getTitle()).position = marker.getPosition();
                }
            });

            // For zooming automatically to the location of the marker when the map has loaded
            if(cameraPosition != null)
            {
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
            else
            {
                googleMap.setOnMapLoadedCallback(() ->
                {
                    cameraPosition = new CameraPosition.Builder().target(new LatLng(51.050862, 13.733363)).zoom(13f).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                });
            }
        });

        try
        {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return view;
    }

    private String buildUrl(LatLng start, LatLng end)
    {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                start.latitude + "," + start.longitude + "&destination=" +
                end.latitude + "," + end.longitude  +
                "&key=" + getString(R.string.google_maps_direction_request_key);
    }

    private String downloadUrl(String strUrl) throws IOException
    {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    public void addLocation(String title, String subtitle, String description, LatLng position, String parent)
    {
        locations.put(title, new Location(position, parent));

        if(googleMap != null)
        {
            googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(title)
                    .draggable(true));
        }

        locationPagerFragment.addLocation(title, subtitle, description, position, parent);
    }

    @Override
    public void onConfigurationChanged (Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL);

        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {

        if(googleMap != null)
            cameraPosition = googleMap.getCameraPosition();

        sensorManager.unregisterListener(this, sensorAccelerometer);
        sensorManager.unregisterListener(this, sensorMagneticField);

        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle state)
    {
        //save map-state
        if(googleMap != null)
        {
            CameraPosition position = googleMap.getCameraPosition();

            state.putFloat("lat", (float) position.target.latitude);
            state.putFloat("lng", (float) position.target.longitude);
            state.putFloat("zoom", position.zoom);
            state.putFloat("tilt", position.tilt);
            state.putFloat("bearing", position.bearing);
        }
        mapView.onSaveInstanceState(state);
        super.onSaveInstanceState(state);

    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @SuppressLint("MissingPermission")
    public void enableUserLocation()
    {

        if(myLocation != null)
        {
            myLocation.setVisible(true);
        }
    }

    public void updateMyLocation(android.location.Location location)
    {
        if(myLocation != null)
        {
            myLocation.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }

    public void displayLocationInfo()
    {
        if(bottomSheetBehavior == null)
            return;

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, locationPagerFragment);
        fragmentTransaction.commit();

        getChildFragmentManager().executePendingTransactions();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void displayCalendar()
    {
        if(bottomSheetBehavior == null)
            return;

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, calendarFragment);
        fragmentTransaction.commit();

        getChildFragmentManager().executePendingTransactions();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void displayProfile()
    {
        if(bottomSheetBehavior == null)
            return;

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, profileFragment);
        fragmentTransaction.commit();

        getChildFragmentManager().executePendingTransactions();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    //SENSOR EVENTS
    @Override
    public void onSensorChanged(SensorEvent event)
    {
        switch(event.sensor.getType())
        {
            case Sensor.TYPE_ACCELEROMETER:
                for ( int i = 0; i < event.values.length; i++ )
                {
                    valuesAccelerometer[i] = valuesAccelerometer[i] + 0.5f * (event.values[i] - valuesAccelerometer[i]);
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                for ( int i = 0; i < event.values.length; i++ )
                {
                    valuesMagneticField[i] = valuesMagneticField[i] + 0.5f * (event.values[i] - valuesMagneticField[i]);
                }
                break;
        }

        if(valuesAccelerometer != null && valuesMagneticField != null)
        {
            boolean success = SensorManager.getRotationMatrix(matrixR, matrixI,
                    valuesAccelerometer, valuesMagneticField);

            if (success)
            {
                SensorManager.getOrientation(matrixR, matrixValues);

                if (myLocation != null)
                {
                    myLocation.setRotation((float)Math.toDegrees(matrixValues[0]) + 55f);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener
    {
        void onMarkerPositionChanged(String name, LatLng position);
        void onActiveMarkerChanged(String name);
    }
}
