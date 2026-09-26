
package com.co.poligran.polirestaurante.Entityes;


public class Administrador extends Usuario {
    
    public Administrador(int id, String nombre, String correo, String password) {
        super(id, nombre, correo, password, "Administrador");
    }
    public void gestionarProductos(){
                System.out.println("Gestionando productos: ");
    }
    
    public void gestionarCategorias(){
                System.out.println("Gestionando categorías: ");

        
    }
    public void gestionarMesas(){
                System.out.println("Gestionando mesas: ");

        
    }
    public void gestionarUsuarios(){
        System.out.println("Gestionando usuarios: ");
        
    }
    public void gestionarReporte(){
        System.out.println("Generando reporte: ");
        
    }
}
