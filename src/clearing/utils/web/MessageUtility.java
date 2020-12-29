/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.utils.web;

/**
 *
 * @author SOCITECH-
 */
public class MessageUtility {
    
    private static String message;

    public static String getMessage() {
        return message;
    }

    public static void setMessage(String message) {
        MessageUtility.message = message;
    }
    
    public static String createTempFormToSendParameters(String merchantId,String montant,String sessionId,String purchaseRef,String token){
        return null;
    }
   // parametres="merchantid=test_merchant_id&amount="+montant+"&sessionid="+operation.getIdOperation().toString()+"&purchaseref=ACHAT&token="+operation.getToken();
    
    
}
