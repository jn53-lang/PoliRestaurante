package com.co.poligran.polirestaurante.Front;

import com.co.poligran.polirestaurante.BackEnd.AdminManager;
import com.co.poligran.polirestaurante.Entityes.Categoria;
import com.co.poligran.polirestaurante.Entityes.Mesa;
import com.co.poligran.polirestaurante.Entityes.Producto;
import com.co.poligran.polirestaurante.Entityes.Usuario;

/**
 * Pantallas del Administrador. Cada menu corresponde a una funcionalidad
 * del AdminManager. La pantalla solo pide datos y muestra mensajes:
 * las validaciones y la base de datos las maneja el Manager.
 */
public class PantallaAdmin {

    private AdminManager admin;
    private PantallaPedidos pantallaPedidos;
    private Usuario actual;   // administrador que inicio sesion

    public PantallaAdmin(AdminManager admin, PantallaPedidos pantallaPedidos, Usuario actual) {
        this.admin = admin;
        this.pantallaPedidos = pantallaPedidos;
        this.actual = actual;
    }

    // =====================================================
    // MENU PRINCIPAL DEL ADMINISTRADOR
    // =====================================================

    public void mostrarMenu() {
        int opcion;

        do {
            Consola.titulo("MENU ADMINISTRADOR");
            System.out.println("1. Gestion de usuarios");
            System.out.println("2. Productos");
            System.out.println("3. Categorias");
            System.out.println("4. Mesas");
            System.out.println("5. Consultar pedidos");
            System.out.println("0. Cerrar sesion");
            opcion = Consola.leerEntero("Opcion: ");

            switch (opcion) {
                case 1 -> menuUsuarios();
                case 2 -> menuProductos();
                case 3 -> menuCategorias();
                case 4 -> menuMesas();
                case 5 -> pantallaPedidos.menuConsulta();
                case 0 -> System.out.println("Cerrando sesion...");
                default -> System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    // =====================================================
    // GESTION DE USUARIOS
    // =====================================================

    private void menuUsuarios() {
        int opcion;

        do {
            Consola.titulo("GESTION DE USUARIOS");
            System.out.println("1. Crear usuario");
            System.out.println("2. Listar usuarios");
            System.out.println("3. Actualizar usuario");
            System.out.println("4. Eliminar usuario");
            System.out.println("0. Volver");
            opcion = Consola.leerEntero("Opcion: ");

            switch (opcion) {
                case 1 -> crearUsuario();
                case 2 -> listarUsuarios();
                case 3 -> actualizarUsuario();
                case 4 -> eliminarUsuario();
                case 0 -> { }
                default -> System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    private void crearUsuario() {
        String nombre = Consola.leerTexto("Nombre: ");
        String correo = Consola.leerTexto("Correo: ");
        String password = Consola.leerTexto("Contrasena (minimo 6 caracteres): ");
        String rol = Consola.leerTexto("Rol (Administrador, Mesero o Cocina): ");

        System.out.println(admin.registrarUsuario(nombre, correo, password, rol));
    }

    private void listarUsuarios() {
        System.out.println("--- Usuarios ---");
        for (Usuario usuario : admin.listarUsuarios()) {
            System.out.println(usuario.getId() + " | " + usuario);
        }
    }

    private void actualizarUsuario() {
        int id = Consola.leerEntero("ID del usuario: ");
        Usuario usuario = admin.buscarUsuario(id);

        if (usuario == null) {
            System.out.println("No existe un usuario con ese ID.");
            return;
        }

        System.out.println("Deje vacio lo que no quiera cambiar.");
        String nombre = Consola.conservar(Consola.leerTexto("Nombre [" + usuario.getNombre() + "]: "), usuario.getNombre());
        String correo = Consola.conservar(Consola.leerTexto("Correo [" + usuario.getCorreo() + "]: "), usuario.getCorreo());
        String password = Consola.leerTexto("Nueva contrasena (vacio = la misma): ");
        String rol = Consola.conservar(Consola.leerTexto("Rol [" + usuario.getRol() + "]: "), usuario.getRol());

        System.out.println(admin.actualizarUsuario(id, nombre, correo, password, rol));
    }

    private void eliminarUsuario() {
        int id = Consola.leerEntero("ID del usuario a eliminar: ");

        if (Consola.leerSiNo("Seguro que desea eliminarlo?")) {
            System.out.println(admin.eliminarUsuario(id, actual.getId()));
        }
    }

    // =====================================================
    // PRODUCTOS
    // =====================================================

    private void menuProductos() {
        int opcion;

        do {
            Consola.titulo("PRODUCTOS");
            System.out.println("1. Crear producto");
            System.out.println("2. Listar productos");
            System.out.println("3. Actualizar producto");
            System.out.println("4. Cambiar disponibilidad");
            System.out.println("5. Eliminar producto");
            System.out.println("0. Volver");
            opcion = Consola.leerEntero("Opcion: ");

            switch (opcion) {
                case 1 -> crearProducto();
                case 2 -> listarProductos();
                case 3 -> actualizarProducto();
                case 4 -> cambiarDisponibilidad();
                case 5 -> eliminarProducto();
                case 0 -> { }
                default -> System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    private void crearProducto() {
        String nombre = Consola.leerTexto("Nombre: ");
        double precio = Consola.leerDecimal("Precio: ");
        String descripcion = Consola.leerTexto("Descripcion: ");
        boolean disponible = Consola.leerSiNo("Esta disponible?");

        listarCategorias();
        int categoria = Consola.leerEntero("ID de la categoria (0 si no tiene): ");
        Integer idCategoria = (categoria == 0) ? null : categoria;

        System.out.println(admin.crearProducto(nombre, precio, descripcion, disponible, idCategoria));
    }

    private void listarProductos() {
        System.out.println("--- Productos ---");
        for (Producto producto : admin.listarProductos()) {
            mostrarProducto(producto);
        }
    }

    private void actualizarProducto() {
        int id = Consola.leerEntero("ID del producto: ");
        Producto producto = admin.buscarProducto(id);

        if (producto == null) {
            System.out.println("No existe un producto con ese ID.");
            return;
        }

        mostrarProducto(producto);
        System.out.println("Digite los nuevos datos:");
        String nombre = Consola.leerTexto("Nombre: ");
        double precio = Consola.leerDecimal("Precio: ");
        String descripcion = Consola.leerTexto("Descripcion: ");
        boolean disponible = Consola.leerSiNo("Esta disponible?");

        listarCategorias();
        int categoria = Consola.leerEntero("ID de la categoria (0 si no tiene): ");
        Integer idCategoria = (categoria == 0) ? null : categoria;

        System.out.println(admin.actualizarProducto(id, nombre, precio, descripcion, disponible, idCategoria));
    }

    private void cambiarDisponibilidad() {
        int id = Consola.leerEntero("ID del producto: ");
        System.out.println(admin.cambiarDisponibilidad(id));
    }

    private void eliminarProducto() {
        int id = Consola.leerEntero("ID del producto a eliminar: ");

        if (Consola.leerSiNo("Seguro que desea eliminarlo?")) {
            System.out.println(admin.eliminarProducto(id));
        }
    }

    private void mostrarProducto(Producto producto) {
        String categoria = "Sin categoria";
        if (producto.getCategoria() != null) {
            categoria = producto.getCategoria().getNombre();
        }
        System.out.println(producto + " | Categoria: " + categoria);
    }

    // =====================================================
    // CATEGORIAS
    // =====================================================

    private void menuCategorias() {
        int opcion;

        do {
            Consola.titulo("CATEGORIAS");
            System.out.println("1. Crear categoria");
            System.out.println("2. Listar categorias");
            System.out.println("3. Ver productos de una categoria");
            System.out.println("4. Actualizar categoria");
            System.out.println("5. Eliminar categoria");
            System.out.println("6. Asignar producto a una categoria");
            System.out.println("0. Volver");
            opcion = Consola.leerEntero("Opcion: ");

            switch (opcion) {
                case 1 -> crearCategoria();
                case 2 -> listarCategorias();
                case 3 -> verProductosDeCategoria();
                case 4 -> actualizarCategoria();
                case 5 -> eliminarCategoria();
                case 6 -> asignarProducto();
                case 0 -> { }
                default -> System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    private void crearCategoria() {
        String nombre = Consola.leerTexto("Nombre: ");
        String descripcion = Consola.leerTexto("Descripcion: ");

        System.out.println(admin.crearCategoria(nombre, descripcion));
    }

    private void listarCategorias() {
        System.out.println("--- Categorias ---");
        for (Categoria categoria : admin.listarCategorias()) {
            System.out.println(categoria.getId() + " | " + categoria.getNombre());
        }
    }

    private void verProductosDeCategoria() {
        int id = Consola.leerEntero("ID de la categoria: ");
        Categoria categoria = admin.buscarCategoria(id);

        if (categoria == null) {
            System.out.println("No existe una categoria con ese ID.");
            return;
        }

        System.out.println("Productos de " + categoria.getNombre() + ":");
        if (categoria.getProductos().isEmpty()) {
            System.out.println("  (ninguno)");
        }
        for (Producto producto : categoria.getProductos()) {
            System.out.println("  " + producto);
        }
    }

    private void actualizarCategoria() {
        int id = Consola.leerEntero("ID de la categoria: ");
        String nombre = Consola.leerTexto("Nuevo nombre: ");
        String descripcion = Consola.leerTexto("Nueva descripcion: ");

        System.out.println(admin.actualizarCategoria(id, nombre, descripcion));
    }

    private void eliminarCategoria() {
        int id = Consola.leerEntero("ID de la categoria a eliminar: ");

        if (Consola.leerSiNo("Seguro que desea eliminarla?")) {
            System.out.println(admin.eliminarCategoria(id));
        }
    }

    private void asignarProducto() {
        listarProductos();
        int idProducto = Consola.leerEntero("ID del producto: ");
        listarCategorias();
        int idCategoria = Consola.leerEntero("ID de la categoria: ");

        System.out.println(admin.asignarProductoACategoria(idProducto, idCategoria));
    }

    // =====================================================
    // MESAS
    // =====================================================

    private void menuMesas() {
        int opcion;

        do {
            Consola.titulo("MESAS");
            System.out.println("1. Crear mesa");
            System.out.println("2. Listar mesas");
            System.out.println("3. Actualizar mesa");
            System.out.println("4. Eliminar mesa");
            System.out.println("0. Volver");
            opcion = Consola.leerEntero("Opcion: ");

            switch (opcion) {
                case 1 -> crearMesa();
                case 2 -> listarMesas();
                case 3 -> actualizarMesa();
                case 4 -> eliminarMesa();
                case 0 -> { }
                default -> System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    private void crearMesa() {
        int numero = Consola.leerEntero("Numero de mesa: ");
        int capacidad = Consola.leerEntero("Capacidad (personas): ");

        System.out.println(admin.crearMesa(numero, capacidad));
    }

    private void listarMesas() {
        System.out.println("--- Mesas ---");
        for (Mesa mesa : admin.listarMesas()) {
            System.out.println("ID " + mesa.getId() + " | " + mesa);
        }
    }

    private void actualizarMesa() {
        int id = Consola.leerEntero("ID de la mesa: ");
        int numero = Consola.leerEntero("Nuevo numero de mesa: ");
        int capacidad = Consola.leerEntero("Nueva capacidad: ");

        System.out.println(admin.actualizarMesa(id, numero, capacidad));
    }

    private void eliminarMesa() {
        int id = Consola.leerEntero("ID de la mesa a eliminar: ");

        if (Consola.leerSiNo("Seguro que desea eliminarla?")) {
            System.out.println(admin.eliminarMesa(id));
        }
    }
}
