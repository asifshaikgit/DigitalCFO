package com.idos.dao;

import actor.VendorTransactionActor;
import com.idos.util.CountryCurrencyUtil;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import com.idos.util.ListUtility;
import java.util.logging.Level;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.WebSocket;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.text.ParseException;
import java.util.*;
import javax.inject.Inject;
import play.db.jpa.JPAApi;
import akka.stream.javadsl.*;
import akka.actor.*;
import akka.NotUsed;

/**
 * Created by Sunil Namdev on 02-02-2017.
 */
public class CustomerDAOImpl implements CustomerDAO {
    private static JPAApi jpaApi;

    @Override
    public Vendor saveCustomer(JsonNode json, Users users, EntityManager entityManager, EntityTransaction transaction,
            int customerType, int txnWalkinCustomerType) throws IDOSException {
        Vendor vend = null;
        try {
            log.log(Level.FINE, "********** start " + json);
            String vendorId = json.findValue("customerId") != null ? json.findValue("customerId").asText() : null;
            String daysOfCredit = json.findValue("daysOfCredit") != null ? json.findValue("daysOfCredit").asText()
                    : null;
            String futurePayAllowed = json.findValue("customerfutPayAlwd") == null ? null
                    : json.findValue("customerfutPayAlwd").asText();
            String vendName = json.findValue("custName") == null ? null : json.findValue("custName").asText();
            // String customerCode=json.findValue("custCode")==null? null :
            // json.findValue("custCode").asText();
            if (vendName == null) {
                vendName = json.findValue("txnforunavailablecustomer") == null ? null
                        : json.findValue("txnforunavailablecustomer").asText(); // read from transaction for sell
            }
            if (vendorId == null && vendName != null) {// VendorId null means not Edit Vendor, so if same vendName is
                                                       // used for Add then give error
                Vendor exisitngCustWithSameName = Vendor.findByOrgIdTypeName(entityManager,
                        users.getOrganization().getId(), IdosConstants.CUSTOMER, vendName);
                if (exisitngCustWithSameName != null) {
                    throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                            IdosConstants.INVALID_DATA_EXCEPTION, "Customer Name Exist");
                }
            }
            String gstinCode = json.findValue("gstinCode") == null ? null : json.findValue("gstinCode").asText();
            String shipcustState = json.findValue("shipcustState") == null ? null
                    : json.findValue("shipcustState").asText();
            String shipcustStateCode = json.findValue("shipcustStateCode") == null ? null
                    : json.findValue("shipcustStateCode").asText();
            Integer custBusinessIndividual = json.findValue("custBusinessIndividual") != null
                    ? json.findValue("custBusinessIndividual").asInt()
                    : 0;
            Integer custRegisteredOrUnReg = json.findValue("custRegisteredOrUnReg") != null
                    ? json.findValue("custRegisteredOrUnReg").asInt()
                    : 0;
            if (customerType == IdosConstants.WALK_IN_CUSTOMER) {
                if (txnWalkinCustomerType == 1 || txnWalkinCustomerType == 2) {
                    custRegisteredOrUnReg = 1;
                } else {
                    custRegisteredOrUnReg = 0;
                }
            }
            gstinCode = IdosUtil.gstinValidate(custRegisteredOrUnReg, gstinCode, vendorId);
            if (gstinCode != null && !"".equals(gstinCode) && (shipcustState == null || "".equals(shipcustState))) {
                throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                        IdosConstants.INVALID_DATA_EXCEPTION, "Invalid GSTIN state.");
            }
            // PBI 477 changes
            if (vendorId == null && gstinCode != null && !"".equals(gstinCode)) {
                Vendor existingCustWithSameGST = Vendor.findByOrgIdTypeGST(entityManager,
                        users.getOrganization().getId(), IdosConstants.CUSTOMER, gstinCode);
                if (existingCustWithSameGST != null) {
                    throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                            IdosConstants.INVALID_DATA_EXCEPTION, "Customer GSTIN Exist");
                }
            }
            if (gstinCode == null || "".equals(gstinCode)) {
                gstinCode = shipcustStateCode;
            }
            if (gstinCode == null || "".equals(gstinCode)) {
                gstinCode = json.findValue("txnDestinGstin").asText();
                if (gstinCode != null && shipcustStateCode == null) {
                    shipcustStateCode = gstinCode.substring(0, 2);
                }
            }
            String newVendEmail = json.findValue("custEmail") == null ? null : json.findValue("custEmail").asText();
            String newVendPhone = json.findValue("customerPhone") == null ? null
                    : json.findValue("customerPhone").asText();
            String newVendPhoneCtryCode = json.findValue("customerPhnCtryCode") == null ? null
                    : json.findValue("customerPhnCtryCode").asText();
            String customerAddress = json.findValue("customerAddress") == null ? null
                    : json.findValue("customerAddress").asText();
            Integer newVendCountry = json.findValue("customerCountry") == null ? null
                    : json.findValue("customerCountry").asInt();
            String newVendLocation = json.findValue("customerLocation") == null ? null
                    : json.findValue("customerLocation").asText();
            String customerState = json.findValue("customerState") == null ? null
                    : json.findValue("customerState").asText();
            String customerStateCode = json.findValue("customerStateCode") == null ? null
                    : json.findValue("customerStateCode").asText();
            String pricelistdoc = json.findPath("customerContractDoc") != null
                    ? json.findPath("customerContractDoc").asText()
                    : null;
            String discount = json.findPath("customerDiscount") != null ? json.findPath("customerDiscount").asText()
                    : null;
            String customerItems = json.findValue("customerItems") == null ? null
                    : json.findValue("customerItems").asText();
            String openingBalance = json.findValue("openingBalance") == null ? null
                    : json.findValue("openingBalance").asText();
            Integer custTranExceedCredLim = json.findValue("custTranExceedCredLim") == null ? 0
                    : json.findValue("custTranExceedCredLim").asInt();
            Double custCreditLimit = json.findValue("custCreditLimit") == null ? 0
                    : json.findValue("custCreditLimit").asDouble();
            Integer exculdeAdvCreLimCheck = json.findValue("exculdeAdvCreLimCheck") == null ? 0
                    : json.findValue("exculdeAdvCreLimCheck").asInt();
            String openingBalanceAdvPaid = json.findValue("openingBalanceAdvPaid") == null ? null
                    : json.findValue("openingBalanceAdvPaid").asText();
            String custItemsDiscount = json.findValue("custItemsDiscount") == null ? null
                    : json.findValue("custItemsDiscount").asText();
            String custStatutoryName1 = json.findValue("custStatutoryName1") == null ? null
                    : json.findValue("custStatutoryName1").asText();
            String custStatutoryNumber1 = json.findValue("custStatutoryNumber1") == null ? null
                    : json.findValue("custStatutoryNumber1").asText();
            String custStatutoryName2 = json.findValue("custStatutoryName2") == null ? null
                    : json.findValue("custStatutoryName2").asText();
            String custStatutoryNumber2 = json.findValue("custStatutoryNumber2") == null ? null
                    : json.findValue("custStatutoryNumber2").asText();
            String custStatutoryName3 = json.findValue("custStatutoryName3") == null ? null
                    : json.findValue("custStatutoryName3").asText();
            String custStatutoryNumber3 = json.findValue("custStatutoryNumber3") == null ? null
                    : json.findValue("custStatutoryNumber3").asText();
            String custStatutoryName4 = json.findValue("custStatutoryName4") == null ? null
                    : json.findValue("custStatutoryName4").asText();
            String custStatutoryNumber4 = json.findValue("custStatutoryNumber4") == null ? null
                    : json.findValue("custStatutoryNumber4").asText();
            String customerSelGroup = json.findValue("customerSelGroup") == null ? null
                    : json.findValue("customerSelGroup").asText();
            Object billwiseOpeningBalance = json.findValue("billwiseOpeningBalance") == null ? null
                    : json.findValue("billwiseOpeningBalance");
            Object branchWiseAdvBalData = json.findValue("branchWiseAdvBalance") == null ? null
                    : json.findValue("branchWiseAdvBalance");
            Integer placeOfSupplyForOrg = json.findValue("placeOfSupplyForOrg") == null ? 0
                    : json.findValue("placeOfSupplyForOrg").asInt();
            if (customerType == IdosConstants.WALK_IN_CUSTOMER) {
                vend = Vendor.findByOrgIdTypeName(entityManager, users.getOrganization().getId(),
                        IdosConstants.WALK_IN_CUSTOMER, vendName.toUpperCase());
                if (vend == null) {
                    vend = new Vendor();
                }
            } else {
                if (vendorId == null) {
                    vend = new Vendor();
                } else {
                    vend = Vendor.findById(IdosUtil.convertStringToLong(vendorId));
                }
            }
            boolean isShippingAddressSame = json.findValue("isShippingAddressSame") == null ? false
                    : json.findValue("isShippingAddressSame").asBoolean();

            if (customerType == IdosConstants.WALK_IN_CUSTOMER) {
                vend.setName(vendName.toUpperCase());
            } else {
                vend.setName(vendName);
            }
            /*
             * if(customerCode != null){
             * vend.setCustomerCode(customerCode);
             * }
             */
            if (custRegisteredOrUnReg == 1) {
                vend.setGstin(gstinCode);
            } else {
                vend.setGstin(null);
            }
            if (custBusinessIndividual != null) {
                vend.setIsBusiness(custBusinessIndividual);
            }
            if (custRegisteredOrUnReg != null) {
                vend.setIsRegistered(custRegisteredOrUnReg);
            }
            if (customerSelGroup != null && !customerSelGroup.equals("")) {
                VendorGroup vendGroup = VendorGroup.findById(IdosUtil.convertStringToLong(customerSelGroup));
                vend.setVendorGroup(vendGroup);
            }
            if (newVendCountry != null) {
                vend.setCountry(newVendCountry);
            }
            if (placeOfSupplyForOrg != null) {
                vend.setPlaceOfSupplyType(placeOfSupplyForOrg);
            }
            if (discount != null && !discount.equals("")) {
                vend.setCustomerRemarks(discount);
            }
            if (customerState != null) {
                vend.setCountryState(customerState);
            }
            // if (openingBalance != null && openingBalance != "") {
            // vend.setTotalOriginalOpeningBalance(IdosUtil.convertStringToDouble(openingBalance));
            // vend.setTotalOpeningBalance(IdosUtil.convertStringToDouble(openingBalance));
            // } else {
            // vend.setTotalOriginalOpeningBalance(0.00);
            // vend.setTotalOpeningBalance(0.00);
            // }

            // if (openingBalanceAdvPaid != null && openingBalanceAdvPaid != "") {
            // vend.setTotalOpeningBalanceAdvPaid(IdosUtil.convertStringToDouble(openingBalanceAdvPaid));
            // vend.setTotalOriginalOpeningBalanceAdvPaid(IdosUtil.convertStringToDouble(openingBalanceAdvPaid));
            // } else {
            // vend.setTotalOpeningBalanceAdvPaid(0.00);
            // vend.setTotalOriginalOpeningBalanceAdvPaid(0.00);
            // }
            if (openingBalance != null && openingBalance != "") {
                vend.setTotalOriginalOpeningBalance(IdosUtil.convertStringToDouble(openingBalance));
                vend.setTotalOpeningBalance(vend.getTotalOriginalOpeningBalance());
            }
            if (openingBalanceAdvPaid != null && openingBalanceAdvPaid != "") {
                vend.setTotalOpeningBalanceAdvPaid(IdosUtil.convertStringToDouble(openingBalanceAdvPaid));
                vend.setTotalOriginalOpeningBalanceAdvPaid(vend.getTotalOpeningBalanceAdvPaid());
            }
            vend.setCreditLimit(custCreditLimit);
            vend.setExceedingCreditProcessStop(custTranExceedCredLim);
            vend.setExcludeAdvFromCreLimCheck(exculdeAdvCreLimCheck);
            if (custStatutoryName1 != null && !custStatutoryName1.equals("")) {
                vend.setStatutoryName1(custStatutoryName1);
            }
            if (custStatutoryNumber1 != null && !custStatutoryNumber1.equals("")) {
                vend.setStatutoryNumber1(custStatutoryNumber1);
            }
            if (custStatutoryName2 != null && !custStatutoryName2.equals("")) {
                vend.setStatutoryName2(custStatutoryName2);
            }
            if (custStatutoryNumber2 != null && !custStatutoryNumber2.equals("")) {
                vend.setStatutoryNumber2(custStatutoryNumber2);
            }
            if (custStatutoryName3 != null && !custStatutoryName3.equals("")) {
                vend.setStatutoryName3(custStatutoryName3);
            }
            if (custStatutoryNumber3 != null && !custStatutoryNumber3.equals("")) {
                vend.setStatutoryNumber3(custStatutoryNumber3);
            }
            if (custStatutoryName4 != null && !custStatutoryName4.equals("")) {
                vend.setStatutoryName4(custStatutoryName4);
            }
            if (custStatutoryNumber4 != null && !custStatutoryNumber4.equals("")) {
                vend.setStatutoryNumber4(custStatutoryNumber4);
            }
            if (newVendLocation != null) {
                vend.setLocation(newVendLocation);
            }
            vend.setEmail(newVendEmail);
            vend.setPhoneCtryCode(newVendPhoneCtryCode);
            // vend.setPriceListDoc(pricelistdoc);
            String audDocument = "";

            /*
             * if (pricelistdoc != null && !pricelistdoc.equals("")) {
             * String pricelistdocUploadsArray[] = pricelistdoc.split(",");
             * for (int i = 0; i <pricelistdocUploadsArray.length; i++) {
             * if (audDocument.equals("")) {
             * audDocument += users.getEmail() + "#" + pricelistdocUploadsArray[i];
             * } else {
             * audDocument += "," + users.getEmail() + "#" + pricelistdocUploadsArray[i];
             * }
             * }
             * vend.setPriceListDoc(audDocument);
             * }
             */
            vend.setPriceListDoc(TRANSACTION_DAO.getAndDeleteSupportingDocument(vend.getPriceListDoc(),
                    users.getEmail(), pricelistdoc, users, entityManager));
            vend.setPhone(newVendPhone);
            vend.setType(customerType);
            if (futurePayAllowed != null && !"".equals(futurePayAllowed)) {
                vend.setPurchaseType(IdosUtil.convertStringToInt(futurePayAllowed));
            }
            if (daysOfCredit != null && !"".equals(daysOfCredit)) {
                vend.setDaysForCredit(IdosUtil.convertStringToInt(daysOfCredit));
            }
            if (customerAddress != null) {
                vend.setAddress(customerAddress);
            }
            vend.setBranch(users.getBranch());
            vend.setOrganization(users.getOrganization());
            String custItems = null;
            String custDiscount = null;
            if (customerItems != null && !customerItems.equals("")) {
                custItems = customerItems.substring(0, customerItems.length() - 1);
            }
            if (custItemsDiscount != null && !custItemsDiscount.equals("")) {
                custDiscount = custItemsDiscount.substring(0, custItemsDiscount.length() - 1);
            }
            vend.setCustomerSpecifics(custItems);
            vend.setCustomerSpecificsDiscountPercentage(custDiscount);
            if (!users.getUserRolesName().contains("MASTER ADMIN")) {
                vend.setPresentStatus(0);
            } else {
                vend.setPresentStatus(1);
            }
            genericDao.saveOrUpdate(vend, users, entityManager);
            FILE_UPLOAD_DAO.updateUploadFileLogs(entityManager, users, pricelistdoc, vend.getId(),
                    IdosConstants.CUSTOMER_MODULE);
            // save CustomerDetail for Main GSTIN
            CustomerDetail customerDetail = null;
            if (customerType == IdosConstants.WALK_IN_CUSTOMER || vend.getIsRegistered() == 0) {
                customerDetail = CustomerDetail.findByCustomerID(entityManager, vend.getId());
            } else {
                customerDetail = CustomerDetail.findByCustomerGSTNID(entityManager, vend.getId(), gstinCode);
            }
            boolean isNewCustomer = false;
            if (customerDetail == null) {
                customerDetail = new CustomerDetail();
                isNewCustomer = true;
            }

            if ((customerType == IdosConstants.WALK_IN_CUSTOMER && isNewCustomer)
                    || (IdosConstants.CUSTOMER == customerType)) {
                customerDetail.setCustomer(vend);
                if (vend.getIsRegistered() == 1) {
                    customerDetail.setGstin(gstinCode);
                } else {
                    customerDetail.setGstin(null);
                }

                customerDetail.setOrganization(users.getOrganization());
                customerDetail.setBillingphoneCtryCode(newVendPhoneCtryCode);
                customerDetail.setBillingphone(newVendPhone);
                // 3 or 4 Unregistered - Place of Supply Not Available
                if (txnWalkinCustomerType == 3 || txnWalkinCustomerType == 4) { // Then Branch address should show
                                                                                // respective fields
                    Branch branch = null;
                    if (newVendLocation != null) {
                        List<Branch> branchList = Branch.findByName(entityManager, users.getOrganization(),
                                newVendLocation);
                        branch = branchList.get(0);
                    } else {
                        branch = users.getBranch();
                    }
                    String branchAdd = branch.getAddress();
                    String branchStateCode = branch.getStateCode();
                    String branchState = IdosConstants.STATE_CODE_MAPPING.get(branchStateCode);
                    String branchLocation = branch.getLocation();
                    Integer countryCode = branch.getCountry();
                    // customerDetail.
                    customerDetail.setBillingcountry(countryCode);
                    customerDetail.setBillinglocation(branchLocation);
                    customerDetail.setBillingaddress(branchAdd);
                    customerDetail.setBillingState(branchState);
                    customerDetail.setBillingStateCode(branchStateCode);
                    customerDetail.setShippingaddress(branchAdd);
                    customerDetail.setShippingcountry(countryCode);
                    customerDetail.setShippinglocation(branchLocation);
                    customerDetail.setShippingphone(newVendPhone);
                    customerDetail.setShippingphoneCtryCode(newVendPhoneCtryCode);
                    customerDetail.setShippingState(branchState);
                    customerDetail.setShippingStateCode(branchStateCode);
                } else {
                    customerDetail.setBillingcountry(newVendCountry);
                    customerDetail.setBillinglocation(newVendLocation);
                    customerDetail.setBillingaddress(customerAddress);
                    if (customerState == null && customerStateCode != null) {
                        customerState = IdosConstants.STATE_CODE_MAPPING.get(customerStateCode);
                    }
                    customerDetail.setBillingState(customerState);
                    customerDetail.setBillingStateCode(customerStateCode);

                    if (!isShippingAddressSame) {
                        String shipVendPhone = json.findValue("shipcustPhone") == null ? null
                                : json.findValue("shipcustPhone").asText();
                        String shipVendPhoneCtryCode = json.findValue("shipcustPhnCtryCode") == null ? null
                                : json.findValue("shipcustPhnCtryCode").asText();
                        String shipcustomerAddress = json.findValue("shipcustAddress") == null ? null
                                : json.findValue("shipcustAddress").asText();
                        Integer shipVendCountry = json.findValue("shipcustCountry") == null ? null
                                : json.findValue("shipcustCountry").asInt();
                        String shipVendLocation = json.findValue("shipcustLocation") == null ? null
                                : json.findValue("shipcustLocation").asText();
                        if (customerType == IdosConstants.WALK_IN_CUSTOMER) {
                            shipVendLocation = newVendLocation;
                        }
                        customerDetail.setIsSameAsBillingAddress(0);
                        customerDetail.setShippingaddress(shipcustomerAddress);
                        customerDetail.setShippingcountry(shipVendCountry);
                        customerDetail.setShippinglocation(shipVendLocation);
                        customerDetail.setShippingphone(shipVendPhone);
                        customerDetail.setShippingphoneCtryCode(shipVendPhoneCtryCode);
                        if (shipcustState == null && shipcustStateCode != null) {
                            shipcustState = IdosConstants.STATE_CODE_MAPPING.get(shipcustStateCode);
                        }
                        customerDetail.setShippingState(shipcustState);
                        customerDetail.setShippingStateCode(shipcustStateCode);
                    } else {
                        customerDetail.setIsSameAsBillingAddress(1);
                        customerDetail.setShippingaddress(customerAddress);
                        customerDetail.setShippingcountry(newVendCountry);
                        customerDetail.setShippinglocation(newVendLocation);
                        customerDetail.setShippingphone(newVendPhone);
                        customerDetail.setShippingphoneCtryCode(newVendPhoneCtryCode);
                        customerDetail.setShippingState(customerState);
                        customerDetail.setShippingStateCode(customerStateCode);
                    }
                }

                genericDao.saveOrUpdate(customerDetail, users, entityManager);
                if (customerType != IdosConstants.WALK_IN_CUSTOMER) {
                    saveCustomerDetail(json, vend, users, entityManager, transaction, customerDetail.getId());
                }
            }

            /***************** END customerDetails *****************/
            // Add Billwise Opening Balance

            BILLWISE_OPENING_BAL_DAO.saveCustomerOpeningBalance(billwiseOpeningBalance, vend, users, entityManager);
            // Add Branchwise Advance Balance
            // BRACHWISE_ADVANCE_BAL_DAO.saveCustomeAdvanceBalance(branchWiseAdvBalData,
            // vend,
            // users, entityManager);
            if (customerType != IdosConstants.WALK_IN_CUSTOMER) {
                ArrayList inparamList = new ArrayList(2);
                inparamList.add(vend.getName());
                inparamList.add(vend.getId());
                List<Vendor> vendSpecOpt = null;
                if (!users.getUserRolesName().contains("MASTER ADMIN")) {

                    vendSpecOpt = genericDao.queryWithParams(CUSTOMR_JQL_OTHERS, entityManager, inparamList);
                } else {
                    vendSpecOpt = genericDao.queryWithParams(CUSTOMR_JQL_MASTER, entityManager, inparamList);

                }
                Vendor enteredVendor = null;
                if (vendSpecOpt != null) {
                    for (Vendor vendSpec : vendSpecOpt) {
                        enteredVendor = vendSpec;
                    }
                }
                // Map<String, ActorRef> orgvendvendregistrered = new HashMap<String,
                // ActorRef>();
                // Object[] keyArray =
                // VendorTransactionActor.vendvendregistrered.keySet().toArray();
                // for (int i = 0; i < keyArray.length; i++) {
                // StringBuilder sbquery = new StringBuilder(
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
                Map<String, String> countryMap = CountryCurrencyUtil.getCountries();
                Map<String, String> countries = new TreeMap<String, String>();
                int count = 0;
                for (String country : countryMap.keySet()) {
                    // count++;
                    countries.put(String.valueOf(country), countryMap.get(country));
                }
                String vendorCountry = "";
                if (enteredVendor.getCountry() != null) {
                    vendorCountry = countries.get(enteredVendor.getCountry().toString());
                }
                String vendorAddress = "";
                if (enteredVendor.getAddress() != null) {
                    vendorAddress = enteredVendor.getAddress();
                }
                // VendorTransactionActor.add(enteredVendor.getId(), orgvendvendregistrered,
                // enteredVendor.getName(),
                // vendorAddress, enteredVendor.getLocation(), enteredVendor.getEmail(),
                // enteredVendor.getGrantAccess(), enteredVendor.getPhone(),
                // enteredVendor.getType(),
                // "vendorCustomer", enteredVendor.getPresentStatus());
                saveCustomerOtherInformation(json, vend, users, entityManager, transaction);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, users.getEmail(), ex);
            if (ex instanceof IDOSException) {
                throw ex;
            } else {
                throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                        "Error on storing Customer", ex.getMessage());
            }
        }
        log.log(Level.FINE, "******** End");
        return vend;
    }

    @Override
    public boolean saveCustomerShippingDetail(JsonNode json, Users user, EntityManager entityManager,
            EntityTransaction transaction) throws IDOSException {
        try {
            log.log(Level.FINE, "********** start " + json);
            Long vendorId = json.findValue("entitycustid") != null ? json.findValue("entitycustid").asLong() : null;
            String shipVendPhone = json.findValue("shipcustPhone").asText();
            String shipVendPhoneCtryCode = json.findValue("shipcustPhnCtryCode").asText();
            String shipcustomerAddress = json.findValue("shipcustAddress").asText();
            Integer shipVendCountry = json.findValue("shipcustCountry") == null ? null
                    : json.findValue("shipcustCountry").asInt();
            String shipVendLocation = json.findValue("shipcustLocation").asText();
            Vendor vend = null;
            if (vendorId != null) {
                vend = Vendor.findById(vendorId);
            } else {
                throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                        IdosConstants.NULL_KEY_EXC_ESMF_MSG, "Customer not found.");
            }
            CustomerDetail customerDetail = CustomerDetail.findByCustomerID(entityManager, vend.getId());
            if (customerDetail == null) {
                customerDetail = new CustomerDetail();
            }
            customerDetail.setOrganization(user.getOrganization());
            customerDetail.setCustomer(vend);
            customerDetail.setShippingaddress(shipcustomerAddress);
            customerDetail.setShippingcountry(shipVendCountry);
            customerDetail.setShippinglocation(shipVendLocation);
            customerDetail.setShippingphone(shipVendPhone);
            customerDetail.setShippingphoneCtryCode(shipVendPhoneCtryCode);
            genericDao.saveOrUpdate(customerDetail, user, entityManager);
        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
            throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF, IdosConstants.TECHNICAL_EXCEPTION,
                    IdosConstants.NULL_KEY_EXC_ESMF_MSG, "Error on saving shipping address.");
        }
        return true;
    }

    private boolean saveCustomerOtherInformation(JsonNode json, Vendor vend, Users users, EntityManager entityManager,
            EntityTransaction transaction) throws IDOSException {
        log.log(Level.FINE, "******** Start");
        try {
            List<BranchVendors> oldVendorBranches = vend.getVendorBranches();
            List<BranchVendors> newVendorBranches = new ArrayList<BranchVendors>();
            String vendorBnchs = json.findValue("custBranches").asText();
            String vendorItems = json.findValue("customerItems").asText();
            String custItemsDiscount = json.findValue("custItemsDiscount").asText();
            String vendRcmApplicableDateItems = json.findValue("vendRcmApplicableDateItems") == null ? null
                    : json.findValue("vendRcmApplicableDateItems").asText();
            String vendRcmTaxRateForItems = json.findValue("vendRcmTaxRateForItems") == null ? null
                    : json.findValue("vendRcmTaxRateForItems").asText();
            String vendCessTaxRateForItems = json.findValue("vendCessTaxRateForItems") == null ? null
                    : json.findValue("vendCessTaxRateForItems").asText();

            String branchOpeningBalance = json.findValue("branchOpeningBalance") == null ? null
                    : json.findValue("branchOpeningBalance").asText();

            String branchOpeningBalanceAP = json.findValue("branchOpeningBalanceAP") == null ? null
                    : json.findValue("branchOpeningBalanceAP").asText();
            String newVendBnchs[] = vendorBnchs.split(",");
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
                    if (branchOpeningBalanceArr.length > i && branchOpeningBalanceArr[i] != null
                            && !"".equals(branchOpeningBalanceArr[i])) {
                        newBnchVend
                                .setOriginalOpeningBalance(IdosUtil.convertStringToDouble(branchOpeningBalanceArr[i]));
                        newBnchVend.setOpeningBalance(IdosUtil.convertStringToDouble(branchOpeningBalanceArr[i]));
                    } else {
                        newBnchVend.setOriginalOpeningBalance(0.0);
                        newBnchVend.setOpeningBalance(0.0);
                    }
                    if (branchOpeningBalanceAPArr.length > i && branchOpeningBalanceAPArr[i] != null
                            && !"".equals(branchOpeningBalanceAPArr[i])) {
                        newBnchVend.setOriginalOpeningBalanceAdvPaid(
                                IdosUtil.convertStringToDouble(branchOpeningBalanceAPArr[i]));
                        newBnchVend
                                .setOpeningBalanceAdvPaid(IdosUtil.convertStringToDouble(branchOpeningBalanceAPArr[i]));
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
            String newCustSpecf = "";
            String newCustDiscPerc = "";
            String newCustRcmTax = "";
            String newCustCessTax = "";
            String newRcmApplicableDate = "";
            List<VendorSpecific> oldVendorSpecifics = vend.getVendorsSpecifics();
            List<VendorSpecific> newVendorSpecifics = new ArrayList<VendorSpecific>();
            if (vendorItems != null && !vendorItems.equals("")) {
                newCustSpecf = vendorItems.substring(0, vendorItems.length() - 1);
                newCustDiscPerc = custItemsDiscount.substring(0, custItemsDiscount.length() - 1);
                if (vendRcmTaxRateForItems != null) {
                    newCustRcmTax = vendRcmTaxRateForItems.substring(0, vendorItems.length() - 1);
                }
                if (vendCessTaxRateForItems != null) {
                    newCustCessTax = vendCessTaxRateForItems.substring(0, vendorItems.length() - 1);
                }
                if (vendRcmApplicableDateItems != null) {
                    newRcmApplicableDate = vendRcmApplicableDateItems.substring(0, vendorItems.length() - 1);
                }
            }
            String newvendspecf[] = newCustSpecf.split(",");
            String newcustspecfdisc[] = newCustDiscPerc.split(",");
            String newRcmApplicableDateSpecfdisc[] = newRcmApplicableDate.split(",");
            String newCustRcmTaxSpecfdisc[] = newCustRcmTax.split(",");
            String newCustCessTaxSpecfdisc[] = newCustCessTax.split(",");
            for (int i = 0; i < newvendspecf.length; i++) {
                VendorSpecific vennSpecf = new VendorSpecific();
                if (!newvendspecf[i].equals("")) {
                    Specifics newVendSpecfics = Specifics.findById(IdosUtil.convertStringToLong(newvendspecf[i]));
                    Particulars newVendParticulars = newVendSpecfics.getParticularsId();
                    vennSpecf.setVendorSpecific(vend);
                    vennSpecf.setSpecificsVendors(newVendSpecfics);
                    vennSpecf.setBranch(users.getBranch());
                    vennSpecf.setOrganization(users.getOrganization());
                    vennSpecf.setParticulars(newVendParticulars);
                    if (newcustspecfdisc.length > i) {
                        if (!newcustspecfdisc[i].equals("") && newcustspecfdisc[i] != null) {
                            vennSpecf.setDiscountPercentage(IdosUtil.convertStringToDouble(newcustspecfdisc[i]));
                        }
                    }

                    if (newCustRcmTaxSpecfdisc.length > i) {
                        if (!newCustRcmTaxSpecfdisc[i].equals("") && newCustRcmTaxSpecfdisc[i] != null) {
                            vennSpecf.setRcmTaxRateVend(IdosUtil.convertStringToDouble(newCustRcmTaxSpecfdisc[i]));
                        }
                    }

                    if (newCustCessTaxSpecfdisc.length > i) {
                        if (!newCustCessTaxSpecfdisc[i].equals("") && newCustCessTaxSpecfdisc[i] != null) {
                            vennSpecf.setCessTaxRateVend(IdosUtil.convertStringToDouble(newCustCessTaxSpecfdisc[i]));
                        }
                    }

                    if (newRcmApplicableDateSpecfdisc.length > i) {
                        if (!newRcmApplicableDateSpecfdisc[i].equals("") && newRcmApplicableDateSpecfdisc[i] != null) {
                            try {
                                Date date = new Date();
                                String dateString = IdosConstants.IDOSDF
                                        .format(IdosConstants.IDOSDF.parse(newRcmApplicableDateSpecfdisc[i]));
                                String timeString = IdosConstants.TIMEFMT.format(date);
                                String applicableDateString = dateString + " " + timeString;
                                vennSpecf.setApplicableDateVendTax(IdosConstants.IDOSDTF.parse(applicableDateString));
                            } catch (ParseException e) {
                                throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE,
                                        IdosConstants.DATA_FORMAT_EXCEPTION, "Data Format Exception for Date!",
                                        IdosConstants.DATA_FORMAT_ERRCODE);
                            }
                        }
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
                if (i == 3) {
                    List<VendorSpecific> updateCustomerSpecf = businessEntityVendSpecfTransactionList.get(i);
                    if (updateCustomerSpecf != null) {
                        for (VendorSpecific vendspecf : updateCustomerSpecf) {
                            genericDao.saveOrUpdate(vendspecf, users, entityManager);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, users.getEmail(), ex);
            throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF, IdosConstants.TECHNICAL_EXCEPTION,
                    IdosConstants.NULL_KEY_EXC_ESMF_MSG, "Error on saving other customer information.");
        }
        log.log(Level.FINE, "******** End");
        return true;
    }

    private boolean saveCustomerDetail(JsonNode json, Vendor customer, Users user, EntityManager entityManager,
            EntityTransaction transaction, long mainCustomerDetailID) throws IDOSException {
        log.log(Level.FINE, "********** start " + json);
        String customerDetailIdListHid = json.findValue("customerDetailIdListHid").asText();
        String gstinCodeHid = json.findValue("gstinCodeHid").asText();
        String customerAddressHid = json.findValue("customerAddressHid").asText();
        String customercountryCodeHid = json.findValue("customercountryCodeHid").asText();
        String custstateHid = json.findValue("custstateHid").asText();
        String custstatecodeHid = json.findValue("custstatecodeHid").asText();
        String custlocationHid = json.findValue("customerAddressHid").asText();
        String custPhnNocountryCodeHid = json.findValue("custPhnNocountryCodeHid").asText();
        String custphone1Hid = json.findValue("custphone1Hid").asText();
        String custphone2Hid = json.findValue("custphone2Hid").asText();
        String custphone3Hid = json.findValue("custphone3Hid").asText();
        String isShippingAddressSameHid = json.findValue("isShippingAddressSameHid").asText();
        String shipcustomerAddressHid = json.findValue("shipcustomerAddressHid").asText();
        String shipcustomerCountryCodeHid = json.findValue("shipcustomerCountryCodeHid").asText();
        String shipstateHid = json.findValue("shipstateHid").asText();
        String shipcustStateCodeHid = json.findValue("shipcustStateCodeHid").asText();
        String shiptlocationHid = json.findValue("shiptlocationHid").asText();
        String shipcustPhnNoCountryCodeHid = json.findValue("shipcustPhnNoCountryCodeHid").asText();
        String shipcustPhone1Hid = json.findValue("shipcustPhone1Hid").asText();
        String shipcustPhone2Hid = json.findValue("shipcustPhone2Hid").asText();
        String shipcustPhone3Hid = json.findValue("shipcustPhone3Hid").asText();
        String gstinCheckedValues = json.findValue("gstinCheckedValues").asText();
        String customerDetailIdArray[] = customerDetailIdListHid.split("\\|", -1);
        String gstinCodeArray[] = gstinCodeHid.split("\\|", -1);
        String customerAddressArray[] = customerAddressHid.split("\\|", -1);
        String customercountryCodeArray[] = customercountryCodeHid.split("\\|", -1);
        String custstateArray[] = custstateHid.split("\\|", -1);
        String custstatecodeArray[] = custstatecodeHid.split("\\|", -1);

        String custlocationArray[] = custlocationHid.split("\\|", -1);
        String custPhnNocountryCodeArray[] = custPhnNocountryCodeHid.split("\\|", -1);
        String custphone1Array[] = custphone1Hid.split("\\|", -1);
        String custphone2Array[] = custphone2Hid.split("\\|", -1);
        String custphone3Array[] = custphone3Hid.split("\\|", -1);
        String isShippingAddressSameArray[] = isShippingAddressSameHid.split("\\|", -1);
        String shipcustomerAddressArray[] = shipcustomerAddressHid.split("\\|", -1);
        String shipcustomerCountryCodeArray[] = shipcustomerCountryCodeHid.split("\\|", -1);
        String shipstateArray[] = shipstateHid.split("\\|", -1);
        String shipcustStateCodeArray[] = shipcustStateCodeHid.split("\\|", -1);
        String shiptlocationArray[] = shiptlocationHid.split("\\|", -1);
        String shipcustPhnNoCountryCodeArray[] = shipcustPhnNoCountryCodeHid.split("\\|", -1);
        String shipcustPhone1Array[] = shipcustPhone1Hid.split("\\|", -1);
        String shipcustPhone2Array[] = shipcustPhone2Hid.split("\\|", -1);
        String shipcustPhone3Array[] = shipcustPhone3Hid.split("\\|", -1);
        String gstinCheckedArray[] = gstinCheckedValues.split(",");
        for (int i = 0; i < customerDetailIdArray.length; i++) {
            if (customerDetailIdArray[i] == null || "".equals(customerDetailIdArray[i])) {
                continue;
            }
            long custID = IdosUtil.convertStringToLong(customerDetailIdArray[i]);
            if (custID == mainCustomerDetailID) {
                continue; // skip main
            }
            CustomerDetail customerDetail = CustomerDetail.findByID(custID);
            if (customerDetail == null) {
                customerDetail = new CustomerDetail();
            }
            if (customer != null) {
                customerDetail.setCustomer(customer);
            }
            if ("true".equals(gstinCheckedArray[i])) {
                customerDetail.setPresentStatus(1);
            } else {
                customerDetail.setPresentStatus(0);
            }
            customerDetail.setOrganization(user.getOrganization());

            if (gstinCodeArray[i] == null || "".equals(gstinCodeArray[i])) {
                customerDetail.setGstin(shipcustStateCodeArray[i]);

            }
            gstinCodeArray[i] = IdosUtil.branchAndMultiGstinValidate(gstinCodeArray[i], customerDetailIdArray[i]);
            customerDetail.setGstin(gstinCodeArray[i]);
            customerDetail.setBillingaddress(customerAddressArray[i]);
            Integer countryCode = 0;
            if (customercountryCodeArray[i] != null && !"".equals(customercountryCodeArray[i])) {
                countryCode = new Integer(customercountryCodeArray[i]);
                customerDetail.setBillingcountry(countryCode);
            }
            customerDetail.setBillingState(custstateArray[i]);
            customerDetail.setBillingStateCode(custstatecodeArray[i]);
            customerDetail.setBillinglocation(custlocationArray[i]);
            customerDetail.setBillingphoneCtryCode(custPhnNocountryCodeArray[i]);
            StringBuilder phone = new StringBuilder();
            phone.append(custphone1Array[i]).append(custphone2Array[i]).append(custphone3Array[i]);
            customerDetail.setBillingphone(phone.toString());
            customerDetail.setShippingaddress(shipcustomerAddressArray[i]);
            if (shipcustomerCountryCodeArray[i] != null && !"".equals(shipcustomerCountryCodeArray[i])) {
                countryCode = new Integer(shipcustomerCountryCodeArray[i]);
                customerDetail.setShippingcountry(countryCode);
            }

            // if(!isShippingAddressSameArray[i].)
            if ("true".equals(isShippingAddressSameArray[i])) {
                customerDetail.setIsSameAsBillingAddress(1);
            } else {
                customerDetail.setIsSameAsBillingAddress(0);
            }
            customerDetail.setIsSameAsBillingAddress(1);
            customerDetail.setShippingState(shipstateArray[i]);
            customerDetail.setShippingStateCode(shipcustStateCodeArray[i]);
            customerDetail.setShippinglocation(shiptlocationArray[i]);
            StringBuilder shipPhone = new StringBuilder();
            shipPhone.append(shipcustPhone1Array[i]).append(shipcustPhone2Array[i]).append(shipcustPhone3Array[i]);
            customerDetail.setShippingphone(shipPhone.toString());
            customerDetail.setShippingphoneCtryCode(shipcustPhnNoCountryCodeArray[i]);

            genericDao.saveOrUpdate(customerDetail, user, entityManager);
            /***************** END customerDetails *****************/
        }
        log.log(Level.FINE, "******** End");
        return true;
    }

    @Override
    public boolean saveWalkinCustomerDetail(JsonNode json, String walkinCustomerName, Users user,
            EntityManager entityManager) throws IDOSException {
        try {
            log.log(Level.FINE, "********** start " + json);
            String isGstinAddedInTransHid = json.findValue("isGstinAddedInTransHid") == null ? null
                    : json.findValue("isGstinAddedInTransHid").asText();
            if (!"1".equals(isGstinAddedInTransHid)) {
                return true;
            }
            String gstinPart1 = json.findValue("gstinPart1") == null ? null : json.findValue("gstinPart1").asText();
            String gstinPart2 = json.findValue("gstinPart2") == null ? null : json.findValue("gstinPart2").asText();
            String addressModal = json.findValue("addressModal") == null ? null
                    : json.findValue("addressModal").asText();
            Integer countryModal = json.findValue("countryModal") == null ? null
                    : json.findValue("countryModal").asInt();
            String stateModal = json.findValue("stateModal") == null ? null : json.findValue("stateModal").asText();
            String locationModal = json.findValue("locationModal") == null ? null
                    : json.findValue("locationModal").asText();
            String phoneCountryCodeModal = json.findValue("phoneCountryCodeModal") == null ? null
                    : json.findValue("phoneCountryCodeModal").asText();
            String customerPhone1Modal = json.findValue("customerPhone1Modal") == null ? null
                    : json.findValue("customerPhone1Modal").asText();
            String customerPhone2Modal = json.findValue("customerPhone2Modal") == null ? null
                    : json.findValue("customerPhone2Modal").asText();
            String customerPhone3Modal = json.findValue("customerPhone3Modal") == null ? null
                    : json.findValue("customerPhone3Modal").asText();
            boolean isAddressSameModal = json.findValue("isAddressSameModal") == null ? null
                    : json.findValue("isAddressSameModal").asBoolean();
            String shipAddressModal = json.findValue("shipAddressModal") == null ? null
                    : json.findValue("shipAddressModal").asText();
            Integer shipCountryModal = json.findValue("shipCountryModal") == null ? null
                    : json.findValue("shipCountryModal").asInt();
            String shipStateModal = json.findValue("shipStateModal") == null ? null
                    : json.findValue("shipStateModal").asText();
            String shipLocationModal = json.findValue("shipLocationModal") == null ? null
                    : json.findValue("shipLocationModal").asText();
            String shipPhnNocountryCodeModal = json.findValue("shipPhnNocountryCodeModal") == null ? null
                    : json.findValue("shipPhnNocountryCodeModal").asText();
            String shipPhone1Modal = json.findValue("shipPhone1Modal") == null ? null
                    : json.findValue("shipPhone1Modal").asText();
            String shipPhone2Modal = json.findValue("shipPhone2Modal") == null ? null
                    : json.findValue("shipPhone2Modal").asText();
            String shipPhone3Modal = json.findValue("shipPhone3Modal") == null ? null
                    : json.findValue("shipPhone3Modal").asText();
            walkinCustomerName = walkinCustomerName.toUpperCase();
            String gstin = gstinPart1 + gstinPart2;
            CustomerWalkinDetail cwd = CustomerWalkinDetail.findByNameAndGSTNID(entityManager, walkinCustomerName,
                    gstin);
            if (cwd == null) {
                cwd = new CustomerWalkinDetail();
            }
            cwd.setCustomerName(walkinCustomerName.toUpperCase());
            cwd.setOrganization(user.getOrganization());
            cwd.setGstin(gstin);
            cwd.setBillingaddress(addressModal);
            cwd.setBillingcountry(countryModal);
            cwd.setBillingState(stateModal);
            cwd.setBillinglocation(locationModal);
            cwd.setBillingphoneCtryCode(phoneCountryCodeModal);
            cwd.setBillingphone(customerPhone1Modal + customerPhone2Modal + customerPhone3Modal);
            if (isAddressSameModal) {
                cwd.setShippingaddress(addressModal);
                cwd.setShippingcountry(countryModal);
                cwd.setShippingState(stateModal);
                cwd.setShippinglocation(locationModal);
                cwd.setShippingphone(cwd.getBillingphone());
                cwd.setShippingphoneCtryCode(phoneCountryCodeModal);
            } else {
                cwd.setShippingaddress(shipAddressModal);
                cwd.setShippingcountry(shipCountryModal);
                cwd.setShippingState(shipStateModal);
                // cwd.setShippingStateCode(); may needed in future.
                cwd.setShippinglocation(shipLocationModal);
                cwd.setShippingphone(shipPhone1Modal + shipPhone2Modal + shipPhone3Modal);
                cwd.setShippingphoneCtryCode(shipPhnNocountryCodeModal);
            }
            genericDao.saveOrUpdate(cwd, user, entityManager);
        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
            throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF, IdosConstants.TECHNICAL_EXCEPTION,
                    IdosConstants.NULL_KEY_EXC_ESMF_MSG, "Error on saving walk-in customer.");
        }
        return true;
    }
}
