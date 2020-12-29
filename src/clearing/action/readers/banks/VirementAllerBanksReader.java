/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.banks;

import clearing.model.CMPUtility;
import clearing.table.Virements;
import org.patware.action.file.FlatFileReader;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class VirementAllerBanksReader extends FlatFileReader  {

    private String codeOperation;

    public VirementAllerBanksReader() {
        setHasNormalExtension(false);
        setExtensionType(END_EXT);
    }



    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;

        
        Virements virement = new Virements();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        BufferedReader is = openFile(aFile);
        while ((line = is.readLine()) != null) {
            setCurrentLine(line);
           
                codeOperation = getChamp(3);
                 if (codeOperation.equals("010")) {
                    getChamp(1);
                    getChamp(18);
                    getChamp(1);
                    virement.setBanqueremettant(getChamp(5));
                    virement.setAgenceremettant(getChamp(5));
                    virement.setNumerocompte_Tire(Utility.bourrageGZero(getChamp(12), 12));
                    getChamp(2);
                    virement.setNom_Tire(Utility.bourrageDroite(getChamp(50), 35, " "));
                    virement.setAdresse_Tire(Utility.bourrageDroite(getChamp(70), 50, " "));
                    getChamp(3);
                    virement.setDateordre(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"), ResLoader.getMessages("patternDate")));
                    virement.setNumerovirement(getChamp(10));
                    getChamp(1);
                    virement.setBanque(getChamp(5));
                    virement.setAgence(getChamp(5));
                    virement.setNumerocompte_Beneficiaire(Utility.bourrageGZero(getChamp(12), 12));
                    getChamp(2);
                    virement.setNom_Beneficiaire(Utility.bourrageDroite(getChamp(50), 35, " "));
                    virement.setAdresse_Beneficiaire(Utility.bourrageDroite(getChamp(70), 50, " "));
                    virement.setMontantvirement(String.valueOf(Long.parseLong(getChamp(13))));
                    getChamp(2);//decimales
                    virement.setLibelle(getChamp(70));


                    virement.setDevise("XOF");



                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
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

               
                if (codeOperation.equals("011")) {
                    getChamp(1);
                    getChamp(18);
                    //getChamp(1);
                    virement.setBanqueremettant(Utility.getParamNameOfType(getChamp(3),"CODE_BANKS"));
                    virement.setAgenceremettant(getChamp(5));
                    virement.setDateordre(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"), ResLoader.getMessages("patternDate")));
                    virement.setNumerovirement(getChamp(10));
                    virement.setBanque(Utility.getParamNameOfType(getChamp(3),"CODE_BANKS"));
                    virement.setAgence(getChamp(5));
                    virement.setMontantvirement(String.valueOf(Long.parseLong(getChamp(13))));
                    getChamp(2);
                    
                    virement.setLibelle(getChamp(70));
                    

                    virement.setDevise("XOF");
                   
                    
                    
                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
                   
                        virement.setEtablissement(CMPUtility.getCodeBanqueSica3());
                        virement.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                        virement.setType_Virement("011");
                    
                    virement.setVille("01");

                    virement.setIdvirement(new BigDecimal(Utility.computeCompteur("IDVIREMENT", "VIREMENTS")));

                    db.insertObjectAsRowByQuery(virement, "VIREMENTS");


                }

                if (codeOperation.equals("012")) {

                    getChamp(1);
                    getChamp(18);
                    //getChamp(1);
                    virement.setBanqueremettant(Utility.getParamNameOfType(getChamp(3),"CODE_BANKS"));
                    virement.setAgenceremettant(getChamp(5));
                    virement.setDateordre(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"), ResLoader.getMessages("patternDate")));
                    virement.setNumerovirement(getChamp(10));
                    virement.setBanque(Utility.getParamNameOfType(getChamp(3),"CODE_BANKS"));
                    virement.setAgence(getChamp(5));
                    virement.setMontantvirement(String.valueOf(Long.parseLong(getChamp(13))));
                    getChamp(2);

                    virement.setLibelle(getChamp(70));

                    virement.setDevise("XOF");



                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
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

                    db.insertObjectAsRowByQuery(virement, "VIREMENTS");

                }
            
        }
        db.close();
        return aFile;
    }
}
