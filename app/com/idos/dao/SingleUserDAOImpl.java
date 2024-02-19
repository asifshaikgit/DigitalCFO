package com.idos.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import javax.inject.Inject;
import play.db.jpa.JPAApi;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.IdosConstants;

import controllers.CRUDController;
import model.AbstractBaseModel;
import model.Branch;
import model.BranchSpecifics;
import model.BranchUsers;
import model.BranchVendors;
import model.ConfigParams;
import model.CustomerDetail;
import model.Organization;
import model.Particulars;
import model.Project;
import model.Role;
import model.Specifics;
import model.Transaction;
import model.TransactionPurpose;
import model.UserRightForProject;
import model.UserRightInBranch;
import model.UserRightSpecifics;
import model.UserRights;
import model.UserTransactionPurpose;
import model.Users;
import model.UsersRoles;
import model.Vendor;
import model.VendorSpecific;
import play.libs.Json;
import service.CustomerService;
import play.Application;

/* Ankush A. Sapkal */

public class SingleUserDAOImpl implements SingleUserDAO {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	public static Application application;

	@Inject
	public SingleUserDAOImpl(Application application) {
		this.application = application;
	}

	private static CRUDController<Users> usercrud = new CRUDController<Users>(application);
	protected static CRUDController<UsersRoles> userrolecrud = new CRUDController<UsersRoles>(application);

