package com.co.poligran.polirestaurante.Main;

import com.co.poligran.polirestaurante.dao.PedidoDAO;
import com.co.poligran.polirestaurante.Entityes.HistorialEstado;

public class PruebaHistorialPedido {

    public static void main(String[] args) {
        try {
            PedidoDAO pedidoDAO = new PedidoDAO();

            for (HistorialEstado registro : pedidoDAO.listarHistorial(1)) {
                System.out.println(
                    "Estado: " + registro.getEstado()
                    + " | Fecha: " + registro.getFechaHora()
                );
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}