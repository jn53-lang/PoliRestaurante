package com.co.poligran.polirestaurante.BackEnd;

import com.co.poligran.polirestaurante.Entityes.DetallePedido;
import com.co.poligran.polirestaurante.Entityes.HistorialEstado;
import com.co.poligran.polirestaurante.Entityes.Mesa;
import com.co.poligran.polirestaurante.Entityes.Mesero;
import com.co.poligran.polirestaurante.Entityes.Pedido;
import com.co.poligran.polirestaurante.Entityes.Producto;
import com.co.poligran.polirestaurante.Entityes.Usuario;
import com.co.poligran.polirestaurante.config.BaseDatos;
import com.co.poligran.polirestaurante.dao.DetallePedidoDAO;
import com.co.poligran.polirestaurante.dao.HistorialEstadoDAO;
import com.co.poligran.polirestaurante.dao.MesaDAO;
import com.co.poligran.polirestaurante.dao.PedidoDAO;
import com.co.poligran.polirestaurante.dao.ProductoDAO;
import com.co.poligran.polirestaurante.dao.UsuarioDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Manager de los pedidos (mesero y cocina). Sus 5 funcionalidades son:
 *   1. Crear pedido
 *   2. Detalle del pedido
 *   3. Consultar pedidos
 *   4. Cambiar estado
 *   5. Historial de estados
 *
 * Las operaciones que modifican datos devuelven un mensaje (String).
 * Las consultas devuelven los objetos, o una lista vacia / null si hay error.
 */
public class PedidoManager {

    private PedidoDAO pedidoDAO = new PedidoDAO();
    private DetallePedidoDAO detalleDAO = new DetallePedidoDAO();
    private HistorialEstadoDAO historialDAO = new HistorialEstadoDAO();
    private MesaDAO mesaDAO = new MesaDAO();
    private ProductoDAO productoDAO = new ProductoDAO();
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    public PedidoManager() {
        try {
            BaseDatos.inicializar();
        } catch (SQLException e) {
            System.out.println("Error al preparar la base de datos: " + e.getMessage());
        }
    }

    // =====================================================
    // 1. CREAR PEDIDO
    // =====================================================

