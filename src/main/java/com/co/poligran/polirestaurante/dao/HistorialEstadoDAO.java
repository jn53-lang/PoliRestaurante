package com.co.poligran.polirestaurante.dao;

import com.co.poligran.polirestaurante.Entityes.HistorialEstado;
import com.co.poligran.polirestaurante.config.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class HistorialEstadoDAO {

    public void crearTabla() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS historial_estado (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pedido_id INTEGER NOT NULL,
                estado TEXT NOT NULL,
                fecha_hora TEXT NOT NULL,
                FOREIGN KEY (pedido_id) REFERENCES pedido(id)
            )
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.executeUpdate();
        }
    }

    public int guardar(int idPedido, HistorialEstado historial)
            throws SQLException {

        String sql = """
            INSERT INTO historial_estado (pedido_id, estado, fecha_hora)
            VALUES (?, ?, ?)
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPedido);
            sentencia.setString(2, historial.getEstado());

            String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                    .format(historial.getFechaHora());

            sentencia.setString(3, fecha);
            sentencia.executeUpdate();

            try (PreparedStatement consulta =
                     conexion.prepareStatement("SELECT last_insert_rowid()");
                 ResultSet resultado = consulta.executeQuery()) {

                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }
        }

        throw new SQLException("No se pudo obtener el ID del historial.");
    }

    public List<HistorialEstado> listarPorPedido(int idPedido)
            throws SQLException {

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

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {

                    Date fechaHora =
                        parsearFecha(resultado.getString("fecha_hora"));

                    historial.add(new HistorialEstado(
                        resultado.getInt("id"),
                        resultado.getString("estado"),
                        fechaHora
                    ));
                }
            }
        }

        return historial;
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM historial_estado WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);
            return sentencia.executeUpdate() > 0;
        }
    }


    // Acepta milisegundos ("1791935898156") o texto "yyyy-MM-dd HH:mm:ss.SSS".
    private Date parsearFecha(String fechaTexto) throws SQLException {
        try {
            if (fechaTexto.matches("\\d+")) {
                return new Date(Long.parseLong(fechaTexto));
            }
            try {
                return Timestamp.valueOf(fechaTexto);
            } catch (IllegalArgumentException e) {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                        .parse(fechaTexto);
            }
        } catch (ParseException | NumberFormatException e) {
            throw new SQLException(
                "Formato de fecha inválido: " + fechaTexto, e
            );
        }
    }

    // Promedio en minutos entre EN_PREPARACION y LISTO de cada pedido
    // cuya preparacion empezo dentro del rango. Devuelve -1 si no hay datos.
    public double tiempoPromedioPreparacionMinutos(Date inicio, Date fin)
            throws SQLException {

        String sql = """
            SELECT pedido_id, estado, fecha_hora
            FROM historial_estado
            WHERE estado IN ('EN_PREPARACION', 'LISTO')
            ORDER BY id
            """;

        Map<Integer, Long> inicioPreparacion = new HashMap<>();
        long sumaMilisegundos = 0;
        int cantidad = 0;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                int idPedido = resultado.getInt("pedido_id");
                long momento =
                    parsearFecha(resultado.getString("fecha_hora")).getTime();

                if ("EN_PREPARACION".equals(resultado.getString("estado"))) {
                    inicioPreparacion.putIfAbsent(idPedido, momento);
                } else {
                    Long comienzo = inicioPreparacion.remove(idPedido);

                    if (comienzo != null
                            && comienzo >= inicio.getTime()
                            && comienzo <= fin.getTime()) {
                        sumaMilisegundos += momento - comienzo;
                        cantidad++;
                    }
                }
            }
        }

        return cantidad == 0 ? -1 : (sumaMilisegundos / 60000.0) / cantidad;
    }
}
