package com.co.poligran.polirestaurante.Managers;

/**
 * Esta clase sirve para saber si hubo algun error en operacion o si salio bien
 * Sin necesidad de que rebote el codigo o entre en bucle.
 */
public record Verificador(boolean exito, String mensaje) {

    public static Verificador ok(String mensaje) {
        return new Verificador(true, mensaje);
    }

    public static Verificador error(String mensaje) {
        return new Verificador(false, mensaje);
    }
}
