/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.banks;

import clearing.model.CMPUtility;
import clearing.model.EnteteLot;
import clearing.model.Operation;
import clearing.model.RIO;
import clearing.table.Cheques;
import clearing.table.Effets;
import clearing.table.Lotcom;
import clearing.table.Remcom;
import clearing.table.Virements;
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
public class LotRetourBanksWriter extends FlatFileWriter {

    private String sql = "";
    private int refLot = 0;
    private Vector<EnteteLot> vEnteteLots = new Vector<EnteteLot>();
    
    
    Cheques cheques[] = null;
    Cheques cheques35[] = null;
    Virements virements10[] = null;
    Virements virements15[] = null;
    Virements virements17[] = null;
    Virements virements11[] = null;
    Virements virements12[] = null;
    Effets effets40[] = null;
    Effets effets41[] = null;
    Effets effets42[] = null;
    Effets effets43[] = null;
    Effets effets45[] = null;
    Effets effets46[] = null;
    Remcom remcom = null;
    Lotcom lotcom = null;
    private boolean remiseHasCheques = false;
    private boolean remiseHasRejetsCheques = false;
    private boolean remiseHasEffets = false;
    private boolean remiseHasVirements = false;


    public LotRetourBanksWriter() {
        setDescription("Envoi de Lot d'opération retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
         String whereClause = " ETAT IN (" +  Utility.getParam("CETAOPERETREC") + "," +   Utility.getParam("CETAOPERET") + ") ";

        
                //Recuperation des cheques 030
                sql = "SELECT * FROM CHEQUES WHERE  " +whereClause+ " AND TYPE_CHEQUE ='030'" ;
                cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                prepareCheques(cheques);
            
           
                //Recuperation des cheques 035 
                sql = "SELECT * FROM CHEQUES WHERE  " +whereClause+ "  AND TYPE_CHEQUE ='035'";
                cheques35 = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                prepareCheques(cheques35);
           
                whereClause = " ETAT IN (" +  Utility.getParam("CETAOPEREJRET") +  ") ";
                //Recuperation des rejets cheques 130
                sql = "SELECT * FROM CHEQUES WHERE "+ whereClause + " AND TYPE_CHEQUE ='030'" ;
                cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                prepareRejetCheques(cheques);


                //Recuperation des cheques 135
                sql = "SELECT * FROM CHEQUES WHERE "+ whereClause + " AND TYPE_CHEQUE ='035'";
                cheques35 = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                prepareRejetCheques(cheques35);
                sql = "SELECT * FROM EFFETS WHERE ETAT IN (" +  Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERET") + ")  AND TYPE_EFFET IN ('045','042')";

                //Effets
                Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                printEffet045(effets);
                sql = "SELECT * FROM EFFETS WHERE ETAT IN (" +  Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERET") + ")  AND TYPE_EFFET IN ('046','043')";
                effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                printEffet046(effets);

                sql = "SELECT * FROM EFFETS WHERE ETAT IN (" +  Utility.getParam("CETAOPEREJRET") + ")  AND TYPE_EFFET IN ('046','043')";
                effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                printRejetEffet046(effets);

                sql = "SELECT * FROM EFFETS WHERE ETAT IN (" +  Utility.getParam("CETAOPEREJRET") + ")  AND TYPE_EFFET IN ('045','042')";
                effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                printRejetEffet045(effets);

                //Virements
                sql = "SELECT * FROM VIREMENTS WHERE ETAT IN (" + Utility.getParam("CETAOPERET") + ")  AND TYPE_VIREMENT IN ('015','010') ";
                Virements[] Virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
                printVirement015(Virements);
                sql = "SELECT * FROM VIREMENTS WHERE ETAT IN ("  + Utility.getParam("CETAOPERET") + ")  AND TYPE_VIREMENT='011'";
                Virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
                printVirement011(Virements);

                sql = "SELECT * FROM VIREMENTS WHERE ETAT IN (" +  Utility.getParam("CETAOPERET") + ")  AND TYPE_VIREMENT IN ('012','017') ";
                Virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
                printVirement011(Virements);



        if (refLot > 0) {
           
            updateEtatOperations(db);
        } else {
            logEvent("WARNING", "Il n'y a aucun élément disponible");
        }

