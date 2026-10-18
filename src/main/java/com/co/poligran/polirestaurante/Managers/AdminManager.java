package com.co.poligran.polirestaurante.Managers;

import com.co.poligran.polirestaurante.Entityes.Administrador;
import com.co.poligran.polirestaurante.Entityes.Categoria;
import com.co.poligran.polirestaurante.Entityes.Cocina;
import com.co.poligran.polirestaurante.Entityes.DetallePedido;
import com.co.poligran.polirestaurante.Entityes.Mesa;
import com.co.poligran.polirestaurante.Entityes.Mesero;
import com.co.poligran.polirestaurante.Entityes.Pedido;
import com.co.poligran.polirestaurante.Entityes.Producto;
import com.co.poligran.polirestaurante.Entityes.Usuario;
import com.co.poligran.polirestaurante.config.BaseDatos;
import com.co.poligran.polirestaurante.dao.CategoriaDAO;
import com.co.poligran.polirestaurante.dao.MesaDAO;
import com.co.poligran.polirestaurante.dao.PedidoDAO;
import com.co.poligran.polirestaurante.dao.ProductoDAO;
import com.co.poligran.polirestaurante.dao.UsuarioDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager del Administrador. Sus 5 funcionalidades son:
 *   1. Acceso 
 *   2. Gestion de usuarios
 *   3. Productos
 *   4. Categorias
 *   5. Mesas
 *
 * Las operaciones que modifican datos devuelven un mensaje .
 * Las consultas devuelven los objetos, o una lista vacia / null si hay error.
 */
public class AdminManager {

    private UsuarioDAO usuarioDAO = new UsuarioDAO();
    private ProductoDAO productoDAO = new ProductoDAO();
    private CategoriaDAO categoriaDAO = new CategoriaDAO();
    private MesaDAO mesaDAO = new MesaDAO();
    private PedidoDAO pedidoDAO = new PedidoDAO();

    public AdminManager() {
        try {
            BaseDatos.inicializar();
        } catch (SQLException e) {
            System.out.println("Error al preparar la base de datos: " + e.getMessage());
        }
    }

    // =====================================================
    // 1. ACCESO
    // =====================================================

