/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.co.poligran.polirestaurante.Front;

import java.util.Scanner;

/**
 *
 * @author Stuve
 */
public class PoliRestaurante {

    public static void main(String[] args) {
                Scanner sc = new Scanner(System.in);

        System.out.println("Bienvenido al Poli Restaurante");
        System.out.println("Si es usuario frecuente recomandamos que inicie sesion, si no tiene una, creela");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println();
        System.out.println("Digite 1 para iniciar sesion");
        System.out.println("Digite 2 para crear usuario");
        
        int login = 2; 
        String usuario;
        String password;
        switch (login){
        case 1 -> {
            System.out.println(" Ingrese su usuario: ");
            usuario = sc.next();
            System.out.println(" Ingrese su contraseña: ");
            password = sc.next();
            }
        
        
    }
    }
}
