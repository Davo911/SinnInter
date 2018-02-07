package com.si.david.sinninter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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

import biz.laenger.android.vpbs.ViewPagerBottomSheetBehavior;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment
{
    MapView mapView;
    GoogleMap googleMap;
    CameraPosition cameraPosition;
    OnFragmentInteractionListener mListener;
    ViewPagerBottomSheetBehavior bottomSheetBehavior;

    LocationPagerFragment locationPagerFragment;
    CalendarFragment calendarFragment;
    ProfileFragment profileFragment;
    boolean locationInfoDisplayed = true;

    LinkedHashMap<String, LocationInfo> locations = new LinkedHashMap<>();
    ArrayList<Polyline> polyLines = new ArrayList<>();

    Bitmap mainLocaitonBitmap;
    Bitmap smallMainLocationBitmap;
    Bitmap spotMarkerBitmap;
    Bitmap inactiveSpotMarkerBitmap;

    Circle radius;
    Circle userPosCenter, userPosRim;

    class LocationInfo
    {
        class ChildLocation
        {
            LatLng coords;
            String title;
            boolean unlocked = false;
            Marker marker;

            public ChildLocation(String name, double lat, double lng, boolean unlocked)
            {
                coords = new LatLng(lat, lng);
                title =  name;
                this.unlocked = unlocked;
            }
        }

        LatLng coords;
        String title, subtitle, description;

        int likes = 0;
        boolean favorite = false;
        boolean notificationsEnabled = false;
        ArrayList<ChildLocation> childLocations = new ArrayList<>();

        Marker marker;

        public LocationInfo(JSONObject obj) throws JSONException
        {
            coords = new LatLng(obj.getDouble("lat"), obj.getDouble("lng"));
            title = obj.getString("name");
            subtitle = obj.getString("subtitle");
            description = obj.getString("description");

            if(obj.has("likes"))
                likes = obj.getInt("likes");

            favorite = obj.has("favorite") && obj.getBoolean("favorite");
            notificationsEnabled = obj.has("notify") && obj.getBoolean("notify");

            JSONArray children = obj.getJSONArray("children");
            for (int i = 0; i < children.length(); i++)
            {
                JSONObject child = children.getJSONObject(i);
                childLocations.add(new ChildLocation(
                        child.getString("name"),
                        child.getDouble("lat"),
                        child.getDouble("lng"),
                        child.getBoolean("unlocked")
                ));
            }
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
            if(result != null)
            {
                for (int i = 0; i < result.size(); i++)
                {
                    points = new ArrayList<>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++)
                    {

                        if (i == 0 && j == 0)
                            j++;

                        if (i == (result.size() - 1) && j == (path.size() - 1))
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
            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null)
            {
                Polyline line = googleMap.addPolyline(lineOptions);
                line.setStartCap(new RoundCap());
                line.setEndCap(new RoundCap());
                line.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
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
        Bitmap markerBitmap = BitmapFactory.decodeResource(getResources(),R.raw.si_marker);

        spotMarkerBitmap = tintImage(Bitmap.createScaledBitmap(markerBitmap, 64, 64, false),
                ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));

        inactiveSpotMarkerBitmap = tintImage(Bitmap.createScaledBitmap(markerBitmap, 64, 64, false),
                ContextCompat.getColor(getContext(), R.color.colorPrimaryDesaturated));

        Bitmap parentMarkerBitmap = BitmapFactory.decodeResource(getResources(), R.raw.si_spot);

        mainLocaitonBitmap = tintImage(Bitmap.createScaledBitmap(parentMarkerBitmap, 128, 128, false),
                ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));

        smallMainLocationBitmap = tintImage(Bitmap.createScaledBitmap(parentMarkerBitmap, 64, 64, false),
                ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        TabLayout pageIndicator = (TabLayout)view.findViewById(R.id.pageIndicator);

        if(locationPagerFragment == null)
            locationPagerFragment = LocationPagerFragment.newInstance();

        locationPagerFragment.setupTabLayout(pageIndicator);

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

                    //reset all markers first
                    for (Map.Entry<String, LocationInfo> mapEntry : locations.entrySet())
                    {
                        LocationInfo loc = mapEntry.getValue();

                        loc.marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMainLocationBitmap));
                        loc.marker.setAnchor(0.5f, 0.5f);

                        for(LocationInfo.ChildLocation child : loc.childLocations)
                        {
                            child.marker.setVisible(false);
                        }
                    }

                    String title = ((TextView) (f.getView().findViewById(R.id.title))).getText().toString();
                    LocationInfo location = locations.get(title);

                    for(LocationInfo.ChildLocation child : location.childLocations)
                    {
                        if(child.unlocked)
                            child.marker.setIcon(BitmapDescriptorFactory.fromBitmap(spotMarkerBitmap));
                        else
                            child.marker.setIcon(BitmapDescriptorFactory.fromBitmap(inactiveSpotMarkerBitmap));

                        child.marker.setVisible(true);
                        new FetchUrl().execute(buildUrl(child.coords, location.coords));
                    }

                    location.marker.setIcon(BitmapDescriptorFactory.fromBitmap(mainLocaitonBitmap));
                    location.marker.setAnchor(0.5f, 0.9f);

                    cameraPosition = new CameraPosition.Builder()
                            .target(location.marker.getPosition())
                            .zoom(cameraPosition.zoom)
                            .build();
                    animateCamera();

                }
            }


            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });

        final View bottomSheet = view.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = ViewPagerBottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setBottomSheetCallback(new ViewPagerBottomSheetBehavior.BottomSheetCallback(){
            @Override
            public void onStateChanged(View bottomSheet, int newState)
            {

                if(newState  == BottomSheetBehavior.STATE_DRAGGING ||
                        newState  == BottomSheetBehavior.STATE_SETTLING )
                {
                    getView().findViewById(R.id.imageView).animate().alpha(0f).setDuration(250);
                    getView().findViewById(R.id.pageIndicator).animate().alpha(0f).setDuration(250);

                }
                if (newState == BottomSheetBehavior.STATE_EXPANDED)
                {
                    locationPagerFragment.setExpanded();
                }
                else if (newState == BottomSheetBehavior.STATE_COLLAPSED)
                {
                    locationPagerFragment.setCollapsed();
                    getView().findViewById(R.id.imageView).animate().alpha(1f).setDuration(250);
                    getView().findViewById(R.id.pageIndicator).animate().alpha(1f).setDuration(250);
                    if(!locationInfoDisplayed)
                        displayLocationInfo();

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
            googleMap.setPadding(0, 0, 0, bottomSheetBehavior.getPeekHeight());
            radius = googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(0,90))
                    .radius(150)
                    .strokeColor(getResources().getColor(R.color.circleStrokeColor))
                    .strokeWidth(3f)
                    .fillColor(getResources().getColor(R.color.circleFillColor)));
            userPosCenter = googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(0,90))
                    .radius(8)
                    .strokeColor(getResources().getColor(R.color.circleStrokeColor))
                    .strokeWidth(3f)
                    .fillColor(getResources().getColor(R.color.circleStrokeColor)));
            userPosRim = googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(0,90))
                    .radius(35)
                    .strokeColor(getResources().getColor(R.color.circleStrokeColor))
                    .strokeWidth(11f));

            for (Map.Entry<String, LocationInfo> mapEntry : locations.entrySet())
            {
                LocationInfo loc = mapEntry.getValue();

                loc.marker = googleMap.addMarker(new MarkerOptions()
                        .position(loc.coords)
                        .title(loc.title)
                        .icon(BitmapDescriptorFactory.fromBitmap(smallMainLocationBitmap))
                        .anchor(0.5f, 0.5f)
                        .draggable(false));

                for(LocationInfo.ChildLocation child : loc.childLocations)
                {
                    child.marker = googleMap.addMarker(new MarkerOptions()
                            .position(child.coords)
                            .title(child.title)
                            .icon(BitmapDescriptorFactory.fromBitmap(spotMarkerBitmap))
                            .anchor(0.5f, 0.5f)
                            .draggable(false)
                            .visible(false));
                }
            }

            googleMap.setOnMarkerClickListener(marker ->
            {
                locationPagerFragment.setActive(marker.getTitle());
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
                    //mListener.onMarkerPositionChanged(marker.getTitle(), marker.getPosition());
                    //locations.get(marker.getTitle()).coords = marker.getPosition();
                }
            });

            // For zooming automatically to the coords of the marker when the map has loaded
            if(cameraPosition != null)
            {
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
            else
            {
                googleMap.setOnMapLoadedCallback(() ->
                {
                    if(!locationPagerFragment.locationCards.isEmpty())
                    {
                        cameraPosition = new CameraPosition.Builder()
                                .target(locationPagerFragment.locationCards.get(0).locationInfo.coords)
                                .zoom(15f)
                                .build();
                    }
                    else
                    {
                        cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(51.050862, 13.733363))
                                .zoom(15f)
                                .build();
                    }
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
                "&mode=walking" +
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

    public void addLocation(JSONObject location) throws JSONException
    {
        LocationInfo loc = new LocationInfo(location);
        locations.put(loc.title, loc);

        if(googleMap != null)
        {
            loc.marker = googleMap.addMarker(new MarkerOptions()
                    .position(loc.coords)
                    .title(loc.title)
                    .draggable(true));

            for(LocationInfo.ChildLocation child : loc.childLocations)
            {
                child.marker = googleMap.addMarker(new MarkerOptions()
                        .position(child.coords)
                        .title(child.title)
                        .draggable(false));
            }

        }

        locationPagerFragment.addLocation(loc);
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
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        if(googleMap != null)
            cameraPosition = googleMap.getCameraPosition();

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

    public void updateMyLocation(android.location.Location location)
    {
        if(userPosCenter != null)
        {
            LatLng newPosition = new LatLng(location.getLatitude(), location.getLongitude());
            radius.setCenter(newPosition);
            userPosCenter.setCenter(newPosition);
            userPosRim.setCenter(newPosition);
        }
    }

    public void displayLocationInfo()
    {
        if(bottomSheetBehavior == null)
            return;

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, locationPagerFragment);
        fragmentTransaction.commit();

        View view = getView();
        if(view != null)
        {
            TabLayout pageIndicator = (TabLayout)view.findViewById(R.id.pageIndicator);
            pageIndicator.setVisibility(View.VISIBLE);

            ImageView imageview = (ImageView)view.findViewById(R.id.imageView);
            imageview.setVisibility(View.VISIBLE);
        }

        getChildFragmentManager().executePendingTransactions();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        locationInfoDisplayed = true;
    }

    public void displayCalendar()
    {
        if(bottomSheetBehavior == null)
            return;

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, calendarFragment);
        fragmentTransaction.commit();

        View view = getView();
        if(view != null)
        {
            TabLayout pageIndicator = (TabLayout)view.findViewById(R.id.pageIndicator);
            pageIndicator.setVisibility(View.INVISIBLE);

            ImageView imageview = (ImageView)view.findViewById(R.id.imageView);
            imageview.setVisibility(View.INVISIBLE);
        }

        getChildFragmentManager().executePendingTransactions();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        locationInfoDisplayed = false;
    }

    public void displayProfile()
    {
        if(bottomSheetBehavior == null)
            return;

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, profileFragment);
        fragmentTransaction.commit();

        View view = getView();
        if(view != null)
        {
            TabLayout pageIndicator = (TabLayout)view.findViewById(R.id.pageIndicator);
            pageIndicator.setVisibility(View.INVISIBLE);

            ImageView imageview = (ImageView)view.findViewById(R.id.imageView);
            imageview.setVisibility(View.INVISIBLE);
        }

        getChildFragmentManager().executePendingTransactions();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        locationInfoDisplayed = false;
    }

    public static Bitmap tintImage(Bitmap bitmap, int color)
    {
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        Bitmap bitmapResult = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapResult);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return bitmapResult;
    }

    public void animateCamera()
    {
        if(googleMap != null)
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public boolean unlockSpot()
    {
        if(userPosCenter != null)
        {
            LocationInfo.ChildLocation unlockedLocation = locationPagerFragment.unlock(userPosCenter.getCenter());
            if(unlockedLocation != null)
            {
                unlockedLocation.marker.setIcon(BitmapDescriptorFactory.fromBitmap(spotMarkerBitmap));
                return true;
            }
        }
        return false;
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
