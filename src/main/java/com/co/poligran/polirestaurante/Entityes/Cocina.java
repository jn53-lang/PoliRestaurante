/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.co.poligran.polirestaurante.Entityes;

/**
 *
 * @author Stuve
 */
public class Cocina extends Usuario{
    
    public Cocina(int id, String nombre, String correo, String password) {
        super(id, nombre, correo, password, "Cocina");
    }
    
    public void verPedidoPendiente(){
        System.out.println("Cocina consultando pedidos pendientes..."); 
    }
    public void prepararPedido(Pedido pedido){
        pedido.cambiarEstado("EN_PREPARACION");
        System.out.println("Pedido #" + pedido.getId() + " en preparación.");
    }    
    
    public void marcarListo(Pedido pedido) {
        pedido.cambiarEstado("LISTO");
        System.out.println("Pedido #" + pedido.getId() + " listo para entregar.");
    }

    public void cambiarEstado(Pedido pedido, String nuevoEstado){
                pedido.cambiarEstado(nuevoEstado);

    }
}

