package com.co.poligran.polirestaurante.dao;

import com.co.poligran.polirestaurante.Entityes.Reporte;
import com.co.poligran.polirestaurante.config.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReporteDAO {

    private static final String CREAR_TABLA = """
        CREATE TABLE IF NOT EXISTS reporte (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            fecha_inicio TEXT NOT NULL,
            fecha_fin TEXT NOT NULL,
            tipo TEXT NOT NULL
        )
        """;

    private static final String FORMATO_FECHA = "yyyy-MM-dd HH:mm:ss.SSS";

    private String convertirFecha(Date fecha) {
        return new SimpleDateFormat(FORMATO_FECHA).format(fecha);
    }

    private Date convertirFecha(String fecha) throws SQLException {
        try {
            return new SimpleDateFormat(FORMATO_FECHA).parse(fecha);
        } catch (ParseException e) {
            throw new SQLException("No se pudo convertir la fecha: " + fecha, e);
        }
    }

    public void crearTabla() throws SQLException {
        try (Connection conexion = Conexion.conectar();
             Statement sentencia = conexion.createStatement()) {
            sentencia.execute(CREAR_TABLA);
        }
    }

    public int guardar(Reporte reporte) throws SQLException {
        crearTabla();

        String sql = """
            INSERT INTO reporte (fecha_inicio, fecha_fin, tipo)
            VALUES (?, ?, ?)
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            sentencia.setString(1, convertirFecha(reporte.getFechaInicio()));
            sentencia.setString(2, convertirFecha(reporte.getFechaFin()));
            sentencia.setString(3, reporte.getTipo());

            sentencia.executeUpdate();

            try (ResultSet resultado = sentencia.getGeneratedKeys()) {
                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }

            try (Statement consulta = conexion.createStatement();
                 ResultSet resultado = consulta.executeQuery(
                         "SELECT last_insert_rowid()")) {
                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }
        }

        throw new SQLException("No se pudo obtener el ID del reporte guardado.");
    }

    public List<Reporte> listarTodos() throws SQLException {
        crearTabla();

        List<Reporte> reportes = new ArrayList<>();
        String sql = """
            SELECT id, fecha_inicio, fecha_fin, tipo
            FROM reporte
            ORDER BY id
            """;

        try (Connection conexion = Conexion.conectar();
             Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery(sql)) {

            while (resultado.next()) {
                Reporte reporte = new Reporte(
                    resultado.getInt("id"),
                    convertirFecha(resultado.getString("fecha_inicio")),
                    convertirFecha(resultado.getString("fecha_fin")),
                    resultado.getString("tipo")
                );

                reportes.add(reporte);
            }
        }

        return reportes;
    }

    public Reporte buscarPorId(int id) throws SQLException {
        crearTabla();

        String sql = """
            SELECT id, fecha_inicio, fecha_fin, tipo
            FROM reporte
            WHERE id = ?
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return new Reporte(
                        resultado.getInt("id"),
                        convertirFecha(resultado.getString("fecha_inicio")),
                        convertirFecha(resultado.getString("fecha_fin")),
                        resultado.getString("tipo")
                    );
                }
            }
        }

        return null;
    }

    public boolean eliminar(int id) throws SQLException {
        crearTabla();

        String sql = "DELETE FROM reporte WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);
            return sentencia.executeUpdate() > 0;
        }
    }
}