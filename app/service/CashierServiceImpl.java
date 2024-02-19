package service;

import javax.persistence.EntityTransaction;

import java.util.logging.Logger;
import java.util.logging.Level;

import model.BranchBankAccountBalance;
import model.BranchBankAccounts;
import model.BranchCashCount;
import model.Users;

import com.idos.dao.CashierDAO;
import com.idos.dao.CashierDAOImpl;

public class CashierServiceImpl implements CashierService {
	

	@Override
	public BranchCashCount recoincileBranchCashAccount(Users user,
			EntityTransaction transaction) {
		log.log(Level.FINE, ">>>> Start");
		BranchCashCount branchCashCount=cashierDao.recoincileBranchCashAccount(user, transaction);
		return branchCashCount;
	}

	@Override
	public BranchBankAccountBalance recoincileBranchBankAccountBalance(Users user,
			BranchBankAccounts branchBankAccount, EntityTransaction transaction) {
		log.log(Level.FINE, ">>>> Start");
		BranchBankAccountBalance branchBankAccountBalance=cashierDao.recoincileBranchBankAccountBalance(user,branchBankAccount,transaction);
		return branchBankAccountBalance;
	}

}
