<%@page contentType="text/html" import="org.patware.jdbc.DataBase,java.math.BigDecimal,clearing.table.Utilisateurs"%>
<%@page pageEncoding="UTF-8" import="org.patware.xml.JDBCXmlReader,org.patware.web.bean.MessageBean,org.patware.utils.Utility"%>
<%@page import="org.patware.xml.JDBCXmlReader,clearing.table.Sequences,clearing.table.RemisesEffets,clearing.table.Sequences,clearing.table.Effets,clearing.table.Banques"%>
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

                String idObjet = request.getParameter("ideffet");
                String idRemise = request.getParameter("idremise");
                lastDocType = "EFFET";
                request.setAttribute("typeObjet", lastDocType);
                request.setAttribute("sequenceObjet", "0");
                long idEffet = 0;
                String sql = "";
                if (action.equalsIgnoreCase("precedent")) {
                    
                      sql = "SELECT * FROM EFFETS WHERE REMISE=" + idRemise + " AND IDEFFET<" + idObjet + " ORDER BY IDEFFET ASC";

                } else {
                    
                      sql = "SELECT * FROM EFFETS WHERE REMISE=" + idRemise + " AND IDEFFET>" + idObjet + " ORDER BY IDEFFET DESC";

                }
                Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                if (effets != null && effets.length > 0) {
                    idEffet = effets[effets.length - 1].getIdeffet().longValue();
                }
                action = null;
                sql = "SELECT * FROM EFFETS WHERE IDEFFET=" + idEffet;
                effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                if (effets != null && effets.length > 0) {
                    BigDecimal curIdEffet = effets[0].getIdeffet();
                    request.setAttribute("idObjet", curIdEffet);
                    session.setAttribute("currentEffetsCorr", effets[0]);
                    request.setAttribute("montantEffet", effets[0].getMontant_Effet());
                    request.setAttribute("codeBanque", (effets[0].getBanque() == null) ? "" : effets[0].getBanque().contains("_") ? "" : effets[0].getBanque());
                    request.setAttribute("codeAgence", (effets[0].getAgence() == null) ? "" : effets[0].getAgence().contains("_") ? "" : effets[0].getAgence());
                    request.setAttribute("numeroCompte", (effets[0].getNumerocompte_Tire() == null) ? "" : effets[0].getNumerocompte_Tire().contains("_") ? "" : effets[0].getNumerocompte_Tire() );
                    request.setAttribute("clerib", (effets[0].getRibcompte() == null) ? "" : effets[0].getRibcompte().contains("_") ? "" : effets[0].getRibcompte());
                    request.setAttribute("numeroEffet", (effets[0].getNumeroeffet() == null) ? "" : effets[0].getNumeroeffet().contains("_") ? "" : effets[0].getNumeroeffet());
                    request.setAttribute("idremise", effets[0].getRemise());
                    sql = "SELECT * FROM REMISESEFFETS WHERE IDREMISE=" + effets[0].getRemise();
                    RemisesEffets[] remises = (RemisesEffets[]) db.retrieveRowAsObject(sql, new RemisesEffets());
                    if (remises != null && remises.length > 0) {
                        session.setAttribute("currentRemiseCorr", remises[0]);
                      
                         sql = "SELECT * FROM EFFETS WHERE REMISE=" + effets[0].getRemise() + " ORDER BY SEQUENCE";
                         
                        Effets[] effetsCal = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                        for (int i = 0; i < effetsCal.length; i++) {
                            if (effetsCal[i].getIdeffet().equals(effets[0].getIdeffet())) {
                                index = i + 1;
                            }
                        }
                        nbOpers = remises[0].getNbOperation().intValue();
                        request.setAttribute("index", index);
                        request.setAttribute("nbOpers", nbOpers);

                    }

                    String fPicture = effets[0].getPathimage().substring(3) + "\\" + effets[0].getFichierimage() + "f.jpg";
                    String rPicture = effets[0].getPathimage().substring(3) + "\\" + effets[0].getFichierimage() + "r.jpg";
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

                String sql = "SELECT * FROM REMISESEFFETS WHERE IDREMISE=" + idRemise;
                RemisesEffets[] remises = (RemisesEffets[]) db.retrieveRowAsObject(sql, new RemisesEffets());
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
                    sql = "SELECT * FROM EFFETS WHERE REMISE=" + curIdRemise;
                    Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                    if (effets != null && effets.length > 0) {
                        nmourem = String.valueOf(effets.length);
                    }
                    request.setAttribute("nmourem", nmourem);
                }

            } else {
                boolean finish = false;
                String sql = "SELECT * FROM REMISESEFFETS WHERE ETAT IN (" + Utility.getParam("CETAOPEANO") + "," + Utility.getParam("CETAOPECOR") +
                        ") AND AGENCEDEPOT='" + user.getAdresse().trim() + "'" +
                        " AND NOMUTILISATEUR LIKE '" + user.getLogin().trim() +
                        "' ORDER BY ETAT DESC, IDREMISE ASC";
                RemisesEffets[] remises = (RemisesEffets[]) db.retrieveRowAsObject(sql, new RemisesEffets());
                BigDecimal lastIdSeq = null, lastId = null;
                if (remises != null && remises.length > 0) {
                    if (remises[0].getEtat().equals(new BigDecimal(Utility.getParam("CETAOPEANO")))) {
                        lastDocType = "REMISE";
                        lastIdSeq = remises[0].getSequence();
                       
                        lastId = remises[0].getIdremise();
                        request.setAttribute("typeObjet", lastDocType);
                        request.setAttribute("sequenceObjet", lastIdSeq);
                    } else {
                        sql = "SELECT * FROM EFFETS WHERE IDEFFET = (SELECT MIN(IDEFFET) FROM EFFETS WHERE REMISE=" + remises[0].getIdremise() + " AND ETAT IN (" + Utility.getParam("CETAOPEANO") + "))";
                        Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                        if (effets != null && effets.length > 0) {
                            lastDocType = "EFFETS";
                            lastIdSeq = effets[0].getSequence();
                            lastId = effets[0].getIdeffet();
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
                            sql = "SELECT * FROM EFFETS WHERE REMISE=" + curIdRemise + " ORDER BY SEQUENCE";
                            Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                            if (effets != null && effets.length > 0) {
                                nmourem = String.valueOf(effets.length);
                                String lastChqSeq = effets[effets.length - 1].getSequence().toPlainString();

                            }
                            request.setAttribute("nmourem", nmourem);
                        }
                    } else {

                        sql = "SELECT * FROM EFFETS WHERE IDEFFET=" + lastId + " AND ETAT =" + Utility.getParam("CETAOPEANO") + "  AND SEQUENCE=" + lastIdSeq;
                        Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                        if (effets != null && effets.length > 0) {
                            Banques[] banques = (Banques[]) db.retrieveRowAsObject("SELECT * FROM BANQUES WHERE CODEBANQUE=" + ((effets[0].getBanque() == null) ? "''" : "'" + effets[0].getBanque() + "'"), new Banques());
                            if (banques != null && banques.length > 0) {
                                request.setAttribute("libelleBanque", banques[0].getLibellebanque());
                            }
                            BigDecimal curIdEffet = effets[0].getIdeffet();
                            request.setAttribute("idObjet", curIdEffet);
                            session.setAttribute("currentEffetCorr", effets[0]);
                            request.setAttribute("montantEffet", effets[0].getMontant_Effet());
                            request.setAttribute("codeBanque", (effets[0].getBanque() == null) ? "" : effets[0].getBanque().contains("_") ? "" : effets[0].getBanque());
                            request.setAttribute("codeAgence", (effets[0].getAgence() == null) ? "" : effets[0].getAgence().contains("_") ? "" : effets[0].getAgence());
                            request.setAttribute("numeroCompte", (effets[0].getNumerocompte_Tire() == null) ? "" : effets[0].getNumerocompte_Tire().contains("_") ? "" : effets[0].getNumerocompte_Tire() );
                            request.setAttribute("clerib", (effets[0].getRibcompte() == null) ? "" : effets[0].getRibcompte().contains("_") ? "" : effets[0].getRibcompte());
                            request.setAttribute("numeroEffet", (effets[0].getNumeroeffet() == null) ? "" : effets[0].getNumeroeffet().contains("_") ? "" : effets[0].getNumeroeffet());

                            request.setAttribute("idremise", effets[0].getRemise());

                            if (remises != null && remises.length > 0) {
                                session.setAttribute("currentRemiseCorr", remises[0]);
                                sql = "SELECT * FROM EFFETS WHERE REMISE=" + effets[0].getRemise() + " ORDER BY SEQUENCE";
                                Effets[] effetsCal = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                                for (int i = 0; i < effetsCal.length; i++) {
                                    if (effetsCal[i].getIdeffet().equals(effets[0].getIdeffet())) {
                                        index = i + 1;
                                    }
                                }

                                if (remises[0].getNbOperation() != null) {
                                    nbOpers = remises[0].getNbOperation().intValue();
                                    request.setAttribute("nbOpers", nbOpers);
                                }

                                request.setAttribute("index", index);


                            }

                            String fPicture = effets[0].getPathimage().substring(3) + "\\" + effets[0].getFichierimage() + "f.jpg";
                            String rPicture = effets[0].getPathimage().substring(3) + "\\" + effets[0].getFichierimage() + "r.jpg";
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
                <input type="button" value="Supprimer" name="deleteDocEffBtn" onclick="  if (confirm('Etes vous sur?')) {
                    document['outilsForm'].action.value ='deleteDocCorEffBtn';doAjaxSubmitWithResult(document['outilsForm'],undefined,'ControlServlet');}"/>
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
            <jsp:useBean id="comboCompteBean" scope="session" class="clearing.web.bean.ComboCompteBean" />

            <form  id="remiseForm" name="remiseForm" onsubmit=" return false;">
                <table>

                    <tbody>
                          <tr>
                            <td>Compte Remettant*: </td>

                            <%  if (request.getAttribute("compteRemettant") == null) {%>
                            <td><input type="text" name="numero" value="" maxlength="12" onKeypress="return numeriqueValide(event);" onblur="chargeInfoCompte();" /></td>
                                <% } else {%>
                            <td><input type="text" name="numero" value="${requestScope['compteRemettant']}" maxlength="12" onKeypress="return numeriqueValide(event);" onblur=" chargeInfoCompte();" /></td>
                                <%}%>
                            <td></td>
                            <td></td>
                            <td>Montant Remise*: </td>
                            <%  if (request.getAttribute("montantRemise") == null) {%>
                            <td><input type="text" name="montantRemise" value="" onKeypress="return(currencyFormat(this, ' ', event));" /></td>
                                <% } else {%>
                            <td><input type="text" name="montantRemise" value="${requestScope['montantRemise']}" onKeypress="return numeriqueValide(event);"/></td>
                                <%}%>
                            <td></td>
                           
                            <td></td>
                        </tr>
                          <tr>
                            <td>Nom Client: </td>
                            <%  if (request.getAttribute("nomClient") == null) {%>
                            <td><input type="text" name="nom" value="" size="25" disabled /></td>
                                <% } else {%>
                            <td><input type="text" name="nom" value="${requestScope['nomClient']}" size="25" disabled /></td>
                                <%}%>
                            <td></td>
                            <td></td>
                            <td>Nbre d'effets*:</td>
                            <%  if (request.getAttribute("nmourem") == null) {%>
                            <td><input type="text" name="nmourem" size="3" value="" onKeypress="return numeriqueValide(event);"/></td>
                                <% } else {%>
                            <td><input type="text" name="nmourem" size="3" value="${requestScope['nmourem']}" onKeypress="return numeriqueValide(event);"/></td>
                                <%}%>
                            <td>
                            </td>
                        </tr>
                        <tr>
                            <td>Agence: </td>
                            <%  if (request.getAttribute("agenceRemettant") == null) {%>
                            <td><input type="text" name="agence" value=""  disabled/></td>
                                <% } else {%>
                            <td><input type="text" name="agence" value="${requestScope['agenceRemettant']}"  disabled/></td>
                                <%}%>
                            <td></td>
                            <td></td>
                            <td>Reference Effet*: </td>
                            <%  if (request.getAttribute("reference") == null) {%>
                            <td><input type="text" name="reference" value="" maxlength="12" /></td>
                                <% } else {%>
                            <td><input type="text" name="reference" value="${requestScope['reference']}" maxlength="12" /></td>
                                <%}%>
                            <td></td>
                            <td><input type="submit" name="valider" value="Valider" onclick="checkRemiseEffets();" /></td>

                        </tr>
                    </tbody>
                </table>
                <p align="center"><input type="text" name="message" value="${requestScope['remarques']}" size="77"  style="text-align:center;color:red;font-size:14pt" disabled/></p>
                <p align="center"><input type="text" name="messageStatut" value="" size="77"  style="text-align:center;color:red;font-size:14pt" disabled/></p>

                <input type="hidden" value="corrigeRemiseEffets" name="action" />
                <input type="hidden" value="" name="escompte" />

                
            </form>


        </div>

        <%}
        if (lastDocType.equalsIgnoreCase("EFFET")) {%>
        <div id="tableSaisieChequeDIV" align="center">

            <form  id="chequeForm" name="chequeForm" onsubmit="return false;">
                <table>

                    <tbody>
                        <tr>
                            <td>Montant Effet*: </td>
                            <%  if (request.getAttribute("montantEffet") == null) {%>
                            <td><input type="text" name="montantEffet" value="" onKeypress="return(currencyFormat(this, ' ', event));"/></td>
                                <% } else {%>
                            <td><input type="text" name="montantEffet" value="${requestScope['montantEffet']}" onKeypress="return numeriqueValide(event);"/></td>
                                <%}%>


                            <td></td>
                            <td></td>
                            <td>Remise: </td>
                            <td><input type="button" name="idremise" value="${requestScope['idremise']}" onclick="location.replace('correctionremises_effets.jsp?action=remiseeffets&idremise=${requestScope["idremise"]}')"/></td>
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
                            <td>Montant Frais: </td>
                            <td><input type=text name="montantfrais" value="0" onKeypress="return numeriqueValide(event);" /></td>
                            <td></td>
                            <td></td>
                            <td><b>Type Effet</b></td>
                            <td>
                                <input type="text" name="typeEffet"   value="${requestScope['typeEffet']}" disabled/>
                            </td>
                            <td></td>
                        </tr>
                        <tr>
                            <td>Date Echeance</td>
                            <td><a:widget name="dojo.dropdowndatepicker" id="dateecheance"/></td>
                            <td></td>
                            <td></td>
                            <td>Date Creation</td>
                            <td><a:widget name="dojo.dropdowndatepicker" id="datecreation"/></td>
                        </tr>
                        
                        
                        <tr>
                             <td>Numero de Compte*: </td>
                            <td><input type="text" name="numeroCompte"  value="${requestScope['numeroCompte']}" maxlength="12" onKeypress="return numeriqueValide(event);"/></td>
                            <td><input type="text" name="clerib" size="2" value="${requestScope['clerib']}"  maxlength="2"/></td>
                            <td></td>

                            <td>Numero de Effet*:</td>
                            <td><input type="text" name="numeroEffet"  value="${requestScope['numeroEffet']}" maxlength="7" onKeypress="return numeriqueValide(event);"/></td>
                            <td></td>

                            <td><input type="submit" name="valider" value="Valider" onclick="checkRemiseEffets();"/></td>

                            <a:widget name="yahoo.tooltip" args="{context:['chequeForm']}" value="Saisie des effets"  />

                        </tr>
                    </tbody>
                </table>

                <input type="hidden" value="corrigeEffets" name="action" />
                <input type="hidden" value="${requestScope['index']}" name="index" />
            </form>
            <b>Cheque ${requestScope['index']} sur ${requestScope['nbOpers']}</b>
            <br>
            <%if (index > 1) {%>
            <input type="button" value="Precedent" name="Précédent" onclick="window.location.replace('correctionremises_effets.jsp?action=precedent&ideffet=${requestScope['idObjet']}&idremise=${requestScope['idremise']}')"/>
            <%}%>
            <%if (nbOpers > index) {%>
            <input type="button" value="Suivant" name="Suivant" onclick="window.location.replace('correctionremises_effets.jsp?action=suivant&ideffet=${requestScope['idObjet']}&idremise=${requestScope['idremise']}')"/>
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
