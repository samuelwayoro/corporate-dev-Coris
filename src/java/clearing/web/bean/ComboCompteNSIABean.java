/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.web.bean;

import clearing.table.ComptesNsia;
import clearing.table.delta.Icsibtc1;
import clearing.table.delta.Icsibtc2;
import clearing.table.delta.SibIccptNSIA;
import clearing.table.delta.Sibiccpt;
import org.patware.web.json.bean.ComboBean;
import java.math.BigDecimal;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.web.json.JSONConverter;
import org.patware.xml.ExtJDBCXmlReader;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ComboCompteNSIABean extends ComboBean {

//    private String requete = "select x0.age AGENCE, x0.ser SERVICE,x0.ncp COMPTE,SUBSTR(x0.inti,1,30) NOM,x0.ribdec CLERIB,x0.CPRO TYPECPT from bkcom x0 where x0.cfe='N'"
//            + " and x0.ife='N' and x0.dev='001' and cpro is not null ";
//     
    private String requete = "select * from sibiccpt";
    private String[] codeAgence;

    public String recupInfoCompte(String numeroCompte) {
        try {
            JSONConverter jsonConverter = new JSONConverter();
//            DataBase db = new DataBase(JDBCXmlReader.getDriver());
//            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
            dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());

            SibIccptNSIA[] sibiccpt = (SibIccptNSIA[]) dbExt.retrieveRowAsObject(requete + " where ncp like '" + Utility.bourrageGauche(numeroCompte, 11, "0") + "'", new SibIccptNSIA());

            if (sibiccpt != null && sibiccpt.length > 0) {
                sibiccpt[0].setNom(sibiccpt[0].getNom().replaceAll("\\p{Punct}", " "));

                // if(sibiccpt[0].get!= null) comptes[0].setAdresse1(comptes[0].getAdresse1().replaceAll("\\p{Punct}", " "));
                //Insertion dans le bean
                CompteMessageBean compteMessageBean = new CompteMessageBean();
                compteMessageBean.setNumeroCompte(sibiccpt[0].getCompte());
                compteMessageBean.setNomClient(Utility.bourrageDroite(sibiccpt[0].getNom().trim(), 35, " "));
                compteMessageBean.setAgence("" + sibiccpt[0].getAgence());
                //      compteMessageBean.setAdresseCompte(""+sibiccpt[0]);
                dbExt.close();
//                dbExt.close();

                return jsonConverter.objectToJSONStringArray(compteMessageBean);

            }
            dbExt.close();
//            dbExt.close();
            return "rien";
            //return getComboLiteral(new String[]{comptes[0].getNom()}, new String[]{comptes[0].getAgence()}).toString();

        } catch (Exception ex) {
            Logger.getLogger(ComboIdBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getInfoCompte(String numeroCompte) {
        Date today = Utility.convertStringToDate(Utility.convertDateToString(new Date(), ResLoader.getMessages("patternDate")), ResLoader.getMessages("patternDate"));
        try {

            JSONConverter jsonConverter = new JSONConverter();

            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
            dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());

            SibIccptNSIA[] sibiccpt = (SibIccptNSIA[]) dbExt.retrieveRowAsObject(requete + " where ncp like '" + Utility.bourrageGauche(numeroCompte, 11, "0") + "'", new SibIccptNSIA());

            ComptesNsia[] comptes = (ComptesNsia[]) db.retrieveRowAsObject("SELECT * from ComptesNSIA  where numero like '" + numeroCompte + "'", new ComptesNsia());

            if (sibiccpt != null && sibiccpt.length > 0) {
                sibiccpt[0].setNom(sibiccpt[0].getNom().replaceAll("\\p{Punct}", " "));

                // if(sibiccpt[0].get!= null) comptes[0].setAdresse1(comptes[0].getAdresse1().replaceAll("\\p{Punct}", " "));
                //Insertion dans le bean
                CompteMessageBean compteMessageBean = new CompteMessageBean();
                compteMessageBean.setNumeroCompte(sibiccpt[0].getCompte());
                compteMessageBean.setNomClient(Utility.bourrageDroite(sibiccpt[0].getNom(), 35, " "));
                compteMessageBean.setAgence("" + sibiccpt[0].getAgence());
                //      compteMessageBean.setAdresseCompte(""+sibiccpt[0]);
                if (comptes != null && comptes.length > 0) {
                    if (((Utility.convertStringToDate(comptes[0].getDateDebutEscompte(), ResLoader.getMessages("patternDate")).before(today)
                            || Utility.convertStringToDate(comptes[0].getDateDebutEscompte(), ResLoader.getMessages("patternDate")).equals(today))
                            && (Utility.convertStringToDate(comptes[0].getDateFinEscompte(), ResLoader.getMessages("patternDate")).after(today)
                            || Utility.convertStringToDate(comptes[0].getDateFinEscompte(), ResLoader.getMessages("patternDate")).equals(today)))
                            && comptes[0].getEtat() != null && comptes[0].getEtat().equals(new BigDecimal("1"))) {

                        compteMessageBean.setEscompte("" + comptes[0].getEtat());
                        System.out.println("compte " + numeroCompte + " escompté");
                    } else {
                        compteMessageBean.setEscompte("0");
                        System.out.println("compte " + numeroCompte + " Non escompté");
                    }

                } else {
                    compteMessageBean.setEscompte("0");
                }
                dbExt.close();
                db.close();
                return jsonConverter.objectToJSONStringArray(compteMessageBean);

            } else {
                
                System.out.println("Pas de comptes trouvés");
                return "rien";

            }

        } catch (Exception ex) {
            Logger.getLogger(ComboIdBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
  public String getComboAgenceDatas(String numeroCompte) {
        try {
            String[] labels = null;
            String[] values = null;
            DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
            dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());

            SibIccptNSIA[] sibiccpt = (SibIccptNSIA[]) dbExt.retrieveRowAsObject(requete + " where ncp like  '" + Utility.bourrageGauche(numeroCompte, 11, "0") + "'", new SibIccptNSIA());


            if (sibiccpt != null && sibiccpt.length > 0) {
                labels = new String[sibiccpt.length];
                values = new String[sibiccpt.length];
                for (int i = 0; i < sibiccpt.length; i++) {
                    labels[i] = sibiccpt[i].getAgence();
                    values[i] = "A" + sibiccpt[i].getAgence();
                }
                dbExt.close();
                return getComboLiteral(labels, values).toString();
            }
            dbExt.close();
        } catch (Exception ex) {
            Logger.getLogger(ComboCompteBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
     public String getInfoCompte( String agence, String numeroCompte) {
        Date today = Utility.convertStringToDate(Utility.convertDateToString(new Date(), ResLoader.getMessages("patternDate")), ResLoader.getMessages("patternDate"));
        try {

            JSONConverter jsonConverter = new JSONConverter();

            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
            dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());

            SibIccptNSIA[] sibiccpt = (SibIccptNSIA[]) dbExt.retrieveRowAsObject(requete + " where ncp like '" + Utility.bourrageGauche(numeroCompte, 11, "0") + "' and agence like '"+ agence +"'", new SibIccptNSIA());  

            ComptesNsia[] comptes = (ComptesNsia[]) db.retrieveRowAsObject("SELECT * from ComptesNSIA  where numero like '" + numeroCompte + "' and agence like '"+ agence +"'", new ComptesNsia());

            if (sibiccpt != null && sibiccpt.length > 0) {
                sibiccpt[0].setNom(sibiccpt[0].getNom().replaceAll("\\p{Punct}", " "));

                // if(sibiccpt[0].get!= null) comptes[0].setAdresse1(comptes[0].getAdresse1().replaceAll("\\p{Punct}", " "));
                //Insertion dans le bean
                CompteMessageBean compteMessageBean = new CompteMessageBean();
                compteMessageBean.setNumeroCompte(sibiccpt[0].getCompte());
                compteMessageBean.setNomClient(Utility.bourrageDroite(sibiccpt[0].getNom(), 35, " "));
                compteMessageBean.setAgence("" + sibiccpt[0].getAgence());
                //      compteMessageBean.setAdresseCompte(""+sibiccpt[0]);
                if (comptes != null && comptes.length > 0) {
                    if (((Utility.convertStringToDate(comptes[0].getDateDebutEscompte(), ResLoader.getMessages("patternDate")).before(today)
                            || Utility.convertStringToDate(comptes[0].getDateDebutEscompte(), ResLoader.getMessages("patternDate")).equals(today))
                            && (Utility.convertStringToDate(comptes[0].getDateFinEscompte(), ResLoader.getMessages("patternDate")).after(today)
                            || Utility.convertStringToDate(comptes[0].getDateFinEscompte(), ResLoader.getMessages("patternDate")).equals(today)))
                            && comptes[0].getEtat() != null && comptes[0].getEtat().equals(new BigDecimal("1"))) {

                        compteMessageBean.setEscompte("" + comptes[0].getEtat());
                        System.out.println("compte " + numeroCompte + " escompté");
                    } else {
                        compteMessageBean.setEscompte("0");
                        System.out.println("compte " + numeroCompte + " Non escompté");
                    }

                } else {
                    compteMessageBean.setEscompte("0");
                }
                dbExt.close();
                db.close();
                return jsonConverter.objectToJSONStringArray(compteMessageBean);

            } else {
                System.out.println("Pas de comptes trouvés");
                return "rien";

            }

        } catch (Exception ex) {
            Logger.getLogger(ComboIdBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    public ComboCompteNSIABean() {
    }
}
