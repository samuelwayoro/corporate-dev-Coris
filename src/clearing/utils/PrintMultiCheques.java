/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.utils;

/**
 *
 * @author Patrick Augou
 */
import clearing.model.CMPUtility;
import clearing.table.Cheques;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.util.Vector;
import javax.imageio.ImageIO;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

public class PrintMultiCheques implements Pageable, Printable {

    private Vector<Image> vImages;
    private PageFormat format = new PageFormat();
   
    public PrintMultiCheques() {
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

        if (this.format==null) this.format = pageFormat;

        if (pageIndex > vImages.size()) {
            return NO_SUCH_PAGE;
        }
        graphics.translate((int)pageFormat.getImageableX(), (int)pageFormat.getImageableY());
       
        if(vImages!=null && vImages.size()>0){
            Image image = vImages.elementAt(pageIndex);
            //graphics.drawImage(image, 0, y, image.getWidth(null), image.getHeight(null), null);
            graphics.drawImage(image, 0, 0, (int)pageFormat.getImageableWidth(), (int)pageFormat.getImageableHeight(), null);
            
     
         return PAGE_EXISTS;
        }
        
 return NO_SUCH_PAGE;

    }

    public int getChequesToPrint(String query) throws Exception {

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(query, new Cheques());
        db.close();
        vImages = new Vector<Image>();
        if (cheques != null && cheques.length > 0) {
            for (Cheques cheque : cheques) {
                File imageRectoFile = new File(cheque.getPathimage() + File.separator + cheque.getFichierimage() + "f.jpg");
                File imageVersoFile = new File(cheque.getPathimage() + File.separator + cheque.getFichierimage() + "r.jpg");
                BufferedImage fullRectoImage = ImageIO.read(imageRectoFile);
                BufferedImage fullVersoImage = ImageIO.read(imageVersoFile);
                if(fullRectoImage!= null && fullVersoImage!= null){
                BufferedImage result = new BufferedImage(fullRectoImage.getWidth(), fullRectoImage.getHeight() + fullVersoImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics g = result.getGraphics();
                g.drawImage(fullRectoImage, 0, 0, null);
                g.drawImage(fullVersoImage, 0, fullRectoImage.getHeight(), null);
                g.dispose();
                String[] text = new String[11];
                text[0] = "INFORMATIONS:";
                text[1] = "Montant = " + Utility.formatNumber(cheque.getMontantcheque()) + " "+ CMPUtility.getDevise();
                text[2] = "Etat = " + cheque.getEtat() +"-"+ Utility.getParamLabelOfType(""+cheque.getEtat(), "CODE_ETAT");
                text[3] = "Numero Chèque = " + cheque.getNumerocheque();
                text[4] = "Banque Remettant = " + cheque.getBanqueremettant() + "-" + CMPUtility.getLibelleBanque(cheque.getBanqueremettant());
                text[5] = "Agence Remettant = " + cheque.getAgenceremettant();
                text[6] = "Compte Débiteur = " + cheque.getNumerocompte();
                text[7] = "Beneficiaire = " + cheque.getNombeneficiaire();
                text[8] = "Date Compensation = " + cheque.getDatecompensation();
                text[9] = "ID Cheque = " + cheque.getIdcheque();
                text[10] = " ";
                if(cheque.getMotifrejet()!=null){
                    text[10] = "Motif Rejet = " + cheque.getMotifrejet()+'-'+ Utility.getParamLabel(cheque.getMotifrejet());
                }

                vImages.add(addTextToImage(result, text));
                 }
            }
        }
      return vImages.size();


    }

    public Image addTextToImage(BufferedImage i, String[] text) {

        final int VERTICLE_PADDING_PIXELS = 5;
        final int LEFT_MARGIN_PIXELS = 5;

        FontMetrics fm = i.createGraphics().getFontMetrics();

        int width = i.getWidth();
        int height = i.getHeight() + (text.length * (fm.getHeight() + VERTICLE_PADDING_PIXELS));

        for (String s : text) {
            width = Math.max(width, fm.stringWidth(s) + LEFT_MARGIN_PIXELS);
        }

        BufferedImage result = new BufferedImage(width, height,BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = result.createGraphics();

        g.drawImage(i, 0, 0, null);

        g.setColor(Color.BLACK);
        for (int x = 0; x < text.length; x++) {
            g.drawString(text[x], LEFT_MARGIN_PIXELS, i.getHeight() + (x + 1) * VERTICLE_PADDING_PIXELS + x * fm.getHeight());
        }
       g.dispose();
        return result;
    }

    public int getNumberOfPages() {
         return vImages.size();
    }

    public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
        return format; 
    }

    public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
        return this;
    }
}