    public String crearPedido(int idMesero, int idMesa) {
        try {
            Usuario usuario = usuarioDAO.buscarPorId(idMesero);
            if (usuario == null || !usuario.getRol().equalsIgnoreCase("Mesero")) {
                return "No existe un mesero con ese ID.";
            }

            Mesa mesa = mesaDAO.buscarPorId(idMesa);
            if (mesa == null) {
                return "No existe una mesa con ese ID.";
            }
            if (mesa.isOcupada()) {
                return "La mesa " + mesa.getNumero() + " esta ocupada.";
            }

            Mesero mesero = new Mesero(usuario.getId(), usuario.getNombre(),
                    usuario.getCorreo(), usuario.getPassword());

            int id = siguienteIdPedido();
            Pedido pedido = new Pedido(id, mesero, mesa);

            if (pedidoDAO.guardar(pedido)) {
                return "Pedido creado con ID " + id + " en la mesa " + mesa.getNumero() + ".";
            }
            return "No se pudo guardar el pedido.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    // Solo se puede cambiar la mesa mientras el pedido esta pendiente.
    public String cambiarMesa(int idPedido, int idMesaNueva) {
        try {
            Pedido pedido = pedidoDAO.buscarPorId(idPedido);
            if (pedido == null) {
                return "No existe un pedido con ese ID.";
            }
            if (!pedido.getEstado().equals("PENDIENTE")) {
                return "Solo se puede cambiar la mesa de un pedido pendiente.";
            }

            Mesa nueva = mesaDAO.buscarPorId(idMesaNueva);
            if (nueva == null) {
                return "No existe una mesa con ese ID.";
            }
            if (nueva.getId() == pedido.getMesa().getId()) {
                return "El pedido ya esta en esa mesa.";
            }
            if (nueva.isOcupada()) {
                return "La mesa " + nueva.getNumero() + " esta ocupada.";
            }

            Pedido modificado = new Pedido(pedido.getId(), pedido.getFecha(), pedido.getEstado(),
                    pedido.getTotal(), pedido.getMesero(), nueva);

            pedidoDAO.actualizar(modificado);
            mesaDAO.actualizarEstado(pedido.getMesa().getId(), Mesa.ESTADO_DISPONIBLE);
            mesaDAO.actualizarEstado(nueva.getId(), Mesa.ESTADO_OCUPADA);

            return "Pedido movido a la mesa " + nueva.getNumero() + ".";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    // Solo se eliminan pedidos pendientes o cancelados.
    public String eliminarPedido(int idPedido) {
        try {
            Pedido pedido = pedidoDAO.buscarPorId(idPedido);
            if (pedido == null) {
                return "No existe un pedido con ese ID.";
            }
            if (!pedido.getEstado().equals("PENDIENTE") && !pedido.getEstado().equals("CANCELADO")) {
                return "Solo se pueden eliminar pedidos pendientes o cancelados.";
            }

            if (pedidoDAO.eliminar(idPedido)) {
                return "Pedido eliminado.";
            }
            return "No se pudo eliminar el pedido.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    // =====================================================
    // 2. DETALLE DEL PEDIDO
    // =====================================================

    public String agregarProducto(int idPedido, int idProducto, int cantidad) {
        try {
            Pedido pedido = pedidoDAO.buscarPorId(idPedido);
            if (pedido == null) {
                return "No existe un pedido con ese ID.";
            }
            if (!pedido.getEstado().equals("PENDIENTE")) {
                return "Solo se puede modificar un pedido pendiente.";
            }
            if (cantidad <= 0) {
                return "La cantidad debe ser mayor a 0.";
            }

            Producto producto = productoDAO.buscarPorId(idProducto);
            if (producto == null) {
                return "No existe un producto con ese ID.";
            }
            if (!producto.isDisponible()) {
                return "El producto no esta disponible.";
            }

            DetallePedido detalle = new DetallePedido(0, producto, cantidad);
            detalleDAO.guardar(idPedido, detalle);
            recalcularTotal(idPedido);

            return "Producto agregado al pedido.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public String cambiarCantidad(int idPedido, int idDetalle, int cantidad) {
        try {
            Pedido pedido = pedidoDAO.buscarPorId(idPedido);
            if (pedido == null) {
                return "No existe un pedido con ese ID.";
            }
            if (!pedido.getEstado().equals("PENDIENTE")) {
                return "Solo se puede modificar un pedido pendiente.";
            }
            if (!detalleEstaEnPedido(pedido, idDetalle)) {
                return "Ese detalle no pertenece al pedido.";
            }
            if (cantidad <= 0) {
                return "La cantidad debe ser mayor a 0.";
            }

            detalleDAO.actualizarCantidad(idDetalle, cantidad);
            recalcularTotal(idPedido);

            return "Cantidad actualizada.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public String quitarProducto(int idPedido, int idDetalle) {
        try {
            Pedido pedido = pedidoDAO.buscarPorId(idPedido);
            if (pedido == null) {
                return "No existe un pedido con ese ID.";
            }
            if (!pedido.getEstado().equals("PENDIENTE")) {
                return "Solo se puede modificar un pedido pendiente.";
            }
            if (!detalleEstaEnPedido(pedido, idDetalle)) {
                return "Ese detalle no pertenece al pedido.";
            }

            detalleDAO.eliminar(idDetalle);
            recalcularTotal(idPedido);

            return "Producto quitado del pedido.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public List<DetallePedido> listarDetalles(int idPedido) {
        try {
            return detalleDAO.listarPorPedido(idPedido);
        } catch (SQLException e) {
            System.out.println("Error al listar detalles: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // =====================================================
    // 3. CONSULTAR PEDIDOS
    // =====================================================

    public List<Pedido> listarPedidos() {
        try {
            return pedidoDAO.listarTodos();
        } catch (SQLException e) {
            System.out.println("Error al listar pedidos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Pedido buscarPedido(int id) {
        try {
            return pedidoDAO.buscarPorId(id);
        } catch (SQLException e) {
            System.out.println("Error al buscar pedido: " + e.getMessage());
            return null;
        }
    }

    // Es la lista que ve la cocina.
    public List<Pedido> listarPedidosPendientes() {
        return listarPedidosPorEstado("PENDIENTE");
    }

    public List<Pedido> listarPedidosPorEstado(String estado) {
        try {
            return pedidoDAO.listarPorEstado(estado);
        } catch (SQLException e) {
            System.out.println("Error al listar pedidos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // =====================================================
    // 4. CAMBIAR ESTADO
    // =====================================================

    // Flujo: PENDIENTE -> EN_PREPARACION -> LISTO -> ENTREGADO (o CANCELADO).
    public String cambiarEstado(int idPedido, String nuevoEstado) {
        try {
            Pedido pedido = pedidoDAO.buscarPorId(idPedido);
            if (pedido == null) {
                return "No existe un pedido con ese ID.";
            }
            if (nuevoEstado == null) {
                return "El estado es obligatorio.";
            }

            nuevoEstado = nuevoEstado.trim().toUpperCase().replace(" ", "_").replace("Ó", "O");
            String actual = pedido.getEstado();

            if (!transicionValida(actual, nuevoEstado)) {
                return "No se puede pasar de " + actual + " a " + nuevoEstado + ".";
            }
            if (nuevoEstado.equals("EN_PREPARACION") && pedido.getDetalles().isEmpty()) {
                return "El pedido no tiene productos.";
            }

            if (pedidoDAO.actualizarEstado(idPedido, nuevoEstado)) {
                return "Pedido " + idPedido + " ahora esta " + nuevoEstado + ".";
            }
            return "No se pudo cambiar el estado.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    // Atajos para cocina y mesero.
    public String prepararPedido(int idPedido) {
        return cambiarEstado(idPedido, "EN_PREPARACION");
    }

    public String marcarListo(int idPedido) {
        return cambiarEstado(idPedido, "LISTO");
    }

    public String entregarPedido(int idPedido) {
        return cambiarEstado(idPedido, "ENTREGADO");
    }

    public String cancelarPedido(int idPedido) {
        return cambiarEstado(idPedido, "CANCELADO");
    }

    // =====================================================
    // 5. HISTORIAL DE ESTADOS
    // =====================================================

    public List<HistorialEstado> verHistorial(int idPedido) {
        try {
            return historialDAO.listarPorPedido(idPedido);
        } catch (SQLException e) {
            System.out.println("Error al consultar el historial: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Minutos entre EN_PREPARACION y LISTO.
    public String tiempoDePreparacion(int idPedido) {
        try {
            if (pedidoDAO.buscarPorId(idPedido) == null) {
                return "No existe un pedido con ese ID.";
            }

            Date inicio = null;
            Date fin = null;

            for (HistorialEstado registro : historialDAO.listarPorPedido(idPedido)) {
                if (registro.getEstado().equals("EN_PREPARACION") && inicio == null) {
                    inicio = registro.getFechaHora();
                }
                if (registro.getEstado().equals("LISTO") && fin == null) {
                    fin = registro.getFechaHora();
                }
            }

            if (inicio == null || fin == null) {
                return "El pedido todavia no ha terminado de prepararse.";
            }

            double minutos = (fin.getTime() - inicio.getTime()) / 60000.0;
            return "Tiempo de preparacion: " + String.format("%.1f", minutos) + " minutos.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    // =====================================================
    // METODOS DE APOYO
    // =====================================================

    private boolean transicionValida(String actual, String nuevo) {
        if (actual.equals("PENDIENTE") && nuevo.equals("EN_PREPARACION")) {
            return true;
        }
        if (actual.equals("EN_PREPARACION") && nuevo.equals("LISTO")) {
            return true;
        }
        if (actual.equals("LISTO") && nuevo.equals("ENTREGADO")) {
            return true;
        }
        // Se puede cancelar mientras el pedido no haya terminado.
        if (nuevo.equals("CANCELADO") && !actual.equals("ENTREGADO") && !actual.equals("CANCELADO")) {
            return true;
        }
        return false;
    }

    private boolean detalleEstaEnPedido(Pedido pedido, int idDetalle) {
        for (DetallePedido detalle : pedido.getDetalles()) {
            if (detalle.getId() == idDetalle) {
                return true;
            }
        }
        return false;
    }

    // Despues de cambiar los detalles hay que volver a calcular el total.
    private void recalcularTotal(int idPedido) throws SQLException {
        double total = 0;
        for (DetallePedido detalle : detalleDAO.listarPorPedido(idPedido)) {
            total += detalle.getSubtotal();
        }
        pedidoDAO.actualizarTotal(idPedido, total);
    }

    private int siguienteIdPedido() throws SQLException {
        int mayor = 0;
        for (Pedido pedido : pedidoDAO.listarTodos()) {
            if (pedido.getId() > mayor) {
                mayor = pedido.getId();
            }
        }
        return mayor + 1;
    }
}
