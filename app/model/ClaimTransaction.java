package model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;

import com.idos.util.IdosUtil;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "CLAIMS_TRANSACTION")
public class ClaimTransaction extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public ClaimTransaction() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String EMPLOYEE_CLAIMS_JPQL = "select obj from ClaimTransaction obj where obj.transactionBranchOrganization.id= ?1 and obj.settlementStatus = 'NOT-SETTLED' and obj.transactionStatus = 'Accounted' and (obj.claimsDueSettlement > 0 or obj.claimsRequiredSettlement > 0) and obj.presentStatus=1";

	private static final String EMPLOYEE_PAID_CLAIMS_JPQL = "select obj from ClaimTransaction obj where obj.transactionBranchOrganization.id= ?1 and obj.settlementStatus = 'SETTLED' and obj.transactionStatus = 'Accounted' and obj.claimsDueSettlement = 0 and obj.claimsRequiredSettlement = 0 and obj.presentStatus=1";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_BRANCH")
	private Branch transactionBranch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_BRANCH_ORGANIZATION")
	private Organization transactionBranchOrganization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_PROJECT")
	private Project transactionProject;

	@Column(name = "TRAVEL_TYPE")
	private String travelType;

	@Column(name = "NUMBER_OF_PLACES_TO_VISIT")
	private Integer numberOfPlacesToVisit;

	@Column(name = "TRAVEL_FROM_TO_PLACES")
	private String travelFromToPlaces;

	@Column(name = "TYPE_OF_CITY")
	private String typeOfCity;

	@Column(name = "APPROPRIATE_DISTANCE")
	private String appropriateDistance;

	@Column(name = "TOTAL_DAYS")
	private Integer totalDays;

	@Column(name = "TRAVEL_ELIGIBILITY_DETAILS")
	private String travelEligibilityDetails;

	@Column(name = "ADVANCE_ELIGIBILITY_DETAILS")
	private String advanceEligibilityDetails;

	@Column(name = "EXISTING_ADVANCE")
	private Double existingAdvance;

	@Column(name = "ADJUSTED_ADVANCE")
	private Double adjustedAdvance;

	@Column(name = "GROSS_AMOUNT")
	private Double grossAmount;

	@Column(name = "NET_AMOUNT")
	private Double newAmount;

	@Column(name = "KL_FOLLOW_STATUS")
	private Integer klFollowStatus;

	@Column(name = "PURPOSE_OF_VISIT")
	private String purposeOfVisit;

	@Column(name = "TXN_REMARKS")
	private String txnRemarks;

	@Column(name = "SUPPORTING_DOCUMENTS")
	private String supportingDocuments;

	@Column(name = "APPROVER_EMAILS")
	private String approverMails;

	@Column(name = "ADDITIONAL_APPROVER_EMAILS")
	private String additionalApproverEmails;

	@Column(name = "SELECTED_ADDITIONAL_APPROVER")
	private String selectedAdditionalApprover;

	@Column(name = "DEBIT_CREDIT")
	private String debitCredit;

	@Column(name = "TRANSACTION_REF_NUMBER")
	private String transactionRefNumber;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CLAIM_TRANSACTION_PURPOSE")
	private TransactionPurpose transactionPurpose;

	@Column(name = "TRANSACTION_DONE")
	private Integer transactionDone;

	@Column(name = "TRANSACTION_STATUS")
	private String transactionStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APPROVER_ACTION_BY")
	public Users approverActionBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ACCOUNTING_ACTION_BY")
	public Users accountingActionBy;

	@Column(name = "ACCOUNTANT_EMAILS")
	private String accountantEmails;

	@Column(name = "PRESENT_TXN_RULES")
	private String presentTxnRules;

	@Column(name = "TRANSACTION_DATE")
	private Date transactionDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_BRANCH_BANK")
	private BranchBankAccounts transactionBranchBankAccount;

	@Column(name = "RECEIPT_DETAILS_TYPE")
	private Integer receiptDetailType;

	@Column(name = "RECEIPT_DETAILS_DESCRIPTION")
	private String receiptDetailsDescription;

	@Column(name = "CLAIMS_SETTLEMENT_REFERENCE_NUMBER")
	private String claimsSettlementRefNumber;

	@Column(name = "CLAIMS_NET_SETTLEMENT")
	private Double claimsNetSettlement = 0.0;

	@Column(name = "CALIMS_DUE_SETTLEMENT")
	private Double claimsDueSettlement = 0.0;

	@Column(name = "CLAIMS_SPENT_SETTLEMENT")
	private Double claimsSpentSettlement = 0.0;

	@Column(name = "CLAIMS_REQUIRED_SETTLEMENT")
	private Double claimsRequiredSettlement = 0.0;

	@Column(name = "CLAIMS_RETURN_SETTLEMENT")
	private Double claimsReturnSettlement = 0.0;

	@Column(name = "SETTLEMENT_STATUS")
	private String settlementStatus;

	@Column(name = "CLAIMS_ADVANCE_ADJUSTMENTS_REF_NUMBERS")
	private String claimsAdvanceAdjustmentsRefNumber;

	@Column(name = "EXISTING_CLAIMS_CURRENT_SETTLEMENT_DETAILS")
	private String existingClaimsCurrentSettlementDetails;

	@Column(name = "USER_EXPENDITURE_ON_THIS_TXN")
	private String userExpenditureOnThisTxn;

	@Column(name = "AMOUNT_RETURN_IN_CASE_OF_DUETOCOMPANY")
	private Double amountReturnInCaseOfDueToCompany = 0.0;

	@Column(name = "UPDATED_UNSETTLED_AMOUNT")
	private Double updatedUnsettledAmount = 0.0;

	@Column(name = "TOTAL_ADVANCE_FOR_EXPENSE_DURING_TXN")
	private Double totalAdvanceForExpenseDuringTxn = 0.0;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ADVANCE_FOR_EXPENSE_ITEM")
	private Specifics advanceForExpenseItems;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ADVANCE_FOR_EXPENSE_ITEM_PARTICULAR")
	private Particulars advanceForExpenseItemsParticulars;

	@Column(name = "EXPENSE_ITEM_ADVANCE_ELIGIBILITY_CURRENT_SETTLEMENT_DETAILS")
	private String expenseItemAdvanceEligibilityCurrentSettlementDetails;

	@Column(name = "PURPOSE_OF_EXPENSE_ADVANCE")
	private String purposeOfAdvance;

	@Column(name = "INSTRUMENT_NUMBER")
	private String instrumentNumber;

	@Column(name = "INSTRUMENT_DATE")
	private String instrumentDate;

	@Column(name = "BRS_BANK_DATE")
	private String brsBankDate;

	@Column(name = "CLAIMS_NET_TAX")
	private Double claimsNetTax;

	public String getBrsBankDate() {
		return brsBankDate;
	}

	public void setBrsBankDate(String brsBankDate) {
		this.brsBankDate = brsBankDate;
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

	public Branch getTransactionBranch() {
		return transactionBranch;
	}

	public void setTransactionBranch(Branch transactionBranch) {
		this.transactionBranch = transactionBranch;
	}

	public Organization getTransactionBranchOrganization() {
		return transactionBranchOrganization;
	}

	public void setTransactionBranchOrganization(
			Organization transactionBranchOrganization) {
		this.transactionBranchOrganization = transactionBranchOrganization;
	}

	public Project getTransactionProject() {
		return transactionProject;
	}

	public void setTransactionProject(Project transactionProject) {
		this.transactionProject = transactionProject;
	}

	public String getTravelType() {
		return travelType;
	}

	public void setTravelType(String travelType) {
		this.travelType = travelType;
	}

	public Integer getNumberOfPlacesToVisit() {
		return numberOfPlacesToVisit;
	}

	public void setNumberOfPlacesToVisit(Integer numberOfPlacesToVisit) {
		this.numberOfPlacesToVisit = numberOfPlacesToVisit;
	}

	public String getTravelFromToPlaces() {
		return travelFromToPlaces;
	}

	public void setTravelFromToPlaces(String travelFromToPlaces) {
		this.travelFromToPlaces = IdosUtil.escapeHtml(travelFromToPlaces);
	}

	public String getTypeOfCity() {
		return typeOfCity;
	}

	public void setTypeOfCity(String typeOfCity) {
		this.typeOfCity = typeOfCity;
	}

	public String getAppropriateDistance() {
		return appropriateDistance;
	}

	public void setAppropriateDistance(String appropriateDistance) {
		this.appropriateDistance = appropriateDistance;
	}

	public Integer getTotalDays() {
		return totalDays;
	}

	public void setTotalDays(Integer totalDays) {
		this.totalDays = totalDays;
	}

	public String getTravelEligibilityDetails() {
		return travelEligibilityDetails;
	}

	public void setTravelEligibilityDetails(String travelEligibilityDetails) {
		this.travelEligibilityDetails = travelEligibilityDetails;
	}

	public Double getExistingAdvance() {
		return existingAdvance;
	}

	public void setExistingAdvance(Double existingAdvance) {
		this.existingAdvance = existingAdvance;
	}

	public Double getAdjustedAdvance() {
		return adjustedAdvance;
	}

	public void setAdjustedAdvance(Double adjustedAdvance) {
		this.adjustedAdvance = adjustedAdvance;
	}

	public Double getGrossAmount() {
		return grossAmount;
	}

	public void setGrossAmount(Double grossAmount) {
		this.grossAmount = grossAmount;
	}

	public Double getNewAmount() {
		return newAmount;
	}

	public void setNewAmount(Double newAmount) {
		this.newAmount = newAmount;
	}

	public Integer getKlFollowStatus() {
		return klFollowStatus;
	}

	public void setKlFollowStatus(Integer klFollowStatus) {
		this.klFollowStatus = klFollowStatus;
	}

	public String getPurposeOfVisit() {
		return purposeOfVisit;
	}

	public void setPurposeOfVisit(String purposeOfVisit) {
		this.purposeOfVisit = IdosUtil.escapeHtml(purposeOfVisit);
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

	public String getApproverMails() {
		return approverMails;
	}

	public void setApproverMails(String approverMails) {
		this.approverMails = approverMails;
	}

	public TransactionPurpose getTransactionPurpose() {
		return transactionPurpose;
	}

	public void setTransactionPurpose(TransactionPurpose transactionPurpose) {
		this.transactionPurpose = transactionPurpose;
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

	public Users getApproverActionBy() {
		return approverActionBy;
	}

	public void setApproverActionBy(Users approverActionBy) {
		this.approverActionBy = approverActionBy;
	}

	public Users getAccountingActionBy() {
		return accountingActionBy;
	}

	public void setAccountingActionBy(Users accountingActionBy) {
		this.accountingActionBy = accountingActionBy;
	}

	public String getAdditionalApproverEmails() {
		return additionalApproverEmails;
	}

	public void setAdditionalApproverEmails(String additionalApproverEmails) {
		this.additionalApproverEmails = additionalApproverEmails;
	}

	public String getDebitCredit() {
		return debitCredit;
	}

	public void setDebitCredit(String debitCredit) {
		this.debitCredit = debitCredit;
	}

	public String getTransactionRefNumber() {
		return transactionRefNumber;
	}

	public void setTransactionRefNumber(String transactionRefNumber) {
		this.transactionRefNumber = transactionRefNumber;
	}

	public String getSelectedAdditionalApprover() {
		return selectedAdditionalApprover;
	}

	public void setSelectedAdditionalApprover(String selectedAdditionalApprover) {
		this.selectedAdditionalApprover = selectedAdditionalApprover;
	}

	public String getAccountantEmails() {
		return accountantEmails;
	}

	public void setAccountantEmails(String accountantEmails) {
		this.accountantEmails = accountantEmails;
	}

	public String getPresentTxnRules() {
		return presentTxnRules;
	}

	public void setPresentTxnRules(String presentTxnRules) {
		this.presentTxnRules = presentTxnRules;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	/**
	 * Find a ClaimTransaction by id.
	 */
	public static ClaimTransaction findById(Long id) {
		return entityManager.find(ClaimTransaction.class, id);
	}

	public BranchBankAccounts getTransactionBranchBankAccount() {
		return transactionBranchBankAccount;
	}

	public void setTransactionBranchBankAccount(
			BranchBankAccounts transactionBranchBankAccount) {
		this.transactionBranchBankAccount = transactionBranchBankAccount;
	}

	public Integer getReceiptDetailType() {
		return receiptDetailType;
	}

	public void setReceiptDetailType(Integer receiptDetailType) {
		this.receiptDetailType = receiptDetailType;
	}

	public String getReceiptDetailsDescription() {
		return receiptDetailsDescription;
	}

	public void setReceiptDetailsDescription(String receiptDetailsDescription) {
		this.receiptDetailsDescription = IdosUtil.escapeHtml(receiptDetailsDescription);
	}

	public String getAdvanceEligibilityDetails() {
		return advanceEligibilityDetails;
	}

	public void setAdvanceEligibilityDetails(String advanceEligibilityDetails) {
		this.advanceEligibilityDetails = advanceEligibilityDetails;
	}

	public String getClaimsSettlementRefNumber() {
		return claimsSettlementRefNumber;
	}

	public void setClaimsSettlementRefNumber(String claimsSettlementRefNumber) {
		this.claimsSettlementRefNumber = claimsSettlementRefNumber;
	}

	public Double getClaimsNetSettlement() {
		return claimsNetSettlement;
	}

	public void setClaimsNetSettlement(Double claimsNetSettlement) {
		this.claimsNetSettlement = claimsNetSettlement;
	}

	public Double getClaimsDueSettlement() {
		return claimsDueSettlement;
	}

	public void setClaimsDueSettlement(Double claimsDueSettlement) {
		this.claimsDueSettlement = claimsDueSettlement;
	}

	public Double getClaimsSpentSettlement() {
		return claimsSpentSettlement;
	}

	public void setClaimsSpentSettlement(Double claimsSpentSettlement) {
		this.claimsSpentSettlement = claimsSpentSettlement;
	}

	public Double getClaimsRequiredSettlement() {
		return claimsRequiredSettlement;
	}

	public void setClaimsRequiredSettlement(Double claimsRequiredSettlement) {
		this.claimsRequiredSettlement = claimsRequiredSettlement;
	}

	public Double getClaimsReturnSettlement() {
		return claimsReturnSettlement;
	}

	public void setClaimsReturnSettlement(Double claimsReturnSettlement) {
		this.claimsReturnSettlement = claimsReturnSettlement;
	}

	public String getSettlementStatus() {
		return settlementStatus;
	}

	public void setSettlementStatus(String settlementStatus) {
		this.settlementStatus = settlementStatus;
	}

	public String getClaimsAdvanceAdjustmentsRefNumber() {
		return claimsAdvanceAdjustmentsRefNumber;
	}

	public void setClaimsAdvanceAdjustmentsRefNumber(
			String claimsAdvanceAdjustmentsRefNumber) {
		this.claimsAdvanceAdjustmentsRefNumber = claimsAdvanceAdjustmentsRefNumber;
	}

	public String getExistingClaimsCurrentSettlementDetails() {
		return existingClaimsCurrentSettlementDetails;
	}

	public void setExistingClaimsCurrentSettlementDetails(
			String existingClaimsCurrentSettlementDetails) {
		this.existingClaimsCurrentSettlementDetails = existingClaimsCurrentSettlementDetails;
	}

	public String getUserExpenditureOnThisTxn() {
		return userExpenditureOnThisTxn;
	}

	public void setUserExpenditureOnThisTxn(String userExpenditureOnThisTxn) {
		this.userExpenditureOnThisTxn = userExpenditureOnThisTxn;
	}

	public Double getAmountReturnInCaseOfDueToCompany() {
		return amountReturnInCaseOfDueToCompany;
	}

	public void setAmountReturnInCaseOfDueToCompany(
			Double amountReturnInCaseOfDueToCompany) {
		this.amountReturnInCaseOfDueToCompany = amountReturnInCaseOfDueToCompany;
	}

	public Double getUpdatedUnsettledAmount() {
		return updatedUnsettledAmount;
	}

	public void setUpdatedUnsettledAmount(Double updatedUnsettledAmount) {
		this.updatedUnsettledAmount = updatedUnsettledAmount;
	}

	public Double getTotalAdvanceForExpenseDuringTxn() {
		return totalAdvanceForExpenseDuringTxn;
	}

	public void setTotalAdvanceForExpenseDuringTxn(
			Double totalAdvanceForExpenseDuringTxn) {
		this.totalAdvanceForExpenseDuringTxn = totalAdvanceForExpenseDuringTxn;
	}

	public Specifics getAdvanceForExpenseItems() {
		return advanceForExpenseItems;
	}

	public void setAdvanceForExpenseItems(Specifics advanceForExpenseItems) {
		this.advanceForExpenseItems = advanceForExpenseItems;
	}

	public Particulars getAdvanceForExpenseItemsParticulars() {
		return advanceForExpenseItemsParticulars;
	}

	public void setAdvanceForExpenseItemsParticulars(
			Particulars advanceForExpenseItemsParticulars) {
		this.advanceForExpenseItemsParticulars = advanceForExpenseItemsParticulars;
	}

	public String getExpenseItemAdvanceEligibilityCurrentSettlementDetails() {
		return expenseItemAdvanceEligibilityCurrentSettlementDetails;
	}

	public void setExpenseItemAdvanceEligibilityCurrentSettlementDetails(
			String expenseItemAdvanceEligibilityCurrentSettlementDetails) {
		this.expenseItemAdvanceEligibilityCurrentSettlementDetails = expenseItemAdvanceEligibilityCurrentSettlementDetails;
	}

	public String getPurposeOfAdvance() {
		return purposeOfAdvance;
	}

	public void setPurposeOfAdvance(String purposeOfAdvance) {
		this.purposeOfAdvance = IdosUtil.escapeHtml(purposeOfAdvance);
	}

	public Double getClaimsNetTax() {
		return claimsNetTax;
	}

	public void setClaimsNetTax(Double claimsNetTax) {
		this.claimsNetTax = claimsNetTax;
	}

	public static List<ClaimTransaction> findEmployeePendingClaims(EntityManager entityManager, Long orgid) {
		Query query = entityManager.createQuery(EMPLOYEE_CLAIMS_JPQL);
		query.setParameter(1, orgid);
		List<ClaimTransaction> resultList = query.getResultList();
		return resultList;
	}

	public static List<ClaimTransaction> findEmployeePaidClaims(EntityManager entityManager, Long orgid) {
		Query query = entityManager.createQuery(EMPLOYEE_PAID_CLAIMS_JPQL);
		query.setParameter(1, orgid);
		List<ClaimTransaction> resultList = query.getResultList();
		return resultList;
	}

	@Override
	public String toString() {
		return "ClaimTransaction{ID=" + id +
				", existingAdvance=" + existingAdvance +
				", adjustedAdvance=" + adjustedAdvance +
				", grossAmount=" + grossAmount +
				", newAmount=" + newAmount +
				", debitCredit='" + debitCredit + '\'' +
				", transactionRefNumber='" + transactionRefNumber + '\'' +
				", transactionStatus='" + transactionStatus + '\'' +
				", claimsSettlementRefNumber='" + claimsSettlementRefNumber + '\'' +
				", claimsNetSettlement=" + claimsNetSettlement +
				", claimsDueSettlement=" + claimsDueSettlement +
				", claimsSpentSettlement=" + claimsSpentSettlement +
				", claimsRequiredSettlement=" + claimsRequiredSettlement +
				", claimsReturnSettlement=" + claimsReturnSettlement +
				", settlementStatus='" + settlementStatus + '\'' +
				", amountReturnInCaseOfDueToCompany=" + amountReturnInCaseOfDueToCompany +
				", updatedUnsettledAmount=" + updatedUnsettledAmount +
				", totalAdvanceForExpenseDuringTxn=" + totalAdvanceForExpenseDuringTxn +
				'}';
	}
}
