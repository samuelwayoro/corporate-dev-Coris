<%@page  import="java.net.URLEncoder" contentType="text/html"%>
<%@page contentType="text/html" import="org.patware.jdbc.DataBase,java.math.BigDecimal,clearing.table.Utilisateurs"%>
<%@page pageEncoding="UTF-8" import="org.patware.xml.JDBCXmlReader,org.patware.web.bean.MessageBean,org.patware.utils.Utility"%>
<%@page import="org.patware.xml.JDBCXmlReader,clearing.table.Effets,clearing.table.Sequences,clearing.table.Cheques,clearing.table.Banques"%>
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
        comboUtilisateurBean.setRequete("SELECT * FROM UTILISATEURS WHERE ADRESSE='" + user.getAdresse().trim() + "' AND TRIM(LOGIN) IN (SELECT DISTINCT TRIM(CODEUTILISATEUR) FROM EFFETS WHERE ETAT= " + Utility.getParam("CETAOPEVAL") + ")");
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String action = request.getParameter("choixUtilisateur");
        if (action != null && action.equalsIgnoreCase("true")) {%>

    <div align="center">
        <form name='choixUtilisateur'>
            Choisissez un utilisateur : <a:widget name="dojo.combobox" id="user" value="${comboUtilisateurBean.comboDatas}"/>
            <input type="button" name="startVal" value="Demarrer la Validation des Effets" onclick="startValidation();">
            <input type="hidden" name="utilisateur" value=""/>
            <input type="hidden" name="action" value="choixUtilisateur"/>
        </form>
    </div>
    <%} else {
        boolean finish = false;
        String sql = "SELECT * FROM EFFETS WHERE IDEFFET = (SELECT MIN(IDEFFET) FROM EFFETS WHERE ETAT=" + Utility.getParam("CETAOPEVAL") +
                " AND CODEUTILISATEUR LIKE '" + userSaisie.trim() + "'" +
                " AND (VALIDEUR='' OR VALIDEUR IS NULL OR VALIDEUR = '" + user.getLogin().trim() + "')) FOR UPDATE";
        Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        if (effets != null && effets.length > 0) {

            BigDecimal curIdEffet = effets[0].getIdeffet();
          
            request.setAttribute("idObjet", curIdEffet);
            
           
            sql = "UPDATE EFFETS SET VALIDEUR='" + user.getLogin().trim() + "' WHERE IDEFFET=" + curIdEffet;
            db.executeUpdate(sql);

           

        } else {
            out.println("Vous n'avez plus d'effets à valider, merci");
            finish = true;
        }
        db.close();


    %>
    <%if (!finish) {%>

<h2> Validation d'un Effet </h2>
    <div align="center">

        <form  id="validationEffetClient" name="validationEffetClient" onsubmit="return ( addHiddenEffet() && doAjaxSubmit(this,undefined,'ControlServlet'));">
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
                            <td><input type="text" name="numero" value="<%out.println(effets[0].getNumerocompte_Beneficiaire());%>"  /></td>
                        <td></td>
                        <td>Numéro de Compte Tiré:</td>
                        <td><input type=text name="numeroCompteTire" value="<%out.println(effets[0].getNumerocompte_Tire());%>" /></td>
                    </tr>
                    <tr>
                            <td>Nom du Beneficiaire: </td>
                            <td><input type="text" name="nom" value="<%out.println(effets[0].getNom_Beneficiaire());%>"   disabled /></td>
                            <td></td>
                            <td>Agence du beneficiaire: </td>
                            <td><input type="text" name="agence" value="<%out.println(effets[0].getAgenceremettant());%>"   disabled/></td>
                        </tr>
                   <tr>
                            <td>Banque Débiteur:</td>
                             <td><input type="text" name="idbandeb" value="<%out.println(effets[0].getBanque());%>"   disabled/></td>
                            <td></td>
                            <td>Agence Débiteur:</td>
                            <td><input type="text" name="idagedeb" value="<%out.println(effets[0].getAgence());%>"   disabled/></td>
                        </tr>

                    <tr>
                        <td>Montant Effet:</td>
                        <td><input type=text name="montant" value="<%out.println(Utility.formatNumber(effets[0].getMontant_Effet()));%>" /></td>
                        <td><input type=text value="XOF" name="devise" size="3" disabled/></td>
                        <td>Montant Frais:</td>
                        <td><input type=text name="montantfrais" value="<%out.println(Utility.formatNumber(effets[0].getMontant_Frais()));%>" /></td>
                        <td><input type=text value="XOF" name="devise" size="3" disabled/></td>
                    </tr>
                    <tr>
                        <td>Nom du Tiré:</td>
                        <td><input type=text name="nomTire" value="<%out.println(effets[0].getNom_Tire());%>" /></td>
                        <td></td>
                        <td>Adresse du Tiré:</td>
                        <td><input type=text name="adresse" value="<%out.println(effets[0].getAdresse_Tire());%>" /></td>
                    </tr>

                       <tr>
                        <td>Numero Effet:</td>
                        <td><input type=text name="numeroEffet" value="<%out.println(effets[0].getNumeroeffet());%>" /></td>
                        <td></td>
                        <td>Libelle Effet:</td>
                        <td><textarea id="libelleEffet" name="libelleEffet" maxlength="70"><%out.println(effets[0].getIdentification_Tire());%> </textarea> </td>
                        

                    </tr>
                     <tr>
                            <td>Date Echeance:</td>
                            <td><input type=text name="dateecheance" value="<%out.println(effets[0].getDate_Echeance());%>"  /></td>
                            <td></td>
                            <td>Date Creation:</td>
                            <td><input type=text name="datecreation" value="<%out.println(effets[0].getDate_Creation());%>"  /></td>
                        </tr>
                   
                </tbody>
            </table>

            <a:widget name="yahoo.tooltip" args="{context:['validationEffetClient']}" value="Formulaire de Validation"  />

            <input type=hidden name="action" value="validationEffetClient"/>


        </form>
        <form name="detailForm" id="detailForm">
        <div id="monitorPanelDIV">
            <input type='button' name='valider' value='Valider' onclick=" validerEffet();"/>
            <input type='button' name='rejeter' value='Rejeter' onclick=" rejeterEffet();"/>
        </div>

        <input type="hidden" name="idObjet" value="${requestScope['idObjet']}"/>
        <input type="hidden" name="action" value="updateDataTable"/>
        <input type="hidden" name="message" value=""/>
        <input type="hidden" name="oldvalue" value=""/>
        <input type="hidden" name="newvalue" value=""/>
        <input type="hidden" name="primaryClause" value=""/>
        <input type="hidden" name="table" value="EFFETS"/>
        <input type="hidden" name="colonne" value="ETAT"/>
        <input type="hidden" name="remarques" value=""/>

    </form>
        
         </div>
   <%}}%>

</body> 
