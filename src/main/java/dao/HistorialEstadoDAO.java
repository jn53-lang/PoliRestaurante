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

                    String fechaTexto = resultado.getString("fecha_hora");
                    Date fechaHora;

                    try {
                        if (fechaTexto.matches("\\d+")) {
                            fechaHora = new Date(Long.parseLong(fechaTexto));
                        } else {
                            try {
                                fechaHora = Timestamp.valueOf(fechaTexto);
                            } catch (IllegalArgumentException e) {
                                fechaHora = new SimpleDateFormat(
                                    "yyyy-MM-dd HH:mm:ss.SSS"
                                ).parse(fechaTexto);
                            }
                        }
                    } catch (ParseException | NumberFormatException e) {
                        throw new SQLException(
                            "Formato de fecha inválido: " + fechaTexto, e
                        );
                    }

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
}