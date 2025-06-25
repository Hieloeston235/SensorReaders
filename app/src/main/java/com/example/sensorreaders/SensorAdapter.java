package com.example.sensorreaders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sensorreaders.Models.Sensor;


import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.List;
import java.util.Locale;

public class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.SensorViewHolder> {

    private List<Sensor> sensorList;

    public SensorAdapter(List<Sensor> sensorList) {
        this.sensorList = sensorList;
    }

    public SensorAdapter() {
        this.sensorList = new ArrayList<>();
    }

    @NonNull
    @Override
    public SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sensor, parent, false);
        return new SensorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SensorViewHolder holder, int position) {
        Sensor sensor = sensorList.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String fechaStr = sdf.format(new Date(sensor.getFecha()));
        holder.horaTextView.setText("🕒 " + fechaStr);

        String resumen = String.format(Locale.getDefault(),
                "Temp: %.1f°C | Humedad: %.0f%% | Gas: %s",
                sensor.getTemperatura(),
                sensor.getHumedad(),
                interpretarGas(sensor.getGas()));

        holder.resumenTextView.setText(resumen);

        String extras = String.format("Lluvia: %s | Presión: %s | Viento: %s",
                sensor.getLluvia() != null && sensor.getLluvia() == 1 ? "Sí" : "No",
                interpretarPresion(sensor.getPresionAtmosferica()),
                interpretarViento(sensor.getViento()));

        holder.extrasTextView.setText(extras);

    }

    @Override
    public int getItemCount() {
        return sensorList.size();
    }

    public void updateData(List<Sensor> newList) {
        this.sensorList = newList;
        notifyDataSetChanged();
    }

    public void setSensorList(List<Sensor> newList) {
        this.sensorList = newList;
        notifyDataSetChanged();
    }

    private String interpretarGas(Double valor) {
        if (valor == null) return "N/A";
        if (valor < 100) return "Bajo";
        else if (valor <= 300) return "Moderado";
        else return "Alto";
    }

    private String interpretarPresion(Double valor) {
        if (valor == null) return "N/A";
        if (valor < 1000) return "Baja";
        else if (valor <= 1020) return "Normal";
        else return "Alta";
    }

    private String interpretarViento(Double valor) {
        if (valor == null) return "N/A";
        if (valor == 0) return "Sin viento";
        else if (valor < 10) return "Brisa leve";
        else if (valor < 30) return "Moderado";
        else return "Fuerte";
    }



    static class SensorViewHolder extends RecyclerView.ViewHolder {
        TextView horaTextView, resumenTextView, extrasTextView;

        public SensorViewHolder(@NonNull View itemView) {
            super(itemView);
            horaTextView = itemView.findViewById(R.id.tvSensorTime);
            resumenTextView = itemView.findViewById(R.id.tvSensorResumen);
            extrasTextView = itemView.findViewById(R.id.tvSensorExtras);
        }

    }
}
