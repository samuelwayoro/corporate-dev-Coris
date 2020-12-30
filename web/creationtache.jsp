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
        <title>Creation d'une tache</title>
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
        
    <h2> Creation d'une tache </h2>
    <div align="center">
        <form  id="creationTacheForm" name="creationTacheForm" onsubmit=" return (addUrl() && doAjaxSubmit(this,undefined,'ControlServlet'));">
            <table border="0" cellspacing="2" cellpadding="2">
                <thead>
                    <tr>
                        <th></th>
                        <th></th>
                    </tr>
               
                </thead>
                <tbody>
                    <tr>
                        <td>Id. Tache:</td>
                        <td><input type=text name="idTache" /></td>
                        <td></td>
                         <td>Libelle:</td>
                        <td><input type=text name="libelle" /></td>
                    </tr>
                    
                    <tr>
                        <td>Type Tache:</td>
                        <td><a:widget name="dojo.combobox" id="type"
   value="[
       {label : 'MENU', value : 'MENU'},
       {label : 'LISTE SIMPLE', value : 'dataTable.jsp'},
       {label : 'LISTE AVEC IMAGE', value : 'imageobjet.jsp'},
       {label : 'PAGE SPECIFIQUE', value : 'PAGE'},
       {label : 'LISTE MAITRE-DETAIL', value : 'dataTableMasterDetail.jsp'},
       {label : 'ACTION', value : 'ControlServlet'}	           
    ]" publish="/type/taches"/></td>
                        <td></td>
                        <td>Poids:</td>
                        <td><input type=text name="poids" onkeypress="return numeriqueValide(event);"/></td>
                    </tr>
                    
                    <tr>
                        <td></td>
                        <td></td>
               
         
                    </tr>
                    
                    <tr>
                        <td> <input type=submit value="Creer "/></td>
                        <td><input type=reset value="Recommencer"/> </td>
                    </tr>
                </tbody>
            </table>
            
            <a:widget name="yahoo.tooltip" args="{context:['creationTacheForm']}" value="Formulaire de Cr&eacute;ation"  />


            <input type=hidden name="action" value="creationtache"/>
            <input type=hidden name="typetache" />
            <input type=hidden name="url" />
        </form>
         </div>
         <div id="requeteMasterDIV" name="requeteMasterDIV" style="display:none;" >
             <H2>Requete:</H2>  <jsp:include  page="paramselect.jsp" />
         </div>
         <div id="actionDIV" style="display:none;" >
             Action:<input type=text id="actionUrl" name="actionUrl" />
         </div>
         <div id="pageDIV" style="display:none;" >
             Page:<input type=text id="page" name="page" />
         </div>
        <% MessageBean message = (MessageBean) request.getAttribute("message");
            if (message != null) {
                out.print(message.getMessage());
            }%>


  





    </body>
</html>
