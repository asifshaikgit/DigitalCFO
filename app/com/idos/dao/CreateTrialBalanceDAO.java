package com.idos.dao;

import com.idos.util.IDOSException;
import model.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import service.ChartOfAccountsService;
import service.ChartOfAccountsServiceImpl;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.Date;

/**
 * Created by Sunil K. Namdev on 06-07-2017.
 */
public interface CreateTrialBalanceDAO extends BaseDAO {
    ChartOfAccountsService coaService = new ChartOfAccountsServiceImpl();
    String OUTPUT_TAX_HQL = "select obj from BranchTaxes obj where obj.branch.id=?1 and obj.organization.id=?2 and obj.taxType in(2,20,21,22,23) and obj.presentStatus=1";

    void insertTrialBalance(Transaction transaction, Users user, EntityManager entityManager) throws IDOSException;

    void saveMultiItemTrialBalance(Transaction transaction, Users user, EntityManager em, Vendor vendor,
            Long tdsMappingID) throws IDOSException;

    void saveTrialBalInterBranch(Transaction transaction, Users user, Integer typeIdentifier, EntityManager em,
            boolean isCredit);

    void saveTrialBalanceCOAItem(Organization org, Branch branch, Long txnId, TransactionPurpose txnPurpose,
            Specifics specifics, Particulars particulars, Date txnDate, Double amount, Users user, EntityManager em,
            boolean isCredit);

    Boolean saveTrialBalanceForRoundOff(Organization org, Branch branch, Long txnId, TransactionPurpose txnPurpose,
            Date txnDate, Double roundOffAmount, Users user, EntityManager em, boolean isCredit) throws IDOSException;

    long saveTrialBalanceTDS(Users user, EntityManager em, Branch branch, Vendor vendor, Long txnid,
            TransactionPurpose txnPurpose, Date date, Specifics specific, Double amount, boolean isCredit, int taxType,
            int presentStatus) throws IDOSException;

    BranchTaxes getTdsType4ExpenseByMappedSpecific(Users user, EntityManager em, Specifics specific, Vendor vendor,
            Branch branch, TransactionPurpose txnPurpose, Date date) throws IDOSException;
}
