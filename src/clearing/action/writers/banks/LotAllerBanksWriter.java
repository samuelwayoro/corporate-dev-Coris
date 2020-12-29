/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.banks;

import clearing.model.CMPUtility;
import clearing.model.EnteteLot;
import clearing.model.Operation;
import clearing.table.Cheques;
import clearing.table.Lotcom;
import clearing.table.Remcom;
import clearing.utils.StaticValues;
import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class LotAllerBanksWriter extends FlatFileWriter {

    private String sql = "";
    private int refLot = 0;
    private Vector<EnteteLot> vEnteteLots = new Vector<EnteteLot>();
    
    
    Cheques cheques[] = null;
    Cheques cheques35[] = null;
   
    Remcom remcom = null;
    Lotcom lotcom = null;
    private boolean remiseHasCheques = false;
    

    public LotAllerBanksWriter() {
        setDescription("Envoi de Lot d'opération aller vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String whereClause = " ETAT=" + Utility.getParam("CETAOPEVAL") + " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ";

        
                //Recuperation des cheques 030
                sql = "SELECT * FROM CHEQUES WHERE  " +whereClause+ " AND TYPE_CHEQUE ='030'" ;
                cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                prepareCheques(cheques);
            
           
                //Recuperation des cheques 035 
                sql = "SELECT * FROM CHEQUES WHERE  " +whereClause+ "  AND TYPE_CHEQUE ='035'";
                cheques35 = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                prepareCheques(cheques35);
           
            

        if (refLot > 0) {
           
            updateEtatOperations(db);
        } else {
            logEvent("WARNING", "Il n'y a aucun élément disponible");
        }

        db.close();


    }

 

    private void feedOperationWithCheque(int j, Cheques cheque, Operation[] operations) {
       operations[j].setTypeOperation("030");
        operations[j].setRefOperation("" +cheque.getIdcheque());
        operations[j].setFlagIBANCre("1");
        operations[j].setIdBanDeb(cheque.getBanque());
        operations[j].setIdAgeDeb(cheque.getAgence());
        operations[j].setNumCptDeb(cheque.getNumerocompte());//cheque.getCompteremettant()
        operations[j].setCleRibDeb(Utility.computeCleRIB(cheque.getBanque(), cheque.getAgence(), cheque.getNumerocompte()));
        operations[j].setNomDebiteur(createBlancs(50, " "));
        operations[j].setAdrDebiteur(createBlancs(70, " "));
        operations[j].setNumCheque(cheque.getNumerocheque());
        operations[j].setIdBanCre(cheque.getBanqueremettant());
        operations[j].setIdAgeRem(cheque.getAgenceremettant());
        operations[j].setRefEmetteur(Utility.bourrageGauche(cheque.getCompteremettant(), 12,"0"));
        //operations[j].setCleRibDeb(Utility.computeCleRIB(CMPUtility.getCodeBanque(), cheque.getAgenceremettant(), Utility.bourrageGauche(cheque.getCompteremettant(), 12,"0")));
        operations[j].setNomCrediteur(cheque.getNombeneficiaire());
        operations[j].setAdrCrediteur(createBlancs(70, " "));
        operations[j].setMontant(Utility.bourrageGauche(cheque.getMontantcheque(), 13, "0"));
        operations[j].setDateOrdreClient(Utility.convertStringToDate(cheque.getDatesaisie(), ResLoader.getMessages("patternDate")));
        operations[j].setCodeCertif("0");
        operations[j].setNumCedant(""+cheque.getRemise());
        operations[j].setCodeAval((cheque.getEscompte().intValue()==0)?"E":"C");
        operations[j].setBlancs(createBlancs(100, " "));
        operations[j].setPfxIBANCre(Utility.bourrageDroite(cheque.getCodeutilisateur(), 10, " "));
        operations[j].setBlancs(createBlancs(185, " "));
    }

    private void feedOperationWithCheque35(int j, Cheques cheque, Operation[] operations) {
        operations[j].setTypeOperation("030");
        operations[j].setRefOperation("" +cheque.getIdcheque());
        operations[j].setFlagIBANCre("1");
        operations[j].setIdBanDeb(cheque.getBanque());
        operations[j].setIdAgeDeb(cheque.getAgence());
        operations[j].setNumCptDeb(cheque.getNumerocompte());//cheque.getCompteremettant()
        operations[j].setCleRibDeb(Utility.computeCleRIB(cheque.getBanque(), cheque.getAgence(), cheque.getNumerocompte()));
        operations[j].setNomDebiteur(createBlancs(50, " "));
        operations[j].setAdrDebiteur(createBlancs(70, " "));
        operations[j].setNumCheque(cheque.getNumerocheque());
        operations[j].setIdBanCre(cheque.getBanqueremettant());
        operations[j].setIdAgeRem(cheque.getAgenceremettant());
        operations[j].setRefEmetteur(Utility.bourrageGauche(cheque.getCompteremettant(), 12,"0"));
        //operations[j].setCleRibDeb(Utility.computeCleRIB(CMPUtility.getCodeBanque(), cheque.getAgenceremettant(), Utility.bourrageGauche(cheque.getCompteremettant(), 12,"0")));
        operations[j].setNomCrediteur(cheque.getNombeneficiaire());
        operations[j].setAdrCrediteur(createBlancs(70, " "));
        operations[j].setMontant(Utility.bourrageGauche(cheque.getMontantcheque(), 13, "0"));
        operations[j].setDateOrdreClient(Utility.convertStringToDate(cheque.getDatesaisie(), ResLoader.getMessages("patternDate")));
        operations[j].setCodeCertif("0");
        operations[j].setNumCedant(""+cheque.getRemise());
        operations[j].setCodeAval((cheque.getEscompte().intValue()==0)?"E":"C");
        operations[j].setBlancs(createBlancs(100, " "));
        operations[j].setPfxIBANCre(Utility.bourrageDroite(cheque.getCodeutilisateur(), 10, " "));
        operations[j].setBlancs(createBlancs(185, " "));
    }

  
    private void prepareCheques(Cheques[] cheques) throws  Exception {

        if (cheques != null && cheques.length > 0) {
            remiseHasCheques = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ELOT");
            enteteLot.setRefLot("" + ++refLot);
            enteteLot.setRefBancaire(enteteLot.getRefLot());
            enteteLot.setTypeOperation("030");
            enteteLot.setIdBanRem(cheques[0].getBanqueremettant());
            enteteLot.setBlancs(createBlancs(29, " "));
            enteteLot.setNbOperations("" + cheques.length);
            Operation[] operations = new Operation[cheques.length];
            long montantLot = 0;
            for (int j = 0; j < cheques.length; j++) {
                Cheques cheque = cheques[j];
                operations[j] = new Operation();
                montantLot = montantLot + Long.parseLong(cheque.getMontantcheque().trim());
                operations[j].setIdObjetOrigine(cheque.getIdcheque());
                switch (Integer.parseInt(cheque.getType_Cheque())) {

                    case StaticValues.CHQ_SCAN:
                         {
                            feedOperationWithCheque(j, cheque, operations);
                        }
                        break;
                    case StaticValues.CHQ_SCAN_SICA3:
                         {
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
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator +  Utility.getParam("CHQ_IN_FILE_ROOTNAME") + Utility.bourrageGZero(Utility.computeCompteur("LOTALL", "BANKS"), 3)+".Aller.030."+ Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy") + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));
        String line = printEnteteLot(enteteLot);
        writeln(line);
        for (int j = 0; j < enteteLot.operations.length; j++) {
            switch (Integer.parseInt(enteteLot.getTypeOperation())) {
                case StaticValues.CHQ_SCAN:
                    ;
                case StaticValues.CHQ_PAP:
                    {
                        line = printCheques(enteteLot, j);
                        writeln(line);
                    }
                    ;
                    break;
                case StaticValues.CHQ_SCAN_SICA3:
                    {
                        line = printCheques(enteteLot, j);
                        writeln(line);
                    }
                    ;
                    break;
            }
        }
        closeFile();
    }

   


    private String printCheques(EnteteLot enteteLot, int j) {
        String line;

        line = new String(//enteteLot.operations[j].getTypeOperation()
        enteteLot.operations[j].getTypeOperation()+
        Utility.bourrageGZero(enteteLot.operations[j].getRefOperation(),18)+
        enteteLot.operations[j].getFlagIBANCre()+
        enteteLot.operations[j].getIdBanCre()+
        enteteLot.operations[j].getIdAgeRem()+
        enteteLot.operations[j].getRefEmetteur()+
        Utility.computeCleRIB(enteteLot.operations[j].getIdBanCre(),enteteLot.operations[j].getIdAgeRem(), enteteLot.operations[j].getRefEmetteur())+//enteteLot.operations[j].getCleRibDeb()+
        Utility.bourrageDroite(enteteLot.operations[j].getNomCrediteur(),50," ")+
        Utility.bourrageDroite(enteteLot.operations[j].getAdrCrediteur(),70," ")+
        enteteLot.operations[j].getIdAgeRem()+
         "1"+
        enteteLot.operations[j].getNumCheque()+
        enteteLot.operations[j].getIdBanDeb()+
        enteteLot.operations[j].getIdAgeDeb()+
        enteteLot.operations[j].getNumCptDeb()+
        enteteLot.operations[j].getCleRibDeb()+
        createBlancs(50, " ")+
        createBlancs(70, " ")+
        Utility.bourrageGauche(enteteLot.operations[j].getMontant(),15,"0")+
        
        Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd") +
        enteteLot.operations[j].getCodeCertif()+
        enteteLot.operations[j].getNumCedant()+
        enteteLot.operations[j].getCodeAval()+
        createBlancs(100, " ")+
        enteteLot.operations[j].getPfxIBANCre()+
        createBlancs(185, " "));
        return line;
    }

   

    private String printEnteteLot(EnteteLot enteteLot) throws Exception {
        String line;
        //lotcom = CMPUtility.insertLotcom(enteteLot, remcom);
        line = new String(enteteLot.getIdEntete() +
                Utility.getParamOfType(CMPUtility.getCodeBanqueSica3(), "CODE_BANKS") +
                "000" +
                enteteLot.getRefLot() +
                CMPUtility.getDevise() +
                enteteLot.getNbOperations() +
                Utility.bourrageGauche(enteteLot.getMontantTotal(), 15,"0") +
                enteteLot.getBlancs());

        return line;
    }

    

    private void updateEtatOperations(DataBase db) throws SQLException {
        String l_sql = "";

        String whereClause = " ETAT=" + Utility.getParam("CETAOPEVAL") + " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ";

        if (remiseHasCheques) {
            l_sql = "UPDATE CHEQUES SET ETAT="+ Utility.getParam("CETAOPEALLICOM1") + ", LOTSIB=1 WHERE  " + whereClause ;
            db.executeUpdate(l_sql);
        }

     

    }
}
