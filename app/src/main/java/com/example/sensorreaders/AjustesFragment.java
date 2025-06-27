package com.example.sensorreaders;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
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
import android.content.Context;
import android.content.SharedPreferences;

public class AjustesFragment extends Fragment {

    private TextView tvNombreUsuario, tvEmailUsuario;
    private Switch switchNotificaciones, switchActualizacion;
    private LinearLayout layoutCambiarPassword, layoutEditarPerfil, layoutCerrarSesion, layoutAcercaDe, layoutNotificaciones;
    private FirebaseAuth mAuth;
    private boolean isNotificationDialogOpen = false;
    private boolean notificationsEnabled = false;

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
        SharedPreferences prefs = getContext().getSharedPreferences("notification_settings", Context.MODE_PRIVATE);
        notificationsEnabled = prefs.getBoolean("notifications_enabled", false);
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
        // InformaciOn del usuario
        tvNombreUsuario = view.findViewById(R.id.tvNombreUsuario);
        tvEmailUsuario = view.findViewById(R.id.tvEmailUsuario);

        // Switches de configuracion
        switchNotificaciones = view.findViewById(R.id.switchNotificaciones);
        switchActualizacion = view.findViewById(R.id.switchActualizacion);

        // Cargar estado guardado de las notificaciones
        SharedPreferences prefs = getContext().getSharedPreferences("notification_settings", Context.MODE_PRIVATE);
        notificationsEnabled = prefs.getBoolean("notifications_enabled", false);
        switchNotificaciones.setChecked(notificationsEnabled);

        // Establecer switch de actualizacion en OFF por defecto
        switchActualizacion.setChecked(false);

        layoutNotificaciones = view.findViewById(R.id.layoutNotificaciones);

        // Opciones de cuenta
        layoutCambiarPassword = view.findViewById(R.id.layoutCambiarPassword);
        layoutEditarPerfil = view.findViewById(R.id.layoutEditarPerfil);
        layoutCerrarSesion = view.findViewById(R.id.layoutCerrarSesion);

