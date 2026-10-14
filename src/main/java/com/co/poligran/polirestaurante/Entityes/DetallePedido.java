
package com.co.poligran.polirestaurante.Entityes;

public class DetallePedido {

    private int id;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
    private Producto producto;

    // Constructor para crear un detalle nuevo.
    public DetallePedido(int id, Producto producto, int cantidad) {
        this.id = id;
        this.producto = producto;
        this.cantidad = cantidad;

        if (producto == null) {
            throw new IllegalArgumentException(
                    "El producto no puede ser null."
            );
        }

        this.precioUnitario = producto.getPrecio();
        calcularSubtotal();
    }

    // Constructor para recuperar un detalle existente de SQLite.
    public DetallePedido(
            int id,
            Producto producto,
            int cantidad,
            double precioUnitario,
            double subtotal) {

        if (producto == null) {
            throw new IllegalArgumentException(
                    "El producto no puede ser null."
            );
        }

        this.id = id;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public void calcularSubtotal() {
        subtotal = cantidad * precioUnitario;
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
