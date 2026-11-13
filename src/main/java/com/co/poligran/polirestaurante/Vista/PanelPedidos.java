
package com.co.poligran.polirestaurante.Vista;

import com.co.poligran.polirestaurante.Entityes.DetallePedido;
import com.co.poligran.polirestaurante.Entityes.HistorialEstado;
import com.co.poligran.polirestaurante.Entityes.Mesa;
import com.co.poligran.polirestaurante.Entityes.Pedido;
import com.co.poligran.polirestaurante.Entityes.Producto;
import com.co.poligran.polirestaurante.Entityes.Usuario;
import com.co.poligran.polirestaurante.Managers.AdminManager;
import com.co.poligran.polirestaurante.Managers.PedidoManager;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class PanelPedidos extends JPanel {

    private PedidoManager pedidoManager;
    private AdminManager adminManager;
    private Usuario actual;

    private JComboBox<Mesa> cmbMesas;
    private JComboBox<Producto> cmbProductos;
    private JSpinner spCantidad;

    private JTable tablaPedidos;
    private JTable tablaDetalles;

    private DefaultTableModel modeloPedidos;
    private DefaultTableModel modeloDetalles;

    private JPanel formulario;
    private int idPedidoSeleccionado = -1;

    private boolean esMesero() {
        return actual != null && actual.getRol().equalsIgnoreCase("Mesero");
    }

    private boolean esCocina() {
        return actual != null && actual.getRol().equalsIgnoreCase("Cocina");
    }

    public PanelPedidos(PedidoManager pedidoManager, AdminManager adminManager, Usuario actual) {
        this.pedidoManager = pedidoManager;
        this.adminManager = adminManager;
        this.actual = actual;

        setLayout(new BorderLayout(10, 10));
        setBackground(Estilo.COLOR_FONDO);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        crearFormulario();
        crearTablas();
        cargarPedidos();
        cargarMesas();
        cargarProductos();
    }

    private void crearFormulario() {
        formulario = new JPanel(new GridLayout(2, 4, 10, 8));
        formulario.setBackground(Estilo.COLOR_FONDO);
        formulario.setBorder(BorderFactory.createTitledBorder("Crear pedido y agregar productos"));

        cmbMesas = new JComboBox<>();
        cmbProductos = new JComboBox<>();

        spCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

        formulario.add(Estilo.crearEtiqueta("Mesa disponible"));
        formulario.add(Estilo.crearEtiqueta("Producto"));
        formulario.add(Estilo.crearEtiqueta("Cantidad"));
        formulario.add(new JLabel(""));

        formulario.add(cmbMesas);
        formulario.add(cmbProductos);
        formulario.add(spCantidad);

        JButton btnCrear = Estilo.crearBoton("Crear pedido", Estilo.COLOR_PRIMARIO);
        JButton btnAgregar = Estilo.crearBoton("Agregar producto", Estilo.COLOR_ACENTO);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        botones.setBackground(Estilo.COLOR_FONDO);
        botones.add(btnCrear);
        botones.add(btnAgregar);

        JPanel superior = new JPanel(new BorderLayout(5, 5));
        superior.setBackground(Estilo.COLOR_FONDO);
        superior.add(formulario, BorderLayout.CENTER);
        superior.add(botones, BorderLayout.SOUTH);

        if (esMesero()) {
            add(superior, BorderLayout.NORTH);
        }

        btnCrear.addActionListener(e -> crearPedido());
        btnAgregar.addActionListener(e -> agregarProducto());
    }

    private void crearTablas() {
        modeloPedidos = new DefaultTableModel(
                new Object[]{"ID", "Fecha", "Estado", "Total", "Mesero", "Mesa"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaPedidos = new JTable(modeloPedidos);
        tablaPedidos.setRowHeight(25);
        tablaPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tablaPedidos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaPedidos.getSelectedRow() != -1) {
                seleccionarPedido();
            }
        });

        modeloDetalles = new DefaultTableModel(
                new Object[]{"ID detalle", "Producto", "Cantidad", "Precio unitario", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaDetalles = new JTable(modeloDetalles);
        tablaDetalles.setRowHeight(25);

        JPanel panelPedidos = new JPanel(new BorderLayout());
        panelPedidos.setBackground(Estilo.COLOR_FONDO);
        panelPedidos.setBorder(BorderFactory.createTitledBorder("Pedidos registrados"));
        panelPedidos.add(new JScrollPane(tablaPedidos), BorderLayout.CENTER);

        JPanel panelDetalles = new JPanel(new BorderLayout());
        panelDetalles.setBackground(Estilo.COLOR_FONDO);
        panelDetalles.setBorder(BorderFactory.createTitledBorder("Productos del pedido seleccionado"));
        panelDetalles.add(new JScrollPane(tablaDetalles), BorderLayout.CENTER);

        JPanel centro = new JPanel(new GridLayout(2, 1, 10, 10));
        centro.setBackground(Estilo.COLOR_FONDO);
        centro.add(panelPedidos);
        centro.add(panelDetalles);

        add(centro, BorderLayout.CENTER);

        crearBotonesAcciones();
    }

    private void crearBotonesAcciones() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        botones.setBackground(Estilo.COLOR_FONDO);

        if (esCocina()) {
            JButton btnPreparar = Estilo.crearBoton("Preparar", Estilo.COLOR_PRIMARIO);
            JButton btnListo = Estilo.crearBoton("Marcar listo", Estilo.COLOR_ACENTO);

            botones.add(btnPreparar);
            botones.add(btnListo);

            btnPreparar.addActionListener(e -> cambiarEstado("PREPARAR"));
            btnListo.addActionListener(e -> cambiarEstado("LISTO"));
        }

        if (esMesero()) {
            JButton btnEntregar = Estilo.crearBoton("Entregar", Estilo.COLOR_PRIMARIO);
            JButton btnCancelar = Estilo.crearBoton("Cancelar", Estilo.COLOR_SECUNDARIO);

            botones.add(btnEntregar);
            botones.add(btnCancelar);

            btnEntregar.addActionListener(e -> cambiarEstado("ENTREGAR"));
            btnCancelar.addActionListener(e -> cambiarEstado("CANCELAR"));
        }

        JButton btnHistorial = Estilo.crearBoton("Ver historial", Estilo.COLOR_ACENTO);
        JButton btnActualizar = Estilo.crearBoton("Actualizar", Estilo.COLOR_PRIMARIO);

        botones.add(btnHistorial);
        botones.add(btnActualizar);

        add(botones, BorderLayout.SOUTH);

        btnHistorial.addActionListener(e -> verHistorial());
        btnActualizar.addActionListener(e -> actualizarTodo());
    }

    private void crearPedido() {
        if (!esMesero()) {
            Estilo.error(this, "Solo un mesero puede crear pedidos.");
            return;
        }

        Mesa mesa = (Mesa) cmbMesas.getSelectedItem();

        if (mesa == null) {
            Estilo.error(this, "No hay mesas disponibles.");
            return;
        }

        String resultado = pedidoManager.crearPedido(actual.getId(), mesa.getId());

        if (resultado.startsWith("Pedido creado")) {
            Estilo.info(this, resultado);
            actualizarTodo();
        } else {
            Estilo.error(this, resultado);
        }
    }

    private void agregarProducto() {
        if (!esMesero()) {
            Estilo.error(this, "Solo un mesero puede agregar productos.");
            return;
        }

        if (idPedidoSeleccionado == -1) {
            Estilo.error(this, "Seleccione un pedido de la tabla.");
            return;
        }

        Producto producto = (Producto) cmbProductos.getSelectedItem();

        if (producto == null) {
            Estilo.error(this, "No hay productos disponibles.");
            return;
        }

        int cantidad = (int) spCantidad.getValue();

        String resultado = pedidoManager.agregarProducto(
                idPedidoSeleccionado,
                producto.getId(),
                cantidad
        );

        if (resultado.equals("Producto agregado al pedido.")) {
            Estilo.info(this, resultado);
            actualizarTodo();
        } else {
            Estilo.error(this, resultado);
        }
    }

    private void cargarPedidos() {
        modeloPedidos.setRowCount(0);

        for (Pedido pedido : pedidoManager.listarPedidos()) {
            modeloPedidos.addRow(new Object[]{
                pedido.getId(),
                new SimpleDateFormat("dd/MM/yyyy HH:mm").format(pedido.getFecha()),
                pedido.getEstado(),
                String.format("$%.2f", pedido.getTotal()),
                pedido.getMesero().getNombre(),
                pedido.getMesa().getNumero()
            });
        }
    }

    private void cargarMesas() {
        cmbMesas.removeAllItems();

        for (Mesa mesa : adminManager.listarMesasDisponibles()) {
            cmbMesas.addItem(mesa);
        }
    }

    private void cargarProductos() {
        cmbProductos.removeAllItems();

        for (Producto producto : adminManager.listarProductos()) {
            if (producto.isDisponible()) {
                cmbProductos.addItem(producto);
            }
        }
    }

    private void seleccionarPedido() {
        int fila = tablaPedidos.getSelectedRow();

        if (fila == -1) {
            return;
        }

        idPedidoSeleccionado = Integer.parseInt(
                modeloPedidos.getValueAt(fila, 0).toString()
        );

        cargarDetalles();
    }

    private void cargarDetalles() {
        modeloDetalles.setRowCount(0);

        if (idPedidoSeleccionado == -1) {
            return;
        }

        for (DetallePedido detalle : pedidoManager.listarDetalles(idPedidoSeleccionado)) {
            modeloDetalles.addRow(new Object[]{
                detalle.getId(),
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                String.format("$%.2f", detalle.getPrecioUnitario()),
                String.format("$%.2f", detalle.getSubtotal())
            });
        }
    }

    private void cambiarEstado(String accion) {
        if (idPedidoSeleccionado == -1) {
            Estilo.error(this, "Seleccione un pedido de la tabla.");
            return;
        }

        if (esMesero() && !accion.equals("ENTREGAR") && !accion.equals("CANCELAR")) {
            Estilo.error(this, "El mesero no puede realizar esa acción.");
            return;
        }

        if (esCocina() && !accion.equals("PREPARAR") && !accion.equals("LISTO")) {
            Estilo.error(this, "Cocina no puede realizar esa acción.");
            return;
        }

        String resultado;

        switch (accion) {
            case "PREPARAR":
                resultado = pedidoManager.prepararPedido(idPedidoSeleccionado);
                break;

            case "LISTO":
                resultado = pedidoManager.marcarListo(idPedidoSeleccionado);
                break;

            case "ENTREGAR":
                resultado = pedidoManager.entregarPedido(idPedidoSeleccionado);
                break;

            default:
                resultado = pedidoManager.cancelarPedido(idPedidoSeleccionado);
                break;
        }

        if (resultado.startsWith("Pedido " + idPedidoSeleccionado + " ahora esta")) {
            Estilo.info(this, resultado);
            actualizarTodo();
        } else {
            Estilo.error(this, resultado);
        }
    }

    private void verHistorial() {
        if (idPedidoSeleccionado == -1) {
            Estilo.error(this, "Seleccione un pedido de la tabla.");
            return;
        }

        StringBuilder historial = new StringBuilder();

        for (HistorialEstado registro : pedidoManager.verHistorial(idPedidoSeleccionado)) {
            historial.append(registro.getEstado())
                    .append(" - ")
                    .append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(registro.getFechaHora()))
                    .append("\n");
        }

        if (historial.length() == 0) {
            historial.append("Este pedido no tiene historial registrado.");
        }

        JOptionPane.showMessageDialog(
                this,
                historial.toString(),
                "Historial del pedido " + idPedidoSeleccionado,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void actualizarTodo() {
        cargarPedidos();
        cargarMesas();
        cargarProductos();

        if (idPedidoSeleccionado != -1) {
            cargarDetalles();
        }
    }
}
