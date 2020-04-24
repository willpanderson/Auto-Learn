package com.example.auto_learn_app;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class ResultDialog extends AppCompatDialogFragment {
    private String[] results;
    private ResultDialogListener listener;
    private ImageView imageView;
    private Bitmap image;
    private TextView guess1;
    private TextView guess2;
    private TextView guess3;
    private TextView guess4;
    private TextView guess5;
    private TextView guess6;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.display_results, null);

        builder.setView(view).setTitle("Image Results").setPositiveButton("OK", null);

        results = listener.getResults();
        imageView = view.findViewById(R.id.results_image);
        image = listener.getImage();
        imageView.setImageBitmap(image);

        guess1 = view.findViewById(R.id.guess1);
        guess2 = view.findViewById(R.id.guess2);
        guess3 = view.findViewById(R.id.guess3);
        guess4 = view.findViewById(R.id.guess4);
        guess5 = view.findViewById(R.id.guess5);
        guess6 = view.findViewById(R.id.guess6);

        guess1.setText(results[0]);
        guess2.setText(results[1]);
        guess3.setText(results[2]);
        guess4.setText(results[3]);
        guess5.setText(results[4]);
        guess6.setText(results[5]);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (ResultDialog.ResultDialogListener) context;
        } catch ( ClassCastException e ){
            throw new ClassCastException(context.toString() + "must implement the dialog");
        }

    }

    public interface ResultDialogListener {
        Bitmap getImage();
        String[] getResults();
    }
}
