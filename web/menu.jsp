<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@ page import="java.util.*" %>
<%@ page import="org.patware.web.jmaki.*" %>


<%
     MenuModel menuModel = new MenuModel();
    
     
     out.println(menuModel.getJsonRepresentation());
   //  System.out.println("Menu = "+menuModel.getJsonRepresentation());
%> 