        db.close();


    }




    private void printVirement015(Virements[] Virements) throws Exception {
         if (Virements != null && 0 < Virements.length) {
             remiseHasVirements = true;refLot++;
             String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("VIR_OUT_FILE_ROOTNAME") + Utility.bourrageGZero(Utility.computeCompteur("VIRRET", "BANKS"), 3)+".Retour.010."+ Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy") + Utility.getParam("SIB_FILE_EXTENSION");
             setOut(createFlatFile(fileName));

            for (int i = 0; i < Virements.length; i ++) {
                Virements virement = Virements[i];
                if(virement.getType_Virement().equalsIgnoreCase("015")){
                String line = "";
                line += Utility.getParamOfType(virement.getBanqueremettant(),"CODE_BANKS")+"XOF";
                line += Utility.bourrageGZero(virement.getIdvirement().toPlainString(), 6);
                line += Utility.convertDateToString(Utility.convertStringToDate(virement.getDatecompensation(), ResLoader.getMessages("patternDate")),"yyyyMMdd");
                line += Utility.bourrageGZero(virement.getReference_Emetteur(), 18);
                line += "1";
                line += Utility.bourrageGZero(virement.getIdvirement().toPlainString(), 18);

                line += "1";
                line += virement.getBanqueremettant();
                line += "01001";
                //line += virement.getAgenceremettant();
                line += virement.getNumerocompte_Tire();
                line += Utility.computeCleRIB(virement.getBanqueremettant(), virement.getAgenceremettant(),virement.getNumerocompte_Tire() );
                line += Utility.bourrageDroite(virement.getNom_Tire(),50," ");
                line += Utility.bourrageDroite(virement.getAdresse_Tire(),70," ");
                line += virement.getAgenceremettant();
                line += Utility.convertDateToString(Utility.convertStringToDate(virement.getDateordre(), ResLoader.getMessages("patternDate")),"yyyyMMdd");
                line += Utility.bourrageGauche(virement.getNumerovirement(), 10,"0");
                line += "1";
                line += virement.getBanque();
                line += virement.getAgence();
                line += virement.getNumerocompte_Beneficiaire();
                line += Utility.computeCleRIB(virement.getBanque(), virement.getAgence(),virement.getNumerocompte_Beneficiaire() );

                line += Utility.bourrageDroite(virement.getNom_Beneficiaire(),50," ");
                line += Utility.bourrageDroite(virement.getAdresse_Beneficiaire(),70," ");

                line += Utility.bourrageGZero(virement.getMontantvirement(), 13);
                line +="00";
                line +=Utility.bourrageDroite(virement.getLibelle(), 70, " ");

                line += createBlancs(230," ");

                writeln(line);


                }




                //Tous les Virements retours LCR

            }
             closeFile();
        }
    }

  

     private void printVirement011(Virements[] Virements) throws Exception {
          if (Virements != null && 0 < Virements.length) {
              remiseHasVirements = true;refLot++;
             String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("VIR_OUT_FILE_ROOTNAME")+ Utility.bourrageGZero(Utility.computeCompteur("VIRRET", "BANKS"), 3)+".Retour.011."+ Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy") + Utility.getParam("SIB_FILE_EXTENSION");
             setOut(createFlatFile(fileName));

            for (int i = 0; i < Virements.length; i ++) {
                Virements virement = Virements[i];
                if(virement.getType_Virement().equalsIgnoreCase("011")){
                String line = "";
                line += Utility.getParamOfType(virement.getBanqueremettant(),"CODE_BANKS")+"XOF";
                line += Utility.bourrageGZero(virement.getIdvirement().toPlainString(), 6);
                line += Utility.convertDateToString(Utility.convertStringToDate(virement.getDatecompensation(), ResLoader.getMessages("patternDate")),"yyyyMMdd");
                line += Utility.bourrageGZero(virement.getReference_Emetteur(), 18);
                line += "1";
                line += Utility.bourrageGZero(virement.getIdvirement().toPlainString(), 18);

                line += Utility.getParamOfType(virement.getBanqueremettant(),"CODE_BANKS");
                line += virement.getAgenceremettant();
                line += Utility.convertDateToString(Utility.convertStringToDate(virement.getDateordre(), ResLoader.getMessages("patternDate")),"yyyyMMdd");
                line += Utility.bourrageGauche(virement.getNumerovirement(), 10,"0");
                line += Utility.getParamOfType(virement.getBanque(),"CODE_BANKS");
                line += virement.getAgence();
                line += Utility.bourrageGZero(virement.getMontantvirement(), 13);
                line +="00";
                line +=Utility.bourrageDroite(virement.getLibelle(), 70, " ");
                line += createBlancs(509," ");

                writeln(line);


                }




                //Tous les Virements retours LCR

            }
             closeFile();
        }
    }

   private void printEffet046(Effets[] effets) throws Exception {
          if (effets != null && 0 < effets.length) {
              remiseHasEffets = true;refLot++;
              String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("EFF_OUT_FILE_ROOTNAME")+ Utility.bourrageGZero(Utility.computeCompteur("EFFRET", "BANKS"), 3)+".Retour.060."+ Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy") + Utility.getParam("SIB_FILE_EXTENSION");
             setOut(createFlatFile(fileName));

            for (int i = 0; i < effets.length; i ++) {
                
                Effets effet = effets[i];
                if(effet.getType_Effet().equalsIgnoreCase("046")|| effet.getType_Effet().equalsIgnoreCase("043")){
                String line = "";
                line += Utility.getParamOfType(effet.getBanqueremettant(),"CODE_BANKS")+"XOF";
                line += createBlancs(6,"0");
                line += Utility.convertDateToString(Utility.convertStringToDate(effet.getDatecompensation(), ResLoader.getMessages("patternDate")),"yyyyMMdd");
                line += Utility.bourrageGZero(effet.getIdeffet().toPlainString(), 18);
                line += Utility.bourrageGZero(effet.getIdeffet().toPlainString(), 18);

                line += "1";
                line += effet.getBanque();
                line += effet.getAgence();
                line += effet.getNumerocompte_Tire();
                line += Utility.computeCleRIB(effet.getBanque(), effet.getAgence(), effet.getNumerocompte_Tire());
                line += Utility.bourrageDroite(effet.getNom_Tire(),50," ");
                line += Utility.bourrageDroite(effet.getAdresse_Tire(),70," ");
                line += "1";
                line += effet.getBanqueremettant();
                line += effet.getAgenceremettant();
                line += effet.getNumerocompte_Beneficiaire();
                line += Utility.computeCleRIB(effet.getBanqueremettant(), effet.getAgenceremettant(), effet.getNumerocompte_Beneficiaire());
                line += Utility.bourrageDroite(effet.getNom_Beneficiaire(),50," ");
                line += Utility.bourrageDroite(effet.getAdresse_Beneficiaire(),70," ");
                line += createBlancs(5,"0")+"01"+createBlancs(144," ");

                line += Utility.bourrageGZero(effet.getMontant_Effet(), 15);
                line += Utility.bourrageGZero(effet.getMontant_Effet(), 15);
                line += Utility.bourrageGZero(effet.getMontant_Frais(), 15);
                line += Utility.bourrageGauche(effet.getNumeroeffet(), 7,"0");
                line += Utility.convertDateToString(Utility.convertStringToDate(effet.getDate_Echeance(), ResLoader.getMessages("patternDate")),"yyyyMMdd");
                line += Utility.convertDateToString(Utility.convertStringToDate(effet.getDate_Creation(), ResLoader.getMessages("patternDate")),"yyyyMMdd");
                line +="1";
                line +=Utility.bourrageDroite(effet.getIdentification_Tire(), 70, " ");

                line += createBlancs(49," ");

                writeln(line);


                }




                //Tous les effets retours LCR

            }
             closeFile();
        }
    }

    private void printEffet045(Effets[] effets) throws Exception {
         if (effets != null && 0 < effets.length) {
             remiseHasEffets = true; refLot++;
              String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("EFF_OUT_FILE_ROOTNAME")+ Utility.bourrageGZero(Utility.computeCompteur("EFFRET", "BANKS"), 3)+".Retour.061."+ Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy") + Utility.getParam("SIB_FILE_EXTENSION");
             setOut(createFlatFile(fileName));

            for (int i = 0; i < effets.length; i ++) {
                
                Effets effet = effets[i];
                if(effet.getType_Effet().equalsIgnoreCase("045")||effet.getType_Effet().equalsIgnoreCase("042")){
                String line = "";
                line += Utility.getParamOfType(effet.getBanqueremettant(),"CODE_BANKS")+"XOF";
                line += createBlancs(6,"0");
                line += Utility.convertDateToString(Utility.convertStringToDate(effet.getDatecompensation(), ResLoader.getMessages("patternDate")),"yyyyMMdd");
                line += Utility.bourrageGZero(effet.getIdeffet().toPlainString(), 18);
                line += Utility.bourrageGZero(effet.getIdeffet().toPlainString(), 18);

                line += "1";
                line += effet.getBanque();
                line += effet.getAgence();
                line += effet.getNumerocompte_Tire();
                line += Utility.computeCleRIB(effet.getBanque(), effet.getAgence(), effet.getNumerocompte_Tire());
                line += Utility.bourrageDroite(effet.getNom_Tire(),50," ");
                line += Utility.bourrageDroite(effet.getAdresse_Tire(),70," ");

                line += "0000001";
                line += effet.getBanqueremettant();
                line += effet.getAgenceremettant();
                line += effet.getNumerocompte_Beneficiaire();
                line += Utility.computeCleRIB(effet.getBanqueremettant(), effet.getAgenceremettant(), effet.getNumerocompte_Beneficiaire());
                line += Utility.bourrageDroite(effet.getNom_Beneficiaire(),50," ");
                line += Utility.bourrageDroite(effet.getAdresse_Beneficiaire(),70," ");
                line += Utility.bourrageGZero(effet.getMontant_Effet(), 15);
                line += Utility.bourrageGauche(effet.getNumeroeffet(), 7,"0");
                line += Utility.convertDateToString(Utility.convertStringToDate(effet.getDate_Echeance(), ResLoader.getMessages("patternDate")),"yyyyMMdd");
                line += Utility.convertDateToString(Utility.convertStringToDate(effet.getDate_Creation(), ResLoader.getMessages("patternDate")),"yyyyMMdd");
                line +=Utility.bourrageDroite(effet.getIdentification_Tire(), 70, " ");
                line += createBlancs(225," ");



                writeln(line);


                }




                //Tous les effets retours LCR

            }
             closeFile();
        }
    }

    private void printRejetEffet046(Effets[] effets) throws Exception {
        if (effets != null && 0 < effets.length) {
             remiseHasEffets = true; refLot++;
              String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("EFF_OUT_FILE_ROOTNAME")+ Utility.bourrageGZero(Utility.computeCompteur("EFFREJ", "BANKS"), 3)+".RejetAller.160."+ Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy") + Utility.getParam("SIB_FILE_EXTENSION");
             setOut(createFlatFile(fileName));

            for (int i = 0; i < effets.length; i ++) {
                
                Effets effet = effets[i];
                if(effet.getType_Effet().equalsIgnoreCase("046")){
                String line = "160";
                line += effet.getMotifrejet();
                line += Utility.bourrageGZero(effet.getIdeffet().toPlainString(), 18);

                writeln(line);


                }



            }
             closeFile();
        }

    }

        private void printRejetEffet045(Effets[] effets) throws Exception {

        if (effets != null && 0 < effets.length) {
            remiseHasEffets = false; refLot++;
              String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("EFF_OUT_FILE_ROOTNAME")+ Utility.bourrageGZero(Utility.computeCompteur("EFFREJ", "BANKS"), 3)+".RejetAller.161."+ Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy") + Utility.getParam("SIB_FILE_EXTENSION");
             setOut(createFlatFile(fileName));

            for (int i = 0; i < effets.length; i ++) {
                Effets effet = effets[i];
                if(effet.getType_Effet().equalsIgnoreCase("046")){
                String line = "161";
                line += effet.getMotifrejet();
                line += Utility.bourrageGZero(effet.getIdeffet().toPlainString(), 18);

                writeln(line);


                }



            }
             closeFile();
        }

    }

    private void feedOperationWithCheque(int j, Cheques cheque, Operation[] operations) {
        operations[j].setTypeOperation("030");
        operations[j].setRefOperation(Utility.bourrageGZero("" +cheque.getIdcheque(), 18));
        operations[j].setFlagIBANCre("1");
        operations[j].setIdBanCre(CMPUtility.getCodeBanque());
        operations[j].setIdAgeRem(cheque.getAgenceremettant());
        operations[j].setRefEmetteur(Utility.bourrageGauche(cheque.getCompteremettant(), 12,"0"));
        operations[j].setCleRibDeb(Utility.computeCleRIB(CMPUtility.getCodeBanque(), cheque.getAgenceremettant(), Utility.bourrageGauche(cheque.getCompteremettant(), 12,"0")));
        operations[j].setNomCrediteur(cheque.getNombeneficiaire());
        operations[j].setAdrCrediteur(createBlancs(70, " "));
        operations[j].setNumCheque(cheque.getNumerocheque());
        operations[j].setIdBanDeb(cheque.getBanque());
        operations[j].setIdAgeDeb(cheque.getAgence());
        operations[j].setNumCptDeb(cheque.getNumerocompte());
        operations[j].setCleRibDeb(Utility.computeCleRIB(cheque.getBanque(), cheque.getAgence(), cheque.getNumerocompte()));
        operations[j].setNomDebiteur(createBlancs(50, " "));
        operations[j].setAdrDebiteur(createBlancs(70, " "));
        operations[j].setMontant(Utility.bourrageGauche(cheque.getMontantcheque(), 13, "0"));
        operations[j].setDateOrdreClient(Utility.convertStringToDate(cheque.getDatesaisie(), ResLoader.getMessages("patternDate")));
        operations[j].setCodeCertif("0");
        operations[j].setNumCedant(""+cheque.getLotcom());
        operations[j].setCodeAval("E");
        operations[j].setBlancs(createBlancs(100, " "));
        operations[j].setPfxIBANCre(Utility.bourrageDroite("wdb", 10, " "));
        operations[j].setBlancs(createBlancs(185, " "));
    }

    private void feedOperationWithCheque35(int j, Cheques cheque, Operation[] operations) {
        operations[j].setTypeOperation("030");
        operations[j].setRefOperation("" +cheque.getIdcheque());
        operations[j].setFlagIBANCre("1");
        operations[j].setIdBanDeb(cheque.getBanque());
        operations[j].setIdAgeDeb(cheque.getAgence());
        operations[j].setNumCptDeb(cheque.getNumerocompte());
        operations[j].setCleRibDeb(Utility.computeCleRIB(cheque.getBanque(), cheque.getAgence(), cheque.getNumerocompte()));
        operations[j].setNomDebiteur(createBlancs(50, " "));
        operations[j].setAdrDebiteur(createBlancs(70, " "));
        operations[j].setNumCheque(cheque.getNumerocheque());
        operations[j].setIdBanCre(cheque.getBanqueremettant());
        operations[j].setIdAgeRem(cheque.getAgenceremettant());
        operations[j].setRefEmetteur(Utility.bourrageGauche(cheque.getCompteremettant(), 12,"0"));
        operations[j].setCleRibCre(Utility.computeCleRIB(cheque.getBanqueremettant(), cheque.getAgenceremettant(), Utility.bourrageGauche(cheque.getCompteremettant(), 12,"0")));
        operations[j].setNomCrediteur(createBlancs(50, " "));
        operations[j].setAdrCrediteur(createBlancs(70, " "));
        operations[j].setMontant(Utility.bourrageGauche(cheque.getMontantcheque(), 13, "0"));
        operations[j].setDateOrdreClient(Utility.convertStringToDate(cheque.getDatesaisie(), ResLoader.getMessages("patternDate")));
        operations[j].setCodeCertif("1");
        operations[j].setNumCedant(""+cheque.getLotcom());
        operations[j].setCodeAval("E");
        operations[j].setBlancs(createBlancs(100, " "));
        operations[j].setPfxIBANCre(Utility.bourrageDroite("wdb", 10, " "));
        operations[j].setBlancs(createBlancs(185, " "));
    }

   
     private String printLotRejets(EnteteLot enteteLot) throws Exception {
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + "Cheque" + Utility.bourrageGZero(Utility.computeCompteur("CHQREJALL", "BANKS"), 3)+ ".RejetAller.130."+Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy") + ".LOT";
        setOut(createFlatFile(fileName));
     //   String line="";
       String line = printEnteteLot(enteteLot);
//        writeln(line);
        for (int j = 0; j < enteteLot.operations.length; j++) {

        line = new String(
                enteteLot.operations[j].getTypeOperation() +
                enteteLot.operations[j].getMotifRejet() +
                Utility.bourrageGZero(enteteLot.operations[j].getRefOperation(), 18) +
                Utility.getParamOfType(enteteLot.operations[j].getIdBanDeb(), "CODE_BANKS")+
                enteteLot.operations[j].getIdAgeRem() +
                createBlancs(168, " "));
        writeln(line);
        }
        closeFile();



        return line;
    }

    private void prepareRejetCheques(Cheques[] cheques) throws  Exception {

        if (cheques != null && cheques.length > 0) {
            remiseHasRejetsCheques = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ECRO");
            enteteLot.setRefLot("" + ++refLot);
            enteteLot.setRefBancaire(enteteLot.getRefLot());
            enteteLot.setTypeOperation("130");
            enteteLot.setIdBanRem(cheques[0].getBanqueremettant());
            enteteLot.setBlancs(createBlancs(24, " "));
            enteteLot.setNbOperations("" + cheques.length);
            Operation[] operations = new Operation[cheques.length];

            for (int j = 0; j < cheques.length; j++) {
                Cheques cheque = cheques[j];
                operations[j] = new Operation();

                operations[j].setRio(new RIO(cheque.getRio_Rejet()));
                operations[j].setTypeOperation("130");
                operations[j].setMotifRejet(cheque.getMotifrejet());
                operations[j].setRefOperation(cheque.getIdcheque().toString());
                operations[j].setIdAgeRem(cheque.getAgence());
                operations[j].setRioOperInitial(new RIO(cheque.getRio()));
                
                operations[j].setBlancs(createBlancs(10, " "));
                operations[j].setIdObjetOrigine(cheque.getIdcheque());
                operations[j].setIdBanDeb(cheque.getBanque());
            }

            enteteLot.setMontantTotal(createBlancs(16, " "));
            enteteLot.operations = operations;
            vEnteteLots.add(enteteLot);
            printLotRejets( enteteLot);
        }
    }
    private void prepareCheques(Cheques[] cheques) throws  Exception {

        if (cheques != null && cheques.length > 0) {
            remiseHasCheques = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ECRO");
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
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator +  Utility.getParam("CHQ_OUT_FILE_ROOTNAME") + Utility.bourrageGZero(Utility.computeCompteur("CHQRET", "BANKS"), 3) + ".Retour.030."+Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy") + ".LOT";
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
        enteteLot.operations[j].getCleRibCre()+
        Utility.bourrageDroite(enteteLot.operations[j].getNomCrediteur(),50," ")+
        Utility.bourrageDroite(enteteLot.operations[j].getAdrCrediteur(),70," ")+
        createBlancs(5, "0")+
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

   if (remiseHasCheques) {
            l_sql = "UPDATE CHEQUES SET ETAT="+Utility.getParam("CETAOPERETENVSIB") +",LOTSIB=" + "1" + " WHERE ETAT = " + Utility.getParam("CETAOPERET") ;
            db.executeUpdate(l_sql);
              l_sql = "UPDATE CHEQUES SET ETAT="+Utility.getParam("CETAOPERETRECENVSIB") +",LOTSIB=" + "1" + " WHERE ETAT = " + Utility.getParam("CETAOPERETREC") ;
            db.executeUpdate(l_sql);

        }
  if (remiseHasRejetsCheques) {
            l_sql = "UPDATE CHEQUES SET ETAT="+Utility.getParam("CETAOPEREJRETENVSIB") +",LOTSIB=" + "1" + " WHERE ETAT = " + Utility.getParam("CETAOPEREJRET") ;
            db.executeUpdate(l_sql);

        }
 if (remiseHasEffets) {
            l_sql = "UPDATE EFFETS SET ETAT="+Utility.getParam("CETAOPERETENVSIB") +",LOTSIB=" + "1" + " WHERE ETAT = " + Utility.getParam("CETAOPERET") ;
            db.executeUpdate(l_sql);
            l_sql = "UPDATE EFFETS SET ETAT="+Utility.getParam("CETAOPERETRECENVSIB") +",LOTSIB=" + "1" + " WHERE ETAT = " + Utility.getParam("CETAOPERETREC") ;
            db.executeUpdate(l_sql);
            l_sql = "UPDATE EFFETS SET ETAT="+Utility.getParam("CETAOPEREJRETENVSIB") +",LOTSIB=" + "1" + " WHERE ETAT = " + Utility.getParam("CETAOPEREJRET") ;
            db.executeUpdate(l_sql);

        }

   if (remiseHasVirements) {
            l_sql = "UPDATE VIREMENTS SET ETAT="+Utility.getParam("CETAOPERETENVSIB") +",LOTSIB=" + "1" + " WHERE ETAT = " + Utility.getParam("CETAOPERET") ;
            db.executeUpdate(l_sql);
           

        }


    }
}
