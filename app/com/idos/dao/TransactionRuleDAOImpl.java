package com.idos.dao;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Branch;
import model.ClaimTransaction;
import model.Specifics;
import model.Transaction;
import model.UserRightInBranch;
import model.UserRightSpecifics;
import model.Users;
import model.UsersRoles;
import model.Vendor;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.inject.Inject;
import play.db.jpa.JPAApi;
import play.libs.Json;

public class TransactionRuleDAOImpl implements TransactionRuleDAO {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Override
	public ObjectNode ruleBasedUserExistence(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager) {
		log.log(Level.FINE, "************* Start " + json);
		result.put("result", false);
		ArrayNode transactionRuleBasedExistencean = result.putArray("transactionRuleBasedExistence");
		String txnItemId = json.findValue("txnItemId") != null ? json.findValue("txnItemId").asText() : null;
		String txnBranchId = json.findValue("txnBranchId") != null ? json.findValue("txnBranchId").asText() : null;
		String transactionNetAmount = json.findValue("transactionNetAmount") != null
				? json.findValue("transactionNetAmount").asText()
				: null;
		String transactionParameter = json.findValue("transactionParameter") != null
				? json.findValue("transactionParameter").asText()
				: null;
		String txnInv = json.findValue("txnInv") != null ? json.findValue("txnInv").asText() : null;
		String txnCreditVendor = json.findValue("txnCreditVendor") != null ? json.findValue("txnCreditVendor").asText()
				: null;
		String claimTransactionRefNumber = json.findValue("claimTransactionRefNumber") != null
				? json.findValue("claimTransactionRefNumber").asText()
				: null;
		Map<String, Object> criterias = new HashMap<String, Object>();
		Specifics specf = null;
		Branch bnch = null;
		Transaction txn = null;
		Vendor vendor = null;
		ClaimTransaction claimTxn = null;
		if (txnItemId != null && !txnItemId.equals("")) {
			specf = Specifics.findById(Long.parseLong(txnItemId));
		}
		if (txnBranchId != null && !txnBranchId.equals("")) {
			bnch = Branch.findById(Long.parseLong(txnBranchId));
		}
		if (txnInv != null && !txnInv.equals("")) {
			txn = Transaction.findById(Long.parseLong(txnInv));
		}
		if (txnCreditVendor != null) {
			vendor = Vendor.findById(Long.parseLong(txnCreditVendor));
		}
		if (claimTransactionRefNumber != null && !claimTransactionRefNumber.equals("")) {
			claimTxn = ClaimTransaction.findById(Long.parseLong(claimTransactionRefNumber));
		}
		if (transactionParameter.equals("inventorystocktransfer")) {
			if (specf != null && bnch != null) {// in case of buy/pay transactions only approveremail exist or not check
												// is required
				criterias.clear();
				criterias.put("role.name", "APPROVER");
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("user.presentStatus", 1);
				List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, entityManager);
				String approverEmails = "";
				for (UsersRoles usrRoles : approverRole) {
					criterias.clear();
					criterias.put("user.id", usrRoles.getUser().getId());
					criterias.put("userRights.id", 2L);
					criterias.put("branch.id", bnch.getId());
					criterias.put("presentStatus", 1);
					UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class,
							criterias, entityManager);
					if (userHasRightInBranch != null) {
						// check for right in chart of accounts
						criterias.clear();
						criterias.put("user.id", usrRoles.getUser().getId());
						criterias.put("userRights.id", 2L);
						criterias.put("specifics.id", specf.getId());
						criterias.put("presentStatus", 1);
						UserRightSpecifics userHasRightInCOA = genericDao.getByCriteria(UserRightSpecifics.class,
								criterias, entityManager);
						if (userHasRightInCOA != null) {
							boolean userAmtLimit = true;
							if (userAmtLimit == true) {
								approverEmails += usrRoles.getUser().getEmail() + ",";
							}
						}
					}
				}
				if (approverEmails != null && !approverEmails.equals("")) {
					result.put("result", true);
					ObjectNode row = Json.newObject();
					row.put("userExistence", "Exist");
					transactionRuleBasedExistencean.add(row);
				} else {
					result.put("result", true);
					ObjectNode row = Json.newObject();
					row.put("userExistence", "Does Not Exist");
					transactionRuleBasedExistencean.add(row);
				}
			}
		}
		if (transactionParameter.equals("transaction")) {
			if (specf != null && bnch != null) {// in case of buy/pay transactions only approveremail exist or not check
												// is required
				criterias.clear();
				criterias.put("role.name", "APPROVER");
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("user.presentStatus", 1);
				List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, entityManager);
				String approverEmails = "";
				for (UsersRoles usrRoles : approverRole) {
					criterias.clear();
					criterias.put("user.id", usrRoles.getUser().getId());
					criterias.put("userRights.id", 2L);
					criterias.put("branch.id", bnch.getId());
					criterias.put("presentStatus", 1);
					UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class,
							criterias, entityManager);
					if (userHasRightInBranch != null) {
						// check for right in chart of accounts
						criterias.clear();
						criterias.put("user.id", usrRoles.getUser().getId());
						criterias.put("userRights.id", 2L);
						criterias.put("specifics.id", specf.getId());
						criterias.put("presentStatus", 1);
						UserRightSpecifics userHasRightInCOA = genericDao.getByCriteria(UserRightSpecifics.class,
								criterias, entityManager);
						if (userHasRightInCOA != null) {
							boolean userAmtLimit = false;
							if (userHasRightInCOA.getAmount() != null) {
								if (userHasRightInCOA.getAmount() > 0) {
									if (Double.parseDouble(transactionNetAmount) > userHasRightInCOA.getAmount()) {
										userAmtLimit = false;
									}
									if (Double.parseDouble(transactionNetAmount) < userHasRightInCOA.getAmount()) {
										userAmtLimit = true;
									}
								}
							}
							if (userHasRightInCOA.getAmountTo() != null)
								if (userHasRightInCOA.getAmountTo() > 0) {
									if (Double.parseDouble(transactionNetAmount) > userHasRightInCOA.getAmountTo()) {
										userAmtLimit = false;
									}
									if (Double.parseDouble(transactionNetAmount) < userHasRightInCOA.getAmountTo()) {
										userAmtLimit = true;
									}
								}
							if (userAmtLimit == true) {
								approverEmails += usrRoles.getUser().getEmail() + ",";
							}
						}
					}
				}
				if (approverEmails != null && !approverEmails.equals("")) {
					result.put("result", true);
					ObjectNode row = Json.newObject();
					row.put("userExistence", "Exist");
					transactionRuleBasedExistencean.add(row);
				} else {
					result.put("result", true);
					ObjectNode row = Json.newObject();
					row.put("userExistence", "Does Not Exist");
					transactionRuleBasedExistencean.add(row);
				}
			}
		}
		if (transactionParameter.equals("payvendorsupplier") || transactionParameter.equals("salesreturn")
				|| transactionParameter.equals("purchasereturn")) {// payvendorsupplier
			if (txn != null) {
				criterias.clear();
				criterias.put("role.name", "APPROVER");
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("user.presentStatus", 1);
				List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, entityManager);
				String approverEmails = "";
				for (UsersRoles usrRoles : approverRole) {
					criterias.clear();
					criterias.put("user.id", usrRoles.getUser().getId());
					criterias.put("userRights.id", 2L);
					criterias.put("branch.id", txn.getTransactionBranch().getId());
					criterias.put("presentStatus", 1);
					UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class,
							criterias, entityManager);
					if (userHasRightInBranch != null) {
						// check for right in chart of accounts
						criterias.clear();
						criterias.put("user.id", usrRoles.getUser().getId());
						criterias.put("userRights.id", 2L);
						criterias.put("specifics.id", txn.getTransactionSpecifics().getId());
						criterias.put("presentStatus", 1);
						UserRightSpecifics userHasRightInCOA = genericDao.getByCriteria(UserRightSpecifics.class,
								criterias, entityManager);
						if (userHasRightInCOA != null) {
							boolean userAmtLimit = false;
							if (userHasRightInCOA.getAmount() != null) {
								if (userHasRightInCOA.getAmount() > 0) {
									if (Double.parseDouble(transactionNetAmount) > userHasRightInCOA.getAmount()) {
										userAmtLimit = false;
									}
									if (Double.parseDouble(transactionNetAmount) < userHasRightInCOA.getAmount()) {
										userAmtLimit = true;
									}
								}
							}
							if (userHasRightInCOA.getAmountTo() != null)
								if (userHasRightInCOA.getAmountTo() > 0) {
									if (Double.parseDouble(transactionNetAmount) > userHasRightInCOA.getAmountTo()) {
										userAmtLimit = false;
									}
									if (Double.parseDouble(transactionNetAmount) < userHasRightInCOA.getAmountTo()) {
										userAmtLimit = true;
									}
								}
							if (userAmtLimit == true) {
								approverEmails += usrRoles.getUser().getEmail() + ",";
							}
						}
					}
				}
				if (approverEmails != null && !approverEmails.equals("")) {
					result.put("result", true);
					ObjectNode row = Json.newObject();
					row.put("userExistence", "Exist");
					transactionRuleBasedExistencean.add(row);
				} else {
					result.put("result", true);
					ObjectNode row = Json.newObject();
					row.put("userExistence", "Does Not Exist");
					transactionRuleBasedExistencean.add(row);
				}
			}
		}
		if (transactionParameter.equals("payadvancetovendorsupplier")) {// pay advance to vendor supplier
			if (specf != null) {
				criterias.clear();
				criterias.put("role.name", "APPROVER");
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("user.presentStatus", 1);
				List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, entityManager);
				String approverEmails = "";
				for (UsersRoles usrRoles : approverRole) {
					criterias.clear();
					criterias.put("user.id", usrRoles.getUser().getId());
					criterias.put("userRights.id", 2L);
					criterias.put("branch.id", usrRoles.getUser().getBranch().getId());
					criterias.put("presentStatus", 1);
					UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class,
							criterias, entityManager);
					if (userHasRightInBranch != null) {
						// check for right in chart of accounts
						criterias.clear();
						criterias.put("user.id", usrRoles.getUser().getId());
						criterias.put("userRights.id", 2L);
						criterias.put("specifics.id", specf.getId());
						criterias.put("presentStatus", 1);
						UserRightSpecifics userHasRightInCOA = genericDao.getByCriteria(UserRightSpecifics.class,
								criterias, entityManager);
						if (userHasRightInCOA != null) {
							boolean userAmtLimit = false;
							if (userHasRightInCOA.getAmount() != null) {
								if (userHasRightInCOA.getAmount() > 0) {
									if (Double.parseDouble(transactionNetAmount) > userHasRightInCOA.getAmount()) {
										userAmtLimit = false;
									}
									if (Double.parseDouble(transactionNetAmount) < userHasRightInCOA.getAmount()) {
										userAmtLimit = true;
									}
								}
							}
							if (userHasRightInCOA.getAmountTo() != null)
								if (userHasRightInCOA.getAmountTo() > 0) {
									if (Double.parseDouble(transactionNetAmount) > userHasRightInCOA.getAmountTo()) {
										userAmtLimit = false;
									}
									if (Double.parseDouble(transactionNetAmount) < userHasRightInCOA.getAmountTo()) {
										userAmtLimit = true;
									}
								}
							if (userAmtLimit == true) {
								approverEmails += usrRoles.getUser().getEmail() + ",";
							}
						}
					}
				}
				if (approverEmails != null && !approverEmails.equals("")) {
					result.put("result", true);
					ObjectNode row = Json.newObject();
					row.put("userExistence", "Exist");
					transactionRuleBasedExistencean.add(row);
				} else {
					result.put("result", true);
					ObjectNode row = Json.newObject();
					row.put("userExistence", "Does Not Exist");
					transactionRuleBasedExistencean.add(row);
				}
			}
		}
		if (transactionParameter.equals("maintopettycashtransfer")) {// main cash to petty cash transfer
			if (bnch != null) {
				criterias.clear();
				criterias.put("role.name", "APPROVER");
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("user.presentStatus", 1);
				List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, entityManager);
				String approverEmails = "";
				for (UsersRoles usrRoles : approverRole) {
					// check for right in chart of accounts
					criterias.clear();
					criterias.put("user.id", usrRoles.getUser().getId());
					criterias.put("userRights.id", 2L);
					criterias.put("branch.id", bnch.getId());
					criterias.put("presentStatus", 1);
					UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class,
							criterias, entityManager);
					if (userHasRightInBranch != null) {
						approverEmails += usrRoles.getUser().getEmail() + ",";
					}
				}
				if (approverEmails != null && !approverEmails.equals("")) {
					result.put("result", true);
					ObjectNode row = Json.newObject();
					row.put("userExistence", "Exist");
					transactionRuleBasedExistencean.add(row);
				} else {
					result.put("result", true);
					ObjectNode row = Json.newObject();
					row.put("userExistence", "Does Not Exist");
					transactionRuleBasedExistencean.add(row);
				}
			}
		}
		if (transactionParameter.equals("payspecialadjustmenttovendor")) {
			if (vendor != null) {
				if ((vendor != null) && (vendor.getBranch() != null)) {
					Branch txnBranch = vendor.getBranch();
					if (txnBranch != null) {
						// list of additional users all approver role users of thet organization
						criterias.clear();
						criterias.put("role.name", "APPROVER");
						criterias.put("organization.id", user.getOrganization().getId());
						criterias.put("user.presentStatus", 1);
						List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias,
								entityManager);
						String approverEmails = "";
						for (UsersRoles usrRoles : approverRole) {
							// check for all user which has right in headquarter branch
							criterias.clear();
							criterias.put("user.id", usrRoles.getUser().getId());
							criterias.put("userRights.id", 2L);
							criterias.put("branch.id", txnBranch.getId());
							criterias.put("presentStatus", 1);
							List<UserRightInBranch> userHasRightInBranch = genericDao
									.findByCriteria(UserRightInBranch.class, criterias, entityManager);
							for (UserRightInBranch usrRightInBnch : userHasRightInBranch) {
								approverEmails += usrRightInBnch.getUser().getEmail() + ",";
							}
						}
						if (approverEmails != null && !approverEmails.equals("")) {
							result.put("result", true);
							ObjectNode row = Json.newObject();
							row.put("userExistence", "Exist");
							transactionRuleBasedExistencean.add(row);
						} else {
							result.put("result", true);
							ObjectNode row = Json.newObject();
							row.put("userExistence", "Does Not Exist");
							transactionRuleBasedExistencean.add(row);
						}
					}
				}
			}
		}
		if (transactionParameter.equals("travelclaims")) {
			if (bnch != null && specf == null) {// in case of travel claims transaction request for travel advance and
												// settle travel advance specifics items are not present both approver
												// and accountant emails should be present
				String approverEmails = "";
				String accountantEmailsStr = "";
				criterias.clear();
				criterias.put("role.name", "APPROVER");
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("user.presentStatus", 1);
				List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, entityManager);
				for (UsersRoles usrRoles : approverRole) {
					criterias.clear();
					criterias.put("user.id", usrRoles.getUser().getId());
					criterias.put("userRights.id", 2L);
					criterias.put("branch.id", bnch.getId());
					criterias.put("presentStatus", 1);
					UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class,
							criterias, entityManager);
					if (userHasRightInBranch != null) {
						approverEmails += usrRoles.getUser().getEmail() + ",";
					}
				}
				criterias.clear();
				criterias.put("role.id", 5l);
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("user.presentStatus", 1);
				criterias.put("presentStatus", 1);
				List<UsersRoles> accountantRole = genericDao.findByCriteria(UsersRoles.class, criterias, entityManager);
				for (UsersRoles usrRoles : accountantRole) {
					accountantEmailsStr += usrRoles.getUser().getEmail() + ",";
				}
				if (approverEmails != null && !approverEmails.equals("") && accountantEmailsStr != null
						&& !accountantEmailsStr.equals("")) {
					result.put("result", true);
					ObjectNode row = Json.newObject();
					row.put("userExistence", "Exist");
					transactionRuleBasedExistencean.add(row);
				} else {
					result.put("result", true);
					ObjectNode row = Json.newObject();
					row.put("userExistence", "Does Not Exist");
					transactionRuleBasedExistencean.add(row);
				}
			}
		}
		if (transactionParameter.equals("expenseclaims") || transactionParameter.equals("expensereimbursementclaims")) {
			if (specf != null && bnch != null) {
				String approverEmails = "";
				String accountantEmailsStr = "";
				criterias.clear();
				criterias.put("role.name", "APPROVER");
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("user.presentStatus", 1);
				List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, entityManager);
				for (UsersRoles usrRoles : approverRole) {
					criterias.clear();
					criterias.put("user.id", usrRoles.getUser().getId());
					criterias.put("userRights.id", 2L);
					criterias.put("branch.id", bnch.getId());
					criterias.put("branch.presentStatus", 1);
					criterias.put("presentStatus", 1);
					UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class,
							criterias, entityManager);
					if (userHasRightInBranch != null) {
						criterias.clear();
						criterias.put("user.id", usrRoles.getUser().getId());
						criterias.put("userRights.id", 2L);
						criterias.put("specifics.id", specf.getId());
						criterias.put("presentStatus", 1);
						UserRightSpecifics userHasRightInCOA = genericDao.getByCriteria(UserRightSpecifics.class,
								criterias, entityManager);
						if (userHasRightInCOA != null) {
							approverEmails += usrRoles.getUser().getEmail() + ",";
						}
					}
				}
				criterias.clear();
				criterias.put("role.id", 5l);
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("user.presentStatus", 1);
				List<UsersRoles> accountantRole = genericDao.findByCriteria(UsersRoles.class, criterias, entityManager);
				for (UsersRoles usrRoles : accountantRole) {
					accountantEmailsStr += usrRoles.getUser().getEmail() + ",";
				}
				if (approverEmails != null && !approverEmails.equals("") && accountantEmailsStr != null
						&& !accountantEmailsStr.equals("")) {
					result.put("result", true);
					ObjectNode row = Json.newObject();
					row.put("userExistence", "Exist");
					transactionRuleBasedExistencean.add(row);
				} else {
					result.put("result", true);
					ObjectNode row = Json.newObject();
					row.put("userExistence", "Does Not Exist");
					transactionRuleBasedExistencean.add(row);
				}
			}
		}
		if (transactionParameter.equals("travelclaimssettlemet")
				|| transactionParameter.equals("expenseclaimssettlement")) {
			if (claimTxn != null) {
				String accountantEmailsStr = "";
				criterias.clear();
				criterias.put("role.id", 5l);
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("user.presentStatus", 1);
				List<UsersRoles> accountantRole = genericDao.findByCriteria(UsersRoles.class, criterias, entityManager);
				for (UsersRoles usrRoles : accountantRole) {
					accountantEmailsStr += usrRoles.getUser().getEmail() + ",";
				}
				if (accountantEmailsStr != null && !accountantEmailsStr.equals("")) {
					result.put("result", true);
					ObjectNode row = Json.newObject();
					row.put("userExistence", "Exist");
					transactionRuleBasedExistencean.add(row);
				} else {
					result.put("result", true);
					ObjectNode row = Json.newObject();
					row.put("userExistence", "Does Not Exist");
					transactionRuleBasedExistencean.add(row);
				}
			}
		}
		return result;
	}
}
