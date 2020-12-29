/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.finacle;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import java.io.File;
import java.math.BigDecimal;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeAllerBFIFinWriter extends FlatFileWriter {

    public ChequeAllerBFIFinWriter() {
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
        sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
         long montantTotal = 0;
         Cheques aCheque = null;
         String line = null;
        if (cheques != null && 0 < cheques.length) {

            for (int i = 0; i < cheques.length; i ++) {
                //Tous les cheques validés

                //Tous les chq de la remise
                        aCheque = cheques[i];
                        line =  "6"+aCheque.getAgenceremettant()+Utility.bourrageGauche(aCheque.getCompteremettant(), 10, "0")+
                                createBlancs(2, " ")+
                                "GNF"+
                                Utility.bourrageGauche(aCheque.getMontantcheque(), 14, " ")+".00"+" "+
                                Utility.bourrageDroite(aCheque.getNombeneficiaire(),30, " ")+
                                Utility.bourrageGauche(aCheque.getNumerocheque(), 8, "0")+
                                Utility.bourrageGauche(aCheque.getMontantcheque(), 14, " ")+".00"+
                                "01 "+
                                createBlancs(9, " ")+
                                Utility.bourrageDroite(aCheque.getAgence(), 6, " ")+
                                Utility.bourrageDroite(aCheque.getBanque(), 6, " ")+"Y";


                        writeln(line);
                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACCENVSIB")));
                        db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                      montantTotal += Long.parseLong(cheques[i].getMontantcheque());
                     

                    //Creation ligne de chèque
                    
                   // db.executeUpdate("UPDATE REMISES SET ETAT="+Utility.getParam("CETAREMENVSIB") +" WHERE IDREMISE="+aCheque.getRemise() );
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
