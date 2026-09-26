/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.co.poligran.polirestaurante.BackEnd;

/**
 *
 * @author Stuve
 */
public class Producto {
    private int id;
    private String nombre;
    private double precio;
    private String descripcion;
    private boolean disponible;
    private Categoria categoria;

    public Producto(int id, String nombre, double precio, String descripcion, boolean disponible) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.disponible = disponible;
    }

public void actualizarPrecio(double nuevoPrecio) {
        if (nuevoPrecio >= 0) {
            this.precio = nuevoPrecio;
        }
    }

    public void cambiarDisponibilidad() {
        this.disponible = !this.disponible;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    
}