    public String registrarUsuario(String nombre, String correo, String password, String rol) {
        try {
            if (nombre == null || nombre.trim().isEmpty()) {
                return "El nombre es obligatorio.";
            }
            if (correo == null || !correo.contains("@")) {
                return "El correo no es valido.";
            }
            if (password == null || password.length() < 6) {
                return "La contrasena debe tener al menos 6 caracteres.";
            }

            String rolValido = normalizarRol(rol);
            if (rolValido == null) {
                return "Rol invalido. Use Administrador, Mesero o Cocina.";
            }

            correo = correo.trim().toLowerCase();
            if (usuarioDAO.buscarPorCorreo(correo) != null) {
                return "Ya existe un usuario con ese correo.";
            }

            int id = siguienteIdUsuario();
            Usuario usuario = crearUsuarioSegunRol(id, nombre.trim(), correo, password, rolValido);

            if (usuarioDAO.guardar(usuario)) {
                return "Usuario creado con ID " + id + ".";
            }
            return "No se pudo guardar el usuario.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    // Devuelve el usuario si los datos son correctos, y null si no.
    public Usuario iniciarSesion(String correo, String password) {
        try {
            if (correo == null || password == null) {
                return null;
            }

            Usuario usuario = usuarioDAO.buscarPorCorreo(correo.trim().toLowerCase());

            if (usuario != null && usuario.getPassword().equals(password)) {
                usuario.iniciarSesion();
                return usuario;
            }
        } catch (SQLException e) {
            System.out.println("Error al iniciar sesion: " + e.getMessage());
        }
        return null;
    }

    public void cerrarSesion(Usuario usuario) {
        if (usuario != null) {
            usuario.cerrarSesion();
        }
    }

    // =====================================================
    // 2. GESTION DE USUARIOS
    // =====================================================

    public List<Usuario> listarUsuarios() {
        try {
            return usuarioDAO.listarTodos();
        } catch (SQLException e) {
            System.out.println("Error al listar usuarios: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Usuario buscarUsuario(int id) {
        try {
            return usuarioDAO.buscarPorId(id);
        } catch (SQLException e) {
            System.out.println("Error al buscar usuario: " + e.getMessage());
            return null;
        }
    }

    // Si la contrasena llega vacia, se deja la que ya tenia.
    public String actualizarUsuario(int id, String nombre, String correo, String password, String rol) {
        try {
            Usuario actual = usuarioDAO.buscarPorId(id);
            if (actual == null) {
                return "No existe un usuario con ese ID.";
            }
            if (nombre == null || nombre.trim().isEmpty()) {
                return "El nombre es obligatorio.";
            }
            if (correo == null || !correo.contains("@")) {
                return "El correo no es valido.";
            }

            String rolValido = normalizarRol(rol);
            if (rolValido == null) {
                return "Rol invalido. Use Administrador, Mesero o Cocina.";
            }

            if (password == null || password.trim().isEmpty()) {
                password = actual.getPassword();
            } else if (password.length() < 6) {
                return "La contrasena debe tener al menos 6 caracteres.";
            }

            correo = correo.trim().toLowerCase();
            Usuario otro = usuarioDAO.buscarPorCorreo(correo);
            if (otro != null && otro.getId() != id) {
                return "Ese correo ya lo usa otro usuario.";
            }

            Usuario nuevo = crearUsuarioSegunRol(id, nombre.trim(), correo, password, rolValido);

            if (usuarioDAO.actualizar(nuevo)) {
                return "Usuario actualizado.";
            }
            return "No se pudo actualizar el usuario.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public String eliminarUsuario(int id, int idUsuarioActual) {
        try {
            if (usuarioDAO.buscarPorId(id) == null) {
                return "No existe un usuario con ese ID.";
            }
            if (id == idUsuarioActual) {
                return "No puede eliminar el usuario con el que inicio sesion.";
            }

            // Un usuario con pedidos no se elimina porque los pedidos quedarian sin mesero.
            for (Pedido pedido : pedidoDAO.listarTodos()) {
                if (pedido.getMesero().getId() == id) {
                    return "El usuario tiene pedidos y no se puede eliminar.";
                }
            }

            if (usuarioDAO.eliminar(id)) {
                return "Usuario eliminado.";
            }
            return "No se pudo eliminar el usuario.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    // =====================================================
    // 3. PRODUCTOS
    // =====================================================

    // idCategoria puede ser null si el producto no tiene categoria.
    public String crearProducto(String nombre, double precio, String descripcion,
                                boolean disponible, Integer idCategoria) {
        try {
            if (nombre == null || nombre.trim().isEmpty()) {
                return "El nombre es obligatorio.";
            }
            if (precio <= 0) {
                return "El precio debe ser mayor a 0.";
            }

            Categoria categoria = null;
            if (idCategoria != null) {
                categoria = categoriaDAO.buscarPorId(idCategoria);
                if (categoria == null) {
                    return "La categoria no existe.";
                }
            }

            int id = siguienteIdProducto();
            Producto producto = new Producto(id, nombre.trim(), precio, descripcion, disponible);
            producto.setCategoria(categoria);

            if (productoDAO.guardar(producto)) {
                return "Producto creado con ID " + id + ".";
            }
            return "No se pudo guardar el producto.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public List<Producto> listarProductos() {
        try {
            return productoDAO.listarTodos();
        } catch (SQLException e) {
            System.out.println("Error al listar productos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Producto buscarProducto(int id) {
        try {
            return productoDAO.buscarPorId(id);
        } catch (SQLException e) {
            System.out.println("Error al buscar producto: " + e.getMessage());
            return null;
        }
    }

    public String actualizarProducto(int id, String nombre, double precio, String descripcion,
                                     boolean disponible, Integer idCategoria) {
        try {
            if (productoDAO.buscarPorId(id) == null) {
                return "No existe un producto con ese ID.";
            }
            if (nombre == null || nombre.trim().isEmpty()) {
                return "El nombre es obligatorio.";
            }
            if (precio <= 0) {
                return "El precio debe ser mayor a 0.";
            }

            Categoria categoria = null;
            if (idCategoria != null) {
                categoria = categoriaDAO.buscarPorId(idCategoria);
                if (categoria == null) {
                    return "La categoria no existe.";
                }
            }

            Producto producto = new Producto(id, nombre.trim(), precio, descripcion, disponible);
            producto.setCategoria(categoria);

            if (productoDAO.actualizar(producto)) {
                return "Producto actualizado.";
            }
            return "No se pudo actualizar el producto.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public String cambiarDisponibilidad(int id) {
        try {
            Producto producto = productoDAO.buscarPorId(id);
            if (producto == null) {
                return "No existe un producto con ese ID.";
            }

            producto.cambiarDisponibilidad();
            productoDAO.actualizar(producto);

            if (producto.isDisponible()) {
                return "El producto ahora esta disponible.";
            }
            return "El producto ahora NO esta disponible.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public String eliminarProducto(int id) {
        try {
            if (productoDAO.buscarPorId(id) == null) {
                return "No existe un producto con ese ID.";
            }

            // Si ya se pidio alguna vez, es mejor dejarlo como no disponible.
            for (Pedido pedido : pedidoDAO.listarTodos()) {
                for (DetallePedido detalle : pedido.getDetalles()) {
                    if (detalle.getProducto().getId() == id) {
                        return "El producto esta en pedidos. Mejor marquelo como no disponible.";
                    }
                }
            }

            if (productoDAO.eliminar(id)) {
                return "Producto eliminado.";
            }
            return "No se pudo eliminar el producto.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    // =====================================================
    // 4. CATEGORIAS
    // =====================================================

    public String crearCategoria(String nombre, String descripcion) {
        try {
            if (nombre == null || nombre.trim().isEmpty()) {
                return "El nombre es obligatorio.";
            }

            int id = siguienteIdCategoria();
            Categoria categoria = new Categoria(id, nombre.trim(), descripcion);

            if (categoriaDAO.guardar(categoria)) {
                return "Categoria creada con ID " + id + ".";
            }
            return "No se pudo guardar la categoria.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public List<Categoria> listarCategorias() {
        try {
            return categoriaDAO.listarTodos();
        } catch (SQLException e) {
            System.out.println("Error al listar categorias: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Devuelve la categoria con sus productos cargados.
    public Categoria buscarCategoria(int id) {
        try {
            Categoria categoria = categoriaDAO.buscarPorId(id);

            if (categoria != null) {
                for (Producto producto : productoDAO.listarPorCategoria(id)) {
                    categoria.agregarProducto(producto);
                }
            }
            return categoria;

        } catch (SQLException e) {
            System.out.println("Error al buscar categoria: " + e.getMessage());
            return null;
        }
    }

    public String actualizarCategoria(int id, String nombre, String descripcion) {
        try {
            if (categoriaDAO.buscarPorId(id) == null) {
                return "No existe una categoria con ese ID.";
            }
            if (nombre == null || nombre.trim().isEmpty()) {
                return "El nombre es obligatorio.";
            }

            Categoria categoria = new Categoria(id, nombre.trim(), descripcion);

            if (categoriaDAO.actualizar(categoria)) {
                return "Categoria actualizada.";
            }
            return "No se pudo actualizar la categoria.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public String eliminarCategoria(int id) {
        try {
            if (categoriaDAO.buscarPorId(id) == null) {
                return "No existe una categoria con ese ID.";
            }
            if (!productoDAO.listarPorCategoria(id).isEmpty()) {
                return "La categoria tiene productos. Muevalos antes de eliminarla.";
            }

            if (categoriaDAO.eliminar(id)) {
                return "Categoria eliminada.";
            }
            return "No se pudo eliminar la categoria.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public String asignarProductoACategoria(int idProducto, int idCategoria) {
        try {
            Producto producto = productoDAO.buscarPorId(idProducto);
            if (producto == null) {
                return "No existe un producto con ese ID.";
            }

            Categoria categoria = categoriaDAO.buscarPorId(idCategoria);
            if (categoria == null) {
                return "No existe una categoria con ese ID.";
            }

            categoria.agregarProducto(producto);
            productoDAO.actualizar(producto);

            return "Producto asignado a " + categoria.getNombre() + ".";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    // =====================================================
    // 5. MESAS
    // =====================================================

    public String crearMesa(int numero, int capacidad) {
        try {
            if (numero <= 0 || capacidad <= 0) {
                return "El numero y la capacidad deben ser mayores a 0.";
            }
            if (numeroDeMesaRepetido(numero, -1)) {
                return "Ya existe la mesa " + numero + ".";
            }

            int id = siguienteIdMesa();
            Mesa mesa = new Mesa(id, numero, capacidad);

            if (mesaDAO.guardar(mesa)) {
                return "Mesa creada con ID " + id + ".";
            }
            return "No se pudo guardar la mesa.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public List<Mesa> listarMesas() {
        try {
            return mesaDAO.listarTodos();
        } catch (SQLException e) {
            System.out.println("Error al listar mesas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Mesa> listarMesasDisponibles() {
        List<Mesa> disponibles = new ArrayList<>();
        for (Mesa mesa : listarMesas()) {
            if (!mesa.isOcupada()) {
                disponibles.add(mesa);
            }
        }
        return disponibles;
    }

    public Mesa buscarMesa(int id) {
        try {
            return mesaDAO.buscarPorId(id);
        } catch (SQLException e) {
            System.out.println("Error al buscar mesa: " + e.getMessage());
            return null;
        }
    }

    // Cambia numero y capacidad. El estado de la mesa se conserva.
    public String actualizarMesa(int id, int numero, int capacidad) {
        try {
            Mesa actual = mesaDAO.buscarPorId(id);
            if (actual == null) {
                return "No existe una mesa con ese ID.";
            }
            if (numero <= 0 || capacidad <= 0) {
                return "El numero y la capacidad deben ser mayores a 0.";
            }
            if (numeroDeMesaRepetido(numero, id)) {
                return "Ya existe la mesa " + numero + ".";
            }

            Mesa nueva = new Mesa(id, numero, capacidad);
            if (actual.isOcupada()) {
                nueva.asignarMesa();
            }

            if (mesaDAO.actualizar(nueva)) {
                return "Mesa actualizada.";
            }
            return "No se pudo actualizar la mesa.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public String eliminarMesa(int id) {
        try {
            if (mesaDAO.buscarPorId(id) == null) {
                return "No existe una mesa con ese ID.";
            }

            // Una mesa con pedidos no se elimina porque los pedidos quedarian sin mesa.
            for (Pedido pedido : pedidoDAO.listarTodos()) {
                if (pedido.getMesa().getId() == id) {
                    return "La mesa tiene pedidos y no se puede eliminar.";
                }
            }

            if (mesaDAO.eliminar(id)) {
                return "Mesa eliminada.";
            }
            return "No se pudo eliminar la mesa.";

        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    // =====================================================
    // METODOS DE APOYO
    // =====================================================

    // Devuelve el rol con el formato de las clases hijas, o null si no es valido.
    private String normalizarRol(String rol) {
        if (rol == null) {
            return null;
        }
        if (rol.trim().equalsIgnoreCase("Administrador")) {
            return "Administrador";
        }
        if (rol.trim().equalsIgnoreCase("Mesero")) {
            return "Mesero";
        }
        if (rol.trim().equalsIgnoreCase("Cocina")) {
            return "Cocina";
        }
        return null;
    }

    // Polimorfismo: se crea la clase hija segun el rol.
    private Usuario crearUsuarioSegunRol(int id, String nombre, String correo, String password, String rol) {
        if (rol.equals("Administrador")) {
            return new Administrador(id, nombre, correo, password);
        }
        if (rol.equals("Mesero")) {
            return new Mesero(id, nombre, correo, password);
        }
        return new Cocina(id, nombre, correo, password);
    }

    private boolean numeroDeMesaRepetido(int numero, int idIgnorado) throws SQLException {
        for (Mesa mesa : mesaDAO.listarTodos()) {
            if (mesa.getNumero() == numero && mesa.getId() != idIgnorado) {
                return true;
            }
        }
        return false;
    }

    // Los DAO reciben el id ya asignado, asi que se calcula el siguiente.
    private int siguienteIdUsuario() throws SQLException {
        int mayor = 0;
        for (Usuario usuario : usuarioDAO.listarTodos()) {
            if (usuario.getId() > mayor) {
                mayor = usuario.getId();
            }
        }
        return mayor + 1;
    }

    private int siguienteIdProducto() throws SQLException {
        int mayor = 0;
        for (Producto producto : productoDAO.listarTodos()) {
            if (producto.getId() > mayor) {
                mayor = producto.getId();
            }
        }
        return mayor + 1;
    }

    private int siguienteIdCategoria() throws SQLException {
        int mayor = 0;
        for (Categoria categoria : categoriaDAO.listarTodos()) {
            if (categoria.getId() > mayor) {
                mayor = categoria.getId();
            }
        }
        return mayor + 1;
    }

    private int siguienteIdMesa() throws SQLException {
        int mayor = 0;
        for (Mesa mesa : mesaDAO.listarTodos()) {
            if (mesa.getId() > mayor) {
                mayor = mesa.getId();
            }
        }
        return mayor + 1;
    }
}
