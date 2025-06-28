package com.example.sensorreaders;

import com.example.sensorreaders.Models.Sensor;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SensorDeserializer implements JsonDeserializer<Sensor> {

    private static final SimpleDateFormat[] formats = new SimpleDateFormat[]{
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    };

    static {
        for (SimpleDateFormat fmt : formats) {
            fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
    }

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


        try {
            String fechaStr = obj.get("fecha").getAsString();
            Date date = null;
            for (SimpleDateFormat fmt : formats) {
                try {
                    date = fmt.parse(fechaStr);
                    break;
                } catch (Exception ignored) {}
            }
            if (date != null) {
                sensor.setFecha(date.getTime());
            } else {
                sensor.setFecha(System.currentTimeMillis()); // fallback si ninguno funciona
            }
        } catch (Exception e) {
            sensor.setFecha(System.currentTimeMillis()); // fallback
        }




        return sensor;
    }
}
