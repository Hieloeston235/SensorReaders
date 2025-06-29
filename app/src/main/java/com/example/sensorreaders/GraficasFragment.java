package com.example.sensorreaders;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GraficasFragment extends Fragment {

    // Botones de filtro
    private Button btnGraficaHoy, btnGraficaAyer, btnGrafica7Dias;
    private Button btnGraficaFechaInicio, btnGraficaFechaFin, btnAplicarFiltroGrafica;
    private Button btnGenerarGrafica;
    private Button btnGraficaHoraInicio, btnGraficaHoraFin;
    private Button btnUltimas3Horas, btnUltimas6Horas, btnUltimas12Horas, btnUltimas24Horas,btnLimpiarFiltros;

    // CheckBoxes para seleccionar variables
    private CheckBox cbTemperatura, cbHumedad, cbPresion, cbHumedadSuelo, cbLuz, cbViento, cbHumo, cbGas;
    private Switch switchFiltroHoras;

    // Vistas
    private TextView tvContadorDatosGrafica, tvEstadisticas, tvRangoSeleccionado;
    private CardView layoutNoDataGrafica, layoutEstadisticas;
    private CardView cardGrafica;
    private LineChart lineChart;
    private TextView tvEmptyChart;

    // Variables de estado
    private SensorViewModel viewModel;
    private List<Sensor> datosFiltrados;
    private Date fechaInicio, fechaFin;
    private int horaInicio = 0, minutoInicio = 0, horaFin = 23, minutoFin = 59;
    private SimpleDateFormat dateFormat, timeFormat, dateTimeFormat;

    // Colores para las lineas del grafico
    private int[] coloresLineas = {
            Color.rgb(255, 102, 102), // Rojo - Temperatura
            Color.rgb(102, 178, 255), // Azul - Humedad
            Color.rgb(153, 204, 0),   // Verde - Presion
            Color.rgb(255, 187, 51),  // Naranja - Humedad Suelo
            Color.rgb(255, 221, 51),  // Amarillo - Luz
            Color.rgb(170, 170, 170), // Gris - Viento
            Color.rgb(153, 102, 255), // Morado - Humo
            Color.rgb(255, 102, 153)  // Rosa - Gas
    };

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

        // Configurar tamaño del chart segun pantalla
        ajustarTamanoChart();

        // Configurar formatos de fecha y hora
        setupFormats();

        // Configurar ViewModel
        setupViewModel();

        // Configurar grafico
        setupChart();

        // Configurar listeners
        setupClickListeners();

        // Cargar datos iniciales (ultimos 7 dias por defecto)
        setFiltro7Dias();

    }

    private void ajustarTamanoChart() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;

        ViewGroup.LayoutParams params = cardGrafica.getLayoutParams();
        if (displayMetrics.widthPixels < 600) { // Pantallas pequeñas
            params.height = (int) (screenHeight * 0.35); // 35% de la altura
        } else { // Tablets o pantallas grandes
            params.height = (int) (screenHeight * 0.45); // 45% de la altura
        }
        cardGrafica.setLayoutParams(params);
    }

    private void initViews(View view) {
        // Botones de filtro rapido
        btnGraficaHoy = view.findViewById(R.id.btnGraficaHoy);
        btnGraficaAyer = view.findViewById(R.id.btnGraficaAyer);
        btnGrafica7Dias = view.findViewById(R.id.btnGrafica7Dias);

        // Botones de fecha personalizada
        btnGraficaFechaInicio = view.findViewById(R.id.btnGraficaFechaInicio);
        btnGraficaFechaFin = view.findViewById(R.id.btnGraficaFechaFin);
        btnAplicarFiltroGrafica = view.findViewById(R.id.btnAplicarFiltroGrafica);
        btnLimpiarFiltros = view.findViewById(R.id.btnLimpiarFiltros);

        // Botones de hora
        btnGraficaHoraInicio = view.findViewById(R.id.btnGraficaHoraInicio);
        btnGraficaHoraFin = view.findViewById(R.id.btnGraficaHoraFin);
        btnUltimas3Horas = view.findViewById(R.id.btnUltimas3Horas);
        btnUltimas6Horas = view.findViewById(R.id.btnUltimas6Horas);
        btnUltimas12Horas = view.findViewById(R.id.btnUltimas12Horas);
        btnUltimas24Horas = view.findViewById(R.id.btnUltimas24Horas);
        switchFiltroHoras = view.findViewById(R.id.switchFiltroHoras);

        // Desactivar botones de rangos rapidos por defecto
        btnUltimas3Horas.setEnabled(false);
        btnUltimas6Horas.setEnabled(false);
        btnUltimas12Horas.setEnabled(false);
        btnUltimas24Horas.setEnabled(false);

        // Boton generar grafica
        btnGenerarGrafica = view.findViewById(R.id.btnGenerarGrafica);

        // CheckBoxes
        cbTemperatura = view.findViewById(R.id.cbTemperatura);
        cbHumedad = view.findViewById(R.id.cbHumedad);
        cbPresion = view.findViewById(R.id.cbPresion);
        cbHumedadSuelo = view.findViewById(R.id.cbHumedadSuelo);
        cbLuz = view.findViewById(R.id.cbLuz);
        cbViento = view.findViewById(R.id.cbViento);
        cbHumo = view.findViewById(R.id.cbHumo);
        cbGas = view.findViewById(R.id.cbGas);

        // Vistas informativas
        tvContadorDatosGrafica = view.findViewById(R.id.tvContadorDatosGrafica);
        tvEstadisticas = view.findViewById(R.id.tvEstadisticas);
        tvRangoSeleccionado = view.findViewById(R.id.tvRangoSeleccionado);
        tvEmptyChart = view.findViewById(R.id.tvEmptyChart);

        // Layouts
        cardGrafica = view.findViewById(R.id.cardGrafica);
        layoutNoDataGrafica = view.findViewById(R.id.layoutNoDataGrafica);
        layoutEstadisticas = view.findViewById(R.id.layoutEstadisticas);

        // Crear LineChart programaticamente y agregarlo al CardView
        lineChart = new LineChart(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        lineChart.setLayoutParams(params);
        ((ViewGroup)cardGrafica.getChildAt(0)).addView(lineChart, 0);
    }

    private void setupFormats() {
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateTimeFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());

        // Opcional: usar UTC para evitar problemas de zona horaria
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(SensorViewModel.class);

        viewModel.getSensorList().observe(getViewLifecycleOwner(), sensores -> {
            if (sensores != null && !sensores.isEmpty()) {
                aplicarFiltroYActualizar(sensores);
                if (sensores != null) {
                    Log.d("DATOS", "Total de datos recibidos: " + sensores.size());
                    for(Sensor s : sensores) {
                        Log.d("DATOS", "ID: " + s.getId() + " - Fecha: " +
                                (s != null ? new Date(s.getFecha()).toString() : "null"));
                    }
                }

            } else {
                // Intentar cargar datos si no hay
                viewModel.refreshFromApi();
            }
        });

        // Forzar carga inicial si no hay datos
        if (viewModel.getSensorList().getValue() == null || viewModel.getSensorList().getValue().isEmpty()) {
            viewModel.refreshFromApi();
        }

    }

    private void setupChart() {
        // Configuracion basica
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDoubleTapToZoomEnabled(true);
        lineChart.setHighlightPerDragEnabled(true);
        lineChart.setDrawGridBackground(false);

        // Ajustar margenes optimizados para la leyenda
        lineChart.setExtraBottomOffset(40f); // Espacio para leyenda horizontal
        lineChart.setExtraTopOffset(10f);
        lineChart.setExtraLeftOffset(5f);
        lineChart.setExtraRightOffset(5f);

        // Descripcion más compacta
        Description description = new Description();
        description.setText("Sensores");
        description.setTextSize(10f);
        description.setTextColor(Color.GRAY);
        lineChart.setDescription(description);

        // Configurar ejes X
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setDrawGridLines(false);
        xAxis.setCenterAxisLabels(false);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setLabelCount(5, true);
        xAxis.setTextSize(9f);
        xAxis.setSpaceMin(0.5f);  // Espacio minimo entre etiquetas
        xAxis.setSpaceMax(0.5f);

        // Configurar eje Y izquierdo
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#EEEEEE"));
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(9f);
        leftAxis.setSpaceTop(10f);  // Espacio en la parte superior
        leftAxis.setSpaceBottom(10f);

        // Deshabilitar eje Y derecho
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Configurar leyenda optimizada para multiples elementos
        setupLegendOptimized();
    }

    private void setupLegendOptimized() {
        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setFormSize(8f);
        legend.setTextSize(10f);
        legend.setTextColor(Color.BLACK);

        // Configuracion para layout horizontal compacto
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        // Configuración de espaciado para layout compacto
        legend.setXEntrySpace(8f);  // Espacio horizontal entre entradas
        legend.setYEntrySpace(2f);  // Espacio vertical entre lineas
        legend.setFormToTextSpace(3f); // Espacio entre icono y texto
        legend.setWordWrapEnabled(true); // Permitir salto de linea
        legend.setMaxSizePercent(0.9f); // Usar hasta 90% del ancho disponible

    }

    private void setupClickListeners() {
        // Botones de filtro rapido
        btnGraficaHoy.setOnClickListener(v -> {
            setFiltroHoy();
            resetearSeleccionBotones();
            btnGraficaHoy.setSelected(true);
            actualizarTextoRangoSeleccionado();
        });

        btnGraficaAyer.setOnClickListener(v -> {
            setFiltroAyer();
            resetearSeleccionBotones();
            btnGraficaAyer.setSelected(true);
            actualizarTextoRangoSeleccionado();
        });

        btnGrafica7Dias.setOnClickListener(v -> {
            setFiltro7Dias();
            resetearSeleccionBotones();
            btnGrafica7Dias.setSelected(true);
            actualizarTextoRangoSeleccionado();
        });

        // Selectores de fecha
        btnGraficaFechaInicio.setOnClickListener(v -> mostrarDatePicker(true));
        btnGraficaFechaFin.setOnClickListener(v -> mostrarDatePicker(false));

        // Selectores de hora
        btnGraficaHoraInicio.setOnClickListener(v -> mostrarTimePicker(true));
        btnGraficaHoraFin.setOnClickListener(v -> mostrarTimePicker(false));

        // Filtros rapidos de horas
        btnUltimas3Horas.setOnClickListener(v -> setFiltroUltimasHoras(3));
        btnUltimas6Horas.setOnClickListener(v -> setFiltroUltimasHoras(6));
        btnUltimas12Horas.setOnClickListener(v -> setFiltroUltimasHoras(12));
        btnUltimas24Horas.setOnClickListener(v -> setFiltroUltimasHoras(24));

        // Switch para habilitar/deshabilitar filtros de hora
        switchFiltroHoras.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateTimeRangeButtonsState(isChecked);
            //  habilita/deshabilita el filtro por horas para cualquier rango
            if(isChecked) {
                // Establecer horas por defecto (0:00 a 23:59) si no están seteadas
                if(btnGraficaHoraInicio.getText().toString().equals("🕐 Hora Inicio")) {
                    horaInicio = 0;
                    minutoInicio = 0;
                    horaFin = 23;
                    minutoFin = 59;
                    actualizarTextoHoras();
                }
            }
        });

        // Aplicar filtro personalizado
        btnAplicarFiltroGrafica.setOnClickListener(v -> {
            // Validar que ambas fechas esten seleccionadas
            if (fechaInicio == null || fechaFin == null) {
                Toast.makeText(getContext(), "Debes seleccionar ambas fechas (inicio y fin)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar que la fecha de inicio no sea posterior a la fecha fin
            if (fechaInicio.after(fechaFin)) {
                Toast.makeText(getContext(), "La fecha de inicio debe ser anterior a la fecha fin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar que el rango no sea mayor a 30 dias
            long diffInMillis = fechaFin.getTime() - fechaInicio.getTime();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            if (diffInDays > 30) {
                Toast.makeText(getContext(), "El rango maximo permitido es de 30 días", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar horas si el filtro de horas esta activado
            if (switchFiltroHoras.isChecked()) {
                if (horaInicio < 0 || horaInicio > 23 || minutoInicio < 0 || minutoInicio > 59 ||
                        horaFin < 0 || horaFin > 23 || minutoFin < 0 || minutoFin > 59) {
                    Toast.makeText(getContext(), "Las horas seleccionadas no son validas", Toast.LENGTH_SHORT).show();
                    return;
                }

                Calendar calInicio = Calendar.getInstance();
                calInicio.setTime(fechaInicio);
                calInicio.set(Calendar.HOUR_OF_DAY, horaInicio);
                calInicio.set(Calendar.MINUTE, minutoInicio);

                Calendar calFin = Calendar.getInstance();
                calFin.setTime(fechaFin);
                calFin.set(Calendar.HOUR_OF_DAY, horaFin);
                calFin.set(Calendar.MINUTE, minutoFin);

                if (calInicio.after(calFin)) {
                    Toast.makeText(getContext(), "La hora de inicio debe ser anterior a la hora fin", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            btnLimpiarFiltros.setOnClickListener(x -> {
                limpiarFiltros();
            });

            aplicarFiltroYActualizar(viewModel.getSensorList().getValue());
            resetearSeleccionBotones();
            actualizarTextoRangoSeleccionado();
            Toast.makeText(getContext(), "Filtro aplicado correctamente", Toast.LENGTH_SHORT).show();
        });

        // Generar grafica
        btnGenerarGrafica.setOnClickListener(v -> {
            if (fechaInicio == null || fechaFin == null) {
                Toast.makeText(getContext(), "Debes seleccionar un rango de fechas primero", Toast.LENGTH_SHORT).show();
                return;
            }
            // Validar que haya datos filtrados
            if (datosFiltrados == null || datosFiltrados.isEmpty()) {
                Toast.makeText(getContext(), "No hay datos para graficar con los filtros actuales", Toast.LENGTH_SHORT).show();
                mostrarNoData(true);
                return;
            }

            // Validar que al menos una variable esté seleccionada
            if (!haySensoresSeleccionados()) {
                Toast.makeText(getContext(), "Selecciona al menos una variable para graficar", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar que las variables seleccionadas tengan datos
            if (!validarDatosSensoresSeleccionados()) {
                Toast.makeText(getContext(), "Las variables seleccionadas no tienen datos en el rango filtrado", Toast.LENGTH_SHORT).show();
                return;
            }

            generarGrafica();
        });


    }

    // Metodo para limpiar filtros
    private void limpiarFiltros() {
        // Resetear fechas
        fechaInicio = null;
        fechaFin = null;
        btnGraficaFechaInicio.setText("📅 Fecha Inicio");
        btnGraficaFechaFin.setText("📅 Fecha Fin");

        // Resetear horas
        horaInicio = 0;
        minutoInicio = 0;
        horaFin = 23;
        minutoFin = 59;
        actualizarTextoHoras();

        // Desactivar switch
        switchFiltroHoras.setChecked(false);

        // Resetear seleccion de botones
        resetearSeleccionBotones();
        resetTimeRangeButtonsSelection();

        // Mostrar mensaje
        Toast.makeText(getContext(), "Filtros limpiados", Toast.LENGTH_SHORT).show();
        actualizarTextoRangoSeleccionado();
    }
    private boolean haySensoresSeleccionados() {
        return cbTemperatura.isChecked() || cbHumedad.isChecked() || cbPresion.isChecked() ||
                cbHumedadSuelo.isChecked() || cbLuz.isChecked() || cbViento.isChecked() ||
                cbHumo.isChecked() || cbGas.isChecked();
    }

    private boolean validarDatosSensoresSeleccionados() {
        boolean hasValidData = false;
        if (cbTemperatura.isChecked() && datosFiltrados.stream().anyMatch(s -> s.getTemperatura() != null)) hasValidData = true;
        if (cbHumedad.isChecked() && datosFiltrados.stream().anyMatch(s -> s.getHumedad() != null)) hasValidData = true;
        if (cbPresion.isChecked() && datosFiltrados.stream().anyMatch(s -> s.getPresionAtmosferica() != null)) hasValidData = true;
        if (cbHumedadSuelo.isChecked() && datosFiltrados.stream().anyMatch(s -> s.getHumedadSuelo() != null)) hasValidData = true;
        if (cbLuz.isChecked() && datosFiltrados.stream().anyMatch(s -> s.getLuz() != null)) hasValidData = true;
        if (cbViento.isChecked() && datosFiltrados.stream().anyMatch(s -> s.getViento() != null)) hasValidData = true;
        if (cbHumo.isChecked() && datosFiltrados.stream().anyMatch(s -> s.getHumo() != null && s.getHumo() > 0)) hasValidData = true;
        if (cbGas.isChecked() && datosFiltrados.stream().anyMatch(s -> s.getGas() != null && s.getGas() > 0)) hasValidData = true;

        return hasValidData;
    }

    private void updateTimeRangeButtonsState(boolean enabled) {
        int bgColor = enabled ? getResources().getColor(R.color.accent_color) : getResources().getColor(R.color.button_disabled);
        int textColor = enabled ? Color.WHITE : getResources().getColor(R.color.text_disabled);

        btnUltimas3Horas.setEnabled(enabled);
        btnUltimas3Horas.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        btnUltimas3Horas.setTextColor(textColor);

        btnUltimas6Horas.setEnabled(enabled);
        btnUltimas6Horas.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        btnUltimas6Horas.setTextColor(textColor);

        btnUltimas12Horas.setEnabled(enabled);
        btnUltimas12Horas.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        btnUltimas12Horas.setTextColor(textColor);

        btnUltimas24Horas.setEnabled(enabled);
        btnUltimas24Horas.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        btnUltimas24Horas.setTextColor(textColor);
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
                    actualizarTextoRangoSeleccionado();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void mostrarTimePicker(boolean esHoraInicio) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (TimePicker view, int hourOfDay, int minute) -> {
                    if (esHoraInicio) {
                        horaInicio = hourOfDay;
                        minutoInicio = minute;
                        btnGraficaHoraInicio.setText("🕐 " + String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                    } else {
                        horaFin = hourOfDay;
                        minutoFin = minute;
                        btnGraficaHoraFin.setText("🕐 " + String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                    }
                    actualizarTextoRangoSeleccionado();
                },
                esHoraInicio ? horaInicio : horaFin,
                esHoraInicio ? minutoInicio : minutoFin,
                true
        );
        timePickerDialog.show();
    }

    private void setFiltroHoy() {
        limpiarGrafica();
        Calendar cal = Calendar.getInstance();
        fechaInicio = inicioDelDia(cal.getTime());
        fechaFin = finDelDia(cal.getTime());
        horaInicio = 0;
        minutoInicio = 0;
        horaFin = 23;
        minutoFin = 59;
        actualizarTextoHoras();
        aplicarFiltroYActualizar(viewModel.getSensorList().getValue());
    }

    private void setFiltroAyer() {
        limpiarGrafica();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        fechaInicio = inicioDelDia(cal.getTime());
        fechaFin = finDelDia(cal.getTime());
        horaInicio = 0;
        minutoInicio = 0;
        horaFin = 23;
        minutoFin = 59;
        actualizarTextoHoras();
        aplicarFiltroYActualizar(viewModel.getSensorList().getValue());
    }

    private void setFiltro7Dias() {
        limpiarGrafica();
        Calendar calInicio = Calendar.getInstance();
        calInicio.add(Calendar.DAY_OF_YEAR, -7);
        fechaInicio = inicioDelDia(calInicio.getTime());

        Calendar calFin = Calendar.getInstance();
        fechaFin = finDelDia(calFin.getTime());

        horaInicio = 0;
        minutoInicio = 0;
        horaFin = 23;
        minutoFin = 59;
        actualizarTextoHoras();
        actualizarTextoFechas();

        // Forzar actualizacion del switch
        switchFiltroHoras.setChecked(true);
        updateTimeRangeButtonsState(true);

        aplicarFiltroYActualizar(viewModel.getSensorList().getValue());

        // Depuración
        Log.d("FILTRO_7DIAS", "Fecha inicio: " + dateFormat.format(fechaInicio) +
                " - Fecha fin: " + dateFormat.format(fechaFin));
    }

    private void setFiltroUltimasHoras(int horas) {
        limpiarGrafica();

        // Configurar fechas basadas en las horas
        Calendar calFin = Calendar.getInstance();
        fechaFin = calFin.getTime();

        Calendar calInicio = Calendar.getInstance();
        calInicio.add(Calendar.HOUR_OF_DAY, -horas);
        fechaInicio = calInicio.getTime();

        // Configurar horas para el filtro
        horaInicio = calInicio.get(Calendar.HOUR_OF_DAY);
        minutoInicio = calInicio.get(Calendar.MINUTE);
        horaFin = calFin.get(Calendar.HOUR_OF_DAY);
        minutoFin = calFin.get(Calendar.MINUTE);

        // Actualizar UI
        actualizarTextoHoras();
        actualizarTextoFechas();
        aplicarFiltroYActualizar(viewModel.getSensorList().getValue());
        actualizarTextoRangoSeleccionado();

        // Resaltar el boton presionado
        resetTimeRangeButtonsSelection();
        switch(horas) {
            case 3: btnUltimas3Horas.setSelected(true); break;
            case 6: btnUltimas6Horas.setSelected(true); break;
            case 12: btnUltimas12Horas.setSelected(true); break;
            case 24: btnUltimas24Horas.setSelected(true); break;
        }

        // Activar el switch de filtro de horas
        switchFiltroHoras.setChecked(true);
    }

    private void resetTimeRangeButtonsSelection() {
        btnUltimas3Horas.setSelected(false);
        btnUltimas6Horas.setSelected(false);
        btnUltimas12Horas.setSelected(false);
        btnUltimas24Horas.setSelected(false);
    }

    private void actualizarTextoFechas() {
        if (fechaInicio != null) {
            btnGraficaFechaInicio.setText("📅 " + dateFormat.format(fechaInicio));
        }
        if (fechaFin != null) {
            btnGraficaFechaFin.setText("📅 " + dateFormat.format(fechaFin));
        }
    }

    private void actualizarTextoHoras() {
        btnGraficaHoraInicio.setText("🕐 " + String.format(Locale.getDefault(), "%02d:%02d", horaInicio, minutoInicio));
        btnGraficaHoraFin.setText("🕐 " + String.format(Locale.getDefault(), "%02d:%02d", horaFin, minutoFin));
    }

    private void actualizarTextoRangoSeleccionado() {
        if (fechaInicio == null || fechaFin == null) {
            tvRangoSeleccionado.setText("Rango: No seleccionado");
            return;
        }

        String rango;
        if (dateFormat.format(fechaInicio).equals(dateFormat.format(fechaFin))) {
            // Mismo dia - mostrar horas si el filtro de horas esta activado
            rango = dateFormat.format(fechaInicio);
            if(switchFiltroHoras.isChecked()) {
                rango += " " + String.format(Locale.getDefault(), "%02d:%02d", horaInicio, minutoInicio) +
                        " - " + String.format(Locale.getDefault(), "%02d:%02d", horaFin, minutoFin);
            }
        } else {
            // Diferentes dias - mostrar fechas completas
            rango = dateFormat.format(fechaInicio);
            if(switchFiltroHoras.isChecked()) {
                rango += " " + String.format(Locale.getDefault(), "%02d:%02d", horaInicio, minutoInicio);
            }
            rango += " - " + dateFormat.format(fechaFin);
            if(switchFiltroHoras.isChecked()) {
                rango += " " + String.format(Locale.getDefault(), "%02d:%02d", horaFin, minutoFin);
            }
        }
        tvRangoSeleccionado.setText("Rango: " + rango);
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
            Log.d("FILTRO", "No hay datos disponibles");
            return;
        }

        // Depuración de fechas
        Log.d("FILTRO", "Filtrando entre: " + dateFormat.format(fechaInicio) +
                " " + horaInicio + ":" + minutoInicio + " y " +
                dateFormat.format(fechaFin) + " " + horaFin + ":" + minutoFin);

        if (fechaInicio == null || fechaFin == null) {
            datosFiltrados = lista;
        } else {
            datosFiltrados = lista.stream()
                    .filter(s -> {
                        if (s == null) return false;

                        Date fechaSensor = new Date(s.getFecha());
                        Log.d("FILTRO", "Procesando dato con fecha: " + dateTimeFormat.format(fechaSensor));

                        Calendar calSensor = Calendar.getInstance();
                        calSensor.setTime(fechaSensor);

                        Calendar calInicio = Calendar.getInstance();
                        calInicio.setTime(fechaInicio);
                        calInicio.set(Calendar.HOUR_OF_DAY, horaInicio);
                        calInicio.set(Calendar.MINUTE, minutoInicio);

                        Calendar calFin = Calendar.getInstance();
                        calFin.setTime(fechaFin);
                        calFin.set(Calendar.HOUR_OF_DAY, horaFin);
                        calFin.set(Calendar.MINUTE, minutoFin);

                        boolean dentroRango = !fechaSensor.before(calInicio.getTime()) &&
                                !fechaSensor.after(calFin.getTime());

                        Log.d("FILTRO", "Dato " + s.getId() + " dentro de rango: " + dentroRango);
                        return dentroRango;
                    })
                    .collect(Collectors.toList());
        }

        Log.d("FILTRO", "Datos encontrados: " + (datosFiltrados != null ? datosFiltrados.size() : 0));
        actualizarContadorRegistros(datosFiltrados != null ? datosFiltrados.size() : 0);
    }

    private void generarGrafica() {
        // Crear datasets para cada variable seleccionada
        List<ILineDataSet> dataSets = new ArrayList<>();
        List<String> etiquetasTiempo = crearEtiquetasTiempo();

        int colorIndex = 0;
        float lineWidth = 2f;
        float circleRadius = 3f;

        // Contar sensores seleccionados para optimizar nombres
        int sensoresSeleccionados = contarSensoresSeleccionados();
        boolean usarNombresCortos = sensoresSeleccionados > 4;

        if (cbTemperatura.isChecked()) {
            String nombre = usarNombresCortos ? "Temp" : "Temperatura (°C)";
            LineDataSet dataSet = crearDataSetMejorado(nombre, extraerDatosTemperatura(),
                    coloresLineas[colorIndex % coloresLineas.length], lineWidth, circleRadius);
            dataSets.add(dataSet);
            colorIndex++;
        }

        if (cbHumedad.isChecked()) {
            String nombre = usarNombresCortos ? "Hum" : "Humedad (%)";
            LineDataSet dataSet = crearDataSetMejorado(nombre, extraerDatosHumedad(),
                    coloresLineas[colorIndex % coloresLineas.length], lineWidth, circleRadius);
            dataSets.add(dataSet);
            colorIndex++;
        }

        if (cbPresion.isChecked()) {
            String nombre = usarNombresCortos ? "Pres" : "Presion (hPa)";
            LineDataSet dataSet = crearDataSetMejorado(nombre, extraerDatosPresion(),
                    coloresLineas[colorIndex % coloresLineas.length], lineWidth, circleRadius);
            dataSets.add(dataSet);
            colorIndex++;
        }

        if (cbHumedadSuelo.isChecked()) {
            String nombre = usarNombresCortos ? "H.Suelo" : "Humedad Suelo (%)";
            LineDataSet dataSet = crearDataSetMejorado(nombre, extraerDatosHumedadSuelo(),
                    coloresLineas[colorIndex % coloresLineas.length], lineWidth, circleRadius);
            dataSets.add(dataSet);
            colorIndex++;
        }

        if (cbLuz.isChecked()) {
            String nombre = usarNombresCortos ? "Luz" : "Luz (lux)";
            LineDataSet dataSet = crearDataSetMejorado(nombre, extraerDatosLuz(),
                    coloresLineas[colorIndex % coloresLineas.length], lineWidth, circleRadius);
            dataSets.add(dataSet);
            colorIndex++;
        }

        if (cbViento.isChecked()) {
            String nombre = usarNombresCortos ? "Viento" : "Viento (km/h)";
            LineDataSet dataSet = crearDataSetMejorado(nombre, extraerDatosViento(),
                    coloresLineas[colorIndex % coloresLineas.length], lineWidth, circleRadius);
            dataSets.add(dataSet);
            colorIndex++;
        }

        if (cbHumo.isChecked()) {
            boolean hasHumoData = datosFiltrados.stream().anyMatch(s -> s.getHumo() != null && s.getHumo() > 0);
            if (!hasHumoData) {
                Toast.makeText(getContext(), "No hay datos validos de humo en el rango seleccionado", Toast.LENGTH_SHORT).show();
                cbHumo.setChecked(false);
            } else {
                String nombre = "Humo";
                LineDataSet dataSet = crearDataSetMejorado(nombre, extraerDatosHumo(),
                        coloresLineas[colorIndex % coloresLineas.length], lineWidth, circleRadius);
                dataSets.add(dataSet);
                colorIndex++;
            }
        }

        if (cbGas.isChecked()) {
            String nombre = "Gas";
            LineDataSet dataSet = crearDataSetMejorado(nombre, extraerDatosGas(),
                    coloresLineas[colorIndex % coloresLineas.length], lineWidth, circleRadius);
            dataSets.add(dataSet);
            colorIndex++;
        }

        if (dataSets.isEmpty()) {
            mostrarNoData(true);
            return;
        }

        // Configurar datos en el grafico
        LineData lineData = new LineData(dataSets);
        lineData.setValueTextSize(0f); // Desactivar texto de valores
        lineChart.setData(lineData);

        // Configurar etiquetas del eje X
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(etiquetasTiempo));
        xAxis.setLabelCount(Math.min(etiquetasTiempo.size(), 8)); // Reducir etiquetas en X

        // Ajustar vista segun nmero de sensores
        ajustarVistaSegunSensores(dataSets.size());

        // Actualizar grafico
        lineChart.invalidate();
        lineChart.animateY(800); // Animacion mas rapida

        // Mostrar grafico
        mostrarNoData(false);

        // Generar estadisticas
        generarEstadisticas();

        Toast.makeText(getContext(), "Grafica generada exitosamente", Toast.LENGTH_SHORT).show();
    }

    private LineDataSet crearDataSetMejorado(String label, List<Entry> entries, int color,
                                             float lineWidth, float circleRadius) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(lineWidth);
        dataSet.setCircleRadius(circleRadius);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(0f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.LINEAR);
        dataSet.setCubicIntensity(0.2f);
        dataSet.setDrawFilled(false);
        dataSet.setHighlightEnabled(true);
        dataSet.setHighLightColor(Color.rgb(244, 117, 117));

        // Mejorar la visibilidad de las lineas
        dataSet.setDrawCircles(entries.size() < 50);
        return dataSet;
    }

    private void ajustarVistaSegunSensores(int numSensores) {
        Legend legend = lineChart.getLegend();

        // Ajustar grosor de líneas segun numero de sensores
        float lineWidth = numSensores > 6 ? 1.5f : 2f;
        float circleRadius = numSensores > 6 ? 2f : 3f;

        for (ILineDataSet set : lineChart.getData().getDataSets()) {
            if (set instanceof LineDataSet) {
                ((LineDataSet) set).setLineWidth(lineWidth);
                ((LineDataSet) set).setCircleRadius(circleRadius);
            }
        }

        // Configurar leyenda segun numero de sensores
        if (numSensores <= 2) {
            // Pocos sensores: layout normal
            legend.setTextSize(12f);
            legend.setFormSize(10f);
            legend.setXEntrySpace(15f);
            legend.setYEntrySpace(5f);
            lineChart.setExtraBottomOffset(30f);

        } else if (numSensores <= 4) {
            // Sensores medianos: layout compacto
            legend.setTextSize(11f);
            legend.setFormSize(9f);
            legend.setXEntrySpace(12f);
            legend.setYEntrySpace(3f);
            lineChart.setExtraBottomOffset(35f);

        } else if (numSensores <= 6) {
            // Muchos sensores: layout muy compacto
            legend.setTextSize(10f);
            legend.setFormSize(8f);
            legend.setXEntrySpace(8f);
            legend.setYEntrySpace(2f);
            lineChart.setExtraBottomOffset(40f);

        } else {
            // Demasiados sensores: layout ultra compacto con abreviaciones
            legend.setTextSize(9f);
            legend.setFormSize(7f);
            legend.setXEntrySpace(6f);
            legend.setYEntrySpace(1f);
            lineChart.setExtraBottomOffset(45f);

            // Usar nombres abreviados para ahorrar espacio
            usarNombresAbreviados();
        }

        // Configurar margenes adicionales si hay muchos sensores
        if (numSensores > 4) {
            // Reducir el tamaño de la descripción
            Description desc = lineChart.getDescription();
            desc.setTextSize(8f);
            desc.setText(""); // Ocultar descripcion para ahorrar espacio
        }
    }

    private void usarNombresAbreviados() {
        // Cambiar nombres largos por abreviaciones
        List<ILineDataSet> dataSets = lineChart.getData().getDataSets();

        for (ILineDataSet dataSet : dataSets) {
            String label = dataSet.getLabel();
            String labelAbreviado = abreviarNombreSensor(label);
            dataSet.setLabel(labelAbreviado);
        }
    }

    private String abreviarNombreSensor(String nombreCompleto) {
        // Mapa de abreviaciones para nombres largos
        switch (nombreCompleto) {
            case "Temperatura (°C)":
                return "Temp";
            case "Humedad (%)":
                return "Hum";
            case "Presion (hPa)":
                return "Pres";
            case "Humedad Suelo (%)":
                return "H.Suelo";
            case "Luz (lux)":
                return "Luz";
            case "Viento (km/h)":
                return "Viento";
            case "Humo":
                return "Humo";
            case "Gas":
                return "Gas";
            default:
                return nombreCompleto;
        }
    }
    private int contarSensoresSeleccionados() {
        int count = 0;
        if (cbTemperatura.isChecked()) count++;
        if (cbHumedad.isChecked()) count++;
        if (cbPresion.isChecked()) count++;
        if (cbHumedadSuelo.isChecked()) count++;
        if (cbLuz.isChecked()) count++;
        if (cbViento.isChecked()) count++;
        if (cbHumo.isChecked()) count++;
        if (cbGas.isChecked()) count++;
        return count;
    }
    private List<String> crearEtiquetasTiempo() {
        List<String> etiquetas = new ArrayList<>();

        if (fechaInicio == null || fechaFin == null || datosFiltrados == null || datosFiltrados.isEmpty()) {
            Log.d("ETIQUETAS", "No hay datos para generar etiquetas");
            return etiquetas;
        }

        try {
            long diffMillis = fechaFin.getTime() - fechaInicio.getTime();
            long diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis);
            long diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis);

            Log.d("ETIQUETAS", "Diferencia en dias: " + diffDays + ", horas: " + diffHours);

            for (Sensor sensor : datosFiltrados) {
                if(sensor == null) {
                    Log.d("ETIQUETAS", "Sensor o fecha nula, omitiendo");
                    continue;
                }

                Date fecha = new Date(sensor.getFecha());
                if (diffHours <= 24) {
                    // Menos de 24 horas - mostrar hora:minuto
                    etiquetas.add(timeFormat.format(fecha));
                } else if (diffDays <= 3) {
                    // 1-3 dias - mostrar dia y hora
                    etiquetas.add(dateTimeFormat.format(fecha));
                } else {
                    // Más de 3 dias - mostrar solo fecha
                    etiquetas.add(dateFormat.format(fecha));
                }
            }
        } catch (Exception e) {
            Log.e("ETIQUETAS", "Error generando etiquetas: " + e.getMessage());
        }

        Log.d("ETIQUETAS", "Etiquetas generadas: " + etiquetas.size());
        return etiquetas;
    }

    // Metodos para extraer datos de cada sensor
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


    private List<Entry> extraerDatosHumo() {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < datosFiltrados.size(); i++) {
            Sensor sensor = datosFiltrados.get(i);
            if (sensor != null) {
                Double humo = sensor.getHumo();
                if (humo != null && humo >= 0) { // Asegurar que no es negativo
                    entries.add(new Entry(i, humo.floatValue()));
                }
            }
        }
        return entries;
    }

    private List<Entry> extraerDatosGas() {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < datosFiltrados.size(); i++) {
            Double gas = datosFiltrados.get(i).getGas();
            if (gas != null) {
                entries.add(new Entry(i, gas.floatValue()));
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

        // Calcular estadisticas para cada variable seleccionada
        if (cbTemperatura.isChecked()) {
            calcularEstadisticasVariable(estadisticas, "Temperatura", "°C",
                    datosFiltrados.stream().map(Sensor::getTemperatura).collect(Collectors.toList()));
        }

        if (cbHumedad.isChecked()) {
            calcularEstadisticasVariable(estadisticas, "Humedad", "%",
                    datosFiltrados.stream().map(Sensor::getHumedad).collect(Collectors.toList()));
        }

        if (cbPresion.isChecked()) {
            calcularEstadisticasVariable(estadisticas, "Presion", "hPa",
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


        if (cbHumo.isChecked()) {
            calcularEstadisticasVariable(estadisticas, "Humo", "",
                    datosFiltrados.stream().map(Sensor::getHumo).collect(Collectors.toList()));
        }

        if (cbGas.isChecked()) {
            calcularEstadisticasVariable(estadisticas, "Gas", "",
                    datosFiltrados.stream().map(Sensor::getGas).collect(Collectors.toList()));
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
            tvEmptyChart.setText("No hay datos para mostrar con los filtros actuales");
        } else {
            cardGrafica.setVisibility(View.VISIBLE);
            layoutNoDataGrafica.setVisibility(View.GONE);
        }
    }

    private void actualizarContadorRegistros(int cantidad) {
        if (tvContadorDatosGrafica != null) {
            String texto;
            if (cantidad == 0) {
                texto = "No hay registros para graficar con los filtros actuales";
                btnGenerarGrafica.setEnabled(false);
            } else {
                texto = cantidad + " registros disponibles para graficar";
                btnGenerarGrafica.setEnabled(true);
            }
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
    private void limpiarGrafica() {
        if (lineChart != null) {
            lineChart.clear();
            lineChart.invalidate();
        }
        mostrarNoData(true);
    }
}