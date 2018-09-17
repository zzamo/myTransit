package com.example.dennisjuma.mytransit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dennisjuma.mytransit.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity {

    EditText email, passsword;
    Button logIn, signUp;
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog, progressDialog2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkIfUserIsSignedIn();

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        signUp = (Button) findViewById(R.id.buttonRegister);
        logIn = (Button) findViewById(R.id.buttonLogIn);
        email = (EditText) findViewById(R.id.editTextEmail);
        passsword = (EditText) findViewById(R.id.editTextPassword);

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = email.getText().toString();
                String Password = passsword.getText().toString();

                if (Email.equals("") || Password.equals("")){
                    Toast.makeText(MainActivity.this, "Please fill in all details", Toast.LENGTH_SHORT).show();
                }else {
                    progressDialog.setMessage("Signing you in");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    mAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()){
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, "Unable to sign you in", Toast.LENGTH_SHORT).show();
                            }else {
                                checkIfUserIsSignedIn();
                                Toast.makeText(MainActivity.this, "Sign in successfull", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
                }
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChooseType.class);
                startActivity(intent);
            }
        });
    }

    private void checkIfUserIsSignedIn(){

        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }else{
            // Write you code here if permission already given.
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null){
            progressDialog2 = new ProgressDialog(this);
            progressDialog2.setMessage("Signing you in");
            progressDialog2.setCancelable(false);
            progressDialog2.show();
//          User is signed in
            DatabaseReference chechTypeOfUser = FirebaseDatabase.getInstance().getReference().child("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            chechTypeOfUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("type").getValue().equals("Sacco")){
                        Intent intent = new Intent(MainActivity.this, SaccoTabActivity.class);
                        startActivity(intent);
                        progressDialog2.dismiss();
                        finish();
                    }else if (dataSnapshot.child("type").getValue().equals("User")){
                        DatabaseReference registerToken = FirebaseDatabase.getInstance().getReference().child("notificationTokens");
                        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

                        registerToken.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(refreshedToken).setValue(true);

                        Intent intent = new Intent(MainActivity.this, UserTabActivity.class);
                        startActivity(intent);
                        progressDialog2.dismiss();
                        finish();
                    }else {
                        Intent intent = new Intent(MainActivity.this, AdminTabActivity.class);
                        startActivity(intent);
                        progressDialog2.dismiss();
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, "Unknown Error "+databaseError, Toast.LENGTH_SHORT).show();
                }
            });
        }else {
//            User not signed in
        }
    }

}