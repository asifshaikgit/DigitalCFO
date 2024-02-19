package com.idos.dao;

import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import model.*;
import model.payroll.PayrollTransaction;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @auther Sunil Namdev created on 25.12.2018
 */
public class TransactionViewDAOImpl implements TransactionViewDAO {
    public BigInteger getTrnsactionsList(Users user, ArrayNode recordArrNodes, EntityManager em, int fromRec,
            int maxRecord) {
        List<UsersRoles> userRoles = user.getUserRoles();
        String roles = "";
        for (UsersRoles role : userRoles) {
            if (!role.getRole().getName().equals("OFFICERS")) {
                roles += role.getRole().getName() + ",";
            }
        }
        roles = roles.substring(0, roles.length() - 1);
        ArrayList inParams = new ArrayList();
        System.out.println("roles" + user.getOrganization().getId());
        inParams.add(user.getOrganization().getId());
        StringBuilder JPQL = new StringBuilder(TXN_JPQL);
        String txnCondition = getTxnCondition(roles, user, inParams);
        JPQL.append(txnCondition);
        JPQL.append(" union all ");
        JPQL.append(PJE_JPQL);
        // inParams.add(user.getOrganization().getId());
        // txnCondition = getTxnCondition(roles, user, inParams);
        JPQL.append(txnCondition);
        JPQL.append(" union all ");
        JPQL.append(PAY_JPQL);
        // inParams.add(user.getOrganization().getId());
        // txnCondition = getTxnCondition(roles, user, inParams);
        JPQL.append(txnCondition);

        /*
         * JPQL.append(" union all ");
         * JPQL.append(BOM_JPQL);
         */

        JPQL.append(" union all ");
        JPQL.append(PURREQ_JPQL);
        // inParams.add(user.getOrganization().getId());
        // txnCondition = getTxnCondition(roles, user, inParams);
        JPQL.append(txnCondition);

        JPQL.append(" union all ");
        JPQL.append(PURORD_JPQL);
        // inParams.add(user.getOrganization().getId());
        JPQL.append(txnCondition);
        // inParams.add(user.getOrganization().getId());
        // txnCondition = getTxnCondition(roles, user, inParams);
        // JPQL.append(txnCondition);
        // System.out.println(inParams);
        // System.out.println(JPQL);
        StringBuilder jpqlStr = new StringBuilder("select * from (");
        jpqlStr.append(JPQL);
        jpqlStr.append(") tbl ORDER BY tbl.CREATED_AT desc limit ").append(fromRec).append(",").append(maxRecord);

        StringBuilder countJpql = new StringBuilder("select count(1) from (");
        countJpql.append(JPQL);
        countJpql.append(") as tbl");

        List<Object[]> txnLists = genericDao.executeNativeQueryWithParam(jpqlStr.toString(), em, inParams);

        for (Object[] txnData : txnLists) {
            long txnid = Long.parseLong(txnData[0].toString());
            int txnPurpose = Integer.parseInt(txnData[1].toString());
            if (txnPurpose == IdosConstants.MAKE_PROVISION_JOURNAL_ENTRY) {
                IdosProvisionJournalEntry txn = IdosProvisionJournalEntry.findById(txnid);
                setPjeDetail(txn, recordArrNodes, user, roles, em);
            } else if (txnPurpose == IdosConstants.PROCESS_PAYROLL) {
                PayrollTransaction txn = PayrollTransaction.findById(txnid);
                setPayrollDetail(txn, recordArrNodes, user, roles);
            } else if (txnPurpose == IdosConstants.BILL_OF_MATERIAL
                    || txnPurpose == IdosConstants.CREATE_PURCHASE_REQUISITION) {
                PurchaseRequisitionTxnModel txn = PurchaseRequisitionTxnModel.findById(txnid);
                setPurReqTranactionDetail(txn, recordArrNodes, user, roles);
            } else if (txnPurpose == IdosConstants.CREATE_PURCHASE_ORDER) {
                PurchaseOrderTxnModel txn = PurchaseOrderTxnModel.findById(txnid);
                setPurOrdTranactionDetail(txn, recordArrNodes, user, roles);
            } else {
                Transaction txn = Transaction.findById(txnid);
                setTranactionDetail(txn, recordArrNodes, user, roles);
            }
        }
        BigInteger totalRecords = genericDao.executeCountNativeQuery(countJpql.toString(), em, inParams);
        return totalRecords;
    }

    private String getTxnCondition(String roles, Users user, ArrayList inParams) {
        String queryCondition = null;
        if (roles.contains("MASTER ADMIN")) {
            queryCondition = "";
        } else if (roles.equals("CREATOR") || roles.equals("CREATOR,CASHIER")) {
            queryCondition = (" and CREATED_BY=" + user.getId());
        } else if (roles.equals("APPROVER") || roles.equals("APPROVER,CASHIER")) {
            queryCondition = (" and (APPROVER_ACTION_BY=?1 or LOCATE(" + user.getId()
                    + ", APPROVER_EMAILS)>0 or SELECTED_ADDITIONAL_APPROVER=" + user.getEmail() + ")");

        } else if (roles.equals("CREATOR,APPROVER") || roles.equals("CREATOR,APPROVER,CASHIER")) {
            queryCondition = (" and (CREATED_BY=?1 or APPROVER_ACTION_BY=" + user.getId() + " or LOCATE("
                    + user.getEmail() + ", APPROVER_EMAILS)>0 or SELECTED_ADDITIONAL_APPROVER=?4)");
        } else if (roles.contains("AUDITOR") || roles.contains("CONTROLLER") || roles.contains("ACCOUNTANT")) {
            queryCondition = (" and TRANSACTION_STATUS = 'Accounted'");
        } else if (roles.equals("CREATOR,APPROVER,ACCOUNTANT") || roles.equals("CREATOR,ACCOUNTANT")
                || roles.equals("CREATOR,ACCOUNTANT,CASHIER") || roles.equals("APPROVER,ACCOUNTANT")
                || roles.equals("CREATOR,APPROVER,ACCOUNTANT,CASHIER") || roles.equals("APPROVER,ACCOUNTANT,CASHIER")) {
            queryCondition = (" and (CREATED_BY=" + user.getId() + " or APPROVER_ACTION_BY=" + user.getId()
                    + " or LOCATE(" + user.getEmail() + ", APPROVER_EMAILS)>0 or SELECTED_ADDITIONAL_APPROVER="
                    + user.getId() + ")");
        }
        if (queryCondition == null) {
            queryCondition = "";
        }
        return queryCondition;
    }

