/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.orion;

import clearing.model.CMPUtility;
import clearing.table.Comptes;
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
public class VirementEffetSitReaderSICA3 extends FlatFileReader {

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
            if (getChamp(2).equalsIgnoreCase("<>")) {
                getChamp(4);
                getChamp(2);
                codeOperation = getChamp(3);
                if (codeOperation.equals("060")) {
                    getChamp(1);
                    effet.setBanqueremettant(getChamp(5));
                    getChamp(3);
                    effet.setBanque(getChamp(5));
                    getChamp(2);
                    effet.setAgence(getChamp(5));
                    effet.setDevise(getChamp(3));
                    getChamp(1);
                    effet.setMontant_Effet(String.valueOf(Long.parseLong(getChamp(14))));
                    getChamp(2);//decimales
                    effet.setDatetraitement(Utility.convertDateToString(Utility.convertStringToDate(getChamp(6), "yyMMdd"), ResLoader.getMessages("patternDate")));
                    getChamp(42);
                    effet.setNumeroeffet(getChamp(10));


                    //effet.setAgenceremettant(getChamp(5));
                    effet.setNumerocompte_Beneficiaire(Utility.bourrageGZero(getChamp(12), 12));
                    getChamp(4);
                    effet.setNumerocompte_Tire(Utility.bourrageGZero(getChamp(12), 12));

                    effet.setNom_Beneficiaire(getChamp(26));
                    effet.setDate_Echeance(Utility.convertDateToString(Utility.convertStringToDate(getChamp(6), "yyMMdd"), ResLoader.getMessages("patternDate")));
                    effet.setDate_Creation(Utility.convertDateToString(Utility.convertStringToDate(getChamp(6), "yyMMdd"), ResLoader.getMessages("patternDate")));
                    effet.setDatesaisie(Utility.convertDateToString(Utility.convertStringToDate(getChamp(6), "yyMMdd"), ResLoader.getMessages("patternDate")));

                    effet.setAgenceremettant(getChamp(5));
                    if (effet.getAgenceremettant() == null) {
                        Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numero like '%" + effet.getNumerocompte_Beneficiaire() + "%'", new Comptes());
                        if (comptes != null && comptes.length > 0) {
                            effet.setAgenceremettant(comptes[0].getAgence());
                        } else {
                            effet.setAgenceremettant("01001");
                        }
                    }
                    getChamp(5);
                    effet.setNom_Tire(getChamp(24));
                    effet.setCode_Acceptation(getChamp(1));
                    getChamp(39);
                    effet.setIdentification_Tire(getChamp(12));
                    effet.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
                    if (Character.isDigit(effet.getBanque().charAt(1))) {
                        effet.setEtablissement(CMPUtility.getCodeBanque());
                        effet.setBanqueremettant(CMPUtility.getCodeBanque());
                        effet.setType_Effet("" + Utility.getParam("LETCHAACCVALPAPANCNOR") + "");
                    } else {
                        effet.setEtablissement(CMPUtility.getCodeBanqueSica3());
                        effet.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                        effet.setType_Effet("" + Utility.getParam("LETCHANOUNOR") + "");
                    }
                    effet.setVille("01");

                    effet.setIdeffet(new BigDecimal(Utility.computeCompteur("IDEFFET", "EFFETS")));

                    db.insertObjectAsRowByQuery(effet, "EFFETS");


                }

                if (codeOperation.equals("120")) {
                    getChamp(1);
                    virement.setBanqueremettant(getChamp(5));
                    getChamp(3);
                    virement.setBanque(getChamp(5));
                    getChamp(2);
                    virement.setAgence(getChamp(5));
                    virement.setDevise(getChamp(3));
                    getChamp(1);
                    virement.setMontantvirement(String.valueOf(Long.parseLong(getChamp(14))));
                    getChamp(2);
                    virement.setDateordre(Utility.convertDateToString(Utility.convertStringToDate(getChamp(6), "yyMMdd"), ResLoader.getMessages("patternDate")));

                    getChamp(42);
                    virement.setNumerovirement(getChamp(10));

                    // virement.setAgenceremettant(getChamp(5));

                    virement.setNumerocompte_Tire(Utility.bourrageGZero(getChamp(12), 12));
                    getChamp(4);
                    virement.setNumerocompte_Beneficiaire(Utility.bourrageGZero(getChamp(12), 12));
                    virement.setNom_Tire(getChamp(26));
                    //getChamp(5);
                    virement.setAgenceremettant(getChamp(5));
                    if (virement.getAgenceremettant() == null) {
                        Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numero like '%" + virement.getNumerocompte_Tire() + "%'", new Comptes());
                        if (comptes != null && comptes.length > 0) {
                            virement.setAgenceremettant(comptes[0].getAgence());
                        } else {
                            virement.setAgenceremettant("01001");
                        }
                    }
                    getChamp(6);
                    getChamp(6);
                    getChamp(11);
                    virement.setNom_Beneficiaire(getChamp(24));
                    getChamp(1);
                    getChamp(39);
                    virement.setLibelle(getChamp(50));
                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
                    virement.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                    //virement.setDatecompensation(Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd"));
                    if (Character.isDigit(virement.getBanque().charAt(1))) {
                        virement.setEtablissement(CMPUtility.getCodeBanque());
                        virement.setBanqueremettant(CMPUtility.getCodeBanque());
                        virement.setType_Virement("" + Utility.getParam("VIRSTACLIANCNOR") + "");
                    } else {
                        virement.setEtablissement(CMPUtility.getCodeBanqueSica3());
                        virement.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                        virement.setType_Virement("" + Utility.getParam("VIRSTACLINOUNOR") + "");
                    }
                    virement.setVille("01");

                    virement.setIdvirement(new BigDecimal(Utility.computeCompteur("IDVIREMENT", "VIREMENTS")));

                    db.insertObjectAsRowByQuery(virement, "VIREMENTS");
                }

            }
        }
        db.close();
        return aFile;
    }
}
