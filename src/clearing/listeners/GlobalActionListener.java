/*
 * ToolbarListener.java
 *
 * Created on 12 octobre 2006, 19:58
 */
package clearing.listeners;

import clearing.Main;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;


import org.patware.action.impl.ExecutableImpl;
import org.patware.log.Loggingable;
import org.patware.utils.ResLoader;

/**
 *
 * @author Administrateur
 */
public class GlobalActionListener implements ActionListener {

    /** Creates a new instance of ToolbarListener */
    public GlobalActionListener() {
    }

    public void actionPerformed(ActionEvent evt) {


        JComponent sourceBtn = ((JComponent) evt.getSource());


        sourceBtn.setEnabled(false);
        String actionName = sourceBtn.getName();
        makeAction(actionName);
        if (actionName.equals("aboutBtn")) {
            Main.tray.showTrayInfoBalloon(ResLoader.getMessages("MenuAbout"), ResLoader.getMessages("AppVersion"));
        }


        sourceBtn.setEnabled(true);
    }

    public static String makeAction(String actionName) {

        return makeAction(actionName, "");
    }
    
    public static String makeAction(String actionName, String description) {

        String message = "";
        System.out.println("Make Action = " + actionName + " Description = "+ description);

        ExecutableImpl anExecutable = null;
        Loggingable aLoggingable = null;
        Object instance;
        try {
            Class aClass = Class.forName(actionName);
            instance = aClass.newInstance();
            anExecutable = (ExecutableImpl) instance;
            aLoggingable = (Loggingable) instance;

        } catch (Exception e) {
            System.out.println("Impossible d'accéder Ã  la tache " + actionName);
            e.printStackTrace();
        }
        
        try {
            if(Main.isTrayed()){
                Main.tray.showTrayInfoBalloon(ResLoader.getMessages("AppTitle"), anExecutable.getDescription() + " en cours ...");
            }
            anExecutable.setDescription(description);
            anExecutable.execute();
            message = anExecutable.getDescription() + "  ";
            if(Main.isTrayed()){
                Main.tray.showTrayInfoBalloon(ResLoader.getMessages("AppTitle"), message);
            }
        } catch (Exception ex) {
            message = "Erreur lors de l'exécution de " + anExecutable.getDescription() + ". Veuillez verifier ";
            if(Main.isTrayed()){
                Main.tray.showTrayInfoBalloon(ResLoader.getMessages("AppTitle"), anExecutable.getDescription() + " en cours ...");
                Main.tray.showTrayInfoBalloon(ResLoader.getMessages("AppTitle"), message);
            }
            
            aLoggingable.logEvent("ERREUR", "Erreur lors de l'exécution de " + anExecutable.getDescription() + ". Veuillez verifier ");
            message += "\n" + ex.toString();
            Logger.getLogger(GlobalActionListener.class.getName()).log(Level.SEVERE, null, ex);
        }



        return message;
    }
     public static String makeAction(String actionName, HashMap parametersMap) {

        String message = "";
        System.out.println("Make Action = " + actionName + " Description = "+ parametersMap.get("description"));

        ExecutableImpl anExecutable = null;
        Loggingable aLoggingable = null;
        Object instance;
        try {
            Class aClass = Class.forName(actionName);
            instance = aClass.newInstance();
            anExecutable = (ExecutableImpl) instance;
            aLoggingable = (Loggingable) instance;

        } catch (Exception e) {
            System.out.println("Impossible d'accéder Ã  la tache " + actionName);
            e.printStackTrace();
        }

        try {
            if(Main.isTrayed()){
                Main.tray.showTrayInfoBalloon(ResLoader.getMessages("AppTitle"), anExecutable.getDescription() + " en cours ...");
            }
           
            anExecutable.setParametersMap(parametersMap);
            anExecutable.execute();
            message = anExecutable.getDescription() + "  ";
            if(Main.isTrayed()){
                Main.tray.showTrayInfoBalloon(ResLoader.getMessages("AppTitle"), message);
            }
        } catch (Exception ex) {
            message = "Erreur lors de l'exécution de " + anExecutable.getDescription() + ". Veuillez verifier ";
            if(Main.isTrayed()){
                Main.tray.showTrayInfoBalloon(ResLoader.getMessages("AppTitle"), anExecutable.getDescription() + " en cours ...");
                Main.tray.showTrayInfoBalloon(ResLoader.getMessages("AppTitle"), message);
            }

            aLoggingable.logEvent("ERREUR", "Erreur lors de l'exécution de " + anExecutable.getDescription() + ". Veuillez verifier ");
            message += "\n" + ex.toString();
            Logger.getLogger(GlobalActionListener.class.getName()).log(Level.SEVERE, null, ex);
        }


        anExecutable.getParametersMap().clear();
        return message;
    }
}
