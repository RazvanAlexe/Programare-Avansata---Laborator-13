package ro.uaic.info.lab12;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class DesignPanel extends JPanel {

    final MainFrame frame;
    final static int W = 1500, H = 600;
    private int NextComponentX;
    private int NextComponentY;

    private ArrayList<Integer> IDs;
    private int NextID;

    BufferedImage image;
    Graphics2D graphics;

    private void createOffscreenImage() {
        // Functia de desenare a backgroundului
        image = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, W, H);
    }

    public DesignPanel(MainFrame frame) {
        this.frame = frame;
        createOffscreenImage();
        init();
    }

    public void reset(){
        // Resetam ID-ul componentelor
        NextID = 0;
        IDs = new ArrayList<>();
    }
    
    public int getNextID() {
        return NextID;
    }

    public void setNextID(int NextID) {
        this.NextID = NextID;
    }

    public void addElement(Object e, int id) {
        // Adaugam o componenta
        IDs.add(id);
        NextID++;
        add((Component) e);
    }

    private void init() {
        // Coordonatele urmatorui element
        NextComponentX = 100;
        NextComponentY = 100;
        
        // ID-urile componentelor
        NextID = 0;
        IDs = new ArrayList<>();

        // Listener-ul ce ne da coordonatele
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Mouse X si Mouse Y
                NextComponentX = e.getPoint().x;
                NextComponentY = e.getPoint().y;
                frame.CP.coordLabel.setText("X: " + frame.DP.getNextComponentX() + " Y:" + frame.DP.getNextComponentY());
                frame.CP.repaint();
                frame.DP.repaint();
            }
        }
        );
        // Dimensiuni...
        setPreferredSize(new Dimension(W, H));
        setBorder(BorderFactory.createEtchedBorder());
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Deseneaza fundalul
        g.drawImage(image, 0, 0, this);
        // Deseneaza punctul de determinare a componentei urmatoare
        g.setColor(Color.RED);
        g.fillRect(NextComponentX, NextComponentY, 3, 3);
    }

    public int getNextComponentX() {
        return NextComponentX;
    }

    public int getNextComponentY() {
        return NextComponentY;
    }

}
