/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.orion;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import java.io.File;
import java.util.Date;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeRetourEISSignWriter extends FlatFileWriter {

    public ChequeRetourEISSignWriter() {
        setDescription("Envoi des chèques retour vers SIGN");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());



        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPERETRECENVSIBVER") +","+ Utility.getParam("CETAOPERETRECENVSIBERR") +","+ Utility.getParam("CETAOPERETRECENVSIB") +") AND (LOTSIB IS NULL OR LOTSIB=1) AND BANQUEREMETTANT IN  (SELECT CODEBANQUE FROM BANQUES B WHERE B.ALGORITHMEDECONTROLESPECIFIQUE =" + Utility.getParam("BANQUE_EIS")+" ) ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long sumComptes = 0, sumMontants = 0;
        if (cheques != null && 0 < cheques.length) {

            String fileName = Utility.getParam("SIGN_IN_FOLDER") + File.separator + Utility.getParam("SIGN_OUT_FILE_ROOTNAME") + CMPUtility.getDate() + Utility.getParam("SIGN_FILE_EXTENSION");
            setOut(createFlatFile(fileName));
            for (int i = 0; i < cheques.length; i++) {
                sumMontants += Long.parseLong(cheques[i].getMontantcheque());
                sumComptes += Long.parseLong(cheques[i].getNumerocompte());

            }
            String line = "01" + CMPUtility.getDate() + Utility.bourrageGZero("" + cheques.length, 5);
            line += Utility.bourrageGauche(String.valueOf(sumComptes), 12, "0");
            line += Utility.bourrageGauche(String.valueOf(sumMontants), 16, "0");
            writeln(line);

            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];
                //Tous les cheques retours reconciliés

                line = "02";
                line += Utility.convertDateToString(Utility.convertStringToDate(cheque.getDatecompensation(), ResLoader.getMessages("patternDate")), "yyyyMMdd");
                line += Utility.bourrageGZero(cheque.getIdcheque().toPlainString(), 18);
                line += Utility.convertDateToString(new Date(), "HHmmss");
                line += cheque.getBanqueremettant();
                line += cheque.getAgenceremettant();
                line += Utility.bourrageGZero(cheque.getType_Cheque(), 3);
                line += Utility.bourrageGZero(cheque.getNumerocheque(), 7);
                line += Utility.bourrageGZero(cheque.getNumerocompte(), 12);
                line += cheque.getRibcompte();
                line += Utility.bourrageGZero(cheque.getMontantcheque(), 16);
                line += Utility.bourrageDroite(cheque.getPathimage().replace(Utility.getParam("MAILO_EXT_FOLDER"), "").trim() + cheque.getFichierimage().trim(), 256, " ");
                writeln(line);

                db.executeUpdate("UPDATE CHEQUES SET LOTSIB = 3 WHERE IDCHEQUE=" + cheque.getIdcheque() +" AND ETAT IN (" + Utility.getParam("CETAOPERETRECENVSIBVER")   +","+ Utility.getParam("CETAOPERETRECENVSIBERR") +")");
                db.executeUpdate("UPDATE CHEQUES SET LOTSIB = 4, ETAT= "+ Utility.getParam("CETAOPERETRECENVSIBERR")+" WHERE IDCHEQUE=" + cheque.getIdcheque() +" AND ETAT IN (" + Utility.getParam("CETAOPERETRECENVSIB") +")");



            }
            closeFile();
            setDescription(getDescription()+" exécuté avec succès: Nombre de Chèque= "+cheques.length +" - Montant Total= "+sumMontants);
            logEvent("INFO", "Nombre de Chèque= "+cheques.length +" - Montant Total= "+sumMontants);
            db.executeUpdate("UPDATE CHEQUES SET LOTSIB = 5 WHERE ETAT IN (" + Utility.getParam("CETAOPERETRECENVSIB")  +")");
        } else {
            setDescription(getDescription()+": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
