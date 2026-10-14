package com.co.poligran.polirestaurante.Main;

import com.co.poligran.polirestaurante.Entityes.HistorialEstado;
import com.co.poligran.polirestaurante.Entityes.Pedido;
import com.co.poligran.polirestaurante.dao.HistorialEstadoDAO;
import com.co.poligran.polirestaurante.dao.PedidoDAO;

import java.sql.SQLException;
import java.util.List;

public class PruebaHistorialEstadoDAO {

    public static void main(String[] args) {
        PedidoDAO pedidoDAO = new PedidoDAO();
        HistorialEstadoDAO historialDAO = new HistorialEstadoDAO();

        int idGuardado = -1;

        try {
            pedidoDAO.crearTablas();
            historialDAO.crearTabla();

            List<Pedido> pedidos = pedidoDAO.listarTodos();

            if (pedidos.isEmpty()) {
                System.out.println(
                    "PRUEBA NO REALIZADA: no hay pedidos registrados."
                );
                return;
            }

            Pedido pedido = pedidos.get(0);

            HistorialEstado historial =
                new HistorialEstado(0, "PRUEBA_DAO");

            idGuardado = historialDAO.guardar(
                pedido.getId(), historial
            );

            System.out.println("Pedido utilizado: " + pedido.getId());
            System.out.println("ID guardado: " + idGuardado);

            List<HistorialEstado> registros =
                historialDAO.listarPorPedido(pedido.getId());

            HistorialEstado encontrado = null;

            for (HistorialEstado registro : registros) {
                if (registro.getId() == idGuardado) {
                    encontrado = registro;
                    break;
                }
            }

            if (encontrado == null
                    || !encontrado.getEstado().equals("PRUEBA_DAO")) {
                throw new SQLException(
                    "No se encontró correctamente el registro guardado."
                );
            }

            System.out.println("Registro guardado y consultado correctamente.");
            System.out.println("Estado: " + encontrado.getEstado());
            System.out.println("Fecha: " + encontrado.getFechaHora());

            if (!historialDAO.eliminar(idGuardado)) {
                throw new SQLException(
                    "No se pudo eliminar el registro de prueba."
                );
            }

            idGuardado = -1;

            System.out.println("Registro de prueba eliminado correctamente.");
            System.out.println("PRUEBA FINALIZADA.");

        } catch (SQLException e) {
            System.err.println("ERROR EN LA PRUEBA: " + e.getMessage());

        } finally {
            if (idGuardado != -1) {
                try {
                    historialDAO.eliminar(idGuardado);
                } catch (SQLException e) {
                    System.err.println(
                        "No se pudo limpiar el registro: " + e.getMessage()
                    );
                }
            }
        }
    }
}