
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

    // Sentencia SQL para crear la tabla de reportes si todavía no existe.
    private static final String CREAR_TABLA = """
        CREATE TABLE IF NOT EXISTS reporte (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            fecha_inicio TEXT NOT NULL,
            fecha_fin TEXT NOT NULL,
            tipo TEXT NOT NULL
        )
        """;

    // Formato utilizado para guardar y recuperar las fechas de los reportes.
    private static final String FORMATO_FECHA = "yyyy-MM-dd HH:mm:ss.SSS";

    // Convierte una fecha de Java a texto para almacenarla en la base de datos.
    private String convertirFecha(Date fecha) {
        return new SimpleDateFormat(FORMATO_FECHA).format(fecha);
    }

    // Convierte el texto almacenado en la base de datos a una fecha de Java.
    // Si el texto no tiene el formato esperado, genera una excepción SQL.
    private Date convertirFecha(String fecha) throws SQLException {
        try {
            return new SimpleDateFormat(FORMATO_FECHA).parse(fecha);
        } catch (ParseException e) {
            throw new SQLException("No se pudo convertir la fecha: " + fecha, e);
        }
    }

    // Crea la tabla reporte utilizando la conexión con la base de datos.
    public void crearTabla() throws SQLException {
        try (Connection conexion = Conexion.conectar();
             Statement sentencia = conexion.createStatement()) {
            sentencia.execute(CREAR_TABLA);
        }
    }

    // Guarda un reporte y devuelve el ID generado para el registro.
    public int guardar(Reporte reporte) throws SQLException {
        // Se asegura de que la tabla exista antes de insertar información.
        crearTabla();

        // Consulta SQL para insertar las fechas y el tipo de reporte.
        String sql = """
            INSERT INTO reporte (fecha_inicio, fecha_fin, tipo)
            VALUES (?, ?, ?)
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            // Asigna los valores del reporte a los parámetros de la consulta.
            sentencia.setString(1, convertirFecha(reporte.getFechaInicio()));
            sentencia.setString(2, convertirFecha(reporte.getFechaFin()));
            sentencia.setString(3, reporte.getTipo());

            // Ejecuta la inserción del reporte.
            sentencia.executeUpdate();

            // Intenta recuperar directamente la clave generada por la base de datos.
            try (ResultSet resultado = sentencia.getGeneratedKeys()) {
                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }

            // Si no se obtuvo la clave anterior, consulta el último ID insertado.
            try (Statement consulta = conexion.createStatement();
                 ResultSet resultado = consulta.executeQuery(
                         "SELECT last_insert_rowid()")) {
                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }
        }

        // Informa que no fue posible recuperar el ID del reporte guardado.
        throw new SQLException("No se pudo obtener el ID del reporte guardado.");
    }

    // Obtiene todos los reportes registrados, ordenados por su ID.
    public List<Reporte> listarTodos() throws SQLException {
        // Garantiza que la tabla exista antes de realizar la consulta.
        crearTabla();

        // Lista donde se almacenarán los reportes recuperados.
        List<Reporte> reportes = new ArrayList<>();

        // Consulta para recuperar los datos de todos los reportes.
        String sql = """
            SELECT id, fecha_inicio, fecha_fin, tipo
            FROM reporte
            ORDER BY id
            """;

        try (Connection conexion = Conexion.conectar();
             Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery(sql)) {

            // Recorre cada registro obtenido de la base de datos.
            while (resultado.next()) {
                // Crea un objeto Reporte con los datos del registro actual.
                Reporte reporte = new Reporte(
                    resultado.getInt("id"),
                    convertirFecha(resultado.getString("fecha_inicio")),
                    convertirFecha(resultado.getString("fecha_fin")),
                    resultado.getString("tipo")
                );

                // Añade el reporte a la lista de resultados.
                reportes.add(reporte);
            }
        }

        // Devuelve la lista completa de reportes.
        return reportes;
    }

    // Busca un reporte específico utilizando su ID.
    public Reporte buscarPorId(int id) throws SQLException {
        // Garantiza que la tabla exista antes de buscar.
        crearTabla();

        // Consulta para recuperar el reporte que coincida con el ID.
        String sql = """
            SELECT id, fecha_inicio, fecha_fin, tipo
            FROM reporte
            WHERE id = ?
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            // Asigna el ID que se desea buscar.
            sentencia.setInt(1, id);

            // Ejecuta la consulta y revisa si existe un resultado.
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    // Si lo encuentra, construye y devuelve el objeto Reporte.
                    return new Reporte(
                        resultado.getInt("id"),
                        convertirFecha(resultado.getString("fecha_inicio")),
                        convertirFecha(resultado.getString("fecha_fin")),
                        resultado.getString("tipo")
                    );
                }
            }
        }

        // Devuelve null cuando no existe un reporte con ese ID.
        return null;
    }

    // Elimina un reporte de la base de datos utilizando su ID.
    public boolean eliminar(int id) throws SQLException {
        // Garantiza que la tabla exista antes de eliminar.
        crearTabla();

        // Consulta SQL para eliminar el registro seleccionado.
        String sql = "DELETE FROM reporte WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            // Asigna el ID del reporte que se desea eliminar.
            sentencia.setInt(1, id);

            // Devuelve true si se eliminó algún registro; de lo contrario, false.
            return sentencia.executeUpdate() > 0;
        }
    }
}
