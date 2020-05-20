package ro.uaic.info.lab12;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TableModelEvent;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

public class ControlPanel extends JPanel {

    final MainFrame frame;

    JButton createBtn = new JButton("Create");
    JButton exitBtn = new JButton("Exit");
    JButton resetBtn = new JButton("Reset");
    JButton loadBtn = new JButton("Load");
    JButton saveBtn = new JButton("Save");

    JLabel nameLabel = new JLabel("Name: ");
    JTextField nameField = new JTextField(10);
    JLabel classLabel = new JLabel("Class: ");
    JTextField classField = new JTextField(10);
    JLabel coordLabel = new JLabel();

    JTable propTable = new JTable();
    JScrollPane scrollPane = new JScrollPane();

    public ControlPanel(MainFrame frame) {
        this.frame = frame;
        init();
    }

    private void init() {
        // Adaugam componentele de control
        coordLabel.setText("X: " + frame.DP.getNextComponentX() + " Y:" + frame.DP.getNextComponentY());

        createBtn.addActionListener(this::create);
        resetBtn.addActionListener(this::reset);
        exitBtn.addActionListener(this::exit);
        loadBtn.addActionListener(this::load);
        saveBtn.addActionListener(this::save);

        add(nameLabel);
        add(nameField);
        add(classLabel);
        add(classField);
        add(coordLabel);
        add(createBtn);
        add(resetBtn);
        add(loadBtn);
        add(saveBtn);
        add(exitBtn);
        scrollPane.setPreferredSize(new Dimension(1400, 80));
        scrollPane.setViewportView(propTable);
        add(scrollPane);
    }

