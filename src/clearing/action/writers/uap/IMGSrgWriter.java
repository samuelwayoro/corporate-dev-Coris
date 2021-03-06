/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.uap;

import clearing.action.readers.bfi.CatPakAcpAchAllerReader;
import clearing.model.CMPUtility;
import clearing.model.EnteteRemise;
import clearing.model.ImageIndex;
import clearing.model.RIO;
import clearing.table.Agences;
import clearing.table.Banques;
import clearing.table.Cheques;
import clearing.table.Effets;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.patware.action.file.BinFileWriter;
import static org.patware.action.file.BinFileWriter.createBlancs;
import org.patware.bean.table.Compteur;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class IMGSrgWriter extends BinFileWriter {

    private String sql = "";
    private String sequence = "";
    EnteteRemise enteteRemise = new EnteteRemise();
    private byte[] rImageBytes;
    private byte[] vImageBytes;
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

    public IMGSrgWriter() {

        setDescription("Envoi Mailis Sous Regionaux vers BCEAO");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        Banques banques[] = CMPUtility.getBanquesSousRegionales(db, " ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ")");

        if (banques != null) {

            //Verifier si le compteur pour la date compense nationale existe; si oui et s'il est strictement superieur a nbBanquesNationales+1 nothing to do
            //si non initialiser le compteur pour la date compense nationale du Jour � nbBanquesNationales+1
            Utility.clearParamsCache();
            Compteur compteurSrg = new Compteur();
            compteurSrg.setNom("MAILI_SRG");
            compteurSrg.setObjet(Utility.getParam("DATECOMPENS_SRG"));
            //sql = "UPDATE COMPTEUR SET VALEUR='" + computeCompteur + "' WHERE NOM='MAILI_SRG' AND OBJET='" + Utility.getParam("DATECOMPENS_SRG") + "'";
            //                db.executeUpdate(sql);
            System.out.println("###################################################################################");
            sql = "SELECT * from  COMPTEUR WHERE    NOM='" + compteurSrg.getNom() + "' AND OBJET='" + compteurSrg.getObjet() + "'";
            Compteur[] compteur = (Compteur[]) db.retrieveRowAsObject(sql, new Compteur());
            if (compteur != null && compteur.length > 0) {

                if (Integer.parseInt(compteur[0].getValeur().trim()) > 300) {
                    System.out.println("nothing to do ");
                } else {
                    compteur[0].setValeur("300");
                    sql = "UPDATE COMPTEUR SET VALEUR='" + compteur[0].getValeur() + "' WHERE NOM='MAILI_SRG' AND OBJET='" + Utility.getParam("DATECOMPENS_SRG") + "'";
                    db.executeUpdate(sql);
                    //    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }
                compteurSrg = compteur[0];

            } else if (compteur.length == 0) {
                //n'existe pas en BD
                compteurSrg.setValeur("300");
                db.insertObjectAsRowByQuery(compteurSrg, "COMPTEUR");

            }

            for (int i = 0; i < banques.length; i++) {
                Banques banque = banques[i];

                if (Utility.getParam("GENMAILIPARBANQUE") != null && Utility.getParam("GENMAILIPARBANQUE").equalsIgnoreCase("1")) {
                    //Recuperation des cheques d'une Banque
                    sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND TYPE_CHEQUE ='030' AND BANQUE LIKE '" + banque.getCodebanque() + "' AND ETATIMAGE = " + Utility.getParam("CETAIMASTO");
                    cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                    prepareCheques(cheques, compteurSrg);

                    //Recuperation des cheques d'une Banque
                    sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND TYPE_CHEQUE ='035' AND BANQUE LIKE '" + banque.getCodebanque() + "' AND ETATIMAGE = " + Utility.getParam("CETAIMASTO");
                    cheques35 = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                    prepareCheques(cheques35, compteurSrg);

                    //Recuperation des effets d'une Banque
                    sql = "SELECT * FROM EFFETS WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND TYPE_EFFET ='045' AND BANQUE LIKE '" + banque.getCodebanque() + "' AND ETATIMAGE = " + Utility.getParam("CETAIMASTO");
                    effets45 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                    prepareEffets(effets45);

                    //Recuperation des effets d'une Banque
                    sql = "SELECT * FROM EFFETS WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND TYPE_EFFET ='046' AND BANQUE LIKE '" + banque.getCodebanque() + "' AND ETATIMAGE = " + Utility.getParam("CETAIMASTO");
                    effets46 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                    prepareEffets(effets46);

                } else {

                    sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '" + banque.getCodebanque() + "'";

                    Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());

                    if (agences != null) {
                        for (int j = 0; j < agences.length; j++) {
                            Agences agence = agences[j];

                            //Recuperation des cheques d'une Banque
                            sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND TYPE_CHEQUE ='030' AND BANQUE LIKE '" + banque.getCodebanque() + "' AND AGENCE LIKE '" + agence.getCodeagence() + "' AND ETATIMAGE = " + Utility.getParam("CETAIMASTO");
                            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                            prepareCheques(cheques, compteurSrg);

                            //Recuperation des cheques d'une Banque
                            sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND TYPE_CHEQUE ='035' AND BANQUE LIKE '" + banque.getCodebanque() + "' AND AGENCE LIKE '" + agence.getCodeagence() + "' AND ETATIMAGE = " + Utility.getParam("CETAIMASTO");
                            cheques35 = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                            prepareCheques(cheques35, compteurSrg);

                            //Recuperation des effets d'une Banque
                            sql = "SELECT * FROM EFFETS WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND TYPE_EFFET ='045' AND BANQUE LIKE '" + banque.getCodebanque() + "' AND AGENCE LIKE '" + agence.getCodeagence() + "' AND ETATIMAGE = " + Utility.getParam("CETAIMASTO");
                            effets45 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                            prepareEffets(effets45);

                            //Recuperation des effets d'une Banque
                            sql = "SELECT * FROM EFFETS WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND TYPE_EFFET ='046' AND BANQUE LIKE '" + banque.getCodebanque() + "' AND AGENCE LIKE '" + agence.getCodeagence() + "' AND ETATIMAGE = " + Utility.getParam("CETAIMASTO");
                            effets46 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                            prepareEffets(effets46);
                        }
                    }

                }

            }
        }

        updateEtatOperations(db);

        db.close();

        logEndOfTask();
    }

    public File[] listOfFiles(String path) {
        System.out.println("listOfFiles path" + path);
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        return listOfFiles;
    }

    private void prepareCheques(Cheques[] cheques, Compteur compteur) throws Exception {

        if (cheques != null && cheques.length > 0) {
            remiseHasCheques = true;
            enteteRemise.setIdEntete("EIMG");
            if (Utility.getParam("VERSION_SICA").equals("2")) {
                enteteRemise.setIdEmetteur("KSCSR");
            } else {
                enteteRemise.setIdEmetteur("SNSSR");
            }

            sequence = Utility.bourrageGauche(Utility.computeCompteur(compteur.getNom(), compteur.getObjet()), 3, "0");
//            sequence = Utility.bourrageGauche("" + sequenc, 3, "0");
//            sequence = Utility.bourrageGZero(Utility.computeCompteur("MAILI_SRG", Utility.getParam("DATECOMPENS_SRG")), 3);

            enteteRemise.setRefRemise(sequence);
            enteteRemise.setDatePresentation(Utility.convertStringToDate(Utility.getParam("DATECOMPENS_SRG"), "yyyyMMdd"));
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
            CMPUtility.insertRemcom(enteteRemise, Integer.parseInt(Utility.getParam("CETAOPEALLICOM1ACC")));
//            String fileName = CMPUtility.getMailiSrgFileName(enteteRemise.getIdDestinataire(), enteteRemise.getCodeLieu(), sequence, "IMC", "MAILI");
//MAILI_NAT_FOLDER        
//MAILI_SRG_FOLDER                                  
            String fileName = Utility.getParam("IMG_SRG_FOLDER") + File.separator
                    + cheques[0].getBanqueremettant() + ".001." + sequence + "."
                    + cheques[0].getType_Cheque() + "." + cheques[0].getBanque() + "." + CMPUtility.getDevise() + ".IMG";
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
                imageIndex.setPfxIBANDeb(createBlancs(4, " "));
                imageIndex.setIdBanTire(cheque.getBanque());
                imageIndex.setIdAgeTire(cheque.getAgence());
                imageIndex.setNumCptTire(cheque.getNumerocompte());
                imageIndex.setCleRibDeb(cheque.getRibcompte());
                imageIndex.setIdBanRem(cheque.getBanqueremettant());
                imageIndex.setIdAgeRem(cheque.getAgenceremettant());
                imageIndex.setRio(new RIO(cheque.getRio()));
                imageIndex.setDateOrdreClient(Utility.convertStringToDate(cheque.getDatetraitement(), ResLoader.getMessages("patternDate")));
                File aRectoFile = new File(cheque.getPathimage() + File.separator + cheque.getFichierimage() + "f.jpg");
                imageIndex.setLongRecto(String.valueOf(aRectoFile.length()));
                File aVersoFile = new File(cheque.getPathimage() + File.separator + cheque.getFichierimage() + "r.jpg");
                imageIndex.setLongVerso(String.valueOf(aVersoFile.length()));
                imageIndex.setMontant(cheque.getMontantcheque());
                imageIndex.setCodeCmc7(String.valueOf(cheque.getIndicateurmodificationcmc7()));

                rImageBytes = new byte[(int) aRectoFile.length()];
                BufferedInputStream isRecto = openFile(aRectoFile);
                isRecto.read(rImageBytes);
                vImageBytes = new byte[(int) aVersoFile.length()];
                BufferedInputStream isVerso = openFile(aVersoFile);
                isVerso.read(vImageBytes);
                Boolean mayTattoo = Boolean.valueOf(Utility.getParam("TATOO_PICTURE"));
                if (mayTattoo.booleanValue()) {

                    BufferedImage bimg = Utility.createImageFromBytes(vImageBytes);
                    String paramText = "DATECOMPENS_SRG";
                    Utility.tattooPictureDB(bimg, null, paramText);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ImageIO.setUseCache(false);
                    ImageIO.write(bimg, "jpg", outputStream);
                    vImageBytes = outputStream.toByteArray();
                    imageIndex.setLongVerso(String.valueOf(vImageBytes.length));

                }
                isVerso.close();
                isRecto.close();

                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    line = new String(imageIndex.getTypeOperation() + imageIndex.getNumCheque() + createBlancs(10, "0")
                            + "0" + imageIndex.getPfxIBANDeb() + imageIndex.getIdBanTire() + imageIndex.getIdAgeTire()
                            + imageIndex.getNumCptTire() + imageIndex.getCleRibDeb() + imageIndex.getIdBanRem()
                            + imageIndex.getIdAgeRem() + imageIndex.getRio().getRio() + Utility.convertDateToString(imageIndex.getDateOrdreClient(), "yyyyMMdd")
                            + imageIndex.getLongRecto() + imageIndex.getLongVerso() + imageIndex.getMontant()
                            + imageIndex.getCodeCmc7() + createBlancs(263, " "));

                } else {
                    line = new String(imageIndex.getTypeOperation() + imageIndex.getNumCheque()
                            + imageIndex.getIdBanTire() + imageIndex.getIdAgeTire()
                            + imageIndex.getNumCptTire() + imageIndex.getCleRibDeb()
                            + imageIndex.getIdBanRem()
                            + imageIndex.getIdAgeRem() + imageIndex.getRio().getRio()
                            + Utility.convertDateToString(imageIndex.getDateOrdreClient(), "yyyyMMdd")
                            + imageIndex.getLongRecto() + imageIndex.getLongVerso()
                            + imageIndex.getMontant()
                            + imageIndex.getCodeCmc7() + createBlancs(278, " "));

                }
                getOut().println(line);
                getOut().write(rImageBytes);
                getOut().println();
                getOut().write(vImageBytes);
                getOut().println();
                cheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMATRA")));
                cheque.setFichiermaili(fileName);

                db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE = " + cheque.getIdcheque());

            }

            getOut().close();
            OrdFileWriter ordFileWriter = new OrdFileWriter(cheques, sequence);
            ordFileWriter.execute();
        }
    }

    private void prepareEffets(Effets[] effets) throws Exception {

        if (cheques != null && effets.length > 0) {
            remiseHasEffets = true;
            enteteRemise.setIdEntete("EIMG");
            if (Utility.getParam("VERSION_SICA").equals("2")) {
                enteteRemise.setIdEmetteur("KSCSR");
            } else {
                enteteRemise.setIdEmetteur("SNSSR");
            }
            sequence = Utility.bourrageGZero(Utility.computeCompteur("MAILI_SRG", Utility.getParam("DATECOMPENS_SRG")), 3);
            enteteRemise.setRefRemise(sequence);
            enteteRemise.setDatePresentation(Utility.convertStringToDate(Utility.getParam("DATECOMPENS_SRG"), "yyyyMMdd"));
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

            String fileName = CMPUtility.getMailiSrgFileName(enteteRemise.getIdDestinataire(), enteteRemise.getCodeLieu(), sequence, "IME", "MAILI");
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
                imageIndex.setRio(new RIO(effet.getRio()));
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
                        + imageIndex.getIdAgeRem() + imageIndex.getRio().getRio() + imageIndex.getDateOrdreClient()
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
            l_sql = "UPDATE CHEQUES SET ETATIMAGE=" + Utility.getParam("CETAIMAENV") + " WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND ETATIMAGE=" + Utility.getParam("CETAIMATRA");
            db.executeUpdate(l_sql);
        }

        if (remiseHasEffets) {
            l_sql = "UPDATE EFFETS SET ETATIMAGE=" + Utility.getParam("CETAIMAENV") + " WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ") AND ETATIMAGE=" + Utility.getParam("CETAIMATRA");
            db.executeUpdate(l_sql);
        }

    }
}
