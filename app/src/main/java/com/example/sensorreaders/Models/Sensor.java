package com.example.sensorreaders.Models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

//modificar clases con sensores pertinente
@Entity(tableName = "sensores_datosensor")
public class Sensor {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo
    private Double gas;
    @ColumnInfo
    private Double humedad;
    @ColumnInfo
    private Double humedadSuelo;
    @ColumnInfo
    private Double humo;
    @ColumnInfo
    private Double lluvia;
    @ColumnInfo
    private Double luz;
    @ColumnInfo
    private Double presionAtmosferica;
    @ColumnInfo
    private Double temperatura;
    @ColumnInfo
    private Double viento;
    @Ignore
    private String firebaseKey;

    @Ignore
    private boolean isLocalOnly;
    @ColumnInfo
    private long fecha;
    @Ignore
    public Sensor() {
    }

    public Sensor(Double gas, Double humedad, Double humedadSuelo, Double humo, Double lluvia, Double luz, Double presionAtmosferica, Double temperatura, Double viento, long fecha) {
        this.gas = gas;
        this.humedad = humedad;
        this.humedadSuelo = humedadSuelo;
        this.humo = humo;
        this.lluvia = lluvia;
        this.luz = luz;
        this.presionAtmosferica = presionAtmosferica;
        this.temperatura = temperatura;
        this.viento = viento;
        this.fecha = fecha;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }

    public Double getLuz() {
        return this.luz;
    }

    public void setLuz(Double luz) {
        this.luz = luz;
    }

    public boolean isLocalOnly() {
        return isLocalOnly;
    }

    public void setLocalOnly(boolean localOnly) {
        isLocalOnly = localOnly;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public Double getGas() {
        return gas;
    }

    public void setGas(Double gas) {
        this.gas = gas;
    }

    public Double getHumedad() {
        return humedad;
    }

    public void setHumedad(Double humedad) {
        this.humedad = humedad;
    }

    public Double getHumedadSuelo() {
        return humedadSuelo;
    }

    public void setHumedadSuelo(Double humedadSuelo) {
        this.humedadSuelo = humedadSuelo;
    }

    public Double getHumo() {
        return humo;
    }

    public void setHumo(Double humo) {
        this.humo = humo;
    }

    public Double getLluvia() {
        return lluvia;
    }

    public void setLluvia(Double lluvia) {
        this.lluvia = lluvia;
    }

    public Double getPresionAtmosferica() {
        return presionAtmosferica;
    }

    public void setPresionAtmosferica(Double presionAtmosferica) {
        this.presionAtmosferica = presionAtmosferica;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Double getViento() {
        return viento;
    }

    public void setViento(Double viento) {
        this.viento = viento;
    }




    public int getId() {
        return id;
    }
}
