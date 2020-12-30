<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@page  import="org.patware.web.bean.MessageBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboBanqueBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboAgenceBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboEtatBean" contentType="text/html"%>
<%@page  import="java.net.URLEncoder" contentType="text/html"%>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Parametres Pour Recherche</title>
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
        <h2>Parametres pour Recherche</h2>
        <jsp:useBean id="comboBanquesBean" class="clearing.web.bean.ComboBanqueBean" />
        <jsp:useBean id="comboAgencesBean" class="clearing.web.bean.ComboAgenceBean" />
        <jsp:useBean id="comboEtatBean" class="clearing.web.bean.ComboEtatBean" />
        <jsp:useBean id="comboUtilisateurBean" class="clearing.web.bean.ComboUtilisateurBean" />
        <jsp:useBean id="comboMachineBean" class="clearing.web.bean.ComboMachineBean" />
        <%

            String requeteCheque = request.getParameter("requetecheques");
            String requeteVirement = request.getParameter("requetevirements");
            String requeteEffet = request.getParameter("requeteeffets");

            if (requeteCheque != null) {
                request.setAttribute("requetecheques", URLEncoder.encode(requeteCheque, "UTF-8"));
            }
            if (requeteVirement != null) {
                request.setAttribute("requetevirements", URLEncoder.encode(requeteVirement, "UTF-8"));
            }

            if (requeteEffet != null) {
                request.setAttribute("requeteeffets", URLEncoder.encode(requeteEffet, "UTF-8"));
            }


        %>
        <div align="center">
            <form  id="paramsearch" name="paramsearch"  onsubmit="return resolveSearch();">

                <table cellspacing="5" cellpadding="5">
                    <thead>

                    </thead>
                    <tbody>
                        <tr>

                            <td><input type="checkbox" name="num" value="ON" checked/>Numero</td>
                            <td><input type=text name="numero" /></td>
                            <td></td>
                            <td><input type="checkbox" name="mnt" value="ON" />Montant</td>
                            <td><input type=text name="montant" maxlength="12" /></td>
                            <td></td>
                            <td><input type="checkbox" name="rem" value="ON" />Remise</td>
                            <td><input type=text name="remise" maxlength="12" /></td>
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
                            <td></td>
                            <td><input type="checkbox" name="refrem" value="ON" />Reference Remise</td>
                            <td><input type=text name="refremise" maxlength="100" /></td>
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
                                      {label : 'Cheques', value : 'Cheques',selected:true},
                                      {label : 'Virements', value : 'Virements'},
                                      {label : 'Effets', value : 'Effets'}
                                      ]" />

                            </td>
                        </tr>

                    </tbody>
                </table>
                <hr>
                <table>
                    </tbody>
                    <tr>

                        <td></td>
                        <td><input type="checkbox" name="traitement" value="ON"  />Date Traitement</td>
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
                        <td><input type=submit value="Rechercher"/></td>
                        <td></td> 
                        <td><input type=reset value="Recommencer"/> </td>
                    </tr>

                    </tbody>
                </table>
                <input type=hidden name="action" value="search"/>
                <input type=hidden name="interval" value=""/>
                <input type=hidden name="requetecheques" value="${requestScope['requetecheques']}" />
                <input type=hidden name="vuecheques" value="${param['vuecheques']}" />
                <input type=hidden name="requeteeffets" value="${requestScope['requeteeffets']}" />
                <input type=hidden name="vueeffets" value="${param['vueeffets']}" />
                <input type=hidden name="requetevirements" value="${requestScope['requetevirements']}" />
                <input type=hidden name="vuevirements" value="${param['vuevirements']}" />

                <input type=hidden name="requete" value="dynamique" />
            </form>
        </div>
        <% MessageBean message = (MessageBean) request.getAttribute("message");
            if (message != null) {
                out.print(message.getMessage());
            }%>


    </body>
</html>

