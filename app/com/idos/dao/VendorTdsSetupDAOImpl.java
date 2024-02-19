package com.idos.dao;

import java.text.ParseException;
import java.util.*;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.logging.Level;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;

import com.idos.util.DateUtil;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import play.libs.Json;
import pojo.WithHoldingTaxPojo;

/**
 * Created by Ankush A. Sapkal
 */

public class VendorTdsSetupDAOImpl implements VendorTdsSetupDAO {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    @Override
    public boolean saveVendorBasicTds(JsonNode json, Users user, EntityManager entityManager) throws IDOSException {
        try {
            String fromDate = json.findValue("fromDate") == null ? null : json.findValue("fromDate").asText();
            String toDate = json.findValue("fromDate") == null ? null : json.findValue("toDate").asText();
            String tdsBasicRowData = json.findValue("tdsBasicRowData").toString();
            Date fromDateObj = null;
            Date toDateObj = null;
            if (fromDate != null && !fromDate.equals("")) {
                fromDateObj = IdosConstants.IDOSDF.parse(fromDate);
            }
            if (toDate != null && !toDate.equals("")) {
                toDateObj = IdosConstants.IDOSDF.parse(toDate);
            }
            JSONArray arrJSON = new JSONArray(tdsBasicRowData);
            if (arrJSON.length() > 0) {
                for (int i = 0; i < arrJSON.length(); i++) {
                    JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                    Long itemId = rowItemData.get("tdsItem") == null || "".equals(rowItemData.getString("tdsItem"))
                            ? null
                            : rowItemData.getLong("tdsItem");
                    String tdsVendors = rowItemData.get("tdsVendors") == null
                            || "".equals(rowItemData.getString("tdsVendors")) ? null
                                    : rowItemData.getString("tdsVendors");
                    Long tdsWHSection = rowItemData.get("tdsWHSection") == null
                            || "".equals(rowItemData.getString("tdsWHSection")) ? null
                                    : rowItemData.getLong("tdsWHSection");
                    Integer tdsMode = rowItemData.get("tdsMode") == null || "".equals(rowItemData.getString("tdsMode"))
                            ? null
                            : rowItemData.getInt("tdsMode");
                    Double tdsTaxRate = rowItemData.get("tdsTaxRate") == null
                            || "".equals(rowItemData.getString("tdsTaxRate")) ? null
                                    : rowItemData.getDouble("tdsTaxRate");
                    Double tdsTransLimit = rowItemData.get("tdsTransLimit") == null
                            || "".equals(rowItemData.getString("tdsTransLimit")) ? null
                                    : rowItemData.getDouble("tdsTransLimit");
                    Integer tdsOverAllLimitApply = rowItemData.get("tdsOverAllLimitApply") == null
                            || "".equals(rowItemData.getString("tdsOverAllLimitApply")) ? null
                                    : rowItemData.getInt("tdsOverAllLimitApply");
                    Double tdsOverAllLimit = rowItemData.get("tdsOverAllLimit") == null
                            || "".equals(rowItemData.getString("tdsOverAllLimit")) ? null
                                    : rowItemData.getDouble("tdsOverAllLimit");
                    String tdsUploads = rowItemData.get("tdsUploads") == null
                            || "".equals(rowItemData.getString("tdsUploads")) ? null
                                    : rowItemData.getString("tdsUploads");

                    Specifics specs = null;
                    VendorTDSTaxes vendTdsTax = new VendorTDSTaxes();
                    vendTdsTax.setOrganization(user.getOrganization());
                    if (itemId != null) {
                        vendTdsTax.setSpecifics(itemId);
                    }
                    if (tdsVendors != null) {
                        vendTdsTax.setVendors(tdsVendors);
                    }
                    if (tdsWHSection != null) {

                        WithholdingTypeDetails whType = WithholdingTypeDetails.findById(entityManager, tdsWHSection);
                        vendTdsTax.setTdsSection(whType);

                    }
                    if ((fromDateObj != null) && (toDateObj != null)) {
                        if (toDateObj.after(fromDateObj) || toDateObj.equals(fromDateObj)) {
                            vendTdsTax.setFromDate(fromDateObj);
                            vendTdsTax.setToDate(toDateObj);
                        } else {
                            throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
                                    IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
                                    "To date cannot be earlier than From date. ");
                        }
                    }
                    if (tdsMode != null) {
                        vendTdsTax.setModeOfComputation(tdsMode);
                    }
                    if (tdsTaxRate != null) {
                        vendTdsTax.setTaxRate(tdsTaxRate);
                    }
                    if (tdsTransLimit != null && tdsOverAllLimit != null) {
                        if (tdsOverAllLimit >= tdsTransLimit) {
                            vendTdsTax.setTransLimit(tdsTransLimit);
                            vendTdsTax.setOverAllLimit(tdsOverAllLimit);
                        } else {
                            throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
                                    IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
                                    "Transaction Limit cannot exceed Overall Limit. ");
                        }
                    } else if (tdsTransLimit != null) {
                        vendTdsTax.setTransLimit(tdsTransLimit);
                    } else if (tdsOverAllLimit != null) {
                        vendTdsTax.setOverAllLimit(tdsOverAllLimit);
                    }
                    /*
                     * if (tdsTransLimit != null) {
                     * vendTdsTax.setTransLimit(tdsTransLimit);
                     * }
                     */
                    if (tdsOverAllLimitApply != null) {
                        vendTdsTax.setOverAllLimitApply(tdsOverAllLimitApply);
                    }
                    /*
                     * if (tdsOverAllLimit != null) {
                     * vendTdsTax.setOverAllLimit(tdsOverAllLimit);
                     * }
                     */
                    if (tdsUploads != null) {
                        vendTdsTax.setTdsUploadDoc(tdsUploads);
                    }
                    genericDao.saveOrUpdate(vendTdsTax, user, entityManager);
                    if (specs != null) {
                        WithholdingTypeDetails whType = WithholdingTypeDetails.findById(entityManager, tdsWHSection);
                        specs.setIdentificationForDataValidPLorBS(whType.getId().toString());
                        genericDao.saveOrUpdate(specs, user, entityManager);
                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on save/update multiitems.", ex.getMessage());
        }
        return true;
    }

    @Override
    public ObjectNode getVendorBasicRowData(ObjectNode result, Users user, EntityManager entityManager)
            throws IDOSException {
        ArrayNode expenceSpecificsList = result.putArray("expenceSpecificsList");
        ArrayNode vendorsList = result.putArray("vendorsList");
        ArrayNode tdsWHSectionList = result.putArray("tdsWHSectionList");
        // Expence Ledgers excluded already configured
        List<Specifics> specificsList = coaDAO.getExpensesCoaChildNodes(entityManager, user);
        if (specificsList != null && specificsList.size() > 0) {
            for (Specifics specifics : specificsList) {
                ObjectNode row = Json.newObject();
                row.put("id", specifics.getId());
                row.put("name", specifics.getName());
                expenceSpecificsList.add(row);
            }
        }
        // Vendors
        List<Vendor> vendorList = Vendor.findByOrgIdAndType(entityManager, user.getOrganization().getId(), 1);
        if (vendorList != null && vendorList.size() > 0) {
            for (Vendor vendor : vendorList) {
                ObjectNode row = Json.newObject();
                row.put("id", vendor.getId());
                row.put("name", vendor.getName());
                vendorsList.add(row);
            }
        }
        // WH Sections List
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.clear();
        criterias.put("presentStatus", 1);
        List<WithholdingTypeDetails> WHSectionList = genericDao.findByCriteria(WithholdingTypeDetails.class, criterias,
                entityManager);
        if (WHSectionList != null && WHSectionList.size() > 0) {
            for (WithholdingTypeDetails whTypeDetails : WHSectionList) {
                ObjectNode row = Json.newObject();
                row.put("id", whTypeDetails.getId());
                row.put("name", whTypeDetails.getName());
                tdsWHSectionList.add(row);
            }
        }
        return result;
    }

    @Override
    public void getVendorBasicTdsDetails(ObjectNode result, Users user, EntityManager entityManager)
            throws IDOSException {
        ArrayNode tdsHistoryData = result.putArray("tdsHistoryData");
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("presentStatus", 1);
        List<VendorTDSTaxes> tdsVendorTaxes = genericDao.findByCriteria(VendorTDSTaxes.class, criterias, entityManager);
        if (tdsVendorTaxes != null && tdsVendorTaxes.size() > 0) {
            for (VendorTDSTaxes vendorTdsTax : tdsVendorTaxes) {
                ObjectNode row = Json.newObject();
                row.put("id", vendorTdsTax.getId());
                Long specificsId = vendorTdsTax.getSpecifics();
                if (specificsId != null) {
                    Specifics specific = Specifics.findById(specificsId);
                    row.put("expenceLedger", specific.getName());
                }
                row.put("tdsSection", vendorTdsTax.getTdsSection().getName());
                String vendors = vendorTdsTax.getVendors();
                if (vendors != null && !"".equals(vendors)) {
                    Vendor vend = null;
                    if (!vendors.contains(",")) {
                        vend = Vendor.findById(Long.parseLong(vendors));
                    }
                    if (vend != null) {
                        row.put("vendorName", vend.getName());
                    } else {
                        row.put("vendorName", "");
                    }
                } else {
                    row.put("vendorName", "");
                }
                if (vendorTdsTax.getTaxRate() != null) {
                    row.put("tdsRate", vendorTdsTax.getTaxRate());
                } else {
                    row.put("tdsRate", "-");
                }

                if (vendorTdsTax.getModeOfComputation() == 1) {
                    row.put("mode", "Automatic");
                } else if (vendorTdsTax.getModeOfComputation() == 2) {
                    row.put("mode", "Manual");
                } else {
                    row.put("mode", "");
                }

                if (vendorTdsTax.getTransLimit() != null) {
                    row.put("transLimit", vendorTdsTax.getTransLimit());
                } else {
                    row.put("transLimit", "-");
                }

                if (vendorTdsTax.getOverAllLimit() != null) {
                    row.put("transOverAllLimit", vendorTdsTax.getOverAllLimit());
                } else {
                    row.put("transOverAllLimit", "-");
                }
                if (vendorTdsTax.getTdsUploadDoc() != null) {
                    row.put("uploadDoc", vendorTdsTax.getTdsUploadDoc());
                } else {
                    row.put("uploadDoc", "");
                }
                tdsHistoryData.add(row);
            }
        }
    }

    @Override
    public boolean saveVendorAdvanceTds(JsonNode json, Users user, EntityManager entityManager) throws IDOSException {
        try {
            String tdsAdvanceRowData = json.findValue("tdsAdvanceRowData").toString();
            JSONArray arrJSON = new JSONArray(tdsAdvanceRowData);
            if (arrJSON.length() > 0) {
                for (int i = 0; i < arrJSON.length(); i++) {
                    JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                    Long id = rowItemData.get("id") == null || "".equals(rowItemData.getString("id")) ? null
                            : rowItemData.getLong("id");
                    Long itemId = rowItemData.get("tdsItem") == null || "".equals(rowItemData.getString("tdsItem"))
                            ? null
                            : rowItemData.getLong("tdsItem");
                    JSONArray arrInnerJSON = rowItemData.getJSONArray("tdsDetails");
                    if (arrInnerJSON.length() > 0 && itemId != null) {
                        for (int j = 0; j < arrInnerJSON.length(); j++) {
                            JSONObject rowItemInnerData = new JSONObject(arrInnerJSON.get(j).toString());
                            Long tdsVendors = rowItemInnerData.get("vendor") == null
                                    || "".equals(rowItemInnerData.getString("vendor")) ? null
                                            : rowItemInnerData.getLong("vendor");
                            Double expenceAmt = rowItemInnerData.get("expenceAmt") == null
                                    || "".equals(rowItemInnerData.getString("expenceAmt")) ? null
                                            : rowItemInnerData.getDouble("expenceAmt");
                            Double tdsEffected = rowItemInnerData.get("tdsEffected") == null
                                    || "".equals(rowItemInnerData.getString("tdsEffected")) ? null
                                            : rowItemInnerData.getDouble("tdsEffected");
                            String uptoDate = rowItemInnerData.get("uptoDate") == null
                                    || "".equals(rowItemInnerData.getString("uptoDate")) ? null
                                            : rowItemInnerData.getString("uptoDate");
                            VendorAdvTdsDetails advanceTds = null;
                            if (id != null) {
                                advanceTds = VendorAdvTdsDetails.findById(id);
                            } else {
                                advanceTds = new VendorAdvTdsDetails();
                            }
                            advanceTds.setOrganization(user.getOrganization());
                            if (itemId != null) {
                                advanceTds.setSpecifics(Specifics.findById(itemId));
                            }
                            if (tdsVendors != null) {
                                advanceTds.setVendor(Vendor.findById(tdsVendors));
                            }
                            if (expenceAmt != null) {
                                advanceTds.setExpenceAmt(expenceAmt);
                            }
                            if (tdsEffected != null) {
                                advanceTds.setExpTdsEffected(tdsEffected);
                            }

                            if (uptoDate != null) {
                                advanceTds.setUptoDate(IdosConstants.IDOSDF.parse(uptoDate));
                            }
                            genericDao.saveOrUpdate(advanceTds, user, entityManager);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on save/update multiitems.", ex.getMessage());
        }
        return true;
    }

    @Override
    public ObjectNode getVendorAdvanceRowData(ObjectNode result, Users user, Long ledger, EntityManager entityManager)
            throws IDOSException {
        ArrayNode expenceSpecificsList = result.putArray("expenceSpecificsList");
        ArrayNode vendorsList = result.putArray("vendorsList");

        // Expence Ledgers
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("presentStatus", 1);
        List<VendorTDSTaxes> vendorTDSTaxesList = genericDao.findByCriteria(VendorTDSTaxes.class, criterias,
                entityManager);
        if (vendorTDSTaxesList != null && vendorTDSTaxesList.size() > 0) {
            for (VendorTDSTaxes vendorTDSTaxes : vendorTDSTaxesList) {

                Long specificsId = vendorTDSTaxes.getSpecifics();
                if (specificsId != null) {
                    ObjectNode row = Json.newObject();
                    Specifics specific = Specifics.findById(specificsId);
                    row.put("expenceLedger", specific.getName());
                    row.put("id", specific.getId());
                    row.put("name", specific.getName());
                    expenceSpecificsList.add(row);
                }

            }
        }
        if (ledger != null) {
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("specifics.id", ledger);
            criterias.put("presentStatus", 1);
            VendorTDSTaxes vendSepe = genericDao.getByCriteria(VendorTDSTaxes.class, criterias, entityManager);
            if (vendSepe != null) {
                String vendors = vendSepe.getVendors();
                String[] split = vendors.split(",");
                for (String vendId : split) {
                    Vendor findVend = Vendor.findById(Long.parseLong(vendId));
                    if (findVend != null) {
                        ObjectNode row = Json.newObject();
                        row.put("id", findVend.getId());
                        row.put("name", findVend.getName());
                        vendorsList.add(row);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<WithHoldingTaxPojo> calculateTds(ObjectNode result, ArrayNode TdsDetail, Users user, Specifics specf,
            Vendor vendor, Double txnGrossAmt, Double newTxnNetAmount, Long txnPurposeValue, Date txnDate,
            EntityManager entityManager, boolean isCalledFromBack) throws IDOSException {
        List<WithHoldingTaxPojo> oldTxnTdsList = null;
        VendorTDSTaxes vendTdsBasic = null;
        String format = IdosConstants.IDOSDF.format(txnDate);
        try {
            txnDate = IdosConstants.IDOSDF.parse(format);
        } catch (ParseException e) {
            throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION,
                    IdosConstants.NULL_KEY_EXC_ESMF_MSG, "cannot parse date: " + format + " " + e.getMessage());
        }
        result.put("specficIsTDSSpec", "false");
        if (specf != null) {
            if (specf.getIsTdsVendorSpecific() != null && specf.getIsTdsVendorSpecific() == 1) {
                if (vendor != null) {
                    vendTdsBasic = VendorTDSTaxes.findByOrgVend(entityManager, user.getOrganization().getId(),
                            vendor.getId(),
                            specf, txnDate);
                }
            } else {
                vendTdsBasic = VendorTDSTaxes.findByOrgSpecific(entityManager, user.getOrganization().getId(),
                        specf.getId(),
                        txnDate);
            }
            if (vendTdsBasic != null) {
                result.put("specficIsTDSSpec", "true");
            } else {
                result.put("specficIsTDSSpec", "false");
            }
        }
        if (vendTdsBasic != null) {
            result.put("modeOfTdsCompute", vendTdsBasic.getModeOfComputation());
            if (vendTdsBasic.getModeOfComputation() == 1) {
                int isWithholdingApplicableOrNot = vendTdsBasic.getModeOfComputation();
                // TransactionPurpose transactionPurpose =
                // TransactionPurpose.findById(txnPurposeValue);
                String tdsApplicableTransactions = user.getOrganization().getTdsApplicableTransactions();
                // ArrayList<Long> tdsApplicableTransactionsForQuery = new ArrayList<Long>();
                boolean tdsApplicableTrans = false;
                if (tdsApplicableTransactions != null && !"".equals(tdsApplicableTransactions)) {
                    tdsApplicableTransactions = tdsApplicableTransactions.substring(0,
                            (tdsApplicableTransactions.length() - 1));
                    String[] split = tdsApplicableTransactions.split(",");
                    for (String appliedTrans : split) {
                        // tdsApplicableTransactionsForQuery.add(Long.parseLong(appliedTrans));
                        if (appliedTrans.equals(txnPurposeValue.toString())) {
                            tdsApplicableTrans = true;
                            break;
                        }
                    }
                }
                if (isWithholdingApplicableOrNot == 1 && tdsApplicableTrans) {
                    // Is this the account where you classify withholding tax (TDS) on payments made
                    // to vendors / creditors and others
                    Integer withheldingTaxType = vendTdsBasic.getTdsSection().getId().intValue();
                    // Long tdsPayableSpecificID = coaService.getSpecificsForMapping(user, "9",
                    // entityManager);
                    Long tdsPayableSpecificID = 0l;
                    if (withheldingTaxType != null) {
                        Specifics specificsMap = coaDAO.getSpecificsForMapping(user, withheldingTaxType.toString(),
                                entityManager);
                        if (specificsMap == null) {
                            throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                                    "COA mapping is not found for item: " + specf.getId(),
                                    "TDS COA mapping not found for, type: " + withheldingTaxType);
                        }
                        tdsPayableSpecificID = specificsMap.getId();
                    }
                    result.put("tdsPayableSpecific", tdsPayableSpecificID);
                    Double withHoldingRate = 0.0;
                    Double withHoldingTaxAmount = 0.0;
                    // Double grossAmount=Double.parseDouble(txnGrossAmt);
                    if (vendTdsBasic.getTaxRate() != null) {
                        withHoldingRate = vendTdsBasic.getTaxRate();
                    }
                    Double withHoldingLimit = null;
                    Double withHoldingMonetoryLimit = null;
                    Integer overAllLimitApply = vendTdsBasic.getOverAllLimitApply();
                    if (overAllLimitApply != null && overAllLimitApply == 1) {
                        if (vendTdsBasic.getOverAllLimit() != null) {
                            oldTxnTdsList = new ArrayList<>(1);
                            withHoldingMonetoryLimit = vendTdsBasic.getOverAllLimit();
                            // Find item gross value for This time limit to date and from date
                            // Double totalGrossTillDate = txnGrossAmt;
                            Double totalGrossTillDateIncludingCurrentAmt = txnGrossAmt;
                            Double totalTdsPaidTillDate = 0.0d;
                            Double totalTdsNotPaidYet = 0.0d;
                            Date toDate = vendTdsBasic.getToDate();
                            Date fromDate = vendTdsBasic.getFromDate();
                            // List<TransactionItems> TransItemList =
                            // TransactionItems.findTransItemsForVendorDateRange(user.getOrganization().getId(),
                            // specf.getId(), vendor.getId(), toDate, fromDate);
                            totalGrossTillDateIncludingCurrentAmt += TRANSACTION_ITEM_DAO
                                    .getGrossAmountForSpecificAndVendor(entityManager, user.getOrganization().getId(),
                                            specf.getId(), vendor.getId(), toDate, fromDate);
                            // find in Advance Tds History and add values to gross till date
                            VendorAdvTdsDetails vendorItemTDSHistory = VendorAdvTdsDetails.findByTdsBasicId(
                                    entityManager,
                                    user.getOrganization().getId(), vendTdsBasic.getId());
                            if (vendorItemTDSHistory != null) {
                                Double expenceAmt = vendorItemTDSHistory.getExpenceAmt();
                                Double advTdsEffected = vendorItemTDSHistory.getExpTdsEffected();
                                if (expenceAmt != null) {
                                    totalGrossTillDateIncludingCurrentAmt += expenceAmt;
                                }
                                if (advTdsEffected != null) {
                                    totalTdsPaidTillDate += advTdsEffected;
                                }
                            }
                            // check with overall limit if exceed calculate tds
                            if (totalGrossTillDateIncludingCurrentAmt > withHoldingMonetoryLimit) {
                                List<TransactionItems> TransItemList = TRANSACTION_ITEM_DAO
                                        .findTransItemsForVendorDateRange(entityManager, user.getOrganization().getId(),
                                                specf.getId(), vendor.getId(), toDate, fromDate, tdsPayableSpecificID);
                                for (TransactionItems tranItem : TransItemList) {
                                    if (log.isLoggable(Level.FINE))
                                        log.log(Level.FINE,
                                                "  txnItem == " + tranItem.getId() + " item= "
                                                        + tranItem.getTransactionSpecifics().getId() + " "
                                                        + tranItem.getGrossAmount());
                                    if (tranItem.getWithholdingAmount() == null
                                            || tranItem.getWithholdingAmount() == 0.0) {
                                        Specifics txnItemSpecific = tranItem.getTransactionSpecifics();
                                        Transaction txn = tranItem.getTransactionId();
                                        VendorTDSTaxes vendorTDSTaxes = null;
                                        if (txnItemSpecific.getIsTdsVendorSpecific() != null
                                                && txnItemSpecific.getIsTdsVendorSpecific() == 1) {
                                            if (vendor != null) {
                                                vendorTDSTaxes = VendorTDSTaxes.findByOrgVend(entityManager,
                                                        user.getOrganization().getId(), vendor.getId(), txnItemSpecific,
                                                        txn.getTransactionDate());
                                            }
                                        } else {
                                            vendorTDSTaxes = VendorTDSTaxes.findByOrgSpecific(entityManager,
                                                    user.getOrganization().getId(), txnItemSpecific.getId(),
                                                    txn.getTransactionDate());
                                        }
                                        if (vendorTDSTaxes != null && txnGrossAmt > 0.0) {
                                            if (log.isLoggable(Level.FINE))
                                                log.log(Level.FINE,
                                                        "item= " + tranItem.getTransactionSpecifics().getId()
                                                                + " rate= " + vendorTDSTaxes.getTaxRate() + " gross= "
                                                                + tranItem.getGrossAmount());
                                            Double tdsTmp = (vendorTDSTaxes.getTaxRate() / 100.0)
                                                    * (tranItem.getGrossAmount());
                                            totalTdsNotPaidYet += tdsTmp;
                                            if (isCalledFromBack) {
                                                CREATE_TRIAL_BALANCE_DAO.saveTrialBalanceTDS(user, entityManager,
                                                        txn.getTransactionBranch(),
                                                        tranItem.getTransactionId().getTransactionVendorCustomer(),
                                                        txn.getId(), txn.getTransactionPurpose(),
                                                        txn.getTransactionDate(), txnItemSpecific, 0.0, false,
                                                        IdosConstants.OUTPUT_TDS, 1);
                                            }
                                            WithHoldingTaxPojo withHoldingTaxPojo = new WithHoldingTaxPojo();
                                            withHoldingTaxPojo.setTaxAmount(tdsTmp);
                                            withHoldingTaxPojo.setTaxRate(vendorTDSTaxes.getTaxRate());
                                            withHoldingTaxPojo.setSpecific(tranItem.getTransactionSpecifics());
                                        }
                                    }
                                }
                                withHoldingTaxAmount = ((withHoldingRate / 100.0) * txnGrossAmt);
                                withHoldingTaxAmount = withHoldingTaxAmount + totalTdsNotPaidYet;
                            } else if (vendTdsBasic.getTransLimit() != null) {
                                withHoldingLimit = vendTdsBasic.getTransLimit();
                                if (withHoldingLimit < txnGrossAmt) {
                                    withHoldingTaxAmount = (withHoldingRate / 100.0) * (txnGrossAmt);
                                }
                            }
                            // TDS has to be applied on the "Total amount (Less) Already deducted amount"
                        }
                    } else {
                        if (vendTdsBasic.getTransLimit() != null) {
                            withHoldingLimit = vendTdsBasic.getTransLimit();
                            if (withHoldingLimit < txnGrossAmt) {
                                // Exceeds Transaction Limit
                                withHoldingTaxAmount = (withHoldingRate / 100.0) * (txnGrossAmt);
                            }
                        }
                    }
                    newTxnNetAmount = newTxnNetAmount - withHoldingTaxAmount;
                    ObjectNode rowtds = Json.newObject();
                    rowtds.put("totalVendNetForFinYearForWithholding", newTxnNetAmount);
                    rowtds.put("withholdingtaxRate", withHoldingRate + "(%)");
                    rowtds.put("withholdingRate", withHoldingRate);
                    if (withHoldingLimit != null) {
                        rowtds.put("withholdingtaxLimit", withHoldingLimit);
                    } else {
                        rowtds.put("withholdingtaxLimit", "0.0");
                    }
                    if (withHoldingMonetoryLimit != null) {
                        rowtds.put("withHoldingMonetoryLimit", withHoldingMonetoryLimit);
                    } else {
                        rowtds.put("withHoldingMonetoryLimit", "0.0");
                    }
                    rowtds.put("withholdingtaxTotalAmount", withHoldingTaxAmount);
                    if (TdsDetail != null) {
                        TdsDetail.add(rowtds);
                    }
                } else {
                    result.put("tdsPayableSpecific", -1);
                }
            }
        } else {
            result.put("modeOfTdsCompute", "");
        }
        result.put("txnNetAmount", newTxnNetAmount);
        return oldTxnTdsList;
    }

    @Override
    public void getVendorAdvanceTdsDetails(ObjectNode result, Users user, EntityManager entityManager)
            throws IDOSException {
        ArrayNode tdsAdvHistoryData = result.putArray("tdsAdvHistoryData");
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("presentStatus", 1);
        List<VendorAdvTdsDetails> tdsAdvVendorTaxes = genericDao.findByCriteria(VendorAdvTdsDetails.class, criterias,
                entityManager);
        if (tdsAdvVendorTaxes != null && tdsAdvVendorTaxes.size() > 0) {
            for (VendorAdvTdsDetails vendorTdsTax : tdsAdvVendorTaxes) {
                ObjectNode row = Json.newObject();
                row.put("id", vendorTdsTax.getId());
                row.put("expenceLedger", vendorTdsTax.getSpecifics().getName());
                row.put("expenceLedgerId", vendorTdsTax.getSpecifics().getId());
                row.put("vendorName", vendorTdsTax.getVendor().getName());
                row.put("vendorId", vendorTdsTax.getVendor().getId());
                if (vendorTdsTax.getExpenceAmt() != null) {
                    row.put("expenceAmount", vendorTdsTax.getExpenceAmt());
                } else {
                    row.put("expenceAmount", "-");
                }

                if (vendorTdsTax.getExpTdsEffected() != null) {
                    row.put("tdsAlreadyEffect", vendorTdsTax.getExpTdsEffected());
                } else {
                    row.put("tdsAlreadyEffect", "");
                }

                if (vendorTdsTax.getUptoDate() != null) {
                    row.put("uptoDate", IdosConstants.IDOSDF.format(vendorTdsTax.getUptoDate()));
                } else {
                    row.put("uptoDate", "");
                }
                tdsAdvHistoryData.add(row);
            }
        }
    }

    @Override
    public boolean saveVendorTdsSetup(JsonNode json, Users user, Vendor vendor, EntityManager entityManager)
            throws IDOSException {
        try {

            String vendTdsData = json.findValue("vendTdsData") == null ? null
                    : json.findValue("vendTdsData").toString();
            if (vendTdsData != null && !vendTdsData.equals("")) {
                JSONArray arrJSON = new JSONArray(vendTdsData);
                for (int i = 0; i < arrJSON.length(); i++) {
                    JSONObject tdsData = new JSONObject(arrJSON.get(i).toString());
                    if (!tdsData.toString().equals("{}")) {
                        Date fromDateObj = null;
                        Date toDateObj = null;
                        Long specificId = tdsData.get("tdsSpecificId") == null
                                || "".equals(tdsData.getString("tdsSpecificId")) ? null
                                        : tdsData.getLong("tdsSpecificId");
                        Long tdsWHSection = tdsData.get("tdsWHSection") == null
                                || "".equals(tdsData.getString("tdsWHSection")) ? null
                                        : tdsData.getLong("tdsWHSection");
                        Double tdsTaxRate = tdsData.get("tdsTaxRate") == null
                                || "".equals(tdsData.getString("tdsTaxRate")) ? null : tdsData.getDouble("tdsTaxRate");
                        Double tdsTransLimit = tdsData.get("tdsTransLimit") == null
                                || "".equals(tdsData.getString("tdsTransLimit")) ? null
                                        : tdsData.getDouble("tdsTransLimit");
                        Integer tdsOverAllLimitApply = tdsData.get("tdsOverAllLimitApply") == null
                                || "".equals(tdsData.getString("tdsOverAllLimitApply")) ? null
                                        : tdsData.getInt("tdsOverAllLimitApply");
                        Double tdsOverAllLimit = tdsData.get("tdsOverAllLimit") == null
                                || "".equals(tdsData.getString("tdsOverAllLimit")) ? null
                                        : tdsData.getDouble("tdsOverAllLimit");
                        String fromDate = tdsData.get("fromDate") == null || "".equals(tdsData.getString("fromDate"))
                                ? null
                                : tdsData.getString("fromDate");
                        String toDate = tdsData.get("toDate") == null || "".equals(tdsData.getString("toDate")) ? null
                                : tdsData.getString("toDate");
                        Double expenceAmount = tdsData.get("expenceAmount") == null
                                || "".equals(tdsData.getString("expenceAmount")) ? null
                                        : tdsData.getDouble("expenceAmount");
                        Double tdsAlreadyEffected = tdsData.get("tdsAlreadyEffected") == null
                                || "".equals(tdsData.getString("tdsAlreadyEffected")) ? null
                                        : tdsData.getDouble("tdsAlreadyEffected");
                        String uptoDate = tdsData.get("uptoDate") == null || "".equals(tdsData.getString("uptoDate"))
                                ? null
                                : tdsData.getString("uptoDate");
                        String supportingDocs = tdsData.get("supportingDocs") == null
                                || "".equals(tdsData.getString("supportingDocs")) ? null
                                        : tdsData.getString("supportingDocs");
                        if (fromDate != null && !fromDate.equals("")) {
                            fromDateObj = IdosConstants.IDOSDF.parse(fromDate);
                        }
                        if (toDate != null && !toDate.equals("")) {
                            toDateObj = IdosConstants.IDOSDF.parse(toDate);
                        }

                        Organization organization = user.getOrganization();
                        VendorTDSTaxes vendTdsTax = new VendorTDSTaxes();
                        vendTdsTax.setOrganization(organization);
                        if (vendor != null) {
                            vendTdsTax.setVendors(vendor.getId().toString());
                        }
                        if (specificId != null) {
                            vendTdsTax.setSpecifics(specificId);
                        }
                        if (tdsWHSection != null) {
                            WithholdingTypeDetails whType = WithholdingTypeDetails.findById(entityManager,
                                    tdsWHSection);
                            vendTdsTax.setTdsSection(whType);
                        }

                        if ((fromDateObj != null) && (toDateObj != null)) {
                            if (toDateObj.after(fromDateObj) || toDateObj.equals(fromDateObj)) {
                                vendTdsTax.setFromDate(fromDateObj);
                                vendTdsTax.setToDate(toDateObj);
                            } else {
                                throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
                                        IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
                                        "To date cannot be earlier than From date. ");
                            }
                        }
                        /*
                         * if (fromDateObj != null) {
                         * vendTdsTax.setFromDate(fromDateObj);
                         * }
                         * if (toDateObj != null) {
                         * vendTdsTax.setToDate(toDateObj);
                         * }
                         */
                        vendTdsTax.setModeOfComputation(1);
                        if (tdsTaxRate != null) {
                            vendTdsTax.setTaxRate(tdsTaxRate);
                        }
                        if (tdsTransLimit != null && tdsOverAllLimit != null) {
                            if (tdsOverAllLimit >= tdsTransLimit) {
                                vendTdsTax.setTransLimit(tdsTransLimit);
                                vendTdsTax.setOverAllLimit(tdsOverAllLimit);
                            } else {
                                throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
                                        IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
                                        "Transaction Limit cannot exceed Overall Limit. ");
                            }
                        } else if (tdsTransLimit != null) {
                            vendTdsTax.setTransLimit(tdsTransLimit);
                        } else if (tdsOverAllLimit != null) {
                            vendTdsTax.setOverAllLimit(tdsOverAllLimit);
                        }
                        /*
                         * if (tdsTransLimit != null) {
                         * vendTdsTax.setTransLimit(tdsTransLimit);
                         * }
                         */
                        if (tdsOverAllLimitApply != null) {
                            vendTdsTax.setOverAllLimitApply(tdsOverAllLimitApply);
                        }
                        /*
                         * if (tdsOverAllLimit != null) {
                         * vendTdsTax.setOverAllLimit(tdsOverAllLimit);
                         * }
                         */
                        genericDao.saveOrUpdate(vendTdsTax, user, entityManager);

                        if (vendTdsTax.getId() != null) {
                            VendorAdvTdsDetails advanceTds = new VendorAdvTdsDetails();
                            advanceTds.setOrganization(organization);
                            advanceTds.setVendor(vendor);
                            advanceTds.setTdsBasicId(vendTdsTax.getId());
                            if (expenceAmount != null) {
                                advanceTds.setExpenceAmt(expenceAmount);
                            }
                            if (tdsAlreadyEffected != null) {
                                advanceTds.setExpTdsEffected(tdsAlreadyEffected);
                            }

                            if (uptoDate != null) {
                                advanceTds.setUptoDate(IdosConstants.IDOSDF.parse(uptoDate));
                            }
                            genericDao.saveOrUpdate(advanceTds, user, entityManager);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on save/update multiitems.", ex.getMessage());
        }
        return true;
    }

    @Override
    public boolean saveCOATdsSetup(JsonNode json, Users user, Specifics specific, EntityManager entityManager)
            throws IDOSException {
        try {
            Date fromDateObj = null;
            Date toDateObj = null;
            String coaTdsData = json.findValue("coaTDSData") == null ? null : json.findValue("coaTDSData").toString();
            if (coaTdsData != null) {
                JSONObject tdsData = new JSONObject(coaTdsData);
                if (!tdsData.toString().equals("{}")) {
                    Long tdsWHSection = tdsData.get("tdsWHSection") == null
                            || "".equals(tdsData.getString("tdsWHSection")) ? null : tdsData.getLong("tdsWHSection");
                    Double tdsTaxRate = tdsData.get("tdsTaxRate") == null || "".equals(tdsData.getString("tdsTaxRate"))
                            ? null
                            : tdsData.getDouble("tdsTaxRate");
                    Double tdsTransLimit = tdsData.get("tdsTransLimit") == null
                            || "".equals(tdsData.getString("tdsTransLimit")) ? null
                                    : tdsData.getDouble("tdsTransLimit");
                    Integer tdsOverAllLimitApply = tdsData.get("tdsOverAllLimitApply") == null
                            || "".equals(tdsData.getString("tdsOverAllLimitApply")) ? null
                                    : tdsData.getInt("tdsOverAllLimitApply");
                    Double tdsOverAllLimit = tdsData.get("tdsOverAllLimit") == null
                            || "".equals(tdsData.getString("tdsOverAllLimit")) ? null
                                    : tdsData.getDouble("tdsOverAllLimit");
                    String fromDate = tdsData.get("fromDate") == null || "".equals(tdsData.getString("fromDate")) ? null
                            : tdsData.getString("fromDate");
                    String toDate = tdsData.get("toDate") == null || "".equals(tdsData.getString("toDate")) ? null
                            : tdsData.getString("toDate");
                    if (fromDate != null && !fromDate.equals("")) {
                        fromDateObj = IdosConstants.IDOSDF.parse(fromDate);
                    }
                    if (toDate != null && !toDate.equals("")) {
                        toDateObj = IdosConstants.IDOSDF.parse(toDate);
                    }
                    Specifics specs = null;
                    Organization organization = user.getOrganization();
                    VendorTDSTaxes vendTdsTax = new VendorTDSTaxes();
                    vendTdsTax.setOrganization(organization);
                    if (specific != null) {
                        vendTdsTax.setSpecifics(specific.getId());
                    }
                    if (tdsWHSection != null) {
                        WithholdingTypeDetails whType = WithholdingTypeDetails.findById(entityManager, tdsWHSection);
                        vendTdsTax.setTdsSection(whType);
                    }
                    if ((fromDateObj != null) && (toDateObj != null)) {
                        if (toDateObj.after(fromDateObj) || toDateObj.equals(fromDateObj)) {
                            vendTdsTax.setFromDate(fromDateObj);
                            vendTdsTax.setToDate(toDateObj);
                        } else {
                            throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
                                    IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
                                    "To date cannot be earlier than From date. ");
                        }
                    }
                    /*
                     * if (fromDateObj != null) {
                     * vendTdsTax.setFromDate(fromDateObj);
                     * }
                     * if (toDateObj != null) {
                     * vendTdsTax.setToDate(toDateObj);
                     * }
                     */
                    vendTdsTax.setModeOfComputation(1);
                    if (tdsTaxRate != null) {
                        vendTdsTax.setTaxRate(tdsTaxRate);
                    }
                    if (tdsTransLimit != null && tdsOverAllLimit != null) {
                        if (tdsOverAllLimit >= tdsTransLimit) {
                            vendTdsTax.setTransLimit(tdsTransLimit);
                            vendTdsTax.setOverAllLimit(tdsOverAllLimit);
                        } else {
                            throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
                                    IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
                                    "Transaction Limit cannot exceed Overall Limit. ");
                        }
                    } else if (tdsTransLimit != null) {
                        vendTdsTax.setTransLimit(tdsTransLimit);
                    } else if (tdsOverAllLimit != null) {
                        vendTdsTax.setOverAllLimit(tdsOverAllLimit);
                    }
                    /*
                     * if (tdsTransLimit != null) {
                     * vendTdsTax.setTransLimit(tdsTransLimit);
                     * }
                     */
                    if (tdsOverAllLimitApply != null) {
                        vendTdsTax.setOverAllLimitApply(tdsOverAllLimitApply);
                    }
                    /*
                     * if (tdsOverAllLimit != null) {
                     * vendTdsTax.setOverAllLimit(tdsOverAllLimit);
                     * }
                     */
                    genericDao.saveOrUpdate(vendTdsTax, user, entityManager);
                }
            }

        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on save/update ledger TDS.", ex.getMessage());
        }
        return true;
    }

}
