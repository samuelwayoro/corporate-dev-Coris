<%@page  import="java.net.URLEncoder" contentType="text/html"%>
<%@page contentType="text/html" import="org.patware.jdbc.DataBase,java.math.BigDecimal,clearing.table.Utilisateurs"%>
<%@page pageEncoding="UTF-8" import="org.patware.xml.JDBCXmlReader,org.patware.web.bean.MessageBean,org.patware.utils.Utility"%>
<%@page import="org.patware.xml.JDBCXmlReader,clearing.model.CMPUtility,clearing.table.Cheques,clearing.table.Banques,java.util.Date"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<jsp:include page="checkaccess.jsp"/>
<style type="text/css">

    body {

        font-weight: bold;
    }

</style>
<script type="text/javascript"  src="mootools-packed.js"></script>
<script type="text/javascript"  src="clearing.js"></script>
<script type="text/javascript"  src="cookies.js"></script>

<link href="travail_prive2.css" rel="stylesheet" type="text/css" />
<jsp:include page="checkaccess.jsp"/>
<jsp:useBean id="comboBanqueBean" class="clearing.web.bean.ComboBanqueBean" />
<body>
    <%
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");

        String banqueChoisie = (String) session.getAttribute("banqueChoisie");
        if (banqueChoisie == null) {
            banqueChoisie = "%";
        }
        comboBanqueBean.setRequete("select * from banques where codebanque in (select distinct banqueremettant from cheques where etat in (" + Utility.getParam("CETAOPERETRECENVSIB") + ") and LOTSIB IN (1,2))");
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String action = request.getParameter("choixBanques");
                 if (action != null && action.equalsIgnoreCase("true")) {%>

    <div align="center">
        <form name='choixCodeBanques'>
            Choisissez une Banque : <a:widget name="dojo.combobox" id="banques" value="${comboBanqueBean.comboDatas}"/>
            <input type="button" name="startVal" value="Demarrer la Vérification des Signatures" onclick="startValidationParBanque();">
            <input type="hidden" name="codeBanque" value=""/>
            <input type="hidden" name="action" value="choixCodeBanques"/>
        </form>
    </div>
    <%} else {
        boolean finish = false;
        String[] signatures = null;
        int nbChqRestant = 0;
        out.println(banqueChoisie);
        String sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPERETRECENVSIB")
                + " AND LOTSIB IN (1,2)"
                + " AND BANQUEREMETTANT LIKE ('" + banqueChoisie + "')"
                + " AND DATECOMPENSATION >=( SELECT MAX(DATECOMPENSATION) FROM CHEQUES  WHERE ETAT=" + Utility.getParam("CETAOPERETRECENVSIB") + ")"
                + " AND (CODEUTILISATEUR='' OR CODEUTILISATEUR IS NULL OR CODEUTILISATEUR = '" + user.getLogin().trim() + "') ORDER BY LOTSIB, IDCHEQUE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        if (cheques != null && cheques.length > 0) {

            BigDecimal curIdCheque = cheques[0].getIdcheque();

            request.setAttribute("idObjet", curIdCheque);
            String fPicture = cheques[0].getPathimage().substring(3) + "\\" + cheques[0].getFichierimage() + "f.jpg";
            String rPicture = cheques[0].getPathimage().substring(3) + "\\" + cheques[0].getFichierimage() + "r.jpg";
            if (Utility.getParam("FLEXVERSION") != null && Utility.getParam("FLEXVERSION").equals("7")) {
                signatures = CMPUtility.getFlexSignatures7(cheques[0].getAgence(), cheques[0].getNumerocompte());
            } else {
                signatures = CMPUtility.getFlexSignatures(cheques[0].getAgence(), cheques[0].getNumerocompte());
            }
            request.setAttribute("fPicture", fPicture.replace("\\", "/"));
            request.setAttribute("rPicture", rPicture.replace("\\", "/"));

            request.setAttribute("montantCheque", Utility.formatNumber(cheques[0].getMontantcheque()));

            sql = "UPDATE CHEQUES SET CODEUTILISATEUR='" + user.getLogin().trim() + "' WHERE IDCHEQUE=" + curIdCheque;
            db.executeUpdate(sql);

            sql = "SELECT IDCHEQUE as \"IDCHEQUE\",NUMEROCHEQUE, MONTANTCHEQUE, BANQUE,AGENCE,NUMEROCOMPTE, RIBCOMPTE,DATEEMISSION,DATETRAITEMENT,NOMBENEFICIAIRE,BANQUEREMETTANT FROM CHEQUES "
                    + "WHERE IDCHEQUE =" + curIdCheque;
            request.setAttribute("filtre", URLEncoder.encode(sql, "UTF-8"));

            nbChqRestant = cheques.length - 1;

        } else {
            out.println("Vous n'avez plus de cheques à verifier, merci");
            finish = true;
        }
        db.close();


    %>
    <%if (!finish) {%>



    <div id="imageDIV2" >
        <form name='imgChqForm'>
            <img name='imgchqr' id='imgchqr' src='${requestScope['fPicture']}' width="700" height="280" alt='|Aucune image recto associee|' />
            Mnt:${requestScope['montantCheque']}
            <br>
            <img name='imgchqv'  id='imgchqv' src='${requestScope['rPicture']}' width="700" height="280" alt='|Aucune image verso associee|' />
            Reste:<%=Utility.formatNumber("" + nbChqRestant)%>
        </form>

    </div>

    <form name="detailForm" id="detailForm">
        <input name="printMaster" type="button"  onClick="printSignDiv('imageDIV2');" value="<-Imprimer">
        <input type='button' name='passer' value='Passer' onclick=" passerSignCheque();"/>
        <input type='button' name='valider' value='Valider' onclick=" validerSignCheque();"/>
        <input type="hidden" name="idObjet" value="${requestScope['idObjet']}"/>
        <input type='hidden' name='etatdebut' value='(170)' />
        <input type='hidden' name='etatfin' value='250' />
        <input type="hidden" name="action" value="validerSignCheque"/>
    </form>
    <div id="imageSignDIV">
        <%if (signatures != null)
                for (int i = 0; i < signatures.length; i++) {
                    if (signatures[i].endsWith("jpg")) {
        %>    
        <table>
            <tr><td align="center"><img name='<%="signature" + i%>' id='<%="signature" + i%>' src='<%=signatures[i].substring(3).replace("\\", "/")%>' width="200" height="160" alt='<%=signatures[i].substring(3).replace("\\", "/")%>' /></td></tr>

            <br>
            <%
                        out.println("<tr><td align='center'>" + signatures[i].substring(signatures[i].lastIndexOf("\\") + 1, signatures[i].indexOf("_")) + "</td></tr>");
                        out.println("</table>");
                    } else {
                        out.println(signatures[i]);
                     }
                 }%>

            </div>

            <jsp:useBean id="comboMotifRejetBean" class="clearing.web.bean.ComboMotifsRejetBean" />
            <form name='rejetForm'>
                <div id="rejetDIV">

                    <a:widget name="dojo.combobox" id="comboMotif" value="${comboMotifRejetBean.comboDatas}" />
                    <input type='hidden' name='motifrejet' value='dynamique' />
                    <input type='hidden' name='idObjet' value="${requestScope['idObjet']}" />
                    <input type='hidden' name='etatdebut' value='(170)' />
                    <input type='hidden' name='etatfin' value='250' />
                    <input type='button' name='rejet' value='Rejeter' onclick='rejeterSignCheque();'/>
                    <input type='hidden' name='action' value='rejeterSignCheque' />
                </div>
            </form>




            <br>
            <DIV id="listSignDIV">

                <a:widget name="yahoo.dataTable" id="tableCheques" args="{paginated:false}"  service="data.jsp?requete=${requestScope['filtre']}&objet=CHEQUES&nomidobjet=IDCHEQUE"
                          />

            </DIV>



            <%}
    }%>
            <script>

                function zoomImgr(e) {

                    document.getElementById('imgchqr').width = document.getElementById('imgchqr').width + 30;
                    document.getElementById('imgchqr').height = document.getElementById('imgchqr').height + 15;
                }
                function zoomImgv(e) {

                    document.getElementById('imgchqv').width = document.getElementById('imgchqv').width + 30;
                    document.getElementById('imgchqv').height = document.getElementById('imgchqv').height + 15;
                }
                function zoomImgs(e) {

                    document.getElementById('imgsnippet').width = document.getElementById('imgsnippet').width + 30;
                    document.getElementById('imgsnippet').height = document.getElementById('imgsnippet').height + 15;
                }
                function deZoomImgs(e) {

                    document.oncontextmenu = new Function("return false");
                    if ((!document.all && e.which == 3) || (document.all && e.button == 2))
                    {
                        document.getElementById('imgsnippet').width = document.getElementById('imgsnippet').width - 30;
                        document.getElementById('imgsnippet').height = document.getElementById('imgsnippet').height - 15;
                    }
                    //document.oncontextmenu = null;

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

                document.getElementById('imgsnippet').onmousedown = deZoomImgs;
                document.getElementById('imgsnippet').onclick = zoomImgs;
                document.getElementById('imgchqr').onmousedown = deZoomImgr;
                document.getElementById('imgchqr').onclick = zoomImgr;
                document.getElementById('imgchqr').onmouseover = changeCursorToZoom;
                document.getElementById('imgchqr').onmouseout = changeCursorToDefault;
                document.getElementById('imgchqv').onmousedown = deZoomImgv;
                document.getElementById('imgchqv').onclick = zoomImgv;
                document.getElementById('imgchqv').onmouseover = changeCursorToZoom;
                document.getElementById('imgchqv').onmouseout = changeCursorToDefault;


            </script>





            </body> 
