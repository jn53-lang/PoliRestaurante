package com.co.poligran.polirestaurante.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL = "jdbc:sqlite:polirestaurante.db";

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}

