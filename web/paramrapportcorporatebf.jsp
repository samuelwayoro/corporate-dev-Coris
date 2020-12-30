<%-- 
    Document   : paramrapportcorporatebf
    Created on : 29 déc. 2020, 15:24:08
    Author     : samuel
--%>

<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@page  import="org.patware.web.bean.MessageBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboBanqueBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboAgenceBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboEtatBean" contentType="text/html"%>
<%@page  import="org.patware.jdbc.DataBase,org.patware.xml.JDBCXmlReader,clearing.table.Utilisateurs"%>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Edition de rapport de chèques par type de chèques </title>
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
        <h2>Parametres des Rapports</h2>
        <jsp:useBean id="comboBanquesBean" class="clearing.web.bean.ComboBanqueBean" />
        <jsp:useBean id="comboAgencesBean" class="clearing.web.bean.ComboAgenceBean" />
        <jsp:useBean id="comboEtablissementBean" class="clearing.web.bean.ComboEtablissementBean" />
        <jsp:useBean id="comboEtatBean" class="clearing.web.bean.ComboEtatBean" />
        <jsp:useBean id="comboUtilisateurBean" class="clearing.web.bean.ComboUtilisateurBean" />
        <jsp:useBean id="comboMachineBean" class="clearing.web.bean.ComboMachineBean" />
        <%
        Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
        comboUtilisateurBean.setRequete("SELECT * FROM UTILISATEURS WHERE ADRESSE='" + user.getAdresse().trim() + "'");
        comboMachineBean.setRequete("select distinct machine from macuti where trim(utilisateur) in (select trim(login) from utilisateurs where adresse='" + user.getAdresse().trim() + "')");
        %>
        <div align="center">
            <form  id="paramrapport" name="paramrapport" method="POST" action="ControlServlet" onsubmit=" return (resolveWebLite() && areFieldsNotEmpty(this));">

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
                            <td>Etat de l'ordre</td>
                            <td><a:widget name="dojo.combobox" id="etat"
                                          value="${comboEtatBean.comboDatas}" /></td>
                            <td></td>
                            <td>Type d'ordre</td>
                            <td><a:widget name="dojo.combobox" id="typeordre"
                                              value="[
                                              {label : 'Cheques', value : 'Cheques', selected:true},
                                              {label : 'Prelevements', value : 'Prelevements'}
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
                        
                        <td><input type=text name="cptCrediteur" maxlength="12" /></td>
                        
                        <td><input type=text name="cptDebiteur" maxlength="12" /></td>

                    </tr>

                    <tr>


                        <td><input type="checkbox" name="machine" value="ON" />Machine</td>
                        <td><a:widget name="dojo.combobox" id="machinescan" value="${comboMachineBean.comboDatas}"/></td>
                        
                        <td><input type=text name="montantMinimum"  onKeypress="return numeriqueValide(event);"/></td>
                        
                        
                    </tr>
                    <tr>

                        <td><input type="radio" name="typerapport" value="application/vnd.ms-excel" />EXCEL</td>
                        <td><input type="radio" name="typerapport" value="application/rtf" />WORD</td>
                        <td><input type="radio" name="typerapport" value="application/pdf"  checked="checked"/>PDF</td>
                        <td><input type="radio" name="typerapport" value="text/html" />Aperçu HTML</td>
                        <td><input type="checkbox" name="avecDetail" value="ON" checked="checked"/>Avec Détails</td>
                        
                    </tr>
                    <tr>

                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        
                    </tr>
                    <tr>
                    <tr>
                        <td></td>
                        <td><input type=submit value="Afficher"/></td>
                        <td></td>
                        <td><input type=reset value="Recommencer"/> </td>
                    </tr>

                    </tbody>
                </table>
                <input type=hidden name="action" value="rapportWebLite"/>
                <input type=hidden name="nomrapport" value=""/>
                <input type=hidden name="interval" value=""/>

                <input type=hidden name="requete" value="dynamique" />
            </form>
        </div>



    </body>
</html>
