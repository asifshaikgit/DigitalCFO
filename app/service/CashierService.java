package service;

import javax.persistence.EntityTransaction;

import model.BranchBankAccountBalance;
import model.BranchBankAccounts;
import model.BranchCashCount;
import model.Users;

public interface CashierService extends BaseService{
	public BranchCashCount recoincileBranchCashAccount(Users user,EntityTransaction transaction);
	public BranchBankAccountBalance recoincileBranchBankAccountBalance(Users user,BranchBankAccounts branchBankAccount,EntityTransaction transaction);
}