    private void create(ActionEvent e) {
        // Functia de creeare a componentei noi
        String input = classField.getText();
        String name = nameField.getText();
        if (!input.equals("")) {
            String classname = "javax.swing." + input;
            Class c = null;
            Component o = null;
            boolean found_class = true;
            // Constrium clasa componentei
            try {
                c = Class.forName(classname);
            } catch (ClassNotFoundException ex) {
                found_class = false;
                Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (found_class) {
                // Daca e o subclasa a clasei Component putem folosi toate proprietatile Component
                try {
                    try {
                        o = (Component) c.newInstance();
                    } catch (InstantiationException | IllegalAccessException ex) {
                        Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // Obtinem X-ul si Y-ul mouse-ului
                    int X = frame.DP.getNextComponentX();
                    int Y = frame.DP.getNextComponentY();

                    int ID = frame.DP.getNextID();

                    // Invocam functia de mutare a componenentei
                    Method[] m;

                    m = o.getClass().getMethods();
                    for (Method m1 : m) {
                        if (m1.getName().equals("setBounds")) {
                            try {
                                Class[] paramTypes = new Class[4];
                                paramTypes[0] = int.class;
                                paramTypes[1] = int.class;
                                paramTypes[2] = int.class;
                                paramTypes[3] = int.class;
                                Method method = o.getClass().getMethod("setBounds", paramTypes);
                                Integer[] params = new Integer[4];
                                params[0] = X;
                                params[1] = Y;
                                params[2] = 150;
                                params[3] = 20;
                                method.invoke(o, (Object[]) params);
                            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                    // Invocam functia de setare text a componenentei
                    m = o.getClass().getMethods();
                    for (Method m1 : m) {
                        if (m1.getName().equals("setText")) {
                            try {
                                Method method = o.getClass().getMethod("setText", String.class);
                                method.invoke(o, name);
                            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                    // Adaugam listener-ul ce ne permite sa vedem tabela
                    o.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            try {
                                // Aflam informatiile legate de componenta folosind BeanInfo
                                BeanInfo beanInfo = Introspector.getBeanInfo(frame.DP.getComponent(ID).getClass());
                                // Pregatim datele pentru tabel
                                ArrayList<String> columns = new ArrayList<>();
                                int n = beanInfo.getPropertyDescriptors().length;
                                ArrayList<Object>[] data = new ArrayList[n];
                                for (int i = 0; i < n; i++) {
                                    data[i] = new ArrayList<>();
                                }
                                // Parcurgem lista de proprietati si alegem cele de tip int si string
                                for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                                    if (pd.getPropertyType() != null) {
                                        if (pd.getPropertyType().isAssignableFrom(String.class)) {
                                            columns.add(pd.getName());
                                            data[0].add(pd.getReadMethod().invoke(frame.DP.getComponent(ID)));
                                        } else if (pd.getPropertyType().isAssignableFrom(int.class)) {
                                            columns.add(pd.getName());
                                            data[0].add(pd.getReadMethod().invoke(frame.DP.getComponent(ID)));
                                        }
                                    }
                                }
                                // Constrium tabela cu datele obtinute
                                String[] Columns = new String[columns.size()];
                                Columns = columns.toArray(Columns);
                                Object[][] Data = new Object[1][data[0].size()];
                                Data[0] = data[0].toArray(Data[0]);
                                propTable = new JTable(Data, Columns);
                                for (int i = 0; i < propTable.getColumnCount(); i++) {
                                    propTable.getColumnModel().getColumn(i).setPreferredWidth(100);
                                }
                                // Adaugam listener-ul de modificare tabel
                                propTable.getModel().addTableModelListener((TableModelEvent evt) -> {
                                    int NewX = Integer.parseInt((String) propTable.getValueAt(0, propTable.getColumn("x").getModelIndex()).toString());
                                    int NewY = Integer.parseInt((String) propTable.getValueAt(0, propTable.getColumn("y").getModelIndex()).toString());
                                    int NewWidth = Integer.parseInt((String) propTable.getValueAt(0, propTable.getColumn("width").getModelIndex()).toString());
                                    int NewHeight = Integer.parseInt((String) propTable.getValueAt(0, propTable.getColumn("height").getModelIndex()).toString());
                                    for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                                        if (pd.getPropertyType() != null) {
                                            if (pd.getPropertyType().isAssignableFrom(java.lang.String.class)) {
                                                if (pd.getWriteMethod() != null) {
                                                    try {
                                                        pd.getWriteMethod().invoke(frame.DP.getComponent(ID), propTable.getValueAt(0, propTable.getColumn(pd.getName()).getModelIndex()));
                                                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                                        Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    // Nu exista functie de set X sau Y deci folosim o invocare a functiei setBounds
                                    Method[] m = frame.DP.getComponent(ID).getClass().getMethods();
                                    for (Method m1 : m) {
                                        if (m1.getName().equals("setBounds")) {
                                            try {
                                                Class[] paramTypes = new Class[4];
                                                paramTypes[0] = int.class;
                                                paramTypes[1] = int.class;
                                                paramTypes[2] = int.class;
                                                paramTypes[3] = int.class;
                                                Method method = frame.DP.getComponent(ID).getClass().getMethod("setBounds", paramTypes);
                                                Integer[] params = new Integer[4];
                                                params[0] = NewX;
                                                params[1] = NewY;
                                                params[2] = NewWidth;
                                                params[3] = NewHeight;
                                                method.invoke(frame.DP.getComponent(ID), (Object[]) params);
                                            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                                Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        }
                                    }
                                    // Facem tabela noua vizibila
                                    scrollPane.setViewportView(propTable);
                                    frame.DP.revalidate();
                                });
                                // Facem tabela noua vizibila
                                scrollPane.setViewportView(propTable);
                                frame.CP.revalidate();
                            } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    });

                    frame.DP.addElement(o, frame.DP.getNextID());
                    frame.DP.repaint();
                } catch (SecurityException ex) {
                    Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                // Verificam daca e o componenta SWING custom
                if (input.equals("RoundButton")) {
                    // Incarcam JAR-ul cu libraria de butoane
                    MyClassLoader myLoader1 = new MyClassLoader();
                    File path = new File("C:\\Users\\razva\\Documents\\NetBeansProjects\\CustomButtons\\target\\CustomButtons-1.0-SNAPSHOT.jar");
                    if (path.exists()) {
                        URL url = null;
                        try {
                            url = path.toURI().toURL();
                        } catch (MalformedURLException ex) {
                            Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        myLoader1.addURL(url);
                    }

                    try {
                        // Initializam o componenta noua folosind clasa gasita si constructorul acesteia
                        Class clazz = myLoader1.loadClass("ro.uaic.info.misc.RoundButton");
                        System.out.println(clazz.getName());
                        Constructor con = clazz.getConstructor(String.class);
                        Component j = (Component) con.newInstance(name);
                        Method[] m = j.getClass().getMethods();
                        int X = frame.DP.getNextComponentX();
                        int Y = frame.DP.getNextComponentY();
                        // Invocam functia de mutare
                        for (Method m1 : m) {
                            if (m1.getName().equals("setBounds")) {
                                try {
                                    Class[] paramTypes = new Class[4];
                                    paramTypes[0] = int.class;
                                    paramTypes[1] = int.class;
                                    paramTypes[2] = int.class;
                                    paramTypes[3] = int.class;
                                    Method method = j.getClass().getMethod("setBounds", paramTypes);
                                    Integer[] params = new Integer[4];
                                    params[0] = X;
                                    params[1] = Y;
                                    params[2] = 80;
                                    params[3] = 80;
                                    method.invoke(j, (Object[]) params);
                                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                    Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                        // Adaugam in design panel
                        frame.DP.addElement(j, frame.DP.getNextID());
                        frame.DP.repaint();
                    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

    }

    private void reset(ActionEvent e) {
        // Stergem toate componentele din Design Panel
        Component[] components = frame.DP.getComponents();
        for (Component c : components) {
            frame.DP.remove(c);
        }
        frame.DP.reset();
        frame.DP.repaint();
    }

    private void load(ActionEvent e) {
        // Folosim XmlParser pentru a incarca componentele salvate
        XmlParser xmlParser = new XmlParser("components.xml", frame.DP);
        ArrayList<String> info = new ArrayList<>();
        xmlParser.load(info);
        Component[] components = frame.DP.getComponents();
        for (Component component : components) {
            frame.DP.remove(component);
        }
        frame.DP.reset();
        frame.DP.repaint();

        for (int i = 0; i < info.size(); i++) {
            // Extragem datele
            String classname = StringUtils.substringBetween(info.get(i), "'");
            String text = StringUtils.substringBetween(info.get(i), "-");
            String X = StringUtils.substringBetween(info.get(i), ":");
            String Y = StringUtils.substringBetween(info.get(i), ";");
            String width = StringUtils.substringBetween(info.get(i), "+");
            String height = StringUtils.substringBetween(info.get(i), "=");
            try {
                Class clazz = Class.forName(classname);
                Component c = (Component) clazz.newInstance();
                // Coordonatele componentei
                int cX = Integer.parseInt(X);
                int cY = Integer.parseInt(Y);
                int cW = Integer.parseInt(width);
                int cH = Integer.parseInt(height);
                int ID = frame.DP.getNextID();
                // Invocam functia de mutare
                Method[] m;
                m = c.getClass().getMethods();
                for (Method m1 : m) {
                    if (m1.getName().equals("setBounds")) {
                        try {
                            Class[] paramTypes = new Class[4];
                            paramTypes[0] = int.class;
                            paramTypes[1] = int.class;
                            paramTypes[2] = int.class;
                            paramTypes[3] = int.class;
                            Method method = c.getClass().getMethod("setBounds", paramTypes);
                            Integer[] params = new Integer[4];
                            params[0] = cX;
                            params[1] = cY;
                            params[2] = cW;
                            params[3] = cH;
                            method.invoke(c, (Object[]) params);
                        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                // Invocam functia de setare a textului
                m = c.getClass().getMethods();
                for (Method m1 : m) {
                    if (m1.getName().equals("setText")) {
                        try {
                            Method method = c.getClass().getMethod("setText", String.class);
                            method.invoke(c, text);
                        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                // Adaugam listener-ul ce ne permite sa vedem tabela
                c.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            // Aflam informatiile legate de componenta folosind BeanInfo
                            BeanInfo beanInfo = Introspector.getBeanInfo(c.getClass());
                            // Pregatim datele pentru tabel
                            ArrayList<String> columns = new ArrayList<>();
                            int n = beanInfo.getPropertyDescriptors().length;
                            ArrayList<Object>[] data = new ArrayList[n];
                            for (int i = 0; i < n; i++) {
                                data[i] = new ArrayList<>();
                            }
                            // Parcurgem lista de proprietati si alegem cele de tip int si string
                            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                                if (pd.getPropertyType() != null) {
                                    if (pd.getPropertyType().isAssignableFrom(String.class)) {
                                        columns.add(pd.getName());
                                        data[0].add(pd.getReadMethod().invoke(c));
                                    } else if (pd.getPropertyType().isAssignableFrom(int.class)) {
                                        columns.add(pd.getName());
                                        data[0].add(pd.getReadMethod().invoke(c));
                                    }
                                }
                            }
                            // Constrium tabela cu datele obtinute
                            String[] Columns = new String[columns.size()];
                            Columns = columns.toArray(Columns);
                            Object[][] Data = new Object[1][data[0].size()];
                            Data[0] = data[0].toArray(Data[0]);
                            propTable = new JTable(Data, Columns);
                            for (int i = 0; i < propTable.getColumnCount(); i++) {
                                propTable.getColumnModel().getColumn(i).setPreferredWidth(100);
                            }
                            // Adaugam listener-ul de modificare tabel
                            propTable.getModel().addTableModelListener((TableModelEvent evt) -> {
                                int NewX = Integer.parseInt((String) propTable.getValueAt(0, propTable.getColumn("x").getModelIndex()).toString());
                                int NewY = Integer.parseInt((String) propTable.getValueAt(0, propTable.getColumn("y").getModelIndex()).toString());
                                int NewWidth = Integer.parseInt((String) propTable.getValueAt(0, propTable.getColumn("width").getModelIndex()).toString());
                                int NewHeight = Integer.parseInt((String) propTable.getValueAt(0, propTable.getColumn("height").getModelIndex()).toString());
                                for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                                    if (pd.getPropertyType() != null) {
                                        if (pd.getPropertyType().isAssignableFrom(java.lang.String.class)) {
                                            if (pd.getWriteMethod() != null) {
                                                try {
                                                    pd.getWriteMethod().invoke(c, propTable.getValueAt(0, propTable.getColumn(pd.getName()).getModelIndex()));
                                                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                                    Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
                                                }
                                            }
                                        }
                                    }
                                }
                                // Nu exista functie de set X sau Y deci folosim o invocare a functiei setBounds
                                Method[] m = c.getClass().getMethods();
                                for (Method m1 : m) {
                                    if (m1.getName().equals("setBounds")) {
                                        try {
                                            Class[] paramTypes = new Class[4];
                                            paramTypes[0] = int.class;
                                            paramTypes[1] = int.class;
                                            paramTypes[2] = int.class;
                                            paramTypes[3] = int.class;
                                            Method method = c.getClass().getMethod("setBounds", paramTypes);
                                            Integer[] params = new Integer[4];
                                            params[0] = NewX;
                                            params[1] = NewY;
                                            params[2] = NewWidth;
                                            params[3] = NewHeight;
                                            method.invoke(c, (Object[]) params);
                                        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                            Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                }
                                // Facem tabela noua vizibila
                                scrollPane.setViewportView(propTable);
                                frame.DP.revalidate();
                            });
                            // Facem tabela noua vizibila
                            scrollPane.setViewportView(propTable);
                            frame.CP.revalidate();
                        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                });
                // Adaugam componenta in DesignPanel
                frame.DP.addElement(c, frame.DP.getNextID());
                frame.DP.repaint();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private void save(ActionEvent e) {
        // Folosim XmlParser pentru a salva componentele actuale
        XmlParser xmlParser = new XmlParser("components.xml", frame.DP);
        try {
            xmlParser.save();
        } catch (SAXException ex) {
            Logger.getLogger(ControlPanel.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void exit(ActionEvent e) {
        System.exit(0);
    }

}
