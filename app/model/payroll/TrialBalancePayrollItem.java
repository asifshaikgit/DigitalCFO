package model.payroll;

import model.*;

import javax.persistence.*;
import java.util.Date;

/**
 * @auther Sunil created on 14.11.2018
 */

@Entity
@Table(name="TRIALBALANCE_PAYROLL_ITEM")
public class TrialBalancePayrollItem extends AbstractBaseModel {
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ORGANIZATION_ID")
    private Organization organization;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="BRANCH_ID")
    private Branch branch;

    @Column(name="TRANSACTION_ID")
    private Long transactionId;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="TRANSACTION_PURPOSE")
    private TransactionPurpose transactionPurpose;

    //Chart of items ID of vitamins
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="TRANSACTION_SPECIFICS")
    private PayrollSetup payrollItem;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="TRANSACTION_SPECIFICS_PARTICULARS")
    private Particulars transactionParticulars;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="USER_ID")
    private Users user;

    @Column(name="DATE")
    private Date date;

    @Column(name="CREDIT_AMOUNT")
    private Double creditAmount;

    @Column(name="DEBIT_AMOUNT")
    private Double debitAmount;

    @Column(name="CLOSING_BALANCE")
    private Double closingBalance;

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Branch getBranch() {
        return this.branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Long getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public TransactionPurpose getTransactionPurpose() {
        return this.transactionPurpose;
    }

    public void setTransactionPurpose(TransactionPurpose transactionPurpose) {
        this.transactionPurpose = transactionPurpose;
    }

    public PayrollSetup getPayrollItem() {
        return this.payrollItem;
    }

    public void setPayrollItem(PayrollSetup payrollItem) {
        this.payrollItem = payrollItem;
    }

    public Particulars getTransactionParticulars() {
        return this.transactionParticulars;
    }

    public void setTransactionParticulars(Particulars transactionParticulars) {
        this.transactionParticulars = transactionParticulars;
    }

    public Users getUser() {
        return this.user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getCreditAmount() {
        return this.creditAmount;
    }

    public void setCreditAmount(Double creditAmount) {
        this.creditAmount = creditAmount;
    }

    public Double getDebitAmount() {
        return this.debitAmount;
    }

    public void setDebitAmount(Double debitAmount) {
        this.debitAmount = debitAmount;
    }

    public Double getClosingBalance() {
        return this.closingBalance;
    }

    public void setClosingBalance(Double closingBalance) {
        this.closingBalance = closingBalance;
    }
}
