package com.idos.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import model.Branch;
import model.CustomerBranchWiseAdvBalance;
import model.CustomerDetail;
import model.Transaction;
import model.Users;
import model.Vendor;
import model.VendorAdvTdsDetails;
import model.Specifics;
import model.VendorDetail;
import model.VendorBillwiseOpBalance;
import model.VendorBranchWiseAdvBalance;
import play.libs.Json;

public class VendCustBranchWiseAdvBalanceDAOImpl implements VendCustBranchWiseAdvanceBalanceDAO {
    @Override
    public boolean saveVendorAdvanceBalance(String brachwiseOpeningAdvBalance, Vendor vendor, Users user,
            EntityManager entityManager) throws IDOSException {
        try {
            if (brachwiseOpeningAdvBalance != null && !"".equals(brachwiseOpeningAdvBalance)
                    && brachwiseOpeningAdvBalance != "") {
                JSONArray arrJSON = new JSONArray(brachwiseOpeningAdvBalance);
                for (int i = 0; i < arrJSON.length(); i++) {

                    JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                    Long branchId = rowItemData.get("branch") == null || "".equals(rowItemData.getString("branch"))
                            ? null
                            : rowItemData.getLong("branch");
                    JSONArray arrInnerJSON = rowItemData.getJSONArray("advDetails");
                    if (arrInnerJSON.length() > 0 && branchId != null) {
                        for (int j = 0; j < arrInnerJSON.length(); j++) {
                            JSONObject rowItemInnerData = new JSONObject(arrInnerJSON.get(j).toString());
                            Long id = rowItemInnerData.get("id") == null || "".equals(rowItemInnerData.getString("id"))
                                    ? null
                                    : rowItemInnerData.getLong("id");
                            String receiptDate = rowItemInnerData.get("receiptDate") == null
                                    || "".equals(rowItemInnerData.getString("receiptDate")) ? null
                                            : rowItemInnerData.getString("receiptDate");
                            String receiptNumber = rowItemInnerData.get("receiptNumber") == null
                                    || "".equals(rowItemInnerData.getString("receiptNumber")) ? null
                                            : rowItemInnerData.getString("receiptNumber");
                            Integer typeOfSupply = rowItemInnerData.get("typeOfSupply") == null
                                    || "".equals(rowItemInnerData.getString("typeOfSupply")) ? null
                                            : rowItemInnerData.getInt("typeOfSupply");
                            String placeOfSply = rowItemInnerData.get("placeOfSply") == null
                                    || "".equals(rowItemInnerData.getString("placeOfSply")) ? null
                                            : rowItemInnerData.getString("placeOfSply");
                            Long item = rowItemInnerData.get("item") == null
                                    || "".equals(rowItemInnerData.getString("item")) ? null
                                            : rowItemInnerData.getLong("item");
                            Double advAmount = rowItemInnerData.get("advAmount") == null
                                    || "".equals(rowItemInnerData.getString("advAmount")) ? null
                                            : rowItemInnerData.getDouble("advAmount");
                            String status = rowItemInnerData.get("status") == null
                                    || "".equals(rowItemInnerData.getString("status")) ? null
                                            : rowItemInnerData.getString("status");
                            VendorBranchWiseAdvBalance vendAdvanceBalance = new VendorBranchWiseAdvBalance();
                            vendAdvanceBalance.setOrganization(user.getOrganization());
                            vendAdvanceBalance.setVendor(vendor);
                            if (branchId != null) {
                                Branch branch = Branch.findById(branchId);
                                vendAdvanceBalance.setBranch(branch);
                            }
                            if (id != null) {
                                VendorBranchWiseAdvBalance advBalObj = VendorBranchWiseAdvBalance
                                        .findById(id);
                                if (advBalObj != null) {
                                    vendAdvanceBalance = advBalObj;
                                }
                            }
                            if (status != null && status.equals("DEL")) {
                                vendAdvanceBalance.setPresentStatus(0);
                            } else {
                                vendAdvanceBalance.setPresentStatus(1);
                                if (receiptDate != null) {
                                    vendAdvanceBalance.setReceiptDate(IdosConstants.IDOSDF.parse(receiptDate));
                                }

                                if (receiptNumber != null) {
                                    vendAdvanceBalance.setReceiptNo(receiptNumber);
                                }

                                if (status != null && status.equals("ADD")) {
                                    if (advAmount != null) {
                                        vendAdvanceBalance.setAdvanceAmount(advAmount);
                                        vendAdvanceBalance.setOpeningBalance(advAmount);
                                    }
                                }
                                if (typeOfSupply != null) {
                                    vendAdvanceBalance.setTypeOfSupply(typeOfSupply);
                                }
                                if (vendor.getIsRegistered() == 1) {
                                    if (placeOfSply != null) {
                                        VendorDetail vendorDetailObj = VendorDetail.findByVendorGSTNID(
                                                entityManager,
                                                vendor.getId(), placeOfSply);
                                        if (vendorDetailObj != null) {
                                            vendAdvanceBalance.setVendorDetail(vendorDetailObj);
                                        }
                                    }
                                } else {
                                    VendorDetail vendorDetailObj = VendorDetail.findByVendorID(entityManager,
                                            vendor.getId());
                                    if (vendorDetailObj != null) {
                                        vendAdvanceBalance.setVendorDetail(vendorDetailObj);
                                    }
                                }

                                if (item != null) {
                                    Specifics specificsObj = Specifics.findById(item);
                                    if (specificsObj != null) {
                                        vendAdvanceBalance.setSpecifics(specificsObj);
                                    }
                                }
                            }
                            genericDao.saveOrUpdate(vendAdvanceBalance, user, entityManager);
                        }
                    }
                }
            }
        } catch (

        Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on save/update multiitems.", ex.getMessage());
        }
        return true;
    }

