package com.example.auto_learn_app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Objects;

public class StatsActivity extends AppCompatActivity {
   BarChart barChart;
   BarData barData;
   BarDataSet barDataSet;
    ArrayList barEntries = new ArrayList<>();
    private float[] data = new float[6];
    private float[] percentages;
    androidx.appcompat.widget.Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        toolbar = findViewById(R.id.toolbarStats);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Model Statistics");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Load data from main

        data = getIntent().getFloatArrayExtra("data");
        //Manipulate data from main to be percentages
        percentages = calculateData(data);

        barChart = findViewById(R.id.BarChart);
        addBarEntries(data);
        barDataSet = new BarDataSet(barEntries, "");
        barData = new BarData(barDataSet);
        barChart.setData(barData);

        barDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(18f);
    }
    public float[] calculateData(float[] conv){
        float[] result = new float[6];

        for (int i = 0; i < result.length; ++i)
        {
            result[i] = (conv[i] * 100) / 6;
        }

        return result;
    }

    public void addBarEntries(float[] array) {
        barEntries.add(new BarEntry(1f, array[0]));
        barEntries.add(new BarEntry(2f, array[1]));
        barEntries.add(new BarEntry(3f, array[2]));
        barEntries.add(new BarEntry(4f, array[3]));
        barEntries.add(new BarEntry(5f, array[4]));
        barEntries.add(new BarEntry(6f, array[5]));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            Intent homeIntent = new Intent(StatsActivity.this, MainActivity.class);

            startActivity(homeIntent);
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}
