package com.andoid.tk.mymap;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andoid.tk.mymap.Models.PlaceInfo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by TK on 3/21/2018.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener,ItemClickListener {

    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST = 123;
    private static final float DEFAULT_ZOOM = 5F;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -168), new LatLng(71, 136));

    //widgets
    private AutoCompleteTextView mSearchText;
    private ImageView addBtn;
    private ImageView mGps;
    private RecyclerView listView;
    private ImageView btnList;
    private Button btnTravel;

    //vars
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutoCompleteAdapter mPlaceAutoCompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private PlaceInfo mPlace;
    private ArrayList<Address> placesList;
    private Address addressBuffer;
    private ArrayList<Data> datas;
    private RecyclerViewAdapter adapter;
    private LatLng deviceLatLng;
    private int distanceBuffer;
    private int durationBuffer;

    //function select
    private boolean ADD_ADDRESS = false;
    private boolean DRAW_PATH = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mSearchText = (AutoCompleteTextView) findViewById(R.id.input_search);
        addBtn = (ImageView) findViewById(R.id.addBtn);
        mGps = (ImageView) findViewById(R.id.ic_gps);

        btnList = (ImageView) findViewById(R.id.btnList);
        btnTravel = (Button) findViewById(R.id.btnTravel);

        datas = new ArrayList<Data>();

        listView = (RecyclerView) findViewById(R.id.listView);
        listView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter(this, datas);
        adapter.setClickListener((ItemClickListener) this);

        listView.setVisibility(View.INVISIBLE);

        distanceBuffer = 0;
        durationBuffer = 0;

        if (isServicesOK()) {
            getLocalPermission();
        }

    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everyhring is fine
            Log.d(TAG, "isServicesOK: Google Play Services is workning");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void getLocalPermission() {
        Log.d(TAG, "getLocalPermission: getting location permission");
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            }
        } else {
            ActivityCompat.requestPermissions(this, permission, LOCATION_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    private void init() {
        Log.d(TAG, "init: initializing");

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        placesList = new ArrayList<>();
        addressBuffer = null;

        mSearchText.setOnItemClickListener(mAutoCompleteClickListener);

        mPlaceAutoCompleteAdapter = new PlaceAutoCompleteAdapter(this, mGoogleApiClient, LAT_LNG_BOUNDS, null);

        mSearchText.setAdapter(mPlaceAutoCompleteAdapter);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    //exevute search method
                    try {
                        Address addr = geoLocate();
                        LatLng targetLatLng = new LatLng(addr.getLatitude(), addr.getLongitude());
                        moveCamera(targetLatLng, DEFAULT_ZOOM, addr.getAddressLine(0));
                        hideSoftKeyBoard();
                    } catch (NullPointerException e) {
                        Toast.makeText(MapActivity.this, "Can't Find the location", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
                mSearchText.setText("Current Location");
                hideSoftKeyBoard();
            }
        });

        mSearchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSoftKeyBoard();
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addressBuffer = geoLocate();
                if (addressBuffer != null) {
                    for (Address ad : placesList) {
                        if (ad.getLatitude() == addressBuffer.getLatitude() && ad.getLongitude() == addressBuffer.getLongitude())
                            return;
                    }

                    ADD_ADDRESS = true;
                    DRAW_PATH = false;
                    String url = getDirectionsUrl(deviceLatLng, new LatLng(addressBuffer.getLatitude(),addressBuffer.getLongitude()));
                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(url);

                    mSearchText.setText("");
                    hideSoftKeyBoard();
                }
            }
        });

        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listView.getVisibility() == View.VISIBLE) {
                    mGps.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.INVISIBLE);
                } else {
                    mGps.setVisibility(View.INVISIBLE);
                    listView.setVisibility(View.VISIBLE);
                }
            }
        });

        btnTravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDistanceBetweenAllPoints(placesList);
                mMap.clear();
            }
        });

        hideSoftKeyBoard();
    }

    public void getDistanceBetweenAllPoints(ArrayList<Address> locationList)
    {
//        GraphG g = new GraphG(locationList.size());
//        int i = 0, j = 0;
        ArrayList<String> urls = new ArrayList<>();

        for(int i=0;i<locationList.size()-1;i++)
        {
            for(int j=i+1;j<locationList.size();j++)
            {
                Address a = locationList.get(i);
                Address b = locationList.get(j);
                LatLng latLngA = new LatLng(a.getLatitude(),a.getLongitude());
                LatLng latLngB = new LatLng(b.getLatitude(),b.getLongitude());
                String url = getDirectionsUrl(latLngA,latLngB);
                urls.add(url);
            }
        }

        String[] urlArr = new String[urls.size()];

        for(int i=0;i<urls.size();i++)
            urlArr[i] = urls.get(i);

        DistanceDownloadTask distanceDownloadTask = new DistanceDownloadTask();
        distanceDownloadTask.execute(urlArr);

//        for(Address a:locationList)
//        {
//            for(Address b:locationList) {
//                {
//                    String url = getDirectionsUrl(new LatLng(a.getLatitude(), a.getLongitude()), new LatLng(b.getLatitude(), b.getLongitude()));
////                    try {
////                        String data = downloadUrl(url);
////                        DirectionsJSONParser parser = new DirectionsJSONParser();
////                        JSONObject jsonObject = new JSONObject(data);
////                        Integer dis = parser.getDist2points(jsonObject);
////                        g.connect(i, j, dis);
////                    } catch (Exception e) {
////                        Toast.makeText(this, "Can't get Data", Toast.LENGTH_SHORT).show();
////                    }
//                }
//                j++;
//            }
//            i++;
//        }
//        g.showAllNodes();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is ready");
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
        }
    }

    private Address geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            if((mSearchText.getText().toString()).equals("Current Location"))
                list = geocoder.getFromLocation(deviceLatLng.latitude,deviceLatLng.longitude,1);
            else
                list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException" + e.getMessage());
        }

        Address address = null;

        if (list.size() > 0) {
            address = list.get(0);

            Log.d(TAG, "geoLocate: found a location : " + address.toString());

        }
        return address;
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting device location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                Task task = location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location");
                            Location currentLocation = (Location) task.getResult();

                            deviceLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            moveCamera(deviceLatLng, DEFAULT_ZOOM, "My location");

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: SecurutyException: " + e.getMessage());
        }
    }

    public void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving camera to lat : " + latLng.latitude + " lng : " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals("My location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }

        hideSoftKeyBoard();
    }


    public void initMap() {
        Log.d(TAG, "initMap: initialinzing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    private void hideSoftKeyBoard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void showSoftKeyBoard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //-------------------------------------Auto Suggesstion---------------------------------------//
    public AdapterView.OnItemClickListener mAutoCompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            hideSoftKeyBoard();

            final AutocompletePrediction item = mPlaceAutoCompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatesPlaceDetailsCallback);
        }
    };

    public ResultCallback<PlaceBuffer> mUpdatesPlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.d(TAG, "onResult: Place query did not complete successfully " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);

            try {
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                mPlace.setAddress(place.getAddress().toString());
                mPlace.setAttributions(place.getAttributions().toString());
                mPlace.setId(place.getId().toString());
                mPlace.setLatLng(place.getLatLng());
                mPlace.setRating(place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                mPlace.setWebsiteUri(place.getWebsiteUri());

                Log.d(TAG, "onResult: " + mPlace.toString());
            } catch (NullPointerException e) {
                Log.e(TAG, "onResult: NullPointerException" + e.getMessage());
            }

            // moveCamera(new LatLng(place.getViewport().getCenter().latitude,place.getViewport().getCenter().longitude),DEFAULT_ZOOM,mPlace.getName());

            places.release();

        }
    };

    @Override
    public void onItemClick(View view, int position) {

    }

    // getDirection

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

