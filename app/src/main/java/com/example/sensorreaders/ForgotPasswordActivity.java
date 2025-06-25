package com.example.sensorreaders;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private TextInputLayout emailInputLayout;
    private Button resetPasswordButton;
    private TextView backToLoginText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Inicializar views
        initViews();

        // Configurar listeners
        setupListeners();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        backToLoginText = findViewById(R.id.backToLoginText);
    }

    private void setupListeners() {
        // Click listener para el botón de restablecer contraseña
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        // Click listener para volver al login
        backToLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Cierra esta actividad y vuelve al login
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
    }

    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();

        // Limpiar errores anteriores
        emailInputLayout.setError(null);

        // Validar email
        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError("Por favor ingresa tu correo electrónico");
            emailEditText.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Por favor ingresa un correo electrónico válido");
            emailEditText.requestFocus();
            return;
        }

        // Deshabilitar botón y mostrar progreso
        setLoadingState(true);

        // Enviar email de restablecimiento
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        setLoadingState(false);

                        if (task.isSuccessful()) {
                            // Email enviado exitosamente
                            showSuccessMessage(email);

                            // Opcional: cerrar la actividad después de un delay
                            emailEditText.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                }
                            }, 2000); // Esperar 2 segundos antes de cerrar

                        } else {
                            // Error al enviar el email
                            handleResetPasswordError(task.getException());
                        }
                    }
                });
    }

    private void setLoadingState(boolean loading) {
        resetPasswordButton.setEnabled(!loading);
        if (loading) {
            resetPasswordButton.setText("Enviando...");
        } else {
            resetPasswordButton.setText("Enviar enlace de restablecimiento");
        }
    }

    private void showSuccessMessage(String email) {
        Toast.makeText(this,
                "✅ Se ha enviado un enlace de restablecimiento a: " + email +
                        "\n\nRevisa tu bandeja de entrada y spam.",
                Toast.LENGTH_LONG).show();
    }

    private void handleResetPasswordError(Exception exception) {
        String errorMessage = getResetPasswordErrorMessage(exception);

        // Mostrar error específico en el campo de email si es relevante
        if (exception instanceof FirebaseAuthInvalidUserException) {
            emailInputLayout.setError("No existe una cuenta con este correo electrónico");
        } else {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    private String getResetPasswordErrorMessage(Exception exception) {
        if (exception == null) {
            return "❌ No se pudo enviar el enlace de restablecimiento. Inténtalo de nuevo.";
        }

        String errorMessage = exception.getMessage();
        if (errorMessage != null) {
            if (errorMessage.contains("INVALID_EMAIL") || errorMessage.contains("invalid-email")) {
                return "❌ El formato del correo electrónico no es válido.";
            } else if (errorMessage.contains("USER_NOT_FOUND") || errorMessage.contains("user-not-found")) {
                return "❌ No existe una cuenta registrada con este correo electrónico.";
            } else if (errorMessage.contains("TOO_MANY_REQUESTS") || errorMessage.contains("too-many-requests")) {
                return "⏰ Demasiados intentos. Espera unos minutos e inténtalo de nuevo.";
            } else if (errorMessage.contains("NETWORK_ERROR") || errorMessage.contains("network-error")) {
                return "🌐 Sin conexión a internet. Verifica tu conexión.";
            }
        }

        return "❌ No se pudo enviar el enlace. Verifica tu correo e inténtalo de nuevo.";
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}