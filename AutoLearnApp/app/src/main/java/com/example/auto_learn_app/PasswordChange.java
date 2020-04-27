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
import com.google.firebase.auth.FirebaseUser;

public class PasswordChange extends AppCompatActivity {
    private String newPassword, confirmPass;
    EditText newer,conf;
    Button nextb;
    androidx.appcompat.widget.Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);

        toolbar = findViewById(R.id.toolbar_password_change);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Change password");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newer = findViewById(R.id.editText);
        conf = findViewById(R.id.editText3);
        newPassword = newer.getText().toString().trim();
        confirmPass = conf.getText().toString().trim();

        nextb = findViewById(R.id.change_password_button);

        nextb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newPassword.equals(confirmPass)) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    assert user != null;
                    user.updatePassword(newPassword)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent intentback = new Intent(PasswordChange.this, MainActivity.class);
                                        startActivity(intentback);
                                    }
                                }
                            });
                }    }
        });




    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            Intent homeIntent = new Intent(PasswordChange.this, ProfileSettings.class);

            startActivity(homeIntent);
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}
