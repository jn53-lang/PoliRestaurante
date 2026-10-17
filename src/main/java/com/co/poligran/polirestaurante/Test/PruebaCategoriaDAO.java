package com.co.poligran.polirestaurante.Test;

import com.co.poligran.polirestaurante.Entityes.Categoria;
import com.co.poligran.polirestaurante.dao.CategoriaDAO;
import java.sql.SQLException;

public class PruebaCategoriaDAO {

    public static void main(String[] args) {
        CategoriaDAO dao = new CategoriaDAO();

        try {
            dao.crearTabla();

            Categoria categoria = new Categoria(
                1,
                "Comidas rapidas",
                "Hamburguesas y perros calientes"
            );

            System.out.println("Categoria guardada: " + dao.guardar(categoria));

            for (Categoria c : dao.listarTodos()) {
                System.out.println(
                    c.getId() + " - " +
                    c.getNombre() + " - " +
                    c.getDescripcion()
                );
            }

        } catch (SQLException e) {
            System.out.println("Error al probar CategoriaDAO");
            e.printStackTrace();
        }
    }
}