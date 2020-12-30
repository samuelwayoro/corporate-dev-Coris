<%@page  import="java.net.URLEncoder" contentType="text/html"%>
<%@page contentType="text/html" import="org.patware.jdbc.DataBase,java.math.BigDecimal,clearing.table.Utilisateurs"%>
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


<link href="css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
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
        comboUtilisateurBean.setRequete("SELECT * FROM UTILISATEURS WHERE ADRESSE='" + user.getAdresse().trim() + "' AND TRIM(LOGIN) IN (SELECT DISTINCT TRIM(CODEUTILISATEUR) FROM VIREMENTS WHERE ETAT= " + Utility.getParam("CETAOPEVAL") + ")");
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String action = request.getParameter("choixUtilisateur");
        if (action != null && action.equalsIgnoreCase("true")) {%>

    <div align="center">
        <form name='choixUtilisateur'>
            Choisissez un utilisateur : <a:widget name="dojo.combobox" id="user" value="${comboUtilisateurBean.comboDatas}"/>
            <input type="button" class="btn btn-primary" name="startVal" value="Demarrer la Validation des Virements" onclick="startValidation();">
            <input type="hidden" name="utilisateur" value=""/>
            <input type="hidden" name="action" value="choixUtilisateur"/>
        </form>
    </div>
    <%} else {
        boolean finish = false;
        String sql = "SELECT * FROM VIREMENTS WHERE IDVIREMENT = (SELECT MIN(IDVIREMENT) FROM VIREMENTS WHERE ETAT=" + Utility.getParam("CETAOPEVAL")
                + " AND CODEUTILISATEUR LIKE '" + userSaisie.trim() + "'"
                + " AND (VALIDEUR='' OR VALIDEUR IS NULL OR VALIDEUR = '" + user.getLogin().trim() + "'))";
        Virements[] virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        if (virements != null && virements.length > 0) {

            BigDecimal curRemise = virements[0].getRemise();
            BigDecimal idVirement = virements[0].getIdvirement();

            request.setAttribute("curRemise", curRemise);
            request.setAttribute("idObjet", idVirement);

            sql = "SELECT * FROM VIREMENTS WHERE IDVIREMENT = (SELECT MIN(IDVIREMENT) FROM VIREMENTS WHERE ETAT=" + Utility.getParam("CETAOPEVAL")
                    + " AND CODEUTILISATEUR LIKE '" + userSaisie.trim() + "'"
                    + " AND (VALIDEUR='' OR VALIDEUR IS NULL OR VALIDEUR = '" + user.getLogin().trim() + "'))"
                    + " AND REMISE =" + virements[0].getRemise() + " FOR UPDATE";
            db.executeUpdate(sql);
            sql = "UPDATE VIREMENTS SET VALIDEUR='" + user.getLogin().trim() + "' WHERE REMISE =" + virements[0].getRemise();
            db.executeUpdate(sql);

            sql = "SELECT BANQUE,AGENCE,NUMEROCOMPTE_BENEFICIAIRE,NOM_BENEFICIAIRE,"
                    + "MONTANTVIREMENT,LIBELLE,ADRESSE_BENEFICIAIRE,DATETRAITEMENT,NUMEROCOMPTE_TIRE,NUMEROVIREMENT "
                    + "FROM VIREMENTS WHERE REMISE=" + virements[0].getRemise() + " AND  ETAT=" + Utility.getParam("CETAOPEVAL");
            request.setAttribute("filtre", URLEncoder.encode(sql, "UTF-8"));

        } else {
            out.println("Vous n'avez plus de virements à valider, merci");
            finish = true;
        }
        db.close();

    %>
    <%if (!finish) {%>

    <h2> Validation d'un Fichier de Virement </h2>
    <div align="center">

        <form  id="validationVirementFichier" name="validationVirementFichier" onsubmit="return (addHiddenVirement() && doAjaxSubmit(this, undefined, 'ControlServlet'));">
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
                        <td><input id="banqueCre" type="text" value="<%out.println(virements[0].getBanque());%>" disabled>
                        </td>
                        <td></td>
                        <td>Agence Crediteur</td>
                        <td><input id="agenceCre" type="text" value="<%out.println(virements[0].getAgence());%>" disabled></td>
                    </tr>

                    <tr>
                        <td>Montant Virement:</td>
                        <td><input id="montant" type=text name="montant" value="<%out.println(Utility.formatNumber(virements[0].getMontantvirement()));%>" maxlength="16" disabled /></td>
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
                        <td><textarea disabled id="libelleVir" name="libelleVirement" maxlength="70"><%out.println(virements[0].getLibelle());%> </textarea> </td>


                    </tr>

                </tbody>
            </table>

            <a:widget name="yahoo.tooltip" args="{context:['validationVirementFichier']}" value="Formulaire de Validation"  />

            <input type=hidden name="action" value="validationVirementFichier"/>


        </form>
        <form name="detailForm" id="detailForm">
            <div id="monitorPanelDIV">
                <input class="btn btn-primary" type='button' name='valider' value='Valider le Fichier' onclick="validerFichierVirement();"/>
                <input type='button' class="btn btn-danger" name='rejeter' value='Rejeter le Virement' onclick=" rejeterVirement();"/>
                </div>

                <input type="hidden" name="curRemise" value="${requestScope['curRemise']}"/>
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
    <DIV    >
        <a:widget name="yahoo.dataTable" id="detail" service="data.jsp?requete=${requestScope['filtre']}"/>
    </DIV>
    <%}
        }%>
</body> 

