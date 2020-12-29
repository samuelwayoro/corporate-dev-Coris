/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.model;

import clearing.table.Agences;
import clearing.table.Banques;
import clearing.table.Comptes;
import clearing.table.Lotcom;
import clearing.table.Remcom;
import clearing.table.Synthese;
import clearing.table.delta.BksigImageValideCompte;
import clearing.table.flexcube.GLTM_MIS_CODE;
import clearing.table.flexcube.STTM_ACCOUNT_MAINT_INSTR;
import clearing.table.flexcube.STTM_AEOD_DATES;
import clearing.table.flexcube.STTM_CUST_ACCOUNT;
import clearing.table.flexcube.STTM_CUST_ACCOUNT7;
import clearing.table.flexcube.SVTM_CIF_SIG_DET;
import clearing.table.flexcube.VW_CMPTBCEAO;
import clearing.table.orion.Sibicsig;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.patware.bean.table.Params;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.ExtJDBCXmlReader;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class CMPUtility {

    private static Hashtable<String, String> codeSica2Cache = new Hashtable<String, String>();
    private static Hashtable<String, String> codeSica3Cache = new Hashtable<String, String>();

    public static Remcom insertRemcom(EnteteRemise enteteRemise, int etat) throws Exception {
        Remcom remcom = new Remcom();
        remcom.setEtat(new BigDecimal(etat));
        remcom.setCoderejet(enteteRemise.getCodeRejet());
        remcom.setDatepresentation(new Timestamp(enteteRemise.getDatePresentation().getTime()));
        remcom.setDevise(enteteRemise.getDevise());
        remcom.setFlaginversion(enteteRemise.getFlagInversion());
        remcom.setIdemetteur(enteteRemise.getIdEmetteur());
        remcom.setIdrecepteur(enteteRemise.getIdRecepeteur());
        remcom.setNblots(enteteRemise.getNbLots());
        remcom.setRefremise(enteteRemise.getRefRemise());
        remcom.setRefremrelatif(enteteRemise.getRefRemRelatif());
        remcom.setSeance(enteteRemise.getSeance());
        remcom.setTyperemise(enteteRemise.getTypeRemise());
        remcom.setIdDestinataire(enteteRemise.getIdDestinataire());
        if (enteteRemise.getTypeRemise().equalsIgnoreCase("ESYI")) {
            remcom.setNbtotremallenv(new BigDecimal(Integer.parseInt(enteteRemise.getNbTotRemAllEnv())));
            remcom.setNbtotremallacctot(new BigDecimal(Integer.parseInt(enteteRemise.getNbTotRemAllAccTot())));
            remcom.setNbtotremallaccpar(new BigDecimal(Integer.parseInt(enteteRemise.getNbTotRemAllAccPar())));
            remcom.setNbtotremallrejtot(new BigDecimal(Integer.parseInt(enteteRemise.getNbTotRemAllRejTot())));
            remcom.setNbtotremallann(new BigDecimal(Integer.parseInt(enteteRemise.getNbTotRemAllAnn())));
            remcom.setNbtotoperval(new BigDecimal(Integer.parseInt(enteteRemise.getNbTotOperVal())));

        }
        if (enteteRemise.getTypeRemise().equalsIgnoreCase("ESMI")) {
            remcom.setNbtotremallenv(new BigDecimal(Integer.parseInt(enteteRemise.getNbTotRemAllEnv())));
            remcom.setNbtotremallacctot(new BigDecimal(Integer.parseInt(enteteRemise.getNbTotRemAllAccTot())));
            remcom.setNbtotremallrejtot(new BigDecimal(Integer.parseInt(enteteRemise.getNbTotRemAllRejTot())));

        }

        remcom.setIdremcom(new BigDecimal(Integer.parseInt(Utility.computeCompteur("IDREMCOM", "REMCOM"))));

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        db.insertObjectAsRowByQuery(remcom, "REMCOM");
        db.close();
        return remcom;
    }

    public static Lotcom insertLotcom(EnteteLot enteteLot1, Remcom remcom) throws Exception {
        Lotcom lotcom = new Lotcom();
        lotcom.setCoderejet(enteteLot1.getCodeRejet());
        lotcom.setIdbanrem(enteteLot1.getIdBanRem());
        lotcom.setMnttotoperacc(enteteLot1.getMntTotOperAcc());
        lotcom.setMontanttotal(enteteLot1.getMontantTotal());
        lotcom.setNboperations(enteteLot1.getNbOperations());
        lotcom.setNbtotoperacc(enteteLot1.getNbTotOperAcc());
        lotcom.setRefbancaire(enteteLot1.getRefBancaire());
        lotcom.setReflot(enteteLot1.getRefLot());
        lotcom.setTypeoperation(enteteLot1.getTypeOperation());
        lotcom.setEtat(remcom.getEtat());
        lotcom.setIdremcom(remcom.getIdremcom());
        lotcom.setIdlotcom(new BigDecimal(Integer.parseInt(Utility.computeCompteur("IDLOTCOM", "LOTCOM"))));
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        db.insertObjectAsRowByQuery(lotcom, "LOTCOM");
        db.close();
        return lotcom;
    }

    public static Synthese insertSynthese(EnteteRemise enteteRemise) throws Exception {
        System.out.println(enteteRemise.toString());
        Synthese synthese = new Synthese();
        Remcom remcom = insertRemcom(enteteRemise, 0);
        for (int i = 0; i < enteteRemise.enregs.length; i++) {
            Enreg enteteLot1 = enteteRemise.enregs[i];
            System.out.println(enteteLot1.toString());

            synthese.setType(enteteRemise.getIdEntete());
            synthese.setDatepresentation(new java.sql.Timestamp(enteteRemise.getDatePresentation().getTime()));
            synthese.setTypeoperation(enteteLot1.getTypeOperation());
            synthese.setIdbancon(enteteLot1.getIdBanCon());
            synthese.setIdremcom(remcom.getIdremcom());
            if (synthese.getType().equalsIgnoreCase("ESYR")) {
                synthese.setSigne(enteteLot1.getSigne());
                synthese.setSolde(enteteLot1.getSolde());
            }

            if (synthese.getType().equalsIgnoreCase("ESYO")) {
                synthese.setMnttotoperrecus(enteteLot1.getMntTotOperRecus());
                synthese.setNbtotoperrecus(new BigDecimal(Integer.parseInt(enteteLot1.getNbTotOperRecus())));
            }

            if (synthese.getType().equalsIgnoreCase("ESYI")) {
                synthese.setMnttotoperemis(enteteLot1.getMntTotOperEmis());
                synthese.setNbtotoperemis(new BigDecimal(Integer.parseInt(enteteLot1.getNbTotOperEmis())));
            }
            if (synthese.getType().equalsIgnoreCase("ESMI")) {
                synthese.setNbtotoperemis(new BigDecimal(Integer.parseInt(enteteLot1.getNbTotOperEmis())));
            }
            if (synthese.getType().equalsIgnoreCase("ESMO")) {
                synthese.setNbtotoperrecus(new BigDecimal(Integer.parseInt(enteteLot1.getNbTotOperRecus())));
            }

            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            db.insertObjectAsRowByQuery(synthese, "SYNTHESE");
            db.close();

        }
        return synthese;
    }

    public static Banques[] getBanquesRemettantesNationales(DataBase db, String etat) {
        //Recuperation des Banques NATIONALES SELON LA VERSION SICA EN COURS
        String sql;
        if (Utility.getParam("VERSION_SICA").equals("2")) {
            sql = "SELECT * FROM BANQUES WHERE ( CODEBANQUE IN (SELECT DISTINCT BANQUEREMETTANT FROM CHEQUES WHERE " + etat + ") OR "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUEREMETTANT FROM EFFETS WHERE " + etat + ") OR "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUEREMETTANT FROM PRELEVEMENTS WHERE " + etat + ") OR "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUEREMETTANT FROM VIREMENTS WHERE " + etat + ") ) AND CODEBANQUE LIKE '" + Utility.getParam("CODE_BANQUE").charAt(0) + "%' AND CODEBANQUE <>'" + Utility.getParam("CODE_BANQUE") + "'";
        } else {
            sql = "SELECT * FROM BANQUES WHERE ( CODEBANQUE IN (SELECT DISTINCT BANQUEREMETTANT FROM CHEQUES WHERE " + etat + ") OR "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUEREMETTANT FROM EFFETS WHERE " + etat + ") OR "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUEREMETTANT FROM PRELEVEMENTS WHERE " + etat + ") OR "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUEREMETTANT FROM VIREMENTS WHERE " + etat + ") ) AND (CODEBANQUE LIKE '" + Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2) + "%' OR CODEBANQUE LIKE '" + Utility.getParam("CODE_BANQUE").charAt(0) + "%') AND (CODEBANQUE NOT IN ('" + Utility.getParam("CODE_BANQUE_SICA3") + "','" + Utility.getParam("CODE_BANQUE") + "'))";
        }
        Banques[] banques = (Banques[]) db.retrieveRowAsObject(sql, new Banques());
        return banques;
    }

    public static Banques[] getBanques(DataBase db, String etat) {
        //Recuperation des Banques NATIONALES SELON LA VERSION SICA EN COURS
        String sql;
        {
            sql = "SELECT * FROM BANQUES WHERE ( "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUE FROM CHEQUES WHERE " + etat + ") OR "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUE FROM PRELEVEMENTS WHERE " + etat + ") OR "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUE FROM EFFETS WHERE " + etat + ") OR "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUE FROM VIREMENTS WHERE " + etat + ") ) ";
        }
        Banques[] banques = (Banques[]) db.retrieveRowAsObject(sql, new Banques());
        return banques;
    }

    public static Banques[] getBanquesNationales(DataBase db, String etat) {
        //Recuperation des Banques NATIONALES SELON LA VERSION SICA EN COURS
        String sql;
        if (Utility.getParam("VERSION_SICA").equals("2")) {
            sql = "SELECT * FROM BANQUES WHERE ( CODEBANQUE IN (SELECT DISTINCT BANQUE FROM CHEQUES WHERE " + etat + ") OR "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUE FROM EFFETS WHERE " + etat + ") OR "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUE FROM VIREMENTS WHERE " + etat + ") ) AND CODEBANQUE LIKE '" + Utility.getParam("CODE_BANQUE").charAt(0) + "%' AND CODEBANQUE <>'" + Utility.getParam("CODE_BANQUE") + "'";
        } else {
            sql = "SELECT * FROM BANQUES WHERE ( "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUE FROM CHEQUES WHERE " + etat + ") OR "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUE FROM PRELEVEMENTS WHERE " + etat + ") OR "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUE FROM EFFETS WHERE " + etat + ") OR "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUE FROM VIREMENTS WHERE " + etat + ") ) "
                    + "AND (CODEBANQUE LIKE '" + Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2) + "%' "
                    + " OR CODEBANQUE LIKE '" + Utility.getParam("CODE_BANQUE").charAt(0) + "%') AND (CODEBANQUE NOT IN ('" + Utility.getParam("CODE_BANQUE_SICA3") + "','" + Utility.getParam("CODE_BANQUE") + "'))";

