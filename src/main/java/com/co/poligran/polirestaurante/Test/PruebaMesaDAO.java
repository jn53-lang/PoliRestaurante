package com.co.poligran.polirestaurante.Test;

import com.co.poligran.polirestaurante.Entityes.Mesa;
import com.co.poligran.polirestaurante.dao.MesaDAO;
import java.sql.SQLException;

public class PruebaMesaDAO {

    public static void main(String[] args) {
        MesaDAO dao = new MesaDAO();

        try {
            dao.crearTabla();

            Mesa mesaExistente = dao.buscarPorId(1);

            if (mesaExistente == null) {
                Mesa mesa = new Mesa(1, 1, 4);
                System.out.println("Mesa guardada: " + dao.guardar(mesa));
            } else {
                System.out.println("La mesa ya estaba guardada.");
            }

            System.out.println("Lista de mesas:");

            for (Mesa mesa : dao.listarTodos()) {
                System.out.println(
                    "ID: " + mesa.getId() +
                    " - Numero: " + mesa.getNumero() +
                    " - Capacidad: " + mesa.getCapacidad() +
                    " - Estado: " + mesa.getEstado()
                );
            }

        } catch (SQLException e) {
            System.out.println("Error al probar MesaDAO");
            e.printStackTrace();
        }
    }
}
