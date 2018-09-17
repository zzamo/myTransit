package com.example.dennisjuma.mytransit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SaccoSignUp extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    android.widget.Spinner spinner;
    EditText SaccoName, phone, email;
    private EditText password, confirmPassword;
    Button signUp, addLocation;
    private FirebaseAuth mAuth;
    String Spinner;
    int PLACE_PICKER_REQUEST = 1;
    ProgressDialog progressDialog;
    double longtitude, latitude;
    GoogleApiClient mGoogleApiClient;
    String addressLocation, placeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sacco_sign_up);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        phone = (EditText) findViewById(R.id.editTextPhone);
        password = (EditText) findViewById(R.id.editTextPassword);
        confirmPassword = (EditText) findViewById(R.id.editTextConfirmPassword);
        email = (EditText) findViewById(R.id.editTextEmail);
        SaccoName = (EditText) findViewById(R.id.editHospitalName);
        signUp = (Button) findViewById(R.id.buttonSignUp);
        addLocation = (Button) findViewById(R.id.buttonAddLocation);

        addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLocation();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Password = password.getText().toString();
                String ConfrirmPassword = confirmPassword.getText().toString();
                final String Email = email.getText().toString();
                final String Phone = phone.getText().toString();
                final String Sname = SaccoName.getText().toString();

                if (Email.equals("") || Password.equals("") || Sname.equals("") || Phone.equals("")){
                    Toast.makeText(SaccoSignUp.this, "Please fill in all details", Toast.LENGTH_SHORT).show();
                }else if (!ConfrirmPassword.equals(Password)){
                    Toast.makeText(SaccoSignUp.this, "Please confirm password", Toast.LENGTH_SHORT).show();
                }else if (placeId == null){
                    Toast.makeText(SaccoSignUp.this, "Please add a location", Toast.LENGTH_SHORT).show();
                }
                else {
                    progressDialog.setMessage("Signing you up");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    mAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()){
                                Toast.makeText(SaccoSignUp.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }else {
                                Map map = new HashMap();
                                map.put("Sname", Sname);
                                map.put("phone", Phone);
                                map.put("email", Email);
                                map.put("type", "Sacco");
                                map.put("placeAddress", addressLocation);
                                map.put("placeId", placeId);

                                DatabaseReference signUpUsers = FirebaseDatabase.getInstance().getReference();
                                signUpUsers.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(map);
                                progressDialog.dismiss();
                                Toast.makeText(SaccoSignUp.this, "Account created", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });
                }
            }
        });

    }

    private void changeLocation(){
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            startActivityForResult(intent, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, "Please install Google Play Services!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                placeId = place.getId().toString();
                addressLocation = place.getAddress().toString();
                addLocation.setText("Change Location");
            }
        }

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
            Toast.makeText(SaccoSignUp.this, ""+longtitude+" "+latitude, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}