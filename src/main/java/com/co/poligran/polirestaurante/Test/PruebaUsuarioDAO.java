package com.co.poligran.polirestaurante.Test;

import com.co.poligran.polirestaurante.Entityes.Usuario;
import com.co.poligran.polirestaurante.dao.UsuarioDAO;
import java.sql.SQLException;

public class PruebaUsuarioDAO {

    public static void main(String[] args) {
        UsuarioDAO dao = new UsuarioDAO();

        try {
            dao.crearTabla();

            Usuario usuario = new Usuario(
                1,
                "Carlos Perez",
                "carlos@restaurante.com",
                "ClaveTemporal123",
                "MESERO"
            );

            Usuario existente = dao.buscarPorId(1);

if (existente == null) {
    System.out.println("Usuario guardado: " + dao.guardar(usuario));
} else {
    System.out.println("El usuario ya estaba guardado.");
}

            Usuario encontrado = dao.buscarPorCorreo("carlos@restaurante.com");

            if (encontrado != null) {
                System.out.println("ID: " + encontrado.getId());
                System.out.println("Nombre: " + encontrado.getNombre());
                System.out.println("Correo: " + encontrado.getCorreo());
                System.out.println("Rol: " + encontrado.getRol());
            } else {
                System.out.println("No se encontro el usuario");
            }

        } catch (SQLException e) {
            System.out.println("Error al probar UsuarioDAO");
            e.printStackTrace();
        }
    }
}