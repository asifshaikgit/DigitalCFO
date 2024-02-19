package com.idos.dao;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import model.*;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.*;
import play.db.jpa.JPAApi;
import javax.inject.Inject;

/**
 * Created by Sunil K. Namdev on 06-07-2017.
 */
public class CreateTrialBalanceDAOImpl implements CreateTrialBalanceDAO {

    @Override
    public void insertTrialBalance(Transaction transaction, Users user, EntityManager entityManager)
            throws IDOSException {
        Map<String, Object> criterias = new HashMap<String, Object>();
        Long txnPurpose = transaction.getTransactionPurpose().getId();
        if (txnPurpose == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                || txnPurpose == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
                || txnPurpose == IdosConstants.DEBIT_NOTE_CUSTOMER
                || (txnPurpose == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER
                        && transaction.getTypeIdentifier() == 1)) {
            if (transaction.getPerformaInvoice() == null || !transaction.getPerformaInvoice()) {
                criterias.clear();
                criterias.put("transaction.id", transaction.getId());
                criterias.put("presentStatus", 1);
                List<TransactionItems> tranItemsList = genericDao.findByCriteria(TransactionItems.class, criterias,
                        entityManager);
                if (tranItemsList != null && tranItemsList.size() > 0) {
                    for (TransactionItems tranItem : tranItemsList) {
                        TrialBalanceCOAItems trialBalCOA = new TrialBalanceCOAItems();
                        Double discountAmt = 0d;
                        if (txnPurpose == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                                || txnPurpose == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
                                || txnPurpose == IdosConstants.DEBIT_NOTE_CUSTOMER) {
                            if (tranItem.getDiscountAmount() != null && tranItem.getDiscountAmount() > 0) {
                                discountAmt = tranItem.getDiscountAmount();
                                Specifics specifics = Specifics.findByOrganizationAndMappingID(entityManager,
                                        transaction.getTransactionBranchOrganization(), "63");
                                /* Mapping id 63 for discount allowed */
                                saveTrialBalanceCOAItem(transaction.getTransactionBranchOrganization(),
                                        transaction.getTransactionBranch(), transaction.getId(),
                                        transaction.getTransactionPurpose(),
                                        specifics, specifics.getParticularsId(), transaction.getTransactionDate(),
                                        discountAmt, user, entityManager, false);
                            }
                        }
                        trialBalCOA.setTransactionId(transaction.getId());
                        trialBalCOA.setTransactionPurpose(transaction.getTransactionPurpose());
                        trialBalCOA.setTransactionSpecifics(tranItem.getTransactionSpecifics());
                        trialBalCOA.setTransactionParticulars(tranItem.getTransactionParticulars());
                        trialBalCOA.setDate(transaction.getTransactionDate());
                        trialBalCOA.setBranch(transaction.getTransactionBranch());
                        trialBalCOA.setOrganization(transaction.getTransactionBranchOrganization());
                        trialBalCOA.setCreditAmount(tranItem.getGrossAmount() + discountAmt);
                        genericDao.saveOrUpdate(trialBalCOA, user, entityManager);
                        if (!tranItem.getAdjustmentFromAdvance().equals("")
                                && tranItem.getAdjustmentFromAdvance() != null
                                && tranItem.getAdjustmentFromAdvance() > 0.0
                                && (transaction.getPerformaInvoice() == null || !transaction.getPerformaInvoice())) {
                            // Out of sell transaction of 1000/- if adjusting 400/- from customer advance
                            // then 400 will go as Debit for sell transaction

                            Double totalAdjTax = 0d;
                            if (txnPurpose != IdosConstants.DEBIT_NOTE_CUSTOMER) {
                                if (tranItem.getAdvAdjTax1Value() != null) {
                                    totalAdjTax += tranItem.getAdvAdjTax1Value();
                                }
                                if (tranItem.getAdvAdjTax2Value() != null) {
                                    totalAdjTax += tranItem.getAdvAdjTax2Value();
                                }
                                if (tranItem.getAdvAdjTax3Value() != null) {
                                    totalAdjTax += tranItem.getAdvAdjTax3Value();
                                }
                                if (tranItem.getAdvAdjTax4Value() != null) {
                                    totalAdjTax += tranItem.getAdvAdjTax4Value();
                                }
                                if (tranItem.getAdvAdjTax5Value() != null) {
                                    totalAdjTax += tranItem.getAdvAdjTax5Value();
                                }
                                if (tranItem.getAdvAdjTax6Value() != null) {
                                    totalAdjTax += tranItem.getAdvAdjTax6Value();
                                }
                                if (tranItem.getAdvAdjTax7Value() != null) {
                                    totalAdjTax += tranItem.getAdvAdjTax7Value();
                                }

                                TrialBalanceVendorAdvance trialBalVenAdv = new TrialBalanceVendorAdvance(); // adv in
                                                                                                            // case of
                                                                                                            // sell on
                                                                                                            // cash and
                                                                                                            // credit
                                                                                                            // both
                                trialBalVenAdv.setTransactionId(transaction.getId());
                                trialBalVenAdv.setTransactionPurpose(transaction.getTransactionPurpose());
                                trialBalVenAdv.setTransactionParticulars(tranItem.getTransactionParticulars());
                                trialBalVenAdv.setTransactionSpecifics(tranItem.getTransactionSpecifics());
                                trialBalVenAdv.setDate(transaction.getTransactionDate());
                                trialBalVenAdv.setBranch(transaction.getTransactionBranch());
                                trialBalVenAdv.setOrganization(transaction.getTransactionBranchOrganization());
                                trialBalVenAdv.setVendorType(2); // vendor type=2 means customer and =1 means vendor
                                trialBalVenAdv.setVendor(transaction.getTransactionVendorCustomer());
                                trialBalVenAdv.setDebitAmount(tranItem.getAdjustmentFromAdvance());
                                if (txnPurpose != IdosConstants.DEBIT_NOTE_CUSTOMER) {
                                    trialBalVenAdv.setCreditAmount(totalAdjTax); // GST -28
                                }
                                genericDao.saveOrUpdate(trialBalVenAdv, user, entityManager);
                            }
                        }
                        // ***************************TAXES*************************/
                        if (transaction.getPerformaInvoice() == null || transaction.getPerformaInvoice() != true) {
                            String txnnetamountdescription = tranItem.getTaxDescription();
                            if (user.getOrganization().getGstCountryCode() != null) {
                                saveTrialBalanceGstTaxes(transaction, tranItem, user, entityManager, true);
                            } else {
                                insertTrialBalOutputTaxes(transaction, tranItem, user, entityManager, true,
                                        txnnetamountdescription);
                            }
                        }
                    }
                }
            }
            if (transaction.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW) {
                insertTrialBalCashOrBank(transaction, user, entityManager, false);
            }
            // sell on credit
            if ((transaction.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
                    || transaction.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_CUSTOMER)
                    && (transaction.getPerformaInvoice() == null || transaction.getPerformaInvoice() != true)) {
                insertTrialBalCustVendor(transaction, user, entityManager, false, IdosConstants.CUSTOMER,
                        transaction.getNetAmount());
            }
            if (transaction.getTransactionPurpose()
                    .getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                saveTrialBalInterBranch(transaction, user, transaction.getTypeIdentifier(), entityManager, false);
            }
        } else if (txnPurpose == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                || txnPurpose == IdosConstants.BUY_ON_CREDIT_PAY_LATER
                || txnPurpose == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT
                || txnPurpose == IdosConstants.CREDIT_NOTE_VENDOR || txnPurpose == IdosConstants.DEBIT_NOTE_VENDOR
                || (txnPurpose == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER
                        && transaction.getTypeIdentifier() == 2)) {
            criterias.clear();
            criterias.put("transaction.id", transaction.getId());
            criterias.put("presentStatus", 1);
            List<TransactionItems> tranItemsList = genericDao.findByCriteria(TransactionItems.class, criterias,
                    entityManager);
            if (tranItemsList != null && tranItemsList.size() > 0) {
                for (TransactionItems tranItem : tranItemsList) {
                    Double discountAmt = 0d;
                    TrialBalanceCOAItems trialBalCOA = new TrialBalanceCOAItems();
                    if (txnPurpose == IdosConstants.CREDIT_NOTE_VENDOR
                            || txnPurpose == IdosConstants.DEBIT_NOTE_VENDOR) {
                        if (tranItem.getDiscountAmount() != null && tranItem.getDiscountAmount() > 0) {
                            discountAmt = tranItem.getDiscountAmount();
                            Specifics specifics = Specifics.findByOrganizationAndMappingID(entityManager,
                                    transaction.getTransactionBranchOrganization(), "63");
                            /* Mapping id 63 for discount allowed */
                            Boolean isCredit = false;
                            if (transaction.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_VENDOR) {
                                isCredit = false;
                            } else {
                                isCredit = true;
                            }
                            saveTrialBalanceCOAItem(transaction.getTransactionBranchOrganization(),
                                    transaction.getTransactionBranch(), transaction.getId(),
                                    transaction.getTransactionPurpose(),
                                    specifics, specifics.getParticularsId(), transaction.getTransactionDate(),
                                    discountAmt, user, entityManager, isCredit);
                        }

                    }
                    trialBalCOA.setTransactionId(transaction.getId());
                    trialBalCOA.setTransactionPurpose(transaction.getTransactionPurpose());
                    trialBalCOA.setTransactionSpecifics(tranItem.getTransactionSpecifics());
                    trialBalCOA.setTransactionParticulars(tranItem.getTransactionParticulars());
                    trialBalCOA.setDate(transaction.getTransactionDate());
                    trialBalCOA.setBranch(transaction.getTransactionBranch());
                    trialBalCOA.setOrganization(transaction.getTransactionBranchOrganization());
                    if (transaction.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_VENDOR) {
                        trialBalCOA.setCreditAmount(tranItem.getGrossAmount() + discountAmt);
                    } else {
                        trialBalCOA.setDebitAmount(tranItem.getGrossAmount() + discountAmt);
                    }
                    genericDao.saveOrUpdate(trialBalCOA, user, entityManager);
                    if (txnPurpose != IdosConstants.CREDIT_NOTE_VENDOR
                            && txnPurpose != IdosConstants.DEBIT_NOTE_VENDOR) {
                        if (tranItem.getAdjustmentFromAdvance() != null && tranItem.getAdjustmentFromAdvance() > 0.0) {
                            TrialBalanceVendorAdvance trialBalVenAdv = new TrialBalanceVendorAdvance(); // adv in case
                                                                                                        // of sell on
                                                                                                        // cash and
                                                                                                        // credit both
                            trialBalVenAdv.setTransactionId(transaction.getId());
                            trialBalVenAdv.setTransactionPurpose(transaction.getTransactionPurpose());
                            trialBalVenAdv.setTransactionSpecifics(tranItem.getTransactionSpecifics());
                            trialBalVenAdv.setTransactionParticulars(tranItem.getTransactionParticulars());
                            trialBalVenAdv.setDate(transaction.getTransactionDate());
                            trialBalVenAdv.setBranch(transaction.getTransactionBranch());
                            trialBalVenAdv.setOrganization(transaction.getTransactionBranchOrganization());
                            trialBalVenAdv.setVendorType(1); // vendor type=2 means customer and =1 means vendor
                            trialBalVenAdv.setVendor(transaction.getTransactionVendorCustomer());
                            if (transaction.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_VENDOR) {
                                trialBalVenAdv.setDebitAmount(tranItem.getAdjustmentFromAdvance());
                            } else {
                                trialBalVenAdv.setCreditAmount(tranItem.getAdjustmentFromAdvance());
                            }
                            genericDao.saveOrUpdate(trialBalVenAdv, user, entityManager);
                        }
                    }
                    if (user.getOrganization().getGstCountryCode() != null) {
                        if (transaction.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_VENDOR) {
                            saveTrialBalanceGstTaxes(transaction, tranItem, user, entityManager, true);
                        } else {
                            saveTrialBalanceGstTaxes(transaction, tranItem, user, entityManager, false);
                        }
                    } else {
                        insertTrialBalInputTaxesForBuy(transaction, tranItem, user, entityManager, false,
                                tranItem.getTaxDescription());
                    }
                    if (tranItem.getWithholdingAmount() != null && tranItem.getWithholdingAmount() > 0.0) {
                        BranchTaxes branchTax = getTdsType4ExpenseByMappedSpecific(user, entityManager,
                                tranItem.getTransactionSpecifics(),
                                tranItem.getTransactionId().getTransactionVendorCustomer(), tranItem.getBranch(),
                                transaction.getTransactionPurpose(), null);
                        TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
                        trialBalTaxes.setBranchTaxes(branchTax);
                        trialBalTaxes.setTransactionId(transaction.getId());
                        trialBalTaxes.setTransactionPurpose(transaction.getTransactionPurpose());
                        trialBalTaxes.setDate(transaction.getTransactionDate());
                        trialBalTaxes.setTaxType(branchTax.getTaxType()); // it will get output TDS type
                        trialBalTaxes.setBranch(transaction.getTransactionBranch());
                        trialBalTaxes.setOrganization(transaction.getTransactionBranchOrganization());
                        if (transaction.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_VENDOR) {
                            trialBalTaxes.setDebitAmount(tranItem.getWithholdingAmount());
                        } else {
                            trialBalTaxes.setCreditAmount(tranItem.getWithholdingAmount());
                        }
                        trialBalTaxes.setTransactionSpecifics(tranItem.getTransactionSpecifics());
                        trialBalTaxes.setTransactionParticulars(tranItem.getTransactionParticulars());
                        genericDao.saveOrUpdate(trialBalTaxes, user, entityManager);
                    }
                }
            }
            if (transaction.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CREDIT_PAY_LATER
                    || transaction.getTransactionPurpose().getId() == IdosConstants.CREDIT_NOTE_VENDOR
                    || transaction.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_VENDOR) {
                if (transaction.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_VENDOR) {
                    insertTrialBalCustVendor(transaction, user, entityManager, false, IdosConstants.VENDOR,
                            transaction.getNetAmount());
                } else {
                    insertTrialBalCustVendor(transaction, user, entityManager, true, IdosConstants.VENDOR,
                            transaction.getNetAmount());
                }
            }
            if (transaction.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY) {
                insertTrialBalCashOrBank(transaction, user, entityManager, true);
            } else if (transaction.getTransactionPurpose().getId() == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) {
                insertTrialBalCashOrPettyCash(transaction, user, entityManager, IdosConstants.PETTY_CASH, true);
            }
            if (transaction.getTransactionPurpose()
                    .getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                saveTrialBalInterBranch(transaction, user, transaction.getTypeIdentifier(), entityManager, true);
            }
        } else if (txnPurpose == IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER) {
            List<TransactionItems> txnItemList = TransactionItems.finfByTxnId(entityManager, transaction.getId());
            for (TransactionItems tranItem : txnItemList) {
                if (IdosConstants.PAYMODE_CASH == transaction.getReceiptDetailsType()) {
                    saveTrialBalanceBranchCash(user, entityManager, transaction, tranItem.getNetAmount(), true);
                } else if (IdosConstants.PAYMODE_BANK == transaction.getReceiptDetailsType()) {
                    saveTrialBalanceBranchBank(user, entityManager, transaction, tranItem.getNetAmount(), true);
                }
                saveTrialBalanceVendorAdvance(transaction, tranItem, user, entityManager, tranItem.getGrossAmount(),
                        tranItem.getTotalTax(), transaction.getTransactionVendorCustomer(), false);
                if (tranItem.getWithholdingAmount() != null && tranItem.getWithholdingAmount() > 0.0) {
                    saveTrialBalanceTDS(user, entityManager, transaction.getTransactionBranch(),
                            tranItem.getTransactionId().getTransactionVendorCustomer(), transaction.getId(),
                            transaction.getTransactionPurpose(), transaction.getTransactionDate(),
                            tranItem.getTransactionSpecifics(), tranItem.getWithholdingAmount(), true,
                            IdosConstants.OUTPUT_TDS, 1);
                }
                saveTrialBalanceGstTaxes(transaction, tranItem, user, entityManager, true);
            }
        } else if (txnPurpose == IdosConstants.SALES_RETURNS) {
            String sellTxnRefNumber = transaction.getPaidInvoiceRefNumber();
            List<Transaction> previousTxnList = Transaction.findByTxnReference(entityManager,
                    transaction.getTransactionBranchOrganization().getId(), sellTxnRefNumber);// original sell on credit
                                                                                              // tran
            Transaction previousTransaction = previousTxnList.get(0);
            List<TransactionItems> tranItemsList = previousTransaction.getTransactionItems();
            if (tranItemsList != null && tranItemsList.size() > 0) {
                for (TransactionItems tranItem : tranItemsList) {
                    if (tranItem.getGrossAmounReturned() != null && tranItem.getGrossAmounReturned() > 0) {
                        TrialBalanceCOAItems trialBalCOA = new TrialBalanceCOAItems();
                        trialBalCOA.setTransactionId(transaction.getId());
                        trialBalCOA.setTransactionPurpose(transaction.getTransactionPurpose());
                        trialBalCOA.setTransactionSpecifics(tranItem.getTransactionSpecifics());
                        trialBalCOA.setTransactionParticulars(tranItem.getTransactionParticulars());
                        trialBalCOA.setDate(transaction.getTransactionDate());
                        trialBalCOA.setBranch(transaction.getTransactionBranch());
                        trialBalCOA.setOrganization(transaction.getTransactionBranchOrganization());
                        trialBalCOA.setDebitAmount(tranItem.getGrossAmounReturned());
                        genericDao.saveOrUpdate(trialBalCOA, user, entityManager);
                    }

                    // *CUST-VENDOR ADVANCE*
                    if (tranItem.getAdjustmentFromAdvanceReturned() != null
                            && tranItem.getAdjustmentFromAdvanceReturned() > 0.0) {
                        TrialBalanceVendorAdvance trialBalVenAdv = new TrialBalanceVendorAdvance();
                        trialBalVenAdv.setTransactionId(transaction.getId());
                        trialBalVenAdv.setTransactionPurpose(transaction.getTransactionPurpose());
                        trialBalVenAdv.setTransactionSpecifics(tranItem.getTransactionSpecifics());
                        trialBalVenAdv.setTransactionParticulars(tranItem.getTransactionParticulars());
                        trialBalVenAdv.setDate(transaction.getTransactionDate());
                        trialBalVenAdv.setBranch(transaction.getTransactionBranch());
                        trialBalVenAdv.setOrganization(transaction.getTransactionBranchOrganization());
                        trialBalVenAdv.setVendorType(2); // vendor type=2 means customer and =1 means vendor
                        trialBalVenAdv.setVendor(transaction.getTransactionVendorCustomer());
                        trialBalVenAdv.setCreditAmount(tranItem.getAdjustmentFromAdvanceReturned());
                        genericDao.saveOrUpdate(trialBalVenAdv, user, entityManager);
                    }

                    // output taxes
                    String txnnetamountdescriptionRet = tranItem.getTaxDescriptionReturned();
                    if (txnnetamountdescriptionRet != null && txnnetamountdescriptionRet != "") {
                        insertTrialBalOutputTaxes(transaction, tranItem, user, entityManager, false,
                                txnnetamountdescriptionRet);
                    }
                }
            }
            // insert tria_balance_customer
            insertTrialBalCustVendor(transaction, user, entityManager, true, IdosConstants.CUSTOMER,
                    transaction.getNetAmount());
        } else if (transaction.getTransactionPurpose().getId() == IdosConstants.PURCHASE_RETURNS) {
            criterias.clear();
            criterias.put("transactionRefNumber", transaction.getPaidInvoiceRefNumber());
            criterias.put("presentStatus", 1);
            Transaction previousTransaction = genericDao.getByCriteria(Transaction.class, criterias, entityManager);// original
            // sell on
            // credit tran
            long tranId = previousTransaction.getId();
            Map<String, Object> criterias1 = new HashMap<String, Object>();
            criterias1.clear();
            criterias1.put("transaction.id", tranId); // get trans_items list from original sell on credit
            criterias.put("presentStatus", 1);
            List<TransactionItems> tranItemsList = genericDao.findByCriteria(TransactionItems.class, criterias1,
                    entityManager);
            if (tranItemsList != null && tranItemsList.size() > 0) {
                for (TransactionItems tranItem : tranItemsList) {
                    if (tranItem.getGrossAmounReturned() > 0) {
                        TrialBalanceCOAItems trialBalCOA = new TrialBalanceCOAItems();
                        trialBalCOA.setTransactionId(transaction.getId());
                        trialBalCOA.setTransactionPurpose(transaction.getTransactionPurpose());
                        trialBalCOA.setTransactionSpecifics(tranItem.getTransactionSpecifics());
                        trialBalCOA.setTransactionParticulars(tranItem.getTransactionParticulars());
                        trialBalCOA.setDate(transaction.getTransactionDate());
                        trialBalCOA.setBranch(transaction.getTransactionBranch());
                        trialBalCOA.setOrganization(transaction.getTransactionBranchOrganization());
                        trialBalCOA.setCreditAmount(tranItem.getGrossAmounReturned());
                        genericDao.saveOrUpdate(trialBalCOA, user, entityManager);
                    }

                    // *CUST-VENDOR ADVANCE*
                    if (tranItem.getAdjustmentFromAdvanceReturned() != null
                            && tranItem.getAdjustmentFromAdvanceReturned() > 0.0) {
                        TrialBalanceVendorAdvance trialBalVenAdv = new TrialBalanceVendorAdvance();
                        trialBalVenAdv.setTransactionId(transaction.getId());
                        trialBalVenAdv.setTransactionPurpose(transaction.getTransactionPurpose());
                        trialBalVenAdv.setTransactionSpecifics(tranItem.getTransactionSpecifics());
                        trialBalVenAdv.setTransactionParticulars(tranItem.getTransactionParticulars());
                        trialBalVenAdv.setDate(transaction.getTransactionDate());
                        trialBalVenAdv.setBranch(transaction.getTransactionBranch());
                        trialBalVenAdv.setOrganization(transaction.getTransactionBranchOrganization());
                        trialBalVenAdv.setVendorType(1); // vendor type=2 means customer and =1 means vendor
                        trialBalVenAdv.setVendor(transaction.getTransactionVendorCustomer());
                        trialBalVenAdv.setDebitAmount(tranItem.getAdjustmentFromAdvanceReturned());
                        genericDao.saveOrUpdate(trialBalVenAdv, user, entityManager);
                    }

                    // input taxes
                    String txnnetamountdescriptionRet = tranItem.getTaxDescriptionReturned();
                    if (txnnetamountdescriptionRet != null && txnnetamountdescriptionRet != "") {
                        insertTrialBalInputTaxesForBuy(transaction, tranItem, user, entityManager, true,
                                txnnetamountdescriptionRet);
                    }
                }
            }
            // insert tria_balance_customer
            insertTrialBalCustVendor(transaction, user, entityManager, true, IdosConstants.VENDOR,
                    transaction.getNetAmount());
        } else if (transaction.getTransactionPurpose().getId() == 14) {// Transfer main cash to petty cash
            insertTrialBalCashOrPettyCash(transaction, user, entityManager, IdosConstants.CASH, true); // insert main
                                                                                                       // cash as
            // debit, so cashType=1 &
            // isCredit=false
            insertTrialBalCashOrPettyCash(transaction, user, entityManager, IdosConstants.PETTY_CASH, false); // petty
                                                                                                              // cash as
            // credit
        } else if (transaction.getTransactionPurpose().getId() == 22L) {// Withdraw cash from bank -debit from bank and
                                                                        // credit to cash account
            insertTrialBalBank(transaction, user, entityManager, true);
            insertTrialBalCashOrPettyCash(transaction, user, entityManager, IdosConstants.CASH, false);
        } else if (transaction.getTransactionPurpose().getId() == 23L) {// Deposit Cash in bank -debit from cash and
                                                                        // credit to bank account
            insertTrialBalCashOrPettyCash(transaction, user, entityManager, IdosConstants.CASH, true);
            insertTrialBalBank(transaction, user, entityManager, false);
        } else if (transaction.getTransactionPurpose().getId() == 24L) {
            insertTrialBalBank(transaction, user, entityManager, true); // from bank where
            // txn.getTransactionBranchBankAccount(),txn.getTransactionBranch()

            // To bank:
            // txn.getTransactionToBranchBankAccount(),txn.getTransactionToBranch();
            TrialBalanceBranchBank trialBalBank = new TrialBalanceBranchBank();
            trialBalBank.setTransactionId(transaction.getId());
            trialBalBank.setTransactionPurpose(transaction.getTransactionPurpose());
            trialBalBank.setDate(transaction.getTransactionDate());
            trialBalBank.setBranch(transaction.getTransactionToBranch());
            trialBalBank.setBranchBankAccounts(transaction.getTransactionToBranchBankAccount());
            trialBalBank.setOrganization(transaction.getTransactionBranchOrganization());
            trialBalBank.setDebitAmount(transaction.getNetAmount());
            genericDao.saveOrUpdate(trialBalBank, user, entityManager);
        } else if (txnPurpose == IdosConstants.CREDIT_NOTE_CUSTOMER) {
            if (transaction.getPerformaInvoice() == null || !transaction.getPerformaInvoice()) {
                criterias.clear();
                criterias.put("transaction.id", transaction.getId());
                criterias.put("presentStatus", 1);
                List<TransactionItems> tranItemsList = genericDao.findByCriteria(TransactionItems.class, criterias,
                        entityManager);
                if (tranItemsList != null && tranItemsList.size() > 0) {
                    for (TransactionItems tranItem : tranItemsList) {
                        Double discountAmt = 0d;
                        insertTrialBalCOAItems(transaction, tranItem, user, entityManager, false);
                        if (tranItem.getDiscountAmount() != null && tranItem.getDiscountAmount() > 0) {
                            discountAmt = tranItem.getDiscountAmount();
                            Specifics specifics = Specifics.findByOrganizationAndMappingID(entityManager,
                                    transaction.getTransactionBranchOrganization(), "63");
                            /* Mapping id 63 for discount allowed */
                            saveTrialBalanceCOAItem(transaction.getTransactionBranchOrganization(),
                                    transaction.getTransactionBranch(), transaction.getId(),
                                    transaction.getTransactionPurpose(),
                                    specifics, specifics.getParticularsId(), transaction.getTransactionDate(),
                                    discountAmt, user, entityManager, true);
                        }
                        /*
                         * if (!tranItem.getAdjustmentFromAdvance().equals("") &&
                         * tranItem.getAdjustmentFromAdvance() != null &&
                         * tranItem.getAdjustmentFromAdvance() > 0.0) {
                         * Double totalAdjTax = 0d;
                         * if (tranItem.getAdvAdjTax1Value() != null) {
                         * totalAdjTax += tranItem.getAdvAdjTax1Value();
                         * }
                         * if (tranItem.getAdvAdjTax2Value() != null) {
                         * totalAdjTax += tranItem.getAdvAdjTax2Value();
                         * }
                         * if (tranItem.getAdvAdjTax3Value() != null) {
                         * totalAdjTax += tranItem.getAdvAdjTax3Value();
                         * }
                         * if (tranItem.getAdvAdjTax4Value() != null) {
                         * totalAdjTax += tranItem.getAdvAdjTax4Value();
                         * }
                         * if (tranItem.getAdvAdjTax5Value() != null) {
                         * totalAdjTax += tranItem.getAdvAdjTax5Value();
                         * }
                         * if (tranItem.getAdvAdjTax6Value() != null) {
                         * totalAdjTax += tranItem.getAdvAdjTax6Value();
                         * }
                         * if (tranItem.getAdvAdjTax7Value() != null) {
                         * totalAdjTax += tranItem.getAdvAdjTax7Value();
                         * }
                         * saveTrialBalanceVendorAdvance(transaction, tranItem, user, entityManager,
                         * tranItem.getAdjustmentFromAdvance(), totalAdjTax,
                         * transaction.getTransactionVendorCustomer(), true);
                         * }
                         */
                        // ***************************TAXES*************************/
                        if (transaction.getPerformaInvoice() == null || transaction.getPerformaInvoice() != true) {
                            String txnnetamountdescription = tranItem.getTaxDescription();
                            if (user.getOrganization().getGstCountryCode() != null) {
                                saveTrialBalanceGstTaxes(transaction, tranItem, user, entityManager, false);
                            } else {
                                insertTrialBalOutputTaxes(transaction, tranItem, user, entityManager, false,
                                        txnnetamountdescription);
                            }
                        }
                    }
                }
            }
            insertTrialBalCustVendor(transaction, user, entityManager, true, IdosConstants.CUSTOMER,
                    transaction.getNetAmount());
        } else if (transaction.getTransactionPurpose().getId() == IdosConstants.REFUND_ADVANCE_RECEIVED) {
            saveMultiItemTrialBalance(transaction, user, entityManager, transaction.getTransactionVendorCustomer(),
                    null);
        } else if (transaction.getTransactionPurpose()
                .getId() == IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE) {
            // saveTrialBalanceForRefundAmount(transaction, user, entityManager,
            // transaction.getTransactionVendorCustomer(), null);
            insertTrialBalCashOrBank(transaction, user, entityManager, true);
            Double tbNetAmount = transaction.getNetAmount();
            if (transaction.getWithholdingTax() != null) {
                tbNetAmount += transaction.getWithholdingTax();
            }
            insertTrialBalCustVendor(transaction, user, entityManager, false, IdosConstants.CUSTOMER, tbNetAmount);
            if (transaction.getWithholdingTax() != null && transaction.getWithholdingTax() != 0.0) {
                Specifics specificsMap = coaService.getSpecificsForMapping(user, "8", entityManager);
                if (specificsMap == null) {
                    throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                            "COA mapping is not found for mapping 8", "TDS COA mapping not found for 8");
                }
                BranchTaxes branchTax = new BranchTaxes();
                branchTax.setBranch(transaction.getTransactionBranch());
                branchTax.setOrganization(user.getOrganization());
                branchTax.setId(specificsMap.getId());
                TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
                trialBalTaxes.setBranchTaxes(branchTax);
                trialBalTaxes.setTransactionId(transaction.getId());
                trialBalTaxes.setTransactionPurpose(transaction.getTransactionPurpose());
                trialBalTaxes.setDate(transaction.getTransactionDate());
                trialBalTaxes.setTaxType((int) IdosConstants.INPUT_TDS);
                trialBalTaxes.setBranch(transaction.getTransactionBranch());
                trialBalTaxes.setOrganization(user.getOrganization());
                trialBalTaxes.setCreditAmount(transaction.getWithholdingTax());
                // TODO set item
                genericDao.saveOrUpdate(trialBalTaxes, user, entityManager);
            }
        } else if (txnPurpose == IdosConstants.CANCEL_INVOICE) {
            // List<TransactionItems> tranItemsList = transaction.getTransactionItems();
            criterias.clear();
            criterias.put("transaction.id", transaction.getId());
            criterias.put("presentStatus", 1);
            List<TransactionItems> tranItemsList = genericDao.findByCriteria(TransactionItems.class, criterias,
                    entityManager);
            if (tranItemsList != null) {
                for (TransactionItems tranItem : tranItemsList) {
                    insertTrialBalCOAItems(transaction, tranItem, user, entityManager, false);
                    String txnnetamountdescription = tranItem.getTaxDescription();
                    if (user.getOrganization().getGstCountryCode() != null) {
                        saveTrialBalanceGstTaxes(transaction, tranItem, user, entityManager, false);
                    } else {
                        insertTrialBalOutputTaxes(transaction, tranItem, user, entityManager, false,
                                txnnetamountdescription);
                    }
                }
            }
            if ((long) transaction.getTypeIdentifier() == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW) {
                insertTrialBalCashOrBank(transaction, user, entityManager, true);
            } else {
                insertTrialBalCustVendor(transaction, user, entityManager, true, IdosConstants.CUSTOMER,
                        transaction.getNetAmount());
            }
        } else if (txnPurpose == IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER) {
            Double tbNetAmount = transaction.getNetAmount();
            if (transaction.getWithholdingTax() != null) {
                tbNetAmount += transaction.getWithholdingTax();
            }
            if (transaction.getAvailableDiscountAmountForTxn() != null) {
                tbNetAmount += transaction.getAvailableDiscountAmountForTxn();
            }
            insertTrialBalCustVendor(transaction, user, entityManager, true, IdosConstants.CUSTOMER, tbNetAmount);
            // saveTrialBalanceVendorAdvance(transaction, null, user, entityManager,
            // transaction.getGrossAmount(), transaction.getTotalTax(),
            // transaction.getTransactionVendorCustomer(), false);
            insertTrialBalCashOrBank(transaction, user, entityManager, false);
            if (transaction.getWithholdingTax() != null && transaction.getWithholdingTax() > 0.0) {
                Specifics specificsMap = coaService.getSpecificsForMapping(user, "8", entityManager);
                if (specificsMap == null) {
                    throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                            "COA is not found for mapping id 8",
                            "TDS COA mapping not found for: withholding tax (TDS) on payments received from customers and others");
                }
                saveTrialBalanceTDS(user, entityManager, transaction.getTransactionBranch(),
                        transaction.getTransactionVendorCustomer(), transaction.getId(),
                        transaction.getTransactionPurpose(), transaction.getTransactionDate(), specificsMap,
                        transaction.getWithholdingTax(), false, IdosConstants.INPUT_TDS, 1);
            }
            if (transaction.getAvailableDiscountAmountForTxn() != null
                    && transaction.getAvailableDiscountAmountForTxn() > 0) {
                Specifics specificsMap = coaService.getSpecificsForMapping(user, "63", entityManager);
                if (specificsMap == null) {
                    throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                            "COA mapping is not found for item: 63",
                            "COA mapping is not found for, mapping: Discount Allowed");
                }
                saveTrialBalanceCOAItem(transaction.getTransactionBranchOrganization(),
                        transaction.getTransactionBranch(), transaction.getId(), transaction.getTransactionPurpose(),
                        specificsMap, specificsMap.getParticularsId(), transaction.getTransactionDate(),
                        transaction.getAvailableDiscountAmountForTxn(), user, entityManager, false);
            }
        } else if (txnPurpose == IdosConstants.PAY_VENDOR_SUPPLIER) {
            Double tbNetAmount = transaction.getNetAmount();
            if (transaction.getAvailableDiscountAmountForTxn() != null) {
                tbNetAmount += transaction.getAvailableDiscountAmountForTxn();
            }
            insertTrialBalCustVendor(transaction, user, entityManager, false, IdosConstants.VENDOR, tbNetAmount);
            // saveTrialBalanceVendorAdvance(transaction, null, user, entityManager,
            // transaction.getGrossAmount(), transaction.getTotalTax(),
            // transaction.getTransactionVendorCustomer(), false);
            insertTrialBalCashOrBank(transaction, user, entityManager, true);
            if (transaction.getAvailableDiscountAmountForTxn() != null
                    && transaction.getAvailableDiscountAmountForTxn() > 0.0) {
                Specifics specificsMap = coaService.getSpecificsForMapping(user, "64", entityManager);
                if (specificsMap == null) {
                    throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                            "COA mapping is not found for item: 64",
                            "COA mapping is not found for, mapping : Discount Received");
                }
                saveTrialBalanceCOAItem(transaction.getTransactionBranchOrganization(),
                        transaction.getTransactionBranch(), transaction.getId(), transaction.getTransactionPurpose(),
                        specificsMap, specificsMap.getParticularsId(), transaction.getTransactionDate(),
                        transaction.getAvailableDiscountAmountForTxn(), user, entityManager, true);
            }
        }
    }

    @Deprecated
    private void insertTrialBalCOAItems(Transaction transaction, Users user, EntityManager entityManager,
            boolean isCredit) {
        TrialBalanceCOAItems trialBalCOA = new TrialBalanceCOAItems();
        trialBalCOA.setTransactionId(transaction.getId());
        trialBalCOA.setTransactionPurpose(transaction.getTransactionPurpose());
        trialBalCOA.setTransactionSpecifics(transaction.getTransactionSpecifics());
        trialBalCOA.setTransactionParticulars(transaction.getTransactionParticulars());
        trialBalCOA.setDate(transaction.getTransactionDate());
        trialBalCOA.setBranch(transaction.getTransactionBranch());
        trialBalCOA.setOrganization(transaction.getTransactionBranchOrganization());
        if (isCredit) {
            trialBalCOA.setCreditAmount(transaction.getGrossAmount());
        } else {
            trialBalCOA.setDebitAmount(transaction.getGrossAmount() - transaction.getFrieghtCharges()); // for petty
                                                                                                        // cash
        }
        genericDao.saveOrUpdate(trialBalCOA, user, entityManager);
    }

    @Deprecated
    private void insertTrialBalCOAItems(Transaction transaction, TransactionItems txnItem, Users user,
            EntityManager entityManager,
            boolean isCredit) {
        TrialBalanceCOAItems trialBalCOA = new TrialBalanceCOAItems();
        trialBalCOA.setTransactionId(transaction.getId());
        trialBalCOA.setTransactionPurpose(transaction.getTransactionPurpose());
        trialBalCOA.setTransactionSpecifics(txnItem.getTransactionSpecifics());
        trialBalCOA.setTransactionParticulars(txnItem.getTransactionParticulars());
        trialBalCOA.setDate(transaction.getTransactionDate());
        trialBalCOA.setBranch(transaction.getTransactionBranch());
        trialBalCOA.setOrganization(transaction.getTransactionBranchOrganization());
        Double disAmt = txnItem.getDiscountAmount() != null ? txnItem.getDiscountAmount() : 0.0;
        if (isCredit) {
            trialBalCOA.setCreditAmount(txnItem.getGrossAmount() + disAmt);
        } else {
            trialBalCOA.setDebitAmount(txnItem.getGrossAmount() + disAmt);
        }
        genericDao.saveOrUpdate(trialBalCOA, user, entityManager);
    }

    private void insertTrialBalFreightInCOA(Transaction transaction, Users user, EntityManager entityManager,
            boolean isCredit) {
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.clear();
        criterias.put("particularsId.id", transaction.getTransactionSpecifics().getParticularsId().getId());
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("identificationForDataValid", "26"); // coming from configCOA.scala
        criterias.put("presentStatus", 1);
        List<Specifics> freightspec = genericDao.findByCriteria(Specifics.class, criterias, entityManager);
        if (freightspec != null && freightspec.size() > 0) {
            TrialBalanceCOAItems trialBalCOA = new TrialBalanceCOAItems();
            trialBalCOA.setTransactionSpecifics(freightspec.get(0));
            trialBalCOA.setTransactionId(transaction.getId());
            trialBalCOA.setTransactionPurpose(transaction.getTransactionPurpose());
            trialBalCOA.setTransactionParticulars(transaction.getTransactionSpecifics().getParticularsId());
            trialBalCOA.setDate(transaction.getTransactionDate());
            trialBalCOA.setBranch(transaction.getTransactionBranch());
            trialBalCOA.setOrganization(transaction.getTransactionBranchOrganization());
            if (isCredit) {
                trialBalCOA.setCreditAmount(transaction.getFrieghtCharges()); // for sell transactions it is credit and
                                                                              // we use gross amount
            } else {
                trialBalCOA.setDebitAmount(transaction.getFrieghtCharges());
            }
            genericDao.saveOrUpdate(trialBalCOA, user, entityManager);
        }
    }

    private void insertTrialBalCustVendor(Transaction transaction, Users user, EntityManager entityManager,
            boolean isCredit,
            int custVenType, Double amount) {
        // TrialBalanceCustomerVendor pojo will hold sell/buy effect on customer(asset)
        // or vendor(liability)
        // Since it is credit txn, bank/cash is not affected, instead it is due from
        // customer, so debit to customer
        TrialBalanceCustomerVendor trialBalCustVendor = new TrialBalanceCustomerVendor(); // sell on credit
        trialBalCustVendor.setTransactionId(transaction.getId());
        trialBalCustVendor.setTransactionPurpose(transaction.getTransactionPurpose());
        trialBalCustVendor.setTransactionParticulars(transaction.getTransactionParticulars());
        trialBalCustVendor.setTransactionSpecifics(transaction.getTransactionSpecifics());
        trialBalCustVendor.setDate(transaction.getTransactionDate());
        trialBalCustVendor.setBranch(transaction.getTransactionBranch());
        trialBalCustVendor.setOrganization(transaction.getTransactionBranchOrganization());
        trialBalCustVendor.setVendorType(custVenType); // vendor type=2 means customer and =1 means vendor
        trialBalCustVendor.setVendor(transaction.getTransactionVendorCustomer());
        if (isCredit) {
            trialBalCustVendor.setCreditAmount(amount);
        } else {
            trialBalCustVendor.setDebitAmount(amount);
        }
        genericDao.saveOrUpdate(trialBalCustVendor, user, entityManager);
    }

    private void insertTrialBalCashOrPettyCash(Transaction transaction, Users user, EntityManager entityManager,
            final short cashType, boolean isCredit) {
        TrialBalanceBranchCash trialBalCash = new TrialBalanceBranchCash();
        trialBalCash.setTransactionId(transaction.getId());
        trialBalCash.setTransactionPurpose(transaction.getTransactionPurpose());
        trialBalCash.setDate(transaction.getTransactionDate());
        trialBalCash.setBranch(transaction.getTransactionBranch());
        trialBalCash.setOrganization(transaction.getTransactionBranchOrganization());
        trialBalCash.setCashType((int) cashType);
        if (isCredit) {
            trialBalCash.setCreditAmount(transaction.getNetAmount()); // normal cash and not petty cash
        } else {
            trialBalCash.setDebitAmount(transaction.getNetAmount());
        }
        if (!transaction.getTransactionBranch().getBranchDepositKeys().isEmpty()) {
            trialBalCash.setBranchDepositBoxKey(transaction.getTransactionBranch().getBranchDepositKeys().get(0));
        }
        genericDao.saveOrUpdate(trialBalCash, user, entityManager);
    }

    private void insertTrialBalBank(Transaction transaction, Users user, EntityManager entityManager, boolean isCredit)
            throws IDOSException {
        // TrialBalanceBranchBank pojo will have info for branch cash transaction and
        // bank transactions
        // for sell on cash it will affect debit amount, we will put netamount which is
        // net=gross-adv adjustment+taxes
        TrialBalanceBranchBank trialBalBank = new TrialBalanceBranchBank(); // will affect only for sell on cash if bank
                                                                            // check or DD
        trialBalBank.setTransactionId(transaction.getId());
        trialBalBank.setTransactionPurpose(transaction.getTransactionPurpose());
        trialBalBank.setDate(transaction.getTransactionDate());
        trialBalBank.setBranch(transaction.getTransactionBranch());
        if (transaction.getTransactionBranchBankAccount() == null) {
            throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                    "Bank account is not found", IdosConstants.RECORD_NOT_FOUND);
        }
        trialBalBank.setBranchBankAccounts(transaction.getTransactionBranchBankAccount());
        trialBalBank.setOrganization(transaction.getTransactionBranchOrganization());

        if (isCredit) {
            trialBalBank.setCreditAmount(transaction.getNetAmount());
        } else {
            trialBalBank.setDebitAmount(transaction.getNetAmount());
        }
        genericDao.saveOrUpdate(trialBalBank, user, entityManager);
    }

    private void insertTrialBalCashOrBank(Transaction transaction, Users user, EntityManager entityManager,
            boolean isCredit)
            throws IDOSException {
        if (transaction.getReceiptDetailsType() == 1) {
            insertTrialBalCashOrPettyCash(transaction, user, entityManager, IdosConstants.CASH, isCredit); // cashType=1,
                                                                                                           // as it is
            // normal cash (not
            // petty cash)
        } else if (transaction.getReceiptDetailsType() == 2) {
            insertTrialBalBank(transaction, user, entityManager, isCredit);
        }
    }

    private void insertTrialBalInputTaxesForBuy(Transaction transaction, TransactionItems tranItem, Users user,
            EntityManager entityManager, boolean isCredit, String alltaxes) {
        // get all taxes for this branch and organization
        Map<String, Object> criteriastax = new HashMap<String, Object>();
        criteriastax.clear();
        criteriastax.put("branch.id", transaction.getTransactionBranch().getId());
        criteriastax.put("organization.id", user.getOrganization().getId());
        criteriastax.put("taxType", 1); // type=1 input taxes
        criteriastax.put("presentStatus", 1);
        List<BranchTaxes> branchTaxesList = genericDao.findByCriteria(BranchTaxes.class, criteriastax, entityManager);

        // String alltaxes = tranItem.getTaxDescription();
        String[] taxes = alltaxes.split(",");
        // Now if tax is defined for branch then get its value
        for (int i = 0; i < taxes.length; i++) {
            String[] taxNameValue = taxes[i].split("=");
            if (taxNameValue.length > 1 && taxNameValue[0] != null && !"".equals(taxNameValue[0])) {
                for (BranchTaxes bnchTaxes : branchTaxesList) {
                    if (taxNameValue[0].equalsIgnoreCase(bnchTaxes.getTaxName())) {
                        TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
                        trialBalTaxes.setTransactionId(transaction.getId());
                        trialBalTaxes.setTransactionPurpose(transaction.getTransactionPurpose());
                        trialBalTaxes.setDate(transaction.getTransactionDate());
                        trialBalTaxes.setBranchTaxes(bnchTaxes);
                        trialBalTaxes.setTaxType((int) IdosConstants.INPUT_TAX); // Input taxes
                        trialBalTaxes.setBranch(transaction.getTransactionBranch());
                        trialBalTaxes.setOrganization(transaction.getTransactionBranchOrganization());
                        if (isCredit) {
                            trialBalTaxes.setCreditAmount(new Double(taxNameValue[1]));
                        } else {
                            trialBalTaxes.setDebitAmount(new Double(taxNameValue[1]));
                        }
                        trialBalTaxes.setTransactionSpecifics(tranItem.getTransactionSpecifics());
                        trialBalTaxes.setTransactionParticulars(tranItem.getTransactionParticulars());
                        genericDao.saveOrUpdate(trialBalTaxes, user, entityManager);
                    }
                }
            }
        }
    }

    /***
     * not in use
     * private void insertTrialBalInputTaxes(Transaction transaction,Users
     * user,EntityManager entityManager,boolean isCredit){
     * //get all taxes for this branch and organization
     * Map<String, Object> criteriastax=new HashMap<String, Object>();
     * criteriastax.clear();
     * criteriastax.put("branch.id",transaction.getTransactionBranch().getId());
     * criteriastax.put("organization.id",user.getOrganization().getId());
     * criteriastax.put("taxType", 1); //type=1 input taxes
     * List<BranchTaxes>
     * branchTaxesList=genericDao.findByCriteria(BranchTaxes.class, criteriastax,
     * entityManager);
     * 
     * String taxNames[] = new String[6]; //Need this for trialbalancetaxes
     * double taxValues[] = new double[6];
     * if(transaction.getTaxName1()!=null && transaction.getTaxValue1()>0.0){
     * taxNames[0]=transaction.getTaxName1();
     * taxValues[0]=transaction.getTaxValue1();
     * }
     * if(transaction.getTaxName2()!=null && transaction.getTaxValue2()>0.0){
     * taxNames[1]=transaction.getTaxName2();
     * taxValues[1]=transaction.getTaxValue2();
     * }
     * if(transaction.getTaxName3()!=null && transaction.getTaxValue3()>0.0){
     * taxNames[2]=transaction.getTaxName3();
     * taxValues[2]=transaction.getTaxValue3();
     * }
     * if(transaction.getTaxName4()!=null && transaction.getTaxValue4()>0.0){
     * taxNames[3]=transaction.getTaxName4();
     * taxValues[3]=transaction.getTaxValue4();
     * }
     * if(transaction.getTaxName5()!=null && transaction.getTaxValue5()>0.0){
     * taxNames[4]=transaction.getTaxName5();
     * taxValues[4]=transaction.getTaxValue5();
     * }
     * if(transaction.getTaxName6()!=null && transaction.getTaxValue6()>0.0){
     * taxNames[5]=transaction.getTaxName6();
     * taxValues[5]=transaction.getTaxValue6();
     * }
     * //Now if tax is defined for branch then get its value
     * for(int i=0; i<taxNames.length; i++){
     * if(taxNames[i]!=null){
     * for(BranchTaxes bnchTaxes:branchTaxesList){
     * if(taxNames[i].equalsIgnoreCase(bnchTaxes.getTaxName())){
     * TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
     * trialBalTaxes.setTransactionId(transaction.getId());
     * trialBalTaxes.setTransactionPurpose(transaction.getTransactionPurpose());
     * trialBalTaxes.setDate(transaction.getTransactionDate());
     * trialBalTaxes.setBranchTaxes(bnchTaxes);
     * trialBalTaxes.setTaxType(1); //Input taxes
     * trialBalTaxes.setBranch(transaction.getTransactionBranch());
     * trialBalTaxes.setOrganization(transaction.getTransactionBranchOrganization());
     * if(isCredit){
     * trialBalTaxes.setCreditAmount(taxValues[i]);
     * }else{
     * trialBalTaxes.setDebitAmount(taxValues[i]);
     * }
     * genericDao.saveOrUpdate(trialBalTaxes, user, entityManager);
     * }
     * }
     * }
     * }
     * 
     * }
     **/
    private void insertTrialBalOutputTaxes(Transaction transaction, TransactionItems tranItem, Users user,
            EntityManager entityManager, boolean isCredit, String txnnetamountdescription) {
        // VAT 1(+10.5%):52.5,
        // get all taxes for this branch and organization
        // VAT 1(+10.0%):5.0,Net Tax:5.00,
        String taxNames[] = new String[6]; // Need this for trialbalancetaxes
        double taxValues[] = new double[6];
        if (txnnetamountdescription != null && !txnnetamountdescription.equals("")
                && !txnnetamountdescription.contains("undefined")) {
            String inputtaxvalarr[] = txnnetamountdescription.split(",");
            for (int i = 0; i < inputtaxvalarr.length; i++) {
                switch (i) {
                    case 0:
                        if (inputtaxvalarr.length > i) {
                            String inputtaxvalwithstrarr[] = inputtaxvalarr[i].split(":");
                            String str = inputtaxvalwithstrarr[0];
                            str = str.replaceAll("\\(.*?\\) ?", "");
                            taxNames[0] = str;
                            taxValues[0] = Double.valueOf(inputtaxvalwithstrarr[1]);
                        }
                        break;
                    case 1:
                        if (inputtaxvalarr.length > i) {
                            String inputtaxvalwithstrarr[] = inputtaxvalarr[i].split(":");
                            taxNames[1] = inputtaxvalwithstrarr[0].replaceAll("\\(.*?\\) ?", "");
                            taxValues[1] = Double.valueOf(inputtaxvalwithstrarr[1]);
                        }
                        break;
                    case 2:
                        if (inputtaxvalarr.length > i) {
                            String inputtaxvalwithstrarr[] = inputtaxvalarr[i].split(":");
                            taxNames[2] = inputtaxvalwithstrarr[0].replaceAll("\\(.*?\\) ?", "");
                            taxValues[2] = Double.valueOf(inputtaxvalwithstrarr[1]);
                        }
                        break;
                    case 3:
                        if (inputtaxvalarr.length > i) {
                            String inputtaxvalwithstrarr[] = inputtaxvalarr[i].split(":");
                            taxNames[3] = inputtaxvalwithstrarr[0].replaceAll("\\(.*?\\) ?", "");
                            taxValues[3] = Double.valueOf(inputtaxvalwithstrarr[1]);
                        }
                        break;
                    case 4:
                        if (inputtaxvalarr.length > i) {
                            String inputtaxvalwithstrarr[] = inputtaxvalarr[i].split(":");
                            taxNames[4] = inputtaxvalwithstrarr[0].replaceAll("\\(.*?\\) ?", "");
                            taxValues[4] = Double.valueOf(inputtaxvalwithstrarr[1]);
                        }
                        break;
                    case 5:
                        if (inputtaxvalarr.length > i) {
                            String inputtaxvalwithstrarr[] = inputtaxvalarr[i].split(":");
                            taxNames[5] = inputtaxvalwithstrarr[0].replaceAll("\\(.*?\\) ?", "");
                            taxValues[5] = Double.valueOf(inputtaxvalwithstrarr[1]);
                        }
                        break;
                    case 6:
                        if (inputtaxvalarr.length > i) {
                            String inputtaxvalwithstrarr[] = inputtaxvalarr[i].split(":");
                            taxNames[6] = inputtaxvalwithstrarr[0].replaceAll("\\(.*?\\) ?", "");
                            taxValues[6] = Double.valueOf(inputtaxvalwithstrarr[1]);
                        }
                        break;
                }
            }
        }

        ArrayList inparamList = new ArrayList(2);
        inparamList.add(transaction.getTransactionBranch().getId());
        inparamList.add(user.getOrganization().getId());
        List<BranchTaxes> branchTaxesList = genericDao.queryWithParams(OUTPUT_TAX_HQL, entityManager, inparamList);

        // Now if tax is defined for branch then get its value
        for (int i = 0; i < taxNames.length; i++) {
            if (taxNames[i] != null) {
                for (BranchTaxes bnchTaxes : branchTaxesList) {
                    if (taxNames[i].equalsIgnoreCase(bnchTaxes.getTaxName())) {
                        TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
                        trialBalTaxes.setTransactionId(transaction.getId());
                        trialBalTaxes.setTransactionPurpose(transaction.getTransactionPurpose());
                        trialBalTaxes.setDate(transaction.getTransactionDate());
                        trialBalTaxes.setBranchTaxes(bnchTaxes);
                        trialBalTaxes.setTaxType(bnchTaxes.getTaxType()); // output taxes
                        trialBalTaxes.setBranch(transaction.getTransactionBranch());
                        trialBalTaxes.setOrganization(transaction.getTransactionBranchOrganization());
                        if (isCredit) {
                            trialBalTaxes.setCreditAmount(taxValues[i]);
                        } else {
                            trialBalTaxes.setDebitAmount(taxValues[i]);
                        }
                        trialBalTaxes.setTransactionSpecifics(tranItem.getTransactionSpecifics());
                        trialBalTaxes.setTransactionParticulars(tranItem.getTransactionParticulars());
                        genericDao.saveOrUpdate(trialBalTaxes, user, entityManager);
                    }
                }
            }
        }
    }

    @Override
    public void saveMultiItemTrialBalance(Transaction transaction, Users user, EntityManager entityManager,
            Vendor vendor,
            Long tdsMappingID) throws IDOSException {
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("transaction.id", transaction.getId());
        criterias.put("presentStatus", 1);
        List<TransactionItems> tranItemsList = genericDao.findByCriteria(TransactionItems.class, criterias,
                entityManager);
        if (transaction.getTransactionPurpose().getId() == IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER) {
            for (TransactionItems tranItem : tranItemsList) {
                Double totalAdv = 0d;
                if (tranItem.getWithholdingAmount() != null) {
                    totalAdv = tranItem.getWithholdingAmount();
                }
                // if(tranItem.getTotalTax() != null){
                // totalAdv += tranItem.getTotalTax();
                // }
                totalAdv += tranItem.getAvailableAdvance();
                saveTrialBalanceVendorAdvance(transaction, tranItem, user, entityManager, totalAdv,
                        tranItem.getTotalTax(), vendor,
                        true);
                if (IdosConstants.PAYMODE_CASH == transaction.getReceiptDetailsType()) {
                    saveTrialBalanceBranchCash(user, entityManager, transaction, tranItem.getAvailableAdvance(), false);
                } else if (IdosConstants.PAYMODE_BANK == transaction.getReceiptDetailsType()) {
                    saveTrialBalanceBranchBank(user, entityManager, transaction, tranItem.getAvailableAdvance(), false);
                }
                if (tranItem.getWithholdingAmount() != null && tranItem.getWithholdingAmount() > 0.0) {
                    saveTrialBalanceTDS(user, entityManager, transaction.getTransactionBranch(),
                            tranItem.getTransactionId().getTransactionVendorCustomer(), transaction.getId(),
                            transaction.getTransactionPurpose(), transaction.getTransactionDate(),
                            tranItem.getTransactionSpecifics(), tranItem.getWithholdingAmount(), false,
                            IdosConstants.INPUT_TDS, 1);
                }
                saveTrialBalanceGstTaxes(transaction, tranItem, user, entityManager, true);
                // insertTrialBalOutputTaxes(transaction, tranItem, user, entityManager, true,
                // tranItem.getTaxDescription());
            }

        } else if (transaction.getTransactionPurpose().getId() == IdosConstants.REFUND_ADVANCE_RECEIVED) {
            for (TransactionItems tranItem : tranItemsList) {
                Double totalRefund = 0d;
                if (tranItem.getWithholdingAmount() != null) {
                    totalRefund = tranItem.getWithholdingAmount();
                }
                totalRefund += tranItem.getNetAmount();
                saveTrialBalanceVendorAdvance(transaction, tranItem, user, entityManager, totalRefund,
                        tranItem.getTotalTax(),
                        vendor, false);
                if (IdosConstants.PAYMODE_CASH == transaction.getReceiptDetailsType()) {
                    saveTrialBalanceBranchCash(user, entityManager, transaction, tranItem.getNetAmount(), true);
                } else if (IdosConstants.PAYMODE_BANK == transaction.getReceiptDetailsType()) {
                    saveTrialBalanceBranchBank(user, entityManager, transaction, tranItem.getNetAmount(), true);
                }
                if (tranItem.getWithholdingAmount() != null && tranItem.getWithholdingAmount() > 0.0) {
                    saveTrialBalanceTDS(user, entityManager, transaction.getTransactionBranch(),
                            tranItem.getTransactionId().getTransactionVendorCustomer(), transaction.getId(),
                            transaction.getTransactionPurpose(), transaction.getTransactionDate(),
                            tranItem.getTransactionSpecifics(), tranItem.getWithholdingAmount(), true,
                            IdosConstants.INPUT_TDS, 1);
                }
                // insertTrialBalOutputTaxes(transaction, tranItem, user, entityManager, false,
                // tranItem.getTaxDescription());
                saveTrialBalanceGstTaxes(transaction, tranItem, user, entityManager, false);
            }
        }
    }

    public void saveTrialBalanceVendorAdvance(Transaction transaction, TransactionItems txnItem, Users user,
            EntityManager entityManager, Double amount, Double taxAmount, Vendor vendor, boolean isCredit) {
        TrialBalanceVendorAdvance trialBalVenAdv = new TrialBalanceVendorAdvance();
        trialBalVenAdv.setTransactionId(transaction.getId());
        trialBalVenAdv.setTransactionPurpose(transaction.getTransactionPurpose());
        trialBalVenAdv.setTransactionParticulars(txnItem.getTransactionParticulars());
        trialBalVenAdv.setTransactionSpecifics(txnItem.getTransactionSpecifics());
        trialBalVenAdv.setDate(transaction.getTransactionDate());
        trialBalVenAdv.setBranch(transaction.getTransactionBranch());
        trialBalVenAdv.setOrganization(transaction.getTransactionBranchOrganization());
        trialBalVenAdv.setVendorType(vendor.getType());
        trialBalVenAdv.setVendor(vendor);
        if (isCredit) {
            trialBalVenAdv.setCreditAmount(amount);
            trialBalVenAdv.setDebitAmount(taxAmount);
        } else {
            trialBalVenAdv.setCreditAmount(taxAmount);
            trialBalVenAdv.setDebitAmount(amount);
        }
        genericDao.saveOrUpdate(trialBalVenAdv, user, entityManager);
    }

    public void saveTrialBalanceBranchCash(Users user, EntityManager entityManager, Transaction txn, Double amount,
            boolean isCredit) {
        TrialBalanceBranchCash trialBalCash = new TrialBalanceBranchCash();
        trialBalCash.setTransactionId(txn.getId());
        trialBalCash.setTransactionPurpose(txn.getTransactionPurpose());
        trialBalCash.setDate(txn.getTransactionDate());
        trialBalCash.setBranch(txn.getTransactionBranch());
        trialBalCash.setOrganization(txn.getTransactionBranchOrganization());
        if (isCredit) {
            trialBalCash.setCreditAmount(amount);
        } else {
            trialBalCash.setDebitAmount(amount);
        }
        if (!txn.getTransactionBranch().getBranchDepositKeys().isEmpty()) {
            trialBalCash.setBranchDepositBoxKey(txn.getTransactionBranch().getBranchDepositKeys().get(0));
        }
        trialBalCash.setCashType(new Integer(IdosConstants.CASH));
        genericDao.saveOrUpdate(trialBalCash, user, entityManager);
    }

    public void saveTrialBalanceBranchBank(Users user, EntityManager entityManager, Transaction txn, Double amount,
            boolean isCredit) {
        TrialBalanceBranchBank trialBalBank = new TrialBalanceBranchBank();
        trialBalBank.setTransactionId(txn.getId());
        trialBalBank.setTransactionPurpose(txn.getTransactionPurpose());
        trialBalBank.setDate(txn.getTransactionDate());
        trialBalBank.setBranch(txn.getTransactionBranch());
        trialBalBank.setOrganization(txn.getTransactionBranchOrganization());
        if (isCredit) {
            trialBalBank.setCreditAmount(amount);
        } else {
            trialBalBank.setDebitAmount(amount);
        }
        trialBalBank.setBranchBankAccounts(txn.getTransactionBranchBankAccount());
        genericDao.saveOrUpdate(trialBalBank, user, entityManager);
    }

    /**
     * @param user
     * @param entityManager
     * @param txn
     * @param amount
     * @param isCredit
     * @param taxType       3 - withholding
     */
    public long saveTrialBalanceTDS(Users user, EntityManager entityManager, Branch branch, Vendor vendor, Long txnid,
            TransactionPurpose txnPurpose, Date date, Specifics specific, Double amount, boolean isCredit, int taxType,
            int presentStatus) throws IDOSException {
        BranchTaxes branchTax = null;
        if (specific == null) {
            throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                    "Item is not found", "Item is not found");
        }
        if (specific.getAccountCodeHirarchy().startsWith("/2")) {
            branchTax = getTdsType4ExpenseByMappedSpecific(user, entityManager, specific, vendor, branch, txnPurpose,
                    null);
        } else if (specific.getAccountCodeHirarchy().startsWith("/1")
                || IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER == txnPurpose.getId()) {
            Specifics specificsMap = coaService.getSpecificsForMapping(user, "8", entityManager);
            if (specificsMap == null) {
                throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                        "COA is not found for mapping id 8",
                        "TDS COA mapping not found for,: withholding tax (TDS) on payments received from customers and others");
            }
            branchTax = new BranchTaxes();
            branchTax.setBranch(branch);
            branchTax.setOrganization(branch.getOrganization());
            branchTax.setId(specificsMap.getId());
            branchTax.setTaxType(taxType);
        }
        TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
        trialBalTaxes.setBranchTaxes(branchTax);
        trialBalTaxes.setTransactionId(txnid);
        trialBalTaxes.setTransactionPurpose(txnPurpose);
        trialBalTaxes.setDate(date);
        trialBalTaxes.setTaxType(branchTax.getTaxType());
        trialBalTaxes.setBranch(branch);
        trialBalTaxes.setOrganization(user.getOrganization());
        if (isCredit) {
            trialBalTaxes.setCreditAmount(amount);
        } else {
            trialBalTaxes.setDebitAmount(amount);
        }
        trialBalTaxes.setPresentStatus(presentStatus);
        trialBalTaxes.setTransactionSpecifics(specific);
        trialBalTaxes.setTransactionParticulars(specific.getParticularsId());
        genericDao.saveOrUpdate(trialBalTaxes, user, entityManager);
        return trialBalTaxes.getId();
    }

    private void saveTrialBalanceGstTaxes(Transaction txn, TransactionItems tranItem, Users user,
            EntityManager entityManager,
            boolean isCredit) {
        Long tanPurpose = txn.getTransactionPurpose().getId();
        if (tranItem.getTaxName1() != null && tranItem.getTaxName1().indexOf("SGST") != -1) {
            BranchTaxes branchTaxes = BranchTaxes.findById(tranItem.getTax1ID());
            Double taxAmount = tranItem.getTaxValue1();
            if (tranItem.getAdvAdjTax1Value() != null) {
                taxAmount = taxAmount - tranItem.getAdvAdjTax1Value();
            }
            saveTrialBalanceGstTax(txn, user, entityManager, isCredit, taxAmount, branchTaxes, tranItem);

            if ((tanPurpose == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                    || tanPurpose == IdosConstants.BUY_ON_CREDIT_PAY_LATER
                    || tanPurpose == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT)
                    && branchTaxes.getTaxType() == IdosConstants.RCM_SGST_IN) {
                BranchTaxes branchTaxesRcm = BranchTaxes.findByRateType(entityManager, branchTaxes.getBranch().getId(),
                        branchTaxes.getOrganization().getId(), IdosConstants.RCM_SGST_OUTPUT, branchTaxes.getTaxRate());
                saveTrialBalanceGstTax(txn, user, entityManager, true, taxAmount, branchTaxesRcm, tranItem);
            }
        }

        if (tranItem.getTaxName2() != null && tranItem.getTaxName2().indexOf("CGST") != -1) {
            BranchTaxes branchTaxes = BranchTaxes.findById(tranItem.getTax2ID());
            Double taxAmount = tranItem.getTaxValue2();
            if (tranItem.getAdvAdjTax2Value() != null) {
                taxAmount = taxAmount - tranItem.getAdvAdjTax2Value();
            }
            saveTrialBalanceGstTax(txn, user, entityManager, isCredit, taxAmount, branchTaxes, tranItem);
            if ((tanPurpose == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                    || tanPurpose == IdosConstants.BUY_ON_CREDIT_PAY_LATER
                    || tanPurpose == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT)
                    && branchTaxes.getTaxType() == IdosConstants.RCM_CGST_IN) {
                BranchTaxes branchTaxesRcm = BranchTaxes.findByRateType(entityManager, branchTaxes.getBranch().getId(),
                        branchTaxes.getOrganization().getId(), IdosConstants.RCM_CGST_OUTPUT, branchTaxes.getTaxRate());
                saveTrialBalanceGstTax(txn, user, entityManager, true, taxAmount, branchTaxesRcm, tranItem);
            }
        }

        if (tranItem.getTaxName3() != null && tranItem.getTaxName3().indexOf("IGST") != -1) {
            BranchTaxes branchTaxes = BranchTaxes.findById(tranItem.getTax3ID());
            Double taxAmount = tranItem.getTaxValue3();
            if (tranItem.getAdvAdjTax3Value() != null) {
                taxAmount = taxAmount - tranItem.getAdvAdjTax3Value();
            }
            saveTrialBalanceGstTax(txn, user, entityManager, isCredit, taxAmount, branchTaxes, tranItem);

            if ((tanPurpose == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                    || tanPurpose == IdosConstants.BUY_ON_CREDIT_PAY_LATER
                    || tanPurpose == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT)
                    && branchTaxes.getTaxType() == IdosConstants.RCM_IGST_IN) {
                BranchTaxes branchTaxesRcm = BranchTaxes.findByRateType(entityManager, branchTaxes.getBranch().getId(),
                        branchTaxes.getOrganization().getId(), IdosConstants.RCM_IGST_OUTPUT, branchTaxes.getTaxRate());
                saveTrialBalanceGstTax(txn, user, entityManager, true, taxAmount, branchTaxesRcm, tranItem);
            }
        }

        if (tranItem.getTaxName4() != null && tranItem.getTaxName4().indexOf("CESS") != -1) {
            BranchTaxes branchTaxes = BranchTaxes.findById(tranItem.getTax4ID());
            Double taxAmount = tranItem.getTaxValue4();
            if (tranItem.getAdvAdjTax4Value() != null) {
                taxAmount = taxAmount - tranItem.getAdvAdjTax4Value();
            }
            saveTrialBalanceGstTax(txn, user, entityManager, isCredit, taxAmount, branchTaxes, tranItem);
            if ((tanPurpose == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                    || tanPurpose == IdosConstants.BUY_ON_CREDIT_PAY_LATER
                    || tanPurpose == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT)
                    && branchTaxes.getTaxType() == IdosConstants.RCM_CESS_IN) {
                BranchTaxes branchTaxesRcm = BranchTaxes.findByRateType(entityManager, branchTaxes.getBranch().getId(),
                        branchTaxes.getOrganization().getId(), IdosConstants.RCM_CESS_OUTPUT, branchTaxes.getTaxRate());
                saveTrialBalanceGstTax(txn, user, entityManager, true, taxAmount, branchTaxesRcm, tranItem);
            }
        }
    }

    private void saveTrialBalanceGstTax(Transaction transaction, Users user, EntityManager entityManager,
            boolean isCredit,
            Double taxAmount, BranchTaxes branchTaxes, TransactionItems tranItem) {
        TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
        trialBalTaxes.setTransactionId(transaction.getId());
        trialBalTaxes.setTransactionPurpose(transaction.getTransactionPurpose());
        trialBalTaxes.setDate(transaction.getTransactionDate());
        trialBalTaxes.setBranchTaxes(branchTaxes);
        trialBalTaxes.setTaxType(branchTaxes.getTaxType()); // output taxes
        trialBalTaxes.setBranch(transaction.getTransactionBranch());
        trialBalTaxes.setOrganization(transaction.getTransactionBranchOrganization());
        if (isCredit) {
            trialBalTaxes.setCreditAmount(taxAmount);
        } else {
            trialBalTaxes.setDebitAmount(taxAmount);
        }
        trialBalTaxes.setTransactionSpecifics(tranItem.getTransactionSpecifics());
        trialBalTaxes.setTransactionParticulars(tranItem.getTransactionParticulars());
        genericDao.saveOrUpdate(trialBalTaxes, user, entityManager);
    }

    @Override
    public void saveTrialBalInterBranch(Transaction transaction, Users user, Integer typeIdentifier,
            EntityManager entityManager,
            boolean isCredit) {
        InterBranchMapping mapping = null;
        TrialBalanceInterBranch trialBalance = new TrialBalanceInterBranch();
        trialBalance.setTransactionId(transaction.getId());
        trialBalance.setTransactionPurpose(transaction.getTransactionPurpose());
        trialBalance.setDate(transaction.getTransactionDate());
        trialBalance.setOrganization(transaction.getTransactionBranchOrganization());
        if (typeIdentifier == 1) {
            trialBalance.setFromBranch(transaction.getTransactionToBranch());
            trialBalance.setToBranch(transaction.getTransactionBranch());
            mapping = InterBranchMapping.findByFromToBranches(entityManager,
                    transaction.getTransactionToBranch().getId(),
                    transaction.getTransactionBranch().getId());
        } else {
            trialBalance.setFromBranch(transaction.getTransactionBranch());
            trialBalance.setToBranch(transaction.getTransactionToBranch());
            mapping = InterBranchMapping.findByFromToBranches(entityManager, transaction.getTransactionBranch().getId(),
                    transaction.getTransactionToBranch().getId());
        }
        if (isCredit) {
            trialBalance.setCreditAmount(transaction.getNetAmount());
        } else {
            trialBalance.setDebitAmount(transaction.getNetAmount());
        }
        trialBalance.setInterBranchMapping(mapping);
        trialBalance.setTypeIdentifier(typeIdentifier);
        genericDao.saveOrUpdate(trialBalance, user, entityManager);
    }

    /*
     * private void saveTrialBalanceGstTax(ClaimTransaction transaction, Users user,
     * EntityManager entityManager, boolean isCredit, Double taxAmount, BranchTaxes
     * branchTaxes, ClaimItemDetails tranItem){
     * TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
     * trialBalTaxes.setTransactionId(transaction.getId());
     * trialBalTaxes.setTransactionPurpose(transaction.getTransactionPurpose());
     * trialBalTaxes.setDate(transaction.getTransactionDate());
     * trialBalTaxes.setBranchTaxes(branchTaxes);
     * trialBalTaxes.setTaxType(branchTaxes.getTaxType()); //output taxes
     * trialBalTaxes.setBranch(transaction.getTransactionBranch());
     * trialBalTaxes.setOrganization(transaction.getTransactionBranchOrganization())
     * ;
     * if(isCredit){
     * trialBalTaxes.setCreditAmount(taxAmount);
     * }else{
     * trialBalTaxes.setDebitAmount(taxAmount);
     * }
     * //trialBalTaxes.setTransactionSpecifics(tranItem.getTransactionSpecifics());
     * //trialBalTaxes.setTransactionParticulars(tranItem.getTransactionParticulars(
     * ));
     * genericDao.saveOrUpdate(trialBalTaxes, user, entityManager);
     * }
     */

    /**
     * @param org
     * @param branch
     * @param txnId
     * @param txnPurpose
     * @param specifics
     * @param particulars
     * @param txnDate
     * @param amount
     * @param user
     * @param entityManager
     * @param isCredit
     */
    @Override
    public void saveTrialBalanceCOAItem(Organization org, Branch branch, Long txnId, TransactionPurpose txnPurpose,
            Specifics specifics, Particulars particulars, Date txnDate, Double amount, Users user,
            EntityManager entityManager,
            boolean isCredit) {
        TrialBalanceCOAItems trialBalCOA = new TrialBalanceCOAItems();
        trialBalCOA.setTransactionId(txnId);
        trialBalCOA.setTransactionPurpose(txnPurpose);
        trialBalCOA.setTransactionSpecifics(specifics);
        trialBalCOA.setTransactionParticulars(particulars);
        trialBalCOA.setDate(txnDate);
        trialBalCOA.setBranch(branch);
        trialBalCOA.setOrganization(org);
        if (isCredit) {
            trialBalCOA.setCreditAmount(amount);
        } else {
            trialBalCOA.setDebitAmount(amount);
        }
        genericDao.saveOrUpdate(trialBalCOA, user, entityManager);
    }

    @Override
    public Boolean saveTrialBalanceForRoundOff(Organization org, Branch branch, Long txnId,
            TransactionPurpose txnPurpose, Date txnDate, Double roundOffAmount, Users user, EntityManager entityManager,
            boolean isCredit) throws IDOSException {
        boolean roundupMappingFound = true;
        if (roundOffAmount != null && roundOffAmount != 0.0) {
            Specifics specifics = coaDAO.getSpecificsForMapping(user, "51", entityManager);
            if (specifics == null) {
                throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                        "COA mapping is not found for mapping Id: 51",
                        "COA mapping is not found for, mapping : rounding off amounts on Incomes");
            }
            if (roundOffAmount < 0.0) {
                roundOffAmount = roundOffAmount * (-1.0); // make it positive value
            }
            saveTrialBalanceCOAItem(org, branch, txnId, txnPurpose, specifics, specifics.getParticularsId(), txnDate,
                    roundOffAmount, user, entityManager, isCredit);
        }
        return roundupMappingFound;
    }

    @Override
    public BranchTaxes getTdsType4ExpenseByMappedSpecific(Users user, EntityManager entityManager, Specifics specific,
            Vendor vendor, Branch branch, TransactionPurpose txnPurpose, Date txnDate) throws IDOSException {
        if (specific == null) {
            throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                    "COA expense item is not found", "COA expense item is not found.");
        }
        VendorTDSTaxes tdsOnVendorItem = null;
        Long tdsMappingId = null;
        if (vendor != null) {
            tdsOnVendorItem = VendorTDSTaxes.findByOrgVend(entityManager, user.getOrganization().getId(),
                    vendor.getId(), specific,
                    new Date());
        } else {
            tdsOnVendorItem = VendorTDSTaxes.findByOrgVend(entityManager, user.getOrganization().getId(), null,
                    specific,
                    new Date());
        }
        Specifics specificsMap = null;
        if (tdsOnVendorItem != null && tdsOnVendorItem.getTdsSection() != null) {
            tdsMappingId = tdsOnVendorItem.getTdsSection().getId();
        } else if (txnPurpose != null && txnPurpose.getId() == IdosConstants.MAKE_PROVISION_JOURNAL_ENTRY) {
            specificsMap = specific;
            if (specific.getIdentificationForDataValid() != null) {
                tdsMappingId = Long.parseLong(specific.getIdentificationForDataValid());
            }
        } else {
            throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                    "TDS configuration not found for transaction item: " + specific.getId(),
                    "TDS configuration is not found for transaction item: " + specific.getName());
        }
        int withheldingTaxType = 0;
        if (tdsMappingId != null) {
            if (txnPurpose != null && txnPurpose.getId() != IdosConstants.MAKE_PROVISION_JOURNAL_ENTRY) {
                specificsMap = coaService.getSpecificsForMapping(user, tdsMappingId.toString(), entityManager);
                if (specificsMap == null) {
                    throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                            "COA mapping is not found for TDS mapping Id: " + tdsMappingId,
                            "TDS COA mapping is not found for, mapping : " + tdsMappingId);
                }
            }
            int mappingId = tdsMappingId.intValue();
            if (mappingId >= 31 && mappingId <= 38) {
                withheldingTaxType = mappingId + 9;
            }
        }
        if (withheldingTaxType == 0) {
            throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                    "Not able to identify TDS for item: " + specific.getId(),
                    "Not able to identify TDS for item: " + specific.getName() + ", mapping: " + tdsMappingId);
        }
        BranchTaxes branchTax = new BranchTaxes();
        branchTax.setBranch(branch);
        branchTax.setOrganization(branch.getOrganization());
        branchTax.setId(specificsMap.getId());
        branchTax.setTaxType(withheldingTaxType);
        return branchTax;
    }
}
