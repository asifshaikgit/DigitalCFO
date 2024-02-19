package model;

import play.db.jpa.JPAApi;
import javax.inject.Inject;

import javax.persistence.*;
import service.EntityManagerProvider;
import org.hibernate.annotations.Index;

import com.idos.util.IdosConstants;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "TRANSACTION_ITEMS")
public class TransactionItems extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public TransactionItems() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String ORG_TXN_JPQL = "select obj from TransactionItems obj where obj.transaction.id=?1 and obj.presentStatus=1";
	private static final String ORG_TXN_SPCFC_JPQL = "select obj from TransactionItems obj where obj.organization.id = ?1 and obj.transaction.id=?2 and obj.transactionSpecifics.id=?3 and obj.presentStatus=1";
	private static final String ORG_SPCFC_JPQL = "select obj from TransactionItems obj where obj.organization.id = ?1 and obj.transactionSpecifics.id=?2 and obj.presentStatus=1";
	private static final String ORG_TXN_ADV_JSQL = "select COALESCE(SUM(COALESCE(obj.netAmount,0)),0) from TransactionItems obj where obj.organization.id = ?1 and obj.branch.id = ?2 and obj.transaction.id in (select obj2.id from Transaction obj2 where obj2.transactionBranchOrganization.id = ?3 and obj2.transactionBranch.id = ?4 and obj2.linkedTxnRef = ?5) and obj.presentStatus=1";
	private static final String ORG_TXN_TDS_JSQL = "select COALESCE(SUM(COALESCE(obj.withholdingAmount,0)),0) from TransactionItems obj where obj.organization.id = ?1 and obj.branch.id = ?2 and obj.transaction.id in (select obj2.id from Transaction obj2 where obj2.transactionBranchOrganization.id = ?3 and obj2.transactionBranch.id = ?4 and obj2.linkedTxnRef = ?5) and obj.presentStatus=1";
	private static final String ORG_TXN_REFUND_JSQL = "select COALESCE(SUM(COALESCE(obj.netAmountReturned,0)),0) from TransactionItems obj where obj.organization.id = ?1 and obj.branch.id = ?2 and obj.transactionRefNumber=?3 and obj.presentStatus=1";
	private static final String ORG_TXN_REFUND_TDS_JSQL = "select COALESCE(SUM(COALESCE(obj.withholdingAmountReturned,0)),0) from TransactionItems obj where obj.organization.id = ?1 and obj.branch.id = ?2 and obj.transactionRefNumber=?3 and obj.presentStatus=1";
	private static final String TDS_GROSS_QUERY = "select obj from TransactionItems obj where obj.organization.id = ?1 and obj.transactionSpecifics.id = ?2 and obj.transaction.id in (select obj2.id from Transaction obj2 where obj2.transactionBranchOrganization.id = ?3 and obj2.transactionVendorCustomer.id = ?4 and obj2.transactionStatus = ?5 and obj2.transactionDate between ?6 and ?7 and obj2.presentStatus = 1)";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BRANCH_ID")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORGANIZATION_ID")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_ID")
	private Transaction transaction;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_SPECIFICS")
	private Specifics transactionSpecifics;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_SPECIFICS_PARTICULARS")
	private Particulars transactionParticulars;

	@Column(name = "NO_OF_UNITS")
	private Double noOfUnits;

	@Column(name = "PRICE_PER_UNIT")
	private Double pricePerUnit;

	@Column(name = "GROSS_AMOUNT")
	private Double grossAmount;

	@Column(name = "TAX_DESCRIPTION")
	private String taxDescription;

	@Column(name = "TOTAL_TAX")
	private Double totalTax;

	@Column(name = "WITHHOLDING_AMOUNT")
	private Double withholdingAmount;

	@Column(name = "AVAILABLE_ADVANCE")
	private Double availableAdvance;

	@Column(name = "ADJUSTMENT_FROM_ADVANCE")
	private Double adjustmentFromAdvance;

	@Column(name = "NET_AMOUNT")
	private Double netAmount;

	@Column(name = "DISCOUNT_PERCENT")
	private String discountPercent;

	@Column(name = "DISCOUNT_AMOUNT")
	private Double discountAmount;

	@Column(name = "TAX_NAME_1")
	private String taxName1;

	@Column(name = "TAX_VALUE_1")
	private Double taxValue1;

	@Column(name = "TAX_RATE_1")
	private Double taxRate1;

	@Column(name = "TAX_NAME_2")
	private String taxName2;

	@Column(name = "TAX_VALUE_2")
	private Double taxValue2;

	@Column(name = "TAX_RATE_2")
	private Double taxRate2;

	@Column(name = "TAX_NAME_3")
	private String taxName3;

	@Column(name = "TAX_VALUE_3")
	private Double taxValue3;

	@Column(name = "TAX_RATE_3")
	private Double taxRate3;

	@Column(name = "TAX_NAME_4")
	private String taxName4;

	@Column(name = "TAX_VALUE_4")
	private Double taxValue4;

	@Column(name = "TAX_RATE_4")
	private Double taxRate4;

	@Column(name = "TAX_NAME_5")
	private String taxName5;

	@Column(name = "TAX_VALUE_5")
	private Double taxValue5;

	@Column(name = "TAX_RATE_5")
	private Double taxRate5;

	@Column(name = "TAX_NAME_6")
	private String taxName6;

	@Column(name = "TAX_VALUE_6")
	private Double taxValue6;

	@Column(name = "TAX_RATE_6")
	private Double taxRate6;

	@Column(name = "TAX_NAME_7")
	private String taxName7;

	@Column(name = "TAX_VALUE_7")
	private Double taxValue7;

	@Column(name = "TAX_RATE_7")
	private Double taxRate7;

	@Column(name = "USER_TXN_LIMIT_DESCRIPTION")
	private String userTxnLimitDesc;

	@Column(name = "BUDGET_AVAILABLE_DURING_TXN")
	private String budgetAvailDuringTxn;

	@Column(name = "ACTUAL_ALLOCATED_BUDGET")
	private String actualAllocatedBudget;

	// data for sales and purchase Return transactions
	@Column(name = "NO_OF_UNITS_RETURNED")
	private Double noOfUnitsReturned = 0.0;

	@Column(name = "PRICE_PER_UNIT_RETURNED")
	private Double pricePerUnitReturned = 0.0;

	@Column(name = "GROSS_AMOUNT_RETURNED")
	private Double grossAmounReturned = 0.0;

	@Column(name = "TAX_DESCRIPTION_RETURNED")
	private String taxDescriptionReturned;

	@Column(name = "WITHHOLDING_AMOUNT_RETURNED")
	private Double withholdingAmountReturned = 0.0;

	@Column(name = "AVAILABLE_ADVANCE_FOR_RETURNED")
	private Double availableAdvanceForReturned = 0.0;

	@Column(name = "TOTAL_TAX_RETURNED")
	private Double totalTaxReturned = 0.0;

	@Column(name = "ADV_ADJUSTMENT_RETURNED")
	private Double adjustmentFromAdvanceReturned = 0.0;

	@Column(name = "NET_AMOUNT_RETURNED")
	private Double netAmountReturned = 0.0;

	@Column(name = "INVOICE_VALUE")
	private Double invoiceValue = 0.0;
	@Column(name = "ADVADJ_TAX1_VALUE")
	private Double advAdjTax1Value;
	@Column(name = "ADVADJ_TAX2_VALUE")
	private Double advAdjTax2Value;
	@Column(name = "ADVADJ_TAX3_VALUE")
	private Double advAdjTax3Value;
	@Column(name = "ADVADJ_TAX4_VALUE")
	private Double advAdjTax4Value;
	@Column(name = "ADVADJ_TAX5_VALUE")
	private Double advAdjTax5Value;
	@Column(name = "ADVADJ_TAX6_VALUE")
	private Double advAdjTax6Value;
	@Column(name = "ADVADJ_TAX7_VALUE")
	private Double advAdjTax7Value;
	@Column(name = "ADVANCE_PURPOSE")
	private String advancePurpose;

	@Column(name = "TAX1_ID")
	private Long tax1ID;
	@Column(name = "TAX2_ID")
	private Long tax2ID;
	@Column(name = "TAX3_ID")
	private Long tax3ID;
	@Column(name = "TAX4_ID")
	private Long tax4ID;
	@Column(name = "TAX5_ID")
	private Long tax5ID;
	@Column(name = "TAX6_ID")
	private Long tax6ID;
	@Column(name = "TAX7_ID")
	private Long tax7ID;

	@Column(name = "BRANCH_SPECIFICS_TAX_FORMULA_ID")
	private Long reverseChargeItemId;

	@Column(name = "REASON_FOR_RETURN")
	private Integer reasonForReturn;

	// BOM
	@Column(name = "OEM")
	private String oem;

	@Column(name = "UNIT_OF_MEASURE")
	private String unitOfMeaseure;

	@Column(name = "VEND_CUST_ID")
	private Long vendCustID;

	@Column(name = "TYPE_OF_MATERIAL")
	private Integer typeOfMaterial;

	@Column(name = "KNOWLEDGE_LIB")
	private Integer knowledgeLib;

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Transaction getTransactionId() {
		return transaction;
	}

	public void setTransactionId(Transaction transactionId) {
		this.transaction = transactionId;
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

	public Double getGrossAmount() {
		return grossAmount;
	}

	public void setGrossAmount(Double grossAmount) {
		this.grossAmount = grossAmount;
	}

	public Double getWithholdingAmount() {
		return withholdingAmount;
	}

	public void setWithholdingAmount(Double withholdingAmount) {
		this.withholdingAmount = withholdingAmount;
	}

	public String getTaxDescription() {
		return taxDescription;
	}

	public void setTaxDescription(String taxDescription) {
		this.taxDescription = taxDescription;
	}

	public Double getTotalTax() {
		return totalTax;
	}

	public void setTotalTax(Double totalTax) {
		this.totalTax = totalTax;
	}

	public Double getNetAmount() {
		return netAmount;
	}

	public void setNetAmount(Double netAmount) {
		this.netAmount = netAmount;
	}

	public String getDiscountPercent() {
		return this.discountPercent;
	}

	public void setDiscountPercent(String discountPercent) {
		this.discountPercent = discountPercent;
	}

	public Double getDiscountAmount() {
		return this.discountAmount;
	}

	public void setDiscountAmount(Double discountAmount) {
		this.discountAmount = discountAmount;
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

	public Double getTaxRate1() {
		return taxRate1;
	}

	public void setTaxRate1(Double taxRate1) {
		this.taxRate1 = taxRate1;
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

	public Double getTaxRate2() {
		return taxRate2;
	}

	public void setTaxRate2(Double taxRate2) {
		this.taxRate2 = taxRate2;
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

	public Double getTaxRate3() {
		return taxRate3;
	}

	public void setTaxRate3(Double taxRate3) {
		this.taxRate3 = taxRate3;
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

	public Double getTaxRate4() {
		return taxRate4;
	}

	public void setTaxRate4(Double taxRate4) {
		this.taxRate4 = taxRate4;
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

	public Double getTaxRate5() {
		return taxRate5;
	}

	public void setTaxRate5(Double taxRate5) {
		this.taxRate5 = taxRate5;
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

	public Double getTaxRate6() {
		return taxRate6;
	}

	public void setTaxRate6(Double taxRate6) {
		this.taxRate6 = taxRate6;
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

	public Double getTaxRate7() {
		return taxRate7;
	}

	public void setTaxRate7(Double taxRate7) {
		this.taxRate7 = taxRate7;
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

	public String getActualAllocatedBudget() {
		return actualAllocatedBudget;
	}

	public void setActualAllocatedBudget(String actualAllocatedBudget) {
		this.actualAllocatedBudget = actualAllocatedBudget;
	}

	// sales Return and purchase Return fields

	public Double getNoOfUnitsReturned() {
		return noOfUnitsReturned;
	}

	public void setNoOfUnitsReturned(Double noOfUnitsReturned) {
		this.noOfUnitsReturned = noOfUnitsReturned;
	}

	public Double getPricePerUnitReturned() {
		return pricePerUnitReturned;
	}

	public void setPricePerUnitReturned(Double pricePerUnitReturned) {
		this.pricePerUnitReturned = pricePerUnitReturned;
	}

	public Double getGrossAmounReturned() {
		return grossAmounReturned;
	}

	public void setGrossAmounReturned(Double grossAmounReturned) {
		this.grossAmounReturned = grossAmounReturned;
	}

	public Double getTotalTaxReturned() {
		return totalTaxReturned;
	}

	public void setTotalTaxReturned(Double totalTaxReturned) {
		this.totalTaxReturned = totalTaxReturned;
	}

	public Double getAdjustmentFromAdvanceReturned() {
		return adjustmentFromAdvanceReturned;
	}

	public void setAdjustmentFromAdvanceReturned(
			Double adjustmentFromAdvanceReturned) {
		this.adjustmentFromAdvanceReturned = adjustmentFromAdvanceReturned;
	}

	public Double getNetAmountReturned() {
		return netAmountReturned;
	}

	public void setNetAmountReturned(Double netAmountReturned) {
		this.netAmountReturned = netAmountReturned;
	}

	public String getTaxDescriptionReturned() {
		return taxDescriptionReturned;
	}

	public void setTaxDescriptionReturned(String taxDescriptionReturned) {
		this.taxDescriptionReturned = taxDescriptionReturned;
	}

	public Double getWithholdingAmountReturned() {
		return withholdingAmountReturned;
	}

	public void setWithholdingAmountReturned(Double withholdingAmountReturned) {
		this.withholdingAmountReturned = withholdingAmountReturned;
	}

	public Double getAvailableAdvanceForReturned() {
		return availableAdvanceForReturned;
	}

	public void setAvailableAdvanceForReturned(Double availableAdvanceForReturned) {
		this.availableAdvanceForReturned = availableAdvanceForReturned;
	}

	public Double getAdvAdjTax1Value() {
		return this.advAdjTax1Value;
	}

	public void setAdvAdjTax1Value(Double advAdjTax1Value) {
		this.advAdjTax1Value = advAdjTax1Value;
	}

	public Double getAdvAdjTax2Value() {
		return this.advAdjTax2Value;
	}

	public void setAdvAdjTax2Value(Double advAdjTax2Value) {
		this.advAdjTax2Value = advAdjTax2Value;
	}

	public Double getAdvAdjTax3Value() {
		return this.advAdjTax3Value;
	}

	public void setAdvAdjTax3Value(Double advAdjTax3Value) {
		this.advAdjTax3Value = advAdjTax3Value;
	}

	public Double getAdvAdjTax4Value() {
		return this.advAdjTax4Value;
	}

	public void setAdvAdjTax4Value(Double advAdjTax4Value) {
		this.advAdjTax4Value = advAdjTax4Value;
	}

	public Double getAdvAdjTax5Value() {
		return this.advAdjTax5Value;
	}

	public void setAdvAdjTax5Value(Double advAdjTax5Value) {
		this.advAdjTax5Value = advAdjTax5Value;
	}

	public Double getAdvAdjTax6Value() {
		return this.advAdjTax6Value;
	}

	public void setAdvAdjTax6Value(Double advAdjTax6Value) {
		this.advAdjTax6Value = advAdjTax6Value;
	}

	public Double getAdvAdjTax7Value() {
		return this.advAdjTax7Value;
	}

	public void setAdvAdjTax7Value(Double advAdjTax7Value) {
		this.advAdjTax7Value = advAdjTax7Value;
	}

	public Double getInvoiceValue() {
		return this.invoiceValue;
	}

	public void setInvoiceValue(Double invoiceValue) {
		this.invoiceValue = invoiceValue;
	}

	public static TransactionItems findById(Long id) {
		return entityManager.find(TransactionItems.class, id);
	}

	public String getAdvancePurpose() {
		return this.advancePurpose;
	}

	public void setAdvancePurpose(String advancePurpose) {
		this.advancePurpose = advancePurpose;
	}

	public Long getTax1ID() {
		return this.tax1ID;
	}

	public void setTax1ID(Long tax1ID) {
		this.tax1ID = tax1ID;
	}

	public Long getTax2ID() {
		return this.tax2ID;
	}

	public void setTax2ID(Long tax2ID) {
		this.tax2ID = tax2ID;
	}

	public Long getTax3ID() {
		return this.tax3ID;
	}

	public void setTax3ID(Long tax3ID) {
		this.tax3ID = tax3ID;
	}

	public Long getTax4ID() {
		return this.tax4ID;
	}

	public void setTax4ID(Long tax4ID) {
		this.tax4ID = tax4ID;
	}

	public Long getTax5ID() {
		return this.tax5ID;
	}

	public void setTax5ID(Long tax5ID) {
		this.tax5ID = tax5ID;
	}

	public Long getTax6ID() {
		return this.tax6ID;
	}

	public void setTax6ID(Long tax6ID) {
		this.tax6ID = tax6ID;
	}

	public Long getTax7ID() {
		return this.tax7ID;
	}

	public void setTax7ID(Long tax7ID) {
		this.tax7ID = tax7ID;
	}

	public Long getReverseChargeItemId() {
		return reverseChargeItemId;
	}

	public void setReverseChargeItemId(Long reverseChargeItemId) {
		this.reverseChargeItemId = reverseChargeItemId;
	}

	@Column(name = "TRANSACTION_REF_NUMBER")
	private String transactionRefNumber;

	public String getTransactionRefNumber() {
		return transactionRefNumber;
	}

	public void setTransactionRefNumber(String transactionRefNumber) {
		this.transactionRefNumber = transactionRefNumber;
	}

	public Integer getReasonForReturn() {
		return reasonForReturn;
	}

	public void setReasonForReturn(Integer reasonForReturn) {
		this.reasonForReturn = reasonForReturn;
	}

	public String getOem() {
		return oem;
	}

	public void setOem(String oem) {
		this.oem = oem;
	}

	public String getUnitOfMeaseure() {
		return unitOfMeaseure;
	}

	public void setUnitOfMeaseure(String unitOfMeaseure) {
		this.unitOfMeaseure = unitOfMeaseure;
	}

	public Long getVendCustID() {
		return vendCustID;
	}

	public void setVendCustID(Long vendCustID) {
		this.vendCustID = vendCustID;
	}

	public Integer getTypeOfMaterial() {
		return typeOfMaterial;
	}

	public void setTypeOfMaterial(Integer typeOfMaterial) {
		this.typeOfMaterial = typeOfMaterial;
	}

	public Integer getKnowledgeLib() {
		return knowledgeLib;
	}

	public void setKnowledgeLib(Integer knowledgeLib) {
		this.knowledgeLib = knowledgeLib;
	}

	public static List<TransactionItems> finfByTxnId(EntityManager entityManager, Long txnid) {
		List<TransactionItems> list = null;
		Query query = entityManager.createQuery(ORG_TXN_JPQL);
		query.setParameter(1, txnid);
		list = query.getResultList();
		return list;
	}

	public static List<TransactionItems> findByOrgTxnSpecific(EntityManager entityManager, Long orgid, Long txnid,
			Long specificid) {
		List<TransactionItems> list = null;
		Query query = entityManager.createQuery(ORG_TXN_SPCFC_JPQL);
		query.setParameter(1, orgid);
		query.setParameter(2, txnid);
		query.setParameter(3, specificid);
		list = query.getResultList();
		return list;
	}

	public static List<TransactionItems> findByOrgSpecific(EntityManager entityManager, Long orgid, Long specificid) {
		List<TransactionItems> list = null;
		Query query = entityManager.createQuery(ORG_SPCFC_JPQL);
		query.setParameter(1, orgid);
		query.setParameter(2, specificid);
		list = query.getResultList();
		return list;
	}

	public static Double findByAdvanceRefund(EntityManager entityManager, Long orgid, Long branchId, String txnRef) {
		Double advRefund = 0.0;
		Query query = entityManager.createQuery(ORG_TXN_ADV_JSQL);
		query.setParameter(1, orgid);
		query.setParameter(2, branchId);
		query.setParameter(3, orgid);
		query.setParameter(4, branchId);
		query.setParameter(5, txnRef);
		advRefund = (Double) query.getSingleResult();
		return advRefund;
	}

	public static Double findByTDSAdvanceRefund(EntityManager entityManager, Long orgid, Long branchId, String txnRef) {
		Double tdsRefund = 0.0;
		Query query = entityManager.createQuery(ORG_TXN_TDS_JSQL);
		query.setParameter(1, orgid);
		query.setParameter(2, branchId);
		query.setParameter(3, orgid);
		query.setParameter(4, branchId);
		query.setParameter(5, txnRef);
		tdsRefund = (Double) query.getSingleResult();
		return tdsRefund;
	}

	public static Double findByAmountRefund(EntityManager entityManager, Long orgid, Long branchId, String txnRef) {
		Double amtRefund = 0.0;
		Query query = entityManager.createQuery(ORG_TXN_REFUND_JSQL);
		query.setParameter(1, orgid);
		query.setParameter(2, branchId);
		query.setParameter(3, txnRef);
		amtRefund = (Double) query.getSingleResult();
		return amtRefund;

	}

	public static Double findByTDSRefund(EntityManager entityManager, Long orgid, Long branchId, String txnRef) {
		Double tdsRefund = 0.0;
		Query query = entityManager.createQuery(ORG_TXN_REFUND_TDS_JSQL);
		query.setParameter(1, orgid);
		query.setParameter(2, branchId);
		query.setParameter(3, txnRef);
		tdsRefund = (Double) query.getSingleResult();
		return tdsRefund;
	}

	public static List<TransactionItems> findTransItemsForVendorDateRange(EntityManager entityManager, Long orgid,
			Long specId, Long vendId, Date toDate, Date fromDate) {
		List<TransactionItems> list = null;
		Query query = entityManager.createQuery(TDS_GROSS_QUERY);
		query.setParameter(1, orgid);
		query.setParameter(2, specId);
		query.setParameter(3, orgid);
		query.setParameter(4, vendId);
		query.setParameter(5, IdosConstants.TXN_STATUS_ACCOUNTED);
		query.setParameter(6, fromDate);
		query.setParameter(7, toDate);
		list = query.getResultList();
		return list;
	}
}
