package com.co.poligran.polirestaurante.Test;

import com.co.poligran.polirestaurante.Entityes.Pedido;
import com.co.poligran.polirestaurante.Entityes.DetallePedido;
import com.co.poligran.polirestaurante.Entityes.HistorialEstado;
import com.co.poligran.polirestaurante.dao.PedidoDAO;
import java.util.List;

public class PruebaPedidoDAO {

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("       PRUEBA DEL PEDIDO DAO");
        System.out.println("======================================");

        PedidoDAO pedidoDAO = new PedidoDAO();

        try {
            // 1. Verificar y crear las tablas necesarias
            pedidoDAO.crearTablas();

            System.out.println("\nTablas verificadas correctamente.");

            // 2. Consultar todos los pedidos registrados
            System.out.println("\n--- PEDIDOS EXISTENTES ---");

            List<Pedido> pedidos = pedidoDAO.listarTodos();

            if (pedidos == null || pedidos.isEmpty()) {

                System.out.println("No hay pedidos registrados.");
                System.out.println(
                    "Crea un pedido desde la aplicación para probar el guardado."
                );

            } else {

                for (Pedido pedido : pedidos) {

                    System.out.println("\n--------------------------------------");
                    System.out.println("Pedido ID: " + pedido.getId());
                    System.out.println("Estado: " + pedido.getEstado());
                    System.out.println("Fecha: " + pedido.getFecha());
                    System.out.println("Total: $" + pedido.getTotal());

                    if (pedido.getMesero() != null) {
                        System.out.println(
                            "Mesero: " + pedido.getMesero().getNombre()
                        );
                    }

                    if (pedido.getMesa() != null) {
                        System.out.println(
                            "Mesa: " + pedido.getMesa().getNumero()
                        );
                    }

                    // 3. Mostrar los detalles del pedido
                    System.out.println("\n--- DETALLES DEL PEDIDO ---");

                    List<DetallePedido> detalles = pedido.getDetalles();

                    if (detalles == null || detalles.isEmpty()) {

                        System.out.println(
                            "Este pedido no tiene detalles registrados."
                        );

                    } else {

                        for (DetallePedido detalle : detalles) {

                            String nombreProducto = "Producto desconocido";

                            if (detalle.getProducto() != null) {
                                nombreProducto =
                                    detalle.getProducto().getNombre();
                            }

                            System.out.println(
                                "Producto: " + nombreProducto
                                + " | Cantidad: " + detalle.getCantidad()
                                + " | Precio unitario: $"
                                + detalle.getPrecioUnitario()
                                + " | Subtotal: $" + detalle.getSubtotal()
                            );
                        }
                    }

                    // 4. Mostrar el historial de estados y sus fechas
                    System.out.println("\n--- HISTORIAL DE ESTADOS ---");

                    List<HistorialEstado> historial =
                        pedido.getHistorial();

                    if (historial == null || historial.isEmpty()) {

                        System.out.println(
                            "Este pedido no tiene historial registrado."
                        );

                    } else {

                        for (HistorialEstado registro : historial) {

                            System.out.println(
                                "ID: " + registro.getId()
                                + " | Estado: " + registro.getEstado()
                                + " | Fecha: " + registro.getFechaHora()
                            );
                        }
                    }

                    System.out.println("--------------------------------------");
                }
            }

            // 5. Probar la búsqueda de un pedido específico
            System.out.println("\n--- PRUEBA DE BUSQUEDA POR ID ---");

            if (pedidos != null && !pedidos.isEmpty()) {

                int idPedido = pedidos.get(0).getId();

                Pedido pedidoEncontrado =
                    pedidoDAO.buscarPorId(idPedido);

                if (pedidoEncontrado != null) {

                    System.out.println(
                        "Pedido encontrado correctamente."
                    );
                    System.out.println(
                        "ID consultado: " + pedidoEncontrado.getId()
                    );
                    System.out.println(
                        "Estado actual: " + pedidoEncontrado.getEstado()
                    );

                } else {

                    System.out.println(
                        "No se encontro el pedido con ID: " + idPedido
                    );
                }

            } else {

                System.out.println(
                    "No se puede probar la busqueda porque no hay pedidos."
                );
            }

            System.out.println("\n======================================");
            System.out.println("       PRUEBA FINALIZADA");
            System.out.println("======================================");

        } catch (Exception e) {

            System.err.println(
                "\nError durante la prueba del PedidoDAO:"
            );

            e.printStackTrace();
        }
    }
}