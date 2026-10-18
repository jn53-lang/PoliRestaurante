package com.co.poligran.polirestaurante.config;

import com.co.poligran.polirestaurante.dao.CategoriaDAO;
import com.co.poligran.polirestaurante.dao.MesaDAO;
import com.co.poligran.polirestaurante.dao.PedidoDAO;
import com.co.poligran.polirestaurante.dao.ProductoDAO;
import com.co.poligran.polirestaurante.dao.ReporteDAO;
import com.co.poligran.polirestaurante.dao.UsuarioDAO;
import java.sql.SQLException;

/**
 * Crea todas las tablas del proyecto en el orden correcto.
 * Es seguro llamarlo varias veces (usa CREATE TABLE IF NOT EXISTS),
 * asi que cada Manager puede invocarlo sin problema.
 */
public class BaseDatos {

    private static boolean inicializada = false;

    public static synchronized void inicializar() throws SQLException {
        if (inicializada) {
            return;
        }

        new CategoriaDAO().crearTabla();
        new ProductoDAO().crearTabla();
        new UsuarioDAO().crearTabla();
        new MesaDAO().crearTabla();
        new PedidoDAO().crearTablas();   // pedido, detalle_pedido, historial_estado
        new ReporteDAO().crearTabla();

        inicializada = true;
    }
}
