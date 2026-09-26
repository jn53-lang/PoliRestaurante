/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.co.poligran.polirestaurante.BackEnd;

/**
 *
 * @author Stuve
 */
public class DetallePedido {
    private int id;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
    private Producto producto;

    public DetallePedido(int id, int cantidad, double precioUnitario, double subtotal) {
        this.id = id;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        calcularSubtotal();
    }
    
    public void cancelarSubtotal(){
        subtotal = cantidad * precioUnitario;
        return subtotal;
    }

    public int getId() {
        return id;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        calcularSubtotal();
    }
    
    
}
