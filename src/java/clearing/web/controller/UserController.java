/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.web.controller;

import clearing.Main;
import clearing.table.Motpashis;
import clearing.table.Utilisateurs;
import clearing.utils.StaticValues;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.patware.bean.table.Params;
import org.patware.jdbc.DataBase;
import org.patware.log.Auditable;
import org.patware.log.Loggingable;
import org.patware.utils.RegValidator;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.web.bean.LogonFormBean;
import org.patware.web.bean.MessageBean;
import org.patware.web.jmaki.MenuModel;
import org.patware.web.jmaki.TableModel;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author AUGOU Patrick
 */
public class UserController extends Loggingable {

    private Utilisateurs utilisateur;
    private MessageBean messageBean;
    private LogonFormBean logonForm;
    private MenuModel menuModel;
    private TableModel tableModel;

    public UserController() {
        utilisateur = new Utilisateurs();
        messageBean = new MessageBean();
    }

    public Utilisateurs getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateurs utilisateur) {
        this.utilisateur = utilisateur;
    }

    public LogonFormBean getLogonForm() {
        return logonForm;
    }

    public void setLogonForm(LogonFormBean logonForm) {
        this.logonForm = logonForm;
    }

    public MenuModel getMenuModel() {
        if (utilisateur != null) {
            menuModel = new MenuModel(getUtilisateur().getPoids());
        }
        return menuModel;
    }

    public TableModel getTableModel() {
        if (utilisateur != null) {
            tableModel = new TableModel();
        }
        return tableModel;
    }

    public void setMenuModel(MenuModel menuModel) {
        this.menuModel = menuModel;
    }

    public String logon(HttpServletRequest request) {
        String login = logonForm.getLogin();
        String password = logonForm.getPassword();
        String sql = "";
        Calendar myCal = Calendar.getInstance();
        Calendar myCal2 = Calendar.getInstance();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            //System.out.println(JDBCXmlReader.getUrl() + "|" + JDBCXmlReader.getUser() + "|" + JDBCXmlReader.getPassword());
            return printMessage("Erreur Base de donnees " + e.getMessage());
//            e.printStackTrace();
        }
        if (Main.isServerDeclared() && Utility.applicationIsValid(StaticValues.AFTER_SPY, StaticValues.BEFORE_SPY, StaticValues.DATE_SPY)) {

            if (!login.trim().equals("") && !password.trim().equals("")) {
                sql = "SELECT * FROM UTILISATEURS WHERE LOGIN = '" + login + "'";
                Utilisateurs unUtilisateur[] = (Utilisateurs[]) db.retrieveRowAsObject(sql, new Utilisateurs());

                if ((unUtilisateur != null) && (unUtilisateur.length > 0)) {
                    //Cet utilisateur existe
                    setUtilisateur(unUtilisateur[0]);

                    if ((Utility.getParam("SINGLE_CONNECT") != null && Utility.getParam("SINGLE_CONNECT").equalsIgnoreCase("1"))
                            && getUtilisateur().getEtat().equals(new BigDecimal(Utility.getParam("CETAUTIACT")))) {
                        db.close();
                        return printMessage("Vous etes connecte sur une autre machine. Merci de vous deconnecter de cette machine ou Contactez l'Administrateur SVP. ");
                    }

                    if (getUtilisateur().getEtat().equals(new BigDecimal(Utility.getParam("CETAUTIDES")))) {
                        db.close();
                        return printMessage("Vous avez ete desactive. Contactez l'Administrateur SVP. ");
                    }

                    if (getUtilisateur().getEtat().equals(new BigDecimal(Utility.getParam("CETAUTICRE")))) {
                        db.close();
                        return printMessage("Votre login n'a pas encore ete valide. Contactez l'Administrateur SVP. ");
                    }
                    //sql += " AND PASSWORD = '" + new RegValidator().encoder(password).replaceAll("\\p{Punct}", "_") + "'";
                    sql = "select * from Utilisateurs where login = '" + login
                            + "' and password = '" + new RegValidator().encoder(password).replaceAll("\\p{Punct}", "_") + "'";

                    unUtilisateur = (Utilisateurs[]) db.retrieveRowAsObject(sql, unUtilisateur[0]);
                    if ((unUtilisateur != null) && (unUtilisateur.length > 0)) {
                        //Bon mot de passe
                        sql = "UPDATE PARAMS SET VALEUR = 0  WHERE NOM='" + login + "' AND TYPE='CODE_RETRY'";
                        try {
                            db.executeUpdate(sql);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                      
                        if (getUtilisateur().getDate_inscrit() != null) {
                            System.out.println("Here 2");
                            myCal.setTimeInMillis((getUtilisateur().getDate_inscrit().getTime()));
                            myCal.add(Calendar.DAY_OF_MONTH, Integer.parseInt(Utility.getParam("MAXDAYTOCHANGEPWD")));
                            if (myCal.getTime().before(new Date(System.currentTimeMillis()))) {
                                getUtilisateur().setEtat(new BigDecimal(Utility.getParam("CETAUTIPASMOD")));
                            }
                        } else {
                            getUtilisateur().setEtat(new BigDecimal(Utility.getParam("CETAUTIPASMOD")));
                        }
                      
                        myCal.setTime(new Date(System.currentTimeMillis()));
                        myCal2.setTime(Utility.convertStringToDate(StaticValues.DATE_SPY, "yyyy-MM-dd"));
                        long day = Utility.getDaysBetween(myCal, myCal2);
                        if (day <= 15) {
                            printMessage("ATTENTION, IL RESTE " + day + " JOURS AVANT L'EXPIRATION DE VOTRE LICENCE TEMPORAIRE.\n"
                                    + "CONTACTER SBS SA AFIN D'OBTENIR UNE LICENCE DEFINITIVE. MERCI.");
                        }
                
                        return unUtilisateur[0].getNom();
                    } else {

                        Params param = new Params();
                        param.setLibelle("Nbr de tentative de Connection");
                        param.setType("CODE_RETRY");
                        param.setNom(getUtilisateur().getLogin());
                        param.setValeur("1");
                        Utility.clearParamsCache();
                        String params = Utility.getParamOfType(param.getNom(), "CODE_RETRY");

                        if (params != null) {
                            if (Integer.parseInt(params.trim()) >= Integer.parseInt(Utility.getParam("MAXLOGINRETRY"))) {
                                getUtilisateur().setEtat(new BigDecimal(Utility.getParam("CETAUTIDES")));
                                db.updateRowByObjectByQuery(getUtilisateur(), "UTILISATEURS", "LOGIN = '" + login + "'");
                               
                                Auditable.logAudit(login, "Utilisateur desactive", "logon", request.getRemoteHost());
                                
                                
                              //  logEvent("WARNING", "Utilisateur :" + login + " desactive");
                                return printMessage("Desole, Plus de (3) trois tentatives. Vous etes desactive.\n Contacter un Administrateur ");
                            }
                            sql = "UPDATE PARAMS SET VALEUR = VALEUR + " + param.getValeur() + " WHERE NOM='" + param.getNom() + "' AND TYPE='CODE_RETRY'";
                            try {

                                db.executeUpdate(sql);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            db.insertObjectAsRowByQuery(param, "PARAMS");
                        }
                      Auditable.logAudit(login, "Mot de passe incorrect. Erreur de connexion", "logon", request.getRemoteHost());
//                        logEvent("WARNING", "Mot de passe " + login + " incorrect. Erreur de connexion");
                        db.close();

                        return printMessage("Desole, Mot de passe incorrect, Reessayez SVP. " + Main.getMessage());
                    }

                } else {
                      Auditable.logAudit(login, "L'utilisateur n'existe pas", "logon", request.getRemoteHost());
                    
//                    logEvent("WARNING", "L'utilisateur " + login + " n'existe pas");
                    db.close();

                    return printMessage("L'utilisateur " + login + " n'existe pas. " + Main.getMessage());
                }

            }
        }
        db.close();
        return printMessage("Vous n'etes pas autorise a  vous connecter. " + Main.getMessage());
    }

    void logout(HttpServletRequest request) {

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
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

    public void activateUser() {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            db.executeUpdate("UPDATE UTILISATEURS SET ETAT=" + Utility.getParam("CETAUTIACT") + " WHERE LOGIN='" + getUtilisateur().getLogin().trim() + "'");
        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
    }

    public void disconnectUser() {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            db.executeUpdate("UPDATE UTILISATEURS SET ETAT=" + Utility.getParam("CETAUTIDEC") + " WHERE LOGIN='" + getUtilisateur().getLogin().trim() + "'");
        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
    }

    String changePassword(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");

            String currentPasswordTyped = new RegValidator().encoder(request.getParameter("oldpassword")).replaceAll("\\p{Punct}", "_");

            //user.setPassword(currentPasswordTyped);
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            String sql = "SELECT * FROM UTILISATEURS WHERE LOGIN='" + user.getLogin().trim() + "' AND PASSWORD='" + currentPasswordTyped + "'";
            //db.updateRowByObjectByQuery(user, "UTILISATEURS", where);
            Utilisateurs[] userFound = (Utilisateurs[]) db.retrieveRowAsObject(sql, user);

            if (userFound != null && userFound.length > 0) {
                String result = checkPassword(request.getParameter("password1"));

                if (result == null) {
                    db.close();
                    return result;
                }

                user.setPassword(new RegValidator().encoder(request.getParameter("password1")).replaceAll("\\p{Punct}", "_"));
                if (user.getPassword().equalsIgnoreCase(currentPasswordTyped)) {
                    db.close();
                    return printMessage("Votre nouveau mot de passe doit etre different de l'ancien!!");
                }

                sql = "SELECT * FROM (SELECT * FROM (SELECT * FROM MOTPASHIS WHERE " + ResLoader.getMessages("trimFunction") + "(LOGIN)='" + user.getLogin().trim() + "' ORDER BY IDMOTPASHIS DESC) WHERE ROWNUM <=" + Utility.getParam("NBRMOTPASHIS") + ") WHERE PASSWORD='" + user.getPassword() + "'";
                Motpashis[] motpashises = (Motpashis[]) db.retrieveRowAsObject(sql, new Motpashis());
                if (motpashises != null && motpashises.length > 0) {
                    db.close();
                    return printMessage("Votre nouveau mot de passe doit etre different de vos " + Utility.getParam("NBRMOTPASHIS") + " derniers mots de passe!!!");
                }

                user.setDate_inscrit(new Timestamp(System.currentTimeMillis()));
                user.setEtat(new BigDecimal(Utility.getParam("CETAUTIDEC")));

                String where = "LOGIN='" + user.getLogin().trim() + "' AND PASSWORD='" + currentPasswordTyped + "'";

                boolean success = db.updateRowByObjectByQuery(user, "UTILISATEURS", where);
                Motpashis motpashis = new Motpashis();
                motpashis.setIdMotpashis(new BigDecimal(Utility.computeCompteur("IDMOTPASHIS", "MOTPASHIS")));
                motpashis.setLogin(user.getLogin());
                motpashis.setDatecreation(new Timestamp(System.currentTimeMillis()));
                motpashis.setPassword(user.getPassword());
                db.insertObjectAsRowByQuery(motpashis, "MOTPASHIS");
                db.close();
                if (success) {
                    if (userFound[0].getEtat().equals(new BigDecimal(Utility.getParam("CETAUTIPASMOD")))) {
                        setUtilisateur(userFound[0]);
                        disconnectUser();
                    }
                    return printMessage("Mot de passe modifie avec succes");
                }

            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("Votre ancien mot de passe n'est pas correct");

    }

    String changePasswordMSSQL(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");

            String currentPasswordTyped = new RegValidator().encoder(request.getParameter("oldpassword")).replaceAll("\\p{Punct}", "_");

            //user.setPassword(currentPasswordTyped);
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            String sql = "SELECT * FROM UTILISATEURS WHERE LOGIN='" + user.getLogin().trim() + "' AND PASSWORD='" + currentPasswordTyped + "'";
            //db.updateRowByObjectByQuery(user, "UTILISATEURS", where);
            Utilisateurs[] userFound = (Utilisateurs[]) db.retrieveRowAsObject(sql, user);

            if (userFound != null && userFound.length > 0) {
                String result = checkPassword(request.getParameter("password1"));

                if (result == null) {
                    db.close();
                    return result;
                }

                user.setPassword(new RegValidator().encoder(request.getParameter("password1")).replaceAll("\\p{Punct}", "_"));
                if (user.getPassword().equalsIgnoreCase(currentPasswordTyped)) {
                    db.close();
                    return printMessage("Votre nouveau mot de passe doit etre different de l'ancien!!");
                }

                sql = "SELECT * FROM (SELECT TOP " + Utility.getParam("NBRMOTPASHIS") + " * FROM MOTPASHIS WHERE " + ResLoader.getMessages("trimFunction") + "(LOGIN)='" + user.getLogin().trim() + "' ORDER BY IDMOTPASHIS DESC)MOTPASHIS WHERE PASSWORD='" + user.getPassword() + "'";
                Motpashis[] motpashises = (Motpashis[]) db.retrieveRowAsObject(sql, new Motpashis());
                if (motpashises != null && motpashises.length > 0) {
                    db.close();
                    return printMessage("Votre nouveau mot de passe doit etre different de vos " + Utility.getParam("NBRMOTPASHIS") + " derniers mots de passe!!!");
                }

                user.setDate_inscrit(new Timestamp(System.currentTimeMillis()));

                String where = "LOGIN='" + user.getLogin().trim() + "' AND PASSWORD='" + currentPasswordTyped + "'";

                boolean success = db.updateRowByObjectByQuery(user, "UTILISATEURS", where);
                Motpashis motpashis = new Motpashis();
                motpashis.setIdMotpashis(new BigDecimal(Utility.computeCompteur("IDMOTPASHIS", "MOTPASHIS")));
                motpashis.setLogin(user.getLogin());
                motpashis.setDatecreation(new Timestamp(System.currentTimeMillis()));
                motpashis.setPassword(user.getPassword());
                db.insertObjectAsRowByQuery(motpashis, "MOTPASHIS");
                db.close();
                if (success) {
                    if (userFound[0].getEtat().equals(new BigDecimal(Utility.getParam("CETAUTIPASMOD")))) {
                        setUtilisateur(userFound[0]);
                        disconnectUser();
                    }
                    return printMessage("Mot de passe modifie avec succes");
                }

            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("Votre ancien mot de passe n'est pas correct");

    }

    public String checkPassword(String password) {

        if (password.length() < Integer.parseInt(Utility.getParam("PWDMINLENGTH"))) {
            return printMessage("Le mot de passe doit contenir au moins " + Utility.getParam("PWDMINLENGTH").trim() + " lettres SVP");
        }

        if (password.matches("\\p{Punct}+|\\p{Alpha}+|\\p{Digit}+")) {
            return printMessage("Le mot de passe doit contenir des lettres, des chiffres et des ponctuactions SVP");
        }
        return "OK";

    }

    public void audit(HttpServletRequest request) {

        if (ResLoader.getMessages("AuditLog").equalsIgnoreCase("true")) {
            HttpSession sess = request.getSession();
            if (sess != null) {
                Utilisateurs user = (Utilisateurs) sess.getAttribute("utilisateur");
                if (user != null) {

                    Auditable.logAudit(user.getLogin().trim(), Utility.getHttpRequestAsString(request), request.getParameter("action"), request.getRemoteHost());

                }
            }

        }

    }

    public MessageBean getMessageBean() {
        return messageBean;
    }

    public String printMessage(String message) {
        //System.out.println(message);

        messageBean.setMessage(message);
        return null;
    }
}
