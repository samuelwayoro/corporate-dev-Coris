/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.web.bean;

import clearing.table.delta.Bkcom;
import org.patware.web.json.bean.ComboBean;
import clearing.table.delta.Icsibtc1;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.web.json.JSONConverter;
import org.patware.xml.ExtJDBCXmlReader;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ComboCompteCBAOBean extends ComboBean {

    private String requete = "select x0.age AGENCE, x0.ser SERVICE,x0.ncp COMPTE,SUBSTR(x0.inti,1,30) NOM,x0.ribdec CLERIB,x0.CPRO TYPECPT from bkcom x0 where x0.cfe='N'"
            + " and x0.ife='N' and x0.dev='001' and cpro is not null ";
     private String requete2 = " and ncp like '";
    private String[] codeAgence;

    public String getInfoCompte(String numeroCompte) {
        try {
            JSONConverter jsonConverter = new JSONConverter();
            DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
            dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());

            if (Utility.getParamLabel("DELTACPTQUERY") != null) {
                requete = Utility.getParamLabel("DELTACPTQUERY");
            }
            
             if(Utility.getParamLabel("DELTACPTQUERY2") !=null){
                 requete2 = Utility.getParam("DELTACPTQUERY2");
             }

            Bkcom[] bkcom = (Bkcom[]) dbExt.retrieveRowAsObject(requete + " "+ requete2 + Utility.bourrageGauche(numeroCompte, 11, "0") + "'", new Bkcom());

            if (bkcom != null && bkcom.length > 0) {
                bkcom[0].setNom(bkcom[0].getNom().replaceAll("\\p{Punct}", " "));

                //Insertion dans le bean
                CompteMessageBean compteMessageBean = new CompteMessageBean();
                compteMessageBean.setNumeroCompte(bkcom[0].getCompte());
                compteMessageBean.setNomClient(Utility.bourrageDroite(bkcom[0].getNom().trim(), 35, " "));

                compteMessageBean.setAdresseCompte("");
                compteMessageBean.setEscompte(Utility.getParam("LCE_SBF").trim());

                compteMessageBean.setAgence(bkcom[0].getAgence());

                //Test de validité sur type de compte
                DataBase db = new DataBase(JDBCXmlReader.getDriver());
                db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

                Icsibtc1[] icsibtc1 = (Icsibtc1[]) db.retrieveRowAsObject("SELECT * FROM Icsibtc1 WHERE TYPECPT ='" + bkcom[0].getTypecpt() + "'", new Icsibtc1());
                if (icsibtc1 != null && icsibtc1.length > 0) {
                    switch (icsibtc1[0].getActions().intValue()) {
                        case 0: {
                            //On ne fait rien
                            compteMessageBean.setAction("0");
                            compteMessageBean.setMessageResult("");

                        }
                        break;
                        case 1: {
                            //On affiche un message non bloquant
                            compteMessageBean.setAction("1");
                            compteMessageBean.setMessageResult(icsibtc1[0].getLibelle());

                        }
                        break;
                        case 9: {
                            //On affiche un message bloquant
                            compteMessageBean.setAction("9");
                            compteMessageBean.setMessageResult(icsibtc1[0].getLibelle());

                        }
                        break;
                        default: {
                            compteMessageBean.setAction("9");
                            compteMessageBean.setMessageResult("ACTION " + icsibtc1[0].getActions() + " INCORRECTE POUR TYPE COMPTE " + bkcom[0].getTypecpt());

                        }
                    }

                } else {
                    compteMessageBean.setAction("9");
                    compteMessageBean.setMessageResult("TYPE COMPTE INEXISTANT =" + bkcom[0].getTypecpt());

                }

                db.close();
                dbExt.close();

                return jsonConverter.objectToJSONStringArray(compteMessageBean);

            }
            dbExt.close();
            return "rien";
            //return getComboLiteral(new String[]{comptes[0].getNom()}, new String[]{comptes[0].getAgence()}).toString();

        } catch (Exception ex) {
            Logger.getLogger(ComboIdBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public ComboCompteCBAOBean() {
    }
}
