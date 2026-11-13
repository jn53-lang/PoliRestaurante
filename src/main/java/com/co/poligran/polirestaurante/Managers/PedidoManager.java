
package com.co.poligran.polirestaurante.Managers;

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

public class PedidoManager {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final DetallePedidoDAO detalleDAO = new DetallePedidoDAO();
    private final HistorialEstadoDAO historialDAO = new HistorialEstadoDAO();
    private final MesaDAO mesaDAO = new MesaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public PedidoManager() {
        try {
            BaseDatos.inicializar();
        } catch (SQLException e) {
            System.out.println("Error al preparar la base de datos: " + e.getMessage());
        }
    }

    // 1. CREAR PEDIDO

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

            Mesero mesero = new Mesero(
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getCorreo(),
                    usuario.getPassword()
            );

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

            int idMesaAnterior = pedido.getMesa().getId();

            Pedido modificado = new Pedido(
                    pedido.getId(),
                    pedido.getFecha(),
                    pedido.getEstado(),
                    pedido.getTotal(),
                    pedido.getMesero(),
                    nueva
            );

            if (!pedidoDAO.actualizar(modificado)) {
                return "No se pudo actualizar la mesa del pedido.";
            }

            if (!mesaDAO.actualizarEstado(idMesaAnterior, Mesa.ESTADO_DISPONIBLE)) {
                pedidoDAO.actualizar(pedido);
                return "No se pudo liberar la mesa anterior.";
            }

            if (!mesaDAO.actualizarEstado(idMesaNueva, Mesa.ESTADO_OCUPADA)) {
                pedidoDAO.actualizar(pedido);
                mesaDAO.actualizarEstado(idMesaAnterior, Mesa.ESTADO_OCUPADA);
                return "No se pudo ocupar la nueva mesa.";
            }

            return "Pedido movido a la mesa " + nueva.getNumero() + ".";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public String eliminarPedido(int idPedido) {
        try {
            Pedido pedido = pedidoDAO.buscarPorId(idPedido);

            if (pedido == null) {
                return "No existe un pedido con ese ID.";
            }

            if (!pedido.getEstado().equals("PENDIENTE")
                    && !pedido.getEstado().equals("CANCELADO")) {
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

    // 2. DETALLE DEL PEDIDO

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

            if (!detalleDAO.guardar(idPedido, detalle)) {
                return "No se pudo agregar el producto.";
            }

            if (!recalcularTotal(idPedido)) {
                return "El producto se agrego, pero no se pudo actualizar el total.";
            }

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

            if (!detalleDAO.actualizarCantidad(idDetalle, cantidad)) {
                return "No se pudo actualizar la cantidad.";
            }

            if (!recalcularTotal(idPedido)) {
                return "La cantidad se actualizo, pero no se pudo actualizar el total.";
            }

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

            if (!detalleDAO.eliminar(idDetalle)) {
                return "No se pudo quitar el producto.";
            }

            if (!recalcularTotal(idPedido)) {
                return "El producto se quito, pero no se pudo actualizar el total.";
            }

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

    // 3. CONSULTAR PEDIDOS

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

    public List<Pedido> listarPedidosPendientes() {
        return listarPedidosPorEstado("PENDIENTE");
    }

    public List<Pedido> listarPedidosPorEstado(String estado) {
        try {
            if (estado == null || estado.trim().isEmpty()) {
                return new ArrayList<>();
            }

            return pedidoDAO.listarPorEstado(estado);

        } catch (SQLException e) {
            System.out.println("Error al listar pedidos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // 4. CAMBIAR ESTADO

    public String cambiarEstado(int idPedido, String nuevoEstado) {
        try {
            Pedido pedido = pedidoDAO.buscarPorId(idPedido);

            if (pedido == null) {
                return "No existe un pedido con ese ID.";
            }

            if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
                return "El estado es obligatorio.";
            }

            nuevoEstado = nuevoEstado.trim()
                    .toUpperCase()
                    .replace(" ", "_")
                    .replace("Ó", "O");

            String actual = pedido.getEstado();

            if (!transicionValida(actual, nuevoEstado)) {
                return "No se puede pasar de " + actual + " a " + nuevoEstado + ".";
            }

            if (nuevoEstado.equals("EN_PREPARACION")
                    && pedido.getDetalles().isEmpty()) {
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

    // 5. HISTORIAL DE ESTADOS

    public List<HistorialEstado> verHistorial(int idPedido) {
        try {
            return historialDAO.listarPorPedido(idPedido);
        } catch (SQLException e) {
            System.out.println("Error al consultar el historial: " + e.getMessage());
            return new ArrayList<>();
        }
    }

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

            if (fin.before(inicio)) {
                return "Las fechas del historial no son validas.";
            }

            double minutos = (fin.getTime() - inicio.getTime()) / 60000.0;

            return "Tiempo de preparacion: "
                    + String.format("%.1f", minutos) + " minutos.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    // METODOS DE APOYO

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

        if (nuevo.equals("CANCELADO")
                && !actual.equals("ENTREGADO")
                && !actual.equals("CANCELADO")) {
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

    private boolean recalcularTotal(int idPedido) throws SQLException {

        double total = 0;

        for (DetallePedido detalle : detalleDAO.listarPorPedido(idPedido)) {
            total += detalle.getSubtotal();
        }

        return pedidoDAO.actualizarTotal(idPedido, total);
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
