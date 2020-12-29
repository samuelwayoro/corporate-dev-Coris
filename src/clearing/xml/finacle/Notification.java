/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.xml.finacle;

import clearing.table.Prelevements;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.patware.utils.Utility;

/**
 *
 * @author alex
 */
@XmlRootElement(name = "C24TRANREQ")
@XmlType(name = "", propOrder = {
    "trainId",
    "tnxDate",
    "instamnt",
    "processingCode",
    "txnCcy",
    "countryCode",
    "instDate",
    "accountnmbr",
    "accountnmbrCr",
    "narration",
    "fee"
})
public class Notification {

    private String trainId;
    private String accountnmbr;
    private String accountnmbrCr; //numero de compte Ã  créditer
    private String tnxDate;
    private String instDate;
//    private String shadowBalcode;
    private String instamnt;
    private String txnCcy;
    // private String instnmbr;
//    private String drwBank;
//    private String drwBranch;
//    private String remitterSortCode;
//    private String subCode;
    private String narration;
    private String countryCode;
    private String processingCode;
    private Notification.Fee []fee;

    public Notification(String trainId, String accountnmbr, String tnxDate, String instDate, String shadowBalcode, String instamnt, String txnCcy, String instnmbr, String drwBank, String drwBranch, String remitterSortCode, String subCode, String narration) {
        this.trainId = trainId;
        this.accountnmbr = accountnmbr;
        this.instDate = instDate;
        //    this.shadowBalcode = shadowBalcode;
        this.instamnt = instamnt;
        this.txnCcy = txnCcy;
//        this.instnmbr = instnmbr;
//        this.drwBank = drwBank;
//        this.drwBranch = drwBranch;
//        this.remitterSortCode = remitterSortCode;
//        this.subCode = subCode;
        this.narration = narration;
    }

    public Notification(Prelevements prelevements) {
        // instnmbr=prelevements.getNumeroprelevement();
        tnxDate = Utility.convertDateToString(Utility.convertStringToDate(prelevements.getDatecompensation() + prelevements.getHeuretraitement(), "yyyy/MM/ddHH:mm:ss"), "yyyyMMddHHmmss");
        instDate = Utility.convertDateToString(Utility.convertStringToDate(prelevements.getDatecompensation(), "yyyy/MM/dd"), "yyyyMMdd");
        // shadowBalcode="2";
        instamnt = prelevements.getMontantprelevement();
        txnCcy = prelevements.getDevise();
        trainId = prelevements.getIdprelevement().toPlainString();
        narration = prelevements.getLibelle();
        countryCode = Utility.getParam("COUNTRY_CODE");
        processingCode = Utility.getParam("PROCESSING_CODE");
        accountnmbr = prelevements.getNumerocompte_Tire();
        accountnmbrCr = Utility.getParam("COMCREPREL");
//       fee=new Fee("1");
//       fee.setAccountnmbr(accountnmbr);
//       fee.setAccountnmbrCr(accountnmbrCr);
//       fee.setAmount("0");

        // drwBank=prelevements.getB
    }

    public Notification() {
    }

    @XmlElement(name = "STAN")
    public String getTrainId() {
        return trainId;
    }

    public void setTrainId(String trainId) {
        this.trainId = trainId;
    }

    @XmlElement(name = "DR_ACCT_NUM")
    public String getAccountnmbr() {
        return accountnmbr;
    }

    public void setAccountnmbr(String accountnmbr) {
        this.accountnmbr = accountnmbr;
    }

    @XmlElement(name = "TRAN_DATE_TIME")
    public String getTnxDate() {
        return tnxDate;
    }

    public void setTnxDate(String tnxDate) {
        this.tnxDate = tnxDate;
    }

    @XmlElement(name = "VALUE_DATE")
    public String getInstDate() {
        return instDate;
    }

    public void setInstDate(String instDate) {
        this.instDate = instDate;
    }

//    @XmlElement(name = "SHADOWBALCODE")    
//    public String getShadowBalcode() {
//        return shadowBalcode;
//    }
//
//    public void setShadowBalcode(String shadowBalcode) {
//        this.shadowBalcode = shadowBalcode;
//    }
    @XmlElement(name = "TRAN_AMT")
    public String getInstamnt() {
        return instamnt;
    }

    public void setInstamnt(String instamnt) {
        this.instamnt = instamnt;
    }

    @XmlElement(name = "TRAN_CRNCY_CODE")
    public String getTxnCcy() {
        return txnCcy;
    }

    public void setTxnCcy(String txnCcy) {
        this.txnCcy = txnCcy;
    }

//    @XmlElement(name = "INSTNMBR")
//    public String getInstnmbr() {
//        return instnmbr;
//    }
//
//    public void setInstnmbr(String instnmbr) {
//        this.instnmbr = instnmbr;
//    }
//    @XmlElement(name = "DRWBANK")
//    public String getDrwBank() {
//        return drwBank;
//    }
//
//    public void setDrwBank(String drwBank) {
//        this.drwBank = drwBank;
//    }
//
//    @XmlElement(name = "DRWBRANCH")
//    public String getDrwBranch() {
//        return drwBranch;
//    }
//
//    public void setDrwBranch(String drwBranch) {
//        this.drwBranch = drwBranch;
//    }
//
//    @XmlElement(name = "REMITTERSORTCODE")
//    public String getRemitterSortCode() {
//        return remitterSortCode;
//    }
//
//    public void setRemitterSortCode(String remitterSortCode) {
//        this.remitterSortCode = remitterSortCode;
//    }
//
//    @XmlElement(name = "SUBCODE")
//    public String getSubCode() {
//        return subCode;
//    }
//
//    public void setSubCode(String subCode) {
//        this.subCode = subCode;
//    }
    @XmlElement(name = "RESERVED_FLD_1")
    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    @XmlElement(name = "CR_ACCT_NUM")
    public String getAccountnmbrCr() {
        return accountnmbrCr;
    }

    public void setAccountnmbrCr(String accountnmbrCr) {
        this.accountnmbrCr = accountnmbrCr;
    }

    @XmlElement(name = "COUNTRY_CODE")
    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @XmlElement(name = "PROCESSING_CODE")
    public String getProcessingCode() {
        return processingCode;
    }

    public void setProcessingCode(String processingCode) {
        this.processingCode = processingCode;
    }

      @XmlElement(name = "FEE")
    public Notification.Fee[] getFee() {
        return fee;
    }

    public void setFee(Notification.Fee []fee) {
        this.fee = fee;
    }
    public static class Fee {

        private String id;
        private String accountnmbr;
        private String accountnmbrCr; //numero de compte Ã  créditer
        private String amount;

        public Fee(String id) {
            this.id = id;
        }

        public Fee() {
        }

        @XmlElement(name = "DR_ACCT_NO")
        public String getAccountnmbr() {
            return accountnmbr;
        }

        public void setAccountnmbr(String accountnmbr) {
            this.accountnmbr = accountnmbr;
        }

        @XmlElement(name = "CR_ACCT_NO")
        public String getAccountnmbrCr() {
            return accountnmbrCr;
        }

        public void setAccountnmbrCr(String accountnmbrCr) {
            this.accountnmbrCr = accountnmbrCr;
        }

        @XmlElement(name = "AMOUNT")
        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        // @XmlTransient
        @XmlAttribute(name = "ID")
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

    }
}
