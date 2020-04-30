package com.example.auto_learn_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ProfileIDSignup extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Button next_button;
    EditText utaID;
    Spinner spinner;
    FirebaseUser mUser;
    FirebaseAnalytics mAnalytics;
    FirebaseAuth mauth;
    FirebaseUser firebaseUser;
    FirebaseFirestore db;
    androidx.appcompat.widget.Toolbar toolbar;
    String profession = new String("");
    String mavID = new String("");
    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_id_signup);
        toolbar = findViewById(R.id.toolbarSignup);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Maverick Information");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set Firestore database
        db = FirebaseFirestore.getInstance();

        // Set firebase user
        mauth = FirebaseAuth.getInstance();
        firebaseUser = mauth.getCurrentUser();


        next_button = findViewById(R.id.next_verify_button);
        utaID = findViewById(R.id.editText2);
        spinner = findViewById(R.id.simpleSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.profession_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String IDcheck = utaID.getText().toString();
                if (IDcheck.isEmpty())
                {
                    utaID.setError("Enter your UTA-issued ID");
                    utaID.requestFocus();
                }
                else if  (IDcheck.length() != 10)
                {
                    utaID.setError("Enter the full 10 digit ID");
                    utaID.requestFocus();
                }
                else
                {
                    // Create a new user with a first and last name
                    Map<String, Object> user = new HashMap<>();
                    user.put("UTA_ID", IDcheck);
                    user.put("PROFESSION", profession);

                    db.collection("users").document(Objects.requireNonNull(firebaseUser.getDisplayName()))
                            .set(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    Log.w(TAG, "Error writing document", e);
                                }
                            });
                    Intent intent = new Intent(ProfileIDSignup.this, EmailVerification.class);
                    startActivity(intent);


                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        profession = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        profession = adapterView.getItemAtPosition(0).toString();
    }
}
