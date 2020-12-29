/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.patware.log.Loggingable;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;

/**
 *
 * @author Patrick
 */
public class Initiator extends Loggingable{
private String command = ResLoader.getMessages("Launch");

    public Initiator() {
    }
    public void execute(){
        try {
             logEvent("INFO", "Tentative d'execution de " +command);
             Utility.execute(command);
             logEvent("INFO", "Execution reussie de " +command);
        } catch (Exception ex) {
             logEvent("ERROR", "Erreur a l'execution de " +command);
            Logger.getLogger(Initiator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

}
