package com.example.auto_learn_app;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.squareup.picasso.Picasso;

public class InformationDialog extends DialogFragment {

    TextView HyperLink,HyperLink2;
    ImageView imageView;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.display_info, null);
        imageView = view.findViewById(R.id.imageView2);
        Picasso.get().load(R.drawable.asq).into(imageView);
        HyperLink = (TextView) view.findViewById(R.id.textView13);
        HyperLink.setClickable(true);
        HyperLink.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "<a href='http://mmlab.ie.cuhk.edu.hk/datasets/comp_cars/index.html'>CompCars Dataset</a>";
        HyperLink.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));

        HyperLink2 = (TextView) view.findViewById(R.id.textView14);
        HyperLink2.setClickable(true);
        HyperLink2.setMovementMethod(LinkMovementMethod.getInstance());
        String text2 = "<a href='https://ai.stanford.edu/~jkrause/cars/car_dataset.html'>Stanford AI Dataset</a>";
        HyperLink2.setText(Html.fromHtml(text2, Html.FROM_HTML_MODE_COMPACT));

        builder.setView(view).setPositiveButton("OK", null);

        return builder.create();
    }
}