	@Override
	public void updateOnOrganizationCreation(Users user, Organization org, Branch branch, EntityManager entityManager) {
		// Single User User Roll Mapping
		Map<String, Object> criterias = new HashMap<String, Object>();
		criterias.clear();
		criterias.put("user.id", user.getId());
		criterias.put("presentStatus", 1);
		List<UsersRoles> userRoles = genericDao.findByCriteria(UsersRoles.class, criterias, entityManager);
		List<Role> roles = Role.findRoleforSingleUser(entityManager);
		if (roles != null) {
			for (Role role : roles) {
				boolean addUserRole = true;
				for (UsersRoles usersRole : userRoles) {
					if (usersRole.getRole().getId() == role.getId()) {
						addUserRole = false;
					}
				}
				if (addUserRole) {
					UsersRoles newuserroles1 = new UsersRoles();
					newuserroles1.setRole(role);
					newuserroles1.setUser(Users.findById(user.getId()));
					newuserroles1.setBranch(Branch.findById(branch.getId()));
					newuserroles1.setOrganization(Organization.findById(org.getId()));
					userrolecrud.save(user, newuserroles1, entityManager);
				}
			}
		}

		// transaction Pourpose

		List<TransactionPurpose> txnPurposeList = TransactionPurpose.getSingleUserTransactionPurpose(entityManager);
		for (TransactionPurpose txnPurpose : txnPurposeList) {
			UserTransactionPurpose usrTxnPurpose = new UserTransactionPurpose();
			usrTxnPurpose.setBranch(branch);
			usrTxnPurpose.setOrganization(org);
			usrTxnPurpose.setUser(user);
			usrTxnPurpose.setTransactionPurpose(txnPurpose);
			genericDao.save(usrTxnPurpose, user, entityManager);
		}

		// Branch Budget
		branch.setBudgetDate(Calendar.getInstance().getTime());
		branch.setBudgetAmountJan(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountFeb(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountMar(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountApr(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountMay(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountJune(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountJuly(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountAug(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountSep(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountOct(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountNov(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountDec(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetTotal(IdosConstants.MAX_DOUBLE_VALUE);
		genericDao.save(branch, user, entityManager);

		// Vendor / Customer creation and Activation
		user.setAccessRights("1111");
		// Granted all Module Rights
		user.setModuleRights("11111111111111111111");

		// saveUser
		usercrud.save(user, user, entityManager);
	}

	@Override
	public void updateOnBranchCreation(Users user, Branch branch, EntityManager entityManager) {
		Map<String, Object> criterias = new HashMap<String, Object>();
		// Transaction Approval Branches

		if (user.getTransactionApprovalBranches() != null && !user.getTransactionApprovalBranches().equals("")) {
			StringBuilder transactionApprovalBranches = new StringBuilder();
			transactionApprovalBranches.append(user.getTransactionApprovalBranches()).append(",")
					.append(branch.getId());
			user.setTransactionApprovalBranches(transactionApprovalBranches.toString());
		} else {
			user.setTransactionApprovalBranches(branch.getId().toString());
		}

		// Transaction Creation Branches

		if (user.getTransactionCreationBranches() != null && !user.getTransactionCreationBranches().equals("")) {
			StringBuilder transactionCreationalBranches = new StringBuilder();
			transactionCreationalBranches.append(user.getTransactionCreationBranches()).append(",")
					.append(branch.getId());
			user.setTransactionCreationBranches(transactionCreationalBranches.toString());
		} else {
			user.setTransactionCreationBranches(branch.getId().toString());
		}

		// Transaction Auditor Branches

		if (user.getTransactionAuditorBranches() != null && !user.getTransactionAuditorBranches().equals("")) {
			StringBuilder transactionAuditorBranches = new StringBuilder();
			transactionAuditorBranches.append(user.getTransactionAuditorBranches()).append(",").append(branch.getId());
			user.setTransactionAuditorBranches(transactionAuditorBranches.toString());
		} else {
			user.setTransactionAuditorBranches(branch.getId().toString());
		}

		// Branch Budget
		branch.setBudgetDate(Calendar.getInstance().getTime());
		branch.setBudgetAmountJan(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountFeb(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountMar(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountApr(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountMay(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountJune(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountJuly(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountAug(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountSep(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountOct(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountNov(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetAmountDec(IdosConstants.MAX_DOUBLE_VALUE);
		branch.setBudgetTotal(IdosConstants.MAX_DOUBLE_VALUE);
		genericDao.save(branch, user, entityManager);

		// User Branches

		criterias.put("user.id", user.getId());
		criterias.put("presentStatus", 1);
		List<BranchUsers> userBranches = genericDao.findByCriteria(BranchUsers.class, criterias, entityManager);
		boolean addUserBranch = true;
		for (BranchUsers branchUsers : userBranches) {
			if (branchUsers.getBranch().getId() == branch.getId()) {
				addUserBranch = false;
			}
		}
		if (addUserBranch) {
			BranchUsers newUserBranch = new BranchUsers();
			newUserBranch.setBranch(branch);
			newUserBranch.setBranchOrganization(user.getOrganization());
			newUserBranch.setUser(user);
			genericDao.save(newUserBranch, user, entityManager);
		}

		// Branch Has Customer/Vendor
		criterias.clear();
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		List<Vendor> custVendors = genericDao.findByCriteria(Vendor.class, criterias, entityManager);
		for (Vendor vendor : custVendors) {
			BranchVendors branchCustVend = new BranchVendors();
			branchCustVend.setOrganization(user.getOrganization());
			branchCustVend.setBranch(branch);
			branchCustVend.setVendor(vendor);
			genericDao.save(branchCustVend, user, entityManager);
		}

		// Branch Has COA
		criterias.clear();
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		List<Specifics> secifics = genericDao.findByCriteria(Specifics.class, criterias, entityManager);
		for (Specifics specifics : secifics) {
			BranchSpecifics branchSpecs = new BranchSpecifics();
			branchSpecs.setOrganization(user.getOrganization());
			branchSpecs.setBranch(branch);
			branchSpecs.setSpecifics(specifics);
			branchSpecs.setParticular(specifics.getParticularsId());
			genericDao.save(branchSpecs, user, entityManager);
		}

		// Save Changes
		genericDao.saveOrUpdate(user, user, entityManager);

		// user Right

		criterias.clear();
		criterias.put("user.id", user.getId());
		criterias.put("presentStatus", 1);
		List<UserRightInBranch> userRightsInBranches = genericDao.findByCriteria(UserRightInBranch.class, criterias,
				entityManager);
		boolean addUserRight = true;
		UserRights usrRights = UserRights.findById(1L);
		UserRights usrRights1 = UserRights.findById(2L);
		UserRights usrRights2 = UserRights.findById(3L);
		for (UserRightInBranch userRightInBranch2 : userRightsInBranches) {
			if (userRightInBranch2.getBranch().getId() == branch.getId()) {
				addUserRight = false;
				break;
			}
		}
		if (addUserRight) {
			UserRightInBranch userRightInBranch = new UserRightInBranch();
			userRightInBranch.setUser(user);
			userRightInBranch.setBranch(branch);
			userRightInBranch.setOrganization(user.getOrganization());
			userRightInBranch.setUserRights(usrRights);
			genericDao.save(userRightInBranch, user, entityManager);

			// Approval
			UserRightInBranch userRightInBranch1 = new UserRightInBranch();
			userRightInBranch1.setUser(user);
			userRightInBranch1.setBranch(branch);
			userRightInBranch1.setOrganization(user.getOrganization());
			userRightInBranch1.setUserRights(usrRights1);
			genericDao.save(userRightInBranch1, user, entityManager);

			// Auditor
			UserRightInBranch userRightInBranch2 = new UserRightInBranch();
			userRightInBranch2.setUser(user);
			userRightInBranch2.setBranch(branch);
			userRightInBranch2.setOrganization(user.getOrganization());
			userRightInBranch2.setUserRights(usrRights2);
			genericDao.save(userRightInBranch2, user, entityManager);
		}
	}

	@Override
	public void updateOnCOACreation(Users user, Specifics specific, EntityManager entityManager) {
		Map<String, Object> criterias = new HashMap<String, Object>();
		// Changes For Customer and Vendor
		List<Vendor> vendors = null;
		Long accountCode = specific.getAccountCode() == null ? 0L : specific.getAccountCode();

		if (accountCode >= 1000000000000000000L && accountCode < 2000000000000000000L) {
			// Income Item Added
			vendors = Vendor.findByOrgIdAndType(entityManager, user.getOrganization().getId(), 2);
			// user Changes for Income
			user = userChangesForCOAIncome(user, specific);
		} else if (accountCode >= 2000000000000000000L && accountCode < 3000000000000000000L) {
			// Expense Item Added
			vendors = Vendor.findByOrgIdAndType(entityManager, user.getOrganization().getId(), 1);
			// user Changes for Expense
			user = userChangesForCOAExpense(user, specific);
			// Branch COA Budget for Expense
			criterias.clear();
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("specifics.id", specific.getId());
			criterias.put("presentStatus", 1);
			List<BranchSpecifics> brancheSpecifics = genericDao.findByCriteria(BranchSpecifics.class, criterias,
					entityManager);
			for (BranchSpecifics branchSpec : brancheSpecifics) {
				branchSpec.setBudgetAmountJan(IdosConstants.MAX_DOUBLE_VALUE);
				branchSpec.setBudgetAmountFeb(IdosConstants.MAX_DOUBLE_VALUE);
				branchSpec.setBudgetAmountMar(IdosConstants.MAX_DOUBLE_VALUE);
				branchSpec.setBudgetAmountApr(IdosConstants.MAX_DOUBLE_VALUE);
				branchSpec.setBudgetAmountMay(IdosConstants.MAX_DOUBLE_VALUE);
				branchSpec.setBudgetAmountJune(IdosConstants.MAX_DOUBLE_VALUE);
				branchSpec.setBudgetAmountJuly(IdosConstants.MAX_DOUBLE_VALUE);
				branchSpec.setBudgetAmountAug(IdosConstants.MAX_DOUBLE_VALUE);
				branchSpec.setBudgetAmountSep(IdosConstants.MAX_DOUBLE_VALUE);
				branchSpec.setBudgetAmountOct(IdosConstants.MAX_DOUBLE_VALUE);
				branchSpec.setBudgetAmountNov(IdosConstants.MAX_DOUBLE_VALUE);
				branchSpec.setBudgetAmountDec(IdosConstants.MAX_DOUBLE_VALUE);
				branchSpec.setBudgetTotal(IdosConstants.MAX_DOUBLE_VALUE);
				genericDao.saveOrUpdate(branchSpec, user, entityManager);
			}
		} else if (accountCode >= 3000000000000000000L && accountCode < 4000000000000000000L) {
			// user Changes for Asset
			user = userChangesForCOAAsset(user, specific);
		} else if (accountCode >= 4000000000000000000L && accountCode < 5000000000000000000L) {
			// user Changes for Liabl
			user = userChangesForCOALiabl(user, specific);
		}
		// common code for Update cust and Vend
		if (vendors != null) {
			for (Vendor vendor : vendors) {
				VendorSpecific vennSpecf = new VendorSpecific();
				Particulars newVendParticulars = specific.getParticularsId();
				vennSpecf.setVendorSpecific(vendor);
				vennSpecf.setSpecificsVendors(specific);
				vennSpecf.setBranch(user.getBranch());
				vennSpecf.setOrganization(user.getOrganization());
				if (vendor.getType() == 1) {
					vennSpecf.setDiscountPercentage(100d);
				}
				vennSpecf.setParticulars(newVendParticulars);
				genericDao.saveOrUpdate(vennSpecf, user, entityManager);

				String vendorSpecifics = vendor.getVendorSpecifics();
				if (vendorSpecifics != null && !vendorSpecifics.equals("")) {
					vendorSpecifics += "," + specific.getId();
				} else {
					vendorSpecifics = "" + specific.getId();
				}

				String custSpecifics = vendor.getCustomerSpecifics();
				String custSpecificsDisc = vendor.getCustomerSpecificsDiscountPercentage();
				if (custSpecifics != null && !custSpecifics.equals("")) {
					custSpecifics += "," + specific.getId();
				} else {
					custSpecifics = "" + specific.getId();
				}
				if (custSpecificsDisc != null && !custSpecificsDisc.equals("")) {
					custSpecificsDisc += ",100";
				} else {
					custSpecificsDisc = "100";
				}

				vendor.setVendorSpecifics(vendorSpecifics);
				vendor.setCustomerSpecifics(custSpecifics);
				vendor.setCustomerSpecificsDiscountPercentage(custSpecificsDisc);
				genericDao.saveOrUpdate(vendor, user, entityManager);
			}
		}

		// Changes for User
		boolean addUserRightSpecifics = true;
		UserRights usrRights = UserRights.findById(1L);
		UserRights usrRights1 = UserRights.findById(2L);
		UserRights usrRights2 = UserRights.findById(3L);
		criterias.clear();
		criterias.put("user.id", user.getId());
		criterias.put("presentStatus", 1);
		List<UserRightSpecifics> userRightsSpecifics = genericDao.findByCriteria(UserRightSpecifics.class, criterias,
				entityManager);

		for (UserRightSpecifics userRightSpecifics : userRightsSpecifics) {
			if (userRightSpecifics.getSpecifics().getId() == specific.getId()) {
				addUserRightSpecifics = false;
				break;
			}
		}

		if (addUserRightSpecifics) {
			// UserRight userCreationRightForCOA
			UserRightSpecifics userRightSpecifics = new UserRightSpecifics();
			userRightSpecifics.setUser(user);
			userRightSpecifics.setSpecifics(specific);
			userRightSpecifics.setParticulars(specific.getParticularsId());
			userRightSpecifics.setAmount(0.0d);
			userRightSpecifics.setAmountTo(IdosConstants.MAX_DOUBLE_VALUE);
			// creation
			userRightSpecifics.setUserRights(usrRights);
			genericDao.saveOrUpdate(userRightSpecifics, user, entityManager);

			// Approval
			UserRightSpecifics userRightSpecifics1 = new UserRightSpecifics();
			userRightSpecifics1.setUser(user);
			userRightSpecifics1.setSpecifics(specific);
			userRightSpecifics1.setParticulars(specific.getParticularsId());
			userRightSpecifics1.setAmount(0.0d);
			userRightSpecifics1.setAmountTo(IdosConstants.MAX_DOUBLE_VALUE);
			userRightSpecifics1.setUserRights(usrRights1);
			genericDao.saveOrUpdate(userRightSpecifics1, user, entityManager);
			// Auditor
			UserRightSpecifics userRightSpecifics2 = new UserRightSpecifics();
			userRightSpecifics2.setUser(user);
			userRightSpecifics2.setSpecifics(specific);
			userRightSpecifics2.setParticulars(specific.getParticularsId());
			userRightSpecifics2.setAmount(0.0d);
			userRightSpecifics2.setAmountTo(IdosConstants.MAX_DOUBLE_VALUE);
			userRightSpecifics2.setUserRights(usrRights2);
			genericDao.saveOrUpdate(userRightSpecifics2, user, entityManager);
		}
		// Update User
		genericDao.saveOrUpdate(user, user, entityManager);
	}

	@Override
	public void updateOnProjectCreation(Users user, Project project, EntityManager entityManager) {
		Map<String, Object> criterias = new HashMap<String, Object>();
		criterias.clear();
		criterias.put("user.id", user.getId());
		criterias.put("presentStatus", 1);
		List<UserRights> rights = genericDao.findByCriteria(UserRights.class, criterias, entityManager);

		for (UserRights userRight : rights) {
			UserRightForProject userRightForProject = new UserRightForProject();
			userRightForProject.setUser(user);
			userRightForProject.setUserRights(userRight);
			userRightForProject.setProject(project);
			genericDao.saveOrUpdate(userRightForProject, user, entityManager);
		}

		// UserRights
		boolean addUserRightProject = true;

		UserRights usrRights = UserRights.findById(1L);
		UserRights usrRights1 = UserRights.findById(2L);
		UserRights usrRights2 = UserRights.findById(3L);
		criterias.clear();
		criterias.put("user.id", user.getId());
		criterias.put("presentStatus", 1);
		List<UserRightForProject> userRightForProjects = genericDao.findByCriteria(UserRightForProject.class, criterias,
				entityManager);

		for (UserRightForProject userRightForProject : userRightForProjects) {
			if (userRightForProject.getUserRights().getId() == usrRights.getId()) {
				addUserRightProject = false;
				break;
			}
		}
		if (addUserRightProject) {
			// Creation
			UserRightForProject userRightForProject = new UserRightForProject();
			userRightForProject.setUser(user);
			userRightForProject.setProject(project);
			userRightForProject.setUserRights(usrRights);
			genericDao.saveOrUpdate(userRightForProject, user, entityManager);

			// Approval
			UserRightForProject userRightForProject1 = new UserRightForProject();
			userRightForProject1.setUser(user);
			userRightForProject1.setProject(project);
			userRightForProject1.setUserRights(usrRights1);
			genericDao.saveOrUpdate(userRightForProject1, user, entityManager);

			// Auditor
			UserRightForProject userRightForProject2 = new UserRightForProject();
			userRightForProject2.setUser(user);
			userRightForProject2.setProject(project);
			userRightForProject2.setUserRights(usrRights2);
			genericDao.saveOrUpdate(userRightForProject2, user, entityManager);
		}
	}

	private Users userChangesForCOAIncome(Users user, Specifics specific) {

		// User Setup Changes for Creation
		if (user.getTxnCreationCoaIncome() != null && !user.getTxnCreationCoaIncome().equals("")) {
			// specific
			StringBuilder strCreationIncome = new StringBuilder(user.getTxnCreationCoaIncome());
			strCreationIncome.append(",").append(specific.getId());
			user.setTxnCreationCoaIncome(strCreationIncome.toString());
			// from
			strCreationIncome = new StringBuilder(user.getTxnCreationCoaIncomeFromAmount());
			strCreationIncome.append(",").append("0");
			user.setTxnCreationCoaIncomeFromAmount(strCreationIncome.toString());
			// to
			strCreationIncome = new StringBuilder(user.getTxnCreationCoaIncomeToAmount());
			strCreationIncome.append(",").append("" + IdosConstants.MAX_DOUBLE_VALUE);
			user.setTxnCreationCoaIncomeToAmount(strCreationIncome.toString());
		} else {
			user.setTxnCreationCoaIncome(specific.getId().toString());
			user.setTxnCreationCoaIncomeFromAmount("0");
			user.setTxnCreationCoaIncomeToAmount("" + IdosConstants.MAX_DOUBLE_VALUE);
		}

		// User Setup Changes for Approval
		if (user.getTxnApprovalCoaIncome() != null && !user.getTxnApprovalCoaIncome().equals("")) {
			// specific
			StringBuilder strApprovalIncome = new StringBuilder(user.getTxnApprovalCoaIncome());
			strApprovalIncome.append(",").append(specific.getId());
			user.setTxnApprovalCoaIncome(strApprovalIncome.toString());
			// from
			strApprovalIncome = new StringBuilder(user.getTxnApprovalCoaIncomeFromAmount());
			strApprovalIncome.append(",").append("0");
			user.setTxnApprovalCoaIncomeFromAmount(strApprovalIncome.toString());
			// to
			strApprovalIncome = new StringBuilder(user.getTxnApprovalCoaIncomeToAmount());
			strApprovalIncome.append(",").append("" + IdosConstants.MAX_DOUBLE_VALUE);
			user.setTxnApprovalCoaIncomeToAmount(strApprovalIncome.toString());
		} else {
			user.setTxnApprovalCoaIncome(specific.getId().toString());
			user.setTxnApprovalCoaIncomeFromAmount("0");
			user.setTxnApprovalCoaIncomeToAmount("" + IdosConstants.MAX_DOUBLE_VALUE);
		}

		// User Setup Changes for Auditor
		if (user.getTxnAuditorCoaIncome() != null && !user.getTxnAuditorCoaIncome().equals("")) {
			// specific
			StringBuilder strAuditorIncome = new StringBuilder(user.getTxnAuditorCoaIncome());
			strAuditorIncome.append(",").append(specific.getId());
			user.setTxnAuditorCoaIncome(strAuditorIncome.toString());
		} else {
			user.setTxnAuditorCoaIncome(specific.getId().toString());

		}

		return user;
	}

	private Users userChangesForCOAExpense(Users user, Specifics specific) {

		// User Setup Changes for Creation
		if (user.getTxnCreationCoaExpense() != null && !user.getTxnCreationCoaExpense().equals("")) {
			// specific
			StringBuilder strCreationExpense = new StringBuilder(user.getTxnCreationCoaExpense());
			strCreationExpense.append(",").append(specific.getId());
			user.setTxnCreationCoaExpense(strCreationExpense.toString());
			// from
			strCreationExpense = new StringBuilder(user.getTxnCreationCoaExpenseFromAmount());
			strCreationExpense.append(",").append("0");
			user.setTxnCreationCoaExpenseFromAmount(strCreationExpense.toString());
			// to
			strCreationExpense = new StringBuilder(user.getTxnCreationCoaExpenseToAmount());
			strCreationExpense.append(",").append("" + IdosConstants.MAX_DOUBLE_VALUE);
			user.setTxnCreationCoaExpenseToAmount(strCreationExpense.toString());
		} else {
			user.setTxnCreationCoaExpense(specific.getId().toString());
			user.setTxnCreationCoaExpenseFromAmount("0");
			user.setTxnCreationCoaExpenseToAmount("" + IdosConstants.MAX_DOUBLE_VALUE);
		}

		// User Setup Changes for Approval
		if (user.getTxnApprovalCoaExpense() != null && !user.getTxnApprovalCoaExpense().equals("")) {
			// specific
			StringBuilder strApprovalExpense = new StringBuilder(user.getTxnApprovalCoaExpense());
			strApprovalExpense.append(",").append(specific.getId());
			user.setTxnApprovalCoaExpense(strApprovalExpense.toString());
			// from
			strApprovalExpense = new StringBuilder(user.getTxnApprovalCoaExpenseFromAmount());
			strApprovalExpense.append(",").append("0");
			user.setTxnApprovalCoaExpenseFromAmount(strApprovalExpense.toString());
			// to
			strApprovalExpense = new StringBuilder(user.getTxnApprovalCoaExpenseToAmount());
			strApprovalExpense.append(",").append("" + IdosConstants.MAX_DOUBLE_VALUE);
			user.setTxnApprovalCoaExpenseToAmount(strApprovalExpense.toString());
		} else {
			user.setTxnApprovalCoaExpense(specific.getId().toString());
			user.setTxnApprovalCoaExpenseFromAmount("0");
			user.setTxnApprovalCoaExpenseToAmount("" + IdosConstants.MAX_DOUBLE_VALUE);
		}

		// User Setup Changes for Auditor
		if (user.getTxnAuditorCoaExpense() != null && !user.getTxnAuditorCoaExpense().equals("")) {
			// specific
			StringBuilder strAuditorExpense = new StringBuilder(user.getTxnAuditorCoaExpense());
			strAuditorExpense.append(",").append(specific.getId());
			user.setTxnAuditorCoaExpense(strAuditorExpense.toString());
		} else {
			user.setTxnAuditorCoaExpense(specific.getId().toString());

		}

		return user;
	}

	private Users userChangesForCOAAsset(Users user, Specifics specific) {

		// User Setup Changes for Creation
		if (user.getTxnCreationCoaAsset() != null && !user.getTxnCreationCoaAsset().equals("")) {
			// specific
			StringBuilder strCreationAsset = new StringBuilder(user.getTxnCreationCoaAsset());
			strCreationAsset.append(",").append(specific.getId());
			user.setTxnCreationCoaAsset(strCreationAsset.toString());
			// from
			strCreationAsset = new StringBuilder(user.getTxnCreationCoaAssetFromAmount());
			strCreationAsset.append(",").append("0");
			user.setTxnCreationCoaAssetFromAmount(strCreationAsset.toString());
			// to
			strCreationAsset = new StringBuilder(user.getTxnCreationCoaAssetToAmount());
			strCreationAsset.append(",").append("" + IdosConstants.MAX_DOUBLE_VALUE);
			user.setTxnCreationCoaAssetToAmount(strCreationAsset.toString());
		} else {
			user.setTxnCreationCoaAsset(specific.getId().toString());
			user.setTxnCreationCoaAssetFromAmount("0");
			user.setTxnCreationCoaAssetToAmount("" + IdosConstants.MAX_DOUBLE_VALUE);
		}

		// User Setup Changes for Approval
		if (user.getTxnApprovalCoaAsset() != null && !user.getTxnApprovalCoaAsset().equals("")) {
			// specific
			StringBuilder strApprovalAsset = new StringBuilder(user.getTxnApprovalCoaAsset());
			strApprovalAsset.append(",").append(specific.getId());
			user.setTxnApprovalCoaAsset(strApprovalAsset.toString());
			// from
			strApprovalAsset = new StringBuilder(user.getTxnApprovalCoaAssetFromAmount());
			strApprovalAsset.append(",").append("0");
			user.setTxnApprovalCoaAssetFromAmount(strApprovalAsset.toString());
			// to
			strApprovalAsset = new StringBuilder(user.getTxnApprovalCoaAssetToAmount());
			strApprovalAsset.append(",").append("" + IdosConstants.MAX_DOUBLE_VALUE);
			user.setTxnApprovalCoaAssetToAmount(strApprovalAsset.toString());
		} else {
			user.setTxnApprovalCoaAsset(specific.getId().toString());
			user.setTxnApprovalCoaAssetFromAmount("0");
			user.setTxnApprovalCoaAssetToAmount("" + IdosConstants.MAX_DOUBLE_VALUE);
		}

		// User Setup Changes for Auditor
		if (user.getTxnAuditorCoaAsset() != null && !user.getTxnAuditorCoaAsset().equals("")) {
			// specific
			StringBuilder strAuditorAsset = new StringBuilder(user.getTxnAuditorCoaAsset());
			strAuditorAsset.append(",").append(specific.getId());
			user.setTxnAuditorCoaAsset(strAuditorAsset.toString());
		} else {
			user.setTxnAuditorCoaAsset(specific.getId().toString());

		}

		return user;
	}

	private Users userChangesForCOALiabl(Users user, Specifics specific) {

		// User Setup Changes for Creation
		if (user.getTxnCreationCoaLiabl() != null && !user.getTxnCreationCoaLiabl().equals("")) {
			// specific
			StringBuilder strCreationLiabl = new StringBuilder(user.getTxnCreationCoaLiabl());
			strCreationLiabl.append(",").append(specific.getId());
			user.setTxnCreationCoaLiabl(strCreationLiabl.toString());
			// from
			strCreationLiabl = new StringBuilder(user.getTxnCreationCoaLiablFromAmount());
			strCreationLiabl.append(",").append("0");
			user.setTxnCreationCoaLiablFromAmount(strCreationLiabl.toString());
			// to
			strCreationLiabl = new StringBuilder(user.getTxnCreationCoaLiablToAmount());
			strCreationLiabl.append(",").append("" + IdosConstants.MAX_DOUBLE_VALUE);
			user.setTxnCreationCoaLiablToAmount(strCreationLiabl.toString());
		} else {
			user.setTxnCreationCoaLiabl(specific.getId().toString());
			user.setTxnCreationCoaLiablFromAmount("0");
			user.setTxnCreationCoaLiablToAmount("" + IdosConstants.MAX_DOUBLE_VALUE);
		}

		// User Setup Changes for Approval
		if (user.getTxnApprovalCoaLiabl() != null && !user.getTxnApprovalCoaLiabl().equals("")) {
			// Specific
			StringBuilder strApprovalLiabl = new StringBuilder(user.getTxnApprovalCoaLiabl());
			strApprovalLiabl.append(",").append(specific.getId());
			user.setTxnApprovalCoaLiabl(strApprovalLiabl.toString());
			// From
			strApprovalLiabl = new StringBuilder(user.getTxnApprovalCoaLiablFromAmount());
			strApprovalLiabl.append(",").append("0");
			user.setTxnApprovalCoaLiablFromAmount(strApprovalLiabl.toString());
			// To
			strApprovalLiabl = new StringBuilder(user.getTxnApprovalCoaLiablToAmount());
			strApprovalLiabl.append(",").append("" + IdosConstants.MAX_DOUBLE_VALUE);
			user.setTxnApprovalCoaLiablToAmount(strApprovalLiabl.toString());
		} else {
			user.setTxnApprovalCoaLiabl(specific.getId().toString());
			user.setTxnApprovalCoaLiablFromAmount("0");
			user.setTxnApprovalCoaLiablToAmount("" + IdosConstants.MAX_DOUBLE_VALUE);
		}

		// User Setup Changes for Auditor
		if (user.getTxnAuditorCoaLiabl() != null && !user.getTxnAuditorCoaLiabl().equals("")) {
			// specific
			StringBuilder strAuditorLiabl = new StringBuilder(user.getTxnAuditorCoaLiabl());
			strAuditorLiabl.append(",").append(specific.getId());
			user.setTxnAuditorCoaLiabl(strAuditorLiabl.toString());
		} else {
			user.setTxnAuditorCoaLiabl(specific.getId().toString());
		}

		return user;
	}

	public ObjectNode createSingleuserJson(Transaction txn, JsonNode json, Users user) {
		ObjectNode row = Json.newObject();
		if (ConfigParams.getInstance().isDeploymentSingleUser(user)) {
			String suppDoc = json.findValue("supportingdoc") != null ? json.findValue("supportingdoc").asText() : "";
			String txnRemarks = json.findValue("txnRemarks") != null ? json.findValue("txnRemarks").asText() : "";
			String paymentDetails = json.findValue("txnReceiptDetails") != null
					? json.findValue("txnReceiptDetails").asText()
					: "";
			String txnPaymentBank = json.findValue("txnreceiptPaymentBank") != null
					? json.findValue("txnreceiptPaymentBank").asText()
					: "";
			String txnInstrumentNum = json.findValue("txnInstrumentNum") != null
					? json.findValue("txnInstrumentNum").asText()
					: "";
			String txnInstrumentDate = json.findValue("txnInstrumentDate") != null
					? json.findValue("txnInstrumentDate").asText()
					: "";
			String bankInf = json.findValue("txnReceiptTypeBankDetails") != null
					? json.findValue("txnReceiptTypeBankDetails").asText()
					: "";
			String useremail = json.findValue("useremail") != null ? json.findValue("useremail").asText() : "";
			if (txn.getId() != null) {
				row.put("transactionPrimId", txn.getId().toString());
			} else {
				row.put("transactionPrimId", "");
			}
			row.put("suppDoc", suppDoc);
			row.put("txnRemarks", txnRemarks);
			row.put("paymentDetails", paymentDetails);
			if (txn.getTransactionDate() != null) {
				row.put("txnInvDate", IdosConstants.idosdf.format(txn.getTransactionDate()));
			} else {
				row.put("txnInvDate", "");
			}
			row.put("txnPaymentBank", txnPaymentBank);
			row.put("txnInstrumentNum", txnInstrumentNum);
			row.put("txnInstrumentDate", txnInstrumentDate);
			row.put("bankInf", bankInf);
			row.put("selectedApproverAction", "1");
			row.put("selectedAddApproverEmail", "");
			row.put("useremail", useremail);
		}
		return row;
	}

}
