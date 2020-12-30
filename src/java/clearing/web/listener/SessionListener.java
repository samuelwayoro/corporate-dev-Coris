/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.web.listener;

import clearing.table.Utilisateurs;
import clearing.web.controller.SiteController;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author AUGOU Patrick
 */
public class SessionListener implements HttpSessionListener {
    private static int sessionCount = 0;
    
    public static int getSessionCount(){
    
        return sessionCount;
    }
    
    @Override
    public void sessionCreated(HttpSessionEvent event) {
        synchronized (this) {
            sessionCount++;
        }

        System.out.println("Session Created: " + event.getSession().getId());
        System.out.println("Total Sessions: " + sessionCount);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        synchronized (this) {
            sessionCount--;
        }
        logout(event.getSession());
        System.out.println("Session Destroyed: " + event.getSession().getId());
        System.out.println("Total Sessions: " + sessionCount);
    }

     public void logout(HttpSession session) {

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
        if (user != null) {
            user.setEtat(new BigDecimal(Utility.getParam("CETAUTIDEC")));
            try {
                db.executeUpdate("UPDATE UTILISATEURS SET ETAT=" + Utility.getParam("CETAUTIDEC") + " WHERE LOGIN='" + user.getLogin() + "'");
            } catch (SQLException ex) {
                Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        db.close();

    }
}
