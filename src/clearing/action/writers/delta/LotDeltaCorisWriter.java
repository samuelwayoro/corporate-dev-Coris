/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.delta;

/**
 *
 * @author samuel
 */
import java.util.Date;
import java.io.File;
import clearing.model.CMPUtility;
import clearing.table.Remises;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.patware.utils.ResLoader;
import clearing.model.Operation;
import org.patware.utils.Utility;
import org.patware.jdbc.DataBase;
import org.patware.xml.JDBCXmlReader;
import clearing.table.Lotcom;
import clearing.table.Remcom;
import clearing.table.Cheques;
import clearing.model.EnteteLot;
import java.util.Vector;
import org.patware.action.file.FlatFileWriter;

public class LotDeltaCorisWriter extends FlatFileWriter{
    private String sql;
    private int refLot;
    private Vector<EnteteLot> vEnteteLots;
    Cheques[] cheques;
    Cheques[] cheques35;
    Remcom remcom;
    Lotcom lotcom;
    
    public LotDeltaCorisWriter() {
        this.sql = "";
        this.refLot = 0;
        this.vEnteteLots = new Vector<EnteteLot>();
        this.cheques = null;
        this.cheques35 = null;
        this.remcom = null;
        this.lotcom = null;
        this.setDescription("Envoi de Lot d'op\u00e9ration aller vers le SIB");
    }
    
