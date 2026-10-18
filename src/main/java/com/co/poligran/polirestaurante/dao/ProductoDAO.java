
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

    // Crea la tabla de productos si todavia no existe.
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

        // Abre la conexion, crea la tabla y revisa su estructura.
        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.executeUpdate();
            agregarColumnaCategoriaSiFalta(conexion);
        }
    }

    // Agrega categoria_id a bases de datos antiguas que no tienen esa columna.
    private void agregarColumnaCategoriaSiFalta(Connection conexion)
            throws SQLException {

        int existe;

        // Consulta si la columna categoria_id ya esta creada.
        try (Statement st = conexion.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT COUNT(*) FROM pragma_table_info('producto') "
                 + "WHERE name = 'categoria_id'")) {
            existe = rs.next() ? rs.getInt(1) : 0;
        }

        // Si no existe, la agrega a la tabla producto.
        if (existe == 0) {
            try (Statement st = conexion.createStatement()) {
                st.executeUpdate(
                    "ALTER TABLE producto ADD COLUMN categoria_id "
                    + "INTEGER REFERENCES categoria(id)");
            }
        }
    }

    // Guarda un producto con sus datos y su categoria.
    public boolean guardar(Producto producto) throws SQLException {
        String sql = """
            INSERT INTO producto
                (id, nombre, precio, descripcion, disponible, categoria_id)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            // Asigna los valores del producto a la consulta SQL.
            ps.setInt(1, producto.getId());
            ps.setString(2, producto.getNombre());
            ps.setDouble(3, producto.getPrecio());
            ps.setString(4, producto.getDescripcion());

            // Guarda la disponibilidad como 1 para true y 0 para false.
            ps.setInt(5, producto.isDisponible() ? 1 : 0);

            // Asigna el ID de la categoria o NULL si no tiene una.
            asignarCategoria(ps, 6, producto.getCategoria());

            // Devuelve true si se inserto algun registro.
            return ps.executeUpdate() > 0;
        }
    }

    // Consulta todos los productos ordenados por su ID.
    public List<Producto> listarTodos() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM producto ORDER BY id";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Convierte cada registro en un objeto Producto.
            while (rs.next()) {
                productos.add(convertirProducto(rs));
            }
        }

        return productos;
    }

    // Consulta los productos que pertenecen a una categoria especifica.
    public List<Producto> listarPorCategoria(int idCategoria)
            throws SQLException {

        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM producto WHERE categoria_id = ? ORDER BY id";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            // Indica el ID de la categoria que se quiere consultar.
            ps.setInt(1, idCategoria);

            try (ResultSet rs = ps.executeQuery()) {
                // Convierte y agrega cada producto encontrado.
                while (rs.next()) {
                    productos.add(convertirProducto(rs));
                }
            }
        }

        return productos;
    }

    // Busca un producto por su identificador.
    public Producto buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM producto WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                // Si existe, devuelve el producto convertido en objeto.
                if (rs.next()) {
                    return convertirProducto(rs);
                }
            }
        }

        // Devuelve null si no encuentra el producto.
        return null;
    }

    // Actualiza los datos de un producto existente.
    public boolean actualizar(Producto producto) throws SQLException {
        String sql = """
            UPDATE producto
            SET nombre = ?, precio = ?, descripcion = ?,
                disponible = ?, categoria_id = ?
            WHERE id = ?
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            // Asigna los nuevos valores del producto.
            ps.setString(1, producto.getNombre());
            ps.setDouble(2, producto.getPrecio());
            ps.setString(3, producto.getDescripcion());
            ps.setInt(4, producto.isDisponible() ? 1 : 0);

            // Actualiza la categoria y luego indica el ID del producto.
            asignarCategoria(ps, 5, producto.getCategoria());
            ps.setInt(6, producto.getId());

            // Devuelve true si se actualizo algun registro.
            return ps.executeUpdate() > 0;
        }
    }

    // Elimina un producto usando su ID.
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM producto WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            // Devuelve true si se elimino algun registro.
            return ps.executeUpdate() > 0;
        }
    }

    // Asigna la categoria al producto o guarda NULL si no tiene categoria.
    private void asignarCategoria(PreparedStatement ps, int posicion,
                                  Categoria categoria) throws SQLException {
        if (categoria != null) {
            // Guarda el identificador de la categoria relacionada.
            ps.setInt(posicion, categoria.getId());
        } else {
            // Indica que el producto no tiene una categoria asignada.
            ps.setNull(posicion, Types.INTEGER);
        }
    }

    // Convierte un registro de la base de datos en un objeto Producto.
    private Producto convertirProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto(
            rs.getInt("id"),
            rs.getString("nombre"),
            rs.getDouble("precio"),
            rs.getString("descripcion"),
            rs.getInt("disponible") == 1
        );

        Integer idCategoria = null;

        // Intenta recuperar el ID de la categoria asociada.
        try {
            int valor = rs.getInt("categoria_id");

            // wasNull comprueba si el valor de la columna era SQL NULL.
            if (!rs.wasNull()) {
                idCategoria = valor;
            }
        } catch (SQLException e) {
            // Permite leer bases de datos antiguas sin categoria_id.
        }

        // Si tiene categoria, recupera el objeto Categoria completo.
        if (idCategoria != null) {
            producto.setCategoria(new CategoriaDAO().buscarPorId(idCategoria));
        }

        return producto;
    }
}
