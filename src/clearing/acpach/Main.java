/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.acpach;

import clearing.utils.StaticValues;
import java.util.EnumSet;
import org.patware.app.PatWareApp;

/**
 * La classe <code>Main</code> est la classe Principale du Moniteur Clearing Son
 * héritage de la classe <code>PatwareApp</code> lui profère tous les
 * comportements d'une Application Patware.
 *
 * @author Patrick
 */
public class Main extends PatWareApp {

    /**
     * Constructeur de la classe Main Ce constructeur ne fait qu'un super() pour
     * instancier la PatwareApp
     */
    public Main() {
        super(true, false, true, false, StaticValues.AFTER_SPY, StaticValues.BEFORE_SPY, StaticValues.DATE_SPY, "clearing/ressources/MessagesBundle", "/clearing/ressources/clearing.xml");

    }

    public static void main(String[] args) {
        Main main = new Main();
    }

}
