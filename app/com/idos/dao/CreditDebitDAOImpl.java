package com.idos.dao;

import actor.CreatorActor;
import com.idos.util.CodeHelper;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import java.util.logging.Level;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import play.mvc.WebSocket;
import pojo.TransactionViewResponse;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.text.ParseException;
import java.util.*;

import static service.BaseService.invDAO;
import javax.inject.Inject;
import play.db.jpa.JPAApi;
import akka.stream.javadsl.*;
import akka.actor.*;
import akka.NotUsed;

/**
 * Created by Sunil Namdev on 17-01-2018.
 */
public class CreditDebitDAOImpl implements CreditDebitDAO {
    private static JPAApi jpaApi;

    @Override
    public Transaction submitForApproval(Users user, JsonNode json, EntityManager em, EntityTransaction et,
            ObjectNode result) throws IDOSException {
        Transaction noteTxn = null;
        try {
            Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l
                    : json.findValue("txnEntityID").asLong();
            Long txnSellEntityID = json.findValue("txnSaleEntityID") == null ? 0l
                    : json.findValue("txnSaleEntityID").asLong();
            Transaction sellTxn = Transaction.findById(txnSellEntityID);
            if (sellTxn == null) {
                result.put("message", "Sale transaction not found");
                return noteTxn;
            }
            Double totalTxnTaxAmt = json.findValue("totalTxnTaxAmt") == null ? 0.0
                    : json.findValue("totalTxnTaxAmt").asDouble();
            long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
            /*
             * if(txnPurposeVal == IdosConstants.DEBIT_NOTE_CUSTOMER){
             * if(totalTxnTaxAmt <= sellTxn.getNetAmount()){
             * result.put("message",
             * "Cannot procced debit note transaction as there is no increase in price/quantity."
             * );
             * return false;
             * }
             * }else{
             * if(totalTxnTaxAmt >= sellTxn.getNetAmount()){
             * result.put("message",
             * "Cannot procced credit note transaction as there is no decrease in price/quantity."
             * );
             * return false;
             * }
             * }
             */

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
            String txnRemarks = "";
            Branch txnBranch = null;
            String branchName = "";
            String paymentMode = "";
            Project txnProject = null;
            String projectName = "";
            String itemName = "";
            String debitCredit = "Credit";
            String txnInstrumentNumber = "";
            String txnInstrumentDate = "";
            TransactionPurpose usertxnPurpose = TransactionPurpose.findById(txnPurposeVal);

            if (txnEntityID > 0) {
                noteTxn = Transaction.findById(txnEntityID);
            } else {
                noteTxn = new Transaction();
            }
            if (txnforbranch != null && !txnforbranch.equals("")) {
                txnBranch = genericDao.getById(Branch.class, IdosUtil.convertStringToLong(txnforbranch), em);
                branchName = txnBranch.getName();
            }
            if (txnforproject != null && !txnforproject.equals("")) {
                txnProject = genericDao.getById(Project.class, IdosUtil.convertStringToLong(txnforproject), em);
                projectName = txnProject.getName();
            }

            noteTxn.setPaymentStatus("NOT-PAID");
            noteTxn.setCustomerDuePayment(txnTotalNetAmount);

            for (int i = 0; i < arrJSON.length(); i++) {
                JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                String itemIdList = rowItemData.getString("txnItems");
            }

            // Enter data for first item in noteTxn table to be displayed in Transaction
            // list
            JSONObject firstRowItemData = new JSONObject(arrJSON.get(0).toString());
            Double txnPerUnitPriceRow0 = firstRowItemData.getDouble("txnPerUnitPrice");
            Double txnNoOfUniRow0t = firstRowItemData.getDouble("txnNoOfUnit");
            noteTxn.setLinkedTxnRef(sellTxn.getTransactionRefNumber());
            noteTxn.setTypeIdentifier(increaseDecrease);
            noteTxn.setTransactionSpecifics(sellTxn.getTransactionSpecifics());
            noteTxn.setTransactionParticulars(sellTxn.getTransactionParticulars());
            noteTxn.setNoOfUnits(txnNoOfUniRow0t);
            noteTxn.setPricePerUnit(txnPerUnitPriceRow0);
            noteTxn.setGrossAmount(totalTxnGrossAmt);
            noteTxn.setSourceGstin(sellTxn.getSourceGstin());
            noteTxn.setDestinationGstin(sellTxn.getDestinationGstin());
            noteTxn.setTypeOfSupply(sellTxn.getTypeOfSupply());
            noteTxn.setWithWithoutTax(sellTxn.getWithWithoutTax());
            noteTxn.setWalkinCustomerType(sellTxn.getWalkinCustomerType());
            noteTxn.setTransactionPurpose(usertxnPurpose);
            noteTxn.setTransactionBranch(txnBranch);
            noteTxn.setTransactionBranchOrganization(txnBranch.getOrganization());
            noteTxn.setTransactionProject(txnProject);
            noteTxn.setTransactionVendorCustomer(sellTxn.getTransactionVendorCustomer());
            noteTxn.setPoReference(sellTxn.getPoReference());
            noteTxn.setNetAmount(txnTotalNetAmount);
            noteTxn.setNetAmountResultDescription(txnNetAmountDescription);
            Double roundedCutPartOfNetAmount = txnTotalNetAmount - netAmountTotalWithDecimalValue;
            noteTxn.setRoundedCutPartOfNetAmount(roundedCutPartOfNetAmount);
            noteTxn.setTransactionDate(txnDate);
            if (!txnremarks.equals("") && txnremarks != null) {
                txnRemarks = user.getEmail() + "#" + txnremarks;
                noteTxn.setRemarks(txnRemarks);
                txnRemarks = noteTxn.getRemarks();
            }
            noteTxn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(noteTxn.getSupportingDocs(),
                    user.getEmail(), supportingdoc, user, em));
            noteTxn.setTransactionStatus("Require Approval");
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
                UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class, criterias,
                        em);
                if (userHasRightInBranch != null) {
                    // check for right in chart of accounts
                    criterias.clear();
                    criterias.put("user.id", usrRoles.getUser().getId());
                    criterias.put("userRights.id", 2L);
                    criterias.put("specifics.id", noteTxn.getTransactionSpecifics().getId());
                    criterias.put("presentStatus", 1);
                    UserRightSpecifics userHasRightInCOA = genericDao.getByCriteria(UserRightSpecifics.class, criterias,
                            em);
                    if (userHasRightInCOA != null) {
                        approverEmails += usrRoles.getUser().getEmail() + ",";
                    }
                }
            }
            noteTxn.setApproverEmails(approverEmails);
            noteTxn.setAdditionalApproverEmails(additionalApprovarUsers);
            String refNo = CodeHelper.getForeverUniqueID("TXN", null);
            noteTxn.setTransactionRefNumber(refNo);
            genericDao.saveOrUpdate(noteTxn, user, em);
            FILE_UPLOAD_DAO.updateUploadFileLogs(em, user, supportingdoc, noteTxn.getId(), IdosConstants.MAIN_TXN_TYPE);
            // Enter multiple items data into TransactionItems table
            if (txnEntityID > 0) {
                transactionItemsService.updateMultipleItemsTransactionItems(em, user, arrJSON, noteTxn);
            } else {
                transactionItemsService.insertMultipleItemsTransactionItems(em, user, arrJSON, noteTxn, txnDate);
            }
            genericDao.saveOrUpdate(noteTxn, user, em); // need to update becuase of desgin issue, as need to set total
                                                        // taxes amount
            invDAO.saveInvoiceLog(user, em, noteTxn, null, json); // set popup addinal details.
            et.commit();
            if (!ConfigParams.getInstance().isDeploymentSingleUser(user)) {
                // Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
                // Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
                // for (int i = 0; i < keyArray.length; i++) {
                // List<Users> orgusers = Users.findByEmailActDeact(em, (String) keyArray[i]);
                // if (!orgusers.isEmpty()
                // && orgusers.get(0).getOrganization().getId() ==
                // user.getOrganization().getId()) {
                // orgtxnregistereduser.put(keyArray[i].toString(),
                // CreatorActor.expenseregistrered.get(keyArray[i]));
                // }
                // }
                String itemParentName = "";
                if (noteTxn.getTransactionSpecifics().getParentSpecifics() != null
                        && !noteTxn.getTransactionSpecifics().getParentSpecifics().equals("")) {
                    itemParentName = noteTxn.getTransactionSpecifics().getParentSpecifics().getName();
                } else {
                    itemParentName = noteTxn.getTransactionSpecifics().getParticularsId().getName();
                }
                String approverEmail = "";
                String approverLabel = "";
                if (noteTxn.getApproverActionBy() != null) {
                    approverLabel = "APPROVER:";
                    approverEmail = noteTxn.getApproverActionBy().getEmail();
                }
                String txnSpecialStatus = "";
                if (noteTxn.getTransactionExceedingBudget() != null && noteTxn.getKlFollowStatus() != null) {
                    if (noteTxn.getTransactionExceedingBudget() == 1 && noteTxn.getKlFollowStatus() == 0) {
                        txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
                    }
                    if (noteTxn.getTransactionExceedingBudget() == 1 && noteTxn.getKlFollowStatus() == 1) {
                        txnSpecialStatus = "Transaction Exceeding Budget";
                    }
                }
                if (noteTxn.getTransactionExceedingBudget() == null && noteTxn.getKlFollowStatus() != null) {
                    if (noteTxn.getKlFollowStatus() == 0) {
                        txnSpecialStatus = "Rules Not Followed";
                    }
                }
                if (noteTxn.getTransactionExceedingBudget() != null && noteTxn.getKlFollowStatus() == null) {
                    txnSpecialStatus = "Transaction Exceeding Budget";
                }
                String txnResultDesc = "";
                if (noteTxn.getNetAmountResultDescription() != null
                        && !noteTxn.getNetAmountResultDescription().equals("null")) {
                    txnResultDesc = noteTxn.getNetAmountResultDescription();
                }
                String txnPurpose = noteTxn.getTransactionPurpose().getTransactionPurpose();
                if(noteTxn.getTransactionPurpose().getId() == IdosConstants.CREDIT_NOTE_CUSTOMER){
                    if (noteTxn.getTypeIdentifier() != null) {
                        if (noteTxn.getTypeIdentifier() == 1) {
                            txnPurpose += " - Decrease in Price";
                        } else {
                            txnPurpose += " - Decrease in Quantity";
                        }
                    }
                } else if (noteTxn.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_CUSTOMER) {
                    if (noteTxn.getTypeIdentifier() != null) {
                        if (noteTxn.getTypeIdentifier() == 1) {
                            txnPurpose += " - Increase in Price";
                        } else {
                            txnPurpose += " - Increase in Quantity";
                        }
                    }
                }
                String txnDocument = noteTxn.getSupportingDocs() == null ? "" : noteTxn.getSupportingDocs();
                TransactionViewResponse.addActionTxn(noteTxn.getId(), branchName, projectName, itemName, itemParentName,
                        "", "",
                        "", "", noteTxn.getTransactionVendorCustomer().getName(),
                        txnPurpose,
                        IdosConstants.idosdf.format(noteTxn.getTransactionDate()), "", "", "", noteTxn.getNoOfUnits(),
                        noteTxn.getPricePerUnit(), noteTxn.getGrossAmount(), noteTxn.getNetAmount(), txnResultDesc, "",
                        noteTxn.getTransactionStatus(), noteTxn.getCreatedBy().getEmail(), approverLabel, approverEmail,
                        txnDocument, txnRemarks, "", approverEmails, additionalApprovarUsers,
                        selectedAdditionalApproval, txnSpecialStatus, 0d,
                        noteTxn.getPoReference(), "", "", noteTxn.getTransactionPurpose().getId(), "", refNo, 0,
                        noteTxn.getTransactionRefNumber(), 0l, 0.0, 0, 0, result);
            }
            // Single User
            if (ConfigParams.getInstance().isDeploymentSingleUser(user)) {
                ArrayNode singleUserAccounting = result.putArray("singleUserAccounting");
                ObjectNode createSingleuserJson = SINGLE_USER_DAO.createSingleuserJson(noteTxn, json, user);
                singleUserAccounting.add(createSingleuserJson);
            }

        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on Sell on Cash- submit for approval", ex.getMessage());
        }
        return noteTxn;
    }

    @Override
    public Transaction submitForApprovalVendor(Users user, JsonNode json, EntityManager em, EntityTransaction et,
            TransactionPurpose txnPurpose, ObjectNode result) throws IDOSException {
        Transaction noteTxn = null;
        Long txnBuyEntityID = json.findValue("txnBuyEntityID") == null ? 0l : json.findValue("txnBuyEntityID").asLong();
        Transaction buyTxn = Transaction.findById(txnBuyEntityID);
        if (buyTxn == null) {
            result.put("message", "Buy transaction not found");
            return noteTxn;
        }
        Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l
                : json.findValue("txnEntityID").asLong();
        int increaseDecrease = json.findValue("txnIncreaseDecrease") == null ? 0
                : json.findValue("txnIncreaseDecrease").asInt();
        Double txnTotalNetAmount = json.findValue("txnTotalNetAmount") == null ? 0.0
                : json.findValue("txnTotalNetAmount").asDouble();
        Long txnForBranch = json.findValue("txnForBranch").asLong();
        String projectID = json.findValue("txnForProject").asText();
        Long txnForProject = "".equals(projectID) ? null : json.findValue("txnForProject").asLong();
        String txnForItemStr = json.findValue("txnForItem").toString();

        Double totalTxnNetAmtWithDecimalValue = json.findValue("totalTxnNetAmtWithDecimalValue").asDouble();
        Double totalWithholdTaxAmt = json.findValue("totalWithholdTaxAmt") == null ? 0.0
                : json.findValue("totalWithholdTaxAmt").asDouble();
        Double totalTxnTaxAmt = json.findValue("totalTxnTaxAmt") == null ? 0.0
                : json.findValue("totalTxnTaxAmt").asDouble();
        Double totalTxnGrossAmt = json.findValue("totalTxnGrossAmt") == null ? 0.0
                : json.findValue("totalTxnGrossAmt").asDouble();
        int klfollowednotfollowed = json.findValue("klfollowednotfollowed") == null ? 1
                : json.findValue("klfollowednotfollowed").asInt();
        String txnRemarks = json.findValue("txnRemarks").asText();
        String supportingdoc = json.findValue("supportingdoc").asText();
        String txnDocumentUploadRequired = json.findValue("txnDocumentUploadRequired") != null
                ? json.findValue("txnDocumentUploadRequired").asText()
                : null;
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

        String remarks = "";
        Branch txnBranch = null;
        String branchName = "";
        String paymentMode = "";
        Project txnProject = null;
        String projectName = "";
        String debitCredit = "Credit";
        String itemName = "";
        String customerVendorName = "";
        String netAmtInputTaxDesc = "";
        try {
            if (txnEntityID > 0) {
                noteTxn = Transaction.findById(txnEntityID);
            } else {
                noteTxn = new Transaction();
            }
            if (txnForBranch != null) {
                txnBranch = Branch.findById(txnForBranch);
                branchName = txnBranch.getName();
            }
            if (txnForProject != null) {
                txnProject = Project.findById(txnForProject);
                projectName = txnProject.getName();
            }
            JSONArray arrJSON = new JSONArray(txnForItemStr);
            // Enter data for first item in noteTxn table to be displayed in Transaction
            // list
            JSONObject firstRowItemData = new JSONObject(arrJSON.get(0).toString());
            Long itemIdRow0 = firstRowItemData.getLong("txnItems");
            Specifics txnSpecificItem = Specifics.findById(itemIdRow0);
            itemName = txnSpecificItem.getName();
            Double txnPerUnitPriceRow0 = firstRowItemData.getDouble("txnPerUnitPrice");
            Double txnNoOfUniRow0t = firstRowItemData.getDouble("txnNoOfUnit");
            noteTxn.setTransactionSpecifics(txnSpecificItem);
            noteTxn.setTransactionParticulars(txnSpecificItem.getParticularsId());
            noteTxn.setNoOfUnits(txnNoOfUniRow0t);
            noteTxn.setPricePerUnit(txnPerUnitPriceRow0);
            noteTxn.setGrossAmount(totalTxnGrossAmt);
            noteTxn.setLinkedTxnRef(buyTxn.getTransactionRefNumber());
            noteTxn.setTypeIdentifier(increaseDecrease);
            noteTxn.setSourceGstin(buyTxn.getSourceGstin());
            noteTxn.setDestinationGstin(buyTxn.getDestinationGstin());
            noteTxn.setTypeOfSupply(buyTxn.getTypeOfSupply());
            noteTxn.setTransactionPurpose(txnPurpose);
            noteTxn.setTransactionBranch(txnBranch);
            noteTxn.setTransactionBranchOrganization(txnBranch.getOrganization());
            noteTxn.setTransactionProject(txnProject);
            customerVendorName = buyTxn.getTransactionVendorCustomer().getName();
            noteTxn.setTransactionVendorCustomer(buyTxn.getTransactionVendorCustomer());
            noteTxn.setKlFollowStatus(klfollowednotfollowed);
            noteTxn.setNetAmount(txnTotalNetAmount);
            Double roundedCutPartOfNetAmount = txnTotalNetAmount - totalTxnNetAmtWithDecimalValue;
            noteTxn.setRoundedCutPartOfNetAmount(roundedCutPartOfNetAmount);
            noteTxn.setTransactionDate(txnDate);
            noteTxn.setWithholdingTax(totalWithholdTaxAmt);
            if (txnRemarks != null && !txnRemarks.equals("")) {
                if (noteTxn.getRemarks() != null) {
                    noteTxn.setRemarks(txnRemarks);
                } else {
                    remarks = user.getEmail() + "#" + txnRemarks;
                    noteTxn.setRemarks(remarks);
                }
                remarks = noteTxn.getRemarks(); // fetch encoded value
            }
            if (txnDocumentUploadRequired != null) {
                if (txnDocumentUploadRequired == "true") {
                    noteTxn.setDocRuleStatus(1);
                }
            }
            noteTxn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(noteTxn.getSupportingDocs(),
                    user.getEmail(), supportingdoc, user, em));
            noteTxn.setTransactionStatus("Require Approval");
            noteTxn.setNetAmountResultDescription(noteTxn.getNetAmountResultDescription() + netAmtInputTaxDesc);
            // list of additional users all approver role users of thet organization
            Map<String, Object> criterias = new HashMap<String, Object>(2);
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
                UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class, criterias,
                        em);
                if (userHasRightInBranch != null) {
                    // check for right in chart of accounts
                    criterias.clear();
                    criterias.put("user.id", usrRoles.getUser().getId());
                    criterias.put("userRights.id", 2L);
                    criterias.put("specifics.id", txnSpecificItem.getId());
                    criterias.put("presentStatus", 1);
                    UserRightSpecifics userHasRightInCOA = genericDao.getByCriteria(UserRightSpecifics.class, criterias,
                            em);
                    if (userHasRightInCOA != null) {
                        approverEmails += usrRoles.getUser().getEmail() + ",";
                    }
                }
            }
            noteTxn.setApproverEmails(approverEmails);
            noteTxn.setAdditionalApproverEmails(additionalApprovarUsers);
            // list of approver user
            String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
            noteTxn.setTransactionRefNumber(transactionNumber);
            genericDao.saveOrUpdate(noteTxn, user, em);
            FILE_UPLOAD_DAO.updateUploadFileLogs(em, user, supportingdoc, noteTxn.getId(), IdosConstants.MAIN_TXN_TYPE);
            // Enter multiple items data into TransactionItems table
            if (txnEntityID > 0) {
                transactionItemsService.updateMultipleItemsTransactionItems(em, user, arrJSON, noteTxn);
            } else {
                transactionItemsService.insertMultipleItemsTransactionItems(em, user, arrJSON, noteTxn, txnDate);
            }
            genericDao.saveOrUpdate(noteTxn, user, em); // to set total tax values
            invDAO.saveInvoiceLog(user, em, noteTxn, null, json);
            et.commit();
            if (!ConfigParams.getInstance().isDeploymentSingleUser(user)) {
                // Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
                // Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
                // for (int i = 0; i < keyArray.length; i++) {
                // List<Users> orgusers = Users.findByEmailActDeact(em, (String) keyArray[i]);
                // if (!orgusers.isEmpty()
                // && orgusers.get(0).getOrganization().getId() ==
                // user.getOrganization().getId()) {
                // orgtxnregistereduser.put(keyArray[i].toString(),
                // CreatorActor.expenseregistrered.get(keyArray[i]));
                // }
                // }
                String itemParentName = "";
                if (noteTxn.getTransactionSpecifics().getParentSpecifics() != null
                        && !noteTxn.getTransactionSpecifics().getParentSpecifics().equals("")) {
                    itemParentName = noteTxn.getTransactionSpecifics().getParentSpecifics().getName();
                } else {
                    itemParentName = noteTxn.getTransactionSpecifics().getParticularsId().getName();
                }
                String approverEmail = "";
                String approverLabel = "";
                if (noteTxn.getApproverActionBy() != null) {
                    approverLabel = "APPROVER:";
                    approverEmail = noteTxn.getApproverActionBy().getEmail();
                }
                String txnSpecialStatus = "";
                if (noteTxn.getTransactionExceedingBudget() != null && noteTxn.getKlFollowStatus() != null) {
                    if (noteTxn.getTransactionExceedingBudget() == 1 && noteTxn.getKlFollowStatus() == 0) {
                        txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
                    }
                    if (noteTxn.getTransactionExceedingBudget() == 1 && noteTxn.getKlFollowStatus() == 1) {
                        txnSpecialStatus = "Transaction Exceeding Budget";
                    }
                }
                if (noteTxn.getTransactionExceedingBudget() == null && noteTxn.getKlFollowStatus() != null) {
                    if (noteTxn.getKlFollowStatus() == 0) {
                        txnSpecialStatus = "Rules Not Followed";
                    }
                }
                if (noteTxn.getTransactionExceedingBudget() != null && noteTxn.getKlFollowStatus() == null) {
                    txnSpecialStatus = "Transaction Exceeding Budget";
                }
                String txnResultDesc = "";
                if (noteTxn.getNetAmountResultDescription() != null
                        && !noteTxn.getNetAmountResultDescription().equals("null")) {
                    txnResultDesc = noteTxn.getNetAmountResultDescription();
                }
                if (noteTxn.getDocRuleStatus() != null && noteTxn.getTransactionExceedingBudget() != null) {
                    if (noteTxn.getDocRuleStatus() == 1 && noteTxn.getTransactionExceedingBudget() == 1) {
                        txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
                    }
                    if (noteTxn.getKlFollowStatus() == 1 && noteTxn.getTransactionExceedingBudget() == 0) {
                        txnSpecialStatus = "Rules Not Followed";
                    }
                }
                if (noteTxn.getDocRuleStatus() != null && noteTxn.getTransactionExceedingBudget() == null) {
                    txnSpecialStatus = "Rules Not Followed";
                }
                String[] actbudgetForBranchExpenseItemArr = { "", "" }; // now budget is stored at Transaction_item
                                                                        // level
                String[] budgetForBranchExpenseItemArr = { "", "" };
                String txnDocument = noteTxn.getSupportingDocs() == null ? "" : noteTxn.getSupportingDocs();
                
                String txnPurposeName = noteTxn.getTransactionPurpose().getTransactionPurpose();
                if(noteTxn.getTransactionPurpose().getId() == IdosConstants.CREDIT_NOTE_VENDOR){
                    if (noteTxn.getTypeIdentifier() != null) {
                        if (noteTxn.getTypeIdentifier() == 1) {
                            txnPurposeName += " - Increase in Price";
                        } else {
                            txnPurposeName += " - Increase in Quantity";
                        }
                    }
                } else if (noteTxn.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_VENDOR) {
                    if (noteTxn.getTypeIdentifier() != null) {
                        if (noteTxn.getTypeIdentifier() == 1) {
                            txnPurposeName += " - Decrease in Price";
                        } else {
                            txnPurposeName += " - Decrease in Quantity";
                        }
                    }
                }
                TransactionViewResponse.addActionTxn(noteTxn.getId(), branchName, projectName, itemName, itemParentName,
                        actbudgetForBranchExpenseItemArr[0], actbudgetForBranchExpenseItemArr[1],
                        budgetForBranchExpenseItemArr[0], budgetForBranchExpenseItemArr[1], customerVendorName,
                        txnPurposeName,
                        IdosConstants.idosdf.format(noteTxn.getTransactionDate()), "", "", paymentMode,
                        noteTxn.getNoOfUnits(), noteTxn.getPricePerUnit(), noteTxn.getGrossAmount(),
                        noteTxn.getNetAmount(), txnResultDesc, "", noteTxn.getTransactionStatus(),
                        noteTxn.getCreatedBy().getEmail(), approverLabel, approverEmail, txnDocument, remarks,
                        debitCredit, approverEmails, additionalApprovarUsers, selectedAdditionalApproval,
                        txnSpecialStatus, noteTxn.getFrieghtCharges(), "", "", "",
                        noteTxn.getTransactionPurpose().getId(), "", "", 0, noteTxn.getTransactionRefNumber(), 0l, 0.0,
                        0, 0, result);
            }
            // Single User
            if (ConfigParams.getInstance().isDeploymentSingleUser(user)) {
                ArrayNode singleUserAccounting = result.putArray("singleUserAccounting");
                ObjectNode createSingleuserJson = SINGLE_USER_DAO.createSingleuserJson(noteTxn, json, user);
                singleUserAccounting.add(createSingleuserJson);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on Sell on Cash- submit for approval", ex.getMessage());
        }
        return noteTxn;
    }
}
