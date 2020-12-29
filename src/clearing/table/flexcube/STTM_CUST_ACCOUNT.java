/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.table.flexcube;

import java.math.BigDecimal;

/**
 *
 * @author Patrick Augou
 */
public class STTM_CUST_ACCOUNT {

    private String cust_no;
    private String cust_ac_no;
    private String ac_stat_stop_pay;
    private String ac_stat_no_dr;
    private BigDecimal lcy_curr_balance;
    private String account_class;

    public STTM_CUST_ACCOUNT() {
    }

    public String getCust_no() {
        return cust_no;
    }

    public void setCust_no(String cust_no) {
        this.cust_no = cust_no;
    }

    public String getAc_stat_no_dr() {
        return ac_stat_no_dr;
    }

    public void setAc_stat_no_dr(String ac_stat_no_dr) {
        this.ac_stat_no_dr = ac_stat_no_dr;
    }

    public String getAc_stat_stop_pay() {
        return ac_stat_stop_pay;
    }

    public void setAc_stat_stop_pay(String ac_stat_stop_pay) {
        this.ac_stat_stop_pay = ac_stat_stop_pay;
    }

    public String getCust_ac_no() {
        return cust_ac_no;
    }

    public void setCust_ac_no(String cust_ac_no) {
        this.cust_ac_no = cust_ac_no;
    }

    public BigDecimal getLcy_curr_balance() {
        return lcy_curr_balance;
    }

    public void setLcy_curr_balance(BigDecimal lcy_curr_balance) {
        this.lcy_curr_balance = lcy_curr_balance;
    }

    public String getAccount_class() {
        return account_class;
    }

    public void setAccount_class(String account_class) {
        this.account_class = account_class;
    }

}
