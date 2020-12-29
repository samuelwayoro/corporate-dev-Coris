/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube.esn;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeRetourRejeteFlexCubeWriter extends FlatFileWriter {

    public ChequeRetourRejeteFlexCubeWriter() {
        setDescription("Envoi des rejets de chèques Retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        String numeroBatch = "";
        String[] param1 = (String[]) getParametersMap().get("textParam1");
        if (param1 != null && param1.length > 0) {
            numeroBatch = param1[0];
        }
        System.out.println("Numéro de Batch = " + numeroBatch);

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String dateTraitement = Utility.convertDateToString(new Date(), "ddMMyy");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_REJ_RET_FILE_ROOTNAME") + dateTraitement + Utility.getParam("SIB_FILE_EXTENSION");
        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM2ACC") + ") ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        if (cheques != null && 0 < cheques.length) {
            setOut(createFlatFile(fileName));
            StringBuffer line = new StringBuffer("H" + Utility.getParam("FLEXBRANCHCODE") + "UAP");
            line.append(numeroBatch.toLowerCase());
            line.append(CMPUtility.getDate());

            writeln(line.toString());
            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];
                if (!isValidLine(cheque)) {
                    continue;
                }
                //Tous les cheques retour rejetes - ligne de credit montant sur cpt
                //Ligne 1 (Extourne du montant préalablement débité)
                line = new StringBuffer();
                line.append((Utility.bourrageGZero(CMPUtility.getNumCptEx(cheque.getNumerocompte(), cheque.getAgence(),"1"), 16)).substring(0, 3));
                line.append(Utility.bourrageGZero(CMPUtility.getNumCptEx(cheque.getNumerocompte(), cheque.getAgence(),"1"), 16));
                line.append(createBlancs(4, " "));
                line.append("C");
                line.append(Utility.bourrageGauche(cheque.getMontantcheque(), 16, " "));
                line.append("F57");
                line.append(CMPUtility.getDate());
                line.append(Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 8, " "));
                line.append(Utility.bourrageDroite("Rej chq N°" + cheque.getNumerocheque() + " " + Utility.getParamLabel(cheque.getMotifrejet()), 25, " "));
                line.append("030");
                writeln(line.toString());

                line = new StringBuffer();
                line.append(Utility.getParam("FLEXMAINBRANCH"));
                line.append(Utility.bourrageDroite(Utility.getParam("CPTDEBREJCHQRET"), 16, " "));
                line.append(createBlancs(4, " "));
                line.append("D");
                line.append(Utility.bourrageGauche(cheque.getMontantcheque(), 16, " "));
                line.append("F03");
                line.append(CMPUtility.getDate());
                line.append(Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 8, " "));
                line.append(Utility.bourrageDroite("Rej chq N°" + cheque.getNumerocheque() + " " + Utility.getParamLabel(cheque.getMotifrejet()), 25, " "));
                line.append("030");
                writeln(line.toString());

                if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM2ACC"))) {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM2ACCENVSIB")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }

                if (cheque.getMotifrejet().equalsIgnoreCase("201") || cheque.getMotifrejet().equalsIgnoreCase("202")) {
                    //Ligne de debit commission

                    line = new StringBuffer();
                    line.append((Utility.bourrageGZero(CMPUtility.getNumCptEx(cheque.getNumerocompte(), cheque.getAgence(),"1"), 16)).substring(0, 3));
                    line.append(Utility.bourrageGZero(CMPUtility.getNumCptEx(cheque.getNumerocompte(), cheque.getAgence(),"1"), 16));
                    line.append(createBlancs(4, " "));
                    line.append("D");
                    line.append(Utility.bourrageGauche(Utility.getParamOfType("COMDEBREJCHQRETFLEX", "CODE_COMMISSION"), 16, " "));
                    line.append("C59");
                    line.append(CMPUtility.getDate());
                    line.append(Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 8, " "));
                    line.append(Utility.bourrageDroite("Nos Frais Rej chq N°" + cheque.getNumerocheque() + " " + Utility.getParamLabel(cheque.getMotifrejet()), 25, " "));
                    line.append("030");
                    writeln(line.toString());

                    //Ligne de credit commission
                    line = new StringBuffer();
                    line.append(Utility.getParam("FLEXMAINBRANCH"));
                    line.append(Utility.bourrageDroite(Utility.getParam("CPTCRECOMREJCHQRET1"), 16, " "));
                    line.append(createBlancs(4, " "));
                    line.append("C");
                    line.append(Utility.bourrageGauche(Utility.getParamOfType("COMCREREJCHQRETFLEX1", "CODE_COMMISSION"), 16, " "));
                    line.append("C59");
                    line.append(CMPUtility.getDate());
                    line.append(Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 8, " "));
                    line.append(Utility.bourrageDroite("Nos Frais Rej chq N°" + cheque.getNumerocheque() + " " + Utility.getParamLabel(cheque.getMotifrejet()), 25, " "));
                    line.append("030");
                    writeln(line.toString());

                    //Ligne de credit com2
                    line = new StringBuffer();
                    line.append(Utility.getParam("FLEXMAINBRANCH"));
                    line.append(Utility.bourrageDroite(Utility.getParam("CPTCRECOMREJCHQRET2"), 16, " "));
                    line.append(createBlancs(4, " "));
                    line.append("C");
                    line.append(Utility.bourrageGauche(Utility.getParamOfType("COMCREREJCHQRETFLEX2", "CODE_COMMISSION"), 16, " "));
                    line.append("C59");
                    line.append(CMPUtility.getDate());
                    line.append(Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 8, " "));
                    line.append(Utility.bourrageDroite("Nos Frais Rej chq N°" + cheque.getNumerocheque() + " " + Utility.getParamLabel(cheque.getMotifrejet()), 25, " "));
                    line.append("030");
                    writeln(line.toString());
                }
                montantTotal += Long.parseLong(cheque.getMontantcheque());
            }

            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));
            closeFile();
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_REJ_RET_ERR_FILE_ROOTNAME") + dateTraitement + Utility.getParam("SIB_FILE_EXTENSION");
        sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM2ACC") + ") ";
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        j = 0;
        montantTotal = 0;
        if (cheques != null && 0 < cheques.length) {
            setOut(createFlatFile(fileName));
            StringBuffer line = new StringBuffer("H" + Utility.getParam("FLEXBRANCHCODE") + "UAP");
            line.append(numeroBatch.toLowerCase());
            line.append(CMPUtility.getDate());

            writeln(line.toString());
            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];

                String numCptEx = CMPUtility.getNumCptEx(cheque.getNumerocompte(), cheque.getAgence(),"1");
                if (numCptEx == null) {
                    numCptEx = cheque.getAgence().substring(2) + "0" + cheque.getNumerocompte();
                }
                //Tous les cheques retour rejetes - ligne de credit montant sur cpt
                //Ligne 1 (Extourne du montant préalablement débité)
                line = new StringBuffer();
                line.append(numCptEx.substring(0, 3));
                line.append(Utility.bourrageGZero(numCptEx, 16));
                line.append(createBlancs(4, " "));
                line.append("C");
                line.append(Utility.bourrageGauche(cheque.getMontantcheque(), 16, " "));
                line.append("F57");
                line.append(CMPUtility.getDate());
                line.append(Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 8, " "));
                line.append(Utility.bourrageDroite("Rej chq N°" + cheque.getNumerocheque() + " " + Utility.getParamLabel(cheque.getMotifrejet()), 25, " "));
                line.append("030");
                writeln(line.toString());

                line = new StringBuffer();
                line.append(Utility.getParam("FLEXMAINBRANCH"));
                line.append(Utility.bourrageDroite(Utility.getParam("CPTDEBREJCHQRET"), 16, " "));
                line.append(createBlancs(4, " "));
                line.append("D");
                line.append(Utility.bourrageGauche(cheque.getMontantcheque(), 16, " "));
                line.append("F03");
                line.append(CMPUtility.getDate());
                line.append(Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 8, " "));
                line.append(Utility.bourrageDroite("Rej chq N°" + cheque.getNumerocheque() + " " + Utility.getParamLabel(cheque.getMotifrejet()), 25, " "));
                line.append("030");
                writeln(line.toString());

                if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM2ACC"))) {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM2ACCENVSIB")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }

                if (cheque.getMotifrejet().equalsIgnoreCase("201") || cheque.getMotifrejet().equalsIgnoreCase("202")) {
                    //Ligne de debit commission

                    line = new StringBuffer();
                    line.append(numCptEx.substring(0, 3));
                    line.append(Utility.bourrageGZero(numCptEx, 16));
                    line.append(createBlancs(4, " "));
                    line.append("D");
                    line.append(Utility.bourrageGauche(Utility.getParamOfType("COMDEBREJCHQRETFLEX", "CODE_COMMISSION"), 16, " "));
                    line.append("C59");
                    line.append(CMPUtility.getDate());
                    line.append(Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 8, " "));
                    line.append(Utility.bourrageDroite("Nos Frais Rej chq N°" + cheque.getNumerocheque() + " " + Utility.getParamLabel(cheque.getMotifrejet()), 25, " "));
                    line.append("030");
                    writeln(line.toString());

                    //Ligne de credit commission
                    line = new StringBuffer();
                    line.append(Utility.getParam("FLEXMAINBRANCH"));
                    line.append(Utility.bourrageDroite(Utility.getParam("CPTCRECOMREJCHQRET1"), 16, " "));
                    line.append(createBlancs(4, " "));
                    line.append("C");
                    line.append(Utility.bourrageGauche(Utility.getParamOfType("COMCREREJCHQRETFLEX1", "CODE_COMMISSION"), 16, " "));
                    line.append("C59");
                    line.append(CMPUtility.getDate());
                    line.append(Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 8, " "));
                    line.append(Utility.bourrageDroite("Nos Frais Rej chq N°" + cheque.getNumerocheque() + " " + Utility.getParamLabel(cheque.getMotifrejet()), 25, " "));
                    line.append("030");
                    writeln(line.toString());

                    //Ligne de credit com2
                    line = new StringBuffer();
                    line.append(Utility.getParam("FLEXMAINBRANCH"));
                    line.append(Utility.bourrageDroite(Utility.getParam("CPTCRECOMREJCHQRET2"), 16, " "));
                    line.append(createBlancs(4, " "));
                    line.append("C");
                    line.append(Utility.bourrageGauche(Utility.getParamOfType("COMCREREJCHQRETFLEX2", "CODE_COMMISSION"), 16, " "));
                    line.append("C59");
                    line.append(CMPUtility.getDate());
                    line.append(Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 8, " "));
                    line.append(Utility.bourrageDroite("Nos Frais Rej chq N°" + cheque.getNumerocheque() + " " + Utility.getParamLabel(cheque.getMotifrejet()), 25, " "));
                    line.append("030");
                    writeln(line.toString());
                }
                montantTotal += Long.parseLong(cheque.getMontantcheque());
            }

            setDescription(getDescription() +"\nFichier Echec des rejet chèques retour vers le SIB");
            setDescription(getDescription() + "\n Nombre de Chèque en echec = " + cheques.length + " - Montant Total en echec= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier echec = " + fileName);
            logEvent("INFO", "Nombre de Chèque en echec = " + cheques.length + " - Montant Total en echec = " + Utility.formatNumber("" + montantTotal));
            closeFile();
        }

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
