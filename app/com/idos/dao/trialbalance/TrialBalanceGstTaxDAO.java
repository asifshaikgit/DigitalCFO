package com.idos.dao.trialbalance;

import com.idos.dao.BaseDAO;
import model.BranchTaxes;
import model.TrialBalance;
import model.Users;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.Date;

/**
 * @author Sunil K Namdev created on 26.11.2019
 *
 */
public interface TrialBalanceGstTaxDAO extends BaseDAO {
    String ORG_GST_TAX = "select obj.branchTaxes.id, obj.branchTaxes.taxName, obj.branch.name, obj.branchTaxes.openingBalance, SUM(obj.creditAmount), SUM(obj.debitAmount), obj.branchTaxes.presentStatus from TrialBalanceTaxes obj where obj.organization.id= ?1 and obj.branchTaxes.id = ?2 and obj.taxType = ?3 and obj.presentStatus=1 and obj.date between ?4 and ?5 group by obj.branchTaxes.id,obj.branchTaxes.taxName,obj.branch.name,obj.branchTaxes.openingBalance,obj.branchTaxes.presentStatus";

    String BRANCH_GST_TAX = "select obj.branchTaxes.id, obj.branchTaxes.taxName, obj.branch.name, obj.branchTaxes.openingBalance, SUM(obj.creditAmount), SUM(obj.debitAmount), obj.branchTaxes.presentStatus from TrialBalanceTaxes obj where obj.organization.id= ?1 and obj.branch.id = ?2 and obj.branchTaxes.branch.id = ?3 and obj.branchTaxes.id = ?4 and obj.taxType= ?5 and obj.presentStatus=1 and obj.date between ?6 and ?7 group by obj.branchTaxes.id,obj.branchTaxes.taxName,obj.branch.name,obj.branchTaxes.openingBalance,obj.branchTaxes.presentStatus";

    TrialBalance getTrialBalanceGstTax(Users user, EntityManager em, Date fromDate, Date toDate, Long branchId,
            final BranchTaxes branchTax, final short particularType);
}
