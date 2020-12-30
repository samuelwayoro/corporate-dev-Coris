/*
 * ControlServlet.java
 *
 * Created on 20 fevrier 2006, 01:07
 */
package clearing.web.controller;

import clearing.table.Utilisateurs;
import clearing.web.bean.ComboIdBean;

import clearing.web.bean.RejetFormBean;
import java.io.*;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.web.bean.LogonFormBean;

/**
 *
 * @author Administrateur
 * @version
 */
public class ControlServlet extends HttpServlet {

    private SiteController siteCtrl;

    // private HttpServletRequest request;
    //private HttpServletResponse response;
    /**
     * Initializes the servlet.
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        siteCtrl = new SiteController(config);

    }

    /**
     * Destroys the servlet.
     */
    @Override
    public void destroy() {
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String ACTION = request.getParameter("ACTION");
        UserController userController = new UserController();

        // System.out.println("Action = " + action);
        HttpSession session = request.getSession();
        if (action != null) {

            if (!action.equalsIgnoreCase("refreshInfoPanel") && !action.equalsIgnoreCase("logonform")) {
                userController.audit(request);
            }

            if (action.equalsIgnoreCase("logonform")) {
                session = request.getSession();
                LogonFormBean logonForm = createLogonFormBean(request);
                userController.setLogonForm(logonForm);

                if (userController.logon(request) != null) {
                    session.setAttribute("utilisateur", userController.getUtilisateur());
                    session.setAttribute("tableModel", userController.getTableModel());
                    request.setAttribute("utilisateur", userController.getUtilisateur());
                    request.setAttribute("message", userController.getMessageBean());
                    request.setAttribute("menuModel", userController.getMenuModel());
                    userController.audit(request);
                    if (userController.getUtilisateur().getEtat() != null && userController.getUtilisateur().getEtat().equals(new BigDecimal(Utility.getParam("CETAUTIPASMOD")))) {
                        // forwardTo(request, response, "/changePassword.jsp");
                        forwardTo(request, response, "/" + ResLoader.getMessages("changePassword"));
                    } else {
                        request.setAttribute("message", userController.getMessageBean());
                        userController.activateUser();
                        forwardTo(request, response, "/index.jsp");
                    }
                } else {
                    request.setAttribute("message", userController.getMessageBean());
                    forwardTo(request, response, "/login.jsp");
                }
            } else if (action.equalsIgnoreCase("logoutform")) {
                userController.logout(request);

                session.removeAttribute("utilisateur");
                session.removeAttribute("message");
                session.removeAttribute("menuModel");
                session.removeAttribute("tableModel");
                session.invalidate();
                forwardTo(request, response, "/login.jsp");

            } else if (action.equalsIgnoreCase("closeWindow")) {
                userController.logout(request);

                session.removeAttribute("utilisateur");
                session.removeAttribute("message");
                session.removeAttribute("menuModel");
                session.removeAttribute("tableModel");
                session.invalidate();

            } else if (action.equalsIgnoreCase("checkICOM")) {

                String parameter = request.getParameter("message");

                String jsonresult = siteCtrl.checkICOM(parameter);
                System.out.println("jsondata = " + jsonresult);
                WriteJSONResponse(response, jsonresult);

            } else if ((action.equalsIgnoreCase("readProfils"))) {
                String jsonresult = siteCtrl.getTableAsExtJSONString("Profils");
                System.out.println("jsondata = " + jsonresult);
                WriteJSONResponse(response, jsonresult);
            } else if ((action.equalsIgnoreCase("loadTree"))) {
                String groupmenu = request.getParameter("menugroup") != null ? request.getParameter("menugroup") : " ";
                String recordid = request.getParameter("recordid") != null ? request.getParameter("recordid") : "0";

                String jsonresult = siteCtrl.getJSONLoadtreemenu(recordid);
                System.out.println("JSONResponse = " + jsonresult);
                WriteJSONResponse(response, jsonresult);
            } else if ((action.equalsIgnoreCase("updateProfils"))) {
                String id = request.getParameter("node") != null ? request.getParameter("node") : " ";
                String code = request.getParameter("nomprofil") != null ? request.getParameter("nomprofil") : " ";
                String libelle = request.getParameter("libelleprofil") != null ? request.getParameter("libelleprofil") : " ";
                String options = request.getParameter("options") != null ? request.getParameter("options") : " ";

                String jsonresult = siteCtrl.updateProfile(code, libelle, id, options, request);

                WriteJSONResponse(response, jsonresult);
            } else if ((action.equalsIgnoreCase("createProfils"))) {
                String id = request.getParameter("node") != null ? request.getParameter("node") : "";
                String code = request.getParameter("nomprofil") != null ? request.getParameter("nomprofil") : " ";
                String libelle = request.getParameter("libelleprofil") != null ? request.getParameter("libelleprofil") : " ";
                String options = request.getParameter("options") != null ? request.getParameter("options") : " ";

                String jsonresult = siteCtrl.createProfile(id, libelle, code, options,request);

                WriteJSONResponse(response, jsonresult);
            } else if ((action.equalsIgnoreCase("deleteProfils"))) {
                String id = request.getParameter("node") != null ? request.getParameter("node") : " ";
                String code = request.getParameter("nomprofil") != null ? request.getParameter("nomprofil") : " ";
                String libelle = request.getParameter("libelleprofil") != null ? request.getParameter("libelleprofil") : " ";
                String options = request.getParameter("options") != null ? request.getParameter("options") : " ";

                String jsonresult = siteCtrl.deleteProfile(code, libelle, id, options);

                WriteJSONResponse(response, jsonresult);
            } else if (action.equalsIgnoreCase("inscription")) {

                Utilisateurs utilisateur = createUtilisateur(request);
                siteCtrl.inscrire(utilisateur);

                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("inscription_lite")) {

                Utilisateurs utilisateur = createUtilisateur(request);
                siteCtrl.inscrire(utilisateur);

                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("creationmenu")) {

                siteCtrl.createMenu(request.getParameter("tacheParent"), request.getParameter("tacheEnfant"));
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("creationtache")) {

                siteCtrl.createTache(request.getParameter("idTache"), request.getParameter("libelle"), request.getParameter("typetache"), new BigDecimal(request.getParameter("poids")), URLDecoder.decode(request.getParameter("url"), "UTF-8"));

                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("rapprochement")) {
                System.out.println("filtre =" + request.getParameter("filtre"));
                ComboIdBean comboIdBean = (ComboIdBean) session.getAttribute("comboIdBean");
                String url = "/rapprochement.jsp?id=" + comboIdBean.getMigrator().getCurrentId() + "&libelle=" + comboIdBean.getMigrator().getCurrentLibelle() + "&filtre=" + URLEncoder.encode(request.getParameter("filtre"), "UTF-8");
                forwardTo(request, response, url);

            } else if (action.equalsIgnoreCase("migration")) {
                System.out.println("filtre =" + request.getParameter("filtre"));
                ComboIdBean comboIdBean = (ComboIdBean) session.getAttribute("comboIdBean");
                PrintWriter writer = response.getWriter();
                writer.write("Migration en cours ...");
                try {
                    comboIdBean.getMigrator().doMigration();
                    // String url = "/migration.jsp?id=" + comboIdBean.getMigrator().getCurrentId() + "&libelle=" + comboIdBean.getMigrator().getCurrentLibelle() + "&filtre=" + URLEncoder.encode(request.getParameter("filtre"), "UTF-8");
                    // forwardTo(request, response, url);
                } catch (Exception ex) {
                    Logger.getLogger(ControlServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
                writer.write("Migration terminee ...");

            } else if (action.equalsIgnoreCase("idRapprochement")) {
                String message = request.getParameter("message");

                siteCtrl.doTableFiltres(session, message);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("tableFiltre")) {
                String message = request.getParameter("message");

                siteCtrl.doColonnes(session, message);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("listAgences")) {
                String message = request.getParameter("message");

                siteCtrl.doListAgences(session, message);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("tableSelect")) {
                String message = request.getParameter("message");

                siteCtrl.getColonnes(session, message);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("update")) {

                siteCtrl.doUpdate(request.getParameter("query"));
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("choixNumeroCompte")) {
                String message = request.getParameter("message");

                siteCtrl.recupereInfoCompte(session, message);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("choixCompteFromSibICCPT")) {
                String message = request.getParameter("message");

                siteCtrl.recupereInfoCompteFromSibICCPT(session, message);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("choixNumeroCompteLite")) {
                String message = request.getParameter("message");

                siteCtrl.recupereInfoCompteLite(session, message);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("choixNumeroComptePF")) {
                String message = request.getParameter("message");

                siteCtrl.recupereInfoComptePF(session, message);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("choixCodeBanque")) {
                String message = request.getParameter("message");

                siteCtrl.recupereCodeBanque(session, message);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("choixNumeroCompteCNCE")) {
                String message = request.getParameter("message");

                siteCtrl.recupereInfoCompteCNCE(session, message);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("choixNumeroCompteTPCI")) {
                String message = request.getParameter("message");

                siteCtrl.recupereInfoCompteTPCI(session, message);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("choixNumeroCheque")) {
                String message = request.getParameter("message");

                siteCtrl.recupereInfoVignette(session, message);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("choixNumeroCompteNSIA")) {
                String message = request.getParameter("message");

                siteCtrl.recupereInfoCompteNSIA(session, message);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("choixNumeroCompteCBAO")) {
                String message = request.getParameter("message");

                siteCtrl.recupereInfoCompteCBAO(session, message);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("choixNumeroCompteBDK")) {
                String message = request.getParameter("message");

                siteCtrl.recupereInfoCompteBDK(session, message);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("choixNumeroCompteECI")) {
                String message = request.getParameter("message");

                siteCtrl.recupereInfoCompteECI(session, message);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("agenceCompteECI")) {
                String message = request.getParameter("message");

                siteCtrl.recupereAgenceCompteECI(session, message);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("agenceCompte")) {
                String message = request.getParameter("message");

                siteCtrl.recupereAgenceCompte(session, message);
                writeResponse(request, response);

            }else if (action.equalsIgnoreCase("agenceCompteCORIS")) {
                String message = request.getParameter("message");

                siteCtrl.recupereAgenceCompteCORIS(session, message);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("agenceCompteNSIA")) {
                String message = request.getParameter("message");

                siteCtrl.recupereAgenceCompteNSIA(session, message);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("verificationCompteCNCE")) {
                String agence = request.getParameter("agence");
                String numeroCompte = request.getParameter("numeroCompte");

                siteCtrl.verificationCompte(session, agence, numeroCompte);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("verificationCompteNSIA")) {
                String agence = request.getParameter("agence");
                String numeroCompte = request.getParameter("numeroCompte");

                //recupereInfoCompteNSIA
                siteCtrl.verificationCompteNSIA(session, agence, numeroCompte);
                writeResponse(request, response);
//
            } else if (action.equalsIgnoreCase("verificationCompteECI")) {
                String agence = request.getParameter("agence");
                String numeroCompte = request.getParameter("numeroCompte");

                siteCtrl.verificationCompteECI(session, agence, numeroCompte);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("verificationCompteETG")) {
                String agence = request.getParameter("agence");
                String numeroCompte = request.getParameter("numeroCompte");

                siteCtrl.verificationCompteETG(session, agence, numeroCompte);
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("refreshInfoPanel")) {

                siteCtrl.refreshInfoPanel();
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("startBtn")) {

                siteCtrl.startWebMonitor();
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("stopBtn")) {
                siteCtrl.stopWebMonitor();
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("rejetform")) {
                // siteCtrl.rejeteObjet(createRejetFormBean(request));
                siteCtrl.rejeteObjet(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("supprime")) {

                siteCtrl.supprimeObjet(createRejetFormBean(request));
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("annule")) {
                siteCtrl.annuleObjet(createRejetFormBean(request));
                writeResponse(request, response);

            } else if (action.equalsIgnoreCase("rapport")) {
                siteCtrl.showRapport(request, response);
            } else if (action.equalsIgnoreCase("rapportWebLite")) {
                siteCtrl.showRapportWebLite(request, response);
            } else if (action.equalsIgnoreCase("rapportSyntheses")) {
                siteCtrl.showRapportSyntheses(request, response);
            } else if (action.equalsIgnoreCase("rapportActivite")) {
                siteCtrl.showRapportActivite(request, response);
            } else if (action.equalsIgnoreCase("rapportChequesLite")) {
                siteCtrl.showRapportChequesLite(request, response);
            } else if (action.equalsIgnoreCase("corrigeRemise")) {
                siteCtrl.corrigeRemise(request, response);
                writeResponse(request, response);
            } 
            
            //same
            //page de correction des effets 
            else if (action.equalsIgnoreCase("corrigeRemiseEffets")) {
                siteCtrl.corrigeRemiseEffets(request, response);
                writeResponse(request, response);
            }
            
            else if (action.equalsIgnoreCase("corrigeRemiseUBAGuinee")) {
                siteCtrl.corrigeRemiseUBAGuinee(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("valideRemiseLite")) {
                siteCtrl.valideRemiseLite(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("valideRemiseLiteRNC")) {
                siteCtrl.valideRemiseLiteRNC(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("valideRemiseLiteCorporates")) {
                siteCtrl.valideRemiseLiteCorporates(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("valideRemiseSignatureETG")) {
                siteCtrl.valideRemiseCtrlSignETG(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("valideRemiseSignatureECI")) {
                siteCtrl.valideRemiseCtrlSignECI(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("valideChequeLite")) {
                siteCtrl.valideChequeLite(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("valideChequeLiteRNC")) {
                siteCtrl.valideChequeLiteRNC(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("valideChequeLiteRC")) {
                siteCtrl.valideChequeLiteRC(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("valideChequeLiteCorporate")) {
                siteCtrl.valideChequeLiteCorporate(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("valideChequeSignature")) {
                siteCtrl.valideChequeSignature(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("valideChequeSignatureETG")) {
                siteCtrl.valideChequeSignatureETG(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("valideChequeSignatureECI")) {
                siteCtrl.valideChequeSignatureECI(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("corrigeCheque")) {
                siteCtrl.corrigeCheque(request, response);
                writeResponse(request, response);
            } 
            //page Effet correction 
            else if (action.equalsIgnoreCase("corrigeEffet")) {
                siteCtrl.corrigeEffet(request, response);
                writeResponse(request, response);
            }
            
            
            
            
            else if (action.equalsIgnoreCase("corrigeChequeUBAGuinee")) {
                siteCtrl.corrigeChequeUBAGuinee(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("corrigeChequeLite")) {
                siteCtrl.corrigeChequeLite(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("checkRemise")) {
                siteCtrl.checkRemise(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("checkRemiseUBAGuinee")) {
                siteCtrl.checkRemiseUBAGuinee(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("checkRemiseVierge")) {
                siteCtrl.checkRemiseVierge(request, response);
                writeResponse(request, response);
                
            }else if (action.equalsIgnoreCase("checkRemiseViergeEffets")) {
                siteCtrl.checkRemiseViergeEffets(request, response);
                writeResponse(request, response);
  
            }  //effets avec type 
             else if (action.equalsIgnoreCase("checkRemiseViergeEffetsType")) {
                siteCtrl.checkRemiseViergeEffetsType(request, response);
                writeResponse(request, response);
  
            } 
            
            else if (action.equalsIgnoreCase("checkRemiseViergeUBAGuinee")) {
                siteCtrl.checkRemiseViergeUBAGuinee(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("checkRemiseViergeMSSQL")) {
                siteCtrl.checkRemiseViergeMSSQL(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("checkRemiseTresor")) {
                siteCtrl.checkRemiseTresor(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("checkCheque")) {
                siteCtrl.checkCheque(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("checkChequeUBAGuinee")) {
                siteCtrl.checkChequeUBAGuinee(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("checkChequeVierge")) {
                siteCtrl.checkChequeVierge(request, response);
                writeResponse(request, response);
            }
            else if (action.equalsIgnoreCase("checkEffetVierge")) {
                siteCtrl.checkEffetVierge(request, response);
                writeResponse(request, response);
            }
            
            else if (action.equalsIgnoreCase("checkChequeViergeUBAGuinee")) {
                siteCtrl.checkChequeViergeUBAGuinee(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("checkChequeUV")) {
                siteCtrl.checkChequeUV(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("checkChequeTresor")) {
                siteCtrl.checkChequeTresor(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("deleteDocBtn")) {
                siteCtrl.deleteDocument(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("deleteDocCorBtn")) {
                siteCtrl.deleteDocumentCorrige(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("rejectDocBtn")) {
                siteCtrl.rejectDocumentLite(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("rejectDocBtnRNC")) {
                siteCtrl.rejectDocumentLiteRNC(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("rejectDocCorpBtn")) {
                siteCtrl.rejectDocumentLiteCorp(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("rejectDocBtnETG")) {
                siteCtrl.rejectDocumentETG(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("rejectDocBtnECI")) {
                siteCtrl.rejectDocumentECI(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("deleteDocCheBtn")) {
                siteCtrl.deleteDocumentCheque(request, response);
                writeResponse(request, response);
            } 
            //same 
            else if (action.equalsIgnoreCase("deleteDocEffBtn")) {
                siteCtrl.deleteDocumentEffet(request, response);
                writeResponse(request, response);
            }
            
            else if (action.equalsIgnoreCase("transformBtn")) {
                siteCtrl.transFormDocument(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("refreshSignature")) {
                siteCtrl.refreshSignature(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("changePassword")) {

                userController.changePassword(request, response);
                writeResponse(request, response, userController);

            } else if (action.equalsIgnoreCase("changePasswordMSSQL")) {

                userController.changePasswordMSSQL(request, response);
                writeResponse(request, response, userController);

            } else if (action.equalsIgnoreCase("changePasswordAdmin")) {

                siteCtrl.changePasswordAdmin(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("creationCompteClient")) {
                siteCtrl.createCompteClient(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("creationCompteClientF12")) {
                siteCtrl.createCompteClientF12(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("creationCompteClientNSIA")) {
                siteCtrl.createCompteClientNSIA(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("creationCompteClientACP")) {
                siteCtrl.createCompteClientACP(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("creationVirementClient")) {
                siteCtrl.createVirementClient(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("creationVirementClientBNP")) {
                siteCtrl.creationVirementClientBNP(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("creationVirementClientACP")) {
                siteCtrl.createVirementClientACP(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("creationVirementBanque")) {
                siteCtrl.createVirementBanque(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("creationVirementDisposition")) {
                siteCtrl.createVirementDisposition(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("creationEffetClient")) {
                siteCtrl.createEffetClient(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("creationPrelevementClient")) {
                siteCtrl.createPrelevementClient(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("creationAgence")) {
                siteCtrl.createAgence(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("creationBanque")) {
                siteCtrl.createBanque(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("creationRepertoire")) {
                siteCtrl.createRepertoire(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("creationMacuti")) {
                siteCtrl.createMacuti(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("creationMachine")) {
                siteCtrl.createMachine(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("creationEtablissement")) {
                siteCtrl.createEtablissement(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("creationProfils")) {
                siteCtrl.createProfils(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("creationParams")) {
                siteCtrl.createParams(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("nettoyage")) {
                siteCtrl.nettoyageScanner(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("archivage")) {
                siteCtrl.archiveBaseCompense(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("resetParams")) {
                siteCtrl.resetParamsCache(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("updateDataTable")) {
                siteCtrl.updateDataTableDev(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("updateDataTableUbaGuinee")) {
                siteCtrl.updateDataTableDevUbaGuinee(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("byPasserRemise")) {
                siteCtrl.byPasserRemise(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("choixUtilisateur")) {
                siteCtrl.startValidation(request, response);
                writeResponse(request, response);
            }
               //same
              else if (action.equalsIgnoreCase("choixUtilisateurEffet")) {
                siteCtrl.startValidationEffet(request, response);
                writeResponse(request, response);
            }
            
            else if (action.equalsIgnoreCase("choixAgenceDepot")) {
                siteCtrl.startValidationAgence(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("choixEtablissement")) {
                siteCtrl.startValidationEtablissement(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("choixFiltre")) {
                siteCtrl.filterDataTable(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("choixCodeBanques")) {
                siteCtrl.startValidationParBanque(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("choixCodeAgences")) {
                siteCtrl.startValidationParAgence(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("checkVignette")) {
                siteCtrl.verifierVignettes(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("confirmVignette")) {
                siteCtrl.confirmerVignettes(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("validerSignCheque")) {
                siteCtrl.validerSignCheque(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("rejeterSignCheque")) {
                siteCtrl.rejeterSignCheque(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("validerImageCheque")) {
                siteCtrl.validerImageCheque(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("passerSignCheque")) {
                siteCtrl.passerSignCheque(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("STARTROBOTTASK")) {
                siteCtrl.startRobotTask(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("STOPROBOTTASK")) {
                siteCtrl.stopRobotTask(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("VALIDEREMISE")) {
                siteCtrl.valideRemise(request, response);
                writeResponse(request, response);
            } else if (action.equalsIgnoreCase("REJETEREMISE")) {
                siteCtrl.rejeteRemise(request, response);
                writeResponse(request, response);
            }
            //SAME  
              else if (action.equalsIgnoreCase("updateDataTableDevEffet")) {
                siteCtrl.updateDataTableDevEffet(request, response);
                writeResponse(request, response);
            }
              else {
                if (!askForConfirmation(request, response)) {
                    forwardTo(request, response, "/confirmation.jsp?tache=" + request.getParameter("action") + "&description=" + request.getParameter("description") + "&paramDate1=" + request.getParameter("paramDate1") + "&paramDate2=" + request.getParameter("paramDate2") + "&paramDate3=" + request.getParameter("paramDate3"));
                } else {
                    Utility.clearParamsCache();
                    siteCtrl.makeAction(action, (HashMap) request.getParameterMap(), request);
                    writeResponse(request, response);

                }
                //siteCtrl.makeAction(action);
                //writeResponse(request, response);
            }
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Control  Servlet";
    }

    public Utilisateurs createUtilisateur(HttpServletRequest request) {

        String nom = request.getParameter("nom");
        String poids = request.getParameter("poids");
        String prenom = request.getParameter("prenom");
        String adresse = request.getParameter("agence");
        String courriel = request.getParameter("courriel");
        String login = request.getParameter("login");
        String password = request.getParameter("password1");
        Utilisateurs utilisateur = new Utilisateurs();
        utilisateur.setNom(nom);
        utilisateur.setPoids(new BigDecimal(poids));
        utilisateur.setPrenom(prenom);
        utilisateur.setAdresse(adresse);
        utilisateur.setCourriel(courriel);
        utilisateur.setLogin(login);
        utilisateur.setPassword(password);
        Utilisateurs user = (Utilisateurs) request.getSession().getAttribute("utilisateur");
        utilisateur.setUcreation(user.getLogin().trim());
        utilisateur.setUmodification(user.getLogin().trim());
        utilisateur.setDcreation(Utility.convertDateToString(new Date(), ResLoader.getMessages("patternDate")));
        utilisateur.setDmodification(Utility.convertDateToString(new Date(), ResLoader.getMessages("patternDate")));
        return utilisateur;
    }

    public LogonFormBean createLogonFormBean(HttpServletRequest request) {
        String login = request.getParameter("login");
        String password = request.getParameter("password");
        return new LogonFormBean(login, password);
    }

    public RejetFormBean createRejetFormBean(HttpServletRequest request) {
        return new RejetFormBean(request.getParameter("objet"), request.getParameter("nomidobjet"), request.getParameter("idobjet"), request.getParameter("motifrejet"));
    }

    private void forwardTo(HttpServletRequest request, HttpServletResponse response, String url) throws ServletException, IOException {

        RequestDispatcher rd = getServletContext().getRequestDispatcher(url);
        rd.forward(request, response);
    }

    private void writeResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setAttribute("message", siteCtrl.getMessageBean());
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        // System.out.println("CtrlSrvlt = " + siteCtrl.getMessageBean().getMessage());
        writer.write(siteCtrl.getMessageBean().getMessage());
        siteCtrl.getMessageBean().setMessage("");

    }

    private void writeResponse(HttpServletRequest request, HttpServletResponse response, UserController userCtrl) throws IOException {
        request.setAttribute("message", userCtrl.getMessageBean());
        PrintWriter writer = response.getWriter();
        // System.out.println("CtrlSrvlt = " + siteCtrl.getMessageBean().getMessage());
        writer.write(userCtrl.getMessageBean().getMessage());
        userCtrl.getMessageBean().setMessage("");

    }

    private boolean askForConfirmation(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        //PrintWriter writer = response.getWriter();
        String validation = request.getParameter("validate");

        if (validation == null) {
            return false;
        } else {
            return true;
        }

    }

    private void WriteJSONResponse(HttpServletResponse response, String htmlsrc)
            throws IOException {
        response.setContentType("application/json"); // response.setContentType("application/json");

        PrintWriter out = response.getWriter();
        try {
            out.print(htmlsrc);
        } finally {
            out.close();
        }
    }

    private void writeTextResponse(HttpServletResponse response, String htmlsrc)
            throws IOException {
        response.setContentType("application/txt"); // response.setContentType("application/json");

        PrintWriter out = response.getWriter();
        try {
            out.print(htmlsrc);
        } finally {
            out.close();
        }
    }
}
