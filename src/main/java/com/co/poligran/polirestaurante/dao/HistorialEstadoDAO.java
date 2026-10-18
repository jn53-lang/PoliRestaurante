
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

    // Crea la tabla para almacenar los cambios de estado de los pedidos.
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

        // Abre la conexión y ejecuta la creación de la tabla.
        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.executeUpdate();
        }
    }

    // Guarda un cambio de estado asociado a un pedido y devuelve su ID.
    public int guardar(int idPedido, HistorialEstado historial)
            throws SQLException {

        String sql = """
            INSERT INTO historial_estado (pedido_id, estado, fecha_hora)
            VALUES (?, ?, ?)
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            // Asigna el ID del pedido y el estado que se va a registrar.
            sentencia.setInt(1, idPedido);
            sentencia.setString(2, historial.getEstado());

            // Convierte la fecha y hora a texto para guardarla en SQLite.
            String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                    .format(historial.getFechaHora());

            sentencia.setString(3, fecha);

            // Inserta el registro del historial.
            sentencia.executeUpdate();

            // Recupera el ID generado para el nuevo registro.
            try (PreparedStatement consulta =
                     conexion.prepareStatement("SELECT last_insert_rowid()");
                 ResultSet resultado = consulta.executeQuery()) {

                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }
        }

        // Informa si no fue posible recuperar el ID generado.
        throw new SQLException("No se pudo obtener el ID del historial.");
    }

    // Consulta todos los cambios de estado realizados a un pedido.
    public List<HistorialEstado> listarPorPedido(int idPedido)
            throws SQLException {

        // Lista que almacenará los registros encontrados.
        List<HistorialEstado> historial = new ArrayList<>();

        // Consulta el historial del pedido ordenado por ID.
        String sql = """
            SELECT id, estado, fecha_hora
            FROM historial_estado
            WHERE pedido_id = ?
            ORDER BY id
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            // Indica el pedido del que se desea consultar el historial.
            sentencia.setInt(1, idPedido);

            try (ResultSet resultado = sentencia.executeQuery()) {
                // Recorre cada registro encontrado.
                while (resultado.next()) {

                    // Convierte la fecha almacenada a un objeto Date.
                    Date fechaHora =
                        parsearFecha(resultado.getString("fecha_hora"));

                    // Crea el objeto con el ID, el estado y la fecha registrados.
                    historial.add(new HistorialEstado(
                        resultado.getInt("id"),
                        resultado.getString("estado"),
                        fechaHora
                    ));
                }
            }
        }

        // Devuelve el historial completo del pedido.
        return historial;
    }

    // Elimina un registro del historial mediante su ID.
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM historial_estado WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            // Asigna el ID del registro que se desea eliminar.
            sentencia.setInt(1, id);

            // Devuelve true si se eliminó algún registro.
            return sentencia.executeUpdate() > 0;
        }
    }

    // Acepta fechas en milisegundos o con formato "yyyy-MM-dd HH:mm:ss.SSS".
    private Date parsearFecha(String fechaTexto) throws SQLException {
        try {
            // Si el valor contiene únicamente números, se interpreta como milisegundos.
            if (fechaTexto.matches("\\d+")) {
                return new Date(Long.parseLong(fechaTexto));
            }

            // Intenta convertir el texto directamente a Timestamp.
            try {
                return Timestamp.valueOf(fechaTexto);
            } catch (IllegalArgumentException e) {
                // Si falla, intenta convertirlo con el formato de fecha establecido.
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                        .parse(fechaTexto);
            }
        } catch (ParseException | NumberFormatException e) {
            // Informa que la fecha almacenada no tiene un formato válido.
            throw new SQLException(
                "Formato de fecha inválido: " + fechaTexto, e
            );
        }
    }

    // Calcula el tiempo promedio de preparación de los pedidos en minutos.
    // Considera el intervalo entre EN_PREPARACION y LISTO.
    // Devuelve -1 si no encuentra datos válidos dentro del rango.
    public double tiempoPromedioPreparacionMinutos(Date inicio, Date fin)
            throws SQLException {

        // Consulta los registros relacionados con el inicio y el final
        // de la preparación, ordenados cronológicamente por su ID.
        String sql = """
            SELECT pedido_id, estado, fecha_hora
            FROM historial_estado
            WHERE estado IN ('EN_PREPARACION', 'LISTO')
            ORDER BY id
            """;

        // Guarda la hora de inicio de preparación de cada pedido.
        Map<Integer, Long> inicioPreparacion = new HashMap<>();

        // Acumula los tiempos de preparación y cuenta los pedidos válidos.
        long sumaMilisegundos = 0;
        int cantidad = 0;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            // Recorre los registros de preparación y finalización.
            while (resultado.next()) {
                int idPedido = resultado.getInt("pedido_id");

                // Convierte la fecha del registro a milisegundos.
                long momento =
                    parsearFecha(resultado.getString("fecha_hora")).getTime();

                // Registra la primera hora en que el pedido entró en preparación.
                if ("EN_PREPARACION".equals(resultado.getString("estado"))) {
                    inicioPreparacion.putIfAbsent(idPedido, momento);
                } else {
                    // Recupera la hora de inicio cuando el pedido pasa a LISTO.
                    Long comienzo = inicioPreparacion.remove(idPedido);

                    // Solo cuenta pedidos cuyo inicio esté dentro del rango solicitado.
                    if (comienzo != null
                            && comienzo >= inicio.getTime()
                            && comienzo <= fin.getTime()) {

                        // Suma la duración de la preparación en milisegundos.
                        sumaMilisegundos += momento - comienzo;
                        cantidad++;
                    }
                }
            }
        }

        // Convierte el tiempo acumulado a minutos y calcula el promedio.
        // Si no hay pedidos válidos, devuelve -1.
        return cantidad == 0 ? -1 : (sumaMilisegundos / 60000.0) / cantidad;
    }
}
