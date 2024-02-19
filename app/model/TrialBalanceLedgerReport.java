package model;

import java.util.Objects;

/**
 * Created by Sunil Namdev on 17-10-2016.
 */
public class TrialBalanceLedgerReport implements Comparable{
    private String ledgerId;
    private String ledgerName;
    private String txnRef = null;
    private String tranDate = null;
    private String branchName = null;
    private String projectName = null;
    private String custVendName;
    private String transactionPurpose;
    private String paymode;
    private String itemName;
    private String creditItemsName;
    private Double debit;
    private Double credit;
    private String remarks;
    private String particulars;
    private String email;
    private String invoiceDate;
    private String invoiceNo;
    private String grnDate;
    private String grnNo;
    private String impDate;
    private String impNo;
    private String taxRate;
    private String cessRate;
    private String typeOfSupply;
    private String typeOfSupplyNo;
    private String hsnSac;
    private String refNo;
    private String placeOfSupply;
    private String placeOfSupplyGstin;
    private String poRef;
    private String docs;
    private String debitProjectName;
    private String creditProjectName;
    private String withWithoutTax;
    private Double openingBalance;
    private Double creditAmount;
    private Double debitAmount;
    private Double closingBalance;
    @Override
    public String toString() {
        return "TrialBalanceLedgerReport{" +
                "txnRef='" + txnRef + '\'' +
                ", tranDate='" + tranDate + '\'' +
                ", branchName='" + branchName + '\'' +
                ", projectName='" + projectName + '\'' +
                ", custVendName='" + custVendName + '\'' +
                ", transactionPurpose='" + transactionPurpose + '\'' +
                ", paymode='" + paymode + '\'' +
                ", itemName='" + itemName + '\'' +
                ", creditItemsName='" + creditItemsName + '\'' +
                ", debit='" + debit + '\'' +
                ", credit='" + credit + '\'' +
                ", remarks='" + remarks + '\'' +
                ", particulars='" + particulars + '\'' +
                '}';
    }

    public String getLedgerId() {
        return this.ledgerId;
    }

    public void setLedgerId(final String ledgerId) {
        this.ledgerId = ledgerId;
    }

    public String getLedgerName() {
        return this.ledgerName;
    }

    public void setLedgerName(final String ledgerName) {
        this.ledgerName = ledgerName;
    }

    public String getCreditItemsName() {
        return this.creditItemsName;
    }

    public void setCreditItemsName(String creditItemsName) {
        this.creditItemsName = creditItemsName;
    }

     public String getParticulars() {
    	 if(txnRef != null && txnRef.startsWith("PRTXN"))
          	this.particulars = particulars;
         else if(txnRef != null && txnRef.startsWith("P"))
             this.particulars = "DEBIT: " + this.itemName + "\nCREDIT: " + this.creditItemsName;
         else
             this.particulars = this.itemName;
        return this.particulars;
    }

    public void setParticulars(String particulars) {
        this.particulars = particulars;
    }

    public String getTxnRef() {
        return this.txnRef;
    }

    public void setTxnRef(String txnRef) {
        this.txnRef = txnRef;
    }

    public String getTranDate() {
        return this.tranDate;
    }

    public void setTranDate(String tranDate) {
        this.tranDate = tranDate;
    }

