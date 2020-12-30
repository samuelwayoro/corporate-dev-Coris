<%@page  import="java.net.URLEncoder" contentType="text/html"%>
<%@page contentType="text/html" import="org.patware.jdbc.DataBase,java.math.BigDecimal,clearing.table.Utilisateurs"%>
<%@page pageEncoding="UTF-8" import="org.patware.xml.JDBCXmlReader,org.patware.web.bean.MessageBean,org.patware.utils.Utility"%>
<%@page import="org.patware.xml.JDBCXmlReader,clearing.table.Sequences,clearing.table.Remises,clearing.table.Sequences,clearing.table.Cheques,clearing.table.Banques"%>
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
<body onload="affiche_master();" >
    <%
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        boolean finish = false;
        String sql = "SELECT * FROM REMISES WHERE IDREMISE = (SELECT MIN(IDREMISE) FROM REMISES WHERE ETAT=" + Utility.getParam("CETAREMSAIIRIS")
                +  " AND AGENCEDEPOT='"+ user.getAdresse().trim()+"'"+
                " AND (VALIDEUR='' OR VALIDEUR IS NULL OR VALIDEUR = '" + user.getLogin().trim() + "')) FOR UPDATE";

        Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
        if (remises != null && remises.length > 0) {

            BigDecimal curIdRemise = remises[0].getIdremise();

            request.setAttribute("idObjet", curIdRemise);
            String fPicture = remises[0].getPathimage().substring(3) + "\\" + remises[0].getFichierimage() + "f.jpg";
            String rPicture = remises[0].getPathimage().substring(3) + "\\" + remises[0].getFichierimage() + "r.jpg";
            request.setAttribute("fPicture", fPicture.replace("\\", "/"));
            request.setAttribute("rPicture", rPicture.replace("\\", "/"));
            request.setAttribute("montantRemise", remises[0].getMontant());
            request.setAttribute("compteRemettant", remises[0].getCompteRemettant());
            sql = "UPDATE REMISES SET VALIDEUR='" + user.getLogin().trim() + "', AGENCEDEPOT='" + user.getAdresse().trim() + "' WHERE IDREMISE=" + curIdRemise;
            db.executeUpdate(sql);

            sql = "SELECT IDREMISE,COMPTEREMETTANT as \"COMPTE REMETTANT\",NOMCLIENT,MONTANT,NBOPERATION,DATESAISIE,AGENCEREMETTANT,"
                    + "NOMUTILISATEUR AS \"UTILISATEUR SAISIE\",REMARQUES AS \"REMARQUES.\",PATHIMAGE,FICHIERIMAGE FROM REMISES "
                    + "WHERE IDREMISE =" + curIdRemise;
            request.setAttribute("filtre", URLEncoder.encode(sql, "UTF-8"));
            sql = "SELECT IDCHEQUE as \"IDCHEQUE\",NUMEROCHEQUE, MONTANTCHEQUE, BANQUE,AGENCE,NUMEROCOMPTE, RIBCOMPTE,DATEEMISSION,DATETRAITEMENT,PATHIMAGE,FICHIERIMAGE FROM CHEQUES "
                    + "WHERE REMISE =" + curIdRemise;
            request.setAttribute("filtre1", URLEncoder.encode(sql, "UTF-8"));

        } else {
            out.println("Vous n'avez plus de remises à valider, merci");
            finish = true;
        }
        db.close();


    %>
    <%if (!finish) {%>

    <div id="imageMasterDIV" >
        <form name='imgRemForm'>
            <img name='imgremr' id='imgremr' src='${requestScope['fPicture']}' width="540" height="220" alt='|Aucune image recto associee|' />
            <img name='imgremv' id='imgremv' src='${requestScope['rPicture']}' width="540" height="220" alt='|Aucune image verso associee|' />
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
            <input type='button' name='valider' value='Valider' onclick=" validerRemiseBorne();"/>
            <input type='button' name='rejeter' value='Rejeter' onclick=" rejeterRemiseBorne();"/>
        </div>
        <input type="hidden" name="requeteDetail" value='${requestScope['filtre1']}'/> 
        <input type="hidden" name="idObjet" value="${requestScope['idObjet']}"/>
        <input type="hidden" name="action" value="updateDataTable"/>
        <input type="hidden" name="message" value=""/>
        <input type="hidden" name="oldvalue" value="16"/>
        <input type="hidden" name="newvalue" value="35"/>
        <input type="hidden" name="primaryClause" value=""/>
        <input type="hidden" name="table" value="REMISES"/>
        <input type="hidden" name="colonne" value="ETAT"/>
        <input type="hidden" name="remarques" value=""/>

    </form>

    <br>
    <div id="imageDetailDIV" >
        <form name='imgForm'>
            <img name='imgchqr' id='imgchqr' src='' width="550" height="250" alt='|Aucune image recto associee|' />
            <img name='imgchqv' id='imgchqv' src='' width="550" height="250" alt='|Aucune image verso associee|' />

        </form>

    </div>
    <input name="printDetail" type="button"   onClick="printdiv('imageDetailDIV');" value=" Imprimer ">
    <div>
        <a href="#" id="prive_btn_masque_baniere" title="Masquer / Afficher l'image"></a>
    </div>
    <DIV  >

        <a:widget name="yahoo.dataTable" id="tableCheques" args="{scrollable:true}" service="data.jsp?requete=${requestScope['filtre1']}&objet=CHEQUES&nomidobjet=IDCHEQUE" publish="/jmaki/tableCheques/onClick"/>

    </DIV> 
    <script>

        function zoomImgr(e){

        document.getElementById('imgchqr').width = document.getElementById('imgchqr').width + 10;
        document.getElementById('imgchqr').height = document.getElementById('imgchqr').height + 5;
        }
        function zoomImgv(e){

        document.getElementById('imgchqv').width = document.getElementById('imgchqr').width + 10;
        document.getElementById('imgchqv').height = document.getElementById('imgchqr').height + 5;
        }

        function deZoomImgr(e){

        document.oncontextmenu = new Function ("return false");
        if ((!document.all && e.which == 3) || (document.all && e.button == 2))
        {
        document.getElementById('imgchqr').width = document.getElementById('imgchqr').width - 10;
        document.getElementById('imgchqr').height = document.getElementById('imgchqr').height - 5;
        }
        //document.oncontextmenu = null;

        }
        function deZoomImgv(e){
        document.oncontextmenu = new Function ("return false");
        if ((!document.all && e.which == 3) || (document.all && e.button == 2))
        {

        document.getElementById('imgchqv').width = document.getElementById('imgchqv').width - 10;
        document.getElementById('imgchqv').height = document.getElementById('imgchqv').height - 5;
        }

        // document.oncontextmenu = null;
        }
        function changeCursorToZoom(e){
        document.getElementById('imgchqr').style.cursor = "crosshair";
        document.getElementById('imgchqv').style.cursor = "crosshair";
        }
        function changeCursorToDefault(e){
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
        $('prive_btn_masque_baniere').addEvent('click', function(e){
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
        function affiche_baniere(){

        if (LireCookie('imageDetail') == 'non') {
        document.getElementById('prive_btn_masque_baniere').style.backgroundImage = 'url(images/btn_montre_entete.gif)'
                slideBaniere.hide();
        }

        }

        //-->

    </script>

   <script>

        function zoomImgr(e){

        document.getElementById('imgremr').width = document.getElementById('imgremr').width + 10;
        document.getElementById('imgremr').height = document.getElementById('imgremr').height + 5;
        }
        function zoomImgv(e){

        document.getElementById('imgremv').width = document.getElementById('imgremr').width + 10;
        document.getElementById('imgremv').height = document.getElementById('imgremr').height + 5;
        }

        function deZoomImgr(e){

        document.oncontextmenu = new Function ("return false");
        if ((!document.all && e.which == 3) || (document.all && e.button == 2))
        {
        document.getElementById('imgremr').width = document.getElementById('imgremr').width - 10;
        document.getElementById('imgremr').height = document.getElementById('imgremr').height - 5;
        }
        //document.oncontextmenu = null;

        }
        function deZoomImgv(e){
        document.oncontextmenu = new Function ("return false");
        if ((!document.all && e.which == 3) || (document.all && e.button == 2))
        {

        document.getElementById('imgremv').width = document.getElementById('imgremv').width - 10;
        document.getElementById('imgremv').height = document.getElementById('imgremv').height - 5;
        }

        // document.oncontextmenu = null;
        }
        function changeCursorToZoom(e){
        document.getElementById('imgremr').style.cursor = "crosshair";
        document.getElementById('imgremv').style.cursor = "crosshair";
        }
        function changeCursorToDefault(e){
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
    </scr    ipt>
        <script>
        <!--
var s        lideMaster = new Fx.Slide('imageMasterDIV');
        
        $('pr        ive_btn_masque_master').addEvent('click', function(e){
                e = new Event(e);
        s            lideMaster.toggle();
        e.stop();
        i            f (document.getElementById('prive_btn_masque_master').style.backgroundImage != 'url(images/btn_montre_entete.gif)') {
        document.getElementById('prive_btn_masque_master').style.backgroundImage = 'url(images/btn_montre_entete.gif)';
        EffaceCookie('imageMaster');
        EcrireCookie('imageMaster', 'non');
        }            else {
                d            ocument.getElementById('prive_btn_masque_master').style.backgroundImage = 'url(images/btn_masque_entete.gif)';
        E            ffaceCookie('imageMaster');
               E            crireCookie('imag e Master', 'oui');
        }
        });
        
       function     affiche_master(){

                if (L        ireCookie('imageMaster') == 'non') {
        d            ocument.getElementById('prive_btn_masque_master').style.backgroundImage = 'url(images/btn_montre_entete.gif)'
                s            lideMaster.hide();
        }
        
        }
        
        //-->
        
        </script>
    <%}%>
    
    <% MessageBean message = (MessageBean) request.getAttribute("message");
            if (message != null && message.getMessage() != null) {
                out.print(message.getMessage());
            }%>

</body> 
