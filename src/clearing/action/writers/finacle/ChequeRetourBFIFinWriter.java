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
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeRetourBFIFinWriter extends FlatFileWriter{

    public ChequeRetourBFIFinWriter() {
        setDescription("Envoi des ch�ques retour vers le SIB");
    }

    @Override
       public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_OUT_FILE_ROOTNAME") + CMPUtility.getDateHeure() + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));

        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" +  Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERET") + ") ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        if (cheques != null && 0 < cheques.length) {

            for (int i = 0; i < cheques.length; i ++) {
                Cheques cheque = cheques[i];
                //Tous les cheques retours
                String line = "";
                //line += Utility.bourrageGZero(cheque.getAgenceremettant().substring(0, 2), 3)+ cheque.getBanqueremettant().substring(2) + cheque.getAgenceremettant().substring(2); //sort code remettant
                line += "001901300"; //sort code remettant
                line += "002002002";//Utility.getParam("SORTCODE");
                line += Utility.convertDateToString(Utility.convertStringToDate(cheque.getDatecompensation(), ResLoader.getMessages("patternDate")),"ddMMyyyy");
                line += Utility.bourrageGZero(cheque.getMontantcheque(), 11)+"00";
                line += Utility.bourrageGZero(cheque.getNumerocheque(), 8);//
                line += Utility.bourrageGZero(cheque.getIdcheque().toPlainString(), 10);
                line += "01";//createBlancs(2," ");
                line += "6"+ cheque.getAgence();
                line += cheque.getNumerocompte();
                // line += Utility.bourrageDroite(cheque.getNumerocompte(), 14, " ");
                line += createBlancs(1,"7");
                line += Utility.bourrageDroite(cheque.getIdcheque().toPlainString(), 20," ");
                line += Utility.convertDateToString(Utility.convertStringToDate(cheque.getDatecompensation(), ResLoader.getMessages("patternDate")),"ddMMyyyy");
                line += Utility.bourrageDroite(cheque.getBanqueremettant(), 8, " ");
                line += Utility.bourrageDroite(cheque.getNombeneficiaire(), 41," ");

                writeln(line);
                if(cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))){
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }else{
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPERETRECENVSIB")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }
                montantTotal += Long.parseLong(cheque.getMontantcheque());
            }
            
            setDescription(getDescription()+" ex�cut� avec succ�s: Nombre de Ch�que= "+cheques.length +" - Montant Total= "+montantTotal);
            logEvent("INFO", "Nombre de Ch�que= "+cheques.length +" - Montant Total= "+montantTotal);

        }else {
            setDescription(getDescription()+": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }
        closeFile();
        db.close();
    }

    public String getTypeCheque(Cheques cheque){

        if(cheque.getNumerocompte().contains(Utility.getParam("SOLID").trim()))
        return "DDS";
        else return "CHQ";
    }
}