    @Override
    public void getVendorAdvanceBalance(ObjectNode result, Users user, Vendor vendor, EntityManager entityManager)
            throws IDOSException {
        ArrayNode branchWiseAdvBalData = result.putArray("branchWiseAdvBalData");
        Map<String, Object> criterias = new HashMap<String, Object>();
        List<Branch> branchConfigForCust = VendorBranchWiseAdvBalance.findBranchWiseAdvanceBalance(
                entityManager,
                user.getOrganization().getId(), vendor.getId());
        if (branchConfigForCust != null && branchConfigForCust.size() > 0) {
            for (Branch branch : branchConfigForCust) {
                criterias.clear();
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("branch.id", branch.getId());
                criterias.put("vendor.id", vendor.getId());
                criterias.put("presentStatus", 1);
                List<VendorBranchWiseAdvBalance> advanceBalanceCust = genericDao
                        .findByCriteria(VendorBranchWiseAdvBalance.class, criterias, entityManager);
                if (advanceBalanceCust != null && advanceBalanceCust.size() > 0) {
                    ObjectNode row = Json.newObject();
                    row.put("branchId", branch.getId());
                    ArrayNode advDetails = row.putArray("advDetails");
                    for (VendorBranchWiseAdvBalance advBalanceBranchWise : advanceBalanceCust) {
                        ObjectNode innerRow = Json.newObject();
                        innerRow.put("id", advBalanceBranchWise.getId());
                        if (advBalanceBranchWise.getReceiptDate() != null) {
                            innerRow.put("receiptDate",
                                    IdosConstants.IDOSDF.format(advBalanceBranchWise.getReceiptDate()));
                        }
                        innerRow.put("receiptNumber", advBalanceBranchWise.getReceiptNo());
                        innerRow.put("advAmount", advBalanceBranchWise.getAdvanceAmount());
                        innerRow.put("openingBalance", advBalanceBranchWise.getOpeningBalance());
                        innerRow.put("typeOfSupply", advBalanceBranchWise.getTypeOfSupply());
                        innerRow.put("placeOfSply",
                                advBalanceBranchWise.getVendrDetail().getGstin() != null
                                        ? advBalanceBranchWise.getVendrDetail().getGstin()
                                        : "0");
                        innerRow.put("item", advBalanceBranchWise.getSpecifics().getId());
                        innerRow.put("editStatus", false);
                        advDetails.add(innerRow);
                    }
                    branchWiseAdvBalData.add(row);
                }
            }
        }

    }

