package com.example.sensorreaders;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

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
        // Crear directorio si no existe
        File dir = new File(Environment.getExternalStorageDirectory() + PDF_DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Crear archivo PDF
        File file = new File(dir, fileName + ".pdf");
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);

            document.open();
            addTitle(document);
            addMetadata(document);
            addSensorDataTable(document, sensorList);
            document.close();

            outputStream.close();

            Toast.makeText(context, "PDF generado en: " + file.getPath(), Toast.LENGTH_LONG).show();
            return true;

        } catch (IOException | DocumentException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al generar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
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
        table.addCell(sensor.getFecha() != null ? dateFormat.format(sensor.getFecha()) : "N/A");
    }

    private String formatDouble(Double value) {
        return value != null ? String.format(Locale.getDefault(), "%.2f", value) : "N/A";
    }
}