        // Información de la app
        layoutAcercaDe = view.findViewById(R.id.layoutAcercaDe);
    }

    // Metodo para guardar el estado del switch de notificaciones
    private void saveNotificationState(boolean enabled) {
        notificationsEnabled = enabled;
        SharedPreferences prefs = getContext().getSharedPreferences("notification_settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("notifications_enabled", enabled);
        editor.apply();
        switchNotificaciones.setChecked(enabled); // Sincronizar el estado del switch
    }

    private void setupUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Mostrar informacion del usuario
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

        // Listener para cerrar sesion
        layoutCerrarSesion.setOnClickListener(v -> {
            showLogoutDialog();
        });

        // Listener para acerca de
        layoutAcercaDe.setOnClickListener(v -> {
            showAboutDialog();
        });

        // Listener para el layout de notificaciones
        layoutNotificaciones.setOnClickListener(v -> {
            if (switchNotificaciones.isChecked()) {
                // Si el switch esta activo, mostrar el dislogo directamente
                showNotificationSettingsDialog();
            }
        });

        // Logica para el switch de notificaciones
        switchNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isNotificationDialogOpen) {
                // Si el dialogo esta abierto, ignorar cambios del switch
                return;
            }

            if (isChecked) {
                // Si se activa el switch abrir el dialogo de configuracion
                notificationsEnabled = true;
                showNotificationSettingsDialog();
            } else {
                // Si se desactiva el switch solo cambiar el estado
                notificationsEnabled = false;
                saveNotificationState(false);
                Toast.makeText(getContext(), "Notificaciones desactivadas", Toast.LENGTH_SHORT).show();
            }
        });

        switchActualizacion.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String message = isChecked ? "Actualizacion automática activada" : "Actualizacion automatica desactivada";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    private void showNotificationSettingsDialog() {
        isNotificationDialogOpen = true; // Marcar que el dialogo esta abierto
        saveNotificationState(true); // Guardar que las notificaciones están activas

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_notification_settings, null);

        // Referencias a todos los elementos del dialog
        ImageButton btnCerrarAlertas = dialogView.findViewById(R.id.btnCerrarAlertas);

        // SeekBars de Temperatura
        SeekBar seekBarTempMin = dialogView.findViewById(R.id.seekBarTempMin);
        SeekBar seekBarTempMax = dialogView.findViewById(R.id.seekBarTempMax);
        TextView tvTempMinValue = dialogView.findViewById(R.id.tvTempMinValue);
        TextView tvTempMaxValue = dialogView.findViewById(R.id.tvTempMaxValue);

        // SeekBars de Humedad
        SeekBar seekBarHumedadMin = dialogView.findViewById(R.id.seekBarHumedadMin);
        SeekBar seekBarHumedadMax = dialogView.findViewById(R.id.seekBarHumedadMax);
        TextView tvHumedadMinValue = dialogView.findViewById(R.id.tvHumedadMinValue);
        TextView tvHumedadMaxValue = dialogView.findViewById(R.id.tvHumedadMaxValue);

        // SeekBars de Presion
        SeekBar seekBarPresionMin = dialogView.findViewById(R.id.seekBarPresionMin);
        SeekBar seekBarPresionMax = dialogView.findViewById(R.id.seekBarPresionMax);
        TextView tvPresionMinValue = dialogView.findViewById(R.id.tvPresionMinValue);
        TextView tvPresionMaxValue = dialogView.findViewById(R.id.tvPresionMaxValue);

        // SeekBar de Viento
        SeekBar seekBarVientoMax = dialogView.findViewById(R.id.seekBarVientoMax);
        TextView tvVientoMaxValue = dialogView.findViewById(R.id.tvVientoMaxValue);

        // SeekBars de Luz
        SeekBar seekBarLuzMin = dialogView.findViewById(R.id.seekBarLuzMin);
        SeekBar seekBarLuzMax = dialogView.findViewById(R.id.seekBarLuzMax);
        TextView tvLuzMinValue = dialogView.findViewById(R.id.tvLuzMinValue);
        TextView tvLuzMaxValue = dialogView.findViewById(R.id.tvLuzMaxValue);

        // CheckBox de Lluvia
        CheckBox checkBoxLluvia = dialogView.findViewById(R.id.checkBoxLluvia);

        // SeekBar de Gas
        SeekBar seekBarGasMax = dialogView.findViewById(R.id.seekBarGasMax);
        TextView tvGasMaxValue = dialogView.findViewById(R.id.tvGasMaxValue);

        // SeekBar de Humo
        SeekBar seekBarHumoMax = dialogView.findViewById(R.id.seekBarHumoMax);
        TextView tvHumoMaxValue = dialogView.findViewById(R.id.tvHumoMaxValue);

        // SeekBars de Humedad del Suelo
        SeekBar seekBarHumedadSueloMin = dialogView.findViewById(R.id.seekBarHumedadSueloMin);
        SeekBar seekBarHumedadSueloMax = dialogView.findViewById(R.id.seekBarHumedadSueloMax);
        TextView tvHumedadSueloMinValue = dialogView.findViewById(R.id.tvHumedadSueloMinValue);
        TextView tvHumedadSueloMaxValue = dialogView.findViewById(R.id.tvHumedadSueloMaxValue);

        // Botones
        Button btnGuardarAlertas = dialogView.findViewById(R.id.btnGuardarAlertas);
        Button btnRestablecerAlertas = dialogView.findViewById(R.id.btnRestablecerAlertas);

        // Cargar configuración actual
        loadCurrentNotificationSettings(seekBarTempMin, seekBarTempMax, seekBarHumedadMin, seekBarHumedadMax,
                seekBarPresionMin, seekBarPresionMax, seekBarVientoMax, seekBarLuzMin,
                seekBarLuzMax, checkBoxLluvia, seekBarGasMax, seekBarHumoMax,
                seekBarHumedadSueloMin, seekBarHumedadSueloMax);

        // Configurar listeners para todos los SeekBars
        setupSeekBarListeners(seekBarTempMin, tvTempMinValue, "°C", 0);
        setupSeekBarListeners(seekBarTempMax, tvTempMaxValue, "°C", 0);
        setupSeekBarListeners(seekBarHumedadMin, tvHumedadMinValue, "%", 0);
        setupSeekBarListeners(seekBarHumedadMax, tvHumedadMaxValue, "%", 0);
        setupSeekBarListeners(seekBarPresionMin, tvPresionMinValue, " hPa", 500);
        setupSeekBarListeners(seekBarPresionMax, tvPresionMaxValue, " hPa", 500);
        setupSeekBarListeners(seekBarVientoMax, tvVientoMaxValue, " km/h", 0);
        setupSeekBarListeners(seekBarLuzMin, tvLuzMinValue, " lux", 0);
        setupSeekBarListeners(seekBarLuzMax, tvLuzMaxValue, " lux", 0);
        setupSeekBarListeners(seekBarGasMax, tvGasMaxValue, " ppm", 0);
        setupSeekBarListeners(seekBarHumoMax, tvHumoMaxValue, " ppm", 0);
        setupSeekBarListeners(seekBarHumedadSueloMin, tvHumedadSueloMinValue, "%", 0);
        setupSeekBarListeners(seekBarHumedadSueloMax, tvHumedadSueloMaxValue, "%", 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        builder.setCancelable(false); // No se puede cerrar tocando fuera

        AlertDialog dialog = builder.create();

        // Listener para el boton cerrar (X) - MANTENER SWITCH ACTIVO
        btnCerrarAlertas.setOnClickListener(v -> {
            isNotificationDialogOpen = false; // Marcar que el dialogo se cerro
            // NO cambiar el estado del switch aqui
            dialog.dismiss();
        });

        // Listener para el botón Guardar
        btnGuardarAlertas.setOnClickListener(v -> {
            // Guardar todas las configuraciones
            saveNotificationSettings(
                    seekBarTempMin.getProgress(),
                    seekBarTempMax.getProgress(),
                    seekBarHumedadMin.getProgress(),
                    seekBarHumedadMax.getProgress(),
                    seekBarPresionMin.getProgress() + 500,
                    seekBarPresionMax.getProgress() + 500,
                    seekBarVientoMax.getProgress(),
                    seekBarLuzMin.getProgress(),
                    seekBarLuzMax.getProgress(),
                    checkBoxLluvia.isChecked(),
                    seekBarGasMax.getProgress(),
                    seekBarHumoMax.getProgress(),
                    seekBarHumedadSueloMin.getProgress(),
                    seekBarHumedadSueloMax.getProgress()
            );

            isNotificationDialogOpen = false; // Marcar que el dialogo se cerro
            Toast.makeText(getContext(), "Configuración de alertas guardada", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        // Listener para el boton Restablecer
        btnRestablecerAlertas.setOnClickListener(v -> {
            // Restablecer valores por defecto
            resetToDefaultValues(seekBarTempMin, seekBarTempMax, seekBarHumedadMin, seekBarHumedadMax,
                    seekBarPresionMin, seekBarPresionMax, seekBarVientoMax, seekBarLuzMin,
                    seekBarLuzMax, checkBoxLluvia, seekBarGasMax, seekBarHumoMax,
                    seekBarHumedadSueloMin, seekBarHumedadSueloMax);

            Toast.makeText(getContext(), "Valores restablecidos por defecto", Toast.LENGTH_SHORT).show();
        });

        // Manejar botón de atras del sistema - MANTENER SWITCH ACTIVO
        dialog.setOnCancelListener(dialogInterface -> {
            isNotificationDialogOpen = false; // Marcar que el dialogo se cerro
            // NO cambiar el estado del switch aqui
        });

        // Manejar cuando se cierre el dialogo por cualquier motivo
        dialog.setOnDismissListener(dialogInterface -> {
            isNotificationDialogOpen = false; // Marcar que el dialog se cerro
        });

        dialog.show();
    }

    // Metodo para cargar la configuracion actual
    private void loadCurrentNotificationSettings(SeekBar tempMin, SeekBar tempMax, SeekBar humedadMin, SeekBar humedadMax,
                                                 SeekBar presionMin, SeekBar presionMax, SeekBar vientoMax, SeekBar luzMin,
                                                 SeekBar luzMax, CheckBox lluvia, SeekBar gasMax, SeekBar humoMax,
                                                 SeekBar humedadSueloMin, SeekBar humedadSueloMax) {

        SharedPreferences prefs = getContext().getSharedPreferences("notification_settings", Context.MODE_PRIVATE);

        tempMin.setProgress(prefs.getInt("temp_min", 10));
        tempMax.setProgress(prefs.getInt("temp_max", 35));
        humedadMin.setProgress(prefs.getInt("humedad_min", 30));
        humedadMax.setProgress(prefs.getInt("humedad_max", 80));
        presionMin.setProgress(prefs.getInt("presion_min", 480)); // 980 hPa
        presionMax.setProgress(prefs.getInt("presion_max", 530)); // 1030 hPa
        vientoMax.setProgress(prefs.getInt("viento_max", 25));
        luzMin.setProgress(prefs.getInt("luz_min", 100));
        luzMax.setProgress(prefs.getInt("luz_max", 800));
        lluvia.setChecked(prefs.getBoolean("lluvia_enabled", true));
        gasMax.setProgress(prefs.getInt("gas_max", 500));
        humoMax.setProgress(prefs.getInt("humo_max", 300));
        humedadSueloMin.setProgress(prefs.getInt("humedad_suelo_min", 20));
        humedadSueloMax.setProgress(prefs.getInt("humedad_suelo_max", 85));
    }

    // Metodo mejorado para guardar configuraciones usando SharedPreferences
    private void saveNotificationSettings(int tempMin, int tempMax, int humedadMin, int humedadMax,
                                          int presionMin, int presionMax, int vientoMax, int luzMin,
                                          int luzMax, boolean lluviaEnabled, int gasMax, int humoMax,
                                          int humedadSueloMin, int humedadSueloMax) {

        SharedPreferences prefs = getContext().getSharedPreferences("notification_settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("temp_min", tempMin);
        editor.putInt("temp_max", tempMax);
        editor.putInt("humedad_min", humedadMin);
        editor.putInt("humedad_max", humedadMax);
        editor.putInt("presion_min", presionMin - 500); // Guardar sin offset
        editor.putInt("presion_max", presionMax - 500); // Guardar sin offset
        editor.putInt("viento_max", vientoMax);
        editor.putInt("luz_min", luzMin);
        editor.putInt("luz_max", luzMax);
        editor.putBoolean("lluvia_enabled", lluviaEnabled);
        editor.putInt("gas_max", gasMax);
        editor.putInt("humo_max", humoMax);
        editor.putInt("humedad_suelo_min", humedadSueloMin);
        editor.putInt("humedad_suelo_max", humedadSueloMax);

        editor.apply();
    }

    // Metodo para configurar listeners de SeekBar
    private void setupSeekBarListeners(SeekBar seekBar, TextView textView, String unit, int offset) {
        // Configurar el valor inicial del TextView
        int initialValue = seekBar.getProgress() + offset;
        textView.setText(initialValue + unit);

        // Configurar el listener del SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Actualizar el TextView con el nuevo valor
                int value = progress + offset;
                textView.setText(value + unit);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No se necesita implementacion especifica
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No se necesita implementacion especifica
            }
        });
    }

    // Metodo para restablecer valores por defecto
    private void resetToDefaultValues(SeekBar tempMin, SeekBar tempMax, SeekBar humedadMin, SeekBar humedadMax,
                                      SeekBar presionMin, SeekBar presionMax, SeekBar vientoMax, SeekBar luzMin,
                                      SeekBar luzMax, CheckBox lluvia, SeekBar gasMax, SeekBar humoMax,
                                      SeekBar humedadSueloMin, SeekBar humedadSueloMax) {

        // Valores por defecto para temperatura (°C)
        tempMin.setProgress(10);
        tempMax.setProgress(35);

        // Valores por defecto para humedad (%)
        humedadMin.setProgress(30);
        humedadMax.setProgress(80);

        // Valores por defecto para presion (hPa) -  tienen offset de 500
        presionMin.setProgress(480); // 980 hPa
        presionMax.setProgress(530); // 1030 hPa

        // Valor por defecto para viento (km/h)
        vientoMax.setProgress(25);

        // Valores por defecto para luz (lux)
        luzMin.setProgress(100);
        luzMax.setProgress(800);

        // Valor por defecto para lluvia
        lluvia.setChecked(true);

        // Valor por defecto para gas (ppm)
        gasMax.setProgress(500);

        // Valor por defecto para humo (ppm)
        humoMax.setProgress(300);

        // Valores por defecto para humedad del suelo (%)
        humedadSueloMin.setProgress(20);
        humedadSueloMax.setProgress(85);
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
        builder.setTitle(" Editar Perfil");
        builder.setView(dialogView);

        builder.setPositiveButton("Guardar", null);
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        // Configurar el boton positivo después de crear el dialogo
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

        // Deshabilitar el boton mientras se procesa
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
        builder.setTitle(" Cambiar Contraseña");
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