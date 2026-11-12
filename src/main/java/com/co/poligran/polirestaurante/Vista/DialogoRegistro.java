package com.co.poligran.polirestaurante.Vista;

import com.co.poligran.polirestaurante.Managers.AdminManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Ventana para crear un usuario. Si es el primer uso del programa
 * (no hay usuarios), solo permite crear un Administrador.
 * En los demas casos solo se pueden crear Mesero o Cocina; los
 * administradores nuevos los crea otro administrador desde su menu.
 */
public class DialogoRegistro extends JDialog {

    private AdminManager admin;

    private JTextField txtNombre;
    private JTextField txtCorreo;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmacion;
    private JComboBox<String> cmbRol;

    public DialogoRegistro(Frame padre, AdminManager admin, boolean primerAdministrador) {
        super(padre, primerAdministrador ? "Crear el primer administrador" : "Crear usuario", true);
        this.admin = admin;

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

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setBackground(Estilo.COLOR_FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        contenido.add(crearFormulario(), BorderLayout.CENTER);
        contenido.add(crearBotones(), BorderLayout.SOUTH);

        setContentPane(contenido);
        pack();
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    private JPanel crearFormulario() {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);

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
            gbc.insets = new Insets(0, 0, 12, 0);
            formulario.add(campos[i], gbc);
        }

        return formulario;
    }

    private JPanel crearBotones() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);

        JButton btnCancelar = Estilo.crearBoton("Cancelar", Estilo.COLOR_SECUNDARIO);
        JButton btnCrear = Estilo.crearBoton("Crear", Estilo.COLOR_PRIMARIO);

        btnCancelar.addActionListener(e -> dispose());
        btnCrear.addActionListener(e -> crearUsuario());

        botones.add(btnCancelar);
        botones.add(btnCrear);
        return botones;
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
            dispose();
        } else {
            Estilo.error(this, mensaje);
        }
    }
}
