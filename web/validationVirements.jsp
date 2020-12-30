<%@page  import="java.net.URLEncoder" contentType="text/html"%>
<%@page contentType="text/html" import="org.patware.jdbc.DataBase,java.math.BigDecimal,clearing.table.Utilisateurs,org.patware.utils.ResLoader"%>
<%@page pageEncoding="UTF-8" import="org.patware.xml.JDBCXmlReader,org.patware.web.bean.MessageBean,org.patware.utils.Utility"%>
<%@page import="org.patware.xml.JDBCXmlReader,clearing.table.Sequences,clearing.table.Virements,clearing.table.Sequences,clearing.table.Cheques,clearing.table.Banques"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<jsp:include page="checkaccess.jsp"/>
<style type="text/css">

      body {
          font-style: italic;
          font-weight: bold;
      }

</style>
<script type="text/javascript"  src="mootools-packed.js"></script>
<script type="text/javascript"  src="clearing.js"></script>
<script type="text/javascript"  src="cookies.js"></script>

<link href="travail_prive2.css" rel="stylesheet" type="text/css" />
<jsp:include page="checkaccess.jsp"/>
<jsp:useBean id="comboUtilisateurBean" class="clearing.web.bean.ComboUtilisateurBean" />
<body >
    <%
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
        String userSaisie = (String) session.getAttribute("utilisateurSaisie");
        if (userSaisie == null) {
            userSaisie = "%";
        }
        comboUtilisateurBean.setRequete("SELECT * FROM UTILISATEURS WHERE ADRESSE='" + user.getAdresse().trim() + "' AND " + ResLoader.getMessages("trimFunction")+"(LOGIN) IN (SELECT DISTINCT " + ResLoader.getMessages("trimFunction")+"(CODEUTILISATEUR) FROM VIREMENTS WHERE ETAT= " + Utility.getParam("CETAOPEVAL") + ")");
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String action = request.getParameter("choixUtilisateur");
        if (action != null && action.equalsIgnoreCase("true")) {%>

    <div align="center">
        <form name='choixUtilisateur'>
            Choisissez un utilisateur : <a:widget name="dojo.combobox" id="user" value="${comboUtilisateurBean.comboDatas}"/>
            <input type="button" name="startVal" value="Demarrer la Validation des Virements" onclick="startValidation();">
            <input type="hidden" name="utilisateur" value=""/>
            <input type="hidden" name="action" value="choixUtilisateur"/>
        </form>
    </div>
    <%} else {
        boolean finish = false;
        String sql = "SELECT * FROM VIREMENTS WHERE IDVIREMENT = (SELECT MIN(IDVIREMENT) FROM VIREMENTS WHERE ETAT=" + Utility.getParam("CETAOPEVAL") +
                " AND CODEUTILISATEUR LIKE '" + userSaisie.trim() + "'" +
                " AND (VALIDEUR='' OR VALIDEUR IS NULL OR VALIDEUR = '" + user.getLogin().trim() + "')) FOR UPDATE";
        Virements[] virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        if (virements != null && virements.length > 0) {

            BigDecimal curIdVirement = virements[0].getIdvirement();
          
            request.setAttribute("idObjet", curIdVirement);
            
           
            sql = "UPDATE VIREMENTS SET VALIDEUR='" + user.getLogin().trim() + "' WHERE IDVIREMENT=" + curIdVirement;
            db.executeUpdate(sql);

           

        } else {
            out.println("Vous n'avez plus de virements à valider, merci");
            finish = true;
        }
        db.close();


    %>
    <%if (!finish) {%>

<h2> Validation d'un Virement </h2>
    <div align="center">

        <form  id="validationVirementClient" name="validationVirementClient" onsubmit="return ( addHiddenVirement() && doAjaxSubmit(this,undefined,'ControlServlet'));">
            <table border="0" cellspacing="2" cellpadding="2">
                <thead>
                    <tr>
                        <th></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td><b>TYPE DE VIREMENT:</b></td>
                        <td><input type="text" value="<%out.println(virements[0].getType_Virement());%>" disabled/></td>
                    </tr>
                    <tr>

                        <td>Numéro de Compte Tiré: </td>
                            <td><input type="text" id="numeroTire" name="numero" value="<%out.println(virements[0].getNumerocompte_Tire());%>" disabled /></td>
                        <td></td>
                        <td>Numéro de Compte Beneficiaire:</td>
                        <td><input type=text  id ="numeroBen" name="numeroCompteBen" value="<%out.println(virements[0].getNumerocompte_Beneficiaire());%>" disabled/></td>
                    </tr>
                    <tr>
                            <td>Nom du Tiré: </td>
                            <td><input type="text" id ="nomTire" name="nom" value="<%out.println(virements[0].getNom_Tire());%>"  disabled /></td>
                            <td></td>
                            <td>Agence du Tiré: </td>
                            <td><input type="text" id="agenceTire" name="agence" value="<%out.println(virements[0].getAgenceremettant());%>"  maxlength="5"  disabled/></td>

                        </tr>
                    <tr>
                            <td>Banque Crediteur</td>
                            <td><input type="text" value="<%out.println(virements[0].getBanque());%>" disabled>
                            </td>
                            <td></td>
                            <td>Agence Crediteur</td>
                            <td><input type="text" value="<%out.println(virements[0].getAgence());%>" disabled></td>
                        </tr>

                    <tr>
                        <td>Montant Virement:</td>
                        <td><input type=text name="montant" value="<%out.println(Utility.formatNumber(virements[0].getMontantvirement()));%>" maxlength="16" disabled /></td>
                        <td><input type=text value="XOF" name="devise" size="3" disabled/></td>
                        <td></td>
                        <td></td>
                    </tr>
                    <tr>
                        <td>Nom du Beneficiaire:</td>
                        <td><input type=text id="nomBen" name="nomBeneficiaire" value="<%out.println(virements[0].getNom_Beneficiaire());%>" maxlength="25" disabled/></td>
                        <td></td>
                        <td>Adresse du Beneficiaire:</td>
                        <td><input type=text id="addressBen" name="adresse" value="<%out.println(virements[0].getAdresse_Beneficiaire());%>" maxlength="25" disabled/></td>
                    </tr>

                       <tr>
                        <td>Numero Virement:</td>
                        <td><input type=text id="numeroVir" name="numeroVirement" maxlength="10" value="<%out.println(virements[0].getNumerovirement());%>" onKeypress="return numeriqueValide(event);" disabled/></td>
                        <td></td>
                        <td>Libelle Virement:</td>
                        <td><textarea id="libelleVir" name="libelleVirement" maxlength="70"><%out.println(virements[0].getLibelle());%> </textarea> </td>
                        

                    </tr>
                   
                </tbody>
            </table>

            <a:widget name="yahoo.tooltip" args="{context:['validationVirementClient']}" value="Formulaire de Validation"  />

            <input type=hidden name="action" value="validationVirementClient"/>


        </form>
        <form name="detailForm" id="detailForm">
        <div id="monitorPanelDIV">
            <input type='button' name='valider' value='Valider' onclick=" validerVirement();"/>
            <input type='button' name='rejeter' value='Rejeter' onclick=" rejeterVirement();"/>
        </div>

        <input type="hidden" name="idObjet" value="${requestScope['idObjet']}"/>
        <input type="hidden" name="action" value="updateDataTable"/>
        <input type="hidden" name="message" value=""/>
        <input type="hidden" name="oldvalue" value=""/>
        <input type="hidden" name="newvalue" value=""/>
        <input type="hidden" name="primaryClause" value=""/>
        <input type="hidden" name="table" value="VIREMENTS"/>
        <input type="hidden" name="colonne" value="ETAT"/>
        <input type="hidden" name="remarques" value=""/>

    </form>
        
         </div>
   <%}}%>

</body> 
