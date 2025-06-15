package com.example.sensorreaders.Models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
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
    private Double presionAtmosferica;
    @ColumnInfo
    private Double temperatura;
    @ColumnInfo
    private Double viento;
    @ColumnInfo
    private Date Fecha;

    public Sensor(Double gas, Double humedad, Double humedadSuelo, Double humo, Double lluvia, Double presionAtmosferica, Double temperatura, Double viento, Date fecha) {
        this.gas = gas;
        this.humedad = humedad;
        this.humedadSuelo = humedadSuelo;
        this.humo = humo;
        this.lluvia = lluvia;
        this.presionAtmosferica = presionAtmosferica;
        this.temperatura = temperatura;
        this.viento = viento;
        Fecha = fecha;
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

    public Date getFecha() {
        return Fecha;
    }

    public void setFecha(Date fecha) {
        Fecha = fecha;
    }

    public int getId() {
        return id;
    }
}
