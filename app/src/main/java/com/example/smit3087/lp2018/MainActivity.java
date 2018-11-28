package com.example.smit3087.lp2018;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapView mapView;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng dest;
    private LocationRequest locationRequest;
    private LatLng curLocation;
    private FusedLocationProviderClient providerClient;
    private RequestQueue requestQueue;
    private LocationCallback locationCallback;
    private Polyline curPoly;
    private static final int PHONE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        providerClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create()
                .setInterval(10000).setFastestInterval(5000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location loc = locationResult.getLastLocation();
                curLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
                super.onLocationResult(locationResult);
            }
        };
        //checks permissions for location request
        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    //checks permissions
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 12:
                //if permission is granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocation();
                }
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */


    public void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET}, 12);
            }
            return;
        }
        //this line updates location
        mMap.setMyLocationEnabled(true);
        providerClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        requestLocation();

        //defines picker request code and builds intent builder for place picker
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }
    }

    //this code retrieves the user's chosen place

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                dest = place.getLatLng();
                getDirections();
                getDistance(); //consider storing in variable, using button to display info
                //After this I should handle result error and result cancelled - the above only handles valid places
            }
        }
    }

    public void findDestination(View view) {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }
    }
    //creates polyline for directions
    public void getDirections() {
        if (curLocation == null) {
            Toast.makeText(getApplicationContext(), "Retrieving location - Try Again", Toast.LENGTH_LONG).show();
            return;
        }
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + curLocation.latitude + "," + curLocation.longitude +
                "&destination=" + dest.latitude + "," + dest.longitude +
                "&mode=walking" +
                "&key=" + getString(R.string.google_maps_key);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            //callback for string request
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject data = new JSONObject(response);
                    String encodedPolyline = data.getJSONArray("routes").getJSONObject(0).
                            getJSONObject("overview_polyline").getString("points");
                    List<LatLng> points = PolyUtil.decode(encodedPolyline);
                    if (curPoly != null) {
                        curPoly.remove();
                    }
                    curPoly = mMap.addPolyline(new PolylineOptions().addAll(points));
                    curPoly.setColor(getColor(R.color.colorAccent));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error retrieving directions", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(request);
    }

    //gets distance between origin and destination
    public void getDistance() {
        if (curLocation == null) {
            Toast.makeText(getApplicationContext(), "Retrieving location - Try Again", Toast.LENGTH_LONG).show();
            return;
        }
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial" +
                "&origins=" + curLocation.latitude + "," + curLocation.longitude +
                "&destinations=" + dest.latitude + "," + dest.longitude +
                "&mode=walking" +
                "&key=" + getString(R.string.google_maps_key);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            //callback for when get distance returns data
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject data = new JSONObject(response);
                    String distance = data.getJSONArray("rows").getJSONObject(0).getJSONArray("elements")
                            .getJSONObject(0).getJSONObject("distance").getString("text");
                    String duration = data.getJSONArray("rows").getJSONObject(0).getJSONArray("elements")
                            .getJSONObject(0).getJSONObject("duration").getString("text");
                    Toast.makeText(getApplicationContext(), "Distance: " + distance + "\nDuration: " + duration, Toast.LENGTH_LONG).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error retrieving directions", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(request);
    }

    //this code create my options menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    //this code responds to options being clicked (launches new activities)
    public boolean onOptionsItemSelected(MenuItem item) {
        //if user selects 'call safewalk'
        if (item.getItemId() == R.id.call_SW) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:2603504538")); //Once tested, change # to safewalk
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        PHONE_REQUEST);
            } else {
                startActivity(callIntent);
            }
        }
        //plan trip
        if (item.getItemId() == R.id.plan_trip) {
            startActivity(new Intent(MainActivity.this, Main_to_Plan.class));
            return true;
        }
        //text buddy
        if (item.getItemId() == R.id.text_friend) {

        }
        return super.onOptionsItemSelected(item);
    }

    //stops pinging for location when app is closed
    @Override
    protected void onPause() {
        super.onPause();
        providerClient.removeLocationUpdates(locationCallback);
    }
    //resumes once map is created
    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            requestLocation();
        }
    }
}
