package com.example.sensorreaders;
public class Sensor {
    private String nombre;
    private Double valor;
    private String hora;

    public Sensor() {
        // Constructor vacío requerido por Firebase
    }

    public Sensor(String nombre, Double valor, String hora) {
        this.nombre = nombre;
        this.valor = valor;
        this.hora = hora;
    }

    public String getNombre() {
        return nombre;
    }

    public Double getValor() {
        return valor;
    }

    public String getHora() {
        return hora;
    }
}
