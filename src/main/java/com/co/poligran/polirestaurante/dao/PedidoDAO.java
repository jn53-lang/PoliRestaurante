
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

    // Crea las tablas necesarias para guardar pedidos, detalles e historial.
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

        // Usa una misma conexion para crear las tres tablas.
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

    // Guarda el pedido junto con sus detalles y su historial inicial.
    public boolean guardar(Pedido pedido) throws SQLException {

        // Verifica que el objeto recibido exista.
        if (pedido == null) {
            throw new IllegalArgumentException(
                    "El pedido no puede ser null."
            );
        }

        // Comprueba que el pedido tenga los datos obligatorios.
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

            // Inicia una transaccion para guardar todo como una sola operacion.
            conexion.setAutoCommit(false);

            try {

                // Guarda los datos generales del pedido.
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

                // Marca la mesa como ocupada si el pedido sigue activo.
                if (!pedido.getEstado().equals("ENTREGADO")
                        && !pedido.getEstado().equals("CANCELADO")) {
                    try (PreparedStatement ps = conexion.prepareStatement(
                            "UPDATE mesa SET estado = ? WHERE id = ?")) {
                        ps.setString(1, "OCUPADA");
                        ps.setInt(2, pedido.getMesa().getId());
                        ps.executeUpdate();
                    }
                }

                // Guarda los productos incluidos en el pedido.
                try (PreparedStatement ps =
                        conexion.prepareStatement(sqlDetalle)) {

                    // Recorre cada detalle asociado al pedido.
                    for (DetallePedido detalle : pedido.getDetalles()) {

                        // Impide guardar un detalle sin producto.
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

                        // Agrega la insercion al lote para ejecutarla despues.
                        ps.addBatch();
                    }

                    // Inserta todos los detalles acumulados.
                    ps.executeBatch();
                }

                // Guarda los cambios de estado que trae el pedido.
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

                    // Registra los elementos del historial en un solo lote.
                    ps.executeBatch();
                }

                // Confirma los cambios cuando todas las operaciones funcionan.
                conexion.commit();
                return true;

            } catch (SQLException | RuntimeException e) {

                // Revierte los cambios si ocurre un error.
                conexion.rollback();
                throw e;

            } finally {
                // Restaura el comportamiento normal de la conexion.
                conexion.setAutoCommit(true);
            }
        }
    }

    // Comprueba si existe un pedido con el ID indicado.
    public boolean existe(int id) throws SQLException {

        String sql = "SELECT id FROM pedido WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                // Si hay una fila, el pedido existe.
                return rs.next();
            }
        }
    }

    // Busca un pedido por su ID.
    public Pedido buscarPorId(int id) throws SQLException {

        String sql = "SELECT * FROM pedido WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                // Convierte el registro encontrado en un objeto Pedido.
                if (rs.next()) {
                    return convertirPedido(rs);
                }
            }
        }

        // Devuelve null si no encuentra el pedido.
        return null;
    }

    // Consulta todos los pedidos y los organiza por ID.
    public List<Pedido> listarTodos() throws SQLException {

        List<Pedido> pedidos = new ArrayList<>();

        String sql = "SELECT * FROM pedido ORDER BY id";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Convierte cada registro y lo agrega a la lista.
            while (rs.next()) {
                pedidos.add(convertirPedido(rs));
            }
        }

        return pedidos;
    }

    // Convierte un registro de la base de datos en un objeto Pedido completo.
    private Pedido convertirPedido(ResultSet rs) throws SQLException {

        int id = rs.getInt("id");
        int idMesero = rs.getInt("mesero_id");
        int idMesa = rs.getInt("mesa_id");

        // Usa los DAO correspondientes para recuperar el mesero y la mesa.
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        MesaDAO mesaDAO = new MesaDAO();

        Usuario usuario = usuarioDAO.buscarPorId(idMesero);
        Mesa mesa = mesaDAO.buscarPorId(idMesa);

        // Informa si el usuario asociado no existe.
        if (usuario == null) {
            throw new SQLException(
                    "No se encontró el usuario/mesero con ID "
                    + idMesero + " del pedido " + id
            );
        }

        // Informa si la mesa asociada no existe.
        if (mesa == null) {
            throw new SQLException(
                    "No se encontró la mesa con ID "
                    + idMesa + " del pedido " + id
            );
        }

        // Crea el objeto Mesero con los datos recuperados del usuario.
        Mesero mesero = new Mesero(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getPassword()
        );

        // Convierte la fecha almacenada en la base de datos.
        Date fecha = convertirFecha(rs.getString("fecha"));

        // Construye el pedido con sus datos principales.
        Pedido pedido = new Pedido(
                id,
                fecha,
                rs.getString("estado"),
                rs.getDouble("total"),
                mesero,
                mesa
        );

        // Carga los productos y cantidades asociados al pedido.
        pedido.getDetalles().addAll(listarDetalles(id));

        // Carga el historial de estados del pedido.
        pedido.getHistorial().addAll(listarHistorial(id));

        return pedido;
    }

    // Convierte diferentes formatos de fecha en un objeto Date.
    private Date convertirFecha(String texto) throws SQLException {

        // Comprueba que la fecha tenga contenido.
        if (texto == null || texto.trim().isEmpty()) {
            throw new SQLException(
                    "La fecha guardada en la base de datos está vacía."
            );
        }

        texto = texto.trim();

        // Intenta interpretar la fecha como milisegundos desde Unix.
        try {
            if (texto.matches("-?\\d+")) {
                return new Date(Long.parseLong(texto));
            }
        } catch (NumberFormatException e) {
            throw new SQLException(
                    "La fecha numérica no es válida: " + texto, e
            );
        }

        // Intenta interpretar el formato SQL habitual.
        try {
            return Timestamp.valueOf(texto);
        } catch (IllegalArgumentException ignored) {
            // Si falla, prueba con otros formatos.
        }

        // Intenta interpretar una fecha ISO con zona horaria UTC.
        try {
            return Date.from(Instant.parse(texto));
        } catch (Exception ignored) {
            // Si falla, prueba el formato ISO con offset.
        }

        try {
            return Date.from(
                    OffsetDateTime.parse(texto).toInstant()
            );
        } catch (Exception ignored) {
            // Si falla, prueba fecha y hora ISO sin zona horaria.
        }

        try {
            return Timestamp.valueOf(
                    LocalDateTime.parse(texto.replace(' ', 'T'))
            );
        } catch (Exception ignored) {
            // Ninguno de los formatos anteriores funciono.
        }

        // Informa que la fecha no pudo convertirse.
        throw new SQLException(
                "No se pudo interpretar la fecha almacenada: " + texto
        );
    }

    // Consulta los detalles asociados a un pedido.
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

        // Permite recuperar los datos de cada producto.
        ProductoDAO productoDAO = new ProductoDAO();

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {

                // Recorre todos los detalles encontrados.
                while (rs.next()) {

                    int idDetalle = rs.getInt("id");
                    int idProducto = rs.getInt("producto_id");
                    int cantidad = rs.getInt("cantidad");
                    double precioUnitario =
                            rs.getDouble("precio_unitario");
                    double subtotal = rs.getDouble("subtotal");

                    // Busca el producto asociado a este detalle.
                    Producto producto =
                            productoDAO.buscarPorId(idProducto);

                    // Informa si el producto asociado no existe.
                    if (producto == null) {
                        throw new SQLException(
                                "No se encontró el producto con ID "
                                + idProducto + " del pedido " + idPedido
                        );
                    }

                    // Construye el detalle con sus datos recuperados.
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

    // Recupera el historial de cambios de estado de un pedido.
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

                // Recorre los registros del historial.
                while (rs.next()) {

                    int idHistorial = rs.getInt("id");
                    String estado = rs.getString("estado");
                    String fechaTexto = rs.getString("fecha_hora");

                    // Convierte la fecha para conservarla en el objeto.
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

    // Actualiza el estado del pedido y registra el cambio en el historial.
    public boolean actualizarEstado(int id, String nuevoEstado)
            throws SQLException {

        // Rechaza un estado nulo.
        if (nuevoEstado == null) {
            return false;
        }

        // Normaliza el texto para aceptar variaciones de mayusculas y tildes.
        nuevoEstado = Normalizer.normalize(nuevoEstado.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase()
                .replaceAll("\\s+", "_");

        // Solo permite los estados definidos para los pedidos.
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

            // Agrupa la actualizacion, el historial y el estado de la mesa.
            conexion.setAutoCommit(false);

            try {

                String estadoActual;
                int idMesa;

                // Comprueba que el pedido exista y recupera su estado actual.
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

                // No permite cancelar un pedido que ya fue entregado.
                if ("ENTREGADO".equals(estadoActual)
                        && "CANCELADO".equals(nuevoEstado)) {
                    conexion.rollback();
                    return false;
                }

                // Evita registrar dos veces el mismo estado consecutivo.
                if (nuevoEstado.equals(estadoActual)) {
                    conexion.rollback();
                    return true;
                }

                // Actualiza el estado del pedido.
                try (PreparedStatement ps =
                        conexion.prepareStatement(sqlActualizar)) {

                    ps.setString(1, nuevoEstado);
                    ps.setInt(2, id);

                    if (ps.executeUpdate() == 0) {
                        conexion.rollback();
                        return false;
                    }
                }

                // Guarda la fecha exacta en que se realizó el cambio.
                String fechaActual =
                        String.valueOf(System.currentTimeMillis());

                try (PreparedStatement ps =
                        conexion.prepareStatement(sqlHistorial)) {

                    ps.setInt(1, id);
                    ps.setString(2, nuevoEstado);
                    ps.setString(3, fechaActual);

                    ps.executeUpdate();
                }

                // Libera la mesa si el pedido termina; de lo contrario, la ocupa.
                boolean terminado = nuevoEstado.equals("ENTREGADO")
                        || nuevoEstado.equals("CANCELADO");

                try (PreparedStatement ps = conexion.prepareStatement(
                        "UPDATE mesa SET estado = ? WHERE id = ?")) {
                    ps.setString(1, terminado ? "DISPONIBLE" : "OCUPADA");
                    ps.setInt(2, idMesa);
                    ps.executeUpdate();
                }

                // Confirma todas las operaciones realizadas.
                conexion.commit();
                return true;

            } catch (SQLException | RuntimeException e) {

                // Revierte todos los cambios si ocurre un error.
                conexion.rollback();
                throw e;

            } finally {
                // Restaura el modo normal de la conexion.
                conexion.setAutoCommit(true);
            }
        }
    }

    // Actualiza el mesero, la mesa y el total de un pedido existente.
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

            // Devuelve true si se actualizo algun registro.
            return ps.executeUpdate() > 0;
        }
    }

    // Actualiza el total de un pedido después de modificar sus detalles.
    public boolean actualizarTotal(int id, double total) throws SQLException {

        String sql = "UPDATE pedido SET total = ? WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setDouble(1, total);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;
        }
    }

    // Elimina el pedido, sus detalles y su historial en una transaccion.
    public boolean eliminar(int id) throws SQLException {

        try (Connection conexion = Conexion.conectar()) {

            conexion.setAutoCommit(false);

            try {
                String estado;
                int idMesa;

                // Consulta el estado y la mesa antes de borrar el pedido.
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

                // Borra primero los registros relacionados con el pedido.
                borrarPorId(conexion,
                        "DELETE FROM detalle_pedido WHERE pedido_id = ?", id);
                borrarPorId(conexion,
                        "DELETE FROM historial_estado WHERE pedido_id = ?", id);
                borrarPorId(conexion,
                        "DELETE FROM pedido WHERE id = ?", id);

                // Si el pedido estaba activo, libera la mesa.
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

                // Revierte las eliminaciones si alguna operacion falla.
                conexion.rollback();
                throw e;

            } finally {
                conexion.setAutoCommit(true);
            }
        }
    }

    // Ejecuta una instruccion de borrado usando una conexion existente.
    private void borrarPorId(Connection conexion, String sql, int id)
            throws SQLException {

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Consulta los pedidos que tienen un estado determinado.
    public List<Pedido> listarPorEstado(String estado) throws SQLException {

        List<Pedido> pedidos = new ArrayList<>();

        String sql = "SELECT * FROM pedido WHERE estado = ? ORDER BY id";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            // Normaliza el estado antes de ejecutar la consulta.
            ps.setString(1, estado.trim().toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                // Convierte cada resultado en un objeto Pedido.
                while (rs.next()) {
                    pedidos.add(convertirPedido(rs));
                }
            }
        }

        return pedidos;
    }

    // Lista los pedidos cuya fecha se encuentra dentro del rango indicado.
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

            // Define la fecha inicial y final en milisegundos.
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

    // Calcula las ventas sumando los pedidos entregados dentro del rango.
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
                // Devuelve la suma de ventas o cero si no hay resultados.
                return rs.next() ? rs.getDouble("ventas") : 0;
            }
        }
    }
}
