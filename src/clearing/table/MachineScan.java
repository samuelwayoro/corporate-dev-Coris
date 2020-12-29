/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.table;

import java.sql.Date;

/**
 *
 * @author Patrick
 */
public class MachineScan {

  private String id_machinescan;
  private String machinescan;
  private String codeAgence;
  private String agence;
  private String adr_ip;
  private String adr_ip_2;
  private String marque;
  private String modele;
  private String contact;
  private Date dateinstall;

    public MachineScan() {
    }

    public String getAgence() {
        return agence;
    }

    public void setAgence(String agence) {
        this.agence = agence;
    }

    public Date getDateinstall() {
        return dateinstall;
    }

    public void setDateinstall(Date dateinstall) {
        this.dateinstall = dateinstall;
    }

    public String getId_machinescan() {
        return id_machinescan;
    }

    public void setId_machinescan(String id_machinescan) {
        this.id_machinescan = id_machinescan;
    }

    public String getMachinescan() {
        return machinescan;
    }

    public void setMachinescan(String machinescan) {
        this.machinescan = machinescan;
    }

   

    public String getAdr_ip() {
        return adr_ip;
    }

    public void setAdr_ip(String adr_ip) {
        this.adr_ip = adr_ip;
    }

    public String getAdr_ip_2() {
        return adr_ip_2;
    }

    public void setAdr_ip_2(String adr_ip_2) {
        this.adr_ip_2 = adr_ip_2;
    }

    public String getCodeAgence() {
        return codeAgence;
    }

    public void setCodeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

  


    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }



}
