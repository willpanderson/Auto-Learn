package com.example.auto_learn_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;


public class SignupActivity extends AppCompatActivity {
    Button btnSignUp;
    EditText email, passwod,passwod2,name;
    FirebaseAuth mfirebaseAuth;
    FirebaseUser user;
    FirebaseAnalytics mAnalytics;
    androidx.appcompat.widget.Toolbar toolbar;
    private String name2 = new String("");
    private ImageView mProfile;
    private Bitmap profileBitmap;
    private Uri selectedImage;
    private boolean uploaded_photo = false;


    // Defining Permission codes.
    // We can give any value
    // but unique for each permission.
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        toolbar = findViewById(R.id.toolbarSignup);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sign up form");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mfirebaseAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.emailReset);
        passwod = findViewById(R.id.passwordSignup);
        passwod2 = findViewById(R.id.confirmSignup);
        btnSignUp = findViewById(R.id.next_mav_button);
        name = findViewById(R.id.nameSignup);
        mProfile= findViewById(R.id.signup_picture);
        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email2 = email.getText().toString();
                String pass = passwod.getText().toString();
                String pass2 = passwod2.getText().toString();
                name2 += name.getText().toString();
                if (email2.isEmpty()) {
                    email.setError("Enter a email");
                    email.requestFocus();
                } else if (pass.isEmpty()) {
                    passwod.setError("Enter a password");
                    passwod.requestFocus();
                } else if (pass2.isEmpty()) {
                    passwod2.setError("Enter a password");
                    passwod2.requestFocus();
                } else if(name2.isEmpty()){
                    name.setError("Enter a password");
                    name.requestFocus();
                } else {

                    if (pass.equals(pass2))
                    {
                        mfirebaseAuth.createUserWithEmailAndPassword(email2, pass).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "Sign up failed", Toast.LENGTH_SHORT).show();
                                } else {
                                    user = mfirebaseAuth.getCurrentUser();
                                    if (user != null) {
                                        if (uploaded_photo) {
                                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                    .setPhotoUri(selectedImage).build();
                                            user.updateProfile(profileUpdates);
                                        }

                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(name2).build();
                                        user.updateProfile(profileUpdates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Intent s = new Intent(SignupActivity.this, ProfileIDSignup.class);
                                                            startActivity(s);
                                                        }
                                                    }
                                                });

                                    }
                                }
                            }
                        });
                    }
                    else
                    {
                        passwod2.setError("Password must match");
                        passwod2.requestFocus();
                    }

                }

            }
        });
    }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // handle arrow click here
            if (item.getItemId() == android.R.id.home) {
                Intent loginIntent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish(); // close this activity and return to preview activity (if there is any)
            }

            return super.onOptionsItemSelected(item);
    }




    // Prompted by clicking the profile image, which calls checkPermission to see if we can access the camera or gallery
    private void selectImage() {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
        builder.setTitle("Select photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo"))
                {
                    checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
                }
                else if (options[item].equals("Choose from Gallery"))
                {
                    checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    // Check to see if permissions are allowed to access camera / gallery
    // if its jump to startActivityForResult()
    // If there is no permission prompt the user for access
    public void checkPermission(String permission, int requestCode)
    {
        Intent intent;
        if (ContextCompat.checkSelfPermission(SignupActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(SignupActivity.this, new String[] { permission }, requestCode);
        }
        else {
            switch (requestCode) {
                case CAMERA_PERMISSION_CODE:
                    Toast.makeText(SignupActivity.this, "Opening camera", Toast.LENGTH_SHORT).show();
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    startActivityForResult(intent, 1);
                    break;
                case STORAGE_PERMISSION_CODE:
                    Toast.makeText(SignupActivity.this, "Opening gallery", Toast.LENGTH_SHORT).show();
                    intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);


                    startActivityForResult(intent, 2);
                    break;
            }
        }
    }

    // Check the status of permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case CAMERA_PERMISSION_CODE: {

                // If they allowed access to the camera then open the camera
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(SignupActivity.this, "Opening camera", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    startActivityForResult(intent,1);

                }

                // Otherwise display that the permissions were denied
                else {
                    Toast.makeText(SignupActivity.this,
                            "Camera Permission Denied",
                            Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            }
            case STORAGE_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(SignupActivity.this, "Opening gallery", Toast.LENGTH_SHORT).show();
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(intent, 2);
                }
                else {
                    Toast.makeText(SignupActivity.this,
                            "Storage Permission Denied",
                            Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        selectedImage = data.getData();
        if (resultCode == RESULT_OK) {
            if (requestCode == 1)
            {
                profileBitmap = (Bitmap) data.getExtras().get("data");
                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), profileBitmap);
                roundedBitmapDrawable.setCircular(true);
                mProfile.setImageDrawable(roundedBitmapDrawable);
            }
            else if (requestCode == 2)
            {
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                profileBitmap = (BitmapFactory.decodeFile(picturePath));
                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), profileBitmap);
                roundedBitmapDrawable.setCircular(true);
                mProfile.setImageDrawable(roundedBitmapDrawable);
            }
        }
        uploaded_photo = true;
    }

}