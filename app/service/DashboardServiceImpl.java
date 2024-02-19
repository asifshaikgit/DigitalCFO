package service;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;

import model.Branch;
import model.BranchBankAccountBalance;
import model.BranchBankAccounts;
import model.BranchCashCount;
import model.BranchDepositBoxKey;
import model.BranchInsurance;
import model.BranchSpecifics;
import model.BranchTaxes;
import model.BranchVendors;
import model.ClaimTransaction;
import model.DistanceMilesKm;
import model.ExpenseGroup;
import model.ExpenseGroupExpenseItemMonetoryClaim;
import model.IDOSCountry;
import model.Organization;
import model.OrganizationKeyOfficials;
import model.OrganizationOperationalRemainders;
import model.Project;
import model.ProjectBranches;
import model.ProjectLabourPosition;
import model.Specifics;
import model.SpecificsKnowledgeLibrary;
import model.SpecificsKnowledgeLibraryForBranch;
import model.SpecificsTransactionPurpose;
import model.StatutoryDetails;
import model.Transaction;
import model.TransactionPurpose;
import model.TravelGroupDistanceMilesKmsAllowedTravelMode;
import model.TravelGroupFixedDailyPerDIAM;
import model.TravelGroupKnowledgeLibrary;
import model.TravelGroupMaxDailyLimitOtherOfficialPurposeExpenses;
import model.TravelGroupPermittedBoardingLodging;
import model.Travel_Group;
import model.TrialBalanceCOAItems;
import model.UserRightForProject;
import model.UserRightInBranch;
import model.UserRightSpecifics;
import model.UserRights;
import model.UserTransactionPurpose;
import model.Users;
import model.UsersRoles;
import model.Vendor;
import model.VendorSpecific;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.libs.Json;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import java.util.logging.Level;
import com.idos.util.CountryCurrencyUtil;
import com.idos.util.DateUtil;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
//import com.idos.util.MySqlConnection;
import java.sql.Statement;

