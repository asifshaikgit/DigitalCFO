package model.payroll;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import model.AbstractBaseModel;
import model.Branch;
import model.BranchBankAccounts;
import model.Organization;
import model.TransactionPurpose;
import model.Users;


	
@Entity
@Table(name="PAYROLL_USER_PAYSLIP")
public class PayrollUserPayslip extends AbstractBaseModel {
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ORGANIZATION_ID")
	private Organization organization;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_ID")
	private Branch branch;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USER_ID")
	private Users user;

	@Column(name="PAYSLIP_YEAR")
	private Integer payslipYear;
	
	@Column(name="PAYSLIP_MONTH")
	private Integer payslipMonth;
	
	@Column(name="PAY_DAYS")
	private Integer payDays;
	
	@Column(name="ELIGIBLE_DAYS")
	private Integer eligibleDays;
	/*@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PAYROLL_SETUP_ID")
	private PayrollSetup payrollSetupInc1; //maintaining the order of payroll types in asc order when showing data on screen and inserting into payslip table..so not using this right now
	*/
	@Column(name="EARNING1")
	private Double earning1;		
	@Column(name="EARNING2")
	private Double earning2;
	@Column(name="EARNING3")
	private Double earning3;
	@Column(name="EARNING4")
	private Double earning4;
	@Column(name="EARNING5")
	private Double earning5;
	@Column(name="EARNING6")
	private Double earning6;
	@Column(name="EARNING7")
	private Double earning7;
		
	
	@Column(name="DEDUCTION1")
	private Double deduction1;
	@Column(name="DEDUCTION2")
	private Double deduction2;
	@Column(name="DEDUCTION3")
	private Double deduction3;
	@Column(name="DEDUCTION4")
	private Double deduction4;
	@Column(name="DEDUCTION5")
	private Double deduction5;
	@Column(name="DEDUCTION6")
	private Double deduction6;
	@Column(name="DEDUCTION7")
	private Double deduction7;
	
	@Column(name="TOTAL_EARNING")
	private Double totalEarning;
	
	@Column(name="TOTAL_DEDUCTION")
	private Double totalDeduction;

	@Column(name="NET_PAY")
	private Double netPay;
	
	@Column(name="PAYMENT_MODE")
	private Integer paymentMode;
	
	@Column(name="INSTRUMENT_NUMBER")
	private String instrumentNumber;		

	@Column(name="INSTRUMENT_DATE")
	private String instrumentDate;
	
	@Column(name="RECEIPT_DETAILS")
	private String receiptDetails;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TRANSACTION_BRANCH_BANK")
	private BranchBankAccounts transactionBranchBankAccount;
	
	@Column(name="TRANSACTION_REF_NUMBER")
	private String transactionRefNumber;
	
	@Column(name="TRANSACTION_ACTIONDATE")
	private Date transactionDate;
	
	@Column(name="EARNING1_ID")
	private Integer earning1Id;		
	@Column(name="EARNING2_ID")
	private Integer earning2Id;
	@Column(name="EARNING3_ID")
	private Integer earning3Id;
	@Column(name="EARNING4_ID")
	private Integer earning4Id;
	@Column(name="EARNING5_ID")
	private Integer earning5Id;
	@Column(name="EARNING6_ID")
	private Integer earning6Id;
	@Column(name="EARNING7_ID")
	private Integer earning7Id;
		
	
	@Column(name="DEDUCTION1_ID")
	private Integer deduction1Id;
	@Column(name="DEDUCTION2_ID")
	private Integer deduction2Id;
	@Column(name="DEDUCTION3_ID")
	private Integer deduction3Id;
	@Column(name="DEDUCTION4_ID")
	private Integer deduction4Id;
	@Column(name="DEDUCTION5_ID")
	private Integer deduction5Id;
	@Column(name="DEDUCTION6_ID")
	private Integer deduction6Id;
	@Column(name="DEDUCTION7_ID")
	private Integer deduction7Id;

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
		
	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

	public Integer getPayslipYear() {
		return payslipYear;
	}

	public void setPayslipYear(Integer payslipYear) {
		this.payslipYear = payslipYear;
	}

	public Integer getPayslipMonth() {
		return payslipMonth;
	}

	public void setPayslipMonth(Integer payslipMonth) {
		this.payslipMonth = payslipMonth;
	}
	
	public Integer getPayDays() {
		return payDays;
	}

	public void setPayDays(Integer payDays) {
		this.payDays = payDays;
	}

	public Integer getEligibleDays() {
		return eligibleDays;
	}

	public void setEligibleDays(Integer eligibleDays) {
		this.eligibleDays = eligibleDays;
	}
	
	public Double getEarning1() {
		return earning1;
	}

	public void setEarning1(Double earning1) {
		this.earning1 = earning1;
	}

	public Double getEarning2() {
		return earning2;
	}

	public void setEarning2(Double earning2) {
		this.earning2 = earning2;
	}

	public Double getEarning3() {
		return earning3;
	}

	public void setEarning3(Double earning3) {
		this.earning3 = earning3;
	}

	public Double getEarning4() {
		return earning4;
	}

	public void setEarning4(Double earning4) {
		this.earning4 = earning4;
	}

	public Double getEarning5() {
		return earning5;
	}

	public void setEarning5(Double earning5) {
		this.earning5 = earning5;
	}

	public Double getEarning6() {
		return earning6;
	}

