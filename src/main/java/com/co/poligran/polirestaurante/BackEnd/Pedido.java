/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.co.poligran.polirestaurante.BackEnd;

import java.util.Date;
import java.util.ArrayList;

/**
 *
 * @author salaG201
 */
public class Pedido {
    private int id;
    private Date fecha;
    private String estado;
    private double total;

    private Mesero mesero;
    private Mesa mesa;
    private ArrayList<DetallePedido> detalles;
    private ArrayList<HistorialEstado> historial;

    public Pedido(int id, Mesero mesero, Mesa mesa) {
        this.id = id;
        this.mesero = mesero;
        this.mesa = mesa;
        this.fecha = new Date();
        this.estado = "PENDIENTE";
        this.total = 0;
        this.detalles = new ArrayList<>();
        this.historial = new ArrayList<>();

        if (mesa != null) {
            mesa.asignarMesa();
        }

        registrarEstado();
    }

    public void crearPedido() {
        System.out.println("Pedido #" + id + " creado.");
    }

    public void agregarProducto(Producto producto, int cantidad) {
        if (producto != null && producto.isDisponible() && cantidad > 0) {
            DetallePedido detalle = new DetallePedido(
                detalles.size() + 1, producto, cantidad
            );
            detalles.add(detalle);
            calcularTotal();
            System.out.println("Producto agregado al pedido.");
        } else {
            System.out.println("No se puede agregar el producto.");
        }
    }

    public void quitarProducto(Producto producto) {
        for (int i = 0; i < detalles.size(); i++) {
            if (detalles.get(i).getProducto() == producto) {
                detalles.remove(i);
                calcularTotal();
                System.out.println("Producto eliminado del pedido.");
                return;
            }
        }
        System.out.println("El producto no está en el pedido.");
    }

    public void cambiarEstado(String nuevoEstado) {
        if (!estadoValido(nuevoEstado)) {
            System.out.println("Estado no válido.");
            return;
        }

        if (nuevoEstado.equals("CANCELADO") && estado.equals("ENTREGADO")) {
            System.out.println("No se puede cancelar un pedido entregado.");
            return;
        }

        estado = nuevoEstado;
        registrarEstado();
        System.out.println("Pedido #" + id + " -> " + estado);
    }

    public void cancelar() {
        if (!estado.equals("ENTREGADO")) {
            cambiarEstado("CANCELADO");
        } else {
            System.out.println("El pedido ya fue entregado.");
        }
    }

    private boolean estadoValido(String nuevoEstado) {
        return nuevoEstado.equals("PENDIENTE")
            || nuevoEstado.equals("EN_PREPARACION")
            || nuevoEstado.equals("LISTO")
            || nuevoEstado.equals("ENTREGADO")
            || nuevoEstado.equals("CANCELADO");
    }

    private void registrarEstado() {
        HistorialEstado registro =
            new HistorialEstado(historial.size() + 1, estado);
        registro.registrarCambio();
        historial.add(registro);
    }

    private void calcularTotal() {
        total = 0;
        for (DetallePedido detalle : detalles) {
            total += detalle.getSubtotal();
        }
    }

    public int getId() { 
        return id; 
    }
    public Date getFecha() { 
        return fecha; 
    }
    public String getEstado() { 
        return estado; 
    }
    public double getTotal() { 
        return total; 
    }
    public Mesero getMesero() { 
        return mesero; 
    }
    public Mesa getMesa() { 
        return mesa; 
    }
    public ArrayList<DetallePedido> getDetalles(){ 
        return detalles; 
    }
    public ArrayList<HistorialEstado> getHistorial() {
        return historial; 
    }
}
