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
public class ChequeAllerSitSystacWriter extends FlatFileWriter {

    public ChequeAllerSitSystacWriter() {
        setDescription("Envoi des chèques vers le SIB");
    }



    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_FILE_ROOTNAME") + CMPUtility.getDateHeure() + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));

        
        String sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ORDER BY COMPTEREMETTANT";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        if (cheques != null && 0 < cheques.length) {

            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques validés

                sql = "SELECT * FROM CHEQUES WHERE COMPTEREMETTANT='" + cheques[i].getCompteremettant() + "' AND ETAT =" + Utility.getParam("CETAOPEVAL") + " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "')";
                Cheques[] chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                j = chequesVal.length;
                
                    if (chequesVal != null && 0 < chequesVal.length) {
                    long sumRemise = 0;

                    //Creation Ligne remise a partir du premier chq
                    Cheques aCheque = chequesVal[0];
                    BigDecimal idRemise = new BigDecimal(Utility.computeCompteur("IDREMISE", "REMISES"));
                    for (int x = 0; x < chequesVal.length; x++) {
                        sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                        chequesVal[x].setRemise(idRemise);
                    }
                    String line = Utility.bourrageGauche(aCheque.getRemise().toPlainString(), 12, "0")
                                                       + createBlancs(9, "0")
                                                       + Utility.bourrageGauche(aCheque.getCompteremettant(), 11, "0")// 12 pour Ecobank, 11 pour Site Orion
                                                       + createBlancs(7, "0")
                                                       + aCheque.getBanqueremettant()
                                                       + aCheque.getAgenceremettant()
                                                       + aCheque.getVilleremettant()
                                                       + Utility.bourrageGauche(String.valueOf(sumRemise), 11, "0")
                                                       + ((aCheque.getOrigine().intValue()==1)?"O":"N")
                                                       + Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement().trim(),ResLoader.getMessages("patternDate")), "yyyyMMdd")
                                                       + createBlancs(7, "0");

                    writeln(line);

                    for (int x = 0; x < chequesVal.length; x++) {
                        //Tous les chq de la remise
                        aCheque = chequesVal[x];
                        line = Utility.bourrageGauche(aCheque.getRemise().toPlainString(), 12, "0")
                                                      + Utility.bourrageGauche(aCheque.getIdcheque().toPlainString(), 9, "0")
                                                      + Utility.bourrageGauche(aCheque.getNumerocompte(), 11, "0")
                                                      + Utility.bourrageGauche(aCheque.getNumerocheque(), 7, "0")
                                                      //+ CMPUtility.getCodeBanqueSICA2(aCheque.getBanque())
                                                      + aCheque.getBanque()
                                                      + aCheque.getAgence()
                                                      + aCheque.getRibcompte()
                                                      + Utility.bourrageGauche(aCheque.getMontantcheque(), 11, "0")
                                                      + ((aCheque.getOrigine().intValue()==1)?"O":"N")
                                                      + Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement().trim(),ResLoader.getMessages("patternDate")), "yyyyMMdd")
                                                      + createBlancs(7, "0");
                        writeln(line);
                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACCENVSIB")));
                        db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                    }
                   
                }else{
                   
                    db.executeUpdate("UPDATE CHEQUES SET ETAT ="+ Utility.getParam("CETAOPEERR")+" WHERE COMPTEREMETTANT="+ cheques[i].getCompteremettant()+ " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "')");
                    
                }
                

            }
        }
        closeFile();
        db.close();
    }
}
