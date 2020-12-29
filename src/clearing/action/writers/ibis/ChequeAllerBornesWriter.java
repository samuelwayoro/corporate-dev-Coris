/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.ibis;


import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Remises;
import java.io.File;
import java.math.BigDecimal;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeAllerBornesWriter extends FlatFileWriter {

    public ChequeAllerBornesWriter() {
        setDescription("Envoi des chèques vers le SIB");
    }



    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_FILE_ROOTNAME") + CMPUtility.getDateHeure() + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));

        String sql = "UPDATE CHEQUES SET ETAT ="+ Utility.getParam("CETAOPEERR")+" WHERE ETAT="+ Utility.getParam("CETAOPEVAL")  +" AND  (MONTANTCHEQUE IS NULL OR TRIM(MONTANTCHEQUE)='')";
        db.executeUpdate(sql);
        sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPEVAL")  + " ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        if (cheques != null && 0 < cheques.length) {

            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques validés

              

                sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[i].getRemise() ;
                Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());

                 sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT >=" + Utility.getParam("CETAOPEVAL") ;
                Cheques[] allChequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                j = allChequesVal.length;

                if((remises != null && 0 < remises.length )){
                    if (allChequesVal != null && 0 < allChequesVal.length) {
                    long sumRemise = 0;

                    //Creation Ligne remise a partir du premier chq
                    Cheques aCheque = allChequesVal[0];
                    for (int x = 0; x < allChequesVal.length; x++) {
                        sumRemise += Long.parseLong(allChequesVal[x].getMontantcheque());
                    }
                    montantTotal += sumRemise;
                    String line ="E"+ "|"+Utility.bourrageGauche(aCheque.getRemise().toPlainString(), 12, "0")
                                    + "|"+Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement().trim(),ResLoader.getMessages("patternDate")), "yyyy/MM/dd")
                                    + "|"+Utility.bourrageGauche(aCheque.getCompteremettant(), 12, "0")// 12 pour Ecobank, 11 pour Site Orion
                                    + "|"+Utility.bourrageGauche(String.valueOf(sumRemise), 13, "0");
                                                       
                                                       

                    writeln(line);

                    for (int x = 0; x < allChequesVal.length; x++) {
                        //Tous les chq de la remise
                        aCheque = allChequesVal[x];
                        line ="D"+"|" + Utility.bourrageGauche(aCheque.getNumerocheque(), 7, "0")
                                 +"|"+ aCheque.getBanque()
                                 +"|"+ aCheque.getAgence()
                                 +"|"+ Utility.bourrageGauche(aCheque.getNumerocompte(), 12, "0")
                                 +"|"+ aCheque.getRibcompte()
                                 +"|"+ createBlancs(35, "X")
                                 +"|"+ createBlancs(35, "X")
                                 +"|"+ Utility.bourrageGauche(aCheque.getMontantcheque(), 16, "0")
                                 +"|"+ Utility.bourrageDroite(aCheque.getPathimage().trim() + aCheque.getFichierimage().trim(), 256, " ");
                            
                        writeln(line);
                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
                        db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());

                    }
                    db.executeUpdate("UPDATE REMISES SET ETAT="+Utility.getParam("CETAREMENVSIB") +" WHERE IDREMISE="+aCheque.getRemise() );

                }

                }else{
                   
                    db.executeUpdate("UPDATE CHEQUES SET ETAT ="+ Utility.getParam("CETAOPEERR")+" WHERE REMISE="+ cheques[i].getRemise());
                    db.executeUpdate("UPDATE REMISES SET ETAT="+Utility.getParam("CETAOPEERR") +" WHERE IDREMISE="+ cheques[i].getRemise() );
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
