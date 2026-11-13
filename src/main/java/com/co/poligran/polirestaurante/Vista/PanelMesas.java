
package com.co.poligran.polirestaurante.Vista;

import com.co.poligran.polirestaurante.Entityes.Mesa;
import com.co.poligran.polirestaurante.Managers.AdminManager;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class PanelMesas extends JPanel {

    private final AdminManager admin;

    private JTextField txtNumero;
    private JTextField txtCapacidad;
    private JTable tablaMesas;
    private DefaultTableModel modelo;
    private int idSeleccionado = -1;

    public PanelMesas(AdminManager admin) {
        this.admin = admin;

        setLayout(new BorderLayout(15, 15));
        setBackground(Estilo.COLOR_FONDO);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        crearComponentes();
        cargarMesas();
    }

    private void crearComponentes() {
        add(Estilo.crearEncabezado(
                "Gestión de mesas",
                "Administra las mesas, su capacidad y disponibilidad."
        ), BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(15, 15));
        contenido.setOpaque(false);

        JPanel formulario = new JPanel(new GridLayout(0, 2, 10, 10));
        formulario.setBackground(Estilo.COLOR_FONDO);
        formulario.setBorder(BorderFactory.createTitledBorder("Datos de la mesa"));

        txtNumero = Estilo.crearCampo();
        txtCapacidad = Estilo.crearCampo();

        formulario.add(Estilo.crearEtiqueta("Número de mesa:"));
        formulario.add(txtNumero);

        formulario.add(Estilo.crearEtiqueta("Capacidad:"));
        formulario.add(txtCapacidad);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        botones.setOpaque(false);

        JButton btnGuardar = Estilo.crearBoton("Crear mesa", Estilo.COLOR_PRIMARIO);
        JButton btnActualizar = Estilo.crearBoton("Actualizar", Estilo.COLOR_ACENTO);
        JButton btnEliminar = Estilo.crearBoton("Eliminar", Estilo.COLOR_SECUNDARIO);
        JButton btnLimpiar = Estilo.crearBoton("Limpiar", Estilo.COLOR_SECUNDARIO);

        btnGuardar.addActionListener(e -> guardarMesa());
        btnActualizar.addActionListener(e -> actualizarMesa());
        btnEliminar.addActionListener(e -> eliminarMesa());
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
                new Object[]{"ID", "Número", "Capacidad", "Estado"},
                0
        ) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaMesas = new JTable(modelo);
        tablaMesas.setFont(Estilo.FUENTE_NORMAL);
        tablaMesas.setRowHeight(25);
        tablaMesas.getTableHeader().setFont(Estilo.FUENTE_NEGRITA);
        tablaMesas.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        tablaMesas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarMesa();
            }
        });

        JPanel panelTabla = new JPanel(new BorderLayout(5, 5));
        panelTabla.setOpaque(false);
        panelTabla.setBorder(BorderFactory.createTitledBorder("Mesas registradas"));
        panelTabla.add(new JScrollPane(tablaMesas), BorderLayout.CENTER);

        JButton btnRecargar = Estilo.crearBoton("Actualizar lista", Estilo.COLOR_PRIMARIO);
        btnRecargar.addActionListener(e -> cargarMesas());

        JPanel panelRecargar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelRecargar.setOpaque(false);
        panelRecargar.add(btnRecargar);

        panelTabla.add(panelRecargar, BorderLayout.SOUTH);

        contenido.add(panelSuperior, BorderLayout.NORTH);
        contenido.add(panelTabla, BorderLayout.CENTER);

        add(contenido, BorderLayout.CENTER);
    }

    private void cargarMesas() {
        modelo.setRowCount(0);

        List<Mesa> mesas = admin.listarMesas();

        for (Mesa mesa : mesas) {
            modelo.addRow(new Object[]{
                mesa.getId(),
                mesa.getNumero(),
                mesa.getCapacidad(),
                mesa.getEstado()
            });
        }
    }

    private void guardarMesa() {
        try {
            int numero = Integer.parseInt(txtNumero.getText().trim());
            int capacidad = Integer.parseInt(txtCapacidad.getText().trim());

            String resultado = admin.crearMesa(numero, capacidad);

            if (resultado.startsWith("Mesa creada")) {
                Estilo.info(this, resultado);
                limpiarFormulario();
                cargarMesas();
            } else {
                Estilo.error(this, resultado);
            }

        } catch (NumberFormatException e) {
            Estilo.error(this, "Ingresa un número de mesa y una capacidad válidos.");
        }
    }

    private void actualizarMesa() {
        if (idSeleccionado == -1) {
            Estilo.error(this, "Selecciona una mesa de la tabla.");
            return;
        }

        try {
            int numero = Integer.parseInt(txtNumero.getText().trim());
            int capacidad = Integer.parseInt(txtCapacidad.getText().trim());

            String resultado = admin.actualizarMesa(
                    idSeleccionado,
                    numero,
                    capacidad
            );

            if (resultado.equals("Mesa actualizada.")) {
                Estilo.info(this, resultado);
                limpiarFormulario();
                cargarMesas();
            } else {
                Estilo.error(this, resultado);
            }

        } catch (NumberFormatException e) {
            Estilo.error(this, "Ingresa un número de mesa y una capacidad válidos.");
        }
    }

    private void eliminarMesa() {
        if (idSeleccionado == -1) {
            Estilo.error(this, "Selecciona una mesa de la tabla.");
            return;
        }

        int respuesta = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de eliminar esta mesa?",
                "Confirmar eliminación",
                javax.swing.JOptionPane.YES_NO_OPTION
        );

        if (respuesta != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        String resultado = admin.eliminarMesa(idSeleccionado);

        if (resultado.equals("Mesa eliminada.")) {
            Estilo.info(this, resultado);
            limpiarFormulario();
            cargarMesas();
        } else {
            Estilo.error(this, resultado);
        }
    }

    private void seleccionarMesa() {
        int fila = tablaMesas.getSelectedRow();

        if (fila == -1) {
            return;
        }

        idSeleccionado = (int) modelo.getValueAt(fila, 0);

        Mesa mesa = admin.buscarMesa(idSeleccionado);

        if (mesa == null) {
            return;
        }

        txtNumero.setText(String.valueOf(mesa.getNumero()));
        txtCapacidad.setText(String.valueOf(mesa.getCapacidad()));
    }

    private void limpiarFormulario() {
        idSeleccionado = -1;
        txtNumero.setText("");
        txtCapacidad.setText("");
        tablaMesas.clearSelection();
    }
}
