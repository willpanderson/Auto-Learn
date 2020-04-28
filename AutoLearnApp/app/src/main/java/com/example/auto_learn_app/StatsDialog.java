package com.example.auto_learn_app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class StatsDialog extends AppCompatDialogFragment {

    BarChart barChart;
    BarData barData;
    BarDataSet barDataSet;
    ArrayList barEntries = new ArrayList<>();
    private float[] data = new float[6];
    private float[] percentages;
    private StatsDialogListener listener;

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_stats, null);

        builder.setView(view).setTitle("Model Statistics for current session").setPositiveButton("OK", null);

        data = listener.getSums();
        percentages = getPercentages();
        barChart = view.findViewById(R.id.BarChart);
        addBarEntries(percentages);
        barDataSet = new BarDataSet(barEntries, "Vehicles");

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("Convertible");
        labels.add("Convertible");
        labels.add("Coupe");
        labels.add("Sedan");
        labels.add("SUV");
        labels.add("Truck");
        labels.add("Van");
        barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setTextSize(8);
        barChart.getXAxis().setGranularityEnabled(true);

        barDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(14f);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (StatsDialog.StatsDialogListener) context;
        } catch ( ClassCastException e ){
            throw new ClassCastException(context.toString() + "must implement the dialog");
        }

    }
    public float getTotal() {
        float result = 0;

        for (int i  = 0; i < 6; ++i)
            result += data[i];

        return result;
    }
    public float[] getPercentages() {
        float[] result = new float[6];

        for (int i  = 0; i < 6; ++i)
            result[i] = (data[i] * 100) / getTotal();

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

    public interface StatsDialogListener {
        float[] getSums();
    }
}
