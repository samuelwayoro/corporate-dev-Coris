/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.orion;

import clearing.table.Cheques;
import org.patware.action.file.FlatFileReader;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeRetourSignReader extends FlatFileReader {

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null, sql = null;
        Cheques[] cheques = null;

       
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        int nbrChq, i = 0;
        long currentSumAmount = 0;
        long currentSumAccount = 0;
        String sumAccount = "";
        String sumAmount = "";
        Cheques aCheque = null;
        BufferedReader is = openFile(aFile);
        while ((line = is.readLine()) != null) {

            setCurrentLine(line);

            //aCheque.setDatetraitement(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"), ResLoader.getMessages("patternDate")));
            String code = getChamp(2);
            if (code.equalsIgnoreCase("01")) {
                getChamp(8);
                nbrChq = Integer.parseInt(getChamp(5));
                cheques = new Cheques[nbrChq];
                sumAccount = getChamp(12);
                sumAmount = getChamp(16);
            } else {
                if (code.equalsIgnoreCase("02")) {
                    aCheque = new Cheques();
                    getChamp(8);
                    aCheque.setIdcheque(new BigDecimal(getChamp(18)));
                    getChamp(6);
                    aCheque.setBanqueremettant(getChamp(5));
                    aCheque.setAgenceremettant(getChamp(5));
                    getChamp(3);
                    aCheque.setNumerocheque(getChamp(7));
                    aCheque.setNumerocompte(getChamp(12));
                    currentSumAccount += Long.parseLong(aCheque.getNumerocompte());
                    getChamp(2);
                    aCheque.setMontantcheque(getChamp(16));
                    currentSumAmount += Long.parseLong(aCheque.getMontantcheque());

                    aCheque.setMotifrejet(getChamp(3));
                    aCheque.setCodeutilisateur(getChamp(4));
                    cheques[i++] = aCheque;
                }
            }
        }

        if (Utility.bourrageGauche(currentSumAccount + "", 12, "0").equalsIgnoreCase(sumAccount) &&
                Utility.bourrageGauche(currentSumAmount + "", 16, "0").equalsIgnoreCase(sumAmount)) {
            for (int j = 0; j < cheques.length; j++) {
                aCheque = cheques[j];
                if(!aCheque.getMotifrejet().equalsIgnoreCase("000")){
                sql = "UPDATE CHEQUES SET ETAT = " + Utility.getParam("CETAOPEALLICOM2") +
                        ", MOTIFREJET='" + aCheque.getMotifrejet() + "', CODEUTILISATEUR='" + aCheque.getCodeutilisateur() + "' WHERE IDCHEQUE=" + aCheque.getIdcheque() + " AND ETAT="+ Utility.getParam("CETAOPERETRECENVSIB");

                db.executeUpdate(sql);
                }else{
                    sql = "UPDATE CHEQUES SET CODEUTILISATEUR='" + aCheque.getCodeutilisateur() + "' WHERE IDCHEQUE=" + aCheque.getIdcheque() + " AND ETAT="+ Utility.getParam("CETAOPERETRECENVSIB");

                db.executeUpdate(sql);
                }
            }

        }
        if (!Utility.bourrageGauche(currentSumAccount + "", 12, "0").equalsIgnoreCase(sumAccount)) {
            logEvent("ERROR", "Erreur dans la somme des montants|Calculé:" + currentSumAccount + "|Fichier:" + sumAccount + "|");
        }
        if (!Utility.bourrageGauche(currentSumAmount + "", 16, "0").equalsIgnoreCase(sumAmount)) {
            logEvent("ERROR", "Erreur dans la somme des comptes|Calculé:" + currentSumAccount + "|Fichier:" + sumAmount + "|");
        }

        db.close();
        return aFile;
    }
}
