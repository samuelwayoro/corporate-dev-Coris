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
public class ComboComptePFBean extends ComboBean {

    private String requete = "select substr(cpt.pcb_numero24,6,5) agence,substr(cpt.pcb_numero24,11,12) compte,substr(cpt.pcb_numero24,23,2) clerib," +
" substr(cpt.CPTCLI_INTITULE,1,35) nom,''typecpt,''service " +
" from v_compteclient cpt, profilcompte pcpt ,client cli, entite ent " +
" where cpt.CPTCLI_PROD_ID = '541' " +
" and cpt.CPTCLI_ETAT_ID = 3 " +
" and cpt.pcb_entite_id = ent.entit_id" +
" and pcpt.profilcpt_compte_id =cpt.cptcli_id " +
" and pcpt.profilcpt_client_id = cli.client_id" +
" and cli.client_categorieclient_id not in (322,329)" +
" and pcpt.profilcpt_droit_id = 1";
    
    

    public String getInfoCompte(String numeroCompte) {
        try {
            JSONConverter jsonConverter = new JSONConverter();
            DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
            dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());

             if(Utility.getParamLabel("DELTACPTQUERY") !=null){
                 requete = Utility.getParamLabel("DELTACPTQUERY");
             }

            Bkcom[] bkcom = (Bkcom[]) dbExt.retrieveRowAsObject(requete + " and substr(cpt.pcb_numero24,11,12)  like '" + Utility.bourrageGauche(numeroCompte, 12, "0") + "'", new Bkcom());


            if (bkcom != null && bkcom.length > 0) {
                bkcom[0].setNom(bkcom[0].getNom().replaceAll("\\p{Punct}", " "));
                

         


                //Insertion dans le bean
                CompteMessageBean compteMessageBean = new CompteMessageBean();
                compteMessageBean.setNumeroCompte(bkcom[0].getCompte());
                compteMessageBean.setNomClient(Utility.bourrageDroite(bkcom[0].getNom().trim(), 35, " "));

                compteMessageBean.setAdresseCompte("");
              
                compteMessageBean.setAgence(bkcom[0].getAgence());
                compteMessageBean.setEscompte("0");
                 

               
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

    

    public ComboComptePFBean() {
    }
}
