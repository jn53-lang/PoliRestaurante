
package com.co.poligran.polirestaurante.Main;

import com.co.poligran.polirestaurante.config.Conexion;
import java.sql.Connection;
import java.sql.SQLException;

public class PruebaConexion {

    public static void main(String[] args) {
        try (Connection conexion = Conexion.conectar()) {
            System.out.println("Conexion a SQLite exitosa.");
        } catch (SQLException e) {
            System.out.println("Error al conectar con SQLite:");
            e.printStackTrace();
        }
    }
}
