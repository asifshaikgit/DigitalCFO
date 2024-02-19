package com.idos.dao.trialbalance;

import com.idos.util.IdosConstants;
import model.BranchTaxes;
import model.Specifics;
import model.TrialBalance;
import model.Users;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Sunil K Namdev created on 26.11.2019
 */
public class TrialBalanceGstTaxDAOImpl implements TrialBalanceGstTaxDAO {
    public TrialBalance getTrialBalanceGstTax(Users user, EntityManager em, Date fromDate, Date toDate, Long branchId,
            final BranchTaxes branchTax, final short particularType) {
        final int taxType = branchTax.getTaxType();
        TrialBalance tb = null;
        String sql = null;
        ArrayList inparams = new ArrayList(5);
        inparams.add(user.getOrganization().getId());
        if (branchId != null && branchId > 0) {
            sql = BRANCH_GST_TAX;
            inparams.add(branchId);
            inparams.add(branchId);
        } else {
            sql = ORG_GST_TAX;
        }
        inparams.add(branchTax.getId());
        inparams.add(taxType);
        inparams.add(fromDate);
        inparams.add(toDate);

        List<Object[]> txnLists = genericDao.queryWithParamsNameGeneric(sql, em, inparams);
        if (txnLists.size() > 0) {
            Double creditAmt = 0.0;
            Double debitAmt = 0.0;
            String taxName = null;
            Object[] custData = txnLists.get(0);
            Long taxId = (Long) custData[0];
            if (user.getOrganization().getGstCountryCode() != null
                    && !"".equals(user.getOrganization().getGstCountryCode())) {
                taxName = custData[1].toString();
                creditAmt = Double.parseDouble(String.valueOf(custData[4] == null ? 0.0 : custData[4]));
                debitAmt = Double.parseDouble(String.valueOf(custData[5] == null ? 0.0 : custData[5]));
            } else {
                taxName = custData[1].toString() + " - " + custData[2].toString();
                creditAmt = Double.parseDouble(String.valueOf(custData[4] == null ? 0.0 : custData[4]));
                debitAmt = Double.parseDouble(String.valueOf(custData[5] == null ? 0.0 : custData[5]));
            }
            int presentStatus = (int) custData[6];
            Double openingBal = Double.parseDouble(String.valueOf(custData[3] == null ? 0.0 : custData[3]));
            Double drivedOpeningBal = TRIAL_BALANCE_DAO.getOpeningBalForTaxes(user,
                    IdosConstants.MYSQLDF.format(fromDate), branchId, em, (short) taxType, taxId, openingBal,
                    particularType);
            tb = new TrialBalance();
            if (presentStatus == 0) {
                tb.setAccountName(taxName + " (Disabled)");
            } else {
                tb.setAccountName(taxName);
            }
            tb.setSpecId(taxId);
            tb.setSpecfaccountCode("1");
            tb.setOpeningBalance(drivedOpeningBal);
            tb.setDebit(debitAmt);
            tb.setCredit(creditAmt);
            if (particularType == IdosConstants.LIABILITIES) {
                tb.setClosingBalance(drivedOpeningBal + creditAmt - debitAmt);
                tb.setTopLevelAccountCode("4000000000000000000");
            } else if (particularType == IdosConstants.ASSETS) {
                tb.setClosingBalance(drivedOpeningBal + debitAmt - creditAmt);
                tb.setTopLevelAccountCode("3000000000000000000");
            }
            if (IdosConstants.OUTPUT_TAX == taxType || IdosConstants.INPUT_TAX == taxType) {
                tb.setHeadType(IdosConstants.HEAD_TAXS);
            } else if (IdosConstants.OUTPUT_SGST == taxType || IdosConstants.INPUT_SGST == taxType) {
                tb.setHeadType(IdosConstants.HEAD_SGST);
            } else if (IdosConstants.OUTPUT_CGST == taxType || IdosConstants.INPUT_CGST == taxType) {
                tb.setHeadType(IdosConstants.HEAD_CGST);
            } else if (IdosConstants.OUTPUT_IGST == taxType || IdosConstants.INPUT_IGST == taxType) {
                tb.setHeadType(IdosConstants.HEAD_IGST);
            } else if (IdosConstants.OUTPUT_CESS == taxType || IdosConstants.INPUT_CESS == taxType) {
                tb.setHeadType(IdosConstants.HEAD_CESS);
            } else if (IdosConstants.RCM_SGST_IN == taxType) {
                tb.setHeadType(IdosConstants.HEAD_RCM_SGST_IN);
            } else if (IdosConstants.RCM_CGST_IN == taxType) {
                tb.setHeadType(IdosConstants.HEAD_RCM_CGST_IN);
            } else if (IdosConstants.RCM_IGST_IN == taxType) {
                tb.setHeadType(IdosConstants.HEAD_RCM_IGST_IN);
            } else if (IdosConstants.RCM_CESS_IN == taxType) {
                tb.setHeadType(IdosConstants.HEAD_RCM_CESS_IN);
            } else if (IdosConstants.RCM_SGST_OUTPUT == taxType) {
                tb.setHeadType(IdosConstants.HEAD_RCM_SGST_OUTPUT);
            } else if (IdosConstants.RCM_CGST_OUTPUT == taxType) {
                tb.setHeadType(IdosConstants.HEAD_RCM_CGST_OUTPUT);
            } else if (IdosConstants.RCM_IGST_OUTPUT == taxType) {
                tb.setHeadType(IdosConstants.HEAD_RCM_IGST_OUTPUT);
            } else if (IdosConstants.RCM_CESS_OUTPUT == taxType) {
                tb.setHeadType(IdosConstants.HEAD_RCM_CESS_OUTPUT);
            }
        }
        return tb;
    }

}
