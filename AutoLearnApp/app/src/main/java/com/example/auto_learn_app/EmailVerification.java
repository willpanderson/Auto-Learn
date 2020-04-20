package com.example.auto_learn_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class EmailVerification extends AppCompatActivity {
    androidx.appcompat.widget.Toolbar toolbar;
    Button mVerfiy;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);
        toolbar = findViewById(R.id.toolbarProfile);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Email Verification");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        mVerfiy = findViewById(R.id.resetButton);
        assert user != null;
        mVerfiy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(EmailVerification.this,
                                            "Hi " + user.getDisplayName() + ". A verification email has been sent to " + user.getEmail(),
                                            Toast.LENGTH_SHORT).show();
                                    Intent loginIntent = new Intent(EmailVerification.this, MainActivity.class);
                                    startActivity(loginIntent);
                                }
                                else{
                                    Toast.makeText(EmailVerification.this,
                                            "Sorry " + user.getDisplayName() + "but the system failed to send verification email.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            Intent loginIntent = new Intent(EmailVerification.this, SignupActivity.class);

            startActivity(loginIntent);
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}
