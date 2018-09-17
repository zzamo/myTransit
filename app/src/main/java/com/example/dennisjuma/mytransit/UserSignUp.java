package com.example.dennisjuma.mytransit;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.Spinner;
        import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
        import java.util.Map;

class UserSignUp extends AppCompatActivity implements  GoogleApiClient.ConnectionCallbacks {

    Spinner spinner;
    EditText fname, hname, phone, nationaId, email,password, confirmPassword;
    public Button signUp;
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    double longitude, latitude;
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_up);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        fname = (EditText) findViewById(R.id.editTextFname);
        hname = (EditText) findViewById(R.id.editTextSname);
        phone = (EditText) findViewById(R.id.editTextPhone);
        password = (EditText) findViewById(R.id.editTextPassword);
        confirmPassword = (EditText) findViewById(R.id.editTextConfirmPassword);
        nationaId = (EditText) findViewById(R.id.editTextNationalID);
        email = (EditText) findViewById(R.id.editTextEmail);
        signUp = (Button) findViewById(R.id.buttonSignUp);



        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String Fname = fname.getText().toString();
                final String Hname = hname.getText().toString();
                final String NationalId = nationaId.getText().toString();
                String Password = password.getText().toString();
                String ConfirmPassword = confirmPassword.getText().toString();
                final String Email = email.getText().toString();
                final String Phone = phone.getText().toString();

                if (Email.equals("") || Password.equals("") || Fname.equals("") || Hname.equals("")
                        || NationalId.equals("") || Phone.equals("")) {
                    Toast.makeText(UserSignUp.this, "Please fill in all details", Toast.LENGTH_SHORT).show();
                } else if (!ConfirmPassword.equals(Password)) {
                    Toast.makeText(UserSignUp.this, "Please confirm password", Toast.LENGTH_SHORT).show();
                }  else {
                    progressDialog.setMessage("Signing you up");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    mAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(UserSignUp.this, "Unable to sign you up", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            } else {

                                Map map = new HashMap();
                                map.put("fname", Fname);
                                map.put("hname", Hname);
                                map.put("nationaId", NationalId);
                                map.put("phone", Phone);
                                map.put("email", Email);
                                map.put("type", "User");
                                map.put("longitude", longitude);
                                map.put("latitude", latitude);

                                DatabaseReference signUpUsers = FirebaseDatabase.getInstance().getReference();
                                signUpUsers.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(map);
                                progressDialog.dismiss();
                                Toast.makeText(UserSignUp.this, "Account created", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });
                }
            }
        });
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
            longitude = mLastLocation.getLongitude();
            latitude = mLastLocation.getLatitude();
            Toast.makeText(UserSignUp.this, ""+longitude+" "+latitude, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}