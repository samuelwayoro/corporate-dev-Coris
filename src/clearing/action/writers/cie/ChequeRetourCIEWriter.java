/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.cie;

import clearing.table.Cheques;
import clearing.table.ibus.IBUS_CHQ_COMP;
import clearing.table.ibus.IBUS_CHQ_RET;
import java.math.BigDecimal;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeRetourCIEWriter extends FlatFileWriter {

    public ChequeRetourCIEWriter() {
        setDescription("Envoi des chèques Retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());


        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERET") + ") ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

        if (cheques != null && 0 < cheques.length) {


            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];
                //Tous les cheques Retour
                IBUS_CHQ_RET flucheret = new IBUS_CHQ_RET(cheque.getNumerocheque(), cheque.getBanque(), cheque.getAgence(), Utility.convertStringToDate(cheque.getDateemission(), "yyyy/MM/dd"), Utility.convertStringToDate(cheque.getDatetraitement(), "yyyy/MM/dd"), new BigDecimal(cheque.getMontantcheque()), cheque.getNumerocompte(), cheque.getIdcheque(), "SICA", "CIE", "N");
                if (db.insertObjectAsRowByQuery(flucheret, "IBUS_CHQ_RET")) {

                    if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))) {
                        cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
                        db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                    } else {
                        cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPERETRECENVSIB")));
                        db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                    }
                } else {
                    logEvent("WARNING", "IMPOSSIBLE D'INSERER LE CHEQUE " + cheque.getNumerocheque() + " DANS IBUS_CHQ_RET:" + db.getMessage());
                }

            }

            setDescription(getDescription()+" exécuté avec succès: \n Nombre de Chèque Retour = "+cheques.length);
            logEvent("INFO", "Nombre de Chèque Retour = "+cheques.length);

        } else {
            setDescription(getDescription() + ": Il n'y a aucun chèque retour disponible");
            logEvent("WARNING", "Il n'y a aucun chèque retour disponible");
        }


        sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") ";
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

        if (cheques != null && 0 < cheques.length) {

            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];
                //Tous les cheques Aller
                IBUS_CHQ_COMP fluchecom = new IBUS_CHQ_COMP(cheque.getIdcheque(), "A", Utility.convertStringToDate(cheque.getDatecompensation(), "yyyy/MM/dd"), "SICA", "CIE", "N");
                if (db.insertObjectAsRowByQuery(fluchecom, "IBUS_CHQ_COMP")) {

                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACCENVSIBCON")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                } else {
                    logEvent("WARNING", "IMPOSSIBLE D'INSERER LE CHEQUE " + cheque.getNumerocheque() + " DANS IBUS_CHQ_COMP:" + db.getMessage());
                }
            }

            setDescription(getDescription()+"\n Nombre de Chèque Aller Accepté= "+cheques.length);
            logEvent("INFO", "Nombre de Chèque Aller Accepté= "+cheques.length);


        } else {
            setDescription(getDescription() + "\n Il n'y a aucun chèque Aller Accepté disponible");
            logEvent("WARNING", "Il n'y a aucun chèque Aller Accepté disponible");
        }

        sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEREJRET") + ") ";
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

        if (cheques != null && 0 < cheques.length) {

            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];
                //Tous les cheques Aller rejeté
                IBUS_CHQ_COMP fluchecom = new IBUS_CHQ_COMP(cheque.getIdcheque(), "R", Utility.convertStringToDate(cheque.getDatecompensation(), "yyyy/MM/dd"), "SICA", "CIE", "N");
                if (db.insertObjectAsRowByQuery(fluchecom, "IBUS_CHQ_COMP")) {

                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEREJRETENVSIB")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                } else {
                    logEvent("WARNING", "IMPOSSIBLE D'INSERER LE REJET " + cheque.getNumerocheque() + " DANS IBUS_CHQ_COMP:" + db.getMessage());
                }
            }

            setDescription(getDescription()+"\n Nombre de Chèque Aller Rejeté= "+cheques.length);
            logEvent("INFO", "Nombre de Chèque Aller Rejeté= "+cheques.length);
        } else {
            setDescription(getDescription() + "\n Il n'y a aucun rejet chèque Aller disponible");
            logEvent("WARNING", "Il n'y a aucun rejet chèque Aller disponible");
        }

        db.close();
    }
}
