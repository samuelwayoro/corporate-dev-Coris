<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="org.patware.web.jmaki.*" %>


<%
           // System.out.println("Requete = " + request.getParameter("requete"));
            TableModel jmakiTable = new TableModel();
            String only = request.getParameter("only");
            String objet = request.getParameter("objet");
            String nomidobjet = request.getParameter("nomidobjet");
            String dropdown = request.getParameter("dropdown");
            String radio = request.getParameter("radio");
            //String requete = URLDecoder.decode(request.getParameter("requete"),"UTF-8");
            String requete = request.getParameter("requete");
            jmakiTable.setQuery(requete);
            jmakiTable.setTable(objet);;
            jmakiTable.setPrimaryKey(nomidobjet);
            jmakiTable.setDropdown(dropdown);
            jmakiTable.setRadio(radio);
            
           // boolean editable = new Boolean(URLDecoder.decode(request.getParameter("editable"),"UTF-8")).booleanValue();
          //  jmakiTable.setEditable(editable);
           
            //System.out.println("Requete decode = " + requete);
            String completeTable = jmakiTable.processSql(requete);
            if (only != null && only.equals("rows")) {
                out.println(jmakiTable.getGlobalRows());
            }else if (only != null && only.equals("cols")) { 
                
                out.println(jmakiTable.getGlobalCols());
            }else {
                out.println(completeTable);
            }

%> 
