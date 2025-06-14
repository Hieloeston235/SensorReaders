package com.example.sensorreaders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.SensorViewHolder> {

    private List<Sensor> sensorList;

    public SensorAdapter(List<Sensor> sensorList) {
        this.sensorList = sensorList;
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
        holder.nombreTextView.setText(sensor.getNombre());
        holder.valorTextView.setText("Valor: " + sensor.getValor());
        holder.horaTextView.setText("Hora: " + sensor.getHora());
    }

    @Override
    public int getItemCount() {
        return sensorList.size();
    }

    public void updateData(List<Sensor> newList) {
        this.sensorList = newList;
        notifyDataSetChanged();
    }

    static class SensorViewHolder extends RecyclerView.ViewHolder {
        TextView nombreTextView, valorTextView, horaTextView;

        public SensorViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.tvSensorName);
            valorTextView = itemView.findViewById(R.id.tvSensorValue);
            horaTextView = itemView.findViewById(R.id.tvSensorTime);
        }
    }
}
