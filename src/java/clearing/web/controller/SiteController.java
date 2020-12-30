/*
 * SiteController.java
 *
 * Created on 15 fevrier 2006, 01:41
 */
package clearing.web.controller;

import clearing.Main;
import clearing.listeners.GlobalActionListener;
import clearing.model.CMPUtility;
import clearing.table.Agences;
import clearing.table.Banques;
import clearing.table.Cheques;
import clearing.table.Cheques_his;
import clearing.table.Comptes;
import clearing.table.ComptesNsia;
import clearing.table.Effets;
import clearing.table.Etablissements;

import clearing.table.MachineScan;
import clearing.table.Prelevements;
import clearing.table.Profils;
import clearing.table.Remises;
import clearing.table.RemisesEffets;
import clearing.table.Sequences;
import clearing.table.Utilisateurs;
import clearing.table.Vignettes;
import clearing.table.Virements;
import clearing.table.ibus.IBUS_CHQ_SOUM;
import clearing.web.bean.ComboAgenceBean;
import clearing.web.bean.ComboBanqueBean;
import clearing.web.bean.ComboCompteBDKBean;
import clearing.web.bean.ComboCompteBean;
import clearing.web.bean.ComboCompteCBAOBean;
import clearing.web.bean.ComboCompteCNCEBean;
import clearing.web.bean.ComboCompteNSIABean;
import clearing.web.bean.ComboComptePFBean;
import clearing.web.bean.ComboCompteTPCIBean;
import clearing.web.bean.ComboIdBean;
import clearing.web.bean.ComboTableBean;
import java.io.UnsupportedEncodingException;
import org.json.JSONException;
import org.patware.utils.RegValidator;
import clearing.web.bean.RejetFormBean;
import java.io.File;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.patware.action.RobotTask;
import org.patware.bean.BeanInfoFactory;
import org.patware.bean.Property;
import org.patware.bean.table.Repertoires;
import org.patware.bean.table.Macuti;
import org.patware.bean.table.Menu;
import org.patware.bean.table.Params;
import org.patware.bean.table.Tache;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.utils.VersionFileReader;
import org.patware.web.bean.MessageBean;
import org.patware.web.bean.RapportBean;
import org.patware.web.jmaki.TreeviewModel;
import org.patware.web.json.JSONConverter;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Administrateur
 */
public class SiteController {

    //private DataBase db;
    private static Main webMonitor;
    private RapportBean rapportBean;
    private VersionFileReader wvfr;
    private ServletConfig servletConfig;
    private JSONConverter jsonConverter = new JSONConverter();
    private MessageBean messageBean;

    /**
     * Creates a new instance of SiteController
     */
    public SiteController(ServletConfig servletConfig) {
        setServletConfig(servletConfig);
        messageBean = new MessageBean();
        rapportBean = new RapportBean(getServletConfig());

        ResLoader.init(Main.class, "clearing/ressources/MessagesBundle");
    }

    public String inscrire(Utilisateurs nouvelUtilisateur) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String sql = "select * from Utilisateurs where login = '" + nouvelUtilisateur.getLogin() + "'";
        Utilisateurs unUtilisateur[] = (Utilisateurs[]) db.retrieveRowAsObject(sql, new Utilisateurs());
        if ((unUtilisateur == null) || (unUtilisateur.length == 0)) {

            String result = checkPassword(nouvelUtilisateur.getPassword());
            if (result == null) {
                return result;
            }

            //return "notUtilisateur";
            nouvelUtilisateur.setDate_inscrit(new java.sql.Timestamp(System.currentTimeMillis()));
            nouvelUtilisateur.setPassword(new RegValidator().encoder(nouvelUtilisateur.getPassword()).replaceAll("\\p{Punct}", "_"));
            nouvelUtilisateur.setEtat(new BigDecimal(Utility.getParam("CETAUTICRE")));

            if (db.insertObjectAsRowByQuery(nouvelUtilisateur, "UTILISATEURS")) {
                db.close();
                return printMessage(nouvelUtilisateur.getPrenom() + " " + nouvelUtilisateur.getNom() + "(" + nouvelUtilisateur.getLogin() + ") inscrit avec succes!");

            } else {
                db.close();
                return printMessage("Erreur d'insertion dans la BD!\n" + db.getMessage());
            }

        } else {
            //Cet utilisateur existe
            db.close();
            return printMessage("Login deja utilise");
        }

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

    public String printMessage(String message) {
        //System.out.println(message);

        messageBean.setMessage(message);
        return null;
    }

    public String rejeteObjet(RejetFormBean rejetFormBean) {

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!rejetFormBean.getIdObjet().trim().equals("")) {
            try {
                String sql = "UPDATE " + rejetFormBean.getObjet() + " SET ETAT = " + Utility.getParam("CETAOPEALLICOM2") + ","
                        + " MOTIFREJET ='" + rejetFormBean.getMotifRejet() + "' WHERE " + rejetFormBean.getNomIdObjet() + " = " + rejetFormBean.getIdObjet() + ""
                        + " AND ETAT IN (170,172,180,182," + Utility.getParam("CETAOPERETRECENVSIBVER") + ")"; //rejetForm
                if (db.executeUpdate(sql) == 0) {
                    db.close();
                    return printMessage(rejetFormBean.getObjet() + " " + rejetFormBean.getIdObjet() + " ne peut être rejete ");
                }

            } catch (SQLException ex) {
                Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        db.close();
        return printMessage(rejetFormBean.getObjet() + " " + rejetFormBean.getIdObjet() + " prêt pour ICOM2 avec code rejet " + rejetFormBean.getMotifRejet());
    }

    public String rejeteObjet(HttpServletRequest request, HttpServletResponse response) {

        String objet = request.getParameter("objet");
        String nomIdObjet = request.getParameter("nomidobjet");
        String idObjet = request.getParameter("idobjet");
        String motifRejet = request.getParameter("motifrejet");
        String etatDebut = request.getParameter("etatdebut");
        String etatFin = request.getParameter("etatfin");

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (!objet.trim().equals("")) {
            try {
                String sql;
                if (Utility.getParam("REJCHQINT") != null && Utility.getParam("REJCHQINT").equals("1") && objet.trim().toUpperCase().equals("CHEQUES")) { //Rejet Cheque interne autorisé
                    sql = " SELECT * from CHEQUES WHERE IDCHEQUE=" + idObjet;
                    Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                    if (cheques != null && cheques.length > 0 && (cheques[0].getBanque().trim().equals(cheques[0].getBanqueremettant().trim()))) {
                        //Cheque Non compensable
                        etatFin = "950";

                    }

                }

                sql = "UPDATE " + objet + " SET ETAT = " + etatFin + ", MOTIFREJET ='" + motifRejet + "' WHERE " + nomIdObjet + " = " + idObjet + " AND ETAT IN " + etatDebut;

                if (db.executeUpdate(sql) == 0) {
                    db.close();
                    return printMessage(objet + " " + idObjet + " ne peut être rejete ");
                }

            } catch (SQLException ex) {
                Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        db.close();
        return printMessage(objet + " " + idObjet + " rejete avec code rejet " + motifRejet);
    }

    public String rejeterSignCheque(HttpServletRequest request, HttpServletResponse response) {

        String objet = "CHEQUES";
        String nomIdObjet = "IDCHEQUE";
        String idObjet = request.getParameter("idObjet");
        String motifRejet = request.getParameter("motifrejet");
        String etatDebut = request.getParameter("etatdebut");
        String etatFin = request.getParameter("etatfin");

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (!objet.trim().equals("")) {
            try {
                String sql = "UPDATE " + objet + " SET ETAT = " + etatFin + ", MOTIFREJET ='" + motifRejet + "' WHERE " + nomIdObjet + " = " + idObjet + " AND ETAT IN " + etatDebut;
                if (db.executeUpdate(sql) == 0) {
                    db.close();
                    return printMessage(objet + " " + idObjet + " ne peut être rejete ");
                }

            } catch (SQLException ex) {
                Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        db.close();
        return printMessage(objet + " " + idObjet + " rejete avec code rejet " + motifRejet);
    }

    public String validerSignCheque(HttpServletRequest request, HttpServletResponse response) {

        String objet = "CHEQUES";
        String nomIdObjet = "IDCHEQUE";
        String idObjet = request.getParameter("idObjet");
        String etatDebut = request.getParameter("etatdebut");

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (!objet.trim().equals("")) {
            try {
                String sql = "UPDATE " + objet + " SET ETAT=" + Utility.getParam("CETAOPERETRECENVSIBVER") + ",LOTSIB = 3 WHERE " + nomIdObjet + " = " + idObjet + " AND ETAT IN " + etatDebut;
                if (db.executeUpdate(sql) == 0) {
                    db.close();
                    return printMessage(objet + " " + idObjet + " ne peut être valide ");
                }

            } catch (SQLException ex) {
                Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        db.close();
        return printMessage("OK");
    }

    public String validerImageCheque(HttpServletRequest request, HttpServletResponse response) {

        String objet = "CHEQUES";
        String nomIdObjet = "IDCHEQUE";
        String idObjet = request.getParameter("idObjet");
        String etatDebut = request.getParameter("etatdebut");

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (!objet.trim().equals("")) {
            try {
                String sql = "UPDATE " + objet + " SET LOTSIB = 3 WHERE " + nomIdObjet + " = " + idObjet + " AND ETAT IN " + etatDebut;
                if (db.executeUpdate(sql) == 0) {
                    db.close();
                    return printMessage(objet + " " + idObjet + " ne peut être valide ");
                }

            } catch (SQLException ex) {
                Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        db.close();
        return printMessage("OK");
    }

    public String passerSignCheque(HttpServletRequest request, HttpServletResponse response) {

        String objet = "CHEQUES";
        String nomIdObjet = "IDCHEQUE";
        String idObjet = request.getParameter("idObjet");
        String etatDebut = request.getParameter("etatdebut");

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (!objet.trim().equals("")) {
            try {
                String sql = "UPDATE " + objet + " SET CODEUTILISATEUR=NULL,LOTSIB = LOTSIB+1 WHERE LOTSIB<5 AND " + nomIdObjet + " = " + idObjet + " AND ETAT IN " + etatDebut;
                if (db.executeUpdate(sql) == 0) {
                    db.close();
                    return printMessage(objet + " " + idObjet + " ne peut être passe ");
                }

            } catch (SQLException ex) {
                Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        db.close();
        return printMessage("OK");
    }

    public String startWebMonitor() {

        if (webMonitor == null) {
            webMonitor = new Main();

            return printMessage(Main.getMessage());
        }
        return printMessage("WebMonitor deja demarre ...");
    }

    public String stopWebMonitor() {

        if (webMonitor != null) {

            webMonitor.logEvent("INFO", "Arret de WebMonitor ...");
            RobotTask[] robotTasks = Main.getRobotTasks();
            if (robotTasks != null) {
                for (int i = 0; i < robotTasks.length; i++) {
                    RobotTask robotTask = robotTasks[i];
                    robotTask.setExecution(false);

                }
                RobotTask.stopAllRobotTask();
                if (Main.isTrayed()) {
                    Main.tray.remove();
                }

            }

            webMonitor = null;
            System.gc();
            return printMessage("WebMonitor arrete avec succes...");
        }
        return printMessage("WebMonitor deja arrete ...");
    }

    public String makeAction(String action, String description) {
        if (webMonitor != null) {

            return printMessage(GlobalActionListener.makeAction(action, description));

        } else {
            return printMessage("WebMonitor n'est pas demarre...");
        }
    }

    public String makeAction(String action, HashMap parametersMap, HttpServletRequest request) {
        if (webMonitor != null) {
            HashMap hashMap = new HashMap(parametersMap);
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            hashMap.put("user", user);
            return printMessage(GlobalActionListener.makeAction(action, hashMap));

        } else {
            return printMessage("WebMonitor n'est pas demarre...");
        }
    }

    public String createMenu(String tacheParent, String tacheEnfant) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Menu aMenu = new Menu(tacheParent, tacheEnfant);

        if (db.insertObjectAsRowByQuery(aMenu, "MENU")) {
            db.close();
            return printMessage("Menu cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD!");
        }

    }

    public String createTache(String idtache, String libelle, String typetache, BigDecimal poids, String url) throws UnsupportedEncodingException {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Tache aTache = new Tache(idtache, libelle, typetache, poids, URLDecoder.decode(url, "UTF-8"));

        if (db.insertObjectAsRowByQuery(aTache, "TACHE")) {
            db.close();
            return printMessage("Tache cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD!");
        }

    }

    public void showRapport(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            Map<String, Object> fillParams = new HashMap<String, Object>();

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            fillParams.put("LogoURL", getServletConfig().getServletContext().getResource("/images/bank_logo_1.png"));
            fillParams.put("DateImpression", Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            fillParams.put("IntervalDate", request.getParameter("interval"));
            fillParams.put("PrintDetail", request.getParameter("avecDetail"));

            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");

            fillParams.put("Auteur", user.getLogin().trim());
            rapportBean.jasperReport(request.getParameter("nomrapport"), request.getParameter("typerapport"), db.execute(request.getParameter("requete")), fillParams, request, response);
            fillParams.clear();
            db.close();

            // writeResponse(request, response);
        } catch (Exception ex) {
            db.close();
            Logger.getLogger(ControlServlet.class.getName()).log(Level.SEVERE, null, ex);

        }

    }

    public void showRapportChequesLite(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            Map<String, Object> fillParams = new HashMap<String, Object>();

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            fillParams.put("LogoURL", getServletConfig().getServletContext().getResource("/images/bank_logo_1.png"));
            fillParams.put("DateImpression", Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            fillParams.put("IntervalDate", request.getParameter("interval"));
            fillParams.put("PrintDetail", request.getParameter("avecDetail"));

            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            String requete = request.getParameter("requete");
            String dateSaisie = request.getParameter("dateChoisie");
            requete = requete.replaceAll("dateSaisie", dateSaisie);

            fillParams.put("Auteur", user.getLogin().trim());
            rapportBean.jasperReport(request.getParameter("nomrapport"), request.getParameter("typerapport"), db.execute(requete), fillParams, request, response);
            fillParams.clear();
            db.close();

            // writeResponse(request, response);
        } catch (Exception ex) {
            db.close();
            Logger.getLogger(ControlServlet.class.getName()).log(Level.SEVERE, null, ex);

        }

    }

    public void showRapportWebLite(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            Map<String, Object> fillParams = new HashMap<String, Object>();

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            fillParams.put("LogoURL", getServletConfig().getServletContext().getResource("/images/bank_logo_1.png"));
            fillParams.put("DateImpression", Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            fillParams.put("IntervalDate", request.getParameter("interval"));
            fillParams.put("PrintDetail", request.getParameter("avecDetail"));

            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            String requete = "SELECT * FROM (" + request.getParameter("requete") + ") WHERE ETABLISSEMENT='" + user.getAdresse().trim() + "'";

            fillParams.put("Auteur", user.getLogin().trim());
            rapportBean.jasperReport(request.getParameter("nomrapport"), request.getParameter("typerapport"), db.execute(requete), fillParams, request, response);
            fillParams.clear();
            db.close();

            // writeResponse(request, response);
        } catch (Exception ex) {
            db.close();
            Logger.getLogger(ControlServlet.class.getName()).log(Level.SEVERE, null, ex);

        }

    }

    public void showRapportSyntheses(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            Map<String, Object> fillParams = new HashMap<String, Object>();

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            fillParams.put("LogoURL", getServletConfig().getServletContext().getResource("/images/bank_logo_1.png"));
            fillParams.put("DateImpression", Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            fillParams.put("dateCompensation", request.getParameter("dateChoisie"));
            fillParams.put("dateCompensationPrec", request.getParameter("datePrecChoisie"));
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            fillParams.put("Auteur", user.getLogin().trim());
            rapportBean.jasperReport(request.getParameter("nomrapport"), request.getParameter("typerapport"), db.getConn(), fillParams, request, response);
            fillParams.clear();
            db.close();

            // writeResponse(request, response);
        } catch (Exception ex) {
            db.close();
            Logger.getLogger(ControlServlet.class.getName()).log(Level.SEVERE, null, ex);

        }

    }

    public void showRapportActivite(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            Map<String, Object> fillParams = new HashMap<String, Object>();

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            fillParams.put("LogoURL", getServletConfig().getServletContext().getResource("/images/bank_logo_1.png"));
            fillParams.put("DateImpression", Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            fillParams.put("dateCompensation", request.getParameter("dateChoisie"));
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            fillParams.put("Auteur", user.getLogin().trim());
            rapportBean.jasperReport(request.getParameter("nomrapport"), request.getParameter("typerapport"), db.getConn(), fillParams, request, response);
            fillParams.clear();
            db.close();

            // writeResponse(request, response);
        } catch (Exception ex) {
            db.close();
            Logger.getLogger(ControlServlet.class.getName()).log(Level.SEVERE, null, ex);

        }

    }

    public String deleteDocument(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            String idObjet = request.getParameter("idObjet");
            String typeObjet = request.getParameter("typeObjet");
            String sequenceObjet = request.getParameter("sequenceObjet");

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            if (sequenceObjet != null && !sequenceObjet.trim().equals("")) {
                db.executeUpdate("DELETE FROM SEQUENCES WHERE IDSEQUENCE=" + sequenceObjet);

                String cheminImage = "";
                if (typeObjet != null && typeObjet.trim().equals("REMISE")) {
                    Remises remise[] = (Remises[]) db.retrieveRowAsObject("SELECT * FROM REMISES WHERE IDREMISE = " + idObjet, new Remises());

                    if (remise != null && remise.length > 0) {
                        if (remise[0].getEtat().intValue() < new Integer(Utility.getParam("CETAREMVAL")).intValue()) {
                            cheminImage = remise[0].getPathimage() + File.separator + remise[0].getFichierimage();
//                            new File(cheminImage + "f.jpg").delete();
//                            new File(cheminImage + "r.jpg").delete();
                            db.insertObjectAsRowByQuery(remise[0], "REMISES_SUPP");
                        }

                    }

                    if (remise[0].getEtat().intValue() < new Integer(Utility.getParam("CETAREMVAL")).intValue()) {
                        db.executeUpdate("DELETE FROM REMISES WHERE IDREMISE=" + idObjet);
                        //Suppression des cheques de la remise
                        Cheques cheques[] = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE REMISE = " + idObjet, new Cheques());
                        if (cheques != null) {
                            for (int i = 0; i < cheques.length; i++) {
                                Cheques cheques1 = cheques[i];
                                if (cheques1.getEtat().intValue() < new Integer(Utility.getParam("CETAOPEVAL")).intValue()) {
                                    db.executeUpdate("DELETE FROM SEQUENCES WHERE IDSEQUENCE=" + cheques1.getSequence());
                                    db.insertObjectAsRowByQuery(cheques1, "CHEQUES_SUPP");
                                    db.executeUpdate("DELETE FROM CHEQUES WHERE IDCHEQUE=" + cheques1.getIdcheque());
                                } else {
                                    System.out.println("Document cheque non supprime");
                                }

                            }

                            // db.executeUpdate("DELETE FROM CHEQUES WHERE REMISE=" + idObjet);
                        }
                    } else {
                        System.out.println("Document remise non supprime " + idObjet);
                    }

                } else if (typeObjet != null && typeObjet.trim().equals("CHEQUE")) {
                    Cheques cheque[] = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE IDCHEQUE = " + idObjet, new Cheques());
                    if (cheque != null && cheque.length > 0) {
                        if (cheque[0].getEtat().intValue() < new Integer(Utility.getParam("CETAOPEVAL")).intValue()) {
                            cheminImage = cheque[0].getPathimage() + File.separator + cheque[0].getFichierimage();
//                            new File(cheminImage + "f.jpg").delete();
//                            new File(cheminImage + "r.jpg").delete();
//                            new File(cheminImage + "d.txt").delete();
                            db.insertObjectAsRowByQuery(cheque[0], "CHEQUES_SUPP");
                        }
                        //Gestion du dernier cheque de la remise
                        //verifions la position du cheque
                        int index = 0;
                        String sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheque[0].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAREMSTO") + "," + Utility.getParam("CETAREMSAI") + ")";
                        Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                        if (remises != null && remises.length > 0) {
                            sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheque[0].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPESTO") + "," + Utility.getParam("CETAOPESAI") + ")  ORDER BY SEQUENCE";
                            Cheques[] chequesCal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                            if (chequesCal != null && chequesCal.length > 0) {
                                for (int i = 0; i < chequesCal.length; i++) {
                                    if (chequesCal[i].getIdcheque().equals(cheque[0].getIdcheque())) {
                                        index = i + 1;
                                    }
                                }

                                if (chequesCal.length > 1 && index == chequesCal.length) {
                                    //Dernier cheque, il faut donc inserer l'avant dernier dans Sequences
                                    Cheques avdCheque = chequesCal[index - 2];
                                    Sequences sequences = new Sequences();
                                    sequences.setTypedocument("CHEQUE");
                                    sequences.setMachinescan(avdCheque.getMachinescan());
                                    sequences.setIdsequence(avdCheque.getSequence());
                                    sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                                    sequences.setCodeline("___________");
                                    db.insertObjectAsRowByQuery(sequences, "SEQUENCES");
                                    //mettre a jour avdCheque pour dire qu'il n'est pas encore saisi
                                    avdCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPESTO")));
                                    db.updateRowByObjectByQuery(avdCheque, "CHEQUES", "IDCHEQUE=" + avdCheque.getIdcheque());

                                } else if (chequesCal.length == 1 && index == chequesCal.length) {
                                    //Dernier cheque sans avant dernier, donc suppression de la remise elle-même
                                    db.executeUpdate("DELETE FROM REMISES WHERE IDREMISE=" + remises[0].getIdremise());

                                }
                                // Mettre a jour le nbOperation dans la remise
                                if (chequesCal.length > 1) {
                                    remises[0].setNbOperation(new BigDecimal(chequesCal.length - 1));
                                    db.updateRowByObjectByQuery(remises[0], "REMISES", "IDREMISE=" + remises[0].getIdremise());
                                }
                            }
                        }

                    }
                    if (cheque != null && cheque.length > 0) {
                        if (cheque[0].getEtat().intValue() < new Integer(Utility.getParam("CETAOPEVAL")).intValue()) {
                            db.executeUpdate("DELETE FROM CHEQUES WHERE IDCHEQUE=" + idObjet);
                        } else {
                            System.out.println("Document cheque non supprime " + idObjet);
                        }
                    }
                }
                db.close();
                return printMessage("Document(s) supprime(s) avec succes");
            }
            db.close();
        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            db.close();
        }
        return printMessage("Erreur de suppression -" + db.getMessage());

    }

    public String deleteDocumentCorrige(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            String idObjet = request.getParameter("idObjet");
            String typeObjet = request.getParameter("typeObjet");

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String cheminImage = "";
            if (typeObjet != null && typeObjet.trim().equals("REMISE")) {
                Remises remise[] = (Remises[]) db.retrieveRowAsObject("SELECT * FROM REMISES WHERE IDREMISE = " + idObjet, new Remises());

                if (remise != null && remise.length > 0) {
                    if (remise[0].getEtat().intValue() < new Integer(Utility.getParam("CETAREMVAL")).intValue()) {
                        cheminImage = remise[0].getPathimage() + File.separator + remise[0].getFichierimage();
//                        new File(cheminImage + "f.jpg").delete();
//                        new File(cheminImage + "r.jpg").delete();
                        db.insertObjectAsRowByQuery(remise[0], "REMISES_SUPP");
                    }

                }

                if (remise[0].getEtat().intValue() < new Integer(Utility.getParam("CETAREMVAL")).intValue()) {
                    db.executeUpdate("DELETE FROM REMISES WHERE IDREMISE=" + idObjet);
                    //Suppression des cheques de la remise
                    Cheques cheques[] = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE REMISE = " + idObjet, new Cheques());
                    if (cheques != null) {
                        for (int i = 0; i < cheques.length; i++) {
                            Cheques cheques1 = cheques[i];
                            if (cheques1.getEtat().intValue() < new Integer(Utility.getParam("CETAOPEVAL")).intValue()) {
                                db.executeUpdate("DELETE FROM SEQUENCES WHERE IDSEQUENCE=" + cheques1.getSequence());
                                db.insertObjectAsRowByQuery(cheques1, "CHEQUES_SUPP");
                                db.executeUpdate("DELETE FROM CHEQUES WHERE IDCHEQUE=" + cheques1.getIdcheque());
                            } else {
                                System.out.println("Document cheque non supprime");
                            }

                        }

                        // db.executeUpdate("DELETE FROM CHEQUES WHERE REMISE=" + idObjet);
                    }
                } else {
                    System.out.println("Document remise non supprime " + idObjet);
                }

            } else if (typeObjet != null && typeObjet.trim().equals("CHEQUE")) {
                Cheques cheque[] = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE IDCHEQUE = " + idObjet, new Cheques());
                if (cheque != null && cheque.length > 0) {
                    if (cheque[0].getEtat().intValue() < new Integer(Utility.getParam("CETAOPEVAL")).intValue()) {
                        cheminImage = cheque[0].getPathimage() + File.separator + cheque[0].getFichierimage();
//                        new File(cheminImage + "f.jpg").delete();
//                        new File(cheminImage + "r.jpg").delete();
//                        new File(cheminImage + "d.txt").delete();
                        db.insertObjectAsRowByQuery(cheque[0], "CHEQUES_SUPP");
                    }
                    //Gestion du dernier cheque de la remise
                    //verifions la position du cheque
                    int index = 0;
                    String sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheque[0].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPEANO") + "," + Utility.getParam("CETAOPECOR") + ")";
                    Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                    if (remises != null && remises.length > 0) {
                        sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheque[0].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPEANO") + "," + Utility.getParam("CETAOPECOR") + ")  ORDER BY SEQUENCE";
                        Cheques[] chequesCal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                        if (chequesCal != null && chequesCal.length > 0) {
                            for (int i = 0; i < chequesCal.length; i++) {
                                if (chequesCal[i].getIdcheque().equals(cheque[0].getIdcheque())) {
                                    index = i + 1;
                                }
                            }

                            if (chequesCal.length > 1 && index == chequesCal.length) {
                                //Dernier cheque, il faut donc inserer l'avant dernier dans Sequences
                                Cheques avdCheque = chequesCal[index - 2];

                                //mettre a jour avdCheque pour dire qu'il n'est pas encore saisi
                                avdCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                                db.updateRowByObjectByQuery(avdCheque, "CHEQUES", "IDCHEQUE=" + avdCheque.getIdcheque());

                            } else if (chequesCal.length == 1 && index == chequesCal.length) {
                                //Dernier cheque sans avant dernier, donc suppression de la remise elle-même
                                db.executeUpdate("DELETE FROM REMISES WHERE IDREMISE=" + remises[0].getIdremise());

                            }
                            // Mettre a jour le nbOperation dans la remise
                            if (chequesCal.length > 1) {
                                remises[0].setNbOperation(new BigDecimal(chequesCal.length - 1));
                                db.updateRowByObjectByQuery(remises[0], "REMISES", "IDREMISE=" + remises[0].getIdremise());
                            }
                        }
                    }

                }
                if (cheque != null && cheque.length > 0) {
                    if (cheque[0].getEtat().intValue() < new Integer(Utility.getParam("CETAOPEVAL")).intValue()) {
                        db.executeUpdate("DELETE FROM CHEQUES WHERE IDCHEQUE=" + idObjet);
                    } else {
                        System.out.println("Document cheque non supprime " + idObjet);
                    }
                }
            }
            db.close();
            return printMessage("Document(s) supprime(s) avec succes");

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            db.close();
        }
        return printMessage("Erreur de suppression -" + db.getMessage());

    }

    public String rejectDocumentLite(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            String idObjet = request.getParameter("idObjet");
            String typeObjet = request.getParameter("typeObjet");
            String remarques = request.getParameter("remarques");

            String motifRejet = request.getParameter("motifrejet");

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String cheminImage = "";
            if (typeObjet != null && typeObjet.trim().equals("REMISE")) {
                Remises remise[] = (Remises[]) db.retrieveRowAsObject("SELECT * FROM REMISES WHERE IDREMISE = " + idObjet, new Remises());

                if (remise != null && remise.length > 0) {
                    if (remise[0].getEtat().intValue() == new Integer(Utility.getParam("CETAREMVAL")).intValue()) {
                        //cheminImage = remise[0].getPathimage() + File.separator + remise[0].getFichierimage();
                        //new File(cheminImage + "f.jpg").delete();
                        //new File(cheminImage + "r.jpg").delete();
                        remise[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                        db.insertObjectAsRowByQuery(remise[0], "REMISES_REJETES");
                    }

                }

                if (remise[0].getEtat().intValue() == Integer.parseInt(Utility.getParam("CETAREMVAL"))) {
                    db.executeUpdate("DELETE FROM REMISES WHERE IDREMISE=" + idObjet);
                    //Suppression des cheques de la remise
                    Cheques cheques[] = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE REMISE = " + idObjet, new Cheques());
                    if (cheques != null) {
                        for (int i = 0; i < cheques.length; i++) {
                            Cheques cheques1 = cheques[i];
                            if (cheques1.getEtat().intValue() == new Integer(Utility.getParam("CETAOPEVAL")).intValue()) {
                                cheques1.setIban(remarques);
                                cheques1.setMotifrejet(motifRejet);
                                cheques1.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                                db.insertObjectAsRowByQuery(cheques1, "CHEQUES_REJETES");

                                db.executeUpdate("DELETE FROM CHEQUES WHERE IDCHEQUE=" + cheques1.getIdcheque());
                            } else {
                                System.out.println("Document cheque non rejete");
                            }

                        }

                        // db.executeUpdate("DELETE FROM CHEQUES WHERE REMISE=" + idObjet);
                    }
                } else {
                    System.out.println("Document remise non rejete " + idObjet);
                }

            } else if (typeObjet != null && typeObjet.trim().equals("CHEQUE")) {
                Cheques cheque[] = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE IDCHEQUE = " + idObjet, new Cheques());
                if (cheque != null && cheque.length > 0) {
                    if (cheque[0].getEtat().intValue() == new Integer(Utility.getParam("CETAOPEVAL")).intValue()) {
                        //cheminImage = cheque[0].getPathimage() + File.separator + cheque[0].getFichierimage();
                        // new File(cheminImage + "f.jpg").delete();
                        //new File(cheminImage + "r.jpg").delete();
                        //new File(cheminImage + "d.txt").delete();
                        cheque[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                        cheque[0].setIban(remarques);
                        db.insertObjectAsRowByQuery(cheque[0], "CHEQUES_REJETES");
                    }
                    //Gestion du dernier cheque de la remise
                    //verifions la position du cheque
                    int index = 0;
                    String sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheque[0].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEVAL2") + ")";
                    Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                    if (remises != null && remises.length > 0) {
                        sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheque[0].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEVAL2") + ")  ORDER BY SEQUENCE";
                        Cheques[] chequesCal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                        if (chequesCal != null && chequesCal.length > 0) {
                            for (int i = 0; i < chequesCal.length; i++) {
                                if (chequesCal[i].getIdcheque().equals(cheque[0].getIdcheque())) {
                                    index = i + 1;
                                }
                            }

                            if (chequesCal.length > 1 && index == chequesCal.length) {
                                //Dernier cheque, il faut donc inserer l'avant dernier dans Sequences
                                Cheques avdCheque = chequesCal[index - 2];

                                //mettre a jour avdCheque pour dire qu'il n'est pas encore saisi
                                avdCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL")));
                                db.updateRowByObjectByQuery(avdCheque, "CHEQUES", "IDCHEQUE=" + avdCheque.getIdcheque());

                            } else if (chequesCal.length == 1 && index == chequesCal.length) {
                                //Dernier cheque sans avant dernier, donc suppression de la remise elle-même
                                remises[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                                db.insertObjectAsRowByQuery(remises[0], "REMISES_REJETES");
                                db.executeUpdate("DELETE FROM REMISES WHERE IDREMISE=" + remises[0].getIdremise());

                            }
                            // Mettre a jour le nbOperation dans la remise
                            if (chequesCal.length > 1) {
                                remises[0].setNbOperation(new BigDecimal(chequesCal.length - 1));
                                long montantRemise = Long.parseLong(remises[0].getMontant()) - Long.parseLong(cheque[0].getMontantcheque());
                                remises[0].setMontant("" + montantRemise);
                                db.updateRowByObjectByQuery(remises[0], "REMISES", "IDREMISE=" + remises[0].getIdremise());
                            }
                        }
                    }

                }
                if (cheque != null && cheque.length > 0) {
                    if (cheque[0].getEtat().intValue() == new Integer(Utility.getParam("CETAOPEANO")).intValue()) {
                        db.executeUpdate("DELETE FROM CHEQUES WHERE IDCHEQUE=" + idObjet);
                    } else {
                        System.out.println("Document cheque non supprime " + idObjet);
                    }
                }
            }
            db.close();
            return printMessage("Document(s) rejete(s) avec succes");

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            db.close();
        }
        return printMessage("Erreur de suppression -" + db.getMessage());

    }

    public String rejectDocumentLiteRNC(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            String idObjet = request.getParameter("idObjet");
            String typeObjet = request.getParameter("typeObjet");
            String remarques = request.getParameter("remarques");

            String motifRejet = request.getParameter("motifrejet");

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String cheminImage = "";
            if (typeObjet != null && typeObjet.trim().equals("REMISE")) {
                Remises remise[] = (Remises[]) db.retrieveRowAsObject("SELECT * FROM REMISES WHERE IDREMISE = " + idObjet, new Remises());

                if (remise != null && remise.length > 0) {
                    if (remise[0].getEtat().intValue() == new Integer(Utility.getParam("CETAOPEVALDELTA")).intValue()) {
                        //cheminImage = remise[0].getPathimage() + File.separator + remise[0].getFichierimage();
                        //new File(cheminImage + "f.jpg").delete();
                        //new File(cheminImage + "r.jpg").delete();
                        remise[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                        db.insertObjectAsRowByQuery(remise[0], "REMISES_REJETES");
                    }

                }

                if (remise[0].getEtat().intValue() == Integer.parseInt(Utility.getParam("CETAOPEVALDELTA"))) {
                    db.executeUpdate("DELETE FROM REMISES WHERE IDREMISE=" + idObjet);
                    //Suppression des cheques de la remise
                    Cheques cheques[] = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE REMISE = " + idObjet, new Cheques());
                    if (cheques != null) {
                        for (int i = 0; i < cheques.length; i++) {
                            Cheques cheques1 = cheques[i];
                            if (cheques1.getEtat().intValue() == new Integer(Utility.getParam("CETAOPEVALDELTA")).intValue()) {
                                cheques1.setIban(remarques);
                                cheques1.setMotifrejet(motifRejet);
                                cheques1.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                                db.insertObjectAsRowByQuery(cheques1, "CHEQUES_REJETES");

                                db.executeUpdate("DELETE FROM CHEQUES WHERE IDCHEQUE=" + cheques1.getIdcheque());
                            } else {
                                System.out.println("Document cheque non rejete");
                            }

                        }

                        // db.executeUpdate("DELETE FROM CHEQUES WHERE REMISE=" + idObjet);
                    }
                } else {
                    System.out.println("Document remise non rejete " + idObjet);
                }

            } else if (typeObjet != null && typeObjet.trim().equals("CHEQUE")) {
                Cheques cheque[] = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE IDCHEQUE = " + idObjet, new Cheques());
                if (cheque != null && cheque.length > 0) {
                    if (cheque[0].getEtat().intValue() == new Integer(Utility.getParam("CETAOPEVALDELTA"))
                            || cheque[0].getEtat().intValue() == new Integer(Utility.getParam("CETAOPEVALDELTA2"))) {
                        //cheminImage = cheque[0].getPathimage() + File.separator + cheque[0].getFichierimage();
                        // new File(cheminImage + "f.jpg").delete();
                        //new File(cheminImage + "r.jpg").delete();
                        //new File(cheminImage + "d.txt").delete();
                        cheque[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                        cheque[0].setIban(remarques);
                        db.insertObjectAsRowByQuery(cheque[0], "CHEQUES_REJETES");
                    }
                    //Gestion du dernier cheque de la remise
                    //verifions la position du cheque
                    int index = 0;
                    String sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheque[0].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPEVALDELTA") + "," + Utility.getParam("CETAOPEVALDELTA2") + ")";
                    Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                    if (remises != null && remises.length > 0) {
                        sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheque[0].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPEVALDELTA") + "," + Utility.getParam("CETAOPEVALDELTA2") + ")  ORDER BY SEQUENCE";
                        Cheques[] chequesCal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                        if (chequesCal != null && chequesCal.length > 0) {
                            for (int i = 0; i < chequesCal.length; i++) {
                                if (chequesCal[i].getIdcheque().equals(cheque[0].getIdcheque())) {
                                    index = i + 1;
                                }
                            }

                            if (chequesCal.length > 1 && index == chequesCal.length) {
                                //Dernier cheque, il faut donc inserer l'avant dernier dans Sequences
                                Cheques avdCheque = chequesCal[index - 2];

                                //mettre a jour avdCheque pour dire qu'il n'est pas encore saisi
                                avdCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALDELTA")));
                                db.updateRowByObjectByQuery(avdCheque, "CHEQUES", "IDCHEQUE=" + avdCheque.getIdcheque());

                            } else if (chequesCal.length == 1 && index == chequesCal.length) {
                                //Dernier cheque sans avant dernier, donc suppression de la remise elle-même
                                remises[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                                db.insertObjectAsRowByQuery(remises[0], "REMISES_REJETES");
                                db.executeUpdate("DELETE FROM REMISES WHERE IDREMISE=" + remises[0].getIdremise());

                            }
                            // Mettre a jour le nbOperation dans la remise
                            if (chequesCal.length > 1) {
                                remises[0].setNbOperation(new BigDecimal(chequesCal.length - 1));
                                long montantRemise = Long.parseLong(remises[0].getMontant()) - Long.parseLong(cheque[0].getMontantcheque());
                                remises[0].setMontant("" + montantRemise);
                                db.updateRowByObjectByQuery(remises[0], "REMISES", "IDREMISE=" + remises[0].getIdremise());
                            }
                        }
                    }

                }
                if (cheque != null && cheque.length > 0) {
                    if (cheque[0].getEtat().intValue() == new Integer(Utility.getParam("CETAOPEANO")).intValue()) {
                        db.executeUpdate("DELETE FROM CHEQUES WHERE IDCHEQUE=" + idObjet);
                    } else {
                        System.out.println("Document cheque non supprime " + idObjet);
                    }
                }
            }
            db.close();
            return printMessage("Document(s) rejete(s) avec succes");

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            db.close();
        }
        return printMessage("Erreur de suppression -" + db.getMessage());

    }

    public String rejectDocumentLiteCorp(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("entré dans la méthode de rejet");
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            String idObjet = request.getParameter("idObjet");
            String typeObjet = request.getParameter("typeObjet");
            String remarques = request.getParameter("remarques");

            String motifRejet = request.getParameter("motifrejet");

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String cheminImage = "";
            if (typeObjet != null && typeObjet.trim().equals("REMISE")) {
                
                Remises remise[] = (Remises[]) db.retrieveRowAsObject("SELECT * FROM REMISES WHERE IDREMISE = " + idObjet, new Remises());

                if (remise != null && remise.length > 0) {
                   
                    if (remise[0].getEtat().intValue() == Integer.parseInt(Utility.getParam("CETAOPEVALITE"))) {
                        //cheminImage = remise[0].getPathimage() + File.separator + remise[0].getFichierimage();
                        //new File(cheminImage + "f.jpg").delete();
                        //new File(cheminImage + "r.jpg").delete();
                         
                        remise[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEANO_LITE")));
                        db.insertObjectAsRowByQuery(remise[0], "REMISES_REJETES");
                    }

                }

                if (remise[0].getEtat().intValue() == Integer.parseInt(Utility.getParam("CETAOPEVALITE"))) {
                        //suppresion de la remise du cheque en question
                    db.executeUpdate("DELETE FROM REMISES WHERE IDREMISE=" + idObjet);
                    //Suppression des cheques de la remise
                    Cheques cheques[] = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE REMISE = " + idObjet, new Cheques());
                    if (cheques != null) {
                        for (Cheques cheques1 : cheques) {
                            if (cheques1.getEtat().intValue() == Integer.parseInt(Utility.getParam("CETAOPEVALITE"))) {
                                cheques1.setIban(remarques);
                                cheques1.setMotifrejet(motifRejet);
                                cheques1.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO_LITE")));
                                db.insertObjectAsRowByQuery(cheques1, "CHEQUES_REJETES");
                                  //suppression du cheques de la remise 
                                db.executeUpdate("DELETE FROM CHEQUES WHERE IDCHEQUE=" + cheques1.getIdcheque());
                            } else {
                                System.out.println("Document cheque non rejete");
                            }
                        } // db.executeUpdate("DELETE FROM CHEQUES WHERE REMISE=" + idObjet);
                    }
                } else {
                    System.out.println("Document remise non rejete " + idObjet);
                }

            } else if (typeObjet != null && typeObjet.trim().equals("CHEQUE")) {
                System.out.println("recuperation  du cheque a rejeter");
                Cheques cheque[] = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE IDCHEQUE = " + idObjet, new Cheques());
                if (cheque != null && cheque.length > 0) {
                    if (cheque[0].getEtat().intValue() == Integer.parseInt(Utility.getParam("CETAOPEVALITE"))) {
                        //cheminImage = cheque[0].getPathimage() + File.separator + cheque[0].getFichierimage();
                        // new File(cheminImage + "f.jpg").delete();
                        //new File(cheminImage + "r.jpg").delete();
                        //new File(cheminImage + "d.txt").delete();
                        System.out.println("recuperation du bon etat :"+Utility.getParam("CETAOPEANO_LITE"));
                        cheque[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEANO_LITE")));
                        cheque[0].setIban(remarques);
                        db.insertObjectAsRowByQuery(cheque[0], "CHEQUES_REJETES");
                        //recuperation de l'id du cheque 
                        //System.out.println("l'id du cheque est -> : "+cheque[0].getIdcheque());
                        //db.executeUpdate("DELETE FROM CHEQUES WHERE IDCHEQUE="+idObjet );
                        //recuperation de sa remise pour suppression de la table remise_cheques
                        //System.out.println("l'id de sa remise est : ->"+cheque[0].getRemise());
                        //db.executeUpdate("DELETE FROM REMISES WHERE IDCHEQUE="+cheque[0].getRemise() );
                        
                    }
                    //Gestion du dernier cheque de la remise
                    //verifions la position du cheque
                    int index = 0;
                    String sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheque[0].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPEVALITE") + "," + Utility.getParam("CETAOPEVALITE2") + ")";
                    Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                    if (remises != null && remises.length > 0) {
                        sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheque[0].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPEVALITE") + "," + Utility.getParam("CETAOPEVALITE2") + ")  ORDER BY SEQUENCE";
                        Cheques[] chequesCal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                        if (chequesCal != null && chequesCal.length > 0) {
                            for (int i = 0; i < chequesCal.length; i++) {
                                if (chequesCal[i].getIdcheque().equals(cheque[0].getIdcheque())) {
                                    index = i + 1;
                                }
                            }

                            if (chequesCal.length > 1 && index == chequesCal.length) {
                                //Dernier cheque, il faut donc inserer l'avant dernier dans Sequences
                                Cheques avdCheque = chequesCal[index - 2];

                                //mettre a jour avdCheque pour dire qu'il n'est pas encore saisi
                                avdCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALITE")));
                                db.updateRowByObjectByQuery(avdCheque, "CHEQUES", "IDCHEQUE=" + avdCheque.getIdcheque());

                            } else if (chequesCal.length == 1 && index == chequesCal.length) {
                                //Dernier cheque sans avant dernier, donc suppression de la remise elle-même
                                remises[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEANO_LITE")));
                                db.insertObjectAsRowByQuery(remises[0], "REMISES_REJETES");
                                db.executeUpdate("DELETE FROM REMISES WHERE IDREMISE=" + remises[0].getIdremise());

                            }
                            // Mettre a jour le nbOperation dans la remise
                            if (chequesCal.length > 1) {
                                remises[0].setNbOperation(new BigDecimal(chequesCal.length - 1));
                                long montantRemise = Long.parseLong(remises[0].getMontant()) - Long.parseLong(cheque[0].getMontantcheque());
                                remises[0].setMontant("" + montantRemise);
                                db.updateRowByObjectByQuery(remises[0], "REMISES", "IDREMISE=" + remises[0].getIdremise());
                            }
                        }
                    }

                }
                if (cheque != null && cheque.length > 0) {
                    if (cheque[0].getEtat().intValue() == Integer.parseInt(Utility.getParam("CETAOPEANO_LITE"))) {
                        db.executeUpdate("DELETE FROM CHEQUES WHERE IDCHEQUE=" + idObjet);
                    } else {
                        System.out.println("Document cheque non supprime " + idObjet);
                    }
                }
            }
            db.close();
            return printMessage("Document(s) rejete(s) avec succes");

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            db.close();
        }
        return printMessage("Erreur de suppression -" + db.getMessage());

    }

    public String rejectDocumentETG(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            String idObjet = request.getParameter("idObjet");
            String typeObjet = request.getParameter("typeObjet");
            String remarques = request.getParameter("remarques");

            String motifRejet = request.getParameter("motifrejet");

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String cheminImage = "";
            if (typeObjet != null && typeObjet.trim().equals("REMISE")) {
                Remises remise[] = (Remises[]) db.retrieveRowAsObject("SELECT * FROM REMISES WHERE IDREMISE = " + idObjet, new Remises());

                if (remise != null && remise.length > 0) {
                    if (remise[0].getEtat().intValue() == Integer.parseInt(Utility.getParam("CETAREMVAL"))) {
                        //cheminImage = remise[0].getPathimage() + File.separator + remise[0].getFichierimage();
                        //new File(cheminImage + "f.jpg").delete();
                        //new File(cheminImage + "r.jpg").delete();
                        remise[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                        db.insertObjectAsRowByQuery(remise[0], "REMISES_REJETES");
                    }

                }

                if (remise[0].getEtat().intValue() == Integer.parseInt(Utility.getParam("CETAREMVAL"))) {
                    db.executeUpdate("DELETE FROM REMISES WHERE IDREMISE=" + idObjet);
                    //Suppression des cheques de la remise
                    Cheques cheques[] = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE REMISE = " + idObjet, new Cheques());
                    if (cheques != null) {
                        for (Cheques cheques1 : cheques) {
                            if (cheques1.getEtat().intValue() == Integer.parseInt(Utility.getParam("CETAREMVAL"))) {
                                cheques1.setIban(remarques);
                                cheques1.setMotifrejet(motifRejet);
                                cheques1.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                                db.insertObjectAsRowByQuery(cheques1, "CHEQUES_REJETES");

                                db.executeUpdate("DELETE FROM CHEQUES WHERE IDCHEQUE=" + cheques1.getIdcheque());
                            } else {
                                System.out.println("Document cheque non rejete");
                            }
                        } // db.executeUpdate("DELETE FROM CHEQUES WHERE REMISE=" + idObjet);
                    }
                } else {
                    System.out.println("Document remise non rejete " + idObjet);
                }

            } else if (typeObjet != null && typeObjet.trim().equals("CHEQUE")) {
                Cheques cheque[] = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE IDCHEQUE = " + idObjet, new Cheques());
                if (cheque != null && cheque.length > 0) {
                    if (cheque[0].getEtat().intValue() == Integer.parseInt(Utility.getParam("CETAOPEVALSURCAI"))) {
                        //cheminImage = cheque[0].getPathimage() + File.separator + cheque[0].getFichierimage();
                        // new File(cheminImage + "f.jpg").delete();
                        //new File(cheminImage + "r.jpg").delete();
                        //new File(cheminImage + "d.txt").delete();
                        cheque[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                        cheque[0].setIban(remarques);
                        db.insertObjectAsRowByQuery(cheque[0], "CHEQUES_REJETES");
                    }
                    //Gestion du dernier cheque de la remise
                    //verifions la position du cheque
                    int index = 0;
                    String sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheque[0].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAREMVAL") + "," + Utility.getParam("CETAREMVAL") + ")";
                    Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                    if (remises != null && remises.length > 0) {
                        sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheque[0].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPEVALSURCAI") + "," + Utility.getParam("CETAOPEVALSURCAI") + ")  ORDER BY SEQUENCE";
                        Cheques[] chequesCal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                        if (chequesCal != null && chequesCal.length > 0) {
                            for (int i = 0; i < chequesCal.length; i++) {
                                if (chequesCal[i].getIdcheque().equals(cheque[0].getIdcheque())) {
                                    index = i + 1;
                                }
                            }

                            if (chequesCal.length > 1 && index == chequesCal.length) {
                                //Dernier cheque, il faut donc inserer l'avant dernier dans Sequences
                                Cheques avdCheque = chequesCal[index - 2];

                                //mettre a jour avdCheque pour dire qu'il n'est pas encore saisi
                                avdCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALSURCAI")));
                                db.updateRowByObjectByQuery(avdCheque, "CHEQUES", "IDCHEQUE=" + avdCheque.getIdcheque());

                            } else if (chequesCal.length == 1 && index == chequesCal.length) {
                                //Dernier cheque sans avant dernier, donc suppression de la remise elle-même
                                remises[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                                db.insertObjectAsRowByQuery(remises[0], "REMISES_REJETES");
                                db.executeUpdate("DELETE FROM REMISES WHERE IDREMISE=" + remises[0].getIdremise());

                            }
                            // Mettre a jour le nbOperation dans la remise
                            if (chequesCal.length > 1) {
                                remises[0].setNbOperation(new BigDecimal(chequesCal.length - 1));
                                long montantRemise = Long.parseLong(remises[0].getMontant()) - Long.parseLong(cheque[0].getMontantcheque());
                                remises[0].setMontant("" + montantRemise);
                                db.updateRowByObjectByQuery(remises[0], "REMISES", "IDREMISE=" + remises[0].getIdremise());
                            }
                        }
                    }

                }
                if (cheque != null && cheque.length > 0) {
                    if (cheque[0].getEtat().intValue() == Integer.parseInt(Utility.getParam("CETAOPEANO"))) {
                        db.executeUpdate("DELETE FROM CHEQUES WHERE IDCHEQUE=" + idObjet);
                    } else {
                        System.out.println("Document cheque non supprime " + idObjet);
                    }
                }
            }
            db.close();
            return printMessage("Document(s) rejete(s) avec succes");

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            db.close();
        }
        return printMessage("Erreur de suppression -" + db.getMessage());

    }

    /* public String rejectDocumentETG(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("rejectDocumentETG");
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemiseCorr");
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            String idObjet = request.getParameter("idObjet");
            String typeObjet = request.getParameter("typeObjet");
            String remarques = request.getParameter("remarques");

            String motifRejet = request.getParameter("motifrejet");

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String cheminImage = "";

            if (typeObjet != null && typeObjet.trim().equals("REMISE")) {
                Remises remise[] = (Remises[]) db.retrieveRowAsObject("SELECT * FROM REMISES WHERE IDREMISE = " + idObjet, new Remises());

                if (remise != null && remise.length > 0) {
                    if (remise[0].getEtat().intValue() == new Integer(Utility.getParam("CETAREMVAL")).intValue()) {
                        //cheminImage = remise[0].getPathimage() + File.separator + remise[0].getFichierimage();
                        //new File(cheminImage + "f.jpg").delete();
                        //new File(cheminImage + "r.jpg").delete();
                        remise[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                        db.insertObjectAsRowByQuery(remise[0], "REMISES_REJETES");
                    }

                }

                if (remise[0].getEtat().intValue() == new Integer(Utility.getParam("CETAREMVAL")).intValue()) {
                    db.executeUpdate("DELETE FROM REMISES WHERE IDREMISE=" + idObjet);
                    //Suppression des cheques de la remise
                    Cheques cheques[] = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE REMISE = " + idObjet, new Cheques());
                    if (cheques != null) {
                        for (int i = 0; i < cheques.length; i++) {
                            Cheques cheques1 = cheques[i];
                            if (cheques1.getEtat().intValue() == new Integer(Utility.getParam("CETAOPEVAL")).intValue()) {
                                cheques1.setIban(remarques);
                                cheques1.setMotifrejet(motifRejet);
                                cheques1.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                                cheques1.setLotsib(BigDecimal.ONE);
                                db.insertObjectAsRowByQuery(cheques1, "CHEQUES_REJETES");

                                db.executeUpdate("DELETE FROM CHEQUES WHERE IDCHEQUE=" + cheques1.getIdcheque());
                            } else {
                                System.out.println("Document cheque non rejete");
                            }

                        }

                        // db.executeUpdate("DELETE FROM CHEQUES WHERE REMISE=" + idObjet);
                    }
                } else {
                    System.out.println("Document remise non rejete " + idObjet);
                }

            } else if (typeObjet != null && typeObjet.trim().equals("CHEQUE")) {
                Cheques cheque[] = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE IDCHEQUE = " + idObjet, new Cheques());
                if (cheque != null && cheque.length > 0) {
                    System.out.println("cheque[0]" + cheque[0].getIdcheque() + " Numero " + cheque[0].getNumerocheque());
                    int index = Integer.parseInt(request.getParameter("index"));
                    String nbOpers = request.getParameter("nbOpers");
                    String motifrejet = request.getParameter("motifrejet");

                    String sql = "SELECT * FROM CHEQUES WHERE ETAT >= " + Utility.getParam("CETAOPESUPVALSURCAI") + "  AND REMISE=" + cheque[0].getRemise();

                    Cheques[] chequesRemises = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                    int nbOperations = 0;
                    if (chequesRemises != null && chequesRemises.length > 0) {
                        nbOperations = chequesRemises.length;

                    }

                    cheque[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                    cheque[0].setLotsib(BigDecimal.ONE);
                    cheque[0].setIban(remarques);
                    cheque[0].setMotifrejet(motifrejet);
                    cheque[0].setCodeutilisateur(cheque[0].getCodeutilisateur()+"+"+user.getLogin().trim());
                    cheque[0].setVilleremettant("00"); //Annuler 00 Annuler, 01 Pas encore touche, 02 Valide
                    boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(cheque[0], "CHEQUES", "IDCHEQUE=" + cheque[0].getIdcheque());

                    if (updateRowByObjectByQuery) {

                        if (index == nbOperations) { //
                            if (db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMSIGVER") + ", VALIDEUR='" + currentRemise.getValideur().trim() + " + " + user.getLogin().trim() + "' WHERE IDREMISE=" + currentRemise.getIdremise()) == 1) {
                                //     if (db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETASUPSIGVER") + " WHERE IDREMISE=" + cheque[0].getRemise()) == 1) {
                                db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMSIGVER") + " WHERE IDREMISE=" + cheque[0].getRemise());

                            } else {
                                db.executeUpdate("UPDATE CHEQUES SET VILLEREMETTANT= '01' WHERE ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " AND REMISE=" + cheque[0].getRemise());
                                //    db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVAL") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                                db.close();
                                return printMessage("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());
                            }

                        }

                        db.close();
                        return printMessage("OK");
                    }

                }

            }
            db.close();
            return printMessage("Document(s) rejete(s) avec succes");

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            db.close();
        }
        return printMessage("Erreur de suppression -" + db.getMessage());

    }*/
    public String rejectDocumentECI(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            String idObjet = request.getParameter("idObjet");
            String typeObjet = request.getParameter("typeObjet");
            String remarques = request.getParameter("remarques");

            String motifRejet = request.getParameter("motifrejet");

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String cheminImage = "";
            if (typeObjet != null && typeObjet.trim().equals("REMISE")) {
                Remises remise[] = (Remises[]) db.retrieveRowAsObject("SELECT * FROM REMISES WHERE IDREMISE = " + idObjet, new Remises());

                if (remise != null && remise.length > 0) {
                    if (remise[0].getEtat().intValue() == Integer.parseInt(Utility.getParam("CETAREMSAIIRIS"))) {
                        //cheminImage = remise[0].getPathimage() + File.separator + remise[0].getFichierimage();
                        //new File(cheminImage + "f.jpg").delete();
                        //new File(cheminImage + "r.jpg").delete();
                        remise[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                        db.insertObjectAsRowByQuery(remise[0], "REMISES_REJETES");
                    }

                }

                if (remise[0].getEtat().intValue() == Integer.parseInt(Utility.getParam("CETAREMSAIIRIS"))) {
                    db.executeUpdate("DELETE FROM REMISES WHERE IDREMISE=" + idObjet);
                    //Suppression des cheques de la remise
                    Cheques cheques[] = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE REMISE = " + idObjet, new Cheques());
                    if (cheques != null) {
                        for (Cheques cheques1 : cheques) {
                            if (cheques1.getEtat().intValue() == Integer.parseInt(Utility.getParam("CETAREMSAIIRIS"))) {
                                cheques1.setIban(remarques);
                                cheques1.setMotifrejet(motifRejet);
                                cheques1.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                                db.insertObjectAsRowByQuery(cheques1, "CHEQUES_REJETES");

                                db.executeUpdate("DELETE FROM CHEQUES WHERE IDCHEQUE=" + cheques1.getIdcheque());
                            } else {
                                System.out.println("Document cheque non rejete");
                            }
                        } // db.executeUpdate("DELETE FROM CHEQUES WHERE REMISE=" + idObjet);
                    }
                } else {
                    System.out.println("Document remise non rejete " + idObjet);
                }

            } else if (typeObjet != null && typeObjet.trim().equals("CHEQUE")) {
                Cheques cheque[] = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE IDCHEQUE = " + idObjet, new Cheques());
                if (cheque != null && cheque.length > 0) {
                    if (cheque[0].getEtat().intValue() == Integer.parseInt(Utility.getParam("CETAREMSAIIRIS"))) {
                        //cheminImage = cheque[0].getPathimage() + File.separator + cheque[0].getFichierimage();
                        // new File(cheminImage + "f.jpg").delete();
                        //new File(cheminImage + "r.jpg").delete();
                        //new File(cheminImage + "d.txt").delete();
                        cheque[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                        cheque[0].setIban(remarques);
                        db.insertObjectAsRowByQuery(cheque[0], "CHEQUES_REJETES");
                    }
                    //Gestion du dernier cheque de la remise
                    //verifions la position du cheque
                    int index = 0;
                    String sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheque[0].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAREMSAIIRIS") + "," + Utility.getParam("CETAREMSAIIRIS") + ")";
                    Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                    if (remises != null && remises.length > 0) {
                        sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheque[0].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAREMSAIIRIS") + "," + Utility.getParam("CETAREMSAIIRIS") + ")  ORDER BY SEQUENCE";
                        Cheques[] chequesCal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                        if (chequesCal != null && chequesCal.length > 0) {
                            for (int i = 0; i < chequesCal.length; i++) {
                                if (chequesCal[i].getIdcheque().equals(cheque[0].getIdcheque())) {
                                    index = i + 1;
                                }
                            }

                            if (chequesCal.length > 1 && index == chequesCal.length) {
                                //Dernier cheque, il faut donc inserer l'avant dernier dans Sequences
                                Cheques avdCheque = chequesCal[index - 2];

                                //mettre a jour avdCheque pour dire qu'il n'est pas encore saisi
                                avdCheque.setEtat(new BigDecimal(Utility.getParam("CETAREMSAIIRIS")));
                                db.updateRowByObjectByQuery(avdCheque, "CHEQUES", "IDCHEQUE=" + avdCheque.getIdcheque());

                            } else if (chequesCal.length == 1 && index == chequesCal.length) {
                                //Dernier cheque sans avant dernier, donc suppression de la remise elle-même
                                remises[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                                db.insertObjectAsRowByQuery(remises[0], "REMISES_REJETES");
                                db.executeUpdate("DELETE FROM REMISES WHERE IDREMISE=" + remises[0].getIdremise());

                            }
                            // Mettre a jour le nbOperation dans la remise
                            if (chequesCal.length > 1) {
                                remises[0].setNbOperation(new BigDecimal(chequesCal.length - 1));
                                long montantRemise = Long.parseLong(remises[0].getMontant()) - Long.parseLong(cheque[0].getMontantcheque());
                                remises[0].setMontant("" + montantRemise);
                                db.updateRowByObjectByQuery(remises[0], "REMISES", "IDREMISE=" + remises[0].getIdremise());
                            }
                        }
                    }

                }
                if (cheque != null && cheque.length > 0) {
                    if (cheque[0].getEtat().intValue() == Integer.parseInt(Utility.getParam("CETAOPEANO"))) {
                        db.executeUpdate("DELETE FROM CHEQUES WHERE IDCHEQUE=" + idObjet);
                    } else {
                        System.out.println("Document cheque non supprime " + idObjet);
                    }
                }
            }
            db.close();
            return printMessage("Document(s) rejete(s) avec succes");

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            db.close();
        }
        return printMessage("Erreur de suppression -" + db.getMessage());

    }

    public String deleteDocumentCheque(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            String idObjet = request.getParameter("idObjet");
            String typeObjet = request.getParameter("typeObjet");
            String sequenceObjet = request.getParameter("sequenceObjet");

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            if (sequenceObjet != null && !sequenceObjet.trim().equals("")) {
                db.executeUpdate("DELETE FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE=" + sequenceObjet);

                String cheminImage = "";
                if (typeObjet != null && typeObjet.trim().equals("CHEQUE")) {
                    Cheques cheque[] = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE IDCHEQUE = " + idObjet, new Cheques());
                    if (cheque != null && cheque.length > 0) {
                        if (cheque[0].getEtat().intValue() < new Integer(Utility.getParam("CETAOPEVAL")).intValue()) {
                            cheminImage = cheque[0].getPathimage() + File.separator + cheque[0].getFichierimage();
//                            new File(cheminImage + "f.jpg").delete();
//                            new File(cheminImage + "r.jpg").delete();
//                            new File(cheminImage + "d.txt").delete();
                            db.insertObjectAsRowByQuery(cheque[0], "CHEQUES_SUPP");
                        }
                        //Gestion du dernier cheque de la remise
                        //verifions la position du cheque
                        int index = 0;
                        String sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheque[0].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAREMSTO") + "," + Utility.getParam("CETAREMSAI") + ")";
                        Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                        if (remises != null && remises.length > 0) {
                            sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheque[0].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPESTO") + "," + Utility.getParam("CETAOPESAI") + ")  ORDER BY SEQUENCE";
                            Cheques[] chequesCal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                            if (chequesCal != null && chequesCal.length > 0) {
                                for (int i = 0; i < chequesCal.length; i++) {
                                    if (chequesCal[i].getIdcheque().equals(cheque[0].getIdcheque())) {
                                        index = i + 1;
                                    }
                                }

                                if (chequesCal.length > 1 && index == chequesCal.length) {
                                    //Dernier cheque, il faut donc inserer l'avant dernier dans Sequences
                                    Cheques avdCheque = chequesCal[index - 2];
                                    Sequences sequences = new Sequences();
                                    sequences.setTypedocument("CHEQUE");
                                    sequences.setMachinescan(avdCheque.getMachinescan());
                                    sequences.setIdsequence(avdCheque.getSequence());
                                    sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                                    sequences.setCodeline("___________");
                                    db.insertObjectAsRowByQuery(sequences, "SEQUENCES_CHEQUES");
                                    //mettre a jour avdCheque pour dire qu'il n'est pas encore saisi
                                    avdCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPESTO")));
                                    db.updateRowByObjectByQuery(avdCheque, "CHEQUES", "IDCHEQUE=" + avdCheque.getIdcheque());

                                } else if (chequesCal.length == 1 && index == chequesCal.length) {
                                    //Dernier cheque sans avant dernier, donc suppression de la remise elle-même
                                    db.executeUpdate("DELETE FROM REMISES WHERE IDREMISE=" + remises[0].getIdremise());

                                }
                                // Mettre a jour le nbOperation dans la remise
                                if (chequesCal.length > 1) {
                                    remises[0].setNbOperation(new BigDecimal(chequesCal.length - 1));
                                    db.updateRowByObjectByQuery(remises[0], "REMISES", "IDREMISE=" + remises[0].getIdremise());
                                }
                            }
                        }

                    }
                    if (cheque != null && cheque.length > 0) {
                        if (cheque[0].getEtat().intValue() < new Integer(Utility.getParam("CETAOPEVAL")).intValue()) {
                            db.executeUpdate("DELETE FROM CHEQUES WHERE IDCHEQUE=" + idObjet);
                        } else {
                            System.out.println("Document cheque non supprime " + idObjet);
                        }
                    }
                }
                db.close();
                return printMessage("Document(s) supprime(s) avec succes");
            }
            db.close();
        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            db.close();
        }
        return printMessage("Erreur de suppression -" + db.getMessage());

    }

    public String corrigeRemiseUBAGuinee(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemiseCorr");
            System.out.println("currentRemise" + currentRemise.toString());
            currentRemise.setEscompte(new BigDecimal(request.getParameter("escompte")));
            currentRemise.setMontant(request.getParameter("montantRemise").replaceAll("\\p{javaWhitespace}+", ""));
            currentRemise.setDevise("GNF");
            currentRemise.setNbOperation(new BigDecimal(request.getParameter("nmourem")));
            currentRemise.setCompteRemettant(request.getParameter("numero"));
            currentRemise.setDateSaisie(Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "yyyy/MM/dd"));
            currentRemise.setReference(request.getParameter("reference").equals("00") ? CMPUtility.getDate().substring(2) : request.getParameter("reference"));
            currentRemise.setAgenceRemettant(request.getParameter("agence"));
            currentRemise.setNomClient(request.getParameter("nom"));
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentRemise.setNomUtilisateur(user.getLogin().trim());
            currentRemise.setValideur("");
            currentRemise.setAgenceDepot(user.getAdresse().trim());
            currentRemise.setRemarques(request.getParameter("remarques"));
            currentRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPECOR")));

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentRemise, "REMISES", "IDREMISE =" + currentRemise.getIdremise());

            if (updateRowByObjectByQuery) {

                db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "' WHERE REMISE=" + currentRemise.getIdremise());

                System.out.println("Here corrigeRemiseUBAGuinee");
                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("REMISE NON OK-" + db.getMessage());

    }

    //
    public String corrigeRemise(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemiseCorr");
            currentRemise.setEscompte(new BigDecimal(request.getParameter("escompte")));
            currentRemise.setMontant(request.getParameter("montantRemise").replaceAll("\\p{javaWhitespace}+", ""));
            currentRemise.setDevise("XOF");
            currentRemise.setNbOperation(new BigDecimal(request.getParameter("nmourem")));
            currentRemise.setCompteRemettant(request.getParameter("numero"));
            currentRemise.setDateSaisie(Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "yyyy/MM/dd"));
            currentRemise.setReference(request.getParameter("reference").equals("00") ? CMPUtility.getDate().substring(2) : request.getParameter("reference"));
            currentRemise.setAgenceRemettant(request.getParameter("agence"));
            currentRemise.setNomClient(request.getParameter("nom"));
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentRemise.setNomUtilisateur(user.getLogin().trim());
            currentRemise.setValideur("");
            currentRemise.setAgenceDepot(user.getAdresse().trim());
            currentRemise.setRemarques(request.getParameter("remarques"));
            currentRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPECOR")));

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentRemise, "REMISES", "IDREMISE =" + currentRemise.getIdremise());

            if (updateRowByObjectByQuery) {

                db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "' WHERE REMISE=" + currentRemise.getIdremise());

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("REMISE NON OK-" + db.getMessage());

    }
    
    
    //same
    //corrigeRemiseEffets
    //page de correction
    public String corrigeRemiseEffets(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            RemisesEffets currentRemise = (RemisesEffets) request.getSession().getAttribute("currentRemiseCorr");
            currentRemise.setEscompte(new BigDecimal(request.getParameter("escompte")));
            currentRemise.setMontant(request.getParameter("montantRemise").replaceAll("\\p{javaWhitespace}+", ""));

            currentRemise.setDevise("XOF");
            currentRemise.setNbOperation(new BigDecimal(request.getParameter("nmourem")));
            currentRemise.setCompteRemettant(request.getParameter("numero"));
            currentRemise.setDateSaisie(Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "yyyy/MM/dd"));
            currentRemise.setReference(request.getParameter("reference").equals("00") ? CMPUtility.getDate().substring(2) : request.getParameter("reference"));
            currentRemise.setAgenceRemettant(request.getParameter("agence"));
            currentRemise.setNomClient(request.getParameter("nom"));
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentRemise.setNomUtilisateur(user.getLogin().trim());
            currentRemise.setValideur("");
            currentRemise.setAgenceDepot(user.getAdresse().trim());
            currentRemise.setRemarques(request.getParameter("remarques"));
            currentRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPECOR")));

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
           
            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentRemise, "REMISESEFFETS", "IDREMISE =" + currentRemise.getIdremise());

            if (updateRowByObjectByQuery) {

                db.executeUpdate("UPDATE EFFETS SET NUMEROCOMPTE_BENEFICIAIRE='" + currentRemise.getCompteRemettant() + "' WHERE REMISE=" + currentRemise.getIdremise());

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("REMISE NON OK-" + db.getMessage());

    }

    public String valideRemiseLite(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemiseCorr");
            // currentRemise.setEscompte(new BigDecimal(request.getParameter("escompte")));
            currentRemise.setMontant(request.getParameter("montantRemise").replaceAll("\\p{javaWhitespace}+", ""));
            currentRemise.setDevise("XOF");
            currentRemise.setNbOperation(new BigDecimal(request.getParameter("nmourem")));
            currentRemise.setCompteRemettant(request.getParameter("numero"));
            //currentRemise.setDateSaisie(CMPUtility.getDate());
            currentRemise.setReference(request.getParameter("reference").equals("00") ? CMPUtility.getDate().substring(2) : request.getParameter("reference"));
            currentRemise.setAgenceRemettant(request.getParameter("agence"));
            currentRemise.setNomClient(request.getParameter("nom"));
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentRemise.setValideur(user.getLogin().trim());
            currentRemise.setAgenceDepot(user.getAdresse().trim());
            currentRemise.setRemarques(request.getParameter("remarques"));
            currentRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL2")));

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentRemise, "REMISES", "IDREMISE =" + currentRemise.getIdremise());

            if (updateRowByObjectByQuery) {

                db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "' WHERE REMISE=" + currentRemise.getIdremise());

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("REMISE NON OK-" + db.getMessage());

    }

    public String valideRemiseLiteRNC(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemiseCorr");
            // currentRemise.setEscompte(new BigDecimal(request.getParameter("escompte")));
            currentRemise.setMontant(request.getParameter("montantRemise").replaceAll("\\p{javaWhitespace}+", ""));
            currentRemise.setDevise("XOF");
            currentRemise.setNbOperation(new BigDecimal(request.getParameter("nmourem")));
            currentRemise.setCompteRemettant(request.getParameter("numero"));
            //currentRemise.setDateSaisie(CMPUtility.getDate());
            currentRemise.setReference(request.getParameter("reference").equals("00") ? CMPUtility.getDate().substring(2) : request.getParameter("reference"));
            currentRemise.setAgenceRemettant(request.getParameter("agence"));
            currentRemise.setNomClient(request.getParameter("nom"));
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentRemise.setValideur(user.getLogin().trim());
            currentRemise.setAgenceDepot(user.getAdresse().trim());
            currentRemise.setRemarques(request.getParameter("remarques"));
            currentRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALDELTA2")));

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentRemise, "REMISES", "IDREMISE =" + currentRemise.getIdremise());

            if (updateRowByObjectByQuery) {

                db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "' WHERE REMISE=" + currentRemise.getIdremise());

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("REMISE NON OK-" + db.getMessage());

    }

    public String valideRemiseCtrlSignETG(HttpServletRequest request, HttpServletResponse response) {

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemiseCorr");
            // currentRemise.setEscompte(new BigDecimal(request.getParameter("escompte")));
            currentRemise.setMontant(request.getParameter("montantRemise").replaceAll("\\p{javaWhitespace}+", ""));
            currentRemise.setDevise("XOF");
            currentRemise.setNbOperation(new BigDecimal(request.getParameter("nmourem")));
            currentRemise.setCompteRemettant(request.getParameter("numero"));
            //currentRemise.setDateSaisie(CMPUtility.getDate());
            currentRemise.setReference(request.getParameter("reference").equals("00") ? CMPUtility.getDate().substring(2) : request.getParameter("reference"));
            currentRemise.setAgenceRemettant(request.getParameter("agence"));
            currentRemise.setNomClient(request.getParameter("nom"));
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentRemise.setValideur(currentRemise.getValideur() + "+" + user.getLogin().trim());
            // currentRemise.setAgenceDepot(user.getAdresse().trim());
            currentRemise.setRemarques(request.getParameter("remarques"));
            currentRemise.setEtat(new BigDecimal(Utility.getParam("CETAREMSIGENC"))); //55

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentRemise, "REMISES", "IDREMISE =" + currentRemise.getIdremise());

            if (updateRowByObjectByQuery) {

                db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "' WHERE REMISE=" + currentRemise.getIdremise());

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("REMISE NON OK-" + db.getMessage());

    }

    public String valideRemiseCtrlSignECI(HttpServletRequest request, HttpServletResponse response) {

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemiseCorr");
            // currentRemise.setEscompte(new BigDecimal(request.getParameter("escompte")));
            currentRemise.setMontant(request.getParameter("montantRemise").replaceAll("\\p{javaWhitespace}+", ""));
            currentRemise.setDevise("XOF");
            currentRemise.setNbOperation(new BigDecimal(request.getParameter("nmourem")));
            currentRemise.setCompteRemettant(request.getParameter("numero"));
            //currentRemise.setDateSaisie(CMPUtility.getDate());
            currentRemise.setReference(request.getParameter("reference").equals("00") ? CMPUtility.getDate().substring(2) : request.getParameter("reference"));
            currentRemise.setAgenceRemettant(request.getParameter("agence"));
            currentRemise.setNomClient(request.getParameter("nom"));
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentRemise.setNomUtilisateur(currentRemise.getNomUtilisateur() + "+" + user.getLogin().trim());
            // currentRemise.setAgenceDepot(user.getAdresse().trim());
            currentRemise.setRemarques(request.getParameter("remarques"));
            currentRemise.setLotAgence(new BigDecimal(Utility.getParam("CETAREMSIGENC"))); //55

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentRemise, "REMISES", "IDREMISE =" + currentRemise.getIdremise());

            if (updateRowByObjectByQuery) {

                db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "' WHERE REMISE=" + currentRemise.getIdremise());

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("REMISE NON OK-" + db.getMessage());

    }

    public String valideRemiseLiteCorporates(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemiseCorr");
            // currentRemise.setEscompte(new BigDecimal(request.getParameter("escompte")));
            currentRemise.setMontant(request.getParameter("montantRemise").replaceAll("\\p{javaWhitespace}+", ""));
            currentRemise.setDevise("XOF");
            currentRemise.setNbOperation(new BigDecimal(request.getParameter("nmourem")));
            currentRemise.setCompteRemettant(request.getParameter("numero"));
            //currentRemise.setDateSaisie(CMPUtility.getDate());
            currentRemise.setReference(request.getParameter("reference").equals("00") ? CMPUtility.getDate().substring(2) : request.getParameter("reference"));
            currentRemise.setAgenceRemettant(request.getParameter("agence"));
            currentRemise.setNomClient(request.getParameter("nom"));
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentRemise.setValideur(user.getLogin().trim());
            currentRemise.setAgenceDepot(user.getAdresse().trim());
            currentRemise.setRemarques(request.getParameter("remarques"));
            currentRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALITE2")));

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentRemise, "REMISES", "IDREMISE =" + currentRemise.getIdremise());

            if (updateRowByObjectByQuery) {

                db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "' WHERE REMISE=" + currentRemise.getIdremise());

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("REMISE NON OK-" + db.getMessage());

    }

    public String valideChequeLiteCorporate(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemiseCorr");
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentChequeCorr");
            System.out.println("currentCheque" + currentCheque.getIdcheque());
            currentCheque.setMontantcheque(request.getParameter("montantCheque").replaceAll("\\p{javaWhitespace}+", ""));
            currentCheque.setBanque(request.getParameter("codeBanque").toUpperCase(Locale.ENGLISH));
            currentCheque.setAgence(Utility.bourrageGZero(request.getParameter("codeAgence"), 5));
            currentCheque.setNumerocompte(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
            currentCheque.setNumerocheque(Utility.bourrageGZero(request.getParameter("numeroCheque"), 7));
            currentCheque.setRibcompte(Utility.bourrageGZero(request.getParameter("clerib"), 2));
            currentCheque.setNomemetteur(request.getParameter("nomTire"));
            currentCheque.setDateemission(request.getParameter("dateEmis"));

            if (currentCheque.getMontantcheque() == null || currentCheque.getMontantcheque().trim().equals("")) {
                return printMessage(" Le Montant du cheque doit être renseigne");
            }
            if (!currentCheque.getMontantcheque().matches("[0-9]+")) {
                return printMessage(" Le Montant du cheque ne doit contenir que des chiffres");
            }

            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("")) {
                return printMessage(" Le Numero du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() == null || currentCheque.getBanque().trim().equals("")) {
                return printMessage(" La Banque du cheque doit être renseigne");
            }
            if (currentCheque.getAgence() == null || currentCheque.getAgence().trim().equals("")) {
                return printMessage(" L'Agence du cheque doit être renseigne");
            }
            if (currentCheque.getNumerocompte() == null || currentCheque.getNumerocompte().trim().equals("")) {
                return printMessage(" Le Numero de compte doit être renseigne");
            }
            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(currentCheque.getNumerocheque().trim())) {
                return printMessage(" Le Numero de cheque doit être renseigne par un entier");
            }
            if (currentCheque.getRibcompte() == null || currentCheque.getRibcompte().trim().equals("")) {
                return printMessage(" La cle RIB du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() != null && Character.isDigit(currentCheque.getBanque().charAt(1))) {

                return printMessage(" Le Code Banque de ce cheque est au format SICA 2.\n Il ne sera pas accepte.");
            }

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            //CETAOPEVAL CETAVALITE

            String sql = "SELECT * FROM ALL_CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEVALITE") + "," + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + "," + Utility.getParam("CETAOPEVALITE2") + "," + Utility.getParam("CETAOPEVALSURCAIENVSIB") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND BANQUE='" + currentCheque.getBanque() + "' AND AGENCE='" + currentCheque.getAgence() + "' AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "'";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                if (!cheques[0].getIdcheque().equals(currentCheque.getIdcheque())) {
                    db.close();
                    return printMessage("Ce cheque a deja ete valide et n'a pas encore ete rejete.\n"
                            + "Il reference le cheque avec IDCHEQUE = " + cheques[0].getIdcheque() + " a l'etat " + cheques[0].getEtat());
                }
            }

            HashMap<BigDecimal, String> hashMap = new HashMap(currentRemise.getNbOperation().intValue());
            sql = "SELECT * FROM CHEQUES WHERE ETAT >= " + Utility.getParam("CETAOPEVALITE") + "  AND REMISE=" + currentRemise.getIdremise();
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                for (int i = 0; i < cheques.length; i++) {
                    if (!cheques[i].getSequence().equals(currentCheque.getSequence())) {
                        hashMap.put(cheques[i].getSequence(), cheques[i].getMontantcheque().trim());
                    }
                }
            }

            hashMap.put(currentCheque.getSequence(), currentCheque.getMontantcheque().trim());

            int index = Integer.parseInt(request.getParameter("index"));

            currentCheque.setDevise("XOF");
            if (Character.isDigit(currentCheque.getBanque().charAt(1))) {
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    currentCheque.setEtablissement(CMPUtility.getCodeBanque());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanque());
                } else {
                    currentCheque.setEtablissement(currentRemise.getEtablissement());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                }
                currentCheque.setType_Cheque("030");
            } else {
                currentCheque.setEtablissement(currentRemise.getEtablissement());
                currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                currentCheque.setType_Cheque("035");
            }
            currentCheque.setAgenceremettant(currentRemise.getAgenceRemettant());
            currentCheque.setCompteremettant(currentRemise.getCompteRemettant());
            currentCheque.setRefremise(currentRemise.getReference());
            currentCheque.setEscompte(currentRemise.getEscompte());
            currentCheque.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setHeuresaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HH:mm:ss"));
            currentCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDateemission(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDevise("XOF");
            currentCheque.setNombeneficiaire(currentRemise.getNomClient());

            sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '" + currentCheque.getBanque() + "' AND CODEAGENCE LIKE '" + currentCheque.getAgence() + "'";
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                currentCheque.setVille(agences[0].getCodevillecompense());
            } else {
                //currentCheque.setVille("01");
                db.close();
                return printMessage("Le code Agence " + currentCheque.getAgence() + " n'est pas declare dans la base");
            }
            currentCheque.setVilleremettant("01");
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");

            currentCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
            currentCheque.setIndicateurmodificationcmc7(new BigDecimal(0));
            currentCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALITE2")));

            if (index > currentRemise.getNbOperation().intValue()) {
                db.close();
                return printMessage("Nombre d'operations de la Remise =" + currentRemise.getNbOperation()
                        + "\n inferieur au Nombre des cheques =" + index);
            }

            String cleribCal = Utility.computeCleRIB(currentCheque.getBanque(), currentCheque.getAgence(), currentCheque.getNumerocompte());
            if (!cleribCal.equals(currentCheque.getRibcompte())) {
                db.close();
                return printMessage("La Banque " + currentCheque.getBanque() + "\nL'Agence " + currentCheque.getAgence() + "\nLe Compte " + currentCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
            }

            long sumAmount = 0;

            String array[] = hashMap.values().toArray(new String[0]);
            for (int i = 0; i < array.length; i++) {
                sumAmount += Long.parseLong(array[i]);
            }

            if (sumAmount > Long.parseLong(currentRemise.getMontant().trim())) {
                db.close();
                return printMessage("Montant de la Remise =" + Utility.formatNumber(currentRemise.getMontant()) + " inferieur \nau montant courant de la somme des cheques =" + Utility.formatNumber("" + sumAmount));
            }

            if (index == currentRemise.getNbOperation().intValue()) {
                if (sumAmount < Long.parseLong(currentRemise.getMontant().trim())) {
                    db.close();
                    return printMessage("Montant de la Remise =" + Utility.formatNumber(currentRemise.getMontant()) + " superieur \nau montant de la somme des cheques =" + Utility.formatNumber("" + sumAmount));
                }

            }

            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentCheque, "CHEQUES", "IDCHEQUE =" + currentCheque.getIdcheque());

            if (updateRowByObjectByQuery) {

                if (index == currentRemise.getNbOperation().intValue()) {
                    if (db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', ETAT=" + Utility.getParam("CETAOPEVAL") + " WHERE ETAT=" + Utility.getParam("CETAOPEVALITE2") + " AND REMISE=" + currentRemise.getIdremise()) == currentRemise.getNbOperation().intValue()) {
                        if (db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEVAL") + " WHERE IDREMISE=" + currentRemise.getIdremise()) == 1) {

                            db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEVAL") + " WHERE IDREMISE=" + currentRemise.getIdremise());
                        } else {
                            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALITE2") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                            db.close();
                            return printMessage("Erreur lors de la MAJ de le remise -" + db.getMessage());
                        }
                    } else {
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALITE2") + " WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND REMISE=" + currentRemise.getIdremise());
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALITE2") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                        db.close();
                        return printMessage("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());
                    }

                }

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);

        }
        db.close();
        return printMessage("CHEQUE NON OK-" + db.getMessage());

    }

    public String checkRemiseVierge1(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            boolean isAnInsertion = false;
            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemise");
            if (currentRemise == null) {
                currentRemise = new Remises();
                currentRemise.setIdremise(new BigDecimal(Utility.computeCompteur("IDREMISE", "REMISE")));
                isAnInsertion = true;
            }

            currentRemise.setEscompte(new BigDecimal(request.getParameter("escompte")));
            currentRemise.setMontant(request.getParameter("montantRemise").replaceAll("\\p{javaWhitespace}+", ""));
            currentRemise.setDevise("XOF");
            currentRemise.setNbOperation(new BigDecimal(request.getParameter("nmourem")));
            currentRemise.setCompteRemettant(request.getParameter("numero"));
            currentRemise.setDateSaisie(Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "yyyy/MM/dd"));
            currentRemise.setReference(request.getParameter("reference").equals("00") ? "" + currentRemise.getIdremise() : request.getParameter("reference"));
            currentRemise.setAgenceRemettant(request.getParameter("agence"));
            currentRemise.setNomClient(request.getParameter("nom"));
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentRemise.setNomUtilisateur(user.getLogin().trim());
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentCheque");
            currentRemise.setMachinescan(currentCheque.getMachinescan());
            currentRemise.setAgenceDepot(user.getAdresse().trim());
            currentRemise.setRemarques(request.getParameter("remarques"));
            currentRemise.setEtat(new BigDecimal(Utility.getParam("CETAREMSAI")));

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            if (isAnInsertion) {
                boolean insertRowByObjectByQuery = db.insertObjectAsRowByQuery(currentRemise, "REMISES");

                if (insertRowByObjectByQuery) {
                    String sequenceObjet = currentCheque.getSequence().toPlainString();
                    BigDecimal lastChqSeq = currentRemise.getNbOperation().add(currentCheque.getSequence());
                    String sql = "UPDATE CHEQUES SET REMISE=" + currentRemise.getIdremise() + ", COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', CODEUTILISATEUR='" + user.getLogin().trim() + "' WHERE (SEQUENCE>=" + sequenceObjet + "  AND SEQUENCE<" + lastChqSeq + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                    db.executeUpdate(sql);
                    sql = "UPDATE SEQUENCES_CHEQUES SET UTILISATEUR='" + user.getLogin().trim() + "' WHERE (IDSEQUENCE>=" + sequenceObjet + "  AND IDSEQUENCE<" + lastChqSeq + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                    db.executeUpdate(sql);

                    db.close();
                    return printMessage("OK");
                }

            } else {
                boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentRemise, "REMISES", "IDREMISE =" + currentRemise.getIdremise());

                if (updateRowByObjectByQuery) {

                    String sequenceObjet = currentCheque.getSequence().toPlainString();
                    BigDecimal lastChqSeq = currentRemise.getNbOperation().add(currentCheque.getSequence());
                    String sql = "UPDATE CHEQUES SET REMISE=" + currentRemise.getIdremise() + ", COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', CODEUTILISATEUR='" + user.getLogin().trim() + "' WHERE (SEQUENCE>=" + sequenceObjet + "  AND SEQUENCE<" + lastChqSeq + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                    db.executeUpdate(sql);
                    sql = "UPDATE SEQUENCES_CHEQUES SET UTILISATEUR='" + user.getLogin().trim() + "' WHERE (IDSEQUENCE>=" + sequenceObjet + "  AND IDSEQUENCE<" + lastChqSeq + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                    db.executeUpdate(sql);

                    //Gestion du cas de maj ou le nvo nmourem est inferieur a l'ancien
                    //Verifions le dernier cheque de la remise
                    Cheques[] chequesLast = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE SEQUENCE =(SELECT MAX(SEQUENCE) FROM CHEQUES WHERE REMISE=" + currentRemise.getIdremise() + " AND SEQUENCE<" + lastChqSeq + ")", new Cheques());
                    if (chequesLast != null && chequesLast.length > 0) {
                        Cheques cheques = chequesLast[0];
                        //il faut donc l inserer dans Sequences_Cheques a s'il n'y est plus.
                        sql = "SELECT * FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE =" + cheques.getSequence();
                        Sequences[] sequences_cheques = (Sequences[]) db.retrieveRowAsObject(sql, new Sequences());
                        if (sequences_cheques != null && sequences_cheques.length > 0) {
                        } else {
                            //Le cheque n'est plus dans Sequences_cheques
                            Sequences sequences = new Sequences();
                            sequences.setTypedocument("CHEQUE");
                            sequences.setMachinescan(cheques.getMachinescan());
                            sequences.setIdsequence(cheques.getSequence());
                            sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                            sequences.setCodeline("___________");
                            db.insertObjectAsRowByQuery(sequences, "SEQUENCES_CHEQUES");
                            cheques.setEtat(new BigDecimal(Utility.getParam("CETAOPESTO")));
                            db.updateRowByObjectByQuery(cheques, "CHEQUES", "IDCHEQUE=" + cheques.getIdcheque());

                        }

                    }
                    //Verifions qu'il y'a des cheques qui appartenaient a la remise
                    Cheques[] chequesOld = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE REMISE=" + currentRemise.getIdremise() + " AND SEQUENCE>=" + lastChqSeq, new Cheques());
                    if (chequesOld != null && chequesOld.length > 0) {
                        //Il y'en a
                        for (int i = 0; i < chequesOld.length; i++) {
                            Cheques cheques = chequesOld[i];

                            //il faut donc les inserer dans Sequences_Cheques avec le dernier cheque s'il n'y sont plus.
                            sql = "SELECT * FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE =" + cheques.getSequence();
                            Sequences[] sequences_cheques = (Sequences[]) db.retrieveRowAsObject(sql, new Sequences());
                            if (sequences_cheques != null && sequences_cheques.length > 0) {
                            } else {
                                //Le cheque n'est plus dans Sequences_cheques
                                Sequences sequences = new Sequences();
                                sequences.setTypedocument("CHEQUE");
                                sequences.setMachinescan(cheques.getMachinescan());
                                sequences.setIdsequence(cheques.getSequence());
                                sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                                sequences.setCodeline("___________");
                                db.insertObjectAsRowByQuery(sequences, "SEQUENCES_CHEQUES");

                            }

                        }
                        sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESTO") + ", REMISE=NULL, COMPTEREMETTANT=NULL, CODEUTILISATEUR=NULL WHERE (SEQUENCE>=" + lastChqSeq + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                        db.executeUpdate(sql);
                        sql = "UPDATE SEQUENCES_CHEQUES SET UTILISATEUR=NULL WHERE (IDSEQUENCE>=" + lastChqSeq + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                        db.executeUpdate(sql);

                    }

                    db.close();
                    return printMessage("OK");
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("REMISE NON OK-" + db.getMessage());

    }

    public String checkRemiseViergeLite(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
        Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentCheque");
        String typeCheque = (currentCheque.getBanque() != null && currentCheque.getBanque().equals(Utility.getParam("CODE_BANQUE_SICA3"))) ? "1" : "2"; //Si code bank pas bien recupéré ca marchera pas
        try {
            boolean isAnInsertion = false;
            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemise");
            if (currentRemise == null) {
                currentRemise = new Remises();
                currentRemise.setIdremise(new BigDecimal(Utility.computeCompteur("IDREMISE", "REMISE")));
                isAnInsertion = true;
            }

            currentRemise.setEscompte(new BigDecimal(request.getParameter("escompte")));
            currentRemise.setMontant(request.getParameter("montantRemise").replaceAll("\\p{javaWhitespace}+", ""));
            currentRemise.setDevise("XOF");
            currentRemise.setNbOperation(new BigDecimal(request.getParameter("nmourem")));
            currentRemise.setCompteRemettant(request.getParameter("numero"));
            currentRemise.setDateSaisie(Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "yyyy/MM/dd"));
            if (Utility.getParam("REFERENCE_CORPORATE") != null && Utility.getParam("REFERENCE_CORPORATE").equals("1")) {
                String nomCompteur = Utility.getParamOfType(user.getAdresse(), "CODE_COMPTEUR"); //ok
                String dateRemise = Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "ddMMyy"); //ok
                nomCompteur += typeCheque + Utility.bourrageGZero("" + currentRemise.getNbOperation(), 3) + dateRemise + Utility.bourrageGZero(Utility.computeCompteur("REFREMLITE", dateRemise), 3);
                currentRemise.setReference(request.getParameter("reference").equals("00") ? "" + nomCompteur : request.getParameter("reference"));
            } else {
                currentRemise.setReference(request.getParameter("reference").equals("00") ? "" + currentRemise.getIdremise() : request.getParameter("reference"));
            }

            currentRemise.setAgenceRemettant(request.getParameter("agence"));
            currentRemise.setNomClient(request.getParameter("nom"));

            currentRemise.setNomUtilisateur(user.getLogin().trim());

            currentRemise.setMachinescan(currentCheque.getMachinescan());
            currentRemise.setAgenceDepot(user.getAdresse().trim());
            currentRemise.setEtablissement(user.getAdresse().trim());
            currentRemise.setRemarques(request.getParameter("remarques"));
            currentRemise.setEtat(new BigDecimal(Utility.getParam("CETAREMSAI")));

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            if (isAnInsertion) {
                boolean insertRowByObjectByQuery = db.insertObjectAsRowByQuery(currentRemise, "REMISES");

                if (insertRowByObjectByQuery) {

                    String sequenceObjet = currentCheque.getSequence().toPlainString();
                    String sql = "UPDATE CHEQUES SET REMISE=" + currentRemise.getIdremise() + ", COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', NOMBENEFICIAIRE='" + currentRemise.getNomClient() + "', CODEUTILISATEUR='" + user.getLogin().trim() + "' WHERE SEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                    db.executeUpdate(sql);
                    sql = "UPDATE SEQUENCES_CHEQUES SET UTILISATEUR='" + user.getLogin().trim() + "' WHERE IDSEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ")";
                    db.executeUpdate(sql);

                    db.close();
                    return printMessage("OK");
                }

            } else {
                boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentRemise, "REMISES", "IDREMISE =" + currentRemise.getIdremise());

                if (updateRowByObjectByQuery) {

                    String sequenceObjet = currentCheque.getSequence().toPlainString();
                    String sql = "UPDATE CHEQUES SET REMISE=" + currentRemise.getIdremise() + ", COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', NOMBENEFICIAIRE='" + currentRemise.getNomClient() + "', CODEUTILISATEUR='" + user.getLogin().trim() + "' WHERE SEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                    db.executeUpdate(sql);
                    sql = "UPDATE SEQUENCES_CHEQUES SET UTILISATEUR='" + user.getLogin().trim() + "' WHERE IDSEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ")";
                    db.executeUpdate(sql);

                    //Gestion du cas de maj ou le nvo nmourem est inferieur a l'ancien
                    //Verifions le dernier cheque de la remise
                    Cheques[] chequesLast = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE SEQUENCE =( SELECT MAX(SEQUENCE) FROM (SELECT * FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ")", new Cheques());
                    if (chequesLast != null && chequesLast.length > 0) {
                        Cheques cheques = chequesLast[0];
                        //il faut donc l inserer dans Sequences_Cheques a s'il n'y est plus.
                        sql = "SELECT * FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE =" + cheques.getSequence();
                        Sequences[] sequences_cheques = (Sequences[]) db.retrieveRowAsObject(sql, new Sequences());
                        if (sequences_cheques != null && sequences_cheques.length > 0) {
                        } else {
                            //Le cheque n'est plus dans Sequences_cheques
                            Sequences sequences = new Sequences();
                            sequences.setTypedocument("CHEQUE");
                            sequences.setMachinescan(cheques.getMachinescan());
                            sequences.setIdsequence(cheques.getSequence());
                            sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                            sequences.setCodeline("___________");
                            db.insertObjectAsRowByQuery(sequences, "SEQUENCES_CHEQUES");
                            cheques.setEtat(new BigDecimal(Utility.getParam("CETAOPESTO")));
                            db.updateRowByObjectByQuery(cheques, "CHEQUES", "IDCHEQUE=" + cheques.getIdcheque());

                        }

                    }
                    //Verifions qu'il y'a des cheques qui appartenaient a la remise
                    Cheques[] chequesOld = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE REMISE=" + currentRemise.getIdremise() + " AND SEQUENCE> " + chequesLast[0].getSequence(), new Cheques());
                    if (chequesOld != null && chequesOld.length > 0) {
                        //Il y'en a
                        for (int i = 0; i < chequesOld.length; i++) {
                            Cheques cheques = chequesOld[i];

                            //il faut donc les inserer dans Sequences_Cheques avec le dernier cheque s'il n'y sont plus.
                            sql = "SELECT * FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE =" + cheques.getSequence();
                            Sequences[] sequences_cheques = (Sequences[]) db.retrieveRowAsObject(sql, new Sequences());
                            if (sequences_cheques != null && sequences_cheques.length > 0) {
                            } else {
                                //Le cheque n'est plus dans Sequences_cheques
                                Sequences sequences = new Sequences();
                                sequences.setTypedocument("CHEQUE");
                                sequences.setMachinescan(cheques.getMachinescan());
                                sequences.setIdsequence(cheques.getSequence());
                                sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                                sequences.setCodeline("___________");
                                db.insertObjectAsRowByQuery(sequences, "SEQUENCES_CHEQUES");

                            }

                        }
                        sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESTO") + ", REMISE=NULL, COMPTEREMETTANT=NULL,NOMBENEFICIAIRE=NULL, CODEUTILISATEUR=NULL WHERE (SEQUENCE>" + chequesLast[0].getSequence() + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                        db.executeUpdate(sql);
                        sql = "UPDATE SEQUENCES_CHEQUES SET UTILISATEUR=NULL WHERE (IDSEQUENCE> " + chequesLast[0].getSequence() + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                        db.executeUpdate(sql);

                    }

                    db.close();
                    return printMessage("OK");
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("REMISE NON OK-" + db.getMessage());

    }

    //
    public String checkRemiseViergeUBAGuinee(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Debut checkRemiseViergeUBAGuinee");
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            boolean isAnInsertion = false;
            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemise");
            if (currentRemise == null) {
                currentRemise = new Remises();
                currentRemise.setIdremise(new BigDecimal(Utility.computeCompteur("IDREMISE", "REMISE")));
                isAnInsertion = true;
            }
            System.out.println("request.getParameter(\"nmourem\")" + request.getParameter("nmourem"));

            currentRemise.setEscompte(new BigDecimal(request.getParameter("escompte")));
            currentRemise.setMontant(request.getParameter("montantRemise").replaceAll("\\p{javaWhitespace}+", ""));
            currentRemise.setDevise("GNF");
            currentRemise.setNbOperation(new BigDecimal(request.getParameter("nmourem")));
            currentRemise.setCompteRemettant(request.getParameter("numero"));
            currentRemise.setDateSaisie(Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "yyyy/MM/dd"));
            currentRemise.setReference(request.getParameter("reference").equals("00") ? "" + currentRemise.getIdremise() : request.getParameter("reference"));
            currentRemise.setAgenceRemettant(request.getParameter("agence"));
            currentRemise.setNomClient(request.getParameter("nom"));
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentRemise.setNomUtilisateur(user.getLogin().trim());
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentCheque");
            currentRemise.setMachinescan(currentCheque.getMachinescan());
            currentRemise.setAgenceDepot(user.getAdresse().trim());
            currentRemise.setEtablissement(Utility.getParam("CODE_BANQUE"));
            currentRemise.setRemarques(request.getParameter("remarques"));
            currentRemise.setEtat(new BigDecimal(Utility.getParam("CETAREMSAI")));

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            if (isAnInsertion) {
                boolean insertRowByObjectByQuery = db.insertObjectAsRowByQuery(currentRemise, "REMISES");

                if (insertRowByObjectByQuery) {

                    String sequenceObjet = currentCheque.getSequence().toPlainString();
                    String sql = "UPDATE CHEQUES SET REMISE=" + currentRemise.getIdremise() + ", COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', NOMBENEFICIAIRE='" + currentRemise.getNomClient() + "', CODEUTILISATEUR='" + user.getLogin().trim() + "' WHERE SEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                    db.executeUpdate(sql);
                    sql = "UPDATE SEQUENCES_CHEQUES SET UTILISATEUR='" + user.getLogin().trim() + "' WHERE IDSEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ")";
                    db.executeUpdate(sql);

                    db.close();
                    return printMessage("OK");
                }

            } else {

                boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentRemise, "REMISES", "IDREMISE =" + currentRemise.getIdremise());

                if (updateRowByObjectByQuery) {

                    String sequenceObjet = currentCheque.getSequence().toPlainString();
                    String sql = "UPDATE CHEQUES SET REMISE=" + currentRemise.getIdremise() + ", COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', NOMBENEFICIAIRE='" + currentRemise.getNomClient() + "', CODEUTILISATEUR='" + user.getLogin().trim() + "' WHERE SEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                    db.executeUpdate(sql);
                    sql = "UPDATE SEQUENCES_CHEQUES SET UTILISATEUR='" + user.getLogin().trim() + "' WHERE IDSEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ")";
                    db.executeUpdate(sql);

                    //Gestion du cas de maj ou le nvo nmourem est inferieur a l'ancien
                    //Verifions le dernier cheque de la remise
                    Cheques[] chequesLast = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE SEQUENCE =( SELECT MAX(SEQUENCE) FROM (SELECT * FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ")", new Cheques());
                    if (chequesLast != null && chequesLast.length > 0) {
                        Cheques cheques = chequesLast[0];
                        //il faut donc l inserer dans Sequences_Cheques a s'il n'y est plus.
                        sql = "SELECT * FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE =" + cheques.getSequence();
                        Sequences[] sequences_cheques = (Sequences[]) db.retrieveRowAsObject(sql, new Sequences());
                        if (sequences_cheques != null && sequences_cheques.length > 0) {
                        } else {
                            //Le cheque n'est plus dans Sequences_cheques
                            Sequences sequences = new Sequences();
                            sequences.setTypedocument("CHEQUE");
                            sequences.setMachinescan(cheques.getMachinescan());
                            sequences.setIdsequence(cheques.getSequence());
                            sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                            sequences.setCodeline("___________");
                            db.insertObjectAsRowByQuery(sequences, "SEQUENCES_CHEQUES");
                            cheques.setEtat(new BigDecimal(Utility.getParam("CETAOPESTO")));
                            db.updateRowByObjectByQuery(cheques, "CHEQUES", "IDCHEQUE=" + cheques.getIdcheque());

                        }

                    }
                    //Verifions qu'il y'a des cheques qui appartenaient a la remise
                    Cheques[] chequesOld = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE REMISE=" + currentRemise.getIdremise() + " AND SEQUENCE> " + chequesLast[0].getSequence(), new Cheques());
                    if (chequesOld != null && chequesOld.length > 0) {
                        //Il y'en a
                        for (int i = 0; i < chequesOld.length; i++) {
                            Cheques cheques = chequesOld[i];

                            //il faut donc les inserer dans Sequences_Cheques avec le dernier cheque s'il n'y sont plus.
                            sql = "SELECT * FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE =" + cheques.getSequence();
                            Sequences[] sequences_cheques = (Sequences[]) db.retrieveRowAsObject(sql, new Sequences());
                            if (sequences_cheques != null && sequences_cheques.length > 0) {
                            } else {
                                //Le cheque n'est plus dans Sequences_cheques
                                Sequences sequences = new Sequences();
                                sequences.setTypedocument("CHEQUE");
                                sequences.setMachinescan(cheques.getMachinescan());
                                sequences.setIdsequence(cheques.getSequence());
                                sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                                sequences.setCodeline("___________");
                                db.insertObjectAsRowByQuery(sequences, "SEQUENCES_CHEQUES");

                            }

                        }
                        sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESTO") + ", REMISE=NULL, COMPTEREMETTANT=NULL,NOMBENEFICIAIRE=NULL, CODEUTILISATEUR=NULL WHERE (SEQUENCE>" + chequesLast[0].getSequence() + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                        db.executeUpdate(sql);
                        sql = "UPDATE SEQUENCES_CHEQUES SET UTILISATEUR=NULL WHERE (IDSEQUENCE> " + chequesLast[0].getSequence() + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                        db.executeUpdate(sql);

                    }

                    db.close();
                    return printMessage("OK");
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        System.out.println("Fin checkRemiseViergeUBAGuinee");
        return printMessage("REMISE NON OK-" + db.getMessage());

    }

    public String checkRemiseVierge(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
        try {
            boolean isAnInsertion = false;
            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemise");
            if (currentRemise == null) {
                currentRemise = new Remises();
                currentRemise.setIdremise(new BigDecimal(Utility.computeCompteur("IDREMISE", "REMISE")));
                isAnInsertion = true;
            }
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentCheque");
            String typeCheque = (currentCheque.getBanque() != null && currentCheque.getBanque().equals(Utility.getParam("CODE_BANQUE_SICA3"))) ? "1" : "2";

            currentRemise.setEscompte(new BigDecimal(request.getParameter("escompte")));
            currentRemise.setMontant(request.getParameter("montantRemise").replaceAll("\\p{javaWhitespace}+", ""));
            currentRemise.setDevise("XOF");
            currentRemise.setNbOperation(new BigDecimal(request.getParameter("nmourem")));
            currentRemise.setCompteRemettant(request.getParameter("numero"));
            currentRemise.setDateSaisie(Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "yyyy/MM/dd"));
////            if (Utility.getParam("REFERENCE_CORPORATE") != null && Utility.getParam("REFERENCE_CORPORATE").equals("1")) {
////                String nomCompteur = Utility.getParamOfType(user.getAdresse(), "CODE_COMPTEUR");
////                String dateRemise = Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "ddMMyy");
////                nomCompteur += Utility.bourrageGZero("" + currentRemise.getNbOperation(), 3) + dateRemise + Utility.bourrageGZero(Utility.computeCompteur("REFERENCE", nomCompteur), 4);
////                currentRemise.setReference(request.getParameter("reference").equals("00") ? "" + nomCompteur : request.getParameter("reference"));
////            } else {
////                currentRemise.setReference(request.getParameter("reference").equals("00") ? "" + currentRemise.getIdremise() : request.getParameter("reference"));
////            }

            if (Utility.getParam("REFERENCE_CORPORATE") != null && Utility.getParam("REFERENCE_CORPORATE").equals("1")) {
                String nomCompteur = Utility.getParamOfType(user.getAdresse(), "CODE_COMPTEUR"); //ok
                String dateRemise = Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "ddMMyy"); //ok
                nomCompteur += typeCheque + Utility.bourrageGZero("" + currentRemise.getNbOperation(), 3) + dateRemise + Utility.bourrageGZero(Utility.computeCompteur("REFREMLITE", dateRemise), 3);
                currentRemise.setReference(request.getParameter("reference").equals("00") ? "" + nomCompteur : request.getParameter("reference"));
            } else {
                currentRemise.setReference(request.getParameter("reference").equals("00") ? "" + currentRemise.getIdremise() : request.getParameter("reference"));
            }
            currentRemise.setAgenceRemettant(request.getParameter("agence"));
            currentRemise.setNomClient(request.getParameter("nom"));
            currentRemise.setNomUtilisateur(user.getLogin().trim());
            currentRemise.setMachinescan(currentCheque.getMachinescan());
            currentRemise.setAgenceDepot(user.getAdresse().trim());
            currentRemise.setEtablissement(user.getAdresse().trim());
            currentRemise.setRemarques(request.getParameter("remarques"));
            currentRemise.setEtat(new BigDecimal(Utility.getParam("CETAREMSAI")));

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            if (isAnInsertion) {
                boolean insertRowByObjectByQuery = db.insertObjectAsRowByQuery(currentRemise, "REMISES");

                if (insertRowByObjectByQuery) {

                    String sequenceObjet = currentCheque.getSequence().toPlainString();
                    String sql = "UPDATE CHEQUES SET REMISE=" + currentRemise.getIdremise() + ", COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', NOMBENEFICIAIRE='" + currentRemise.getNomClient() + "', CODEUTILISATEUR='" + user.getLogin().trim() + "' WHERE SEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                    db.executeUpdate(sql);
                    sql = "UPDATE SEQUENCES_CHEQUES SET UTILISATEUR='" + user.getLogin().trim() + "' WHERE IDSEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ")";
                    db.executeUpdate(sql);

                    db.close();
                    return printMessage("OK");
                }

            } else {
                boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentRemise, "REMISES", "IDREMISE =" + currentRemise.getIdremise());

                if (updateRowByObjectByQuery) {

                    String sequenceObjet = currentCheque.getSequence().toPlainString();
                    String sql = "UPDATE CHEQUES SET REMISE=" + currentRemise.getIdremise() + ", COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', NOMBENEFICIAIRE='" + currentRemise.getNomClient() + "', CODEUTILISATEUR='" + user.getLogin().trim() + "' WHERE SEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                    db.executeUpdate(sql);
                    sql = "UPDATE SEQUENCES_CHEQUES SET UTILISATEUR='" + user.getLogin().trim() + "' WHERE IDSEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ")";
                    db.executeUpdate(sql);

                    //Gestion du cas de maj ou le nvo nmourem est inferieur a l'ancien
                    //Verifions le dernier cheque de la remise
                    Cheques[] chequesLast = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE SEQUENCE =( SELECT MAX(SEQUENCE) FROM (SELECT * FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ")", new Cheques());
                    if (chequesLast != null && chequesLast.length > 0) {
                        Cheques cheques = chequesLast[0];
                        //il faut donc l inserer dans Sequences_Cheques a s'il n'y est plus.
                        sql = "SELECT * FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE =" + cheques.getSequence();
                        Sequences[] sequences_cheques = (Sequences[]) db.retrieveRowAsObject(sql, new Sequences());
                        if (sequences_cheques != null && sequences_cheques.length > 0) {
                        } else {
                            //Le cheque n'est plus dans Sequences_cheques
                            Sequences sequences = new Sequences();
                            sequences.setTypedocument("CHEQUE");
                            sequences.setMachinescan(cheques.getMachinescan());
                            sequences.setIdsequence(cheques.getSequence());
                            sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                            sequences.setCodeline("___________");
                            db.insertObjectAsRowByQuery(sequences, "SEQUENCES_CHEQUES");
                            cheques.setEtat(new BigDecimal(Utility.getParam("CETAOPESTO")));
                            db.updateRowByObjectByQuery(cheques, "CHEQUES", "IDCHEQUE=" + cheques.getIdcheque());

                        }

                    }
                    //Verifions qu'il y'a des cheques qui appartenaient a la remise
                    Cheques[] chequesOld = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE REMISE=" + currentRemise.getIdremise() + " AND SEQUENCE> " + chequesLast[0].getSequence(), new Cheques());
                    if (chequesOld != null && chequesOld.length > 0) {
                        //Il y'en a
                        for (int i = 0; i < chequesOld.length; i++) {
                            Cheques cheques = chequesOld[i];

                            //il faut donc les inserer dans Sequences_Cheques avec le dernier cheque s'il n'y sont plus.
                            sql = "SELECT * FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE =" + cheques.getSequence();
                            Sequences[] sequences_cheques = (Sequences[]) db.retrieveRowAsObject(sql, new Sequences());
                            if (sequences_cheques != null && sequences_cheques.length > 0) {
                            } else {
                                //Le cheque n'est plus dans Sequences_cheques
                                Sequences sequences = new Sequences();
                                sequences.setTypedocument("CHEQUE");
                                sequences.setMachinescan(cheques.getMachinescan());
                                sequences.setIdsequence(cheques.getSequence());
                                sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                                sequences.setCodeline("___________");
                                db.insertObjectAsRowByQuery(sequences, "SEQUENCES_CHEQUES");

                            }

                        }
                        sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESTO") + ", REMISE=NULL, COMPTEREMETTANT=NULL,NOMBENEFICIAIRE=NULL, CODEUTILISATEUR=NULL WHERE (SEQUENCE>" + chequesLast[0].getSequence() + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                        db.executeUpdate(sql);
                        sql = "UPDATE SEQUENCES_CHEQUES SET UTILISATEUR=NULL WHERE (IDSEQUENCE> " + chequesLast[0].getSequence() + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                        db.executeUpdate(sql);

                    }

                    db.close();
                    return printMessage("OK");
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("REMISE NON OK-" + db.getMessage());

    }
    
    
     public String checkRemiseViergeEffetsType(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");

        try {
            boolean isAnInsertion = false;
            RemisesEffets currentRemise = (RemisesEffets) request.getSession().getAttribute("currentRemise");
            if (currentRemise == null) {
                currentRemise = new RemisesEffets();
                currentRemise.setIdremise(new BigDecimal(Utility.computeCompteur("IDREMISEEFFETS", "REMISESEFFETS")));
                isAnInsertion = true;
            }
            Effets currentEffet = (Effets) request.getSession().getAttribute("currentEffet");
            String typeCheque = (currentEffet.getBanque() != null && currentEffet.getBanque().equals(Utility.getParam("CODE_BANQUE_SICA3"))) ? "1" : "2";

            currentRemise.setEscompte(new BigDecimal(request.getParameter("escompte")));
            currentRemise.setMontant(request.getParameter("montantRemise").replaceAll("\\p{javaWhitespace}+", ""));
            currentRemise.setDevise("XOF");
            currentRemise.setNbOperation(new BigDecimal(request.getParameter("nmourem")));
            currentRemise.setCompteRemettant(request.getParameter("numero"));
            currentRemise.setDateSaisie(Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "yyyy/MM/dd"));
////            if (Utility.getParam("REFERENCE_CORPORATE") != null && Utility.getParam("REFERENCE_CORPORATE").equals("1")) {
////                String nomCompteur = Utility.getParamOfType(user.getAdresse(), "CODE_COMPTEUR");
////                String dateRemise = Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "ddMMyy");
////                nomCompteur += Utility.bourrageGZero("" + currentRemise.getNbOperation(), 3) + dateRemise + Utility.bourrageGZero(Utility.computeCompteur("REFERENCE", nomCompteur), 4);
////                currentRemise.setReference(request.getParameter("reference").equals("00") ? "" + nomCompteur : request.getParameter("reference"));
////            } else {
////                currentRemise.setReference(request.getParameter("reference").equals("00") ? "" + currentRemise.getIdremise() : request.getParameter("reference"));
////            }

            currentRemise.setTypeRemise(request.getParameter("typeEffet"));
             
            if (Utility.getParam("REFERENCE_CORPORATE") != null && Utility.getParam("REFERENCE_CORPORATE").equals("1")) {
                String nomCompteur = Utility.getParamOfType(user.getAdresse(), "CODE_COMPTEUR"); //ok
                String dateRemise = Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "ddMMyy"); //ok
                nomCompteur += typeCheque + Utility.bourrageGZero("" + currentRemise.getNbOperation(), 3) + dateRemise + Utility.bourrageGZero(Utility.computeCompteur("REFREMLITE", dateRemise), 3);
                currentRemise.setReference(request.getParameter("reference").equals("00") ? "" + nomCompteur : request.getParameter("reference"));
            } else {
                currentRemise.setReference(request.getParameter("reference").equals("00") ? "" + currentRemise.getIdremise() : request.getParameter("reference"));
            }
            currentRemise.setAgenceRemettant(request.getParameter("agence"));
            currentRemise.setNomClient(request.getParameter("nom"));
            currentRemise.setNomUtilisateur(user.getLogin().trim());
            currentRemise.setMachinescan(currentEffet.getMachinescan());
            currentRemise.setAgenceDepot(user.getAdresse().trim());
            currentRemise.setEtablissement(user.getAdresse().trim());
            currentRemise.setRemarques(request.getParameter("remarques"));
            currentRemise.setEtat(new BigDecimal(Utility.getParam("CETAREMSAI")));

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            if (isAnInsertion) {
                boolean insertRowByObjectByQuery = db.insertObjectAsRowByQuery(currentRemise, "REMISESEFFETS");

                if (insertRowByObjectByQuery) {

                    String sequenceObjet = currentEffet.getSequence().toPlainString();
                    String sql = "UPDATE EFFETS SET REMISE=" + currentRemise.getIdremise() + ", NUMEROCOMPTE_BENEFICIAIRE='" + currentRemise.getCompteRemettant() + "', NOM_BENEFICIAIRE='" + currentRemise.getNomClient() + "', CODEUTILISATEUR='" + user.getLogin().trim() + "' WHERE SEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM EFFETS WHERE MACHINESCAN='" + currentEffet.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ") AND MACHINESCAN='" + currentEffet.getMachinescan() + "'";
                    db.executeUpdate(sql);
                    sql = "UPDATE SEQUENCES_EFFETS SET UTILISATEUR='" + user.getLogin().trim() + "' WHERE IDSEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM EFFETS WHERE MACHINESCAN='" + currentEffet.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ")";
                    db.executeUpdate(sql);

                    db.close();
                    return printMessage("OK");
                }

            } else {
                boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentRemise, "REMISESEFFETS", "IDREMISE =" + currentRemise.getIdremise());

                if (updateRowByObjectByQuery) {

                    String sequenceObjet = currentEffet.getSequence().toPlainString();
                    String sql = "UPDATE EFFETS SET REMISE=" + currentRemise.getIdremise() + ", NUMEROCOMPTE_BENEFICIAIRE='" + currentRemise.getCompteRemettant() + "', NOM_BENEFICIAIRE='" + currentRemise.getNomClient() + "', CODEUTILISATEUR='" + user.getLogin().trim() + "' WHERE SEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM EFFETS WHERE MACHINESCAN='" + currentEffet.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ") AND MACHINESCAN='" + currentEffet.getMachinescan() + "'";
                    db.executeUpdate(sql);
                    sql = "UPDATE SEQUENCES_CHEQUES SET UTILISATEUR='" + user.getLogin().trim() + "' WHERE IDSEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM EFFETS WHERE MACHINESCAN='" + currentEffet.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ")";
                    db.executeUpdate(sql);

                    //Gestion du cas de maj ou le nvo nmourem est inferieur a l'ancien
                    //Verifions le dernier cheque de la remise
                    Effets[] effetsLast = (Effets[]) db.retrieveRowAsObject("SELECT * FROM EFFETS WHERE SEQUENCE =( SELECT MAX(SEQUENCE) FROM (SELECT * FROM EFFETS WHERE MACHINESCAN='" + currentEffet.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ")", new Effets());
                    if (effetsLast != null && effetsLast.length > 0) {
                        Effets effets = effetsLast[0];
                        //il faut donc l inserer dans Sequences_Effets a s'il n'y est plus.
                        sql = "SELECT * FROM SEQUENCES_EFFETS WHERE IDSEQUENCE =" + effets.getSequence();
                        Sequences[] sequences_effets = (Sequences[]) db.retrieveRowAsObject(sql, new Sequences());
                        if (sequences_effets != null && sequences_effets.length > 0) {
                        } else {
                            //Le cheque n'est plus dans Sequences_cheques
                            Sequences sequences = new Sequences();
                            sequences.setTypedocument("EFFETS");
                            sequences.setMachinescan(effets.getMachinescan());
                            sequences.setIdsequence(effets.getSequence());
                            sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                            sequences.setCodeline("___________");
                            db.insertObjectAsRowByQuery(sequences, "SEQUENCES_EFFETS");
                            effets.setEtat(new BigDecimal(Utility.getParam("CETAOPESTO")));
                            db.updateRowByObjectByQuery(effets, "EFFETS", "IDEFFET=" + effets.getIdeffet());

                        }

                    }
                    //Verifions qu'il y'a des cheques qui appartenaient a la remise
                    Effets[] effetsOld = (Effets[]) db.retrieveRowAsObject("SELECT * FROM EFFETS WHERE REMISE=" + currentRemise.getIdremise() + " AND SEQUENCE> " + effetsLast[0].getSequence(), new Effets());
                    if (effetsOld != null && effetsOld.length > 0) {
                        //Il y'en a
                        for (int i = 0; i < effetsOld.length; i++) {
                            Effets effet = effetsOld[i];

                            //il faut donc les inserer dans Sequences_Cheques avec le dernier cheque s'il n'y sont plus.
                            sql = "SELECT * FROM SEQUENCES_EFFETS WHERE IDSEQUENCE =" + effet.getSequence();
                            Sequences[] sequences_effets = (Sequences[]) db.retrieveRowAsObject(sql, new Sequences());
                            if (sequences_effets != null && sequences_effets.length > 0) {
                            } else {
                                //Le cheque n'est plus dans Sequences_cheques
                                Sequences sequences = new Sequences();
                                sequences.setTypedocument("EFFET");
                                sequences.setMachinescan(effet.getMachinescan());
                                sequences.setIdsequence(effet.getSequence());
                                sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                                sequences.setCodeline("___________");
                                db.insertObjectAsRowByQuery(sequences, "SEQUENCES_EFFETS");

                            }

                        }
                        sql = "UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPESTO") + ", REMISE=NULL, NUMEROCOMPTE_BENEFICIAIRE=NULL,NOM_BENEFICIAIRE=NULL, CODEUTILISATEUR=NULL WHERE (SEQUENCE>" + effetsLast[0].getSequence() + ") AND MACHINESCAN='" + currentEffet.getMachinescan() + "'";
                        db.executeUpdate(sql);
                        sql = "UPDATE SEQUENCES_EFFETS SET UTILISATEUR=NULL WHERE (IDSEQUENCE> " + effetsLast[0].getSequence() + ") AND MACHINESCAN='" + currentEffet.getMachinescan() + "'";
                        db.executeUpdate(sql);

                    }

                    db.close();
                    return printMessage("OK");
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("REMISE EFFETS NON OK-" + db.getMessage());

    }

     
     
    public String checkRemiseViergeEffets(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");

        try {
            boolean isAnInsertion = false;
            RemisesEffets currentRemise = (RemisesEffets) request.getSession().getAttribute("currentRemise");
            if (currentRemise == null) {
                currentRemise = new RemisesEffets();
                currentRemise.setIdremise(new BigDecimal(Utility.computeCompteur("IDREMISEEFFETS", "REMISESEFFETS")));
                isAnInsertion = true;
            }
            Effets currentEffet = (Effets) request.getSession().getAttribute("currentEffet");
            String typeCheque = (currentEffet.getBanque() != null && currentEffet.getBanque().equals(Utility.getParam("CODE_BANQUE_SICA3"))) ? "1" : "2";

            currentRemise.setEscompte(new BigDecimal(request.getParameter("escompte")));
            currentRemise.setMontant(request.getParameter("montantRemise").replaceAll("\\p{javaWhitespace}+", ""));
            currentRemise.setDevise("XOF");
            currentRemise.setNbOperation(new BigDecimal(request.getParameter("nmourem")));
            currentRemise.setCompteRemettant(request.getParameter("numero"));
            currentRemise.setDateSaisie(Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "yyyy/MM/dd"));
////            if (Utility.getParam("REFERENCE_CORPORATE") != null && Utility.getParam("REFERENCE_CORPORATE").equals("1")) {
////                String nomCompteur = Utility.getParamOfType(user.getAdresse(), "CODE_COMPTEUR");
////                String dateRemise = Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "ddMMyy");
////                nomCompteur += Utility.bourrageGZero("" + currentRemise.getNbOperation(), 3) + dateRemise + Utility.bourrageGZero(Utility.computeCompteur("REFERENCE", nomCompteur), 4);
////                currentRemise.setReference(request.getParameter("reference").equals("00") ? "" + nomCompteur : request.getParameter("reference"));
////            } else {
////                currentRemise.setReference(request.getParameter("reference").equals("00") ? "" + currentRemise.getIdremise() : request.getParameter("reference"));
////            }

            if (Utility.getParam("REFERENCE_CORPORATE") != null && Utility.getParam("REFERENCE_CORPORATE").equals("1")) {
                String nomCompteur = Utility.getParamOfType(user.getAdresse(), "CODE_COMPTEUR"); //ok
                String dateRemise = Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "ddMMyy"); //ok
                nomCompteur += typeCheque + Utility.bourrageGZero("" + currentRemise.getNbOperation(), 3) + dateRemise + Utility.bourrageGZero(Utility.computeCompteur("REFREMLITE", dateRemise), 3);
                currentRemise.setReference(request.getParameter("reference").equals("00") ? "" + nomCompteur : request.getParameter("reference"));
            } else {
                currentRemise.setReference(request.getParameter("reference").equals("00") ? "" + currentRemise.getIdremise() : request.getParameter("reference"));
            }
            currentRemise.setAgenceRemettant(request.getParameter("agence"));
            currentRemise.setNomClient(request.getParameter("nom"));
            currentRemise.setNomUtilisateur(user.getLogin().trim());
            currentRemise.setMachinescan(currentEffet.getMachinescan());
            currentRemise.setAgenceDepot(user.getAdresse().trim());
            currentRemise.setEtablissement(user.getAdresse().trim());
            currentRemise.setRemarques(request.getParameter("remarques"));
            currentRemise.setEtat(new BigDecimal(Utility.getParam("CETAREMSAI")));

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            if (isAnInsertion) {
                boolean insertRowByObjectByQuery = db.insertObjectAsRowByQuery(currentRemise, "REMISESEFFETS");

                if (insertRowByObjectByQuery) {

                    String sequenceObjet = currentEffet.getSequence().toPlainString();
                    String sql = "UPDATE EFFETS SET REMISE=" + currentRemise.getIdremise() + ", NUMEROCOMPTE_BENEFICIAIRE='" + currentRemise.getCompteRemettant() + "', NOM_BENEFICIAIRE='" + currentRemise.getNomClient() + "', CODEUTILISATEUR='" + user.getLogin().trim() + "' WHERE SEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM EFFETS WHERE MACHINESCAN='" + currentEffet.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ") AND MACHINESCAN='" + currentEffet.getMachinescan() + "'";
                    db.executeUpdate(sql);
                    sql = "UPDATE SEQUENCES_EFFETS SET UTILISATEUR='" + user.getLogin().trim() + "' WHERE IDSEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM EFFETS WHERE MACHINESCAN='" + currentEffet.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ")";
                    db.executeUpdate(sql);

                    db.close();
                    return printMessage("OK");
                }

            } else {
                boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentRemise, "REMISESEFFETS", "IDREMISE =" + currentRemise.getIdremise());

                if (updateRowByObjectByQuery) {

                    String sequenceObjet = currentEffet.getSequence().toPlainString();
                    String sql = "UPDATE EFFETS SET REMISE=" + currentRemise.getIdremise() + ", NUMEROCOMPTE_BENEFICIAIRE='" + currentRemise.getCompteRemettant() + "', NOM_BENEFICIAIRE='" + currentRemise.getNomClient() + "', CODEUTILISATEUR='" + user.getLogin().trim() + "' WHERE SEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM EFFETS WHERE MACHINESCAN='" + currentEffet.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ") AND MACHINESCAN='" + currentEffet.getMachinescan() + "'";
                    db.executeUpdate(sql);
                    sql = "UPDATE SEQUENCES_CHEQUES SET UTILISATEUR='" + user.getLogin().trim() + "' WHERE IDSEQUENCE IN ( SELECT SEQUENCE FROM (SELECT * FROM EFFETS WHERE MACHINESCAN='" + currentEffet.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ")";
                    db.executeUpdate(sql);

                    //Gestion du cas de maj ou le nvo nmourem est inferieur a l'ancien
                    //Verifions le dernier cheque de la remise
                    Effets[] effetsLast = (Effets[]) db.retrieveRowAsObject("SELECT * FROM EFFETS WHERE SEQUENCE =( SELECT MAX(SEQUENCE) FROM (SELECT * FROM EFFETS WHERE MACHINESCAN='" + currentEffet.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) WHERE rownum <=" + currentRemise.getNbOperation() + ")", new Effets());
                    if (effetsLast != null && effetsLast.length > 0) {
                        Effets effets = effetsLast[0];
                        //il faut donc l inserer dans Sequences_Effets a s'il n'y est plus.
                        sql = "SELECT * FROM SEQUENCES_EFFETS WHERE IDSEQUENCE =" + effets.getSequence();
                        Sequences[] sequences_effets = (Sequences[]) db.retrieveRowAsObject(sql, new Sequences());
                        if (sequences_effets != null && sequences_effets.length > 0) {
                        } else {
                            //Le cheque n'est plus dans Sequences_cheques
                            Sequences sequences = new Sequences();
                            sequences.setTypedocument("EFFETS");
                            sequences.setMachinescan(effets.getMachinescan());
                            sequences.setIdsequence(effets.getSequence());
                            sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                            sequences.setCodeline("___________");
                            db.insertObjectAsRowByQuery(sequences, "SEQUENCES_EFFETS");
                            effets.setEtat(new BigDecimal(Utility.getParam("CETAOPESTO")));
                            db.updateRowByObjectByQuery(effets, "EFFETS", "IDEFFET=" + effets.getIdeffet());

                        }

                    }
                    //Verifions qu'il y'a des cheques qui appartenaient a la remise
                    Effets[] effetsOld = (Effets[]) db.retrieveRowAsObject("SELECT * FROM EFFETS WHERE REMISE=" + currentRemise.getIdremise() + " AND SEQUENCE> " + effetsLast[0].getSequence(), new Effets());
                    if (effetsOld != null && effetsOld.length > 0) {
                        //Il y'en a
                        for (int i = 0; i < effetsOld.length; i++) {
                            Effets effet = effetsOld[i];

                            //il faut donc les inserer dans Sequences_Cheques avec le dernier cheque s'il n'y sont plus.
                            sql = "SELECT * FROM SEQUENCES_EFFETS WHERE IDSEQUENCE =" + effet.getSequence();
                            Sequences[] sequences_effets = (Sequences[]) db.retrieveRowAsObject(sql, new Sequences());
                            if (sequences_effets != null && sequences_effets.length > 0) {
                            } else {
                                //Le cheque n'est plus dans Sequences_cheques
                                Sequences sequences = new Sequences();
                                sequences.setTypedocument("EFFET");
                                sequences.setMachinescan(effet.getMachinescan());
                                sequences.setIdsequence(effet.getSequence());
                                sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                                sequences.setCodeline("___________");
                                db.insertObjectAsRowByQuery(sequences, "SEQUENCES_EFFETS");

                            }

                        }
                        sql = "UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPESTO") + ", REMISE=NULL, NUMEROCOMPTE_BENEFICIAIRE=NULL,NOM_BENEFICIAIRE=NULL, CODEUTILISATEUR=NULL WHERE (SEQUENCE>" + effetsLast[0].getSequence() + ") AND MACHINESCAN='" + currentEffet.getMachinescan() + "'";
                        db.executeUpdate(sql);
                        sql = "UPDATE SEQUENCES_EFFETS SET UTILISATEUR=NULL WHERE (IDSEQUENCE> " + effetsLast[0].getSequence() + ") AND MACHINESCAN='" + currentEffet.getMachinescan() + "'";
                        db.executeUpdate(sql);

                    }

                    db.close();
                    return printMessage("OK");
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("REMISE EFFETS  NON OK-" + db.getMessage());

    }

     
    public String checkRemiseViergeMSSQL(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            boolean isAnInsertion = false;
            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemise");
            if (currentRemise == null) {
                currentRemise = new Remises();
                currentRemise.setIdremise(new BigDecimal(Utility.computeCompteur("IDREMISE", "REMISE")));
                isAnInsertion = true;
            }

            currentRemise.setEscompte(new BigDecimal(request.getParameter("escompte")));
            currentRemise.setMontant(request.getParameter("montantRemise").replaceAll("\\p{javaWhitespace}+", ""));
            currentRemise.setDevise("XOF");
            currentRemise.setNbOperation(new BigDecimal(request.getParameter("nmourem")));
            currentRemise.setCompteRemettant(request.getParameter("numero"));
            currentRemise.setDateSaisie(Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "yyyy/MM/dd"));
            currentRemise.setReference(request.getParameter("reference").equals("00") ? "" + currentRemise.getIdremise() : request.getParameter("reference"));
            currentRemise.setAgenceRemettant(request.getParameter("agence"));
            currentRemise.setNomClient(request.getParameter("nom"));

            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentRemise.setNomUtilisateur(user.getLogin().trim());
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentCheque");
            currentRemise.setMachinescan(currentCheque.getMachinescan());
            currentRemise.setAgenceDepot(user.getAdresse().trim());
            currentRemise.setEtablissement(user.getAdresse().trim());
            currentRemise.setRemarques(request.getParameter("remarques"));
            currentRemise.setEtat(new BigDecimal(Utility.getParam("CETAREMSAI")));

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            if (isAnInsertion) {
                boolean insertRowByObjectByQuery = db.insertObjectAsRowByQuery(currentRemise, "REMISES");

                if (insertRowByObjectByQuery) {

                    String sequenceObjet = currentCheque.getSequence().toPlainString();
                    String sql = "UPDATE CHEQUES SET REMISE=" + currentRemise.getIdremise() + ", COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', NOMBENEFICIAIRE='" + currentRemise.getNomClient() + "', CODEUTILISATEUR='" + user.getLogin().trim() + "' WHERE SEQUENCE IN ( SELECT TOP " + currentRemise.getNbOperation() + " SEQUENCE FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                    db.executeUpdate(sql);
                    sql = "UPDATE SEQUENCES_CHEQUES SET UTILISATEUR='" + user.getLogin().trim() + "' WHERE IDSEQUENCE IN ( SELECT TOP " + currentRemise.getNbOperation() + " SEQUENCE FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE)";
                    db.executeUpdate(sql);

                    db.close();
                    return printMessage("OK");
                }

            } else {
                boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentRemise, "REMISES", "IDREMISE =" + currentRemise.getIdremise());

                if (updateRowByObjectByQuery) {

                    String sequenceObjet = currentCheque.getSequence().toPlainString();
                    String sql = "UPDATE CHEQUES SET REMISE=" + currentRemise.getIdremise() + ", COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', NOMBENEFICIAIRE='" + currentRemise.getNomClient() + "', CODEUTILISATEUR='" + user.getLogin().trim() + "' WHERE SEQUENCE IN ( SELECT TOP " + currentRemise.getNbOperation() + " SEQUENCE FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                    db.executeUpdate(sql);
                    sql = "UPDATE SEQUENCES_CHEQUES SET UTILISATEUR='" + user.getLogin().trim() + "' WHERE IDSEQUENCE IN ( SELECT TOP " + currentRemise.getNbOperation() + " SEQUENCE FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE)";
                    db.executeUpdate(sql);

                    //Gestion du cas de maj ou le nvo nmourem est inferieur a l'ancien
                    //Verifions le dernier cheque de la remise
                    Cheques[] chequesLast = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE SEQUENCE =( SELECT MAX(SEQUENCE) FROM ( SELECT TOP " + currentRemise.getNbOperation() + " SEQUENCE FROM CHEQUES WHERE MACHINESCAN='" + currentCheque.getMachinescan() + "' AND SEQUENCE>=" + sequenceObjet + " ORDER BY SEQUENCE) A)", new Cheques());
                    if (chequesLast != null && chequesLast.length > 0) {
                        Cheques cheques = chequesLast[0];
                        //il faut donc l inserer dans Sequences_Cheques a s'il n'y est plus.
                        sql = "SELECT * FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE =" + cheques.getSequence();
                        Sequences[] sequences_cheques = (Sequences[]) db.retrieveRowAsObject(sql, new Sequences());
                        if (sequences_cheques != null && sequences_cheques.length > 0) {
                        } else {
                            //Le cheque n'est plus dans Sequences_cheques
                            Sequences sequences = new Sequences();
                            sequences.setTypedocument("CHEQUE");
                            sequences.setMachinescan(cheques.getMachinescan());
                            sequences.setIdsequence(cheques.getSequence());
                            sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                            sequences.setCodeline("___________");
                            db.insertObjectAsRowByQuery(sequences, "SEQUENCES_CHEQUES");
                            cheques.setEtat(new BigDecimal(Utility.getParam("CETAOPESTO")));
                            db.updateRowByObjectByQuery(cheques, "CHEQUES", "IDCHEQUE=" + cheques.getIdcheque());

                        }

                    }
                    //Verifions qu'il y'a des cheques qui appartenaient a la remise
                    Cheques[] chequesOld = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE REMISE=" + currentRemise.getIdremise() + " AND SEQUENCE> " + chequesLast[0].getSequence(), new Cheques());
                    if (chequesOld != null && chequesOld.length > 0) {
                        //Il y'en a
                        for (int i = 0; i < chequesOld.length; i++) {
                            Cheques cheques = chequesOld[i];

                            //il faut donc les inserer dans Sequences_Cheques avec le dernier cheque s'il n'y sont plus.
                            sql = "SELECT * FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE =" + cheques.getSequence();
                            Sequences[] sequences_cheques = (Sequences[]) db.retrieveRowAsObject(sql, new Sequences());
                            if (sequences_cheques != null && sequences_cheques.length > 0) {
                            } else {
                                //Le cheque n'est plus dans Sequences_cheques
                                Sequences sequences = new Sequences();
                                sequences.setTypedocument("CHEQUE");
                                sequences.setMachinescan(cheques.getMachinescan());
                                sequences.setIdsequence(cheques.getSequence());
                                sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                                sequences.setCodeline("___________");
                                db.insertObjectAsRowByQuery(sequences, "SEQUENCES_CHEQUES");

                            }

                        }
                        sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESTO") + ", REMISE=NULL, COMPTEREMETTANT=NULL,NOMBENEFICIAIRE=NULL, CODEUTILISATEUR=NULL WHERE (SEQUENCE>" + chequesLast[0].getSequence() + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                        db.executeUpdate(sql);
                        sql = "UPDATE SEQUENCES_CHEQUES SET UTILISATEUR=NULL WHERE (IDSEQUENCE> " + chequesLast[0].getSequence() + ") AND MACHINESCAN='" + currentCheque.getMachinescan() + "'";
                        db.executeUpdate(sql);

                    }

                    db.close();
                    return printMessage("OK");
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("REMISE NON OK-" + db.getMessage());

    }

    public String checkRemise(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemise");
            currentRemise.setEscompte(new BigDecimal(request.getParameter("escompte")));
            currentRemise.setMontant(request.getParameter("montantRemise").replaceAll("\\p{javaWhitespace}+", ""));
            currentRemise.setDevise("XOF");
            currentRemise.setNbOperation(new BigDecimal(request.getParameter("nmourem")));
            currentRemise.setCompteRemettant(request.getParameter("numero"));
            currentRemise.setDateSaisie(Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "yyyy/MM/dd"));
            currentRemise.setReference(request.getParameter("reference").equals("00") ? CMPUtility.getDate().substring(2) : request.getParameter("reference"));
            currentRemise.setAgenceRemettant(request.getParameter("agence"));
            currentRemise.setNomClient(request.getParameter("nom"));
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentRemise.setNomUtilisateur(user.getLogin().trim());
            currentRemise.setAgenceDepot(user.getAdresse().trim());
            currentRemise.setRemarques(request.getParameter("remarques"));
            currentRemise.setEtat(new BigDecimal(Utility.getParam("CETAREMSAI")));

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentRemise, "REMISES", "IDREMISE =" + currentRemise.getIdremise());

            if (updateRowByObjectByQuery) {
                String sequenceObjet = request.getParameter("sequenceObjet");
                db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "' WHERE REMISE=" + currentRemise.getIdremise());
                if (sequenceObjet != null && !sequenceObjet.trim().equals("")) {
                    db.executeUpdate("DELETE FROM SEQUENCES WHERE IDSEQUENCE=" + currentRemise.getSequence());
                }
                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("REMISE NON OK-" + db.getMessage());

    }

    public String checkRemiseUBAGuinee(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemise");
            currentRemise.setEscompte(new BigDecimal(request.getParameter("escompte")));
            currentRemise.setMontant(request.getParameter("montantRemise").replaceAll("\\p{javaWhitespace}+", ""));
            currentRemise.setDevise("GNF"); //GNF
            currentRemise.setNbOperation(new BigDecimal(request.getParameter("nmourem")));
            currentRemise.setCompteRemettant(request.getParameter("numero"));
            currentRemise.setDateSaisie(Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "yyyy/MM/dd"));
            currentRemise.setReference(request.getParameter("reference").equals("00") ? CMPUtility.getDate().substring(2) : request.getParameter("reference"));
            currentRemise.setAgenceRemettant(request.getParameter("agence"));
            currentRemise.setNomClient(request.getParameter("nom"));
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentRemise.setNomUtilisateur(user.getLogin().trim());
            currentRemise.setAgenceDepot(user.getAdresse().trim());
            currentRemise.setRemarques(request.getParameter("remarques"));
            currentRemise.setEtat(new BigDecimal(Utility.getParam("CETAREMSAI")));

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentRemise, "REMISES", "IDREMISE =" + currentRemise.getIdremise());

            if (updateRowByObjectByQuery) {
                String sequenceObjet = request.getParameter("sequenceObjet");
                db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "' WHERE REMISE=" + currentRemise.getIdremise());
                if (sequenceObjet != null && !sequenceObjet.trim().equals("")) {
                    db.executeUpdate("DELETE FROM SEQUENCES WHERE IDSEQUENCE=" + currentRemise.getSequence());
                }
                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("REMISE NON OK-" + db.getMessage());

    }

    public String checkRemiseTresor(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemise");
            currentRemise.setEscompte(new BigDecimal(request.getParameter("escompte")));
            currentRemise.setMontant(request.getParameter("montantRemise").replaceAll("\\p{javaWhitespace}+", ""));
            currentRemise.setDevise("XOF");
            currentRemise.setNbOperation(new BigDecimal(request.getParameter("nmourem")));
            currentRemise.setCompteRemettant(request.getParameter("numero"));
            currentRemise.setDateSaisie(Utility.convertDateToString(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"), "yyyy/MM/dd"));
            currentRemise.setReference(request.getParameter("reference"));
            currentRemise.setAgenceRemettant(request.getParameter("agence"));
            currentRemise.setNomClient(request.getParameter("nom"));
            currentRemise.setNoex(new BigDecimal(request.getParameter("noex")));
            currentRemise.setIdop(new BigDecimal(request.getParameter("idop")));
            currentRemise.setCdscpta(request.getParameter("cdscpta"));
            currentRemise.setTypeinter(request.getParameter("typeinter"));

            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentRemise.setNomUtilisateur(user.getLogin().trim());
            currentRemise.setEtat(new BigDecimal(Utility.getParam("CETAREMSAI")));

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentRemise, "REMISES", "IDREMISE =" + currentRemise.getIdremise());

            if (updateRowByObjectByQuery) {
                String sequenceObjet = request.getParameter("sequenceObjet");
                if (sequenceObjet != null && !sequenceObjet.trim().equals("")) {
                    db.executeUpdate("DELETE FROM SEQUENCES WHERE IDSEQUENCE=" + currentRemise.getSequence());
                }
                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("REMISE NON OK");

    }

    public String checkChequeUV(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemise");
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentCheque");
            currentCheque.setMontantcheque(request.getParameter("montantCheque").replaceAll("\\p{javaWhitespace}+", ""));
            currentCheque.setBanque(request.getParameter("codeBanque").toUpperCase(Locale.ENGLISH));
            currentCheque.setAgence(Utility.bourrageGZero(request.getParameter("codeAgence"), 5));
            currentCheque.setNumerocompte(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
            currentCheque.setNumerocheque(Utility.bourrageGZero(request.getParameter("numeroCheque"), 7));
            currentCheque.setRibcompte(Utility.bourrageGZero(request.getParameter("clerib"), 2));
            currentCheque.setNomemetteur(request.getParameter("nomTire"));
            currentCheque.setDateemission(request.getParameter("dateEmis"));
            currentCheque.setCodeVignetteUV(request.getParameter("codeUV"));
            String numeroEndos = request.getParameter("numeroEndos");

            boolean isVignetteSaisie = numeroEndos.equalsIgnoreCase("XX");

            if (currentCheque.getMontantcheque() == null || currentCheque.getMontantcheque().trim().equals("")) {
                return printMessage(" Le Montant du cheque doit être renseigne");
            }
            if (!currentCheque.getMontantcheque().matches("[0-9]+")) {
                return printMessage(" Le Montant du cheque ne doit contenir que des chiffres");
            }

            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("")) {
                return printMessage(" Le Numero du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() == null || currentCheque.getBanque().trim().equals("")) {
                return printMessage(" La Banque du cheque doit être renseigne");
            }
            if (currentCheque.getAgence() == null || currentCheque.getAgence().trim().equals("")) {
                return printMessage(" L'Agence du cheque doit être renseigne");
            }
            if (currentCheque.getNumerocompte() == null || currentCheque.getNumerocompte().trim().equals("")) {
                return printMessage(" Le Numero de compte doit être renseigne");
            }
            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(currentCheque.getNumerocheque().trim())) {
                return printMessage(" Le Numero de cheque doit être renseigne par un entier");
            }
            if (currentCheque.getRibcompte() == null || currentCheque.getRibcompte().trim().equals("")) {
                return printMessage(" La cle RIB du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() != null && Character.isDigit(currentCheque.getBanque().charAt(1))) {

                return printMessage(" Le Code Banque de ce cheque est au format SICA 2.\n Il ne sera pas accepte.");
            }

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String sql = "SELECT * FROM ALL_CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPERETRECENVSIBVER") + "," + Utility.getParam("CETAOPERET") + "," + Utility.getParam("CETAOPERETENVSIB") + "," + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERETRECENVSIB") + "," + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEVALSURCAIENVSIB") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + "," + Utility.getParam("CETAOPEVALDELTA") + ") AND BANQUE='" + currentCheque.getBanque() + "' AND AGENCE='" + currentCheque.getAgence() + "' AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "'";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                if (!cheques[0].getIdcheque().equals(currentCheque.getIdcheque())) {
                    db.close();
                    return printMessage("Ce cheque a deja ete valide et n'a pas encore ete rejete.\n"
                            + "Il reference le cheque avec IDCHEQUE = " + cheques[0].getIdcheque() + " a l'etat " + cheques[0].getEtat() + " - " + Utility.getParamLabelOfType("" + cheques[0].getEtat(), "CODE_ETAT"));
                }
            }

            sql = "SELECT * FROM VIGNETTES WHERE CODEBANQUE='" + currentCheque.getBanque() + "' AND CODEGUICHET='" + currentCheque.getAgence() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "' AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "'";
            Vignettes[] vignettes = (Vignettes[]) db.retrieveRowAsObject(sql, new Vignettes());
            if (vignettes != null && vignettes.length > 0) {
                System.out.println("Vignette trouvee = " + vignettes[0].getCodevignette().substring(14));
                System.out.println("Vignette saisie = " + currentCheque.getCodeVignetteUV());

                if (isVignetteSaisie) {
                    if (vignettes[0].getCodevignette().substring(14).trim().equalsIgnoreCase(currentCheque.getCodeVignetteUV())) {
                        //TODO
                        //Flagguer vignette UV OK
                        vignettes[0].setEtat(new BigDecimal(Utility.getParam("CETAVIGVAL")));

                        db.updateRowByObjectByQuery(vignettes[0], "VIGNETTES", "IDVIGNETTE=" + vignettes[0].getIdvignette());
                        db.close();
                    } else {
                        vignettes[0].setEtat(new BigDecimal(Utility.getParam("CETAVIGVER")));
                        db.updateRowByObjectByQuery(vignettes[0], "VIGNETTES", "IDVIGNETTE=" + vignettes[0].getIdvignette());
                        db.close();
                        return printMessage("Le code vignette " + currentCheque.getCodeVignetteUV() + " ne correspond pas a la ligne CMC7");
                    }

                } else {
                    // Utiliser le code Endos

                    if (vignettes[0].getNumeroendos().equalsIgnoreCase(numeroEndos)) {
                        vignettes[0].setEtat(new BigDecimal(Utility.getParam("CETAVIGVAL")));
                    } else {
                        vignettes[0].setEtat(new BigDecimal(Utility.getParam("CETAVIGVER")));
                        db.close();
                        return printMessage("Le code endos " + currentCheque.getNumeroEndos() + " ne correspond pas a la ligne CMC7");

                    }
                }

            } else {
                db.close();
                return printMessage("La vignette correspondant a ce cheque n'a pas ete importe dans la base");
            }

            HashMap<BigDecimal, String> hashMap = new HashMap(currentRemise.getNbOperation().intValue());
            sql = "SELECT * FROM CHEQUES WHERE ETAT >= " + Utility.getParam("CETAOPESAI") + "  AND REMISE=" + currentRemise.getIdremise();
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                for (int i = 0; i < cheques.length; i++) {
                    if (!cheques[i].getSequence().equals(currentCheque.getSequence())) {
                        hashMap.put(cheques[i].getSequence(), cheques[i].getMontantcheque().trim());
                    }
                }
            }

            hashMap.put(currentCheque.getSequence(), currentCheque.getMontantcheque().trim());

            int index = Integer.parseInt(request.getParameter("index"));

            currentCheque.setDevise("XOF");
            if (Character.isDigit(currentCheque.getBanque().charAt(1))) {
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    currentCheque.setEtablissement(CMPUtility.getCodeBanque());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanque());
                } else {
                    currentCheque.setEtablissement(CMPUtility.getCodeBanqueSica3());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                }
                currentCheque.setType_Cheque("030");
            } else {
                currentCheque.setEtablissement(CMPUtility.getCodeBanqueSica3());
                currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                currentCheque.setType_Cheque("035");
            }
            currentCheque.setAgenceremettant(currentRemise.getAgenceRemettant());
            currentCheque.setCompteremettant(currentRemise.getCompteRemettant());
            currentCheque.setRefremise(currentRemise.getReference());
            currentCheque.setEscompte(currentRemise.getEscompte());
            currentCheque.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setHeuresaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HH:mm:ss"));
            currentCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDateemission(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDevise("XOF");
            currentCheque.setNombeneficiaire(currentRemise.getNomClient());

            sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '" + currentCheque.getBanque() + "' AND CODEAGENCE LIKE '" + currentCheque.getAgence() + "'";
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                currentCheque.setVille(agences[0].getCodevillecompense());
            } else {
                //currentCheque.setVille("01");
                db.close();
                return printMessage("Le code Agence " + currentCheque.getAgence() + " n'est pas declare dans la base");
            }
            currentCheque.setVilleremettant("01");
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentCheque.setCodeutilisateur(user.getLogin().trim());

            currentCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
            currentCheque.setIndicateurmodificationcmc7(new BigDecimal(0));
            currentCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPESAI")));

            if (index > currentRemise.getNbOperation().intValue()) {
                db.close();
                return printMessage("Nombre d'operations de la Remise =" + currentRemise.getNbOperation()
                        + "\n inferieur au Nombre des cheques =" + index);
            }

            String cleribCal = Utility.computeCleRIB(currentCheque.getBanque(), currentCheque.getAgence(), currentCheque.getNumerocompte());
            if (!cleribCal.equals(currentCheque.getRibcompte())) {
                db.close();
                return printMessage("La Banque " + currentCheque.getBanque() + "\nL'Agence " + currentCheque.getAgence() + "\nLe Compte " + currentCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
            }

            long sumAmount = 0;

            String array[] = hashMap.values().toArray(new String[0]);
            for (int i = 0; i < array.length; i++) {
                sumAmount += Long.parseLong(array[i]);
            }

            if (sumAmount > Long.parseLong(currentRemise.getMontant().trim())) {
                return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " inferieur \nau montant courant de la somme des cheques =" + sumAmount);
            }

            if (index == currentRemise.getNbOperation().intValue()) {
                if (sumAmount < Long.parseLong(currentRemise.getMontant().trim())) {
                    db.close();
                    return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " superieur \nau montant de la somme des cheques =" + sumAmount);
                }

            }

            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentCheque, "CHEQUES", "IDCHEQUE =" + currentCheque.getIdcheque());

            if (updateRowByObjectByQuery) {

                if (index == currentRemise.getNbOperation().intValue()) {
                    if (db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "',ETAT=" + Utility.getParam("CETAOPEVAL") + " WHERE ETAT=" + Utility.getParam("CETAOPESAI") + " AND REMISE=" + currentRemise.getIdremise()) == currentRemise.getNbOperation().intValue()) {
                        if (db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMVAL") + " WHERE IDREMISE=" + currentRemise.getIdremise()) == 1) {
                            db.executeUpdate("INSERT INTO SEQUENCES_ARCHIVES SELECT * FROM SEQUENCES WHERE IDSEQUENCE=" + currentCheque.getSequence());
                            db.executeUpdate("DELETE FROM SEQUENCES WHERE IDSEQUENCE=" + currentCheque.getSequence());
                            db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMVAL") + " WHERE IDREMISE=" + currentRemise.getIdremise());
                        } else {
                            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESTO") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                            db.close();
                            return printMessage("Erreur lors de la MAJ de le remise -" + db.getMessage());
                        }
                    } else {
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESAI") + " WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND REMISE=" + currentRemise.getIdremise());
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESTO") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                        db.close();
                        return printMessage("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());
                    }

                } else {
                    db.executeUpdate("INSERT INTO SEQUENCES_ARCHIVES SELECT * FROM SEQUENCES WHERE IDSEQUENCE=" + currentCheque.getSequence());
                    db.executeUpdate("DELETE FROM SEQUENCES WHERE IDSEQUENCE=" + currentCheque.getSequence());

                }

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);

        }
        db.close();
        return printMessage("CHEQUE NON OK-" + db.getMessage());

    }

    public String checkCheque(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("checkCheque Test");
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemise");
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentCheque");
            currentCheque.setMontantcheque(request.getParameter("montantCheque").replaceAll("\\p{javaWhitespace}+", ""));
            currentCheque.setBanque(request.getParameter("codeBanque").toUpperCase(Locale.ENGLISH));
            currentCheque.setAgence(Utility.bourrageGZero(request.getParameter("codeAgence"), 5));
            currentCheque.setNumerocompte(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
            currentCheque.setNumerocheque(Utility.bourrageGZero(request.getParameter("numeroCheque"), 7));
            currentCheque.setRibcompte(Utility.bourrageGZero(request.getParameter("clerib"), 2));
            currentCheque.setNomemetteur(request.getParameter("nomTire"));
            currentCheque.setDateemission(request.getParameter("dateEmis"));
            String forcage = request.getParameter("forcage");

            if (currentCheque.getMontantcheque() == null || currentCheque.getMontantcheque().trim().equals("")) {
                return printMessage(" Le Montant du cheque doit être renseigne");
            }
            if (!currentCheque.getMontantcheque().matches("[0-9]+")) {
                return printMessage(" Le Montant du cheque ne doit contenir que des chiffres");
            }

            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("")) {
                return printMessage(" Le Numero du cheque doit être renseigne");
            }
            if (!currentCheque.getNumerocheque().matches("[0-9]+")) {
                return printMessage(" Le Numero du cheque ne doit contenir que des chiffres");
            }
            if (Integer.parseInt(currentCheque.getNumerocheque()) == 0) {
                return printMessage(" Le Numero du cheque ne doit pas être egale a 0000000");
            }
            if (currentCheque.getBanque() == null || currentCheque.getBanque().trim().equals("")) {
                return printMessage(" La Banque du cheque doit être renseigne");
            }
            if (currentCheque.getAgence() == null || currentCheque.getAgence().trim().equals("")) {
                return printMessage(" L'Agence du cheque doit être renseigne");
            }
            if (!currentCheque.getAgence().matches("[0-9]+")) {
                return printMessage(" L'Agence du cheque ne doit contenir que des chiffres");
            }
            if (currentCheque.getNumerocompte() == null || currentCheque.getNumerocompte().trim().equals("")) {
                return printMessage(" Le Numero de compte doit être renseigne");
            }
            if (!currentCheque.getNumerocompte().matches("[0-9]+")) {
                return printMessage(" Le Numero de compte ne doit contenir que des chiffres");
            }
            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(currentCheque.getNumerocheque().trim())) {
                return printMessage(" Le Numero de cheque doit être renseigne par un entier");
            }
            if (currentCheque.getRibcompte() == null || currentCheque.getRibcompte().trim().equals("")) {
                return printMessage(" La cle RIB du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() != null && Character.isDigit(currentCheque.getBanque().charAt(1))) {

                return printMessage(" Le Code Banque de ce cheque est au format SICA 2.\n Il ne sera pas accepte.");
            }

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String sql = "SELECT * FROM ALL_CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPERETRECENVSIBVER") + "," + Utility.getParam("CETAOPERET") + "," + Utility.getParam("CETAOPERETENVSIB") + "," + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERETRECENVSIB") + "," + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + "," + Utility.getParam("CETAOPEVAL2") + "," + Utility.getParam("CETAOPEVALSURCAIENVSIB") + "," + Utility.getParam("CETAOPESUPVALSURCAI") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + "," + Utility.getParam("CETAOPEVALDELTA") + ") AND BANQUE='" + currentCheque.getBanque() + "' AND AGENCE='" + currentCheque.getAgence() + "' AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "'";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            currentCheque.setCoderepresentation(BigDecimal.ZERO);
            if (cheques != null && cheques.length > 0) {
                if (forcage != null && forcage.equals("ON")) {
                    currentCheque.setCoderepresentation(new BigDecimal(cheques.length));
                } else {
                    if (!cheques[0].getIdcheque().equals(currentCheque.getIdcheque())) {
                        db.close();
                        return printMessage("Ce cheque a deja ete valide et n'a pas encore ete rejete.\n"
                                + "Il reference le cheque avec IDCHEQUE = " + cheques[0].getIdcheque() + " a l'etat " + cheques[0].getEtat() + " - " + Utility.getParamLabelOfType("" + cheques[0].getEtat(), "CODE_ETAT"));
                    }
                }
            }
            HashMap<BigDecimal, String> hashMap = new HashMap(currentRemise.getNbOperation().intValue());
            sql = "SELECT * FROM CHEQUES WHERE ETAT >= " + Utility.getParam("CETAOPESAI") + "  AND REMISE=" + currentRemise.getIdremise();
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                for (int i = 0; i < cheques.length; i++) {
                    if (!cheques[i].getSequence().equals(currentCheque.getSequence())) {
                        hashMap.put(cheques[i].getSequence(), cheques[i].getMontantcheque().trim());
                    }
                }
            }

            hashMap.put(currentCheque.getSequence(), currentCheque.getMontantcheque().trim());

            int index = Integer.parseInt(request.getParameter("index"));

            currentCheque.setDevise("XOF");
            if (Character.isDigit(currentCheque.getBanque().charAt(1))) {
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    currentCheque.setEtablissement(CMPUtility.getCodeBanque());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanque());
                } else {
                    currentCheque.setEtablissement(CMPUtility.getCodeBanqueSica3());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                }
                currentCheque.setType_Cheque("030");
            } else {
                currentCheque.setEtablissement(CMPUtility.getCodeBanqueSica3());
                currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                currentCheque.setType_Cheque("035");
            }
            currentCheque.setAgenceremettant(currentRemise.getAgenceRemettant());
            currentCheque.setCompteremettant(currentRemise.getCompteRemettant());
            currentCheque.setRefremise(currentRemise.getReference());
            currentCheque.setEscompte(currentRemise.getEscompte());
            currentCheque.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setHeuresaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HH:mm:ss"));
            currentCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDateemission(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDevise("XOF");
            currentCheque.setNombeneficiaire(currentRemise.getNomClient());

            sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '" + currentCheque.getBanque() + "' AND CODEAGENCE LIKE '" + currentCheque.getAgence() + "'";
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                currentCheque.setVille(agences[0].getCodevillecompense());
            } else {
                //currentCheque.setVille("01");
                db.close();
                return printMessage("Le code Agence " + currentCheque.getAgence() + " n'est pas declare dans la base");
            }
            currentCheque.setVilleremettant("01");
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentCheque.setCodeutilisateur(user.getLogin().trim());

            currentCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
            currentCheque.setIndicateurmodificationcmc7(new BigDecimal(0));
            currentCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPESAI")));

            if (index > currentRemise.getNbOperation().intValue()) {
                db.close();
                return printMessage("Nombre d'operations de la Remise =" + currentRemise.getNbOperation()
                        + "\n inferieur au Nombre des cheques =" + index);
            }

            String cleribCal = Utility.computeCleRIB(currentCheque.getBanque(), currentCheque.getAgence(), currentCheque.getNumerocompte());
            if (!cleribCal.equals(currentCheque.getRibcompte())) {
                db.close();
                return printMessage("La Banque " + currentCheque.getBanque() + "\nL'Agence " + currentCheque.getAgence() + "\nLe Compte " + currentCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
            }

            long sumAmount = 0;

            String array[] = hashMap.values().toArray(new String[0]);
            for (int i = 0; i < array.length; i++) {
                sumAmount += Long.parseLong(array[i]);
            }

            if (sumAmount > Long.parseLong(currentRemise.getMontant().trim())) {
                db.close();
                return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " inferieur \nau montant courant de la somme des cheques =" + sumAmount);
            }

            if (index == currentRemise.getNbOperation().intValue()) {
                if (sumAmount < Long.parseLong(currentRemise.getMontant().trim())) {
                    db.close();
                    return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " superieur \nau montant de la somme des cheques =" + sumAmount);
                }

            }

            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentCheque, "CHEQUES", "IDCHEQUE =" + currentCheque.getIdcheque());

            if (updateRowByObjectByQuery) {
                System.out.println("checkCheque Test Here 1");

                if (index == currentRemise.getNbOperation().intValue()) {
                    System.out.println("checkCheque Test Here 2");

                    if (db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', ETAT=" + Utility.getParam("CETAOPEVAL") + " WHERE ETAT=" + Utility.getParam("CETAOPESAI") + " AND REMISE=" + currentRemise.getIdremise()) == currentRemise.getNbOperation().intValue()) {
                        if (db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMVAL") + " WHERE IDREMISE=" + currentRemise.getIdremise()) == 1) {
                            db.executeUpdate("INSERT INTO SEQUENCES_ARCHIVES SELECT * FROM SEQUENCES WHERE IDSEQUENCE=" + currentCheque.getSequence());
                            db.executeUpdate("DELETE FROM SEQUENCES WHERE IDSEQUENCE=" + currentCheque.getSequence());
                            db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMVAL") + " WHERE IDREMISE=" + currentRemise.getIdremise());
                            System.out.println("checkCheque Test Here 3");
                            if (Utility.getParam("VALIDATION_CHEQUE_AUTO") != null && Utility.getParam("VALIDATION_CHEQUE_AUTO").equalsIgnoreCase("AUTO")) {
                                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM1") + " WHERE REMISE=" + currentRemise.getIdremise());
                                db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPESUPVAL") + " WHERE IDREMISE=" + currentRemise.getIdremise());
                            }
                        } else {
                            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESTO") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                            db.close();
                            System.out.println("Erreur lors de la MAJ de la remise -" + db.getMessage());
                            return printMessage("Erreur lors de la MAJ de la remise -" + db.getMessage());
                        }
                    } else {
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESAI") + " WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND REMISE=" + currentRemise.getIdremise());
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESTO") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                        db.close();
                        System.out.println("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());
                        return printMessage("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());
                    }

                } else {
                    db.executeUpdate("INSERT INTO SEQUENCES_ARCHIVES SELECT * FROM SEQUENCES WHERE IDSEQUENCE=" + currentCheque.getSequence());
                    db.executeUpdate("DELETE FROM SEQUENCES WHERE IDSEQUENCE=" + currentCheque.getSequence());

                }

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);

        }
        db.close();
        System.out.println("CHEQUE NON OK-" + db.getMessage());
        return printMessage("CHEQUE NON OK-" + db.getMessage());

    }

    public String checkChequeUBAGuinee(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemise");
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentCheque");
            currentCheque.setMontantcheque(request.getParameter("montantCheque").replaceAll("\\p{javaWhitespace}+", ""));
            currentCheque.setBanque(request.getParameter("codeBanque").toUpperCase(Locale.ENGLISH));
            currentCheque.setAgence(Utility.bourrageGZero(request.getParameter("codeAgence"), 5));
            currentCheque.setNumerocompte(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
            currentCheque.setNumerocheque(Utility.bourrageGZero(request.getParameter("numeroCheque"), 7));
            currentCheque.setRibcompte(Utility.bourrageGZero(request.getParameter("clerib"), 2));
            currentCheque.setNomemetteur(request.getParameter("nomTire"));
            currentCheque.setDateemission(request.getParameter("dateEmis"));
            String forcage = request.getParameter("forcage");

            if (currentCheque.getMontantcheque() == null || currentCheque.getMontantcheque().trim().equals("")) {
                return printMessage(" Le Montant du cheque doit être renseigne");
            }
            if (!currentCheque.getMontantcheque().matches("[0-9]+")) {
                return printMessage(" Le Montant du cheque ne doit contenir que des chiffres");
            }

            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("")) {
                return printMessage(" Le Numero du cheque doit être renseigne");
            }
            if (!currentCheque.getNumerocheque().matches("[0-9]+")) {
                return printMessage(" Le Numero du cheque ne doit contenir que des chiffres");
            }
            if (Integer.parseInt(currentCheque.getNumerocheque()) == 0) {
                return printMessage(" Le Numero du cheque ne doit pas être egale a 0000000");
            }
            if (currentCheque.getBanque() == null || currentCheque.getBanque().trim().equals("")) {
                return printMessage(" La Banque du cheque doit être renseigne");
            }
            if (currentCheque.getAgence() == null || currentCheque.getAgence().trim().equals("")) {
                return printMessage(" L'Agence du cheque doit être renseigne");
            }
            if (!currentCheque.getAgence().matches("[0-9]+")) {
                return printMessage(" L'Agence du cheque ne doit contenir que des chiffres");
            }
            if (currentCheque.getNumerocompte() == null || currentCheque.getNumerocompte().trim().equals("")) {
                return printMessage(" Le Numero de compte doit être renseigne");
            }
            if (!currentCheque.getNumerocompte().matches("[0-9]+")) {
                return printMessage(" Le Numero de compte ne doit contenir que des chiffres");
            }
            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(currentCheque.getNumerocheque().trim())) {
                return printMessage(" Le Numero de cheque doit être renseigne par un entier");
            }
            if (currentCheque.getRibcompte() == null || currentCheque.getRibcompte().trim().equals("")) {
                return printMessage(" La cle RIB du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() != null && Character.isDigit(currentCheque.getBanque().charAt(1))) {

                return printMessage(" Le Code Banque de ce cheque est au format SICA 2.\n Il ne sera pas accepte.");
            }

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String sql = "SELECT * FROM ALL_CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPERETRECENVSIBVER") + ","
                    + " " + Utility.getParam("CETAOPERET") + "," + Utility.getParam("CETAOPERETENVSIB") + ","
                    + " " + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERETRECENVSIB") + ","
                    + " " + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + ","
                    + " " + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEALLICOM1") + ","
                    + " " + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + ","
                    + " " + Utility.getParam("CETAOPEVAL2") + "," + Utility.getParam("CETAOPEVALSURCAIENVSIB") + ","
                    + " " + Utility.getParam("CETAOPESUPVALSURCAI") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ","
                    + " " + Utility.getParam("CETAOPEVALDELTA") + ") AND BANQUE='" + currentCheque.getBanque() + "' "
                    + " AND AGENCE='" + currentCheque.getAgence() + "' AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "'";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            currentCheque.setCoderepresentation(BigDecimal.ZERO);
            if (cheques != null && cheques.length > 0) {
                if (forcage != null && forcage.equals("ON")) {
                    currentCheque.setCoderepresentation(new BigDecimal(cheques.length));
                } else {
                    if (!cheques[0].getIdcheque().equals(currentCheque.getIdcheque())) {
                        db.close();
                        return printMessage("Ce cheque a deja ete valide et n'a pas encore ete rejete.\n"
                                + "Il reference le cheque avec IDCHEQUE = " + cheques[0].getIdcheque() + " a l'etat " + cheques[0].getEtat() + " - " + Utility.getParamLabelOfType("" + cheques[0].getEtat(), "CODE_ETAT"));
                    }
                }
            }
            HashMap<BigDecimal, String> hashMap = new HashMap(currentRemise.getNbOperation().intValue());
            sql = "SELECT * FROM CHEQUES WHERE ETAT >= " + Utility.getParam("CETAOPESAI") + "  AND REMISE=" + currentRemise.getIdremise();
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                for (int i = 0; i < cheques.length; i++) {
                    if (!cheques[i].getSequence().equals(currentCheque.getSequence())) {
                        hashMap.put(cheques[i].getSequence(), cheques[i].getMontantcheque().trim());
                    }
                }
            }

            hashMap.put(currentCheque.getSequence(), currentCheque.getMontantcheque().trim());

            int index = Integer.parseInt(request.getParameter("index"));

            currentCheque.setDevise("XOF");
            if (Character.isDigit(currentCheque.getBanque().charAt(1))) {
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    currentCheque.setEtablissement(CMPUtility.getCodeBanque());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanque());
                } else {
                    currentCheque.setEtablissement(CMPUtility.getCodeBanqueSica3());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                }
                currentCheque.setType_Cheque("030");
            } else {
                currentCheque.setEtablissement(CMPUtility.getCodeBanqueSica3());
                currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                currentCheque.setType_Cheque("035");
            }
            currentCheque.setAgenceremettant(currentRemise.getAgenceRemettant());
            currentCheque.setCompteremettant(currentRemise.getCompteRemettant());
            currentCheque.setRefremise(currentRemise.getReference());
            currentCheque.setEscompte(currentRemise.getEscompte());
            currentCheque.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setHeuresaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HH:mm:ss"));
            currentCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDateemission(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDevise("XOF");
            currentCheque.setNombeneficiaire(currentRemise.getNomClient());

            sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '" + currentCheque.getBanque() + "' AND CODEAGENCE LIKE '" + currentCheque.getAgence() + "'";
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                currentCheque.setVille(agences[0].getCodevillecompense());
            } else {
                //currentCheque.setVille("01");
                db.close();
                return printMessage("Le code Agence " + currentCheque.getAgence() + " n'est pas declare dans la base");
            }
            currentCheque.setVilleremettant("01");
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentCheque.setCodeutilisateur(user.getLogin().trim());

            currentCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
            currentCheque.setIndicateurmodificationcmc7(new BigDecimal(0));
            currentCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPESAI")));

            if (index > currentRemise.getNbOperation().intValue()) {
                db.close();
                return printMessage("Nombre d'operations de la Remise =" + currentRemise.getNbOperation()
                        + "\n inferieur au Nombre des cheques =" + index);
            }

            String cleribCal = Utility.computeCleRIB(currentCheque.getBanque(), currentCheque.getAgence(), currentCheque.getNumerocompte());
            if (!cleribCal.equals(currentCheque.getRibcompte())) {
                db.close();
                return printMessage("La Banque " + currentCheque.getBanque() + "\nL'Agence " + currentCheque.getAgence() + "\nLe Compte " + currentCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
            }

            long sumAmount = 0;

            String array[] = hashMap.values().toArray(new String[0]);
            for (int i = 0; i < array.length; i++) {
                sumAmount += Long.parseLong(array[i]);
            }

            if (sumAmount > Long.parseLong(currentRemise.getMontant().trim())) {
                db.close();
                return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " inferieur \nau montant courant de la somme des cheques =" + sumAmount);
            }

            if (index == currentRemise.getNbOperation().intValue()) {
                if (sumAmount < Long.parseLong(currentRemise.getMontant().trim())) {
                    db.close();
                    return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " superieur \nau montant de la somme des cheques =" + sumAmount);
                }

            }

            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentCheque, "CHEQUES", "IDCHEQUE =" + currentCheque.getIdcheque());

            if (updateRowByObjectByQuery) {

                if (index == currentRemise.getNbOperation().intValue()) {
                    if (db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', ETAT=" + Utility.getParam("CETAOPEVAL") + " "
                            + " WHERE ETAT=" + Utility.getParam("CETAOPESAI") + " AND REMISE=" + currentRemise.getIdremise()) == currentRemise.getNbOperation().intValue()) {
                        if (db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMVAL") + " WHERE IDREMISE=" + currentRemise.getIdremise()) == 1) {
                            db.executeUpdate("INSERT INTO SEQUENCES_ARCHIVES SELECT * FROM SEQUENCES WHERE IDSEQUENCE=" + currentCheque.getSequence());
                            db.executeUpdate("DELETE FROM SEQUENCES WHERE IDSEQUENCE=" + currentCheque.getSequence());
                            db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMVAL") + " WHERE IDREMISE=" + currentRemise.getIdremise());
                        } else {
                            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESTO") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                            db.close();
                            System.out.println("Erreur lors de la MAJ de la remise -" + db.getMessage());
                            return printMessage("Erreur lors de la MAJ de la remise -" + db.getMessage());
                        }
                    } else {
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESAI") + " WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND REMISE=" + currentRemise.getIdremise());
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESTO") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                        db.close();
                        System.out.println("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());
                        return printMessage("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());
                    }

                } else {
                    db.executeUpdate("INSERT INTO SEQUENCES_ARCHIVES SELECT * FROM SEQUENCES WHERE IDSEQUENCE=" + currentCheque.getSequence());
                    db.executeUpdate("DELETE FROM SEQUENCES WHERE IDSEQUENCE=" + currentCheque.getSequence());

                }

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);

        }
        db.close();
        System.out.println("CHEQUE NON OK-" + db.getMessage());
        return printMessage("CHEQUE NON OK-" + db.getMessage());

    }

    //UBAGuinee
    public String checkChequeViergeUBAGuinee(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Debut checkChequeViergeUBAGuinee");
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemise");
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentCheque");
            currentCheque.setMontantcheque(request.getParameter("montantCheque").replaceAll("\\p{javaWhitespace}+", ""));
            currentCheque.setBanque(request.getParameter("codeBanque").toUpperCase(Locale.ENGLISH));
            currentCheque.setAgence(Utility.bourrageGZero(request.getParameter("codeAgence"), 3));
            currentCheque.setNumerocompte(Utility.bourrageGZero(request.getParameter("numeroCompte"), 10));
            currentCheque.setNumerocheque(Utility.bourrageGZero(request.getParameter("numeroCheque"), 8));
            currentCheque.setRibcompte(Utility.bourrageGZero(request.getParameter("clerib"), 2));
            currentCheque.setNomemetteur(request.getParameter("nomTire"));
            currentCheque.setDateemission(request.getParameter("dateEmis"));
            String forcage = request.getParameter("forcage");
            String escompte = request.getParameter("escompte");

            if (currentCheque.getMontantcheque() == null || currentCheque.getMontantcheque().trim().equals("")) {
                return printMessage(" Le Montant du cheque doit être renseigne");
            }
            if (!currentCheque.getMontantcheque().matches("[0-9]+")) {
                return printMessage(" Le Montant du cheque ne doit contenir que des chiffres");
            }

            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("")) {
                return printMessage(" Le Numero du cheque doit être renseigne");
            }
            if (!currentCheque.getNumerocheque().matches("[0-9]+")) {
                return printMessage(" Le Numero du cheque ne doit contenir que des chiffres");
            }
            if (currentCheque.getBanque() == null || currentCheque.getBanque().trim().equals("")) {
                return printMessage(" La Banque du cheque doit être renseignee");
            }
            if (currentCheque.getAgence() == null || currentCheque.getAgence().trim().equals("")) {
                return printMessage(" L'Agence du cheque doit être renseignee");
            }
            if (!currentCheque.getAgence().matches("[0-9]+")) {
                return printMessage(" L'Agence du cheque ne doit contenir que des chiffres");
            }
            if (currentCheque.getNumerocompte() == null || currentCheque.getNumerocompte().trim().equals("")) {
                return printMessage(" Le Numero de compte doit être renseigne");
            }
            if (!currentCheque.getNumerocompte().matches("[0-9]+")) {
                return printMessage(" Le Numero de compte ne doit contenir que des chiffres");
            }
            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(currentCheque.getNumerocheque().trim())) {
                return printMessage(" Le Numero de cheque doit être renseigne par un entier");
            }
            if (currentCheque.getRibcompte() == null || currentCheque.getRibcompte().trim().equals("")) {
                return printMessage(" La cle RIB du cheque doit être renseigne");
            }
//            if (currentCheque.getBanque() != null && Character.isDigit(currentCheque.getBanque().charAt(1))) {
//
//                return printMessage(" Le Code Banque de ce cheque est au format SICA 2.\n Il ne sera pas accepte.");
//            }
            currentCheque.setType_Cheque("30");
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String sql = "SELECT * FROM ALL_CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPERETRECENVSIBVER") + "," + Utility.getParam("CETAOPERET") + ","
                    + " " + Utility.getParam("CETAOPERETENVSIB") + "," + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERETRECENVSIB") + ","
                    + " " + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + ","
                    + " " + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + ","
                    + " " + Utility.getParam("CETAOPEALLPREICOM1") + "," + Utility.getParam("CETAOPEVAL2") + ","
                    + " " + Utility.getParam("CETAOPEVALSURCAIENVSIB") + "," + Utility.getParam("CETAOPESUPVALSURCAI") + ","
                    + "" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + "," + Utility.getParam("CETAOPEVALDELTA") + ")"
                    + "  AND BANQUE='" + currentCheque.getBanque() + "' AND AGENCE='" + currentCheque.getAgence() + "'"
                    + "  AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "'";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            currentCheque.setCoderepresentation(BigDecimal.ZERO);
            if (cheques != null && cheques.length > 0) {
                if (forcage != null && forcage.equals("ON")) {
                    currentCheque.setCoderepresentation(new BigDecimal(cheques.length));
                    //le type du cheque change dans la zone ACP/ACH
                    currentCheque.setType_Cheque("33");
                } else {

                    if (!cheques[0].getIdcheque().equals(currentCheque.getIdcheque())) {
                        db.close();
                        return printMessage("Ce cheque a deja ete valide et n'a pas encore ete rejete.\n"
                                + "Il reference le cheque avec IDCHEQUE = " + cheques[0].getIdcheque() + " a l'etat " + cheques[0].getEtat() + " - " + Utility.getParamLabelOfType("" + cheques[0].getEtat(), "CODE_ETAT"));
                    }
                }
            }

            HashMap<BigDecimal, String> hashMap = new HashMap(currentRemise.getNbOperation().intValue());
            sql = "SELECT * FROM CHEQUES WHERE ETAT >= " + Utility.getParam("CETAOPESAI") + " "
                    + " AND ETAT <= " + Utility.getParam("CETAOPEVAL") + ""
                    + " AND REMISE=" + currentRemise.getIdremise();
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                for (int i = 0; i < cheques.length; i++) {
                    if (!cheques[i].getSequence().equals(currentCheque.getSequence())) {
                        hashMap.put(cheques[i].getSequence(), cheques[i].getMontantcheque().trim());
                    }
                }
            }

            hashMap.put(currentCheque.getSequence(), currentCheque.getMontantcheque().trim());

            int index = Integer.parseInt(request.getParameter("index"));
            currentCheque.setEtablissement(Utility.getParam("CODE_BANQUE"));//currentRemise.getAgenceDepot()
            currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());

            currentCheque.setAgenceremettant(currentRemise.getAgenceRemettant());
            currentCheque.setCompteremettant(currentRemise.getCompteRemettant());
            currentCheque.setRefremise(currentRemise.getReference());
            currentCheque.setEscompte(currentRemise.getEscompte());
            if (escompte != null && escompte.equals("ON")) {
                currentCheque.setEscompte(new BigDecimal(1));
            }
//            else {
//                currentCheque.setEscompte(new BigDecimal(0));
//            }

            currentCheque.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setHeuresaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HH:mm:ss"));
            currentCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDateemission(Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy"));
            currentCheque.setDevise("GNF"); //XOF
            currentCheque.setNombeneficiaire(currentRemise.getNomClient());

            sql = "SELECT * FROM AGENCES WHERE trim(CODEBANQUE) LIKE '" + currentCheque.getBanque().trim() + "' AND trim(CODEAGENCE) LIKE '" + currentCheque.getAgence() + "'";
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                currentCheque.setVille(agences[0].getCodevillecompense());
            } else {
                //currentCheque.setVille("01");
                db.close();
                return printMessage("Le code Agence " + currentCheque.getAgence() + " n'est pas declare dans la base");
            }
            currentCheque.setVilleremettant("01");
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentCheque.setCodeutilisateur(user.getLogin().trim());

            currentCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
            currentCheque.setIndicateurmodificationcmc7(new BigDecimal(0));
            currentCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPESAI")));

            if (index > currentRemise.getNbOperation().intValue()) {
                db.close();
                return printMessage("Nombre d'operations de la Remise =" + currentRemise.getNbOperation()
                        + "\n inferieur au Nombre des cheques =" + index);
            }

            String cleribCal = Utility.computeCleRIBACPACH(currentCheque.getBanque(), currentCheque.getAgence(), currentCheque.getNumerocompte());
            if (!cleribCal.equals(currentCheque.getRibcompte())) {
                db.close();
                return printMessage("La Banque " + currentCheque.getBanque() + "\nL'Agence " + currentCheque.getAgence() + "\nLe Compte " + currentCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
            }

            long sumAmount = 0;

            String array[] = hashMap.values().toArray(new String[0]);
            System.out.println("array length " + array.length);
            for (int i = 0; i < array.length; i++) {
                sumAmount += Long.parseLong(array[i]);
            }

            if (sumAmount > Long.parseLong(currentRemise.getMontant().trim())) {
                db.close();
                return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " inferieur \nau montant courant de la somme des cheques =" + sumAmount);
            }

            if (index == currentRemise.getNbOperation().intValue()) {
                if (sumAmount < Long.parseLong(currentRemise.getMontant().trim())) {
                    db.close();
                    return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " superieur \nau montant de la somme des cheques =" + sumAmount);
                }

            }

            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentCheque, "CHEQUES", "IDCHEQUE =" + currentCheque.getIdcheque());

            if (updateRowByObjectByQuery) {

                if (index == currentRemise.getNbOperation().intValue()) {
                    //Recuperation de letablissement du user connecte
                    boolean validationCorporate = false;

                    String flagValCorp = Utility.getParam("VALIDATION_CORP");
                    if (flagValCorp != null && flagValCorp.equals("1")) {
                        sql = "SELECT e.* FROM ETABLISSEMENTS e JOIN UTILISATEURS u ON trim(u.adresse)=trim(e.codeetablissement) AND trim(u.login)='" + user.getLogin().trim() + "'";

                        Etablissements etablissements[] = (Etablissements[]) db.retrieveRowAsObject(sql, new Etablissements());
                        if (etablissements != null && etablissements.length > 0) {
                            BigDecimal algo = etablissements[0].getAlgo();
                            if (algo != null && algo.equals(new BigDecimal("1"))) {
                                validationCorporate = true;
                            }

                        }
                    }

                    String nextStep;
                    if (validationCorporate) {
                        nextStep = Utility.getParam("CETAOPEVALITE");
                    } else {
                        nextStep = Utility.getParam("CETAOPEVAL");
                    }

                    if (db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', ETAT=" + nextStep + " WHERE ETAT=" + Utility.getParam("CETAOPESAI") + " "
                            + " AND REMISE=" + currentRemise.getIdremise()) == currentRemise.getNbOperation().intValue()) {
                        if (db.executeUpdate("UPDATE REMISES SET ETAT=" + nextStep + " WHERE IDREMISE=" + currentRemise.getIdremise()) == 1) {
                            db.executeUpdate("INSERT INTO SEQUENCES_CHEQUES_ARCHIVES SELECT * FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE=" + currentCheque.getSequence());
                            db.executeUpdate("DELETE FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE=" + currentCheque.getSequence());
                            db.executeUpdate("UPDATE REMISES SET ETAT=" + nextStep + " WHERE IDREMISE=" + currentRemise.getIdremise());
                        } else {
                            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESTO") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                            db.close();
                            System.out.println("Erreur lors de la MAJ de le remise -" + db.getMessage());
                            return printMessage("Erreur lors de la MAJ de le remise -" + db.getMessage());
                        }
                    } else {
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESAI") + " WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND REMISE=" + currentRemise.getIdremise());
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESTO") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                        db.close();
                        System.out.println("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());

                        return printMessage("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());
                    }

                } else {
                    db.executeUpdate("INSERT INTO SEQUENCES_CHEQUES_ARCHIVES SELECT * FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE=" + currentCheque.getSequence());
                    db.executeUpdate("DELETE FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE=" + currentCheque.getSequence());

                }

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);

        }
        System.out.println("Fin checkChequeViergeUBAGuinee");
        db.close();
        System.out.println("CHEQUE NON OK-" + db.getMessage());
        return printMessage("CHEQUE NON OK-" + db.getMessage());

    }

    public String checkChequeVierge(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("checkChequeVierge");
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemise");
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentCheque");
            currentCheque.setMontantcheque(request.getParameter("montantCheque").replaceAll("\\p{javaWhitespace}+", ""));
            currentCheque.setBanque(request.getParameter("codeBanque").toUpperCase(Locale.ENGLISH));
            currentCheque.setAgence(Utility.bourrageGZero(request.getParameter("codeAgence"), 5));
            currentCheque.setIban(request.getParameter("police"));
            currentCheque.setNumerocompte(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
            currentCheque.setNumerocheque(Utility.bourrageGZero(request.getParameter("numeroCheque"), 7));
            currentCheque.setRibcompte(Utility.bourrageGZero(request.getParameter("clerib"), 2));
            currentCheque.setNomemetteur(request.getParameter("nomTire"));
            currentCheque.setDateemission(request.getParameter("dateEmis"));
            String forcage = request.getParameter("forcage");
            String datvalj1 = request.getParameter("datvalj1");
            String escompte = request.getParameter("escompte");
            // Gestion Garde Cheque 
            String garde = request.getParameter("garde");
            if (garde != null) {
                String calcul = request.getParameter("calcul");
                currentCheque.setDateecheance(request.getParameter("dateEch"));
                currentCheque.setGarde(garde.equals("ON") ? "1" : "0");
                currentCheque.setCalcul(calcul.equals("ON") ? "1" : "0");
                if (garde.equals("ON")) {
                    Calendar myCal = Calendar.getInstance();
                    Calendar myCal2 = Calendar.getInstance();
                    myCal.setTime(Utility.convertStringToDate(currentCheque.getDateemission(), "yyyy/MM/dd"));
                    myCal2.setTime(Utility.convertStringToDate(currentCheque.getDateecheance(), "yyyy/MM/dd"));

                    long day = Utility.getDaysBetween(myCal, myCal2);
                    if (day < Integer.parseInt(Utility.getParam("GARDE_MIN")) || day > Integer.parseInt(Utility.getParam("GARDE_MAX"))) {
                        return printMessage(" La Date d'echeance doit etre superieur a " + Integer.parseInt(Utility.getParam("GARDE_MIN")) + " jours et inferieur a " + Integer.parseInt(Utility.getParam("GARDE_MAX")) + " jours");
                    }
                }

            }

            if (currentCheque.getMontantcheque() == null || currentCheque.getMontantcheque().trim().equals("")) {
                return printMessage(" Le Montant du cheque doit être renseigne");
            }
            if (!currentCheque.getMontantcheque().matches("[0-9]+")) {
                return printMessage(" Le Montant du cheque ne doit contenir que des chiffres");
            }

            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("")) {
                return printMessage(" Le Numero du cheque doit être renseigne");
            }
            if (!currentCheque.getNumerocheque().matches("[0-9]+")) {
                return printMessage(" Le Numero du cheque ne doit contenir que des chiffres");
            }
            if (currentCheque.getBanque() == null || currentCheque.getBanque().trim().equals("")) {
                return printMessage(" La Banque du cheque doit être renseigne");
            }
            if (currentCheque.getAgence() == null || currentCheque.getAgence().trim().equals("")) {
                return printMessage(" L'Agence du cheque doit être renseigne");
            }
            if (!currentCheque.getAgence().matches("[0-9]+")) {
                return printMessage(" L'Agence du cheque ne doit contenir que des chiffres");
            }
            if (currentCheque.getNumerocompte() == null || currentCheque.getNumerocompte().trim().equals("")) {
                return printMessage(" Le Numero de compte doit être renseigne");
            }
            if (!currentCheque.getNumerocompte().matches("[0-9]+")) {
                return printMessage(" Le Numero de compte ne doit contenir que des chiffres");
            }
            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(currentCheque.getNumerocheque().trim())) {
                return printMessage(" Le Numero de cheque doit être renseigne par un entier");
            }
            if (currentCheque.getRibcompte() == null || currentCheque.getRibcompte().trim().equals("")) {
                return printMessage(" La cle RIB du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() != null && Character.isDigit(currentCheque.getBanque().charAt(1))) {

                return printMessage(" Le Code Banque de ce cheque est au format SICA 2.\n Il ne sera pas accepte.");
            }

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String sql = "SELECT * FROM ALL_CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPERETRECENVSIBVER") + "," + Utility.getParam("CETAOPERET") + ","//180 
                    + "" + Utility.getParam("CETAOPERETENVSIB") + "," + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERETRECENVSIB") + ","
                    + "" + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + ","
                    + "" + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") +","+Utility.getParam("CETAOPEVALITE")+","+Utility.getParam("CETAOPEVALITE2") + "," + Utility.getParam("CETAOPEALLPREICOM1") + ","
                    + "" + Utility.getParam("CETAOPEVAL2") + "," + Utility.getParam("CETAOPEVALSURCAIENVSIB") + "," + Utility.getParam("CETAOPESUPVALSURCAI") + ","
                    + "" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + "," + Utility.getParam("CETAOPEVALDELTA") + ") AND BANQUE='" + currentCheque.getBanque() + "'"
                    + "  AND AGENCE='" + currentCheque.getAgence() + "' AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "'";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            currentCheque.setCoderepresentation(BigDecimal.ZERO);
            if (cheques != null && cheques.length > 0) {
                if (forcage != null && forcage.equals("ON")) {
                    currentCheque.setCoderepresentation(new BigDecimal(cheques.length));
                } else {
                    if (!cheques[0].getIdcheque().equals(currentCheque.getIdcheque())) {
                        db.close();
                        return printMessage("Ce cheque a deja ete valide et n'a pas encore ete rejete.\n"
                                + "Il reference le cheque avec IDCHEQUE = " + cheques[0].getIdcheque() + " a l'etat " + cheques[0].getEtat() + " - " + Utility.getParamLabelOfType("" + cheques[0].getEtat(), "CODE_ETAT"));
                    }
                }
            }

            HashMap<BigDecimal, String> hashMap = new HashMap(currentRemise.getNbOperation().intValue());
            sql = "SELECT * FROM CHEQUES WHERE ETAT >= " + Utility.getParam("CETAOPESAI") + "  AND REMISE=" + currentRemise.getIdremise();
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                for (int i = 0; i < cheques.length; i++) {
                    if (!cheques[i].getSequence().equals(currentCheque.getSequence())) {
                        hashMap.put(cheques[i].getSequence(), cheques[i].getMontantcheque().trim());
                    }
                }
            }

            hashMap.put(currentCheque.getSequence(), currentCheque.getMontantcheque().trim());

            int index = Integer.parseInt(request.getParameter("index"));

            currentCheque.setDevise("XOF");
            if (Character.isDigit(currentCheque.getBanque().charAt(1))) {
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    currentCheque.setEtablissement(currentRemise.getAgenceDepot());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanque());
                } else {
                    currentCheque.setEtablissement(currentRemise.getAgenceDepot());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                }
                currentCheque.setType_Cheque("030");
            } else {
                currentCheque.setEtablissement(currentRemise.getAgenceDepot());
                currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                currentCheque.setType_Cheque("035");
            }
            currentCheque.setAgenceremettant(currentRemise.getAgenceRemettant());
            currentCheque.setCompteremettant(currentRemise.getCompteRemettant());
            currentCheque.setRefremise(currentRemise.getReference());
            currentCheque.setEscompte(currentRemise.getEscompte());
            if (escompte != null && escompte.equals("ON")) {
                currentCheque.setEscompte(new BigDecimal(1));
            }
            if (datvalj1 != null && datvalj1.equals("ON")) {
                currentCheque.setEscompte(new BigDecimal(2));
            }
//            else {
//                currentCheque.setEscompte(new BigDecimal(0));
//            }

            currentCheque.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setHeuresaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HH:mm:ss"));
            currentCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            //currentCheque.setDateemission(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDevise("XOF");
            currentCheque.setNombeneficiaire(currentRemise.getNomClient());

            sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '" + currentCheque.getBanque() + "' AND CODEAGENCE LIKE '" + currentCheque.getAgence() + "'";
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                currentCheque.setVille(agences[0].getCodevillecompense());
            } else {
                //currentCheque.setVille("01");
                db.close();
                return printMessage("Le code Agence " + currentCheque.getAgence() + " n'est pas declare dans la base");
            }
            currentCheque.setVilleremettant("01");
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentCheque.setCodeutilisateur(user.getLogin().trim());

            currentCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
            currentCheque.setIndicateurmodificationcmc7(new BigDecimal(0));
            currentCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPESAI")));

            if (index > currentRemise.getNbOperation().intValue()) {
                db.close();
                return printMessage("Nombre d'operations de la Remise =" + currentRemise.getNbOperation()
                        + "\n inferieur au Nombre des cheques =" + index);
            }

            String cleribCal = Utility.computeCleRIB(currentCheque.getBanque(), currentCheque.getAgence(), currentCheque.getNumerocompte());
            if (!cleribCal.equals(currentCheque.getRibcompte())) {
                db.close();
                return printMessage("La Banque " + currentCheque.getBanque() + "\nL'Agence " + currentCheque.getAgence() + "\nLe Compte " + currentCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
            }

            long sumAmount = 0;

            String array[] = hashMap.values().toArray(new String[0]);
            for (int i = 0; i < array.length; i++) {
                sumAmount += Long.parseLong(array[i]);
            }

            if (sumAmount > Long.parseLong(currentRemise.getMontant().trim())) {
                db.close();
                return printMessage("Montant de la Remise =" + Utility.formatNumber(currentRemise.getMontant()) + " inferieur \nau montant courant de la somme des cheques =" + Utility.formatNumber("" + sumAmount));
            }

            if (index == currentRemise.getNbOperation().intValue()) {
                if (sumAmount < Long.parseLong(currentRemise.getMontant().trim())) {
                    db.close();
                    return printMessage("Montant de la Remise =" + Utility.formatNumber(currentRemise.getMontant()) + " superieur \nau montant de la somme des cheques =" + Utility.formatNumber("" + sumAmount));
                }

            }

            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentCheque, "CHEQUES", "IDCHEQUE =" + currentCheque.getIdcheque());

            if (updateRowByObjectByQuery) {

                if (index == currentRemise.getNbOperation().intValue()) {
                    //Recuperation de letablissement du user connecte
                    boolean validationCorporate = false;

                    String flagValCorp = Utility.getParam("VALIDATION_CORP");
                    if (flagValCorp != null && flagValCorp.equals("1")) {
                        sql = "SELECT e.* FROM ETABLISSEMENTS e JOIN UTILISATEURS u ON trim(u.adresse)=trim(e.codeetablissement) AND trim(u.login)='" + user.getLogin().trim() + "'";

                        Etablissements etablissements[] = (Etablissements[]) db.retrieveRowAsObject(sql, new Etablissements());
                        if (etablissements != null && etablissements.length > 0) {
                            BigDecimal algo = etablissements[0].getAlgo();
                            if (algo != null && algo.equals(new BigDecimal("1"))) {
                                validationCorporate = true;
                            }

                        }
                    }

                    String nextStep;
                    if (validationCorporate) {
                        nextStep = Utility.getParam("CETAOPEVALITE");
                    } else {
                        nextStep = Utility.getParam("CETAOPEVAL");
                    }

                    if (db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', ETAT=" + nextStep + " WHERE ETAT=" + Utility.getParam("CETAOPESAI") + " "
                            + " AND REMISE=" + currentRemise.getIdremise()) == currentRemise.getNbOperation().intValue()) {
                        System.out.println("Fin de saisie de la remise");
                        if (db.executeUpdate("UPDATE REMISES SET ETAT=" + nextStep + " WHERE IDREMISE=" + currentRemise.getIdremise()) == 1) {
                            db.executeUpdate("INSERT INTO SEQUENCES_CHEQUES_ARCHIVES SELECT * FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE=" + currentCheque.getSequence());
                            db.executeUpdate("DELETE FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE=" + currentCheque.getSequence());
                            db.executeUpdate("UPDATE REMISES SET ETAT=" + nextStep + " WHERE IDREMISE=" + currentRemise.getIdremise());
                            if (Utility.getParam("VALIDATION_CHEQUE_AUTO") != null && Utility.getParam("VALIDATION_CHEQUE_AUTO").equalsIgnoreCase("AUTO")) {
                                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM1") + " WHERE BANQUE<>BANQUEREMETTANT AND REMISE=" + currentRemise.getIdremise());
                                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " WHERE BANQUE= BANQUEREMETTANT AND REMISE=" + currentRemise.getIdremise());
                                db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPESUPVAL") + " WHERE IDREMISE=" + currentRemise.getIdremise());
                            }
                            System.out.println("XXXXXXXXX");
                        } else {
                            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESTO") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                            db.close();
                            System.out.println("Erreur lors de la MAJ de le remise -" + db.getMessage());
                            return printMessage("Erreur lors de la MAJ de le remise -" + db.getMessage());
                        }
                    } else {
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESAI") + " WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND REMISE=" + currentRemise.getIdremise());
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESTO") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                        db.close();
                        System.out.println("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());

                        return printMessage("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());
                    }

                } else {
                    db.executeUpdate("INSERT INTO SEQUENCES_CHEQUES_ARCHIVES SELECT * FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE=" + currentCheque.getSequence());
                    db.executeUpdate("DELETE FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE=" + currentCheque.getSequence());

                }

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);

        }
        db.close();
        System.out.println("CHEQUE NON OK-" + db.getMessage());
        return printMessage("CHEQUE NON OK-" + db.getMessage());

    }

      public String checkEffetVierge(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("checkEffetVierge");
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            RemisesEffets currentRemise = (RemisesEffets) request.getSession().getAttribute("currentRemise");
            Effets currentEffet = (Effets) request.getSession().getAttribute("currentEffet");
            
            currentEffet.setMontant_Effet(request.getParameter("montantEffet").replaceAll("\\p{javaWhitespace}+", ""));
            currentEffet.setBanque(request.getParameter("codeBanque").toUpperCase(Locale.ENGLISH));
            currentEffet.setAgence(Utility.bourrageGZero(request.getParameter("codeAgence"), 5));
            currentEffet.setIban_Tire(request.getParameter("police"));
            currentEffet.setNumerocompte_Tire(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
            //currentEffet.setNumeroeffet(Utility.bourrageGZero(request.getParameter("numeroEffet"), 7));
            currentEffet.setNumeroeffet(request.getParameter("numeroEffet"));
            currentEffet.setRibcompte(Utility.bourrageGZero(request.getParameter("clerib"), 2));
            currentEffet.setNom_Tire(request.getParameter("nomTire"));
            currentEffet.setDate_Creation(request.getParameter("dateCrea"));
            currentEffet.setDate_Echeance(request.getParameter("dateEch"));
            
                    
            currentEffet.setType_Effet(request.getParameter("typeEffet"));
            currentEffet.setMontant_Frais(request.getParameter("montantfrais"));
            
            String forcage = request.getParameter("forcage");
            String datvalj1 = request.getParameter("datvalj1");
            String escompte = request.getParameter("escompte");
           // Gestion Garde Cheque 
           //String garde = request.getParameter("garde");
//            if (garde != null) {
//                String calcul = request.getParameter("calcul");
//                currentEffet.setDate_Echeance(request.getParameter("dateEch"));
//                currentEffet.setGarde(garde.equals("ON") ? "1" : "0");
//                currentEffet.setCalcul(calcul.equals("ON") ? "1" : "0");
//                if (garde.equals("ON")) {
//                    Calendar myCal = Calendar.getInstance();
//                    Calendar myCal2 = Calendar.getInstance();
//                    myCal.setTime(Utility.convertStringToDate(currentCheque.getDateemission(), "yyyy/MM/dd"));
//                    myCal2.setTime(Utility.convertStringToDate(currentCheque.getDateecheance(), "yyyy/MM/dd"));
//
//                    long day = Utility.getDaysBetween(myCal, myCal2);
//                    if (day < Integer.parseInt(Utility.getParam("GARDE_MIN")) || day > Integer.parseInt(Utility.getParam("GARDE_MAX"))) {
//                        return printMessage(" La Date d'echeance doit etre superieur a " + Integer.parseInt(Utility.getParam("GARDE_MIN")) + " jours et inferieur a " + Integer.parseInt(Utility.getParam("GARDE_MAX")) + " jours");
//                    }
//                }
//
//            }

            if (currentEffet.getType_Effet() == null || currentEffet.getType_Effet().trim().equals("")) {
                return printMessage(" Le type de  l'effet doit être renseigne");
            }

            if (currentEffet.getMontant_Effet() == null || currentEffet.getMontant_Effet().trim().equals("")) {
                return printMessage(" Le Montant de l'effet doit être renseigne");
            }
            if (!currentEffet.getMontant_Effet().matches("[0-9]+")) {
                return printMessage(" Le Montant de l'effet  ne doit contenir que des chiffres");
            }

            if (currentEffet.getNumeroeffet()== null || currentEffet.getNumeroeffet().trim().equals("")) {
                return printMessage(" Le Numero de l'effet  doit être renseigne");
            }
            if (!currentEffet.getNumeroeffet().matches("[0-9]+")) {
                return printMessage(" Le Numero de l'effet ne doit contenir que des chiffres");
            }
            if (currentEffet.getBanque() == null || currentEffet.getBanque().trim().equals("")) {
                return printMessage(" La Banque de l'effet doit être renseigne");
            }
            if (currentEffet.getAgence() == null || currentEffet.getBanque().trim().equals("")) {
                return printMessage(" L'Agence de l'effet doit être renseigne");
            }
            if (!currentEffet.getAgence().matches("[0-9]+")) {
                return printMessage(" L'Agence de l'effet ne doit contenir que des chiffres");
            }
            if (currentEffet.getNumerocompte_Tire()== null || currentEffet.getNumerocompte_Tire().trim().equals("")) {
                return printMessage(" Le Numero de compte doit être renseigne");
            }
            if (!currentEffet.getNumerocompte_Tire().matches("[0-9]+")) {
                return printMessage(" Le Numero de compte ne doit contenir que des chiffres");
            }
            if (currentEffet.getNumeroeffet() == null || currentEffet.getNumeroeffet().trim().equals("") || !Utility.isInteger(currentEffet.getNumeroeffet().trim())) {
                return printMessage(" Le Numero de l'effet doit être renseigne par un entier");
            }
            if (currentEffet.getRibcompte() == null || currentEffet.getRibcompte().trim().equals("")) {
                return printMessage(" La cle RIB de l'effet doit être renseigne");
            }
            if (currentEffet.getBanque() != null && Character.isDigit(currentEffet.getBanque().charAt(1))) {

                return printMessage(" Le Code Banque de cet effet est au format SICA 2.\n Il ne sera pas accepte.");
            }
            
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String sql = "SELECT * FROM ALL_EFFETS WHERE ETAT IN (" + Utility.getParam("CETAOPERETRECENVSIBVER") + "," + Utility.getParam("CETAOPERET") + ","
                    + "" + Utility.getParam("CETAOPERETENVSIB") + "," + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERETRECENVSIB") + ","
                    + "" + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + ","
                    + "" + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + ","
                    + "" + Utility.getParam("CETAOPEVAL2") + "," + Utility.getParam("CETAOPEVALSURCAIENVSIB") + "," + Utility.getParam("CETAOPESUPVALSURCAI") + ","
                    + "" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + "," + Utility.getParam("CETAOPEVALDELTA") + ") AND BANQUE='" + currentEffet.getBanque() + "'"
                    + "  AND AGENCE='" + currentEffet.getAgence() + "' AND NUMEROEFFET='" + currentEffet.getNumeroeffet() + "' AND NUMEROCOMPTE_TIRE='" + currentEffet.getNumerocompte_Tire() + "'";
            Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
            //currentEffet.setCoderepresentation(BigDecimal.ZERO);
            currentEffet.setCoderepresentation(BigDecimal.ZERO);
            if (effets != null && effets.length > 0) {
                if (forcage != null && forcage.equals("ON")) {
                    currentEffet.setCoderepresentation(new BigDecimal(effets.length));
                } else {
                    if (!effets[0].getIdeffet().equals(currentEffet.getIdeffet())) {
                        db.close();
                        return printMessage("Cet effet a deja ete valide et n'a pas encore ete rejete.\n"
                                + "Il reference l'effet avec IDEFFET = " + effets[0].getIdeffet() + " a l'etat " + effets[0].getEtat() + " - " + Utility.getParamLabelOfType("" + effets[0].getEtat(), "CODE_ETAT"));
                    }
                }
            }

            HashMap<BigDecimal, String> hashMap = new HashMap(currentRemise.getNbOperation().intValue());
            sql = "SELECT * FROM EFFETS WHERE ETAT >= " + Utility.getParam("CETAOPESAI") + "  AND REMISE=" + currentRemise.getIdremise();
            effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
            if (effets != null && effets.length > 0) {
                for (int i = 0; i < effets.length; i++) {
                    if (!effets[i].getSequence().equals(currentEffet.getSequence())) {
                        hashMap.put(effets[i].getSequence(), effets[i].getMontant_Effet().trim());
                    }
                }
            }

            hashMap.put(currentEffet.getSequence(), currentEffet.getMontant_Effet().trim());

            int index = Integer.parseInt(request.getParameter("index"));

            //type effets a mettre a jour 
            currentEffet.setDevise("XOF");
            
            //same 
            if (Character.isDigit(currentEffet.getBanque().charAt(1))) {
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    currentEffet.setEtablissement(currentRemise.getAgenceDepot());
                    currentEffet.setBanqueremettant(CMPUtility.getCodeBanque());
                } else {
                    currentEffet.setEtablissement(currentRemise.getAgenceDepot());
                    currentEffet.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                }
                currentEffet.setType_Effet(request.getParameter("typeEffet"));
            } else {
                currentEffet.setEtablissement(currentRemise.getAgenceDepot());
                currentEffet.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                currentEffet.setType_Effet(request.getParameter("typeEffet"));
            }
            
            
            
            currentEffet.setAgenceremettant(currentRemise.getAgenceRemettant());
            currentEffet.setNumerocompte_Beneficiaire(currentRemise.getCompteRemettant());
            currentEffet.setRefremise(currentRemise.getReference());
            //currentEffet.setEscompte(currentRemise.getEscompte());
//            if (escompte != null && escompte.equals("ON")) {
//                currentCheque.setEscompte(new BigDecimal(1));
//            }
//            if (datvalj1 != null && datvalj1.equals("ON")) {
//                currentCheque.setEscompte(new BigDecimal(2));
//            }
//            else {
//                currentCheque.setEscompte(new BigDecimal(0));
//            }

            currentEffet.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentEffet.setHeuresaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HH:mm:ss"));
            currentEffet.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            //currentCheque.setDateemission(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));

            currentEffet.setDevise("XOF");
            currentEffet.setNom_Beneficiaire(currentRemise.getNomClient());

            sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '" + currentEffet.getBanque() + "' AND CODEAGENCE LIKE '" + currentEffet.getAgence() + "'";
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                currentEffet.setVille(agences[0].getCodevillecompense());
            } else {
                //currentCheque.setVille("01");
                db.close();
                return printMessage("Le code Agence " + currentEffet.getAgence() + " n'est pas declare dans la base");
            }
            currentEffet.setVille("01");
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentEffet.setCodeUtilisateur(user.getLogin().trim());

            currentEffet.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
            currentEffet.setIndicateurmodificationcmc7(new BigDecimal(0));
            currentEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPESAI")));

            if (index > currentRemise.getNbOperation().intValue()) {
                db.close();
                return printMessage("Nombre d'operations de la Remise =" + currentRemise.getNbOperation()
                        + "\n inferieur au Nombre des effets =" + index);
            }

            String cleribCal = Utility.computeCleRIB(currentEffet.getBanque(), currentEffet.getAgence(),currentEffet.getNumerocompte_Tire());
            if (!cleribCal.equals(currentEffet.getRibcompte())) {
                db.close();
                return printMessage("La Banque " + currentEffet.getBanque() + "\nL'Agence " + currentEffet.getAgence() + "\nLe Compte " + currentEffet.getNumerocompte_Tire() + " donne la cle rib " + cleribCal);
            }

            long sumAmount = 0;

            String array[] = hashMap.values().toArray(new String[0]);
            for (int i = 0; i < array.length; i++) {
                sumAmount += Long.parseLong(array[i]);
            }

            if (sumAmount > Long.parseLong(currentRemise.getMontant().trim())) {
                db.close();
                return printMessage("Montant de la Remise =" + Utility.formatNumber(currentRemise.getMontant()) + " inferieur \nau montant courant de la somme des effets =" + Utility.formatNumber("" + sumAmount));
            }

            if (index == currentRemise.getNbOperation().intValue()) {
                if (sumAmount < Long.parseLong(currentRemise.getMontant().trim())) {
                    db.close();
                    return printMessage("Montant de la Remise =" + Utility.formatNumber(currentRemise.getMontant()) + " superieur \nau montant de la somme des effets =" + Utility.formatNumber("" + sumAmount));
                }

            }

            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentEffet, "EFFETS", "IDEFFET =" + currentEffet.getIdeffet());

            if (updateRowByObjectByQuery) {

                if (index == currentRemise.getNbOperation().intValue()) {
                    //Recuperation de letablissement du user connecte
                    boolean validationCorporate = false;

                    String flagValCorp = Utility.getParam("VALIDATION_CORP");
                    if (flagValCorp != null && flagValCorp.equals("1")) {
                        sql = "SELECT e.* FROM ETABLISSEMENTS e JOIN UTILISATEURS u ON trim(u.adresse)=trim(e.codeetablissement) AND trim(u.login)='" + user.getLogin().trim() + "'";

                        Etablissements etablissements[] = (Etablissements[]) db.retrieveRowAsObject(sql, new Etablissements());
                        if (etablissements != null && etablissements.length > 0) {
                            BigDecimal algo = etablissements[0].getAlgo();
                            if (algo != null && algo.equals(new BigDecimal("1"))) {
                                validationCorporate = true;
                            }

                        }
                    }

                    String nextStep;
                    if (validationCorporate) {
                        nextStep = Utility.getParam("CETAOPEVALITE");
                    } else {
                        nextStep = Utility.getParam("CETAOPEVAL");
                    }

                    if (db.executeUpdate("UPDATE EFFETS SET NUMEROCOMPTE_BENEFICIAIRE='" + currentRemise.getCompteRemettant() + "', ETAT=" + nextStep + " WHERE ETAT=" + Utility.getParam("CETAOPESAI") + " "
                            + " AND REMISE=" + currentRemise.getIdremise()) == currentRemise.getNbOperation().intValue()) {
                        System.out.println("Fin de saisie de la remise");
                        if (db.executeUpdate("UPDATE REMISESEFFETS SET ETAT=" + nextStep + " WHERE IDREMISE=" + currentRemise.getIdremise()) == 1) {
                           // db.executeUpdate("INSERT INTO SEQUENCES_EFFETS_ARCHIVES SELECT * FROM SEQUENCES_CHEQUES WHERE IDSEQUENCE=" + currentEffet.getSequence());
                            db.executeUpdate("DELETE FROM SEQUENCES_EFFETS WHERE IDSEQUENCE=" + currentEffet.getSequence());
                            db.executeUpdate("UPDATE REMISESEFFETS SET ETAT=" + nextStep + " WHERE IDREMISE=" + currentRemise.getIdremise());
                            if (Utility.getParam("VALIDATION_CHEQUE_AUTO") != null && Utility.getParam("VALIDATION_CHEQUE_AUTO").equalsIgnoreCase("AUTO")) {
                                db.executeUpdate("UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPEALLICOM1") + " WHERE BANQUE<>BANQUEREMETTANT AND REMISE=" + currentRemise.getIdremise());
                                db.executeUpdate("UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " WHERE BANQUE= BANQUEREMETTANT AND REMISE=" + currentRemise.getIdremise());
                                db.executeUpdate("UPDATE REMISESEFFETS SET ETAT=" + Utility.getParam("CETAOPESUPVAL") + " WHERE IDREMISE=" + currentRemise.getIdremise());
                            }
                            System.out.println("XXXXXXXXX");
                        } else {
                            db.executeUpdate("UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPESTO") + " WHERE IDEFFET=" + currentEffet.getIdeffet());
                            db.close();
                            System.out.println("Erreur lors de la MAJ de le remise -" + db.getMessage());
                            return printMessage("Erreur lors de la MAJ de le remise -" + db.getMessage());
                        }
                    } else {
                        db.executeUpdate("UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPESAI") + " WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND REMISE=" + currentRemise.getIdremise());
                        db.executeUpdate("UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPESTO") + " WHERE IDEFFET=" + currentEffet.getIdeffet());
                        db.close();
                        System.out.println("Erreur lors de MAJ des effets de la remise -" + db.getMessage());

                        return printMessage("Erreur lors de MAJ des effets de la remise -" + db.getMessage());
                    }

                } else {
                  //  db.executeUpdate("INSERT INTO SEQUENCES_EFFET_ARCHIVES SELECT * FROM SEQUENCES_EFFETS WHERE IDSEQUENCE=" + currentEffet.getSequence());
                    db.executeUpdate("DELETE FROM SEQUENCES_EFFETS WHERE IDSEQUENCE=" + currentEffet.getSequence());

                }

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);

        }
        db.close();
        System.out.println("EFFETS NON OK-" + db.getMessage());
        return printMessage("EFFETS NON OK-" + db.getMessage());

    }
      
      
      
    public String deleteDocumentEffet(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            String idObjet = request.getParameter("idObjet");
            String typeObjet = request.getParameter("typeObjet");
            String sequenceObjet = request.getParameter("sequenceObjet");

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            if (sequenceObjet != null && !sequenceObjet.trim().equals("")) {
                db.executeUpdate("DELETE FROM SEQUENCES_EFFETS WHERE IDSEQUENCE=" + sequenceObjet);

                String cheminImage = "";
                if (typeObjet != null && typeObjet.trim().equals("EFFETS")) {
                    Effets[] effets = (Effets[]) db.retrieveRowAsObject("SELECT * FROM EFFETS WHERE IDEFFET = " + idObjet, new Effets());
                    if (effets != null && effets.length > 0) {
                        if (effets[0].getEtat().intValue() < new Integer(Utility.getParam("CETAOPEVAL")).intValue()) {
                            cheminImage = effets[0].getPathimage() + File.separator + effets[0].getFichierimage();
//                            new File(cheminImage + "f.jpg").delete();
//                            new File(cheminImage + "r.jpg").delete();
//                            new File(cheminImage + "d.txt").delete();
                            db.insertObjectAsRowByQuery(effets[0], "EFFETS_SUPP");
                        }
                        //Gestion du dernier cheque de la remise
                        //verifions la position du cheque
                        int index = 0;
                        String sql = "SELECT * FROM REMISESEFFETS WHERE IDREMISE=" + effets[0].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAREMSTO") + "," + Utility.getParam("CETAREMSAI") + ")";
                        RemisesEffets[] remisesEffets = (RemisesEffets[]) db.retrieveRowAsObject(sql, new RemisesEffets());
                        if (remisesEffets != null && remisesEffets.length > 0) {
                            sql = "SELECT * FROM EFFETS WHERE REMISE=" + effets[0].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPESTO") + "," + Utility.getParam("CETAOPESAI") + ")  ORDER BY SEQUENCE";
                            Effets[] effetsCal = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                            if (effetsCal != null && effetsCal.length > 0) {
                                for (int i = 0; i < effetsCal.length; i++) {
                                    if (effetsCal[i].getIdeffet().equals(effets[0].getIdeffet())) {
                                        index = i + 1;
                                    }
                                }

                                if (effetsCal.length > 1 && index == effetsCal.length) {
                                    //Dernier cheque, il faut donc inserer l'avant dernier dans Sequences
                                    Effets avdEffets = effetsCal[index - 2];
                                    Sequences sequences = new Sequences();
                                    sequences.setTypedocument("EFFETS");
                                    sequences.setMachinescan(avdEffets.getMachinescan());
                                    sequences.setIdsequence(avdEffets.getSequence());
                                    sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                                    sequences.setCodeline("___________");
                                    db.insertObjectAsRowByQuery(sequences, "SEQUENCES_EFFETS");
                                    //mettre a jour avdCheque pour dire qu'il n'est pas encore saisi
                                    avdEffets.setEtat(new BigDecimal(Utility.getParam("CETAOPESTO")));
                                    db.updateRowByObjectByQuery(avdEffets, "EFFETS", "IDEFFET=" + avdEffets.getIdeffet());

                                } else if (effetsCal.length == 1 && index == effetsCal.length) {
                                    //Dernier cheque sans avant dernier, donc suppression de la remise elle-même
                                    db.executeUpdate("DELETE FROM REMISESEFFETS WHERE IDREMISE=" + remisesEffets[0].getIdremise());

                                }
                                // Mettre a jour le nbOperation dans la remise
                                if (effetsCal.length > 1) {
                                    remisesEffets[0].setNbOperation(new BigDecimal(effetsCal.length - 1));
                                    db.updateRowByObjectByQuery(remisesEffets[0], "REMISESEFFETS", "IDREMISE=" + remisesEffets[0].getIdremise());
                                }
                            }
                        }

                    }
                    if (effets != null && effets.length > 0) {
                        if (effets[0].getEtat().intValue() < new Integer(Utility.getParam("CETAOPEVAL")).intValue()) {
                            db.executeUpdate("DELETE FROM EFFETS WHERE IDEFFET=" + idObjet);
                        } else {
                            System.out.println("Document effet non supprime " + idObjet);
                        }
                    }
                }
                db.close();
                return printMessage("Document(s) supprime(s) avec succes");
            }
            db.close();
        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            db.close();
        }
        return printMessage("Erreur de suppression -" + db.getMessage());

    }

      
    //UBAGuinee
    public String corrigeChequeUBAGuinee(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemiseCorr");
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentChequeCorr");
            currentCheque.setMontantcheque(request.getParameter("montantCheque").replaceAll("\\p{javaWhitespace}+", ""));
            currentCheque.setBanque(request.getParameter("codeBanque").toUpperCase(Locale.ENGLISH));
            currentCheque.setAgence(Utility.bourrageGZero(request.getParameter("codeAgence"), 3));
            currentCheque.setNumerocompte(Utility.bourrageGZero(request.getParameter("numeroCompte"), 10));
            currentCheque.setNumerocheque(Utility.bourrageGZero(request.getParameter("numeroCheque"), 8));
            currentCheque.setRibcompte(Utility.bourrageGZero(request.getParameter("clerib"), 2));
            currentCheque.setNomemetteur(request.getParameter("nomTire"));
            currentCheque.setDateemission(request.getParameter("dateEmis"));

            if (currentCheque.getMontantcheque() == null || currentCheque.getMontantcheque().trim().equals("")) {
                return printMessage(" Le Montant du cheque doit être renseigne");
            }
            if (!currentCheque.getMontantcheque().matches("[0-9]+")) {
                return printMessage(" Le Montant du cheque ne doit contenir que des chiffres");
            }

            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("")) {
                return printMessage(" Le Numero du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() == null || currentCheque.getBanque().trim().equals("")) {
                return printMessage(" La Banque du cheque doit être renseigne");
            }
            if (currentCheque.getAgence() == null || currentCheque.getAgence().trim().equals("")) {
                return printMessage(" L'Agence du cheque doit être renseigne");
            }
            if (currentCheque.getNumerocompte() == null || currentCheque.getNumerocompte().trim().equals("")) {
                return printMessage(" Le Numero de compte doit être renseigne");
            }
            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(currentCheque.getNumerocheque().trim())) {
                return printMessage(" Le Numero de cheque doit être renseigne par un entier");
            }
            if (currentCheque.getRibcompte() == null || currentCheque.getRibcompte().trim().equals("")) {
                return printMessage(" La cle RIB du cheque doit être renseignee");
            }

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String sql = "SELECT * FROM ALL_CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + ","
                    + "" + Utility.getParam("CETAOPEVAL") + ","+ "" + Utility.getParam("CETAOPEVALITE") + "," + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + ","
                    + "" + Utility.getParam("CETAOPEALLPREICOM1") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEVALSURCAIENVSIB") + ","
                    + "" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND BANQUE='" + currentCheque.getBanque() + "' AND AGENCE='" + currentCheque.getAgence() + "'"
                    + " AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "'";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                if (!cheques[0].getIdcheque().equals(currentCheque.getIdcheque())) {
                    db.close();
                    return printMessage("Ce cheque a deja ete valide et n'a pas encore ete rejete.\n"
                            + "Il reference le cheque avec IDCHEQUE = " + cheques[0].getIdcheque() + " a l'etat " + cheques[0].getEtat());
                }
            }

            HashMap<BigDecimal, String> hashMap = new HashMap(currentRemise.getNbOperation().intValue());
            sql = "SELECT * FROM CHEQUES WHERE ETAT >= " + Utility.getParam("CETAOPECOR") + "  AND REMISE=" + currentRemise.getIdremise();
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                for (int i = 0; i < cheques.length; i++) {
                    if (!cheques[i].getSequence().equals(currentCheque.getSequence())) {
                        hashMap.put(cheques[i].getSequence(), cheques[i].getMontantcheque().trim());
                    }
                }
            }

            hashMap.put(currentCheque.getSequence(), currentCheque.getMontantcheque().trim());

            int index = Integer.parseInt(request.getParameter("index"));

            currentCheque.setDevise("GNF");
            currentCheque.setType_Cheque("30");
//            if (Character.isDigit(currentCheque.getBanque().charAt(1))) {
//                if (Utility.getParam("VERSION_SICA").equals("2")) {
//                    currentCheque.setEtablissement(CMPUtility.getCodeBanque());
//                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanque());
//                } else {
//                    currentCheque.setEtablissement(CMPUtility.getCodeBanqueSica3());
//                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
//                }
//                currentCheque.setType_Cheque("030");
//            } else {
//                currentCheque.setEtablissement(currentRemise.getEtablissement());
//                currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
//                currentCheque.setType_Cheque("035");
//            }
            currentCheque.setAgenceremettant(currentRemise.getAgenceRemettant());
            currentCheque.setCompteremettant(currentRemise.getCompteRemettant());
            currentCheque.setRefremise(currentRemise.getReference());
            currentCheque.setEscompte(currentRemise.getEscompte());
            currentCheque.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setHeuresaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HH:mm:ss"));
            currentCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDateemission(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));

            currentCheque.setNombeneficiaire(currentRemise.getNomClient());

            sql = "SELECT * FROM AGENCES WHERE TRIM(CODEBANQUE) LIKE '" + currentCheque.getBanque() + "' AND TRIM(CODEAGENCE) LIKE '" + currentCheque.getAgence() + "'";
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                currentCheque.setVille(agences[0].getCodevillecompense());
            } else {
                //currentCheque.setVille("01");
                db.close();
                return printMessage("Le code Agence " + currentCheque.getAgence() + " n'est pas declare dans la base");
            }
            currentCheque.setVilleremettant("01");
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentCheque.setCodeutilisateur(user.getLogin().trim());

            currentCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
            currentCheque.setIndicateurmodificationcmc7(new BigDecimal(0));
            currentCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPECOR")));

            if (index > currentRemise.getNbOperation().intValue()) {
                db.close();
                return printMessage("Nombre d'operations de la Remise =" + currentRemise.getNbOperation()
                        + "\n inferieur au Nombre des cheques =" + index);
            }

            String cleribCal = Utility.computeCleRIBACPACH(currentCheque.getBanque(), currentCheque.getAgence(), currentCheque.getNumerocompte());
            if (!cleribCal.equals(currentCheque.getRibcompte())) {
                db.close();
                return printMessage("La Banque " + currentCheque.getBanque() + "\nL'Agence " + currentCheque.getAgence() + "\nLe Compte " + currentCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
            }

            long sumAmount = 0;

            String array[] = hashMap.values().toArray(new String[0]);
            for (int i = 0; i < array.length; i++) {
                sumAmount += Long.parseLong(array[i]);
            }

            if (sumAmount > Long.parseLong(currentRemise.getMontant().trim())) {
                db.close();
                return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " inferieur \nau montant courant de la somme des cheques =" + sumAmount);
            }

            if (index == currentRemise.getNbOperation().intValue()) {
                if (sumAmount < Long.parseLong(currentRemise.getMontant().trim())) {
                    db.close();
                    return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " superieur \nau montant de la somme des cheques =" + sumAmount);
                }

            }

            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentCheque, "CHEQUES", "IDCHEQUE =" + currentCheque.getIdcheque());

            if (updateRowByObjectByQuery) { //                                        

                if (index == currentRemise.getNbOperation().intValue()) {
                    if (db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', ETAT=" + Utility.getParam("CETAOPEVAL") + " "
                            + "WHERE ETAT=" + Utility.getParam("CETAOPECOR") + " AND REMISE=" + currentRemise.getIdremise()) == currentRemise.getNbOperation().intValue()) {
                        if (db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMVAL") + " WHERE ETAT IN (" + Utility.getParam("CETAOPECOR") + "," + Utility.getParam("CETAOPEERR") + " ) "
                                + " AND NBOPERATION=" + currentRemise.getNbOperation() + " AND IDREMISE=" + currentRemise.getIdremise()) == 1) {

                            db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMVAL") + " WHERE IDREMISE=" + currentRemise.getIdremise());
                        } else {
                            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                            db.close();
                            return printMessage("Erreur lors de la MAJ de le remise -" + db.getMessage());
                        }
                    } else {
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPECOR") + " WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND REMISE=" + currentRemise.getIdremise());
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                        db.close();
                        return printMessage("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());
                    }

                }

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);

        }
        db.close();
        return printMessage("CHEQUE NON OK-" + db.getMessage());

    }

    public String corrigeCheque(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemiseCorr");
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentChequeCorr");
            currentCheque.setMontantcheque(request.getParameter("montantCheque").replaceAll("\\p{javaWhitespace}+", ""));
            currentCheque.setBanque(request.getParameter("codeBanque").toUpperCase(Locale.ENGLISH));
            currentCheque.setAgence(Utility.bourrageGZero(request.getParameter("codeAgence"), 5));
            currentCheque.setNumerocompte(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
            currentCheque.setNumerocheque(Utility.bourrageGZero(request.getParameter("numeroCheque"), 7));
            currentCheque.setRibcompte(Utility.bourrageGZero(request.getParameter("clerib"), 2));
            currentCheque.setNomemetteur(request.getParameter("nomTire"));
            currentCheque.setDateemission(request.getParameter("dateEmis"));

            if (currentCheque.getMontantcheque() == null || currentCheque.getMontantcheque().trim().equals("")) {
                return printMessage(" Le Montant du cheque doit être renseigne");
            }
            if (!currentCheque.getMontantcheque().matches("[0-9]+")) {
                return printMessage(" Le Montant du cheque ne doit contenir que des chiffres");
            }

            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("")) {
                return printMessage(" Le Numero du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() == null || currentCheque.getBanque().trim().equals("")) {
                return printMessage(" La Banque du cheque doit être renseigne");
            }
            if (currentCheque.getAgence() == null || currentCheque.getAgence().trim().equals("")) {
                return printMessage(" L'Agence du cheque doit être renseigne");
            }
            if (currentCheque.getNumerocompte() == null || currentCheque.getNumerocompte().trim().equals("")) {
                return printMessage(" Le Numero de compte doit être renseigne");
            }
            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(currentCheque.getNumerocheque().trim())) {
                return printMessage(" Le Numero de cheque doit être renseigne par un entier");
            }
            if (currentCheque.getRibcompte() == null || currentCheque.getRibcompte().trim().equals("")) {
                return printMessage(" La cle RIB du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() != null && Character.isDigit(currentCheque.getBanque().charAt(1))) {

                return printMessage(" Le Code Banque de ce cheque est au format SICA 2.\n Il ne sera pas accepte.");
            }

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String sql = "SELECT * FROM ALL_CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEVALSURCAIENVSIB") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND BANQUE='" + currentCheque.getBanque() + "' AND AGENCE='" + currentCheque.getAgence() + "' AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "'";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                if (!cheques[0].getIdcheque().equals(currentCheque.getIdcheque())) {
                    db.close();
                    return printMessage("Ce cheque a deja ete valide et n'a pas encore ete rejete.\n"
                            + "Il reference le cheque avec IDCHEQUE = " + cheques[0].getIdcheque() + " a l'etat " + cheques[0].getEtat());
                }
            }

            HashMap<BigDecimal, String> hashMap = new HashMap(currentRemise.getNbOperation().intValue());
            sql = "SELECT * FROM CHEQUES WHERE ETAT >= " + Utility.getParam("CETAOPECOR") + "  AND REMISE=" + currentRemise.getIdremise();
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                for (int i = 0; i < cheques.length; i++) {
                    if (!cheques[i].getSequence().equals(currentCheque.getSequence())) {
                        hashMap.put(cheques[i].getSequence(), cheques[i].getMontantcheque().trim());
                    }
                }
            }

            hashMap.put(currentCheque.getSequence(), currentCheque.getMontantcheque().trim());

            int index = Integer.parseInt(request.getParameter("index"));

            currentCheque.setDevise("XOF");
            if (Character.isDigit(currentCheque.getBanque().charAt(1))) {
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    currentCheque.setEtablissement(CMPUtility.getCodeBanque());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanque());
                } else {
                    currentCheque.setEtablissement(CMPUtility.getCodeBanqueSica3());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                }
                currentCheque.setType_Cheque("030");
            } else {
                currentCheque.setEtablissement(currentRemise.getEtablissement());
                currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                currentCheque.setType_Cheque("035");
            }
            currentCheque.setAgenceremettant(currentRemise.getAgenceRemettant());
            currentCheque.setCompteremettant(currentRemise.getCompteRemettant());
            currentCheque.setRefremise(currentRemise.getReference());
            currentCheque.setEscompte(currentRemise.getEscompte());
            currentCheque.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setHeuresaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HH:mm:ss"));
            currentCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDateemission(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDevise("XOF");
            currentCheque.setNombeneficiaire(currentRemise.getNomClient());

            sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '" + currentCheque.getBanque() + "' AND CODEAGENCE LIKE '" + currentCheque.getAgence() + "'";
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                currentCheque.setVille(agences[0].getCodevillecompense());
            } else {
                //currentCheque.setVille("01");
                db.close();
                return printMessage("Le code Agence " + currentCheque.getAgence() + " n'est pas declare dans la base");
            }
            currentCheque.setVilleremettant("01");
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentCheque.setCodeutilisateur(user.getLogin().trim());

            currentCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
            currentCheque.setIndicateurmodificationcmc7(new BigDecimal(0));
            currentCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPECOR")));

            if (index > currentRemise.getNbOperation().intValue()) {
                db.close();
                return printMessage("Nombre d'operations de la Remise =" + currentRemise.getNbOperation()
                        + "\n inferieur au Nombre des cheques =" + index);
            }

            String cleribCal = Utility.computeCleRIB(currentCheque.getBanque(), currentCheque.getAgence(), currentCheque.getNumerocompte());
            if (!cleribCal.equals(currentCheque.getRibcompte())) {
                db.close();
                return printMessage("La Banque " + currentCheque.getBanque() + "\nL'Agence " + currentCheque.getAgence() + "\nLe Compte " + currentCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
            }

            long sumAmount = 0;

            String array[] = hashMap.values().toArray(new String[0]);
            for (int i = 0; i < array.length; i++) {
                sumAmount += Long.parseLong(array[i]);
            }

            if (sumAmount > Long.parseLong(currentRemise.getMontant().trim())) {
                db.close();
                return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " inferieur \nau montant courant de la somme des cheques =" + sumAmount);
            }

            if (index == currentRemise.getNbOperation().intValue()) {
                if (sumAmount < Long.parseLong(currentRemise.getMontant().trim())) {
                    db.close();
                    return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " superieur \nau montant de la somme des cheques =" + sumAmount);
                }

            }

            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentCheque, "CHEQUES", "IDCHEQUE =" + currentCheque.getIdcheque());

            if (updateRowByObjectByQuery) {

                if (index == currentRemise.getNbOperation().intValue()) {
                    if (db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', ETAT=" + Utility.getParam("CETAOPEVAL") + " WHERE ETAT=" + Utility.getParam("CETAOPECOR") + " AND REMISE=" + currentRemise.getIdremise()) == currentRemise.getNbOperation().intValue()) {
                        if (db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMVAL") + " WHERE IDREMISE=" + currentRemise.getIdremise()) == 1) {

                            db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMVAL") + " WHERE IDREMISE=" + currentRemise.getIdremise());
                        } else {
                            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                            db.close();
                            return printMessage("Erreur lors de la MAJ de le remise -" + db.getMessage());
                        }
                    } else {
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPECOR") + " WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND REMISE=" + currentRemise.getIdremise());
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                        db.close();
                        return printMessage("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());
                    }

                }

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);

        }
        db.close();
        return printMessage("CHEQUE NON OK-" + db.getMessage());

    }
    
    
    public String corrigeEffet(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            //Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemiseCorr");
            //Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentChequeCorr");
            
            RemisesEffets currentRemise = (RemisesEffets) request.getSession().getAttribute("currentRemiseCorr");
            Effets currentEffet = (Effets) request.getSession().getAttribute("currentEffetCorr");

            currentEffet.setMontant_Effet(request.getParameter("montantEffet").replaceAll("\\p{javaWhitespace}+", ""));
            currentEffet.setBanque(request.getParameter("codeBanque").toUpperCase(Locale.ENGLISH));
            currentEffet.setAgence(Utility.bourrageGZero(request.getParameter("codeAgence"), 5));
            currentEffet.setNumerocompte_Tire(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
            currentEffet.setNumeroeffet(request.getParameter("numeroEffet"));
            currentEffet.setRibcompte(Utility.bourrageGZero(request.getParameter("clerib"), 2));
            //currentCheque.setNomemetteur(request.getParameter("nomTire"));
            currentEffet.setNom_Tire(request.getParameter("nomTire"));
            //currentCheque.setDateemission(request.getParameter("dateEmis"));
            currentEffet.setDate_Creation(request.getParameter("dateCrea"));
            currentEffet.setDate_Echeance(request.getParameter("dateEch"));
            
            System.out.println("currentRemise.getTypeRemise()  "+ currentRemise.getTypeRemise() +"  request.getParameter(\"typeEffet\") "+request.getParameter("typeEffet"));
            currentEffet.setType_Effet(request.getParameter("typeEffet"));
          // currentEffet.setType_Effet(currentRemise.getTypeRemise());
            
            currentEffet.setMontant_Frais(request.getParameter("montantfrais"));
            
            String forcage = request.getParameter("forcage");
            String datvalj1 = request.getParameter("datvalj1");
            String escompte = request.getParameter("escompte");
            

            if (currentEffet.getType_Effet() == null || currentEffet.getType_Effet().trim().equals("")) {
                return printMessage(" Le type de  l'effet doit être renseigne");
            }

            if (currentEffet.getMontant_Effet() == null || currentEffet.getMontant_Effet().trim().equals("")) {
                return printMessage(" Le Montant de l'effet doit être renseigne");
            }
            if (!currentEffet.getMontant_Effet().matches("[0-9]+")) {
                return printMessage(" Le Montant de l'effet  ne doit contenir que des chiffres");
            }

            if (currentEffet.getNumeroeffet()== null || currentEffet.getNumeroeffet().trim().equals("")) {
                return printMessage(" Le Numero de l'effet  doit être renseigne");
            }
            if (!currentEffet.getNumeroeffet().matches("[0-9]+")) {
                return printMessage(" Le Numero de l'effet ne doit contenir que des chiffres");
            }
            if (currentEffet.getBanque() == null || currentEffet.getBanque().trim().equals("")) {
                return printMessage(" La Banque de l'effet doit être renseigne");
            }
            if (currentEffet.getAgence() == null || currentEffet.getBanque().trim().equals("")) {
                return printMessage(" L'Agence de l'effet doit être renseigne");
            }
            if (!currentEffet.getAgence().matches("[0-9]+")) {
                return printMessage(" L'Agence de l'effet ne doit contenir que des chiffres");
            }
            if (currentEffet.getNumerocompte_Tire()== null || currentEffet.getNumerocompte_Tire().trim().equals("")) {
                return printMessage(" Le Numero de compte doit être renseigne");
            }
            if (!currentEffet.getNumerocompte_Tire().matches("[0-9]+")) {
                return printMessage(" Le Numero de compte ne doit contenir que des chiffres");
            }
            if (currentEffet.getNumeroeffet() == null || currentEffet.getNumeroeffet().trim().equals("") || !Utility.isInteger(currentEffet.getNumeroeffet().trim())) {
                return printMessage(" Le Numero de l'effet doit être renseigne par un entier");
            }
            if (currentEffet.getRibcompte() == null || currentEffet.getRibcompte().trim().equals("")) {
                return printMessage(" La cle RIB de l'effet doit être renseigne");
            }
            if (currentEffet.getBanque() != null && Character.isDigit(currentEffet.getBanque().charAt(1))) {

                return printMessage(" Le Code Banque de cet effet est au format SICA 2.\n Il ne sera pas accepte.");
            }

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

           // String sql = "SELECT * FROM ALL_CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEVALSURCAIENVSIB") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND BANQUE='" + currentCheque.getBanque() + "' AND AGENCE='" + currentCheque.getAgence() + "' AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "'";
            //Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            
             String sql = "SELECT * FROM ALL_EFFETS WHERE ETAT IN (" + Utility.getParam("CETAOPERETRECENVSIBVER") + "," + Utility.getParam("CETAOPERET") + ","
                    + "" + Utility.getParam("CETAOPERETENVSIB") + "," + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERETRECENVSIB") + ","
                    + "" + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + ","
                    + "" + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + ","
                    + "" + Utility.getParam("CETAOPEVAL2") + "," + Utility.getParam("CETAOPEVALSURCAIENVSIB") + "," + Utility.getParam("CETAOPESUPVALSURCAI") + ","
                    + "" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + "," + Utility.getParam("CETAOPEVALDELTA") + ") AND BANQUE='" + currentEffet.getBanque() + "'"
                    + "  AND AGENCE='" + currentEffet.getAgence() + "' AND NUMEROEFFET='" + currentEffet.getNumeroeffet() + "' AND NUMEROCOMPTE_TIRE='" + currentEffet.getNumerocompte_Tire() + "'";
            Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
            //currentEffet.setCoderepresentation(BigDecimal.ZERO);
            currentEffet.setCoderepresentation(BigDecimal.ZERO);
            if (effets != null && effets.length > 0) {
                if (!effets[0].getIdeffet().equals(currentEffet.getIdeffet())) {
                    db.close();
                    return printMessage("Cet effet a deja ete valide et n'a pas encore ete rejete.\n"
                            + "Il reference l'effet avec IDEFFET = " + effets[0].getIdeffet()+ " a l'etat " + effets[0].getEtat());
                }
            }

            HashMap<BigDecimal, String> hashMap = new HashMap(currentRemise.getNbOperation().intValue());
            sql = "SELECT * FROM EFFETS WHERE ETAT >= " + Utility.getParam("CETAOPECOR") + "  AND REMISE=" + currentRemise.getIdremise();
            effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
            if (effets != null && effets.length > 0) {
                for (int i = 0; i < effets.length; i++) {
                    if (!effets[i].getSequence().equals(currentEffet.getSequence())) {
                        hashMap.put(effets[i].getSequence(), effets[i].getMontant_Effet().trim());
                    }
                }
            }

            hashMap.put(currentEffet.getSequence(), currentEffet.getMontant_Effet().trim());

            int index = Integer.parseInt(request.getParameter("index"));

            currentEffet.setDevise("XOF");
              //same 
            if (Character.isDigit(currentEffet.getBanque().charAt(1))) {
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    currentEffet.setEtablissement(currentRemise.getAgenceDepot());
                    currentEffet.setBanqueremettant(CMPUtility.getCodeBanque());
                } else {
                    currentEffet.setEtablissement(currentRemise.getAgenceDepot());
                    currentEffet.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                }
                currentEffet.setType_Effet(request.getParameter("typeEffet"));
            } else {
                currentEffet.setEtablissement(currentRemise.getAgenceDepot());
                currentEffet.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                currentEffet.setType_Effet(request.getParameter("typeEffet"));
            }
            
            currentEffet.setAgenceremettant(currentRemise.getAgenceRemettant());
            currentEffet.setNumerocompte_Beneficiaire(currentRemise.getCompteRemettant());
            currentEffet.setRefremise(currentRemise.getReference());
            
            
            currentEffet.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentEffet.setHeuresaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HH:mm:ss"));
            currentEffet.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            //currentCheque.setDateemission(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));

            currentEffet.setDevise("XOF");
            currentEffet.setNom_Beneficiaire(currentRemise.getNomClient());
            sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '" + currentEffet.getBanque() + "' AND CODEAGENCE LIKE '" + currentEffet.getAgence() + "'";
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                currentEffet.setVille(agences[0].getCodevillecompense());
            } else {
                //currentCheque.setVille("01");
                db.close();
                return printMessage("Le code Agence " + currentEffet.getAgence() + " n'est pas declare dans la base");
            }
            
            currentEffet.setVille("01");
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentEffet.setCodeUtilisateur(user.getLogin().trim());

            

            currentEffet.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
            currentEffet.setIndicateurmodificationcmc7(new BigDecimal(0));
            currentEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPECOR")));

            if (index > currentRemise.getNbOperation().intValue()) {
                db.close();
                return printMessage("Nombre d'operations de la Remise =" + currentRemise.getNbOperation()
                        + "\n inferieur au Nombre des effets =" + index);
            }

            //String cleribCal = Utility.computeCleRIB(currentEffet.getBanque(), currentEffet.getAgence(), currentEffet.getNumerocompte());
            String cleribCal = Utility.computeCleRIB(currentEffet.getBanque(), currentEffet.getAgence(),currentEffet.getNumerocompte_Tire());
            if (!cleribCal.equals(currentEffet.getRibcompte())) {
                db.close();
                //return printMessage("La Banque " + currentCheque.getBanque() + "\nL'Agence " + currentCheque.getAgence() + "\nLe Compte " + currentCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
                return printMessage("La Banque " + currentEffet.getBanque() + "\nL'Agence " + currentEffet.getAgence() + "\nLe Compte " + currentEffet.getNumerocompte_Tire() + " donne la cle rib " + cleribCal);
            
            }

            long sumAmount = 0;

            String array[] = hashMap.values().toArray(new String[0]);
            for (int i = 0; i < array.length; i++) {
                sumAmount += Long.parseLong(array[i]);
            }
            
             if (sumAmount > Long.parseLong(currentRemise.getMontant().trim())) {
                db.close();
                return printMessage("Montant de la Remise =" + Utility.formatNumber(currentRemise.getMontant()) + " inferieur \nau montant courant de la somme des effets =" + Utility.formatNumber("" + sumAmount));
            }

            if (index == currentRemise.getNbOperation().intValue()) {
                if (sumAmount < Long.parseLong(currentRemise.getMontant().trim())) {
                    db.close();
                    return printMessage("Montant de la Remise =" + Utility.formatNumber(currentRemise.getMontant()) + " superieur \nau montant de la somme des effets =" + Utility.formatNumber("" + sumAmount));
                }

            }

            //boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentCheque, "CHEQUES", "IDCHEQUE =" + currentCheque.getIdcheque());
            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentEffet, "EFFETS", "IDEFFET =" + currentEffet.getIdeffet());

            if (updateRowByObjectByQuery) {

                if (index == currentRemise.getNbOperation().intValue()) {
                    if (db.executeUpdate("UPDATE EFFETS SET NUMEROCOMPTE_BENEFICIAIRE='" + currentRemise.getCompteRemettant() + "', ETAT=" + Utility.getParam("CETAOPEVAL") + " WHERE ETAT=" + Utility.getParam("CETAOPECOR") + " AND REMISE=" + currentRemise.getIdremise()) == currentRemise.getNbOperation().intValue()) {
                        if (db.executeUpdate("UPDATE REMISESEFFETS SET ETAT=" + Utility.getParam("CETAREMVAL") + " WHERE IDREMISE=" + currentRemise.getIdremise()) == 1) {

                            db.executeUpdate("UPDATE REMISESEFFETS SET ETAT=" + Utility.getParam("CETAREMVAL") + " WHERE IDREMISE=" + currentRemise.getIdremise());
                        } else {
                            db.executeUpdate("UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE IDEFFET=" + currentEffet.getIdeffet());
                            db.close();
                            return printMessage("Erreur lors de la MAJ de le remise -" + db.getMessage());
                        }
                    } else {
                        db.executeUpdate("UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPECOR") + " WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND REMISE=" + currentRemise.getIdremise());
                        db.executeUpdate("UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE IDEFFET=" + currentEffet.getIdeffet());
                        db.close();
                        return printMessage("Erreur lors de MAJ des effets de la remise -" + db.getMessage());
                    }

                }

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);

        }
        db.close();
        return printMessage("EFFETS NON OK-" + db.getMessage());

    }

    public String corrigeChequeLite(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemiseCorr");
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentChequeCorr");
            currentCheque.setMontantcheque(request.getParameter("montantCheque").replaceAll("\\p{javaWhitespace}+", ""));
            currentCheque.setBanque(request.getParameter("codeBanque").toUpperCase(Locale.ENGLISH));
            currentCheque.setAgence(Utility.bourrageGZero(request.getParameter("codeAgence"), 5));
            currentCheque.setNumerocompte(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
            currentCheque.setNumerocheque(Utility.bourrageGZero(request.getParameter("numeroCheque"), 7));
            currentCheque.setRibcompte(Utility.bourrageGZero(request.getParameter("clerib"), 2));
            currentCheque.setNomemetteur(request.getParameter("nomTire"));
            currentCheque.setDateemission(request.getParameter("dateEmis"));

            if (currentCheque.getMontantcheque() == null || currentCheque.getMontantcheque().trim().equals("")) {
                return printMessage(" Le Montant du cheque doit être renseigne");
            }
            if (!currentCheque.getMontantcheque().matches("[0-9]+")) {
                return printMessage(" Le Montant du cheque ne doit contenir que des chiffres");
            }

            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("")) {
                return printMessage(" Le Numero du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() == null || currentCheque.getBanque().trim().equals("")) {
                return printMessage(" La Banque du cheque doit être renseigne");
            }
            if (currentCheque.getAgence() == null || currentCheque.getAgence().trim().equals("")) {
                return printMessage(" L'Agence du cheque doit être renseigne");
            }
            if (currentCheque.getNumerocompte() == null || currentCheque.getNumerocompte().trim().equals("")) {
                return printMessage(" Le Numero de compte doit être renseigne");
            }
            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(currentCheque.getNumerocheque().trim())) {
                return printMessage(" Le Numero de cheque doit être renseigne par un entier");
            }
            if (currentCheque.getRibcompte() == null || currentCheque.getRibcompte().trim().equals("")) {
                return printMessage(" La cle RIB du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() != null && Character.isDigit(currentCheque.getBanque().charAt(1))) {

                return printMessage(" Le Code Banque de ce cheque est au format SICA 2.\n Il ne sera pas accepte.");
            }

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String sql = "SELECT * FROM ALL_CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEVALSURCAIENVSIB") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND BANQUE='" + currentCheque.getBanque() + "' AND AGENCE='" + currentCheque.getAgence() + "' AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "'";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                if (!cheques[0].getIdcheque().equals(currentCheque.getIdcheque())) {
                    db.close();
                    return printMessage("Ce cheque a deja ete valide et n'a pas encore ete rejete.\n"
                            + "Il reference le cheque avec IDCHEQUE = " + cheques[0].getIdcheque() + " a l'etat " + cheques[0].getEtat());
                }
            }

            HashMap<BigDecimal, String> hashMap = new HashMap(currentRemise.getNbOperation().intValue());
            sql = "SELECT * FROM CHEQUES WHERE ETAT >= " + Utility.getParam("CETAOPECOR") + "  AND REMISE=" + currentRemise.getIdremise();
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                for (int i = 0; i < cheques.length; i++) {
                    if (!cheques[i].getSequence().equals(currentCheque.getSequence())) {
                        hashMap.put(cheques[i].getSequence(), cheques[i].getMontantcheque().trim());
                    }
                }
            }

            hashMap.put(currentCheque.getSequence(), currentCheque.getMontantcheque().trim());

            int index = Integer.parseInt(request.getParameter("index"));

            currentCheque.setDevise("XOF");
            if (Character.isDigit(currentCheque.getBanque().charAt(1))) {
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    currentCheque.setEtablissement(CMPUtility.getCodeBanque());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanque());
                } else {
                    currentCheque.setEtablissement(CMPUtility.getCodeBanqueSica3());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                }
                currentCheque.setType_Cheque("030");
            } else {
                currentCheque.setEtablissement(currentRemise.getEtablissement());
                currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                currentCheque.setType_Cheque("035");
            }
            currentCheque.setAgenceremettant(currentRemise.getAgenceRemettant());
            currentCheque.setCompteremettant(currentRemise.getCompteRemettant());
            currentCheque.setRefremise(currentRemise.getReference());
            currentCheque.setEscompte(currentRemise.getEscompte());
            currentCheque.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setHeuresaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HH:mm:ss"));
            currentCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDateemission(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDevise("XOF");
            currentCheque.setNombeneficiaire(currentRemise.getNomClient());

            sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '" + currentCheque.getBanque() + "' AND CODEAGENCE LIKE '" + currentCheque.getAgence() + "'";
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                currentCheque.setVille(agences[0].getCodevillecompense());
            } else {
                //currentCheque.setVille("01");
                db.close();
                return printMessage("Le code Agence " + currentCheque.getAgence() + " n'est pas declare dans la base");
            }
            currentCheque.setVilleremettant("01");
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentCheque.setCodeutilisateur(user.getLogin().trim());

            currentCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
            currentCheque.setIndicateurmodificationcmc7(new BigDecimal(0));
            currentCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPECOR")));

            if (index > currentRemise.getNbOperation().intValue()) {
                db.close();
                return printMessage("Nombre d'operations de la Remise =" + currentRemise.getNbOperation()
                        + "\n inferieur au Nombre des cheques =" + index);
            }

            String cleribCal = Utility.computeCleRIB(currentCheque.getBanque(), currentCheque.getAgence(), currentCheque.getNumerocompte());
            if (!cleribCal.equals(currentCheque.getRibcompte())) {
                db.close();
                return printMessage("La Banque " + currentCheque.getBanque() + "\nL'Agence " + currentCheque.getAgence() + "\nLe Compte " + currentCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
            }

            long sumAmount = 0;

            String array[] = hashMap.values().toArray(new String[0]);
            for (int i = 0; i < array.length; i++) {
                sumAmount += Long.parseLong(array[i]);
            }

            if (sumAmount > Long.parseLong(currentRemise.getMontant().trim())) {
                db.close();
                return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " inferieur \nau montant courant de la somme des cheques =" + sumAmount);
            }

            if (index == currentRemise.getNbOperation().intValue()) {
                if (sumAmount < Long.parseLong(currentRemise.getMontant().trim())) {
                    db.close();
                    return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " superieur \nau montant de la somme des cheques =" + sumAmount);
                }

            }

            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentCheque, "CHEQUES", "IDCHEQUE =" + currentCheque.getIdcheque());

            if (updateRowByObjectByQuery) {

                if (index == currentRemise.getNbOperation().intValue()) {
                    if (db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', ETAT=" + Utility.getParam("CETAOPEVALITE") + " WHERE ETAT=" + Utility.getParam("CETAOPECOR") + " AND REMISE=" + currentRemise.getIdremise()) == currentRemise.getNbOperation().intValue()) {
                        if (db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEVALITE") + " WHERE IDREMISE=" + currentRemise.getIdremise()) == 1) {

                            db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEVALITE") + " WHERE IDREMISE=" + currentRemise.getIdremise());
                        } else {
                            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                            db.close();
                            return printMessage("Erreur lors de la MAJ de le remise -" + db.getMessage());
                        }
                    } else {
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPECOR") + " WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND REMISE=" + currentRemise.getIdremise());
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                        db.close();
                        return printMessage("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());
                    }

                }

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);

        }
        db.close();
        return printMessage("CHEQUE NON OK-" + db.getMessage());

    }

    public String valideChequeLite(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemiseCorr");
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentChequeCorr");
            currentCheque.setMontantcheque(request.getParameter("montantCheque").replaceAll("\\p{javaWhitespace}+", ""));
            currentCheque.setBanque(request.getParameter("codeBanque").toUpperCase(Locale.ENGLISH));
            currentCheque.setAgence(Utility.bourrageGZero(request.getParameter("codeAgence"), 5));
            currentCheque.setNumerocompte(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
            currentCheque.setNumerocheque(Utility.bourrageGZero(request.getParameter("numeroCheque"), 7));
            currentCheque.setRibcompte(Utility.bourrageGZero(request.getParameter("clerib"), 2));
            currentCheque.setNomemetteur(request.getParameter("nomTire"));
            currentCheque.setDateemission(request.getParameter("dateEmis"));

            if (currentCheque.getMontantcheque() == null || currentCheque.getMontantcheque().trim().equals("")) {
                return printMessage(" Le Montant du cheque doit être renseigne");
            }
            if (!currentCheque.getMontantcheque().matches("[0-9]+")) {
                return printMessage(" Le Montant du cheque ne doit contenir que des chiffres");
            }

            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("")) {
                return printMessage(" Le Numero du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() == null || currentCheque.getBanque().trim().equals("")) {
                return printMessage(" La Banque du cheque doit être renseigne");
            }
            if (currentCheque.getAgence() == null || currentCheque.getAgence().trim().equals("")) {
                return printMessage(" L'Agence du cheque doit être renseigne");
            }
            if (currentCheque.getNumerocompte() == null || currentCheque.getNumerocompte().trim().equals("")) {
                return printMessage(" Le Numero de compte doit être renseigne");
            }
            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(currentCheque.getNumerocheque().trim())) {
                return printMessage(" Le Numero de cheque doit être renseigne par un entier");
            }
            if (currentCheque.getRibcompte() == null || currentCheque.getRibcompte().trim().equals("")) {
                return printMessage(" La cle RIB du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() != null && Character.isDigit(currentCheque.getBanque().charAt(1))) {

                return printMessage(" Le Code Banque de ce cheque est au format SICA 2.\n Il ne sera pas accepte.");
            }

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String sql = "SELECT * FROM ALL_CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEVALSURCAIENVSIB") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND BANQUE='" + currentCheque.getBanque() + "' AND AGENCE='" + currentCheque.getAgence() + "' AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "'";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                if (!cheques[0].getIdcheque().equals(currentCheque.getIdcheque())) {
                    db.close();
                    return printMessage("Ce cheque a deja ete valide et n'a pas encore ete rejete.\n"
                            + "Il reference le cheque avec IDCHEQUE = " + cheques[0].getIdcheque() + " a l'etat " + cheques[0].getEtat());
                }
            }

            HashMap<BigDecimal, String> hashMap = new HashMap(currentRemise.getNbOperation().intValue());
            sql = "SELECT * FROM CHEQUES WHERE ETAT >= " + Utility.getParam("CETAOPEVAL2") + "  AND REMISE=" + currentRemise.getIdremise();
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                for (int i = 0; i < cheques.length; i++) {
                    if (!cheques[i].getSequence().equals(currentCheque.getSequence())) {
                        hashMap.put(cheques[i].getSequence(), cheques[i].getMontantcheque().trim());
                    }
                }
            }

            hashMap.put(currentCheque.getSequence(), currentCheque.getMontantcheque().trim());

            int index = Integer.parseInt(request.getParameter("index"));

            currentCheque.setDevise("XOF");
            if (Character.isDigit(currentCheque.getBanque().charAt(1))) {
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    currentCheque.setEtablissement(CMPUtility.getCodeBanque());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanque());
                } else {
                    currentCheque.setEtablissement(currentRemise.getEtablissement());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                }
                currentCheque.setType_Cheque("030");
            } else {
                currentCheque.setEtablissement(currentRemise.getEtablissement());
                currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                currentCheque.setType_Cheque("035");
            }
            currentCheque.setAgenceremettant(currentRemise.getAgenceRemettant());
            currentCheque.setCompteremettant(currentRemise.getCompteRemettant());
            currentCheque.setRefremise(currentRemise.getReference());
            currentCheque.setEscompte(currentRemise.getEscompte());
            currentCheque.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setHeuresaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HH:mm:ss"));
            currentCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDateemission(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDevise("XOF");
            currentCheque.setNombeneficiaire(currentRemise.getNomClient());

            sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '" + currentCheque.getBanque() + "' AND CODEAGENCE LIKE '" + currentCheque.getAgence() + "'";
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                currentCheque.setVille(agences[0].getCodevillecompense());
            } else {
                //currentCheque.setVille("01");
                db.close();
                return printMessage("Le code Agence " + currentCheque.getAgence() + " n'est pas declare dans la base");
            }
            currentCheque.setVilleremettant("01");
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");

            currentCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
            currentCheque.setIndicateurmodificationcmc7(new BigDecimal(0));
            currentCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL2")));

            if (index > currentRemise.getNbOperation().intValue()) {
                db.close();
                return printMessage("Nombre d'operations de la Remise =" + currentRemise.getNbOperation()
                        + "\n inferieur au Nombre des cheques =" + index);
            }

            String cleribCal = Utility.computeCleRIB(currentCheque.getBanque(), currentCheque.getAgence(), currentCheque.getNumerocompte());
            if (!cleribCal.equals(currentCheque.getRibcompte())) {
                db.close();
                return printMessage("La Banque " + currentCheque.getBanque() + "\nL'Agence " + currentCheque.getAgence() + "\nLe Compte " + currentCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
            }

            long sumAmount = 0;

            String array[] = hashMap.values().toArray(new String[0]);
            for (int i = 0; i < array.length; i++) {
                sumAmount += Long.parseLong(array[i]);
            }

            if (sumAmount > Long.parseLong(currentRemise.getMontant().trim())) {
                db.close();
                return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " inferieur \nau montant courant de la somme des cheques =" + sumAmount);
            }

            if (index == currentRemise.getNbOperation().intValue()) {
                if (sumAmount < Long.parseLong(currentRemise.getMontant().trim())) {
                    db.close();
                    return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " superieur \nau montant de la somme des cheques =" + sumAmount);
                }

            }

            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentCheque, "CHEQUES", "IDCHEQUE =" + currentCheque.getIdcheque());

            if (updateRowByObjectByQuery) {

                if (index == currentRemise.getNbOperation().intValue()) {
                    if (db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', ETAT=" + Utility.getParam("CETAOPEVALDELTA") + ""
                            + " WHERE ETAT=" + Utility.getParam("CETAOPEVAL2") + " AND REMISE=" + currentRemise.getIdremise()) == currentRemise.getNbOperation().intValue()) {
                        if (db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEVALDELTA") + " WHERE IDREMISE=" + currentRemise.getIdremise()) == 1) {
                            if (Utility.getParam("VALIDATION_CHEQUE_AUTO") != null && Utility.getParam("VALIDATION_CHEQUE_AUTO").equalsIgnoreCase("AUTO")) {
                                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM1") + " WHERE REMISE=" + currentRemise.getIdremise() + " AND BANQUE <> BANQUEREMETTANT");
                                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " WHERE REMISE=" + currentRemise.getIdremise() + " AND BANQUE = BANQUEREMETTANT"); //                               
                                db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEVALDELTA") + " WHERE IDREMISE=" + currentRemise.getIdremise());
                            }

                        } else {
                            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVAL") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                            db.close();
                            return printMessage("Erreur lors de la MAJ de le remise -" + db.getMessage());
                        }
                    } else {
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVAL") + " WHERE ETAT=" + Utility.getParam("CETAOPEVALDELTA") + " AND REMISE=" + currentRemise.getIdremise());
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVAL") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                        db.close();
                        return printMessage("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());
                    }

                }

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);

        }
        db.close();
        return printMessage("CHEQUE NON OK-" + db.getMessage());

    }

    public String valideChequeLiteRNC(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemiseCorr");
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentChequeCorr");
            currentCheque.setMontantcheque(request.getParameter("montantCheque").replaceAll("\\p{javaWhitespace}+", ""));
            currentCheque.setBanque(request.getParameter("codeBanque").toUpperCase(Locale.ENGLISH));
            currentCheque.setAgence(Utility.bourrageGZero(request.getParameter("codeAgence"), 5));
            currentCheque.setNumerocompte(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
            currentCheque.setNumerocheque(Utility.bourrageGZero(request.getParameter("numeroCheque"), 7));
            currentCheque.setRibcompte(Utility.bourrageGZero(request.getParameter("clerib"), 2));
            currentCheque.setNomemetteur(request.getParameter("nomTire"));
            currentCheque.setDateemission(request.getParameter("dateEmis"));

            if (currentCheque.getMontantcheque() == null || currentCheque.getMontantcheque().trim().equals("")) {
                return printMessage(" Le Montant du cheque doit être renseigne");
            }
            if (!currentCheque.getMontantcheque().matches("[0-9]+")) {
                return printMessage(" Le Montant du cheque ne doit contenir que des chiffres");
            }

            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("")) {
                return printMessage(" Le Numero du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() == null || currentCheque.getBanque().trim().equals("")) {
                return printMessage(" La Banque du cheque doit être renseigne");
            }
            if (currentCheque.getAgence() == null || currentCheque.getAgence().trim().equals("")) {
                return printMessage(" L'Agence du cheque doit être renseigne");
            }
            if (currentCheque.getNumerocompte() == null || currentCheque.getNumerocompte().trim().equals("")) {
                return printMessage(" Le Numero de compte doit être renseigne");
            }
            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(currentCheque.getNumerocheque().trim())) {
                return printMessage(" Le Numero de cheque doit être renseigne par un entier");
            }
            if (currentCheque.getRibcompte() == null || currentCheque.getRibcompte().trim().equals("")) {
                return printMessage(" La cle RIB du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() != null && Character.isDigit(currentCheque.getBanque().charAt(1))) {

                return printMessage(" Le Code Banque de ce cheque est au format SICA 2.\n Il ne sera pas accepte.");
            }

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String sql = "SELECT * FROM ALL_CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEVALSURCAIENVSIB") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND BANQUE='" + currentCheque.getBanque() + "' AND AGENCE='" + currentCheque.getAgence() + "' AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "'";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                if (!cheques[0].getIdcheque().equals(currentCheque.getIdcheque())) {
                    db.close();
                    return printMessage("Ce cheque a deja ete valide et n'a pas encore ete rejete.\n"
                            + "Il reference le cheque avec IDCHEQUE = " + cheques[0].getIdcheque() + " a l'etat " + cheques[0].getEtat());
                }
            }

            HashMap<BigDecimal, String> hashMap = new HashMap(currentRemise.getNbOperation().intValue());
            sql = "SELECT * FROM CHEQUES WHERE ETAT >= " + Utility.getParam("CETAOPEVALDELTA2") + "  AND REMISE=" + currentRemise.getIdremise();
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                for (int i = 0; i < cheques.length; i++) {
                    if (!cheques[i].getSequence().equals(currentCheque.getSequence())) {
                        hashMap.put(cheques[i].getSequence(), cheques[i].getMontantcheque().trim());
                    }
                }
            }

            hashMap.put(currentCheque.getSequence(), currentCheque.getMontantcheque().trim());

            int index = Integer.parseInt(request.getParameter("index"));

            currentCheque.setDevise("XOF");
            if (Character.isDigit(currentCheque.getBanque().charAt(1))) {
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    currentCheque.setEtablissement(CMPUtility.getCodeBanque());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanque());
                } else {
                    currentCheque.setEtablissement(currentRemise.getEtablissement());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                }
                currentCheque.setType_Cheque("030");
            } else {
                currentCheque.setEtablissement(currentRemise.getEtablissement());
                currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                currentCheque.setType_Cheque("035");
            }
            currentCheque.setAgenceremettant(currentRemise.getAgenceRemettant());
            currentCheque.setCompteremettant(currentRemise.getCompteRemettant());
            currentCheque.setRefremise(currentRemise.getReference());
            currentCheque.setEscompte(currentRemise.getEscompte());
            currentCheque.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setHeuresaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HH:mm:ss"));
            currentCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDateemission(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDevise("XOF");
            currentCheque.setNombeneficiaire(currentRemise.getNomClient());

            sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '" + currentCheque.getBanque() + "' AND CODEAGENCE LIKE '" + currentCheque.getAgence() + "'";
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                currentCheque.setVille(agences[0].getCodevillecompense());
            } else {
                //currentCheque.setVille("01");
                db.close();
                return printMessage("Le code Agence " + currentCheque.getAgence() + " n'est pas declare dans la base");
            }
            currentCheque.setVilleremettant("01");
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");

            currentCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
            currentCheque.setIndicateurmodificationcmc7(new BigDecimal(0));
            currentCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALDELTA2")));

            if (index > currentRemise.getNbOperation().intValue()) {
                db.close();
                return printMessage("Nombre d'operations de la Remise =" + currentRemise.getNbOperation()
                        + "\n inferieur au Nombre des cheques =" + index);
            }

            String cleribCal = Utility.computeCleRIB(currentCheque.getBanque(), currentCheque.getAgence(), currentCheque.getNumerocompte());
            if (!cleribCal.equals(currentCheque.getRibcompte())) {
                db.close();
                return printMessage("La Banque " + currentCheque.getBanque() + "\nL'Agence " + currentCheque.getAgence() + "\nLe Compte " + currentCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
            }

            long sumAmount = 0;

            String array[] = hashMap.values().toArray(new String[0]);
            for (int i = 0; i < array.length; i++) {
                sumAmount += Long.parseLong(array[i]);
            }

            if (sumAmount > Long.parseLong(currentRemise.getMontant().trim())) {
                db.close();
                return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " inferieur \nau montant courant de la somme des cheques =" + sumAmount);
            }

            if (index == currentRemise.getNbOperation().intValue()) {
                if (sumAmount < Long.parseLong(currentRemise.getMontant().trim())) {
                    db.close();
                    return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " superieur \nau montant de la somme des cheques =" + sumAmount);
                }

            }

            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentCheque, "CHEQUES", "IDCHEQUE =" + currentCheque.getIdcheque());

            if (updateRowByObjectByQuery) {

                if (index == currentRemise.getNbOperation().intValue()) {
                    if (db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + ""
                            + " WHERE ETAT=" + Utility.getParam("CETAOPEVALDELTA2") + " AND REMISE=" + currentRemise.getIdremise()) == currentRemise.getNbOperation().intValue()) {
                        if (db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPESUPVAL") + " WHERE IDREMISE=" + currentRemise.getIdremise()) == 1) {
                            if (Utility.getParam("VALIDATION_CHEQUE_AUTO") != null && Utility.getParam("VALIDATION_CHEQUE_AUTO").equalsIgnoreCase("AUTO")) {
                                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM1") + " WHERE REMISE=" + currentRemise.getIdremise() + " AND BANQUE <> BANQUEREMETTANT");
                                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " WHERE REMISE=" + currentRemise.getIdremise() + " AND BANQUE = BANQUEREMETTANT"); //                               
                                db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEVALDELTA") + " WHERE IDREMISE=" + currentRemise.getIdremise());
                            }

                        } else {
                            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALDELTA") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                            db.close();
                            return printMessage("Erreur lors de la MAJ de le remise -" + db.getMessage());
                        }
                    } else {
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALDELTA") + " WHERE ETAT=" + Utility.getParam("CETAOPEVALDELTA2") + " AND REMISE=" + currentRemise.getIdremise());
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALDELTA") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                        db.close();
                        return printMessage("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());
                    }

                }

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);

        }
        db.close();
        return printMessage("CHEQUE NON OK-" + db.getMessage());

    }

    public String valideChequeLiteRC(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemiseCorr");
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentChequeCorr");
            currentCheque.setMontantcheque(request.getParameter("montantCheque").replaceAll("\\p{javaWhitespace}+", ""));
            currentCheque.setBanque(request.getParameter("codeBanque").toUpperCase(Locale.ENGLISH));
            currentCheque.setAgence(Utility.bourrageGZero(request.getParameter("codeAgence"), 5));
            currentCheque.setNumerocompte(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
            currentCheque.setNumerocheque(Utility.bourrageGZero(request.getParameter("numeroCheque"), 7));
            currentCheque.setRibcompte(Utility.bourrageGZero(request.getParameter("clerib"), 2));
            currentCheque.setNomemetteur(request.getParameter("nomTire"));
            currentCheque.setDateemission(request.getParameter("dateEmis"));

            if (currentCheque.getMontantcheque() == null || currentCheque.getMontantcheque().trim().equals("")) {
                return printMessage(" Le Montant du cheque doit être renseigne");
            }
            if (!currentCheque.getMontantcheque().matches("[0-9]+")) {
                return printMessage(" Le Montant du cheque ne doit contenir que des chiffres");
            }

            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("")) {
                return printMessage(" Le Numero du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() == null || currentCheque.getBanque().trim().equals("")) {
                return printMessage(" La Banque du cheque doit être renseigne");
            }
            if (currentCheque.getAgence() == null || currentCheque.getAgence().trim().equals("")) {
                return printMessage(" L'Agence du cheque doit être renseigne");
            }
            if (currentCheque.getNumerocompte() == null || currentCheque.getNumerocompte().trim().equals("")) {
                return printMessage(" Le Numero de compte doit être renseigne");
            }
            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(currentCheque.getNumerocheque().trim())) {
                return printMessage(" Le Numero de cheque doit être renseigne par un entier");
            }
            if (currentCheque.getRibcompte() == null || currentCheque.getRibcompte().trim().equals("")) {
                return printMessage(" La cle RIB du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() != null && Character.isDigit(currentCheque.getBanque().charAt(1))) {

                return printMessage(" Le Code Banque de ce cheque est au format SICA 2.\n Il ne sera pas accepte.");
            }

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String sql = "SELECT * FROM ALL_CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEVALSURCAIENVSIB") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND BANQUE='" + currentCheque.getBanque() + "' AND AGENCE='" + currentCheque.getAgence() + "' AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "'";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                if (!cheques[0].getIdcheque().equals(currentCheque.getIdcheque())) {
                    db.close();
                    return printMessage("Ce cheque a deja ete valide et n'a pas encore ete rejete.\n"
                            + "Il reference le cheque avec IDCHEQUE = " + cheques[0].getIdcheque() + " a l'etat " + cheques[0].getEtat());
                }
            }

            HashMap<BigDecimal, String> hashMap = new HashMap(currentRemise.getNbOperation().intValue());
            sql = "SELECT * FROM CHEQUES WHERE ETAT >= " + Utility.getParam("CETAOPEVALDELTA2") + "  AND REMISE=" + currentRemise.getIdremise();
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                for (int i = 0; i < cheques.length; i++) {
                    if (!cheques[i].getSequence().equals(currentCheque.getSequence())) {
                        hashMap.put(cheques[i].getSequence(), cheques[i].getMontantcheque().trim());
                    }
                }
            }

            hashMap.put(currentCheque.getSequence(), currentCheque.getMontantcheque().trim());

            int index = Integer.parseInt(request.getParameter("index"));

            currentCheque.setDevise("XOF");
            if (Character.isDigit(currentCheque.getBanque().charAt(1))) {
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    currentCheque.setEtablissement(CMPUtility.getCodeBanque());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanque());
                } else {
                    currentCheque.setEtablissement(currentRemise.getEtablissement());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                }
                currentCheque.setType_Cheque("030");
            } else {
                currentCheque.setEtablissement(currentRemise.getEtablissement());
                currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                currentCheque.setType_Cheque("035");
            }
            currentCheque.setAgenceremettant(currentRemise.getAgenceRemettant());
            currentCheque.setCompteremettant(currentRemise.getCompteRemettant());
            currentCheque.setRefremise(currentRemise.getReference());
            currentCheque.setEscompte(currentRemise.getEscompte());
            currentCheque.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setHeuresaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HH:mm:ss"));
            currentCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDateemission(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDevise("XOF");
            currentCheque.setNombeneficiaire(currentRemise.getNomClient());

            sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '" + currentCheque.getBanque() + "' AND CODEAGENCE LIKE '" + currentCheque.getAgence() + "'";
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                currentCheque.setVille(agences[0].getCodevillecompense());
            } else {
                //currentCheque.setVille("01");
                db.close();
                return printMessage("Le code Agence " + currentCheque.getAgence() + " n'est pas declare dans la base");
            }
            currentCheque.setVilleremettant("01");
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");

            currentCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
            currentCheque.setIndicateurmodificationcmc7(new BigDecimal(0));
            currentCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALDELTA2")));

            if (index > currentRemise.getNbOperation().intValue()) {
                db.close();
                return printMessage("Nombre d'operations de la Remise =" + currentRemise.getNbOperation()
                        + "\n inferieur au Nombre des cheques =" + index);
            }

            String cleribCal = Utility.computeCleRIB(currentCheque.getBanque(), currentCheque.getAgence(), currentCheque.getNumerocompte());
            if (!cleribCal.equals(currentCheque.getRibcompte())) {
                db.close();
                return printMessage("La Banque " + currentCheque.getBanque() + "\nL'Agence " + currentCheque.getAgence() + "\nLe Compte " + currentCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
            }

            long sumAmount = 0;

            String array[] = hashMap.values().toArray(new String[0]);
            for (int i = 0; i < array.length; i++) {
                sumAmount += Long.parseLong(array[i]);
            }

            if (sumAmount > Long.parseLong(currentRemise.getMontant().trim())) {
                db.close();
                return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " inferieur \nau montant courant de la somme des cheques =" + sumAmount);
            }

            if (index == currentRemise.getNbOperation().intValue()) {
                if (sumAmount < Long.parseLong(currentRemise.getMontant().trim())) {
                    db.close();
                    return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " superieur \nau montant de la somme des cheques =" + sumAmount);
                }

            }

            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentCheque, "CHEQUES", "IDCHEQUE =" + currentCheque.getIdcheque());

            if (updateRowByObjectByQuery) {

                if (index == currentRemise.getNbOperation().intValue()) {
                    if (db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "', ETAT=" + Utility.getParam("CETAOPEALLICOM1") + ""
                            + " WHERE ETAT=" + Utility.getParam("CETAOPEVALDELTA2") + " AND REMISE=" + currentRemise.getIdremise()) == currentRemise.getNbOperation().intValue()) {
                        if (db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPESUPVAL") + " WHERE IDREMISE=" + currentRemise.getIdremise()) == 1) {
                            if (Utility.getParam("VALIDATION_CHEQUE_AUTO") != null && Utility.getParam("VALIDATION_CHEQUE_AUTO").equalsIgnoreCase("AUTO")) {
                                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM1") + " WHERE REMISE=" + currentRemise.getIdremise() + " AND BANQUE <> BANQUEREMETTANT");
                                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " WHERE REMISE=" + currentRemise.getIdremise() + " AND BANQUE = BANQUEREMETTANT"); //                               
                                db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEVALDELTA") + " WHERE IDREMISE=" + currentRemise.getIdremise());
                            }

                        } else {
                            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALDELTA") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                            db.close();
                            return printMessage("Erreur lors de la MAJ de le remise -" + db.getMessage());
                        }
                    } else {
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALDELTA") + " WHERE ETAT=" + Utility.getParam("CETAOPEVALDELTA2") + " AND REMISE=" + currentRemise.getIdremise());
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALDELTA") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                        db.close();
                        return printMessage("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());
                    }

                }

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);

        }
        db.close();
        return printMessage("CHEQUE NON OK-" + db.getMessage());

    }

    public String valideChequeSignature(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemiseCorr");
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentChequeCorr");
            currentCheque.setMontantcheque(request.getParameter("montantCheque").replaceAll("\\p{javaWhitespace}+", ""));
            currentCheque.setBanque(request.getParameter("codeBanque").toUpperCase(Locale.ENGLISH));
            currentCheque.setAgence(Utility.bourrageGZero(request.getParameter("codeAgence"), 5));
            currentCheque.setNumerocompte(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
            currentCheque.setNumerocheque(Utility.bourrageGZero(request.getParameter("numeroCheque"), 7));
            currentCheque.setRibcompte(Utility.bourrageGZero(request.getParameter("clerib"), 2));
            currentCheque.setNomemetteur(request.getParameter("nomTire"));
            currentCheque.setDateemission(request.getParameter("dateEmis"));

            if (currentCheque.getMontantcheque() == null || currentCheque.getMontantcheque().trim().equals("")) {
                return printMessage(" Le Montant du cheque doit être renseigne");
            }
            if (!currentCheque.getMontantcheque().matches("[0-9]+")) {
                return printMessage(" Le Montant du cheque ne doit contenir que des chiffres");
            }

            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("")) {
                return printMessage(" Le Numero du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() == null || currentCheque.getBanque().trim().equals("")) {
                return printMessage(" La Banque du cheque doit être renseigne");
            }
            if (currentCheque.getAgence() == null || currentCheque.getAgence().trim().equals("")) {
                return printMessage(" L'Agence du cheque doit être renseigne");
            }
            if (currentCheque.getNumerocompte() == null || currentCheque.getNumerocompte().trim().equals("")) {
                return printMessage(" Le Numero de compte doit être renseigne");
            }
            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(currentCheque.getNumerocheque().trim())) {
                return printMessage(" Le Numero de cheque doit être renseigne par un entier");
            }
            if (currentCheque.getRibcompte() == null || currentCheque.getRibcompte().trim().equals("")) {
                return printMessage(" La cle RIB du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() != null && Character.isDigit(currentCheque.getBanque().charAt(1))) {

                return printMessage(" Le Code Banque de ce cheque est au format SICA 2.\n Il ne sera pas accepte.");
            }

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String sql = "SELECT * FROM ALL_CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEVALSURCAIENVSIB") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND BANQUE='" + currentCheque.getBanque() + "' AND AGENCE='" + currentCheque.getAgence() + "' AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "'";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                if (!cheques[0].getIdcheque().equals(currentCheque.getIdcheque())) {
                    db.close();
                    return printMessage("Ce cheque a deja ete valide et n'a pas encore ete rejete.\n"
                            + "Il reference le cheque avec IDCHEQUE = " + cheques[0].getIdcheque() + " a l'etat " + cheques[0].getEtat());
                }
            }

            HashMap<BigDecimal, String> hashMap = new HashMap(currentRemise.getNbOperation().intValue());
            sql = "SELECT * FROM CHEQUES WHERE ETAT >= " + Utility.getParam("CETAOPEVAL2") + "  AND REMISE=" + currentRemise.getIdremise();
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                for (int i = 0; i < cheques.length; i++) {
                    if (!cheques[i].getSequence().equals(currentCheque.getSequence())) {
                        hashMap.put(cheques[i].getSequence(), cheques[i].getMontantcheque().trim());
                    }
                }
            }

            hashMap.put(currentCheque.getSequence(), currentCheque.getMontantcheque().trim());

            int index = Integer.parseInt(request.getParameter("index"));

            currentCheque.setDevise("XOF");
            if (Character.isDigit(currentCheque.getBanque().charAt(1))) {
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    currentCheque.setEtablissement(CMPUtility.getCodeBanque());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanque());
                } else {
                    currentCheque.setEtablissement(currentRemise.getEtablissement());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                }
                currentCheque.setType_Cheque("030");
            } else {
                currentCheque.setEtablissement(currentRemise.getEtablissement());
                currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                currentCheque.setType_Cheque("035");
            }
            currentCheque.setAgenceremettant(currentRemise.getAgenceRemettant());
            currentCheque.setCompteremettant(currentRemise.getCompteRemettant());
            currentCheque.setRefremise(currentRemise.getReference());
            currentCheque.setEscompte(currentRemise.getEscompte());
            currentCheque.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setHeuresaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HH:mm:ss"));
            currentCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDateemission(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDevise("XOF");
            currentCheque.setNombeneficiaire(currentRemise.getNomClient());

            sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '" + currentCheque.getBanque() + "' AND CODEAGENCE LIKE '" + currentCheque.getAgence() + "'";
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                currentCheque.setVille(agences[0].getCodevillecompense());
            } else {
                //currentCheque.setVille("01");
                db.close();
                return printMessage("Le code Agence " + currentCheque.getAgence() + " n'est pas declare dans la base");
            }
            currentCheque.setVilleremettant("01");
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");

            currentCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
            currentCheque.setIndicateurmodificationcmc7(new BigDecimal(0));
            currentCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL2"))); // CETAOPEVAL2 Remplace par CETAOPESUPVALSURCAI

            if (index > currentRemise.getNbOperation().intValue()) {
                db.close();
                return printMessage("Nombre d'operations de la Remise =" + currentRemise.getNbOperation()
                        + "\n inferieur au Nombre des cheques =" + index);
            }

            String cleribCal = Utility.computeCleRIB(currentCheque.getBanque(), currentCheque.getAgence(), currentCheque.getNumerocompte());
            if (!cleribCal.equals(currentCheque.getRibcompte())) {
                db.close();
                return printMessage("La Banque " + currentCheque.getBanque() + "\nL'Agence " + currentCheque.getAgence() + "\nLe Compte " + currentCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
            }

            long sumAmount = 0;

            String array[] = hashMap.values().toArray(new String[0]);
            for (int i = 0; i < array.length; i++) {
                sumAmount += Long.parseLong(array[i]);
            }

            if (sumAmount > Long.parseLong(currentRemise.getMontant().trim())) {
                db.close();
                return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " inferieur \nau montant courant de la somme des cheques =" + sumAmount);
            }

            if (index == currentRemise.getNbOperation().intValue()) {
                if (sumAmount < Long.parseLong(currentRemise.getMontant().trim())) {
                    db.close();
                    return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " superieur \nau montant de la somme des cheques =" + sumAmount);
                }

            }

            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentCheque, "CHEQUES", "IDCHEQUE =" + currentCheque.getIdcheque());

            if (updateRowByObjectByQuery) {

                if (index == currentRemise.getNbOperation().intValue()) { //+Utility.getParam("CETAOPEALLICOM1") +
                    if (db.executeUpdate("UPDATE CHEQUES SET COMPTEREMETTANT='" + currentRemise.getCompteRemettant() + "',  "
                            + " ETAT=(CASE  "
                            + "        WHEN banque = '" + Utility.getParam("CODE_BANQUE_SICA3") + "'   THEN  800  "
                            + "        WHEN banque <>'" + Utility.getParam("CODE_BANQUE_SICA3") + "'   THEN  50   "
                            + " ELSE etat  "
                            + "        END ) "
                            + " WHERE ETAT=" + Utility.getParam("CETAOPEVAL2") + ""
                            + " AND REMISE=" + currentRemise.getIdremise()) == currentRemise.getNbOperation().intValue()) {
                        if (db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEALLICOM1") + " WHERE IDREMISE=" + currentRemise.getIdremise()) == 1) {

                            db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEALLICOM1") + " WHERE IDREMISE=" + currentRemise.getIdremise());
                        } else {
                            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVAL") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                            db.close();
                            return printMessage("Erreur lors de la MAJ de le remise -" + db.getMessage());
                        }
                    } else {
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVAL") + " WHERE ETAT=" + Utility.getParam("CETAOPEALLICOM1") + " AND REMISE=" + currentRemise.getIdremise());
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVAL") + " WHERE IDCHEQUE=" + currentCheque.getIdcheque());
                        db.close();
                        return printMessage("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());
                    }

                }

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);

        }
        db.close();
        return printMessage("CHEQUE NON OK-" + db.getMessage());

    }

    public String valideChequeSignatureETG(HttpServletRequest request, HttpServletResponse response) {

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemiseCorr");
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentChequeCorr");
            currentCheque.setMontantcheque(request.getParameter("montantCheque").replaceAll("\\p{javaWhitespace}+", ""));
            currentCheque.setBanque(request.getParameter("codeBanque").toUpperCase(Locale.ENGLISH));
            currentCheque.setAgence(Utility.bourrageGZero(request.getParameter("codeAgence"), 5));
            currentCheque.setNumerocompte(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
            currentCheque.setNumerocheque(Utility.bourrageGZero(request.getParameter("numeroCheque"), 7));
            currentCheque.setRibcompte(Utility.bourrageGZero(request.getParameter("clerib"), 2));
            currentCheque.setNomemetteur(request.getParameter("nomTire"));
            currentCheque.setDateemission(request.getParameter("dateEmis"));

            if (currentCheque.getMontantcheque() == null || currentCheque.getMontantcheque().trim().equals("")) {
                return printMessage(" Le Montant du cheque doit être renseigne");
            }
            if (!currentCheque.getMontantcheque().matches("[0-9]+")) {
                return printMessage(" Le Montant du cheque ne doit contenir que des chiffres");
            }

            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("")) {
                return printMessage(" Le Numero du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() == null || currentCheque.getBanque().trim().equals("")) {
                return printMessage(" La Banque du cheque doit être renseigne");
            }
            if (currentCheque.getAgence() == null || currentCheque.getAgence().trim().equals("")) {
                return printMessage(" L'Agence du cheque doit être renseigne");
            }
            if (currentCheque.getNumerocompte() == null || currentCheque.getNumerocompte().trim().equals("")) {
                return printMessage(" Le Numero de compte doit être renseigne");
            }
            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(currentCheque.getNumerocheque().trim())) {
                return printMessage(" Le Numero de cheque doit être renseigne par un entier");
            }
            if (currentCheque.getRibcompte() == null || currentCheque.getRibcompte().trim().equals("")) {
                return printMessage(" La cle RIB du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() != null && Character.isDigit(currentCheque.getBanque().charAt(1))) {

                return printMessage(" Le Code Banque de ce cheque est au format SICA 2.\n Il ne sera pas accepte.");
            }

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String sql = "SELECT * FROM ALL_CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + ","
                    + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + "," + Utility.getParam("CETAOPEVAL") + ","
                    + Utility.getParam("CETAOPEVALSURCAIENVSIB") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND BANQUE='" + currentCheque.getBanque() + "' AND AGENCE='" + currentCheque.getAgence() + "' "
                    + " AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "'";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                if (!cheques[0].getIdcheque().equals(currentCheque.getIdcheque())) {
                    db.close();
                    return printMessage("Ce cheque a deja ete valide et n'a pas encore ete rejete.\n"
                            + "Il reference le cheque avec IDCHEQUE = " + cheques[0].getIdcheque() + " a l'etat " + cheques[0].getEtat());
                }
            }

            HashMap<BigDecimal, String> hashMap = new HashMap(currentRemise.getNbOperation().intValue());
            sql = "SELECT * FROM CHEQUES WHERE ETAT >= " + Utility.getParam("CETAOPESUPVALSURCAI") + "  AND REMISE=" + currentRemise.getIdremise();
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            int nbOperations = 0;
            if (cheques != null && cheques.length > 0) {
                nbOperations = cheques.length;
                for (int i = 0; i < cheques.length; i++) {
                    if (!cheques[i].getSequence().equals(currentCheque.getSequence())) {
                        hashMap.put(cheques[i].getSequence(), cheques[i].getMontantcheque().trim());
                    }
                }
            }

            hashMap.put(currentCheque.getSequence(), currentCheque.getMontantcheque().trim());

            int index = Integer.parseInt(request.getParameter("index"));

            currentCheque.setDevise("XOF");
            if (Character.isDigit(currentCheque.getBanque().charAt(1))) {
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    currentCheque.setEtablissement(CMPUtility.getCodeBanque());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanque());
                } else {
                    currentCheque.setEtablissement(currentRemise.getEtablissement());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                }
                currentCheque.setType_Cheque("030");
            } else {
                currentCheque.setEtablissement(currentRemise.getEtablissement());
                currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                currentCheque.setType_Cheque("035");
            }
            currentCheque.setAgenceremettant(currentRemise.getAgenceRemettant());
            currentCheque.setCompteremettant(currentRemise.getCompteRemettant());
            currentCheque.setRefremise(currentRemise.getReference());
            currentCheque.setEscompte(currentRemise.getEscompte());
            currentCheque.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setHeuresaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HH:mm:ss"));
            currentCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDateemission(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDevise("XOF");
            currentCheque.setNombeneficiaire(currentRemise.getNomClient());

            sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '" + currentCheque.getBanque() + "' AND CODEAGENCE LIKE '" + currentCheque.getAgence() + "'";
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                currentCheque.setVille(agences[0].getCodevillecompense());
            } else {
                //currentCheque.setVille("01");
                db.close();
                return printMessage("Le code Agence " + currentCheque.getAgence() + " n'est pas declare dans la base");
            }
            currentCheque.setLotsib(new BigDecimal("1"));
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");

            currentCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
            currentCheque.setIndicateurmodificationcmc7(new BigDecimal(0));
            currentCheque.setLotsib(BigDecimal.ONE);

//            if (index > currentRemise.getNbOperation().intValue()) {
//                db.close();
//                return printMessage("Nombre d'operations de la Remise =" + currentRemise.getNbOperation()
//                        + "\n inferieur au Nombre des cheques =" + index);
//            }
            String cleribCal = Utility.computeCleRIB(currentCheque.getBanque(), currentCheque.getAgence(), currentCheque.getNumerocompte());
            if (!cleribCal.equals(currentCheque.getRibcompte())) {
                db.close();
                return printMessage("La Banque " + currentCheque.getBanque() + "\nL'Agence " + currentCheque.getAgence() + "\nLe Compte " + currentCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
            }

            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentCheque, "CHEQUES", "IDCHEQUE =" + currentCheque.getIdcheque());

            if (updateRowByObjectByQuery) {

                if (index == nbOperations) { //
                    if (db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMSIGVER") + " WHERE IDREMISE=" + currentRemise.getIdremise()) == 1) {

                        db.executeUpdate("UPDATE CHEQUES SET LOTSIB=1 WHERE REMISE=" + currentRemise.getIdremise());

                    } else {
                        db.close();
                        return printMessage("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());
                    }

                }

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);

        }
        db.close();
        return printMessage("CHEQUE NON OK-" + db.getMessage());

    }

    public String valideChequeSignatureECI(HttpServletRequest request, HttpServletResponse response) {

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemiseCorr");
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentChequeCorr");
            currentCheque.setMontantcheque(request.getParameter("montantCheque").replaceAll("\\p{javaWhitespace}+", ""));
            currentCheque.setBanque(request.getParameter("codeBanque").toUpperCase(Locale.ENGLISH));
            currentCheque.setAgence(Utility.bourrageGZero(request.getParameter("codeAgence"), 5));
            currentCheque.setNumerocompte(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
            currentCheque.setNumerocheque(Utility.bourrageGZero(request.getParameter("numeroCheque"), 7));
            currentCheque.setRibcompte(Utility.bourrageGZero(request.getParameter("clerib"), 2));
            currentCheque.setNomemetteur(request.getParameter("nomTire"));
            currentCheque.setDateemission(request.getParameter("dateEmis"));

            if (currentCheque.getMontantcheque() == null || currentCheque.getMontantcheque().trim().equals("")) {
                return printMessage(" Le Montant du cheque doit être renseigne");
            }
            if (!currentCheque.getMontantcheque().matches("[0-9]+")) {
                return printMessage(" Le Montant du cheque ne doit contenir que des chiffres");
            }

            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("")) {
                return printMessage(" Le Numero du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() == null || currentCheque.getBanque().trim().equals("")) {
                return printMessage(" La Banque du cheque doit être renseigne");
            }
            if (currentCheque.getAgence() == null || currentCheque.getAgence().trim().equals("")) {
                return printMessage(" L'Agence du cheque doit être renseigne");
            }
            if (currentCheque.getNumerocompte() == null || currentCheque.getNumerocompte().trim().equals("")) {
                return printMessage(" Le Numero de compte doit être renseigne");
            }
            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(currentCheque.getNumerocheque().trim())) {
                return printMessage(" Le Numero de cheque doit être renseigne par un entier");
            }
            if (currentCheque.getRibcompte() == null || currentCheque.getRibcompte().trim().equals("")) {
                return printMessage(" La cle RIB du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() != null && Character.isDigit(currentCheque.getBanque().charAt(1))) {

                return printMessage(" Le Code Banque de ce cheque est au format SICA 2.\n Il ne sera pas accepte.");
            }

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String sql = "SELECT * FROM ALL_CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + ","
                    + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + "," + Utility.getParam("CETAOPEVAL") + ","
                    + Utility.getParam("CETAOPEVALSURCAIENVSIB") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND BANQUE='" + currentCheque.getBanque() + "' AND AGENCE='" + currentCheque.getAgence() + "' "
                    + " AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "'";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                if (!cheques[0].getIdcheque().equals(currentCheque.getIdcheque())) {
                    db.close();
                    return printMessage("Ce cheque a deja ete valide et n'a pas encore ete rejete.\n"
                            + "Il reference le cheque avec IDCHEQUE = " + cheques[0].getIdcheque() + " a l'etat " + cheques[0].getEtat());
                }
            }

            HashMap<BigDecimal, String> hashMap = new HashMap(currentRemise.getNbOperation().intValue());
            sql = "SELECT * FROM CHEQUES WHERE ETAT >= " + Utility.getParam("CETAREMSAIIRIS") + "  AND REMISE=" + currentRemise.getIdremise();
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            int nbOperations = 0;
            if (cheques != null && cheques.length > 0) {
                nbOperations = cheques.length;
                for (int i = 0; i < cheques.length; i++) {
                    if (!cheques[i].getSequence().equals(currentCheque.getSequence())) {
                        hashMap.put(cheques[i].getSequence(), cheques[i].getMontantcheque().trim());
                    }
                }
            }

            hashMap.put(currentCheque.getSequence(), currentCheque.getMontantcheque().trim());

            int index = Integer.parseInt(request.getParameter("index"));

            currentCheque.setDevise("XOF");
            if (Character.isDigit(currentCheque.getBanque().charAt(1))) {
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    currentCheque.setEtablissement(CMPUtility.getCodeBanque());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanque());
                } else {
                    currentCheque.setEtablissement(currentRemise.getEtablissement());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                }
                currentCheque.setType_Cheque("030");
            } else {
                currentCheque.setEtablissement(currentRemise.getEtablissement());
                currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                currentCheque.setType_Cheque("035");
            }
            currentCheque.setAgenceremettant(currentRemise.getAgenceRemettant());
            currentCheque.setCompteremettant(currentRemise.getCompteRemettant());
            currentCheque.setRefremise(currentRemise.getReference());
            currentCheque.setEscompte(currentRemise.getEscompte());
            currentCheque.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setHeuresaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HH:mm:ss"));
            currentCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDateemission(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDevise("XOF");
            currentCheque.setNombeneficiaire(currentRemise.getNomClient());

            sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '" + currentCheque.getBanque() + "' AND CODEAGENCE LIKE '" + currentCheque.getAgence() + "'";
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                currentCheque.setVille(agences[0].getCodevillecompense());
            } else {
                //currentCheque.setVille("01");
                db.close();
                return printMessage("Le code Agence " + currentCheque.getAgence() + " n'est pas declare dans la base");
            }
            currentCheque.setLotsib(new BigDecimal("1"));
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentCheque.setCodeutilisateur(currentCheque.getCodeutilisateur() + "+" + user.getLogin());

            currentCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
            currentCheque.setIndicateurmodificationcmc7(new BigDecimal(0));
            currentCheque.setLotsib(BigDecimal.ONE);

//            if (index > currentRemise.getNbOperation().intValue()) {
//                db.close();
//                return printMessage("Nombre d'operations de la Remise =" + currentRemise.getNbOperation()
//                        + "\n inferieur au Nombre des cheques =" + index);
//            }
            String cleribCal = Utility.computeCleRIB(currentCheque.getBanque(), currentCheque.getAgence(), currentCheque.getNumerocompte());
            if (!cleribCal.equals(currentCheque.getRibcompte())) {
                db.close();
                return printMessage("La Banque " + currentCheque.getBanque() + "\nL'Agence " + currentCheque.getAgence() + "\nLe Compte " + currentCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
            }

            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentCheque, "CHEQUES", "IDCHEQUE =" + currentCheque.getIdcheque());

            if (updateRowByObjectByQuery) {

                if (index == nbOperations) { //
                    if (db.executeUpdate("UPDATE REMISES SET LOTAGENCE=" + Utility.getParam("CETAREMSIGVER") + " WHERE IDREMISE=" + currentRemise.getIdremise()) == 1) {

                        db.executeUpdate("UPDATE CHEQUES SET LOTSIB=1 WHERE REMISE=" + currentRemise.getIdremise());

                    } else {
                        db.close();
                        return printMessage("Erreur lors de MAJ des cheques de la remise -" + db.getMessage());
                    }

                }

                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);

        }
        db.close();
        return printMessage("CHEQUE NON OK-" + db.getMessage());

    }

    public String checkChequeTresor(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Remises currentRemise = (Remises) request.getSession().getAttribute("currentRemise");
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentCheque");
            currentCheque.setMontantcheque(request.getParameter("montantCheque").replaceAll("\\p{javaWhitespace}+", ""));
            currentCheque.setBanque(request.getParameter("codeBanque").toUpperCase(Locale.ENGLISH));
            currentCheque.setAgence(Utility.bourrageGZero(request.getParameter("codeAgence"), 5));
            currentCheque.setNumerocompte(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
            currentCheque.setNumerocheque(Utility.bourrageGZero(request.getParameter("numeroCheque"), 7));
            currentCheque.setRibcompte(Utility.bourrageGZero(request.getParameter("clerib"), 2));
            currentCheque.setNomemetteur(request.getParameter("nomTire"));

            if (currentCheque.getMontantcheque() == null || currentCheque.getMontantcheque().trim().equals("")) {
                return printMessage(" Le Montant du cheque doit être renseigne");
            }
            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("")) {
                return printMessage(" Le Numero du cheque doit être renseigne");
            }
            if (currentCheque.getBanque() == null || currentCheque.getBanque().trim().equals("")) {
                return printMessage(" La Banque du cheque doit être renseigne");
            }
            if (currentCheque.getAgence() == null || currentCheque.getAgence().trim().equals("")) {
                return printMessage(" L'Agence du cheque doit être renseigne");
            }
            if (currentCheque.getNumerocompte() == null || currentCheque.getNumerocompte().trim().equals("")) {
                return printMessage(" Le Numero de compte doit être renseigne");
            }
            if (currentCheque.getNumerocheque() == null || currentCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(currentCheque.getNumerocheque().trim())) {
                return printMessage(" Le Numero de cheque doit être renseigne par un entier");
            }
            if (currentCheque.getRibcompte() == null || currentCheque.getRibcompte().trim().equals("")) {
                return printMessage(" La cle RIB du cheque doit être renseigne");
            }

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPERETRECENVSIBVER") + "," + Utility.getParam("CETAOPERET") + "," + Utility.getParam("CETAOPERETENVSIB") + "," + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERETRECENVSIB") + "," + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + "," + Utility.getParam("CETAOPEVALSURCAIENVSIB") + ") AND BANQUE='" + currentCheque.getBanque() + "' AND AGENCE='" + currentCheque.getAgence() + "' AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "'";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                if (!cheques[0].getIdcheque().equals(currentCheque.getIdcheque())) {
                    db.close();
                    return printMessage("Ce cheque a deja ete valide et n'a pas encore ete rejete.\n"
                            + "Il reference le cheque avec IDCHEQUE = " + cheques[0].getIdcheque() + " a l'etat " + cheques[0].getEtat() + " - " + Utility.getParamLabelOfType("" + cheques[0].getEtat(), "CODE_ETAT"));
                }
            }

            HashMap<BigDecimal, String> hashMap = new HashMap(currentRemise.getNbOperation().intValue());
            sql = "SELECT * FROM CHEQUES WHERE ETAT >= " + Utility.getParam("CETAOPESAI") + "  AND REMISE=" + currentRemise.getIdremise();
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                for (int i = 0; i < cheques.length; i++) {
                    if (!cheques[i].getSequence().equals(currentCheque.getSequence())) {
                        hashMap.put(cheques[i].getSequence(), cheques[i].getMontantcheque().trim());
                    }
                }
            }
            hashMap.put(currentCheque.getSequence(), currentCheque.getMontantcheque().trim());

            int index = Integer.parseInt(request.getParameter("index"));

            currentCheque.setDevise("XOF");
            if (Character.isDigit(currentCheque.getBanque().charAt(1))) {
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    currentCheque.setEtablissement(CMPUtility.getCodeBanque());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanque());
                } else {
                    currentCheque.setEtablissement(CMPUtility.getCodeBanqueSica3());
                    currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                }
                currentCheque.setType_Cheque("030");
            } else {
                currentCheque.setEtablissement(CMPUtility.getCodeBanqueSica3());
                currentCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                currentCheque.setType_Cheque("035");
            }
            currentCheque.setAgenceremettant(currentRemise.getAgenceRemettant());
            currentCheque.setCompteremettant(currentRemise.getCompteRemettant());
            currentCheque.setRefremise(currentRemise.getReference());
            currentCheque.setEscompte(currentRemise.getEscompte());
            currentCheque.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            currentCheque.setDateemission(request.getParameter("dateEmis"));
            currentCheque.setDevise("XOF");
            currentCheque.setNombeneficiaire(currentRemise.getNomClient());

            sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '" + currentCheque.getBanque() + "' AND CODEAGENCE LIKE '" + currentCheque.getAgence() + "'";
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                currentCheque.setVille(agences[0].getCodevillecompense());
            } else {
                //currentCheque.setVille("01");
                db.close();
                return printMessage("Le Code Banque " + currentCheque.getBanque() + " et/ou le code Agence " + currentCheque.getAgence()
                        + "\n ne sont pas declare dans la base");
            }
            currentCheque.setVilleremettant("01");
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            currentCheque.setCodeutilisateur(user.getLogin().trim());

            currentCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
            currentCheque.setIndicateurmodificationcmc7(new BigDecimal(0));
            currentCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPESAI")));

            if (index > currentRemise.getNbOperation().intValue()) {
                db.close();
                return printMessage("Nombre d'operations de la Remise =" + currentRemise.getNbOperation()
                        + "\n inferieur au Nombre des cheques =" + index);
            }

            String cleribCal = Utility.computeCleRIB(currentCheque.getBanque(), currentCheque.getAgence(), currentCheque.getNumerocompte());
            if (!cleribCal.equals(currentCheque.getRibcompte())) {
                db.close();
                return printMessage("La Banque " + currentCheque.getBanque() + "\nL'Agence " + currentCheque.getAgence() + "\nLe Compte " + currentCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
            }

            long sumAmount = 0;

            String array[] = hashMap.values().toArray(new String[0]);
            for (int i = 0; i < array.length; i++) {
                sumAmount += Long.parseLong(array[i]);
            }

            if (sumAmount > Long.parseLong(currentRemise.getMontant().trim())) {
                db.close();
                return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " inferieur \nau montant courant de la somme des cheques =" + sumAmount);
            }

            if (index == currentRemise.getNbOperation().intValue()) {
                if (sumAmount < Long.parseLong(currentRemise.getMontant().trim())) {
                    db.close();
                    return printMessage("Montant de la Remise =" + currentRemise.getMontant() + " superieur \nau montant de la somme des cheques =" + sumAmount);
                }

            }

            boolean updateRowByObjectByQuery = db.updateRowByObjectByQuery(currentCheque, "CHEQUES", "IDCHEQUE =" + currentCheque.getIdcheque());

            if (updateRowByObjectByQuery) {
                db.executeUpdate("DELETE FROM SEQUENCES WHERE IDSEQUENCE=" + currentCheque.getSequence());
                if (index == currentRemise.getNbOperation().intValue()) {
                    db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVAL") + " WHERE REMISE=" + currentRemise.getIdremise());
                    db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMVAL") + " WHERE IDREMISE=" + currentRemise.getIdremise());

                    IBUS_CHQ_SOUM fluchesous[] = new IBUS_CHQ_SOUM[cheques.length + 1];
                    for (int i = 0; i < cheques.length; i++) {
                        fluchesous[i] = new IBUS_CHQ_SOUM(currentRemise.getNoex(), currentRemise.getCdscpta(), currentRemise.getIdop(),
                                new BigDecimal(currentRemise.getMontant()), cheques[i].getNumerocheque(), cheques[i].getBanque(), cheques[i].getAgence(), cheques[i].getNomemetteur(),
                                Utility.convertStringToDate(cheques[i].getDateemission(), ResLoader.getMessages("patternDate")), Utility.convertStringToDate(cheques[i].getDatesaisie(), ResLoader.getMessages("patternDate")),
                                new BigDecimal(cheques[i].getMontantcheque()), cheques[i].getNumerocompte(), cheques[i].getIdcheque(), "SICA", "CIE", "N");

                    }
                    fluchesous[cheques.length] = new IBUS_CHQ_SOUM(currentRemise.getNoex(), currentRemise.getCdscpta(), currentRemise.getIdop(),
                            new BigDecimal(currentRemise.getMontant()), currentCheque.getNumerocheque(), currentCheque.getBanque(), currentCheque.getAgence(), currentCheque.getNomemetteur(),
                            Utility.convertStringToDate(currentCheque.getDateemission(), ResLoader.getMessages("patternDate")), Utility.convertStringToDate(currentCheque.getDatesaisie(), ResLoader.getMessages("patternDate")),
                            new BigDecimal(currentCheque.getMontantcheque()), currentCheque.getNumerocompte(), currentCheque.getIdcheque(), "SICA", "CIE", "N");
//                    for (int i = 0; i < fluchesous.length; i++) {
//                        IBUS_CHQ_SOUM fluchesou = fluchesous[i];
//                        if (db.insertObjectAsRowByQuery(fluchesou, "IBUS_CHQ_SOUM")) {
//
//                            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE REMISE=" + currentRemise.getIdremise());
//                            db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + currentRemise.getIdremise());
//                        }
//
//                    }
                    boolean success = false;
                    for (int i = 0; i < fluchesous.length; i++) {
                        IBUS_CHQ_SOUM fluchesou = fluchesous[i];
                        success = db.insertObjectAsRowByQuery(fluchesou, "IBUS_CHQ_SOUM");

                    }
                    if (success) {

                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM1") + " WHERE REMISE=" + currentRemise.getIdremise() + " AND BANQUE<>BANQUEREMETTANT");
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALSURCAIENVSIB") + " WHERE REMISE=" + currentRemise.getIdremise() + " AND BANQUE=BANQUEREMETTANT");
                        db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + currentRemise.getIdremise());
                        db.executeUpdate("DELETE FROM IBUS_OPE_GUI WHERE NO_EX=" + currentRemise.getNoex() + " AND CD_SCPTA='" + currentRemise.getCdscpta() + "' AND ID_OP=" + currentRemise.getIdop());
                    }

                }
                db.close();
                return printMessage("OK");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);

        }
        db.close();
        return printMessage("CHEQUE NON OK");

    }

    public String doUpdate(String query) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            int result = db.executeUpdate(query);
            db.close();
            return printMessage("Nombre de lignes mis a jour = " + result);

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("Erreur lors de la mise a jour" + "\n " + db.getMessage());
    }

    public String recupereInfoVignette(HttpSession session, String numeroCompte) {

        return printMessage("rien");
    }

    public String doRapprochement(HttpSession session, String filtre) {
        try {
        } catch (Exception ex) {

            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            return printMessage(ex.getMessage());
        }
        return printMessage("Rapprochement Termine!");
    }

    public String doTableFiltres(HttpSession session, String id) {
        ComboIdBean comboIdBean = (ComboIdBean) session.getAttribute("comboIdBean");
        String tableFiltres = comboIdBean.getTableFiltres(id);
        session.setAttribute("comboIdBean", comboIdBean);
        return printMessage(tableFiltres);
    }

    public String doColonnes(HttpSession session, String table) {
        ComboIdBean comboIdBean = (ComboIdBean) session.getAttribute("comboIdBean");
        String colonnes = comboIdBean.getColonnes(table);
        session.setAttribute("comboIdBean", comboIdBean);
        return printMessage(colonnes);
    }

    public String doListAgences(HttpSession session, String banque) {
        ComboAgenceBean comboAgenceBean = new ComboAgenceBean();
        String agences = comboAgenceBean.getComboBanqueData(banque);
        return printMessage(agences);
    }

    public String getColonnes(HttpSession session, String table) {
        ComboTableBean comboTableBean = (ComboTableBean) session.getAttribute("comboTableBean");
        String colonnes = comboTableBean.getColonnes(table);
        session.setAttribute("comboTableBean", comboTableBean);
        return printMessage(colonnes);
    }

    public String recupereInfoCompteFromSibICCPT(HttpSession session, String numeroCompte) {
        ComboCompteNSIABean comboCompteBean = (ComboCompteNSIABean) session.getAttribute("comboCompteBean");
        String infoCompte = comboCompteBean.recupInfoCompte(numeroCompte);
        session.setAttribute("comboCompteBean", comboCompteBean);
        System.out.println("infoCompte : " + infoCompte);
        return printMessage(infoCompte);
    }

    public String recupereInfoCompte(HttpSession session, String numeroCompte) {
        ComboCompteBean comboCompteBean = (ComboCompteBean) session.getAttribute("comboCompteBean");
        String infoCompte = comboCompteBean.getInfoCompte(numeroCompte);
        session.setAttribute("comboCompteBean", comboCompteBean);
        return printMessage(infoCompte);
    }

    public String recupereInfoComptePF(HttpSession session, String numeroCompte) {
        ComboComptePFBean comboCompteBean = (ComboComptePFBean) session.getAttribute("comboCompteBean");
        String infoCompte = comboCompteBean.getInfoCompte(numeroCompte);
        session.setAttribute("comboCompteBean", comboCompteBean);
        return printMessage(infoCompte);
    }

    public String recupereInfoCompteLite(HttpSession session, String numeroCompte) {
        ComboCompteBean comboCompteBean = (ComboCompteBean) session.getAttribute("comboCompteBean");
        Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
        String infoCompte = comboCompteBean.getInfoCompteLite(numeroCompte, user.getAdresse().trim());
        session.setAttribute("comboCompteBean", comboCompteBean);
        return printMessage(infoCompte);
    }

    public String recupereCodeBanque(HttpSession session, String codeBanque) {
        ComboBanqueBean comboBanqueBean = new ComboBanqueBean();
        String infoBanque = comboBanqueBean.getInfoBanque(codeBanque);

        return printMessage(infoBanque);
    }

    public String recupereCodeAgence(HttpSession session, String codeBanque) {
        ComboBanqueBean comboBanqueBean = new ComboBanqueBean();
        String infoBanque = comboBanqueBean.getInfoBanque(codeBanque);

        return printMessage(infoBanque);
    }

    public String recupereInfoCompteCNCE(HttpSession session, String numeroCompte) {
        ComboCompteCNCEBean comboCompteBean = (ComboCompteCNCEBean) session.getAttribute("comboCompteBean");
        String infoCompte = comboCompteBean.getInfoCompte(numeroCompte);
        session.setAttribute("comboCompteBean", comboCompteBean);
        return printMessage(infoCompte);
    }

    public String recupereInfoCompteTPCI(HttpSession session, String numeroCompte) {
        ComboCompteTPCIBean comboCompteBean = (ComboCompteTPCIBean) session.getAttribute("comboCompteBean");
        String infoCompte = comboCompteBean.getInfoCompte(numeroCompte);
        session.setAttribute("comboCompteBean", comboCompteBean);
        return printMessage(infoCompte);
    }

    public String recupereInfoCompteNSIA(HttpSession session, String numeroCompte) {
        ComboCompteNSIABean comboCompteBean = (ComboCompteNSIABean) session.getAttribute("comboCompteBean");

        String infoCompte = comboCompteBean.getInfoCompte(numeroCompte);
        session.setAttribute("comboCompteBean", comboCompteBean);
        return printMessage(infoCompte);
    }

    public String recupereInfoCompteCBAO(HttpSession session, String numeroCompte) {
        ComboCompteCBAOBean comboCompteBean = (ComboCompteCBAOBean) session.getAttribute("comboCompteBean");

        String infoCompte = comboCompteBean.getInfoCompte(numeroCompte);
        session.setAttribute("comboCompteBean", comboCompteBean);
        return printMessage(infoCompte);
    }

    public String recupereInfoCompteBDK(HttpSession session, String numeroCompte) {
        ComboCompteBDKBean comboCompteBean = (ComboCompteBDKBean) session.getAttribute("comboCompteBean");

        String infoCompte = comboCompteBean.getInfoCompte(numeroCompte);
        session.setAttribute("comboCompteBean", comboCompteBean);
        return printMessage(infoCompte);
    }

    public String recupereInfoCompteECI(HttpSession session, String numeroCompte) {
        ComboCompteBean comboCompteBean = (ComboCompteBean) session.getAttribute("comboCompteBean");
        String infoCompte = comboCompteBean.getCompteMessageBean(numeroCompte);
        session.setAttribute("comboCompteBean", comboCompteBean);
        return printMessage(infoCompte);
    }

    public String recupereAgenceCompte(HttpSession session, String numeroCompte) {
        ComboCompteCNCEBean comboCompteBean = (ComboCompteCNCEBean) session.getAttribute("comboCompteBean");
        String infoCompte = comboCompteBean.getComboAgenceDatas(numeroCompte);
        session.setAttribute("comboCompteBean", comboCompteBean);
        return printMessage(infoCompte);
    }
    
    
    public String recupereAgenceCompteCORIS(HttpSession session, String numeroCompte) {
        ComboCompteCNCEBean comboCompteBean = (ComboCompteCNCEBean) session.getAttribute("comboCompteBean");
        String infoCompte = comboCompteBean.getComboAgenceDatas(numeroCompte);
        session.setAttribute("comboCompteBean", comboCompteBean);
        return printMessage(infoCompte);
    }

    public String recupereAgenceCompteNSIA(HttpSession session, String numeroCompte) {
        ComboCompteNSIABean comboCompteBean = (ComboCompteNSIABean) session.getAttribute("comboCompteBean");
        String infoCompte = comboCompteBean.getComboAgenceDatas(numeroCompte);
        session.setAttribute("comboCompteBean", comboCompteBean);
        return printMessage(infoCompte);
    }

    public String recupereAgenceCompteECI(HttpSession session, String numeroCompte) {
        ComboCompteBean comboCompteBean = (ComboCompteBean) session.getAttribute("comboCompteBean");
        String infoCompte = comboCompteBean.getComboAgenceDatas(numeroCompte);
        session.setAttribute("comboCompteBean", comboCompteBean);
        return printMessage(infoCompte);
    }

    public String verificationCompte(HttpSession session, String agence, String numeroCompte) {
        ComboCompteCNCEBean comboCompteBean = (ComboCompteCNCEBean) session.getAttribute("comboCompteBean");
        String infoCompte = comboCompteBean.getInfoCompte(agence, numeroCompte);
        session.setAttribute("comboCompteBean", comboCompteBean);
        return printMessage(infoCompte);
    }

    public String verificationCompteNSIA(HttpSession session, String agence, String numeroCompte) {
        ComboCompteNSIABean comboCompteBean = (ComboCompteNSIABean) session.getAttribute("comboCompteBean");
        String infoCompte = comboCompteBean.getInfoCompte(agence, numeroCompte);
        session.setAttribute("comboCompteBean", comboCompteBean);
        return printMessage(infoCompte);
    }

    public String verificationCompteECI(HttpSession session, String agence, String numeroCompte) {
        ComboCompteBean comboCompteBean = (ComboCompteBean) session.getAttribute("comboCompteBean");
        String infoCompte = comboCompteBean.getInfoCompte(agence, numeroCompte);
        session.setAttribute("comboCompteBean", comboCompteBean);
        return printMessage(infoCompte);
    }

    public String verificationCompteETG(HttpSession session, String agence, String numeroCompte) {
        ComboCompteBean comboCompteBean = (ComboCompteBean) session.getAttribute("comboCompteBean");
        String infoCompte = comboCompteBean.getInfoCompte(agence, numeroCompte);
        session.setAttribute("comboCompteBean", comboCompteBean);
        return printMessage(infoCompte);
    }

    public void removeSelectedObjects() {
    }

    public void addObject() {
    }

    public MessageBean getMessageBean() {
        return messageBean;
    }

    public void setMessageBean(MessageBean messageBean) {
        this.messageBean = messageBean;
    }

    public RapportBean getRapportBean() {
        return rapportBean;
    }

    public void setRapportBean(RapportBean rapportBean) {
        this.rapportBean = rapportBean;
    }

    public ServletConfig getServletConfig() {
        return servletConfig;
    }

    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }

    String archiveBaseCompense(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Date dateCompens = Utility.convertStringToDate(Utility.getParam("DATECOMPENS_NAT"), "yyyyMMdd");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateCompens);
            calendar.add(Calendar.DAY_OF_MONTH, -Integer.parseInt(Utility.getParam("NBROFDAYTOSTORE")));
            dateCompens = new Date(calendar.getTimeInMillis());

            String dateArchive = Utility.convertDateToString(dateCompens, ResLoader.getMessages("patternDate"));

            int result = db.executeUpdate("INSERT INTO CHEQUES_ARCHIVES SELECT * FROM CHEQUES WHERE DATETRAITEMENT<='" + dateArchive + "'");
            if (result > 0) {
                result = db.executeUpdate("DELETE FROM CHEQUES WHERE DATETRAITEMENT<='" + dateArchive + "'");
            }

            result = db.executeUpdate("INSERT INTO EFFETS_ARCHIVES SELECT * FROM EFFETS WHERE DATETRAITEMENT<='" + dateArchive + "'");
            if (result > 0) {
                result = db.executeUpdate("DELETE FROM EFFETS WHERE DATETRAITEMENT<='" + dateArchive + "'");
            }
            result = db.executeUpdate("INSERT INTO VIREMENTS_ARCHIVES SELECT * FROM VIREMENTS WHERE DATETRAITEMENT<='" + dateArchive + "'");
            if (result > 0) {
                result = db.executeUpdate("DELETE FROM VIREMENTS WHERE DATETRAITEMENT<='" + dateArchive + "'");
            }

            result = db.executeUpdate("INSERT INTO REMISES_ARCHIVES SELECT * FROM REMISES WHERE DATESAISIE<='" + Utility.convertDateToString(dateCompens, "yyyyMMdd") + "'");
            if (result > 0) {
                result = db.executeUpdate("DELETE FROM REMISES WHERE DATESAISIE<='" + Utility.convertDateToString(dateCompens, "yyyyMMdd") + "'");
            }

            result = db.executeUpdate("INSERT INTO REMCOM_ARCHIVES SELECT * FROM REMCOM WHERE DATEPRESENTATION<=TO_TIMESTAMP('" + dateArchive + "','YYYY/MM/DD')");
            if (result > 0) {
                result = db.executeUpdate("DELETE FROM REMCOM WHERE DATEPRESENTATION<=TO_TIMESTAMP('" + dateArchive + "','YYYY/MM/DD')");
            }
            result = db.executeUpdate("INSERT INTO SYNTHESE_ARCHIVES SELECT * FROM SYNTHESE WHERE DATEPRESENTATION<=TO_TIMESTAMP('" + dateArchive + "','YYYY/MM/DD')");
            if (result > 0) {
                result = db.executeUpdate("DELETE FROM SYNTHESE WHERE DATEPRESENTATION<=TO_TIMESTAMP('" + dateArchive + "','YYYY/MM/DD')");
            }
            result = db.executeUpdate("INSERT INTO AUDITLOG_ARCHIVES SELECT * FROM AUDITLOG ");
            if (result > 0) {
                result = db.executeUpdate("DELETE FROM AUDITLOG");
            }
            result = db.executeUpdate("INSERT INTO FICHIERS_ARCHIVES SELECT * FROM FICHIERS ");
            if (result > 0) {
                result = db.executeUpdate("DELETE FROM FICHIERS ");
            }

            db.close();
            return printMessage("Archivage effectue avec Succes");
        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("Erreur lors de l'archivage, Veuillez recommencer.");

    }

    String changePasswordAdmin(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            String sql = "SELECT * FROM UTILISATEURS WHERE LOGIN='" + request.getParameter("login") + "'";
            Utilisateurs unUtilisateur[] = (Utilisateurs[]) db.retrieveRowAsObject(sql, new Utilisateurs());
            Utilisateurs user = unUtilisateur[0];
            String result = checkPassword(request.getParameter("password1"));
            if (result == null) {
                return result;
            }
            user.setPassword(new RegValidator().encoder(request.getParameter("password1")).replaceAll("\\p{Punct}", "_"));
            user.setEtat(new BigDecimal(Utility.getParam("CETAUTICRE")));
            Utilisateurs utilisateur = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            user.setUcreation(utilisateur.getLogin());
            user.setDmodification(Utility.convertDateToString(new java.util.Date(), ResLoader.getMessages("patternDate")));
            String where = "LOGIN='" + user.getLogin() + "'";
            boolean success = db.updateRowByObjectByQuery(user, "UTILISATEURS", where);
            db.executeUpdate("DELETE FROM PARAMS WHERE NOM='" + user.getLogin() + "'");
            db.close();
            if (success) {
                return printMessage("Mot de passe modifie avec succes");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("Le Login de l'utilisateur n'est pas correct");

    }

    public String createCompteClientNSIA(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Entree creation Compte Client NSIA");
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String sql = "SELECT * FROM COMPTESNSIA WHERE numero='" + Utility.bourrageGZero(request.getParameter("numeroCompte"), 12) + "'";
        ComptesNsia[] compteExisted = (ComptesNsia[]) db.retrieveRowAsObject(sql, new ComptesNsia());

        //   String escompte = request.getParameter("esCompte");
        String dateFinEscompte = request.getParameter("dateFinEscompte");
        String dateDebutEscompte = request.getParameter("dateDebutEscompte");

        ComptesNsia aCompte = new ComptesNsia();
        aCompte.setNumero(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
        aCompte.setNom(request.getParameter("nomClient"));
        aCompte.setAgence(request.getParameter("agenceCompte"));
        aCompte.setAdresse1(request.getParameter("adresse"));

        aCompte.setDateFinEscompte(dateFinEscompte);
        aCompte.setDateDebutEscompte(dateDebutEscompte);
        aCompte.setEtat(new BigDecimal("" + Utility.getParam("LCE_SBF").trim()));
        if (compteExisted != null && compteExisted.length > 0) {
            System.out.println("creation Compte Client NSIA. Ce compte existe deja");
            return printMessage("Ce compte existe deja. Modifiez son etat plutôt.");
        } else {
            if (db.insertObjectAsRowByQuery(aCompte, "COMPTESNSIA")) {
                db.close();
                System.out.println("Creation Compte Client NSIA. Compte Client cree avec succes!");
                return printMessage("Compte Client cree avec succes!");

            } else {
                System.out.println("Creation Compte Client NSIA. Erreur d'insertion dans la BD!!");
                db.close();
                return printMessage("Erreur d'insertion dans la BD! " + db.getMessage());
            }
        }

    }

    public String createCompteClientF12(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Comptes aCompte = new Comptes();
        aCompte.setNumero(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
        aCompte.setNom(request.getParameter("nomClient"));
        aCompte.setAgence(request.getParameter("agenceCompte"));
        aCompte.setAdresse2(request.getParameter("agenceF12"));

        if (aCompte.getAgence().length() != 5) {
            return printMessage("Le code Agence doit être sur 5 positions");
        }
        if (aCompte.getAdresse2().length() != 3) {
            return printMessage("Le code Agence FLEXCUBE  doit être sur 3 positions");
        }
        aCompte.setAdresse1(request.getParameter("adresse"));
        aCompte.setNumcptex(request.getParameter("numCptEx"));
        String accountLength = Utility.getParam("ACCOUNT_LENGTH") != null ? Utility.getParam("ACCOUNT_LENGTH") : "12"; //ACCOUNT_LENGTH a mettre sur 12
        if (aCompte.getNumcptex().length() != Integer.parseInt(accountLength)) {
            return printMessage("Le Numéro de compte externe doit être sur " + accountLength + " positions");
        }
        aCompte.setSignature2("R");
        aCompte.setEtat(new BigDecimal(0));

        if (db.insertObjectAsRowByQuery(aCompte, "COMPTES")) {
            db.close();
            return printMessage("Compte Client cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD! " + db.getMessage());
        }

    }

    public String createCompteClient(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Comptes aCompte = new Comptes();
        aCompte.setNumero(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
        aCompte.setNom(request.getParameter("nomClient"));
        aCompte.setAgence(request.getParameter("agenceCompte"));
        if (aCompte.getAgence().length() != 5) {
            return printMessage("Le code Agence doit être sur 5 positions");
        }
        aCompte.setAdresse1(request.getParameter("adresse"));
        aCompte.setNumcptex(request.getParameter("numCptEx"));
        String accountLength = Utility.getParam("ACCOUNT_LENGTH") != null ? Utility.getParam("ACCOUNT_LENGTH") : "12";
        if (aCompte.getNumcptex().length() != Integer.parseInt(accountLength)) {
            return printMessage("Le Numéro de compte externe doit être sur " + accountLength + " positions");
        }
        if(Utility.getParam("VALIDATION_COMPTE")!=null && Utility.getParam("VALIDATION_COMPTE").equals("1")){
        aCompte.setEtat(null);    
        }else{
        aCompte.setEtat(new BigDecimal(0));    
        }
        

        if (db.insertObjectAsRowByQuery(aCompte, "COMPTES")) {
            db.close();
            return printMessage("Compte Client cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD! " + db.getMessage());
        }

    }

    public String createCompteClientACP(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Comptes aCompte = new Comptes();
        aCompte.setNumero(Utility.bourrageGZero(request.getParameter("numeroCompte"), 10));
        aCompte.setNom(request.getParameter("nomClient"));
        aCompte.setAgence(request.getParameter("agenceCompte"));
        aCompte.setAdresse1(request.getParameter("adresse"));
        aCompte.setNumcptex(request.getParameter("numCptEx"));
        aCompte.setEtat(new BigDecimal(0));
        if (db.insertObjectAsRowByQuery(aCompte, "COMPTES")) {
            db.close();
            return printMessage("Compte Client cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD! " + db.getMessage());
        }

    }

    public String creationVirementClientBNP(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("creationVirementClientBNP");
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Virements virement = new Virements();

        Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
        virement.setCodeUtilisateur(user.getLogin().trim());
        virement.setBanqueremettant(CMPUtility.getCodeBanque());

        virement.setBanque(request.getParameter("bancre"));
        virement.setAgence(request.getParameter("agecre"));
        virement.setDevise("XOF");
        virement.setMontantvirement(request.getParameter("montant").replaceAll("\\p{javaWhitespace}+", ""));
        virement.setDateordre(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
        virement.setNumerovirement(request.getParameter("numeroVirement"));
        virement.setReference_Emetteur(request.getParameter("numeroVirement"));
        virement.setAdresse_Beneficiaire(request.getParameter("adresse"));
        virement.setAgenceremettant(request.getParameter("agence"));
        virement.setNumerocompte_Tire("0" + request.getParameter("numero").substring(5));
        virement.setNumerocompte_Beneficiaire(request.getParameter("numeroCompteBen"));
        virement.setNom_Tire(request.getParameter("nom"));
        virement.setNom_Beneficiaire(request.getParameter("nomBeneficiaire"));
        virement.setLibelle(request.getParameter("libelleVirement"));
        System.out.println("Utility.getParam(\"VALIDATION_VIREMENT_AUTO\")" + Utility.getParam("VALIDATION_VIREMENT_AUTO"));
        if (Utility.getParam("VALIDATION_VIREMENT_AUTO") != null && Utility.getParam("VALIDATION_VIREMENT_AUTO").equalsIgnoreCase("AUTO")) {
            virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
            System.out.println("Utility.getParam(\"VALIDATION_VIREMENT_AUTO\")" + Utility.getParam("VALIDATION_VIREMENT_AUTO"));

        } else {
            virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL")));
        }

        virement.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
        //virement.setDatecompensation(Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd"));
        if (Character.isDigit(virement.getBanque().charAt(1))) {
            virement.setEtablissement(CMPUtility.getCodeBanque());
            virement.setBanqueremettant(CMPUtility.getCodeBanque());
            virement.setType_Virement("010");
        } else {
            virement.setEtablissement(CMPUtility.getCodeBanqueSica3());
            virement.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
            virement.setType_Virement("015");
        }
        virement.setVille("01");

        virement.setIdvirement(new BigDecimal(Utility.computeCompteur("IDVIREMENT", "VIREMENTS")));

        if (db.insertObjectAsRowByQuery(virement, "VIREMENTS")) {
            db.close();
            return printMessage("Virement Client cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD! " + db.getMessage());
        }

    }

    public String createVirementClient(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("createVirementClient sout");
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Virements virement = new Virements();

        Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
        virement.setCodeUtilisateur(user.getLogin().trim());
        virement.setBanqueremettant(CMPUtility.getCodeBanque());

        virement.setBanque(request.getParameter("bancre"));
        virement.setAgence(request.getParameter("agecre"));
        virement.setDevise("XOF");
        virement.setMontantvirement(request.getParameter("montant").replaceAll("\\p{javaWhitespace}+", ""));
        virement.setDateordre(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
        virement.setNumerovirement(request.getParameter("numeroVirement"));
        virement.setReference_Emetteur(request.getParameter("numeroVirement"));
        virement.setAdresse_Beneficiaire(request.getParameter("adresse"));
        virement.setAgenceremettant(request.getParameter("agence"));
        virement.setNumerocompte_Tire(request.getParameter("numero"));
        virement.setNumerocompte_Beneficiaire(request.getParameter("numeroCompteBen"));
        virement.setNom_Tire(request.getParameter("nom"));
        virement.setNom_Beneficiaire(request.getParameter("nomBeneficiaire"));
        virement.setLibelle(request.getParameter("libelleVirement"));
        System.out.println("Utility.getParam(\"VALIDATION_VIREMENT_AUTO\")" + Utility.getParam("VALIDATION_VIREMENT_AUTO"));
        if (Utility.getParam("VALIDATION_VIREMENT_AUTO") != null && Utility.getParam("VALIDATION_VIREMENT_AUTO").equalsIgnoreCase("AUTO")) {
            virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
            System.out.println("Utility.getParam(\"VALIDATION_VIREMENT_AUTO\")" + Utility.getParam("VALIDATION_VIREMENT_AUTO"));

        } else {
            virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL")));
        }

        virement.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
        //virement.setDatecompensation(Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd"));
        if (Character.isDigit(virement.getBanque().charAt(1))) {
            virement.setEtablissement(CMPUtility.getCodeBanque());
            virement.setBanqueremettant(CMPUtility.getCodeBanque());
            virement.setType_Virement("010");
        } else {
            virement.setEtablissement(CMPUtility.getCodeBanqueSica3());
            virement.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
            virement.setType_Virement("015");
        }
        virement.setVille("01");

        virement.setIdvirement(new BigDecimal(Utility.computeCompteur("IDVIREMENT", "VIREMENTS")));

        if (db.insertObjectAsRowByQuery(virement, "VIREMENTS")) {
            db.close();
            return printMessage("Virement Client cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD! " + db.getMessage());
        }

    }

    public String createVirementClientACP(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Virements virement = new Virements();
        virement.setBanqueremettant(CMPUtility.getCodeBanque());

        virement.setBanque(request.getParameter("bancre"));
        virement.setAgence(request.getParameter("agecre"));
        virement.setDevise("GNF");
        virement.setMontantvirement(request.getParameter("montant").replaceAll("\\p{javaWhitespace}+", ""));
        virement.setDateordre(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
        virement.setNumerovirement(request.getParameter("numeroVirement"));

        virement.setAgenceremettant(request.getParameter("agence"));
        virement.setAdresse_Beneficiaire(request.getParameter("adresse"));
        virement.setNumerocompte_Tire(request.getParameter("numero"));
        virement.setNumerocompte_Beneficiaire(request.getParameter("numeroCompteBen"));
        virement.setNom_Tire(request.getParameter("nom"));
        virement.setNom_Beneficiaire(request.getParameter("nomBeneficiaire"));
        virement.setLibelle(request.getParameter("libelleVirement"));
        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL")));
        virement.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
        //virement.setDatecompensation(Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd"));

        virement.setEtablissement(CMPUtility.getCodeBanque());
        virement.setBanqueremettant(CMPUtility.getCodeBanque());
        virement.setType_Virement(Utility.getParam(request.getParameter("typevirement")));

        virement.setVille("01");

        virement.setIdvirement(new BigDecimal(Utility.computeCompteur("IDVIREMENT", "VIREMENTS")));

        if (db.insertObjectAsRowByQuery(virement, "VIREMENTS")) {
            db.close();
            return printMessage("Virement Client cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD! " + db.getMessage());
        }

    }

    public String createVirementBanque(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("createVirementClient create Virement Banque");
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Virements virement = new Virements();
        Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");

        virement.setCodeUtilisateur(user.getLogin().trim());
        virement.setAgenceremettant(request.getParameter("agence"));
        virement.setBanque(request.getParameter("bancre"));
        virement.setAgence(request.getParameter("agecre"));
        virement.setDevise("XOF");
        virement.setMontantvirement(request.getParameter("montant").replaceAll("\\p{javaWhitespace}+", ""));
        virement.setNom_Tire(request.getParameter("nom"));
        virement.setNom_Beneficiaire(request.getParameter("nomBeneficiaire"));
        virement.setAdresse_Beneficiaire(request.getParameter("adresse"));
        virement.setNumerovirement(request.getParameter("numeroVirement"));
        virement.setReference_Emetteur(request.getParameter("numeroVirement"));
        virement.setDateordre(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
        virement.setLibelle(request.getParameter("libelleVirement"));
        System.out.println("Utility.getParam(\"VALIDATION_VIREMENT_AUTO\")" + Utility.getParam("VALIDATION_VIREMENT_AUTO"));
        if (Utility.getParam("VALIDATION_VIREMENT_AUTO") != null && Utility.getParam("VALIDATION_VIREMENT_AUTO").equalsIgnoreCase("AUTO")) {
            virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
            System.out.println("Utility.getParam(\"VALIDATION_VIREMENT_AUTO\")" + Utility.getParam("VALIDATION_VIREMENT_AUTO"));

        } else {
            virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL")));
        }

        virement.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
        //virement.setDatecompensation(Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd"));
        if (Character.isDigit(virement.getBanque().charAt(1))) {
            virement.setEtablissement(CMPUtility.getCodeBanque());
            virement.setBanqueremettant(CMPUtility.getCodeBanque());
            virement.setType_Virement("011");
        } else {
            virement.setEtablissement(CMPUtility.getCodeBanqueSica3());
            virement.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
            virement.setType_Virement("011");
        }
        virement.setVille("01");

        virement.setIdvirement(new BigDecimal(Utility.computeCompteur("IDVIREMENT", "VIREMENTS")));

        if (db.insertObjectAsRowByQuery(virement, "VIREMENTS")) {
            db.close();
            return printMessage("Virement Banque a Banque cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD!" + db.getMessage());
        }

    }

    public String createVirementDisposition(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Virements virement = new Virements();
        Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");

        virement.setCodeUtilisateur(user.getLogin().trim());

        virement.setAgenceremettant(request.getParameter("agence"));
        virement.setNumerocompte_Tire(request.getParameter("numero"));
        virement.setBanque(request.getParameter("bancre"));
        virement.setAgence(request.getParameter("agecre"));
        virement.setDevise("XOF");
        virement.setMontantvirement(request.getParameter("montant").replaceAll("\\p{javaWhitespace}+", ""));
        virement.setNom_Tire(request.getParameter("nom"));
        virement.setAdresse_Tire(request.getParameter("nom"));
        virement.setNom_Beneficiaire(request.getParameter("nomBeneficiaire"));
        virement.setAdresse_Beneficiaire(request.getParameter("adresse"));
        virement.setReference_Emetteur(request.getParameter("numeroVirement"));
        virement.setNumerovirement(request.getParameter("numeroVirement"));
        virement.setDateordre(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
        virement.setLibelle(request.getParameter("libelleVirement"));
        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL")));
        virement.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
        //virement.setDatecompensation(Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd"));
        if (Character.isDigit(virement.getBanque().charAt(1))) {
            virement.setEtablissement(CMPUtility.getCodeBanque());
            virement.setBanqueremettant(CMPUtility.getCodeBanque());
            virement.setType_Virement("012");
        } else {
            virement.setEtablissement(CMPUtility.getCodeBanqueSica3());
            virement.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
            virement.setType_Virement("017");
        }
        virement.setVille("01");

        virement.setIdvirement(new BigDecimal(Utility.computeCompteur("IDVIREMENT", "VIREMENTS")));

        if (db.insertObjectAsRowByQuery(virement, "VIREMENTS")) {
            db.close();
            return printMessage("Virement Mise a Disposition cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD! " + db.getMessage());
        }

    }

    public String createEffetClient(HttpServletRequest request, HttpServletResponse response) {

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Effets effet = new Effets();
        effet.setBanqueremettant(CMPUtility.getCodeBanque());
        Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");

        effet.setCodeUtilisateur(user.getLogin().trim());

        effet.setBanque(request.getParameter("bandeb"));
        effet.setAgence(request.getParameter("agedeb"));
        effet.setDevise("XOF");
        effet.setMontant_Effet(request.getParameter("montant"));
        effet.setMontant_Frais(request.getParameter("montantfrais"));
        effet.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
        effet.setNumeroeffet(request.getParameter("numeroEffet"));
        if (effet.getNumeroeffet().equalsIgnoreCase("0000000000") || effet.getNumeroeffet().contains("/")) {
            db.close();
            return printMessage("Le numero de l'effet doit etre different de 0000000000\n et ne peut contenir que des chiffres !");
        }
        effet.setAgenceremettant(request.getParameter("agence"));
        effet.setNumerocompte_Beneficiaire(request.getParameter("numero"));
        effet.setNumerocompte_Tire(Utility.bourrageGZero(request.getParameter("numeroCompteTire"), 12));
        effet.setNom_Beneficiaire(request.getParameter("nom"));
        effet.setDate_Echeance(request.getParameter("dateecheance"));
        effet.setDate_Creation(request.getParameter("datecreation"));
        effet.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));

        effet.setNom_Tire(request.getParameter("nomTire"));
        effet.setCode_Acceptation("1");
        effet.setIdentification_Tire(request.getParameter("libelleEffet"));
        if (Utility.getParam("VALIDATION_EFFET_AUTO") != null && Utility.getParam("VALIDATION_EFFET_AUTO").equalsIgnoreCase("AUTO")) {
            effet.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
        } else {
            effet.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL")));
        }

        if (Character.isDigit(effet.getBanque().charAt(1))) {
            effet.setEtablissement(CMPUtility.getCodeBanque());
            effet.setBanqueremettant(CMPUtility.getCodeBanque());
            effet.setType_Effet("043");
        } else {
            effet.setEtablissement(CMPUtility.getCodeBanqueSica3());
            effet.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
            effet.setType_Effet("046");
        }
        effet.setVille("01");

        effet.setIdeffet(new BigDecimal(Utility.computeCompteur("IDEFFET", "EFFETS")));

        if (db.insertObjectAsRowByQuery(effet, "EFFETS")) {
            db.close();
            return printMessage("Effet Client cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD!" + db.getMessage());
        }

    }

    public String createPrelevementClient(HttpServletRequest request, HttpServletResponse response) {

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Prelevements prelevement = new Prelevements();
        prelevement.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
        Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");

        //prelevement.setCodeUtilisateur(user.getLogin().trim());
        prelevement.setBanque(request.getParameter("bandeb"));
        prelevement.setAgence(request.getParameter("agedeb"));
        prelevement.setDevise(Utility.getParam("DEVISE"));
        prelevement.setMontantprelevement(request.getParameter("montant"));

        prelevement.setDateordre(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
        prelevement.setNumeroprelevement(request.getParameter("numeroAutorisation"));
        if (prelevement.getNumeroprelevement().equalsIgnoreCase("0000000000") || prelevement.getNumeroprelevement().contains("/")) {
            db.close();
            return printMessage("Le numero du prelevement doit etre different de 0000000000\n et ne peut contenir que des chiffres !");
        }
        prelevement.setAgenceremettant(request.getParameter("agence"));
        prelevement.setNumerocompte_Beneficiaire(request.getParameter("numero"));
        prelevement.setNumerocompte_Tire(Utility.bourrageGZero(request.getParameter("numeroCompteTire"), 12));
        prelevement.setNom_Beneficiaire(request.getParameter("nom"));
        prelevement.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));

        prelevement.setNom_Tire(request.getParameter("nomTire"));
        prelevement.setLibelle(request.getParameter("libellePrelevement"));

        prelevement.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL")));

        prelevement.setEtablissement(CMPUtility.getCodeBanqueSica3());
        prelevement.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
        prelevement.setType_prelevement(Utility.getParam("ORDPRENOUNOR"));

        prelevement.setVille("01");

        prelevement.setIdprelevement(new BigDecimal(Utility.computeCompteur("IDPRELEVEMENT", "PRELEVEMENTS")));

        if (db.insertObjectAsRowByQuery(prelevement, "PRELEVEMENTS")) {
            db.close();
            return printMessage("Prelevement Client cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD!" + db.getMessage());
        }

    }

    String annuleObjet(RejetFormBean rejetFormBean) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!rejetFormBean.getIdObjet().trim().equals("")) {
            try {
                String sql = "UPDATE " + rejetFormBean.getObjet() + " SET ETAT = " + Utility.getParam("CETAOPEALLICOM3") + " WHERE " + rejetFormBean.getNomIdObjet() + " = " + rejetFormBean.getIdObjet() + " AND ETAT IN (130,330,135,370)";
                if (db.executeUpdate(sql) == 0) {
                    db.close();
                    return printMessage(rejetFormBean.getObjet() + " " + rejetFormBean.getIdObjet() + " ne peut être annule ");
                }

                if (rejetFormBean.getObjet().equalsIgnoreCase("REMCOM")) {
                    sql = "UPDATE LOTCOM SET ETAT = " + Utility.getParam("CETAOPEALLICOM3") + " WHERE IDREMCOM = " + rejetFormBean.getIdObjet() + " ";
                    db.executeUpdate(sql);
                    sql = "UPDATE CHEQUES SET ETAT = " + Utility.getParam("CETAOPEALLICOM3") + " WHERE REMCOM = " + rejetFormBean.getIdObjet() + "";
                    db.executeUpdate(sql);
                    sql = "UPDATE EFFETS SET ETAT = " + Utility.getParam("CETAOPEALLICOM3") + " WHERE REMCOM = " + rejetFormBean.getIdObjet() + " ";
                    db.executeUpdate(sql);
                    sql = "UPDATE VIREMENTS SET ETAT = " + Utility.getParam("CETAOPEALLICOM3") + " WHERE REMCOM = " + rejetFormBean.getIdObjet() + "";
                    db.executeUpdate(sql);

                }

                if (rejetFormBean.getObjet().equalsIgnoreCase("LOTCOM")) {

                    sql = "UPDATE CHEQUES SET ETAT = " + Utility.getParam("CETAOPEALLICOM3") + " WHERE LOTCOM = " + rejetFormBean.getIdObjet() + "";
                    db.executeUpdate(sql);
                    sql = "UPDATE EFFETS SET ETAT = " + Utility.getParam("CETAOPEALLICOM3") + " WHERE LOTCOM = " + rejetFormBean.getIdObjet() + " ";
                    db.executeUpdate(sql);
                    sql = "UPDATE VIREMENTS SET ETAT = " + Utility.getParam("CETAOPEALLICOM3") + " WHERE LOTCOM = " + rejetFormBean.getIdObjet() + "";
                    db.executeUpdate(sql);

                }

            } catch (SQLException ex) {
                Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        db.close();
        return printMessage(rejetFormBean.getObjet() + " " + rejetFormBean.getIdObjet() + " prêt pour ICOM3 ");
    }

    public String createAgence(HttpServletRequest request, HttpServletResponse response) {
        Agences agences = new Agences();
        agences.setCodeagence(request.getParameter("codeAgence"));
        agences.setCodebanque(request.getParameter("codeBanque"));
        agences.setCodeville(request.getParameter("codeVille"));
        agences.setLibelleagence(request.getParameter("libelleAgence"));

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (db.insertObjectAsRowByQuery(agences, "AGENCES")) {
            db.close();
            return printMessage("Agence cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD! " + db.getMessage());
        }
    }

    public String createBanque(HttpServletRequest request, HttpServletResponse response) {
        Banques banques = new Banques();
        banques.setCodebanque(request.getParameter("codeBanque"));
        banques.setCodepays(request.getParameter("codePays"));
        banques.setLibellebanque(request.getParameter("libelleBanque"));
        banques.setAlgorithmedecontrolespecifique(new BigDecimal(0));

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (db.insertObjectAsRowByQuery(banques, "BANQUES")) {
            db.close();
            return printMessage("Banque cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD!");
        }
    }

    public String createMacuti(HttpServletRequest request, HttpServletResponse response) {

        Macuti macuti = new Macuti();

        macuti.setMachine(request.getParameter("machine"));
        macuti.setUtilisateur(request.getParameter("utilisateur"));
        macuti.setIdmacuti(new BigDecimal(Utility.computeCompteur("IDMACUTI", "MACUTI")));

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (db.insertObjectAsRowByQuery(macuti, "MACUTI")) {
            db.close();
            return printMessage("Macuti cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD!");
        }
    }

    public String refreshInfoPanel() {

        String dateCompensNat = Utility.convertDateToString(Utility.convertStringToDate(Utility.getParam("DATECOMPENS_NAT"), "yyyyMMdd"), ResLoader.getMessages("patternDate"));
        String dateCompensSrg = Utility.convertDateToString(Utility.convertStringToDate(Utility.getParam("DATECOMPENS_SRG"), "yyyyMMdd"), ResLoader.getMessages("patternDate"));
        if (webMonitor != null) {
            return printMessage("|Date de Compensation Nationale : <font color=\"blue\">" + dateCompensNat + "</font> | Date de Compensation Sous-Regionale : <font color=\"blue\">" + dateCompensSrg + "</font> | Etat de WebMonitor : <font color=\"green\">Demarre</font>");
        } else {
            return printMessage("|Date de Compensation Nationale : <font color=\"blue\">" + dateCompensNat + "</font> | Date de Compensation Sous-Regionale : <font color=\"blue\">" + dateCompensSrg + "</font> | Etat de WebMonitor : <font color=\"red\">Arrete</font>");
        }

    }

    public String createMachine(HttpServletRequest request, HttpServletResponse response) {
        MachineScan machine = new MachineScan();
        String libelleAgence = request.getParameter("undefined");
        libelleAgence = libelleAgence.substring(libelleAgence.indexOf("-") + 1);
        machine.setId_machinescan(request.getParameter("idmachinescan"));
        machine.setMachinescan(request.getParameter("machine"));
        machine.setCodeAgence(request.getParameter("agence"));
        machine.setAgence(libelleAgence);
        machine.setContact(request.getParameter("contact"));
        machine.setAdr_ip(request.getParameter("adresseip"));
        machine.setAdr_ip_2(request.getParameter("adresseip2"));
        machine.setMarque(request.getParameter("marque"));
        machine.setModele(request.getParameter("modele"));
        machine.setDateinstall(Utility.convertStringToDate(request.getParameter("dateinstall"), "yyyy/MM/dd"));

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (db.insertObjectAsRowByQuery(machine, "MACHINES")) {
            db.close();
            return printMessage("Machine cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD!");
        }
    }

    public String confirmerVignettes(HttpServletRequest request, HttpServletResponse response) {

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        String idObjet = request.getParameter("idObjet");
        String codeVignette = request.getParameter("codeVignette");
        String numeroEndos = request.getParameter("numeroEndos");
        boolean isVignetteSaisie = numeroEndos.equalsIgnoreCase("XX");
        Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");

        String sql = "SELECT * FROM CHEQUES WHERE IDCHEQUE=" + idObjet;

        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                sql = "SELECT * FROM VIGNETTES WHERE CODEBANQUE='" + cheques[0].getBanque() + "' AND CODEGUICHET='" + cheques[0].getAgence() + "' AND NUMEROCOMPTE='" + cheques[0].getNumerocompte() + "' AND NUMEROCHEQUE='" + cheques[0].getNumerocheque() + "'";
                Vignettes[] vignettes = (Vignettes[]) db.retrieveRowAsObject(sql, new Vignettes());
                if (vignettes != null && vignettes.length > 0) {
                    System.out.println("Vignette trouvee = " + vignettes[0].getCodevignette().substring(14));
                    System.out.println("Vignette saisie = " + codeVignette);

                    if (isVignetteSaisie) {
                        if (vignettes[0].getCodevignette().substring(14).equalsIgnoreCase(codeVignette)) {
                            vignettes[0].setEtat(new BigDecimal(Utility.getParam("CETAVIGVAL")));
                            cheques[0].setEtat(new BigDecimal(Utility.getParam("CETAOPERETRECENVSIBVER")));
                            cheques[0].setLotsib(new BigDecimal("1"));

                        } else {
                            vignettes[0].setEtat(new BigDecimal(Utility.getParam("CETAVIGVER")));
                            cheques[0].setLotsib(new BigDecimal("1"));
                            cheques[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM2")));
                            cheques[0].setMotifrejet("224");

                        }
                    } else {
                        if (vignettes[0].getNumeroendos().equalsIgnoreCase(numeroEndos)) {
                            vignettes[0].setEtat(new BigDecimal(Utility.getParam("CETAVIGVAL")));
                            cheques[0].setEtat(new BigDecimal(Utility.getParam("CETAOPERETRECENVSIBVER")));
                            cheques[0].setLotsib(new BigDecimal("1"));

                        } else {
                            vignettes[0].setEtat(new BigDecimal(Utility.getParam("CETAVIGVER")));
                            cheques[0].setLotsib(new BigDecimal("1"));
                            cheques[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM2")));
                            cheques[0].setMotifrejet("224");

                        }
                    }

                    vignettes[0].setUsermaj(user.getLogin());
                    vignettes[0].setDatemaj(Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyy/MM/dd"));

                    db.updateRowByObjectByQuery(vignettes[0], "VIGNETTES", "IDVIGNETTE=" + vignettes[0].getIdvignette());
                } else {
                    cheques[0].setEtat(new BigDecimal(Utility.getParam("CETAOPERETRECENVSIBERR")));
                    cheques[0].setLotsib(new BigDecimal("1"));

                }
                cheques[0].setLotsib(new BigDecimal("1"));
                db.updateRowByObjectByQuery(cheques[0], "CHEQUES", "IDCHEQUE=" + cheques[0].getIdcheque());

                return printMessage("OK");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        return printMessage("OK");

    }

    public String verifierVignettes(HttpServletRequest request, HttpServletResponse response) {
        MessageBean aMessageBean = new MessageBean();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        String idObjet = request.getParameter("idObjet");
        String codeVignette = request.getParameter("codeVignette");
        String numeroEndos = request.getParameter("numeroEndos");
        boolean isVignetteSaisie = numeroEndos.equalsIgnoreCase("XX");

        String sql = "SELECT * FROM CHEQUES WHERE IDCHEQUE=" + idObjet;

        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                sql = "SELECT * FROM VIGNETTES WHERE CODEBANQUE='" + cheques[0].getBanque() + "' AND CODEGUICHET='" + cheques[0].getAgence() + "' AND NUMEROCOMPTE='" + cheques[0].getNumerocompte() + "' AND NUMEROCHEQUE='" + cheques[0].getNumerocheque() + "'";
                Vignettes[] vignettes = (Vignettes[]) db.retrieveRowAsObject(sql, new Vignettes());
                if (vignettes != null && vignettes.length > 0) {
                    System.out.println("Vignette trouvee = " + vignettes[0].getCodevignette().substring(14));
                    System.out.println("Vignette saisie = " + codeVignette);

                    if (isVignetteSaisie) {
                        if (vignettes[0].getCodevignette().substring(14).equalsIgnoreCase(codeVignette)) {
                            aMessageBean.setAction("1");
                            aMessageBean.setTypeMessage("INFO");
                            aMessageBean.setMessage("Le code vignette " + codeVignette + " correspond a la ligne CMC7");
                            // return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

                        } else {
                            aMessageBean.setAction("9");
                            aMessageBean.setTypeMessage("ERROR");
                            aMessageBean.setMessage("Le code vignette " + codeVignette + " ne correspond pas a la ligne CMC7, Le cheque sera rejete avec le motif rejet 224:Faux cheque/effet identifie par image");
                            //return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
                        }
                    } else {
                        if (vignettes[0].getNumeroendos().equalsIgnoreCase(numeroEndos)) {
                            aMessageBean.setAction("1");
                            aMessageBean.setTypeMessage("INFO");
                            aMessageBean.setMessage("Le numero d'endos " + numeroEndos + " correspond a la ligne CMC7");
                            // return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

                        } else {
                            aMessageBean.setAction("9");
                            aMessageBean.setTypeMessage("ERROR");
                            aMessageBean.setMessage("Le numero d'endos " + numeroEndos + " ne correspond pas a la ligne CMC7, Le cheque sera rejete avec le motif rejet 224:Faux cheque/effet identifie par image");
                            //  return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

                        }
                    }
                    db.close();
                    return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
                }

                aMessageBean.setAction("9");
                aMessageBean.setTypeMessage("ERROR");
                aMessageBean.setMessage("La vignette correspondant a ce cheque n'a pas ete importe dans la base");
                db.close();
                return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        return printMessage("OK");

    }

    public String createRepertoire(HttpServletRequest request, HttpServletResponse response) {
        Repertoires repertoire = new Repertoires();

        repertoire.setId(request.getParameter("ID"));
        repertoire.setChemin(request.getParameter("chemin"));
        repertoire.setExtension(request.getParameter("extension"));
        String partenaire = request.getParameter("partenaire");
        if (partenaire.equalsIgnoreCase("XX")) {
            partenaire = null;
        }
        repertoire.setPartenaire(partenaire);
        repertoire.setTache(request.getParameter("tache"));
        repertoire.setIdRepertoire(new BigDecimal(Utility.computeCompteur("IDREPERTOIRE", "REPERTOIRES")));
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (db.insertObjectAsRowByQuery(repertoire, "REPERTOIRES")) {
            db.close();
            return printMessage("Repertoire cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD!");
        }

    }

    public String createProfils(HttpServletRequest request, HttpServletResponse response) {
        Profils profils = new Profils();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (db.insertObjectAsRowByQuery(profils, "PROFILS")) {
            db.close();
            return printMessage("Profil cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD!" + db.getMessage());
        }

    }

    public String createParams(HttpServletRequest request, HttpServletResponse response) {
        Params params = new Params();

        String typeParam = request.getParameter("type");
        String valeur = request.getParameter("valeur");
        if (typeParam.equals("CODE_CRYPTED")) {
            byte[] encoded = Base64.encodeBase64(valeur.getBytes());
            valeur = new String(encoded);
        }
        params.setNom(request.getParameter("nom"));
        params.setValeur(valeur);
        params.setType(typeParam);
        params.setLibelle(request.getParameter("libelle"));

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (db.insertObjectAsRowByQuery(params, "PARAMS")) {
            db.close();
            return printMessage("Parametre cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD!" + db.getMessage());
        }
    }

    public String createEtablissement(HttpServletRequest request, HttpServletResponse response) {

        Etablissements etablissement = new Etablissements();

        etablissement.setAdmin(request.getParameter("admin"));
        etablissement.setAlgo(new BigDecimal(request.getParameter("algo")));
        etablissement.setCodeetablissement(request.getParameter("codeetablissement"));
        etablissement.setEtat(new BigDecimal(10));
        etablissement.setLibelleetablissement(request.getParameter("libelleetablissement"));
        Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
        etablissement.setUsercreation(user.getNom());
        etablissement.setDatecreation(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
        etablissement.setDatemaj(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (db.insertObjectAsRowByQuery(etablissement, "ETABLISSEMENTS")) {
            db.close();
            return printMessage("Etablissement cree avec succes!");

        } else {
            db.close();
            return printMessage("Erreur d'insertion dans la BD!" + db.getMessage());
        }
    }

    String createProfile(String id, String libelle, String code, String options,HttpServletRequest request ) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] taches = options.split(";");
        String[] tabTaches;
        String regTaches = ";";
        for (int i = 0; i < taches.length; i++) {
            if (!taches[i].isEmpty()) {

                tabTaches = taches[i].split("-");
                regTaches += tabTaches[0] + ";";

            }

        }
        Profils profil = new Profils();
        profil.setIdprofil(new BigDecimal(Utility.computeCompteur("IDPROFIL", "PROFILS")));
        profil.setNomprofil(code);
        profil.setLibelleprofil(libelle);
        profil.setRegtache(regTaches);
        Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
        profil.setUcreation(user.getLogin().trim());
        profil.setUmodification(user.getLogin().trim());
        profil.setDcreation(Utility.convertDateToString(new java.util.Date(), ResLoader.getMessages("patternDate")));
        profil.setDmodification(Utility.convertDateToString(new java.util.Date(), ResLoader.getMessages("patternDate")));
        profil.setEtat(new BigDecimal(Utility.getParam("CETAUTICRE")));
        
        db.insertObjectAsRowByQuery(profil, "PROFILS");
        Params param = new Params();
        param.setNom(code);
        param.setValeur(profil.getIdprofil().toString());
        param.setType("CODE_POIDS");
        param.setLibelle(libelle);
        db.insertObjectAsRowByQuery(param, "PARAMS");
        db.close();
        String jsonresponse = "{success: true, msg: 'Enregistrement effectue avec succes'}";
        return jsonresponse;

    }

    String deleteProfile(String code, String libelle, String id, String options) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            db.executeUpdate("DELETE FROM PROFILS WHERE IDPROFIL=" + id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.close();
        String jsonresponse = "{success: true, msg: 'Enregistrement effectue avec succes'}";
        return jsonresponse;

    }

    String getJSONLoadtreemenu(String recordid) {
        if (recordid.isEmpty()) {
            recordid = "0";
        }
        TreeviewModel treeviewModel = new TreeviewModel(new BigDecimal(recordid));
        String result = treeviewModel.getJsonRepresentation();
        System.out.println(result);
        ;
        return result;
    }

    String rejeteRemise(HttpServletRequest request, HttpServletResponse response) {
        String updateQuery = "UPDATE REMISES SET ETAT =" + Utility.getParam("CETAOPEANO") + " WHERE IDREMISE=" + request.getParameter("primaryValue");
        String table = "REMISES";
        String colonne = "ETAT";
        String oldValue = Utility.getParam("CETAREMVAL");
        String newValue = Utility.getParam("CETAOPEANO");
        String primaryClause = request.getParameter("primaryKey") + "=" + request.getParameter("primaryValue");
        return updateDataTableDev(request, updateQuery, table, colonne, oldValue, newValue, primaryClause, "");

    }

    String startRobotTask(HttpServletRequest request, HttpServletResponse response) {
        try {
            String primaryKey = request.getParameter("primaryKey");
            String primaryValue = request.getParameter("primaryValue");
            MessageBean aMessageBean = new MessageBean();
            aMessageBean.setAction("9");
            if (primaryKey != null && primaryValue != null) {
                if (webMonitor != null) {
                    RobotTask[] robotTasks = Main.getRobotTasks();
                    if (robotTasks != null) {
                        for (int i = 0; i < robotTasks.length; i++) {
                            if (robotTasks[i].getRepertoire().getIdRepertoire().equals(new BigDecimal(primaryValue))) {
                                RobotTask robotTask = robotTasks[i];
                                if (robotTask.isExecution()) {
                                    webMonitor.logEvent("WARNING", "La tache Repertoire " + robotTasks[i].getRepertoire().getIdRepertoire() + " est deja en cours");
                                    aMessageBean.setMessage("La tache Repertoire" + robotTasks[i].getRepertoire().getIdRepertoire() + " est deja en cours");
                                    aMessageBean.setTypeMessage("WARNING");
                                    return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
                                    //return printMessage("La tache "+robotTasks[i].getRepertoire().toString()+ " est deja en cours");
                                } else {
                                    Main.loadRepertoiresCfgFromTable(primaryValue);
                                    webMonitor.logEvent("INFO", "La tache Repertoire " + robotTasks[i].getRepertoire().getIdRepertoire() + " a demarre");
                                    // return printMessage("La tache "+robotTasks[i].getRepertoire().toString()+ " a demarre");
                                    aMessageBean.setMessage("La tache Repertoire " + robotTasks[i].getRepertoire().getIdRepertoire() + " a demarre");
                                    aMessageBean.setTypeMessage("INFO");
                                    return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
                                }
                            }
                        }
                    }
                    Repertoires[] repertoires = Utility.getRepertoires(primaryValue);
                    if (repertoires != null) {
                        //Nouvelle tache a charger
                        Main.loadRepertoiresCfgFromTable(primaryValue);
                        webMonitor.logEvent("INFO", "La tache Repertoire " + repertoires[0].getIdRepertoire() + " a demarre");
                        // return printMessage("La tache "+robotTasks[i].getRepertoire().toString()+ " a demarre");
                        aMessageBean.setMessage("La tache Repertoire " + repertoires[0].getIdRepertoire() + " a demarre");
                        aMessageBean.setTypeMessage("INFO");
                        return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
                    }
                    //  return printMessage("Aucune tache n'est chargee");
                    aMessageBean.setMessage("Aucune tache n'est chargee");
                    aMessageBean.setTypeMessage("ERROR");
                    return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
                }
                // return printMessage("WebMonitor arrete, Veuillez le demarrer avant de demarrer une tache");
                aMessageBean.setMessage("WebMonitor arrete, Veuillez le demarrer avant de demarrer une tache");
                aMessageBean.setTypeMessage("ERROR");
                return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
            }
            //return printMessage("PrimaryKey ="+primaryKey +" PrimaryValue ="+ primaryValue);
            aMessageBean.setMessage("PrimaryKey =" + primaryKey + " PrimaryValue =" + primaryValue);
            aMessageBean.setTypeMessage("ERROR");
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
        } catch (JSONException ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return printMessage("rien");
    }

    String startValidation(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        String utilisateurSaisie = request.getParameter("utilisateur");
        session.setAttribute("utilisateurSaisie", utilisateurSaisie);
        return printMessage("OK");
    }
    
     String startValidationEffet(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        String utilisateurSaisie = request.getParameter("utilisateur"); 
        session.setAttribute("utilisateurSaisie", utilisateurSaisie);
        return printMessage("OK");
    }
    

    String startValidationAgence(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        String agenceDepot = request.getParameter("agenceDepot");
        session.setAttribute("agenceDepot", agenceDepot);
        return printMessage("OK");
    }

    String startValidationEtablissement(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        String agenceDepot = request.getParameter("etablissementChoisi");
        session.setAttribute("etablissementDepot", agenceDepot);
        return printMessage("OK");
    }

    String filterDataTable(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        String filtreChoisi = request.getParameter("filtreChoisi");
        session.setAttribute("filterData", filtreChoisi);
        return printMessage("OK");
    }

    String startValidationParBanque(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        String codeBanque = request.getParameter("codeBanque");
        session.setAttribute("banqueChoisie", codeBanque);
        return printMessage("OK");
    }

    String startValidationParAgence(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        String codeAgence = request.getParameter("codeAgence");
        session.setAttribute("agenceChoisie", codeAgence);
        return printMessage("OK");
    }

    String stopRobotTask(HttpServletRequest request, HttpServletResponse response) {
        try {
            String primaryKey = request.getParameter("primaryKey");
            String primaryValue = request.getParameter("primaryValue");
            MessageBean aMessageBean = new MessageBean();
            aMessageBean.setAction("9");
            if (primaryKey != null && primaryValue != null) {
                if (webMonitor != null) {
                    RobotTask[] robotTasks = Main.getRobotTasks();
                    if (robotTasks != null) {
                        for (int i = 0; i < robotTasks.length; i++) {
                            if (robotTasks[i].getRepertoire().getIdRepertoire().equals(new BigDecimal(primaryValue))) {
                                RobotTask robotTask = robotTasks[i];
                                if (!robotTask.isExecution()) {
                                    webMonitor.logEvent("WARNING", "La tache Repertoire " + robotTasks[i].getRepertoire().getIdRepertoire() + " est deja arrete");
                                    aMessageBean.setMessage("La tache Repertoire " + robotTasks[i].getRepertoire().getIdRepertoire() + " est deja arrete");
                                    aMessageBean.setTypeMessage("WARNING");
                                    return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
                                    //return printMessage("La tache "+robotTasks[i].getRepertoire().toString()+ " est deja en cours");
                                } else {
                                    robotTask.setExecution(false);
                                    webMonitor.logEvent("INFO", "La tache Repertoire " + robotTasks[i].getRepertoire().getIdRepertoire() + " est en cours d'arret");
                                    // return printMessage("La tache "+robotTasks[i].getRepertoire().toString()+ " a demarre");
                                    aMessageBean.setMessage("La tache Repertoire " + robotTasks[i].getRepertoire().getIdRepertoire() + " est en cours d'arret");
                                    aMessageBean.setTypeMessage("INFO");
                                    return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
                                }
                            }
                        }
                        webMonitor.logEvent("ERROR", "Impossible de trouver la tache pour le repertoire " + primaryValue);
                        //return printMessage("Impossible de trouver la tache pour le repertoire "+primaryValue);
                        aMessageBean.setMessage("Impossible de trouver la tache pour le repertoire " + primaryValue);
                        aMessageBean.setTypeMessage("ERROR");
                        return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
                    }
                    //  return printMessage("Aucune tache n'est chargee");
                    aMessageBean.setMessage("Aucune tache n'est chargee");
                    aMessageBean.setTypeMessage("ERROR");
                    return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
                }
                // return printMessage("WebMonitor arrete, Veuillez le demarrer avant de demarrer une tache");
                aMessageBean.setMessage("WebMonitor arrete, Veuillez le demarrer avant d'arreter une tache");
                aMessageBean.setTypeMessage("ERROR");
                return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
            }
            //return printMessage("PrimaryKey ="+primaryKey +" PrimaryValue ="+ primaryValue);
            aMessageBean.setMessage("PrimaryKey =" + primaryKey + " PrimaryValue =" + primaryValue);
            aMessageBean.setTypeMessage("ERROR");
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
        } catch (JSONException ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return printMessage("rien");
    }

    String supprimeObjet(RejetFormBean rejetFormBean) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!rejetFormBean.getIdObjet().trim().equals("")) {
            try {
                String sql = "DELETE FROM " + rejetFormBean.getObjet() + " WHERE " + rejetFormBean.getNomIdObjet() + " = " + rejetFormBean.getIdObjet();
                if (db.executeUpdate(sql) == 0) {
                    db.close();
                    return printMessage(rejetFormBean.getObjet() + " " + rejetFormBean.getIdObjet() + " ne peut être supprime ");
                }

            } catch (SQLException ex) {
                Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        db.close();
        return printMessage(rejetFormBean.getObjet() + " " + rejetFormBean.getIdObjet() + " supprime avec succes ");

    }

    String nettoyageScanner(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            db.executeUpdate("DELETE FROM CHEQUES WHERE ETAT<=" + Utility.getParam("CETAOPESAI") + " AND MACHINESCAN='" + request.getParameter("machine") + "'");
            db.executeUpdate("DELETE FROM REMISES WHERE ETAT<=" + Utility.getParam("CETAREMSAI") + " AND MACHINESCAN='" + request.getParameter("machine") + "'");
            db.executeUpdate("DELETE FROM SEQUENCES WHERE MACHINESCAN='" + request.getParameter("machine") + "'");
            db.executeUpdate("DELETE FROM SEQUENCES_CHEQUES WHERE MACHINESCAN='" + request.getParameter("machine") + "'");
            db.executeUpdate("DELETE FROM CHEQUES_SUPP WHERE ETAT<=" + Utility.getParam("CETAOPESAI") + " AND MACHINESCAN='" + request.getParameter("machine") + "'");

            db.close();
            return printMessage("Nettoyage effectue avec Succes");
        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("Erreur lors du Nettoyage, Procedez a un nettoyage manuel");

    }

    String resetParamsCache(HttpServletRequest request, HttpServletResponse response) {
        Utility.clearParamsCache();
        return printMessage("Cache de Parametres vide");
    }

    String transFormDocument(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            String idObjet = request.getParameter("idObjet");
            String typeObjet = request.getParameter("typeObjet");
            String sequenceObjet = request.getParameter("sequenceObjet");
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            if (sequenceObjet != null && !sequenceObjet.trim().equals("")) {
                db.executeUpdate("UPDATE SEQUENCES SET TYPEDOCUMENT='CHEQUE',CODELINE='____' WHERE IDSEQUENCE=" + sequenceObjet);

                if (typeObjet != null && typeObjet.trim().equals("REMISE")) {
                    Remises remise[] = (Remises[]) db.retrieveRowAsObject("SELECT * FROM REMISES WHERE IDREMISE = " + idObjet, new Remises());
                    if (remise != null && remise.length > 0) {
                        Cheques cheque = new Cheques();

                        cheque.setCodeutilisateur(user.getLogin().trim());
                        cheque.setNumerocheque("_");
                        cheque.setBanque("_");
                        cheque.setAgence("_");
                        cheque.setRibcompte("_");
                        cheque.setNumerocompte("_");
                        cheque.setIdcheque(new BigDecimal(Utility.computeCompteur("IDCHEQUE", "CHEQUES")));
                        cheque.setPathimage(remise[0].getPathimage());
                        cheque.setFichierimage(remise[0].getFichierimage());
                        cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPESTO")));
                        cheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
                        cheque.setSequence(new BigDecimal(sequenceObjet));
                        cheque.setMachinescan(remise[0].getMachinescan());
                        db.executeUpdate("DELETE FROM REMISES WHERE IDREMISE=" + idObjet);
                        String sql = "SELECT * FROM REMISES WHERE NOMUTILISATEUR='" + user.getLogin().trim() + "' AND MACHINESCAN='" + remise[0].getMachinescan() + "' ORDER BY IDREMISE DESC";
                        Remises[] remiseDebut = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                        if (remiseDebut != null && remiseDebut.length > 0) {
                            cheque.setRemise(remiseDebut[0].getIdremise());
                            int nbOperation = db.executeUpdate("UPDATE CHEQUES SET REMISE=" + remiseDebut[0].getIdremise() + " WHERE REMISE=" + idObjet);
                            remiseDebut[0].setNbOperation(new BigDecimal(++nbOperation).add(remiseDebut[0].getNbOperation()));
                            db.updateRowByObjectByQuery(remiseDebut[0], "REMISES", "IDREMISE=" + remiseDebut[0].getIdremise());

                        }

                        db.insertObjectAsRowByQuery(cheque, "CHEQUES");

                    }

                }
                db.close();
                return printMessage("Document transforme avec succes");
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("Erreur de Transformation");

    }

    String updateDataTable(HttpServletRequest request, HttpServletResponse response) {
        MessageBean aMessageBean = new MessageBean();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            String updateQuery = request.getParameter("message");
            String table = request.getParameter("table");
            String colonne = request.getParameter("colonne");
            String oldValue = request.getParameter("oldvalue");
            String newValue = request.getParameter("newvalue");
            String primaryClause = request.getParameter("primaryClause");
            String sql = "";

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            if (db.executeUpdate(updateQuery) == 0) {
                db.close();
                return printMessage("rien");
            }
            //Apres MAJ, Certaines Verifications
            if (table.equalsIgnoreCase("CHEQUES")) {
                sql = "SELECT * FROM CHEQUES WHERE " + primaryClause;
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {
                    Cheques aCheque = cheques[0];
                    if (aCheque.getNumerocheque().contains("_") || aCheque.getBanque().contains("_") || aCheque.getAgence().contains("_") || aCheque.getNumerocompte().contains("_") || aCheque.getRibcompte().contains("_")) {
                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                        aMessageBean.setAction("9");
                        aMessageBean.setTypeMessage("ERROR");
                        aMessageBean.setMessage("La Ligne CMC7 contient des caracteres speciaux, Priere de verifier!");
                        db.updateRowByObjectByQuery(aCheque, table, primaryClause);
                        db.close();
                        return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
                    }

                    if (aCheque.getNumerocheque() == null || aCheque.getNumerocheque().trim().equals("")) {
                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                        aMessageBean.setAction("9");
                        aMessageBean.setTypeMessage("ERROR");
                        aMessageBean.setMessage(" Le Numero du cheque doit être renseigne");
                        db.updateRowByObjectByQuery(aCheque, table, primaryClause);
                        db.close();
                        return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

                    }
                    if (aCheque.getBanque() == null || aCheque.getBanque().trim().equals("")) {
                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                        aMessageBean.setAction("9");
                        aMessageBean.setTypeMessage("ERROR");
                        aMessageBean.setMessage(" La Banque du cheque doit être renseigne");
                        db.updateRowByObjectByQuery(aCheque, table, primaryClause);
                        db.close();
                        return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

                    }
                    if (aCheque.getAgence() == null || aCheque.getAgence().trim().equals("")) {
                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                        aMessageBean.setAction("9");
                        aMessageBean.setTypeMessage("ERROR");
                        aMessageBean.setMessage("Agence du cheque doit être renseigne");
                        db.updateRowByObjectByQuery(aCheque, table, primaryClause);
                        db.close();
                        return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

                    }
                    if (aCheque.getNumerocompte() == null || aCheque.getNumerocompte().trim().equals("")) {
                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                        aMessageBean.setAction("9");
                        aMessageBean.setTypeMessage("ERROR");
                        aMessageBean.setMessage(" Le Numero de compte doit être renseigne");
                        db.updateRowByObjectByQuery(aCheque, table, primaryClause);
                        db.close();
                        return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

                    }
                    if (aCheque.getNumerocheque() == null || aCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(aCheque.getNumerocheque().trim())) {
                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                        aMessageBean.setAction("9");
                        aMessageBean.setTypeMessage("ERROR");
                        aMessageBean.setMessage(" Le Numero de cheque doit être renseigne par un entier");
                        db.updateRowByObjectByQuery(aCheque, table, primaryClause);
                        db.close();
                        return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

                    }
                    if (aCheque.getRibcompte() == null || aCheque.getRibcompte().trim().equals("")) {
                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                        aMessageBean.setAction("9");
                        aMessageBean.setTypeMessage("ERROR");
                        aMessageBean.setMessage(" La cle RIB du cheque doit être renseigne");
                        db.updateRowByObjectByQuery(aCheque, table, primaryClause);
                        db.close();
                        return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

                    }
                    if (aCheque.getBanque() != null && Character.isDigit(aCheque.getBanque().charAt(1))) {
                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                        aMessageBean.setAction("9");
                        aMessageBean.setTypeMessage("ERROR");
                        aMessageBean.setMessage(" Le Code Banque de ce cheque est au format SICA 2. Il ne sera pas accepte.");
                        db.updateRowByObjectByQuery(aCheque, table, primaryClause);
                        db.close();
                        return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

                    }
                    String cleribCal = Utility.computeCleRIB(aCheque.getBanque(), aCheque.getAgence(), aCheque.getNumerocompte());
                    if (!cleribCal.equals(aCheque.getRibcompte())) {
                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                        aMessageBean.setAction("9");
                        aMessageBean.setMessage(" Banque " + aCheque.getBanque() + " Agence " + aCheque.getAgence() + " Le Compte " + aCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
                        aMessageBean.setTypeMessage("ERROR");
                        db.updateRowByObjectByQuery(aCheque, table, primaryClause);
                        db.close();
                        return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

                    }

                }
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("OK");
    }

    String controleCoherenceRemiseUbaGuinee(Remises aRemise, MessageBean aMessageBean, String colonne, String oldValue, String newValue) throws JSONException, SQLException {

        long sumAmount = 0;
        boolean dbResult = true;
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }

        String sql = "SELECT * FROM CHEQUES WHERE ETAT > " + Utility.getParam("CETAOPESAI") + "  AND ETAT<=" + Utility.getParam("CETAOPEALLICOM1") + " AND  REMISE=" + aRemise.getIdremise();
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        try {

            if (cheques != null && cheques.length > 0) {
                if (aRemise.getCdscpta() == null || aRemise.getCdscpta().trim().isEmpty()) {

                    aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                    aMessageBean.setAction("9");
                    aMessageBean.setMessage("Fichier Data Non recupere. Cette remise doit etre scanee a nouveau");
                    aMessageBean.setTypeMessage("ERROR");
                    printMessage("Fichier Data Non recupere. Cette remise doit etre scanee a nouveau. Beneficiaire:" + aRemise.getNomClient());
                    dbResult = db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE=" + aRemise.getIdremise());

                    for (Cheques cheque : cheques) {
                        cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                        dbResult = db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                    }

                    db.close();
                    return "ERROR";
                }
                for (Cheques cheque : cheques) {
                    if (cheque.getDateecheance() == null || cheque.getDateecheance().trim().isEmpty() || cheque.getPathimage() == null
                            || cheque.getPathimage().trim().isEmpty() || cheque.getFichierimage() == null
                            || cheque.getPathimage().trim().isEmpty()) {
                        aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                        aMessageBean.setAction("9");
                        aMessageBean.setMessage("Fichier Cat/ Pak de cheques " + cheque.getIdcheque() + " Non recupere");
                        aMessageBean.setTypeMessage("ERROR");
                        printMessage("Fichier Cat/ Pak du cheque " + cheque.getIdcheque() + "Numero Cheque=" + cheque.getNumerocheque() + " Non recupere" + ". \nCette remise doit etre scanee a nouveau. Beneficiaire:" + aRemise.getNomClient());
                        for (Cheques cheque1 : cheques) {
                            cheque1.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                            dbResult = db.updateRowByObjectByQuery(cheque1, "CHEQUES", "IDCHEQUE=" + cheque1.getIdcheque());
                        }
                        return "ERROR";

                    }

                }
                for (int i = 0; i < cheques.length; i++) {
                    sumAmount += Long.parseLong(cheques[i].getMontantcheque().trim());
                    Cheques currentCheque = cheques[i];
                    sql = "SELECT * FROM ALL_CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAREMSAIIRIS") + ","
                            + "" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + ","
                            + "" + Utility.getParam("CETAOPEVAL2") + "," + Utility.getParam("CETAOPEALLICOM1") + ","
                            + "" + Utility.getParam("CETAOPEALLICOM1ENV") + ","
                            + "" + Utility.getParam("CETAOPEALLPREICOM1") + ","
                            + "" + Utility.getParam("CETAOPEVAL") + ","
                            + "" + Utility.getParam("CETAOPEVALSURCAIENVSIB") + ","
                            + "" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ","
                            + "" + Utility.getParam("CETAOPEVALDELTA") + ") AND BANQUE='" + currentCheque.getBanque() + "' AND AGENCE='" + currentCheque.getAgence() + "' AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "'";
                    Cheques[] existCheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                    if (existCheques != null && existCheques.length > 0) {
                        if (!existCheques[0].getIdcheque().equals(currentCheque.getIdcheque()) && (currentCheque.getCoderepresentation() == null || (currentCheque.getCoderepresentation() != null && currentCheque.getCoderepresentation().intValue() == 0))) {
                            db.close();
                            printMessage("Ce cheque a deja ete valide et n'a pas encore ete rejete.\n"
                                    + "Il reference le cheque avec IDCHEQUE = " + existCheques[0].getIdcheque() + " a l'etat " + existCheques[0].getEtat());
                            return "ERROR";
                        }
                    }
                    String result = controleCoherenceChequeOfRemiseUbaGuinee(currentCheque, aMessageBean);
                    if (result != null && result.equals("OK")) {
                        continue;
                    } else {
                        db.close();
                        return "ERROR";
                    }
                }

                if (sumAmount > Long.parseLong(aRemise.getMontant().trim())) {
                    System.out.println("sumAmount > Long.parseLong(aRemise.getMontant().trim())");
                    aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                    aMessageBean.setAction("9");
                    aMessageBean.setMessage("Montant de la Remise =" + aRemise.getMontant() + " inferieur \nau montant courant de la somme des cheques =" + sumAmount);
                    aMessageBean.setTypeMessage("ERROR");
                    dbResult = db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE=" + aRemise.getIdremise());
                    db.close();
//                   return    jsonConverter.objectToJSONStringArray(aMessageBean);
                    printMessage("Montant de la Remise =" + aRemise.getMontant() + " inferieur \nau montant courant de la somme des cheques =" + sumAmount);
                    return "ERROR";

                }

                if (sumAmount < Long.parseLong(aRemise.getMontant().trim())) {
                    System.out.println("sumAmount < Long.parseLong(aRemise.getMontant().trim())");
                    System.out.println("Here Une erreur est censee s'afficher");
                    aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                    aMessageBean.setAction("9");
                    aMessageBean.setMessage("Montant de la Remise =" + aRemise.getMontant() + " superieur au montant de la somme des cheques =" + sumAmount);
                    aMessageBean.setTypeMessage("ERROR");
                    dbResult = db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE=" + aRemise.getIdremise());
                    db.close();

                    printMessage("Montant de la Remise =" + aRemise.getMontant() + " superieur au montant de la somme des cheques =" + sumAmount);
//                    return    jsonConverter.objectToJSONStringArray(aMessageBean);
                    return "ERROR";

                }

                if (colonne != null && colonne.equalsIgnoreCase("COMPTEREMETTANT")) {
                    ComboCompteBean comboCompteBean = new ComboCompteBean();
                    Comptes aCompte = comboCompteBean.getCompte(newValue);
                    if (aCompte != null) {
                        aRemise.setNomClient(aCompte.getNom());
                        aRemise.setAgenceRemettant(aCompte.getAgence());
                        aRemise.setEscompte(aCompte.getEtat());
                        dbResult = db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE=" + aRemise.getIdremise());
                        for (int i = 0; i < cheques.length; i++) {
                            cheques[i].setCompteremettant(aRemise.getCompteRemettant());
                            cheques[i].setNombeneficiaire(aRemise.getNomClient());
                            cheques[i].setEscompte(aRemise.getEscompte());
                            dbResult = db.updateRowByObjectByQuery(cheques[i], "CHEQUES", "IDCHEQUE=" + cheques[i].getIdcheque());

                        }
                    } else {

                        aMessageBean.setAction("9");
                        aMessageBean.setMessage("Le numero de compte  =" + newValue + " n'existe pas dans la base Clearing");
                        aMessageBean.setTypeMessage("ERROR");
                        aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                        aRemise.setCompteRemettant(oldValue);
                        dbResult = db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE=" + aRemise.getIdremise());
                        db.close();
                        printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
                        return "ERROR";
                    }

                }

                if (colonne != null && colonne.equalsIgnoreCase("ETAT") && newValue.equals(Utility.getParam("CETAOPEALLICOM1"))) {

                    for (int i = 0; i < cheques.length; i++) {
                        System.out.println("cheques.length" + cheques.length);
                        if (cheques[i].getBanque().trim().equalsIgnoreCase(cheques[i].getBanqueremettant().trim())) {
                            cheques[i].setEtat(new BigDecimal(Utility.getParam("CETAOPESUPVALSURCAI")));
                            dbResult = db.updateRowByObjectByQuery(cheques[i], "CHEQUES", "IDCHEQUE=" + cheques[i].getIdcheque());
                        } else {
                            cheques[i].setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
                            dbResult = db.updateRowByObjectByQuery(cheques[i], "CHEQUES", "IDCHEQUE=" + cheques[i].getIdcheque());
                        }
                    }

                } else if (colonne != null && colonne.equalsIgnoreCase("ETAT") && !newValue.equals(Utility.getParam("CETAOPEALLICOM1"))) {

                    sql = "UPDATE CHEQUES SET ETAT=" + newValue + " WHERE REMISE =" + aRemise.getIdremise();
                    if (db.executeUpdate(sql) < 0) {
                        db.close();
                        return "rien";
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            db.close();
            return "rien";
        }
        if (!dbResult) {
            db.close();
            return "rien";
        }
        db.close();

        return printMessage("OK");
    }

    String controleCoherenceRemise(Remises aRemise, MessageBean aMessageBean, String colonne, String oldValue, String newValue) throws JSONException, SQLException {

        long sumAmount = 0;
        boolean dbResult = true;
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        String sql = "SELECT * FROM CHEQUES WHERE ETAT > " + Utility.getParam("CETAOPESAI") + "  AND REMISE=" + aRemise.getIdremise();
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        try {
            if (cheques != null && cheques.length > 0) {
                for (int i = 0; i < cheques.length; i++) {
                    sumAmount += Long.parseLong(cheques[i].getMontantcheque().trim());
                    Cheques currentCheque = cheques[i];
                    sql = "SELECT * FROM ALL_CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAREMSAIIRIS") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEVAL2") + "," + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEVALSURCAIENVSIB") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + "," + Utility.getParam("CETAOPEVALDELTA") + ") AND BANQUE='" + currentCheque.getBanque() + "' AND AGENCE='" + currentCheque.getAgence() + "' AND NUMEROCHEQUE='" + currentCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + currentCheque.getNumerocompte() + "'";
                    Cheques[] existCheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                    if (existCheques != null && existCheques.length > 0) {
                        if (!existCheques[0].getIdcheque().equals(currentCheque.getIdcheque()) && (currentCheque.getCoderepresentation() == null || (currentCheque.getCoderepresentation() != null && currentCheque.getCoderepresentation().intValue() == 0))) {
                            db.close();
                            printMessage("Ce cheque a deja ete valide et n'a pas encore ete rejete.\n"
                                    + "Il reference le cheque avec IDCHEQUE = " + existCheques[0].getIdcheque() + " a l'etat " + existCheques[0].getEtat());
                            return "ERROR";
                        }
                    }
                    String result = controleCoherenceChequeOfRemise(currentCheque, aMessageBean);
                    if (result != null && result.equals("OK")) {
                        continue;
                    } else {
                        db.close();
                        return "ERROR";
                    }
                }

                if (sumAmount > Long.parseLong(aRemise.getMontant().trim())) {
                    System.out.println("sumAmount > Long.parseLong(aRemise.getMontant().trim())");
                    aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                    aMessageBean.setAction("9");
                    aMessageBean.setMessage("Montant de la Remise =" + aRemise.getMontant() + " inferieur \nau montant courant de la somme des cheques =" + sumAmount);
                    aMessageBean.setTypeMessage("ERROR");
                    dbResult = db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE=" + aRemise.getIdremise());
                    db.close();
//                   return    jsonConverter.objectToJSONStringArray(aMessageBean);
                    printMessage("Montant de la Remise =" + aRemise.getMontant() + " inferieur \nau montant courant de la somme des cheques =" + sumAmount);
                    return "ERROR";

                }

                if (sumAmount < Long.parseLong(aRemise.getMontant().trim())) {
                    System.out.println("sumAmount < Long.parseLong(aRemise.getMontant().trim())");
                    System.out.println("Here Une erreur est censee s'afficher");
                    aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                    aMessageBean.setAction("9");
                    aMessageBean.setMessage("Montant de la Remise =" + aRemise.getMontant() + " superieur au montant de la somme des cheques =" + sumAmount);
                    aMessageBean.setTypeMessage("ERROR");
                    dbResult = db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE=" + aRemise.getIdremise());
                    db.close();

                    printMessage("Montant de la Remise =" + aRemise.getMontant() + " superieur au montant de la somme des cheques =" + sumAmount);
//                    return    jsonConverter.objectToJSONStringArray(aMessageBean);
                    return "ERROR";

                }

                if (colonne != null && colonne.equalsIgnoreCase("COMPTEREMETTANT")) {
                    ComboCompteBean comboCompteBean = new ComboCompteBean();
                    Comptes aCompte = comboCompteBean.getCompte(newValue);
                    if (aCompte != null) {
                        aRemise.setNomClient(aCompte.getNom());
                        aRemise.setAgenceRemettant(aCompte.getAgence());
                        aRemise.setEscompte(aCompte.getEtat());
                        dbResult = db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE=" + aRemise.getIdremise());
                        for (int i = 0; i < cheques.length; i++) {
                            cheques[i].setCompteremettant(aRemise.getCompteRemettant());
                            cheques[i].setNombeneficiaire(aRemise.getNomClient());
                            cheques[i].setEscompte(aRemise.getEscompte());
                            dbResult = db.updateRowByObjectByQuery(cheques[i], "CHEQUES", "IDCHEQUE=" + cheques[i].getIdcheque());

                        }
                    } else {

                        aMessageBean.setAction("9");
                        aMessageBean.setMessage("Le numero de compte  =" + newValue + " n'existe pas dans la base Clearing");
                        aMessageBean.setTypeMessage("ERROR");
                        aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                        aRemise.setCompteRemettant(oldValue);
                        dbResult = db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE=" + aRemise.getIdremise());
                        db.close();
                        printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
                        return "ERROR";
                    }

                }

                if (colonne != null && colonne.equalsIgnoreCase("ETAT") && newValue.equals(Utility.getParam("CETAOPEALLICOM1"))) {

                    for (int i = 0; i < cheques.length; i++) {
                        System.out.println("cheques.length" + cheques.length);
                        if (cheques[i].getBanque().trim().equalsIgnoreCase(cheques[i].getBanqueremettant().trim())) {
                            cheques[i].setEtat(new BigDecimal(Utility.getParam("CETAOPESUPVALSURCAI")));
                            dbResult = db.updateRowByObjectByQuery(cheques[i], "CHEQUES", "IDCHEQUE=" + cheques[i].getIdcheque());
                        } else {
                            cheques[i].setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
                            dbResult = db.updateRowByObjectByQuery(cheques[i], "CHEQUES", "IDCHEQUE=" + cheques[i].getIdcheque());
                        }
                    }

                } else if (colonne != null && colonne.equalsIgnoreCase("ETAT") && !newValue.equals(Utility.getParam("CETAOPEALLICOM1"))) {

                    sql = "UPDATE CHEQUES SET ETAT=" + newValue + " WHERE REMISE =" + aRemise.getIdremise();
                    if (db.executeUpdate(sql) < 0) {
                        db.close();
                        return "rien";
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            db.close();
            return "rien";
        }
        if (!dbResult) {
            db.close();
            return "rien";
        }
        db.close();

        return printMessage("OK");
    }

    
    String controleCoherenceRemiseEffet(RemisesEffets aRemiseEffet, MessageBean aMessageBean, String colonne, String oldValue, String newValue) throws JSONException, SQLException {

        long sumAmount = 0;
        boolean dbResult = true;
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        String sql = "SELECT * FROM EFFETS WHERE ETAT > " + Utility.getParam("CETAOPESAI") + "  AND REMISE=" + aRemiseEffet.getIdremise();
        Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        try {
            if (effets != null && effets.length > 0) {
                for (int i = 0; i < effets.length; i++) {
                    sumAmount += Long.parseLong(effets[i].getMontant_Effet().trim());
                    Effets currentEffet = effets[i];
                    sql = "SELECT * FROM ALL_EFFETS WHERE ETAT IN (" + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAREMSAIIRIS") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEVAL2") + "," + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEVALSURCAIENVSIB") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + "," + Utility.getParam("CETAOPEVALDELTA") + ") AND BANQUE='" + currentEffet.getBanque() + "' AND AGENCE='" + currentEffet.getAgence() + "' AND NUMEROEFFET='" + currentEffet.getNumeroeffet() + "' AND NUMEROCOMPTE_TIRE='" + currentEffet.getNumerocompte_Tire() + "'";
                    Effets[] existEffets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                    if (existEffets != null && existEffets.length > 0) {
                        if (!existEffets[0].getIdeffet().equals(currentEffet.getIdeffet()) && (currentEffet.getCoderepresentation() == null || (currentEffet.getCoderepresentation() != null && currentEffet.getCoderepresentation().intValue() == 0))) {
                            db.close();
                            printMessage("Cet effet a deja ete valide et n'a pas encore ete rejete.\n"
                                    + "Il reference le effet avec IDEFFET = " + existEffets[0].getIdeffet() + " a l'etat " + existEffets[0].getEtat());
                            return "ERROR";
                        }
                    }
                   // String result = controleCoherenceChequeOfRemise(currentEffet, aMessageBean);
                   String result = controleCoherenceEffetsOfRemiseEffets(currentEffet, aMessageBean);
                    
                    
                    if (result != null && result.equals("OK")) {
                        continue;
                    } else {
                        db.close();
                        return "ERROR";
                    }
                }

                if (sumAmount > Long.parseLong(aRemiseEffet.getMontant().trim())) {
                    System.out.println("sumAmount > Long.parseLong(aRemise.getMontant().trim())");
                    aRemiseEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                    aMessageBean.setAction("9");
                    aMessageBean.setMessage("Montant de la Remise =" + aRemiseEffet.getMontant() + " inferieur \nau montant courant de la somme des effets =" + sumAmount);
                    aMessageBean.setTypeMessage("ERROR");
                    dbResult = db.updateRowByObjectByQuery(aRemiseEffet, "REMISESEFFETS", "IDREMISE=" + aRemiseEffet.getIdremise());
                    db.close();
//                   return    jsonConverter.objectToJSONStringArray(aMessageBean);
                    printMessage("Montant de la Remise =" + aRemiseEffet.getMontant() + " inferieur \nau montant courant de la somme des effets =" + sumAmount);
                    return "ERROR";

                }

                if (sumAmount < Long.parseLong(aRemiseEffet.getMontant().trim())) {
                    System.out.println("sumAmount < Long.parseLong(aRemise.getMontant().trim())");
                    System.out.println("Here Une erreur est censee s'afficher");
                    aRemiseEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                    aMessageBean.setAction("9");
                    aMessageBean.setMessage("Montant de la Remise =" + aRemiseEffet.getMontant() + " superieur au montant de la somme des effets =" + sumAmount);
                    aMessageBean.setTypeMessage("ERROR");
                    dbResult = db.updateRowByObjectByQuery(aRemiseEffet, "REMISESEFFETS", "IDREMISE=" + aRemiseEffet.getIdremise());
                    db.close();

                    printMessage("Montant de la Remise =" + aRemiseEffet.getMontant() + " superieur au montant de la somme des effets =" + sumAmount);
//                    return    jsonConverter.objectToJSONStringArray(aMessageBean);
                    return "ERROR";

                }

                if (colonne != null && colonne.equalsIgnoreCase("COMPTEREMETTANT")) {
                    ComboCompteBean comboCompteBean = new ComboCompteBean();
                    Comptes aCompte = comboCompteBean.getCompte(newValue);
                    if (aCompte != null) {
                        aRemiseEffet.setNomClient(aCompte.getNom());
                        aRemiseEffet.setAgenceRemettant(aCompte.getAgence());
                        aRemiseEffet.setEscompte(aCompte.getEtat());
                        dbResult = db.updateRowByObjectByQuery(aRemiseEffet, "REMISESEFFETS", "IDREMISE=" + aRemiseEffet.getIdremise());
                        for (int i = 0; i < effets.length; i++) {
                            effets[i].setNumerocompte_Beneficiaire(aRemiseEffet.getCompteRemettant());
                            effets[i].setNom_Beneficiaire(aRemiseEffet.getNomClient());
                            //effets[i].setEscompte(aRemiseEffet.getEscompte());
                            dbResult = db.updateRowByObjectByQuery(effets[i], "EFFETS", "IDEFFET=" + effets[i].getIdeffet());

                        }
                    } else {

                        aMessageBean.setAction("9");
                        aMessageBean.setMessage("Le numero de compte  =" + newValue + " n'existe pas dans la base Clearing");
                        aMessageBean.setTypeMessage("ERROR");
                        aRemiseEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                        aRemiseEffet.setCompteRemettant(oldValue);
                        dbResult = db.updateRowByObjectByQuery(aRemiseEffet, "REMISESEFFETS", "IDREMISE=" + aRemiseEffet.getIdremise());
                        db.close();
                        printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
                        return "ERROR";
                    }

                }

                if (colonne != null && colonne.equalsIgnoreCase("ETAT") && newValue.equals(Utility.getParam("CETAOPEALLICOM1"))) {

                    for (int i = 0; i < effets.length; i++) {
                        System.out.println("effets.length" + effets.length);
                        if (effets[i].getBanque().trim().equalsIgnoreCase(effets[i].getBanqueremettant().trim())) {
                            effets[i].setEtat(new BigDecimal(Utility.getParam("CETAOPESUPVALSURCAI")));
                            dbResult = db.updateRowByObjectByQuery(effets[i], "EFFETS", "IDEFFET=" + effets[i].getIdeffet());
                        } else {
                            effets[i].setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
                            dbResult = db.updateRowByObjectByQuery(effets[i], "EFFETS", "IDEFFET=" + effets[i].getIdeffet());
                        }
                    }

                } else if (colonne != null && colonne.equalsIgnoreCase("ETAT") && !newValue.equals(Utility.getParam("CETAOPEALLICOM1"))) {

                    sql = "UPDATE EFFETS SET ETAT=" + newValue + " WHERE REMISE =" + aRemiseEffet.getIdremise();
                    if (db.executeUpdate(sql) < 0) {
                        db.close();
                        return "rien";
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            db.close();
            return "rien";
        }
        if (!dbResult) {
            db.close();
            return "rien";
        }
        db.close();

        return printMessage("OK");
    }

    
    
    String controleCoherenceCheque(Cheques aCheque, MessageBean aMessageBean) throws Exception {
        Remises aRemise = null;
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        if (aCheque.getNumerocheque().contains("_") || aCheque.getBanque().contains("_") || aCheque.getAgence().contains("_") || aCheque.getNumerocompte().contains("_") || aCheque.getRibcompte().contains("_")) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage("La Ligne CMC7 contient des caracteres speciaux, Priere de verifier!");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
        }

        if (aCheque.getNumerocheque() == null || aCheque.getNumerocheque().trim().equals("")) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" Le Numero du cheque doit être renseigne");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

        }
        if (aCheque.getBanque() == null || aCheque.getBanque().trim().equals("")) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" La Banque du cheque doit être renseigne");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

        }
        if (aCheque.getAgence() == null || aCheque.getAgence().trim().equals("")) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage("Agence du cheque doit être renseigne");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

        }
        if (aCheque.getNumerocompte() == null || aCheque.getNumerocompte().trim().equals("")) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" Le Numero de compte doit être renseigne");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

        }
        if (aCheque.getNumerocheque() == null || aCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(aCheque.getNumerocheque().trim())) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" Le Numero de cheque doit être renseigne par un entier");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

        }
        if (aCheque.getRibcompte() == null || aCheque.getRibcompte().trim().equals("")) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" La cle RIB du cheque doit être renseigne");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

        }
        if (aCheque.getBanque() != null && Character.isDigit(aCheque.getBanque().charAt(1))) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" Le Code Banque de ce cheque est au format SICA 2. Il ne sera pas accepte.");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

        }
        String cleribCal = Utility.computeCleRIB(aCheque.getBanque(), aCheque.getAgence(), aCheque.getNumerocompte());
        if (!cleribCal.equals(aCheque.getRibcompte())) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            aMessageBean.setAction("9");
            aMessageBean.setMessage(" Banque " + aCheque.getBanque() + " Agence " + aCheque.getAgence() + " Le Compte " + aCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
            aMessageBean.setTypeMessage("ERROR");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

        }

        String sql = "SELECT * FROM REMISES WHERE IDREMISE=" + aCheque.getRemise();
        Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
        if (remises != null && remises.length > 0) {
            aRemise = remises[0];
            String result = controleCoherenceRemise(aRemise, aMessageBean, null, null, null);
            if (result != null && !result.equals("OK")) {
                return result;
            }

        }
        db.close();
        return printMessage("OK");
    }
    
    
    String controleCoherenceEffet(Effets aEffet, MessageBean aMessageBean) throws Exception {
        RemisesEffets aRemiseEffet = null;
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        if (aEffet.getNumeroeffet().contains("_") || aEffet.getBanque().contains("_") || aEffet.getAgence().contains("_") || aEffet.getNumerocompte_Tire().contains("_") || aEffet.getRibcompte().contains("_")) {
            aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage("La Ligne CMC7 contient des caracteres speciaux, Priere de verifier!");
            db.updateRowByObjectByQuery(aEffet, "EFFETS", "IDEFFET =" + aEffet.getIdeffet());
            db.close();
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
        }

        if (aEffet.getNumeroeffet()== null || aEffet.getNumeroeffet().trim().equals("")) {
            aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" Le Numero de l'effet doit être renseigne");
            db.updateRowByObjectByQuery(aEffet, "EFFETS", "IDEFFET =" + aEffet.getIdeffet());
            db.close();
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

        }
        if (aEffet.getBanque() == null || aEffet.getBanque().trim().equals("")) {
            aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" La Banque de l'effet doit être renseigne");
            db.updateRowByObjectByQuery(aEffet, "EFFETS", "IDEFFET =" + aEffet.getIdeffet());
            db.close();
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

        }
        if (aEffet.getAgence() == null || aEffet.getAgence().trim().equals("")) {
            aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage("Agence de l'effet doit être renseigne");
            db.updateRowByObjectByQuery(aEffet, "EFFETS", "IDEFFET =" + aEffet.getIdeffet());
            db.close();
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

        }
        if (aEffet.getNumerocompte_Tire()== null || aEffet.getNumerocompte_Tire().trim().equals("")) {
            aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" Le Numero de compte doit être renseigne");
            db.updateRowByObjectByQuery(aEffet, "EFFETS", "IDEFFET =" + aEffet.getIdeffet());
            db.close();
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

        }
        if (aEffet.getNumeroeffet()== null || aEffet.getNumeroeffet().trim().equals("") || !Utility.isInteger(aEffet.getNumeroeffet().trim())) {
            aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" Le Numero de l'effet doit être renseigne par un entier");
            db.updateRowByObjectByQuery(aEffet, "EFFETS", "IDEFFET =" + aEffet.getIdeffet());
            db.close();
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

        }
        if (aEffet.getRibcompte() == null || aEffet.getRibcompte().trim().equals("")) {
            aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" La cle RIB de l'effet doit être renseigne");
            db.updateRowByObjectByQuery(aEffet, "EFFETS", "IDEFFET =" + aEffet.getIdeffet());
            db.close();
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

        }
        if (aEffet.getBanque() != null && Character.isDigit(aEffet.getBanque().charAt(1))) {
            aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" Le Code Banque de cet effet  est au format SICA 2. Il ne sera pas accepte.");
            db.updateRowByObjectByQuery(aEffet, "EFFETS", "IDEFFET =" + aEffet.getIdeffet());
            db.close();
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

        }
        String cleribCal = Utility.computeCleRIB(aEffet.getBanque(), aEffet.getAgence(), aEffet.getNumerocompte_Tire());
        if (!cleribCal.equals(aEffet.getRibcompte())) {
            aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            aMessageBean.setAction("9");
            aMessageBean.setMessage(" Banque " + aEffet.getBanque() + " Agence " + aEffet.getAgence() + " Le Compte " + aEffet.getNumerocompte_Tire()+ " donne la cle rib " + cleribCal);
            aMessageBean.setTypeMessage("ERROR");
            db.updateRowByObjectByQuery(aEffet, "EFFETS", "IDEFFET =" + aEffet.getIdeffet());
            db.close();
            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

        }

        String sql = "SELECT * FROM REMISESEFFETS WHERE IDREMISE=" + aEffet.getRemise();
        RemisesEffets[] remisesEffets = (RemisesEffets[]) db.retrieveRowAsObject(sql, new RemisesEffets());
        if (remisesEffets != null && remisesEffets.length > 0) {
            aRemiseEffet = remisesEffets[0];
            //String result = controleCoherenceRemise(aRemiseEffet, aMessageBean, null, null, null);      
            String result = controleCoherenceRemiseEffet(aRemiseEffet, aMessageBean, null, null, null);

            if (result != null && !result.equals("OK")) {
                return result;
            }

        }
        db.close();
        return printMessage("OK");
    }
    

    String controleCoherenceChequeOfRemise(Cheques aCheque, MessageBean aMessageBean) throws Exception {
        Remises aRemise = null;
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String sql = "SELECT * FROM REMISES WHERE IDREMISE=" + aCheque.getRemise();
        Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
        if (remises != null && remises.length > 0) {
            aRemise = remises[0];

        }

        if (aCheque.getNumerocheque().contains("_") || aCheque.getBanque().contains("_") || aCheque.getAgence().contains("_") || aCheque.getNumerocompte().contains("_") || aCheque.getRibcompte().contains("_")) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemise != null) {
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE =" + aCheque.getRemise());
                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aCheque.getRemise());
            }

            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage("La Ligne CMC7 contient des caracteres speciaux, Priere de verifier!");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage(aMessageBean.getMessage());
        }

        if (aCheque.getNumerocheque() == null || aCheque.getNumerocheque().trim().equals("")) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemise != null) {
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE =" + aCheque.getRemise());
                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aCheque.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" Le Numero du cheque doit être renseigne");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }
        if (aCheque.getBanque() == null || aCheque.getBanque().trim().equals("")) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemise != null) {
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE =" + aCheque.getRemise());
                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aCheque.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" La Banque du cheque doit être renseigne");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }
        if (aCheque.getAgence() == null || aCheque.getAgence().trim().equals("")) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemise != null) {
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE =" + aCheque.getRemise());
                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aCheque.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage("Agence du cheque doit être renseigne");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }
        if (aCheque.getNumerocompte() == null || aCheque.getNumerocompte().trim().equals("")) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemise != null) {
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE =" + aCheque.getRemise());
                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aCheque.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" Le Numero de compte doit être renseigne");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }
        if (aCheque.getNumerocheque() == null || aCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(aCheque.getNumerocheque().trim())) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemise != null) {
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE =" + aCheque.getRemise());
                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aCheque.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" Le Numero de cheque doit être renseigne par un entier");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }
        if (aCheque.getRibcompte() == null || aCheque.getRibcompte().trim().equals("")) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemise != null) {
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE =" + aCheque.getRemise());
                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aCheque.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" La cle RIB du cheque doit être renseigne");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }
        if (aCheque.getBanque() != null && Character.isDigit(aCheque.getBanque().charAt(1))) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemise != null) {
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE =" + aCheque.getRemise());
                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aCheque.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" Le Code Banque de ce cheque est au format SICA 2. Il ne sera pas accepte.");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }
        String cleribCal = Utility.computeCleRIB(aCheque.getBanque(), aCheque.getAgence(), aCheque.getNumerocompte());
        if (!cleribCal.equals(aCheque.getRibcompte())) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemise != null) {
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE =" + aCheque.getRemise());
                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aCheque.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setMessage(" Banque " + aCheque.getBanque() + " Agence " + aCheque.getAgence() + " Le Compte " + aCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
            aMessageBean.setTypeMessage("ERROR");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }

        db.close();
        return "OK";
    }
    
     String controleCoherenceEffetsOfRemiseEffets(Effets aEffet, MessageBean aMessageBean) throws Exception {
        RemisesEffets aRemiseEffet = null;
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String sql = "SELECT * FROM REMISESEFFETS WHERE IDREMISE=" + aEffet.getRemise();
        RemisesEffets[] remisesEffets = (RemisesEffets[]) db.retrieveRowAsObject(sql, new RemisesEffets());
        if (remisesEffets != null && remisesEffets.length > 0) {
            aRemiseEffet = remisesEffets[0];

        }

        if (aEffet.getNumeroeffet().contains("_") || aEffet.getBanque().contains("_") || aEffet.getAgence().contains("_") || aEffet.getNumerocompte_Tire().contains("_") || aEffet.getRibcompte().contains("_")) {
            aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemiseEffet != null) {
                aRemiseEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemiseEffet, "REMISESEFFETS", "IDREMISE =" + aEffet.getRemise());
                db.executeUpdate("UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aEffet.getRemise());
            }

            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage("La Ligne CMC7 contient des caracteres speciaux, Priere  verifier!");
            db.updateRowByObjectByQuery(aEffet, "EFFETS", "IDEFFET =" + aEffet.getIdeffet());
            db.close();
            return printMessage(aMessageBean.getMessage());
        }

        if (aEffet.getNumeroeffet() == null || aEffet.getNumeroeffet().trim().equals("")) {
            aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemiseEffet != null) {
                aRemiseEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemiseEffet, "REMISESEFFETS", "IDREMISE =" + aEffet.getRemise());
                db.executeUpdate("UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aEffet.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" Le Numero de l'effet doit être renseigne");
            db.updateRowByObjectByQuery(aEffet, "EFFETS", "IDEFFET =" + aEffet.getIdeffet());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }
        if (aEffet.getBanque() == null || aEffet.getBanque().trim().equals("")) {
            aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemiseEffet != null) {
                aRemiseEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemiseEffet, "REMISESEFFETS", "IDREMISE =" + aEffet.getRemise());
                db.executeUpdate("UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aEffet.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" La Banque du cheque doit être renseigne");
            db.updateRowByObjectByQuery(aEffet, "EFFETS", "IDEFFET =" + aEffet.getIdeffet());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }
        if (aEffet.getAgence() == null || aEffet.getAgence().trim().equals("")) {
            aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemiseEffet != null) {
                aRemiseEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemiseEffet, "REMISESEFFETS", "IDREMISE =" + aEffet.getRemise());
                db.executeUpdate("UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aEffet.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage("Agence de l'effet doit être renseigne");
            db.updateRowByObjectByQuery(aEffet, "EFFETS", "IDEFFET =" + aEffet.getIdeffet());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }
        if (aEffet.getNumerocompte_Tire()== null || aEffet.getNumerocompte_Tire().trim().equals("")) {
            aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemiseEffet != null) {
                aRemiseEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemiseEffet, "REMISESEFFETS", "IDREMISE =" + aEffet.getRemise());
                db.executeUpdate("UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aEffet.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" Le Numero de compte doit être renseigne");
            db.updateRowByObjectByQuery(aEffet, "EFFETS", "IDEFFET =" + aEffet.getIdeffet());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }
        if (aEffet.getNumeroeffet()== null || aEffet.getNumeroeffet().trim().equals("") || !Utility.isInteger(aEffet.getNumeroeffet().trim())) {
            aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemiseEffet != null) {
                aRemiseEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemiseEffet, "REMISESEFFETS", "IDREMISE =" + aEffet.getRemise());
                db.executeUpdate("UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aEffet.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" Le Numero de l'effet doit être renseigne par un entier");
            db.updateRowByObjectByQuery(aEffet, "EFFETS", "IDEFFET =" + aEffet.getIdeffet());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }
        if (aEffet.getRibcompte() == null || aEffet.getRibcompte().trim().equals("")) {
            aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemiseEffet != null) {
                aRemiseEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemiseEffet, "REMISESEFFETS", "IDREMISE =" + aEffet.getRemise());
                db.executeUpdate("UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aEffet.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" La cle RIB de l'effet doit être renseigne");
            db.updateRowByObjectByQuery(aEffet, "EFFETS", "IDEFFET =" + aEffet.getIdeffet());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }
        if (aEffet.getBanque() != null && Character.isDigit(aEffet.getBanque().charAt(1))) {
            aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemiseEffet != null) {
                aRemiseEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemiseEffet, "REMISESEFFETS", "IDREMISE =" + aEffet.getRemise());
                db.executeUpdate("UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aEffet.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" Le Code Banque de cet effet est au format SICA 2. Il ne sera pas accepte.");
            db.updateRowByObjectByQuery(aEffet, "EFFETS", "IDEFFET =" + aEffet.getIdeffet());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }
        String cleribCal = Utility.computeCleRIB(aEffet.getBanque(), aEffet.getAgence(), aEffet.getNumerocompte_Tire());
        if (!cleribCal.equals(aEffet.getRibcompte())) {
            aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemiseEffet != null) {
                aRemiseEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemiseEffet, "REMISESEFFETS", "IDREMISE =" + aEffet.getRemise());
                db.executeUpdate("UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aEffet.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setMessage(" Banque " + aEffet.getBanque() + " Agence " + aEffet.getAgence() + " Le Compte " + aEffet.getNumerocompte_Tire()+ " donne la cle rib " + cleribCal);
            aMessageBean.setTypeMessage("ERROR");
            db.updateRowByObjectByQuery(aEffet, "EFFETS", "IDEFFET =" + aEffet.getIdeffet());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }

        db.close();
        return "OK";
    }
    
//

    String controleCoherenceChequeOfRemiseUbaGuinee(Cheques aCheque, MessageBean aMessageBean) throws Exception {
        Remises aRemise = null;
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String sql = "SELECT * FROM REMISES WHERE IDREMISE=" + aCheque.getRemise();
        Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
        if (remises != null && remises.length > 0) {
            aRemise = remises[0];

        }

        //Caracteres speciaux sur la ligne CMC7 ==>annulation
        if (aCheque.getNumerocheque().contains("_") || aCheque.getBanque().contains("_") || aCheque.getAgence().contains("_") || aCheque.getNumerocompte().contains("_") || aCheque.getRibcompte().contains("_")) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemise != null) {
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE =" + aCheque.getRemise());
                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aCheque.getRemise());
            }

            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage("La Ligne CMC7 contient des caracteres speciaux, Priere de verifier!");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage(aMessageBean.getMessage());
        }
        //Numero de cheques Vide ou null  ==>annulation
        if (aCheque.getNumerocheque() == null || aCheque.getNumerocheque().trim().equals("")) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemise != null) {
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE =" + aCheque.getRemise());
                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aCheque.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" Le Numero du cheque doit être renseigne");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }
        //Banque du cheque Vide ou null  ==>annulation
        if (aCheque.getBanque() == null || aCheque.getBanque().trim().equals("")) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemise != null) {
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE =" + aCheque.getRemise());
                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aCheque.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" La Banque du cheque doit être renseigne");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }
        //agence du cheque Vide ou null  ==>annulation
        if (aCheque.getAgence() == null || aCheque.getAgence().trim().equals("")) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemise != null) {
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE =" + aCheque.getRemise());
                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aCheque.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage("Agence du cheque doit être renseigne");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }
        //Compte du cheque Vide ou null  ==>annulation
        if (aCheque.getNumerocompte() == null || aCheque.getNumerocompte().trim().equals("")) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemise != null) {
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE =" + aCheque.getRemise());
                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aCheque.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" Le Numero de compte doit être renseigne");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }
        // //Numero du cheque Vide ou null ou non entier  ==>annulation
        if (aCheque.getNumerocheque() == null || aCheque.getNumerocheque().trim().equals("") || !Utility.isInteger(aCheque.getNumerocheque().trim())) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemise != null) {
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE =" + aCheque.getRemise());
                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aCheque.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" Le Numero de cheque doit être renseigne par un entier");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }
        //Rib compte du cheque Vide ou null  ==>annulation
        if (aCheque.getRibcompte() == null || aCheque.getRibcompte().trim().equals("")) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemise != null) {
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE =" + aCheque.getRemise());
                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aCheque.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setTypeMessage("ERROR");
            aMessageBean.setMessage(" La cle RIB du cheque doit être renseigne");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }

        String cleribCal = Utility.computeCleRIBACPACH(aCheque.getBanque(), aCheque.getAgence(), aCheque.getNumerocompte());
        //Clef Rib pas bonne == Annulation
        if (!cleribCal.equals(aCheque.getRibcompte())) {
            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            if (aRemise != null) {
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                db.updateRowByObjectByQuery(aRemise, "REMISES", "IDREMISE =" + aCheque.getRemise());
                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + " WHERE REMISE=" + aCheque.getRemise());
            }
            aMessageBean.setAction("9");
            aMessageBean.setMessage(" Banque " + aCheque.getBanque() + " Agence " + aCheque.getAgence() + " Le Compte " + aCheque.getNumerocompte() + " donne la cle rib " + cleribCal);
            aMessageBean.setTypeMessage("ERROR");
            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE =" + aCheque.getIdcheque());
            db.close();
            return printMessage((aMessageBean.getMessage()));

        }

        db.close();
        return "OK";
    }
//

    String updateDataTableDev(HttpServletRequest request, HttpServletResponse response) {

        MessageBean aMessageBean = new MessageBean();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());

        try {

            String updateQuery = request.getParameter("message");
            String table = request.getParameter("table");
            String colonne = request.getParameter("colonne");
            String oldValue = request.getParameter("oldvalue");
            String newValue = request.getParameter("newvalue");
            String primaryClause = request.getParameter("primaryClause");
            String remarques = request.getParameter("remarques");
            System.out.println("message :" + updateQuery + " table " + table + " colonne : " + colonne + " oldValue: " + oldValue + " newValue :" + newValue + "primaryClause : " + primaryClause + " remarques : " + remarques);

            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            String sql = "";
            Remises aRemise = null;
            Cheques aCheque = null;

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            //Avant MAJ, Certaines Verifications
            if (table.equalsIgnoreCase("REMISES") && ((Utility.getParam("CETAOPEANO") + "-"
                    + Utility.getParam("CETAOPEVAL") + "-"
                    + Utility.getParam("CETAOPEVAL2") + "-"
                    + Utility.getParam("CETAOPEERR") + "-"
                    + Utility.getParam("CETAOPEALLICOM1")).contains(newValue) || colonne.equalsIgnoreCase("COMPTEREMETTANT"))) {
                sql = "SELECT * FROM REMISES WHERE " + primaryClause;
                Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                if (remises != null && remises.length > 0) {
                    aRemise = remises[0];
                    aRemise.setValideur((aRemise.getValideur() != null) ? aRemise.getValideur() : user.getLogin().trim());
                    aRemise.setRemarques((remarques != null) ? remarques : (aRemise.getRemarques() != null) ? aRemise.getRemarques() : "");
                    db.updateRowByObjectByQuery(remises[0], "REMISES", "IDREMISE=" + remises[0].getIdremise());
                    String result = controleCoherenceRemise(aRemise, aMessageBean, colonne, oldValue, newValue);
                    System.out.println("#### result #####:" + result);
                    if (result != null && !result.equals("OK")) {
                        System.out.println("result :" + result);
                        db.close();
                        return result;
                    }

                }

            }

            if (table.contains("CHEQUES")) {
                sql = "SELECT * FROM CHEQUES WHERE " + primaryClause;
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {
                    Cheques_his aChequeHis = new Cheques_his(cheques[0]);
                    aChequeHis.setIdcheque_his(new BigDecimal(Utility.computeCompteur("IDCHEQUEHIS", "CHEQUES")));
                    db.insertObjectAsRowByQuery(aChequeHis, "CHEQUES_HIS");

                }
            }

            if (table.contains("CHEQUES") && "DATEECHEANCE".contains(colonne.toUpperCase())) {
                sql = "SELECT * FROM CHEQUES WHERE " + primaryClause;
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {

                    try {
                        Calendar myCal = Calendar.getInstance();
                        Calendar myCal2 = Calendar.getInstance();
                        myCal.setTime(Utility.convertStringToDate(cheques[0].getDateemission(), "yyyy/MM/dd"));
                        myCal2.setTime(Utility.convertStringToDate(newValue, "yyyy/MM/dd"));

                        long day = Utility.getDaysBetween(myCal, myCal2);
                        if (day < Integer.parseInt(Utility.getParam("GARDE_MIN")) || day > Integer.parseInt(Utility.getParam("GARDE_MAX"))) {
                            aMessageBean.setAction("9");
                            aMessageBean.setTypeMessage("ERROR");
                            aMessageBean.setMessage("La Date d'echeance doit etre superieur de " + Integer.parseInt(Utility.getParam("GARDE_MIN")) + " jours a " + Integer.parseInt(Utility.getParam("GARDE_MAX")) + " jours a la date d'emission.");
                            db.close();
                            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
                        }

                    } catch (Exception ex) {
                        aMessageBean.setAction("9");
                        aMessageBean.setTypeMessage("ERROR");
                        aMessageBean.setMessage("Merci de respecter le format de la date :yyyy/MM/dd");
                        db.close();
                        return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
                    }

                }

            }
            if (table.contains("UTILISATEURS")) {
                if (Utility.getParam("DOUBLE_VALIDATION_UTI") != null && Utility.getParam("DOUBLE_VALIDATION_UTI").equalsIgnoreCase("1")) {
                    Utilisateurs utilisateurs[] = (Utilisateurs[]) db.retrieveRowAsObject("SELECT * FROM UTILISATEURS WHERE " + primaryClause.substring(primaryClause.lastIndexOf(".") + 1), new Utilisateurs());
                    if (utilisateurs != null && utilisateurs.length > 0) {
                        if (user.getLogin().trim().equalsIgnoreCase(utilisateurs[0].getUmodification())) {
                            aMessageBean.setAction("9");
                            aMessageBean.setTypeMessage("ERROR");
                            aMessageBean.setMessage("Vous ne pouvez pas modifier un attribut de l'utilisateur " + utilisateurs[0].getLogin().trim() + "Raison: La double validation est activée(DOUBLE_VALIDATION_UTI=" + Utility.getParam("DOUBLE_VALIDATION_UTI") + ")");
                            db.close();
                            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

                        }
                    }
                }
            }
             if (table.contains("PROFILS")) {
                if (Utility.getParam("DOUBLE_VALIDATION_UTI") != null && Utility.getParam("DOUBLE_VALIDATION_UTI").equalsIgnoreCase("1")) {
                    Profils profils[] = (Profils[]) db.retrieveRowAsObject("SELECT * FROM Profils WHERE " + primaryClause.substring(primaryClause.lastIndexOf(".") + 1), new Profils());
                    if (profils != null && profils.length > 0) {
                        if (user.getLogin().trim().equalsIgnoreCase(profils[0].getUmodification())) {
                            aMessageBean.setAction("9");
                            aMessageBean.setTypeMessage("ERROR");
                            aMessageBean.setMessage("Vous ne pouvez pas modifier un attribut du profil " + profils[0].getLibelleprofil().trim() + "\nRaison: La double validation est activée(DOUBLE_VALIDATION_UTI=" + Utility.getParam("DOUBLE_VALIDATION_UTI") + ")");
                            db.close();
                            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));

                        }
                    }
                }
            }

            //MAJ
            if (db.executeUpdate(updateQuery) <= 0) {
                db.close();
                return printMessage("rien");
            }

            primaryClause = primaryClause.substring(primaryClause.lastIndexOf(".") + 1);
            //Controles apres MAJ
            if (table.equalsIgnoreCase("CHEQUES") && oldValue.contains((Utility.getParam("CETAOPEANO") + "-" + Utility.getParam("CETAREMSAIIRIS")))) {
                sql = "SELECT * FROM CHEQUES WHERE " + primaryClause;
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {
                    aCheque = cheques[0];
                    db.close();
                    return controleCoherenceCheque(aCheque, aMessageBean);

                }
            }
            if (table.equalsIgnoreCase("CHEQUES") && newValue.equals(Utility.getParam("CETAOPEALLICOM1"))) {
                sql = "SELECT * FROM CHEQUES WHERE " + primaryClause;
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {
                    aCheque = cheques[0];
                    sql = "UPDATE REMISES SET ETAT = " + Utility.getParam("CETAOPEALLICOM1") + " WHERE IDREMISE = " + aCheque.getRemise();
                    db.executeUpdate(sql);

                }
            }
            if (table.contains("CHEQUES") && "DATEECHEANCE".contains(colonne.toUpperCase())) {
                sql = "SELECT * FROM CHEQUES WHERE " + primaryClause;
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {
                    aCheque = cheques[0];
                    sql = "UPDATE CHEQUES SET ETAT = " + Utility.getParam("CETAOPEGARCHEMOD") + ",LOTSIB=0 WHERE IDCHEQUE = " + aCheque.getIdcheque();
                    db.executeUpdate(sql);

                }
            }
            if (table.contains("CHEQUES") && colonne.toUpperCase().contains("ETAT")
                    && (newValue.equalsIgnoreCase(Utility.getParam("CETAOPEGARCHEMODCON")))) {
                sql = "SELECT * FROM CHEQUES  WHERE " + primaryClause;
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {
                    aCheque = cheques[0];
                    sql = "UPDATE CHEQUES SET ETAT = " + Utility.getParam("CETAOPEGARCHECONSIB") + ",LOTSIB=0 WHERE IDCHEQUE = " + aCheque.getIdcheque();
                    db.executeUpdate(sql);

                }
            }
            if (table.contains("CHEQUES") && colonne.toUpperCase().contains("ETAT")
                    && (newValue.equalsIgnoreCase(Utility.getParam("CETAOPEGARCHEMODREJ")) || newValue.equalsIgnoreCase(Utility.getParam("CETAOPEGARCHESUPREJ")))) {
                sql = "DELETE FROM CHEQUES WHERE " + primaryClause;
                db.executeUpdate(sql);
                sql = "SELECT * FROM CHEQUES_HIS WHERE IDCHEQUE_HIS=(SELECT MAX(IDCHEQUE_HIS) FROM CHEQUES_HIS WHERE " + primaryClause + ")";
                Cheques_his[] cheques_hises = (Cheques_his[]) db.retrieveRowAsObject(sql, new Cheques_his());
                if (cheques_hises != null && cheques_hises.length > 0) {

                    Cheques anotherCheque = new Cheques(cheques_hises[0]);
                    anotherCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEGARCHECONSIB")));
                    db.insertObjectAsRowByQuery(anotherCheque, "CHEQUES");

                }

            }
            if (table.contains("CHEQUES") && colonne.toUpperCase().contains("ETAT")
                    && (newValue.equalsIgnoreCase(Utility.getParam("CETAOPEGARCHESUPCON")))) {
                sql = "SELECT * FROM CHEQUES  WHERE " + primaryClause;
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {
                    aCheque = cheques[0];
                    sql = "UPDATE CHEQUES SET LOTSIB=0 WHERE IDCHEQUE = " + aCheque.getIdcheque();
                    db.executeUpdate(sql);

                }
            }
            if (table.contains("UTILISATEURS")) {
                db.executeUpdate("UPDATE UTILISATEURS SET UMODIFICATION='" + user.getLogin().trim() + "', DMODIFICATION ='" + Utility.convertDateToString(new java.util.Date(), ResLoader.getMessages("patternDate")) + "' WHERE " + primaryClause);

            }

            if (table.contains("UTILISATEURS") && !colonne.toUpperCase().contains("ETAT")) {
                db.executeUpdate("UPDATE UTILISATEURS SET ETAT='" + Utility.getParam("CETAUTICRE") + "', DMODIFICATION ='" + Utility.convertDateToString(new java.util.Date(), ResLoader.getMessages("patternDate")) + "' WHERE " + primaryClause);

            }
            
            if (table.contains("PROFILS")) {
                db.executeUpdate("UPDATE PROFILS SET UMODIFICATION='" + user.getLogin().trim() + "', DMODIFICATION ='" + Utility.convertDateToString(new java.util.Date(), ResLoader.getMessages("patternDate")) + "' WHERE " + primaryClause);

            }
              if (table.contains("PROFILS") && !colonne.toUpperCase().contains("ETAT")) {
                db.executeUpdate("UPDATE PROFILS SET ETAT='" + Utility.getParam("CETAUTICRE") + "', DMODIFICATION ='" + Utility.convertDateToString(new java.util.Date(), ResLoader.getMessages("patternDate")) + "' WHERE " + primaryClause);

            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("OK");
    }
    //UbaGuinee

    String updateDataTableDevUbaGuinee(HttpServletRequest request, HttpServletResponse response) {

        MessageBean aMessageBean = new MessageBean();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());

        try {

            String updateQuery = request.getParameter("message");
            String table = request.getParameter("table");
            String colonne = request.getParameter("colonne");
            String oldValue = request.getParameter("oldvalue");
            String newValue = request.getParameter("newvalue");
            String primaryClause = request.getParameter("primaryClause");
            String remarques = request.getParameter("remarques");
            System.out.println("updateDataTableDevUbaGuinee message :" + updateQuery + " table " + table + " colonne : " + colonne + " oldValue: " + oldValue + " newValue :" + newValue + "primaryClause : " + primaryClause + " remarques : " + remarques);

            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            String sql = "";
            Remises aRemise = null;
            Cheques aCheque = null;

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            //Avant MAJ, Certaines Verifications
            if (table.equalsIgnoreCase("REMISES") && ((Utility.getParam("CETAOPEANO") + "-"
                    + Utility.getParam("CETAOPEVAL") + "-"
                    + Utility.getParam("CETAOPEVAL2") + "-"
                    + Utility.getParam("CETAOPEERR") + "-"
                    + Utility.getParam("CETAOPEALLICOM1")).contains(newValue) || colonne.equalsIgnoreCase("COMPTEREMETTANT"))) {
                sql = "SELECT * FROM REMISES WHERE " + primaryClause;
                Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                if (remises != null && remises.length > 0) {
                    aRemise = remises[0];
                    aRemise.setValideur((aRemise.getValideur() != null) ? aRemise.getValideur() : user.getLogin().trim());
                    aRemise.setRemarques((remarques != null) ? remarques : (aRemise.getRemarques() != null) ? aRemise.getRemarques() : "");
                    if (newValue.equals(Utility.getParam("CETAOPEANO"))) {
                        aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                        db.updateRowByObjectByQuery(remises[0], "REMISES", "IDREMISE=" + remises[0].getIdremise());
                    } else {
                        db.updateRowByObjectByQuery(remises[0], "REMISES", "IDREMISE=" + remises[0].getIdremise());
                        String result = controleCoherenceRemiseUbaGuinee(aRemise, aMessageBean, colonne, oldValue, newValue);
                        System.out.println("#### result #####:" + result);
                        if (result != null && !result.equals("OK")) {
                            System.out.println("result :" + result);
                            db.close();
                            return result;
                        }

                    }

                }

            }

            if (table.contains("CHEQUES")) {
                sql = "SELECT * FROM CHEQUES WHERE " + primaryClause;
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {
                    Cheques_his aChequeHis = new Cheques_his(cheques[0]);
                    aChequeHis.setIdcheque_his(new BigDecimal(Utility.computeCompteur("IDCHEQUEHIS", "CHEQUES")));
                    db.insertObjectAsRowByQuery(aChequeHis, "CHEQUES_HIS");

                }
            }

            if (table.contains("CHEQUES") && "DATEECHEANCE".contains(colonne.toUpperCase())) {
                sql = "SELECT * FROM CHEQUES WHERE " + primaryClause;
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {

                    try {
                        Calendar myCal = Calendar.getInstance();
                        Calendar myCal2 = Calendar.getInstance();
                        myCal.setTime(Utility.convertStringToDate(cheques[0].getDateemission(), "yyyy/MM/dd"));
                        myCal2.setTime(Utility.convertStringToDate(newValue, "yyyy/MM/dd"));

                        long day = Utility.getDaysBetween(myCal, myCal2);
                        if (day < Integer.parseInt(Utility.getParam("GARDE_MIN")) || day > Integer.parseInt(Utility.getParam("GARDE_MAX"))) {
                            aMessageBean.setAction("9");
                            aMessageBean.setTypeMessage("ERROR");
                            aMessageBean.setMessage("La Date d'echeance doit etre superieure de " + Integer.parseInt(Utility.getParam("GARDE_MIN")) + " jours a " + Integer.parseInt(Utility.getParam("GARDE_MAX")) + " jours a la date d'emission.");
                            db.close();
                            return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
                        }

                    } catch (Exception ex) {
                        aMessageBean.setAction("9");
                        aMessageBean.setTypeMessage("ERROR");
                        aMessageBean.setMessage("Merci de respecter le format de la date :yyyy/MM/dd");
                        db.close();
                        return printMessage(jsonConverter.objectToJSONStringArray(aMessageBean));
                    }

                }

            }

            //MAJ
            if (db.executeUpdate(updateQuery) <= 0) {
                db.close();
                return printMessage("rien");
            }

            primaryClause = primaryClause.substring(primaryClause.lastIndexOf(".") + 1);
            //Controles apres MAJ
            if (table.equalsIgnoreCase("CHEQUES") && oldValue.contains((Utility.getParam("CETAOPEANO") + "-" + Utility.getParam("CETAREMSAIIRIS")))) {
                sql = "SELECT * FROM CHEQUES WHERE " + primaryClause;
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {
                    aCheque = cheques[0];
                    db.close();
                    return controleCoherenceCheque(aCheque, aMessageBean);

                }
            }
            if (table.equalsIgnoreCase("CHEQUES") && newValue.equals(Utility.getParam("CETAOPEALLICOM1"))) {
                sql = "SELECT * FROM CHEQUES WHERE " + primaryClause;
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {
                    aCheque = cheques[0];
                    sql = "UPDATE REMISES SET ETAT = " + Utility.getParam("CETAOPEALLICOM1") + " WHERE IDREMISE = " + aCheque.getRemise();
                    db.executeUpdate(sql);

                }
            }
            if (table.contains("CHEQUES") && "DATEECHEANCE".contains(colonne.toUpperCase())) {
                sql = "SELECT * FROM CHEQUES WHERE " + primaryClause;
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {
                    aCheque = cheques[0];
                    sql = "UPDATE CHEQUES SET ETAT = " + Utility.getParam("CETAOPEGARCHEMOD") + ",LOTSIB=0 WHERE IDCHEQUE = " + aCheque.getIdcheque();
                    db.executeUpdate(sql);

                }
            }
            if (table.contains("CHEQUES") && colonne.toUpperCase().contains("ETAT")
                    && (newValue.equalsIgnoreCase(Utility.getParam("CETAOPEGARCHEMODCON")))) {
                sql = "SELECT * FROM CHEQUES  WHERE " + primaryClause;
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {
                    aCheque = cheques[0];
                    sql = "UPDATE CHEQUES SET ETAT = " + Utility.getParam("CETAOPEGARCHECONSIB") + ",LOTSIB=0 WHERE IDCHEQUE = " + aCheque.getIdcheque();
                    db.executeUpdate(sql);

                }
            }
            if (table.contains("CHEQUES") && colonne.toUpperCase().contains("ETAT")
                    && (newValue.equalsIgnoreCase(Utility.getParam("CETAOPEGARCHEMODREJ")) || newValue.equalsIgnoreCase(Utility.getParam("CETAOPEGARCHESUPREJ")))) {
                sql = "DELETE FROM CHEQUES WHERE " + primaryClause;
                db.executeUpdate(sql);
                sql = "SELECT * FROM CHEQUES_HIS WHERE IDCHEQUE_HIS=(SELECT MAX(IDCHEQUE_HIS) FROM CHEQUES_HIS WHERE " + primaryClause + ")";
                Cheques_his[] cheques_hises = (Cheques_his[]) db.retrieveRowAsObject(sql, new Cheques_his());
                if (cheques_hises != null && cheques_hises.length > 0) {

                    Cheques anotherCheque = new Cheques(cheques_hises[0]);
                    anotherCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEGARCHECONSIB")));
                    db.insertObjectAsRowByQuery(anotherCheque, "CHEQUES");

                }

            }
            if (table.contains("CHEQUES") && colonne.toUpperCase().contains("ETAT")
                    && (newValue.equalsIgnoreCase(Utility.getParam("CETAOPEGARCHESUPCON")))) {
                sql = "SELECT * FROM CHEQUES  WHERE " + primaryClause;
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {
                    aCheque = cheques[0];
                    sql = "UPDATE CHEQUES SET LOTSIB=0 WHERE IDCHEQUE = " + aCheque.getIdcheque();
                    db.executeUpdate(sql);

                }
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("OK");
    }

    String updateDataTableDev(HttpServletRequest request, String updateQuery, String table, String colonne, String oldValue, String newValue, String primaryClause, String remarques) {
        MessageBean aMessageBean = new MessageBean();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            String sql = "";
            Remises aRemise = null;
            Cheques aCheque = null;

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            //Avant MAJ, Certaines Verifications
            if (table.equalsIgnoreCase("REMISES") && ((Utility.getParam("CETAOPEANO") + "-"
                    + Utility.getParam("CETAOPEVAL") + "-"
                    + Utility.getParam("CETAOPEVAL2") + "-"
                    + Utility.getParam("CETAOPEERR") + "-"
                    + Utility.getParam("CETAOPEALLICOM1")).contains(newValue) || colonne.equalsIgnoreCase("COMPTEREMETTANT"))) {
                sql = "SELECT * FROM REMISES WHERE " + primaryClause;
                Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                if (remises != null && remises.length > 0) {
                    aRemise = remises[0];
                    aRemise.setValideur((aRemise.getValideur() != null) ? aRemise.getValideur() : user.getLogin().trim());
                    aRemise.setRemarques((remarques != null) ? remarques : (aRemise.getRemarques() != null) ? aRemise.getRemarques() : "");
                    db.updateRowByObjectByQuery(remises[0], "REMISES", "IDREMISE=" + remises[0].getIdremise());

                    String result = controleCoherenceRemise(aRemise, aMessageBean, colonne, oldValue, newValue);
                    if (result != null && !result.equals("OK")) {
                        db.close();
                        return result;
                    }

                }

            }

            //MAJ
            if (db.executeUpdate(updateQuery) <= 0) {
                db.close();
                return printMessage("rien");
            }

            //Controles apres MAJ
            if (table.equalsIgnoreCase("CHEQUES") && oldValue.contains((Utility.getParam("CETAOPEANO") + "-" + Utility.getParam("CETAREMSAIIRIS")))) {
                sql = "SELECT * FROM CHEQUES WHERE " + primaryClause;
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {
                    aCheque = cheques[0];
                    db.close();
                    return controleCoherenceCheque(aCheque, aMessageBean);

                }
            }
            if (table.equalsIgnoreCase("CHEQUES") && newValue.equals(Utility.getParam("CETAOPEALLICOM1"))) {
                sql = "SELECT * FROM CHEQUES WHERE " + primaryClause;
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {
                    aCheque = cheques[0];
                    sql = "UPDATE REMISES SET ETAT = " + Utility.getParam("CETAREMVAL") + " WHERE IDREMISE = " + aCheque.getRemise();
                    db.executeUpdate(sql);

                }
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("OK");
    }

    
    String updateDataTableDevEffet(HttpServletRequest request, String updateQuery, String table, String colonne, String oldValue, String newValue, String primaryClause, String remarques) {
        MessageBean aMessageBean = new MessageBean();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            String sql = "";
            RemisesEffets aRemiseeffet = null;
            Effets aEffet = null;

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            //Avant MAJ, Certaines Verifications
            if (table.equalsIgnoreCase("REMISESEFFETS") && ((Utility.getParam("CETAOPEANO") + "-"
                    + Utility.getParam("CETAOPEVAL") + "-"
                    + Utility.getParam("CETAOPEVAL2") + "-"
                    + Utility.getParam("CETAOPEERR") + "-" 
                    
                    //same et eugene 
                    + Utility.getParam("CETAOPEVALDELTA") + "-"  
                    //
                    + Utility.getParam("CETAOPEALLICOM1")).contains(newValue) || colonne.equalsIgnoreCase("COMPTEREMETTANT"))) {
                sql = "SELECT * FROM REMISESEFFETS WHERE " + primaryClause;
                RemisesEffets[] remiseseffets = (RemisesEffets[]) db.retrieveRowAsObject(sql, new RemisesEffets());
                if (remiseseffets != null && remiseseffets.length > 0) {
                    aRemiseeffet = remiseseffets[0];
                    aRemiseeffet.setValideur((aRemiseeffet.getValideur() != null) ? aRemiseeffet.getValideur() : user.getLogin().trim());
                    aRemiseeffet.setRemarques((remarques != null) ? remarques : (aRemiseeffet.getRemarques() != null) ? aRemiseeffet.getRemarques() : "");
                    db.updateRowByObjectByQuery(remiseseffets[0], "REMISESEFFETS", "IDREMISE=" + remiseseffets[0].getIdremise());

                   // String result = controleCoherenceRemise(aRemiseeffet, aMessageBean, colonne, oldValue, newValue);
                   String result = controleCoherenceRemiseEffet(aRemiseeffet, aMessageBean, colonne, oldValue, newValue);
                                   
                    if (result != null && !result.equals("OK")) {
                        db.close();
                        return result;
                    }

                }

            }

            //MAJ
            if (db.executeUpdate(updateQuery) <= 0) {
                db.close();
                return printMessage("rien");
            }

            //Controles apres MAJ
            if (table.equalsIgnoreCase("EFFETS") && oldValue.contains((Utility.getParam("CETAOPEANO") + "-" + Utility.getParam("CETAREMSAIIRIS")))) {
                sql = "SELECT * FROM EFFETS WHERE " + primaryClause;
                Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                if (effets != null && effets.length > 0) {
                    aEffet = effets[0];
                    db.close();
                   // return controleCoherenceCheque(aEffet, aMessageBean);
                   return controleCoherenceEffet(aEffet, aMessageBean);
                }
            }
            if (table.equalsIgnoreCase("EFFETS") && newValue.equals(Utility.getParam("CETAOPEALLICOM1"))) {
                sql = "SELECT * FROM EFFETS WHERE " + primaryClause;
                Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                if (effets != null && effets.length > 0) {
                    aEffet = effets[0];
                    sql = "UPDATE REMISESEFFETS SET ETAT = " + Utility.getParam("CETAREMVAL") + " WHERE IDREMISE = " + aEffet.getRemise();
                    db.executeUpdate(sql);

                }
            }

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("OK");
    }

    
    String updateDataTableDevEffet(HttpServletRequest request, HttpServletResponse response) {

        MessageBean aMessageBean = new MessageBean();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());

        try {

            String updateQuery = request.getParameter("message");
            String table = request.getParameter("table");
            String colonne = request.getParameter("colonne");
            String oldValue = request.getParameter("oldvalue");
            String newValue = request.getParameter("newvalue");
            String primaryClause = request.getParameter("primaryClause");
            String remarques = request.getParameter("remarques");
            System.out.println("message :" + updateQuery + " table " + table + " colonne : " + colonne + " oldValue: " + oldValue + " newValue :" + newValue + "primaryClause : " + primaryClause + " remarques : " + remarques);

            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
            String sql = "";
            RemisesEffets aRemiseeffet = null;
            Effets aEffet = null;

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            //Avant MAJ, Certaines Verifications
            if (table.equalsIgnoreCase("REMISESEFFETS") && ((Utility.getParam("CETAOPEANO") + "-"
                    + Utility.getParam("CETAOPEVAL") + "-"
                    + Utility.getParam("CETAOPEVAL2") + "-"
                    + Utility.getParam("CETAOPEERR") + "-"
                    //SAME ET EUGENE
                     //same et eugene 
                    + Utility.getParam("CETAOPEVALDELTA") + "-"  
                    //
                    + Utility.getParam("CETAOPEALLICOM1")).contains(newValue) || colonne.equalsIgnoreCase("COMPTEREMETTANT"))) {
                sql = "SELECT * FROM REMISESEFFETS  WHERE " + primaryClause;
                RemisesEffets[] remiseseffets = (RemisesEffets[]) db.retrieveRowAsObject(sql, new RemisesEffets());
                if (remiseseffets != null && remiseseffets.length > 0) {
                    aRemiseeffet = remiseseffets[0];
                    aRemiseeffet.setValideur((aRemiseeffet.getValideur() != null) ? aRemiseeffet.getValideur() : user.getLogin().trim());
                    aRemiseeffet.setRemarques((remarques != null) ? remarques : (aRemiseeffet.getRemarques() != null) ? aRemiseeffet.getRemarques() : "");
                    db.updateRowByObjectByQuery(remiseseffets[0], "REMISESEFFETS", "IDREMISE=" + remiseseffets[0].getIdremise());
                    String result = controleCoherenceRemiseEffet(aRemiseeffet, aMessageBean, colonne, oldValue, newValue);
                    System.out.println("#### result #####:" + result);
                    if (result != null && !result.equals("OK")) {
                        System.out.println("result :" + result);
                        db.close();
                        return result;
                    }

                }

            }


            //MAJ
            if (db.executeUpdate(updateQuery) <= 0) {
                db.close();
                return printMessage("rien");
            }

            primaryClause = primaryClause.substring(primaryClause.lastIndexOf(".") + 1);
           
        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.close();
        return printMessage("OK");
    }
    
    
    String updateProfile(String code, String libelle, String id, String options, HttpServletRequest request) {
        System.out.println("Options =" + options);
        String[] taches = options.split(";");
        String[] tabTaches;
        String regTaches = ";";
        Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
         
        for (int i = 0; i < taches.length; i++) {
            if (!taches[i].isEmpty()) {

                tabTaches = taches[i].split("-");
                regTaches += tabTaches[0] + ";";

            }

        }

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        int i = 0;
        try {
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            i = db.executeUpdate("UPDATE PROFILS SET ETAT="+ Utility.getParam("CETAUTICRE") +",REGTACHE='" + regTaches + "', NOMPROFIL='" + code + "',LIBELLEPROFIL='" + libelle + "', UMODIFICATION='"+user.getLogin().trim()+"',DMODIFICATION='"+Utility.convertDateToString(new java.util.Date(), ResLoader.getMessages("patternDate"))+"' WHERE IDPROFIL=" + id);
        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }

        String jsonresponse = "{success: false, msg: 'Erreur de traitement. Merci de reessayer.'}";
        if (i > 0) {
            jsonresponse = "{success: true, msg: 'Mise a jour effectue avec succes'}";
        }
        return jsonresponse;

    }

    String valideRemise(HttpServletRequest request, HttpServletResponse response) {
        String updateQuery = "UPDATE REMISES SET ETAT =" + Utility.getParam("CETAOPEALLICOM1") + " WHERE IDREMISE=" + request.getParameter("primaryValue");
        String table = "REMISES";
        String colonne = "ETAT";
        String oldValue = Utility.getParam("CETAREMVAL");
        String newValue = Utility.getParam("CETAOPEALLICOM1");
        String primaryClause = request.getParameter("primaryKey") + "=" + request.getParameter("primaryValue");
        return updateDataTableDev(request, updateQuery, table, colonne, oldValue, newValue, primaryClause, "");
    }
    
    public Object createObject(String objectName) {
        Class aClass;
        Object instance = null;
        try {
            aClass = Class.forName(objectName);
            instance = aClass.newInstance();

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return instance;
    }

    public String getTableAsExtJSONString(String table) {

        JSONObject extDataTable = new JSONObject();
        JSONObject resp = new JSONObject();
        JSONArray data = new JSONArray();
        String sql;
        String total;
        String jsonresult = "{success: false, msg: 'Aucune information trouvee!' }";

        try {

            // Request the data from mysql
            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            try {
                db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            } catch (Exception e) {
                System.out.println("Erreur Acces a la base de donnee :" + e.getMessage());
                jsonresult = "{success: false, msg: 'Erreur Acces a la base de donnee' }";
                return jsonresult;
            }

            sql = (new StringBuilder()).append("SELECT * FROM " + table).toString();
            String objectName = "clearing.table." + table.replaceFirst("" + table.charAt(0), ("" + table.charAt(0)).toUpperCase());
            Object anObject = createObject(objectName);
            Object[] tabObjects = db.retrieveRowAsObject(sql, anObject);
            db.close();

            resp = resp.put("success", true);

            if (tabObjects != null && tabObjects.length > 0) {
                for (Object anInstance : tabObjects) {
                    Property[] properties = BeanInfoFactory.getProperties(anInstance);
                    JSONObject row = new JSONObject();
                    for (Property propertie : properties) {
                        if (!propertie.getPropertyName().equalsIgnoreCase("class")) {
                            row = row.put(propertie.getPropertyName(), propertie.getPropertyValue());
                        }

                    }
                    data.put(row);

                }

                total = String.valueOf(tabObjects.length);

                data.put(resp);
                extDataTable.put("total", total);
                extDataTable.put("data", data);
            } else {
                System.out.println("No data found !!!");
                jsonresult = "{success: false, msg: 'Aucune information trouvee!' }";
                return jsonresult;
            }

            db.close();
            System.out.println("[" + table + "] JSON stream = [ " + extDataTable.toString() + " ]");
            // Return to Server

            //  jsonresult = new JSONConverter().jsonToObjectLibertal(extDataTable, new StringBuffer());
        } catch (JSONException ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return extDataTable.toString();
    }

    public String checkICOM(String message) {

        JSONObject extDataTable = new JSONObject();
        JSONObject resp = new JSONObject();
        JSONArray data = new JSONArray();
        JSONObject row = new JSONObject();
        try {
            String paramWithoutCache = "";
            resp = resp.put("success", true);
            if (message.toUpperCase().contains("SRGWRITER")) {
                paramWithoutCache = Utility.getParam("ENVOI_ICOM_SRG");

                if (paramWithoutCache == null || paramWithoutCache.isEmpty()) {
                    row.put("ENVOI_ICOM_SRG", 0);
                } else {
                    row.put("ENVOI_ICOM_SRG", paramWithoutCache);
                }
                data.put(row);

            } else if (message.toUpperCase().contains("NATWRITER")) {
                paramWithoutCache = Utility.getParam("ENVOI_ICOM_NAT");

                if (paramWithoutCache == null || paramWithoutCache.isEmpty()) {
                    row.put("ENVOI_ICOM_NAT", 0);
                } else {
                    row.put("ENVOI_ICOM_NAT", paramWithoutCache);
                }
                data.put(row);

            }

            try {
                extDataTable.put("success", true);
                extDataTable.put("data", row);
            } catch (JSONException ex) {
                Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println(" JSON stream = [ " + extDataTable.toString() + " ]");

        } catch (JSONException ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return extDataTable.toString();
    }

    public String byPasserRemise(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            String idObjet = request.getParameter("idObjet");
            String typeObjet = "REMISE";
            String sequenceObjet = request.getParameter("sequenceObjet");
            Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            if (sequenceObjet != null && !sequenceObjet.trim().equals("")) {
                db.executeUpdate("DELETE FROM SEQUENCES WHERE IDSEQUENCE=" + sequenceObjet);

                if (typeObjet != null && typeObjet.trim().equals("REMISE")) {
                    Remises remise[] = (Remises[]) db.retrieveRowAsObject("SELECT * FROM REMISES WHERE IDREMISE = " + idObjet, new Remises());

                    if (remise[0].getEtat().intValue() < new Integer(Utility.getParam("CETAREMVAL")).intValue()) {
                        db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEANO") + "," + "NOMUTILISATEUR='" + user.getLogin().trim() + "' WHERE IDREMISE=" + idObjet);
                        //BYPASS des cheques de la remise
                        Cheques cheques[] = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE REMISE = " + idObjet, new Cheques());
                        if (cheques != null) {
                            for (int i = 0; i < cheques.length; i++) {
                                Cheques cheques1 = cheques[i];
                                if (cheques1.getEtat().intValue() < new Integer(Utility.getParam("CETAOPEVAL")).intValue()) {
                                    db.executeUpdate("DELETE FROM SEQUENCES WHERE IDSEQUENCE=" + cheques1.getSequence());

                                    db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + "," + "CODEUTILISATEUR='" + user.getLogin().trim() + "' WHERE IDCHEQUE=" + cheques1.getIdcheque());
                                } else {
                                    System.out.println("Document cheque non BYPASSE");
                                }

                            }

                            // db.executeUpdate("DELETE FROM CHEQUES WHERE REMISE=" + idObjet);
                        }
                    } else {
                        System.out.println("Document remise BYPASSE " + idObjet);
                    }

                }
                db.close();
                return printMessage("Document(s) bypasse(s) avec succes");
            }
            db.close();
        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            db.close();
        }
        return printMessage("Erreur de byPass -" + db.getMessage());

    }

    String refreshSignature(HttpServletRequest request, HttpServletResponse response) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Cheques currentCheque = (Cheques) request.getSession().getAttribute("currentCheque");
            currentCheque.setAgence(Utility.bourrageGZero(request.getParameter("codeAgence"), 5));
            currentCheque.setNumerocompte(Utility.bourrageGZero(request.getParameter("numeroCompte"), 12));
            db.updateRowByObjectByQuery(currentCheque, "CHEQUES", "IDCHEQUE =" + currentCheque.getIdcheque());
            db.close();

        } catch (Exception ex) {
            Logger.getLogger(SiteController.class.getName()).log(Level.SEVERE, null, ex);
            db.close();
        }
        return "OK";
    }
}
