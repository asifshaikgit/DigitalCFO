package com.idos.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityTransaction;

import java.util.logging.Logger;
import java.util.logging.Level;
import org.jfree.util.Log;

import controllers.StaticController;

import model.BranchBankAccountBalance;
import model.BranchBankAccounts;
import model.BranchCashCount;
import model.Organization;
import model.Transaction;
import model.Users;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

public class CashierDAOImpl implements CashierDAO {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Override
	public BranchCashCount recoincileBranchCashAccount(Users user, EntityTransaction transaction) {
		BranchCashCount newBranchCashCount = new BranchCashCount();
		try {
			StringBuilder newsbquery = new StringBuilder("");
			newsbquery.append("select obj from BranchCashCount obj WHERE obj.branch.id='" + user.getBranch().getId()
					+ "' AND obj.organization.id='" + user.getOrganization().getId()
					+ "' and obj.presentStatus=1 ORDER BY obj.date desc");
			List<BranchCashCount> prevBranchCashCount = genericDao.executeSimpleQueryWithLimit(newsbquery.toString(),
					entityManager, 1);
			Organization org = user.getOrganization();
			String finStartDate = null;
			String finStDt = null;
			StringBuilder startYear = null;
			String finEndDate = null;
			String finEndDt = null;
			StringBuilder endYear = null;
			int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
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
			if (org.getFinancialEndDate() != null && !org.getFinancialEndDate().equals("")) {
				finEndDt = StaticController.idosmdtdf.format(org.getFinancialEndDate()) + "," + endYear;
				finEndDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
			} else {
				finEndDt = "Mar 31" + "," + endYear;
				finEndDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
			}
			Double lastRecordedCashCountNotesTotal = 0.0;
			Double lastRecordedCashCountCoinsTotal = 0.0;
			Double lastRecordedCashCountSmallerCoinsTotal = 0.0;
			Double lastRecordedGrandTotal = 0.0;
			Double totalCashCredited = 0.0;
			Double totalCashDebited = 0.0;
			Double resultantCash = 0.0;
			Double totalMainToPettyCashAccount = 0.0;
			Double debittedPettyCashAmount = 0.0;
			Double resultantPettyCashAmount = 0.0;
			totalCashCredited = getTotalSellTxnForFinYear(user, finStartDate, finEndDate);
			totalCashDebited = getTotalPurchaseTxnForFinYear(user, finStartDate, finEndDate);
			debittedPettyCashAmount = getTotalPurchaseFromPettyCashAccount(user, finStartDate, finEndDate);
			if (prevBranchCashCount.size() > 0) {
				BranchCashCount bnchCashCount = prevBranchCashCount.get(0);
				if (bnchCashCount.getDate().compareTo(StaticController.mysqldf.parse(finStartDate)) >= 0
						&& bnchCashCount.getDate().compareTo(StaticController.mysqldf.parse(finEndDate)) <= 0) {
					if (bnchCashCount.getNotesTotal() != null) {
						lastRecordedCashCountNotesTotal = bnchCashCount.getNotesTotal();
					}
					if (bnchCashCount.getCoinsTotal() != null) {
						lastRecordedCashCountCoinsTotal = bnchCashCount.getCoinsTotal();
					}
					if (bnchCashCount.getSmallerCoinsTotal() != null) {
						lastRecordedCashCountSmallerCoinsTotal = bnchCashCount.getSmallerCoinsTotal();
					}
					if (bnchCashCount.getGrandTotal() != null) {
						lastRecordedGrandTotal = bnchCashCount.getGrandTotal();
					}
					if (bnchCashCount.getTotalMainCashToPettyCash() != null) {
						totalMainToPettyCashAccount = bnchCashCount.getTotalMainCashToPettyCash();
					}
					newBranchCashCount.setSupportingDocument(bnchCashCount.getSupportingDocument());
					newBranchCashCount
							.setDocUploadedForPettyCashTransfer(bnchCashCount.getDocUploadedForPettyCashTransfer());
				}
			}
			resultantCash = Double.valueOf(StaticController.decimalFormat.format(
					lastRecordedGrandTotal + totalCashCredited - totalCashDebited - totalMainToPettyCashAccount));
			resultantPettyCashAmount = Double.valueOf(
					StaticController.decimalFormat.format(totalMainToPettyCashAccount - debittedPettyCashAmount));
			newBranchCashCount = new BranchCashCount();
			newBranchCashCount.setNotesTotal(lastRecordedCashCountNotesTotal);
			newBranchCashCount.setCoinsTotal(lastRecordedCashCountCoinsTotal);
			newBranchCashCount.setSmallerCoinsTotal(lastRecordedCashCountSmallerCoinsTotal);
			newBranchCashCount.setGrandTotal(lastRecordedGrandTotal);
			newBranchCashCount.setTotalMainCashToPettyCash(totalMainToPettyCashAccount);
			newBranchCashCount.setDebittedPettyCashAmount(debittedPettyCashAmount);
			newBranchCashCount.setResultantPettyCash(resultantPettyCashAmount);
			newBranchCashCount.setResultantCash(resultantCash);
			newBranchCashCount.setCreditAmount(totalCashCredited);
			newBranchCashCount.setDebitAmount(totalCashDebited);
			newBranchCashCount.setDate(Calendar.getInstance().getTime());
			newBranchCashCount.setBranch(user.getBranch());
			newBranchCashCount.setOrganization(user.getOrganization());
			genericDao.saveOrUpdate(newBranchCashCount, user, entityManager);
			transaction.commit();
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
		}
		return newBranchCashCount;
	}

