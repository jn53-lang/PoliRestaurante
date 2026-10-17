
package com.co.poligran.polirestaurante.Entityes;

/**
 *
 * @author Jimmy Beltran 
 */
public class Mesa {
    public static final String ESTADO_DISPONIBLE = "DISPONIBLE";
    public static final String ESTADO_OCUPADA = "OCUPADA";

    private int id;
    private int numero;
    private int capacidad;
    private String estado;

    public Mesa(int id, int numero, int capacidad) {
        this.id = id;
        this.numero = numero;
        this.capacidad = capacidad;
        this.estado = ESTADO_DISPONIBLE;
    }

    public void asignarMesa() {
        this.estado = ESTADO_OCUPADA;
    }

    public void liberarMesa() {
        this.estado = ESTADO_DISPONIBLE;
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

    public boolean isOcupada() {
        return ESTADO_OCUPADA.equals(estado);
    }

    @Override
    public String toString() {
        return "Mesa " + numero + " (capacidad " + capacidad + ") - " + estado;
    }
}
