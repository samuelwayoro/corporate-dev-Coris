/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.corporates;

import clearing.model.CMPUtility;
import clearing.model.EnteteRemise;
import clearing.model.ImageIndex;
import clearing.table.Agences;
import clearing.table.Banques;
import clearing.table.Cheques;
import clearing.table.Effets;
import clearing.table.Remises;
import clearing.table.Utilisateurs;
import java.io.BufferedInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import org.patware.action.file.BinFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class MailiIntUtiWriter extends BinFileWriter {

    private String sql = "";
    private String sequence = "";
    EnteteRemise enteteRemise = new EnteteRemise();
    private byte[] imageBytes;
    private ImageIndex imageIndex = new ImageIndex();
    Cheques cheques[] = null;
    Cheques cheques35[] = null;
    Effets effets40[] = null;
    Effets effets41[] = null;
    Effets effets42[] = null;
    Effets effets43[] = null;
    Effets effets45[] = null;
    Effets effets46[] = null;
    private boolean remiseHasCheques = false;
    private boolean remiseHasEffets = false;
    private DataBase db;

    public MailiIntUtiWriter() {

        setDescription("Envoi Mailis Internes vers Banque");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        long nbCheques = 0;
        Utilisateurs user = (Utilisateurs) getParametersMap().get("user");

        //Recuperation des Banques NATIONALES SELON LA VERSION SICA EN COURS
        String corporateOffline = Utility.getParam("CORPORATE_OFFLINE"); //                                        
        if (corporateOffline != null && corporateOffline.equals("1")) { //Gestion des Corporates OFFLINE. Ajout de lupdate des cheques si  CORPORATE_OFFLINE est a 1
            String l_sql = "UPDATE CHEQUES SET  ETAT=" + Utility.getParam("CETAOPEVALDELTA") + " WHERE ETAT =" + Utility.getParam("CETAOPEVAL");
            db.executeUpdate(l_sql);
        }
        String parRemise = Utility.getParam("GENMAIPARREM"); //  

        long sumCheques = 0;
        if (parRemise != null && parRemise.equals("1")) {
            sql = " SELECT * FROM REMISES WHERE ETABLISSEMENT='" + user.getAdresse().trim() + "'   AND IDREMISE IN (SELECT DISTINCT REMISE FROM CHEQUES WHERE  ETAT = " + Utility.getParam("CETAOPEVALDELTA") + ")";
            Remises remises[] = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
            if (remises != null) {
                for (int i = 0; i < remises.length; i++) {
                    Remises remise = remises[i];

                    sql = "SELECT * FROM CHEQUES WHERE ETAT = " + Utility.getParam("CETAOPEVALDELTA") + " AND  ETABLISSEMENT='" + user.getAdresse().trim() + "'  AND TYPE_CHEQUE ='035' AND REMISE LIKE '" + remise.getIdremise() + "' "
                            + " AND ETATIMAGE = " + Utility.getParam("CETAIMASTO");
                    cheques35 = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                    prepareCheques(cheques35, db);
                    for (Cheques cheque : cheques35) {
                        //sumCheques
                        sumCheques += Long.parseLong(cheque.getMontantcheque());

                    }
                    nbCheques += cheques35.length;

//       
                }
            }

        } else {
            Banques banques[] = CMPUtility.getBanques(db, " ETAT = " + Utility.getParam("CETAOPEVALDELTA"));
            if (banques != null) {
                for (int i = 0; i < banques.length; i++) {
                    Banques banque = banques[i];
                    sql = "SELECT * FROM CHEQUES WHERE ETAT = " + Utility.getParam("CETAOPEVALDELTA") + " AND  ETABLISSEMENT='" + user.getAdresse().trim() + "'  AND TYPE_CHEQUE ='035' AND BANQUE LIKE '" + banque.getCodebanque() + "' "
                            + " AND ETATIMAGE = " + Utility.getParam("CETAIMASTO");
                    cheques35 = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                    prepareCheques(cheques35, db);
                    for (Cheques cheque : cheques35) {
                        //sumCheques
                        sumCheques += Long.parseLong(cheque.getMontantcheque());

                    }
                    nbCheques += cheques35.length;

//       
                }
            }
        }

        setDescription(getDescription() + " exécuté avec succès:\n Nombre de Chèques= " + nbCheques + " Montant  " + (sumCheques != 0 ? Utility.formatNumber("" + sumCheques) : ""));
        logEvent("INFO", "Nombre de Chèques = " + nbCheques);

        updateEtatOperations(db);

        db.close();
        logEndOfTask();

    }

    private void prepareCheques(Cheques[] cheques, DataBase db) throws Exception {

        if (cheques != null && cheques.length > 0) {
            remiseHasCheques = true;
            enteteRemise.setIdEntete("EIMG");
            if (Utility.getParam("VERSION_SICA").equals("2")) {
                enteteRemise.setIdEmetteur(CMPUtility.getCodeBanque().charAt(0) + "SCPM");
            } else {
                enteteRemise.setIdEmetteur(CMPUtility.getCodeBanqueSica3().substring(0, 2) + Utility.getParam("CONSTANTE_SICA").trim());
            }
//            sequence = Utility.bourrageGZero(Utility.computeCompteur("MAILI_INT", Utility.getParam("DATECOMPENS_NAT")), 3);
            sequence = Utility.bourrageGauche(Utility.computeCompteur("MAILI_INT", Utility.getParam("DATECOMPENS_NAT")), 3, "0");
            enteteRemise.setRefRemise(sequence);
            enteteRemise.setDatePresentation(Utility.convertStringToDate(Utility.getParam("DATECOMPENS_NAT"), "yyyyMMdd"));
            if (Utility.getParam("VERSION_SICA").equals("2")) {
                enteteRemise.setIdRecepeteur(CMPUtility.getCodeBanque());
            } else {
                enteteRemise.setIdRecepeteur(CMPUtility.getCodeBanqueSica3());
            }
            enteteRemise.setDevise(CMPUtility.getDevise());
            if (Utility.getParam("VERSION_SICA").equals("2")) {
                enteteRemise.setTypeRemise("IMAGC");
            } else {
                enteteRemise.setTypeRemise("IMC");
            }

            enteteRemise.setIdDestinataire(CMPUtility.getCodeBanqueDestinataire(cheques[0].getBanque()));
            enteteRemise.setCodeLieu(cheques[0].getVille());
            enteteRemise.setNbTotOperVal(String.valueOf(cheques.length));
            if (Utility.getParam("VERSION_SICA").equals("2")) {
                enteteRemise.setBlancs(createBlancs(20, " "));
            } else {
                enteteRemise.setBlancs(createBlancs(22, " "));
            }

            String fileName = CMPUtility.getMailiNatFileName(cheques[0].getEtablissement().trim(), enteteRemise.getCodeLieu(), sequence, "IMC", "MAILI");
            setOut(createBinFile(fileName));
            String line = new String(enteteRemise.getIdEntete()
                    + enteteRemise.getIdEmetteur()
                    + enteteRemise.getRefRemise()
                    + Utility.convertDateToString(enteteRemise.getDatePresentation(), "yyyyMMdd")
                    + enteteRemise.getIdRecepeteur()
                    + enteteRemise.getDevise()
                    + enteteRemise.getTypeRemise()
                    + enteteRemise.getIdDestinataire()
                    + enteteRemise.getCodeLieu()
                    + enteteRemise.getNbTotOperVal()
                    + enteteRemise.getBlancs());
            getOut().println(line);
            imageIndex = new ImageIndex();

            for (int j = 0; j < cheques.length; j++) {
                Cheques cheque = cheques[j];

                imageIndex.setTypeOperation(cheque.getType_Cheque());
                imageIndex.setNumCheque(cheque.getNumerocheque());
                // imageIndex.setPfxIBANDeb(createBlancs(4, " "));
                imageIndex.setIdBanTire(cheque.getBanque());
                imageIndex.setIdAgeTire(cheque.getAgence());
                imageIndex.setNumCptTire(cheque.getNumerocompte());
                imageIndex.setCleRibDeb(cheque.getRibcompte());
                imageIndex.setIdBanRem(cheque.getBanqueremettant());
                imageIndex.setIdAgeRem(cheque.getAgenceremettant());
                //imageIndex.setRio(new RIO(cheque.getRio()));
                imageIndex.setEtablissement(cheque.getEtablissement());
                imageIndex.setNomBeneficiaire(cheque.getNombeneficiaire());

                imageIndex.setDateOrdreClient(Utility.convertStringToDate(cheque.getDatetraitement(), ResLoader.getMessages("patternDate")));
                File aRectoFile = new File(cheque.getPathimage() + File.separator + cheque.getFichierimage() + "f.jpg");
                imageIndex.setLongRecto(String.valueOf(aRectoFile.length()));
                File aVersoFile = new File(cheque.getPathimage() + File.separator + cheque.getFichierimage() + "r.jpg");
                imageIndex.setLongVerso(String.valueOf(aVersoFile.length()));
                imageIndex.setMontant(cheque.getMontantcheque());
                imageIndex.setCodeCmc7(String.valueOf(cheque.getIndicateurmodificationcmc7()));
                imageIndex.setCompteRemettant(cheque.getCompteremettant());
                // imageIndex.setRibCompte(cheque.getRibcompte());
                imageIndex.setEscompte("" + cheque.getEscompte());
                imageIndex.setVilleRemettant(cheque.getVilleremettant());
                imageIndex.setCodeUtilisateur(cheque.getCodeutilisateur());
                imageIndex.setMachineScan(cheque.getMachinescan());
                imageIndex.setReferenceRemise("" + cheque.getRemise());
                imageIndex.setReferenceExterne(cheque.getRefremise());
                imageIndex.setNumEffet(Utility.bourrageGauche("" + cheque.getIdcheque(), 10, "0"));

                Remises[] aRemise = (Remises[]) db.retrieveRowAsObject("SELECT * FROM REMISES WHERE IDREMISE= " + cheque.getRemise(), new Remises());

                if (aRemise != null && aRemise.length > 0) {
                    imageIndex.setNbrOperRemise("" + aRemise[0].getNbOperation());
                    imageIndex.setMontantRemise(aRemise[0].getMontant());
                } else {
                    imageIndex.setNbrOperRemise("1");
                    imageIndex.setMontantRemise(cheque.getMontantcheque());
                }

                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    line = new String(imageIndex.getTypeOperation() + imageIndex.getNumCheque() + createBlancs(10, "0")
                            + "0" + imageIndex.getPfxIBANDeb() + imageIndex.getIdBanTire() + imageIndex.getIdAgeTire()
                            + imageIndex.getNumCptTire() + imageIndex.getCleRibDeb() + imageIndex.getIdBanRem()
                            + imageIndex.getIdAgeRem() + imageIndex.getEtablissement() + imageIndex.getNomBeneficiaire() + Utility.convertDateToString(imageIndex.getDateOrdreClient(), "yyyyMMdd")
                            + imageIndex.getLongRecto() + imageIndex.getLongVerso() + imageIndex.getMontant()
                            + imageIndex.getCodeCmc7()
                            + imageIndex.getCompteRemettant()
                            + imageIndex.getEscompte()
                            + imageIndex.getVilleRemettant()
                            + imageIndex.getCodeUtilisateur()
                            + imageIndex.getMachineScan()
                            + imageIndex.getReferenceRemise()
                            + imageIndex.getReferenceExterne()
                            + createBlancs(263, " "));

                } else {
                    line = imageIndex.getTypeOperation() + imageIndex.getNumCheque()
                            + imageIndex.getIdBanTire() + imageIndex.getIdAgeTire()
                            + imageIndex.getNumCptTire() + imageIndex.getCleRibDeb()
                            + imageIndex.getIdBanRem()
                            + imageIndex.getIdAgeRem() + imageIndex.getEtablissement()
                            + imageIndex.getNomBeneficiaire()
                            + Utility.convertDateToString(imageIndex.getDateOrdreClient(), "yyyyMMdd")
                            + imageIndex.getLongRecto() + imageIndex.getLongVerso()
                            + imageIndex.getMontant()
                            + imageIndex.getCodeCmc7()
                            + imageIndex.getCompteRemettant()
                            + imageIndex.getEscompte()
                            + imageIndex.getVilleRemettant()
                            + imageIndex.getCodeUtilisateur()
                            + imageIndex.getMachineScan()
                            + imageIndex.getReferenceRemise()
                            + imageIndex.getReferenceExterne()
                            + imageIndex.getNbrOperRemise()
                            + imageIndex.getMontantRemise()
                            + imageIndex.getNumEffet()
                            + createBlancs(84, " ");

                }
                getOut().println(line);
                imageBytes = new byte[(int) aRectoFile.length()];
                BufferedInputStream isRecto = openFile(aRectoFile);
                isRecto.read(imageBytes);
                getOut().write(imageBytes);
                getOut().println();
                imageBytes = new byte[(int) aVersoFile.length()];
                BufferedInputStream isVerso = openFile(aVersoFile);
                isVerso.read(imageBytes);
                getOut().write(imageBytes);
                getOut().println();
                cheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMATRA")));
                cheque.setFichiermaili(fileName);
                db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE = " + cheque.getIdcheque());

            }

            getOut().close();
        }
    }

    private void prepareEffets(Effets[] effets) throws Exception {

        if (cheques != null && effets.length > 0) {
            remiseHasEffets = true;
            enteteRemise.setIdEntete("EIMG");
            if (Utility.getParam("VERSION_SICA").equals("2")) {
                enteteRemise.setIdEmetteur(CMPUtility.getCodeBanque().charAt(0) + "SCPM");
            } else {
                enteteRemise.setIdEmetteur(CMPUtility.getCodeBanqueSica3().substring(0, 2) + Utility.getParam("CONSTANTE_SICA").trim());
            }
            sequence = Utility.bourrageGZero(Utility.computeCompteur("MAILI_NAT", Utility.getParam("DATECOMPENS_NAT")), 3);
            enteteRemise.setRefRemise(sequence);
            enteteRemise.setDatePresentation(Utility.convertStringToDate(Utility.getParam("DATECOMPENS_NAT"), "yyyyMMdd"));
            if (Utility.getParam("VERSION_SICA").equals("2")) {
                enteteRemise.setIdRecepeteur(CMPUtility.getCodeBanque());
            } else {
                enteteRemise.setIdRecepeteur(CMPUtility.getCodeBanqueSica3());
            }
            enteteRemise.setDevise(CMPUtility.getDevise());
            enteteRemise.setTypeRemise("IMAGF");
            enteteRemise.setIdDestinataire(effets[0].getBanque());
            enteteRemise.setCodeLieu(effets[0].getVille());
            enteteRemise.setNbTotOperVal(String.valueOf(effets.length));
            enteteRemise.setBlancs(createBlancs(20, " "));

            String fileName = CMPUtility.getMailiNatFileName(enteteRemise.getIdDestinataire(), enteteRemise.getCodeLieu(), sequence, "IME", "MAILI");
            setOut(createBinFile(fileName));
            String line = new String(enteteRemise.getIdEntete() + enteteRemise.getIdEmetteur() + enteteRemise.getRefRemise() + Utility.convertDateToString(enteteRemise.getDatePresentation(), "yyyyMMdd") + enteteRemise.getIdRecepeteur() + enteteRemise.getDevise() + enteteRemise.getTypeRemise() + enteteRemise.getIdDestinataire() + enteteRemise.getCodeLieu() + enteteRemise.getNbTotOperVal() + enteteRemise.getBlancs());
            getOut().println(line);
            imageIndex = new ImageIndex();

            for (int j = 0; j < effets.length; j++) {
                Effets effet = effets[j];

                imageIndex.setTypeOperation(effet.getType_Effet());
                imageIndex.setNumCheque(effet.getNumeroeffet());
                imageIndex.setPfxIBANDeb(effet.getIban_Tire());
                imageIndex.setIdBanTire(effet.getBanque());
                imageIndex.setIdAgeTire(effet.getAgence());
                imageIndex.setNumCptTire(effet.getNumerocompte_Tire());
                imageIndex.setCleRibDeb(effet.getZoneinterbancaire_Tire());
                imageIndex.setIdBanRem(effet.getBanqueremettant());
                imageIndex.setIdAgeRem(effet.getAgenceremettant());
                //imageIndex.setRio(new RIO(effet.getRio()));

                imageIndex.setEtablissement(effet.getEtablissement());
                imageIndex.setCompteRemettant(effet.getNumerocompte_Beneficiaire());
                imageIndex.setDateOrdreClient(Utility.convertStringToDate(effet.getDatetraitement(), ResLoader.getMessages("patternDate")));
                File aRectoFile = new File(effet.getPathimage() + File.pathSeparator + effet.getFichierimage() + "f.jpg");
                imageIndex.setLongRecto(String.valueOf(aRectoFile.length()));
                File aVersoFile = new File(effet.getPathimage() + File.pathSeparator + effet.getFichierimage() + "r.jpg");
                imageIndex.setLongVerso(String.valueOf(aVersoFile.length()));
                imageIndex.setMontant(effet.getMontant_Effet());
                imageIndex.setCodeCmc7("0");
                line = new String(imageIndex.getTypeOperation() + createBlancs(7, "0") + imageIndex.getNumEffet()
                        + "0" + imageIndex.getPfxIBANDeb() + imageIndex.getIdBanTire() + imageIndex.getIdAgeTire()
                        + imageIndex.getNumCptTire() + imageIndex.getCleRibDeb() + imageIndex.getIdBanRem()
                        + imageIndex.getIdAgeRem() + imageIndex.getEtablissement() + imageIndex.getDateOrdreClient()
                        + imageIndex.getLongRecto() + imageIndex.getLongVerso() + imageIndex.getMontant()
                        + imageIndex.getCodeCmc7() + createBlancs(263, " "));
                getOut().println(line);
                imageBytes = new byte[(int) aRectoFile.length()];
                BufferedInputStream isRecto = openFile(aRectoFile);
                isRecto.read(imageBytes);
                getOut().write(imageBytes);
                getOut().println();
                imageBytes = new byte[(int) aVersoFile.length()];
                BufferedInputStream isVerso = openFile(aVersoFile);
                isVerso.read(imageBytes);
                getOut().write(imageBytes);
                getOut().println();
                effet.setEtatimage(new BigDecimal(Utility.getParam("CETAIMATRA")));
                db.updateRowByObjectByQuery(effet, "EFFETS", "IDEFFET = " + effet.getIdeffet());

            }

            getOut().close();
        }
    }

    private void updateEtatOperations(DataBase db) throws SQLException {
        String l_sql = "";

        if (remiseHasCheques) {
            l_sql = "UPDATE CHEQUES SET ETATIMAGE=" + Utility.getParam("CETAIMAENV") + ", ETAT=" + Utility.getParam("CETAOPEALLICOM1") + " WHERE ETAT =" + Utility.getParam("CETAOPEVALDELTA") + " AND ETATIMAGE=" + Utility.getParam("CETAIMATRA");
            db.executeUpdate(l_sql);
        }

        if (remiseHasEffets) {
            l_sql = "UPDATE EFFETS SET ETATIMAGE=" + Utility.getParam("CETAIMAENV") + ", ETAT=" + Utility.getParam("CETAOPEALLICOM1") + " WHERE ETAT =" + Utility.getParam("CETAOPEVALDELTA") + " AND ETATIMAGE=" + Utility.getParam("CETAIMATRA");
            db.executeUpdate(l_sql);
        }

    }
}
