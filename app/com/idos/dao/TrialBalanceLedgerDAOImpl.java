/**
 *
 */
package com.idos.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.logging.Level;
import com.idos.util.CoaMappingConstants;
import com.idos.util.IDOSException;
import model.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.IdosConstants;

import play.libs.Json;

/**
 * @author Sunil K Namdev created on 19.03.2019
 *
 */
public class TrialBalanceLedgerDAOImpl implements TrialBalanceLedgerDAO {
    private static EntityManager entityManager;

    private static String selectLedgerQuery(ArrayList inparams, long branchid, int ledgerType) {
        String sql = null;
        if (ledgerType == IdosConstants.INCOME) {
            if (branchid != 0L) {
                inparams.add(branchid);
                sql = TB_LDGR_COA_BRNCH_JPQL;
            } else {
                sql = TB_LDGR_COA_ORG_JPQL;
            }
        } else {
            if (branchid != 0L) {
                inparams.add(branchid);
                sql = TB_LDGR_COA_BRNCH_JPQL;
            } else {
                sql = TB_LDGR_COA_ORG_JPQL;
            }
        }
        return sql;
    }

    @Override
    public void getTransactionsForIncomeExpenseCOAItems(EntityManager em, Users user, String headType, Long headId,
            Long headId2, Date fromDate, Date toDate, Long branchId, ArrayNode itemTransData, int ledgerType)
            throws IDOSException {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "*******Start ledgerType: " + ledgerType + " headType: " + headType + " headId: "
                    + headId + " headId2: " + headId2);
        Specifics specifics = Specifics.findById(headId);
        List<Specifics> list = null;
        if (specifics == null) {
            return;
        } else {
            list = Specifics.findChildBySpecificId(em, user.getOrganization().getId(), specifics.getId());
        }
        ArrayList inparams = new ArrayList(5);
        inparams.add(user.getOrganization().getId());
        String sql = selectLedgerQuery(inparams, branchId, ledgerType);
        if (list != null && list.size() > 0) {
            inparams.add(0L);
        } else {
            inparams.add(headId);
        }
        inparams.add(specifics.getAccountCodeHirarchy() + specifics.getAccountCode() + "/%");
        inparams.add(fromDate);
        inparams.add(toDate);