//        String key = "key=AIzaSyBbYiTC1KMK6KFE3k1pOLjzehdZRU6nueU";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
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
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception ", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, DataFromJSON> {

        // Parsing the data in non-ui thread
        @Override
        protected DataFromJSON doInBackground(String... jsonData) {

            JSONObject jObject;
            DataFromJSON dataFromJSON = new DataFromJSON();

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                dataFromJSON = parser.parse(jObject);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return  dataFromJSON;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(DataFromJSON dataFromJSON) {
            List<List<HashMap<String, String>>> result = dataFromJSON.getRoutes();
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            try {
                // Traversing through all the routes
                if(DRAW_PATH) {
                    for (int i = 0; i < result.size(); i++) {
                        points = new ArrayList<LatLng>();
                        lineOptions = new PolylineOptions();

                        // Fetching i-th route
                        List<HashMap<String, String>> path = result.get(i);

                        // Fetching all the points in i-th route
                        for (int j = 0; j < path.size(); j++) {
                            HashMap<String, String> point = path.get(j);

                            double lat = Double.parseDouble(point.get("lat"));
                            double lng = Double.parseDouble(point.get("lng"));
                            LatLng position = new LatLng(lat, lng);

                            points.add(position);
                        }

                        // Adding all the points in the route to LineOptions
                        lineOptions.addAll(points);
                        lineOptions.width(10);
                        lineOptions.color(Color.RED);
                    }
                    // Drawing polyline in the Google Map for the i-th route
                    mMap.addPolyline(lineOptions);
                }



                if (ADD_ADDRESS) {
                    //get distance duration and add them in list
                    distanceBuffer = dataFromJSON.getDistance();
                    durationBuffer = dataFromJSON.getDuration();

                    String durationString = durationFormat(durationBuffer);
                    String distanceString = distanceFormat(distanceBuffer);

                    placesList.add(addressBuffer);
                    Data data = new Data((datas.size() + 1) + "", addressBuffer.getAddressLine(0), distanceString, durationString);
                    datas.add(data);
                    listView.setAdapter(adapter);
                    moveCamera(new LatLng(addressBuffer.getLatitude(), addressBuffer.getLongitude()), DEFAULT_ZOOM, addressBuffer.getFeatureName());
                    Toast.makeText(MapActivity.this, "Address Added", Toast.LENGTH_SHORT).show();
                }

            }
            catch (NullPointerException e)
            {
                Toast.makeText(MapActivity.this, "Can't Find Direction", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onPostExecute: NULL");
            }
        }
    }

    /*--------------------------------- Get Distance Methods ------------------------------*/
    private class DistanceDownloadTask extends AsyncTask<String, Void, String[]> {

        // Downloading data in non-ui thread
        @Override
        protected String[] doInBackground(String... url) {

            // For storing data from web service
            String[] data = new String[url.length];

            try {
                // Fetching the data from web service
                for(int i=0;i<url.length;i++)
                    data[i] = downloadUrl(url[i]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            GetDistanceTask getDistanceTask = new GetDistanceTask();

            // Invokes the thread for parsing the JSON data
            getDistanceTask.execute(result);
        }
    }

    private class GetDistanceTask extends AsyncTask<String, Integer, ArrayList<Integer>> {

        // Parsing the data in non-ui thread
        @Override
        protected ArrayList<Integer> doInBackground(String... jsonData) {

            JSONObject jObject;
            ArrayList<Integer> distances = new ArrayList<>();

            try {
                for(int i = 0 ; i<jsonData.length ;i++)
                {
                    jObject = new JSONObject(jsonData[i]);
                    DirectionsJSONParser parser = new DirectionsJSONParser();
                    Integer dist = parser.getDist2points(jObject);
                    distances.add(dist);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return  distances;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(ArrayList<Integer> distances) {

            GraphG g = new GraphG(placesList.size());

            int k=0;

            try {

                for(int i=0;i<placesList.size()-1;i++)
                {
                    for(int j=i+1;j<placesList.size();j++)
                    {
                        g.connect(i,j,distances.get(k)/1000);
                        k++;
                    }
                }
                g.showAllNodes();
                for(Integer d : distances)
                    System.out.println("Distance : " + d);
                g.TSPNear(0);
                mMap.clear();
                int[] path = g.getPath();
                for(int i=0;i<placesList.size();i++)
                {
                    Address src = placesList.get(path[i]);
                    Address dest = placesList.get(path[i+1]);
                    LatLng srcLatLng = new LatLng(src.getLatitude(),src.getLongitude());
                    LatLng desLatLng = new LatLng(dest.getLatitude(),dest.getLongitude());

                    ADD_ADDRESS = false;
                    DRAW_PATH = true;
                    moveCamera(srcLatLng,DEFAULT_ZOOM,src.getAddressLine(0));
                    String url = getDirectionsUrl(srcLatLng, desLatLng);
                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(url);

                }
                g.showPath();
            }
            catch (NullPointerException e)
            {
                Toast.makeText(MapActivity.this, "Can't Find Direction", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onPostExecute: NULL");
            }
        }
    }


    public String durationFormat(int value)
    {
        int miniutes = (int) Math.ceil(durationBuffer / 60.0);
        String s = miniutes + " min";
        if(miniutes>=60)
        {
            int hr = miniutes/60;
            int min = miniutes%60;
            s = hr + " hr " + min + " min";
        }
        return s;
    }

    public  String distanceFormat(int value)
    {
        int meter = value;
        String s = meter + " m";
        if(meter>=1000)
        {
            double km = meter/1000.0;
            s = km + " km";
        }
        return  s;
    }


}
