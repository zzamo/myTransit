package com.example.dennisjuma.mytransit;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dennisjuma.mytransit.model.CircleTransform;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class EditUserProfile extends AppCompatActivity {

    EditText fname, sname, email, nationalId, phone;
    ProgressDialog progressDialog;
    ImageView profileImage;
    DatabaseReference profileInfo;
    String Fname, Sname, NationalId, Phone, ProfileUrl;
    private static final int CAMERA_REQUEST_CODE = 1;
    private Uri downloadUri, uri;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    Button update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(this);

        profileInfo = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        profileImage = (ImageView) findViewById(R.id.imageViewProfileImage);
        fname = (EditText) findViewById(R.id.editTextFname);
        sname = (EditText) findViewById(R.id.editTextSname);
        phone = (EditText) findViewById(R.id.editTextPhone);
        nationalId = (EditText) findViewById(R.id.editTextNationaId);
        update = (Button) findViewById(R.id.buttonUpdate);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference updateProfileInfo = FirebaseDatabase.getInstance().getReference().child("users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                final String Fname = fname.getText().toString();
                final String Sname = sname.getText().toString();
                final String NationalId = nationalId.getText().toString();
                final String Phone = phone.getText().toString();

                if (Fname.equals("") || Sname.equals("") || NationalId.equals("") || Phone.equals("")) {
                    Toast.makeText(EditUserProfile.this, "Please fill in all details", Toast.LENGTH_SHORT).show();
                }else {

                    Map map = new HashMap();
                    map.put("fname", Fname);
                    map.put("sname", Sname);
                    map.put("nationaId", NationalId);
                    map.put("phone", Phone);

                    updateProfileInfo.updateChildren(map);
                    Toast.makeText(EditUserProfile.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        profileInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> map = (Map)dataSnapshot.getValue();
                Fname = map.get("fname");
                fname.setText(Fname);
                Sname = map.get("sname");
                sname.setText(Sname);
                NationalId = map.get("nationaId");
                nationalId.setText(NationalId);
                Phone = map.get("phone");
                phone.setText(Phone);

                ProfileUrl = map.get("profileUrl");

                Glide.with(EditUserProfile.this).load(ProfileUrl).bitmapTransform(new CircleTransform(EditUserProfile.this)).into(profileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePhoto();
            }
        });

    }

    private void changePhoto(){
        String [] items = new String[]{"Take Photo", "Choose From Gallery"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditUserProfile.this, android.R.layout.select_dialog_item, items);
        AlertDialog.Builder builder = new AlertDialog.Builder(EditUserProfile.this);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(camera, CAMERA_REQUEST_CODE);
                    dialog.cancel();
                }else {
                    Intent gallery = new Intent();
                    gallery.setAction(Intent.ACTION_GET_CONTENT);
                    gallery.setType("image/*");
                    startActivityForResult(gallery, CAMERA_REQUEST_CODE);
                    dialog.cancel();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {

            uri = data.getData();

            CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                progressDialog.setMessage("Uploading Your Image");
                progressDialog.show();
                progressDialog.setCancelable(false);

                Uri resultUri = result.getUri();

                StorageReference filepath = storageReference.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("Photos").child("Profile Image").child(UUID.randomUUID()+uri.getLastPathSegment());


                filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        downloadUri = taskSnapshot.getDownloadUrl();
                        databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child("profileUrl").setValue(downloadUri.toString());
                        Toast.makeText(EditUserProfile.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                        Glide.with(EditUserProfile.this).load(downloadUri.toString()).bitmapTransform(new CircleTransform(EditUserProfile.this)).into(profileImage);
                        progressDialog.dismiss();
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}