	public void setEarning6(Double earning6) {
		this.earning6 = earning6;
	}

	public Double getEarning7() {
		return earning7;
	}

	public void setEarning7(Double earning7) {
		this.earning7 = earning7;
	}

	public Double getDeduction1() {
		return deduction1;
	}

	public void setDeduction1(Double deduction1) {
		this.deduction1 = deduction1;
	}

	public Double getDeduction2() {
		return deduction2;
	}

	public void setDeduction2(Double deduction2) {
		this.deduction2 = deduction2;
	}

	public Double getDeduction3() {
		return deduction3;
	}

	public void setDeduction3(Double deduction3) {
		this.deduction3 = deduction3;
	}

	public Double getDeduction4() {
		return deduction4;
	}

	public void setDeduction4(Double deduction4) {
		this.deduction4 = deduction4;
	}

	public Double getDeduction5() {
		return deduction5;
	}

	public void setDeduction5(Double deduction5) {
		this.deduction5 = deduction5;
	}

	public Double getDeduction6() {
		return deduction6;
	}

	public void setDeduction6(Double deduction6) {
		this.deduction6 = deduction6;
	}

	public Double getDeduction7() {
		return deduction7;
	}

	public void setDeduction7(Double deduction7) {
		this.deduction7 = deduction7;
	}

	public Double getTotalEarning() {
		return totalEarning;
	}

	public void setTotalEarning(Double totalEarning) {
		this.totalEarning = totalEarning;
	}

	public Double getTotalDeduction() {
		return totalDeduction;
	}

	public void setTotalDeduction(Double totalDeduction) {
		this.totalDeduction = totalDeduction;
	}

	public Double getNetPay() {
		return netPay;
	}

	public void setNetPay(Double netPay) {
		this.netPay = netPay;
	}

	public Integer getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(Integer paymentMode) {
		this.paymentMode = paymentMode;
	}

	public String getInstrumentNumber() {
		return instrumentNumber;
	}

	public void setInstrumentNumber(String instrumentNumber) {
		this.instrumentNumber = instrumentNumber;
	}

	public String getInstrumentDate() {
		return instrumentDate;
	}

	public void setInstrumentDate(String instrumentDate) {
		this.instrumentDate = instrumentDate;
	}

	public BranchBankAccounts getTransactionBranchBankAccount() {
		return transactionBranchBankAccount;
	}

	public void setTransactionBranchBankAccount(BranchBankAccounts transactionBranchBankAccount) {
		this.transactionBranchBankAccount = transactionBranchBankAccount;
	}
	
	public String getReceiptDetails() {
		return receiptDetails;
	}

	public void setReceiptDetails(String receiptDetails) {
		this.receiptDetails = receiptDetails;
	}
	
	public String getTransactionRefNumber() {
		return transactionRefNumber;
	}

	public void setTransactionRefNumber(String transactionRefNumber) {
		this.transactionRefNumber = transactionRefNumber;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}
	
	public Integer getEarning1Id() {
		return earning1Id;
	}

	public void setEarning1Id(Integer earning1Id) {
		this.earning1Id = earning1Id;
	}

	public Integer getEarning2Id() {
		return earning2Id;
	}

	public void setEarning2Id(Integer earning2Id) {
		this.earning2Id = earning2Id;
	}

	public Integer getEarning3Id() {
		return earning3Id;
	}

	public void setEarning3Id(Integer earning3Id) {
		this.earning3Id = earning3Id;
	}

	public Integer getEarning4Id() {
		return earning4Id;
	}

	public void setEarning4Id(Integer earning4Id) {
		this.earning4Id = earning4Id;
	}

	public Integer getEarning5Id() {
		return earning5Id;
	}

	public void setEarning5Id(Integer earning5Id) {
		this.earning5Id = earning5Id;
	}

	public Integer getEarning6Id() {
		return earning6Id;
	}

	public void setEarning6Id(Integer earning6Id) {
		this.earning6Id = earning6Id;
	}

	public Integer getEarning7Id() {
		return earning7Id;
	}

	public void setEarning7Id(Integer earning7Id) {
		this.earning7Id = earning7Id;
	}

	public Integer getDeduction1Id() {
		return deduction1Id;
	}

	public void setDeduction1Id(Integer deduction1Id) {
		this.deduction1Id = deduction1Id;
	}

	public Integer getDeduction2Id() {
		return deduction2Id;
	}

	public void setDeduction2Id(Integer deduction2Id) {
		this.deduction2Id = deduction2Id;
	}

	public Integer getDeduction3Id() {
		return deduction3Id;
	}

	public void setDeduction3Id(Integer deduction3Id) {
		this.deduction3Id = deduction3Id;
	}

	public Integer getDeduction4Id() {
		return deduction4Id;
	}

	public void setDeduction4Id(Integer deduction4Id) {
		this.deduction4Id = deduction4Id;
	}

	public Integer getDeduction5Id() {
		return deduction5Id;
	}

	public void setDeduction5Id(Integer deduction5Id) {
		this.deduction5Id = deduction5Id;
	}

	public Integer getDeduction6Id() {
		return deduction6Id;
	}

	public void setDeduction6Id(Integer deduction6Id) {
		this.deduction6Id = deduction6Id;
	}

	public Integer getDeduction7Id() {
		return deduction7Id;
	}

	public void setDeduction7Id(Integer deduction7Id) {
		this.deduction7Id = deduction7Id;
	}

	
}
