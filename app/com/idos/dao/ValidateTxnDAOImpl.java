package com.idos.dao;

import javax.persistence.EntityTransaction;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import model.BranchCashCount;
import model.Users;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import java.util.logging.Level;

public class ValidateTxnDAOImpl implements ValidateTxnDAO {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Override
	public Boolean validateMainCashToPettyTxn(Users user, EntityTransaction transaction,
			BranchCashCount branchCashCount, String resultantCashBranchCashAccount,
			String cashMainActToPettyAccount, String cashTransferSuppDocs) {
		Boolean validationError = false;
		if (branchCashCount != null) {
			Double resCashBranchCashAmount = Double.parseDouble(resultantCashBranchCashAccount);
			Double cashMainToPettyAmt = Double.parseDouble(cashMainActToPettyAccount);
			if (!(branchCashCount.getResultantCash().compareTo(resCashBranchCashAmount) == 0)) {
				validationError = true;
			} else {
				branchCashCount.setResultantCash(branchCashCount.getResultantCash() - cashMainToPettyAmt);
				if (branchCashCount.getTotalMainCashToPettyCash() != null) {
					branchCashCount.setTotalMainCashToPettyCash(
							branchCashCount.getTotalMainCashToPettyCash() + cashMainToPettyAmt);
				} else {
					branchCashCount.setTotalMainCashToPettyCash(cashMainToPettyAmt);
				}
				if (branchCashCount.getResultantPettyCash() != null) {
					branchCashCount.setResultantPettyCash(branchCashCount.getResultantPettyCash() + cashMainToPettyAmt);
				} else {
					branchCashCount.setResultantPettyCash(cashMainToPettyAmt);
				}
				if (cashTransferSuppDocs != null && !cashTransferSuppDocs.equals("")) {
					branchCashCount.setDocUploadedForPettyCashTransfer(cashTransferSuppDocs);
				}
				genericDao.saveOrUpdate(branchCashCount, user, entityManager);
				transaction.commit();
			}
		}
		return validationError;
	}

}
