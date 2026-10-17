
package com.co.poligran.polirestaurante.dao;

import com.co.poligran.polirestaurante.Entityes.DetallePedido;
import com.co.poligran.polirestaurante.Entityes.HistorialEstado;
import com.co.poligran.polirestaurante.Entityes.Mesa;
import com.co.poligran.polirestaurante.Entityes.Mesero;
import com.co.poligran.polirestaurante.Entityes.Pedido;
import com.co.poligran.polirestaurante.Entityes.Producto;
import com.co.poligran.polirestaurante.Entityes.Usuario;
import com.co.poligran.polirestaurante.config.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Normalizer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PedidoDAO {

    // =========================================================
    // CREAR TABLAS
    // =========================================================

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

        try (Connection conexion = Conexion.conectar()) {

            try (PreparedStatement ps =
                    conexion.prepareStatement(tablaPedido)) {
                ps.executeUpdate();
            }

            try (PreparedStatement ps =
                    conexion.prepareStatement(tablaDetalle)) {
                ps.executeUpdate();
            }

            try (PreparedStatement ps =
                    conexion.prepareStatement(tablaHistorial)) {
                ps.executeUpdate();
            }
        }
    }

    // =========================================================
    // GUARDAR PEDIDO, DETALLES E HISTORIAL
    // =========================================================

    public boolean guardar(Pedido pedido) throws SQLException {

        if (pedido == null) {
            throw new IllegalArgumentException(
                    "El pedido no puede ser null."
            );
        }

        if (pedido.getFecha() == null
                || pedido.getMesero() == null
                || pedido.getMesa() == null) {
            throw new IllegalArgumentException(
                    "El pedido debe tener fecha, mesero y mesa."
            );
        }

        String sqlPedido = """
            INSERT INTO pedido
                (id, fecha, estado, total, mesero_id, mesa_id)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        String sqlDetalle = """
            INSERT INTO detalle_pedido
                (pedido_id, producto_id, cantidad,
                 precio_unitario, subtotal)
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

                // Guardar pedido.
                try (PreparedStatement ps =
                        conexion.prepareStatement(sqlPedido)) {

                    ps.setInt(1, pedido.getId());
                    ps.setString(
                            2,
                            String.valueOf(pedido.getFecha().getTime())
                    );
                    ps.setString(3, pedido.getEstado());
                    ps.setDouble(4, pedido.getTotal());
                    ps.setInt(5, pedido.getMesero().getId());
                    ps.setInt(6, pedido.getMesa().getId());

                    ps.executeUpdate();
                }

                // La mesa queda ocupada mientras el pedido siga activo.
                if (!pedido.getEstado().equals("ENTREGADO")
                        && !pedido.getEstado().equals("CANCELADO")) {
                    try (PreparedStatement ps = conexion.prepareStatement(
                            "UPDATE mesa SET estado = ? WHERE id = ?")) {
                        ps.setString(1, "OCUPADA");
                        ps.setInt(2, pedido.getMesa().getId());
                        ps.executeUpdate();
                    }
                }

                // Guardar detalles respetando precio y subtotal.
                try (PreparedStatement ps =
                        conexion.prepareStatement(sqlDetalle)) {

                    for (DetallePedido detalle : pedido.getDetalles()) {

                        if (detalle.getProducto() == null) {
                            throw new SQLException(
                                    "Hay un detalle sin producto."
                            );
                        }

                        ps.setInt(1, pedido.getId());
                        ps.setInt(
                                2,
                                detalle.getProducto().getId()
                        );
                        ps.setInt(3, detalle.getCantidad());
                        ps.setDouble(
                                4,
                                detalle.getPrecioUnitario()
                        );
                        ps.setDouble(5, detalle.getSubtotal());

                        ps.addBatch();
                    }

                    ps.executeBatch();
                }

                // Guardar historial con las fechas reales.
                try (PreparedStatement ps =
                        conexion.prepareStatement(sqlHistorial)) {

                    for (HistorialEstado registro
                            : pedido.getHistorial()) {

                        ps.setInt(1, pedido.getId());
                        ps.setString(2, registro.getEstado());
                        ps.setString(
                                3,
                                String.valueOf(
                                        registro.getFechaHora().getTime()
                                )
                        );

                        ps.addBatch();
                    }

                    ps.executeBatch();
                }

                conexion.commit();
                return true;

            } catch (SQLException | RuntimeException e) {

                conexion.rollback();
                throw e;

            } finally {
                conexion.setAutoCommit(true);
            }
        }
    }

    // =========================================================
    // COMPROBAR SI EXISTE UN PEDIDO
    // =========================================================

    public boolean existe(int id) throws SQLException {

        String sql = "SELECT id FROM pedido WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // =========================================================
    // BUSCAR PEDIDO POR ID
    // =========================================================

    public Pedido buscarPorId(int id) throws SQLException {

        String sql = "SELECT * FROM pedido WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return convertirPedido(rs);
                }
            }
        }

        return null;
    }

    // =========================================================
    // LISTAR TODOS LOS PEDIDOS
    // =========================================================

    public List<Pedido> listarTodos() throws SQLException {

        List<Pedido> pedidos = new ArrayList<>();

        String sql = "SELECT * FROM pedido ORDER BY id";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                pedidos.add(convertirPedido(rs));
            }
        }

        return pedidos;
    }

    // =========================================================
    // CONVERTIR REGISTRO DE SQLITE EN OBJETO PEDIDO
    // =========================================================

    private Pedido convertirPedido(ResultSet rs) throws SQLException {

        int id = rs.getInt("id");
        int idMesero = rs.getInt("mesero_id");
        int idMesa = rs.getInt("mesa_id");

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        MesaDAO mesaDAO = new MesaDAO();

        Usuario usuario = usuarioDAO.buscarPorId(idMesero);
        Mesa mesa = mesaDAO.buscarPorId(idMesa);

        if (usuario == null) {
            throw new SQLException(
                    "No se encontró el usuario/mesero con ID "
                    + idMesero + " del pedido " + id
            );
        }

        if (mesa == null) {
            throw new SQLException(
                    "No se encontró la mesa con ID "
                    + idMesa + " del pedido " + id
            );
        }

        Mesero mesero = new Mesero(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getPassword()
        );

        Date fecha = convertirFecha(rs.getString("fecha"));

        Pedido pedido = new Pedido(
                id,
                fecha,
                rs.getString("estado"),
                rs.getDouble("total"),
                mesero,
                mesa
        );

        // Cargar los detalles reales del pedido.
        pedido.getDetalles().addAll(listarDetalles(id));

        // Cargar el historial con sus fechas originales.
        pedido.getHistorial().addAll(listarHistorial(id));

        return pedido;
    }

    // =========================================================
    // CONVERTIR FECHAS DE SQLITE
    // =========================================================

    private Date convertirFecha(String texto) throws SQLException {

        if (texto == null || texto.trim().isEmpty()) {
            throw new SQLException(
                    "La fecha guardada en la base de datos está vacía."
            );
        }

        texto = texto.trim();

        // Formato Unix en milisegundos, por ejemplo 1791935898156.
        try {
            if (texto.matches("-?\\d+")) {
                return new Date(Long.parseLong(texto));
            }
        } catch (NumberFormatException e) {
            throw new SQLException(
                    "La fecha numérica no es válida: " + texto, e
            );
        }

        // Formato SQL: yyyy-MM-dd HH:mm:ss
        try {
            return Timestamp.valueOf(texto);
        } catch (IllegalArgumentException ignored) {
            // Probar los formatos siguientes.
        }

        // Formato ISO con zona horaria.
        try {
            return Date.from(Instant.parse(texto));
        } catch (Exception ignored) {
            // Probar formato ISO con offset.
        }

        try {
            return Date.from(
                    OffsetDateTime.parse(texto).toInstant()
            );
        } catch (Exception ignored) {
            // Probar fecha y hora ISO sin zona.
        }

        try {
            return Timestamp.valueOf(
                    LocalDateTime.parse(texto.replace(' ', 'T'))
            );
        } catch (Exception ignored) {
            // Ningún formato reconocido.
        }

        throw new SQLException(
                "No se pudo interpretar la fecha almacenada: " + texto
        );
    }

    // =========================================================
    // LISTAR DETALLES DEL PEDIDO
    // =========================================================

    public List<DetallePedido> listarDetalles(int idPedido)
            throws SQLException {

        List<DetallePedido> detalles = new ArrayList<>();

        String sql = """
            SELECT id, producto_id, cantidad,
                   precio_unitario, subtotal
            FROM detalle_pedido
            WHERE pedido_id = ?
            ORDER BY id
            """;

        ProductoDAO productoDAO = new ProductoDAO();

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    int idDetalle = rs.getInt("id");
                    int idProducto = rs.getInt("producto_id");
                    int cantidad = rs.getInt("cantidad");
                    double precioUnitario =
                            rs.getDouble("precio_unitario");
                    double subtotal = rs.getDouble("subtotal");

                    Producto producto =
                            productoDAO.buscarPorId(idProducto);

                    if (producto == null) {
                        throw new SQLException(
                                "No se encontró el producto con ID "
                                + idProducto + " del pedido " + idPedido
                        );
                    }

                    DetallePedido detalle = new DetallePedido(
                            idDetalle,
                            producto,
                            cantidad,
                            precioUnitario,
                            subtotal
                    );

                    detalles.add(detalle);
                }
            }
        }

        return detalles;
    }

    // =========================================================
    // LISTAR HISTORIAL CONSERVANDO FECHA Y HORA
    // =========================================================

    public List<HistorialEstado> listarHistorial(int idPedido)
            throws SQLException {

        List<HistorialEstado> historial = new ArrayList<>();

        String sql = """
            SELECT id, estado, fecha_hora
            FROM historial_estado
            WHERE pedido_id = ?
            ORDER BY id
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    int idHistorial = rs.getInt("id");
                    String estado = rs.getString("estado");
                    String fechaTexto = rs.getString("fecha_hora");

                    Date fechaHora = convertirFecha(fechaTexto);

                    HistorialEstado registro = new HistorialEstado(
                            idHistorial,
                            estado,
                            fechaHora
                    );

                    historial.add(registro);
                }
            }
        }

        return historial;
    }

    // =========================================================
    // ACTUALIZAR ESTADO Y REGISTRAR EL CAMBIO
    // =========================================================

    public boolean actualizarEstado(int id, String nuevoEstado)
            throws SQLException {

        if (nuevoEstado == null) {
            return false;
        }

        // Acepta "En preparacion", "en preparación", "EN_PREPARACION", etc.
        nuevoEstado = Normalizer.normalize(nuevoEstado.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase()
                .replaceAll("\\s+", "_");

        if (!nuevoEstado.equals("PENDIENTE")
                && !nuevoEstado.equals("EN_PREPARACION")
                && !nuevoEstado.equals("LISTO")
                && !nuevoEstado.equals("ENTREGADO")
                && !nuevoEstado.equals("CANCELADO")) {
            return false;
        }

        String sqlBuscar =
                "SELECT estado, mesa_id FROM pedido WHERE id = ?";

        String sqlActualizar =
                "UPDATE pedido SET estado = ? WHERE id = ?";

        String sqlHistorial = """
            INSERT INTO historial_estado
                (pedido_id, estado, fecha_hora)
            VALUES (?, ?, ?)
            """;

        try (Connection conexion = Conexion.conectar()) {

            conexion.setAutoCommit(false);

            try {

                String estadoActual;
                int idMesa;

                // Verificar que el pedido exista y leer su estado.
                try (PreparedStatement ps =
                        conexion.prepareStatement(sqlBuscar)) {

                    ps.setInt(1, id);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            conexion.rollback();
                            return false;
                        }

                        estadoActual = rs.getString("estado");
                        idMesa = rs.getInt("mesa_id");
                    }
                }

                // No permitir cancelar un pedido ya entregado.
                if ("ENTREGADO".equals(estadoActual)
                        && "CANCELADO".equals(nuevoEstado)) {
                    conexion.rollback();
                    return false;
                }

                // Si el estado no cambia, no duplicar el historial.
                if (nuevoEstado.equals(estadoActual)) {
                    conexion.rollback();
                    return true;
                }

                // Actualizar estado del pedido.
                try (PreparedStatement ps =
                        conexion.prepareStatement(sqlActualizar)) {

                    ps.setString(1, nuevoEstado);
                    ps.setInt(2, id);

                    if (ps.executeUpdate() == 0) {
                        conexion.rollback();
                        return false;
                    }
                }

                // Registrar la fecha exacta del cambio.
                String fechaActual =
                        String.valueOf(System.currentTimeMillis());

                try (PreparedStatement ps =
                        conexion.prepareStatement(sqlHistorial)) {

                    ps.setInt(1, id);
                    ps.setString(2, nuevoEstado);
                    ps.setString(3, fechaActual);

                    ps.executeUpdate();
                }

                // Pedido terminado: liberar la mesa. Pedido reactivado: ocuparla.
                boolean terminado = nuevoEstado.equals("ENTREGADO")
                        || nuevoEstado.equals("CANCELADO");

                try (PreparedStatement ps = conexion.prepareStatement(
                        "UPDATE mesa SET estado = ? WHERE id = ?")) {
                    ps.setString(1, terminado ? "DISPONIBLE" : "OCUPADA");
                    ps.setInt(2, idMesa);
                    ps.executeUpdate();
                }

                conexion.commit();
                return true;

            } catch (SQLException | RuntimeException e) {

                conexion.rollback();
                throw e;

            } finally {
                conexion.setAutoCommit(true);
            }
        }
    }


    // =========================================================
    // ACTUALIZAR DATOS GENERALES (mesero, mesa y total)
    // =========================================================

    public boolean actualizar(Pedido pedido) throws SQLException {

        String sql = """
            UPDATE pedido
            SET mesero_id = ?, mesa_id = ?, total = ?
            WHERE id = ?
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, pedido.getMesero().getId());
            ps.setInt(2, pedido.getMesa().getId());
            ps.setDouble(3, pedido.getTotal());
            ps.setInt(4, pedido.getId());

            return ps.executeUpdate() > 0;
        }
    }

    // Sirve para recalcular el total despues de cambiar los detalles.
    public boolean actualizarTotal(int id, double total) throws SQLException {

        String sql = "UPDATE pedido SET total = ? WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setDouble(1, total);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // ELIMINAR PEDIDO CON SUS DETALLES E HISTORIAL
    // =========================================================

    public boolean eliminar(int id) throws SQLException {

        try (Connection conexion = Conexion.conectar()) {

            conexion.setAutoCommit(false);

            try {
                String estado;
                int idMesa;

                try (PreparedStatement ps = conexion.prepareStatement(
                        "SELECT estado, mesa_id FROM pedido WHERE id = ?")) {

                    ps.setInt(1, id);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conexion.rollback();
                            return false;
                        }
                        estado = rs.getString("estado");
                        idMesa = rs.getInt("mesa_id");
                    }
                }

                borrarPorId(conexion,
                        "DELETE FROM detalle_pedido WHERE pedido_id = ?", id);
                borrarPorId(conexion,
                        "DELETE FROM historial_estado WHERE pedido_id = ?", id);
                borrarPorId(conexion,
                        "DELETE FROM pedido WHERE id = ?", id);

                // Si el pedido seguia activo, la mesa queda disponible.
                if (!"ENTREGADO".equals(estado)
                        && !"CANCELADO".equals(estado)) {

                    try (PreparedStatement ps = conexion.prepareStatement(
                            "UPDATE mesa SET estado = ? WHERE id = ?")) {
                        ps.setString(1, "DISPONIBLE");
                        ps.setInt(2, idMesa);
                        ps.executeUpdate();
                    }
                }

                conexion.commit();
                return true;

            } catch (SQLException | RuntimeException e) {

                conexion.rollback();
                throw e;

            } finally {
                conexion.setAutoCommit(true);
            }
        }
    }

    private void borrarPorId(Connection conexion, String sql, int id)
            throws SQLException {

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // =========================================================
    // CONSULTAS PARA COCINA Y REPORTES
    // =========================================================

    public List<Pedido> listarPorEstado(String estado) throws SQLException {

        List<Pedido> pedidos = new ArrayList<>();

        String sql = "SELECT * FROM pedido WHERE estado = ? ORDER BY id";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, estado.trim().toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(convertirPedido(rs));
                }
            }
        }

        return pedidos;
    }

    public List<Pedido> listarPorRango(Date inicio, Date fin)
            throws SQLException {

        List<Pedido> pedidos = new ArrayList<>();

        String sql = """
            SELECT * FROM pedido
            WHERE CAST(fecha AS INTEGER) BETWEEN ? AND ?
            ORDER BY id
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setLong(1, inicio.getTime());
            ps.setLong(2, fin.getTime());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(convertirPedido(rs));
                }
            }
        }

        return pedidos;
    }

    // Suma de los pedidos ENTREGADOS dentro del rango de fechas.
    public double totalVentas(Date inicio, Date fin) throws SQLException {

        String sql = """
            SELECT COALESCE(SUM(total), 0) AS ventas
            FROM pedido
            WHERE estado = 'ENTREGADO'
              AND CAST(fecha AS INTEGER) BETWEEN ? AND ?
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setLong(1, inicio.getTime());
            ps.setLong(2, fin.getTime());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("ventas") : 0;
            }
        }
    }
}
