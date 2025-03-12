package com.example.app2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class ActivitySplash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Délai avant de passer à l'activité principale
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Passer à l'activité principale
                Intent intent = new Intent(ActivitySplash.this, MainActivity.class);
                startActivity(intent);
                finish(); // Fermer l'activité splash
            }
        }, 3000); // Attendre 3 secondes (3000 ms)
    }
}
