package com.co.poligran.polirestaurante.Vista;

import com.co.poligran.polirestaurante.Managers.AdminManager;
import com.co.poligran.polirestaurante.Entityes.Usuario;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Panel del inicio de sesion. Solo se encarga de pedir el correo y la
 * contrasena y de llamar al AdminManager; lo que pasa despues de iniciar
 * sesion lo decide la ventana (por eso recibe los "callbacks").
 */
public class PanelLogin extends JPanel {

    private AdminManager admin;
    private Consumer<Usuario> alIniciarSesion;   // que hacer si el login es correcto
    private Runnable alCrearUsuario;             // que hacer si pulsan "Crear usuario"

    private JTextField txtCorreo;
    private JPasswordField txtPassword;
    private JButton btnIngresar;
    private JButton btnCrearUsuario;

    public PanelLogin(AdminManager admin, Consumer<Usuario> alIniciarSesion, Runnable alCrearUsuario) {
        this.admin = admin;
        this.alIniciarSesion = alIniciarSesion;
        this.alCrearUsuario = alCrearUsuario;

        setLayout(new BorderLayout());
        add(Estilo.crearEncabezado("PoliRestaurante", "Sistema de gestión de restaurante"), BorderLayout.NORTH);
        add(crearFormulario(), BorderLayout.CENTER);
    }

    private JPanel crearFormulario() {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(Estilo.COLOR_FONDO);
        formulario.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));

        txtCorreo = Estilo.crearCampo();
        txtPassword = Estilo.crearCampoPassword();

        btnIngresar = Estilo.crearBoton("Iniciar sesión", Estilo.COLOR_PRIMARIO);
        btnCrearUsuario = Estilo.crearBoton("Crear usuario", Estilo.COLOR_SECUNDARIO);

        // Cada componente va en una fila (gridy) y ocupa todo el ancho.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        formulario.add(Estilo.crearEtiqueta("Correo"), gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 14, 0);
        formulario.add(txtCorreo, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        formulario.add(Estilo.crearEtiqueta("Contraseña"), gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 22, 0);
        formulario.add(txtPassword, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 18, 0);
        formulario.add(btnIngresar, gbc);

        JLabel pregunta = new JLabel("¿Aún no tiene una cuenta?", SwingConstants.CENTER);
        pregunta.setFont(Estilo.FUENTE_NORMAL);
        pregunta.setForeground(Estilo.COLOR_TEXTO);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 6, 0);
        formulario.add(pregunta, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        formulario.add(btnCrearUsuario, gbc);

        // Eventos de los botones.
        btnIngresar.addActionListener(e -> iniciarSesion());
        btnCrearUsuario.addActionListener(e -> alCrearUsuario.run());

        return formulario;
    }

    private void iniciarSesion() {
        String correo = txtCorreo.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (correo.isEmpty() || password.isEmpty()) {
            Estilo.error(this, "Escriba el correo y la contraseña.");
            return;
        }

        Usuario usuario = admin.iniciarSesion(correo, password);

        if (usuario == null) {
            Estilo.error(this, "Correo o contraseña incorrectos.");
            txtPassword.setText("");
            txtPassword.requestFocus();
            return;
        }

        limpiar();
        alIniciarSesion.accept(usuario);
    }

    // Deja los campos vacios (por ejemplo, despues de cerrar sesion).
    public void limpiar() {
        txtCorreo.setText("");
        txtPassword.setText("");
    }

    // La ventana lo usa para que ENTER pulse "Iniciar sesion".
    public JButton getBotonIngresar() {
        return btnIngresar;
    }
}
