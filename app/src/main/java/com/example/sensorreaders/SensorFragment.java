package com.example.sensorreaders;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.sensorreaders.Models.Sensor;
import com.example.sensorreaders.ViewModel.SensorViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragmento principal para mostrar los datos actuales de los sensores y sus gráficos.
 */
public class SensorFragment extends Fragment {

    // Elementos de la interfaz de usuario
    private TextView tvTemperatura, tvHumedad, tvPresion, tvViento, tvLuz, tvLluvia, tvGas, tvHumo, tvHumedadSuelo;
    private TextView tvEstadoConexion, tvFechaActual;
    private View indicadorConexion;
    private Button btnDescargarPDF, btnDescargarExcel, btnRefrescar;

    // ViewModel para acceder a los datos
    private SensorViewModel viewModels;
    private PDFGenerator pdfGenerator;
    private LiveData<List<Sensor>> listaSensores;
    private Integer lastSensorIdShown = null;

    // Gráficos
    private LineChart chartPresion, chartLuz, chartViento;

    // Datos para los gráficos
    private ArrayList<Entry> presionEntries = new ArrayList<>();
    private ArrayList<Entry> luzEntries = new ArrayList<>();
    private ArrayList<Entry> vientoEntries = new ArrayList<>();
    private ArrayList<String> timeLabels = new ArrayList<>();
    private int dataPointCounter = 0;
    private static final int MAX_DATA_POINTS = 20;

    public SensorFragment() {
        // Constructor requerido vacío
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor, container, false);
        pdfGenerator = new PDFGenerator(getContext());

        // Inicializar ViewModel y obtener datos
        viewModels = new ViewModelProvider(requireActivity()).get(SensorViewModel.class);
        viewModels.fromApiToFirebase();
        listaSensores = viewModels.getSensorList();

        initializeViews(view);
        setupCharts();
        observeData();
        setupButtons();
        updateCurrentDate();

