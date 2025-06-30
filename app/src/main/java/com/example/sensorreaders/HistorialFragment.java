package com.example.sensorreaders;

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.DatePicker;

import com.example.sensorreaders.Models.Sensor;
import com.example.sensorreaders.ViewModel.SensorViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Fragmento encargado de mostrar el historial de sensores.
 * Permite aplicar filtros por fechas, visualizar resultados y exportarlos en PDF o Excel.
 */
public class HistorialFragment extends Fragment {
    private Button btnHoy, btnAyer, btn7Dias, btnAplicarFiltro, btnFechaInicio, btnFechaFin, btnDescargarPDF, btnDescargarExcel;
    private RecyclerView recyclerViewHistorial;
    private LinearLayout layoutNoData;
    private TextView tvContadorRegistros;
    private SensorViewModel viewModel;
    private PDFGenerator pdfGenerator;
    private List<Sensor> datosFiltrados;
    private Date fechaInicio, fechaFin;
    private SensorAdapter adapter;
    private SimpleDateFormat dateFormat;

    public HistorialFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historial, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializa referencias a elementos del layout
        initViews(view);

        // Inicializa formato de fecha
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Configura ViewModel y RecyclerView
        setupViewModelAndRecyclerView();

        // Configura listeners de botones
        setupClickListeners();

