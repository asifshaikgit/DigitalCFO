package com.idos.dao.trialbalance;

import com.idos.util.IdosConstants;
import model.Specifics;
import model.TrialBalance;
import model.Users;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Sunil K Namdev created on 26.11.2019
 *
 */
public class TrialBalanceTdsDAOImpl implements TrialBalanceTdsDAO {

    public TrialBalance getTrialBalanceForTds(Users user, EntityManager em, Date fromDate, Date toDate, Long branchId,
            Specifics specific, final int taxType, final short particularType, final int mappedID) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "****** Start ");
        String sql = null;
        ArrayList inparams = new ArrayList();
        inparams.add(user.getOrganization().getId());
        TrialBalance tb = null;
        if (branchId != null && branchId > 0) {
            if (IdosConstants.ASSETS == particularType) {
                sql = TDS_BRANCH_SQL8;
            } else {
                sql = TDS_BRANCH_SQL9;
            }
            inparams.add(branchId);
            inparams.add(specific.getId());
            inparams.add(mappedID);
            inparams.add(user.getOrganization().getId());
            inparams.add(branchId);
            inparams.add(specific.getId());
            inparams.add(mappedID);
            inparams.add(taxType);
            inparams.add(fromDate);
            inparams.add(user.getOrganization().getId());
            inparams.add(branchId);
        } else {
            if (IdosConstants.ASSETS == particularType) {
                sql = TDS_ORG_SQL8;
            } else {
                sql = TDS_ORG_SQL9;
            }
            inparams.add(specific.getId());
            inparams.add(mappedID);
            inparams.add(user.getOrganization().getId());
            inparams.add(specific.getId());
            inparams.add(mappedID);
            inparams.add(taxType);
            inparams.add(fromDate);
            inparams.add(user.getOrganization().getId());
        }
        inparams.add(specific.getId());
        inparams.add(mappedID);
        inparams.add(taxType);
        inparams.add(fromDate);
        inparams.add(toDate);
        List<Object[]> txnLists = genericDao.executeNativeQueryWithParam(sql, em, inparams);
        Double creditAmt = 0.0;
        Double debitAmt = 0.0;
        Double openBal = 0.0;

        for (Object[] custData : txnLists) {
            if (custData[0] != null)
                openBal += Double.parseDouble(String.valueOf(custData[0]));
            if (custData[1] != null) {
                creditAmt += Double.parseDouble(String.valueOf(custData[1]));
            }
            if (custData[2] != null) {
                debitAmt += Double.parseDouble(String.valueOf(custData[2]));
            }
        }
        if (txnLists.size() > 0) {
            tb = new TrialBalance();
            tb.setOpeningBalance(openBal);
            tb.setDebit(debitAmt);
            tb.setCredit(creditAmt);
            if (IdosConstants.ASSETS == particularType) {
                tb.setClosingBalance(openBal + debitAmt - creditAmt);
            } else if (IdosConstants.LIABILITIES == particularType) {
                tb.setClosingBalance(openBal + creditAmt - debitAmt);
            }
            tb.setHeadType(IdosConstants.HEAD_TDS);
            tb.setIdentificationForDataValid(String.valueOf(mappedID));
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "****** End ");
        return tb;
    }
}
