/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action.writers.orion;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
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
public class ChequeRejeteSitWriter extends FlatFileWriter {

    public ChequeRejeteSitWriter() {
        setDescription("Envoi des rejets chèques vers le SIB");
    }

    @Override
       public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_REJ_FILE_ROOTNAME") + CMPUtility.getDateHeure() + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));

        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" +  Utility.getParam("CETAOPEREJRET")  + ") ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        if (cheques != null && 0 < cheques.length) {

            for (int i = 0; i < cheques.length; i ++) {
                Cheques cheque = cheques[i];
                //Tous les cheques retours rejetes
                String line = "<>0640025602";
                line += cheque.getAgence();
                line += createBlancs(2," ")+"2";
                line += cheque.getBanqueremettant();
                line += createBlancs(7," ")+"XOF2";
                line += Utility.bourrageGZero(cheque.getMontantcheque(), 16);
                line += Utility.convertDateToString(Utility.convertStringToDate(cheque.getDatesaisie(), ResLoader.getMessages("patternDate")),"yyMMdd");
                line += "0"+createBlancs(7," ")+"0"+createBlancs(2," ")+createBlancs(6,"0")+createBlancs(4, "b")+createBlancs(1," ");
                line += Utility.getParam(cheque.getMotifrejet()).trim();
                line += createBlancs(2," ");
                line += Utility.bourrageGZero(cheque.getIdcheque().toPlainString(), 7);
                line += createBlancs(29," ");
                line += Utility.convertDateToString(Utility.convertStringToDate(cheque.getDatecompensation(), ResLoader.getMessages("patternDate")),"yyyyMMdd");
                line += Utility.bourrageGZero(cheque.getNumerocheque(), 7);
                line += createBlancs(48," ");
                line += Utility.bourrageGauche(cheque.getNumerocompte(),11,"0");
                line += createBlancs(367," ");
                line += Utility.bourrageGZero(cheque.getCompteremettant(),12);
                
                writeln(line);
                if(cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEREJRET"))){
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEREJRETENVSIB")));
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