    private void setTranactionDetail(Transaction usrTxn, ArrayNode recordArrNodes, Users user, String roles) {
        ObjectNode event = Json.newObject();
        event.put("userroles", roles);
        event.put("id", usrTxn.getId());

        if (usrTxn.getTransactionProject() != null) {
            event.put("projectName", usrTxn.getTransactionProject().getName());
        } else {
            event.put("projectName", "");
        }
        if (usrTxn.getTransactionSpecifics() != null) {
            event.put("itemName", usrTxn.getTransactionSpecifics().getName());
        } else {
            event.put("itemName", "");
        }
        if (usrTxn.getTransactionSpecifics() != null) {
            if (usrTxn.getTransactionSpecifics().getParentSpecifics() != null
                    && !usrTxn.getTransactionSpecifics().getParentSpecifics().equals("")) {
                event.put("itemParentName", usrTxn.getTransactionSpecifics().getParentSpecifics().getName());
            } else {
                event.put("itemParentName", usrTxn.getTransactionSpecifics().getParticularsId().getName());
            }
        } else {
            event.put("itemParentName", "");
        }
        if (usrTxn.getBudgetAvailDuringTxn() != null) {
            String[] budgetAvailableArr = usrTxn.getBudgetAvailDuringTxn().split(":");
            event.put("budgetAvailable", budgetAvailableArr[0]);
            if (budgetAvailableArr.length > 1) {
                event.put("budgetAvailableAmt", budgetAvailableArr[1]);
            } else {
                event.put("budgetAvailableAmt", "");
            }
        } else {
            event.put("budgetAvailable", "");
            event.put("budgetAvailableAmt", "");
        }
        if (usrTxn.getActualAllocatedBudget() != null) {
            String[] budgetAllocatedArr = usrTxn.getActualAllocatedBudget().split(":");
            event.put("budgetAllocated", budgetAllocatedArr[0]);
            if (budgetAllocatedArr.length > 1) {
                event.put("budgetAllocatedAmt", budgetAllocatedArr[1]);
            } else {
                event.put("budgetAllocatedAmt", "");
            }
        } else {
            event.put("budgetAllocated", "");
            event.put("budgetAllocatedAmt", "");
        }
        if (usrTxn.getTransactionVendorCustomer() != null) {
            event.put("customerVendorName", usrTxn.getTransactionVendorCustomer().getName());
        } else {
            if (usrTxn.getTransactionUnavailableVendorCustomer() != null) {
                event.put("customerVendorName", usrTxn.getTransactionUnavailableVendorCustomer());
            } else {
                event.put("customerVendorName", "");
            }
        }
        if (usrTxn.getTransactionBranch() != null) {
            event.put("branchName", usrTxn.getTransactionBranch().getName());
        } else {

        }
        String branchName = null;
        if (usrTxn.getTransactionBranch() != null) {
            branchName = usrTxn.getTransactionBranch().getName() == null ? "" : usrTxn.getTransactionBranch().getName();
        } else {
            branchName = "";
        }
        String txnPurpose = usrTxn.getTransactionPurpose().getTransactionPurpose();
        if (usrTxn.getTransactionPurpose().getId() == IdosConstants.CREDIT_NOTE_VENDOR) {
            if (usrTxn.getTypeIdentifier() != null) {
                if (usrTxn.getTypeIdentifier() == 1) {
                    txnPurpose += " - Increase in Price";
                    event.put("txnIdentifier", 1);
                } else {
                    txnPurpose += " - Increase in Quantity";
                    event.put("txnIdentifier", 2);
                }
            } else {
                event.put("txnIdentifier", 0);
            }
        } else if (usrTxn.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_VENDOR) {
            if (usrTxn.getTypeIdentifier() != null) {
                if (usrTxn.getTypeIdentifier() == 1) {
                    txnPurpose += " - Decrease in Price";
                    event.put("txnIdentifier", 1);
                } else {
                    txnPurpose += " - Decrease in Quantity";
                    event.put("txnIdentifier", 2);
                }
            } else {
                event.put("txnIdentifier", 0);
            }
        } else if (usrTxn.getTransactionPurpose().getId() == IdosConstants.CREDIT_NOTE_CUSTOMER) {
            if (usrTxn.getTypeIdentifier() != null) {
                if (usrTxn.getTypeIdentifier() == 1) {
                    txnPurpose += " - Decrease in Price";
                    event.put("txnIdentifier", 1);
                } else {
                    txnPurpose += " - Decrease in Quantity";
                    event.put("txnIdentifier", 2);
                }
            } else {
                event.put("txnIdentifier", 0);
            }
        } else if (usrTxn.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_CUSTOMER) {
            if (usrTxn.getTypeIdentifier() != null) {
                if (usrTxn.getTypeIdentifier() == 1) {
                    txnPurpose += " - Increase in Price";
                    event.put("txnIdentifier", 1);
                } else {
                    txnPurpose += " - Increase in Quantity";
                    event.put("txnIdentifier", 2);
                }
            } else {
                event.put("txnIdentifier", 0);
            }
        } else if (usrTxn.getTransactionPurpose()
                .getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
            if (usrTxn.getTransactionToBranch() != null && usrTxn.getTransactionToBranch().getName() != null) {
                branchName += " to " + usrTxn.getTransactionToBranch().getName();
            }
            if (usrTxn.getTypeIdentifier() != null) {
                if (usrTxn.getTypeIdentifier() == 1) {
                    txnPurpose += "(Outward)";
                    event.put("txnIdentifier", 1);
                } else {
                    txnPurpose += "(Inward)";
                    event.put("txnIdentifier", 2);
                }
            } else {
                event.put("txnIdentifier", 0);
            }
        } else {
            event.put("txnIdentifier", 0);
        }
        event.put("branchName", branchName);
        event.put("transactionPurpose", txnPurpose);
        event.put("transactionPurposeID", usrTxn.getTransactionPurpose().getId());
        // added line
        event.put("typeOfSupply", usrTxn.getTypeOfSupply());
        if (usrTxn.getTransactionDate() != null) {
            event.put("txnDate", IdosConstants.IDOSDF.format(usrTxn.getTransactionDate()));
        } else {
            event.put("txnDate", "");
        }
        String invoiceDate = "";
        String invoiceDateLabel = "";
        if (usrTxn.getTransactionInvoiceDate() != null) {
            invoiceDateLabel = "INVOICE DATE:";
            invoiceDate = IdosConstants.IDOSDF.format(usrTxn.getTransactionInvoiceDate());
        }
        event.put("invoiceDateLabel", invoiceDateLabel);
        event.put("invoiceDate", invoiceDate);
        if (usrTxn.getReceiptDetailsType() != null) {
            if (usrTxn.getReceiptDetailsType() == IdosConstants.PAYMODE_CASH) {
                event.put("paymentMode", "CASH");
            } else if (usrTxn.getReceiptDetailsType() == IdosConstants.PAYMODE_BANK) {
                event.put("paymentMode", "BANK");
            } else if (usrTxn.getReceiptDetailsType() == IdosConstants.PAYMODE_PETTY_CASH) {
                event.put("paymentMode", "PETTYCASH");
            } else {
                event.put("paymentMode", "");
            }
        } else {
            event.put("paymentMode", "");
        }
        if (usrTxn.getNoOfUnits() != null) {
            event.put("noOfUnit", IdosConstants.DECIMAL_FORMAT.format(usrTxn.getNoOfUnits()));
        } else {
            event.put("noOfUnit", "");
        }
        if (usrTxn.getPricePerUnit() != null) {
            event.put("unitPrice", IdosConstants.decimalFormat.format(usrTxn.getPricePerUnit()));
        } else {
            event.put("unitPrice", "");
        }
        if (usrTxn.getFrieghtCharges() != null) {
            event.put("frieghtCharges", IdosConstants.decimalFormat.format(usrTxn.getFrieghtCharges()));
        } else {
            event.put("frieghtCharges", "");
        }
        if (usrTxn.getGrossAmount() != null) {
            event.put("grossAmount", IdosConstants.DECIMAL_FORMAT.format(usrTxn.getGrossAmount()));
        } else {
            event.put("grossAmount", "");
        }
        if (usrTxn.getNetAmount() != null) {
            event.put("netAmount", IdosConstants.DECIMAL_FORMAT.format(usrTxn.getNetAmount()));
        } else {
            event.put("netAmount", "");
        }
        if (usrTxn.getNetAmountResultDescription() != null && !usrTxn.getNetAmountResultDescription().equals("null")) {
            event.put("netAmtDesc", usrTxn.getNetAmountResultDescription());
        } else {
            event.put("netAmtDesc", "");
        }

        if (usrTxn.getPoReference() != null) {
            event.put("poReference", usrTxn.getPoReference());
        } else {
            event.put("poReference", "");
        }

        event.put("status", usrTxn.getTransactionStatus());
        event.put("createdBy", usrTxn.getCreatedBy().getEmail());
        if (usrTxn.getApproverActionBy() != null) {
            event.put("approverLabel", "APPROVER:");
            event.put("approverEmail", usrTxn.getApproverActionBy().getEmail());
        } else {
            event.put("approverLabel", "");
            event.put("approverEmail", "");
        }
        if (usrTxn.getSupportingDocs() != null) {
            event.put("txnDocument", usrTxn.getSupportingDocs());
        } else {
            event.put("txnDocument", "");
        }
        if (usrTxn.getRemarks() != null) {
            event.put("txnRemarks", usrTxn.getRemarks());
        } else {
            event.put("txnRemarks", "");
        }
        if (usrTxn.getRemarksPrivate() != null) {
            event.put("remarksPrivate", usrTxn.getRemarksPrivate());
        } else {
            event.put("remarksPrivate", "");
        }

        if (usrTxn.getInvoiceNumber() != null) {
            event.put("invoiceNumber", usrTxn.getInvoiceNumber());
        } else {
            event.put("invoiceNumber", "");
        }
        String txnSpecialStatus = "";
        if (usrTxn.getTransactionExceedingBudget() != null && usrTxn.getKlFollowStatus() != null) {
            if (usrTxn.getTransactionExceedingBudget() == 1 && usrTxn.getKlFollowStatus() == 0) {
                txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
            }
            if (usrTxn.getTransactionExceedingBudget() == 1 && usrTxn.getKlFollowStatus() == 1) {
                txnSpecialStatus = "Transaction Exceeding Budget";
            }
        }
        if (usrTxn.getTransactionExceedingBudget() == null && usrTxn.getKlFollowStatus() != null) {
            if (usrTxn.getKlFollowStatus() == 0) {
                txnSpecialStatus = "Rules Not Followed";
            }
        }
        if (usrTxn.getTransactionExceedingBudget() != null && usrTxn.getKlFollowStatus() == null) {
            txnSpecialStatus = "Transaction Exceeding Budget";
        }
        if (usrTxn.getTransactionExceedingBudget() != null && usrTxn.getKlFollowStatus() == null) {
            txnSpecialStatus = "Transaction Exceeding Budget";
        }
        if (usrTxn.getDocRuleStatus() != null && usrTxn.getTransactionExceedingBudget() != null) {
            if (usrTxn.getDocRuleStatus() == 1 && usrTxn.getTransactionExceedingBudget() == 1) {
                txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
            }
            if (usrTxn.getKlFollowStatus() != null && usrTxn.getKlFollowStatus() == 1
                    && usrTxn.getTransactionExceedingBudget() == 0) {
                txnSpecialStatus = "Rules Not Followed";
            }
        }
        if (usrTxn.getDocRuleStatus() != null && usrTxn.getTransactionExceedingBudget() == null) {
            txnSpecialStatus = "Rules Not Followed";
        }
        event.put("txnSpecialStatus", txnSpecialStatus);
        event.put("roles", roles);
        event.put("useremail", user.getEmail());
        event.put("approverEmails", usrTxn.getApproverEmails());
        event.put("additionalapproverEmails", usrTxn.getAdditionalApproverEmails());
        event.put("selectedAdditionalApproval", usrTxn.getSelectedAdditionalApprover());
        event.put("instrumentNumber", usrTxn.getInstrumentNumber() == null ? "" : usrTxn.getInstrumentNumber());
        event.put("instrumentDate", usrTxn.getInstrumentDate() == null ? "" : usrTxn.getInstrumentDate());
        event.put("txnReferenceNo", usrTxn.getTransactionRefNumber());
        recordArrNodes.add(event);
    }

