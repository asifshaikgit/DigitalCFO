package com.idos.dao;

import com.idos.util.*;
import model.*;
import model.payroll.PayrollSetup;
import model.payroll.TrialBalancePayrollItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import play.db.jpa.JPAApi;
import play.libs.Json;
import pojo.ProvisionJournalEntryDetailPojo;
import service.BranchBankService;
import service.BranchBankServiceImpl;
import service.BranchCashService;
import service.BranchCashServiceImpl;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.*;
import javax.inject.Inject;

/**
 * Created by Sunil Namdev on 08.07.16.
 */
public class ProvisionJournalEntryDAOImpl implements ProvisionJournalEntryDAO {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    @Override
    public void saveProvisionJournalEntryBRSDate(Users user, EntityManager em, EntityTransaction entitytransaction,
            String transactionRef, String brsBankDate) throws Exception {
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("transactionRefNumber", transactionRef);
        criterias.put("presentStatus", 1);
        IdosProvisionJournalEntry transaction = genericDao.getByCriteria(IdosProvisionJournalEntry.class, criterias,
                entityManager);
        if (null != transaction && null != transaction.getId()) {
            transaction.setBrsBankDate(brsBankDate);
            genericDao.saveOrUpdate(transaction, user, entityManager);
        }
    }

    @Override
    public ObjectNode provisionJournalEntry(ObjectNode result, JsonNode json, Users user, EntityManager em,
            EntityTransaction entitytransaction, IdosProvisionJournalEntry newProvJournalEntry) throws Exception {
        // log.log(Level.FINE, log.isLoggable(Level.FINE));
        log.log(Level.FINE, "************* Start " + json);
        String txnPurpose = json.findValue("txnPurpose").asText();
        String txnPurposeVal = json.findValue("txnPurposeVal").asText();
        TransactionPurpose usertxnPurpose = TransactionPurpose.findById(Long.valueOf(txnPurposeVal));
        String txnDebitBranchId = json.findValue("txnDebitBranchId") != null
                ? json.findValue("txnDebitBranchId").asText()
                : null;
        String txnCreditBranchId = json.findValue("txnCreditBranchId") != null
                ? json.findValue("txnCreditBranchId").asText()
                : null;
        String txnTotalDebitAmount = json.findValue("txnTotalDebitAmount") != null
                ? json.findValue("txnTotalDebitAmount").asText()
                : null;
        String txnTotalCreditAmount = json.findValue("txnTotalCreditAmount") != null
                ? json.findValue("txnTotalCreditAmount").asText()
                : null;
        String txnpurpose = json.findValue("txnpurpose") != null ? json.findValue("txnpurpose").asText() : null;
        String txnmtefpeRemarks = json.findValue("txnmtefpeRemarks") != null
                ? json.findValue("txnmtefpeRemarks").asText()
                : null;
        String supportingdoc = json.findValue("supportingdoc") != null ? json.findValue("supportingdoc").asText()
                : null;
        String txnalertForReversalRequired = json.findValue("txnalertForReversalRequired") != null
                ? json.findValue("txnalertForReversalRequired").asText()
                : null;
        String txnalertForReversalRequiredDateOfReversal = json
                .findValue("txnalertForReversalRequiredDateOfReversal") != null
                        ? json.findValue("txnalertForReversalRequiredDateOfReversal").asText()
                        : null;
        String txnInstrumentNumber = json.findValue("txnInstrumentNum") != null
                ? json.findValue("txnInstrumentNum").asText()
                : null;
        String txnInstrumentDate = json.findValue("txnInstrumentDate") != null
                ? json.findValue("txnInstrumentDate").asText()
                : null;
        Double txnDebitRoundOff = json.findValue("txnDebitRoundOff") != null
                ? json.findValue("txnDebitRoundOff").asDouble()
                : null;
        Double txnCreditRoundOff = json.findValue("txnCreditRoundOff") != null
                ? json.findValue("txnCreditRoundOff").asDouble()
                : null;
        Specifics roundOfSpecific = null;
        if (txnDebitRoundOff != null || txnCreditRoundOff != null) {
            roundOfSpecific = coaDAO.getSpecificsForMapping(user, "51", em);
            if (roundOfSpecific == null) {
                throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                        "COA mapping is not found for mapping Id: 51",
                        "COA mapping is not found for, mapping: rounding off amounts on Incomes.");
            }
        }
        String txnRemarks = "";
        String txnDocument = "";
        Specifics debitspecf = null;
        Vendor debitcustomer = null;
        Vendor debitvendor = null;
        BranchCashCount debitbranchCashCount = null;
        BranchBankAccounts debitbranchBankAccounts = null;
        Specifics creditspecf = null;
        Vendor creditcustomer = null;
        Vendor creditvendor = null;
        Users creditUser = null;
        Users debitUser = null;
        BranchCashCount creditbranchCashCount = null;
        BranchBankAccounts creditbranchBankAccounts = null;

        newProvJournalEntry.setTransactionPurpose(usertxnPurpose);
        // Sunil: if date is not set then transaction date will be todate.
        // transaction date setup is only applicable for "provision Journal Entry"
        String transactionDate = json.findPath("transactionDate") != null ? json.findPath("transactionDate").asText()
                : null;

        Date transactionDt = null;
        if (transactionDate != null && !"".equals(transactionDate)) {
            /*
             * Sunil: This will not work and always create backdated transactions even for
             * todate.
             * String timeformat = IdosConstants.TIMEFMT.format(new Date());
             * transactionDate += " "+timeformat;
             * transactionDt=IdosConstants.MYSQLDTF.parse(IdosConstants.MYSQLDTF.format(
             * IdosConstants.IDOSDF.parse(transactionDate)));
             * if(transactionDt.compareTo(Calendar.getInstance().getTime()) < 0){
             * newProvJournalEntry.setIsBackdatedTransaction(1);
             * }
             */
            transactionDt = IdosConstants.IDOSDF.parse(transactionDate);
            if (DateUtil.isBackDate(transactionDt)) {
                newProvJournalEntry.setIsBackdatedTransaction(1);
            }
        } else {
            transactionDt = Calendar.getInstance().getTime();
        }
        newProvJournalEntry.setTransactionDate(transactionDt);
        Date finStartDate = user.getOrganization().getFinancialStartDate();
        Date finEndDate = user.getOrganization().getFinancialEndDate();
        int isValid = DateUtil.isDateInFinancialYearRange(finStartDate, finEndDate, transactionDt);
        result.put("validTransactionDate", isValid);
        if (isValid == 0) { // transaction cannot be created as transaction date is out of financial year
            log.log(Level.SEVERE,
                    "******** End ** transaction cannot be created as transaction date is out of financial year "
                            + result);
            return result;
        }

        Branch provJournalTxnBranch = null;
        if (txnDebitBranchId != null && !txnDebitBranchId.equals("")) {
            provJournalTxnBranch = Branch.findById(IdosUtil.convertStringToLong(txnDebitBranchId));
            newProvJournalEntry.setDebitBranch(provJournalTxnBranch);
            newProvJournalEntry.setProvisionMadeForOrganization(user.getOrganization());
        }
        if (txnCreditBranchId != null && !txnCreditBranchId.equals("")) {
            provJournalTxnBranch = Branch.findById(IdosUtil.convertStringToLong(txnCreditBranchId));
            newProvJournalEntry.setCreditBranch(provJournalTxnBranch);
        }

        if (txnTotalDebitAmount != null && !txnTotalDebitAmount.equals("")) {
            newProvJournalEntry.setTotalDebitAmount(IdosUtil.convertStringToDouble(txnTotalDebitAmount));
        }

        if (txnTotalCreditAmount != null && !txnTotalCreditAmount.equals("")) {
            newProvJournalEntry.setTotalCreditAmount(IdosUtil.convertStringToDouble(txnTotalCreditAmount));
        }

        if (!txnmtefpeRemarks.equals("") && txnmtefpeRemarks != null) {
            txnRemarks = user.getEmail() + "#" + txnmtefpeRemarks;
            newProvJournalEntry.setTxnRemarks(txnRemarks);
        }

        newProvJournalEntry.setSupportingDocuments(TRANSACTION_DAO.getAndDeleteSupportingDocument(
                newProvJournalEntry.getSupportingDocuments(), user.getEmail(), supportingdoc, user, em));
        if (txnalertForReversalRequired != null && !txnalertForReversalRequired.equals("")) {
            Integer alertForReversal = IdosUtil.convertStringToInt(txnalertForReversalRequired);
            newProvJournalEntry.setAllowedReversal(alertForReversal);
            if (txnalertForReversalRequiredDateOfReversal != null
                    && !txnalertForReversalRequiredDateOfReversal.equals("")) {
                Date rDate = IdosConstants.mysqldf.parse(IdosConstants.mysqldf
                        .format(IdosConstants.idosdf.parse(txnalertForReversalRequiredDateOfReversal)));
                newProvJournalEntry.setReversalDate(rDate);
            }
        }
        newProvJournalEntry.setReversalStatus("NOT-REVERSED");
        newProvJournalEntry.setTransactionStatus("Require Approval");
        List<String> emailSet = UserRolesUtil.approverAdditionalApprovalBasedOnSelectedBranch(user,
                provJournalTxnBranch, em);
        Object[] array = emailSet.toArray();
        String approverEmails = "";
        String additionalApprovarUsers = "";
        String selectedAdditionalApproval = "";
        for (int i = 0; i < array.length; i++) {
            if (i == 0) {
                approverEmails = array[i].toString();
            }
            if (i == 1) {
                additionalApprovarUsers = array[i].toString();
            }
            if (i == 2) {
                selectedAdditionalApproval = array[i].toString();
            }
        }
        newProvJournalEntry.setInstrumentNumber(txnInstrumentNumber);
        newProvJournalEntry.setInstrumentDate(txnInstrumentDate);
        newProvJournalEntry.setApproverEmails(approverEmails);
        newProvJournalEntry.setAdditionalApproverUserEmails(additionalApprovarUsers);
        String transactionNumber = CodeHelper.getForeverUniqueID("PROVTXN", null);
        newProvJournalEntry.setTransactionRefNumber(transactionNumber);
        if (txnpurpose != null && !txnpurpose.equals("")) {
            newProvJournalEntry.setPurpose(txnpurpose);
        }
        genericDao.saveOrUpdate(newProvJournalEntry, user, em);
        FILE_UPLOAD_DAO.updateUploadFileLogs(em, user, supportingdoc, newProvJournalEntry.getId(),
                IdosConstants.PJE_TXN_TYPE);
        // Adding debit and credit Detail
        List<ProvisionJournalEntryDetail> pjEntryDetailDebitCreditList = new ArrayList<ProvisionJournalEntryDetail>();
        String debitHeadList = json.findValue("debitHeadList") != null ? json.findValue("debitHeadList").toString()
                : null;
        int isDebit = 1;
        saveProvisionJournalEntryDetail(user, em, newProvJournalEntry, debitHeadList, isDebit,
                pjEntryDetailDebitCreditList, txnDebitRoundOff, roundOfSpecific);

        String creditHeadList = json.findValue("creditHeadList") != null ? json.findValue("creditHeadList").toString()
                : null;
        isDebit = 0;
        saveProvisionJournalEntryDetail(user, em, newProvJournalEntry, creditHeadList, isDebit,
                pjEntryDetailDebitCreditList, txnCreditRoundOff, roundOfSpecific);
        newProvJournalEntry.setProvisionJournalEntryDetails(pjEntryDetailDebitCreditList);
        entitytransaction.commit();