import controllers.StaticController;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DashboardServiceImpl implements DashboardService {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public ObjectNode getBranchesOrProjectsOrOperationalData(final Organization org, final int type, EntityManager entityManager) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		ObjectNode row = null;
		ArrayNode an = result.putArray("result");
		if (null != org) {
			if (2 == type) {
				List<Branch> branches = org.getBranches();
				if (!branches.isEmpty()) {
					for (Branch branch : branches) {
						if (null != branch) {
							row = Json.newObject();
							if (null != branch.getId()) {
								row.put("itemId", branch.getId());
								if (null != branch.getName()) {
									row.put("itemName", branch.getName());
								} else {
									row.put("itemName", "");
								}
							} else {
								row.put("itemId", "");
							}
							an.add(row);
						}
					}
				}
			} else if (3 == type) {
				List<Specifics> specifics = org.getSpecifics();
				if (!specifics.isEmpty()) {
					for (Specifics specific : specifics) {
						if (null != specific) {
							row = Json.newObject();
							if (null != specific.getId()) {
								row.put("itemId", specific.getId());
								if (null != specific.getName()) {
									row.put("itemName", specific.getName());
								} else {
									row.put("itemName", "");
								}
							} else {
								row.put("itemId", "");
							}
							an.add(row);
						}
					}
				}
			} else if (4 == type) {
				List<Project> projects = org.getProjects();
				if (!projects.isEmpty()) {
					for (Project project : projects) {
						if (null != project) {
							row = Json.newObject();
							if (null != project.getId()) {
								row.put("itemId", project.getId());
								if (null != project.getName()) {
									row.put("itemName", project.getName());
								} else {
									row.put("itemName", "");
								}
							} else {
								row.put("itemId", "");
							}
							an.add(row);
						}
					}
				}
			} else if (7 == type) {
				StringBuilder query = new StringBuilder();
				query.append("SELECT obj FROM Users obj WHERE obj.organization.id  =  ").append(org.getId());
				List<Users> users = genericDAO.executeSimpleQuery(query.toString(), entityManager);
				if (!users.isEmpty() && users.size() > 0) {
					for (Users user : users) {
						if (null != user) {
							row = Json.newObject();
							if (null != user.getId()) {
								row.put("itemId", user.getId());
								if (null != user.getFullName()) {
									if (null != user.getEmail()) {
										row.put("itemName", user.getFullName() + " (" + user.getEmail() + ")");
									} else {
										row.put("itemName", user.getFullName());
									}
								} else {
									row.put("itemName", "");
								}
							} else {
								row.put("itemId", "");
							}
							an.add(row);
						}
					}
				}
			} else if (8 == type) {
				List<Travel_Group> groups = org.getOrganizationTravelGroups();
				if (!groups.isEmpty()) {
					for (Travel_Group group : groups) {
						if (null != groups) {
							row = Json.newObject();
							if (null != group.getId()) {
								row.put("itemId", group.getId());
								if (null != group.getTravelGroupName()) {
									row.put("itemName", group.getTravelGroupName());
								} else {
									row.put("itemName", "");
								}
							} else {
								row.put("itemId", "");
							}
							an.add(row);
						}
					}
				}
			} else if (9 == type) {
				List<ExpenseGroup> groups = org.getOrganizationExpenseGroups();
				if (!groups.isEmpty()) {
					for (ExpenseGroup group : groups) {
						if (null != groups) {
							row = Json.newObject();
							if (null != group.getId()) {
								row.put("itemId", group.getId());
								if (null != group.getExpenseGroupName()) {
									row.put("itemName", group.getExpenseGroupName());
								} else {
									row.put("itemName", "");
								}
							} else {
								row.put("itemId", "");
							}
							an.add(row);
						}
					}
				}
			}
		}
		return result;
	}

	public ObjectNode getVendorsOrCustomers(final Organization org, final int type, EntityManager entityManager) {
		log.log(Level.FINE, "============ Start");
		String query = "SELECT obj FROM Vendor obj WHERE obj.type  =  " + type + " AND obj.organization.id  =  '"
				+ org.getId() + "'";
		List<Vendor> vendors = genericDAO.executeSimpleQuery(query, entityManager);
		/*
		 * Map<String, Object> criterias = new HashMap<String, Object>();
		 * criterias.put("type", type);
		 * criterias.put("organization.id", org.getId());
		 * List<Vendor> vendors = genericDAO.findByCriteria(Vendor.class, criterias,
		 * entityManager);
		 */
		Collections.reverse(vendors);
		ObjectNode result = Json.newObject();
		ObjectNode row = null;
		ArrayNode an = result.putArray("result");
		if (!vendors.isEmpty()) {
			for (Vendor vendor : vendors) {
				row = Json.newObject();
				row.put("itemId", vendor.getId());
				row.put("itemName", vendor.getName());
				an.add(row);
			}
		}
		return result;
	}

	@Override
	public ObjectNode getOrganizationDetails(final Organization org) throws Exception {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = null;
		try {
			if (null != org) {
				result = Json.newObject();
				ObjectNode row = Json.newObject();
				ArrayNode an = result.putArray("organization");
				ArrayNode subArray = null;
				if (null != org.getName()) {
					row.put("name", org.getName());
				} else {
					row.put("name", "");
				}
				if (null != org.getCorporateMail()) {
					row.put("email", org.getCorporateMail());
				} else {
					row.put("email", "");
				}
				if (null != org.getWebUrl()) {
					row.put("webUrl", org.getWebUrl());
				} else {
					row.put("webUrl", "");
				}
				if (null != org.getRegisteredAddress()) {
					row.put("address", org.getRegisteredAddress());
				} else {
					row.put("address", "");
				}
				if (null != org.getRegisteredPhoneNumber()) {
					row.put("phone", org.getRegisteredPhoneNumber());
				} else {
					row.put("phone", "");
				}
				if (null != org.getRegPhNoCtryCode()) {
					row.put("phoneNumberCountryCode", org.getRegPhNoCtryCode());
				} else {
					row.put("phoneNumberCountryCode", "");
				}
				if (null != org.getCountry()) {
					Map<String, String> countries = CountryCurrencyUtil.getCountries();
					if (countries.containsKey(org.getCountry().toString())) {
						row.put("country", countries.get(org.getCountry().toString()));
					} else {
						row.put("country", "");
					}
				} else {
					row.put("country", "");
				}
				if (null != org.getCurrency()) {
					IDOSCountry country = IDOSCountry.findById((org.getCountry().longValue()));
					log.log(Level.INFO, "currency" + country.getCurrencyCode());
					if (country != null) {
						row.put("currency", country.getCurrencyCode());
					} else {
						row.put("currency", "");
					}

				} else {
					row.put("currency", "");
				}
				if (null != org.getFinancialStartDate()) {
					row.put("financialStart", StaticController.idosmdtdf.format(org.getFinancialStartDate()));
				} else {
					row.put("financialStart", "");
				}
				if (null != org.getFinancialEndDate()) {
					row.put("financialEnd", StaticController.idosmdtdf.format(org.getFinancialEndDate()));
				} else {
					row.put("financialEnd", "");
				}

				subArray = row.putArray("documents");
				if (null != org.getAccountingManualDoc() && (!("").equals(org.getAccountingManualDoc()))) {
					subArray.add(org.getAccountingManualDoc());
				}
				if (null != org.getAuditedAccountDoc() && (!("").equals(org.getAuditedAccountDoc()))) {
					subArray.add(org.getAuditedAccountDoc());
				}
				if (null != org.getCompanyTemplateDoc() && (!("").equals(org.getCompanyTemplateDoc()))) {
					subArray.add(org.getCompanyTemplateDoc());
				}
				if (null != org.getOrganizationChartDoc() && (!("").equals(org.getOrganizationChartDoc()))) {
					subArray.add(org.getOrganizationChartDoc());
				}
				if (null != org.getSignatoryListDoc() && (!("").equals(org.getSignatoryListDoc()))) {
					subArray.add(org.getSignatoryListDoc());
				}
				if (null != org.getTaxReturnDoc() && (!("").equals(org.getTaxReturnDoc()))) {
					subArray.add(org.getTaxReturnDoc());
				}
				an.add(row);
			}
		} catch (Exception e) {
			throw e;
		}
		return result;
	}

	@Override
	public ObjectNode getBranchDetails(final long branchId, final int subType, EntityManager entityManager)
			throws Exception {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = null;
		try {
			Branch branch = Branch.findById(branchId);
			if (null != branch) {
				result = Json.newObject();
				ObjectNode row = null;
				result.put("branchname", branch.getName());
				if (1 == subType) {
					List<OrganizationKeyOfficials> officials = branch.getBranchOfficers();
					Map<String, String> countries = CountryCurrencyUtil.getCountries();
					if (!officials.isEmpty()) {
						ArrayNode an = result.putArray("officials");
						for (OrganizationKeyOfficials official : officials) {
							if (null != official) {
								row = Json.newObject();
								if (null != official.getName()) {
									row.put("officialName", official.getName());
								} else {
									row.put("officialName", "");
								}
								if (null != official.getDesignation()) {
									row.put("designation", official.getDesignation());
								} else {
									row.put("designation", "");
								}
								if (null != official.getCountry()) {
									if (countries.containsKey(official.getCountry().toString())) {
										row.put("country", countries.get(official.getCountry().toString()));
									} else {
										row.put("country", "");
									}
								} else {
									row.put("country", "");
								}
								if (null != official.getCity()) {
									row.put("city", official.getCity());
								} else {
									row.put("city", "");
								}
								if (null != official.getEmail()) {
									row.put("email", official.getEmail());
								} else {
									row.put("email", "");
								}
								if (null != official.getCtryPhCode()) {
									row.put("officialPhoneCountryCode", official.getCtryPhCode());
								} else {
									row.put("officialPhoneCountryCode", "");
								}
								if (null != official.getPhoneNumber()) {
									row.put("officialPhoneNumber", official.getPhoneNumber());
								} else {
									row.put("officialPhoneNumber", "");
								}
								if (null != official.getPersonalPhoneCountryCode()) {
									row.put("personalPhoneCountryCode", official.getPersonalPhoneCountryCode());
								} else {
									row.put("personalPhoneCountryCode", "");
								}
								if (null != official.getPersonalPhoneNumber()) {
									row.put("personalPhoneNumber", official.getPersonalPhoneNumber());
								} else {
									row.put("personalPhoneNumber", "");
								}
								if (null != official.getUploadedId()) {
									row.put("uploadId", official.getUploadedId());
								} else {
									row.put("uploadId", "");
								}
								an.add(row);
							}
						}
					}
				} else if (2 == subType) {
					List<StatutoryDetails> details = branch.getBranchStatutoryDetails();
					if (!details.isEmpty()) {
						ArrayNode an = result.putArray("statutoryDetails");
						for (StatutoryDetails detail : details) {
							if (null != detail) {
								row = Json.newObject();
								if (null != detail.getStatutoryDetails()) {
									row.put("statDetails", detail.getStatutoryDetails());
								} else {
									row.put("statDetails", "");
								}
								if (null != detail.getRegistrationNumber()) {
									row.put("regNumber", detail.getRegistrationNumber());
								} else {
									row.put("regNumber", "");
								}
								if (null != detail.getPresentStatus()) {
									row.put("inInvoice", detail.getPresentStatus());
								} else {
									row.put("inInvoice", "");
								}
								if (null != detail.getRegistrationDoc()) {
									row.put("regDocument", detail.getRegistrationDoc());
								} else {
									row.put("regDocument", "");
								}
								if (null != detail.getValidFrom()) {
									row.put("validFrom", StaticController.idosdf.format(detail.getValidFrom()));
								} else {
									row.put("validFrom", "");
								}
								if (null != detail.getValidTo()) {
									row.put("validTo", StaticController.idosdf.format(detail.getValidTo()));
								} else {
									row.put("validTo", "");
								}
								if (null != detail.getAlertForAction()) {
									row.put("alertForAction", detail.getAlertForAction());
								} else {
									row.put("alertForAction", "");
								}
								if (null != detail.getAlertForInformation()) {
									row.put("alertForInfo", detail.getAlertForInformation());
								} else {
									row.put("alertForInfo", "");
								}
								if (null != detail.getNameAddressOfConsultant()) {
									row.put("nameAddress", detail.getNameAddressOfConsultant());
								} else {
									row.put("nameAddress", "");
								}
								if (null != detail.getRemarks()) {
									row.put("remarks", detail.getRemarks());
								} else {
									row.put("remarks", "");
								}
								an.add(row);
							}
						}
					}
				} else if (3 == subType) {
					List<OrganizationOperationalRemainders> reminders = branch.getBranchOperationAlerts();
					if (!reminders.isEmpty()) {
						ArrayNode an = result.putArray("reminders");
						for (OrganizationOperationalRemainders reminder : reminders) {
							if (null != reminder) {
								row = Json.newObject();
								if (null != reminder.getRequiements()) {
									row.put("requirements", reminder.getRequiements());
								} else {
									row.put("requirements", "");
								}
								if (null != reminder.getDueOn()) {
									row.put("dueOn", StaticController.idosdf.format(reminder.getDueOn()));
								} else {
									row.put("requirements", "");
								}
								if (null != reminder.getRecurrences()) {
									row.put("recurrenceNumber", reminder.getRecurrences());
									if (1 == reminder.getRecurrences()) {
										row.put("recurrence", "Weekly");
									} else if (2 == reminder.getRecurrences()) {
										row.put("recurrence", "Monthly");
									} else if (3 == reminder.getRecurrences()) {
										row.put("recurrence", "Quarterly");
									} else if (4 == reminder.getRecurrences()) {
										row.put("recurrence", "Half Yearly");
									} else if (5 == reminder.getRecurrences()) {
										row.put("recurrence", "Annually");
									} else if (6 == reminder.getRecurrences()) {
										row.put("recurrence", "Once in 2 years");
									} else if (7 == reminder.getRecurrences()) {
										row.put("recurrence", "Once in 3 years");
									} else if (8 == reminder.getRecurrences()) {
										row.put("recurrence", "One Time");
									} else {
										row.put("recurrence", "");
									}
								} else {
									row.put("recurrence", "");
									row.put("recurrenceNumber", "");
								}
								if (null != reminder.getAlertForAction()) {
									row.put("alertForAction", reminder.getAlertForAction());
								} else {
									row.put("alertForAction", "");
								}
								if (null != reminder.getAlertForInformation()) {
									row.put("alertForInfo", reminder.getAlertForInformation());
								} else {
									row.put("alertForInfo", "");
								}
								if (null != reminder.getRemarks()) {
									row.put("remarks", reminder.getRemarks());
								} else {
									row.put("remarks", "");
								}
								an.add(row);
							}
						}
					}
				} else if (4 == subType) {
					List<BranchDepositBoxKey> depositBoxKeys = branch.getBranchDepositKeys();
					if (!depositBoxKeys.isEmpty()) {
						ArrayNode an = result.putArray("depositBoxKeys");
						for (BranchDepositBoxKey boxKey : depositBoxKeys) {
							if (null != boxKey) {
								row = Json.newObject();
								if (null != boxKey.getName()) {
									row.put("custodianName", boxKey.getName());
								} else {
									row.put("custodianName", "");
								}
								if (null != boxKey.getPhoneNumber()) {
									row.put("custodianPhone", boxKey.getPhoneNumber());
								} else {
									row.put("custodianPhone", "");
								}
								if (null != boxKey.getEmail()) {
									row.put("custodianEmail", boxKey.getEmail());
								} else {
									row.put("custodianEmail", "");
								}
								if (null != boxKey.getCashierName()) {
									row.put("cashierName", boxKey.getCashierName());
								} else {
									row.put("cashierName", "");
								}
								if (null != boxKey.getCashierPhnNo()) {
									row.put("cashierPhone", boxKey.getCashierPhnNo());
								} else {
									row.put("cashierPhone", "");
								}
								if (null != boxKey.getCashierEmail()) {
									row.put("cashierEmail", boxKey.getCashierEmail());
								} else {
									row.put("cashierEmail", "");
								}
								if (null != boxKey.getCashierEmail() && null != boxKey.getCashierName()
										&& null != boxKey.getCashierPhnNo()) {
									if (null != boxKey.getCashierKnowledgeLibrary()) {
										row.put("cashierKnowledge", boxKey.getCashierKnowledgeLibrary());
									} else {
										row.put("cashierKnowledge", "");
									}
									if (null != boxKey.getPettyCashTxnApprovalRequired()) {
										if (1 == boxKey.getPettyCashTxnApprovalRequired()) {
											row.put("cashTransApprove", "No");
										} else if (0 == boxKey.getPettyCashTxnApprovalRequired()) {
											row.put("cashTransApprove", "Yes");
										} else {
											row.put("cashTransApprove", "");
										}
									} else {
										row.put("cashTransApprove", "");
									}
									if (null != branch.getId()) {
										StringBuilder newsbquery = new StringBuilder("");
										newsbquery.append("select obj from BranchCashCount obj WHERE obj.branch.id = '"
												+ branch.getId() + "' AND obj.organization.id = '"
												+ branch.getOrganization().getId()
												+ "' and obj.presentStatus=1 ORDER BY obj.date desc");
										List<BranchCashCount> cashCounts = genericDAO
												.executeSimpleQueryWithLimit(newsbquery.toString(), entityManager, 1);
										if (!cashCounts.isEmpty()) {
											BranchCashCount cashCount = cashCounts.get(0);
											if (null != cashCount) {
												if (null != cashCount.getGrandTotal()) {
													row.put("cashLastRecord", IdosConstants.decimalFormat
															.format(cashCount.getGrandTotal()));
												} else {
													row.put("cashLastRecord", "");
												}
												if (null != cashCount.getCreditAmount()) {
													row.put("cashCredit", IdosConstants.decimalFormat
															.format(cashCount.getCreditAmount()));
												} else {
													row.put("cashCredit", "");
												}
												if (null != cashCount.getDebitAmount()) {
													row.put("cashDebit", IdosConstants.decimalFormat
															.format(cashCount.getDebitAmount()));
												} else {
													row.put("cashDebit", "");
												}
												if (null != cashCount.getResultantCash()) {
													row.put("cashResult", IdosConstants.decimalFormat
															.format(cashCount.getResultantCash()));
												} else {
													row.put("cashResult", "");
												}
												if (null != cashCount.getDebittedPettyCashAmount()) {
													row.put("cashPettyDebit", IdosConstants.decimalFormat
															.format(cashCount.getDebittedPettyCashAmount()));
												} else {
													row.put("cashPettyDebit", "");
												}
												if (null != cashCount.getTotalMainCashToPettyCash()) {
													row.put("cashTotalToPetty", IdosConstants.decimalFormat
															.format(cashCount.getTotalMainCashToPettyCash()));
												} else {
													row.put("cashTotalToPetty", "");
												}
												if (null != cashCount.getResultantPettyCash()) {
													row.put("cashPettyResult", IdosConstants.decimalFormat
															.format(cashCount.getResultantPettyCash()));
												} else {
													row.put("cashPettyResult", "");
												}
											}
										} else {
											row.put("cashLastRecord", "");
											row.put("cashCredit", "");
											row.put("cashDebit", "");
											row.put("cashResult", "");
											row.put("cashPettyDebit", "");
											row.put("cashTotalToPetty", "");
											row.put("cashPettyResult", "");
										}
									}
								} else {
									row.put("cashLastRecord", "");
									row.put("cashCredit", "");
									row.put("cashDebit", "");
									row.put("cashResult", "");
									row.put("cashPettyDebit", "");
									row.put("cashTotalToPetty", "");
									row.put("cashPettyResult", "");
									row.put("cashierKnowledge", "");
									row.put("cashTransApprove", "");
								}
								an.add(row);
							}
						}
					}
				} else if (5 == subType) {
					List<BranchInsurance> insurances = branch.getBranchInsurance();
					if (!insurances.isEmpty()) {
						ArrayNode an = result.putArray("insurances");
						for (BranchInsurance insurance : insurances) {
							if (null != insurance) {
								row = Json.newObject();
								if (null != insurance.getPolicyType()) {
									row.put("policyType", insurance.getPolicyType());
								} else {
									row.put("policyType", "");
								}
								if (null != insurance.getPolicyNumber()) {
									row.put("policyNumber", insurance.getPolicyNumber());
								} else {
									row.put("policyNumber", "");
								}
								if (null != insurance.getInsurenceCompany()) {
									row.put("insuranceCompany", insurance.getInsurenceCompany());
								} else {
									row.put("insuranceCompany", "");
								}
								if (null != insurance.getInsurancePolicyDocUrl()) {
									row.put("insurancePolicyDoc", insurance.getInsurancePolicyDocUrl());
								} else {
									row.put("insurancePolicyDoc", "");
								}
								if (null != insurance.getPolicyValidFrom()) {
									row.put("validFrom",
											StaticController.idosdf.format(insurance.getPolicyValidFrom()));
								} else {
									row.put("validFrom", "");
								}
								if (null != insurance.getPolicyValidTo()) {
									row.put("validTo", StaticController.idosdf.format(insurance.getPolicyValidTo()));
								} else {
									row.put("validTo", "");
								}
								if (null != insurance.getAnnualPremium()) {
									row.put("annualPremium", insurance.getAnnualPremium());
								} else {
									row.put("annualPremium", "");
								}
								if (null != insurance.getAlertOfAction()) {
									row.put("alertForAction", insurance.getAlertOfAction());
								} else {
									row.put("alertForAction", "");
								}
								if (null != insurance.getAlertOfInformation()) {
									row.put("alertForInfo", insurance.getAlertOfInformation());
								} else {
									row.put("alertForInfo", "");
								}
								if (null != insurance.getRemarks()) {
									row.put("remarks", insurance.getRemarks());
								} else {
									row.put("remarks", "");
								}
								an.add(row);
							}
						}
					}
				} else if (6 == subType) {
					List<BranchBankAccounts> accounts = branch.getBranchBankAccounts();
					if (!accounts.isEmpty()) {
						ArrayNode an = result.putArray("bankAccounts");
						for (BranchBankAccounts account : accounts) {
							if (null != account) {
								row = Json.newObject();
								if (null != account.getBankName()) {
									row.put("bankName", account.getBankName());
								} else {
									row.put("bankName", "");
								}
								if (null != account.getAccountNumber()) {
									row.put("accountNumber", account.getAccountNumber());
								} else {
									row.put("accountNumber", "");
								}
								if (null != account.getAccountType()) {
									row.put("accountTypeNum", account.getAccountType());
									if (1 == account.getAccountType()) {
										row.put("accountType", "CASH_CREDIT");
									} else if (2 == account.getAccountType()) {
										row.put("accountType", "CHECKING");
									} else if (3 == account.getAccountType()) {
										row.put("accountType", "CURRENT");
									} else if (4 == account.getAccountType()) {
										row.put("accountType", "DEPOSIT");
									} else if (5 == account.getAccountType()) {
										row.put("accountType", "LINE_OF_CREDIT");
									} else if (6 == account.getAccountType()) {
										row.put("accountType", "LOAN");
									} else if (7 == account.getAccountType()) {
										row.put("accountType", "OVER_DRAFT");
									} else if (8 == account.getAccountType()) {
										row.put("accountType", "SAVINGS");
									} else {
										row.put("accountType", "");
									}
								} else {
									row.put("accountType", "");
									row.put("accountTypeNum", "");
								}
								if (null != account.getRoutingNumber()) {
									row.put("routingNumber", account.getRoutingNumber());
								} else {
									row.put("routingNumber", "");
								}
								if (null != account.getSwiftCode()) {
									row.put("swiftCode", account.getSwiftCode());
								} else {
									row.put("swiftCode", "");
								}
								if (null != account.getPhoneNumber()) {
									row.put("phoneNumber", account.getPhoneNumber());
								} else {
									row.put("phoneNumber", "");
								}
								if (null != account.getBankAddress()) {
									row.put("bankAddress", account.getBankAddress());
								} else {
									row.put("bankAddress", "");
								}
								if (null != account.getCheckBookCustodtName()) {
									row.put("custodianName", account.getCheckBookCustodtName());
								} else {
									row.put("custodianName", "");
								}
								if (null != account.getCheckBookCustodyEmail()) {
									row.put("custodianEmail", account.getCheckBookCustodyEmail());
								} else {
									row.put("custodianEmail", "");
								}
								if (null != account.getAuthorizedSignatoryName()) {
									row.put("authorizedName", account.getAuthorizedSignatoryName());
								} else {
									row.put("authorizedName", "");
								}
								if (null != account.getAuthorizedSignatoryEmail()) {
									row.put("authorizedEmail", account.getAuthorizedSignatoryEmail());
								} else {
									row.put("authorizedEmail", "");
								}
								StringBuilder newsbquery = new StringBuilder("");
								newsbquery.append(
										"select obj from BranchBankAccountBalance obj WHERE obj.branchBankAccounts.id =  '"
												+ account.getId() + "' AND obj.branch.id  =  '"
												+ account.getBranch().getId() + "' AND obj.organization.id  =  '"
												+ account.getOrganization().getId()
												+ "' and obj.presentStatus=1 ORDER BY obj.date desc");
								List<BranchBankAccountBalance> accountBalances = genericDAO
										.executeSimpleQueryWithLimit(newsbquery.toString(), entityManager, 1);
								if (!accountBalances.isEmpty()) {
									BranchBankAccountBalance accountBalance = accountBalances.get(0);
									if (null != accountBalance) {
										if (null != accountBalance.getAmountBalance()) {
											row.put("amountBalance", IdosConstants.decimalFormat
													.format(accountBalance.getAmountBalance()));
										} else {
											row.put("amountBalance", "");
										}
										if (null != accountBalance.getCreditAmount()) {
											row.put("creditAmount", IdosConstants.decimalFormat
													.format(accountBalance.getCreditAmount()));
										} else {
											row.put("creditAmount", "");
										}
										if (null != accountBalance.getDebitAmount()) {
											row.put("debitAmount", IdosConstants.decimalFormat
													.format(accountBalance.getDebitAmount()));
										} else {
											row.put("debitAmount", "");
										}
										if (null != accountBalance.getResultantCash()) {
											row.put("resultantAmount", IdosConstants.decimalFormat
													.format(accountBalance.getResultantCash()));
										} else {
											row.put("resultantAmount", "");
										}
										if (null != accountBalance.getBalanceStatement()) {
											row.put("balanceStatement", IdosConstants.decimalFormat
													.format(accountBalance.getBalanceStatement()));
										} else {
											row.put("balanceStatement", "");
										}
									}
								} else {
									row.put("amountBalance", "");
									row.put("creditAmount", "");
									row.put("debitAmount", "");
									row.put("resultantAmount", "");
								}
								an.add(row);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return result;
	}

	@Override
	public ObjectNode getVendorOrCustomerDetails(final long vendorId) throws Exception {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		try {
			Vendor vendor = Vendor.findById(vendorId);
			ObjectNode row = null;
			if (null != vendor) {
				ArrayNode an = result.putArray("result");
				ArrayNode subArray = null;
				row = Json.newObject();
				Map<String, String> countries = CountryCurrencyUtil.getCountries();
				if (null != vendor.getName()) {
					row.put("vendorName", vendor.getName());
				} else {
					row.put("vendorName", "");
				}
				if (null != vendor.getType()) {
					row.put("vendorType", vendor.getName());
				} else {
					row.put("vendorType", "");
				}
				if (null != vendor.getEmail()) {
					row.put("vendorEmail", vendor.getType());
				} else {
					row.put("vendorEmail", "");
				}
				if (null != vendor.getAddress()) {
					row.put("vendorAddress", vendor.getAddress());
				} else {
					row.put("vendorAddress", "");
				}
				if (null != vendor.getCountry()) {
					if (countries.containsKey(vendor.getCountry().toString())) {
						row.put("vendorCountry", countries.get(vendor.getCountry().toString()));
					} else {
						row.put("vendorCountry", "");
					}
				} else {
					row.put("vendorCountry", "");
				}
				if (null != vendor.getLocation()) {
					row.put("vendorLocation", vendor.getLocation());
				} else {
					row.put("vendorLocation", "");
				}
				if (null != vendor.getPhoneCtryCode()) {
					row.put("vendorPhoneCode", vendor.getPhoneCtryCode());
				} else {
					row.put("vendorPhoneCode", "");
				}
				if (null != vendor.getPhone()) {
					row.put("vendorPhone", vendor.getPhone());
				} else {
					row.put("vendorPhone", "");
				}
				if (null != vendor.getVendorGroup()) {
					row.put("vendorGroup", vendor.getVendorGroup().getGroupName());
				} else {
					row.put("vendorGroup", "-");
				}
				List<BranchVendors> branchVendors = vendor.getVendorBranches();
				if (!branchVendors.isEmpty()) {
					subArray = row.putArray("vendorBranches");
					for (BranchVendors branchVendor : branchVendors) {
						if (null != branchVendor && null != branchVendor.getBranch()) {
							if (null != branchVendor.getBranch().getName()) {
								subArray.add(branchVendor.getBranch().getName());
							}
						}
					}
				}
				if (null != vendor.getAdjustmentsAllowed()) {
					if (0 == vendor.getAdjustmentsAllowed()) {
						row.put("vendorAdjustAllowed", "No");
					} else if (1 == vendor.getAdjustmentsAllowed()) {
						row.put("vendorAdjustAllowed", "Yes");
						if (null != vendor.getAdjustmentsName()) {
							row.put("vendorAdjustName", vendor.getAdjustmentsName());
						} else {
							row.put("vendorAdjustName", "");
						}
					} else {
						row.put("vendorAdjustAllowed", "");
					}
				} else {
					row.put("vendorAdjustAllowed", "");
				}
				if (null != vendor.getStatutoryName1()) {
					row.put("statName1", vendor.getStatutoryName1());
				} else {
					row.put("statName1", "");
				}
				if (null != vendor.getStatutoryName2()) {
					row.put("statName2", vendor.getStatutoryName2());
				} else {
					row.put("statName2", "");
				}
				if (null != vendor.getStatutoryName3()) {
					row.put("statName3", vendor.getStatutoryName3());
				} else {
					row.put("statName3", "");
				}
				if (null != vendor.getStatutoryName4()) {
					row.put("statName4", vendor.getStatutoryName4());
				} else {
					row.put("statName4", "");
				}
				if (null != vendor.getStatutoryNumber1()) {
					row.put("statNumber1", vendor.getStatutoryNumber1());
				} else {
					row.put("statNumber1", "");
				}
				if (null != vendor.getStatutoryNumber2()) {
					row.put("statNumber2", vendor.getStatutoryNumber2());
				} else {
					row.put("statNumber2", "");
				}
				if (null != vendor.getStatutoryNumber3()) {
					row.put("statNumber3", vendor.getStatutoryNumber3());
				} else {
					row.put("statNumber3", "");
				}
				if (null != vendor.getStatutoryNumber4()) {
					row.put("statNumber4", vendor.getStatutoryNumber4());
				} else {
					row.put("statNumber4", "");
				}
				if (null != vendor.getPurchaseType()) {
					row.put("purchaseTypeNumber", vendor.getPurchaseType());
					if (1 == vendor.getPurchaseType()) {
						row.put("purchaseType", "Cash");
					} else if (0 == vendor.getPurchaseType()) {
						row.put("purchaseType", "Credit");
					} else if (2 == vendor.getPurchaseType()) {
						row.put("purchaseType", "Both");
					} else {
						row.put("purchaseType", "");
					}
					if (0 == vendor.getPurchaseType() || 2 == vendor.getPurchaseType()) {
						if (null != vendor.getDaysForCredit()) {
							row.put("daysForCredit", vendor.getDaysForCredit());
						} else {
							row.put("daysForCredit", "0.0");
						}
					}
				} else {
					row.put("purchaseType", "");
				}
				if (null != vendor.getContractPoDoc()) {
					row.put("contractPO", vendor.getContractPoDoc());
				} else {
					row.put("contractPO", "");
				}
				if (null != vendor.getPriceListDoc()) {
					row.put("priceList", vendor.getPriceListDoc());
				} else {
					row.put("priceList", "");
				}
				if (null != vendor.getValidityFrom()) {
					row.put("validFrom", StaticController.idosdf.format(vendor.getValidityFrom()));
				} else {
					row.put("validFrom", "");
				}
				if (null != vendor.getValidityTo()) {
					row.put("validTo", StaticController.idosdf.format(vendor.getValidityTo()));
				} else {
					row.put("validTo", "");
				}
				if (null != vendor.getCustomerRemarks()) {
					row.put("remarks", vendor.getCustomerRemarks());
				} else {
					row.put("remarks", "");
				}
				List<VendorSpecific> vendorSpecifics = vendor.getVendorsSpecifics();
				StringBuilder unitPriceItem;
				if (!vendorSpecifics.isEmpty()) {
					subArray = row.putArray("vendorSpecifics");
					for (VendorSpecific vendorSpecific : vendorSpecifics) {
						if (null != vendorSpecific) {
							unitPriceItem = new StringBuilder();
							if (null != vendorSpecific.getSpecificsVendors()
									&& null != vendorSpecific.getSpecificsVendors().getName()) {
								unitPriceItem.append(vendorSpecific.getSpecificsVendors().getName());
							} else {
								unitPriceItem.append("");
							}
							unitPriceItem.append(" - ");
							if (null != vendor.getType()) {
								if (2 == vendor.getType()) {
									if (null != vendorSpecific.getDiscountPercentage()) {
										unitPriceItem.append(vendorSpecific.getDiscountPercentage());
									} else {
										unitPriceItem.append("");
									}
									unitPriceItem.append(" - ");
									if (null != vendor.getVendorSpecificsUnitPrice()) {
										unitPriceItem.append(new Double((IdosConstants.decimalFormat
												.format((vendor.getVendorSpecificsUnitPrice()))).toString()));
									} else {
										unitPriceItem.append("");
									}
								} else if (1 == vendor.getType()) {
									if (null != vendorSpecific.getUnitPrice()) {
										unitPriceItem.append(
												IdosConstants.decimalFormat.format(vendorSpecific.getUnitPrice()));
									} else {
										unitPriceItem.append("");
									}
								}
							}
							subArray.add(unitPriceItem.toString());
						}
					}
				}
				an.add(row);
			}
		} catch (Exception e) {
			throw e;
		}
		return result;
	}

	@Override
	public ObjectNode getChartOfAccount(final long fetchDataId) throws Exception {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = null;
		try {
			Specifics specific = Specifics.findById(fetchDataId);
			if (null != specific) {
				Branch branch = null;
				// Map<String, String> countries = CountryCurrencyUtil.getCountries();
				result = Json.newObject();
				ObjectNode row = Json.newObject();
				ObjectNode subRow = null;
				ArrayNode an = result.putArray("result");
				ArrayNode subArray = null;
				ArrayNode an2 = null;
				if (null != specific.getParticularsId() && null != specific.getParticularsId().getName()) {
					row.put("parent", specific.getParticularsId().getName());
					if ("incomes".equalsIgnoreCase(specific.getParticularsId().getName().toLowerCase())) {
						if (null != specific.getIncomeSpecfPerUnitPrice()) {
							row.put("pricePerUnit",
									IdosConstants.decimalFormat.format(specific.getIncomeSpecfPerUnitPrice()));
						} else {
							row.put("pricePerUnit", "");
						}
						if (null != specific.getIncomeOrExpenseType()) {
							row.put("incomeExpenseInt", specific.getIncomeOrExpenseType());
							if (1 == specific.getIncomeOrExpenseType()) {
								row.put("incomeExpense", "Domestic");
							} else if (2 == specific.getIncomeOrExpenseType()) {
								row.put("incomeExpense", "International");
							} else if (3 == specific.getIncomeOrExpenseType()) {
								row.put("incomeExpense", "Both");
							} else {
								row.put("incomeExpense", "");
							}
						} else {
							row.put("incomeExpense", "");
							row.put("incomeExpenseInt", "");
						}
						/*
						 * List<Branch> branches = specific.getOrganization().getBranches();
						 * if (!branches.isEmpty()) {
						 * subArray = row.putArray("branchTaxes");
						 * for (Branch branch2 : branches) {
						 * if (null != branch2) {
						 * subRow = Json.newObject();
						 * if (null != branch2.getName()) {
						 * subRow.put("branchName", branch2.getName());
						 * } else {
						 * subRow.put("branchName", "");
						 * }
						 * List<BranchTaxes> branchTaxes = branch2.getBranchTaxes();
						 * if (!branchTaxes.isEmpty()) {
						 * for (BranchTaxes branchTax: branchTaxes) {
						 * if (null != branchTax) {
						 * if (null != branchTax.getTaxRate()) {
						 * subRow.put("taxName", branchTax.getTaxName());
						 * } else {
						 * subRow.put("taxName", "");
						 * }
						 * if (null != branchTax.getTaxRate()) {
						 * subRow.put("taxRate", branchTax.getTaxRate());
						 * } else {
						 * subRow.put("taxRate", "");
						 * }
						 * subArray.add(subRow);
						 * }
						 * }
						 * }
						 * }
						 * }
						 * }
						 */
					} else if ("expenses".equalsIgnoreCase(specific.getParticularsId().getName().toLowerCase())) {
						if (null != specific.getEmployeeClaimItem()) {
							row.put("empClaimItemInt", specific.getEmployeeClaimItem());
							if (0 == specific.getEmployeeClaimItem()) {
								row.put("empClaimItem", "No");
							} else if (1 == specific.getEmployeeClaimItem()) {
								row.put("empClaimItem", "Yes");
							} else {
								row.put("empClaimItem", "");
							}
						} else {
							row.put("empClaimItem", "");
							row.put("empClaimItemInt", "");
						}
						if (null != specific.getIsWithholdingApplicable()) {
							row.put("withHoldingApplicableInt", specific.getIsWithholdingApplicable());
							if (1 == specific.getIsWithholdingApplicable()) {
								row.put("withHoldingApplicable", "Yes");
							} else if (0 == specific.getIsWithholdingApplicable()) {
								row.put("withHoldingApplicable", "No");
							} else {
								row.put("withHoldingApplicable", "");
							}
						} else {
							row.put("withHoldingApplicableInt", "");
							row.put("withHoldingApplicable", "");
						}
						if (null != specific.getWithHoldingRate()) {
							row.put("withHoldingRate", specific.getWithHoldingRate());
						} else {
							row.put("withHoldingRate", "");
						}
						if (null != specific.getIsCaptureInputTaxes()) {
							row.put("captureInputTaxesInt", specific.getIsCaptureInputTaxes());
							if (1 == specific.getIsCaptureInputTaxes()) {
								row.put("captureInputTaxes", "Yes");
							} else if (0 == specific.getIsCaptureInputTaxes()) {
								row.put("captureInputTaxes", "No");
							} else {
								row.put("captureInputTaxes", "");
							}
						} else {
							row.put("captureInputTaxesInt", "");
							row.put("captureInputTaxes", "");
						}
						if (null != specific.getWithHoldingLimit()) {
							row.put("withHoldingTransLimit", specific.getWithHoldingLimit());
						} else {
							row.put("withHoldingTransLimit", "");
						}
						if (null != specific.getWithholdingMonetoryLimit()) {
							row.put("withHoldingMonetoryLimit",
									IdosConstants.decimalFormat.format(specific.getWithholdingMonetoryLimit()));
						} else {
							row.put("withHoldingMonetoryLimit", "");
						}
						if (null != specific.getIncomeOrExpenseType()) {
							row.put("incomeExpenseInt", specific.getIncomeOrExpenseType());
							if (1 == specific.getIncomeOrExpenseType()) {
								row.put("incomeExpense", "Capital");
							} else if (2 == specific.getIncomeOrExpenseType()) {
								row.put("incomeExpense", "Revenue");
							} else {
								row.put("incomeExpense", "");
							}
						} else {
							row.put("incomeExpense", "");
							row.put("incomeExpenseInt", "");
						}
					} else if ("assets".equalsIgnoreCase(specific.getParticularsId().getName().toLowerCase())) {
						if (null != specific.getEmployeeClaimItem()) {
							row.put("empClaimItemInt", specific.getEmployeeClaimItem());
							if (0 == specific.getEmployeeClaimItem()) {
								row.put("empClaimItem", "No");
							} else if (1 == specific.getEmployeeClaimItem()) {
								row.put("empClaimItem", "Yes");
							} else {
								row.put("empClaimItem", "");
							}
						} else {
							row.put("empClaimItem", "");
							row.put("empClaimItemInt", "");
						}
					} else if ("liabilities".equalsIgnoreCase(specific.getParticularsId().getName().toLowerCase())) {

					}
				} else {
					row.put("parent", "");
				}
				if (null != specific.getParentSpecifics() && null != specific.getParentSpecifics().getName()) {
					row.put("parentSpecific", specific.getParentSpecifics().getName());
				} else {
					row.put("parentSpecific", "");
				}
				if (null != specific.getName()) {
					row.put("specificName", specific.getName());
				} else {
					row.put("specificName", "");
				}
				if ("incomes".equalsIgnoreCase(specific.getParticularsId().getName().toLowerCase())
						|| "expenses".equalsIgnoreCase(specific.getParticularsId().getName().toLowerCase())) {
					List<VendorSpecific> vendorSpecifics = specific.getSpecificsVendors();
					if (!vendorSpecifics.isEmpty()) {
						subArray = row.putArray("vendors");
						for (VendorSpecific vendorSpecific : vendorSpecifics) {
							if (null != vendorSpecific && null != vendorSpecific.getVendorSpecific()
									&& null != vendorSpecific.getVendorSpecific().getName()) {
								subArray.add(vendorSpecific.getVendorSpecific().getName());
							}
						}
					}
				}
				List<BranchSpecifics> branchSpecifics = specific.getSpecificsBranch();
				if (!branchSpecifics.isEmpty()) {
					subArray = row.putArray("branches");
					for (BranchSpecifics branchSpecific : branchSpecifics) {
						if (null != branchSpecific) {
							branch = branchSpecific.getBranch();
							if (null != branch) {
								subRow = Json.newObject();
								if (null != branch.getName()) {
									subRow.put("branchName", branch.getName());
									if ("incomes"
											.equalsIgnoreCase(specific.getParticularsId().getName().toLowerCase())) {
										List<BranchTaxes> branchTaxes = branch.getBranchTaxes();
										if (!branchTaxes.isEmpty()) {
											ArrayNode sArray = subRow.putArray("branchTax");
											for (BranchTaxes branchTax : branchTaxes) {
												ObjectNode sRow = Json.newObject();
												if (null != branchTax) {
													if (null != branchTax.getTaxName()) {
														sRow.put("taxName", branchTax.getTaxName());
													} else {
														sRow.put("taxName", "");
													}
													if (null != branchTax.getTaxRate()) {
														sRow.put("taxRate", branchTax.getTaxRate());
													} else {
														sRow.put("taxRate", "");
													}
													sArray.add(sRow);
												}
											}
										}
									}
									subArray.add(subRow);
								}
							}
						}
					}
				}
				List<SpecificsTransactionPurpose> transactionPurposes = specific.getSpecificsTransactionPurposes();
				TransactionPurpose transPurpose = null;
				if (!transactionPurposes.isEmpty()) {
					subArray = row.putArray("transactionPurposes");
					for (SpecificsTransactionPurpose purpose : transactionPurposes) {
						if (null != purpose) {
							transPurpose = purpose.getTransactionPurpose();
							if (null != transPurpose) {
								if (null != transPurpose.getTransactionPurpose()) {
									subArray.add(transPurpose.getTransactionPurpose());
								}
							}
						}
					}
				}
				/*
				 * List<VendorSpecific> vendorSpecifics = specific.getSpecificsVendors();
				 * Vendor vendor = null;
				 * if (!vendorSpecifics.isEmpty()) {
				 * subArray = row.putArray("customerDetails");
				 * for (VendorSpecific vendorSpecific : vendorSpecifics) {
				 * vendor = vendorSpecific.getVendorSpecific();
				 * if (null != vendor) {
				 * subRow = Json.newObject();
				 * if (null != vendor.getName()) {
				 * subRow.put("customerName", vendor.getName());
				 * } else {
				 * subRow.put("customerName", "");
				 * }
				 * if (null != vendor.getEmail()) {
				 * subRow.put("customerEmail", vendor.getEmail());
				 * } else {
				 * subRow.put("customerEmail", "");
				 * }
				 * if (null != vendor.getAddress()) {
				 * subRow.put("customerAddress", vendor.getAddress());
				 * } else {
				 * subRow.put("customerAddress", "");
				 * }
				 * if (null != vendor.getCountry()) {
				 * if (countries.containsKey(vendor.getCountry().toString())) {
				 * subRow.put("customerCountry", countries.get(vendor.getCountry().toString()));
				 * } else {
				 * subRow.put("customerCountry", "");
				 * }
				 * } else {
				 * subRow.put("customerCountry", "");
				 * }
				 * if (null != vendor.getLocation()) {
				 * subRow.put("customerLocation", vendor.getLocation());
				 * } else {
				 * subRow.put("customerLocation", "");
				 * }
				 * if (null != vendor.getVendorGroup() && null !=
				 * vendor.getVendorGroup().getGroupName()) {
				 * subRow.put("customerGroup", vendor.getVendorGroup().getGroupName());
				 * } else {
				 * subRow.put("customerGroup", "");
				 * }
				 * List<BranchVendors> branchVendors = vendor.getVendorBranches();
				 * if (!branchVendors.isEmpty()) {
				 * an2 = subRow.putArray("customerBranches");
				 * for (BranchVendors branchVendor : branchVendors) {
				 * if (null != branchVendor) {
				 * branch = branchVendor.getBranch();
				 * if (null != branch && null != branch.getName()) {
				 * an2.add(branch.getName());
				 * }
				 * }
				 * }
				 * }
				 * if (null != vendor.getPurchaseType()) {
				 * subRow.put("customerPurchaseTypeNumber", vendor.getPurchaseType());
				 * if (1 == vendor.getPurchaseType()) {
				 * subRow.put("customerPurchaseType", "Cash");
				 * } else if (2 == vendor.getPurchaseType()) {
				 * subRow.put("customerPurchaseType", "Credit");
				 * } else if (3 == vendor.getPurchaseType()) {
				 * subRow.put("customerPurchaseType", "Both");
				 * } else {
				 * subRow.put("customerPurchaseType", "");
				 * }
				 * if (3 == vendor.getPurchaseType() || 2 == vendor.getPurchaseType()) {
				 * if (null != vendor.getDaysForCredit()) {
				 * subRow.put("customerDaysForCredit", vendor.getDaysForCredit());
				 * } else {
				 * subRow.put("customerDaysForCredit", "0.0");
				 * }
				 * }
				 * } else {
				 * subRow.put("customerPurchaseType", "");
				 * subRow.put("customerDaysForCredit", "0.0");
				 * }
				 * if (null != vendor.getContractAgreement()) {
				 * subRow.put("customerContractAgreement", vendor.getContractAgreement());
				 * } else {
				 * subRow.put("customerContractAgreement", "");
				 * }
				 * if (!vendor.getContractPoDoc().isEmpty()) {
				 * subRow.put("customerContractPoDoc", vendor.getContractPoDoc());
				 * } else {
				 * subRow.put("customerContractPoDoc", "");
				 * }
				 * if (null != vendor.getDiscountPercentage()) {
				 * subRow.put("customerDiscount", vendor.getDiscountPercentage());
				 * } else {
				 * subRow.put("customerDiscount", "");
				 * }
				 * subArray.add(subRow);
				 * }
				 * }
				 * }
				 */
				List<SpecificsKnowledgeLibrary> libraries = specific.getSpecificsKl();
				if (!libraries.isEmpty()) {
					subArray = row.putArray("libraries");
					for (SpecificsKnowledgeLibrary library : libraries) {
						if (null != library) {
							subRow = Json.newObject();
							if (null != library.getKnowledgeLibraryContent()) {
								subRow.put("libraryGuidance", library.getKnowledgeLibraryContent());
							} else {
								subRow.put("libraryGuidance", "");
							}
							if (null != library.getIsMandatory()) {
								subRow.put("libraryIsMandatInt", library.getIsMandatory());
								if (0 == library.getIsMandatory()) {
									subRow.put("libraryIsMandatStr", "No");
								} else if (1 == library.getIsMandatory()) {
									subRow.put("libraryIsMandatStr", "Yes");
								} else {
									subRow.put("libraryIsMandatStr", "");
								}
							} else {
								subRow.put("libraryIsMandatInt", "");
								subRow.put("libraryIsMandatStr", "");
							}
							List<SpecificsKnowledgeLibraryForBranch> libraryBranches = library.getSpecificsKlibrary();
							if (!libraryBranches.isEmpty()) {
								an2 = subRow.putArray("libraryBranches");
								for (SpecificsKnowledgeLibraryForBranch libraryBranch : libraryBranches) {
									if (null != libraryBranch) {
										branch = libraryBranch.getBranch();
										if (null != branch && null != branch.getName()) {
											an2.add(branch.getName());
										}
									}
								}
							}
							subArray.add(subRow);
						}
					}
				}
				an.add(row);
			}
		} catch (Exception e) {
			throw e;
		}
		return result;
	}

	@Override
	public ObjectNode getProject(final long fetchDataId) throws Exception {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = null;
		try {
			Project project = Project.findById(fetchDataId);
			if (null != project) {
				result = Json.newObject();
				ObjectNode row = Json.newObject();
				ObjectNode row2 = null;
				// ArrayNode an = result.putArray("result");
				ArrayNode an2 = null;
				ArrayNode subArray = null;
				Map<String, String> countries = CountryCurrencyUtil.getCountries();
				if (null != project.getName()) {
					result.put("projectName", project.getName());
				} else {
					result.put("projectName", "");
				}
				if (null != project.getNumber()) {
					result.put("projectNumber", project.getNumber());
				} else {
					result.put("projectNumber", "");
				}
				if (null != project.getStartDate()) {
					result.put("projectStartDate", StaticController.idosdf.format(project.getStartDate()));
				} else {
					result.put("projectStartDate", "");
				}
				if (null != project.getEndDate()) {
					result.put("projectEndDate", StaticController.idosdf.format(project.getEndDate()));
				} else {
					row.put("projectEndDate", "");
				}
				if (null != project.getCountry()) {
					// row.put("projectCountryNumber", project.getNumber());
					if (countries.containsKey(project.getCountry().toString())) {
						result.put("projectCountry", countries.get(project.getCountry().toString()));
					} else {
						result.put("projectCountry", "");
					}
				} else {
					// result.put("projectCountryNumber", "");
					result.put("projectCountry", "");
				}
				if (null != project.getLocation()) {
					result.put("projectLocation", project.getLocation());
				} else {
					result.put("projectLocation", "");
				}
				List<ProjectBranches> projectBranches = project.getProjectBranch();
				if (!projectBranches.isEmpty()) {
					subArray = result.putArray("projectBranches");
					for (ProjectBranches projectBranch : projectBranches) {
						if (null != projectBranch && null != projectBranch.getProjectBranch()
								&& null != projectBranch.getProjectBranch().getName()) {
							subArray.add(projectBranch.getProjectBranch().getName());
						}
					}
				}
				if (null != project.getProjectDirectorName()) {
					result.put("projectDirector", project.getProjectDirectorName());
				} else {
					result.put("projectDirector", "");
				}
				if (null != project.getDirectorPhoneNumber()) {
					result.put("projectDirectorPhone", project.getDirectorPhoneNumber());
				} else {
					result.put("projectDirectorPhone", "");
				}
				if (null != project.getProjectManagerName()) {
					result.put("projectManager", project.getProjectManagerName());
				} else {
					result.put("projectManager", "");
				}
				if (null != project.getManagerPhoneNumber()) {
					result.put("projectManagerPhone", project.getManagerPhoneNumber());
				} else {
					result.put("projectManagerPhone", "");
				}
				if (null != project.getId()) {
					String query = "From ProjectLabourPosition where project.id  =  " + project.getId();
					List<ProjectLabourPosition> labourPositions = genericDAO.executeSimpleQuery(query, entityManager);
					if (!labourPositions.isEmpty()) {
						an2 = result.putArray("labourPositions");
						for (ProjectLabourPosition labourPosition : labourPositions) {
							if (null != labourPosition) {
								row2 = Json.newObject();
								if (null != labourPosition.getPositionName()) {
									row2.put("positionName", labourPosition.getPositionName());
								} else {
									row2.put("positionName", "");
								}
								if (null != labourPosition.getPositionValidity()) {
									row2.put("positionValidity",
											StaticController.idosdf.format(labourPosition.getPositionValidity()));
								} else {
									row2.put("positionValidity", "");
								}
								if (null != labourPosition.getLocation()) {
									try {
										Long id = Long.parseLong(labourPosition.getLocation());
										if (null != id) {
											Branch branch = Branch.findById(id);
											if (null != branch) {
												row2.put("positionLocation", branch.getName());
											} else {
												row2.put("positionLocation", "");
											}
										} else {
											row2.put("positionLocation", "");
										}
									} catch (NumberFormatException e) {
										row2.put("positionLocation", labourPosition.getLocation());
									}
								} else {
									row2.put("positionLocation", "");
								}
								/*
								 * List<ProjectLabourPositionQualification> qualifications =
								 * labourPosition.getPjctLabourpositionQualification();
								 * if (!qualifications.isEmpty()) {
								 * subArray = row2.putArray("labourQualifications");
								 * for (ProjectLabourPositionQualification qualification : qualifications) {
								 * if (null != qualification) {
								 * subRow = Json.newObject();
								 * System.out.println(qualification.getQualificationName() + "\t" +
								 * qualification.getQualificationDegree());
								 * if (null != qualification.getQualificationName()) {
								 * if ("1".equalsIgnoreCase(qualification.getQualificationName())) {
								 * subRow.put("qualificationName", "High School");
								 * } else if ("2".equalsIgnoreCase(qualification.getQualificationName())) {
								 * subRow.put("qualificationName", "Under Graduate");
								 * } else if ("3".equalsIgnoreCase(qualification.getQualificationName())) {
								 * subRow.put("qualificationName", "Graduate");
								 * } else if ("4".equalsIgnoreCase(qualification.getQualificationName())) {
								 * subRow.put("qualificationName", "Post Graduate");
								 * } else if ("5".equalsIgnoreCase(qualification.getQualificationName())) {
								 * subRow.put("qualificationName", "Professional Qualification");
								 * } else if ("6".equalsIgnoreCase(qualification.getQualificationName())) {
								 * subRow.put("qualificationName", "Doctoral Qualification");
								 * } else {
								 * subRow.put("qualificationName", "");
								 * }
								 * } else {
								 * subRow.put("qualificationName", "");
								 * }
								 * if (null != qualification.getQualificationDegree()) {
								 * subRow.put("qualificationDegree", qualification.getQualificationDegree());
								 * } else {
								 * subRow.put("qualificationDegree", "");
								 * }
								 * System.out.println(subRow.size());
								 * subArray.add(subRow);
								 * System.out.println(subArray.size());
								 * }
								 * }
								 * }
								 * if (null != labourPosition.getExpRequired()) {
								 * if ("1".equalsIgnoreCase(labourPosition.getExpRequired())) {
								 * row2.put("expRequired", "0");
								 * } else if ("2".equalsIgnoreCase(labourPosition.getExpRequired())) {
								 * row2.put("expRequired", "1");
								 * } else if ("3".equalsIgnoreCase(labourPosition.getExpRequired())) {
								 * row2.put("expRequired", "2");
								 * } else if ("4".equalsIgnoreCase(labourPosition.getExpRequired())) {
								 * row2.put("expRequired", "3-4");
								 * } else if ("5".equalsIgnoreCase(labourPosition.getExpRequired())) {
								 * row2.put("expRequired", "5-6");
								 * } else if ("6".equalsIgnoreCase(labourPosition.getExpRequired())) {
								 * row2.put("expRequired", "6-7");
								 * } else if ("7".equalsIgnoreCase(labourPosition.getExpRequired())) {
								 * row2.put("expRequired", "8-10");
								 * } else if ("8".equalsIgnoreCase(labourPosition.getExpRequired())) {
								 * row2.put("expRequired", "11-12");
								 * } else if ("9".equalsIgnoreCase(labourPosition.getExpRequired())) {
								 * row2.put("expRequired", "12-15");
								 * } else if ("10".equalsIgnoreCase(labourPosition.getExpRequired())) {
								 * row2.put("expRequired", "15-20");
								 * } else if ("11".equalsIgnoreCase(labourPosition.getExpRequired())) {
								 * row2.put("expRequired", "20-25");
								 * } else if ("12".equalsIgnoreCase(labourPosition.getExpRequired())) {
								 * row2.put("expRequired", "25+");
								 * } else {
								 * row2.put("expRequired", "");
								 * }
								 * }
								 * if (null != labourPosition.getLanguages()) {
								 * row2.put("langRequired", labourPosition.getLanguages());
								 * } else {
								 * row2.put("langRequired", "");
								 * }
								 * if (null != labourPosition.getProficiency()) {
								 * row2.put("langProficiency", labourPosition.getProficiency());
								 * } else {
								 * row2.put("langProficiency", "");
								 * }
								 * if (null != labourPosition.getJobDescription()) {
								 * row2.put("jobDescription", labourPosition.getJobDescription());
								 * } else {
								 * row2.put("jobDescription", "");
								 * }
								 * if (null != labourPosition.getRequiresApproval()) {
								 * if (1 == labourPosition.getRequiresApproval()) {
								 * row2.put("requiresApproval", "Yes");
								 * } else if (2 == labourPosition.getRequiresApproval()) {
								 * row2.put("requiresApproval", "No");
								 * } else {
								 * row2.put("requiresApproval", "");
								 * }
								 * } else {
								 * row2.put("requiresApproval", "");
								 * }
								 * if (null != labourPosition.getPlaceOfAdvertisement()) {
								 * row2.put("placeOfAd", labourPosition.getPlaceOfAdvertisement());
								 * } else {
								 * row2.put("placeOfAd", "");
								 * }
								 * if (null != labourPosition.getBudget()) {
								 * row2.put("budget", labourPosition.getBudget());
								 * } else {
								 * row2.put("budget", "");
								 * }
								 * if (null != labourPosition.getAgreementTemlateDoc()) {
								 * row2.put("agreementTemplate",
								 * labourPosition.getAgreementTemlateDoc().trim());
								 * } else {
								 * row2.put("agreementTemplate", "");
								 * }
								 */
								an2.add(row2);
							}
						}
					}
				}
				// an.add(row);
			}
		} catch (Exception e) {
			throw e;
		}
		return result;
	}

	@Override
	public void downloadOperationalVendorCustomerData(final Organization org, final int type) throws Exception {
		log.log(Level.FINE, "============ Start");
		// String query = "FROM Vendor WHERE type = " + type;
		// List<Vendor> vendors = genericDAO.executeSimpleQuery(query, entityManager);
		try {
			Map criterias = new HashMap();
			criterias.put("type", type);
			criterias.put("organization.id", org.getId());
			criterias.put("presentStatus", 1);
			List<Vendor> vendors = genericDAO.findByCriteria(Vendor.class, criterias, entityManager);
			Collections.reverse(vendors);
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public ObjectNode getTransactionExceedingBudgetGroupDetails(final long specificId, final Users user)
			throws Exception {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		ArrayNode an = result.putArray("resultArray");
		result.put("result", false);
		try {
			Calendar newestcal = Calendar.getInstance();
			String currentDate = StaticController.mysqldf.format(newestcal.getTime());
			currentDate += " 23:59:59";
			newestcal.add(Calendar.DAY_OF_WEEK, -14);
			String forteenDaysBack = StaticController.mysqldf.format(newestcal.getTime());
			forteenDaysBack += " 00:00:00";
			String query = "select obj from Transaction obj WHERE obj.transactionExceedingBudget = 1 and obj.transactionBranchOrganization = '"
					+ user.getOrganization().getId() + "' and obj.transactionSpecifics = '" + specificId
					+ "' and obj.presentStatus=1 and obj.transactionDate between '" + forteenDaysBack + "' and '"
					+ currentDate + "'";
			List<Transaction> transactions = genericDAO.executeSimpleQuery(query, entityManager);
			if (!transactions.isEmpty() && transactions.size() > 0) {
				result.put("result", true);
				for (Transaction usrTxn : transactions) {
					if (null != usrTxn) {
						ObjectNode event = Json.newObject();
						event.put("id", usrTxn.getId());
						if (usrTxn.getTransactionBranch() != null) {
							event.put("branchName", usrTxn.getTransactionBranch().getName());
						} else {
							event.put("branchName", "");
						}
						if (usrTxn.getTransactionProject() != null) {
							event.put("projectName", usrTxn.getTransactionProject().getName());
						} else {
							event.put("projectName", "");
						}
						if (usrTxn.getTransactionSpecifics() != null) {
							event.put("itemName", usrTxn.getTransactionSpecifics().getName());
						} else {
							event.put("itemName", "");
						}
						if (usrTxn.getTransactionSpecifics() != null) {
							if (usrTxn.getTransactionSpecifics().getParentSpecifics() != null
									&& !usrTxn.getTransactionSpecifics().getParentSpecifics().equals("")) {
								event.put("itemParentName",
										usrTxn.getTransactionSpecifics().getParentSpecifics().getName());
							} else {
								event.put("itemParentName",
										usrTxn.getTransactionSpecifics().getParticularsId().getName());
							}
						} else {
							event.put("itemParentName", "");
						}
						if (usrTxn.getBudgetAvailDuringTxn() != null) {
							String[] budgetAvailableArr = usrTxn.getBudgetAvailDuringTxn().split(":");
							event.put("budgetAvailable", budgetAvailableArr[0]);
							Double budgetExceededBy = 0.0;
							if (budgetAvailableArr.length > 1) {
								event.put("budgetAvailableAmt", budgetAvailableArr[1]);
								budgetExceededBy = Double.parseDouble(budgetAvailableArr[1]) - usrTxn.getNetAmount();
							} else {
								event.put("budgetAvailableAmt", "");
							}
							if (budgetExceededBy > 0.0 || budgetExceededBy < 0.0) {
								event.put("budgetExceededBy", StaticController.decimalFormat.format(budgetExceededBy));
							} else {
								event.put("budgetExceededBy", "");
							}
						} else {
							event.put("budgetAvailable", "");
							event.put("budgetAvailableAmt", "");
						}
						if (usrTxn.getActualAllocatedBudget() != null) {
							String[] budgetAllocatedArr = usrTxn.getActualAllocatedBudget().split(":");
							event.put("budgetAllocated", budgetAllocatedArr[0]);
							if (budgetAllocatedArr.length > 1) {
								event.put("budgetAllocatedAmt", budgetAllocatedArr[1]);
							} else {
								event.put("budgetAllocatedAmt", "");
							}
						} else {
							event.put("budgetAllocated", "");
							event.put("budgetAllocatedAmt", "");
						}
						if (usrTxn.getTransactionVendorCustomer() != null) {
							event.put("customerVendorName", usrTxn.getTransactionVendorCustomer().getName());
						} else {
							if (usrTxn.getTransactionUnavailableVendorCustomer() != null) {
								event.put("customerVendorName", usrTxn.getTransactionUnavailableVendorCustomer());
							} else {
								event.put("customerVendorName", "");
							}
						}
						event.put("transactionPurpose", usrTxn.getTransactionPurpose().getTransactionPurpose());
						event.put("txnDate", StaticController.idosdf.format(usrTxn.getTransactionDate()));
						String invoiceDate = "";
						String invoiceDateLabel = "";
						if (usrTxn.getTransactionInvoiceDate() != null) {
							invoiceDateLabel = "INVOICE DATE:";
							invoiceDate = StaticController.idosdf.format(usrTxn.getTransactionInvoiceDate());
						}
						event.put("invoiceDateLabel", invoiceDateLabel);
						event.put("invoiceDate", invoiceDate);
						if (usrTxn.getReceiptDetailsType() != null) {
							if (usrTxn.getReceiptDetailsType() == 1) {
								event.put("paymentMode", "CASH");
							}
							if (usrTxn.getReceiptDetailsType() == 2) {
								event.put("paymentMode", "BANK");
							}
						} else {
							event.put("paymentMode", "");
						}
						if (usrTxn.getNoOfUnits() != null) {
							event.put("noOfUnit", usrTxn.getNoOfUnits());
						} else {
							event.put("noOfUnit", "");
						}
						if (usrTxn.getPricePerUnit() != null) {
							event.put("unitPrice", IdosConstants.decimalFormat.format(usrTxn.getPricePerUnit()));
						} else {
							event.put("unitPrice", "");
						}
						if (usrTxn.getGrossAmount() != null) {
							event.put("grossAmount", IdosConstants.decimalFormat.format(usrTxn.getGrossAmount()));
						} else {
							event.put("grossAmount", "");
						}
						event.put("netAmount", IdosConstants.decimalFormat.format(usrTxn.getNetAmount()));
						if (usrTxn.getNetAmountResultDescription() != null
								&& !usrTxn.getNetAmountResultDescription().equals("null")) {
							event.put("netAmtDesc", usrTxn.getNetAmountResultDescription());
						} else {
							event.put("netAmtDesc", "");
						}
						event.put("status", usrTxn.getTransactionStatus());
						event.put("createdBy", usrTxn.getCreatedBy().getEmail());
						if (usrTxn.getApproverActionBy() != null) {
							event.put("approverLabel", "APPROVER:");
							event.put("approverEmail", usrTxn.getApproverActionBy().getEmail());
						} else {
							event.put("approverLabel", "");
							event.put("approverEmail", "");
						}
						if (usrTxn.getSupportingDocs() != null) {
							event.put("txnDocument", usrTxn.getSupportingDocs());
						} else {
							event.put("txnDocument", "");
						}
						if (usrTxn.getRemarks() != null) {
							event.put("txnRemarks", usrTxn.getRemarks());
						} else {
							event.put("txnRemarks", "");
						}
						String txnSpecialStatus = "";
						if (usrTxn.getTransactionExceedingBudget() != null && usrTxn.getKlFollowStatus() != null) {
							if (usrTxn.getTransactionExceedingBudget() == 1 && usrTxn.getKlFollowStatus() == 0) {
								txnSpecialStatus = "Transaction Exceeding Budget && Knowledge library not followed";
							}
							if (usrTxn.getTransactionExceedingBudget() == 1 && usrTxn.getKlFollowStatus() == 1) {
								txnSpecialStatus = "Transaction Exceeding Budget";
							}
						}
						if (usrTxn.getTransactionExceedingBudget() == null && usrTxn.getKlFollowStatus() != null) {
							if (usrTxn.getKlFollowStatus() == 0) {
								txnSpecialStatus = "Knowledge Library Not Followed";
							}
						}
						if (usrTxn.getTransactionExceedingBudget() != null && usrTxn.getKlFollowStatus() == null) {
							txnSpecialStatus = "Transaction Exceeding Budget";
						}
						event.put("txnSpecialStatus", txnSpecialStatus);
						event.put("useremail", user.getEmail());
						event.put("approverEmails", usrTxn.getApproverEmails());
						event.put("additionalapproverEmails", usrTxn.getAdditionalApproverEmails());
						event.put("selectedAdditionalApproval", usrTxn.getSelectedAdditionalApprover());
						an.add(event);
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return result;
	}

	@Override
	public ObjectNode getUser(final Long id) throws Exception {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		try {
			if (null != id || !"".equals(id)) {
				Users user = Users.findById(id);
				if (null != user) {
					result.put("result", true);
					if (null != user.getId()) {
						ArrayNode an = null;
						ObjectNode row = null;
						result.put("id", user.getId());
						if (null != user.getFullName()) {
							result.put("name", user.getFullName());
						} else {
							result.put("name", "");
						}
						if (null != user.getEmail()) {
							result.put("email", user.getEmail());
						} else {
							result.put("email", "");
						}
						if (null != user.getAllowedProcurementRequest()) {
							result.put("procurementInt", user.getAllowedProcurementRequest());
							if (user.getAllowedProcurementRequest().equals(1)) {
								result.put("procurement", "Yes");
							} else {
								result.put("procurement", "No");
							}
						} else {
							result.put("procurementInt", "");
							result.put("procurement", "");
						}
						if (null != user.getPhoneNumberCountryCode()) {
							result.put("phoneCode", user.getPhoneNumberCountryCode());
						} else {
							result.put("phoneCode", "");
						}
						if (null != user.getMobile()) {
							result.put("phone", user.getMobile());
						} else {
							result.put("phone", "");
						}
						if (null != user.getAddress()) {
							result.put("address", user.getAddress());
						} else {
							result.put("address", "");
						}
						if (!user.getUserRoles().isEmpty() && user.getUserRoles().size() > 0) {
							List<UsersRoles> roles = user.getUserRoles();
							an = result.putArray("roles");
							for (UsersRoles role : roles) {
								if (null != role) {
									row = Json.newObject();
									if (null != role.getRole() && null != role.getRole().getName()) {
										row.put("role", role.getRole().getName());
									}
									an.add(row);
								}
							}
						} else {
							an = result.putArray("roles");
						}
						if (!user.getUserTxnQuestions().isEmpty() && user.getUserTxnQuestions().size() > 0) {
							List<UserTransactionPurpose> purposes = user.getUserTxnQuestions();
							an = result.putArray("transactionPurpose");
							for (UserTransactionPurpose purpose : purposes) {
								if (null != purpose) {
									row = Json.newObject();
									if (null != purpose.getTransactionPurpose()
											&& null != purpose.getTransactionPurpose().getTransactionPurpose()) {
										row.put("transaction", purpose.getTransactionPurpose().getTransactionPurpose());
									} else {
										row.put("transaction", "");
									}
									an.add(row);
								}
							}
						}
						if (null != user.getBranch() && null != user.getBranch().getName()) {
							result.put("branch", user.getBranch().getName());
						} else {
							result.put("branch", "");
						}
						if (null != user.getDob()) {
							result.put("dob", StaticController.idosdf.format(user.getDob()));
						} else {
							result.put("dob", "");
						}
						if (null != user.getBloodGroup()) {
							result.put("bloodGroup", user.getBloodGroup());
						} else {
							result.put("bloodGroup", "");
						}
						if (null != user.getAltEmail()) {
							result.put("alternateEmail", user.getAltEmail());
						} else {
							result.put("alternateEmail", "");
						}
						if (null != user.getIdproof()) {
							result.put("idProof", user.getIdproof());
						} else {
							result.put("idProof", "");
						}
						if (null != user.getUserRightsInBranches() && user.getUserRightsInBranches().size() > 0) {
							List<UserRightInBranch> branches = user.getUserRightsInBranches();
							UserRights rights = null;
							ObjectNode node = null;
							ArrayNode anCreator = result.putArray("transactionCreatorBranch");
							ArrayNode anApprover = result.putArray("transactionApproverBranch");
							for (UserRightInBranch branch : branches) {
								if (null != branch && null != branch.getBranch()) {
									rights = branch.getUserRights();
									if (null != rights && null != rights.getId()) {
										node = Json.newObject();
										if (rights.getId() == 1) {
											node.put("creator", branch.getBranch().getName());
											anCreator.add(node);
										} else if (rights.getId() == 2) {
											node.put("approver", branch.getBranch().getName());
											anApprover.add(node);
										}
									}
								}
							}
						}
						if (null != user.getUserRightForProjects() && user.getUserRightForProjects().size() > 0) {
							List<UserRightForProject> projects = user.getUserRightForProjects();
							UserRights rights = null;
							ObjectNode node = null;
							ArrayNode anCreator = result.putArray("transactionCreatorProject");
							ArrayNode anApprover = result.putArray("transactionApproverProject");
							for (UserRightForProject project : projects) {
								if (null != project && null != project.getProject()
										&& null != project.getProject().getName()) {
									rights = project.getUserRights();
									if (null != rights && null != rights.getId()) {
										node = Json.newObject();
										if (rights.getId() == 1) {
											node.put("creator", project.getProject().getName());
											anCreator.add(node);
										} else if (rights.getId() == 2) {
											node.put("approver", project.getProject().getName());
											anApprover.add(node);
										}
									}
								}
							}
						}
						if (null != user.getUserRightsSpecifics() && user.getUserRightsSpecifics().size() > 0) {
							List<UserRightSpecifics> specifics = user.getUserRightsSpecifics();
							UserRights rights = null;
							ObjectNode node = null;
							ArrayNode anCreator = result.putArray("transactionCreatorCOA");
							ArrayNode anApprover = result.putArray("transactionApproverCOA");
							for (UserRightSpecifics specific : specifics) {
								if (null != specific && null != specific.getSpecifics()
										&& null != specific.getSpecifics().getName()) {
									rights = specific.getUserRights();
									if (null != rights && null != rights.getId()) {
										node = Json.newObject();
										if (rights.getId() == 1) {
											node.put("creator", specific.getSpecifics().getName());
											anCreator.add(node);
										} else if (rights.getId() == 2) {
											node.put("approver", specific.getSpecifics().getName());
											anApprover.add(node);
										}
									}
								}
							}
						}
						if (null != user.gettGroup() && null != user.gettGroup().getTravelGroupName()) {
							result.put("travelGroup", user.gettGroup().getTravelGroupName());
						} else {
							result.put("travelGroup", "");
						}
						if (null != user.getUsersTravelClaimTxnQuestions()) {
							String claim = user.getUsersTravelClaimTxnQuestions();
							String[] claims = claim.split(",");
							an = result.putArray("travelGroupPurpose");
							for (String s : claims) {
								if (null != s && !"".equals(s)) {
									TransactionPurpose purpose = TransactionPurpose.findById(Long.parseLong(s));
									if (null != purpose && null != purpose.getTransactionPurpose()) {
										row.put("purpose", purpose.getTransactionPurpose());
									}
									an.add(row);
								}
							}
						}
						if (null != user.geteGroup() && null != user.geteGroup().getExpenseGroupName()) {
							result.put("expenseGroup", user.geteGroup().getExpenseGroupName());
						} else {
							result.put("expenseGroup", "");
						}
						if (null != user.getUsersExpenseClaimTxnQuestions()) {
							String claim = user.getUsersExpenseClaimTxnQuestions();
							String[] claims = claim.split(",");
							an = result.putArray("expenseGroupPurpose");
							for (String s : claims) {
								if (null != s && !"".equals(s)) {
									TransactionPurpose purpose = TransactionPurpose.findById(Long.parseLong(s));
									if (null != purpose && null != purpose.getTransactionPurpose()) {
										row.put("purpose", purpose.getTransactionPurpose());
									}
									an.add(row);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return result;
	}

	@Override
	public ObjectNode getExpenseClaims(final Long id) throws Exception {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		try {
			if (null != id && !"".equals(id)) {
				ObjectNode row = null;
				ArrayNode an = result.putArray("claims");
				ExpenseGroup expenseGroup = ExpenseGroup.findById(id);
				if (null != expenseGroup && null != expenseGroup.getId()) {
					result.put("result", true);
					result.put("id", expenseGroup.getId());
					if (null != expenseGroup.getExpenseGroupName()) {
						result.put("name", expenseGroup.getExpenseGroupName());
					} else {
						result.put("name", "");
					}
					if (null != expenseGroup.getExpenseGroupExpenseItemMonetoryClaims()
							&& expenseGroup.getExpenseGroupExpenseItemMonetoryClaims().size() > 0) {
						List<ExpenseGroupExpenseItemMonetoryClaim> claims = expenseGroup
								.getExpenseGroupExpenseItemMonetoryClaims();
						for (ExpenseGroupExpenseItemMonetoryClaim claim : claims) {
							if (null != claim && null != claim.getId()) {
								row = Json.newObject();
								row.put("id", claim.getId());
								if (null != claim.getSpecificsItem()) {
									if (null != claim.getSpecificsItem().getName()) {
										row.put("name", claim.getSpecificsItem().getName());
									} else {
										row.put("name", "");
									}
									if (null != claim.getMaximumPermittedAdvance()) {
										row.put("advance",
												IdosConstants.decimalFormat.format(claim.getMaximumPermittedAdvance()));
									} else {
										row.put("advance", "");
									}
									if (null != claim.getMonthlyMonetoryLimitForReimbursement()) {
										row.put("limit", IdosConstants.decimalFormat
												.format(claim.getMonthlyMonetoryLimitForReimbursement()));
									} else {
										row.put("limit", "");
									}
								}
								an.add(row);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return result;
	}

	@Override
	public ObjectNode getTravelClaims(final Long id) throws Exception {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		try {
			if (null != id && !"".equals("id")) {
				Travel_Group group = Travel_Group.findById(id);
				if (null != group && null != group.getId()) {
					result.put("result", true);
					result.put("id", group.getId());
					ObjectNode row = null;
					ArrayNode an = null;
					if (null != group.getTravelGroupName()) {
						result.put("name", group.getTravelGroupName());
					} else {
						result.put("name", "");
					}
					if (null != group.getDailyLimitOtherOfficialPurposeExpenses()
							&& group.getDailyLimitOtherOfficialPurposeExpenses().size() > 0) {
						an = result.putArray("otherExpenses");
						List<TravelGroupMaxDailyLimitOtherOfficialPurposeExpenses> expenses = group
								.getDailyLimitOtherOfficialPurposeExpenses();
						for (TravelGroupMaxDailyLimitOtherOfficialPurposeExpenses expense : expenses) {
							if (null != expense && null != expense.getId()) {
								row = Json.newObject();
								row.put("id", expense.getId());
								if (null != expense.getCountryCapital()) {
									row.put("countryCapital", expense.getCountryCapital());
									row.put("1", expense.getCountryCapital());
								} else {
									row.put("countryCapital", "");
									row.put("1", "");
								}
								if (null != expense.getStateCapital()) {
									row.put("stateCapital", expense.getStateCapital());
									row.put("2", expense.getStateCapital());
								} else {
									row.put("stateCapital", "");
									row.put("2", "");
								}
								if (null != expense.getMetroCity()) {
									row.put("metroCity", expense.getMetroCity());
									row.put("3", expense.getMetroCity());
								} else {
									row.put("metroCity", "");
									row.put("3", "");
								}
								if (null != expense.getOtherCities()) {
									row.put("otherCities", expense.getOtherCities());
									row.put("4", expense.getOtherCities());
								} else {
									row.put("otherCities", "");
									row.put("4", "");
								}
								if (null != expense.getTown()) {
									row.put("town", expense.getTown());
									row.put("5", expense.getTown());
								} else {
									row.put("town", "");
									row.put("5", "");
								}
								if (null != expense.getCounty()) {
									row.put("country", expense.getCounty());
									row.put("6", expense.getCounty());
								} else {
									row.put("country", "");
									row.put("6", "");
								}
								if (null != expense.getMunicipality()) {
									row.put("municipality", expense.getMunicipality());
									row.put("7", expense.getMunicipality());
								} else {
									row.put("municipality", "");
									row.put("7", "");
								}
								if (null != expense.getVillage()) {
									row.put("village", expense.getVillage());
									row.put("8", expense.getVillage());
								} else {
									row.put("village", "");
									row.put("8", "");
								}
								if (null != expense.getRemoteLocation()) {
									row.put("remoteLocation", expense.getRemoteLocation());
									row.put("9", expense.getRemoteLocation());
								} else {
									row.put("remoteLocation", "");
									row.put("9", "");
								}
								if (null != expense.getTwentyMilesAwayFromClosestCityTown()) {
									row.put("20Miles", expense.getTwentyMilesAwayFromClosestCityTown());
									row.put("10", expense.getTwentyMilesAwayFromClosestCityTown());
								} else {
									row.put("20Miles", "");
									row.put("10", "");
								}
								if (null != expense.getHillStation()) {
									row.put("hillStation", expense.getHillStation());
									row.put("11", expense.getHillStation());
								} else {
									row.put("hillStation", "");
									row.put("11", "");
								}
								if (null != expense.getResort()) {
									row.put("resort", expense.getResort());
									row.put("12", expense.getResort());
								} else {
									row.put("resort", "");
									row.put("12", "");
								}
								if (null != expense.getConflictWarZonePlace()) {
									row.put("warZone", expense.getConflictWarZonePlace());
									row.put("13", expense.getConflictWarZonePlace());
								} else {
									row.put("warZone", "");
									row.put("13", "");
								}
								an.add(row);
							}
						}
					}
					if (null != group.getTravelGroupPermittedBoardingLodging()
							&& group.getTravelGroupPermittedBoardingLodging().size() > 0) {
						an = result.putArray("lodgings");
						List<TravelGroupPermittedBoardingLodging> lodgings = group
								.getTravelGroupPermittedBoardingLodging();
						for (TravelGroupPermittedBoardingLodging lodging : lodgings) {
							if (null != lodging && null != lodging.getId()) {
								row = Json.newObject();
								row.put("id", lodging.getId());
								if (null != lodging.getMaxPermittedFoodCostPerDay()) {
									row.put("day", lodging.getMaxPermittedFoodCostPerDay());
								} else {
									row.put("day", "");
								}
								if (null != lodging.getMaxPermittedRoomCostPerNight()) {
									row.put("night", lodging.getMaxPermittedRoomCostPerNight());
								} else {
									row.put("night", "");
								}
								if (null != lodging.getAccomodationType()
										&& null != lodging.getAccomodationType().getAccomodationTypeName()) {
									row.put("name", lodging.getAccomodationType().getAccomodationTypeName());
								} else {
									row.put("name", "");
								}
								if (null != lodging.getCityType()) {
									row.put("city", lodging.getCityType());
								} else {
									row.put("city", "");
								}
								an.add(row);
							}
						}
					}
					if (null != group.getDistanceMilesKmsAllowedTravelModes()
							&& group.getDistanceMilesKmsAllowedTravelModes().size() > 0) {
						an = result.putArray("travelModes");
						List<TravelGroupDistanceMilesKmsAllowedTravelMode> modes = group
								.getDistanceMilesKmsAllowedTravelModes();
						List<DistanceMilesKm> distanceMilesKms = genericDAO.findAll(DistanceMilesKm.class, true, false,
								entityManager);
						if (null != distanceMilesKms && distanceMilesKms.size() > 0) {
							for (DistanceMilesKm km : distanceMilesKms) {
								if (null != km && null != km.getDistanceInMilesKms()) {
									row = Json.newObject();
									row.put("name", km.getDistanceInMilesKms());
									row.putArray(km.getDistanceInMilesKms() + "Array");
								}
								an.add(row);
							}
						}
						for (TravelGroupDistanceMilesKmsAllowedTravelMode mode : modes) {
							if (null != mode && null != mode.getId()) {
								if (null != mode.getDistanceMilesKms()
										&& null != mode.getDistanceMilesKms().getDistanceInMilesKms()) {
									an = (ArrayNode) result.get("travelModes");
									an = (ArrayNode) an
											.findValue(mode.getDistanceMilesKms().getDistanceInMilesKms() + "Array");
									row = Json.newObject();
									row.put("id", mode.getId());
									if (null != mode.getOneWayFare()) {
										row.put("oneWay", mode.getOneWayFare());
									} else {
										row.put("oneWay", "");
									}
									if (null != mode.getReturnFare()) {
										row.put("return", mode.getReturnFare());
									} else {
										row.put("return", "");
									}
									if (null != mode.getTravelMode()
											&& null != mode.getTravelMode().getTravelModeName()) {
										row.put("name", mode.getTravelMode().getTravelModeName());
									} else {
										row.put("name", "");
									}
								} else {
									row.put("oneWay", "");
									row.put("return", "");
									row.put("name", "");
								}
								an.add(row);
							}
						}
					}
					if (null != group.getFixedDailyPerDIAM() && group.getFixedDailyPerDIAM().size() > 0) {
						an = result.putArray("fixedDaily");
						List<TravelGroupFixedDailyPerDIAM> diams = group.getFixedDailyPerDIAM();
						for (TravelGroupFixedDailyPerDIAM diam : diams) {
							if (null != diam && null != diam.getId()) {
								row = Json.newObject();
								row.put("id", diam.getId());
								row = Json.newObject();
								if (null != diam.getCountryCapital()) {
									row.put("countryCapital", diam.getCountryCapital());
									row.put("1", diam.getCountryCapital());
								} else {
									row.put("countryCapital", "");
									row.put("1", "");
								}
								an.add(row);
								row = Json.newObject();
								if (null != diam.getStateCapital()) {
									row.put("stateCapital", diam.getStateCapital());
									row.put("2", diam.getStateCapital());
								} else {
									row.put("stateCapital", "");
									row.put("2", "");
								}
								an.add(row);
								row = Json.newObject();
								if (null != diam.getMetroCity()) {
									row.put("metroCity", diam.getMetroCity());
									row.put("3", diam.getMetroCity());
								} else {
									row.put("metroCity", "");
									row.put("3", "");
								}
								an.add(row);
								row = Json.newObject();
								if (null != diam.getOtherCities()) {
									row.put("otherCities", diam.getOtherCities());
									row.put("4", diam.getOtherCities());
								} else {
									row.put("otherCities", "");
									row.put("4", "");
								}
								an.add(row);
								row = Json.newObject();
								if (null != diam.getTown()) {
									row.put("town", diam.getTown());
									row.put("5", diam.getTown());
								} else {
									row.put("town", "");
									row.put("5", "");
								}
								an.add(row);
								row = Json.newObject();
								if (null != diam.getCounty()) {
									row.put("country", diam.getCounty());
									row.put("6", diam.getCounty());
								} else {
									row.put("country", "");
									row.put("6", "");
								}
								an.add(row);
								row = Json.newObject();
								if (null != diam.getMunicipality()) {
									row.put("municipality", diam.getMunicipality());
									row.put("7", diam.getMunicipality());
								} else {
									row.put("municipality", "");
									row.put("7", "");
								}
								an.add(row);
								row = Json.newObject();
								if (null != diam.getVillage()) {
									row.put("village", diam.getVillage());
									row.put("8", diam.getVillage());
								} else {
									row.put("village", "");
									row.put("8", "");
								}
								an.add(row);
								row = Json.newObject();
								if (null != diam.getRemoteLocation()) {
									row.put("remoteLocation", diam.getRemoteLocation());
									row.put("9", diam.getRemoteLocation());
								} else {
									row.put("remoteLocation", "");
									row.put("9", "");
								}
								an.add(row);
								row = Json.newObject();
								if (null != diam.getTwentyMilesAwayFromClosestCityTown()) {
									row.put("20Miles", diam.getTwentyMilesAwayFromClosestCityTown());
									row.put("10", diam.getTwentyMilesAwayFromClosestCityTown());
								} else {
									row.put("20Miles", "");
									row.put("10", "");
								}
								an.add(row);
								row = Json.newObject();
								if (null != diam.getHillStation()) {
									row.put("hillStation", diam.getHillStation());
									row.put("11", diam.getHillStation());
								} else {
									row.put("hillStation", "");
									row.put("11", "");
								}
								an.add(row);
								row = Json.newObject();
								if (null != diam.getResort()) {
									row.put("resort", diam.getResort());
									row.put("12", diam.getResort());
								} else {
									row.put("resort", "");
									row.put("12", "");
								}
								an.add(row);
								row = Json.newObject();
								if (null != diam.getConflictWarZonePlace()) {
									row.put("warZone", diam.getConflictWarZonePlace());
									row.put("13", diam.getConflictWarZonePlace());
								} else {
									row.put("warZone", "");
									row.put("13", "");
								}
								an.add(row);
							}
						}
						if (null != group.getTravelGroupkL() && group.getTravelGroupkL().size() > 0) {
							an = result.putArray("knowledgeLibrary");
							List<TravelGroupKnowledgeLibrary> libraries = group.getTravelGroupkL();
							for (TravelGroupKnowledgeLibrary library : libraries) {
								if (null != library && null != library.getId()) {
									row = Json.newObject();
									row.put("id", library.getId());
									if (null != library.getKlMandatory()) {
										row.put("klMandatoryInt", library.getKlMandatory());
										if (1 == library.getKlMandatory()) {
											row.put("klMandatory", "Yes");
										} else {
											row.put("klMandatory", "No");
										}
									} else {
										row.put("klMandatory", "");
										row.put("klMandatoryInt", "");
									}
									if (null != library.getKlContent()) {
										row.put("content", library.getKlContent());
									} else {
										row.put("content", "");
									}
									an.add(row);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return result;
	}

	/**
	 * Now it gets data for month and not for week
	 * 
	 * @param user
	 * @param json
	 * @param entityManager
	 * @return
	 */
	@Override
	public ObjectNode getDashboardProjectFinancial(final Users user, final JsonNode json,
			final EntityManager entityManager) {
		log.log(Level.FINE, "============ Start");
		String currDashboardFromDate = json.findValue("currDashboardFromDate") != null
				? json.findValue("currDashboardFromDate").asText()
				: null;
		String currDashboardToDate = json.findValue("currDashboardToDate") != null
				? json.findValue("currDashboardToDate").asText()
				: null;
		String prevDashboardFromDate = json.findValue("prevDashboardFromDate") != null
				? json.findValue("prevDashboardFromDate").asText()
				: null;
		String prevDashboardToDate = json.findValue("prevDashboardToDate") != null
				? json.findValue("prevDashboardToDate").asText()
				: null;
		ObjectNode results = Json.newObject();
		ArrayNode projectdashboardan = results.putArray("projectDashBoardData");
		ObjectNode projectdashboardrow = Json.newObject();
		try {
			if (user != null && !user.equals("")) {
				String currentWeekStartDate = null, currentWeekEndDate = null;
				if (currDashboardFromDate != null && !"".equals(currDashboardFromDate) && currDashboardToDate != null
						&& !"".equals(currDashboardToDate)) {
					currentWeekStartDate = IdosConstants.mysqldf
							.format(IdosConstants.idosdf.parse(currDashboardFromDate));
					currentWeekEndDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(currDashboardToDate));
				} else {
					// get live data of current Month
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
					// cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
					currentWeekStartDate = IdosConstants.mysqldf.format(cal.getTime());
					// cal.add(Calendar.DAY_OF_WEEK, 6);
					cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
					currentWeekEndDate = IdosConstants.mysqldf.format(cal.getTime());
					// cal.add(Calendar.DAY_OF_WEEK, 1);
				}
				// sum total cash expense for all projects within organization this month start
				StringBuilder sbquery = new StringBuilder("");
				sbquery.append(
						"select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionProject IS NOT NULL AND (obj.transactionPurpose = 3 or obj.transactionPurpose = 11) and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
				List<Transaction> projectcashexpensetxn = genericDAO.executeSimpleQuery(sbquery.toString(),
						entityManager);
				Double projectCreditExpenseThisWeek = null;
				Double projectCashExpenseThisWeek = null;
				if (projectcashexpensetxn.size() > 0) {
					Object val = projectcashexpensetxn.get(0);
					if (val != null) {
						projectdashboardrow.put("projectCashExpense", IdosConstants.decimalFormat.format(val));
						projectCashExpenseThisWeek = Double.parseDouble(String.valueOf(val));
					} else {
						projectdashboardrow.put("projectCashExpense", "");
					}
				} else {
					projectdashboardrow.put("projectCashExpense", "");
				}
				// sum total cash expense for all projects within organization this week end
				// sum total cash sell for all projects this week start
				StringBuilder cssbquery = new StringBuilder("");
				cssbquery.append(
						"select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionProject IS NOT NULL AND obj.transactionPurpose = 1 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
				List<Transaction> projectcashselltxn = genericDAO.executeSimpleQuery(cssbquery.toString(),
						entityManager);
				if (projectcashselltxn.size() > 0) {
					Object val = projectcashselltxn.get(0);
					if (val != null) {
						projectdashboardrow.put("projectCashIncome", IdosConstants.decimalFormat.format(val));
					} else {
						projectdashboardrow.put("projectCashIncome", "");
					}
				} else {
					projectdashboardrow.put("projectCashIncome", "");
				}
				// sum total cash sell for all projects this week end
				// sum total credit expense for all projects this week start
				StringBuilder cdtsbquery = new StringBuilder("");
				cdtsbquery.append(
						"select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionProject IS NOT NULL AND obj.transactionPurpose = 4 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
				List<Transaction> projecrcreditexpensetxn = genericDAO.executeSimpleQuery(cdtsbquery.toString(),
						entityManager);
				if (projecrcreditexpensetxn.size() > 0) {
					Object val = projecrcreditexpensetxn.get(0);
					if (val != null) {
						projectdashboardrow.put("projectCreditExpense", IdosConstants.decimalFormat.format(val));
						projectCreditExpenseThisWeek = Double.parseDouble(String.valueOf(val));
					} else {
						projectdashboardrow.put("projectCreditExpense", "");
					}
				} else {
					projectdashboardrow.put("projectCreditExpense", "");
				}
				// sum total credit expense for all projects this week end
				// sum total credit income for all projects this week start
				StringBuilder cdtincsbquery = new StringBuilder("");
				cdtincsbquery.append(
						"select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionProject IS NOT NULL AND obj.transactionPurpose = 2 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
				List<Transaction> creditincometxn = genericDAO.executeSimpleQuery(cdtincsbquery.toString(),
						entityManager);
				if (creditincometxn.size() > 0) {
					Object val = creditincometxn.get(0);
					if (val != null) {
						projectdashboardrow.put("projectCreditIncome", IdosConstants.decimalFormat.format(val));
					} else {
						projectdashboardrow.put("projectCreditIncome", "");
					}
				} else {
					projectdashboardrow.put("projectCreditIncome", "");
				}
				// sum total credit income for all projects this week end
				// sum total of expense budget allocated for all projects this week start
				/*
				 * StringBuilder expbudgetallocsbquery = new StringBuilder("");
				 * expbudgetallocsbquery.
				 * append("select SUM(obj.budgetTotal) from BranchSpecifics obj WHERE obj.organization = '"
				 * +user.getOrganization().getId()+"'");
				 * List<BranchSpecifics> projectExpbudgetalloc =
				 * genericDAO.executeSimpleQuery(expbudgetallocsbquery.toString(),entityManager)
				 * ;
				 * Object projectExpBudAllocAmount = projectExpbudgetalloc.get(0);
				 * StringBuilder expbudgetdeducsbquery = new StringBuilder("");
				 * expbudgetdeducsbquery.
				 * append("select SUM(obj.budgetDeductedTotal) from BranchSpecifics obj WHERE obj.organization = '"
				 * +user.getOrganization().getId()+"'");
				 * List<BranchSpecifics> projectExpbudgetdeducted =
				 * genericDAO.executeSimpleQuery(expbudgetdeducsbquery.toString(),entityManager)
				 * ;
				 * Object projectExpBudDeductedAmount = projectExpbudgetdeducted.get(0);
				 * Double projectExpBudgetAvail = null;
				 * if(projectExpbudgetdeducted==null){
				 * if(projectExpBudAllocAmount != null){
				 * projectExpBudgetAvail =
				 * Double.parseDouble(decimalFormat.format(projectExpBudAllocAmount));
				 * }
				 * }
				 * if(projectExpBudAllocAmount != null && !"".equals(projectExpBudAllocAmount)
				 * && projectExpBudDeductedAmount != null &&
				 * !projectExpBudDeductedAmount.equals("")){
				 * projectExpBudgetAvail =
				 * Double.parseDouble(decimalFormat.format(projectExpBudAllocAmount))-Double.
				 * parseDouble(decimalFormat.format(projectExpBudDeductedAmount));
				 * }
				 * if(projectCashExpenseThisWeek != null){
				 * projectExpBudgetAvail = projectExpBudgetAvail+projectCashExpenseThisWeek;
				 * }
				 * if(projectCreditExpenseThisWeek != null){
				 * projectExpBudgetAvail = projectExpBudgetAvail+projectCreditExpenseThisWeek;
				 * }
				 * if(projectExpBudgetAvail != null){
				 * projectdashboardrow.put("projectExpBudgetAvail",
				 * decimalFormat.format(projectExpBudgetAvail));
				 * }else{
				 * projectdashboardrow.put("projectExpBudgetAvail", "");
				 * }
				 */
				// sum total of expense budget allocated for all projects this week end
				// sum of total receivables for all projects this week start
				/*
				 * StringBuilder creditincomepaymentmadequery = new StringBuilder("");
				 * creditincomepaymentmadequery.
				 * append("select SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranchOrganization = '"
				 * +user.getOrganization().getId()
				 * +"' AND obj.transactionProject IS NOT NULL AND obj.transactionPurpose = 2 and obj.transactionStatus = 'Accounted' and obj.transactionDate  between '"
				 * +currentWeekStartDate+"' and '"+currentWeekEndDate+"'");
				 * List<Transaction> creditIncomeCustomerNetPaymentMade =
				 * genericDAO.executeSimpleQuery(creditincomepaymentmadequery.toString(),
				 * entityManager);
				 * Object projectCreditIncomeCustPaymentMade =
				 * creditIncomeCustomerNetPaymentMade.get(0);
				 * Double projectNetRecievableThisWeek = null;
				 * if(projectCreditIncomeCustPaymentMade != null &&
				 * !projectCreditIncomeCustPaymentMade.equals("")){
				 * if(creditincometxn.size()>0){
				 * Object val = creditincometxn.get(0);
				 * if(val != null && !val.equals("")){
				 * projectNetRecievableThisWeek =
				 * Double.parseDouble(decimalFormat.format(val))-Double.parseDouble(
				 * decimalFormat.format(projectCreditIncomeCustPaymentMade));
				 * }
				 * }
				 * }
				 * if(projectCreditIncomeCustPaymentMade==null){
				 * if(creditincometxn.size()>0){
				 * Object val = creditincometxn.get(0);
				 * if(val != null && !val.equals("")){
				 * projectNetRecievableThisWeek = Double.parseDouble(decimalFormat.format(val));
				 * }
				 * }
				 * }
				 * if(projectNetRecievableThisWeek != null){
				 * projectdashboardrow.put("projectNetRecievableThisWeek",
				 * decimalFormat.format(projectNetRecievableThisWeek));
				 * }else{
				 * projectdashboardrow.put("projectNetRecievableThisWeek", "");
				 * }
				 */
				// sum of total receivables for all projects this week end
				// sum of total payables for all projects this week start
				/*
				 * StringBuilder creditexpensepaymentmadequery = new StringBuilder("");
				 * creditexpensepaymentmadequery.
				 * append("select SUM(obj.vendorNetPayment) from Transaction obj WHERE obj.transactionBranchOrganization = '"
				 * +user.getOrganization().getId()
				 * +"' AND obj.transactionProject IS NOT NULL AND obj.transactionPurpose = 4 and obj.transactionStatus = 'Accounted' and obj.transactionDate  between '"
				 * +currentWeekStartDate+"' and '"+currentWeekEndDate+"'");
				 * List<Transaction> creditExpenseCustomerNetPaymentMade =
				 * genericDAO.executeSimpleQuery(creditexpensepaymentmadequery.toString(),
				 * entityManager);
				 * Object creditExpenseCustPaymentMade =
				 * creditExpenseCustomerNetPaymentMade.get(0);
				 * Double projectNetPayableThisWeek = null;
				 * if(creditExpenseCustPaymentMade != null &&
				 * !creditExpenseCustPaymentMade.equals("")){
				 * if(projecrcreditexpensetxn.size()>0){
				 * Object val = projecrcreditexpensetxn.get(0);
				 * if(val != null && !val.equals("")){
				 * projectNetPayableThisWeek =
				 * Double.parseDouble(decimalFormat.format(val))-Double.parseDouble(
				 * decimalFormat.format(creditExpenseCustPaymentMade));
				 * }
				 * }
				 * }
				 * if(creditExpenseCustPaymentMade==null){
				 * if(projecrcreditexpensetxn.size()>0){
				 * Object val = projecrcreditexpensetxn.get(0);
				 * if(val != null && !val.equals("")){
				 * projectNetPayableThisWeek = Double.parseDouble(decimalFormat.format(val));
				 * }
				 * }
				 * }
				 * if(projectNetPayableThisWeek != null){
				 * projectdashboardrow.put("projectNetPayableThisWeek",
				 * decimalFormat.format(projectNetPayableThisWeek));
				 * }else{
				 * projectdashboardrow.put("projectNetPayableThisWeek", "");
				 * }
				 */
				// previous month project dash board data start
				// sum of total payables for all projects this week end
				// start previous week dash board datas
				String previousWeekStartDate = null, previousWeekEndDate = null;
				if (prevDashboardFromDate != null && !"".equals(prevDashboardFromDate) && prevDashboardToDate != null
						&& !"".equals(prevDashboardToDate)) {
					previousWeekStartDate = IdosConstants.mysqldf
							.format(IdosConstants.idosdf.parse(prevDashboardFromDate));
					previousWeekEndDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(prevDashboardToDate));
				} else {
					Calendar newcal = Calendar.getInstance();
					newcal.add(Calendar.MONTH, -1);
					newcal.set(Calendar.DAY_OF_MONTH, newcal.getActualMinimum(Calendar.DAY_OF_MONTH));
					// newcal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
					// newcal.add(Calendar.DAY_OF_WEEK, -7);
					previousWeekStartDate = IdosConstants.mysqldf.format(newcal.getTime());
					// newcal.add(Calendar.DAY_OF_WEEK, 6);
					newcal.set(Calendar.DAY_OF_MONTH, newcal.getActualMaximum(Calendar.DAY_OF_MONTH));
					previousWeekEndDate = IdosConstants.mysqldf.format(newcal.getTime());
				}
				Double previousWeekProjectCashExpense = null;
				Double previousWeekProjectCreditExpense = null;
				// sum total cash expense for all projects previous week start
				StringBuilder pwsbquery = new StringBuilder("");
				pwsbquery.append(
						"select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionProject IS NOT NULL AND (obj.transactionPurpose = 3 or obj.transactionPurpose = 11) and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
				List<Transaction> pwprojectcashexpensetxn = genericDAO.executeSimpleQuery(pwsbquery.toString(),
						entityManager);
				if (pwprojectcashexpensetxn.size() > 0) {
					Object val = pwprojectcashexpensetxn.get(0);
					if (val != null) {
						projectdashboardrow.put("previousWeekProjectCashExpense",
								IdosConstants.decimalFormat.format(val));
						previousWeekProjectCashExpense = Double.parseDouble(String.valueOf(val));
					} else {
						projectdashboardrow.put("previousWeekProjectCashExpense", "");
					}
				} else {
					projectdashboardrow.put("previousWeekProjectCashExpense", "");
				}
				// sum total cash expense for all projects previous week end
				// sum total cash sell for all projects previous week start
				StringBuilder pwcssbquery = new StringBuilder("");
				pwcssbquery.append(
						"select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionProject IS NOT NULL AND obj.transactionPurpose = 1 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
				List<Transaction> pwprojectcashselltxn = genericDAO.executeSimpleQuery(pwcssbquery.toString(),
						entityManager);
				if (pwprojectcashselltxn.size() > 0) {
					Object val = pwprojectcashselltxn.get(0);
					if (val != null) {
						projectdashboardrow.put("previousWeekProjectCashIncome",
								IdosConstants.decimalFormat.format(val));
					} else {
						projectdashboardrow.put("previousWeekProjectCashIncome", "");
					}
				} else {
					projectdashboardrow.put("previousWeekProjectCashIncome", "");
				}
				// sum total cash sell for all projects previous week end
				// sum total credit expense for all projects previous week start
				StringBuilder pwcdtsbquery = new StringBuilder("");
				pwcdtsbquery.append(
						"select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionProject IS NOT NULL AND obj.transactionPurpose = 4 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
				List<Transaction> pwprojectcreditexpensetxn = genericDAO.executeSimpleQuery(pwcdtsbquery.toString(),
						entityManager);
				if (pwprojectcreditexpensetxn.size() > 0) {
					Object val = pwprojectcreditexpensetxn.get(0);
					if (val != null) {
						projectdashboardrow.put("previousWeekProjectCreditExpense",
								IdosConstants.decimalFormat.format(val));
						previousWeekProjectCreditExpense = Double.parseDouble(String.valueOf(val));
					} else {
						projectdashboardrow.put("previousWeekProjectCreditExpense", "");
					}
				} else {
					projectdashboardrow.put("previousWeekProjectCreditExpense", "");
				}
				// sum total credit expense for all projects previous week end
				// sum total credit income for all projects previous week start
				StringBuilder pwcdtincsbquery = new StringBuilder("");
				pwcdtincsbquery.append(
						"select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionProject IS NOT NULL AND obj.transactionPurpose = 2 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
				List<Transaction> pwprojectcreditincometxn = genericDAO.executeSimpleQuery(pwcdtincsbquery.toString(),
						entityManager);
				if (pwprojectcreditincometxn.size() > 0) {
					Object val = pwprojectcreditincometxn.get(0);
					if (val != null) {
						projectdashboardrow.put("previousWeekProjectCreditIncome",
								IdosConstants.decimalFormat.format(val));
					} else {
						projectdashboardrow.put("previousWeekProjectCreditIncome", "");
					}
				} else {
					projectdashboardrow.put("previousWeekProjectCreditIncome", "");
				}
				// sum total credit income for all projects previous week end
				// sum total of expense budget allocated for all projects previous week start
				/*
				 * StringBuilder pwexpbudgetallocsbquery = new StringBuilder("");
				 * pwexpbudgetallocsbquery.
				 * append("select SUM(obj.budgetTotal) from BranchSpecifics obj WHERE obj.organization = '"
				 * +user.getOrganization().getId()+"'");
				 * List<BranchSpecifics> pwexpbudgetalloc =
				 * genericDAO.executeSimpleQuery(expbudgetallocsbquery.toString(),entityManager)
				 * ;
				 * Object pwProjectExpBudAllocAmount = pwexpbudgetalloc.get(0);
				 * StringBuilder pwexpbudgetdeducsbquery = new StringBuilder("");
				 * pwexpbudgetdeducsbquery.
				 * append("select SUM(obj.budgetDeductedTotal) from BranchSpecifics obj WHERE obj.organization = '"
				 * +user.getOrganization().getId()+"'");
				 * List<BranchSpecifics> pwexpbudgetdeducted =
				 * genericDAO.executeSimpleQuery(expbudgetdeducsbquery.toString(),entityManager)
				 * ;
				 * Object pwprojectExpBudDeductedAmount = pwexpbudgetdeducted.get(0);
				 * Double pwProjectExpBudgetAvail = null;
				 * if(pwprojectExpBudDeductedAmount==null){
				 * if(pwProjectExpBudAllocAmount != null){
				 * pwProjectExpBudgetAvail =
				 * Double.parseDouble(decimalFormat.format(pwProjectExpBudAllocAmount));
				 * }
				 * }
				 * if(pwprojectExpBudDeductedAmount != null &&
				 * !pwprojectExpBudDeductedAmount.equals("")){
				 * pwProjectExpBudgetAvail =
				 * Double.parseDouble(decimalFormat.format(pwProjectExpBudAllocAmount))-Double.
				 * parseDouble(decimalFormat.format(pwprojectExpBudDeductedAmount));
				 * }
				 * if(projectCashExpenseThisWeek != null){
				 * pwProjectExpBudgetAvail = pwProjectExpBudgetAvail+projectCashExpenseThisWeek;
				 * }
				 * if(projectCreditExpenseThisWeek != null){
				 * pwProjectExpBudgetAvail =
				 * pwProjectExpBudgetAvail+projectCreditExpenseThisWeek;
				 * }
				 * if(previousWeekProjectCashExpense != null){
				 * pwProjectExpBudgetAvail =
				 * pwProjectExpBudgetAvail+previousWeekProjectCashExpense;
				 * }
				 * if(previousWeekProjectCreditExpense != null){
				 * pwProjectExpBudgetAvail =
				 * pwProjectExpBudgetAvail+previousWeekProjectCreditExpense;
				 * }
				 * Calendar pwnewcal = Calendar.getInstance();
				 * if(pwProjectExpBudgetAvail != null){
				 * projectdashboardrow.put("previousWeekProjectExpBudgetAvail",
				 * decimalFormat.format(pwProjectExpBudgetAvail));
				 * }else{
				 * projectdashboardrow.put("previousWeekProjectExpBudgetAvail", "");
				 * }
				 */
				// sum total of expense budget allocated for all projects previous week end
				// sum of total receivables for all projects previous week start
				/*
				 * StringBuilder pwcreditincomepaymentmadequery = new StringBuilder("");
				 * pwcreditincomepaymentmadequery.
				 * append("select SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranchOrganization = '"
				 * +user.getOrganization().getId()
				 * +"' AND obj.transactionProject IS NOT NULL AND obj.transactionPurpose = 2 and obj.transactionStatus = 'Accounted' and obj.transactionDate  between '"
				 * +previousWeekStartDate+"' and '"+previousWeekEndDate+"'");
				 * List<Transaction> pwProjectCreditIncomeCustomerNetPaymentMade =
				 * genericDAO.executeSimpleQuery(creditincomepaymentmadequery.toString(),
				 * entityManager);
				 * Object pwProjectCreditIncomeCustPaymentMade =
				 * pwProjectCreditIncomeCustomerNetPaymentMade.get(0);
				 * Double netProjectRecievablePreviousWeek = null;
				 * if(pwProjectCreditIncomeCustPaymentMade != null &&
				 * !pwProjectCreditIncomeCustPaymentMade.equals("")){
				 * if(pwprojectcreditincometxn.size()>0){
				 * Object val = pwprojectcreditincometxn.get(0);
				 * if(val != null && !val.equals("")){
				 * netProjectRecievablePreviousWeek =
				 * Double.parseDouble(decimalFormat.format(val))-Double.parseDouble(
				 * decimalFormat.format(pwProjectCreditIncomeCustPaymentMade));
				 * }
				 * }
				 * }
				 * if(pwProjectCreditIncomeCustPaymentMade==null){
				 * if(pwprojectcreditincometxn.size()>0){
				 * Object val = pwprojectcreditincometxn.get(0);
				 * if(val != null && !val.equals("")){
				 * netProjectRecievablePreviousWeek =
				 * Double.parseDouble(decimalFormat.format(val));
				 * }
				 * }
				 * }
				 * if(netProjectRecievablePreviousWeek != null){
				 * projectdashboardrow.put("netProjectRecievablePreviousWeek",
				 * decimalFormat.format(netProjectRecievablePreviousWeek));
				 * }else{
				 * projectdashboardrow.put("netProjectRecievablePreviousWeek", "");
				 * }
				 */
				// sum of total receivables for all projects previous week end
				// sum of total payables for all projects previous week start
				/*
				 * StringBuilder pwcreditexpensepaymentmadequery = new StringBuilder("");
				 * pwcreditexpensepaymentmadequery.
				 * append("select SUM(obj.vendorNetPayment) from Transaction obj WHERE obj.transactionBranchOrganization = '"
				 * +user.getOrganization().getId()
				 * +"' AND obj.transactionProject IS NOT NULL AND obj.transactionPurpose = 4 and obj.transactionStatus = 'Accounted' and obj.transactionDate  between '"
				 * +previousWeekStartDate+"' and '"+previousWeekEndDate+"'");
				 * List<Transaction> pwcreditExpenseCustomerNetPaymentMade =
				 * genericDAO.executeSimpleQuery(creditexpensepaymentmadequery.toString(),
				 * entityManager);
				 * Object pwProjectCreditExpenseCustPaymentMade =
				 * pwcreditExpenseCustomerNetPaymentMade.get(0);
				 * Double netProjectPayablePreviousWeek = null;
				 * if(pwProjectCreditExpenseCustPaymentMade != null &&
				 * !pwProjectCreditExpenseCustPaymentMade.equals("")){
				 * if(pwprojectcreditexpensetxn.size()>0){
				 * Object val = pwprojectcreditexpensetxn.get(0);
				 * if(val != null && !val.equals("")){
				 * netProjectPayablePreviousWeek =
				 * Double.parseDouble(decimalFormat.format(val))-Double.parseDouble(
				 * decimalFormat.format(pwProjectCreditExpenseCustPaymentMade));
				 * }
				 * }
				 * }
				 * if(pwProjectCreditExpenseCustPaymentMade==null){
				 * if(pwprojectcreditexpensetxn.size()>0){
				 * Object val = pwprojectcreditexpensetxn.get(0);
				 * if(val != null && !val.equals("")){
				 * netProjectPayablePreviousWeek =
				 * Double.parseDouble(decimalFormat.format(val));
				 * }
				 * }
				 * }
				 * if(netProjectPayablePreviousWeek != null){
				 * projectdashboardrow.put("netProjectPayablePreviousWeek",
				 * decimalFormat.format(netProjectPayablePreviousWeek));
				 * }else{
				 * projectdashboardrow.put("netProjectPayablePreviousWeek", "");
				 * }
				 */
				// sum of total payables for all projects previous week end
				// previous week project dash board data ends
				// newcal.add(Calendar.DAY_OF_WEEK, 1);
				Calendar newestcal = Calendar.getInstance();
				String currentDate = IdosConstants.mysqldf.format(newestcal.getTime());
				String currDate = currentDate;
				currentDate += " 23:59:59";
				// newestcal.add(Calendar.DAY_OF_WEEK, -14);
				newestcal.add(Calendar.DAY_OF_WEEK, -30);
				String forteenDaysBack = IdosConstants.mysqldf.format(newestcal.getTime());
				String forteenBackDate = forteenDaysBack;
				forteenDaysBack += " 00:00:00";

				// Connection con = MySqlConnection.getConnection();
				// Statement stmt = null;
				StringBuilder maxexpenseprojectquery = new StringBuilder("");
				maxexpenseprojectquery.append(
						"SELECT TRANSACTION_PROJECT, SUM(NET_AMOUNT) AS NET_AMOUNT FROM TRANSACTION WHERE TRANSACTION_BRANCH_ORGANIZATION = '"
								+ user.getOrganization().getId()
								+ "' AND TRANSACTION_PROJECT IS NOT NULL AND TRANSACTION_STATUS = 'Accounted' AND (TRANSACTION_PURPOSE = 3 OR TRANSACTION_PURPOSE = 4 OR TRANSACTION_PURPOSE = 11) AND TRANSACTION_ACTIONDATE BETWEEN  '"
								+ forteenBackDate + "' and '" + currDate
								+ "' GROUP BY TRANSACTION_BRANCH ORDER BY NET_AMOUNT DESC LIMIT 1");
				// stmt = (Statement) con.createStatement();
				// ResultSet rs = stmt.executeQuery(maxexpenseprojectquery.toString());

				String maxExpenseProject = "";
				Query query = entityManager.createNativeQuery(maxexpenseprojectquery.toString());
				List<Object[]> resultList = query.getResultList();
				// while(rs.next()){
				for (Object[] resultObj : resultList) {
					// Integer projectId = rs.getInt("TRANSACTION_PROJECT");
					// Double maxNetAmount = rs.getDouble("NET_AMOUNT");
					Integer projectId = (Integer) resultObj[0];
					Double maxNetAmount = (Double) resultObj[1];
					Project maxTxnProjectEntity = null;
					if (projectId != null && projectId > 0) {
						maxTxnProjectEntity = Project.findById(projectId.longValue());
					}
					if (maxNetAmount != null && maxNetAmount > 0.0) {
						maxExpenseProject = maxTxnProjectEntity.getName() + ":" + String.valueOf(maxNetAmount);
					}
				}
				projectdashboardrow.put("maxExpenseProject", maxExpenseProject);
				Statement stmt1 = null;
				StringBuilder maxincomeprojectquery = new StringBuilder("");
				maxincomeprojectquery.append(
						"SELECT TRANSACTION_PROJECT, SUM(NET_AMOUNT) AS NET_AMOUNT FROM TRANSACTION WHERE TRANSACTION_BRANCH_ORGANIZATION = '"
								+ user.getOrganization().getId()
								+ "' AND TRANSACTION_PROJECT IS NOT NULL AND TRANSACTION_STATUS = 'Accounted' AND (TRANSACTION_PURPOSE = 1 OR TRANSACTION_PURPOSE = 2) AND TRANSACTION_ACTIONDATE BETWEEN  '"
								+ forteenBackDate + "' and '" + currDate
								+ "' GROUP BY TRANSACTION_BRANCH ORDER BY NET_AMOUNT DESC LIMIT 1");
				// stmt1 = (Statement) con.createStatement();
				// ResultSet rs1 = stmt1.executeQuery(maxincomeprojectquery.toString());
				String maxIncomeProject = "";
				query = entityManager.createNativeQuery(maxincomeprojectquery.toString());
				// query.setMaxResults(1);
				List<Object[]> projectList = query.getResultList();
				// while(rs.next()){
				for (Object[] resultObj : projectList) {
					// Integer projectId = rs1.getInt("TRANSACTION_PROJECT");
					// Double maxNetAmount = rs1.getDouble("NET_AMOUNT");
					Integer projectId = (Integer) resultObj[0];
					Double maxNetAmount = (Double) resultObj[1];
					Project maxTxnProjectEntity = null;
					if (projectId != null && projectId > 0) {
						maxTxnProjectEntity = Project.findById(projectId.longValue());
					}
					if (maxNetAmount != null && maxNetAmount > 0.0) {
						maxIncomeProject = maxTxnProjectEntity.getName() + ":" + String.valueOf(maxNetAmount);
					}
				}
				projectdashboardrow.put("maxIncomeProject", maxIncomeProject);
				projectdashboardan.add(projectdashboardrow);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
		}
		return results;
	}

	/**
	 * Now it is for Previous Month and this Month, so if it is 24thJune 2015 then
	 * previous month is May month and this month is till 24thJune2015
	 * 
	 * @param user
	 * @param json
	 * @param entityManager
	 * @return
	 */
	@Override
	public ObjectNode getDashboardFinancial(final Users user, final JsonNode json, final EntityManager entityManager) {
		log.log(Level.FINE, "============ Start");
		String currDashboardFromDate = json.findValue("currDashboardFromDate") != null
				? json.findValue("currDashboardFromDate").asText()
				: null;
		String currDashboardToDate = json.findValue("currDashboardToDate") != null
				? json.findValue("currDashboardToDate").asText()
				: null;
		String prevDashboardFromDate = json.findValue("prevDashboardFromDate") != null
				? json.findValue("prevDashboardFromDate").asText()
				: null;
		String prevDashboardToDate = json.findValue("prevDashboardToDate") != null
				? json.findValue("prevDashboardToDate").asText()
				: null;

		ObjectNode results = Json.newObject();
		ArrayNode dashboardan = results.putArray("dashBoardData");
		ArrayNode lastForteenDaysCustomersan = results.putArray("lastForteenDaysCustomerData");
		ArrayNode lastForteenDaysBranchan = results.putArray("lastForteenDaysBranchData"); // though names are
																							// lastFourteen, it is
																							// actually for 30days
		ArrayNode lastForteenDaysVendorsan = results.putArray("lastForteenDaysVendorsData");
		ArrayNode lastForteenDaysUsersan = results.putArray("lastForteenDaysUsersData");
		ArrayNode lastForteenDaysPendingApprovalan = results.putArray("lastForteenDaysPendingApprovalData");
		ArrayNode lastForteenDaysTxnExceedingBudgetAHWan = results.putArray("lastForteenDaysTxnExceedingBudgetAWHData");
		ArrayNode lastForteenDaysTxnExceedingBudgetan = results.putArray("lastForteenDaysTxnExceedingBudgetData");
		ArrayNode lastForteenDaysTxnKlNotFollwedAHWan = results.putArray("lastForteenDaysTxnKlNotFollwedAWHData");
		ArrayNode lastForteenDaysTxnKlNotFollwedan = results.putArray("lastForteenDaysTxnKlNotFollwedData");
		ObjectNode dashboardrow = Json.newObject();
		Date startDate = null;
		Date endDate = null;
		try {
			String currentWeekStartDate = null, currentWeekEndDate = null;
			if (currDashboardFromDate != null && !"".equals(currDashboardFromDate) && currDashboardToDate != null
					&& !"".equals(currDashboardToDate)) {
				currentWeekStartDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(currDashboardFromDate));
				currentWeekEndDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(currDashboardToDate));

				startDate = IdosConstants.IDOSDF.parse(currDashboardFromDate);
				endDate = IdosConstants.IDOSDF.parse(currDashboardToDate);
			} else {
				// Now instead of this week, it is for this month
				Calendar cal = Calendar.getInstance();
				// cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				currentWeekStartDate = IdosConstants.mysqldf.format(cal.getTime());
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				startDate = cal.getTime();
				// cal.add(Calendar.DAY_OF_WEEK, 6);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				currentWeekEndDate = IdosConstants.mysqldf.format(cal.getTime());
				endDate = cal.getTime();
				// cal.add(Calendar.DAY_OF_WEEK, 1);
			}
			// get live data of current Month
			// get Journal entry data for Cash/credit expense and income
			Map provisionEntries = new HashMap();
			Map allSpecifcsAmtData = new HashMap();
			Map vendorPayablesData = new HashMap();
			Map custReceivablesData = new HashMap();
			ProvisionJournalEntryService jourObj = new ProvisionJournalEntryServiceImpl();
			ObjectNode journalResult = jourObj.getDashboardProvisionEntriesDataBranchWise(currentWeekStartDate,
					currentWeekEndDate, provisionEntries, allSpecifcsAmtData, vendorPayablesData, custReceivablesData,
					user, entityManager);
			Double creditExpenseThisWeek = new Double(provisionEntries.get("totalCreditExpense").toString());
			Double cashExpenseThisWeek = new Double(provisionEntries.get("totalCashExpense").toString());
			Double cashIncome = new Double(provisionEntries.get("totalCashIncome").toString());
			Double creditIncome = new Double(provisionEntries.get("totalCreditIncome").toString());
			Double accountReceivables = new Double(provisionEntries.get("totalCustReceivables").toString());
			Double accountPayables = new Double(provisionEntries.get("totalVendPayables").toString());

			// sum total cash expense this month
			StringBuilder sbquery = new StringBuilder("");
			sbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
					+ user.getOrganization().getId()
					+ "' AND (obj.transactionPurpose = 3 or obj.transactionPurpose = 11) and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
					+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
			List<Transaction> cashexpensetxn = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
			if (cashexpensetxn.size() > 0) {
				Object val = cashexpensetxn.get(0);
				if (val != null) {
					cashExpenseThisWeek = cashExpenseThisWeek + IdosUtil.convertStringToDouble(String.valueOf(val));
				}
			}
			// Expenses due to claims transaction ident_data_Valid = 23,24,25,26 or
			// tran_purpose = 16,18,19 cash payment when settling travel advance/settle
			// expense reimbursement
			sbquery.delete(0, sbquery.length());
			sbquery.append(
					"select SUM(obj.newAmount) from ClaimTransaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' and obj.transactionPurpose in (16,18,19) and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.receiptDetailType in (1,0) and obj.transactionDate  between '"
							+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
			List<ClaimTransaction> clmTxnList = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
			if (!clmTxnList.isEmpty() && clmTxnList.size() > 0) {
				Object val = clmTxnList.get(0);
				if (val != null) {
					cashExpenseThisWeek += IdosUtil.convertStringToDouble(String.valueOf(val));
				}
			}
			dashboardrow.put("cashExpense", IdosConstants.decimalFormat.format(cashExpenseThisWeek));
			// sum total cash sell this month
			StringBuilder cssbquery = new StringBuilder("");
			cssbquery
					.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' AND ((obj.transactionPurpose = 1) OR (obj.transactionPurpose = 2 AND obj.performaInvoice = true AND obj.paymentStatus  =  'PAID' )) and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
							+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
			List<Transaction> cashselltxn = genericDAO.executeSimpleQuery(cssbquery.toString(), entityManager);
			if (cashselltxn.size() > 0) {
				Object val = cashselltxn.get(0);
				if (val != null) {
					cashIncome = cashIncome + IdosUtil.convertStringToDouble(String.valueOf(val));
				}
			}
			dashboardrow.put("cashIncome", IdosConstants.decimalFormat.format(cashIncome));
			// sum total credit expense this month
			StringBuilder cdtsbquery = new StringBuilder("");
			cdtsbquery
					.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' AND obj.transactionPurpose = 4 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
							+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
			List<Transaction> creditexpensetxn = genericDAO.executeSimpleQuery(cdtsbquery.toString(), entityManager);
			if (creditexpensetxn.size() > 0) {
				Object val = creditexpensetxn.get(0);
				if (val != null) {
					creditExpenseThisWeek = creditExpenseThisWeek + IdosUtil.convertStringToDouble(val.toString());
				}
			}
			// Purchase return should be subtracted from totalexpense
			cdtsbquery = new StringBuilder("");
			cdtsbquery
					.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' AND obj.transactionPurpose = 13 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.presentStatus=1 and obj.transactionDate  between '"
							+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
			creditexpensetxn = genericDAO.executeSimpleQuery(cdtsbquery.toString(), entityManager);
			if (creditexpensetxn.size() > 0) {
				Object val = creditexpensetxn.get(0);
				if (val != null) {
					creditExpenseThisWeek = creditExpenseThisWeek - new Double(val.toString());
				}
			}
			// Expenses due to claims transaction ident_data_Valid = 23,24,25,26 or
			// tran_purpose = 16,18,19 credit payment when settling travel advance/settle
			// expense reimbursement
			sbquery.delete(0, sbquery.length());
			sbquery.append(
					"select SUM(obj.newAmount) from ClaimTransaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' and obj.transactionPurpose in (16,18,19) and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.receiptDetailType  =  2 and obj.transactionDate  between '"
							+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
			clmTxnList = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
			if (!clmTxnList.isEmpty() && clmTxnList.size() > 0) {
				Object val = clmTxnList.get(0);
				if (val != null) {
					creditExpenseThisWeek += IdosUtil.convertStringToDouble(String.valueOf(val));
				}
			}
			dashboardrow.put("creditExpense", IdosConstants.decimalFormat.format(creditExpenseThisWeek));

			// sum total credit income this month
			StringBuilder cdtincsbquery = new StringBuilder("");
			cdtincsbquery
					.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' AND obj.transactionPurpose = 2 AND (obj.performaInvoice != true or performa_invoice is null )and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
							+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
			List<Transaction> creditincometxn = genericDAO.executeSimpleQuery(cdtincsbquery.toString(), entityManager);
			if (creditincometxn.size() > 0) {
				Object val = creditincometxn.get(0);
				if (val != null) {
					creditIncome = creditIncome + IdosUtil.convertStringToDouble(val.toString());
				}
			}
			// sales retrun should be subtracted from totalSales
			cdtincsbquery = new StringBuilder("");
			cdtincsbquery
					.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' AND obj.transactionPurpose = 12 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
							+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
			creditincometxn = genericDAO.executeSimpleQuery(cdtincsbquery.toString(), entityManager);
			if (creditincometxn.size() > 0) {
				Object val = creditincometxn.get(0);
				if (val != null) {
					creditIncome = creditIncome - IdosUtil.convertStringToDouble(val.toString());
				}
			}
			dashboardrow.put("creditIncome", IdosConstants.decimalFormat.format(creditIncome));

			// sum total proforma Invoice income this this
			String[] arr = DateUtil.getFinancialDate(user);
			String finStartDate = arr[0];
			String finEndDate = arr[1];
			Double netAmount = 0.0;
			Double customerNetPayment = 0.0;
			StringBuilder pinvincsbquery = new StringBuilder(
					"select SUM(obj.netAmount), SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' AND obj.transactionPurpose = 28  and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
							+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");

			if (log.isLoggable(Level.FINE)) {
				log.log(Level.INFO, "Proforma HQL: " + pinvincsbquery);
			}

			List<Object[]> peformaInvincometxn = entityManager.createQuery(pinvincsbquery.toString()).getResultList();
			// List<Transaction> peformaInvincometxn =
			// genericDAO.executeSimpleQuery(pinvincsbquery.toString(),entityManager);
			for (Object[] custData : peformaInvincometxn) {
				if (custData[0] != null) {
					netAmount = IdosUtil.convertStringToDouble(String.valueOf(custData[0]));
				}
				/*
				 * if(custData[1] != null){
				 * customerNetPayment = Double.parseDouble(String.valueOf(custData[1]));
				 * }
				 * netAmount = netAmount - customerNetPayment;
				 */
			}
			dashboardrow.put("expBudgetAvail", IdosConstants.decimalFormat.format(netAmount));
			/*
			 * if(peformaInvincometxn.size()>0){
			 * Object val = peformaInvincometxn.get(0);
			 * if(val != null){
			 * dashboardrow.put("expBudgetAvail", decimalFormat.format(val));
			 * }else{
			 * dashboardrow.put("expBudgetAvail", "");
			 * }
			 * }else{
			 * dashboardrow.put("expBudgetAvail", "");
			 * }
			 */

			// sum total quotation Invoice income this this
			netAmount = 0.0;
			StringBuilder quotationHql = new StringBuilder(
					"select SUM(obj.netAmount), SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' AND obj.transactionPurpose = 27  and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
							+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.INFO, "Quotation HQL: " + quotationHql);
			}
			List<Object[]> quotationNetAmountList = entityManager.createQuery(quotationHql.toString()).getResultList();
			for (Object[] custData : quotationNetAmountList) {
				if (custData[0] != null) {
					netAmount = IdosUtil.convertStringToDouble(String.valueOf(custData[0]));
				}
			}
			dashboardrow.put("quotationAvail", IdosConstants.decimalFormat.format(netAmount));

			// sum total of expense budget allocated
			/*
			 * StringBuilder expbudgetallocsbquery = new StringBuilder("");
			 * expbudgetallocsbquery.
			 * append("select SUM(obj.budgetTotal) from BranchSpecifics obj WHERE obj.organization = '"
			 * +user.getOrganization().getId()+"'");
			 * List<BranchSpecifics> expbudgetalloc =
			 * genericDAO.executeSimpleQuery(expbudgetallocsbquery.toString(),entityManager)
			 * ;
			 * Object expBudAllocAmount = expbudgetalloc.get(0);
			 * 
			 * StringBuilder expbudgetdeducsbquery = new StringBuilder("");
			 * expbudgetdeducsbquery.
			 * append("select SUM(obj.budgetDeductedTotal) from BranchSpecifics obj WHERE obj.organization = '"
			 * +user.getOrganization().getId()+"'");
			 * List<BranchSpecifics> expbudgetdeducted =
			 * genericDAO.executeSimpleQuery(expbudgetdeducsbquery.toString(),entityManager)
			 * ;
			 * Object expBudDeductedAmount = expbudgetdeducted.get(0);
			 * Double expBudgetAvail = null;
			 * if(expBudDeductedAmount==null){
			 * if(expBudAllocAmount != null){
			 * expBudgetAvail = Double.parseDouble(decimalFormat.format(expBudAllocAmount));
			 * }
			 * }
			 * if(expBudDeductedAmount != null && !expBudDeductedAmount.equals("") &&
			 * (expBudAllocAmount != null) && (!"".equals(expBudAllocAmount))){
			 * expBudgetAvail =
			 * Double.parseDouble(decimalFormat.format(expBudAllocAmount))-Double.
			 * parseDouble(decimalFormat.format(expBudDeductedAmount));
			 * }
			 * if((cashExpenseThisWeek != null) && (expBudgetAvail != null)){
			 * expBudgetAvail = expBudgetAvail+cashExpenseThisWeek;
			 * }
			 * if((creditExpenseThisWeek != null) && (expBudgetAvail != null)){
			 * expBudgetAvail = expBudgetAvail+creditExpenseThisWeek;
			 * }
			 * if(expBudgetAvail != null){
			 * dashboardrow.put("expBudgetAvail", decimalFormat.format(expBudgetAvail));
			 * }else{
			 * dashboardrow.put("expBudgetAvail", "");
			 * }
			 */

			// sum of total receivables this Year from financial start date till todate
			// selected on dashboard else todays date
			ObjectNode row = totalReceivablePayables(currDashboardToDate, user, entityManager);
			if (row != null && row.get("accountsReceivables") != null) {
				accountReceivables = accountReceivables + row.get("accountsReceivables").asDouble();
			}

			double totalCustOpeningBal = custVendorOpeningBalanceTotal(IdosConstants.CUSTOMER, user, entityManager);
			dashboardrow.put("netRecievableThisWeek",
					IdosConstants.decimalFormat.format(accountReceivables + totalCustOpeningBal));
			// Total payables
			if (row != null && row.get("accountsPayables") != null) {
				accountPayables = accountPayables + row.get("accountsPayables").asDouble();
			}
			double totalVendOpeningBal = custVendorOpeningBalanceTotal(IdosConstants.VENDOR, user, entityManager);
			dashboardrow.put("netPayableThisWeek",
					IdosConstants.decimalFormat.format(accountPayables + totalVendOpeningBal));
			/*
			 * StringBuilder creditincomepaymentmadequery = new StringBuilder("");
			 * creditincomepaymentmadequery.
			 * append("select SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranchOrganization = '"
			 * +user.getOrganization().getId()
			 * +"' AND obj.transactionPurpose = 2 and obj.transactionStatus = 'Accounted' and obj.transactionDate  between '"
			 * +currentWeekStartDate+"' and '"+currentWeekEndDate+"'");
			 * List<Transaction> creditIncomeCustomerNetPaymentMade =
			 * genericDAO.executeSimpleQuery(creditincomepaymentmadequery.toString(),
			 * entityManager);
			 * Object creditIncomeCustPaymentMade =
			 * creditIncomeCustomerNetPaymentMade.get(0);
			 * Double netRecievableThisWeek = null;
			 * if(creditIncomeCustPaymentMade != null &&
			 * !creditIncomeCustPaymentMade.equals("")){
			 * if(creditincometxn.size()>0){
			 * Object val = creditincometxn.get(0);
			 * if(val != null && !val.equals("")){
			 * netRecievableThisWeek =
			 * Double.parseDouble(decimalFormat.format(val))-Double.parseDouble(
			 * decimalFormat.format(creditIncomeCustPaymentMade));
			 * }
			 * }
			 * }
			 * if(creditIncomeCustPaymentMade==null){
			 * if(creditincometxn.size()>0){
			 * Object val = creditincometxn.get(0);
			 * if(val != null && !val.equals("")){
			 * netRecievableThisWeek = Double.parseDouble(decimalFormat.format(val));
			 * }
			 * }
			 * }
			 * if(netRecievableThisWeek != null){
			 * dashboardrow.put("netRecievableThisWeek",
			 * decimalFormat.format(netRecievableThisWeek));
			 * }else{
			 * dashboardrow.put("netRecievableThisWeek", "");
			 * }
			 */
			// sum of total payables this month
			/*
			 * StringBuilder creditexpensepaymentmadequery = new StringBuilder("");
			 * creditexpensepaymentmadequery.
			 * append("select SUM(obj.vendorNetPayment) from Transaction obj WHERE obj.transactionBranchOrganization = '"
			 * +user.getOrganization().getId()
			 * +"' AND obj.transactionPurpose = 4 and obj.transactionStatus = 'Accounted' and obj.transactionDate  between '"
			 * +currentWeekStartDate+"' and '"+currentWeekEndDate+"'");
			 * List<Transaction> creditExpenseCustomerNetPaymentMade =
			 * genericDAO.executeSimpleQuery(creditexpensepaymentmadequery.toString(),
			 * entityManager);
			 * Object creditExpenseCustPaymentMade =
			 * creditExpenseCustomerNetPaymentMade.get(0);
			 * Double netPayableThisWeek = null;
			 * if(creditExpenseCustPaymentMade != null &&
			 * !creditExpenseCustPaymentMade.equals("")){
			 * if(creditexpensetxn.size()>0){
			 * Object val = creditexpensetxn.get(0);
			 * if(val != null && !val.equals("")){
			 * netPayableThisWeek =
			 * Double.parseDouble(decimalFormat.format(val))-Double.parseDouble(
			 * decimalFormat.format(creditExpenseCustPaymentMade));
			 * }
			 * }
			 * }
			 * if(creditExpenseCustPaymentMade==null){
			 * if(creditexpensetxn.size()>0){
			 * Object val = creditexpensetxn.get(0);
			 * if(val != null && !val.equals("")){
			 * netPayableThisWeek = Double.parseDouble(decimalFormat.format(val));
			 * }
			 * }
			 * }
			 * if(netPayableThisWeek != null){
			 * dashboardrow.put("netPayableThisWeek",
			 * decimalFormat.format(netPayableThisWeek));
			 * }else{
			 * dashboardrow.put("netPayableThisWeek", "");
			 * }
			 */
			// this month dash board data ends
			/*********************************************************************************************************/
			// start previous month dash board datas, it is MONTH not week, changed by
			// Manali on 30thJune2015
			String previousWeekStartDate = null, previousWeekEndDate = null;
			if (prevDashboardFromDate != null && !"".equals(prevDashboardFromDate) && prevDashboardToDate != null
					&& !"".equals(prevDashboardToDate)) {
				previousWeekStartDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(prevDashboardFromDate));
				previousWeekEndDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(prevDashboardToDate));
			} else {
				Calendar newcal = Calendar.getInstance();
				newcal.add(Calendar.MONTH, -1);
				newcal.set(Calendar.DAY_OF_MONTH, newcal.getActualMinimum(Calendar.DAY_OF_MONTH));
				// newcal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				// newcal.add(Calendar.DAY_OF_WEEK, -7);
				previousWeekStartDate = IdosConstants.mysqldf.format(newcal.getTime());
				// newcal.add(Calendar.DAY_OF_WEEK, 6);
				newcal.set(Calendar.DAY_OF_MONTH, newcal.getActualMaximum(Calendar.DAY_OF_MONTH));
				previousWeekEndDate = IdosConstants.mysqldf.format(newcal.getTime());
			}
			Double previousWeekCashExpense = 0.0;
			Double previousWeekCreditExpense = 0.0;
			// sum total cash expense previous week
			StringBuilder pwsbquery = new StringBuilder("");
			pwsbquery
					.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' AND (obj.transactionPurpose = 3 or obj.transactionPurpose = 11) and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
							+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
			List<Transaction> pwcashexpensetxn = genericDAO.executeSimpleQuery(pwsbquery.toString(), entityManager);
			if (pwcashexpensetxn.size() > 0) {
				Object val = pwcashexpensetxn.get(0);
				if (val != null) {
					previousWeekCashExpense = previousWeekCashExpense
							+ IdosUtil.convertStringToDouble(String.valueOf(val));
				}
			}
			// Expenses due to claims transaction ident_data_Valid = 23,24,25,26 or
			// tran_purpose = 16,18,19 credit payment when settling travel advance/settle
			// expense reimbursement
			sbquery.delete(0, sbquery.length());
			sbquery.append(
					"select SUM(obj.newAmount) from ClaimTransaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' and obj.transactionPurpose in (16,18,19) and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.receiptDetailType  =  1 and obj.transactionDate  between '"
							+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
			clmTxnList = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
			if (!clmTxnList.isEmpty() && clmTxnList.size() > 0) {
				Object val = clmTxnList.get(0);
				if (val != null) {
					previousWeekCashExpense += IdosUtil.convertStringToDouble(String.valueOf(val));
				}
			}
			dashboardrow.put("previousWeekcashExpense", IdosConstants.decimalFormat.format(previousWeekCashExpense));
			// sum total cash sell previous week
			StringBuilder pwcssbquery = new StringBuilder("");
			pwcssbquery
					.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' AND ((obj.transactionPurpose = 1) OR (obj.transactionPurpose = 2 AND obj.performaInvoice = true AND obj.paymentStatus  =  'PAID' )) and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
							+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
			List<Transaction> pwcashselltxn = genericDAO.executeSimpleQuery(pwcssbquery.toString(), entityManager);
			if (pwcashselltxn.size() > 0) {
				Object val = pwcashselltxn.get(0);
				if (val != null) {
					dashboardrow.put("previousWeekcashIncome", IdosConstants.decimalFormat.format(val));
				} else {
					dashboardrow.put("previousWeekcashIncome", "");
				}
			} else {
				dashboardrow.put("previousWeekcashIncome", "");
			}
			// sum total credit expense previous week
			StringBuilder pwcdtsbquery = new StringBuilder("");
			pwcdtsbquery
					.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' AND obj.transactionPurpose = 4 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
							+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
			List<Transaction> pwcreditexpensetxn = genericDAO.executeSimpleQuery(pwcdtsbquery.toString(),
					entityManager);
			if (pwcreditexpensetxn.size() > 0) {
				Object val = pwcreditexpensetxn.get(0);
				if (val != null) {
					previousWeekCreditExpense = previousWeekCreditExpense
							+ IdosUtil.convertStringToDouble(String.valueOf(val));
				}
			}
			// Expenses due to claims transaction ident_data_Valid = 23,24,25,26 or
			// tran_purpose = 16,18,19 credit payment when settling travel advance/settle
			// expense reimbursement
			sbquery.delete(0, sbquery.length());
			sbquery.append(
					"select SUM(obj.newAmount) from ClaimTransaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' and obj.transactionPurpose in (16,18,19) and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.receiptDetailType  =  2 and obj.transactionDate  between '"
							+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
			clmTxnList = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
			if (!clmTxnList.isEmpty() && clmTxnList.size() > 0) {
				Object val = clmTxnList.get(0);
				if (val != null) {
					previousWeekCreditExpense += IdosUtil.convertStringToDouble(String.valueOf(val));
				}
			}
			dashboardrow.put("previousWeekcreditExpense",
					IdosConstants.decimalFormat.format(previousWeekCreditExpense));
			// sum total credit income previous week
			StringBuilder pwcdtincsbquery = new StringBuilder("");
			pwcdtincsbquery
					.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' AND obj.transactionPurpose = 2 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 AND (obj.performaInvoice != true or performa_invoice is null) and obj.transactionDate  between '"
							+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
			List<Transaction> pwcreditincometxn = genericDAO.executeSimpleQuery(pwcdtincsbquery.toString(),
					entityManager);
			if (pwcreditincometxn.size() > 0) {
				Object val = pwcreditincometxn.get(0);
				if (val != null) {
					dashboardrow.put("previousWekcreditIncome", IdosConstants.decimalFormat.format(val));
				} else {
					dashboardrow.put("previousWekcreditIncome", "");
				}
			} else {
				dashboardrow.put("previousWekcreditIncome", "");
			}
			// sum total performa Invoice income previous month
			StringBuilder pwpinvincsbquery = new StringBuilder(
					"select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' AND obj.transactionPurpose = 28 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
							+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.INFO, "Prev Proforma HQL: " + pwpinvincsbquery);
			}
			List<Transaction> pwpeformaInvincometxn = genericDAO.executeSimpleQuery(pwpinvincsbquery.toString(),
					entityManager);
			if (pwpeformaInvincometxn.size() > 0) {
				Object val = pwpeformaInvincometxn.get(0);
				if (val != null) {
					dashboardrow.put("previousWeekexpBudgetAvail", IdosConstants.decimalFormat.format(val));
				} else {
					dashboardrow.put("previousWeekexpBudgetAvail", "");
				}
			} else {
				dashboardrow.put("previousWeekexpBudgetAvail", "");
			}

			// sum total quotation Invoice income this this
			netAmount = 0.0;
			StringBuilder quotationHqlPrev = new StringBuilder(
					"select SUM(obj.netAmount), SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' AND obj.transactionPurpose = 27  and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
							+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.INFO, "Pre Quotation HQL: " + quotationHqlPrev);
			}
			List<Object[]> quotationNetAmountListPrev = entityManager.createQuery(quotationHqlPrev.toString())
					.getResultList();
			for (Object[] custData : quotationNetAmountListPrev) {
				if (custData[0] != null) {
					netAmount = IdosUtil.convertStringToDouble(String.valueOf(custData[0]));
				}
			}
			dashboardrow.put("previousWeekQuotationAvail", IdosConstants.decimalFormat.format(netAmount));

			// sum total of expense budget allocated
			/*
			 * StringBuilder pwexpbudgetallocsbquery = new StringBuilder("");
			 * pwexpbudgetallocsbquery.
			 * append("select SUM(obj.budgetTotal) from BranchSpecifics obj WHERE obj.organization = '"
			 * +user.getOrganization().getId()+"'");
			 * List<BranchSpecifics> pwexpbudgetalloc =
			 * genericDAO.executeSimpleQuery(expbudgetallocsbquery.toString(),entityManager)
			 * ;
			 * Object pwexpBudAllocAmount = pwexpbudgetalloc.get(0);
			 * StringBuilder pwexpbudgetdeducsbquery = new StringBuilder("");
			 * pwexpbudgetdeducsbquery.
			 * append("select SUM(obj.budgetDeductedTotal) from BranchSpecifics obj WHERE obj.organization = '"
			 * +user.getOrganization().getId()+"'");
			 * List<BranchSpecifics> pwexpbudgetdeducted =
			 * genericDAO.executeSimpleQuery(expbudgetdeducsbquery.toString(),entityManager)
			 * ;
			 * Object pwexpBudDeductedAmount = pwexpbudgetdeducted.get(0);
			 * Double pwexpBudgetAvail = null;
			 * if(pwexpBudDeductedAmount==null){
			 * if(pwexpBudAllocAmount != null){
			 * pwexpBudgetAvail =
			 * Double.parseDouble(decimalFormat.format(pwexpBudAllocAmount));
			 * }
			 * }
			 * if(pwexpBudDeductedAmount != null && !pwexpBudDeductedAmount.equals("") &&
			 * (pwexpBudgetAvail != null)){
			 * pwexpBudgetAvail = pwexpBudgetAvail-Double.parseDouble(decimalFormat.format(
			 * pwexpBudDeductedAmount));
			 * }
			 * if((cashExpenseThisWeek != null ) && (pwexpBudgetAvail != null)){
			 * pwexpBudgetAvail = pwexpBudgetAvail+cashExpenseThisWeek;
			 * }
			 * if((creditExpenseThisWeek != null) && (pwexpBudgetAvail != null)){
			 * pwexpBudgetAvail = pwexpBudgetAvail+creditExpenseThisWeek;
			 * }
			 * if((previousWeekCashExpense != null) && (pwexpBudgetAvail != null)){
			 * pwexpBudgetAvail = pwexpBudgetAvail+previousWeekCashExpense;
			 * }
			 * if((previousWeekCreditExpense != null) && (pwexpBudgetAvail != null)){
			 * pwexpBudgetAvail = pwexpBudgetAvail+previousWeekCreditExpense;
			 * }
			 * Calendar pwnewcal = Calendar.getInstance();
			 * if(pwexpBudgetAvail != null){
			 * dashboardrow.put("previousWeekexpBudgetAvail",
			 * decimalFormat.format(pwexpBudgetAvail));
			 * }else{
			 * dashboardrow.put("previousWeekexpBudgetAvail", "");
			 * }
			 */
			// sum of total receivables previous week
			dashboardrow.put("netRecievablePreviousWeek", row.get("accountsReceivablesOverdues"));
			dashboardrow.put("netPayablePreviousWeek", row.get("accountsPayablesOverdues"));
			/*
			 * StringBuilder pwcreditincomepaymentmadequery = new StringBuilder("");
			 * pwcreditincomepaymentmadequery.
			 * append("select SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranchOrganization = '"
			 * +user.getOrganization().getId()
			 * +"' AND obj.transactionPurpose = 2 and obj.transactionStatus = 'Accounted' and obj.transactionDate  between '"
			 * +previousWeekStartDate+"' and '"+previousWeekEndDate+"'");
			 * List<Transaction> pwcreditIncomeCustomerNetPaymentMade =
			 * genericDAO.executeSimpleQuery(pwcreditincomepaymentmadequery.toString(),
			 * entityManager);
			 * Object pwcreditIncomeCustPaymentMade =
			 * pwcreditIncomeCustomerNetPaymentMade.get(0);
			 * Double netRecievablePreviousWeek = null;
			 * if(pwcreditIncomeCustPaymentMade != null &&
			 * !pwcreditIncomeCustPaymentMade.equals("")){
			 * if(pwcreditincometxn.size()>0){
			 * Object val = pwcreditincometxn.get(0);
			 * if(val != null && !val.equals("")){
			 * netRecievablePreviousWeek =
			 * Double.parseDouble(decimalFormat.format(val))-Double.parseDouble(
			 * decimalFormat.format(pwcreditIncomeCustPaymentMade));
			 * }
			 * }
			 * }
			 * if(pwcreditIncomeCustPaymentMade==null){
			 * if(pwcreditincometxn.size()>0){
			 * Object val = pwcreditincometxn.get(0);
			 * if(val != null && !val.equals("")){
			 * netRecievablePreviousWeek = Double.parseDouble(decimalFormat.format(val));
			 * }
			 * }
			 * }
			 * if(netRecievablePreviousWeek != null){
			 * dashboardrow.put("netRecievablePreviousWeek",
			 * decimalFormat.format(netRecievablePreviousWeek));
			 * }else{
			 * dashboardrow.put("netRecievablePreviousWeek", "");
			 * }
			 */
			// sum of total payables previous week
			/*
			 * StringBuilder pwcreditexpensepaymentmadequery = new StringBuilder("");
			 * pwcreditexpensepaymentmadequery.
			 * append("select SUM(obj.vendorNetPayment) from Transaction obj WHERE obj.transactionBranchOrganization = '"
			 * +user.getOrganization().getId()
			 * +"' AND obj.transactionPurpose = 4 and obj.transactionStatus = 'Accounted' and obj.transactionDate  between '"
			 * +previousWeekStartDate+"' and '"+previousWeekEndDate+"'");
			 * List<Transaction> pwcreditExpenseCustomerNetPaymentMade =
			 * genericDAO.executeSimpleQuery(pwcreditexpensepaymentmadequery.toString(),
			 * entityManager);
			 * Object pwcreditExpenseCustPaymentMade =
			 * pwcreditExpenseCustomerNetPaymentMade.get(0);
			 * Double netPayablePreviousWeek = null;
			 * if(pwcreditExpenseCustPaymentMade != null &&
			 * !pwcreditExpenseCustPaymentMade.equals("")){
			 * if(pwcreditexpensetxn.size()>0){
			 * Object val = pwcreditexpensetxn.get(0);
			 * if(val != null && !val.equals("")){
			 * netPayablePreviousWeek =
			 * Double.parseDouble(decimalFormat.format(val))-Double.parseDouble(
			 * decimalFormat.format(pwcreditExpenseCustPaymentMade));
			 * }
			 * }
			 * }
			 * if(pwcreditExpenseCustPaymentMade==null){
			 * if(pwcreditexpensetxn.size()>0){
			 * Object val = pwcreditexpensetxn.get(0);
			 * if(val != null && !val.equals("")){
			 * netPayablePreviousWeek = Double.parseDouble(decimalFormat.format(val));
			 * }
			 * }
			 * }
			 * if(netPayablePreviousWeek != null){
			 * dashboardrow.put("netPayablePreviousWeek",
			 * decimalFormat.format(netPayablePreviousWeek));
			 * }else{
			 * dashboardrow.put("netPayablePreviousWeek", "");
			 * }
			 */
			// previous week dash board data ends
			// newcal.add(Calendar.DAY_OF_WEEK, 1);
			Calendar newestcal = Calendar.getInstance();
			String currentDate = IdosConstants.mysqldf.format(newestcal.getTime());
			String currDate = currentDate;
			currentDate += " 23:59:59";
			// newestcal.add(Calendar.DAY_OF_WEEK, -14);
			newestcal.add(Calendar.DAY_OF_WEEK, -30); // now instead of last 14days it is last 30days data
			String forteenDaysBack = IdosConstants.mysqldf.format(newestcal.getTime());
			String forteenBackDate = forteenDaysBack;
			forteenDaysBack += " 00:00:00";
			StringBuilder fortenDaysBackCustomer = new StringBuilder();
			fortenDaysBackCustomer.append("select obj from Vendor obj WHERE obj.type = 2 and obj.organization = '"
					+ user.getOrganization().getId() + "' and obj.presentStatus=1 and (obj.createdAt  between '"
					+ forteenDaysBack + "' and '" + currentDate + "' or obj.modifiedAt between '" + forteenDaysBack
					+ "' and '" + currentDate + "')");
			List<Vendor> lastForteenDaysAddedModifiedCustomers = genericDAO
					.executeSimpleQueryWithLimit(fortenDaysBackCustomer.toString(), entityManager, 5);
			for (Vendor customers : lastForteenDaysAddedModifiedCustomers) {
				ObjectNode lastForteenDaysCustomersrow = Json.newObject();
				lastForteenDaysCustomersrow.put("lastForteenDaysCustomers", customers.getName());
				List<BranchVendors> customerBranches = customers.getVendorBranches();
				String branches = "";
				String customerItems = "";
				for (BranchVendors custBnchs : customerBranches) {
					branches += custBnchs.getBranch().getName() + ",";
				}
				lastForteenDaysCustomersrow.put("lastForteenDaysCustomersBranches", branches);
				List<VendorSpecific> customerItemsList = customers.getVendorsSpecifics();
				for (VendorSpecific custItems : customerItemsList) {
					customerItems += custItems.getSpecificsVendors().getName() + ",";
				}
				lastForteenDaysCustomersrow.put("lastForteenDaysCustomersItems", customerItems);
				lastForteenDaysCustomersan.add(lastForteenDaysCustomersrow);
			}
			StringBuilder fortenDaysBackVendor = new StringBuilder();
			fortenDaysBackVendor.append("select obj from Vendor obj WHERE obj.type = 1 and obj.organization = '"
					+ user.getOrganization().getId() + "' and obj.presentStatus=1 and (obj.createdAt  between '"
					+ forteenDaysBack + "' and '" + currentDate + "' or obj.modifiedAt between '" + forteenDaysBack
					+ "' and '" + currentDate + "')");
			// List<Vendor> lastForteenDaysAddedModifiedVendors =
			// genericDAO.executeSimpleQuery(fortenDaysBackVendor.toString(),
			// entityManager);
			List<Vendor> lastForteenDaysAddedModifiedVendors = genericDAO
					.executeSimpleQueryWithLimit(fortenDaysBackVendor.toString(), entityManager, 5); // limiting no. of
																										// vendors else
																										// it hangs
			for (Vendor vendors : lastForteenDaysAddedModifiedVendors) {
				ObjectNode lastForteenDaysVendorsrow = Json.newObject();
				lastForteenDaysVendorsrow.put("lastForteenDaysVendors", vendors.getName());
				List<BranchVendors> vendorBranches = vendors.getVendorBranches();
				String branches = "";
				String vendorItems = "";
				for (BranchVendors vendBnchs : vendorBranches) {
					branches += vendBnchs.getBranch().getName() + ",";
				}
				lastForteenDaysVendorsrow.put("lastForteenDaysVendorBranches", branches);
				List<VendorSpecific> vendorItemsList = vendors.getVendorsSpecifics();
				for (VendorSpecific vendItems : vendorItemsList) {
					vendorItems += vendItems.getSpecificsVendors().getName() + ",";
				}
				lastForteenDaysVendorsrow.put("lastForteenDaysVendorItems", vendorItems);
				lastForteenDaysVendorsan.add(lastForteenDaysVendorsrow);
			}
			StringBuilder fortenDaysBackUsers = new StringBuilder();
			fortenDaysBackUsers.append("select obj from Users obj WHERE obj.presentStatus = 1 and obj.organization = '"
					+ user.getOrganization().getId() + "' and obj.presentStatus=1 and (obj.createdAt  between '"
					+ forteenDaysBack + "' and '" + currentDate + "' or obj.modifiedAt between '" + forteenDaysBack
					+ "' and '" + currentDate + "')");
			List<Users> lastForteenDaysAddedModifiedUsers = genericDAO
					.executeSimpleQueryWithLimit(fortenDaysBackUsers.toString(), entityManager, 5);
			for (Users users : lastForteenDaysAddedModifiedUsers) {
				String userCreationRights = "";
				String userApprovalRights = "";
				String userRoles = "";
				String userCreationRightsSpecifics = "";
				String userApprovalRightsSpecifics = "";
				ObjectNode lastForteenDaysUsersrow = Json.newObject();
				lastForteenDaysUsersrow.put("lastForteenDaysUsers", users.getFullName() + "(" + users.getEmail() + ")");
				List<UserRightInBranch> userRightsInBranches = users.getUserRightsInBranches();
				for (UserRightInBranch usrRghtBnchs : userRightsInBranches) {
					if (usrRghtBnchs.getUserRights().getId() == 1L) {
						userCreationRights += usrRghtBnchs.getBranch().getName() + ",";
					}
					if (usrRghtBnchs.getUserRights().getId() == 2L) {
						userApprovalRights += usrRghtBnchs.getBranch().getName() + ",";
					}
				}
				lastForteenDaysUsersrow.put("lastForteenDaysUsersCreationRightForBranches", userCreationRights);
				lastForteenDaysUsersrow.put("lastForteenDaysUsersApprovalRightForBranches", userApprovalRights);
				List<UsersRoles> userRolesList = users.getUserRoles();
				for (UsersRoles usrRoles : userRolesList) {
					userRoles += usrRoles.getRole().getName() + ",";
				}
				lastForteenDaysUsersrow.put("lastForteenDaysUsersRoles", userRoles);
				StringBuilder userrightsspecifics = new StringBuilder();
				userrightsspecifics.append("select obj from UserRightSpecifics obj WHERE obj.user = '" + users.getId()
						+ "' and obj.presentStatus=1");
				List<UserRightSpecifics> userRightSpecificsList = genericDAO
						.executeSimpleQuery(userrightsspecifics.toString(), entityManager);
				for (UserRightSpecifics usrRightSpecf : userRightSpecificsList) {
					if (usrRightSpecf.getUserRights().getId() == 1L) {
						userCreationRightsSpecifics += usrRightSpecf.getSpecifics().getName() + ",";
					}
					if (usrRightSpecf.getUserRights().getId() == 2L) {
						userApprovalRightsSpecifics += usrRightSpecf.getSpecifics().getName() + ",";
					}
				}
				lastForteenDaysUsersrow.put("lastForteenDaysUsersCreationRightForItems", userCreationRightsSpecifics);
				lastForteenDaysUsersrow.put("lastForteenDaysUsersApprovalRightForItems", userApprovalRightsSpecifics);
				lastForteenDaysUsersan.add(lastForteenDaysUsersrow);
			}
			StringBuilder forteenBackDatePendingApproval = new StringBuilder("");
			forteenBackDatePendingApproval
					.append("select obj from Transaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' and obj.presentStatus=1 and (obj.transactionStatus = 'Require Approval' or obj.transactionStatus = 'Require Additional Approval') and obj.transactionDate between '"
							+ forteenBackDate + "' and '" + currDate + "'");
			List<Transaction> txnPendindApproval = genericDAO
					.executeSimpleQueryWithLimit(forteenBackDatePendingApproval.toString(), entityManager, 10);
			if (txnPendindApproval.size() > 0) {
				dashboardrow.put("forteenBackDatePendingApproval", txnPendindApproval.size());
			} else {
				dashboardrow.put("forteenBackDatePendingApproval", "");
			}
			for (Transaction pendTxn : txnPendindApproval) {
				ObjectNode forteenBackDatePendingApprovalrow = Json.newObject();
				forteenBackDatePendingApprovalrow.put("forteenBackDatePendingApprovalTxnRef",
						pendTxn.getTransactionRefNumber());
				lastForteenDaysPendingApprovalan.add(forteenBackDatePendingApprovalrow);
			}
			// Connection con = MySqlConnection.getConnection();
			// Statement stmt = null;
			StringBuilder maxexpensebranchquery = new StringBuilder("");
			maxexpensebranchquery.append(
					"SELECT TRANSACTION_BRANCH, SUM(NET_AMOUNT) AS NET_AMOUNT FROM TRANSACTION WHERE TRANSACTION_BRANCH_ORGANIZATION = '"
							+ user.getOrganization().getId()
							+ "' AND TRANSACTION_STATUS = 'Accounted' AND (TRANSACTION_PURPOSE = 3 OR TRANSACTION_PURPOSE = 4 OR TRANSACTION_PURPOSE = 11) AND TRANSACTION_ACTIONDATE BETWEEN  '"
							+ forteenBackDate + "' and '" + currDate
							+ "' GROUP BY TRANSACTION_BRANCH ORDER BY NET_AMOUNT DESC LIMIT 1");
			// stmt = (Statement) con.createStatement();
			// ResultSet rs = stmt.executeQuery(maxexpensebranchquery.toString());
			String maxExpenseBranch = "";
			System.out.println("DashBoard Query : " + maxexpensebranchquery.toString());
			Query query = entityManager.createNativeQuery(maxexpensebranchquery.toString());
			// query.setMaxResults(1);
			List<Object[]> resultList = query.getResultList();
			// while(rs.next()){
			for (Object[] resultObj : resultList) {
				// Integer branchId = rs.getInt("TRANSACTION_BRANCH");
				// Double maxNetAmount = rs.getDouble("NET_AMOUNT");
				Integer branchId = (Integer) resultObj[0];
				Double maxNetAmount = (Double) resultObj[1];
				Branch maxTxnBranchEntity = null;
				if (branchId != null && branchId > 0) {
					maxTxnBranchEntity = Branch.findById(branchId.longValue());
				}
				if (maxNetAmount != null && maxNetAmount > 0.0) {
					maxExpenseBranch = maxTxnBranchEntity.getName() + ":"
							+ String.valueOf(IdosConstants.decimalFormat.format(maxNetAmount));
				}
			}

			dashboardrow.put("maxExpenseBranch", maxExpenseBranch);
			// Statement stmt1 = null;
			StringBuilder maxincomebranchquery = new StringBuilder("");
			maxincomebranchquery.append(
					"SELECT TRANSACTION_BRANCH, SUM(NET_AMOUNT) AS NET_AMOUNT FROM TRANSACTION WHERE TRANSACTION_BRANCH_ORGANIZATION = '"
							+ user.getOrganization().getId()
							+ "' AND TRANSACTION_STATUS = 'Accounted' AND (TRANSACTION_PURPOSE = 1 OR TRANSACTION_PURPOSE = 2) AND TRANSACTION_ACTIONDATE BETWEEN  '"
							+ forteenBackDate + "' and '" + currDate
							+ "' GROUP BY TRANSACTION_BRANCH ORDER BY NET_AMOUNT DESC LIMIT 1");
			// stmt1 = (Statement) con.createStatement();
			// ResultSet rs1 = stmt1.executeQuery(maxincomebranchquery.toString());
			String maxIncomeBranch = "";
			query = entityManager.createNativeQuery(maxincomebranchquery.toString());
			// query.setMaxResults(1);
			List<Object[]> branchList = query.getResultList();
			// while(rs.next()){
			for (Object[] resultObj : branchList) {
				// Integer branchId = rs.getInt("TRANSACTION_BRANCH");
				// Double maxNetAmount = rs.getDouble("NET_AMOUNT");
				Integer branchId = (Integer) resultObj[0];
				Double maxNetAmount = (Double) resultObj[1];

				Branch maxTxnBranchEntity = null;
				if (branchId != null && branchId > 0) {
					maxTxnBranchEntity = Branch.findById(branchId.longValue());
				}
				if (maxNetAmount != null && maxNetAmount > 0.0) {
					maxIncomeBranch = maxTxnBranchEntity.getName() + ":"
							+ String.valueOf(IdosConstants.decimalFormat.format(maxNetAmount));
				}
			}
			dashboardrow.put("maxIncomeBranch", maxIncomeBranch);
			StringBuilder fortenDaysBackTxnExceedingBudgetAccountHeadWise = new StringBuilder();
			// fortenDaysBackTxnExceedingBudgetAccountHeadWise.append("select obj from
			// Transaction obj WHERE obj.transactionExceedingBudget = 1 and
			// obj.transactionBranchOrganization = '"+user.getOrganization().getId()+"' and
			// obj.transactionDate between '"+forteenBackDate+"' and '"+currDate+"' GROUP BY
			// obj.transactionSpecifics");
			fortenDaysBackTxnExceedingBudgetAccountHeadWise.append(
					"select obj from Transaction obj WHERE obj.transactionExceedingBudget = 1 and obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' and obj.presentStatus=1 and obj.transactionDate between '" + forteenBackDate
							+ "' and '" + currDate + "'");
			List<Transaction> lastForteenDaysTxnExceedingBudgetAccountHeadWise = genericDAO
					.executeSimpleQuery(fortenDaysBackTxnExceedingBudgetAccountHeadWise.toString(), entityManager);
			for (Transaction txnExceedingBudgetAHW : lastForteenDaysTxnExceedingBudgetAccountHeadWise) {
				String txnExcBudRefNumberCommaSeparatedString = "";
				ObjectNode lastForteenDaysTxnExceedingBudgetAHWrow = Json.newObject();
				String immediateParent = "";
				if (txnExceedingBudgetAHW.getTransactionSpecifics().getParentSpecifics() != null) {
					immediateParent = txnExceedingBudgetAHW.getTransactionSpecifics().getParentSpecifics().getName();
				} else {
					immediateParent = txnExceedingBudgetAHW.getTransactionSpecifics().getParticularsId().getName();
				}
				lastForteenDaysTxnExceedingBudgetAHWrow.put("txnExceedingBudgetBranchRefNoAHSpecificsId",
						txnExceedingBudgetAHW.getTransactionSpecifics().getId());
				lastForteenDaysTxnExceedingBudgetAHWrow.put("txnExceedingBudgetBranchRefNoAH", "(" + immediateParent
						+ " )-->" + txnExceedingBudgetAHW.getTransactionSpecifics().getName() + "");
				StringBuilder fortenDaysBackTxnExceedingBudget = new StringBuilder();
				fortenDaysBackTxnExceedingBudget.append(
						"select obj from Transaction obj WHERE obj.transactionExceedingBudget = 1 and obj.transactionBranchOrganization = '"
								+ user.getOrganization().getId() + "' and obj.transactionSpecifics = '"
								+ txnExceedingBudgetAHW.getTransactionSpecifics().getId()
								+ "' and obj.presentStatus=1 and obj.transactionDate between '" + forteenBackDate
								+ "' and '" + currDate + "'");
				List<Transaction> lastForteenDaysTxnExceedingBudget = genericDAO
						.executeSimpleQuery(fortenDaysBackTxnExceedingBudget.toString(), entityManager);
				for (Transaction txnExceedingBudget : lastForteenDaysTxnExceedingBudget) {
					ObjectNode lastForteenDaysTxnExceedingBudgetrow = Json.newObject();
					lastForteenDaysTxnExceedingBudgetrow.put("txnExceedingBudgetBranchRefNo",
							txnExceedingBudget.getTransactionRefNumber());
					lastForteenDaysTxnExceedingBudgetan.add(lastForteenDaysTxnExceedingBudgetrow);
					txnExcBudRefNumberCommaSeparatedString += txnExceedingBudget.getTransactionRefNumber() + ",";
				}
				lastForteenDaysTxnExceedingBudgetAHWrow.put("txnExceedingBudgetBranchRefNo",
						txnExcBudRefNumberCommaSeparatedString.substring(0,
								txnExcBudRefNumberCommaSeparatedString.length() - 1));
				lastForteenDaysTxnExceedingBudgetAHWan.add(lastForteenDaysTxnExceedingBudgetAHWrow);
			}
			StringBuilder fortenDaysBackTxnKlNotFollwedAccountHeadWise = new StringBuilder();
			// fortenDaysBackTxnKlNotFollwedAccountHeadWise.append("select obj from
			// Transaction obj WHERE obj.klFollowStatus = 0 and
			// obj.transactionBranchOrganization = '"+user.getOrganization().getId()+"' and
			// obj.transactionDate between '"+forteenBackDate+"' and '"+currDate+"' GROUP BY
			// obj.transactionSpecifics");
			fortenDaysBackTxnKlNotFollwedAccountHeadWise.append(
					"select obj from Transaction obj WHERE obj.klFollowStatus = 0 and obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' and obj.presentStatus=1 and obj.transactionDate between '" + forteenBackDate
							+ "' and '" + currDate + "'");
			List<Transaction> lastForteenDaysTxnKlNotFollwedAccountHeadWise = genericDAO.executeSimpleQueryWithLimit(
					fortenDaysBackTxnKlNotFollwedAccountHeadWise.toString(), entityManager, 10);
			for (Transaction txnKlNotFollowedAHW : lastForteenDaysTxnKlNotFollwedAccountHeadWise) {
				String txnKlNotFollowedRefNumberCommaSeparatedString = "";
				ObjectNode lastForteenDaysKlNotFollowedAHWrow = Json.newObject();
				String immediateParent = "";
				if (txnKlNotFollowedAHW.getTransactionSpecifics().getParentSpecifics() != null) {
					immediateParent = txnKlNotFollowedAHW.getTransactionSpecifics().getParentSpecifics().getName();
				} else {
					immediateParent = txnKlNotFollowedAHW.getTransactionSpecifics().getParticularsId().getName();
				}
				lastForteenDaysKlNotFollowedAHWrow.put("txnKlNotFollowedBranchRefNoAHSpecificsId",
						txnKlNotFollowedAHW.getTransactionSpecifics().getId());
				lastForteenDaysKlNotFollowedAHWrow.put("txnKlNotFollowedBranchRefNoAH",
						"(" + immediateParent + " )-->" + txnKlNotFollowedAHW.getTransactionSpecifics().getName() + "");
				StringBuilder fortenDaysBackTxnKlNotFollowed = new StringBuilder();
				fortenDaysBackTxnKlNotFollowed.append(
						"select obj from Transaction obj WHERE obj.klFollowStatus = 0 and obj.transactionBranchOrganization = '"
								+ user.getOrganization().getId()
								+ "' and obj.presentStatus=1 and obj.transactionSpecifics = '"
								+ txnKlNotFollowedAHW.getTransactionSpecifics().getId()
								+ "' and obj.transactionDate between '" + forteenBackDate + "' and '" + currDate + "'");
				List<Transaction> lastForteenDaysTxnKlNotFollowed = genericDAO
						.executeSimpleQueryWithLimit(fortenDaysBackTxnKlNotFollowed.toString(), entityManager, 10);
				for (Transaction txnKLNotFollowed : lastForteenDaysTxnKlNotFollowed) {
					ObjectNode lastForteenDaysTxnKlNotFollowedrow = Json.newObject();
					lastForteenDaysTxnKlNotFollowedrow.put("txnKlNotFollowedBranchRefNo",
							txnKLNotFollowed.getTransactionRefNumber());
					lastForteenDaysTxnKlNotFollwedan.add(lastForteenDaysTxnKlNotFollowedrow);
					txnKlNotFollowedRefNumberCommaSeparatedString += txnKLNotFollowed.getTransactionRefNumber() + ",";
				}
				lastForteenDaysKlNotFollowedAHWrow.put("txnKlNotFollowedBranchRefNo",
						txnKlNotFollowedRefNumberCommaSeparatedString.substring(0,
								txnKlNotFollowedRefNumberCommaSeparatedString.length() - 1));
				lastForteenDaysTxnKlNotFollwedAHWan.add(lastForteenDaysKlNotFollowedAHWrow);
			}
			StringBuilder fortenDaysBackBranches = new StringBuilder();
			fortenDaysBackBranches
					.append("select obj from Branch obj WHERE obj.presentStatus = 1 and obj.organization = ")
					.append(user.getOrganization().getId());
			fortenDaysBackBranches.append(" and (obj.createdAt between '").append(forteenDaysBack).append("' and '")
					.append(currentDate).append("'");
			fortenDaysBackBranches.append(" or obj.modifiedAt between '").append(forteenDaysBack).append("' and '")
					.append(currentDate).append("')");
			List<Branch> lastForteenDaysAddedModifiedBranches = genericDAO
					.executeSimpleQueryWithLimit(fortenDaysBackBranches.toString(), entityManager, 10);
			ObjectNode lastForteenDaysBranchrow = null;
			for (Branch branch : lastForteenDaysAddedModifiedBranches) {
				lastForteenDaysBranchrow = Json.newObject();
				lastForteenDaysBranchrow.put("lastForteenDaysBranch", branch.getName());
				lastForteenDaysBranchan.add(lastForteenDaysBranchrow);
			}
			// Get TrialBalance Turnover
			log.log(Level.FINE, " start date: " + startDate + " end Date: " + endDate);
			Double intraGSTTournover = TrialBalanceCOAItems.findTournOverIntraState(user.getOrganization().getId(),
					user.getBranch().getId(), startDate, endDate);
			Double interGSTTournover = TrialBalanceCOAItems.findTournOverInterState(user.getOrganization().getId(),
					user.getBranch().getId(), startDate, endDate);
			Double nonGSTTournover = TrialBalanceCOAItems.findTournOverNonGST(user.getOrganization().getId(),
					user.getBranch().getId(), startDate, endDate);
			Double exportTournover = TrialBalanceCOAItems.findTournOverExport(user.getOrganization().getId(),
					user.getBranch().getId(), startDate, endDate);
			Double totalTournover = intraGSTTournover + interGSTTournover + nonGSTTournover + exportTournover;

			if (user.getOrganization().getIsCompositionScheme() != null
					&& user.getOrganization().getIsCompositionScheme() == 1) {
				dashboardrow.put("isCompositionSchemeOrg", "true");
			} else {
				dashboardrow.put("isCompositionSchemeOrg", "false");
			}

			dashboardrow.put("intraGstTurnover", IdosConstants.decimalFormat.format(intraGSTTournover));
			dashboardrow.put("interGstTurnover", IdosConstants.decimalFormat.format(interGSTTournover));
			dashboardrow.put("nonGstTurnover", IdosConstants.decimalFormat.format(nonGSTTournover));
			dashboardrow.put("exportTurnover", IdosConstants.decimalFormat.format(exportTournover));
			dashboardrow.put("totalTurnover", IdosConstants.decimalFormat.format(totalTournover));

			dashboardan.add(dashboardrow);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
		}
		log.log(Level.FINE, "============ End " + results);
		return results;
	}

	@Override
	public ObjectNode getProjectGraph(final Users user, final EntityManager entityManager, final JsonNode json) {
		log.log(Level.FINE, "============ Start");
		int dashboardType = json.findValue("dType").asInt();
		int graphType = json.findValue("gType").asInt();
		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		if (0 == dashboardType || dashboardType > 8) {
			result.put("message", "Not a valid dashboard type.");
		} else if (0 == graphType || graphType > 3) {
			result.put("message", "Not a valid graph type.");
		} else if (1 == dashboardType || 2 == dashboardType || 3 == dashboardType || 4 == dashboardType) {
			result = getProjectCashCreditExpenseIncome(user, entityManager, dashboardType, graphType, json);
			result.put("dashboardforbranch", "project");
		} else if (5 == dashboardType) {
			result = getExpenseBudgetAvaialable(user, entityManager, dashboardType, graphType);
			result.put("dashboardforbranch", "branch");
		} else if (6 == dashboardType || 7 == dashboardType) {
			result = getProjectTotalRecieveablesPayables(user, entityManager, dashboardType, graphType);
			result.put("dashboardforbranch", "project");
		}
		if (result.findValue("result").asBoolean() && dashboardType > 0 && dashboardType <= 7 && graphType > 0
				&& graphType <= 3) {
			StringBuilder graphName = new StringBuilder(), type = new StringBuilder();
			if (1 == dashboardType) {
				graphName.append("Cash Expense");
				type.append("CashExpense");
			} else if (2 == dashboardType) {
				graphName.append("Credit Expense");
				type.append("CreditExpense");
			} else if (3 == dashboardType) {
				graphName.append("Cash Income");
				type.append("CashIncome");
			} else if (4 == dashboardType) {
				graphName.append("Credit Income");
				type.append("CreditIncome");
			} else if (5 == dashboardType) {
				graphName.append("Proforma Invoice - Branchwise");
				type.append("ProformaInvoice");
			} else if (6 == dashboardType) {
				graphName.append("Total Recieveables");
				type.append("TotalReceivables");
			} else if (7 == dashboardType) {
				graphName.append("Toatal Payables");
				type.append("TotalPayables");
			}
			if (1 == graphType) {
				graphName.append(" - This Month");
				type.insert(0, "thisWeek");
			} else if (2 == graphType) {
				graphName.append(" - Previous Month");
				type.insert(0, "previousWeek");
			} else if (3 == graphType) {
				graphName.append(" - Variance");
				type.insert(0, "thisWeekPreviousWeek");
				type.append("Varience");
			}
			result.put("graphName", graphName.toString());
			result.put("type", type.toString());
			result.put("dashboardfor", "project");
		}
		return result;
	}

	@Override
	public ObjectNode getGraph(final Users user, final EntityManager entityManager, final JsonNode json) {
		log.log(Level.FINE, "============ Start");
		int dashboardType = json.findValue("dType").asInt();
		int graphType = json.findValue("gType").asInt();
		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		if (0 == dashboardType || dashboardType > 8) {
			result.put("message", "Not a valid dashboard type.");
		} else if (0 == graphType || graphType > 3) {
			result.put("message", "Not a valid graph type.");
		} else if (1 == dashboardType || 2 == dashboardType || 3 == dashboardType || 4 == dashboardType
				|| 5 == dashboardType || 8 == dashboardType) {
			result = getCashCreditExpenseIncome(user, entityManager, dashboardType, graphType, json);
			result.put("dashboardforbranch", "branch");
		} /*
			 * else if (5 == dashboardType) {
			 * result = getExpenseBudgetAvaialable(user, entityManager, dashboardType,
			 * graphType);
			 * result.put("dashboardforbranch", "branch");
			 * }
			 */else if (6 == dashboardType || 7 == dashboardType) {
			result = getTotalRecieveablesPayables(user, entityManager, dashboardType, graphType);
			result.put("dashboardforbranch", "branch");
		}
		if (result.findValue("result").asBoolean() && dashboardType > 0 && dashboardType <= 8 && graphType > 0
				&& graphType <= 3) {
			StringBuilder graphName = new StringBuilder(), type = new StringBuilder();
			if (1 == dashboardType) {
				graphName.append("Cash Expense");
				type.append("CashExpense");
			} else if (2 == dashboardType) {
				graphName.append("Credit Expense");
				type.append("CreditExpense");
			} else if (3 == dashboardType) {
				graphName.append("Cash Income");
				type.append("CashIncome");
			} else if (4 == dashboardType) {
				graphName.append("Credit Income");
				type.append("CreditIncome");
			} else if (5 == dashboardType) {
				graphName.append("Proforma Invoice - Branchwise");
				type.append("ProformaInvoice");
			} else if (6 == dashboardType) {
				graphName.append("Total Recieveables");
				type.append("TotalReceivables");
			} else if (7 == dashboardType) {
				graphName.append("Toatal Payables");
				type.append("TotalPayables");
			} else if (8 == dashboardType) {
				graphName.append("Quotation Invoice - Branchwise");
				type.append("quotationInvoice");
			}
			if (1 == graphType) {
				graphName.append(" - This Month");
				type.insert(0, "thisWeek");
			} else if (2 == graphType) {
				graphName.append(" - Previous Month");
				type.insert(0, "previousWeek");
			} else if (3 == graphType) {
				graphName.append(" - Variance");
				type.insert(0, "thisWeekPreviousWeek");
				type.append("Varience");
			}
			result.put("graphName", graphName.toString());
			result.put("type", type.toString());
			result.put("dashboardfor", "branch");
		}
		log.log(Level.FINE, "============ End " + result);
		return result;
	}

	@Override
	public ObjectNode getProjectCashCreditExpenseIncome(final Users user, final EntityManager entityManager,
			final int dashboardType, final int graphType, final JsonNode json) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		try {
			if (null == user) {
				result.put("message", "Not a valid request. Please login again to continue.");
			} else {
				String currDashboardFromDate = json.findValue("currDashboardFromDate") != null
						? json.findValue("currDashboardFromDate").asText()
						: null;
				String currDashboardToDate = json.findValue("currDashboardToDate") != null
						? json.findValue("currDashboardToDate").asText()
						: null;
				String prevDashboardFromDate = json.findValue("prevDashboardFromDate") != null
						? json.findValue("prevDashboardFromDate").asText()
						: null;
				String prevDashboardToDate = json.findValue("prevDashboardToDate") != null
						? json.findValue("prevDashboardToDate").asText()
						: null;
				ArrayNode data = result.putArray("data");
				ObjectNode dataRow = null;
				String startCurWeek = null, endCurWeek = null, startPrevWeek = null, endPrevWeek = null;
				if (1 == graphType || 3 == graphType) {
					if (currDashboardFromDate != null && !"".equals(currDashboardFromDate)
							&& currDashboardToDate != null && !"".equals(currDashboardToDate)) {
						startCurWeek = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(currDashboardFromDate));
						endCurWeek = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(currDashboardToDate));
					} else {
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
						startCurWeek = IdosConstants.mysqldf.format(cal.getTime());
						cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
						endCurWeek = IdosConstants.mysqldf.format(cal.getTime());
					}
					/*
					 * Calendar cal = Calendar.getInstance();
					 * cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
					 * startCurWeek = mysqldf.format(cal.getTime());
					 * cal.add(Calendar.DAY_OF_WEEK, 6);
					 * endCurWeek = mysqldf.format(cal.getTime());
					 */
				}
				if (2 == graphType || 3 == graphType) {
					if (prevDashboardFromDate != null && !"".equals(prevDashboardFromDate)
							&& prevDashboardToDate != null && !"".equals(prevDashboardToDate)) {
						startPrevWeek = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(prevDashboardFromDate));
						endPrevWeek = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(prevDashboardToDate));
					} else {
						Calendar newcal = Calendar.getInstance();
						newcal.add(Calendar.MONTH, -1);
						newcal.set(Calendar.DAY_OF_MONTH, newcal.getActualMinimum(Calendar.DAY_OF_MONTH));
						startPrevWeek = IdosConstants.mysqldf.format(newcal.getTime());
						newcal.set(Calendar.DAY_OF_MONTH, newcal.getActualMaximum(Calendar.DAY_OF_MONTH));
						endPrevWeek = IdosConstants.mysqldf.format(newcal.getTime());
					}
				}
				double amount = 0.0;
				List<Project> projects = user.getOrganization().getProjects();
				for (Project project : projects) {
					if (null != project && null != project.getId()) {
						dataRow = Json.newObject();
						amount = getCashCreditAmountForProject(project.getId(), user.getOrganization().getId(),
								dashboardType, graphType, entityManager, startCurWeek, endCurWeek, startPrevWeek,
								endPrevWeek);
						dataRow.put("amount", amount);
						dataRow.put("branch", project.getName());
						dataRow.put("branchid", project.getId());
						dataRow.put(project.getName(), amount);
						data.add(dataRow);
					}
				}
				result.put("result", true);
				result.remove("message");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			log.log(Level.SEVERE, ex.getMessage());
		}
		return result;
	}

	@Override
	public ObjectNode getCashCreditExpenseIncome(final Users user, final EntityManager entityManager,
			final int dashboardType, final int graphType, final JsonNode json) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		try {
			if (null == user) {
				result.put("message", "Not a valid request. Please login again to continue.");
			} else {
				String currDashboardFromDate = json.findValue("currDashboardFromDate") != null
						? json.findValue("currDashboardFromDate").asText()
						: null;
				String currDashboardToDate = json.findValue("currDashboardToDate") != null
						? json.findValue("currDashboardToDate").asText()
						: null;
				String prevDashboardFromDate = json.findValue("prevDashboardFromDate") != null
						? json.findValue("prevDashboardFromDate").asText()
						: null;
				String prevDashboardToDate = json.findValue("prevDashboardToDate") != null
						? json.findValue("prevDashboardToDate").asText()
						: null;
				ArrayNode data = result.putArray("data");
				ObjectNode dataRow = null;
				String startCurWeek = null, endCurWeek = null, startPrevWeek = null, endPrevWeek = null;
				if (1 == graphType || 3 == graphType) {
					if (currDashboardFromDate != null && !"".equals(currDashboardFromDate)
							&& currDashboardToDate != null && !"".equals(currDashboardToDate)) {
						startCurWeek = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(currDashboardFromDate));
						endCurWeek = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(currDashboardToDate));
					} else {
						// now we will get dashboard data for month basis
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
						startCurWeek = IdosConstants.mysqldf.format(cal.getTime());
						cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
						endCurWeek = IdosConstants.mysqldf.format(cal.getTime());
					}
				}
				if (2 == graphType || 3 == graphType) {
					if (prevDashboardFromDate != null && !"".equals(prevDashboardFromDate)
							&& prevDashboardToDate != null && !"".equals(prevDashboardToDate)) {
						startPrevWeek = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(prevDashboardFromDate));
						endPrevWeek = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(prevDashboardToDate));
					} else {
						Calendar newcal = Calendar.getInstance();
						newcal.add(Calendar.MONTH, -1);
						newcal.set(Calendar.DAY_OF_MONTH, newcal.getActualMinimum(Calendar.DAY_OF_MONTH));
						startPrevWeek = IdosConstants.mysqldf.format(newcal.getTime());
						newcal.set(Calendar.DAY_OF_MONTH, newcal.getActualMaximum(Calendar.DAY_OF_MONTH));
						endPrevWeek = IdosConstants.mysqldf.format(newcal.getTime());
					}
				}
				double amount = 0.0;
				List<Branch> branches = user.getOrganization().getBranches();
				for (Branch branch : branches) {
					if (null != branch && null != branch.getId()) {
						dataRow = Json.newObject();
						amount = getCashCreditAmountForBranch(user, branch.getId(), user.getOrganization().getId(),
								dashboardType, graphType, entityManager, startCurWeek, endCurWeek, startPrevWeek,
								endPrevWeek);
						dataRow.put("amount", amount);
						dataRow.put("branch", branch.getName());
						dataRow.put("branchid", branch.getId());
						dataRow.put(branch.getName(), amount);
						data.add(dataRow);
					}
				}
				result.put("result", true);
				result.remove("message");
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
		}
		log.log(Level.FINE, "============ End");
		return result;
	}

	private double getCashCreditAmountForProject(final long projectId, final long orgId, final int dashboardType,
			final int graphType, final EntityManager entityManager, final String startCurWeek, final String endCurWeek,
			final String startPrevWeek, final String endPrevWeek) {
		log.log(Level.FINE, "============ Start");
		double amount = 0.0;
		StringBuilder query = new StringBuilder();
		query.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionProject = ")
				.append(projectId);
		query.append(" and obj.transactionBranchOrganization = ").append(orgId);
		query.append(" and obj.transactionStatus = 'Accounted' and obj.presentStatus=1");
		if (1 == dashboardType) {
			query.append(" AND (obj.transactionPurpose = 3 or obj.transactionPurpose = 11)");
		} else if (2 == dashboardType) {
			query.append(" AND obj.transactionPurpose = 4");
		} else if (3 == dashboardType) {
			query.append(" AND obj.transactionPurpose = 1");
		} else if (4 == dashboardType) {
			query.append(" AND obj.transactionPurpose = 2");
		}
		if (1 == graphType || 3 == graphType) {
			query.append(" and obj.transactionDate between '").append(startCurWeek).append("' and '").append(endCurWeek)
					.append("'");
		} else if (2 == graphType) {
			query.append(" and obj.transactionDate between '").append(startPrevWeek).append("' and '")
					.append(endPrevWeek).append("'");
		}
		List<Transaction> cashTransaction = genericDAO.executeSimpleQuery(query.toString(), entityManager);
		if (cashTransaction.size() > 0) {
			Object val = cashTransaction.get(0);
			amount = (null == val) ? 0.0 : Double.parseDouble(String.valueOf(val));
		}
		if (3 == graphType) {
			query.delete(0, query.length());
			query.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionProject = ")
					.append(projectId);
			query.append(" and obj.transactionBranchOrganization = ").append(orgId);
			query.append(" and obj.transactionStatus  =  'Accounted' and obj.presentStatus=1");
			if (1 == dashboardType) {
				query.append(" AND (obj.transactionPurpose = 3 or obj.transactionPurpose = 11)");
			} else if (2 == dashboardType) {
				query.append(" AND obj.transactionPurpose = 4");
			} else if (3 == dashboardType) {
				query.append(" AND obj.transactionPurpose = 1");
			} else if (4 == dashboardType) {
				query.append(" AND obj.transactionPurpose = 2");
			}
			query.append(" and obj.transactionDate between '").append(startPrevWeek).append("' and '")
					.append(endPrevWeek).append("'");
			cashTransaction = genericDAO.executeSimpleQuery(query.toString(), entityManager);
			if (cashTransaction.size() > 0) {
				Object val = cashTransaction.get(0);
				Double prevAmount = (null == val) ? 0.0 : Double.parseDouble(String.valueOf(val));
				amount -= prevAmount;
			}
		}
		return amount;
	}

	private double getCashCreditAmountForBranch(final Users user, final long branchId, final long orgId,
			final int dashboardType, final int graphType, final EntityManager entityManager, final String startCurWeek,
			final String endCurWeek, final String startPrevWeek, final String endPrevWeek) {
		log.log(Level.FINE, "============ Start");
		double amount = 0.0;
		double customerNetPayment = 0.0;
		Branch branch = genericDAO.getById(Branch.class, branchId, entityManager);
		try {

			// get journal entry amount
			Map allSpecifcsAmtData = new HashMap();
			Map vendorPayablesData = new HashMap();
			Map custReceivablesData = new HashMap();
			ProvisionJournalEntryService jourObj = new ProvisionJournalEntryServiceImpl();
			Map provisionEntries = jourObj.getDashboardProvisionEntriesDataForBranch(startCurWeek, endCurWeek, user,
					allSpecifcsAmtData, vendorPayablesData, custReceivablesData, branch, entityManager);

			StringBuilder query = new StringBuilder(
					"select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch = ").append(branchId);
			query.append(" and obj.transactionBranchOrganization = ").append(orgId);
			query.append(" and obj.transactionStatus = 'Accounted' and obj.presentStatus=1");

			if (1 == dashboardType) {// Cash Expense
				query.append(" AND (obj.transactionPurpose  =  3 or obj.transactionPurpose = 11)");
				amount = new Double(provisionEntries.get("totalBranchCashExpense").toString());
				// Expenses due to claims transaction ident_data_Valid = 23,24,25,26 or
				// tran_purpose = 16,18,19 cash payment when settling travel advance/settle
				// expense reimbursement
				StringBuilder sbquery = new StringBuilder();
				sbquery.append(
						"select SUM(obj.newAmount) from ClaimTransaction obj WHERE obj.transactionBranchOrganization = '"
								+ orgId + "' and obj.transactionBranch = '" + branchId
								+ "' and obj.transactionPurpose in (16,18,19) and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.receiptDetailType in (1,0) and obj.transactionDate  between '"
								+ startCurWeek + "' and '" + endCurWeek + "'");
				List<ClaimTransaction> clmTxnList = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
				if (!clmTxnList.isEmpty() && clmTxnList.size() > 0) {
					Object val = clmTxnList.get(0);
					if (val != null) {
						amount += Double.parseDouble(String.valueOf(val));
					}
				}
			} else if (2 == dashboardType) {// Credit Expense
				query.append(" AND obj.transactionPurpose  =  4");
				amount = new Double(provisionEntries.get("totalBranchCreditExpense").toString());
				// Expenses due to claims transaction ident_data_Valid = 23,24,25,26 or
				// tran_purpose = 16,18,19 cash payment when settling travel advance/settle
				// expense reimbursement
				StringBuilder sbquery = new StringBuilder();
				sbquery.append(
						"select SUM(obj.newAmount) from ClaimTransaction obj WHERE obj.transactionBranchOrganization = '"
								+ orgId + "' and obj.transactionBranch = '" + branchId
								+ "' and obj.transactionPurpose in (16,18,19) and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.receiptDetailType = 2 and obj.transactionDate  between '"
								+ startCurWeek + "' and '" + endCurWeek + "'");
				List<ClaimTransaction> clmTxnList = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
				if (!clmTxnList.isEmpty() && clmTxnList.size() > 0) {
					Object val = clmTxnList.get(0);
					if (val != null) {
						amount += Double.parseDouble(String.valueOf(val));
					}
				}
			} else if (3 == dashboardType) {// Cash Income
				query.append(
						" AND ((obj.transactionPurpose  =  1) or (obj.transactionPurpose = 2 and obj.performaInvoice  =  true and obj.paymentStatus = 'PAID'))");
				amount = new Double(provisionEntries.get("totalBranchCashIncome").toString());
			} else if (4 == dashboardType) {// Credit Income
				query.append(
						" AND obj.transactionPurpose  =  2 and (obj.performaInvoice  !=  true or obj.performaInvoice is null )");
				amount = new Double(provisionEntries.get("totalBranchCreditIncome").toString());
			} else if (5 == dashboardType) { // for proforma invoice, show data for entire financial year
				String[] arr = DateUtil.getFinancialDate(user);
				String finStartDate = arr[0];
				String finEndDate = arr[1];
				query.append(" AND obj.transactionPurpose  =  28 and obj.transactionDate  between '" + finStartDate
						+ "' and '" + finEndDate + "'");

				/*
				 * StringBuilder sbr = new
				 * StringBuilder("select SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranch = '"
				 * +branchId+"' and obj.transactionBranchOrganization = '"
				 * +orgId+"' AND obj.transactionPurpose = 2 and obj.performaInvoice = true and obj.transactionStatus = 'Accounted' and (obj.paymentStatus = 'NOT-PAID' or obj.paymentStatus = 'PARTLY-PAID') and obj.transactionDate  between '"
				 * +finStartDate+"' and '"+finEndDate+"'");
				 * List<Transaction> customerNetPaymentTxn =
				 * genericDAO.executeSimpleQuery(sbr.toString(), entityManager);
				 * if(customerNetPaymentTxn.size()>0){
				 * Object val1 = customerNetPaymentTxn.get(0);
				 * customerNetPayment = (null == val1) ? 0.0 :
				 * Double.parseDouble(String.valueOf(val1));
				 * }
				 */
			} else if (8 == dashboardType) { // for quotation invoice, show data for entire financial year
				String[] arr = DateUtil.getFinancialDate(user);
				String finStartDate = arr[0];
				String finEndDate = arr[1];
				query.append(" AND obj.transactionPurpose  =  27 and obj.transactionDate  between '" + finStartDate
						+ "' and '" + finEndDate + "'");
			}
			// if (5 != dashboardType){
			if (1 == graphType || 3 == graphType) {
				query.append(" and obj.transactionDate between '").append(startCurWeek).append("' and '")
						.append(endCurWeek).append("'");
			} else if (2 == graphType) {
				query.append(" and obj.transactionDate between '").append(startPrevWeek).append("' and '")
						.append(endPrevWeek).append("'");
			}
			// }
			List<Transaction> cashTransaction = genericDAO.executeSimpleQuery(query.toString(), entityManager);
			if (cashTransaction.size() > 0) {
				Object val = cashTransaction.get(0);
				if (val != null) {
					amount = amount + Double.parseDouble(String.valueOf(val));
				}
				// amount = (null == val) ? 0.0 : Double.parseDouble(String.valueOf(val));
				// amount = amount - customerNetPayment;
			}
			StringBuilder sbquery = new StringBuilder("");
			if (2 == dashboardType) {
				// purchase retrun of 200 then totalbuy - purchaseReturn should be shown
				if (1 == graphType || 3 == graphType) {
					sbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch = '"
							+ branchId + "' and obj.transactionBranchOrganization = '" + orgId
							+ "' AND obj.transactionPurpose = 13 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
							+ startCurWeek + "' and '" + endCurWeek + "'");
				} else {
					sbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch = '"
							+ branchId + "' and obj.transactionBranchOrganization = '" + orgId
							+ "' AND obj.transactionPurpose = 13 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
							+ startPrevWeek + "' and '" + endPrevWeek + "'");
				}
			} else if (4 == dashboardType) {
				// sales retrun of 200 then totalsell - salesreturn should be shown
				if (1 == graphType || 3 == graphType) {
					sbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch = '"
							+ branchId + "' and obj.transactionBranchOrganization = '" + orgId
							+ "' AND obj.transactionPurpose = 12 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
							+ startCurWeek + "' and '" + endCurWeek + "'");
				} else {
					sbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch = '"
							+ branchId + "' and obj.transactionBranchOrganization = '" + orgId
							+ "' AND obj.transactionPurpose = 12 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
							+ startPrevWeek + "' and '" + endPrevWeek + "'");
				}
			}
			if (2 == dashboardType || 4 == dashboardType) {
				List<Transaction> salesReturnTxn = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
				if (salesReturnTxn.size() > 0) {
					Object val = salesReturnTxn.get(0);
					if (val != null) {
						amount = amount - Double.parseDouble(String.valueOf(val));
					}
				}
			}
			if (3 == graphType) {
				query.delete(0, query.length());
				query.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch = ")
						.append(branchId);
				query.append(" and obj.transactionBranchOrganization = ").append(orgId);
				query.append(" and obj.transactionStatus  =  'Accounted' and obj.presentStatus=1");
				if (1 == dashboardType) {
					query.append(" AND (obj.transactionPurpose  =  3 or obj.transactionPurpose  =  11)");
				} else if (2 == dashboardType) {
					query.append(" AND obj.transactionPurpose  =  4");
				} else if (3 == dashboardType) {
					query.append(
							" AND ((obj.transactionPurpose  =  1) or (obj.transactionPurpose = 2 and obj.performaInvoice  =  true and obj.paymentStatus = 'PAID'))");
				} else if (4 == dashboardType) {
					query.append(
							" AND obj.transactionPurpose  =  2 and (obj.performaInvoice  !=  true or obj.performaInvoice is null )");
				} else if (5 == dashboardType) {
					query.append(" AND obj.transactionPurpose  =  28 ");
				} else if (8 == dashboardType) {
					query.append(" AND obj.transactionPurpose  =  27 ");
				}

				query.append(" and obj.transactionDate between '").append(startPrevWeek).append("' and '")
						.append(endPrevWeek).append("'");
				cashTransaction = genericDAO.executeSimpleQuery(query.toString(), entityManager);
				if (cashTransaction.size() > 0) {
					Object val = cashTransaction.get(0);
					Double prevAmount = (null == val) ? 0.0 : Double.parseDouble(String.valueOf(val));
					amount -= prevAmount;
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
		}
		log.log(Level.FINE, "============ End");
		return amount;
	}

	/*
	 * private double getCashCreditAmountForBranch(final long branchId, final long
	 * orgId, final int dashboardType, final int graphType, final EntityManager
	 * entityManager,
	 * final String startCurWeek, final String endCurWeek, final String
	 * startPrevWeek, final String endPrevWeek) {
	 * log.log(Level.FINE, "============ Start");
	 * double amount = 0.0;
	 * StringBuilder query = new StringBuilder();
	 * query.
	 * append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch = "
	 * ).append(branchId);
	 * query.append(" and obj.transactionBranchOrganization = ").append(orgId);
	 * query.append(" and obj.transactionStatus  =  'Accounted'");
	 * if (1 == dashboardType) {
	 * query.
	 * append(" AND (obj.transactionPurpose  =  3 or obj.transactionPurpose  =  11)"
	 * );
	 * } else if (2 == dashboardType) {
	 * query.append(" AND obj.transactionPurpose  =  4");
	 * } else if (3 == dashboardType) {
	 * query.
	 * append(" AND (obj.transactionPurpose  =  1) or (obj.transactionPurpose = 2 and obj.performaInvoice  =  true and obj.paymentStatus = 'PAID')"
	 * );
	 * } else if (4 == dashboardType) {
	 * query.
	 * append(" AND obj.transactionPurpose  =  2 and (obj.performaInvoice  !=  true or obj.performaInvoice is null )"
	 * );
	 * } else if (5 == dashboardType) {
	 * query.
	 * append(" AND obj.transactionPurpose  =  2 And obj.performaInvoice = true And (obj.paymentStatus = 'NOT-PAID' OR obj.paymentStatus = 'PARTLY-PAID')"
	 * );
	 * }
	 * 
	 * if (1 == graphType || 3 == graphType) {
	 * query.append(" and obj.transactionDate between '").append(startCurWeek).
	 * append("' and '").append(endCurWeek).append("'");
	 * } else if (2 == graphType) {
	 * query.append(" and obj.transactionDate between '").append(startPrevWeek).
	 * append("' and '").append(endPrevWeek).append("'");
	 * }
	 * List<Transaction> cashTransaction =
	 * genericDAO.executeSimpleQuery(query.toString(), entityManager);
	 * if(cashTransaction.size()>0){
	 * Object val = cashTransaction.get(0);
	 * amount = (null == val) ? 0.0 : Double.parseDouble(String.valueOf(val));
	 * }
	 * if (3 == graphType) {
	 * query.delete(0, query.length());
	 * query.
	 * append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch = "
	 * ).append(branchId);
	 * query.append(" and obj.transactionBranchOrganization = ").append(orgId);
	 * query.append(" and obj.transactionStatus  =  'Accounted'");
	 * if (1 == dashboardType) {
	 * query.
	 * append(" AND (obj.transactionPurpose  =  3 or obj.transactionPurpose  =  11)"
	 * );
	 * } else if (2 == dashboardType) {
	 * query.append(" AND obj.transactionPurpose  =  4");
	 * } else if (3 == dashboardType) {
	 * query.
	 * append(" AND (obj.transactionPurpose  =  1) or (obj.transactionPurpose = 2 and obj.performaInvoice  =  true and obj.paymentStatus = 'PAID')"
	 * );
	 * } else if (4 == dashboardType) {
	 * query.
	 * append(" AND obj.transactionPurpose  =  2 and (obj.performaInvoice  !=  true or obj.performaInvoice is null )"
	 * );
	 * } else if (5 == dashboardType) {
	 * query.
	 * append(" AND obj.transactionPurpose  =  2 And obj.performaInvoice = true And (obj.paymentStatus = 'NOT-PAID' OR obj.paymentStatus = 'PARTLY-PAID')"
	 * );
	 * }
	 * 
	 * query.append(" and obj.transactionDate between '").append(startPrevWeek).
	 * append("' and '").append(endPrevWeek).append("'");
	 * cashTransaction =
	 * genericDAO.executeSimpleQuery(query.toString(),entityManager);
	 * if(cashTransaction.size()>0){
	 * Object val = cashTransaction.get(0);
	 * Double prevAmount = (null == val) ? 0.0 :
	 * Double.parseDouble(String.valueOf(val));
	 * amount -= prevAmount;
	 * }
	 * }
	 * return amount;
	 * }
	 */

	public ObjectNode getExpenseBudgetAvaialable(final Users user, final EntityManager entityManager,
			final int dashboardType, final int graphType) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		if (null == user) {
			result.put("message", "Not a valid request. Please login again to continue.");
		} else {
			String startCurWeek = null, endCurWeek = null, startPrevWeek = null, endPrevWeek = null;
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			startCurWeek = IdosConstants.mysqldf.format(cal.getTime());
			cal.add(Calendar.DAY_OF_WEEK, 6);
			endCurWeek = IdosConstants.mysqldf.format(cal.getTime());
			cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			cal.add(Calendar.DAY_OF_WEEK, -7);
			startPrevWeek = IdosConstants.mysqldf.format(cal.getTime());
			cal.add(Calendar.DAY_OF_WEEK, 6);
			endPrevWeek = IdosConstants.mysqldf.format(cal.getTime());
			ArrayNode data = result.putArray("data");
			ObjectNode dataRow = null;
			StringBuilder query = new StringBuilder();
			double amountAlloted = 0.0, amountDeducted = 0.0, curAmount = 0.0;
			List<BranchSpecifics> branchSpecific = Collections.emptyList();
			List<Branch> branches = user.getOrganization().getBranches();
			double cashExpenseAmt = 0.0, creditExpenseAmt = 0.0;
			for (Branch branch : branches) {
				if (null != branch && null != branch.getId()) {
					dataRow = Json.newObject();
					query.delete(0, query.length());
					query.append("select SUM(obj.budgetTotal) from BranchSpecifics obj WHERE obj.organization  =  ")
							.append(user.getOrganization().getId());
					query.append(" and obj.presentStatus=1 and obj.branch = ").append(branch.getId());
					branchSpecific = genericDAO.executeSimpleQuery(query.toString(), entityManager);
					if (branchSpecific.size() > 0) {
						Object val = branchSpecific.get(0);
						curAmount = (null == val) ? 0.0 : Double.parseDouble(String.valueOf(val));
					}
					query.delete(0, query.length());
					query.append(
							"select SUM(obj.budgetDeductedTotal) from BranchSpecifics obj WHERE obj.organization  =  ")
							.append(user.getOrganization().getId());
					query.append(" and obj.presentStatus=1 and obj.branch = ").append(branch.getId());
					branchSpecific = genericDAO.executeSimpleQuery(query.toString(), entityManager);
					if (branchSpecific.size() > 0) {
						Object val = branchSpecific.get(0);
						amountDeducted = (null == val) ? 0.0 : Double.parseDouble(String.valueOf(val));
					}
					curAmount = Double.parseDouble(IdosConstants.decimalFormat.format(curAmount))
							- Double.parseDouble(IdosConstants.decimalFormat.format(amountDeducted));
					cashExpenseAmt = getCashCreditAmountForBranch(user, branch.getId(), user.getOrganization().getId(),
							1, 1, entityManager, startCurWeek, endCurWeek, startPrevWeek, endPrevWeek);
					creditExpenseAmt = getCashCreditAmountForBranch(user, branch.getId(),
							user.getOrganization().getId(), 2, 1, entityManager, startCurWeek, endCurWeek,
							startPrevWeek, endPrevWeek);
					curAmount += (cashExpenseAmt + creditExpenseAmt);
					if (1 == graphType) {
						amountAlloted = curAmount;
					} else if (2 == graphType || 3 == graphType) {
						cashExpenseAmt = getCashCreditAmountForBranch(user, branch.getId(),
								user.getOrganization().getId(), 1, 2, entityManager, startCurWeek, endCurWeek,
								startPrevWeek, endPrevWeek);
						creditExpenseAmt = getCashCreditAmountForBranch(user, branch.getId(),
								user.getOrganization().getId(), 2, 2, entityManager, startCurWeek, endCurWeek,
								startPrevWeek, endPrevWeek);
						amountAlloted = curAmount + (cashExpenseAmt + creditExpenseAmt);
					}
					if (3 == graphType) {
						amountAlloted = curAmount - amountAlloted;
					}
					dataRow.put("amount", amountAlloted);
					dataRow.put("branch", branch.getName());
					dataRow.put(branch.getName(), amountAlloted);
					data.add(dataRow);
				}
			}
			result.put("result", true);
			result.remove("message");
		}
		return result;
	}

	public ObjectNode getProjectTotalRecieveablesPayables(final Users user, final EntityManager entityManager,
			final int dashboardType, final int graphType) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		if (null == user) {
			result.put("message", "Not a valid request. Please login again to continue.");
		} else {
			ArrayNode data = result.putArray("data");
			ObjectNode dataRow = null;
			String startCurWeek = null, endCurWeek = null, startPrevWeek = null, endPrevWeek = null;
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			startCurWeek = IdosConstants.mysqldf.format(cal.getTime());
			cal.add(Calendar.DAY_OF_WEEK, 6);
			endCurWeek = IdosConstants.mysqldf.format(cal.getTime());
			cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			cal.add(Calendar.DAY_OF_WEEK, -7);
			startPrevWeek = IdosConstants.mysqldf.format(cal.getTime());
			cal.add(Calendar.DAY_OF_WEEK, 6);
			endPrevWeek = IdosConstants.mysqldf.format(cal.getTime());
			double amount = 0.0, curAmount = 0.0;
			StringBuilder query = new StringBuilder();
			List<Transaction> cashTransaction = Collections.emptyList();
			List<Project> projects = user.getOrganization().getProjects();
			for (Project project : projects) {
				if (null != project && null != project.getId()) {
					dataRow = Json.newObject();
					query.delete(0, query.length());
					if (6 == dashboardType) {
						query.append(
								"select SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionProject = ")
								.append(project.getId());
					} else if (7 == dashboardType) {
						query.append(
								"select SUM(obj.vendorNetPayment) from Transaction obj WHERE obj.transactionProject = ")
								.append(project.getId());
					}
					query.append(" and obj.transactionBranchOrganization = ").append(user.getOrganization().getId());
					query.append(" and obj.transactionStatus  =  'Accounted' and obj.presentStatus=1");
					if (6 == dashboardType) {
						query.append(" AND obj.transactionPurpose  =  2");
					} else if (7 == dashboardType) {
						query.append(" AND obj.transactionPurpose  =  4");
					}
					if (1 == graphType || 3 == graphType) {
						query.append(" and obj.transactionDate between '").append(startCurWeek).append("' and '")
								.append(endCurWeek).append("'");
					} else if (2 == graphType) {
						query.append(" and obj.transactionDate between '").append(startPrevWeek).append("' and '")
								.append(endPrevWeek).append("'");
					}
					cashTransaction = genericDAO.executeSimpleQuery(query.toString(), entityManager);
					curAmount = getCashCreditAmountForProject(project.getId(), user.getOrganization().getId(), 3, 1,
							entityManager, startCurWeek, endCurWeek, startPrevWeek, endPrevWeek);
					if (null != cashTransaction) {
						Object val = cashTransaction.get(0);
						curAmount = (null == val) ? 0.0
								: Double.parseDouble(IdosConstants.decimalFormat.format(curAmount))
										- Double.parseDouble(IdosConstants.decimalFormat.format(val));
					}
					amount = curAmount;
					if (3 == graphType) {
						query.delete(0, query.length());
						if (6 == dashboardType) {
							query.append(
									"select SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionProject = ")
									.append(project.getId());
						} else if (7 == dashboardType) {
							query.append(
									"select SUM(obj.vendorNetPayment) from Transaction obj WHERE obj.transactionProject = ")
									.append(project.getId());
						}
						query.append(" and obj.transactionBranchOrganization = ")
								.append(user.getOrganization().getId());
						query.append(" and obj.transactionStatus  =  'Accounted' and obj.presentStatus=1");
						if (6 == dashboardType) {
							query.append(" AND obj.transactionPurpose  =  2");
						} else if (7 == dashboardType) {
							query.append(" AND obj.transactionPurpose  =  4");
						}
						query.append(" and obj.transactionDate between '").append(startPrevWeek).append("' and '")
								.append(startPrevWeek).append("'");
						cashTransaction = genericDAO.executeSimpleQuery(query.toString(), entityManager);
						curAmount = getCashCreditAmountForProject(project.getId(), user.getOrganization().getId(), 3, 1,
								entityManager, startCurWeek, endCurWeek, startPrevWeek, endPrevWeek);
						if (null != cashTransaction) {
							Object val = cashTransaction.get(0);
							curAmount = (null == val) ? 0.0
									: Double.parseDouble(IdosConstants.decimalFormat.format(curAmount))
											- Double.parseDouble(IdosConstants.decimalFormat.format(val));
						}
						amount -= curAmount;
					}
					dataRow.put("amount", amount);
					dataRow.put("branch", project.getName());
					dataRow.put(project.getName(), amount);
					data.add(dataRow);
				}
			}
			result.put("result", true);
			result.remove("message");
		}
		return result;
	}

	public ObjectNode getTotalRecieveablesPayables(final Users user, final EntityManager entityManager,
			final int dashboardType, final int graphType) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		if (null == user) {
			result.put("message", "Not a valid request. Please login again to continue.");
		} else {
			ArrayNode data = result.putArray("data");
			ObjectNode dataRow = null;
			String startCurWeek = null, endCurWeek = null, startPrevWeek = null, endPrevWeek = null;
			// Now we show data for this month and prev month instead of this week and prev
			// week: Changed in March 2016
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
			startCurWeek = IdosConstants.mysqldf.format(cal.getTime());
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			endCurWeek = IdosConstants.mysqldf.format(cal.getTime());
			Calendar newcal = Calendar.getInstance();
			newcal.add(Calendar.MONTH, -1);
			newcal.set(Calendar.DAY_OF_MONTH, newcal.getActualMinimum(Calendar.DAY_OF_MONTH));
			startPrevWeek = IdosConstants.mysqldf.format(newcal.getTime());
			newcal.set(Calendar.DAY_OF_MONTH, newcal.getActualMaximum(Calendar.DAY_OF_MONTH));
			endPrevWeek = IdosConstants.mysqldf.format(newcal.getTime());

			double amount = 0.0, curAmount = 0.0;
			StringBuilder query = new StringBuilder();
			List<Transaction> cashTransaction = Collections.emptyList();
			List<Branch> branches = user.getOrganization().getBranches();
			for (Branch branch : branches) {
				if (null != branch && null != branch.getId()) {
					dataRow = Json.newObject();
					query.delete(0, query.length());
					if (6 == dashboardType) {
						query.append(
								"select SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranch = ")
								.append(branch.getId());
					} else if (7 == dashboardType) {
						query.append(
								"select SUM(obj.vendorNetPayment) from Transaction obj WHERE obj.transactionBranch = ")
								.append(branch.getId());
					}
					query.append(" and obj.transactionBranchOrganization = ").append(user.getOrganization().getId());
					query.append(" and obj.transactionStatus  =  'Accounted' and obj.presentStatus=1");
					if (6 == dashboardType) {
						query.append(" AND obj.transactionPurpose  =  2");
					} else if (7 == dashboardType) {
						query.append(" AND obj.transactionPurpose  =  4");
					}
					if (1 == graphType || 3 == graphType) {
						query.append(" and obj.transactionDate between '").append(startCurWeek).append("' and '")
								.append(endCurWeek).append("'");
					} else if (2 == graphType) {
						query.append(" and obj.transactionDate between '").append(startPrevWeek).append("' and '")
								.append(endPrevWeek).append("'");
					}
					cashTransaction = genericDAO.executeSimpleQuery(query.toString(), entityManager);
					curAmount = getCashCreditAmountForBranch(user, branch.getId(), user.getOrganization().getId(), 3, 1,
							entityManager, startCurWeek, endCurWeek, startPrevWeek, endPrevWeek);
					if (null != cashTransaction) {
						Object val = cashTransaction.get(0);
						curAmount = (null == val) ? 0.0
								: Double.parseDouble(IdosConstants.decimalFormat.format(curAmount))
										- Double.parseDouble(IdosConstants.decimalFormat.format(val));
					}
					amount = curAmount;
					if (3 == graphType) {
						query.delete(0, query.length());
						if (6 == dashboardType) {
							query.append(
									"select SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranch = ")
									.append(branch.getId());
						} else if (7 == dashboardType) {
							query.append(
									"select SUM(obj.vendorNetPayment) from Transaction obj WHERE obj.transactionBranch = ")
									.append(branch.getId());
						}
						query.append(" and obj.transactionBranchOrganization = ")
								.append(user.getOrganization().getId());
						query.append(" and obj.transactionStatus  =  'Accounted' and obj.presentStatus=1");
						if (6 == dashboardType) {
							query.append(" AND obj.transactionPurpose  =  2");
						} else if (7 == dashboardType) {
							query.append(" AND obj.transactionPurpose  =  4");
						}
						query.append(" and obj.transactionDate between '").append(startPrevWeek).append("' and '")
								.append(startPrevWeek).append("'");
						cashTransaction = genericDAO.executeSimpleQuery(query.toString(), entityManager);
						curAmount = getCashCreditAmountForBranch(user, branch.getId(), user.getOrganization().getId(),
								3, 1, entityManager, startCurWeek, endCurWeek, startPrevWeek, endPrevWeek);
						if (null != cashTransaction) {
							Object val = cashTransaction.get(0);
							curAmount = (null == val) ? 0.0
									: Double.parseDouble(IdosConstants.decimalFormat.format(curAmount))
											- Double.parseDouble(IdosConstants.decimalFormat.format(val));
						}
						amount -= curAmount;
					}
					dataRow.put("amount", amount);
					dataRow.put("branch", branch.getName());
					dataRow.put(branch.getName(), amount);
					data.add(dataRow);
				}
			}
			result.put("result", true);
			result.remove("message");
		}
		return result;
	}

	/**
	 * This method is not getting used
	 * 
	 * @param result
	 * @param branch
	 * @param user
	 * @param entityManager
	 * @return
	 */
	public ObjectNode customerwiseProformaInvoice(ObjectNode result, Branch branch, Users user,
			EntityManager entityManager) {
		ArrayNode an = result.putArray("customerWiseProformaInvoicesValues");
		String[] arr = DateUtil.getFinancialDate(user);
		String finStartDate = arr[0];
		String finEndDate = arr[1];
		StringBuilder branchcreditincomesbquery = new StringBuilder(
				"select obj from Transaction obj WHERE obj.transactionBranch = '" + branch.getId()
						+ "' and obj.transactionBranchOrganization = '" + branch.getOrganization().getId()
						+ "' AND obj.transactionPurpose = 28 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionVendorCustomer IS NOT NULL and obj.transactionDate  between '"
						+ finStartDate + "' and '" + finEndDate + "' GROUP BY obj.transactionVendorCustomer");

		List<Transaction> bnchcustcreditincometxn = genericDAO.executeSimpleQuery(branchcreditincomesbquery.toString(),
				entityManager);
		if (!bnchcustcreditincometxn.isEmpty() && bnchcustcreditincometxn.size() > 0) {
			result.put("result", true);
			for (Transaction maintxn : bnchcustcreditincometxn) {
				Double amount = 0.0;
				branchcreditincomesbquery.delete(0, branchcreditincomesbquery.length());
				branchcreditincomesbquery.append("select obj from Transaction obj WHERE obj.transactionBranch = '"
						+ branch.getId() + "' and obj.transactionBranchOrganization = '"
						+ branch.getOrganization().getId() + "' AND obj.transactionVendorCustomer = '"
						+ maintxn.getTransactionVendorCustomer().getId()
						+ "' AND obj.transactionPurpose = 28 and  obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
						+ finStartDate + "' and '" + finEndDate + "'");
				List<Transaction> bnchcustcreditincometxncust = genericDAO
						.executeSimpleQuery(branchcreditincomesbquery.toString(), entityManager);
				for (Transaction txn : bnchcustcreditincometxncust) {
					if (txn.getCustomerNetPayment() != null) {
						amount += txn.getNetAmount() - txn.getCustomerNetPayment();
					} else {
						amount += txn.getNetAmount();
					}
				}
				if (amount > 0.0) {
					ObjectNode row = Json.newObject();
					row.put("id", maintxn.getTransactionVendorCustomer().getId());
					row.put("branchName", branch.getName());
					row.put("customerName", maintxn.getTransactionVendorCustomer().getName());
					row.put("netAmount", amount);
					row.put("txnModelFor", "proformaInvoice");
					row.put("branchID", branch.getId());
					an.add(row);
				}
			}
			result.put("result", true);
		}
		return result;
	}

	@Override
	public ObjectNode recPayablesOpeningBalAndCurrentYearTotal(JsonNode json, String tabElement, Users user,
			EntityManager entityManager) {
		ObjectNode result = Json.newObject();
		ArrayNode an = result.putArray("recPayablesOpeningBalAndCurrentYearTotal");
		String[] arr = DateUtil.getFinancialDate(user);
		String finStartDate = arr[0];
		String finEndDate = arr[1];
		ObjectNode row = Json.newObject();
		try {
			String currDashboardToDate = json.findValue("currDashboardToDate") != null
					? json.findValue("currDashboardToDate").asText()
					: null;
			if (currDashboardToDate != null && !"".equals(currDashboardToDate)) {
				finEndDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(currDashboardToDate));
			}
			// journal entry data
			Map provisionEntries = new HashMap();
			Map allSpecifcsAmtData = new HashMap();
			Map vendorPayablesData = new HashMap();
			Map custReceivablesData = new HashMap();
			ProvisionJournalEntryService jourObj = new ProvisionJournalEntryServiceImpl();
			ObjectNode journalResult = jourObj.getDashboardProvisionEntriesDataBranchWise(finStartDate, finEndDate,
					provisionEntries, allSpecifcsAmtData, vendorPayablesData, custReceivablesData, user, entityManager);
			Double accountReceivables = new Double(provisionEntries.get("totalCustReceivables").toString());
			Double accountPayables = new Double(provisionEntries.get("totalVendPayables").toString());
			ObjectNode servObj = totalReceivablePayables(currDashboardToDate, user, entityManager); // we are
																									// calculating total
																									// receivables/paybles
																									// in this mehtod
			if (tabElement.equalsIgnoreCase("accountsReceivablesAllBranches")) {
				// total customer wise opening balances which are still unpaid
				double totalCustOpeningBal = custVendorOpeningBalanceTotal(IdosConstants.CUSTOMER, user, entityManager);
				row.put("openingBalance", IdosConstants.decimalFormat.format(totalCustOpeningBal));
				// sum of total receivables this year)
				if (servObj.get("accountsReceivables") != null) {
					accountReceivables = accountReceivables + servObj.get("accountsReceivables").asDouble();
				}
				row.put("netRecievablePayablesCurrentYear", IdosConstants.decimalFormat.format(accountReceivables));
				row.put("totalOBAndNetRecPay", totalCustOpeningBal + accountReceivables);
			} else if (tabElement.equalsIgnoreCase("accountsPayablesAllBranches")) {
				// total vendor wise opening balances which are still unpaid
				double totalVendOpeningBal = custVendorOpeningBalanceTotal(IdosConstants.VENDOR, user, entityManager);
				row.put("openingBalance", IdosConstants.decimalFormat.format(totalVendOpeningBal));
				// Total payables
				if (servObj.get("accountsPayables") != null) {
					accountPayables = accountPayables + servObj.get("accountsPayables").asDouble();
				}
				row.put("netRecievablePayablesCurrentYear", IdosConstants.decimalFormat.format(accountPayables));
				row.put("totalOBAndNetRecPay", totalVendOpeningBal + accountPayables);
			}
			an.add(row);
			result.put("result", true);
		} catch (Exception ex) {

			log.log(Level.SEVERE, ex.getMessage());
		}
		return result;
	}

	private Double custVendorOpeningBalanceTotal(int type, Users user, EntityManager entityManager) {
		Double totalOpeningBal = 0.0;
		StringBuilder sumOBquery = new StringBuilder(
				"select SUM(obj.totalOpeningBalance) from Vendor obj WHERE obj.organization = '"
						+ user.getOrganization().getId() + "' AND obj.type = '" + type
						+ "' and obj.presentStatus  = 1");
		List<Transaction> sumOBquerytxn = genericDAO.executeSimpleQuery(sumOBquery.toString(), entityManager);
		if (sumOBquerytxn.size() > 0) {
			Object val = sumOBquerytxn.get(0);
			if (val != null) {
				totalOpeningBal = Double.valueOf(val.toString());
			}
		}
		return totalOpeningBal;
	}

	@Override
	public ObjectNode custVendOpeningBalanceBreakup(ObjectNode result, String tabElement, Users user,
			EntityManager entityManager) {
		ArrayNode an = result.putArray("recPayablesOpeningBalBreakup");
		String[] arr = DateUtil.getFinancialDate(user);
		String finStartDate = arr[0];
		String finEndDate = arr[1];
		int type = 1;
		if (tabElement.equalsIgnoreCase("accountsReceivablesAllBranches")) {
			type = IdosConstants.CUSTOMER;
		} else if (tabElement.equalsIgnoreCase("accountsPayablesAllBranches")) {
			type = IdosConstants.VENDOR;
		}
		// customer/vendor list of Opening Balances
		StringBuilder OBquery = new StringBuilder("");
		OBquery.append("select obj from Vendor obj WHERE obj.organization = '" + user.getOrganization().getId()
				+ "' AND obj.type = '" + type + "' and obj.presentStatus  = 1");
		List<Vendor> OBquerytxn = genericDAO.executeSimpleQuery(OBquery.toString(), entityManager);
		for (Vendor vendCust : OBquerytxn) {
			if (vendCust.getTotalOpeningBalance() != null) {
				ObjectNode row = Json.newObject();
				row.put("customerName", vendCust.getName());
				row.put("openingBal", IdosConstants.decimalFormat.format(vendCust.getTotalOpeningBalance()));
				an.add(row);
			}
		}
		result.put("result", true);
		return result;
	}

	public ObjectNode totalReceivablePayables(String currentToDate, Users user, EntityManager entityManager) {
		ObjectNode row = Json.newObject();
		try {
			Map<String, Object> criterias = new HashMap<String, Object>();
			Organization org = user.getOrganization();
			String finStartDate = null;
			String finStDt = null;
			StringBuilder startYear = null;
			String finEndDate = null;
			String finEndDt = null;
			StringBuilder endYear = null;
			Calendar currentDate = Calendar.getInstance();
			if (currentToDate != null && currentToDate != "") {
				currentDate.setTime(IdosConstants.idosdf.parse(currentToDate));
				finEndDate = StaticController.mysqldf.format(StaticController.idosdf.parse(currentToDate));
			} else {
				currentToDate = Calendar.getInstance().getTime().toString();
				finEndDate = StaticController.mysqldf.format(Calendar.getInstance().getTime());
			}
			int currentMonth = currentDate.get(Calendar.MONTH) + 1;
			int finStartMonth = 4;
			int finEndMonth = 3;
			if (org.getFinancialStartDate() != null) {
				finStartMonth = org.getFinancialStartDate().getMonth() + 1;
			}
			if (org.getFinancialEndDate() != null) {
				finEndMonth = org.getFinancialEndDate().getMonth() + 1;
			}
			if (currentMonth < finStartMonth) {
				startYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - 1));
				endYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
			} else {
				startYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
				endYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) + 1));
			}
			if (org.getFinancialStartDate() != null && !org.getFinancialStartDate().equals("")) {
				finStDt = StaticController.idosmdtdf.format(org.getFinancialStartDate()) + "," + startYear;
				finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
			} else {
				finStDt = "Apr 01" + "," + startYear;
				finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
			}

			/*
			 * if(org.getFinancialEndDate() != null &&
			 * !org.getFinancialEndDate().equals("")){
			 * finEndDt =
			 * StaticController.idosmdtdf.format(org.getFinancialEndDate())+","+endYear;
			 * finEndDate =
			 * StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
			 * }else{
			 * finEndDt = "Mar 31"+","+endYear;
			 * finEndDate =
			 * StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
			 * }
			 */
			Double accountsReceivables = 0.0, accountsPayables = 0.0, accountsReceivablesOverdues = 0.0,
					accountsPayablesOverdues = 0.0;

			StringBuilder branchcreditincomesbquery = new StringBuilder("");
			branchcreditincomesbquery
					.append("select obj from Transaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' AND obj.transactionPurpose = 2 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and (obj.paymentStatus = 'NOT-PAID' or obj.paymentStatus = 'PARTLY-PAID') and obj.transactionDate  between '"
							+ finStartDate + "' and '" + finEndDate + "'");
			List<Transaction> bnchcreditincometxn = genericDAO.executeSimpleQuery(branchcreditincomesbquery.toString(),
					entityManager);
			Vendor cust = null;
			for (Transaction cdtincmtxn : bnchcreditincometxn) {
				if (cdtincmtxn.getCustomerNetPayment() != null) {
					accountsReceivables += cdtincmtxn.getNetAmount() - cdtincmtxn.getCustomerNetPayment();
				} else if (cdtincmtxn.getNetAmount() != null) {
					accountsReceivables += cdtincmtxn.getNetAmount();
				}
				if (cdtincmtxn.getTransactionVendorCustomer() != null) {
					cust = cdtincmtxn.getTransactionVendorCustomer();
					if ((cust.getPurchaseType() == 0 || cust.getPurchaseType() == 2)
							&& cust.getDaysForCredit() != null) {
						int daysdiff = (int) (Calendar.getInstance().getTimeInMillis()
								- cdtincmtxn.getTransactionDate().getTime() / 1000 * 60 * 60 * 24);
						if (daysdiff > cust.getDaysForCredit()) {
							if (cdtincmtxn.getCustomerNetPayment() != null) {
								accountsReceivablesOverdues += cdtincmtxn.getNetAmount()
										- cdtincmtxn.getCustomerNetPayment();
							} else {
								accountsReceivablesOverdues += cdtincmtxn.getNetAmount();
							}
						}
					}
				}
			}
			// sales retrun should be subtracted from totalSales
			StringBuilder newsbquery = new StringBuilder("");
			newsbquery
					.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' AND obj.transactionPurpose = 12 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
							+ finStartDate + "' and '" + finEndDate + "'");
			List<Transaction> creditincometxn = genericDAO.executeSimpleQuery(newsbquery.toString(), entityManager);
			if (creditincometxn.size() > 0) {
				Object val = creditincometxn.get(0);
				if (val != null) {
					accountsReceivables = accountsReceivables - IdosUtil.convertStringToDouble(val.toString());
				}
			}
			StringBuilder branchcreditexpensebquery = new StringBuilder("");
			branchcreditexpensebquery
					.append("select obj from Transaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' AND obj.transactionPurpose = 4 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and (obj.paymentStatus = 'NOT-PAID' or obj.paymentStatus = 'PARTLY-PAID') and obj.transactionDate  between '"
							+ finStartDate + "' and '" + finEndDate + "'");
			List<Transaction> bnchcreditexpensetxn = genericDAO.executeSimpleQuery(branchcreditexpensebquery.toString(),
					entityManager);
			Vendor vend = null;
			for (Transaction cdtexptxn : bnchcreditexpensetxn) {
				if (cdtexptxn.getNetAmount() != null) {
					if (cdtexptxn.getVendorNetPayment() != null) {
						accountsPayables += cdtexptxn.getNetAmount() - cdtexptxn.getVendorNetPayment();
					} else {
						accountsPayables += cdtexptxn.getNetAmount();
					}
				}
				if (cdtexptxn.getTransactionVendorCustomer() != null) {
					vend = cdtexptxn.getTransactionVendorCustomer();
					if ((vend.getPurchaseType() == 0 || vend.getPurchaseType() == 2)
							&& vend.getDaysForCredit() != null) {
						int daysdiff = (int) (Calendar.getInstance().getTimeInMillis()
								- cdtexptxn.getTransactionDate().getTime() / 1000 * 60 * 60 * 24);
						if (daysdiff > vend.getDaysForCredit()) {
							if (cdtexptxn.getVendorNetPayment() != null) {
								accountsPayablesOverdues += cdtexptxn.getNetAmount() - cdtexptxn.getVendorNetPayment();
							} else {
								accountsPayablesOverdues += cdtexptxn.getNetAmount();
							}
						}
					}
				}
			}
			// Purchase return should be subtracted from totalexpense
			StringBuilder cdtsbquery = new StringBuilder("");
			cdtsbquery
					.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization = '"
							+ user.getOrganization().getId()
							+ "' AND obj.transactionPurpose = 13 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
							+ finStartDate + "' and '" + finEndDate + "'");
			List<Transaction> creditexpensetxn = genericDAO.executeSimpleQuery(cdtsbquery.toString(), entityManager);
			if (creditexpensetxn.size() > 0) {
				Object val = creditexpensetxn.get(0);
				if (val != null) {
					accountsPayables = accountsPayables - new Double(val.toString());
				}
			}
			row.put("accountsReceivables", IdosConstants.decimalFormat.format(accountsReceivables));
			row.put("accountsPayables", IdosConstants.decimalFormat.format(accountsPayables));
			row.put("accountsReceivablesOverdues", IdosConstants.decimalFormat.format(accountsReceivablesOverdues));
			row.put("accountsPayablesOverdues", IdosConstants.decimalFormat.format(accountsPayablesOverdues));
		} catch (Exception ex) {
			log.log(Level.SEVERE, "error", ex);
		}
		return row;
	}

	@Override
	public ObjectNode branchWiseReceivablePayablesGraphData(JsonNode json, String tabElement, Users user,
			EntityManager entityManager) {
		ObjectNode result = Json.newObject();
		ArrayNode an = result.putArray("branchWiseRecPayGraphData");
		ProvisionJournalEntryService jourObj = new ProvisionJournalEntryServiceImpl();
		try {
			String[] arr = DateUtil.getFinancialDate(user);
			String finStartDate = arr[0];
			String finEndDate = arr[1];
			String currDate = null;
			String currDashboardToDate = json.findValue("currDashboardToDate") != null
					? json.findValue("currDashboardToDate").asText()
					: null;
			// String oneEightyDaysBackDate = DateUtil.returnOneEightyDaysBackDate();
			if (currDashboardToDate != null && !"".equals(currDashboardToDate)) {
				currDate = finEndDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(currDashboardToDate));
			} else {
				currDate = IdosConstants.mysqldf.format(Calendar.getInstance().getTime());
				// Date currDateTime =
				// mysqldf.parse(mysqldf.format(Calendar.getInstance().getTime()));
			}
			Date currDateTime = IdosConstants.mysqldf.parse(currDate);
			Date under0to30daysDatedate = DateUtil.returnPrevOneMonthDateDate(currDateTime);
			String under0to30daysDate = IdosConstants.mysqldf.format(under0to30daysDatedate);
			Date under31to60daysDatedate = DateUtil.returnPrevOneMonthDateDate(under0to30daysDatedate);
			String under31to60daysDate = IdosConstants.mysqldf.format(under31to60daysDatedate);
			Date under61to90daysDatedate = DateUtil.returnPrevOneMonthDateDate(under31to60daysDatedate);
			String under61to90daysDate = IdosConstants.mysqldf.format(under61to90daysDatedate);
			Date under91To180daysDatedate = DateUtil.returnPrevThreeMonthDateDate(under61to90daysDatedate);
			String under91To180daysDate = IdosConstants.mysqldf.format(under91To180daysDatedate);
			int trnPurpose = 2;
			// amount paybles to vendors as per branch
			ObjectNode row1 = Json.newObject();
			if (tabElement != null) {
				if (tabElement.equals("accountsPayablesAllBranches")
						|| tabElement.equals("payableOverduesAllBranches")) {
					trnPurpose = 4;
				} else {
					trnPurpose = 2; // for receivables
				}
			}
			Map criterias = new HashMap();
			criterias.clear();
			criterias.put("presentStatus", 1);
			criterias.put("organization.id", user.getOrganization().getId());
			List<Branch> availableBranches = genericDAO.findByCriteria(Branch.class, criterias, entityManager);
			for (Branch branch : availableBranches) {
				ObjectNode row = Json.newObject();
				row.put("branchId", branch.getId());
				row.put("branchName", branch.getName());
				// receivables/paybles only Overdue transactions
				if (tabElement.equals("payableOverduesAllBranches")
						|| tabElement.equals("receivableOverduesAllBranches")) {
					double overdueamount = 0.0;
					overdueamount = getOverdueAmtForGivenBranchAndPeriod(branch, trnPurpose, entityManager,
							under0to30daysDate, currDate);
					row.put("amt0to30days", overdueamount);
					overdueamount = getOverdueAmtForGivenBranchAndPeriod(branch, trnPurpose, entityManager,
							under31to60daysDate, under0to30daysDate);
					row.put("amt31to60days", overdueamount);
					overdueamount = getOverdueAmtForGivenBranchAndPeriod(branch, trnPurpose, entityManager,
							under61to90daysDate, under31to60daysDate);
					row.put("amt61to90days", overdueamount);
					overdueamount = getOverdueAmtForGivenBranchAndPeriod(branch, trnPurpose, entityManager,
							under91To180daysDate, under61to90daysDate);
					row.put("amt91to180days", overdueamount);
					overdueamount = getOverdueAmtForGivenBranchAndPeriod(branch, trnPurpose, entityManager,
							finStartDate, under91To180daysDate);
					row.put("amtOver180days", overdueamount);
				} else {// receivables/payables all transctions
						// 0-30days :current date till last 30days i.e. say from 5 Oct 2016- 5 sept 2016
					StringBuilder branchcreditexpensebquery = new StringBuilder("");
					branchcreditexpensebquery.append(
							"select SUM(obj.netAmount),SUM(obj.vendorNetPayment),SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranch = '"
									+ branch.getId() + "' and obj.transactionBranchOrganization = '"
									+ user.getOrganization().getId() + "' AND obj.transactionPurpose = '" + trnPurpose
									+ "' and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and (obj.paymentStatus = 'NOT-PAID' or obj.paymentStatus = 'PARTLY-PAID') and obj.transactionDate  between '"
									+ under0to30daysDate + "' and '" + currDate + "'");
					// branchcreditexpensebquery.append("select
					// SUM(obj.netAmount),SUM(obj.vendorNetPayment),SUM(obj.customerNetPayment) from
					// Transaction obj WHERE obj.transactionBranch = '"+branch.getId()+"' and
					// obj.transactionBranchOrganization = '"+branch.getOrganization().getId()+"'
					// AND obj.transactionPurpose = '"+trnPurpose+"' and obj.transactionStatus =
					// 'Accounted' and (obj.paymentStatus = 'NOT-PAID' or obj.paymentStatus =
					// 'PARTLY-PAID')");
					List<Object[]> txnLists = entityManager.createQuery(branchcreditexpensebquery.toString())
							.getResultList();
					// get provision entry data
					Map provisionEntries = new HashMap();
					Map allSpecifcsAmtData = new HashMap();
					Map vendorPayablesData = new HashMap();
					Map custReceivablesData = new HashMap();
					Map branchMap = jourObj.getDashboardProvisionEntriesDataForBranch(under0to30daysDate, currDate,
							user, allSpecifcsAmtData, vendorPayablesData, custReceivablesData, branch, entityManager);
					double amount = getRecPayNetAmt(txnLists, trnPurpose, branchMap);
					row.put("amt0to30days", amount);
					// 31-60days
					branchcreditexpensebquery = new StringBuilder("");
					branchcreditexpensebquery.append(
							"select SUM(obj.netAmount),SUM(obj.vendorNetPayment),SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranch = '"
									+ branch.getId() + "' and obj.transactionBranchOrganization = '"
									+ user.getOrganization().getId() + "' AND obj.transactionPurpose = '" + trnPurpose
									+ "' and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and (obj.paymentStatus = 'NOT-PAID' or obj.paymentStatus = 'PARTLY-PAID') and obj.transactionDate  between '"
									+ under31to60daysDate + "' and '" + under0to30daysDate + "'");
					txnLists = entityManager.createQuery(branchcreditexpensebquery.toString()).getResultList();
					branchMap = jourObj.getDashboardProvisionEntriesDataForBranch(under31to60daysDate,
							under0to30daysDate, user, allSpecifcsAmtData, vendorPayablesData, custReceivablesData,
							branch, entityManager);
					amount = getRecPayNetAmt(txnLists, trnPurpose, branchMap);
					row.put("amt31to60days", amount);
					// 61-90days
					branchcreditexpensebquery = new StringBuilder("");
					branchcreditexpensebquery.append(
							"select SUM(obj.netAmount),SUM(obj.vendorNetPayment),SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranch = '"
									+ branch.getId() + "' and obj.transactionBranchOrganization = '"
									+ user.getOrganization().getId() + "' AND obj.transactionPurpose = '" + trnPurpose
									+ "' and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and (obj.paymentStatus = 'NOT-PAID' or obj.paymentStatus = 'PARTLY-PAID') and obj.transactionDate  between '"
									+ under61to90daysDate + "' and '" + under31to60daysDate + "'");
					txnLists = entityManager.createQuery(branchcreditexpensebquery.toString()).getResultList();
					branchMap = jourObj.getDashboardProvisionEntriesDataForBranch(under61to90daysDate,
							under31to60daysDate, user, allSpecifcsAmtData, vendorPayablesData, custReceivablesData,
							branch, entityManager);
					amount = getRecPayNetAmt(txnLists, trnPurpose, branchMap);
					row.put("amt61to90days", amount);
					// 91-180days
					branchcreditexpensebquery = new StringBuilder("");
					branchcreditexpensebquery.append(
							"select SUM(obj.netAmount),SUM(obj.vendorNetPayment),SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranch = '"
									+ branch.getId() + "' and obj.transactionBranchOrganization = '"
									+ user.getOrganization().getId() + "' AND obj.transactionPurpose = '" + trnPurpose
									+ "' and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and (obj.paymentStatus = 'NOT-PAID' or obj.paymentStatus = 'PARTLY-PAID') and obj.transactionDate  between '"
									+ under91To180daysDate + "' and '" + under61to90daysDate + "'");
					txnLists = entityManager.createQuery(branchcreditexpensebquery.toString()).getResultList();
					branchMap = jourObj.getDashboardProvisionEntriesDataForBranch(under91To180daysDate,
							under61to90daysDate, user, allSpecifcsAmtData, vendorPayablesData, custReceivablesData,
							branch, entityManager);
					amount = getRecPayNetAmt(txnLists, trnPurpose, branchMap);
					row.put("amt91to180days", amount);
					// over 180days
					branchcreditexpensebquery = new StringBuilder("");
					branchcreditexpensebquery.append(
							"select SUM(obj.netAmount),SUM(obj.vendorNetPayment),SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranch = '"
									+ branch.getId() + "' and obj.transactionBranchOrganization = '"
									+ user.getOrganization().getId() + "' AND obj.transactionPurpose = '" + trnPurpose
									+ "' and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and (obj.paymentStatus = 'NOT-PAID' or obj.paymentStatus = 'PARTLY-PAID') and obj.transactionDate<'"
									+ under91To180daysDate + "'");
					txnLists = entityManager.createQuery(branchcreditexpensebquery.toString()).getResultList();
					branchMap = jourObj.getDashboardProvisionEntriesDataForBranch(finStartDate, under91To180daysDate,
							user, allSpecifcsAmtData, vendorPayablesData, custReceivablesData, branch, entityManager);
					amount = getRecPayNetAmt(txnLists, trnPurpose, branchMap);
					row.put("amtOver180days", amount);
				}
				an.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
		}
		result.put("result", true);
		return result;
	}

	private double getOverdueAmtForGivenBranchAndPeriod(Branch branch, int trnPurpose, EntityManager entityManager,
			String fromDate, String toDate) {
		StringBuilder query = new StringBuilder("");
		query.append("select obj from Transaction obj WHERE obj.transactionBranch = '" + branch.getId()
				+ "' and obj.transactionBranchOrganization = '" + branch.getOrganization().getId()
				+ "' AND obj.transactionPurpose = '" + trnPurpose
				+ "' and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and (obj.paymentStatus = 'NOT-PAID' or obj.paymentStatus = 'PARTLY-PAID') and (obj.transactionVendorCustomer IS NOT NULL and (obj.transactionVendorCustomer.purchaseType = 0 OR obj.transactionVendorCustomer.purchaseType = 2) AND obj.transactionVendorCustomer.daysForCredit IS NOT NULL) and obj.transactionDate  between '"
				+ fromDate + "' and '" + toDate + "'");
		List<Transaction> bnchcustcreditincometxncust = genericDAO.executeSimpleQuery(query.toString(), entityManager);
		double overdueamount = 0.0;
		for (Transaction txn : bnchcustcreditincometxncust) {
			int daysdiff = (int) (Calendar.getInstance().getTimeInMillis()
					- txn.getTransactionDate().getTime() / 1000 * 60 * 60 * 24);
			if (daysdiff > txn.getTransactionVendorCustomer().getDaysForCredit()) {
				if (trnPurpose == 4) {
					if (txn.getVendorNetPayment() != null) {
						overdueamount += txn.getNetAmount() - txn.getVendorNetPayment();
					} else {
						overdueamount += txn.getNetAmount();
					}
				} else {
					if (txn.getCustomerNetPayment() != null) {
						overdueamount += txn.getNetAmount() - txn.getCustomerNetPayment();
					} else {
						overdueamount += txn.getNetAmount();
					}
				}
			}
		}
		return overdueamount;
	}

	private double getRecPayNetAmt(List<Object[]> txnLists, int trnPurpose, Map branchMap) {
		double amount = 0.0;
		if (txnLists.size() > 0) {
			double netAmt = 0.0, vendNetAmt = 0.0, custNetAmt = 0.0;
			for (Object[] val : txnLists) {
				netAmt += val[0] != null ? Double.parseDouble(String.valueOf(val[0])) : 0.0;
				vendNetAmt += val[1] != null ? Double.parseDouble(String.valueOf(val[1])) : 0.0;
				custNetAmt += val[2] != null ? Double.parseDouble(String.valueOf(val[2])) : 0.0;
			}
			if (trnPurpose == 4) {// receivables
				amount = netAmt - vendNetAmt;
				if (branchMap != null && branchMap.containsKey("totalBranchVendPayables")) {
					amount = amount + new Double(branchMap.get("totalBranchVendPayables").toString());
				}
			} else if (trnPurpose == 2) {// netAMt = total amt recivables i.e credit sell done,custNetAmt = purchase
											// returned by the customer
				amount = netAmt - custNetAmt;
				if (branchMap != null && branchMap.containsKey("totalBranchCustReceivables")) {
					amount = amount + new Double(branchMap.get("totalBranchCustReceivables").toString());
				}
			}
		}
		return amount;
	}

	@Override
	public ObjectNode displayBarnchAndPeriodWiseCustVend(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager) {
		ArrayNode an1 = result.putArray("branchAndPeriodInfo");
		ArrayNode an = result.putArray("branchAndPeriodWiseCustBreakup");
		try {
			String tabElement = json.findValue("tabId").asText();
			String branchId = json.findValue("branchId").asText();
			String period = json.findValue("series").asText();
			int type = 1, tranPurpose = 2;
			Branch branch = null;
			if (branchId != null && !branchId.equals("")) {
				branch = Branch.findById(Long.parseLong(branchId));
			}
			ObjectNode row1 = Json.newObject();
			if (tabElement.equalsIgnoreCase("accountsReceivablesAllBranches")
					|| tabElement.equalsIgnoreCase("receivableOverduesAllBranches")) {
				type = IdosConstants.CUSTOMER;
				tranPurpose = 2;
			} else if (tabElement.equalsIgnoreCase("accountsPayablesAllBranches")
					|| tabElement.equalsIgnoreCase("payableOverduesAllBranches")) {
				type = IdosConstants.VENDOR;
				tranPurpose = 4;
			}
			if (tabElement.equalsIgnoreCase("receivableOverduesAllBranches")) {
				row1.put("txnModelFor", "receivableOverduesAllBranches");
			} else if (tabElement.equalsIgnoreCase("payableOverduesAllBranches")) {
				row1.put("txnModelFor", "payableOverduesAllBranches");
			} else if (tabElement.equalsIgnoreCase("accountsReceivablesAllBranches")) {
				row1.put("txnModelFor", "customerReceivables");
			} else if (tabElement.equalsIgnoreCase("accountsPayablesAllBranches")) {
				row1.put("txnModelFor", "vendorPayables");
			}
			row1.put("branchId", branchId);
			row1.put("branchName", branch.getName());
			row1.put("period", period);
			an1.add(row1);
			String[] arr = DateUtil.getFinancialDate(user);
			String periodStartDate = arr[0];
			String periodEndDate = arr[1];
			String currDate = null;
			String currDashboardToDate = json.findValue("currDashboardToDate") != null
					? json.findValue("currDashboardToDate").asText()
					: null;
			// String oneEightyDaysBackDate = DateUtil.returnOneEightyDaysBackDate();
			if (currDashboardToDate != null && !"".equals(currDashboardToDate)) {
				currDate = periodEndDate = IdosConstants.mysqldf
						.format(IdosConstants.idosdf.parse(currDashboardToDate));
			} else {
				currDate = IdosConstants.mysqldf.format(Calendar.getInstance().getTime());
			}
			// String oneEightyDaysBackDate = DateUtil.returnOneEightyDaysBackDate();
			Date currDateTime = IdosConstants.mysqldf.parse(currDate);
			Date under0to30daysDatedate = DateUtil.returnPrevOneMonthDateDate(currDateTime);
			String under0to30daysDate = IdosConstants.mysqldf.format(under0to30daysDatedate);
			Date under31to60daysDatedate = DateUtil.returnPrevOneMonthDateDate(under0to30daysDatedate);
			String under31to60daysDate = IdosConstants.mysqldf.format(under31to60daysDatedate);
			Date under61to90daysDatedate = DateUtil.returnPrevOneMonthDateDate(under31to60daysDatedate);
			String under61to90daysDate = IdosConstants.mysqldf.format(under61to90daysDatedate);
			Date under91To180daysDatedate = DateUtil.returnPrevThreeMonthDateDate(under61to90daysDatedate);
			String under91To180daysDate = IdosConstants.mysqldf.format(under91To180daysDatedate);
			if (period.equalsIgnoreCase("under0to30")) {
				periodStartDate = under0to30daysDate;
				periodEndDate = currDate;
				row1.put("periodLabel", "Under 0to30 days");
			} else if (period.equalsIgnoreCase("under31to60")) {
				periodStartDate = under31to60daysDate;
				periodEndDate = under0to30daysDate;
				row1.put("periodLabel", "Under 31to60 days");
			} else if (period.equalsIgnoreCase("under61to90")) {
				periodStartDate = under61to90daysDate;
				periodEndDate = under31to60daysDate;
				row1.put("periodLabel", "Under 61to90 days");
			} else if (period.equalsIgnoreCase("under91to180")) {
				periodStartDate = under91To180daysDate;
				periodEndDate = under61to90daysDate;
				row1.put("periodLabel", "Under 91to180 days");
			} else if (period.equalsIgnoreCase("over")) {
				periodStartDate = periodStartDate;
				periodEndDate = under91To180daysDate;
				row1.put("periodLabel", "Over 180days");
			}
			Map provisionEntries = new HashMap();
			Map allSpecifcsAmtData = new HashMap();
			Map vendorPayablesData = new HashMap();
			Map custReceivablesData = new HashMap();
			ProvisionJournalEntryService jourObj = new ProvisionJournalEntryServiceImpl();
			Map branchMap = jourObj.getDashboardProvisionEntriesDataForBranch(periodStartDate, periodEndDate, user,
					allSpecifcsAmtData, vendorPayablesData, custReceivablesData, branch, entityManager);
			// based on branch and priod (0-30days etc get list if customer/vendor
			StringBuilder query = new StringBuilder("");
			query.append("select obj from Transaction obj WHERE obj.transactionBranch = '" + branch.getId()
					+ "' and obj.transactionBranchOrganization = '" + branch.getOrganization().getId()
					+ "' AND obj.transactionPurpose = '" + tranPurpose
					+ "' and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionVendorCustomer IS NOT NULL and (obj.paymentStatus = 'NOT-PAID' or obj.paymentStatus = 'PARTLY-PAID') and obj.transactionDate  between '"
					+ periodStartDate + "' and '" + periodEndDate + "' GROUP BY obj.transactionVendorCustomer");
			List<Transaction> custVendTxnList = genericDAO.executeSimpleQuery(query.toString(), entityManager);
			if (!custVendTxnList.isEmpty() && custVendTxnList.size() > 0) {
				result.put("result", true);
				// receivables/paybles only Overdue transactions customerwise
				if (tabElement.equals("payableOverduesAllBranches")
						|| tabElement.equals("receivableOverduesAllBranches")) {
					for (Transaction maintxn : custVendTxnList) {
						query.delete(0, query.length());
						query.append("select obj from Transaction obj WHERE obj.transactionBranch = '" + branch.getId()
								+ "' and obj.transactionBranchOrganization = '" + branch.getOrganization().getId()
								+ "' AND obj.transactionVendorCustomer = '"
								+ maintxn.getTransactionVendorCustomer().getId()
								+ "'and (obj.transactionVendorCustomer IS NOT NULL and (obj.transactionVendorCustomer.purchaseType = 0 OR obj.transactionVendorCustomer.purchaseType = 2) AND obj.transactionVendorCustomer.daysForCredit IS NOT NULL)  AND obj.transactionPurpose = '"
								+ tranPurpose
								+ "' and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and (obj.paymentStatus = 'NOT-PAID' or obj.paymentStatus = 'PARTLY-PAID') and obj.transactionDate  between '"
								+ periodStartDate + "' and '" + periodEndDate + "'");
						List<Transaction> bnchcustcreditincometxncust = genericDAO.executeSimpleQuery(query.toString(),
								entityManager);
						double overdueamount = 0.0;
						for (Transaction txn : bnchcustcreditincometxncust) {
							int daysdiff = (int) (Calendar.getInstance().getTimeInMillis()
									- txn.getTransactionDate().getTime() / 1000 * 60 * 60 * 24);
							if (daysdiff > txn.getTransactionVendorCustomer().getDaysForCredit()) {
								if (tranPurpose == 4) {
									if (txn.getVendorNetPayment() != null) {
										overdueamount += txn.getNetAmount() - txn.getVendorNetPayment();
									} else {
										overdueamount += txn.getNetAmount();
									}
								} else {
									if (txn.getCustomerNetPayment() != null) {
										overdueamount += txn.getNetAmount() - txn.getCustomerNetPayment();
									} else {
										overdueamount += txn.getNetAmount();
									}
								}
							}
						}
						ObjectNode row = Json.newObject();
						row.put("customerId", maintxn.getTransactionVendorCustomer().getId());
						row.put("customerName", maintxn.getTransactionVendorCustomer().getName());
						row.put("amount", IdosConstants.decimalFormat.format(overdueamount));
						an.add(row);
					}

				} else {
					for (Transaction maintxn : custVendTxnList) {
						query.delete(0, query.length());
						query.append(
								"select SUM(obj.netAmount),SUM(obj.vendorNetPayment),SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranch = '"
										+ branch.getId() + "' and obj.transactionBranchOrganization = '"
										+ branch.getOrganization().getId() + "' AND obj.transactionVendorCustomer = '"
										+ maintxn.getTransactionVendorCustomer().getId()
										+ "' AND obj.transactionPurpose = '" + tranPurpose
										+ "' and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and (obj.paymentStatus = 'NOT-PAID' or obj.paymentStatus = 'PARTLY-PAID') and obj.transactionDate  between '"
										+ periodStartDate + "' and '" + periodEndDate + "'");
						List<Object[]> txnLists = entityManager.createQuery(query.toString()).getResultList();

						double amount = getRecPayNetAmt(txnLists, tranPurpose, null);
						if (tranPurpose == 2) {// receivables
							if (custReceivablesData.containsKey(maintxn.getTransactionVendorCustomer().getId())) {
								amount = amount + new Double(custReceivablesData
										.get(maintxn.getTransactionVendorCustomer().getId()).toString());
							}
						} else if (tranPurpose == 4) {// vend payables
							if (vendorPayablesData.containsKey(maintxn.getTransactionVendorCustomer().getId())) {
								amount = amount + new Double(vendorPayablesData
										.get(maintxn.getTransactionVendorCustomer().getId()).toString());
							}
						}
						ObjectNode row = Json.newObject();
						row.put("customerId", maintxn.getTransactionVendorCustomer().getId());
						row.put("customerName", maintxn.getTransactionVendorCustomer().getName());
						row.put("amount", IdosConstants.decimalFormat.format(amount));
						an.add(row);
					}
				}
			}
			if (tranPurpose == 2) {// receivables - sell on credit to customers
				addJournalEntryDataForCustVendNotPresentInTransList(custReceivablesData, custVendTxnList, an,
						entityManager);
			} else if (tranPurpose == 4) {
				addJournalEntryDataForCustVendNotPresentInTransList(vendorPayablesData, custVendTxnList, an,
						entityManager);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
		}
		result.put("result", true);
		return result;
	}

	private void addJournalEntryDataForCustVendNotPresentInTransList(Map custReceivablesData,
			List<Transaction> custVendTxnList, ArrayNode an, EntityManager entityManager) {
		Set<String> keySet = custReceivablesData.keySet();
		Iterator<String> keySetIterator = keySet.iterator();
		Map<String, Object> criterias = new HashMap<String, Object>();
		while (keySetIterator.hasNext()) {
			String key = keySetIterator.next();
			boolean found = false;
			for (Transaction maintxn : custVendTxnList) {
				if (key.equals(maintxn.getTransactionVendorCustomer().getId().toString())) {
					found = true;
				}
			}
			if (!found) {
				criterias.clear();
				criterias.put("id", new Long(key));
				criterias.put("presentStatus", 1);
				List<Vendor> custList = genericDAO.findByCriteria(Vendor.class, criterias, entityManager);
				if (!custList.isEmpty()) {
					Vendor cust = custList.get(0);
					ObjectNode row = Json.newObject();
					row.put("customerId", cust.getId());
					row.put("customerName", cust.getName());
					row.put("amount", IdosConstants.decimalFormat.format(custReceivablesData.get(key)));
					an.add(row);
				}
			}
		}
	}

}
