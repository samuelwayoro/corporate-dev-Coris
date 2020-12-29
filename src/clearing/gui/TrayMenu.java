/*
 * TrayMenu.java
 *
 * Created on 12 novembre 2006, 21:15
 */

package clearing.gui;


import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.patware.listeners.GlobalActionListener;
import org.patware.utils.ResLoader;
/**
 *
 * @author Administrateur
 */
public class TrayMenu extends JPopupMenu{
  
    private JMenuItem showMenuItem = null;
    private JMenuItem connMenuItem = null;
    private GlobalActionListener globalActionListener = new GlobalActionListener();   
    
    /** Creates a new instance of TrayMenu */
    public TrayMenu() {
        super(ResLoader.getMessages("TitreApp"));
        createMenu();
    }
    
    public JPopupMenu createMenu(){
        
        JMenu submenu;
        JMenuItem menuItem;
        JRadioButtonMenuItem rbMenuItem;
        JCheckBoxMenuItem cbMenuItem;
     
        
       /* try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        
        if( Integer.parseInt(System.getProperty("java.version").substring(2,3)) >=5 )
            System.setProperty("javax.swing.adjustPopupLocationToFit", "false");
       
        
        // a group of JMenuItems
       
        ImageIcon icon = ResLoader.getIcon("/clearing/images/T5.gif");
        
        menuItem = new JMenuItem(ResLoader.getMessages("TitreApp"),icon);
        this.add(menuItem);
       // "About" this item
        this.addSeparator();
        menuItem = new JMenuItem(ResLoader.getMessages("MenuAbout"));
        menuItem.setName("aboutBtn");
        menuItem.addActionListener(globalActionListener);
        this.add(menuItem);
        // "MENU NAT" this item
        this.addSeparator();
        submenu = new JMenu("NATIONAL");
        this.add(submenu);
       
       // "INITR NAT" this item
        this.addSeparator();
        menuItem = new JMenuItem("INITR NATIONAL");
        menuItem.setName("initrNatBtn");
        menuItem.addActionListener(globalActionListener);
        submenu.add(menuItem);
       
        // "ICOM1 NAT" submenu item
        submenu.addSeparator();
        menuItem = new JMenuItem("ICOM1 NATIONAL");
        menuItem.setName("icom1NatBtn");
        menuItem.addActionListener(globalActionListener);
        submenu.add(menuItem);
       
        // "ICOM2 NAT" submenu item
        submenu.addSeparator();
        menuItem = new JMenuItem("ICOM2 NATIONAL");
        menuItem.setName("icom2NatBtn");
        menuItem.addActionListener(globalActionListener);
        submenu.add(menuItem);
       
        // "ICOM3 NAT" submenu item
        submenu.addSeparator();
        menuItem = new JMenuItem("ICOM3 NATIONAL");
        menuItem.setName("icom3NatBtn");
        menuItem.addActionListener(globalActionListener);
        submenu.add(menuItem);
       
        // "MENU SRG" submenu item
        this.addSeparator();
        submenu = new JMenu("SOUS REGIONAL");
        this.add(submenu);
        // "INITR NAT" this item
        this.addSeparator();
        menuItem = new JMenuItem("INITR REGIONAL");
        menuItem.setName("initrSrgBtn");
        menuItem.addActionListener(globalActionListener);
        submenu.add(menuItem);
       
        // "ICOM1 NAT" submenu item
        submenu.addSeparator();
        menuItem = new JMenuItem("ICOM1 REGIONAL");
        menuItem.setName("icom1SrgBtn");
        menuItem.addActionListener(globalActionListener);
        submenu.add(menuItem);
       
        // "ICOM2 NAT" submenu item
        submenu.addSeparator();
        menuItem = new JMenuItem("ICOM2 REGIONAL");
        menuItem.setName("icom2SrgBtn");
        menuItem.addActionListener(globalActionListener);
        submenu.add(menuItem);
       
        // "ICOM3 NAT" submenu item
        submenu.addSeparator();
        menuItem = new JMenuItem("ICOM3 REGIONAL");
        menuItem.setName("icom3SrgBtn");
        menuItem.addActionListener(globalActionListener);
        submenu.add(menuItem);
       
        // "Quit" this item
        this.addSeparator();
        menuItem = new JMenuItem(ResLoader.getMessages("MenuQuitter"));
        menuItem.setName("quitBtn");
        menuItem.addActionListener(globalActionListener);
        this.add(menuItem);
        return this;
        
}
    public JMenuItem getShowMenuItem() {
        return showMenuItem;
    }

    public void setShowMenuItem(JMenuItem showMenuItem) {
        this.showMenuItem = showMenuItem;
    }

    public JMenuItem getConnMenuItem() {
        return connMenuItem;
    }

    public void setConnMenuItem(JMenuItem connMenuItem) {
        this.connMenuItem = connMenuItem;
    }

    public static TrayMenu getInstance(){
        
        return trayMenu;
    }
    
    private static TrayMenu trayMenu = new TrayMenu();
    
}
