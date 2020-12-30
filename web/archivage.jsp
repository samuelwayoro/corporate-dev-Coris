<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@page  import="org.patware.web.bean.MessageBean" contentType="text/html"%>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Archivage Base</title>
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
        
    <h2> Archivage Base  </h2>
    <div align="center">
        <form  id="archivageBase" name="archivageBase" onsubmit="if (confirm('Etes vous sur?')) {
                document['archivageBase'].archiveBtn.disabled = true;
                document.forms['archivageBase'].archiveBtn.disabled = true;
                return (doAjaxSubmit(this,undefined,'ControlServlet'));
            }">
            <table border="0" cellspacing="2" cellpadding="2">
                <thead>
                    <tr>
                        <th></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    
                    <tr>
                        <td> <input type=submit name="archiveBtn" value="Archiver la BD"/></td>
                        
                    </tr>
                </tbody>
            </table>
            
            <a:widget name="yahoo.tooltip" args="{context:['archivageBase']}" value="Archivage de la base"  />


            <input type=hidden name="action" value="archivage"/>
            
        </form>
         </div>
        <% MessageBean message = (MessageBean) request.getAttribute("message");
            if (message != null) {
                out.print(message.getMessage());
            }   
        %>
   
    
    </body>
</html>
