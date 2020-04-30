package com.example.auto_learn_app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class ChangePasswordDialog extends DialogFragment {
    EditText editText;
    FirebaseAuth auth;
    FirebaseUser user;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_password_change, null);
        editText = view.findViewById(R.id.emailReset3);

        // Set firebase variables
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        builder.setView(view).setTitle("Send reset password email").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                String emailAddress = editText.getText().toString();
                if (emailAddress.isEmpty()){
                    editText.setError("Enter a email");
                    editText.requestFocus();
                }
                else
                {
                    if (emailAddress.equals(user.getEmail())) {
                        auth.sendPasswordResetEmail(emailAddress)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        dialogInterface.cancel();
                                    }
                                });
                    }
                    else
                    {
                        editText.setError("Email does not match");
                        editText.requestFocus();
                    }
                }
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        return builder.create();
    }
}
