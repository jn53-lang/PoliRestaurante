
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

    // Crea la tabla que almacena los productos asociados a cada pedido.
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

        // Abre la conexion y ejecuta la instruccion SQL.
        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.executeUpdate();
        }
    }

    // Guarda un producto dentro de un pedido existente.
    public boolean guardar(int idPedido, DetallePedido detalle)
            throws SQLException {

        String sql = """
            INSERT INTO detalle_pedido
                (pedido_id, producto_id, cantidad, precio_unitario, subtotal)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            // Asigna el ID del pedido y los datos del detalle.
            sentencia.setInt(1, idPedido);
            sentencia.setInt(2, detalle.getProducto().getId());
            sentencia.setInt(3, detalle.getCantidad());
            sentencia.setDouble(4, detalle.getPrecioUnitario());
            sentencia.setDouble(5, detalle.getSubtotal());

            // Devuelve true si se guardo algun registro.
            return sentencia.executeUpdate() > 0;
        }
    }

    // Consulta todos los detalles que pertenecen a un pedido.
    public List<DetallePedido> listarPorPedido(int idPedido)
            throws SQLException {

        List<DetallePedido> detalles = new ArrayList<>();

        // Une detalle_pedido con producto para recuperar sus datos.
        String sql = """
            SELECT dp.id, dp.cantidad, dp.precio_unitario,
                   p.id AS producto_id, p.nombre, p.precio,
                   p.descripcion, p.disponible
            FROM detalle_pedido dp
            INNER JOIN producto p ON dp.producto_id = p.id
            WHERE dp.pedido_id = ?
            ORDER BY dp.id
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            // Indica el pedido cuyos detalles se quieren consultar.
            sentencia.setInt(1, idPedido);

            try (ResultSet resultados = sentencia.executeQuery()) {

                // Recorre los resultados de la consulta.
                while (resultados.next()) {

                    // Reconstruye el producto con los datos de la base de datos.
                    Producto producto = new Producto(
                        resultados.getInt("producto_id"),
                        resultados.getString("nombre"),
                        resultados.getDouble("precio"),
                        resultados.getString("descripcion"),
                        resultados.getInt("disponible") == 1
                    );

                    // Crea el detalle con su producto, cantidad y precios.
                    DetallePedido detalle = new DetallePedido(
                        resultados.getInt("id"),
                        producto,
                        resultados.getInt("cantidad"),
                        resultados.getDouble("precio_unitario"),
                        resultados.getDouble("subtotal")
                    );

                    // Agrega el detalle a la lista.
                    detalles.add(detalle);
                }
            }
        }

        // Devuelve la lista de detalles encontrados.
        return detalles;
    }

    // Elimina un detalle del pedido mediante su identificador.
    public boolean eliminar(int idDetalle) throws SQLException {
        String sql = "DELETE FROM detalle_pedido WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idDetalle);

            // Devuelve true si se elimino algun registro.
            return sentencia.executeUpdate() > 0;
        }
    }

    // Busca un detalle especifico y recupera tambien los datos de su producto.
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

            // Asigna el ID del detalle que se va a buscar.
            sentencia.setInt(1, idDetalle);

            try (ResultSet resultados = sentencia.executeQuery()) {

                // Si existe, construye y devuelve el detalle encontrado.
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

        // Devuelve null si no existe un detalle con ese ID.
        return null;
    }

    // Cambia la cantidad y recalcula el subtotal con el precio guardado.
    public boolean actualizarCantidad(int idDetalle, int cantidad)
            throws SQLException {

        // Impide guardar cantidades iguales o inferiores a cero.
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

            // Asigna la nueva cantidad y la utiliza para recalcular el subtotal.
            sentencia.setInt(1, cantidad);
            sentencia.setInt(2, cantidad);
            sentencia.setInt(3, idDetalle);

            // Devuelve true si se actualizo algun registro.
            return sentencia.executeUpdate() > 0;
        }
    }
}
