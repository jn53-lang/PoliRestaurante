package com.co.poligran.polirestaurante.Vista;

import com.co.poligran.polirestaurante.Managers.AdminManager;
import com.co.poligran.polirestaurante.Entityes.Administrador;
import com.co.poligran.polirestaurante.Entityes.Cocina;
import com.co.poligran.polirestaurante.Entityes.Mesero;
import com.co.poligran.polirestaurante.Entityes.Usuario;
import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Unica ventana del programa. Todo lo que se ve (login, registro, menu del
 * administrador...) es un JPanel que se muestra aqui dentro, uno a la vez,
 * usando un CardLayout.
 */
public class VentanaPrincipal extends JFrame {

    private AdminManager admin;

    private CardLayout cartas = new CardLayout();
    private JPanel contenedor = new JPanel(cartas);

    private PanelLogin panelLogin;
    private PanelRegistro panelRegistro;   // se crea cada vez que se necesita
    private PanelAdmin panelAdmin;         // se crea al iniciar sesion

    private Usuario usuarioActual;

    public VentanaPrincipal(AdminManager admin) {
        super("PoliRestaurante");
        this.admin = admin;

        panelLogin = new PanelLogin(admin, this::alIniciarSesion,
                () -> mostrarRegistro(admin.listarUsuarios().isEmpty()));
        contenedor.add(panelLogin, "login");

        setContentPane(contenedor);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void mostrar() {
        mostrarLogin();
        setVisible(true);

        // Primer uso: si no hay usuarios, se obliga a crear un administrador.
        if (admin.listarUsuarios().isEmpty()) {
            mostrarRegistro(true);
        }
    }

    // ---------- Cambio de panel ----------

    private void mostrarLogin() {
        cartas.show(contenedor, "login");
        getRootPane().setDefaultButton(panelLogin.getBotonIngresar());   // ENTER = iniciar sesion
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

    // Cada panel tiene su tamano: el login es pequeno y el menu del admin es grande.
    private void ajustarVentana(String titulo, int ancho, int alto, boolean redimensionable) {
        setTitle(titulo);
        setResizable(redimensionable);
        setSize(ancho, alto);
        setLocationRelativeTo(null);   // centrada en la pantalla
    }

    // ---------- Sesion ----------

    private void alIniciarSesion(Usuario usuario) {
        usuarioActual = usuario;

        if (usuario instanceof Administrador) {
            mostrarAdmin(usuario);
            return;
        }

        // Los paneles del mesero y cocina los desarrolla el companero.
        String rol = (usuario instanceof Mesero) ? "mesero"
                : (usuario instanceof Cocina) ? "cocina" : "este rol";

        Estilo.info(this, "Bienvenido, " + usuario.getNombre()
                + ".\n\nEl panel de " + rol + " todavía no está disponible.");
        cerrarSesion();
    }

    private void cerrarSesion() {
        admin.cerrarSesion(usuarioActual);
        usuarioActual = null;

        if (panelAdmin != null) {
            contenedor.remove(panelAdmin);
            panelAdmin = null;
        }

        panelLogin.limpiar();
        mostrarLogin();
    }
}