    private void setPjeDetail(IdosProvisionJournalEntry usrTxn, ArrayNode recordArrNodes, Users user, String roles,
            EntityManager em) {
        ObjectNode event = Json.newObject();
        event.put("userroles", roles);
        event.put("id", usrTxn.getId());
        if (usrTxn.getDebitBranch() != null) {
            event.put("branchName", usrTxn.getDebitBranch().getName());
        } else {
            event.put("branchName", "");
        }
        event.put("projectName", "");
        StringBuilder itemParentName = new StringBuilder();
        StringBuilder creditItemsName = new StringBuilder();
        StringBuilder debitItemsName = new StringBuilder();
        PROVISION_JOURNAL_ENTRY_DAO.getProvisionJournalEntryDetail(em, usrTxn, itemParentName, creditItemsName,
                debitItemsName);
        event.put("itemName", IdosUtil.removeLastChar(debitItemsName.toString()) + "|"
                + IdosUtil.removeLastChar(creditItemsName.toString()));
        event.put("debitItemsName", IdosUtil.removeLastChar(debitItemsName.toString()));
        event.put("creditItemsName", IdosUtil.removeLastChar(creditItemsName.toString()));
        event.put("itemParentName", IdosUtil.removeLastChar(itemParentName.toString()));
        event.put("budgetAvailable", "");
        event.put("budgetAvailableAmt", "");
        event.put("customerVendorName", "");
        event.put("transactionPurpose", usrTxn.getTransactionPurpose().getTransactionPurpose());
        event.put("txnDate", IdosConstants.idosdf.format(usrTxn.getTransactionDate()));
        String invoiceDate = "";
        String invoiceDateLabel = "";
        if (usrTxn.getReversalDate() != null) {
            invoiceDateLabel = "REVERSAL DATE:";
            invoiceDate = IdosConstants.idosdf.format(usrTxn.getReversalDate());
        }
        event.put("invoiceDateLabel", invoiceDateLabel);
        event.put("invoiceDate", invoiceDate);
        event.put("paymentMode", "");
        event.put("noOfUnit", "");
        event.put("unitPrice", "");
        if (usrTxn.getTotalDebitAmount() != null) {
            event.put("grossAmount", IdosConstants.decimalFormat.format(usrTxn.getTotalDebitAmount()));
        } else {
            event.put("grossAmount", "");
        }
        event.put("netAmount", IdosConstants.decimalFormat.format(usrTxn.getTotalDebitAmount()));
        String txnResultDesc = "";
        if (usrTxn.getPurpose() != null && !usrTxn.getPurpose().equals("null")) {
            txnResultDesc = usrTxn.getPurpose();
        }
        event.put("netAmtDesc", txnResultDesc);
        event.put("status", usrTxn.getTransactionStatus());
        event.put("createdBy", usrTxn.getCreatedBy().getEmail());
        if (usrTxn.getApproverActionBy() != null) {
            event.put("approverLabel", "APPROVER:");
            event.put("approverEmail", usrTxn.getApproverActionBy().getEmail());
        } else {
            event.put("approverLabel", "");
            event.put("approverEmail", "");
        }
        if (usrTxn.getSupportingDocuments() != null) {
            event.put("txnDocument", usrTxn.getSupportingDocuments());
        } else {
            event.put("txnDocument", "");
        }
        if (usrTxn.getTxnRemarks() != null) {
            event.put("txnRemarks", usrTxn.getTxnRemarks());
        } else {
            event.put("txnRemarks", "");
        }
        String txnSpecialStatus = "";
        event.put("txnSpecialStatus", txnSpecialStatus);
        event.put("roles", roles);
        event.put("useremail", user.getEmail());
        event.put("approverEmails", usrTxn.getApproverEmails());
        event.put("additionalapproverEmails", usrTxn.getAdditionalApproverUserEmails());
        event.put("selectedAdditionalApproval", usrTxn.getSelectedAdditionalApprover());
        event.put("instrumentNumber", usrTxn.getInstrumentNumber() == null ? "" : usrTxn.getInstrumentNumber());
        event.put("instrumentDate", usrTxn.getInstrumentDate() == null ? "" : usrTxn.getInstrumentDate());
        event.put("txnReferenceNo", usrTxn.getTransactionRefNumber());
        recordArrNodes.add(event);

    }

