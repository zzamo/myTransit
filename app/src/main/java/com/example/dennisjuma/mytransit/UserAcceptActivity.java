package com.example.dennisjuma.mytransit;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.maps.android.SphericalUtil;
import com.google.android.gms.common.api.GoogleApiClient;

public class UserAcceptActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    DatabaseReference databaseReference, profileInfo;
    private static final int SECOND_MILLIS = 60;
    private static final int MINUTE_MILLIS = 1 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    MapView mMapView;
    String Route;
    String Names;
    String Hname;
    long Date;
    String Reason;
    String PlaceAddress;
    String PLaceId;
    String Gender;
    TextView route, names, hName, date, reason, placeAddress, distance, gender;
    double latitude, longtitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_accept);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Hname = bundle.getString("hname");
        Route = bundle.getString("route");
        Names = bundle.getString("names");
        Date = bundle.getLong("date");
        Reason = bundle.getString("reason");
        PlaceAddress = bundle.getString("place");
        PLaceId = bundle.getString("placeId");
        Gender = bundle.getString("gender");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();

        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        hName = (TextView) findViewById(R.id.textViewHname);
        names = (TextView) findViewById(R.id.textViewNames);
        route = (TextView) findViewById(R.id.textViewBloodType);
        gender = (TextView) findViewById(R.id.textViewGender);
        reason = (TextView) findViewById(R.id.textViewReason);
        date = (TextView) findViewById(R.id.textViewDate);
        placeAddress = (TextView) findViewById(R.id.textViewAddressLocation);

        hName.setText(Hname);
        names.setText(Names);
        placeAddress.setText(PlaceAddress);
        reason.setText(Reason);
        route.setText(Route);
        gender.setText(Gender);

        long time = Date;
        long now = System.currentTimeMillis() / 1000;
        long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            date.setText(" just now");
        } else if (diff < 2 * MINUTE_MILLIS) {
            date.setText(" a minute ago");
        } else if (diff < 50 * MINUTE_MILLIS) {
            date.setText(" " + diff / MINUTE_MILLIS + " minutes ago");
        } else if (diff < 90 * MINUTE_MILLIS) {
            date.setText(" an hour ago");
        } else if (diff < 24 * HOUR_MILLIS) {
            date.setText(" " + diff / HOUR_MILLIS + " hours ago");
        } else if (diff < 48 * HOUR_MILLIS) {
            date.setText(" yesterday");
        } else {
            date.setText(" " + diff / DAY_MILLIS + " days ago");
        }

        startMapActivity(PLaceId);

    }

    private void startMapActivity(final String placeId) {
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(UserAcceptActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            MapsInitializer.initialize(UserAcceptActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(UserAcceptActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserAcceptActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                googleMap.setMyLocationEnabled(true);
            }
        });

        Places.GeoDataApi
                .getPlaceById(mGoogleApiClient, placeId)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                       @Override
                                       public void onResult(PlaceBuffer places) {
                                           //Place place = PlacePicker.getPlace(places, this);
                                           LatLng location = places.get(0).getLatLng();
                                           TextView distance = (TextView) findViewById(R.id.textViewDistance);
                                           final LatLng myLocation = new LatLng(latitude, longtitude);
                                           final LatLng destination = new LatLng(places.get(0).getLatLng().latitude, places.get(0).getLatLng().longitude);
                                           double distanceMeter = SphericalUtil.computeDistanceBetween(myLocation, destination);
                                           distance.setText("Distance: "+ String.format("%.2f", distanceMeter) +" meters");
                                           googleMap.addMarker(new MarkerOptions().position(destination));
                                           CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(11).build();
                                           googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                           googleMap.addCircle(new CircleOptions()
                                                   .center(myLocation)
                                                   .fillColor(R.color.colorAccent)
                                                   .radius(10000)
                                                   .strokeColor(R.color.colorAccent));

                                           places.release();
                                       }
                                   }
                );
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            longtitude = mLastLocation.getLongitude();
            latitude = mLastLocation.getLatitude();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}

