/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.banks;

import clearing.model.CMPUtility;
import clearing.table.Effets;
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
public class EffetAllerBanksReader extends FlatFileReader  {

    private String codeOperation;

    
    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;

        
        Effets effet = new Effets();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        BufferedReader is = openFile(aFile);
        while ((line = is.readLine()) != null) {
            setCurrentLine(line);
           
                codeOperation = getChamp(3);
                if (codeOperation.equals("060")) {
                    getChamp(18);
                    getChamp(1);
                    effet.setBanque(getChamp(5));
                    effet.setAgence(getChamp(5));
                    effet.setNumerocompte_Tire(Utility.bourrageGZero(getChamp(12), 12));
                    getChamp(2);
                     effet.setNom_Tire(getChamp(50));
                    effet.setAdresse_Tire(getChamp(70));

                    effet.setBanqueremettant(getChamp(5));
                    effet.setAgenceremettant(getChamp(5));
                    effet.setNumerocompte_Beneficiaire(Utility.bourrageGZero(getChamp(12), 12));
                    getChamp(2);
                    effet.setNom_Beneficiaire(getChamp(50));
                    effet.setAdresse_Beneficiaire(getChamp(70));
                    getChamp(5);
                    getChamp(146);
                    effet.setMontant_Brut(String.valueOf(Long.parseLong(getChamp(13))));
                    getChamp(2);//decimales
                    effet.setMontant_Effet(String.valueOf(Long.parseLong(getChamp(13))));
                    getChamp(2);//decimales
                    effet.setMontant_Frais(String.valueOf(Long.parseLong(getChamp(13))));
                    getChamp(2);//decimales
                    effet.setNumeroeffet(getChamp(7));
                    effet.setDate_Echeance(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"), ResLoader.getMessages("patternDate")));
                    effet.setDate_Creation(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"), ResLoader.getMessages("patternDate")));
                    effet.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                    effet.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                    effet.setCode_Acceptation(getChamp(1));
                    effet.setIdentification_Tire(getChamp(70));
                    getChamp(49);
                    effet.setDevise("XOF");
                   
                    
                    
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

                if (codeOperation.equals("061")) {
                    getChamp(18);
                    getChamp(1);
                    effet.setBanque(getChamp(5));
                    effet.setAgence(getChamp(5));
                    effet.setNumerocompte_Tire(Utility.bourrageGZero(getChamp(12), 12));
                    getChamp(2);
                    effet.setNom_Tire(getChamp(50));
                    effet.setAdresse_Tire(getChamp(70));
                    getChamp(5);
                    getChamp(1);
                    getChamp(1);
                    effet.setBanqueremettant(getChamp(5));
                    effet.setAgenceremettant(getChamp(5));
                    effet.setNumerocompte_Beneficiaire(Utility.bourrageGZero(getChamp(12), 12));
                    getChamp(2);
                    effet.setNom_Beneficiaire(getChamp(50));
                    effet.setAdresse_Beneficiaire(getChamp(70));
                    
                    effet.setMontant_Effet(String.valueOf(Long.parseLong(getChamp(13))));
                    getChamp(2);//decimales

                    effet.setNumeroeffet(getChamp(7));
                    effet.setDate_Echeance(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"), ResLoader.getMessages("patternDate")));
                    effet.setDate_Creation(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"), ResLoader.getMessages("patternDate")));
                    effet.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                    effet.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));

                    effet.setIdentification_Tire(getChamp(70));
                    getChamp(225);
                    effet.setDevise("XOF");



                    effet.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
                    if (Character.isDigit(effet.getBanque().charAt(1))) {
                        effet.setEtablissement(CMPUtility.getCodeBanque());
                        effet.setBanqueremettant(CMPUtility.getCodeBanque());
                        effet.setType_Effet("042");
                    } else {
                        effet.setEtablissement(CMPUtility.getCodeBanqueSica3());
                        effet.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                        effet.setType_Effet("045");
                    }
                    effet.setVille("01");

                    effet.setIdeffet(new BigDecimal(Utility.computeCompteur("IDEFFET", "EFFETS")));

                    db.insertObjectAsRowByQuery(effet, "EFFETS");

                }

            
        }
        db.close();
        return aFile;
    }
}