    private void setPayrollDetail(PayrollTransaction usrTxn, ArrayNode recordArrNodes, Users user, String roles) {
        ObjectNode event = Json.newObject();
        event.put("userroles", roles);
        event.put("id", usrTxn.getId());
        if (usrTxn.getBranch() != null) {
            event.put("branchId", usrTxn.getBranch().getId());
            event.put("branchName", usrTxn.getBranch().getName());
        } else {
            event.put("branchName", "");
        }
        event.put("projectName", "");
        // StringBuilder itemParentName= new StringBuilder("");
        // String itemName = getProvisionJournalEntryDetail(entityManager, usrTxn,
        // itemParentName);

        // event.put("itemName", itemName);
        // event.put("itemParentName", itemParentName.toString());
        event.put("budgetAvailable", "");
        event.put("budgetAvailableAmt", "");
        event.put("customerVendorName", "");
        log.log(Level.FINE, "usrTxn.getTransactionPurpose().getTransactionPurpose():::"
                + usrTxn.getTransactionPurpose().getTransactionPurpose());
        event.put("transactionPurpose", usrTxn.getTransactionPurpose().getTransactionPurpose());

        if (usrTxn.getTransactionDate() != null) {
            event.put("txnDate", IdosConstants.idosdf.format(usrTxn.getTransactionDate()));
        }
        String invoiceDate = "";
        String invoiceDateLabel = "";
        /*
         * if(usrTxn.getReversalDate()!=null){
         * invoiceDateLabel="REVERSAL DATE:";
         * invoiceDate=IdosConstants.idosdf.format(usrTxn.getReversalDate());
         * }
         */
        event.put("invoiceDateLabel", invoiceDateLabel);
        event.put("invoiceDate", invoiceDate);

        event.put("paymentMode", usrTxn.getPayMode() == null ? "" : usrTxn.getPayMode());
        event.put("noOfUnit", "");
        event.put("unitPrice", "");
        event.put("workingDays", usrTxn.getPayDays());
        if (usrTxn.getTotalTotalIncome() != null) {
            event.put("grossAmount", IdosConstants.decimalFormat.format(usrTxn.getTotalTotalIncome()));
        } else {
            event.put("grossAmount", "");
        }
        if (usrTxn.getTotalTotalDeduction() != null) {
            event.put("totalDeductions", IdosConstants.decimalFormat.format(usrTxn.getTotalTotalDeduction()));
        } else {
            event.put("totalDeductions", "");
        }
        event.put("netAmount", IdosConstants.decimalFormat.format(usrTxn.getTotalNetPay()));
        String txnResultDesc = "";
        /*
         * if(usrTxn.getPurpose()!=null && !usrTxn.getPurpose().equals("null")){
         * txnResultDesc=usrTxn.getPurpose();
         * }
         */
        event.put("netAmtDesc", txnResultDesc);
        event.put("status", usrTxn.getTransactionStatus());
        event.put("createdBy", usrTxn.getCreatedBy().getEmail());
        if (usrTxn.getApproverActionBy() != null) {
            event.put("approverLabel", "APPROVER:");
            event.put("approverEmail", usrTxn.getApproverActionBy().getEmail());
        } else {
            event.put("approverLabel", "");
            event.put("approverEmail", "");
        }
        String txnSpecialStatus = "";
        event.put("txnSpecialStatus", txnSpecialStatus);
        event.put("roles", roles);
        event.put("useremail", user.getEmail());
        event.put("approverEmails", usrTxn.getApproverEmails());
        event.put("additionalapproverEmails", usrTxn.getAdditionalApproverEmails());
        event.put("selectedAdditionalApproval", usrTxn.getSelectedAdditionalApprover());
        event.put("instrumentNumber", usrTxn.getInstrumentNumber() == null ? "" : usrTxn.getInstrumentNumber());
        event.put("instrumentDate", usrTxn.getInstrumentDate() == null ? "" : usrTxn.getInstrumentDate());
        event.put("txnReferenceNo", usrTxn.getTransactionRefNumber());
        recordArrNodes.add(event);

    }

