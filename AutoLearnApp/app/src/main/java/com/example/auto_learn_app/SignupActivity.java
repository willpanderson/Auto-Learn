package com.example.auto_learn_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class SignupActivity extends AppCompatActivity {
    Button btnSignUp;
    EditText email, passwod,passwod2;
    FirebaseAuth mfirebaseAuth;
    androidx.appcompat.widget.Toolbar toolbar;

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
        email = findViewById(R.id.emailSignup);
        passwod = findViewById(R.id.passwordSignup);
        passwod2 = findViewById(R.id.confirmSignup);
        btnSignUp = findViewById(R.id.registerButton);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email2 = email.getText().toString();
                String pass = passwod.getText().toString();
                String pass2 = passwod2.getText().toString();
                if (email2.isEmpty()){
                    email.setError("Enter a email");
                    email.requestFocus();
                }
                else if (pass.isEmpty()){
                    passwod.setError("Enter a password");
                    passwod.requestFocus();
                }
                else if (pass2.isEmpty()){
                    passwod2.setError("Enter a password");
                    passwod2.requestFocus();
                }
                else if (email2.isEmpty() && pass.isEmpty() && pass2.isEmpty()){
                    Toast.makeText(SignupActivity.this, "Please enter account info!", Toast.LENGTH_SHORT).show();

                }
                else if (!(email2.isEmpty() && pass.isEmpty() && pass2.isEmpty())){
                    mfirebaseAuth.createUserWithEmailAndPassword(email2,pass).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(SignupActivity.this, "Sign up failed", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Intent s = new Intent(SignupActivity.this, MainActivity.class);
                                startActivity(s);
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(SignupActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
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
}
