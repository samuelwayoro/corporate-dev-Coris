<%@page  import="org.patware.web.bean.MessageBean" contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script type="text/javascript"  src="clearing.js"></script>
        <style type="text/css">
           
            
        </style>
         <link href="travail_prive2.css" rel="stylesheet" type="text/css" />
    </head>
     <jsp:include page="checkaccess.jsp"/>
    <body>
       
        <div id="monitorPanelDIV" >  
            <form name='monitorForm'>
                <input type="button" value="Demarrer WebMonitor" name="startBtn" onclick="document['monitorForm'].action.value ='startBtn';document['monitorForm'].startBtn.disabled=true;alert('Demande de démarrage pris en compte');doAjaxSubmit(document['monitorForm'],new Array('action'),'ControlServlet');"/>
                <input type="button" value="Arreter WebMonitor" name="stopBtn" onclick="document['monitorForm'].action.value ='stopBtn';doAjaxSubmit(document['monitorForm'],new Array('action'),'ControlServlet');document['monitorForm'].startBtn.disabled=false;"/>
                <input type="hidden" name="action" value="dynamique"/>
            </form>
            
        </div>
        
        <% MessageBean message = (MessageBean) request.getAttribute("message");
            if (message != null) {
                out.print(message.getMessage());
            }%>
        <div id="tableEventDIV">     
            <a:widget name="yahoo.dataTable" id="tableEvent" service="data.jsp?requete=${param['requete']}"  
                      args="rowSingleSelect=true" />
        </div> 
       
        
    </body>
</html>
