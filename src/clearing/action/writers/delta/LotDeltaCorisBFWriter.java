/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.delta;

import clearing.model.CMPUtility;
import clearing.model.EnteteLot;
import clearing.model.Operation;
import clearing.table.Cheques;
import clearing.table.Lotcom;
import clearing.table.Remcom;
import clearing.table.Remises;
import clearing.utils.StaticValues;
import java.io.File;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class LotDeltaCorisBFWriter extends FlatFileWriter {

    private String sql = "";
    private int refLot = 0;
    private Vector<EnteteLot> vEnteteLots = new Vector<EnteteLot>();
    Cheques cheques[] = null;
    Cheques cheques35[] = null;
    Remcom remcom = null;
    Lotcom lotcom = null;

    public LotDeltaCorisBFWriter() {
        setDescription("Envoi de Lot d'opération aller vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
       // String whereClause = " ETAT=" + Utility.getParam("CETAOPEVAL2"); //+ " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ";
        
        String whereClause = " ETAT=" + Utility.getParam("CETAOPEVAL2")+" AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ";


        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVAL2") + " WHERE ETAT=" + Utility.getParam("CETAOPEVAL")
                + " AND REMISE IN (SELECT IDREMISE FROM REMISES WHERE ETAT=" + Utility.getParam("CETAOPEVAL2") + ")");
        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVAL") + " WHERE ETAT=" + Utility.getParam("CETAOPESAI")
                + " AND REMISE IN (SELECT IDREMISE FROM REMISES WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + ")");
        db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEVAL2") + " WHERE ETAT=" + Utility.getParam("CETAOPEVAL")
                + " AND IDREMISE IN (SELECT REMISE FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPEVAL2") + ")");
        db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEVAL") + " WHERE ETAT=" + Utility.getParam("CETAOPESAI")
                + " AND IDREMISE IN (SELECT REMISE FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + ")");

        //Gestion Garde Chèque
        sql = "UPDATE CHEQUES SET ETAT="+ Utility.getParam("CETAOPEGARCHECON") +" WHERE GARDE='1' AND ETAT="+Utility.getParam("CETAOPEVAL2");
        db.executeUpdate(sql);
        sql="UPDATE CHEQUES SET ETAT="+Utility.getParam("CETAOPEVAL2")+" WHERE GARDE='1' AND ETAT="+ Utility.getParam("CETAOPEGARCHECONSIB") +" AND DATEECHEANCE<=TO_CHAR(SYSDATE,'YYYY/MM/DD')";
        db.executeUpdate(sql);

        //Recuperation des cheques 030
        sql = "SELECT * FROM CHEQUES WHERE  " + whereClause + " AND TYPE_CHEQUE ='030'";
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        prepareCheques(cheques);


        //Recuperation des cheques 035 
        sql = "SELECT * FROM CHEQUES WHERE  " + whereClause + "  AND TYPE_CHEQUE ='035'";
        cheques35 = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        prepareCheques(cheques35);

        
        //Sam et Eugene 
        //MAJ des cheques interne de 800 à 900
        sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALSURCAIENVSIB") + ", LOTSIB=1 WHERE ETAT=800 AND BANQUE IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ";
        db.executeUpdate(sql);
        //

        if (refLot > 0) {

            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Chèque= " + cheques35.length + " - Montant Total= " + Utility.formatNumber(vEnteteLots.get(0).getMontantTotal()));
            logEvent("INFO", "Nombre de Chèque= " + cheques35.length + " - Montant Total= " + Utility.formatNumber("" + vEnteteLots.get(0).getMontantTotal()));

        } else {
            logEvent("WARNING", "Il n'y a aucun élément disponible");
        }

        db.close();


    }

    private void feedOperationWithCheque(int j, Cheques cheque, Operation[] operations) {
        operations[j].setTypeOperation(cheque.getType_Cheque());
        operations[j].setRefOperation("" + cheque.getIdcheque());
        operations[j].setIdAgeRem(cheque.getAgenceremettant());
        operations[j].setIPac(cheque.getVilleremettant());
        operations[j].setFlagIBANCre("0");
        operations[j].setPfxIBANCre(createBlancs(4, " "));
        operations[j].setNumCheque(cheque.getNumerocheque());
        operations[j].setDateOrdreClient(Utility.convertStringToDate(cheque.getDatesaisie(), ResLoader.getMessages("patternDate")));
        operations[j].setIdBanDeb(cheque.getBanque());
        operations[j].setIdAgeDeb(cheque.getAgence());
        operations[j].setNumCptDeb(cheque.getNumerocompte());
        operations[j].setCleRibDeb(Utility.computeCleRIB(cheque.getBanque(), cheque.getAgence(), cheque.getNumerocompte()));
        operations[j].setMontant(cheque.getMontantcheque());
        operations[j].setNumCedant("" + cheque.getRemise());
        operations[j].setRefEmetteur(Utility.bourrageGauche(cheque.getCompteremettant(), 12, "0"));
        operations[j].setCodeAval((cheque.getEscompte().intValue() == 0) ? "E" : "C");
        if(cheque.getBanque().equalsIgnoreCase(Utility.getParam("CODE_BANQUE_SICA3"))) operations[j].setCodeAval("C");
        operations[j].setCodeCertif("0");
        operations[j].setBlancs(createBlancs(286, " "));
    }

    private void feedOperationWithCheque35(int j, Cheques cheque, Operation[] operations) {
        operations[j].setTypeOperation(cheque.getType_Cheque());
        operations[j].setRefOperation("" + cheque.getIdcheque());
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception ex) {
            Logger.getLogger(LotDeltaCorisBFWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        String l_sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheque.getRemise();
        Remises[] remises = (Remises[]) db.retrieveRowAsObject(l_sql, new Remises());
        db.close();
        if (remises != null && remises.length > 0) {
            operations[j].setIdAgeRem(remises[0].getAgenceDepot());
        } else {
            operations[j].setIdAgeRem(cheque.getMachinescan().substring(0, 5));
        }

        operations[j].setIdAgeCre(cheque.getAgenceremettant());
        operations[j].setIPac(cheque.getVilleremettant());
        operations[j].setFlagIBANCre("0");
        operations[j].setPfxIBANCre(createBlancs(4, " "));
        operations[j].setNumCheque(cheque.getNumerocheque());
        operations[j].setDateOrdreClient(Utility.convertStringToDate(cheque.getDatesaisie(), ResLoader.getMessages("patternDate")));
        operations[j].setIdBanDeb(cheque.getBanque());
        operations[j].setIdAgeDeb(cheque.getAgence());
        operations[j].setNumCptDeb(cheque.getNumerocompte());
        operations[j].setCleRibDeb(Utility.computeCleRIB(cheque.getBanque(), cheque.getAgence(), cheque.getNumerocompte()));
        operations[j].setMontant(cheque.getMontantcheque());
        operations[j].setNumCedant("" + cheque.getRemise());
        String compteRemettant = Utility.bourrageGauche(cheque.getCompteremettant(), 12, "0");
       // if(compteRemettant.indexOf(sql, refLot)
        operations[j].setRefEmetteur(compteRemettant);
        
        operations[j].setCodeAval((cheque.getEscompte().intValue() == 0) ? "E" : "C");
        if(cheque.getBanque().equalsIgnoreCase(Utility.getParam("CODE_BANQUE_SICA3"))) operations[j].setCodeAval("C");
        operations[j].setCodeCertif("0");
        operations[j].setAdrSouscripteur(Utility.bourrageDroite(remises[0].getReference(), 50, " "));

        operations[j].setNomSouscripteur(Utility.bourrageDroite(cheque.getCodeutilisateur(), 4, " "));
        operations[j].setBlancs(createBlancs(286, " "));
    }

    private void prepareCheques(Cheques[] cheques) throws Exception {

        if (cheques != null && cheques.length > 0) {

            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ELOT");
            enteteLot.setRefLot("" + ++refLot);
            enteteLot.setRefLot(Utility.computeCompteur("REFLOT", CMPUtility.getDate()));
            enteteLot.setRefBancaire(enteteLot.getRefLot());
            enteteLot.setTypeOperation(cheques[0].getType_Cheque());
            enteteLot.setIdBanRem(cheques[0].getBanqueremettant());
            enteteLot.setBlancs(createBlancs(26, " "));
            enteteLot.setNbOperations("" + cheques.length);
            Operation[] operations = new Operation[cheques.length];
            long montantLot = 0;
            for (int j = 0; j < cheques.length; j++) {
                Cheques cheque = cheques[j];
                operations[j] = new Operation();
                montantLot = montantLot + Long.parseLong(cheque.getMontantcheque().trim());
                operations[j].setIdObjetOrigine(cheque.getIdcheque());
                switch (Integer.parseInt(cheque.getType_Cheque())) {

                    case StaticValues.CHQ_SCAN: {
                        feedOperationWithCheque(j, cheque, operations);
                    }
                    break;
                    case StaticValues.CHQ_SCAN_SICA3: {
                        feedOperationWithCheque35(j, cheque, operations);
                    }
                    break;

                }

            }

            enteteLot.setMontantTotal("" + montantLot);
            enteteLot.operations = operations;
            vEnteteLots.add(enteteLot);

            printLotCheque(cheques, enteteLot);
        }
    }

    public void printLotCheque(Cheques[] cheques, EnteteLot enteteLot) throws Exception {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String l_sql = "";
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + cheques[0].getBanqueremettant() + ".000." + enteteLot.getRefLot() + "." + cheques[0].getType_Cheque() + "." + CMPUtility.getDevise() + ".LOT";
        setOut(createFlatFile(fileName));
        String line = printEnteteLot(enteteLot);
        writeln(line);
        for (int j = 0; j < enteteLot.operations.length; j++) {
            switch (Integer.parseInt(enteteLot.getTypeOperation())) {
                case StaticValues.CHQ_SCAN:
                    ;
                case StaticValues.CHQ_PAP: {
                    line = printCheques(enteteLot, j);
                    writeln(line);

                }
                ;
                break;
                case StaticValues.CHQ_SCAN_SICA3: {
                    line = printCheques(enteteLot, j);
                    writeln(line);
                }
                ;
                break;
            }
            l_sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALDELTA") + ", LOTSIB=1 WHERE  IDCHEQUE=" + cheques[j].getIdcheque() + " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ";
            db.executeUpdate(l_sql);

            l_sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALSURCAIENVSIB") + ", LOTSIB=1 WHERE  IDCHEQUE=" + cheques[j].getIdcheque() + " AND BANQUE IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ";
            db.executeUpdate(l_sql);

            l_sql = "UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEVALDELTA") + " WHERE IDREMISE=" + cheques[j].getRemise();
            db.executeUpdate(l_sql);
        }
        db.close();
        closeFile();
    }

    private String printCheques(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getTypeOperation()
                + enteteLot.operations[j].getRefOperation()
                + enteteLot.operations[j].getIdAgeRem()
                + enteteLot.operations[j].getIPac()
                + enteteLot.operations[j].getFlagIBANCre()
                + enteteLot.operations[j].getPfxIBANCre()
                + enteteLot.operations[j].getNumCheque()
                + Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteLot.operations[j].getIdBanDeb()
                + enteteLot.operations[j].getIdAgeDeb()
                + enteteLot.operations[j].getNumCptDeb()
                + enteteLot.operations[j].getCleRibDeb()
                + enteteLot.operations[j].getMontant()
                + enteteLot.operations[j].getNumCedant()
                + Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteLot.operations[j].getRefEmetteur()
                + enteteLot.operations[j].getCodeAval()
                + enteteLot.operations[j].getIdAgeCre()
                + createBlancs(2, " ")
                + enteteLot.operations[j].getCodeCertif()
                + enteteLot.operations[j].getAdrSouscripteur()
                + createBlancs(50, " ")
                + enteteLot.operations[j].getNomSouscripteur()
                + enteteLot.operations[j].getBlancs());
        return line;
    }

    private String printEnteteLot(EnteteLot enteteLot) throws Exception {
        String line;
        //lotcom = CMPUtility.insertLotcom(enteteLot, remcom);
        line = new String(enteteLot.getIdEntete()
                + enteteLot.getIdBanRem()
                + "000"
                + enteteLot.getRefLot()
                + CMPUtility.getDevise()
                + enteteLot.getNbOperations()
                + enteteLot.getMontantTotal()
                + enteteLot.getBlancs());
        return line;
    }
}
