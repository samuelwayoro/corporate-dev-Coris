/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.xml.finacle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author alex
 */
@XmlRootElement(name = "C24TRANRES")
public class NotificationResponse {
    private String availableBalance;
    private String ledgerBalance;
    private String balanceCurrency;
    private String actionCode;
    private String stan;
    private String tranDate;
    private String countryCode;

    public NotificationResponse() {
    }
    
    
    @XmlElement(name = "AVAILABLE_BALANCE")
    public String getAvailableBalance() {
        return availableBalance;
    }

       
    public void setAvailableBalance(String availableBalance) {
        this.availableBalance = availableBalance;
    }

    @XmlElement(name = "LEDGER_BALANCE") 
    public String getLedgerBalance() {
        return ledgerBalance;
    }

    public void setLedgerBalance(String ledgerBalance) {
        this.ledgerBalance = ledgerBalance;
    }

    @XmlElement(name = "BALANCE_CURRENCY")
    public String getBalanceCurrency() {
        return balanceCurrency;
    }

    public void setBalanceCurrency(String balanceCurrency) {
        this.balanceCurrency = balanceCurrency;
    }

    @XmlElement(name = "ACTION_CODE")
    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    @XmlElement(name = "STAN")
    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    @XmlElement(name = "TRAN_DATE_TIME")
    public String getTranDate() {
        return tranDate;
    }

    public void setTranDate(String tranDate) {
        this.tranDate = tranDate;
    }

    @XmlElement(name = "COUNTRY_CODE")
    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    
    
}
