package com.example.auto_learn_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordReset extends AppCompatActivity {
    androidx.appcompat.widget.Toolbar toolbar;
    EditText resetEmail;
    FirebaseAuth auth;

    Button initiatereset;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwordreset);
        toolbar = findViewById(R.id.toolbar_password_reset);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Reset Password");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        auth = FirebaseAuth.getInstance();
        resetEmail = findViewById(R.id.emailReset);
        initiatereset = findViewById(R.id.verify_new_email_button);
        initiatereset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddress = resetEmail.getText().toString();
                if (emailAddress.isEmpty()){
                    resetEmail.setError("Enter a email");
                    resetEmail.requestFocus();
                }
                else
                {
                    auth.sendPasswordResetEmail(emailAddress)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent recallIntent = new Intent(PasswordReset.this, LoginActivity.class);
                                        startActivity(recallIntent);
                                    }
                                }
                            });
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            Intent loginIntent = new Intent(PasswordReset.this, LoginActivity.class);

            startActivity(loginIntent);
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}


