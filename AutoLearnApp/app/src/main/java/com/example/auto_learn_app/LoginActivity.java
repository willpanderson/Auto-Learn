package com.example.auto_learn_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    Button mButton;
    TextView signUp;
    TextView mforgot;
    private FirebaseAuth mAuth;
    EditText email, password;
    private FirebaseAuth.AuthStateListener mListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.emailEntry);
        password = findViewById(R.id.passwordEntry);
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

        mforgot = (TextView) findViewById(R.id.forgotPsswd);
        mforgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent forgotIntent = new Intent(LoginActivity.this,PasswordReset.class);

                startActivity(forgotIntent);
                finish();
            }
        });
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email2 = email.getText().toString();
                String pass = password.getText().toString();
                if (email2.isEmpty()){
                    email.setError("Enter a email");
                    email.requestFocus();
                }
                else if (pass.isEmpty()){
                    password.setError("Enter a password");
                    password.requestFocus();
                }
                else if (email2.isEmpty() && pass.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Please enter your credentials", Toast.LENGTH_SHORT).show();
                    email.requestFocus();
                    password.requestFocus();
                }
                else if (! (email2.isEmpty() && pass.isEmpty())){
                    mAuth.signInWithEmailAndPassword(email2,pass).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
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
                if (mUser == null){
                    Toast.makeText(LoginActivity.this, "The previous user has been logged out",Toast.LENGTH_SHORT).show();
                }
            }
        };

    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mListener);
    }
    }

