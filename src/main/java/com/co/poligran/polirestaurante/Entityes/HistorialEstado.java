
package com.co.poligran.polirestaurante.Entityes;

import java.util.Date;

public class HistorialEstado {

    private int id;
    private String estado;
    private Date fechaHora;

    // Constructor original: conserva compatibilidad con el proyecto.
    public HistorialEstado(int id, String estado) {
        this(id, estado, new Date());
    }

    // Constructor para cargar la fecha real desde la base de datos.
    public HistorialEstado(int id, String estado, Date fechaHora) {
        this.id = id;
        this.estado = estado;
        this.fechaHora = fechaHora != null
                ? new Date(fechaHora.getTime())
                : new Date();
    }

    public int getId() {
        return id;
    }

    public String getEstado() {
        return estado;
    }

    public Date getFechaHora() {
        return new Date(fechaHora.getTime());
    }

    public void registrarCambio() {
        fechaHora = new Date();
        System.out.println(
                "Estado registrado: " + estado + " - " + fechaHora
        );
    }
}