    private void setPurReqTranactionDetail(PurchaseRequisitionTxnModel usrTxn, ArrayNode recordArrNodes, Users user,
            String roles) {
        ObjectNode event = Json.newObject();
        event.put("userroles", roles);
        event.put("id", usrTxn.getId());

        if (usrTxn.getProject() != null) {
            event.put("projectName", usrTxn.getProject().getName());
        } else {
            event.put("projectName", "");
        }

        event.put("itemParentName", "");
        event.put("budgetAvailable", "");
        event.put("budgetAvailableAmt", "");
        event.put("budgetAllocated", "");
        event.put("budgetAllocatedAmt", "");
        event.put("customerVendorName", "");

        if (usrTxn.getBranch() != null) {
            event.put("branchName", usrTxn.getBranch().getName());
        } else {

        }
        String branchName = usrTxn.getBranch().getName() == null ? "" : usrTxn.getBranch().getName();
        String txnPurpose = usrTxn.getTransactionPurpose().getTransactionPurpose();
        if (usrTxn.getTransactionPurpose().getId() == IdosConstants.CREATE_PURCHASE_REQUISITION
                && usrTxn.getTypeIdentifier() != null
                && usrTxn.getTypeIdentifier() == IdosConstants.PURCHASE_REQUISITION_AGAINST_BOM) {
            txnPurpose += " (Against Bill Of Material)";
        }

        if (usrTxn.getTypeIdentifier() != null)
            event.put("txnIdentifier", usrTxn.getTypeIdentifier());
        else
            event.put("txnIdentifier", 0);

        event.put("branchName", branchName);
        event.put("transactionPurpose", txnPurpose);
        event.put("transactionPurposeID", usrTxn.getTransactionPurpose().getId());
        // added line
        event.put("typeOfSupply", "");
        if (usrTxn.getActionDate() != null) {
            event.put("txnDate", IdosConstants.IDOSDF.format(usrTxn.getActionDate()));
        } else {
            event.put("txnDate", "");
        }
        String invoiceDate = "";
        String invoiceDateLabel = "";

        event.put("invoiceDateLabel", invoiceDateLabel);
        event.put("invoiceDate", invoiceDate);

        event.put("paymentMode", "");
        if (usrTxn.getTransactionPurpose().getId() == IdosConstants.CREATE_PURCHASE_REQUISITION) {

            event.put("noOfUnit", "");

        } else {
            if (usrTxn.getTotalNoOfUnits() != null) {
                event.put("noOfUnit", IdosConstants.DECIMAL_FORMAT.format(usrTxn.getTotalNoOfUnits()));
            } else {
                event.put("noOfUnit", "");
            }
        }

        event.put("unitPrice", "");
        event.put("frieghtCharges", "");
        if (usrTxn.getTotalAmount() != null) {
            event.put("grossAmount", IdosConstants.DECIMAL_FORMAT.format(usrTxn.getTotalAmount()));
        } else {
            event.put("grossAmount", "");
        }
        if (usrTxn.getTotalNetAmount() != null) {
            event.put("netAmount", IdosConstants.DECIMAL_FORMAT.format(usrTxn.getTotalNetAmount()));
        } else {
            event.put("netAmount", "");
        }

        event.put("netAmtDesc", "");

        if (usrTxn.getDocumentRef() != null) {
            event.put("poReference", usrTxn.getDocumentRef());
        } else {
            event.put("poReference", "");
        }

        event.put("status", usrTxn.getTransactionStatus());
        event.put("createdBy", usrTxn.getCreatedBy().getEmail());
        if (usrTxn.getApproverActionBy() != null) {
            event.put("approverLabel", "APPROVER:");
            event.put("approverEmail", usrTxn.getApproverActionBy().getEmail());
        } else {
            event.put("approverLabel", "");
            event.put("approverEmail", "");
        }
        if (usrTxn.getSupportingDocs() != null) {
            event.put("txnDocument", usrTxn.getSupportingDocs());
        } else {
            event.put("txnDocument", "");
        }
        if (usrTxn.getRemarks() != null) {
            event.put("txnRemarks", usrTxn.getRemarks());
        } else {
            event.put("txnRemarks", "");
        }
        if (usrTxn.getPrivateRemarks() != null) {
            event.put("remarksPrivate", usrTxn.getPrivateRemarks());
        } else {
            event.put("remarksPrivate", "");
        }

        if (usrTxn.getInvoiceNumber() != null) {
            event.put("invoiceNumber", usrTxn.getInvoiceNumber());
        } else {
            event.put("invoiceNumber", "");
        }
        String txnSpecialStatus = "";
        event.put("txnSpecialStatus", txnSpecialStatus);
        event.put("roles", roles);
        event.put("useremail", user.getEmail());
        event.put("approverEmails", usrTxn.getApproverEmails());
        event.put("additionalapproverEmails", usrTxn.getAdditionalApproverEmails());
        event.put("selectedAdditionalApproval", usrTxn.getSelectedAdditionalApprover());
        event.put("instrumentNumber", "");
        event.put("instrumentDate", "");
        event.put("txnReferenceNo", usrTxn.getTransactionRefNumber());
        recordArrNodes.add(event);
    }

