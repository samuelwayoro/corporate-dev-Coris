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
public class ChequeRetourRejeteFlexCubeEBJWriter extends FlatFileWriter {

    public ChequeRetourRejeteFlexCubeEBJWriter() {
        setDescription("Envoi des rejets de chèques Retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        boolean isEcobankStandard;
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String compteur;
        if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("0")) {
            compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTREJCHQRET", "REJCHQRET"), 4, "0");
            isEcobankStandard = false;
        } else if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("2")) {
            compteur = "d" + Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTREJCHQRET", "REJCHQRET"), 3, "0");
            isEcobankStandard = false;
        } else {
            compteur = Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTREJCHQRET", "REJCHQRET"), 4, "0");
            isEcobankStandard = true;
        }

        String dateValeur = Utility.getParam("DATEVALEUR_RETOUR");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_REJ_RET_FILE_ROOTNAME") + compteur + Utility.getParam("SIB_FILE_EXTENSION");


        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM2ACC") + ") ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        if (cheques != null && 0 < cheques.length) {
            setOut(createFlatFile(fileName));
            StringBuffer line = new StringBuffer("H" + Utility.getParam("FLEXBRANCHCODE") + "UAP");
            line.append(compteur.toLowerCase());
            line.append(CMPUtility.getDate());
            if (isEcobankStandard) {
                line.append(createBlancs(79, " "));
            }
            writeln(line.toString());
            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];
                //Tous les cheques retour rejetes - ligne de credit montant sur cpt
                //Ligne 1 (Extourne du montant préalablement débité)
                line = new StringBuffer();
                line.append(CMPUtility.getNumCptExAgence(cheque.getNumerocompte(), cheque.getAgence()));
                line.append(Utility.bourrageDroite(CMPUtility.getNumCptEx(cheque.getNumerocompte(), cheque.getAgence(),"0"), 16,   " "));
                line.append(createBlancs(4, " "));
                line.append("C");
                line.append(Utility.bourrageGauche(cheque.getMontantcheque(), 16, " "));
                line.append("F57");
                line.append(dateValeur);
                line.append(Utility.bourrageGauche(cheque.getNumerocheque(), 8, " "));
                line.append(Utility.bourrageDroite(Utility.getParam("LIBREJCHQRETFLEX1"), 200, " "));
                line.append("030");
                line.append(Utility.bourrageGZero(cheque.getIdcheque().toPlainString(), 8));
                line.append(cheque.getMotifrejet());
                writeln(line.toString());

                if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM2ACC"))) {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM2ACCENVSIB")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }

                if (cheque.getMotifrejet().equalsIgnoreCase("201") || cheque.getMotifrejet().equalsIgnoreCase("202")) {
                    //Ligne de debit commission
                    String pers = ent(CMPUtility.getAcctClass(cheque.getNumerocompte(), cheque.getAgence()));
                    line = new StringBuffer();
                    line.append(CMPUtility.getNumCptExAgence(cheque.getNumerocompte(), cheque.getAgence()));
                    //Utility.bourrageDroite(CMPUtility.getNumCptEx(cheque.getNumerocompte(), cheque.getAgence()), 16,   " ")
                    line.append(Utility.bourrageDroite(CMPUtility.getNumCptEx(cheque.getNumerocompte(), cheque.getAgence(),"0"), 16,   " "));
                    line.append(createBlancs(4, " "));
                    line.append("D");
                    line.append(Utility.bourrageGauche(Utility.getParamOfType("COMDEBREJCHQRET" + pers + "FLEX", "CODE_COMMISSION"), 16, " "));
                    line.append("F03");
                    line.append(dateValeur);
                    line.append(Utility.bourrageGauche(cheque.getNumerocheque(), 8, " "));
                    line.append(Utility.bourrageDroite(Utility.getParam("LIBREJCHQRETFLEX1"), 200, " "));
                    line.append("030");
                    line.append(Utility.bourrageGZero(cheque.getIdcheque().toPlainString(), 8));
                    line.append(cheque.getMotifrejet());
                    writeln(line.toString());

                    //Ligne de credit commission
                    line = new StringBuffer();
                    line.append(Utility.getParam("FLEXMAINBRANCH"));
                    line.append(Utility.bourrageDroite(Utility.getParam("CPTCRECOMREJCHQRET1"), 16, " "));
                    line.append(createBlancs(4, " "));
                    line.append("C");
                    line.append(Utility.bourrageGauche(Utility.getParamOfType("COMCREREJCHQRET" + pers + "FLEX1", "CODE_COMMISSION"), 16, " "));
                    line.append("F57");
                    line.append(dateValeur);
                    line.append(createBlancs(8, " "));
                    line.append(Utility.bourrageDroite(Utility.getParam("LIBREJCHQRETFLEX2"), 200, " "));
                    line.append(createBlancs(14, " "));
                    writeln(line.toString());

                    //Ligne de credit com2
                    line = new StringBuffer();
                    line.append(Utility.getParam("FLEXMAINBRANCH"));
                    line.append(Utility.bourrageDroite(Utility.getParam("CPTCRECOMREJCHQRET2"), 16, " "));
                    line.append(createBlancs(4, " "));
                    line.append("C");
                    line.append(Utility.bourrageGauche(Utility.getParamOfType("COMCREREJCHQRET" + pers + "FLEX2", "CODE_COMMISSION"), 16, " "));
                    line.append("F57");
                    line.append(dateValeur);
                    line.append(createBlancs(8, " "));
                    line.append(Utility.bourrageDroite(Utility.getParam("LIBREJCHQRETFLEX3"), 200, " "));
                    line.append(createBlancs(14, " "));
                    writeln(line.toString());
                }
                montantTotal += Long.parseLong(cheque.getMontantcheque());
            }
// Gestion cpt globalisation
            line = new StringBuffer();
            line.append(Utility.getParam("FLEXMAINBRANCH"));
            line.append(Utility.bourrageDroite(Utility.getParam("CPTDEBREJCHQRET"), 16, " "));
            line.append(createBlancs(4, " "));
            line.append("D");
            line.append(Utility.bourrageGauche("" + montantTotal, 16, " "));
            line.append("Q11");
            line.append(dateValeur);
            line.append(createBlancs(8, " "));
            line.append(Utility.bourrageDroite(Utility.getParam("LIBREJCHQRETFLEX4"), 200, " "));
            line.append(createBlancs(14, " "));
            writeln(line.toString());



            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));
            closeFile();
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }

    private String ent(String acctClass) {
        if (Utility.getParam(acctClass) != null) {
            return Utility.getParam(acctClass).equalsIgnoreCase("PERSPHYS") ? "" : "ENT";
        } else {
            return "";
        }
    }
}
