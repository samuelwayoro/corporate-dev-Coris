/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.bfi;


import clearing.table.Virements;
import org.patware.action.file.FlatFileReader;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;

import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class RCPVirementRetourReader extends FlatFileReader {

    public RCPVirementRetourReader() {
        setHasNormalExtension(false);
         setExtensionType(END_EXT);
    }

  

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;
        String sql = null;
        String typeOperation = null;
        Virements virement = new Virements();
       
        

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        int compteur = 0;
        BufferedReader is = openFile(aFile);
        while ((line = is.readLine()) != null) {
            setCurrentLine(line);
            compteur++;
            if(compteur != 1){
                    getChamp(1);
                    getChamp(2);
                    getChamp(2);
                    getChamp(8);
                    getChamp(6);
                    virement.setType_Virement(getChamp(2));
                    virement.setBanqueremettant(getChamp(3));
                    virement.setDatecompensation(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "ddMMyyyy"), ResLoader.getMessages("patternDate")));
                    getChamp(8);
                    virement.setRemise(new BigDecimal(getChamp(7))) ;
                    typeOperation = getChamp(2);
                    getChamp(3);
                    virement.setDevise("GNF");
                    getChamp(3);
                    virement.setMontantvirement(String.valueOf(Long.parseLong(getChamp(15))));
                    //getChamp(2);//Decimal 00
                    virement.setNumerovirement(getChamp(7));
                    virement.setAgenceremettant(getChamp(3));
                    getChamp(8);
                    virement.setBanqueremettant(getChamp(3));
                    virement.setAgenceremettant(getChamp(3));
                    virement.setNumerocompte_Tire(getChamp(10));
                    getChamp(2);
                    virement.setNom_Tire(Utility.removeAccent(getChamp(30)));
                    virement.setAdresse_Tire(Utility.removeAccent(getChamp(30)));
                    virement.setBanque(getChamp(3));
                    getChamp(2);
                    virement.setBanque(getChamp(3));
                    virement.setAgence(getChamp(3));
                    virement.setNumerocompte_Beneficiaire(getChamp(10));
                    getChamp(2);
                    virement.setNom_Beneficiaire(Utility.removeAccent(getChamp(30)));
                    virement.setAdresse_Beneficiaire(Utility.removeAccent(getChamp(30)));
                    virement.setReference_Emetteur(Utility.removeAccent(getChamp(20)));
                    getChamp(2);
                    virement.setLibelle(getChamp(45));
                    getChamp(15);
                    virement.setDateordre(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "ddMMyyyy"), ResLoader.getMessages("patternDate")));
                    getChamp(41);
                    virement.setEtablissement(Utility.getParam("CODE_BANQUE"));
                    virement.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));

                    if (typeOperation.equals("21")) {
                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPERET")));
                    virement.setIdvirement(new BigDecimal(Utility.computeCompteur("IDVIREMENT", "VIREMENTS")));
                    virement.setDatecompensation(virement.getDatecompensation());
                    db.insertObjectAsRowByQuery(virement, "VIREMENTS");
                    }
                    if (typeOperation.equals("24")) {
                    sql = "SELECT * FROM VIREMENTS WHERE REMISE=" + virement.getRemise()+
                            "  AND BANQUE ='" + virement.getBanque() +
                            "' AND NUMEROVIREMENT ='" + virement.getNumerovirement() +
                            "' AND NUMEROCOMPTE_TIRE ='" + virement.getNumerocompte_Tire() +
                            "' AND MONTANTVIREMENT ='" + virement.getMontantvirement() + "' AND ETAT =" + Utility.getParam("CETAOPEREJRET") +
                            "";
                    Virements[] virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());

                    if (virements != null && virements.length > 0) {
                        virements[0].setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPEALLICOM1ACC"))));
                        sql = " IDVIREMENT =" + virements[0].getIdvirement();
                        db.updateRowByObjectByQuery(virements[0], "VIREMENTS", sql);
                    }
                }

                       if (typeOperation.equals("22")) {
                    sql = "SELECT * FROM VIREMENTS WHERE  REMISE=" + virement.getRemise()+
                            " AND BANQUE ='" + virement.getBanque() +
                            "' AND NUMEROVIREMENT ='" + virement.getNumerovirement() +
                            "' AND NUMEROCOMPTE_TIRE ='" + virement.getNumerocompte_Tire() +
                            "' AND MONTANTVIREMENT ='" + virement.getMontantvirement() + "' AND ETAT =" + Utility.getParam("CETAOPEALLICOM1ACC") +
                            "";
                    Virements[] virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());

                    if (virements != null && virements.length > 0) {
                        virements[0].setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPEREJRET"))));
                        sql = " IDVIREMENT =" + virements[0].getIdvirement();
                        db.updateRowByObjectByQuery(virements[0], "VIREMENTS", sql);
                    }
                }

                     if (typeOperation.equals("23")) {
                    sql = "SELECT * FROM VIREMENTS WHERE REMISE=" + virement.getRemise()+
                            "  AND BANQUE ='" + virement.getBanque() +
                            "' AND NUMEROVIREMENT ='" + virement.getNumerovirement() +
                            "' AND NUMEROCOMPTE_TIRE ='" + virement.getNumerocompte_Tire() +
                            "' AND MONTANTVIREMENT ='" + virement.getMontantvirement() + "' AND ETAT =" + Utility.getParam("CETAOPERET") +
                            "";
                    Virements[] virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());

                    if (virements != null && virements.length > 0) {
                        virements[0].setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPEERR"))));
                        sql = " IDVIREMENT =" + virements[0].getIdvirement();
                        db.updateRowByObjectByQuery(virements[0], "VIREMENTS", sql);
                    }
                }



            }
                    
                }

            
        
        db.close();

       
        return aFile;
    }
}
