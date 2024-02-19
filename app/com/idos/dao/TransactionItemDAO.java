package com.idos.dao;

import model.TransactionItems;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.Date;
import java.util.List;

/**
 * Created by Sunil K. Namdev on 20-10-2019.
 */
public interface TransactionItemDAO extends BaseDAO {
        String TDS_GROSS_JPQL = "select obj from TransactionItems obj where obj.organization.id = ?1 and obj.transactionSpecifics.id = ?2 and obj.transaction.id in (select obj2.id from Transaction obj2 where obj2.transactionBranchOrganization.id = ?3 and obj2.transactionVendorCustomer.id = ?4 and obj2.transactionStatus = ?5 and obj2.transactionPurpose.id in (3,4,8,11,32,33,20) and obj2.presentStatus = 1 and obj2.id not in (select t3.transactionId from TrialBalanceTaxes t3 where t3.organization.id = ?6 and t3.transactionSpecifics.id = ?7 and t3.branchTaxes.id = ?8 and t3.taxType >= 40 and t3.transactionPurpose.id in (3,4,8,11,32,33,20) and  t3.presentStatus=1))";

        String TDS_TOTAL_GROSS_JPQL = "select sum(obj.grossAmount) from TransactionItems obj where obj.organization.id = ?1 and obj.transactionSpecifics.id = ?2 and obj.presentStatus = 1 and obj.transaction.id in (select obj2.id from Transaction obj2 where obj2.transactionBranchOrganization.id = ?3 and obj2.transactionVendorCustomer.id = ?4 and obj2.transactionStatus = ?5 and obj2.transactionPurpose.id in (3,4,8,11,32,33,20) and obj2.presentStatus = 1) group by obj.transactionSpecifics";

        List<TransactionItems> findTransItemsForVendorDateRange(EntityManager em, Long orgid, Long specId, Long vendId,
                        Date toDate, Date fromDate, Long tdsPayableSpecificID);

        Double getGrossAmountForSpecificAndVendor(EntityManager em, Long orgid, Long specId, Long vendId, Date toDate,
                        Date fromDate);

        String BY_TXN_JPQL = "select obj from TransactionItems obj where obj.organization.id = ?1 and obj.transaction.id = ?2";

        List<TransactionItems> findByTransaction(EntityManager em, long orgid, long txnId);
}
