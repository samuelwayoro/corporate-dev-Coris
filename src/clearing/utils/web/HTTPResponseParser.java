/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.utils.web;

/**
 *
 * @author alex
 */
public class HTTPResponseParser {
    
    private int responseCode;
    private String responseMessage;
    private String body;

    public HTTPResponseParser(int responseCode, String responseMessage, String body) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.body = body;
    }
            
            

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public String getBody() {
        return body;
    }
            
            
    
}
