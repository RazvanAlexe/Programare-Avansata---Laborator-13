package ro.uaic.info.misc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;

import javax.swing.JButton;

public class RoundButton extends JButton {
    public RoundButton(String label) {
        super(label);
        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed( MouseEvent e )
            {
                System.out.println("Clicked");
            }
        };
        addMouseListener( mouseListener );

        setSize(80,80);
        setContentAreaFilled(false);
    }
    
    @Override
    protected void paintComponent(Graphics g) {

    if (getModel().isArmed()) {
            // Daca e apasat
            g.setColor(Color.lightGray);
        } else {
            // Altfel
            g.setColor(getBackground());
        }
    // Ovalul butonului
    g.fillOval(0, 0, getSize().width-1, getSize().height-1);
    super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        // Ovalul ce inconjoara butonul
        g.setColor(getForeground());
        g.drawOval(0, 0, getSize().width-1, getSize().height-1);
    }

    Shape shape;
    @Override
    public boolean contains(int x, int y) {
        // Suprascriem functia de verificare a coordonatelor de incadrare
        // Verificam folosind forma unui Oval inloc de Dreptunghi
        if (shape == null || !shape.getBounds().equals(getBounds())) {
            shape = new Ellipse2D.Float(0, 0, getWidth(), getHeight());
        }
        return shape.contains(x, y);
    }

}
