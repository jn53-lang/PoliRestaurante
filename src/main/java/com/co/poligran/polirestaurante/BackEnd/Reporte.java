/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.co.poligran.polirestaurante.BackEnd;

import java.util.Date;

/**
 *
 * @author Stuve
 */
public class Reporte {
    private int id;
    private Date fechaInicio;
    private Date fechaFin;
    private String tipo;

    public Reporte(int id, Date fechaInicio, Date fechaFin, String tipo) {
        this.id = id;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public String getTipo() {
        return tipo;
    }
    
    
    public void generarReporte(){
        System.out.println("Reporte #" + id);
        System.out.println("Tipo: " + tipo);
        System.out.println("Desde: " + fechaInicio);
        System.out.println("Hasta: " + fechaFin);
    }
}
