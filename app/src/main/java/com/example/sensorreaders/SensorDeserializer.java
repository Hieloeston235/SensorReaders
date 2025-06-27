package com.example.sensorreaders;

import com.example.sensorreaders.Models.Sensor;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SensorDeserializer implements JsonDeserializer<Sensor> {
    @Override
    public Sensor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        Sensor sensor = new Sensor();

        sensor.setId(obj.get("id").getAsInt());
        sensor.setTemperatura(obj.get("temperatura").getAsDouble());
        sensor.setHumedad(obj.get("humedad").getAsDouble());
        sensor.setPresionAtmosferica(obj.get("presionAtmosferica").getAsDouble());
        sensor.setLuz(obj.get("luz").getAsDouble());
        sensor.setViento(obj.get("viento").getAsDouble());
        sensor.setLluvia(obj.get("lluvia").getAsDouble());
        sensor.setGas(obj.get("gas").getAsDouble());
        sensor.setHumo(obj.get("humo").getAsDouble());
        sensor.setHumedadSuelo(obj.get("humedadSuelo").getAsDouble());

        // Convertir fecha en formato ISO8601 a timestamp
        String fechaString = obj.get("fecha").getAsString();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault());
            Date date = sdf.parse(fechaString);
            sensor.setFecha(date.getTime()); // ✅ Aquí se guarda la fecha REAL
        } catch (ParseException e) {
            e.printStackTrace();
            sensor.setFecha(System.currentTimeMillis()); // fallback si falla
        }

        return sensor;
    }
}

