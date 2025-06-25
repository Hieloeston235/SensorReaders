package com.example.sensorreaders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sensorreaders.Models.Sensor;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.SensorViewHolder> {

    private List<Sensor> sensorList = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    // Constructor vacío
    public SensorAdapter() {
        this.sensorList = new ArrayList<>();
    }

    // Constructor con lista inicial
    public SensorAdapter(List<Sensor> sensorList) {
        this.sensorList = sensorList != null ? sensorList : new ArrayList<>();
    }

    // Método para actualizar la lista completa
    public void setSensorList(List<Sensor> sensorList) {
        this.sensorList = sensorList != null ? sensorList : new ArrayList<>();
        notifyDataSetChanged();
    }

    // Método alternativo para compatibilidad
    public void updateData(List<Sensor> newList) {
        setSensorList(newList);
    }

    @NonNull
    @Override
    public SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sensor_historial, parent, false);
        return new SensorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SensorViewHolder holder, int position) {
        Sensor sensor = sensorList.get(position);
        holder.bind(sensor, position);
    }

    @Override
    public int getItemCount() {
        return sensorList.size();
    }

    public class SensorViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFechaHora, tvTemperatura, tvHumedad, tvPresion;
        private TextView tvHumedadSuelo, tvLuz, tvViento, tvLluvia, tvHumo, tvGas;
        private View itemContainer;

        public SensorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHora);
            tvTemperatura = itemView.findViewById(R.id.tvTemperatura);
            tvHumedad = itemView.findViewById(R.id.tvHumedad);
            tvPresion = itemView.findViewById(R.id.tvPresion);
            tvHumedadSuelo = itemView.findViewById(R.id.tvHumedadSuelo);
            tvLuz = itemView.findViewById(R.id.tvLuz);
            tvViento = itemView.findViewById(R.id.tvViento);
            tvLluvia = itemView.findViewById(R.id.tvLluvia);
            tvHumo = itemView.findViewById(R.id.tvHumo);
            tvGas = itemView.findViewById(R.id.tvGas);
            itemContainer = itemView.findViewById(R.id.itemContainer);
        }

        public void bind(Sensor sensor, int position) {
            // Formatear fecha y hora
            Date fecha = new Date(sensor.getFecha());
            String fechaStr = dateFormat.format(fecha);
            String horaStr = timeFormat.format(fecha);
            tvFechaHora.setText(fechaStr + "\n" + horaStr);

            // Formatear temperatura con validación de null
            Double temperaturaObj = sensor.getTemperatura();
            if (temperaturaObj != null) {
                double temp = temperaturaObj.doubleValue();
                tvTemperatura.setText(String.format(Locale.getDefault(), "%.1f°C", temp));

                // Aplicar colores según temperatura
                if (temp < 15) {
                    tvTemperatura.setTextColor(itemView.getContext().getResources().getColor(R.color.temp_cold));
                } else if (temp > 30) {
                    tvTemperatura.setTextColor(itemView.getContext().getResources().getColor(R.color.temp_hot));
                } else {
                    tvTemperatura.setTextColor(itemView.getContext().getResources().getColor(R.color.temp_normal));
                }
            } else {
                tvTemperatura.setText("N/A");
                tvTemperatura.setTextColor(itemView.getContext().getResources().getColor(R.color.text_primary));
            }

            // Formatear humedad con validación de null
            Double humedadObj = sensor.getHumedad();
            if (humedadObj != null) {
                double hum = humedadObj.doubleValue();
                tvHumedad.setText(String.format(Locale.getDefault(), "%.0f%%", hum));

                // Aplicar colores según humedad
                if (hum < 30) {
                    tvHumedad.setTextColor(itemView.getContext().getResources().getColor(R.color.hum_low));
                } else if (hum > 70) {
                    tvHumedad.setTextColor(itemView.getContext().getResources().getColor(R.color.hum_high));
                } else {
                    tvHumedad.setTextColor(itemView.getContext().getResources().getColor(R.color.hum_normal));
                }
            } else {
                tvHumedad.setText("N/A");
                tvHumedad.setTextColor(itemView.getContext().getResources().getColor(R.color.text_primary));
            }

            // Formatear presión con validación de null
            Double presionObj = sensor.getPresionAtmosferica();
            if (presionObj != null) {
                double presion = presionObj.doubleValue();
                tvPresion.setText(String.format(Locale.getDefault(), "%.0f hPa", presion));
            } else {
                tvPresion.setText("N/A");
            }
            tvPresion.setTextColor(itemView.getContext().getResources().getColor(R.color.text_primary));

            // Formatear humedad del suelo
            Double humedadSueloObj = sensor.getHumedadSuelo();
            if (humedadSueloObj != null) {
                double humSuelo = humedadSueloObj.doubleValue();
                tvHumedadSuelo.setText(String.format(Locale.getDefault(), "%.0f%%", humSuelo));

                // Aplicar colores según humedad del suelo
                if (humSuelo < 20) {
                    tvHumedadSuelo.setTextColor(itemView.getContext().getResources().getColor(R.color.hum_low));
                } else if (humSuelo > 80) {
                    tvHumedadSuelo.setTextColor(itemView.getContext().getResources().getColor(R.color.hum_high));
                } else {
                    tvHumedadSuelo.setTextColor(itemView.getContext().getResources().getColor(R.color.hum_normal));
                }
            } else {
                tvHumedadSuelo.setText("N/A");
                tvHumedadSuelo.setTextColor(itemView.getContext().getResources().getColor(R.color.text_primary));
            }

            // Formatear luz
            Double luzObj = sensor.getLuz();
            if (luzObj != null) {
                double luz = luzObj.doubleValue();
                tvLuz.setText(String.format(Locale.getDefault(), "%.0f lux", luz));
            } else {
                tvLuz.setText("N/A");
            }
            tvLuz.setTextColor(itemView.getContext().getResources().getColor(R.color.text_primary));

            // Formatear viento
            Double vientoObj = sensor.getViento();
            if (vientoObj != null) {
                String vientoTexto = interpretarViento(vientoObj);
                tvViento.setText(vientoTexto);

                // Aplicar colores según interpretación del viento
                if (vientoTexto.equals("Sin viento")) {
                    tvViento.setTextColor(itemView.getContext().getResources().getColor(R.color.text_secondary));
                } else if (vientoTexto.equals("Brisa leve")) {
                    tvViento.setTextColor(itemView.getContext().getResources().getColor(R.color.success_color));
                } else if (vientoTexto.equals("Moderado")) {
                    tvViento.setTextColor(itemView.getContext().getResources().getColor(R.color.warning_color));
                } else if (vientoTexto.equals("Fuerte")) {
                    tvViento.setTextColor(itemView.getContext().getResources().getColor(R.color.error_color));
                }
            } else {
                tvViento.setText("N/A");
                tvViento.setTextColor(itemView.getContext().getResources().getColor(R.color.text_primary));
            }

            // CORREGIDO: Formatear lluvia - solo mostrar "Sí" o "No" como en el adapter anterior
            Double lluviaObj = sensor.getLluvia();
            if (lluviaObj != null) {
                // Lógica igual que el adapter anterior: si es 1 = "Sí", si no = "No"
                String lluviaTexto = (lluviaObj == 1) ? "Sí" : "No";
                tvLluvia.setText(lluviaTexto);

                // Aplicar colores: verde para "No" (sin lluvia), azul para "Sí" (con lluvia)
                if (lluviaTexto.equals("No")) {
                    tvLluvia.setTextColor(itemView.getContext().getResources().getColor(R.color.success_color));
                } else {
                    tvLluvia.setTextColor(itemView.getContext().getResources().getColor(R.color.primary_color));
                }
            } else {
                tvLluvia.setText("N/A");
                tvLluvia.setTextColor(itemView.getContext().getResources().getColor(R.color.text_primary));
            }

            // Formatear humo
            Double humoObj = sensor.getHumo();
            if (humoObj != null) {
                String humoTexto = interpretarHumo(humoObj);
                tvHumo.setText(humoTexto);

                // Aplicar colores según nivel de humo
                if (humoTexto.equals("Bajo")) {
                    tvHumo.setTextColor(itemView.getContext().getResources().getColor(R.color.success_color));
                } else if (humoTexto.equals("Moderado")) {
                    tvHumo.setTextColor(itemView.getContext().getResources().getColor(R.color.warning_color));
                } else {
                    tvHumo.setTextColor(itemView.getContext().getResources().getColor(R.color.error_color));
                }
            } else {
                tvHumo.setText("N/A");
                tvHumo.setTextColor(itemView.getContext().getResources().getColor(R.color.text_primary));
            }

            // Formatear gas
            Double gasObj = sensor.getGas();
            if (gasObj != null) {
                String gasTexto = interpretarGas(gasObj);
                tvGas.setText(gasTexto);

                // Aplicar colores según nivel de gas
                if (gasTexto.equals("Bajo")) {
                    tvGas.setTextColor(itemView.getContext().getResources().getColor(R.color.success_color));
                } else if (gasTexto.equals("Moderado")) {
                    tvGas.setTextColor(itemView.getContext().getResources().getColor(R.color.warning_color));
                } else {
                    tvGas.setTextColor(itemView.getContext().getResources().getColor(R.color.error_color));
                }
            } else {
                tvGas.setText("N/A");
                tvGas.setTextColor(itemView.getContext().getResources().getColor(R.color.text_primary));
            }

            // Alternar color de fondo para mejor legibilidad
            if (position % 2 == 0) {
                itemContainer.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.row_even));
            } else {
                itemContainer.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.row_odd));
            }
        }
    }

    // Métodos auxiliares para interpretación
    private String interpretarGas(Double valor) {
        if (valor == null) return "N/A";
        if (valor < 100) return "Bajo";
        else if (valor <= 300) return "Moderado";
        else return "Alto";
    }

    private String interpretarHumo(Double valor) {
        if (valor == null) return "N/A";
        if (valor < 50) return "Bajo";
        else if (valor <= 150) return "Moderado";
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
}