<%@page  import="java.net.URLEncoder" contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        
        <style type="text/css">
        </style>
        
        <script type="text/javascript"  src="clearing.js"></script>
        <link href="travail_prive2.css" rel="stylesheet" type="text/css" />
    </head>
    <jsp:include page="checkaccess.jsp"/>
     
    <body>
        
        <h1>${param['libelle']}</h1>
        <%
         request.setAttribute("filtre", URLEncoder.encode(request.getParameter("filtre"), "UTF-8"));
         
%> 

<form name='cancelForm'>
                <input type="button" value="Arreter la migration" name="stopRapBtn" onclick="doAjaxSubmit(document['cancelForm'],new Array('action'),'ControlServlet');"/>
                
                <input type="hidden" name="action" value="stopMigration"/>
            </form>
        
       
    </body>
    
</html>
