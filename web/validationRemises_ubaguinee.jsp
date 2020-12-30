<%@page  import="java.net.URLEncoder" contentType="text/html"%>
<%@page contentType="text/html" import="org.patware.jdbc.DataBase,java.util.Date,java.math.BigDecimal,clearing.table.Utilisateurs,org.patware.utils.ResLoader"%>
<%@page pageEncoding="UTF-8" import="org.patware.xml.JDBCXmlReader,org.patware.web.bean.MessageBean,org.patware.utils.Utility"%>
<%@page import="org.patware.xml.JDBCXmlReader,clearing.table.Remises,clearing.table.Sequences,clearing.table.Cheques,clearing.table.Banques"%>
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
<body onload="affiche_master();" >
    <%
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
        String userSaisie = (String) session.getAttribute("utilisateurSaisie");
        if (userSaisie == null) {
            userSaisie = "%";
        }
        comboUtilisateurBean.setRequete("SELECT * FROM UTILISATEURS WHERE ADRESSE='" + user.getAdresse().trim() + "'"
                + "  AND TRIM(LOGIN) IN (SELECT DISTINCT TRIM(NOMUTILISATEUR) FROM REMISES WHERE ETAT= " + Utility.getParam("CETAOPEVAL") + ")");
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String action = request.getParameter("choixUtilisateur");
        if (action != null && action.equalsIgnoreCase("true")) {%>

    <div align="center">
        <form name='choixUtilisateur'>
            Choisissez un utilisateur : <a:widget name="dojo.combobox" id="user" value="${comboUtilisateurBean.comboDatas}"/>
            <input type="button" name="startVal" value="Demarrer la Validation des Remises" onclick="startValidation();">
            <input type="hidden" name="utilisateur" value=""/>
            <input type="hidden" name="action" value="choixUtilisateur"/>
        </form>
        <br>
        <br>
            <br>
    <%

        out.println("\nVoulez-vous imprimer votre rapport quotidien?");

        String dateTraitement = Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyy/MM/dd");

        String interval = "Agence de Saisie:" + user.getAdresse().trim() + "| Date de Traitement du " + dateTraitement;
        out.println(interval);
        String query = "select C1.IDCHEQUE,C1.REMISE,C1.REFREMISE,C1.DEVISE,C1.NUMEROCHEQUE,C1.NUMEROCOMPTE,C1.NOMBENEFICIAIRE,C1.ETAT,C1.AGENCE,C1.COMPTEREMETTANT,C1.AGENCEREMETTANT,C1.DATETRAITEMENT,C1.DATECOMPENSATION,"
                + "C1.banqueremettant ,B1.libellebanque as LIBELLEBANQUEREMETTANT,C2.banque ,B2.libellebanque as LIBELLEBANQUE,C1.montantcheque ,C1.MOTIFREJET "
                + "from cheques C1,cheques C2, Banques B1,Banques B2,Remises R "
                + "where C1.IDCHEQUE=C2.IDCHEQUE  AND C1.banqueremettant=B1.codebanque  and C2.banque=B2.codebanque "
                + "and C1.etat in (35,40,130,900)  "
                + "and C1.datetraitement ='" + dateTraitement + "' and C1.remise=R.idremise "
                + "and R.agenceDepot like '" + user.getAdresse().trim() + "' ";


    %>
    <form name="printReport"  action="ControlServlet" method="POST" onsubmit="return resolveQuotidien();">
        <table>
            
            <tr>
                <td><input type="radio" id="orderbybanque" name="nomrapport" value="Cheques" checked="checked"/>Regroupé par Banque</td>
                <td><input type="radio" id="orderbyremise" name="nomrapport" value="Cheques_clients_remises"  />Regroupé par Remise</td>
            </tr>
            <tr>
                <td><input type="radio" name="typerapport" value="application/vnd.ms-excel" />EXCEL</td>
                <td><input type="radio" name="typerapport" value="application/rtf" />WORD</td>
                <td><input type="radio" name="typerapport" value="application/pdf"  checked="checked"/>PDF</td>
                <td><input type="radio" name="typerapport" value="text/html" />Aperçu HTML</td>
            </tr>
<tr>
                <td><input type="submit" value="Imprimer" name="oui" /></td>
                <td><input type="checkbox" name="avecDetail" value="ON" checked="checked"/>Avec Détails</td>

            </tr>
        </table>
        <input type='hidden' name='action' value='rapport' />
        <input type='hidden' name='interval' value='<%=interval%>'/>
       <input type='hidden' name='requete' value="dynamique" />
        <input type='hidden' name='query' value="<%=query%>"/>
     
    </form>

</div>
    <%    } else {
        boolean finish = false;
        String sql = "SELECT * FROM REMISES WHERE IDREMISE = (SELECT MIN(IDREMISE) FROM REMISES WHERE "
                + "  ETAT=" + Utility.getParam("CETAOPEVAL")
                + " AND AGENCEDEPOT='" + user.getAdresse().trim() + "'"
                + " AND NOMUTILISATEUR LIKE '" + userSaisie.trim() + "'"
                + " AND (VALIDEUR='' OR VALIDEUR IS NULL OR VALIDEUR = '" + user.getLogin().trim() + "')) FOR UPDATE";
        Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
        if (remises != null && remises.length > 0) {

            BigDecimal curIdRemise = remises[0].getIdremise();
            String fPicture = "";
            String rPicture = "";
            String fPictureCheque = "";
            String rPictureCheque = "";
            request.setAttribute("idObjet", curIdRemise);
            if (remises[0].getPathimage() != null) {
                //   fPicture = (remises[0].getPathimage() == null || remises[0].getPathimage().isEmpty()) ? " " : remises[0].getPathimage().substring(3) + "\\" + remises[0].getFichierimage() + "f.jpg";
                //  rPicture = (remises[0].getPathimage() == null || remises[0].getPathimage().isEmpty()) ? " " : remises[0].getPathimage().substring(3) + "\\" + remises[0].getFichierimage() + "r.jpg";

            }
            request.setAttribute("fPicture", fPicture.replace("\\", "/"));
            request.setAttribute("rPicture", rPicture.replace("\\", "/"));
            request.setAttribute("montantRemise", remises[0].getMontant());
            request.setAttribute("compteRemettant", remises[0].getCompteRemettant());
            sql = "UPDATE REMISES SET VALIDEUR='" + user.getLogin().trim() + "' WHERE IDREMISE=" + curIdRemise;
            db.executeUpdate(sql);
            sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND REMISE=" + curIdRemise;
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                if (fPictureCheque != null && !fPictureCheque.isEmpty()) {
                    fPictureCheque = cheques[0].getPathimage().substring(3) + "\\" + cheques[0].getFichierimage() + "f.jpg"; //(cheques[0].getPathimage() == null || cheques[0].getPathimage().isEmpty()) ? " " : 
                }
                if (rPictureCheque != null && !rPictureCheque.isEmpty()) {
                    rPictureCheque = cheques[0].getPathimage().substring(3) + "\\" + cheques[0].getFichierimage() + "r.jpg"; //(cheques[0].getPathimage() == null || cheques[0].getPathimage().isEmpty()) ? " " :
                }

            }
            request.setAttribute("fPictureCheque", fPictureCheque.replace("\\", "/"));
            request.setAttribute("rPictureCheque", rPictureCheque.replace("\\", "/"));

            sql = "SELECT IDREMISE,COMPTEREMETTANT as \"COMPTE REMETTANT\",NOMCLIENT,MONTANT,NBOPERATION,DATESAISIE,AGENCEREMETTANT,"
                    + "NOMUTILISATEUR AS \"UTILISATEUR SAISIE\",REMARQUES AS \"REMARQUES.\",PATHIMAGE,FICHIERIMAGE FROM REMISES "
                    + "WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND IDREMISE  =" + curIdRemise;
            request.setAttribute("filtre", URLEncoder.encode(sql, "UTF-8"));
            sql = "SELECT IDCHEQUE as \"IDCHEQUE\",NUMEROCHEQUE, MONTANTCHEQUE, BANQUE,AGENCE,NUMEROCOMPTE, RIBCOMPTE,DATEEMISSION,DATETRAITEMENT,PATHIMAGE,FICHIERIMAGE FROM CHEQUES "
                    + "WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND REMISE =" + curIdRemise;
            request.setAttribute("filtre1", URLEncoder.encode(sql, "UTF-8"));

        } else {
            finish = true;
            out.println("Vous n'avez plus de remises à valider, merci");
            out.println("\nVoulez-vous imprimer votre rapport quotidien?");

            String dateTraitement = Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyy/MM/dd");
            String interval = "Agence de Saisie:" + user.getAdresse().trim() + "| Date de Traitement du " + dateTraitement;
            out.println(interval);
            String query = "select C1.IDCHEQUE,C1.REMISE,C1.DEVISE,C1.NUMEROCHEQUE,C1.NUMEROCOMPTE,C1.NOMBENEFICIAIRE,C1.ETAT,C1.AGENCE,C1.COMPTEREMETTANT,C1.AGENCEREMETTANT,C1.DATETRAITEMENT,C1.DATECOMPENSATION,"
                    + "C1.banqueremettant ,B1.libellebanque as LIBELLEBANQUEREMETTANT,C2.banque ,B2.libellebanque as LIBELLEBANQUE,C1.montantcheque ,C1.MOTIFREJET "
                    + "from cheques C1,cheques C2, Banques B1,Banques B2,Remises R "
                    + "where C1.IDCHEQUE=C2.IDCHEQUE  AND C1.banqueremettant=B1.codebanque and C2.banque=B2.codebanque "
                    + "and C1.etat in (35,40,50,130,900)  "
                    + "and C1.datetraitement ='" + dateTraitement + "' and C1.remise=R.idremise "
                    + "and R.agenceDepot like '" + user.getAdresse().trim() + "' ";


    %>
    <form name="printReport"  action="ControlServlet" method="POST" onsubmit="return resolveQuotidien();">
        <table>
            
            <tr>
                 <td><input type="radio" id="orderbybanque" name="nomrapport" value="Cheques" checked="checked"/>Regroupé par Banque</td>
                <td><input type="radio" id="orderbyremise" name="nomrapport" value="Cheques_clients_remises"  />Regroupé par Remise</td>
            </tr>
            <tr>
                <td><input type="radio" name="typerapport" value="application/vnd.ms-excel" />EXCEL</td>
                <td><input type="radio" name="typerapport" value="application/rtf" />WORD</td>
                <td><input type="radio" name="typerapport" value="application/pdf"  checked="checked"/>PDF</td>
                <td><input type="radio" name="typerapport" value="text/html" />Aperçu HTML</td>
            </tr>
            <tr>
                <td><input type="submit" value="Imprimer" name="oui" /></td>
                <td><input type="checkbox" name="avecDetail" value="ON" checked="checked"/>Avec Détails</td>
            </tr>

        </table>
        <input type='hidden' name='action' value='rapport' />
        <input type='hidden' name='interval' value='<%=interval%>'/>
        <input type='hidden' name='requete' value="dynamique" />
        <input type='hidden' name='query' value="<%=query%>"/>
       
        
    </form>

    <%
        }
        db.close();

    %>
    <%if (!finish) {%>

    <div id="imageMasterDIV" >
        <form name='imgRemForm'>
            <img name='imgremr' id='imgremr' src='${requestScope['fPicture']}' width="580" height="280" alt='|Aucune image recto associee|' />
            <br>
            <img name='imgremv' id='imgremv' src='${requestScope['rPicture']}' width="580" height="280" alt='|Aucune image verso associee|' />
        </form>

    </div>
    <input name="printMaster" type="button"   onClick="printdiv('imageMasterDIV');" value=" Imprimer ">
    <div>
        <a href="#" id="prive_btn_masque_master" title="Masquer / Afficher l'image"></a>
    </div>
    <DIV  >

        <a:widget name="yahoo.dataTable" id="remises" args="{paginated:false}"  service="data.jsp?requete=${requestScope['filtre']}&objet=REMISES&nomidobjet=IDREMISE"
                  />

    </DIV>


    <form name="detailForm" id="detailForm">
        <div id="monitorPanelDIV">
            <input type='button' name='valider' value='Valider' onclick=" validerRemiseCBAO();"/>
            <input type='button' name='rejeter' value='Rejeter' onclick=" rejeterRemise();"/>
        </div>
        <input type="hidden" name="requeteDetail" value='${requestScope['filtre1']}'/>
        <input type="hidden" name="idObjet" value="${requestScope['idObjet']}"/>
        <input type="hidden" name="action" value="updateDataTableUbaGuinee"/>
        <input type="hidden" name="message" value=""/>
        <input type="hidden" name="oldvalue" value="30"/>
        <input type="hidden" name="newvalue" value="20"/>
        <input type="hidden" name="primaryClause" value=""/>
        <input type="hidden" name="table" value="REMISES"/>
        <input type="hidden" name="colonne" value="ETAT"/>
        <input type="hidden" name="remarques" value=""/>

    </form>

    <br>
    <div id="imageDetailDIV" >
        <form name='imgForm'>
            <img name='imgchqr' id='imgchqr' src='${requestScope['fPictureCheque']}' width="570" height="280" alt='|Aucune image recto associee|' />
            <img name='imgchqv' id='imgchqv' src='${requestScope['rPictureCheque']}' width="570" height="280" alt='|Aucune image verso associee|' />

        </form>

    </div>
    <input name="printDetail" type="button"   onClick="printdiv('imageDetailDIV');" value=" Imprimer ">
    <div>
        <a href="#" id="prive_btn_masque_baniere" title="Masquer / Afficher l'image"></a>
    </div>
    <DIV  >

        <a:widget name="yahoo.dataTable" id="tableCheques" args="{scrollable:true}" service="data.jsp?requete=${requestScope['filtre1']}&objet=CHEQUES&nomidobjet=IDCHEQUE" publish="/jmaki/tableCheques/onClick"/>

    </DIV>
    <%}
        }%>
    <script>

        function zoomImgr(e) {

            document.getElementById('imgchqr').width = document.getElementById('imgchqr').width + 30;
            document.getElementById('imgchqr').height = document.getElementById('imgchqr').height + 15;
        }
        function zoomImgv(e) {

            document.getElementById('imgchqv').width = document.getElementById('imgchqr').width + 30;
            document.getElementById('imgchqv').height = document.getElementById('imgchqr').height + 15;
        }

        function deZoomImgr(e) {

            document.oncontextmenu = new Function("return false");
            if ((!document.all && e.which == 3) || (document.all && e.button == 2))
            {
                document.getElementById('imgchqr').width = document.getElementById('imgchqr').width - 30;
                document.getElementById('imgchqr').height = document.getElementById('imgchqr').height - 15;
            }
            //document.oncontextmenu = null;

        }
        function deZoomImgv(e) {
            document.oncontextmenu = new Function("return false");
            if ((!document.all && e.which == 3) || (document.all && e.button == 2))
            {

                document.getElementById('imgchqv').width = document.getElementById('imgchqv').width - 30;
                document.getElementById('imgchqv').height = document.getElementById('imgchqv').height - 15;
            }

            // document.oncontextmenu = null;
        }
        function changeCursorToZoom(e) {
            document.getElementById('imgchqr').style.cursor = "crosshair";
            document.getElementById('imgchqv').style.cursor = "crosshair";
        }
        function changeCursorToDefault(e) {
            document.getElementById('imgchqr').style.cursor = "default";
            document.getElementById('imgchqv').style.cursor = "default";
        }
        document.getElementById('imgchqr').onmousedown = deZoomImgr;
        document.getElementById('imgchqr').onclick = zoomImgr;
        document.getElementById('imgchqr').onmouseover = changeCursorToZoom;
        document.getElementById('imgchqr').onmouseout = changeCursorToDefault;
        document.getElementById('imgchqv').onmousedown = deZoomImgv;
        document.getElementById('imgchqv').onclick = zoomImgv;
        document.getElementById('imgchqv').onmouseover = changeCursorToZoom;
        document.getElementById('imgchqv').onmouseout = changeCursorToDefault;
    </script>
    <script>
        <!--
        var slideBaniere = new Fx.Slide('imageDetailDIV');

        $('prive_btn_masque_baniere').addEvent('click', function (e) {
            e = new Event(e);
            slideBaniere.toggle();
            e.stop();

            if (document.getElementById('prive_btn_masque_baniere').style.backgroundImage != 'url(images/btn_montre_entete.gif)') {
                document.getElementById('prive_btn_masque_baniere').style.backgroundImage = 'url(images/btn_montre_entete.gif)';
                EffaceCookie('imageDetail');
                EcrireCookie('imageDetail', 'non');
            } else {
                document.getElementById('prive_btn_masque_baniere').style.backgroundImage = 'url(images/btn_masque_entete.gif)';
                EffaceCookie('imageDetail');
                EcrireCookie('imageDetail', 'oui');
            }
        });

        function affiche_baniere() {

            if (LireCookie('imageDetail') == 'non') {
                document.getElementById('prive_btn_masque_baniere').style.backgroundImage = 'url(images/btn_montre_entete.gif)'
                slideBaniere.hide();
            }

        }

