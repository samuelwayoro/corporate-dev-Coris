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
public class RejetChequeRetourPM01SNWriter extends FlatFileWriter {

    public RejetChequeRetourPM01SNWriter() {
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
                cptComptable = Utility.getParam("CPTCNPP1");
                codeMouvement = Utility.getParam("CODOPEDEBPM01");
                refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                libelleComptable = Utility.bourrageDroite("IMP CHQ    " + Utility.getParamLabel(cheque.getMotifrejet()), 34, " ");
                type = "0";
                if (!createLinePM01(cptComptable, Long.parseLong(cheque.getMontantcheque()), dateValeur, codeMouvement, refDenotage, libelleComptable, type)) {
                    return;
                }
             
                //Frais SN010
                if (Utility.getParam("CODE_BANQUE_SICA3").equals("SN010")) {
                    //Prise de Frais
                    //Prise de frais sur le client
                    //prise de commission
                    boolean priseCommission = priseCommission(cheque);
                    if (priseCommission) {
                        //Ligne de debit des frais du compte client
                        cptComptable = CMPUtility.getNumCpt(cheque.getNumerocompte().substring(1), cheque.getAgence()) ;
                        System.out.println("debit des frais du compte client  cptComptable " + cptComptable);
                        codeMouvement = Utility.getParam("CODOPECRESNPM01");
                        refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                        libelleComptable = Utility.bourrageDroite("FRAIS IMP CHQ (COM+TAXE)" + cheque.getNumerocheque(), 34, " ");
                        type = "0";
                        if (!createLinePM01(cptComptable, Long.parseLong(Utility.getParam("MNTFRAREJCHQ")), dateValeur, codeMouvement, refDenotage, libelleComptable, type, null)) {
                            return;
                        }

                        //Ligne de credit du compte de produit(commission)
                        String cptComptable2 = CMPUtility.getNumCpt(cheque.getNumerocompte().substring(1), cheque.getAgence()) ;
                        System.out.println("credit du compte de produit  cptComptable2 " + cptComptable2);
                        cptComptable = cptComptable2.substring(0, 5) + Utility.getParam("CPTPCE");
                        System.out.println("credit du compte de produit  cptComptable" + cptComptable);
                        codeMouvement = Utility.getParam("CODOPEDEBSNPM01");
                        refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                        libelleComptable = Utility.bourrageDroite("FRAIS IMP CHQ " + cheque.getNumerocheque(), 34, " ");
                        type = "2";

                        if (!createLinePM01(cptComptable, Long.parseLong(Utility.getParam("MNTFRACPTPCE")), dateValeur, codeMouvement, refDenotage, libelleComptable, type, cptComptable2)) {
                            return;
                        }

                        //Ligne de credit du compte de taxe(commission)
                        cptComptable = cptComptable2.substring(0, 5) + Utility.getParam("CPTTAX");
                        System.out.println("credit du compte de taxe  cptComptable2 " + cptComptable2);
                        System.out.println("credit du compte de taxe  cptComptable" + cptComptable);
                        codeMouvement = Utility.getParam("CODOPEDEBSNPM01");
                        refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                        libelleComptable = Utility.bourrageDroite("FRAIS IMP CHQ " + cheque.getNumerocheque(), 34, " ");
                        type = "0";
                        if (!createLinePM01(cptComptable, Long.parseLong(Utility.getParam("MNTFRACPTTAX")), dateValeur, codeMouvement, refDenotage, libelleComptable, type, null)) {
                            return;
                        }

                    }

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

    private boolean createLinePM01(String cptComptable, long montantLigne, String dateValeur, String codeMouvement, String refDenotage, String libelleComptable, String type, String cptComptable2) {
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
        if (type.endsWith("2")) {
            line.append(cptComptable2.substring(0, cptComptable2.length() - 2));
            line.append("XOF");

        }

        writeln(line.toString());
        return true;
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

    public static boolean priseCommission(Cheques cheque) {
        if (cheque.getMotifrejet().equals("201")
                || cheque.getMotifrejet().equals("202")) {
            return true;
        } else {
            return false;
        }

    }
}
