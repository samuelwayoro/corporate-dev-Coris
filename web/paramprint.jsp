<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@page  import="org.patware.web.bean.MessageBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboBanqueBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboAgenceBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboEtatBean" contentType="text/html"%>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Parametres Pour Impression en Lot</title>
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
    <body onload="return chargeTous()">
        <h2>Parametres pour Impression en lot</h2>
        <jsp:useBean id="comboBanquesBean" class="clearing.web.bean.ComboBanqueBean" />
        <jsp:useBean id="comboAgencesBean" class="clearing.web.bean.ComboAgenceBean" />
        <jsp:useBean id="comboEtatBean" class="clearing.web.bean.ComboEtatBean" />
        <jsp:useBean id="comboUtilisateurBean" class="clearing.web.bean.ComboUtilisateurBean" />
        <jsp:useBean id="comboMachineBean" class="clearing.web.bean.ComboMachineBean" />
        <div align="center">
            <form  id="paramprint" name="paramprint" method="POST" action="printMultiCheques.jsp" onsubmit=" return (resolvePrint() && areFieldsNotEmpty(this));">
                
                <table cellspacing="5" cellpadding="5">
                    <thead>
                       
                    </thead>
                    <tbody>
                        <tr>
                            
                            <td></td>
                            <td><input type="checkbox" name="traitement" value="ON" checked="checked" />Date Traitement</td>
                             <td></td>
                                  <td></td>
                            <td><input type="checkbox" name="compensation" value="ON" />Date Compensation</td>
                            
                            </tr>
                        <tr>
                            <td>Date Debut</td>
                            <td><a:widget name="dojo.dropdowndatepicker" id="datedebut"/></td>
                            <td></td>
                            <td>Date Fin</td>
                            <td><a:widget name="dojo.dropdowndatepicker" id="datefin"/></td>
                        </tr>
                        <tr>
                            <td>Banque Crediteur</td>
                            <td><a:widget name="dojo.combobox" id="idbancre"
                                              value="${comboBanquesBean.comboDatas}"  />
                            </td>
                            <td></td>
                            <td>Agence Crediteur</td>
                            <td><a:widget name="dojo.combobox" id="idagecre"
                                          value="${comboAgencesBean.comboDatas}" /></td>
                        </tr>
                        <tr>
                            <td>Banque Debiteur</td>
                            <td><a:widget name="dojo.combobox" id="idbandeb"
                                          value="${comboBanquesBean.comboDatas}" /></td>
                            <td></td>
                            <td>Agence Debiteur</td>
                            <td><a:widget name="dojo.combobox" id="idagedeb"
                                              value="${comboAgencesBean.comboDatas}" />
                            </td>
                        </tr>
                        <tr>
                            <td>Etat de l'ordre</td>
                            <td><a:widget name="dojo.combobox" id="etat"
                                          value="${comboEtatBean.comboDatas}" /></td>
                            <td></td>
                            <td>Type d'ordre</td>
                            <td><a:widget name="dojo.combobox" id="typeordre"
                                              value="[
                                              {label : 'Cheques', value : 'Cheques',selected:true}
                                              ]" />
                                              
                            </td> 
                        </tr>

                    </tbody>
                </table>
                <hr>
                <table>
                    </tbody>
                        
                        <tr>

                           
                            <td><input type="checkbox" name="utilisateur" value="ON" />Utilisateur</td>
                            <td><a:widget name="dojo.combobox" id="user" value="${comboUtilisateurBean.comboDatas}"/></td>
                            <td><input type="checkbox" name="cptcre" value="ON" />Compte Crediteur</td>
                            <td><input type=text name="cptCrediteur" maxlength="12" /></td>
                            <td><input type="checkbox" name="cptdeb" value="ON" />Compte Debiteur</td>
                            <td><input type=text name="cptDebiteur" maxlength="12" /></td>
 
                        </tr>

                        <tr>
                          
                            
                            <td><input type="checkbox" name="machine" value="ON" />Machine</td>
                            <td><a:widget name="dojo.combobox" id="machinescan" value="${comboMachineBean.comboDatas}"/></td>
                            <td><input type="checkbox" name="montantMin" value="ON" />Montant Minimum</td>
                            <td><input type=text name="montantMinimum"  onKeypress="return numeriqueValide(event);"/></td>
                            <td><input type="checkbox" name="montantMax" value="ON" />Montant Maximum</td>
                            <td><input type=text name="montantMaximum"  onKeypress="return numeriqueValide(event);"/></td>
                            
                        </tr>
                       
                        <tr>
                        <tr>
                            <td></td>
                            <td><input type=submit value="Imprimer"/></td>
                            <td></td> 
                            <td><input type=reset value="Recommencer"/> </td>
                        </tr>
                         
                    </tbody>
                </table>
                <input type=hidden name="action" value="printbatch"/>
                 <input type=hidden name="interval" value=""/>
                
                <input type=hidden name="requete" value="dynamique" />
            </form>
        </div>
        <% MessageBean message = (MessageBean) request.getAttribute("message");
            if (message != null) {
                out.print(message.getMessage());
            }%>


    </body>
</html>

