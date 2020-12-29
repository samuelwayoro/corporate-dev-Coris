/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.atlas;

import clearing.table.Cheques;
import clearing.table.Comptes;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ExportRetourAtlasWriter extends FlatFileWriter {

    public ExportRetourAtlasWriter() {
        setDescription("Envoi des cheques retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        List<Cheques> chequesDTO = new ArrayList<>();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        //MAJ DES CHEQUES DE BANQUES ENCORE EN ESP (=0)
        String sql = " UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPERETMAN") + " WHERE ETAT=" + Utility.getParam("CETAOPERET") + " AND BANQUEREMETTANT IN "
                + "  (SELECT CODEBANQUE FROM BANQUES B WHERE B.ALGORITHMEDECONTROLESPECIFIQUE =" + Utility.getParam("BANQUE_ESP") + " ) ";
        db.executeUpdate(sql);

        sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERETMAN") + "," + Utility.getParam("CETAOPERET") + ") ORDER BY BANQUEREMETTANT ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
       
        int j = 0;
        long montantTotal = 0;
        int numRemise = 0;

        if (cheques != null && 0 < cheques.length) {
            String refLot = Utility.computeCompteur("REFLOT", "LOTBNP");
            String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_OUT_FILE_ROOTNAME") + Utility.getParam("SIB_FILE_EXTENSION");
            setOut(createFlatFile(fileName));

            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques retour par banque

                sql = "SELECT * FROM CHEQUES WHERE BANQUEREMETTANT='" + cheques[i].getBanqueremettant() + "' "
                        + " AND ETAT IN (" + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERETMAN") + "," + Utility.getParam("CETAOPERET") + ")";
                Cheques[] chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                //Tous les cheques retour  
                Cheques cheque = cheques[i];

                j = chequesVal.length;
                numRemise++;
                long sumRemise = 0;
                String line;

                for (int x = 0; x < chequesVal.length; x++) {
                    sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                }
                montantTotal += sumRemise;

                String codeBanqueRemettant = Utility.getParamOfType(cheque.getBanqueremettant().substring(0, 2), "CODE_SICA3").trim() + cheque.getBanqueremettant().substring(2);
                String codeBanque = Utility.getParamOfType(cheque.getBanque().substring(0, 2), "CODE_SICA3").trim() + cheque.getBanque().substring(2);

                for (int x = 0; x < chequesVal.length; x++) {
                    
                    line = Utility.getParam("AGENCE_SIEGE") + ";"
                            + " " + Utility.bourrageGauche("" + refLot, 10, "0") + ";"
                            + Utility.convertDateToString(Utility.convertStringToDate(chequesVal[x].getDatecompensation(), "yyyy/MM/dd"), "dd/MM/yy") + ";"
                            + " " + Utility.bourrageGauche("" + numRemise, 10, "0") + ";"
                            + Utility.bourrageDroite(codeBanqueRemettant, 20, " ") + ";"
                            + Utility.bourrageGauche("" + sumRemise, 15, "0") + "+;"
                            + Utility.convertDateToString(Utility.convertStringToDate(chequesVal[x].getDatecompensation(), "yyyy/MM/dd"), "dd/MM/yy") + ";"
                            + " " + Utility.bourrageGauche("" + x, 10, "0") + ";"
                            + Utility.bourrageGauche(chequesVal[x].getNumerocheque(), 7, "") + ";"
                            + codeBanque + ";"
                            + chequesVal[x].getAgence() + ";"
                            + chequesVal[x].getNumerocompte() + ";"
                            + Utility.computeCleRIB(chequesVal[x].getBanque(), chequesVal[x].getAgence(), chequesVal[x].getNumerocompte()) + ";"
                            + Utility.bourrageGauche(chequesVal[x].getMontantcheque(), 15, "0") + "+;"
                            + Utility.convertDateToString(Utility.convertStringToDate(chequesVal[x].getDatecompensation(), "yyyy/MM/dd"), "dd/MM/yy") + ";"
                            + Utility.convertDateToString(Utility.convertStringToDate(chequesVal[x].getDatecompensation(), "yyyy/MM/dd"), "dd/MM/yy") + ";"
                            + "XX" + createBlancs(29, " ");

                    wwriteln(line);
                    Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("SELECT * from COMPTES WHERE NUMERO "
                            + " LIKE '%" + chequesVal[x].getNumerocompte().substring(1) + "%' AND AGENCE LIKE '" + chequesVal[x].getAgence() + "'", new Comptes());

                    if (comptes != null && comptes.length > 0 && comptes[0].getEtat().equals(new BigDecimal(Utility.getParam("CETAOPEANO")))) {
                        if (Utility.getParam("FLAG_INTERDIT") != null && Utility.getParam("FLAG_INTERDIT").equals("1")) {
                            chequesVal[x].setMotifrejet("103");
                            chequesVal[x].setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM2")));
                        } else {
                            if (chequesVal[x].getEtat().toPlainString().equals(Utility.getParam("CETAOPERETMAN"))) {
                                chequesVal[x].setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
//                            db.updateRowByObjectByQuery(chequesVal[x], "CHEQUES", "IDCHEQUE=" + chequesVal[x].getIdcheque());
                            } else if (chequesVal[x].getEtat().toPlainString().equals(Utility.getParam("CETAOPERETREC"))) {
                                chequesVal[x].setEtat(new BigDecimal(Utility.getParam("CETAOPERETRECENVSIB")));
//                            db.updateRowByObjectByQuery(chequesVal[x], "CHEQUES", "IDCHEQUE=" + chequesVal[x].getIdcheque());
                            }
                        }

//                        db.updateRowByObjectByQuery(chequesVal[x], "CHEQUES", "IDCHEQUE=" + chequesVal[x].getIdcheque());
                    } else {
                        if (chequesVal[x].getEtat().toPlainString().equals(Utility.getParam("CETAOPERETMAN"))) {
                            chequesVal[x].setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
//                            db.updateRowByObjectByQuery(chequesVal[x], "CHEQUES", "IDCHEQUE=" + chequesVal[x].getIdcheque());
                        } else if (chequesVal[x].getEtat().toPlainString().equals(Utility.getParam("CETAOPERETREC"))) {
                            chequesVal[x].setEtat(new BigDecimal(Utility.getParam("CETAOPERETRECENVSIB")));
//                            db.updateRowByObjectByQuery(chequesVal[x], "CHEQUES", "IDCHEQUE=" + chequesVal[x].getIdcheque());
                        }
                    }
                     
                    chequesDTO.add(chequesVal[x]);

//                    montantTotal += Long.parseLong(chequesVal[x].getMontantcheque());
                }
            }
            System.out.println("cheques  size ExportRetourWriter :" + chequesDTO.size());
            if (!chequesDTO.isEmpty()) {
                //a ameliorer risk de OutOfMemoryError 
                final int batchSize = 1000;
                int count = 0;
                for (Cheques chequeDTO : chequesDTO) {
                    db.updateRowByObjectByQuery(chequeDTO, "CHEQUES", "IDCHEQUE=" + chequeDTO.getIdcheque());
                }

            }
            setDescription(getDescription() + " execute avec succes: Nombre de Cheque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));
            logEvent("INFO", "Nombre de Cheque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));

            closeFile();

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        //MAJ DES CHEQUES SANS IMAGES AVEC MOTIF REJET 215
        sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM2") + " , MOTIFREJET='215' WHERE ETAT=" + Utility.getParam("CETAOPERET");
        db.executeUpdate(sql);
        db.close();
    }
}
