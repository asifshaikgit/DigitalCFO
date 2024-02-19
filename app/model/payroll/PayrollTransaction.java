package model.payroll;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import model.AbstractBaseModel;
import model.Branch;
import model.BranchBankAccounts;
import model.IdosProvisionJournalEntry;
import model.Organization;
import model.TransactionPurpose;
import model.Users;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "PAYROLL_TRANSACTION")
public class PayrollTransaction extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public PayrollTransaction() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORGANIZATION_ID")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BRANCH_ID")
	private Branch branch;

	@Column(name = "PAYSLIP_YEAR")
	private Integer payslipYear;

	@Column(name = "PAYSLIP_MONTH")
	private Integer payslipMonth;

	@Column(name = "PAY_DAYS")
	private Integer payDays;

	@Column(name = "ELIGIBLE_DAYS")
	private Double eligibleDays;

	@Column(name = "TOTAL_EARNING1")
	private Double totalEarning1;

	@Column(name = "TOTAL_EARNING2")
	private Double totalEarning2;

	@Column(name = "TOTAL_EARNING3")
	private Double totalEarning3;

	@Column(name = "TOTAL_EARNING4")
	private Double totalEarning4;

	@Column(name = "TOTAL_EARNING5")
	private Double totalEarning5;

	@Column(name = "TOTAL_EARNING6")
	private Double totalEarning6;

	@Column(name = "TOTAL_EARNING7")
	private Double totalEarning7;

	@Column(name = "TOTAL_DEDUCTION1")
	private Double totalDeduction1;

	@Column(name = "TOTAL_DEDUCTION2")
	private Double totalDeduction2;

	@Column(name = "TOTAL_DEDUCTION3")
	private Double totalDeduction3;

	@Column(name = "TOTAL_DEDUCTION4")
	private Double totalDeduction4;

	@Column(name = "TOTAL_DEDUCTION5")
	private Double totalDeduction5;

	@Column(name = "TOTAL_DEDUCTION6")
	private Double totalDeduction6;

	@Column(name = "TOTAL_DEDUCTION7")
	private Double totalDeduction7;

	@Column(name = "TOTAL_TOTAL_INCOME")
	private Double totalTotalIncome;

	@Column(name = "TOTAL_TOTAL_DEDUCTION")
	private Double totalTotalDeduction;

	@Column(name = "TOTAL_NET_PAY")
	private Double totalNetPay;

	@Column(name = "SUPPORTING_DOCS")
	private String supportingDocs;

	@Column(name = "TRANSACTION_REF_NUMBER")
	private String transactionRefNumber;

	@Column(name = "TRANSACTION_ACTIONDATE")
	private Date transactionDate;

	@Column(name = "APPROVER_EMAILS")
	private String approverEmails;

	@Column(name = "ADDITIONAL_APPROVER_USER_EMAILS")
	private String additionalApproverEmails;

	@Column(name = "TRANSACTION_STATUS")
	private String transactionStatus;

	@Column(name = "SELECTED_ADDITIONAL_APPROVER")
	private String selectedAdditionalApprover;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_PURPOSE")
	private TransactionPurpose transactionPurpose;

	// @ManyToOne
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APPROVER_ACTION_BY")
	private Users approverActionBy;

	@Column(name = "INSTRUMENT_NUMBER")
	private String instrumentNumber;

	@Column(name = "INSTRUMENT_DATE")
	private String instrumentDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_BRANCH_BANK")
	private BranchBankAccounts transactionBranchBankAccount;

	@Column(name = "REMARKS")
	private String remarks;

	@Column(name = "PAY_MODE")
	private String payMode;

	@Column(name = "EARNING1_ID")
	private Integer earning1Id;
	@Column(name = "EARNING2_ID")
	private Integer earning2Id;
	@Column(name = "EARNING3_ID")
	private Integer earning3Id;
	@Column(name = "EARNING4_ID")
	private Integer earning4Id;
	@Column(name = "EARNING5_ID")
	private Integer earning5Id;
	@Column(name = "EARNING6_ID")
	private Integer earning6Id;
	@Column(name = "EARNING7_ID")
	private Integer earning7Id;

	@Column(name = "DEDUCTION1_ID")
	private Integer deduction1Id;
	@Column(name = "DEDUCTION2_ID")
	private Integer deduction2Id;
	@Column(name = "DEDUCTION3_ID")
	private Integer deduction3Id;
	@Column(name = "DEDUCTION4_ID")
	private Integer deduction4Id;
	@Column(name = "DEDUCTION5_ID")
	private Integer deduction5Id;
	@Column(name = "DEDUCTION6_ID")
	private Integer deduction6Id;
	@Column(name = "DEDUCTION7_ID")
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

	public Double getEligibleDays() {
		return eligibleDays;
	}

	public void setEligibleDays(Double eligibleDays) {
		this.eligibleDays = eligibleDays;
	}

	public Double getTotalEarning1() {
		return totalEarning1;
	}

	public void setTotalEarning1(Double totalEarning1) {
		this.totalEarning1 = totalEarning1;
	}

	public Double getTotalEarning2() {
		return totalEarning2;
	}

	public void setTotalEarning2(Double totalEarning2) {
		this.totalEarning2 = totalEarning2;
	}

	public Double getTotalEarning3() {
		return totalEarning3;
	}

	public void setTotalEarning3(Double totalEarning3) {
		this.totalEarning3 = totalEarning3;
	}

	public Double getTotalEarning4() {
		return totalEarning4;
	}

	public void setTotalEarning4(Double totalEarning4) {
		this.totalEarning4 = totalEarning4;
	}

	public Double getTotalEarning5() {
		return totalEarning5;
	}

	public void setTotalEarning5(Double totalEarning5) {
		this.totalEarning5 = totalEarning5;
	}

	public Double getTotalEarning6() {
		return totalEarning6;
	}

	public void setTotalEarning6(Double totalEarning6) {
		this.totalEarning6 = totalEarning6;
	}

	public Double getTotalEarning7() {
		return totalEarning7;
	}

	public void setTotalEarning7(Double totalEarning7) {
		this.totalEarning7 = totalEarning7;
	}

	public Double getTotalDeduction1() {
		return totalDeduction1;
	}

	public void setTotalDeduction1(Double totalDeduction1) {
		this.totalDeduction1 = totalDeduction1;
	}

	public Double getTotalDeduction2() {
		return totalDeduction2;
	}

	public void setTotalDeduction2(Double totalDeduction2) {
		this.totalDeduction2 = totalDeduction2;
	}

	public Double getTotalDeduction3() {
		return totalDeduction3;
	}

	public void setTotalDeduction3(Double totalDeduction3) {
		this.totalDeduction3 = totalDeduction3;
	}

	public Double getTotalDeduction4() {
		return totalDeduction4;
	}

	public void setTotalDeduction4(Double totalDeduction4) {
		this.totalDeduction4 = totalDeduction4;
	}

	public Double getTotalDeduction5() {
		return totalDeduction5;
	}

	public void setTotalDeduction5(Double totalDeduction5) {
		this.totalDeduction5 = totalDeduction5;
	}

	public Double getTotalDeduction6() {
		return totalDeduction6;
	}

	public void setTotalDeduction6(Double totalDeduction6) {
		this.totalDeduction6 = totalDeduction6;
	}

	public Double getTotalDeduction7() {
		return totalDeduction7;
	}

	public void setTotalDeduction7(Double totalDeduction7) {
		this.totalDeduction7 = totalDeduction7;
	}

	public Double getTotalTotalIncome() {
		return totalTotalIncome;
	}

	public void setTotalTotalIncome(Double totalTotalIncome) {
		this.totalTotalIncome = totalTotalIncome;
	}

	public Double getTotalTotalDeduction() {
		return totalTotalDeduction;
	}

	public void setTotalTotalDeduction(Double totalTotalDeduction) {
		this.totalTotalDeduction = totalTotalDeduction;
	}

	public Double getTotalNetPay() {
		return totalNetPay;
	}

	public void setTotalNetPay(Double totalNetPay) {
		this.totalNetPay = totalNetPay;
	}

	public String getSupportingDocs() {
		return supportingDocs;
	}

	public void setSupportingDocs(String supportingDocs) {
		this.supportingDocs = supportingDocs;
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

	public String getApproverEmails() {
		return approverEmails;
	}

	public void setApproverEmails(String approverEmails) {
		this.approverEmails = approverEmails;
	}

	public String getAdditionalApproverEmails() {
		return additionalApproverEmails;
	}

	public void setAdditionalApproverEmails(String additionalApproverEmails) {
		this.additionalApproverEmails = additionalApproverEmails;
	}

	public String getTransactionStatus() {
		return transactionStatus;
	}

	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

	public String getSelectedAdditionalApprover() {
		return selectedAdditionalApprover;
	}

	public void setSelectedAdditionalApprover(String selectedAdditionalApprover) {
		this.selectedAdditionalApprover = selectedAdditionalApprover;
	}

	public TransactionPurpose getTransactionPurpose() {
		return transactionPurpose;
	}

	public void setTransactionPurpose(TransactionPurpose transactionPurpose) {
		this.transactionPurpose = transactionPurpose;
	}

	public Users getApproverActionBy() {
		return approverActionBy;
	}

	public void setApproverActionBy(Users approverActionBy) {
		this.approverActionBy = approverActionBy;
	}

	public static PayrollTransaction findById(Long id) {
		return entityManager.find(PayrollTransaction.class, id);
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

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getPayMode() {
		return payMode;
	}

	public void setPayMode(String payMode) {
		this.payMode = payMode;
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
