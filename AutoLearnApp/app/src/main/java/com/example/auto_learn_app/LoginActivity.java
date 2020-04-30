package com.example.auto_learn_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    /*//////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  A C T I V I T Y     L A Y O U T
    //////                  V A R I A B L E S
    ///*/   Button mButton;
    /**/    TextView signUp;
    /**/    TextView mforgot;
    /**/    EditText email, password;



    /*//////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  F I R E   B A S E    V A R I A B L E S
    //////
    ///*/  private FirebaseAuth mAuth;
    /**/   private FirebaseAuth.AuthStateListener mListener;


    /*//////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  C O N S T A N T    P E R M I S S I O N    V A R I A B L E S
    //////
    ///*/   private static final int CAMERA_PERMISSION_CODE = 100;


    /*//////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  B O O L E A N    V A R I A B L E    F O R    P E R M I S S I O N S
    //////
    ///*/   private boolean permissions_granted = false;


    /*//////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  D E B U G    C O N S O L E    O U T P U T
    //////
    ///*/   private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Attach all layout items so that java code
        // may work
        email = findViewById(R.id.emailEntry);
        password = findViewById(R.id.passwordEntry);
        mButton = (Button) findViewById(R.id.loginButton);
        signUp = (TextView) findViewById(R.id.signupClick);
        mforgot = (TextView) findViewById(R.id.forgotPsswd);

        // Set up the firebase authenticator for login requests
        mAuth = FirebaseAuth.getInstance();

        // Navigate to SignupActivity to create a profile
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signupIntent = new Intent(LoginActivity.this, SignupActivity.class);

                startActivity(signupIntent);
                finish();
            }
        });

        // Navigate to PasswordReset if the user forgot password and would like a reset password link
        // sent via email
        mforgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent forgotIntent = new Intent(LoginActivity.this,PasswordReset.class);

                startActivity(forgotIntent);
                finish();
            }
        });

        // Allow user to log in only if the user has allowed all permissions
        // and they have entered a valid email and password
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissions_granted) {
                    String email2 = email.getText().toString();
                    String pass = password.getText().toString();
                    if (email2.isEmpty()) {
                        email.setError("Enter a email");
                        email.requestFocus();
                    } else if (pass.isEmpty()) {
                        password.setError("Enter a password");
                        password.requestFocus();
                    } else {
                        mAuth.signInWithEmailAndPassword(email2, pass).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                                    Intent p = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(p);
                                }
                            }
                        });
                    }
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Allow permissions to continue", Toast.LENGTH_SHORT).show();
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

        // Check for all necessary permissions up front
        checkPermission(Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,CAMERA_PERMISSION_CODE);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////  M E T H O D S    T O    R E Q U E S T    A L L    P E R M I S S I O N S
    //////
    public void checkPermission(String permission1, String permission2, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(LoginActivity.this, permission1)
                == PackageManager.PERMISSION_DENIED
            && ContextCompat.checkSelfPermission(LoginActivity.this, permission2)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(LoginActivity.this, new String[] { permission1, permission2 }, requestCode);
        }else { permissions_granted = true; }
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case CAMERA_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(LoginActivity.this, "Permissions allowed", Toast.LENGTH_SHORT).show();
                    permissions_granted = true;
                }
                else {
                    Toast.makeText(LoginActivity.this,
                            "Permissions Denied",
                            Toast.LENGTH_SHORT)
                            .show();
                    permissions_granted = false;
                }
                break;
            }
        }

    }
    }

