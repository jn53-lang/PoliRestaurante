package com.co.poligran.polirestaurante.Vista;

import com.co.poligran.polirestaurante.Managers.AdminManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Panel para crear un usuario (reemplaza al antiguo cuadro de dialogo).
 * Si es el primer uso del programa (no hay usuarios) solo permite crear
 * un Administrador. En los demas casos solo Mesero o Cocina; los
 * administradores nuevos los crea otro administrador desde su menu.
 */
public class PanelRegistro extends JPanel {

    private AdminManager admin;
    private Runnable alTerminar;   // que hacer al crear el usuario o al volver

    private JTextField txtNombre;
    private JTextField txtCorreo;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmacion;
    private JComboBox<String> cmbRol;

    public PanelRegistro(AdminManager admin, boolean primerAdministrador, Runnable alTerminar) {
        this.admin = admin;
        this.alTerminar = alTerminar;

        txtNombre = Estilo.crearCampo();
        txtCorreo = Estilo.crearCampo();
        txtPassword = Estilo.crearCampoPassword();
        txtConfirmacion = Estilo.crearCampoPassword();

        if (primerAdministrador) {
            cmbRol = new JComboBox<>(new String[]{"Administrador"});
        } else {
            cmbRol = new JComboBox<>(new String[]{"Mesero", "Cocina"});
        }
        cmbRol.setFont(Estilo.FUENTE_NORMAL);

        String titulo = primerAdministrador ? "Primer administrador" : "Crear usuario";
        String subtitulo = primerAdministrador
                ? "Aún no hay usuarios: cree el administrador"
                : "Complete los datos de la cuenta";

        setLayout(new BorderLayout());
        add(Estilo.crearEncabezado(titulo, subtitulo), BorderLayout.NORTH);
        add(crearFormulario(primerAdministrador), BorderLayout.CENTER);
    }

    private JPanel crearFormulario(boolean primerAdministrador) {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(Estilo.COLOR_FONDO);
        formulario.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        String[] etiquetas = {"Nombre", "Correo", "Contraseña (mínimo 6 caracteres)", "Confirmar contraseña", "Rol"};
        Component[] campos = {txtNombre, txtCorreo, txtPassword, txtConfirmacion, cmbRol};

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < etiquetas.length; i++) {
            gbc.gridy = i * 2;
            gbc.insets = new Insets(0, 0, 3, 0);
            formulario.add(Estilo.crearEtiqueta(etiquetas[i]), gbc);

            gbc.gridy = i * 2 + 1;
            gbc.insets = new Insets(0, 0, 10, 0);
            formulario.add(campos[i], gbc);
        }

        JButton btnCrear = Estilo.crearBoton("Crear usuario", Estilo.COLOR_PRIMARIO);
        btnCrear.addActionListener(e -> crearUsuario());

        gbc.gridy = etiquetas.length * 2;
        gbc.insets = new Insets(10, 0, 8, 0);
        formulario.add(btnCrear, gbc);

        // En el primer uso no se puede volver: hay que crear el administrador.
        if (!primerAdministrador) {
            JButton btnVolver = Estilo.crearBoton("Volver", Estilo.COLOR_SECUNDARIO);
            btnVolver.addActionListener(e -> alTerminar.run());

            gbc.gridy = etiquetas.length * 2 + 1;
            gbc.insets = new Insets(0, 0, 0, 0);
            formulario.add(btnVolver, gbc);
        }

        return formulario;
    }

    private void crearUsuario() {
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmacion = new String(txtConfirmacion.getPassword());
        String rol = (String) cmbRol.getSelectedItem();

        if (!password.equals(confirmacion)) {
            Estilo.error(this, "Las contraseñas no coinciden.");
            return;
        }

        String mensaje = admin.registrarUsuario(nombre, correo, password, rol);

        if (mensaje.startsWith("Usuario creado")) {
            Estilo.info(this, mensaje);
            alTerminar.run();
        } else {
            Estilo.error(this, mensaje);
        }
    }
}
