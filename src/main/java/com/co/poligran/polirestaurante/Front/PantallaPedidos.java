package com.co.poligran.polirestaurante.Front;

import com.co.poligran.polirestaurante.BackEnd.AdminManager;
import com.co.poligran.polirestaurante.BackEnd.PedidoManager;
import com.co.poligran.polirestaurante.Entityes.DetallePedido;
import com.co.poligran.polirestaurante.Entityes.HistorialEstado;
import com.co.poligran.polirestaurante.Entityes.Mesa;
import com.co.poligran.polirestaurante.Entityes.Pedido;
import com.co.poligran.polirestaurante.Entityes.Producto;
import com.co.poligran.polirestaurante.Entityes.Usuario;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Pantallas de pedidos. Hay un menu para el mesero, uno para cocina y uno
 * de consulta (que tambien usa el administrador). Todas llaman al PedidoManager.
 * El AdminManager solo se usa para mostrar las mesas y los productos disponibles.
 */
public class PantallaPedidos {

    private PedidoManager pedidos;
    private AdminManager admin;

    public PantallaPedidos(PedidoManager pedidos, AdminManager admin) {
        this.pedidos = pedidos;
        this.admin = admin;
    }

    // =====================================================
    // MENU DEL MESERO
    // =====================================================

    public void menuMesero(Usuario mesero) {
        int opcion;

        do {
            Consola.titulo("MENU MESERO");
            System.out.println("1. Crear pedido");
            System.out.println("2. Agregar productos a un pedido");
            System.out.println("3. Cambiar cantidad o quitar un producto");
            System.out.println("4. Ver mis pedidos");
            System.out.println("5. Entregar pedido");
            System.out.println("6. Cancelar pedido");
            System.out.println("7. Ver historial de un pedido");
            System.out.println("0. Cerrar sesion");
            opcion = Consola.leerEntero("Opcion: ");

            switch (opcion) {
                case 1 -> crearPedido(mesero);
                case 2 -> agregarProductos();
                case 3 -> modificarDetalle();
                case 4 -> verMisPedidos(mesero);
                case 5 -> System.out.println(pedidos.entregarPedido(Consola.leerEntero("ID del pedido: ")));
                case 6 -> System.out.println(pedidos.cancelarPedido(Consola.leerEntero("ID del pedido: ")));
                case 7 -> verHistorial();
                case 0 -> System.out.println("Cerrando sesion...");
                default -> System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    private void crearPedido(Usuario mesero) {
        System.out.println("--- Mesas disponibles ---");
        List<Mesa> libres = admin.listarMesasDisponibles();

        if (libres.isEmpty()) {
            System.out.println("No hay mesas disponibles.");
            return;
        }
        for (Mesa mesa : libres) {
            System.out.println("ID " + mesa.getId() + " | " + mesa);
        }

        int idMesa = Consola.leerEntero("ID de la mesa: ");
        System.out.println(pedidos.crearPedido(mesero.getId(), idMesa));
    }

    private void agregarProductos() {
        int idPedido = Consola.leerEntero("ID del pedido: ");

        System.out.println("--- Productos disponibles ---");
        for (Producto producto : admin.listarProductos()) {
            if (producto.isDisponible()) {
                System.out.println(producto);
            }
        }

        // Se pueden agregar varios productos seguidos; con 0 termina.
        int idProducto = Consola.leerEntero("ID del producto (0 para terminar): ");
        while (idProducto != 0) {
            int cantidad = Consola.leerEntero("Cantidad: ");
            System.out.println(pedidos.agregarProducto(idPedido, idProducto, cantidad));
            idProducto = Consola.leerEntero("ID del producto (0 para terminar): ");
        }
    }

    private void modificarDetalle() {
        int idPedido = Consola.leerEntero("ID del pedido: ");
        Pedido pedido = pedidos.buscarPedido(idPedido);

        if (pedido == null) {
            System.out.println("No existe un pedido con ese ID.");
            return;
        }

        mostrarPedido(pedido);
        int idDetalle = Consola.leerEntero("ID del detalle: ");

        System.out.println("1. Cambiar cantidad");
        System.out.println("2. Quitar producto");
        int opcion = Consola.leerEntero("Opcion: ");

        if (opcion == 1) {
            int cantidad = Consola.leerEntero("Nueva cantidad: ");
            System.out.println(pedidos.cambiarCantidad(idPedido, idDetalle, cantidad));
        } else if (opcion == 2) {
            System.out.println(pedidos.quitarProducto(idPedido, idDetalle));
        } else {
            System.out.println("Opcion no valida.");
        }
    }

    private void verMisPedidos(Usuario mesero) {
        System.out.println("--- Mis pedidos ---");
        boolean hayPedidos = false;

        for (Pedido pedido : pedidos.listarPedidos()) {
            if (pedido.getMesero().getId() == mesero.getId()) {
                mostrarPedido(pedido);
                hayPedidos = true;
            }
        }
        if (!hayPedidos) {
            System.out.println("No tiene pedidos.");
        }
    }

    // =====================================================
    // MENU DE COCINA
    // =====================================================

    public void menuCocina() {
        int opcion;

        do {
            Consola.titulo("MENU COCINA");
            System.out.println("1. Ver pedidos pendientes");
            System.out.println("2. Preparar un pedido");
            System.out.println("3. Ver pedidos en preparacion");
            System.out.println("4. Marcar pedido como listo");
            System.out.println("5. Ver historial de un pedido");
            System.out.println("0. Cerrar sesion");
            opcion = Consola.leerEntero("Opcion: ");

            switch (opcion) {
                case 1 -> mostrarLista(pedidos.listarPedidosPendientes());
                case 2 -> System.out.println(pedidos.prepararPedido(Consola.leerEntero("ID del pedido: ")));
                case 3 -> mostrarLista(pedidos.listarPedidosPorEstado("EN_PREPARACION"));
                case 4 -> System.out.println(pedidos.marcarListo(Consola.leerEntero("ID del pedido: ")));
                case 5 -> verHistorial();
                case 0 -> System.out.println("Cerrando sesion...");
                default -> System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    // =====================================================
    // MENU DE CONSULTA (para el administrador)
    // =====================================================

    public void menuConsulta() {
        int opcion;

        do {
            Consola.titulo("CONSULTAR PEDIDOS");
            System.out.println("1. Ver todos los pedidos");
            System.out.println("2. Ver pedidos por estado");
            System.out.println("3. Ver historial de un pedido");
            System.out.println("0. Volver");
            opcion = Consola.leerEntero("Opcion: ");

            switch (opcion) {
                case 1 -> mostrarLista(pedidos.listarPedidos());
                case 2 -> verPorEstado();
                case 3 -> verHistorial();
                case 0 -> { }
                default -> System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    private void verPorEstado() {
        System.out.println("Estados: PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, CANCELADO");
        String estado = Consola.leerTexto("Estado: ");

        mostrarLista(pedidos.listarPedidosPorEstado(estado));
    }

    // =====================================================
    // HISTORIAL (lo usan los tres menus)
    // =====================================================

    private void verHistorial() {
        int idPedido = Consola.leerEntero("ID del pedido: ");
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        System.out.println("--- Historial del pedido " + idPedido + " ---");
        List<HistorialEstado> historial = pedidos.verHistorial(idPedido);

        if (historial.isEmpty()) {
            System.out.println("No hay registros para ese pedido.");
            return;
        }
        for (HistorialEstado registro : historial) {
            System.out.println(formato.format(registro.getFechaHora()) + " - " + registro.getEstado());
        }
        System.out.println(pedidos.tiempoDePreparacion(idPedido));
    }

    // =====================================================
    // METODOS PARA MOSTRAR PEDIDOS
    // =====================================================

    private void mostrarLista(List<Pedido> lista) {
        if (lista.isEmpty()) {
            System.out.println("No hay pedidos.");
        }
        for (Pedido pedido : lista) {
            mostrarPedido(pedido);
        }
    }

    private void mostrarPedido(Pedido pedido) {
        System.out.println("Pedido #" + pedido.getId()
                + " | Mesa " + pedido.getMesa().getNumero()
                + " | Mesero: " + pedido.getMesero().getNombre()
                + " | " + pedido.getEstado()
                + " | Total: $" + pedido.getTotal());

        for (DetallePedido detalle : pedido.getDetalles()) {
            System.out.println("    (detalle " + detalle.getId() + ") "
                    + detalle.getProducto().getNombre()
                    + " x" + detalle.getCantidad()
                    + " = $" + detalle.getSubtotal());
        }
    }
}
