<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="clearing.table.Utilisateurs" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<%
            response.setHeader("Cache-Control", "no-cache"); //Forces caches to obtain a new copy of the page from the origin server
            response.setHeader("Cache-Control", "no-store"); //Directs caches not to store the page under any circumstance
            response.setDateHeader("Expires", 0); //Causes the proxy cache to see the page as "stale"
            response.setHeader("Pragma", "no-cache"); //HTTP 1.0 backward compatibility
            Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
            if (null == user) {
                request.setAttribute("Erreur", "Votre session est expirée.\n Veuillez vous reconnecter");
                RequestDispatcher rd = request.getRequestDispatcher("/ControlServlet?action=logoutForm");
                rd.forward(request, response);
            }  
 %>