package com.example.app2;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;

import java.util.ArrayList;
import java.util.Calendar;

public class RunFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView stepsValue, distanceValue, minutesValue, caloriesValue;
    private BarChart stepsBarChart;

    private int stepCount = 0;
    private long lastTime = 0;
    private boolean isFirstStep = true;

    private static final double STEP_LENGTH_METERS = 0.7;
    private static final double CALORIES_PER_STEP = 0.04;

    // Variables pour stocker les données quotidiennes
    private int dailySteps = 0;
    private int dailyMinutes = 0;
    private double dailyDistance = 0;
    private double dailyCalories = 0;
    private static final String PREF_NAME = "daily_data";
    private static final String KEY_STEPS = "steps";
    private static final String KEY_MINUTES = "minutes";
    private static final String KEY_DISTANCE = "distance";
    private static final String KEY_CALORIES = "calories";
    private static final String KEY_DATE = "date";

    public RunFragment() {
        super(R.layout.fragment_run);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_run, container, false);

        stepsValue = view.findViewById(R.id.steps_value);
        distanceValue = view.findViewById(R.id.distance_value);
        minutesValue = view.findViewById(R.id.minutes_value);
        caloriesValue = view.findViewById(R.id.calories_value);
        stepsBarChart = view.findViewById(R.id.stepsBarChart);

        loadDailyData();
        updateChart();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double acceleration = Math.sqrt(x * x + y * y + z * z);
            long currentTime = System.currentTimeMillis();

            if (acceleration > 12) {
                if (isFirstStep || (currentTime - lastTime > 500)) {
                    stepCount++;
                    updateUI();
                    saveDailyData();
                    lastTime = currentTime;
                    isFirstStep = false;
                }
            }
        }
    }

    private void updateUI() {
        stepsValue.setText(String.valueOf(stepCount));
        dailySteps++;
        dailyDistance = dailySteps * STEP_LENGTH_METERS;
        dailyCalories = dailySteps * CALORIES_PER_STEP;
        dailyMinutes = dailySteps / 100;

        distanceValue.setText(String.format("%.2f m", dailyDistance));
        caloriesValue.setText(String.format("%.1f cal", dailyCalories));
        minutesValue.setText(String.format("%d min", dailyMinutes));
    }

    private void saveDailyData() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_YEAR);

        // Si la date a changé, réinitialiser les données
        if (today != sharedPreferences.getInt(KEY_DATE, 0)) {
            editor.putInt(KEY_DATE, today);
            editor.putInt(KEY_STEPS, 0);
            editor.putInt(KEY_MINUTES, 0);
            editor.putString(KEY_DISTANCE, "0");
            editor.putString(KEY_CALORIES, "0");
        }

        // Sauvegarder les données du jour
        editor.putInt(KEY_STEPS, dailySteps);
        editor.putInt(KEY_MINUTES, dailyMinutes);
        editor.putString(KEY_DISTANCE, String.format("%.2f", dailyDistance));
        editor.putString(KEY_CALORIES, String.format("%.1f", dailyCalories));
        editor.apply();
    }

    private void loadDailyData() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_YEAR);
        int savedDate = sharedPreferences.getInt(KEY_DATE, -1);

        if (today != savedDate) {
            // Nouveau jour, réinitialiser les valeurs
            dailySteps = 0;
            dailyMinutes = 0;
            dailyDistance = 0;
            dailyCalories = 0;

            // Sauvegarder la nouvelle date
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(KEY_DATE, today);
            editor.putInt(KEY_STEPS, 0);
            editor.putInt(KEY_MINUTES, 0);
            editor.putString(KEY_DISTANCE, "0");
            editor.putString(KEY_CALORIES, "0");
            editor.apply();
        } else {
            // Charger les anciennes valeurs si la date est la même
            dailySteps = sharedPreferences.getInt(KEY_STEPS, 0);
            dailyMinutes = sharedPreferences.getInt(KEY_MINUTES, 0);
            dailyDistance = Double.parseDouble(sharedPreferences.getString(KEY_DISTANCE, "0").replace(",", "."));
            dailyCalories = Double.parseDouble(sharedPreferences.getString(KEY_CALORIES, "0").replace(",", "."));
        }

        // 🔥 Ajoute cette ligne pour synchroniser `stepCount`
        stepCount = dailySteps;

        updateUI();
    }




    private void updateChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, dailySteps));
        entries.add(new BarEntry(1, dailyMinutes));

        BarDataSet dataSet = new BarDataSet(entries, "Steps & Minutes");
        BarData data = new BarData(dataSet);
        stepsBarChart.setData(data);
        stepsBarChart.invalidate(); // Mettre à jour le graphique
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
