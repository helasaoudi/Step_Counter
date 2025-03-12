package com.example.app2;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class TargetFragment extends Fragment {

    private EditText distanceInput, timeValue, caloriesValue, steps_value;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "UserInputs";

    public TargetFragment() {
        // Constructeur vide requis
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflater le layout du fragment
        View view = inflater.inflate(R.layout.fragment_target, container, false);

        // Initialiser les éléments
        distanceInput = view.findViewById(R.id.distance_input);
        timeValue = view.findViewById(R.id.time_value);
        caloriesValue = view.findViewById(R.id.calories_value);
        steps_value = view.findViewById(R.id.steps_value);

        // Charger les préférences
        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, 0);
        loadSavedValues();

        // Ajouter des écouteurs pour sauvegarder les valeurs
        setFocusChangeListener(distanceInput);
        setFocusChangeListener(timeValue);
        setFocusChangeListener(caloriesValue);
        setFocusChangeListener(steps_value);

        return view;
    }

    private void setFocusChangeListener(EditText editText) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) saveValues();
        });
    }

    private void saveValues() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("distance", distanceInput.getText().toString());
        editor.putString("time", timeValue.getText().toString());
        editor.putString("calories", caloriesValue.getText().toString());
        editor.putString("steps", steps_value.getText().toString());
        editor.apply();
    }

    private void loadSavedValues() {
        distanceInput.setText(sharedPreferences.getString("distance", ""));
        timeValue.setText(sharedPreferences.getString("time", ""));
        caloriesValue.setText(sharedPreferences.getString("calories", ""));
        steps_value.setText(sharedPreferences.getString("steps", ""));
    }
}
