package com.idos.dao;

import actor.CreatorActor;
import com.idos.util.*;
import controllers.Karvy.KarvyAuthorization;

import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import play.libs.Json;
import play.mvc.WebSocket;
import pojo.TransactionViewResponse;

import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;
import java.text.ParseException;
import java.util.*;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import akka.stream.javadsl.*;
import akka.actor.*;
import akka.NotUsed;

/**
 * Created by Sunil K. Namdev on 10-10-2017.
 */
public class BuyTransactionDAOImpl implements BuyTransactionDAO {
    private static JPAApi jpaApi;
    private static EntityManager em;

    @Override
    public Transaction submit4AccoutingBuyOnPetty(Users user, EntityManager em, JsonNode json,
            TransactionPurpose usertxnPurpose, ObjectNode result) throws IDOSException {
        Transaction transaction = null;
        try {
            Long tdsPayableSpecificID = 0l;
            Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l
                    : json.findValue("txnEntityID").asLong();
            Long txnForBranch = json.findValue("txnForBranch") == null ? null : json.findValue("txnForBranch").asLong();
            Long txnForProject = (json.findValue("txnForProject") == null || "".equals(json.findValue("txnForProject")))
                    ? 0l
                    : json.findValue("txnForProject").asLong();
            String txnPOInvoice = json.findValue("txnPOInvoice") == null ? null
                    : json.findValue("txnPOInvoice").asText();
            String txnForItemStr = json.findValue("txnForItem") == null ? null
                    : json.findValue("txnForItem").toString();
            Long txnForCustomer = "".equals(json.findValue("txnForCustomer").asText()) ? null
                    : json.findValue("txnForCustomer").asLong();
            String txnForUnavailableCustomer = json.findValue("txnForUnavailableCustomer") != null ? null
                    : json.findValue("txnForUnavailableCustomer").asText();
            Double txnTotalNetAmount = json.findValue("txnTotalNetAmount") == null ? 0.0
                    : json.findValue("txnTotalNetAmount").asDouble();
            Double totalTxnNetAmtWithDecimalValue = json.findValue("totalTxnNetAmtWithDecimalValue") == null ? 0.0
                    : json.findValue("totalTxnNetAmtWithDecimalValue").asDouble();
            Double totalWithholdTaxAmt = json.findValue("totalWithholdTaxAmt") == null ? 0.0
                    : json.findValue("totalWithholdTaxAmt").asDouble();
            Double totalTxnTaxAmt = json.findValue("totalTxnTaxAmt") == null ? 0.0
                    : json.findValue("totalTxnTaxAmt").asDouble();
            Double totalTxnGrossAmt = json.findValue("totalTxnGrossAmt") == null ? 0.0
                    : json.findValue("totalTxnGrossAmt").asDouble();
            int klfollowednotfollowed = json.findValue("klfollowednotfollowed") == null ? 1
                    : json.findValue("klfollowednotfollowed").asInt();
            String txnRemarks = json.findValue("txnRemarks") != null ? "" : json.findValue("txnRemarks").asText();
            String txnprocrem = json.findValue("txnprocrem") != null ? json.findValue("txnprocrem").asText() : null;
            String supportingdoc = json.findValue("supportingdoc") != null ? null
                    : json.findValue("supportingdoc").asText();
            String txnDocumentUploadRequired = json.findValue("txnDocumentUploadRequired") != null
                    ? json.findValue("txnDocumentUploadRequired").asText()
                    : null;
            // String txnLeftOutWithholdTransIDs =
            // json.findValue("txnLeftOutWithholdTransIDs") == null ? null :
            // json.findValue("txnLeftOutWithholdTransIDs").asText();
            String txnSourceGstin = json.findValue("txnSourceGstin") == null ? null
                    : json.findValue("txnSourceGstin").asText();
            String txnDestinGstin = json.findValue("txnDestinGstin") == null ? null
                    : json.findValue("txnDestinGstin").asText();
            int txnTypeOfSupply = json.findValue("txnTypeOfSupply") == null ? 0
                    : json.findValue("txnTypeOfSupply").asInt();
            int txnWalkinCustomerType = json.findValue("txnWalkinCustomerType") == null ? 0
                    : json.findValue("txnWalkinCustomerType").asInt();
            String txnInvDate = json.findValue("txnInvDate") == null ? null : json.findValue("txnInvDate").asText();
            int txnReceiptDetails = json.findValue("txnReceiptDetails") == null ? 0
                    : json.findValue("txnReceiptDetails").asInt();
            Boolean isPettyCashLimitExceded = false;
            String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
            Date txnDate = null;
            if (selectedTxnDate != null) {
                txnDate = IdosConstants.IDOSDF.parse(selectedTxnDate);
            } else {
                txnDate = new Date();
            }

            if (txnWalkinCustomerType == 1 || txnWalkinCustomerType == 2) {
                VENDOR_DAO.saveVendor(json, user, em, IdosConstants.WALK_IN_VENDOR);
            }
            String remarks = "";
            Branch txnBranch = null;
            String branchName = "";
            Vendor txncustomer = null;
            String paymentMode = "";
            Project txnProject = null;
            String projectName = "";
            String itemName = "";
            String customerVendorName = "";
            String netAmtInputTaxDesc = "";
            String debitCredit = "Credit";

            if (txnEntityID > 0) {
                transaction = Transaction.findById(txnEntityID);
            } else {
                transaction = new Transaction();
            }
            if (txnForBranch != null) {
                txnBranch = Branch.findById(txnForBranch);
                branchName = txnBranch.getName();
                List<BranchDepositBoxKey> branchSafeDepBox = txnBranch.getBranchDepositKeys();
                if (branchSafeDepBox.size() > 0) {
                    for (BranchDepositBoxKey bnchSfeDepBox : branchSafeDepBox) {
                        if (bnchSfeDepBox.getPettyCashTxnApprovalRequired() == 1) {
                            if (bnchSfeDepBox.getApprovalAmountLimit() != null) {
                                if (bnchSfeDepBox.getApprovalAmountLimit() < txnTotalNetAmount) {
                                    isPettyCashLimitExceded = true;
                                }
                            }
                        }
                    }
                }
            }
            if (txnForProject != null && txnForProject > 0) {
                txnProject = Project.findById(txnForProject);
                projectName = txnProject.getName();
            }
            JSONArray arrJSON = new JSONArray(txnForItemStr);
            // Enter data for first item in transaction table to be displayed in Transaction
            // list
            Specifics txnItem = null;
            if (arrJSON.length() > 0) {
                JSONObject firstRowItemData = new JSONObject(arrJSON.get(0).toString());
                Long itemIdRow0 = firstRowItemData.getLong("txnItems");
                txnItem = Specifics.findById(itemIdRow0);
                itemName = txnItem.getName();
                Double txnPerUnitPriceRow0 = firstRowItemData.getDouble("txnPerUnitPrice");
                Double txnNoOfUniRow0t = firstRowItemData.getDouble("txnNoOfUnit");
                Double txnGrossRow0 = firstRowItemData.getDouble("txnGross");
                transaction.setTransactionSpecifics(txnItem);
                transaction.setTransactionParticulars(txnItem.getParticularsId());
                transaction.setNoOfUnits(txnNoOfUniRow0t);
                transaction.setPricePerUnit(txnPerUnitPriceRow0);
            }
            transaction.setGrossAmount(txnTotalNetAmount);
            if (txnInvDate != null && !txnInvDate.equals("")) {
                transaction.setTransactionInvoiceDate(IdosConstants.MYSQLDF
                        .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(txnInvDate))));
            }
            if (txnForCustomer != null) {
                txncustomer = Vendor.findById(txnForCustomer);
                customerVendorName = txncustomer.getName();
            }
            transaction.setKlFollowStatus(klfollowednotfollowed);
            transaction.setSourceGstin(txnSourceGstin);
            transaction.setDestinationGstin(txnDestinGstin);
            transaction.setTypeOfSupply(txnTypeOfSupply);
            transaction.setWalkinCustomerType(txnWalkinCustomerType);
            transaction.setTransactionPurpose(usertxnPurpose);
            transaction.setTransactionBranch(txnBranch);
            transaction.setTransactionBranchOrganization(txnBranch.getOrganization());
            transaction.setTransactionProject(txnProject);
            transaction.setTransactionVendorCustomer(txncustomer);
            transaction.setTransactionUnavailableVendorCustomer(txnForUnavailableCustomer);
            if (txnForUnavailableCustomer != null) {
                customerVendorName = txnForUnavailableCustomer;
            }
            transaction.setNetAmount(txnTotalNetAmount);
            Double roundedCutPartOfNetAmount = txnTotalNetAmount - totalTxnNetAmtWithDecimalValue;
            transaction.setRoundedCutPartOfNetAmount(roundedCutPartOfNetAmount);
            // TODO should be added later
            /*
             * if (!txn.equals("") && txnnetamountdescription != null) {
             * transaction.setNetAmountResultDescription(txnnetamountdescription);
             * }
             */
            transaction.setTransactionDate(txnDate);
            boolean isBackDated = DateUtil.isBackDate(txnDate);
            if (isBackDated) {
                transaction.setIsBackdatedTransaction(IdosConstants.BACK_DATED_TXN);
            }
            transaction.setReceiptDetailsType(txnReceiptDetails);
            String txnReceiptTypeBankDetails = json.findValue("txnReceiptTypeBankDetails") == null ? null
                    : json.findValue("txnReceiptTypeBankDetails").asText();
            transaction.setReceiptDetailsDescription(txnReceiptTypeBankDetails);
            if (IdosConstants.PAYMODE_CASH == txnReceiptDetails) {
                paymentMode = "CASH";
            } else if (IdosConstants.PAYMODE_BANK == txnReceiptDetails) {
                paymentMode = "BANK";
            } else if (IdosConstants.PAYMODE_PETTY_CASH == txnReceiptDetails) {
                paymentMode = "PETTY CASH";
            }
            String txnprocreq = json.findValue("txnprocreq") != null ? json.findValue("txnprocreq").asText() : null;
            if (txnprocreq != null) {
                ProcurementRequest procreq = ProcurementRequest.findById(IdosUtil.convertStringToLong(txnprocreq));
                transaction.setProcuredBy(procreq.getCreatedBy());
                procreq.setProcurementStatus("Procurement Completed");
                genericDao.saveOrUpdate(procreq, user, em);
                if (txnprocrem != null) {
                    txnRemarks = procreq.getCreatedBy().getEmail() + "#" + txnprocrem;
                    transaction.setRemarks(txnRemarks);
                }
            }
            if (txnRemarks != null && !txnRemarks.equals("")) {
                if (transaction.getRemarks() != null) {
                    remarks = transaction.getRemarks() + "|" + user.getEmail() + "#" + txnRemarks;
                    transaction.setRemarks(txnRemarks);
                } else {
                    remarks = user.getEmail() + "#" + txnRemarks;
                    transaction.setRemarks(remarks);
                }
                remarks = transaction.getRemarks();
            }
            transaction.setNetAmountResultDescription(transaction.getNetAmountResultDescription() + netAmtInputTaxDesc);
            transaction.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(
                    transaction.getSupportingDocs(), user.getEmail(), supportingdoc, user, em));
            String approverEmails = "";
            String additionalApprovarUsers = "";
            String selectedAdditionalApproval = "";
            if (isPettyCashLimitExceded) {
                transaction.setTransactionStatus(IdosConstants.TXN_STATUS_REQUIRE_APPROVAL);
                transaction.setPaymentStatus(IdosConstants.NOT_PAID);
                transaction.setVendorDuePayment(0.0);
                transaction.setVendorNetPayment(txnTotalNetAmount);
                Map<String, Object> criterias = new HashMap<String, Object>(2);
                criterias.put("role.name", "APPROVER");
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("presentStatus", 1);
                List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, em);
                Boolean approver = null;
                for (UsersRoles usrRoles : approverRole) {
                    approver = false;
                    additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
                    criterias.clear();
                    criterias.put("user.id", usrRoles.getUser().getId());
                    criterias.put("userRights.id", 2L);
                    criterias.put("branch.id", txnBranch.getId());
                    criterias.put("presentStatus", 1);
                    UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class,
                            criterias, em);
                    if (userHasRightInBranch != null) {
                        for (int i = 0; i < arrJSON.length(); i++) {
                            // Double howMuchAdvance=0.0;Double txnTaxAmount=0.0;Double
                            // customerAdvance=0.0;String txnTaxDesc="";Double withholdingAmount=0.0;
                            JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                            // TransactionItems transactionItem = new TransactionItems();
                            Long itemId = rowItemData.getLong("txnItems");
                            Specifics txnItem1 = genericDao.getById(Specifics.class, itemId, em);
                            criterias.clear();
                            criterias.put("user.id", usrRoles.getUser().getId());
                            criterias.put("userRights.id", 2L);
                            criterias.put("specifics.id", txnItem1.getId());
                            criterias.put("presentStatus", 1);
                            UserRightSpecifics userHasRightInCOA = genericDao.getByCriteria(UserRightSpecifics.class,
                                    criterias, em);
                            if (userHasRightInCOA != null) {
                                approver = true;
                            } else {
                                approver = false;
                            }
                        }
                        if (approver) {
                            approverEmails += usrRoles.getUser().getEmail() + ",";
                        }

                    }
                }
                transaction.setApproverEmails(approverEmails);
                transaction.setAdditionalApproverEmails(additionalApprovarUsers);
            } else {
                transaction.setTransactionStatus("Accounted");
                transaction.setPaymentStatus("PAID");
                transaction.setVendorDuePayment(0.0);
                transaction.setVendorNetPayment(txnTotalNetAmount);
            }
            if (txnReceiptDetails == IdosConstants.PAYMODE_PETTY_CASH && !isPettyCashLimitExceded) {
                boolean updatePettyCash = false;
                double resultantCash = branchCashService.updateBranchPettyCashDetail(em, genericDao, user,
                        transaction.getTransactionBranch(), txnTotalNetAmount, true, transaction.getTransactionDate(),
                        result);
                if (resultantCash < 0) {
                    throw new IDOSException(IdosConstants.INSUFFICIENT_BALANCE_ERRCODE,
                            IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INSUFFICIENT_BALANCE_EXCEPTION,
                            "Pettycash balance is insufficient.");
                }
            }

            String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
            transaction.setTransactionRefNumber(transactionNumber);
            if (transaction.getTxnDone() == null) {
                transaction.setTxnDone(1);
            }
            transaction.setTxnDone(1);
            genericDao.saveOrUpdate(transaction, user, em);
            FILE_UPLOAD_DAO.updateUploadFileLogs(em, user, supportingdoc, transaction.getId(),
                    IdosConstants.MAIN_TXN_TYPE);
            // Enter multiple items data into TransactionItems table
            if (txnEntityID > 0) {
                transactionItemsService.updateMultipleItemsTransactionItems(em, user, arrJSON, transaction);
            } else {
                transactionItemsService.insertMultipleItemsTransactionItems(em, user, arrJSON, transaction, txnDate);
            }
            if (!isPettyCashLimitExceded) {
                CREATE_TRIAL_BALANCE_DAO.insertTrialBalance(transaction, user, em); // TrailBalance entries
                STOCK_SERVICE.insertTradingInventory(transaction, user, em);
                if (transaction.getRoundedCutPartOfNetAmount() != null
                        && transaction.getRoundedCutPartOfNetAmount() != 0.0) {
                    Boolean roundupMappingFound = null;
                    if (transaction.getRoundedCutPartOfNetAmount() > 0) {
                        roundupMappingFound = CREATE_TRIAL_BALANCE_DAO.saveTrialBalanceForRoundOff(
                                transaction.getTransactionBranchOrganization(), transaction.getTransactionBranch(),
                                transaction.getId(), transaction.getTransactionPurpose(),
                                transaction.getTransactionDate(), transaction.getRoundedCutPartOfNetAmount(), user, em,
                                false);
                    } else {
                        roundupMappingFound = CREATE_TRIAL_BALANCE_DAO.saveTrialBalanceForRoundOff(
                                transaction.getTransactionBranchOrganization(), transaction.getTransactionBranch(),
                                transaction.getId(), transaction.getTransactionPurpose(),
                                transaction.getTransactionDate(), transaction.getRoundedCutPartOfNetAmount(), user, em,
                                true);
                    }
                    result.put("roundupMappingFound", roundupMappingFound);
                    if (roundupMappingFound == null || !roundupMappingFound) {
                        return null;
                    }
                }
            }

            String invoiceDate = "";
            String invoiceDateLabel = "";
            if (transaction.getTransactionInvoiceDate() != null) {
                invoiceDateLabel = "INVOICE DATE:";
                invoiceDate = IdosConstants.IDOSDF.format(transaction.getTransactionInvoiceDate());
            }
            String approverEmail = "";
            String approverLabel = "";
            if (transaction.getApproverActionBy() != null) {
                approverLabel = "APPROVER:";
                approverEmail = transaction.getApproverActionBy().getEmail();
            }
            String itemParentName = "";
            if (txnItem.getParentSpecifics() != null && !txnItem.getParentSpecifics().equals("")) {
                itemParentName = txnItem.getParentSpecifics().getName();
            } else {
                itemParentName = txnItem.getParticularsId().getName();
            }
            String txnSpecialStatus = "";
            if (transaction.getTransactionExceedingBudget() != null && transaction.getKlFollowStatus() != null) {
                if (transaction.getTransactionExceedingBudget() == 1 && transaction.getKlFollowStatus() == 0) {
                    txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
                }
                if (transaction.getTransactionExceedingBudget() == 1 && transaction.getKlFollowStatus() == 1) {
                    txnSpecialStatus = "Transaction Exceeding Budget";
                }
            }
            if (transaction.getTransactionExceedingBudget() == null && transaction.getKlFollowStatus() != null) {
                if (transaction.getKlFollowStatus() == 0) {
                    txnSpecialStatus = "Rules Not Followed";
                }
            }
            if (transaction.getTransactionExceedingBudget() != null && transaction.getKlFollowStatus() == null) {
                txnSpecialStatus = "Transaction Exceeding Budget";
            }
            // Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
            // Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
            // for (int i = 0; i < keyArray.length; i++) {
            // StringBuilder sbquery = new StringBuilder("");
            // sbquery.append(
            // "select obj from Users obj WHERE obj.email ='" + keyArray[i] + "' and
            // obj.presentStatus=1");
            // List<Users> orgusers = genericDao.executeSimpleQuery(sbquery.toString(), em);
            // if (!orgusers.isEmpty()
            // && orgusers.get(0).getOrganization().getId() ==
            // user.getOrganization().getId()) {
            // orgtxnregistereduser.put(keyArray[i].toString(),
            // CreatorActor.expenseregistrered.get(keyArray[i]));
            // }
            // }
            String txnResultDesc = "";
            if (transaction.getNetAmountResultDescription() != null
                    && !transaction.getNetAmountResultDescription().equals("null")) {
                txnResultDesc = transaction.getNetAmountResultDescription();
            }
            String invoiceNumber = transaction.getInvoiceNumber() == null ? "" : transaction.getInvoiceNumber();
            KarvyAuthorization karvyAPICall = new KarvyAuthorization(application);
            karvyAPICall.saveGSTFilingData(user, transaction, em);
            Integer typeOfSupply = transaction.getTypeOfSupply() == null ? 0 : transaction.getTypeOfSupply();
            String txnDocument = transaction.getSupportingDocs() == null ? "" : transaction.getSupportingDocs();
            if (isPettyCashLimitExceded) {
                TransactionViewResponse.addActionTxn(transaction.getId(), branchName, projectName, itemName,
                        itemParentName, "",
                        "", "", "", customerVendorName, transaction.getTransactionPurpose().getTransactionPurpose(),
                        IdosConstants.idosdf.format(transaction.getTransactionDate()), invoiceDateLabel, invoiceDate,
                        paymentMode, transaction.getNoOfUnits(), transaction.getPricePerUnit(),
                        transaction.getGrossAmount(), transaction.getNetAmount(), txnResultDesc, "",
                        transaction.getTransactionStatus(), transaction.getCreatedBy().getEmail(), approverLabel,
                        approverEmail, txnDocument, remarks, debitCredit, approverEmails, additionalApprovarUsers,
                        selectedAdditionalApproval, txnSpecialStatus,
                        transaction.getFrieghtCharges(), "", "", "", transaction.getTransactionPurpose().getId(), "",
                        "", 0, transaction.getTransactionRefNumber(), 0l, 0.0, 0, typeOfSupply, result);
            } else {
                TransactionViewResponse.addTxn(transaction.getId(), branchName, projectName, itemName, itemParentName,
                        customerVendorName, transaction.getTransactionPurpose().getTransactionPurpose(),
                        IdosConstants.IDOSDF.format(transaction.getTransactionDate()), invoiceDateLabel, invoiceDate,
                        paymentMode, transaction.getNoOfUnits(), transaction.getPricePerUnit(),
                        transaction.getGrossAmount(), transaction.getNetAmount(), txnResultDesc, "",
                        transaction.getTransactionStatus(), transaction.getCreatedBy().getEmail(), approverEmail,
                        approverEmail, txnDocument, remarks, debitCredit, txnSpecialStatus,
                        transaction.getFrieghtCharges(), "", "", "", transaction.getTransactionPurpose().getId(),
                        invoiceNumber, transaction.getTransactionRefNumber(), typeOfSupply, result);
            }

        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on submit for accounting Buy on petty.", ex.getMessage());
        }
        return transaction;
    }

    @Override
    public boolean getAdvanceDiscount(Users user, EntityManager em, JsonNode json, ObjectNode result)
            throws IDOSException {
        long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
        long txnVendCustId = json.findValue("txnVendCustId") == null ? 0L : json.findValue("txnVendCustId").asLong();
        Long txnVendCustItemId = json.findValue("txnVendCustItemId").asLong();
        int txnTypeOfSupply = json.findValue("txnTypeOfSupply") == null ? 0 : json.findValue("txnTypeOfSupply").asInt();
        Long txnPlaceOfSupplyId = json.findValue("destinGstinId") == null ? 0
                : json.findValue("destinGstinId").asLong();
        Integer txnWithWithoutTax = json.findValue("txnWithWithoutTax") == null ? null
                : json.findValue("txnWithWithoutTax").asInt();
        String txnPlaceOfSupply = json.findValue("txnPlaceOfSupply") == null ? null
                : json.findValue("txnPlaceOfSupply").asText();
        String txnVisitingCustomer = json.findValue("txnvisitingCustomer") == null ? null
                : json.findValue("txnvisitingCustomer").asText();
        long txnBranchId = json.findValue("txnBranchId").asLong();
        String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
        Date txnDate = null;
        try {
            if (selectedTxnDate != null && !"".equals(selectedTxnDate)) {
                txnDate = IdosConstants.IDOSDF.parse(selectedTxnDate);
            } else {
                txnDate = new Date();
            }
        } catch (ParseException e) {
            throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION,
                    IdosConstants.NULL_KEY_EXC_ESMF_MSG,
                    "cannot parse date: " + selectedTxnDate + " " + e.getMessage());
        }

        ArrayNode vendorAdvanceUnitPricean = result.putArray("vendorAdvanceUnitPriceData");
        Double advAmount = 0d, adjuestedAmt = 0d;
        Query query = null;
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, VENDOR_TXN_HQL);
        query = em.createQuery(VENDOR_TXN_HQL);
        query.setParameter(1, user.getOrganization().getId());
        query.setParameter(2, txnBranchId);
        query.setParameter(3, txnVendCustItemId);
        query.setParameter(4, user.getOrganization().getId());
        query.setParameter(5, txnBranchId);
        query.setParameter(6, txnPlaceOfSupply);
        query.setParameter(7, txnTypeOfSupply);
        query.setParameter(8, txnVendCustId);
        query.setParameter(9, txnDate);

        List<Object[]> txnLists = query.getResultList();
        for (Object[] val : txnLists) {
            advAmount = val[0] != null ? IdosUtil.convertStringToDouble(String.valueOf(val[0])) : 0.0;
            adjuestedAmt = val[1] != null ? IdosUtil.convertStringToDouble(String.valueOf(val[1])) : 0.0;
        }
        advAmount -= adjuestedAmt;

        boolean isBackDated = DateUtil.isBackDate(txnDate);
        if (isBackDated) {
            query.setParameter(9, new Date());
            txnLists = query.getResultList();
            double advAmountNow = 0.0, adjuestedAmtNow = 0.0;
            for (Object[] val : txnLists) {
                advAmountNow = val[0] != null ? IdosUtil.convertStringToDouble(String.valueOf(val[0])) : 0.0;
                adjuestedAmtNow = val[1] != null ? IdosUtil.convertStringToDouble(String.valueOf(val[1])) : 0.0;
            }
            advAmountNow -= adjuestedAmtNow;
            if (advAmount > advAmountNow) {
                advAmount = advAmountNow;
            }
        }
        // if (txnPurposeVal == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
        // || txnPurposeVal == IdosConstants.BUY_ON_CREDIT_PAY_LATER
        // || txnPurposeVal == IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER
        // || txnPurposeVal == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) {
        // VendorBranchWiseAdvBalance itemAdvAmount = VendorBranchWiseAdvBalance
        // .getAdvAmountForItem(em, user.getOrganization().getId(), txnVendCustId,
        // txnBranchId,
        // txnTypeOfSupply, txnPlaceOfSupplyId, txnVendCustItemId);
        // if (itemAdvAmount != null) {
        // advAmount += itemAdvAmount.getAdvanceAmount();
        // }
        // }

        ObjectNode row = Json.newObject();
        row.put("vendAdvanceMoney", IdosConstants.decimalFormat.format(advAmount));
        Map<String, Object> criterias = new HashMap<String, Object>();
        if (txnVendCustItemId != null) {
            criterias.put("vendorSpecific.id", txnVendCustId);
            criterias.put("specificsVendors.id", txnVendCustItemId);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("branch.id", txnBranchId);
            criterias.put("presentStatus", 1);
            VendorSpecific vendorTxnSpecifics = genericDao.getByCriteria(VendorSpecific.class, criterias, em);
            if (vendorTxnSpecifics != null && vendorTxnSpecifics.getUnitPrice() != null) {
                if (vendorTxnSpecifics.getUnitPrice() > 0) {
                    row.put("vendUnitPrice", vendorTxnSpecifics.getUnitPrice().toString());
                } else {
                    row.put("vendUnitPrice", "");
                }
            } else {
                row.put("vendUnitPrice", "");
            }
        }
        vendorAdvanceUnitPricean.add(row);
        return true;
    }

    @Override
    public void saveUpdateBudget4Items(Transaction txn, Users user, EntityManager em) throws IDOSException {
        List<TransactionItems> txnItemList = TransactionItems.finfByTxnId(em, txn.getId());
        for (TransactionItems txnItem : txnItemList) {
            saveUpdateBudget(txn.getTransactionBranch(), txnItem, user, em);
        }
    }

    @Override
    public void saveUpdateBudget(Branch txnBranch, TransactionItems txnItem, Users user, EntityManager em)
            throws IDOSException {
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        Double txnTotalNetAmount = txnItem.getGrossAmount();
        if (txnItem != null) {
            ArrayList inparams = new ArrayList();
            inparams.add(txnBranch.getOrganization().getId());
            inparams.add(txnBranch.getId());
            inparams.add(txnItem.getTransactionSpecifics().getId());
            inparams.add(txnItem.getTransactionSpecifics().getParticularsId().getId());
            List<BranchSpecifics> list = genericDao.queryWithParams(BRANCH_SPECIFIC_JPQL, em, inparams);
            BranchSpecifics txnBnchSpef = null;
            if (list != null && list.size() > 0) {
                txnBnchSpef = list.get(0);
            } else {
                log.log(Level.SEVERE, user.getEmail() + "Specific under Branch is not configured.");
                throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                        "Specific under Branch is not configured.", "Specific under Branch is not configured.");
            }
            switch (currentMonth) {
                case 1:
                    if (txnBranch != null) {
                        if (txnBranch.getBudgetDeductedAmountJan() != null) {
                            txnBranch.setBudgetDeductedAmountJan(
                                    txnBranch.getBudgetDeductedAmountJan() + txnTotalNetAmount);
                            txnBranch.setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBranch.setBudgetDeductedAmountJan(txnTotalNetAmount);
                            if (txnBranch.getBudgetDeductedTotal() != null) {
                                txnBranch
                                        .setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBranch.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    if (txnBnchSpef != null) {
                        if (txnBnchSpef.getBudgetDeductedAmountJan() != null) {
                            txnBnchSpef.setBudgetDeductedAmountJan(
                                    txnBnchSpef.getBudgetDeductedAmountJan() + txnTotalNetAmount);
                            txnBnchSpef
                                    .setBudgetDeductedTotal(txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBnchSpef.setBudgetDeductedAmountJan(txnTotalNetAmount);
                            if (txnBnchSpef.getBudgetDeductedTotal() != null) {
                                txnBnchSpef.setBudgetDeductedTotal(
                                        txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBnchSpef.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    break;
                case 2:
                    if (txnBranch != null) {
                        if (txnBranch.getBudgetDeductedAmountFeb() != null) {
                            txnBranch.setBudgetDeductedAmountFeb(
                                    txnBranch.getBudgetDeductedAmountFeb() + txnTotalNetAmount);
                            txnBranch.setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBranch.setBudgetDeductedAmountFeb(txnTotalNetAmount);
                            if (txnBranch.getBudgetDeductedTotal() != null) {
                                txnBranch
                                        .setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBranch.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    if (txnBnchSpef != null) {
                        if (txnBnchSpef.getBudgetDeductedAmountFeb() != null) {
                            txnBnchSpef.setBudgetDeductedAmountFeb(
                                    txnBnchSpef.getBudgetDeductedAmountFeb() + txnTotalNetAmount);
                            txnBnchSpef
                                    .setBudgetDeductedTotal(txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBranch.setBudgetDeductedAmountFeb(txnTotalNetAmount);
                            if (txnBnchSpef.getBudgetDeductedTotal() != null) {
                                txnBnchSpef.setBudgetDeductedTotal(
                                        txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBnchSpef.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    break;
                case 3:
                    if (txnBranch != null) {
                        if (txnBranch.getBudgetDeductedAmountMar() != null) {
                            txnBranch.setBudgetDeductedAmountMar(
                                    txnBranch.getBudgetDeductedAmountMar() + txnTotalNetAmount);
                            txnBranch.setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBranch.setBudgetDeductedAmountMar(txnTotalNetAmount);
                            if (txnBranch.getBudgetDeductedTotal() != null) {
                                txnBranch
                                        .setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBranch.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    if (txnBnchSpef != null) {
                        if (txnBnchSpef.getBudgetDeductedAmountMar() != null) {
                            txnBnchSpef.setBudgetDeductedAmountMar(
                                    txnBnchSpef.getBudgetDeductedAmountMar() + txnTotalNetAmount);
                            txnBnchSpef
                                    .setBudgetDeductedTotal(txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBnchSpef.setBudgetDeductedAmountMar(txnTotalNetAmount);
                            if (txnBnchSpef.getBudgetDeductedTotal() != null) {
                                txnBnchSpef.setBudgetDeductedTotal(
                                        txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBnchSpef.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    break;
                case 4:
                    if (txnBranch != null) {
                        if (txnBranch.getBudgetDeductedAmountApr() != null) {
                            txnBranch.setBudgetDeductedAmountApr(
                                    txnBranch.getBudgetDeductedAmountApr() + txnTotalNetAmount);
                            txnBranch.setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBranch.setBudgetDeductedAmountApr(txnTotalNetAmount);
                            if (txnBranch.getBudgetDeductedTotal() != null) {
                                txnBranch
                                        .setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBranch.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    if (txnBnchSpef != null) {
                        if (txnBnchSpef.getBudgetDeductedAmountApr() != null) {
                            txnBnchSpef.setBudgetDeductedAmountApr(
                                    txnBnchSpef.getBudgetDeductedAmountApr() + txnTotalNetAmount);
                            txnBnchSpef
                                    .setBudgetDeductedTotal(txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBnchSpef.setBudgetDeductedAmountApr(txnTotalNetAmount);
                            if (txnBnchSpef.getBudgetDeductedTotal() != null) {
                                txnBnchSpef.setBudgetDeductedTotal(
                                        txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBnchSpef.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    break;
                case 5:
                    if (txnBranch != null) {
                        if (txnBranch.getBudgetDeductedAmountMay() != null) {
                            txnBranch.setBudgetDeductedAmountMay(
                                    txnBranch.getBudgetDeductedAmountMay() + txnTotalNetAmount);
                            txnBranch.setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBranch.setBudgetDeductedAmountMay(txnTotalNetAmount);
                            if (txnBranch.getBudgetDeductedTotal() != null) {
                                txnBranch
                                        .setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBranch.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    if (txnBnchSpef != null) {
                        if (txnBnchSpef.getBudgetDeductedAmountMay() != null) {
                            txnBnchSpef.setBudgetDeductedAmountMay(
                                    txnBnchSpef.getBudgetDeductedAmountMay() + txnTotalNetAmount);
                            txnBnchSpef
                                    .setBudgetDeductedTotal(txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBnchSpef.setBudgetDeductedAmountMay(txnTotalNetAmount);
                            if (txnBnchSpef.getBudgetDeductedTotal() != null) {
                                txnBnchSpef.setBudgetDeductedTotal(
                                        txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBnchSpef.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    break;
                case 6:
                    if (txnBranch != null) {
                        if (txnBranch.getBudgetDeductedAmountJune() != null) {
                            txnBranch.setBudgetDeductedAmountJune(
                                    txnBranch.getBudgetDeductedAmountJune() + txnTotalNetAmount);
                            txnBranch.setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBranch.setBudgetDeductedAmountJune(txnTotalNetAmount);
                            if (txnBranch.getBudgetDeductedTotal() != null) {
                                txnBranch
                                        .setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBranch.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    if (txnBnchSpef != null) {
                        if (txnBnchSpef.getBudgetDeductedAmountJune() != null) {
                            txnBnchSpef.setBudgetDeductedAmountJune(
                                    txnBnchSpef.getBudgetDeductedAmountJune() + txnTotalNetAmount);
                            txnBnchSpef
                                    .setBudgetDeductedTotal(txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBnchSpef.setBudgetDeductedAmountJune(txnTotalNetAmount);
                            if (txnBnchSpef.getBudgetDeductedTotal() != null) {
                                txnBnchSpef.setBudgetDeductedTotal(
                                        txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBnchSpef.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    break;
                case 7:
                    if (txnBranch != null) {
                        if (txnBranch.getBudgetDeductedAmountJuly() != null) {
                            txnBranch.setBudgetDeductedAmountJuly(
                                    txnBranch.getBudgetDeductedAmountJuly() + txnTotalNetAmount);
                            txnBranch.setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBranch.setBudgetDeductedAmountJuly(txnTotalNetAmount);
                            if (txnBranch.getBudgetDeductedTotal() != null) {
                                txnBranch
                                        .setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBranch.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    if (txnBnchSpef != null) {
                        if (txnBnchSpef.getBudgetDeductedAmountJuly() != null) {
                            txnBnchSpef.setBudgetDeductedAmountJuly(
                                    txnBnchSpef.getBudgetDeductedAmountJuly() + txnTotalNetAmount);
                            txnBnchSpef
                                    .setBudgetDeductedTotal(txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBnchSpef.setBudgetDeductedAmountJuly(txnTotalNetAmount);
                            if (txnBnchSpef.getBudgetDeductedTotal() != null) {
                                txnBnchSpef.setBudgetDeductedTotal(
                                        txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBnchSpef.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    break;
                case 8:
                    if (txnBranch != null) {
                        if (txnBranch.getBudgetDeductedAmountAug() != null) {
                            txnBranch.setBudgetDeductedAmountAug(
                                    txnBranch.getBudgetDeductedAmountAug() + txnTotalNetAmount);
                            txnBranch.setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBranch.setBudgetDeductedAmountAug(txnTotalNetAmount);
                            if (txnBranch.getBudgetDeductedTotal() != null) {
                                txnBranch
                                        .setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBranch.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    if (txnBnchSpef != null) {
                        if (txnBnchSpef.getBudgetDeductedAmountAug() != null) {
                            txnBnchSpef.setBudgetDeductedAmountAug(
                                    txnBnchSpef.getBudgetDeductedAmountAug() + txnTotalNetAmount);
                            txnBnchSpef
                                    .setBudgetDeductedTotal(txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBnchSpef.setBudgetDeductedAmountAug(txnTotalNetAmount);
                            if (txnBnchSpef.getBudgetDeductedTotal() != null) {
                                txnBnchSpef.setBudgetDeductedTotal(
                                        txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBnchSpef.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    break;
                case 9:
                    if (txnBranch != null) {
                        if (txnBranch.getBudgetDeductedAmountSep() != null) {
                            txnBranch.setBudgetDeductedAmountSep(
                                    txnBranch.getBudgetDeductedAmountSep() + txnTotalNetAmount);
                            txnBranch.setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBranch.setBudgetDeductedAmountSep(txnTotalNetAmount);
                            if (txnBranch.getBudgetDeductedTotal() != null) {
                                txnBranch
                                        .setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBranch.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    if (txnBnchSpef != null) {
                        if (txnBnchSpef.getBudgetDeductedAmountSep() != null) {
                            txnBnchSpef.setBudgetDeductedAmountSep(
                                    txnBnchSpef.getBudgetDeductedAmountSep() + txnTotalNetAmount);
                            txnBnchSpef
                                    .setBudgetDeductedTotal(txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBnchSpef.setBudgetDeductedAmountSep(txnTotalNetAmount);
                            if (txnBnchSpef.getBudgetDeductedTotal() != null) {
                                txnBnchSpef.setBudgetDeductedTotal(
                                        txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBnchSpef.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    break;
                case 10:
                    if (txnBnchSpef != null) {
                        if (txnBranch.getBudgetDeductedAmountOct() != null) {
                            txnBranch.setBudgetDeductedAmountOct(
                                    txnBranch.getBudgetDeductedAmountOct() + txnTotalNetAmount);
                            txnBranch.setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBranch.setBudgetDeductedAmountOct(txnTotalNetAmount);
                            if (txnBranch.getBudgetDeductedTotal() != null) {
                                txnBranch
                                        .setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBranch.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    if (txnBnchSpef != null) {
                        if (txnBnchSpef.getBudgetDeductedAmountOct() != null) {
                            txnBnchSpef.setBudgetDeductedAmountOct(
                                    txnBnchSpef.getBudgetDeductedAmountOct() + txnTotalNetAmount);
                            txnBnchSpef
                                    .setBudgetDeductedTotal(txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBnchSpef.setBudgetDeductedAmountOct(txnTotalNetAmount);
                            if (txnBnchSpef.getBudgetDeductedTotal() != null) {
                                txnBnchSpef.setBudgetDeductedTotal(
                                        txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBnchSpef.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    break;
                case 11:
                    if (txnBnchSpef != null) {
                        if (txnBranch.getBudgetDeductedAmountNov() != null) {
                            txnBranch.setBudgetDeductedAmountNov(
                                    txnBranch.getBudgetDeductedAmountNov() + txnTotalNetAmount);
                            txnBranch.setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBranch.setBudgetDeductedAmountNov(txnTotalNetAmount);
                            if (txnBranch.getBudgetDeductedTotal() != null) {
                                txnBranch
                                        .setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBranch.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    if (txnBnchSpef != null) {
                        if (txnBnchSpef.getBudgetDeductedAmountNov() != null) {
                            txnBnchSpef.setBudgetDeductedAmountNov(
                                    txnBnchSpef.getBudgetDeductedAmountNov() + txnTotalNetAmount);
                            txnBnchSpef
                                    .setBudgetDeductedTotal(txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBnchSpef.setBudgetDeductedAmountNov(txnTotalNetAmount);
                            if (txnBnchSpef.getBudgetDeductedTotal() != null) {
                                txnBnchSpef.setBudgetDeductedTotal(
                                        txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBnchSpef.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    break;
                case 12:
                    if (txnBnchSpef != null) {
                        if (txnBranch.getBudgetDeductedAmountDec() != null) {
                            txnBranch.setBudgetDeductedAmountDec(
                                    txnBranch.getBudgetDeductedAmountDec() + txnTotalNetAmount);
                            txnBranch.setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBranch.setBudgetDeductedAmountDec(txnTotalNetAmount);
                            if (txnBranch.getBudgetDeductedTotal() != null) {
                                txnBranch
                                        .setBudgetDeductedTotal(txnBranch.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBranch.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    if (txnBnchSpef != null) {
                        if (txnBnchSpef.getBudgetDeductedAmountDec() != null) {
                            txnBnchSpef.setBudgetDeductedAmountDec(
                                    txnBnchSpef.getBudgetDeductedAmountDec() + txnTotalNetAmount);
                            txnBnchSpef
                                    .setBudgetDeductedTotal(txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                        } else {
                            txnBnchSpef.setBudgetDeductedAmountDec(txnTotalNetAmount);
                            if (txnBnchSpef.getBudgetDeductedTotal() != null) {
                                txnBnchSpef.setBudgetDeductedTotal(
                                        txnBnchSpef.getBudgetDeductedTotal() + txnTotalNetAmount);
                            } else {
                                txnBnchSpef.setBudgetDeductedTotal(txnTotalNetAmount);
                            }
                        }
                    }
                    break;
            }
            genericDao.saveOrUpdate(txnBranch, user, em);
            genericDao.saveOrUpdate(txnBnchSpef, user, em);
        }
    }

    @Override
    public void calculateAndSaveTds(EntityManager em, Users user, Transaction txn) throws IDOSException {
        long txnPurpose = txn.getTransactionPurpose().getId();
        if (txnPurpose == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                || txnPurpose == IdosConstants.BUY_ON_CREDIT_PAY_LATER
                || txnPurpose == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT
                || txnPurpose == IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER
                || txnPurpose == IdosConstants.CREDIT_NOTE_VENDOR || txnPurpose == IdosConstants.DEBIT_NOTE_VENDOR) {
            ObjectNode result = Json.newObject();
            if (txn.getTransactionItems() != null && !txn.getTransactionItems().isEmpty()) {
                for (TransactionItems txnItem : txn.getTransactionItems()) {
                    VENDOR_TDS_DAO.calculateTds(result, null, user, txnItem.getTransactionSpecifics(),
                            txn.getTransactionVendorCustomer(), txnItem.getGrossAmount(), txnItem.getNetAmount(),
                            txnPurpose, txn.getTransactionDate(), em, true);
                }
            }
        }
    }
}
