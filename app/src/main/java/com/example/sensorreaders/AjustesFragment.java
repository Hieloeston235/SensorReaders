package com.example.sensorreaders;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class AjustesFragment extends Fragment {

    private TextView tvNombreUsuario, tvEmailUsuario;
    private Switch switchNotificaciones, switchActualizacion;
    private LinearLayout layoutCambiarPassword, layoutEditarPerfil, layoutCerrarSesion, layoutAcercaDe;
    private FirebaseAuth mAuth;

    public AjustesFragment() {
        // Required empty public constructor
    }

    public static AjustesFragment newInstance(String param1, String param2) {
        AjustesFragment fragment = new AjustesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ajustes, container, false);

        initViews(view);
        setupUserInfo();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        // Información del usuario
        tvNombreUsuario = view.findViewById(R.id.tvNombreUsuario);
        tvEmailUsuario = view.findViewById(R.id.tvEmailUsuario);

        // Switches de configuración
        switchNotificaciones = view.findViewById(R.id.switchNotificaciones);
        switchActualizacion = view.findViewById(R.id.switchActualizacion);

        // Opciones de cuenta
        layoutCambiarPassword = view.findViewById(R.id.layoutCambiarPassword);
        layoutEditarPerfil = view.findViewById(R.id.layoutEditarPerfil);
        layoutCerrarSesion = view.findViewById(R.id.layoutCerrarSesion);

        // Información de la app
        layoutAcercaDe = view.findViewById(R.id.layoutAcercaDe);
    }

    private void setupUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Mostrar información del usuario
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                tvNombreUsuario.setText(currentUser.getDisplayName());
            } else {
                tvNombreUsuario.setText("Usuario");
            }

            if (currentUser.getEmail() != null) {
                tvEmailUsuario.setText(currentUser.getEmail());
            } else {
                tvEmailUsuario.setText("usuario@ejemplo.com");
            }
        }
    }

    private void setupListeners() {
        // Listener para cambiar contraseña
        layoutCambiarPassword.setOnClickListener(v -> {
            showChangePasswordDialog();
        });

        // Listener para editar perfil
        layoutEditarPerfil.setOnClickListener(v -> {
            showEditProfileDialog();
        });

        // Listener para cerrar sesión
        layoutCerrarSesion.setOnClickListener(v -> {
            showLogoutDialog();
        });

        // Listener para acerca de
        layoutAcercaDe.setOnClickListener(v -> {
            showAboutDialog();
        });

        // Listeners para los switches (funcionalidad básica)
        switchNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String message = isChecked ? "Notificaciones activadas" : "Notificaciones desactivadas";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });

        switchActualizacion.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String message = isChecked ? "Actualización automática activada" : "Actualización automática desactivada";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });

    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);

        TextInputLayout nameLayout = dialogView.findViewById(R.id.nameLayout);
        TextInputEditText nameEdit = dialogView.findViewById(R.id.nameEdit);

        // Cargar el nombre actual del usuario
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getDisplayName() != null) {
            nameEdit.setText(currentUser.getDisplayName());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("✏️ Editar Perfil");
        builder.setView(dialogView);

        builder.setPositiveButton("Guardar", null);
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        // Configurar el boton positivo después de crear el diálogo
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String newName = nameEdit.getText().toString().trim();

                // Limpiar errores anteriores
                nameLayout.setError(null);

                // Validaciones
                if (TextUtils.isEmpty(newName)) {
                    nameLayout.setError("El nombre no puede estar vacío");
                    return;
                }

                if (newName.length() < 4) {
                    nameLayout.setError("El nombre debe tener al menos 4 caracteres");
                    return;
                }

                if (newName.length() > 50) {
                    nameLayout.setError("El nombre no puede exceder 50 caracteres");
                    return;
                }

                // Validar que solo contenga letras, numeros, espacios y algunos caracteres especiales
                if (!newName.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s._-]+$")) {
                    nameLayout.setError("El nombre contiene caracteres no válidos");
                    return;
                }

                // Si todas las validaciones pasan, actualizar el perfil
                updateUserProfile(newName, dialog);
            });
        });

        dialog.show();
    }

    private void updateUserProfile(String newName, AlertDialog dialog) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar el botón mientras se procesa
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Guardando...");

        // Crear el perfil actualizado
        com.google.firebase.auth.UserProfileChangeRequest profileUpdates =
                new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build();

        // Actualizar el perfil
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Restaurar el botón
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Guardar");

                        if (task.isSuccessful()) {
                            // Perfil actualizado exitosamente
                            Toast.makeText(getContext(),
                                    " Perfil actualizado exitosamente",
                                    Toast.LENGTH_LONG).show();

                            // Actualizar la UI con el nuevo nombre
                            tvNombreUsuario.setText(newName);

                            dialog.dismiss();
                        } else {
                            // Error al actualizar perfil
                            handleUpdateProfileError(task.getException());
                        }
                    }
                });
    }

    private void handleUpdateProfileError(Exception exception) {
        String errorMessage = " No se pudo actualizar el perfil";

        if (exception != null) {

            String exceptionMessage = exception.getMessage();
            if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
                errorMessage = " Error: " + exceptionMessage;
            }
        }

        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
    }

    private void showChangePasswordDialog() {

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, null);

        TextInputLayout currentPasswordLayout = dialogView.findViewById(R.id.currentPasswordLayout);
        TextInputLayout newPasswordLayout = dialogView.findViewById(R.id.newPasswordLayout);
        TextInputLayout confirmPasswordLayout = dialogView.findViewById(R.id.confirmPasswordLayout);

        TextInputEditText currentPasswordEdit = dialogView.findViewById(R.id.currentPasswordEdit);
        TextInputEditText newPasswordEdit = dialogView.findViewById(R.id.newPasswordEdit);
        TextInputEditText confirmPasswordEdit = dialogView.findViewById(R.id.confirmPasswordEdit);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("🔐 Cambiar Contraseña");
        builder.setView(dialogView);

        builder.setPositiveButton("Cambiar", null);
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        // Configurar el botón positivo después de crear el diálogo para evitar que se cierre automáticamente
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String currentPassword = currentPasswordEdit.getText().toString().trim();
                String newPassword = newPasswordEdit.getText().toString().trim();
                String confirmPassword = confirmPasswordEdit.getText().toString().trim();

                // Limpiar errores anteriores
                currentPasswordLayout.setError(null);
                newPasswordLayout.setError(null);
                confirmPasswordLayout.setError(null);

                // Validaciones
                if (TextUtils.isEmpty(currentPassword)) {
                    currentPasswordLayout.setError("Ingresa tu contraseña actual");
                    return;
                }

                if (TextUtils.isEmpty(newPassword)) {
                    newPasswordLayout.setError("Ingresa la nueva contraseña");
                    return;
                }

                if (newPassword.length() < 6) {
                    newPasswordLayout.setError("La contraseña debe tener al menos 6 caracteres");
                    return;
                }

                if (TextUtils.isEmpty(confirmPassword)) {
                    confirmPasswordLayout.setError("Confirma la nueva contraseña");
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    confirmPasswordLayout.setError("Las contraseñas no coinciden");
                    return;
                }

                if (currentPassword.equals(newPassword)) {
                    newPasswordLayout.setError("La nueva contraseña debe ser diferente a la actual");
                    return;
                }

                // Si todas las validaciones pasan, cambiar la contraseña
                changePassword(currentPassword, newPassword, dialog);
            });
        });

        dialog.show();
    }

    private void changePassword(String currentPassword, String newPassword, AlertDialog dialog) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(getContext(), " Error: Usuario no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar el botón mientras se procesa
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Cambiando...");

        // Crear credenciales para re-autenticar
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        // Re-autenticar al usuario
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> reauthTask) {
                        if (reauthTask.isSuccessful()) {
                            // Re-autenticación exitosa, ahora cambiar la contraseña
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> updateTask) {
                                            // Restaurar el botón
                                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Cambiar");

                                            if (updateTask.isSuccessful()) {
                                                // Contraseña cambiada exitosamente
                                                Toast.makeText(getContext(),
                                                        " Contraseña cambiada exitosamente",
                                                        Toast.LENGTH_LONG).show();
                                                dialog.dismiss();
                                            } else {
                                                // Error al cambiar contraseña
                                                handleChangePasswordError(updateTask.getException());
                                            }
                                        }
                                    });
                        } else {
                            // Error en re-autenticación (contraseña actual incorrecta)
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Cambiar");

                            handleReauthenticationError(reauthTask.getException());
                        }
                    }
                });
    }

    private void handleReauthenticationError(Exception exception) {
        String errorMessage = " Contraseña actual incorrecta";

        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = " La contraseña actual es incorrecta";
        }

        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
    }

    private void handleChangePasswordError(Exception exception) {
        String errorMessage = " No se pudo cambiar la contraseña";

        if (exception instanceof FirebaseAuthWeakPasswordException) {
            errorMessage = " La nueva contraseña es muy débil";
        } else if (exception instanceof FirebaseAuthRecentLoginRequiredException) {
            errorMessage = " Necesitas iniciar sesión nuevamente para cambiar la contraseña";
        }

        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Cerrar Sesión");
        builder.setMessage("¿Estás seguro de que quieres cerrar sesión?");

        builder.setPositiveButton("Sí, cerrar sesión", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout();
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void logout() {
        // Llamar al método de logout del MainActivity
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            Toast.makeText(getContext(), "Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show();
            mainActivity.logoutUser();
        }
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Acerca de Weather Station");

        builder.setMessage("Weather Station v1.0.0\n\n" +
                "Aplicación de estación meteorológica creada en el Ciclo 1 del año 2025, como parte de la materia de " +
                "Programación de Dispositivos Móviles (PDM) impartido por el Ingeniero Javier Paíz.\n\n" +
                "Esta app permite el monitoreo en tiempo real de sensores meteorológicos, ofreciendo también un historial " +
                "completo de los datos registrados. Está diseñada para brindar a los usuarios información precisa sobre las " +
                "condiciones ambientales actuales y pasadas.\n\n" +
                "Datos que se monitorean:\n" +
                "- Fecha y Hora\n" +
                "- Temperatura\n" +
                "- Humedad relativa del aire\n" +
                "- Presión atmosférica\n" +
                "- Humedad del suelo\n" +
                "- Intensidad de luz\n" +
                "- Velocidad del viento\n" +
                "- Si hay lluvia\n" +
                "- Detección de humo\n" +
                "- Calidad del aire (gas)\n\n" +
                "Desarrollado con ❤️ por:\n" +
                "• Meybell Jacqueline Ramírez (RQ22004)\n" +
                "• Arturo Elías Torres Esperanza (TE22003)\n" +
                "• Gabriel Enrique De la O Aguirre (DO22023)");

        builder.setPositiveButton("Cerrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }
}