package model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.*;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import com.idos.util.IdosUtil;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "BRANCH")
public class Branch extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public Branch() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String ORG_BRANCH_LIKE_HQL = "select obj from Branch obj WHERE obj.organization.id=?1 and obj.name like ?2 and obj.presentStatus=1";
	private static final String ORG_BRANCH_EQUL_HQL = "select obj from Branch obj WHERE obj.organization.id=?1 and upper(obj.name) = ?2 and obj.presentStatus=1";
	private static final String BYNAME_JPQL = "select obj from Branch obj where obj.organization.id= ?1 and upper(name) = ?2 and obj.presentStatus=1";

	private static final String ORG_BRANCH_GSTIN_HQL = "select distinct obj.gstin from Branch obj WHERE obj.organization.id=?1 and obj.presentStatus=1";
	@Column(name = "name")
	public String name;

	@Column(name = "branch_head")
	private String branchHead;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@Column(name = "is_headquarter")
	private Integer isHeadQuarter;

	@Column(name = "location")
	private String location;

	@Column(name = "address")
	private String address;

	@Column(name = "country")
	private Integer country;

	public Integer getCountry() {
		return country;
	}

	public void setCountry(Integer country) {
		this.country = country;
	}

	@Column(name = "phone_number_country_code")
	private String phoneNumberCtryCode;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "branch_open_date")
	private Date branchOpenDate;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "branch")
	private List<BranchCashCount> branchCashCount;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "branch")
	private List<BranchDepositBoxKey> branchDepositKeys;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "branch")
	private List<BranchTaxes> branchTaxes;

	public List<BranchTaxes> getBranchTaxes() {
		return branchTaxes;
	}

	public void setBranchTaxes(List<BranchTaxes> branchTaxes) {
		this.branchTaxes = branchTaxes;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "branch")
	private List<StatutoryDetails> branchStatutoryDetails;

	public List<StatutoryDetails> getBranchStatutoryDetails() {
		return branchStatutoryDetails;
	}

	public void setBranchStatutoryDetails(
			List<StatutoryDetails> branchStatutoryDetails) {
		this.branchStatutoryDetails = branchStatutoryDetails;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "branch")
	private List<OrganizationOperationalRemainders> branchOperationAlerts;

	public List<OrganizationOperationalRemainders> getBranchOperationAlerts() {
		return branchOperationAlerts;
	}

	public void setBranchOperationAlerts(
			List<OrganizationOperationalRemainders> branchOperationAlerts) {
		this.branchOperationAlerts = branchOperationAlerts;
	}

	@Column(name = "branch_facility")
	private Integer branchFacility;

	@Column(name = "aggreement")
	private String aggreement;

	@Column(name = "aggreement_valid_from")
	private Date aggreementValidFrom;

	@Column(name = "aggreement_valid_to")
	private Date aggreementValidTo;

	public Date getAggreementValidFrom() {
		return aggreementValidFrom;
	}

	public void setAggreementValidFrom(Date aggreementValidFrom) {
		this.aggreementValidFrom = aggreementValidFrom;
	}

	public Date getAggreementValidTo() {
		return aggreementValidTo;
	}

	public void setAggreementValidTo(Date aggreementValidTo) {
		this.aggreementValidTo = aggreementValidTo;
	}

	@Column(name = "branch_has_depositbox")
	private Integer branchHasDepositBox;

	@Column(name = "IS_BRANCH_ASSET_INSURED")
	private Integer branchAssetsInsured;

	@Column(name = "no_of_keys")
	private Integer noOfKeys;

	@Column(name = "currency")
	private String currency;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "branch")
	List<BranchSpecifics> branchSpecifics;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	private List<BranchUsers> branchUsers;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "branch")
	private List<BranchKeyOfficials> branchKeyOfficials;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "branch")
	private List<OrganizationKeyOfficials> branchOfficers;

	public List<OrganizationKeyOfficials> getBranchOfficers() {
		return branchOfficers;
	}

	public void setBranchOfficers(List<OrganizationKeyOfficials> branchOfficers) {
		this.branchOfficers = branchOfficers;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "branch")
	private List<BranchInsurance> branchInsurance;

	@Column(name = "cin_registration_number")
	private String cinRegNumber;

	@Column(name = "cin_registration_url")
	private String cinRegUrl;

	@Column(name = "cin_registration_validfrom")
	private Date cinRegValidFrom;

	@Column(name = "cin_registration_validto")
	private Date cinRegValidTo;

	@Column(name = "tin_registration_number")
	private String tinRegNumber;

	@Column(name = "tin_registration_url")
	private String tinRegUrl;

	@Column(name = "tin_registration_validfrom")
	private Date tinRegValidFrom;

	@Column(name = "tin_registration_validto")
	private Date tinRegValidTo;

	@Column(name = "vat_registration_number")
	private String vatRegNumber;

	@Column(name = "vat_registration_url")
	private String vatRegUrl;

	@Column(name = "vat_registration_validfrom")
	private Date vatRegValidFrom;

	@Column(name = "vat_registration_validto")
	private Date vatRegValidTo;

	@Column(name = "salestax_registration_number")
	private String salestaxRegNumber;

	@Column(name = "salestax_registration_url")
	private String salestaxRegUrl;

	@Column(name = "salestax_registration_validfrom")
	private Date salestaxRegValidFrom;

	@Column(name = "salestax_registration_validto")
	private Date salestaxRegValidTo;

	@Column(name = "filing_vat_returns")
	private Date filingVatReturns;

	@Column(name = "filing_tds_returns")
	private Date filingTdsReturns;

	@Column(name = "pay_state_taxes")
	private Date payStateTaxes;

	@Column(name = "pay_municipal_taxes")
	private Date payMunicipalTaxes;

	@Column(name = "submitting_mis_reports")
	private Date submittingMisReports;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "branch")
	List<BranchVendors> branchVendors;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "branch")
	List<BranchBankAccounts> branchBankAccounts;

	@Column(name = "periodicity_of_payment")
	private Integer periodicityOfPayment;

	@Column(name = "rent_payable")
	private Double rentPayable;

	@Column(name = "landlord_name")
	private String landlordName;

	@Column(name = "landlord_address")
	private String landlordAddress;

	@Column(name = "bank_account_name")
	private String bankAccountName;

	@Column(name = "bank_account_number")
	private String bankAccountNumber;

	@Column(name = "bank_account_branch")
	private String bankAccountBranch;

	@Column(name = "rent_revised_due_on")
	private Date rentRevisedDueOn;

	@Column(name = "ALERT_FOR_ACTION")
	private String alertForAction;

	@Column(name = "ALERT_FOR_INFORMATION")
	private String alertForInformation;

	@Column(name = "LAST_UPDATED_VALIDITY_DATE")
	private Date lastUpdatedValidityDate;

	@Column(name = "LAST_UPDATED_RENT_REVISION_DATE")
	private Date lastUpdatedRentRevisionDate;

	@Column(name = "LAST_RENT_PAID_DATE")
	private Date lastRentPaidDate;

	@Column(name = "LAST_RENT_PAID_DUE_DATED")
	private Date lastRentPaidDueDated;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "projectBranch")
	private List<ProjectBranches> branchProject;

	@Column(name = "remarks")
	private String remarks;

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

	@Column(name = "total")
	private Double budgetTotal;

	@Column(name = "budget_deducted_amount_jan")
	private Double budgetDeductedAmountJan;

	@Column(name = "budget_deducted_amount_feb")
	private Double budgetDeductedAmountFeb;

	@Column(name = "budget_deducted_amount_mar")
	private Double budgetDeductedAmountMar;

	@Column(name = "budget_deducted_amount_apr")
	private Double budgetDeductedAmountApr;

	@Column(name = "budget_deducted_amount_may")
	private Double budgetDeductedAmountMay;

	@Column(name = "budget_deducted_amount_june")
	private Double budgetDeductedAmountJune;

	@Column(name = "budget_deducted_amount_july")
	private Double budgetDeductedAmountJuly;

	@Column(name = "budget_deducted_amount_aug")
	private Double budgetDeductedAmountAug;

	@Column(name = "budget_deducted_amount_sep")
	private Double budgetDeductedAmountSep;

	@Column(name = "budget_deducted_amount_oct")
	private Double budgetDeductedAmountOct;

	@Column(name = "budget_deducted_amount_nov")
	private Double budgetDeductedAmountNov;

	@Column(name = "budget_deducted_amount_dec")
	private Double budgetDeductedAmountDec;

	@Column(name = "total_deducted")
	private Double budgetDeductedTotal;

	@Column(name = "LAST_PAYMENT_MADE")
	private Date lastPaymentDate;

	@Column(name = "STATE_CODE")
	private String stateCode;

	@Column(name = "GSTIN")
	private String gstin;

	public Date getLastRentPaidDueDated() {
		return lastRentPaidDueDated;
	}

	public void setLastRentPaidDueDated(Date lastRentPaidDueDated) {
		this.lastRentPaidDueDated = lastRentPaidDueDated;
	}

	public Date getLastUpdatedValidityDate() {
		return lastUpdatedValidityDate;
	}

	public void setLastUpdatedValidityDate(Date lastUpdatedValidityDate) {
		this.lastUpdatedValidityDate = lastUpdatedValidityDate;
	}

	public Date getLastUpdatedRentRevisionDate() {
		return lastUpdatedRentRevisionDate;
	}

	public void setLastUpdatedRentRevisionDate(Date lastUpdatedRentRevisionDate) {
		this.lastUpdatedRentRevisionDate = lastUpdatedRentRevisionDate;
	}

	public Date getLastRentPaidDate() {
		return lastRentPaidDate;
	}

	public void setLastRentPaidDate(Date lastRentPaidDate) {
		this.lastRentPaidDate = lastRentPaidDate;
	}

	public String getAlertForAction() {
		return alertForAction;
	}

	public void setAlertForAction(String alertForAction) {
		this.alertForAction = alertForAction;
	}

	public String getAlertForInformation() {
		return alertForInformation;
	}

	public void setAlertForInformation(String alertForInformation) {
		this.alertForInformation = alertForInformation;
	}

	public Double getBudgetDeductedAmountJan() {
		return budgetDeductedAmountJan;
	}

	public void setBudgetDeductedAmountJan(Double budgetDeductedAmountJan) {
		this.budgetDeductedAmountJan = budgetDeductedAmountJan;
	}

	public Double getBudgetDeductedAmountFeb() {
		return budgetDeductedAmountFeb;
	}

	public void setBudgetDeductedAmountFeb(Double budgetDeductedAmountFeb) {
		this.budgetDeductedAmountFeb = budgetDeductedAmountFeb;
	}

	public Double getBudgetDeductedAmountMar() {
		return budgetDeductedAmountMar;
	}

	public void setBudgetDeductedAmountMar(Double budgetDeductedAmountMar) {
		this.budgetDeductedAmountMar = budgetDeductedAmountMar;
	}

	public Double getBudgetDeductedAmountApr() {
		return budgetDeductedAmountApr;
	}

	public void setBudgetDeductedAmountApr(Double budgetDeductedAmountApr) {
		this.budgetDeductedAmountApr = budgetDeductedAmountApr;
	}

	public Double getBudgetDeductedAmountMay() {
		return budgetDeductedAmountMay;
	}

	public void setBudgetDeductedAmountMay(Double budgetDeductedAmountMay) {
		this.budgetDeductedAmountMay = budgetDeductedAmountMay;
	}

	public Double getBudgetDeductedAmountJune() {
		return budgetDeductedAmountJune;
	}

	public void setBudgetDeductedAmountJune(Double budgetDeductedAmountJune) {
		this.budgetDeductedAmountJune = budgetDeductedAmountJune;
	}

	public Double getBudgetDeductedAmountJuly() {
		return budgetDeductedAmountJuly;
	}

	public void setBudgetDeductedAmountJuly(Double budgetDeductedAmountJuly) {
		this.budgetDeductedAmountJuly = budgetDeductedAmountJuly;
	}

	public Double getBudgetDeductedAmountAug() {
		return budgetDeductedAmountAug;
	}

	public void setBudgetDeductedAmountAug(Double budgetDeductedAmountAug) {
		this.budgetDeductedAmountAug = budgetDeductedAmountAug;
	}

	public Double getBudgetDeductedAmountSep() {
		return budgetDeductedAmountSep;
	}

	public void setBudgetDeductedAmountSep(Double budgetDeductedAmountSep) {
		this.budgetDeductedAmountSep = budgetDeductedAmountSep;
	}

	public Double getBudgetDeductedAmountOct() {
		return budgetDeductedAmountOct;
	}

	public void setBudgetDeductedAmountOct(Double budgetDeductedAmountOct) {
		this.budgetDeductedAmountOct = budgetDeductedAmountOct;
	}

	public Double getBudgetDeductedAmountNov() {
		return budgetDeductedAmountNov;
	}

	public void setBudgetDeductedAmountNov(Double budgetDeductedAmountNov) {
		this.budgetDeductedAmountNov = budgetDeductedAmountNov;
	}

	public Double getBudgetDeductedAmountDec() {
		return budgetDeductedAmountDec;
	}

	public void setBudgetDeductedAmountDec(Double budgetDeductedAmountDec) {
		this.budgetDeductedAmountDec = budgetDeductedAmountDec;
	}

	public Double getBudgetDeductedTotal() {
		return budgetDeductedTotal;
	}

	public void setBudgetDeductedTotal(Double budgetDeductedTotal) {
		this.budgetDeductedTotal = budgetDeductedTotal;
	}

	public Double getBudgetTotal() {
		return budgetTotal;
	}

	public void setBudgetTotal(Double budgetTotal) {
		this.budgetTotal = budgetTotal;
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

	public Integer getPeriodicityOfPayment() {
		return periodicityOfPayment;
	}

	public void setPeriodicityOfPayment(Integer periodicityOfPayment) {
		this.periodicityOfPayment = periodicityOfPayment;
	}

	public Double getRentPayable() {
		return rentPayable;
	}

	public void setRentPayable(Double rentPayable) {
		this.rentPayable = rentPayable;
	}

	public String getLandlordName() {
		return landlordName;
	}

	public void setLandlordName(String landlordName) {
		this.landlordName = landlordName;
	}

	public String getLandlordAddress() {
		return landlordAddress;
	}

	public void setLandlordAddress(String landlordAddress) {
		this.landlordAddress = IdosUtil.escapeHtml(landlordAddress);
	}

	public String getBankAccountName() {
		return bankAccountName;
	}

	public void setBankAccountName(String bankAccountName) {
		this.bankAccountName = IdosUtil.escapeHtml(bankAccountName);
	}

	public String getBankAccountNumber() {
		return bankAccountNumber;
	}

	public void setBankAccountNumber(String bankAccountNumber) {
		this.bankAccountNumber = IdosUtil.escapeHtml(bankAccountNumber);
	}

	public String getBankAccountBranch() {
		return bankAccountBranch;
	}

	public void setBankAccountBranch(String bankAccountBranch) {
		this.bankAccountBranch = IdosUtil.escapeHtml(bankAccountBranch);
	}

	public Date getRentRevisedDueOn() {
		return rentRevisedDueOn;
	}

	public void setRentRevisedDueOn(Date rentRevisedDueOn) {
		this.rentRevisedDueOn = rentRevisedDueOn;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = IdosUtil.escapeHtml(remarks);
	}

	public List<BranchBankAccounts> getBranchBankAccounts() {
		return branchBankAccounts;
	}

	public void setBranchBankAccounts(List<BranchBankAccounts> branchBankAccounts) {
		this.branchBankAccounts = branchBankAccounts;
	}

	public List<BranchVendors> getBranchVendors() {
		return branchVendors;
	}

	public void setBranchVendors(List<BranchVendors> branchVendors) {
		this.branchVendors = branchVendors;
	}

	public Date getFilingVatReturns() {
		return filingVatReturns;
	}

	public void setFilingVatReturns(Date filingVatReturns) {
		this.filingVatReturns = filingVatReturns;
	}

	public Date getFilingTdsReturns() {
		return filingTdsReturns;
	}

	public void setFilingTdsReturns(Date filingTdsReturns) {
		this.filingTdsReturns = filingTdsReturns;
	}

	public Date getPayStateTaxes() {
		return payStateTaxes;
	}

	public void setPayStateTaxes(Date payStateTaxes) {
		this.payStateTaxes = payStateTaxes;
	}

	public Date getPayMunicipalTaxes() {
		return payMunicipalTaxes;
	}

	public void setPayMunicipalTaxes(Date payMunicipalTaxes) {
		this.payMunicipalTaxes = payMunicipalTaxes;
	}

	public Date getSubmittingMisReports() {
		return submittingMisReports;
	}

	public void setSubmittingMisReports(Date submittingMisReports) {
		this.submittingMisReports = submittingMisReports;
	}

	public String getSalestaxRegUrl() {
		return salestaxRegUrl;
	}

	public void setSalestaxRegUrl(String salestaxRegUrl) {
		this.salestaxRegUrl = salestaxRegUrl;
	}

	public Date getSalestaxRegValidFrom() {
		return salestaxRegValidFrom;
	}

	public void setSalestaxRegValidFrom(Date salestaxRegValidFrom) {
		this.salestaxRegValidFrom = salestaxRegValidFrom;
	}

	public Date getSalestaxRegValidTo() {
		return salestaxRegValidTo;
	}

	public void setSalestaxRegValidTo(Date salestaxRegValidTo) {
		this.salestaxRegValidTo = salestaxRegValidTo;
	}

	public String getCinRegNumber() {
		return cinRegNumber;
	}

	public void setCinRegNumber(String cinRegNumber) {
		this.cinRegNumber = cinRegNumber;
	}

	public String getCinRegUrl() {
		return cinRegUrl;
	}

	public void setCinRegUrl(String cinRegUrl) {
		this.cinRegUrl = cinRegUrl;
	}

	public Date getCinRegValidFrom() {
		return cinRegValidFrom;
	}

	public void setCinRegValidFrom(Date cinRegValidFrom) {
		this.cinRegValidFrom = cinRegValidFrom;
	}

	public Date getCinRegValidTo() {
		return cinRegValidTo;
	}

	public void setCinRegValidTo(Date cinRegValidTo) {
		this.cinRegValidTo = cinRegValidTo;
	}

	public String getTinRegNumber() {
		return tinRegNumber;
	}

	public void setTinRegNumber(String tinRegNumber) {
		this.tinRegNumber = tinRegNumber;
	}

	public String getTinRegUrl() {
		return tinRegUrl;
	}

	public void setTinRegUrl(String tinRegUrl) {
		this.tinRegUrl = tinRegUrl;
	}

	public Date getTinRegValidFrom() {
		return tinRegValidFrom;
	}

	public void setTinRegValidFrom(Date tinRegValidFrom) {
		this.tinRegValidFrom = tinRegValidFrom;
	}

	public Date getTinRegValidTo() {
		return tinRegValidTo;
	}

	public void setTinRegValidTo(Date tinRegValidTo) {
		this.tinRegValidTo = tinRegValidTo;
	}

	public String getVatRegNumber() {
		return vatRegNumber;
	}

	public void setVatRegNumber(String vatRegNumber) {
		this.vatRegNumber = IdosUtil.escapeHtml(vatRegNumber);
	}

	public String getVatRegUrl() {
		return vatRegUrl;
	}

	public void setVatRegUrl(String vatRegUrl) {
		this.vatRegUrl = vatRegUrl;
	}

	public Date getVatRegValidFrom() {
		return vatRegValidFrom;
	}

	public void setVatRegValidFrom(Date vatRegValidFrom) {
		this.vatRegValidFrom = vatRegValidFrom;
	}

	public Date getVatRegValidTo() {
		return vatRegValidTo;
	}

	public void setVatRegValidTo(Date vatRegValidTo) {
		this.vatRegValidTo = vatRegValidTo;
	}

	public String getSalestaxRegNumber() {
		return salestaxRegNumber;
	}

	public void setSalestaxRegNumber(String salestaxRegNumber) {
		this.salestaxRegNumber = IdosUtil.escapeHtml(salestaxRegNumber);
	}

	public List<BranchInsurance> getBranchInsurance() {
		return branchInsurance;
	}

	public void setBranchInsurance(List<BranchInsurance> branchInsurance) {
		this.branchInsurance = branchInsurance;
	}

	public List<BranchKeyOfficials> getBranchKeyOfficials() {
		return branchKeyOfficials;
	}

	public void setBranchKeyOfficials(List<BranchKeyOfficials> branchKeyOfficials) {
		this.branchKeyOfficials = branchKeyOfficials;
	}

	public List<BranchUsers> getBranchUsers() {
		return branchUsers;
	}

	public void setBranchUsers(List<BranchUsers> branchUsers) {
		this.branchUsers = branchUsers;
	}

	public List<BranchSpecifics> getBranchSpecifics() {
		return branchSpecifics;
	}

	public void setBranchSpecifics(List<BranchSpecifics> branchSpecifics) {
		this.branchSpecifics = branchSpecifics;
	}

	public String getAggreement() {
		return aggreement;
	}

	public void setAggreement(String aggreement) {
		this.aggreement = IdosUtil.escapeHtml(aggreement);
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = IdosUtil.escapeHtml(currency);
	}

	public String getBranchHead() {
		return branchHead;
	}

	public String getLocation() {
		return location;
	}

	public String getAddress() {
		return address;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public Date getBranchOpenDate() {
		return branchOpenDate;
	}

	public Integer getBranchFacility() {
		return branchFacility;
	}

	public Integer getBranchHasDepositBox() {
		return branchHasDepositBox;
	}

	public void setBranchDepositKeys(List<BranchDepositBoxKey> branchDepositKeys) {
		this.branchDepositKeys = branchDepositKeys;
	}

	public Integer getIsHeadQuarter() {
		return isHeadQuarter;
	}

	public Integer getNoOfKeys() {
		return noOfKeys;
	}

	public void setNoOfKeys(Integer noOfKeys) {
		this.noOfKeys = noOfKeys;
	}

	public void setIsHeadQuarter(Integer isHeadQuarter) {
		this.isHeadQuarter = isHeadQuarter;
	}

	public void setLocation(String location) {
		this.location = IdosUtil.escapeHtml(location);
	}

	public void setAddress(String address) {
		this.address = IdosUtil.escapeHtml(address);
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setBranchHead(String branchHead) {
		this.branchHead = branchHead;
	}

	public String getName() {
		return IdosUtil.escapeHtml(name);
	}

	public String getNameWithGSTIN() {
		String branchName = name;
		if (gstin != null && !"".equals(gstin)) {
			branchName += "-" + gstin;
		}
		return branchName;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setBranchOpenDate(Date bnchOpenDt) {
		this.branchOpenDate = bnchOpenDt;
	}

	public void setBranchHasDepositBox(Integer bnchDeposit) {
		this.branchHasDepositBox = bnchDeposit;
	}

	public void setBranchFacility(Integer bnchFacility) {
		this.branchFacility = bnchFacility;
	}

	public Organization getOrganization() {
		return organization;
	}

	public List<BranchDepositBoxKey> getBranchDepositKeys() {
		return branchDepositKeys;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public String getPhoneNumberCtryCode() {
		return phoneNumberCtryCode;
	}

	public void setPhoneNumberCtryCode(String phoneNumberCtryCode) {
		this.phoneNumberCtryCode = phoneNumberCtryCode;
	}

	/**
	 * Find a branch by id.
	 */
	public static Branch findById(Long id) {
		return entityManager.find(Branch.class, id);
	}

	public Integer getBranchAssetsInsured() {
		return branchAssetsInsured;
	}

	public void setBranchAssetsInsured(Integer branchAssetsInsured) {
		this.branchAssetsInsured = branchAssetsInsured;
	}

	/**
	 * list of branch.
	 */
	public List<Branch> list(EntityManager entityManager) {
		String sbquery = "select obj from Branch obj where obj.presentStatus=1";
		List<Branch> branches = entityManager.createQuery(sbquery).getResultList();
		return branches;
	}

	public List<ProjectBranches> getBranchProject() {
		return branchProject;
	}

	public void setBranchProject(List<ProjectBranches> branchProject) {
		this.branchProject = branchProject;
	}

	public Date getLastPaymentDate() {
		return lastPaymentDate;
	}

	public void setLastPaymentDate(Date lastPaymentDate) {
		this.lastPaymentDate = lastPaymentDate;
	}

	public List<BranchCashCount> getBranchCashCount() {
		return this.branchCashCount;
	}

	public void setBranchCashCount(List<BranchCashCount> branchCashCount) {
		this.branchCashCount = branchCashCount;
	}

	public String getStateCode() {
		return this.stateCode;
	}

	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}

	public String getGstin() {
		return this.gstin;
	}

	public void setGstin(String gstin) {
		this.gstin = gstin;
	}

	public static List<Branch> findListByOrgIdAndName(EntityManager entityManager, Long orgid, String name) {
		List<Branch> customerList = null;
		Query query = entityManager.createQuery(ORG_BRANCH_LIKE_HQL);
		query.setParameter(1, orgid);
		query.setParameter(2, "%" + name + "%");
		customerList = query.getResultList();
		return customerList;
	}

	public static List<Branch> findListByOrgIdEqualName(Long orgid, String name) {
		if (name == null) {
			return null;
		}
		List<Branch> branchList = null;
		Query query = entityManager.createQuery(ORG_BRANCH_EQUL_HQL);
		query.setParameter(1, orgid);
		query.setParameter(2, name.toUpperCase());
		branchList = query.getResultList();
		return branchList;
	}

	public static List<String> findListOfGSTIN(EntityManager entityManager, Long orgid) {
		List<String> branchGstList = null;
		Query query = entityManager.createQuery(ORG_BRANCH_GSTIN_HQL);
		query.setParameter(1, orgid);
		branchGstList = query.getResultList();
		return branchGstList;
	}

	public static List<Branch> findByName(EntityManager entityManager, Organization orgn, String branchName) {
		Query query = entityManager.createQuery(BYNAME_JPQL);
		query.setParameter(1, orgn.getId());
		query.setParameter(2, branchName.toUpperCase());
		List<Branch> branches = query.getResultList();
		return branches;
	}

}
