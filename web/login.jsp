<%@page import="org.patware.web.bean.MessageBean,org.patware.utils.VersionFileReader" contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %> 

<html>
    <head>
        <title>WebClearing&reg; - Login</title>
        <link href="login.css" rel="stylesheet" type="text/css" />
        <script>
            history.forward();
        </script>
        <script type="text/javascript"  src="clearing.js"></script>
    </head>
    
    <body onload="ouvrirPopup();document.loginForm.login.focus();clearInfoPanelLoadHandler()" >
          <br/>
       
       
         
            <br/>    <br/>    <br/>
            <br/>    <br/>    <br/>
            <br/>    <br/>    <br/>
            
        <div  id="loginDIV" align="center">
             
            <form name="loginForm" method=post action="ControlServlet" >
                
                <table border="0" cellspacing="2" cellpadding="2">
                    <thead>
                        <tr>
                            <th></th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><B><i>Login : </i></B></td>
                            <td><input type="text" name="login"/></td>
                        </tr>
                        <tr>
                            <td><B><i>Mot de Passe : </i></B></td>
                            <td><input type="password" name="password"/></td>
                        </tr>
                        <tr>
                            <td></td>
                            <td><input type="submit" value="Entrer"/></td>
                        </tr>
                    </tbody>
                </table>
                
                <input type=hidden name="action" value="logonform"/>
                
            </form>
            <br/>
          
        </div>
           
        <div align="center" style="color:red">
            <% MessageBean message = (MessageBean) request.getAttribute("message");
            
            if (message != null && message.getMessage() != null) {
                out.print(message.getMessage());
            }%>
        </div>
          <!-- <br/>  <br/>
          <marquee SCROLLAMOUNT="1" behavior="alternate" >
         <img src="images/fond.gif" alt="Des solutions à votre image"/>
        </marquee>//-->
        <div id="footer-legal">
            <%if (VersionFileReader.getVersion() != null) {
                out.print(VersionFileReader.getVersion());
            }%> &copy; 2018 SBS&reg; Inc 
        </div>
    </body>
</html>
