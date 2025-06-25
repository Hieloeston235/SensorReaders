package com.example.sensorreaders;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AjustesFragment extends Fragment {

    private TextView tvNombreUsuario, tvEmailUsuario;
    private Switch switchNotificaciones, switchActualizacion, switchTemaOscuro;
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
        switchTemaOscuro = view.findViewById(R.id.switchTemaOscuro);

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
            Toast.makeText(getContext(), "Función de cambiar contraseña en desarrollo", Toast.LENGTH_SHORT).show();
        });

        // Listener para editar perfil
        layoutEditarPerfil.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Función de editar perfil en desarrollo", Toast.LENGTH_SHORT).show();
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

        switchTemaOscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String message = isChecked ? "Tema oscuro activado" : "Tema claro activado";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });
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