
package com.co.poligran.polirestaurante.Vista;

import com.co.poligran.polirestaurante.Entityes.Categoria;
import com.co.poligran.polirestaurante.Entityes.Producto;
import com.co.poligran.polirestaurante.Managers.AdminManager;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class PanelCategorias extends JPanel {

    private final AdminManager admin;

    private JTextField txtNombre;
    private JTextArea txtDescripcion;
    private JTable tablaCategorias;
    private DefaultTableModel modelo;
    private int idSeleccionado = -1;

    public PanelCategorias(AdminManager admin) {
        this.admin = admin;

        setLayout(new BorderLayout(15, 15));
        setBackground(Estilo.COLOR_FONDO);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        crearComponentes();
        cargarCategorias();
    }

    private void crearComponentes() {
        add(Estilo.crearEncabezado(
                "Gestión de categorías",
                "Organiza los productos del restaurante por categorías."
        ), BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(15, 15));
        contenido.setOpaque(false);

        JPanel formulario = new JPanel(new GridLayout(0, 2, 10, 10));
        formulario.setBackground(Estilo.COLOR_FONDO);
        formulario.setBorder(BorderFactory.createTitledBorder("Datos de la categoría"));

        txtNombre = Estilo.crearCampo();

        txtDescripcion = new JTextArea(4, 20);
        txtDescripcion.setFont(Estilo.FUENTE_NORMAL);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);

        formulario.add(Estilo.crearEtiqueta("Nombre:"));
        formulario.add(txtNombre);

        formulario.add(Estilo.crearEtiqueta("Descripción:"));
        formulario.add(new JScrollPane(txtDescripcion));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        botones.setOpaque(false);

        JButton btnGuardar = Estilo.crearBoton("Crear categoría", Estilo.COLOR_PRIMARIO);
        JButton btnActualizar = Estilo.crearBoton("Actualizar", Estilo.COLOR_ACENTO);
        JButton btnEliminar = Estilo.crearBoton("Eliminar", Estilo.COLOR_SECUNDARIO);
        JButton btnLimpiar = Estilo.crearBoton("Limpiar", Estilo.COLOR_SECUNDARIO);

        btnGuardar.addActionListener(e -> guardarCategoria());
        btnActualizar.addActionListener(e -> actualizarCategoria());
        btnEliminar.addActionListener(e -> eliminarCategoria());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        botones.add(btnGuardar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.setOpaque(false);
        panelSuperior.add(formulario, BorderLayout.CENTER);
        panelSuperior.add(botones, BorderLayout.SOUTH);

        modelo = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Descripción", "Productos asociados"},
                0
        ) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaCategorias = new JTable(modelo);
        tablaCategorias.setFont(Estilo.FUENTE_NORMAL);
        tablaCategorias.setRowHeight(25);
        tablaCategorias.getTableHeader().setFont(Estilo.FUENTE_NEGRITA);
        tablaCategorias.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        tablaCategorias.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarCategoria();
            }
        });

        JPanel panelTabla = new JPanel(new BorderLayout(5, 5));
        panelTabla.setOpaque(false);
        panelTabla.setBorder(BorderFactory.createTitledBorder("Categorías registradas"));
        panelTabla.add(new JScrollPane(tablaCategorias), BorderLayout.CENTER);

        JButton btnRecargar = Estilo.crearBoton("Actualizar lista", Estilo.COLOR_PRIMARIO);
        btnRecargar.addActionListener(e -> cargarCategorias());

        JPanel panelRecargar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelRecargar.setOpaque(false);
        panelRecargar.add(btnRecargar);

        panelTabla.add(panelRecargar, BorderLayout.SOUTH);

        contenido.add(panelSuperior, BorderLayout.NORTH);
        contenido.add(panelTabla, BorderLayout.CENTER);

        add(contenido, BorderLayout.CENTER);
    }

    private void cargarCategorias() {
        modelo.setRowCount(0);

        List<Categoria> categorias = admin.listarCategorias();

        for (Categoria categoria : categorias) {
            Categoria completa = admin.buscarCategoria(categoria.getId());

            int cantidadProductos = 0;

            if (completa != null) {
                cantidadProductos = completa.getProductos().size();
            }

            modelo.addRow(new Object[]{
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                cantidadProductos
            });
        }
    }

    private void guardarCategoria() {
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        String resultado = admin.crearCategoria(nombre, descripcion);

        if (resultado.startsWith("Categoria creada")) {
            Estilo.info(this, resultado);
            limpiarFormulario();
            cargarCategorias();
        } else {
            Estilo.error(this, resultado);
        }
    }

    private void actualizarCategoria() {
        if (idSeleccionado == -1) {
            Estilo.error(this, "Selecciona una categoría de la tabla.");
            return;
        }

        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        String resultado = admin.actualizarCategoria(
                idSeleccionado,
                nombre,
                descripcion
        );

        if (resultado.equals("Categoria actualizada.")) {
            Estilo.info(this, resultado);
            limpiarFormulario();
            cargarCategorias();
        } else {
            Estilo.error(this, resultado);
        }
    }

    private void eliminarCategoria() {
        if (idSeleccionado == -1) {
            Estilo.error(this, "Selecciona una categoría de la tabla.");
            return;
        }

        int respuesta = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de eliminar esta categoría?",
                "Confirmar eliminación",
                javax.swing.JOptionPane.YES_NO_OPTION
        );

        if (respuesta != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        String resultado = admin.eliminarCategoria(idSeleccionado);

        if (resultado.equals("Categoria eliminada.")) {
            Estilo.info(this, resultado);
            limpiarFormulario();
            cargarCategorias();
        } else {
            Estilo.error(this, resultado);
        }
    }

    private void seleccionarCategoria() {
        int fila = tablaCategorias.getSelectedRow();

        if (fila == -1) {
            return;
        }

        idSeleccionado = (int) modelo.getValueAt(fila, 0);

        Categoria categoria = admin.buscarCategoria(idSeleccionado);

        if (categoria == null) {
            return;
        }

        txtNombre.setText(categoria.getNombre());
        txtDescripcion.setText(categoria.getDescripcion());
    }

    private void limpiarFormulario() {
        idSeleccionado = -1;
        txtNombre.setText("");
        txtDescripcion.setText("");
        tablaCategorias.clearSelection();
    }
}
