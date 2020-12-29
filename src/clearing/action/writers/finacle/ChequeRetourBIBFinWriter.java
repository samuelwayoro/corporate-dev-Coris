/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action.writers.finacle;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Comptes;
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
public class ChequeRetourBIBFinWriter extends FlatFileWriter{

    public ChequeRetourBIBFinWriter() {
        setDescription("Envoi des chèques retour vers le SIB");
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
                line += Utility.bourrageGZero(CMPUtility.getAgencePrincipale(cheque.getBanqueremettant()).substring(0, 2), 3)+ cheque.getBanqueremettant().substring(2) + CMPUtility.getAgencePrincipale(cheque.getBanqueremettant()).substring(2); //sort code remettant
                line += Utility.getParam("SORTCODE");
                line += Utility.convertDateToString(Utility.convertStringToDate(cheque.getDatecompensation(), ResLoader.getMessages("patternDate")),"ddMMyy");
                line += Utility.bourrageGZero(cheque.getMontantcheque(), 11)+"00";
                line += Utility.bourrageGZero(cheque.getNumerocheque(), 8);//
                line += Utility.bourrageGZero(cheque.getIdcheque().toPlainString(), 10);
                line += createBlancs(2," ");
                line += getCompteFinacle(cheque.getNumerocompte(),db);
                line += createBlancs(1," ")+ getTypeCheque(cheque);
                line += createBlancs(8," ");
                line += Utility.bourrageDroite(cheque.getAgenceremettant(), 6, " ");
                line += Utility.bourrageDroite(cheque.getBanqueremettant(), 6, " ");
                line += Utility.bourrageDroite(cheque.getAgence(), 6, " ");
                line += Utility.bourrageDroite(cheque.getBanque(), 6, " ");
                line += createBlancs(100," ");
                line += Utility.bourrageDroite(cheque.getIdcheque().toPlainString(), 50," ");
                
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
            
       setDescription(getDescription()+" exécuté avec succès: Nombre de Chèque= "+cheques.length +" - Montant Total= "+montantTotal);
            logEvent("INFO", "Nombre de Chèque= "+cheques.length +" - Montant Total= "+montantTotal);

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

     public String getCompteFinacle(String compte, DataBase db) throws Exception {

        
        String sql = "SELECT * FROM COMPTES WHERE NUMERO like '" + compte + "'";
        Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject(sql, new Comptes());
        if (comptes != null && comptes.length > 0) {
            return compte;
        }

        sql = "SELECT * FROM COMPTES WHERE VILLE like '" + compte + "'";
        comptes = (Comptes[]) db.retrieveRowAsObject(sql, new Comptes());
        if (comptes != null && comptes.length > 0) {
            return comptes[0].getNumero();
        }
        return compte;
    }
}