//-->

    </script>

        <script>

        function zoomImgr(e) {

            document.getElementById('imgremr').width = document.getElementById('imgremr').width + 10;
            document.getElementById('imgremr').height = document.getElementById('imgremr').height + 5;
        }
        function zoomImgv(e) {

            document.getElementById('imgremv').width = document.getElementById('imgremr').width + 10;
            document.getElementById('imgremv').height = document.getElementById('imgremr').height + 5;
        }

        function deZoomImgr(e) {

            document.oncontextmenu = new Function("return false");
            if ((!document.all && e.which == 3) || (document.all && e.button == 2))
            {
                document.getElementById('imgremr').width = document.getElementById('imgremr').width - 10;
                document.getElementById('imgremr').height = document.getElementById('imgremr').height - 5;
            }
            //document.oncontextmenu = null;

        }
        function deZoomImgv(e) {
            document.oncontextmenu = new Function("return false");
            if ((!document.all && e.which == 3) || (document.all && e.button == 2))
            {

                document.getElementById('imgremv').width = document.getElementById('imgremv').width - 10;
                document.getElementById('imgremv').height = document.getElementById('imgremv').height - 5;
            }

            // document.oncontextmenu = null;
        }
        function changeCursorToZoom(e) {
            document.getElementById('imgremr').style.cursor = "crosshair";
            document.getElementById('imgremv').style.cursor = "crosshair";
        }
        function changeCursorToDefault(e) {
            document.getElementById('imgremr').style.cursor = "default";
            document.getElementById('imgremv').style.cursor = "default";
        }
        document.getElementById('imgremr').onmousedown = deZoomImgr;
        document.getElementById('imgremr').onclick = zoomImgr;
        document.getElementById('imgremr').onmouseover = changeCursorToZoom;
        document.getElementById('imgremr').onmouseout = changeCursorToDefault;
        document.getElementById('imgremv').onmousedown = deZoomImgv;
        document.getElementById('imgremv').onclick = zoomImgv;
        document.getElementById('imgremv').onmouseover = changeCursorToZoom;
        document.getElementById('imgremv').onmouseout = changeCursorToDefault;
    </script>
    <script>
        <!--
        var slideMaster = new Fx.Slide('imageMasterDIV');

        $('prive_btn_masque_master').addEvent('click', function (e) {
            e = new Event(e);
            slideMaster.toggle();
            e.stop();

            if (document.getElementById('prive_btn_masque_master').style.backgroundImage != 'url(images/btn_montre_entete.gif)') {
                document.getElementById('prive_btn_masque_master').style.backgroundImage = 'url(images/btn_montre_entete.gif)';
                EffaceCookie('imageMaster');
                EcrireCookie('imageMaster', 'non');
            } else {
                document.getElementById('prive_btn_masque_master').style.backgroundImage = 'url(images/btn_masque_entete.gif)';
                EffaceCookie('imageMaster');
                EcrireCookie('imageMaster', 'oui');
            }
        });

        function affiche_master() {

            if (LireCookie('imageMaster') == 'non') {
                document.getElementById('prive_btn_masque_master').style.backgroundImage = 'url(images/btn_montre_entete.gif)'
                slideMaster.hide();
            }
            jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value = '%%';

        }

        //-->

    </script>


</body> 
