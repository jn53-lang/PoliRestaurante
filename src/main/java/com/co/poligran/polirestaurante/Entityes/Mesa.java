package com.co.poligran.polirestaurante.Entityes;

public class Mesa {
    private int id;
    private int numero;
    private int capacidad;
    private String estado;

    public Mesa(int id, int numero, int capacidad) {
        this.id = id;
        this.numero = numero;
        this.capacidad = capacidad;
        this.estado = "DISPONIBLE";
    }

    public void asignarMesa() {
        this.estado = "OCUPADA";
    }

    public void liberarMesa() {
        this.estado = "DISPONIBLE";
    }

    public int getId() {
        return id;
    }

    public int getNumero() {
        return numero;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public String getEstado() {
        return estado;
    }
}