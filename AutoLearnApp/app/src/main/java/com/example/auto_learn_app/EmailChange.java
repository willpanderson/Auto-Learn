package com.example.auto_learn_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailChange extends AppCompatActivity {
    EditText editnewe;
    Button changeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_change);

        editnewe = findViewById(R.id.emailReset);
        changeb = findViewById(R.id.nextButton);

        changeb.setOnClickListener(new View.OnClickListener() {
            String newemail = editnewe.getText().toString().trim();


            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                user.updateEmail("user@example.com")
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Intent reverify = new Intent(EmailChange.this, EmailVerification.class);
                                    startActivity(reverify);
                                }
                            }
                        });
            }
        });




    }
}
