package com.co.poligran.polirestaurante.Main;

import com.co.poligran.polirestaurante.Managers.AdminManager;
import com.co.poligran.polirestaurante.Vista.Estilo;
import com.co.poligran.polirestaurante.Vista.VentanaLogin;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la version grafica.
 * (La version de consola sigue en PoliRestaurante.java)
 */
public class PoliRestauranteGUI {

    public static void main(String[] args) {
        Estilo.aplicarTema();

        // Las ventanas de Swing siempre se crean en el hilo de eventos.
        SwingUtilities.invokeLater(() -> {
            AdminManager admin = new AdminManager();
            new VentanaLogin(admin).mostrar();
        });
    }
}
