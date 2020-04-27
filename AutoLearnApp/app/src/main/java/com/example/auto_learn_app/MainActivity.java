package com.example.auto_learn_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ResultDialog.ResultDialogListener {
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mToggle;
    androidx.appcompat.widget.Toolbar toolbar;
    ImageView IDProf;
    ImageView mProfile;
    FloatingActionButton mButton;
    Button classifyButton;
    private TextView name,utaID;
    private Bitmap bitmap;
    private Bitmap profileBitmap;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    FirebaseAnalytics mAnalytics;
    private FirebaseVisionImage image;
    private String[] result = new String[6];
    private boolean uploaded_for_model = false;
    private boolean uploaded_for_profile = false;
    private boolean has_been_uploaded = false;


    // Defining Permission codes.
    // We can give any value
    // but unique for each permission.
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the Firebase Authenticator and user
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mAnalytics = FirebaseAnalytics.getInstance(MainActivity.this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Display the Navigation view
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Create an instance of the navigation header
        // in order to access the contents in the lines below
        View hView =  navigationView.getHeaderView(0);

        // Update the name of the user in navigation header
        name = (TextView) hView.findViewById(R.id.profile_name);
        utaID = (TextView) hView.findViewById(R.id.profile_id);
        if (user != null)
            name.setText(user.getDisplayName());
        // Declare our image views
        IDProf=(ImageView)findViewById(R.id.IDProf);
        mProfile=(ImageView) hView.findViewById(R.id.profile_picture);

        // Set the current profile picture
        if (user != null)
        {
            Uri firebaseProfile = user.getPhotoUrl();
            if (firebaseProfile != null)
            {
                mProfile.setImageURI(firebaseProfile);
                profileBitmap = ((BitmapDrawable) mProfile.getDrawable()).getBitmap();
                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), profileBitmap);
                roundedBitmapDrawable.setCircular(true);
                mProfile.setImageDrawable(roundedBitmapDrawable);
            }

            // If there is no profile picture saved set the default
            else
            {
                profileBitmap = ((BitmapDrawable) mProfile.getDrawable()).getBitmap();
                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), profileBitmap);
                roundedBitmapDrawable.setCircular(true);
                mProfile.setImageDrawable(roundedBitmapDrawable);
            }
        }

        // Listener to update profile picture
        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploaded_for_model = false;
                uploaded_for_profile = true;
                selectImage();
            }
        });

        // Listener to run model
        mButton = findViewById(R.id.GalleryButton);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploaded_for_model = true;
                uploaded_for_profile = false;
                 selectImage();
            }
        });


        // Display the results again by running the model
        classifyButton = findViewById(R.id.classificationButton);
        classifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uploaded_for_model || has_been_uploaded )
                    runModel();
                else
                {
                    Toast.makeText(MainActivity.this, "Please upload a photo", Toast.LENGTH_SHORT);
                }
            }
        });

        // Display the navigation drawer
        mDrawerLayout = findViewById(R.id.drawer_layout);

        // Add a toggle to allow button on toolbar to open navigation view
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        // Set the toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar,menu);
        return super.onCreateOptionsMenu(menu);
    }
    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode)
    {
        Intent intent;
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
        else {
            switch (requestCode) {
                case CAMERA_PERMISSION_CODE:
                    Toast.makeText(MainActivity.this, "Opening camera", Toast.LENGTH_SHORT).show();
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (uploaded_for_model)
                        startActivityForResult(intent,1);
                    else if (uploaded_for_profile)
                        startActivityForResult(intent, 3);
                    break;
                case STORAGE_PERMISSION_CODE:
                    Toast.makeText(MainActivity.this, "Opening gallery", Toast.LENGTH_SHORT).show();
                    intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    if (uploaded_for_model)
                        startActivityForResult(intent,2);
                    else if (uploaded_for_profile)
                        startActivityForResult(intent, 4);
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case CAMERA_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Opening camera", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (uploaded_for_model)
                        startActivityForResult(intent,1);
                    else if (uploaded_for_profile)
                        startActivityForResult(intent, 3);
                }
                else {
                    Toast.makeText(MainActivity.this,
                            "Camera Permission Denied",
                            Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            }
            case STORAGE_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Opening gallery", Toast.LENGTH_SHORT).show();
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    if (uploaded_for_model)
                        startActivityForResult(intent,2);
                    else if (uploaded_for_profile)
                        startActivityForResult(intent, 4);
                }
                else {
                    Toast.makeText(MainActivity.this,
                            "Storage Permission Denied",
                            Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            }
        }

    }

    private void selectImage() {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                bitmap = (Bitmap) data.getExtras().get("data");
                IDProf.setImageBitmap(bitmap);
                image = FirebaseVisionImage.fromBitmap(bitmap);
                has_been_uploaded = true;
                runModel();
                
            } else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                bitmap = (BitmapFactory.decodeFile(picturePath));
                bitmap=getResizedBitmap(bitmap, 400);
                Log.w("path of image from gallery......******************.........", picturePath+"");
                IDProf.setImageBitmap(bitmap);
                image = FirebaseVisionImage.fromBitmap(bitmap);
                has_been_uploaded = true;
                runModel();
            }
            else if (requestCode == 3)
            {
                profileBitmap = (Bitmap) data.getExtras().get("data");
                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), profileBitmap);
                roundedBitmapDrawable.setCircular(true);
                mProfile.setImageDrawable(roundedBitmapDrawable);
            }
            else if (requestCode == 4)
            {
                final Uri selectedImage = data.getData();
                if (user != null)
                {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(selectedImage).build();
                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
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
                            });
                }

            }
        }
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mToggle.onOptionsItemSelected(item))
            return true;
        switch (item.getItemId())
        {
            case R.id.stats:
                Toast.makeText(this, "Statistics clicked", Toast.LENGTH_SHORT);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.nav_settings:
                Intent profileIntent = new Intent(MainActivity.this,ProfileSettings.class);
                startActivity(profileIntent);
                finish();
                break;
            case R.id.nav_about:
                Toast.makeText(this, "Info", Toast.LENGTH_SHORT).show();

                Intent infoIntent = new Intent(MainActivity.this, InformationActivity.class);
                startActivity(infoIntent);
                finish();
                break;
            case R.id.nav_logout:
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

                signOut();
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
                break;
        }

        return true;
    }
    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        FirebaseUser mUser = null;

    }
    private void openDialog() {
        ResultDialog resultDialog = new ResultDialog();
        resultDialog.show(getSupportFragmentManager(),"result dialog");

    }

    @Override
    public Bitmap getImage() {
        return bitmap;
    }

    @Override
    public String[] getResults() { return result; }


    public void runModel() {

        // Build the model
        FirebaseAutoMLLocalModel localModel = new FirebaseAutoMLLocalModel.Builder()
                .setAssetFilePath("manifest.json")
                .build();

        // Generate a labeler
        FirebaseVisionImageLabeler labeler;
        try {
            final FirebaseVisionOnDeviceAutoMLImageLabelerOptions options =
                    new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel)
                            .setConfidenceThreshold(0.0f)  // Evaluate your model in the Firebase console
                            // to determine an appropriate value.
                            .build();
            labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options);
            labeler.processImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                            String text = "";
                            float confidence;
                            int i = 0;

                            for (FirebaseVisionImageLabel label: labels) {
                                text = label.getText();
                                confidence = label.getConfidence();

                                result[i++] = text + "  " + confidence;
                            }
                            openDialog();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Unable to run model",Toast.LENGTH_SHORT);
                        }
                    });
        } catch (FirebaseMLException e) {
            Toast.makeText(this, "Unable to create labeler",Toast.LENGTH_SHORT);
        }

    }
}

