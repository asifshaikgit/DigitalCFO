package com.idos.dao.trialbalance;

import com.idos.util.IdosConstants;
import model.Specifics;
import model.TrialBalance;
import model.Users;
import model.Vendor;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Sunil K Namdev created on 24.11.2019
 *
 */
public class TrialBalanceVendorCustomerDAOImpl implements TrialBalanceVendorCustomerDAO {

    @Override
    public TrialBalance getTb4CustomerOrVendorAdvance(Users user, EntityManager em, Date fromDate, Date toDate,
            Long branchId, Vendor vendor) {
        TrialBalance tb = null;
        String hql = null;
        int vendorType = vendor.getType();
        int walkinType = IdosConstants.WALK_IN_VENDOR;
        if (branchId != null && branchId > 0) {
            if (IdosConstants.CUSTOMER == vendorType) {
                hql = CUSTVEND_ADV_BRANCH_JPQL_LIB;
                walkinType = IdosConstants.WALK_IN_CUSTOMER;
            } else {
                hql = CUSTVEND_ADV_BRANCH_JPQL_AST;
            }
        } else {
            if (IdosConstants.CUSTOMER == vendorType) {
                hql = CUSTVEND_ADV_ORG_JPQL_LIB;
                walkinType = IdosConstants.WALK_IN_CUSTOMER;
            } else {
                hql = CUSTVEND_ADV_ORG_JPQL_AST;
            }
        }
        ArrayList inparams = new ArrayList();
        if (branchId != null && branchId > 0) {
            inparams.add(user.getOrganization().getId());
            inparams.add(branchId);
            inparams.add(vendor.getId());
            inparams.add(user.getOrganization().getId());
            inparams.add(branchId);
            inparams.add(vendor.getId());
            inparams.add(vendorType);
            inparams.add(walkinType);
            inparams.add(fromDate);
            inparams.add(user.getOrganization().getId());
            inparams.add(branchId);
            inparams.add(vendor.getId());
            inparams.add(vendorType);
            inparams.add(walkinType);
            inparams.add(fromDate);
            inparams.add(toDate);
        } else {
            inparams.add(user.getOrganization().getId());
            inparams.add(vendor.getId());
            inparams.add(user.getOrganization().getId());
            inparams.add(vendor.getId());
            inparams.add(vendorType);
            inparams.add(walkinType);
            inparams.add(fromDate);
            inparams.add(user.getOrganization().getId());
            inparams.add(vendor.getId());
            inparams.add(vendorType);
            inparams.add(walkinType);
            inparams.add(fromDate);
            inparams.add(toDate);
        }
        List<Object[]> txnLists = genericDao.executeNativeQueryWithParam(hql, em, inparams);
        Double creditAmt = 0.0;
        Double debitAmt = 0.0;
        Double openingBal = 0.0;
        if (txnLists.size() > 0) {
            Object[] custData = txnLists.get(0);
            // Long vendorId = new Long(custData[4].toString());
            if (vendor.getType() == vendorType) {
                if (custData[1] != null) {
                    openingBal += (Double) custData[1];
                } else if (branchId == null || branchId == 0) {
                    openingBal += vendor.getTotalOriginalOpeningBalanceAdvPaid();
                }
                // String vendName = vendor.getName();
                if (custData[2] != null) {
                    creditAmt += Double.parseDouble(String.valueOf(custData[2]));
                }
                if (custData[3] != null) {
                    debitAmt += Double.parseDouble(String.valueOf(custData[3]));
                }
            }
            tb = new TrialBalance();
            tb.setOpeningBalance(openingBal);
            tb.setDebit(debitAmt);
            tb.setCredit(creditAmt);
            if (vendorType == IdosConstants.VENDOR) { // vendor advance
                tb.setClosingBalance(openingBal + debitAmt - creditAmt);
                tb.setHeadType(IdosConstants.HEAD_VENDOR_ADV);
            } else {
                tb.setClosingBalance(openingBal + creditAmt - debitAmt);
                tb.setHeadType(IdosConstants.HEAD_CUSTOMER_ADV);
            }
        }
        return tb;
    }

