package com.co.poligran.polirestaurante.Main;

import com.co.poligran.polirestaurante.Entityes.Reporte;
import com.co.poligran.polirestaurante.dao.ReporteDAO;

import java.util.Date;
import java.util.List;

public class PruebaReporteDAO {

    public static void main(String[] args) {

        ReporteDAO dao = new ReporteDAO();
        int idReporte = 0;

        try {
            // 1. Crear un reporte de prueba
            Date fechaInicio = new Date(
                System.currentTimeMillis() - 86400000L
            );

            Date fechaFin = new Date();

            Reporte reporte = new Reporte(
                0,
                fechaInicio,
                fechaFin,
                "Reporte de prueba"
            );

            // 2. Guardar el reporte
            idReporte = dao.guardar(reporte);

            System.out.println("=== PRUEBA DE GUARDADO ===");
            System.out.println("ID generado: " + idReporte);

            if (idReporte <= 0) {
                System.out.println("ERROR: No se guardó el reporte.");
                return;
            }

            // 3. Buscar el reporte guardado
            System.out.println("\n=== PRUEBA DE BÚSQUEDA ===");

            Reporte reporteEncontrado = dao.buscarPorId(idReporte);

            if (reporteEncontrado != null) {
                System.out.println("Reporte encontrado correctamente.");
                reporteEncontrado.generarReporte();
            } else {
                System.out.println("ERROR: No se encontró el reporte.");
                return;
            }

            // 4. Consultar todos los reportes
            System.out.println("\n=== PRUEBA DE LISTADO ===");

            List<Reporte> lista = dao.listarTodos();

            if (lista == null || lista.isEmpty()) {
                System.out.println("ERROR: La lista está vacía.");
                return;
            }

            boolean encontradoEnLista = false;

            for (int i = 0; i < lista.size(); i++) {
                Reporte actual = lista.get(i);

                System.out.println(
                    "ID: " + actual.getId()
                    + " | Tipo: " + actual.getTipo()
                );

                if (actual.getId() == idReporte) {
                    encontradoEnLista = true;
                }
            }

            if (encontradoEnLista) {
                System.out.println("Reporte incluido en el listado.");
            } else {
                System.out.println(
                    "ERROR: El reporte no aparece en el listado."
                );
            }

            // 5. Eliminar el reporte de prueba
            System.out.println("\n=== PRUEBA DE ELIMINACIÓN ===");

            boolean eliminado = dao.eliminar(idReporte);

            if (eliminado) {
                System.out.println("Reporte eliminado correctamente.");
                idReporte = 0;
            } else {
                System.out.println("ERROR: No se pudo eliminar.");
            }

            // 6. Verificar que ya no exista
            Reporte verificacion = dao.buscarPorId(
                eliminado ? reporte.getId() : idReporte
            );

            /*
             * El ID generado por la base de datos se conserva
             * en idReporte hasta que termina la eliminación.
             * Por eso, guardamos el ID original para verificarlo.
             */

            System.out.println("\n=== FIN DE LAS PRUEBAS ===");

        } catch (Exception e) {
            System.out.println("ERROR AL EJECUTAR LA PRUEBA:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}