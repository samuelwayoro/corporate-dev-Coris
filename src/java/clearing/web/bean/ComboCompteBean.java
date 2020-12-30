/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.web.bean;

import org.patware.web.json.bean.ComboBean;
import clearing.model.CMPUtility;
import clearing.table.Comptes;
import java.sql.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.web.json.JSONConverter;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ComboCompteBean extends ComboBean {

    private String requete = "select * from Comptes";

    public String getCompteMessageBean(String numeroCompte) {
        try {
            JSONConverter jsonConverter = new JSONConverter();
            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Comptes[] comptes;
            if (numeroCompte != null && !numeroCompte.isEmpty() && numeroCompte.length() == 9) {
                comptes = (Comptes[]) db.retrieveRowAsObject(requete + " where numero like '" + Utility.bourrageGauche(numeroCompte, 9, "0") + "'", new Comptes());
            } else {
                comptes = (Comptes[]) db.retrieveRowAsObject(requete + " where numero like '" + Utility.bourrageGauche(numeroCompte, 12, "0") + "'", new Comptes());
            }

            if (comptes != null && comptes.length > 0) {
                comptes[0].setNom(comptes[0].getNom().replaceAll("\\p{Punct}", " "));
                if (comptes[0].getPrenom() != null) {
                    comptes[0].setPrenom(comptes[0].getPrenom().replaceAll("\\p{Punct}", " "));
                }

                //Insertion dans le bean
                CompteMessageBean compteMessageBean = new CompteMessageBean();
                compteMessageBean.setNumeroCompte(comptes[0].getNumero());
                compteMessageBean.setNomClient(Utility.bourrageDroite(comptes[0].getNom().trim(), 35, " "));

                compteMessageBean.setAdresseCompte(comptes[0].getAdresse1());
                compteMessageBean.setEscompte(comptes[0].getEtat().toString());

                db.close();

                return jsonConverter.objectToJSONStringArray(compteMessageBean);

            }
            db.close();
            return "rien";

        } catch (Exception ex) {
            Logger.getLogger(ComboIdBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getInfoCompteLite(String numeroCompte, String etablissement) {
        try {
            JSONConverter jsonConverter = new JSONConverter();
            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject(requete + " where numero like '" + Utility.bourrageGauche(numeroCompte, 12, "0") + "' and adresse1 like '" + etablissement + "'", new Comptes());

            if (comptes != null && comptes.length > 0) {
                comptes[0].setNom(comptes[0].getNom().replaceAll("\\p{Punct}", " "));
                if (comptes[0].getPrenom() != null) {
                    comptes[0].setPrenom(comptes[0].getPrenom().replaceAll("\\p{Punct}", " "));
                }

                db.close();

                return jsonConverter.objectToJSONStringArray(comptes[0]);

            }
            db.close();
            return "rien";

        } catch (Exception ex) {
            Logger.getLogger(ComboIdBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getInfoCompte(String agence, String numeroCompte) {
        try {
            JSONConverter jsonConverter = new JSONConverter();
            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject(requete + " where numero like '" + Utility.bourrageGauche(numeroCompte, 12, "0") + "' and agence like '" + agence + "'", new Comptes());

            if (comptes != null && comptes.length > 0) {
                comptes[0].setNom(comptes[0].getNom().replaceAll("\\p{Punct}", " "));
                if (comptes[0].getPrenom() != null) {
                    comptes[0].setPrenom(comptes[0].getPrenom().replaceAll("\\p{Punct}", " "));
                }

                //Insertion dans le bean
                CompteMessageBean compteMessageBean = new CompteMessageBean();
                compteMessageBean.setNumeroCompte(comptes[0].getNumero());
                compteMessageBean.setNomClient(Utility.bourrageDroite(comptes[0].getNom().trim(), 35, " "));

                compteMessageBean.setAdresseCompte(comptes[0].getAdresse1());
                compteMessageBean.setEscompte(comptes[0].getEtat().toString());
                compteMessageBean.setCleRib(Utility.computeCleRIB(CMPUtility.getCodeBanqueSica3(), agence, numeroCompte));

                db.close();

                return jsonConverter.objectToJSONStringArray(compteMessageBean);

            }
            db.close();
            return "rien";

        } catch (Exception ex) {
            Logger.getLogger(ComboIdBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getInfoCompte(String numeroCompte) {
        try {
            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
//            db.executeUpdate("UPDATE COMPTES SET ETAT=0 WHERE DATEFINESCOMPTE<='" +
//                    Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")) + "'");

            //Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject(requete +" where numero ='"+Utility.bourrageGZero(numeroCompte, 12)+"'", new Comptes());
            //BIAO
            Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject(requete + " where numero like '" + numeroCompte + "'", new Comptes());

            if (comptes != null && comptes.length > 0) {
                comptes[0].setNom(comptes[0].getNom().replaceAll("\\p{Punct}", " "));
                if (comptes[0].getPrenom() != null) {
                    comptes[0].setPrenom(comptes[0].getPrenom().replaceAll("\\p{Punct}", " "));
                }
                if (comptes[0].getAdresse1() != null) {
                    comptes[0].setAdresse1(comptes[0].getAdresse1().replaceAll("\\p{Punct}", " "));
                }

                JSONConverter jsonConverter = new JSONConverter();
                return jsonConverter.objectToJSONStringArray(comptes[0]);
            }
            if (CMPUtility.getCodeBanqueSica3().equalsIgnoreCase("BF022")) {

                comptes = (Comptes[]) db.retrieveRowAsObject(requete + " where ville  ='" + Utility.bourrageGZero(numeroCompte, 12) + "'", new Comptes());

                if (comptes != null && comptes.length > 0) {
                    comptes[0].setNom(comptes[0].getNom().replaceAll("\\p{Punct}", " "));

                    if (comptes[0].getPrenom() != null) {
                        comptes[0].setPrenom(comptes[0].getPrenom().replaceAll("\\p{Punct}", " "));
                    }
                    if (comptes[0].getAdresse1() != null) {
                        comptes[0].setAdresse1(comptes[0].getAdresse1().replaceAll("\\p{Punct}", " "));
                    }

                    JSONConverter jsonConverter = new JSONConverter();
                    return jsonConverter.objectToJSONStringArray(comptes[0]);
                }
            }
            if (Utility.getParam("NUMCPTEX") != null && Utility.getParam("NUMCPTEX").equalsIgnoreCase("1")) {

                comptes = (Comptes[]) db.retrieveRowAsObject(requete + " where NUMCPTEX  ='" + numeroCompte + "'", new Comptes());

                if (comptes != null && comptes.length > 0) {
                    comptes[0].setNom(comptes[0].getNom().replaceAll("\\p{Punct}", " "));

                    if (comptes[0].getPrenom() != null) {
                        comptes[0].setPrenom(comptes[0].getPrenom().replaceAll("\\p{Punct}", " "));
                    }
                    if (comptes[0].getAdresse1() != null) {
                        comptes[0].setAdresse1(comptes[0].getAdresse1().replaceAll("\\p{Punct}", " "));
                    }

                    JSONConverter jsonConverter = new JSONConverter();
                    return jsonConverter.objectToJSONStringArray(comptes[0]);
                }
            }
            db.close();
            return "rien";
            //return getComboLiteral(new String[]{comptes[0].getNom()}, new String[]{comptes[0].getAgence()}).toString();

        } catch (Exception ex) {
            Logger.getLogger(ComboIdBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Comptes getCompte(String numeroCompte) {
        try {
            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            db.executeUpdate("UPDATE COMPTES SET ETAT=0 WHERE DATEFINESCOMPTE<='"
                    + Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")) + "'");

            //Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject(requete +" where numero ='"+Utility.bourrageGZero(numeroCompte, 12)+"'", new Comptes());
            //BIAO
            Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject(requete + " where numero like '" + numeroCompte + "'", new Comptes());

            if (comptes != null && comptes.length > 0) {
                comptes[0].setNom(comptes[0].getNom().replaceAll("\\p{Punct}", " "));
                if (comptes[0].getPrenom() != null) {
                    comptes[0].setPrenom(comptes[0].getPrenom().replaceAll("\\p{Punct}", " "));
                }
                if (comptes[0].getAdresse1() != null) {
                    comptes[0].setAdresse1(comptes[0].getAdresse1().replaceAll("\\p{Punct}", " "));
                }

                return (comptes[0]);
            }

        } catch (Exception ex) {
            Logger.getLogger(ComboIdBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getComboDatas() {
        try {

            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject(requete, new Comptes());
            db.close();
            String[] labels = null;
            String[] values = null;
            if (comptes != null && comptes.length > 0) {
                labels = new String[comptes.length];
                values = new String[comptes.length];
                for (int i = 0; i < comptes.length; i++) {
                    labels[i] = comptes[i].getNumero();
                    values[i] = comptes[i].getNumero();
                }

                return getComboLiteral(labels, values).toString();
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(ComboCompteBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getComboAgenceDatas(String numeroCompte) {
        try {
            String[] labels = null;
            String[] values = null;
            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

             Comptes[] comptes;
            if (numeroCompte != null && !numeroCompte.isEmpty() && numeroCompte.length() == 9) {
                comptes = (Comptes[]) db.retrieveRowAsObject(requete + " where numero like '" + Utility.bourrageGauche(numeroCompte, 9, "0") + "'", new Comptes());
            } else {
                comptes = (Comptes[]) db.retrieveRowAsObject(requete + " where numero like '" + Utility.bourrageGauche(numeroCompte, 12, "0") + "'", new Comptes());
            }
            
//            Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject(requete + " where numero like '" + Utility.bourrageGauche(numeroCompte, 12, "0") + "'", new Comptes());

            if (comptes != null && comptes.length > 0) {
                labels = new String[comptes.length];
                values = new String[comptes.length];
                for (int i = 0; i < comptes.length; i++) {
                    labels[i] = "AGENCE " + comptes[i].getAgence();
                    values[i] = "A" + comptes[i].getAgence();
                }
                db.close();
                return getComboLiteral(labels, values).toString();
            }
            db.close();
        } catch (Exception ex) {
            Logger.getLogger(ComboCompteBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public ComboCompteBean() {
    }
}
