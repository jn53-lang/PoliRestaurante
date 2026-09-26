/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.co.poligran.polirestaurante.BackEnd;

/**
 *
 * @author Stuve
 */
public class Mesero extends Usuario{
    
    public Mesero(int id, String nombre, String correo, String password) {
        super(id, nombre, correo, password, "Mesero");
    }
    
    public void crearPedido(){
                System.out.println("El mesero " + getNombre() + " está creando un pedido.");
    }
     public void agregarProducto(Pedido pedido, Producto producto, int cantidad) {
        pedido.agregarProducto(producto, cantidad);
    }

    public void quitarProducto(Pedido pedido, Producto producto) {
        pedido.quitarProducto(producto);
    }

    public void entregarPedido(Pedido pedido) {
        pedido.cambiarEstado("ENTREGADO");
        System.out.println("Pedido #" + pedido.getId() + " entregado.");
    }
}