        List<Object[]> txnLists = genericDao.executeNativeQueryWithParam(sql, em, inparams);
        int rowNoOfItem = 0;
        for (Object[] custData : txnLists) {
            long transactionId = 0L;
            long txnPurposeId = -1L;
            int mappingId = 0;
            double credit = 0.0;
            double debit = 0.0;
            Specifics specific = null;
            if (custData[0] != null) {
                transactionId = Long.parseLong(String.valueOf(custData[0]));
            } else {
                continue;
            }
            if (custData[1] != null) {
                txnPurposeId = Long.parseLong(String.valueOf(custData[1]));
            }
            if (custData[2] != null) {
                credit = Double.parseDouble(String.valueOf(custData[2]));
            }
            if (custData[3] != null) {
                debit = Double.parseDouble(String.valueOf(custData[3]));
            }
            if (custData[4] != null && !"".equals(custData[4])) {
                mappingId = Integer.parseInt(String.valueOf(custData[4]));
            }
            if (custData[5] != null) {
                BigInteger specificId = (BigInteger) custData[5];
                specific = Specifics.findById(specificId.longValue());
            }
            ObjectNode row = null;
            if (txnPurposeId >= IdosConstants.REQUEST_FOR_TRAVEL_ADVANCE
                    && txnPurposeId <= IdosConstants.REQUEST_FOR_EXPENSE_REIMBURSEMENT) {
                row = fetchClaimTransactionDetails(em, user, transactionId, debit, credit, txnPurposeId);
            } else if (txnPurposeId == IdosConstants.MAKE_PROVISION_JOURNAL_ENTRY
                    || txnPurposeId == IdosConstants.JOURNAL_ENTRY) {
                rowNoOfItem++;
                row = fetchProvisionTransactionDetails(em, user, transactionId, debit, credit, txnPurposeId, headId,
                        rowNoOfItem);
            } else {
                row = fetchTxnDetailsCOAItems(em, user, transactionId, debit, credit, txnPurposeId, specific);
            }
            setTrialBalanceData4Coa(user, specific, fromDate, toDate, branchId, em, row);
            itemTransData.add(row);
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "******* End ");
    }

    @Override
    public void getMappedItemsTransactionDetails(int ledgerType, String headType, EntityManager em, Users user,
            Long branchid, long headId, long headId2, Date fromDate, Date toDate, ArrayNode itemTransData,
            int mappingID) throws IDOSException {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "******* Start ledgerType= " + ledgerType + " mappingID=" + mappingID);
        if (ledgerType == (int) IdosConstants.ASSETS) {
            if (IdosConstants.HEAD_CASH.equalsIgnoreCase(headType) || mappingID == 3) {
                getTrialBalanceCashTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.CASH,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_PETTY.equalsIgnoreCase(headType) || mappingID == 30) {
                getTrialBalanceCashTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.PETTY_CASH,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_BANK.equalsIgnoreCase(headType) || mappingID == 4) {
                getTrialBalanceBankTrans(em, user, branchid, headId, fromDate, toDate, itemTransData, mappingID);
            } else if (IdosConstants.HEAD_VENDOR_ADV.equalsIgnoreCase(headType) || mappingID == 7) {
                getTrialBalanceCustomerVendorAdvTrans(em, user, branchid, headId, fromDate, toDate,
                        IdosConstants.VENDOR, itemTransData, mappingID);
            } else if (IdosConstants.HEAD_CUSTOMER.equalsIgnoreCase(headType) || mappingID == 1) {
                getTrialBalanceVendorCustomerTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.CUSTOMER,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_TAXS.equalsIgnoreCase(headType) || mappingID == 14) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.INPUT_TAX,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_SGST.equalsIgnoreCase(headType) || mappingID == 39) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.INPUT_SGST,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_CGST.equalsIgnoreCase(headType) || mappingID == 40) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.INPUT_CGST,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_IGST.equalsIgnoreCase(headType) || mappingID == 41) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.INPUT_IGST,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_CESS.equalsIgnoreCase(headType) || mappingID == 42) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.INPUT_CESS,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_RCM_SGST_IN.equalsIgnoreCase(headType) || mappingID == 53) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.RCM_SGST_IN,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_RCM_CGST_IN.equalsIgnoreCase(headType) || mappingID == 54) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.RCM_CGST_IN,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_RCM_IGST_IN.equalsIgnoreCase(headType) || mappingID == 55) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.RCM_IGST_IN,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_RCM_CESS_IN.equalsIgnoreCase(headType) || mappingID == 56) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.RCM_CESS_IN,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_TDS.equalsIgnoreCase(headType) || mappingID == 8) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.INPUT_TDS,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_EXP_ADV.equalsIgnoreCase(headType)
                    || mappingID == CoaMappingConstants.EMPLOYEE_ADVANCES_PAID) {
                getEmployeeAdvance(em, user, branchid, headId, fromDate, toDate, headType, itemTransData, mappingID);
            } else if (IdosConstants.HEAD_INTR_BRANCH_OUT.equalsIgnoreCase(headType)
                    || IdosConstants.HEAD_INTR_BRANCH_IN.equalsIgnoreCase(headType) || mappingID == 57) {
                getTrialBalanceInterBranchTxn(em, user, branchid, headId, headId2, fromDate, toDate, itemTransData,
                        headType, mappingID);
            } else {
                getTrialBalanceCOAItems(em, user, headId, fromDate, toDate, branchid, itemTransData);
            }
        } else if (ledgerType == (int) IdosConstants.LIABILITIES) {
            if (IdosConstants.HEAD_CUSTOMER_ADV.equalsIgnoreCase(headType) || mappingID == 6) {
                getTrialBalanceCustomerVendorAdvTrans(em, user, branchid, headId, fromDate, toDate,
                        IdosConstants.CUSTOMER, itemTransData, mappingID);
            } else if (IdosConstants.HEAD_VENDOR.equalsIgnoreCase(headType) || mappingID == 2) {
                getTrialBalanceVendorCustomerTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.VENDOR,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_TAXS.equalsIgnoreCase(headType) || mappingID == 15) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.OUTPUT_TAX,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_SGST.equalsIgnoreCase(headType) || mappingID == 43) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.OUTPUT_SGST,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_CGST.equalsIgnoreCase(headType) || mappingID == 44) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.OUTPUT_CGST,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_IGST.equalsIgnoreCase(headType) || mappingID == 45) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.OUTPUT_IGST,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_CESS.equalsIgnoreCase(headType) || mappingID == 46) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.OUTPUT_CESS,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_RCM_SGST_OUTPUT.equalsIgnoreCase(headType) || mappingID == 47) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.RCM_SGST_OUTPUT,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_RCM_CGST_OUTPUT.equalsIgnoreCase(headType) || mappingID == 48) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.RCM_CGST_OUTPUT,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_RCM_IGST_OUTPUT.equalsIgnoreCase(headType) || mappingID == 49) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.RCM_IGST_OUTPUT,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_RCM_CESS_OUTPUT.equalsIgnoreCase(headType) || mappingID == 50) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, IdosConstants.RCM_CESS_OUTPUT,
                        itemTransData, mappingID);
            } else if (IdosConstants.HEAD_TDS.equalsIgnoreCase(headType) || (mappingID >= 31 && mappingID <= 38)) {
                getTrialBalanceTaxTrans(em, user, branchid, headId, fromDate, toDate, (mappingID + 9), itemTransData,
                        mappingID);
            } else if (IdosConstants.HEAD_EMP_CLAIM.equalsIgnoreCase(headType)
                    || mappingID == CoaMappingConstants.EMPLOYEE_CLAIMS_PAYABLE) {
                getEmployeeClaimPayable(em, user, branchid, headId, fromDate, toDate, headType, itemTransData,
                        mappingID);
            } else if (headType.equalsIgnoreCase(IdosConstants.HEAD_PAYROLL_DEDUCTIONS)
                    || mappingID == CoaMappingConstants.PAYROLL_SALARIES_DEDUCTIONS) {
                PAYROLL_DAO.getTrialBalancePayrollDeduItems(em, user, headId, fromDate, toDate, branchid,
                        itemTransData);
            } else {
                getTrialBalanceCOAItems(em, user, headId, fromDate, toDate, branchid, itemTransData);
            }
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "******* End ");
    }

    @Override
    public ObjectNode fetchTxnDetailsCOAItems(EntityManager entityManager, Users user, Long transactionID,
            Double debitAmount, Double creditAmount, Long transPurposeID, Specifics txnSpecific) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "*******Start");
        Transaction transaction = Transaction.findById(transactionID);
        if (transaction == null) {
            log.log(Level.SEVERE, "Transaction not found for id: " + transactionID);
            return null;
        }
        ObjectNode row = Json.newObject();
        row.put("txnRef", transaction.getTransactionRefNumber());
        row.put("tranDate", IdosConstants.IDOSDF.format(transaction.getTransactionDate()));
        row.put("email", transaction.getCreatedBy().getEmail());
        row.put("branchName", transaction.getTransactionBranch().getName());
        if (transaction.getTransactionProject() != null && transaction.getTransactionProject().getName() != null) {
            row.put("projectName", transaction.getTransactionProject().getName());
        } else {
            row.put("projectName", "");
        }
        String placeOfSupply = null;
        if (transaction.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                || transaction.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CREDIT_PAY_LATER
                || transaction.getTransactionPurpose().getId() == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT
                || transaction.getTransactionPurpose().getId() == IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER
                || transaction.getTransactionPurpose().getId() == IdosConstants.PAY_VENDOR_SUPPLIER
                || transaction.getTransactionPurpose().getId() == IdosConstants.CREDIT_NOTE_VENDOR
                || transaction.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_VENDOR) {
            if (transaction.getTypeOfSupply() != null) {
                row.put("typeOfSupply",
                        IdosConstants.BUY_TYPE_OF_SUPPLY_MAP.get(String.valueOf(transaction.getTypeOfSupply())));
            } else {
                row.put("typeOfSupply", "");
            }

            if (transaction.getDestinationGstin() != null && transaction.getDestinationGstin().length() > 1) {
                VendorDetail vendorDetail = null;
                if (transaction.getTransactionVendorCustomer() != null) {
                    if (log.isLoggable(Level.FINE))
                        log.log(Level.FINE, "*******Start 1 findByVendorGSTNID");
                    vendorDetail = VendorDetail.findByVendorGSTNID(entityManager,
                            transaction.getTransactionVendorCustomer().getId(), transaction.getDestinationGstin());
                    if (log.isLoggable(Level.FINE))
                        log.log(Level.FINE, "*******End 1 findByVendorGSTNID");
                }
                if (vendorDetail != null)
                    placeOfSupply = vendorDetail.getLocation();
            }
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, "*******Start 2findByTransactionID");
            TransactionInvoice txnInvoice = TransactionInvoice.findByTransactionID(entityManager,
                    user.getOrganization().getId(), transaction.getId());
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, "*******End 2 findByTransactionID");
            if (txnInvoice != null) {
                if (txnInvoice.getInvRefDate() != null) {
                    row.put("invoiceDate", IdosConstants.REPORTDF.format(txnInvoice.getInvRefDate()));
                } else {
                    row.put("invoiceDate", "");
                }
                if (txnInvoice.getInvRefNumber() != null) {
                    row.put("invoiceNo", txnInvoice.getInvRefNumber());
                } else {
                    row.put("invoiceNo", "");
                }
                if (txnInvoice.getGrnDate() != null) {
                    row.put("grnDate", IdosConstants.REPORTDF.format(txnInvoice.getGrnDate()));
                } else {
                    row.put("grnDate", "");
                }
                if (txnInvoice.getGrnRefNumber() != null) {
                    row.put("grnNo", txnInvoice.getGrnRefNumber());
                } else {
                    row.put("grnNo", "");
                }
                if (txnInvoice.getImportDate() != null) {
                    row.put("impDate", IdosConstants.REPORTDF.format(txnInvoice.getImportDate()));
                } else {
                    row.put("impDate", "");
                }
                if (txnInvoice.getImportRefNumber() != null) {
                    row.put("impNo", txnInvoice.getImportRefNumber());
                } else {
                    row.put("impNo", "");
                }
            } else {
                row.put("invoiceDate", "");
                row.put("invoiceNo", "");
                row.put("grnDate", "");
                row.put("grnNo", "");
                row.put("impDate", "");
                row.put("impNo", "");
            }
        } else {
            if (transaction.getTypeOfSupply() != null) {
                String typeOfSupply = null;
                if (transaction.getWithWithoutTax() != null && transaction.getWithWithoutTax() == 1) {
                    typeOfSupply = "On payment of IGST";
                } else if (transaction.getWithWithoutTax() != null && transaction.getWithWithoutTax() == 2) {
                    typeOfSupply = "Under Bond / LUT without payment of IGST";
                }
                if (typeOfSupply != null)
                    typeOfSupply = IdosConstants.TYPE_OF_SUPPLY_MAP.get(String.valueOf(transaction.getTypeOfSupply()))
                            + "-" + typeOfSupply;
                else
                    typeOfSupply = IdosConstants.TYPE_OF_SUPPLY_MAP.get(String.valueOf(transaction.getTypeOfSupply()));
                row.put("typeOfSupply", typeOfSupply);
            } else {
                row.put("typeOfSupply", "");
            }
            if (transaction.getInvoiceNumber() != null) {
                row.put("invoiceNo", transaction.getInvoiceNumber());
            } else {
                row.put("invoiceNo", "");
            }
            if (IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER == transaction.getTransactionPurpose()
                    .getId() && transaction.getTypeIdentifier() == 2) {
                if (transaction.getTransactionDate() != null) {
                    row.put("invoiceDate", IdosConstants.idosdf.format(transaction.getTransactionDate()));
                } else {
                    row.put("invoiceDate", "");
                }
            } else {
                row.put("invoiceDate", "");
            }
            if (txnSpecific != null && txnSpecific.getGstItemCode() != null) {
                row.put("hsnSac", txnSpecific.getGstItemCode());
            } else {
                row.put("hsnSac", "");
            }
            if (transaction.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                    || transaction.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
                    || transaction.getTransactionPurpose().getId() == IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER
                    || transaction.getTransactionPurpose().getId() == IdosConstants.CREDIT_NOTE_CUSTOMER
                    || transaction.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_CUSTOMER) {
                if (log.isLoggable(Level.FINE))
                    log.log(Level.FINE, "*******Start 3 findListByTxn");
                List<AdvanceAdjustmentDetail> aaDetailList = AdvanceAdjustmentDetail.findListByTxn(entityManager,
                        transaction.getId());
                if (log.isLoggable(Level.FINE))
                    log.log(Level.FINE, "*******End 3 findListByTxn");
                String arvNumbers = "";
                for (AdvanceAdjustmentDetail aad : aaDetailList) {
                    if (aad.getAdvTransaction().getInvoiceNumber() != null) {
                        arvNumbers += aad.getAdvTransaction().getInvoiceNumber() + ", ";
                    }
                }
                row.put("refNo", arvNumbers);
            } else if (transaction.getTransactionPurpose().getId() == IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER) {
                if (log.isLoggable(Level.FINE))
                    log.log(Level.FINE, "*******Start 4 findByTxnReference");
                List<Transaction> txnList = Transaction.findByTxnReference(entityManager,
                        user.getOrganization().getId(), transaction.getPaidInvoiceRefNumber());
                if (log.isLoggable(Level.FINE))
                    log.log(Level.FINE, "*******End 4 findByTxnReference");
                if (txnList != null && txnList.size() > 0) {
                    row.put("refNo", txnList.get(0).getInvoiceNumber());
                } else {
                    row.put("refNo", "");
                }
            } else {
                row.put("refNo", "");
            }

            if (transaction.getDestinationGstin() != null && transaction.getDestinationGstin().length() > 1) {
                CustomerDetail customerDetail = null;
                CustomerWalkinDetail customerWalkinDetail = null;
                if (transaction.getTransactionVendorCustomer() != null) {
                    if (log.isLoggable(Level.FINE))
                        log.log(Level.FINE, "*******Start 5 findByCustomerGSTNID");
                    customerDetail = CustomerDetail.findByCustomerGSTNID(entityManager,
                            transaction.getTransactionVendorCustomer().getId(), transaction.getDestinationGstin());
                    if (log.isLoggable(Level.FINE))
                        log.log(Level.FINE, "*******End 5 findByCustomerGSTNID");
                } else {
                    if (log.isLoggable(Level.FINE))
                        log.log(Level.FINE, "*******Start 6 findByNameAndGST23NID");
                    customerWalkinDetail = CustomerWalkinDetail.findByNameAndGSTNID(entityManager,
                            transaction.getTransactionUnavailableVendorCustomer(), transaction.getDestinationGstin());
                    if (log.isLoggable(Level.FINE))
                        log.log(Level.FINE, "*******Start 6 findByNameAndGST2NID");
                }
                if (customerDetail != null)
                    placeOfSupply = customerDetail.getBillinglocation();
                else if (customerWalkinDetail != null)
                    placeOfSupply = customerWalkinDetail.getBillinglocation();
            }
        }

        if (transaction.getTransactionVendorCustomer() != null
                && transaction.getTransactionVendorCustomer().getName() != null)
            row.put("custVendName", transaction.getTransactionVendorCustomer().getName());
        else
            row.put("custVendName", "");

        if (transaction.getDestinationGstin() != null && transaction.getDestinationGstin().length() > 1) {
            String gstinStateCode = transaction.getDestinationGstin().substring(0, 2);
            if (placeOfSupply == null) {
                placeOfSupply = gstinStateCode;
            } else {
                placeOfSupply += "-" + gstinStateCode;
            }
            placeOfSupply += "-" + IdosConstants.STATE_CODE_MAPPING.get(gstinStateCode);
            row.put("placeOfSupply", placeOfSupply);
        } else {
            row.put("placeOfSupply", "");
        }
        if (txnSpecific != null) {
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, "*******Start 7 findByOrgTxnSpecific");
            List<TransactionItems> txnItemList = TransactionItems.findByOrgTxnSpecific(entityManager,
                    user.getOrganization().getId(), transaction.getId(), txnSpecific.getId());
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, "*******End 7 findByOrgTxnSpecific");
            if (txnItemList != null && txnItemList.size() > 0) {
                Double totalTaxRate = 0.0;
                Double totalCessRate = 0.0;
                for (TransactionItems txnItem : txnItemList) {
                    if (txnItem.getTaxRate1() != null) {
                        totalTaxRate += txnItem.getTaxRate1();
                    }
                    if (txnItem.getTaxRate2() != null) {
                        totalTaxRate += txnItem.getTaxRate2();
                    }
                    if (totalTaxRate == 0.0 && txnItem.getTaxRate3() != null) {
                        totalTaxRate = txnItem.getTaxRate3();
                    }
                    if (txnItem.getTaxRate4() != null) {
                        totalCessRate += txnItem.getTaxRate4();
                    }
                }
                row.put("taxRate", totalTaxRate);
                row.put("cessRate", totalCessRate);

            } else {
                row.put("taxRate", "");
                row.put("cessRate", "");
            }
        } else {
            row.put("taxRate", "");
            row.put("cessRate", "");
        }
        if (transaction.getPoReference() != null) {
            row.put("poRef", transaction.getPoReference());
        } else {
            row.put("poRef", "");
        }

        row.put("transactionPurpose", transaction.getTransactionPurpose().getTransactionPurpose());
        if (transaction.getReceiptDetailsType() != null
                && IdosConstants.PAYMODE_CASH == transaction.getReceiptDetailsType()) {
            row.put("paymode", "Cash");
        } else if (transaction.getReceiptDetailsType() != null
                && IdosConstants.PAYMODE_PETTY_CASH == transaction.getReceiptDetailsType()) {
            row.put("paymode", "Pettycash");
        } else if (transaction.getReceiptDetailsType() != null
                && IdosConstants.PAYMODE_BANK == transaction.getReceiptDetailsType()) {
            row.put("paymode", "Bank");
        } else {
            row.put("paymode", "");
        }

        if (creditAmount != null)
            row.put("credit", IdosConstants.decimalFormat.format(creditAmount));
        else
            row.put("credit", "0.00");
        if (debitAmount != null)
            row.put("debit", IdosConstants.decimalFormat.format(debitAmount));
        else
            row.put("debit", "0.00");

        if (transaction.getRemarks() != null)
            row.put("remarks", transaction.getRemarks());
        else
            row.put("remarks", "");

        if (transaction.getSupportingDocs() != null)
            row.put("docs", transaction.getSupportingDocs());
        else
            row.put("docs", "");
        if (transaction.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                || transaction.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
                || transaction.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                || transaction.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CREDIT_PAY_LATER ||
                transaction.getTransactionPurpose().getId() == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) {
            StringBuilder itemsName = new StringBuilder();
            List<TransactionItems> txnItems = transaction.getTransactionItems();
            if (txnItems != null && !txnItems.isEmpty()) {
                for (TransactionItems transactionItems : txnItems) {
                    itemsName.append(" ").append(transactionItems.getTransactionSpecifics().getName()).append(": ")
                            .append(transactionItems.getNetAmount()).append(",");
                }
            }
            if (itemsName.length() > 1) {
                itemsName = itemsName.deleteCharAt(itemsName.lastIndexOf(","));
            }
            row.put("itemName", itemsName.toString());
        } else {
            if (txnSpecific != null && txnSpecific.getName() != null)
                row.put("itemName", txnSpecific.getName());
            else
                row.put("itemName", "");
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "*******End");
        return row;
    }

    @Override
    public ObjectNode fetchClaimTransactionDetails(EntityManager em, Users user, Long transactionID, Double debitAmount,
            Double creditAmount, Long transPurposeID) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "*******Start");
        ObjectNode row = Json.newObject();
        ClaimTransaction transaction = ClaimTransaction.findById(transactionID);
        row.put("txnRef", transaction.getTransactionRefNumber());
        row.put("email", transaction.getCreatedBy().getEmail());
        row.put("tranDate", IdosConstants.idosdf.format(transaction.getTransactionDate()));
        row.put("branchName", transaction.getTransactionBranch().getName());
        if (transaction.getTransactionProject() != null && transaction.getTransactionProject().getName() != null) {
            row.put("projectName", transaction.getTransactionProject().getName());
        } else {
            row.put("projectName", "");
        }
        row.put("typeOfSupply", "");
        StringBuilder hql = new StringBuilder("select obj from TrialBalanceUserAdvance obj where obj.transactionId = ");
        hql.append(transactionID).append(" and transactionPurpose.id = ").append(transPurposeID);
        hql.append(" and organization.id = ").append(user.getOrganization().getId()).append(" and obj.presentStatus=1");

        List<TrialBalanceUserAdvance> claimUserList = genericDao.executeSimpleQuery(hql.toString(), em);
        String userName = null;
        if (claimUserList.size() > 0) {
            userName = claimUserList.get(0).getUser().getEmail();
        } else {
            userName = "";
        }
        row.put("custVendName", userName);
        row.put("placeOfSupply", "");
        row.put("poRef", "");
        row.put("transactionPurpose", transaction.getTransactionPurpose().getTransactionPurpose());
        if (transaction.getReceiptDetailType() != null) {
            if (IdosConstants.PAYMODE_CASH == transaction.getReceiptDetailType()) {
                row.put("paymode", "Cash");
            } else if (IdosConstants.PAYMODE_PETTY_CASH == transaction.getReceiptDetailType()) {
                row.put("paymode", "Pettycash");
            } else if (IdosConstants.PAYMODE_BANK == transaction.getReceiptDetailType()) {
                row.put("paymode", "Bank");
            } else {
                row.put("paymode", "");
            }
        } else {
            row.put("paymode", "");
        }
        if (creditAmount != null)
            row.put("credit", IdosConstants.decimalFormat.format(creditAmount));
        else
            row.put("credit", "0.00");
        if (debitAmount != null)
            row.put("debit", IdosConstants.decimalFormat.format(debitAmount));
        else
            row.put("debit", "0.00");
        if (transaction.getTxnRemarks() != null)
            row.put("remarks", transaction.getTxnRemarks());
        else
            row.put("remarks", "");
        if (transaction.getTransactionPurpose().getId() == 16) {
            row.put("itemName", getClaimsSettlementHeads(em, transaction.getId()));
        } else if (transaction.getTransactionPurpose().getId() == 18) {
            Specifics claimSpecifics = null;
            for (TrialBalanceUserAdvance userExpenseCash : claimUserList) {
                if (userExpenseCash.getCreditAmount() == creditAmount
                        && userExpenseCash.getDebitAmount() == debitAmount) {
                    claimSpecifics = userExpenseCash.getTransactionSpecifics(); // if amt is returned as cash, then
                                                                                // specifics is null in
                                                                                // TrialBalanceUserAdvance and so should
                                                                                // not appear in TB
                }
            }
            if (claimSpecifics != null) {
                row.put("itemName", transaction.getAdvanceForExpenseItems().getName());
            } else {
                row.put("itemName", "");
            }
        } else if (transaction.getTransactionPurpose().getId() == 19) {
            row.put("itemName", transaction.getAdvanceForExpenseItems().getName());
        } else {
            row.put("itemName", "");
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "*******End");
        return row;
    }

    @Override
    public ObjectNode fetchProvisionTransactionDetails(EntityManager em, Users user, Long transactionID,
            Double debitAmount, Double creditAmount, Long transPurposeID, Long headId, int rowNoOfItem) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "*******Strt");
        IdosProvisionJournalEntry transaction = IdosProvisionJournalEntry.findById(transactionID);
        if (transaction == null) {
            return null;
        }
        ObjectNode row = Json.newObject();
        if (transaction.getTransactionRefNumber() != null)
            row.put("txnRef", transaction.getTransactionRefNumber());
        else
            row.put("txnRef", "");
        row.put("tranDate", IdosConstants.idosdf.format(transaction.getTransactionDate()));
        row.put("email", transaction.getCreatedBy().getEmail());
        row.put("branchName", transaction.getDebitBranch().getName());
        // if (transaction.get != null && transaction.getTransactionProject().getName()
        // != null) {
        // row.put("projectName", transaction.getTransactionProject().getName());
        // } else {
        row.put("projectName", "");
        row.put("typeOfSupply", "");
        // }
        /*
         * if (transaction.get != null &&
         * transaction.getTransactionVendorCustomer().getName() != null)
         * row.put("custVendName",
         * transaction.getTransactionVendorCustomer().getName());
         * else
         */
        row.put("custVendName", "");
        row.put("placeOfSupply", "");
        row.put("poRef", "");
        row.put("transactionPurpose", transaction.getTransactionPurpose().getTransactionPurpose());
        /*
         * if (IdosConstants.PAYMODE_CASH == transaction.getReceiptDetailsType()) {
         * row.put("paymode", "Cash");
         * } else if (IdosConstants.PAYMODE_PETTY_CASH ==
         * transaction.getReceiptDetailsType()) {
         * row.put("paymode", "Pettycash");
         * } else if (IdosConstants.PAYMODE_BANK == transaction.getReceiptDetailsType())
         * {
         * row.put("paymode", "Bank");
         * } else {
         */
        row.put("paymode", "");
        // }

        if (creditAmount != null)
            row.put("credit", IdosConstants.decimalFormat.format(creditAmount));
        else
            row.put("credit", "0.00");
        if (debitAmount != null)
            row.put("debit", IdosConstants.decimalFormat.format(debitAmount));
        else
            row.put("debit", "0.00");

        if (transaction.getTxnRemarks() != null)
            row.put("remarks", transaction.getTxnRemarks());
        else
            row.put("remarks", "");

        StringBuilder debitItems = new StringBuilder();
        StringBuilder creditItems = new StringBuilder();
        StringBuilder debitProjectName = new StringBuilder();
        StringBuilder creditProjectName = new StringBuilder();
        getProvisionJournalEntryHeads(em, transaction, IdosConstants.HEAD_SPECIFIC, headId, debitItems, creditItems,
                debitProjectName, creditProjectName, rowNoOfItem);
        row.put("itemName", debitItems.toString());
        row.put("creditItemsName", creditItems.toString());
        row.put("invoiceDate", "");
        row.put("invoiceNo", "");
        row.put("grnDate", "");
        row.put("grnNo", "");
        row.put("impDate", "");
        row.put("impNo", "");
        row.put("taxRate", "");
        row.put("cessRate", "");
        row.put("typeOfSupply", "");
        row.put("hsnSac", "");
        row.put("refNo", "");
        row.put("placeOfSupply", "");
        row.put("poRef", "");
        row.put("debitProjectName", debitProjectName.toString());
        row.put("creditProjectName", creditProjectName.toString());
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "*******End");
        return row;
    }

    private String getClaimsSettlementHeads(EntityManager em, Long transactionId) {
        StringBuilder items = new StringBuilder("");
        List<ClaimsSettlement> claimsSettlementList = ClaimsSettlement.findClaimsSettlementByClaimID(em, transactionId);
        for (ClaimsSettlement settlement : claimsSettlementList) {
            items.append(settlement.getItemName()).append(": ").append(settlement.getItemValue());
            items.append(",");
        }
        if (items.length() > 1) {
            items = items.deleteCharAt(items.lastIndexOf(","));
        }
        return items.toString();
    }

    @Override
    public void getProvisionJournalEntryHeads(EntityManager em, IdosProvisionJournalEntry provisionJournalEntry,
            String headType, Long headID, StringBuilder itemName, StringBuilder creditItems,
            StringBuilder debitProjectName, StringBuilder creditProjectName, int rowNoOfItem) {
        // StringBuilder itemName = new StringBuilder("");
        Map<String, Object> criterias = new HashMap<String, Object>(4);
        List<ProvisionJournalEntryDetail> pjeItemDetailList = provisionJournalEntry.getProvisionJournalEntryDetails();
        int rowNoPjeItemDetail = 0;
        for (ProvisionJournalEntryDetail pjeItemDetail : pjeItemDetailList) {
            /*
             * if(headID == pjEntryDetail.getHeadID() ||
             * headID.equals(pjEntryDetail.getHeadID())){
             * continue;
             * }
             */
            if (pjeItemDetail.getHeadID() != null) {
                if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjeItemDetail.getIsDebit()) {
                    if (pjeItemDetail.getHeadID().equals(headID)) {
                        rowNoPjeItemDetail++;
                        if (rowNoOfItem == rowNoPjeItemDetail) {
                            if (pjeItemDetail.getProject() != null) {
                                debitProjectName.append(pjeItemDetail.getProject().getName());
                            }
                        }
                    }
                } else {
                    if (pjeItemDetail.getHeadID().equals(headID)) {
                        rowNoPjeItemDetail++;
                        if (rowNoOfItem == rowNoPjeItemDetail) {
                            if (pjeItemDetail.getProject() != null) {
                                creditProjectName.append(pjeItemDetail.getProject().getName());
                            }
                        }
                    }
                }
            }

            if (IdosConstants.HEAD_CASH.equals(pjeItemDetail.getHeadType())) { // cash
                criterias.clear();
                criterias.put("id", pjeItemDetail.getHeadID());
                criterias.put("presentStatus", 1);
                List<BranchDepositBoxKey> branchCashCountList = genericDao.findByCriteria(BranchDepositBoxKey.class,
                        criterias, em);
                if (branchCashCountList.size() > 0) {
                    BranchDepositBoxKey branchCashCount = branchCashCountList.get(0);
                    if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjeItemDetail.getIsDebit()) {
                        itemName.append(branchCashCount.getBranch().getName()).append(" Cash");
                        itemName.append(": ").append(pjeItemDetail.getHeadAmount());
                        itemName.append(",");
                    } else {
                        creditItems.append(branchCashCount.getBranch().getName()).append(" Cash");
                        creditItems.append(": ").append(pjeItemDetail.getHeadAmount());
                        creditItems.append(",");
                    }
                }
            } else if (IdosConstants.HEAD_BANK.equals(pjeItemDetail.getHeadType())) { // Bank
                criterias.clear();
                criterias.put("id", pjeItemDetail.getHeadID());
                criterias.put("presentStatus", 1);
                List<BranchBankAccounts> branchBankAccountsList = genericDao.findByCriteria(BranchBankAccounts.class,
                        criterias, em);
                if (branchBankAccountsList.size() > 0) {
                    BranchBankAccounts branchBankAccounts = branchBankAccountsList.get(0);
                    if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjeItemDetail.getIsDebit()) {
                        itemName.append(branchBankAccounts.getBranch().getName() + branchBankAccounts.getBankName());
                        itemName.append(": ").append(pjeItemDetail.getHeadAmount());
                        itemName.append(",");
                    } else {
                        creditItems.append(branchBankAccounts.getBranch().getName() + branchBankAccounts.getBankName());
                        creditItems.append(": ").append(pjeItemDetail.getHeadAmount());
                        creditItems.append(",");
                    }
                }
            } else if (IdosConstants.HEAD_PETTY.equals(pjeItemDetail.getHeadType())) { // petty cash currently not
                                                                                       // getting used
                criterias.clear();
                criterias.put("id", pjeItemDetail.getHeadID());
                criterias.put("presentStatus", 1);
                List<BranchDepositBoxKey> branchCashCountList = genericDao.findByCriteria(BranchDepositBoxKey.class,
                        criterias, em);
                if (branchCashCountList.size() > 0) {
                    BranchDepositBoxKey branchCashCount = branchCashCountList.get(0);
                    if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjeItemDetail.getIsDebit()) {
                        itemName.append(branchCashCount.getBranch().getName()).append(" Pettycash");
                        itemName.append(": ").append(pjeItemDetail.getHeadAmount());
                        itemName.append(",");
                    } else {
                        creditItems.append(branchCashCount.getBranch().getName()).append(" Pettycash");
                        creditItems.append(": ").append(pjeItemDetail.getHeadAmount());
                        creditItems.append(",");
                    }
                }
            } else if (IdosConstants.HEAD_VENDOR.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_CUSTOMER.equals(pjeItemDetail.getHeadType())) {
                criterias.clear();
                criterias.put("id", pjeItemDetail.getHeadID());
                criterias.put("presentStatus", 1);
                List<Vendor> vendorList = genericDao.findByCriteria(Vendor.class, criterias, em);
                if (vendorList.size() > 0) {
                    Vendor vendor = vendorList.get(0);
                    if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjeItemDetail.getIsDebit()) {
                        itemName.append(vendor.getName());
                        itemName.append(": ").append(pjeItemDetail.getHeadAmount());
                        itemName.append(",");
                    } else {
                        creditItems.append(vendor.getName());
                        creditItems.append(": ").append(pjeItemDetail.getHeadAmount());
                        creditItems.append(",");
                    }
                }
            } else if (IdosConstants.HEAD_VENDOR_ADV.equals(headType)
                    || IdosConstants.HEAD_CUSTOMER_ADV.equals(headType)) {
                criterias.clear();
                criterias.put("id", pjeItemDetail.getHeadID());
                criterias.put("presentStatus", 1);
                List<Vendor> vendorList = genericDao.findByCriteria(Vendor.class, criterias, em);
                if (!vendorList.isEmpty()) {
                    Vendor vendor = vendorList.get(0);
                    if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjeItemDetail.getIsDebit()) {
                        itemName.append(vendor.getName()).append("_Adv:").append(pjeItemDetail.getHeadAmount())
                                .append(", ");
                    } else if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeItemDetail.getIsDebit()) {
                        creditItems.append(vendor.getName()).append("_Adv:").append(pjeItemDetail.getHeadAmount())
                                .append(", ");
                    }
                }
            } else if (IdosConstants.HEAD_USER.equals(pjeItemDetail.getHeadType())) {
                criterias.clear();
                criterias.put("id", pjeItemDetail.getHeadID());
                criterias.put("presentStatus", 1);
                List<Users> usersList = genericDao.findByCriteria(Users.class, criterias, em);
                if (usersList.size() > 0) {
                    Users users = usersList.get(0);
                    if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjeItemDetail.getIsDebit()) {
                        itemName.append(users.getFullName());
                        itemName.append(": ").append(pjeItemDetail.getHeadAmount());
                        itemName.append(",");
                    } else {
                        creditItems.append(users.getFullName());
                        creditItems.append(": ").append(pjeItemDetail.getHeadAmount());
                        creditItems.append(",");
                    }
                }
            } else if (IdosConstants.HEAD_TAXS.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_SGST.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_CGST.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_IGST.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_CESS.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_RCM_CESS_IN.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_RCM_SGST_IN.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_RCM_CGST_IN.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_RCM_IGST_IN.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_RCM_CESS_OUTPUT.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_RCM_SGST_OUTPUT.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_RCM_CGST_OUTPUT.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_RCM_IGST_OUTPUT.equals(pjeItemDetail.getHeadType())) {
                criterias.clear();
                criterias.put("id", pjeItemDetail.getHeadID());
                criterias.put("presentStatus", 1);
                List<BranchTaxes> branchTaxesList = genericDao.findByCriteria(BranchTaxes.class, criterias, em);
                if (branchTaxesList.size() > 0) {
                    BranchTaxes branchTaxes = branchTaxesList.get(0);
                    if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjeItemDetail.getIsDebit()) {
                        itemName.append(branchTaxes.getTaxName());
                        itemName.append(": ").append(pjeItemDetail.getHeadAmount());
                        itemName.append(",");
                    } else {
                        creditItems.append(branchTaxes.getTaxName());
                        creditItems.append(": ").append(pjeItemDetail.getHeadAmount());
                        creditItems.append(",");
                    }
                }
            } else if (IdosConstants.HEAD_SPECIFIC.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_TDS_INPUT.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_TDS_192.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_TDS_194A.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_TDS_194C1.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_TDS_194C2.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_TDS_194H.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_TDS_194I1.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_TDS_194I2.equals(pjeItemDetail.getHeadType())
                    || IdosConstants.HEAD_TDS_194J.equals(pjeItemDetail.getHeadType())) {
                criterias.clear();
                criterias.put("id", pjeItemDetail.getHeadID());
                criterias.put("presentStatus", 1);
                List<Specifics> specificsList = genericDao.findByCriteria(Specifics.class, criterias, em);
                if (specificsList.size() > 0) {
                    Specifics specifics = specificsList.get(0);
                    if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjeItemDetail.getIsDebit()) {
                        itemName.append(specifics.getName());
                        itemName.append(": ").append(pjeItemDetail.getHeadAmount());
                        itemName.append(",");
                    } else {
                        creditItems.append(specifics.getName());
                        creditItems.append(": ").append(pjeItemDetail.getHeadAmount());
                        creditItems.append(",");
                    }
                }
            } else if (IdosConstants.HEAD_INTR_BRANCH.equals(pjeItemDetail.getHeadType())) {
                Branch branch = Branch.findById(pjeItemDetail.getHeadID());
                Branch branch2 = Branch.findById(pjeItemDetail.getHeadID2());
                if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjeItemDetail.getIsDebit()) {
                    itemName.append(branch.getName() + "-" + branch2.getName());
                    itemName.append(": ").append(pjeItemDetail.getHeadAmount());
                    itemName.append(",");
                } else {
                    creditItems.append(branch.getName() + "-" + branch2.getName());
                    creditItems.append(": ").append(pjeItemDetail.getHeadAmount());
                    creditItems.append(",");
                }
            }
        }
        if (itemName.length() > 1) {
            if (itemName.lastIndexOf(",") != -1) {
                itemName = itemName.deleteCharAt(itemName.lastIndexOf(","));
            }
        }
        if (creditItems.length() > 1) {
            if (creditItems.lastIndexOf(",") != -1) {
                creditItems = creditItems.deleteCharAt(creditItems.lastIndexOf(","));
            }
        }
        // itemName.append(" \n ").append(creditItems);
        // return itemName.toString();
    }

    private void getTrialBalanceCOAItems(EntityManager em, Users user, Long headId, Date fromDate, Date toDate,
            Long branchId, ArrayNode itemTransData) {
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "******* Start " + headId);
        }
        String sql = null;
        ArrayList inparams = new ArrayList();
        inparams.add(user.getOrganization().getId());
        if (branchId != 0L) {
            inparams.add(branchId);
            sql = TB_LDGR_COA_BRNCH_JPQL2;
        } else {
            sql = TB_LDGR_COA_ORG_JPQL2;
        }
        inparams.add(headId);
        inparams.add(fromDate);
        inparams.add(toDate);

        List<TrialBalanceCOAItems> tbList = genericDao.queryWithParams(sql, em, inparams);
        for (TrialBalanceCOAItems tbCoaItem : tbList) {
            Specifics txnSpecific = tbCoaItem.getTransactionSpecifics();
            if ((tbCoaItem.getTransactionPurpose().getId() >= 15)
                    && (tbCoaItem.getTransactionPurpose().getId() <= 19)) {
                ObjectNode row = fetchClaimTransactionDetails(em, user, tbCoaItem.getTransactionId(),
                        tbCoaItem.getDebitAmount(), tbCoaItem.getCreditAmount(),
                        tbCoaItem.getTransactionPurpose().getId());
                if (row != null) {
                    row.put("ledgerId", IdosConstants.HEAD_SPECIFIC + tbCoaItem.getTransactionSpecifics().getId());
                    row.put("ledgerName", tbCoaItem.getTransactionSpecifics().getName());
                }
                itemTransData.add(row);
                // TODO
            } else if ((tbCoaItem.getTransactionPurpose().getId() == 20)
                    || (tbCoaItem.getTransactionPurpose().getId() == 21)) {
                ObjectNode row = fetchProvisionTransactionDetails(em, user, tbCoaItem.getTransactionId(),
                        tbCoaItem.getDebitAmount(), tbCoaItem.getCreditAmount(),
                        tbCoaItem.getTransactionPurpose().getId(), headId, 0);
                if (row != null) {
                    row.put("ledgerId", IdosConstants.HEAD_SPECIFIC + tbCoaItem.getTransactionSpecifics().getId());
                    row.put("ledgerName", tbCoaItem.getTransactionSpecifics().getName());
                }
                setTrialBalanceData4Coa(user, txnSpecific, fromDate, toDate, branchId, em, row);
                itemTransData.add(row);
            } else {
                ObjectNode row = fetchTxnDetailsCOAItems(em, user, tbCoaItem.getTransactionId(),
                        tbCoaItem.getDebitAmount(), tbCoaItem.getCreditAmount(),
                        tbCoaItem.getTransactionPurpose().getId(), txnSpecific);
                setTrialBalanceData4Coa(user, txnSpecific, fromDate, toDate, branchId, em, row);
                itemTransData.add(row);
            }
        }
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "******* End ");
        }
    }

    @Override
    public void getTrialBalanceCashTrans(EntityManager em, Users user, Long branchId, Long headId, Date fromDate,
            Date toDate, int cashType, ArrayNode itemTransData, int mappId) throws IDOSException {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "******* Start " + headId + " " + mappId);
        String sql = null;
        ArrayList inparams = new ArrayList(4);
        inparams.add(user.getOrganization().getId());
        if (mappId != 3 && mappId != 30 && branchId == 0) {
            sql = CASH_ORG_JPQL;
            inparams.add(headId);
        } else if ((mappId == 3 || mappId == 30) && branchId == 0) {
            sql = ALL_CASH_ORG_JPQL;
        } else if (mappId != 3 && mappId != 30 && branchId != 0) {
            sql = CASH_BRANCH_JPQL;
            inparams.add(branchId);
            inparams.add(headId);
        } else if ((mappId == 3 || mappId == 30) && branchId != 0) {
            sql = ALL_CASH_BRANCH_JPQL;
            inparams.add(branchId);
        }
        inparams.add(cashType);
        inparams.add(fromDate);
        inparams.add(toDate);
        List<TrialBalanceBranchCash> tbList = genericDao.queryWithParamsName(sql, em, inparams);
        for (TrialBalanceBranchCash tbBranchCash : tbList) {
            ObjectNode row = null;
            if ((tbBranchCash.getTransactionPurpose().getId() >= 15)
                    && (tbBranchCash.getTransactionPurpose().getId() <= 19)) {
                row = fetchClaimTransactionDetails(em, user, tbBranchCash.getTransactionId(),
                        tbBranchCash.getDebitAmount(), tbBranchCash.getCreditAmount(),
                        tbBranchCash.getTransactionPurpose().getId());
            } else if ((tbBranchCash.getTransactionPurpose().getId() == 20)
                    || (tbBranchCash.getTransactionPurpose().getId() == 21)) {
                row = fetchProvisionTransactionDetails(em, user, tbBranchCash.getTransactionId(),
                        tbBranchCash.getDebitAmount(), tbBranchCash.getCreditAmount(),
                        tbBranchCash.getTransactionPurpose().getId(), headId, 0);
            } else if (tbBranchCash.getTransactionPurpose().getId() == 34) {
                row = PAYROLL_DAO.fetchPayrollTransactionDetails(em, user, tbBranchCash.getTransactionId(),
                        tbBranchCash.getDebitAmount(), tbBranchCash.getCreditAmount(),
                        tbBranchCash.getTransactionPurpose().getId(), headId);
            } else {
                row = fetchTransactionDetails(em, user, tbBranchCash.getTransactionId(), tbBranchCash.getDebitAmount(),
                        tbBranchCash.getCreditAmount(), tbBranchCash.getTransactionPurpose().getId());
            }
            if (row != null) {
                TrialBalance tb = TRIAL_BALANCE_CASH_DAO.getTrialBalance4SpecificCash(user, em, fromDate, toDate,
                        cashType, branchId, tbBranchCash.getBranchDepositBoxKey());
                if (cashType == IdosConstants.CASH) {
                    row.put("ledgerId", IdosConstants.HEAD_CASH + tb.getSpecId());
                } else {
                    row.put("ledgerId", IdosConstants.HEAD_PETTY + tb.getSpecId());
                }
                row.put("ledgerName", tb.getAccountName());
                setTrialBalanceData(tb, row);
                itemTransData.add(row);
            }
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "******* End ");
    }

    @Override
    public void getTrialBalanceBankTrans(EntityManager em, Users user, Long branchId, Long headId, Date fromDate,
            Date toDate, ArrayNode itemTransData, int mappId) throws IDOSException {
        String sql = null;
        ArrayList inparams = new ArrayList(3);
        inparams.add(user.getOrganization().getId());
        if (mappId != 4 && branchId == 0) {
            sql = BANK_ORG_JPQL;
            inparams.add(headId);
        } else if (mappId == 4 && branchId == 0) {
            sql = ALL_BANK_ORG_JPQL;
        } else if (mappId != 4 && branchId != 0) {
            sql = BANK_BRANCH_JPQL;
            inparams.add(branchId);
            inparams.add(headId);
        } else if (mappId == 4 && branchId != 0) {
            sql = ALL_BANK_BRANCH_JPQL;
            inparams.add(branchId);
        }
        inparams.add(fromDate);
        inparams.add(toDate);
        List<TrialBalanceBranchBank> tbList = genericDao.queryWithParamsName(sql, em, inparams);
        for (TrialBalanceBranchBank tbBranchBank : tbList) {
            ObjectNode row = null;
            if ((tbBranchBank.getTransactionPurpose().getId() >= 15)
                    && (tbBranchBank.getTransactionPurpose().getId() <= 19)) {
                row = fetchClaimTransactionDetails(em, user, tbBranchBank.getTransactionId(),
                        tbBranchBank.getDebitAmount(), tbBranchBank.getCreditAmount(),
                        tbBranchBank.getTransactionPurpose().getId());
            } else if ((tbBranchBank.getTransactionPurpose().getId() == 20)
                    || (tbBranchBank.getTransactionPurpose().getId() == 21)) {
                row = fetchProvisionTransactionDetails(em, user, tbBranchBank.getTransactionId(),
                        tbBranchBank.getDebitAmount(), tbBranchBank.getCreditAmount(),
                        tbBranchBank.getTransactionPurpose().getId(), headId, 0);
            } else if (tbBranchBank.getTransactionPurpose().getId() == 34) {

                row = PAYROLL_DAO.fetchPayrollTransactionDetails(em, user, tbBranchBank.getTransactionId(),
                        tbBranchBank.getDebitAmount(), tbBranchBank.getCreditAmount(),
                        tbBranchBank.getTransactionPurpose().getId(), headId);
            } else {
                row = fetchTransactionDetails(em, user, tbBranchBank.getTransactionId(), tbBranchBank.getDebitAmount(),
                        tbBranchBank.getCreditAmount(), tbBranchBank.getTransactionPurpose().getId());
            }
            if (row != null) {
                TrialBalance tb = TRIAL_BALANCE_BANK_DAO.getTrialBalance4SpecificBank(user, em, fromDate, toDate,
                        branchId, tbBranchBank.getBranchBankAccounts());
                row.put("ledgerId", IdosConstants.HEAD_BANK + tb.getSpecId());
                row.put("ledgerName", tb.getAccountName());
                setTrialBalanceData(tb, row);
                itemTransData.add(row);
            }
        }
    }

    @Override
    public void getTrialBalanceCustomerVendorAdvTrans(EntityManager em, Users user, Long branchId, Long headId,
            Date fromDate, Date toDate, short vendorOrCustomer, ArrayNode itemTransData, int mappId) {
        String sql = null;
        ArrayList inparams = new ArrayList(4);
        inparams.add(user.getOrganization().getId());
        if (mappId != 6 && mappId != 7 && branchId == 0) {
            sql = CUSTVEN_ADV_ORG_JPQL;
            inparams.add(headId);
        } else if ((mappId == 6 || mappId == 7) && branchId == 0) {
            sql = ALL_CUSTVEN_ADV_ORG_JPQL;
        } else if (mappId != 6 && mappId != 7 && branchId != 0) {
            sql = CUSTVEN_ADV_BRANCH_JPQL;
            inparams.add(branchId);
            inparams.add(headId);
        } else if ((mappId == 6 || mappId == 7) && branchId != 0) {
            sql = ALL_CUSTVEN_ADV_BRANCH_JPQL;
            inparams.add(branchId);
        }
        inparams.add((int) vendorOrCustomer);
        inparams.add(fromDate);
        inparams.add(toDate);
        List<TrialBalanceVendorAdvance> tbList = genericDao.queryWithParamsName(sql, em, inparams);
        for (TrialBalanceVendorAdvance tbBranchVendAdv : tbList) {
            ObjectNode row = null;
            if ((tbBranchVendAdv.getTransactionPurpose().getId() >= 15)
                    && (tbBranchVendAdv.getTransactionPurpose().getId() <= 19)) {
                row = fetchClaimTransactionDetails(em, user, tbBranchVendAdv.getTransactionId(),
                        tbBranchVendAdv.getDebitAmount(), tbBranchVendAdv.getCreditAmount(),
                        tbBranchVendAdv.getTransactionPurpose().getId());
            } else if ((tbBranchVendAdv.getTransactionPurpose().getId() == 20)
                    || (tbBranchVendAdv.getTransactionPurpose().getId() == 21)) {
                row = fetchProvisionTransactionDetails(em, user, tbBranchVendAdv.getTransactionId(),
                        tbBranchVendAdv.getDebitAmount(), tbBranchVendAdv.getCreditAmount(),
                        tbBranchVendAdv.getTransactionPurpose().getId(), headId, 0);
            } else {
                row = fetchTxnDetailsCOAItems(em, user, tbBranchVendAdv.getTransactionId(),
                        tbBranchVendAdv.getDebitAmount(), tbBranchVendAdv.getCreditAmount(),
                        tbBranchVendAdv.getTransactionPurpose().getId(), tbBranchVendAdv.getTransactionSpecifics());
            }
            if (row != null) {
                if (tbBranchVendAdv.getVendor().getType() == IdosConstants.VENDOR) {
                    row.put("ledgerId", IdosConstants.HEAD_VENDOR_ADV + tbBranchVendAdv.getVendor().getId());
                } else {
                    row.put("ledgerId", IdosConstants.HEAD_CUSTOMER_ADV + tbBranchVendAdv.getVendor().getId());
                }
                row.put("ledgerName", tbBranchVendAdv.getVendor().getName());
                TrialBalance tb = TRIAL_BALANCE_VENDOR_CUSTOMER_DAO.getTb4CustomerOrVendorAdvance(user, em, fromDate,
                        toDate, branchId, tbBranchVendAdv.getVendor());
                setTrialBalanceData(tb, row);
                itemTransData.add(row);
            }
        }
    }

    @Override
    public void getTrialBalanceVendorCustomerTrans(EntityManager em, Users user, Long branchId, Long headId,
            Date fromDate, Date toDate, int vendorOrCustomer, ArrayNode itemTransData, int mappId) {
        String sql = null;
        ArrayList inparams = new ArrayList(4);
        inparams.add(user.getOrganization().getId());
        if (mappId != 1 && mappId != 2 && branchId == 0) {
            sql = CUSTVEN_ORG_JPQL;
            inparams.add(headId);
        } else if ((mappId == 1 || mappId == 2) && branchId == 0) {
            sql = ALL_CUSTVEN_ORG_JPQL;
        } else if (mappId != 1 && mappId != 2 && branchId != 0) {
            sql = CUSTVEN_BRANCH_JPQL;
            inparams.add(branchId);
            inparams.add(headId);
        } else if ((mappId == 1 || mappId == 2) && branchId != 0) {
            sql = ALL_CUSTVEN_BRANCH_JPQL;
            inparams.add(branchId);
        }
        inparams.add(vendorOrCustomer);
        inparams.add(fromDate);
        inparams.add(toDate);
        List<TrialBalanceCustomerVendor> tbList = genericDao.queryWithParamsName(sql, em, inparams);
        for (TrialBalanceCustomerVendor tbCustVendor : tbList) {
            ObjectNode row = null;
            if ((tbCustVendor.getTransactionPurpose().getId() >= 15)
                    && (tbCustVendor.getTransactionPurpose().getId() <= 19)) {
                row = fetchClaimTransactionDetails(em, user, tbCustVendor.getTransactionId(),
                        tbCustVendor.getDebitAmount(), tbCustVendor.getCreditAmount(),
                        tbCustVendor.getTransactionPurpose().getId());
            } else if ((tbCustVendor.getTransactionPurpose().getId() == 20)
                    || (tbCustVendor.getTransactionPurpose().getId() == 21)) {
                row = fetchProvisionTransactionDetails(em, user, tbCustVendor.getTransactionId(),
                        tbCustVendor.getDebitAmount(), tbCustVendor.getCreditAmount(),
                        tbCustVendor.getTransactionPurpose().getId(), headId, 0);
            } else {
                row = fetchTxnDetailsCOAItems(em, user, tbCustVendor.getTransactionId(), tbCustVendor.getDebitAmount(),
                        tbCustVendor.getCreditAmount(), tbCustVendor.getTransactionPurpose().getId(),
                        tbCustVendor.getTransactionSpecifics());
            }
            if (row != null) {
                if (tbCustVendor.getVendor().getType() == IdosConstants.VENDOR) {
                    row.put("ledgerId", IdosConstants.HEAD_VENDOR + tbCustVendor.getVendor().getId());
                } else {
                    row.put("ledgerId", IdosConstants.HEAD_CUSTOMER + tbCustVendor.getVendor().getId());
                }
                row.put("ledgerName", tbCustVendor.getVendor().getName());
                TrialBalance tb = TRIAL_BALANCE_VENDOR_CUSTOMER_DAO.getTb4CustomerOrVendor(user, em, fromDate, toDate,
                        branchId, tbCustVendor.getVendor());
                setTrialBalanceData(tb, row);
                itemTransData.add(row);
            }
        }
    }

    @Override
    public void getTrialBalanceTaxTrans(EntityManager em, Users user, long branchId, Long headId, Date fromDate,
            Date toDate, int taxType, ArrayNode itemTransData, int mappId) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "*******Start taxType: " + taxType + " headId: " + headId + " mappId: " + mappId);
        String sql = null;
        ArrayList inparams = new ArrayList(4);
        inparams.add(user.getOrganization().getId());
        if ((mappId != 8 && mappId != 9 && mappId != 14 && mappId != 15 && mappId != 31 && mappId != 32 && mappId != 33
                && mappId != 34 &&
                mappId != 35 && mappId != 36 && mappId != 37 && mappId != 38 && mappId != 39 && mappId != 40
                && mappId != 41 && mappId != 42 && mappId != 43 && mappId != 44 && mappId != 45 && mappId != 46
                && mappId != 47 && mappId != 48 && mappId != 49 && mappId != 50 && mappId != 53 && mappId != 54
                && mappId != 55 && mappId != 56) && branchId == 0) {
            sql = TAX_ORG_JPQL;
            inparams.add(headId);
        } else if ((mappId == 8 || mappId == 9 || mappId == 14 || mappId == 15 || (mappId >= 31 && mappId <= 50)
                || (mappId >= 53 && mappId <= 56)) && branchId == 0) {
            sql = ALL_TAX_ORG_JPQL;
        } else if ((mappId != 8 && mappId != 9 && mappId != 14 && mappId != 15 && mappId != 31 && mappId != 32
                && mappId != 33 && mappId != 34 &&
                mappId != 35 && mappId != 36 && mappId != 37 && mappId != 38 && mappId != 39 && mappId != 40
                && mappId != 41 && mappId != 42 && mappId != 43 && mappId != 44 && mappId != 45 && mappId != 46
                && mappId != 47 && mappId != 48 && mappId != 49 && mappId != 50 && mappId != 53 && mappId != 54
                && mappId != 55 && mappId != 56) && branchId != 0) {
            sql = TAX_BRANCH_JPQL;
            inparams.add(branchId);
            inparams.add(headId);
        } else if ((mappId == 8 || mappId == 9 || mappId == 14 || mappId == 15 || (mappId >= 31 && mappId <= 50)
                || (mappId >= 53 && mappId <= 56)) && branchId != 0) {
            sql = ALL_TAX_BRANCH_JPQL;
            inparams.add(branchId);
        }
        inparams.add(taxType);
        inparams.add(fromDate);
        inparams.add(toDate);
        List<TrialBalanceTaxes> tbList = genericDao.queryWithParamsName(sql, em, inparams);
        for (TrialBalanceTaxes tbTaxes : tbList) {
            ObjectNode row = null;
            if ((tbTaxes.getTransactionPurpose().getId() >= 15) && (tbTaxes.getTransactionPurpose().getId() <= 19)) {
                row = fetchClaimTransactionDetails(em, user, tbTaxes.getTransactionId(), tbTaxes.getDebitAmount(),
                        tbTaxes.getCreditAmount(), tbTaxes.getTransactionPurpose().getId());
            } else if ((tbTaxes.getTransactionPurpose().getId() == 20)
                    || (tbTaxes.getTransactionPurpose().getId() == 21)) {
                row = fetchProvisionTransactionDetails(em, user, tbTaxes.getTransactionId(), tbTaxes.getDebitAmount(),
                        tbTaxes.getCreditAmount(), tbTaxes.getTransactionPurpose().getId(), headId, 0);
            } else {
                row = fetchTxnDetailsCOAItems(em, user, tbTaxes.getTransactionId(), tbTaxes.getDebitAmount(),
                        tbTaxes.getCreditAmount(), tbTaxes.getTransactionPurpose().getId(),
                        tbTaxes.getTransactionSpecifics());
            }
            if (row != null) {
                if (mappId == 8 || mappId == 9 || (mappId >= 31 && mappId <= 38)) {
                    Specifics specifics = CHART_OF_ACCOUNTS_SERVICE.getSpecificsForMapping(user, String.valueOf(mappId),
                            em);
                    if (specifics != null) {
                        if (specifics.getPresentStatus() == 0) {
                            row.put("ledgerName", specifics.getName() + " (Disabled)");
                        } else {
                            row.put("ledgerName", specifics.getName());
                        }
                        row.put("ledgerId", IdosConstants.HEAD_TDS + specifics.getId());
                        TrialBalance tb = null;
                        if (mappId == 8) {
                            tb = TRIAL_BALANCE_TDS_DAO.getTrialBalanceForTds(user, em, fromDate, toDate, branchId,
                                    specifics, IdosConstants.INPUT_TDS, IdosConstants.ASSETS, mappId);
                        } else if (mappId >= 31 && mappId <= 38) {
                            tb = TRIAL_BALANCE_TDS_DAO.getTrialBalanceForTds(user, em, fromDate, toDate, branchId,
                                    specifics, (mappId + 9), IdosConstants.LIABILITIES, mappId);
                        } else if (mappId == 9) {
                            tb = TRIAL_BALANCE_TDS_DAO.getTrialBalanceForTds(user, em, fromDate, toDate, branchId,
                                    specifics, IdosConstants.OUTPUT_TDS, IdosConstants.LIABILITIES, mappId);
                        }
                        setTrialBalanceData(tb, row);
                    }
                } else {
                    if (tbTaxes.getBranchTaxes().getPresentStatus() != null
                            && tbTaxes.getBranchTaxes().getPresentStatus() == 0) {
                        row.put("ledgerName", tbTaxes.getBranchTaxes().getTaxName() + " (Disabled)");
                    } else {
                        row.put("ledgerName", tbTaxes.getBranchTaxes().getTaxName());
                    }
                    TrialBalance tb = null;
                    if (mappId == 14 || (mappId >= 39 && mappId <= 42) || (mappId >= 53 && mappId <= 56)) {
                        tb = TRIAL_BALANCE_GST_TAX_DAO.getTrialBalanceGstTax(user, em, fromDate, toDate, branchId,
                                tbTaxes.getBranchTaxes(), IdosConstants.ASSETS);
                    } else {
                        tb = TRIAL_BALANCE_GST_TAX_DAO.getTrialBalanceGstTax(user, em, fromDate, toDate, branchId,
                                tbTaxes.getBranchTaxes(), IdosConstants.LIABILITIES);
                    }
                    setTrialBalanceData(tb, row);
                    row.put("ledgerId", IdosConstants.HEAD_TAXS + tbTaxes.getBranchTaxes().getId());
                }
                itemTransData.add(row);
            }
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "*******End");
    }

    @Override
    public void getEmployeeAdvance(EntityManager em, Users user, long branchId, Long headId, Date fromDate, Date toDate,
            String headType, ArrayNode itemTransData, int mappId) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "*******Start");
        String sql = null;
        ArrayList inparams = new ArrayList(4);
        inparams.add(user.getOrganization().getId());
        if (branchId > 0L) {
            inparams.add(branchId);
            if (mappId == CoaMappingConstants.EMPLOYEE_ADVANCES_PAID) {
                sql = ALL_BRANCH_EMP_ADV_JPQL;
            } else if (mappId != CoaMappingConstants.EMPLOYEE_ADVANCES_PAID) {
                sql = BRANCH_EMP_ADV_JPQL;
                inparams.add(headId);
            }
        } else {
            if (mappId == CoaMappingConstants.EMPLOYEE_ADVANCES_PAID) {
                sql = ALL_ORG_EMP_ADV_JPQL;
            } else if (mappId != CoaMappingConstants.EMPLOYEE_ADVANCES_PAID) {
                sql = ORG_EMP_ADV_JPQL;
                inparams.add(headId);
            }
        }
        inparams.add(fromDate);
        inparams.add(toDate);
        List<TrialBalanceUserAdvance> tbList = genericDao.queryWithParamsName(sql, em, inparams);
        for (TrialBalanceUserAdvance userAdvance : tbList) {
            ObjectNode row = fetchClaimTransactionDetails(em, user, userAdvance.getTransactionId(),
                    userAdvance.getDebitAmount(), userAdvance.getCreditAmount(),
                    userAdvance.getTransactionPurpose().getId());
            if (row != null) {
                row.put("ledgerName", userAdvance.getUser().getFullName() + "-Advance");
                row.put("ledgerId", IdosConstants.HEAD_EXP_ADV + userAdvance.getUser().getId());
                TrialBalance tb = TRIAL_BALANCE_EMP_ADVANCE_PAID_DAO.getUserAdvanceForExpenseAndTravel(user, em,
                        fromDate, toDate, branchId, userAdvance.getUser());
                setTrialBalanceData(tb, row);
                itemTransData.add(row);
            }
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "*******End");
    }

    @Override
    public void getEmployeeClaimPayable(EntityManager em, Users user, long branchId, Long headId, Date fromDate,
            Date toDate, String headType, ArrayNode itemTransData, int mappId) {
        String sql = null;
        ArrayList inparams = new ArrayList(4);
        inparams.add(user.getOrganization().getId());
        if (branchId > 0L) {
            inparams.add(branchId);
            if (mappId == CoaMappingConstants.EMPLOYEE_CLAIMS_PAYABLE) {
                sql = ALL_BRANCH_EMP_CLAIM_JPQL;
            } else if (mappId != CoaMappingConstants.EMPLOYEE_CLAIMS_PAYABLE) {
                sql = BRANCH_EMP_CLAIM_JPQL;
                inparams.add(headId);
            }
        } else {
            if (mappId == CoaMappingConstants.EMPLOYEE_CLAIMS_PAYABLE) {
                sql = ALL_ORG_EMP_CLAIM_JPQL;
            } else if (mappId != CoaMappingConstants.EMPLOYEE_CLAIMS_PAYABLE) {
                sql = ORG_EMP_CLAIM_JPQL;
                inparams.add(headId);
            }
        }
        inparams.add(fromDate);
        inparams.add(toDate);
        List<TrialBalanceUserClaims> tbList = genericDao.queryWithParamsName(sql, em, inparams);
        for (TrialBalanceUserClaims userClaims : tbList) {
            ObjectNode row = fetchClaimTransactionDetails(em, user, userClaims.getTransactionId(),
                    userClaims.getDebitAmount(), userClaims.getCreditAmount(),
                    userClaims.getTransactionPurpose().getId());
            if (row != null) {
                row.put("ledgerName", userClaims.getUser().getFullName() + "-Claim");
                row.put("ledgerId", IdosConstants.HEAD_EXP_ADV + userClaims.getUser().getId());
                TrialBalance tb = TRIAL_BALANCE_EMP_CLAIM_DAO.getEmployeeClaim(user, em, fromDate, toDate, branchId,
                        userClaims.getUser());
                setTrialBalanceData(tb, row);
                itemTransData.add(row);
            }
        }
    }

    @Override
    public ObjectNode fetchTransactionDetails(EntityManager em, Users user, Long transactionID, Double debitAmount,
            Double creditAmount, Long transPurposeID) throws IDOSException {
        ObjectNode row = Json.newObject();
        Transaction transaction = Transaction.findById(transactionID);
        if (transaction == null) {
            throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                    IdosConstants.NULL_KEY_EXC_ESMF_MSG, "Transaction not found for : " + transactionID);
        }
        row.put("txnRef", transaction.getTransactionRefNumber());
        row.put("tranDate", IdosConstants.idosdf.format(transaction.getTransactionDate()));
        row.put("email", transaction.getCreatedBy().getEmail());

        if (transaction.getTransactionProject() != null && transaction.getTransactionProject().getName() != null) {
            row.put("projectName", transaction.getTransactionProject().getName());
        } else {
            row.put("projectName", "");
        }
        String txnPurposeTxt = transaction.getTransactionPurpose().getTransactionPurpose();
        if (transaction.getTransactionPurpose()
                .getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
            if (transaction.getTypeIdentifier() == 1) {
                txnPurposeTxt += "(Outward)";
                row.put("branchName", transaction.getTransactionBranch().getName());
                if (transaction.getTransactionToBranch() != null
                        && transaction.getTransactionToBranch().getName() != null)
                    row.put("custVendName", transaction.getTransactionToBranch().getName());
                else
                    row.put("custVendName", "");
            } else {
                txnPurposeTxt += "(Inward)";
                row.put("branchName", transaction.getTransactionToBranch().getName());
                if (transaction.getTransactionBranch() != null && transaction.getTransactionBranch().getName() != null)
                    row.put("custVendName", transaction.getTransactionBranch().getName());
                else
                    row.put("custVendName", "");
            }
        } else {
            row.put("branchName", transaction.getTransactionBranch().getName());
            if (transaction.getTransactionVendorCustomer() != null
                    && transaction.getTransactionVendorCustomer().getName() != null)
                row.put("custVendName", transaction.getTransactionVendorCustomer().getName());
            else
                row.put("custVendName", "");
        }
        row.put("transactionPurpose", txnPurposeTxt);
        if (transaction.getReceiptDetailsType() != null
                && IdosConstants.PAYMODE_CASH == transaction.getReceiptDetailsType()) {
            row.put("paymode", "Cash");
        } else if (transaction.getReceiptDetailsType() != null
                && IdosConstants.PAYMODE_PETTY_CASH == transaction.getReceiptDetailsType()) {
            row.put("paymode", "Pettycash");
        } else if (transaction.getReceiptDetailsType() != null
                && IdosConstants.PAYMODE_BANK == transaction.getReceiptDetailsType()) {
            row.put("paymode", "Bank");
        } else {
            row.put("paymode", "");
        }

        if (creditAmount != null)
            row.put("credit", IdosConstants.decimalFormat.format(creditAmount));
        else
            row.put("credit", "0.00");
        if (debitAmount != null)
            row.put("debit", IdosConstants.decimalFormat.format(debitAmount));
        else
            row.put("debit", "0.00");

        if (transaction.getRemarks() != null)
            row.put("remarks", transaction.getRemarks());
        else
            row.put("remarks", "");
        if (transaction.getTransactionSpecifics() != null && transaction.getTransactionSpecifics().getName() != null)
            if (transaction.getTransactionItems() != null && transaction.getTransactionItems().size() > 1) {
                String itemNames = "";
                for (TransactionItems txnItems : transaction.getTransactionItems()) {
                    if (txnItems.getTransactionSpecifics() != null
                            && txnItems.getTransactionSpecifics().getName() != null) {
                        itemNames += txnItems.getTransactionSpecifics().getName() + ",";
                    }
                }
                row.put("itemName", itemNames);
            } else {
                row.put("itemName", transaction.getTransactionSpecifics().getName());
            }
        else
            row.put("itemName", "");
        fetchTxnGSTDetails(em, transaction, user, row);
        return row;
    }

    /**
     *
     *
     * @param txn
     * @param txnSpecific
     * @param row
     */
    @Override
    public void fetchTxnGSTDetails(EntityManager em, Transaction txn, Users user, ObjectNode row) {
        String placeOfSupply = null;
        long txnPurpose = txn.getTransactionPurpose().getId();
        if (txnPurpose == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                || txnPurpose == IdosConstants.BUY_ON_CREDIT_PAY_LATER
                || txnPurpose == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT
                || txnPurpose == IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER
                || txnPurpose == IdosConstants.PAY_VENDOR_SUPPLIER || txnPurpose == IdosConstants.CREDIT_NOTE_VENDOR
                || txnPurpose == IdosConstants.DEBIT_NOTE_VENDOR) {
            if (txn.getTypeOfSupply() != null) {
                row.put("typeOfSupply",
                        IdosConstants.BUY_TYPE_OF_SUPPLY_MAP.get(String.valueOf(txn.getTypeOfSupply())));
                row.put("typeOfSupplyNo", txn.getTypeOfSupply());
            } else {
                row.put("typeOfSupply", "");
                row.put("typeOfSupplyNo", "");
            }

            if (txn.getDestinationGstin() != null && txn.getDestinationGstin().length() > 1) {
                VendorDetail vendorDetail = null;
                if (txn.getTransactionVendorCustomer() != null) {
                    vendorDetail = VendorDetail.findByVendorGSTNID(em, txn.getTransactionVendorCustomer().getId(),
                            txn.getDestinationGstin());
                }
                if (vendorDetail != null) {
                    placeOfSupply = vendorDetail.getLocation();
                }
            }
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, "*******Start 1 TransactionInvoice.findByTransactionID");
            TransactionInvoice txnInvoice = TransactionInvoice.findByTransactionID(em,
                    user.getOrganization().getId(), txn.getId());
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, "*******End 1 TransactionInvoice.findByTransactionID");
            if (txnInvoice != null) {
                if (txnInvoice.getInvRefDate() != null) {
                    row.put("invoiceDate", IdosConstants.REPORTDF.format(txnInvoice.getInvRefDate()));
                } else {
                    row.put("invoiceDate", "");
                }
                if (txnInvoice.getInvRefNumber() != null) {
                    row.put("invoiceNo", txnInvoice.getInvRefNumber());
                } else {
                    row.put("invoiceNo", "");
                }
                if (txnInvoice.getGrnDate() != null) {
                    row.put("grnDate", IdosConstants.REPORTDF.format(txnInvoice.getGrnDate()));
                } else {
                    row.put("grnDate", "");
                }
                if (txnInvoice.getGrnRefNumber() != null) {
                    row.put("grnNo", txnInvoice.getGrnRefNumber());
                } else {
                    row.put("grnNo", "");
                }
                if (txnInvoice.getImportDate() != null) {
                    row.put("impDate", IdosConstants.REPORTDF.format(txnInvoice.getImportDate()));
                } else {
                    row.put("impDate", "");
                }
                if (txnInvoice.getImportRefNumber() != null) {
                    row.put("impNo", txnInvoice.getImportRefNumber());
                } else {
                    row.put("impNo", "");
                }
            } else {
                row.put("invoiceDate", "");
                row.put("invoiceNo", "");
                row.put("grnDate", "");
                row.put("grnNo", "");
                row.put("impDate", "");
                row.put("impNo", "");
            }
        } else {
            if (txn.getTypeOfSupply() != null && (txnPurpose == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
                    || txnPurpose == IdosConstants.REFUND_ADVANCE_RECEIVED
                    || txnPurpose == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                    || txnPurpose == IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER
                    || txnPurpose == IdosConstants.CREDIT_NOTE_CUSTOMER
                    || txnPurpose == IdosConstants.DEBIT_NOTE_CUSTOMER)) {
                if (txn.getTypeOfSupply() == 3 || txn.getTypeOfSupply() == 4 || txn.getTypeOfSupply() == 5) {
                    String typeOfSupply = null;
                    if (txn.getWithWithoutTax() != null) {
                        if (txn.getWithWithoutTax() == 1) {
                            typeOfSupply = "On payment of IGST";
                        } else if (txn.getWithWithoutTax() == 2) {
                            typeOfSupply = "Under Bond / LUT without payment of IGST";
                        }
                        row.put("withWithoutTax", txn.getWithWithoutTax());
                    } else {
                        row.put("withWithoutTax", "");
                    }
                    row.put("typeOfSupply", IdosConstants.TYPE_OF_SUPPLY_MAP.get(String.valueOf(txn.getTypeOfSupply()))
                            + "-" + typeOfSupply);
                    row.put("typeOfSupplyNo", txn.getTypeOfSupply());
                } else {
                    row.put("typeOfSupply",
                            IdosConstants.TYPE_OF_SUPPLY_MAP.get(String.valueOf(txn.getTypeOfSupply())));
                }
                row.put("typeOfSupplyNo", txn.getTypeOfSupply());
            } else {
                if (txn.getTypeOfSupply() != null) {
                    String typeOfSupply = null;
                    if (txn.getTypeOfSupply() == 1) {
                        typeOfSupply = "On payment of IGST";
                    } else if (txn.getTypeOfSupply() == 2) {
                        typeOfSupply = "Under Bond / LUT without payment of IGST";

                    }
                    if (typeOfSupply != null)
                        typeOfSupply = IdosConstants.TYPE_OF_SUPPLY_MAP.get(String.valueOf(txn.getTypeOfSupply())) + "-"
                                + typeOfSupply;
                    else
                        typeOfSupply = IdosConstants.TYPE_OF_SUPPLY_MAP.get(String.valueOf(txn.getTypeOfSupply()));
                    row.put("typeOfSupply", typeOfSupply);
                    row.put("typeOfSupplyNo", txn.getTypeOfSupply());
                } else {
                    row.put("typeOfSupply", "");
                    row.put("typeOfSupplyNo", "");
                }
            }

            if (txn.getDestinationGstin() != null && txn.getDestinationGstin().length() > 1) {
                CustomerDetail customerDetail = null;
                CustomerWalkinDetail customerWalkinDetail = null;
                if (txn.getTransactionVendorCustomer() != null) {
                    if (log.isLoggable(Level.FINE))
                        log.log(Level.FINE, "*******Start X findByCustomerGSTNID");
                    customerDetail = CustomerDetail.findByCustomerGSTNID(em,
                            txn.getTransactionVendorCustomer().getId(), txn.getDestinationGstin());
                    if (log.isLoggable(Level.FINE))
                        log.log(Level.FINE, "*******End X findByCustomerGSTNID");
                } else {
                    if (log.isLoggable(Level.FINE))
                        log.log(Level.FINE, "*******Start X findByNameAndGSTNID");
                    customerWalkinDetail = CustomerWalkinDetail.findByNameAndGSTNID(em,
                            txn.getTransactionUnavailableVendorCustomer(), txn.getDestinationGstin());
                    if (log.isLoggable(Level.FINE))
                        log.log(Level.FINE, "*******Start X findByNameAndGSTNID");
                }
                if (customerDetail != null)
                    placeOfSupply = customerDetail.getBillinglocation();
                else if (customerWalkinDetail != null)
                    placeOfSupply = customerWalkinDetail.getBillinglocation();
            }
        }
        if (txn.getDestinationGstin() != null && txn.getDestinationGstin().length() > 1) {
            String gstinStateCode = txn.getDestinationGstin().substring(0, 2);
            if (placeOfSupply == null) {
                placeOfSupply = gstinStateCode;
            } else {
                placeOfSupply += "-" + gstinStateCode;
            }
            placeOfSupply += "-" + IdosConstants.STATE_CODE_MAPPING.get(gstinStateCode);
            row.put("placeOfSupply", placeOfSupply);
            row.put("placeOfSupplyGstin", txn.getDestinationGstin());
        } else {
            row.put("placeOfSupply", placeOfSupply);
            row.put("placeOfSupplyGstin", txn.getDestinationGstin());
        }
    }

    @Override
    public void getTrialBalanceInterBranchTxn(EntityManager em, Users user, Long branchId, Long headId, Long headid2,
            Date fromDate, Date toDate, ArrayNode itemTransData, String headType, int mappId) throws IDOSException {
        String sql = null;
        ArrayList inparams = new ArrayList(3);
        inparams.add(user.getOrganization().getId());
        if (mappId == 57 && branchId == 0) {
            sql = ALL_INTERBRANCH_ORG_JPQL;
        } else if (mappId == 57 && branchId != 0) {
            sql = ALL_INTERBRANCH_BRANCH_JPQL;
            inparams.add(branchId);
            inparams.add(branchId);
        } else {
            sql = INTERBRANCH_ORG_JPQL;
            if (headType == IdosConstants.HEAD_INTR_BRANCH_OUT) {
                inparams.add(headid2);
                inparams.add(headId);
            } else {
                inparams.add(headId);
                inparams.add(headid2);
            }
        }
        inparams.add(fromDate);
        inparams.add(toDate);
        List<TrialBalanceInterBranch> tbList = genericDao.queryWithParamsName(sql, em, inparams);
        for (TrialBalanceInterBranch tbBranch : tbList) {
            ObjectNode row = null;
            if ((tbBranch.getTransactionPurpose().getId() == 20) || (tbBranch.getTransactionPurpose().getId() == 21)) {
                row = fetchProvisionTransactionDetails(em, user, tbBranch.getTransactionId(), tbBranch.getDebitAmount(),
                        tbBranch.getCreditAmount(), tbBranch.getTransactionPurpose().getId(), headId, 0);
            } else {
                row = fetchTransactionDetails(em, user, tbBranch.getTransactionId(), tbBranch.getDebitAmount(),
                        tbBranch.getCreditAmount(), tbBranch.getTransactionPurpose().getId());
            }
            if (row != null) {
                if (tbBranch.getInterBranchMapping() != null) {
                    row.put("ledgerId", IdosConstants.HEAD_INTR_BRANCH + tbBranch.getInterBranchMapping().getId());
                } else {
                    row.put("ledgerId", IdosConstants.HEAD_INTR_BRANCH + tbBranch.getFromBranch().getId() + "-"
                            + tbBranch.getToBranch().getId());
                }
                row.put("ledgerName", tbBranch.getFromBranch().getName() + " - " + tbBranch.getToBranch().getName());
            }
            TrialBalance tb = INTER_BRANCH_TRANSFER_DAO.getTrialBalance4SpecificInterBranch(user, em, fromDate, toDate,
                    branchId, tbBranch.getInterBranchMapping());
            setTrialBalanceData(tb, row);
            itemTransData.add(row);
        }
    }

    @Override
    public void getTransactionsForCoaChildItems(EntityManager em, Users user, String headType, Long headId,
            Long headId2, Date fromDate, Date toDate, Long branchid, ArrayNode itemTransData, int ledgerType)
            throws IDOSException {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "*******Start ledgerType:" + ledgerType + " headType:" + headType);
        Specifics item = Specifics.findById(headId);
        ArrayList inparams = new ArrayList(3);
        inparams.add(user.getOrganization().getId());
        inparams.add(headId);
        inparams.add(item.getAccountCodeHirarchy() + item.getAccountCode() + "/%");
        List<Specifics> specificsList = genericDao.queryWithParamsName(COA_ORG_TXN_JPQL, em, inparams);
        for (Specifics specific : specificsList) {
            if (specific.getIdentificationForDataValid() != null
                    && !"".equals(specific.getIdentificationForDataValid())) {
                int mappingId = Integer.parseInt(specific.getIdentificationForDataValid());
                getMappedItemsTransactionDetails(ledgerType, headType, em, user, branchid, specific.getId(), headId2,
                        fromDate, toDate, itemTransData, mappingId);
            } else {
                getTransactionsForIncomeExpenseCOAItems(em, user, headType, specific.getId(), headId2, fromDate, toDate,
                        branchid, itemTransData, ledgerType);
            }
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "******* End " + itemTransData);
    }

    private void setTrialBalanceData4Coa(Users user, Specifics txnSpecific, Date fromDate, Date toDate, Long branchId,
            EntityManager em, ObjectNode row) {
        if (row == null) {
            return;
        }
        TrialBalance tb = null;
        if (txnSpecific.getAccountCodeHirarchy().startsWith("/1")) {
            tb = TRIAL_BALANCE_SPECIFIC_DAO.getTrialBalance4Specific(user, em, txnSpecific, fromDate, toDate, branchId,
                    IdosConstants.INCOME);
        } else if (txnSpecific.getAccountCodeHirarchy().startsWith("/2")) {
            tb = TRIAL_BALANCE_SPECIFIC_DAO.getTrialBalance4Specific(user, em, txnSpecific, fromDate, toDate, branchId,
                    IdosConstants.EXPENSE);
        } else if (txnSpecific.getAccountCodeHirarchy().startsWith("/3")) {
            tb = TRIAL_BALANCE_SPECIFIC_DAO.getTrialBalance4Specific(user, em, txnSpecific, fromDate, toDate, branchId,
                    IdosConstants.ASSETS);
        } else if (txnSpecific.getAccountCodeHirarchy().startsWith("/4")) {
            tb = TRIAL_BALANCE_SPECIFIC_DAO.getTrialBalance4Specific(user, em, txnSpecific, fromDate, toDate, branchId,
                    IdosConstants.LIABILITIES);
        }
        row.put("ledgerId", IdosConstants.HEAD_SPECIFIC + txnSpecific.getId());
        row.put("ledgerName", txnSpecific.getName());
        setTrialBalanceData(tb, row);
    }

    private void setTrialBalanceData(TrialBalance tb, ObjectNode row) {
        if (tb != null) {
            row.put("openingBalance", IdosConstants.DECIMAL_FORMAT2.format(tb.getOpeningBalance()));
            row.put("creditAmount", IdosConstants.DECIMAL_FORMAT2.format(tb.getCredit()));
            row.put("debitAmount", IdosConstants.DECIMAL_FORMAT2.format(tb.getDebit()));
            row.put("closingBalance", IdosConstants.DECIMAL_FORMAT2.format(tb.getClosingBalance()));
        }
    }
}
