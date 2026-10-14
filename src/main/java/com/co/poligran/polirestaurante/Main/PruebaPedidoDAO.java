package com.co.poligran.polirestaurante.Main;

import com.co.poligran.polirestaurante.Entityes.Mesero;
import com.co.poligran.polirestaurante.Entityes.Mesa;
import com.co.poligran.polirestaurante.Entityes.Producto;
import com.co.poligran.polirestaurante.Entityes.Pedido;
import com.co.poligran.polirestaurante.dao.MesaDAO;
import com.co.poligran.polirestaurante.dao.ProductoDAO;
import com.co.poligran.polirestaurante.dao.UsuarioDAO;
import com.co.poligran.polirestaurante.dao.PedidoDAO;
import java.sql.SQLException;

public class PruebaPedidoDAO {

    public static void main(String[] args) {
        try {
            MesaDAO mesaDAO = new MesaDAO();
            ProductoDAO productoDAO = new ProductoDAO();
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            PedidoDAO pedidoDAO = new PedidoDAO();

            mesaDAO.crearTabla();
            productoDAO.crearTabla();
            usuarioDAO.crearTabla();
            pedidoDAO.crearTablas();

            Mesero mesero = new Mesero(
                1,
                "Carlos",
                "carlos@restaurante.com",
                "12345"
            );

            Mesa mesa = new Mesa(1, 1, 4);
            Producto producto = new Producto(
                1,
                "Hamburguesa",
                18000,
                "Hamburguesa de la casa",
                true
            );

            if (usuarioDAO.buscarPorId(mesero.getId()) == null) {
                usuarioDAO.guardar(mesero);
            }

            if (mesaDAO.buscarPorId(mesa.getId()) == null) {
                mesaDAO.guardar(mesa);
            }

            if (productoDAO.buscarPorId(producto.getId()) == null) {
                productoDAO.guardar(producto);
            }

            if (pedidoDAO.existe(1)) {
                System.out.println("El pedido de prueba ya existe.");
                return;
            }

            Pedido pedido = new Pedido(1, mesero, mesa);
            pedido.agregarProducto(producto, 2);

            System.out.println("Total del pedido: $" + pedido.getTotal());

            boolean guardado = pedidoDAO.guardar(pedido);

            System.out.println("Pedido guardado: " + guardado);

        } catch (SQLException e) {
            System.out.println("Error al probar el pedido.");
            e.printStackTrace();
        }
    }
}
