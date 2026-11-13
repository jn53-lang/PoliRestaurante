
package com.co.poligran.polirestaurante.Vista;

import com.co.poligran.polirestaurante.Entityes.Categoria;
import com.co.poligran.polirestaurante.Entityes.Producto;
import com.co.poligran.polirestaurante.Managers.AdminManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableModel;

public class PanelProductos extends JPanel {

    private final AdminManager admin;

    private JTextField txtNombre;
    private JTextField txtPrecio;
    private JTextArea txtDescripcion;
    private JComboBox<Categoria> comboCategorias;
    private JCheckBox chkDisponible;
    private JTable tablaProductos;
    private DefaultTableModel modelo;
    private int idSeleccionado = -1;

    public PanelProductos(AdminManager admin) {
        this.admin = admin;

        setLayout(new BorderLayout(15, 15));
        setBackground(Estilo.COLOR_FONDO);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        crearComponentes();
        cargarCategorias();
        cargarProductos();
    }

    private void crearComponentes() {
        add(Estilo.crearEncabezado(
                "Gestión de productos",
                "Registra, consulta, modifica y elimina productos."
        ), BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(15, 15));
        contenido.setOpaque(false);

        JPanel formulario = new JPanel(new GridLayout(0, 2, 10, 10));
        formulario.setBackground(Estilo.COLOR_FONDO);
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del producto"));

        txtNombre = Estilo.crearCampo();
        txtPrecio = Estilo.crearCampo();

        txtDescripcion = new JTextArea(3, 20);
        txtDescripcion.setFont(Estilo.FUENTE_NORMAL);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);

