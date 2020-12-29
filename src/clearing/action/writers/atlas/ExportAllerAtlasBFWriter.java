/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.atlas;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Comptes;
import clearing.table.Remises;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ExportAllerAtlasBFWriter extends FlatFileWriter {

    public ExportAllerAtlasBFWriter() {
        setDescription("Envoi des cheques Aller vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String sql = "UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPEERR") + " WHERE ETAT=" + Utility.getParam("CETAOPESUPVAL") + " AND  (MONTANTCHEQUE IS NULL OR TRIM(MONTANTCHEQUE)='')";
        db.executeUpdate(sql);
        sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPESUPVAL") + " ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        List<Cheques> chequesDTO = new ArrayList<>();
        int j = 0;
        long montantTotal = 0;
        int numRemise = 0;

        if (cheques != null && 0 < cheques.length) {
            String refLot = Utility.computeCompteur("REFLOT", "LOTBNP");

            String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_FILE_ROOTNAME") + Utility.getParam("SIB_FILE_EXTENSION");
            setOut(createFlatFile(fileName));

            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques valides

                //Tous les cheques  valides d'une remise
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT =" + Utility.getParam("CETAOPESUPVAL");
                Cheques[] chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                //La remise en question
                sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[i].getRemise();
                Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());

                j = chequesVal.length;
                numRemise++;

                if ((remises != null && 0 < remises.length)
                        && (chequesVal.length == remises[0].getNbOperation().intValue())) {
                    if (0 < chequesVal.length) {
                        long sumRemise = 0;
                        String line;

                        Cheques aCheque = chequesVal[0];
                        for (int x = 0; x < chequesVal.length; x++) {
                            sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                        }
                        montantTotal += sumRemise;

                        String numCptEx = "";
                        Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numero  ='" + remises[0].getCompteRemettant() + "'"
                                + " and agence ='" + remises[0].getAgenceRemettant() + "'", new Comptes());
                        if (comptes != null && comptes.length > 0) {
                            numCptEx = comptes[0].getNumcptex().trim();
                        }

                        //Creation ligne de cheque
                        for (int x = 0; x < chequesVal.length; x++) {
                            //Tous les chq de la remise
                            aCheque = chequesVal[x];
                            String codeBanque = Utility.getParamOfType(aCheque.getBanque().substring(0, 2), "CODE_SICA3").trim() + aCheque.getBanque().substring(2);
                            String typeCheque = "";
                            if (CMPUtility.isBanqueNationale(aCheque.getBanque())) {
                                if (aCheque.getBanque().equalsIgnoreCase(Utility.getParam("CODE_BANQUE_SICA3"))) {

                                    typeCheque = "01";
                                    if (comptes != null && comptes.length > 0 && comptes[0].getEtat().equals(new BigDecimal(10))) {
                                        typeCheque = "21";
                                    }
                                    if (aCheque.getEscompte() != null && aCheque.getEscompte().equals(new BigDecimal(2))) {
                                        typeCheque = "11";
                                    }
                                } else {
                                    typeCheque = "05";
                                    if (comptes != null && comptes.length > 0 && comptes[0].getEtat().equals(new BigDecimal(10))) {
                                        typeCheque = "25";
                                    }
                                    if (aCheque.getEscompte() != null && aCheque.getEscompte().equals(new BigDecimal(2))) {
                                        typeCheque = "12";
                                    }
                                }

                            } else {
                                typeCheque = "08"; //==Harmonisation a refaire
                                if (comptes != null && comptes.length > 0 && comptes[0].getEtat().equals(new BigDecimal(10))) {
                                    typeCheque = "30";
                                }
                            }
                            //mise a jour
                            String agenceDepot = remises[0].getAgenceDepot().trim();
                            if (Utility.getParam("CODE_BANQUE_SICA3").equals("SN010")) {
                                agenceDepot = "01520";
                            }
                            line = agenceDepot + ";"
                                    + " " + Utility.bourrageGauche("" + refLot, 10, "0") + ";"
                                    + Utility.convertDateToString(new Date(), "dd/MM/yy") + ";"
                                    + " " + Utility.bourrageGauche("" + remises[0].getIdremise().toPlainString(), 10, "0") + ";"
                                    + Utility.bourrageDroite(remises[0].getCompteRemettant(), 20, " ") + ";"
                                    + Utility.bourrageGauche(remises[0].getMontant(), 15, "0") + "+;"
                                    + Utility.convertDateToString(new Date(), "dd/MM/yy") + ";"
                                    + " " + Utility.bourrageGauche("" + x, 10, "0") + ";"
                                    + Utility.bourrageGauche(aCheque.getNumerocheque(), 7, "") + ";"
                                    + codeBanque + ";"
                                    + aCheque.getAgence() + ";"
                                    + aCheque.getNumerocompte() + ";"
                                    + Utility.computeCleRIB(aCheque.getBanque(), aCheque.getAgence(), aCheque.getNumerocompte()) + ";"
                                    + Utility.bourrageGauche(aCheque.getMontantcheque(), 15, "0") + "+;"
                                    + Utility.convertDateToString(new Date(), "dd/MM/yy") + ";"
                                    + Utility.convertDateToString(new Date(), "dd/MM/yy") + ";"
                                    + typeCheque + createBlancs(29, " ");
                            //0952015039800073    ;  001;26/09/18
                            wwriteln(line);

                            if (aCheque.getBanque().equalsIgnoreCase(Utility.getParam("CODE_BANQUE_SICA3"))) {
                                aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALSURCAIENVSIB")));

                                if (Utility.getParam("FLAG_INTERDIT") != null && Utility.getParam("FLAG_INTERDIT").equals("1")) {
                                    comptes = (Comptes[]) db.retrieveRowAsObject("SELECT * from COMPTES WHERE NUMERO LIKE '%" + aCheque.getNumerocompte().substring(1) + "%'"
                                            + " AND AGENCE LIKE '" + aCheque.getAgence() + "'", new Comptes());
                                    if (comptes != null && comptes.length > 0 && comptes[0].getEtat().equals(new BigDecimal(Utility.getParam("CETAOPEANO")))) {
                                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPESURCAIREJ")));
                                        aCheque.setMotifrejet("103");

                                    }
                                }

                            } else {
                                aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
                            }

                            //    db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                            chequesDTO.add(aCheque);
                        }

                        db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());

                        //Remise a 50 & cheques a 50
                        //  select * from remises where idremise in (select remise from cheques where etat=50) and etat=50
                    }

                } else {

                    db.executeUpdate("UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPEERR") + " WHERE REMISE=" + cheques[i].getRemise());
                    db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEERR") + " WHERE IDREMISE=" + cheques[i].getRemise());
                }

            }
            if (!chequesDTO.isEmpty()) {
                //a ameliorer risk de OutOfMemoryError 
                System.out.println("cheques  size ExportAllerAtlasBFWriter :" + chequesDTO.size());
                final int batchSize = 1000;
                int count = 0;
                for (Cheques chequeDTO : chequesDTO) {
                    db.updateRowByObjectByQuery(chequeDTO, "CHEQUES", "IDCHEQUE=" + chequeDTO.getIdcheque());
                }

            }
            setDescription(getDescription() + " execute avec succes: Nombre de Cheque= " + cheques.length + " - Montant Total= " + montantTotal);
            logEvent("INFO", "Nombre de Cheque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));

            closeFile();

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