    @Override
    public boolean saveCustomeAdvanceBalance(Object branchWiseAdvBalance, Vendor customer, Users user,
            EntityManager entityManager) throws IDOSException {
        try {
            if (branchWiseAdvBalance != null && !"".equals(branchWiseAdvBalance)
                    && branchWiseAdvBalance != "") {
                JSONArray arrJSON = new JSONArray(branchWiseAdvBalance.toString());
                for (int i = 0; i < arrJSON.length(); i++) {
                    JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                    Long branchId = rowItemData.get("branch") == null || "".equals(rowItemData.getString("branch"))
                            ? null
                            : rowItemData.getLong("branch");
                    JSONArray arrInnerJSON = rowItemData.getJSONArray("advDetails");
                    if (arrInnerJSON.length() > 0 && branchId != null) {
                        for (int j = 0; j < arrInnerJSON.length(); j++) {
                            JSONObject rowItemInnerData = new JSONObject(arrInnerJSON.get(j).toString());
                            Long id = rowItemInnerData.get("id") == null || "".equals(rowItemInnerData.getString("id"))
                                    ? null
                                    : rowItemInnerData.getLong("id");
                            String receiptDate = rowItemInnerData.get("receiptDate") == null
                                    || "".equals(rowItemInnerData.getString("receiptDate")) ? null
                                            : rowItemInnerData.getString("receiptDate");
                            String receiptNumber = rowItemInnerData.get("receiptNumber") == null
                                    || "".equals(rowItemInnerData.getString("receiptNumber")) ? null
                                            : rowItemInnerData.getString("receiptNumber");
                            Integer typeOfSupply = rowItemInnerData.get("typeOfSupply") == null
                                    || "".equals(rowItemInnerData.getString("typeOfSupply")) ? null
                                            : rowItemInnerData.getInt("typeOfSupply");
                            String placeOfSply = rowItemInnerData.get("placeOfSply") == null
                                    || "".equals(rowItemInnerData.getString("placeOfSply")) ? null
                                            : rowItemInnerData.getString("placeOfSply");
                            Long item = rowItemInnerData.get("item") == null
                                    || "".equals(rowItemInnerData.getString("item")) ? null
                                            : rowItemInnerData.getLong("item");
                            Double advAmount = rowItemInnerData.get("advAmount") == null
                                    || "".equals(rowItemInnerData.getString("advAmount")) ? null
                                            : rowItemInnerData.getDouble("advAmount");
                            String status = rowItemInnerData.get("status") == null
                                    || "".equals(rowItemInnerData.getString("status")) ? null
                                            : rowItemInnerData.getString("status");
                            CustomerBranchWiseAdvBalance custAdvanceBalance = new CustomerBranchWiseAdvBalance();
                            custAdvanceBalance.setOrganization(user.getOrganization());
                            custAdvanceBalance.setCustomer(customer);
                            if (branchId != null) {
                                Branch branch = Branch.findById(branchId);
                                custAdvanceBalance.setBranch(branch);
                            }
                            if (id != null) {
                                CustomerBranchWiseAdvBalance advBalObj = CustomerBranchWiseAdvBalance
                                        .findById(id);
                                if (advBalObj != null) {
                                    custAdvanceBalance = advBalObj;
                                }
                            }
                            if (status != null && status.equals("DEL")) {
                                custAdvanceBalance.setPresentStatus(0);
                            } else {
                                custAdvanceBalance.setPresentStatus(1);
                                if (receiptDate != null) {
                                    custAdvanceBalance.setReceiptDate(IdosConstants.IDOSDF.parse(receiptDate));
                                }

                                if (receiptNumber != null) {
                                    custAdvanceBalance.setReceiptNo(receiptNumber);
                                }

                                if (status != null && status.equals("ADD")) {
                                    if (advAmount != null) {
                                        custAdvanceBalance.setAdvanceAmount(advAmount);
                                        custAdvanceBalance.setOpeningBalance(advAmount);
                                    }
                                }
                                if (typeOfSupply != null) {
                                    custAdvanceBalance.setTypeOfSupply(typeOfSupply);
                                }

                                if (customer.getIsRegistered() == 1) {
                                    if (placeOfSply != null) {
                                        CustomerDetail vendorDetailObj = CustomerDetail.findByCustomerGSTNID(
                                                entityManager,
                                                customer.getId(), placeOfSply);
                                        if (vendorDetailObj != null) {
                                            custAdvanceBalance.setCustomerDetail(vendorDetailObj);
                                        }
                                    }
                                } else {
                                    CustomerDetail vendorDetailObj = CustomerDetail.findByCustomerID(entityManager,
                                            customer.getId());
                                    if (vendorDetailObj != null) {
                                        custAdvanceBalance.setCustomerDetail(vendorDetailObj);
                                    }
                                }
                                if (item != null) {
                                    Specifics specificsObj = Specifics.findById(item);
                                    if (specificsObj != null) {
                                        custAdvanceBalance.setSpecifics(specificsObj);
                                    }
                                }
                            }
                            genericDao.saveOrUpdate(custAdvanceBalance, user, entityManager);
                        }
                    }
                }
            }
        } catch (

        Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on save/update multiitems.", ex.getMessage());
        }
        return true;
    }

