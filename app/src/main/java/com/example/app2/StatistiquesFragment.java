package com.example.app2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class StatistiquesFragment extends Fragment {
    private BarChart barChart;
    private LineChart lineChart;

    private static final String PREF_NAME = "daily_data";
    private static final String KEY_STEPS = "steps";
    private static final String KEY_MINUTES = "minutes";
    private static final String KEY_DISTANCE = "distance";
    private static final String KEY_CALORIES = "calories";

    public StatistiquesFragment() {
        super(R.layout.fragment_statistiques);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistiques, container, false);

        barChart = view.findViewById(R.id.barChart);
        lineChart = view.findViewById(R.id.lineChart);

        loadStatistics();
        return view;
    }

    private void loadStatistics() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        int steps = sharedPreferences.getInt(KEY_STEPS, 0);
        int minutes = sharedPreferences.getInt(KEY_MINUTES, 0);
        float distance = Float.parseFloat(sharedPreferences.getString(KEY_DISTANCE, "0").replace(",", "."));
        float calories = Float.parseFloat(sharedPreferences.getString(KEY_CALORIES, "0").replace(",", "."));

        setupBarChart(steps, minutes);
        setupLineChart(distance, calories);
    }

    private void setupBarChart(int steps, int minutes) {
        List<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0, steps));
        barEntries.add(new BarEntry(1, minutes));

        BarDataSet dataSet = new BarDataSet(barEntries, "Steps & Minutes");
        dataSet.setColor(getResources().getColor(R.color.teal_200));

        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.invalidate();
    }

    private void setupLineChart(float distance, float calories) {
        List<Entry> lineEntries = new ArrayList<>();
        lineEntries.add(new Entry(0, distance));
        lineEntries.add(new Entry(1, calories));

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Distance & Calories");
        lineDataSet.setColor(getResources().getColor(R.color.light_blue_900));
        lineDataSet.setCircleColor(getResources().getColor(R.color.purple_700));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(4f);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }
}
