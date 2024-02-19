package com.idos.dao;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.IdosUtil;
import com.idos.util.IdosConstants;
import model.*;

import model.payroll.PayrollTransaction;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.typesafe.config.Config;
import play.libs.Json;
import service.DynamicReportService;
import service.DynamicReportServiceImpl;
import service.ExceptionService;
import service.ExceptionServiceImpl;

import com.idos.util.DateUtil;
import play.Application;
import javax.inject.Inject;

import controllers.StaticController;

/**
 * Complete file is rewriten by Sunil Namdev
 * 
 * @author Sunil Namdev
 * @version 1.0
 */
public class CashNBankDAOImpl implements CashNBankDAO {
	public Application application;

	@Inject
	public CashNBankDAOImpl(Application application) {
		this.application = application;
	}

	private DynamicReportService dynReportService = new DynamicReportServiceImpl(application);
	private ExceptionService expService = new ExceptionServiceImpl();

	@Override
	public ObjectNode displayCashBook(ObjectNode result, JsonNode json, Users user, EntityManager entityManager) {
		log.log(Level.FINE, "************ Start " + json);
		try {
			result.put("result", false);
			ArrayNode cashNBankan = result.putArray("cashNBankData");
			String bnchCashNBank = json.findValue("bnchCashNBank") != null ? json.findValue("bnchCashNBank").asText()
					: null;
			String fmDate = json.findValue("fmDate") != null ? json.findValue("fmDate").asText() : null;
			String tDate = json.findValue("tDate") != null ? json.findValue("tDate").asText() : null;
			int bookType = json.findValue("bookType") != null ? json.findValue("bookType").asInt() : 0;
			Branch branch = null;
			String fromDate = null;
			String toDate = null;
			Map<String, Object> criterias = new HashMap<String, Object>();
			List<TransactionCreatedDateComparator> sortedCashNBankData = Collections.emptyList();

			if ((bnchCashNBank == null || bnchCashNBank.equals("")) && (fmDate == null || fmDate.equals(""))
					&& (tDate == null || tDate.equals(""))) {
				// Organization Cash Wise Breakups for income expense for the last one year for
				// the head office branch
				fromDate = DateUtil.returnOneMonthBackDate();
				toDate = IdosConstants.MYSQLDF.format(Calendar.getInstance().getTime());
				criterias.clear();
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("isHeadQuarter", 1);
				criterias.put("presentStatus", 1);
				branch = genericDao.getByCriteria(Branch.class, criterias, entityManager);
				// sortedCashNBankData = getCreditDebitCashTransaction(branch, cashNBankan,
				// user, fromDate, toDate, entityManager);
				sortedCashNBankData = getCreditDebitTransaction(branch, null, cashNBankan, user, fromDate, toDate,
						entityManager, bookType);
			} else {
				// CashNBank Wise Breakups for income expense for the last one or choosen date
				// for the selected branch or bank
				if (fmDate != null && !fmDate.equals("")) {
					fromDate = IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(fmDate));
				}
				if (tDate != null && !tDate.equals("")) {
					toDate = IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(tDate));
				}
				branch = Branch.findById(Long.parseLong(bnchCashNBank));

				sortedCashNBankData = getCreditDebitTransaction(branch, null, cashNBankan, user, fromDate, toDate,
						entityManager, bookType);
			}

