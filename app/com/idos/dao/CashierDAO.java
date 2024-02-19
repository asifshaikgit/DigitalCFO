package com.idos.dao;

import javax.persistence.EntityTransaction;

import model.BranchBankAccountBalance;
import model.BranchBankAccounts;
import model.BranchCashCount;
import model.Users;

public interface CashierDAO extends BaseDAO{
	public BranchCashCount recoincileBranchCashAccount(Users user,EntityTransaction transaction);
	public BranchBankAccountBalance recoincileBranchBankAccountBalance(Users user,BranchBankAccounts branchBankAccount,EntityTransaction transaction); 
}
