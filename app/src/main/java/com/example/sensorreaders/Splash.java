package com.example.sensorreaders;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Splash extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Timer to navigate to LoginActivity after splash duration
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start LoginActivity
                Intent intent = new Intent(Splash.this, LoginActivity.class);
                startActivity(intent);

                // Add transition animation
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                // Close splash activity
                finish();
            }
        }, SPLASH_DURATION);
    }

    @Override
    public void onBackPressed() {
        // Disable back button during splash
        // Do nothing
    }
}