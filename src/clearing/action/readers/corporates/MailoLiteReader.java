/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.corporates;

import clearing.model.CMPUtility;
import clearing.model.EnteteRemise;
import clearing.model.ImageIndex;
import clearing.model.RIO;
import clearing.table.Cheques;
import clearing.table.Comptes;
import clearing.table.Effets;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class MailoLiteReader extends BinFileReader  {

    private byte[] bentete = new byte[65];
    private byte[] bindex = new byte[400];
    private String sentete = "";
    private String sindex = "";
    private EnteteRemise enteteRemise = new EnteteRemise();
    private ImageIndex imageIndex = new ImageIndex();
    private String mailoExtFolder = Utility.getParam("MAILO_EXT_FOLDER");
    private String imagePath;
    private String imageName;
    private static final int MAXIMGSIZE = 1000000;

    private void insertImageIndexIntoDB(DataBase db) throws SQLException {

        if (imageIndex.getTypeOperation().equals("030") || imageIndex.getTypeOperation().equals("035")) {
            Cheques aCheque = new Cheques();
            aCheque.setType_Cheque(imageIndex.getTypeOperation());
            aCheque.setNumerocheque(imageIndex.getNumCheque());

            aCheque.setBanque(imageIndex.getIdBanTire());
            aCheque.setAgence(imageIndex.getIdAgeTire());
            aCheque.setNumerocompte(imageIndex.getNumCptTire());
            imageIndex.getCleRibDeb();
            aCheque.setBanqueremettant(imageIndex.getIdBanRem());
            aCheque.setAgenceremettant(imageIndex.getIdAgeRem());
            aCheque.setRio(imageIndex.getRio().getRio());
            aCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            aCheque.setDatesaisie(Utility.convertDateToString(imageIndex.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
            //imageIndex.getLongRecto();
            //imageIndex.getLongVerso();
            aCheque.setMontantcheque(String.valueOf(Long.parseLong(imageIndex.getMontant())));
            //imageIndex.getCodeCmc7();
            aCheque.setPathimage(imagePath);
            aCheque.setFichierimage(imageName);
//MAJ ou INSERTION
            String sql = "SELECT * FROM CHEQUES WHERE BANQUE ='" + aCheque.getBanque() +
                    "' AND NUMEROCHEQUE ='" + aCheque.getNumerocheque() +
                    "' AND NUMEROCOMPTE ='" + aCheque.getNumerocompte() +
                    "' AND MONTANTCHEQUE ='" + aCheque.getMontantcheque() + "' AND ETAT =" + Utility.getParam("CETAOPERET") +
                    "";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

            if (cheques != null && cheques.length > 0) {
                // aCheque.setEtat(new BigDecimal(Integer.parseInt(Utility.getParam("CETAOPERETREC"))));

                sql = "UPDATE CHEQUES SET PATHIMAGE='" + imagePath + "',FICHIERIMAGE='" + imageName + "', ETAT=" + Utility.getParam("CETAOPERETREC") + " WHERE IDCHEQUE =" + cheques[0].getIdcheque();
                db.executeUpdate(sql);
            // db.updateRowByObjectByQuery(aCheque, "CHEQUES", sql);

            } else {
                aCheque.setIdcheque(new BigDecimal(Utility.computeCompteur("IDCHEQUE", "CHEQUES")));
                aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPERETIMA")));

                Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("SELECT * FROM COMPTES WHERE NUMERO='"+aCheque.getNumerocompte()+"'", new Comptes());
                if(comptes!=null && comptes.length>0){
                    aCheque.setEtablissement(comptes[0].getAdresse1());
                    db.insertObjectAsRowByQuery(aCheque, "CHEQUES");
                }

                
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
            aEffet.setRio(imageIndex.getRio().getRio());
            aEffet.setDatetraitement(Utility.convertDateToString(imageIndex.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
            aEffet.setDatesaisie(Utility.convertDateToString(imageIndex.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
            imageIndex.getLongRecto();
            imageIndex.getLongVerso();
            aEffet.setMontant_Effet(String.valueOf(Long.parseLong(imageIndex.getMontant())));
            imageIndex.getCodeCmc7();
            aEffet.setPathimage(imagePath);
            aEffet.setFichierimage(imageName);
            //MAJ ou INSERTION

            String sql = "SELECT * FROM EFFETS WHERE BANQUE ='" + aEffet.getBanque() +
                    "' AND NUMEROEFFET ='" + aEffet.getNumeroeffet() +
                    "' AND NUMEROCOMPTE_TIRE ='" + aEffet.getNumerocompte_Tire() +
                    "' AND MONTANT_EFFET ='" + aEffet.getMontant_Effet() + "' AND ETAT =" + Utility.getParam("CETAOPERET") +
                    "";
            Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());

            if (effets != null && effets.length > 0) {
                aEffet.setEtat(new BigDecimal(Integer.parseInt(Utility.getParam("CETAOPERETREC"))));

                sql = " IDEFFET =" + effets[0].getIdeffet();

                db.updateRowByObjectByQuery(aEffet, "EFFETS", sql);
            } else {

                aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPERETIMA")));
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
            imageIndex.setRio(new RIO(getChamp(35)));
            imageIndex.setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
            imageIndex.setLongRecto(getChamp(9));
            imageIndex.setLongVerso(getChamp(9));
            imageIndex.setMontant(getChamp(16));
            imageIndex.setCodeCmc7(getChamp(1));
            imageIndex.setBlancs(getChamp(278));
            bis.read(new byte[2]);
        }
    }

    private void readImageRecto(BufferedInputStream bis, int taille) throws Exception {

        if (MAXIMGSIZE > taille) {

            byte[] image = new byte[taille];
            imagePath = mailoExtFolder + File.separator +
                    Utility.convertDateToString(enteteRemise.getDatePresentation(), "yyyyMMdd") + File.separator +
                    imageIndex.getIdBanRem() + File.separator +
                    imageIndex.getIdAgeRem() + File.separator +
                    imageIndex.getTypeOperation() + File.separator;

            imageName = imageIndex.getNumCptTire() +
                    imageIndex.getCleRibDeb() +
                    ((imageIndex.getTypeOperation().equals("035")) ? imageIndex.getNumCheque() : imageIndex.getNumEffet()) +
                    imageIndex.getMontant();
            if (!new File(imagePath).exists()) {
                if (!new File(imagePath).mkdirs()) {
                    logEvent("ERREUR", "Impossible de créer " + imagePath);
                }
            }
            if (bis.read(image) != -1) {
                BufferedOutputStream bos = new BufferedOutputStream((new FileOutputStream(imagePath +
                        imageName +
                        "f.jpg")));
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
                BufferedOutputStream bos = new BufferedOutputStream((new FileOutputStream(imagePath +
                        imageName +
                        "r.jpg")));
                bis.read(new byte[2]);
                bos.write(image);
                //System.out.print(image);

                bos.close();
            }
        }
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire)  {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            setFile(aFile);
            BufferedInputStream is = openFile(aFile);
            readEntete(is);
            CMPUtility.insertRemcom(enteteRemise,Integer.parseInt(Utility.getParam("CETAOPERET")));
            int cptImage = Integer.parseInt(enteteRemise.getNbTotOperVal());
            System.out.println("Entete : " + enteteRemise.toString());
            System.out.println("CptImage : " + cptImage);
            for (int i = 0; i < cptImage; i++) {
                System.out.println("Cpt : " + i);
                readImageIndex(is);
                System.out.println("ImageIndex :" + imageIndex.toString());
                if(imageIndex.getLongRecto().matches("[0-9]+") && imageIndex.getLongVerso().matches("[0-9]+")){

                readImageRecto(is, Integer.parseInt(imageIndex.getLongRecto()));
                readImageVerso(is, Integer.parseInt(imageIndex.getLongVerso()));
                insertImageIndexIntoDB(db);
                }
                
            }
            is.close();
            db.close();

        } catch (Exception ex) {
            Logger.getLogger(MailoLiteReader.class.getName()).log(Level.INFO, null, ex);
            db.close();
        }
        return aFile;
    }

    public MailoLiteReader() {
         setCopyOriginalFile(true);
    }

    
}


    
