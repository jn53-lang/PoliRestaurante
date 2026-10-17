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

    public void crearTabla() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS mesa (
                id INTEGER PRIMARY KEY,
                numero INTEGER NOT NULL UNIQUE,
                capacidad INTEGER NOT NULL,
                estado TEXT NOT NULL
            )
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.executeUpdate();
        }
    }

    public boolean guardar(Mesa mesa) throws SQLException {
        String sql = """
            INSERT INTO mesa (id, numero, capacidad, estado)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, mesa.getId());
            sentencia.setInt(2, mesa.getNumero());
            sentencia.setInt(3, mesa.getCapacidad());
            sentencia.setString(4, mesa.getEstado());

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<Mesa> listarTodos() throws SQLException {
        List<Mesa> mesas = new ArrayList<>();
        String sql = "SELECT * FROM mesa ORDER BY numero";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultados = sentencia.executeQuery()) {

            while (resultados.next()) {
                Mesa mesa = new Mesa(
                    resultados.getInt("id"),
                    resultados.getInt("numero"),
                    resultados.getInt("capacidad")
                );

                if (Mesa.ESTADO_OCUPADA.equalsIgnoreCase(resultados.getString("estado"))) {
                    mesa.asignarMesa();
                }

                mesas.add(mesa);
            }
        }

        return mesas;
    }

    public Mesa buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM mesa WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);

            try (ResultSet resultados = sentencia.executeQuery()) {
                if (resultados.next()) {
                    Mesa mesa = new Mesa(
                        resultados.getInt("id"),
                        resultados.getInt("numero"),
                        resultados.getInt("capacidad")
                    );

                    if (Mesa.ESTADO_OCUPADA.equalsIgnoreCase(resultados.getString("estado"))) {
                        mesa.asignarMesa();
                    }

                    return mesa;
                }
            }
        }

        return null;
    }

    public boolean actualizarEstado(int id, String estado) throws SQLException {
        String sql = "UPDATE mesa SET estado = ? WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, estado);
            sentencia.setInt(2, id);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM mesa WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);

            return sentencia.executeUpdate() > 0;
        }
    }


    public boolean actualizar(Mesa mesa) throws SQLException {
        String sql = """
            UPDATE mesa
            SET numero = ?, capacidad = ?, estado = ?
            WHERE id = ?
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, mesa.getNumero());
            sentencia.setInt(2, mesa.getCapacidad());
            sentencia.setString(3, mesa.getEstado());
            sentencia.setInt(4, mesa.getId());

            return sentencia.executeUpdate() > 0;
        }
    }
}
