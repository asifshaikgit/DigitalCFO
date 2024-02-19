package com.idos.dao;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import com.idos.cache.OrganizationConfigCache;
import com.idos.util.*;
import model.*;

import model.payroll.PayrollSetup;
import model.payroll.TrialBalancePayrollItem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.libs.Json;

public class TrialBalanceDAOImpl implements TrialBalanceDAO {
	@Override
	public List<TrialBalance> displayTrialBalance(ObjectNode result, JsonNode json, Users user, EntityManager em)
			throws IDOSException {
		List<TrialBalance> trialBalanceList = null;
		try {
			result.put("result", false);
			ArrayNode trialBalancean = result.putArray("coaSpecfChildData");
			Long branchId = json.findValue("trialBalanceForBranch") != null
					? json.findValue("trialBalanceForBranch").asLong()
					: null;
			String fmDate = json.findValue("trialBalanceFromDate") != null
					? json.findValue("trialBalanceFromDate").asText()
					: null;
			String tDate = json.findValue("trialBalanceToDate") != null ? json.findValue("trialBalanceToDate").asText()
					: null;
			String coaActCode = json.findValue("coaAccountCode") != null ? json.findValue("coaAccountCode").asText()
					: null;
			int coaIdentForDataValid = json.findValue("identForDataValid") != null
					? json.findValue("identForDataValid").asInt()
					: 0;
			String fromDate = null;
			String toDate = null;

			List<TrialBalance> tbList = Collections.emptyList();
			if (fmDate == null || fmDate.equals("")) {
				List<String> listOfFinYeardate = DateUtil.returnOrgFinancialStartEndDate(user.getOrganization());
				fromDate = listOfFinYeardate.get(0);
			} else {
				fromDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(fmDate));
			}
			if (tDate == null || tDate.equals("")) {
				toDate = IdosConstants.mysqldf.format(Calendar.getInstance().getTime());
			} else {
				toDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(tDate));
			}

			if (coaActCode != null) {
				trialBalanceList = getTrialBalanceOnCriteriaForCOAItem(coaActCode, coaIdentForDataValid, user, fromDate,
						toDate, branchId, em);
				for (TrialBalance trialBalanceData : trialBalanceList) {
					result.put("result", true);
					ObjectNode row = Json.newObject();
					row.put("accountName", trialBalanceData.getAccountName());
					row.put("specId", trialBalanceData.getSpecId());
					row.put("headid2", trialBalanceData.getHeadid2());
					row.put("specfaccountCode", trialBalanceData.getSpecfaccountCode());
					row.put("topLevelAccountCode", trialBalanceData.getTopLevelAccountCode());
					if (trialBalanceData.getTopLevelAccountCode().equals("3000000000000000000")
							|| trialBalanceData.getTopLevelAccountCode().equals("4000000000000000000")) {
						if (trialBalanceData.getOpeningBalance() != null) {
							row.put("openingBalance",
									IdosConstants.decimalFormat.format(trialBalanceData.getOpeningBalance()));
						} else {
							row.put("openingBalance", "0.0");
						}
						if (trialBalanceData.getClosingBalance() != null) {
							row.put("closingBalance",
									IdosConstants.decimalFormat.format(trialBalanceData.getClosingBalance()));
						} else {
							row.put("closingBalance", "0.0");
						}
					} else {
						row.put("openingBalance", "0.0");
						row.put("closingBalance", "0.0");
					}
					if (trialBalanceData.getDebit() != null) {
						row.put("debit", IdosConstants.decimalFormat.format(trialBalanceData.getDebit()));
					} else {
						row.put("debit", "0.0");
					}
					if (trialBalanceData.getCredit() != null) {
						row.put("credit", IdosConstants.decimalFormat.format(trialBalanceData.getCredit()));
					} else {
						row.put("credit", "0.0");
					}
					if (trialBalanceData.getIdentificationForDataValid() != null
							&& !"".equals(trialBalanceData.getIdentificationForDataValid())) {
						row.put("identificationForDataValid", trialBalanceData.getIdentificationForDataValid());
					} else {
						row.put("identificationForDataValid", "0");
					}
					if (trialBalanceData.getHeadType() != null) {
						row.put("headType", trialBalanceData.getHeadType());
					} else {
						row.put("headType", "");
					}
					trialBalancean.add(row);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			log.log(Level.SEVERE, user.getEmail(), ex);
		}
		return trialBalanceList;
	}

