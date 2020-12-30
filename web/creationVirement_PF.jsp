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
    <jsp:useBean id="comboAgencesBean" class="clearing.web.bean.ComboAgenceBean" />
    <jsp:useBean id="comboBanquesBean" class="clearing.web.bean.ComboBanqueBean" />
    <jsp:useBean id="comboTypeVirementBean" class="clearing.web.bean.ComboTypeVirementBean" />

    <h2> Creation d'un Virement </h2>
    <div align="center">
         <jsp:useBean id="comboCompteBean" scope="session" class="clearing.web.bean.ComboComptePFBean" />
        <form  id="creationVirementClient" name="creationVirementClient" onsubmit="return ( addHiddenVirement() && doAjaxSubmit(this,undefined,'ControlServlet'));">
            <table border="0" cellspacing="2" cellpadding="2">
                <thead>
                    <tr>
                        <th></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td><b>TYPE DE VIREMENT</b></td>
                        <td><a:widget name="dojo.combobox" id="typevirement"
                                              value="${comboTypeVirementBean.comboDatas}" publish="/choix/virement"/></td>
                    </tr>
                    <tr>

                        <td>Numéro de Compte Tiré: </td>
                            <td><input type="text" id="numeroTire" name="numero" value="" maxlength="12" onKeypress="return numeriqueValide(event);" 
                                       onblur="return recupereInfoComptePF(document.forms['creationVirementClient']);" /></td>
                        <td></td>
                        <td>Numéro de Compte Beneficiaire:</td>
                        <td><input type=text  id ="numeroBen" name="numeroCompteBen" maxlength="12" onKeypress="return numeriqueValide(event);"/></td>
                    </tr>
                    <tr>
                            <td>Nom du Tiré: </td>
                            <td><input type="text" id ="nomTire" name="nom" value=""  /></td>
                            <td></td>
                            <td>Agence du Tiré: </td>
                            <td><input type="text" id="agenceTire" name="agence" value=""  maxlength="5" onKeypress="return numeriqueValide(event);" disabled/></td>

                        </tr>
                    <tr>
                            <td>Banque Crediteur</td>
                            <td><a:widget name="dojo.combobox" id="idbancre"
                                              value="${comboBanquesBean.comboDatas2}" publish="/cl/getAgences"/>
                            </td>
                            <td></td>
                            <td>Agence Crediteur</td>
                            <td><a:widget name="dojo.combobox" id="idagecre"
                            value="" subscribe="/cl" /></td>
                        </tr>
                    
                    <tr>
                        <td>Montant Virement:</td>
                        <td><input type=text name="montant" maxlength="16" onKeypress="return numeriqueValide(event);" /></td>
                        <td><input type=text value="XOF" name="devise" size="3" disabled/></td>
                        <td></td>
                        <td></td>
                    </tr>
                    <tr>
                        <td>Nom du Beneficiaire:</td>
                        <td><input type=text id="nomBen" name="nomBeneficiaire" maxlength="25"/></td>
                        <td></td>
                        <td>Adresse du Beneficiaire:</td>
                        <td><input type=text id="addressBen" name="adresse" maxlength="25"/></td>
                    </tr>

                       <tr>
                        <td>Numero Virement:</td>
                        <td><input type=text id="numeroVir" name="numeroVirement" maxlength="10" /></td>
                        
                        <td></td>
                        <td>Libelle Virement:</td>
                        <td><textarea id="libelleVir" name="libelleVirement" maxlength="70"> </textarea> </td>

                    </tr>
                    <tr>
                         <td></td>
                        <td> <input type=submit value="Creer "/></td>
                           <td></td>
                         <td><input type='button' name='changeBenef' value='Changer Beneficiaire' onclick="changerBenef();"/></td>
                        <td><input type=reset value="Recommencer"/> </td>
                      
                        
                    </tr>
                </tbody>
            </table>
           
             <input type=hidden name="bancre" />
            <input type=hidden name="agecre" />
            <input type=hidden name="typevirement" />
            <input type=hidden name="action" value="creationVirementClient"/>
            <input type=hidden name="requete" value="${param['requete']}"/>
           <input type=hidden name="cle" value="${param['cle']}"/>
             <a:widget name="yahoo.tooltip" args="{context:['creationVirementClient']}" value="Formulaire de Cr&eacute;ation"  />
            
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
