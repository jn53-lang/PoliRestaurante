
package com.co.poligran.polirestaurante.Entityes;

/**
 *
 * @author Jimmy Beltran 
 */
public class Mesa {
    int id;
    int numero;
    int capacidad;
    String estado;

    public Mesa(int id, int numero, int capacidad) {
        this.id = id;
        this.numero = numero;
        this.capacidad = capacidad;
        this.estado = estado;
    }
    
    public void asignarMesa(){
        System.out.println("Ocupada:");
    }
    
    public void liberarMesa(){
        System.out.println("Mesa disponible: ");  
    }
    public int getId() { return id; }
    public int getNumero() { return numero; }
    public int getCapacidad() { return capacidad; }
    public String getEstado() { return estado; }
}
