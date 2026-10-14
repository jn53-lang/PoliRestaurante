package com.co.poligran.polirestaurante.dao;

import com.co.poligran.polirestaurante.Entityes.Pedido;
import com.co.poligran.polirestaurante.Entityes.Mesero;
import com.co.poligran.polirestaurante.Entityes.Mesa;
import com.co.poligran.polirestaurante.dao.UsuarioDAO;
import com.co.poligran.polirestaurante.dao.MesaDAO;
import com.co.poligran.polirestaurante.Entityes.DetallePedido;
import com.co.poligran.polirestaurante.Entityes.HistorialEstado;
import com.co.poligran.polirestaurante.Entityes.Usuario;
import com.co.poligran.polirestaurante.config.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    public void crearTablas() throws SQLException {
        String tablaPedido = """
            CREATE TABLE IF NOT EXISTS pedido (
                id INTEGER PRIMARY KEY,
                fecha TEXT NOT NULL,
                estado TEXT NOT NULL,
                total REAL NOT NULL,
                mesero_id INTEGER NOT NULL,
                mesa_id INTEGER NOT NULL,
                FOREIGN KEY (mesero_id) REFERENCES usuario(id),
                FOREIGN KEY (mesa_id) REFERENCES mesa(id)
            )
            """;

        String tablaDetalle = """
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

        String tablaHistorial = """
            CREATE TABLE IF NOT EXISTS historial_estado (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pedido_id INTEGER NOT NULL,
                estado TEXT NOT NULL,
                fecha_hora TEXT NOT NULL,
                FOREIGN KEY (pedido_id) REFERENCES pedido(id)
            )
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(tablaPedido)) {
            sentencia.executeUpdate();
        }

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(tablaDetalle)) {
            sentencia.executeUpdate();
        }

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(tablaHistorial)) {
            sentencia.executeUpdate();
        }
    }

    public boolean guardar(Pedido pedido) throws SQLException {
        String sqlPedido = """
            INSERT INTO pedido (id, fecha, estado, total, mesero_id, mesa_id)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        String sqlDetalle = """
            INSERT INTO detalle_pedido
            (pedido_id, producto_id, cantidad, precio_unitario, subtotal)
            VALUES (?, ?, ?, ?, ?)
            """;

        String sqlHistorial = """
            INSERT INTO historial_estado
            (pedido_id, estado, fecha_hora)
            VALUES (?, ?, ?)
            """;

        try (Connection conexion = Conexion.conectar()) {
            conexion.setAutoCommit(false);

            try {
                try (PreparedStatement sentencia = conexion.prepareStatement(sqlPedido)) {
                    sentencia.setInt(1, pedido.getId());
                    sentencia.setTimestamp(2, new Timestamp(pedido.getFecha().getTime()));
                    sentencia.setString(3, pedido.getEstado());
                    sentencia.setDouble(4, pedido.getTotal());
                    sentencia.setInt(5, pedido.getMesero().getId());
                    sentencia.setInt(6, pedido.getMesa().getId());
                    sentencia.executeUpdate();
                }

                try (PreparedStatement sentencia = conexion.prepareStatement(sqlDetalle)) {
                    for (DetallePedido detalle : pedido.getDetalles()) {
                        sentencia.setInt(1, pedido.getId());
                        sentencia.setInt(2, detalle.getProducto().getId());
                        sentencia.setInt(3, detalle.getCantidad());
                        sentencia.setDouble(4, detalle.getPrecioUnitario());
                        sentencia.setDouble(5, detalle.getSubtotal());
                        sentencia.addBatch();
                    }
                    sentencia.executeBatch();
                }

                try (PreparedStatement sentencia = conexion.prepareStatement(sqlHistorial)) {
                    for (HistorialEstado historial : pedido.getHistorial()) {
                        sentencia.setInt(1, pedido.getId());
                        sentencia.setString(2, historial.getEstado());
                        sentencia.setTimestamp(3, new Timestamp(historial.getFechaHora().getTime()));
                        sentencia.addBatch();
                    }
                    sentencia.executeBatch();
                }

                conexion.commit();
                return true;

            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            } finally {
                conexion.setAutoCommit(true);
            }
        }
    }

    public boolean existe(int id) throws SQLException {
        String sql = "SELECT id FROM pedido WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);

            try (ResultSet resultados = sentencia.executeQuery()) {
                return resultados.next();
            }
        }
    }
    public Pedido buscarPorId(int id) throws SQLException {
    String sql = "SELECT * FROM pedido WHERE id = ?";

    try (Connection conexion = Conexion.conectar();
         PreparedStatement sentencia = conexion.prepareStatement(sql)) {

        sentencia.setInt(1, id);

        try (ResultSet resultados = sentencia.executeQuery()) {
            if (resultados.next()) {
                return convertirPedido(resultados);
            }
        }
    }

    return null;
}

public List<Pedido> listarTodos() throws SQLException {
    List<Pedido> pedidos = new ArrayList<>();
    String sql = "SELECT * FROM pedido ORDER BY id";

    try (Connection conexion = Conexion.conectar();
         PreparedStatement sentencia = conexion.prepareStatement(sql);
         ResultSet resultados = sentencia.executeQuery()) {

        while (resultados.next()) {
            pedidos.add(convertirPedido(resultados));
        }
    }

    return pedidos;
}

private Pedido convertirPedido(ResultSet resultados) throws SQLException {
    int id = resultados.getInt("id");
    int idMesero = resultados.getInt("mesero_id");
    int idMesa = resultados.getInt("mesa_id");

    UsuarioDAO usuarioDAO = new UsuarioDAO();
    MesaDAO mesaDAO = new MesaDAO();

    Usuario usuario = usuarioDAO.buscarPorId(idMesero);
    Mesa mesa = mesaDAO.buscarPorId(idMesa);

    if (usuario == null || mesa == null) {
        throw new SQLException(
            "No se encontró el mesero o la mesa del pedido " + id
        );
    }

    Mesero mesero = new Mesero(
        usuario.getId(),
        usuario.getNombre(),
        usuario.getCorreo(),
        usuario.getPassword()
    );

    String fechaTexto = resultados.getString("fecha");
Timestamp fecha;

try {
    fecha = Timestamp.valueOf(fechaTexto);
} catch (IllegalArgumentException e) {
    fecha = new Timestamp(System.currentTimeMillis());
}

    return new Pedido(
        id,
        fecha,
        resultados.getString("estado"),
        resultados.getDouble("total"),
        mesero,
        mesa
    );
}
public boolean actualizarEstado(int id, String nuevoEstado) throws SQLException {
    if (!nuevoEstado.equals("PENDIENTE")
            && !nuevoEstado.equals("EN_PREPARACION")
            && !nuevoEstado.equals("LISTO")
            && !nuevoEstado.equals("ENTREGADO")
            && !nuevoEstado.equals("CANCELADO")) {
        return false;
    }

    String sqlBuscar = "SELECT estado FROM pedido WHERE id = ?";
    String sqlActualizar = "UPDATE pedido SET estado = ? WHERE id = ?";
    String sqlHistorial = "INSERT INTO historial_estado (pedido_id, estado, fecha_hora) VALUES (?, ?, ?)";

    try (Connection conexion = Conexion.conectar()) {
        conexion.setAutoCommit(false);

        try {
            String estadoActual;

            try (PreparedStatement sentencia = conexion.prepareStatement(sqlBuscar)) {
                sentencia.setInt(1, id);

                try (ResultSet resultados = sentencia.executeQuery()) {
                    if (!resultados.next()) {
                        conexion.rollback();
                        return false;
                    }

                    estadoActual = resultados.getString("estado");
                }
            }

            if (estadoActual.equals("ENTREGADO") && nuevoEstado.equals("CANCELADO")) {
                conexion.rollback();
                return false;
            }

            try (PreparedStatement sentencia = conexion.prepareStatement(sqlActualizar)) {
                sentencia.setString(1, nuevoEstado);
                sentencia.setInt(2, id);
                sentencia.executeUpdate();
            }

            try (PreparedStatement sentencia = conexion.prepareStatement(sqlHistorial)) {
                sentencia.setInt(1, id);
                sentencia.setString(2, nuevoEstado);
                sentencia.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                sentencia.executeUpdate();
            }

            conexion.commit();
            return true;

        } catch (SQLException e) {
            conexion.rollback();
            throw e;
        } finally {
            conexion.setAutoCommit(true);
        }
    }
}
public List<HistorialEstado> listarHistorial(int idPedido) throws SQLException {
    List<HistorialEstado> historial = new ArrayList<>();

    String sql = """
        SELECT id, estado, fecha_hora
        FROM historial_estado
        WHERE pedido_id = ?
        ORDER BY id
        """;

    try (Connection conexion = Conexion.conectar();
         PreparedStatement sentencia = conexion.prepareStatement(sql)) {

        sentencia.setInt(1, idPedido);

        try (ResultSet resultados = sentencia.executeQuery()) {
            while (resultados.next()) {
                HistorialEstado registro = new HistorialEstado(
                    resultados.getInt("id"),
                    resultados.getString("estado")
                );

                historial.add(registro);
            }
        }
    }

    return historial;
}
}
