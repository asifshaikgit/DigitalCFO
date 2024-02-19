package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.*;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.idos.util.IdosUtil;
import com.idos.util.PasswordUtil;
import play.data.validation.Constraints;
import play.db.jpa.JPAApi;
import javax.inject.Inject;

@Entity
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
@Table(name = "USERS")
public class Users extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public Users() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String USR_ACTIVE_HQL = "select obj from Users obj WHERE obj.email =?1 and  obj.presentStatus=1 order by id";
	private static final String USR_PWD_HQL = "select obj from Users obj WHERE obj.email=?1 and obj.password =?2 and obj.presentStatus=1 order by id";
	private static final String USR_ALL_HQL = "select obj from Users obj WHERE obj.email=?1";
	private static final String USR_ACTIVE_PASS_HQL = "select obj from Users obj WHERE obj.email=?1 and obj.presentStatus=1 and obj.password =?2";
	private static final String USR_ALL_ACT_ORG_HQL = "select obj from Users obj WHERE obj.organization.id=?1 and obj.presentStatus=1";
	private static final String USR_ALL_EXCLUDE_EXT_USER = "select obj from Users obj WHERE obj.id not in(select obj1.user.id from UsersRoles obj1 where obj1.role.name = 'EXTERNAL USER' or obj1.role.name = 'SUPER ADMIN') and obj.id not in(select obj2.corrUserId.id from ExternalUserCompanyDetails obj2 where obj2.corrUserId.id is not null) and obj.organization.id=?1 and obj.presentStatus=1";
	private static final String USR_ALL_EXT_USER = "select obj from Users obj WHERE obj.id in (select obj1.corrUserId.id from ExternalUserCompanyDetails obj1 where obj1.corrUserId.id is not null and obj1.org.id=?1 and obj1.externalUserStatus = 1) and obj.presentStatus=1";

	@Column(name = "full_name")
	private String fullName;

	@Column(name = "password")
	private String password;

	@Column(name = "karvy_password")
	private String passwordForKarvy;

	public String getPasswordForKarvy() {
		return passwordForKarvy;
	}

	public void setPasswordForKarvy(String passwordForKarvy) {
		this.passwordForKarvy = passwordForKarvy;
	}

	@Column(name = "email", length = 256, unique = true, nullable = false)
	@Constraints.MaxLength(256)
	@Constraints.Required
	@Constraints.Email
	private String email;

	@Column(name = "address")
	private String address;

	@Column(name = "date_of_birth")
	private Date dob;

	@Column(name = "phone_number_country_code")
	private String phoneNumberCountryCode;

	@Column(name = "phone_number")
	private String mobile;

	@Column(name = "alternate_email")
	private String altEmail;

	@Column(name = "user_photograph")
	private String photograph;

	@Column(name = "id_proof_document")
	private String idproof;

	@Column(name = "blood_group")
	private String bloodGroup;

	/*
	 * @Column(name="STATUS")
	 * private String status;
	 * 
	 * @Column(name="ETHNICITY")
	 * private String ethnicity;
	 */

	@Column(name = "DATE_OF_HIRE")
	private Date dateOfHire;

	@Column(name = "DATE_OF_RELEASE")
	private Date dateOfRelease;

	@Column(name = "DATE_OF_CONFIRMATION")
	private Date dateOfConfirmation;

	@Column(name = "NOTICE_START_DATE")
	private Date noticeStartDate;

	@Column(name = "TRANSACTION_CREATION_BRANCHES")
	private String transactionCreationBranches;

	@Column(name = "TRANSACTION_CREATION_PROJECTS")
	private String transactionCreationProjects;

	@Column(name = "ALLOWED_PROCUREMENT_REQUEST")
	private Integer allowedProcurementRequest;

	@Column(name = "IN_SESSION")
	private Integer inSession = 0;

	@Column(name = "LAST_UPDATED_PASSWORD_DATE")
	private Date lastUpdatedPasswordDate;

	@Column(name = "EMERGENCY_CONTACT_NAME")
	private String emergencyContactName;

	@Column(name = "EMERGENCY_CONTACT_EMAIL")
	private String emergencyContactEmail;

	@Column(name = "EMERGENCY_CONTACT_PHONE")
	private String emergencyContactPhone;

	@Column(name = "TRANSACTION_APPROVAL_BRANCHES")
	private String transactionApprovalBranches;

	@Column(name = "TRANSACTION_APPROVAL_PROJECT")
	private String transactionApproverProject;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<UsersRoles> userRoles;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.PERSIST)
	private List<UserRightInBranch> userRightsInBranches;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.PERSIST)
	private List<UserRightSpecifics> userRightsSpecifics;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SUPERVISOR_ACCOUNTMANAGER")
	private Users supervisorUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "HR_MANAGER")
	private Users hrManager;

	@Column(name = "EMPLOYEMENT_TYPE")
	private String employmentType;

	@Column(name = "SOURCE")
	private String source;

	@Column(name = "PAN_NUMBER")
	private String panNumber;

	@Column(name = "PASSPORT_NUMBER")
	private String passportNumber;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "branch_id")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "branch_officers_id")
	private OrganizationKeyOfficials branchKeyOff;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "branch_safe_deposit_box_id")
	private BranchDepositBoxKey branchSafeDepositBox;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "USERS_TRAVEL_GROUP_ID")
	private Travel_Group tGroup;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "USERS_EXPENSE_GROUP_ID")
	private ExpenseGroup eGroup;

	@Column(name = "USERS_TRAVELCLAIM_TXN_QUESTIONS")
	private String usersTravelClaimTxnQuestions;

	@Column(name = "USERS_EXPENSECLAIM_TXN_QUESTIONS")
	private String usersExpenseClaimTxnQuestions;

	@ManyToOne(optional = false, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "branch_organization_id")
	private Organization organization;

	@Column(name = "last_login_date")
	private Date lastLoginDate;

	@Column(name = "RESET_PASSWORD_TOKEN")
	private String resetPasswordToken;

	@Column(name = "TRAVEL_ADVANCE_ACCOUNT_AMOUNT")
	private Double travelAdvanceAccountAmount = 0.0;

	@Column(name = "TRAVEL_ADVANCE_SETTLED_AMOUNT")
	private Double travelAdvanceSettledAmount = 0.0;

	@Column(name = "TRAVEL_ADVANCE_RETURNED_AMOUNT")
	private Double travelAdvanceReturnedAmount = 0.0;

	@Column(name = "EXPENSE_ADVANCE_ACCOUNT_AMOUNT")
	private Double expenseAdvanceAccountAmount = 0.0;

	@Column(name = "EXPENSE_ADVANCE_SETTLED_AMOUNT")
	private Double expenseAdvanceAettledAmount = 0.0;

	@Column(name = "EXPENSE_ADVANCE_RETURNED_AMOUNT")
	private Double expenseAdvanceReturnedAmount = 0.0;

	@Column(name = "EXPENSE_REIMBURSEMENT_ACCOUNT_AMOUNT")
	private Double expenseReimbursementAccountAmount = 0.0;

	@Column(name = "EXPENSE_REIMBURSEMENT_SETTLED_AMOUNT")
	private Double expenseReimbursementSettledAmount = 0.0;

	@Column(name = "DESIGNATION")
	private String designation;

	@Column(name = "DEPARTMENT")
	private String department;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "branch", cascade = CascadeType.PERSIST)
	private List<BranchUsers> userBranches;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.PERSIST)
	private List<UserTransactionPurpose> userTxnQuestions;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	private List<UserRightForProject> userRightForProjects;

	// Auditor Access Rights: In Admin User section
	@Column(name = "TRANSACTION_AUDITOR_BRANCHES")
	private String transactionAuditorBranches;

	@Column(name = "ACCESS_RIGHTS")
	private String accessRights;

	@Column(name = "TRANSACTION_CREATION_COAINCOME")
	private String txnCreationCoaIncome;
	@Column(name = "TRANSACTION_CREATION_COAINCOME_FROM_AMOUNT")
	private String txnCreationCoaIncomeFromAmount;
	@Column(name = "TRANSACTION_CREATION_COAINCOME_TO_AMOUNT")
	private String txnCreationCoaIncomeToAmount;

	@Column(name = "TRANSACTION_CREATION_COAEXPENSE")
	private String txnCreationCoaExpense;
	@Column(name = "TRANSACTION_CREATION_COAEXPENSE_FROM_AMOUNT")
	private String txnCreationCoaExpenseFromAmount;
	@Column(name = "TRANSACTION_CREATION_COAEXPENSE_TO_AMOUNT")
	private String txnCreationCoaExpenseToAmount;

	@Column(name = "TRANSACTION_CREATION_COAASSET")
	private String txnCreationCoaAsset;
	@Column(name = "TRANSACTION_CREATION_COAASSET_FROM_AMOUNT")
	private String txnCreationCoaAssetFromAmount;
	@Column(name = "TRANSACTION_CREATION_COAASSET_TO_AMOUNT")
	private String txnCreationCoaAssetToAmount;

	@Column(name = "TRANSACTION_CREATION_COALIABL")
	private String txnCreationCoaLiabl;
	@Column(name = "TRANSACTION_CREATION_COALIABL_FROM_AMOUNT")
	private String txnCreationCoaLiablFromAmount;
	@Column(name = "TRANSACTION_CREATION_COALIABL_TO_AMOUNT")
	private String txnCreationCoaLiablToAmount;

	@Column(name = "TRANSACTION_APPROVAL_COAINCOME")
	private String txnApprovalCoaIncome;
	@Column(name = "TRANSACTION_APPROVAL_COAINCOME_FROM_AMOUNT")
	private String txnApprovalCoaIncomeFromAmount;
	@Column(name = "TRANSACTION_APPROVAL_COAINCOME_TO_AMOUNT")
	private String txnApprovalCoaIncomeToAmount;

	@Column(name = "TRANSACTION_APPROVAL_COAEXPENSE")
	private String txnApprovalCoaExpense;
	@Column(name = "TRANSACTION_APPROVAL_COAEXPENSE_FROM_AMOUNT")
	private String txnApprovalCoaExpenseFromAmount;
	@Column(name = "TRANSACTION_APPROVAL_COAEXPENSE_TO_AMOUNT")
	private String txnApprovalCoaExpenseToAmount;

	@Column(name = "TRANSACTION_APPROVAL_COAASSET")
	private String txnApprovalCoaAsset;
	@Column(name = "TRANSACTION_APPROVAL_COAASSET_FROM_AMOUNT")
	private String txnApprovalCoaAssetFromAmount;
	@Column(name = "TRANSACTION_APPROVAL_COAASSET_TO_AMOUNT")
	private String txnApprovalCoaAssetToAmount;

	@Column(name = "TRANSACTION_APPROVAL_COALIABL")
	private String txnApprovalCoaLiabl;
	@Column(name = "TRANSACTION_APPROVAL_COALIABL_FROM_AMOUNT")
	private String txnApprovalCoaLiablFromAmount;
	@Column(name = "TRANSACTION_APPROVAL_COALIABL_TO_AMOUNT")
	private String txnApprovalCoaLiablToAmount;

	@Column(name = "TRANSACTION_AUDITOR_COAINCOME")
	private String txnAuditorCoaIncome;
	@Column(name = "TRANSACTION_AUDITOR_COAEXPENSE")
	private String txnAuditorCoaExpense;
	@Column(name = "TRANSACTION_AUDITOR_COAASSET")
	private String txnAuditorCoaAsset;
	@Column(name = "TRANSACTION_AUDITOR_COALIABL")
	private String txnAuditorCoaLiabl;
	@Column(name = "verification_code", length = 6)
	private String verificationCode;

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}

	public String getAccessRights() {
		return this.accessRights;
	}

	public void setAccessRights(String accessRights) {
		this.accessRights = accessRights;
	}

	public boolean canCreateCustomer() {
		if (this.accessRights == null) {
			return false;
		}
		char[] temp = this.accessRights.toCharArray();
		return temp[0] == '1' ? true : false;
	}

	public boolean canActivateCustomer() {
		if (this.accessRights == null) {
			return false;
		}
		char[] temp = this.accessRights.toCharArray();
		return temp[1] == '1' ? true : false;
	}

	public boolean canCreateVendor() {
		if (this.accessRights == null) {
			return false;
		}
		char[] temp = this.accessRights.toCharArray();
		return temp[2] == '1' ? true : false;
	}

	public boolean canActivateVendor() {
		if (this.accessRights == null) {
			return false;
		}
		char[] temp = this.accessRights.toCharArray();
		return temp[3] == '1' ? true : false;
	}

	public Date getLastUpdatedPasswordDate() {
		return lastUpdatedPasswordDate;
	}

	public void setLastUpdatedPasswordDate(Date lastUpdatedPasswordDate) {
		this.lastUpdatedPasswordDate = lastUpdatedPasswordDate;
	}

	public Integer getInSession() {
		return inSession;
	}

	public void setInSession(Integer inSession) {
		this.inSession = inSession;
	}

	public Integer getAllowedProcurementRequest() {
		return allowedProcurementRequest;
	}

	public void setAllowedProcurementRequest(Integer allowedProcurementRequest) {
		this.allowedProcurementRequest = allowedProcurementRequest;
	}

	public String getTransactionCreationBranches() {
		return transactionCreationBranches;
	}

	public void setTransactionCreationBranches(String transactionCreationBranches) {
		this.transactionCreationBranches = transactionCreationBranches;
	}

	public String getTransactionCreationProjects() {
		return transactionCreationProjects;
	}

	public void setTransactionCreationProjects(String transactionCreationProjects) {
		this.transactionCreationProjects = transactionCreationProjects;
	}

	public String getTransactionApprovalBranches() {
		return transactionApprovalBranches;
	}

	public void setTransactionApprovalBranches(String transactionApprovalBranches) {
		this.transactionApprovalBranches = transactionApprovalBranches;
	}

	public String getTransactionApproverProject() {
		return transactionApproverProject;
	}

	public void setTransactionApproverProject(String transactionApproverProject) {
		this.transactionApproverProject = transactionApproverProject;
	}

	public String getBloodGroup() {
		return bloodGroup;
	}

	public void setBloodGroup(String bloodGroup) {
		this.bloodGroup = bloodGroup;
	}

	public List<UserRightSpecifics> getUserRightsSpecifics() {
		return userRightsSpecifics;
	}

	public void setUserRightsSpecifics(List<UserRightSpecifics> userRightsSpecifics) {
		this.userRightsSpecifics = userRightsSpecifics;
	}

	public List<UserRightInBranch> getUserRightsInBranches() {
		return userRightsInBranches;
	}

	public void setUserRightsInBranches(List<UserRightInBranch> userRightsInBranches) {
		this.userRightsInBranches = userRightsInBranches;
	}

	public Travel_Group gettGroup() {
		return tGroup;
	}

	public void settGroup(Travel_Group tGroup) {
		this.tGroup = tGroup;
	}

	public ExpenseGroup geteGroup() {
		return eGroup;
	}

	public void seteGroup(ExpenseGroup eGroup) {
		this.eGroup = eGroup;
	}

	public String getUsersTravelClaimTxnQuestions() {
		return usersTravelClaimTxnQuestions;
	}

	public void setUsersTravelClaimTxnQuestions(String usersTravelClaimTxnQuestions) {
		this.usersTravelClaimTxnQuestions = usersTravelClaimTxnQuestions;
	}

	public String getUsersExpenseClaimTxnQuestions() {
		return usersExpenseClaimTxnQuestions;
	}

	public void setUsersExpenseClaimTxnQuestions(
			String usersExpenseClaimTxnQuestions) {
		this.usersExpenseClaimTxnQuestions = usersExpenseClaimTxnQuestions;
	}

	public OrganizationKeyOfficials getBranchKeyOff() {
		return branchKeyOff;
	}

	public void setBranchKeyOff(OrganizationKeyOfficials branchKeyOff) {
		this.branchKeyOff = branchKeyOff;
	}

	public BranchDepositBoxKey getBranchSafeDepositBox() {
		return branchSafeDepositBox;
	}

	public void setBranchSafeDepositBox(BranchDepositBoxKey branchSafeDepositBox) {
		this.branchSafeDepositBox = branchSafeDepositBox;
	}

	public String getResetPasswordToken() {
		return resetPasswordToken;
	}

	public void setResetPasswordToken(String resetPasswordToken) {
		this.resetPasswordToken = resetPasswordToken;
	}

	public List<UserTransactionPurpose> getUserTxnQuestions() {
		return userTxnQuestions;
	}

	public void setUserTxnQuestions(List<UserTransactionPurpose> userTxnQuestions) {
		this.userTxnQuestions = userTxnQuestions;
	}

	public List<BranchUsers> getUserBranches() {
		return userBranches;
	}

	public void setUserBranches(List<BranchUsers> userBranches) {
		this.userBranches = userBranches;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = IdosUtil.escapeHtml(address);
	}

	public Date getDob() {
		return dob;
	}

	public void setDob(Date dob) {
		this.dob = dob;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getAltEmail() {
		return altEmail;
	}

	public void setAltEmail(String altEmail) {
		this.altEmail = altEmail;
	}

	public String getPhotograph() {
		return photograph;
	}

	public void setPhotograph(String photograph) {
		this.photograph = photograph;
	}

	public String getIdproof() {
		return idproof;
	}

	public void setIdproof(String idproof) {
		this.idproof = idproof;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
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

	public List<UsersRoles> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(List<UsersRoles> userRoles) {
		this.userRoles = userRoles;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFullName() {
		return IdosUtil.unescapeHtml(fullName);
	}

	public void setFullName(String fullName) {
		this.fullName = IdosUtil.escapeHtml(fullName);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Find a user by id.
	 */
	public static Users findById(Long id) {
		return entityManager.find(Users.class, id);
	}

	public String getPhoneNumberCountryCode() {
		return phoneNumberCountryCode;
	}

	public void setPhoneNumberCountryCode(String phoneNumberCountryCode) {
		this.phoneNumberCountryCode = phoneNumberCountryCode;
	}

	public Double getTravelAdvanceAccountAmount() {
		return travelAdvanceAccountAmount;
	}

	public void setTravelAdvanceAccountAmount(Double travelAdvanceAccountAmount) {
		this.travelAdvanceAccountAmount = travelAdvanceAccountAmount;
	}

	public Double getTravelAdvanceSettledAmount() {
		return travelAdvanceSettledAmount;
	}

	public void setTravelAdvanceSettledAmount(Double travelAdvanceSettledAmount) {
		this.travelAdvanceSettledAmount = travelAdvanceSettledAmount;
	}

	public Double getExpenseAdvanceAccountAmount() {
		return expenseAdvanceAccountAmount;
	}

	public void setExpenseAdvanceAccountAmount(Double expenseAdvanceAccountAmount) {
		this.expenseAdvanceAccountAmount = expenseAdvanceAccountAmount;
	}

	public Double getExpenseAdvanceAettledAmount() {
		return expenseAdvanceAettledAmount;
	}

	public void setExpenseAdvanceAettledAmount(Double expenseAdvanceAettledAmount) {
		this.expenseAdvanceAettledAmount = expenseAdvanceAettledAmount;
	}

	public Double getTravelAdvanceReturnedAmount() {
		return travelAdvanceReturnedAmount;
	}

	public void setTravelAdvanceReturnedAmount(Double travelAdvanceReturnedAmount) {
		this.travelAdvanceReturnedAmount = travelAdvanceReturnedAmount;
	}

	public Double getExpenseAdvanceReturnedAmount() {
		return expenseAdvanceReturnedAmount;
	}

	public void setExpenseAdvanceReturnedAmount(Double expenseAdvanceReturnedAmount) {
		this.expenseAdvanceReturnedAmount = expenseAdvanceReturnedAmount;
	}

	public Double getExpenseReimbursementAccountAmount() {
		return expenseReimbursementAccountAmount;
	}

	public void setExpenseReimbursementAccountAmount(
			Double expenseReimbursementAccountAmount) {
		this.expenseReimbursementAccountAmount = expenseReimbursementAccountAmount;
	}

	public Double getExpenseReimbursementSettledAmount() {
		return expenseReimbursementSettledAmount;
	}

	public void setExpenseReimbursementSettledAmount(
			Double expenseReimbursementSettledAmount) {
		this.expenseReimbursementSettledAmount = expenseReimbursementSettledAmount;
	}

	public List<UserRightForProject> getUserRightForProjects() {
		return userRightForProjects;
	}

	public void setUserRightForProjects(
			List<UserRightForProject> userRightForProjects) {
		this.userRightForProjects = userRightForProjects;
	}

	public Date getDateOfHire() {
		return dateOfHire;
	}

	public void setDateOfHire(Date dateOfHire) {
		this.dateOfHire = dateOfHire;
	}

	public Date getDateOfRelease() {
		return dateOfRelease;
	}

	public void setDateOfRelease(Date dateOfRelease) {
		this.dateOfRelease = dateOfRelease;
	}

	public Date getDateOfConfirmation() {
		return dateOfConfirmation;
	}

	public void setDateOfConfirmation(Date dateOfConfirmation) {
		this.dateOfConfirmation = dateOfConfirmation;
	}

	public Date getNoticeStartDate() {
		return noticeStartDate;
	}

	public void setNoticeStartDate(Date noticeStartDate) {
		this.noticeStartDate = noticeStartDate;
	}

	public Users getSupervisorUser() {
		return supervisorUser;
	}

	public void setSupervisorUser(Users supervisorUser) {
		this.supervisorUser = supervisorUser;
	}

	public Users getHrManager() {
		return hrManager;
	}

	public void setHrManager(Users hrManager) {
		this.hrManager = hrManager;
	}

	public String getEmploymentType() {
		return employmentType;
	}

	public void setEmploymentType(String employmentType) {
		this.employmentType = employmentType;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = IdosUtil.escapeHtml(source);
	}

	public String getPanNumber() {
		return panNumber;
	}

	public void setPanNumber(String panNumber) {
		this.panNumber = IdosUtil.escapeHtml(panNumber);
	}

	public String getPassportNumber() {
		return passportNumber;
	}

	public void setPassportNumber(String passportNumber) {
		this.passportNumber = IdosUtil.escapeHtml(passportNumber);
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = IdosUtil.escapeHtml(designation);
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = IdosUtil.escapeHtml(department);
	}

	public String getEmergencyContactName() {
		return emergencyContactName;
	}

	public void setEmergencyContactName(String emergencyContactName) {
		this.emergencyContactName = IdosUtil.escapeHtml(emergencyContactName);
	}

	public String getEmergencyContactEmail() {
		return emergencyContactEmail;
	}

	public void setEmergencyContactEmail(String emergencyContactEmail) {
		this.emergencyContactEmail = emergencyContactEmail;
	}

	public String getEmergencyContactPhone() {
		return emergencyContactPhone;
	}

	public void setEmergencyContactPhone(String emergencyContactPhone) {
		this.emergencyContactPhone = emergencyContactPhone;
	}

	public String getTransactionAuditorBranches() {
		return transactionAuditorBranches;
	}

	public void setTransactionAuditorBranches(String transactionAuditorBranches) {
		this.transactionAuditorBranches = transactionAuditorBranches;
	}

	public static List<Users> getAllUsersByOrganization(EntityManager entityManager, Organization org) {
		String sbquery = "select obj from Users obj where obj.organization.id = " + org.getId()
				+ " and obj.presentStatus=1";
		List<Users> orgList = entityManager.createQuery(sbquery).getResultList();
		return orgList;
	}

	public String getAuthToken() {
		return this.authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	@Column(name = "auth_token")
	private String authToken;

	@Column(name = "failed_attempt")
	private Integer failedAttempt = 0;

	@Column(name = "MODULE_RIGHTS")
	private String moduleRights;

	@Column(name = "THEME")
	private Integer theme;

	@Column(name = "OPENING_BALANCE_ADV")
	private Double openigBalanceAdvance;

	@Column(name = "OPENING_BALANCE_CLAIM")
	private Double openigBalanceClaim;

	@Column(name = "AGREED_TERMS_AND_CONDITIONS")
	private Integer agreedTermsAndContions = 0; // User Agree = 1 otherwise 0

	public String createToken() {
		authToken = UUID.randomUUID().toString();
		return authToken;
	}

	public Integer getFailedAttempt() {
		return this.failedAttempt;
	}

	public void setFailedAttempt(Integer failedAttempt) {
		this.failedAttempt = failedAttempt;
	}

	public static Users findByAuthToken(EntityManager entityManager, String authToken, Long id) {
		Users user = null;
		if (authToken == null) {
			return user;
		}
		System.out.println(">>>>>" + authToken + "<<<<<<<<<" + id);
		try {
			Query query = entityManager.createQuery(
					"select obj from Users obj WHERE obj.authToken=?1 and obj.id=?2 and obj.presentStatus=1");
			query.setParameter(1, authToken);
			query.setParameter(2, id);
			String queryString = query.unwrap(org.hibernate.Query.class).getQueryString();
			System.out.println("Generated Query: " + queryString);
			List<Users> resultList = query.getResultList();
			if (!resultList.isEmpty()) {
				user = resultList.get(0);
				System.out.println(user.toString());
			}
		} catch (NoResultException ex) {
			ex.printStackTrace();
		}
		return user;
	}

	public static Users findByEmailNresetPasswordToken(EntityManager entityManager, String token, String email) {
		Users user = null;
		if (email == null) {
			return user;
		}
		try {
			Query query = entityManager
					.createQuery("SELECT obj FROM Users obj WHERE obj.email = ?1 AND obj.resetPasswordToken =?2");
			query.setParameter(1, email);
			query.setParameter(2, token);
			List<Users> resultList = query.getResultList();
			if (!resultList.isEmpty()) {
				user = resultList.get(0);
				System.out.println(user.toString());
			}
		} catch (NoResultException ex) {
		}
		return user;
	}

	public static Users findByEmailAddressAndPassword(EntityManager entityManager, String emailAddress, String password)
			throws Exception {
		Users user = null;
		if (emailAddress == null || password == null) {
			return user;
		}
		password = PasswordUtil.encrypt(password);
		System.out.println("SK >>>>>>>>>>>> User Details Pass " + password);
		Query query = entityManager.createQuery(USR_PWD_HQL);
		System.out.println("SK >>>>>>>>>>>> Inside UserModel Query" + query);
		query.setParameter(1, emailAddress);
		query.setParameter(2, password);
		System.out.println("SK >>>>>>>>>>>> User Details Query " + query);
		List<Users> users = query.getResultList();
		System.out.println("SK >>>>>>>>>>>> User Details " + users);
		if (users.size() > 0) {
			user = users.get(0);
		}
		return user;
	}

	public static Users findActiveByEmailAndPassword(EntityManager entityManager, String emailAddress,
			String password) {
		Users user = null;
		if (emailAddress == null || password == null) {
			return user;
		}
		try {
			password = PasswordUtil.encrypt(password);
			Query query = entityManager.createQuery(USR_ACTIVE_PASS_HQL);
			query.setParameter(1, emailAddress);
			query.setParameter(2, password);
			// user = (Users) query.getSingleResult();
			List<Users> resultList = query.getResultList();
			if (!resultList.isEmpty()) {
				user = resultList.get(0);
				System.out.println(user.toString());
			}
		} catch (NoResultException ex) {

		}
		return user;
	}

	public static Users findActiveByEmail(String emailAddress) {
		Users user = null;
		try {
			System.out.println("SK >>>>>>>>>>>> Inside User email " + emailAddress);
			if (emailAddress == null) {
				System.out.println("SK >>>>>>>>>>>> Inside User user " + user);
				return user;
			}
			System.out.println("SK >>>>>>>>>>>> Inside UserModel " + entityManager);
			Query query = entityManager.createQuery(USR_ACTIVE_HQL);
			System.out.println("SK >>>>>>>>>>>> Inside UserModel Query " + query);
			query.setParameter(1, emailAddress);
			List<Users> users = query.getResultList();
			System.out.println("SK >>>>>>>>>>>> Inside UserModel Query Response " + users.toString());
			if (users.size() > 0) {
				user = users.get(0);
			}
		} catch (NoResultException ex) {
			ex.printStackTrace();
		}
		return user;
	}

	public String getTxnCreationCoaIncome() {
		return this.txnCreationCoaIncome;
	}

	public void setTxnCreationCoaIncome(String txnCreationCoaIncome) {
		this.txnCreationCoaIncome = txnCreationCoaIncome;
	}

	public String getTxnCreationCoaIncomeFromAmount() {
		return this.txnCreationCoaIncomeFromAmount;
	}

	public void setTxnCreationCoaIncomeFromAmount(String txnCreationCoaIncomeFromAmount) {
		this.txnCreationCoaIncomeFromAmount = txnCreationCoaIncomeFromAmount;
	}

	public String getTxnCreationCoaIncomeToAmount() {
		return this.txnCreationCoaIncomeToAmount;
	}

	public void setTxnCreationCoaIncomeToAmount(String txnCreationCoaIncomeToAmount) {
		this.txnCreationCoaIncomeToAmount = txnCreationCoaIncomeToAmount;
	}

	public String getTxnCreationCoaExpense() {
		return this.txnCreationCoaExpense;
	}

	public void setTxnCreationCoaExpense(String txnCreationCoaExpense) {
		this.txnCreationCoaExpense = txnCreationCoaExpense;
	}

	public String getTxnCreationCoaExpenseFromAmount() {
		return this.txnCreationCoaExpenseFromAmount;
	}

	public void setTxnCreationCoaExpenseFromAmount(String txnCreationCoaExpenseFromAmount) {
		this.txnCreationCoaExpenseFromAmount = txnCreationCoaExpenseFromAmount;
	}

	public String getTxnCreationCoaExpenseToAmount() {
		return this.txnCreationCoaExpenseToAmount;
	}

	public void setTxnCreationCoaExpenseToAmount(String txnCreationCoaExpenseToAmount) {
		this.txnCreationCoaExpenseToAmount = txnCreationCoaExpenseToAmount;
	}

	public String getTxnCreationCoaAsset() {
		return this.txnCreationCoaAsset;
	}

	public void setTxnCreationCoaAsset(String txnCreationCoaAsset) {
		this.txnCreationCoaAsset = txnCreationCoaAsset;
	}

	public String getTxnCreationCoaAssetFromAmount() {
		return this.txnCreationCoaAssetFromAmount;
	}

	public void setTxnCreationCoaAssetFromAmount(String txnCreationCoaAssetFromAmount) {
		this.txnCreationCoaAssetFromAmount = txnCreationCoaAssetFromAmount;
	}

	public String getTxnCreationCoaAssetToAmount() {
		return this.txnCreationCoaAssetToAmount;
	}

	public void setTxnCreationCoaAssetToAmount(String txnCreationCoaAssetToAmount) {
		this.txnCreationCoaAssetToAmount = txnCreationCoaAssetToAmount;
	}

	public String getTxnCreationCoaLiabl() {
		return this.txnCreationCoaLiabl;
	}

	public void setTxnCreationCoaLiabl(String txnCreationCoaLiabl) {
		this.txnCreationCoaLiabl = txnCreationCoaLiabl;
	}

	public String getTxnCreationCoaLiablFromAmount() {
		return this.txnCreationCoaLiablFromAmount;
	}

	public void setTxnCreationCoaLiablFromAmount(String txnCreationCoaLiablFromAmount) {
		this.txnCreationCoaLiablFromAmount = txnCreationCoaLiablFromAmount;
	}

	public String getTxnCreationCoaLiablToAmount() {
		return this.txnCreationCoaLiablToAmount;
	}

	public void setTxnCreationCoaLiablToAmount(String txnCreationCoaLiablToAmount) {
		this.txnCreationCoaLiablToAmount = txnCreationCoaLiablToAmount;
	}

	public String getTxnApprovalCoaIncome() {
		return this.txnApprovalCoaIncome;
	}

	public void setTxnApprovalCoaIncome(String txnApprovalCoaIncome) {
		this.txnApprovalCoaIncome = txnApprovalCoaIncome;
	}

	public String getTxnApprovalCoaIncomeFromAmount() {
		return this.txnApprovalCoaIncomeFromAmount;
	}

	public void setTxnApprovalCoaIncomeFromAmount(String txnApprovalCoaIncomeFromAmount) {
		this.txnApprovalCoaIncomeFromAmount = txnApprovalCoaIncomeFromAmount;
	}

	public String getTxnApprovalCoaIncomeToAmount() {
		return this.txnApprovalCoaIncomeToAmount;
	}

	public void setTxnApprovalCoaIncomeToAmount(String txnApprovalCoaIncomeToAmount) {
		this.txnApprovalCoaIncomeToAmount = txnApprovalCoaIncomeToAmount;
	}

	public String getTxnApprovalCoaExpense() {
		return this.txnApprovalCoaExpense;
	}

	public void setTxnApprovalCoaExpense(String txnApprovalCoaExpense) {
		this.txnApprovalCoaExpense = txnApprovalCoaExpense;
	}

	public String getTxnApprovalCoaExpenseFromAmount() {
		return this.txnApprovalCoaExpenseFromAmount;
	}

	public void setTxnApprovalCoaExpenseFromAmount(String txnApprovalCoaExpenseFromAmount) {
		this.txnApprovalCoaExpenseFromAmount = txnApprovalCoaExpenseFromAmount;
	}

	public String getTxnApprovalCoaExpenseToAmount() {
		return this.txnApprovalCoaExpenseToAmount;
	}

	public void setTxnApprovalCoaExpenseToAmount(String txnApprovalCoaExpenseToAmount) {
		this.txnApprovalCoaExpenseToAmount = txnApprovalCoaExpenseToAmount;
	}

	public String getTxnApprovalCoaAsset() {
		return this.txnApprovalCoaAsset;
	}

	public void setTxnApprovalCoaAsset(String txnApprovalCoaAsset) {
		this.txnApprovalCoaAsset = txnApprovalCoaAsset;
	}

	public String getTxnApprovalCoaAssetFromAmount() {
		return this.txnApprovalCoaAssetFromAmount;
	}

	public void setTxnApprovalCoaAssetFromAmount(String txnApprovalCoaAssetFromAmount) {
		this.txnApprovalCoaAssetFromAmount = txnApprovalCoaAssetFromAmount;
	}

	public String getTxnApprovalCoaAssetToAmount() {
		return this.txnApprovalCoaAssetToAmount;
	}

	public void setTxnApprovalCoaAssetToAmount(String txnApprovalCoaAssetToAmount) {
		this.txnApprovalCoaAssetToAmount = txnApprovalCoaAssetToAmount;
	}

	public String getTxnApprovalCoaLiabl() {
		return this.txnApprovalCoaLiabl;
	}

	public void setTxnApprovalCoaLiabl(String txnApprovalCoaLiabl) {
		this.txnApprovalCoaLiabl = txnApprovalCoaLiabl;
	}

	public String getTxnApprovalCoaLiablFromAmount() {
		return this.txnApprovalCoaLiablFromAmount;
	}

	public void setTxnApprovalCoaLiablFromAmount(String txnApprovalCoaLiablFromAmount) {
		this.txnApprovalCoaLiablFromAmount = txnApprovalCoaLiablFromAmount;
	}

	public String getTxnApprovalCoaLiablToAmount() {
		return this.txnApprovalCoaLiablToAmount;
	}

	public void setTxnApprovalCoaLiablToAmount(String txnApprovalCoaLiablToAmount) {
		this.txnApprovalCoaLiablToAmount = txnApprovalCoaLiablToAmount;
	}

	public String getTxnAuditorCoaIncome() {
		return this.txnAuditorCoaIncome;
	}

	public void setTxnAuditorCoaIncome(String txnAuditorCoaIncome) {
		this.txnAuditorCoaIncome = txnAuditorCoaIncome;
	}

	public String getTxnAuditorCoaExpense() {
		return this.txnAuditorCoaExpense;
	}

	public void setTxnAuditorCoaExpense(String txnAuditorCoaExpense) {
		this.txnAuditorCoaExpense = txnAuditorCoaExpense;
	}

	public String getTxnAuditorCoaAsset() {
		return this.txnAuditorCoaAsset;
	}

	public void setTxnAuditorCoaAsset(String txnAuditorCoaAsset) {
		this.txnAuditorCoaAsset = txnAuditorCoaAsset;
	}

	public String getTxnAuditorCoaLiabl() {
		return this.txnAuditorCoaLiabl;
	}

	public void setTxnAuditorCoaLiabl(String txnAuditorCoaLiabl) {
		this.txnAuditorCoaLiabl = txnAuditorCoaLiabl;
	}

	public Integer getAgreedTermsAndContions() {
		return agreedTermsAndContions;
	}

	public void setAgreedTermsAndContions(Integer agreedTermsAndContions) {
		this.agreedTermsAndContions = agreedTermsAndContions;
	}

	public static List<Users> findByEmailActDeact(EntityManager entityManager, String emailAddress) {
		List<Users> users = null;
		if (emailAddress == null) {
			return users;
		}
		Query query = entityManager.createQuery(USR_ALL_HQL);
		query.setParameter(1, emailAddress);
		users = query.getResultList();
		return users;
	}

	public static List<Users> findAllActByOrg(EntityManager entityManager, Long orgId) {
		List<Users> users = null;
		if (orgId == null) {
			return users;
		}
		// Query query = JPA.em().createQuery(USR_ALL_EXCLUDE_EXT_USER);
		Query query = entityManager.createQuery(USR_ALL_EXCLUDE_EXT_USER);
		query.setParameter(1, orgId);
		users = query.getResultList();
		return users;
	}

	// To retrieve the list of external user
	public static List<Users> findAllExtUserActByOrg(EntityManager entityManager, Long orgId) {
		List<Users> users = null;
		if (orgId == null) {
			return users;
		}
		Query query = entityManager.createQuery(USR_ALL_EXT_USER);
		query.setParameter(1, orgId);
		users = query.getResultList();
		return users;
	}

	public String getUserRolesName() {
		String userRolesStr = "";
		List<UsersRoles> listuserRoles = this.getUserRoles();
		for (UsersRoles usrRole : listuserRoles) {
			userRolesStr += usrRole.getRole().getName() + ",";
		}
		String actUserRoles = userRolesStr.substring(0, userRolesStr.length());
		return actUserRoles;
	}

	public String getUserRoleIDs() {
		String userRolesStr = "";
		List<UsersRoles> listuserRoles = this.getUserRoles();
		for (UsersRoles usrRole : listuserRoles) {
			userRolesStr += usrRole.getRole().getId();
		}
		// String actUserRoles=userRolesStr.substring(0, userRolesStr.length());
		return userRolesStr;
	}

	public static boolean validateSecurityCode(EntityManager entityManager, String loginName, String securityCode) {
		boolean isValid = false;
		String query = "FROM Users u WHERE u.verificationCode = :securityCode AND u.email = :loginName";
		Query q = entityManager.createQuery(query);
		q.setParameter("securityCode", securityCode);
		q.setParameter("loginName", loginName);
		try {
			isValid = q.getSingleResult() == null ? false : true;
		} catch (NoResultException ex) {
			// getLogger().error("Security code not found", ex);
		}
		return isValid;
	}

	public String getModuleRights() {
		return this.moduleRights;
	}

	public void setModuleRights(String moduleRights) {
		this.moduleRights = moduleRights;
	}

	public Integer getTheme() {
		return this.theme;
	}

	public void setTheme(Integer theme) {
		this.theme = theme;
	}

	public Double getOpenigBalanceAdvance() {
		return openigBalanceAdvance;
	}

	public void setOpenigBalanceAdvance(Double openigBalanceAdvance) {
		this.openigBalanceAdvance = openigBalanceAdvance;
	}

	public Double getOpenigBalanceClaim() {
		return openigBalanceClaim;
	}

	public void setOpenigBalanceClaim(Double openigBalanceClaim) {
		this.openigBalanceClaim = openigBalanceClaim;
	}

}
