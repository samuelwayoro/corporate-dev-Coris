<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@ page import="javax.print.attribute.*" %>
<%@ page import="javax.print.DocFlavor"%>
<%@ page import="javax.print.attribute.standard.*" %>
<%@ page import="java.awt.print.PrinterJob" %>
<%@ page import="javax.print.PrintService"%>
<%@ page import="javax.print.PrintServiceLookup"%>
<%@ page import="java.awt.print.PrinterException" %>
<%@ page import="clearing.utils.PrintMultiCheques" %>
<%@ page import="org.patware.utils.Utility" %>




<%

        String paramPrint = (String) request.getParameter("Print");
        String requete = request.getParameter("requete");
        PrintMultiCheques printCheques = new PrintMultiCheques();
        int nbChq = printCheques.getChequesToPrint(requete);
        if (paramPrint == null) {

            out.println("Vous tentez d'imprimer " + nbChq + " pages.");
            out.print("En êtes-vous sûr?");

%>
<form name="PrintCheques" action="printMultiCheques.jsp" method="POST">
    <input type="hidden" value="oui" name="Print" />
    <input type="hidden" value="<%=requete%>" name="requete" />
    <input type="submit" value="OUI" name="Oui" />
</form>

<%
        } else {
            //String requete = URLDecoder.decode(request.getParameter("requete"),"UTF-8");
            PrinterJob printJob = PrinterJob.getPrinterJob();
            PrintService printService = printJob.getPrintService();

            if (printService == null) {

                PrintService pss[] = PrintServiceLookup.lookupPrintServices(null, null);
                if (pss != null) {
                    for (int i = 0; i < pss.length; ++i) {
                        System.out.println(pss[i]);
                        PrintService ps = pss[i];

                        PrintServiceAttributeSet psas = ps.getAttributes();
                        Attribute attributes[] = psas.toArray();

                        for (int j = 0; j < attributes.length; ++j) {
                            Attribute attribute = attributes[j];
                            System.out.println("  attribute: " + attribute.getName());
                            if (attribute instanceof PrinterName) {
                                System.out.println("    printer name: " + ((PrinterName) attribute).getValue());
                                String printerName = ((PrinterName) attribute).getValue();
                                if (printerName.equalsIgnoreCase(Utility.getParam("CMP_PRINTER"))) {
                                    printService = pss[i];
                                      System.out.println("printService :: "+printService.getName());
                                }
                            }
                        }
                        DocFlavor supportedFlavors[] = ps.getSupportedDocFlavors();
                        for (int j = 0; j < supportedFlavors.length; ++j) {
                            System.out.println("  flavor: " + supportedFlavors[j]);
                        }
                    }

                    printJob.setPrintService(printService);
                    out.println("Imprimante = " + printService.getName());
                    printJob.setJobName("Impression des chèques en lot");
                    printJob.setPageable(printCheques);


                    try {
                        printJob.print();
                    } catch (PrinterException ex) {
                        ex.printStackTrace();
                    }

                    out.println("Job d'impression envoyé vers l'imprimante:" + nbChq + " pages.");


                } else {

                out.println("Aucune imprimante n'est accessible");

                }



            } else {
                out.println("Imprimante = " + printService.getName());
                printJob.setJobName("Impression des chèques en lot");
                printJob.setPageable(printCheques);

                // if (printJob.printDialog()) {
                try {
                    // Effectue l'impression
                    printJob.print();
                } catch (PrinterException ex) {
                    ex.printStackTrace();
                }
                // }
                out.println("Job d'impression envoyé vers l'imprimante:" + nbChq + " pages.");
            }



        }





%> 
