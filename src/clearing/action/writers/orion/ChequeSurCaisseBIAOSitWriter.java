/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.orion;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Remises;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeSurCaisseBIAOSitWriter extends FlatFileWriter {

    public ChequeSurCaisseBIAOSitWriter() {
        setDescription("Envoi des chèques sur caisses vers le SIB");
    }



    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQCAISSE_IN_FILE_ROOTNAME") + CMPUtility.getDateHeure() + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));
        String sql = "UPDATE CHEQUES SET ETAT ="+ Utility.getParam("CETAOPEERR")+" WHERE ETAT="+ Utility.getParam("CETAOPEVAL")  +" AND  (MONTANTCHEQUE IS NULL OR TRIM(MONTANTCHEQUE)='')";
        db.executeUpdate(sql);
        sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND BANQUE  IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        if (cheques != null && 0 < cheques.length) {

            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques validés

                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT =" + Utility.getParam("CETAOPEVAL") + " AND BANQUE  IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "')";
                Cheques[] chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                 sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[i].getRemise() ;
                Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());

                 sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT >=" + Utility.getParam("CETAOPEVAL") ;
                Cheques[] allChequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                j = chequesVal.length;
                if(allChequesVal.length == remises[0].getNbOperation().intValue()){
                    if (chequesVal != null && 0 < chequesVal.length) {
                    long sumRemise = 0;

                    //Creation Ligne remise a partir du premier chq
                    Cheques aCheque = chequesVal[0];
                    for (int x = 0; x < chequesVal.length; x++) {
                        sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                    }
                    montantTotal += sumRemise;
                    String line = Utility.bourrageGauche(aCheque.getRemise().toPlainString(), 12, "0")
                                                       + createBlancs(9, "0")
                                                       + Utility.bourrageGauche(aCheque.getCompteremettant(), 11, "0")// 12 pour Ecobank, 11 pour Site Orion
                                                       + createBlancs(7, "0")
                                                       + aCheque.getBanqueremettant()
                                                       + aCheque.getAgenceremettant()
                                                       + Utility.computeCleRIB(aCheque.getBanqueremettant(), aCheque.getAgenceremettant(),Utility.bourrageGauche(aCheque.getCompteremettant(), 12, "0"))//createBlancs(2, "0")
                                                       + Utility.bourrageGauche(String.valueOf(sumRemise), 11, "0")
                                                       + "N"
                                                       +Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement().trim(),ResLoader.getMessages("patternDate")), "yyyyMMdd")
                                                       //+createBlancs(7, "0");
                                                       //+ Utility.bourrageDroite(aCheque.getRefremise(),7 , " ")
                                                       ;

                    writeln(line);

                    for (int x = 0; x < chequesVal.length; x++) {
                        //Tous les chq de la remise
                        aCheque = chequesVal[x];
                        line = Utility.bourrageGauche(aCheque.getRemise().toPlainString(), 12, "0")
                                                      + Utility.bourrageGauche(aCheque.getIdcheque().toPlainString(), 9, "0")
                                                      + Utility.bourrageGauche(aCheque.getNumerocompte(), 11, "0")
                                                      + Utility.bourrageGauche(aCheque.getNumerocheque(), 7, "0")
                                                      + aCheque.getBanque()
                                                      + aCheque.getAgence()
                                                      + aCheque.getRibcompte()
//                                                      + CMPUtility.getCodeBanqueDestinataire(aCheque.getBanque())
//                                                      + aCheque.getAgence()
//                                                      +  Utility.computeCleRIB(CMPUtility.getCodeBanqueDestinataire(aCheque.getBanque()), aCheque.getAgenceremettant(),Utility.bourrageGauche(aCheque.getCompteremettant(), 12, "0"))
                                                      + Utility.bourrageGauche(aCheque.getMontantcheque(), 11, "0")
                                                      + "N"
                                                      +Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement().trim(),ResLoader.getMessages("patternDate")), "yyyyMMdd")
                                                      + createBlancs(7, "0");
                        writeln(line);
                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALSURCAIENVSIB")));
                        aCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                        db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());

                    }
                    db.executeUpdate("UPDATE REMISES SET ETAT="+Utility.getParam("CETAREMENVSIB") +" WHERE IDREMISE="+aCheque.getRemise() );
                }

                }else{

                    db.executeUpdate("UPDATE CHEQUES SET ETAT ="+ Utility.getParam("CETAOPEERR")+" WHERE REMISE="+ remises[0].getIdremise());
                    db.executeUpdate("UPDATE REMISES SET ETAT="+Utility.getParam("CETAOPEERR") +" WHERE IDREMISE="+remises[0].getIdremise() );
                }
                

            }
       setDescription(getDescription()+" exécuté avec succès: Nombre de Chèque= "+cheques.length +" - Montant Total= "+montantTotal);
            logEvent("INFO", "Nombre de Chèque= "+cheques.length +" - Montant Total= "+montantTotal);
        }else {
            setDescription(getDescription()+": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }
        closeFile();
        db.close();
    }
}
