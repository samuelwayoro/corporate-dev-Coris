/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.orion;

import clearing.model.CMPUtility;
import clearing.table.Effets;
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
public class VirementEffetNSIAReader extends FlatFileReader  {

    private String codeOperation;

    
    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;

        Virements virement = new Virements();
        Effets effet = new Effets();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        BufferedReader is = openFile(aFile);
        while ((line = is.readLine()) != null) {
            setCurrentLine(line);
            
                getChamp(24);
                String reference = getChamp(7);
                getChamp(9);

                codeOperation = getChamp(3);
                if (codeOperation.equals("051")) {
                    effet.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                    getChamp(8);
                    getChamp(8);
                    getChamp(4);

                    effet.setBanqueremettant(getChamp(5));
                    effet.setAgenceremettant(getChamp(5));
                    effet.setNumerocompte_Beneficiaire(Utility.bourrageGZero(getChamp(12), 12));
                    getChamp(6);
                    effet.setBanque(getChamp(5));
                    effet.setAgence(getChamp(5));
                    effet.setNumerocompte_Tire(Utility.bourrageGZero(getChamp(12), 12));
                    getChamp(2);
                    effet.setDevise(getChamp(3));
                    effet.setMontant_Effet(String.valueOf(Long.parseLong(getChamp(16))));
                    getChamp(16);//decimales
                    getChamp(7);
                    effet.setDatesaisie(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"), ResLoader.getMessages("patternDate")));
                    getChamp(76);
                    effet.setDate_Creation(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"), ResLoader.getMessages("patternDate")));
                    effet.setDate_Echeance(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"), ResLoader.getMessages("patternDate")));
                    getChamp(117);
                    effet.setCode_Acceptation(getChamp(1));
                    getChamp(62);
                    effet.setNom_Beneficiaire(getChamp(35));
                    getChamp(100);
                    effet.setNom_Tire(getChamp(35));
                    effet.setNumeroeffet(reference);
                    effet.setIdentification_Tire(getChamp(12));
                    
                    
                    effet.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
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

                    db.insertObjectAsRowByQuery(effet, "EFFETS");


                }

                if (codeOperation.equals("030")) {
                    virement.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                    getChamp(8);
                    getChamp(8);
                    getChamp(4);
                    getChamp(2);
                    virement.setBanque(getChamp(3));
                    getChamp(2);
                    virement.setAgence(getChamp(3));
                    virement.setNumerocompte_Beneficiaire(Utility.bourrageGZero(getChamp(12), 12));
                    getChamp(2);
                    getChamp(4);
                    virement.setBanqueremettant(getChamp(5));
                    virement.setAgenceremettant(getChamp(5));
                    virement.setNumerocompte_Tire(Utility.bourrageGZero(getChamp(12), 12));
                    getChamp(2);
                    virement.setDevise(getChamp(3));
                    getChamp(16);
                    virement.setMontantvirement(String.valueOf(Long.parseLong(getChamp(16))));
                    getChamp(7);
                    virement.setDateordre(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"), ResLoader.getMessages("patternDate")));
                    
                    getChamp(272);
                    virement.setNom_Beneficiaire(getChamp(35));
                    getChamp(100);
                    virement.setNom_Tire(getChamp(35));
                    virement.setLibelle(getChamp(50));
                    virement.setNumerovirement(Utility.bourrageGZero(reference, 10));
                    //virement.setReference(Utility.bourrageGZero(reference, 10));
                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
                  
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
                   
                    db.insertObjectAsRowByQuery(virement, "VIREMENTS");
                }

            
        }
        db.close();
        return aFile;
    }
}
