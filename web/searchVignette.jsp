<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@page  import="org.patware.web.bean.MessageBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboBanqueBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboAgenceBean" contentType="text/html"%>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
   <jsp:include page="checkaccess.jsp"/>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Creation manuelle d'un Virement client</title>
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
   

    <h2> Recherche d'une Vignette </h2>
    <div align="center">
       
        <form  id="searchVignette" name="searchVignette" >
            <table border="0" cellspacing="2" cellpadding="2">
                <thead>
                    <tr>
                        <th></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                   
                    <tr>

                        <td>Numéro de Cheque: </td>
                            <td><input type="text" id="numeroCheque" name="numero" value="" maxlength="12" onKeypress="return numeriqueValide(event);" onblur="return recupereInfoVignette(document.forms['searchVignette']);" /></td>
                        <td></td>
                        <td>Numéro Endos:</td>
                        <td><input type=text  id ="numeroEndos" name="numeroEndos" maxlength="12" onKeypress="return numeriqueValide(event);" onblur="return recupereInfoVignette(document.forms['searchVignette']);"/></td>
                    </tr>
                   
                    <tr>
                        
                               <td><input type=reset value="Recommencer"/> </td>
                      
                        
                    </tr>
                </tbody>
            </table>
           
            
            <input type=hidden name="action" value="searchVignette"/>
            <input type=hidden name="requete" value="${param['requete']}"/>
           <input type=hidden name="cle" value="${param['cle']}"/>
           <input type=hidden name="cle1" value="${param['cle1']}"/>
             <a:widget name="yahoo.tooltip" args="{context:['searchVignette']}" value="Formulaire de Recherche"  />
            
        </form>
         </div>
         <br>
         <DIV id="imageDIV" >
        <a:widget name="yahoo.dataTable" id="detail" service="data.jsp?only=cols&requete=${param['requete']}"/>
        </DIV>
        <% MessageBean message = (MessageBean) request.getAttribute("message");
            if (message != null) {
                out.print(message.getMessage());
            }%>
   
    
    </body>
</html>
