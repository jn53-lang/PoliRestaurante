
package com.co.poligran.polirestaurante.Vista;

import com.co.poligran.polirestaurante.Entityes.Usuario;
import com.co.poligran.polirestaurante.Managers.AdminManager;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class PanelUsuarios extends JPanel {

    private AdminManager admin;
    private Usuario actual;

    private JTextField txtNombre;
    private JTextField txtCorreo;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRol;

    private JTable tabla;
    private DefaultTableModel modelo;

    private int idSeleccionado = -1;

    public PanelUsuarios(AdminManager admin, Usuario actual) {
        this.admin = admin;
        this.actual = actual;

        setLayout(new BorderLayout(15, 15));
        setBackground(Estilo.COLOR_FONDO);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        crearFormulario();
        crearTabla();

        cargarUsuarios();
    }

    private void crearFormulario() {
        JPanel formulario = new JPanel(new GridLayout(2, 4, 10, 8));
        formulario.setBackground(Estilo.COLOR_FONDO);
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del usuario"));

        txtNombre = Estilo.crearCampo();
        txtCorreo = Estilo.crearCampo();
        txtPassword = Estilo.crearCampoPassword();

        cmbRol = new JComboBox<>(new String[]{
            "Administrador",
            "Mesero",
            "Cocina"
        });

        formulario.add(Estilo.crearEtiqueta("Nombre"));
        formulario.add(Estilo.crearEtiqueta("Correo"));
        formulario.add(Estilo.crearEtiqueta("Contraseña"));
        formulario.add(Estilo.crearEtiqueta("Rol"));

        formulario.add(txtNombre);
        formulario.add(txtCorreo);
        formulario.add(txtPassword);
        formulario.add(cmbRol);

        JButton btnRegistrar = Estilo.crearBoton("Registrar", Estilo.COLOR_PRIMARIO);
        JButton btnActualizar = Estilo.crearBoton("Actualizar", Estilo.COLOR_ACENTO);
        JButton btnEliminar = Estilo.crearBoton("Eliminar", Estilo.COLOR_SECUNDARIO);
        JButton btnLimpiar = Estilo.crearBoton("Limpiar", Estilo.COLOR_PRIMARIO);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        botones.setBackground(Estilo.COLOR_FONDO);

        botones.add(btnRegistrar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        JPanel superior = new JPanel(new BorderLayout(10, 10));
        superior.setBackground(Estilo.COLOR_FONDO);
        superior.add(formulario, BorderLayout.CENTER);
        superior.add(botones, BorderLayout.SOUTH);

        add(superior, BorderLayout.NORTH);

        btnRegistrar.addActionListener(e -> registrarUsuario());
        btnActualizar.addActionListener(e -> actualizarUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
    }

    private void crearTabla() {
        modelo = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Correo", "Rol"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tabla = new JTable(modelo);
        tabla.setFont(Estilo.FUENTE_NORMAL);
        tabla.setRowHeight(28);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tabla.getTableHeader().setFont(Estilo.FUENTE_NEGRITA);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() != -1) {
                seleccionarUsuario();
            }
        });

        JPanel panelTabla = new JPanel(new BorderLayout(5, 5));
        panelTabla.setBackground(Estilo.COLOR_FONDO);
        panelTabla.setBorder(BorderFactory.createTitledBorder("Usuarios registrados"));

        panelTabla.add(new JScrollPane(tabla), BorderLayout.CENTER);

        add(panelTabla, BorderLayout.CENTER);
    }

    private void registrarUsuario() {
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();
        String password = new String(txtPassword.getPassword());
        String rol = cmbRol.getSelectedItem().toString();

        String resultado = admin.registrarUsuario(
                nombre, correo, password, rol
        );

        if (resultado.startsWith("Usuario creado")) {
            Estilo.info(this, resultado);
            cargarUsuarios();
            limpiarFormulario();
        } else {
            Estilo.error(this, resultado);
        }
    }

    private void actualizarUsuario() {
        if (idSeleccionado == -1) {
            Estilo.error(this, "Seleccione un usuario de la tabla.");
            return;
        }

        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();
        String password = new String(txtPassword.getPassword());
        String rol = cmbRol.getSelectedItem().toString();

        String resultado = admin.actualizarUsuario(
                idSeleccionado, nombre, correo, password, rol
        );

        if (resultado.equals("Usuario actualizado.")) {
            Estilo.info(this, resultado);
            cargarUsuarios();
            limpiarFormulario();
        } else {
            Estilo.error(this, resultado);
        }
    }

    private void eliminarUsuario() {
        if (idSeleccionado == -1) {
            Estilo.error(this, "Seleccione un usuario de la tabla.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de eliminar este usuario?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        int idActual = actual != null ? actual.getId() : -1;

        String resultado = admin.eliminarUsuario(
                idSeleccionado, idActual
        );

        if (resultado.equals("Usuario eliminado.")) {
            Estilo.info(this, resultado);
            cargarUsuarios();
            limpiarFormulario();
        } else {
            Estilo.error(this, resultado);
        }
    }

    private void cargarUsuarios() {
        modelo.setRowCount(0);

        for (Usuario usuario : admin.listarUsuarios()) {
            modelo.addRow(new Object[]{
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol()
            });
        }
    }

    private void seleccionarUsuario() {
        int fila = tabla.getSelectedRow();

        if (fila == -1) {
            return;
        }

        idSeleccionado = Integer.parseInt(
                modelo.getValueAt(fila, 0).toString()
        );

        Usuario usuario = admin.buscarUsuario(idSeleccionado);

        if (usuario != null) {
            txtNombre.setText(usuario.getNombre());
            txtCorreo.setText(usuario.getCorreo());
            txtPassword.setText("");

            cmbRol.setSelectedItem(usuario.getRol());
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = -1;

        txtNombre.setText("");
        txtCorreo.setText("");
        txtPassword.setText("");

        cmbRol.setSelectedIndex(0);
        tabla.clearSelection();
    }
}