    private void setPurOrdTranactionDetail(PurchaseOrderTxnModel usrTxn, ArrayNode recordArrNodes, Users user,
            String roles) {
        ObjectNode event = Json.newObject();
        event.put("userroles", roles);
        event.put("id", usrTxn.getId());

        if (usrTxn.getProject() != null) {
            event.put("projectName", usrTxn.getProject().getName());
        } else {
            event.put("projectName", "");
        }

        event.put("itemParentName", "");
        event.put("budgetAvailable", "");
        event.put("budgetAvailableAmt", "");
        event.put("budgetAllocated", "");
        event.put("budgetAllocatedAmt", "");
        event.put("customerVendorName", "");

        if (usrTxn.getBranch() != null) {
            event.put("branchName", usrTxn.getBranch().getName());
        } else {

        }
        String branchName = usrTxn.getBranch().getName() == null ? "" : usrTxn.getBranch().getName();
        String txnPurpose = usrTxn.getTransactionPurpose().getTransactionPurpose();
        if (usrTxn.getTransactionPurpose().getId() == IdosConstants.CREATE_PURCHASE_ORDER
                && usrTxn.getTypeIdentifier() != null
                && usrTxn.getTypeIdentifier() == IdosConstants.PURCHASE_ORDER_AGAINST_REQUISITION) {
            txnPurpose += " (Against Purchase Requisition)";
        }

        if (usrTxn.getTypeIdentifier() != null)
            event.put("txnIdentifier", usrTxn.getTypeIdentifier());
        else
            event.put("txnIdentifier", 0);

        event.put("branchName", branchName);
        event.put("transactionPurpose", txnPurpose);
        event.put("transactionPurposeID", usrTxn.getTransactionPurpose().getId());
        // added line
        event.put("typeOfSupply", "");
        if (usrTxn.getActionDate() != null) {
            event.put("txnDate", IdosConstants.IDOSDF.format(usrTxn.getActionDate()));
        } else {
            event.put("txnDate", "");
        }
        String invoiceDate = "";
        String invoiceDateLabel = "";

        event.put("invoiceDateLabel", invoiceDateLabel);
        event.put("invoiceDate", invoiceDate);

        event.put("paymentMode", "");
        if (usrTxn.getTransactionPurpose().getId() == IdosConstants.CREATE_PURCHASE_ORDER) {

            event.put("noOfUnit", "");

        } else {
            if (usrTxn.getTotalNoOfUnits() != null) {
                event.put("noOfUnit", IdosConstants.DECIMAL_FORMAT.format(usrTxn.getTotalNoOfUnits()));
            } else {
                event.put("noOfUnit", "");
            }
        }

        event.put("unitPrice", "");
        event.put("frieghtCharges", "");
        if (usrTxn.getTotalAmount() != null) {
            event.put("grossAmount", IdosConstants.DECIMAL_FORMAT.format(usrTxn.getTotalAmount()));
        } else {
            event.put("grossAmount", "");
        }
        if (usrTxn.getTotalNetAmount() != null) {
            event.put("netAmount", IdosConstants.DECIMAL_FORMAT.format(usrTxn.getTotalNetAmount()));
        } else {
            event.put("netAmount", "");
        }

        event.put("netAmtDesc", "");

        if (usrTxn.getDocumentRef() != null) {
            event.put("poReference", usrTxn.getDocumentRef());
        } else {
            event.put("poReference", "");
        }

        event.put("status", usrTxn.getTransactionStatus());
        event.put("createdBy", usrTxn.getCreatedBy().getEmail());
        if (usrTxn.getApproverActionBy() != null) {
            event.put("approverLabel", "APPROVER:");
            event.put("approverEmail", usrTxn.getApproverActionBy().getEmail());
        } else {
            event.put("approverLabel", "");
            event.put("approverEmail", "");
        }
        if (usrTxn.getSupportingDocs() != null) {
            event.put("txnDocument", usrTxn.getSupportingDocs());
        } else {
            event.put("txnDocument", "");
        }
        if (usrTxn.getRemarks() != null) {
            event.put("txnRemarks", usrTxn.getRemarks());
        } else {
            event.put("txnRemarks", "");
        }
        if (usrTxn.getPrivateRemarks() != null) {
            event.put("remarksPrivate", usrTxn.getPrivateRemarks());
        } else {
            event.put("remarksPrivate", "");
        }

        if (usrTxn.getInvoiceNumber() != null) {
            event.put("invoiceNumber", usrTxn.getInvoiceNumber());
        } else {
            event.put("invoiceNumber", "");
        }
        String txnSpecialStatus = "";
        event.put("txnSpecialStatus", txnSpecialStatus);
        event.put("roles", roles);
        event.put("useremail", user.getEmail());
        event.put("approverEmails", usrTxn.getApproverEmails());
        event.put("additionalapproverEmails", usrTxn.getAdditionalApproverEmails());
        event.put("selectedAdditionalApproval", usrTxn.getSelectedAdditionalApprover());
        event.put("instrumentNumber", "");
        event.put("instrumentDate", "");
        event.put("txnReferenceNo", usrTxn.getTransactionRefNumber());
        event.put("parentPurchaseOrder", usrTxn.getParentPurchaseOrder());
        event.put("isParent", usrTxn.getIsParent());
        recordArrNodes.add(event);
    }