        comboCategorias = new JComboBox<>();
        comboCategorias.setFont(Estilo.FUENTE_NORMAL);
        comboCategorias.setRenderer(new ListCellRenderer<Categoria>() {
            private final JLabel etiqueta = new JLabel();

            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<? extends Categoria> lista,
                    Categoria categoria,
                    int indice,
                    boolean seleccionado,
                    boolean tieneFoco) {

                if (categoria == null) {
                    etiqueta.setText("Sin categoría");
                } else {
                    etiqueta.setText(categoria.getNombre());
                }

                if (seleccionado) {
                    etiqueta.setBackground(lista.getSelectionBackground());
                    etiqueta.setForeground(lista.getSelectionForeground());
                } else {
                    etiqueta.setBackground(lista.getBackground());
                    etiqueta.setForeground(lista.getForeground());
                }

                etiqueta.setOpaque(true);
                etiqueta.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
                return etiqueta;
            }
        });

        chkDisponible = new JCheckBox("Producto disponible");
        chkDisponible.setSelected(true);
        chkDisponible.setBackground(Estilo.COLOR_FONDO);
        chkDisponible.setFont(Estilo.FUENTE_NORMAL);

        formulario.add(Estilo.crearEtiqueta("Nombre:"));
        formulario.add(txtNombre);

        formulario.add(Estilo.crearEtiqueta("Precio:"));
        formulario.add(txtPrecio);

        formulario.add(Estilo.crearEtiqueta("Categoría:"));
        formulario.add(comboCategorias);

        formulario.add(Estilo.crearEtiqueta("Descripción:"));
        formulario.add(new JScrollPane(txtDescripcion));

        formulario.add(Estilo.crearEtiqueta("Disponibilidad:"));
        formulario.add(chkDisponible);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        botones.setOpaque(false);

        JButton btnGuardar = Estilo.crearBoton("Guardar", Estilo.COLOR_PRIMARIO);
        JButton btnActualizar = Estilo.crearBoton("Actualizar", Estilo.COLOR_ACENTO);
        JButton btnEliminar = Estilo.crearBoton("Eliminar", Estilo.COLOR_SECUNDARIO);
        JButton btnLimpiar = Estilo.crearBoton("Limpiar", Estilo.COLOR_SECUNDARIO);

        btnGuardar.addActionListener(e -> guardarProducto());
        btnActualizar.addActionListener(e -> actualizarProducto());
        btnEliminar.addActionListener(e -> eliminarProducto());
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
                new Object[]{"ID", "Nombre", "Precio", "Categoría", "Descripción", "Disponible"},
                0
        ) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaProductos = new JTable(modelo);
        tablaProductos.setFont(Estilo.FUENTE_NORMAL);
        tablaProductos.setRowHeight(25);
        tablaProductos.getTableHeader().setFont(Estilo.FUENTE_NEGRITA);
        tablaProductos.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarProducto();
            }
        });

        JPanel panelTabla = new JPanel(new BorderLayout(5, 5));
        panelTabla.setOpaque(false);
        panelTabla.setBorder(BorderFactory.createTitledBorder("Productos registrados"));
        panelTabla.add(new JScrollPane(tablaProductos), BorderLayout.CENTER);

        JButton btnRecargar = Estilo.crearBoton("Actualizar lista", Estilo.COLOR_PRIMARIO);
        btnRecargar.addActionListener(e -> {
            cargarCategorias();
            cargarProductos();
        });

        JPanel panelRecargar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelRecargar.setOpaque(false);
        panelRecargar.add(btnRecargar);
        panelTabla.add(panelRecargar, BorderLayout.SOUTH);

        contenido.add(panelSuperior, BorderLayout.NORTH);
        contenido.add(panelTabla, BorderLayout.CENTER);

        add(contenido, BorderLayout.CENTER);
    }

    private void cargarCategorias() {
        comboCategorias.removeAllItems();
        comboCategorias.addItem(null);

        List<Categoria> categorias = admin.listarCategorias();

        for (Categoria categoria : categorias) {
            comboCategorias.addItem(categoria);
        }
    }

    private void cargarProductos() {
        modelo.setRowCount(0);

        List<Producto> productos = admin.listarProductos();

        for (Producto producto : productos) {
            String categoria = "Sin categoría";

            if (producto.getCategoria() != null) {
                categoria = producto.getCategoria().getNombre();
            }

            modelo.addRow(new Object[]{
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                categoria,
                producto.getDescripcion(),
                producto.isDisponible() ? "Sí" : "No"
            });
        }
    }

    private void guardarProducto() {
        try {
            String nombre = txtNombre.getText().trim();
            String descripcion = txtDescripcion.getText().trim();
            double precio = Double.parseDouble(txtPrecio.getText().trim());

            Categoria categoria = (Categoria) comboCategorias.getSelectedItem();
            Integer idCategoria = categoria == null ? null : categoria.getId();

            String resultado = admin.crearProducto(
                    nombre,
                    precio,
                    descripcion,
                    chkDisponible.isSelected(),
                    idCategoria
            );

            if (resultado.startsWith("Producto creado")) {
                Estilo.info(this, resultado);
                limpiarFormulario();
                cargarProductos();
            } else {
                Estilo.error(this, resultado);
            }

        } catch (NumberFormatException e) {
            Estilo.error(this, "Ingresa un precio válido.");
        }
    }

    private void actualizarProducto() {
        if (idSeleccionado == -1) {
            Estilo.error(this, "Selecciona un producto de la tabla.");
            return;
        }

        try {
            String nombre = txtNombre.getText().trim();
            String descripcion = txtDescripcion.getText().trim();
            double precio = Double.parseDouble(txtPrecio.getText().trim());

            Categoria categoria = (Categoria) comboCategorias.getSelectedItem();
            Integer idCategoria = categoria == null ? null : categoria.getId();

            String resultado = admin.actualizarProducto(
                    idSeleccionado,
                    nombre,
                    precio,
                    descripcion,
                    chkDisponible.isSelected(),
                    idCategoria
            );

            if (resultado.equals("Producto actualizado.")) {
                Estilo.info(this, resultado);
                limpiarFormulario();
                cargarProductos();
            } else {
                Estilo.error(this, resultado);
            }

        } catch (NumberFormatException e) {
            Estilo.error(this, "Ingresa un precio válido.");
        }
    }

    private void eliminarProducto() {
        if (idSeleccionado == -1) {
            Estilo.error(this, "Selecciona un producto de la tabla.");
            return;
        }

        int respuesta = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de eliminar este producto?",
                "Confirmar eliminación",
                javax.swing.JOptionPane.YES_NO_OPTION
        );

        if (respuesta != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        String resultado = admin.eliminarProducto(idSeleccionado);

        if (resultado.equals("Producto eliminado.")) {
            Estilo.info(this, resultado);
            limpiarFormulario();
            cargarProductos();
        } else {
            Estilo.error(this, resultado);
        }
    }

    private void seleccionarProducto() {
        int fila = tablaProductos.getSelectedRow();

        if (fila == -1) {
            return;
        }

        idSeleccionado = (int) modelo.getValueAt(fila, 0);

        Producto producto = admin.buscarProducto(idSeleccionado);

        if (producto == null) {
            return;
        }

        txtNombre.setText(producto.getNombre());
        txtPrecio.setText(String.valueOf(producto.getPrecio()));
        txtDescripcion.setText(producto.getDescripcion());
        chkDisponible.setSelected(producto.isDisponible());

        comboCategorias.setSelectedItem(null);

        if (producto.getCategoria() != null) {
            for (int i = 0; i < comboCategorias.getItemCount(); i++) {
                Categoria categoria = comboCategorias.getItemAt(i);

                if (categoria != null
                        && categoria.getId() == producto.getCategoria().getId()) {
                    comboCategorias.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = -1;
        txtNombre.setText("");
        txtPrecio.setText("");
        txtDescripcion.setText("");
        chkDisponible.setSelected(true);
        comboCategorias.setSelectedItem(null);
        tablaProductos.clearSelection();
    }
}
