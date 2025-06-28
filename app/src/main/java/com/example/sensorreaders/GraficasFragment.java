package com.example.sensorreaders;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.WebView;

import com.example.sensorreaders.Models.Sensor;
import com.example.sensorreaders.ViewModel.SensorViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class GraficasFragment extends Fragment {

    // Botones de filtro
    private Button btnGraficaHoy, btnGraficaAyer, btnGrafica7Dias;
    private Button btnGraficaFechaInicio, btnGraficaFechaFin, btnAplicarFiltroGrafica;
    private Button btnGenerarGrafica;

    // CheckBoxes para seleccionar variables
    private CheckBox cbTemperatura, cbHumedad, cbPresion, cbHumedadSuelo, cbLuz, cbViento;

    // Vistas
    private TextView tvContadorDatosGrafica, tvEstadisticas;
    private CardView layoutNoDataGrafica, layoutEstadisticas;
    private CardView cardGrafica;
    private LineChart lineChart;

    // Variables de estado
    private SensorViewModel viewModel;
    private List<Sensor> datosFiltrados;
    private Date fechaInicio, fechaFin;
    private SimpleDateFormat dateFormat;

    // Colores para las líneas del gráfico
    private int[] coloresLineas = {
            Color.rgb(255, 102, 102), // Rojo - Temperatura
            Color.rgb(102, 178, 255), // Azul - Humedad
            Color.rgb(153, 204, 0),   // Verde - Presión
            Color.rgb(255, 187, 51),  // Naranja - Humedad Suelo
            Color.rgb(255, 221, 51),  // Amarillo - Luz
            Color.rgb(170, 170, 170)  // Gris - Viento
    };

    public GraficasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_graficas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        initViews(view);

        // Configurar formato de fecha
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Configurar ViewModel
        setupViewModel();

        // Configurar gráfico
        setupChart();

        // Configurar listeners
        setupClickListeners();

        // Cargar datos iniciales (últimos 7 días por defecto)
        setFiltro7Dias();
    }

    private void initViews(View view) {
        // Botones de filtro rápido
        btnGraficaHoy = view.findViewById(R.id.btnGraficaHoy);
        btnGraficaAyer = view.findViewById(R.id.btnGraficaAyer);
        btnGrafica7Dias = view.findViewById(R.id.btnGrafica7Dias);

        // Botones de fecha personalizada
        btnGraficaFechaInicio = view.findViewById(R.id.btnGraficaFechaInicio);
        btnGraficaFechaFin = view.findViewById(R.id.btnGraficaFechaFin);
        btnAplicarFiltroGrafica = view.findViewById(R.id.btnAplicarFiltroGrafica);

        // Botón generar gráfica
        btnGenerarGrafica = view.findViewById(R.id.btnGenerarGrafica);

        // CheckBoxes
        cbTemperatura = view.findViewById(R.id.cbTemperatura);
        cbHumedad = view.findViewById(R.id.cbHumedad);
        cbPresion = view.findViewById(R.id.cbPresion);
        cbHumedadSuelo = view.findViewById(R.id.cbHumedadSuelo);
        cbLuz = view.findViewById(R.id.cbLuz);
        cbViento = view.findViewById(R.id.cbViento);

        // Vistas informativas
        tvContadorDatosGrafica = view.findViewById(R.id.tvContadorDatosGrafica);
        tvEstadisticas = view.findViewById(R.id.tvEstadisticas);

        // Layouts
        cardGrafica = view.findViewById(R.id.cardGrafica);
        layoutNoDataGrafica = view.findViewById(R.id.layoutNoDataGrafica);
        layoutEstadisticas = view.findViewById(R.id.layoutEstadisticas);

        // Crear LineChart programáticamente y agregarlo al CardView
        lineChart = new LineChart(getContext());
        cardGrafica.addView(lineChart);
    }

    private void setupViewModel() {
        viewModel = new SensorViewModel(getActivity().getApplication());
        viewModel.refreshFromApi();

        viewModel.getSensorList().observe(getViewLifecycleOwner(), sensores -> {
            aplicarFiltroYActualizar(sensores);
        });
    }

    private void setupChart() {
        // Configuración básica del gráfico
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        // Descripción
        Description description = new Description();
        description.setText("Datos de Sensores");
        description.setTextSize(12f);
        lineChart.setDescription(description);

        // Configurar ejes
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setGranularity(1f);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Configurar leyenda
        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(12f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
    }

    private void setupClickListeners() {
        // Botones de filtro rápido
        btnGraficaHoy.setOnClickListener(v -> {
            setFiltroHoy();
            resetearSeleccionBotones();
            btnGraficaHoy.setSelected(true);
        });

        btnGraficaAyer.setOnClickListener(v -> {
            setFiltroAyer();
            resetearSeleccionBotones();
            btnGraficaAyer.setSelected(true);
        });

        btnGrafica7Dias.setOnClickListener(v -> {
            setFiltro7Dias();
            resetearSeleccionBotones();
            btnGrafica7Dias.setSelected(true);
        });

        // Selectores de fecha
        btnGraficaFechaInicio.setOnClickListener(v -> mostrarDatePicker(true));
        btnGraficaFechaFin.setOnClickListener(v -> mostrarDatePicker(false));

        // Aplicar filtro personalizado
        btnAplicarFiltroGrafica.setOnClickListener(v -> {
            if (fechaInicio != null && fechaFin != null) {
                if (fechaInicio.after(fechaFin)) {
                    Toast.makeText(getContext(), "La fecha de inicio debe ser anterior a la fecha fin", Toast.LENGTH_SHORT).show();
                    return;
                }
                aplicarFiltroYActualizar(viewModel.getSensorList().getValue());
                resetearSeleccionBotones();
                Toast.makeText(getContext(), "Filtro aplicado correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Selecciona ambas fechas (inicio y fin)", Toast.LENGTH_SHORT).show();
            }
        });

        // Generar gráfica
        btnGenerarGrafica.setOnClickListener(v -> generarGrafica());
    }

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
                        btnGraficaFechaInicio.setText("📅 " + dateFormat.format(fechaInicio));
                    } else {
                        fechaFin = finDelDia(selectedDate.getTime());
                        btnGraficaFechaFin.setText("📅 " + dateFormat.format(fechaFin));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void setFiltroHoy() {
        Calendar cal = Calendar.getInstance();
        fechaInicio = inicioDelDia(cal.getTime());
        fechaFin = finDelDia(cal.getTime());
        aplicarFiltroYActualizar(viewModel.getSensorList().getValue());
        actualizarTextoFechas();
    }

    private void setFiltroAyer() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        fechaInicio = inicioDelDia(cal.getTime());
        fechaFin = finDelDia(cal.getTime());
        aplicarFiltroYActualizar(viewModel.getSensorList().getValue());
        actualizarTextoFechas();
    }

    private void setFiltro7Dias() {
        Calendar calInicio = Calendar.getInstance();
        calInicio.add(Calendar.DAY_OF_YEAR, -6);
        fechaInicio = inicioDelDia(calInicio.getTime());
        fechaFin = finDelDia(new Date());
        aplicarFiltroYActualizar(viewModel.getSensorList().getValue());
        actualizarTextoFechas();
    }

    private void actualizarTextoFechas() {
        if (fechaInicio != null) {
            btnGraficaFechaInicio.setText("📅 " + dateFormat.format(fechaInicio));
        }
        if (fechaFin != null) {
            btnGraficaFechaFin.setText("📅 " + dateFormat.format(fechaFin));
        }
    }

    private void resetearSeleccionBotones() {
        btnGraficaHoy.setSelected(false);
        btnGraficaAyer.setSelected(false);
        btnGrafica7Dias.setSelected(false);
    }

    private void aplicarFiltroYActualizar(List<Sensor> lista) {
        if (lista == null || lista.isEmpty()) {
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

        actualizarContadorRegistros(datosFiltrados != null ? datosFiltrados.size() : 0);
    }

    private void generarGrafica() {
        if (datosFiltrados == null || datosFiltrados.isEmpty()) {
            Toast.makeText(getContext(), "No hay datos para graficar", Toast.LENGTH_SHORT).show();
            mostrarNoData(true);
            return;
        }

        // Verificar que al menos una variable esté seleccionada
        if (!cbTemperatura.isChecked() && !cbHumedad.isChecked() && !cbPresion.isChecked() &&
                !cbHumedadSuelo.isChecked() && !cbLuz.isChecked() && !cbViento.isChecked()) {
            Toast.makeText(getContext(), "Selecciona al menos una variable para graficar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear datasets para cada variable seleccionada
        List<ILineDataSet> dataSets = new ArrayList<>();
        List<String> etiquetasTiempo = crearEtiquetasTiempo();

        int colorIndex = 0;

        if (cbTemperatura.isChecked()) {
            LineDataSet dataSet = crearDataSet("Temperatura (°C)", extraerDatosTemperatura(), coloresLineas[colorIndex % coloresLineas.length]);
            dataSets.add(dataSet);
            colorIndex++;
        }

        if (cbHumedad.isChecked()) {
            LineDataSet dataSet = crearDataSet("Humedad (%)", extraerDatosHumedad(), coloresLineas[colorIndex % coloresLineas.length]);
            dataSets.add(dataSet);
            colorIndex++;
        }

        if (cbPresion.isChecked()) {
            LineDataSet dataSet = crearDataSet("Presión (hPa)", extraerDatosPresion(), coloresLineas[colorIndex % coloresLineas.length]);
            dataSets.add(dataSet);
            colorIndex++;
        }

        if (cbHumedadSuelo.isChecked()) {
            LineDataSet dataSet = crearDataSet("Humedad Suelo (%)", extraerDatosHumedadSuelo(), coloresLineas[colorIndex % coloresLineas.length]);
            dataSets.add(dataSet);
            colorIndex++;
        }

        if (cbLuz.isChecked()) {
            LineDataSet dataSet = crearDataSet("Luz (lux)", extraerDatosLuz(), coloresLineas[colorIndex % coloresLineas.length]);
            dataSets.add(dataSet);
            colorIndex++;
        }

        if (cbViento.isChecked()) {
            LineDataSet dataSet = crearDataSet("Viento (km/h)", extraerDatosViento(), coloresLineas[colorIndex % coloresLineas.length]);
            dataSets.add(dataSet);
        }

        // Configurar datos en el gráfico
        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);

        // Configurar etiquetas del eje X
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(etiquetasTiempo));
        xAxis.setLabelCount(Math.min(etiquetasTiempo.size(), 10));

        // Actualizar gráfico
        lineChart.invalidate();

        // Mostrar gráfico
        mostrarNoData(false);

        // Generar estadísticas
        generarEstadisticas();

        Toast.makeText(getContext(), "Gráfica generada exitosamente", Toast.LENGTH_SHORT).show();
    }

    private List<String> crearEtiquetasTiempo() {
        List<String> etiquetas = new ArrayList<>();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());

        for (Sensor sensor : datosFiltrados) {
            Date fecha = new Date(sensor.getFecha());
            // Si el rango es de un día, mostrar solo hora, si no, mostrar fecha y hora
            long diffDays = (fechaFin.getTime() - fechaInicio.getTime()) / (24 * 60 * 60 * 1000);
            if (diffDays <= 1) {
                etiquetas.add(timeFormat.format(fecha));
            } else {
                etiquetas.add(dateTimeFormat.format(fecha));
            }
        }
        return etiquetas;
    }

    private LineDataSet crearDataSet(String label, List<Entry> entries, int color) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(0f); // Ocultar valores en los puntos
        dataSet.setDrawFilled(false);
        return dataSet;
    }

    private List<Entry> extraerDatosTemperatura() {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < datosFiltrados.size(); i++) {
            Double temp = datosFiltrados.get(i).getTemperatura();
            if (temp != null) {
                entries.add(new Entry(i, temp.floatValue()));
            }
        }
        return entries;
    }

    private List<Entry> extraerDatosHumedad() {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < datosFiltrados.size(); i++) {
            Double hum = datosFiltrados.get(i).getHumedad();
            if (hum != null) {
                entries.add(new Entry(i, hum.floatValue()));
            }
        }
        return entries;
    }

    private List<Entry> extraerDatosPresion() {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < datosFiltrados.size(); i++) {
            Double pres = datosFiltrados.get(i).getPresionAtmosferica();
            if (pres != null) {
                entries.add(new Entry(i, pres.floatValue()));
            }
        }
        return entries;
    }

    private List<Entry> extraerDatosHumedadSuelo() {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < datosFiltrados.size(); i++) {
            Double humSuelo = datosFiltrados.get(i).getHumedadSuelo();
            if (humSuelo != null) {
                entries.add(new Entry(i, humSuelo.floatValue()));
            }
        }
        return entries;
    }

    private List<Entry> extraerDatosLuz() {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < datosFiltrados.size(); i++) {
            Double luz = datosFiltrados.get(i).getLuz();
            if (luz != null) {
                entries.add(new Entry(i, luz.floatValue()));
            }
        }
        return entries;
    }

    private List<Entry> extraerDatosViento() {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < datosFiltrados.size(); i++) {
            Double viento = datosFiltrados.get(i).getViento();
            if (viento != null) {
                entries.add(new Entry(i, viento.floatValue()));
            }
        }
        return entries;
    }

    private void generarEstadisticas() {
        if (datosFiltrados == null || datosFiltrados.isEmpty()) {
            layoutEstadisticas.setVisibility(View.GONE);
            return;
        }

        StringBuilder estadisticas = new StringBuilder();

        // Calcular estadísticas para cada variable seleccionada
        if (cbTemperatura.isChecked()) {
            calcularEstadisticasVariable(estadisticas, "Temperatura", "°C",
                    datosFiltrados.stream().map(Sensor::getTemperatura).collect(Collectors.toList()));
        }

        if (cbHumedad.isChecked()) {
            calcularEstadisticasVariable(estadisticas, "Humedad", "%",
                    datosFiltrados.stream().map(Sensor::getHumedad).collect(Collectors.toList()));
        }

        if (cbPresion.isChecked()) {
            calcularEstadisticasVariable(estadisticas, "Presión", "hPa",
                    datosFiltrados.stream().map(Sensor::getPresionAtmosferica).collect(Collectors.toList()));
        }

        if (cbHumedadSuelo.isChecked()) {
            calcularEstadisticasVariable(estadisticas, "Humedad Suelo", "%",
                    datosFiltrados.stream().map(Sensor::getHumedadSuelo).collect(Collectors.toList()));
        }

        if (cbLuz.isChecked()) {
            calcularEstadisticasVariable(estadisticas, "Luz", "lux",
                    datosFiltrados.stream().map(Sensor::getLuz).collect(Collectors.toList()));
        }

        if (cbViento.isChecked()) {
            calcularEstadisticasVariable(estadisticas, "Viento", "km/h",
                    datosFiltrados.stream().map(Sensor::getViento).collect(Collectors.toList()));
        }

        tvEstadisticas.setText(estadisticas.toString());
        layoutEstadisticas.setVisibility(View.VISIBLE);
    }

    private void calcularEstadisticasVariable(StringBuilder sb, String nombre, String unidad, List<Double> valores) {
        List<Double> valoresLimpios = valores.stream()
                .filter(v -> v != null)
                .collect(Collectors.toList());

        if (valoresLimpios.isEmpty()) return;

        double min = valoresLimpios.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = valoresLimpios.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double promedio = valoresLimpios.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        sb.append(String.format("📊 %s:\n", nombre));
        sb.append(String.format("   Min: %.1f %s\n", min, unidad));
        sb.append(String.format("   Max: %.1f %s\n", max, unidad));
        sb.append(String.format("   Promedio: %.1f %s\n\n", promedio, unidad));
    }

    private void mostrarNoData(boolean mostrar) {
        if (mostrar) {
            cardGrafica.setVisibility(View.GONE);
            layoutNoDataGrafica.setVisibility(View.VISIBLE);
            layoutEstadisticas.setVisibility(View.GONE);
        } else {
            cardGrafica.setVisibility(View.VISIBLE);
            layoutNoDataGrafica.setVisibility(View.GONE);
        }
    }

    private void actualizarContadorRegistros(int cantidad) {
        if (tvContadorDatosGrafica != null) {
            String texto = cantidad + " registros para graficar";
            tvContadorDatosGrafica.setText(texto);
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