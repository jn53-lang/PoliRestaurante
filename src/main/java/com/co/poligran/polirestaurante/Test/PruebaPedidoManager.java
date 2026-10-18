package com.co.poligran.polirestaurante.Test;

import com.co.poligran.polirestaurante.BackEnd.AdminManager;
import com.co.poligran.polirestaurante.BackEnd.PedidoManager;
import com.co.poligran.polirestaurante.Entityes.DetallePedido;
import com.co.poligran.polirestaurante.Entityes.HistorialEstado;
import com.co.poligran.polirestaurante.Entityes.Mesa;
import com.co.poligran.polirestaurante.Entityes.Pedido;
import com.co.poligran.polirestaurante.Entityes.Producto;
import com.co.poligran.polirestaurante.Entityes.Usuario;

/**
 * Prueba rapida del PedidoManager. Usa el AdminManager para crear
 * un mesero, una mesa y un producto, y luego recorre todo el flujo
 * de un pedido. Se puede ejecutar varias veces.
 */
public class PruebaPedidoManager {

    public static void main(String[] args) throws InterruptedException {
        AdminManager admin = new AdminManager();
        PedidoManager pedidos = new PedidoManager();
        String sufijo = String.valueOf(System.currentTimeMillis() % 100000);

        // ---- Datos de apoyo (los crea el administrador) ----
        admin.registrarUsuario("Mesero " + sufijo, "mesero" + sufijo + "@poli.com", "Mesero123", "Mesero");
        admin.crearMesa(100 + (int) (System.currentTimeMillis() % 800), 4);
        admin.crearProducto("Pizza " + sufijo, 25000, "Pizza personal", true, null);
        admin.crearProducto("Sopa " + sufijo, 9000, "Sopa del dia", true, null);

        int idMesero = 0;
        for (Usuario u : admin.listarUsuarios()) {
            idMesero = Math.max(idMesero, u.getId());
        }
        int idMesa = 0;
        for (Mesa m : admin.listarMesas()) {
            idMesa = Math.max(idMesa, m.getId());
        }
        int idSopa = 0;
        int idPizza;
        for (Producto p : admin.listarProductos()) {
            idSopa = Math.max(idSopa, p.getId());   // la sopa se creo de ultima
        }
        idPizza = idSopa - 1;                       // y la pizza justo antes

        System.out.println("=== 1. CREAR PEDIDO ===");
        System.out.println(pedidos.crearPedido(idMesero, idMesa));
        System.out.println("(debe fallar, mesa ocupada) " + pedidos.crearPedido(idMesero, idMesa));
        int idPedido = 0;
        for (Pedido p : pedidos.listarPedidos()) {
            idPedido = Math.max(idPedido, p.getId());
        }

        System.out.println("\n=== 2. DETALLE ===");
        System.out.println("(debe fallar, sin productos) " + pedidos.prepararPedido(idPedido));
        System.out.println(pedidos.agregarProducto(idPedido, idPizza, 2));
        System.out.println(pedidos.agregarProducto(idPedido, idSopa, 1));
        System.out.println("(debe fallar) " + pedidos.agregarProducto(idPedido, idSopa, 0));
        int idDetalleSopa = 0;
        for (DetallePedido d : pedidos.listarDetalles(idPedido)) {
            System.out.println("  " + d.getProducto().getNombre() + " x" + d.getCantidad() + " = " + d.getSubtotal());
            idDetalleSopa = d.getId();
        }
        System.out.println(pedidos.cambiarCantidad(idPedido, idDetalleSopa, 3));
        System.out.println("Total del pedido: " + pedidos.buscarPedido(idPedido).getTotal() + " (esperado 77000.0)");
        System.out.println(pedidos.quitarProducto(idPedido, idDetalleSopa));
        System.out.println("Total del pedido: " + pedidos.buscarPedido(idPedido).getTotal() + " (esperado 50000.0)");

        System.out.println("\n=== 3. CONSULTAR ===");
        System.out.println("Pendientes: " + pedidos.listarPedidosPendientes().size());

        System.out.println("\n=== 4. ESTADOS ===");
        System.out.println(pedidos.prepararPedido(idPedido));
        Thread.sleep(1500);
        System.out.println("(debe fallar, salto de estado) " + pedidos.entregarPedido(idPedido));
        System.out.println(pedidos.marcarListo(idPedido));
        System.out.println(pedidos.entregarPedido(idPedido));
        System.out.println("(debe fallar) " + pedidos.cancelarPedido(idPedido));
        System.out.println("Mesa tras entregar: " + admin.buscarMesa(idMesa) + " (esperado DISPONIBLE)");

        System.out.println("\n=== 5. HISTORIAL ===");
        for (HistorialEstado h : pedidos.verHistorial(idPedido)) {
            System.out.println("  " + h.getEstado() + " - " + h.getFechaHora());
        }
        System.out.println(pedidos.tiempoDePreparacion(idPedido));

        System.out.println("\nPRUEBA FINALIZADA.");
    }
}
