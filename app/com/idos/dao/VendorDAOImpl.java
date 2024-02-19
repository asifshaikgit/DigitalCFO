package com.idos.dao;

import actor.VendorTransactionActor;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import com.idos.util.ListUtility;
import java.util.logging.Level;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;

import play.mvc.WebSocket;

import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.inject.Inject;
import akka.stream.javadsl.*;
import akka.actor.*;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;
import akka.NotUsed;

/**
 * Created by admin on 15-07-2017.
 */
public class VendorDAOImpl implements VendorDAO {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public Vendor saveVendor(JsonNode json, Users users, EntityManager entityManager, short vendorType)
            throws IDOSException {
        boolean returnValue = false;
        log.log(Level.FINE, "******* Start");
        String vendorId = json.findValue("vendId") != null ? json.findValue("vendId").asText() : null;
        String daysOfCredit = json.findValue("daysOfCredit") != null ? json.findValue("daysOfCredit").asText() : null;
        String futurePayAllowed = json.findValue("futurePayAlwd") == null ? null
                : json.findValue("futurePayAlwd").asText();
        String vendName = json.findValue("vendName") == null ? null : json.findValue("vendName").asText();
        if (vendName == null) {
            vendName = json.findValue("txnForUnavailableCustomer") == null ? null
                    : json.findValue("txnForUnavailableCustomer").asText(); // read from transaction for sell
        }
        if (vendorId == null && vendName != null) { // VendorId null means not Edit Vendor, so if same vendName is used
                                                    // for Add then give error
            Vendor exisitngCustWithSameName = Vendor.findByOrgIdTypeName(entityManager, users.getOrganization().getId(),
                    IdosConstants.VENDOR, vendName);
            if (exisitngCustWithSameName != null) {
                throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                        IdosConstants.INVALID_DATA_EXCEPTION, "Vendor Name Exist");
            }
        }
        String gstinCode = json.findValue("gstinCode") == null ? null : json.findValue("gstinCode").asText();
        String vendorState = json.findValue("vendorState") == null ? null : json.findValue("vendorState").asText();
        String stateCode = json.findValue("vendorStateCode") == null ? null
                : json.findValue("vendorStateCode").asText();
        Integer businessIndividual = json.findValue("businessIndividual") != null
                ? json.findValue("businessIndividual").asInt()
                : 0;
        Integer registeredOrUnReg = json.findValue("registeredOrUnReg") != null
                ? json.findValue("registeredOrUnReg").asInt()
                : 0;
        gstinCode = IdosUtil.gstinValidate(registeredOrUnReg, gstinCode, vendorId);
        if (gstinCode != null && !"".equals(gstinCode) && (vendorState == null || "".equals(vendorState))) {
            throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                    IdosConstants.INVALID_DATA_EXCEPTION, "Invalid GSTIN state.");
        }
        // PBI 478, VendorId null means not Edit Vendor, so if same vendGSTIN is used
        // for Add then give error
        if (vendorId == null && gstinCode != null && !"".equals(gstinCode)) {
            Vendor existingCustWithSameGST = Vendor.findByOrgIdTypeGST(entityManager, users.getOrganization().getId(),
                    IdosConstants.VENDOR, gstinCode);
            if (existingCustWithSameGST != null) {
                throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                        IdosConstants.INVALID_DATA_EXCEPTION, "Vendor GSTIN Exist");
            }
        }
        if (gstinCode == null || "".equals(gstinCode)) {
            gstinCode = stateCode;
        }
        String newVendEmail = json.findValue("vendEmail") == null ? null : json.findValue("vendEmail").asText();
        String newVendPhone = json.findValue("vendPhone").asText();
        String newVendPhoneCtryCode = json.findValue("vendPhnCtryCode").asText();
        String newVendAddress = json.findValue("vendAddress").asText();
        Integer newVendCountry = json.findValue("vendCountry") == null ? null : json.findValue("vendCountry").asInt();
        String newVendLocation = json.findValue("vendLocation").asText();
        String contAgg = json.findPath("vendContAgg") != null ? json.findPath("vendContAgg").asText() : null;
        String purOrd = json.findPath("vendPurOrd") != null ? json.findPath("vendPurOrd").asText() : null;
        String newVendSpecifics = json.findValue("vendSelSpecf") == null ? null
                : json.findValue("vendSelSpecf").asText();
        String newVendUnitCost = json.findValue("vendUnitCost") == null ? null
                : json.findValue("vendUnitCost").asText();
        String validityFrom = json.findValue("validityFrom") == null ? null : json.findValue("validityFrom").asText();
        String validityTo = json.findValue("validityTo") == null ? null : json.findValue("validityTo").asText();
        String openingBalance = json.findValue("openingBalance") == null ? null
                : json.findValue("openingBalance").asText();
        String openingBalanceAdvPaid = json.findValue("openingBalanceAdvPaid") == null ? null
                : json.findValue("openingBalanceAdvPaid").asText();
        String vendStatutoryName1 = json.findValue("vendStatutoryName1") == null ? null
                : json.findValue("vendStatutoryName1").asText();
        String vendStatutoryNumber1 = json.findValue("vendStatutoryNumber1") == null ? null
                : json.findValue("vendStatutoryNumber1").asText();
        String vendStatutoryName2 = json.findValue("vendStatutoryName2") == null ? null
                : json.findValue("vendStatutoryName2").asText();
        String vendStatutoryNumber2 = json.findValue("vendStatutoryNumber2") == null ? null
                : json.findValue("vendStatutoryNumber2").asText();
        String vendStatutoryName3 = json.findValue("vendStatutoryName3") == null ? null
                : json.findValue("vendStatutoryName3").asText();
        String vendStatutoryNumber3 = json.findValue("vendStatutoryNumber3") == null ? null
                : json.findValue("vendStatutoryNumber3").asText();
        String vendStatutoryName4 = json.findValue("vendStatutoryName4") == null ? null
                : json.findValue("vendStatutoryName4").asText();
        String vendStatutoryNumber4 = json.findValue("vendStatutoryNumber4") == null ? null
                : json.findValue("vendStatutoryNumber4").asText();
        String vendSelGroup = json.findValue("vendSelGroup") == null ? null : json.findValue("vendSelGroup").asText();
        String vendPanNo = json.findValue("vendPanNo") == null ? null : json.findValue("vendPanNo").asText();
        // PBI 478 changes
        if (vendorId == null && vendPanNo != null && !"".equals(vendPanNo)) {
            Vendor existingCustWithSamePanNo = Vendor.findByOrgIdTypePanNo(entityManager,
                    users.getOrganization().getId(), IdosConstants.VENDOR, vendPanNo);
            if (existingCustWithSamePanNo != null) {
                throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                        IdosConstants.INVALID_DATA_EXCEPTION, "Vendor PAN No. Exist");
            }
        }
        Integer natureOfVend = json.findValue("natureOfVend") == null ? null : json.findValue("natureOfVend").asInt();
        String billwiseOpeningBalance = json.findValue("billwiseOpeningBalance") == null ? null
                : json.findValue("billwiseOpeningBalance").toString();
        // String brachwiseOpeningAdvBalance = json.findValue("branchWiseAdvBalance") ==
        // null ? null
        // : json.findValue("branchWiseAdvBalance").toString();
        String vendRcmTaxRateForItems = json.findValue("vendRcmTaxRateForItems") == null ? null
                : json.findValue("vendRcmTaxRateForItems").asText();
        String vendCessTaxRateForItems = json.findValue("vendCessTaxRateForItems") == null ? null
                : json.findValue("vendCessTaxRateForItems").asText();
        String vendRcmApplicableDateItems = json.findValue("vendRcmApplicableDateItems") == null ? null
                : json.findValue("vendRcmApplicableDateItems").asText();

        /*
         * Manali: IDOS 12May2015 CHanges to remove vendor adj, as it is handled using
         * journal entry.
         * String vendAllowedForAdjustments =
         * json.findValue("vendAllowedForAdjustments").asText();
         * String vendAdjustmentsName =
         * json.findValue("vendAdjustmentsName")!=null?json.findValue(
         * "vendAdjustmentsName").asText():null;
         * String vendAdjustmentsBasis =
         * json.findValue("vendAdjustmentsBasis")!=null?json.findValue(
         * "vendAdjustmentsBasis").asText():null;
         * String vendAdjustmentsBasisRateForEachInvoice =
         * json.findValue("vendAdjustmentsBasisRateForEachInvoice")!=null?json.findValue
         * ("vendAdjustmentsBasisRateForEachInvoice").asText():null;
         */
        String newVendSpecf = null;
        String newVendUnitPrice = null;
        String newVendRcmTaxRateForItems = null;
        String newVendCessTaxRateForItems = null;
        String newVendRcmApplicableDateItems = null;
        if (newVendSpecifics != null && !newVendSpecifics.equals("")) {
            newVendSpecf = newVendSpecifics.substring(0, newVendSpecifics.length() - 1);
        }
        if (newVendUnitCost != null && !newVendUnitCost.equals("")) {
            newVendUnitPrice = newVendUnitCost.substring(0, newVendUnitCost.length() - 1);
        }
        if (vendRcmTaxRateForItems != null && !vendRcmTaxRateForItems.equals("")) {
            newVendRcmTaxRateForItems = vendRcmTaxRateForItems.substring(0, vendRcmTaxRateForItems.length() - 1);
        }
        if (vendCessTaxRateForItems != null && !vendCessTaxRateForItems.equals("")) {
            newVendCessTaxRateForItems = vendCessTaxRateForItems.substring(0, vendCessTaxRateForItems.length() - 1);
        }
        if (vendRcmApplicableDateItems != null && !vendRcmApplicableDateItems.equals("")) {
            newVendRcmApplicableDateItems = vendRcmApplicableDateItems.substring(0,
                    vendRcmApplicableDateItems.length() - 1);
        }

        Vendor vend;
        if (vendorId == null) {
            vend = new Vendor();
        } else {
            vend = Vendor.findById(IdosUtil.convertStringToLong(vendorId));
        }
        if (registeredOrUnReg == 1) {
            vend.setGstin(gstinCode);
        } else {
            vend.setGstin(null);
        }
        vend.setIsBusiness(businessIndividual);
        vend.setIsRegistered(registeredOrUnReg);

        if (vendSelGroup != null && !vendSelGroup.equals("")) {
            VendorGroup vendGroup = VendorGroup.findById(IdosUtil.convertStringToLong(vendSelGroup));
            vend.setVendorGroup(vendGroup);
        }
        if (openingBalance != null && openingBalance != "") {
            vend.setTotalOriginalOpeningBalance(IdosUtil.convertStringToDouble(openingBalance));
            vend.setTotalOpeningBalance(vend.getTotalOriginalOpeningBalance());
        }
        if (openingBalanceAdvPaid != null && openingBalanceAdvPaid != "") {
            vend.setTotalOpeningBalanceAdvPaid(IdosUtil.convertStringToDouble(openingBalanceAdvPaid));
            vend.setTotalOriginalOpeningBalanceAdvPaid(vend.getTotalOpeningBalanceAdvPaid());
        }
        if (vendStatutoryName1 != null && !vendStatutoryName1.equals("")) {
            vend.setStatutoryName1(vendStatutoryName1);
        }
        if (vendStatutoryNumber1 != null && !vendStatutoryNumber1.equals("")) {
            vend.setStatutoryNumber1(vendStatutoryNumber1);
        }
        if (vendStatutoryName2 != null && !vendStatutoryName2.equals("")) {
            vend.setStatutoryName2(vendStatutoryName2);
        }
        if (vendStatutoryNumber2 != null && !vendStatutoryNumber2.equals("")) {
            vend.setStatutoryNumber2(vendStatutoryNumber2);
        }
        if (vendStatutoryName3 != null && !vendStatutoryName3.equals("")) {
            vend.setStatutoryName3(vendStatutoryName3);
        }
        if (vendStatutoryNumber3 != null && !vendStatutoryNumber3.equals("")) {
            vend.setStatutoryNumber3(vendStatutoryNumber3);
        }
        if (vendStatutoryName4 != null && !vendStatutoryName4.equals("")) {
            vend.setStatutoryName4(vendStatutoryName4);
        }
        if (vendStatutoryNumber4 != null && !vendStatutoryNumber4.equals("")) {
            vend.setStatutoryNumber4(vendStatutoryNumber4);
        }
        vend.setVendorSpecifics(newVendSpecf);
        vend.setVendorSpecificsUnitPrice(newVendUnitPrice);
        vend.setVendorSpecificsRcmCessRate(newVendCessTaxRateForItems);
        vend.setVendorSpecificsRcmTaxRate(newVendRcmTaxRateForItems);
        vend.setVendorSpecificsRcmApplicableDate(newVendRcmApplicableDateItems);
        vend.setName(vendName);
        vend.setAddress(newVendAddress);
        vend.setCountryState(vendorState);
        vend.setCountry(newVendCountry);
        try {
            if (validityFrom != null && !validityFrom.equals("")) {
                vend.setValidityFrom(IdosConstants.mysqldf
                        .parse(IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(validityFrom))));
            }
            if (validityTo != null && !validityTo.equals("")) {
                vend.setValidityTo(IdosConstants.mysqldf
                        .parse(IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(validityTo))));
            }
        } catch (java.text.ParseException ex) {
            log.log(Level.SEVERE, "Date cannot be parsed", ex);
            throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    IdosConstants.INVALID_DATA_EXCEPTION, "Date cannot be parsed");
        }
        vend.setLocation(newVendLocation);
        vend.setEmail(newVendEmail);
        vend.setPhoneCtryCode(newVendPhoneCtryCode);
        // vend.setContractPoDoc(contAgg);
        String audDocument = "";

        /*
         * if (contAgg != null && !contAgg.equals("")) {
         * String contAggUploadsArray[] = contAgg.split(",");
         * for (int i = 0; i <contAggUploadsArray.length; i++) {
         * if (audDocument.equals("")) {
         * audDocument += users.getEmail() + "#" + contAggUploadsArray[i];
         * } else {
         * audDocument += "," + users.getEmail() + "#" + contAggUploadsArray[i];
         * }
         * }
         * vend.setContractPoDoc(audDocument);
         * }
         */
        vend.setPriceListDoc(TRANSACTION_DAO.getAndDeleteSupportingDocument(vend.getPriceListDoc(), users.getEmail(),
                contAgg, users, entityManager));
        vend.setPurchaseOrder(purOrd);
        vend.setPhone(newVendPhone);
        if (vendorType == IdosConstants.WALK_IN_VENDOR) {
            vend.setType(4);
        } else {
            vend.setType(1);
        }
        if (vendPanNo != null && !vendPanNo.equals("")) {
            vend.setPanNo(vendPanNo);
        }
        if (natureOfVend != null) {
            vend.setNatureOfVendor(natureOfVend);
        }
        if (futurePayAllowed != null)
            vend.setPurchaseType(IdosUtil.convertStringToInt(futurePayAllowed));

        if (daysOfCredit != "" && daysOfCredit != null) {
            vend.setDaysForCredit(IdosUtil.convertStringToInt(daysOfCredit));
        }
        vend.setBranch(users.getBranch());
        vend.setOrganization(users.getOrganization());
        if (!users.getUserRolesName().contains("MASTER ADMIN")) {
            vend.setPresentStatus(0);
        } else {
            vend.setPresentStatus(1);
        }
        genericDao.saveOrUpdate(vend, users, entityManager);
        FILE_UPLOAD_DAO.updateUploadFileLogs(entityManager, users, contAgg, vend.getId(), IdosConstants.VENDOR_MODULE);
        // Add Billwise Opening Balance
        BILLWISE_OPENING_BAL_DAO.saveVendorOpeningBalance(billwiseOpeningBalance, vend, users, entityManager);
        VENDOR_TDS_DAO.saveVendorTdsSetup(json, users, vend, entityManager);

        VendorDetail vendorDetail = null;
        if (vendorType == IdosConstants.WALK_IN_CUSTOMER || vend.getIsRegistered() == 0) {
            vendorDetail = VendorDetail.findByVendorID(entityManager, vend.getId());
        } else {
            vendorDetail = VendorDetail.findByVendorGSTNLocationID(entityManager, vend.getId(), gstinCode,
                    newVendLocation);
        }
        boolean isNewVendor = false;
        if (vendorDetail == null) {
            vendorDetail = new VendorDetail();
            isNewVendor = true;
        }

        if ((vendorType == IdosConstants.WALK_IN_VENDOR && isNewVendor) || (IdosConstants.VENDOR == vendorType)) {
            vendorDetail.setVendor(vend);
            vendorDetail.setAddress(newVendAddress);
            vendorDetail.setCountry(newVendCountry);
            vendorDetail.setCountryState(vendorState);
            vendorDetail.setStateCode(stateCode);
            if (registeredOrUnReg == 1) {
                vendorDetail.setGstin(gstinCode);
            } else {
                vendorDetail.setGstin(null);
            }
            vendorDetail.setLocation(newVendLocation);
            vendorDetail.setOrganization(users.getOrganization());
            vendorDetail.setPhone(newVendPhone);
            vendorDetail.setPhoneCtryCode(newVendPhoneCtryCode);
            genericDao.saveOrUpdate(vendorDetail, users, entityManager);
            if (vendorType != IdosConstants.WALK_IN_VENDOR && vendorDetail.getId() != null) {
                saveVendorDetail(json, vend, users, entityManager, vendorDetail.getId());
            }
        }
        // BRACHWISE_ADVANCE_BAL_DAO.saveVendorAdvanceBalance(brachwiseOpeningAdvBalance,
        // vend, users,
        // entityManager);
        returnValue = true;
        if (vendorType != IdosConstants.WALK_IN_VENDOR) {
            ArrayList inparamList = new ArrayList(2);
            inparamList.add(vend.getName());
            inparamList.add(vend.getId());
            List<Vendor> vendSpecOpt = null;

            if (!users.getUserRolesName().contains("MASTER ADMIN")) {
                vendSpecOpt = genericDao.queryWithParams(VENDOR_JQL_OTHERS, entityManager, inparamList);
            } else {
                vendSpecOpt = genericDao.queryWithParams(VENDOR_JQL_MASTER, entityManager, inparamList);
            }
            Vendor enteredVendor = null;
            for (Vendor vendSpec : vendSpecOpt) {
                enteredVendor = vendSpec;
            }
            // Map<String, ActorRef> orgvendvendregistrered = new HashMap<String,
            // ActorRef>();
            // Object[] keyArray =
            // VendorTransactionActor.vendvendregistrered.keySet().toArray();
            // for (int i = 0; i < keyArray.length; i++) {
            // StringBuilder sbquery = new StringBuilder("");
            // sbquery.append(
            // "select obj from Users obj WHERE obj.email ='" + keyArray[i] + "' and
            // obj.presentStatus=1");
            // List<Users> orgusers = genericDao.executeSimpleQuery(sbquery.toString(),
            // entityManager);
            // if (!orgusers.isEmpty()
            // && orgusers.get(0).getOrganization().getId() ==
            // enteredVendor.getOrganization().getId()) {
            // orgvendvendregistrered.put(keyArray[i].toString(),
            // VendorTransactionActor.vendvendregistrered.get(keyArray[i]));
            // }
            // }
            String vendorCountry = "";
            if (enteredVendor.getCountry() != null) {
                IDOSCountry country = IDOSCountry
                        .findById(IdosUtil.convertStringToLong(enteredVendor.getCountry().toString()));
                if (country != null) {
                    vendorCountry = country.getName();
                }
            }
            String vendorAddress = "";
            if (enteredVendor.getAddress() != null) {
                vendorAddress = enteredVendor.getAddress();
            }
            // VendorTransactionActor.add(enteredVendor.getId(), orgvendvendregistrered,
            // enteredVendor.getName(),
            // vendorAddress, enteredVendor.getLocation(), enteredVendor.getEmail(),
            // enteredVendor.getGrantAccess(), enteredVendor.getPhone(),
            // enteredVendor.getType(), "vendorCustomer",
            // enteredVendor.getPresentStatus());
            returnValue = saveVendorOtherInformation(json, vend, users, entityManager);
        }
        log.log(Level.FINE, "******* End");
        return vend;
    }

    private boolean saveVendorDetail(JsonNode json, Vendor vendor, Users user, EntityManager entityManager,
            long mainVendorDetailID) throws IDOSException {
        log.log(Level.FINE, "********** start " + json);
        String isGstinAddedInTransHid = json.findValue("isGstinAddedInTransHid") == null ? null
                : json.findValue("isGstinAddedInTransHid").asText();
        if (isGstinAddedInTransHid != null && !"1".equals(isGstinAddedInTransHid)) {
            return true;
        }
        String vendorDetailIdListHid = json.findValue("vendorDetailIdListHid") == null ? null
                : json.findValue("vendorDetailIdListHid").asText();
        String gstinCodeHid = json.findValue("gstinCodeHid") == null ? null : json.findValue("gstinCodeHid").asText();
        String vendorAddressHid = json.findValue("vendorAddressHid") == null ? null
                : json.findValue("vendorAddressHid").asText();
        String vendorcountryCodeHid = json.findValue("vendorcountryCodeHid") == null ? null
                : json.findValue("vendorcountryCodeHid").asText();
        String vendorstateHid = json.findValue("vendorstateHid") == null ? null
                : json.findValue("vendorstateHid").asText();
        String vendorStateCodeHid = json.findValue("vendorStateCodeHid") == null ? null
                : json.findValue("vendorStateCodeHid").asText();
        String vendorlocationHid = json.findValue("vendorlocationHid") == null ? null
                : json.findValue("vendorlocationHid").asText();
        String vendorPhnNocountryCodeHid = json.findValue("vendorPhnNocountryCodeHid") == null ? null
                : json.findValue("vendorPhnNocountryCodeHid").asText();
        String vendorphone1Hid = json.findValue("vendorphone1Hid") == null ? null
                : json.findValue("vendorphone1Hid").asText();
        String vendorphone2Hid = json.findValue("vendorphone2Hid") == null ? null
                : json.findValue("vendorphone2Hid").asText();
        String vendorphone3Hid = json.findValue("vendorphone3Hid") == null ? null
                : json.findValue("vendorphone3Hid").asText();
        String gstinCheckedValues = json.findValue("gstinCheckedValues") == null ? null
                : json.findValue("gstinCheckedValues").asText();

        String vendorDetailIdListArray[] = vendorDetailIdListHid.split("\\|", -1);
        String gstinCodeArray[] = gstinCodeHid.split("\\|", -1);
        String vendorAddressArray[] = vendorAddressHid.split("\\|", -1);
        String vendorCountryCodeArray[] = vendorcountryCodeHid.split("\\|", -1);
        String vendorStateArray[] = vendorstateHid.split("\\|", -1);
        String vendorStateCodeArray[] = vendorStateCodeHid.split("\\|", -1);
        String vendorLocationArray[] = vendorlocationHid.split("\\|", -1);
        String vendorPhnNocountryCodeArray[] = vendorPhnNocountryCodeHid.split("\\|", -1);
        String vendorPhone1Array[] = vendorphone1Hid.split("\\|", -1);
        String vendorPhone2Array[] = vendorphone2Hid.split("\\|", -1);
        String vendorPhone3Array[] = vendorphone3Hid.split("\\|", -1);
        String gstinCheckedArray[] = gstinCheckedValues.split(",");
        for (int i = 0; i < vendorDetailIdListArray.length; i++) {
            if (vendorDetailIdListArray[i] == null || "".equals(vendorDetailIdListArray[i])) {
                continue;
            }
            long vendorDetailID = IdosUtil.convertStringToLong(vendorDetailIdListArray[i]);
            if (vendorDetailID == mainVendorDetailID) {
                continue; // skip main
            }
            VendorDetail vendorDetail = VendorDetail.findByVendorDetailID(vendorDetailID);
            if (vendorDetail == null) {
                vendorDetail = new VendorDetail();
            }
            if ("true".equals(gstinCheckedArray[i])) {
                vendorDetail.setPresentStatus(1);
            } else {
                vendorDetail.setPresentStatus(0);
            }
            vendorDetail.setVendor(vendor);
            vendorDetail.setOrganization(user.getOrganization());

            if (gstinCodeArray[i] == null || "".equals(gstinCodeArray[i])) {
                vendorDetail.setGstin(vendorStateCodeArray[i]);
            }
            gstinCodeArray[i] = IdosUtil.branchAndMultiGstinValidate(gstinCodeArray[i], vendorDetailIdListArray[i]);
            vendorDetail.setGstin(gstinCodeArray[i]);
            vendorDetail.setAddress(vendorAddressArray[i]);
            vendorDetail.setLocation(vendorLocationArray[i]);
            vendorDetail.setCountryState(vendorStateArray[i]);
            vendorDetail.setStateCode(vendorStateCodeArray[i]);
            if (vendorCountryCodeArray[i] != null && !"".equals(vendorCountryCodeArray[i])) {
                Integer countryCode = new Integer(vendorCountryCodeArray[i]);
                vendorDetail.setCountry(countryCode);
            }
            vendorDetail.setPhoneCtryCode(vendorPhnNocountryCodeArray[i]);
            StringBuilder phone = new StringBuilder();
            phone.append(vendorPhone1Array[i]).append(vendorPhone2Array[i]).append(vendorPhone3Array[i]);
            vendorDetail.setPhone(phone.toString());
            genericDao.saveOrUpdate(vendorDetail, user, entityManager);
        }
        log.log(Level.FINE, "******** End");
        return true;
    }

    private boolean saveVendorOtherInformation(JsonNode json, Vendor vend, Users users, EntityManager entityManager)
            throws IDOSException {
        log.log(Level.FINE, "******* Start");
        List<BranchVendors> oldVendorBranches = BranchVendors.findByVendor(entityManager,
                users.getOrganization().getId(),
                vend.getId());
        List<BranchVendors> newVendorBranches = new ArrayList<BranchVendors>();
        String vendorBnchs = json.findValue("vendorBnchs").asText();
        String newVendSpecifics = json.findValue("vendSelSpecf").asText();
        String newVendUnitCost = json.findValue("vendUnitCost").asText();
        String vendRcmTaxRateForItems = json.findValue("vendRcmTaxRateForItems") == null ? null
                : json.findValue("vendRcmTaxRateForItems").asText();
        String vendCessTaxRateForItems = json.findValue("vendCessTaxRateForItems") == null ? null
                : json.findValue("vendCessTaxRateForItems").asText();
        String vendRcmApplicableDateItems = json.findValue("vendRcmApplicableDateItems") == null ? null
                : json.findValue("vendRcmApplicableDateItems").asText();

        String newVendBnchs[] = vendorBnchs.split(",");
        String branchOpeningBalance = json.findValue("branchOpeningBalance") == null ? null
                : json.findValue("branchOpeningBalance").asText();
        String branchOpeningBalanceAP = json.findValue("branchOpeningBalanceAP") == null ? null
                : json.findValue("branchOpeningBalanceAP").asText();
        String branchOpeningBalanceArr[] = null;
        String branchOpeningBalanceAPArr[] = null;
        if (branchOpeningBalance != null) {
            branchOpeningBalanceArr = branchOpeningBalance.split(",");
        }
        if (branchOpeningBalanceAP != null) {
            branchOpeningBalanceAPArr = branchOpeningBalanceAP.split(",");
        }
        for (int i = 0; i < newVendBnchs.length; i++) {
            BranchVendors newBnchVend = new BranchVendors();
            if (!newVendBnchs[i].equals("")) {
                newBnchVend.setVendor(vend);
                Branch bnch = Branch.findById(IdosUtil.convertStringToLong(newVendBnchs[i]));
                newBnchVend.setBranch(bnch);
                newBnchVend.setOrganization(bnch.getOrganization());
                if (i < branchOpeningBalanceArr.length && branchOpeningBalanceArr[i] != null
                        && !"".equals(branchOpeningBalanceArr[i])) {
                    newBnchVend.setOriginalOpeningBalance(IdosUtil.convertStringToDouble(branchOpeningBalanceArr[i]));
                    newBnchVend.setOpeningBalance(IdosUtil.convertStringToDouble(branchOpeningBalanceArr[i]));
                } else {
                    newBnchVend.setOriginalOpeningBalance(0.0);
                    newBnchVend.setOpeningBalance(0.0);
                }
                if (i < branchOpeningBalanceAPArr.length && branchOpeningBalanceAPArr[i] != null
                        && !"".equals(branchOpeningBalanceAPArr[i])) {
                    newBnchVend.setOriginalOpeningBalanceAdvPaid(
                            IdosUtil.convertStringToDouble(branchOpeningBalanceAPArr[i]));
                    newBnchVend.setOpeningBalanceAdvPaid(IdosUtil.convertStringToDouble(branchOpeningBalanceAPArr[i]));
                } else {
                    newBnchVend.setOriginalOpeningBalanceAdvPaid(0.0);
                    newBnchVend.setOpeningBalanceAdvPaid(0.0);
                }
                newVendorBranches.add(newBnchVend);
            }
        }
        List<List<BranchVendors>> businessEntityTransactionList = ListUtility.getTransactionList(oldVendorBranches,
                newVendorBranches);
        for (int i = 0; i < businessEntityTransactionList.size(); i++) {
            if (i == 0) {
                List<BranchVendors> oldBnchVendor = businessEntityTransactionList.get(i);
                if (oldBnchVendor != null) {
                    for (BranchVendors bnchVendor : oldBnchVendor) {
                        entityManager.remove(bnchVendor);
                    }
                }
            }
            if (i == 1) {
                List<BranchVendors> newBnchVendor = businessEntityTransactionList.get(i);
                if (newBnchVendor != null) {
                    for (BranchVendors newbnchVendor : newBnchVendor) {
                        genericDao.saveOrUpdate(newbnchVendor, users, entityManager);
                    }
                }
            }
        }
        String newVendSpecf = "";
        String newVendUnitPrice = "";
        String newVendSpecfTaxRate = "";
        String newVendSpecfCessRate = "";
        String newVendSpecfApplicableDate = "";
        List<VendorSpecific> oldVendorSpecifics = VendorSpecific.findBySpecific(entityManager,
                users.getOrganization().getId(),
                vend.getId());
        List<VendorSpecific> newVendorSpecifics = new ArrayList<VendorSpecific>();
        if (newVendSpecifics != null && !newVendSpecifics.equals("") && newVendSpecifics != "") {
            newVendSpecf = newVendSpecifics.substring(0, newVendSpecifics.length() - 1);
            newVendUnitPrice = newVendUnitCost.substring(0, newVendUnitCost.length() - 1);
        }
        if (vendRcmTaxRateForItems != null && !vendRcmTaxRateForItems.equals("")) {
            newVendSpecfTaxRate = vendRcmTaxRateForItems.substring(0, vendRcmTaxRateForItems.length() - 1);
        }
        if (vendCessTaxRateForItems != null && !vendCessTaxRateForItems.equals("")) {
            newVendSpecfCessRate = vendCessTaxRateForItems.substring(0, vendCessTaxRateForItems.length() - 1);
        }
        if (vendRcmApplicableDateItems != null && !vendRcmApplicableDateItems.equals("")) {
            newVendSpecfApplicableDate = vendRcmApplicableDateItems.substring(0,
                    vendRcmApplicableDateItems.length() - 1);
        }

        String newvendspecf[] = newVendSpecf.split(",");
        String newvendunitprice[] = newVendUnitPrice.split(",");
        String newvendTaxRate[] = newVendSpecfTaxRate.split(",");
        String newvendCessRate[] = newVendSpecfCessRate.split(",");
        String newvendApplicableDate[] = newVendSpecfApplicableDate.split(Pattern.quote("|"));
        for (int i = 0; i < newvendspecf.length; i++) {
            VendorSpecific vennSpecf = null;
            if (!newvendspecf[i].equals("")) {
                Specifics newVendSpecfics = Specifics.findById(IdosUtil.convertStringToLong(newvendspecf[i]));
                vennSpecf = VendorSpecific.findByVendorAndSpecific(entityManager, users.getOrganization().getId(),
                        users.getBranch().getId(), vend.getId(), IdosUtil.convertStringToLong(newvendspecf[i]));
                if (vennSpecf == null) {
                    vennSpecf = new VendorSpecific();
                }
                Particulars newVendParticulars = newVendSpecfics.getParticularsId();
                vennSpecf.setVendorSpecific(vend);
                vennSpecf.setSpecificsVendors(newVendSpecfics);
                vennSpecf.setBranch(users.getBranch());
                vennSpecf.setOrganization(users.getOrganization());
                vennSpecf.setParticulars(newVendParticulars);
                if (newvendunitprice.length > i) {
                    if (!newvendunitprice[i].equals("") && newvendunitprice[i] != null) {
                        vennSpecf.setUnitPrice(IdosUtil.convertStringToDouble(newvendunitprice[i]));
                    }
                }
                if (newvendTaxRate.length > i) {
                    if (!newvendTaxRate[i].equals("") && newvendTaxRate[i] != null) {
                        vennSpecf.setRcmTaxRateVend(IdosUtil.convertStringToDouble(newvendTaxRate[i]));
                    } else {
                        vennSpecf.setRcmTaxRateVend(null);
                    }
                }
                if (newvendCessRate.length > i) {
                    if (!newvendCessRate[i].equals("") && newvendCessRate[i] != null) {
                        vennSpecf.setCessTaxRateVend(IdosUtil.convertStringToDouble(newvendCessRate[i]));
                    } else {
                        vennSpecf.setCessTaxRateVend(null);
                    }
                }
                if (newvendApplicableDate.length > i) {
                    if (!newvendApplicableDate[i].equals("") && newvendApplicableDate[i] != null) {
                        try {
                            Date date = new Date();
                            String dateString = IdosConstants.IDOSDF
                                    .format(IdosConstants.IDOSDF.parse(newvendApplicableDate[i]));
                            String timeString = IdosConstants.TIMEFMT.format(date);
                            String applicableDateString = dateString + " " + timeString;
                            vennSpecf.setApplicableDateVendTax(IdosConstants.IDOSDTF.parse(applicableDateString));
                        } catch (ParseException e) {
                            log.log(Level.SEVERE, "Date cannot be parsed", e);
                            throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
                                    IdosConstants.TECHNICAL_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
                                    "Date cannot be parsed");
                        }
                    }
                } else {
                    vennSpecf.setApplicableDateVendTax(new Date());
                }
                newVendorSpecifics.add(vennSpecf);
            }
        }
        List<List<VendorSpecific>> businessEntityVendSpecfTransactionList = ListUtility
                .getVendSpecificsTransactionList(oldVendorSpecifics, newVendorSpecifics);
        for (int i = 0; i < businessEntityTransactionList.size(); i++) {
            if (i == 0) {
                List<VendorSpecific> oldVendorSpecf = businessEntityVendSpecfTransactionList.get(i);
                if (oldVendorSpecf != null) {
                    for (VendorSpecific vendspecf : oldVendorSpecf) {
                        entityManager.remove(vendspecf);
                    }
                }
            }
            if (i == 1) {
                List<VendorSpecific> newVendorSpecf = businessEntityVendSpecfTransactionList.get(i);
                if (newVendorSpecf != null) {
                    for (VendorSpecific vendspecf : newVendorSpecf) {
                        genericDao.saveOrUpdate(vendspecf, users, entityManager);
                    }
                }
            }
            if (i == 2) {
                List<VendorSpecific> updateVendorSpecf = businessEntityVendSpecfTransactionList.get(i);
                if (updateVendorSpecf != null) {
                    for (VendorSpecific vendspecf : updateVendorSpecf) {
                        genericDao.saveOrUpdate(vendspecf, users, entityManager);
                    }
                }
            }
        }

        // RCM TAX SETUP
        for (int j = 0; j < newVendBnchs.length; j++) {
            for (int i = 0; i < newvendspecf.length; i++) {
                if (newvendspecf[i] != null && !newvendspecf[i].equals("")) {
                    Branch branch = null;
                    Specifics specific = null;
                    Double gstTaxRate = null;
                    Double cessRate = null;
                    Date applicableDate = new Date();
                    if (newVendBnchs.length > j) {
                        if (!newVendBnchs[j].equals("") && newVendBnchs[j] != null) {
                            branch = Branch.findById(IdosUtil.convertStringToLong(newVendBnchs[j]));
                        }
                    }
                    if (newvendspecf.length > i) {
                        if (!newvendspecf[i].equals("") && newvendspecf[i] != null) {
                            specific = Specifics.findById(IdosUtil.convertStringToLong(newvendspecf[i]));
                        }
                    }
                    if (newvendTaxRate.length > i) {
                        if (!newvendTaxRate[i].equals("") && newvendTaxRate[i] != null) {
                            gstTaxRate = IdosUtil.convertStringToDouble(newvendTaxRate[i]);
                        }
                    }
                    if (newvendCessRate.length > i) {
                        if (!newvendCessRate[i].equals("") && newvendCessRate[i] != null) {
                            cessRate = IdosUtil.convertStringToDouble(newvendCessRate[i]);
                        }
                    }
                    if (newvendApplicableDate.length > i) {
                        if (!newvendApplicableDate[i].equals("") && newvendApplicableDate[i] != null) {
                            try {
                                Date date = new Date();
                                String dateString = IdosConstants.IDOSDF
                                        .format(IdosConstants.IDOSDF.parse(newvendApplicableDate[i]));
                                String timeString = IdosConstants.TIMEFMT.format(date);
                                String applicableDateString = dateString + " " + timeString;
                                applicableDate = IdosConstants.IDOSDTF.parse(applicableDateString);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                log.log(Level.SEVERE, "Date cannot be parsed", e);
                                throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
                                        IdosConstants.TECHNICAL_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
                                        "Date cannot be parsed");
                            }
                        }
                    }
                    try {
                        VendorSpecific vennSpecf = null;
                        if (specific != null) {
                            vennSpecf = VendorSpecific.findByVendorAndSpecific(entityManager,
                                    users.getOrganization().getId(), users.getBranch().getId(), vend.getId(),
                                    specific.getId());
                        }
                        if (vennSpecf == null) {
                            vennSpecf = new VendorSpecific();
                        }
                        Particulars newVendParticulars = specific.getParticularsId();
                        vennSpecf.setVendorSpecific(vend);
                        vennSpecf.setSpecificsVendors(specific);
                        vennSpecf.setBranch(users.getBranch());
                        vennSpecf.setOrganization(users.getOrganization());
                        vennSpecf.setRcmTaxRateVend(gstTaxRate);
                        vennSpecf.setCessTaxRateVend(cessRate);
                        vennSpecf.setParticulars(newVendParticulars);
                        genericDao.saveOrUpdate(vennSpecf, users, entityManager);
                        GST_TAX_DAO.saveRcmTaxBranchVendor(branch, vend, specific, gstTaxRate, cessRate, applicableDate,
                                entityManager, users);
                    } catch (IDOSException e) {
                        log.log(Level.SEVERE, users.getEmail(), e);
                        e.printStackTrace();
                        throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
                                IdosConstants.BUSINESS_EXCEPTION,
                                e.getErrorDescription(), e.getErrorText());
                    }
                }
            }
        }
        log.log(Level.FINE, "******** End ");
        return true;
    }
}