    public void execute() throws Exception {
        super.execute();
        final DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        final String whereClause = " ETAT=" + Utility.getParam("CETAOPEVAL2");
        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVAL2") + " WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND REMISE IN (SELECT IDREMISE FROM REMISES WHERE ETAT=" + Utility.getParam("CETAOPEVAL2") + ")");
        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVAL") + " WHERE ETAT=" + Utility.getParam("CETAOPESAI") + " AND REMISE IN (SELECT IDREMISE FROM REMISES WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + ")");
        db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEVAL2") + " WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND IDREMISE IN (SELECT REMISE FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPEVAL2") + ")");
        db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEVAL") + " WHERE ETAT=" + Utility.getParam("CETAOPESAI") + " AND IDREMISE IN (SELECT REMISE FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + ")");
        db.executeUpdate(this.sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEGARCHECON") + " WHERE GARDE='1' AND ETAT=" + Utility.getParam("CETAOPEVAL2"));
        db.executeUpdate(this.sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVAL2") + " WHERE GARDE='1' AND ETAT=" + Utility.getParam("CETAOPEGARCHECONSIB") + " AND DATEECHEANCE<=TO_CHAR(SYSDATE,'YYYY/MM/DD')");
        this.sql = "SELECT * FROM CHEQUES WHERE  " + whereClause + " AND TYPE_CHEQUE ='030'";
        this.prepareCheques(this.cheques = (Cheques[])db.retrieveRowAsObject(this.sql, (Object)new Cheques()));
        this.sql = "SELECT * FROM CHEQUES WHERE  " + whereClause + "  AND TYPE_CHEQUE ='035'";
        this.prepareCheques(this.cheques35 = (Cheques[])db.retrieveRowAsObject(this.sql, (Object)new Cheques()));
        if (this.refLot > 0) {
            this.setDescription(this.getDescription() + " ex\u00e9cut\u00e9 avec succ\u00e8s:\n Nombre de Ch\u00e8que= " + this.cheques35.length + " - Montant Total= " + Utility.formatNumber(this.vEnteteLots.get(0).getMontantTotal()));
            this.logEvent("INFO", "Nombre de Ch\u00e8que= " + this.cheques35.length + " - Montant Total= " + Utility.formatNumber("" + this.vEnteteLots.get(0).getMontantTotal()));
        }
        else {
            this.logEvent("WARNING", "Il n'y a aucun \u00e9l\u00e9ment disponible");
        }
        db.close();
    }
    
    private void feedOperationWithCheque(final int j, final Cheques cheque, final Operation[] operations) {
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
        if (cheque.getBanque().equalsIgnoreCase(Utility.getParam("CODE_BANQUE_SICA3"))) {
            operations[j].setCodeAval("C");
        }
        operations[j].setCodeCertif("0");
        operations[j].setBlancs(createBlancs(286, " "));
    }
    
    private void feedOperationWithCheque35(final int j, final Cheques cheque, final Operation[] operations) {
        operations[j].setTypeOperation(cheque.getType_Cheque());
        operations[j].setRefOperation("" + cheque.getIdcheque());
        final DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        }
        catch (Exception ex) {
            Logger.getLogger(LotDeltaBDKWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        final String l_sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheque.getRemise();
        final Remises[] remises = (Remises[])db.retrieveRowAsObject(l_sql, (Object)new Remises());
        db.close();
        if (remises != null && remises.length > 0) {
            operations[j].setIdAgeRem(remises[0].getAgenceDepot());
        }
        else {
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
        final String compteRemettant = Utility.bourrageGauche(cheque.getCompteremettant(), 12, "0");
        operations[j].setRefEmetteur(compteRemettant);
        operations[j].setCodeAval((cheque.getEscompte().intValue() == 0) ? "E" : "C");
        if (cheque.getBanque().equalsIgnoreCase(Utility.getParam("CODE_BANQUE_SICA3"))) {
            operations[j].setCodeAval("C");
        }
        operations[j].setCodeCertif("0");
        operations[j].setAdrSouscripteur(Utility.bourrageDroite(remises[0].getReference(), 50, " "));
        operations[j].setNomSouscripteur(Utility.bourrageDroite(cheque.getCodeutilisateur(), 4, " "));
        operations[j].setBlancs(createBlancs(286, " "));
    }
    
    private void prepareCheques(final Cheques[] cheques) throws Exception {
        if (cheques != null && cheques.length > 0) {
            final EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ELOT");
            enteteLot.setRefLot("" + ++this.refLot);
            enteteLot.setRefLot(Utility.computeCompteur("REFLOT", CMPUtility.getDate()));
            enteteLot.setRefBancaire(enteteLot.getRefLot());
            enteteLot.setTypeOperation(cheques[0].getType_Cheque());
            enteteLot.setIdBanRem(cheques[0].getBanqueremettant());
            enteteLot.setBlancs(createBlancs(26, " "));
            enteteLot.setNbOperations("" + cheques.length);
            final Operation[] operations = new Operation[cheques.length];
            long montantLot = 0L;
            for (int j = 0; j < cheques.length; ++j) {
                final Cheques cheque = cheques[j];
                operations[j] = new Operation();
                montantLot += Long.parseLong(cheque.getMontantcheque().trim());
                operations[j].setIdObjetOrigine(cheque.getIdcheque());
                switch (Integer.parseInt(cheque.getType_Cheque())) {
                    case 30: {
                        this.feedOperationWithCheque(j, cheque, operations);
                        break;
                    }
                    case 35: {
                        this.feedOperationWithCheque35(j, cheque, operations);
                        break;
                    }
                }
            }
            enteteLot.setMontantTotal("" + montantLot);
            enteteLot.operations = operations;
            this.vEnteteLots.add(enteteLot);
            this.printLotCheque(cheques, enteteLot);
        }
    }
    
    public void printLotCheque(final Cheques[] cheques, final EnteteLot enteteLot) throws Exception {
        final DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String l_sql = "";
        final String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + cheques[0].getBanqueremettant() + ".000." + enteteLot.getRefLot() + "." + cheques[0].getType_Cheque() + "." + CMPUtility.getDevise() + ".LOT";
        this.setOut(this.createFlatFile(fileName));
        String line = this.printEnteteLot(enteteLot);
        this.writeln(line);
        for (int j = 0; j < enteteLot.operations.length; ++j) {
            switch (Integer.parseInt(enteteLot.getTypeOperation())) {
                case 30:
                case 31: {
                    line = this.printCheques(enteteLot, j);
                    this.writeln(line);
                    break;
                }
                case 35: {
                    line = this.printCheques(enteteLot, j);
                    this.writeln(line);
                    break;
                }
            }
            l_sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALDELTA") + ", LOTSIB=1 WHERE  IDCHEQUE=" + cheques[j].getIdcheque() + " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ";
            db.executeUpdate(l_sql);
            l_sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALSURCAIENVSIB") + ", LOTSIB=1 WHERE  IDCHEQUE=" + cheques[j].getIdcheque() + " AND BANQUE IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ";
            db.executeUpdate(l_sql);
            //l_sql = "UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEVALDELTA") + " WHERE IDREMISE=" + cheques[j].getRemise(); CETAOPEALLICOM1                                               
            l_sql = "UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEALLICOM1") + " WHERE IDREMISE=" + cheques[j].getRemise();                                    

            db.executeUpdate(l_sql);
        }
        db.close();
        this.closeFile();
    }
    
    private String printCheques(final EnteteLot enteteLot, final int j) {
        final String line = new String(enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getIdAgeRem() + enteteLot.operations[j].getIPac() + enteteLot.operations[j].getFlagIBANCre() + enteteLot.operations[j].getPfxIBANCre() + enteteLot.operations[j].getNumCheque() + Utility.convertDateToString((Date)enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd") + enteteLot.operations[j].getIdBanDeb() + enteteLot.operations[j].getIdAgeDeb() + enteteLot.operations[j].getNumCptDeb() + enteteLot.operations[j].getCleRibDeb() + enteteLot.operations[j].getMontant() + enteteLot.operations[j].getNumCedant() + Utility.convertDateToString((Date)enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd") + enteteLot.operations[j].getRefEmetteur() + enteteLot.operations[j].getCodeAval() + enteteLot.operations[j].getIdAgeCre() + createBlancs(2, " ") + enteteLot.operations[j].getCodeCertif() + enteteLot.operations[j].getAdrSouscripteur() + createBlancs(50, " ") + enteteLot.operations[j].getNomSouscripteur() + enteteLot.operations[j].getBlancs());
        return line;
    }
    
    private String printEnteteLot(final EnteteLot enteteLot) throws Exception {
        final String line = new String(enteteLot.getIdEntete() + enteteLot.getIdBanRem() + "000" + enteteLot.getRefLot() + CMPUtility.getDevise() + enteteLot.getNbOperations() + enteteLot.getMontantTotal() + enteteLot.getBlancs());
        return line;
    }
}