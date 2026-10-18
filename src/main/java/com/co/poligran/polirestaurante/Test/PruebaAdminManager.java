package com.co.poligran.polirestaurante.Test;

import com.co.poligran.polirestaurante.Managers.AdminManager;
import com.co.poligran.polirestaurante.Entityes.Categoria;
import com.co.poligran.polirestaurante.Entityes.Mesa;
import com.co.poligran.polirestaurante.Entityes.Producto;
import com.co.poligran.polirestaurante.Entityes.Usuario;

/**
 * Prueba rapida del AdminManager. Se puede ejecutar varias veces porque
 * usa un sufijo distinto en los nombres y correos.
 */
public class PruebaAdminManager {

    public static void main(String[] args) {
        AdminManager admin = new AdminManager();
        String sufijo = String.valueOf(System.currentTimeMillis() % 100000);

        System.out.println("=== 1. ACCESO ===");
        System.out.println(admin.registrarUsuario("Admin " + sufijo, "admin" + sufijo + "@poli.com", "Admin123", "Administrador"));
        System.out.println(admin.registrarUsuario("Mesero " + sufijo, "mesero" + sufijo + "@poli.com", "Mesero123", "Mesero"));
        System.out.println("(debe fallar) " + admin.registrarUsuario("X", "correo-malo", "123456", "Mesero"));

        Usuario sesion = admin.iniciarSesion("admin" + sufijo + "@poli.com", "Admin123");
        System.out.println("Login correcto: " + sesion);
        System.out.println("Login con clave mala (debe ser null): " + admin.iniciarSesion("admin" + sufijo + "@poli.com", "mala"));
        admin.cerrarSesion(sesion);

        System.out.println("\n=== 2. GESTION DE USUARIOS ===");
        for (Usuario u : admin.listarUsuarios()) {
            System.out.println(u.getId() + " | " + u.getClass().getSimpleName() + " | " + u);
        }
        System.out.println(admin.actualizarUsuario(sesion.getId(), "Admin Editado " + sufijo, "admin" + sufijo + "@poli.com", "", "Administrador"));
        System.out.println("(debe fallar) " + admin.eliminarUsuario(sesion.getId(), sesion.getId()));

        System.out.println("\n=== 4. CATEGORIAS ===");
        System.out.println(admin.crearCategoria("Platos " + sufijo, "Platos fuertes"));
        int idCategoria = 0;
        for (Categoria c : admin.listarCategorias()) {
            idCategoria = Math.max(idCategoria, c.getId());
        }

        System.out.println("\n=== 3. PRODUCTOS ===");
        System.out.println(admin.crearProducto("Hamburguesa " + sufijo, 18000, "Carne y queso", true, idCategoria));
        System.out.println("(debe fallar) " + admin.crearProducto("Gratis " + sufijo, 0, "x", true, null));
        int idProducto = 0;
        for (Producto p : admin.listarProductos()) {
            idProducto = Math.max(idProducto, p.getId());
        }
        System.out.println(admin.actualizarProducto(idProducto, "Hamburguesa " + sufijo, 19500, "Carne y queso", true, idCategoria));
        System.out.println(admin.cambiarDisponibilidad(idProducto));

        Categoria conProductos = admin.buscarCategoria(idCategoria);
        System.out.println("La categoria " + conProductos.getNombre() + " tiene: " + conProductos.getProductos());
        System.out.println("(debe fallar) " + admin.eliminarCategoria(idCategoria));
        System.out.println(admin.asignarProductoACategoria(idProducto, idCategoria));

        System.out.println("\n=== 5. MESAS ===");
        int numero = 100 + (int) (System.currentTimeMillis() % 800);
        System.out.println(admin.crearMesa(numero, 4));
        System.out.println("(debe fallar) " + admin.crearMesa(numero, 2));
        for (Mesa m : admin.listarMesasDisponibles()) {
            System.out.println("Disponible: " + m);
        }

        System.out.println("\nPRUEBA FINALIZADA.");
    }
}
