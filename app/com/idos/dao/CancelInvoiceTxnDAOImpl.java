package com.idos.dao;

import actor.CreatorActor;
import com.idos.util.*;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import play.mvc.WebSocket;
import pojo.TransactionViewResponse;

import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.text.ParseException;
import java.util.*;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import akka.stream.javadsl.*;
import akka.actor.*;
import akka.NotUsed;

/**
 * Created by Sunil K. Namdev on 18-01-2018.
 */
public class CancelInvoiceTxnDAOImpl implements CancelInvoiceTxnDAO {
    private static JPAApi jpaApi;

    @Override
    public Transaction submitForCancellation(Users user, JsonNode json, EntityManager em, EntityTransaction et,
            ObjectNode result) throws IDOSException {
        Transaction txn = null;
        try {
            Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l
                    : json.findValue("txnEntityID").asLong();
            Long txnSellEntityID = json.findValue("txnSaleEntityID") == null ? 0l
                    : json.findValue("txnSaleEntityID").asLong();
            Transaction sellTxn = Transaction.findById(txnSellEntityID);
            if (sellTxn == null) {
                result.put("message", "Sale transaction is not found");
                return txn;
            }
            Double totalTxnTaxAmt = json.findValue("totalTxnTaxAmt") == null ? 0.0
                    : json.findValue("totalTxnTaxAmt").asDouble();
            long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
            String txnforbranch = json.findValue("txnForBranch").asText();
            String txnforproject = json.findValue("txnForProject").asText();
            String txnforcustomer = json.findValue("txnForCustomer").asText();
            String txnNetAmountDescription = json.findValue("txnNetAmountDescription").asText();
            int increaseDecrease = json.findValue("txnIncreaseDecrease") == null ? 0
                    : json.findValue("txnIncreaseDecrease").asInt();

            Double totalTxnGrossAmt = json.findValue("totalTxnGrossAmt") == null ? 0.0
                    : json.findValue("totalTxnGrossAmt").asDouble();
            Double txnTotalNetAmount = json.findValue("txnTotalNetAmount") == null ? 0.0
                    : json.findValue("txnTotalNetAmount").asDouble();
            Double netAmountTotalWithDecimalValue = json.findValue("totalTxnNetAmtWithDecimalValue").asDouble();
            String txnremarks = json.findValue("txnRemarks").asText();
            String supportingdoc = json.findValue("supportingdoc").asText();
            String txnForItemStr = json.findValue("txnForItem").toString();

            String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
            Date txnDate = null;
            try {
                if (selectedTxnDate != null) {
                    txnDate = IdosConstants.IDOSDF.parse(selectedTxnDate);
                } else {
                    txnDate = new Date();
                }
            } catch (ParseException e) {
                throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION,
                        IdosConstants.NULL_KEY_EXC_ESMF_MSG,
                        "cannot parse date: " + selectedTxnDate + " " + e.getMessage());
            }
            JSONArray arrJSON = new JSONArray(txnForItemStr);
            TransactionPurpose usertxnPurpose = TransactionPurpose.findById(txnPurposeVal);

            if (txnEntityID > 0) {
                txn = Transaction.findById(txnEntityID);
            } else {
                txn = new Transaction();
            }
            Branch txnBranch = null;
            String branchName = "";
            if (txnforbranch != null && !txnforbranch.equals("")) {
                txnBranch = genericDao.getById(Branch.class, Long.parseLong(txnforbranch), em);
                branchName = txnBranch.getName();
            }

            Project txnProject = null;
            String projectName = "";
            if (txnforproject != null && !txnforproject.equals("")) {
                txnProject = genericDao.getById(Project.class, IdosUtil.convertStringToLong(txnforproject), em);
                projectName = txnProject.getName();
            }

            for (int i = 0; i < arrJSON.length(); i++) {
                JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                String itemIdList = rowItemData.getString("txnItems");
            }

            JSONObject firstRowItemData = new JSONObject(arrJSON.get(0).toString());
            Double txnPerUnitPriceRow0 = firstRowItemData.getDouble("txnPerUnitPrice");
            Double txnNoOfUniRow0t = firstRowItemData.getDouble("txnNoOfUnit");
            txn.setLinkedTxnRef(sellTxn.getTransactionRefNumber());
            txn.setTransactionSpecifics(sellTxn.getTransactionSpecifics());
            txn.setTransactionParticulars(sellTxn.getTransactionParticulars());
            txn.setNoOfUnits(txnNoOfUniRow0t);
            txn.setPricePerUnit(txnPerUnitPriceRow0);
            txn.setGrossAmount(totalTxnGrossAmt);
            txn.setSourceGstin(sellTxn.getSourceGstin());
            txn.setDestinationGstin(sellTxn.getDestinationGstin());
            txn.setTypeOfSupply(sellTxn.getTypeOfSupply());
            txn.setWithWithoutTax(sellTxn.getWithWithoutTax());
            txn.setWalkinCustomerType(sellTxn.getWalkinCustomerType());
            txn.setTransactionPurpose(usertxnPurpose);
            txn.setTransactionBranch(txnBranch);
            txn.setTransactionBranchOrganization(txnBranch.getOrganization());
            txn.setTransactionProject(txnProject);
            txn.setTransactionVendorCustomer(sellTxn.getTransactionVendorCustomer());
            txn.setPoReference(sellTxn.getPoReference());
            txn.setNetAmount(txnTotalNetAmount);
            txn.setNetAmountResultDescription(txnNetAmountDescription);
            Double roundedCutPartOfNetAmount = txnTotalNetAmount - netAmountTotalWithDecimalValue;
            txn.setRoundedCutPartOfNetAmount(roundedCutPartOfNetAmount);
            txn.setCustomerNetPayment(sellTxn.getCustomerNetPayment());
            txn.setTransactionDate(txnDate);
            String txnRemarks = "";
            if (!txnremarks.equals("") && txnremarks != null) {
                txnRemarks = user.getEmail() + "#" + txnremarks;
                txn.setRemarks(txnRemarks);
                txnRemarks = txn.getRemarks();
            }
            txn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(txn.getSupportingDocs(),
                    user.getEmail(), supportingdoc, user, em));
            if (ConfigParams.getInstance().isDeploymentSingleUser(user).booleanValue()) {
                txn.setTransactionStatus(IdosConstants.TXN_STATUS_ACCOUNTED);
            } else {
                txn.setTransactionStatus(IdosConstants.TXN_STATUS_REQUIRE_APPROVAL);
                // list of additional users all approver role users of thet organization
                Map<String, Object> criterias = new HashMap<String, Object>();
                criterias.put("role.name", "APPROVER");
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("presentStatus", 1);
                List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, em);
                String approverEmails = "";
                String additionalApprovarUsers = "";
                String selectedAdditionalApproval = "";
                for (UsersRoles usrRoles : approverRole) {
                    additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
                    criterias.clear();
                    criterias.put("user.id", usrRoles.getUser().getId());
                    criterias.put("userRights.id", 2L);
                    criterias.put("branch.id", txnBranch.getId());
                    criterias.put("presentStatus", 1);
                    UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class,
                            criterias, em);
                    if (userHasRightInBranch != null) {
                        // check for right in chart of accounts
                        criterias.clear();
                        criterias.put("user.id", usrRoles.getUser().getId());
                        criterias.put("userRights.id", 2L);
                        criterias.put("specifics.id", txn.getTransactionSpecifics().getId());
                        criterias.put("presentStatus", 1);
                        UserRightSpecifics userHasRightInCOA = genericDao.getByCriteria(UserRightSpecifics.class,
                                criterias, em);
                        if (userHasRightInCOA != null) {
                            approverEmails += usrRoles.getUser().getEmail() + ",";
                        }
                    }
                }
                txn.setApproverEmails(approverEmails);
                txn.setAdditionalApproverEmails(additionalApprovarUsers);
            }
            String refNo = CodeHelper.getForeverUniqueID("TXN", null);
            txn.setTransactionRefNumber(refNo);
            String paymentMode = null;
            if (sellTxn.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW) {
                txn.setTxnDone(1);
                txn.setCustomerDuePayment(0.0);
                txn.setCustomerNetPayment(txnTotalNetAmount);
                txn.setReceiptDetailsType(sellTxn.getReceiptDetailsType());
                if (IdosConstants.PAYMODE_CASH == sellTxn.getReceiptDetailsType()) {
                    paymentMode = "CASH";
                    branchCashService.updateBranchCashDetail(em, user, txnBranch, txnTotalNetAmount, true,
                            txn.getTransactionDate(), result);
                } else if (IdosConstants.PAYMODE_BANK == sellTxn.getReceiptDetailsType()) {
                    paymentMode = "BANK";
                    txn.setTransactionBranchBankAccount(sellTxn.getTransactionBranchBankAccount());
                    txn.setInstrumentNumber(sellTxn.getInstrumentNumber());
                    txn.setInstrumentDate(sellTxn.getInstrumentDate());
                    boolean branchBankDetailEntered = branchBankService.updateBranchBankDetailTransaction(em, user,
                            sellTxn.getTransactionBranchBankAccount(), txnTotalNetAmount, true, result,
                            txn.getTransactionDate(), txn.getTransactionBranch());
                    if (!branchBankDetailEntered) {
                        return txn; // since balance is in -ve don't make any changes in DB
                    }
                } else {
                    paymentMode = "";
                }
            } else {
                paymentMode = "";
            }
            txn.setPaymentStatus("NOT-PAID");
            if (sellTxn.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW) {
                txn.setTypeIdentifier((int) IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW);
            } else {
                txn.setTypeIdentifier((int) IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER);
            }
            genericDao.saveOrUpdate(txn, user, em);
            if (txnEntityID > 0) {
                transactionItemsService.updateMultipleItemsTransactionItems(em, user, arrJSON, txn);
            } else {
                transactionItemsService.insertMultipleItemsTransactionItems(em, user, arrJSON, txn, txnDate);
            }
            genericDao.saveOrUpdate(txn, user, em); // need to update becuase of desgin issue, as need to set total
                                                    // taxes amount
            FILE_UPLOAD_DAO.updateUploadFileLogs(em, user, supportingdoc, txn.getId(), IdosConstants.MAIN_TXN_TYPE);
            if (txn.getTransactionStatus().equals(IdosConstants.TXN_STATUS_ACCOUNTED)) {
                TRANSACTION_DAO.setInvoiceQuotProfSerial(user, em, txn);
                INVOICE_DAO.saveInvoiceLog(user, em, txn, null, json);
                CREATE_TRIAL_BALANCE_DAO.insertTrialBalance(txn, user, em);
                if (txn.getRoundedCutPartOfNetAmount() != null && txn.getRoundedCutPartOfNetAmount() != 0.0) {
                    Boolean roundupMappingFound = null;
                    if (txn.getRoundedCutPartOfNetAmount() > 0) {
                        roundupMappingFound = CREATE_TRIAL_BALANCE_DAO.saveTrialBalanceForRoundOff(
                                txn.getTransactionBranchOrganization(), txn.getTransactionBranch(), txn.getId(),
                                txn.getTransactionPurpose(), txn.getTransactionDate(),
                                txn.getRoundedCutPartOfNetAmount(), user, em, false);
                    } else {
                        roundupMappingFound = CREATE_TRIAL_BALANCE_DAO.saveTrialBalanceForRoundOff(
                                txn.getTransactionBranchOrganization(), txn.getTransactionBranch(), txn.getId(),
                                txn.getTransactionPurpose(), txn.getTransactionDate(),
                                txn.getRoundedCutPartOfNetAmount(), user, em, true);
                    }
                    result.put("roundupMappingFound", roundupMappingFound);
                    if (roundupMappingFound == null || !roundupMappingFound) {
                        return txn;
                    }
                }
                STOCK_SERVICE.insertTradingInventory(txn, user, em);
            }
            et.commit();

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

            String invoiceDate = "";
            String invoiceDateLabel = "";
            if (txn.getTransactionInvoiceDate() != null) {
                invoiceDateLabel = "INVOICE DATE:";
                invoiceDate = IdosConstants.idosdf.format(txn.getTransactionInvoiceDate());
            }
            String customerVendorName = txn.getTransactionVendorCustomer() == null
                    ? txn.getTransactionUnavailableVendorCustomer()
                    : txn.getTransactionVendorCustomer().getName();
            String txnResultDesc = "";
            if (txn.getNetAmountResultDescription() != null && !txn.getNetAmountResultDescription().equals("null")) {
                txnResultDesc = txn.getNetAmountResultDescription();
            }
            String invoiceNumber = txn.getInvoiceNumber() == null ? "" : txn.getInvoiceNumber();
            String itemName = "";
            String itemParentName = "";
            String txnSpecialStatus = "";
            String tranInvoice = txn.getInvoiceNumber();
            String txnInstrumentNumber = txn.getInstrumentNumber();
            String txnInstrumentDate = txn.getInstrumentDate() == null ? "" : txn.getInstrumentDate();
            Integer typeOfSupply = txn.getTypeOfSupply() == null ? 0 : txn.getTypeOfSupply();
            String txnDocument = txn.getSupportingDocs() == null ? "" : txn.getSupportingDocs();
            TransactionViewResponse.addTxn(txn.getId(), branchName, projectName, itemName, itemParentName,
                    customerVendorName,
                    txn.getTransactionPurpose().getTransactionPurpose(),
                    IdosConstants.IDOSDF.format(txn.getTransactionDate()), invoiceDateLabel, invoiceDate, paymentMode,
                    txn.getNoOfUnits(), txn.getPricePerUnit(), txn.getGrossAmount(), txn.getNetAmount(), txnResultDesc,
                    "", txn.getTransactionStatus(), txn.getCreatedBy().getEmail(), "", "", txnDocument, txnRemarks, "",
                    txnSpecialStatus, txn.getFrieghtCharges(), txn.getPoReference(),
                    txnInstrumentNumber, txnInstrumentDate, txn.getTransactionPurpose().getId(), tranInvoice,
                    txn.getTransactionRefNumber(), typeOfSupply, result);
        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on submit for cancellation", ex.getMessage());
        }
        return txn;
    }
}
