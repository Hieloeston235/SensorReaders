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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;


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

            // 🔽 Agrega aquí el gráfico después de la tabla
            Bitmap chartBitmap = generateChart(sensorList);
            if (chartBitmap != null) {
                addChartToDocument(document, chartBitmap);
            }


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
        Context chartContext = context.getApplicationContext();

        LineChart lineChart = new LineChart(chartContext);
        lineChart.setLayoutParams(new ViewGroup.LayoutParams(800, 600)); // Tamaño del gráfico

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

        LineDataSet dataSet = new LineDataSet(entries, "Temperatura (°C)");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircles(true);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        lineChart.getDescription().setText("Temperatura vs. Tiempo");
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setLabelCount(labels.size(), true);
        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                return i >= 0 && i < labels.size() ? labels.get(i) : "";
            }
        });


        lineChart.invalidate(); // Actualiza visualmente

        // Renderizar a Bitmap
        lineChart.measure(
                View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY)
        );
        lineChart.layout(0, 0, 800, 600);
        return lineChart.getChartBitmap();
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

    private void addChartToDocument(Document document, Bitmap chartBitmap) throws IOException, DocumentException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        chartBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        Image chartImage = Image.getInstance(stream.toByteArray());
        chartImage.scaleToFit(500, 300);
        chartImage.setAlignment(Element.ALIGN_CENTER);
        document.add(new Paragraph("\nGráfico de Temperaturas:\n"));
        document.add(chartImage);
    }



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
