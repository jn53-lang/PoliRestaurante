
package com.co.poligran.polirestaurante.dao;

import com.co.poligran.polirestaurante.Entityes.Categoria;
import com.co.poligran.polirestaurante.config.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    // Crea la tabla de categorias si todavia no existe en SQLite.
    public void crearTabla() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS categoria (
                id INTEGER PRIMARY KEY,
                nombre TEXT NOT NULL,
                descripcion TEXT
            )
            """;

        // Abre la conexion y prepara la instruccion SQL.
        // try-with-resources cierra los recursos automaticamente.
        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    // Guarda una categoria utilizando los datos del objeto recibido.
    public boolean guardar(Categoria categoria) throws SQLException {
        String sql = """
            INSERT INTO categoria (id, nombre, descripcion)
            VALUES (?, ?, ?)
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            // Los signos ? se reemplazan por los valores de la categoria.
            ps.setInt(1, categoria.getId());
            ps.setString(2, categoria.getNombre());
            ps.setString(3, categoria.getDescripcion());

            // Retorna true si se inserto al menos un registro.
            return ps.executeUpdate() > 0;
        }
    }

    // Consulta todas las categorias y las devuelve en una lista.
    public List<Categoria> listarTodos() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT * FROM categoria ORDER BY id";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Recorre cada fila del resultado de la consulta.
            while (rs.next()) {
                Categoria categoria = new Categoria(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("descripcion")
                );

                // Agrega cada categoria encontrada a la lista.
                categorias.add(categoria);
            }
        }

        return categorias;
    }

    // Busca una categoria por su identificador.
    public Categoria buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM categoria WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            // Define el ID que se utilizara en la consulta.
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                // Si existe una fila, crea y devuelve el objeto Categoria.
                if (rs.next()) {
                    return new Categoria(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion")
                    );
                }
            }
        }

        // Devuelve null cuando no encuentra la categoria.
        return null;
    }

    // Modifica el nombre y la descripcion de una categoria existente.
    public boolean actualizar(Categoria categoria) throws SQLException {
        String sql = """
            UPDATE categoria
            SET nombre = ?, descripcion = ?
            WHERE id = ?
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            // Asigna los nuevos datos y al final el ID de la categoria.
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.setInt(3, categoria.getId());

            // Devuelve true si se actualizo algun registro.
            return ps.executeUpdate() > 0;
        }
    }

    // Elimina una categoria a partir de su ID.
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM categoria WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            // Devuelve true si se elimino algun registro.
            return ps.executeUpdate() > 0;
        }
    }
}
