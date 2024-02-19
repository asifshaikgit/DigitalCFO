package com.idos.dao.trialbalance;

import com.idos.dao.BaseDAO;
import model.BranchBankAccounts;
import model.TrialBalance;
import model.Users;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.Date;

/**
 * @author Sunil K Namdev created on 24.11.2019
 *
 */
public interface TrialBalanceBankDAO extends BaseDAO {
    String orgBank = "select obj.branchBankAccounts.bankName, obj.branch.name, obj.branchBankAccounts.id, obj.branchBankAccounts.openingBalance, SUM(obj.creditAmount), SUM(obj.debitAmount) from TrialBalanceBranchBank obj where obj.organization.id = ?1 and obj.branchBankAccounts.id = ?2 and obj.presentStatus=1 and obj.date between ?3 and ?4 group by obj.branchBankAccounts.bankName,obj.branch.name,obj.branchBankAccounts.id,obj.branchBankAccounts.openingBalance";

    String branchBank = "select obj.branchBankAccounts.bankName, obj.branch.name, obj.branchBankAccounts.id, obj.branchBankAccounts.openingBalance, SUM(obj.creditAmount), SUM(obj.debitAmount) from TrialBalanceBranchBank obj where obj.organization.id = ?1 and obj.branch.id = ?2 and obj.branchBankAccounts.id = ?3 and obj.presentStatus=1 and obj.date between ?4 and ?5 group by obj.branchBankAccounts.bankName,obj.branch.name,obj.branchBankAccounts.id,obj.branchBankAccounts.openingBalance";

    TrialBalance getTrialBalance4SpecificBank(Users user, EntityManager em, Date fromDate, Date toDate, Long branchId,
            BranchBankAccounts bankAccount);
}
