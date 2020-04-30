package com.example.auto_learn_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailChange extends AppCompatActivity {
    EditText editnewe;
    Button changeb;
    androidx.appcompat.widget.Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_change);

        toolbar = findViewById(R.id.toolbar_email_change);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Change email");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editnewe = findViewById(R.id.emailReset);
        changeb = findViewById(R.id.verify_new_email_button);

        changeb.setOnClickListener(new View.OnClickListener() {
            String newemail = editnewe.getText().toString().trim();


            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String newemail = editnewe.getText().toString().trim();
                assert user != null;
                user.updateEmail(newemail)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Intent reverify = new Intent(EmailChange.this, EmailVerification.class);
                                    startActivity(reverify);
                                }
                                else if(!task.isSuccessful())
                                {
                                    Toast.makeText(EmailChange.this,"An error has occurred while changing your email.",Toast.LENGTH_SHORT).show();
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
            Intent homeIntent = new Intent(EmailChange.this, ProfileSettings.class);

            startActivity(homeIntent);
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}
