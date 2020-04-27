package com.example.auto_learn_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ProfileIDSignup extends AppCompatActivity {
    Button next_button;
    EditText utaID;
    FirebaseUser mUser;
    FirebaseAnalytics mAnalytics;
    FirebaseAuth mauth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_id_signup);
    mauth = FirebaseAuth.getInstance();
    mAnalytics = FirebaseAnalytics.getInstance(ProfileIDSignup.this);
    user = mauth.getCurrentUser();
    next_button = findViewById(R.id.nextButton);
    utaID = findViewById(R.id.editText2);
    
    next_button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String IDcheck = utaID.getText().toString();
            if (IDcheck.isEmpty())
            {
                utaID.setError("Enter your UTA-issued ID");
                utaID.requestFocus();
            }
            else
            {
                if (user != null)
                {
                    mAnalytics.setUserId(IDcheck);
                    Intent intent = new Intent(ProfileIDSignup.this, EmailVerification.class);
                    startActivity(intent);

                }
                else
                {
                    Toast.makeText(ProfileIDSignup.this, "unable to save id", Toast.LENGTH_SHORT);
                }

            }
        }
    });
    }
}
