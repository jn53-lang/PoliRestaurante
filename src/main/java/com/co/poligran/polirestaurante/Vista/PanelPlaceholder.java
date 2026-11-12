package com.co.poligran.polirestaurante.Vista;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Panel de relleno para las secciones que todavia no se han construido.
 * En cada parte siguiente se reemplaza por el panel real.
 */
public class PanelPlaceholder extends JPanel {

    public PanelPlaceholder(String titulo, String detalle) {
        setLayout(new BorderLayout());
        setBackground(Estilo.COLOR_FONDO);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Estilo.FUENTE_TITULO);
        lblTitulo.setForeground(Estilo.COLOR_TEXTO);

        JLabel lblDetalle = new JLabel(detalle, SwingConstants.CENTER);
        lblDetalle.setFont(Estilo.FUENTE_NORMAL);
        lblDetalle.setForeground(Estilo.COLOR_SECUNDARIO);

        add(lblTitulo, BorderLayout.NORTH);
        add(lblDetalle, BorderLayout.CENTER);
    }
}