        if (ConfigParams.getInstance().isDeploymentSingleUser(user)) {
            ArrayNode singleUserAccounting = result.putArray("singleUserAccounting");
            ObjectNode row = Json.newObject();
            String suppDoc = json.findValue("supportingdoc") != null ? json.findValue("supportingdoc").asText() : "";
            String paymentDetails = json.findValue("txnReceiptDetails") != null
                    ? json.findValue("txnReceiptDetails").asText()
                    : "";
            String txnPaymentBank = json.findValue("txnreceiptPaymentBank") != null
                    ? json.findValue("txnreceiptPaymentBank").asText()
                    : "";
            String txnInstrumentNum = json.findValue("txnInstrumentNum") != null
                    ? json.findValue("txnInstrumentNum").asText()
                    : "";
            String bankInf = json.findValue("txnReceiptTypeBankDetails") != null
                    ? json.findValue("txnReceiptTypeBankDetails").asText()
                    : "";
            String useremail = json.findValue("useremail") != null ? json.findValue("useremail").asText() : "";
            if (newProvJournalEntry.getId() != null) {
                row.put("transactionPrimId", newProvJournalEntry.getId().toString());
            } else {
                row.put("transactionPrimId", "");
            }
            row.put("suppDoc", suppDoc);
            row.put("txnRemarks", txnRemarks);
            row.put("paymentDetails", paymentDetails);
            if (newProvJournalEntry.getTransactionDate() != null) {
                row.put("txnInvDate", IdosConstants.idosdf.format(newProvJournalEntry.getTransactionDate()));
            } else {
                row.put("txnInvDate", "");
            }
            row.put("txnPaymentBank", txnPaymentBank);
            row.put("txnInstrumentNum", txnInstrumentNum);
            row.put("txnInstrumentDate", txnInstrumentDate);
            row.put("bankInf", bankInf);
            row.put("selectedApproverAction", "1");
            row.put("selectedAddApproverEmail", "");
            row.put("useremail", useremail);
            singleUserAccounting.add(row);
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "************* End " + result);
        return result;
    }

    /**
     * Store provision journal entry in DB.
     *
     * @param user
     * @param em
     * @param provisionJournalEntry
     * @param accountHeads
     * @param accountHeadsClass
     * @param headAmounts
     */
    private void saveProvisionJournalEntryDetail(Users user, EntityManager em,
            IdosProvisionJournalEntry provisionJournalEntry, String txnForItemStr, int isDebit,
            List<ProvisionJournalEntryDetail> pjEntryDetailDebitCreditList, Double roundOffAmount,
            Specifics roundOffSpecific) throws IDOSException {
        try {
            JSONArray arrJSON = new JSONArray(txnForItemStr);
            for (int i = 0; i < arrJSON.length(); i++) {
                JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                String accountHead = rowItemData.getString("txnItems").trim();
                // String firstFourType = accountHead.substring(0, 4);
                // String restAfterFourId = accountHead.substring(4, accountHead.length());
                String headId = accountHead.substring(4, accountHead.length()); // rowItemData.getString("txnItems").trim();
                String headType = rowItemData.getString("headType").trim();
                Double txnPerUnitPrice = 0.0;
                if (rowItemData.get("txnUnitPrice") != null && !"".equals(rowItemData.getString("txnUnitPrice"))) {
                    txnPerUnitPrice = rowItemData.getDouble("txnUnitPrice");
                }
                Double txnNoOfUnit = 0.0;
                if (rowItemData.get("txnNoOfUnit") != null && !"".equals(rowItemData.getString("txnNoOfUnit"))) {
                    txnNoOfUnit = rowItemData.getDouble("txnNoOfUnit");
                }
                Double amountDbl = rowItemData.get("headTotalAmt") == null ? 0.0
                        : rowItemData.getDouble("headTotalAmt");
                Long projectid = 0L;
                if (rowItemData.get("projectid") != null && !rowItemData.isNull("projectid")
                        && !rowItemData.getString("projectid").isEmpty()
                        && !"".equals(rowItemData.getString("projectid"))) {
                    projectid = rowItemData.getLong("projectid");
                }
                Project project = null;
                if (projectid != null) {
                    project = Project.findById(projectid);
                }
                ProvisionJournalEntryDetail provisionJournalEntryDetail = new ProvisionJournalEntryDetail();
                if (IdosConstants.HEAD_SPECIFIC.equals(headType) || IdosConstants.HEAD_TDS_INPUT.equals(headType)
                        || IdosConstants.HEAD_TDS_192.equals(headType) || IdosConstants.HEAD_TDS_194A.equals(headType)
                        || IdosConstants.HEAD_TDS_194C1.equals(headType)
                        || IdosConstants.HEAD_TDS_194C2.equals(headType) || IdosConstants.HEAD_TDS_194H.equals(headType)
                        || IdosConstants.HEAD_TDS_194I1.equals(headType)
                        || IdosConstants.HEAD_TDS_194I2.equals(headType) || IdosConstants.HEAD_TDS_194J.equals(headType)
                        || IdosConstants.HEAD_EMP_CLAIM.equals(headType)
                        || IdosConstants.HEAD_PAYROLL_EXPENSE.equals(headType)
                        || IdosConstants.HEAD_PAYROLL_DEDUCTIONS.equals(headType)) {
                    if (headId != null && !headId.equals("")) {
                        Specifics specifics = Specifics.findById(IdosUtil.convertStringToLong(headId));
                        provisionJournalEntryDetail.setHeadID(specifics.getId());
                        provisionJournalEntryDetail.setUnitPrice(txnPerUnitPrice);
                        provisionJournalEntryDetail.setUnits(txnNoOfUnit);
                        /*
                         * if (txnPerUnitPrice != null && txnNoOfUnit != null && txnPerUnitPrice > 0.0
                         * && txnNoOfUnit > 0.0) {
                         * amountDbl = txnPerUnitPrice * txnNoOfUnit;
                         * }
                         */
                    } else {
                        throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                                IdosConstants.INVALID_DATA_EXCEPTION, "Ledger not found.");
                    }
                } else if (headType.equals("cash")) {
                    if (headId != null && !headId.equals("")) {
                        BranchDepositBoxKey branchDepositBoxKey = BranchDepositBoxKey
                                .findById(IdosUtil.convertStringToLong(headId));
                        provisionJournalEntryDetail.setHeadID(branchDepositBoxKey.getId());
                    } else {
                        throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                                IdosConstants.INVALID_DATA_EXCEPTION, "Cash account is not found.");
                    }
                } else if (headType.equals("pett")) {
                    if (headId != null && !headId.equals("")) {
                        BranchDepositBoxKey branchDepositBoxKey = BranchDepositBoxKey
                                .findById(IdosUtil.convertStringToLong(headId));
                        provisionJournalEntryDetail.setHeadID(branchDepositBoxKey.getId());
                    } else {
                        throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                                IdosConstants.INVALID_DATA_EXCEPTION, "Pettycash account is not found.");
                    }
                } else if (headType.equals("bank")) {
                    if (headId != null && !headId.equals("")) {
                        BranchBankAccounts branchBankAccounts = BranchBankAccounts
                                .findById(IdosUtil.convertStringToLong(headId));
                        if (branchBankAccounts == null) {
                            throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
                                    IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
                                    "Bank is not selected in transaction when payment mode is Bank.");
                        }
                        provisionJournalEntryDetail.setHeadID(branchBankAccounts.getId());
                    } else {
                        throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                                IdosConstants.INVALID_DATA_EXCEPTION, "Bank account is not found.");
                    }
                } else if (headType.equals("vend") || headType.equals("vAdv")) {
                    if (headId != null && !headId.equals("")) {
                        Vendor vendor = Vendor.findById(IdosUtil.convertStringToLong(headId));
                        provisionJournalEntryDetail.setHeadID(vendor.getId());
                    } else {
                        throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                                IdosConstants.INVALID_DATA_EXCEPTION, "Vendor is not found.");
                    }
                } else if (headType.equals("cust") || headType.equals("cAdv")) {
                    if (headId != null && !headId.equals("")) {
                        Vendor vendor = Vendor.findById(IdosUtil.convertStringToLong(headId));
                        provisionJournalEntryDetail.setHeadID(vendor.getId());
                    } else {
                        throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                                IdosConstants.INVALID_DATA_EXCEPTION, "Customer is not found.");
                    }
                } else if (headType.equals("user")) {
                    if (headId != null && !headId.equals("")) {
                        Users users = Users.findById(IdosUtil.convertStringToLong(headId));
                        provisionJournalEntryDetail.setHeadID(users.getId());
                    } else {
                        throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                                IdosConstants.INVALID_DATA_EXCEPTION, "User is not found.");
                    }
                } else if (headType.equals("taxs") || IdosConstants.HEAD_SGST.equals(headType)
                        || IdosConstants.HEAD_CGST.equals(headType) || IdosConstants.HEAD_IGST.equals(headType)
                        || IdosConstants.HEAD_CESS.equals(headType) || IdosConstants.HEAD_RCM_SGST_IN.equals(headType)
                        || IdosConstants.HEAD_RCM_CGST_IN.equals(headType)
                        || IdosConstants.HEAD_RCM_IGST_IN.equals(headType)
                        || IdosConstants.HEAD_RCM_CESS_IN.equals(headType)
                        || IdosConstants.HEAD_RCM_SGST_OUTPUT.equals(headType)
                        || IdosConstants.HEAD_RCM_CGST_OUTPUT.equals(headType)
                        || IdosConstants.HEAD_RCM_IGST_OUTPUT.equals(headType)
                        || IdosConstants.HEAD_RCM_CESS_OUTPUT.equals(headType)) {
                    if (headId != null && !headId.equals("")) {
                        BranchTaxes branchtaxes = BranchTaxes.findById(IdosUtil.convertStringToLong(headId));
                        provisionJournalEntryDetail.setHeadID(branchtaxes.getId());
                    } else {
                        throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                                "selected tax id is not found.", "selected tax is not found.");
                    }
                } else if (IdosConstants.HEAD_INTR_BRANCH.equalsIgnoreCase(headType)) {
                    String headId2 = headId.substring(headId.indexOf("-") + 1);
                    headId = headId.substring(0, headId.indexOf("-"));
                    Branch fromBranch = Branch.findById(IdosUtil.convertStringToLong(headId));
                    Branch toBranch = Branch.findById(IdosUtil.convertStringToLong(headId2));
                    provisionJournalEntryDetail.setHeadID(fromBranch.getId());
                    provisionJournalEntryDetail.setHeadID2(toBranch.getId());
                }

                provisionJournalEntryDetail.setHeadType(headType);
                /*
                 * if(amountDbl != null && amountDbl != 0.0) {
                 * amountDbl = Math.round(amountDbl * 100.00)/100.00;
                 * }
                 */
                provisionJournalEntryDetail.setHeadAmount(amountDbl);
                provisionJournalEntryDetail.setIsDebit(isDebit);
                if (isDebit == 1) {
                    provisionJournalEntryDetail.setBranch(provisionJournalEntry.getDebitBranch());
                } else {
                    provisionJournalEntryDetail.setBranch(provisionJournalEntry.getCreditBranch());
                }
                provisionJournalEntryDetail.setOrganization(provisionJournalEntry.getProvisionMadeForOrganization());
                provisionJournalEntryDetail.setProvisionJournalEntry(provisionJournalEntry);
                provisionJournalEntryDetail.setProject(project);

                // pjEntryDetailList.add(provisionJournalEntryDetail);
                pjEntryDetailDebitCreditList.add(provisionJournalEntryDetail);
                genericDao.saveOrUpdate(provisionJournalEntryDetail, user, em);
            }
            if (roundOffAmount != null && roundOffSpecific != null) {
                ProvisionJournalEntryDetail provisionJournalEntryDetail = new ProvisionJournalEntryDetail();
                provisionJournalEntryDetail.setHeadID(roundOffSpecific.getId());
                provisionJournalEntryDetail.setUnitPrice(roundOffAmount);
                provisionJournalEntryDetail.setUnits(1.0);
                provisionJournalEntryDetail.setHeadType(IdosConstants.HEAD_SPECIFIC);
                provisionJournalEntryDetail.setHeadAmount(roundOffAmount);
                provisionJournalEntryDetail.setIsDebit(isDebit);
                if (isDebit == 1) {
                    provisionJournalEntryDetail.setBranch(provisionJournalEntry.getDebitBranch());
                } else {
                    provisionJournalEntryDetail.setBranch(provisionJournalEntry.getCreditBranch());
                }
                provisionJournalEntryDetail.setOrganization(provisionJournalEntry.getProvisionMadeForOrganization());
                provisionJournalEntryDetail.setProvisionJournalEntry(provisionJournalEntry);
                pjEntryDetailDebitCreditList.add(provisionJournalEntryDetail);
                genericDao.saveOrUpdate(provisionJournalEntryDetail, user, em);
            }
        } catch (org.json.JSONException ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    ex.getMessage(), "Error on Journal Entry- submit for approval");
        }
        // provisionJournalEntry.setProvisionJournalEntryDetails(pjEntryDetailDebitCreditList);
    }

    @Override
    public int provisionApproverAction(ObjectNode result, JsonNode json, Users user, EntityManager em,
            EntityTransaction entitytransaction, IdosProvisionJournalEntry newProvJournalEntry) throws IDOSException {
        Boolean isSingleUserDeploy = ConfigParams.getInstance().isDeploymentSingleUser(user);
        String selectedApproverAction = json.findValue("selectedApproverAction").asText();
        String transactionPrimId = json.findValue("transactionPrimId").asText();
        String suppDoc = json.findValue("suppDoc") != null ? json.findValue("suppDoc").asText() : null;
        String txnRmarks = json.findValue("txnRmarks").asText();
        do {
            entitytransaction.begin();
            if (!newProvJournalEntry.getTransactionStatus().equals("Approved")) {
                if (selectedApproverAction.equals("1")) {
                    newProvJournalEntry.setTransactionStatus("Approved");
                    newProvJournalEntry.setModifiedBy(user);
                    newProvJournalEntry.setApproverActionBy(user);
                    saveUpdateRemarksAndUploadedDocuments(suppDoc, txnRmarks, newProvJournalEntry, user);
                }
            }
            if (!newProvJournalEntry.getTransactionStatus().equals("Rejected")) {
                if (selectedApproverAction.equals("2")) {
                    newProvJournalEntry.setTransactionStatus("Rejected");
                    newProvJournalEntry.setModifiedBy(user);
                    saveUpdateRemarksAndUploadedDocuments(suppDoc, txnRmarks, newProvJournalEntry, user);
                }
            }
            if (!newProvJournalEntry.getTransactionStatus().equals("Require Additional Approval")) {
                if (selectedApproverAction.equals("3")) {
                    newProvJournalEntry.setTransactionStatus("Require Additional Approval");
                    newProvJournalEntry.setModifiedBy(user);
                    newProvJournalEntry.setApproverActionBy(user);
                    String selectedAddApproverEmail = json.findValue("selectedAddApproverEmail").asText();
                    newProvJournalEntry.setSelectedAdditionalApprover(selectedAddApproverEmail);
                    saveUpdateRemarksAndUploadedDocuments(suppDoc, txnRmarks, newProvJournalEntry, user);
                }
            }
            if (selectedApproverAction.equals("4")) {
                if (!newProvJournalEntry.getTransactionStatus().equals("Accounted")) {
                    BranchCashService branchCashService = new BranchCashServiceImpl();
                    BranchBankService branchBankService = new BranchBankServiceImpl();
                    newProvJournalEntry.setTransactionStatus("Accounted");
                    newProvJournalEntry.setModifiedBy(user);
                    saveUpdateRemarksAndUploadedDocuments(suppDoc, txnRmarks, newProvJournalEntry, user);
                    Map<String, Object> criterias = new HashMap<String, Object>();
                    /***************************
                     * TRIAL BALANCE TABLES
                     ******************************/
                    // Income or Expense Items or for which item specifics is set even in case of
                    // assets/liabilities
                    // credited expense/income items
                    List<ProvisionJournalEntryDetail> provisionJournalEntryDetailList = newProvJournalEntry
                            .getProvisionJournalEntryDetails();
                    for (ProvisionJournalEntryDetail pjeDetail : provisionJournalEntryDetailList) {
                        if (IdosConstants.HEAD_SPECIFIC.equals(pjeDetail.getHeadType())) {
                            criterias.clear();
                            criterias.put("id", pjeDetail.getHeadID());
                            criterias.put("presentStatus", 1);
                            List<Specifics> specificsList = genericDao.findByCriteria(Specifics.class, criterias, em);
                            Specifics specifics = specificsList.get(0);
                            TrialBalanceCOAItems trialBalCOA = new TrialBalanceCOAItems(); // for sell on cash and
                                                                                           // credit both
                            trialBalCOA.setTransactionId(newProvJournalEntry.getId());
                            trialBalCOA.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                            trialBalCOA.setDate(newProvJournalEntry.getTransactionDate());
                            trialBalCOA.setBranch(newProvJournalEntry.getDebitBranch()); // both branches are same
                            trialBalCOA.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                            trialBalCOA.setTransactionParticulars(specifics.getParticularsId());
                            trialBalCOA.setTransactionSpecifics(specifics);
                            if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
                                trialBalCOA.setCreditAmount(pjeDetail.getHeadAmount());
                            } else if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjeDetail.getIsDebit()) {
                                trialBalCOA.setDebitAmount(pjeDetail.getHeadAmount()); // for expense/income debit item
                            }
                            genericDao.saveOrUpdate(trialBalCOA, user, em);
                            if (specifics.getIsTradingInvenotryItem() != null
                                    && specifics.getIsTradingInvenotryItem() == 1) {
                                PJE_INVENTORY_DAO.saveTradingInventory(newProvJournalEntry, pjeDetail, specifics, user,
                                        em);
                            }
                        } else if (IdosConstants.HEAD_VENDOR.equals(pjeDetail.getHeadType())
                                || IdosConstants.HEAD_CUSTOMER.equals(pjeDetail.getHeadType())) {
                            /*
                             * Customer-vendor - No specifics or particulars are set since it is cust
                             * credit/debit, but if
                             * required we can set it in TransactionDAOImpl.provisionJournalEntry();
                             */
                            criterias.clear();
                            criterias.put("id", pjeDetail.getHeadID());
                            criterias.put("presentStatus", 1);
                            List<Vendor> vendorList = genericDao.findByCriteria(Vendor.class, criterias, em);
                            if (vendorList != null && vendorList.size() > 0) {
                                Vendor vendor = vendorList.get(0);
                                if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
                                    TrialBalanceCustomerVendor trialBalCustVendor = new TrialBalanceCustomerVendor(); // sell
                                                                                                                      // on
                                                                                                                      // credit
                                    trialBalCustVendor.setTransactionId(newProvJournalEntry.getId());
                                    trialBalCustVendor
                                            .setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                                    // trialBalCustVendor.setTransactionSpecifics(newProvJournalEntry.getTransactionSpecifics());
                                    // //for vendor/cust not storing specifics currently
                                    // trialBalCustVendor.setTransactionParticulars(newProvJournalEntry.getTransactionParticulars());
                                    trialBalCustVendor.setDate(newProvJournalEntry.getTransactionDate());
                                    trialBalCustVendor.setBranch(newProvJournalEntry.getDebitBranch());
                                    trialBalCustVendor
                                            .setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                                    trialBalCustVendor.setVendorType(vendor.getType()); // vendor type=2 means customer
                                                                                        // and =1 means vendor
                                    trialBalCustVendor.setVendor(vendor);
                                    trialBalCustVendor.setCreditAmount(pjeDetail.getHeadAmount());
                                    genericDao.saveOrUpdate(trialBalCustVendor, user, em);
                                } else if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjeDetail.getIsDebit()) {
                                    TrialBalanceCustomerVendor trialBalCustVendor = new TrialBalanceCustomerVendor(); // sell
                                                                                                                      // on
                                                                                                                      // credit
                                    trialBalCustVendor.setTransactionId(newProvJournalEntry.getId());
                                    trialBalCustVendor
                                            .setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                                    trialBalCustVendor.setDate(newProvJournalEntry.getTransactionDate());
                                    trialBalCustVendor.setBranch(newProvJournalEntry.getDebitBranch());
                                    trialBalCustVendor
                                            .setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                                    trialBalCustVendor.setVendorType(vendor.getType()); // vendor type=2 means customer
                                                                                        // and =1 means vendor
                                    trialBalCustVendor.setVendor(vendor);
                                    trialBalCustVendor.setDebitAmount(pjeDetail.getHeadAmount());
                                    genericDao.saveOrUpdate(trialBalCustVendor, user, em);
                                }
                            }
                        } else if (IdosConstants.HEAD_VENDOR_ADV.equals(pjeDetail.getHeadType())
                                || IdosConstants.HEAD_CUSTOMER_ADV.equals(pjeDetail.getHeadType())) {
                            /*
                             * Customer-vendor advance - No specifics or particulars are set since it is
                             * cust credit/debit, but if
                             * required we can set it in TransactionDAOImpl.provisionJournalEntry();
                             */
                            criterias.clear();
                            criterias.put("id", pjeDetail.getHeadID());
                            criterias.put("presentStatus", 1);
                            List<Vendor> vendorList = genericDao.findByCriteria(Vendor.class, criterias, em);
                            if (vendorList != null && vendorList.size() > 0) {
                                Vendor vendor = vendorList.get(0);
                                if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
                                    // Shown under Assets- Vendor Advance or libaities - customer advance
                                    TrialBalanceVendorAdvance trialBalVenAdv = new TrialBalanceVendorAdvance();
                                    trialBalVenAdv.setTransactionId(newProvJournalEntry.getId());
                                    trialBalVenAdv.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                                    // trialBalVenAdv.setTransactionParticulars(tranItem.getTransactionParticulars());
                                    // trialBalVenAdv.setTransactionSpecifics(tranItem.getTransactionSpecifics());
                                    trialBalVenAdv.setDate(newProvJournalEntry.getTransactionDate());
                                    trialBalVenAdv.setBranch(newProvJournalEntry.getDebitBranch());
                                    trialBalVenAdv
                                            .setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                                    trialBalVenAdv.setVendorType(vendor.getType()); // vendor type=2 means customer and
                                                                                    // =1 means vendor
                                    trialBalVenAdv.setVendor(vendor);
                                    // for vendor&cust if vendor adv is credited then it is credited in TB too
                                    trialBalVenAdv.setCreditAmount(pjeDetail.getHeadAmount());
                                    genericDao.saveOrUpdate(trialBalVenAdv, user, em);
                                } else if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjeDetail.getIsDebit()) {
                                    TrialBalanceVendorAdvance trialBalVenAdv = new TrialBalanceVendorAdvance();
                                    trialBalVenAdv.setTransactionId(newProvJournalEntry.getId());
                                    trialBalVenAdv.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                                    // trialBalVenAdv.setTransactionParticulars(tranItem.getTransactionParticulars());
                                    // trialBalVenAdv.setTransactionSpecifics(tranItem.getTransactionSpecifics());
                                    trialBalVenAdv.setDate(newProvJournalEntry.getTransactionDate());
                                    trialBalVenAdv.setBranch(newProvJournalEntry.getDebitBranch());
                                    trialBalVenAdv
                                            .setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                                    trialBalVenAdv.setVendorType(vendor.getType()); // vendor type=2 means customer and
                                                                                    // =1 means vendor
                                    trialBalVenAdv.setVendor(vendor);
                                    trialBalVenAdv.setDebitAmount(pjeDetail.getHeadAmount());
                                    genericDao.saveOrUpdate(trialBalVenAdv, user, em);
                                }
                            }
                        } else if (IdosConstants.HEAD_CASH.equals(pjeDetail.getHeadType())
                                || IdosConstants.HEAD_PETTY.equals(pjeDetail.getHeadType())) {
                            // asset - Cash Account is selected for either debit or creidt
                            /*
                             * criterias.clear();
                             * criterias.put("id", pjeDetail.getHeadID());
                             * List<BranchCashCount> branchCashCountList =
                             * genericDao.findByCriteria(BranchCashCount.class, criterias, em);
                             * BranchCashCount branchCashCount = branchCashCountList.get(0);
                             */
                            BranchDepositBoxKey branchDepositBoxKey = BranchDepositBoxKey
                                    .findById(pjeDetail.getHeadID());
                            if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
                                TrialBalanceBranchCash trialBalCash = new TrialBalanceBranchCash(); // will affect only
                                                                                                    // for sell on cash
                                trialBalCash.setTransactionId(newProvJournalEntry.getId());
                                trialBalCash.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                                trialBalCash.setDate(newProvJournalEntry.getTransactionDate());
                                trialBalCash.setBranch(newProvJournalEntry.getDebitBranch());
                                trialBalCash.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                                trialBalCash.setCreditAmount(pjeDetail.getHeadAmount());
                                if (IdosConstants.HEAD_CASH.equals(pjeDetail.getHeadType())) {
                                    trialBalCash.setCashType(new Integer(IdosConstants.CASH));
                                    Double resultantCash = branchCashService.updateBranchCashDetail(em, user,
                                            newProvJournalEntry.getCreditBranch(), pjeDetail.getHeadAmount(), true,
                                            newProvJournalEntry.getTransactionDate(), result);
                                    result.put("resultantCash", resultantCash);
                                    if (resultantCash < 0) {
                                        return -1;
                                    }
                                } else {
                                    trialBalCash.setCashType(new Integer(IdosConstants.PETTY_CASH));
                                    Double resultantCash = branchCashService.updateBranchPettyCashDetail(em, genericDao,
                                            user, newProvJournalEntry.getCreditBranch(), pjeDetail.getHeadAmount(),
                                            true, newProvJournalEntry.getTransactionDate(), result);
                                    result.put("resultantPettyCashAmount", resultantCash);
                                    if (resultantCash < 0) {
                                        return -1;
                                    }
                                }
                                if (branchDepositBoxKey != null) {
                                    trialBalCash.setBranchDepositBoxKey(branchDepositBoxKey);
                                }
                                genericDao.saveOrUpdate(trialBalCash, user, em);
                            } else if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjeDetail.getIsDebit()) {
                                TrialBalanceBranchCash trialBalCash = new TrialBalanceBranchCash(); // will affect only
                                                                                                    // for sell on cash
                                trialBalCash.setTransactionId(newProvJournalEntry.getId());
                                trialBalCash.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                                trialBalCash.setDate(newProvJournalEntry.getTransactionDate());
                                trialBalCash.setBranch(newProvJournalEntry.getDebitBranch());
                                trialBalCash.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                                trialBalCash.setDebitAmount(pjeDetail.getHeadAmount());
                                if (IdosConstants.HEAD_CASH.equals(pjeDetail.getHeadType())) {
                                    trialBalCash.setCashType(new Integer(IdosConstants.CASH));
                                    Double resultantCash = branchCashService.updateBranchCashDetail(em, user,
                                            newProvJournalEntry.getDebitBranch(), pjeDetail.getHeadAmount(), false,
                                            newProvJournalEntry.getTransactionDate(), result);
                                    result.put("resultantCash", resultantCash);
                                    if (resultantCash < 0) {
                                        return -1;
                                    }
                                } else {
                                    trialBalCash.setCashType(new Integer(IdosConstants.PETTY_CASH));
                                    Double resultantCash = branchCashService.updateBranchPettyCashDetail(em, genericDao,
                                            user, newProvJournalEntry.getDebitBranch(), pjeDetail.getHeadAmount(),
                                            false, newProvJournalEntry.getTransactionDate(), result);
                                    result.put("resultantPettyCashAmount", resultantCash);
                                    if (resultantCash < 0) {
                                        return -1;
                                    }
                                }
                                if (branchDepositBoxKey != null) {
                                    trialBalCash.setBranchDepositBoxKey(branchDepositBoxKey);
                                }
                                genericDao.saveOrUpdate(trialBalCash, user, em);
                            }
                        } else if (IdosConstants.HEAD_BANK.equals(pjeDetail.getHeadType())) {
                            criterias.clear();
                            criterias.put("id", pjeDetail.getHeadID());
                            criterias.put("presentStatus", 1);
                            List<BranchBankAccounts> branchBankAccountsList = genericDao
                                    .findByCriteria(BranchBankAccounts.class, criterias, em);
                            BranchBankAccounts branchBankAccount = branchBankAccountsList.get(0);
                            TrialBalanceBranchBank trialBalBank = new TrialBalanceBranchBank(); // will affect only for
                                                                                                // sell on cash if bank
                                                                                                // check or DD
                            trialBalBank.setTransactionId(newProvJournalEntry.getId());
                            trialBalBank.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                            trialBalBank.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                            trialBalBank.setDate(newProvJournalEntry.getTransactionDate());
                            trialBalBank.setBranchBankAccounts(branchBankAccount);
                            if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
                                boolean branchBankDetailEntered = branchBankService.updateBranchBankDetailTransaction(
                                        em, user, branchBankAccount, pjeDetail.getHeadAmount(), true, result,
                                        newProvJournalEntry.getTransactionDate(),
                                        newProvJournalEntry.getCreditBranch());
                                if (!branchBankDetailEntered) {
                                    return -1;
                                }
                                trialBalBank.setBranch(newProvJournalEntry.getCreditBranch());
                                trialBalBank.setCreditAmount(pjeDetail.getHeadAmount());
                                // Double resultantAmount = branchBankService.updateBranchBankDetail(em,
                                // genericDao, user, branchBankAccount, pjeDetail.getHeadAmount(), true);
                            } else if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjeDetail.getIsDebit()) {
                                boolean branchBankDetailEntered = branchBankService.updateBranchBankDetailTransaction(
                                        em, user, branchBankAccount, pjeDetail.getHeadAmount(), false, result,
                                        newProvJournalEntry.getTransactionDate(), newProvJournalEntry.getDebitBranch());
                                if (!branchBankDetailEntered) {
                                    return -1; // since balance is in -ve don't make any changes in DB
                                }
                                // Double resultantAmount = branchBankService.updateBranchBankDetail(em,
                                // genericDao, user, branchBankAccount, pjeDetail.getHeadAmount(), false)
                                trialBalBank.setBranch(newProvJournalEntry.getDebitBranch());
                                trialBalBank.setDebitAmount(pjeDetail.getHeadAmount());
                            }
                            genericDao.saveOrUpdate(trialBalBank, user, em);
                        } else if (IdosConstants.HEAD_USER.equals(pjeDetail.getHeadType())) {
                            criterias.clear();
                            criterias.put("id", pjeDetail.getHeadID());
                            criterias.put("presentStatus", 1);
                            List<Users> usersList = genericDao.findByCriteria(Users.class, criterias, em);
                            Users users = usersList.get(0);
                            if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
                                TrialBalanceUserAdvance trialBalUser = new TrialBalanceUserAdvance(); // will affect
                                                                                                      // only for sell
                                                                                                      // on cash if bank
                                                                                                      // check or DD
                                trialBalUser.setTransactionId(newProvJournalEntry.getId());
                                trialBalUser.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                                trialBalUser.setDate(newProvJournalEntry.getTransactionDate());
                                trialBalUser.setBranch(newProvJournalEntry.getCreditBranch());
                                trialBalUser.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                                trialBalUser.setUser(users);
                                trialBalUser.setCreditAmount(pjeDetail.getHeadAmount());
                                genericDao.saveOrUpdate(trialBalUser, user, em);
                            } else if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjeDetail.getIsDebit()) {
                                TrialBalanceUserAdvance trialBalUser = new TrialBalanceUserAdvance(); // will affect
                                                                                                      // only for sell
                                                                                                      // on cash if bank
                                                                                                      // check or DD
                                trialBalUser.setTransactionId(newProvJournalEntry.getId());
                                trialBalUser.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                                trialBalUser.setDate(newProvJournalEntry.getTransactionDate());
                                trialBalUser.setBranch(newProvJournalEntry.getDebitBranch());
                                trialBalUser.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                                trialBalUser.setUser(users);
                                trialBalUser.setDebitAmount(pjeDetail.getHeadAmount());
                                genericDao.saveOrUpdate(trialBalUser, user, em);
                            }
                        } else if (IdosConstants.HEAD_TAXS.equals(pjeDetail.getHeadType())) {
                            criterias.clear();
                            criterias.put("id", pjeDetail.getHeadID());
                            criterias.put("presentStatus", 1);
                            List<BranchTaxes> branchList = genericDao.findByCriteria(BranchTaxes.class, criterias, em);
                            BranchTaxes bnchTaxes = branchList.get(0);
                            if (bnchTaxes != null && bnchTaxes.getTaxType() == 1) {
                                TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
                                trialBalTaxes.setTransactionId(newProvJournalEntry.getId());
                                trialBalTaxes.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                                trialBalTaxes.setDate(newProvJournalEntry.getTransactionDate());
                                trialBalTaxes.setBranchTaxes(bnchTaxes);
                                trialBalTaxes.setTaxType(1); // Input taxes
                                trialBalTaxes.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                                if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
                                    trialBalTaxes.setCreditAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getCreditBranch());
                                } else {
                                    trialBalTaxes.setDebitAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getDebitBranch());
                                }
                                genericDao.saveOrUpdate(trialBalTaxes, user, em);
                            } else if (bnchTaxes != null && bnchTaxes.getTaxType() == 2) {
                                TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
                                trialBalTaxes.setTransactionId(newProvJournalEntry.getId());
                                trialBalTaxes.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                                trialBalTaxes.setDate(newProvJournalEntry.getTransactionDate());
                                trialBalTaxes.setBranchTaxes(bnchTaxes);
                                trialBalTaxes.setTaxType(2); // output taxes
                                trialBalTaxes.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                                if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
                                    trialBalTaxes.setCreditAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getCreditBranch());
                                } else {
                                    trialBalTaxes.setDebitAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getDebitBranch());
                                }
                                genericDao.saveOrUpdate(trialBalTaxes, user, em);
                            }
                        } else if (IdosConstants.HEAD_SGST.equals(pjeDetail.getHeadType())) {
                            criterias.clear();
                            criterias.put("id", pjeDetail.getHeadID());
                            criterias.put("presentStatus", 1);
                            List<BranchTaxes> branchList = genericDao.findByCriteria(BranchTaxes.class, criterias, em);
                            BranchTaxes bnchTaxes = branchList.get(0);
                            if (bnchTaxes != null && bnchTaxes.getTaxType() == IdosConstants.INPUT_SGST) {
                                TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
                                trialBalTaxes.setTransactionId(newProvJournalEntry.getId());
                                trialBalTaxes.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                                trialBalTaxes.setDate(newProvJournalEntry.getTransactionDate());
                                trialBalTaxes.setBranchTaxes(bnchTaxes);
                                trialBalTaxes.setTaxType(bnchTaxes.getTaxType()); // Input taxes
                                trialBalTaxes.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                                if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
                                    trialBalTaxes.setCreditAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getCreditBranch());
                                } else {
                                    trialBalTaxes.setDebitAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getDebitBranch());
                                }
                                genericDao.saveOrUpdate(trialBalTaxes, user, em);
                            } else if (bnchTaxes != null && bnchTaxes.getTaxType() == IdosConstants.OUTPUT_SGST) {
                                TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
                                trialBalTaxes.setTransactionId(newProvJournalEntry.getId());
                                trialBalTaxes.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                                trialBalTaxes.setDate(newProvJournalEntry.getTransactionDate());
                                trialBalTaxes.setBranchTaxes(bnchTaxes);
                                trialBalTaxes.setTaxType(bnchTaxes.getTaxType()); // output taxes
                                trialBalTaxes.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                                if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
                                    trialBalTaxes.setCreditAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getCreditBranch());
                                } else {
                                    trialBalTaxes.setDebitAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getDebitBranch());
                                }
                                genericDao.saveOrUpdate(trialBalTaxes, user, em);
                            }
                        } else if (IdosConstants.HEAD_CGST.equals(pjeDetail.getHeadType())) {
                            criterias.clear();
                            criterias.put("id", pjeDetail.getHeadID());
                            criterias.put("presentStatus", 1);
                            List<BranchTaxes> branchList = genericDao.findByCriteria(BranchTaxes.class, criterias, em);
                            BranchTaxes bnchTaxes = branchList.get(0);
                            if (bnchTaxes != null && bnchTaxes.getTaxType() == IdosConstants.INPUT_CGST) {
                                TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
                                trialBalTaxes.setTransactionId(newProvJournalEntry.getId());
                                trialBalTaxes.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                                trialBalTaxes.setDate(newProvJournalEntry.getTransactionDate());
                                trialBalTaxes.setBranchTaxes(bnchTaxes);
                                trialBalTaxes.setTaxType(bnchTaxes.getTaxType()); // Input taxes
                                trialBalTaxes.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                                if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
                                    trialBalTaxes.setCreditAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getCreditBranch());
                                } else {
                                    trialBalTaxes.setDebitAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getDebitBranch());
                                }
                                genericDao.saveOrUpdate(trialBalTaxes, user, em);
                            } else if (bnchTaxes != null && bnchTaxes.getTaxType() == IdosConstants.OUTPUT_CGST) {
                                TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
                                trialBalTaxes.setTransactionId(newProvJournalEntry.getId());
                                trialBalTaxes.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                                trialBalTaxes.setDate(newProvJournalEntry.getTransactionDate());
                                trialBalTaxes.setBranchTaxes(bnchTaxes);
                                trialBalTaxes.setTaxType(bnchTaxes.getTaxType()); // output taxes
                                trialBalTaxes.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                                if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
                                    trialBalTaxes.setCreditAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getCreditBranch());
                                } else {
                                    trialBalTaxes.setDebitAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getDebitBranch());
                                }
                                genericDao.saveOrUpdate(trialBalTaxes, user, em);
                            }
                        } else if (IdosConstants.HEAD_IGST.equals(pjeDetail.getHeadType())) {
                            criterias.clear();
                            criterias.put("id", pjeDetail.getHeadID());
                            criterias.put("presentStatus", 1);
                            List<BranchTaxes> branchList = genericDao.findByCriteria(BranchTaxes.class, criterias, em);
                            BranchTaxes bnchTaxes = branchList.get(0);
                            if (bnchTaxes != null && bnchTaxes.getTaxType() == IdosConstants.INPUT_IGST) {
                                TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
                                trialBalTaxes.setTransactionId(newProvJournalEntry.getId());
                                trialBalTaxes.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                                trialBalTaxes.setDate(newProvJournalEntry.getTransactionDate());
                                trialBalTaxes.setBranchTaxes(bnchTaxes);
                                trialBalTaxes.setTaxType(bnchTaxes.getTaxType()); // Input taxes
                                trialBalTaxes.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                                if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
                                    trialBalTaxes.setCreditAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getCreditBranch());
                                } else {
                                    trialBalTaxes.setDebitAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getDebitBranch());
                                }
                                genericDao.saveOrUpdate(trialBalTaxes, user, em);
                            } else if (bnchTaxes != null && bnchTaxes.getTaxType() == IdosConstants.OUTPUT_IGST) {
                                TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
                                trialBalTaxes.setTransactionId(newProvJournalEntry.getId());
                                trialBalTaxes.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                                trialBalTaxes.setDate(newProvJournalEntry.getTransactionDate());
                                trialBalTaxes.setBranchTaxes(bnchTaxes);
                                trialBalTaxes.setTaxType(bnchTaxes.getTaxType()); // output taxes
                                trialBalTaxes.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                                if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
                                    trialBalTaxes.setCreditAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getCreditBranch());
                                } else {
                                    trialBalTaxes.setDebitAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getDebitBranch());
                                }
                                genericDao.saveOrUpdate(trialBalTaxes, user, em);
                            }
                        } else if (IdosConstants.HEAD_CESS.equals(pjeDetail.getHeadType())) {
                            criterias.clear();
                            criterias.put("id", pjeDetail.getHeadID());
                            criterias.put("presentStatus", 1);
                            List<BranchTaxes> branchList = genericDao.findByCriteria(BranchTaxes.class, criterias, em);
                            BranchTaxes bnchTaxes = branchList.get(0);
                            if (bnchTaxes != null && bnchTaxes.getTaxType() == IdosConstants.INPUT_CESS) {
                                TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
                                trialBalTaxes.setTransactionId(newProvJournalEntry.getId());
                                trialBalTaxes.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                                trialBalTaxes.setDate(newProvJournalEntry.getTransactionDate());
                                trialBalTaxes.setBranchTaxes(bnchTaxes);
                                trialBalTaxes.setTaxType(bnchTaxes.getTaxType()); // Input taxes
                                trialBalTaxes.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                                if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
                                    trialBalTaxes.setCreditAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getCreditBranch());
                                } else {
                                    trialBalTaxes.setDebitAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getDebitBranch());
                                }
                                genericDao.saveOrUpdate(trialBalTaxes, user, em);
                            } else if (bnchTaxes != null && bnchTaxes.getTaxType() == IdosConstants.OUTPUT_CESS) {
                                TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
                                trialBalTaxes.setTransactionId(newProvJournalEntry.getId());
                                trialBalTaxes.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                                trialBalTaxes.setDate(newProvJournalEntry.getTransactionDate());
                                trialBalTaxes.setBranchTaxes(bnchTaxes);
                                trialBalTaxes.setTaxType(bnchTaxes.getTaxType());
                                trialBalTaxes.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                                if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
                                    trialBalTaxes.setCreditAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getCreditBranch());
                                } else {
                                    trialBalTaxes.setDebitAmount(pjeDetail.getHeadAmount());
                                    trialBalTaxes.setBranch(newProvJournalEntry.getDebitBranch());
                                }
                                genericDao.saveOrUpdate(trialBalTaxes, user, em);
                            }
                        } else if (IdosConstants.HEAD_RCM_SGST_IN.equals(pjeDetail.getHeadType())
                                || IdosConstants.HEAD_RCM_CGST_IN.equals(pjeDetail.getHeadType())
                                || IdosConstants.HEAD_RCM_IGST_IN.equals(pjeDetail.getHeadType())
                                || IdosConstants.HEAD_RCM_CESS_IN.equals(pjeDetail.getHeadType())
                                || IdosConstants.HEAD_RCM_SGST_OUTPUT.equals(pjeDetail.getHeadType())
                                || IdosConstants.HEAD_RCM_CGST_OUTPUT.equals(pjeDetail.getHeadType())
                                || IdosConstants.HEAD_RCM_IGST_OUTPUT.equals(pjeDetail.getHeadType())
                                || IdosConstants.HEAD_RCM_CESS_OUTPUT.equals(pjeDetail.getHeadType())) {
                            BranchTaxes bnchTaxes = BranchTaxes.findById(pjeDetail.getHeadID());
                            saveTrialBalanceTax(newProvJournalEntry, pjeDetail, em, user, bnchTaxes);
                        } else if (IdosConstants.HEAD_INTR_BRANCH.equals(pjeDetail.getHeadType())) {
                            TrialBalanceInterBranch tbInterBranch = new TrialBalanceInterBranch();
                            tbInterBranch.setTransactionId(newProvJournalEntry.getId());
                            tbInterBranch.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                            tbInterBranch.setDate(newProvJournalEntry.getTransactionDate());
                            Branch fromBranch = Branch.findById(pjeDetail.getHeadID());
                            Branch toBranch = Branch.findById(pjeDetail.getHeadID2());
                            tbInterBranch.setFromBranch(fromBranch);
                            tbInterBranch.setToBranch(toBranch);
                            tbInterBranch.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                            if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
                                tbInterBranch.setCreditAmount(pjeDetail.getHeadAmount());
                                tbInterBranch.setTypeIdentifier(1);
                            } else {
                                tbInterBranch.setDebitAmount(pjeDetail.getHeadAmount());
                                tbInterBranch.setTypeIdentifier(2);
                            }
                            genericDao.saveOrUpdate(tbInterBranch, user, em);
                        } else if (IdosConstants.HEAD_TDS_INPUT.equals(pjeDetail.getHeadType())
                                || IdosConstants.HEAD_TDS_192.equals(pjeDetail.getHeadType())
                                || IdosConstants.HEAD_TDS_194A.equals(pjeDetail.getHeadType())
                                || IdosConstants.HEAD_TDS_194C1.equals(pjeDetail.getHeadType())
                                || IdosConstants.HEAD_TDS_194C2.equals(pjeDetail.getHeadType())
                                || IdosConstants.HEAD_TDS_194H.equals(pjeDetail.getHeadType())
                                || IdosConstants.HEAD_TDS_194I1.equals(pjeDetail.getHeadType())
                                || IdosConstants.HEAD_TDS_194I2.equals(pjeDetail.getHeadType())
                                || IdosConstants.HEAD_TDS_194J.equals(pjeDetail.getHeadType())) {
                            Branch branch = null;
                            if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
                                branch = newProvJournalEntry.getCreditBranch();
                            } else {
                                branch = newProvJournalEntry.getDebitBranch();
                            }
                            BranchTaxes branchTax = null;
                            Specifics txnSpecific = Specifics.findById(pjeDetail.getHeadID());
                            if (IdosConstants.HEAD_TDS_INPUT.equals(pjeDetail.getHeadType())) {
                                branchTax = new BranchTaxes();
                                branchTax.setBranch(branch);
                                branchTax.setOrganization(user.getOrganization());
                                branchTax.setId(pjeDetail.getHeadID());
                                branchTax.setTaxType((int) IdosConstants.INPUT_TDS);
                            } else {
                                branchTax = CREATE_TRIAL_BALANCE_DAO.getTdsType4ExpenseByMappedSpecific(user, em,
                                        txnSpecific, null, branch, newProvJournalEntry.getTransactionPurpose(),
                                        newProvJournalEntry.getTransactionDate());
                                branchTax.setId(pjeDetail.getHeadID());
                            }
                            saveTrialBalanceTds(newProvJournalEntry, pjeDetail, em, user, branchTax, txnSpecific);
                        } else if (IdosConstants.HEAD_PAYROLL_EXPENSE.equals(pjeDetail.getHeadType())
                                || IdosConstants.HEAD_PAYROLL_DEDUCTIONS.equals(pjeDetail.getHeadType())) {
                            TrialBalancePayrollItem tbPayroll = new TrialBalancePayrollItem();
                            tbPayroll.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
                            tbPayroll.setBranch(newProvJournalEntry.getDebitBranch());
                            tbPayroll.setDate(newProvJournalEntry.getTransactionDate());
                            tbPayroll.setPayrollItem(PayrollSetup.findById(pjeDetail.getHeadID()));
                            tbPayroll.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
                            tbPayroll.setTransactionId(newProvJournalEntry.getId());
                            tbPayroll.setUser(user);
                            if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
                                tbPayroll.setCreditAmount(pjeDetail.getHeadAmount());
                            } else {
                                tbPayroll.setDebitAmount(pjeDetail.getHeadAmount());
                            }
                            genericDao.saveOrUpdate(tbPayroll, user, em);
                        }
                    }
                    // ****************************TRIAL BALANCE ENDS***********************//
                }
            }
            if (!newProvJournalEntry.getTransactionStatus().equals("Require Clarification")) {
                if (selectedApproverAction.equals("5")) {
                    newProvJournalEntry.setTransactionStatus("Require Clarification");
                    newProvJournalEntry.setModifiedBy(user);
                    saveUpdateRemarksAndUploadedDocuments(suppDoc, txnRmarks, newProvJournalEntry, user);
                }
            }
            if (!newProvJournalEntry.getTransactionStatus().equals("Clarified")) {
                if (selectedApproverAction.equals("6")) {
                    newProvJournalEntry.setTransactionStatus("Clarified");
                    newProvJournalEntry.setModifiedBy(user);
                    saveUpdateRemarksAndUploadedDocuments(suppDoc, txnRmarks, newProvJournalEntry, user);
                }
            }
            if (selectedApproverAction.equals("7")) {
                newProvJournalEntry.setModifiedBy(user);
                saveUpdateRemarksAndUploadedDocuments(suppDoc, txnRmarks, newProvJournalEntry, user);
            }
            if (selectedApproverAction.equals("8")) {
                newProvJournalEntry.setModifiedBy(user);
                saveUpdateRemarksAndUploadedDocuments(suppDoc, txnRmarks, newProvJournalEntry, user);
            }
            entitytransaction.commit();

            // Single User
            if (isSingleUserDeploy && selectedApproverAction.equals("1")) {
                selectedApproverAction = "4"; // for Complete accounting
            } else {
                selectedApproverAction = "1"; // for break point
            }
        } while (isSingleUserDeploy && selectedApproverAction.equals("4"));
        return 1;
    }

    private void saveTrialBalanceTax(IdosProvisionJournalEntry newProvJournalEntry,
            ProvisionJournalEntryDetail pjeDetail, EntityManager em, Users user, BranchTaxes bnchTaxes) {
        TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
        trialBalTaxes.setTransactionId(newProvJournalEntry.getId());
        trialBalTaxes.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
        trialBalTaxes.setDate(newProvJournalEntry.getTransactionDate());
        trialBalTaxes.setBranchTaxes(bnchTaxes);
        trialBalTaxes.setTaxType(bnchTaxes.getTaxType());
        trialBalTaxes.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
        if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
            trialBalTaxes.setCreditAmount(pjeDetail.getHeadAmount());
            trialBalTaxes.setBranch(newProvJournalEntry.getCreditBranch());
        } else {
            trialBalTaxes.setDebitAmount(pjeDetail.getHeadAmount());
            trialBalTaxes.setBranch(newProvJournalEntry.getDebitBranch());
        }
        genericDao.saveOrUpdate(trialBalTaxes, user, em);
    }

    private void saveTrialBalanceTds(IdosProvisionJournalEntry newProvJournalEntry,
            ProvisionJournalEntryDetail pjeDetail, EntityManager em, Users user, BranchTaxes bnchTaxes,
            Specifics specifics) {
        TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
        trialBalTaxes.setTransactionId(newProvJournalEntry.getId());
        trialBalTaxes.setTransactionPurpose(newProvJournalEntry.getTransactionPurpose());
        trialBalTaxes.setDate(newProvJournalEntry.getTransactionDate());
        trialBalTaxes.setBranchTaxes(bnchTaxes);
        trialBalTaxes.setTaxType(bnchTaxes.getTaxType());
        trialBalTaxes.setOrganization(newProvJournalEntry.getProvisionMadeForOrganization());
        if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjeDetail.getIsDebit()) {
            trialBalTaxes.setCreditAmount(pjeDetail.getHeadAmount());
            trialBalTaxes.setBranch(newProvJournalEntry.getCreditBranch());
        } else {
            trialBalTaxes.setDebitAmount(pjeDetail.getHeadAmount());
            trialBalTaxes.setBranch(newProvJournalEntry.getDebitBranch());
        }
        trialBalTaxes.setTransactionSpecifics(specifics);
        trialBalTaxes.setTransactionParticulars(specifics.getParticularsId());
        genericDao.saveOrUpdate(trialBalTaxes, user, em);
    }

    private void saveUpdateRemarksAndUploadedDocuments(String suppDoc, String txnRmarks,
            IdosProvisionJournalEntry newProvJournalEntry, Users user) {
        if (newProvJournalEntry != null && suppDoc != null && !suppDoc.equals("")) {
            String txnDocument = "";
            String suppdocarr[] = suppDoc.split(",");
            for (int i = 0; i < suppdocarr.length; i++) {
                if (txnDocument.equals("")) {
                    txnDocument += user.getEmail() + "#" + suppdocarr[i];
                } else {
                    txnDocument += "," + user.getEmail() + "#" + suppdocarr[i];
                }
            }
            newProvJournalEntry.setSupportingDocuments(txnDocument);
        }

        if (txnRmarks != null && !txnRmarks.equals("")) {
            if (newProvJournalEntry.getTxnRemarks() != null) {
                newProvJournalEntry
                        .setTxnRemarks(newProvJournalEntry.getTxnRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
            } else {
                newProvJournalEntry.setTxnRemarks(user.getEmail() + "#" + txnRmarks);
            }
        }
    }

    @Override
    public void getProvisionJournalEntryList(Users user, String roles, ArrayNode recordsArrayNode, EntityManager em) {
        log.log(Level.FINE, ">>>> Start ");
        try {
            List<IdosProvisionJournalEntry> userProvisionTransactionList = null;
            StringBuilder sbquery = null;
            // if role is only of creator
            if (roles.equals("CREATOR")) {
                sbquery = new StringBuilder("select obj from IdosProvisionJournalEntry obj WHERE obj.createdBy ='"
                        + user.getId() + "' and obj.provisionMadeForOrganization='" + user.getOrganization().getId()
                        + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("APPROVER")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from IdosProvisionJournalEntry obj WHERE (obj.approverActionBy='"
                        + user.getId() + "' or LOCATE('" + user.getEmail()
                        + "',obj.approverEmails)>0 or obj.selectedAdditionalApprover='" + user.getEmail()
                        + "') and obj.provisionMadeForOrganization='" + user.getOrganization().getId()
                        + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("CREATOR,APPROVER")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from IdosProvisionJournalEntry obj WHERE (obj.createdBy ='" + user.getId()
                        + "' or obj.approverActionBy='" + user.getId() + "' or LOCATE('" + user.getEmail()
                        + "',obj.approverEmails)>0 or obj.selectedAdditionalApprover='" + user.getEmail()
                        + "') and obj.provisionMadeForOrganization='" + user.getOrganization().getId()
                        + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("CREATOR,ACCOUNTANT")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from IdosProvisionJournalEntry obj WHERE obj.provisionMadeForOrganization='"
                        + user.getOrganization().getId() + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("CREATOR,CASHIER")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from IdosProvisionJournalEntry obj WHERE obj.createdBy ='" + user.getId()
                        + "' and obj.provisionMadeForOrganization='" + user.getOrganization().getId()
                        + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("CREATOR,APPROVER,ACCOUNTANT")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from IdosProvisionJournalEntry obj WHERE obj.provisionMadeForOrganization='"
                        + user.getOrganization().getId() + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("CREATOR,APPROVER,CASHIER")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from IdosProvisionJournalEntry obj WHERE (obj.createdBy ='" + user.getId()
                        + "' or obj.approverActionBy='" + user.getId() + "' or LOCATE('" + user.getEmail()
                        + "',obj.approverEmails)>0 or obj.selectedAdditionalApprover='" + user.getEmail()
                        + "') and obj.provisionMadeForOrganization='" + user.getOrganization().getId()
                        + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("CREATOR,ACCOUNTANT,CASHIER")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from IdosProvisionJournalEntry obj WHERE obj.provisionMadeForOrganization='"
                        + user.getOrganization().getId() + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("CREATOR,APPROVER,ACCOUNTANT,CASHIER")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from IdosProvisionJournalEntry obj WHERE obj.provisionMadeForOrganization='"
                        + user.getOrganization().getId() + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("APPROVER,ACCOUNTANT")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from IdosProvisionJournalEntry obj WHERE obj.provisionMadeForOrganization='"
                        + user.getOrganization().getId() + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("APPROVER,CASHIER")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from IdosProvisionJournalEntry obj WHERE (obj.approverActionBy='"
                        + user.getId() + "' or LOCATE('" + user.getEmail()
                        + "',obj.approverEmails)>0 or obj.selectedAdditionalApprover='" + user.getEmail()
                        + "') and obj.provisionMadeForOrganization='" + user.getOrganization().getId()
                        + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("APPROVER,ACCOUNTANT,CASHIER") || roles.contains("CONTROLLER")
                    || roles.contains("ACCOUNTANT")) {
                sbquery = new StringBuilder(
                        "select obj from IdosProvisionJournalEntry obj WHERE obj.provisionMadeForOrganization='"
                                + user.getOrganization().getId()
                                + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }

            userProvisionTransactionList = genericDao.executeSimpleQuery(sbquery.toString(), em);
            for (IdosProvisionJournalEntry usrTxn : userProvisionTransactionList) {
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
                getProvisionJournalEntryDetail(em, usrTxn, itemParentName, creditItemsName, debitItemsName);
                event.put("itemName", IdosUtil.removeLastChar(debitItemsName.toString()) + "|"
                        + creditItemsName.deleteCharAt(creditItemsName.length() - 1).toString());
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
                recordsArrayNode.add(event);
                log.log(Level.FINE, ">>>> End " + event);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
    }

    @Override
    public void searchProvisionJournalEntry(Users user, String roles, ArrayNode recordsArrayNode, JsonNode json,
            EntityManager em) {
        log.log(Level.FINE, ">>>> Start");
        try {
            List<IdosProvisionJournalEntry> userProvisionTransactionList = null;
            StringBuilder sbquery = null;
            // if role is only of creator
            if (roles.equals("CREATOR")) {
                sbquery = new StringBuilder("select obj from IdosProvisionJournalEntry obj WHERE obj.createdBy ='"
                        + user.getId() + "' and obj.provisionMadeForOrganization='" + user.getOrganization().getId()
                        + "' and obj.presentStatus=1");
            }
            if (roles.equals("APPROVER")) {
                sbquery = new StringBuilder(
                        "select obj from IdosProvisionJournalEntry obj WHERE (obj.approverActionBy='" + user.getId()
                                + "' or LOCATE('" + user.getEmail()
                                + "',obj.approverEmails)>0 or obj.selectedAdditionalApprover='" + user.getEmail()
                                + "') and obj.provisionMadeForOrganization='" + user.getOrganization().getId()
                                + "' and obj.presentStatus=1");
            }
            if (roles.equals("CREATOR,APPROVER")) {
                sbquery = new StringBuilder("select obj from IdosProvisionJournalEntry obj WHERE (obj.createdBy ='"
                        + user.getId() + "' or obj.approverActionBy='" + user.getId() + "' or LOCATE('"
                        + user.getEmail() + "',obj.approverEmails)>0 or obj.selectedAdditionalApprover='"
                        + user.getEmail() + "') and obj.provisionMadeForOrganization='" + user.getOrganization().getId()
                        + "' and obj.presentStatus=1");
            }
            if (roles.equals("CREATOR,ACCOUNTANT")) {
                sbquery = new StringBuilder(
                        "select obj from IdosProvisionJournalEntry obj WHERE obj.provisionMadeForOrganization='"
                                + user.getOrganization().getId() + "' and obj.presentStatus=1");
            }
            if (roles.equals("CREATOR,CASHIER")) {
                sbquery = new StringBuilder("select obj from IdosProvisionJournalEntry obj WHERE obj.createdBy ='"
                        + user.getId() + "' and obj.provisionMadeForOrganization='" + user.getOrganization().getId()
                        + "' and obj.presentStatus=1");
            }
            if (roles.equals("CREATOR,APPROVER,ACCOUNTANT")) {
                sbquery = new StringBuilder(
                        "select obj from IdosProvisionJournalEntry obj WHERE obj.provisionMadeForOrganization='"
                                + user.getOrganization().getId() + "' and obj.presentStatus=1");
            }
            if (roles.equals("CREATOR,APPROVER,CASHIER")) {
                sbquery = new StringBuilder("select obj from IdosProvisionJournalEntry obj WHERE (obj.createdBy ='"
                        + user.getId() + "' or obj.approverActionBy='" + user.getId() + "' or LOCATE('"
                        + user.getEmail() + "',obj.approverEmails)>0 or obj.selectedAdditionalApprover='"
                        + user.getEmail() + "') and obj.provisionMadeForOrganization='" + user.getOrganization().getId()
                        + "' and obj.presentStatus=1");
            }
            if (roles.equals("CREATOR,ACCOUNTANT,CASHIER")) {
                sbquery = new StringBuilder(
                        "select obj from IdosProvisionJournalEntry obj WHERE obj.provisionMadeForOrganization='"
                                + user.getOrganization().getId() + "' and obj.presentStatus=1");
            }
            if (roles.equals("CREATOR,APPROVER,ACCOUNTANT,CASHIER")) {
                sbquery = new StringBuilder(
                        "select obj from IdosProvisionJournalEntry obj WHERE obj.provisionMadeForOrganization='"
                                + user.getOrganization().getId() + "' and obj.presentStatus=1");
            }
            if (roles.equals("APPROVER,ACCOUNTANT")) {
                sbquery = new StringBuilder(
                        "select obj from IdosProvisionJournalEntry obj WHERE obj.provisionMadeForOrganization='"
                                + user.getOrganization().getId() + "' and obj.presentStatus=1");
            }
            if (roles.equals("APPROVER,CASHIER")) {
                sbquery = new StringBuilder(
                        "select obj from IdosProvisionJournalEntry obj WHERE (obj.approverActionBy='" + user.getId()
                                + "' or LOCATE('" + user.getEmail()
                                + "',obj.approverEmails)>0 or obj.selectedAdditionalApprover='" + user.getEmail()
                                + "') and obj.provisionMadeForOrganization='" + user.getOrganization().getId()
                                + "' and obj.presentStatus=1");
            }
            if (roles.equals("APPROVER,ACCOUNTANT,CASHIER")) {
                sbquery = new StringBuilder(
                        "select obj from IdosProvisionJournalEntry obj WHERE obj.provisionMadeForOrganization='"
                                + user.getOrganization().getId() + "' and obj.presentStatus=1");
            }
            if (roles.contains("CONTROLLER")) {
                sbquery = new StringBuilder(
                        "select obj from IdosProvisionJournalEntry obj WHERE obj.provisionMadeForOrganization='"
                                + user.getOrganization().getId() + "' and obj.presentStatus=1");
            }
            if (roles.contains("ACCOUNTANT")) {
                sbquery = new StringBuilder(
                        "select obj from IdosProvisionJournalEntry obj WHERE obj.provisionMadeForOrganization='"
                                + user.getOrganization().getId() + "' and obj.presentStatus=1");
            }
            if (roles.contains("AUDITOR")) {
                sbquery = new StringBuilder(
                        "select obj from IdosProvisionJournalEntry obj WHERE obj.provisionMadeForOrganization='"
                                + user.getOrganization().getId()
                                + "' and obj.transactionStatus = 'Approved' and obj.presentStatus=1");
            }
            String searchCategory = json.findValue("searchCategory") != null ? json.findValue("searchCategory").asText()
                    : null;
            String searchTransactionRefNumber = json.findValue("searchTransactionRefNumber") != null
                    ? json.findValue("searchTransactionRefNumber").asText()
                    : null;
            String searchItems = json.findValue("searchItems") != null ? json.findValue("searchItems").asText() : null;
            String searchTxnStatus = json.findValue("searchTxnStatus") != null
                    ? json.findValue("searchTxnStatus").asText()
                    : null;
            String searchTxnFromDate = json.findValue("searchTxnFromDate") != null
                    ? json.findValue("searchTxnFromDate").asText()
                    : null;
            String searchTxnToDate = json.findValue("searchTxnToDate") != null
                    ? json.findValue("searchTxnToDate").asText()
                    : null;
            String searchTxnBranch = json.findValue("searchTxnBranch") != null
                    ? json.findValue("searchTxnBranch").asText()
                    : null;
            String searchTxnProjects = json.findValue("searchTxnProjects") != null
                    ? json.findValue("searchTxnProjects").asText()
                    : null;
            String searchVendors = json.findValue("searchVendors") != null ? json.findValue("searchVendors").asText()
                    : null;
            String searchCustomers = json.findValue("searchCustomers") != null
                    ? json.findValue("searchCustomers").asText()
                    : null;
            String searchTxnWithWithoutDoc = json.findValue("searchTxnWithWithoutDoc") != null
                    ? json.findValue("searchTxnWithWithoutDoc").asText()
                    : null;
            String searchTxnPyMode = json.findValue("searchTxnPyMode") != null
                    ? json.findValue("searchTxnPyMode").asText()
                    : null;
            String searchTxnWithWithoutRemarks = json.findValue("searchTxnWithWithoutRemarks") != null
                    ? json.findValue("searchTxnWithWithoutRemarks").asText()
                    : null;
            String searchTxnException = json.findValue("searchTxnException") != null
                    ? json.findValue("searchTxnException").asText()
                    : null;
            String searchAmountRanseLimitFrom = json.findValue("searchAmountRanseLimitFrom") != null
                    ? json.findValue("searchAmountRanseLimitFrom").asText()
                    : null;
            String searchAmountRanseLimitTo = json.findValue("searchAmountRanseLimitTo") != null
                    ? json.findValue("searchAmountRanseLimitTo").asText()
                    : null;
            /*
             * if(searchCategory!=null && !searchCategory.equals("")){
             * sbquery.append("and (obj.debitSpecifics.particularsId='"+Long.parseLong(
             * searchCategory)+"' or obj.creditSpecifics.particularsId='"+Long.parseLong(
             * searchCategory)+"')");
             * }
             * if(searchItems!=null && !searchItems.equals("")){
             * sbquery.append("and (obj.debitSpecifics='"+Long.parseLong(searchItems)
             * +"' or obj.creditSpecifics='"+Long.parseLong(searchItems)+"')");
             * }
             */
            if (searchTxnStatus != null && !searchTxnStatus.equals("")) {
                sbquery.append(" and obj.transactionStatus='" + searchTxnStatus + "'");
            }
            if (searchTransactionRefNumber != null && !searchTransactionRefNumber.equals("")) {
                sbquery.append(" and obj.transactionRefNumber='" + searchTransactionRefNumber + "'");
            }
            Date txnFmDate = null;
            Date txnToDt = null;
            String fmDt = null;
            String tDt = null;
            if (searchTxnFromDate != null && !searchTxnFromDate.equals("")) {
                txnFmDate = IdosConstants.mysqldf
                        .parse(IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(searchTxnFromDate)));
            }
            if (searchTxnToDate != null && !searchTxnToDate.equals("")) {
                txnToDt = IdosConstants.mysqldf
                        .parse(IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(searchTxnToDate)));
            }
            if (txnFmDate != null && txnToDt == null) {
                fmDt = "\'" + IdosConstants.mysqldf.format(txnFmDate) + "\'";
                sbquery.append(" and obj.transactionDate= ").append(fmDt);
            }
            if (txnFmDate == null && txnToDt != null) {
                tDt = "\'" + IdosConstants.mysqldf.format(txnToDt) + "\'";
                sbquery.append(" and obj.transactionDate= ").append(tDt);
            }
            if (txnFmDate != null && txnToDt != null) {
                fmDt = "\'" + IdosConstants.mysqldf.format(txnFmDate) + "\'";
                tDt = "\'" + IdosConstants.mysqldf.format(txnToDt) + "\'";
                sbquery.append(" and obj.transactionDate between ").append(fmDt).append(" and ").append(tDt);
            }
            if (searchTxnBranch != null && !searchTxnBranch.equals("")) {
                sbquery.append(" and obj.debitBranch='" + Long.parseLong(searchTxnBranch) + "'");
            }
            if (searchAmountRanseLimitFrom != null && searchAmountRanseLimitTo == null) {
                sbquery.append(" and obj.totalDebitAmount>='" + Double.parseDouble(searchAmountRanseLimitFrom) + "'");
            }
            if (searchAmountRanseLimitFrom == null && searchAmountRanseLimitTo != null) {
                sbquery.append(" and obj.totalDebitAmount<='" + Double.parseDouble(searchAmountRanseLimitTo) + "'");
            }
            if (searchAmountRanseLimitFrom != null && searchAmountRanseLimitTo != null) {
                sbquery.append(" and obj.totalDebitAmount>='" + Double.parseDouble(searchAmountRanseLimitFrom) + "'");
                sbquery.append(" and obj.totalDebitAmount<='" + Double.parseDouble(searchAmountRanseLimitTo) + "'");
            }
            if (searchTxnWithWithoutDoc != null && !searchTxnWithWithoutDoc.equals("")) {
                if (searchTxnWithWithoutDoc.equals("1")) {
                    sbquery.append(" and obj.supportingDocuments!=null");
                }
                if (searchTxnWithWithoutDoc.equals("0")) {
                    sbquery.append(" and obj.supportingDocuments=null");
                }
            }
            if (searchTxnWithWithoutRemarks != null && !searchTxnWithWithoutRemarks.equals("")) {
                if (searchTxnWithWithoutRemarks.equals("1")) {
                    sbquery.append(" and obj.txnRemarks!=null");
                }
                if (searchTxnWithWithoutRemarks.equals("0")) {
                    sbquery.append(" and obj.txnRemarks!=null");
                }
            }
            sbquery.append(" ORDER BY obj.createdAt desc");
            userProvisionTransactionList = genericDao.executeSimpleQueryWithLimit(sbquery.toString(), entityManager,
                    100);
            for (IdosProvisionJournalEntry usrTxn : userProvisionTransactionList) {
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
                getProvisionJournalEntryDetail(em, usrTxn, itemParentName, creditItemsName, debitItemsName);
                String invoiceDate = "";
                String invoiceDateLabel = "";
                if (usrTxn.getReversalDate() != null) {
                    invoiceDateLabel = "REVERSAL DATE:";
                    invoiceDate = IdosConstants.idosdf.format(usrTxn.getReversalDate());
                }
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
                event.put("invoiceDateLabel", invoiceDateLabel);
                event.put("invoiceDate", invoiceDate);
                event.put("paymentMode", "");
                event.put("noOfUnit", "");
                event.put("unitPrice", "");
                if (usrTxn.getTotalDebitAmount() != null) {
                    event.put("grossAmount", usrTxn.getTotalDebitAmount());
                } else {
                    event.put("grossAmount", "");
                }
                event.put("netAmount", usrTxn.getTotalDebitAmount());
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

                recordsArrayNode.add(event);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
    }

    @Override
    public List<ProvisionJournalEntryDetailPojo> getProvisionJournalEntryDetail(EntityManager em,
            IdosProvisionJournalEntry provisionJournalEntry, StringBuilder itemParentName, StringBuilder creditItems,
            StringBuilder debitItems) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>>>>>>START  getProvisionJournalEntryDetail: ");
        List<ProvisionJournalEntryDetail> pjEntryDetailList = provisionJournalEntry.getProvisionJournalEntryDetails();
        String itemList = null;
        // StringBuilder debitItems = new StringBuilder();
        List<ProvisionJournalEntryDetailPojo> pjeItemsDetailList = new ArrayList<ProvisionJournalEntryDetailPojo>();
        // HashMap<String,List<ProvisionJournalEntryDetailsPojo>> pjeDebitCreditItemList
        // = new HashMap<String,List<ProvisionJournalEntryDetailsPojo>>();
        Map<String, Object> criterias = new HashMap<String, Object>(2);
        for (ProvisionJournalEntryDetail pjEntryDetail : pjEntryDetailList) {
            ProvisionJournalEntryDetailPojo pjeItemDetail = new ProvisionJournalEntryDetailPojo();

            String headType = pjEntryDetail.getHeadType();
            if (IdosConstants.HEAD_SPECIFIC.equals(headType) || IdosConstants.HEAD_TDS_INPUT.equals(headType)
                    || IdosConstants.HEAD_TDS_192.equals(headType) || IdosConstants.HEAD_TDS_194A.equals(headType)
                    || IdosConstants.HEAD_TDS_194C1.equals(headType) || IdosConstants.HEAD_TDS_194C2.equals(headType)
                    || IdosConstants.HEAD_TDS_194H.equals(headType) || IdosConstants.HEAD_TDS_194I1.equals(headType)
                    || IdosConstants.HEAD_TDS_194I2.equals(headType) || IdosConstants.HEAD_TDS_194J.equals(headType)) {
                criterias.clear();
                criterias.put("id", pjEntryDetail.getHeadID());
                criterias.put("presentStatus", 1);
                List<Specifics> specificsList = genericDao.findByCriteria(Specifics.class, criterias, em);
                if (!specificsList.isEmpty()) {
                    Specifics specifics = specificsList.get(0);
                    if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjEntryDetail.getIsDebit()) {
                        // itemName+="Debit:"+ specifics.getName()+",";
                        debitItems.append(specifics.getName()).append(":").append(pjEntryDetail.getHeadAmount())
                                .append(", ");
                        pjeItemDetail.setIsDebit(IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT);
                        if (specifics.getParentSpecifics() != null && !specifics.getParentSpecifics().equals("")) {
                            itemParentName.append("Debit:").append(specifics.getParentSpecifics().getName())
                                    .append(",");
                        } else {
                            itemParentName.append("Debit:").append(specifics.getParticularsId().getName()).append(",");
                        }
                    } else if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjEntryDetail.getIsDebit()) {
                        // itemName += "Credit:" + specifics.getName();
                        creditItems.append(specifics.getName()).append(":").append(pjEntryDetail.getHeadAmount())
                                .append(", ");
                        pjeItemDetail.setIsDebit(IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT);
                        if (specifics.getParentSpecifics() != null && !specifics.getParentSpecifics().equals("")) {
                            // itemParentName += "Credit:" + specifics.getParentSpecifics().getName();
                            itemParentName.append("Credit:").append(specifics.getParentSpecifics().getName())
                                    .append(",");
                        } else {
                            // itemParentName += "Credit:" + specifics.getParticularsId().getName();
                            itemParentName.append("Credit:").append(specifics.getParticularsId().getName()).append(",");
                        }
                    }
                    pjeItemDetail.setItemName(specifics.getName());
                }
            } else if (IdosConstants.HEAD_VENDOR.equals(headType) || IdosConstants.HEAD_CUSTOMER.equals(headType)) {
                criterias.clear();
                criterias.put("id", pjEntryDetail.getHeadID());
                criterias.put("presentStatus", 1);
                List<Vendor> vendorList = genericDao.findByCriteria(Vendor.class, criterias, em);
                if (!vendorList.isEmpty()) {
                    Vendor vendor = vendorList.get(0);
                    if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjEntryDetail.getIsDebit()) {
                        debitItems.append(vendor.getName()).append(":").append(pjEntryDetail.getHeadAmount())
                                .append(", ");
                    } else if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjEntryDetail.getIsDebit()) {
                        creditItems.append(vendor.getName()).append(":").append(pjEntryDetail.getHeadAmount())
                                .append(", ");
                    }
                    pjeItemDetail.setItemName(vendor.getName());
                }
            } else if (IdosConstants.HEAD_VENDOR_ADV.equals(headType)
                    || IdosConstants.HEAD_CUSTOMER_ADV.equals(headType)) {
                criterias.clear();
                criterias.put("id", pjEntryDetail.getHeadID());
                criterias.put("presentStatus", 1);
                List<Vendor> vendorList = genericDao.findByCriteria(Vendor.class, criterias, em);
                if (!vendorList.isEmpty()) {
                    Vendor vendor = vendorList.get(0);
                    if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjEntryDetail.getIsDebit()) {
                        debitItems.append(vendor.getName()).append("_Adv:").append(pjEntryDetail.getHeadAmount())
                                .append(", ");
                    } else if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjEntryDetail.getIsDebit()) {
                        creditItems.append(vendor.getName()).append("_Adv:").append(pjEntryDetail.getHeadAmount())
                                .append(", ");
                    }
                    pjeItemDetail.setItemName(vendor.getName() + "_Adv");
                }
            } else if (IdosConstants.HEAD_CASH.equals(headType)) {
                criterias.clear();
                criterias.put("id", pjEntryDetail.getHeadID());
                criterias.put("presentStatus", 1);
                List<BranchDepositBoxKey> branchCashCountList = genericDao.findByCriteria(BranchDepositBoxKey.class,
                        criterias, em);
                if (!branchCashCountList.isEmpty()) {
                    BranchDepositBoxKey branchCashCount = branchCashCountList.get(0);
                    if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjEntryDetail.getIsDebit()) {
                        // itemName+="Debit:" + branchCashCount.getBranch().getName()+" Cash,";
                        debitItems.append(branchCashCount.getBranch().getName()).append(" Cash:")
                                .append(pjEntryDetail.getHeadAmount()).append(", ");
                    } else if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjEntryDetail.getIsDebit()) {
                        // itemName+="Credit:" + branchCashCount.getBranch().getName()+" Cash,";
                        creditItems.append(branchCashCount.getBranch().getName()).append(" Cash:")
                                .append(pjEntryDetail.getHeadAmount()).append(", ");
                    }
                    pjeItemDetail.setItemName(branchCashCount.getBranch().getName() + " Cash");
                }
            } else if (IdosConstants.HEAD_PETTY.equals(headType)) {
                criterias.clear();
                criterias.put("id", pjEntryDetail.getHeadID());
                criterias.put("presentStatus", 1);
                List<BranchDepositBoxKey> branchCashCountList = genericDao.findByCriteria(BranchDepositBoxKey.class,
                        criterias, em);
                if (!branchCashCountList.isEmpty()) {
                    BranchDepositBoxKey branchCashCount = branchCashCountList.get(0);
                    if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjEntryDetail.getIsDebit()) {
                        // itemName+="Debit:" + branchCashCount.getBranch().getName()+" Pettycash,";
                        debitItems.append(branchCashCount.getBranch().getName()).append(" Pettycash:")
                                .append(pjEntryDetail.getHeadAmount()).append(", ");
                    } else if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjEntryDetail.getIsDebit()) {
                        // itemName+="Credit:" + branchCashCount.getBranch().getName()+" Pettycash,";
                        creditItems.append(branchCashCount.getBranch().getName()).append(" Pettycash:")
                                .append(pjEntryDetail.getHeadAmount()).append(", ");
                    }
                    pjeItemDetail.setItemName(branchCashCount.getBranch().getName() + " Pettycash");
                }
            } else if (IdosConstants.HEAD_BANK.equals(headType)) {
                criterias.clear();
                criterias.put("id", pjEntryDetail.getHeadID());
                criterias.put("presentStatus", 1);
                List<BranchBankAccounts> branchBankAccountsList = genericDao.findByCriteria(BranchBankAccounts.class,
                        criterias, em);
                if (!branchBankAccountsList.isEmpty()) {
                    BranchBankAccounts branchBankAccounts = branchBankAccountsList.get(0);
                    if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjEntryDetail.getIsDebit()) {
                        // itemName+="Debit:" + branchBankAccounts.getBranch().getName() +
                        // branchBankAccounts.getBankName()+",";
                        debitItems.append(branchBankAccounts.getBranch().getName()).append("-")
                                .append(branchBankAccounts.getBankName()).append(":")
                                .append(pjEntryDetail.getHeadAmount()).append(", ");
                    } else if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjEntryDetail.getIsDebit()) {
                        // itemName+="Credit:" + branchBankAccounts.getBranch().getName() +
                        // branchBankAccounts.getBankName()+",";
                        creditItems.append(branchBankAccounts.getBranch().getName()).append("-")
                                .append(branchBankAccounts.getBankName()).append(":")
                                .append(pjEntryDetail.getHeadAmount()).append(", ");
                    }
                    pjeItemDetail.setItemName(
                            branchBankAccounts.getBranch().getName() + "-" + branchBankAccounts.getBankName());
                }
            } else if (IdosConstants.HEAD_TAXS.equals(headType) || IdosConstants.HEAD_SGST.equals(headType)
                    || IdosConstants.HEAD_CGST.equals(headType) || IdosConstants.HEAD_IGST.equals(headType)
                    || IdosConstants.HEAD_CESS.equals(headType) || IdosConstants.HEAD_RCM_CESS_IN.equals(headType)
                    || IdosConstants.HEAD_RCM_SGST_IN.equals(headType)
                    || IdosConstants.HEAD_RCM_CGST_IN.equals(headType)
                    || IdosConstants.HEAD_RCM_IGST_IN.equals(headType)
                    || IdosConstants.HEAD_RCM_CESS_OUTPUT.equals(headType)
                    || IdosConstants.HEAD_RCM_SGST_OUTPUT.equals(headType)
                    || IdosConstants.HEAD_RCM_CGST_OUTPUT.equals(headType)
                    || IdosConstants.HEAD_RCM_IGST_OUTPUT.equals(headType)) {
                criterias.clear();
                criterias.put("id", pjEntryDetail.getHeadID());
                List<BranchTaxes> branchTaxesList = genericDao.findByCriteria(BranchTaxes.class, criterias, em);
                String itemName = null;
                BranchTaxes branchTaxes = null;
                if (!branchTaxesList.isEmpty()) {
                    branchTaxes = branchTaxesList.get(0);
                }
                if (branchTaxes != null) {
                    if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjEntryDetail.getIsDebit()) {
                        itemName = branchTaxes.getTaxName();
                        debitItems.append(itemName).append(":").append(pjEntryDetail.getHeadAmount()).append(", ");
                    } else if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjEntryDetail.getIsDebit()) {
                        itemName = branchTaxes.getTaxName();
                        creditItems.append(itemName).append(":").append(pjEntryDetail.getHeadAmount()).append(", ");
                    }
                    pjeItemDetail.setItemName(itemName);
                }
            } else if (IdosConstants.HEAD_USER.equals(headType)) {
                criterias.clear();
                criterias.put("id", pjEntryDetail.getHeadID());
                criterias.put("presentStatus", 1);
                List<Users> usersList = genericDao.findByCriteria(Users.class, criterias, em);
                if (!usersList.isEmpty()) {
                    Users user = usersList.get(0);
                    if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjEntryDetail.getIsDebit()) {
                        // itemName+="Debit:" + branchTaxes.getTaxName()+",";
                        debitItems.append(user.getFullName()).append(":").append(pjEntryDetail.getHeadAmount())
                                .append(", ");
                    } else if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjEntryDetail.getIsDebit()) {
                        // itemName+="Credit:" + branchTaxes.getTaxName()+",";
                        creditItems.append(user.getFullName()).append(":").append(pjEntryDetail.getHeadAmount())
                                .append(", ");
                    }
                    pjeItemDetail.setItemName(user.getFullName());
                }
            } else if (IdosConstants.HEAD_INTR_BRANCH.equals(headType)) {
                criterias.clear();
                criterias.put("id", pjEntryDetail.getHeadID());
                criterias.put("presentStatus", 1);
                List<Branch> branchList = genericDao.findByCriteria(Branch.class, criterias, em);
                criterias.clear();
                criterias.put("id", pjEntryDetail.getHeadID2());
                criterias.put("presentStatus", 1);
                List<Branch> branchList2 = genericDao.findByCriteria(Branch.class, criterias, em);
                if (!branchList.isEmpty() && !branchList2.isEmpty()) {
                    Branch branch = branchList.get(0);
                    Branch branch2 = branchList2.get(0);
                    if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjEntryDetail.getIsDebit()) {
                        debitItems.append(branch.getName()).append("-").append(branch2.getName()).append(":")
                                .append(pjEntryDetail.getHeadAmount()).append(", ");
                    } else if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjEntryDetail.getIsDebit()) {
                        creditItems.append(branch.getName()).append("-").append(branch2.getName()).append(":")
                                .append(pjEntryDetail.getHeadAmount()).append(", ");
                    }
                    pjeItemDetail.setItemName(branch.getName() + "-" + branch2.getName());
                }
            }

            if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjEntryDetail.getIsDebit()) {
                pjeItemDetail.setIsDebit(IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT);
            } else if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjEntryDetail.getIsDebit()) {
                pjeItemDetail.setIsDebit(IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT);
            }

            if (pjEntryDetail.getUnits() != null) {
                if (pjEntryDetail.getUnits() > 0)
                    pjeItemDetail.setUnits(pjEntryDetail.getUnits());
            }

            if (pjEntryDetail.getUnitPrice() != null) {
                if (pjEntryDetail.getUnitPrice() > 0)
                    pjeItemDetail.setUnitPrice(pjEntryDetail.getUnitPrice());
            }

            if (pjEntryDetail.getHeadAmount() != null) {
                if (pjEntryDetail.getHeadAmount() > 0)
                    pjeItemDetail.setHeadAmount(pjEntryDetail.getHeadAmount());
            }

            if (pjEntryDetail.getProject() != null)
                pjeItemDetail.setProjectName(pjEntryDetail.getProject().getName());

            pjeItemsDetailList.add(pjeItemDetail);
        }
        itemList = debitItems.toString();
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>>>>>>End  getProvisionJournalEntryDetail: " + itemList);
        return pjeItemsDetailList;
    }

    public ObjectNode getDashboardProvisionEntriesDataBranchWise(String startDate, String endDate,
            Map<String, Double> totalProvJourEntForExpIncMap, Map<String, Double> allSpecifcsAmtData,
            Map<String, Double> vendorPayablesData, Map<String, Double> custReceivablesData, Users user,
            EntityManager em) {
        ObjectNode result = Json.newObject();
        // ArrayNode antotal = result.putArray("totalProvJourEntForExpInc");
        ArrayNode an = result.putArray("branchWiseProvJourEntForExpInc");
        Map criterias = new HashMap();
        criterias.clear();
        criterias.put("presentStatus", 1);
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("presentStatus", 1);
        List<Branch> availableBranches = genericDao.findByCriteria(Branch.class, criterias, em);
        double totalCashIncome = 0.0, totalCreditIncome = 0.0;
        double totalCashExpense = 0.0, totalCreditExpense = 0.0, totalCustReceivables = 0.0, totalVendPayables = 0.0;
        for (Branch branch : availableBranches) {
            ObjectNode row = Json.newObject();
            row.put("branchId", branch.getId());
            row.put("branchName", branch.getName());
            Map debitBranchMap = debitBranchData(startDate, endDate, user, allSpecifcsAmtData, vendorPayablesData,
                    custReceivablesData, em, branch);
            Map creditBranchMap = creditBranchData(startDate, endDate, user, allSpecifcsAmtData, vendorPayablesData,
                    custReceivablesData, em, branch);
            // For debit branch, cash/credit INCOME is -ve
            double totalBranchCashIncome = new Double(creditBranchMap.get("totalCreditBranchCashIncome").toString())
                    - new Double(debitBranchMap.get("totalDebitBranchCashIncome").toString());
            double totalBranchCreditIncome = new Double(creditBranchMap.get("totalCreditBranchCreditIncome").toString())
                    - new Double(debitBranchMap.get("totalDebitBranchCreditIncome").toString());
            // For credit branch, cash/credit Expense is +ve
            double totalBranchCashExpense = new Double(debitBranchMap.get("totalDebitBranchCashExpense").toString())
                    - new Double(creditBranchMap.get("totalCreditBranchCashExpense").toString());
            double totalBranchCreditExpense = new Double(debitBranchMap.get("totalDebitBranchCreditExpense").toString())
                    - new Double(creditBranchMap.get("totalCreditBranchCreditExpense").toString());
            // receivables & payables
            double totalBranchCustReceivables = new Double(
                    debitBranchMap.get("totalDebitBranchCustReceivables").toString())
                    - new Double(creditBranchMap.get("totalCreditBranchCustReceivables").toString());
            double totalBranchVendPayables = new Double(creditBranchMap.get("totalCreditBranchVendPayables").toString())
                    - new Double(debitBranchMap.get("totalDebitBranchVendPayables").toString());
            row.put("totalBranchCustReceivables", totalBranchCustReceivables);
            row.put("totalBranchVendPayables", totalBranchVendPayables);
            row.put("totalBranchCashIncome", totalBranchCashIncome);
            row.put("totalBranchCreditIncome", totalBranchCreditIncome);
            row.put("totalBranchCashExpense", totalBranchCashExpense); // for expense signs are opposite to income,
                                                                       // credit expense -ve, debit is +ve
            row.put("totalBranchCreditExpense", totalBranchCreditExpense);
            an.add(row);
            totalCustReceivables = totalCustReceivables + totalBranchCustReceivables;
            totalVendPayables = totalVendPayables + totalBranchVendPayables;
            totalCashIncome = totalCashIncome + totalBranchCashIncome;
            totalCreditIncome = totalCreditIncome + totalBranchCreditIncome;
            totalCashExpense = totalCashExpense + totalBranchCashExpense;
            totalCreditExpense = totalCreditExpense + totalBranchCreditExpense;
        }
        // ObjectNode rowTotal = Json.newObject();
        totalProvJourEntForExpIncMap.put("totalCustReceivables", totalCustReceivables);
        totalProvJourEntForExpIncMap.put("totalVendPayables", totalVendPayables);
        totalProvJourEntForExpIncMap.put("totalCashIncome", totalCashIncome);
        totalProvJourEntForExpIncMap.put("totalCreditIncome", totalCreditIncome);
        totalProvJourEntForExpIncMap.put("totalCashExpense", totalCashExpense);
        totalProvJourEntForExpIncMap.put("totalCreditExpense", totalCreditExpense);
        // antotal.add(rowTotal);
        result.put("result", true);
        return result;
    }

    public Map<String, Double> getDashboardProvisionEntriesDataForBranch(String startDate, String endDate, Users user,
            Map<String, Double> allSpecifcsAmtData, Map<String, Double> vendorPayablesData,
            Map<String, Double> custReceivablesData, Branch branch, EntityManager em) {
        Map<String, Double> branchMap = new HashMap<String, Double>();
        Map debitBranchMap = debitBranchData(startDate, endDate, user, allSpecifcsAmtData, vendorPayablesData,
                custReceivablesData, em, branch);
        Map creditBranchMap = creditBranchData(startDate, endDate, user, allSpecifcsAmtData, vendorPayablesData,
                custReceivablesData, em, branch);
        // For debit branch, cash/credit INCOME is -ve
        double totalBranchCashIncome = new Double(creditBranchMap.get("totalCreditBranchCashIncome").toString())
                - new Double(debitBranchMap.get("totalDebitBranchCashIncome").toString());
        double totalBranchCreditIncome = new Double(creditBranchMap.get("totalCreditBranchCreditIncome").toString())
                - new Double(debitBranchMap.get("totalDebitBranchCreditIncome").toString());
        // For credit branch, cash/credit Expense is +ve
        double totalBranchCashExpense = new Double(debitBranchMap.get("totalDebitBranchCashExpense").toString())
                - new Double(creditBranchMap.get("totalCreditBranchCashExpense").toString());
        double totalBranchCreditExpense = new Double(debitBranchMap.get("totalDebitBranchCreditExpense").toString())
                - new Double(creditBranchMap.get("totalCreditBranchCreditExpense").toString());
        // receivables & payables
        double totalBranchCustReceivables = new Double(debitBranchMap.get("totalDebitBranchCustReceivables").toString())
                - new Double(creditBranchMap.get("totalCreditBranchCustReceivables").toString());
        double totalBranchVendPayables = new Double(creditBranchMap.get("totalCreditBranchVendPayables").toString())
                - new Double(debitBranchMap.get("totalDebitBranchVendPayables").toString());
        branchMap.put("totalBranchCustReceivables", totalBranchCustReceivables);
        branchMap.put("totalBranchVendPayables", totalBranchVendPayables);
        branchMap.put("totalBranchCashIncome", totalBranchCashIncome);
        branchMap.put("totalBranchCreditIncome", totalBranchCreditIncome);
        branchMap.put("totalBranchCashExpense", totalBranchCashExpense); // for expense signs are opposite to income,
                                                                         // credit expense -ve, debit is +ve
        branchMap.put("totalBranchCreditExpense", totalBranchCreditExpense);
        return branchMap;
    }

    private Map<String, Double> debitBranchData(String startDate, String endDate, Users user,
            Map<String, Double> allSpecifcsAmtData, Map<String, Double> vendorPayablesData,
            Map<String, Double> custReceivablesData, EntityManager em, Branch branch) {
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, ">>>>>> Start " + branch + " " + startDate + " " + endDate);
        }
        StringBuilder sbquery = new StringBuilder("");
        Map<String, Object> criterias = new HashMap<String, Object>();
        Map<String, Double> debitBranchData = new HashMap<String, Double>();
        double txnAmount = 0.0, totalBranchCashIncome = 0.0, totalBranchCreditIncome = 0.0;
        double totalBranchCashExpense = 0.0, totalBranchCreditExpense = 0.0;
        double totalBranchCustReceivables = 0.0, totalBranchVendPayables = 0.0;
        // For this debit branch(say mumbai)
        sbquery.append("select obj from IdosProvisionJournalEntry obj WHERE obj.provisionMadeForOrganization='"
                + user.getOrganization().getId() + "' and obj.transactionStatus='Accounted' and obj.debitBranch='"
                + branch.getId() + "' and obj.presentStatus=1 and obj.transactionDate  between '" + startDate
                + "' and '" + endDate + "'");
        List<IdosProvisionJournalEntry> userProvisionTransactionList = genericDao
                .executeSimpleQueryWithLimit(sbquery.toString(), em, 100);
        for (IdosProvisionJournalEntry journalEntTrx : userProvisionTransactionList) {
            List<ProvisionJournalEntryDetail> pjEntryDetailList = journalEntTrx.getProvisionJournalEntryDetails();
            boolean isCashBank = false;
            short isIncomeExpItem = 0; // 0=no income/exp item, 1=income item, 2=exp item
            Specifics specifics = null;
            for (ProvisionJournalEntryDetail pjEntryDetail : pjEntryDetailList) {
                // credit tran is cash or bank for say Mumbai branch, so whatever is debit
                // transaction take it as Cash Income/Expense depending on specifics parent id
                if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjEntryDetail.getIsDebit()
                        && (IdosConstants.HEAD_CASH.equals(pjEntryDetail.getHeadType())
                                || IdosConstants.HEAD_BANK.equals(pjEntryDetail.getHeadType()))) {
                    isCashBank = true;
                    break;
                }
            }
            for (ProvisionJournalEntryDetail pjEntryDetail : pjEntryDetailList) {
                if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjEntryDetail.getIsDebit()) {
                    if (IdosConstants.HEAD_SPECIFIC.equals(pjEntryDetail.getHeadType())) {
                        criterias.clear();
                        criterias.put("id", pjEntryDetail.getHeadID());
                        criterias.put("presentStatus", 1);
                        List<Specifics> specificsList = genericDao.findByCriteria(Specifics.class, criterias, em);
                        if (!specificsList.isEmpty()) {
                            specifics = specificsList.get(0);
                            txnAmount = pjEntryDetail.getHeadAmount();
                            if (specifics.getAccountCodeHirarchy().startsWith("/1000000000000000000/")) {// income
                                isIncomeExpItem = 1;
                            } else if (specifics.getAccountCodeHirarchy().startsWith("/2000000000000000000/")) {
                                isIncomeExpItem = 2;
                            }
                        }
                        String key = null;
                        double specificsAmt = txnAmount;
                        if (isIncomeExpItem == 1) {// income item
                            if (isCashBank == true) {
                                totalBranchCashIncome = totalBranchCashIncome + txnAmount;
                                key = specifics.getId() + "CashIncome";
                            } else {
                                totalBranchCreditIncome = totalBranchCreditIncome + txnAmount;
                                key = specifics.getId() + "CreditIncome";
                            }
                            if (allSpecifcsAmtData.containsKey(key)) {
                                specificsAmt = new Double(allSpecifcsAmtData.get(key).toString()) - specificsAmt;
                            }
                            allSpecifcsAmtData.put(key, specificsAmt);
                        } else if (isIncomeExpItem == 2) {// expense item
                            if (isCashBank == true) {
                                totalBranchCashExpense = totalBranchCashExpense + txnAmount;
                                // totalBranchCashExpense =totalBranchCashExpense+txnAmount;
                                key = specifics.getId() + "CashExpense";

                            } else {
                                totalBranchCreditExpense = totalBranchCreditExpense + txnAmount;
                                key = specifics.getId() + "CreditExpense";
                            }
                            if (allSpecifcsAmtData.containsKey(key)) {
                                specificsAmt = specificsAmt + new Double(allSpecifcsAmtData.get(key).toString());
                            }
                            allSpecifcsAmtData.put(key, specificsAmt);
                        }
                    } else if (IdosConstants.HEAD_CUSTOMER.equals(pjEntryDetail.getHeadType())) {
                        criterias.clear();
                        criterias.put("id", pjEntryDetail.getHeadID());
                        criterias.put("presentStatus", 1);
                        List<Vendor> custList = genericDao.findByCriteria(Vendor.class, criterias, em);
                        if (!custList.isEmpty()) {
                            Vendor cust = custList.get(0);
                            double receAmount = pjEntryDetail.getHeadAmount();
                            String key = cust.getId().toString();
                            totalBranchCustReceivables = totalBranchCustReceivables + receAmount;
                            if (custReceivablesData.containsKey(key)) {
                                receAmount = receAmount + new Double(custReceivablesData.get(key).toString());
                            }
                            custReceivablesData.put(key, receAmount);
                        }
                    } else if (IdosConstants.HEAD_VENDOR.equals(pjEntryDetail.getHeadType())) {
                        criterias.clear();
                        criterias.put("id", pjEntryDetail.getHeadID());
                        criterias.put("presentStatus", 1);
                        List<Vendor> vendorList = genericDao.findByCriteria(Vendor.class, criterias, em);
                        if (!vendorList.isEmpty()) {
                            Vendor vendor = vendorList.get(0);
                            double payaAmount = pjEntryDetail.getHeadAmount();
                            String key = vendor.getId().toString();
                            totalBranchVendPayables = totalBranchVendPayables + payaAmount;
                            if (vendorPayablesData.containsKey(key)) {
                                payaAmount = payaAmount + new Double(vendorPayablesData.get(key).toString());
                            }
                            vendorPayablesData.put(key, payaAmount);
                        }
                    }
                }
            }
        }
        debitBranchData.put("totalDebitBranchCustReceivables", totalBranchCustReceivables);
        debitBranchData.put("totalDebitBranchVendPayables", totalBranchVendPayables);
        debitBranchData.put("totalDebitBranchCashIncome", totalBranchCashIncome);
        debitBranchData.put("totalDebitBranchCreditIncome", totalBranchCreditIncome);
        debitBranchData.put("totalDebitBranchCashExpense", totalBranchCashExpense); // for expense signs are opposite to
                                                                                    // income, credit expense -ve, debit
                                                                                    // is +ve
        debitBranchData.put("totalDebitBranchCreditExpense", totalBranchCreditExpense);
        return debitBranchData;
    }

    private Map<String, Double> creditBranchData(String startDate, String endDate, Users user,
            Map<String, Double> allSpecifcsAmtData, Map<String, Double> vendorPayablesData,
            Map<String, Double> custReceivablesData, EntityManager em, Branch branch) {
        StringBuilder sbquery = new StringBuilder("");
        Map<String, Object> criterias = new HashMap<String, Object>();
        Map<String, Double> creditBranchData = new HashMap<String, Double>();
        double txnAmount = 0.0, totalBranchCashIncome = 0.0, totalBranchCreditIncome = 0.0;
        double totalBranchCashExpense = 0.0, totalBranchCreditExpense = 0.0;
        double totalBranchCustReceivables = 0.0, totalBranchVendPayables = 0.0;
        // For this debit branch(say mumbai)
        sbquery.append("select obj from IdosProvisionJournalEntry obj WHERE obj.provisionMadeForOrganization='"
                + user.getOrganization().getId() + "' and obj.transactionStatus='Accounted' and obj.creditBranch='"
                + branch.getId() + "' and obj.presentStatus=1 and obj.transactionDate  between '" + startDate
                + "' and '" + endDate + "'");
        List<IdosProvisionJournalEntry> userProvisionTransactionList = genericDao
                .executeSimpleQueryWithLimit(sbquery.toString(), em, 100);
        for (IdosProvisionJournalEntry journalEntTrx : userProvisionTransactionList) {
            List<ProvisionJournalEntryDetail> pjEntryDetailList = journalEntTrx.getProvisionJournalEntryDetails();
            boolean isCashBank = false;
            short isIncomeExpItem = 0; // 0=no income/exp item, 1=income item, 2=exp item
            Specifics specifics = null;
            for (ProvisionJournalEntryDetail pjEntryDetail : pjEntryDetailList) {
                // debit tran is cash or bank for say Mumbai branch, so whatever is debit
                // transaction take it as Cash Income/Expense depending on specifics parent id
                if (IdosConstants.PROVISION_JOURNAL_ENTRY_DEBIT == pjEntryDetail.getIsDebit()
                        && (IdosConstants.HEAD_CASH.equals(pjEntryDetail.getHeadType())
                                || IdosConstants.HEAD_BANK.equals(pjEntryDetail.getHeadType()))) {
                    isCashBank = true;
                    break;
                }
            }
            for (ProvisionJournalEntryDetail pjEntryDetail : pjEntryDetailList) {
                if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == pjEntryDetail.getIsDebit()) {
                    if (IdosConstants.HEAD_SPECIFIC.equals(pjEntryDetail.getHeadType())) {
                        criterias.clear();
                        criterias.put("id", pjEntryDetail.getHeadID());
                        criterias.put("presentStatus", 1);
                        List<Specifics> specificsList = genericDao.findByCriteria(Specifics.class, criterias, em);
                        if (!specificsList.isEmpty()) {
                            specifics = specificsList.get(0);
                            txnAmount = pjEntryDetail.getHeadAmount();
                            if (specifics.getAccountCodeHirarchy().startsWith("/1000000000000000000/")) {// income
                                isIncomeExpItem = 1;
                            } else if (specifics.getAccountCodeHirarchy().startsWith("/2000000000000000000/")) {
                                isIncomeExpItem = 2;
                            }
                        }
                        String key = null;
                        double specificsAmt = txnAmount;
                        if (isIncomeExpItem == 1) {// income item
                            if (isCashBank == true) {
                                totalBranchCashIncome = totalBranchCashIncome + txnAmount;
                                key = specifics.getId() + "CashIncome";
                            } else {
                                totalBranchCreditIncome = totalBranchCreditIncome + txnAmount;
                                key = specifics.getId() + "CreditIncome";
                            }
                            if (allSpecifcsAmtData.containsKey(key)) {
                                specificsAmt = new Double(allSpecifcsAmtData.get(key).toString()) + specificsAmt;
                            }
                            allSpecifcsAmtData.put(key, specificsAmt);
                        } else if (isIncomeExpItem == 2) {// expense item
                            if (isCashBank == true) {
                                totalBranchCashExpense = totalBranchCashExpense + txnAmount;
                                key = specifics.getId() + "CashExpense";
                            } else {
                                totalBranchCreditExpense = totalBranchCreditExpense + txnAmount;
                                key = specifics.getId() + "CreditExpense";
                            }
                            if (allSpecifcsAmtData.containsKey(key)) {
                                specificsAmt = new Double(allSpecifcsAmtData.get(key).toString()) - specificsAmt;
                            }
                            allSpecifcsAmtData.put(key, specificsAmt);
                        }
                    } else if (IdosConstants.HEAD_CUSTOMER.equals(pjEntryDetail.getHeadType())) {
                        criterias.clear();
                        criterias.put("id", pjEntryDetail.getHeadID());
                        criterias.put("presentStatus", 1);
                        List<Vendor> custList = genericDao.findByCriteria(Vendor.class, criterias, em);
                        if (!custList.isEmpty()) {
                            Vendor cust = custList.get(0);
                            totalBranchCustReceivables = totalBranchCustReceivables + pjEntryDetail.getHeadAmount();
                            custReceivablesData.put(cust.getId().toString(), pjEntryDetail.getHeadAmount());
                        }
                    } else if (IdosConstants.HEAD_VENDOR.equals(pjEntryDetail.getHeadType())) {
                        criterias.clear();
                        criterias.put("id", pjEntryDetail.getHeadID());
                        criterias.put("presentStatus", 1);
                        List<Vendor> vendorList = genericDao.findByCriteria(Vendor.class, criterias, em);
                        if (!vendorList.isEmpty()) {
                            Vendor vendor = vendorList.get(0);
                            totalBranchVendPayables = totalBranchVendPayables + pjEntryDetail.getHeadAmount();
                            vendorPayablesData.put(vendor.getId().toString(), pjEntryDetail.getHeadAmount());
                        }
                    }
                }
            }
        }
        creditBranchData.put("totalCreditBranchCustReceivables", totalBranchCustReceivables);
        creditBranchData.put("totalCreditBranchVendPayables", totalBranchVendPayables);
        creditBranchData.put("totalCreditBranchCashIncome", totalBranchCashIncome);
        creditBranchData.put("totalCreditBranchCreditIncome", totalBranchCreditIncome);
        creditBranchData.put("totalCreditBranchCashExpense", totalBranchCashExpense); // for expense signs are opposite
                                                                                      // to income, credit expense -ve,
                                                                                      // debit is +ve
        creditBranchData.put("totalCreditBranchCreditExpense", totalBranchCreditExpense);
        return creditBranchData;
    }

    public List getDetailProvisionEntriesForCustVen(EntityManager em, String startDate, String endDate, Users user,
            Branch branch, Vendor vend, String vendorType) {
        StringBuilder sbquery = new StringBuilder("");
        sbquery.append("select obj from ProvisionJournalEntryDetail obj WHERE obj.organization.id='"
                + user.getOrganization().getId() + "' and obj.headType='" + vendorType + "' and obj.headID='"
                + vend.getId() + "' and obj.branch.id='" + branch.getId()
                + "' and obj.presentStatus=1 and obj.createdAt  between '" + startDate + "' and '" + endDate + "'");
        List<ProvisionJournalEntryDetail> userProvisionDetailTransactionList = genericDao
                .executeSimpleQueryWithLimit(sbquery.toString(), em, 100);
        return userProvisionDetailTransactionList;
    }

    @Override
    public IdosProvisionJournalEntry findByReferenceNumber(String referenceNumber, EntityManager entityManager) {
        IdosProvisionJournalEntry t = (IdosProvisionJournalEntry) entityManager
                .createQuery("SELECT t FROM IdosProvisionJournalEntry t WHERE t.transactionRefNumber =:referenceNumber")
                .setParameter("referenceNumber", referenceNumber).getSingleResult();
        return t;
    }
}
