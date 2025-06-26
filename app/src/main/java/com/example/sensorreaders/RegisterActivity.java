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
import com.google.firebase.auth.FirebaseAuthException;
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

        // Click listener para el boton de registro
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

        // Limpiar errores previos
        clearFieldErrors();

        // Validar campos
        if (!validateFields(fullName, email, password)) {
            return;
        }

        // Deshabilitar botón mientras se procesa
        setRegisterButtonState(false, "Creando cuenta...");

        // Crear usuario con Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registro exitoso
                            handleSuccessfulRegistration(fullName);
                        } else {
                            // Error en el registro
                            handleRegistrationError(task.getException());
                        }
                    }
                });
    }

    private void clearFieldErrors() {
        fullNameEditText.setError(null);
        emailEditText.setError(null);
        passwordEditText.setError(null);
    }

    private boolean validateFields(String fullName, String email, String password) {
        boolean isValid = true;

        // Validar nombre completo
        if (TextUtils.isEmpty(fullName)) {
            fullNameEditText.setError("Por favor ingresa tu nombre completo");
            isValid = false;
        } else if (fullName.length() < 2) {
            fullNameEditText.setError("El nombre debe tener al menos 2 caracteres");
            isValid = false;
        }

        // Validar email
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Por favor ingresa tu correo electrónico");
            isValid = false;
        } else if (!isValidEmail(email)) {
            emailEditText.setError("Por favor ingresa un correo electrónico válido");
            isValid = false;
        }

        // Validar contraseña
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Por favor crea una contraseña");
            isValid = false;
        } else if (password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener mínimo 6 caracteres");
            isValid = false;
        } else if (isWeakPassword(password)) {
            passwordEditText.setError("La contraseña es muy simple. Usa letras, números y símbolos");
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isWeakPassword(String password) {
        // Verificar si la contraseña es muy simple
        return password.matches("\\d+") || // Solo numeros
                password.matches("[a-zA-Z]+") || // Solo letras
                password.equals("123456") ||
                password.equals("password") ||
                password.equals("123456789");
    }

    private void setRegisterButtonState(boolean enabled, String text) {
        registerButton.setEnabled(enabled);
        registerButton.setText(text);
    }

    private void handleSuccessfulRegistration(String fullName) {
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
                            setRegisterButtonState(true, "Crear Cuenta");

                            if (task.isSuccessful()) {
                                // Perfil actualizado exitosamente
                                showSuccessMessage("¡Bienvenido " + fullName + "! Tu cuenta ha sido creada exitosamente.");
                            } else {
                                // Error al actualizar perfil, pero la cuenta se creó
                                showSuccessMessage("¡Cuenta creada exitosamente! Bienvenido.");
                            }

                            // Crear sesión local y ir a MainActivity
                            sessionManager.createSession();
                            openMainActivity();
                        }
                    });
        }
    }

    private void handleRegistrationError(Exception exception) {
        // Rehabilitar boton
        setRegisterButtonState(true, "Crear Cuenta");

        String userFriendlyMessage = getUserFriendlyErrorMessage(exception);

        // Si es error de email ya existe, tambien mostrar el error en el campo
        if (exception instanceof FirebaseAuthException) {
            FirebaseAuthException firebaseException = (FirebaseAuthException) exception;
            if ("ERROR_EMAIL_ALREADY_IN_USE".equals(firebaseException.getErrorCode())) {
                emailEditText.setError("Este correo ya está registrado");
            }
        }

        showErrorMessage(userFriendlyMessage);
    }

    private String getUserFriendlyErrorMessage(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            FirebaseAuthException firebaseException = (FirebaseAuthException) exception;
            String errorCode = firebaseException.getErrorCode();

            switch (errorCode) {
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    return "Ya tienes cuenta con este correo. ¿Quieres iniciar sesión en su lugar?";

                case "ERROR_INVALID_EMAIL":
                    return "El correo electrónico no tiene un formato válido. Verifica que esté escrito correctamente.";

                case "ERROR_WEAK_PASSWORD":
                    return "Tu contraseña es muy simple. Intenta con una combinación de letras, números y símbolos.";

                case "ERROR_TOO_MANY_REQUESTS":
                    return "Has intentado crear cuenta demasiadas veces. Espera unos minutos e inténtalo de nuevo.";

                case "ERROR_NETWORK_REQUEST_FAILED":
                    return "No hay conexión a internet. Verifica tu conexión y vuelve a intentarlo.";

                case "ERROR_OPERATION_NOT_ALLOWED":
                    return "El registro está temporalmente deshabilitado. Intenta más tarde.";

                default:
                    return "No pudimos crear tu cuenta en este momento. Verifica tus datos e inténtalo de nuevo.";
            }
        }

        // Para otros tipos de excepciones
        String message = exception.getMessage();
        if (message != null) {
            if (message.contains("network") || message.contains("timeout")) {
                return "Problemas de conexión. Verifica tu internet e inténtalo de nuevo.";
            }
        }

        return "Algo salió mal. Verifica tus datos e inténtalo de nuevo.";
    }

    private void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
        finish();
    }
}