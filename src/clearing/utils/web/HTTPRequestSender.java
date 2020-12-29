/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.utils.web;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.patware.utils.Utility;

/**
 *
 * @author alex
 */
public class HTTPRequestSender {

   // private static final String WS_URL = Utility.getParam("URL_UBACLEARING");
    private static final String LOGIN = Utility.getParam("LOGIN_UBACLEARING");
    private static final String PASSWORD = Utility.getParam("PASS_UBACLEARING");

    public static HTTPResponseParser sendPost(String url, String parametres) throws Exception {

        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        
        String authString = LOGIN + ":" + PASSWORD;
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = new String(authEncBytes);
        System.out.println("Base64 encoded auth string: " + authStringEnc);


        //add reuqest header
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Basic "+authStringEnc);
        //connection.setRequestProperty("Accept-Encoding", "gzip,deflate");
        connection.setRequestProperty("Accept-Encoding", "gzip,deflate");
        connection.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");

        // Send post request
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(parametres);
        wr.flush();
        wr.close();

        int responseCode = connection.getResponseCode();
        String responseMessage = connection.getResponseMessage();
        System.out.println("\nSending 'POST' request to URL : " + url);
      //  System.out.println("Post parameters : " + parametres);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer tmp = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            tmp.append(inputLine);
        }
        in.close();
        String response=tmp.toString().replaceAll("&lt;", "<").replaceAll("&gt;", ">");
        if(response.contains("<![CDATA[") && response.contains("]]>"))
            response=response.replace("<![CDATA[","").replace("]]>","");
       // response=
        //print result
      //  System.out.println(response);
        return new HTTPResponseParser(responseCode, responseMessage, response);

    }

}
