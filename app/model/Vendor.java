package model;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.*;

import com.idos.util.IdosConstants;
import com.idos.util.IdosDaoConstants;
import com.idos.util.IdosUtil;

import play.data.validation.Constraints;
import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.inject.Inject;

@Entity
@Table(name = "VENDOR")
public class Vendor extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public Vendor() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String GSTIN_JQL = "select obj from Vendor obj WHERE obj.gstin=?1 and obj.name=?2 and obj.type=?3 and obj.presentStatus=1";
	private static final String ORG_CUST_JQL = "select obj from Vendor obj WHERE obj.organization.id=?1 and obj.type=?2 and obj.presentStatus=1 order by obj.createdAt desc";
	private static final String ORG_NAMELIKE_HQL = "select obj from Vendor obj WHERE obj.organization.id=?1 and obj.type=?2 and obj.name like ?3 and obj.presentStatus=1";

	private static final String ORG_LIKE_HQL = "select obj from Vendor obj WHERE obj.organization.id=?1 and obj.name like ?2 and obj.presentStatus=1";

	private static final String CUSTOMER_HQL = "select obj from Vendor obj WHERE obj.organization.id=?1 and obj.type=?2 and obj.name=?3 and obj.presentStatus=1";
	private static final String GSTIN_HQL = "select obj from Vendor obj WHERE obj.organization.id=?1 and obj.type=?2 and obj.gstin=?3 and obj.presentStatus=1";
	private static final String PAN_HQL = "select obj from Vendor obj WHERE obj.organization.id=?1 and obj.type=?2 and obj.panNo=?3 and obj.presentStatus=1";
	private static final String TOKEN_JPQL = "select obj from Vendor obj WHERE obj.authToken=?1 and obj.presentStatus=1";
	private static final String BY_EMAIL_JPQL = "select obj from Vendor obj WHERE obj.email = ?1 and obj.presentStatus=1 order by id";
	@Column(name = "name")
	private String name;

	@Column(name = "CUSTOMER_CODE", length = 100, unique = true)
	@Constraints.MaxLength(100)
	private String customerCode;

	@Column(name = "GSTIN")
	private String gstin;

	@Column(name = "IS_BUSINESS") // 1=business, 2=Individual
	private Integer isBusiness;

	@Column(name = "IS_REGISTERED") // 1=registered, 2= unregistered
	private Integer isRegistered;

	@Column(name = "address")
	private String address;

	@Column(name = "location")
	private String location;

	@Column(name = "country_state")
	private String countryState;

	@Column(name = "email", length = 256, unique = true)
	@Constraints.MaxLength(256)
	@Constraints.Email
	private String email;

	@Column(name = "phone_country_code")
	private String phoneCtryCode;

	@Column(name = "phone")
	private String phone;

	@Column(name = "country")
	private Integer country;

	@Column(name = "price_list_doc")
	private String priceListDoc;

	@Column(name = "discount_percentage")
	private Double discountPercentage;

	@Column(name = "contract_po_doc")
	private String contractPoDoc;

	@Column(name = "validity_from")
	private Date validityFrom;

	@Column(name = "validity_to")
	private Date validityTo;

	@Column(name = "type")
	private Integer type;

	@Column(name = "vendor_specifics")
	private String vendorSpecifics;

	@Column(name = "CUSTOMER_SPECIFICS")
	private String customerSpecifics;

	@Column(name = "CUSTOMER_SPECIFICS_DISCOUNT_PERCENTAGE")
	private String customerSpecificsDiscountPercentage;

	@Column(name = "CUSTOMER_REMARKS")
	private String customerRemarks;

	@Column(name = "STATUTORY_NAME_1")
	private String statutoryName1;

	@Column(name = "STATUTORY_NUMBER_1")
	private String statutoryNumber1;

	@Column(name = "STATUTORY_NAME_2")
	private String statutoryName2;

	@Column(name = "STATUTORY_NUMBER_2")
	private String statutoryNumber2;

	@Column(name = "STATUTORY_NAME_3")
	private String statutoryName3;

	@Column(name = "STATUTORY_NUMBER_3")
	private String statutoryNumber3;

	@Column(name = "STATUTORY_NAME_4")
	private String statutoryName4;

	@Column(name = "STATUTORY_NUMBER_4")
	private String statutoryNumber4;

	@Column(name = "GRANT_ACCESS")
	private Integer grantAccess = 0;

	@Column(name = "ORIGINAL_OPENING_BALANCE") // opening bal for customer receivables/vendor payables- this will not
												// change when Receive payment from customer
	private Double totalOriginalOpeningBalance = 0.0;

	@Column(name = "OPENING_BALANCE") // opening bal for customer receivables/vendor payables-this will change when
										// receive payment from customer
	private Double totalOpeningBalance = 0.0;

	@Column(name = "opening_balance_advancepaid") // Opening balance for advance paid by customer/ to vendor
	private Double totalOpeningBalanceAdvPaid = 0.0;

	@Column(name = "ORIGINAL_OPENING_BALANCE_ADVANCEPAID") // advance paid opening bal for customer receivables/vendor
															// payables- this will not change when Receive payment from
															// customer
	private Double totalOriginalOpeningBalanceAdvPaid = 0.0;

	@Column(name = "CREDIT_LIMIT") // opening bal for customer receivables/vendor payables-this will change when
									// receive payment from customer
	private Double creditLimit = 0.0;

	@Column(name = "EXCEEDING_CREDIT_PROCESS_STOP") // Transaction exceeding credit limi 0=process by default, 1=stop
	private Integer exceedingCreditProcessStop;

	@Column(name = "EXCLUDE_ADVANCE_FROM_CREDIT_LIMIT") // Transaction exceeding credit limi 0=process by default,
														// 1=stop sale on credit tran
	private Integer excludeAdvFromCreLimCheck;

	@Column(name = "account_code")
	private Long accountCode;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "customer")
	private List<CustomerDetail> customerDetails;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "vendor")
	private List<VendorDetail> vendorDetails;

	public Integer getExcludeAdvFromCreLimCheck() {
		return excludeAdvFromCreLimCheck;
	}

	public void setExcludeAdvFromCreLimCheck(Integer excludeAdvFromCreLimCheck) {
		this.excludeAdvFromCreLimCheck = excludeAdvFromCreLimCheck;
	}

	public String getCustomerCode() {
		return customerCode;
	}

	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}

	public String getGstin() {
		return gstin;
	}

	public void setGstin(String gstin) {
		this.gstin = gstin;
	}

	public Integer getIsBusiness() {
		return isBusiness;
	}

	public void setIsBusiness(Integer isBusiness) {
		this.isBusiness = isBusiness;
	}

	public Integer getIsRegistered() {
		return isRegistered;
	}

	public void setIsRegistered(Integer isRegistered) {
		this.isRegistered = isRegistered;
	}

	public Double getCreditLimit() {
		return creditLimit;
	}

	public void setCreditLimit(Double creditLimit) {
		this.creditLimit = creditLimit;
	}

	public Integer getExceedingCreditProcessStop() {
		return exceedingCreditProcessStop;
	}

	public void setExceedingCreditProcessStop(Integer exceedingCreditProcessStop) {
		this.exceedingCreditProcessStop = exceedingCreditProcessStop;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "transactionVendorCustomer")
	private List<Transaction> vendorCustomerTransactions;

	public List<Transaction> getVendorCustomerTransactions() {
		return vendorCustomerTransactions;
	}

	public void setVendorCustomerTransactions(
			List<Transaction> vendorCustomerTransactions) {
		this.vendorCustomerTransactions = vendorCustomerTransactions;
	}

	public Integer getGrantAccess() {
		return grantAccess;
	}

	public void setGrantAccess(Integer grantAccess) {
		this.grantAccess = grantAccess;
	}

	public String getAccessPassword() {
		return accessPassword;
	}

	public void setAccessPassword(String accessPassword) {
		this.accessPassword = accessPassword;
	}

	@Column(name = "ACCESS_PASSWORD")
	private String accessPassword;

	public String getStatutoryName1() {
		return statutoryName1;
	}

	public void setStatutoryName1(String statutoryName1) {
		this.statutoryName1 = statutoryName1;
	}

	public String getStatutoryNumber1() {
		return statutoryNumber1;
	}

	public void setStatutoryNumber1(String statutoryNumber1) {
		this.statutoryNumber1 = statutoryNumber1;
	}

	public String getStatutoryName2() {
		return statutoryName2;
	}

	public void setStatutoryName2(String statutoryName2) {
		this.statutoryName2 = statutoryName2;
	}

	public String getStatutoryNumber2() {
		return statutoryNumber2;
	}

	public void setStatutoryNumber2(String statutoryNumber2) {
		this.statutoryNumber2 = statutoryNumber2;
	}

	public String getStatutoryName3() {
		return statutoryName3;
	}

	public void setStatutoryName3(String statutoryName3) {
		this.statutoryName3 = statutoryName3;
	}

	public String getStatutoryNumber3() {
		return statutoryNumber3;
	}

	public void setStatutoryNumber3(String statutoryNumber3) {
		this.statutoryNumber3 = statutoryNumber3;
	}

	public String getStatutoryName4() {
		return statutoryName4;
	}

	public void setStatutoryName4(String statutoryName4) {
		this.statutoryName4 = statutoryName4;
	}

	public String getStatutoryNumber4() {
		return statutoryNumber4;
	}

	public void setStatutoryNumber4(String statutoryNumber4) {
		this.statutoryNumber4 = statutoryNumber4;
	}

	public String getCustomerRemarks() {
		return customerRemarks;
	}

	public void setCustomerRemarks(String customerRemarks) {
		this.customerRemarks = IdosUtil.escapeHtml(customerRemarks);
	}

	public String getCustomerSpecificsDiscountPercentage() {
		return customerSpecificsDiscountPercentage;
	}

	public void setCustomerSpecificsDiscountPercentage(
			String customerSpecificsDiscountPercentage) {
		this.customerSpecificsDiscountPercentage = customerSpecificsDiscountPercentage;
	}

	public String getCustomerSpecifics() {
		return customerSpecifics;
	}

	public void setCustomerSpecifics(String customerSpecifics) {
		this.customerSpecifics = customerSpecifics;
	}

	public String getVendorSpecificsUnitPrice() {
		return vendorSpecificsUnitPrice;
	}

	public void setVendorSpecificsUnitPrice(String vendorSpecificsUnitPrice) {
		this.vendorSpecificsUnitPrice = vendorSpecificsUnitPrice;
	}

	@Column(name = "VENDOR_SPECIFICS_UNIT_PRICE")
	private String vendorSpecificsUnitPrice;

	public String getVendorSpecifics() {
		return vendorSpecifics;
	}

	public void setVendorSpecifics(String vendorSpecifics) {
		this.vendorSpecifics = vendorSpecifics;
	}

	public String getPriceListDoc() {
		return priceListDoc;
	}

	public void setPriceListDoc(String priceListDoc) {
		this.priceListDoc = priceListDoc;
	}

	public Double getDiscountPercentage() {
		return discountPercentage;
	}

	public void setDiscountPercentage(Double discountPercentage) {
		this.discountPercentage = discountPercentage;
	}

	public String getContractPoDoc() {
		return contractPoDoc;
	}

	public void setContractPoDoc(String contractPoDoc) {
		this.contractPoDoc = contractPoDoc;
	}

	public Date getValidityFrom() {
		return validityFrom;
	}

	public void setValidityFrom(Date validityFrom) {
		this.validityFrom = validityFrom;
	}

	public Date getValidityTo() {
		return validityTo;
	}

	public void setValidityTo(Date validityTo) {
		this.validityTo = validityTo;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getCountry() {
		return country;
	}

	public void setCountry(Integer country) {
		this.country = country;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "vendorSpecific")
	private List<VendorSpecific> vendorsSpecifics;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_organization_id")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "VENDOR_GROUP_ID")
	private VendorGroup vendorGroup;

	@Column(name = "ADJUSTMENTS_ALLOWED")
	private Integer adjustmentsAllowed;

	@Column(name = "ADJUSTMENTS_NAME")
	private String adjustmentsName;

	@Column(name = "ADJUSTMENTS_BASIS")
	private Integer adjustmentsBasis;

	@Column(name = "ADJUSTMENTS_RATE")
	private Double adjustmentsRate;

	@Column(name = "AVAILABLE_SPECIAL_ADJUSTMENT_AMOUNT")
	private Double availableSpecAdjAmount = 0.0;

	@Column(name = "contract_agreement")
	private String contractAgreement;

	@Column(name = "purchase_order")
	private String purchaseOrder;

	@Column(name = "purchase_type")
	private Integer purchaseType = 0;

	@Column(name = "days_for_credit")
	private Integer daysForCredit;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "vendor")
	List<BranchVendors> vendorBranches;

	@Column(name = "LAST_LOGIN_DATE")
	private Date lastLoginDate;

	@Column(name = "VENDOR_IN_SESSION")
	private Integer vendInSession = 0;

	@Column(name = "LAST_UPDATED_PASSWORD")
	private Date lastUpdatedPassword;

	// need this when vendor/customer is ticked in setup vendor/cust screen and mail
	// is sent with pwd
	@Column(name = "auth_token")
	private String authToken;

	@Column(name = "PAN_NO")
	private String panNo;

	@Column(name = "NATURE_OF_VEND")
	private Integer natureOfVendor;

	@Column(name = "PLACE_OF_SUPPLY_TYPE")
	private Integer placeOfSupplyType;

	@Column(name = "VENDOR_SPECIFICS_RCM_CESS_RATE")
	private String vendorSpecificsRcmCessRate;

	@Column(name = "VENDOR_SPECIFICS_RCM_TAX_RATE")
	private String vendorSpecificsRcmTaxRate;

	@Column(name = "VENDOR_SPECIFICS_RCM_APPLICABLE_DATE")
	private String vendorSpecificsRcmApplicableDate;

	public Integer getPlaceOfSupplyType() {
		return placeOfSupplyType;
	}

	public void setPlaceOfSupplyType(Integer placeOfSupplyType) {
		this.placeOfSupplyType = placeOfSupplyType;
	}

	public Double getAvailableSpecAdjAmount() {
		return availableSpecAdjAmount;
	}

	public void setAvailableSpecAdjAmount(Double availableSpecAdjAmount) {
		this.availableSpecAdjAmount = availableSpecAdjAmount;
	}

	public Integer getAdjustmentsAllowed() {
		return adjustmentsAllowed;
	}

	public void setAdjustmentsAllowed(Integer adjustmentsAllowed) {
		this.adjustmentsAllowed = adjustmentsAllowed;
	}

	public String getAdjustmentsName() {
		return adjustmentsName;
	}

	public void setAdjustmentsName(String adjustmentsName) {
		this.adjustmentsName = adjustmentsName;
	}

	public Integer getAdjustmentsBasis() {
		return adjustmentsBasis;
	}

	public void setAdjustmentsBasis(Integer adjustmentsBasis) {
		this.adjustmentsBasis = adjustmentsBasis;
	}

	public Double getAdjustmentsRate() {
		return adjustmentsRate;
	}

	public void setAdjustmentsRate(Double adjustmentsRate) {
		this.adjustmentsRate = adjustmentsRate;
	}

	public VendorGroup getVendorGroup() {
		return vendorGroup;
	}

	public void setVendorGroup(VendorGroup vendorGroup) {
		this.vendorGroup = vendorGroup;
	}

	public List<BranchVendors> getVendorBranches() {
		return vendorBranches;
	}

	public void setVendorBranches(List<BranchVendors> vendorBranches) {
		this.vendorBranches = vendorBranches;
	}

	public Integer getPurchaseType() {
		return purchaseType;
	}

	public void setPurchaseType(Integer purchaseType) {
		this.purchaseType = purchaseType;
	}

	public Integer getDaysForCredit() {
		return daysForCredit;
	}

	public void setDaysForCredit(Integer daysForCredit) {
		this.daysForCredit = daysForCredit;
	}

	public String getContractAgreement() {
		return contractAgreement;
	}

	public void setContractAgreement(String contractAgreement) {
		this.contractAgreement = contractAgreement;
	}

	public String getPurchaseOrder() {
		return purchaseOrder;
	}

	public void setPurchaseOrder(String purchaseOrder) {
		this.purchaseOrder = purchaseOrder;
	}

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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = IdosUtil.escapeHtml(location);
	}

	public List<VendorSpecific> getVendorsSpecifics() {
		return vendorsSpecifics;
	}

	public void setVendorsSpecifics(List<VendorSpecific> vendorsSpecifics) {
		this.vendorsSpecifics = vendorsSpecifics;
	}

	public String getName() {
		return this.name = IdosUtil.unescapeHtml(name);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = IdosUtil.escapeHtml(address);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPhoneCtryCode() {
		return phoneCtryCode;
	}

	public void setPhoneCtryCode(String phoneCtryCode) {
		this.phoneCtryCode = phoneCtryCode;
	}

	public String getVendorSpecificsRcmCessRate() {
		return vendorSpecificsRcmCessRate;
	}

	public void setVendorSpecificsRcmCessRate(String vendorSpecificsRcmCessRate) {
		this.vendorSpecificsRcmCessRate = vendorSpecificsRcmCessRate;
	}

	public String getVendorSpecificsRcmTaxRate() {
		return vendorSpecificsRcmTaxRate;
	}

	public void setVendorSpecificsRcmTaxRate(String vendorSpecificsRcmTaxRate) {
		this.vendorSpecificsRcmTaxRate = vendorSpecificsRcmTaxRate;
	}

	public String getVendorSpecificsRcmApplicableDate() {
		return vendorSpecificsRcmApplicableDate;
	}

	public void setVendorSpecificsRcmApplicableDate(String vendorSpecificsRcmApplicableDate) {
		this.vendorSpecificsRcmApplicableDate = vendorSpecificsRcmApplicableDate;
	}

	/**
	 * Find a vendor by id.
	 */
	public static Vendor findById(Long id) {
		return entityManager.find(Vendor.class, id);
	}

	public Long getAccountCode() {
		return accountCode;
	}

	public void setAccountCode(Long accountCode) {
		this.accountCode = accountCode;
	}

	public String getAuthToken() {
		return this.authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String createToken() {
		authToken = UUID.randomUUID().toString();
		authToken = "vend" + authToken;
		return authToken;
	}

	public static Vendor findByAuthToken(EntityManager entityManager, String authToken) {
		Vendor user = null;
		if (authToken == null) {
			return user;
		}
		try {
			Query query = entityManager.createQuery(TOKEN_JPQL);
			query.setParameter(1, authToken);
			user = (Vendor) query.getSingleResult();
		} catch (NoResultException ex) {
		}
		return user;
	}

	public Integer getVendInSession() {
		return vendInSession;
	}

	public void setRegVendInSession(Integer vendInSession) {
		this.vendInSession = vendInSession;
	}

	public Date getLastUpdatedPassword() {
		return lastUpdatedPassword;
	}

	public void setLastUpdatedPassword(Date lastUpdatedPassword) {
		this.lastUpdatedPassword = lastUpdatedPassword;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public String getCountryState() {
		return this.countryState;
	}

	public void setCountryState(String countryState) {
		this.countryState = countryState;
	}

	public List<CustomerDetail> getCustomerDetails() {
		return this.customerDetails;
	}

	public void setCustomerDetails(List<CustomerDetail> customerDetails) {
		this.customerDetails = customerDetails;
	}

	public List<VendorDetail> getVendorDetails() {
		return this.vendorDetails;
	}

	public void setVendorDetails(List<VendorDetail> vendorDetails) {
		this.vendorDetails = vendorDetails;
	}

	public static Vendor findByNameAndGSTNID(EntityManager entityManager, String name, String gstin, int type) {
		Vendor customer = null;
		try {
			Query query = entityManager.createQuery(GSTIN_JQL);
			query.setParameter(1, gstin);
			query.setParameter(2, name);
			query.setParameter(3, type);
			customer = (Vendor) query.getSingleResult();
		} catch (NoResultException ex) {
		}
		return customer;
	}

	public static List<Vendor> findByOrgIdAndType(EntityManager entityManager, Long orgid, int type) {
		List<Vendor> customerList = null;
		Query query = entityManager.createQuery(ORG_CUST_JQL);
		query.setParameter(1, orgid);
		query.setParameter(2, type);
		customerList = query.getResultList();
		return customerList;
	}

	public static List<Vendor> findListByOrgIdAndTypeName(EntityManager entityManager, Long orgid, int type,
			String name) {
		List<Vendor> customerList = null;
		Query query = entityManager.createQuery(ORG_NAMELIKE_HQL);
		query.setParameter(1, orgid);
		query.setParameter(2, type);
		query.setParameter(3, "%" + name + "%");
		customerList = query.getResultList();
		return customerList;
	}

	public static Vendor findByOrgIdTypeName(EntityManager entityManager, Long orgid, int type, String name) {
		Vendor customer = null;
		try {
			Query query = entityManager.createQuery(CUSTOMER_HQL);
			query.setParameter(1, orgid);
			query.setParameter(2, type);
			query.setParameter(3, name);
			customer = (Vendor) query.getSingleResult();
		} catch (NoResultException ex) {
		}
		return customer;
	}

	public static Vendor findByOrgIdTypeGST(EntityManager entityManager, Long orgid, int type, String gstinCode) {
		Vendor gstin = null;
		try {
			Query query = entityManager.createQuery(GSTIN_HQL);
			query.setParameter(1, orgid);
			query.setParameter(2, type);
			query.setParameter(3, gstinCode);
			gstin = (Vendor) query.getSingleResult();
		} catch (NoResultException ex) {
		}
		return gstin;
	}

	public static Vendor findByOrgIdTypePanNo(EntityManager entityManager, Long orgid, int type, String vendPanNo) {
		Vendor panNo = null;
		try {
			Query query = entityManager.createQuery(PAN_HQL);
			query.setParameter(1, orgid);
			query.setParameter(2, type);
			query.setParameter(3, vendPanNo);
			panNo = (Vendor) query.getSingleResult();
		} catch (NoResultException ex) {
			// TODO: handle exception
		}
		return panNo;
	}

	public static List<Vendor> findListByOrgIdAndName(EntityManager entityManager, Long orgid, String name) {
		List<Vendor> customerList = null;
		Query query = entityManager.createQuery(ORG_LIKE_HQL);
		query.setParameter(1, orgid);
		query.setParameter(2, "%" + name + "%");
		customerList = query.getResultList();
		return customerList;
	}

	public Double getTotalOriginalOpeningBalance() {
		return this.totalOriginalOpeningBalance;
	}

	public void setTotalOriginalOpeningBalance(Double totalOriginalOpeningBalance) {
		this.totalOriginalOpeningBalance = totalOriginalOpeningBalance;
	}

	public Double getTotalOpeningBalance() {
		return this.totalOpeningBalance;
	}

	public void setTotalOpeningBalance(Double totalOpeningBalance) {
		this.totalOpeningBalance = totalOpeningBalance;
	}

	public Double getTotalOpeningBalanceAdvPaid() {
		return this.totalOpeningBalanceAdvPaid;
	}

	public void setTotalOpeningBalanceAdvPaid(Double totalOpeningBalanceAdvPaid) {
		this.totalOpeningBalanceAdvPaid = totalOpeningBalanceAdvPaid;
	}

	public Double getTotalOriginalOpeningBalanceAdvPaid() {
		return this.totalOriginalOpeningBalanceAdvPaid;
	}

	public void setTotalOriginalOpeningBalanceAdvPaid(Double totalOriginalOpeningBalanceAdvPaid) {
		this.totalOriginalOpeningBalanceAdvPaid = totalOriginalOpeningBalanceAdvPaid;
	}

	public String getPanNo() {
		return panNo;
	}

	public void setPanNo(String panNo) {
		this.panNo = panNo;
	}

	public Integer getNatureOfVendor() {
		return natureOfVendor;
	}

	public void setNatureOfVendor(Integer natureOfVendor) {
		this.natureOfVendor = natureOfVendor;
	}

	public static Vendor findByEmailAddress(String emailAddress) throws Exception {
		Vendor vendor = null;
		if (emailAddress == null) {
			return vendor;
		}
		Query query = entityManager.createQuery(BY_EMAIL_JPQL);
		query.setParameter(1, emailAddress);
		List<Vendor> vendors = query.getResultList();
		if (vendors.size() > 0) {
			vendor = vendors.get(0);
		}
		return vendor;
	}
}
