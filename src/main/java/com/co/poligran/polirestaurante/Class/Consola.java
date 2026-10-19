package com.co.poligran.polirestaurante.Class;

import java.util.Scanner;

/**
 * Metodos para leer datos del teclado. Todas las clases usan esta clase,
 * asi hay un solo Scanner y no se repite codigo.
 */
public class Consola {

    private static Scanner sc = new Scanner(System.in);

    // Lee una linea completa, sin saltar texto o lineas.
    public static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return sc.nextLine().trim();
    }

    
    public static int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Digite un numero entero valido.");
            }
        }
    }

    public static double leerDecimal(String mensaje) {
        while (true) {
            try {
                return Double.parseDouble(leerTexto(mensaje).replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.println("Digite un numero valido.");
            }
        }
    }

    public static boolean leerSiNo(String mensaje) {
        String respuesta = leerTexto(mensaje + " (s/n): ");
        return respuesta.equalsIgnoreCase("s") || respuesta.equalsIgnoreCase("si");
    }

    // Si no se escribe nada se ejecuta esta linea, asi no cambia nada
    public static String conservar(String nuevo, String actual) {
        return nuevo.isEmpty() ? actual : nuevo;
    }

    public static void titulo(String texto) {
        System.out.println();
        System.out.println("===== " + texto + " =====");
    }
}
