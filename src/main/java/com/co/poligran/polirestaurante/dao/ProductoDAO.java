package com.co.poligran.polirestaurante.dao;

import com.co.poligran.polirestaurante.Entityes.Categoria;
import com.co.poligran.polirestaurante.Entityes.Producto;
import com.co.poligran.polirestaurante.config.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public void crearTabla() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS producto (
                id INTEGER PRIMARY KEY,
                nombre TEXT NOT NULL,
                precio REAL NOT NULL,
                descripcion TEXT,
                disponible INTEGER NOT NULL,
                categoria_id INTEGER,
                FOREIGN KEY (categoria_id) REFERENCES categoria(id)
            )
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.executeUpdate();
            agregarColumnaCategoriaSiFalta(conexion);
        }
    }

    // Migracion: bases de datos creadas antes de existir categoria_id.
    private void agregarColumnaCategoriaSiFalta(Connection conexion)
            throws SQLException {

        int existe;

        try (Statement st = conexion.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT COUNT(*) FROM pragma_table_info('producto') "
                 + "WHERE name = 'categoria_id'")) {
            existe = rs.next() ? rs.getInt(1) : 0;
        }

        if (existe == 0) {
            try (Statement st = conexion.createStatement()) {
                st.executeUpdate(
                    "ALTER TABLE producto ADD COLUMN categoria_id "
                    + "INTEGER REFERENCES categoria(id)");
            }
        }
    }

    public boolean guardar(Producto producto) throws SQLException {
        String sql = """
            INSERT INTO producto
                (id, nombre, precio, descripcion, disponible, categoria_id)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, producto.getId());
            ps.setString(2, producto.getNombre());
            ps.setDouble(3, producto.getPrecio());
            ps.setString(4, producto.getDescripcion());
            ps.setInt(5, producto.isDisponible() ? 1 : 0);
            asignarCategoria(ps, 6, producto.getCategoria());

            return ps.executeUpdate() > 0;
        }
    }

    public List<Producto> listarTodos() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM producto ORDER BY id";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                productos.add(convertirProducto(rs));
            }
        }

        return productos;
    }

    public List<Producto> listarPorCategoria(int idCategoria)
            throws SQLException {

        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM producto WHERE categoria_id = ? ORDER BY id";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idCategoria);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(convertirProducto(rs));
                }
            }
        }

        return productos;
    }

    public Producto buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM producto WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return convertirProducto(rs);
                }
            }
        }

        return null;
    }

    public boolean actualizar(Producto producto) throws SQLException {
        String sql = """
            UPDATE producto
            SET nombre = ?, precio = ?, descripcion = ?,
                disponible = ?, categoria_id = ?
            WHERE id = ?
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, producto.getNombre());
            ps.setDouble(2, producto.getPrecio());
            ps.setString(3, producto.getDescripcion());
            ps.setInt(4, producto.isDisponible() ? 1 : 0);
            asignarCategoria(ps, 5, producto.getCategoria());
            ps.setInt(6, producto.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM producto WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    private void asignarCategoria(PreparedStatement ps, int posicion,
                                  Categoria categoria) throws SQLException {
        if (categoria != null) {
            ps.setInt(posicion, categoria.getId());
        } else {
            ps.setNull(posicion, Types.INTEGER);
        }
    }

    private Producto convertirProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto(
            rs.getInt("id"),
            rs.getString("nombre"),
            rs.getDouble("precio"),
            rs.getString("descripcion"),
            rs.getInt("disponible") == 1
        );

        Integer idCategoria = null;

        try {
            int valor = rs.getInt("categoria_id");
            if (!rs.wasNull()) {
                idCategoria = valor;
            }
        } catch (SQLException e) {
            // Base de datos antigua sin la columna categoria_id.
        }

        if (idCategoria != null) {
            producto.setCategoria(new CategoriaDAO().buscarPorId(idCategoria));
        }

        return producto;
    }
}
