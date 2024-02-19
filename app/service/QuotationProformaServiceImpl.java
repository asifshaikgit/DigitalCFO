package service;

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
import play.libs.Json;
import play.mvc.WebSocket;
import pojo.TransactionViewResponse;
import akka.stream.javadsl.*;
import akka.actor.*;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.text.ParseException;
import java.util.*;
import akka.NotUsed;

/**
 * Created by Sunil Namdev on 05-12-2016.
 */
public class QuotationProformaServiceImpl implements QuotationProformaService {
    @Override
    public Transaction submitForApprovalQuotation(Users user, JsonNode json, final EntityManager entityManager,
            EntityTransaction entitytransaction, ObjectNode result) throws IDOSException {
        Long txnforbranch = json.findValue("txnforbranch").asLong();
        String txnforproject = json.findValue("txnforproject").asText();
        String txnForItemStr = json.findValue("txnforitem").toString();
        String txnPurpose = json.findValue("txnPurpose").asText();
        long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
        Long txnforcustomer = json.findValue("txnforcustomer").asLong();
        String txnPoReference = json.findValue("txnPoReference") == null ? ""
                : json.findValue("txnPoReference").asText();
        Double txnnetamount = json.findValue("txnnetamount").asDouble();
        String txnremarks = json.findValue("txnremarks").asText();
        String txnRemarksPrivate = json.findValue("txnRemarksPrivate").asText();
        String supportingdoc = json.findValue("supportingdoc").asText();
        Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l
                : json.findValue("txnEntityID").asLong();
        Transaction transaction = null;
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

        String txnRemarks = "";
        Branch txnBranch = null;
        String branchName = "";
        Vendor txncustomer = null;
        Project txnProject = null;
        String projectName = "";
        String itemName = "";
        String customerVendorName = "";

        try {
            JSONArray arrJSON = new JSONArray(txnForItemStr);

            if (txnEntityID > 0) {
                transaction = Transaction.findById(txnEntityID);
            } else {
                transaction = new Transaction();
            }
            if (txnforbranch != null && !txnforbranch.equals("")) {
                txnBranch = genericDAO.getById(Branch.class, txnforbranch, entityManager);
                branchName = txnBranch.getName();
            }
            if (txnforproject != null && !txnforproject.equals("")) {
                txnProject = genericDAO.getById(Project.class, IdosUtil.convertStringToLong(txnforproject),
                        entityManager);
                projectName = txnProject.getName();
            }
            if (txnforcustomer != null && !txnforcustomer.equals("")) {
                txncustomer = genericDAO.getById(Vendor.class, txnforcustomer, entityManager);
                customerVendorName = txncustomer.getName();
            }
            TransactionPurpose usertxnPurpose = TransactionPurpose.findById(txnPurposeVal);
            transaction.setTransactionPurpose(usertxnPurpose);
            transaction.setTransactionBranch(txnBranch);
            transaction.setTransactionBranchOrganization(txnBranch.getOrganization());
            transaction.setTransactionProject(txnProject);
            transaction.setTransactionVendorCustomer(txncustomer);
            // Enter data for first item in transaction table to be displayed in Transaction
            // list
            JSONObject firstRowItemData = new JSONObject(arrJSON.get(0).toString());
            Long itemIdRow0 = firstRowItemData.getLong("txnItems");
            Specifics txnSpecificItem = genericDAO.getById(Specifics.class, itemIdRow0, entityManager);
            Double txnPerUnitPriceRow0 = firstRowItemData.getDouble("txnPerUnitPrice");
            Double txnNoOfUniRow0t = firstRowItemData.getDouble("txnNoOfUnit");
            Double txnGrossRow0 = firstRowItemData.getDouble("txnGross");
            transaction.setTransactionSpecifics(txnSpecificItem);
            transaction.setTransactionParticulars(txnSpecificItem.getParticularsId());
            transaction.setNoOfUnits(txnNoOfUniRow0t);
            transaction.setPricePerUnit(txnPerUnitPriceRow0);
            transaction.setGrossAmount(txnGrossRow0);
            transaction.setPoReference(txnPoReference);
            transaction.setTransactionDate(txnDate);
            transaction.setNetAmount(txnnetamount);
            if (txnremarks != null && !txnremarks.equals("")) {
                if (transaction.getRemarks() != null) {
                    transaction.setRemarks(txnremarks);
                } else {
                    txnRemarks = user.getEmail() + "#" + txnremarks;
                    transaction.setRemarks(txnRemarks);
                }
                txnRemarks = transaction.getRemarks(); // fetch encoded value
            }
            if (txnRemarksPrivate != null && !txnRemarksPrivate.equals("")) {
                if (transaction.getRemarksPrivate() != null) {
                    txnRemarksPrivate = transaction.getRemarksPrivate() + "," + user.getEmail() + "#"
                            + txnRemarksPrivate;
                    transaction.setRemarksPrivate(txnRemarksPrivate);
                } else {
                    txnRemarksPrivate = user.getEmail() + "#" + txnRemarksPrivate;
                    transaction.setRemarksPrivate(txnRemarksPrivate);
                }
                txnRemarksPrivate = transaction.getRemarksPrivate(); // fetch encoded value
            }
            transaction.setSupportingDocs(transactionDao.getAndDeleteSupportingDocument(transaction.getSupportingDocs(),
                    user.getEmail(), supportingdoc, user, entityManager));
            transaction.setTransactionStatus("Require Approval");
            // list of additional users all approver role users of thet organization
            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("role.name", "APPROVER");
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            List<UsersRoles> approverRole = genericDAO.findByCriteria(UsersRoles.class, criterias, entityManager);

            String approverEmails = "";
            String additionalApprovarUsers = "";
            String selectedAdditionalApproval = "";
            /*
             * for (UsersRoles usrRoles : approverRole) {
             * additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
             * criterias.clear();
             * criterias.put("user.id", usrRoles.getUser().getId());
             * criterias.put("userRights.id", 2L);
             * criterias.put("branch.id", txnBranch.getId());
             * UserRightInBranch userHasRightInBranch =
             * genericDAO.getByCriteria(UserRightInBranch.class, criterias, entityManager);
             * if (userHasRightInBranch != null) {
             * //check for right in chart of accounts
             * criterias.clear();
             * criterias.put("user.id", usrRoles.getUser().getId());
             * criterias.put("userRights.id", 2L);
             * criterias.put("specifics.id", txnSpecificItem.getId());
             * UserRightSpecifics userHasRightInCOA =
             * genericDAO.getByCriteria(UserRightSpecifics.class, criterias, entityManager);
             * if (userHasRightInCOA != null) {
             * approverEmails += usrRoles.getUser().getEmail() + ",";
             * }
             * }
             * }
             */
            Boolean approver = null;
            for (UsersRoles usrRoles : approverRole) {
                approver = false;
                additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
                criterias.clear();
                criterias.put("user.id", usrRoles.getUser().getId());
                criterias.put("userRights.id", 2L);
                criterias.put("branch.id", txnBranch.getId());
                criterias.put("presentStatus", 1);

                UserRightInBranch userHasRightInBranch = genericDAO.getByCriteria(UserRightInBranch.class, criterias,
                        entityManager);
                if (userHasRightInBranch != null) {
                    /*
                     * //check for right in chart of accounts
                     * criterias.clear();
                     * criterias.put("user.id", usrRoles.getUser().getId());
                     * criterias.put("userRights.id", 2L);
                     * criterias.put("specifics.id", txnSpecificItem.getId());
                     * UserRightSpecifics userHasRightInCOA =
                     * genericDAO.getByCriteria(UserRightSpecifics.class, criterias, entityManager);
                     * if (userHasRightInCOA != null) {
                     * approverEmails += usrRoles.getUser().getEmail() + ",";
                     * }
                     */
                    for (int i = 0; i < arrJSON.length(); i++) {
                        // Double howMuchAdvance=0.0;Double txnTaxAmount=0.0;Double
                        // customerAdvance=0.0;String txnTaxDesc="";Double withholdingAmount=0.0;
                        JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                        // TransactionItems transactionItem = new TransactionItems();
                        Long itemId = rowItemData.getLong("txnItems");
                        Specifics txnItem = genericDAO.getById(Specifics.class, itemId, entityManager);
                        criterias.clear();
                        criterias.put("user.id", usrRoles.getUser().getId());
                        criterias.put("userRights.id", 2L);
                        criterias.put("specifics.id", txnItem.getId());
                        criterias.put("presentStatus", 1);
                        UserRightSpecifics userHasRightInCOA = genericDAO.getByCriteria(UserRightSpecifics.class,
                                criterias, entityManager);
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

            // list of approver user
            String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
            transaction.setTransactionRefNumber(transactionNumber);
            genericDAO.saveOrUpdate(transaction, user, entityManager);
            FILE_UPLOAD_DAO.updateUploadFileLogs(entityManager, user, supportingdoc, transaction.getId(),
                    IdosConstants.MAIN_TXN_TYPE);
            // Enter multiple items data into TransactionItems table
            if (txnEntityID > 0) {
                transactionItemsService.updateMultipleItemsTransactionItems(entityManager, user, arrJSON, transaction);
            } else {
                transactionItemsService.insertMultipleItemsTransactionItems(entityManager, user, arrJSON, transaction,
                        txnDate);
            }
            entitytransaction.commit();
            if (!ConfigParams.getInstance().isDeploymentSingleUser(user)) {
                // Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
                // Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
                // String sbquery = "select obj from Users obj WHERE obj.email = ?1 and
                // obj.presentStatus=1";
                // for (int i = 0; i < keyArray.length; i++) {
                // Query query = entityManager.createQuery(sbquery);
                // query.setParameter(1, keyArray[i]);
                // List<Users> orgusers = genericDAO.executeQuery(query, entityManager);
                // if (!orgusers.isEmpty()
                // && orgusers.get(0).getOrganization().getId() ==
                // user.getOrganization().getId()) {
                // orgtxnregistereduser.put(keyArray[i].toString(),
                // CreatorActor.expenseregistrered.get(keyArray[i]));
                // }
                // }
                String itemParentName = "";
                if (txnSpecificItem.getParentSpecifics() != null && !txnSpecificItem.getParentSpecifics().equals("")) {
                    itemParentName = txnSpecificItem.getParentSpecifics().getName();
                } else {
                    itemParentName = txnSpecificItem.getParticularsId().getName();
                }

                String approverEmail = "";
                String approverLabel = "";
                if (transaction.getApproverActionBy() != null) {
                    approverLabel = "APPROVER:";
                    approverEmail = transaction.getApproverActionBy().getEmail();
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
                String txnResultDesc = "";
                if (transaction.getNetAmountResultDescription() != null
                        && !transaction.getNetAmountResultDescription().equals("null")) {
                    txnResultDesc = transaction.getNetAmountResultDescription();
                }
                if (transaction.getDocRuleStatus() != null && transaction.getTransactionExceedingBudget() != null) {
                    if (transaction.getDocRuleStatus() == 1 && transaction.getTransactionExceedingBudget() == 1) {
                        txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
                    }
                    if (transaction.getKlFollowStatus() == 1 && transaction.getTransactionExceedingBudget() == 0) {
                        txnSpecialStatus = "Rules Not Followed";
                    }
                }
                if (transaction.getDocRuleStatus() != null && transaction.getTransactionExceedingBudget() == null) {
                    txnSpecialStatus = "Rules Not Followed";
                }
                Integer typeOfSupply = transaction.getTypeOfSupply() == null ? 0 : transaction.getTypeOfSupply();
                String txnDocument = transaction.getSupportingDocs() == null ? "" : transaction.getSupportingDocs();
                TransactionViewResponse.addActionTxn(transaction.getId(), branchName, projectName, itemName,
                        itemParentName,
                        "",
                        "", "", "", customerVendorName, transaction.getTransactionPurpose().getTransactionPurpose(),
                        IdosConstants.idosdf.format(transaction.getTransactionDate()), "", "", "",
                        transaction.getNoOfUnits(), transaction.getPricePerUnit(), transaction.getGrossAmount(),
                        transaction.getNetAmount(), txnResultDesc, "", transaction.getTransactionStatus(),
                        transaction.getCreatedBy().getEmail(), approverLabel, approverEmail, txnDocument, txnRemarks,
                        "", approverEmails, additionalApprovarUsers, selectedAdditionalApproval,
                        txnSpecialStatus, 0d, txnPoReference, "", "", transaction.getTransactionPurpose().getId(),
                        txnRemarksPrivate, "", 0, transaction.getTransactionRefNumber(), 0l, 0.0, 0, typeOfSupply,
                        result);
            }

        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on Quotation- submit for approval", ex.getMessage());
        }
        return transaction;
    }

    @Override
    public Transaction submitForApprovalProforma(Users user, JsonNode json, EntityManager entityManager,
            EntityTransaction entitytransaction, ObjectNode result) throws IDOSException {
        Long txnforbranch = json.findValue("txnforbranch").asLong();
        String txnforproject = json.findValue("txnforproject").asText();
        String txnForItemStr = json.findValue("txnforitem").toString();
        String txnPurpose = json.findValue("txnPurpose").asText();
        long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
        Long txnforcustomer = json.findValue("txnforcustomer").asLong();
        // String txnNoOfUnits = json.findValue("txnnoofunits").asText();
        // String txnpriceperunit = json.findValue("txnpriceperunit").asText();
        // String txngross = json.findValue("txngross").asText();
        String txnPoReference = json.findValue("txnPoReference") == null ? ""
                : json.findValue("txnPoReference").asText();
        Double txnnetamount = json.findValue("txnnetamount").asDouble();
        String txnremarks = json.findValue("txnremarks").asText();
        String txnRemarksPrivate = json.findValue("txnRemarksPrivate").asText();
        String supportingdoc = json.findValue("supportingdoc").asText();
        Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l
                : json.findValue("txnEntityID").asLong();
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
        String txnRemarks = "";
        Branch txnBranch = null;
        String branchName = "";
        Vendor txncustomer = null;
        Project txnProject = null;
        String projectName = "";
        String itemName = "";
        String customerVendorName = "";
        Transaction transaction = null;
        try {
            JSONArray arrJSON = new JSONArray(txnForItemStr);

            if (txnEntityID > 0) {
                transaction = Transaction.findById(txnEntityID);
            } else {
                transaction = new Transaction();
            }
            if (txnforbranch != null && !txnforbranch.equals("")) {
                txnBranch = genericDAO.getById(Branch.class, txnforbranch, entityManager);
                branchName = txnBranch.getName();
            }
            if (txnforproject != null && !txnforproject.equals("")) {
                txnProject = genericDAO.getById(Project.class, IdosUtil.convertStringToLong(txnforproject),
                        entityManager);
                projectName = txnProject.getName();
            }
            if (txnforcustomer != null && !txnforcustomer.equals("")) {
                txncustomer = genericDAO.getById(Vendor.class, txnforcustomer, entityManager);
                customerVendorName = txncustomer.getName();
            }
            TransactionPurpose usertxnPurpose = TransactionPurpose.findById(txnPurposeVal);
            transaction.setTransactionPurpose(usertxnPurpose);
            transaction.setTransactionBranch(txnBranch);
            transaction.setTransactionBranchOrganization(txnBranch.getOrganization());
            transaction.setTransactionProject(txnProject);
            transaction.setTransactionVendorCustomer(txncustomer);
            // Enter data for first item in transaction table to be displayed in Transaction
            // list
            JSONObject firstRowItemData = new JSONObject(arrJSON.get(0).toString());
            Long itemIdRow0 = firstRowItemData.getLong("txnItems");
            Specifics txnSpecificItem = genericDAO.getById(Specifics.class, itemIdRow0, entityManager);
            Double txnPerUnitPriceRow0 = firstRowItemData.getDouble("txnPerUnitPrice");
            Double txnNoOfUniRow0t = firstRowItemData.getDouble("txnNoOfUnit");
            Double txnGrossRow0 = firstRowItemData.getDouble("txnGross");
            transaction.setTransactionSpecifics(txnSpecificItem);
            transaction.setTransactionParticulars(txnSpecificItem.getParticularsId());
            transaction.setNoOfUnits(txnNoOfUniRow0t);
            transaction.setPricePerUnit(txnPerUnitPriceRow0);
            transaction.setGrossAmount(txnGrossRow0);
            transaction.setPoReference(txnPoReference);
            transaction.setTransactionDate(txnDate);
            transaction.setNetAmount(txnnetamount);
            if (txnremarks != null && !txnremarks.equals("")) {
                if (transaction.getRemarks() != null) {
                    transaction.setRemarks(txnRemarks);
                } else {
                    txnRemarks = user.getEmail() + "#" + txnremarks;
                    transaction.setRemarks(txnRemarks);
                }
                txnRemarks = transaction.getRemarks(); // fetch encoded value
            }
            if (txnRemarksPrivate != null && !txnRemarksPrivate.equals("")) {
                if (transaction.getRemarksPrivate() != null) {
                    txnRemarksPrivate = transaction.getRemarksPrivate() + "," + user.getEmail() + "#"
                            + txnRemarksPrivate;
                    transaction.setRemarksPrivate(txnRemarksPrivate);
                } else {
                    txnRemarksPrivate = user.getEmail() + "#" + txnRemarksPrivate;
                    transaction.setRemarksPrivate(txnRemarksPrivate);
                }
                txnRemarksPrivate = transaction.getRemarksPrivate(); // fetch encoded value
            }
            transaction.setSupportingDocs(transactionDao.getAndDeleteSupportingDocument(transaction.getSupportingDocs(),
                    user.getEmail(), supportingdoc, user, entityManager));
            transaction.setTransactionStatus("Require Approval");
            // list of additional users all approver role users of thet organization
            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("role.name", "APPROVER");
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            List<UsersRoles> approverRole = genericDAO.findByCriteria(UsersRoles.class, criterias, entityManager);

            String approverEmails = "";
            String additionalApprovarUsers = "";
            String selectedAdditionalApproval = "";
            /*
             * for (UsersRoles usrRoles : approverRole) {
             * additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
             * criterias.clear();
             * criterias.put("user.id", usrRoles.getUser().getId());
             * criterias.put("userRights.id", 2L);
             * criterias.put("branch.id", txnBranch.getId());
             * UserRightInBranch userHasRightInBranch =
             * genericDAO.getByCriteria(UserRightInBranch.class, criterias, entityManager);
             * if (userHasRightInBranch != null) {
             * //check for right in chart of accounts
             * criterias.clear();
             * criterias.put("user.id", usrRoles.getUser().getId());
             * criterias.put("userRights.id", 2L);
             * criterias.put("specifics.id", txnSpecificItem.getId());
             * UserRightSpecifics userHasRightInCOA =
             * genericDAO.getByCriteria(UserRightSpecifics.class, criterias, entityManager);
             * if (userHasRightInCOA != null) {
             * approverEmails += usrRoles.getUser().getEmail() + ",";
             * }
             * }
             * }
             */
            Boolean approver = null;
            for (UsersRoles usrRoles : approverRole) {
                approver = false;
                additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
                criterias.clear();
                criterias.put("user.id", usrRoles.getUser().getId());
                criterias.put("userRights.id", 2L);
                criterias.put("branch.id", txnBranch.getId());
                criterias.put("presentStatus", 1);

                UserRightInBranch userHasRightInBranch = genericDAO.getByCriteria(UserRightInBranch.class, criterias,
                        entityManager);
                if (userHasRightInBranch != null) {
                    /*
                     * //check for right in chart of accounts
                     * criterias.clear();
                     * criterias.put("user.id", usrRoles.getUser().getId());
                     * criterias.put("userRights.id", 2L);
                     * criterias.put("specifics.id", txnSpecificItem.getId());
                     * UserRightSpecifics userHasRightInCOA =
                     * genericDAO.getByCriteria(UserRightSpecifics.class, criterias, entityManager);
                     * if (userHasRightInCOA != null) {
                     * approverEmails += usrRoles.getUser().getEmail() + ",";
                     * }
                     */
                    for (int i = 0; i < arrJSON.length(); i++) {
                        // Double howMuchAdvance=0.0;Double txnTaxAmount=0.0;Double
                        // customerAdvance=0.0;String txnTaxDesc="";Double withholdingAmount=0.0;
                        JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                        // TransactionItems transactionItem = new TransactionItems();
                        Long itemId = rowItemData.getLong("txnItems");
                        Specifics txnItem = genericDAO.getById(Specifics.class, itemId, entityManager);
                        criterias.clear();
                        criterias.put("user.id", usrRoles.getUser().getId());
                        criterias.put("userRights.id", 2L);
                        criterias.put("specifics.id", txnItem.getId());
                        criterias.put("presentStatus", 1);
                        UserRightSpecifics userHasRightInCOA = genericDAO.getByCriteria(UserRightSpecifics.class,
                                criterias, entityManager);
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

            // list of approver user
            String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
            transaction.setTransactionRefNumber(transactionNumber);
            genericDAO.saveOrUpdate(transaction, user, entityManager);
            FILE_UPLOAD_DAO.updateUploadFileLogs(entityManager, user, supportingdoc, transaction.getId(),
                    IdosConstants.MAIN_TXN_TYPE);
            // Enter multiple items data into TransactionItems table
            if (txnEntityID > 0) {
                transactionItemsService.updateMultipleItemsTransactionItems(entityManager, user, arrJSON, transaction);
            } else {
                transactionItemsService.insertMultipleItemsTransactionItems(entityManager, user, arrJSON, transaction,
                        txnDate);
            }
            entitytransaction.commit();
            if (!ConfigParams.getInstance().isDeploymentSingleUser(user)) {
                // Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
                // Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
                // String sbquery = "select obj from Users obj WHERE obj.email = ?1 and
                // obj.presentStatus=1";
                // for (int i = 0; i < keyArray.length; i++) {
                // Query query = entityManager.createQuery(sbquery);
                // query.setParameter(1, keyArray[i]);
                // List<Users> orgusers = genericDAO.executeQuery(query, entityManager);
                // if (!orgusers.isEmpty()
                // && orgusers.get(0).getOrganization().getId() ==
                // user.getOrganization().getId()) {
                // orgtxnregistereduser.put(keyArray[i].toString(),
                // CreatorActor.expenseregistrered.get(keyArray[i]));
                // }
                // }
                String itemParentName = "";
                if (txnSpecificItem.getParentSpecifics() != null && !txnSpecificItem.getParentSpecifics().equals("")) {
                    itemParentName = txnSpecificItem.getParentSpecifics().getName();
                } else {
                    itemParentName = txnSpecificItem.getParticularsId().getName();
                }

                String approverEmail = "";
                String approverLabel = "";
                if (transaction.getApproverActionBy() != null) {
                    approverLabel = "APPROVER:";
                    approverEmail = transaction.getApproverActionBy().getEmail();
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
                String txnResultDesc = "";
                if (transaction.getNetAmountResultDescription() != null
                        && !transaction.getNetAmountResultDescription().equals("null")) {
                    txnResultDesc = transaction.getNetAmountResultDescription();
                }
                if (transaction.getDocRuleStatus() != null && transaction.getTransactionExceedingBudget() != null) {
                    if (transaction.getDocRuleStatus() == 1 && transaction.getTransactionExceedingBudget() == 1) {
                        txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
                    }
                    if (transaction.getKlFollowStatus() == 1 && transaction.getTransactionExceedingBudget() == 0) {
                        txnSpecialStatus = "Rules Not Followed";
                    }
                }
                if (transaction.getDocRuleStatus() != null && transaction.getTransactionExceedingBudget() == null) {
                    txnSpecialStatus = "Rules Not Followed";
                }
                Integer typeOfSupply = transaction.getTypeOfSupply() == null ? 0 : transaction.getTypeOfSupply();
                String txnDocument = transaction.getSupportingDocs() == null ? "" : transaction.getSupportingDocs();
                TransactionViewResponse.addActionTxn(transaction.getId(), branchName, projectName, itemName,
                        itemParentName,
                        "",
                        "", "", "", customerVendorName, transaction.getTransactionPurpose().getTransactionPurpose(),
                        IdosConstants.idosdf.format(transaction.getTransactionDate()), "", "", "",
                        transaction.getNoOfUnits(), transaction.getPricePerUnit(), transaction.getGrossAmount(),
                        transaction.getNetAmount(), txnResultDesc, "", transaction.getTransactionStatus(),
                        transaction.getCreatedBy().getEmail(), approverLabel, approverEmail, txnDocument, txnRemarks,
                        "", approverEmails, additionalApprovarUsers, selectedAdditionalApproval,
                        txnSpecialStatus, 0d, txnPoReference, "", "", transaction.getTransactionPurpose().getId(),
                        txnRemarksPrivate, "", 0, transaction.getTransactionRefNumber(), 0l, 0.0, 0, typeOfSupply,
                        result);
            }

        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on Quotation- submit for approval", ex.getMessage());
        }
        return transaction;
    }

    @Override
    public boolean getQuotationProformaProjectBy(Branch branch, Users user, EntityManager entityManager,
            long transactionPurposeID, Date fromDate, Date toDate, ObjectNode result) {
        ArrayNode arrayNode = result.putArray("quotationProformaData");
        String totalNetHql = "select sum(obj.netAmount) from Transaction obj WHERE obj.transactionBranch.id = ?1 and obj.transactionBranchOrganization.id = ?2 AND obj.transactionPurpose.id = ?3 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 and obj.transactionDate  between ?4 and ?5 GROUP BY obj.transactionBranch.id";
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "total HQL: " + totalNetHql);
            log.log(Level.FINE, "transactionPurposeID: " + transactionPurposeID + " branch ID: " + branch.getId()
                    + " fromDate: " + fromDate + " toDate: " + toDate);
        }
        Query query = entityManager.createQuery(totalNetHql);
        query.setParameter(1, branch.getId());
        query.setParameter(2, user.getOrganization().getId());
        query.setParameter(3, transactionPurposeID);
        query.setParameter(4, fromDate);
        query.setParameter(5, toDate);
        List<Object> totalList = query.getResultList();
        Double totalAmount = 0.0;
        if (totalList != null && !totalList.isEmpty()) {
            Object val = totalList.get(0);
            totalAmount = (Double) val;
        }
        String groupPrjctHql = "select TRANSACTION_BRANCH, CREATED_BY, TRANSACTION_PROJECT, sum(NET_AMOUNT) from TRANSACTION where TRANSACTION_BRANCH = ?1 and TRANSACTION_BRANCH_ORGANIZATION = ?2 and TRANSACTION_PURPOSE = ?3 and TRANSACTION_STATUS = 'Accounted' and PRESENT_STATUS = 1 and (TRANSACTION_ACTIONDATE  between ?4 and ?5) group by TRANSACTION_PROJECT, CREATED_BY order by NET_AMOUNT desc";
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "main HQL: " + groupPrjctHql);
        }
        query = entityManager.createQuery(groupPrjctHql);
        query.setParameter(1, branch.getId());
        query.setParameter(2, user.getOrganization().getId());
        query.setParameter(3, transactionPurposeID);
        query.setParameter(4, fromDate);
        query.setParameter(5, toDate);

        List<Object[]> branchWiseDataList = query.getResultList();
        for (Object[] maintxn : branchWiseDataList) {
            Double amount = 0.0;
            ObjectNode row = Json.newObject();
            row.put("branchID", String.valueOf(maintxn[0]));
            String userid = String.valueOf(maintxn[1]);
            Users userTxn = Users.findById(Long.parseLong(userid));
            row.put("userName", userTxn.getFullName());
            String projectID = maintxn[2] == null ? "0" : String.valueOf(maintxn[2]);
            Project projectTxn = Project.findById(Long.parseLong(projectID));
            if (projectTxn != null && projectTxn.getName() != null) {
                row.put("projectName", projectTxn.getName());
            } else {
                row.put("projectName", "");
            }
            // row.put("branchName", branch.getName());
            // row.put("customerName", maintxn.getTransactionVendorCustomer().getName());
            amount = (Double) maintxn[3];
            row.put("netAmount", amount);
            Double percentAmount = ((amount * 100) / totalAmount);
            row.put("percentAmount", percentAmount);
            if (transactionPurposeID == 27) {
                row.put("txnModelFor", "quotationInvoice");
            } else {
                row.put("txnModelFor", "proformaInvoice");
            }
            row.put("userID", userid);
            row.put("projectID", projectID);
            arrayNode.add(row);

        }
        return true;
    }

    @Override
    public boolean getQuotationProformaItems(EntityManager entityManager, Users user, JsonNode json, Date startDate,
            Date endDate, long transactionPurposeID, ObjectNode result) throws IDOSException {
        log.log(Level.FINE, "======= Start");
        ArrayNode arrayNode = result.putArray("quotationProformaData");
        try {
            long branchID = json.findValue("branchID").asLong();
            long userID = json.findValue("userID").asLong();
            String projectID = json.findValue("projectID") == null ? "" : json.findValue("projectID").asText();

            StringBuilder groupPrjctHql = new StringBuilder(
                    "select a.transactionSpecifics.id, a.transactionSpecifics.name, sum(a.netAmount) from TransactionItems a where a.transaction in (select b from Transaction b where b.transactionPurpose=")
                    .append(transactionPurposeID)
                    .append(" and b.transactionBranchOrganization=").append(user.getOrganization().getId())
                    .append(" and b.transactionBranch=").append(branchID).append(" and b.createdBy=").append(userID)
                    .append(" and b.transactionStatus='Accounted' and b.presentStatus=1 ");
            if ("".equals(projectID) || "0".equals(projectID)) {
                groupPrjctHql.append(" and b.transactionProject is null");
            } else {
                groupPrjctHql.append(" and b.transactionProject=").append(projectID);
            }
            groupPrjctHql.append(" and (TRANSACTION_ACTIONDATE  between ? and ?) ) group by a.transactionSpecifics");
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.INFO, "HQL: " + groupPrjctHql);
            }
            Query query = entityManager.createQuery(groupPrjctHql.toString());
            query.setParameter(1, startDate);
            query.setParameter(2, endDate);
            List<Object[]> itemsNetList = query.getResultList();
            for (Object[] itemData : itemsNetList) {
                ObjectNode row = Json.newObject();
                row.put("specificid", String.valueOf(itemData[0]));
                row.put("specificname", String.valueOf(itemData[1]));
                row.put("netamounttotal", String.valueOf(itemData[2]));
                arrayNode.add(row);
            }

        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on fetching transaction items", ex.getMessage());
        }
        log.log(Level.FINE, "====== End " + result);
        return true;
    }

    @Override
    public boolean getTransactionsForItem(EntityManager entityManager, Users user, JsonNode json, Date startDate,
            Date endDate, long transactionPurposeID, ObjectNode result) throws IDOSException {
        log.log(Level.FINE, "======= Start");
        ArrayNode arrayNode = result.putArray("transactionData");
        try {
            long branchID = json.findValue("branchID").asLong();
            long userID = json.findValue("userID").asLong();
            long specificID = json.findValue("specificid").asLong();
            String projectID = json.findValue("projectID") == null ? "" : json.findValue("projectID").asText();
            StringBuilder tranHql = new StringBuilder(
                    "select a from TransactionItems a where a.transaction in (select b from Transaction b where b.transactionPurpose=")
                    .append(transactionPurposeID)
                    .append(" and b.transactionBranchOrganization=").append(user.getOrganization().getId())
                    .append(" and b.transactionBranch=").append(branchID).append(" and b.createdBy=").append(userID)
                    .append(" and b.transactionStatus='Accounted' and b.presentStatus=1 ");
            if ("".equals(projectID) || "0".equals(projectID)) {
                tranHql.append(" and b.transactionProject is null");
            } else {
                tranHql.append(" and b.transactionProject=").append(projectID);
            }
            tranHql.append(" ) ").append(" and a.transactionSpecifics = ").append(specificID);
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.INFO, "HQL: " + tranHql);
            }
            Query query = entityManager.createQuery(tranHql.toString());
            List<TransactionItems> itemsNetList = query.getResultList();
            for (TransactionItems itemData : itemsNetList) {
                ObjectNode row = Json.newObject();
                row.put("transid", itemData.getTransactionId().getTransactionRefNumber());
                row.put("netamounttotal", itemData.getNetAmount());
                arrayNode.add(row);
            }

        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on fetching transaction for items", ex.getMessage());
        }
        log.log(Level.FINE, "====== End " + result);
        return true;
    }
}
