package ro.uaic.info.lab12;

import java.awt.Component;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.SAXException;

public class XmlParser {

    private final String Name;
    private static JPanel Panel;

    public XmlParser(String n, JPanel p) {
        this.Panel = p;
        this.Name = n;
    }

    public void save() throws SAXException {
        try {
            // Pregatim datele pentru document
            Component[] components = Panel.getComponents();
            Document dom;
            Element e = null;
            Element e2 = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.newDocument();

            Element rootEle = dom.createElement("components");
            for (Component component : components) {
                // Pentru fiecare componenta aflam X,Y,Width,Height si Text
                String classname = component.getClass().getName();
                String Y = "";
                String X = "";
                String width = "";
                String height = "";
                String text = "";
                // Aflam daca clasa componentei are aceste proprietati
                Class beanClass = component.getClass();
                BeanInfo info = Introspector.getBeanInfo(beanClass);
                for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                    switch (pd.getName()) {
                        case "x":
                            X = "" + pd.getReadMethod().invoke(component).toString();
                            break;
                        case "y":
                            Y = "" + pd.getReadMethod().invoke(component).toString();
                            break;
                        case "width":
                            width = "" + pd.getReadMethod().invoke(component).toString();
                            break;
                        case "height":
                            height = "" + pd.getReadMethod().invoke(component).toString();
                            break;
                        case "text":
                            Method[] m = component.getClass().getMethods();
                            for (Method m1 : m) {
                                if (m1.getName().equals("getText")) {
                                    try {
                                        Method method = component.getClass().getMethod("getText", null);
                                        text = (String) method.invoke(component, null);
                                    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                        Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
                // Adaugaum nodul component
                e = dom.createElement("component");
                // Adaugam subnodurile 
                e2 = dom.createElement("classname");
                e2.appendChild(dom.createTextNode("" + classname));
                e.appendChild(e2);
                e2 = dom.createElement("text");
                e2.appendChild(dom.createTextNode("" + text));
                e.appendChild(e2);
                e2 = dom.createElement("x");
                e2.appendChild(dom.createTextNode("" + X));
                e.appendChild(e2);
                e2 = dom.createElement("y");
                e2.appendChild(dom.createTextNode("" + Y));
                e.appendChild(e2);
                e2 = dom.createElement("width");
                e2.appendChild(dom.createTextNode("" + width));
                e.appendChild(e2);
                e2 = dom.createElement("height");
                e2.appendChild(dom.createTextNode("" + height));
                e.appendChild(e2);
                // Adaugam componenta la lista de componente
                rootEle.appendChild(e);
            }

            dom.appendChild(rootEle);

            try {
                // Setam proprietatile XML
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                tr.transform(new DOMSource(dom),
                        new StreamResult(new FileOutputStream(this.Name)));

            } catch (TransformerException | IOException te) {
                System.out.println(te.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(XmlParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void load(ArrayList<String> info) {
        // Pregatim datele pentru document
        Document dom;
        File fXmlFile = new File(this.Name);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(fXmlFile);
            Element doc = dom.getDocumentElement();
            //Parcurgem lista de componente ("component" tag)
            NodeList nList = doc.getElementsByTagName("component");
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    // Extragem informatiile dorite dupa tag
                    Element eElement = (Element) nNode;
                    String element = "'" + eElement.getElementsByTagName("classname").item(0).getTextContent() + "'"
                            + "-" + eElement.getElementsByTagName("text").item(0).getTextContent() + "-"
                            + ":" + eElement.getElementsByTagName("x").item(0).getTextContent() + ":"
                            + ";" + eElement.getElementsByTagName("y").item(0).getTextContent() + ";"
                            + "+" + eElement.getElementsByTagName("width").item(0).getTextContent() + "+"
                            + "=" + eElement.getElementsByTagName("height").item(0).getTextContent() + "=";
                    info.add(element);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(XmlParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
