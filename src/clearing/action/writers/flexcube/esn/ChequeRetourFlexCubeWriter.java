/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube.esn;

import clearing.model.CMPUtility;
import clearing.model.RIO;
import clearing.table.Cheques;
import java.io.File;
import java.math.BigDecimal;
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
public class ChequeRetourFlexCubeWriter extends FlatFileWriter {

    public ChequeRetourFlexCubeWriter() {
        setDescription("Envoi des chèques retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String dateTraitement = Utility.convertDateToString(new Date(), "ddMMyy");

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_OUT_FILE_ROOTNAME") + dateTraitement + Utility.getParam("SIB_FILE_EXTENSION");

        //Population
        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERET") + ") ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        long montantTotalEchec = 0;
        if (cheques != null && 0 < cheques.length) {
            setOut(createFlatFile(fileName));
            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];
                String numCptEx = CMPUtility.getNumCptEx(cheque.getNumerocompte(), cheque.getAgence(),"1");
                if (!isValidLine(cheque)) {

                    montantTotalEchec += Long.parseLong(cheque.getMontantcheque());
                    if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))) {
                        cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPERETMAN")));
                        db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                    } else {
                        cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPERETRECMAN")));
                        db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                    }

                } else {
                    //Tous les cheques retours
                    String line = "";
                    line += cheque.getBanqueremettant();
                    line += cheque.getBanque();
                    line += "011 ";
                    line += Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 7, " ");
                    line += Utility.convertDateToString(Utility.convertStringToDate(cheque.getDatecompensation(), ResLoader.getMessages("patternDate")), "yyyyMMdd");
                    line += Utility.convertDateToString(Utility.convertStringToDate(cheque.getDatecompensation(), ResLoader.getMessages("patternDate")), "yyyyMMdd");
                    //line += dateValeur;
                    line += Utility.bourrageGauche(cheque.getMontantcheque(), 16, "0");
                    line += createBlancs(3, " ") + "UAP";
                    line += Utility.bourrageDroite(cheque.getNombeneficiaire(), 27, " ");
                    line += cheque.getRio().substring(27);

                    line += Utility.bourrageGZero(numCptEx, 16);
                    line += Utility.bourrageGZero(numCptEx, 16).substring(0, 3);

                    writeln(line);
                    cheque.setLotsib(new BigDecimal(1));

                    montantTotal += Long.parseLong(cheque.getMontantcheque());
                }

            }
            db.executeUpdate("UPDATE CHEQUES SET LOTSIB=1,ETAT=" + Utility.getParam("CETAOPERETENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPERET"));
            db.executeUpdate("UPDATE CHEQUES SET LOTSIB=1,ETAT=" + Utility.getParam("CETAOPERETRECENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPERETREC"));
            closeFile();

            setDescription(getDescription() + " exécuté avec succès:<br> Nombre de Chèque accepte = " + cheques.length + " - Montant Total accepte = " + Utility.formatNumber("" + montantTotal) + " <br> - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total accepte = " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);

            fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_OUT_ERR_FILE_ROOTNAME") + dateTraitement + Utility.getParam("SIB_FILE_EXTENSION");

            //Population
            sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPERETRECMAN") + "," + Utility.getParam("CETAOPERETMAN") + ") ";
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

            if (cheques != null && 0 < cheques.length) {
                setOut(createFlatFile(fileName));
                for (int i = 0; i < cheques.length; i++) {
                    Cheques cheque = cheques[i];
                    String line = "";
                    line += cheque.getBanqueremettant() + ";";
                    line += cheque.getBanque() + ";";
                    line += Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 7, " ") + ";";
                    line += Utility.convertDateToString(Utility.convertStringToDate(cheque.getDatecompensation(), ResLoader.getMessages("patternDate")), "yyyyMMdd") + ";";
                    line += Utility.bourrageGauche(cheque.getMontantcheque(), 16, "0") + ";";
                    line += cheque.getAgence() + ";";
                    line += cheque.getNumerocompte() + ";";
                    line += Utility.bourrageDroite(cheque.getNombeneficiaire(), 27, " ") + ";";
                    line += cheque.getRio().substring(27);
                    writeln(line);
                    cheque.setLotsib(new BigDecimal(1));

                }

                db.executeUpdate("UPDATE CHEQUES SET LOTSIB=1,ETAT=" + Utility.getParam("CETAOPERETENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPERETMAN"));
                db.executeUpdate("UPDATE CHEQUES SET LOTSIB=1,ETAT=" + Utility.getParam("CETAOPERETRECENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPERETRECMAN"));

                closeFile();
                setDescription(getDescription() +"<br> Nombre de Chèque en echec= " + cheques.length + " - Montant Total en echec = " + Utility.formatNumber("" + montantTotalEchec) + "<br> - Nom de Fichier Echec =  <a onclick=\"NoPrompt()\" target=\"dynamic\" href=\"sortie/" + fileName.replace(Utility.getParam("SIB_IN_FOLDER")+"\\", "") +"\">"+ fileName.replace(Utility.getParam("SIB_IN_FOLDER")+"\\", "") + "</a>" );
                logEvent("INFO", "Nombre de Chèque en Echec = " + cheques.length + " - Montant Total en echec = " + Utility.formatNumber("" + montantTotalEchec) + " - Nom de Fichier Echec = " + fileName);

            }

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        //MAJ DES CHEQUES SANS IMAGES AVEC MOTIF REJET 215
        sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM2") + " , MOTIFREJET='215' WHERE ETAT=" + Utility.getParam("CETAOPERETENVSIB") + " AND BANQUEREMETTANT IN (SELECT CODEBANQUE FROM BANQUES WHERE ALGORITHMEDECONTROLESPECIFIQUE=1)";
        db.executeUpdate(sql);
        db.close();
    }

    private boolean isValidLine(Cheques cheque) throws Exception {
        //Verification de l'existence du compte
        String numCptEx = CMPUtility.getNumCptEx(cheque.getNumerocompte(), cheque.getAgence(),"1");
        if (numCptEx == null) {
            return false;
        }

        //Verification des manager cheques
        if (cheque.getNumerocompte().equals(Utility.getParam("CPTFLEXMCACCOUNT"))) {
            return false;
        }

        //Verification des comptes staff
        if ("051|085".contains(CMPUtility.getAcctClass(numCptEx))) {
            return false;
        }
        return true;
    }
}
