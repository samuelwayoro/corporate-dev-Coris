/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action.writers.orion;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
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
public class ChequeRejeteEISSignSitWriter extends FlatFileWriter {

    public ChequeRejeteEISSignSitWriter() {
        setDescription("Envoi des rejets chèques Signature vers le SIB");
    }

    @Override
       public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_REJ_SIGN_FILE_ROOTNAME") + CMPUtility.getDateHeure() + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));

        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" +  Utility.getParam("CETAOPEALLICOM2ACC")  + ") AND LOTSIB IN (4,3)  ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        if (cheques != null && 0 < cheques.length) {

            for (int i = 0; i < cheques.length; i ++) {
                Cheques cheque = cheques[i];
                //Tous les cheques retours rejetes
                String line =  Utility.convertDateToString(Utility.convertStringToDate(cheque.getDatesaisie(), ResLoader.getMessages("patternDate")),"yyyyMMdd");

                line += createBlancs(1," ");
                line += Utility.bourrageGZero(cheque.getIdcheque().toPlainString(), 11);
                line += Utility.getParam(cheque.getMotifrejet()).trim();
                line += cheque.getBanqueremettant();
                line += cheque.getAgenceremettant();
                line += Utility.bourrageGauche(cheque.getNumerocompte(),12,"0");
                line += createBlancs(2," ");
                line += Utility.bourrageGZero(cheque.getNumerocheque(), 7);
                line += Utility.bourrageGZero(cheque.getMontantcheque(), 16);
                        
                line += createBlancs(25," ");
                
                line += createBlancs(2," ");
                line += Utility.convertDateToString(new Date(),"ddMMyy");
                
              
                writeln(line);
                if(cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM2ACC"))){
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM2ACCENVSIB")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }
                 montantTotal += Long.parseLong(cheque.getMontantcheque());
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
