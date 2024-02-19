package model;

import java.util.Date;
import java.util.List;

import javax.persistence.*;

import com.idos.util.IdosDaoConstants;
import com.idos.util.IdosUtil;
import org.hibernate.annotations.Index;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "TRANSACTION")
public class Transaction extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public Transaction() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String TXN_REF_HQL = "select obj from Transaction obj where obj.transactionBranchOrganization.id=?1 and obj.transactionRefNumber=?2 and obj.presentStatus=1";

	private static final String BY_TXN_REF_NO_JPQL = "select obj from Transaction obj where obj.transactionBranchOrganization.id= ?1 and upper(obj.invoiceNumber) = ?2 and obj.presentStatus=1";

	private static final String SPEC_TAX_APPLIED_DATE = "select obj from Transaction obj where obj.transactionBranchOrganization.id = ?1 and obj.transactionBranch.id = ?2 and obj.id in (select obj2.transaction.id from TransactionItems obj2 where obj2.organization.id = ?3 and obj2.branch.id = ?4 and obj2.transactionSpecifics.id = ?5 ) and obj.presentStatus=1 order by obj.transactionDate desc";

	private static final String BILLWISE_OPENING_TXN_REF = "select obj from Transaction obj where obj.transactionBranchOrganization.id= ?1 and obj.paidInvoiceRefNumber = ?2 and obj.typeIdentifier = ?3 and obj.presentStatus =1";

	private static final String PENDING_INVOICE_JPQL = "from Transaction obj WHERE obj.transactionBranchOrganization.id = ?1 and obj.transactionBranch.id = ?2 and obj.transactionVendorCustomer.id = ?3 and obj.transactionPurpose.id in (?4) and obj.paymentStatus in (?5) and obj.transactionStatus = ?6 and obj.presentStatus=1";

	// private static final String LINKED_TXN_JPQL = "from Transaction obj WHERE
	// obj.transactionBranchOrganization.id = ?1 and obj.transactionBranch.id = ?2
	// and obj.transactionVendorCustomer.id = ?3 and obj.transactionPurpose.id =
	// ?4 and obj.paymentStatus in (?5) and obj.transactionStatus = ?6 and
	// obj.linkedTxnRef = ?7 and obj.typeIdentifier = ?8 and obj.presentStatus=1";
	private static final String ORG_SPECIFIC_JPQL = "from Transaction obj WHERE obj.transactionBranchOrganization.id = ?1 and obj.transactionSpecifics.id = ?2";

	@ManyToOne
	@JoinColumn(name = "APPROVER_ACTION_BY")
	private Users approverActionBy;

	public Users getApproverActionBy() {
		return approverActionBy;
	}

	public void setApproverActionBy(Users approverActionBy) {
		this.approverActionBy = approverActionBy;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_BRANCH")
	private Branch transactionBranch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_TO_BRANCH")
	private Branch transactionToBranch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "INVENTORY_TRANSFER_FROM_BRANCH")
	private Branch inventoryTransferFromBranch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_BRANCH_ORGANIZATION")
	private Organization transactionBranchOrganization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_TO_BRANCH_ORGANIZATION")
	private Organization transactionToBranchOrganization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "INVENTORY_TRANSFER_FROM_BRANCH_ORGANIZATION")
	private Organization inventoryTransferFromBranchOrganization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_PROJECT")
	private Project transactionProject;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_SPECIFICS")
	private Specifics transactionSpecifics;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_SPECIFICS_PARTICULARS")
	private Particulars transactionParticulars;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_VENDOR_CUSTOMER")
	private Vendor transactionVendorCustomer;

	@Column(name = "TRANSACTION_UNAVAILABLE_VENDOR_CUSTOMER")
	private String transactionUnavailableVendorCustomer;

	@Column(name = "NO_OF_UNITS")
	private Double noOfUnits;

	@Column(name = "PRICE_PER_UNIT")
	private Double pricePerUnit;

	@Column(name = "GROSS_AMOUNT")
	private Double grossAmount;

	@Column(name = "INVOICE_VALUE")
	private Double invoiceValue;

	public Double getInvoiceValue() {
		return invoiceValue;
	}

	public void setInvoiceValue(Double invoiceValue) {
		this.invoiceValue = invoiceValue;
	}

	@Column(name = "ADVANCE_TYPE") // 2=opening balance set from vendor/customer screen, 1=item wise adv paid using
									// Receive adv from customer/vendor
	private Integer advanceType;

	@Column(name = "AVAILABLE_ADVANCE")
	private Double availableAdvance;

	@Column(name = "ADJUSTMENT_FROM_ADVANCE")
	private Double adjustmentFromAdvance;

	@Column(name = "AVAILABLE_SPECIAL_ADJUSTMENT_AMOUNT")
	private Double availableSpecialAdjustmentAmount;

	@Column(name = "ADJUSTMENT_FROM_SPECIAL_ADJUSTMENT_AMOUNT")
	private Double adjustmentFromSpecialAdjustmentAmount;

	@Column(name = "DOC_RULE_STATUS")
	private Integer docRuleStatus;

	@Column(name = "PRESENT_TXN_RULES")
	private String presentTxnRules;

	@Column(name = "AVAILABLE_STOCK_FROM_BRANCH")
	private Integer availableStockFromBranch;

	@Column(name = "STOCK_TRANSFER_IN_PROGRESS")
	private Integer stockTransferInProgress;

	@Column(name = "AVAILABLE_STOCK_TO_BRANCH")
	private Integer availableStockToBranch;

	@Column(name = "RESULTANT_STOCK")
	private Integer resultantStock;

	@Column(name = "PERFORMA_INVOICE")
	private Boolean performaInvoice;

	@Column(name = "INSTRUMENT_NUMBER")
	private String instrumentNumber;

	@Column(name = "INSTRUMENT_DATE")
	private String instrumentDate;

	@Column(name = "BRS_BANK_DATE")
	private String brsBankDate;

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

	public Integer getAdvanceType() {
		return advanceType;
	}

	public void setAdvanceType(Integer advanceType) {
		this.advanceType = advanceType;
	}

	public Boolean getPerformaInvoice() {
		return performaInvoice;
	}

	public void setPerformaInvoice(Boolean performaInvoice) {
		this.performaInvoice = performaInvoice;
	}

	public Integer getDocRuleStatus() {
		return docRuleStatus;
	}

	public void setDocRuleStatus(Integer docRuleStatus) {
		this.docRuleStatus = docRuleStatus;
	}

	public Double getAvailableSpecialAdjustmentAmount() {
		return availableSpecialAdjustmentAmount;
	}

	public void setAvailableSpecialAdjustmentAmount(
			Double availableSpecialAdjustmentAmount) {
		this.availableSpecialAdjustmentAmount = availableSpecialAdjustmentAmount;
	}

	public Double getAdjustmentFromSpecialAdjustmentAmount() {
		return adjustmentFromSpecialAdjustmentAmount;
	}

	public void setAdjustmentFromSpecialAdjustmentAmount(
			Double adjustmentFromSpecialAdjustmentAmount) {
		this.adjustmentFromSpecialAdjustmentAmount = adjustmentFromSpecialAdjustmentAmount;
	}

	@Column(name = "AVAILABLE_DISCOUNT_PERCENTAGE")
	private Double availableDiscountPercentage;

	@Column(name = "AVAILABLE_DISCOUNT")
	private Double availableDiscountAmountForTxn;

	@Column(name = "NET_AMOUNT")
	private Double netAmount;

	@Column(name = "ROUNDED_CUTPART_OF_NET_AMOUNT")
	private Double roundedCutPartOfNetAmount;

	@Column(name = "NET_AMOUNT_RESULT_DESCRIPTION")
	private String netAmountResultDescription;

	@Column(name = "RECEIPT_DETAILS_TYPE")
	private Integer receiptDetailsType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_BRANCH_BANK")
	private BranchBankAccounts transactionBranchBankAccount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_TO_BRANCH_BANK")
	private BranchBankAccounts transactionToBranchBankAccount;

	public Double getRoundedCutPartOfNetAmount() {
		return roundedCutPartOfNetAmount;
	}

	public void setRoundedCutPartOfNetAmount(Double roundedCutPartOfNetAmount) {
		this.roundedCutPartOfNetAmount = roundedCutPartOfNetAmount;
	}

	public BranchBankAccounts getTransactionBranchBankAccount() {
		return transactionBranchBankAccount;
	}

	public void setTransactionBranchBankAccount(
			BranchBankAccounts transactionBranchBankAccount) {
		this.transactionBranchBankAccount = transactionBranchBankAccount;
	}

	@Column(name = "RECEIPT_DETAILS_DESCRIPTION")
	private String receiptDetailsDescription;

	@Column(name = "REMARKS")
	private String remarks;

	@Column(name = "REMARKS_PRIVATE")
	private String remarksPrivate;

	@Index(name = "fk_TRANSACTION_STATUS", columnNames = "TRANSACTION_STATUS")
	@Column(name = "TRANSACTION_STATUS")
	private String transactionStatus;

	@Index(name = "fk_TRANSACTION_DATE", columnNames = "TRANSACTION_ACTIONDATE")
	@Column(name = "TRANSACTION_ACTIONDATE")
	private Date transactionDate;

	@Column(name = "TRANSACTION_INVOICE_DATE")
	private Date transactionInvoiceDate;

	public Date getTransactionInvoiceDate() {
		return transactionInvoiceDate;
	}

	public void setTransactionInvoiceDate(Date transactionInvoiceDate) {
		this.transactionInvoiceDate = transactionInvoiceDate;
	}

	@Index(name = "fk_TRANSACTION_REF_NUMBER", columnNames = "TRANSACTION_REF_NUMBER")
	@Column(name = "TRANSACTION_REF_NUMBER")
	private String transactionRefNumber;

	@Column(name = "SUPPORTING_DOCS")
	private String supportingDocs;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_PURPOSE")
	private TransactionPurpose transactionPurpose;

	@Index(name = "fk_KLFOLLOW_STATUS", columnNames = "KLFOLLOWSTATUS")
	@Column(name = "KLFOLLOWSTATUS")
	private Integer klFollowStatus;

	@Column(name = "USER_TXN_LIMIT_DESCRIPTION")
	private String userTxnLimitDesc;

	@Column(name = "BUDGET_AVAILABLE_DURING_TXN")
	private String budgetAvailDuringTxn;

	@Column(name = "ACTUAL_ALLOCATED_BUDGET")
	private String actualAllocatedBudget;

	@Column(name = "TRANSACTION_DONE")
	private Integer txnDone;

	@Column(name = "PAYMENT_STATUS")
	private String paymentStatus;

	@Column(name = "CUSTOMER_DUE_PAYMENT")
	private Double customerDuePayment;

	@Column(name = "CUSTOMER_NET_PAYMENT")
	private Double customerNetPayment;

	@Column(name = "VENDOR_DUE_PAYMENT")
	private Double vendorDuePayment;

	@Column(name = "VENDOR_NET_PAYMENT")
	private Double vendorNetPayment;

	@Column(name = "SALES_RETURN_AMOUNT")
	private Double salesReturnAmount;

	@Column(name = "PURCHASE_RETURN_AMOUNT")
	private Double purchaseReturnAmount;

	@Index(name = "fk_PAID_INVOICE_REF_NUMBER", columnNames = "PAID_INVOICE_REF_NUMBER")
	@Column(name = "PAID_INVOICE_REF_NUMBER")
	private String paidInvoiceRefNumber;

	@Column(name = "AUDITOR_USER_REMARKS_DATE")
	private String auditorUserRemarksDate;

	@ManyToOne
	@JoinColumn(name = "PROCURED_BY")
	private Users procuredBy;

	@Column(name = "TRANSACTION_EXCEEDING_BUDGET")
	private Integer transactionExceedingBudget;

	public Integer getTransactionExceedingBudget() {
		return transactionExceedingBudget;
	}

	public void setTransactionExceedingBudget(Integer transactionExceedingBudget) {
		this.transactionExceedingBudget = transactionExceedingBudget;
	}

	public Users getProcuredBy() {
		return procuredBy;
	}

	public void setProcuredBy(Users procuredBy) {
		this.procuredBy = procuredBy;
	}

	@Index(name = "fk_PROCUREMENT_STATUS", columnNames = "PROCUREMENT_STATUS")
	@Column(name = "PROCUREMENT_STATUS")
	private String procurementStatus;

	@Column(name = "TAX_NAME_1")
	private String taxName1;

	@Column(name = "TAX_VALUE_1")
	private Double taxValue1;

	@Column(name = "TAX_NAME_2")
	private String taxName2;

	@Column(name = "TAX_VALUE_2")
	private Double taxValue2;

	@Column(name = "TAX_NAME_3")
	private String taxName3;

	@Column(name = "TAX_VALUE_3")
	private Double taxValue3;

	@Column(name = "TAX_NAME_4")
	private String taxName4;

	@Column(name = "TAX_VALUE_4")
	private Double taxValue4;

	@Column(name = "TAX_NAME_5")
	private String taxName5;

	@Column(name = "TAX_VALUE_5")
	private Double taxValue5;

	@Column(name = "TAX_NAME_6")
	private String taxName6;

	@Column(name = "TAX_VALUE_6")
	private Double taxValue6;

	@Column(name = "TAX_NAME_7")
	private String taxName7;

	@Column(name = "TAX_VALUE_7")
	private Double taxValue7;

	@Column(name = "WITHHOLDING_TAX")
	private Double withholdingTax;

	@Column(name = "SALES_INDIVIDUAL_TAX_FORMULA")
	private String salesIndividualTaxFormula;

	public String getSalesIndividualTaxFormula() {
		return salesIndividualTaxFormula;
	}

	public void setSalesIndividualTaxFormula(String salesIndividualTaxFormula) {
		this.salesIndividualTaxFormula = salesIndividualTaxFormula;
	}

	public Double getWithholdingTax() {
		return withholdingTax;
	}

	public void setWithholdingTax(Double withholdingTax) {
		this.withholdingTax = withholdingTax;
	}

	public String getTaxName1() {
		return taxName1;
	}

	public void setTaxName1(String taxName1) {
		this.taxName1 = taxName1;
	}

	public Double getTaxValue1() {
		return taxValue1;
	}

	public void setTaxValue1(Double taxValue1) {
		this.taxValue1 = taxValue1;
	}

	public String getTaxName2() {
		return taxName2;
	}

	public void setTaxName2(String taxName2) {
		this.taxName2 = taxName2;
	}

	public Double getTaxValue2() {
		return taxValue2;
	}

	public void setTaxValue2(Double taxValue2) {
		this.taxValue2 = taxValue2;
	}

	public String getTaxName3() {
		return taxName3;
	}

	public void setTaxName3(String taxName3) {
		this.taxName3 = taxName3;
	}

	public Double getTaxValue3() {
		return taxValue3;
	}

	public void setTaxValue3(Double taxValue3) {
		this.taxValue3 = taxValue3;
	}

	public String getTaxName4() {
		return taxName4;
	}

	public void setTaxName4(String taxName4) {
		this.taxName4 = taxName4;
	}

	public Double getTaxValue4() {
		return taxValue4;
	}

	public void setTaxValue4(Double taxValue4) {
		this.taxValue4 = taxValue4;
	}

	public String getTaxName5() {
		return taxName5;
	}

	public void setTaxName5(String taxName5) {
		this.taxName5 = taxName5;
	}

	public Double getTaxValue5() {
		return taxValue5;
	}

	public void setTaxValue5(Double taxValue5) {
		this.taxValue5 = taxValue5;
	}

	public String getTaxName6() {
		return taxName6;
	}

	public void setTaxName6(String taxName6) {
		this.taxName6 = taxName6;
	}

	public Double getTaxValue6() {
		return taxValue6;
	}

	public void setTaxValue6(Double taxValue6) {
		this.taxValue6 = taxValue6;
	}

	public String getTaxName7() {
		return taxName7;
	}

	public void setTaxName7(String taxName7) {
		this.taxName7 = taxName7;
	}

	public Double getTaxValue7() {
		return taxValue7;
	}

	public void setTaxValue7(Double taxValue7) {
		this.taxValue7 = taxValue7;
	}

	public String getProcurementStatus() {
		return procurementStatus;
	}

	public void setProcurementStatus(String procurementStatus) {
		this.procurementStatus = procurementStatus;
	}

	public String getAuditorUserRemarksDate() {
		return auditorUserRemarksDate;
	}

	public void setAuditorUserRemarksDate(String auditorUserRemarksDate) {
		this.auditorUserRemarksDate = auditorUserRemarksDate;
	}

	public String getPaidInvoiceRefNumber() {
		return paidInvoiceRefNumber;
	}

	public void setPaidInvoiceRefNumber(String paidInvoiceRefNumber) {
		this.paidInvoiceRefNumber = paidInvoiceRefNumber;
	}

	public Double getSalesReturnAmount() {
		return salesReturnAmount;
	}

	public void setSalesReturnAmount(Double salesReturnAmount) {
		this.salesReturnAmount = salesReturnAmount;
	}

	public Double getPurchaseReturnAmount() {
		return purchaseReturnAmount;
	}

	public void setPurchaseReturnAmount(Double purchaseReturnAmount) {
		this.purchaseReturnAmount = purchaseReturnAmount;
	}

	@Column(name = "ADVANCE_PURPOSE")
	private String advancePurpose;

	@Column(name = "VENDOR_CUSTOMER_ACCEPTENCE")
	private Integer vendCustAcceptence = 0;

	public Integer getVendCustAcceptence() {
		return vendCustAcceptence;
	}

	public void setVendCustAcceptence(Integer vendCustAcceptence) {
		this.vendCustAcceptence = vendCustAcceptence;
	}

	public String getAdvancePurpose() {
		return advancePurpose;
	}

	public void setAdvancePurpose(String advancePurpose) {
		this.advancePurpose = IdosUtil.escapeHtml(advancePurpose);
	}

	public Double getCustomerNetPayment() {
		return customerNetPayment;
	}

	public void setCustomerNetPayment(Double customerNetPayment) {
		this.customerNetPayment = customerNetPayment;
	}

	public Double getCustomerDuePayment() {
		return customerDuePayment;
	}

	public void setCustomerDuePayment(Double customerDuePayment) {
		this.customerDuePayment = customerDuePayment;
	}

	public Double getVendorDuePayment() {
		return vendorDuePayment;
	}

	public void setVendorDuePayment(Double vendorDuePayment) {
		this.vendorDuePayment = vendorDuePayment;
	}

	public Double getVendorNetPayment() {
		return vendorNetPayment;
	}

	public void setVendorNetPayment(Double vendorNetPayment) {
		this.vendorNetPayment = vendorNetPayment;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public Integer getTxnDone() {
		return txnDone;
	}

	public void setTxnDone(Integer txnDone) {
		this.txnDone = txnDone;
	}

	public String getActualAllocatedBudget() {
		return actualAllocatedBudget;
	}

	public void setActualAllocatedBudget(String actualAllocatedBudget) {
		this.actualAllocatedBudget = actualAllocatedBudget;
	}

	public String getUserTxnLimitDesc() {
		return userTxnLimitDesc;
	}

	public void setUserTxnLimitDesc(String userTxnLimitDesc) {
		this.userTxnLimitDesc = userTxnLimitDesc;
	}

	public String getBudgetAvailDuringTxn() {
		return budgetAvailDuringTxn;
	}

	public void setBudgetAvailDuringTxn(String budgetAvailDuringTxn) {
		this.budgetAvailDuringTxn = budgetAvailDuringTxn;
	}

	@Index(name = "fk_APPROVER_EMAILS", columnNames = "APPROVER_EMAILS")
	@Column(name = "APPROVER_EMAILS")
	private String approverEmails;

	@Index(name = "fk_ADDITIONAL_APPROVER_EMAILS", columnNames = "ADDITIONAL_APPROVER_USER_EMAILS")
	@Column(name = "ADDITIONAL_APPROVER_USER_EMAILS")
	private String additionalApproverEmails;

	@Index(name = "fk_SELECTED_ADDITIONAL_APPROVER_EMAILS", columnNames = "SELECTED_ADDITIONAL_APPROVER")
	@Column(name = "SELECTED_ADDITIONAL_APPROVER")
	private String selectedAdditionalApprover;

	@Column(name = "IS_BACKDATED_TRANSACTION") // 0=normal transaction 1=backdated transaction, so will have effect on
												// inventory
	private Integer isBackdatedTransaction;

	public String getVendCustDocUrl() {
		return vendCustDocUrl;
	}

	public void setVendCustDocUrl(String vendCustDocUrl) {
		this.vendCustDocUrl = vendCustDocUrl;
	}

	@Column(name = "VENDOR_CUSTOMER_REMARKS")
	private String vendCustRemarks;

	public String getVendCustRemarks() {
		return vendCustRemarks;
	}

	public void setVendCustRemarks(String vendCustRemarks) {
		this.vendCustRemarks = vendCustRemarks;
	}

	@Column(name = "VENDOR_CUSTOMER_DOC_URL")
	private String vendCustDocUrl;

	public String getSelectedAdditionalApprover() {
		return selectedAdditionalApprover;
	}

	public void setSelectedAdditionalApprover(String selectedAdditionalApprover) {
		this.selectedAdditionalApprover = selectedAdditionalApprover;
	}

	public String getAdditionalApproverEmails() {
		return additionalApproverEmails;
	}

	public void setAdditionalApproverEmails(String additionalApproverEmails) {
		this.additionalApproverEmails = additionalApproverEmails;
	}

	public String getApproverEmails() {
		return approverEmails;
	}

	public void setApproverEmails(String approverEmails) {
		this.approverEmails = approverEmails;
	}

	public Integer getKlFollowStatus() {
		return klFollowStatus;
	}

	public void setKlFollowStatus(Integer klFollowStatus) {
		this.klFollowStatus = klFollowStatus;
	}

	public TransactionPurpose getTransactionPurpose() {
		return transactionPurpose;
	}

	public void setTransactionPurpose(TransactionPurpose transactionPurpose) {
		this.transactionPurpose = transactionPurpose;
	}

	public String getSupportingDocs() {
		return supportingDocs;
	}

	public void setSupportingDocs(String supportingDocs) {
		this.supportingDocs = supportingDocs;
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

	public Specifics getTransactionSpecifics() {
		return transactionSpecifics;
	}

	public void setTransactionSpecifics(Specifics transactionSpecifics) {
		this.transactionSpecifics = transactionSpecifics;
	}

	public Particulars getTransactionParticulars() {
		return transactionParticulars;
	}

	public void setTransactionParticulars(Particulars transactionParticulars) {
		this.transactionParticulars = transactionParticulars;
	}

	public Vendor getTransactionVendorCustomer() {
		return transactionVendorCustomer;
	}

	public void setTransactionVendorCustomer(Vendor transactionVendorCustomer) {
		this.transactionVendorCustomer = transactionVendorCustomer;
	}

	public String getTransactionUnavailableVendorCustomer() {
		return transactionUnavailableVendorCustomer;
	}

	public void setTransactionUnavailableVendorCustomer(
			String transactionUnavailableVendorCustomer) {
		this.transactionUnavailableVendorCustomer = IdosUtil.escapeHtml(transactionUnavailableVendorCustomer);
	}

	public Double getNoOfUnits() {
		return noOfUnits;
	}

	public void setNoOfUnits(Double noOfUnits) {
		this.noOfUnits = noOfUnits;
	}

	public Double getPricePerUnit() {
		return pricePerUnit;
	}

	public void setPricePerUnit(Double pricePerUnit) {
		this.pricePerUnit = pricePerUnit;
	}

	public Double getGrossAmount() {
		return grossAmount;
	}

	public void setGrossAmount(Double grossAmount) {
		this.grossAmount = grossAmount;
	}

	public Double getAvailableAdvance() {
		return availableAdvance;
	}

	public void setAvailableAdvance(Double availableAdvance) {
		this.availableAdvance = availableAdvance;
	}

	public Double getAdjustmentFromAdvance() {
		return adjustmentFromAdvance;
	}

	public void setAdjustmentFromAdvance(Double adjustmentFromAdvance) {
		this.adjustmentFromAdvance = adjustmentFromAdvance;
	}

	public Double getAvailableDiscountPercentage() {
		return availableDiscountPercentage;
	}

	public void setAvailableDiscountPercentage(Double availableDiscountPercentage) {
		this.availableDiscountPercentage = availableDiscountPercentage;
	}

	public Double getAvailableDiscountAmountForTxn() {
		return availableDiscountAmountForTxn;
	}

	public void setAvailableDiscountAmountForTxn(
			Double availableDiscountAmountForTxn) {
		this.availableDiscountAmountForTxn = availableDiscountAmountForTxn;
	}

	public Double getNetAmount() {
		return netAmount;
	}

	public void setNetAmount(Double netAmount) {
		this.netAmount = netAmount;
	}

	public String getNetAmountResultDescription() {
		return netAmountResultDescription;
	}

	public void setNetAmountResultDescription(String netAmountResultDescription) {
		this.netAmountResultDescription = netAmountResultDescription;
	}

	public Integer getReceiptDetailsType() {
		return receiptDetailsType;
	}

	public void setReceiptDetailsType(Integer receiptDetailsType) {
		this.receiptDetailsType = receiptDetailsType;
	}

	public String getReceiptDetailsDescription() {
		return receiptDetailsDescription;
	}

	public void setReceiptDetailsDescription(String receiptDetailsDescription) {
		this.receiptDetailsDescription = IdosUtil.escapeHtml(receiptDetailsDescription);
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = IdosUtil.escapeHtml(remarks);
	}

	public String getRemarksPrivate() {
		return this.remarksPrivate;
	}

	public void setRemarksPrivate(String remarksPrivate) {
		this.remarksPrivate = remarksPrivate;
	}

	public String getTransactionStatus() {
		return transactionStatus;
	}

	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	public String getTransactionRefNumber() {
		return transactionRefNumber;
	}

	public void setTransactionRefNumber(String transactionRefNumber) {
		this.transactionRefNumber = transactionRefNumber;
	}

	/**
	 * Find a Transaction by id.
	 */
	public static Transaction findById(Long id) {
		return entityManager.find(Transaction.class, id);
	}

	public String getPresentTxnRules() {
		return presentTxnRules;
	}

	public void setPresentTxnRules(String presentTxnRules) {
		this.presentTxnRules = presentTxnRules;
	}

	public BranchBankAccounts getTransactionToBranchBankAccount() {
		return transactionToBranchBankAccount;
	}

	public void setTransactionToBranchBankAccount(
			BranchBankAccounts transactionToBranchBankAccount) {
		this.transactionToBranchBankAccount = transactionToBranchBankAccount;
	}

	public Branch getTransactionToBranch() {
		return transactionToBranch;
	}

	public void setTransactionToBranch(Branch transactionToBranch) {
		this.transactionToBranch = transactionToBranch;
	}

	public Organization getTransactionToBranchOrganization() {
		return transactionToBranchOrganization;
	}

	public void setTransactionToBranchOrganization(
			Organization transactionToBranchOrganization) {
		this.transactionToBranchOrganization = transactionToBranchOrganization;
	}

	public Organization getInventoryTransferFromBranchOrganization() {
		return inventoryTransferFromBranchOrganization;
	}

	public void setInventoryTransferFromBranchOrganization(
			Organization inventoryTransferFromBranchOrganization) {
		this.inventoryTransferFromBranchOrganization = inventoryTransferFromBranchOrganization;
	}

	public Branch getInventoryTransferFromBranch() {
		return inventoryTransferFromBranch;
	}

	public void setInventoryTransferFromBranch(Branch inventoryTransferFromBranch) {
		this.inventoryTransferFromBranch = inventoryTransferFromBranch;
	}

	public Integer getAvailableStockFromBranch() {
		return availableStockFromBranch;
	}

	public void setAvailableStockFromBranch(Integer availableStockFromBranch) {
		this.availableStockFromBranch = availableStockFromBranch;
	}

	public Integer getStockTransferInProgress() {
		return stockTransferInProgress;
	}

	public void setStockTransferInProgress(Integer stockTransferInProgress) {
		this.stockTransferInProgress = stockTransferInProgress;
	}

	public Integer getAvailableStockToBranch() {
		return availableStockToBranch;
	}

	public void setAvailableStockToBranch(Integer availableStockToBranch) {
		this.availableStockToBranch = availableStockToBranch;
	}

	public Integer getResultantStock() {
		return resultantStock;
	}

	public void setResultantStock(Integer resultantStock) {
		this.resultantStock = resultantStock;
	}

	@Column(name = "FRIEGHT_CHARGES")
	private Double frieghtCharges;

	@Column(name = "PO_REFERENCE")
	private String poReference;

	@Column(name = "INVOICE_NUMBER")
	private String invoiceNumber;

	@Column(name = "SOURCE_GSTIN")
	private String sourceGstin;

	@Column(name = "DESTINATION_GSTIN")
	private String destinationGstin;

	@Column(name = "TYPE_OF_SUPPLY")
	private Integer typeOfSupply;

	@Column(name = "WITH_WITHOUT_TAX")
	private Integer withWithoutTax;

	@Column(name = "WALKIN_CUSTOMER_TYPE")
	private Integer walkinCustomerType = 0;

	@Column(name = "LINKED_TXN_REF")
	private String linkedTxnRef;

	@Column(name = "TYPE_IDENTIFIER")
	private Integer typeIdentifier; // in case of credit/debit -increase or decrease, for inter branch transfer -
									// outword/inword

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "transaction")
	private List<TransactionItems> transactionItems;

	public Double getFrieghtCharges() {
		return frieghtCharges;
	}

	public void setFrieghtCharges(Double frieghtCharges) {
		this.frieghtCharges = frieghtCharges;
	}

	public String getPoReference() {
		return poReference;
	}

	public void setPoReference(String poReference) {
		this.poReference = IdosUtil.escapeHtml(poReference);
	}

	public String getInvoiceNumber() {
		return this.invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public String getSourceGstin() {
		return this.sourceGstin;
	}

	public void setSourceGstin(String sourceGstin) {
		this.sourceGstin = sourceGstin;
	}

	public String getDestinationGstin() {
		return this.destinationGstin;
	}

	public void setDestinationGstin(String destinationGstin) {
		this.destinationGstin = destinationGstin;
	}

	public Integer getTypeOfSupply() {
		return this.typeOfSupply;
	}

	public void setTypeOfSupply(Integer typeOfSupply) {
		this.typeOfSupply = typeOfSupply;
	}

	public Integer getWithWithoutTax() {
		return this.withWithoutTax;
	}

	public void setWithWithoutTax(Integer withWithoutTax) {
		this.withWithoutTax = withWithoutTax;
	}

	public Integer getWalkinCustomerType() {
		return this.walkinCustomerType;
	}

	public void setWalkinCustomerType(Integer walkinCustomerType) {
		this.walkinCustomerType = walkinCustomerType;
	}

	public String getLinkedTxnRef() {
		return this.linkedTxnRef;
	}

	public void setLinkedTxnRef(String linkedTxnRef) {
		this.linkedTxnRef = linkedTxnRef;
	}

	public Integer getTypeIdentifier() {
		return this.typeIdentifier;
	}

	public void setTypeIdentifier(Integer typeIdentifier) {
		this.typeIdentifier = typeIdentifier;
	}

	public List<TransactionItems> getTransactionItems() {
		return this.transactionItems;
	}

	public void setTransactionItems(List<TransactionItems> transactionItems) {
		this.transactionItems = transactionItems;
	}

	public Integer getIsBackdatedTransaction() {
		return isBackdatedTransaction;
	}

	public void setIsBackdatedTransaction(Integer isBackdatedTransaction) {
		this.isBackdatedTransaction = isBackdatedTransaction;
	}

	public static List<Transaction> findByTxnReference(EntityManager entityManager, Long orgid, String txnRefNo) {
		List<Transaction> list = null;
		Query query = entityManager.createQuery(TXN_REF_HQL);
		query.setParameter(1, orgid);
		query.setParameter(2, txnRefNo);
		list = query.getResultList();
		return list;
	}

	public static boolean isTxnPresentForItem(EntityManager entityManager, Long orgid, Long txnPurpose, Long itemid) {
		List<Transaction> list = null;
		Query query = entityManager.createQuery(
				"select a from Transaction a where a.transactionBranchOrganization.id=?1 and a.transactionPurpose.id=?2 and a.transactionStatus='Accounted' and a.id in (select b.transaction.id from TransactionItems b where b.organization.id=?3 and b.transactionSpecifics.id = ?4) and a.presentStatus=1");
		query.setParameter(1, orgid);
		query.setParameter(2, txnPurpose);
		query.setParameter(3, orgid);
		query.setParameter(4, itemid);
		list = query.getResultList();
		return list.size() > 0;
	}

	public static boolean isTxnPresentForItemBranch(EntityManager entityManager, Long orgid, Long txnPurpose,
			Long branchId, Long itemid) {
		List<Transaction> list = null;
		Query query = entityManager.createQuery(
				"select a from Transaction a where a.transactionBranchOrganization.id=?1 and a.transactionPurpose.id=?2 and a.transactionStatus='Accounted' and a.id in (select b.transaction.id from TransactionItems b where b.organization.id=?3 and b.branch.id = ?4 and b.transactionSpecifics.id = ?5) and a.presentStatus=1");
		query.setParameter(1, orgid);
		query.setParameter(2, txnPurpose);
		query.setParameter(3, orgid);
		query.setParameter(4, branchId);
		query.setParameter(5, itemid);
		list = query.getResultList();
		return list.size() > 0;
	}

	public static Boolean isTxnLinkedWithBillwiseOpeningBal(EntityManager entityManager, Long orgid, String billId,
			int typeIdentifier) {
		List<Transaction> list = null;
		Query query = entityManager.createQuery(BILLWISE_OPENING_TXN_REF);
		query.setParameter(1, orgid);
		query.setParameter(2, billId);
		query.setParameter(3, typeIdentifier);
		list = query.getResultList();
		if (list != null && list.size() > 0) {
			return false;
		}
		return true;
	}

	public static List<Transaction> findByInvoiceNumber(EntityManager entityManager, Organization orgn,
			String txnRefNo) {
		Query query = entityManager.createQuery(BY_TXN_REF_NO_JPQL);
		query.setParameter(1, orgn.getId());
		query.setParameter(2, txnRefNo.toUpperCase());
		List<Transaction> transactions = query.getResultList();
		return transactions;
	}

	public static Date getAppliedDateForCOA(EntityManager entityManager, Long orgid, Long branchId, Long specId) {
		List<Transaction> list = null;
		Query query = entityManager.createQuery(SPEC_TAX_APPLIED_DATE);
		query.setParameter(1, orgid);
		query.setParameter(2, branchId);
		query.setParameter(3, orgid);
		query.setParameter(4, branchId);
		query.setParameter(5, specId);
		list = query.getResultList();
		if (list != null && list.size() > 0) {
			Transaction tran = (Transaction) list.get(0);
			return tran.getTransactionDate();
		}
		return null;
	}

	public boolean isTaxApplied() {
		Double taxAmt = 0d;
		if (this.taxValue1 != null) {
			taxAmt += this.taxValue1;
		}
		if (this.taxValue2 != null) {
			taxAmt += this.taxValue2;
		}
		if (this.taxValue3 != null) {
			taxAmt += this.taxValue3;
		}
		if (this.taxValue4 != null) {
			taxAmt += this.taxValue4;
		}
		if (this.taxValue5 != null) {
			taxAmt += this.taxValue5;
		}
		if (this.taxValue6 != null) {
			taxAmt += this.taxValue6;
		}
		if (this.taxValue7 != null) {
			taxAmt += this.taxValue7;
		}
		if (taxAmt > 0) {
			return true;
		} else {
			return false;
		}
	}

	public static List<Transaction> findByOrgCustVendPaymentStatus(EntityManager entityManager, long orgid,
			long branchId, long custVendId, List<Long> txnPurposeList, List<String> paymentStatusList,
			String txnStatus) {
		List<Transaction> list = null;
		Query query = entityManager.createQuery(PENDING_INVOICE_JPQL);
		query.setParameter(1, orgid);
		query.setParameter(2, branchId);
		query.setParameter(3, custVendId);
		query.setParameter(4, txnPurposeList);
		query.setParameter(5, paymentStatusList);
		query.setParameter(6, txnStatus);
		list = query.getResultList();
		return list;
	}

	public static List<Transaction> findByOrgBOMNotAccounted(EntityManager entityManager, long orgid,
			long transacrionPurposeId) {
		String BOM_NOT_ACCOUNTED_JPQL = "from Transaction obj WHERE obj.transactionBranchOrganization.id = ?1 and obj.transactionPurpose.id in (?2) and obj.transactionStatus != 'Accounted' and obj.presentStatus=1";
		List<Transaction> list = null;
		Query query = entityManager.createQuery(BOM_NOT_ACCOUNTED_JPQL);
		query.setParameter(1, orgid);
		query.setParameter(2, transacrionPurposeId);
		list = query.getResultList();
		return list;
	}

	public static Transaction findCancelledSellInvoice(EntityManager entityManager, long orgid,
			String transactionRefNo) {
		String CANCELLED_SELL_INV = "from Transaction obj WHERE obj.transactionBranchOrganization.id = ?1 and obj.linkedTxnRef = ?2 and obj.transactionPurpose.id= ?3 and obj.transactionStatus = 'Accounted'";
		List<Transaction> list = null;
		Query query = entityManager.createQuery(CANCELLED_SELL_INV);
		query.setParameter(1, orgid);
		query.setParameter(2, transactionRefNo);
		query.setParameter(3, 38l);
		list = query.getResultList();
		Transaction transaction = null;
		if (list.size() > 0)
			transaction = list.get(0);
		return transaction;
	}

	public static List<Transaction> findByOrgSpecific(EntityManager entityManager, long orgid, long specificId) {
		List<Transaction> list = null;
		Query query = entityManager.createQuery(ORG_SPECIFIC_JPQL);
		query.setParameter(1, orgid);
		query.setParameter(2, specificId);
		list = query.getResultList();
		return list;
	}

}