    // setBomTransactionDetail might be of no use now because for purchase
    // requisition the new models, DAO and service created. But this need to be
    // tested after remove so that there will be no impact on other functionality
    private void setBomTranactionDetail(BillOfMaterialTxnModel usrTxn, ArrayNode recordArrNodes, Users user,
            String roles) {
        ObjectNode event = Json.newObject();
        event.put("userroles", roles);
        event.put("id", usrTxn.getId());

        if (usrTxn.getProject() != null) {
            event.put("projectName", usrTxn.getProject().getName());
        } else {
            event.put("projectName", "");
        }
        if (usrTxn.getIncome() != null) {
            event.put("itemName", usrTxn.getIncome().getName());
        } else {
            event.put("itemName", "");
        }
        if (usrTxn.getIncome() != null) {
            if (usrTxn.getIncome().getParentSpecifics() != null
                    && !usrTxn.getIncome().getParentSpecifics().equals("")) {
                event.put("itemParentName", usrTxn.getIncome().getParentSpecifics().getName());
            } else {
                event.put("itemParentName", usrTxn.getIncome().getParticularsId().getName());
            }
        } else {
            event.put("itemParentName", "");
        }
        event.put("budgetAvailable", "");
        event.put("budgetAvailableAmt", "");
        event.put("budgetAllocated", "");
        event.put("budgetAllocatedAmt", "");

        if (usrTxn.getCustomerVendor() != null) {
            event.put("customerVendorName", usrTxn.getCustomerVendor().getName());
        } else {
            event.put("customerVendorName", "");
        }
        if (usrTxn.getBranch() != null) {
            event.put("branchName", usrTxn.getBranch().getName());
        } else {

        }
        String branchName = usrTxn.getBranch().getName() == null ? "" : usrTxn.getBranch().getName();
        String txnPurpose = usrTxn.getTransactionPurpose().getTransactionPurpose();
        if (usrTxn.getTransactionPurpose().getId() == IdosConstants.CREATE_PURCHASE_REQUISITION
                && usrTxn.getTypeIdentifier() != null
                && usrTxn.getTypeIdentifier() == IdosConstants.PURCHASE_REQUISITION_AGAINST_SALES_ORDER) {
            txnPurpose += " (Against Sales Order)";
        }

        if (usrTxn.getTypeIdentifier() != null)
            event.put("txnIdentifier", usrTxn.getTypeIdentifier());
        else
            event.put("txnIdentifier", 0);

        event.put("branchName", branchName);
        event.put("transactionPurpose", txnPurpose);
        event.put("transactionPurposeID", usrTxn.getTransactionPurpose().getId());
        // added line
        event.put("typeOfSupply", "");
        if (usrTxn.getActionDate() != null) {
            event.put("txnDate", IdosConstants.IDOSDF.format(usrTxn.getActionDate()));
        } else {
            event.put("txnDate", "");
        }
        String invoiceDate = "";
        String invoiceDateLabel = "";

        event.put("invoiceDateLabel", invoiceDateLabel);
        event.put("invoiceDate", invoiceDate);

        event.put("paymentMode", "");
        if (usrTxn.getTransactionPurpose().getId() == IdosConstants.BILL_OF_MATERIAL) {
            if (usrTxn.getIncomeNoOfUnits() != null) {
                event.put("noOfUnit", IdosConstants.DECIMAL_FORMAT.format(usrTxn.getIncomeNoOfUnits()));
            } else {
                event.put("noOfUnit", "");
            }
        } else {
            if (usrTxn.getTotalNoOfUnits() != null) {
                event.put("noOfUnit", IdosConstants.DECIMAL_FORMAT.format(usrTxn.getTotalNoOfUnits()));
            } else {
                event.put("noOfUnit", "");
            }
        }

        event.put("unitPrice", "");
        event.put("frieghtCharges", "");
        if (usrTxn.getTotalAmount() != null) {
            event.put("grossAmount", IdosConstants.DECIMAL_FORMAT.format(usrTxn.getTotalAmount()));
        } else {
            event.put("grossAmount", "");
        }
        if (usrTxn.getTotalNetAmount() != null) {
            event.put("netAmount", IdosConstants.DECIMAL_FORMAT.format(usrTxn.getTotalNetAmount()));
        } else {
            event.put("netAmount", "");
        }

        event.put("netAmtDesc", "");

        if (usrTxn.getDocumentRef() != null) {
            event.put("poReference", usrTxn.getDocumentRef());
        } else {
            event.put("poReference", "");
        }

        event.put("status", usrTxn.getTransactionStatus());
        event.put("createdBy", usrTxn.getCreatedBy().getEmail());
        if (usrTxn.getApproverActionBy() != null) {
            event.put("approverLabel", "APPROVER:");
            event.put("approverEmail", usrTxn.getApproverActionBy().getEmail());
        } else {
            event.put("approverLabel", "");
            event.put("approverEmail", "");
        }
        if (usrTxn.getSupportingDocs() != null) {
            event.put("txnDocument", usrTxn.getSupportingDocs());
        } else {
            event.put("txnDocument", "");
        }
        if (usrTxn.getRemarks() != null) {
            event.put("txnRemarks", usrTxn.getRemarks());
        } else {
            event.put("txnRemarks", "");
        }
        if (usrTxn.getPrivateRemarks() != null) {
            event.put("remarksPrivate", usrTxn.getPrivateRemarks());
        } else {
            event.put("remarksPrivate", "");
        }

        if (usrTxn.getInvoiceNumber() != null) {
            event.put("invoiceNumber", usrTxn.getInvoiceNumber());
        } else {
            event.put("invoiceNumber", "");
        }
        String txnSpecialStatus = "";
        event.put("txnSpecialStatus", txnSpecialStatus);
        event.put("roles", roles);
        event.put("useremail", user.getEmail());
        event.put("approverEmails", usrTxn.getApproverEmails());
        event.put("additionalapproverEmails", usrTxn.getAdditionalApproverEmails());
        event.put("selectedAdditionalApproval", usrTxn.getSelectedAdditionalApprover());
        event.put("instrumentNumber", "");
        event.put("instrumentDate", "");
        event.put("txnReferenceNo", usrTxn.getTransactionRefNumber());
        recordArrNodes.add(event);
    }

