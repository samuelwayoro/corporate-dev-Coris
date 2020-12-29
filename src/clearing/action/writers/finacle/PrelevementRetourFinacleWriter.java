/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.finacle;

import clearing.table.Prelevements;
import clearing.utils.web.HTTPRequestSender;
import clearing.utils.web.HTTPResponseParser;
import clearing.xml.finacle.Notification;
import clearing.xml.finacle.NotificationResponse;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class PrelevementRetourFinacleWriter extends FlatFileWriter {

    private String etatPopulation = Utility.getParam("CETAOPEALLICOM2");

    public String getEtatPopulation() {
        return etatPopulation;
    }

    public final void setEtatPopulation(String etatPopulation) {
        this.etatPopulation = etatPopulation;
    }

    public PrelevementRetourFinacleWriter() {
        setDescription("Arbitrage des prelevements retour vers le SIB");
    }

    public PrelevementRetourFinacleWriter(String etat) {
        super();
        setEtatPopulation(etat);

    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String sql = "SELECT * FROM PRELEVEMENTS WHERE ETAT=" + getEtatPopulation();

        Prelevements[] prelevements = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());

        StringWriter sw = new StringWriter();

        Notification notification;
        NotificationResponse notificationResponse;
        JAXBContext jaxbContext = JAXBContext.newInstance(Notification.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

        JAXBContext jaxbContextResp = JAXBContext.newInstance(NotificationResponse.class);
        Unmarshaller jaxbUnmarshaller = jaxbContextResp.createUnmarshaller();

        String input;

        String reponse = "";

        if (prelevements != null && prelevements.length > 0) {
            for (Prelevements pr : prelevements) {
                notification = new Notification(pr);
                notificationResponse = new NotificationResponse();
                sw.getBuffer().setLength(0);
                input = createSoapEnveloppe(jaxbMarshaller, notification, sw);
                HTTPResponseParser hTTPResponseParser = HTTPRequestSender.sendPost(Utility.getParam("URL_UBACLEARING"), input);
                if (hTTPResponseParser.getResponseCode() != 200) {
                    System.out.println("REPONSE: " + hTTPResponseParser.getResponseCode() + ":" + hTTPResponseParser.getResponseMessage());
                    continue;
                }

                if (!hTTPResponseParser.getBody().contains("<return> </return>")) {

                    reponse = hTTPResponseParser.getBody().substring(hTTPResponseParser.getBody().indexOf("<C24TRANRES>"), hTTPResponseParser.getBody().indexOf("</return>")).trim();

                }
                System.out.println("REPONSE: " + reponse);
                if (reponse != null) {
                    notificationResponse = (NotificationResponse) jaxbUnmarshaller.unmarshal(new StringReader(reponse));
                }

                if (notificationResponse.getActionCode().equals("000")) {
                    pr.setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
                    pr.setMotifrejet("000");

                } else {
                    pr.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM2")));
                    String motifRejet = Utility.getParamNameOfType(notificationResponse.getActionCode(), "CODE_REJET");
                    if(motifRejet!=null){
                    pr.setMotifrejet(motifRejet);
                    }
                    else{
                        logEvent("WARNING", "Aucune correspondance pour le code retour "+notificationResponse.getActionCode()+" - IDPRELEVEMENT " + pr.getIdprelevement() );
                        continue;
                    }
                    if (Utility.getParam("FINACTIVEFEE")!= null && Utility.getParam("FINACTIVEFEE").equalsIgnoreCase("1")) {
                        if (getEtatPopulation().equalsIgnoreCase(Utility.getParam("CETAOPEALLICOM2")) && !notificationResponse.getActionCode().equals("114")) {
                            
                            String solid = "6"+notification.getAccountnmbr().substring(0, 3);
                            Notification.Fee fee1 = new Notification.Fee();
                            fee1.setAccountnmbr(notification.getAccountnmbr());
                            fee1.setAccountnmbrCr(Utility.getParam("PRECPTCOMREJPREFIN")+solid+Utility.getParam("SUFCPTCOMREJPREFIN"));
                            fee1.setAmount(Utility.getParam("MNTCOMCREREJPREFIN"));
                            fee1.setId("1");
                            
                            Notification.Fee fee2 = new Notification.Fee();
                            fee2.setAccountnmbr(notification.getAccountnmbr());
                            fee2.setAccountnmbrCr(Utility.getParam("PRECPTTOBREJPREFIN")+solid+Utility.getParam("SUFCPTTOBREJPREFIN"));
                            fee2.setAmount(Utility.getParam("MNTTOBCREREJPREFIN"));
                            fee2.setId("2");

                            Notification.Fee feeTable[] = {fee1,fee2};
                            notification.setFee(feeTable);
                            
                            sw.getBuffer().setLength(0);
                            input = createSoapEnveloppe(jaxbMarshaller, notification, sw);
                            hTTPResponseParser = HTTPRequestSender.sendPost(Utility.getParam("URL_UBACLEARING"), input);
                            if (hTTPResponseParser.getResponseCode() != 200) {
                               // System.out.println("REPONSE: " + hTTPResponseParser.getResponseCode() + ":" + hTTPResponseParser.getResponseMessage());
                               logEvent("ERROR", "Erreur de connexion Ã  Finacle = "+ hTTPResponseParser.getResponseCode() + ":" + hTTPResponseParser.getResponseMessage());
                              
                                continue;
                            }

                        }
                    }

                }

                db.updateRowByObjectByQuery(pr, "PRELEVEMENTS", "IDPRELEVEMENT=" + pr.getIdprelevement());


            }
            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Prelevements= " + prelevements.length );
            logEvent("INFO", "Nombre de Prelevements= " + prelevements.length );
        }else{
             setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }
                

        db.close();
    }

    private String createSoapEnveloppe(Marshaller jaxbMarshaller, Notification notification, StringWriter sw) throws JAXBException {
        String data;
        String input;
        jaxbMarshaller.marshal(notification, sw);
        data = sw.toString();
        data = "<![CDATA[" + data + "]]>";
        input = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:fin=\"http://finaclews.org\">"
                + "<soapenv:Header/>"
                + "<soapenv:Body>"
                + "<fin:sendTransaction>"
                + "<arg0>"
                + data
                + "</arg0>"
                + "</fin:sendTransaction>"
                + "</soapenv:Body>"
                + "</soapenv:Envelope>";
        System.out.println("INPUT: " + data);
        return input;
    }
}
