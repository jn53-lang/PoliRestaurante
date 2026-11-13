
package com.co.poligran.polirestaurante.Vista;

import com.co.poligran.polirestaurante.Managers.AdminManager;
import com.co.poligran.polirestaurante.Managers.PedidoManager;
import com.co.poligran.polirestaurante.Entityes.Administrador;
import com.co.poligran.polirestaurante.Entityes.Cocina;
import com.co.poligran.polirestaurante.Entityes.Mesero;
import com.co.poligran.polirestaurante.Entityes.Usuario;
import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class VentanaPrincipal extends JFrame {

    private AdminManager admin;
    private PedidoManager pedidoManager;

    private CardLayout cartas = new CardLayout();
    private JPanel contenedor = new JPanel(cartas);

    private PanelLogin panelLogin;
    private PanelRegistro panelRegistro;
    private PanelAdmin panelAdmin;
    private PanelPedidos panelPedidos;

    private Usuario usuarioActual;

    public VentanaPrincipal(AdminManager admin) {
        super("PoliRestaurante");

        this.admin = admin;
        this.pedidoManager = new PedidoManager();

        panelLogin = new PanelLogin(admin, this::alIniciarSesion,
                () -> mostrarRegistro(admin.listarUsuarios().isEmpty()));

        contenedor.add(panelLogin, "login");

        setContentPane(contenedor);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void mostrar() {
        mostrarLogin();
        setVisible(true);

        if (admin.listarUsuarios().isEmpty()) {
            mostrarRegistro(true);
        }
    }

    private void mostrarLogin() {
        cartas.show(contenedor, "login");
        getRootPane().setDefaultButton(panelLogin.getBotonIngresar());
        ajustarVentana("PoliRestaurante - Inicio de sesión", 420, 520, false);
    }

    private void mostrarRegistro(boolean primerAdministrador) {
        if (panelRegistro != null) {
            contenedor.remove(panelRegistro);
        }

        panelRegistro = new PanelRegistro(admin, primerAdministrador, this::mostrarLogin);
        contenedor.add(panelRegistro, "registro");

        cartas.show(contenedor, "registro");
        getRootPane().setDefaultButton(null);

        ajustarVentana("PoliRestaurante - Crear usuario", 420, 680, false);
    }

    private void mostrarAdmin(Usuario usuario) {
        panelAdmin = new PanelAdmin(admin, usuario, this::cerrarSesion);
        contenedor.add(panelAdmin, "admin");

        cartas.show(contenedor, "admin");
        getRootPane().setDefaultButton(null);

        ajustarVentana("PoliRestaurante - Administrador", 1000, 650, true);
    }

    private void mostrarPedidos(Usuario usuario) {
        panelPedidos = new PanelPedidos(pedidoManager, admin, usuario);
        contenedor.add(panelPedidos, "pedidos");

        cartas.show(contenedor, "pedidos");
        getRootPane().setDefaultButton(null);

        ajustarVentana("PoliRestaurante - Gestión de pedidos", 1100, 700, true);
    }

    private void ajustarVentana(String titulo, int ancho, int alto, boolean redimensionable) {
        setTitle(titulo);
        setResizable(redimensionable);
        setSize(ancho, alto);
        setLocationRelativeTo(null);
    }

    private void alIniciarSesion(Usuario usuario) {
        usuarioActual = usuario;

        if (usuario instanceof Administrador) {
            mostrarAdmin(usuario);
        } else if (usuario instanceof Mesero || usuario instanceof Cocina) {
            mostrarPedidos(usuario);
        } else {
            Estilo.error(this, "Este usuario no tiene un rol válido.");
            cerrarSesion();
        }
    }

    private void cerrarSesion() {
        admin.cerrarSesion(usuarioActual);
        usuarioActual = null;

        if (panelAdmin != null) {
            contenedor.remove(panelAdmin);
            panelAdmin = null;
        }

        if (panelPedidos != null) {
            contenedor.remove(panelPedidos);
            panelPedidos = null;
        }

        panelLogin.limpiar();
        mostrarLogin();
    }
}

