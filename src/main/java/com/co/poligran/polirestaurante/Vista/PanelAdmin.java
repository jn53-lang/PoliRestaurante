
package com.co.poligran.polirestaurante.Vista;

import com.co.poligran.polirestaurante.Managers.AdminManager;
import com.co.poligran.polirestaurante.Entityes.Usuario;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class PanelAdmin extends JPanel {

    private static final Color COLOR_BARRA = Estilo.COLOR_PRIMARIO;
    private static final Color COLOR_ENCIMA = new Color(0x24, 0x4A, 0x75);

    private static final String[] SECCIONES = {
        "Usuarios", "Productos", "Categorías", "Mesas"
    };

    private AdminManager admin;
    private Usuario actual;

    private CardLayout cartas = new CardLayout();
    private JPanel contenido = new JPanel(cartas);

    private List<JButton> botonesMenu = new ArrayList<>();
    private JButton seleccionado;

    public PanelAdmin(AdminManager admin, Usuario actual, Runnable alCerrarSesion) {
        this.admin = admin;
        this.actual = actual;

        setLayout(new BorderLayout());
        add(crearBarraLateral(alCerrarSesion), BorderLayout.WEST);

        crearSecciones();
        add(contenido, BorderLayout.CENTER);

        mostrarSeccion(SECCIONES[0], botonesMenu.get(0));
    }

    private void crearSecciones() {
        contenido.setBackground(Estilo.COLOR_FONDO);

        contenido.add(new PanelUsuarios(admin, actual), "Usuarios");
        contenido.add(new PanelProductos(admin), "Productos");
        contenido.add(new PanelCategorias(admin), "Categorías");
        contenido.add(new PanelMesas(admin), "Mesas");
    }

    private JPanel crearBarraLateral(Runnable alCerrarSesion) {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(COLOR_BARRA);
        barra.setPreferredSize(new Dimension(220, 0));

        JPanel arriba = new JPanel();
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        arriba.setOpaque(false);
        arriba.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));

        arriba.add(crearTextoBarra(
                "PoliRestaurante",
                Estilo.FUENTE_NEGRITA.deriveFont(18f),
                Color.WHITE
        ));

        arriba.add(crearTextoBarra(
                "Administrador",
                Estilo.FUENTE_NORMAL,
                Estilo.COLOR_ACENTO
        ));

        arriba.add(crearTextoBarra(
                actual.getNombre(),
                Estilo.FUENTE_NORMAL,
                new Color(0xCF, 0xE8, 0xF5)
        ));

        arriba.add(javax.swing.Box.createVerticalStrut(24));

        for (String seccion : SECCIONES) {
            JButton boton = crearBotonMenu(seccion);

            boton.addActionListener(e -> mostrarSeccion(seccion, boton));

            botonesMenu.add(boton);
            arriba.add(boton);
        }

        JPanel abajo = new JPanel(new BorderLayout());
        abajo.setOpaque(false);
        abajo.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JButton btnCerrar = Estilo.crearBoton(
                "Cerrar sesión",
                Estilo.COLOR_ACENTO
        );

        btnCerrar.addActionListener(e -> alCerrarSesion.run());

        abajo.add(btnCerrar, BorderLayout.CENTER);

        barra.add(arriba, BorderLayout.NORTH);
        barra.add(abajo, BorderLayout.SOUTH);

        return barra;
    }

    private JLabel crearTextoBarra(String texto, java.awt.Font fuente, Color color) {
        JLabel etiqueta = new JLabel(texto);

        etiqueta.setFont(fuente);
        etiqueta.setForeground(color);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(0, 20, 4, 10));
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);

        return etiqueta;
    }

    private JButton crearBotonMenu(String texto) {
        JButton boton = new JButton(texto);

        boton.setFont(Estilo.FUENTE_NORMAL);
        boton.setForeground(Color.WHITE);
        boton.setBackground(COLOR_BARRA);
        boton.setOpaque(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 10));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        boton.setPreferredSize(new Dimension(220, 46));

        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (boton != seleccionado) {
                    boton.setBackground(COLOR_ENCIMA);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                pintarBotones();
            }
        });

        return boton;
    }

    private void mostrarSeccion(String nombre, JButton boton) {
        cartas.show(contenido, nombre);
        seleccionado = boton;
        pintarBotones();
    }

    private void pintarBotones() {
        for (JButton boton : botonesMenu) {
            boton.setBackground(
                    boton == seleccionado
                    ? Estilo.COLOR_ACENTO
                    : COLOR_BARRA
            );
        }
    }
}