    @Override
    public List<Object[]> getAccountingList(long id, long purposeId, EntityManager entityManager) {
        List<Object[]> procedureList = entityManager
                .createNativeQuery("{call getTransactionAccountingInfo(:parameter,:transactionpurpose)}")
                .setParameter("parameter", id)
                .setParameter("transactionpurpose", purposeId)
                .getResultList();
        return procedureList;
    }
    /*
     * @Override
     * public String getPjeCondition(Users user, String roles, ArrayList inParams){
     * String queryCondition = null;
     * if (roles.equals("CREATOR") || roles.equals("CREATOR,CASHIER")) {
     * queryCondition = (" and CREATED_BY.id=?x");
     * inParams.add(user.getId());
     * } else if (roles.equals("APPROVER") || roles.equals("APPROVER,CASHIER")) {
     * queryCondition =
     * (" and (APPROVER_ACTION_BY=?x or LOCATE(?x, APPROVER_EMAILS)>0 or SELECTED_ADDITIONAL_APPROVER=?x)"
     * );
     * inParams.add(user.getId());
     * inParams.add(user.getEmail());
     * inParams.add(user.getEmail());
     * } else if (roles.equals("CREATOR,APPROVER") ||
     * roles.equals("CREATOR,APPROVER,CASHIER")) {
     * queryCondition =
     * (" and (CREATED_BY.id=?x or APPROVER_ACTION_BY=?x or LOCATE(?x, APPROVER_EMAILS)>0 or SELECTED_ADDITIONAL_APPROVER=?x)"
     * );
     * inParams.add(user.getId());
     * inParams.add(user.getId());
     * inParams.add(user.getEmail());
     * inParams.add(user.getEmail());
     * } else if (roles.equals("CREATOR,ACCOUNTANT") ||
     * roles.equals("CREATOR,APPROVER,ACCOUNTANT") ||
     * roles.equals("CREATOR,ACCOUNTANT,CASHIER") ||
     * roles.equals("APPROVER,ACCOUNTANT,CASHIER") ||
     * roles.equals("CREATOR,APPROVER,ACCOUNTANT,CASHIER") ||
     * roles.equals("APPROVER,ACCOUNTANT") || roles.contains("CONTROLLER") ||
     * roles.contains("ACCOUNTANT")) {
     * queryCondition = (" ORDER BY obj.createdAt desc");
     * }
     * return queryCondition;
     * }
     */
}
