/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers;

import clearing.model.CMPUtility;
import clearing.table.Vignettes;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class VignettesReader extends FlatFileReader {

    public VignettesReader() {

        setTattooProcessDate(true);
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;
       
        Vignettes vignette = new Vignettes();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        int compteur = 0;
        BufferedReader is = openFile(aFile);
        while ((line = is.readLine()) != null) {
            compteur++;
            if (compteur != 1) {
                setCurrentLine(line);

                getChamp(1);
                vignette.setNumerosequence(getChamp(5));
                getChamp(1);
                vignette.setNumerocommande(getChamp(4));
                vignette.setDatecommande(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "ddMMyyyy"), "yyyy/MM/dd"));
                vignette.setDatereception(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "ddMMyyyy"), "yyyy/MM/dd"));
                vignette.setFournisseur(getChamp(15));
                getChamp(5);
                vignette.setProducteur(getChamp(25));
                vignette.setMachineproduction(getChamp(25));
                vignette.setDateproduction(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "ddMMyyyy"), "yyyy/MM/dd"));
                vignette.setEditeur(getChamp(25));
                vignette.setDateedition(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "ddMMyyyy"), "yyyy/MM/dd"));
                getChamp(10);
                vignette.setTypeimprime(getChamp(1));
                vignette.setCodebarrement(getChamp(1));
                vignette.setEtatreprise(getChamp(1));
                vignette.setCodereprise(getChamp(2));
                vignette.setNumerocheque(getChamp(7));
                getChamp(1);
                vignette.setNumeroendos(getChamp(7));
                getChamp(1);
                vignette.setNumeroserie(getChamp(20));
                getChamp(1);
                vignette.setCodevignette(getChamp(21));
                getChamp(1);
                vignette.setLibellebarrement(getChamp(13));
                vignette.setLibelletypeimprime(getChamp(15));
                vignette.setLibellereprise(getChamp(15));
                vignette.setLibellecodereprise(getChamp(10));
                getChamp(1);
                getChamp(10);
                vignette.setNbrchqcarnet(getChamp(4));
                //getChamp(2);
                vignette.setLibellefixe(getChamp(12));
                vignette.setNumdernierchq(getChamp(7));
                getChamp(5);
                getChamp(1);
                // Donnees ligne CmC7
                getChamp(7);// Num de cheque
                getChamp(1);
                vignette.setCodebanque(CMPUtility.getAlphaNumericCodeBanque(getChamp(5)));
                vignette.setCodeguichet(getChamp(5));
                vignette.setClerib(getChamp(2));
                getChamp(2);
                vignette.setNumerocompte(Utility.bourrageGZero(getChamp(12), 12));
                getChamp(1);
                vignette.setPayable1(getChamp(25));
                vignette.setPayable2(getChamp(25));
                vignette.setPayable3(getChamp(25));
                vignette.setPayable4(getChamp(25));
                vignette.setPayable5(getChamp(25));
                getChamp(25);
                vignette.setInfoclient1(getChamp(35));
                vignette.setInfoclient2(getChamp(35));
                vignette.setInfoclient3(getChamp(35));
                vignette.setInfoclient4(getChamp(35));
                vignette.setInfoclient5(getChamp(35));
                getChamp(34);
                getChamp(1);

                vignette.setDatecreation(Utility.convertDateToString(new Date(System.currentTimeMillis()),"yyyy/MM/dd"));
                vignette.setEtat(new BigDecimal(Utility.getParam("CETAVIGSTO")));
                vignette.setIdvignette(new BigDecimal(Utility.computeCompteur("IDVIGNETTE", "VIGNETTES")));
                db.insertObjectAsRowByQuery(vignette, "VIGNETTES");

            }

        }
        db.close();
        return aFile;

    }
}
