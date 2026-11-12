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

/**
 * Panel principal del administrador: una barra lateral con el menu
 * y, a la derecha, la seccion que se este viendo (cada una es un JPanel).
 */
public class PanelAdmin extends JPanel {

    private static final Color COLOR_BARRA = Estilo.COLOR_PRIMARIO;
    private static final Color COLOR_ENCIMA = new Color(0x24, 0x4A, 0x75);

    private static final String[] SECCIONES = {"Usuarios", "Productos", "Categorías", "Mesas"};

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

    // Cada seccion es un panel. En las siguientes partes se reemplaza
    // el PanelPlaceholder por el panel real de esa seccion.
    private void crearSecciones() {
        contenido.setBackground(Estilo.COLOR_FONDO);

        contenido.add(new PanelPlaceholder("Gestión de usuarios", "Este panel se agrega en la Parte 3."), "Usuarios");
        contenido.add(new PanelPlaceholder("Productos", "Este panel se agrega en la Parte 4."), "Productos");
        contenido.add(new PanelPlaceholder("Categorías", "Este panel se agrega en la Parte 5."), "Categorías");
        contenido.add(new PanelPlaceholder("Mesas", "Este panel se agrega en la Parte 6."), "Mesas");
    }

    private JPanel crearBarraLateral(Runnable alCerrarSesion) {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(COLOR_BARRA);
        barra.setPreferredSize(new Dimension(220, 0));

        // Parte de arriba: datos del usuario y botones del menu.
        JPanel arriba = new JPanel();
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        arriba.setOpaque(false);
        arriba.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));

        arriba.add(crearTextoBarra("PoliRestaurante", Estilo.FUENTE_NEGRITA.deriveFont(18f), Color.WHITE));
        arriba.add(crearTextoBarra("Administrador", Estilo.FUENTE_NORMAL, Estilo.COLOR_ACENTO));
        arriba.add(crearTextoBarra(actual.getNombre(), Estilo.FUENTE_NORMAL, new Color(0xCF, 0xE8, 0xF5)));
        arriba.add(javax.swing.Box.createVerticalStrut(24));

        for (String seccion : SECCIONES) {
            JButton boton = crearBotonMenu(seccion);
            boton.addActionListener(e -> mostrarSeccion(seccion, boton));
            botonesMenu.add(boton);
            arriba.add(boton);
        }

        // Parte de abajo: cerrar sesion.
        JPanel abajo = new JPanel(new BorderLayout());
        abajo.setOpaque(false);
        abajo.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JButton btnCerrar = Estilo.crearBoton("Cerrar sesión", Estilo.COLOR_ACENTO);
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

        // Resalta el boton cuando el mouse pasa por encima.
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

    // Muestra la seccion elegida y marca su boton en rojo.
    private void mostrarSeccion(String nombre, JButton boton) {
        cartas.show(contenido, nombre);
        seleccionado = boton;
        pintarBotones();
    }

    private void pintarBotones() {
        for (JButton boton : botonesMenu) {
            boton.setBackground(boton == seleccionado ? Estilo.COLOR_ACENTO : COLOR_BARRA);
        }
    }
}
