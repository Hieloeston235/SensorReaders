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

public class LoginActivity extends AppCompatActivity {

    private TextView registerText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
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

    @Override
    protected void onStart() {
        super.onStart();
        // Verificar si el usuario ya está logueado y la sesión está activa
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && sessionManager.isSessionValid()) {
            // Usuario ya está logueado y sesión activa, ir al MainActivity
            sessionManager.updateLastActivity();
            openMainActivity();
        }
    }

    private void initViews() {
        registerText = findViewById(R.id.registerText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
    }

    private void setupListeners() {
        // Click listener para el enlace de registro
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRegisterActivity();
            }
        });

        // Click listener para el botón de login
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validar campos
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Por favor ingresa tu correo electrónico");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Por favor ingresa tu contraseña");
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener mínimo 6 caracteres");
            return;
        }

        // Deshabilitar botón mientras se procesa
        loginButton.setEnabled(false);
        loginButton.setText("Iniciando sesión...");

        // Iniciar sesión con Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Rehabilitar botón
                        loginButton.setEnabled(true);
                        loginButton.setText("Iniciar Sesión");

                        if (task.isSuccessful()) {
                            // Login exitoso - crear sesión
                            sessionManager.createSession();
                            Toast.makeText(LoginActivity.this, "¡Bienvenido de vuelta!", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            openMainActivity();
                        } else {
                            // Error en el login - mensajes amigables para el usuario
                            String errorMessage = getLoginErrorMessage(task.getException());
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void openRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void openMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Cerrar LoginActivity para que no pueda volver con el botón atrás
    }

    private String getLoginErrorMessage(Exception exception) {
        if (exception == null) {
            return "No se pudo iniciar sesión. Inténtalo de nuevo.";
        }

        String errorCode = exception.getMessage();
        if (errorCode != null) {
            if (errorCode.contains("INVALID_EMAIL")) {
                return "El formato del correo electrónico no es válido.";
            } else if (errorCode.contains("WRONG_PASSWORD")) {
                return "La contraseña es incorrecta.";
            } else if (errorCode.contains("USER_NOT_FOUND")) {
                return "No existe una cuenta con este correo electrónico.";
            } else if (errorCode.contains("USER_DISABLED")) {
                return "Esta cuenta ha sido deshabilitada.";
            } else if (errorCode.contains("TOO_MANY_REQUESTS")) {
                return "Demasiados intentos fallidos. Espera unos minutos e inténtalo de nuevo.";
            } else if (errorCode.contains("NETWORK_ERROR")) {
                return "Sin conexión a internet. Verifica tu conexión.";
            }
        }
        return "No se pudo iniciar sesión. Verifica tus datos e inténtalo de nuevo.";
    }
}