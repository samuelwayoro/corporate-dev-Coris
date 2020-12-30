<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@ page import="javax.print.attribute.HashPrintRequestAttributeSet" %>
<%@ page import="javax.print.attribute.standard.OrientationRequested" %>
<%@ page import="java.awt.print.PrinterJob" %>
<%@ page import="java.awt.print.PrinterException" %>
<%@ page import="clearing.utils.PrintCheques" %>



<%


   PrintCheques printCheques = new PrintCheques();
   //String requete = URLDecoder.decode(request.getParameter("requete"),"UTF-8");
   printCheques.getChequesToPrint(request.getParameter("requete"));
   PrinterJob printJob = PrinterJob.getPrinterJob();
   HashPrintRequestAttributeSet printRequestSet = new HashPrintRequestAttributeSet();
   printRequestSet.add(OrientationRequested.LANDSCAPE);
   printJob.setPrintable(printCheques);
   try {
    printJob.print();
  } catch (PrinterException e1) {
    e1.printStackTrace();
  }/*
   if (printJob.printDialog()){
         try {
            // Effectue l'impression
            printJob.print();
         } catch (PrinterException ex) {
            ex.printStackTrace();
         }
      }*/
   out.print("Job d'impression envoyé vers l'imprimante");
   


%> 
