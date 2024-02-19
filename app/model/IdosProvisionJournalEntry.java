package model;

import java.util.Date;
import java.util.List;

import javax.persistence.*;

import com.idos.util.IdosUtil;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "PROVISION_JOURNAL_ENTRY")
public class IdosProvisionJournalEntry extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public IdosProvisionJournalEntry() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "provisionJournalEntry")
	private List<ProvisionJournalEntryDetail> provisionJournalEntryDetails;

	@Column(name = "TOTAL_DEBIT_AMOUNT")
	private Double totalDebitAmount = 0.0;

	@Column(name = "TOTAL_CREDIT_AMOUNT")
	private Double totalCreditAmount = 0.0;

	@Column(name = "TXN_REFERENCE_NUMBER")
	private String txnReferenceNumber;

	@Column(name = "PURPOSE")
	private String purpose;

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		IdosUtil.escapeHtml(this.purpose = purpose);
	}

	@Column(name = "REMARKS")
	private String txnRemarks;

	@Column(name = "SUPPORTING_DOCUMENTS")
	private String supportingDocuments;

	@Column(name = "ALLOWED_REVERSAL")
	private Integer allowedReversal = 0;

	@Column(name = "REVERSAL_DATE")
	private Date reversalDate;

	@Column(name = "DEBIT_INCOMES_EFFECT")
	private Integer debitIncomesEffect = 0;

	@Column(name = "DEBIT_EXPENSES_EFFECT")
	private Integer debitExpensesEffect = 0;

	@Column(name = "DEBIT_ASSETS_EFFECT")
	private Integer debitAssetsEffect = 0;

	@Column(name = "DEBIT_LIABILITIES_EFFECT")
	private Integer debitLiabilitiesEffect = 0;

	@Column(name = "CREDIT_INCOMES_EFFECT")
	private Integer creditIncomesEffect = 0;

	@Column(name = "CREDIT_EXPENSE_EFFECT")
	private Integer creditExpensesEffect = 0;

	@Column(name = "CREDIT_ASSETS_EFFECT")
	private Integer creditAssetsEffect = 0;

	@Column(name = "CREDIT_LABILITIES_EFFECT")
	private Integer creditLiabilitiesEffect = 0;

	@Column(name = "REVERSAL_STATUS")
	private String reversalStatus;

	@Column(name = "PROVISION_TYPE")
	private String provisionType;

	@Column(name = "APPROVER_EMAILS")
	private String approverEmails;

	@Column(name = "ADDITIONAL_APPROVER_USER_EMAILS")
	private String additionalApproverUserEmails;

	@Column(name = "SELECTED_ADDITIONAL_APPROVER")
	private String selectedAdditionalApprover;

	@Column(name = "TRANSACTION_DONE")
	private Integer transactionDone = 0;

	@Column(name = "TRANSACTION_STATUS")
	private String transactionStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DEBIT_BRANCH")
	private Branch debitBranch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CREDIT_BRANCH")
	private Branch creditBranch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BRANCH_ORGANIZATION")
	private Organization provisionMadeForOrganization;

	public Organization getProvisionMadeForOrganization() {
		return provisionMadeForOrganization;
	}

	public void setProvisionMadeForOrganization(Organization provisionMadeForOrganization) {
		this.provisionMadeForOrganization = provisionMadeForOrganization;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_PURPOSE")
	private TransactionPurpose transactionPurpose;

	@Column(name = "TRANSACTION_DATE")
	private Date transactionDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APPROVER_ACTION_BY")
	private Users approverActionBy;

	@Column(name = "TRANSACTION_REF_NUMBER")
	private String transactionRefNumber;

	@Column(name = "INSTRUMENT_NUMBER")
	private String instrumentNumber;

	@Column(name = "INSTRUMENT_DATE")
	private String instrumentDate;

	@Column(name = "BRS_BANK_DATE")
	private String brsBankDate;

	@Column(name = "IS_BACKDATED_TRANSACTION")
	private Integer isBackdatedTransaction;

	public String getBrsBankDate() {
		return brsBankDate;
	}

	public void setBrsBankDate(String brsBankDate) {
		this.brsBankDate = IdosUtil.escapeHtml(brsBankDate);
	}

	public String getInstrumentDate() {
		return instrumentDate;
	}

	public void setInstrumentDate(String instrumentDate) {
		this.instrumentDate = instrumentDate;
	}

	public String getInstrumentNumber() {
		return instrumentNumber;
	}

	public void setInstrumentNumber(String instrumentNumber) {
		this.instrumentNumber = IdosUtil.escapeHtml(instrumentNumber);
	}

	public String getTransactionRefNumber() {
		return transactionRefNumber;
	}

	public void setTransactionRefNumber(String transactionRefNumber) {
		this.transactionRefNumber = transactionRefNumber;
	}

	public Users getApproverActionBy() {
		return approverActionBy;
	}

	public void setApproverActionBy(Users approverActionBy) {
		this.approverActionBy = approverActionBy;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	public Branch getDebitBranch() {
		return debitBranch;
	}

	public void setDebitBranch(Branch debitBranch) {
		this.debitBranch = debitBranch;
	}

	public Branch getCreditBranch() {
		return creditBranch;
	}

	public void setCreditBranch(Branch creditBranch) {
		this.creditBranch = creditBranch;
	}

	public TransactionPurpose getTransactionPurpose() {
		return transactionPurpose;
	}

	public void setTransactionPurpose(TransactionPurpose transactionPurpose) {
		this.transactionPurpose = transactionPurpose;
	}

	public String getApproverEmails() {
		return approverEmails;
	}

	public void setApproverEmails(String approverEmails) {
		this.approverEmails = approverEmails;
	}

	public String getAdditionalApproverUserEmails() {
		return additionalApproverUserEmails;
	}

	public void setAdditionalApproverUserEmails(String additionalApproverUserEmails) {
		this.additionalApproverUserEmails = additionalApproverUserEmails;
	}

	public String getSelectedAdditionalApprover() {
		return selectedAdditionalApprover;
	}

	public void setSelectedAdditionalApprover(String selectedAdditionalApprover) {
		this.selectedAdditionalApprover = selectedAdditionalApprover;
	}

	public Integer getTransactionDone() {
		return transactionDone;
	}

	public void setTransactionDone(Integer transactionDone) {
		this.transactionDone = transactionDone;
	}

	public String getTransactionStatus() {
		return transactionStatus;
	}

	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

	public Integer getDebitIncomesEffect() {
		return debitIncomesEffect;
	}

	public void setDebitIncomesEffect(Integer debitIncomesEffect) {
		this.debitIncomesEffect = debitIncomesEffect;
	}

	public Integer getDebitExpensesEffect() {
		return debitExpensesEffect;
	}

	public void setDebitExpensesEffect(Integer debitExpensesEffect) {
		this.debitExpensesEffect = debitExpensesEffect;
	}

	public Integer getDebitAssetsEffect() {
		return debitAssetsEffect;
	}

	public void setDebitAssetsEffect(Integer debitAssetsEffect) {
		this.debitAssetsEffect = debitAssetsEffect;
	}

	public Integer getDebitLiabilitiesEffect() {
		return debitLiabilitiesEffect;
	}

	public void setDebitLiabilitiesEffect(Integer debitLiabilitiesEffect) {
		this.debitLiabilitiesEffect = debitLiabilitiesEffect;
	}

	public Integer getCreditIncomesEffect() {
		return creditIncomesEffect;
	}

	public void setCreditIncomesEffect(Integer creditIncomesEffect) {
		this.creditIncomesEffect = creditIncomesEffect;
	}

	public Integer getCreditExpensesEffect() {
		return creditExpensesEffect;
	}

	public void setCreditExpensesEffect(Integer creditExpensesEffect) {
		this.creditExpensesEffect = creditExpensesEffect;
	}

	public Integer getCreditAssetsEffect() {
		return creditAssetsEffect;
	}

	public void setCreditAssetsEffect(Integer creditAssetsEffect) {
		this.creditAssetsEffect = creditAssetsEffect;
	}

	public Integer getCreditLiabilitiesEffect() {
		return creditLiabilitiesEffect;
	}

	public void setCreditLiabilitiesEffect(Integer creditLiabilitiesEffect) {
		this.creditLiabilitiesEffect = creditLiabilitiesEffect;
	}

	public String getReversalStatus() {
		return reversalStatus;
	}

	public void setReversalStatus(String reversalStatus) {
		this.reversalStatus = reversalStatus;
	}

	public String getProvisionType() {
		return provisionType;
	}

	public void setProvisionType(String provisionType) {
		this.provisionType = provisionType;
	}

	public Double getTotalDebitAmount() {
		return totalDebitAmount;
	}

	public void setTotalDebitAmount(Double totalDebitAmount) {
		this.totalDebitAmount = totalDebitAmount;
	}

	public Double getTotalCreditAmount() {
		return totalCreditAmount;
	}

	public void setTotalCreditAmount(Double totalCreditAmount) {
		this.totalCreditAmount = totalCreditAmount;
	}

	public String getTxnReferenceNumber() {
		return txnReferenceNumber;
	}

	public void setTxnReferenceNumber(String txnReferenceNumber) {
		this.txnReferenceNumber = txnReferenceNumber;
	}

	public String getTxnRemarks() {
		return txnRemarks;
	}

	public void setTxnRemarks(String txnRemarks) {
		this.txnRemarks = IdosUtil.escapeHtml(txnRemarks);
	}

	public String getSupportingDocuments() {
		return supportingDocuments;
	}

	public void setSupportingDocuments(String supportingDocuments) {
		this.supportingDocuments = supportingDocuments;
	}

	public Integer getAllowedReversal() {
		return allowedReversal;
	}

	public void setAllowedReversal(Integer allowedReversal) {
		this.allowedReversal = allowedReversal;
	}

	public Date getReversalDate() {
		return reversalDate;
	}

	public void setReversalDate(Date reversalDate) {
		this.reversalDate = reversalDate;
	}

	/**
	 * Find a IdosProvisionJournalEntry by id.
	 */
	public static IdosProvisionJournalEntry findById(Long id) {
		return entityManager.find(IdosProvisionJournalEntry.class, id);
	}

	public List<ProvisionJournalEntryDetail> getProvisionJournalEntryDetails() {
		return provisionJournalEntryDetails;
	}

	public void setProvisionJournalEntryDetails(List<ProvisionJournalEntryDetail> provisionJournalEntryDetails) {
		this.provisionJournalEntryDetails = provisionJournalEntryDetails;
	}

	public Integer getIsBackdatedTransaction() {
		return isBackdatedTransaction;
	}

	public void setIsBackdatedTransaction(Integer isBackdatedTransaction) {
		this.isBackdatedTransaction = isBackdatedTransaction;
	}

	public static boolean isTxnPresentForItem(EntityManager entityManager, Long orgid, Long itemid) {
		List<Transaction> list = null;
		Query query = entityManager.createQuery(
				"select a from IdosProvisionJournalEntry a where a.provisionMadeForOrganization.id=?1 and a.transactionStatus='Accounted' and a.id in (select b.provisionJournalEntry.id from ProvisionJournalEntryDetail b where b.organization.id=?2 and b.headID = ?3) and a.presentStatus=1");
		query.setParameter(1, orgid);
		query.setParameter(2, orgid);
		query.setParameter(3, itemid);
		list = query.getResultList();
		return list.size() > 0;
	}

	public static boolean isTxnPresentForItemBranch(EntityManager entityManager, Long orgid, Long branchId,
			Long itemid) {
		List<Transaction> list = null;
		Query query = entityManager.createQuery(
				"select a from IdosProvisionJournalEntry a where a.provisionMadeForOrganization.id=?1 and a.transactionStatus='Accounted' and a.id in (select b.provisionJournalEntry.id from ProvisionJournalEntryDetail b where b.organization.id=?2 and b.branch.id = ?3 and b.headID = ?4) and a.presentStatus=1");
		query.setParameter(1, orgid);
		query.setParameter(2, orgid);
		query.setParameter(3, branchId);
		query.setParameter(4, itemid);
		list = query.getResultList();
		return list.size() > 0;
	}
}
