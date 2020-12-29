/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.cie;

import clearing.table.Cheques;
import clearing.table.ibus.IBUS_CHQ_COMP;
import java.math.BigDecimal;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeAllerCIEWriter extends FlatFileWriter {

    public ChequeAllerCIEWriter() {
        setDescription("Envoi des chèques Aller vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());


        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + ") ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

        if (cheques != null && 0 < cheques.length) {


            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];
                //Tous les cheques Aller
                IBUS_CHQ_COMP fluchecom = new IBUS_CHQ_COMP(cheque.getIdcheque(), "C", Utility.convertStringToDate(cheque.getDatecompensation(), "yyyy/MM/dd"), "SICA", "CIE", "N");
                if (db.insertObjectAsRowByQuery(fluchecom, "IBUS_CHQ_COMP")) {

                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACCENVSIB")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                } else {
                    logEvent("WARNING", "IMPOSSIBLE D'INSERER LE CHEQUE " + cheque.getIdcheque() + " DANS IBUS_CHQ_COMP:" + db.getMessage());
                }

            }
            setDescription(getDescription() + " exécuté avec succès: Nombre de Chèque= " + cheques.length);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length);

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
