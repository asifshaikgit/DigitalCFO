package com.idos.dao;

import com.idos.util.*;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import java.util.logging.Level;
import javax.inject.Inject;
import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sunil K. Namdev on 10-08-2017.
 */
public class SellTransactionDAOImpl implements SellTransactionDAO {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    @Override
    public boolean getAdvanceDiscount(Users user, EntityManager entityManager, JsonNode json, ObjectNode result)
            throws IDOSException {
        long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
        long txnVendCustId = json.findValue("txnVendCustId") == null ? 0L : json.findValue("txnVendCustId").asLong();
        long txnVendCustItemId = json.findValue("txnVendCustItemId").asLong();
        int txnTypeOfSupply = json.findValue("txnTypeOfSupply") == null ? 0 : json.findValue("txnTypeOfSupply").asInt();
        Integer txnWithWithoutTax = json.findValue("txnWithWithoutTax") == null ? null
                : json.findValue("txnWithWithoutTax").asInt();
        String txnPlaceOfSupply = json.findValue("txnPlaceOfSupply") == null ? null
                : json.findValue("txnPlaceOfSupply").asText();
        String txnVisitingCustomer = json.findValue("txnvisitingCustomer") == null ? null
                : json.findValue("txnvisitingCustomer").asText();
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
        long txnBranchId = json.findValue("txnBranchId").asLong();
        boolean isBackDated = DateUtil.isBackDate(txnDate);

        ArrayNode customerAdvanceDiscountan = result.putArray("customerAdvanceDiscountData");
        Double advAmount = 0d, adjuestedAmt = 0d;
        Query query = null;
        if (IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER != txnPurposeVal) {
            if (txnVisitingCustomer != null && !"".equals(txnVisitingCustomer)) {
                if (log.isLoggable(Level.FINE))
                    log.log(Level.FINE, TXN_WALKIN_HQL);
                query = entityManager.createQuery(TXN_WALKIN_HQL);
                query.setParameter(1, user.getOrganization().getId());
                query.setParameter(2, txnVendCustItemId);
                query.setParameter(3, user.getOrganization().getId());
                query.setParameter(4, txnPlaceOfSupply);
                query.setParameter(5, txnTypeOfSupply);
                query.setParameter(6, txnWithWithoutTax);
                query.setParameter(7, txnVisitingCustomer);
                query.setParameter(8, txnDate);
            } else {
                if (log.isLoggable(Level.FINE))
                    log.log(Level.FINE, TXN_HQL);
                query = entityManager.createQuery(TXN_HQL);
                query.setParameter(1, user.getOrganization().getId());
                query.setParameter(2, txnBranchId);
                query.setParameter(3, txnVendCustItemId);
                query.setParameter(4, user.getOrganization().getId());
                query.setParameter(5, txnBranchId);
                query.setParameter(6, txnPlaceOfSupply);
                query.setParameter(7, txnTypeOfSupply);
                query.setParameter(8, txnWithWithoutTax);
                query.setParameter(9, txnVendCustId);
                query.setParameter(10, txnDate);
            }
            List<Object[]> txnLists = query.getResultList();
            for (Object[] val : txnLists) {
                advAmount = val[0] != null ? IdosUtil.convertStringToDouble(String.valueOf(val[0])) : 0.0;
                adjuestedAmt = val[1] != null ? IdosUtil.convertStringToDouble(String.valueOf(val[1])) : 0.0;
            }
            advAmount -= adjuestedAmt;
            if (isBackDated) {
                if (txnVisitingCustomer != null && !"".equals(txnVisitingCustomer)) {
                    query.setParameter(8, new Date());
                } else {
                    query.setParameter(10, new Date());
                }
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
        } else {
            txnVisitingCustomer = "-1";
        }
        // if (txnPurposeVal == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
        // || txnPurposeVal == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER) {
        // CustomerBranchWiseAdvBalance itemAdvAmount = CustomerBranchWiseAdvBalance
        // .getAdvAmountForItem(entityManager, user.getOrganization().getId(),
        // txnVendCustId, txnBranchId,
        // txnTypeOfSupply, Long.parseLong(txnPlaceOfSupply), txnVendCustItemId);
        // if (itemAdvAmount != null) {
        // advAmount += itemAdvAmount.getAdvanceAmount();
        // }
        // }
        ObjectNode row = Json.newObject();
        row.put("custAdvanceMoney", IdosConstants.decimalFormat.format(advAmount));
        Map<String, Object> criterias = new HashMap<String, Object>();
        if (txnVisitingCustomer != null && !"".equals(txnVisitingCustomer)) {
            criterias.put("branch.id", txnBranchId);
            criterias.put("specifics.id", txnVendCustItemId);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            BranchSpecifics branchSpecifics = genericDao.getByCriteria(BranchSpecifics.class, criterias, entityManager);
            if ((branchSpecifics != null && branchSpecifics.getWalkinCustomerMaxDiscount() != null)
                    && !ConfigParams.getInstance().isDeploymentSingleUser(user)) {
                row.put("custDiscountPerc", branchSpecifics.getWalkinCustomerMaxDiscount());
            } else {
                row.put("custDiscountPerc", "");
            }
        } else {
            criterias.put("vendorSpecific.id", txnVendCustId);
            criterias.put("specificsVendors.id", txnVendCustItemId);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            VendorSpecific customerTxnSpecifics = genericDao.getByCriteria(VendorSpecific.class, criterias,
                    entityManager);
            if ((customerTxnSpecifics != null && customerTxnSpecifics.getDiscountPercentage() != null)
                    && !ConfigParams.getInstance().isDeploymentSingleUser(user)) {
                row.put("custDiscountPerc", customerTxnSpecifics.getDiscountPercentage());
            } else {
                row.put("custDiscountPerc", "");
            }
        }
        customerAdvanceDiscountan.add(row);
        return true;
    }

    @Override
    public boolean saveAdvanceAdjustmentDetail(Users user, EntityManager entityManager, Specifics specific,
            TransactionItems txnItem, Transaction txn, Double amountToAdj, Double tdsAmountToAdj, Date txnDate)
            throws IDOSException {
        long txnVendCustId = txn.getTransactionVendorCustomer().getId();
        long txnItemId = specific.getId();
        int txnTypeOfSupply = txn.getTypeOfSupply() == null ? 0 : txn.getTypeOfSupply();
        Integer withWithoutTax = txn.getWithWithoutTax() == null ? null : txn.getWithWithoutTax();
        String txnPlaceOfSupply = txn.getDestinationGstin();
        String txnVisitingCustomer = txn.getTransactionUnavailableVendorCustomer();
        long txnBranchId = txn.getTransactionBranch().getId();
        String transItemsUsedInAdj = "";
        Long transItemId = 0l;
        Query query = null;
        if (txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                || txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CREDIT_PAY_LATER
                || txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) {
            if (txnVisitingCustomer != null && !"".equals(txnVisitingCustomer)) {
                if (log.isLoggable(Level.FINE))
                    log.log(Level.FINE, TXN_WALKIN_ADJ_BUY_HQL);
                query = entityManager.createQuery(TXN_WALKIN_ADJ_BUY_HQL);
                query.setParameter(1, user.getOrganization().getId());
                query.setParameter(2, txnItemId);
                query.setParameter(3, user.getOrganization().getId());
                query.setParameter(4, txnPlaceOfSupply);
                query.setParameter(5, txnTypeOfSupply);
                query.setParameter(6, txnVisitingCustomer);
                query.setParameter(7, txnDate);
            } else {
                if (log.isLoggable(Level.FINE))
                    log.log(Level.FINE, TXN_ADJ_BUY_HQL);
                query = entityManager.createQuery(TXN_ADJ_BUY_HQL);
                query.setParameter(1, user.getOrganization().getId());
                query.setParameter(2, txnBranchId);
                query.setParameter(3, txnItemId);
                query.setParameter(4, user.getOrganization().getId());
                query.setParameter(5, txnBranchId);
                query.setParameter(6, txnPlaceOfSupply);
                query.setParameter(7, txnTypeOfSupply);
                query.setParameter(8, txnVendCustId);
                query.setParameter(9, txnDate);
            }
        } else {
            if (txnVisitingCustomer != null && !"".equals(txnVisitingCustomer)) {
                if (log.isLoggable(Level.FINE))
                    log.log(Level.FINE, TXN_WALKIN_ADJ_HQL);
                query = entityManager.createQuery(TXN_WALKIN_ADJ_HQL);
                query.setParameter(1, user.getOrganization().getId());
                query.setParameter(2, txnItemId);
                query.setParameter(3, user.getOrganization().getId());
                query.setParameter(4, txnPlaceOfSupply);
                query.setParameter(5, txnTypeOfSupply);
                query.setParameter(6, withWithoutTax);
                query.setParameter(7, txnVisitingCustomer);
                query.setParameter(8, txnDate);
            } else {
                if (log.isLoggable(Level.FINE))
                    log.log(Level.FINE, TXN_ADJ_HQL);
                query = entityManager.createQuery(TXN_ADJ_HQL);
                query.setParameter(1, user.getOrganization().getId());
                query.setParameter(2, txnBranchId);
                query.setParameter(3, txnItemId);
                query.setParameter(4, user.getOrganization().getId());
                query.setParameter(5, txnBranchId);
                query.setParameter(6, txnPlaceOfSupply);
                query.setParameter(7, txnTypeOfSupply);
                query.setParameter(8, withWithoutTax);
                query.setParameter(9, txnVendCustId);
                query.setParameter(10, txnDate);
            }
        }
        List<TransactionItems> txnLists = query.getResultList();
        for (TransactionItems advTransItem : txnLists) {
            AdvanceAdjustmentDetail aad = new AdvanceAdjustmentDetail();
            aad.setBranch(txn.getTransactionBranch());
            aad.setOrganization(txn.getTransactionBranchOrganization());
            aad.setTransaction(txn);
            aad.setAdvTransaction(advTransItem.getTransactionId());
            Double avaliableAdv = advTransItem.getAvailableAdvance();
            Double tdsAmt = advTransItem.getWithholdingAmount() == null ? 0 : advTransItem.getWithholdingAmount();
            Double adjDoneTill = advTransItem.getAdjustmentFromAdvance() == null ? 0.0
                    : advTransItem.getAdjustmentFromAdvance();
            Double tdsDoneTill = advTransItem.getWithholdingAmountReturned() == null ? 0.0
                    : advTransItem.getWithholdingAmountReturned();
            avaliableAdv = (avaliableAdv + tdsAmt) - adjDoneTill;
            Double tmpValue = avaliableAdv - amountToAdj;
            if (tmpValue > 0.0) {
                advTransItem.setAdjustmentFromAdvance(amountToAdj + adjDoneTill);
                if (txn.getTransactionPurpose().getId() == IdosConstants.REFUND_ADVANCE_RECEIVED) {
                    advTransItem.setWithholdingAmountReturned(tdsAmountToAdj + tdsDoneTill);
                }
                genericDao.saveOrUpdate(advTransItem, user, entityManager);
                calcAdvanceAdjustmentTax(amountToAdj, aad, txnItem);
                genericDao.saveOrUpdate(aad, user, entityManager);
                break;
            } else if (tmpValue < 0.0) {
                amountToAdj = amountToAdj - avaliableAdv;
                advTransItem.setAdjustmentFromAdvance(avaliableAdv + adjDoneTill);
                if (txn.getTransactionPurpose().getId() == IdosConstants.REFUND_ADVANCE_RECEIVED) {
                    advTransItem.setWithholdingAmountReturned(tdsAmountToAdj + tdsDoneTill);
                }
                genericDao.saveOrUpdate(advTransItem, user, entityManager);
                calcAdvanceAdjustmentTax(avaliableAdv, aad, txnItem);
                genericDao.saveOrUpdate(aad, user, entityManager);
            } else if (tmpValue == 0.0) {
                advTransItem.setAdjustmentFromAdvance(avaliableAdv + adjDoneTill);
                if (txn.getTransactionPurpose().getId() == IdosConstants.REFUND_ADVANCE_RECEIVED) {
                    advTransItem.setWithholdingAmountReturned(tdsAmountToAdj + tdsDoneTill);
                }
                genericDao.saveOrUpdate(advTransItem, user, entityManager);
                calcAdvanceAdjustmentTax(amountToAdj, aad, txnItem);
                genericDao.saveOrUpdate(aad, user, entityManager);
                break;
            }
        }
        return true;
    }

    private boolean calcAdvanceAdjustmentTax(Double adjustmentAmount, AdvanceAdjustmentDetail aad,
            TransactionItems txnItem) {
        aad.setAdjustedAmount(adjustmentAmount);
        String taxName1 = txnItem.getTaxName1() == null ? "" : txnItem.getTaxName1();
        Double taxRate1 = txnItem.getTaxRate1() == null ? 0d : txnItem.getTaxRate1();
        if (taxName1.startsWith("SGST")) {
            Double taxRateToApply = taxRate1 * 2;
            Double taxAmount = (adjustmentAmount * taxRateToApply) / (100 + taxRateToApply) / 2;
            aad.setAdvAdjTax1Value(taxAmount);
        }
        String taxName2 = txnItem.getTaxName2() == null ? "" : txnItem.getTaxName2();
        Double taxRate2 = txnItem.getTaxRate2() == null ? 0d : txnItem.getTaxRate2();
        if (taxName2.startsWith("CGST")) {
            Double taxRateToApply = taxRate2 * 2;
            Double taxAmount = (adjustmentAmount * taxRateToApply) / (100 + taxRateToApply) / 2;
            aad.setAdvAdjTax2Value(taxAmount);
        }
        String taxName3 = txnItem.getTaxName3() == null ? "" : txnItem.getTaxName3();
        Double taxRate3 = txnItem.getTaxRate3() == null ? 0d : txnItem.getTaxRate3();
        if (taxName3.startsWith("IGST")) {
            Double taxAmount = (adjustmentAmount * taxRate3) / (100 + taxRate3);
            aad.setAdvAdjTax3Value(taxAmount);
        }
        String taxName4 = txnItem.getTaxName4() == null ? "" : txnItem.getTaxName4();
        Double taxRate4 = txnItem.getTaxRate4() == null ? 0d : txnItem.getTaxRate4();
        if (taxName4.startsWith("CESS")) {
            Double taxAmount = (adjustmentAmount * taxRate4) / (100 + taxRate4);
            aad.setAdvAdjTax4Value(taxAmount);
        }
        return true;
    }

    @Override
    public boolean getShippingAddress(Users user, Transaction transaction, ObjectNode result,
            EntityManager entityManager) {

        if (transaction.getTransactionVendorCustomer() != null) {
            CustomerDetail customerDetail = null;
            if (transaction.getTransactionVendorCustomer().getPlaceOfSupplyType() != null
                    && transaction.getTransactionVendorCustomer().getPlaceOfSupplyType() == 1) {
                if (transaction.getDestinationGstin() != null && transaction.getDestinationGstin().length() == 2) {
                    customerDetail = CustomerDetail.findByCustomerBillingState(entityManager,
                            transaction.getTransactionVendorCustomer().getId(), transaction.getDestinationGstin());
                } else {
                    customerDetail = CustomerDetail.findByCustomerGSTNID(entityManager,
                            transaction.getTransactionVendorCustomer().getId(), transaction.getDestinationGstin());
                }
            } else {
                if (transaction.getDestinationGstin() != null && transaction.getDestinationGstin().length() == 2) {
                    customerDetail = CustomerDetail.findByCustomerShippingState(entityManager,
                            transaction.getTransactionVendorCustomer().getId(), transaction.getDestinationGstin());
                } else {
                    customerDetail = CustomerDetail.findByCustomerGSTNID(entityManager,
                            transaction.getTransactionVendorCustomer().getId(), transaction.getDestinationGstin());
                }
            }
            StringBuilder shippingAddress = new StringBuilder();
            String shippingState = "";
            String shippingLocation = "";

            if (customerDetail != null) {

                if (customerDetail.getShippingaddress() != null) {
                    shippingAddress.append(customerDetail.getShippingaddress());
                }
                if (customerDetail.getShippinglocation() != null) {
                    shippingLocation = customerDetail.getShippinglocation();
                }
                if (customerDetail.getShippingState() != null) {
                    shippingState = customerDetail.getShippingState();
                }

            } else {

                if (transaction.getTransactionVendorCustomer().getAddress() != null) {
                    shippingAddress.append(transaction.getTransactionVendorCustomer().getAddress());
                }
                if (transaction.getTransactionVendorCustomer().getLocation() != null) {
                    shippingAddress.append(", ").append(transaction.getTransactionVendorCustomer().getLocation());
                    shippingLocation = transaction.getTransactionVendorCustomer().getLocation();
                }
                if (transaction.getTransactionVendorCustomer().getCountryState() != null) {
                    shippingAddress.append(", ").append(transaction.getTransactionVendorCustomer().getCountryState());
                    shippingState = transaction.getTransactionVendorCustomer().getCountryState();
                }
            }
            result.put("shippingAddress", IdosUtil.replaceFormatingChar(shippingAddress.toString()));
            result.put("shippingState", IdosUtil.replaceFormatingChar(shippingState));
            result.put("shippingLocation", IdosUtil.replaceFormatingChar(shippingLocation));
        } else if (transaction.getTransactionUnavailableVendorCustomer() != null
                && (transaction.getWalkinCustomerType() == 1 || transaction.getWalkinCustomerType() == 2)) {

            Vendor customer = Vendor.findByOrgIdTypeName(entityManager,
                    transaction.getTransactionBranchOrganization().getId(), IdosConstants.WALK_IN_CUSTOMER,
                    transaction.getTransactionUnavailableVendorCustomer().toUpperCase());
            CustomerDetail customerDetail = CustomerDetail.findByCustomerID(entityManager, customer.getId());
            StringBuilder shippingAddress = new StringBuilder();
            String shippingState = "";
            String shippingLocation = "";
            if (customerDetail != null) {
                if (customerDetail.getShippingaddress() != null) {
                    shippingAddress.append(customerDetail.getShippingaddress());
                }
                if (customerDetail.getShippinglocation() != null) {
                    shippingAddress.append(", ").append(customerDetail.getShippinglocation());
                    shippingLocation = customerDetail.getShippinglocation();
                }
                if (customerDetail.getShippingState() != null) {
                    shippingAddress.append(", ").append(customerDetail.getShippingState());
                    shippingState = customerDetail.getShippingState();
                }
            } else {
                if (transaction.getTransactionVendorCustomer().getAddress() != null) {
                    shippingAddress.append(transaction.getTransactionVendorCustomer().getAddress());
                }

                if (transaction.getTransactionVendorCustomer().getLocation() != null) {
                    shippingAddress.append(", ").append(transaction.getTransactionVendorCustomer().getLocation());
                    shippingLocation = transaction.getTransactionVendorCustomer().getLocation();
                }
                if (transaction.getTransactionVendorCustomer().getCountryState() != null) {
                    shippingAddress.append(", ").append(transaction.getTransactionVendorCustomer().getCountryState());
                    shippingState = transaction.getTransactionVendorCustomer().getCountryState();
                }
                String gstinStateCode = "";
                if (transaction.getDestinationGstin() != null && transaction.getDestinationGstin().length() > 1) {
                    gstinStateCode = transaction.getDestinationGstin().substring(0, 2);
                }
            }
            result.put("shippingAddress", shippingAddress.toString());
            result.put("shippingState", IdosUtil.replaceFormatingChar(shippingState));
            result.put("shippingLocation", IdosUtil.replaceFormatingChar(shippingLocation));
        } else if (transaction.getWalkinCustomerType() == 3 || transaction.getWalkinCustomerType() == 4
                || transaction.getWalkinCustomerType() == 5 || transaction.getWalkinCustomerType() == 6) {

            result.put("shippingAddress", "");
            result.put("shippingState", "");
            result.put("shippingLocation", "");
        } else if (transaction.getTransactionToBranch() != null) {
            // String shippingState =
            // transaction.getTransactionVendorCustomer().getCountryState();
            result.put("shippingAddress", transaction.getTransactionToBranch().getAddress());
            result.put("shippingState",
                    IdosConstants.STATE_CODE_MAPPING.get(transaction.getTransactionToBranch().getStateCode()));
            result.put("shippingLocation", transaction.getTransactionToBranch().getLocation());
        }
        log.log(Level.FINE, "********** End " + result);
        return true;
    }

    public boolean getAdditionalDetails(Users user, Transaction transaction, ObjectNode result,
            EntityManager entityManager) {
        if (transaction != null) {
            TransactionInvoice addDetails = null;
            addDetails = TransactionInvoice.findByTransactionID(entityManager, user.getOrganization().getId(),
                    transaction.getId());
            String dateTimeofSupply = "", dateRemovalOfGoods = "", gstIN = "";

            if (addDetails != null) {
                if (addDetails.getDatetimeOfSupply() != null) {
                    dateTimeofSupply = IdosConstants.IDOSSDFTIME.format(addDetails.getDatetimeOfSupply());
                }

                if (addDetails.getDateRemovalGoods() != null) {
                    dateRemovalOfGoods = IdosConstants.IDOSSDFTIME.format(addDetails.getDateRemovalGoods());
                }

                if (addDetails.getDatetimeOfSupply() != null) {
                    result.put("dateTimeofSupply", dateTimeofSupply);
                } else {
                    result.put("dateTimeofSupply", "");
                }

                if (addDetails.getTranportationMode() != null) {
                    result.put("transMode", addDetails.getTranportationMode());
                } else {
                    result.put("transMode", "");
                }

                if (addDetails.getVehicleDetail() != null) {
                    result.put("vehicleDetails", addDetails.getVehicleDetail());
                } else {
                    result.put("vehicleDetails", "");
                }

                if (addDetails.getDateRemovalGoods() != null) {
                    result.put("dateRemovalOfGoods", dateRemovalOfGoods);
                } else {
                    result.put("dateRemovalOfGoods", "");
                }

                if (addDetails.getApplNumberGoodsRemoval() != null) {
                    result.put("appNoGoodsRem", addDetails.getApplNumberGoodsRemoval());
                } else {
                    result.put("appNoGoodsRem", "");
                }

                if (addDetails.getGstinEcomOperator() != null && !addDetails.getGstinEcomOperator().equals("")) {
                    result.put("gstIneCommOp1", addDetails.getGstinEcomOperator().substring(0, 2));
                    result.put("gstIneCommOp2",
                            addDetails.getGstinEcomOperator().substring(2, addDetails.getGstinEcomOperator().length()));
                } else {
                    result.put("gstIneCommOp", "");
                }

                if (addDetails.getCurrencyCode() != null) {
                    result.put("currCode", addDetails.getCurrencyCode());
                } else {
                    result.put("currCode", "");
                }

                if (addDetails.getCountryName() != null) {
                    result.put("countryName", addDetails.getCountryName());
                } else {
                    result.put("countryName", "");
                }

                if (addDetails.getCurrencyConvRate() != null) {
                    result.put("convRate", addDetails.getCurrencyConvRate());
                } else {
                    result.put("convRate", "");
                }

                if (addDetails.getPortCode() != null) {
                    result.put("portCode", addDetails.getPortCode());
                } else {
                    result.put("portCode", "");
                }

                if (addDetails.getReasonForReturn() != null) {
                    result.put("reasonForReturn", addDetails.getReasonForReturn());
                } else {
                    result.put("reasonForReturn", "");
                }

                if (addDetails.getTerms() != null) {
                    result.put("terms", addDetails.getTerms());
                } else {
                    result.put("terms", "");
                }

                if (addDetails.getInvoiceHeading() != null) {
                    result.put("invoiceHeading", addDetails.getInvoiceHeading());
                } else {
                    result.put("invoiceHeading", "");
                }
                DigitalSignatureBranchWise digitalSignData = DigitalSignatureBranchWise.findByOrgAndBranch(
                        entityManager, user.getOrganization().getId(), transaction.getTransactionBranch().getId());
                if (digitalSignData != null) {
                    if (addDetails.getDigitalSignatureContent() != null)
                        result.put("digitalSignatureContent", addDetails.getDigitalSignatureContent());
                    else {
                        result.put("digitalSignatureContent", "");
                    }
                }
            } else {
                result.put("dateTimeofSupply", "");
                result.put("transMode", "");
                result.put("vehicleDetails", "");
                result.put("dateRemovalOfGoods", "");
                result.put("appNoGoodsRem", "");
                result.put("gstIneCommOp", "");
                result.put("currCode", "");
                result.put("countryName", "");
                result.put("convRate", "");
                result.put("portCode", "");
                result.put("terms", "");
            }
        }
        return true;
    }
}
