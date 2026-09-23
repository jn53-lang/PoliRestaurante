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
    int id;
    Date fechaInicio;
    Date fechaFin;
    String tipo;

    public Reporte(int id, Date fechaInicio, Date fechaFin, String tipo) {
        this.id = id;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tipo = tipo;
    }
    
    public void generarReporte(){
    
    }
}
