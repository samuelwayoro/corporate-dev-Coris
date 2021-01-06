
<%@page import="org.patware.web.bean.MessageBean,org.patware.utils.VersionFileReader" contentType="text/html"%>
<%@page contentType="text/html" import="org.patware.jdbc.DataBase,java.math.BigDecimal,clearing.table.Utilisateurs"%>
<%@page pageEncoding="UTF-8" import="org.patware.xml.JDBCXmlReader,org.patware.web.bean.MessageBean,org.patware.utils.Utility"%>
<%@page import="org.patware.xml.JDBCXmlReader,clearing.table.Sequences,clearing.table.Remises,clearing.table.Sequences,clearing.table.Cheques,clearing.table.Banques"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>



<body >

    <a:widget name="dojo.clock" args="{clockType:'grayPlastic'}"/>

    <%if (VersionFileReader.getVersion() != null) {
            out.print("Version " + VersionFileReader.getVersion());
        }
        out.print("<hr>");
        if (session != null) {
            DataBase db = new DataBase(JDBCXmlReader.getDriver());

            Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
            if (user != null) {
                db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
                String sql = "SELECT * FROM REMISES WHERE ETAT IN (" + Utility.getParam("CETAOPEANO") + "," + Utility.getParam("CETAOPECOR")
                        + ") AND AGENCEDEPOT='" + user.getAdresse().trim() + "'"
                        + " AND NOMUTILISATEUR LIKE '" + user.getLogin().trim()
                        + "' ORDER BY ETAT DESC, IDREMISE ASC";
                Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());

                if (remises != null && remises.length > 0) {
                    out.print("<BR><B><h1>Vous avez " + remises.length + " remise(s) à corriger</h1><B>");
                }
                sql = "SELECT * FROM SEQUENCES_CHEQUES WHERE (MACHINESCAN IN (SELECT MACHINE FROM MACUTI WHERE UTILISATEUR = '" + user.getLogin().trim() + "')) AND (UTILISATEUR='' OR UTILISATEUR IS NULL OR UTILISATEUR = '" + user.getLogin().trim() + "') ORDER BY MACHINESCAN,IDSEQUENCE";
                Sequences[] sequences = (Sequences[]) db.retrieveRowAsObject(sql, new Sequences());

                if (sequences != null && sequences.length > 0) {
                    out.print("<BR><B><h1>Vous avez " + sequences.length + " chèque(s) scanné(s) sans bordereaux à saisir<h1><B>");
                }

                sql = "SELECT * FROM SEQUENCES WHERE (MACHINESCAN IN (SELECT MACHINE FROM MACUTI WHERE UTILISATEUR = '" + user.getLogin().trim() + "')) AND (UTILISATEUR='' OR UTILISATEUR IS NULL OR UTILISATEUR = '" + user.getLogin().trim() + "') ORDER BY MACHINESCAN,IDSEQUENCE ";
                sequences = (Sequences[]) db.retrieveRowAsObject(sql, new Sequences());

                if (sequences != null && sequences.length > 0) {
                    out.print("<BR><B><h1>Vous avez " + sequences.length + " chèque(s) scanné(s) avec bordereaux à saisir</h1><B>");
                }
                
                //affichage de nombre de cheques a valider par le valideur corporate
                /*sql = "SELECT * FROM REMISES WHERE ETAT=" + Utility.getParam("CETAOPEVAL")
                        + " AND AGENCEDEPOT='" + user.getAdresse().trim() + "'"
                        + " AND (VALIDEUR='' OR VALIDEUR IS NULL OR VALIDEUR = '" + user.getLogin().trim() + "')";
                */
                sql = "SELECT * FROM REMISES WHERE ETAT=" + Utility.getParam("CETAOPEVALITE")
                        + " AND ETABLISSEMENT= '" + user.getAdresse().trim() + "'"
                        + " AND (VALIDEUR='' OR VALIDEUR IS NULL OR VALIDEUR = '" + user.getLogin().trim() + "')";
                remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                if (remises != null && remises.length > 0) {
                    out.print("<BR><B><h1>Il reste " + remises.length + " remise(s) disponible(s) à la validation</h1><B>");
                }
                
                //affichage de nombre de cheques a valider par le valideur banque 
                sql = "SELECT * FROM REMISES WHERE ETAT ="+Utility.getParam("CETAOPEVAL") 
                        +"AND ETABLISSEMENT != '"+user.getAdresse().trim() + "'"
                        + " AND (VALIDEUR='' OR VALIDEUR IS NULL OR  VALIDEUR IN (select trim(login) from utilisateurs  where poids=4))";
                remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                if (remises != null && remises.length > 0) {
                    out.print("<BR><B><h1>Il reste " + remises.length + " remise(s) disponible(s) à la validation</h1><B>");
                }
                
                db.close();
            }

        }

    %>


</body>
