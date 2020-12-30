<%@page  import="java.net.URLEncoder" contentType="text/html"%>
<%@page contentType="text/html" import="org.patware.jdbc.DataBase,java.math.BigDecimal,clearing.table.Utilisateurs"%>
<%@page pageEncoding="UTF-8" import="org.patware.xml.JDBCXmlReader,org.patware.web.bean.MessageBean,org.patware.utils.Utility"%>
<%@page import="org.patware.xml.JDBCXmlReader,clearing.table.Sequences,clearing.table.Remises,clearing.table.Sequences,clearing.table.Cheques,clearing.table.Banques"%>
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
<body onload="affiche_master();" >
             <%
              DataBase db = new DataBase(JDBCXmlReader.getDriver());
              Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");

              String banqueChoisie = (String) session.getAttribute("banqueChoisie");
              if(banqueChoisie == null) banqueChoisie = "%";
              comboBanqueBean.setRequete("select * from banques where codebanque in (select distinct banqueremettant from cheques where etat in ("+ Utility.getParam("CETAOPERETRECENVSIB")+") and lotsib is null)");
              db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
              String action = request.getParameter("choixBanques");
              if(action != null && action.equalsIgnoreCase("true")){%>
            
            <div align="center">
            <form name='choixCodeBanques'>
                 Choisissez une Banque : <a:widget name="dojo.combobox" id="banques" value="${comboBanqueBean.comboDatas}"/>
              <input type="button" name="startVal" value="Demarrer la Vérification des Vignettes" onclick="startValidationParBanque();">
                <input type="hidden" name="codeBanque" value=""/>
                <input type="hidden" name="action" value="choixCodeBanques"/>
            </form>
        </div>
              <%}else{
              boolean finish = false;
              out.println(banqueChoisie);
              String sql = "SELECT * FROM CHEQUES WHERE IDCHEQUE = (SELECT MIN(IDCHEQUE) FROM CHEQUES WHERE ETAT="+ Utility.getParam("CETAOPERETRECENVSIB")+
                           " AND LOTSIB IS NULL"+
                           " AND BANQUEREMETTANT LIKE ('"+ banqueChoisie+"')"+
                           " AND (CODEUTILISATEUR='' OR CODEUTILISATEUR IS NULL OR CODEUTILISATEUR = '" + user.getLogin().trim() + "')) FOR UPDATE";
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {

                    BigDecimal curIdCheque = cheques[0].getIdcheque();
                    
                    request.setAttribute("idObjet", curIdCheque);
                    String fPicture = cheques[0].getPathimage().substring(3) + "\\" + cheques[0].getFichierimage() + "f.jpg";
                    String rPicture = cheques[0].getPathimage().substring(3) + "\\" + cheques[0].getFichierimage() + "r.jpg";
                    String snippet =  cheques[0].getPathimage().substring(3) + "\\" + cheques[0].getFichierimage() + "s.jpg";
                    request.setAttribute("fPicture", fPicture.replace("\\", "/"));
                    request.setAttribute("rPicture", rPicture.replace("\\", "/"));
                    request.setAttribute("snippet", snippet.replace("\\", "/"));
                    request.setAttribute("montantCheque", cheques[0].getMontantcheque());
                    request.setAttribute("codeUV", cheques[0].getCodeVignetteUV());
                    sql = "UPDATE CHEQUES SET CODEUTILISATEUR='" + user.getLogin().trim() + "' WHERE IDCHEQUE=" + curIdCheque;
                    db.executeUpdate(sql);

                   
                    sql = "SELECT IDCHEQUE as \"IDCHEQUE\",NUMEROCHEQUE, MONTANTCHEQUE, BANQUE,AGENCE,NUMEROCOMPTE, RIBCOMPTE,DATEEMISSION,DATETRAITEMENT,PATHIMAGE,FICHIERIMAGE FROM CHEQUES "+
                           "WHERE IDCHEQUE ="+ curIdCheque;
                    request.setAttribute("filtre", URLEncoder.encode(sql, "UTF-8"));

                }else{
                    out.println("Vous n'avez plus de cheques à verifier, merci");
                    finish = true;
                    }
         db.close();
         

%>
 <%if (!finish) {%>

    <DIV>
 
        <a:widget name="yahoo.dataTable" id="cheques" args="{paginated:false}"  service="data.jsp?requete=${requestScope['filtre']}&objet=CHEQUES&nomidobjet=IDCHEQUE"
                  />
                  
    </DIV> 
    <br><br>



        <div id="vignetteDIV" >
            <form name='imgChqForm'>
                <img name='imgchqr' id='imgchqr' src='${requestScope['fPicture']}' width="540" height="220" alt='|Aucune image recto associee|' />
                <img name='imgchqv' id='imgchqv' src='${requestScope['rPicture']}' width="540" height="220" alt='|Aucune image verso associee|' />
            </form>

        </div>
        <br><center>Apercu du Numero d'endos </center>
        <div align="center">
            <img name='imgsnippet' id='imgsnippet' src='${requestScope['snippet']}' width="540" height="60" alt='|Aucun snippet associe|' />
            <input name="rotateBtn" type="button" onclick="rotateSnippet();" value="Rotate">
        </div>
        <input name="printMaster" type="button"   onClick="printdiv('vignetteDIV');" value=" Imprimer ">
        <div>
             <a href="#" id="prive_btn_masque_master" title="Masquer / Afficher l'image"></a>
        </div>

        <form name="detailForm" id="detailForm">
            <div id="monitorPanelDIV">
                <table border="1" cellpadding="2" align="center">

                        <tr>
                            <td>Code Vignette:</td>
                            <td><input type='text' name='codeVignette' value='${requestScope['codeUV']}' size="25"/></td>
                            <td> ou </td>
                            <td>Numero d'endos:</td>
                            <td><input type='text' name='numeroEndos' value='' size="25"/></td>
                            <td> <input type='button' name='verifier' value='Vérifier' onclick=" verifierVignette();"/></td>
                        </tr>

                       
                </table>

           
              </div>
           
           <input type="hidden" name="idObjet" value="${requestScope['idObjet']}"/>
           <input type="hidden" name="action" value="checkVignette"/>
          

        </form>
        <div id="messageDiv" align="center"></div>
        <div align="center">
        <form name="confirmForm" id="confirmForm">
            <input type='button' name='confirmer' value='Confirmer' style="display:none" onclick=" confirmerVignette();"/>
            <input type="hidden" name="idObjet" value="${requestScope['idObjet']}"/>
            <input type="hidden" name="codeVignette" value=""/>
            <input type="hidden" name="numeroEndos" value=""/>
           <input type="hidden" name="action" value="confirmVignette"/>
        </form>
       </div>
<br>
   
<%}}%>
   <script>

            function zoomImgr(e){

                document.getElementById('imgchqr').width = document.getElementById('imgchqr').width + 30;
                document.getElementById('imgchqr').height = document.getElementById('imgchqr').height + 15;
            }
            function zoomImgv(e){

                document.getElementById('imgchqv').width = document.getElementById('imgchqv').width + 30;
                document.getElementById('imgchqv').height = document.getElementById('imgchqv').height + 15;
            }
            function zoomImgs(e){

                document.getElementById('imgsnippet').width = document.getElementById('imgsnippet').width + 30;
                document.getElementById('imgsnippet').height = document.getElementById('imgsnippet').height + 15;
            }
            function deZoomImgs(e){

                document.oncontextmenu = new Function ("return false");
                if ((!document.all && e.which == 3) || (document.all && e.button==2))
                    {
                        document.getElementById('imgsnippet').width = document.getElementById('imgsnippet').width - 30;
                        document.getElementById('imgsnippet').height = document.getElementById('imgsnippet').height - 15;
                    }
                    //document.oncontextmenu = null;

                }

            function deZoomImgr(e){

                document.oncontextmenu = new Function ("return false");
                if ((!document.all && e.which == 3) || (document.all && e.button==2))
                    {
                        document.getElementById('imgchqr').width = document.getElementById('imgchqr').width - 30;
                        document.getElementById('imgchqr').height = document.getElementById('imgchqr').height - 15;
                    }
                    //document.oncontextmenu = null;

                }
                function deZoomImgv(e){
                    document.oncontextmenu = new Function ("return false");
                    if ((!document.all && e.which == 3) || (document.all && e.button==2))
                        {

                            document.getElementById('imgchqv').width = document.getElementById('imgchqv').width - 30;
                            document.getElementById('imgchqv').height = document.getElementById('imgchqv').height - 15;
                        }

                        // document.oncontextmenu = null;
                    }
                    function changeCursorToZoom(e){
                        document.getElementById('imgchqr').style.cursor="crosshair";
                        document.getElementById('imgchqv').style.cursor="crosshair";
                    }
                    function changeCursorToDefault(e){
                        document.getElementById('imgchqr').style.cursor="default";
                        document.getElementById('imgchqv').style.cursor="default";
                    }
                    
            document.getElementById('imgsnippet').onmousedown = deZoomImgs;
            document.getElementById('imgsnippet').onclick=zoomImgs;
            document.getElementById('imgchqr').onmousedown = deZoomImgr;
            document.getElementById('imgchqr').onclick=zoomImgr;
            document.getElementById('imgchqr').onmouseover=changeCursorToZoom;
            document.getElementById('imgchqr').onmouseout=changeCursorToDefault;
            document.getElementById('imgchqv').onmousedown = deZoomImgv;
            document.getElementById('imgchqv').onclick=zoomImgv;
            document.getElementById('imgchqv').onmouseover=changeCursorToZoom;
            document.getElementById('imgchqv').onmouseout=changeCursorToDefault;
           
            
        </script>
      

       
        <script>
            <!--
            var slideMaster = new Fx.Slide('vignetteDIV');

            $('prive_btn_masque_master').addEvent('click', function(e){
                e = new Event(e);
                slideMaster.toggle();
                e.stop();

                if (document.getElementById('prive_btn_masque_master').style.backgroundImage != 'url(images/btn_montre_entete.gif)') {
                    document.getElementById('prive_btn_masque_master').style.backgroundImage = 'url(images/btn_montre_entete.gif)';
                    EffaceCookie('imageVignette');
                    EcrireCookie('imageVignette', 'non');
                } else {
                document.getElementById('prive_btn_masque_master').style.backgroundImage = 'url(images/btn_masque_entete.gif)';
                EffaceCookie('imageVignette');
                EcrireCookie('imageVignette', 'oui');
            }
        });

        function affiche_master(){

            if (LireCookie('imageVignette') == 'non') {
                document.getElementById('prive_btn_masque_master').style.backgroundImage = 'url(images/btn_montre_entete.gif)'
                slideMaster.hide();
            }
            x=0;


        }
        function rotateSnippet(){

            document.getElementById('imgsnippet').style.transform='rotate('+((x=++x%4)*180)+'deg)';
            //alert('rotate('+((x=++x%4)*90)+'deg)');
        }
        //-->

        </script>


</body> 
