package com.co.poligran.polirestaurante.Test;

import com.co.poligran.polirestaurante.Entityes.Producto;
import com.co.poligran.polirestaurante.dao.ProductoDAO;
import java.sql.SQLException;
import java.util.List;

public class PruebaProductoDAO {

    public static void main(String[] args) {
        ProductoDAO dao = new ProductoDAO();

        try {
            dao.crearTabla();

            Producto producto = new Producto(
                1,
                "Hamburguesa",
                18000,
                "Hamburguesa de carne",
                true
            );

            boolean guardado = dao.guardar(producto);
            System.out.println("Producto guardado: " + guardado);

            List<Producto> productos = dao.listarTodos();

            for (Producto p : productos) {
                System.out.println(
                    p.getId() + " - " +
                    p.getNombre() + " - $" +
                    p.getPrecio() + " - " +
                    p.isDisponible()
                );
            }

        } catch (SQLException e) {
            System.out.println("Error al probar el ProductoDAO");
            e.printStackTrace();
        }
    }
}