        // Aplica el filtro por defecto de los últimos 7 días
        setFiltro7Dias();
    }

    private void initViews(View view) {
        btnHoy = view.findViewById(R.id.btnHoy);
        btnAyer = view.findViewById(R.id.btnAyer);
        btn7Dias = view.findViewById(R.id.btn7Dias);
        btnAplicarFiltro = view.findViewById(R.id.btnAplicarFiltro);
        btnFechaInicio = view.findViewById(R.id.btnFechaInicio);
        btnFechaFin = view.findViewById(R.id.btnFechaFin);
        btnDescargarPDF = view.findViewById(R.id.btnDescargarPDF);
        btnDescargarExcel = view.findViewById(R.id.btnDescargarExcel);
        recyclerViewHistorial = view.findViewById(R.id.recyclerViewHistorial);
        layoutNoData = view.findViewById(R.id.layoutNoData);
        tvContadorRegistros = view.findViewById(R.id.tvContadorRegistros);
    }

    private void RefreshConection() {
        // Cambia la fuente de datos a la API en lugar de Firebase
        viewModel.fromFirebaseToApi();
    }

    private void setupViewModelAndRecyclerView() {
        pdfGenerator = new PDFGenerator(getContext());
        viewModel = new ViewModelProvider(requireActivity()).get(SensorViewModel.class);

        recyclerViewHistorial.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SensorAdapter();
        recyclerViewHistorial.setAdapter(adapter);

        // Actualiza la fuente de datos desde la API
        RefreshConection();

        // Observa la lista de sensores y aplica filtro automáticamente cuando llegan datos
        viewModel.getSensorList().observe(getViewLifecycleOwner(), sensores -> aplicarFiltroYMostrar(sensores));
    }

    private void setupClickListeners() {
        // Filtros rápidos
        btnHoy.setOnClickListener(v -> {
            setFiltroHoy();
            resetearSeleccionBotones();
            btnHoy.setSelected(true);
        });

        btnAyer.setOnClickListener(v -> {
            setFiltroAyer();
            resetearSeleccionBotones();
            btnAyer.setSelected(true);
        });

        btn7Dias.setOnClickListener(v -> {
            setFiltro7Dias();
            resetearSeleccionBotones();
            btn7Dias.setSelected(true);
        });

        // Selección de fechas
        btnFechaInicio.setOnClickListener(v -> mostrarDatePicker(true));
        btnFechaFin.setOnClickListener(v -> mostrarDatePicker(false));

        // Aplicar filtro personalizado
        btnAplicarFiltro.setOnClickListener(v -> {
            if (fechaInicio != null && fechaFin != null) {
                if (fechaInicio.after(fechaFin)) {
                    Toast.makeText(getContext(), "La fecha de inicio debe ser anterior a la fecha fin", Toast.LENGTH_SHORT).show();
                    return;
                }
                aplicarFiltroYMostrar(viewModel.getSensorList().getValue());
                resetearSeleccionBotones();
                Toast.makeText(getContext(), "Filtro aplicado correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Selecciona ambas fechas (inicio y fin)", Toast.LENGTH_SHORT).show();
            }
        });

        // Exportar como PDF
        btnDescargarPDF.setOnClickListener(v -> {
            if (datosFiltrados != null && !datosFiltrados.isEmpty()) {
                pdfGenerator.generateFromList(datosFiltrados, "Historial_PDF_" + System.currentTimeMillis(), getActivity());
                Toast.makeText(getContext(), "Generando PDF...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No hay datos para exportar", Toast.LENGTH_SHORT).show();
            }
        });

        // Exportar como Excel
        btnDescargarExcel.setOnClickListener(v -> {
            if (datosFiltrados != null && !datosFiltrados.isEmpty()) {
                pdfGenerator.generateExcelFromList(datosFiltrados, "Historial_Excel_" + System.currentTimeMillis(), getActivity());
                Toast.makeText(getContext(), "Generando Excel...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No hay datos para exportar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Muestra un selector de fecha y actualiza los botones con la selección.
     */
    private void mostrarDatePicker(boolean esFechaInicio) {
        Calendar calendar = Calendar.getInstance();

        if (esFechaInicio && fechaInicio != null) {
            calendar.setTime(fechaInicio);
        } else if (!esFechaInicio && fechaFin != null) {
            calendar.setTime(fechaFin);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    if (esFechaInicio) {
                        fechaInicio = inicioDelDia(selectedDate.getTime());
                        btnFechaInicio.setText("📅 " + dateFormat.format(fechaInicio));
                    } else {
                        fechaFin = finDelDia(selectedDate.getTime());
                        btnFechaFin.setText("📅 " + dateFormat.format(fechaFin));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    // Aplica filtro para hoy
    private void setFiltroHoy() {
        Calendar cal = Calendar.getInstance();
        fechaInicio = inicioDelDia(cal.getTime());
        fechaFin = finDelDia(cal.getTime());
        aplicarFiltroYMostrar(viewModel.getSensorList().getValue());
        actualizarTextoFechas();
    }

    // Aplica filtro para ayer
    private void setFiltroAyer() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        fechaInicio = inicioDelDia(cal.getTime());
        fechaFin = finDelDia(cal.getTime());
        aplicarFiltroYMostrar(viewModel.getSensorList().getValue());
        actualizarTextoFechas();
    }

    // Aplica filtro para los últimos 7 días
    private void setFiltro7Dias() {
        Calendar calInicio = Calendar.getInstance();
        calInicio.add(Calendar.DAY_OF_YEAR, -6);
        fechaInicio = inicioDelDia(calInicio.getTime());
        fechaFin = finDelDia(new Date());
        aplicarFiltroYMostrar(viewModel.getSensorList().getValue());
        actualizarTextoFechas();
    }

    private void actualizarTextoFechas() {
        if (fechaInicio != null) {
            btnFechaInicio.setText("📅 " + dateFormat.format(fechaInicio));
        }
        if (fechaFin != null) {
            btnFechaFin.setText("📅 " + dateFormat.format(fechaFin));
        }
    }

    private void resetearSeleccionBotones() {
        btnHoy.setSelected(false);
        btnAyer.setSelected(false);
        btn7Dias.setSelected(false);
    }

    /**
     * Filtra la lista de sensores por fecha y actualiza la interfaz.
     */
    private void aplicarFiltroYMostrar(List<Sensor> lista) {
        if (lista == null || lista.isEmpty()) {
            recyclerViewHistorial.setVisibility(View.GONE);
            layoutNoData.setVisibility(View.VISIBLE);
            datosFiltrados = null;
            actualizarContadorRegistros(0);
            return;
        }

        if (fechaInicio == null || fechaFin == null) {
            datosFiltrados = lista;
        } else {
            datosFiltrados = lista.stream()
                    .filter(s -> {
                        Date fechaSensor = new Date(s.getFecha());
                        return fechaSensor != null &&
                                !fechaSensor.before(fechaInicio) &&
                                !fechaSensor.after(fechaFin);
                    })
                    .collect(Collectors.toList());
        }

        if (datosFiltrados.isEmpty()) {
            recyclerViewHistorial.setVisibility(View.GONE);
            layoutNoData.setVisibility(View.VISIBLE);
        } else {
            recyclerViewHistorial.setVisibility(View.VISIBLE);
            layoutNoData.setVisibility(View.GONE);
            adapter.setSensorList(datosFiltrados);
        }

        actualizarContadorRegistros(datosFiltrados.size());
    }

    /**
     * Muestra cuántos registros están siendo visualizados.
     */
    private void actualizarContadorRegistros(int cantidad) {
        if (tvContadorRegistros != null) {
            String texto = cantidad == 1 ? cantidad + " registro" : cantidad + " registros";
            tvContadorRegistros.setText(texto);
        }
    }

    private Date inicioDelDia(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date finDelDia(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
}