        return view;
    }

    /** Inicializa las vistas del layout */
    private void initializeViews(View view) {
        // TextViews
        tvTemperatura = view.findViewById(R.id.tvTemperatura);
        tvHumedad = view.findViewById(R.id.tvHumedad);
        tvPresion = view.findViewById(R.id.tvPresion);
        tvViento = view.findViewById(R.id.tvViento);
        tvLuz = view.findViewById(R.id.tvLuz);
        tvLluvia = view.findViewById(R.id.tvLluvia);
        tvGas = view.findViewById(R.id.tvGas);
        tvHumo = view.findViewById(R.id.tvHumo);
        tvHumedadSuelo = view.findViewById(R.id.tvHumedadSuelo);
        tvEstadoConexion = view.findViewById(R.id.tvEstadoConexion);
        tvFechaActual = view.findViewById(R.id.tvFechaActual);
        indicadorConexion = view.findViewById(R.id.indicadorConexion);

        // Botones
        btnDescargarExcel = view.findViewById(R.id.btnDescargarExcel);
        btnDescargarPDF = view.findViewById(R.id.btnDescargarPDF);
        btnRefrescar = view.findViewById(R.id.btnActualizar);

        // Gráficos
        chartPresion = view.findViewById(R.id.chartPresion);
        chartLuz = view.findViewById(R.id.chartLuz);
        chartViento = view.findViewById(R.id.chartViento);
    }

    /** Configura los gráficos */
    private void setupCharts() {
        setupChart(chartPresion, "Presión Atmosférica (hPa)", Color.rgb(76, 175, 80));
        setupChart(chartLuz, "Intensidad Lumínica (lux)", Color.rgb(255, 193, 7));
        setupChart(chartViento, "Velocidad del Viento (km/h)", Color.rgb(33, 150, 243));
    }

    /** Configura un gráfico individual */
    private void setupChart(LineChart chart, String description, int color) {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);
        chart.setBackgroundColor(Color.WHITE);

        // Eje X
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.GRAY);
        xAxis.setDrawGridLines(true);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return index >= 0 && index < timeLabels.size() ? timeLabels.get(index) : "";
            }
        });

        // Eje Y izquierdo
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(color);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);

        // Eje Y derecho desactivado
        chart.getAxisRight().setEnabled(false);

        chart.getLegend().setEnabled(false);
        chart.animateX(1000);
    }

    /** Observa cambios en los sensores y actualiza la UI */
    private void observeData() {
        listaSensores.observe(getActivity(), sensors -> {
            if (sensors != null && !sensors.isEmpty()) {
                updateConnectionStatus(true);

                Sensor lastSensor = sensors.get(sensors.size() - 1);

                if (lastSensorIdShown == null || !lastSensorIdShown.equals(lastSensor.getId())) {
                    lastSensorIdShown = lastSensor.getId();
                    updateSensorData(lastSensor);
                    updateCharts(lastSensor);
                }
            } else {
                updateConnectionStatus(false);
                clearSensorData();
            }
        });
    }

    /** Actualiza los datos en pantalla */
    private void updateSensorData(Sensor sensor) {
        if (sensor == null) return;
        try {
            tvTemperatura.setText(String.format(Locale.getDefault(), "%.1f °C", sensor.getTemperatura()));
            tvHumedad.setText(String.format(Locale.getDefault(), "%.0f%%", sensor.getHumedad()));
            tvPresion.setText(String.format(Locale.getDefault(), "%.2f hPa", sensor.getPresionAtmosferica()));
            tvViento.setText(String.format(Locale.getDefault(), "%.1f km/h", sensor.getViento()));
            tvLuz.setText(String.format(Locale.getDefault(), "%.0f lux", sensor.getLuz()));
            tvLluvia.setText(sensor.getLluvia() != null && sensor.getLluvia() == 1 ? "Sí" : "No");
            tvGas.setText(String.format(Locale.getDefault(), "%.0f ppm", sensor.getGas()));
            tvHumo.setText(String.format(Locale.getDefault(), "%.0f ppm", sensor.getHumo()));
            tvHumedadSuelo.setText(String.format(Locale.getDefault(), "%.0f%%", sensor.getHumedadSuelo()));
        } catch (Exception e) {
            Log.e("SensorFragment", "Error updating sensor data", e);
            clearSensorData();
        }
    }

    /** Actualiza los gráficos */
    private void updateCharts(Sensor sensor) {
        if (sensor == null) return;
        try {
            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

            presionEntries.add(new Entry(dataPointCounter, sensor.getPresionAtmosferica().floatValue()));
            luzEntries.add(new Entry(dataPointCounter, sensor.getLuz().floatValue()));
            vientoEntries.add(new Entry(dataPointCounter, sensor.getViento().floatValue()));
            timeLabels.add(currentTime);

            if (presionEntries.size() > MAX_DATA_POINTS) {
                presionEntries.remove(0);
                luzEntries.remove(0);
                vientoEntries.remove(0);
                timeLabels.remove(0);

                for (int i = 0; i < presionEntries.size(); i++) {
                    presionEntries.get(i).setX(i);
                    luzEntries.get(i).setX(i);
                    vientoEntries.get(i).setX(i);
                }
            } else {
                dataPointCounter++;
            }

            updateChart(chartPresion, new ArrayList<>(presionEntries), "Presión", Color.rgb(76, 175, 80));
            updateChart(chartLuz, new ArrayList<>(luzEntries), "Luz", Color.rgb(255, 193, 7));
            updateChart(chartViento, new ArrayList<>(vientoEntries), "Viento", Color.rgb(33, 150, 243));

        } catch (Exception e) {
            Log.e("SensorFragment", "Error updating charts", e);
        }
    }

    /** Configura y actualiza un gráfico */
    private void updateChart(LineChart chart, ArrayList<Entry> entries, String label, int color) {
        if (entries.isEmpty()) return;

        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(color);
        dataSet.setFillAlpha(30);
        dataSet.setDrawValues(false);

        chart.setData(new LineData(dataSet));
        chart.invalidate();
    }

    /** Limpia los TextViews si no hay datos */
    private void clearSensorData() {
        tvTemperatura.setText("-- °C");
        tvHumedad.setText("--%");
        tvPresion.setText("-- hPa");
        tvViento.setText("-- km/h");
        tvLuz.setText("-- lux");
        tvLluvia.setText("--");
        tvGas.setText("-- ppm");
        tvHumo.setText("-- ppm");
        tvHumedadSuelo.setText("--%");
    }

    /** Muestra estado de conexión en pantalla */
    private void updateConnectionStatus(boolean isConnected) {
        tvEstadoConexion.setText(isConnected ? "En línea" : "Sin conexión");
        indicadorConexion.setBackgroundColor(isConnected ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));
    }

    /** Muestra la fecha actual */
    private void updateCurrentDate() {
        String currentDate = new SimpleDateFormat("EEEE, dd 'de' MMMM", new Locale("es", "ES")).format(new Date());
        tvFechaActual.setText(currentDate);
    }

    /** Configura listeners para los botones */
    private void setupButtons() {
        btnDescargarExcel.setOnClickListener(v -> {
            viewModels.fromFirebaseToApi();
            viewModels.getSensorList().observe(getViewLifecycleOwner(), new Observer<List<Sensor>>() {
                @Override
                public void onChanged(List<Sensor> sensors) {
                    if (sensors != null && !sensors.isEmpty()) {
                        pdfGenerator.generateExcelFromLiveData(viewModels.getSensorList(),
                                "Reporte_Sensores_Excel_" + System.currentTimeMillis(), getActivity());
                    } else {
                        Toast.makeText(getContext(), "No hay datos para exportar", Toast.LENGTH_SHORT).show();
                    }
                    viewModels.fromApiToFirebase();
                    viewModels.getSensorList().removeObserver(this);
                }
            });
        });

        btnDescargarPDF.setOnClickListener(v -> {
            viewModels.fromFirebaseToApi();
            viewModels.getSensorList().observe(getViewLifecycleOwner(), new Observer<List<Sensor>>() {
                @Override
                public void onChanged(List<Sensor> sensors) {
                    if (sensors != null && !sensors.isEmpty()){
                        pdfGenerator.generateFromLiveData(viewModels.getSensorList(),
                                "Reporte_Sensores_Excel_" + System.currentTimeMillis(), getActivity());
                    } else {
                        Toast.makeText(getContext(), "No hay datos para exportar", Toast.LENGTH_SHORT).show();
                    }
                    viewModels.fromApiToFirebase();
                    viewModels.getSensorList().removeObserver(this);
                }
            });
        });

        btnRefrescar.setOnClickListener(v -> {
            viewModels.fromApiToFirebase();
            listaSensores = viewModels.getSensorList();
            Toast.makeText(getContext(), "Refrescando datos...", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presionEntries != null) presionEntries.clear();
        if (luzEntries != null) luzEntries.clear();
        if (vientoEntries != null) vientoEntries.clear();
        if (timeLabels != null) timeLabels.clear();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Se puede agregar lógica adicional si es necesario al crear la vista
    }
}
