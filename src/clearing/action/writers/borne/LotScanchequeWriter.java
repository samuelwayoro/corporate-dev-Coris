/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.borne;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Comptes;
import clearing.table.Remises;
import java.io.File;
import java.math.BigDecimal;
import org.apache.commons.io.FileUtils;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;



/**
 *
 * @author AUGOU Patrick
 */
public class LotScanchequeWriter extends FlatFileWriter {

    public LotScanchequeWriter() {
        setDescription("Envoi des chèques vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String refLot = Utility.computeCompteur("REFLOT", "BORNE");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_FILE_ROOTNAME") + refLot + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));

        String sql = "UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPEERR") + " WHERE ETAT=" + Utility.getParam("CETAREMSAIIRIS") + " AND  (MONTANTCHEQUE IS NULL OR TRIM(MONTANTCHEQUE)='')";
        db.executeUpdate(sql);
        sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAREMSAIIRIS") + " ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

        sql = "SELECT * FROM REMISES WHERE ETAT=" + Utility.getParam("CETAREMSAIIRIS");
        Remises[] remisesAll = (Remises[]) db.retrieveRowAsObject(sql, new Remises());


        int j = 0;
        long montantTotal = 0;
        int numRemise = 0;


        if (cheques != null && 0 < cheques.length) {
            String line = "#dataAndImage";
            wwriteln(line);
            //L;04572;33;20050825;O;20050826;N;20050826;1;1
            line = "L;" + Utility.getParam("AGENCE_BORNE") + ";" + refLot + ";" + CMPUtility.getDate() + ";" + "N" + ";" + Utility.getParam("DATECOMPENS_NAT") + ";N;" + Utility.getParam("DATECOMPENS_NAT") + ";" + remisesAll.length + ";" + cheques.length;
            wwriteln(line);

            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques validés


                sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[i].getRemise();
                Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());

                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT >=" + Utility.getParam("CETAREMSAIIRIS");
                Cheques[] allChequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                j = allChequesVal.length;
                numRemise++;

                if ((remises != null && 0 < remises.length)) {
                    if (allChequesVal != null && 0 < allChequesVal.length) {
                        long sumRemise = 0;


                        //Creation Ligne remise a partir du premier chq
                        Cheques aCheque = allChequesVal[0];
                        for (int x = 0; x < allChequesVal.length; x++) {
                            sumRemise += Long.parseLong(allChequesVal[x].getMontantcheque());
                        }
                        montantTotal += sumRemise;

                        String numCptEx = "";
                        Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numero  ='" + Utility.bourrageGZero(remises[0].getCompteRemettant(), 12) + "' and agence ='" + remises[0].getAgenceRemettant() + "'", new Comptes());
                        if (comptes != null && comptes.length > 0) {
                            numCptEx = comptes[0].getNumcptex().trim();
                        }



                        //R;04572;33;1;0;09572025466000;78; ; ; ;1;120000;XOF;0;20050826;04572;N;0;4;0;
                        line = "R;" + Utility.getParam("AGENCE_BORNE") + ";" + refLot + ";" + numRemise + ";" + "0" + ";" + numCptEx + ";" + " ; ;" + remises[0].getNomClient() + ";" + "XOF;" + allChequesVal.length + ";" + sumRemise + ";XOF;0;" + Utility.getParam("DATECOMPENS_NAT") + ";" + Utility.getParam("AGENCE_BORNE") + ";" + "N;1;4;0;";

                        wwriteln(line);



                        for (int x = 0; x < allChequesVal.length; x++) {
                            //Tous les chq de la remise
                            aCheque = allChequesVal[x];
                            //C;04572;33;1;3729837;10006;01550;010569700081;79;120000; ; ;20050826;20050826;jpg;04;0;4;10006015500105697000813729837F.jpg;10006015500105697000813729837B.jpg
                            String codeBanque = Utility.getParamOfType(aCheque.getBanque().substring(0, 2), "CODE_SICA3").trim() + aCheque.getBanque().substring(2);
                            String nomImage = codeBanque + aCheque.getAgence() + aCheque.getNumerocompte() + aCheque.getNumerocheque();
                            String typeCheque = "";

                            if (CMPUtility.isBanqueNationale(aCheque.getBanque())) {
                                if (aCheque.getBanque().equalsIgnoreCase(Utility.getParam("CODE_BANQUE_SICA3"))) {
                                    typeCheque = "01";
                                    if (comptes[0].getEtat().equals(new BigDecimal(10))) {
                                        typeCheque = "21";
                                    }
                                } else {
                                    typeCheque = "05";
                                    if (comptes[0].getEtat().equals(new BigDecimal(10))) {
                                        typeCheque = "25";
                                    }

                                }

                            }else{
                                typeCheque = "10";
                                    if (comptes[0].getEtat().equals(new BigDecimal(10))) {
                                        typeCheque = "30";
                                    }
                            }

                            line = "C;" + Utility.getParam("AGENCE_BORNE") + ";" + 
                                    refLot + ";" + numRemise + ";" +
                                    Utility.bourrageGauche(aCheque.getNumerocheque(), 7, "0") +
                                    ";" + codeBanque + ";" + aCheque.getAgence() + ";" +
                                    aCheque.getNumerocompte() + ";" + aCheque.getRibcompte() + ";" +
                                    aCheque.getMontantcheque() + "; ; ;" +
                                    Utility.getParam("DATECOMPENS_NAT") + ";" +
                                    Utility.getParam("DATECOMPENS_NAT") + ";" +
                                    "jpg;"+typeCheque+";1;4;" + nomImage + "F.jpg;" + nomImage + "B.jpg;";

                            wwriteln(line);
                            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
                            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                            //Copie des images
                            String imageFolderName = Utility.getParam("SCANCHEQUES_IMG_FOLDER") +
                                    File.separator + Utility.getParam("DATECOMPENS_NAT") +
                                    File.separator + Utility.getParam("AGENCE_BORNE") +
                                    File.separator + refLot +
                                    File.separator + numRemise;
                            File aRectoFile = null;
                            File aVersoFile = null;
                            if (Utility.createFolderIfItsnt(new File(imageFolderName), this)) {
                                aRectoFile = new File(aCheque.getPathimage() + File.separator + aCheque.getFichierimage() + "f.jpg");
                                aVersoFile = new File(aCheque.getPathimage() + File.separator + aCheque.getFichierimage() + "r.jpg");

                                FileUtils.copyFile(aRectoFile, new File(imageFolderName + File.separator + nomImage + "F.jpg"));
                                FileUtils.copyFile(aVersoFile, new File(imageFolderName + File.separator + nomImage + "B.jpg"));


                            }
                            db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());

                        }

                    }

                } else {

                    db.executeUpdate("UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPEERR") + " WHERE REMISE=" + cheques[i].getRemise());
                    db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEERR") + " WHERE IDREMISE=" + cheques[i].getRemise());
                }


            }
            setDescription(getDescription() + " exécuté avec succès: Nombre de Chèque= " + cheques.length + " - Montant Total= " + montantTotal);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + montantTotal);
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }
        closeFile();
        db.close();
    }
}
