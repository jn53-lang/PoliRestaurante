package com.co.poligran.polirestaurante.Main;

import com.co.poligran.polirestaurante.dao.PedidoDAO;
import com.co.poligran.polirestaurante.Entityes.Pedido;

public class PruebaActualizarPedido {

    public static void main(String[] args) {
        try {
            PedidoDAO pedidoDAO = new PedidoDAO();

            Pedido pedido = pedidoDAO.buscarPorId(1);

            if (pedido == null) {
                System.out.println("No existe el pedido con ID 1.");
                return;
            }

            System.out.println("Estado anterior: " + pedido.getEstado());

            boolean actualizado = pedidoDAO.actualizarEstado(
                1,
                "EN_PREPARACION"
            );

            if (actualizado) {
                Pedido pedidoActualizado = pedidoDAO.buscarPorId(1);
                System.out.println("Estado nuevo: " + pedidoActualizado.getEstado());
                System.out.println("Actualización exitosa.");
            } else {
                System.out.println("No se pudo actualizar el pedido.");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}