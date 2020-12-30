<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@page  import="org.patware.web.bean.MessageBean" contentType="text/html"%>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
   <jsp:include page="checkaccess.jsp"/>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Creation manuelle d'un Macuti</title>
        <style type="text/css">
            body {
                
                font-weight: normal;
                font-size: 12px;
                color: #333333;
            }
            
        </style>
        <script type="text/javascript"  src="clearing.js"></script>
    </head>
     <jsp:include page="checkaccess.jsp"/>
    <body>
    
    
    <h2> Creation d'un lien Machine-Utilisateur </h2>
    <div align="center">
        <form  id="creationMacuti" name="creationMacuti" onsubmit="return ( doAjaxSubmit(this,undefined,'ControlServlet'));">
            <table border="0" cellspacing="2" cellpadding="2">
                <thead>
                    <tr>
                        <th></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>Machine:</td>
                        <td><input type=text name="machine" /></td>
                    </tr>
                    <tr>
                        <td>Utilisateur:</td>
                        <td><input type=text name="utilisateur"  /></td>
                        
                    </tr>
                    
                    <tr>
                        
                        <td> <input type=submit value="Creer "/></td>
                        <td><input type=reset value="Recommencer"/> </td>
                    </tr>
                </tbody>
            </table>
            
            <a:widget name="yahoo.tooltip" args="{context:['creationMacuti']}" value="Formulaire de Cr&eacute;ation"  />

            <input type=hidden name="action" value="creationMacuti"/>
            
        </form>
         </div>
        <% MessageBean message = (MessageBean) request.getAttribute("message");
            if (message != null) {
                out.print(message.getMessage());
            }%>
   
    
    </body>
</html>
