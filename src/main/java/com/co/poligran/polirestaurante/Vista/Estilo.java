package com.co.poligran.polirestaurante.Vista;

import java.awt.BorderLayout;
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
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * Colores, fuentes y componentes con el mismo aspecto para todas las ventanas.
 * Asi si se quiere cambiar el estilo, se cambia solo aqui.
 */
public class Estilo {

    // Colores tomados del logo del Politecnico Grancolombiano.
    public static final Color COLOR_PRIMARIO = new Color(0x17, 0x35, 0x57);   // azul oscuro del logo
    public static final Color COLOR_ACENTO = new Color(0x3D, 0xB2, 0xDE);     // azul claro del logo
    public static final Color COLOR_SECUNDARIO = new Color(0x6B, 0x7C, 0x8F); // gris azulado
    public static final Color COLOR_FONDO = new Color(0xF4, 0xF8, 0xFB);      // blanco azulado
    public static final Color COLOR_TEXTO = new Color(0x17, 0x35, 0x57);

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

    // Franja roja con titulo y subtitulo. La usan el login y el registro.
    public static JPanel crearEncabezado(String titulo, String subtitulo) {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(COLOR_PRIMARIO);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, COLOR_ACENTO),
                BorderFactory.createEmptyBorder(28, 20, 24, 20)));

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(FUENTE_TITULO);
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel(subtitulo, SwingConstants.CENTER);
        lblSubtitulo.setFont(FUENTE_NORMAL);
        lblSubtitulo.setForeground(new Color(0xCF, 0xE8, 0xF5));

        encabezado.add(lblTitulo, BorderLayout.CENTER);
        encabezado.add(lblSubtitulo, BorderLayout.SOUTH);
        return encabezado;
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
