/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.co.poligran.polirestaurante.BackEnd;

import java.util.ArrayList;

/**
 *
 * @author Stuve
 */
public class Categoria {
    private int id;
    private String nombre;
    private String descripcion;
    private ArrayList<Producto> productos;

    public Categoria(int id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.productos = new ArrayList<>();
    }
    public void agregarProducto(Producto producto) {
        if (!productos.contains(producto)) {
            productos.add(producto);
            producto.setCategoria(this);
        }
    }

    public void eliminarProducto(Producto producto) {
        productos.remove(producto);
        if (producto.getCategoria() == this) {
            producto.setCategoria(null);
        }
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public ArrayList<Producto> getProductos() {
        return productos;
    }
    
    
    

    
    
    
}
