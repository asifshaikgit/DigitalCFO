package com.idos.dao.trialbalance;

import com.idos.dao.BaseDAO;
import model.BranchBankAccounts;
import model.BranchDepositBoxKey;
import model.TrialBalance;
import model.Users;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.Date;

/**
 * @author Sunil K Namdev created on 24.11.2019
 *
 */
public interface TrialBalanceCashDAO extends BaseDAO {
    String cashOrg = "select obj.branch.name, obj.branchDepositBoxKey.id, obj.branchDepositBoxKey.openingBalance, SUM(obj.creditAmount), SUM(obj.debitAmount), obj.branchDepositBoxKey.pettyCashOpeningBalance from TrialBalanceBranchCash obj where obj.organization.id = ?1 and obj.branchDepositBoxKey.id = ?2 and obj.presentStatus=1 and obj.date between ?3 and ?4 group by obj.branch.name, obj.branchDepositBoxKey.id, obj.branchDepositBoxKey.openingBalance,obj.branchDepositBoxKey.pettyCashOpeningBalance";

    String cashBranch = "select obj.branch.name, obj.branchDepositBoxKey.id, obj.branchDepositBoxKey.openingBalance, SUM(obj.creditAmount), SUM(obj.debitAmount), obj.branchDepositBoxKey.pettyCashOpeningBalance from TrialBalanceBranchCash obj where obj.organization.id = ?1 and obj.branch.id = ?2 and obj.branchDepositBoxKey.id = ?3 and obj.presentStatus=1 and obj.date between ?4 and ?5 group by obj.branch.name, obj.branchDepositBoxKey.id, obj.branchDepositBoxKey.openingBalance,obj.branchDepositBoxKey.pettyCashOpeningBalance";

    TrialBalance getTrialBalance4SpecificCash(Users user, EntityManager em, Date fromDate, Date toDate,
            final int cashType, Long branchId, BranchDepositBoxKey cashAccount);
}
