package com.example.sensorreaders;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.example.sensorreaders.Models.Sensor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import java.util.Collections;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;


public class PDFGenerator {

    private final Context context;
    private static final String PDF_DIRECTORY = "ReportesSensores";

    public PDFGenerator(Context context) {
        this.context = context;
    }

    public void generateFromLiveData(LiveData<List<Sensor>> sensorLiveData, String fileName, LifecycleOwner lifecycleOwner) {
        sensorLiveData.observe(lifecycleOwner, sensorList -> {
            if (sensorList != null && !sensorList.isEmpty()) {
                generateSensorReport(sensorList, fileName);
                sensorLiveData.removeObservers(lifecycleOwner);
            }
        });
    }

    public boolean generateSensorReport(List<Sensor> sensorList, String fileName) {
        File dir = new File(context.getExternalFilesDir(null), "ReportesSensores");
        if (!dir.exists() && !dir.mkdirs()) {
            Toast.makeText(context, "No se pudo crear el directorio", Toast.LENGTH_SHORT).show();
            return false;
        }

        File file = new File(dir, fileName + ".pdf");

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);

            document.open();
            addTitle(document);
            addMetadata(document);
            addSensorDataTable(document, sensorList);

            addAllChartsToDocument(document, sensorList);

            document.close();

            Toast.makeText(context, "PDF generado en:\n" + file.getPath(), Toast.LENGTH_LONG).show();
            return true;

        } catch (IOException | DocumentException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al generar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PDFGenerator", "generateSensorReport: " + e.getMessage());
            return false;
        }
    }


    private Bitmap generateChart(List<Sensor> sensorList) {
        try {
            // Crear un contexto limpio para el gráfico
            Context chartContext = context.getApplicationContext();

            // Crear el LineChart con un ID único para evitar conflictos
            LineChart lineChart = new LineChart(chartContext);
            lineChart.setId(View.generateViewId()); // ID único

            // Configurar dimensiones fijas
            int chartWidth = 800;
            int chartHeight = 600;
            lineChart.setLayoutParams(new ViewGroup.LayoutParams(chartWidth, chartHeight));

            // Preparar datos
            List<Entry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

            int index = 0;
            for (Sensor sensor : sensorList) {
                if (sensor.getFecha() > 0 && sensor.getTemperatura() != null) {
                    entries.add(new Entry(index, sensor.getTemperatura().floatValue()));
                    labels.add(sdf.format(new Date(sensor.getFecha())));
                    index++;
                }
            }

            // Verificar que hay datos
            if (entries.isEmpty()) {
                Log.w("PDFGenerator", "No hay datos de temperatura válidos para el gráfico");
                return null;
            }

            // Configurar el dataset
            LineDataSet dataSet = new LineDataSet(entries, "Temperatura (°C)");
            dataSet.setColor(Color.BLUE);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(3f);
            dataSet.setDrawCircles(true);
            dataSet.setDrawValues(false);
            dataSet.setMode(LineDataSet.Mode.LINEAR);

            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);

            // Configurar apariencia del gráfico
            lineChart.getDescription().setText("Temperatura");
            lineChart.getDescription().setTextSize(12f);
            lineChart.setBackgroundColor(Color.WHITE);

            // Configurar eje X
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setLabelCount(Math.min(labels.size(), 8), false); // Máximo 8 etiquetas
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int i = (int) value;
                    return i >= 0 && i < labels.size() ? labels.get(i) : "";
                }
            });
            xAxis.setTextSize(10f);

            // Configurar eje Y izquierdo
            YAxis leftAxis = lineChart.getAxisLeft();
            leftAxis.setTextSize(10f);
            leftAxis.setGranularity(1f);

            // Deshabilitar eje Y derecho
            lineChart.getAxisRight().setEnabled(false);

            // Configurar leyenda
            Legend legend = lineChart.getLegend();
            legend.setTextSize(12f);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

            // Deshabilitar interacciones
            lineChart.setTouchEnabled(false);
            lineChart.setDragEnabled(false);
            lineChart.setScaleEnabled(false);
            lineChart.setPinchZoom(false);
            lineChart.setDoubleTapToZoomEnabled(false);

            // Forzar el layout del gráfico
            lineChart.measure(
                    View.MeasureSpec.makeMeasureSpec(chartWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(chartHeight, View.MeasureSpec.EXACTLY)
            );
            lineChart.layout(0, 0, chartWidth, chartHeight);

            // Invalidar y notificar cambios de datos
            lineChart.notifyDataSetChanged();
            lineChart.invalidate();

            // Esperar un momento para que el layout se complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Generar el bitmap
            Bitmap bitmap = Bitmap.createBitmap(chartWidth, chartHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE); // Fondo blanco
            lineChart.draw(canvas);

            Log.d("PDFGenerator", "Gráfico generado exitosamente con " + entries.size() + " puntos de datos");
            return bitmap;

        } catch (Exception e) {
            Log.e("PDFGenerator", "Error al generar gráfico: " + e.getMessage(), e);
            return null;
        }
    }
    private Bitmap generateSensorChart(List<Sensor> sensorList, String sensorType, String chartTitle) {
        try {
            // Crear bitmap directamente
            int width = 800;
            int height = 600;
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            // Dibujar fondo blanco
            canvas.drawColor(Color.WHITE);

            // Configurar paint para dibujar
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(3f);

            // Preparar datos según el tipo de sensor
            List<Float> sensorValues = new ArrayList<>();
            List<String> timeLabels = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String unit = getUnitForSensor(sensorType);
            int chartColor = getColorForSensor(sensorType);

            for (Sensor sensor : sensorList) {
                if (sensor.getFecha() > 0) {
                    Double value = getSensorValue(sensor, sensorType);
                    if (value != null) {
                        sensorValues.add(value.floatValue());
                        timeLabels.add(sdf.format(new Date(sensor.getFecha())));
                    }
                }
            }

            if (sensorValues.isEmpty()) {
                Log.w("PDFGenerator", "No hay datos válidos para el sensor: " + sensorType);
                return null;
            }

            // Calcular dimensiones del área de dibujo
            int margin = 80;
            int chartWidth = width - (2 * margin);
            int chartHeight = height - (2 * margin);

            // Encontrar valores min y max para escalar
            float minValue = Collections.min(sensorValues);
            float maxValue = Collections.max(sensorValues);
            float valueRange = maxValue - minValue;
            if (valueRange == 0) valueRange = 1; // Evitar división por cero

            // Dibujar título
            paint.setTextSize(18f);
            paint.setColor(Color.BLACK);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(chartTitle, width / 2f, 40f, paint);

            // Dibujar ejes
            paint.setColor(Color.GRAY);
            paint.setStrokeWidth(2f);
            // Eje Y (vertical)
            canvas.drawLine(margin, margin, margin, height - margin, paint);
            // Eje X (horizontal)
            canvas.drawLine(margin, height - margin, width - margin, height - margin, paint);

            // Dibujar línea de datos
            paint.setColor(chartColor);
            paint.setStrokeWidth(3f);

            if (sensorValues.size() > 1) {
                Path path = new Path();
                boolean firstPoint = true;

                for (int i = 0; i < sensorValues.size(); i++) {
                    float x = margin + (i * chartWidth / (float)(sensorValues.size() - 1));
                    float y = height - margin - ((sensorValues.get(i) - minValue) / valueRange * chartHeight);

                    if (firstPoint) {
                        path.moveTo(x, y);
                        firstPoint = false;
                    } else {
                        path.lineTo(x, y);
                    }

                    // Dibujar punto
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(x, y, 4f, paint);
                    paint.setStyle(Paint.Style.STROKE);
                }

                canvas.drawPath(path, paint);
            } else if (sensorValues.size() == 1) {
                // Si solo hay un punto, dibujarlo en el centro
                float x = width / 2f;
                float y = height / 2f;
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(x, y, 6f, paint);
            }

            // Dibujar etiquetas del eje Y (valores del sensor)
            // Eje Y (valores numéricos)
            paint.setTextSize(14f); // antes: 11f
            paint.setColor(Color.BLACK);
            paint.setTextAlign(Paint.Align.RIGHT);
            for (int i = 0; i <= 5; i++) {
                float value = minValue + (valueRange * i / 5f);
                float y = height - margin - (i * chartHeight / 5f);
                canvas.drawText(String.format("%.1f%s", value, unit), margin - 15, y + 5, paint); // más espacio
            }


            paint.setTextSize(14f); // antes: 11f
            paint.setTextAlign(Paint.Align.CENTER);
            int labelStep = Math.max(1, timeLabels.size() / 6); // limita a 6 etiquetas visibles

            for (int i = 0; i < timeLabels.size(); i += labelStep) {
                if (sensorValues.size() > 1) {
                    float x = margin + (i * chartWidth / (float)(sensorValues.size() - 1));
                    canvas.drawText(timeLabels.get(i), x, height - margin + 30, paint); // más abajo
                }
            }


            paint.setTextSize(13f); // más grande
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(String.format("Min: %.2f%s", minValue, unit), 20, height - 60, paint);
            canvas.drawText(String.format("Max: %.2f%s", maxValue, unit), 20, height - 40, paint);
            canvas.drawText(String.format("Puntos: %d", sensorValues.size()), 20, height - 20, paint);


            return bitmap;

        } catch (Exception e) {
            Log.e("PDFGenerator", "Error al generar gráfico para " + sensorType + ": " + e.getMessage(), e);
            return null;
        }
    }

    // Método para obtener el valor del sensor según el tipo
    private Double getSensorValue(Sensor sensor, String sensorType) {
        switch (sensorType.toLowerCase()) {
            case "temperatura":
                return sensor.getTemperatura();
            case "humedad":
                return sensor.getHumedad();
            case "humedadsuelo":
                return sensor.getHumedadSuelo();
            case "gas":
                return sensor.getGas();
            case "presionatmosferica":
                return sensor.getPresionAtmosferica();
            case "luz":
                return sensor.getLuz();
            case "viento":
                return sensor.getViento();
            case "humo":
                return sensor.getHumo();
            case "lluvia":
                return sensor.getLluvia();
            default:
                return null;
        }
    }

    private String getUnitForSensor(String sensorType) {
        switch (sensorType.toLowerCase()) {
            case "temperatura":
                return "°C";
            case "humedad":
                return "%";
            case "humedadsuelo":
                return "";
            case "gas":
                return " ppm";
            case "presionatmosferica":
                return " hPa";
            case "luz":
                return " lux";
            case "viento":
                return " m/s";
            case "humo":
                return "";
            case "lluvia":
                return "";
            default:
                return "";
        }
    }

    private int getColorForSensor(String sensorType) {
        switch (sensorType.toLowerCase()) {
            case "temperatura":
                return Color.RED;
            case "humedad":
                return Color.BLUE;
            case "humedadsuelo":
                return Color.rgb(139, 69, 19); // Brown
            case "gas":
                return Color.rgb(255, 165, 0); // Orange
            case "presionatmosferica":
                return Color.rgb(128, 0, 128); // Purple
            case "luz":
                return Color.YELLOW;
            case "viento":
                return Color.CYAN;
            case "humo":
                return Color.GRAY;
            case "lluvia":
                return Color.rgb(0, 191, 255); // Deep Sky Blue
            default:
                return Color.BLACK;
        }
    }

    private Bitmap generateChartAlternative(List<Sensor> sensorList) {
        try {
            // Crear bitmap directamente
            int width = 800;
            int height = 600;
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            // Dibujar fondo blanco
            canvas.drawColor(Color.WHITE);

            // Configurar paint para dibujar
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(3f);

            // Preparar datos
            List<Float> temperatures = new ArrayList<>();
            List<String> timeLabels = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

            for (Sensor sensor : sensorList) {
                if (sensor.getFecha() > 0 && sensor.getTemperatura() != null) {
                    temperatures.add(sensor.getTemperatura().floatValue());
                    timeLabels.add(sdf.format(new Date(sensor.getFecha())));
                }
            }

            if (temperatures.isEmpty()) {
                return null;
            }

            // Calcular dimensiones del área de dibujo
            int margin = 80;
            int chartWidth = width - (2 * margin);
            int chartHeight = height - (2 * margin);

            // Encontrar valores min y max para escalar
            float minTemp = Collections.min(temperatures);
            float maxTemp = Collections.max(temperatures);
            float tempRange = maxTemp - minTemp;
            if (tempRange == 0) tempRange = 1; // Evitar división por cero

            // Dibujar título
            paint.setTextSize(20f);
            paint.setColor(Color.BLACK);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Temperatura", width / 2f, 40f, paint);

            // Dibujar ejes
            paint.setColor(Color.GRAY);
            paint.setStrokeWidth(2f);
            // Eje Y (vertical)
            canvas.drawLine(margin, margin, margin, height - margin, paint);
            // Eje X (horizontal)
            canvas.drawLine(margin, height - margin, width - margin, height - margin, paint);

            // Dibujar línea de datos
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(3f);

            if (temperatures.size() > 1) {
                Path path = new Path();
                boolean firstPoint = true;

                for (int i = 0; i < temperatures.size(); i++) {
                    float x = margin + (i * chartWidth / (float)(temperatures.size() - 1));
                    float y = height - margin - ((temperatures.get(i) - minTemp) / tempRange * chartHeight);

                    if (firstPoint) {
                        path.moveTo(x, y);
                        firstPoint = false;
                    } else {
                        path.lineTo(x, y);
                    }

                    // Dibujar punto
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(x, y, 4f, paint);
                    paint.setStyle(Paint.Style.STROKE);
                }

                canvas.drawPath(path, paint);
            }

            // Dibujar etiquetas del eje Y (temperaturas)
            paint.setTextSize(12f);
            paint.setColor(Color.BLACK);
            paint.setTextAlign(Paint.Align.RIGHT);
            for (int i = 0; i <= 5; i++) {
                float temp = minTemp + (tempRange * i / 5f);
                float y = height - margin - (i * chartHeight / 5f);
                canvas.drawText(String.format("%.1f°C", temp), margin - 10, y + 5, paint);
            }

            // Dibujar algunas etiquetas del eje X (tiempo)
            paint.setTextAlign(Paint.Align.CENTER);
            int labelStep = Math.max(1, timeLabels.size() / 5);
            for (int i = 0; i < timeLabels.size(); i += labelStep) {
                float x = margin + (i * chartWidth / (float)(temperatures.size() - 1));
                canvas.drawText(timeLabels.get(i), x, height - margin + 20, paint);
            }

            return bitmap;

        } catch (Exception e) {
            Log.e("PDFGenerator", "Error en método alternativo: " + e.getMessage(), e);
            return null;
        }
    }

    private void addAllChartsToDocument(Document document, List<Sensor> sensorList) throws IOException, DocumentException {
        // Agregar nueva página para los gráficos
        document.newPage();

        // Título de la sección de gráficos
        Paragraph sectionTitle = new Paragraph("\nGráficos de Monitoreo de Sensores\n\n");
        sectionTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(sectionTitle);

        // Generar y agregar cada gráfico
        String[] sensorTypes = {"temperatura", "humedad", "humedadSuelo", "gas", "presionAtmosferica", "luz", "viento"};
        String[] chartTitles = {
                "Temperatura (°C)",
                "Humedad (%)",
                "Humedad del Suelo",
                "Niveles de Gas",
                "Presión Atmosférica (hPa)",
                "Niveles de Luz",
                "Velocidad del Viento"
        };

        for (int i = 0; i < sensorTypes.length; i++) {
            Bitmap chartBitmap = generateSensorChart(sensorList, sensorTypes[i], chartTitles[i]);
            if (chartBitmap != null) {
                addChartToDocument(document, chartBitmap, chartTitles[i]);
                // Agregar espacio entre gráficos
                document.add(new Paragraph("\n"));
            }
        }
    }

    // Método mejorado para agregar un gráfico específico al documento
    private void addChartToDocument(Document document, Bitmap chartBitmap, String title) throws IOException, DocumentException {
        if (chartBitmap == null) {
            Log.w("PDFGenerator", "No se pudo generar el gráfico: " + title);
            return;
        }

        try {
            // Comprimir bitmap a PNG
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            chartBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            // Crear imagen para el PDF
            Image chartImage = Image.getInstance(stream.toByteArray());

            // Escalar imagen para que quepa bien en el PDF
            chartImage.scaleToFit(500, 300);
            chartImage.setAlignment(Element.ALIGN_CENTER);

            // Agregar título del gráfico
            Paragraph chartTitle = new Paragraph(title);
            chartTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(chartTitle);

            // Agregar la imagen
            document.add(chartImage);

            // Cerrar el stream
            stream.close();

            Log.d("PDFGenerator", "Gráfico agregado exitosamente: " + title);

        } catch (Exception e) {
            Log.e("PDFGenerator", "Error al agregar gráfico al documento: " + e.getMessage(), e);
            throw e;
        }
    }

    private void addTitle(Document document) throws DocumentException {
        Paragraph title = new Paragraph("Reporte de Datos de Sensores");
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subTitle = new Paragraph("\nDatos recopilados del sistema de monitoreo\n\n");
        subTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subTitle);
    }

    private void addMetadata(Document document) throws DocumentException {
        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        Paragraph metadata = new Paragraph("Generado el: " + currentDate + "\n\n");
        document.add(metadata);
    }

    private void addSensorDataTable(Document document, List<Sensor> sensorList) throws DocumentException {
        PdfPTable table = new PdfPTable(11);
        float[] columnWidths = {5f, 8f, 8f, 10f, 8f, 8f, 8f, 12f, 10f, 8f, 12f};
        table.setWidths(columnWidths);
        table.setWidthPercentage(100);

        addTableHeader(table);
        for (Sensor sensor : sensorList) {
            addSensorRow(table, sensor);
        }

        document.add(table);
    }


    private void addTableHeader(PdfPTable table) {
        String[] headers = { "ID", "Gas", "Humedad", "Humedad Suelo", "Humo", "Lluvia", "Luz", "Presión Atmosf.", "Temperatura", "Viento", "Fecha/Hora" };
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        BaseColor headerColor = BaseColor.DARK_GRAY;

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
            cell.setBackgroundColor(headerColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    /*private void addChartToDocument(Document document, Bitmap chartBitmap) throws IOException, DocumentException {
        if (chartBitmap == null) {
            Log.w("PDFGenerator", "No se pudo generar el gráfico, omitiendo...");
            return;
        }

        try {
            // Agregar espacio antes del gráfico
            document.add(new Paragraph("\n"));

            // Comprimir bitmap a PNG
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            chartBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            // Crear imagen para el PDF
            Image chartImage = Image.getInstance(stream.toByteArray());

            // Escalar imagen para que quepa bien en el PDF
            chartImage.scaleToFit(500, 300);
            chartImage.setAlignment(Element.ALIGN_CENTER);

            // Agregar título del gráfico
            Paragraph chartTitle = new Paragraph("Gráfico de Temperaturas:");
            chartTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(chartTitle);

            // Agregar la imagen
            document.add(chartImage);

            // Cerrar el stream
            stream.close();

            Log.d("PDFGenerator", "Gráfico agregado exitosamente al PDF");

        } catch (Exception e) {
            Log.e("PDFGenerator", "Error al agregar gráfico al documento: " + e.getMessage(), e);
            throw e;
        }
    }*/



    private void addSensorRow(PdfPTable table, Sensor sensor) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        /* aqui sin intepretar
        table.addCell(String.valueOf(sensor.getId()));
        table.addCell(formatDouble(sensor.getGas()));
        table.addCell(formatDouble(sensor.getHumedad()));
        table.addCell(formatDouble(sensor.getHumedadSuelo()));
        table.addCell(formatDouble(sensor.getHumo()));
        table.addCell(formatDouble(sensor.getLluvia()));
        table.addCell(formatDouble(sensor.getLuz()));
        table.addCell(formatDouble(sensor.getPresionAtmosferica()));
        table.addCell(formatDouble(sensor.getTemperatura()));
        table.addCell(formatDouble(sensor.getViento()));
        //table.addCell(sensor.getFecha() != null ? dateFormat.format(sensor.getFecha()) : "N/A");
        */
        //Intepretado para guarda en el pdf
        table.addCell(String.valueOf(sensor.getId()));
        table.addCell(interpretarGas(sensor.getGas()));
        table.addCell(interpretarHumedad(sensor.getHumedad()));
        table.addCell(convertirHumedadSuelo(sensor.getHumedadSuelo()));
        table.addCell(interpretarHumo(sensor.getHumo()));
        table.addCell(sensor.getLluvia() != null && sensor.getLluvia() == 1 ? "Sí" : "No");
        table.addCell(convertirLuz(sensor.getLuz()));
        table.addCell(interpretarPresion(sensor.getPresionAtmosferica()));
        table.addCell(formatDouble(sensor.getTemperatura()));
        table.addCell(interpretarViento(sensor.getViento()));
        table.addCell(sensor.getFecha() > 0 ? dateFormat.format(new Date(sensor.getFecha())) : "N/A");

    }
    private String interpretarHumedad(Double valor) {
        if (valor == null) return "N/A";
        if (valor < 30) return "Muy seca";
        else if (valor < 60) return "Seca";
        else if (valor < 80) return "Húmeda";
        else return "Muy húmeda";
    }
    private String interpretarHumo(Double valor) {
        if (valor == null) return "N/A";
        if (valor < 100) return "Bajo";
        else if (valor <= 200) return "Moderado";
        else return "Alto";
    }
    private String interpretarPresion(Double valor) {
        if (valor == null) return "N/A";
        if (valor < 1000) return "Baja presión";
        else if (valor <= 1020) return "Presión normal";
        else return "Alta presión";
    }

    private String interpretarViento(Double valor) {
        if (valor == null) return "N/A";
        if (valor == 0) return "Sin viento";
        else if (valor < 10) return "Brisa leve";
        else if (valor < 30) return "Viento moderado";
        else return "Viento fuerte";
    }

    private String convertirHumedadSuelo(Double valor) {
        if (valor == null) return "N/A";
        if (valor < 300) return "Seco";
        else if (valor <= 700) return "Húmedo";
        else return "Encharcado";
    }

    private String convertirLuz(Double valor) {
        if (valor == null) return "N/A";
        if (valor < 300) return "Oscuro";
        else if (valor <= 700) return "Normal";
        else return "Brillante";
    }

    private String interpretarGas(Double valor) {
        if (valor == null) return "N/A";
        if (valor < 100) return "Bajo";
        else if (valor <= 300) return "Moderado";
        else return "Alto";
    }

    private String formatDouble(Double value) {
        return value != null ? String.format(Locale.getDefault(), "%.2f", value) : "N/A";
    }

    //Generate el excel
    public boolean generateSensorExcelReport(List<Sensor> sensorList, String fileName) {
        File dir = new File(context.getExternalFilesDir(null), "ReportesSensores");

        if (!dir.exists() && !dir.mkdirs()) {
            Log.e("PDFGenerator", "No se pudo crear el directorio: " + dir.getAbsolutePath());
            Toast.makeText(context, "No se pudo crear el directorio", Toast.LENGTH_SHORT).show();
            return false;
        }

        File file = new File(dir, fileName + ".xlsx");

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            Workbook workbook = new XSSFWorkbook(); // Usamos formato moderno .xlsx
            Sheet sheet = workbook.createSheet("Reporte Sensores");

            // Crear encabezado
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "ID", "Gas", "Humedad", "Humedad Suelo", "Humo",
                    "Lluvia", "Luz", "Presión Atmosférica", "Temperatura", "Viento", "Fecha/Hora"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Llenar datos
            for (int i = 0; i < sensorList.size(); i++) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Sensor sensor = sensorList.get(i);
                Row row = sheet.createRow(i + 1);

                row.createCell(0).setCellValue(sensor.getId());
                row.createCell(1).setCellValue(getSafeDouble(sensor.getGas()));
                row.createCell(2).setCellValue(getSafeDouble(sensor.getHumedad()));
                row.createCell(3).setCellValue(getSafeDouble(sensor.getHumedadSuelo()));
                row.createCell(4).setCellValue(getSafeDouble(sensor.getHumo()));
                row.createCell(5).setCellValue(getSafeDouble(sensor.getLluvia()));
                row.createCell(6).setCellValue(getSafeDouble(sensor.getLuz()));
                row.createCell(7).setCellValue(getSafeDouble(sensor.getPresionAtmosferica()));
                row.createCell(8).setCellValue(getSafeDouble(sensor.getTemperatura()));
                row.createCell(9).setCellValue(getSafeDouble(sensor.getViento()));
                row.createCell(10).setCellValue(
                        sensor.getFecha() > 0 ? dateFormat.format(new Date(sensor.getFecha())) : "N/A"
                );
            }



            workbook.write(outputStream);
            workbook.close();

            Toast.makeText(context, "Excel generado en: " + file.getPath(), Toast.LENGTH_LONG).show();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al generar Excel: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PDFGenerator", "generateSensorExcelReport: "+e.getMessage());
            return false;
        }
    }
    private double getSafeDouble(Double value) {
        return value != null ? value : 0.0;
    }
    /**
     * Genera un archivo Excel cuando los datos del LiveData estén disponibles
     * @param sensorLiveData LiveData con la lista de sensores
     * @param fileName Nombre del archivo sin extensión
     * @param lifecycleOwner Activity o Fragment que posee el ciclo de vida
     */
    public void generateExcelFromLiveData(LiveData<List<Sensor>> sensorLiveData, String fileName, LifecycleOwner lifecycleOwner) {
        sensorLiveData.observe(lifecycleOwner, sensorList -> {
            if (sensorList != null && !sensorList.isEmpty()) {
                generateSensorExcelReport(sensorList, fileName);
                // Detener la observación para evitar múltiples ejecuciones
                sensorLiveData.removeObservers(lifecycleOwner);
            }
        });
    }

    public void generateFromList(List<Sensor> sensorList, String fileName, Context context) {
        generateSensorReport(sensorList, fileName);
    }

    public void generateExcelFromList(List<Sensor> sensorList, String fileName, Context context) {
        generateSensorExcelReport(sensorList, fileName);
    }


    /**
     * Genera un archivo Excel y Pdf cuando los datos del LiveData estén disponibles
     * @param sensorLiveData LiveData con la lista de sensores
     * @param baseFileName Nombre del archivo sin extensión
     * @param lifecycleOwner Activity o Fragment que posee el ciclo de vida
     */
    public void generateBothFromLiveData(LiveData<List<Sensor>> sensorLiveData, String baseFileName, LifecycleOwner lifecycleOwner) {
        sensorLiveData.observe(lifecycleOwner, sensorList -> {
            if (sensorList != null && !sensorList.isEmpty()) {
                generateSensorReport(sensorList, baseFileName + "_pdf");
                generateSensorExcelReport(sensorList, baseFileName + "_excel");

                // Eliminar el observer solo después de generar ambos archivos
                sensorLiveData.removeObservers(lifecycleOwner);
            }
        });
    }


}
