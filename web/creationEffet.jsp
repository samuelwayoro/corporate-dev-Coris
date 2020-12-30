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
        <title>Creation manuelle d'un Effet client</title>
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
    <jsp:useBean id="comboAgencesBean" class="clearing.web.bean.ComboAgenceBean" />
    <jsp:useBean id="comboBanquesBean" class="clearing.web.bean.ComboBanqueBean" />
    
    <h2> Creation d'un Effet Client </h2>
    <div align="center">
         <jsp:useBean id="comboCompteBean" scope="session" class="clearing.web.bean.ComboCompteBean" />
        <form  id="creationEffetClient" name="creationEffetClient" onsubmit="return ( addHiddenEffet() && doAjaxSubmit(this,undefined,'ControlServlet'));">
            <table border="0" cellspacing="2" cellpadding="2">
                <thead>
                    <tr>
                        <th></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>Numéro de Compte Beneficiaire:</td>
                            <td><input type="text" name="numero" value="" maxlength="16" onKeypress="return numeriqueValide(event);" onblur="return recupereInfoCompte(document.forms['creationEffetClient']);" /></td>
                        <td></td>
                        <td>Numéro de Compte Tiré:</td>
                        <td><input type=text name="numeroCompteTire" maxlength="12" onKeypress="return numeriqueValide(event);"/></td>
                    </tr>
                    <tr>
                            <td>Nom du Beneficiaire: </td>
                            <td><input type="text" name="nom" value=""  disabled /></td>
                            <td></td>
                            <td>Agence du beneficiaire: </td>
                            <td><input type="text" name="agence" value=""  disabled/></td>
                        </tr>
                   <tr>
                            <td>Banque Débiteur:</td>
                            <td><a:widget name="dojo.combobox" id="idbandeb"
                                              value="${comboBanquesBean.comboDatas2}" publish="/cl/getAgences"/>
                            </td>
                            <td></td>
                            <td>Agence Débiteur:</td>
                            <td><a:widget name="dojo.combobox" id="idagedeb"
                            value="" subscribe="/cl" /></td>
                        </tr>
                    
                    <tr>
                        <td>Montant Effet:</td>
                        <td><input type=text name="montant" onKeypress="return numeriqueValide(event);" /></td>
                        <td><input type=text value="XOF" name="devise" size="3" disabled/></td>                      
                        <td>Montant Frais:</td>
                        <td><input type=text name="montantfrais" onKeypress="return numeriqueValide(event);" /></td>
                        <td><input type=text value="XOF" name="devise" size="3" disabled/></td>
                    </tr>
                     <tr>
                        
                        <td></td>
                        <td></td>
                    </tr>
                    <tr>
                        <td>Nom du Tiré:</td>
                        <td><input type=text name="nomTire" maxlength="25"/></td>
                        <td></td>
                        <td>Adresse du Tiré:</td>
                        <td><input type=text name="adresse" maxlength="25"/></td>
                    </tr>

                       <tr>
                        <td>Numero Effet:</td>
                        <td><input type=text name="numeroEffet" maxlength="10" onKeypress="return numeriqueValide(event);"/></td>
                        <td></td>
                        <td>Libelle Effet:</td>
                        <td><textarea id="libelleEffet" name="libelleEffet" maxlength="70"> </textarea> </td>
                        

                    </tr>
                     <tr>
                            <td>Date Echeance</td>
                            <td><a:widget name="dojo.dropdowndatepicker" id="dateecheance"/></td>
                            <td></td>
                            <td>Date Creation</td>
                            <td><a:widget name="dojo.dropdowndatepicker" id="datecreation"/></td>
                        </tr>
                    <tr>
                         <td></td>
                        <td> <input type=submit value="Creer "/></td>
                        <td><input type=reset value="Recommencer"/> </td>
                         <td></td>
                    </tr>
                </tbody>
            </table>
            
            <a:widget name="yahoo.tooltip" args="{context:['creationEffetClient']}" value="Formulaire de Cr&eacute;ation"  />
            
            <input type=hidden name="dateecheance" />
            <input type=hidden name="datecreation" />
            <input type=hidden name="bandeb" />
            <input type=hidden name="agedeb" />
            <input type=hidden name="action" value="creationEffetClient"/>
            
        </form>
         </div>
        <% MessageBean message = (MessageBean) request.getAttribute("message");
            if (message != null) {
                out.print(message.getMessage());
            }%>
   
    
    </body>
</html>
