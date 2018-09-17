package com.example.dennisjuma.mytransit;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.cardemulation.HostApduService;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.util.HashMap;
import java.util.Map;

import android.widget.TextView;

public class SaccoTabActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    ProgressDialog progressDialog;
    DatabaseReference profileInfo;
    Double longt, lat;
    Spinner spinner, spinnerGender;
    String Spinner, SpinnerGender;
    GeoFire geoFire;
    GoogleApiClient mGoogleApiClient;

    String Hname, PlaceAddress, PlaceId;
    Button addLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sacco_tab);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();

        progressDialog = new ProgressDialog(this);

        profileInfo = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        profileInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> map = (Map)dataSnapshot.getValue();
                Hname = map.get("hname");
                PlaceAddress = map.get("placeAddress");
                PlaceId = map.get("placeId");

                Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, PlaceId)
                        .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                               @Override
                                               public void onResult(PlaceBuffer places) {
                                                   //Place place = PlacePicker.getPlace(places, this);
                                                   longt = places.get(0).getLatLng().longitude;
                                                   lat = places.get(0).getLatLng().latitude;
                                                   places.release();
                                               }
                                           }
                        );
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequst();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sacco_tab, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(SaccoTabActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_sacco_tab, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    SaccoActivity saccoActivity = new SaccoActivity();
                    return saccoActivity;
                case 1:
                    SaccoMapActivity saccoMapActivity = new SaccoMapActivity();
                    return saccoMapActivity;
                case 2:
                    SaccoProfileActivity saccoProfileActivity = new SaccoProfileActivity();
                    return saccoProfileActivity;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "REQUESTS";
                case 1:
                    return "NEARBY USERS";
                case 2:
                    return "PROFILE";
            }
            return null;
        }
    }

    private void sendRequst(){
        LayoutInflater inflater = this.getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.add_request, null);
        final EditText reason = (EditText) alertLayout.findViewById(R.id.editTextReason);
        final EditText fullNames = (EditText) alertLayout.findViewById(R.id.editTextNames);
        spinner = (Spinner) alertLayout.findViewById(R.id.spinner);
        spinnerGender = (Spinner) alertLayout.findViewById(R.id.spinnerGender);


        ArrayAdapter<CharSequence> adapterGender = ArrayAdapter.createFromResource(this,
                R.array.gender, android.R.layout.simple_spinner_item);


        adapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        spinnerGender.setAdapter(adapterGender);

        spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerGender = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Add A Request");
        alert.setView(alertLayout);
        alert.setCancelable(false);
        alert.setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                progressDialog.setMessage("Sending Request");
                progressDialog.setCancelable(false);
                progressDialog.show();
                String Reason = reason.getText().toString().trim();
                //FirebaseAuth auth = FirebaseAuth.getInstance();
                if (Reason.equals("")){
                    Toast.makeText(SaccoTabActivity.this, "Please enter a reason", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }else if (Spinner.equals("Choose  type")) {
                    Toast.makeText(SaccoTabActivity.this, "Please choose a category", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }else if (SpinnerGender.equals("Choose gender")) {
                    Toast.makeText(SaccoTabActivity.this, "Please choose a category", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }else {
                    DatabaseReference sendRequest = FirebaseDatabase.getInstance().getReference().child("hospital_requests");
                    DatabaseReference individualHospitalRequests = FirebaseDatabase.getInstance().getReference().child("individual_hospital_requests");

                    final String postKey = sendRequest.child(Spinner).push().getKey();

                    final Map map = new HashMap();
                    map.put("full_names", fullNames.getText().toString());
                    map.put("reason", Reason);
                    map.put("bloodType", Spinner);
                    map.put("gender", SpinnerGender);
                    map.put("hname", Hname);
                    map.put("postKey", postKey);
                    map.put("timestamp", System.currentTimeMillis()/1000);
                    map.put("placeId", PlaceId);
                    map.put("placeAddress", PlaceAddress);

                    sendRequest.child(Spinner).child(postKey).setValue(map);
                    individualHospitalRequests.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(postKey).setValue(map);

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("geofire");
                    geoFire = new GeoFire(databaseReference);

                    geoFire.setLocation("by_blood_type/"+Spinner+"/"+postKey, new GeoLocation(lat, longt));
                    geoFire.setLocation("by_location"+"/"+postKey, new GeoLocation(lat, longt));

                    DatabaseReference getUserLocations  = FirebaseDatabase.getInstance().getReference().child("geofire/by_user_location/"+Spinner);
                    GeoFire geoFire1 = new GeoFire(getUserLocations);

                    GeoQuery geoQuery = geoFire1.queryAtLocation(new GeoLocation(lat, longt), 10);

                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {
                            DatabaseReference sendNotifications = FirebaseDatabase.getInstance().getReference().child("notifications");
                            sendNotifications.child(key).child(postKey).setValue(map);
                        }

                        @Override
                        public void onKeyExited(String key) {

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

                    Toast.makeText(SaccoTabActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }

            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }


}
