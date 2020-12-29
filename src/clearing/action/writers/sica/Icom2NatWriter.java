/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.sica;

import clearing.model.CMPUtility;
import clearing.model.EnteteLot;
import clearing.model.EnteteRemise;
import clearing.model.Operation;
import clearing.model.RIO;
import clearing.table.Banques;
import clearing.table.Cheques;
import clearing.table.Effets;
import clearing.table.Lotcom;
import clearing.table.Prelevements;
import clearing.table.Remcom;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Vector;
import org.patware.action.file.FlatFileWriter;
import static org.patware.action.file.FlatFileWriter.createBlancs;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class Icom2NatWriter extends FlatFileWriter {

    private String sql = "";
    private int refLot = 0;
    private Vector<EnteteLot> vEnteteLots = new Vector<EnteteLot>();
    private String sequence;
    EnteteRemise enteteRemise = new EnteteRemise();
    Cheques cheques[] = null;
    Cheques cheques35[] = null;
    Effets effets40[] = null;
    Effets effets41[] = null;
    Effets effets42[] = null;
    Effets effets43[] = null;
    Effets effets45[] = null;
    Effets effets46[] = null;
    Prelevements prelevements[] = null;
    Remcom remcom = null;
    Lotcom lotcom = null;
    private boolean remiseHasCheques = false;
    private boolean remiseHasEffets = false;
    private boolean remiseHasPrelevements = false;

    public Icom2NatWriter() {

        setDescription("Envoi ICOM2 National vers la BCEAO ");
    }

    @Override
    public void execute() throws Exception {

        super.execute();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        if (Utility.getParam("ENVOI_ICOM_NAT") == null || Utility.getParam("ENVOI_ICOM_NAT").equals("0")) {
            if (Utility.getParam("ATTENTE_ICOMA_NAT") == null || Utility.getParam("ATTENTE_ICOMA_NAT").equals("0")) {

                //Recuperation des Banques NATIONALES
                Banques banques[] = CMPUtility.getBanquesRemettantesNationales(db, " ETAT = " + Utility.getParam("CETAOPEALLICOM2"));
                if (banques != null) {
                    if (Utility.getParam("ENVOI_ICOM_NAT") != null) {
                        db.executeUpdate("UPDATE PARAMS SET VALEUR='1' WHERE NOM='ENVOI_ICOM_NAT'");
                        Utility.clearParamsCache();
                    }
                    for (int i = 0; i < banques.length; i++) {
                        Banques banque = banques[i];
                        String motifRejetsClause = " AND MOTIFREJET IN (SELECT " + ResLoader.getMessages("trimFunction") + "(NOM) FROM PARAMS WHERE TYPE = 'CODE_REJET')";
                        //Recuperation des cheques d'une Banque
                        sql = "SELECT * FROM CHEQUES WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM2") + " AND TYPE_CHEQUE ='030'  AND BANQUEREMETTANT LIKE '" + banque.getCodebanque() + "' " + motifRejetsClause;
                        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                        prepareCheques(cheques);
                        //Recuperation des cheques 035 d'une Banque
                        sql = "SELECT * FROM CHEQUES WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM2") + " AND TYPE_CHEQUE ='035' AND BANQUEREMETTANT LIKE '" + banque.getCodebanque() + "'" + motifRejetsClause;
                        cheques35 = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                        prepareCheques(cheques35);

                        //Recuperation des effets d'une Banque
                        sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM2") + " AND TYPE_EFFET ='045' AND BANQUEREMETTANT LIKE '" + banque.getCodebanque() + "'" + motifRejetsClause;
                        effets45 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                        prepareEffets(effets45);
//                //Recuperation des effets d'une Banque
                        sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM2") + " AND TYPE_EFFET ='040' AND BANQUEREMETTANT LIKE '" + banque.getCodebanque() + "'" + motifRejetsClause;
                        effets40 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                        prepareEffets(effets40);
                        //Recuperation des effets d'une Banque
                        sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM2") + " AND TYPE_EFFET ='041' AND BANQUEREMETTANT LIKE '" + banque.getCodebanque() + "'" + motifRejetsClause;
                        effets41 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                        prepareEffets(effets41);
                        //Recuperation des effets d'une Banque
                        sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM2") + " AND TYPE_EFFET ='042' AND BANQUEREMETTANT LIKE '" + banque.getCodebanque() + "'" + motifRejetsClause;
                        effets42 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                        prepareEffets(effets42);
                        //Recuperation des effets d'une Banque
                        sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM2") + " AND TYPE_EFFET ='043' AND BANQUEREMETTANT LIKE '" + banque.getCodebanque() + "'" + motifRejetsClause;
                        effets43 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                        prepareEffets(effets43);
                        //Recuperation des effets d'une Banque
                        sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM2") + " AND TYPE_EFFET ='046' AND BANQUEREMETTANT LIKE '" + banque.getCodebanque() + "'" + motifRejetsClause;
                        effets46 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                        prepareEffets(effets46);

                        //Recuperation des prelevements d'une Banque
                        sql = "SELECT * FROM PRELEVEMENTS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM2") + " AND TYPE_PRELEVEMENT ='025'  AND BANQUEREMETTANT LIKE '" + banque.getCodebanque() + "' " + motifRejetsClause;
                        prelevements = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());
                        preparePrelevements(prelevements);
                    }

                }

                if (refLot > 0) {
                    printRemise(db);
                    updateEtatOperations(db);
                    if (Utility.getParam("ENVOI_ICOM_NAT") != null) {
                        db.executeUpdate("UPDATE PARAMS SET VALEUR='0' WHERE NOM='ENVOI_ICOM_NAT'");
                        Utility.clearParamsCache();
                    }

                    if (Utility.getParam("ATTENTE_ICOMA_NAT") != null) {
                        db.executeUpdate("UPDATE PARAMS SET VALEUR='1' WHERE NOM='ATTENTE_ICOMA_NAT'");
                        Utility.clearParamsCache();
                    }

                } else {
                    if (Utility.getParam("ENVOI_ICOM_NAT") != null) {
                        db.executeUpdate("UPDATE PARAMS SET VALEUR='0' WHERE NOM='ENVOI_ICOM_NAT'");
                        Utility.clearParamsCache();
                    }
                    logEvent("WARNING", "Il n'y a aucun element disponible");
                    setDescription(getDescription() + " - WARNING: Il n'y a aucun element disponible");

                }
            } else {
                logEvent("WARNING", "Le Système est en attente d'un ICOMA|ATTENTE_ICOMA_NAT=1");
                setDescription(getDescription() + " - WARNING: Le Système est en attente d'un ICOMA|ATTENTE_ICOMA_NAT=1");
            }
        } else {
            logEvent("WARNING", "Un ICOM2 est en cours d'envoi|ENVOI_ICOM_NAT=1");
            setDescription(getDescription() + " - WARNING:Un ICOM2 est en cours d'envoi|ENVOI_ICOM_NAT=1");
        }

        db.close();

    }

    private void preparePrelevements(Prelevements[] prelevements) throws NumberFormatException, SQLException {

        if (prelevements != null && prelevements.length > 0) {
            remiseHasPrelevements = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ELOT");
            enteteLot.setRefLot("" + ++refLot);
            enteteLot.setRefBancaire(enteteLot.getRefLot());

            enteteLot.setTypeOperation(prelevements[0].getType_prelevement().replaceFirst("0", "1"));//rejet
            enteteLot.setIdBanRem(prelevements[0].getBanqueremettant());
            enteteLot.setBlancs(createBlancs(24, " "));
            enteteLot.setNbOperations("" + prelevements.length);
            Operation[] operations = new Operation[prelevements.length];

            for (int j = 0; j < prelevements.length; j++) {
                Prelevements prelevement = prelevements[j];
                operations[j] = new Operation();

                operations[j].setTypeOperation(prelevement.getType_prelevement().replaceFirst("0", "1"));
                operations[j].setRefOperation(prelevement.getReference_Operation_Interne().trim());
                operations[j].setIdAgeRem(prelevement.getAgenceremettant());
                operations[j].setRioOperInitial(new RIO(prelevement.getRio()));
                operations[j].setMotifRejet(prelevement.getMotifrejet());
                operations[j].setBlancs(createBlancs(10, " "));
                operations[j].setIdObjetOrigine(prelevement.getIdprelevement());
            }

            enteteLot.setMontantTotal(createBlancs(16, " "));
            enteteLot.operations = operations;
            vEnteteLots.add(enteteLot);
        }
    }

    private void prepareCheques(Cheques[] cheques) throws NumberFormatException, SQLException {

        if (cheques != null && cheques.length > 0) {
            remiseHasCheques = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ELOT");
            enteteLot.setRefLot("" + ++refLot);
            enteteLot.setRefBancaire(enteteLot.getRefLot());
            enteteLot.setTypeOperation(cheques[0].getType_Cheque().replaceFirst("0", "1"));
            enteteLot.setIdBanRem(cheques[0].getBanqueremettant());
            enteteLot.setBlancs(createBlancs(24, " "));
            enteteLot.setNbOperations("" + cheques.length);
            Operation[] operations = new Operation[cheques.length];

            for (int j = 0; j < cheques.length; j++) {
                Cheques cheque = cheques[j];
                operations[j] = new Operation();

                operations[j].setTypeOperation(cheque.getType_Cheque().replaceFirst("0", "1"));
                operations[j].setRefOperation(cheque.getReference_Operation_Interne().trim());
                operations[j].setIdAgeRem(cheque.getAgenceremettant());
                operations[j].setRioOperInitial(new RIO(cheque.getRio()));
                operations[j].setMotifRejet(cheque.getMotifrejet());
                operations[j].setBlancs(createBlancs(10, " "));
                operations[j].setIdObjetOrigine(cheque.getIdcheque());
            }

            enteteLot.setMontantTotal(createBlancs(16, " "));
            enteteLot.operations = operations;
            vEnteteLots.add(enteteLot);
        }
    }

    private void prepareEffets(Effets[] effets) throws SQLException {
        if (effets != null && effets.length > 0) {
            remiseHasEffets = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ELOT");
            enteteLot.setRefLot("" + ++refLot);
            enteteLot.setRefBancaire(enteteLot.getRefLot());
            enteteLot.setTypeOperation(effets[0].getType_Effet().replaceFirst("0", "1"));
            enteteLot.setIdBanRem(effets[0].getBanqueremettant());
            enteteLot.setBlancs(createBlancs(24, " "));
            enteteLot.setNbOperations("" + effets.length);
            Operation[] operations = new Operation[effets.length];

            for (int j = 0; j < effets.length; j++) {
                Effets effet = effets[j];
                operations[j] = new Operation();

                operations[j].setTypeOperation(effet.getType_Effet().replaceFirst("0", "1"));
                operations[j].setRefOperation(effet.getReference_Operation_Rejet().trim());
                operations[j].setIdAgeRem(effet.getAgenceremettant());

                operations[j].setRioOperInitial(new RIO(effet.getRio()));
                operations[j].setMotifRejet(effet.getMotifrejet());
                operations[j].setBlancs(createBlancs(10, " "));
                operations[j].setIdObjetOrigine(effet.getIdeffet());
            }

            enteteLot.setMontantTotal(createBlancs(16, " "));
            enteteLot.operations = operations;
            vEnteteLots.add(enteteLot);
        }
    }

    private String printRejets(EnteteRemise enteteRemise, int i, int j) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation()
                + enteteRemise.enteteLots[i].operations[j].getRefOperation()
                + enteteRemise.enteteLots[i].operations[j].getIdAgeRem()
                + enteteRemise.enteteLots[i].operations[j].getRioOperInitial()
                + enteteRemise.enteteLots[i].operations[j].getMotifRejet()
                + enteteRemise.enteteLots[i].operations[j].getBlancs());
        return line;
    }

    private void printRemise(DataBase db) throws Exception {
        String line = "";
        String l_sql = "";
        line = printEnteteRemise(enteteRemise);
        writeln(line);

        remcom = CMPUtility.insertRemcom(enteteRemise, Integer.parseInt(Utility.getParam("CETAOPEALLPREICOM2")));
        for (int i = 0; i < enteteRemise.enteteLots.length; i++) {
            line = printEnteteLot(enteteRemise, remcom, i);
            writeln(line);
            for (int j = 0; j < enteteRemise.enteteLots[i].operations.length; j++) {
                line = printRejets(enteteRemise, i, j);
                writeln(line);
                RIO rio;
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    rio = new RIO(CMPUtility.getCodeBanque() + CMPUtility.getCodeBanque().charAt(0) + enteteRemise.enteteLots[i].operations[j].getIPac() + CMPUtility.getDevise() + sequence + Utility.getParam("DATECOMPENS_NAT") + enteteRemise.enteteLots[i].getRefBancaire() + enteteRemise.enteteLots[i].operations[j].getRefOperation());
                } else {
                    rio = new RIO(CMPUtility.getCodeBanqueSica3() + CMPUtility.getPacSCMPSICA3() + CMPUtility.getDevise() + sequence + Utility.getParam("DATECOMPENS_NAT") + enteteRemise.enteteLots[i].getRefBancaire() + enteteRemise.enteteLots[i].operations[j].getRefOperation());
                }
                if (enteteRemise.enteteLots[i].getTypeOperation().startsWith("13")) {
                    l_sql = "UPDATE CHEQUES SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ", RIO_REJET='" + rio.getRio() + "',ETAT=" + Utility.getParam("CETAOPEALLPREICOM2") + " WHERE IDCHEQUE =" + enteteRemise.enteteLots[i].operations[j].getIdObjetOrigine();
                } else if (enteteRemise.enteteLots[i].getTypeOperation().startsWith("14")) {
                    l_sql = "UPDATE EFFETS SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ", RIO_REJET='" + rio.getRio() + "',ETAT=" + Utility.getParam("CETAOPEALLPREICOM2") + " WHERE IDEFFET =" + enteteRemise.enteteLots[i].operations[j].getIdObjetOrigine();
                } else if (enteteRemise.enteteLots[i].getTypeOperation().startsWith("12")) {
                    l_sql = "UPDATE PRELEVEMENTS SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ", RIO_REJET='" + rio.getRio() + "',ETAT=" + Utility.getParam("CETAOPEALLPREICOM2") + " WHERE IDPRELEVEMENT =" + enteteRemise.enteteLots[i].operations[j].getIdObjetOrigine();
                }

                db.executeUpdate(l_sql);
            }
        }

        writeEOF("FREM", createBlancs(28, " "));
    }

    private String printEnteteLot(EnteteRemise enteteRemise, Remcom remcom, int i) throws Exception {
        String line;
        lotcom = CMPUtility.insertLotcom(enteteRemise.enteteLots[i], remcom);
        line = new String(enteteRemise.enteteLots[i].getIdEntete() + enteteRemise.enteteLots[i].getRefLot() + enteteRemise.enteteLots[i].getRefBancaire() + enteteRemise.enteteLots[i].getTypeOperation() + enteteRemise.enteteLots[i].getIdBanRem() + enteteRemise.enteteLots[i].getNbOperations() + enteteRemise.enteteLots[i].getMontantTotal() + enteteRemise.enteteLots[i].getBlancs());
        return line;
    }

    private String printEnteteRemise(EnteteRemise enteteRemise) throws Exception {
        String line;

        enteteRemise.enteteLots = vEnteteLots.toArray(new EnteteLot[vEnteteLots.size()]);

        enteteRemise.setIdEntete("EREM");
        if (Utility.getParam("VERSION_SICA").equals("2")) {
            enteteRemise.setIdEmetteur(CMPUtility.getCodeBanque().charAt(0) + "SCPM");
        } else {
            enteteRemise.setIdEmetteur(CMPUtility.getCodeBanqueSica3().substring(0, 2) + Utility.getParam("CONSTANTE_SICA").trim());
        }
        sequence = Utility.bourrageGZero(Utility.computeCompteur("ICOM_NAT", Utility.getParam("DATECOMPENS_NAT")), 3);
        enteteRemise.setRefRemise(sequence);
        enteteRemise.setDatePresentation(Utility.convertStringToDate(Utility.getParam("DATECOMPENS_NAT"), "yyyyMMdd"));
        if (Utility.getParam("VERSION_SICA").equals("2")) {
            enteteRemise.setIdRecepeteur(CMPUtility.getCodeBanque());
        } else {
            enteteRemise.setIdRecepeteur(CMPUtility.getCodeBanqueSica3());
        }
        enteteRemise.setDevise(CMPUtility.getDevise());
        enteteRemise.setTypeRemise("ICOM2");
        enteteRemise.setRefRemRelatif("000");
        enteteRemise.setNbLots("" + refLot);
        enteteRemise.setBlancs(createBlancs(15, " "));

        String fileName = CMPUtility.getNatFileName(sequence, enteteRemise.getTypeRemise());
        setOut(createFlatFile(fileName));
        line = new String(enteteRemise.getIdEntete() + enteteRemise.getIdEmetteur() + enteteRemise.getRefRemise() + Utility.convertDateToString(enteteRemise.getDatePresentation(), "yyyyMMdd") + enteteRemise.getIdRecepeteur() + enteteRemise.getDevise() + enteteRemise.getTypeRemise() + enteteRemise.getRefRemRelatif() + enteteRemise.getNbLots() + enteteRemise.getBlancs());
        return line;
    }

    private void updateEtatOperations(DataBase db) throws SQLException {
        String l_sql = "";
        remcom.setEtat(new BigDecimal(Integer.parseInt(Utility.getParam("CETAOPEALLICOM2ENV"))));
        db.updateRowByObjectByQuery(remcom, "REMCOM", "IDREMCOM=" + remcom.getIdremcom());
        l_sql = "UPDATE LOTCOM SET ETAT=" + Utility.getParam("CETAOPEALLICOM2ENV") + " WHERE IDREMCOM=" + remcom.getIdremcom();
        db.executeUpdate(l_sql);
        if (remiseHasCheques) {
            l_sql = "UPDATE CHEQUES SET DATECOMPENSATION='" + Utility.convertDateToString(Utility.convertStringToDate(Utility.getParam("DATECOMPENS_NAT"), "yyyyMMdd"), ResLoader.getMessages("patternDate")) + "',ETAT=" + Utility.getParam("CETAOPEALLICOM2ENV") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLPREICOM2");
            db.executeUpdate(l_sql);
        }
        if (remiseHasEffets) {
            l_sql = "UPDATE EFFETS SET DATECOMPENSATION='" + Utility.convertDateToString(Utility.convertStringToDate(Utility.getParam("DATECOMPENS_NAT"), "yyyyMMdd"), ResLoader.getMessages("patternDate")) + "',ETAT=" + Utility.getParam("CETAOPEALLICOM2ENV") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLPREICOM2");
            db.executeUpdate(l_sql);
        }
        if (remiseHasPrelevements) {
            l_sql = "UPDATE PRELEVEMENTS SET DATECOMPENSATION='" + Utility.convertDateToString(Utility.convertStringToDate(Utility.getParam("DATECOMPENS_NAT"), "yyyyMMdd"), ResLoader.getMessages("patternDate")) + "',ETAT=" + Utility.getParam("CETAOPEALLICOM2ENV") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLPREICOM2");
            db.executeUpdate(l_sql);
        }

    }
}
