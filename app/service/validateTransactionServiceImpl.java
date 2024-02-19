package service;

import javax.persistence.EntityTransaction;

import model.BranchCashCount;
import model.Users;
import java.util.logging.Level;

public class validateTransactionServiceImpl implements validateTransactionService {
	
	@Override
	public Boolean validateMainCashToPettyTxn(Users user,EntityTransaction transaction,
			BranchCashCount branchCashCount,String resultantCashBranchCashAccount,
			String cashMainActToPettyAccount,String cashTransferSuppDocs) {
		log.log(Level.FINE, "============ Start");
		Boolean validationError=valdateTxnDao.validateMainCashToPettyTxn(user,transaction,branchCashCount, resultantCashBranchCashAccount, cashMainActToPettyAccount,cashTransferSuppDocs);
		return validationError;
	}
}
