/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import java.io.File;
import java.math.BigDecimal;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeAllerRejeteFlexWriter extends FlatFileWriter {

    public ChequeAllerRejeteFlexWriter() {
        setDescription("Envoi des rejets de chèques Aller vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String compteur;
        boolean isEcobankStandard;
         if(Utility.getParam("ECOBANK_STANDARD")!= null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("0")){
        compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTREJCHQALE", "REJCHQALE"), 4, "0");
        isEcobankStandard = false;
         }else{
             compteur = Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTREJCHQALE", "REJCHQALE"), 4, "0");
             isEcobankStandard = true;
         }
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_REJ_FILE_ROOTNAME") + compteur + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));

        StringBuffer line = new StringBuffer("H001UAP");
        line.append(compteur.toLowerCase());
        line.append(CMPUtility.getDate());
        if(isEcobankStandard) line.append(createBlancs(79, " "));
        writeln(line.toString());

        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEREJRET") + ") ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        if (cheques != null && 0 < cheques.length) {

            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];
                //Tous les cheques Aller rejetes - ligne de debit montant sur cpt
                line = new StringBuffer();
                line.append(CMPUtility.getNumCptExAgence(cheque.getCompteremettant(), cheque.getAgenceremettant()));
                line.append(Utility.bourrageDroite(CMPUtility.getNumCptEx(cheque.getCompteremettant(), cheque.getAgenceremettant(),"0"), 16, " "));
                line.append(createBlancs(4, " "));
                line.append("D");
                line.append(Utility.bourrageGauche(cheque.getMontantcheque(), 16," "));
                line.append("Q11");
                line.append(Utility.getParam("DATEVALEUR_ALLER"));
                line.append(Utility.bourrageGauche(cheque.getNumerocheque(), 8, " "));
                line.append(Utility.bourrageDroite(Utility.getParam("LIBREJCHQALEFLEX1"), 25, " "));
                line.append("030");
                line.append(Utility.bourrageGZero(cheque.getIdcheque().toPlainString(), 8));
                line.append(cheque.getMotifrejet());
                writeln(line.toString());

                if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEREJRET"))) {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEREJRETENVSIB")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }

                if (cheque.getMotifrejet().equalsIgnoreCase("201") || cheque.getMotifrejet().equalsIgnoreCase("202")) {
                    //Ligne de debit commission
                    line = new StringBuffer();
                    line.append(CMPUtility.getNumCptExAgence(cheque.getCompteremettant(), cheque.getAgenceremettant()));
                    line.append(Utility.bourrageDroite(CMPUtility.getNumCptEx(cheque.getCompteremettant(), cheque.getAgenceremettant(),"0"), 16, " "));
                    line.append(createBlancs(4, " "));
                    line.append("D");
                    line.append(Utility.bourrageGauche(Utility.getParamOfType("COMDEBREJCHQALEFLEX", "CODE_COMMISSION"), 16, " "));
                    line.append("Q11");
                    line.append(CMPUtility.getDate());
                    line.append(Utility.bourrageGauche(cheque.getNumerocheque(), 8, " "));
                    line.append(Utility.bourrageDroite(Utility.getParam("LIBREJCHQALEFLEX1"), 25, " "));
                    line.append("030");
                    line.append(Utility.bourrageGZero(cheque.getIdcheque().toPlainString(), 8));
                    line.append(cheque.getMotifrejet());
                    writeln(line.toString());

                    //Ligne de credit com1
                    line = new StringBuffer();
                    line.append("001");
                    line.append(Utility.bourrageDroite(Utility.getParam("CPTCRECOMREJCHQALE1"), 16, " "));
                    line.append(createBlancs(4, " "));
                    line.append("C");
                    line.append(Utility.bourrageGauche(Utility.getParamOfType("COMCREREJCHQALEFLEX1", "CODE_COMMISSION"), 16, " "));
                    line.append("Q11");
                    line.append(Utility.getParam("DATEVALEUR_ALLER"));
                    line.append(createBlancs(8, " "));
                    line.append(Utility.bourrageDroite(Utility.getParam("LIBREJCHQALEFLEX2"), 25, " "));
                    line.append(createBlancs(14, " "));
                    writeln(line.toString());

                    //Ligne de credit com2
                    line = new StringBuffer();
                    line.append("001");
                    line.append(Utility.bourrageDroite(Utility.getParam("CPTCRECOMREJCHQALE2"), 16, " "));
                    line.append(createBlancs(4, " "));
                    line.append("C");
                    line.append(Utility.bourrageGauche(Utility.getParamOfType("COMCREREJCHQALEFLEX2", "CODE_COMMISSION"), 16," "));
                    line.append("Q11");
                    line.append(Utility.getParam("DATEVALEUR_ALLER"));
                    line.append(createBlancs(8, " "));
                    line.append(Utility.bourrageDroite(Utility.getParam("LIBREJCHQALEFLEX3"), 25, " "));
                    line.append(createBlancs(14, " "));
                    writeln(line.toString());
                }
                montantTotal += Long.parseLong(cheque.getMontantcheque());
            }
// Gestion cpt globalisation
            line = new StringBuffer();
            line.append("001");
            line.append(Utility.bourrageDroite(Utility.getParam("CPTGLOCOMREJCHQALE"), 16, " "));
            line.append(createBlancs(4, " "));
            line.append("C");
            line.append(Utility.bourrageGauche("" + montantTotal, 16," "));
            line.append("Q11");
            line.append(Utility.getParam("DATEVALEUR_ALLER"));
            line.append(createBlancs(8, " "));
            line.append(Utility.bourrageDroite(Utility.getParam("LIBREJCHQALEFLEX4"), 25, " "));
            line.append(createBlancs(14, " "));
            writeln(line.toString());

            line = new StringBuffer();
            line.append("001");
            line.append(Utility.bourrageDroite(Utility.getParam("CPTGLOCOMREJCHQALE"), 16, " "));
            line.append(createBlancs(4, " "));
            line.append("D");
            line.append(Utility.bourrageGauche("" + montantTotal, 16," "));
            line.append("Q11");
            line.append(Utility.getParam("DATEVALEUR_ALLER"));
            line.append(createBlancs(8, " "));
            line.append(Utility.bourrageDroite(Utility.getParam("LIBREJCHQALEFLEX4"), 25, " "));
            line.append(createBlancs(14, " "));
            writeln(line.toString());

            line = new StringBuffer();
            line.append("001");
            line.append(Utility.bourrageDroite(Utility.getParam("CPTGLOCOMREJCHQALE"), 16, " "));
            line.append(createBlancs(4, " "));
            line.append("C");
            line.append(Utility.bourrageGauche("" + montantTotal, 16, " "));
            line.append("Q11");
            line.append(Utility.getParam("DATEVALEUR_ALLER"));
            line.append(createBlancs(8, " "));
            line.append(Utility.bourrageDroite(Utility.getParam("LIBREJCHQALEFLEX5"), 25, " "));
            line.append(createBlancs(14, " "));
            writeln(line.toString());


            setDescription(getDescription() + " exécuté avec succès: Nombre de Chèque= " + cheques.length + " - Montant Total= " +  Utility.formatNumber(""+montantTotal));
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " +  Utility.formatNumber(""+montantTotal));

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }
        closeFile();
        db.close();
    }
}
