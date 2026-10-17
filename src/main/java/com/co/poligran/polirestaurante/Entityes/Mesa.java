
package com.co.poligran.polirestaurante.Entityes;

/**
 *
 * @author Jimmy Beltran 
 */
public class Mesa {
    private int id;
    private int numero;
    private int capacidad;
    private String estado;

    public Mesa(int id, int numero, int capacidad) {
        this.id = id;
        this.numero = numero;
        this.capacidad = capacidad;
        this.estado = "Disponible";
    }

    public void asignarMesa() {
        this.estado = "Ocupada";
    }

    public void liberarMesa() {
        this.estado = "Disponible";
    }

    public int getId() {
        return id;
    }

    public int getNumero() {
        return numero;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public String getEstado() {
        return estado;
    }
}
