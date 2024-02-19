package com.idos.dao;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import model.*;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @auther Sunil Namdev created on 30.04.2018
 */
public class ClaimSettlementDAOImpl implements ClaimSettlementDAO {
    @Override
    public Double saveClaimTrialBalanceGstTax(ClaimTransaction txn, Users user, EntityManager em, ClaimItemDetails item,
            Specifics specifics) {
        Double totalTax = 0.0;
        BranchTaxes tax = null;
        if (item.getSgstId() != null) {
            tax = BranchTaxes.findById(item.getSgstId());
            saveTrialBalanceGstTax(txn, user, em, false, item.getSgstAmt(), tax, specifics);
            totalTax = item.getSgstAmt();
            tax = BranchTaxes.findById(item.getCgstId());
            saveTrialBalanceGstTax(txn, user, em, false, item.getCgstAmt(), tax, specifics);
            totalTax += item.getCgstAmt();
        } else if (item.getIgstId() != null) {
            tax = BranchTaxes.findById(item.getIgstId());
            saveTrialBalanceGstTax(txn, user, em, false, item.getIgstAmt(), tax, specifics);
            totalTax = item.getIgstAmt();
        }
        if (item.getCessId() != null) {
            tax = BranchTaxes.findById(item.getCessId());
            saveTrialBalanceGstTax(txn, user, em, false, item.getCessAmt(), tax, specifics);
            totalTax += item.getCessAmt();
        }

        return totalTax;
    }

    @Override
    public void saveTrialBalanceGstTax(ClaimTransaction txn, Users user, EntityManager entityManager, boolean isCredit,
            Double taxAmount, BranchTaxes branchTaxes, Specifics specifics) {
        TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
        trialBalTaxes.setTransactionId(txn.getId());
        trialBalTaxes.setTransactionPurpose(txn.getTransactionPurpose());
        trialBalTaxes.setDate(txn.getTransactionDate());
        trialBalTaxes.setBranchTaxes(branchTaxes);
        trialBalTaxes.setTaxType(branchTaxes.getTaxType()); // output taxes
        trialBalTaxes.setBranch(txn.getTransactionBranch());
        trialBalTaxes.setOrganization(txn.getTransactionBranchOrganization());
        if (isCredit) {
            trialBalTaxes.setCreditAmount(taxAmount);
        } else {
            trialBalTaxes.setDebitAmount(taxAmount);
        }
        trialBalTaxes.setTransactionSpecifics(specifics);
        trialBalTaxes.setTransactionParticulars(specifics.getParticularsId());
        genericDao.saveOrUpdate(trialBalTaxes, user, entityManager);
    }

    /**
     * used to set Trial balance for Travel Expense, Boarding & Lodging, Other
     * expenses and fixed per diam and Incurred Exp.
     *
     */
    @Override
    public void addExpensesInTrialBalance(Users user, EntityManager entityManager, ClaimTransaction claimTransaction,
            boolean isCredit) throws IDOSException {
        if (claimTransaction.getTransactionPurpose().getId() != IdosConstants.REQUEST_FOR_EXPENSE_REIMBURSEMENT
                && claimTransaction.getTransactionPurpose().getId() != IdosConstants.SETTLE_ADVANCE_FOR_EXPENSE) {
            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("branch.id", claimTransaction.getTransactionBranch().getId());
            criterias.put("transaction.id", claimTransaction.getId());
            criterias.put("presentStatus", 1);
            List<ClaimItemDetails> claimItemDetailsList = genericDao.findByCriteria(ClaimItemDetails.class, criterias,
                    entityManager);
            if (claimItemDetailsList != null && !claimItemDetailsList.isEmpty()) {
                for (ClaimItemDetails claimItemDetails : claimItemDetailsList) {
                    Long specificId = claimItemDetails.getClaimSpecific();
                    if (specificId == null) {
                        throw new IDOSException(IdosConstants.TB_EXCEPTION_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                                "Specific is null", "Can't store TB for the Claim Details");
                    }
                    Specifics specific = Specifics.findById(specificId);
                    if (specific.getIsEligibleInputTaxCredit() == 1) {
                        trialBalanceService.addTrialBalanceCOAItems(user, entityManager, genericDao, claimTransaction,
                                claimItemDetails.getGrossAmt(), specific, isCredit);
                        saveClaimTrialBalanceGstTax(claimTransaction, user, entityManager, claimItemDetails, specific);
                    } else {
                        trialBalanceService.addTrialBalanceCOAItems(user, entityManager, genericDao, claimTransaction,
                                claimItemDetails.getNetAmount(), specific, isCredit);
                    }
                }
            }
        }
    }

}
