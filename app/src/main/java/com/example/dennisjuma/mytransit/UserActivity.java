package com.example.dennisjuma.mytransit;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.example.dennisjuma.mytransit.model.Getter;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;



public class UserActivity extends Fragment
        implements GoogleApiClient.ConnectionCallbacks {

    DatabaseReference databaseReference, profileInfo;
    ListView listView;
    FirebaseListAdapter<Getter> firebaseListAdapter;
    GeoFire geoFire;
    DatabaseReference sendCurrentLocationToDatabase;
    GeoQuery geoQuery;
    String uid;
    String Type;
    double longtitude, latitude;
    GoogleApiClient mGoogleApiClient;
    private static final int SECOND_MILLIS = 60;
    private static final int MINUTE_MILLIS = 1 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootview =  inflater.inflate(R.layout.content_user, container, false);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();

        listView = (ListView) rootview.findViewById(R.id.listView);

        return rootview;
    }

    @Override
    public void onDestroy() {
        DatabaseReference deleteFeed = FirebaseDatabase.getInstance().getReference().child("feed");
        deleteFeed.child(uid).removeValue();
        super.onDestroy();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

            getDataFromQueryLocation();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void getDataFromQueryLocation(){

        sendCurrentLocationToDatabase = FirebaseDatabase.getInstance().getReference();

        profileInfo = FirebaseDatabase.getInstance().getReference().child("users")
                .child(uid);

        profileInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> map = (Map)dataSnapshot.getValue();
                Type = map.get("Type");

                GeoFire geoFireUser = new GeoFire(sendCurrentLocationToDatabase);
                String sendLocationRef = "geofire/by_user_location/"+Type+"/"+uid;
                geoFireUser.setLocation(sendLocationRef, new GeoLocation(latitude, longtitude));

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("geofire");
                geoFire = new GeoFire(ref);

                geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longtitude), 10);

                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(final String key, GeoLocation location) {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                                .child("Sacco_requests").child(Type).child(key);
                        databaseReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                DatabaseReference addFeed = FirebaseDatabase.getInstance().getReference().child("feed")
                                        .child(uid).child(key);
                                addFeed.setValue(dataSnapshot.getValue());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onKeyExited(String key) {
                        DatabaseReference removeFeed = FirebaseDatabase.getInstance().getReference().child("feed")
                                .child(uid).child(key);
                        removeFeed.removeValue();
                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {

                    }

                    @Override
                    public void onGeoQueryReady() {

                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {

                    }
                });


                databaseReference = FirebaseDatabase.getInstance().getReference().child("feed")
                        .child(uid);
                firebaseListAdapter = new FirebaseListAdapter<Getter>(
                        getActivity(),
                        Getter.class,
                        R.layout.sacco_listview,
                        databaseReference
                ) {
                    @Override
                    protected void populateView(View v, final Getter model, int position) {
                        TextView date = (TextView) v.findViewById(R.id.textViewDate);
                        TextView reason = (TextView) v.findViewById(R.id.textViewReason);
                        TextView names = (TextView) v.findViewById(R.id.textViewNames);
                        TextView hname = (TextView) v.findViewById(R.id.textViewHname);
                        TextView placeAddress = (TextView) v.findViewById(R.id.textViewPlaceAddress);
                        TextView gender = (TextView) v.findViewById(R.id.textViewGender);



                        reason.setText("Reason: "+model.getReason());
                        names.setText(model.getFull_names());
                        hname.setText(model.getHname());
                        placeAddress.setText(model.getPlaceAddress());
                        gender.setText(model.getGender());

                        long time = model.getTimestamp();
                        long now = System.currentTimeMillis() / 1000;
                        long diff = now - time;
                        if (diff < MINUTE_MILLIS) {
                            date.setText(" just now");
                        } else if (diff < 2 * MINUTE_MILLIS) {
                            date.setText(" a minute ago");
                        } else if (diff < 50 * MINUTE_MILLIS) {
                            date.setText(" "+diff / MINUTE_MILLIS + " minutes ago");
                        } else if (diff < 90 * MINUTE_MILLIS) {
                            date.setText(" an hour ago");
                        } else if (diff < 24 * HOUR_MILLIS) {
                            date.setText(" "+diff / HOUR_MILLIS + " hours ago");
                        } else if (diff < 48 * HOUR_MILLIS) {
                            date.setText(" yesterday");
                        } else {
                            date.setText(" "+diff / DAY_MILLIS + " days ago");
                        }

                        v.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                DatabaseReference views = FirebaseDatabase.getInstance().getReference().child("Views");
                                views.child(model.getHname()).child(model.getPostKey()).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(true);

                                Intent intent = new Intent(getActivity(), UserAcceptActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putLong("date", model.getTimestamp());
                                bundle.putString("reason", model.getReason());
                                bundle.putString("names", model.getFull_names());
                                bundle.putString("hname", model.getHname());
                                bundle.putString("place", model.getPlaceAddress());
                                bundle.putString("gender", model.getGender());
                                bundle.putString("placeId", model.getPlaceId());
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        });
                    }
                };

                listView.setAdapter(firebaseListAdapter);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    listView.setNestedScrollingEnabled(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}