    @Override
    public void getCustomerAdvanceBalance(ObjectNode result, Users user, Vendor customer, EntityManager entityManager)
            throws IDOSException {
        ArrayNode branchWiseAdvBalData = result.putArray("branchWiseAdvBalData");
        Map<String, Object> criterias = new HashMap<String, Object>();
        List<Branch> branchConfigForCust = CustomerBranchWiseAdvBalance.findBranchWiseAdvanceBalance(
                entityManager,
                user.getOrganization().getId(), customer.getId());
        if (branchConfigForCust != null && branchConfigForCust.size() > 0) {
            for (Branch branch : branchConfigForCust) {
                criterias.clear();
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("branch.id", branch.getId());
                criterias.put("customer.id", customer.getId());
                criterias.put("presentStatus", 1);
                List<CustomerBranchWiseAdvBalance> advanceBalanceCust = genericDao
                        .findByCriteria(CustomerBranchWiseAdvBalance.class, criterias, entityManager);
                if (advanceBalanceCust != null && advanceBalanceCust.size() > 0) {
                    ObjectNode row = Json.newObject();
                    row.put("branchId", branch.getId());
                    ArrayNode advDetails = row.putArray("advDetails");
                    for (CustomerBranchWiseAdvBalance advBalanceBranchWise : advanceBalanceCust) {
                        ObjectNode innerRow = Json.newObject();
                        innerRow.put("id", advBalanceBranchWise.getId());
                        if (advBalanceBranchWise.getReceiptDate() != null) {
                            innerRow.put("receiptDate",
                                    IdosConstants.IDOSDF.format(advBalanceBranchWise.getReceiptDate()));
                        }
                        innerRow.put("receiptNumber", advBalanceBranchWise.getReceiptNo());
                        innerRow.put("advAmount", advBalanceBranchWise.getAdvanceAmount());
                        innerRow.put("openingBalance", advBalanceBranchWise.getOpeningBalance());
                        innerRow.put("typeOfSupply", advBalanceBranchWise.getTypeOfSupply());
                        innerRow.put("placeOfSply",
                                advBalanceBranchWise.getCustomerDetail().getGstin() != null
                                        ? advBalanceBranchWise.getCustomerDetail().getGstin()
                                        : "0");
                        innerRow.put("item", advBalanceBranchWise.getSpecifics().getId());
                        innerRow.put("editStatus", false);
                        advDetails.add(innerRow);
                    }
                    branchWiseAdvBalData.add(row);
                }
            }
        }

    }
}
