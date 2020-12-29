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
public class ChequeCaisseRejetePM01BFWriter extends FlatFileWriter {

    public ChequeCaisseRejetePM01BFWriter() {
        setDescription("Envoi des rejets Cheques sur Caisse vers le SIB");
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
        String pm01FileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQCAISSE_IN_FILE_ROOTNAME1") + compteur + ".prn";

// Population
        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPESURCAIREJ") + ")  ORDER BY AGENCE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

        long sumRemiseComp = 0;

        if (cheques != null && 0 < cheques.length) {
            for (Cheques cheque : cheques) {
                sumRemiseComp += Long.parseLong(cheque.getMontantcheque());
            }
            setOut(createFlatFile(pm01FileName));

            String cptComptable, cptComptableDO, codeMouvement, refDenotage, libelleComptable, type;

            writeln(Utility.getParam("ENTPM01CHECAI")); //Nom Parametre ENTPM01CHECAI O 0

            for (Cheques cheque : cheques) {

                //Ligne de debit du compte benef
                cptComptable = cheque.getCompteremettant();
                codeMouvement = Utility.getParam("CODOPEDEBBFPM01");
                refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                libelleComptable = Utility.bourrageDroite("IMP CHQ    " + Utility.getParamLabel(cheque.getMotifrejet()), 34, " ");
                type = "0";
                if (!createLinePM01(cptComptable, Long.parseLong(cheque.getMontantcheque()), dateValeur, codeMouvement, refDenotage, libelleComptable, type)) {
                    return;
                }

                //Ligne de credit du compte do
                cptComptableDO = CMPUtility.getNumCpt(cheque.getNumerocompte().substring(1), cheque.getAgence());
                if (cptComptableDO == null) {
                    cptComptableDO = Utility.getParam("CPTINTREJCHQINT");
                }
                codeMouvement = Utility.getParam("CODOPECREBF1PM01");
                refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                libelleComptable = Utility.bourrageDroite("IMP CHQ    " + Utility.getParamLabel(cheque.getMotifrejet()), 34, " ");
                type = "0";
                if (!createLinePM01(cptComptableDO, Long.parseLong(cheque.getMontantcheque()), dateValeur, codeMouvement, refDenotage, libelleComptable, type)) {
                    return;
                }

                if (Utility.getParam("CODE_BANQUE_SICA3").equals("SN010")) {
                    //Lignes CNPP
                    //Ligne de debit du compte interne CNPP
                    System.out.println("ChequeCaisseRejetePM01BFWriter applique a SN010 ");
                    cptComptable = Utility.getParam("CPTCNPP1");
                    codeMouvement = Utility.getParam("CODOPEDEBBFPM01");
                    refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                    libelleComptable = Utility.bourrageDroite("IMP CHQ    " + Utility.getParamLabel(cheque.getMotifrejet()), 34, " ");
                    type = "0";
                    if (!createLinePM01(cptComptable, Long.parseLong(cheque.getMontantcheque()), dateValeur, codeMouvement, refDenotage, libelleComptable, type)) {
                        return;
                    }

                    //Ligne de credit du interne CNPP
                    cptComptable = Utility.getParam("CPTCNPP1");
                    if (cptComptable == null) {
                        cptComptable = Utility.getParam("CPTINTREJCHQINT");
                    }
                    codeMouvement = Utility.getParam("CODOPECREBF1PM01");
                    refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                    libelleComptable = Utility.bourrageDroite("IMP CHQ    " + Utility.getParamLabel(cheque.getMotifrejet()), 34, " ");
                    type = "0";
                    if (!createLinePM01(cptComptable, Long.parseLong(cheque.getMontantcheque()), dateValeur, codeMouvement, refDenotage, libelleComptable, type)) {
                        return;
                    }
                    System.out.println("Fin de creation lignes CNPP");

                }
                boolean priseCommission = priseCommission(cheque);
                if (priseCommission) {
                    //Ligne de debit des frais du compte BENEFICIAIRE 
                    cptComptable = cheque.getCompteremettant();
                    codeMouvement = Utility.getParam("CODOPEDEBFRAPM01");
                    refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                    libelleComptable = Utility.bourrageDroite("FRAIS IMP CHQ (COM+TAXE)" + cheque.getNumerocheque(), 34, " ");
                    type = "0";
                    if (!createLinePM01(cptComptable, Long.parseLong(Utility.getParam("MNTFRAREJCHQ1")), dateValeur, codeMouvement, refDenotage, libelleComptable, type)) {
                        return;
                    }

                    //Ligne de credit du compte de produit(commission)
                    String cptComptable2 = cheque.getCompteremettant(); //Cp Benef
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
                    if (!createLinePM01(cptComptable, Long.parseLong(Utility.getParam("MNTFRACPTTAX1")), dateValeur, codeMouvement, refDenotage, libelleComptable, type)) {
                        return;
                    }

                    /**
                     *
                     */
                    //Ligne de debit des frais du compte DO 
                    codeMouvement = Utility.getParam("CODOPEDEBFRAPM01");
                    refDenotage = Utility.bourrageGauche("" + cheque.getNumerocheque(), 8, "0");
                    libelleComptable = Utility.bourrageDroite("FRAIS IMP CHQ (COM+TAXE)" + cheque.getNumerocheque(), 34, " ");
                    type = "0";
                    if (!createLinePM01(cptComptableDO, Long.parseLong(Utility.getParam("MNTFRAREJCHQ")), dateValeur, codeMouvement, refDenotage, libelleComptable, type)) {
                        return;
                    }

                    //Ligne de credit du compte de produit(commission)
                    cptComptable2 = cptComptableDO; //Cp DO
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
                    if (!createLinePM01(cptComptable, Long.parseLong(Utility.getParam("MNTFRACPTTAX")), dateValeur, codeMouvement, refDenotage, libelleComptable, type)) {
                        return;
                    }
                }

//                cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPESURCAIREJENVSIB")));
//                db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
            }

            setDescription(getDescription() + " execute avec succes:\n Nombre de cheques = " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + sumRemiseComp));
            logEvent("INFO", "Nombre de cheques= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + sumRemiseComp));

            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPESURCAIREJENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPESURCAIREJ"));
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
        //   line.append(Utility.bourrageGauche(cptComptable, 14, "0"));
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

    private boolean createLinePM01(String cptComptable, long montantLigne, String dateValeur, String codeMouvement, String refDenotage, String libelleComptable, String type, String cptComptable2) {
        StringBuffer line;
        line = new StringBuffer();
        line.append(Utility.bourrageGauche(cptComptable.substring(0, cptComptable.length() - 2), 14, "0"));
//        line.append(Utility.bourrageGauche(cptComptable, 14, "0"));
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

    public boolean priseCommission(Cheques cheque) {
        if (cheque.getMotifrejet().equals("201")
                || cheque.getMotifrejet().equals("202")) {
            return true;
        } else {
            return false;
        }

    }
}
