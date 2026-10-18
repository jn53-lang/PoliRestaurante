package com.co.poligran.polirestaurante.Main;

import com.co.poligran.polirestaurante.BackEnd.AdminManager;
import com.co.poligran.polirestaurante.BackEnd.PedidoManager;
import com.co.poligran.polirestaurante.Entityes.Administrador;
import com.co.poligran.polirestaurante.Entityes.Cocina;
import com.co.poligran.polirestaurante.Entityes.Mesero;
import com.co.poligran.polirestaurante.Entityes.Usuario;
import com.co.poligran.polirestaurante.Front.PantallaAcceso;
import com.co.poligran.polirestaurante.Front.PantallaAdmin;
import com.co.poligran.polirestaurante.Front.PantallaPedidos;

public class PoliRestaurante {

    public static void main(String[] args) {

        // 1. Se crean los dos Managers (cada uno prepara la base de datos si hace falta).
        AdminManager adminManager = new AdminManager();
        PedidoManager pedidoManager = new PedidoManager();

        // 2. Se crean las pantallas que usan esos Managers.
        PantallaAcceso acceso = new PantallaAcceso(adminManager);
        PantallaPedidos pantallaPedidos = new PantallaPedidos(pedidoManager, adminManager);

        System.out.println("Bienvenido al Poli Restaurante");
        System.out.println("--------------------------------------------------");

        // 3. Mientras alguien inicie sesion, se le muestra el menu de su rol.
        while (true) {
            Usuario usuario = acceso.iniciar();

            if (usuario == null) {
                break;   // eligio salir
            }

            // Polimorfismo: el tipo de objeto decide que menu se abre.
            if (usuario instanceof Administrador) {
                new PantallaAdmin(adminManager, pantallaPedidos, usuario).mostrarMenu();
            } else if (usuario instanceof Mesero) {
                pantallaPedidos.menuMesero(usuario);
            } else if (usuario instanceof Cocina) {
                pantallaPedidos.menuCocina();
            } else {
                System.out.println("Este usuario no tiene un rol valido.");
            }

            adminManager.cerrarSesion(usuario);
        }

        System.out.println("Hasta pronto.");
    }
}
