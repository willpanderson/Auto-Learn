package com.example.auto_learn_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class InformationActivity extends AppCompatActivity {

    androidx.appcompat.widget.Toolbar toolbar;
    TextView HyperLink,HyperLink2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        toolbar = findViewById(R.id.toolbarInformation);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Model Information");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        HyperLink = (TextView) findViewById(R.id.textView13);
        HyperLink.setClickable(true);
        HyperLink.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "<a href='http://mmlab.ie.cuhk.edu.hk/datasets/comp_cars/index.html'>CompCars Dataset</a>";
        HyperLink.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));

        HyperLink2 = (TextView) findViewById(R.id.textView14);
        HyperLink2.setClickable(true);
        HyperLink2.setMovementMethod(LinkMovementMethod.getInstance());
        String text2 = "<a href='https://ai.stanford.edu/~jkrause/cars/car_dataset.html'>Stanford AI Dataset</a>";
        HyperLink2.setText(Html.fromHtml(text2, Html.FROM_HTML_MODE_COMPACT));
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            Intent homeIntent = new Intent(InformationActivity.this, MainActivity.class);

            startActivity(homeIntent);
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}