    public TrialBalance getTb4CustomerOrVendor(Users user, EntityManager em, Date fromDate, Date toDate, Long branchId,
            Vendor vendor) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "******* Start ");
        String hql = null;
        TrialBalance tb = null;
        int vendorType = vendor.getType();
        int walkinType = IdosConstants.WALK_IN_VENDOR;
        if (branchId != null && branchId > 0) {
            if (IdosConstants.CUSTOMER == vendorType) {
                hql = CUSTVEND_BRANCH_JPQL_AST;
                walkinType = IdosConstants.WALK_IN_CUSTOMER;
            } else {
                hql = CUSTVEND_BRANCH_JPQL_LIB;
            }
        } else {
            if (IdosConstants.CUSTOMER == vendorType) {
                hql = CUSTVEND_ORG_JPQL_AST;
                walkinType = IdosConstants.WALK_IN_CUSTOMER;
            } else {
                hql = CUSTVEND_ORG_JPQL_LIB;
            }
        }
        ArrayList inparams = new ArrayList();
        if (branchId != null && branchId > 0) {
            inparams.add(user.getOrganization().getId());
            inparams.add(branchId);
            inparams.add(vendor.getId());
            inparams.add(user.getOrganization().getId());
            inparams.add(branchId);
            inparams.add(vendor.getId());
            inparams.add(vendorType);
            inparams.add(walkinType);
            inparams.add(fromDate);
            inparams.add(user.getOrganization().getId());
            inparams.add(branchId);
            inparams.add(vendor.getId());
            inparams.add(vendorType);
            inparams.add(walkinType);
            inparams.add(fromDate);
            inparams.add(toDate);
        } else {
            inparams.add(user.getOrganization().getId());
            inparams.add(vendor.getId());
            inparams.add(user.getOrganization().getId());
            inparams.add(vendor.getId());
            inparams.add(vendorType);
            inparams.add(walkinType);
            inparams.add(fromDate);
            inparams.add(user.getOrganization().getId());
            inparams.add(vendor.getId());
            inparams.add(vendorType);
            inparams.add(walkinType);
            inparams.add(fromDate);
            inparams.add(toDate);
        }
        List<Object[]> txnLists = genericDao.executeNativeQueryWithParam(hql, em, inparams);
        Double derivedOpenBal = 0.0;
        if (txnLists.size() > 0) {
            Object[] custData = txnLists.get(0);
            if (vendor.getType() == vendorType) {
                Double creditAmt = 0.0;
                Double debitAmt = 0.0;
                Double openingBal = 0.0;
                if (custData[0] != null) {
                    openingBal = (Double) custData[0];
                } else if (branchId == null || branchId == 0) {
                    openingBal = vendor.getTotalOriginalOpeningBalance();
                }
                String custName = vendor.getName();
                if (custData[2] != null) {
                    creditAmt = Double.parseDouble(String.valueOf(custData[2]));
                }
                if (custData[3] != null) {
                    debitAmt = Double.parseDouble(String.valueOf(custData[3]));
                }
                derivedOpenBal = openingBal;
                tb = new TrialBalance();
                tb.setAccountName(custName);
                tb.setSpecId(vendor.getId());
                tb.setSpecfaccountCode(vendor.getId().toString());
                tb.setOpeningBalance(derivedOpenBal);
                tb.setDebit(debitAmt);
                tb.setCredit(creditAmt);
                if (vendorType == IdosConstants.VENDOR) { // vendor credit sales: Liability
                    tb.setTopLevelAccountCode("4000000000000000000");
                    tb.setClosingBalance(derivedOpenBal + creditAmt - debitAmt);
                    tb.setHeadType(IdosConstants.HEAD_VENDOR);
                } else { // customer
                    tb.setTopLevelAccountCode("3000000000000000000");
                    tb.setClosingBalance(derivedOpenBal + debitAmt - creditAmt);
                    tb.setHeadType(IdosConstants.HEAD_CUSTOMER); // customer assets
                }
            }
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "******* End " + tb.toString());
        return tb;
    }
}
