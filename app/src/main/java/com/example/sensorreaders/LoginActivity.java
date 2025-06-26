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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextView registerText;
    private TextView forgotPasswordText;
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
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
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

        // Click listener para el enlace de olvidé contraseña
        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openForgotPasswordActivity();
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

        // Limpiar errores previos
        clearFieldErrors();

        // Validar campos
        if (!validateFields(email, password)) {
            return;
        }

        // Deshabilitar botón mientras se procesa
        setLoadingState(true);

        // Iniciar sesión con Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        setLoadingState(false);

                        if (task.isSuccessful()) {
                            // Login exitoso - crear sesión
                            sessionManager.createSession();
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Verificar si el email está verificado
                            if (user != null && user.isEmailVerified()) {
                                showSuccessMessage("¡Bienvenido de vuelta!");
                                openMainActivity();
                            } else if (user != null) {
                                // Email no verificado pero permitir acceso
                                showSuccessMessage("¡Bienvenido de vuelta!");
                                openMainActivity();
                            }
                        } else {
                            // Error en el login
                            handleLoginError(task.getException());
                        }
                    }
                });
    }

    private void clearFieldErrors() {
        emailEditText.setError(null);
        passwordEditText.setError(null);
    }

    private boolean validateFields(String email, String password) {
        boolean isValid = true;

        // Validar email
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Por favor ingresa tu correo electrónico");
            emailEditText.requestFocus();
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Por favor ingresa un correo electrónico válido");
            emailEditText.requestFocus();
            isValid = false;
        }

        // Validar contraseña
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Por favor ingresa tu contraseña");
            if (isValid) passwordEditText.requestFocus();
            isValid = false;
        } else if (password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener mínimo 6 caracteres");
            if (isValid) passwordEditText.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private void setLoadingState(boolean loading) {
        loginButton.setEnabled(!loading);
        if (loading) {
            loginButton.setText("Iniciando sesión...");
        } else {
            loginButton.setText("Iniciar Sesión");
        }
    }

    private void handleLoginError(Exception exception) {
        String userFriendlyMessage = getUserFriendlyErrorMessage(exception);

        // Si es error de credenciales, también mostrar el error en los campos correspondientes
        if (exception instanceof FirebaseAuthException) {
            FirebaseAuthException firebaseException = (FirebaseAuthException) exception;
            String errorCode = firebaseException.getErrorCode();

            if ("ERROR_USER_NOT_FOUND".equals(errorCode)) {
                emailEditText.setError("No existe una cuenta con este correo");
            } else if ("ERROR_WRONG_PASSWORD".equals(errorCode)) {
                passwordEditText.setError("Contraseña incorrecta");
            } else if ("ERROR_INVALID_CREDENTIAL".equals(errorCode)) {
                // Para este error genérico, no asumimos qué campo está mal
                // El usuario verá el mensaje general en el Toast
            }
        }

        showErrorMessage(userFriendlyMessage);
    }

    private String getUserFriendlyErrorMessage(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            FirebaseAuthException firebaseException = (FirebaseAuthException) exception;
            String errorCode = firebaseException.getErrorCode();

            switch (errorCode) {
                case "ERROR_USER_NOT_FOUND":
                    return "No encontramos una cuenta con este correo electrónico. ¿Quieres crear una cuenta nueva?";

                case "ERROR_WRONG_PASSWORD":
                    return "La contraseña es incorrecta. ¿Olvidaste tu contraseña? Puedes restablecerla fácilmente.";

                case "ERROR_INVALID_CREDENTIAL":
                    return "El correo electrónico o la contraseña son incorrectos. Verifica ambos datos e inténtalo de nuevo.";

                case "ERROR_INVALID_EMAIL":
                    return "El correo electrónico no tiene un formato válido. Verifica que esté escrito correctamente.";

                case "ERROR_USER_DISABLED":
                    return "Tu cuenta ha sido deshabilitada temporalmente. Contacta con soporte para más información.";

                case "ERROR_TOO_MANY_REQUESTS":
                    return "Has intentado iniciar sesión demasiadas veces. Por seguridad, espera unos minutos antes de intentar de nuevo.";

                case "ERROR_NETWORK_REQUEST_FAILED":
                    return "No hay conexión a internet. Verifica tu conexión WiFi o datos móviles e inténtalo de nuevo.";

                case "ERROR_OPERATION_NOT_ALLOWED":
                    return "El inicio de sesión está temporalmente deshabilitado. Intenta más tarde.";

                case "ERROR_INVALID_CUSTOM_TOKEN":
                    return "Hubo un problema con la autenticación. Intenta cerrar y abrir la app de nuevo.";

                case "ERROR_CUSTOM_TOKEN_MISMATCH":
                    return "Error de autenticación. Por favor, intenta iniciar sesión de nuevo.";

                default:
                    return "No pudimos iniciar tu sesión en este momento. Verifica que tu correo y contraseña sean correctos.";
            }
        }

        // Para otros tipos de excepciones
        String message = exception != null ? exception.getMessage() : "";
        if (message != null) {
            if (message.contains("network") || message.contains("timeout")) {
                return "Problemas de conexión a internet. Verifica tu conexión e inténtalo de nuevo.";
            } else if (message.contains("INVALID_EMAIL") || message.contains("invalid-email")) {
                return "El formato del correo electrónico no es válido. Revisa que esté bien escrito.";
            } else if (message.contains("WRONG_PASSWORD") || message.contains("wrong-password")) {
                return "La contraseña es incorrecta. ¿Olvidaste tu contraseña? Puedes restablecerla fácilmente.";
            } else if (message.contains("INVALID_CREDENTIAL") || message.contains("invalid-credential")) {
                return "El correo electrónico o la contraseña son incorrectos. Verifica ambos datos e inténtalo de nuevo.";
            } else if (message.contains("USER_NOT_FOUND") || message.contains("user-not-found")) {
                return "No encontramos una cuenta con este correo. ¿Quieres crear una cuenta nueva?";
            }
        }

        return "Algo salió mal al iniciar sesión. Verifica tus datos e inténtalo de nuevo.";
    }

    private void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void openRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void openForgotPasswordActivity() {
        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void openMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Cerrar LoginActivity para que no pueda volver con el botón atrás
    }
}