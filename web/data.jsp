<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="org.patware.web.jmaki.*" %>
<%@ page import="clearing.table.Utilisateurs" %>


<%
    // System.out.println("Requete = " + request.getParameter("requete"));
     System.out.println("data.jsp");
    //TableModel jmakiTable  = (TableModel) session.getAttribute("tableModel");
    TableModel jmakiTable = new TableModel();
    if (null != jmakiTable) {

        String only = request.getParameter("only");
        String objet = request.getParameter("objet");
        //String objetDetail = request.getParameter("objetDetail");
        String nomidobjet = request.getParameter("nomidobjet");
        //String nomidobjetDetail = request.getParameter("nomidobjetDetail");
        String dropdown = request.getParameter("dropdown");
        String radio = request.getParameter("radio");
        //String requete = URLDecoder.decode(request.getParameter("requete"),"UTF-8");
        String requete = request.getParameter("requete");
        jmakiTable.setOnly(only);
        jmakiTable.setQuery(requete);
        jmakiTable.setTable(objet);
        //jmakiTable.setTableDetail(objetDetail);
        jmakiTable.setPrimaryKey(nomidobjet);
        //jmakiTable.setPrimaryKeyDetail(nomidobjetDetail);
        jmakiTable.setDropdown(dropdown);
        jmakiTable.setRadio(radio);
        jmakiTable.setConnectedUser(((Utilisateurs) session.getAttribute("utilisateur")).getLogin());

        // boolean editable = new Boolean(URLDecoder.decode(request.getParameter("editable"),"UTF-8")).booleanValue();
        //  jmakiTable.setEditable(editable);
        //System.out.println("Requete decode = " + requete);
        String completeTable = jmakiTable.processSql(requete);
        if (only != null && only.equals("rows")) {
            out.println(jmakiTable.getGlobalRows());
        } else if (only != null && only.equals("cols")) {

            out.println(jmakiTable.getGlobalCols());
        } else {
            out.println(completeTable);
           
        }
    }
%> 
