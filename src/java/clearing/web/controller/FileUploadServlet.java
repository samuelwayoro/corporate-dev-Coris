/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.web.controller;

import clearing.action.readers.corporates.PrelevementUploadReader;
import clearing.table.Comptes;
import clearing.table.Prelevements;
import clearing.table.Utilisateurs;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.patware.bean.table.Fichiers;
import org.patware.bean.table.Params;
import org.patware.bean.table.Repertoires;

import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * //
 *
 * @author DavyStephane
 */
public class FileUploadServlet extends HttpServlet {

//    private final static String bufferPath = "C:/uploads/UploadRepository";
//    private final static String destPath = "C:/uploads/UploadStore";
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO Auto-generated method stub  
        System.out.println("doGet");
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String montant = null;
        File fileTreated;
        int nbPrelevements = 0;
        Prelevements[] prelevements = null;
        Prelevements[] prelevementsAttribute;
        
        boolean isMultipart = ServletFileUpload.isMultipartContent(req);
        if (isMultipart) {
            try {
                //Connection ? la bd
                DataBase db = new DataBase(JDBCXmlReader.getDriver());
                db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
                String bufferPath = null;
                String destPath = null;
                
                String sql = "SELECT * FROM Params WHERE nom='UPLOADBUFFERPATH'";
                Params[] params = (Params[]) db.retrieveRowAsObject(sql, new Params());
                if (params != null && params.length > 0) {
                    bufferPath = params[0].getValeur().trim();
                }
                sql = "SELECT * FROM Params WHERE  nom='UPLOADDESTPATH'";
                params = (Params[]) db.retrieveRowAsObject(sql, new Params());
                if (params != null && params.length > 0) {
                    destPath = params[0].getValeur().trim();
                }




                // Create a factory for disk-based file items  
                DiskFileItemFactory factory = new DiskFileItemFactory();
                File repository = new File(bufferPath);
                Utility.createFolderIfItsnt(repository, null);
                Utility.createFolderIfItsnt(new File(destPath), null);
                factory.setRepository(repository); // Set the buffer directory.  
                factory.setSizeThreshold(1024 * 2); // Set the size of buffer.  

                ServletFileUpload upload = new ServletFileUpload(factory);

                // Create a progress listener  
                ProgressListener progressListener = new ProgressListener() {
                    private long megaBytes = -1;

                    /**
                     * pBytesRead: The total number of bytes, which have been
                     * read so far pContentLength: The total number of bytes,
                     * which are being read. May be -1, if this number is
                     * unknown. pItems : The number of the field, which is
                     * currently being read. (0 = no item so far, 1 = first item
                     * is being read, ...)
                     */
                    public void update(long pBytesRead, long pContentLength, int pItems) {
                        long mBytes = pBytesRead / 1000000;
                        // "if" statement is for the purpose of reduce the progress  
                        // listeners activity  
                        // For example, you might emit a message only, if the number of  
                        // megabytes has changed:  
                        if (megaBytes == mBytes) {
                            return;
                        }
                        megaBytes = mBytes;
                        System.out.println("We are currently reading item " + pItems);
                        if (pContentLength == -1) {
                            System.out.println("So far, " + pBytesRead + " bytes have been read.");
                        } else {
                            System.out.println("So far, " + pBytesRead + " of " + pContentLength + " bytes have been read.");
                        }
                    }
                };
                
                upload.setProgressListener(progressListener);
                Fichiers fichier = new Fichiers();
                
                List<FileItem> items = upload.parseRequest(req);
                HashMap<String, String> fieldsMap = getFieldsForm(items);
                
                Utilisateurs userInSession = getUserInSession(req);
                for (FileItem item : items) {
                    if (!item.isFormField()) {
                        processFormField(item);
                        File uploadedFile = processUploadedFile(item, userInSession.getAdresse().trim(), destPath);
                        PrelevementUploadReader readerPrelevement = new PrelevementUploadReader();
                        Repertoires repertoire = new Repertoires();
                        repertoire.setChemin(destPath);
                        repertoire.setExtension("txt");
                        repertoire.setTache("clearing.action.readers.corporates.PrelevementUploadReader");
                        repertoire.setPartenaire(userInSession.getAdresse().trim());
                        session.removeAttribute("resultat");
                        fileTreated = readerPrelevement.treatFile(uploadedFile, repertoire);
                        String resultat = readerPrelevement.getResultat();
                        if (resultat == null ) {
                            montant = Utility.formatNumber(readerPrelevement.getMontantTotal());
                            nbPrelevements = readerPrelevement.getNbPrelevements();
                            prelevements = readerPrelevement.getPrelevements();
                            session.setAttribute("prelevementsAttribute", prelevements);
                        }
                        System.out.println("resultat " + resultat);
                        System.out.println("montant " + montant);
                        System.out.println("fileTreated " + fileTreated.toString());



                        //upload du fichier etat 10
                        fichier.setDateReception(Utility.convertDateToString(new Date(), "yyyy/MM/dd HH:mm:ss"));
                        
                        fichier.setNomFichier(fileTreated.toString());
                        fichier.setIdFichier(new BigDecimal(Utility.computeCompteur("IDFICHIER", "FICHIERS")));
                        fichier.setUserUpload(userInSession.getLogin().trim());//user dans la session
                        fichier.setEtat(new BigDecimal(Utility.getParam("CETATFICUP")));//Param de mise en BD
                        fichier.setParam1(fieldsMap.get("param1"));
                        fichier.setParam2(fieldsMap.get("param2"));
                        db.insertObjectAsRowByQuery(fichier, "FICHIERS");
                        session.setAttribute("fileTreated", fichier);
                        session.setAttribute("message", "Il y a  " + nbPrelevements + " prelevements dans le fichier intégré pour un montant total de : " + montant + " XOF");
                        session.setAttribute("resultat", resultat);
                        
                        if (resultat != null) {
                            
                            if (fileTreated.exists()) {
                                fileTreated.renameTo(new File(fichier.getNomFichier() + "#ERREUR"));
                            }
                        }

//                        req.setAttribute("message", "Il y a  " + nbPrelevements + " prelevements dans le fichier integre pour un montant de : " + montant);
//                        req.getRequestDispatcher("/result.jsp").forward(req, resp);
                    }
                }
                db.close();
            } catch (FileUploadException e) {
                // TODO Auto-generated catch block  
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block  
                e.printStackTrace();
            }
            
        } else {
            String action = req.getParameter("action");
            Utilisateurs userInSession = getUserInSession(req);
            
            Fichiers f = (Fichiers) session.getAttribute("fileTreated");
            System.out.println("action value :" + action);
            
            if (action.equals("OUI")) {
                try {
                    //Fichier confirm�. ins�rer les prelevements en BD
                    //Connection ? la bd
                    DataBase db = new DataBase(JDBCXmlReader.getDriver());
                    db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
                    
                    System.out.println("Confirmation");
                    //Confirmation du fichier etat 30

                    prelevementsAttribute = (Prelevements[]) session.getAttribute("prelevementsAttribute");
                    String sql = "SELECT * FROM COMPTES WHERE NUMERO = '" + prelevementsAttribute[0].getNumerocompte_Beneficiaire() + "'";
                    Comptes comptes[] = (Comptes[]) db.retrieveRowAsObject(sql, new Comptes());
                    String codeClient = "";
                    if (comptes != null && comptes.length > 0) {
                        codeClient = comptes[0].getNumcptex();
                    }
                    for (int i = 0; i < prelevementsAttribute.length; i++) {
                        prelevementsAttribute[i].setRemise(f.getIdFichier());
                        prelevementsAttribute[i].setZoneinterbancaire_Beneficiaire(codeClient);
                        prelevementsAttribute[i].setDateFinRecyclage(f.getParam1());
                        prelevementsAttribute[i].setNumeroprelevement(Utility.bourrageGZero(Utility.computeCompteur("NUMPREL", "PRELEVEMENTS"), 10));
                        
                        
                        db.insertObjectAsRowByQuery(prelevementsAttribute[i], "PRELEVEMENTS");
                    }
                    
                    f.setEtat(new BigDecimal(Utility.getParam("CETATFICCON")));
                    db.updateRowByObjectByQuery(f, "FICHIERS", "IDFICHIER=" + f.getIdFichier());
                    File aFile = new File(f.getNomFichier());
                    if (aFile.exists()) {
                        aFile.renameTo(new File(f.getNomFichier() + "#ACCEPTE"));
                    }
                    
                    System.out.println("taille prelevements :" + prelevementsAttribute.length);
                    
                    db.close();
                } catch (Exception e) {
                    // TODO Auto-generated catch block  
                    e.printStackTrace();
                }
                session.setAttribute("resultat", "Fichier integre avec succes");
                req.getRequestDispatcher("/result.jsp").forward(req, resp);
                session.removeAttribute("resultat");
                
            } else {
                System.out.println("Pas de Confirmation");
                
                try {
                    //Connection ? la bd
                    DataBase db = new DataBase(JDBCXmlReader.getDriver());
                    db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
//                      vignette.setEtat(new BigDecimal(Utility.getParam("CETAVIGSTO")));
                    f.setEtat(new BigDecimal(Utility.getParam("CETATFICDEL")));
//                    db.insertObjectAsRowByQuery(f, "FICHIERS");
                    db.updateRowByObjectByQuery(f, "FICHIERS", "IDFICHIER=" + f.getIdFichier());
                    //suppresion du fichier etat 20
                    db.close();
                    File aFile = new File(f.getNomFichier());
                    if (aFile.exists()) {
                        aFile.renameTo(new File(f.getNomFichier() + "#ANNULE"));
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block  
                    e.printStackTrace();
                }
                session.removeAttribute("prelevementsAttribute");
                session.removeAttribute("fileTreated");
                session.removeAttribute("message");
                req.getRequestDispatcher("/uploadPrelevement.jsp").forward(req, resp);
            }
            session.removeAttribute("prelevementsAttribute");
            session.removeAttribute("fileTreated");
            session.removeAttribute("message");
        }

        // TODO Auto-generated method stub  

        
        
        
        System.out.println("doPost");
    }
    
