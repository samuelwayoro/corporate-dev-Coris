/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.corporates;

import clearing.action.writers.corporates.MailaWriter;
import clearing.model.CMPUtility;
import clearing.model.EnteteRemise;
import clearing.model.ImageIndex;
import clearing.table.Cheques;
import clearing.table.Comptes;
import clearing.table.Effets;
import clearing.table.Remises;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import org.patware.action.file.BinFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class MailiIntReader extends BinFileReader {

    private byte[] bentete = new byte[65];
    private byte[] bindex = new byte[312 + 9];
    private String sentete = "";
    private String sindex = "";
    private EnteteRemise enteteRemise = new EnteteRemise();
    private ImageIndex imageIndex = new ImageIndex();
    private String imgAllerFolder = Utility.getParam("IMG_ALLER_FOLDER");
    private String imagePath;
    private String imageName;
    private static final int MAXIMGSIZE = 1000000;
    private MailaWriter mailaWriter;
    private String etat;

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    private void insertImageIndexIntoDB(DataBase db) throws SQLException {

        if (imageIndex.getTypeOperation().equals("030") || imageIndex.getTypeOperation().equals("035")) {
            Cheques aCheque = new Cheques();
            aCheque.setType_Cheque(imageIndex.getTypeOperation());
            aCheque.setNumerocheque(imageIndex.getNumCheque());
            aCheque.setOrigine(new BigDecimal(imageIndex.getNumEffet()));

            aCheque.setBanque(imageIndex.getIdBanTire());
            aCheque.setAgence(imageIndex.getIdAgeTire());
            aCheque.setNumerocompte(imageIndex.getNumCptTire());
            aCheque.setRibcompte(imageIndex.getCleRibDeb());
            aCheque.setBanqueremettant(imageIndex.getIdBanRem());
            aCheque.setAgenceremettant(imageIndex.getIdAgeRem());
            //aCheque.setRio(imageIndex.getRio().getRio());
            aCheque.setEtablissement(imageIndex.getEtablissement());
            aCheque.setNombeneficiaire(imageIndex.getNomBeneficiaire());
            aCheque.setCompteremettant(imageIndex.getCompteRemettant());

            //gestion Escompte Corporates Credit Immediat
            Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numero  ='" + Utility.bourrageGZero(aCheque.getCompteremettant(), 12) + "' and agence ='" + aCheque.getAgenceremettant() + "'", new Comptes());
            if (comptes != null && comptes.length > 0) {
                aCheque.setEscompte(comptes[0].getEtat());
            } else {
                aCheque.setEscompte(new BigDecimal(imageIndex.getEscompte()));
            }

            aCheque.setVilleremettant(imageIndex.getVilleRemettant());
            aCheque.setCodeutilisateur(imageIndex.getCodeUtilisateur());
            aCheque.setMachinescan(imageIndex.getMachineScan());
            aCheque.setRefremise(imageIndex.getReferenceExterne());
            aCheque.setDevise(enteteRemise.getDevise());

            aCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            aCheque.setDatesaisie(Utility.convertDateToString(imageIndex.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
            aCheque.setDateemission(Utility.convertDateToString(imageIndex.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
            //imageIndex.getLongRecto();
            //imageIndex.getLongVerso();
            aCheque.setMontantcheque(String.valueOf(Long.parseLong(imageIndex.getMontant())));
            aCheque.setIndicateurmodificationcmc7(new BigDecimal(imageIndex.getCodeCmc7()));
            aCheque.setPathimage(imagePath);
            aCheque.setFichierimage(imageName);

            //Gestion de la remise
            String sql = "SELECT * FROM REMISES WHERE ETAT IN (" + etat + ","
                    + Utility.getParam("CETAMANVALRC") + "," + Utility.getParam("CETAMANVALRNC")
                    + ")  AND COMPTEREMETTANT='" + aCheque.getCompteremettant()
                    + "' AND MONTANT='" + Long.parseLong(imageIndex.getMontantRemise())
                    + "' AND DATESAISIE='" + aCheque.getDatesaisie()
                    + "' AND ETABLISSEMENT='" + aCheque.getEtablissement()
                    + "' AND REFERENCE='" + imageIndex.getReferenceExterne().trim()
                    + "' AND NOMUTILISATEUR='" + aCheque.getCodeutilisateur() + "'";

            Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());

            if (remises != null && remises.length > 0) {
                aCheque.setRemise(remises[0].getIdremise());
                aCheque.setRefremise(remises[0].getReference());
            } else {
                Remises aRemise = new Remises();
                aRemise.setIdremise(new BigDecimal(Utility.computeCompteur("IDREMISE", "REMISE")));
                aCheque.setRemise(aRemise.getIdremise());
                aRemise.setEscompte(new BigDecimal(imageIndex.getEscompte()));
                aRemise.setMontant("" + Long.parseLong(imageIndex.getMontantRemise()));
                aRemise.setDevise("XOF");
                aRemise.setNbOperation(new BigDecimal(imageIndex.getNbrOperRemise()));
                aRemise.setCompteRemettant(aCheque.getCompteremettant());
                aRemise.setDateSaisie(aCheque.getDatesaisie());
                aRemise.setReference(imageIndex.getReferenceExterne().trim());
                aRemise.setAgenceRemettant(aCheque.getAgenceremettant());
                aRemise.setNomClient(imageIndex.getNomBeneficiaire());
                aRemise.setNomUtilisateur(aCheque.getCodeutilisateur());
                aRemise.setMachinescan(aCheque.getMachinescan());
                aRemise.setEtablissement(aCheque.getEtablissement());
                aRemise.setAgenceDepot(aCheque.getAgenceremettant());

                aRemise.setEtat(new BigDecimal(etat));
                db.insertObjectAsRowByQuery(aRemise, "REMISES");

            }

//MAJ ou INSERTION
            sql = "SELECT * FROM CHEQUES WHERE BANQUE ='" + aCheque.getBanque()
                    + "' AND NUMEROCHEQUE ='" + aCheque.getNumerocheque()
                    + "' AND NUMEROCOMPTE ='" + aCheque.getNumerocompte()
                    + "' AND MONTANTCHEQUE ='" + aCheque.getMontantcheque() + "' AND ETAT IN (" + Utility.getParam("CETAOPESAI") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + "," + Utility.getParam("CETAOPEVAL2") + "," + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + "," + Utility.getParam("CETAOPEVALSURCAIENVSIB") + ")";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

            if (cheques != null && cheques.length > 0) {
                // aCheque.setEtat(new BigDecimal(Integer.parseInt(Utility.getParam("CETAOPERETREC"))));

                //sql = "UPDATE CHEQUES SET PATHIMAGE='" + imagePath + "',FICHIERIMAGE='" + imageName + "', ETAT=" + Utility.getParam("CETAOPERETREC") + " WHERE IDCHEQUE =" + cheques[0].getIdcheque();
                //db.executeUpdate(sql);
                // db.updateRowByObjectByQuery(aCheque, "CHEQUES", sql);
                System.out.println("Cheque en doublon :" + cheques[0].getBanque() + " " + cheques[0].getNumerocheque() + " " + cheques[0].getNumerocompte() + " " + cheques[0].getMontantcheque());
                logEvent("WARNING", "Cheque en doublon :" + cheques[0].getBanque() + " " + cheques[0].getNumerocheque() + " " + cheques[0].getNumerocompte() + " " + cheques[0].getMontantcheque());
                aCheque.setIdcheque(new BigDecimal(Utility.computeCompteur("IDCHEQUE", "CHEQUES")));
                db.insertObjectAsRowByQuery(aCheque, "CHEQUES_DOUBLONS");

            } else {
                aCheque.setIdcheque(new BigDecimal(Utility.computeCompteur("IDCHEQUE", "CHEQUES")));
                aCheque.setSequence(new BigDecimal(Utility.computeCompteur("SEQCORPORATE", aCheque.getEtablissement())));

                String manuelValidationRC = Utility.getParam("CORPORATE_MANUEL_VALIDATION_RC"); //                                        
                if (manuelValidationRC != null && manuelValidationRC.equals("1")) {
                    if (aCheque.getBanque().equalsIgnoreCase(CMPUtility.getCodeBanqueSica3())) {
                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAMANVALRC")));
                        db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAMANVALRC") + " WHERE IDREMISE=" + aCheque.getRemise());
                    }
                } else {
                    aCheque.setEtat(new BigDecimal(etat));
                }
                String manuelValidationRNC = Utility.getParam("CORPORATE_MANUEL_VALIDATION_RNC");
                if (manuelValidationRNC != null && manuelValidationRNC.equals("1")) {
                    if (aCheque.getBanque().equalsIgnoreCase(CMPUtility.getCodeBanqueSica3())) {
                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAMANVALRNC")));
                        db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAMANVALRNC") + " WHERE IDREMISE=" + aCheque.getRemise());
                    }
                } else {
                    aCheque.setEtat(new BigDecimal(etat));
                }
                System.out.println("aCheque ID " + aCheque.getIdcheque());
                System.out.println("aCheque REMISE" + aCheque.getRemise());
                System.out.println("aCheque REF REMISE" + aCheque.getRefremise());
                aCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
                db.insertObjectAsRowByQuery(aCheque, "CHEQUES");
            }

        } else {
            Effets aEffet = new Effets();
            aEffet.setType_Effet(imageIndex.getTypeOperation());
            aEffet.setNumeroeffet(imageIndex.getNumEffet());
            aEffet.setIban_Tire(imageIndex.getPfxIBANDeb());
            aEffet.setBanque(imageIndex.getIdBanTire());
            aEffet.setAgence(imageIndex.getIdAgeTire());
            aEffet.setNumerocompte_Tire(imageIndex.getNumCptTire());
            imageIndex.getCleRibDeb();
            aEffet.setBanqueremettant(imageIndex.getIdBanRem());
            aEffet.setAgenceremettant(imageIndex.getIdAgeRem());
            //aEffet.setRio(imageIndex.getRio().getRio());
            aEffet.setEtablissement(imageIndex.getEtablissement());
            aEffet.setDatetraitement(Utility.convertDateToString(imageIndex.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
            aEffet.setDatesaisie(Utility.convertDateToString(imageIndex.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
            imageIndex.getLongRecto();
            imageIndex.getLongVerso();
            aEffet.setMontant_Effet(String.valueOf(Long.parseLong(imageIndex.getMontant())));
            imageIndex.getCodeCmc7();
            aEffet.setPathimage(imagePath);
            aEffet.setFichierimage(imageName);
            //MAJ ou INSERTION

            String sql = "SELECT * FROM EFFETS WHERE BANQUE ='" + aEffet.getBanque()
                    + "' AND NUMEROCHEQUE ='" + aEffet.getNumeroeffet()
                    + "' AND NUMEROCOMPTE ='" + aEffet.getNumerocompte_Beneficiaire()
                    + "' AND MONTANTEFFET ='" + aEffet.getMontant_Effet() + "' AND ETAT =" + Utility.getParam("CETAOPERET")
                    + "";
            Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());

            if (effets != null && effets.length > 0) {
//                aEffet.setEtat(new BigDecimal(Integer.parseInt(Utility.getParam("CETAOPERETREC"))));
//
//                sql = " IDEFFET =" + effets[0].getIdeffet();
//
//                db.updateRowByObjectByQuery(aEffet, "EFFETS", sql);
            } else {

                aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL")));
                aEffet.setIdeffet(new BigDecimal(Utility.computeCompteur("IDEFFET", "EFFETS")));
                db.insertObjectAsRowByQuery(aEffet, "EFFETS");
            }

        }
    }

    private void readEntete(BufferedInputStream bis) throws IOException {
        if (bis.read(bentete) != -1) {

            sentete = new String(bentete);
            setCurrentLine(sentete);
            enteteRemise.setIdEntete(getChamp(4));
            enteteRemise.setIdRecepeteur(getChamp(5));
            enteteRemise.setRefRemise(getChamp(3));
            enteteRemise.setDatePresentation(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
            enteteRemise.setIdEmetteur(getChamp(5));
            enteteRemise.setDevise(getChamp(3));
            enteteRemise.setTypeRemise(getChamp(3));
            enteteRemise.setIdDestinataire(getChamp(5));
            enteteRemise.setCodeLieu(getChamp(2));
            enteteRemise.setNbTotOperVal(getChamp(4));

            bis.read(new byte[1]);

        }
    }

    private void readImageIndex(BufferedInputStream bis) throws IOException {
        if (bis.read(bindex) != -1) {
            sindex = new String(bindex);
            setCurrentLine(sindex);

            imageIndex.setTypeOperation(getChamp(3));
            imageIndex.setNumCheque(getChamp(7));
            imageIndex.setIdBanTire(getChamp(5));
            imageIndex.setIdAgeTire(getChamp(5));
            imageIndex.setNumCptTire(getChamp(12));
            imageIndex.setCleRibDeb(getChamp(2));
            imageIndex.setIdBanRem(getChamp(5));
            imageIndex.setIdAgeRem(getChamp(5));
            imageIndex.setEtablissement(getChamp(10));
            imageIndex.setNomBeneficiaire(getChamp(35));
            imageIndex.setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
            imageIndex.setLongRecto(getChamp(9));
            imageIndex.setLongVerso(getChamp(9));
            imageIndex.setMontant(getChamp(16));
            imageIndex.setCodeCmc7(getChamp(1));
            imageIndex.setCompteRemettant(getChamp(12));
            imageIndex.setEscompte(getChamp(1));
            imageIndex.setVilleRemettant(getChamp(2));
            imageIndex.setCodeUtilisateur(getChamp(10));
            imageIndex.setMachineScan(getChamp(15));
            imageIndex.setReferenceRemise(getChamp(10));
            imageIndex.setReferenceExterne(getChamp(25));
            imageIndex.setNbrOperRemise(getChamp(4));
            imageIndex.setMontantRemise(getChamp(16));
            imageIndex.setNumEffet(getChamp(10));
            imageIndex.setBlancs(getChamp(90));
            bis.read(new byte[2]);
        }
    }

    private void readImageRecto(BufferedInputStream bis, int taille) throws Exception {

        if (MAXIMGSIZE > taille) {

            byte[] image = new byte[taille];
            imagePath = imgAllerFolder + File.separator
                    + imageIndex.getEtablissement().trim() + File.separator
                    + imageIndex.getMachineScan().trim() + File.separator
                    + Utility.convertDateToString(enteteRemise.getDatePresentation(), "yyyyMMdd") + File.separator
                    + imageIndex.getIdBanRem() + File.separator
                    + imageIndex.getIdAgeRem() + File.separator
                    + imageIndex.getTypeOperation() + File.separator;

            imageName = imageIndex.getNumCptTire()
                    + imageIndex.getCleRibDeb()
                    + ((imageIndex.getTypeOperation().equals("035")) ? imageIndex.getNumCheque() : imageIndex.getNumEffet())
                    + imageIndex.getMontant();
            if (!new File(imagePath).exists()) {
                if (!new File(imagePath).mkdirs()) {
                    logEvent("ERREUR", "Impossible de créer " + imagePath);
                }
            }
            if (bis.read(image) != -1) {
                BufferedOutputStream bos = new BufferedOutputStream((new FileOutputStream(imagePath
                        + imageName
                        + "f.jpg")));
                bis.read(new byte[2]);
                bos.write(image);
                //System.out.print(image);

                bos.close();
            }

        }

    }

    private void readImageVerso(BufferedInputStream bis, int taille) throws Exception {
        if (MAXIMGSIZE > taille) {
            byte[] image = new byte[taille];

            if (bis.read(image) != -1) {
                File newVersoFile = new File(imagePath + imageName + "r.jpg");
                bis.read(new byte[2]);

                BufferedImage bimg = Utility.createImageFromBytes(image);
                String corporateOffline = Utility.getParam("CORPORATE_OFFLINE"); //                                        
                if (corporateOffline != null && corporateOffline.equals("1")) {
                } else {
                    Boolean mayTattoo = Boolean.valueOf(Utility.getParam("TATOO_PICTURE"));
                    String paramText = CMPUtility.isBanqueNationale(imageIndex.getIdBanRem()) ? "DATECOMPENS_NAT" : "DATECOMPENS_SRG";
                    if (mayTattoo) {
                        Utility.tattooPictureDB(bimg, newVersoFile, paramText);
                    }

                }
                ImageIO.write(bimg, "jpeg", newVersoFile);

            }
        }
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {

        String corporateOffline = Utility.getParam("CORPORATE_OFFLINE"); //                                        
        if (corporateOffline != null && corporateOffline.equals("1")) {
            setEtat(Utility.getParam("CETAOPEVAL"));
        } else {
            setEtat(Utility.getParam("CETAOPEVAL2"));
        }

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        setFile(aFile);
        BufferedInputStream is = openFile(aFile);

        readEntete(is);
        int cptImage = Integer.parseInt(enteteRemise.getNbTotOperVal());
        System.out.println("Entete : " + enteteRemise.toString());
        System.out.println("CptImage : " + cptImage);
        for (int i = 0; i < cptImage; i++) {
            System.out.println("Cpt : " + i);
            readImageIndex(is);
            System.out.println("ImageIndex :" + imageIndex.toString());

            readImageRecto(is, Integer.parseInt(imageIndex.getLongRecto()));
            readImageVerso(is, Integer.parseInt(imageIndex.getLongVerso()));

            insertImageIndexIntoDB(db);

        }

        logEvent("INFO", "Nombre de Chèques= " + cptImage);
        is.close();
        db.close();
        mailaWriter = new MailaWriter(aFile.getName());
        mailaWriter.execute();
        return aFile;
    }
}
