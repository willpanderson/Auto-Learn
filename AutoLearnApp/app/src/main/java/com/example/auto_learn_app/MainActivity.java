package com.example.auto_learn_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import com.google.firebase.ml.custom.FirebaseCustomLocalModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;
import com.squareup.picasso.Picasso;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ResultDialog.ResultDialogListener, StatsDialog.StatsDialogListener {

    /*//////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  F I R E   B A S E    V A R I A B L E S
    //////
    ///*/  private FirebaseAuth mAuth;
    /**/   private FirebaseUser user;
    /**/   private FirebaseFirestore db;
    /**/   private FirebaseAnalytics mAnalytics;
    /**/   private FirebaseVisionImage image;
    /**/   private Bitmap bitmap;


    /*//////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  C L A S S I F I C A T I O N    R E S U L T S
    //////              D A T A    V A R I A B L E S
    ///*/   private float[] sum = new float[6];
    /**/    private String[] result = new String[6];
    /**/    private int idx;


    /*//////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  B O O L E A N    V A R I A B L E S
    //////
    ///*/   private boolean uploaded_for_model = false;   // Image should be used with runModel()
    /**/    private boolean uploaded_for_profile = false; // Image shall be stored into firebase
    /**/    private boolean has_been_uploaded = false;    // If true allow the user to view results and statistics


    /*//////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  A C T I V I T Y     L A Y O U T
    //////                  V A R I A B L E S
    ///*/   FloatingActionButton mButton;     // Select image button
    /**/    Button classifyButton;            // View Results button
    /**/
    /**/    // Navigation view variables
    /**/    private NavigationView navigationView;
    /**/    private DrawerLayout mDrawerLayout;
    /**/    private ActionBarDrawerToggle mToggle;
    /**/
    /**/    // Activity toolbar with Title and buttons
    /**/    private androidx.appcompat.widget.Toolbar toolbar;
    /**/
    /**/    private ImageView IDProf;       // Image view for model image
    /**/    private ImageView mProfile;     // Image view for profile picture
    /**/
    /**/    private TextView name,utaID,profession;  // Text views for profile information
    /**/
    /**/    private View hView;  // View to contain the Navigation menu header with profile information




    /*//////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  C O N S T A N T    D I A L O G    V A R I A B L E S
    //////
    ///*/   private static final int RESULTS_DIALOG = 50;
    /**/    private static final int STATS_DIALOG = 51;
    /**/    private static final int INFO_DIALOG = 52;


    /*//////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  C O N S T A N T    P E R M I S S I O N    V A R I A B L E S
    //////
    ///*/   private static final int CAMERA_PERMISSION_CODE = 100;
    /**/    private static final int STORAGE_PERMISSION_CODE = 101;


    /*//////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  D E B U G    C O N S O L E    O U T P U T
    //////
    ///*/   private String TAG;


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

        // Attach the TextViews, ImageViews and Buttons
        // from the layout file to work with the java code
        name = (TextView) hView.findViewById(R.id.profile_name);
        utaID = (TextView) hView.findViewById(R.id.profile_id);
        profession = (TextView) hView.findViewById(R.id.profile_profession);
        IDProf=(ImageView)findViewById(R.id.IDProf);
        mProfile=(ImageView) hView.findViewById(R.id.profile_picture);
        classifyButton = findViewById(R.id.classificationButton);

        // Grab the document with UTA_ID and Profession from Firebase Firestore
        final DocumentReference docRef = db.collection("users").document(user.getDisplayName());    // Filename is the users name
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        if (user != null)
                            displayInfo(document.get("UTA_ID").toString(), document.get("PROFESSION").toString(),user.getDisplayName());  // Call a helper method to set the profile information
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        // Set the current profile picture from logged in user
        if (user != null) {
            Uri firebaseProfile = user.getPhotoUrl();
            if (firebaseProfile != null) {
                // Display the circle image of the users profile, if it cannot find it display the standard profile picture
                Picasso.get().load(firebaseProfile).error(R.drawable.profile).transform(new CircleTransform()).into(mProfile);
            }
            else
            {
                // If user has no picture display default picture
                Picasso.get().load(R.drawable.profile).transform(new CircleTransform()).into(mProfile);
            }
        }

        // Listener to update profile picture
        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploaded_for_model = false;
                uploaded_for_profile = true;
                selectProfileImage();   // Allow user to choose from Gallery app
            }
        });

        // Listener to run model
        mButton = findViewById(R.id.GalleryButton);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploaded_for_model = true;
                uploaded_for_profile = false;
                selectImage();        // Allow user to take pic or choose from Gallery app
            }
        });


        // Listener to display results dialog after an intial upload for model usage
        classifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uploaded_for_model || has_been_uploaded )
                    openDialog(RESULTS_DIALOG);
                else
                {
                    // Tell the user to upload a photo to get started with image classification
                    Toast.makeText(MainActivity.this, "Please upload a photo", Toast.LENGTH_SHORT).show();
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  T O O L    B A R    M E N U
    //////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar,menu);   // Set the menu on the toolbar to contain
        return super.onCreateOptionsMenu(menu);                 // the statistics button
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // Open the Navigation view if the menu button is selected
        if (mToggle.onOptionsItemSelected(item))
            return true;

        // Handle menu item for Statistics
        switch (item.getItemId())
        {
            case R.id.stats:

                // Check to see if the user has uploaded a photo
                if (has_been_uploaded) {
                    Toast.makeText(this, "Statistics clicked", Toast.LENGTH_SHORT).show();
                    openDialog(STATS_DIALOG);   // If it has then open the statistics dialog
                }
                else  // Display a warning to tell user to upload an image
                    Toast.makeText(this, "Upload to get started", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  N A V I G A T I O N    V I E W    M E N U   I T E M S
    //////
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            // Handle the menu options
            case R.id.nav_settings:
                Toast.makeText(this, "Account Settings", Toast.LENGTH_SHORT).show();

                // Switch to the profile settings activity
                Intent profileIntent = new Intent(MainActivity.this,ProfileSettings.class);
                startActivity(profileIntent);
                finish();
                break;
            case R.id.nav_about:
                Toast.makeText(this, "Model Information", Toast.LENGTH_SHORT).show();

                // Display the Model Information Dialog
                openDialog(INFO_DIALOG);
                break;
            case R.id.nav_logout:
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

                // Call sign out to log the user out with Firebase Authenticator
                signOut();

                // Return to the Login Activity so that user may log in again
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
                break;
        }

        return true;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  R U N   M O D E L   O N   S E L E C T E D   I M A G E
    /////
    public void runModel() {

        // Build the model which is located in app > src > main > assets
        // as model.tflite  (Model trained with Google Firebase ML Kit, Firebase Vision)
        FirebaseCustomLocalModel localModel = new FirebaseCustomLocalModel.Builder()
                .setAssetFilePath("manifest.json")
                .build();

        // Generate a labeler to provide the results of the classification
        FirebaseVisionImageLabeler labeler;
        try {

            // Set the labeler to use the model for classification
            final FirebaseModelInputOutputOptions options =
                    new FirebaseModelInputOutputOptions.Builder()
                            .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 224, 224, 3})
                            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 5})
                            .build();

            bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

            int batchNum = 0;
            float[][][][] input = new float[1][224][224][3];
            for (int x = 0; x < 224; x++) {
                for (int y = 0; y < 224; y++) {
                    int pixel = bitmap.getPixel(x, y);
                    // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                    // model. For example, some models might require values to be normalized
                    // to the range [0.0, 1.0] instead.
                    input[batchNum][x][y][0] = (Color.red(pixel) - 127) / 128.0f;
                    input[batchNum][x][y][1] = (Color.green(pixel) - 127) / 128.0f;
                    input[batchNum][x][y][2] = (Color.blue(pixel) - 127) / 128.0f;
                }
            }
            // Grab an instance of the labeling object using our machine learning model
            // then run the image
            FirebaseModelInputs inputs = new FirebaseModelInputs.Builder()
                    .add(input)  // add() as many input arrays as your model requires
                    .build();
            labeler.processImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionImageLabel> labels) {

                            // If it successfully processed the image store off the data
                            // for use in the dialogs using helper method
                            storeData(labels);

                            // Open the Results dialog to display results
                            // of the current image classification
                            openDialog(RESULTS_DIALOG);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // If it fails present a toast
                            Toast.makeText(MainActivity.this, "Unable to run model",Toast.LENGTH_SHORT);
                        }
                    });
        } catch (FirebaseMLException e) {
            // Fatal error could not find model
            Toast.makeText(this, "Unable to create labeler",Toast.LENGTH_SHORT);
        }

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  O P E N    D I A L O G    M E T H O D 
    //////
    private void openDialog(int choice) {
        switch (choice)
        {
            case RESULTS_DIALOG:
                ResultDialog resultDialog = new ResultDialog();
                resultDialog.show(getSupportFragmentManager(),"result dialog");
                break;
            case INFO_DIALOG:
                InformationDialog informationDialog = new InformationDialog();
                informationDialog.show(getSupportFragmentManager(),"info dialog");
                break;
            case STATS_DIALOG:
                StatsDialog statsDialog = new StatsDialog();
                statsDialog.show(getSupportFragmentManager(),"stats dialog");
                break;
        }
    }




    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  R E S U L T   D I A L O G    I N T E R F A C E
    //////
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  S T A T S   D I A L O G    I N T E R F A C E
    //////
    @Override
    public float[] getSums() {
        return sum;
    }





    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  C A M E R A     A N D     G A L L E R Y    P E R M I S S I O N S
    //////
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



    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  H E L P E R    M E T H O D S
    //////
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


    private void displayInfo(String ID, String prof, String userName) {
        utaID.setText(ID);
        profession.setText(prof);
        name.setText(userName);
    }


    private void signOut() {
        FirebaseAuth.getInstance().signOut();
    }



    private void storeData(List<FirebaseVisionImageLabel> labels) {
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
    }
}

