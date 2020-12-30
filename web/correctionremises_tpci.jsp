<%@page contentType="text/html" import="org.patware.jdbc.DataBase,java.math.BigDecimal,clearing.table.Utilisateurs"%>
<%@page pageEncoding="UTF-8" import="org.patware.xml.JDBCXmlReader,org.patware.web.bean.MessageBean,org.patware.utils.Utility"%>
<%@page import="org.patware.xml.JDBCXmlReader,clearing.table.Sequences,clearing.table.Remises,clearing.table.Sequences,clearing.table.Cheques,clearing.table.Banques"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

        <style type="text/css">
        </style>
        <script type="text/javascript"  src="clearing.js"></script>
        <link href="travail_prive2.css" rel="stylesheet" type="text/css" />
    </head>
    <jsp:include page="checkaccess.jsp"/>
    <body onload="setFocus()">
        <%

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        String lastDocType = "";
        String nmourem = "0";
        int index = 0;
        int nbOpers = 0;
        Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String action = request.getParameter("action");
        String params_auto = Utility.getParam("AUTORISATION_SAISIE");
        if (params_auto == null || (params_auto != null && params_auto.equalsIgnoreCase("1"))) {

            //out.println(action);
            if (action != null && (action.equalsIgnoreCase("precedent") || action.equalsIgnoreCase("suivant"))) {
                //out.println(request.getParameter("idcheque"));

                String idObjet = request.getParameter("idcheque");
                String idRemise = request.getParameter("idremise");
                lastDocType = "CHEQUE";
                request.setAttribute("typeObjet", lastDocType);
                request.setAttribute("sequenceObjet", "0");
                long idCheque = 0;
                String sql = "";
                if (action.equalsIgnoreCase("precedent")) {
                    sql = "SELECT * FROM CHEQUES WHERE REMISE=" + idRemise + " AND IDCHEQUE<" + idObjet + " ORDER BY IDCHEQUE ASC";

                } else {
                    sql = "SELECT * FROM CHEQUES WHERE REMISE=" + idRemise + " AND IDCHEQUE>" + idObjet + " ORDER BY IDCHEQUE DESC";

                }
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {
                    idCheque = cheques[cheques.length - 1].getIdcheque().longValue();
                }
                action = null;
                sql = "SELECT * FROM CHEQUES WHERE IDCHEQUE=" + idCheque;
                cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if (cheques != null && cheques.length > 0) {
                    BigDecimal curIdCheque = cheques[0].getIdcheque();
                    request.setAttribute("idObjet", curIdCheque);
                    session.setAttribute("currentChequeCorr", cheques[0]);
                    request.setAttribute("montantCheque", cheques[0].getMontantcheque());
                    request.setAttribute("codeBanque", (cheques[0].getBanque() == null) ? "" : cheques[0].getBanque().contains("_") ? "" : cheques[0].getBanque());
                    request.setAttribute("codeAgence", (cheques[0].getAgence() == null) ? "" : cheques[0].getAgence().contains("_") ? "" : cheques[0].getAgence());
                    request.setAttribute("numeroCompte", (cheques[0].getNumerocompte() == null) ? "" : cheques[0].getNumerocompte().contains("_") ? "" : cheques[0].getNumerocompte());
                    request.setAttribute("clerib", (cheques[0].getRibcompte() == null) ? "" : cheques[0].getRibcompte().contains("_") ? "" : cheques[0].getRibcompte());
                    request.setAttribute("numeroCheque", (cheques[0].getNumerocheque() == null) ? "" : cheques[0].getNumerocheque().contains("_") ? "" : cheques[0].getNumerocheque());
                    request.setAttribute("idremise", cheques[0].getRemise());
                    sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[0].getRemise();
                    Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                    if (remises != null && remises.length > 0) {
                        session.setAttribute("currentRemiseCorr", remises[0]);
                        sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[0].getRemise() + " ORDER BY SEQUENCE";
                        Cheques[] chequesCal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                        for (int i = 0; i < chequesCal.length; i++) {
                            if (chequesCal[i].getIdcheque().equals(cheques[0].getIdcheque())) {
                                index = i + 1;
                            }
                        }
                        nbOpers = remises[0].getNbOperation().intValue();
                        request.setAttribute("index", index);
                        request.setAttribute("nbOpers", nbOpers);

                    }

                    String fPicture = cheques[0].getPathimage().substring(3) + "\\" + cheques[0].getFichierimage() + "f.jpg";
                    String rPicture = cheques[0].getPathimage().substring(3) + "\\" + cheques[0].getFichierimage() + "r.jpg";
                    request.setAttribute("fPicture", fPicture.replace("\\", "/"));
                    request.setAttribute("rPicture", rPicture.replace("\\", "/"));
                }
            } else if (action != null && action.equalsIgnoreCase("remise")) {
                //out.println(request.getParameter("idremise"));
                action = null;
                String idRemise = request.getParameter("idremise");
                lastDocType = "REMISE";
                request.setAttribute("typeObjet", lastDocType);
                request.setAttribute("sequenceObjet", "0");

                String sql = "SELECT * FROM REMISES WHERE IDREMISE=" + idRemise;
                Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                if (remises != null && remises.length > 0) {
                    BigDecimal curIdRemise = remises[0].getIdremise();
                    session.setAttribute("currentRemiseCorr", remises[0]);
                    request.setAttribute("idObjet", curIdRemise);
                    String fPicture = "";
                    String rPicture = "";
                    if (remises[0].getPathimage() != null) {
                        fPicture = remises[0].getPathimage().substring(3) + "\\" + remises[0].getFichierimage() + "f.jpg";
                        rPicture = remises[0].getPathimage().substring(3) + "\\" + remises[0].getFichierimage() + "r.jpg";
                    }

                    request.setAttribute("fPicture", fPicture.replace("\\", "/"));
                    request.setAttribute("rPicture", rPicture.replace("\\", "/"));
                    request.setAttribute("montantRemise", remises[0].getMontant());
                    request.setAttribute("compteRemettant", remises[0].getCompteRemettant());
                    request.setAttribute("nomClient", remises[0].getNomClient());
                    request.setAttribute("agenceRemettant", remises[0].getAgenceRemettant());
                    request.setAttribute("reference", remises[0].getReference());
                    request.setAttribute("remarques", remises[0].getRemarques());
                    sql = "SELECT * FROM CHEQUES WHERE REMISE=" + curIdRemise;
                    Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                    if (cheques != null && cheques.length > 0) {
                        nmourem = String.valueOf(cheques.length);
                    }
                    request.setAttribute("nmourem", nmourem);
                }

            } else {
                boolean finish = false;
                String sql = "SELECT * FROM REMISES WHERE ETAT IN (" + Utility.getParam("CETAOPEANO") + "," + Utility.getParam("CETAOPECOR") +
                        ") AND AGENCEDEPOT='" + user.getAdresse().trim() + "'" +
                        " AND NOMUTILISATEUR LIKE '" + user.getLogin().trim() +
                        "' ORDER BY ETAT DESC, IDREMISE ASC";
                Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                BigDecimal lastIdSeq = null, lastId = null;
                if (remises != null && remises.length > 0) {
                    if (remises[0].getEtat().equals(new BigDecimal(Utility.getParam("CETAOPEANO")))) {
                        lastDocType = "REMISE";
                        lastIdSeq = remises[0].getSequence();
                       
                        lastId = remises[0].getIdremise();
                        request.setAttribute("typeObjet", lastDocType);
                        request.setAttribute("sequenceObjet", lastIdSeq);
                    } else {
                        sql = "SELECT * FROM CHEQUES WHERE IDCHEQUE = (SELECT MIN(IDCHEQUE) FROM CHEQUES WHERE REMISE=" + remises[0].getIdremise() + " AND ETAT IN (" + Utility.getParam("CETAOPEANO") + "))";
                        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                        if (cheques != null && cheques.length > 0) {
                            lastDocType = "CHEQUE";
                            lastIdSeq = cheques[0].getSequence();
                            lastId = cheques[0].getIdcheque();
                            request.setAttribute("typeObjet", lastDocType);
                            request.setAttribute("sequenceObjet", lastIdSeq);

                        } else {
                            lastDocType = "FIN";
                        }
                    }
                    if (lastDocType.equalsIgnoreCase("REMISE")) {

                        if (remises != null && remises.length > 0) {
                            BigDecimal curIdRemise = remises[0].getIdremise();
                            session.setAttribute("currentRemiseCorr", remises[0]);
                            request.setAttribute("idObjet", curIdRemise);
                            String fPicture = "";
                            String rPicture = "";
                            if (remises[0].getPathimage() != null) {
                                fPicture = remises[0].getPathimage().substring(3) + "\\" + remises[0].getFichierimage() + "f.jpg";
                                rPicture = remises[0].getPathimage().substring(3) + "\\" + remises[0].getFichierimage() + "r.jpg";
                            }

                            request.setAttribute("fPicture", fPicture.replace("\\", "/"));
                            request.setAttribute("rPicture", rPicture.replace("\\", "/"));
                            request.setAttribute("montantRemise", remises[0].getMontant());
                            request.setAttribute("compteRemettant", remises[0].getCompteRemettant());
                            request.setAttribute("nomClient", remises[0].getNomClient());
                            request.setAttribute("agenceRemettant", remises[0].getAgenceRemettant());
                            request.setAttribute("reference", remises[0].getReference());
                            request.setAttribute("remarques", remises[0].getRemarques());
                            sql = "SELECT * FROM CHEQUES WHERE REMISE=" + curIdRemise + " ORDER BY SEQUENCE";
                            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                            if (cheques != null && cheques.length > 0) {
                                nmourem = String.valueOf(cheques.length);
                                String lastChqSeq = cheques[cheques.length - 1].getSequence().toPlainString();

                            }
                            request.setAttribute("nmourem", nmourem);
                        }
                    } else {

                        sql = "SELECT * FROM CHEQUES WHERE IDCHEQUE=" + lastId + " AND ETAT =" + Utility.getParam("CETAOPEANO") + "  AND SEQUENCE=" + lastIdSeq;
                        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                        if (cheques != null && cheques.length > 0) {
                            Banques[] banques = (Banques[]) db.retrieveRowAsObject("SELECT * FROM BANQUES WHERE CODEBANQUE=" + ((cheques[0].getBanque() == null) ? "''" : "'" + cheques[0].getBanque() + "'"), new Banques());
                            if (banques != null && banques.length > 0) {
                                request.setAttribute("libelleBanque", banques[0].getLibellebanque());
                            }
                            BigDecimal curIdCheque = cheques[0].getIdcheque();
                            request.setAttribute("idObjet", curIdCheque);
                            session.setAttribute("currentChequeCorr", cheques[0]);
                            request.setAttribute("montantCheque", cheques[0].getMontantcheque());
                            request.setAttribute("codeBanque", (cheques[0].getBanque() == null) ? "" : cheques[0].getBanque().contains("_") ? "" : cheques[0].getBanque());
                            request.setAttribute("codeAgence", (cheques[0].getAgence() == null) ? "" : cheques[0].getAgence().contains("_") ? "" : cheques[0].getAgence());
                            request.setAttribute("numeroCompte", (cheques[0].getNumerocompte() == null) ? "" : cheques[0].getNumerocompte().contains("_") ? "" : cheques[0].getNumerocompte());
                            request.setAttribute("clerib", (cheques[0].getRibcompte() == null) ? "" : cheques[0].getRibcompte().contains("_") ? "" : cheques[0].getRibcompte());
                            request.setAttribute("numeroCheque", (cheques[0].getNumerocheque() == null) ? "" : cheques[0].getNumerocheque().contains("_") ? "" : cheques[0].getNumerocheque());
                            request.setAttribute("idremise", cheques[0].getRemise());

                            if (remises != null && remises.length > 0) {
                                session.setAttribute("currentRemiseCorr", remises[0]);
                                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[0].getRemise() + " ORDER BY SEQUENCE";
                                Cheques[] chequesCal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                                for (int i = 0; i < chequesCal.length; i++) {
                                    if (chequesCal[i].getIdcheque().equals(cheques[0].getIdcheque())) {
                                        index = i + 1;
                                    }
                                }

                                if (remises[0].getNbOperation() != null) {
                                    nbOpers = remises[0].getNbOperation().intValue();
                                    request.setAttribute("nbOpers", nbOpers);
                                }

                                request.setAttribute("index", index);


                            }

                            String fPicture = cheques[0].getPathimage().substring(3) + "\\" + cheques[0].getFichierimage() + "f.jpg";
                            String rPicture = cheques[0].getPathimage().substring(3) + "\\" + cheques[0].getFichierimage() + "r.jpg";
                            request.setAttribute("fPicture", fPicture.replace("\\", "/"));
                            request.setAttribute("rPicture", rPicture.replace("\\", "/"));
                        }
                    }

                } else {
                    out.println("Plus rien à saisir");
                    lastDocType = "";
                }
            }

        } else {
            out.println("Saisie désactivée par Administrateur|AUTORISATION_SAISIE = " + params_auto);
            lastDocType = "";
        }
        db.close();

        %>
        <%if (!lastDocType.equals("")) {%>

        <div align="center">
            <form name='outilsForm'>
                <input type="button" value="Supprimer" name="deleteDocBtn" onclick="  if (confirm('Etes vous sur?')) {
                    document['outilsForm'].action.value ='deleteDocCorBtn';doAjaxSubmitWithResult(document['outilsForm'],undefined,'ControlServlet');}"/>
                <input type="hidden" name="idObjet" value="${requestScope['idObjet']}"/>
                <input type="hidden" name="typeObjet" value="${requestScope['typeObjet']}"/>
                
                <input type="hidden" name="action" value="dynamique"/>
            </form>
            <% MessageBean message = (MessageBean) request.getAttribute("message");
    if (message != null && message.getMessage() != null) {
        out.print(message.getMessage());
    }%>
        </div>
        <br>

        <div id="imageDIV" >
            <form name='imgForm'>
                <img name='imgchqr' id='imgchqr' src='${requestScope['fPicture']}' width="570" height="280" alt='|Aucune image recto associee|' />

                <img name='imgchqv' id='imgchqv' src='${requestScope['rPicture']}' width="570" height="280" alt='|Aucune image verso associee|' />

            </form>

        </div>
        <br><br>
        <%}%>
        <% if (lastDocType.equalsIgnoreCase("REMISE")) {%>

        <div id="tableSaisieRemiseDIV" align="center">
            <jsp:useBean id="comboCompteBean" scope="session" class="clearing.web.bean.ComboCompteTPCIBean" />

            <form  id="remiseForm" name="remiseForm" onsubmit=" return false;">
                <table>

                    <tbody>

                        <tr>
                            <td>Numéro de Compte: </td>

                            <%  if (request.getAttribute("compteRemettant") == null) {%>
                            <td><input type="text" name="numero" value="" maxlength="12" onKeypress="return numeriqueValide(event);" onblur=" chargeInfoCompteTPCI();" /></td>
                            <% } else {%>
                            <td><input type="text" name="numero" value="${requestScope['compteRemettant']}" maxlength="12" onKeypress="return numeriqueValide(event);" onblur=" chargeInfoCompteTPCI();" /></td>
                            <%}
     ;%>
                            <td></td>
                            <td></td>
                            <td>Montant: </td>
                            <%  if (request.getAttribute("montantRemise") == null) {%>
                            <!--<td><input type="text" name="montantRemise" value="" onKeypress="return(currencyFormat(this,' ',event));" /></td>-->
                            <td><input type="text" name="montantRemise" value="" onKeypress="return(currencyFormat(this,' ',event));" /></td>
                            <% } else {%>
                            <td><input type="text" name="montantRemise" value="${requestScope['montantRemise']}" onKeypress="return numeriqueValide(event);"/></td>
                            <%}
     ;%>

                            <td></td>
                        </tr>
                        <tr>

                            <td>Nom: </td>
                            <%  if (request.getAttribute("nomClient") == null) {%>
                            <td><input type="text" name="nom" value="" size="25" disabled /></td>
                            <% } else {%>
                            <td><input type="text" name="nom" value="${requestScope['nomClient']}" size="25" disabled /></td>
                            <%}%>
                            <td></td>
                            <td></td>
                            <td>Nbre d'instruments:</td>
                            <td><input type="text" name="nmourem" value="${requestScope['nmourem']}" disabled/></td>
                            <td>
                            </td>
                        </tr>
                        <tr>
                            <td>Agence: </td>
                            <%  if (request.getAttribute("agenceRemettant") == null) {%>
                            <td> <input type="text" name="agence" value=""  disabled/></td>
                            <% } else {%>
                            <td> <input type="text" name="agence" value="${requestScope['agenceRemettant']}"  disabled/></td>
                            <%}%>
                            <td></td>
                            <td></td>
                            <td>Reference: </td>
                            <%  if (request.getAttribute("reference") == null) {%>
                            <td><input type="text" name="reference" value="" maxlength="7" /></td>
                            <% } else {%>
                            <td><input type="text" name="reference" value="${requestScope['reference']}" maxlength="7" /></td>
                            <%}%>
                            <td></td>
                            <td><input type="submit" name="valider" value="Valider" onclick="checkRemise();" /></td>
                            <a:widget name="yahoo.tooltip" args="{context:['remiseForm']}" value="Saisie de Remise"  />
                        </tr>
                    </tbody>
                </table>
                <p align="center"><input type="text" name="message" value="${requestScope['remarques']}" size="77"  style="text-align:center;color:red;font-size:14pt" disabled/></p>
                <p align="center"><input type="text" name="messageStatut" value="" size="77"  style="text-align:center;color:red;font-size:14pt" disabled/></p>

                <input type="hidden" value="corrigeRemise" name="action" />
                <input type="hidden" value="" name="escompte" />

                
            </form>


        </div>

        <%}
        if (lastDocType.equalsIgnoreCase("CHEQUE")) {%>
        <div id="tableSaisieChequeDIV" align="center">

            <form  id="chequeForm" name="chequeForm" onsubmit="return false;">
                <table>

                    <tbody>
                        <tr>
                            <td>Montant: </td>
                            <%  if (request.getAttribute("montantCheque") == null) {%>
                            <td><input type="text" name="montantCheque" value="" onKeypress="return(currencyFormat(this,' ',event));" tabindex="1"/></td>
                            <% } else {%>
                            <td><input type="text" name="montantCheque" value="${requestScope['montantCheque']}" onKeypress="return numeriqueValide(event);" tabindex="1"/></td>
                            <%}
            ;%>

                            <td></td>
                            <td></td>
                            <td>Remise: </td>
                            <td><input type="button" name="idremise" value="${requestScope['idremise']}" onclick="location.replace('correctionremises_tpci.jsp?action=remise&idremise=${requestScope["idremise"]}')"/></td>
                            <td></td>
                        </tr>

                        <tr>
                            <td>Banque: </td>

                            <td><input type="text" name="libelleBanque" value="${requestScope['libelleBanque']}" disabled/></td>
                            <td><input type="text" name="codeBanque" value="${requestScope['codeBanque']}" maxlength="5" size="5" onblur=" chargeLibelleBanque();" tabindex="2"/></td>
                            <td></td>
                            <td>Agence:</td>
                            <td><input type="text" name="codeAgence" value="${requestScope['codeAgence']}" maxlength="5" onKeypress="return numeriqueValide(event);" tabindex="3"/></td>
                            <td>
                            </td>
                        </tr>
                        <tr>
                            <td>Numero de Compte: </td>
                            <td><input type="text" name="numeroCompte"  value="${requestScope['numeroCompte']}" maxlength="12" onKeypress="return numeriqueValide(event);" tabindex="4"/></td>
                            <td><input type="text" name="clerib" size="2" value="${requestScope['clerib']}"  maxlength="2" tabindex="5"/></td>
                            <td></td>

                            <td>Numero de Cheque:</td>
                            <td><input type="text" name="numeroCheque"  value="${requestScope['numeroCheque']}" maxlength="7" onKeypress="return numeriqueValide(event);" tabindex="6"/></td>
                            <td></td>
                            <td><input type="submit" name="valider" value="Valider" onclick="checkCheque();" tabindex="7"/></td>

                            <a:widget name="yahoo.tooltip" args="{context:['chequeForm']}" value="Saisie de Chèques"  />



                        </tr>
                    </tbody>
                </table>

                <input type="hidden" value="corrigeCheque" name="action" />
                <input type="hidden" value="${requestScope['index']}" name="index" />
            </form>
            <b>Cheque ${requestScope['index']} sur ${requestScope['nbOpers']}</b>
            <br>
            <%if (index > 1) {%>
            <input type="button" value="Precedent" name="Précédent" onclick="window.location.replace('correctionremises_tpci.jsp?action=precedent&idcheque=${requestScope['idObjet']}&idremise=${requestScope['idremise']}')"/>
            <%}%>
            <%if (nbOpers > index) {%>
            <input type="button" value="Suivant" name="Suivant" onclick="window.location.replace('correctionremises_tpci.jsp?action=suivant&idcheque=${requestScope['idObjet']}&idremise=${requestScope['idremise']}')"/>
            <%}%>
        </div>
        <%}%>


        <script>

        function zoomImgr(e){

            document.getElementById('imgchqr').width = document.getElementById('imgchqr').width + 30;
            document.getElementById('imgchqr').height = document.getElementById('imgchqr').height + 15;
        }
        function zoomImgv(e){

            document.getElementById('imgchqv').width = document.getElementById('imgchqr').width + 30;
            document.getElementById('imgchqv').height = document.getElementById('imgchqr').height + 15;
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
        document.getElementById('imgchqr').onmousedown = deZoomImgr;
        document.getElementById('imgchqr').onclick=zoomImgr;
        document.getElementById('imgchqr').onmouseover=changeCursorToZoom;
        document.getElementById('imgchqr').onmouseout=changeCursorToDefault;
        document.getElementById('imgchqv').onmousedown = deZoomImgv;
        document.getElementById('imgchqv').onclick=zoomImgv;
        document.getElementById('imgchqv').onmouseover=changeCursorToZoom;
        document.getElementById('imgchqv').onmouseout=changeCursorToDefault;
        </script>
    </body>
</html>
