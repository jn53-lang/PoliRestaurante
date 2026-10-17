/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.co.poligran.polirestaurante.Main;

import com.co.poligran.polirestaurante.Entityes.Administrador;
import com.co.poligran.polirestaurante.Entityes.Categoria;
import com.co.poligran.polirestaurante.Entityes.Cocina;
import com.co.poligran.polirestaurante.Entityes.Mesa;
import com.co.poligran.polirestaurante.Entityes.Mesero;
import com.co.poligran.polirestaurante.Entityes.Pedido;
import com.co.poligran.polirestaurante.Entityes.Producto;
import com.co.poligran.polirestaurante.Entityes.Reporte;
import com.co.poligran.polirestaurante.Entityes.Usuario;
import java.util.Scanner;
import java.util.Date;

public class PoliRestaurante {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Usuario admin = new Usuario(10913674, " Juan ", "Juanadm@gmail.com", "Admin123", "Administrador");
        System.out.println(admin);
        System.out.println("Bienvenido al Poli Restaurante");
        System.out.println("Si es usuario frecuente recomandamos que inicie sesion, si no tiene una, creela");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println();
        System.out.println("Digite 1 para iniciar sesion");
        System.out.println("Digite 2 para crear usuario");
            int login = sc.nextInt(); 
        
        switch (login){
        case 1 -> {
            System.out.println(" Ingrese su usuario: ");
            String usuario = sc.next();
            System.out.println(" Ingrese su contraseña: ");
            String password = sc.next();
            System.out.println(" Bienvenido: "+usuario);
            }
        case 2 -> {
            System.out.println(" Registre su usuario: ");
            String usuario = sc.next();
            System.out.println(" Registre su contraseña: ");
            String password = sc.next();
            System.out.println(" Confirme su contraseña: ");
            String password2 = sc.next();
            while (password2 != password){
                System.out.println(" Las contraseñas no coinciden");
                System.out.println(" Digite nuevamente la contraseña ");
                String password3 = sc.next();
                if (password3 == password){
                                                break;

            }
        }

        }
    }
    }
}


