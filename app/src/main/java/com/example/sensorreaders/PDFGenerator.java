package com.example.sensorreaders;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.example.sensorreaders.Models.Sensor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PDFGenerator {

    private Context context;
    private static final String PDF_DIRECTORY = "/ReportesSensores/";

    public PDFGenerator(Context context) {
        this.context = context;
    }

    /**
     * Genera un PDF con los datos de los sensores
     * @param sensorList Lista de objetos Sensor
     * @param fileName Nombre del archivo PDF (sin extensión)
     * @return true si se generó correctamente, false si hubo error
     */
    public boolean generateSensorReport(List<Sensor> sensorList, String fileName) {
        // Verificar permisos primero (para Android 6.0+)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Se necesitan permisos de almacenamiento", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Crear directorio usando File(File parent, String child) para mejor compatibilidad
        File dir = new File(Environment.getExternalStorageDirectory(), PDF_DIRECTORY);

        // Verificar y crear directorio con verificación de éxito
        if (!dir.exists() && !dir.mkdirs()) {
            Toast.makeText(context, "No se pudo crear el directorio", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Verificar si el directorio es realmente un directorio y se puede escribir
        if (!dir.isDirectory() || !dir.canWrite()) {
            Toast.makeText(context, "Problemas con el directorio de destino", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Crear archivo PDF
        File file = new File(dir, fileName + ".pdf");

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);

            document.open();
            addTitle(document);
            addMetadata(document);
            addSensorDataTable(document, sensorList);
            document.close();

            Toast.makeText(context, "PDF generado en: " + file.getPath(), Toast.LENGTH_LONG).show();
            return true;

        } catch (IOException | DocumentException e) {
            e.printStackTrace();
            String errorMsg = "Error al generar PDF: " + e.getMessage();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                errorMsg += "\nEn Android 10+, usa el almacenamiento específico de la app";
            }
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
            return false;
        }
    }
    /**
     * Genera PDF cuando los datos del LiveData estén disponibles
     * @param sensorLiveData LiveData con la lista de sensores
     * @param fileName Nombre del archivo
     * @param lifecycleOwner Activity/Fragment dueño del ciclo de vida
     */
    public void generateFromLiveData(LiveData<List<Sensor>> sensorLiveData, String fileName, LifecycleOwner lifecycleOwner) {
        sensorLiveData.observe(lifecycleOwner, sensorList -> {
            if (sensorList != null && !sensorList.isEmpty()) {
                generateSensorReport(sensorList, fileName);
                // Opcional: dejar de observar después de generar el PDF
                sensorLiveData.removeObservers(lifecycleOwner);
            }
        });
    }

    private void addTitle(Document document) throws DocumentException {
        Paragraph title = new Paragraph("Reporte de Datos de Sensores");
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);

        Paragraph subTitle = new Paragraph("\nDatos recopilados del sistema de monitoreo\n\n");
        subTitle.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(subTitle);
    }

    private void addMetadata(Document document) throws DocumentException {
        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        Paragraph metadata = new Paragraph("Generado el: " + currentDate + "\n\n");
        document.add(metadata);
    }

    private void addSensorDataTable(Document document, List<Sensor> sensorList) throws DocumentException {
        PdfPTable table = new PdfPTable(10); // 10 columnas

        // Configurar anchos de columnas (en porcentajes)
        float[] columnWidths = {5f, 8f, 8f, 10f, 8f, 8f, 12f, 10f, 8f, 12f};
        table.setWidths(columnWidths);
        table.setWidthPercentage(100);

        // Encabezados de la tabla
        addTableHeader(table);

        // Datos de los sensores
        for (Sensor sensor : sensorList) {
            addSensorRow(table, sensor);
        }

        document.add(table);
    }

    private void addTableHeader(PdfPTable table) {
        String[] headers = {
                "ID", "Gas", "Humedad", "Humedad Suelo", "Humo",
                "Lluvia", "Presión Atmosf.", "Temperatura", "Viento", "Fecha"
        };

        for (String header : headers) {
            PdfPCell cell = new PdfPCell();
            cell.setPhrase(new Phrase(header));
            table.addCell(cell);
        }
    }

    private void addSensorRow(PdfPTable table, Sensor sensor) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        table.addCell(String.valueOf(sensor.getId()));
        table.addCell(formatDouble(sensor.getGas()));
        table.addCell(formatDouble(sensor.getHumedad()));
        table.addCell(formatDouble(sensor.getHumedadSuelo()));
        table.addCell(formatDouble(sensor.getHumo()));
        table.addCell(formatDouble(sensor.getLluvia()));
        table.addCell(formatDouble(sensor.getPresionAtmosferica()));
        table.addCell(formatDouble(sensor.getTemperatura()));
        table.addCell(formatDouble(sensor.getViento()));
        //table.addCell(sensor.getFecha() != null ? dateFormat.format(sensor.getFecha()) : "N/A");
    }

    private String formatDouble(Double value) {
        return value != null ? String.format(Locale.getDefault(), "%.2f", value) : "N/A";
    }
}