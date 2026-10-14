package com.co.poligran.polirestaurante.dao;

import com.co.poligran.polirestaurante.Entityes.Usuario;
import com.co.poligran.polirestaurante.config.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public void crearTabla() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS usuario (
                id INTEGER PRIMARY KEY,
                nombre TEXT NOT NULL,
                correo TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                rol TEXT NOT NULL
            )
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    public boolean guardar(Usuario usuario) throws SQLException {
        String sql = """
            INSERT INTO usuario (id, nombre, correo, password, rol)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, usuario.getId());
            ps.setString(2, usuario.getNombre());
            ps.setString(3, usuario.getCorreo());
            ps.setString(4, usuario.getPassword());
            ps.setString(5, usuario.getRol());

            return ps.executeUpdate() > 0;
        }
    }

    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario ORDER BY id";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Usuario usuario = new Usuario(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("correo"),
                    rs.getString("password"),
                    rs.getString("rol")
                );

                usuarios.add(usuario);
            }
        }

        return usuarios;
    }

    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("correo"),
                        rs.getString("password"),
                        rs.getString("rol")
                    );
                }
            }
        }

        return null;
    }

    public boolean actualizar(Usuario usuario) throws SQLException {
        String sql = """
            UPDATE usuario
            SET nombre = ?, correo = ?, password = ?, rol = ?
            WHERE id = ?
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getCorreo());
            ps.setString(3, usuario.getPassword());
            ps.setString(4, usuario.getRol());
            ps.setInt(5, usuario.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM usuario WHERE id = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    public Usuario buscarPorCorreo(String correo) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE correo = ?";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, correo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("correo"),
                        rs.getString("password"),
                        rs.getString("rol")
                    );
                }
            }
        }

        return null;
    }
}
