package com.example.sensorreaders;

import android.content.Intent;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private TextView loginText;
    private TextInputEditText fullNameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase Auth y SessionManager
        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);

        // Inicializar views
        initViews();

        // Configurar listeners
        setupListeners();
    }

    private void initViews() {
        loginText = findViewById(R.id.loginText);
        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
    }

    private void setupListeners() {
        // Click listener para el enlace de login
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
            }
        });

        // Click listener para el botón de registro
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validar campos
        if (TextUtils.isEmpty(fullName)) {
            fullNameEditText.setError("Por favor ingresa tu nombre completo");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Por favor ingresa tu correo electrónico");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Por favor crea una contraseña");
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener mínimo 6 caracteres");
            return;
        }

        // Deshabilitar botón mientras se procesa
        registerButton.setEnabled(false);
        registerButton.setText("Creando cuenta...");

        // Crear usuario con Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registro exitoso
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Actualizar perfil con el nombre
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(fullName)
                                        .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                // Rehabilitar botón
                                                registerButton.setEnabled(true);
                                                registerButton.setText("Crear Cuenta");

                                                if (task.isSuccessful()) {
                                                    // Perfil actualizado exitosamente
                                                    Toast.makeText(RegisterActivity.this,
                                                            "¡Cuenta creada exitosamente! Bienvenido " + fullName,
                                                            Toast.LENGTH_LONG).show();

                                                    // Crear sesión local y ir a MainActivity
                                                    sessionManager.createSession();
                                                    openMainActivity();
                                                } else {
                                                    // Error al actualizar perfil, pero la cuenta se creó
                                                    Toast.makeText(RegisterActivity.this,
                                                            "Cuenta creada, pero hubo un problema al guardar el nombre",
                                                            Toast.LENGTH_LONG).show();

                                                    // Crear sesión local y ir a MainActivity de todas formas
                                                    sessionManager.createSession();
                                                    openMainActivity();
                                                }
                                            }
                                        });
                            }
                        } else {
                            // Error en el registro
                            registerButton.setEnabled(true);
                            registerButton.setText("Crear Cuenta");

                            String errorMessage = getRegisterErrorMessage(task.getException());
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void openLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }

    private void openMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Cerrar RegisterActivity para que no pueda volver con el botón atrás
    }

    private String getRegisterErrorMessage(Exception exception) {
        if (exception == null) {
            return "No se pudo crear la cuenta. Inténtalo de nuevo.";
        }

        String errorCode = exception.getMessage();
        if (errorCode != null) {
            if (errorCode.contains("INVALID_EMAIL")) {
                return "El formato del correo electrónico no es válido.";
            } else if (errorCode.contains("EMAIL_ALREADY_IN_USE")) {
                return "Ya existe una cuenta con este correo electrónico.";
            } else if (errorCode.contains("WEAK_PASSWORD")) {
                return "La contraseña es muy débil. Usa al menos 6 caracteres.";
            } else if (errorCode.contains("TOO_MANY_REQUESTS")) {
                return "Demasiados intentos. Espera unos minutos e inténtalo de nuevo.";
            } else if (errorCode.contains("NETWORK_ERROR")) {
                return "Sin conexión a internet. Verifica tu conexión.";
            }
        }
        return "No se pudo crear la cuenta. Verifica tus datos e inténtalo de nuevo.";
    }
}