    public String getBranchName() {
        return this.branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getProjectName() {
        return this.projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCustVendName() {
        return this.custVendName;
    }

    public void setCustVendName(String custVendName) {
        this.custVendName = custVendName;
    }

    public String getTransactionPurpose() {
        return this.transactionPurpose;
    }

    public void setTransactionPurpose(String transactionPurpose) {
        this.transactionPurpose = transactionPurpose;
    }

    public String getPaymode() {
        return this.paymode;
    }

    public void setPaymode(String paymode) {
        this.paymode = paymode;
    }

    public String getItemName() {
        return this.itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Double getDebit() {
        return this.debit;
    }

    public void setDebit(Double debit) {
        this.debit = debit;
    }

    public Double getCredit() {
        return this.credit;
    }

    public void setCredit(Double credit) {
        this.credit = credit;
    }

    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTypeOfSupply() {
        return this.typeOfSupply;
    }

    public void setTypeOfSupply(String typeOfSupply) {
        this.typeOfSupply = typeOfSupply;
    }

    public String getTypeOfSupplyNo() {
        return this.typeOfSupplyNo;
    }

    public void setTypeOfSupplyNo(String typeOfSupplyNo) {
        this.typeOfSupplyNo = typeOfSupplyNo;
    }

    public String getInvoiceNo() {
        return this.invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getGrnDate() {
        return this.grnDate;
    }

    public void setGrnDate(String grnDate) {
        this.grnDate = grnDate;
    }

    public String getGrnNo() {
        return this.grnNo;
    }

    public void setGrnNo(String grnNo) {
        this.grnNo = grnNo;
    }

    public String getImpDate() {
        return this.impDate;
    }

    public void setImpDate(String impDate) {
        this.impDate = impDate;
    }

    public String getImpNo() {
        return this.impNo;
    }

    public void setImpNo(String impNo) {
        this.impNo = impNo;
    }

    public String getTaxRate() {
        return this.taxRate;
    }

    public void setTaxRate(String taxRate) {
        this.taxRate = taxRate;
    }

    public String getCessRate() {
        return this.cessRate;
    }

    public void setCessRate(String cessRate) {
        this.cessRate = cessRate;
    }

    public String getHsnSac() {
        return this.hsnSac;
    }

    public void setHsnSac(String hsnSac) {
        this.hsnSac = hsnSac;
    }

    public String getRefNo() {
        return this.refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public String getPlaceOfSupply() {
        return this.placeOfSupply;
    }

    public void setPlaceOfSupply(String placeOfSupply) {
        this.placeOfSupply = placeOfSupply;
    }

    public String getPlaceOfSupplyGstin() {
        return this.placeOfSupplyGstin;
    }

    public void setPlaceOfSupplyGstin(String placeOfSupplyGstin) {
        this.placeOfSupplyGstin = placeOfSupplyGstin;
    }

    public String getPoRef() {
        return this.poRef;
    }

    public void setPoRef(String poRef) {
        this.poRef = poRef;
    }

    public String getInvoiceDate() {
        return this.invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getDocs() {
        return this.docs;
    }

    public void setDocs(String docs) {
        this.docs = docs;
    }

	public String getDebitProjectName() {
		return debitProjectName;
	}

	public void setDebitProjectName(String debitProjectName) {
		this.debitProjectName = debitProjectName;
	}

	public String getCreditProjectName() {
		return creditProjectName;
	}

	public void setCreditProjectName(String creditProjectName) {
		this.creditProjectName = creditProjectName;
	}

    public String getWithWithoutTax() {
        return this.withWithoutTax;
    }

    public void setWithWithoutTax(final String withWithoutTax) {
        this.withWithoutTax = withWithoutTax;
    }

    public Double getOpeningBalance() {
        return this.openingBalance;
    }

    public void setOpeningBalance(final Double openingBalance) {
        this.openingBalance = openingBalance;
    }

    public Double getCreditAmount() {
        return this.creditAmount;
    }

    public void setCreditAmount(final Double creditAmount) {
        this.creditAmount = creditAmount;
    }

    public Double getDebitAmount() {
        return this.debitAmount;
    }

    public void setDebitAmount(final Double debitAmount) {
        this.debitAmount = debitAmount;
    }

    public Double getClosingBalance() {
        return this.closingBalance;
    }

    public void setClosingBalance(final Double closingBalance) {
        this.closingBalance = closingBalance;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     *
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(Object o) {
        String ledgerIdObj = ((TrialBalanceLedgerReport)o).getLedgerId();
        if(this.ledgerId != null && ledgerIdObj != null)
            return this.ledgerId.compareTo(ledgerIdObj);
        else
            return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TrialBalanceLedgerReport) {
            return ((TrialBalanceLedgerReport) obj).ledgerId == ledgerId;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ledgerId, ledgerName, txnRef, branchName, creditAmount, debitAmount);
    }
}

