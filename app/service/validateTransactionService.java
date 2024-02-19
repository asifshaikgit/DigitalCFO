package service;

import javax.persistence.EntityTransaction;

import model.BranchCashCount;
import model.Users;

public interface validateTransactionService extends BaseService{
	public Boolean validateMainCashToPettyTxn(Users user,EntityTransaction transaction,BranchCashCount branchCashCount,String resultantCashBranchCashAccount,String cashMainActToPettyAccount,String cashTransferSuppDocs);
}
