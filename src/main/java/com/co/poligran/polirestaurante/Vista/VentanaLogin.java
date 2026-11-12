package com.co.poligran.polirestaurante.Vista;

import com.co.poligran.polirestaurante.Managers.AdminManager;
import com.co.poligran.polirestaurante.Entityes.Administrador;
import com.co.poligran.polirestaurante.Entityes.Cocina;
import com.co.poligran.polirestaurante.Entityes.Mesero;
import com.co.poligran.polirestaurante.Entityes.Usuario;
import javax.swing.JFrame;

/**
 * Ventana del inicio de sesion. Contiene el PanelLogin y decide
 * que se abre segun el rol del usuario que entra.
 */
public class VentanaLogin extends JFrame {

    private AdminManager admin;
    private PanelLogin panel;

    public VentanaLogin(AdminManager admin) {
        super("PoliRestaurante - Inicio de sesión");
        this.admin = admin;

        panel = new PanelLogin(admin, this::alIniciarSesion, this::abrirRegistro);

        setContentPane(panel);
        getRootPane().setDefaultButton(panel.getBotonIngresar());   // ENTER = iniciar sesion
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 520);
        setResizable(false);
        setLocationRelativeTo(null);   // centrada en la pantalla
    }

    public void mostrar() {
        setVisible(true);

        // Primer uso: si no hay usuarios, se obliga a crear un administrador.
        if (admin.listarUsuarios().isEmpty()) {
            Estilo.info(this, "No hay usuarios registrados.\nCree el primer administrador.");
            abrirRegistro();
        }
    }

    private void abrirRegistro() {
        boolean primero = admin.listarUsuarios().isEmpty();
        new DialogoRegistro(this, admin, primero).setVisible(true);
    }

    // Por ahora solo se confirma el ingreso. En las siguientes partes
    // aqui se abre la ventana del administrador.
    private void alIniciarSesion(Usuario usuario) {
        String detalle;

        if (usuario instanceof Administrador) {
            detalle = "El menú del administrador se agrega en la siguiente parte.";
        } else if (usuario instanceof Mesero) {
            detalle = "El panel del mesero lo desarrolla su compañero.";
        } else if (usuario instanceof Cocina) {
            detalle = "El panel de cocina lo desarrolla su compañero.";
        } else {
            detalle = "Este usuario no tiene un rol válido.";
        }

        Estilo.info(this, "Bienvenido, " + usuario.getNombre()
                + " (" + usuario.getRol() + ")\n\n" + detalle);

        admin.cerrarSesion(usuario);
    }
}