	public Double getTotalSellTxnForFinYear(Users user, String finStartDate, String finEndDate) {
		Double totalSellTxnAmount = 0.0;
		StringBuilder cssbquery = new StringBuilder("");
		cssbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch='"
				+ user.getBranch().getId() + "' AND obj.transactionBranchOrganization='"
				+ user.getOrganization().getId()
				+ "' AND (obj.transactionPurpose=1 or obj.transactionPurpose=5 or obj.transactionPurpose=6 or obj.transactionPurpose=9) and obj.receiptDetailsType=1 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
				+ finStartDate + "' and '" + finEndDate + "'");
		List<Transaction> cashselltxn = genericDao.executeSimpleQuery(cssbquery.toString(), entityManager);
		if (cashselltxn.size() > 0) {
			Object val = cashselltxn.get(0);
			if (val != null) {
				totalSellTxnAmount = Double
						.valueOf(StaticController.decimalFormat.format(Double.parseDouble(val.toString())));
			}
		}
		return totalSellTxnAmount;
	}

	public Double getTotalBankSellTxnForFinYear(Users user, String finStartDate, String finEndDate,
			BranchBankAccounts branchBankAccount) {
		Double totalSellTxnAmount = 0.0;
		StringBuilder cssbquery = new StringBuilder("");
		cssbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch='"
				+ user.getBranch().getId() + "' AND obj.transactionBranchOrganization='"
				+ user.getOrganization().getId()
				+ "' AND (obj.transactionPurpose=1 or obj.transactionPurpose=5 or obj.transactionPurpose=6 or obj.transactionPurpose=9) and obj.receiptDetailsType=2 and obj.transactionBranchBankAccount='"
				+ branchBankAccount.getId()
				+ "' and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
				+ finStartDate + "' and '" + finEndDate + "'");
		List<Transaction> cashselltxn = genericDao.executeSimpleQuery(cssbquery.toString(), entityManager);
		if (cashselltxn.size() > 0) {
			Object val = cashselltxn.get(0);
			if (val != null) {
				totalSellTxnAmount = Double
						.valueOf(StaticController.decimalFormat.format(Double.parseDouble(val.toString())));
			}
		}
		log.log(Level.INFO, "Info " + totalSellTxnAmount);
		return totalSellTxnAmount;
	}

	public Double getTotalPurchaseTxnForFinYear(Users user, String finStartDate, String finEndDate) {
		Double totalPurchaseTxnAmount = 0.0;
		StringBuilder sbquery = new StringBuilder("");
		sbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch='"
				+ user.getBranch().getId() + "' AND obj.transactionBranchOrganization='"
				+ user.getOrganization().getId()
				+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=7 or obj.transactionPurpose=8 or obj.transactionPurpose=10) and obj.receiptDetailsType=1 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
				+ finStartDate + "' and '" + finEndDate + "'");
		List<Transaction> cashexpensetxn = genericDao.executeSimpleQuery(sbquery.toString(), entityManager);
		if (cashexpensetxn.size() > 0) {
			Object val = cashexpensetxn.get(0);
			if (val != null) {
				totalPurchaseTxnAmount = Double
						.valueOf(StaticController.decimalFormat.format(Double.parseDouble(String.valueOf(val))));
			}
		}
		return totalPurchaseTxnAmount;
	}

	public Double getTotalBankPurchaseTxnForFinYear(Users user, String finStartDate, String finEndDate,
			BranchBankAccounts branchBankAccount) {
		Double totalPurchaseTxnAmount = 0.0;
		StringBuilder sbquery = new StringBuilder("");
		sbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch='"
				+ user.getBranch().getId() + "' AND obj.transactionBranchOrganization='"
				+ user.getOrganization().getId()
				+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=7 or obj.transactionPurpose=8 or obj.transactionPurpose=10) and obj.receiptDetailsType=2 and obj.transactionBranchBankAccount='"
				+ branchBankAccount.getId()
				+ "' and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
				+ finStartDate + "' and '" + finEndDate + "'");
		List<Transaction> cashexpensetxn = genericDao.executeSimpleQuery(sbquery.toString(), entityManager);
		if (cashexpensetxn.size() > 0) {
			Object val = cashexpensetxn.get(0);
			if (val != null) {
				totalPurchaseTxnAmount = Double
						.valueOf(StaticController.decimalFormat.format(Double.parseDouble(String.valueOf(val))));
			}
		}
		return totalPurchaseTxnAmount;
	}

	public Double getTotalPurchaseFromPettyCashAccount(Users user, String finStartDate, String finEndDate) {
		Double totalPurchaseTxnFromPettyCashAccount = 0.0;
		StringBuilder sbquery = new StringBuilder("");
		sbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch='"
				+ user.getBranch().getId() + "' AND obj.transactionBranchOrganization='"
				+ user.getOrganization().getId()
				+ "' AND (obj.transactionPurpose=11) and obj.receiptDetailsType=1 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
				+ finStartDate + "' and '" + finEndDate + "'");
		List<Transaction> cashexpensetxn = genericDao.executeSimpleQuery(sbquery.toString(), entityManager);
		if (cashexpensetxn.size() > 0) {
			Object val = cashexpensetxn.get(0);
			if (val != null) {
				totalPurchaseTxnFromPettyCashAccount = Double
						.valueOf(StaticController.decimalFormat.format(Double.parseDouble(String.valueOf(val))));
			}
		}
		return totalPurchaseTxnFromPettyCashAccount;
	}

	@Override
	public BranchBankAccountBalance recoincileBranchBankAccountBalance(Users user, BranchBankAccounts branchBankAccount,
			EntityTransaction transaction) {
		BranchBankAccountBalance newBranchBankAccountBalance = new BranchBankAccountBalance();
		try {
			Organization org = user.getOrganization();
			String finStartDate = null;
			String finStDt = null;
			StringBuilder startYear = null;
			String finEndDate = null;
			String finEndDt = null;
			StringBuilder endYear = null;
			int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
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
			if (org.getFinancialEndDate() != null && !org.getFinancialEndDate().equals("")) {
				finEndDt = StaticController.idosmdtdf.format(org.getFinancialEndDate()) + "," + endYear;
				finEndDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
			} else {
				finEndDt = "Mar 31" + "," + endYear;
				finEndDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
			}
			StringBuilder newsbquery = new StringBuilder("");
			newsbquery.append("select obj from BranchBankAccountBalance obj WHERE obj.branch.id='"
					+ user.getBranch().getId() + "' AND obj.organization.id='" + user.getOrganization().getId()
					+ "' and obj.branchBankAccounts.id='" + branchBankAccount.getId()
					+ "' and obj.presentStatus=1 ORDER BY obj.date desc");
			List<BranchBankAccountBalance> branchBankAccountBal = genericDao
					.executeSimpleQueryWithLimit(newsbquery.toString(), entityManager, 1);
			Double lastRecordedAccountBalance = 0.0;
			Double totalCredit = 0.0;
			Double totalDebit = 0.0;
			Double resultantBalance = 0.0;
			totalCredit = getTotalBankSellTxnForFinYear(user, finStartDate, finEndDate, branchBankAccount);
			totalDebit = getTotalBankPurchaseTxnForFinYear(user, finStartDate, finEndDate, branchBankAccount);
			if (branchBankAccountBal.size() > 0) {
				BranchBankAccountBalance branchBankAccountBalance = branchBankAccountBal.get(0);
				if (branchBankAccountBalance.getDate().compareTo(StaticController.mysqldf.parse(finStartDate)) >= 0
						&& branchBankAccountBalance.getDate()
								.compareTo(StaticController.mysqldf.parse(finEndDate)) <= 0) {
					if (branchBankAccountBalance.getAmountBalance() != null) {
						lastRecordedAccountBalance = branchBankAccountBalance.getAmountBalance();
					}
					newBranchBankAccountBalance.setBalanceStatement(branchBankAccountBalance.getBalanceStatement());
				}
			}
			resultantBalance = lastRecordedAccountBalance + totalCredit - totalDebit;
			newBranchBankAccountBalance.setAmountBalance(lastRecordedAccountBalance);
			newBranchBankAccountBalance.setCreditAmount(totalCredit);
			newBranchBankAccountBalance.setDebitAmount(totalDebit);
			newBranchBankAccountBalance.setResultantCash(resultantBalance);
			newBranchBankAccountBalance.setDate(Calendar.getInstance().getTime());
			newBranchBankAccountBalance.setBranchBankAccounts(branchBankAccount);
			newBranchBankAccountBalance.setBranch(user.getBranch());
			newBranchBankAccountBalance.setOrganization(user.getOrganization());
			genericDao.saveOrUpdate(newBranchBankAccountBalance, user, entityManager);
			transaction.commit();
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
		}
		return newBranchBankAccountBalance;
	}
}