    private Utilisateurs getUserInSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Utilisateurs utilisateur = (Utilisateurs) session.getAttribute("utilisateur");
        if (utilisateur == null) {
            utilisateur = new Utilisateurs();
            utilisateur.setLogin("userTest");
            
        }
        return utilisateur;
    }
    
    private void processFormField(FileItem item) {
        String name = item.getFieldName();
        String value = item.getString();
        System.out.println("name = " + name + ", value = " + value);
    }
    
    private HashMap<String, String> getFieldsForm(List<FileItem> fileItems) {
        HashMap<String, String> fieldsNames = new HashMap<String, String>();
        for (FileItem item : fileItems) {
            if (item.isFormField()) {
                String nameField = item.getFieldName();
                String value = item.getString();
                fieldsNames.put(nameField, value);
                System.out.println(nameField+"-"+value);
                
                
            }
        }
        return fieldsNames;
        
        
    }
    
    private File processUploadedFile(FileItem item, String prefix, String path) throws Exception {
        String fieldName = item.getFieldName();
        String fileName = item.getName();
        String contentType = item.getContentType();
        String dateTraitement = Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss") + "_" + prefix;
        
        boolean isInMemory = item.isInMemory();
        long sizeInBytes = item.getSize();
        
        System.out.println("fieldName = " + fieldName + ", fileName = " + fileName + ", contentType = " + contentType + ", isInMemory = " + isInMemory + ", sizeInBytes = " + sizeInBytes);
        System.out.println("prefix+\"_\"+fileName  :: " + prefix + "_" + fileName);
        File fileToSave = new File(path, dateTraitement + "_" + fileName);
        
        item.write(fileToSave);
        return fileToSave;
    }
}
