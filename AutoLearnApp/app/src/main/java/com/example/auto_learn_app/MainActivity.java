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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;
import com.squareup.picasso.Picasso;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ResultDialog.ResultDialogListener, StatsDialog.StatsDialogListener {
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mToggle;
    androidx.appcompat.widget.Toolbar toolbar;
    ImageView IDProf;
    ImageView mProfile;
    FloatingActionButton mButton;
    Button classifyButton;
    private TextView name,utaID,profession;
    private Bitmap bitmap;
    private Bitmap profileBitmap;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    FirebaseAnalytics mAnalytics;
    private FirebaseVisionImage image;
    private float[] sum = new float[6];
    private String[] result = new String[6];
    private boolean uploaded_for_model = false;
    private boolean uploaded_for_profile = false;
    private boolean has_been_uploaded = false;
    private NavigationView navigationView;
    private View hView;
    private int idx;


    // Defining Permission codes.
    // We can give any value
    // but unique for each permission.
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set Firestore database
        db = FirebaseFirestore.getInstance();
        
        // Set the Firebase Authenticator and user
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mAnalytics = FirebaseAnalytics.getInstance(MainActivity.this);

        // Display the Navigation view
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Create an instance of the navigation header
        // in order to access the contents in the lines below
        hView =  navigationView.getHeaderView(0);

        // Update the name of the user in navigation header
        name = (TextView) hView.findViewById(R.id.profile_name);
        utaID = (TextView) hView.findViewById(R.id.profile_id);
        profession = (TextView) hView.findViewById(R.id.profile_profession);
        // Grab the document with UTA_ID and PRofession
        final DocumentReference docRef = db.collection("users").document(user.getDisplayName());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        if (user != null)
                            displayInfo(document.get("UTA_ID").toString(), document.get("PROFESSION").toString());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        if (user != null)
            name.setText(user.getDisplayName());
        // Declare our image views
        IDProf=(ImageView)findViewById(R.id.IDProf);
        mProfile=(ImageView) hView.findViewById(R.id.profile_picture);

        // Set the current profile picture
        if (user != null) {
            Uri firebaseProfile = user.getPhotoUrl();
            if (firebaseProfile != null) {
                Picasso.get().load(firebaseProfile).transform(new CircleTransform()).into(mProfile);
            }
            else
            {
                Picasso.get().load(R.drawable.profile).transform(new CircleTransform()).into(mProfile);
            }
        }

        // Listener to update profile picture
        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploaded_for_model = false;
                uploaded_for_profile = true;
                selectProfileImage();
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

    private void displayInfo(String ID, String prof) {
        utaID.setText(ID);
        profession.setText(prof);

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
                    startActivityForResult(intent,1);

                    break;
                case STORAGE_PERMISSION_CODE:
                    Toast.makeText(MainActivity.this, "Opening gallery", Toast.LENGTH_SHORT).show();
                    intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    if (uploaded_for_model)
                        startActivityForResult(intent,2);
                    else if (uploaded_for_profile)
                        startActivityForResult(intent, 3);
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

                    startActivityForResult(intent,1);

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
                        startActivityForResult(intent, 3);
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
                    uploaded_for_model = false;
                    uploaded_for_profile = false;
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void selectProfileImage() {
        final CharSequence[] options = { "Choose from Gallery","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Select photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Choose from Gallery"))
                {
                    checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
                }
                else if (options[item].equals("Cancel")) {
                    uploaded_for_model = false;
                    uploaded_for_profile = false;
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
                                        Picasso.get().load(selectedImage).transform(new CircleTransform()).into(mProfile);
                                    }
                                }
                            });
                }

            }
        }
        else if (resultCode == RESULT_CANCELED)
        {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT);
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
                if (sum[0] != 0) {
                    Toast.makeText(this, "Statistics clicked", Toast.LENGTH_SHORT).show();
                    openStats();
                }
                else
                    Toast.makeText(this, "Upload to get started", Toast.LENGTH_SHORT).show();
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

    private void openStats() {
        StatsDialog statsDialog = new StatsDialog();
        statsDialog.show(getSupportFragmentManager(),"stats dialog");

    }

    @Override
    public Bitmap getImage() {
        return bitmap;
    }

    @Override
    public String[] getResults() { return result; }

    @Override
    public int getMax() {
        return idx;
    }


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
                            float largest = 0;

                            for (FirebaseVisionImageLabel label: labels) {
                                text = label.getText();
                                confidence = label.getConfidence();

                                if (text.equalsIgnoreCase("convertible"))
                                {
                                    if (confidence >= largest)
                                    {
                                        largest = confidence;
                                        idx = 0;
                                    }
                                    sum[0] += confidence;
                                    result[0] = text + "  " + confidence;
                                }
                                else if (text.equalsIgnoreCase("coupe"))
                                {
                                    if (confidence >= largest)
                                    {
                                        largest = confidence;
                                        idx = 1;
                                    }
                                    sum[1] += confidence;
                                    result[1] = text + "  " + confidence;
                                }
                                else if (text.equalsIgnoreCase("sedan"))
                                {
                                    if (confidence >= largest)
                                    {
                                        largest = confidence;
                                        idx = 2;
                                    }
                                    sum[2] += confidence;
                                    result[2] = text + "  " + confidence;
                                }
                                else if (text.equalsIgnoreCase("suv"))
                                {
                                    if (confidence >= largest)
                                    {
                                        largest = confidence;
                                        idx = 3;
                                    }
                                    sum[3] += confidence;
                                    result[3] = text + "  " + confidence;
                                }
                                else if (text.equalsIgnoreCase("truck"))
                                {
                                    if (confidence >= largest)
                                    {
                                        largest = confidence;
                                        idx = 4;
                                    }
                                    sum[4] += confidence;
                                    result[4] = text + "  " + confidence;
                                }
                                else if (text.equalsIgnoreCase("van"))
                                {
                                    if (confidence >= largest)
                                    {
                                        largest = confidence;
                                        idx = 5;
                                    }
                                    sum[5] += confidence;
                                    result[5] = text + "  " + confidence;
                                }
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


    @Override
    public float[] getSums() {
        return sum;
    }
}