//            sql = "SELECT * FROM BANQUES WHERE ( "
//                    + " CODEBANQUE IN (SELECT DISTINCT BANQUE FROM CHEQUES WHERE " + etat + ") OR "
//                    + " CODEBANQUE IN (SELECT DISTINCT BANQUE FROM PRELEVEMENTS WHERE " + etat + ") OR "
//                    + " CODEBANQUE IN (SELECT DISTINCT BANQUE FROM EFFETS WHERE " + etat + ") OR "
//                    + " CODEBANQUE IN (SELECT DISTINCT BANQUE FROM VIREMENTS WHERE " + etat + ") ) "
//                    + " AND (CODEBANQUE LIKE '" + Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2) + "%' OR CODEBANQUE LIKE '" + Utility.getParam("CODE_BANQUE").charAt(0) + "%') AND (CODEBANQUE NOT IN ('" + Utility.getParam("CODE_BANQUE_SICA3") + "','" + Utility.getParam("CODE_BANQUE") + "'))";
        }
        Banques[] banques = (Banques[]) db.retrieveRowAsObject(sql, new Banques());
        return banques;
    }

    public static Banques[] getBanquesRemettantesSousRegionales(DataBase db, String etat) {
        //Recuperation des Banques SOUS-REGIONALES SELON LA VERSION SICA EN COURS
        String sql;
        if (Utility.getParam("VERSION_SICA").equals("2")) {
            sql = "SELECT * FROM BANQUES WHERE ( CODEBANQUE IN (SELECT DISTINCT BANQUEREMETTANT FROM CHEQUES WHERE " + etat + ") OR "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUEREMETTANT FROM EFFETS WHERE " + etat + ") OR "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUEREMETTANT FROM PRELEVEMENTS WHERE " + etat + ") OR "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUEREMETTANT FROM VIREMENTS WHERE " + etat + ") ) AND CODEBANQUE NOT LIKE '" + Utility.getParam("CODE_BANQUE").charAt(0) + "%' AND CODEBANQUE <>'" + Utility.getParam("CODE_BANQUE") + "'";
        } else {
            sql = "SELECT * FROM BANQUES WHERE  CODEBANQUE IN (SELECT DISTINCT BANQUEREMETTANT FROM CHEQUES WHERE   " + etat + " AND BANQUEREMETTANT NOT LIKE '" + Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2) + "%' AND BANQUEREMETTANT NOT LIKE '" + Utility.getParam("CODE_BANQUE").charAt(0) + "%' AND BANQUEREMETTANT NOT IN ('" + Utility.getParam("CODE_BANQUE_SICA3") + "','" + Utility.getParam("CODE_BANQUE") + "'))"
                    + " UNION SELECT * FROM BANQUES WHERE  CODEBANQUE IN (SELECT DISTINCT BANQUEREMETTANT FROM EFFETS WHERE  " + etat + " AND BANQUEREMETTANT NOT LIKE '" + Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2) + "%' AND BANQUEREMETTANT NOT LIKE '" + Utility.getParam("CODE_BANQUE").charAt(0) + "%' AND BANQUEREMETTANT NOT IN ('" + Utility.getParam("CODE_BANQUE_SICA3") + "','" + Utility.getParam("CODE_BANQUE") + "'))"
                    + " UNION SELECT * FROM BANQUES WHERE  CODEBANQUE IN (SELECT DISTINCT BANQUEREMETTANT FROM PRELEVEMENTS WHERE  " + etat + " AND BANQUEREMETTANT NOT LIKE '" + Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2) + "%' AND BANQUEREMETTANT NOT LIKE '" + Utility.getParam("CODE_BANQUE").charAt(0) + "%' AND BANQUEREMETTANT NOT IN ('" + Utility.getParam("CODE_BANQUE_SICA3") + "','" + Utility.getParam("CODE_BANQUE") + "'))"
                    + " UNION  SELECT * FROM BANQUES WHERE  CODEBANQUE IN (SELECT DISTINCT BANQUEREMETTANT FROM VIREMENTS WHERE " + etat + " AND BANQUEREMETTANT NOT LIKE '" + Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2) + "%' AND BANQUEREMETTANT NOT LIKE '" + Utility.getParam("CODE_BANQUE").charAt(0) + "%' AND BANQUEREMETTANT NOT IN ('" + Utility.getParam("CODE_BANQUE_SICA3") + "','" + Utility.getParam("CODE_BANQUE") + "'))";
        }
        Banques[] banques = (Banques[]) db.retrieveRowAsObject(sql, new Banques());
        return banques;
    }

    public static Banques[] getBanquesSousRegionales(DataBase db, String etat) {
        //Recuperation des Banques SOUS-REGIONALES SELON LA VERSION SICA EN COURS
        String sql;
        if (Utility.getParam("VERSION_SICA").equals("2")) {
            sql = "SELECT * FROM BANQUES WHERE ( CODEBANQUE IN (SELECT DISTINCT BANQUE FROM CHEQUES WHERE " + etat + ") OR "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUE FROM EFFETS WHERE " + etat + ") OR "
                    + "CODEBANQUE IN (SELECT DISTINCT BANQUE FROM VIREMENTS WHERE " + etat + ") ) AND CODEBANQUE NOT LIKE '" + Utility.getParam("CODE_BANQUE").charAt(0) + "%'AND CODEBANQUE <>'" + Utility.getParam("CODE_BANQUE") + "'";
        } else {
            sql = "SELECT * FROM BANQUES WHERE  CODEBANQUE IN (SELECT DISTINCT BANQUE FROM CHEQUES WHERE   " + etat + " AND BANQUE NOT LIKE '" + Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2) + "%' AND BANQUE NOT LIKE '" + Utility.getParam("CODE_BANQUE").charAt(0) + "%' AND BANQUE NOT IN ('" + Utility.getParam("CODE_BANQUE_SICA3") + "','" + Utility.getParam("CODE_BANQUE") + "'))"
                    + " UNION SELECT * FROM BANQUES WHERE  CODEBANQUE IN (SELECT DISTINCT BANQUE FROM EFFETS WHERE  " + etat + " AND BANQUE NOT LIKE '" + Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2) + "%' AND BANQUE NOT LIKE '" + Utility.getParam("CODE_BANQUE").charAt(0) + "%' AND BANQUE NOT IN ('" + Utility.getParam("CODE_BANQUE_SICA3") + "','" + Utility.getParam("CODE_BANQUE") + "'))"
                    + " UNION SELECT * FROM BANQUES WHERE  CODEBANQUE IN (SELECT DISTINCT BANQUE FROM PRELEVEMENTS WHERE  " + etat + " AND BANQUE NOT LIKE '" + Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2) + "%' AND BANQUE NOT LIKE '" + Utility.getParam("CODE_BANQUE").charAt(0) + "%' AND BANQUE NOT IN ('" + Utility.getParam("CODE_BANQUE_SICA3") + "','" + Utility.getParam("CODE_BANQUE") + "'))"
                    + " UNION  SELECT * FROM BANQUES WHERE  CODEBANQUE IN (SELECT DISTINCT BANQUE FROM VIREMENTS WHERE  " + etat + " AND BANQUE NOT LIKE '" + Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2) + "%' AND BANQUE NOT LIKE '" + Utility.getParam("CODE_BANQUE").charAt(0) + "%' AND BANQUE NOT IN ('" + Utility.getParam("CODE_BANQUE_SICA3") + "','" + Utility.getParam("CODE_BANQUE") + "'))";
        }
        Banques[] banques = (Banques[]) db.retrieveRowAsObject(sql, new Banques());
        return banques;
    }

    public static Banques[] getBanquesNationales() {
        Banques[] banques = null;
        try {
            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            //Recuperation des Banques SOUS-REGIONALES SELON LA VERSION SICA EN COURS
            String sql = " SELECT * FROM BANQUES WHERE CODEBANQUE LIKE '" + Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2) + "%'  ";

            banques = (Banques[]) db.retrieveRowAsObject(sql, new Banques());

        } catch (Exception ex) {
            Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return banques;
    }

    public static Banques[] getBanquesSousRegionales() {
        Banques[] banques = null;
        try {
            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            //Recuperation des Banques SOUS-REGIONALES SELON LA VERSION SICA EN COURS
            String sql = " SELECT * FROM BANQUES WHERE CODEBANQUE NOT LIKE  '" + Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2) + "%'  AND  REGEXP_LIKE(CODEPAYS, '[A-Z][A-Z]')  "; //AND  REGEXP_LIKE(CODEPAYS, '[A-Z][A-Z]')

            banques = (Banques[]) db.retrieveRowAsObject(sql, new Banques());

        } catch (Exception ex) {
            Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return banques;
    }

    public static String getFilePath(String type, String natOrSrg) {
        return Utility.getParam(type + "_" + natOrSrg + "_FOLDER");
    }

    public static String getPacSCSR() {
        return Utility.getParam("PAC_SCSR");
    }

    public static String getPacSCMP() {
        return Utility.getParam("PAC_SCMP");
    }

    public static String getPacSCSRSICA3() {
        return Utility.getParam("PAC_SCSR_SICA3");
    }

    public static String getPacSCMPSICA3() {
        return Utility.getParam("PAC_SCMP_SICA3");
    }

    public static String getFileName(String pac, String seq, String type) {
        if (Utility.getParam("VERSION_SICA").equals("2")) {
            return getCodeBanque() + "." + pac + "." + getDevise() + "." + seq + "." + getDateHeure() + "." + type;
        } else {
            return getCodeBanqueSica3() + "." + pac + "." + getDevise() + "." + seq + "." + getDateHeure() + "." + type;
        }
    }

    public static String getMailiFileName(String pac, String destinataire, String lieu, String seq, String genre, String type) {
        if (Utility.getParam("VERSION_SICA").equals("2")) {
            return getCodeBanque() + "." + pac + "." + destinataire + "." + lieu + "." + getDevise() + "." + seq + "." + getDate() + "." + genre + "." + type;
        } else {
            return getCodeBanqueSica3() + "." + pac + "." + getCodeBanqueDestinataire(destinataire) + /*"."+ lieu +*/ "." + getDevise() + "." + seq + "." + getDate() + "." + genre + "." + type;
        }
    }

    public static String getNatFileName(String seq, String type) {
        if (Utility.getParam("VERSION_SICA").equals("2")) {
            return getFilePath(type, "NAT") + File.separator + getFileName(getPacSCMP(), seq, type);
        } else {
            return getFilePath(type, "NAT") + File.separator + getFileName(getPacSCMPSICA3(), seq, type);
        }
    }

    public static String getMailiNatFileName(String destinataire, String lieu, String seq, String genre, String type) {
        if (Utility.getParam("VERSION_SICA").equals("2")) {
            return getFilePath(type, "NAT") + File.separator + getMailiFileName(getCodeBanque().charAt(0) + lieu, destinataire, lieu, seq, genre, type);
        } else {
            return getFilePath(type, "NAT") + File.separator + getMailiFileName(getPacSCMPSICA3(), destinataire, lieu, seq, genre, type);
        }
    }

    public static String getMailiSrgFileName(String destinataire, String lieu, String seq, String genre, String type) {
        if (Utility.getParam("VERSION_SICA").equals("2")) {
            return getFilePath(type, "SRG") + File.separator + getMailiFileName(getPacSCSR(), destinataire, lieu, seq, genre, type);
        } else {
            return getFilePath(type, "SRG") + File.separator + getMailiFileName(getPacSCSRSICA3(), destinataire, lieu, seq, genre, type);
        }
    }

    public static String getSrgFileName(String seq, String type) {
        if (Utility.getParam("VERSION_SICA").equals("2")) {
            return getFilePath(type, "SRG") + File.separator + getFileName(getPacSCSR(), seq, type);
        } else {
            return getFilePath(type, "SRG") + File.separator + getFileName(getPacSCSRSICA3(), seq, type);
        }
    }

    public static String getCodeBanque() {
        return (Utility.getParam("CODE_BANQUE"));
    }

    public static String getCodeBanqueSica3() {
        return (Utility.getParam("CODE_BANQUE_SICA3"));
    }

    public static String getDevise() {
        return Utility.getParam("DEVISE");
    }

    public static String getDateHeure() {
        return Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss");

    }

    public static String getDate() {
        return Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd");

    }

    public static String getCodeBanqueDestinataire(String idBanRem) {
        if (Utility.getParam("VERSION_SICA").equals("2")) {
            return idBanRem;
        } else {
            if (idBanRem.charAt(1) == '0') {
                return Utility.getParam(idBanRem.substring(0, 2)) + idBanRem.substring(2);
            } else if (Character.isDigit(idBanRem.charAt(1))) {
                return Utility.getParam(idBanRem);
            } else {
                return idBanRem;
            }
        }
    }

    public static String getAgencePrincipale(String banque) {
        String sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '%" + banque + "%' AND LIBELLEAGENCE LIKE '%PRINCIPALE%'";
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        String temp = "01001";
        try {
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                temp = agences[0].getCodeagence().trim();
                return temp;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

    public static String getAgence(String banque) {
        String sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '%" + banque + "%' ";
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        String temp = "01001";
        try {
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                temp = agences[0].getCodeagence().trim();
                return temp;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

    public static String getLibelleBanque(String codeBanque) {
        String libelleBanque = codeBanque;
        try {
            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Banques[] banques = (Banques[]) db.retrieveRowAsObject("SELECT * FROM BANQUES WHERE CODEBANQUE=" + ((codeBanque == null) ? "''" : "'" + codeBanque + "'"), new Banques());
            if (banques != null && banques.length > 0) {
                libelleBanque = banques[0].getLibellebanque();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return libelleBanque;
    }

    public static String getAgenceACP(String banque, String agenceACP) {
        String sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '%" + banque + "%' AND CODEAGENCE like '%" + agenceACP + "%'";
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        String temp = "01001";
        try {
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
            if (agences != null && agences.length > 0) {
                temp = agences[0].getCodeville().trim() + agences[0].getCodeagence().trim();
                return temp;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

    public static String getNumCptExF7(String numeroCompte, String agence) throws Exception {

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numero  ='" + Utility.bourrageGZero(numeroCompte, 12) + "' and agence ='" + agence + "'", new Comptes());

        if (comptes != null && comptes.length > 0) {
            return comptes[0].getNumcptex().trim();
        }

        String numCptEx = agence.substring(2) + "0" + numeroCompte;

        comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numcptex ='" + numCptEx + "'", new Comptes());
        if (comptes != null && comptes.length > 0) {
            return comptes[0].getNumcptex().trim();
        }

        comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numero ='" + Utility.bourrageGZero(numeroCompte, 12) + "'", new Comptes());

        if (comptes != null && comptes.length > 0) {
            return comptes[0].getNumcptex().trim();
        }

        db.close();
        return null;
    }

    public static String getNumCptEx(String numeroCompte, String agence, String migration) throws Exception {

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String accountLength = Utility.getParam("ACCOUNT_LENGTH") != null ? Utility.getParam("ACCOUNT_LENGTH") : "16";

        String cptUser = "";
        if (numeroCompte != null && numeroCompte.length() != 9) {
            cptUser = Utility.bourrageGZero(numeroCompte, 12);
        } else if (numeroCompte != null && numeroCompte.length() == 9) {
            cptUser = numeroCompte;
        }
        Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numero  ='" + cptUser + "' and agence ='" + agence + "'", new Comptes());

        if (comptes != null && comptes.length > 0) {
            return Utility.bourrageDroite(comptes[0].getNumcptex().trim(), Integer.parseInt(accountLength), " ");
        }

        comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numero ='" + cptUser + "'", new Comptes());

        if (comptes != null && comptes.length > 0) {
            return Utility.bourrageDroite(comptes[0].getNumcptex().trim(), Integer.parseInt(accountLength), " ");
        }
        if (migration.equals("0")) {
            if (comptes != null && comptes.length == 0) {
                return Utility.bourrageDroite(numeroCompte, Integer.parseInt(accountLength), " ");
            }
        }

        db.close();
        return null;
    }

    public static String getNumCptExAgence(String numeroCompte, String agence) throws Exception {

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String cptUser = "";
        if (numeroCompte != null && numeroCompte.length() != 9) {
            cptUser = Utility.bourrageGZero(numeroCompte, 12);
        } else if (numeroCompte != null && numeroCompte.length() == 9) {
            cptUser = numeroCompte;
        }

        Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numero  ='" + cptUser + "' and agence ='" + agence + "'", new Comptes());

        if (comptes != null && comptes.length > 0) {
            return comptes[0].getAdresse2().trim();
        }

        comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numero ='" + cptUser + "'", new Comptes());

        if (comptes != null && comptes.length > 0) {
            return comptes[0].getAdresse2().trim();
        }
        if (comptes != null && comptes.length == 0) {
            return agence.substring(2);
        }
        db.close();
        return null;
    }

    public static VW_CMPTBCEAO getCompteFlexCube(String numeroCompte, String agence) throws Exception {

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        VW_CMPTBCEAO[] comptes = (VW_CMPTBCEAO[]) db.retrieveRowAsObject("select * from VW_CMPTBCEAO where numero  ='" + Utility.bourrageGZero(numeroCompte, 12) + "' and agence ='" + agence + "'", new VW_CMPTBCEAO());

        if (comptes != null && comptes.length > 0) {
            return comptes[0];
        }

        String numCptEx = agence.substring(2) + "0" + numeroCompte;

        comptes = (VW_CMPTBCEAO[]) db.retrieveRowAsObject("select * from VW_CMPTBCEAO where numcptex ='" + numCptEx + "'", new VW_CMPTBCEAO());
        if (comptes != null && comptes.length > 0) {
            return comptes[0];
        }

        comptes = (VW_CMPTBCEAO[]) db.retrieveRowAsObject("select * from VW_CMPTBCEAO where numero ='" + Utility.bourrageGZero(numeroCompte, 12) + "'", new VW_CMPTBCEAO());

        if (comptes != null && comptes.length > 0) {
            return comptes[0];
        }

        db.close();
        return null;
    }

    public static Comptes getCompteESNFlexCube(String numeroCompte, String agence) throws Exception {

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String cptUser = "";
        if (numeroCompte != null && numeroCompte.length() != 9) {
            cptUser = Utility.bourrageGZero(numeroCompte, 12);
        } else if (numeroCompte != null && numeroCompte.length() == 9) {
            cptUser = numeroCompte;
        }
        Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("select * from Comptes where numero  ='" + cptUser + "' and agence ='" + agence + "'", new Comptes());

        if (comptes != null && comptes.length > 0) {
            return comptes[0];
        }

        System.out.println("Compte not found  " + numeroCompte);
        String numCptEx;
        if (Utility.getParam("ACCOUNT_LENGTH") != null && Utility.getParam("ACCOUNT_LENGTH").equals("19")) {
            numCptEx = agence.substring(0, 2) + "000" + numeroCompte + Utility.computeCleRIB(Utility.getParam("CODE_BANQUE_SICA3"), agence, numeroCompte);
        } else {
            numCptEx = agence.substring(2) + "0" + numeroCompte;
        }

        comptes = (Comptes[]) db.retrieveRowAsObject("select * from Comptes where numcptex ='" + numCptEx + "'", new Comptes());
        if (comptes != null && comptes.length > 0) {
            return comptes[0];
        }

        comptes = (Comptes[]) db.retrieveRowAsObject("select * from Comptes where numero ='" + Utility.bourrageGZero(numeroCompte, 12) + "'", new Comptes());

        if (comptes != null && comptes.length > 0) {
            return comptes[0];
        }

        db.close();
        return null;
    }

    public static String getNumCpt(String numeroCompte, String agence) throws Exception {

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numero  like '%" + numeroCompte + "%' and agence ='" + agence + "'", new Comptes());
        db.close();

        if (comptes != null && comptes.length > 0) {
            return comptes[0].getNumero().trim();
        }

        return null;
    }

    public static String getNumCptExEAC(String numeroCompte, String agence) throws Exception {

        return agence.substring(2)
                .concat(numeroCompte.substring(0, 3))
                .concat(Utility.getParam("FILIALE_ECOBANK"))
                .concat(numeroCompte.substring(3));

    }

    public static String getAcctClass(String numeroCompte, String agence) throws Exception {

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numero  ='" + Utility.bourrageGZero(numeroCompte, 12) + "' and agence ='" + agence + "'", new Comptes());
        db.close();

        if (comptes != null && comptes.length > 0) {
            if (comptes[0].getSignature1() != null) {
                return comptes[0].getSignature1().trim();
            }
        }
        return null;
    }

    private static String getAlphaSicaCode(String numericCode, String type) {
        String result = null;

        result = codeSica2Cache.get(numericCode);
        if (result != null) {
            return result;
        }
        result = codeSica3Cache.get(numericCode);
        if (result != null) {
            return result;
        }

        try {

            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String sql = "SELECT * FROM PARAMS WHERE VALEUR='" + numericCode + "'AND TYPE ='" + type + "'";
            Params[] params = (Params[]) db.retrieveRowAsObject(sql, new Params());
            if (params != null && params.length > 0) {

                if (type.equalsIgnoreCase("CODE_SICA2")) {
                    if ("23".contains(numericCode)) {
                        for (Params param : params) {
                            if (param.getNom().trim().equalsIgnoreCase(Utility.getParam("CODE_BANQUE").substring(0, 1))) {
                                codeSica2Cache.put(param.getValeur().trim(), param.getNom().trim());
                                return codeSica2Cache.get(numericCode);
                            }
                        }
                        return "_";
                    } else {
                        codeSica2Cache.put(params[0].getValeur().trim(), params[0].getNom().trim());
                        return codeSica2Cache.get(numericCode);
                    }

                } else {
                    codeSica3Cache.put(params[0].getValeur().trim(), params[0].getNom().trim());
                    return codeSica3Cache.get(numericCode);
                }

            } else {
                System.out.println("Il n'existe pas de Parametre de type = " + type + " et de valeur = " + numericCode);

            }

            db.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static String getAlphaNumericCodeBanque(String numeric) {
        String codeBanque = numeric;
        String alpha = getAlphaSicaCode(numeric.substring(0, 2), "CODE_SICA3");

        if (alpha != null) {

            codeBanque = numeric.replaceFirst(numeric.substring(0, 2), alpha);
            return codeBanque;

        }

        alpha = getAlphaSicaCode(numeric.substring(0, 1), "CODE_SICA2");
        if (alpha != null) {

            codeBanque = numeric.replaceFirst(numeric.substring(0, 1), alpha);
            return codeBanque;
        }

        codeBanque = "_____";
        return codeBanque;

    }

    public static boolean isBanqueNationale(String codeBanque) {
        return codeBanque.substring(0, 2).equalsIgnoreCase(CMPUtility.getCodeBanqueSica3().substring(0, 2));
    }

    public static String[] getDeltaSignatures(String agence, String numeroCompte) {
        String results[] = null;
        String converteds[] = null;
        String strChemin = Utility.getParam("SIGNATURE_FOLDER") + File.separator + agence + File.separator + numeroCompte.substring(1);
        String otPath = Utility.getParam("LOCAL_SIGN_FOLDER") + File.separator + agence + File.separator + numeroCompte.substring(1);

        File cheminSignature = new File(strChemin);
        if (cheminSignature.exists()) {

            results = cheminSignature.list();
            System.out.println("results length" + results.length);
            converteds = new String[results.length];
            for (int i = 0; i < results.length; i++) {
                System.out.println(results[i]);
                if (results[i].contains("_")) {
                    Utility.createFolderIfItsnt(new File(otPath), null);
                    try {
                        Utility.convertTIFFToJPEG(strChemin + File.separator + results[i], otPath + File.separator + results[i] + ".jpg");
                    } catch (Exception ex) {
                        Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex);
                        try {
                            FileUtils.copyFile(new File(strChemin + File.separator + results[i]), new File(otPath + File.separator + results[i] + ".jpg"));
                        } catch (IOException ex1) {
                            Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                    }
                    converteds[i] = otPath + File.separator + results[i] + ".jpg";
                } else {
                    BufferedReader is;
                    try {
                        is = new BufferedReader(new FileReader(strChemin + File.separator + results[i]));
                        converteds[i] = Utility.convertReaderToString(is);
                        is.close();
                    } catch (Exception ex) {
                        Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }

            }
            if (results != null) {
                System.out.println(results.length + " Signatures et/ou instructions trouvées");
            }
        } else {
            System.out.println("Le chemin " + cheminSignature.getAbsolutePath() + " n'est pas accessible");
            converteds = new String[1];
            converteds[0] = "Le chemin " + cheminSignature.getAbsolutePath() + " n'est pas accessible";

        }

        return converteds;

    }

    public static String[] getFlexSignatures(String agence, String numeroCompte) {

        String[] results = null;
        ArrayList<String> lstConverteds = null;
        ArrayList<String> lstInstructions = null;
        ArrayList<String> lstAccountInfos = null;
        ArrayList<String> lstGestInfos = null;

        if (agence != null && agence.length() == 5 && numeroCompte != null && numeroCompte.length() == 12) {
            //old FlexCube 7
//            String account = agence.substring(2) + "0" + numeroCompte;
//            String altAccount = agence.substring(3) + numeroCompte;
            //NEW Flexcube 12

            String account = numeroCompte;
            Comptes cpt = null;
            try {
                cpt = getCompteESNFlexCube(numeroCompte, agence);
            } catch (Exception e) {
            }
            if (cpt != null) {
                account = cpt.getNumcptex();
            }

            String otPath = Utility.getParam("LOCAL_SIGN_FOLDER") + File.separator + agence + File.separator + numeroCompte;
            Utility.createFolderIfItsnt(new File(otPath), null);
            DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
            try {
                dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());
            } catch (Exception ex) {
                Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex);
            }

            /**
             * NEW FOR FLEXCUBE 12
             */
            //select cif_id, a.cif_sig_id, specimen_no, specimen_seq_no, nvl(signature, 'ERROR SIGNATURE') signature, a.record_stat, sig_text, sign_img, file_type, status, nvl(b.cif_sig_name, 'ERROR SIGNAME') signame from FCUBSFWA.SVTM_CIF_SIG_DET a ,FCUBSFWA.SVTM_ACC_SIG_DET b  where a.record_stat<>'D' and a.SIGN_IMG is not null and a.cif_id=substr(b.acc_no, 0, 9)  and a.cif_sig_id=b.cif_sig_id and branch in(select branch_code from FCUBSFWA.sttm_branch where regional_office='ESN') and acc_no=(select cust_ac_no from FCUBSFWA.sttm_cust_account where cust_ac_no='100031781007' or alt_ac_no = '100031781007') 
//"select a.*,b.cif_sig_name signame from fcubsfwa.SVTM_CIF_SIG_DET a , fcubsfwa.SVTM_ACC_SIG_DET b where a.record_stat<>'D' and a.SIGN_IMG is not null and a.cif_id=substr(b.acc_no, 0, 9) and a.cif_sig_id=b.cif_sig_id and branch in(select branch_code from fcubsfwa.sttm_branch where regional_office='ESN') and acc_no=(select cust_ac_no from fcubsfwa.sttm_cust_account where cust_ac_no='100007880001' or alt_ac_no = '0010811001254001')  union all  select  d.*,d.signature signame  from fcubsfwa.SVTM_CIF_SIG_DET d  where d.record_stat<>'D' and d.SIGN_IMG is not null and d.cif_sig_id  in (select cif_sig_id from fcubsfwa.SVTM_ACC_SIG_DET where acc_no=(select cust_ac_no from fcubsfwa.sttm_cust_account where  (cust_ac_no='100007880001' or alt_ac_no = '0010811001254001') and branch_code = branch) and branch in(select branch_code from fcubsfwa.sttm_branch where regional_office='ESN') )   and cif_id=(select cust_no from fcubsfwa.sttm_cust_account where cust_ac_no='100007584001' or alt_ac_no = '0010811001220501') ";
//            String sql = "select a.*,b.cif_sig_name signame from " + Utility.getParam("FLEXSCHEMA") + ".SVTM_CIF_SIG_DET a , " + Utility.getParam("FLEXSCHEMA") + ".SVTM_ACC_SIG_DET b "
//                    + " where a.record_stat<>'D' and a.SIGN_IMG is not null and a.cif_id=substr(b.acc_no, 0, 9) "
//                    + " and a.cif_sig_id=b.cif_sig_id and branch in(select branch_code from " + Utility.getParam("FLEXSCHEMA") + ".sttm_branch where regional_office='" + Utility.getParam("FILIALE") + "') "
//                    + " and acc_no=(select cust_ac_no from " + Utility.getParam("FLEXSCHEMA") + ".sttm_cust_account where cust_ac_no='" + account + "'"
//                    + " or alt_ac_no = '" + account + "') "
//                    
//                    + " union all "
//                    + " select  d.*,d.signature signame  from " + Utility.getParam("FLEXSCHEMA") + ".SVTM_CIF_SIG_DET d  where d.record_stat<>'D'"
//                    + " and d.SIGN_IMG is not null and d.cif_sig_id  in (select cif_sig_id from " + Utility.getParam("FLEXSCHEMA") + ".SVTM_ACC_SIG_DET"
//                    + "  where acc_no=(select cust_ac_no from " + Utility.getParam("FLEXSCHEMA") + ".sttm_cust_account where  (cust_ac_no='" + account + "' or alt_ac_no = '" + account + "')"
//                    + "  and branch_code = branch) and branch in(select branch_code from " + Utility.getParam("FLEXSCHEMA") + ".sttm_branch where regional_office='" + Utility.getParam("FILIALE") + "') ) "
//                    + "  and cif_id=(select cust_no from " + Utility.getParam("FLEXSCHEMA") + ".sttm_cust_account where cust_ac_no='" + account + "' or alt_ac_no = '" + account + "') ";
     

//ancienne requette
/*String sql = " select cif_id, a.cif_sig_id, specimen_no, specimen_seq_no, nvl(signature, 'ERROR SIGNATURE') signature, a.record_stat,"
                    + " sig_text, sign_img, file_type, status, nvl(b.cif_sig_name, 'ERROR SIGNAME') signame from " + Utility.getParam("FLEXSCHEMA") + ".SVTM_CIF_SIG_DET a ,"
                    + " " + Utility.getParam("FLEXSCHEMA") + ".SVTM_ACC_SIG_DET b  where a.record_stat<>'D' and a.SIGN_IMG is not null and a.cif_id=substr(b.acc_no, 0, 9)"
                    + "  and a.cif_sig_id=b.cif_sig_id and branch "
                    + " in(select branch_code from " + Utility.getParam("FLEXSCHEMA") + ".sttm_branch where regional_office='" + Utility.getParam("FILIALE") + "')"
                    + " and acc_no=(select cust_ac_no from " + Utility.getParam("FLEXSCHEMA") + ".sttm_cust_account where cust_ac_no='" + account + "' or alt_ac_no = '" + account + "') "
                    + " union all  "
                    + " select cif_id, d.cif_sig_id, specimen_no, specimen_seq_no, nvl(signature, 'ERROR SIGNATURE') signature,"
                    + " d.record_stat, sig_text, sign_img, file_type, status, nvl(d.signature, 'ERROR SIGNAME')"
                    + " signame from " + Utility.getParam("FLEXSCHEMA") + ".SVTM_CIF_SIG_DET d where d.record_stat<>'D' and d.SIGN_IMG is not "
                    + " null and d.cif_sig_id in (select cif_sig_id from " + Utility.getParam("FLEXSCHEMA") + ".SVTM_ACC_SIG_DET  "
                    + " where acc_no=(select cust_ac_no from " + Utility.getParam("FLEXSCHEMA") + ".sttm_cust_account where "
                    + "(cust_ac_no='" + account + "' or alt_ac_no = '" + account + "')  and branch_code = branch)"
                    + " and branch in(select branch_code from " + Utility.getParam("FLEXSCHEMA") + ".sttm_branch where regional_office='" + Utility.getParam("FILIALE") + "') )"
                    + " and cif_id=(select cust_no from " + Utility.getParam("FLEXSCHEMA") + ".sttm_cust_account where cust_ac_no='" + account + "'"
                    + " or alt_ac_no = '" + account + "')    ";*/

//nouvelle requette
String sql = " select cif_id, a.cif_sig_id, specimen_no, specimen_seq_no, nvl(signature, 'ERROR SIGNATURE') signature, a.record_stat,"
                    + " sig_text, sign_img, file_type, status, nvl(b.cif_sig_name, 'ERROR SIGNAME') signame from " + Utility.getParam("FLEXSCHEMA") + ".SVTM_CIF_SIG_DET a ,"
                    + " " + Utility.getParam("FLEXSCHEMA") + ".SVTM_ACC_SIG_DET b  where  a.cif_id=substr(b.acc_no, 0, 9)"
                    + "  and a.cif_sig_id=b.cif_sig_id and branch "
                    + " in(select branch_code from " + Utility.getParam("FLEXSCHEMA") + ".sttm_branch where regional_office='" + Utility.getParam("FILIALE") + "')"
                    + " and acc_no=(select cust_ac_no from " + Utility.getParam("FLEXSCHEMA") + ".sttm_cust_account where cust_ac_no='" + account + "' or alt_ac_no = '" + account + "') "
                    + " union all  "
                    + " select cif_id, d.cif_sig_id, specimen_no, specimen_seq_no, nvl(signature, 'ERROR SIGNATURE') signature,"
                    + " d.record_stat, sig_text, sign_img, file_type, status, nvl(d.signature, 'ERROR SIGNAME')"
                    + " signame from " + Utility.getParam("FLEXSCHEMA") + ".SVTM_CIF_SIG_DET d where d.record_stat='O' and d.SIGN_IMG is not "
                    + " null and d.cif_sig_id in (select cif_sig_id from " + Utility.getParam("FLEXSCHEMA") + ".SVTM_ACC_SIG_DET  "
                    + " where acc_no=(select cust_ac_no from " + Utility.getParam("FLEXSCHEMA") + ".sttm_cust_account where "
                    + "(cust_ac_no='" + account + "' or alt_ac_no = '" + account + "')  and branch_code = branch)"
                    + " and branch in(select branch_code from " + Utility.getParam("FLEXSCHEMA") + ".sttm_branch where regional_office='" + Utility.getParam("FILIALE") + "') )"
                    + " and cif_id=(select cust_no from " + Utility.getParam("FLEXSCHEMA") + ".sttm_cust_account where cust_ac_no='" + account + "'"
                    + " or alt_ac_no = '" + account + "')    ";

            SVTM_CIF_SIG_DET[] svtm_cif_sig_det = (SVTM_CIF_SIG_DET[]) dbExt.retrieveRowAsObject(sql, new SVTM_CIF_SIG_DET());

            if (svtm_cif_sig_det != null && svtm_cif_sig_det.length > 0) {
                //converteds = new String[svtm_cif_sig_det.length];
                lstConverteds = new ArrayList<>(svtm_cif_sig_det.length);
                for (int i = 0; i < svtm_cif_sig_det.length; i++) {
                    try {
                        if (svtm_cif_sig_det[i].getSIGN_IMG() != null && svtm_cif_sig_det[i].getSIGN_IMG().length() > 0 && svtm_cif_sig_det[i].getSIGNAME() != null && svtm_cif_sig_det[i].getCIF_SIG_ID() != null && svtm_cif_sig_det[i].getSPECIMEN_NO() != null) {
                            String name = Utility.removeAccent((svtm_cif_sig_det[i].getSIGNAME() != null && !svtm_cif_sig_det[i].getSIGNAME().isEmpty()) ? svtm_cif_sig_det[i].getSIGNAME() : "Signataire") + "_" + svtm_cif_sig_det[i].getCIF_SIG_ID() + "_" + svtm_cif_sig_det[i].getSPECIMEN_NO();

                            BufferedImage bimg = null;
                            try {
                                bimg = Utility.createImageFromBytes(svtm_cif_sig_det[i].getSIGN_IMG().getBytes(1L, (int) svtm_cif_sig_det[i].getSIGN_IMG().length()));
                            } catch (SQLException ex) {
                                Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            lstConverteds.add(otPath + File.separator + name + ".jpg"); //Value a afficher dans l'ecran

                            try {
                                if (bimg != null) {
                                    ImageIO.write(bimg, "jpeg", new File(lstConverteds.get(i)));
                                }

                            } catch (IOException ex) {

                                Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else {
                            System.out.println("Aucune signature trouvée pour ce compte");
                            // lstConverteds = new ArrayList<String>(1);
                            lstConverteds.add("<BR>Aucune signature trouvée pour ce compte");
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            } else {
                System.out.println("Aucune signature trouvée pour ce compte");
                lstConverteds = new ArrayList<String>(1);
                lstConverteds.add("<BR>Aucune signature trouvée pour ce compte");

            }
//select acc_no as cust_ac_no, acc_msg as maint_instr_1 , '' maint_instr_2, '' maint_instr_3, ''maint_instr_4  from  fcubsfwa.SVTM_ACC_SIG_MASTER where 1=1   and branch in(select branch_code from fcubsfwa.sttm_branch where regional_office='ESN')    and branch <>'812'    and acc_no='100008246001'
            sql = "select acc_no as cust_ac_no, acc_msg as maint_instr_1 , '' maint_instr_2, '' maint_instr_3, ''maint_instr_4  from  " + Utility.getParam("FLEXSCHEMA") + ".SVTM_ACC_SIG_MASTER where 1=1 "
                    + "  and branch in(select branch_code from fcubsfwa.sttm_branch where regional_office='" + Utility.getParam("FILIALE") + "')    and branch <>'812'"
                    + "    and acc_no='" + account + "'";

            STTM_ACCOUNT_MAINT_INSTR[] sttm_account_maint_instrs = (STTM_ACCOUNT_MAINT_INSTR[]) dbExt.retrieveRowAsObject(sql, new STTM_ACCOUNT_MAINT_INSTR());
            if (sttm_account_maint_instrs != null && sttm_account_maint_instrs.length > 0) {

                lstInstructions = new ArrayList<String>(sttm_account_maint_instrs.length);
                for (int i = 0; i < sttm_account_maint_instrs.length; i++) {
                    STTM_ACCOUNT_MAINT_INSTR sttm_account_maint_instr = sttm_account_maint_instrs[i];
                    lstInstructions.add("<BR>INSTRUCTIONS:<BR>"
                            + sttm_account_maint_instr.getMaint_instr_1() + "<BR>"
                            + sttm_account_maint_instr.getMaint_instr_2() + "<BR>"
                            + sttm_account_maint_instr.getMaint_instr_3() + "<BR>"
                            + sttm_account_maint_instr.getMaint_instr_4() + "<BR>");

                }

            } else {
                System.out.println("Aucune instruction trouvée pour ce compte");
                lstInstructions = new ArrayList<String>(1);
                lstInstructions.add("<BR>Aucune instruction trouvée pour ce compte");

            }

            sql = " select a.cust_no, a.cust_ac_no, ac_stat_stop_pay, ac_stat_no_dr, acy_withdrawable_bal lcy_curr_balance , account_class "
                    + "  from " + Utility.getParam("FLEXSCHEMA") + ".sttm_cust_account a, " + Utility.getParam("FLEXSCHEMA") + ".sttm_account_balance b"
                    + " where a.cust_ac_no = b.cust_ac_no and record_stat='O' and auth_stat='A' and a.cust_ac_no='" + account + "'";
            STTM_CUST_ACCOUNT[] sttm_cust_accounts = (STTM_CUST_ACCOUNT[]) dbExt.retrieveRowAsObject(sql, new STTM_CUST_ACCOUNT());
            if (sttm_cust_accounts != null && sttm_cust_accounts.length > 0) {
                lstAccountInfos = new ArrayList<String>(sttm_cust_accounts.length);
                for (int i = 0; i < sttm_cust_accounts.length; i++) {
                    STTM_CUST_ACCOUNT sttm_cust_account = sttm_cust_accounts[i];
                    lstAccountInfos.add("STATUS NO DEBIT = " + sttm_cust_account.getAc_stat_no_dr()
                            + "<BR>STATUS STOP PAYMENT = " + sttm_cust_account.getAc_stat_stop_pay()
                            + "<BR>SOLDE COURANT = "
                            + ((sttm_cust_account.getAccount_class().equals("BJCASF")
                            || sttm_cust_account.getAccount_class().equals("BJSASF")) ? "XXXX"
                            : Utility.formatNumber("" + sttm_cust_account.getLcy_curr_balance())));

                }

            } else {
                System.out.println("Aucune instruction trouvée pour ce compte");
                lstAccountInfos = new ArrayList<String>(1);
                lstAccountInfos.add("<BR>Aucun status trouvé pour ce compte");

            }

            //select  cust_mis_1 , code_desc as code_desc  from  fcubsfwa.GLTM_MIS_CODE , fcubsfwa.MITM_CUSTOMER_DEFAULT  where mis_code =cust_mis_1 and mis_class='ACC_OFCR' and  customer ='" + account.substring(0, 9) + "'";
            sql = "select  cust_mis_1 , code_desc as code_desc  from  " + Utility.getParam("FLEXSCHEMA") + ".GLTM_MIS_CODE , " + Utility.getParam("FLEXSCHEMA") + ".MITM_CUSTOMER_DEFAULT "
                    + "  where mis_code =cust_mis_1 and mis_class='ACC_OFCR' and  customer ='" + account.substring(0, 9) + "'"; //+ (cpt != null) ? cpt.getNumcptex() : "" + account +

            GLTM_MIS_CODE[] gltm_mis_codes = (GLTM_MIS_CODE[]) dbExt.retrieveRowAsObject(sql, new GLTM_MIS_CODE());
            if (gltm_mis_codes != null && gltm_mis_codes.length > 0) {
                lstGestInfos = new ArrayList<String>(gltm_mis_codes.length);
                for (int i = 0; i < gltm_mis_codes.length; i++) {
                    GLTM_MIS_CODE gltm_mis_code = gltm_mis_codes[i];
                    lstGestInfos.add("<BR>NOM GESTIONNAIRE = " + gltm_mis_code.getCode_desc()
                            + "<BR>CODE GESTIONNAIRE = " + gltm_mis_code.getCust_mis_1());

                }

            } else {
                System.out.println("Aucun gestionnaire trouvé pour ce compte");
                lstGestInfos = new ArrayList<String>(1);
                lstGestInfos.add("<BR>Aucun gestionnaire trouvé pour ce compte");

            }
            lstConverteds.addAll(lstInstructions);
            lstConverteds.addAll(lstAccountInfos);
            lstConverteds.addAll(lstGestInfos);
            results = new String[lstConverteds.size()];
            results = lstConverteds.toArray(results);

        } else {
            System.out.println("Aucun compte saisi");
            lstConverteds = new ArrayList<String>(1);
            lstConverteds.add("<BR>Aucun compte saisi");
        }
        return results;

    }

    public static String[] getFlexSignatures7(String agence, String numeroCompte) throws Exception {

        String[] results = null;
        ArrayList<String> lstConverteds = null;
        ArrayList<String> lstInstructions = null;
        ArrayList<String> lstAccountInfos = null;
        ArrayList<String> lstGestInfos = null;

        if (agence != null && agence.length() == 5 && numeroCompte != null && numeroCompte.length() == 12) {
            Comptes compteFlexCube = CMPUtility.getCompteESNFlexCube(numeroCompte, agence);
            String numCptEx = "";

            if (compteFlexCube != null) {
                numCptEx = compteFlexCube.getNumcptex();
            } else {
                numCptEx = agence.substring(0, 2) + "000" + numeroCompte + Utility.computeCleRIB(Utility.getParam("CODE_BANQUE_SICA3"), agence, numeroCompte);
            }
            String account = numCptEx;
            String altAccount = numCptEx;
            String otPath = Utility.getParam("LOCAL_SIGN_FOLDER") + File.separator + agence + File.separator + numeroCompte;
            Utility.createFolderIfItsnt(new File(otPath), null);
            DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
            try {
                dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());
            } catch (Exception ex) {
                Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex);
            }

//            String sql = "select a.*,b.cif_sig_name signame from " + Utility.getParam("FLEXSCHEMA") + ".SVTM_CIF_SIG_DET a , " + Utility.getParam("FLEXSCHEMA") + ".SVTM_ACC_SIG_DET b where a.record_stat<>'D' and a.SIGN_IMG is not null and a.cif_id=substr(b.acc_no, 11, 5) and a.cif_sig_id=b.cif_sig_id and acc_no=(select cust_ac_no from " + Utility.getParam("FLEXSCHEMA") + ".sttm_cust_account where cust_ac_no='" + account + "' or alt_ac_no = '" + altAccount + "') "
//                    + "union all "
//                    + "select  d.*,d.signature signame from " + Utility.getParam("FLEXSCHEMA") + ".SVTM_CIF_SIG_DET d where d.record_stat<>'D' and d.SIGN_IMG is not null and d.cif_sig_id not in (select cif_sig_id from " + Utility.getParam("FLEXSCHEMA") + ".SVTM_ACC_SIG_DET where acc_no=(select cust_ac_no from " + Utility.getParam("FLEXSCHEMA") + ".sttm_cust_account where cust_ac_no='" + account + "' or alt_ac_no = '" + altAccount + "'))  and cif_id=substr((select cust_ac_no from " + Utility.getParam("FLEXSCHEMA") + ".sttm_cust_account where cust_ac_no='" + account + "' or alt_ac_no = '" + altAccount + "') , 11, 5)";
//           
            String sql = "select a.*,b.cif_sig_name signame from " + Utility.getParam("FLEXSCHEMA") + ".SVTM_CIF_SIG_DET a , " 
                                                                   + Utility.getParam("FLEXSCHEMA") + ".SVTM_ACC_SIG_DET b where"
                                                                   + " a.record_stat<>'D' and b.record_stat<>'D' and a.SIGN_IMG is not null and a.cif_sig_id=b.cif_sig_id and b.acc_no='" + account + "' ";
                   
            SVTM_CIF_SIG_DET[] svtm_cif_sig_det = (SVTM_CIF_SIG_DET[]) dbExt.retrieveRowAsObject(sql, new SVTM_CIF_SIG_DET());

            if (svtm_cif_sig_det != null && svtm_cif_sig_det.length > 0) {
                //converteds = new String[svtm_cif_sig_det.length];
                lstConverteds = new ArrayList<>(svtm_cif_sig_det.length);
                for (int i = 0; i < svtm_cif_sig_det.length; i++) {
                    if (svtm_cif_sig_det[i].getSIGN_IMG() != null) {
                        String name = Utility.removeAccent(svtm_cif_sig_det[i].getSIGNAME()) + "_" + svtm_cif_sig_det[i].getCIF_SIG_ID() + "_" + svtm_cif_sig_det[i].getSPECIMEN_NO();
                        //System.out.println("Nom = "+ name);
                        BufferedImage bimg = null;
                        try {
                            bimg = Utility.createImageFromBytes(svtm_cif_sig_det[i].getSIGN_IMG().getBytes(1L, (int) svtm_cif_sig_det[i].getSIGN_IMG().length()));
                        } catch (SQLException ex) {
                            Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        // converteds[i] = otPath + File.separator + name + ".jpg";
                        lstConverteds.add(otPath + File.separator + name + ".jpg");
                        try {
                            ImageIO.write(bimg, "jpeg", new File(lstConverteds.get(i)));
                        } catch (Exception ex) {
                            Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        System.out.println("Aucune signature trouvée pour ce compte");
                        lstConverteds = new ArrayList<String>(1);
                        lstConverteds.add("<BR>Aucune signature trouvée pour ce compte");
                    }

                }
            } else {
                System.out.println("Aucune signature trouvée pour ce compte");
                lstConverteds = new ArrayList<String>(1);
                lstConverteds.add("<BR>Aucune signature trouvée pour ce compte");

            }

            sql = "select cust_ac_no,maint_instr_1 , maint_instr_2, maint_instr_3, maint_instr_4 from " + Utility.getParam("FLEXSCHEMA") + ".STTM_ACCOUNT_MAINT_INSTR where record_stat='O' and auth_stat='A' and cust_ac_no='" + account + "'";
            STTM_ACCOUNT_MAINT_INSTR[] sttm_account_maint_instrs = (STTM_ACCOUNT_MAINT_INSTR[]) dbExt.retrieveRowAsObject(sql, new STTM_ACCOUNT_MAINT_INSTR());
            if (sttm_account_maint_instrs != null && sttm_account_maint_instrs.length > 0) {

                lstInstructions = new ArrayList<String>(sttm_account_maint_instrs.length);
                for (int i = 0; i < sttm_account_maint_instrs.length; i++) {
                    STTM_ACCOUNT_MAINT_INSTR sttm_account_maint_instr = sttm_account_maint_instrs[i];
                    lstInstructions.add("<BR>INSTRUCTIONS:<BR>"
                            + sttm_account_maint_instr.getMaint_instr_1() + "<BR>"
                            + sttm_account_maint_instr.getMaint_instr_2() + "<BR>"
                            + sttm_account_maint_instr.getMaint_instr_3() + "<BR>"
                            + sttm_account_maint_instr.getMaint_instr_4() + "<BR>");

                }

            } else {
                System.out.println("Aucune instruction trouvée pour ce compte");
                lstInstructions = new ArrayList<String>(1);
                lstInstructions.add("<BR>Aucune instruction trouvée pour ce compte");

            }

            sql = "select cust_no, cust_ac_no, ac_stat_stop_pay, ac_stat_no_dr, lcy_curr_balance  from " + Utility.getParam("FLEXSCHEMA") + ".sttm_cust_account where record_stat='O' and auth_stat='A' and cust_ac_no='" + account + "'";
            STTM_CUST_ACCOUNT7[] sttm_cust_accounts = (STTM_CUST_ACCOUNT7[]) dbExt.retrieveRowAsObject(sql, new STTM_CUST_ACCOUNT7());
            if (sttm_cust_accounts != null && sttm_cust_accounts.length > 0) {
                lstAccountInfos = new ArrayList<String>(sttm_cust_accounts.length);
                for (int i = 0; i < sttm_cust_accounts.length; i++) {
                    STTM_CUST_ACCOUNT7 sttm_cust_account = sttm_cust_accounts[i];
                    lstAccountInfos.add("STATUS NO DEBIT = " + sttm_cust_account.getAc_stat_no_dr()
                            + "<BR>STATUS STOP PAYMENT = " + sttm_cust_account.getAc_stat_stop_pay()
                            + "<BR>SOLDE COURANT = "
                            + ((sttm_cust_account.getCust_ac_no().substring(3, 6).equals("051")
                            || sttm_cust_account.getCust_ac_no().substring(3, 6).equals("085")) ? "XXXX"
                            : Utility.formatNumber("" + sttm_cust_account.getLcy_curr_balance())));

                }

            } else {
                System.out.println("Aucune instruction trouvée pour ce compte");
                lstAccountInfos = new ArrayList<String>(1);
                lstAccountInfos.add("<BR>Aucun status trouvé pour ce compte");

            }

            sql = "select  cust_mis_1 , code_desc  from  (SELECT '000' as cust_mis_1,  " + Utility.getParam("FLEXSCHEMA") + ".F_RETURN_GESTIONNAIRE_SN('"+ numeroCompte+"') as code_desc from dual )";

            GLTM_MIS_CODE[] gltm_mis_codes = (GLTM_MIS_CODE[]) dbExt.retrieveRowAsObject(sql, new GLTM_MIS_CODE());
            if (gltm_mis_codes != null && gltm_mis_codes.length > 0) {
                lstGestInfos = new ArrayList<String>(gltm_mis_codes.length);
                for (int i = 0; i < gltm_mis_codes.length; i++) {
                    GLTM_MIS_CODE gltm_mis_code = gltm_mis_codes[i];
                    lstGestInfos.add("<BR>NOM GESTIONNAIRE = " + gltm_mis_code.getCode_desc()
                            + "<BR>CODE GESTIONNAIRE = " + gltm_mis_code.getCust_mis_1());

                }

            } else {
                System.out.println("Aucun gestionnaire trouvé pour ce compte");
                lstGestInfos = new ArrayList<String>(1);
                lstGestInfos.add("<BR>Aucun gestionnaire trouvé pour ce compte");

            }
            lstConverteds.addAll(lstInstructions);
            lstConverteds.addAll(lstAccountInfos);
            lstConverteds.addAll(lstGestInfos);
            results = new String[lstConverteds.size()];
            results = lstConverteds.toArray(results);

        } else {
            System.out.println("Aucun compte saisi");
            lstConverteds = new ArrayList<String>(1);
            lstConverteds.add("<BR>Aucun compte saisi");
        }
        return results;

    }

    public static String[] getAmplitudeSignatures(String agence, String numeroCompte) {

        String[] results = null;
        ArrayList<String> lstConverteds = null;
        ArrayList<String> lstInstructions = null;
        String ownerDeltaSignature = "deltasig";
        if (Utility.getParam("DELTASIGOWN") != null) {
            ownerDeltaSignature = Utility.getParam("DELTASIGOWN");
        }
        String strSqlQuery = "SELECT IDENT_SIG, IDENT_BNK, IDENT_CARTON, NO_CARTON, CREAT_TIME, CREAT_CUTI, CREAT_VALID_DATE, "
                + "CREAT_VALID, CREAT_VALID_TIME, COMMENTAIRE, IDENT_NCP, AGE, NCP, SUF, DEV, NO_FORMAT, b.CHEMIN, TYPE_DE_FICHIER, "
                + "NB_BITS_PIXEL, WIDTH, HEIGHT, IMAGE, TAILLE FROM  " + ownerDeltaSignature + ".bksig_v_Image_Valid_compte c , " + ownerDeltaSignature + ".bksig_blob b  ";

        if (agence != null && agence.length() == 5 && numeroCompte != null) {
            String account = numeroCompte.substring(1);
            String accountQuery = "c.ncp";
            if (Utility.getParam("DELTASIGACC") != null) {
                accountQuery = Utility.getParam("DELTASIGACC");
            }

            String otPath = Utility.getParam("LOCAL_SIGN_FOLDER") + File.separator + agence + File.separator + numeroCompte;
            Utility.createFolderIfItsnt(new File(otPath), null);
            DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
            try {
                dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());
            } catch (Exception ex) {
                Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex);
            }

            String sql = strSqlQuery + " where b.chemin=c.chemin and " + accountQuery + "='" + account + "' and c.age='" + agence + "'";
            BksigImageValideCompte[] bivcs = (BksigImageValideCompte[]) dbExt.retrieveRowAsObject(sql, new BksigImageValideCompte());

            if (bivcs != null && bivcs.length > 0) {
                //converteds = new String[svtm_cif_sig_det.length];
                lstConverteds = new ArrayList<>(bivcs.length);
                lstInstructions = new ArrayList<>(bivcs.length);

                for (int i = 0; i < bivcs.length; i++) {
                    if (bivcs[i].getIMAGE() != null) {
                        String name = bivcs[i].getNCP() + "_" + bivcs[i].getCHEMIN();
                        //System.out.println("Nom = "+ name);
                        BufferedImage bimg = null;
                        try {
                            bimg = Utility.createImageFromBytes(bivcs[i].getIMAGE().getBytes(1L, (int) bivcs[i].getIMAGE().length()));
                        } catch (SQLException ex) {
                            Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        // converteds[i] = otPath + File.separator + name + ".jpg";
                        String format = Utility.getParamOfType("" + bivcs[i].getNO_FORMAT(), "CODE_FORMAT");
                        if (format == null) {
                            System.out.println("Pas de format paramétré : " + bivcs[i].getNO_FORMAT());
                            break;
                        }
                        lstConverteds.add(otPath + File.separator + name + "." + format.toLowerCase());
                        lstInstructions.add("<BR>" + bivcs[i].getCOMMENTAIRE() + "<BR>");
                        try {
                            ImageIO.write(bimg, format.toLowerCase(), new File(lstConverteds.get(i)));
                        } catch (Exception ex) {
                            Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        System.out.println("Aucune signature trouvée pour ce compte");
                        lstConverteds = new ArrayList<String>(1);
                        lstConverteds.add("<BR>Aucune signature trouvée pour ce compte");
                        System.out.println("Aucune instruction trouvée pour ce compte");
                        lstInstructions = new ArrayList<String>(1);
                        lstInstructions.add("<BR>Aucune instruction trouvée pour ce compte");
                    }

                }
            } else {
                System.out.println("Aucune signature trouvée pour ce compte");
                lstConverteds = new ArrayList<String>(1);
                lstConverteds.add("<BR>Aucune signature trouvée pour ce compte");
                System.out.println("Aucune instruction trouvée pour ce compte");
                lstInstructions = new ArrayList<String>(1);
                lstInstructions.add("<BR>Aucune instruction trouvée pour ce compte");

            }

            dbExt.close();
            lstConverteds.addAll(lstInstructions);
            results = new String[lstConverteds.size()];
            results = lstConverteds.toArray(results);

        } else {
            System.out.println("Aucun compte saisi");
            lstConverteds = new ArrayList<String>(1);
            lstConverteds.add("<BR>Aucun compte saisi");
        }
        return results;

    }

    public static String[] getOrionSignatures(String agence, String numeroCompte) {

        String[] results = null;
        ArrayList<String> lstConverteds = null;
        ArrayList<String> lstInstructions = null;

        String strSqlQuery = "SELECT * FROM SIBICSIG ";
        if (agence != null && agence.length() == 5 && numeroCompte != null) {
            String account = numeroCompte;
            String otPath = Utility.getParam("LOCAL_SIGN_FOLDER") + File.separator + agence + File.separator + numeroCompte;
            Utility.createFolderIfItsnt(new File(otPath), null);
            DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
            try {
                dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());
            } catch (Exception ex) {
                Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex);
            }

            String sql = strSqlQuery + " where COMPTE ='" + account + "' and GUICHET ='" + agence + "'";
            clearing.table.orion.Sibicsig[] bivcs = (clearing.table.orion.Sibicsig[]) dbExt.retrieveRowAsObject(sql, new clearing.table.orion.Sibicsig());

            dbExt.close();
            if (bivcs != null && bivcs.length > 0) {
                //converteds = new String[svtm_cif_sig_det.length];
                lstConverteds = new ArrayList<String>(bivcs.length);
                lstInstructions = new ArrayList<String>(bivcs.length);

                for (int i = 0; i < bivcs.length; i++) {
                    if (bivcs[i].getChemin() != null) {

                        String name = bivcs[i].getChemin() + File.separator + bivcs[i].getNomimg();
                        System.out.println("name getOrionSignatures " + name);
                        try {
                            FileUtils.copyFile(new File(name), new File(otPath + File.separator + bivcs[i].getNomimg()));
                        } catch (Exception ex) {
                            //Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex);
                            if (ex instanceof FileNotFoundException) {
                                name = Utility.getParam("DISTANT_SIGN_FOLDER") + File.separator + bivcs[i].getNomimg();
                                try {
                                    FileUtils.copyFile(new File(name), new File(otPath + File.separator + bivcs[i].getNomimg()));
                                } catch (IOException ex1) {
                                    Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex1);
                                }
                            }
                        }
                        lstConverteds.add(otPath + File.separator + bivcs[i].getNomimg());
                        lstInstructions.add("<BR>" + bivcs[i].getBanquev3() + "<BR>");

                    } else {
                        System.out.println("Aucune signature trouvée pour ce compte");
                        lstConverteds = new ArrayList<String>(1);
                        lstConverteds.add("<BR>Aucune signature trouvée pour ce compte");
                        System.out.println("Aucune instruction trouvée pour ce compte");
                        lstInstructions = new ArrayList<String>(1);
                        lstInstructions.add("<BR>Aucune instruction trouvée pour ce compte");
                    }

                }
            } else {
                System.out.println("Aucune signature trouvée pour ce compte");
                lstConverteds = new ArrayList<String>(1);
                lstConverteds.add("<BR>Aucune signature trouvée pour ce compte");
                System.out.println("Aucune instruction trouvée pour ce compte");
                lstInstructions = new ArrayList<String>(1);
                lstInstructions.add("<BR>Aucune instruction trouvée pour ce compte");

            }

            //         lstConverteds.addAll(lstInstructions);
            results = new String[lstConverteds.size()];
            results = lstConverteds.toArray(results);

        } else {
            System.out.println("Aucun compte saisi");
            lstConverteds = new ArrayList<String>(1);
            lstConverteds.add("<BR>Aucun compte saisi");
        }
        return results;

    }

    public static clearing.table.orion.Sibicsig[] getOrionSignaturesModified(String agence, String numeroCompte) {
        clearing.table.orion.Sibicsig[] results = null;
        ArrayList< clearing.table.orion.Sibicsig> lstConverteds = null;
        ArrayList< clearing.table.orion.Sibicsig> lstInstructions = null;

        String strSqlQuery = "SELECT * FROM SIBICSIG ";
        if (agence != null && agence.length() == 5 && numeroCompte != null) {
            String account = numeroCompte;
            String otPath = Utility.getParam("LOCAL_SIGN_FOLDER") + File.separator + agence + File.separator + numeroCompte;
            Utility.createFolderIfItsnt(new File(otPath), null);
            DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
            try {
                dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());
            } catch (Exception ex) {
                Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex);
            }

            String sql = strSqlQuery + " where COMPTE ='" + account + "' and GUICHET ='" + agence + "'";
            clearing.table.orion.Sibicsig[] bivcs = (clearing.table.orion.Sibicsig[]) dbExt.retrieveRowAsObject(sql, new clearing.table.orion.Sibicsig());

            dbExt.close();

            if (bivcs != null && bivcs.length > 0) {
                //converteds = new String[svtm_cif_sig_det.length];
                lstConverteds = new ArrayList< clearing.table.orion.Sibicsig>();
                lstInstructions = new ArrayList< clearing.table.orion.Sibicsig>();

                for (int i = 0; i < bivcs.length; i++) {

                    if (bivcs[i].getChemin() != null) {
                        String name = bivcs[i].getChemin() + File.separator + bivcs[i].getNomimg();
                        try {
                            FileUtils.copyFile(new File(name), new File(otPath + File.separator + bivcs[i].getNomimg()));
                        } catch (Exception ex) {
                            //Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex);
                            if (ex instanceof FileNotFoundException) {
                                name = Utility.getParam("DISTANT_SIGN_FOLDER") + File.separator + bivcs[i].getNomimg();
                                try {
                                    FileUtils.copyFile(new File(name), new File(otPath + File.separator + bivcs[i].getNomimg()));
                                } catch (IOException ex1) {
                                    Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex1);
                                }
                            }
                        }
                        bivcs[i].setNomimg(otPath + File.separator + bivcs[i].getNomimg());
                        bivcs[i].setBanquev3("<BR>" + bivcs[i].getBanquev3() + "</BR>");
                        //     lstConverteds.add(otPath + File.separator + bivcs[i].getNomimg());
                        //   lstInstructions.add("<BR>" + bivcs[i].getBanquev3() + "<BR>");

                        lstConverteds.add(bivcs[i]);
                    } else {
                        System.out.println("Aucune signature trouvée pour ce compte");
                        lstConverteds = new ArrayList< clearing.table.orion.Sibicsig>();
                        Sibicsig sibicsigAucuneSignEtInst = new clearing.table.orion.Sibicsig();
                        sibicsigAucuneSignEtInst.setNomimg("<BR>Aucune signature trouvée pour ce compte");
                        sibicsigAucuneSignEtInst.setBanquev3("<BR>Aucune instruction trouvée pour ce compte");
//                        lstConverteds.add( "<BR>Aucune signature trouvée pour ce compte");
                        lstConverteds.add(sibicsigAucuneSignEtInst);
                        System.out.println("Aucune instruction trouvée pour ce compte");
//                        lstInstructions = new ArrayList<String>(1);
//                        lstInstructions.add("<BR>Aucune instruction trouvée pour ce compte");

                    }

                }
            } else {
                System.out.println("Aucune signature trouvée pour ce compte");
                lstConverteds = new ArrayList< clearing.table.orion.Sibicsig>();
                Sibicsig sibicsigAucuneSignEtInst = new clearing.table.orion.Sibicsig();
                sibicsigAucuneSignEtInst.setNomimg("<BR>Aucune signature trouvée pour ce compte");
                sibicsigAucuneSignEtInst.setBanquev3("<BR>Aucune instruction trouvée pour ce compte");
//                        lstConverteds.add( "<BR>Aucune signature trouvée pour ce compte");
                lstConverteds.add(sibicsigAucuneSignEtInst);

//                lstConverteds = new ArrayList<String>(1);
//                lstConverteds.add("<BR>Aucune signature trouvée pour ce compte");
//                System.out.println("Aucune instruction trouvée pour ce compte");
//                lstInstructions = new ArrayList<String>(1);
//                lstInstructions.add("<BR>Aucune instruction trouvée pour ce compte");
            }

            //         lstConverteds.addAll(lstInstructions);
            //int marks[][]={{50,60,55,67,70},{62,65,70,70,81},{72,66,77,80,69}};
            results = new clearing.table.orion.Sibicsig[lstConverteds.size()];
            results = lstConverteds.toArray(results);

        } else {
            System.out.println("Aucun compte saisi");
            lstConverteds = new ArrayList< clearing.table.orion.Sibicsig>();
            Sibicsig sibicsigAucuneSignEtInst = new clearing.table.orion.Sibicsig();
            sibicsigAucuneSignEtInst.setNomimg("<BR>Aucun compte saisi");
            sibicsigAucuneSignEtInst.setBanquev3("<BR>Aucun compte saisi");
//                        lstConverteds.add( "<BR>Aucune signature trouvée pour ce compte");
            lstConverteds.add(sibicsigAucuneSignEtInst);

//            lstConverteds = new ArrayList<String>(1);
//            lstConverteds.add("<BR>Aucun compte saisi");
        }
        return results;

    }

    public static String getParamDateDefaultValue(String param) {
        String paramDate = Utility.getParam(param);
        if (paramDate != null && paramDate.equalsIgnoreCase("FLEXNEXTWORKINGDAY")) {
            paramDate = getFlexNextWorkingDate();
        }
        return paramDate;
    }

    public static String getFlexNextWorkingDate() {

        DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
        try {
            dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());
        } catch (Exception ex) {
            Logger.getLogger(CMPUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        String sql = "select to_char(next_working_day,'YYYY/MM/DD') as next_working_day from "+ Utility.getParam("FLEXSCHEMA") +".STTM_AEOD_DATES where branch_code ='" + Utility.getParam("TXN_BRANCH") + "' ";
        STTM_AEOD_DATES[] sttm_aeod_dates = (STTM_AEOD_DATES[]) dbExt.retrieveRowAsObject(sql, new STTM_AEOD_DATES());
        String dateValeurJ2 = "";
        if (sttm_aeod_dates != null && sttm_aeod_dates.length > 0) {
            dateValeurJ2 = sttm_aeod_dates[0].getNext_working_day();
        }
        dbExt.close();
        return dateValeurJ2;
    }

    public static Comptes getInfoCompte(String numeroCompte) {
        try {
            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numero like '%" + numeroCompte + "%'", new Comptes());

            if (comptes != null && comptes.length > 0) {
                comptes[0].setNom(comptes[0].getNom().replaceAll("\\p{Punct}", " "));
                if (comptes[0].getPrenom() != null) {
                    comptes[0].setPrenom(comptes[0].getPrenom().replaceAll("\\p{Punct}", " "));
                }
                if (comptes[0].getAdresse1() != null) {
                    comptes[0].setAdresse1(comptes[0].getAdresse1().replaceAll("\\p{Punct}", " "));
                }

                return comptes[0];
            }
            db.close();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static CharSequence getAcctClass(String numCptEx) {
        return numCptEx.subSequence(3, 6);
    }
}
