package com.co.poligran.polirestaurante.Class;

import com.co.poligran.polirestaurante.Managers.AdminManager;
import com.co.poligran.polirestaurante.Entityes.Usuario;

/**
 * Inicio de Sesion y Registro de usuario.
 */
public class AccesoLogin {

    private AdminManager admin;

    public AccesoLogin(AdminManager admin) {
        this.admin = admin;
    }

    // Devuelve el usuario que inicio sesion, o null si decide salir.
    public Usuario iniciar() {

        // En dado caso que se borren los datos, se tiene que agregar un administrador de primeras
        if (admin.listarUsuarios().isEmpty()) {
            crearPrimerAdministrador();
        }

        while (true) {
            Consola.titulo("INICIO DE SESION");
            System.out.println("1. Iniciar sesion");
            System.out.println("2. Crear usuario");
            System.out.println("0. Salir");
            int opcion = Consola.leerEntero("Opcion: ");

            if (opcion == 0) {
                return null;
            }

            if (opcion == 1) {
                Usuario usuario = pedirCredenciales();

                if (usuario != null) {
                    return usuario;
                }
            } else if (opcion == 2) {
                crearUsuario();
            } else {
                System.out.println("Opcion no valida.");
            }
        }
    }

    private Usuario pedirCredenciales() {
        String correo = Consola.leerTexto("Correo: ");
        String password = Consola.leerTexto("Contrasena: ");

        Usuario usuario = admin.iniciarSesion(correo, password);

        if (usuario != null) {
            System.out.println("Bienvenido, " + usuario.getNombre() + " (" + usuario.getRol() + ")");
            return usuario;
        }

        System.out.println("Correo o contrasena incorrectos.");
        return null;
    }

    // Registro desde el login: solo se permite Mesero o Cocina.
    // Los administradores solo pueden ser creador por otro administrador
    private void crearUsuario() {
        Consola.titulo("CREAR USUARIO");
        String nombre = Consola.leerTexto("Nombre: ");
        String correo = Consola.leerTexto("Correo: ");
        String password = Consola.leerTexto("Contrasena (minimo 6 caracteres): ");
        String confirmacion = Consola.leerTexto("Confirme la contrasena: ");

        if (!password.equals(confirmacion)) {
            System.out.println("Las contrasenas no coinciden.");
            return;
        }

        String rol = Consola.leerTexto("Rol (Mesero o Cocina): ");

        if (rol.equalsIgnoreCase("Administrador")) {
            System.out.println("Solo un administrador puede crear otro administrador.");
            return;
        }

        System.out.println(admin.registrarUsuario(nombre, correo, password, rol));
    }

    private void crearPrimerAdministrador() {
        Consola.titulo("PRIMER USO: CREE EL ADMINISTRADOR");
        String mensaje;

        do {
            String nombre = Consola.leerTexto("Nombre: ");
            String correo = Consola.leerTexto("Correo: ");
            String password = Consola.leerTexto("Contrasena (minimo 6 caracteres): ");

            mensaje = admin.registrarUsuario(nombre, correo, password, "Administrador");
            System.out.println(mensaje);
        } while (!mensaje.startsWith("Usuario creado"));
    }
}
