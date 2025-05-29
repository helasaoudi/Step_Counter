package com.example.app2;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class TargetFragment extends Fragment {

    private EditText distanceInput, timeValue, caloriesValue, steps_value;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "UserInputs";
    private Button btnTranslate;
    private boolean isTranslated = false;
    // Maps for storing original and translated text
    private Map<TextView, String> originalButtonTexts = new HashMap<>();
    private Map<TextView, String> translatedButtonTexts = new HashMap<>();
    private String originalSubtitle;
    private String translatedSubtitle;
    private TextView distance, temp, calories;

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
       // btnTranslate = view.findViewById(R.id.btnTranslate);
        distance = view.findViewById(R.id.distance);
        temp = view.findViewById(R.id.time);
        calories = view.findViewById(R.id.calories);

        // Initialiser les données de traduction
        setupTranslationData();

       // btnTranslate.setOnClickListener(v -> toggleTranslation());

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

    private void setupTranslationData() {
        // Store original button texts
        originalButtonTexts.put(distance, "Mètres");
        originalButtonTexts.put(temp, "Minutes");
        originalButtonTexts.put(calories, "Calories");

        // Store translated button texts
        translatedButtonTexts.put(distance, "أمتار");
        translatedButtonTexts.put(temp, "دقائق");
        translatedButtonTexts.put(calories, "سعرات حرارية");

        // Subtitle texts
        originalSubtitle = "Select a service category:";
        translatedSubtitle = "Sélectionnez une catégorie de service:";
    }

    private void toggleTranslation() {
        isTranslated = !isTranslated;
        // Update button texts based on translation state
        Map<TextView, String> textsToUse = isTranslated ? translatedButtonTexts : originalButtonTexts;
        for (Map.Entry<TextView, String> entry : textsToUse.entrySet()) {
            entry.getKey().setText(entry.getValue());
        }

        // Update translate button text
        btnTranslate.setText(isTranslated ? "Show English" : "en arabe");

        // Show toast message
        Toast.makeText(getContext(), isTranslated ? "Traduit en arabe" : "Retour en anglais", Toast.LENGTH_SHORT).show();
    }
}