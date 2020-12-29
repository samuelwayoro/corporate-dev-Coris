/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.atlas;

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
public class ChequeAllerRejetePM01BFWriter extends FlatFileWriter {

    public ChequeAllerRejetePM01BFWriter() {
        setDescription("Envoi des cheques aller rejetes vers le SIB");
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

        compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTCHQALLREJ", "CHQALE"), 4, "0");
        String pm01FileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_REJ_FILE_ROOTNAME") + compteur + ".prn";

// Population
        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEREJRET") + ")  ORDER BY AGENCEREMETTANT";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

        long sumRemiseComp = 0;

        if (cheques != null && 0 < cheques.length) {
            for (Cheques cheque : cheques) {
                sumRemiseComp += Long.parseLong(cheque.getMontantcheque());
            }
            setOut(createFlatFile(pm01FileName));
            writeln(Utility.getParam("ENTPM01CHEALLREJ")); //Utility.getParam("ENTPMO1CHEALLREJ")    118ZB

            //Ligne de debit du Total du compte interne CNPP
            String cptComptable = Utility.getParam("CPTCNPP");
            String codeMouvement = Utility.getParam("CODOPEDEBPM01");
            String refDenotage = dateValeur;
            String libelleComptable = Utility.bourrageDroite("IMP CHQ    CHEQUE REJETES PAR LES CONFRERES", 34, " ");
            String type = "0";
            if (!Utility.getParam("CODE_BANQUE_SICA3").equals("SN010")) {

                if (!createLinePM01(cptComptable, sumRemiseComp, dateValeur, codeMouvement, refDenotage, libelleComptable, type, null)) {
                    return;
                }

                //Ligne de credit du Total du compte VAI
                cptComptable = Utility.getParam("CPTVAICHACOM");
                codeMouvement = Utility.getParam("CODOPECREPM01");
                refDenotage = dateValeur;
                libelleComptable = Utility.bourrageDroite("IMP CHQ    CHEQUE REJETES PAR LES CONFRERES", 34, " ");
                type = "0";
                if (!createLinePM01(cptComptable, sumRemiseComp, dateValeur, codeMouvement, refDenotage, libelleComptable, type, null)) {
                    return;
                }
            }
            for (Cheques cheque : cheques) {
                //Ligne de debit du compte client
                cptComptable = cheque.getCompteremettant();
                codeMouvement = Utility.getParam("CODOPEDEBBFPM01");//160
                refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                libelleComptable = Utility.bourrageDroite("IMP CHQ    " + Utility.getParamLabel(cheque.getMotifrejet()), 34, " ");
                type = "0";
                if (!createLinePM01(cptComptable, Long.parseLong(cheque.getMontantcheque()), dateValeur, codeMouvement, refDenotage, libelleComptable, type, null)) {
                    return;
                }

                //Ligne de credit du compte CNPP
                cptComptable = Utility.getParam("CPTCNPP");
                codeMouvement = Utility.getParam("CODOPECREPM01");
                refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                libelleComptable = Utility.bourrageDroite("IMP CHQ    " + Utility.getParamLabel(cheque.getMotifrejet()), 34, " ");
                type = "0";
                if (!createLinePM01(cptComptable, Long.parseLong(cheque.getMontantcheque()), dateValeur, codeMouvement, refDenotage, libelleComptable, type, null)) {
                    return;
                }
                //prise de commission
                boolean priseCommission = priseCommission(cheque);

                if (priseCommission) {

                    if (Utility.getParam("CODE_BANQUE_SICA3").equals("SN010")) {
                        //Ligne de debit des frais du compte client
                        cptComptable = cheque.getCompteremettant();
                        codeMouvement = Utility.getParam("CODOPEDEBFRAPM01");
                        refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                        libelleComptable = Utility.bourrageDroite("FRAIS IMP CHQ (COM+TAXE)" + cheque.getNumerocheque(), 34, " ");
                        type = "0";
                        if (!createLinePM01(cptComptable, Long.parseLong(Utility.getParam("MNTFRAREJCHQ1")), dateValeur, codeMouvement, refDenotage, libelleComptable, type, null)) {
                            return;
                        }

                        //Ligne de credit du compte de produit(commission)
                        String cptComptable2 = cheque.getCompteremettant();
                        cptComptable = cptComptable2.substring(0, 5) + Utility.getParam("CPTPCE");
                        codeMouvement = Utility.getParam("CODOPECREPM01");
                        refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                        libelleComptable = Utility.bourrageDroite("FRAIS IMP CHQ " + cheque.getNumerocheque(), 34, " ");
                        type = "2";

                        if (!createLinePM01(cptComptable, Long.parseLong(Utility.getParam("MNTFRACPTPCE1")), dateValeur, codeMouvement, refDenotage, libelleComptable, type, cptComptable2)) {
                            return;
                        }

                        //Ligne de credit du compte de taxe(commission)
                        cptComptable = cptComptable2.substring(0, 5) + Utility.getParam("CPTTAX");
                        codeMouvement = Utility.getParam("CODOPECREPM01");
                        refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                        libelleComptable = Utility.bourrageDroite("FRAIS IMP CHQ " + cheque.getNumerocheque(), 34, " ");
                        type = "0";
                        if (!createLinePM01(cptComptable, Long.parseLong(Utility.getParam("MNTFRACPTTAX1")), dateValeur, codeMouvement, refDenotage, libelleComptable, type, null)) {
                            return;
                        }
                    } else {
                        //Ligne de debit des frais du compte client
                        cptComptable = cheque.getCompteremettant();
                        codeMouvement = Utility.getParam("CODOPEDEBFRAPM01");
                        refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                        libelleComptable = Utility.bourrageDroite("FRAIS IMP CHQ (COM+TAXE)" + cheque.getNumerocheque(), 34, " ");
                        type = "0";
                        if (!createLinePM01(cptComptable, Long.parseLong(Utility.getParam("MNTFRAREJCHQ")), dateValeur, codeMouvement, refDenotage, libelleComptable, type, null)) {
                            return;
                        }

                        //Ligne de credit du compte de produit(commission)
                        String cptComptable2 = cheque.getCompteremettant();
                        cptComptable = cptComptable2.substring(0, 5) + Utility.getParam("CPTPCE");
                        codeMouvement = Utility.getParam("CODOPECREPM01");
                        refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                        libelleComptable = Utility.bourrageDroite("FRAIS IMP CHQ " + cheque.getNumerocheque(), 34, " ");
                        type = "2";

                        if (!createLinePM01(cptComptable, Long.parseLong(Utility.getParam("MNTFRACPTPCE")), dateValeur, codeMouvement, refDenotage, libelleComptable, type, cptComptable2)) {
                            return;
                        }

                        //Ligne de credit du compte de taxe(commission)
                        cptComptable = cptComptable2.substring(0, 5) + Utility.getParam("CPTTAX");
                        codeMouvement = Utility.getParam("CODOPECREPM01");
                        refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                        libelleComptable = Utility.bourrageDroite("FRAIS IMP CHQ " + cheque.getNumerocheque(), 34, " ");
                        type = "0";
                        if (!createLinePM01(cptComptable, Long.parseLong(Utility.getParam("MNTFRACPTTAX")), dateValeur, codeMouvement, refDenotage, libelleComptable, type, null)) {
                            return;
                        }
                    }

                }
//                cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEREJRETENVSIB")));
//                db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
            }

            setDescription(getDescription() + " execute avec succes:\n Nombre de cheques = " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + sumRemiseComp));
            logEvent("INFO", "Nombre de cheques= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + sumRemiseComp));
            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEREJRETENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPEREJRET"));
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

    public static boolean priseCommission(Cheques cheque) {
        if (cheque.getMotifrejet().equals("201")
                || cheque.getMotifrejet().equals("202")) {
            return true;
        } else {
            return false;
        }

    }
}
