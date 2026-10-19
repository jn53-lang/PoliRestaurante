
package com.co.poligran.polirestaurante.dao;

import com.co.poligran.polirestaurante.Entityes.DetallePedido;
import com.co.poligran.polirestaurante.Entityes.Producto;
import com.co.poligran.polirestaurante.config.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DetallePedidoDAO {

    public void crearTabla() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS detalle_pedido (
                id INTEGER PRIMARY KEY,
                pedido_id INTEGER NOT NULL,
                producto_id INTEGER NOT NULL,
                cantidad INTEGER NOT NULL,
                precio_unitario REAL NOT NULL,
                subtotal REAL NOT NULL,
                FOREIGN KEY (pedido_id) REFERENCES pedido(id),
                FOREIGN KEY (producto_id) REFERENCES producto(id)
            )
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.executeUpdate();
        }
    }

    public boolean guardar(int idPedido, DetallePedido detalle)
            throws SQLException {

        String sql = """
            INSERT INTO detalle_pedido
                (pedido_id, producto_id, cantidad, precio_unitario, subtotal)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPedido);
            sentencia.setInt(2, detalle.getProducto().getId());
            sentencia.setInt(3, detalle.getCantidad());
            sentencia.setDouble(4, detalle.getPrecioUnitario());
            sentencia.setDouble(5, detalle.getSubtotal());

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<DetallePedido> listarPorPedido(int idPedido)
            throws SQLException {

        List<DetallePedido> detalles = new ArrayList<>();

        String sql = """
            SELECT dp.id, dp.cantidad, dp.precio_unitario, dp.subtotal,
                   p.id AS producto_id, p.nombre, p.precio,
                   p.descripcion, p.disponible
            FROM detalle_pedido dp
            INNER JOIN producto p ON dp.producto_id = p.id
            WHERE dp.pedido_id = ?
            ORDER BY dp.id
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPedido);

            try (ResultSet resultados = sentencia.executeQuery()) {
                while (resultados.next()) {

                    Producto producto = new Producto(
                        resultados.getInt("producto_id"),
                        resultados.getString("nombre"),
                        resultados.getDouble("precio"),
                        resultados.getString("descripcion"),
                        resultados.getInt("disponible") == 1
                    );

                    DetallePedido detalle = new DetallePedido(
                        resultados.getInt("id"),
                        producto,
                        resultados.getInt("cantidad"),
                        resultados.getDouble("precio_unitario"),
                        resultados.getDouble("subtotal")
                    );

                    detalles.add(detalle);
                }
            }
        }

        return detalles;
    }

    public boolean eliminar(int idDetalle) throws SQLException {
        String sql = "DELETE FROM detalle_pedido WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idDetalle);

            return sentencia.executeUpdate() > 0;
        }
    }


    public DetallePedido buscarPorId(int idDetalle) throws SQLException {
        String sql = """
            SELECT dp.id, dp.cantidad, dp.precio_unitario, dp.subtotal,
                   p.id AS producto_id, p.nombre, p.precio,
                   p.descripcion, p.disponible
            FROM detalle_pedido dp
            INNER JOIN producto p ON dp.producto_id = p.id
            WHERE dp.id = ?
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idDetalle);

            try (ResultSet resultados = sentencia.executeQuery()) {
                if (resultados.next()) {
                    Producto producto = new Producto(
                        resultados.getInt("producto_id"),
                        resultados.getString("nombre"),
                        resultados.getDouble("precio"),
                        resultados.getString("descripcion"),
                        resultados.getInt("disponible") == 1
                    );

                    return new DetallePedido(
                        resultados.getInt("id"),
                        producto,
                        resultados.getInt("cantidad"),
                        resultados.getDouble("precio_unitario"),
                        resultados.getDouble("subtotal")
                    );
                }
            }
        }

        return null;
    }

    // Cambia la cantidad y recalcula el subtotal con el precio guardado.
    public boolean actualizarCantidad(int idDetalle, int cantidad)
            throws SQLException {

        if (cantidad <= 0) {
            return false;
        }

        String sql = """
            UPDATE detalle_pedido
            SET cantidad = ?, subtotal = precio_unitario * ?
            WHERE id = ?
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, cantidad);
            sentencia.setInt(2, cantidad);
            sentencia.setInt(3, idDetalle);

            return sentencia.executeUpdate() > 0;
        }
    }
}
