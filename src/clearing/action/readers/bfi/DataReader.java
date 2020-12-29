/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.bfi;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Remises;
import org.patware.action.file.FlatFileReader;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;

import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class DataReader extends FlatFileReader {

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;

        Cheques aCheque = new Cheques();
        Remises aRemise = new Remises();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        BufferedReader is = openFile(aFile);
        while ((line = is.readLine()) != null) {
            setCurrentLine(line);
            String typeEnreg = getChamp(1);
            if (typeEnreg.equals("1")) {
                aRemise.setEtablissement(getChamp(5));
                aRemise.setAgenceRemettant(getChamp(5));
                aRemise.setCompteRemettant(getChamp(11));
                getChamp(2);
                aRemise.setAgenceDepot(getChamp(7));
                aRemise.getAgenceDepot();
                aRemise.setNbOperation(new BigDecimal(getChamp(10)));
                aRemise.setMontant(String.valueOf(Long.parseLong(getChamp(15))));
                getChamp(2);// reconciliation
                getChamp(20);
                aRemise.setNomUtilisateur(getChamp(20));
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL")));
                getChamp(70);
                aRemise.setDateSaisie(aFile.getName().substring(13, 21));
                aRemise.setDevise(CMPUtility.getDevise());
                aRemise.setSequence(new BigDecimal(Utility.computeCompteur("IDREMISE", "REMISES")));
                aRemise.setIdremise(aRemise.getSequence());
                db.insertObjectAsRowByQuery(aRemise, "REMISES");

            }
            if (typeEnreg.equals("2")) {
                aCheque.setRio(getChamp(7));
                aCheque.setNumerocheque(getChamp(7));
                aCheque.setBanque(getChamp(5));
                aCheque.setAgence(getChamp(5));
                aCheque.setNumerocompte(getChamp(11));
                aCheque.setRibcompte(getChamp(2));
                aCheque.setDateemission(getChamp(8));
                aCheque.setMontantcheque(String.valueOf(Long.parseLong(getChamp(15))));
                getChamp(2);
                aCheque.setType_Cheque(getChamp(2));
                aCheque.setCodeutilisateur(getChamp(20));
                aCheque.setDevise(CMPUtility.getDevise());
                aCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                aCheque.setDatesaisie(aCheque.getDatetraitement());
                aCheque.setBanqueremettant(aRemise.getEtablissement());
                aCheque.setAgenceremettant(aRemise.getAgenceRemettant());
                aCheque.setCompteremettant(aRemise.getCompteRemettant());
                aCheque.setOrigine(new BigDecimal(0));
                aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL")));
                aCheque.setIdcheque(new BigDecimal(Utility.computeCompteur("IDCHEQUE", "CHEQUES")));
                if (aCheque.getRio().equals(aRemise.getAgenceDepot())) {
                    aCheque.setRemise(aRemise.getIdremise());
                }
                db.insertObjectAsRowByQuery(aCheque, "CHEQUES");

            }

        }
        db.close();
        return aFile;
    }

}
