<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@page  import="org.patware.web.bean.MessageBean" contentType="text/html"%>
<%@page import="clearing.web.bean.ComboTacheBean" contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
   <jsp:include page="checkaccess.jsp"/>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Creation d'une ligne de menu</title>
        <style type="text/css">
            body {
                
                font-weight: normal;
                font-size: 12px;
                color: #999999;
            }
            
        </style>
        <script type="text/javascript"  src="clearing.js"></script>
    </head>
     <jsp:include page="checkaccess.jsp"/>
    <body>
      <jsp:useBean id="comboTacheBean" class="clearing.web.bean.ComboTacheBean" />  
    <h2> Creation d'une ligne de menu </h2>
    <div align="center">
        <form  id="creationMenuForm" name="creationMenuForm" onsubmit=" return (addHidden() && doAjaxSubmit(this,undefined,'ControlServlet'));">
            <table border="0" cellspacing="2" cellpadding="2">
                <thead>
                    <tr>
                        <th></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>Tache Parent:</td>
     <td><a:widget name="dojo.combobox" id="tacheParent" value="${comboTacheBean.tacheMenu}" /></td>
                    </tr>
                    <tr>
                        <td>Tache Enfant:</td>
                        <td><a:widget name="dojo.combobox" id="tacheEnfant" value="${comboTacheBean.comboDatas}" /></td>
                    </tr>
                   
                    <tr>
                        <td> <input type=submit value="Creer "/></td>
                        <td><input type=reset value="Recommencer"/> </td>
                    </tr>
                </tbody>
            </table>
            
            <a:widget name="yahoo.tooltip" args="{context:['creationMenuForm']}" value="Formulaire de Cr&eacute;ation"  />
            <input type=hidden name="tacheEnfant" />
            <input type=hidden name="tacheParent" />
            <input type=hidden name="action" value="creationMenu"/>
            
        </form>
         </div>
        <% MessageBean message = (MessageBean) request.getAttribute("message");
            if (message != null) {
                out.print(message.getMessage());
            }%>
   
    
    </body>
</html>
