package com.co.poligran.polirestaurante.Managers;

/**
 * Resultado de una operacion del Manager.
 * La pantalla solo necesita saber si salio bien y que mensaje mostrar.
 */
public record Resultado(boolean exito, String mensaje) {

    public static Resultado ok(String mensaje) {
        return new Resultado(true, mensaje);
    }

    public static Resultado error(String mensaje) {
        return new Resultado(false, mensaje);
    }
}
