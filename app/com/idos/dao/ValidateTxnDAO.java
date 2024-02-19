package com.idos.dao;

import javax.persistence.EntityTransaction;

import model.BranchCashCount;
import model.Users;

public interface ValidateTxnDAO extends BaseDAO {
	public Boolean validateMainCashToPettyTxn(Users user,EntityTransaction transaction,BranchCashCount branchCashCount,String resultantCashBranchCashAccount,String cashMainActToPettyAccount,String cashTransferSuppDocs);
}
