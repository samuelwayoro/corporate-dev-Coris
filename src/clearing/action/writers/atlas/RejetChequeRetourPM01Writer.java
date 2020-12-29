/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.atlas;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import java.io.File;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class RejetChequeRetourPM01Writer extends FlatFileWriter {

    public RejetChequeRetourPM01Writer() {
        setDescription("Envoi des rejets cheques retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        String dateValeur = Utility.getParam("DATEVALEUR_ALLER");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        System.out.println("Date Valeur = " + dateValeur);

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String compteur;

        compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTCHQRETREJ", "CHQRET"), 4, "0");
        String pm01FileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_REJ_RET_FILE_ROOTNAME") + compteur + ".prn";

// Population
        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM2ACC") + ")  ORDER BY AGENCE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

        long sumRemiseComp = 0;

        if (cheques != null && 0 < cheques.length) {
            for (Cheques cheque : cheques) {
                sumRemiseComp += Long.parseLong(cheque.getMontantcheque());
            }
            setOut(createFlatFile(pm01FileName));

            //Ligne de credit du Total du compte interne CNPP
            String cptComptable;
            String codeMouvement;
            String refDenotage;
            String libelleComptable;
            String type;
//            Ligne 
            writeln(Utility.getParam("ENTPM01CHERETREJ")); // "118ZA"

            for (Cheques cheque : cheques) {
                //Ligne de credit du compte client
                cptComptable = CMPUtility.getNumCpt(cheque.getNumerocompte().substring(1), cheque.getAgence());
                if (cptComptable == null) {
                    cptComptable = Utility.getParam("CPTINTREJCHQRET");
                }
                codeMouvement = Utility.getParam("CODOPECREPM01");
                refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                libelleComptable = Utility.bourrageDroite("IMP CHQ    " + Utility.getParamLabel(cheque.getMotifrejet()), 34, " ");
                type = "0";
                if (!createLinePM01(cptComptable, Long.parseLong(cheque.getMontantcheque()), dateValeur, codeMouvement, refDenotage, libelleComptable, type)) {
                    return;
                }

                //Ligne de DEBIT du compte CNPP
                cptComptable = Utility.getParam("CPTCNPP");
                codeMouvement = Utility.getParam("CODOPEDEBPM01");
                refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                libelleComptable = Utility.bourrageDroite("IMP CHQ    " + Utility.getParamLabel(cheque.getMotifrejet()), 34, " ");
                type = "0";
                if (!createLinePM01(cptComptable, Long.parseLong(cheque.getMontantcheque()), dateValeur, codeMouvement, refDenotage, libelleComptable, type)) {
                    return;
                }

//                cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM2ACCENVSIB")));
//                db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
            }

            setDescription(getDescription() + " execute avec succes:\n Nombre de cheques = " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + sumRemiseComp));
            logEvent("INFO", "Nombre de cheques= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + sumRemiseComp));
            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM2ACCENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPEALLICOM2ACC"));
            closeFile();

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }

    private boolean createLinePM01(String cptComptable, long montantLigne, String dateValeur, String codeMouvement, String refDenotage, String libelleComptable, String type) {
        StringBuffer line;
        line = new StringBuffer();

        line.append(Utility.bourrageGauche(cptComptable.substring(0, cptComptable.length() - 2), 14, "0"));
        line.append("99XOF");
        line.append(Utility.bourrageGauche("" + montantLigne, 11, "0"));
        line.append(codeMouvement);
        line.append(refDenotage);
        line.append(dateValeur);
        line.append(libelleComptable);
        line.append(type);

        writeln(line.toString());
        return true;
    }

}
