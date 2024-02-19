package model;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.*;

import com.idos.util.DateUtil;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import model.internal.IdosAdminUser;
import org.hibernate.annotations.Cache;

import play.Logger;
import play.data.validation.Constraints;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.Query;

import pojo.OrganiationPage;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "ORGANIZATION")
public class Organization extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public Organization() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "name")
	private String name;

	@Column(name = "corporate_email", length = 256, unique = true, nullable = false)
	@Constraints.MaxLength(256)
	@Constraints.Required
	@Constraints.Email
	private String corporateMail;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "organization")
	private List<Branch> branches;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "organization")
	private List<Project> projects;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "organization")
	private List<Specifics> specifics;

	@Column(name = "company_logo")
	private String companyLogo;

	@Column(name = "registered_address")
	private String registeredAddress;

	@Column(name = "registered_phone_number_country_code")
	private String regPhNoCtryCode;

	@Column(name = "registered_phone_number")
	private String registeredPhoneNumber;

	@Column(name = "company_weburl")
	private String webUrl;

	@Column(name = "GST_COUNTRY_CODE")
	private String gstCountryCode;

	@Column(name = "currency")
	private String currency;

	@Column(name = "country")
	private Integer country;

	@Column(name = "financial_start_date")
	private Date financialStartDate;

	@Column(name = "financial_end_date")
	private Date financialEndDate;

	@Column(name = "audited_account_doc")
	private String auditedAccountDoc;

	@Column(name = "tax_return_doc")
	private String taxReturnDoc;

	@Column(name = "organization_chart_doc")
	private String organizationChartDoc;

	@Column(name = "accounting_manual_doc")
	private String accountingManualDoc;

	@Column(name = "signatorylist_doc")
	private String signatoryListDoc;

	@Column(name = "company_template_doc")
	private String companyTemplateDoc;

	@Column(name = "budget_date")
	private Date budgetDate;

	@Column(name = "budget_amount_jan")
	private Double budgetAmountJan;

	@Column(name = "budget_amount_feb")
	private Double budgetAmountFeb;

	@Column(name = "budget_amount_mar")
	private Double budgetAmountMar;

	@Column(name = "budget_amount_apr")
	private Double budgetAmountApr;

	@Column(name = "budget_amount_may")
	private Double budgetAmountMay;

	@Column(name = "budget_amount_june")
	private Double budgetAmountJune;

	@Column(name = "budget_amount_july")
	private Double budgetAmountJuly;

	@Column(name = "budget_amount_aug")
	private Double budgetAmountAug;

	@Column(name = "budget_amount_sep")
	private Double budgetAmountSep;

	@Column(name = "budget_amount_oct")
	private Double budgetAmountOct;

	@Column(name = "budget_amount_nov")
	private Double budgetAmountNov;

	@Column(name = "budget_amount_dec")
	private Double budgetAmountDec;

	@Column(name = "TRIAL_START_DATE")
	private Date trialStartDate;

	@Column(name = "TRIAL_END_DATE")
	private Date trialEndDate;

	@Column(name = "PAYMENT_CONSIDERATION_DAY_OF_MONTH")
	private Integer payConsiderationDayOfMonth;

	@Column(name = "AUTO_ITEM_REGISTRATION_ALLOWED")
	private Integer autoItemRegistrationAllowed = 0;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "organization")
	private List<Travel_Group> organizationTravelGroups;

	@Column(name = "CHART_OF_ACCOUNTS_UPLOAD_STATUS")
	private Integer chartOfAccountUploadStatus = 0;

	@Column(name = "PERSON_NAME")
	@Constraints.MaxLength(256)
	private String personName;

	@Column(name = "REGISTRATION_SOURCE")
	@Constraints.MaxLength(256)
	private String registrationSource;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "organization")
	private List<ExpenseGroup> organizationExpenseGroups;

	@Column(name = "company_has_chart_of_accounts")
	private Integer companyHasChartOfAccounts;

	@Column(name = "INVOICE_SERIAL")
	private Integer invoiceSerial;

	@Column(name = "PROFORMA_SERIAL")
	private Integer proformaSerial;

	@Column(name = "PURCHASE_ORDER_SERIAL")
	private Integer purchaseOrderSerial;

	@Column(name = "QUOTATION_SERIAL")
	private Integer quotationSerial;

	@Column(name = "RECEIPT_SERIAL")
	private Integer receiptSerial;

	@Column(name = "OFF_INVOICE_SERIAL")
	private Integer offInvoiceSerial;

	@Column(name = "OFF_PROFORMA_SERIAL")
	private Integer offProformaSerial;

	@Column(name = "OFF_PURCHASEORDER_SERIAL")
	private Integer offPurchaseOrderSerial;

	@Column(name = "OFF_QUOTATION_SERIAL")
	private Integer offQuotationSerial;

	@Column(name = "OFF_RECEIPT_SERIAL")
	private Integer offReceiptSerial;

	@Column(name = "INVOICE_INTERVAL")
	private Integer invoiceInterval;

	@Column(name = "PROFORMA_INTERVAL")
	private Integer proformaInterval;

	@Column(name = "QUOTATION_INTERVAL")
	private Integer quotationInterval;

	@Column(name = "RECEIPT_INTERVAL")
	private Integer receiptInterval;

	@Column(name = "ORG_LOGO")
	@Basic(fetch = FetchType.LAZY)
	@Lob
	public byte[] logo;

	@Column(name = "LOGO_FILE_NAME")
	public String logoMimeType;

	@Column(name = "LOGO_MIME_TYPE")
	public String logoFileName;

	@Column(name = "IS_COMPOSITION_SCHEME")
	private Integer isCompositionScheme;

	@Column(name = "ADVANCE_RECEIPT_SERIAL")
	private Integer advanceReceiptSerial;

	@Column(name = "ADVANCE_RECEIPT_INTERVAL")
	private Integer advanceReceiptInterval;

	@Column(name = "DEBIT_NOTE_CUST_SERIAL")
	private Integer debitNoteCustomerSerial;

	@Column(name = "DEBIT_NOTE_CUST_INTERVAL")
	private Integer debitNoteCustomerInterval;

	@Column(name = "CREDIT_NOTE_CUST_SERIAL")
	private Integer creditNoteCustomerSerial;

	@Column(name = "CREDIT_NOTE_CUST_INTERVAL")
	private Integer creditNoteCustomerInterval;

	@Column(name = "PURCHASE_ORDER_INTERVAL")
	private Integer purchaseOrderInterval;

	@Column(name = "ORG_SERIAL_GENRATION_TYPE")
	private Integer orgSerialGenrationType; // 1.Organization 2.GST

	@Column(name = "GSTIN_INTERVAL")
	private Integer gstInInterval;

	@Column(name = "PLACE_OF_SUPPLY_TYPE")
	private Integer placeOfSupplyType;

	@Column(name = "SERIAL_CURRENT_MONTH")
	private Integer serialCurrentMonth;

	@Column(name = "SERIAL_YEAR_CHANGED_DATE")
	private Date serialChangedDateYear;

	@Column(name = "REFUND_ADVANCE_RECEIPT_SERIAL")
	private Integer refundAdvanceReceiptSerial;

	@Column(name = "REFUND_ADVANCE_RECEIPT_INTERVAL")
	private Integer refundAdvanceReceiptInterval;

	@Column(name = "REFUND_AMOUNT_RECEIPT_SERIAL")
	private Integer refundAmounteReceiptSerial;

	@Column(name = "REFUND_AMOUNT_RECEIPT_INTERVAL")
	private Integer refundAmountReceiptInterval;

	@Column(name = "USER_MODE")
	private Integer userMode;

	@Column(name = "TDS_APPLICABLE_TRANSACTIONS")
	private String tdsApplicableTransactions;

	@Column(name = "DELIVERY_CHALLAN_SERIAL_NUMBER")
	private Integer deliveryChallanReceiptSerial;

	@Column(name = "DELIVER_CHALLAN_INTERVAL")
	private Integer deliverChallanReceiptInterval;

	@Column(name = "PAYMENT_VOUCHER_SERIAL_NUMBER")
	private Integer paymentVoucherSerial;

	@Column(name = "PAYMENT_VOUCHER_INTERVAL")
	private Integer paymentVoucherInterval;

	@Column(name = "LIMIT_FOR_BACKDATED_TXN")
	private Integer limitDaysForBackdatedTxn;

	@Column(name = "SELF_INVOICE_NUMBER")
	private Integer selfInvoice;

	@Column(name = "SELF_INVOICE_INTERVAL")
	private Integer selfInvoiceInterval;

	@Column(name = "CREATE_PURCHASE_ORDER_SERIAL")
	private Integer createPurchaseOrderSerial;

	@Column(name = "CREATE_PURCHASE_ORDER_INTERVAL")
	private Integer createPurchaseOrderInterval;
	@Column(name = "FILE_UPLOAD_DESTINATION")
	private Integer fileUploadDestination;

	@Column(name = "COMPANY_ID")
	private Integer companyId;

	public Integer getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}

	@OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Users> usersList;

	public Integer getDeliveryChallanReceiptSerial() {
		return deliveryChallanReceiptSerial;
	}

	public void setDeliveryChallanReceiptSerial(Integer deliveryChallanReceiptSerial) {
		this.deliveryChallanReceiptSerial = deliveryChallanReceiptSerial;
	}

	public Integer getDeliverChallanReceiptInterval() {
		return deliverChallanReceiptInterval;
	}

	public void setDeliverChallanReceiptInterval(Integer deliverChallanReceiptInterval) {
		this.deliverChallanReceiptInterval = deliverChallanReceiptInterval;
	}

	public Integer getIsCompositionScheme() {
		return isCompositionScheme;
	}

	public void setIsCompositionScheme(Integer isCompositionScheme) {
		this.isCompositionScheme = isCompositionScheme;
	}

	public String getPersonName() {
		return this.personName;
	}

	public void setPersonName(String personName) {
		this.personName = IdosUtil.escapeHtml(personName);
	}

	public String getRegistrationSource() {
		return this.registrationSource;
	}

	public void setRegistrationSource(String source) {
		this.registrationSource = IdosUtil.escapeHtml(source);
	}

	public Integer getChartOfAccountUploadStatus() {
		return chartOfAccountUploadStatus;
	}

	public void setChartOfAccountUploadStatus(Integer chartOfAccountUploadStatus) {
		this.chartOfAccountUploadStatus = chartOfAccountUploadStatus;
	}

	public List<Travel_Group> getOrganizationTravelGroups() {
		return organizationTravelGroups;
	}

	public void setOrganizationTravelGroups(
			List<Travel_Group> organizationTravelGroups) {
		this.organizationTravelGroups = organizationTravelGroups;
	}

	public List<ExpenseGroup> getOrganizationExpenseGroups() {
		return organizationExpenseGroups;
	}

	public void setOrganizationExpenseGroups(
			List<ExpenseGroup> organizationExpenseGroups) {
		this.organizationExpenseGroups = organizationExpenseGroups;
	}

	public Integer getAutoItemRegistrationAllowed() {
		return autoItemRegistrationAllowed;
	}

	public void setAutoItemRegistrationAllowed(Integer autoItemRegistrationAllowed) {
		this.autoItemRegistrationAllowed = autoItemRegistrationAllowed;
	}

	public Date getTrialStartDate() {
		return trialStartDate;
	}

	public void setTrialStartDate(Date trialStartDate) {
		this.trialStartDate = trialStartDate;
	}

	public Date getTrialEndDate() {
		return trialEndDate;
	}

	public void setTrialEndDate(Date trialEndDate) {
		this.trialEndDate = trialEndDate;
	}

	public Integer getPayConsiderationDayOfMonth() {
		return payConsiderationDayOfMonth;
	}

	public void setPayConsiderationDayOfMonth(Integer payConsiderationDayOfMonth) {
		this.payConsiderationDayOfMonth = payConsiderationDayOfMonth;
	}

	public Date getBudgetDate() {
		return budgetDate;
	}

	public void setBudgetDate(Date budgetDate) {
		this.budgetDate = budgetDate;
	}

	public Double getBudgetAmountJan() {
		return budgetAmountJan;
	}

	public void setBudgetAmountJan(Double budgetAmountJan) {
		this.budgetAmountJan = budgetAmountJan;
	}

	public Double getBudgetAmountFeb() {
		return budgetAmountFeb;
	}

	public void setBudgetAmountFeb(Double budgetAmountFeb) {
		this.budgetAmountFeb = budgetAmountFeb;
	}

	public Double getBudgetAmountMar() {
		return budgetAmountMar;
	}

	public void setBudgetAmountMar(Double budgetAmountMar) {
		this.budgetAmountMar = budgetAmountMar;
	}

	public Double getBudgetAmountApr() {
		return budgetAmountApr;
	}

	public void setBudgetAmountApr(Double budgetAmountApr) {
		this.budgetAmountApr = budgetAmountApr;
	}

	public Double getBudgetAmountMay() {
		return budgetAmountMay;
	}

	public void setBudgetAmountMay(Double budgetAmountMay) {
		this.budgetAmountMay = budgetAmountMay;
	}

	public Double getBudgetAmountJune() {
		return budgetAmountJune;
	}

	public void setBudgetAmountJune(Double budgetAmountJune) {
		this.budgetAmountJune = budgetAmountJune;
	}

	public Double getBudgetAmountJuly() {
		return budgetAmountJuly;
	}

	public void setBudgetAmountJuly(Double budgetAmountJuly) {
		this.budgetAmountJuly = budgetAmountJuly;
	}

	public Double getBudgetAmountAug() {
		return budgetAmountAug;
	}

	public void setBudgetAmountAug(Double budgetAmountAug) {
		this.budgetAmountAug = budgetAmountAug;
	}

	public Double getBudgetAmountSep() {
		return budgetAmountSep;
	}

	public void setBudgetAmountSep(Double budgetAmountSep) {
		this.budgetAmountSep = budgetAmountSep;
	}

	public Double getBudgetAmountOct() {
		return budgetAmountOct;
	}

	public void setBudgetAmountOct(Double budgetAmountOct) {
		this.budgetAmountOct = budgetAmountOct;
	}

	public Double getBudgetAmountNov() {
		return budgetAmountNov;
	}

	public void setBudgetAmountNov(Double budgetAmountNov) {
		this.budgetAmountNov = budgetAmountNov;
	}

	public Double getBudgetAmountDec() {
		return budgetAmountDec;
	}

	public void setBudgetAmountDec(Double budgetAmountDec) {
		this.budgetAmountDec = budgetAmountDec;
	}

	public Date getFinancialStartDate() {
		return financialStartDate;
	}

	public void setFinancialStartDate(Date financialStartDate) {
		this.financialStartDate = financialStartDate;
	}

	public Date getFinancialEndDate() {
		return financialEndDate;
	}

	public void setFinancialEndDate(Date financialEndDate) {
		this.financialEndDate = financialEndDate;
	}

	public String getAuditedAccountDoc() {
		return auditedAccountDoc;
	}

	public void setAuditedAccountDoc(String auditedAccountDoc) {
		this.auditedAccountDoc = auditedAccountDoc;
	}

	public String getTaxReturnDoc() {
		return taxReturnDoc;
	}

	public void setTaxReturnDoc(String taxReturnDoc) {
		this.taxReturnDoc = taxReturnDoc;
	}

	public String getOrganizationChartDoc() {
		return organizationChartDoc;
	}

	public void setOrganizationChartDoc(String organizationChartDoc) {
		this.organizationChartDoc = organizationChartDoc;
	}

	public String getAccountingManualDoc() {
		return accountingManualDoc;
	}

	public void setAccountingManualDoc(String accountingManualDoc) {
		this.accountingManualDoc = accountingManualDoc;
	}

	public String getSignatoryListDoc() {
		return signatoryListDoc;
	}

	public void setSignatoryListDoc(String signatoryListDoc) {
		this.signatoryListDoc = signatoryListDoc;
	}

	public String getCompanyTemplateDoc() {
		return companyTemplateDoc;
	}

	public void setCompanyTemplateDoc(String companyTemplateDoc) {
		this.companyTemplateDoc = companyTemplateDoc;
	}

	public List<Specifics> getSpecifics() {
		return specifics;
	}

	public void setSpecifics(List<Specifics> specifics) {
		this.specifics = specifics;
	}

	public List<Project> getProjects() {
		return projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	public String getCurrency() {
		return this.currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Integer getCountry() {
		return this.country;
	}

	public void setCountry(Integer country) {
		this.country = country;
	}

	public String getCorporateMail() {
		return corporateMail;
	}

	public void setCorporateMail(String corporateMail) {
		this.corporateMail = corporateMail;
	}

	public Integer getCompanyHasChartOfAccounts() {
		return companyHasChartOfAccounts;
	}

	public void setCompanyHasChartOfAccounts(Integer companyHasChartOfAccounts) {
		this.companyHasChartOfAccounts = companyHasChartOfAccounts;
	}

	public String getCompanyLogo() {
		return companyLogo;
	}

	public void setCompanyLogo(String companyLogo) {
		this.companyLogo = companyLogo;
	}

	public String getRegisteredAddress() {
		return registeredAddress;
	}

	public void setRegisteredAddress(String registeredAddress) {
		this.registeredAddress = IdosUtil.escapeHtml(registeredAddress);
	}

	public String getRegisteredPhoneNumber() {
		return registeredPhoneNumber;
	}

	public void setRegisteredPhoneNumber(String registeredPhoneNumber) {
		this.registeredPhoneNumber = registeredPhoneNumber;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public void setWebUrl(String webUrl) {
		this.webUrl = IdosUtil.escapeHtml(webUrl);
	}

	public List<Branch> getBranches() {
		return branches;
	}

	public void setBranches(List<Branch> branches) {
		this.branches = branches;
	}

	public String getName() {
		return IdosUtil.unescapeHtml(name);
	}

	public void setName(String name) {
		this.name = IdosUtil.escapeHtml(name);
	}

	public String getRegPhNoCtryCode() {
		return regPhNoCtryCode;
	}

	public void setRegPhNoCtryCode(String regPhNoCtryCode) {
		this.regPhNoCtryCode = regPhNoCtryCode;
	}

	/**
	 * Find an organization by id.
	 */
	public static Organization findById(Long id) {
		return entityManager.find(Organization.class, id);
	}

	/**
	 * list of organization.
	 */
	public static List<Organization> getAll(EntityManager entityManager) {
		String sbquery = new String("select obj from Organization obj where obj.presentStatus=1");
		List<Organization> orgs = entityManager.createQuery(sbquery).getResultList();
		return orgs;
	}

	public Integer getInvoiceSerial() {
		return this.invoiceSerial;
	}

	public void setInvoiceSerial(Integer invoiceSerial) {
		this.invoiceSerial = invoiceSerial;
	}

	public Integer getProformaSerial() {
		return this.proformaSerial;
	}

	public void setProformaSerial(Integer proformaSerial) {
		this.proformaSerial = proformaSerial;
	}

	public Integer getQuotationSerial() {
		return this.quotationSerial;
	}

	public void setQuotationSerial(Integer quotationSerial) {
		this.quotationSerial = quotationSerial;
	}

	public Integer getOffInvoiceSerial() {
		return this.offInvoiceSerial;
	}

	public void setOffInvoiceSerial(Integer offInvoiceSerial) {
		this.offInvoiceSerial = offInvoiceSerial;
	}

	public Integer getOffProformaSerial() {
		return this.offProformaSerial;
	}

	public void setOffProformaSerial(Integer offProformaSerial) {
		this.offProformaSerial = offProformaSerial;
	}

	public Integer getOffQuotationSerial() {
		return this.offQuotationSerial;
	}

	public void setOffQuotationSerial(Integer offQuotationSerial) {
		this.offQuotationSerial = offQuotationSerial;
	}

	public Integer getReceiptSerial() {
		return this.receiptSerial;
	}

	public void setReceiptSerial(Integer receiptSerial) {
		this.receiptSerial = receiptSerial;
	}

	public Integer getOffReceiptSerial() {
		return this.offReceiptSerial;
	}

	public void setOffReceiptSerial(Integer offReceiptSerial) {
		this.offReceiptSerial = offReceiptSerial;
	}

	public Integer getInvoiceInterval() {
		return this.invoiceInterval;
	}

	public void setInvoiceInterval(Integer invoiceInterval) {
		this.invoiceInterval = invoiceInterval;
	}

	public Integer getProformaInterval() {
		return this.proformaInterval;
	}

	public void setProformaInterval(Integer proformaInterval) {
		this.proformaInterval = proformaInterval;
	}

	public Integer getQuotationInterval() {
		return this.quotationInterval;
	}

	public void setQuotationInterval(Integer quotationInterval) {
		this.quotationInterval = quotationInterval;
	}

	public Integer getReceiptInterval() {
		return this.receiptInterval;
	}

	public void setReceiptInterval(Integer receiptInterval) {
		this.receiptInterval = receiptInterval;
	}

	public byte[] getLogo() {
		return this.logo;
	}

	public void setLogo(byte[] logo) {
		this.logo = logo;
	}

	public String getLogoMimeType() {
		return this.logoMimeType;
	}

	public void setLogoMimeType(String logoMimeType) {
		this.logoMimeType = logoMimeType;
	}

	public String getLogoFileName() {
		return this.logoFileName;
	}

	public void setLogoFileName(String logoFileName) {
		this.logoFileName = logoFileName;
	}

	public String getGstCountryCode() {
		return gstCountryCode;
	}

	public void setGstCountryCode(String gstCountryCode) {
		this.gstCountryCode = gstCountryCode;
	}

	public Integer getPurchaseOrderSerial() {
		return purchaseOrderSerial;
	}

	public void setPurchaseOrderSerial(Integer purchaseOrderSerial) {
		this.purchaseOrderSerial = purchaseOrderSerial;
	}

	public Integer getOffPurchaseOrderSerial() {
		return offPurchaseOrderSerial;
	}

	public void setOffPurchaseOrderSerial(Integer offPurchaseOrderSerial) {
		this.offPurchaseOrderSerial = offPurchaseOrderSerial;
	}

	public Integer getAdvanceReceiptInterval() {
		return advanceReceiptInterval;
	}

	public Integer getPlaceOfSupplyType() {
		return this.placeOfSupplyType;
	}

	public void setPlaceOfSupplyType(Integer placeOfSupplyType) {
		this.placeOfSupplyType = placeOfSupplyType;
	}

	public Integer getAdvanceReceiptSerial() {
		return advanceReceiptSerial;
	}

	public void setAdvanceReceiptSerial(Integer advanceReceiptSerial) {
		this.advanceReceiptSerial = advanceReceiptSerial;
	}

	public Integer getDebitNoteCustomerSerial() {
		return debitNoteCustomerSerial;
	}

	public void setDebitNoteCustomerSerial(Integer debitNoteCustomerSerial) {
		this.debitNoteCustomerSerial = debitNoteCustomerSerial;
	}

	public Integer getDebitNoteCustomerInterval() {
		return debitNoteCustomerInterval;
	}

	public void setDebitNoteCustomerInterval(Integer debitNoteCustomerInterval) {
		this.debitNoteCustomerInterval = debitNoteCustomerInterval;
	}

	public Integer getCreditNoteCustomerSerial() {
		return creditNoteCustomerSerial;
	}

	public void setCreditNoteCustomerSerial(Integer creditNoteCustomerSerial) {
		this.creditNoteCustomerSerial = creditNoteCustomerSerial;
	}

	public Integer getCreditNoteCustomerInterval() {
		return creditNoteCustomerInterval;
	}

	public void setCreditNoteCustomerInterval(Integer creditNoteCustomerInterval) {
		this.creditNoteCustomerInterval = creditNoteCustomerInterval;
	}

	public Integer getPurchaseOrderInterval() {
		return purchaseOrderInterval;
	}

	public void setPurchaseOrderInterval(Integer purchaseOrderInterval) {
		this.purchaseOrderInterval = purchaseOrderInterval;
	}

	public void setAdvanceReceiptInterval(Integer advanceReceiptInterval) {
		this.advanceReceiptInterval = advanceReceiptInterval;
	}

	public Integer getOrgSerialGenrationType() {
		return orgSerialGenrationType;
	}

	public void setOrgSerialGenrationType(Integer orgSerialGenrationType) {
		this.orgSerialGenrationType = orgSerialGenrationType;
	}

	public Integer getGstInInterval() {
		return gstInInterval;
	}

	public void setGstInInterval(Integer gstInInterval) {
		this.gstInInterval = gstInInterval;
	}

	public Integer getSerialCurrentMonth() {
		return serialCurrentMonth;
	}

	public void setSerialCurrentMonth(Integer serialCurrentMonth) {
		this.serialCurrentMonth = serialCurrentMonth;
	}

	public Date getSerialChangedDateYear() {
		return serialChangedDateYear;
	}

	public void setSerialChangedDateYear(Date serialChangedDateYear) {
		this.serialChangedDateYear = serialChangedDateYear;
	}

	public Integer getRefundAdvanceReceiptInterval() {
		return refundAdvanceReceiptInterval;
	}

	public void setRefundAdvanceReceiptInterval(Integer refundAdvanceReceiptInterval) {
		this.refundAdvanceReceiptInterval = refundAdvanceReceiptInterval;
	}

	public Integer getRefundAdvanceReceiptSerial() {
		return refundAdvanceReceiptSerial;
	}

	public void setRefundAdvanceReceiptSerial(Integer refundAdvanceReceiptSerial) {
		this.refundAdvanceReceiptSerial = refundAdvanceReceiptSerial;
	}

	public Integer getRefundAmounteReceiptSerial() {
		return refundAmounteReceiptSerial;
	}

	public void setRefundAmounteReceiptSerial(Integer refundAmounteReceiptSerial) {
		this.refundAmounteReceiptSerial = refundAmounteReceiptSerial;
	}

	public Integer getRefundAmountReceiptInterval() {
		return refundAmountReceiptInterval;
	}

	public void setRefundAmountReceiptInterval(Integer refundAmountReceiptInterval) {
		this.refundAmountReceiptInterval = refundAmountReceiptInterval;
	}

	public Integer getUserMode() {
		return userMode;
	}

	public void setUserMode(Integer userMode) {
		this.userMode = userMode;
	}

	public String getTdsApplicableTransactions() {
		return tdsApplicableTransactions;
	}

	public void setTdsApplicableTransactions(String tdsApplicableTransactions) {
		this.tdsApplicableTransactions = tdsApplicableTransactions;
	}

	public Integer getLimitDaysForBackdatedTxn() {
		return limitDaysForBackdatedTxn;
	}

	public void setLimitDaysForBackdatedTxn(Integer limitDaysForBackdatedTxn) {
		this.limitDaysForBackdatedTxn = limitDaysForBackdatedTxn;
	}

	public Integer getPaymentVoucherSerial() {
		return paymentVoucherSerial;
	}

	public void setPaymentVoucherSerial(Integer paymentVoucherSerial) {
		this.paymentVoucherSerial = paymentVoucherSerial;
	}

	public Integer getPaymentVoucherInterval() {
		return paymentVoucherInterval;
	}

	public void setPaymentVoucherInterval(Integer paymentVoucherInterval) {
		this.paymentVoucherInterval = paymentVoucherInterval;
	}

	public Integer getSelfInvoice() {
		return selfInvoice;
	}

	public void setSelfInvoice(Integer selfInvoice) {
		this.selfInvoice = selfInvoice;
	}

	public Integer getSelfInvoiceInterval() {
		return selfInvoiceInterval;
	}

	public void setSelfInvoiceInterval(Integer selfInvoiceInterval) {
		this.selfInvoiceInterval = selfInvoiceInterval;
	}

	public Integer getCreatePurchaseOrderSerial() {
		return createPurchaseOrderSerial;
	}

	public void setCreatePurchaseOrderSerial(Integer createPurchaseOrderSerial) {
		this.createPurchaseOrderSerial = createPurchaseOrderSerial;
	}

	public Integer getCreatePurchaseOrderInterval() {
		return createPurchaseOrderInterval;
	}

	public void setCreatePurchaseOrderInterval(Integer createPurchaseOrderInterval) {
		this.createPurchaseOrderInterval = createPurchaseOrderInterval;
	}

	/***************** Below code is for paging *****************************/

	public Long getDaysSinceRegiserter() {
		if (this.getCreatedAt() != null)
			return (long) ((new Date().getTime() - this.getCreatedAt().getTime()) / (1000 * 60 * 60 * 24));
		else
			return null;
	}

	public Integer getRegisertedUsersCount() {
		List<Users> usersList = Users.getAllUsersByOrganization(entityManager, this);
		return usersList.size();
	}

	public String getAccoutTypeDetail() {
		String type = null;
		IdosAdminUser idosAdminUser = IdosAdminUser.findByOrganization(entityManager, this.getId());
		if (idosAdminUser != null) {
			if (idosAdminUser.getAccountType() == 1) {
				type = "Super User (" + idosAdminUser.getActiveForDays() + ")";
			} else if (idosAdminUser.getAccountType() == 2) {
				type = "Demo (" + idosAdminUser.getActiveForDays() + ")";
			} else {
				type = "";
			}
		}
		return type;
	}

	public int getAccoutType(EntityManager entityManager) {
		int type = -1;
		IdosAdminUser idosAdminUser = IdosAdminUser.findByOrganization(entityManager, this.getId());
		if (idosAdminUser != null) {
			type = idosAdminUser.getAccountType();
		}
		return type;
	}

	public boolean isAccountExpired() {
		boolean expireIndays = false;
		IdosAdminUser idosAdminUser = IdosAdminUser.findByOrganization(entityManager, this.getId());
		if (idosAdminUser != null) {
			if (idosAdminUser.getExpiryRemainingDays() > idosAdminUser.getActiveForDays()) {
				expireIndays = true;
			}
		}
		return expireIndays;
	}

	public String getStartDate() {
		String expiryDate = null;
		IdosAdminUser idosAdminUser = IdosAdminUser.findByOrganization(entityManager, this.getId());
		if (idosAdminUser != null) {
			expiryDate = IdosConstants.IDOSDTF.format(idosAdminUser.getModifiedAt());
		}
		return expiryDate;
	}

	public Integer getFileUploadDestination() {
		return this.fileUploadDestination;
	}

	public void setFileUploadDestination(final Integer fileUploadDestination) {
		this.fileUploadDestination = fileUploadDestination;
	}

	/**
	 * Return a page of Users
	 *
	 * @param page     Page to display
	 * @param pageSize Number of computers per page
	 * @param sortBy   Computer property used for sorting
	 * @param order    Sort order (either or asc or desc)
	 * @param filter   Filter applied on the name column
	 */
	public static OrganiationPage page(EntityManager entityManager, int page, int pageSize, String sortBy, String order,
			String filter) {
		if (page < 1)
			page = 1;
		Long total = (Long) entityManager
				.createQuery("select count(c) from Organization c where lower(c.name) like ? and c.presentStatus=1")
				.setParameter(1, "%" + filter.toLowerCase() + "%")
				.getSingleResult();

		String sqlQuery = "select c from Organization c where lower(c.name) like ? and c.presentStatus=1 order by c.? ?";
		Query query = entityManager.createQuery(sqlQuery);
		query.setParameter(1, "%" + filter.toLowerCase() + "%");
		query.setParameter(2, sortBy);
		query.setParameter(3, order);
		query.setFirstResult((page - 1) * pageSize);
		query.setMaxResults(pageSize);
		List<Organization> data = query.getResultList();
		OrganiationPage up = new OrganiationPage(total, page, pageSize);
		up.setList(data);
		return up;
	}

	public void update(Long id) {
		Organization org = Organization.findById(id);
		entityManager.merge(org);
	}

	public void save() {
		// Users user = Users.findById(id);
		entityManager.persist(this);
	}

	public void delete() {
		entityManager.remove(this);
	}

	public static Map<String, String> options(EntityManager entityManager) {
		List<Organization> companies = entityManager.createQuery("from Organization order by id desc").getResultList();
		LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
		for (Organization c : companies) {
			options.put(c.id.toString(), c.corporateMail);
		}
		return options;
	}

	public List<Users> getUsersList() {
		return this.usersList;
	}

	public void setUsersList(final List<Users> usersList) {
		this.usersList = usersList;
	}

	public String getCompanyLogoName(Integer companyId) {
		String path = null;
		Long id = Long.valueOf(companyId);
		try {
			String queryStr = "select companyLogo from Organization where id = ?1 and presentStatus = 1";
			Query query = entityManager.createQuery(queryStr);
			query.setParameter(1, id);
			path = (String) query.getSingleResult();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return path;
	}

	public String getCompanySuperAdmin(Integer companyId) {
		String name = null;
		Long id = Long.valueOf(companyId);
		try {
			String queryStr = "select name from Organization where id = ?1 and presentStatus = 1";
			Query query = entityManager.createQuery(queryStr);
			query.setParameter(1, id);
			name = (String) query.getSingleResult();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return name;
	}

	public static List<Organization> findByCompanyId(EntityManager entityManager, Integer companyId) {
		List<Organization> companyOrgList = null;
		if (companyId == null) {
			return companyOrgList;
		}
		String queryStr = "select obj from Organization obj where obj.companyId = ?1 and obj.presentStatus = 1 order by id";
		Query query = entityManager.createQuery(queryStr);
		query.setParameter(1, companyId);
		companyOrgList = query.getResultList();
		return companyOrgList;
	}
}
