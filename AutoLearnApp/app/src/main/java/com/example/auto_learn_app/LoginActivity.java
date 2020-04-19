package com.example.auto_learn_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    Button mButton;
    TextView signUp;
    TextView mfrogot;
    private FirebaseAuth mAuth;
    EditText email, passwod;
    private FirebaseAuth.AuthStateListener mListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.emailEntry);
        passwod = findViewById(R.id.passwordEntry);
        mButton = (Button) findViewById(R.id.loginButton);
        signUp = (TextView) findViewById(R.id.signupClick);
        mAuth = FirebaseAuth.getInstance();
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signupIntent = new Intent(LoginActivity.this, SignupActivity.class);

                startActivity(signupIntent);
                finish();
            }
        });

        mfrogot = (TextView) findViewById(R.id.forgotPsswd);
        mfrogot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent frogotIntent = new Intent(LoginActivity.this,PasswordReset.class);

                startActivity(frogotIntent);
                finish();
            }
        });
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email2 = email.getText().toString();
                String pass = passwod.getText().toString();
                if (email2.isEmpty()){
                    email.setError("Enter a email");
                    email.requestFocus();
                }
                else if (pass.isEmpty()){
                    passwod.setError("Enter a password");
                    passwod.requestFocus();
                }
                else if (! (email2.isEmpty() && pass.isEmpty())){
                    mAuth.signInWithEmailAndPassword(email2,pass).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(LoginActivity.this, "Login Failed. Please check your credentials and try again", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(LoginActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                                Intent p = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(p);
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(LoginActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mListener = new FirebaseAuth.AuthStateListener() {
            FirebaseUser mUser;

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                
            }
        };

    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mListener);
    }
    }

