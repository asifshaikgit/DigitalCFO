package model;
/**
 *@author Sunil
 *@since 25.11.2017
 */
public class AgeingReport {
    private String vendCustHead;
    private String recInvHead;
    private String crdtDbtTxnHead;
    private String recPaidHead;
    private String paymentDateHead;
    private String commissionHead;
    private String totalAmtHead;
    private String payMethodHead;

    private  String invoice;
    private  String vendCust;
    private  String branch;
    private  String txnRef;
    private  String accDate;
    private  Integer creditPeriod;
    private  Double netAmount;
    private Double crdtDbtTxnAmt;
    private  Double amtReceived;
    private  Double amountDue;
    private  Long daysO_s;
    private  Double days0_30;
    private  Double days31_60;
    private  Double days61_90;
    private  Double days91_180;
    private  Double over180Days;
    private  Double overdue;
    private  Double totalAmount;
    private  Double totalRec;
    private  Double totalDue;
    private  Double total0_30;
    private  Double total31_60;
    private  Double total61_90;
    private  Double total91_180;
    private  Double totalOver180;
    private  Double totalOverdue;
    private  String poReference;
    private String txnPurpose;

    public String getVendCustHead() {
        return this.vendCustHead;
    }

    public void setVendCustHead(String vendCustHead) {
        this.vendCustHead = vendCustHead;
    }

    public String getRecInvHead() {
        return this.recInvHead;
    }

    public void setRecInvHead(String recInvHead) {
        this.recInvHead = recInvHead;
    }

    public String getInvoice() {
        return this.invoice;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
    }

    public String getVendCust() {
        return this.vendCust;
    }

    public void setVendCust(String vendCust) {
        this.vendCust = vendCust;
    }

    public String getBranch() {
        return this.branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getTxnRef() {
        return this.txnRef;
    }

    public void setTxnRef(String txnRef) {
        this.txnRef = txnRef;
    }

    public String getAccDate() {
        return this.accDate;
    }

    public void setAccDate(String accDate) {
        this.accDate = accDate;
    }

    public Integer getCreditPeriod() {
        return this.creditPeriod;
    }

    public void setCreditPeriod(Integer creditPeriod) {
        this.creditPeriod = creditPeriod;
    }

    public Double getNetAmount() {
        return this.netAmount;
    }

    public void setNetAmount(Double netAmount) {
        this.netAmount = netAmount;
    }

    public Double getAmtReceived() {
        return this.amtReceived;
    }

    public void setAmtReceived(Double amtReceived) {
        this.amtReceived = amtReceived;
    }

    public Double getAmountDue() {
        return this.amountDue;
    }

    public void setAmountDue(Double amountDue) {
        this.amountDue = amountDue;
    }

    public Long getDaysO_s() {
        return this.daysO_s;
    }

    public void setDaysO_s(Long daysO_s) {
        this.daysO_s = daysO_s;
    }

    public Double getDays0_30() {
        return this.days0_30;
    }

    public void setDays0_30(Double days0_30) {
        this.days0_30 = days0_30;
    }

    public Double getDays31_60() {
        return this.days31_60;
    }

    public void setDays31_60(Double days31_60) {
        this.days31_60 = days31_60;
    }

    public Double getDays61_90() {
        return this.days61_90;
    }

    public void setDays61_90(Double days61_90) {
        this.days61_90 = days61_90;
    }

    public Double getDays91_180() {
        return this.days91_180;
    }

    public void setDays91_180(Double days91_180) {
        this.days91_180 = days91_180;
    }

    public Double getOver180Days() {
        return this.over180Days;
    }

    public void setOver180Days(Double over180Days) {
        this.over180Days = over180Days;
    }

    public Double getOverdue() {
        return this.overdue;
    }

    public void setOverdue(Double overdue) {
        this.overdue = overdue;
    }

    public Double getTotalAmount() {
        return this.totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getTotalRec() {
        return this.totalRec;
    }

    public void setTotalRec(Double totalRec) {
        this.totalRec = totalRec;
    }

    public Double getTotalDue() {
        return this.totalDue;
    }

    public void setTotalDue(Double totalDue) {
        this.totalDue = totalDue;
    }

    public Double getTotal0_30() {
        return this.total0_30;
    }

    public void setTotal0_30(Double total0_30) {
        this.total0_30 = total0_30;
    }

    public Double getTotal31_60() {
        return this.total31_60;
    }

    public void setTotal31_60(Double total31_60) {
        this.total31_60 = total31_60;
    }

    public Double getTotal61_90() {
        return this.total61_90;
    }

    public void setTotal61_90(Double total61_90) {
        this.total61_90 = total61_90;
    }

    public Double getTotal91_180() {
        return this.total91_180;
    }

    public void setTotal91_180(Double total91_180) {
        this.total91_180 = total91_180;
    }

    public Double getTotalOver180() {
        return this.totalOver180;
    }

    public void setTotalOver180(Double totalOver180) {
        this.totalOver180 = totalOver180;
    }

    public Double getTotalOverdue() {
        return this.totalOverdue;
    }

    public void setTotalOverdue(Double totalOverdue) {
        this.totalOverdue = totalOverdue;
    }

    public String getRecPaidHead() {
        return this.recPaidHead;
    }

    public void setRecPaidHead(String recPaidHead) {
        this.recPaidHead = recPaidHead;
    }

    public String getPoReference() {
        return this.poReference;
    }

    public void setPoReference(final String poReference) {
        this.poReference = poReference;
    }

    public String getPaymentDateHead() {
        return this.paymentDateHead;
    }

    public void setPaymentDateHead(final String paymentDateHead) {
        this.paymentDateHead = paymentDateHead;
    }

    public String getCommissionHead() {
        return this.commissionHead;
    }

    public void setCommissionHead(final String commissionHead) {
        this.commissionHead = commissionHead;
    }

    public String getTotalAmtHead() {
        return this.totalAmtHead;
    }

    public void setTotalAmtHead(final String totalAmtHead) {
        this.totalAmtHead = totalAmtHead;
    }

    public String getPayMethodHead() {
        return this.payMethodHead;
    }

    public void setPayMethodHead(final String payMethodHead) {
        this.payMethodHead = payMethodHead;
    }

    public String getCrdtDbtTxnHead() {
        return this.crdtDbtTxnHead;
    }

    public void setCrdtDbtTxnHead(final String crdtDbtTxnHead) {
        this.crdtDbtTxnHead = crdtDbtTxnHead;
    }

    public Double getCrdtDbtTxnAmt() {
        return this.crdtDbtTxnAmt;
    }

    public void setCrdtDbtTxnAmt(final Double crdtDbtTxnAmt) {
        this.crdtDbtTxnAmt = crdtDbtTxnAmt;
    }

    public String getTxnPurpose() {
        return this.txnPurpose;
    }

    public void setTxnPurpose(final String txnPurpose) {
        this.txnPurpose = txnPurpose;
    }
}
