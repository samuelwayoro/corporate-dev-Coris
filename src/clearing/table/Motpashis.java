/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.table;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 *
 * @author AUGOU Patrick
 */
public class Motpashis {

    private BigDecimal idMotpashis;
    private String login;
    private String password;
    private Timestamp datecreation;
    private Timestamp datechange;

    public Timestamp getDatechange() {
        return datechange;
    }

    public void setDatechange(Timestamp datechange) {
        this.datechange = datechange;
    }

    public Timestamp getDatecreation() {
        return datecreation;
    }

    public void setDatecreation(Timestamp datecreation) {
        this.datecreation = datecreation;
    }

    public BigDecimal getIdMotpashis() {
        return idMotpashis;
    }

    public void setIdMotpashis(BigDecimal idMotpashis) {
        this.idMotpashis = idMotpashis;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



}
