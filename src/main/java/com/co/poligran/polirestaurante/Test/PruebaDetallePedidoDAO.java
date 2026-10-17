package com.co.poligran.polirestaurante.Test;

import com.co.poligran.polirestaurante.Entityes.DetallePedido;
import com.co.poligran.polirestaurante.Entityes.Pedido;
import com.co.poligran.polirestaurante.Entityes.Producto;
import com.co.poligran.polirestaurante.dao.DetallePedidoDAO;
import com.co.poligran.polirestaurante.dao.PedidoDAO;
import com.co.poligran.polirestaurante.dao.ProductoDAO;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PruebaDetallePedidoDAO {

    public static void main(String[] args) {
        PedidoDAO pedidoDAO = new PedidoDAO();
        ProductoDAO productoDAO = new ProductoDAO();
        DetallePedidoDAO detalleDAO = new DetallePedidoDAO();

        int idDetalleGuardado = -1;

        try {
            productoDAO.crearTabla();
            pedidoDAO.crearTablas();
            detalleDAO.crearTabla();

            List<Pedido> pedidos = pedidoDAO.listarTodos();
            List<Producto> productos = productoDAO.listarTodos();

            if (pedidos.isEmpty()) {
                System.out.println(
                    "PRUEBA NO REALIZADA: no hay pedidos registrados."
                );
                return;
            }

            if (productos.isEmpty()) {
                System.out.println(
                    "PRUEBA NO REALIZADA: no hay productos registrados."
                );
                return;
            }

            Pedido pedido = pedidos.get(0);
            Producto producto = productos.get(0);

            List<DetallePedido> detallesAntes =
                detalleDAO.listarPorPedido(pedido.getId());

            Set<Integer> idsAnteriores = new HashSet<>();

            for (DetallePedido detalle : detallesAntes) {
                idsAnteriores.add(detalle.getId());
            }

            DetallePedido detalle = new DetallePedido(0, producto, 2);

            System.out.println("Pedido utilizado: " + pedido.getId());
            System.out.println("Producto utilizado: " + producto.getNombre());
            System.out.println("Cantidad: " + detalle.getCantidad());
            System.out.println(
                "Precio unitario: " + detalle.getPrecioUnitario()
            );
            System.out.println("Subtotal esperado: " + detalle.getSubtotal());

            boolean guardado = detalleDAO.guardar(pedido.getId(), detalle);

            if (!guardado) {
                throw new SQLException("No se pudo guardar el detalle.");
            }

            List<DetallePedido> detallesDespues =
                detalleDAO.listarPorPedido(pedido.getId());

            DetallePedido encontrado = null;

            for (DetallePedido d : detallesDespues) {
                if (!idsAnteriores.contains(d.getId())) {
                    encontrado = d;
                    idDetalleGuardado = d.getId();
                    break;
                }
            }

            if (encontrado == null) {
                throw new SQLException(
                    "El detalle se guardó, pero no se pudo identificar."
                );
            }

            if (encontrado.getProducto().getId() != producto.getId()
                    || encontrado.getCantidad() != 2) {
                throw new SQLException(
                    "Los datos del detalle guardado no coinciden."
                );
            }

            System.out.println("Detalle guardado y consultado correctamente.");
            System.out.println("ID del detalle: " + idDetalleGuardado);
            System.out.println(
                "Subtotal consultado: " + encontrado.getSubtotal()
            );

            boolean eliminado = detalleDAO.eliminar(idDetalleGuardado);

            if (!eliminado) {
                throw new SQLException(
                    "No se pudo eliminar el detalle de prueba."
                );
            }

            idDetalleGuardado = -1;

            System.out.println("Detalle de prueba eliminado correctamente.");
            System.out.println("PRUEBA FINALIZADA.");

        } catch (SQLException e) {
            System.err.println("ERROR EN LA PRUEBA: " + e.getMessage());

        } finally {
            if (idDetalleGuardado != -1) {
                try {
                    detalleDAO.eliminar(idDetalleGuardado);
                    System.out.println("Se intentó limpiar el detalle de prueba.");
                } catch (SQLException e) {
                    System.err.println(
                        "No se pudo limpiar el detalle: " + e.getMessage()
                    );
                }
            }
        }
    }
}