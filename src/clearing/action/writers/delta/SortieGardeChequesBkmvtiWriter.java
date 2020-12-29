/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.delta;

import clearing.table.Cheques;
import clearing.table.delta.Bkcom;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import org.patware.action.file.FlatFileWriter;
import static org.patware.action.file.FlatFileWriter.createBlancs;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.ExtJDBCXmlReader;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class SortieGardeChequesBkmvtiWriter extends FlatFileWriter {

    public SortieGardeChequesBkmvtiWriter() {
        setDescription("Envoi des cheques sortis du Garde Cheque vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();





        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String compteur;

        compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTCHQALE", "CHQALE"), 4, "0");
        String bkmvtiFileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + "bkmvti" + compteur + Utility.getParam("SIB_FILE_EXTENSION");


// Population
        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEGARCHESUPCON") + ") "
                // + "OR  (ETAT IN (" + Utility.getParam("CETAOPEGARCHEMODCON") + ") AND CALCUL=0)"
                + "OR (ETAT IN (" + Utility.getParam("CETAOPEVALSURCAIENVSIB") + "," + Utility.getParam("CETAOPEVALDELTA") + ") AND GARDE=1 )";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

        Cheques aCheque = null;

        if (cheques != null && 0 < cheques.length) {

            setOut(createFlatFile(bkmvtiFileName));
            StringBuffer line;
            long montantTotal = 0;
            for (int i = 0; i < cheques.length; i++) {


                String requete = "select * from bkcom where cha ='" + Utility.getParam("CHACOMGARCHE") + "' and cli in (select cli from bkcom where ncp='" + Utility.bourrageDroite(cheques[i].getCompteremettant().substring(1), 11, " ") + "')";
                requete = "select x0.age AGENCE, x0.ser SERVICE,x0.ncp COMPTE,SUBSTR(x0.inti,1,30) NOM,x0.ribdec CLERIB,x0.CPRO TYPECPT from (" + requete + ") x0";
                DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
                dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());
                Bkcom[] bkcom = (Bkcom[]) dbExt.retrieveRowAsObject(requete, new Bkcom());



                line = new StringBuffer();
                line.append(cheques[i].getAgenceremettant()).append("|952|");
                line.append("|");
                line.append(bkcom[0].getCompte()).append("|");
                line.append(createBlancs(1, " ")).append("|");
                line.append(Utility.getParam("CODOPEGARCHE")).append("|").append("|").append("|");
                line.append("AUTO||");
                line.append(Utility.computeCleRIB(Utility.getParam("CODE_BANQUE_SICA3"), cheques[i].getAgenceremettant(), "0" + bkcom[0].getCompte())).append("|");
                line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));

                try {
                    line.append("|").append(bkcom[0].getService()).append("|");
                } catch (NullPointerException ex) {
                    setDescription(getDescription() + " Compte " + bkcom[0].getCompte() + " non trouvé dans bkcom");
                    logEvent("ERREUR", getDescription());
                    return;
                }

                line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy")).append("|");
                line.append(cheques[i].getMontantcheque());
                line.append("|D|");
                if (cheques[i].getEtat().equals(new BigDecimal(Utility.getParam("CETAOPEGARCHESUPCON")))) {
                    line.append("Suppression Cheque " + cheques[i].getNumerocheque());
                } else {
                    line.append("Echéance Chèque " + cheques[i].getNumerocheque());
                }

                line.append("|N|");
                String numPiece = Utility.computeCompteur("CPTPIEGARCHE", Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                line.append("GARCHE").append(numPiece).append("|GARCHE").append(numPiece).append("||||||||1,0|| |0,0|||N|N|N| |");
                line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                line.append("|||||952|");
                line.append(cheques[i].getMontantcheque());
                line.append("|");
                line.append(numPiece);
                line.append("|");
                line.append(" |001| ||| |||||| ||");
                writeln(line.toString());
                requete = "select x0.age AGENCE, x0.ser SERVICE,x0.ncp COMPTE,SUBSTR(x0.inti,1,30) NOM,x0.ribdec CLERIB,x0.CPRO TYPECPT from bkcom x0 where x0.cfe='N' and x0.ife='N' ";
                bkcom = (Bkcom[]) dbExt.retrieveRowAsObject(requete + " and ncp like '" + Utility.bourrageGauche(Utility.getParam("CPTCONGARCHE"), 11, "0") + "'", new Bkcom());

                line = new StringBuffer();
                line.append("01001").append("|952|");
                line.append("|");
                line.append(Utility.bourrageDroite(Utility.getParam("CPTCONGARCHE"), 11, " ")).append("|");
                line.append(createBlancs(1, " ")).append("|");
                line.append(Utility.getParam("CODOPEGARCHE")).append("|").append("|").append("|");
                line.append("AUTO||");
                line.append(Utility.computeCleRIB(Utility.getParam("CODE_BANQUE_SICA3"), "01001", Utility.bourrageGZero(Utility.getParam("CPTCONGARCHE"), 12))).append("|");
                line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                try {
                    line.append("|").append(bkcom[0].getService()).append("|");
                } catch (NullPointerException ex) {
                    setDescription(getDescription() + " Compte " + Utility.bourrageGauche(Utility.getParam("CPTCONGARCHE"), 11, "0") + " non trouvé dans bkcom");
                    logEvent("ERREUR", getDescription());
                    return;
                }
                line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy")).append("|");
                line.append(cheques[i].getMontantcheque());
                line.append("|C|");
                if (cheques[i].getEtat().equals(new BigDecimal(Utility.getParam("CETAOPEGARCHESUPCON")))) {
                    line.append("Suppression Cheque " + cheques[i].getNumerocheque());
                } else {
                    line.append("Echéance Chèque " + cheques[i].getNumerocheque());
                }
                line.append("|N|");
                line.append("PRL").append(numPiece).append("|PRL").append(numPiece).append("||||||||1,0|| |0,0|||N|N|N| |");
                line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                line.append("|||||952|");
                line.append(cheques[i].getMontantcheque());
                line.append("|");
                line.append(numPiece);
                line.append("|");
                line.append(" |001| ||| |||||| ||");
                writeln(line.toString());
                dbExt.close();
                montantTotal += Long.parseLong(cheques[i].getMontantcheque());
                if (cheques[i].getEtat().toPlainString().equals(Utility.getParam("CETAOPEGARCHESUPCON"))) {
                    cheques[i].setEtat(new BigDecimal(Utility.getParam("CETAOPEGARCHESUPCONSIB")));
                    cheques[i].setLotsib(new BigDecimal(1));
                    db.updateRowByObjectByQuery(cheques[i], "CHEQUES", "IDCHEQUE=" + cheques[i].getIdcheque());
                } else {
                    cheques[i].setGarde("2");
                    db.updateRowByObjectByQuery(cheques[i], "CHEQUES", "IDCHEQUE=" + cheques[i].getIdcheque());
                }

            }




            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Cheques= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));
            logEvent("INFO", "Nombre de Cheques= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));
            closeFile();

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
