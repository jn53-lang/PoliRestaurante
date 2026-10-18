package com.co.poligran.polirestaurante.Class;

import java.util.Scanner;

/**
 * Metodos para leer datos del teclado. Todas las pantallas usan esta clase,
 * asi hay un solo Scanner y no se repite codigo.
 */
public class Consola {

    private static Scanner sc = new Scanner(System.in);

    // Lee una linea completa (siempre con nextLine para no dejar saltos de linea sueltos).
    public static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return sc.nextLine().trim();
    }

    // Repite la pregunta hasta que el usuario escriba un numero entero.
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

    // Si el usuario no escribio nada, se conserva el valor actual.
    public static String conservar(String nuevo, String actual) {
        return nuevo.isEmpty() ? actual : nuevo;
    }

    public static void titulo(String texto) {
        System.out.println();
        System.out.println("===== " + texto + " =====");
    }
}
