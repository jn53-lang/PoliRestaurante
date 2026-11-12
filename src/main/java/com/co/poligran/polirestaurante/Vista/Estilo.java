package com.co.poligran.polirestaurante.Vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * Colores, fuentes y componentes con el mismo aspecto para todas las ventanas.
 * Asi si se quiere cambiar el estilo, se cambia solo aqui.
 */
public class Estilo {

    public static final Color COLOR_PRIMARIO = new Color(0xC0, 0x39, 0x2B);
    public static final Color COLOR_PRIMARIO_OSCURO = new Color(0x96, 0x2B, 0x20);
    public static final Color COLOR_SECUNDARIO = new Color(0x7F, 0x8C, 0x8D);
    public static final Color COLOR_FONDO = new Color(0xFD, 0xF6, 0xEC);
    public static final Color COLOR_TEXTO = new Color(0x2C, 0x3E, 0x50);

    public static final Font FUENTE_NORMAL = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    public static final Font FUENTE_NEGRITA = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    public static final Font FUENTE_TITULO = new Font(Font.SANS_SERIF, Font.BOLD, 28);

    // Se llama una vez al iniciar el programa.
    public static void aplicarTema() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // Si falla, se queda con el aspecto por defecto.
        }
    }

    public static JButton crearBoton(String texto, Color fondo) {
        final Color normal = fondo;
        final Color encima = fondo.darker();

        JButton boton = new JButton(texto);
        boton.setFont(FUENTE_NEGRITA);
        boton.setForeground(Color.WHITE);
        boton.setBackground(normal);
        boton.setOpaque(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(160, 40));

        // Cambia de color cuando el mouse pasa por encima.
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(encima);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(normal);
            }
        });

        return boton;
    }

    public static JLabel crearEtiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(FUENTE_NEGRITA);
        etiqueta.setForeground(COLOR_TEXTO);
        return etiqueta;
    }

    public static JTextField crearCampo() {
        JTextField campo = new JTextField();
        configurarCampo(campo);
        return campo;
    }

    public static JPasswordField crearCampoPassword() {
        JPasswordField campo = new JPasswordField();
        configurarCampo(campo);
        return campo;
    }

    private static void configurarCampo(JTextField campo) {
        campo.setFont(FUENTE_NORMAL);
        campo.setForeground(COLOR_TEXTO);
        campo.setPreferredSize(new Dimension(260, 36));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_SECUNDARIO),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "PoliRestaurante",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "PoliRestaurante",
                JOptionPane.ERROR_MESSAGE);
    }
}