			result.put("result", true);
			String booktyp = String.valueOf(bookType);
			Double openingBalanceForTheStatement = openingBalanceForThisCriteria(branch, null, fromDate, toDate,
					entityManager, booktyp);
			Double balance = openingBalanceForTheStatement;
			ObjectNode firstrow = Json.newObject();
			firstrow.put("txnLookUp", "");
			firstrow.put("incomeExpense", "");
			firstrow.put("createdDate", "");
			firstrow.put("debit", "");
			firstrow.put("credit", "");
			firstrow.put("transPurpose", "");
			firstrow.put("transLedger", "");
			firstrow.put("balance", IdosConstants.decimalFormat.format(openingBalanceForTheStatement));
			cashNBankan.add(firstrow);
			for (TransactionCreatedDateComparator txnData : sortedCashNBankData) {
				ObjectNode row = Json.newObject();
				row.put("txnReference", txnData.getTxnRefNumber());
				row.put("incomeExpense", txnData.getIncomeExpense() + "(" + txnData.getTxnRefNumber() + ")");
				String txn = txnData.getTxnRefNumber().substring(0, 3);
				String clmTxn = txnData.getTxnRefNumber().substring(0, 8);
				String provTxn = txnData.getTxnRefNumber().substring(0, 7);
				String payTxn = txnData.getTxnRefNumber().substring(0, 5);
				if (txn != null && !txn.equals("") && txn.equals("TXN")) {
					row.put("txnLookUp", "transactionLookUp");
				}
				if (clmTxn != null && !clmTxn.equals("") && clmTxn.equals("CLAIMTXN")) {
					row.put("txnLookUp", "claimTransactionLookUp");
				}
				if (provTxn != null && !provTxn.equals("") && provTxn.equals("PROVTXN")) {
					row.put("txnLookUp", "provisionalTransactionLookUp");
				}
				if (payTxn != null && !payTxn.equals("") && payTxn.equals("PRTXN")) {
					row.put("txnLookUp", "payTransactionLookUp");
				}
				row.put("createdDate", IdosConstants.IDOSDF.format(txnData.getCreatedDate()));
				if (txnData.getCredit() != null) {
					row.put("credit", IdosConstants.decimalFormat.format(txnData.getCredit()));
					row.put("debit", "");
					balance -= txnData.getCredit();
					row.put("balance", IdosConstants.decimalFormat.format(balance));
				}
				if (txnData.getDebit() != null) {
					row.put("credit", "");
					row.put("debit", IdosConstants.decimalFormat.format(txnData.getDebit()));
					balance += txnData.getDebit();
					row.put("balance", IdosConstants.decimalFormat.format(balance));
				}
				if (txnData.getTransactionPurpose() != null) {
					row.put("transPurpose", txnData.getTransactionPurpose());
				} else {
					row.put("transPurpose", "");
				}
				if (txnData.getLedgerCustVend() != null) {
					row.put("transLedger", txnData.getLedgerCustVend());
				} else {
					row.put("transLedger", "");
				}
				cashNBankan.add(row);
			}

		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
		}
		log.log(Level.FINE, "************ End " + result);
		return result;
	}

	@Override
	public ObjectNode displayBankBook(ObjectNode result, JsonNode json, Users user, EntityManager entityManager) {
		log.log(Level.FINE, "************ Start " + json);
		try {
			result.put("result", false);
			ArrayNode bankBookData = result.putArray("bankBookData");
			String branchInput = json.findValue("bnchCashNBank") != null ? json.findValue("bnchCashNBank").asText()
					: null;
			String branchBank = json.findValue("bnkCashNBank") != null ? json.findValue("bnkCashNBank").asText() : null;
			String fmDate = json.findValue("fmDate") != null ? json.findValue("fmDate").asText() : null;
			String tDate = json.findValue("tDate") != null ? json.findValue("tDate").asText() : null;

			Branch branch = null;
			BranchBankAccounts bnchBankAccount = null;
			String fromDate = null;
			String toDate = null;
			Map<String, Object> criterias = new HashMap<String, Object>();
			List<TransactionCreatedDateComparator> sortedCashNBankData = Collections.emptyList();

			if (fmDate != null && !fmDate.equals("")) {
				fromDate = IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(fmDate));
			}
			if (tDate != null && !tDate.equals("")) {
				toDate = IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(tDate));
			}

			if ((branchInput == null || branchInput.equals("")) && (branchBank == null || branchBank.equals(""))
					&& (fmDate == null || fmDate.equals("")) && (tDate == null || tDate.equals(""))) {
				// Organization Wise Bank Breakups for income expense for the last one year for
				// the head office branch
				fromDate = DateUtil.returnOneMonthBackDate();
				toDate = IdosConstants.MYSQLDF.format(Calendar.getInstance().getTime());
				criterias.clear();
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("isHeadQuarter", 1);
				criterias.put("presentStatus", 1);
				branch = genericDao.getByCriteria(Branch.class, criterias, entityManager);
				sortedCashNBankData = getCreditDebitTransaction(branch, bnchBankAccount, bankBookData, user, fromDate,
						toDate, entityManager, 2);
			} else {
				// Bank Wise Breakups for income expense for the last one or choosen date for
				// the selected branch or bank
				fromDate = IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(fmDate));
				toDate = IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(tDate));
				branch = Branch.findById(Long.parseLong(branchInput));
				if (branchBank != null && !branchBank.equals("")) {
					bnchBankAccount = BranchBankAccounts.findById(Long.parseLong(branchBank));
				}
				sortedCashNBankData = getCreditDebitTransaction(branch, bnchBankAccount, bankBookData, user, fromDate,
						toDate, entityManager, 2);
			}
			// first record as balance should be display always even there no transaction
			// records.
			result.put("result", true);
			Double openingBalanceForTheStatement = openingBalanceForThisCriteria(branch, bnchBankAccount, fromDate,
					toDate, entityManager, "2");
			Double balance = openingBalanceForTheStatement;
			ObjectNode firstrow = Json.newObject();
			firstrow.put("txnLookUp", "");
			firstrow.put("incomeExpense", "");
			firstrow.put("createdDate", "");
			firstrow.put("debit", "");
			firstrow.put("credit", "");
			firstrow.put("balance", IdosConstants.decimalFormat.format(openingBalanceForTheStatement));
			firstrow.put("instrumentNumber", "");
			firstrow.put("instrumentDate", "");
			firstrow.put("txnRefNumber", "");
			firstrow.put("transPurpose", "");
			firstrow.put("transLedger", "");
			firstrow.put("brsBankDate", "");
			bankBookData.add(firstrow);

			for (TransactionCreatedDateComparator txnData : sortedCashNBankData) {
				ObjectNode row = Json.newObject();
				row.put("txnReference", txnData.getTxnRefNumber());
				row.put("incomeExpense", txnData.getIncomeExpense() + "(" + txnData.getTxnRefNumber() + ")");
				String txn = txnData.getTxnRefNumber().substring(0, 3);
				String clmTxn = txnData.getTxnRefNumber().substring(0, 8);
				String provTxn = txnData.getTxnRefNumber().substring(0, 7);
				if (txn != null && !txn.equals("") && txn.equals("TXN")) {
					row.put("txnLookUp", "transactionLookUp");
				}
				if (clmTxn != null && !clmTxn.equals("") && clmTxn.equals("CLAIMTXN")) {
					row.put("txnLookUp", "claimTransactionLookUp");
				}
				if (provTxn != null && !provTxn.equals("") && provTxn.equals("PROVTXN")) {
					row.put("txnLookUp", "provisionalTransactionLookUp");
				}
				row.put("createdDate", IdosConstants.IDOSDF.format(txnData.getCreatedDate()));
				if (txnData.getCredit() != null) {
					row.put("debit", "");
					row.put("credit", IdosConstants.decimalFormat.format(txnData.getCredit()));
					balance -= txnData.getCredit();
					row.put("balance", IdosConstants.decimalFormat.format(balance));
				}
				if (txnData.getDebit() != null) {
					row.put("debit", IdosConstants.decimalFormat.format(txnData.getDebit()));
					row.put("credit", "");
					balance += txnData.getDebit();
					row.put("balance", IdosConstants.decimalFormat.format(balance));
				}

				if (txnData.getInstrumentNumber() != null) {
					row.put("instrumentNumber", txnData.getInstrumentNumber());
				} else {
					row.put("instrumentNumber", "");
				}

				if (txnData.getInstrumentDate() != null) {
					row.put("instrumentDate", txnData.getInstrumentDate());
				} else {
					row.put("instrumentDate", "");
				}
				if (txnData.getTxnRefNumber() != null) {
					row.put("txnRefNumber", txnData.getTxnRefNumber());
				} else {
					row.put("txnRefNumber", "");
				}
				if (txnData.getBrsBankDate() != null) {
					row.put("brsBankDate", txnData.getBrsBankDate());
				} else {
					row.put("brsBankDate", "");
				}
				if (txnData.getTransactionPurpose() != null) {
					row.put("transPurpose", txnData.getTransactionPurpose());
				} else {
					row.put("transPurpose", "");
				}
				if (txnData.getLedgerCustVend() != null) {
					row.put("transLedger", txnData.getLedgerCustVend());
				} else {
					row.put("transLedger", "");
				}
				bankBookData.add(row);
			}

		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
		}
		log.log(Level.FINE, "************ End " + result);
		return result;
	}

	private void setTransactionDetail(EntityManager entityManager, String sqlStr,
			ArrayList<TransactionCreatedDateComparator> txnCashBankListUnsorted, int transactionType,
			boolean isCredit) {
		String itemName = null;
		if (transactionType == 1) {
			List<Transaction> txnList = genericDao.executeSimpleQuery(sqlStr, entityManager);
			if (txnList.size() > 0) {
				for (Transaction txn : txnList) {
					TransactionCreatedDateComparator newObj = new TransactionCreatedDateComparator();
					// Want to show Income:Scrap Sale
					if (null == txn.getTransactionSpecifics() || null == txn.getTransactionSpecifics().getName()) {
						itemName = "";
					} else {
						itemName = txn.getTransactionSpecifics().getName();
					}
					if (isCredit) {
						newObj.setIncomeExpense("Expense:" + itemName);
						if (txn.getTransactionPurpose().getId() == 8) {
							Double netAmountAfterTDS = txn.getNetAmount();
							if (txn.getWithholdingTax() != null) {
								netAmountAfterTDS -= txn.getWithholdingTax();
							}
							newObj.setCredit(netAmountAfterTDS);
						} else {
							newObj.setCredit(txn.getNetAmount());
						}
						newObj.setDebit(null);
					} else {
						newObj.setIncomeExpense("Income:" + itemName);
						newObj.setCredit(null);
						newObj.setDebit(txn.getNetAmount());
					}
					newObj.setCreatedDate(txn.getTransactionDate());
					newObj.setTxnRefNumber(txn.getTransactionRefNumber());
					newObj.setInstrumentDate(txn.getInstrumentDate());
					newObj.setInstrumentNumber(txn.getInstrumentNumber());
					newObj.setBrsBankDate(txn.getBrsBankDate());
					newObj.setTransactionPurpose(txn.getTransactionPurpose().getTransactionPurpose());
					newObj.setLedgerCustVend(getTransLedgerCustVend(txn, 1, txn.getTransactionBranch().getId()));
					txnCashBankListUnsorted.add(newObj);
				}
			}
		} else if (transactionType == 2) {

		} else if (transactionType == 3) {
			List<ClaimTransaction> clmTxnList = genericDao.executeSimpleQuery(sqlStr, entityManager);
			if (clmTxnList.size() > 0) {
				for (ClaimTransaction txn : clmTxnList) {
					TransactionCreatedDateComparator newObj = new TransactionCreatedDateComparator();

					if (isCredit) {
						if (txn.getTransactionPurpose().getId() == 15) {
							newObj.setIncomeExpense("Expense:Travel Advance");
							newObj.setCredit(txn.getClaimsNetSettlement());
						} else if (txn.getTransactionPurpose().getId() == 16) {
							newObj.setIncomeExpense("Expense:Travel Advance");
							newObj.setCredit(txn.getClaimsRequiredSettlement());
						} else if (txn.getTransactionPurpose().getId() == 17) {
							newObj.setIncomeExpense("Expense:Expense Advance");
							newObj.setCredit(txn.getNewAmount());
						} else if (txn.getTransactionPurpose().getId() == 18) {
							newObj.setIncomeExpense("Expense:Expense Advance");
							newObj.setCredit(txn.getClaimsRequiredSettlement());
						} else if (txn.getTransactionPurpose().getId() == 19) {
							newObj.setIncomeExpense("Expense:Reimbursement");
							newObj.setCredit(txn.getClaimsNetSettlement());
						}
						newObj.setDebit(null);
					} else {
						if (txn.getTransactionPurpose().getId() == 15 || txn.getTransactionPurpose().getId() == 16) {
							newObj.setIncomeExpense("Income:Travel Advance");
						} else if (txn.getTransactionPurpose().getId() == 17
								|| txn.getTransactionPurpose().getId() == 18) {
							newObj.setIncomeExpense("Income:Expense Advance");
						}
						newObj.setCredit(null);
						newObj.setDebit(txn.getAmountReturnInCaseOfDueToCompany());
					}
					newObj.setCreatedDate(txn.getTransactionDate());
					newObj.setTxnRefNumber(txn.getTransactionRefNumber());
					newObj.setInstrumentDate(txn.getInstrumentDate());
					newObj.setInstrumentNumber(txn.getInstrumentNumber());
					newObj.setTransactionPurpose(txn.getTransactionPurpose().getTransactionPurpose());
					newObj.setLedgerCustVend(txn.getCreatedBy().getFullName());
					newObj.setBrsBankDate(txn.getBrsBankDate());
					txnCashBankListUnsorted.add(newObj);
				}
			}
		} else if (transactionType == 4) {
			List<PayrollTransaction> payrollTxnList = genericDao.executeSimpleQuery(sqlStr, entityManager);
			for (PayrollTransaction txn : payrollTxnList) {
				TransactionCreatedDateComparator newObj = new TransactionCreatedDateComparator();

				itemName = DateUtil.getMonthName(txn.getPayslipMonth());
				itemName += "," + txn.getPayslipYear();

				newObj.setIncomeExpense("Expense:" + itemName);
				newObj.setCredit(txn.getTotalNetPay());
				newObj.setDebit(null);

				newObj.setCreatedDate(txn.getTransactionDate());
				newObj.setTxnRefNumber(txn.getTransactionRefNumber());
				newObj.setInstrumentDate(txn.getInstrumentDate());
				newObj.setInstrumentNumber(txn.getInstrumentNumber());
				newObj.setBrsBankDate(txn.getInstrumentDate());
				newObj.setTransactionPurpose(txn.getTransactionPurpose().getTransactionPurpose());
				newObj.setLedgerCustVend("");
				txnCashBankListUnsorted.add(newObj);
			}
		}

	}

	/**
	 *
	 * @param branch
	 * @param bnchBankAccount
	 * @param an
	 * @param user
	 * @param fromDate
	 * @param toDate
	 * @param entityManager
	 * @param paymentMode
	 * @return
	 */
	private List getCreditDebitTransaction(Branch branch, BranchBankAccounts bnchBankAccount, ArrayNode an, Users user,
			String fromDate, String toDate, EntityManager entityManager, int bookType) {
		StringBuilder txnsbr = new StringBuilder("select obj from Transaction obj WHERE obj.transactionBranch='");
		txnsbr.append(branch.getId()).append("' and obj.transactionBranchOrganization='")
				.append(branch.getOrganization().getId()).append("' and obj.receiptDetailsType (1?) ");
		if (bnchBankAccount != null && !bnchBankAccount.equals("")) {
			txnsbr.append(" and obj.transactionBranchBankAccount='").append(bnchBankAccount.getId()).append("'");
		}
		txnsbr.append(
				" AND obj.presentStatus=1 AND obj.transactionPurpose=(2?) and obj.transactionStatus='Accounted' and obj.transactionDate  between '");
		txnsbr.append(fromDate).append("' and '").append(toDate).append("'");
		String transactionSQL = txnsbr.toString();

		StringBuilder claimTxnQuery = new StringBuilder(
				"select obj from ClaimTransaction obj WHERE obj.transactionBranch='");
		claimTxnQuery.append(branch.getId()).append("' and obj.transactionBranchOrganization='")
				.append(branch.getOrganization().getId()).append("' and obj.receiptDetailType (1?) ");
		if (bnchBankAccount != null && !bnchBankAccount.equals("")) {
			claimTxnQuery.append(" and obj.transactionBranchBankAccount='").append(bnchBankAccount.getId()).append("'");
		}
		claimTxnQuery.append(
				" AND obj.presentStatus=1 AND obj.transactionPurpose=(2?) and obj.transactionStatus='Accounted' and obj.transactionDate  between '");
		claimTxnQuery.append(fromDate).append("' and '").append(toDate).append("'");
		String claimSQL = claimTxnQuery.toString();

		StringBuilder journalTxnQuery = new StringBuilder(
				"select obj from IdosProvisionJournalEntry obj WHERE obj.debitBranch='");
		journalTxnQuery.append(branch.getId()).append("' and obj.provisionMadeForOrganization=")
				.append(branch.getOrganization().getId());
		journalTxnQuery.append(
				" AND obj.presentStatus=1 AND obj.transactionPurpose=20 and obj.transactionStatus='Accounted' and obj.transactionDate  between '");
		journalTxnQuery.append(fromDate).append("' and '").append(toDate).append("' order by obj.transactionDate ASC");

		ArrayList<TransactionCreatedDateComparator> txnCashBankListUnsorted = new ArrayList<TransactionCreatedDateComparator>();
		String sqlTmp = null;
		String paymentMode = String.valueOf(bookType);
		paymentMode = "=" + paymentMode;
		// only branch particular bank transaction is considered
		for (int i = 1; i <= 38; i++) {// since there are 24 transaction questions
			switch (i) {
				case 1:
					// Sell on cash & collect payment now transaction always results into credit
					// amount to the concerned branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "1"); // transaction purpose
					setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, false);
					break;
				case 2:// Sell on credit & collect payment later transaction no effect on cash and bank
					break;
				case 3:// Buy on cash & pay right away always results in debit amount to concerned
						// branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "3"); // transaction purpose
					setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, true);
					break;
				case 4:// Buy on credit & pay later no effect on cash and bank
					break;
				case 5:// Receive payment from customer results into credit amount to the concerned
						// branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "5"); // transaction purpose
					setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, false);
					break;
				case 6:// Receive advance from customer results into credit amount to the concerned
						// branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "6"); // transaction purpose
					setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, false);
					break;

				case 7:// Pay vendor/supplier always results in debit amount to concerned branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "7"); // transaction purpose
					setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, true);
					break;
				case 8:// Pay advance to vendor or supplier always results in debit amount to concerned
						// branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "8"); // transaction purpose
					setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, true);
					break;
				case 9:// Receive special adjustments amount from vendors results into credit amount to
						// the concerned branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "9"); // transaction purpose
					setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, false);
					break;
				case 10:// Pay special adjustments amount to vendors always results t debit amount to
						// the concerned branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "10"); // transaction purpose
					setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, true);
					break;
				case 11:// Buy on Petty Cash Account always results in debit amount to concerned branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "11"); // transaction purpose
					setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, true);
					break;
				case 12:// Sales returns no effect on cash and bank added to customer advance account
					break;
				case 13:// Purchase returns no effect on cash and bank added to vendor advance account
					break;
				case 14:// Transfer main cash to petty cash no effect bank
					if (bookType != 2) {
						sqlTmp = transactionSQL;
						sqlTmp = sqlTmp.replace("(1?)", "=1"); // cash
						sqlTmp = sqlTmp.replace("(2?)", "14"); // transaction purpose
						if (bookType == 3) {
							setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, false);
						} else {
							setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, true);
						}
					}
					break;
				case 15: // Request For Travel Advance always results in debit amount to concerned branch
					sqlTmp = claimSQL;
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "15");
					setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 3, true);
					break;
				case 16:
					/*
					 * Settle Travel Advance effect on cash and bank depends on weather settlement
					 * done for traveladvance
					 * claimtxn requires more amount or return amount in case of access if require
					 * then debit to that
					 * particular branch cash account if return then credit to that particular
					 * branch cash account
					 */
					sqlTmp = claimSQL;
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "16");
					String claimTmp = sqlTmp + " and obj.claimsRequiredSettlement>0.0";
					setTransactionDetail(entityManager, claimTmp, txnCashBankListUnsorted, 3, true);
					claimTmp = sqlTmp + " and obj.amountReturnInCaseOfDueToCompany>0.0";
					setTransactionDetail(entityManager, claimTmp, txnCashBankListUnsorted, 3, false);
					break;
				case 17: // Request Advance For Expense always results in debit amount to concerned
							// branch
					sqlTmp = claimSQL;
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "17");
					setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 3, true);
					break;
				case 18:
					/*
					 * Settle Advance For Expense effect on cash and bank depends on weather
					 * settlement done for expense advance requires
					 * more amount or return amount in case of access if require then debit to that
					 * particular branch cash account
					 * if return then credit to that particular branch cash account
					 */
					sqlTmp = claimSQL;
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "18");
					claimTmp = sqlTmp + " and obj.claimsRequiredSettlement>0.0";
					setTransactionDetail(entityManager, claimTmp, txnCashBankListUnsorted, 3, true);
					claimTmp = sqlTmp + " and obj.amountReturnInCaseOfDueToCompany>0.0";
					setTransactionDetail(entityManager, claimTmp, txnCashBankListUnsorted, 3, false);
					break;
				case 19: // Request For Expense Reimbursement results in debit amount to concerned branch
					sqlTmp = claimSQL;
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "19");
					setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 3, true);
					break;
				case 20:
					if (bnchBankAccount != null && !bnchBankAccount.equals("")) {
						getProvisionJournalEntryCreditDebit(entityManager, txnCashBankListUnsorted,
								journalTxnQuery.toString(), false, IdosConstants.HEAD_BANK, bnchBankAccount.getId());
						getProvisionJournalEntryCreditDebit(entityManager, txnCashBankListUnsorted,
								journalTxnQuery.toString(), true, IdosConstants.HEAD_BANK, bnchBankAccount.getId());
					} else if (bookType == 1) {
						getProvisionJournalEntryCreditDebit(entityManager, txnCashBankListUnsorted,
								journalTxnQuery.toString(), false, IdosConstants.HEAD_CASH, null);
						getProvisionJournalEntryCreditDebit(entityManager, txnCashBankListUnsorted,
								journalTxnQuery.toString(), true, IdosConstants.HEAD_CASH, null);
					} else if (bookType == 3) {
						getProvisionJournalEntryCreditDebit(entityManager, txnCashBankListUnsorted,
								journalTxnQuery.toString(), false, IdosConstants.HEAD_PETTY, null);
						getProvisionJournalEntryCreditDebit(entityManager, txnCashBankListUnsorted,
								journalTxnQuery.toString(), true, IdosConstants.HEAD_PETTY, null);
					}
					break;
				case 22: // Withdraw Cash From Bank results into credit amount to the concerned branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("and obj.receiptDetailsType (1?)", "");
					sqlTmp = sqlTmp.replace("(2?)", "22"); // transaction purpose
					if (bookType == 1) {
						setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, false);
					} else if (bookType == 2) {
						setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, true);
					}
					break;
				case 23:// Deposit Cash In Bank always results in debit amount to concerned branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("and obj.receiptDetailsType (1?)", "");
					sqlTmp = sqlTmp.replace("(2?)", "23"); // transaction purpose
					if (bookType == 1) {
						setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, true);
					} else if (bookType == 2) {
						setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, false);
					}
					break;
				case 24:
					/*
					 * Transfer Funds From One Bank To Another has debit effect one one bank and
					 * credit effect to another bank
					 */
					if (bookType == 2) {
						sqlTmp = transactionSQL;
						sqlTmp = sqlTmp.replace("and obj.receiptDetailsType (1?)", "");
						sqlTmp = sqlTmp.replace("(2?)", "24"); // transaction purpose
						setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, true); // first check in
																										// from bank
						sqlTmp = sqlTmp.replace("transactionBranchBankAccount", "transactionToBranchBankAccount");
						sqlTmp = sqlTmp.replace("transactionBranch", "transactionToBranch");
						setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, false); // check in to
																										// bank
					}
					break;
				case 34:
					StringBuilder paysbr = new StringBuilder(
							"select obj from PayrollTransaction obj WHERE obj.branch.id=");
					paysbr.append(branch.getId()).append(" and obj.organization.id=")
							.append(branch.getOrganization().getId());
					if (bnchBankAccount != null && !bnchBankAccount.equals("")) {
						paysbr.append(" and obj.transactionBranchBankAccount.id=").append(bnchBankAccount.getId());
					}
					paysbr.append(
							" and obj.transactionPurpose=34 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '");
					paysbr.append(fromDate).append("' and '").append(toDate).append("'");
					sqlTmp = paysbr.toString();
					setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 4, true);
					break;
				case 35:
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "35"); // transaction purpose
					setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, true);
					break;
				case 36:
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "36"); // transaction purpose
					setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, true);
					break;
				case 38:
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "38"); // transaction purpose
					sqlTmp += " and obj.typeIdentifier = 1";
					setTransactionDetail(entityManager, sqlTmp, txnCashBankListUnsorted, 1, true);
					break;
			}
		}
		Collections.sort(txnCashBankListUnsorted, new TransactionCreatedDateComparator());
		return txnCashBankListUnsorted;
	}

	private Double getOpeningBalance(EntityManager entityManager, String sqlStr, int transactionType,
			boolean isCredit) {
		Double openingBalance = 0.0;
		if (transactionType == 1) {
			List<Transaction> txnList = genericDao.executeSimpleQuery(sqlStr, entityManager);
			if (txnList.size() > 0) {
				Object val = txnList.get(0);
				if (val != null) {
					openingBalance += Double.parseDouble(String.valueOf(val));
				}
			}
		} else if (transactionType == 2) {
			List<IdosProvisionJournalEntry> provJourTxn = genericDao.executeSimpleQuery(sqlStr, entityManager);
			if (provJourTxn.size() > 0) {
				Object val = provJourTxn.get(0);
				if (val != null) {
					openingBalance += Double.parseDouble(String.valueOf(val));
				}
			}
		} else if (transactionType == 3) {
			List<ClaimTransaction> clmTxnList = genericDao.executeSimpleQuery(sqlStr, entityManager);
			if (clmTxnList.size() > 0) {
				Object val = clmTxnList.get(0);
				if (val != null) {
					openingBalance += Double.parseDouble(String.valueOf(val));
				}
			}
		}
		return openingBalance;
	}

	private Double openingBalanceForThisCriteria(Branch branch, BranchBankAccounts bnchBankAccount, String fromDate,
			String toDate, EntityManager entityManager, String paymentMode) {
		Double openingBalance = 0.0;
		Double openingBalanceCredit = 0.0;
		Double openingBalanceDebit = 0.0;
		Double startBalance = 0.0;
		int bookType = Integer.parseInt(paymentMode);
		if (bookType == 2) {
			StringBuilder balanceSQL = new StringBuilder(
					"select SUM(obj.creditAmount),SUM(obj.debitAmount), obj.date from TrialBalanceBranchBank obj WHERE obj.branch.id=");
			balanceSQL.append(branch.getId()).append(" AND obj.organization.id=")
					.append(branch.getOrganization().getId());
			if (bnchBankAccount != null && !bnchBankAccount.equals("")) {// only branch particular bank transaction is
																			// considered
				balanceSQL.append(" AND obj.branchBankAccounts.id=").append(bnchBankAccount.getId());
			}
			balanceSQL.append(" AND obj.date < '").append(fromDate)
					.append("' and obj.presentStatus=1 GROUP BY obj.date, obj.branchBankAccounts.id ORDER BY obj.date desc");
			if (log.isLoggable(Level.INFO))
				log.log(Level.INFO, "HQL: " + balanceSQL);
			List<Object[]> txnLists = entityManager.createQuery(balanceSQL.toString()).getResultList();
			Double creditAmt = 0.0;
			Double debitAmt = 0.0;
			java.util.Date lastDate = null;
			Double openingBal = 0.0;
			if (bnchBankAccount != null) {
				openingBal = bnchBankAccount.getOpeningBalance() == null ? 0.0 : bnchBankAccount.getOpeningBalance();
			}

			if (!txnLists.isEmpty()) {
				for (Object[] custData : txnLists) {
					creditAmt += Double.parseDouble(String.valueOf(custData[0] != null ? custData[0] : 0.0));
					debitAmt += Double.parseDouble(String.valueOf(custData[1] != null ? custData[1] : 0.0));
					lastDate = (java.util.Date) custData[2];
				}
				startBalance = openingBal + debitAmt - creditAmt;
				if (lastDate != null) {
					fromDate = IdosConstants.MYSQLDF.format(lastDate);
				} else {
					fromDate = DateUtil.returnOneBackDate(fromDate);
				}
			} else {
				startBalance = openingBal;
				fromDate = DateUtil.returnOneBackDate(fromDate);
			}
			toDate = DateUtil.returnOneBackDate(fromDate);
		} else if (bookType == 1 || (bookType == 3)) {
			StringBuilder balanceSQL = new StringBuilder(
					"select SUM(obj.creditAmount), SUM(obj.debitAmount), obj.date from TrialBalanceBranchCash obj WHERE obj.branch.id=");
			balanceSQL.append(branch.getId()).append(" AND obj.organization.id=")
					.append(branch.getOrganization().getId());
			Double openingBal = 0.0;
			if (bookType == 1) {
				balanceSQL.append(" AND cashType = 1");
				openingBal = branch.getBranchDepositKeys().get(0).getOpeningBalance() == null ? 0.0
						: branch.getBranchDepositKeys().get(0).getOpeningBalance();
			} else {
				balanceSQL.append(" AND cashType = 2");
				openingBal = branch.getBranchDepositKeys().get(0).getPettyCashOpeningBalance() == null ? 0.0
						: branch.getBranchDepositKeys().get(0).getPettyCashOpeningBalance();
			}
			balanceSQL.append(" AND obj.date < '").append(fromDate)
					.append("' and obj.presentStatus=1 GROUP BY obj.date ORDER BY obj.date desc");
			log.log(Level.INFO, "SQL =" + balanceSQL);
			List<Object[]> txnLists = entityManager.createQuery(balanceSQL.toString()).getResultList();
			Double creditAmt = 0.0;
			Double debitAmt = 0.0;
			java.util.Date lastDate = null;

			if (!txnLists.isEmpty()) {
				for (Object[] custData : txnLists) {
					String tmp1 = custData[0] == null ? "0.0" : String.valueOf(custData[0]);
					String tmp2 = custData[1] == null ? "0.0" : String.valueOf(custData[1]);
					creditAmt += Double.parseDouble(tmp1);
					debitAmt += Double.parseDouble(tmp2);
					lastDate = (java.util.Date) custData[2];
				}
				startBalance = openingBal + debitAmt - creditAmt;
				if (lastDate != null) {
					fromDate = IdosConstants.MYSQLDF.format(lastDate);
				} else {
					fromDate = DateUtil.returnOneBackDate(fromDate);
				}
			} else {
				startBalance = openingBal;
				fromDate = DateUtil.returnOneBackDate(fromDate);
			}
			toDate = DateUtil.returnOneBackDate(fromDate);
		}

		StringBuilder txnsbr = new StringBuilder("select SUM((3?)) from Transaction obj WHERE obj.transactionBranch='");
		txnsbr.append(branch.getId()).append("' and obj.transactionBranchOrganization='")
				.append(branch.getOrganization().getId()).append("' and obj.receiptDetailsType (1?) ");
		if (bnchBankAccount != null && !bnchBankAccount.equals("")) {
			txnsbr.append(" and obj.transactionBranchBankAccount='").append(bnchBankAccount.getId()).append("'");
		}
		txnsbr.append(
				" AND obj.transactionPurpose=(2?) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '");
		txnsbr.append(fromDate).append("' and '").append(toDate).append("'");
		String transactionSQL = txnsbr.toString();

		StringBuilder claimTxnQuery = new StringBuilder(
				"select SUM((3?)) from ClaimTransaction obj WHERE obj.transactionBranch='");
		claimTxnQuery.append(branch.getId()).append("' and obj.transactionBranchOrganization='")
				.append(branch.getOrganization().getId()).append("' and obj.receiptDetailType (1?) ");
		if (bnchBankAccount != null && !bnchBankAccount.equals("")) {
			claimTxnQuery.append(" and obj.transactionBranchBankAccount='").append(bnchBankAccount.getId()).append("'");
		}
		claimTxnQuery.append(
				" AND obj.transactionPurpose=(2?) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '");
		claimTxnQuery.append(fromDate).append("' and '").append(toDate).append("'");
		String claimSQL = claimTxnQuery.toString();

		StringBuilder journalTxnQuery = new StringBuilder(
				"select sum(obj.headAmount) from ProvisionJournalEntryDetail obj where obj.isDebit=(1?) ");
		if (bnchBankAccount != null && !bnchBankAccount.equals("")) {
			journalTxnQuery.append(" and obj.headType='").append(IdosConstants.HEAD_BANK);
			journalTxnQuery.append("' and obj.headID=").append(bnchBankAccount.getId());
		} else if ("1".equals(paymentMode)) {
			journalTxnQuery.append(" and obj.headType='").append(IdosConstants.HEAD_CASH).append("'");
		} else if ("3".equals(paymentMode)) {
			journalTxnQuery.append(" and obj.headType='").append(IdosConstants.HEAD_PETTY).append("'");
		}
		journalTxnQuery.append(
				" and obj.provisionJournalEntry in (select obj1.id from IdosProvisionJournalEntry obj1 WHERE obj1.debitBranch='");
		journalTxnQuery.append(branch.getId()).append("' and obj1.provisionMadeForOrganization=")
				.append(branch.getOrganization().getId());
		journalTxnQuery.append(
				" AND obj1.transactionPurpose=20 and obj1.transactionStatus='Accounted' and obj1.presentStatus=1 and obj1.transactionDate  between '");
		journalTxnQuery.append(fromDate).append("' and '").append(toDate).append("')");

		paymentMode = "=" + paymentMode;
		String sqlTmp = null;
		// only branch particular bank transaction is considered
		for (int i = 1; i <= 24; i++) {// since there are 24 transaction questions
			switch (i) {
				case 1: // Sell on cash & collect payment now transaction always results into credit
						// amount to the concerned branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(3?)", "obj.netAmount");
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "1"); // transaction purpose
					openingBalanceDebit += getOpeningBalance(entityManager, sqlTmp, 1, false);
					break;
				case 2:// Sell on credit & collect payment later transaction no effect on cash and bank
					break;
				case 3:// Buy on cash & pay right away always results in debit amount to concerned
						// branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(3?)", "obj.netAmount");
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "3"); // transaction purpose
					openingBalanceCredit += getOpeningBalance(entityManager, sqlTmp, 1, true);
					break;
				case 4:// Buy on credit & pay later no effect on cash and bank
					break;
				case 5:// Receive payment from customer results into credit amount to the concerned
						// branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(3?)", "obj.netAmount");
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "5"); // transaction purpose
					openingBalanceDebit += getOpeningBalance(entityManager, sqlTmp, 1, false);
					break;
				case 6:// Receive advance from customer results into credit amount to the concerned
						// branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(3?)", "obj.netAmount");
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "6"); // transaction purpose
					openingBalanceDebit += getOpeningBalance(entityManager, sqlTmp, 1, false);
					break;

				case 7:// Pay vendor/supplier always results in debit amount to concerned branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(3?)", "obj.netAmount");
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "7"); // transaction purpose
					openingBalanceCredit += getOpeningBalance(entityManager, sqlTmp, 1, true);
					break;
				case 8:// Pay advance to vendor or supplier always results in debit amount to concerned
						// branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(3?)", "obj.netAmount");
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "8"); // transaction purpose
					openingBalanceCredit += getOpeningBalance(entityManager, sqlTmp, 1, true);
					break;
				case 9:// Receive special adjustments amount from vendors results into credit amount to
						// the concerned branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(3?)", "obj.netAmount");
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "9"); // transaction purpose
					openingBalanceDebit += getOpeningBalance(entityManager, sqlTmp, 1, false);
					break;
				case 10:// Pay special adjustments amount to vendors always results t debit amount to
						// the concerned branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(3?)", "obj.netAmount");
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "10"); // transaction purpose
					openingBalanceCredit += getOpeningBalance(entityManager, sqlTmp, 1, true);
					break;
				case 11:// Buy on Petty Cash Account always results in debit amount to concerned branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(3?)", "obj.netAmount");
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "11"); // transaction purpose
					openingBalanceCredit += getOpeningBalance(entityManager, sqlTmp, 1, true);
					break;
				case 12:// Sales returns no effect on cash and bank added to customer advance account
					break;
				case 13:// Purchase returns no effect on cash and bank added to vendor advance account
					break;
				case 14:// Transfer main cash to petty cash no effect on cash and bank
					if (bookType != 2) {
						sqlTmp = transactionSQL;
						sqlTmp = sqlTmp.replace("(3?)", "obj.netAmount");
						sqlTmp = sqlTmp.replace("(1?)", "=1"); // cash only
						sqlTmp = sqlTmp.replace("(2?)", "14"); // transaction purpose
						if (bookType == 3) {
							openingBalanceDebit += getOpeningBalance(entityManager, sqlTmp, 1, false);
						} else {
							openingBalanceCredit += getOpeningBalance(entityManager, sqlTmp, 1, true);
						}
					}
					break;
				case 15: // Request For Travel Advance always results in debit amount to concerned branch
					sqlTmp = claimSQL;
					sqlTmp = sqlTmp.replace("(3?)", "obj.newAmount");
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "15");
					openingBalanceCredit += getOpeningBalance(entityManager, sqlTmp, 3, true);
					break;
				case 16:
					/*
					 * Settle Travel Advance effect on cash and bank depends on weather settlement
					 * done for traveladvance
					 * claimtxn requires more amount or return amount in case of access if require
					 * then debit to that
					 * particular branch cash account if return then credit to that particular
					 * branch cash account
					 */
					sqlTmp = claimSQL;
					sqlTmp = sqlTmp.replace("(3?)", "obj.claimsRequiredSettlement");
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "16");
					String claimTmp = sqlTmp + " and obj.claimsRequiredSettlement>0.0";
					openingBalanceCredit += getOpeningBalance(entityManager, claimTmp, 3, true);
					sqlTmp = claimSQL;
					sqlTmp = sqlTmp.replace("(3?)", "obj.amountReturnInCaseOfDueToCompany");
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "16");
					claimTmp = sqlTmp + " and obj.amountReturnInCaseOfDueToCompany>0.0";
					openingBalanceDebit += getOpeningBalance(entityManager, claimTmp, 3, false);

					break;
				case 17: // Request Advance For Expense always results in debit amount to concerned
							// branch
					sqlTmp = claimSQL;
					sqlTmp = sqlTmp.replace("(3?)", "obj.newAmount");
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "17");
					openingBalanceCredit += getOpeningBalance(entityManager, sqlTmp, 3, true);
					break;
				case 18:
					/*
					 * Settle Advance For Expense effect on cash and bank depends on weather
					 * settlement done for expense advance requires
					 * more amount or return amount in case of access if require then debit to that
					 * particular branch cash account
					 * if return then credit to that particular branch cash account
					 */
					sqlTmp = claimSQL;
					sqlTmp = sqlTmp.replace("(3?)", "obj.claimsRequiredSettlement");
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "18");
					claimTmp = sqlTmp + " and obj.claimsRequiredSettlement>0.0";
					openingBalanceCredit += getOpeningBalance(entityManager, claimTmp, 3, true);

					sqlTmp = claimSQL;
					sqlTmp = sqlTmp.replace("(3?)", "obj.amountReturnInCaseOfDueToCompany");
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "18");
					claimTmp = sqlTmp + " and obj.amountReturnInCaseOfDueToCompany>0.0";
					openingBalanceDebit += getOpeningBalance(entityManager, claimTmp, 3, false);
					break;
				case 19: // Request For Expense Reimbursement results in debit amount to concerned branch
					sqlTmp = claimSQL;
					sqlTmp = sqlTmp.replace("(3?)", "obj.newAmount");
					sqlTmp = sqlTmp.replace("(1?)", paymentMode);
					sqlTmp = sqlTmp.replace("(2?)", "19");
					openingBalanceCredit += getOpeningBalance(entityManager, sqlTmp, 3, true);
					break;
				case 20:
					sqlTmp = journalTxnQuery.toString();
					sqlTmp = sqlTmp.replace("(1?)", "1");
					openingBalanceDebit += getOpeningBalance(entityManager, sqlTmp, 2, false); // fetch debit records
					/* now fetching credit records */
					sqlTmp = journalTxnQuery.toString();
					sqlTmp = sqlTmp.replace("(1?)", "0");
					openingBalanceCredit += getOpeningBalance(entityManager, sqlTmp, 2, true);
					break;
				case 22: // Withdraw Cash From Bank results into credit amount to the concerned branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(3?)", "obj.netAmount");
					sqlTmp = sqlTmp.replace("and obj.receiptDetailsType (1?)", "");
					sqlTmp = sqlTmp.replace("(2?)", "22"); // transaction purpose
					if (bookType == 1) {
						openingBalanceDebit += getOpeningBalance(entityManager, sqlTmp, 1, false);
					} else if (bookType == 2) {
						openingBalanceCredit += getOpeningBalance(entityManager, sqlTmp, 1, true);
					}
					break;
				case 23:// Deposit Cash In Bank always results in debit amount to concerned branch
					sqlTmp = transactionSQL;
					sqlTmp = sqlTmp.replace("(3?)", "obj.netAmount");
					sqlTmp = sqlTmp.replace("and obj.receiptDetailsType (1?)", "");
					sqlTmp = sqlTmp.replace("(2?)", "23"); // transaction purpose

					if (bookType == 1) {
						openingBalanceCredit += getOpeningBalance(entityManager, sqlTmp, 1, true);
					} else if (bookType == 2) {
						openingBalanceDebit += getOpeningBalance(entityManager, sqlTmp, 1, false);
					}
					break;
				case 24:
					/*
					 * Transfer Funds From One Bank To Another has debit effect one one bank and
					 * credit effect to another bank
					 */
					if (bookType == 2) {
						sqlTmp = transactionSQL;
						sqlTmp = sqlTmp.replace("(3?)", "obj.netAmount");
						sqlTmp = sqlTmp.replace("and obj.receiptDetailsType (1?)", "");
						sqlTmp = sqlTmp.replace("(2?)", "24"); // transaction purpose
						Double balanceTmp = getOpeningBalance(entityManager, sqlTmp, 1, true); // check in to bank
						if (balanceTmp == 0.0) {
							/*
							 * Check whether record present for "to Bank"
							 */
							sqlTmp = sqlTmp.replace("transactionBranchBankAccount", "transactionToBranchBankAccount");
							openingBalanceDebit += getOpeningBalance(entityManager, sqlTmp, 1, false);
						} else {
							openingBalanceCredit += balanceTmp;
						}
					}
					break;
			}
		}
		openingBalance = startBalance + openingBalanceCredit - openingBalanceDebit;
		return openingBalance;
	}

	@Override
	public String exportCashAndBankBook(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction, String path, Application application) {
		String fileName = null;
		try {
			log.log(Level.FINE, "***** Start " + json);
			ArrayNode cashNBankan = result.putArray("exportData");
			String bnchCashNBank = json.findValue("bnchCashNBank") != null ? json.findValue("bnchCashNBank").asText()
					: null;
			String bnkCashNBank = json.findValue("bnkCashNBank") != null ? json.findValue("bnkCashNBank").asText()
					: null;
			String fmDate = json.findValue("fmDate") != null ? json.findValue("fmDate").asText() : null;
			String tDate = json.findValue("tDate") != null ? json.findValue("tDate").asText() : null;
			int bookType = json.findValue("bookType") != null ? json.findValue("bookType").asInt() : 0;
			Branch branch = null;
			BranchBankAccounts bnchBankAccount = null;
			String fromDate = null;
			String toDate = null;
			String reportTemplateName = null;
			String bookTitle = null;
			String outReportName = null;
			Map<String, Object> criterias = new HashMap<String, Object>();
			List<TransactionCreatedDateComparator> sortedCashNBankData = Collections.emptyList();
			if ((bnchCashNBank == null || bnchCashNBank.equals("")) && (bnkCashNBank == null || bnkCashNBank.equals(""))
					&& (fmDate == null || fmDate.equals("")) && (tDate == null || tDate.equals(""))) {
				// Organization Cash Wise Breakups for income expense for the last one year for
				// the head office branch
				fromDate = DateUtil.returnOneMonthBackDate();
				toDate = IdosConstants.MYSQLDF.format(Calendar.getInstance().getTime());
				criterias.clear();
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("isHeadQuarter", 1);
				criterias.put("presentStatus", 1);
				branch = genericDao.getByCriteria(Branch.class, criterias, entityManager);
				if (bookType == 1) {
					// sortedCashNBankData = getCreditDebitCashTransaction(branch, cashNBankan,
					// user, fromDate, toDate, entityManager);
					sortedCashNBankData = getCreditDebitTransaction(branch, bnchBankAccount, cashNBankan, user,
							fromDate, toDate, entityManager, bookType);
					reportTemplateName = "cashBookReport";
					outReportName = reportTemplateName;
					bookTitle = "CASH BOOK DETAILS";
				} else if (bookType == 2) {
					// sortedCashNBankData = getCreditDebitBankTransaction(branch, bnchBankAccount,
					// cashNBankan, user, fromDate, toDate, entityManager);
					sortedCashNBankData = getCreditDebitTransaction(branch, bnchBankAccount, cashNBankan, user,
							fromDate, toDate, entityManager, bookType);
					reportTemplateName = "bankBookReport";
				} else if (bookType == 3) {
					sortedCashNBankData = getCreditDebitTransaction(branch, bnchBankAccount, cashNBankan, user,
							fromDate, toDate, entityManager, bookType);
					reportTemplateName = "cashBookReport";
					outReportName = "pettyCashBookReport";
					bookTitle = "PETTY CASH BOOK DETAILS";
				}
				log.log(Level.FINE, "******* Start22 bookType =" + bookType + "  " + outReportName);
			} else {
				// CashNBank Wise Breakups for income expense for the last one or choosen date
				// for the selected branch or bank
				fromDate = IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(fmDate));
				toDate = IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(tDate));
				branch = Branch.findById(Long.parseLong(bnchCashNBank));
				if (bnkCashNBank != null && !bnkCashNBank.equals("")) {
					bnchBankAccount = BranchBankAccounts.findById(Long.parseLong(bnkCashNBank));
				}
				if (bookType == 1) {
					// sortedCashNBankData = getCreditDebitCashTransaction(branch, cashNBankan,
					// user, fromDate, toDate, entityManager);
					sortedCashNBankData = getCreditDebitTransaction(branch, null, cashNBankan, user, fromDate, toDate,
							entityManager, bookType);
					reportTemplateName = "cashBookReport";
					outReportName = reportTemplateName;
					if (user.getOrganization().getName() != null) {
						bookTitle = "CASH BOOK DETAIL";
					}
				} else if (bookType == 2) {
					// sortedCashNBankData = getCreditDebitBankTransaction(branch, bnchBankAccount,
					// cashNBankan, user, fromDate, toDate, entityManager);
					sortedCashNBankData = getCreditDebitTransaction(branch, bnchBankAccount, cashNBankan, user,
							fromDate, toDate, entityManager, bookType);
					reportTemplateName = "bankBookReport";
					outReportName = reportTemplateName;
					if (user.getOrganization().getName() != null) {
						bookTitle = "BANK BOOK DETAIL";
					}
				} else if (bookType == 3) {
					sortedCashNBankData = getCreditDebitTransaction(branch, bnchBankAccount, cashNBankan, user,
							fromDate, toDate, entityManager, bookType);
					reportTemplateName = "cashBookReport";
					outReportName = "pettyCashBookReport";
					if (user.getOrganization().getName() != null) {
						bookTitle = "PETTYCASH BOOK DETAIL";
					}
				}

				log.log(Level.FINE, "******* Start33 " + outReportName);
			}
			String exporttype = json.findValue("exporttype") != null ? json.findValue("exporttype").asText() : null;
			if (exporttype != null && !exporttype.equals("")) {
				String branchName = "", bankName = "";
				if (null != branch) {
					branchName = branch.getName();
				}
				if (null != bnchBankAccount) {
					bankName = bnchBankAccount.getBankName();
				}
				String booktyp = String.valueOf(bookType);
				Double openingBalanceForTheStatement = openingBalanceForThisCriteria(branch, bnchBankAccount, fromDate,
						toDate, entityManager, booktyp);
				if (exporttype.equals("xlsx")) {
					fileName = exportCashNBankBook(sortedCashNBankData, user, branchName, bankName, fmDate, tDate,
							exporttype, openingBalanceForTheStatement, reportTemplateName, bookTitle, outReportName,
							path, application);
				} else if (exporttype.equals("pdf")) {
					fileName = exportCashNBankBook(sortedCashNBankData, user, branchName, bankName, fmDate, tDate,
							exporttype, openingBalanceForTheStatement, reportTemplateName, bookTitle, outReportName,
							path, application);
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
		}
		return fileName;
	}

	private String exportCashNBankBook(List<TransactionCreatedDateComparator> sortedCashNBankData, Users user,
			String branchName, String bankName, String from, String to, String type,
			Double openingBalanceForTheStatement, String reportTemplateName, String bookTitle, String outReportName,
			String path, Application application) {
		log.log(Level.FINE, "******* Start " + outReportName + "  " + sortedCashNBankData);
		StringBuilder fileName = new StringBuilder(outReportName);
		try {
			Long timeInMillis = Calendar.getInstance().getTimeInMillis();
			fileName.append(timeInMillis);
			if ("xlsx".equalsIgnoreCase(type)) {
				fileName.append(".xlsx");
			} else if ("pdf".equalsIgnoreCase(type)) {
				fileName.append(".pdf");
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Map<String, Object> params = getParams(branchName, bankName, from, to, openingBalanceForTheStatement,
					bookTitle);
			IdosUtil.seOrganization4Report(params, user.getOrganization());
			Double balance = openingBalanceForTheStatement;
			for (TransactionCreatedDateComparator txn : sortedCashNBankData) {
				if (txn.getCredit() != null) {
					balance -= txn.getCredit();
					txn.setBalance(balance);
				}
				if (txn.getDebit() != null) {
					balance += txn.getDebit();
					txn.setBalance(balance);
				}
			}
			out = dynReportService.generateStaticReportOld(reportTemplateName, sortedCashNBankData, params, type, null,
					application);
			path = path.concat(fileName.toString());
			File file = new File(path);
			if (file.exists()) {
				file.delete();
			}
			FileOutputStream fileOut = new FileOutputStream(path);
			out.writeTo(fileOut);
			fileOut.close();
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = StaticController.getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return fileName.toString();
	}

	private Map<String, Object> getParams(final String branchName, final String bankName, final String fromDate,
			final String toDate, final Double openingBalanceForTheStatement, String bookTitle) {
		Map<String, Object> params = new HashMap<String, Object>();
		try {
			if (null != branchName) {
				params.put("branchName", branchName);
			} else {
				params.put("branchName", "");
			}
			if (null != bankName) {
				params.put("bankName", bankName);
			} else {
				params.put("bankName", "");
			}
			if (null != fromDate && !fromDate.equals("")) {
				params.put("fromDate", IdosConstants.IDOSDF.parse(fromDate));
			} else {
				params.put("fromDate", IdosConstants.IDOSDF.parse(
						IdosConstants.IDOSDF.format((IdosConstants.MYSQLDF.parse(DateUtil.returnOneMonthBackDate())))));
			}
			if (null != toDate && !toDate.equals("")) {
				params.put("toDate", IdosConstants.IDOSDF.parse(toDate));
			} else {
				params.put("toDate",
						IdosConstants.IDOSDF.parse(IdosConstants.IDOSDF.format(Calendar.getInstance().getTime())));
			}
			if (null != openingBalanceForTheStatement) {
				params.put("openingBalanceForTheStatement", openingBalanceForTheStatement);
			} else {
				params.put("openingBalanceForTheStatement", 0.0);
			}
			if (bookTitle != null) {
				params.put("bookTitle", bookTitle);
			} else {
				params.put("bookTitle", "");
			}
		} catch (ParseException e) {
			params.put("fromDate", null);
			params.put("toDate", null);
		}
		return params;
	}

	private void getProvisionJournalEntryCreditDebit(EntityManager entityManager,
			ArrayList<TransactionCreatedDateComparator> txnCashBankListUnsorted, String sqlStr, boolean isCredit,
			String bookType, Long bankId) {
		List<IdosProvisionJournalEntry> provJourTxn = genericDao.executeSimpleQuery(sqlStr, entityManager);
		if (provJourTxn.size() > 0) {
			for (IdosProvisionJournalEntry provisionJournalEntry : provJourTxn) {
				List<ProvisionJournalEntryDetail> pjEntryDetailList = provisionJournalEntry
						.getProvisionJournalEntryDetails();
				String itemName = "";
				Map<String, Object> criterias = new HashMap<String, Object>();
				Double debitAmount = 0.0;
				Double creditAmount = 0.0;
				StringBuilder debitDesc = new StringBuilder("");
				StringBuilder creditDesc = new StringBuilder("");
				boolean checkFlag = false;
				for (ProvisionJournalEntryDetail pjEntryDetail : pjEntryDetailList) {
					if (IdosConstants.HEAD_CASH.equals(pjEntryDetail.getHeadType())
							&& IdosConstants.HEAD_CASH.equals(bookType)) { // cash
						criterias.clear();
						criterias.put("id", pjEntryDetail.getHeadID());
						criterias.put("presentStatus", 1);
						// criterias.put("headType", IdosConstants.HEAD_CASH);
						// List<BranchCashCount> branchCashCountList =
						// genericDao.findByCriteria(BranchCashCount.class, criterias, entityManager);
						List<BranchDepositBoxKey> branchCashCountList = genericDao
								.findByCriteria(BranchDepositBoxKey.class, criterias, entityManager);
						BranchDepositBoxKey branchCashCount = branchCashCountList.get(0);
						if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjEntryDetail.getIsDebit() && !isCredit) {
							itemName += "Debit:" + branchCashCount.getBranch().getName() + " Cash,";
							debitAmount += pjEntryDetail.getHeadAmount();
							checkFlag = true;
						} else if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjEntryDetail.getIsDebit()
								&& isCredit) {
							itemName += "Credit:" + branchCashCount.getBranch().getName() + " Cash,";
							creditAmount += pjEntryDetail.getHeadAmount();
							checkFlag = true;
						}
					} else if (IdosConstants.HEAD_BANK.equals(pjEntryDetail.getHeadType())
							&& IdosConstants.HEAD_BANK.equals(bookType)) { // Bank
						criterias.clear();
						// criterias.put("id", pjEntryDetail.getHeadID());
						criterias.put("id", bankId);
						criterias.put("presentStatus", 1);
						// criterias.put("headType", IdosConstants.HEAD_BANK);
						List<BranchBankAccounts> branchBankAccountsList = genericDao
								.findByCriteria(BranchBankAccounts.class, criterias, entityManager);
						BranchBankAccounts branchBankAccounts = branchBankAccountsList.get(0);
						if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjEntryDetail.getIsDebit() && !isCredit
								&& bankId.equals(pjEntryDetail.getHeadID())) {
							itemName += "Debit: " + branchBankAccounts.getBranch().getName()
									+ branchBankAccounts.getBankName() + ",";
							debitAmount += pjEntryDetail.getHeadAmount();
							checkFlag = true;
						} else if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjEntryDetail.getIsDebit()
								&& isCredit && bankId.equals(pjEntryDetail.getHeadID())) {
							itemName += "Credit: " + branchBankAccounts.getBranch().getName()
									+ branchBankAccounts.getBankName() + ",";
							creditAmount += pjEntryDetail.getHeadAmount();
							checkFlag = true;
						}
					} else if (IdosConstants.HEAD_PETTY.equals(pjEntryDetail.getHeadType())
							&& IdosConstants.HEAD_PETTY.equals(bookType)) { // petty cash currently not getting used
						criterias.clear();
						criterias.put("id", pjEntryDetail.getHeadID());
						criterias.put("presentStatus", 1);
						// criterias.put("headType", IdosConstants.HEAD_PETTY);
						List<BranchDepositBoxKey> branchCashCountList = genericDao
								.findByCriteria(BranchDepositBoxKey.class, criterias, entityManager);
						BranchDepositBoxKey branchCashCount = branchCashCountList.get(0);
						if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjEntryDetail.getIsDebit() && !isCredit) {
							itemName += "Debit:" + branchCashCount.getBranch().getName() + " Pettycash,";
							debitAmount += pjEntryDetail.getHeadAmount();
							checkFlag = true;
						} else if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjEntryDetail.getIsDebit()
								&& isCredit) {
							itemName += "Credit:" + branchCashCount.getBranch().getName() + " Pettycash,";
							creditAmount += pjEntryDetail.getHeadAmount();
							checkFlag = true;
						}
					}

					if (!pjEntryDetail.getHeadType().equals(bookType)) {
						trialBalanceService.getProvisionJournalEntryHeads(entityManager, provisionJournalEntry,
								pjEntryDetail.getHeadType(), pjEntryDetail.getHeadID(), debitDesc, creditDesc);
					}
				}
				if (checkFlag) {
					TransactionCreatedDateComparator newObj = new TransactionCreatedDateComparator();
					if (isCredit) {
						newObj.setIncomeExpense("Expense:" + itemName);
						newObj.setCredit(creditAmount);
					} else {
						newObj.setIncomeExpense("Income:" + itemName);
						newObj.setDebit(debitAmount);
					}
					newObj.setCreatedDate(provisionJournalEntry.getTransactionDate());
					newObj.setTxnRefNumber(provisionJournalEntry.getTransactionRefNumber());
					newObj.setInstrumentDate(provisionJournalEntry.getInstrumentDate());
					newObj.setInstrumentNumber(provisionJournalEntry.getInstrumentNumber());
					newObj.setBrsBankDate(provisionJournalEntry.getBrsBankDate());
					newObj.setTransactionPurpose(provisionJournalEntry.getTransactionPurpose().getTransactionPurpose());
					newObj.setLedgerCustVend("Debit:" + debitDesc.toString() + " | Credit:" + creditDesc.toString());
					txnCashBankListUnsorted.add(newObj);
				}
			}
		}
	}

	private String getTransLedgerCustVend(Transaction txn, int bookType, Long bnchCashNBank) {
		// Long
		// bnchCashNBank=json.findValue("bnchCashNBank")!=null?json.findValue("bnchCashNBank").asLong():null;
		// int bookType = json.findValue("bookType") != null ?
		// json.findValue("bookType").asInt() : 0;
		String ledgerString = "";
		if (txn.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW ||
				txn.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER ||
				txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY ||
				txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CREDIT_PAY_LATER ||
				txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT ||
				txn.getTransactionPurpose().getId() == IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER ||
				txn.getTransactionPurpose().getId() == IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER ||
				txn.getTransactionPurpose().getId() == IdosConstants.PAY_VENDOR_SUPPLIER ||
				txn.getTransactionPurpose().getId() == IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER ||
				txn.getTransactionPurpose().getId() == IdosConstants.REFUND_ADVANCE_RECEIVED ||
				txn.getTransactionPurpose().getId() == IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE) {
			Vendor transactionVendorCustomer = txn.getTransactionVendorCustomer();
			if (transactionVendorCustomer != null && transactionVendorCustomer.getName() != null) {
				ledgerString = transactionVendorCustomer.getName();
			} else {
				if (txn.getTransactionUnavailableVendorCustomer() != null) {
					ledgerString = txn.getTransactionUnavailableVendorCustomer();
				}
			}
		} else if (txn.getTransactionPurpose().getId() == IdosConstants.TRANSFER_MAIN_CASH_TO_PETTY_CASH) {
			if (bookType == 1) {
				ledgerString = txn.getTransactionBranch().getName() + "(PETTY CASH)";
			} else if (bookType == 3) {
				ledgerString = txn.getTransactionBranch().getName() + "(CASH)";
			}
		} else if (txn.getTransactionPurpose().getId() == IdosConstants.WITHDRAW_CASH_FROM_BANK) {
			if (bookType == 1) {
				ledgerString = txn.getTransactionBranchBankAccount().getBankName();
			} else if (bookType == 2) {
				ledgerString = txn.getTransactionBranch().getName() + "(CASH)";
			}
		} else if (txn.getTransactionPurpose().getId() == IdosConstants.DEPOSIT_CASH_IN_BANK) {
			if (bookType == 1) {
				ledgerString = txn.getTransactionBranchBankAccount().getBankName();
			} else if (bookType == 2) {
				ledgerString = txn.getTransactionBranch().getName() + "(CASH)";
			}
		} else if (txn.getTransactionPurpose().getId() == IdosConstants.TRANSFER_FUNDS_FROM_ONE_BANK_TO_ANOTHER) {
			if (bnchCashNBank != null) {
				if (txn.getTransactionBranch().getId() == bnchCashNBank) {
					ledgerString = txn.getTransactionToBranchBankAccount().getBankName();
				} else {
					ledgerString = txn.getTransactionBranchBankAccount().getBankName();
				}
			}
		}
		return ledgerString;
	}

}