	private List<TrialBalance> getTrialBalanceOnCriteriaForCOAItem(String coaActCode, int coaIdentForDataValid,
			Users user, String fromDate, String toDate, Long branchId, EntityManager em) throws IDOSException {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "============ Start coaActCode: " + coaActCode + ", coaIdentForDataValid: "
					+ coaIdentForDataValid + " branch: " + branchId);
		}
		List<TrialBalance> trialBalanceList = new ArrayList<TrialBalance>();
		Long accountCode = Long.parseLong(coaActCode);
		if (coaActCode == null || accountCode == null) {
			throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF, IdosConstants.BUSINESS_EXCEPTION,
					IdosConstants.NULL_KEY_EXC_ESMF_MSG, "null account code.");
		}
		Map<String, Object> criterias = new HashMap<String, Object>(3);
		criterias.put("accountCode", accountCode);
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		List<Specifics> specfList = genericDao.findByCriteria(Specifics.class, criterias, em);
		Specifics itemSpecific = null;
		if (specfList != null && specfList.size() > 0) {
			itemSpecific = specfList.get(0);
		}
		if (coaIdentForDataValid == 4) { // Bank Account then give tb branchwise,
			getTrialBalanceBranchBank(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em);
		} else if (coaIdentForDataValid == 3) { // Cash Account then give tb branchwise
			getTrialBalanceBranchCash(trialBalanceList, user, fromDate, toDate, IdosConstants.CASH, branchId, em);
		} else if (coaIdentForDataValid == 30) { // petty Cash Account then give tb branchwise
			getTrialBalanceBranchCash(trialBalanceList, user, fromDate, toDate, IdosConstants.PETTY_CASH, branchId, em);
		} else if (coaIdentForDataValid == 7) { // Adv paid to vendors
			getTrialBalanceCustomerVendorAdvance(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.VENDOR);
		} else if (coaIdentForDataValid == 1) { // Acct receivables(credit sales to customers),
			getTrialBalanceCustomerVendorCreditSales(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId,
					em, IdosConstants.CUSTOMER);
		} else if (coaIdentForDataValid == 8) { // withholding tax payment from customers
			// Sunil getTrialBalanceWithholdingTaxOnPaymentFromCustomers(coaActCode,
			// trialBalanceList, user,fromDate,toDate, em);
			// getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate,
			// toDate, em, IdosConstants.INPUT_TDS, IdosConstants.ASSETS);
			// getTrialBalanceForTds(itemSpecific, trialBalanceList, user, fromDate, toDate,
			// branchId, em, IdosConstants.INPUT_TDS, IdosConstants.ASSETS, 8);
		} else if (coaIdentForDataValid == 2) { // Acct payables(purchase on credit from vendors)
			getTrialBalanceCustomerVendorCreditSales(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId,
					em, IdosConstants.VENDOR);
		} else if (coaIdentForDataValid == 6) { // Adv received from customers
			getTrialBalanceCustomerVendorAdvance(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.CUSTOMER);
		} else if (coaIdentForDataValid == 14) { // Input taxes on buy transactions
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.INPUT_TAX, IdosConstants.ASSETS);
		} else if (coaIdentForDataValid == 39) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.INPUT_SGST, IdosConstants.ASSETS);
		} else if (coaIdentForDataValid == 40) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.INPUT_CGST, IdosConstants.ASSETS);
		} else if (coaIdentForDataValid == 41) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.INPUT_IGST, IdosConstants.ASSETS);
		} else if (coaIdentForDataValid == 42) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.INPUT_CESS, IdosConstants.ASSETS);
		} else if (coaIdentForDataValid == 53) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_SGST_IN, IdosConstants.ASSETS);
		} else if (coaIdentForDataValid == 54) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_CGST_IN, IdosConstants.ASSETS);
		} else if (coaIdentForDataValid == 55) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_IGST_IN, IdosConstants.ASSETS);
		} else if (coaIdentForDataValid == 56) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_CESS_IN, IdosConstants.ASSETS);
		} else if (coaIdentForDataValid == 15) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.OUTPUT_TAX, IdosConstants.LIABILITIES);
		} else if (coaIdentForDataValid == 43) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.OUTPUT_SGST, IdosConstants.LIABILITIES);
		} else if (coaIdentForDataValid == 44) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.OUTPUT_CGST, IdosConstants.LIABILITIES);
		} else if (coaIdentForDataValid == 45) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.OUTPUT_IGST, IdosConstants.LIABILITIES);
		} else if (coaIdentForDataValid == 46) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.OUTPUT_CESS, IdosConstants.LIABILITIES);
		} else if (coaIdentForDataValid == 47) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_SGST_OUTPUT, IdosConstants.LIABILITIES);
		} else if (coaIdentForDataValid == 48) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_CGST_OUTPUT, IdosConstants.LIABILITIES);
		} else if (coaIdentForDataValid == 49) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_IGST_OUTPUT, IdosConstants.LIABILITIES);
		} else if (coaIdentForDataValid == 50) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_CESS_OUTPUT, IdosConstants.LIABILITIES);
		} else if (coaIdentForDataValid == 57) {
			INTER_BRANCH_TRANSFER_DAO.getTrialBalanceInterBranch(trialBalanceList, user, fromDate, toDate, branchId,
					em);
		} else if (coaIdentForDataValid == 58) { // payroll expenses
			getTrialBalancePayrollExpenses(trialBalanceList, user, fromDate, toDate, branchId, em);
		} else if (coaIdentForDataValid == 59) { // payroll deductions
			getTrialBalancePayrollDeductions(trialBalanceList, user, fromDate, toDate, branchId, em);
		} else if (coaIdentForDataValid == 60) {
			getUserAdvanceForExpenseAndTravel(coaActCode, trialBalanceList, user, fromDate, toDate, branchId, em);
		} else if (coaIdentForDataValid == 61) {
			getEmployeeClaim(coaActCode, trialBalanceList, user, fromDate, toDate, branchId, em);
		} else {
			List<Specifics> incomeSpecfList = ChartOfAccountUtil.getChildCOA(coaActCode, user, branchId, em); // get
																												// nodes
																												// for
																												// the +
																												// clicked
																												// item
																												// e.g.
																												// sales
																												// product
			if (incomeSpecfList.size() > 0) {
				Specifics firstSpecific = incomeSpecfList.get(0);
				String acctCodeHierarchy = firstSpecific.getAccountCodeHirarchy();
				if (acctCodeHierarchy.startsWith("/1000000000000000000/")) {
					for (Specifics specf : incomeSpecfList) {
						TrialBalance tb = new TrialBalance();

						String description = specf.getName();
						if (specf.getInvoiceItemDescription1() != null) {
							description += " " + specf.getInvoiceItemDescription1();
						}
						if (specf.getInvoiceItemDescription2() != null) {
							description += " " + specf.getInvoiceItemDescription2();
						}
						tb.setAccountName(description);
						tb.setSpecId(specf.getId());
						tb.setSpecfaccountCode(specf.getAccountCode().toString());
						tb.setTopLevelAccountCode(specf.getParticularsId().getAccountCode().toString());
						tb.setIdentificationForDataValid(specf.getIdentificationForDataValid());
						/*
						 * if("51".equals(specf.getIdentificationForDataValid())){ //Rounding off
						 * getTrialBalanceRoundingOffForSellTranTotal(tb, user,fromDate,toDate,specf,
						 * branchId, em,true);
						 * }else{
						 */
						getTrialBalanceAmountForCOAItem(tb, user, specf, fromDate, toDate, branchId, em,
								IdosConstants.INCOME);
						// }
						trialBalanceList.add(tb);
					}
				} else if (acctCodeHierarchy.startsWith("/2000000000000000000/")) {
					for (Specifics specf : incomeSpecfList) { // get trial balance for all those items i.e. for Sun
																// screen, vitamins etc which are under sales product
						TrialBalance tb = new TrialBalance();
						tb.setAccountName(specf.getName());
						tb.setSpecId(specf.getId());
						tb.setSpecfaccountCode(specf.getAccountCode().toString());
						tb.setTopLevelAccountCode(specf.getParticularsId().getAccountCode().toString());
						tb.setIdentificationForDataValid(specf.getIdentificationForDataValid());
						if ("58".equals(specf.getIdentificationForDataValid())) { // paryoll expenses
							getTrialBalancePayrollExpensesTotal(tb, user, fromDate, toDate, branchId, em);
						} else {
							getTrialBalanceAmountForCOAItem(tb, user, specf, fromDate, toDate, branchId, em,
									IdosConstants.EXPENSE);
						}
						trialBalanceList.add(tb);
					}
				} else if (acctCodeHierarchy.startsWith("/3000000000000000000/")) {
					for (Specifics specf : incomeSpecfList) {
						TrialBalance tb = new TrialBalance();
						tb.setAccountName(specf.getName());
						tb.setSpecId(specf.getId());
						tb.setSpecfaccountCode(specf.getAccountCode().toString());
						tb.setTopLevelAccountCode(specf.getParticularsId().getAccountCode().toString());
						tb.setIdentificationForDataValid(specf.getIdentificationForDataValid());
						// tb.setOpeningBalance(specf.getOpeningBalance());
						// tb.setClosingBalance(specf.getOpeningBalance());
						getTrialBalanceAmountForAssets(tb, user, specf, fromDate, toDate, branchId, em);
						trialBalanceList.add(tb);
					}
				} else if (acctCodeHierarchy.startsWith("/4000000000000000000/")) {
					for (Specifics specf : incomeSpecfList) { // liabilities like adv paid by customers/credit purchase
																// from vendor
						TrialBalance tb = new TrialBalance();
						tb.setAccountName(specf.getName());
						tb.setSpecId(specf.getId());
						tb.setSpecfaccountCode(specf.getAccountCode().toString());
						tb.setTopLevelAccountCode(specf.getParticularsId().getAccountCode().toString());
						tb.setIdentificationForDataValid(specf.getIdentificationForDataValid());
						getTrialBalanceAmountForLiabilities(tb, user, specf, fromDate, toDate, branchId, em);
						trialBalanceList.add(tb);
					}
				}
			}
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "============ End");
		}
		return trialBalanceList;
	}

	/**
	 * used for income and expense
	 * 
	 * @param tb
	 * @param user
	 * @param itemSpecifics
	 * @param fromDate
	 * @param toDate
	 * @param em
	 */
	@Override
	public void getTrialBalanceAmountForCOAItem(TrialBalance tb, Users user, Specifics specifics, String fromDate,
			String toDate, Long branchId, EntityManager em, final short incomeOrExpense) {
		try {
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, ">>>>>>>>>>> Start incomeOrExpense: " + incomeOrExpense);
			}
			List<Specifics> leafItems = new ArrayList<Specifics>();
			ChartOfAccountUtil.getCoaLeafNodesForSpecific(specifics, user, branchId, em, leafItems);
			if (leafItems.size() >= 1) {
				getTBForSpecificsTotal(leafItems, tb, user, specifics, fromDate, toDate, branchId, em, incomeOrExpense);
			} else {
				getTrialBalanceForSpecific(tb, user, specifics, fromDate, toDate, branchId, em, incomeOrExpense);
			}
			// getTBForSpecifics(leafItems, tb, user, itemSpecifics, fromDate, toDate, em,
			// incomeOrExpense);

			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, ">>>>>>>>>>> End");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * Use for a mapped ledger where the ledger is the last child of the tree
	 * 
	 * @param tb
	 * @param user
	 * @param fromDate
	 * @param toDate
	 * @param branchId
	 * @param em
	 * @param mappedID
	 */
	private void getTrialBalanceOfWithoutTreeMappedItemTotal(TrialBalance tb, Users user, String fromDate,
			String toDate, Long branchId, EntityManager em, final int mappedID) {
		Query query = null;
		if (branchId != null && branchId > 0) {
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.INFO, " mappedID: " + mappedID);
				log.log(Level.INFO, "SQL: " + WITHOUT_TREE_MAPPED_ITEM_TOTAL_BRANCH_SQL51);
			}
			query = em.createNativeQuery(WITHOUT_TREE_MAPPED_ITEM_TOTAL_BRANCH_SQL51);
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, mappedID);
			query.setParameter(3, user.getOrganization().getId());
			query.setParameter(4, branchId);
			query.setParameter(5, mappedID);
			query.setParameter(6, fromDate);
			query.setParameter(7, user.getOrganization().getId());
			query.setParameter(8, branchId);
			query.setParameter(9, mappedID);
			query.setParameter(10, fromDate);
			query.setParameter(11, toDate);
		} else {
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.INFO, " mappedID: " + mappedID + " fromDate= " + fromDate + " toDate= " + toDate);
				log.log(Level.INFO, "SQL: " + WITHOUT_TREE_MAPPED_ITEM_TOTAL_ORG_SQL51);
			}
			query = em.createNativeQuery(WITHOUT_TREE_MAPPED_ITEM_TOTAL_ORG_SQL51);
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, mappedID);
			query.setParameter(3, user.getOrganization().getId());
			query.setParameter(4, mappedID);
			query.setParameter(5, fromDate);
			query.setParameter(6, user.getOrganization().getId());
			query.setParameter(7, mappedID);
			query.setParameter(8, fromDate);
			query.setParameter(9, toDate);
		}
		List<Object[]> txnLists = query.getResultList();
		Double creditAmt = 0.0;
		Double debitAmt = 0.0;
		Double openBal = 0.0;
		for (Object[] custData : txnLists) {
			if (custData[0] != null)
				openBal += Double.parseDouble(String.valueOf(custData[0]));
			if (custData[1] != null) {
				creditAmt += Double.parseDouble(String.valueOf(custData[1]));
			}
			if (custData[2] != null) {
				debitAmt += Double.parseDouble(String.valueOf(custData[2]));
			}
		}
		tb.setOpeningBalance(openBal);
		tb.setDebit(debitAmt);
		tb.setCredit(creditAmt);
		tb.setClosingBalance(openBal + creditAmt - debitAmt);
		tb.setHeadType(IdosConstants.HEAD_SPECIFIC);
		tb.setIdentificationForDataValid(String.valueOf(mappedID));
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "****** End " + openBal);
	}

	private void getTrialBalanceAmountForAssets(TrialBalance tb, Users user, Specifics specifics, String fromDate,
			String toDate, Long branchId, EntityManager em) {
		List<Specifics> leafItems = new ArrayList<Specifics>();
		ChartOfAccountUtil.getCoaLeafNodesForSpecific(specifics, user, branchId, em, leafItems);
		log.log(Level.FINE, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  " + leafItems);
		if (leafItems != null && leafItems.size() > 0) {
			if ("3".equals(specifics.getIdentificationForDataValid())) { // Cash Account
				getTrialBalanceBranchCashTotal(tb, user, fromDate, toDate, specifics, IdosConstants.CASH, branchId, em);
			} else if ("30".equals(specifics.getIdentificationForDataValid())) { // Petty Cash Account
				getTrialBalanceBranchCashTotal(tb, user, fromDate, toDate, specifics, IdosConstants.PETTY_CASH,
						branchId, em);
			} else if ("4".equals(specifics.getIdentificationForDataValid())) { // "Bank Account" name is at specifics
				getTrialBalanceBranchBankTotal(tb, user, fromDate, toDate, specifics, branchId, em);
			} else if ("7".equals(specifics.getIdentificationForDataValid())) { // Advance paid to vendors
				getTrialBalanceCustomerVendorAdvanceTotal(tb, user, fromDate, toDate, branchId, em, specifics,
						IdosConstants.VENDOR);
			} else if ("1".equals(specifics.getIdentificationForDataValid())) {// Account Receivables(credit Sales to
																				// customers)
				getTrialBalanceCustomerVendorCreditSalesTotal(tb, user, fromDate, toDate, branchId, em, specifics,
						IdosConstants.CUSTOMER);
			} else if ("8".equals(specifics.getIdentificationForDataValid())) {
				getTrialBalanceForTdsTotal(tb, user, fromDate, toDate, branchId, em, IdosConstants.INPUT_TDS,
						IdosConstants.ASSETS, 8);
			} else if ("14".equals(specifics.getIdentificationForDataValid())) {
				getTrialBalanceForBranchTaxesTotal(tb, user, fromDate, toDate, branchId, em, IdosConstants.INPUT_TAX,
						IdosConstants.ASSETS);
			} else if ("39".equals(specifics.getIdentificationForDataValid())) {
				getTrialBalanceForBranchTaxesTotal(tb, user, fromDate, toDate, branchId, em, IdosConstants.INPUT_SGST,
						IdosConstants.ASSETS);
			} else if ("40".equals(specifics.getIdentificationForDataValid())) {
				getTrialBalanceForBranchTaxesTotal(tb, user, fromDate, toDate, branchId, em, IdosConstants.INPUT_CGST,
						IdosConstants.ASSETS);
			} else if ("41".equals(specifics.getIdentificationForDataValid())) {
				getTrialBalanceForBranchTaxesTotal(tb, user, fromDate, toDate, branchId, em, IdosConstants.INPUT_IGST,
						IdosConstants.ASSETS);
			} else if ("42".equals(specifics.getIdentificationForDataValid())) {
				getTrialBalanceForBranchTaxesTotal(tb, user, fromDate, toDate, branchId, em, IdosConstants.INPUT_CESS,
						IdosConstants.ASSETS);
			} else if ("53".equals(specifics.getIdentificationForDataValid())) {
				getTrialBalanceForBranchTaxesTotal(tb, user, fromDate, toDate, branchId, em, IdosConstants.RCM_SGST_IN,
						IdosConstants.ASSETS);
			} else if ("54".equals(specifics.getIdentificationForDataValid())) {
				getTrialBalanceForBranchTaxesTotal(tb, user, fromDate, toDate, branchId, em, IdosConstants.RCM_CGST_IN,
						IdosConstants.ASSETS);
			} else if ("55".equals(specifics.getIdentificationForDataValid())) {
				getTrialBalanceForBranchTaxesTotal(tb, user, fromDate, toDate, branchId, em, IdosConstants.RCM_IGST_IN,
						IdosConstants.ASSETS);
			} else if ("56".equals(specifics.getIdentificationForDataValid())) {
				getTrialBalanceForBranchTaxesTotal(tb, user, fromDate, toDate, branchId, em, IdosConstants.RCM_CESS_IN,
						IdosConstants.ASSETS);
			} else if ("60".equals(specifics.getIdentificationForDataValid())) {
				getUserAdvanceForExpenseAndTravelTotal(tb, user, fromDate, toDate, branchId, em, specifics);
			} else { // this assets belongs to Specifics (i.e. it is item like Share Capital (which
						// has no mapping in COA so getIdentificationForDataValid=null
				if (leafItems.size() >= 1) {
					getTBForSpecificsTotal(leafItems, tb, user, specifics, fromDate, toDate, branchId, em,
							IdosConstants.ASSETS);
				} else {
					getTrialBalanceForSpecific(tb, user, specifics, fromDate, toDate, branchId, em,
							IdosConstants.ASSETS);
				}
			}
		}
	}

	private void getTrialBalanceAmountForLiabilities(TrialBalance tb, Users user, Specifics itemSpecifics,
			String fromDate, String toDate, Long branchId, EntityManager em) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "********** Start " + tb.getAccountName());
		}
		List<Specifics> leafItems = new ArrayList<Specifics>();
		ChartOfAccountUtil.getCoaLeafNodesForSpecific(itemSpecifics, user, branchId, em, leafItems);
		// Double creditAmt=0.0;Double debitAmt=0.0;
		if (leafItems != null && leafItems.size() > 0) {
			int mappingId = 0;
			if (itemSpecifics.getIdentificationForDataValid() != null
					&& !itemSpecifics.getIdentificationForDataValid().equals("")) {
				mappingId = Integer.parseInt(itemSpecifics.getIdentificationForDataValid());
			}
			if ("15".equals(itemSpecifics.getIdentificationForDataValid())) {// Current Liabilities
				getTrialBalanceForBranchTaxesTotal(tb, user, fromDate, toDate, branchId, em, IdosConstants.OUTPUT_TAX,
						IdosConstants.LIABILITIES);
			} else if ("43".equals(itemSpecifics.getIdentificationForDataValid())) {
				getTrialBalanceForBranchTaxesTotal(tb, user, fromDate, toDate, branchId, em, IdosConstants.OUTPUT_SGST,
						IdosConstants.LIABILITIES);
			} else if ("44".equals(itemSpecifics.getIdentificationForDataValid())) {
				getTrialBalanceForBranchTaxesTotal(tb, user, fromDate, toDate, branchId, em, IdosConstants.OUTPUT_CGST,
						IdosConstants.LIABILITIES);
			} else if ("45".equals(itemSpecifics.getIdentificationForDataValid())) {
				getTrialBalanceForBranchTaxesTotal(tb, user, fromDate, toDate, branchId, em, IdosConstants.OUTPUT_IGST,
						IdosConstants.LIABILITIES);
			} else if ("46".equals(itemSpecifics.getIdentificationForDataValid())) {
				getTrialBalanceForBranchTaxesTotal(tb, user, fromDate, toDate, branchId, em, IdosConstants.OUTPUT_CESS,
						IdosConstants.LIABILITIES);
			} else if ("47".equals(itemSpecifics.getIdentificationForDataValid())) {
				getTrialBalanceForBranchTaxesTotal(tb, user, fromDate, toDate, branchId, em,
						IdosConstants.RCM_SGST_OUTPUT, IdosConstants.LIABILITIES);
			} else if ("48".equals(itemSpecifics.getIdentificationForDataValid())) {
				getTrialBalanceForBranchTaxesTotal(tb, user, fromDate, toDate, branchId, em,
						IdosConstants.RCM_CGST_OUTPUT, IdosConstants.LIABILITIES);
			} else if ("49".equals(itemSpecifics.getIdentificationForDataValid())) {
				getTrialBalanceForBranchTaxesTotal(tb, user, fromDate, toDate, branchId, em,
						IdosConstants.RCM_IGST_OUTPUT, IdosConstants.LIABILITIES);
			} else if ("50".equals(itemSpecifics.getIdentificationForDataValid())) {
				getTrialBalanceForBranchTaxesTotal(tb, user, fromDate, toDate, branchId, em,
						IdosConstants.RCM_CESS_OUTPUT, IdosConstants.LIABILITIES);
			} else if ("6".equals(itemSpecifics.getIdentificationForDataValid())) { // Advance received from customers
				getTrialBalanceCustomerVendorAdvanceTotal(tb, user, fromDate, toDate, branchId, em, itemSpecifics, 2);
			} else if ("2".equals(itemSpecifics.getIdentificationForDataValid())) { // Account Payables(credit Purchase
																					// from vendors)
				getTrialBalanceCustomerVendorCreditSalesTotal(tb, user, fromDate, toDate, branchId, em, itemSpecifics,
						1);
			} else if ("59".equals(itemSpecifics.getIdentificationForDataValid())) { // payroll deductions
				getTrialBalancePayrollDeductionsTotal(tb, user, fromDate, toDate, branchId, em);
			} else if (mappingId >= 31 && mappingId <= 38) { // Withholidng tax on payments made to vendors
				getTrialBalanceForTdsTotal(tb, user, fromDate, toDate, branchId, em, (mappingId + 9),
						IdosConstants.LIABILITIES, mappingId);
			} else if ("61".equals(itemSpecifics.getIdentificationForDataValid())) {
				getEmployeeClaimTotal(tb, user, fromDate, toDate, branchId, em, itemSpecifics);
			} else if ("65".equals(itemSpecifics.getIdentificationForDataValid())) {
				getReserveAndSurplusTotal(tb, user, fromDate, toDate, branchId, em, itemSpecifics);
			} else { // this assets belongs to Specifics (i.e. it is item like Share Capital (which
						// has no mapping in COA so getIdentificationForDataValid=null
				if (leafItems.size() >= 1) {
					getTBForSpecificsTotal(leafItems, tb, user, itemSpecifics, fromDate, toDate, branchId, em,
							IdosConstants.LIABILITIES);
				} else {
					// getTBForSpecifics(leafItems, tb, user, itemSpecifics, fromDate, toDate, em,
					// IdosConstants.LIABILITIES);
					getTrialBalanceForSpecific(tb, user, itemSpecifics, fromDate, toDate, branchId, em,
							IdosConstants.LIABILITIES);
				}
			}
			// }
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "********** End ");
		}
	}

	/**
	 * This method will previous setup amount in opening, closing, credit and debit.
	 * this should be uonly used for Specifics items.
	 * 
	 * @param tb
	 * @param user
	 * @param specific
	 * @param fromDate
	 * @param toDate
	 * @param em
	 * @param particularType
	 */
	private void getTrialBalanceForSpecific(TrialBalance tb, Users user, Specifics specific, String fromDate,
			String toDate, Long branchId, EntityManager em, final short particularType) {
		Double creditAmt = tb.getCredit() == null ? 0.0 : tb.getCredit();
		Double debitAmt = tb.getDebit() == null ? 0.0 : tb.getDebit();
		Double openBal = tb.getOpeningBalance() == null ? 0.0 : tb.getOpeningBalance();
		StringBuilder sbr = new StringBuilder(
				"select SUM(obj.creditAmount), SUM(obj.debitAmount) from TrialBalanceCOAItems obj where obj.organization = ")
				.append(user.getOrganization().getId()).append(" and obj.presentStatus=1");
		if (branchId != null && branchId > 0L) {
			sbr.append(" and obj.branch.id=").append(branchId);
		}
		sbr.append(" and obj.transactionSpecifics= ").append(specific.getId());
		sbr.append(" and obj.transactionParticulars=").append(specific.getParticularsId().getId())
				.append(" and obj.date  between '" + fromDate + "' and '" + toDate + "'");
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, "HQL: " + sbr);
		}
		String queryString = em.createQuery(sbr.toString()).unwrap(org.hibernate.Query.class).getQueryString();
		System.out.println(queryString);
		List<Object[]> txnLists = em.createQuery(sbr.toString()).getResultList();
		for (Object[] val : txnLists) {
			creditAmt += val[0] != null ? Double.parseDouble(String.valueOf(val[0])) : 0.0;
			debitAmt += val[1] != null ? Double.parseDouble(String.valueOf(val[1])) : 0.0;
		}
		openBal += specific.getTotalOpeningBalance() == null ? 0.0 : specific.getTotalOpeningBalance();
		Double derivedOpeningBal = getOpeningBalForSpecifics("TrialBalanceCOAItems", openBal, specific.getId(), user,
				fromDate, "transactionSpecifics", particularType, branchId, em, IdosConstants.HEAD_SPECIFIC);
		tb.setOpeningBalance(derivedOpeningBal);
		tb.setDebit(debitAmt);
		tb.setCredit(creditAmt);
		tb.setHeadType(IdosConstants.HEAD_SPECIFIC);
		if (particularType == IdosConstants.ASSETS || particularType == IdosConstants.EXPENSE) {
			tb.setClosingBalance(derivedOpeningBal + debitAmt - creditAmt);
		} else {
			tb.setClosingBalance(derivedOpeningBal + creditAmt - debitAmt);
		}
	}

	/**
	 * USed to calculate Total for parent node
	 * 
	 * @param leafItems
	 * @param tb
	 * @param user
	 * @param itemSpecifics
	 * @param fromDate
	 * @param toDate
	 * @param em
	 * @param particularType
	 */
	@Override
	public void getTBForSpecificsTotal(List<Specifics> leafItems, TrialBalance tb, Users user, Specifics itemSpecifics,
			String fromDate, String toDate, Long branchId, EntityManager em, final short particularType) {
		if (log.isLoggable(Level.FINE) && itemSpecifics != null) {
			log.log(Level.FINE, ">>>>>> Start branchId: " + branchId + " ParticularsId: " + itemSpecifics.getId()
					+ " IdentificationForDataValid: " + itemSpecifics.getIdentificationForDataValid());
		}
		Double creditAmt = 0.0;
		Double debitAmt = 0.0;
		Double openBal = 0.0;
		Double derivedOpenBal = 0.0;
		if (leafItems != null && leafItems.size() > 0) {
			for (Specifics specifics : leafItems) {
				Double openBalance = 0.0;
				if (specifics.getIdentificationForDataValid() != null
						&& !"".equals(specifics.getIdentificationForDataValid())
						&& !specifics.getIdentificationForDataValid().equals("24")
						&& !specifics.getIdentificationForDataValid().equals("25")
						&& !specifics.getIdentificationForDataValid().equals("26")
						&& !specifics.getIdentificationForDataValid().equals("27")
						&& !specifics.getIdentificationForDataValid().equals("62")) {
					TrialBalance trialBalance = getTotalForMappedSpecifics(specifics, user, fromDate, toDate, branchId,
							em, itemSpecifics);
					creditAmt += trialBalance.getCredit();
					debitAmt += trialBalance.getDebit();
					derivedOpenBal += trialBalance.getOpeningBalance();
				} else {
					StringBuilder sbr = new StringBuilder(
							"select SUM(obj.creditAmount), SUM(obj.debitAmount) from TrialBalanceCOAItems obj where obj.organization = ")
							.append(user.getOrganization().getId());
							if (branchId != null && branchId > 0L) {
								sbr.append(" and obj.branch.id=").append(branchId);
							}
							sbr.append(" and obj.presentStatus=1");
					// if (branchId != null && branchId > 0l) {
					// sbr.append(" and obj.branch.id= ").append(branchId);
					// }
					sbr.append(" and obj.transactionSpecifics.id= ").append(specifics.getId());
					// sbr.append(" and
					// obj.transactionParticulars.id=").append(specifics.getParticularsId().getId())
					sbr.append(" and obj.date  between '" + fromDate + "' and '" + toDate + "'");
					if (log.isLoggable(Level.FINE)) {
						log.log(Level.INFO, "HQL: " + sbr);
					}
					String queryString = em.createQuery(sbr.toString()).unwrap(org.hibernate.Query.class)
							.getQueryString();
					System.out.println("Generated Query: " + queryString);

					Object[] result = (Object[]) em.createQuery(sbr.toString()).getSingleResult();
					creditAmt += result[0] != null ? Double.parseDouble(String.valueOf(result[0])) : 0.0;
					debitAmt += result[1] != null ? Double.parseDouble(String.valueOf(result[1])) : 0.0;

					if (specifics != null && branchId != null && branchId > 0L) {
						for (BranchSpecifics branchSpecifics : specifics.getSpecificsBranch()) {
							if (branchSpecifics.getPresentStatus() == 1
									&& branchSpecifics.getBranch().getId().compareTo(branchId) == 0
									&& branchSpecifics.getOpeningBalance() != null) {
								openBalance += branchSpecifics.getOpeningBalance();
							}
						}
						openBal = openBalance;
					} else {
						openBal = specifics.getTotalOpeningBalance();
					}
					derivedOpenBal += getOpeningBalForSpecifics("TrialBalanceCOAItems", openBal, specifics.getId(),
							user, fromDate, "transactionSpecifics", particularType, branchId, em,
							IdosConstants.HEAD_SPECIFIC);
				}
			}
			tb.setOpeningBalance(derivedOpenBal);
			tb.setDebit(debitAmt);
			tb.setCredit(creditAmt);
			tb.setHeadType(IdosConstants.HEAD_SPECIFIC);
			if (particularType == IdosConstants.ASSETS || particularType == IdosConstants.EXPENSE) { // for expense it
																										// is reverse
				tb.setClosingBalance(derivedOpenBal + debitAmt - creditAmt);
			} else {
				tb.setClosingBalance(derivedOpenBal + creditAmt - debitAmt);
			}
		}
		if (log.isLoggable(Level.FINE))

		{
			log.log(Level.FINE, ">>>>>> End " + tb);
		}
	}

	/**
	 * This will
	 * 
	 * @param specifics
	 * @param user
	 * @param fromDate
	 * @param toDate
	 * @param em
	 * @param itemSpecifics
	 * @return
	 */
	private TrialBalance getTotalForMappedSpecifics(Specifics specifics, Users user, String fromDate, String toDate,
			Long branchId, EntityManager em, Specifics itemSpecifics) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "**** Start name: " + specifics);
		}
		int mappingID = 0;
		if (specifics.getIdentificationForDataValid() != null
				&& !"".equals(specifics.getIdentificationForDataValid().trim())) {
			mappingID = Integer.parseInt(specifics.getIdentificationForDataValid());
		}
		TrialBalance tempTb = new TrialBalance();
		Double creditAmt = 0.0;
		Double debitAmt = 0.0;
		Double derivedOpenBal = 0.0;
		if ("1".equals(specifics.getIdentificationForDataValid())) {// Account Receivables(credit Sales to customers)
			getTrialBalanceCustomerVendorCreditSalesTotal(tempTb, user, fromDate, toDate, branchId, em, itemSpecifics,
					IdosConstants.CUSTOMER);
		} else if ("3".equals(specifics.getIdentificationForDataValid())) { // Cash Account
			getTrialBalanceBranchCashTotal(tempTb, user, fromDate, toDate, itemSpecifics, IdosConstants.CASH, branchId,
					em);
		} else if ("4".equals(specifics.getIdentificationForDataValid())) { //
			getTrialBalanceBranchBankTotal(tempTb, user, fromDate, toDate, itemSpecifics, branchId, em);
		} else if ("7".equals(specifics.getIdentificationForDataValid())) { // Advance paid to vendors
			getTrialBalanceCustomerVendorAdvanceTotal(tempTb, user, fromDate, toDate, branchId, em, itemSpecifics,
					IdosConstants.VENDOR);
		} else if ("8".equals(specifics.getIdentificationForDataValid())) {
			// getTrialBalanceForBranchTaxesTotal(tempTb, user, fromDate, toDate, em,
			// IdosConstants.INPUT_TDS, IdosConstants.ASSETS);
			// getTrialBalanceForSpecific(tempTb, user, itemSpecifics, fromDate, toDate, em,
			// IdosConstants.ASSETS);
			getTrialBalanceForTdsTotal(tempTb, user, fromDate, toDate, branchId, em, IdosConstants.INPUT_TDS,
					IdosConstants.ASSETS, 8);
		} else if ("14".equals(specifics.getIdentificationForDataValid())) {
			getTrialBalanceForBranchTaxesTotal(tempTb, user, fromDate, toDate, branchId, em, IdosConstants.INPUT_TAX,
					IdosConstants.ASSETS);
		} else if ("39".equals(specifics.getIdentificationForDataValid())) {
			getTrialBalanceForBranchTaxesTotal(tempTb, user, fromDate, toDate, branchId, em, IdosConstants.INPUT_SGST,
					IdosConstants.ASSETS);
		} else if ("40".equals(specifics.getIdentificationForDataValid())) {
			getTrialBalanceForBranchTaxesTotal(tempTb, user, fromDate, toDate, branchId, em, IdosConstants.INPUT_CGST,
					IdosConstants.ASSETS);
		} else if ("41".equals(specifics.getIdentificationForDataValid())) {
			getTrialBalanceForBranchTaxesTotal(tempTb, user, fromDate, toDate, branchId, em, IdosConstants.INPUT_IGST,
					IdosConstants.ASSETS);
		} else if ("42".equals(specifics.getIdentificationForDataValid())) {
			getTrialBalanceForBranchTaxesTotal(tempTb, user, fromDate, toDate, branchId, em, IdosConstants.INPUT_CESS,
					IdosConstants.ASSETS);
		} else if ("53".equals(specifics.getIdentificationForDataValid())) {
			getTrialBalanceForBranchTaxesTotal(tempTb, user, fromDate, toDate, branchId, em, IdosConstants.RCM_SGST_IN,
					IdosConstants.ASSETS);
		} else if ("54".equals(specifics.getIdentificationForDataValid())) {
			getTrialBalanceForBranchTaxesTotal(tempTb, user, fromDate, toDate, branchId, em, IdosConstants.RCM_CGST_IN,
					IdosConstants.ASSETS);
		} else if ("55".equals(specifics.getIdentificationForDataValid())) {
			getTrialBalanceForBranchTaxesTotal(tempTb, user, fromDate, toDate, branchId, em, IdosConstants.RCM_IGST_IN,
					IdosConstants.ASSETS);
		} else if ("56".equals(specifics.getIdentificationForDataValid())) {
			getTrialBalanceForBranchTaxesTotal(tempTb, user, fromDate, toDate, branchId, em, IdosConstants.RCM_CESS_IN,
					IdosConstants.ASSETS);
		} else if ("30".equals(specifics.getIdentificationForDataValid())) { // Petty Cash Account
			getTrialBalanceBranchCashTotal(tempTb, user, fromDate, toDate, itemSpecifics, IdosConstants.PETTY_CASH,
					branchId, em);
		} else if ("15".equals(specifics.getIdentificationForDataValid())) { // Current Liabilities
			getTrialBalanceForBranchTaxesTotal(tempTb, user, fromDate, toDate, branchId, em, IdosConstants.OUTPUT_TAX,
					IdosConstants.LIABILITIES);
		} else if ("43".equals(specifics.getIdentificationForDataValid())) {
			getTrialBalanceForBranchTaxesTotal(tempTb, user, fromDate, toDate, branchId, em, IdosConstants.OUTPUT_SGST,
					IdosConstants.LIABILITIES);
		} else if ("44".equals(specifics.getIdentificationForDataValid())) {
			getTrialBalanceForBranchTaxesTotal(tempTb, user, fromDate, toDate, branchId, em, IdosConstants.OUTPUT_CGST,
					IdosConstants.LIABILITIES);
		} else if ("45".equals(specifics.getIdentificationForDataValid())) {
			getTrialBalanceForBranchTaxesTotal(tempTb, user, fromDate, toDate, branchId, em, IdosConstants.OUTPUT_IGST,
					IdosConstants.LIABILITIES);
		} else if ("46".equals(specifics.getIdentificationForDataValid())) {
			getTrialBalanceForBranchTaxesTotal(tempTb, user, fromDate, toDate, branchId, em, IdosConstants.OUTPUT_CESS,
					IdosConstants.LIABILITIES);
		} else if ("47".equals(specifics.getIdentificationForDataValid())) {
			getTrialBalanceForBranchTaxesTotal(tempTb, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_SGST_OUTPUT, IdosConstants.LIABILITIES);
		} else if ("48".equals(specifics.getIdentificationForDataValid())) {
			getTrialBalanceForBranchTaxesTotal(tempTb, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_CGST_OUTPUT, IdosConstants.LIABILITIES);
		} else if ("49".equals(specifics.getIdentificationForDataValid())) {
			getTrialBalanceForBranchTaxesTotal(tempTb, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_IGST_OUTPUT, IdosConstants.LIABILITIES);
		} else if ("50".equals(specifics.getIdentificationForDataValid())) {
			getTrialBalanceForBranchTaxesTotal(tempTb, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_CESS_OUTPUT, IdosConstants.LIABILITIES);
		} else if ("6".equals(specifics.getIdentificationForDataValid())) { // Advance received from customers
			getTrialBalanceCustomerVendorAdvanceTotal(tempTb, user, fromDate, toDate, branchId, em, itemSpecifics, 2);
		} else if ("2".equals(specifics.getIdentificationForDataValid())) { // Account Payables(credit Purchase from
																			// vendors)
			getTrialBalanceCustomerVendorCreditSalesTotal(tempTb, user, fromDate, toDate, branchId, em, itemSpecifics,
					1);
		} else if (mappingID >= 31 && mappingID <= 38) { // 31 to 38 Withholidng tax on payments made to vendors
			getTrialBalanceForTdsTotal(tempTb, user, fromDate, toDate, branchId, em, (mappingID + 9),
					IdosConstants.LIABILITIES, mappingID);
		} else if ("51".equals(specifics.getIdentificationForDataValid())) {
			// getTrialBalanceRoundingOffForSellTranTotal(tempTb, user, fromDate, toDate,
			// itemSpecifics, branchId, em,true);
			getTrialBalanceOfWithoutTreeMappedItemTotal(tempTb, user, fromDate, toDate, branchId, em, 51);
		} else if (mappingID == 57) {
			INTER_BRANCH_TRANSFER_DAO.getTrialBalanceInterBranchTotal(tempTb, user, fromDate, toDate, branchId, em);
		} else if ("58".equals(specifics.getIdentificationForDataValid())) { // paryoll expenses
			getTrialBalancePayrollExpensesTotal(tempTb, user, fromDate, toDate, branchId, em);
		} else if ("59".equals(specifics.getIdentificationForDataValid())) { // payroll deductions
			getTrialBalancePayrollDeductionsTotal(tempTb, user, fromDate, toDate, branchId, em);
		} else if ("60".equals(specifics.getIdentificationForDataValid())) {
			getUserAdvanceForExpenseAndTravelTotal(tempTb, user, fromDate, toDate, branchId, em, itemSpecifics);
		} else if ("61".equals(specifics.getIdentificationForDataValid())) {
			getEmployeeClaimTotal(tempTb, user, fromDate, toDate, branchId, em, itemSpecifics);
		} else if ("63".equals(specifics.getIdentificationForDataValid())) {
			getTrialBalanceOfWithoutTreeMappedItemTotal(tempTb, user, fromDate, toDate, branchId, em, 63);
		} else if ("64".equals(specifics.getIdentificationForDataValid())) {
			getTrialBalanceOfWithoutTreeMappedItemTotal(tempTb, user, fromDate, toDate, branchId, em, 64);
		} else if (mappingID == 65) {
			getReserveAndSurplusTotal(tempTb, user, fromDate, toDate, branchId, em, itemSpecifics);
		} else if (mappingID == 67) {
			getProfitLossTotal(tempTb, user, fromDate, toDate, branchId, em, itemSpecifics);
		}
		creditAmt += tempTb.getCredit() == null ? 0 : tempTb.getCredit();
		debitAmt += tempTb.getDebit() == null ? 0 : tempTb.getDebit();
		derivedOpenBal += tempTb.getOpeningBalance() == null ? 0 : tempTb.getOpeningBalance();
		tempTb.setCredit(creditAmt);
		tempTb.setDebit(debitAmt);
		tempTb.setOpeningBalance(derivedOpenBal);
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "******End " + tempTb);
		}
		return tempTb;
	}

	/**
	 * should be called from getOpeningBalForSpecifics
	 * 
	 * @param em
	 * @param user
	 * @param headType
	 * @return
	 */
	private Double getTotalOpeningBalanceForSpecifics(EntityManager em, Users user, String headType, Long branchId) {
		Double openingBal = 0.0;
		Map<String, Object> criterias = new HashMap<String, Object>(1);
		if (IdosConstants.HEAD_CASH.equals(headType) || IdosConstants.HEAD_PETTY.equals(headType)) {
			List<BranchDepositBoxKey> branchCashList = null;
			criterias.put("organization.id", user.getOrganization().getId());
			if (branchId != null && branchId > 0) {
				criterias.put("branch.id", branchId);
			}
			criterias.put("presentStatus", 1);
			branchCashList = genericDao.findByCriteria(BranchDepositBoxKey.class, criterias, em);
			if (IdosConstants.HEAD_CASH.equals(headType)) {
				for (BranchDepositBoxKey branchCashAccount : branchCashList) {
					if (branchCashAccount.getOpeningBalance() != null) {
						openingBal += branchCashAccount.getOpeningBalance();
					}
				}
			} else {
				for (BranchDepositBoxKey branchCashAccount : branchCashList) {
					if (branchCashAccount.getPettyCashOpeningBalance() != null) {
						openingBal += branchCashAccount.getPettyCashOpeningBalance();
					}
				}
			}
		} else if (IdosConstants.HEAD_BANK.equals(headType)) {
			List<BranchBankAccounts> branchBankAccountsesList = null;
			criterias.put("organization.id", user.getOrganization().getId());
			if (branchId != null && branchId > 0) {
				criterias.put("branch.id", branchId);
			}
			criterias.put("presentStatus", 1);
			branchBankAccountsesList = genericDao.findByCriteria(BranchBankAccounts.class, criterias, em);
			for (BranchBankAccounts branchBankAccount : branchBankAccountsesList) {
				if (branchBankAccount.getOpeningBalance() != null) {
					openingBal += branchBankAccount.getOpeningBalance();
				}
			}
		} else if (IdosConstants.HEAD_SPECIFIC.equals(headType)) {
			/*
			 * List <Specifics> specificsList = null;
			 * criterias.put("organization.id", user.getOrganization().getId());
			 * specificsList = genericDao.findByCriteria(Specifics.class, criterias, em);
			 * for (Specifics specifics : specificsList) {
			 * if (specifics.getOpeningBalance() != null) {
			 * openingBal += specifics.getOpeningBalance();
			 * }
			 * }
			 */
		}
		return openingBal;
	}

	/**
	 *
	 * @param tableName
	 * @param openingBal
	 * @param headID
	 * @param user
	 * @param fromDate
	 * @param columnName
	 * @param particularType
	 * @param em
	 * @param headType
	 * @return
	 */
	@Override
	public Double getOpeningBalForSpecifics(String tableName, Double openingBal, Long headID, Users user,
			String fromDate, String columnName, final short particularType, Long branchId, EntityManager em,
			String headType) {
		log.log(Level.FINE, ">>>>>>>>>> Start openingBal = " + openingBal);
		StringBuilder sbr = new StringBuilder("select SUM(obj.creditAmount), SUM(obj.debitAmount) from ");
		sbr.append(tableName).append(" obj where obj.organization.id=").append(user.getOrganization().getId())
				.append(" and obj.presentStatus=1");
		if (branchId != null && branchId > 0L) {
			sbr.append(" and obj.branch.id = ").append(branchId);
		}
		if (IdosConstants.HEAD_CASH.equals(headType)) {
			sbr.append(" and obj.cashType = ").append(IdosConstants.CASH);
		} else if (IdosConstants.HEAD_PETTY.equals(headType)) {
			sbr.append(" and obj.cashType = ").append(IdosConstants.PETTY_CASH);
		} else if (IdosConstants.HEAD_CUSTOMER.equals(headType)) {
			sbr.append(" and obj.vendorType = ").append(IdosConstants.CUSTOMER);
		} else if (IdosConstants.HEAD_VENDOR.equals(headType)) {
			sbr.append(" and obj.vendorType = ").append(IdosConstants.VENDOR);
		}
		if (headID != 0) { // if headid is 0, it means it for cach/bank/petty cash total
			sbr.append(" and obj.").append(columnName).append(" = ").append(headID);
		}
		sbr.append(" and obj.date  < '").append(fromDate).append("'");
		// sbr.append(" group by obj.").append(columnName);

		log.log(Level.FINE, "HQL: " + sbr);
		String queryString = em.createQuery(sbr.toString()).unwrap(org.hibernate.Query.class).getQueryString();
		System.out.println("Generated Query: " + queryString);
		List<Object[]> txnLists = em.createQuery(sbr.toString()).getResultList();
		Double creditAmt = 0.0;
		Double debitAmt = 0.0;
		if (openingBal == null) {
			openingBal = 0.0;
		}
		for (Object[] custData : txnLists) {
			if (custData[0] != null) {
				creditAmt += Double.parseDouble(String.valueOf(custData[0]));
			}
			if (custData[1] != null) {
				debitAmt += Double.parseDouble(String.valueOf(custData[1]));
			}
		}

		if (headID == 0) {
			openingBal = getTotalOpeningBalanceForSpecifics(em, user, headType, branchId);
		}

		if (IdosConstants.ASSETS == particularType || IdosConstants.EXPENSE == particularType) { // for Assets
			openingBal = openingBal + debitAmt - creditAmt; // Assets
		} else {
			openingBal = openingBal + creditAmt - debitAmt; // Liabilities
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, ">>>>>>>>>> End openingBal = " + openingBal);
		}
		return openingBal;
	}

	/**
	 *
	 * @param itemSpecifics
	 * @param trialBalanceList
	 * @param user
	 * @param fromDate
	 * @param toDate
	 * @param em
	 * @param vendorType       1- vendor, 2- customer, type=2 means customers to
	 *                         whom we have sold items on credit
	 */
	private void getTrialBalanceCustomerVendorCreditSales(Specifics itemSpecifics, List<TrialBalance> trialBalanceList,
			Users user, String fromDate, String toDate, Long branchId, EntityManager em, int vendorType) {
		String hql = null;
		int walkinType = IdosConstants.WALK_IN_VENDOR;
		if (branchId != null && branchId > 0) {
			if (IdosConstants.CUSTOMER == vendorType) {
				hql = CUSTVEND_BRANCH_JPQL_AST;
				walkinType = IdosConstants.WALK_IN_CUSTOMER;
			} else {
				hql = CUSTVEND_BRANCH_JPQL_LIB;
			}
		} else {
			if (IdosConstants.CUSTOMER == vendorType) {
				hql = CUSTVEND_ORG_JPQL_AST;
				walkinType = IdosConstants.WALK_IN_CUSTOMER;
			} else {
				hql = CUSTVEND_ORG_JPQL_LIB;
			}
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.INFO, "HQL: " + hql);
		}
		Query query = em.createNativeQuery(hql);
		if (branchId != null && branchId > 0) {
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, branchId);
			query.setParameter(3, user.getOrganization().getId());
			query.setParameter(4, branchId);
			query.setParameter(5, vendorType);
			query.setParameter(6, walkinType);
			query.setParameter(7, fromDate);
			query.setParameter(8, user.getOrganization().getId());
			query.setParameter(9, branchId);
			query.setParameter(10, vendorType);
			query.setParameter(11, walkinType);
			query.setParameter(12, fromDate);
			query.setParameter(13, toDate);
		} else {
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, user.getOrganization().getId());
			query.setParameter(3, vendorType);
			query.setParameter(4, walkinType);
			query.setParameter(5, fromDate);
			query.setParameter(6, user.getOrganization().getId());
			query.setParameter(7, vendorType);
			query.setParameter(8, walkinType);
			query.setParameter(9, fromDate);
			query.setParameter(10, toDate);
		}
		// get OB for journal entries or sell transactions from
		// trialbalance_vendor_customer for < from_date i.e. suppose
		// from_date=21stApr2018, then whatever data in this table will be its OB.
		String headType = "";
		if (vendorType == IdosConstants.VENDOR) {
			headType = IdosConstants.HEAD_VENDOR;
		} else {
			headType = IdosConstants.HEAD_CUSTOMER;
		}
		List<Object[]> txnLists = query.getResultList();
		Double derivedOpenBal = 0.0;
		for (Object[] custData : txnLists) {
			Long vendorId = new Long(custData[4].toString());
			Vendor vendor = Vendor.findById(vendorId);
			if (vendor.getType() == vendorType) {
				Double creditAmt = 0.0;
				Double debitAmt = 0.0;
				Double openingBal = 0.0;
				if (custData[0] != null) {
					openingBal = (Double) custData[0];
				} else if (branchId == null || branchId == 0) {
					openingBal = vendor.getTotalOriginalOpeningBalance();
				}
				String custName = vendor.getName();
				if (custData[2] != null) {
					creditAmt = Double.parseDouble(String.valueOf(custData[2]));
				}
				if (custData[3] != null) {
					debitAmt = Double.parseDouble(String.valueOf(custData[3]));
				}
				// derivedOpenBal = getOpeningBalForSpecifics("TrialBalanceCustomerVendor",
				// openingBal, vendorId, user, fromDate, "vendor.id", IdosConstants.ASSETS,
				// branchId, em, headType);
				derivedOpenBal = openingBal;
				TrialBalance tb = new TrialBalance();
				tb.setAccountName(custName);
				tb.setSpecId(vendorId);
				tb.setSpecfaccountCode(vendorId.toString());
				tb.setOpeningBalance(derivedOpenBal);
				tb.setDebit(debitAmt);
				tb.setCredit(creditAmt);
				if (vendorType == IdosConstants.VENDOR) { // vendor credit sales: Liability
					tb.setTopLevelAccountCode("4000000000000000000");
					tb.setClosingBalance(derivedOpenBal + creditAmt - debitAmt);
					tb.setHeadType(IdosConstants.HEAD_VENDOR);
				} else { // customer
					tb.setTopLevelAccountCode("3000000000000000000");
					tb.setClosingBalance(derivedOpenBal + debitAmt - creditAmt);
					tb.setHeadType(IdosConstants.HEAD_CUSTOMER); // customer assets
				}
				trialBalanceList.add(tb);
			}
		}
	}

	/**
	 *
	 * @param tb
	 * @param user
	 * @param fromDate
	 * @param toDate
	 * @param em
	 * @param itemSpecifics
	 * @param vendorType    1- vendor, 2- customer, type=2 means customers to whom
	 *                      we have sold items on credit
	 */
	private void getTrialBalanceCustomerVendorCreditSalesTotal(TrialBalance tb, Users user, String fromDate,
			String toDate, Long branchId, EntityManager em, Specifics itemSpecifics, int vendorType) {
		String hql = null;
		int walkinType = IdosConstants.WALK_IN_VENDOR;
		if (branchId != null && branchId > 0) {
			if (IdosConstants.CUSTOMER == vendorType) {
				hql = CUSTVEND_BRANCH_JPQL_AST;
				walkinType = IdosConstants.WALK_IN_CUSTOMER;
			} else {
				hql = CUSTVEND_BRANCH_JPQL_LIB;
			}
		} else {
			if (IdosConstants.CUSTOMER == vendorType) {
				hql = CUSTVEND_ORG_JPQL_AST;
				walkinType = IdosConstants.WALK_IN_CUSTOMER;
			} else {
				hql = CUSTVEND_ORG_JPQL_LIB;
			}
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.INFO, "HQL: " + hql);
		}
		Query query = em.createNativeQuery(hql);
		if (branchId != null && branchId > 0) {
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, branchId);
			query.setParameter(3, user.getOrganization().getId());
			query.setParameter(4, branchId);
			query.setParameter(5, vendorType);
			query.setParameter(6, walkinType);
			query.setParameter(7, fromDate);
			query.setParameter(8, user.getOrganization().getId());
			query.setParameter(9, branchId);
			query.setParameter(10, vendorType);
			query.setParameter(11, walkinType);
			query.setParameter(12, fromDate);
			query.setParameter(13, toDate);
		} else {
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, user.getOrganization().getId());
			query.setParameter(3, vendorType);
			query.setParameter(4, walkinType);
			query.setParameter(5, fromDate);
			query.setParameter(6, user.getOrganization().getId());
			query.setParameter(7, vendorType);
			query.setParameter(8, walkinType);
			query.setParameter(9, fromDate);
			query.setParameter(10, toDate);
		}
		// get OB for journal entries or sell transactions from
		// trialbalance_vendor_customer for < from_date i.e. suppose
		// from_date=21stApr2018, then whatever data in this table will be its OB.
		String headType = "";
		if (vendorType == IdosConstants.VENDOR) {
			headType = IdosConstants.HEAD_VENDOR;
		} else {
			headType = IdosConstants.HEAD_CUSTOMER;
		}
		List<Object[]> txnLists = query.getResultList();
		Double creditAmt = 0.0;
		Double debitAmt = 0.0;
		Double openingBal = 0.0;
		Double derivedOpenBal = 0.0;
		for (Object[] custData : txnLists) {
			Long vendorId = new Long(custData[4].toString());
			Vendor vendor = Vendor.findById(vendorId);
			if (vendor != null && vendor.getType() == vendorType) {
				if (custData[0] != null) {
					openingBal += (Double) custData[0];
				} else if (branchId == null || branchId == 0) {
					openingBal += vendor.getTotalOriginalOpeningBalance();
				}
				String vendName = vendor.getName();
				if (custData[2] != null) {
					creditAmt += Double.parseDouble(String.valueOf(custData[2]));
				}
				if (custData[3] != null) {
					debitAmt += Double.parseDouble(String.valueOf(custData[3]));
				}
				// derivedOpenBal += getOpeningBalForSpecifics("TrialBalanceCustomerVendor",
				// openingBal, vendorId, user, fromDate, "vendor.id", IdosConstants.ASSETS,
				// branchId, em, headType);
			}
		}
		derivedOpenBal = openingBal;
		tb.setHeadType(IdosConstants.HEAD_SPECIFIC);
		tb.setOpeningBalance(derivedOpenBal);
		tb.setDebit(debitAmt);
		tb.setCredit(creditAmt);
		if (vendorType == IdosConstants.VENDOR) { // vendor credit sales: Liability
			tb.setClosingBalance(derivedOpenBal + creditAmt - debitAmt);
		} else {
			tb.setClosingBalance(derivedOpenBal + debitAmt - creditAmt);
		}
	}

	/**
	 *
	 * @param itemSpecifics
	 * @param trialBalanceList
	 * @param user
	 * @param fromDate
	 * @param toDate
	 * @param em
	 * @param vendorType       1- vendor,2 - customer
	 */
	private void getTrialBalanceCustomerVendorAdvance(Specifics itemSpecifics, List<TrialBalance> trialBalanceList,
			Users user, String fromDate, String toDate, Long branchId, EntityManager em, int vendorType) {
		String hql = null;
		int walkinType = IdosConstants.WALK_IN_VENDOR;
		;
		if (branchId != null && branchId > 0) {
			if (IdosConstants.CUSTOMER == vendorType) {
				hql = CUSTVEND_ADV_BRANCH_JPQL_LIB;
				walkinType = IdosConstants.WALK_IN_CUSTOMER;
				;
			} else {
				hql = CUSTVEND_ADV_BRANCH_JPQL_AST;
			}
		} else {
			if (IdosConstants.CUSTOMER == vendorType) {
				hql = CUSTVEND_ADV_ORG_JPQL_LIB;
				walkinType = IdosConstants.WALK_IN_CUSTOMER;
				;
			} else {
				hql = CUSTVEND_ADV_ORG_JPQL_AST;
			}
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.INFO, "HQL: " + hql);
		}
		Query query = em.createNativeQuery(hql);
		if (branchId != null && branchId > 0) {
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, branchId);
			query.setParameter(3, user.getOrganization().getId());
			query.setParameter(4, branchId);
			query.setParameter(5, vendorType);
			query.setParameter(6, walkinType);
			query.setParameter(7, fromDate);
			query.setParameter(8, user.getOrganization().getId());
			query.setParameter(9, branchId);
			query.setParameter(10, vendorType);
			query.setParameter(11, walkinType);
			query.setParameter(12, fromDate);
			query.setParameter(13, toDate);
		} else {
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, user.getOrganization().getId());
			query.setParameter(3, vendorType);
			query.setParameter(4, walkinType);
			query.setParameter(5, fromDate);
			query.setParameter(6, user.getOrganization().getId());
			query.setParameter(7, vendorType);
			query.setParameter(8, walkinType);
			query.setParameter(9, fromDate);
			query.setParameter(10, toDate);
		}

		List<Object[]> txnLists = query.getResultList();
		for (Object[] custData : txnLists) {
			Long vendorId = new Long(custData[4].toString());
			Vendor vendor = Vendor.findById(vendorId);
			if (vendor.getType() == vendorType) {
				Double creditAmt = 0.0;
				Double debitAmt = 0.0;
				Double openingBal = 0.0;
				if (custData[1] != null) {
					openingBal = (Double) custData[1];
				} else if (branchId == null || branchId == 0) {
					openingBal = vendor.getTotalOriginalOpeningBalanceAdvPaid();
				}
				String vendName = vendor.getName() + "_Adv";
				if (custData[2] != null) {
					creditAmt = Double.parseDouble(String.valueOf(custData[2]));
				}
				if (custData[3] != null) {
					debitAmt = Double.parseDouble(String.valueOf(custData[3]));
				}
				TrialBalance tb = new TrialBalance();
				tb.setAccountName(vendName);
				tb.setSpecId(vendorId);
				tb.setSpecfaccountCode(vendorId.toString());
				tb.setOpeningBalance(openingBal);
				tb.setDebit(debitAmt);
				tb.setCredit(creditAmt);
				if (vendorType == IdosConstants.VENDOR) {// vendor adv is asset
					tb.setTopLevelAccountCode("3000000000000000000");
					tb.setClosingBalance(openingBal + debitAmt - creditAmt);
					tb.setHeadType(IdosConstants.HEAD_VENDOR_ADV); // advance to Vendor asset
				} else {// customer adv
					tb.setTopLevelAccountCode("4000000000000000000");
					tb.setClosingBalance(openingBal + creditAmt - debitAmt);
					tb.setHeadType(IdosConstants.HEAD_CUSTOMER_ADV);
				}
				trialBalanceList.add(tb);
			}
		}
	}

	/**
	 *
	 * @param tb
	 * @param user
	 * @param fromDate
	 * @param toDate
	 * @param em
	 * @param itemSpecifics
	 * @param vendorType    1- vendor, 2 - customer, type=1 means vendors to whom we
	 *                      have paid advance
	 */
	private void getTrialBalanceCustomerVendorAdvanceTotal(TrialBalance tb, Users user, String fromDate, String toDate,
			Long branchId, EntityManager em, Specifics itemSpecifics, int vendorType) {
		String hql = null;
		int walkinType = IdosConstants.WALK_IN_VENDOR;
		if (branchId != null && branchId > 0) {
			if (IdosConstants.CUSTOMER == vendorType) {
				hql = CUSTVEND_ADV_BRANCH_JPQL_LIB;
				walkinType = IdosConstants.WALK_IN_CUSTOMER;
			} else {
				hql = CUSTVEND_ADV_BRANCH_JPQL_AST;
			}
		} else {
			if (IdosConstants.CUSTOMER == vendorType) {
				hql = CUSTVEND_ADV_ORG_JPQL_LIB;
				walkinType = IdosConstants.WALK_IN_CUSTOMER;
			} else {
				hql = CUSTVEND_ADV_ORG_JPQL_AST;
			}
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.INFO, "HQL: " + hql);
		}
		Query query = em.createNativeQuery(hql);
		if (branchId != null && branchId > 0) {
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, branchId);
			query.setParameter(3, user.getOrganization().getId());
			query.setParameter(4, branchId);
			query.setParameter(5, vendorType);
			query.setParameter(6, walkinType);
			query.setParameter(7, fromDate);
			query.setParameter(8, user.getOrganization().getId());
			query.setParameter(9, branchId);
			query.setParameter(10, vendorType);
			query.setParameter(11, walkinType);
			query.setParameter(12, fromDate);
			query.setParameter(13, toDate);
		} else {
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, user.getOrganization().getId());
			query.setParameter(3, vendorType);
			query.setParameter(4, walkinType);
			query.setParameter(5, fromDate);
			query.setParameter(6, user.getOrganization().getId());
			query.setParameter(7, vendorType);
			query.setParameter(8, walkinType);
			query.setParameter(9, fromDate);
			query.setParameter(10, toDate);
		}
		List<Object[]> txnLists = query.getResultList();
		Double creditAmt = 0.0;
		Double debitAmt = 0.0;
		Double openingBal = 0.0;
		for (Object[] custData : txnLists) {
			Long vendorId = new Long(custData[4].toString());
			Vendor vendor = Vendor.findById(vendorId);
			if (vendor.getType() == vendorType) {
				if (custData[1] != null) {
					openingBal += (Double) custData[1];
				} else if (branchId == null || branchId == 0) {
					openingBal += vendor.getTotalOriginalOpeningBalanceAdvPaid();
				}
				// String vendName = vendor.getName();
				if (custData[2] != null) {
					creditAmt += Double.parseDouble(String.valueOf(custData[2]));
				}
				if (custData[3] != null) {
					debitAmt += Double.parseDouble(String.valueOf(custData[3]));
				}
			}
		}
		tb.setHeadType(IdosConstants.HEAD_SPECIFIC);
		tb.setOpeningBalance(openingBal);
		tb.setDebit(debitAmt);
		tb.setCredit(creditAmt);
		if (vendorType == IdosConstants.VENDOR) { // vendor advance
			tb.setClosingBalance(openingBal + debitAmt - creditAmt);
		} else {
			tb.setClosingBalance(openingBal + creditAmt - debitAmt);
		}
	}

	private void getTrialBalanceBranchCash(List<TrialBalance> trialBalanceList, Users user, String fromDate,
			String toDate, final short cashType, Long branchId, EntityManager em) {
		log.log(Level.FINE, ">>>>>>>>>>Start ");
		String cashAcPostfix = null;
		String headType = null;
		StringBuilder sbr = new StringBuilder("select obj.branch.name, obj.branchDepositBoxKey.id,");
		if (cashType == IdosConstants.CASH) {
			sbr.append(" obj.branchDepositBoxKey.openingBalance, ");
			cashAcPostfix = " - Cash";
			headType = IdosConstants.HEAD_CASH;
		} else {
			sbr.append(" obj.branchDepositBoxKey.pettyCashOpeningBalance, ");
			cashAcPostfix = " - Pettycash";
			headType = IdosConstants.HEAD_PETTY;
		}
		sbr.append(" SUM(obj.creditAmount), SUM(obj.debitAmount) from TrialBalanceBranchCash obj where obj.cashType='");
		sbr.append(cashType).append("' and obj.presentStatus=1 and obj.organization=")
				.append(user.getOrganization().getId());
		if (branchId != null && branchId > 0L) {
			sbr.append(" and obj.branch.id = ").append(branchId);
		}
		sbr.append(" and obj.date between '").append(fromDate).append("' and '").append(toDate).append("'");
		sbr.append(" group by obj.branch.name, obj.branchDepositBoxKey.id, obj.branchDepositBoxKey.openingBalance");

		if (log.isLoggable(Level.FINE)) {
			log.log(Level.INFO, "HQL: " + sbr);
		}

		List<Object[]> txnLists = em.createQuery(sbr.toString()).getResultList();
		Double creditAmt = 0.0;
		Double debitAmt = 0.0;
		String cashAccNAme;
		Long cashID;
		StringBuilder selectedCashIDs = new StringBuilder();
		for (Object[] custData : txnLists) {
			cashAccNAme = custData[0].toString() + cashAcPostfix;
			cashID = Long.parseLong(String.valueOf(custData[1]));
			selectedCashIDs.append(cashID).append(",");
			creditAmt = Double.parseDouble(String.valueOf(custData[3]));
			debitAmt = Double.parseDouble(String.valueOf(custData[4]));
			Double openBal = Double.parseDouble(String.valueOf(custData[2] == null ? "0.0" : custData[2]));
			;
			Double derivedOpenBal = getOpeningBalForSpecifics("TrialBalanceBranchCash", openBal, cashID, user, fromDate,
					"branchDepositBoxKey", IdosConstants.ASSETS, branchId, em, headType);

			TrialBalance tb = new TrialBalance();
			tb.setAccountName(cashAccNAme);
			tb.setSpecId(cashID);
			tb.setSpecfaccountCode("1"); // leaf node
			tb.setTopLevelAccountCode("3000000000000000000");
			tb.setOpeningBalance(derivedOpenBal);
			tb.setDebit(debitAmt);
			tb.setCredit(creditAmt);
			tb.setHeadType(headType);
			tb.setClosingBalance(derivedOpenBal + debitAmt - creditAmt);
			trialBalanceList.add(tb);
		}

		StringBuilder strSQL = new StringBuilder("select obj from BranchDepositBoxKey obj where obj.organization=")
				.append(user.getOrganization().getId())
				.append(" and obj.branch.presentStatus=1 and obj.presentStatus=1");
		if (branchId != null && branchId > 0L) {
			strSQL.append(" and obj.branch.id = ").append(branchId);
		}
		if (selectedCashIDs.length() > 0) {
			String str = selectedCashIDs.substring(0, selectedCashIDs.length() - 1);
			strSQL.append(" and obj.id not in (").append(str).append(")");
		}
		List<BranchDepositBoxKey> depositBoxList = genericDao.executeSimpleQuery(strSQL.toString(), em);
		for (BranchDepositBoxKey cashAccount : depositBoxList) {
			TrialBalance tb = new TrialBalance();
			tb.setAccountName(cashAccount.getBranch().getName() + cashAcPostfix);
			tb.setSpecId(cashAccount.getId());
			tb.setSpecfaccountCode("1");
			tb.setTopLevelAccountCode("3000000000000000000");

			if (cashType == IdosConstants.CASH) {
				Double derivedOpenBal = getOpeningBalForSpecifics("TrialBalanceBranchCash",
						cashAccount.getOpeningBalance(), cashAccount.getId(), user, fromDate, "branchDepositBoxKey",
						IdosConstants.ASSETS, branchId, em, headType);
				tb.setOpeningBalance(derivedOpenBal);
				tb.setClosingBalance(derivedOpenBal);
			} else {
				Double derivedOpenBal = getOpeningBalForSpecifics("TrialBalanceBranchCash",
						cashAccount.getPettyCashOpeningBalance(), cashAccount.getId(), user, fromDate,
						"branchDepositBoxKey", IdosConstants.ASSETS, branchId, em, headType);
				tb.setOpeningBalance(derivedOpenBal);
				tb.setClosingBalance(derivedOpenBal);
			}
			tb.setDebit(0.0);
			tb.setCredit(0.0);
			tb.setHeadType(headType);
			trialBalanceList.add(tb);
		}
		log.log(Level.FINE, ">>>>>>>>>>End ");
	}

	/**
	 *
	 * @param tb
	 * @param user
	 * @param fromDate
	 * @param toDate
	 * @param itemSpecifics
	 * @param cashType
	 * @param em
	 */
	@Override
	public double getTrialBalanceBranchCashTotal(TrialBalance tb, Users user, String fromDate, String toDate,
			Specifics itemSpecifics, short cashType, Long branchId, EntityManager em) {
		StringBuilder sbr = new StringBuilder(
				"select obj.branch.name, SUM(obj.creditAmount), SUM(obj.debitAmount) from TrialBalanceBranchCash obj where obj.cashType=");
		sbr.append(cashType).append(" and obj.organization= ").append(user.getOrganization().getId());
		if (branchId != null && branchId > 0) {
			sbr.append(" and obj.branch.id= ").append(branchId);
		}
		sbr.append(" and obj.presentStatus=1 and obj.date  between '").append(fromDate).append("' and '").append(toDate)
				.append("' group by obj.branch.name, obj.branchDepositBoxKey");
		String headType = IdosConstants.HEAD_CASH;
		if (cashType == IdosConstants.PETTY_CASH) {
			headType = IdosConstants.HEAD_PETTY;
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.INFO, "HQL: " + sbr);
		}
		String queryString = em.createQuery(sbr.toString()).unwrap(org.hibernate.Query.class).getQueryString();
		System.out.println("Generated Query: " + queryString);
		List<Object[]> txnLists = em.createQuery(sbr.toString()).getResultList();
		Double creditAmt = 0.0;
		Double debitAmt = 0.0;
		Double openBal = getOpeningBalForSpecifics("TrialBalanceBranchCash", 0.0, 0L, user, fromDate,
				"branchDepositBoxKey", IdosConstants.ASSETS, branchId, em, headType);
		if (!txnLists.isEmpty()) {
			for (Object[] custData : txnLists) {
				// branchName = custData[0].toString();
				if (custData[1] != null)
					creditAmt += Double.parseDouble(String.valueOf(custData[1]));
				if (custData[2] != null)
					debitAmt += Double.parseDouble(String.valueOf(custData[2]));
			}
		}
		tb.setHeadType(IdosConstants.HEAD_SPECIFIC);
		tb.setOpeningBalance(openBal);
		tb.setDebit(debitAmt);
		tb.setCredit(creditAmt);
		tb.setClosingBalance(openBal + debitAmt - creditAmt);
		return tb.getClosingBalance();
	}

	private void getTrialBalanceBranchBank(Specifics itemSpecifics, List<TrialBalance> trialBalanceList, Users user,
			String fromDate, String toDate, Long branchId, EntityManager em) {
		StringBuilder sbr = new StringBuilder(
				"select a.branchBankAccounts.bankName, a.branch.name, a.branchBankAccounts.id, a.branchBankAccounts.openingBalance, SUM(a.creditAmount), SUM(a.debitAmount) from TrialBalanceBranchBank a where a.organization=");
		sbr.append(user.getOrganization().getId());
		if (branchId != null && branchId > 0L) {
			sbr.append(" and a.branch.id = ").append(branchId);
		}
		if ("ORG".equals(OrganizationConfigCache.getParamValue(user.getId(), "bank.account.type"))) {
			sbr.append(" and a.presentStatus=1 and a.date >= '").append(fromDate).append("' and a.date <= '")
					.append(toDate).append("'").append(" group by a.branchBankAccounts.accountNumber");
		} else {
			sbr.append(" and a.presentStatus=1 and a.date >= '").append(fromDate).append("' and a.date <= '")
					.append(toDate).append("'")
					.append(" group by a.branchBankAccounts.bankName, a.branch.name, a.branchBankAccounts.id, a.branchBankAccounts.openingBalance");
		}
		/*StringBuilder sbr1 = new StringBuilder("SELECT bba.bankName, b.name, bba.id, bba.openingBalance, SUM(tb.creditAmount), SUM(tb.debitAmount) FROM TrialBalanceBranchBank tb ");
		sbr1.append("INNER JOIN BranchBankAccountMapping bbam ON tb.branch.id = bbam.branch.id ");
		sbr1.append("INNER JOIN Branch b ON b.id = bbam.branch.id ");
		sbr1.append("INNER JOIN BranchBankAccounts bba ON bba.id = bbam.branchBankAccounts.id ");
		sbr1.append("WHERE tb.organization = ").append(user.getOrganization().getId());

		if (branchId != null && branchId > 0L) {
				sbr1.append(" AND tb.branch.id = ").append(branchId);
		}

		if ("ORG".equals(OrganizationConfigCache.getParamValue(user.getId(), "bank.account.type"))) {
				sbr1.append(" AND tb.presentStatus = 1 AND tb.date >= '").append(fromDate).append("' AND tb.date <= '").append(toDate).append("'");
				sbr1.append(" GROUP BY bba.accountNumber");
		} else {
				sbr1.append(" AND tb.presentStatus = 1 AND tb.date >= '").append(fromDate).append("' AND tb.date <= '").append(toDate).append("'");
				sbr1.append(" GROUP BY bba.bankName, b.name, bba.id, bba.openingBalance");
		}*/

		Double creditAmt = 0.0;
		Double debitAmt = 0.0;
		String branchName;
		Long bankID;
		StringBuilder selectdBankIDList = new StringBuilder();
		List<Object[]> txnLists = em.createQuery(sbr.toString()).getResultList();
		/*List<Object[]> txnLists1 = em.createQuery(sbr1.toString()).getResultList();
		txnLists.addAll(txnLists1);*/
		for (Object[] custData : txnLists) {
			branchName = custData[0] == null ? ""
					: custData[0].toString() + " - " + custData[1] == null ? "" : custData[1].toString();
			bankID = Long.parseLong(String.valueOf(custData[2]));
			selectdBankIDList.append(bankID).append(",");
			Double openBal = Double.parseDouble(String.valueOf(custData[3] == null ? 0.0 : custData[3]));
			creditAmt = Double.parseDouble(String.valueOf(custData[4] == null ? 0.0 : custData[4]));
			debitAmt = Double.parseDouble(String.valueOf(custData[5] == null ? 0.0 : custData[5]));

			Double derivedOpeningBal = getOpeningBalForSpecifics("TrialBalanceBranchBank", openBal, bankID, user,
					fromDate, "branchBankAccounts", IdosConstants.ASSETS, branchId, em, IdosConstants.HEAD_BANK);
			TrialBalance tb = new TrialBalance();
			tb.setAccountName(branchName);
			tb.setSpecId(bankID);
			tb.setSpecfaccountCode("1");
			tb.setTopLevelAccountCode("3000000000000000000");
			tb.setOpeningBalance(derivedOpeningBal);
			tb.setDebit(debitAmt);
			tb.setCredit(creditAmt);
			tb.setClosingBalance(derivedOpeningBal + debitAmt - creditAmt);
			tb.setHeadType(IdosConstants.HEAD_BANK);
			trialBalanceList.add(tb);
		}

		StringBuilder strSQL = new StringBuilder("select obj from BranchBankAccounts obj where obj.organization=")
				.append(user.getOrganization().getId()).append(" and obj.presentStatus=1");
		if (branchId != null && branchId > 0L) {
			strSQL.append(" and obj.branch.id = ").append(branchId);
		}
		if (selectdBankIDList.length() > 0) {
			String str = selectdBankIDList.substring(0, selectdBankIDList.length() - 1);
			strSQL.append(" and obj.id not in (").append(str).append(")");
		}

		List<BranchBankAccounts> bankAccountList = genericDao.executeSimpleQuery(strSQL.toString(), em);
		for (BranchBankAccounts bankAccount : bankAccountList) {
			TrialBalance tb = new TrialBalance();
			tb.setAccountName(bankAccount.getBankName() + " - " + bankAccount.getBranch().getName());
			tb.setSpecId(bankAccount.getId());
			tb.setSpecfaccountCode("1");
			tb.setTopLevelAccountCode("3000000000000000000");

			Double derivedOpeningBal = getOpeningBalForSpecifics("TrialBalanceBranchBank",
					bankAccount.getOpeningBalance(), bankAccount.getId(), user, fromDate, "branchBankAccounts",
					IdosConstants.ASSETS, branchId, em, IdosConstants.HEAD_BANK);

			tb.setOpeningBalance(derivedOpeningBal);
			tb.setDebit(0.0);
			tb.setCredit(0.0);
			tb.setClosingBalance(derivedOpeningBal);
			tb.setHeadType(IdosConstants.HEAD_BANK);
			trialBalanceList.add(tb);
		}

		/*StringBuilder strSQL1 = new StringBuilder("SELECT obj FROM BranchBankAccounts obj ");
		strSQL1.append("JOIN BranchBankAccountMapping mapping ON obj.id = mapping.branchBankAccounts.id ");
		strSQL1.append("WHERE obj.organization=").append(user.getOrganization().getId());
		strSQL1.append(" AND obj.presentStatus=1");

		if (branchId != null && branchId > 0L) {
				strSQL1.append(" AND mapping.branch.id = ").append(branchId);
		}

		if (selectdBankIDList.length() > 0) {
				String str = selectdBankIDList.substring(0, selectdBankIDList.length() - 1);
				strSQL1.append(" AND obj.id NOT IN (").append(str).append(")");
		}

		List<BranchBankAccounts> bankAccountList1 = genericDao.executeSimpleQuery(strSQL1.toString(), em);
		for (BranchBankAccounts bankAccount1 : bankAccountList1) {
			TrialBalance tb = new TrialBalance();
			BranchBankAccountMapping bbaMapping = bankAccount1.getBranchBankMapping(em,bankAccount1, branchId);
			tb.setAccountName(bankAccount1.getBankName() + " - " + bbaMapping.getBranch().getName());
			tb.setSpecId(bbaMapping.getSpecifics().getId());
			tb.setSpecfaccountCode("1");
			tb.setTopLevelAccountCode("3000000000000000000");
			System.out.println(bbaMapping.getBranchBankAccountBalance().getAmountBalance()+"......."+bbaMapping.getBranch().getId());
			Double derivedOpeningBal = getOpeningBalForSpecifics("TrialBalanceBranchBank",
			bbaMapping.getBranchBankAccountBalance().getAmountBalance(), bbaMapping.getBranch().getId(), user, fromDate, "branchBankAccounts",
					IdosConstants.ASSETS, branchId, em, IdosConstants.HEAD_BANK);

			tb.setOpeningBalance(derivedOpeningBal);
			tb.setDebit(0.0);
			tb.setCredit(0.0);
			tb.setClosingBalance(derivedOpeningBal);
			tb.setHeadType(IdosConstants.HEAD_BANK);
			trialBalanceList.add(tb);
		}*/
	}

	@Override
	public double getTrialBalanceBranchBankTotal(TrialBalance tb, Users user, String fromDate, String toDate,
			Specifics itemSpecifics, Long branchId, EntityManager em) {
		StringBuilder sbr = new StringBuilder(
				"select obj.branch.name, SUM(obj.creditAmount), SUM(obj.debitAmount) from TrialBalanceBranchBank obj where obj.organization=")
				.append(user.getOrganization().getId());
		if (branchId != null && branchId > 0) {
			sbr.append(" and obj.branch.id= ").append(branchId);
		}
		sbr.append(" and obj.presentStatus=1 and obj.date  between '").append(fromDate).append("' and '").append(toDate)
				.append("'");
		sbr.append(" group by obj.branch.name, obj.branch");
		System.out.println("getTrialBalanceBranchBankTotal : " + sbr.toString());
		List<Object[]> txnLists = em.createQuery(sbr.toString()).getResultList();
		Double creditAmt = 0.0;
		Double debitAmt = 0.0;
		String branchName;
		Double openBal = getOpeningBalForSpecifics("TrialBalanceBranchBank", 0.0, 0L, user, fromDate,
				"branchBankAccounts", IdosConstants.ASSETS, branchId, em, IdosConstants.HEAD_BANK);
		if (!txnLists.isEmpty()) {
			for (Object[] custData : txnLists) {
				if (custData[0] != null) {
					branchName = custData[0].toString();
				}
				if (custData[1] != null) {
					creditAmt += Double.parseDouble(String.valueOf(custData[1]));
				}
				if (custData[2] != null) {
					debitAmt += Double.parseDouble(String.valueOf(custData[2]));
				}
			}
		}
		tb.setHeadType(IdosConstants.HEAD_SPECIFIC);
		tb.setOpeningBalance(openBal);
		tb.setDebit(debitAmt);
		tb.setCredit(creditAmt);
		tb.setClosingBalance(openBal + debitAmt - creditAmt);
		return tb.getClosingBalance();
	}

	/**
	 *
	 * @param user
	 * @param fromDate
	 * @param em
	 * @param taxType
	 * @param taxId
	 * @param openingBal
	 * @param particularType
	 * @return derived opening balance for the head
	 */
	@Override
	public Double getOpeningBalForTaxes(Users user, String fromDate, Long branchId, EntityManager em,
			final short taxType, Long taxId, Double openingBal, final short particularType) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "****** Start fromDate: " + fromDate + " branchId: " + branchId + " taxType: " + taxType
					+ " particularType: " + particularType);
		StringBuilder sbr = new StringBuilder(
				"select SUM(obj.creditAmount), SUM(obj.debitAmount) from TrialBalanceTaxes obj where obj.taxType=");
		sbr.append(taxType).append(" and obj.organization.id=").append(user.getOrganization().getId());
		if (branchId != null && branchId > 0L) {
			sbr.append(" and obj.branch.id = ").append(branchId);
			sbr.append(" and obj.branchTaxes.branch.id = ").append(branchId);
		}
		sbr.append(" and obj.presentStatus=1 and obj.date < '").append(fromDate).append("'");
		if (taxId != 0) {
			sbr.append(" and obj.branchTaxes.id=").append(taxId);
		}
		sbr.append(" group by obj.branchTaxes");

		if (log.isLoggable(Level.FINE)) {
			log.log(Level.INFO, "hql: " + sbr);
		}

		Map<String, Object> criterias = new HashMap<String, Object>();
		List<Object[]> txnLists = em.createQuery(sbr.toString()).getResultList();
		Double creditAmt = 0.0;
		Double debitAmt = 0.0;
		for (Object[] custData : txnLists) {
			if (custData[0] != null)
				creditAmt += Double.parseDouble(String.valueOf(custData[0]));
			if (custData[1] != null)
				debitAmt += Double.parseDouble(String.valueOf(custData[1]));
			if (log.isLoggable(Level.FINE))
				log.log(Level.FINE, "**loop**** End debitAmt: " + debitAmt + " creditAmt: " + creditAmt
						+ " openingBal: " + openingBal);
		}

		// Get opening Bal for all taxes defined for that org
		if (taxId == 0) {
			openingBal = 0.0;
			Query query = null;
			if (branchId != null && branchId > 0) {
				query = em.createQuery(TAX_BRANCH_JPQL);
				query.setParameter(1, user.getOrganization().getId());
				query.setParameter(2, branchId);
				query.setParameter(3, new Integer(taxType));
			} else {
				query = em.createQuery(TAX_ORG_JPQL);
				query.setParameter(1, user.getOrganization().getId());
				query.setParameter(2, new Integer(taxType));
			}
			List<Double> branchTaxes = query.getResultList();
			if (branchTaxes.size() > 0) {
				openingBal = branchTaxes.get(0);
			}
		}

		if (openingBal == null) {
			openingBal = 0.0;
		}

		if (creditAmt == null) {
			creditAmt = 0.0;
		}
		if (debitAmt == null) {
			debitAmt = 0.0;
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE,
					"****** End debitAmt: " + debitAmt + " creditAmt: " + creditAmt + " openingBal: " + openingBal);

		if (particularType == IdosConstants.ASSETS) {
			openingBal = openingBal + debitAmt - creditAmt;
		} else if (particularType == IdosConstants.LIABILITIES) {
			openingBal = openingBal + creditAmt - debitAmt;
		}

		return openingBal;
	}

	/**
	 *
	 * @param itemSpecifics
	 * @param trialBalanceList
	 * @param user
	 * @param fromDate
	 * @param toDate
	 * @param em
	 * @param taxType
	 * @param particularType
	 */
	private void getTrialBalanceBranchForTaxes(Specifics itemSpecifics, List<TrialBalance> trialBalanceList, Users user,
			String fromDate, String toDate, Long branchId, EntityManager em, final short taxType,
			final short particularType) {
		StringBuilder sbr = new StringBuilder(
				"select obj.branchTaxes.id, obj.branchTaxes.taxName, obj.branch.name, obj.branchTaxes.openingBalance, SUM(obj.creditAmount), SUM(obj.debitAmount), obj.branchTaxes.presentStatus from TrialBalanceTaxes obj where obj.taxType=")
				.append(taxType).append(" and obj.organization.id=");
		sbr.append(user.getOrganization().getId());
		if (branchId != null && branchId > 0L) {
			sbr.append(" and obj.branch.id = ").append(branchId);
			sbr.append(" and obj.branchTaxes.branch.id = ").append(branchId);
		}
		sbr.append(" and obj.presentStatus=1 and obj.date between '").append(fromDate).append("' and '").append(toDate)
				.append("' group by obj.branchTaxes.id, obj.branchTaxes.taxName, obj.branch.id, obj.branch.name, obj.branchTaxes.openingBalance, obj.branchTaxes.presentStatus");

		Double creditAmt = 0.0;
		Double debitAmt = 0.0;
		Double openingBal = 0.0;
		String taxName;
		Long taxId;
		List<Object[]> txnLists = em.createQuery(sbr.toString()).getResultList();
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.INFO, "HQL: " + sbr);
		}

		StringBuilder selectedIDs = new StringBuilder();
		for (Object[] custData : txnLists) {
			taxId = (Long) custData[0];
			selectedIDs.append(taxId).append(",");
			if (user.getOrganization().getGstCountryCode() != null
					&& !"".equals(user.getOrganization().getGstCountryCode())) {
				taxName = custData[1].toString();
				creditAmt = Double.parseDouble(String.valueOf(custData[4] == null ? 0.0 : custData[4]));
				debitAmt = Double.parseDouble(String.valueOf(custData[5] == null ? 0.0 : custData[5]));
			} else {
				taxName = custData[1].toString() + " - " + custData[2].toString();
				creditAmt = Double.parseDouble(String.valueOf(custData[4] == null ? 0.0 : custData[4]));
				debitAmt = Double.parseDouble(String.valueOf(custData[5] == null ? 0.0 : custData[5]));
			}
			int presentStatus = (int) custData[6];
			openingBal = Double.parseDouble(String.valueOf(custData[3] == null ? 0.0 : custData[3]));
			Double drivedOpeningBal = getOpeningBalForTaxes(user, fromDate, branchId, em, taxType, taxId, openingBal,
					particularType);
			TrialBalance tb = new TrialBalance();
			if (presentStatus == 0) {
				tb.setAccountName(taxName + " (Disabled)");
			} else {
				tb.setAccountName(taxName);
			}
			tb.setSpecId(taxId);
			tb.setSpecfaccountCode("1");
			tb.setOpeningBalance(drivedOpeningBal);
			tb.setDebit(debitAmt);
			tb.setCredit(creditAmt);
			if (particularType == IdosConstants.LIABILITIES) {
				tb.setClosingBalance(drivedOpeningBal + creditAmt - debitAmt);
				tb.setTopLevelAccountCode("4000000000000000000");
			} else if (particularType == IdosConstants.ASSETS) {
				tb.setClosingBalance(drivedOpeningBal + debitAmt - creditAmt);
				tb.setTopLevelAccountCode("3000000000000000000");
			}
			if (IdosConstants.OUTPUT_TAX == taxType || IdosConstants.INPUT_TAX == taxType) {
				tb.setHeadType(IdosConstants.HEAD_TAXS);
			} else if (IdosConstants.OUTPUT_SGST == taxType || IdosConstants.INPUT_SGST == taxType) {
				tb.setHeadType(IdosConstants.HEAD_SGST);
			} else if (IdosConstants.OUTPUT_CGST == taxType || IdosConstants.INPUT_CGST == taxType) {
				tb.setHeadType(IdosConstants.HEAD_CGST);
			} else if (IdosConstants.OUTPUT_IGST == taxType || IdosConstants.INPUT_IGST == taxType) {
				tb.setHeadType(IdosConstants.HEAD_IGST);
			} else if (IdosConstants.OUTPUT_CESS == taxType || IdosConstants.INPUT_CESS == taxType) {
				tb.setHeadType(IdosConstants.HEAD_CESS);
			} else if (IdosConstants.RCM_SGST_IN == taxType) {
				tb.setHeadType(IdosConstants.HEAD_RCM_SGST_IN);
			} else if (IdosConstants.RCM_CGST_IN == taxType) {
				tb.setHeadType(IdosConstants.HEAD_RCM_CGST_IN);
			} else if (IdosConstants.RCM_IGST_IN == taxType) {
				tb.setHeadType(IdosConstants.HEAD_RCM_IGST_IN);
			} else if (IdosConstants.RCM_CESS_IN == taxType) {
				tb.setHeadType(IdosConstants.HEAD_RCM_CESS_IN);
			} else if (IdosConstants.RCM_SGST_OUTPUT == taxType) {
				tb.setHeadType(IdosConstants.HEAD_RCM_SGST_OUTPUT);
			} else if (IdosConstants.RCM_CGST_OUTPUT == taxType) {
				tb.setHeadType(IdosConstants.HEAD_RCM_CGST_OUTPUT);
			} else if (IdosConstants.RCM_IGST_OUTPUT == taxType) {
				tb.setHeadType(IdosConstants.HEAD_RCM_IGST_OUTPUT);
			} else if (IdosConstants.RCM_CESS_OUTPUT == taxType) {
				tb.setHeadType(IdosConstants.HEAD_RCM_CESS_OUTPUT);
			}
			trialBalanceList.add(tb);
		}

		StringBuilder strSQL = new StringBuilder("select obj from BranchTaxes obj where obj.organization=")
				.append(user.getOrganization().getId());
		if (branchId != null && branchId > 0L) {
			strSQL.append(" and obj.branch.id = ").append(branchId);
		}
		strSQL.append(" and obj.presentStatus=1 and obj.taxType=").append(taxType);
		if (selectedIDs.length() > 0) {
			String str = selectedIDs.substring(0, selectedIDs.length() - 1);
			strSQL.append(" and obj.id not in (").append(str).append(")");
		}
		List<BranchTaxes> taxList = genericDao.executeSimpleQuery(strSQL.toString(), em);
		for (BranchTaxes branchTax : taxList) {
			TrialBalance tb = new TrialBalance();
			tb.setAccountName(branchTax.getTaxName());
			tb.setSpecId(branchTax.getId());
			tb.setSpecfaccountCode("1");
			tb.setDebit(0.0);
			tb.setCredit(0.0);
			if (particularType == IdosConstants.LIABILITIES) {
				tb.setTopLevelAccountCode("4000000000000000000");
			} else if (particularType == IdosConstants.ASSETS) {
				tb.setTopLevelAccountCode("3000000000000000000");
			}
			if (IdosConstants.OUTPUT_TAX == taxType || IdosConstants.INPUT_TAX == taxType) {
				tb.setHeadType(IdosConstants.HEAD_TAXS);
			} else if (IdosConstants.OUTPUT_SGST == taxType || IdosConstants.INPUT_SGST == taxType) {
				tb.setHeadType(IdosConstants.HEAD_SGST);
			} else if (IdosConstants.OUTPUT_CGST == taxType || IdosConstants.INPUT_CGST == taxType) {
				tb.setHeadType(IdosConstants.HEAD_CGST);
			} else if (IdosConstants.OUTPUT_IGST == taxType || IdosConstants.INPUT_IGST == taxType) {
				tb.setHeadType(IdosConstants.HEAD_IGST);
			} else if (IdosConstants.OUTPUT_CESS == taxType || IdosConstants.INPUT_CESS == taxType) {
				tb.setHeadType(IdosConstants.HEAD_CESS);
			} else if (IdosConstants.RCM_SGST_IN == taxType) {
				tb.setHeadType(IdosConstants.HEAD_RCM_SGST_IN);
			} else if (IdosConstants.RCM_CGST_IN == taxType) {
				tb.setHeadType(IdosConstants.HEAD_RCM_CGST_IN);
			} else if (IdosConstants.RCM_IGST_IN == taxType) {
				tb.setHeadType(IdosConstants.HEAD_RCM_IGST_IN);
			} else if (IdosConstants.RCM_CESS_IN == taxType) {
				tb.setHeadType(IdosConstants.HEAD_RCM_CESS_IN);
			} else if (IdosConstants.RCM_SGST_OUTPUT == taxType) {
				tb.setHeadType(IdosConstants.HEAD_RCM_SGST_OUTPUT);
			} else if (IdosConstants.RCM_CGST_OUTPUT == taxType) {
				tb.setHeadType(IdosConstants.HEAD_RCM_CGST_OUTPUT);
			} else if (IdosConstants.RCM_IGST_OUTPUT == taxType) {
				tb.setHeadType(IdosConstants.HEAD_RCM_IGST_OUTPUT);
			} else if (IdosConstants.RCM_CESS_OUTPUT == taxType) {
				tb.setHeadType(IdosConstants.HEAD_RCM_CESS_OUTPUT);
			}
			Double drivedOpeningBal = getOpeningBalForTaxes(user, fromDate, branchId, em, taxType, branchTax.getId(),
					branchTax.getOpeningBalance(), particularType);
			tb.setOpeningBalance(drivedOpeningBal);
			tb.setClosingBalance(drivedOpeningBal);
			trialBalanceList.add(tb);
		}
	}

	/**
	 *
	 * @param itemSpecifics
	 * @param trialBalanceList
	 * @param user
	 * @param fromDate
	 * @param toDate
	 * @param em
	 * @param taxType
	 * @param particularType
	 * @param mappedID
	 */
	private void getTrialBalanceForTds(Specifics itemSpecifics, List<TrialBalance> trialBalanceList, Users user,
			String fromDate, String toDate, Long branchId, EntityManager em, final int taxType,
			final short particularType, final int mappedID) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "****** Start " + branchId);
		Query query = null;
		String sql = null;
		if (branchId != null && branchId > 0) {
			if (IdosConstants.ASSETS == particularType) {
				sql = TDS_BRANCH_SQL8;
			} else {
				sql = TDS_BRANCH_SQL9;
			}
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.INFO, "branchId: " + branchId + " taxType: " + taxType + " mappedID: " + mappedID
						+ " fromDate: " + fromDate + " toDate: " + toDate);
				log.log(Level.INFO, "SQL: " + sql);
			}
			query = em.createNativeQuery(sql);
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, branchId);
			query.setParameter(3, mappedID);
			query.setParameter(4, user.getOrganization().getId());
			query.setParameter(5, branchId);
			query.setParameter(6, mappedID);
			query.setParameter(7, taxType);
			query.setParameter(8, fromDate);
			query.setParameter(9, user.getOrganization().getId());
			query.setParameter(10, branchId);
			query.setParameter(11, mappedID);
			query.setParameter(12, taxType);
			query.setParameter(13, fromDate);
			query.setParameter(14, toDate);
		} else {
			if (IdosConstants.ASSETS == particularType) {
				sql = TDS_ORG_SQL8;
			} else {
				sql = TDS_ORG_SQL9;
			}
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.INFO, " taxType: " + taxType + " mappedID: " + mappedID + " fromDate: " + fromDate
						+ " toDate: " + toDate);
				log.log(Level.INFO, "SQL: " + sql);
			}
			query = em.createNativeQuery(sql);
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, mappedID);
			query.setParameter(3, user.getOrganization().getId());
			query.setParameter(4, mappedID);
			query.setParameter(5, taxType);
			query.setParameter(6, fromDate);
			query.setParameter(7, user.getOrganization().getId());
			query.setParameter(8, mappedID);
			query.setParameter(9, taxType);
			query.setParameter(10, fromDate);
			query.setParameter(11, toDate);
		}
		List<Object[]> txnLists = query.getResultList();

		Double creditAmt = 0.0;
		Double debitAmt = 0.0;
		Double openBal = 0.0;
		for (Object[] custData : txnLists) {
			/*
			 * creditAmt += Double.parseDouble(String.valueOf(custData[0]));
			 * debitAmt += Double.parseDouble(String.valueOf(custData[1]));
			 * openBal += Double.parseDouble(String.valueOf(custData[2]));
			 */

			if (custData[0] != null)
				openBal += Double.parseDouble(String.valueOf(custData[0]));
			if (custData[1] != null) {
				creditAmt += Double.parseDouble(String.valueOf(custData[1]));
			}
			if (custData[2] != null) {
				debitAmt += Double.parseDouble(String.valueOf(custData[2]));
			}
		}

		TrialBalance tb = new TrialBalance();
		tb.setAccountName(itemSpecifics.getName());
		tb.setSpecId(itemSpecifics.getId());
		tb.setSpecfaccountCode("1");
		tb.setHeadType(IdosConstants.HEAD_TDS);
		tb.setOpeningBalance(openBal);
		tb.setDebit(debitAmt);
		tb.setCredit(creditAmt);
		if (IdosConstants.ASSETS == particularType) {
			tb.setClosingBalance(openBal + debitAmt - creditAmt);
		} else if (IdosConstants.LIABILITIES == particularType) {
			tb.setClosingBalance(openBal + creditAmt - debitAmt);
		}
		tb.setIdentificationForDataValid(String.valueOf(mappedID));
		trialBalanceList.add(tb);
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "****** End ");
	}

	/**
	 *
	 * @param tb
	 * @param user
	 * @param fromDate
	 * @param toDate
	 * @param em
	 * @param taxType
	 * @param particularType
	 */
	private void getTrialBalanceForBranchTaxesTotal(TrialBalance tb, Users user, String fromDate, String toDate,
			Long branchId, EntityManager em, final short taxType, final short particularType) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "****** Start fromDate: " + fromDate + " toDate: " + toDate + " branchId: " + branchId
					+ " taxType: " + taxType + " particularType: " + particularType);
		StringBuilder sbr = new StringBuilder(
				"select SUM(obj.creditAmount), SUM(obj.debitAmount) from TrialBalanceTaxes obj where obj.taxType=");
		sbr.append(taxType).append(" and obj.organization=").append(user.getOrganization().getId());
		if (branchId != null && branchId > 0) {
			sbr.append(" and obj.branch.id= ").append(branchId);
		}
		sbr.append(" and obj.presentStatus=1 and obj.date between '").append(fromDate).append("' and '").append(toDate)
				.append("' group by obj.branchTaxes");

		if (log.isLoggable(Level.FINE))
			log.log(Level.INFO, "hql: " + sbr);

		List<Object[]> txnLists = em.createQuery(sbr.toString()).getResultList();
		Double creditAmt = 0.0;
		Double debitAmt = 0.0;
		if (!txnLists.isEmpty()) {
			for (Object[] custData : txnLists) {
				if (custData[0] != null)
					creditAmt += Double.parseDouble(String.valueOf(custData[0]));
				if (custData[1] != null)
					debitAmt += Double.parseDouble(String.valueOf(custData[1]));
			}
		}
		Double openBal = getOpeningBalForTaxes(user, fromDate, branchId, em, taxType, 0L, 0.0, particularType);
		tb.setOpeningBalance(openBal);
		if (IdosConstants.ASSETS == particularType) {
			tb.setClosingBalance(openBal + debitAmt - creditAmt);
		} else if (IdosConstants.LIABILITIES == particularType) {
			tb.setClosingBalance(openBal + creditAmt - debitAmt);
		}
		tb.setDebit(debitAmt);
		tb.setCredit(creditAmt);
		tb.setHeadType(IdosConstants.HEAD_SPECIFIC);
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "****** End " + openBal);
	}

	/**
	 *
	 * @param tb
	 * @param user
	 * @param fromDate
	 * @param toDate
	 * @param em
	 * @param taxType
	 * @param particularType
	 * @param mappedID
	 */
	private void getTrialBalanceForTdsTotal(TrialBalance tb, Users user, String fromDate, String toDate, Long branchId,
			EntityManager em, final int taxType, final short particularType, final int mappedID) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "****** Start " + branchId);
		Query query = null;
		String sql = null;
		if (branchId != null && branchId > 0) {
			if (IdosConstants.ASSETS == particularType) {
				sql = TDS_BRANCH_SQL8;
			} else {
				sql = TDS_BRANCH_SQL9;
			}
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.INFO, " taxType: " + taxType + " mappedID: " + mappedID);
				log.log(Level.INFO, "SQL: " + sql);
			}
			query = em.createNativeQuery(sql);
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, branchId);
			query.setParameter(3, mappedID);
			query.setParameter(4, user.getOrganization().getId());
			query.setParameter(5, branchId);
			query.setParameter(6, mappedID);
			query.setParameter(7, taxType);
			query.setParameter(8, fromDate);
			query.setParameter(9, user.getOrganization().getId());
			query.setParameter(10, branchId);
			query.setParameter(11, mappedID);
			query.setParameter(12, taxType);
			query.setParameter(13, fromDate);
			query.setParameter(14, toDate);
		} else {
			if (IdosConstants.ASSETS == particularType) {
				sql = TDS_ORG_SQL8;
			} else {
				sql = TDS_ORG_SQL9;
			}
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.INFO, " taxType: " + taxType + " mappedID: " + mappedID + " fromDate= " + fromDate
						+ " toDate= " + toDate);
				log.log(Level.INFO, "SQL: " + sql);
			}
			query = em.createNativeQuery(sql);
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, mappedID);
			query.setParameter(3, user.getOrganization().getId());
			query.setParameter(4, mappedID);
			query.setParameter(5, taxType);
			query.setParameter(6, fromDate);
			query.setParameter(7, user.getOrganization().getId());
			query.setParameter(8, mappedID);
			query.setParameter(9, taxType);
			query.setParameter(10, fromDate);
			query.setParameter(11, toDate);
		}
		List<Object[]> txnLists = query.getResultList();
		Double creditAmt = 0.0;
		Double debitAmt = 0.0;
		Double openBal = 0.0;
		for (Object[] custData : txnLists) {
			if (custData[0] != null)
				openBal += Double.parseDouble(String.valueOf(custData[0]));
			if (custData[1] != null) {
				creditAmt += Double.parseDouble(String.valueOf(custData[1]));
			}
			if (custData[2] != null) {
				debitAmt += Double.parseDouble(String.valueOf(custData[2]));
			}
		}
		// Double openBal = getOpeningBalForTaxes(user, fromDate, em, taxType, 0L, 0.0,
		// particularType);
		tb.setOpeningBalance(openBal);
		tb.setDebit(debitAmt);
		tb.setCredit(creditAmt);
		if (IdosConstants.ASSETS == particularType) {
			tb.setClosingBalance(openBal + debitAmt - creditAmt);
		} else if (IdosConstants.LIABILITIES == particularType) {
			tb.setClosingBalance(openBal + creditAmt - debitAmt);
		}
		tb.setHeadType(IdosConstants.HEAD_TDS);
		tb.setIdentificationForDataValid(String.valueOf(mappedID));
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "****** End " + openBal);
	}

	@Override
	public String exportTrialBalancePDF(String fromDate, String toDate, Users user, Branch branch) {
		return null;
	}

	public Map<String, Object> getParams(final Branch branch, final String fromDate, final String toDate) {
		Map<String, Object> params = new HashMap<String, Object>();
		try {
			if (null == branch || null == branch.getName()) {
				params.put("branchName", "");
			} else {
				params.put("branchName", branch.getName());
			}
			if (null != fromDate && !fromDate.equals("")) {
				params.put("fromDate", IdosConstants.idosdf.parse(fromDate));
			} else {
				params.put("fromDate", IdosConstants.idosdf.parse(
						IdosConstants.idosdf.format((IdosConstants.mysqldf.parse(DateUtil.returnOneMonthBackDate())))));
			}
			if (null != toDate && !toDate.equals("")) {
				params.put("toDate", IdosConstants.idosdf.parse(toDate));
			} else {
				params.put("toDate",
						IdosConstants.idosdf.parse(IdosConstants.idosdf.format(Calendar.getInstance().getTime())));
			}
		} catch (ParseException e) {
			params.put("fromDate", null);
			params.put("toDate", null);
		}
		return params;
	}

	@Override
	public String downloadTrialBalance(ObjectNode result, JsonNode json, Users user, EntityManager em, String path) {
		String fileName = null;
		try {
			ArrayNode an = result.putArray("trialBalanceFileCred");
			String fmDt = json.findValue("fromDate").asText();
			String toDt = json.findValue("toDate").asText();
			Long branchId = json.findValue("trialBalanceForBranch") != null
					? json.findValue("trialBalanceForBranch").asLong()
					: null;
			// Date fDate=mysqldf.parse(mysqldf.format(idosdf.parse(fmDt)));
			// Date tDate=mysqldf.parse(mysqldf.format(idosdf.parse(toDt)));
			String fromDate = null; // mysqldf.format(fDate);
			String toDate = null; // mysqldf.format(tDate);

			if (fmDt == null || toDt.equals("")) {
				List<String> listOfFinYeardate = DateUtil.returnOrgFinancialStartEndDate(user.getOrganization());
				fromDate = listOfFinYeardate.get(0);
			} else {
				fromDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(fmDt));
			}
			if (toDt == null || toDt.equals("")) {
				toDate = IdosConstants.mysqldf.format(Calendar.getInstance().getTime());
			} else {
				toDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(toDt));
			}

			String orgName = user.getOrganization().getName().replaceAll("\\s", "");
			fileName = orgName + "_TrialBalance.xlsx";
			path = path.concat(fileName);

			XSSFWorkbook wb = new XSSFWorkbook();
			CreationHelper createHelper = wb.getCreationHelper();
			CellStyle unlockedCellStyle = wb.createCellStyle();
			unlockedCellStyle.setLocked(false);

			XSSFCellStyle cellStyleHeader = ExcelUtil.getCellStyleHeader(wb);
			XSSFCellStyle cellStyleBorderBoldFont = ExcelUtil.getCsBorderBoldFont(wb);

			Sheet sheets = wb.createSheet(user.getOrganization().getName() + "_TrialBalance");
			Row row = sheets.createRow((short) 0);
			Cell datacells00 = row.createCell(0);
			datacells00.setCellValue(createHelper.createRichTextString(orgName));
			Cell datacells01 = row.createCell(1);
			datacells01.setCellValue(createHelper.createRichTextString("From: " + fromDate));
			Cell datacells02 = row.createCell(2);
			datacells02.setCellValue(createHelper.createRichTextString("To: " + toDate));
			Row row2 = sheets.createRow((short) 1); // add blank line
			Row row1 = sheets.createRow((short) 2);

			Cell datacells10 = row1.createCell(0);
			datacells10.setCellValue(createHelper.createRichTextString("Particulars"));
			datacells10.setCellStyle(cellStyleHeader);
			Cell datacells11 = row1.createCell(1);
			datacells11.setCellValue(createHelper.createRichTextString("Opening Balance"));
			datacells11.setCellStyle(cellStyleHeader);
			Cell datacells12 = row1.createCell(2);
			datacells12.setCellValue(createHelper.createRichTextString("Debit"));
			datacells12.setCellStyle(cellStyleHeader);
			Cell datacells13 = row1.createCell(3);
			datacells13.setCellValue(createHelper.createRichTextString("Credit"));
			datacells13.setCellStyle(cellStyleHeader);
			Cell datacells14 = row1.createCell(4);
			datacells14.setCellValue(createHelper.createRichTextString("Closing Balance"));
			datacells14.setCellStyle(cellStyleHeader);
			Cell datacells15 = row1.createCell(5);
			int incomeRowCountInt = 2;

			int coaTreeDepth = coaDAO.getOrgCOATreeDepth(user, em);

			StringBuilder mainJql = new StringBuilder("select ID, ACCOUNT_CODE, IDENT_DATA_VALID ");
			StringBuilder sqlTmp = new StringBuilder("");
			StringBuilder sqlTemp = new StringBuilder(" from SPECIFICS t0 ");
			StringBuilder sqlNull = new StringBuilder(
					" select t0.ID, t0.ACCOUNT_CODE, t0.IDENT_DATA_VALID, t0.NAME as name0 ");
			for (int i = 0; i < coaTreeDepth; i++) {
				Cell datacellsTmp = row1.createCell(i + 6);
				datacellsTmp.setCellValue(createHelper.createRichTextString("Level " + (coaTreeDepth - i)));
				datacellsTmp.setCellStyle(cellStyleHeader);
				sqlTmp.append(", ").append("t").append(i).append(".NAME as name").append(i);
				sqlTemp.append(" left join SPECIFICS t").append(i + 1).append(" on ").append(" t").append(i + 1)
						.append(".ID = t").append(i).append(".PARENT_SPECIFIC ");
				if (i > 0) {
					sqlNull.append(", ").append(" null as name").append(i);
				}
				mainJql.append(", ").append(" name").append(i);
			}
			// sqlTmp.substring(0, sqlTmp.length()-2);

			/*
			 * Particulars particular=new Particulars();
			 * List<Particulars> catList=particular.list(user.getOrganization());
			 */

			StringBuilder sql = new StringBuilder("select t0.ID, t0.ACCOUNT_CODE, t0.IDENT_DATA_VALID ");
			sql.append(sqlTmp).append(sqlTemp);
			sql.append(" where t0.ORGANIZATION_ID = t1.ORGANIZATION_ID and t0.ORGANIZATION_ID=?1 ");
			sql.append(" union all ");
			sql.append(sqlNull).append(
					" from SPECIFICS t0 where t0.ORGANIZATION_ID=?2 and t0.PRESENT_STATUS = 1 and t0.PARENT_SPECIFIC is null ");

			mainJql.append(" from (").append(sql).append(") tbl order by ACCOUNT_CODE");

			/*
			 * if(coaTreeDepth > 1) {
			 * sql.append(sqlTemp);
			 * sql.
			 * append(" where t0.ORGANIZATION_ID = t1.ORGANIZATION_ID and t0.ORGANIZATION_ID=?x order by t0.ACCOUNT_CODE"
			 * );
			 * }else{
			 * sql.
			 * append(" from SPECIFICS t0 where t0.ORGANIZATION_ID=?x order by t0.ACCOUNT_CODE"
			 * );
			 * }
			 */
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "tree Depth : " + coaTreeDepth);
				log.log(Level.FINE, "HQL : " + mainJql);
			}
			Query query = em.createNativeQuery(mainJql.toString());
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, user.getOrganization().getId());
			List<Object[]> bsValLst = query.getResultList();
			List<String> absoluteChildList = coaDAO.getCoaForOrganizationWithAllHeads(user, em);
			if (log.isLoggable(Level.FINE))
				log.log(Level.FINE, "=#################" + absoluteChildList);
			String accountCodeTmp = "";
			String parentName = null;
			String particularCode = null;
			for (Object[] specific : bsValLst) {
				if (specific != null) {
					BigInteger specificId = (BigInteger) specific[0];
					Long itemId = 0L;
					if (specificId != null) {
						itemId = specificId.longValue();
					}
					BigInteger coaActCode = (BigInteger) specific[1];
					String accountCode = String.valueOf(coaActCode);
					String mappedID = specific[2] == null ? "" : (String) specific[2];
					if ("".equals(mappedID)) {
						mappedID = "0";
					}

					int coaMappedID = Integer.parseInt(mappedID);

					if (accountCode.startsWith("1") && !accountCodeTmp.startsWith("1")) {
						parentName = "Income";
						particularCode = "1000000000000000000";
					} else if (accountCode.startsWith("2") && !accountCodeTmp.startsWith("2")) {
						parentName = "Expense";
						particularCode = "2000000000000000000";
					} else if (accountCode.startsWith("3") && !accountCodeTmp.startsWith("3")) {
						parentName = "Assets";
						particularCode = "3000000000000000000";
					} else if (accountCode.startsWith("4") && !accountCodeTmp.startsWith("4")) {
						parentName = "Liabilities";
						particularCode = "4000000000000000000";
					}

					if (particularCode != null) {
						ObjectNode particular = Json.newObject();
						ObjectNode jsonParam = Json.newObject();
						jsonParam.put("trialBalanceFromDate", fmDt);
						jsonParam.put("trialBalanceToDate", toDt);
						jsonParam.put("identForDataValid", "0");
						jsonParam.put("coaAccountCode", particularCode);
						jsonParam.put("trialBalanceForBranch", branchId);
						displayTrialBalance(particular, jsonParam, user, em);
						JsonNode arrNode = particular.get("coaSpecfChildData");
						Double openingBalance = 0D;
						Double debitAmount = 0D;
						Double creditAmount = 0D;
						Double closingBalance = 0D;
						if (arrNode.isArray()) {
							for (JsonNode valueNode : arrNode) {
								openingBalance += valueNode.findValue("openingBalance") == null ? 0d
										: valueNode.findValue("openingBalance").asDouble();
								debitAmount += valueNode.findValue("debit") == null ? 0d
										: valueNode.findValue("debit").asDouble();
								creditAmount += valueNode.findValue("credit") == null ? 0d
										: valueNode.findValue("credit").asDouble();
								closingBalance += valueNode.findValue("closingBalance") == null ? 0d
										: valueNode.findValue("closingBalance").asDouble();
							}
						}
						Row datarows = sheets.createRow((short) ++incomeRowCountInt);
						Cell datacells0 = datarows.createCell(0);
						Cell datacells1 = datarows.createCell(1);
						datacells1.setCellValue(openingBalance);
						datacells1.setCellStyle(cellStyleBorderBoldFont);
						Cell datacells2 = datarows.createCell(2);
						datacells2.setCellValue(debitAmount);
						datacells2.setCellStyle(cellStyleBorderBoldFont);
						Cell datacells3 = datarows.createCell(3);
						datacells3.setCellValue(creditAmount);
						datacells3.setCellStyle(cellStyleBorderBoldFont);
						Cell datacells4 = datarows.createCell(4);
						datacells4.setCellValue(closingBalance);
						datacells4.setCellStyle(cellStyleBorderBoldFont);
						Cell datacells5 = datarows.createCell(5);

						for (int i = 0; i < coaTreeDepth; i++) {
							Cell datacellsTmp = datarows.createCell(i + 6);
						}
						Cell datacellsLast = datarows.createCell(coaTreeDepth + 6);
						datacellsLast.setCellValue(createHelper.createRichTextString(parentName));

					}

					List<TrialBalance> trialBalanceList = getTBForSpecific(itemId, accountCode, coaMappedID, user,
							fromDate, toDate, branchId, em);
					for (TrialBalance tb : trialBalanceList) {
						Row datarows = sheets.createRow((short) ++incomeRowCountInt);
						Double openingBalance = null;
						Double debit = null;
						Double credit = null;
						Double closingBalance = null;
						openingBalance = tb.getOpeningBalance() == null ? 0d : tb.getOpeningBalance();
						debit = tb.getDebit() == null ? 0d : tb.getDebit();
						credit = tb.getCredit() == null ? 0d : tb.getCredit();
						closingBalance = tb.getClosingBalance() == null ? 0d : tb.getClosingBalance();

						Cell datacells0 = datarows.createCell(0);
						if (parentName.equalsIgnoreCase("Income") || parentName.equalsIgnoreCase("Expense")) {
							Cell datacells1 = datarows.createCell(1);
							datacells1.setCellValue(0.0);
							Cell datacells4 = datarows.createCell(4);
							datacells4.setCellValue(0.0);
						} else {
							Cell datacells1 = datarows.createCell(1);
							datacells1.setCellValue(openingBalance);
							Cell datacells4 = datarows.createCell(4);
							datacells4.setCellValue(closingBalance);
						}
						Cell datacells2 = datarows.createCell(2);
						datacells2.setCellValue(debit);
						Cell datacells3 = datarows.createCell(3);
						datacells3.setCellValue(credit);
						Cell datacells5 = datarows.createCell(5);
						if (log.isLoggable(Level.FINE))
							log.log(Level.FINE, "=################################### " + tb.getSpecId());
						String interBranchHeadid = null;
						if (IdosConstants.HEAD_INTR_BRANCH.equals(tb.getHeadType())
								|| IdosConstants.HEAD_INTR_BRANCH_IN.equals(tb.getHeadType())
								|| IdosConstants.HEAD_INTR_BRANCH_OUT.equals(tb.getHeadType())) {
							interBranchHeadid = IdosConstants.HEAD_INTR_BRANCH + tb.getSpecId() + "-" + tb.getHeadid2();
							if (log.isLoggable(Level.FINE))
								log.log(Level.FINE, "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ " + interBranchHeadid);
						}

						if (absoluteChildList.contains(IdosConstants.HEAD_SPECIFIC + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_CASH + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_BANK + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_PETTY + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_VENDOR + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_VENDOR_ADV + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_CUSTOMER + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_CUSTOMER_ADV + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_USER + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_TAXS + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_SGST + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_CGST + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_IGST + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_CESS + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_RCM_SGST_IN + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_RCM_CGST_IN + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_RCM_IGST_IN + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_RCM_CESS_IN + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_RCM_SGST_OUTPUT + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_RCM_CGST_OUTPUT + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_RCM_IGST_OUTPUT + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_RCM_CESS_OUTPUT + tb.getSpecId())
								|| absoluteChildList.contains(interBranchHeadid)
								|| absoluteChildList.contains(IdosConstants.HEAD_TDS_INPUT + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_TDS_192 + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_TDS_194A + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_TDS_194C1 + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_TDS_194C2 + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_TDS_194H + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_TDS_194I1 + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_TDS_194I2 + tb.getSpecId())
								|| absoluteChildList.contains(IdosConstants.HEAD_TDS_194J + tb.getSpecId())) {
							datacells0.setCellValue(createHelper.createRichTextString(tb.getAccountName()));
						}
						for (int i = 0; i < coaTreeDepth; i++) {
							Cell datacellsTmp = datarows.createCell(i + 6);
						}
						Cell datacellsLast = datarows.createCell(coaTreeDepth + 6);
						datacellsLast.setCellValue(createHelper.createRichTextString(parentName));

						int coaTreeDepthTmp = coaTreeDepth + 5;
						for (int i = coaTreeDepth + 1; i > 1; i--) {
							String nameTmp = (String) specific[i];
							if (nameTmp == null && "".equals("")) {
								continue;
							}
							// String nameTmp = specific[i+2] == null ? "" : (String)specific[i+2];
							// log.log(Level.FINE, "############################# i=" + i + " i+2=" + (i+2)
							// + " name = " + nameTmp);

							Cell datacellsTmp = datarows.getCell(coaTreeDepthTmp--);
							datacellsTmp.setCellValue(createHelper.createRichTextString(nameTmp));
						}
					}
					accountCodeTmp = accountCode;
					particularCode = null;
				}
			}
			for (int i = 0; i < coaTreeDepth + 6; i++) {
				sheets.autoSizeColumn(i);
			}
			FileOutputStream fileOut = new FileOutputStream(path);
			// FileOutputStream fileOut1 = new FileOutputStream(path1);
			wb.write(fileOut);
			// wb = new XSSFWorkbook(new FileInputStream(path));
			// wb.write(fileOut1);
			fileOut.close();
			// fileOut1.close();
			/*
			 * ObjectNode datarow = Json.newObject();
			 * datarow.put("fileName", fileName);
			 * an.add(datarow);
			 */
		} catch (Exception ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
		}
		return fileName;
	}

	/**
	 * will used in download
	 * 
	 * @param coaActCode
	 * @param coaIdentForDataValid
	 * @param user
	 * @param fromDate
	 * @param toDate
	 * @param em
	 * @return
	 * @throws IDOSException
	 */
	private List<TrialBalance> getTBForSpecific(Long itemId, String coaActCode, int coaIdentForDataValid, Users user,
			String fromDate, String toDate, Long branchId, EntityManager em) throws IDOSException {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE,
					"============ Start coaActCode: " + coaActCode + ", coaIdentForDataValid: " + coaIdentForDataValid);
		}
		List<TrialBalance> trialBalanceList = new ArrayList<TrialBalance>();

		Long accountCode = Long.parseLong(coaActCode);
		if (coaActCode == null || accountCode == null) {
			throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF, IdosConstants.BUSINESS_EXCEPTION,
					IdosConstants.NULL_KEY_EXC_ESMF_MSG, "null code account code.");
		}
		Specifics itemSpecific = Specifics.findById(itemId);
		if (itemSpecific == null) {
			throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF, IdosConstants.BUSINESS_EXCEPTION,
					IdosConstants.NULL_KEY_EXC_ESMF_MSG, "null Specific.");
		}
		if (itemSpecific == null) {
			return trialBalanceList;
		}

		if (coaIdentForDataValid == 4) { // Bank Account then give tb branchwise,
			getTrialBalanceBranchBank(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em);
		} else if (coaIdentForDataValid == 3) { // Cash Account then give tb branchwise
			getTrialBalanceBranchCash(trialBalanceList, user, fromDate, toDate, IdosConstants.CASH, branchId, em);
		} else if (coaIdentForDataValid == 30) { // petty Cash Account then give tb branchwise
			getTrialBalanceBranchCash(trialBalanceList, user, fromDate, toDate, IdosConstants.PETTY_CASH, branchId, em);
		} else if (coaIdentForDataValid == 7) { // Adv paid to vendors
			getTrialBalanceCustomerVendorAdvance(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.VENDOR);
		} else if (coaIdentForDataValid == 1) { // Acct receivables(credit sales to customers),
			getTrialBalanceCustomerVendorCreditSales(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId,
					em, IdosConstants.CUSTOMER);
		} else if (coaIdentForDataValid == 8) { // withholding tax payment from customers
			// Sunil getTrialBalanceWithholdingTaxOnPaymentFromCustomers(coaActCode,
			// trialBalanceList, user,fromDate,toDate, em);
			// getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate,
			// toDate, em, IdosConstants.INPUT_TDS, IdosConstants.ASSETS);
			getTrialBalanceForTds(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.INPUT_TDS, IdosConstants.ASSETS, 8);
		} else if (coaIdentForDataValid == 14) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.INPUT_TAX, IdosConstants.ASSETS);
		} else if (coaIdentForDataValid == 39) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.INPUT_SGST, IdosConstants.ASSETS);
		} else if (coaIdentForDataValid == 40) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.INPUT_CGST, IdosConstants.ASSETS);
		} else if (coaIdentForDataValid == 41) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.INPUT_IGST, IdosConstants.ASSETS);
		} else if (coaIdentForDataValid == 42) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.INPUT_CESS, IdosConstants.ASSETS);
		} else if (coaIdentForDataValid == 53) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_SGST_IN, IdosConstants.ASSETS);
		} else if (coaIdentForDataValid == 54) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_CGST_IN, IdosConstants.ASSETS);
		} else if (coaIdentForDataValid == 55) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_IGST_IN, IdosConstants.ASSETS);
		} else if (coaIdentForDataValid == 56) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_CESS_IN, IdosConstants.ASSETS);
		} else if (coaIdentForDataValid == 2) { // Acct payables(purchase on credit from vendors)
			getTrialBalanceCustomerVendorCreditSales(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId,
					em, IdosConstants.VENDOR);
		} else if (coaIdentForDataValid == 6) { // Adv received from customers
			getTrialBalanceCustomerVendorAdvance(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.CUSTOMER);
		} else if (coaIdentForDataValid == 15) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.OUTPUT_TAX, IdosConstants.LIABILITIES);
		} else if (coaIdentForDataValid == 43) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.OUTPUT_SGST, IdosConstants.LIABILITIES);
		} else if (coaIdentForDataValid == 44) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.OUTPUT_CGST, IdosConstants.LIABILITIES);
		} else if (coaIdentForDataValid == 45) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.OUTPUT_IGST, IdosConstants.LIABILITIES);
		} else if (coaIdentForDataValid == 46) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.OUTPUT_CESS, IdosConstants.LIABILITIES);
		} else if (coaIdentForDataValid == 47) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_SGST_OUTPUT, IdosConstants.LIABILITIES);
		} else if (coaIdentForDataValid == 48) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_CGST_OUTPUT, IdosConstants.LIABILITIES);
		} else if (coaIdentForDataValid == 49) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_IGST_OUTPUT, IdosConstants.LIABILITIES);
		} else if (coaIdentForDataValid == 50) {
			getTrialBalanceBranchForTaxes(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					IdosConstants.RCM_CESS_OUTPUT, IdosConstants.LIABILITIES);
		} else if (coaIdentForDataValid >= 31 && coaIdentForDataValid <= 38) {
			getTrialBalanceForTds(itemSpecific, trialBalanceList, user, fromDate, toDate, branchId, em,
					(coaIdentForDataValid + 9), IdosConstants.LIABILITIES, coaIdentForDataValid);
		} else if (coaIdentForDataValid == 57) {
			INTER_BRANCH_TRANSFER_DAO.getTrialBalanceInterBranch(trialBalanceList, user, fromDate, toDate, branchId,
					em);
		} else if (coaIdentForDataValid == 60) {
			getUserAdvanceForExpenseAndTravel(coaActCode, trialBalanceList, user, fromDate, toDate, branchId, em);
		} else if (coaIdentForDataValid == 61) {
			getEmployeeClaim(coaActCode, trialBalanceList, user, fromDate, toDate, branchId, em);
		} else {
			String acctCodeHierarchy = itemSpecific.getAccountCodeHirarchy();
			if (acctCodeHierarchy.startsWith("/1000000000000000000/")) {
				TrialBalance tb = new TrialBalance();
				String description = itemSpecific.getName();
				if (itemSpecific.getInvoiceItemDescription1() != null) {
					description += " " + itemSpecific.getInvoiceItemDescription1();
				}
				if (itemSpecific.getInvoiceItemDescription2() != null) {
					description += " " + itemSpecific.getInvoiceItemDescription2();
				}
				tb.setAccountName(description);
				tb.setSpecId(itemSpecific.getId());
				tb.setSpecfaccountCode(itemSpecific.getAccountCode().toString());
				tb.setTopLevelAccountCode(itemSpecific.getParticularsId().getAccountCode().toString());
				tb.setIdentificationForDataValid(itemSpecific.getIdentificationForDataValid());
				/*
				 * if ("51".equals(itemSpecific.getIdentificationForDataValid())) { //Rounding
				 * off
				 * getTrialBalanceRoundingOffForSellTranTotal(tb, user, fromDate, toDate,
				 * itemSpecific, branchId, em, true);
				 * } else {
				 */
				getTrialBalanceAmountForCOAItem(tb, user, itemSpecific, fromDate, toDate, branchId, em,
						IdosConstants.INCOME);
				trialBalanceList.add(tb);
			} else if (acctCodeHierarchy.startsWith("/2000000000000000000/")) {
				TrialBalance tb = new TrialBalance();
				tb.setAccountName(itemSpecific.getName());
				tb.setSpecId(itemSpecific.getId());
				tb.setSpecfaccountCode(itemSpecific.getAccountCode().toString());
				tb.setTopLevelAccountCode(itemSpecific.getParticularsId().getAccountCode().toString());
				tb.setIdentificationForDataValid(itemSpecific.getIdentificationForDataValid());
				getTrialBalanceAmountForCOAItem(tb, user, itemSpecific, fromDate, toDate, branchId, em,
						IdosConstants.EXPENSE);
				trialBalanceList.add(tb);
			} else if (acctCodeHierarchy.startsWith("/3000000000000000000/")) {
				TrialBalance tb = new TrialBalance();
				tb.setAccountName(itemSpecific.getName());
				tb.setSpecId(itemSpecific.getId());
				tb.setSpecfaccountCode(itemSpecific.getAccountCode().toString());
				tb.setTopLevelAccountCode(itemSpecific.getParticularsId().getAccountCode().toString());
				tb.setIdentificationForDataValid(itemSpecific.getIdentificationForDataValid());
				// tb.setOpeningBalance(specf.getOpeningBalance());
				// tb.setClosingBalance(specf.getOpeningBalance());
				getTrialBalanceAmountForAssets(tb, user, itemSpecific, fromDate, toDate, branchId, em);
				trialBalanceList.add(tb);
			} else if (acctCodeHierarchy.startsWith("/4000000000000000000/")) {
				TrialBalance tb = new TrialBalance();
				tb.setAccountName(itemSpecific.getName());
				tb.setSpecId(itemSpecific.getId());
				tb.setSpecfaccountCode(itemSpecific.getAccountCode().toString());
				tb.setTopLevelAccountCode(itemSpecific.getParticularsId().getAccountCode().toString());
				tb.setIdentificationForDataValid(itemSpecific.getIdentificationForDataValid());
				getTrialBalanceAmountForLiabilities(tb, user, itemSpecific, fromDate, toDate, branchId, em);
				trialBalanceList.add(tb);
			}
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "============ End");
		}
		return trialBalanceList;
	}

	private void getTrialBalancePayrollExpenses(List<TrialBalance> trialBalanceList, Users user, String fromDate,
			String toDate, Long branchId, EntityManager em) {
		// date = 2018-06-11

		String str[] = fromDate.split("-");
		int fromyear = Integer.parseInt(str[0]);
		int frommonth = Integer.parseInt(str[1]);

		String str1[] = toDate.split("-");
		int toyear = Integer.parseInt(str1[0]);
		int tomonth = Integer.parseInt(str1[1]);
		Map criterias = new HashMap();
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("payrollType", IdosConstants.PAYROLL_TYPE_EARNINGS); // Earning/Deductions list
		// criterias.put("inForce", 1);
		criterias.put("presentStatus", 1);
		List<PayrollSetup> payrollSetupList = genericDao.findByCriteria(PayrollSetup.class, criterias, em);
		if (payrollSetupList != null && payrollSetupList.size() > 0) {
			for (int i = 0; i < payrollSetupList.size(); i++) {
				PayrollSetup payrollItem = payrollSetupList.get(i);
				ArrayList inparams = new ArrayList();
				String dataQuery = "";
				dataQuery = "select SUM(debitAmount) from TrialBalancePayrollItem where payrollItem.id = ?1 and organization.id = ?2 and presentStatus=1 and (date BETWEEN '"
						+ fromDate + "' AND '" + toDate + "')";
				inparams.add(payrollItem.getId());
				inparams.add(user.getOrganization().getId());
				if (branchId != null && branchId > 0L) {
					inparams.add(branchId);
					dataQuery = "select SUM(debitAmount) from TrialBalancePayrollItem where payrollItem.id = ?1 and organization.id = ?2 and presentStatus=1 and (date BETWEEN '"
							+ fromDate + "' AND '" + toDate + "') and branch.id = ?3";
				}
				List<TrialBalancePayrollItem> sumAmountTB = genericDao.queryWithParams(dataQuery, em, inparams);
				Object sum = sumAmountTB.get(0);
				inparams.clear();
				dataQuery = "select SUM(debitAmount) from TrialBalancePayrollItem where payrollItem.id = ?1 and organization.id = ?2 and presentStatus=1 and date < '"
						+ fromDate + "'";
				inparams.add(payrollItem.getId());
				inparams.add(user.getOrganization().getId());
				if (branchId != null && branchId > 0L) {
					inparams.add(branchId);
					dataQuery = "select SUM(debitAmount) from TrialBalancePayrollItem where payrollItem.id = ?1 and organization.id = ?2 and presentStatus=1 and date < '"
							+ fromDate + "' and branch.id = ?3";
				}
				List<TrialBalancePayrollItem> openingBal = genericDao.queryWithParams(dataQuery, em, inparams);
				Object openingBalOB = openingBal.get(0);
				TrialBalance tb = new TrialBalance();
				tb.setAccountName(payrollItem.getPayrollHeadName());
				tb.setSpecId(payrollItem.getId());
				tb.setSpecfaccountCode("1");
				tb.setTopLevelAccountCode("2000000000000000000");
				if (openingBalOB != null) {
					tb.setOpeningBalance(Double.parseDouble(openingBalOB.toString()));
					if (sum != null)
						tb.setClosingBalance(
								Double.parseDouble(openingBalOB.toString()) + Double.parseDouble(sum.toString()) - 0.0);
					else
						tb.setClosingBalance(Double.parseDouble(openingBalOB.toString()) - 0.0);
				} else {
					tb.setOpeningBalance(0.0);
					if (sum != null)
						tb.setClosingBalance(Double.parseDouble(sum.toString()) - 0.0);
					else
						tb.setClosingBalance(0.0);
				}

				if (sum != null)
					tb.setDebit(Double.parseDouble(sum.toString()));
				else
					tb.setDebit(0.0);
				tb.setCredit(0.0);
				// tb.setClosingBalance(derivedOpeningBal + income - 0.0);
				tb.setHeadType(IdosConstants.HEAD_PAYROLL_EXPENSE);
				trialBalanceList.add(tb);
			}
		}
	}

	private void getTrialBalancePayrollDeductions(List<TrialBalance> trialBalanceList, Users user, String fromDate,
			String toDate, Long branchId, EntityManager em) {
		// date = 2018-06-11
		String str[] = fromDate.split("-");
		int fromyear = Integer.parseInt(str[0]);
		int frommonth = Integer.parseInt(str[1]);

		String str1[] = toDate.split("-");
		int toyear = Integer.parseInt(str1[0]);
		int tomonth = Integer.parseInt(str1[1]);
		Map criterias = new HashMap();
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("payrollType", IdosConstants.PAYROLL_TYPE_DEDUCTIONS); // Earning/Deductions list
		// criterias.put("inForce", 1);
		criterias.put("presentStatus", 1);
		List<PayrollSetup> payrollSetupList = genericDao.findByCriteria(PayrollSetup.class, criterias, em);
		if (payrollSetupList != null && payrollSetupList.size() > 0) {
			for (int i = 0; i < payrollSetupList.size(); i++) {
				PayrollSetup payrollItem = payrollSetupList.get(i);
				ArrayList inparams = new ArrayList();
				String dataQuery = "";
				dataQuery = "select SUM(creditAmount) from TrialBalancePayrollItem where payrollItem.id = ?1 and organization.id = ?2 and presentStatus=1 and (date BETWEEN '"
						+ fromDate + "' AND '" + toDate + "')";
				inparams.add(payrollItem.getId());
				inparams.add(user.getOrganization().getId());
				if (branchId != null && branchId > 0L) {
					inparams.add(branchId);
					dataQuery = "select SUM(creditAmount) from TrialBalancePayrollItem where payrollItem.id = ?1 and organization.id = ?2 and presentStatus=1 and (date BETWEEN '"
							+ fromDate + "' AND '" + toDate + "') and branch.id = ?3";
				}
				List<TrialBalancePayrollItem> sumAmountTB = genericDao.queryWithParams(dataQuery, em, inparams);
				Object sum = sumAmountTB.get(0);
				inparams.clear();
				dataQuery = "select SUM(creditAmount) from TrialBalancePayrollItem where payrollItem.id = ?1 and organization.id = ?2 and presentStatus=1 and date < '"
						+ fromDate + "'";
				inparams.add(payrollItem.getId());
				inparams.add(user.getOrganization().getId());
				if (branchId != null && branchId > 0L) {
					inparams.add(branchId);
					dataQuery = "select SUM(creditAmount) from TrialBalancePayrollItem where payrollItem.id = ?1 and organization.id = ?2 and presentStatus=1 and date < '"
							+ fromDate + "' and branch.id = ?3";
				}
				List<TrialBalancePayrollItem> openingBal = genericDao.queryWithParams(dataQuery, em, inparams);
				Object openingBalOB = openingBal.get(0);
				TrialBalance tb = new TrialBalance();
				tb.setAccountName(payrollItem.getPayrollHeadName());
				tb.setSpecId(payrollItem.getId());
				tb.setSpecfaccountCode("1");
				tb.setTopLevelAccountCode("4000000000000000000");
				// tb.setOpeningBalance(derivedOpeningBal);
				if (openingBalOB != null) {
					tb.setOpeningBalance(Double.parseDouble(openingBalOB.toString()));
					if (sum != null)
						tb.setClosingBalance(
								Double.parseDouble(openingBalOB.toString()) + Double.parseDouble(sum.toString()) - 0.0);
					else
						tb.setClosingBalance(Double.parseDouble(openingBalOB.toString()) - 0.0);
				} else {
					tb.setOpeningBalance(0.0);
					if (sum != null)
						tb.setClosingBalance(Double.parseDouble(sum.toString()) - 0.0);
					else
						tb.setClosingBalance(0.0);
				}
				tb.setDebit(0.0);
				// tb.setCredit(income);
				if (sum != null)
					tb.setCredit(Double.parseDouble(sum.toString()));
				else
					tb.setCredit(0.0);
				// tb.setClosingBalance(derivedOpeningBal + income - 0.0);
				tb.setHeadType(IdosConstants.HEAD_PAYROLL_DEDUCTIONS);
				trialBalanceList.add(tb);
			}
		}
	}

	public void getTrialBalancePayrollExpensesTotal(TrialBalance tb, Users user, String fromDate, String toDate,
			Long branchId, EntityManager em) {
		// OB query
		// date = 2018-06-11
		String str[] = fromDate.split("-");
		int fromyear = Integer.parseInt(str[0]);
		int frommonth = Integer.parseInt(str[1]);

		String str1[] = toDate.split("-");
		int toyear = Integer.parseInt(str1[0]);
		int tomonth = Integer.parseInt(str1[1]);

		String payrollTotal = "";
		payrollTotal = "select SUM(debitAmount) from TrialBalancePayrollItem where organization.id =?1 and presentStatus=1 and date BETWEEN '"
				+ fromDate + "' and '" + toDate + "' ";
		ArrayList inparams = new ArrayList();
		inparams.add(user.getOrganization().getId());
		if (branchId != null && branchId > 0L) {
			inparams.add(branchId);
			payrollTotal = "select SUM(debitAmount) from TrialBalancePayrollItem where organization.id =?1 and date BETWEEN '"
					+ fromDate + "' and '" + toDate + "' and branch.id = ?2";
		}
		List<TrialBalancePayrollItem> sumAmountTB = genericDao.queryWithParams(payrollTotal, em, inparams);
		String payrollTotalOB = "";
		payrollTotalOB = "select SUM(debitAmount) from TrialBalancePayrollItem where organization.id =?1 and presentStatus=1 and date < '"
				+ fromDate + "'";
		inparams.clear();
		inparams.add(user.getOrganization().getId());
		if (branchId != null && branchId > 0L) {
			inparams.add(branchId);
			payrollTotalOB = "select SUM(debitAmount) from TrialBalancePayrollItem where organization.id =?1 and presentStatus=1 and date < '"
					+ fromDate + "' and branch.id = ?";
		}
		List<TrialBalancePayrollItem> sumAmountOB = genericDao.queryWithParams(payrollTotalOB, em, inparams);
		Object sumAmountTBO = sumAmountTB.get(0);
		Object sumAmountOBO = sumAmountOB.get(0);
		if (sumAmountTB != null && sumAmountTB.size() > 0) {
			Double debitAmt = 0.0;
			if (sumAmountTBO != null) {
				debitAmt = Double.parseDouble(sumAmountTBO.toString());
			}
			Double derivedOpeningBal = 0.0;
			if (sumAmountOB != null && sumAmountOB.size() > 0 && !sumAmountOB.isEmpty()) {
				if (sumAmountOBO != null) {
					derivedOpeningBal = Double.parseDouble(sumAmountOBO.toString());
				}
			}
			tb.setOpeningBalance(derivedOpeningBal);
			tb.setDebit(debitAmt);
			tb.setCredit(0.0);
			tb.setClosingBalance(derivedOpeningBal + debitAmt - 0.0);
			tb.setHeadType(IdosConstants.HEAD_PAYROLL_EXPENSE);
		}
	}

	public void getTrialBalancePayrollDeductionsTotal(TrialBalance tb, Users user, String fromDate, String toDate,
			Long branchId, EntityManager em) {
		// OB query
		String str[] = fromDate.split("-");
		int fromyear = Integer.parseInt(str[0]);
		int frommonth = Integer.parseInt(str[1]);

		String str1[] = toDate.split("-");
		int toyear = Integer.parseInt(str1[0]);
		int tomonth = Integer.parseInt(str1[1]);

		String payrollTotal = "";
		payrollTotal = "select SUM(creditAmount) from TrialBalancePayrollItem where organization.id =?1 and presentStatus=1 and date BETWEEN '"
				+ fromDate + "' and '" + toDate + "' ";
		ArrayList inparams = new ArrayList();
		inparams.add(user.getOrganization().getId());
		if (branchId != null && branchId > 0L) {
			inparams.add(branchId);
			payrollTotal = "select SUM(creditAmount) from TrialBalancePayrollItem where organization.id =?1 and presentStatus=1 and date BETWEEN '"
					+ fromDate + "' and '" + toDate + "' and branch.id = ?2";
		}
		List<TrialBalancePayrollItem> sumAmountTB = genericDao.queryWithParams(payrollTotal, em, inparams);
		String payrollTotalOB = "";
		payrollTotalOB = "select SUM(creditAmount) from TrialBalancePayrollItem where organization.id =?1 and presentStatus=1 and date < '"
				+ fromDate + "'";
		inparams.clear();
		inparams.add(user.getOrganization().getId());
		if (branchId != null && branchId > 0L) {
			inparams.add(branchId);
			payrollTotalOB = "select SUM(creditAmount) from TrialBalancePayrollItem where organization.id =?1 and presentStatus=1 and date < '"
					+ fromDate + "' and branch.id = ?2";
		}
		List<TrialBalancePayrollItem> sumAmountOB = genericDao.queryWithParams(payrollTotalOB, em, inparams);
		Object sumAmountTBO = sumAmountTB.get(0);
		Object sumAmountOBO = sumAmountOB.get(0);

		if (sumAmountTB != null && sumAmountTB.size() > 0) {
			Double creditAmt = 0.0;
			if (sumAmountTBO != null)
				creditAmt = Double.parseDouble(sumAmountTBO.toString());

			Double derivedOpeningBal = 0.0;
			if (sumAmountOB != null && sumAmountOB.size() > 0) {
				if (sumAmountOBO != null)
					derivedOpeningBal = Double.parseDouble(sumAmountOBO.toString());
			}
			tb.setOpeningBalance(derivedOpeningBal);
			tb.setDebit(0.0);
			tb.setCredit(creditAmt);
			tb.setClosingBalance(derivedOpeningBal + creditAmt - 0.0);
		}
	}

	private void getUserAdvanceForExpenseAndTravelTotal(TrialBalance tb, Users user, String fromDate, String toDate,
			Long branchId, EntityManager em, Specifics specifics) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "*****Start ");
		try {
			String jpql = null;
			ArrayList inparams = new ArrayList(9);
			if (branchId != null && branchId > 0L) {
				jpql = USER_ADV_EXP_TRAV_BRNCH_TOTAL_JPQL;
				inparams.add(user.getOrganization().getId());
				inparams.add(CoaMappingConstants.EMPLOYEE_ADVANCES_PAID);
				inparams.add(user.getOrganization().getId());
				inparams.add(branchId);
				inparams.add(IdosConstants.MYSQLDF.parse(fromDate));
				inparams.add(user.getOrganization().getId());
				inparams.add(branchId);
				inparams.add(IdosConstants.MYSQLDF.parse(fromDate));
				inparams.add(IdosConstants.MYSQLDF.parse(toDate));
			} else {
				jpql = USER_ADV_EXP_TRAV_ORG_TOTAL_JPQL;
				inparams.add(user.getOrganization().getId());
				inparams.add(CoaMappingConstants.EMPLOYEE_ADVANCES_PAID);
				inparams.add(user.getOrganization().getId());
				inparams.add(IdosConstants.MYSQLDF.parse(fromDate));
				inparams.add(user.getOrganization().getId());
				inparams.add(IdosConstants.MYSQLDF.parse(fromDate));
				inparams.add(IdosConstants.MYSQLDF.parse(toDate));
			}
			List<Object[]> txnLists = genericDao.executeNativeQueryWithParamName(jpql, em, inparams);
			Double creditAmt = 0.0;
			Double debitAmt = 0.0;
			Double openingBal = 0.0;
			for (Object[] custData : txnLists) {
				if (custData[0] != null) {
					openingBal += Double.parseDouble(String.valueOf(custData[0]));
				}
				if (custData[1] != null) {
					creditAmt += Double.parseDouble(String.valueOf(custData[1]));
				}
				if (custData[2] != null) {
					debitAmt += Double.parseDouble(String.valueOf(custData[2]));
				}
			}
			tb.setOpeningBalance(openingBal);
			tb.setDebit(debitAmt);
			tb.setCredit(creditAmt);
			tb.setClosingBalance(openingBal + debitAmt - creditAmt);
			tb.setHeadType(IdosConstants.HEAD_SPECIFIC);
		} catch (ParseException e) {
			log.log(Level.SEVERE, user.getEmail(), e);
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "*****End ");
	}

	private void getUserAdvanceForExpenseAndTravel(String coaActCode, List<TrialBalance> trialBalanceList, Users user,
			String fromDate, String toDate, Long branchId, EntityManager em) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "*****Start ");
		String jpql = null;
		try {
			ArrayList inparams = new ArrayList(6);
			if (branchId != null && branchId > 0L) {
				jpql = USER_ADV_EXP_TRAV_BRNH_JPQL;
				inparams.add(user.getOrganization().getId());
				inparams.add(branchId);
				inparams.add(user.getOrganization().getId());
				inparams.add(branchId);
				inparams.add(IdosConstants.MYSQLDF.parse(fromDate));
				inparams.add(user.getOrganization().getId());
				inparams.add(branchId);
				inparams.add(IdosConstants.MYSQLDF.parse(fromDate));
				inparams.add(IdosConstants.MYSQLDF.parse(toDate));
			} else {
				jpql = USER_ADV_EXP_TRAV_ORG_JPQL;
				inparams.add(user.getOrganization().getId());
				inparams.add(user.getOrganization().getId());
				inparams.add(IdosConstants.MYSQLDF.parse(fromDate));
				inparams.add(user.getOrganization().getId());
				inparams.add(IdosConstants.MYSQLDF.parse(fromDate));
				inparams.add(IdosConstants.MYSQLDF.parse(toDate));
			}
			List<Object[]> txnLists = genericDao.executeNativeQueryWithParamName(jpql, em, inparams);
			Double creditAmt = 0.0;
			Double debitAmt = 0.0;
			String userName;
			Long userID = 0l;
			Double openingBalance = 0.0;

			for (Object[] usertraveladvData : txnLists) {
				openingBalance = (Double) usertraveladvData[0];
				openingBalance = openingBalance == null ? 0.0 : openingBalance;
				creditAmt = usertraveladvData[1] == null ? 0.0
						: Double.parseDouble(String.valueOf(usertraveladvData[1]));
				debitAmt = usertraveladvData[2] == null ? 0.0
						: Double.parseDouble(String.valueOf(usertraveladvData[2]));
				userName = usertraveladvData[3] == null ? "" : usertraveladvData[3].toString();
				userID = Long.parseLong(usertraveladvData[4].toString());
				TrialBalance tb = new TrialBalance();
				tb.setAccountName(userName + "-Advance");
				tb.setSpecId(userID);
				tb.setSpecfaccountCode("1");
				tb.setTopLevelAccountCode("3000000000000000000");
				tb.setOpeningBalance(openingBalance);
				tb.setDebit(debitAmt);
				tb.setCredit(creditAmt);
				tb.setClosingBalance(openingBalance + debitAmt - creditAmt);
				tb.setHeadType(IdosConstants.HEAD_EXP_ADV);
				trialBalanceList.add(tb);
			}
		} catch (ParseException e) {
			log.log(Level.SEVERE, user.getEmail(), e);
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "*****End ");
	}

	private void getEmployeeClaimTotal(TrialBalance tb, Users user, String fromDate, String toDate, Long branchId,
			EntityManager em, Specifics specifics) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "*****Start ");
		try {
			String jpql = null;
			ArrayList inparams = new ArrayList(9);
			if (branchId != null && branchId > 0L) {
				jpql = EMP_CLAIM_BRNCH_TOTAL_JPQL;
				inparams.add(user.getOrganization().getId());
				inparams.add(CoaMappingConstants.EMPLOYEE_CLAIMS_PAYABLE);
				inparams.add(user.getOrganization().getId());
				inparams.add(branchId);
				inparams.add(IdosConstants.MYSQLDF.parse(fromDate));
				inparams.add(user.getOrganization().getId());
				inparams.add(branchId);
				inparams.add(IdosConstants.MYSQLDF.parse(fromDate));
				inparams.add(IdosConstants.MYSQLDF.parse(toDate));
			} else {
				jpql = EMP_CLAIM_ORG_TOTAL_JPQL;
				inparams.add(user.getOrganization().getId());
				inparams.add(CoaMappingConstants.EMPLOYEE_CLAIMS_PAYABLE);
				inparams.add(user.getOrganization().getId());
				inparams.add(IdosConstants.MYSQLDF.parse(fromDate));
				inparams.add(user.getOrganization().getId());
				inparams.add(IdosConstants.MYSQLDF.parse(fromDate));
				inparams.add(IdosConstants.MYSQLDF.parse(toDate));
			}
			List<Object[]> txnLists = genericDao.executeNativeQueryWithParamName(jpql, em, inparams);
			Double creditAmt = 0.0;
			Double debitAmt = 0.0;
			Double openingBal = 0.0;
			for (Object[] custData : txnLists) {
				if (custData[0] != null) {
					openingBal += Double.parseDouble(String.valueOf(custData[0]));
				}
				if (custData[1] != null) {
					creditAmt += Double.parseDouble(String.valueOf(custData[1]));
				}
				if (custData[2] != null) {
					debitAmt += Double.parseDouble(String.valueOf(custData[2]));
				}
			}
			tb.setOpeningBalance(openingBal);
			tb.setDebit(debitAmt);
			tb.setCredit(creditAmt);
			tb.setClosingBalance(openingBal + debitAmt - creditAmt);
			tb.setHeadType(IdosConstants.HEAD_SPECIFIC);
		} catch (ParseException e) {
			log.log(Level.SEVERE, user.getEmail(), e);
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "*****End ");
	}

	private void getEmployeeClaim(String coaActCode, List<TrialBalance> trialBalanceList, Users user, String fromDate,
			String toDate, Long branchId, EntityManager em) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "*****Start ");
		String jpql = null;
		try {
			ArrayList inparams = new ArrayList(6);
			if (branchId != null && branchId > 0L) {
				jpql = EMP_CLAIM_BRNH_JPQL;
				inparams.add(user.getOrganization().getId());
				inparams.add(branchId);
				inparams.add(user.getOrganization().getId());
				inparams.add(branchId);
				inparams.add(IdosConstants.MYSQLDF.parse(fromDate));
				inparams.add(user.getOrganization().getId());
				inparams.add(branchId);
				inparams.add(IdosConstants.MYSQLDF.parse(fromDate));
				inparams.add(IdosConstants.MYSQLDF.parse(toDate));
			} else {
				jpql = EMP_CLAIM_ORG_JPQL;
				inparams.add(user.getOrganization().getId());
				inparams.add(user.getOrganization().getId());
				inparams.add(IdosConstants.MYSQLDF.parse(fromDate));
				inparams.add(user.getOrganization().getId());
				inparams.add(IdosConstants.MYSQLDF.parse(fromDate));
				inparams.add(IdosConstants.MYSQLDF.parse(toDate));
			}
			List<Object[]> txnLists = genericDao.executeNativeQueryWithParamName(jpql, em, inparams);
			Double creditAmt = 0.0;
			Double debitAmt = 0.0;
			String userName;
			Long userID = 0l;
			Double openingBalance = 0.0;

			for (Object[] usertraveladvData : txnLists) {
				openingBalance = (Double) usertraveladvData[0];
				openingBalance = openingBalance == null ? 0.0 : openingBalance;
				creditAmt = Double.parseDouble(String.valueOf(usertraveladvData[1]));
				debitAmt = Double.parseDouble(String.valueOf(usertraveladvData[2]));
				if (debitAmt == null) {
					debitAmt = 0.0;
				}
				if (creditAmt == null) {
					creditAmt = 0.0;
				}
				userName = usertraveladvData[3].toString();
				userID = Long.parseLong(usertraveladvData[4].toString());
				TrialBalance tb = new TrialBalance();
				tb.setAccountName(userName + "-Claim");
				tb.setSpecId(userID);
				tb.setSpecfaccountCode("1");
				tb.setTopLevelAccountCode("4000000000000000000");
				tb.setOpeningBalance(openingBalance);
				tb.setDebit(debitAmt);
				tb.setCredit(creditAmt);
				tb.setClosingBalance(openingBalance + debitAmt - creditAmt);
				tb.setHeadType(IdosConstants.HEAD_EMP_CLAIM);
				trialBalanceList.add(tb);
			}
		} catch (ParseException e) {
			log.log(Level.SEVERE, user.getEmail(), e);
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "*****End ");
	}

	private void getReserveAndSurplusTotal(TrialBalance tb, Users user, String fromDate, String toDate, Long branchId,
			EntityManager em, Specifics specificsForMapping) {
		Double openingBalance = 0.0;
		// final Specifics specificsForMapping = coaDAO.getSpecificsForMapping(user,
		// "65", em);
		if (specificsForMapping != null) {
			if (branchId != null && branchId > 0L) {
				ArrayList inparams = new ArrayList(3);
				inparams.add(user.getOrganization().getId());
				inparams.add(branchId);
				inparams.add(specificsForMapping.getId());
				List<BranchSpecifics> branchSpecifics = genericDao.queryWithParamsName(RESERVE_SURPLUS_JPQL, em,
						inparams);
				for (BranchSpecifics branchItem : branchSpecifics) {
					openingBalance += branchItem.getOpeningBalance();
				}
			} else {
				openingBalance = specificsForMapping.getTotalOpeningBalance() != null
						? specificsForMapping.getTotalOpeningBalance()
						: 0.0;
			}
		}
		tb.setOpeningBalance(openingBalance);
		tb.setDebit(0.0);
		tb.setCredit(0.0);
		tb.setClosingBalance(openingBalance);
	}

	private void getProfitLossTotal(TrialBalance tb, Users user, String fromDate, String toDate, Long branchId,
			EntityManager em, Specifics itemSpecifics) {
		try {
			double inTotalOpeningBalance = 0.0D;
			double inTotalClosingBalance = 0.0D;
			double inTotalCredit = 0.0D;
			double inTotalDebit = 0.0D;
			List<TrialBalance> inTbList = getTrialBalanceOnCriteriaForCOAItem("1000000000000000000", 0, user, fromDate,
					toDate, branchId, em);
			for (TrialBalance trialBalanceData : inTbList) {
				inTotalOpeningBalance += trialBalanceData.getOpeningBalance() == null ? 0d
						: trialBalanceData.getOpeningBalance();
				inTotalDebit += trialBalanceData.getDebit() == null ? 0d : trialBalanceData.getDebit();
				inTotalCredit += trialBalanceData.getCredit() == null ? 0d : trialBalanceData.getCredit();
				inTotalClosingBalance += trialBalanceData.getClosingBalance() == null ? 0d
						: trialBalanceData.getClosingBalance();
			}

			double exTotalOpeningBalance = 0.0D;
			double exTotalClosingBalance = 0.0D;
			double exTotalCredit = 0.0D;
			double exTotalDebit = 0.0D;
			List<TrialBalance> exTbList = getTrialBalanceOnCriteriaForCOAItem("2000000000000000000", 0, user, fromDate,
					toDate, branchId, em);
			for (TrialBalance trialBalanceData : exTbList) {
				exTotalOpeningBalance += trialBalanceData.getOpeningBalance() == null ? 0d
						: trialBalanceData.getOpeningBalance();
				exTotalDebit += trialBalanceData.getDebit() == null ? 0d : trialBalanceData.getDebit();
				exTotalCredit += trialBalanceData.getCredit() == null ? 0d : trialBalanceData.getCredit();
				exTotalClosingBalance += trialBalanceData.getClosingBalance() == null ? 0d
						: trialBalanceData.getClosingBalance();
			}

			Double plOpeningBalance = inTotalOpeningBalance - exTotalOpeningBalance;
			Double plCredit = inTotalCredit - inTotalDebit;
			Double plDebit = exTotalDebit - exTotalCredit;
			Double plClosingBalance = plOpeningBalance + plCredit - plDebit;
			tb.setOpeningBalance(plOpeningBalance);
			tb.setDebit(plDebit);
			tb.setCredit(plCredit);
			tb.setClosingBalance(plClosingBalance);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
