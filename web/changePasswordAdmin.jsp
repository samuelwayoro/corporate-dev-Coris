<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="clearing.table.Utilisateurs" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
   <jsp:include page="checkaccess.jsp"/>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
            <script type="text/javascript"  src="clearing.js"></script>
        <title>JSP Page</title>
        <style type="text/css">
            body {
                
                font-weight: normal;
                font-size: 12px;
                color: #999999;
            }
            
        </style>
    </head>
     <jsp:include page="checkaccess.jsp"/>
    <body>
      <h2> Modification de mot de passe </h2>
    <div align="center">
        <form  id="modification" name="modification" onsubmit="return (testPass(this) && doAjaxSubmit(this,undefined,'ControlServlet'));">
            <table border="0" cellspacing="2" cellpadding="2">
                <thead>
                    <tr>
                        <th></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    
                    <tr>
                        <td>Login:</td>
                        <td><input type=text name="login"  /></td>
                    </tr>
                    
                    <tr>
                        <td>Nouveau Mot de Passe:</td>
                        <td><input type=text name="password1" /></td>
                    </tr>
                    <tr>
                        <td>Nouveau Mot de Passe:</td>
                        <td><input type=text name="password2" /></td>
                    </tr>
                    <tr>
                        <td> <input type=submit value="Modifier"/></td>
                        
                    </tr>
                </tbody>
            </table>
           <a:widget name="yahoo.tooltip" args="{context:['modification']}" value="Modification de Mot de Passe"  />


            <input type=hidden name="action" value="changePasswordAdmin"/>
            
        </form>
         </div>
 
    
    
    
    
    </body>
</html>
