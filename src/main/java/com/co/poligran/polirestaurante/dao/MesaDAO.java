
package com.co.poligran.polirestaurante.dao;

import com.co.poligran.polirestaurante.Entityes.Mesa;
import com.co.poligran.polirestaurante.config.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MesaDAO {

    // Crea la tabla de mesas si todavia no existe.
    public void crearTabla() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS mesa (
                id INTEGER PRIMARY KEY,
                numero INTEGER NOT NULL UNIQUE,
                capacidad INTEGER NOT NULL,
                estado TEXT NOT NULL
            )
            """;

        // Abre la conexion y ejecuta la instruccion SQL.
        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.executeUpdate();
        }
    }

    // Guarda una mesa con sus datos en la base de datos.
    public boolean guardar(Mesa mesa) throws SQLException {
        String sql = """
            INSERT INTO mesa (id, numero, capacidad, estado)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            // Asigna los valores de la mesa a los parametros de la consulta.
            sentencia.setInt(1, mesa.getId());
            sentencia.setInt(2, mesa.getNumero());
            sentencia.setInt(3, mesa.getCapacidad());
            sentencia.setString(4, mesa.getEstado());

            // Devuelve true si se inserto algun registro.
            return sentencia.executeUpdate() > 0;
        }
    }

    // Consulta todas las mesas y las organiza por numero.
    public List<Mesa> listarTodos() throws SQLException {
        List<Mesa> mesas = new ArrayList<>();
        String sql = "SELECT * FROM mesa ORDER BY numero";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultados = sentencia.executeQuery()) {

            // Recorre todas las mesas encontradas.
            while (resultados.next()) {
                Mesa mesa = new Mesa(
                    resultados.getInt("id"),
                    resultados.getInt("numero"),
                    resultados.getInt("capacidad")
                );

                // Si la mesa esta ocupada, actualiza su estado en el objeto.
                if (Mesa.ESTADO_OCUPADA.equalsIgnoreCase(resultados.getString("estado"))) {
                    mesa.asignarMesa();
                }

                // Agrega la mesa a la lista.
                mesas.add(mesa);
            }
        }

        return mesas;
    }

    // Busca una mesa por su identificador.
    public Mesa buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM mesa WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            // Define el ID de la mesa que se quiere buscar.
            sentencia.setInt(1, id);

            try (ResultSet resultados = sentencia.executeQuery()) {

                // Si encuentra la mesa, crea y devuelve el objeto.
                if (resultados.next()) {
                    Mesa mesa = new Mesa(
                        resultados.getInt("id"),
                        resultados.getInt("numero"),
                        resultados.getInt("capacidad")
                    );

                    // Mantiene el estado de ocupacion en el objeto.
                    if (Mesa.ESTADO_OCUPADA.equalsIgnoreCase(resultados.getString("estado"))) {
                        mesa.asignarMesa();
                    }

                    return mesa;
                }
            }
        }

        // Devuelve null si no encuentra una mesa con ese ID.
        return null;
    }

    // Actualiza el estado de una mesa usando su ID.
    public boolean actualizarEstado(int id, String estado) throws SQLException {
        String sql = "UPDATE mesa SET estado = ? WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, estado);
            sentencia.setInt(2, id);

            // Devuelve true si se modifico algun registro.
            return sentencia.executeUpdate() > 0;
        }
    }

    // Elimina una mesa por su identificador.
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM mesa WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);

            // Devuelve true si se elimino algun registro.
            return sentencia.executeUpdate() > 0;
        }
    }

    // Actualiza el numero, la capacidad y el estado de una mesa.
    public boolean actualizar(Mesa mesa) throws SQLException {
        String sql = """
            UPDATE mesa
            SET numero = ?, capacidad = ?, estado = ?
            WHERE id = ?
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            // Asigna los nuevos datos y al final el ID de la mesa.
            sentencia.setInt(1, mesa.getNumero());
            sentencia.setInt(2, mesa.getCapacidad());
            sentencia.setString(3, mesa.getEstado());
            sentencia.setInt(4, mesa.getId());

            // Devuelve true si se actualizo algun registro.
            return sentencia.executeUpdate() > 0;
        }
    }
}
