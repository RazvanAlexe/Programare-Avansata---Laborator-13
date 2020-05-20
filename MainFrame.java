package ro.uaic.info.lab12;

import javax.swing.*;
import java.awt.BorderLayout;

public class MainFrame extends JFrame {

    ControlPanel CP;
    DesignPanel DP;

    public MainFrame() {

        super("My Drawing Application");
        init();
    }

    private void init() {
        // Proprietatile frame-ului
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(1500, 800);
        // adaugam componentele necesare (Design Panel si Control Panel)
        DP = new DesignPanel(this);
        DP.setLayout(null);
        
        CP = new ControlPanel(this);

        add(DP, BorderLayout.CENTER);
        add(CP, BorderLayout.NORTH);
    }
}
