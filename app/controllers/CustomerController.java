package controllers;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import model.Branch;
import model.BranchVendors;
import model.ConfigParams;
// import model.CustomerBranchWiseAdvBalance;
import model.CustomerDetail;
import model.Particulars;
import model.Specifics;
import model.Transaction;
import model.TrialBalance;
import model.Users;
import model.UsersRoles;
import model.Vendor;
import model.VendorGroup;
import model.VendorSpecific;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import akka.NotUsed;

import com.typesafe.config.Config;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.WebSocket;
import views.html.errorPage;
import actor.VendorTransactionActor;
import java.util.logging.Level;
import com.idos.util.CountryCurrencyUtil;
import com.idos.util.DateUtil;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import play.Application;
import javax.inject.Inject;
import akka.stream.javadsl.*;
import akka.actor.*;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.libs.Files.TemporaryFile;
//import play.mvc.Http.Session;

public class CustomerController extends StaticController {
    private static final String CUSTOMR_GSTIN_JQL = "select obj from CustomerDetail obj WHERE obj.organization.id=?1 and  obj.customerId=?2 and obj.presentStatus=1 order by obj.shippinglocation";

    private static final String CASH_CREDIT_SALES_PURCHASE_JQL = "select sum(t1.netAmount), t1.transactionSpecifics from TransactionItems t1 where t1.transaction in ( select t2.id from Transaction t2 where t2.transactionPurpose.id=?1 and t2.transactionVendorCustomer.id=?2 and t2.transactionStatus='ACCOUNTED' and t2.presentStatus=1 and t2.transactionDate between ?3 and ?4) group by t1.transactionSpecifics.id";
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    public static Application application;
    private Request request;
    // private Http.Session session = request.session();

    @Inject
    public CustomerController(Application application) {
        super(application);
        entityManager = EntityManagerProvider.getEntityManager();
        this.application = application;
    }

    @Transactional
    public Result downloadCustomerTemplate(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        ObjectNode results = Json.newObject();
        // ArrayNode an = results.putArray("orgCustomerFileCred");
        File file = null;
        Users user = null;
        try {
            JsonNode json = request.body().asJson();
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            String orgName = user.getOrganization().getName().replaceAll("\\s", "");
            String fileName = orgName + "_Customer_Template.xlsx";
            String sheetName = "Customers";

            String path = application.path().toString() + "/logs/OrgCustomer/";
            File filepath = new File(path);
            if (!filepath.exists()) {
                filepath.mkdir();
            }
            path = path + fileName;
            excelService.createOrgCustomerTemplateExcel(user, entityManager, path, sheetName);
            file = new File(path);
            return Results.ok(file).withHeader("ContentType", "application/xlsx").withHeader("Content-Disposition",
                    "attachment; filename=" + fileName);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }

    }

    @Transactional
    public Result saveCustomer(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            JsonNode json = request.body().asJson();
            ArrayNode an = result.putArray("newcustomerData");
            transaction.begin();
            Vendor customer = customerService.saveCustomer(json, user, entityManager, transaction,
                    IdosConstants.CUSTOMER);
            String role = "";
            List<UsersRoles> userRoles = UsersRoles.getUserRoleList(entityManager, user.getOrganization().getId(),
                    user.getId(),
                    user.getBranch().getId());
            for (UsersRoles roles : userRoles) {
                role += roles.getRole().getName() + ",";
            }
            role = role.substring(0, role.length() - 1);
            result.put("role", role);
            result.put("canCreateCustomer", user.canCreateCustomer());
            result.put("canActivateCustomer", user.canActivateCustomer());
            result.put("canActivateVendor", user.canActivateVendor());
            result.put("canCreateVendor", user.canCreateVendor());
            result.put("customerId", customer.getId());
            result.put("info", "vendorAdded");
            result.put("id", customer.getId());
            result.put("name", customer.getName());
            result.put("address", customer.getAddress());
            result.put("location", customer.getLocation());
            result.put("email", customer.getEmail());
            result.put("grantAccess", customer.getGrantAccess());
            result.put("phone", customer.getPhone());
            result.put("type", customer.getType());
            result.put("entityType", "vendorCustomer");
            result.put("presentStatus", customer.getPresentStatus());
            transaction.commit();
        } catch (Exception ex) {
            reportException(entityManager, transaction, user, ex, result);
        } catch (Throwable th) {
            reportThrowable(entityManager, transaction, user, th, result);
        }
        return Results.ok(result);
    }

    @Transactional
    public Result saveCustomerGroup(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        EntityTransaction transaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users users = null;
        try {
            transaction.begin();
            JsonNode json = request.body().asJson();
            ArrayNode an = result.putArray("newvendorGroupData");
            String useremail = json.findValue("useremail").asText();
            String customerGroupId = json.findValue("custGrpId") != null ? json.findValue("custGrpId").asText() : null;
            String custGroupName = json.findValue("custGroupName").asText();
            String custGroupKl = json.findValue("custGroupKl").asText();
            session.adding("email", useremail);
            users = getUserInfo(request);
            VendorGroup vendGroup;
            if (customerGroupId == null) {
                vendGroup = new VendorGroup();
            } else {
                vendGroup = VendorGroup.findById(IdosUtil.convertStringToLong(customerGroupId));
            }
            vendGroup.setGroupName(custGroupName);
            vendGroup.setKnowledgeLibrary(custGroupKl);
            vendGroup.setGroupType(2);
            vendGroup.setOrganization(users.getOrganization());
            genericDAO.saveOrUpdate(vendGroup, users, entityManager);
            Map<String, ActorRef> orgvendvendregistrered = new HashMap<String, ActorRef>();
            // Object[] keyArray =
            // VendorTransactionActor.vendvendregistrered.keySet().toArray();
            // for (int i = 0; i < keyArray.length; i++) {
            // List<Users> orgusers = Users.findByEmailActDeact(entityManager, (String)
            // keyArray[i]);
            // if (!orgusers.isEmpty()
            // && orgusers.get(0).getOrganization().getId() ==
            // vendGroup.getOrganization().getId()) {
            // orgvendvendregistrered.put(keyArray[i].toString(),
            // VendorTransactionActor.vendvendregistrered.get(keyArray[i]));
            // }
            // }
            // VendorTransactionActor.addGroup(vendGroup.getId(), orgvendvendregistrered,
            // vendGroup.getGroupName(), 2,
            // "vendorCustomerGroup");
            String role = "";
            List<UsersRoles> userRoles = UsersRoles.getUserRoleList(entityManager, users.getOrganization().getId(),
                    users.getId(),
                    users.getBranch().getId());
            for (UsersRoles roles : userRoles) {
                role += roles.getRole().getName() + ",";
            }
            role = role.substring(0, role.length() - 1);
            result.put("role", role);
            result.put("canCreateCustomer", users.canCreateCustomer());
            result.put("canActivateCustomer", users.canActivateCustomer());
            result.put("canActivateVendor", users.canActivateVendor());
            result.put("canCreateVendor", users.canCreateVendor());
            result.put("id", vendGroup.getId());
            result.put("groupname", vendGroup.getGroupName());
            result.put("type", 2);
            result.put("entityType", "vendorCustomerGroup");
            transaction.commit();
        } catch (Exception ex) {
            reportException(entityManager, transaction, users, ex, result);
        } catch (Throwable th) {
            reportThrowable(entityManager, transaction, users, th, result);
        }
        return Results.ok(result);
    }

    @Transactional
    public Result listCustomerGroup(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        EntityTransaction transaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users users = null;
        try {
            JsonNode json = request.body().asJson();
            ArrayNode an = result.putArray("customerGroupList");
            String useremail = json.findValue("useremail").asText();
            session.adding("email", useremail);
            users = getUserInfo(request);
            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("organization.id", users.getOrganization().getId());
            criterias.put("groupType", 2);
            criterias.put("presentStatus", 1);
            List<VendorGroup> vendorGroupList = genericDAO.findByCriteria(VendorGroup.class, criterias, entityManager);
            for (VendorGroup vendGrp : vendorGroupList) {
                ObjectNode row = Json.newObject();
                row.put("id", vendGrp.getId());
                row.put("vendGroupName", vendGrp.getGroupName());
                an.add(row);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, users.getEmail(), users.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result);
    }

    @Transactional
    public Result customerGroupDetails(Http.Request request) {

        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        EntityTransaction transaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users users = null;
        try {
            JsonNode json = request.body().asJson();
            ArrayNode an = result.putArray("customerGroupDetails");
            String useremail = json.findValue("useremail").asText();
            String entityPrimaryId = json.findValue("entityPrimaryId").asText();
            session.adding("email", useremail);
            users = getUserInfo(request);
            VendorGroup vendGroup = VendorGroup.findById(IdosUtil.convertStringToLong(entityPrimaryId));
            ObjectNode row = Json.newObject();
            row.put("id", vendGroup.getId());
            row.put("groupName", vendGroup.getGroupName());
            row.put("groupKl", vendGroup.getKnowledgeLibrary());
            an.add(row);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, users.getEmail(), users.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result);
    }

    @Transactional
    public Result uploadCustomers(Request request) {
        log.log(Level.FINE, ">>>> Start");
        Map<String, Object> criterias = new HashMap<String, Object>();
        // EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        ArrayNode an = result.putArray("successUploading");
        DataFormatter df = new DataFormatter();
        Users user = null;
        int batchSize = 50;
        int rowCount = 0;
        int lastrownum = 0;
        long totalRowsInserted = 0;
        StringBuilder itemsNotFound = new StringBuilder();
        StringBuilder branchNotFound = new StringBuilder();
        try {
            transaction.begin();
            MultipartFormData<File> body = request.body().asMultipartFormData();
            user = getUserInfo(request);
            List<FilePart<File>> chartofaccount = body.getFiles();
            for (FilePart<File> filePart : chartofaccount) {
                String fileName = filePart.getFilename();
                String contentType = filePart.getContentType();
                // File file = filePart.getRef();
                TemporaryFile temporaryFile = (TemporaryFile) filePart.getRef();
                String filePath = temporaryFile.path().toString();
                File file = new File(filePath);
                InputStream is = new java.io.FileInputStream(file);
                try {
                    XSSFWorkbook wb = new XSSFWorkbook(is);
                    int numOfSheets = wb.getNumberOfSheets();
                    for (int i = 0; i < numOfSheets; i++) {
                        XSSFSheet sheet = wb.getSheetAt(i);
                        if (!"Customers".equalsIgnoreCase(sheet.getSheetName())) {
                            continue;
                        }
                        lastrownum = sheet.getLastRowNum();
                        result.put("totalRowsInXls", lastrownum);
                        Iterator rows = sheet.rowIterator();
                        while (rows.hasNext()) {
                            rowCount++;
                            totalRowsInserted++;
                            if (rowCount > 0 && rowCount % batchSize == 0) { // batch commit of 25
                                entityManager.flush();
                                entityManager.clear();

                                transaction.commit();
                                transaction.begin();
                                rowCount = 0;
                            }
                            XSSFRow row = (XSSFRow) rows.next();
                            if (row.getRowNum() == 0) {
                                continue;
                            }
                            String branchList = row.getCell(0) != null ? row.getCell(0).toString() : null;
                            String branchOpeningBalanceList = row.getCell(1) == null ? null
                                    : df.formatCellValue(((XSSFCell) row.getCell(1)));
                            String branchAdvanceOpeningBalanceList = row.getCell(2) == null ? null
                                    : df.formatCellValue(((XSSFCell) row.getCell(2)));
                            String customerGroup = row.getCell(3) != null ? row.getCell(3).toString() : null;
                            String name = row.getCell(4) != null ? row.getCell(4).toString() : null;
                            String email = row.getCell(5) != null ? row.getCell(5).toString() : null;
                            String isGstRegistered = row.getCell(6) != null ? row.getCell(6).toString() : null;
                            String gstinStateCode = row.getCell(7) == null ? null
                                    : ((XSSFCell) row.getCell(7)).toString();
                            String gstinCode = row.getCell(8) == null ? null : ((XSSFCell) row.getCell(8)).toString();
                            String custType = row.getCell(9) == null ? null : ((XSSFCell) row.getCell(9)).toString();
                            String billingAddress = row.getCell(10) == null ? null
                                    : ((XSSFCell) row.getCell(10)).toString();
                            String billingCountry = row.getCell(11) == null ? null
                                    : ((XSSFCell) row.getCell(11)).toString();
                            String billingState = row.getCell(12) == null ? null
                                    : ((XSSFCell) row.getCell(12)).toString();
                            String billingLocation = row.getCell(13) == null ? null
                                    : ((XSSFCell) row.getCell(13)).toString();
                            String billingCountryCode = row.getCell(14) == null ? null
                                    : ((XSSFCell) row.getCell(14)).toString();
                            String billingPhoneNo = row.getCell(15) == null ? null
                                    : df.formatCellValue(((XSSFCell) row.getCell(15)));
                            String isShippingSameAsBilling = row.getCell(16) == null ? null
                                    : ((XSSFCell) row.getCell(16)).toString();
                            String shippingAddress = row.getCell(17) == null ? null
                                    : ((XSSFCell) row.getCell(17)).toString();
                            String shippingCountry = row.getCell(18) == null ? null
                                    : ((XSSFCell) row.getCell(18)).toString();
                            String shippingState = row.getCell(19) == null ? null
                                    : ((XSSFCell) row.getCell(19)).toString();
                            String shippingLocation = row.getCell(20) == null ? null
                                    : ((XSSFCell) row.getCell(20)).toString();
                            String shippingCountryCode = row.getCell(21) == null ? null
                                    : ((XSSFCell) row.getCell(21)).toString();
                            String shippingPhoneNo = row.getCell(22) == null ? null
                                    : ((XSSFCell) row.getCell(22)).toString();
                            String sellItemsList = row.getCell(23) == null ? null
                                    : ((XSSFCell) row.getCell(23)).toString();
                            String statutoryId1 = row.getCell(24) == null ? null
                                    : ((XSSFCell) row.getCell(24)).toString();
                            // String statutoryNo1 = row.getCell(23) == null ? null : ((XSSFCell)
                            // row.getCell(23)).toString();
                            String statutoryNo1 = row.getCell(25) == null ? null
                                    : df.formatCellValue(((XSSFCell) row.getCell(25)));
                            String statutoryId2 = row.getCell(26) == null ? null
                                    : ((XSSFCell) row.getCell(26)).toString();
                            String statutoryNo2 = row.getCell(27) == null ? null
                                    : df.formatCellValue(((XSSFCell) row.getCell(27)));
                            String statutoryId3 = row.getCell(28) == null ? null
                                    : ((XSSFCell) row.getCell(28)).toString();
                            String statutoryNo3 = row.getCell(29) == null ? null
                                    : df.formatCellValue(((XSSFCell) row.getCell(29)));
                            String cashCredit = row.getCell(30) == null ? null
                                    : ((XSSFCell) row.getCell(30)).toString();
                            String daysOfCredit = row.getCell(31) == null ? null
                                    : ((XSSFCell) row.getCell(31)).toString();
                            String creditLimit = row.getCell(32) == null ? null
                                    : ((XSSFCell) row.getCell(32)).toString();
                            String txnExceedingCreditLimit = row.getCell(33) == null ? null
                                    : ((XSSFCell) row.getCell(33)).toString();
                            String isExcludeAdv = row.getCell(34) == null ? null
                                    : ((XSSFCell) row.getCell(34)).toString();

                            String GSTINStateCode2 = row.getCell(35) == null ? null
                                    : ((XSSFCell) row.getCell(35)).toString();
                            String GSTINCode2 = row.getCell(36) == null ? null
                                    : ((XSSFCell) row.getCell(36)).toString();
                            String billingAddress2 = row.getCell(37) == null ? null
                                    : ((XSSFCell) row.getCell(37)).toString();
                            String billingCountry2 = row.getCell(38) == null ? null
                                    : ((XSSFCell) row.getCell(38)).toString();
                            String billingState2 = row.getCell(39) == null ? null
                                    : ((XSSFCell) row.getCell(39)).toString();
                            String billingLocation2 = row.getCell(40) == null ? null
                                    : ((XSSFCell) row.getCell(40)).toString();
                            String billingCountryCode2 = row.getCell(41) == null ? null
                                    : ((XSSFCell) row.getCell(41)).toString();
                            String billingPhoneNo2 = row.getCell(42) == null ? null
                                    : df.formatCellValue(((XSSFCell) row.getCell(42)));
                            String isShippingSameAsBilling2 = row.getCell(43) == null ? null
                                    : ((XSSFCell) row.getCell(43)).toString();
                            String shippingAddress2 = row.getCell(44) == null ? null
                                    : ((XSSFCell) row.getCell(44)).toString();
                            String shippingCountry2 = row.getCell(45) == null ? null
                                    : ((XSSFCell) row.getCell(45)).toString();
                            String shippingState2 = row.getCell(46) == null ? null
                                    : ((XSSFCell) row.getCell(46)).toString();
                            String shippingLocation2 = row.getCell(47) == null ? null
                                    : ((XSSFCell) row.getCell(47)).toString();
                            String shippingCountryCode2 = row.getCell(48) == null ? null
                                    : ((XSSFCell) row.getCell(48)).toString();
                            String shippingPhoneNo2 = row.getCell(49) == null ? null
                                    : ((XSSFCell) row.getCell(49)).toString();

                            String GSTINStateCode3 = row.getCell(50) == null ? null
                                    : ((XSSFCell) row.getCell(50)).toString();
                            String GSTINCode3 = row.getCell(51) == null ? null
                                    : ((XSSFCell) row.getCell(51)).toString();
                            String billingAddress3 = row.getCell(52) == null ? null
                                    : ((XSSFCell) row.getCell(52)).toString();
                            String billingCountry3 = row.getCell(53) == null ? null
                                    : ((XSSFCell) row.getCell(53)).toString();
                            String billingState3 = row.getCell(54) == null ? null
                                    : ((XSSFCell) row.getCell(54)).toString();
                            String billingLocation3 = row.getCell(55) == null ? null
                                    : ((XSSFCell) row.getCell(55)).toString();
                            String billingCountryCode3 = row.getCell(56) == null ? null
                                    : ((XSSFCell) row.getCell(56)).toString();
                            String billingPhoneNo3 = row.getCell(57) == null ? null
                                    : df.formatCellValue(((XSSFCell) row.getCell(57)));
                            String isShippingSameAsBilling3 = row.getCell(58) == null ? null
                                    : ((XSSFCell) row.getCell(58)).toString();
                            String shippingAddress3 = row.getCell(59) == null ? null
                                    : ((XSSFCell) row.getCell(59)).toString();
                            String shippingCountry3 = row.getCell(60) == null ? null
                                    : ((XSSFCell) row.getCell(60)).toString();
                            String shippingState3 = row.getCell(61) == null ? null
                                    : ((XSSFCell) row.getCell(61)).toString();
                            String shippingLocation3 = row.getCell(62) == null ? null
                                    : ((XSSFCell) row.getCell(62)).toString();
                            String shippingCountryCode3 = row.getCell(63) == null ? null
                                    : ((XSSFCell) row.getCell(63)).toString();
                            String shippingPhoneNo3 = row.getCell(64) == null ? null
                                    : ((XSSFCell) row.getCell(64)).toString();

                            if (name == null || name.equals("") || name.equals("null")) {
                                continue;
                            }
                            if (isGstRegistered != null && isGstRegistered != "null") {
                                if (isGstRegistered.equalsIgnoreCase("Yes")) {
                                    if (gstinStateCode == null || gstinCode == null) {
                                        throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
                                                IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
                                                "Invalid GSTIN state code or GSTIN code for customer " + name);
                                    }
                                }
                            }
                            String billingStateCode = IdosUtil.getStateCode(billingState);
                            String GSTIN = "";
                            if (gstinStateCode != null && gstinStateCode != "null") {
                                if (billingState == null || billingState.equalsIgnoreCase("null")
                                        || billingStateCode == null || !billingStateCode.equals(gstinStateCode)) {
                                    throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
                                            IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
                                            "Invalid GSTIN state for customer: " + name
                                                    + " or billing state code not found for : " + billingState);
                                }
                                GSTIN = gstinStateCode;
                            } else if (billingState != null && billingStateCode != null) {
                                GSTIN = billingStateCode;
                            }
                            if (gstinCode != null) {
                                GSTIN = GSTIN + gstinCode;
                            }
                            Vendor vend = null;
                            criterias.clear();
                            criterias.put("name", name);
                            criterias.put("organization.id", user.getOrganization().getId());
                            criterias.put("type", 2);
                            criterias.put("presentStatus", 1);
                            List<Vendor> existVend = genericDAO.findByCriteria(Vendor.class, criterias, entityManager);
                            if (existVend.size() == 0) {
                                vend = new Vendor();
                            } else {
                                vend = existVend.get(0);
                                if (vend.getId() == null) {
                                    throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
                                            IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
                                            "Please contact Support team " + name);
                                }
                            }

                            if (customerGroup != null && !customerGroup.equals("") && !customerGroup.equals("null")) {
                                List<VendorGroup> vendGroup = VendorGroup.findListByOrgIdAndName(entityManager,
                                        user.getOrganization().getId(), customerGroup);
                                if (vendGroup != null && vendGroup.size() > 0) {
                                    vend.setVendorGroup(vendGroup.get(0));
                                }
                            }
                            vend.setType(2); // customer type
                            vend.setName(name);
                            vend.setEmail(email);
                            vend.setAddress(billingAddress);
                            vend.setLocation(billingLocation);
                            vend.setPhoneCtryCode(billingCountryCode);
                            vend.setPhone(billingPhoneNo);
                            if (isGstRegistered != null && isGstRegistered != "") {
                                if (isGstRegistered.equalsIgnoreCase("Yes")) {
                                    vend.setIsRegistered(1);
                                } else {
                                    vend.setIsRegistered(0);
                                }
                            }
                            if (custType != null) {
                                if (custType.equalsIgnoreCase("Customer is a business Establishment")) {
                                    vend.setIsBusiness(1);
                                } else if (custType.equalsIgnoreCase("Customer is a individual Consumer")) {
                                    vend.setIsBusiness(2);
                                } else {
                                    vend.setIsBusiness(0);
                                }
                            }
                            vend.setGstin(GSTIN);
                            int billingCountryId = 0;
                            if (billingCountry != null) {
                                String counrtyId = CountryCurrencyUtil.getCountryId(billingCountry);
                                if (counrtyId != null) {
                                    billingCountryId = IdosUtil.convertStringToInt(counrtyId);
                                    vend.setCountry(billingCountryId);
                                }
                            }
                            vend.setCountryState(billingState);
                            vend.setStatutoryName1(statutoryId1);
                            vend.setStatutoryNumber1(statutoryNo1);
                            vend.setStatutoryName2(statutoryId2);
                            vend.setStatutoryNumber2(statutoryNo2);
                            vend.setStatutoryName3(statutoryId3);
                            vend.setStatutoryNumber3(statutoryNo3);
                            if (daysOfCredit != null && !daysOfCredit.equals("null")) {
                                Integer daysOfCreditInt = IdosUtil.convertStringToInt(daysOfCredit);
                                vend.setDaysForCredit(daysOfCreditInt);
                            }
                            if (cashCredit != null && cashCredit != "null") {
                                if (cashCredit.equalsIgnoreCase("Cash")) {
                                    vend.setPurchaseType(1);
                                } else if (cashCredit.equalsIgnoreCase("Credit")) {
                                    vend.setPurchaseType(0);
                                } else if (cashCredit.equalsIgnoreCase("Both")) {
                                    vend.setPurchaseType(2);
                                }
                            }
                            if (creditLimit != null && creditLimit != "null") {
                                vend.setCreditLimit(IdosUtil.convertStringToDouble(creditLimit));
                            }
                            if (txnExceedingCreditLimit != null && txnExceedingCreditLimit != "null") {
                                if (txnExceedingCreditLimit.equals("Prcoess")) {
                                    vend.setExceedingCreditProcessStop(0);
                                } else if (txnExceedingCreditLimit.equals("Stop")) {
                                    vend.setExceedingCreditProcessStop(1);
                                }
                            }
                            if (isExcludeAdv != null && isExcludeAdv != "") {
                                if (isGstRegistered.equalsIgnoreCase("Yes")) {
                                    vend.setExcludeAdvFromCreLimCheck(1);
                                } else {
                                    vend.setExcludeAdvFromCreLimCheck(0);
                                }
                            }
                            Double totalOpeningBalance = 0.0;
                            Double totalAdvanceOpeningBalance = 0.0;

                            String branchListNames[] = branchList.split(",");
                            if (branchOpeningBalanceList != null && !branchOpeningBalanceList.equals("null")) {
                                String branchOpeningBalances[] = branchOpeningBalanceList.split(",");
                                for (int j = 0; j < branchListNames.length; j++) {
                                    totalOpeningBalance += IdosUtil.convertStringToDouble(branchOpeningBalances[i]);
                                }
                                vend.setTotalOriginalOpeningBalance(totalOpeningBalance);
                                vend.setTotalOpeningBalance(totalOpeningBalance);
                            } else {
                                vend.setTotalOriginalOpeningBalance(0.0);
                                vend.setTotalOpeningBalance(0.0);
                            }
                            if (branchAdvanceOpeningBalanceList != null && branchAdvanceOpeningBalanceList != ""
                                    && !branchAdvanceOpeningBalanceList.equals("null")) {
                                String branchAdvanceOpeningBalances[] = branchAdvanceOpeningBalanceList.split(",");
                                for (int j = 0; j < branchListNames.length; j++) {
                                    totalAdvanceOpeningBalance += IdosUtil
                                            .convertStringToDouble(branchAdvanceOpeningBalances[i]);
                                }
                                vend.setTotalOpeningBalanceAdvPaid(totalAdvanceOpeningBalance);
                                vend.setTotalOriginalOpeningBalanceAdvPaid(totalAdvanceOpeningBalance);
                            } else {
                                vend.setTotalOpeningBalanceAdvPaid(0.0);
                                vend.setTotalOriginalOpeningBalanceAdvPaid(0.0);
                            }
                            vend.setBranch(user.getBranch());
                            vend.setOrganization(user.getOrganization());
                            String specifics = "";
                            if (sellItemsList != null && sellItemsList != "" && !sellItemsList.equals("null")) {
                                String[] sellItems = sellItemsList.split(",");
                                for (int y = 0; y < sellItems.length; y++) {
                                    List<Specifics> specific = Specifics.findByNameAndHeadType(entityManager,
                                            user.getOrganization(), sellItems[y], "1");
                                    if (specific != null && specific.size() > 0) {
                                        specifics = specifics + specific.get(0).getId() + ",";
                                    }
                                }
                                vend.setCustomerSpecifics(specifics);
                            }
                            if (user.getUserRolesName().contains("MASTER ADMIN") || IdosConstants.SOLV
                                    .equalsIgnoreCase(ConfigParams.getInstance().getCompanyOwner())) {
                                vend.setPresentStatus(1);
                            } else {
                                vend.setPresentStatus(0);
                            }

                            genericDAO.saveOrUpdate(vend, user, entityManager);

                            // save CustomerDetail for Main GSTIN
                            CustomerDetail customerDetail = null;
                            if (GSTIN != null) {
                                if (GSTIN.length() == 15) {
                                    customerDetail = CustomerDetail.findByCustomerGSTNID(entityManager, vend.getId(),
                                            GSTIN);
                                } else {
                                    throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
                                            IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
                                            "GSTIN: " + GSTIN + " is not valid should be 15 characters long");
                                }
                            }
                            if (customerDetail == null) {
                                customerDetail = new CustomerDetail();
                            }
                            customerDetail.setCustomer(vend);
                            customerDetail.setGstin(GSTIN);
                            customerDetail.setOrganization(user.getOrganization());
                            customerDetail.setBillingcountry(billingCountryId);
                            customerDetail.setBillinglocation(billingLocation);
                            customerDetail.setBillingphoneCtryCode(billingCountryCode);
                            customerDetail.setBillingphone(billingPhoneNo);
                            customerDetail.setBillingaddress(billingAddress);
                            customerDetail.setBillingState(billingState);
                            customerDetail.setBillingStateCode(billingStateCode);
                            if (isShippingSameAsBilling != null && isShippingSameAsBilling.equalsIgnoreCase("No")) {
                                customerDetail.setIsSameAsBillingAddress(0);
                                customerDetail.setShippingaddress(shippingAddress);
                                if (shippingCountry != null) {
                                    String counrtyId = CountryCurrencyUtil.getCountryId(shippingCountry);
                                    if (counrtyId != null) {
                                        customerDetail.setShippingcountry(IdosUtil.convertStringToInt(counrtyId));
                                    }
                                }
                                customerDetail.setShippinglocation(shippingLocation);
                                customerDetail.setShippingphone(shippingPhoneNo);
                                customerDetail.setShippingphoneCtryCode(shippingCountryCode);
                                customerDetail.setShippingState(shippingState);
                                String shippingStateCode = IdosUtil.getStateCode(shippingState);
                                customerDetail.setShippingStateCode(shippingStateCode);

                            } else {
                                customerDetail.setIsSameAsBillingAddress(1);
                                customerDetail.setShippingaddress(billingAddress);
                                customerDetail.setShippingcountry(billingCountryId);
                                customerDetail.setShippinglocation(billingLocation);
                                customerDetail.setShippingphone(billingPhoneNo);
                                customerDetail.setShippingphoneCtryCode(billingCountryCode);
                                customerDetail.setShippingState(billingState);
                                customerDetail.setShippingStateCode(billingStateCode);
                            }
                            genericDAO.saveOrUpdate(customerDetail, user, entityManager);

                            // insert 2nd GSTIN
                            String GSTIN2 = null;
                            String billingStateCode2 = IdosUtil.getStateCode(billingState2);
                            if (GSTINStateCode2 != null && GSTINStateCode2 != "null" && billingStateCode2 != null) {
                                if (billingState2 == null || billingState2 == "null"
                                        || !billingStateCode2.equals(GSTINStateCode2)) {
                                    throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
                                            IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
                                            "Invalid GSTIN state for customer " + name);
                                }
                            }
                            if (GSTINStateCode2 != null && GSTINCode2 != null) {
                                GSTIN2 = GSTINStateCode2 + GSTINCode2;
                            }
                            if (GSTIN2 != null) {
                                CustomerDetail customerDetail2 = CustomerDetail.findByCustomerGSTNID(entityManager,
                                        vend.getId(), GSTIN2);
                                if (customerDetail2 == null) {
                                    customerDetail2 = new CustomerDetail();
                                }
                                int billingCountryId2 = 0;
                                if (billingCountry2 != null) {
                                    String counrtyId2 = CountryCurrencyUtil.getCountryId(billingCountry2);
                                    if (counrtyId2 != null) {
                                        billingCountryId2 = IdosUtil.convertStringToInt(counrtyId2);
                                    }
                                }
                                customerDetail2.setCustomer(vend);
                                customerDetail2.setGstin(GSTIN2);
                                customerDetail2.setOrganization(user.getOrganization());
                                customerDetail2.setBillingcountry(billingCountryId2);
                                customerDetail2.setBillinglocation(billingLocation2);
                                customerDetail2.setBillingphoneCtryCode(billingCountryCode2);
                                customerDetail2.setBillingphone(billingPhoneNo2);
                                customerDetail2.setBillingaddress(billingAddress2);
                                customerDetail2.setBillingState(billingState2);
                                customerDetail2.setBillingStateCode(billingStateCode2);
                                if (isShippingSameAsBilling2 != null
                                        && isShippingSameAsBilling2.equalsIgnoreCase("No")) {
                                    customerDetail.setIsSameAsBillingAddress(0);
                                    customerDetail2.setShippingaddress(shippingAddress2);
                                    if (shippingCountry2 != null) {
                                        String counrtyId2 = CountryCurrencyUtil.getCountryId(shippingCountry2);
                                        if (counrtyId2 != null) {
                                            customerDetail2.setShippingcountry(IdosUtil.convertStringToInt(counrtyId2));
                                        }
                                    }
                                    customerDetail2.setShippinglocation(shippingLocation2);
                                    customerDetail2.setShippingphone(shippingPhoneNo2);
                                    customerDetail2.setShippingphoneCtryCode(shippingCountryCode2);
                                    customerDetail2.setShippingState(shippingState2);
                                    String shippingStateCode2 = IdosUtil.getStateCode(shippingState2);
                                    customerDetail2.setShippingStateCode(shippingStateCode2);

                                } else {
                                    customerDetail.setIsSameAsBillingAddress(1);
                                    customerDetail2.setShippingaddress(billingAddress2);
                                    customerDetail2.setShippingcountry(billingCountryId2);
                                    customerDetail2.setShippinglocation(billingLocation2);
                                    customerDetail2.setShippingphone(billingPhoneNo2);
                                    customerDetail2.setShippingphoneCtryCode(billingCountryCode2);
                                    customerDetail2.setShippingState(billingState2);
                                    customerDetail2.setShippingStateCode(billingStateCode2);
                                }
                                genericDAO.saveOrUpdate(customerDetail2, user, entityManager);

                            }
                            // Insert 3rd GSTIN
                            String GSTIN3 = null;
                            String billingStateCode3 = IdosUtil.getStateCode(billingState3);
                            if (GSTINStateCode3 != null && GSTINStateCode3 != "null") {
                                if (billingState3 == null || billingState3 == "null"
                                        || !billingStateCode3.equals(GSTINStateCode3)) {
                                    throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
                                            IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
                                            "Invalid GSTIN state for customer " + name);
                                }
                            }
                            if (GSTINCode3 != null && GSTINStateCode3 != null) {
                                GSTIN3 = GSTINStateCode3 + GSTINCode3;
                            }
                            if (GSTIN3 != null) {
                                CustomerDetail customerDetail3 = CustomerDetail.findByCustomerGSTNID(entityManager,
                                        vend.getId(), GSTIN3);
                                if (customerDetail3 == null) {
                                    customerDetail3 = new CustomerDetail();
                                }
                                int billingCountryId3 = 0;
                                if (billingCountry3 != null) {
                                    String counrtyId3 = CountryCurrencyUtil.getCountryId(billingCountry3);
                                    if (counrtyId3 != null) {
                                        billingCountryId3 = IdosUtil.convertStringToInt(counrtyId3);
                                    }
                                }
                                customerDetail3.setCustomer(vend);
                                customerDetail3.setGstin(GSTIN3);
                                customerDetail3.setOrganization(user.getOrganization());
                                customerDetail3.setBillingcountry(billingCountryId3);
                                customerDetail3.setBillinglocation(billingLocation3);
                                customerDetail3.setBillingphoneCtryCode(billingCountryCode3);
                                customerDetail3.setBillingphone(billingPhoneNo3);
                                customerDetail3.setBillingaddress(billingAddress3);
                                customerDetail3.setBillingState(billingState3);
                                customerDetail3.setBillingStateCode(billingStateCode3);
                                if (isShippingSameAsBilling3 != null
                                        && isShippingSameAsBilling3.equalsIgnoreCase("No")) {
                                    customerDetail.setIsSameAsBillingAddress(0);
                                    customerDetail3.setShippingaddress(shippingAddress3);
                                    if (shippingCountry3 != null) {
                                        String counrtyId3 = CountryCurrencyUtil.getCountryId(shippingCountry3);
                                        if (counrtyId3 != null) {
                                            customerDetail3.setShippingcountry(IdosUtil.convertStringToInt(counrtyId3));
                                        }
                                    }
                                    customerDetail3.setShippinglocation(shippingLocation2);
                                    customerDetail3.setShippingphone(shippingPhoneNo3);
                                    customerDetail3.setShippingphoneCtryCode(shippingCountryCode3);
                                    customerDetail3.setShippingState(shippingState3);
                                    String shippingStateCode3 = IdosUtil.getStateCode(shippingState3);
                                    customerDetail3.setShippingStateCode(shippingStateCode3);

                                } else {
                                    customerDetail.setIsSameAsBillingAddress(1);
                                    customerDetail3.setShippingaddress(billingAddress3);
                                    customerDetail3.setShippingcountry(billingCountryId3);
                                    customerDetail3.setShippinglocation(billingLocation3);
                                    customerDetail3.setShippingphone(billingPhoneNo3);
                                    customerDetail3.setShippingphoneCtryCode(billingCountryCode3);
                                    customerDetail3.setShippingState(billingState3);
                                    customerDetail3.setShippingStateCode(billingStateCode3);
                                }
                                genericDAO.saveOrUpdate(customerDetail3, user, entityManager);

                            }
                            // customer other information start
                            // branches

                            String newVendBnchs[] = branchList.split(",");
                            String branchOpeningBalances[] = null;
                            String branchAdvanceOpeningBalances[] = null;
                            if (branchOpeningBalanceList != null) {
                                branchOpeningBalances = branchOpeningBalanceList.split(",");
                            }
                            if (branchAdvanceOpeningBalanceList != null) {
                                branchAdvanceOpeningBalances = branchAdvanceOpeningBalanceList.split(",");
                            }
                            for (int x = 0; x < newVendBnchs.length; x++) {
                                if (!newVendBnchs[x].equals("")) {
                                    List<Branch> bnch = Branch.findListByOrgIdAndName(entityManager,
                                            user.getOrganization().getId(), newVendBnchs[x]);
                                    if (bnch != null && bnch.size() > 0) {
                                        BranchVendors newBnchVend = new BranchVendors();
                                        Branch branch = bnch.get(0);
                                        newBnchVend.setBranch(branch);
                                        newBnchVend.setOrganization(branch.getOrganization());
                                        newBnchVend.setVendor(vend);

                                        if (branchOpeningBalanceList != null) {
                                            if (i < branchOpeningBalances.length && branchOpeningBalances[i] != null
                                                    && !"".equals(branchOpeningBalances[i])) {

                                                newBnchVend.setOriginalOpeningBalance(
                                                        IdosUtil.convertStringToDouble(branchOpeningBalances[x]));
                                                newBnchVend.setOpeningBalance(
                                                        IdosUtil.convertStringToDouble(branchOpeningBalances[x]));
                                            } else {
                                                newBnchVend.setOriginalOpeningBalance(0.0);
                                                newBnchVend.setOpeningBalance(0.0);
                                            }
                                        } else {
                                            newBnchVend.setOriginalOpeningBalance(0.0);
                                            newBnchVend.setOpeningBalance(0.0);
                                        }
                                        if (branchAdvanceOpeningBalanceList != null) {
                                            if (i < branchAdvanceOpeningBalances.length
                                                    && branchAdvanceOpeningBalances[i] != null
                                                    && !"".equals(branchAdvanceOpeningBalances[i])) {
                                                newBnchVend.setOriginalOpeningBalanceAdvPaid(IdosUtil
                                                        .convertStringToDouble(branchAdvanceOpeningBalances[x]));
                                                newBnchVend.setOpeningBalanceAdvPaid(IdosUtil
                                                        .convertStringToDouble(branchAdvanceOpeningBalances[x]));
                                            } else {
                                                newBnchVend.setOriginalOpeningBalanceAdvPaid(0.0);
                                                newBnchVend.setOpeningBalanceAdvPaid(0.0);
                                            }
                                        } else {
                                            newBnchVend.setOriginalOpeningBalanceAdvPaid(0.0);
                                            newBnchVend.setOpeningBalanceAdvPaid(0.0);
                                        }
                                        genericDAO.saveOrUpdate(newBnchVend, user, entityManager);
                                    } else {
                                        if (branchNotFound.indexOf(newVendBnchs[x]) != -1)
                                            branchNotFound.append(newVendBnchs[x]).append(", ");
                                    }
                                }
                            }

                            // vendor specifics
                            String customerSpecificDisc = "";
                            if (sellItemsList != null && sellItemsList != "" && !sellItemsList.equals("null")) {
                                String[] sellItems = sellItemsList.split(",");
                                for (int y = 0; y < sellItems.length; y++) {
                                    List<Specifics> specific = Specifics.findByNameAndHeadType(entityManager,
                                            user.getOrganization(), sellItems[y], "1");
                                    if (specific != null && specific.size() > 0) {
                                        Specifics itemSpec = specific.get(0);
                                        VendorSpecific vennSpecf = new VendorSpecific();
                                        Particulars newVendParticulars = itemSpec.getParticularsId();
                                        vennSpecf.setVendorSpecific(vend);
                                        vennSpecf.setSpecificsVendors(itemSpec);
                                        vennSpecf.setBranch(user.getBranch());
                                        vennSpecf.setOrganization(user.getOrganization());
                                        vennSpecf.setParticulars(newVendParticulars);
                                        if (ConfigParams.getInstance().isDeploymentSingleUser(user)) {
                                            customerSpecificDisc += "100,";
                                            vennSpecf.setDiscountPercentage(100d);
                                        }
                                        genericDAO.saveOrUpdate(vennSpecf, user, entityManager);
                                    } else {
                                        if (itemsNotFound.indexOf(sellItems[y]) != -1)
                                            itemsNotFound.append(sellItems[y]).append(", ");
                                    }
                                }
                            }
                            if (!"".equals(customerSpecificDisc)) {
                                customerSpecificDisc = customerSpecificDisc.substring(0,
                                        customerSpecificDisc.length() - 1);
                                vend.setCustomerSpecificsDiscountPercentage(customerSpecificDisc);
                                genericDAO.saveOrUpdate(vend, user, entityManager);
                            }
                        }
                    }
                    transaction.commit();
                } catch (RuntimeException e) {
                    if (transaction != null && transaction.isActive()) {
                        transaction.rollback();
                    }
                    throw e;
                } catch (Exception ex) {
                    log.log(Level.SEVERE, "Error", ex);
                    if (transaction != null && transaction.isActive()) {
                        transaction.rollback();
                    }
                    throw ex;
                } finally {
                    result.put("totalRowsInserted", totalRowsInserted - 1);
                    result.put("branchNotFound", branchNotFound.toString());
                    result.put("itemsNotFound", itemsNotFound.toString());
                    log.log(Level.INFO, "Total rows inserted " + totalRowsInserted);
                }
            }
            ObjectNode row = Json.newObject();
            an.add(row);
        } catch (Exception ex) {
            reportException(entityManager, transaction, user, ex, result);
        } catch (Throwable th) {
            reportThrowable(entityManager, transaction, user, th, result);
        }
        log.log(Level.FINE, ">>>> End");
        return Results.ok(result);
    }

    @Transactional
    public Result showCustomerDetails(Request request) {
        log.log(Level.FINE, ">>>> Start");
        ObjectNode results = Json.newObject();
        // EntityManager entityManager = getEntityManager();
        Users user = null;
        try {
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            JsonNode json = request.body().asJson();
            ArrayNode vendordetailan = results.putArray("vendordetailsData");
            ArrayNode custGstinState = results.putArray("custGstinState");
            String vendorEntityId = json.findValue("entityPrimaryId").asText();

            Vendor vendorDet = Vendor.findById(IdosUtil.convertStringToLong(vendorEntityId));
            if (vendorDet != null) {
                ObjectNode row = Json.newObject();
                row.put("id", vendorDet.getId());
                row.put("vendorName", vendorDet.getName());
                // row.put("code", vendorDet.getCustomerCode());
                row.put("vendorEmail", vendorDet.getEmail());
                row.put("vendPhnCtryCode", vendorDet.getPhoneCtryCode());
                if (vendorDet != null && vendorDet.getGstin() != null) {
                    if (vendorDet.getGstin().length() > 14) {
                        row.put("custGstinPart1", vendorDet.getGstin().substring(0, 2));
                        row.put("custGstinPart2", vendorDet.getGstin().substring(2));
                    } else {
                        row.put("custGstinPart1", "");
                        row.put("custGstinPart2", "");
                    }
                } else {
                    row.put("custGstinPart1", "");
                    row.put("custGstinPart2", "");
                }

                row.put("custBusinessIndividual", vendorDet.getIsBusiness());
                row.put("custRegisteredOrUnReg", vendorDet.getIsRegistered());
                if (vendorDet.getPhone() != null) {
                    int k = vendorDet.getPhone().indexOf("-");
                    row.put("vendorPhone", vendorDet.getPhone().substring(k + 1, vendorDet.getPhone().length()));
                } else {
                    row.put("vendorPhone", "");
                }
                if (vendorDet.getCountry() != null) {
                    row.put("vendorAddress", vendorDet.getCountry());
                } else {
                    row.put("vendorAddress", "");
                }
                if (vendorDet.getCountryState() != null) {
                    row.put("vendorState", vendorDet.getCountryState());
                } else {
                    row.put("vendorState", "");
                }
                if (vendorDet.getPlaceOfSupplyType() != null) {
                    row.put("placeOfSupplyType", vendorDet.getPlaceOfSupplyType().toString());
                } else {
                    row.put("placeOfSupplyType", "");
                }
                if (vendorDet.getVendorGroup() != null) {
                    row.put("vendorGroup", vendorDet.getVendorGroup().getId());
                } else {
                    row.put("vendorGroup", "");
                }
                if (vendorDet.getAddress() != null) {
                    row.put("vendAddress", vendorDet.getAddress());
                } else {
                    row.put("vendAddress", "");
                }
                row.put("vendorFutPayAlwd", vendorDet.getPurchaseType());
                if (vendorDet.getDaysForCredit() != null) {
                    row.put("daysOfCredit", vendorDet.getDaysForCredit());
                } else {
                    row.put("daysOfCredit", "");
                }
                if (vendorDet.getCreditLimit() != null) {
                    row.put("custCreditLimit", IdosConstants.decimalFormat.format(vendorDet.getCreditLimit()));
                } else {
                    row.put("custCreditLimit", "");
                }
                if (vendorDet.getExceedingCreditProcessStop() != null) {
                    row.put("custTranExceedCredLim", vendorDet.getExceedingCreditProcessStop());
                } else {
                    row.put("custTranExceedCredLim", "0");
                }
                if (vendorDet.getExcludeAdvFromCreLimCheck() != null) {
                    row.put("exculdeAdvCreLimCheck", vendorDet.getExcludeAdvFromCreLimCheck());
                } else {
                    row.put("exculdeAdvCreLimCheck", "0");
                }
                if (vendorDet.getTotalOriginalOpeningBalance() != null) {
                    // row.put("openingBalance", vendorDet.getOpeningBalance());
                    row.put("openingBalance",
                            IdosConstants.decimalFormat.format(vendorDet.getTotalOriginalOpeningBalance()));
                } else {
                    row.put("openingBalance", "");
                }
                if (vendorDet.getTotalOriginalOpeningBalanceAdvPaid() != null) {
                    row.put("openingBalanceAdvPaid",
                            IdosConstants.decimalFormat.format(vendorDet.getTotalOriginalOpeningBalanceAdvPaid()));
                } else {
                    row.put("openingBalanceAdvPaid", "");
                }
                if (vendorDet.getCustomerRemarks() != null) {
                    row.put("discount", vendorDet.getCustomerRemarks());
                } else {
                    row.put("discount", "");
                }
                if (vendorDet.getStatutoryName1() != null) {
                    row.put("customerStatutoryName1", vendorDet.getStatutoryName1());
                } else {
                    row.put("customerStatutoryName1", "");
                }
                if (vendorDet.getStatutoryNumber1() != null) {
                    row.put("customerStatutoryNumber1", vendorDet.getStatutoryNumber1());
                } else {
                    row.put("customerStatutoryNumber1", "");
                }
                if (vendorDet.getStatutoryName2() != null) {
                    row.put("customerStatutoryName2", vendorDet.getStatutoryName2());
                } else {
                    row.put("customerStatutoryName2", "");
                }
                if (vendorDet.getStatutoryNumber2() != null) {
                    row.put("customerStatutoryNumber2", vendorDet.getStatutoryNumber2());
                } else {
                    row.put("customerStatutoryNumber2", "");
                }
                if (vendorDet.getStatutoryName3() != null) {
                    row.put("customerStatutoryName3", vendorDet.getStatutoryName3());
                } else {
                    row.put("customerStatutoryName3", "");
                }
                if (vendorDet.getStatutoryNumber3() != null) {
                    row.put("customerStatutoryNumber3", vendorDet.getStatutoryNumber3());
                } else {
                    row.put("customerStatutoryNumber3", "");
                }
                if (vendorDet.getStatutoryName4() != null) {
                    row.put("customerStatutoryName4", vendorDet.getStatutoryName4());
                } else {
                    row.put("customerStatutoryName4", "");
                }
                if (vendorDet.getStatutoryNumber4() != null) {
                    row.put("customerStatutoryNumber4", vendorDet.getStatutoryNumber4());
                } else {
                    row.put("customerStatutoryNumber4", "");
                }
                row.put("vendorLocation", vendorDet.getLocation());
                row.put("vendorContractDoc", vendorDet.getPriceListDoc());
                row.put("priceListDoc", vendorDet.getPriceListDoc());
                String Specifications = "";
                String custspecfdisperc = "";
                if (vendorDet.getCustomerSpecifics() != null) {
                    Specifications = vendorDet.getCustomerSpecifics();
                    custspecfdisperc = vendorDet.getCustomerSpecificsDiscountPercentage();
                }
                row.put("Specifications", Specifications);
                row.put("custspecfdisperc", custspecfdisperc);
                List<BranchVendors> vendorBranches = BranchVendors.findByVendor(entityManager,
                        user.getOrganization().getId(), vendorDet.getId());
                String vendBranches = "";
                String openingBalance = "";
                String openingBalanceAP = "";
                for (BranchVendors bnchVendors : vendorBranches) {
                    vendBranches += bnchVendors.getBranch().getId() + ",";
                    openingBalance += bnchVendors.getOriginalOpeningBalance() == null ? ""
                            : bnchVendors.getOriginalOpeningBalance() + ",";
                    openingBalanceAP += bnchVendors.getOriginalOpeningBalanceAdvPaid() == null ? ""
                            : bnchVendors.getOriginalOpeningBalanceAdvPaid() + ",";
                }
                if (!vendBranches.equals("")) {
                    vendBranches = vendBranches.substring(0, vendBranches.length() - 1);
                }
                if (!openingBalance.equals("")) {
                    openingBalance = openingBalance.substring(0, openingBalance.length() - 1);
                }
                if (!openingBalanceAP.equals("")) {
                    openingBalanceAP = openingBalanceAP.substring(0, openingBalanceAP.length() - 1);
                }
                row.put("vendBranches", vendBranches);
                row.put("branchOpeningBalance", openingBalance);
                row.put("branchOpeningBalanceAP", openingBalanceAP);
                row.put("entityType", vendorDet.getType());
                StringBuilder customerDetailIdHid = new StringBuilder();
                StringBuilder gstinCodeHid = new StringBuilder();
                StringBuilder customerAddressHid = new StringBuilder();
                StringBuilder customercountryCodeHid = new StringBuilder();
                StringBuilder custstateHid = new StringBuilder();
                StringBuilder custstatecodeHid = new StringBuilder();
                StringBuilder custlocationHid = new StringBuilder();
                StringBuilder custPhnNocountryCodeHid = new StringBuilder();
                // StringBuilder custPhnNocountryTextHid = new StringBuilder();
                StringBuilder custphone1Hid = new StringBuilder();
                StringBuilder custphone2Hid = new StringBuilder();
                StringBuilder custphone3Hid = new StringBuilder();

                StringBuilder isShippingAddressSameHid = new StringBuilder();
                StringBuilder shipcustomerAddressHid = new StringBuilder();
                StringBuilder shipcustomerCountryCodeHid = new StringBuilder();
                StringBuilder shipstateHid = new StringBuilder();
                StringBuilder shipstateCodeHid = new StringBuilder();
                StringBuilder shiptlocationHid = new StringBuilder();
                StringBuilder shipcustPhnNoCountryCodeHid = new StringBuilder();
                // StringBuilder shipcustPhnNoCountryTextHid = new StringBuilder();
                StringBuilder shipcustPhone1Hid = new StringBuilder();
                StringBuilder shipcustPhone2Hid = new StringBuilder();
                StringBuilder shipcustPhone3Hid = new StringBuilder();
                String customerDetailId = "0";

                // Vendor Branch and Billwise Opening Balance
                BILLWISE_OPENING_BALANCE_SERVICE.getCustomerOpeningBalance(results, user, vendorDet, entityManager);
                // BRANCHWISE_ADVANCE_BALANCE_SERVICE.getCustomerAdvanceBalance(results, user,
                // vendorDet, entityManager);

                for (CustomerDetail customerDetail : CustomerDetail.findGstByCustomerID(entityManager,
                        vendorDet.getId())) {
                    if (vendorDet.getIsRegistered() == 1) {
                        if (customerDetail.getGstin() != null && vendorDet.getGstin() != null
                                && vendorDet.getGstin().equals(customerDetail.getGstin())) {
                            row.put("shippingSameAsBilling", customerDetail.getIsSameAsBillingAddress());
                            row.put("shippingvendPhnCtryCode", customerDetail.getShippingphoneCtryCode() == null ? ""
                                    : customerDetail.getShippingphoneCtryCode());
                            if (customerDetail.getShippingphone() != null) {
                                int k = customerDetail.getShippingphone().indexOf("-");
                                row.put("shippingPhone", customerDetail.getShippingphone().substring(k + 1,
                                        customerDetail.getShippingphone().length()));
                            } else {
                                row.put("shippingPhone", "");
                            }
                            if (customerDetail.getShippingcountry() != null) {
                                row.put("shippingCountry", customerDetail.getShippingcountry());
                            } else {
                                row.put("shippingCountry", "");
                            }
                            if (customerDetail.getShippingState() != null) {
                                row.put("shippingState", customerDetail.getShippingState());
                            } else {
                                row.put("shippingState", "");
                            }
                            if (customerDetail.getShippingaddress() != null) {
                                row.put("shippingAddress", customerDetail.getShippingaddress());
                            } else {
                                row.put("shippingAddress", "");
                            }
                            row.put("shippingLocation", customerDetail.getShippinglocation() == null ? ""
                                    : customerDetail.getShippinglocation());
                        } else {
                            ObjectNode row1 = Json.newObject();
                            if (customerDetail.getGstin() != null && customerDetail.getGstin().length() == 15) {
                                row1.put("gstinCode", customerDetail.getGstin());
                            } else {
                                row1.put("gstinCode", "");
                            }
                            if (customerDetail.getShippingState() != null) {
                                row1.put("gstState", customerDetail.getShippingState());
                            } else {
                                row1.put("gstState", "");
                            }
                            row1.put("customerDetID", customerDetail.getId());
                            row1.put("customerStatus", customerDetail.getPresentStatus());
                            row1.put("shippingLocation", customerDetail.getShippinglocation() == null ? ""
                                    : customerDetail.getShippinglocation());
                            custGstinState.add(row1);

                            customerDetailIdHid.append("|").append(customerDetail.getId());
                            customerDetailId = String.valueOf(customerDetail.getId());
                            gstinCodeHid.append("|")
                                    .append(customerDetail.getGstin() == null ? "" : customerDetail.getGstin());
                            customerAddressHid.append("|").append(
                                    customerDetail.getBillingaddress() == null ? ""
                                            : customerDetail.getBillingaddress());
                            customercountryCodeHid.append("|").append(
                                    customerDetail.getBillingcountry() == null ? ""
                                            : customerDetail.getBillingcountry());
                            custstateHid.append("|").append(
                                    customerDetail.getBillingState() == null ? "" : customerDetail.getBillingState());
                            custstatecodeHid.append("|").append(customerDetail.getBillingStateCode() == null ? ""
                                    : customerDetail.getBillingStateCode());
                            custlocationHid.append("|").append(
                                    customerDetail.getBillinglocation() == null ? ""
                                            : customerDetail.getBillinglocation());
                            custPhnNocountryCodeHid.append("|")
                                    .append(customerDetail.getBillingphoneCtryCode() == null ? ""
                                            : customerDetail.getBillingphoneCtryCode());
                            String phone = customerDetail.getBillingphone() == null ? ""
                                    : customerDetail.getBillingphone();
                            if (phone.length() > 2) {
                                custphone1Hid.append("|").append(phone.substring(0, 3));
                            } else {
                                custphone1Hid.append("|");
                            }
                            if (phone.length() > 5) {
                                custphone2Hid.append("|").append(phone.substring(3, 6));
                            } else {
                                custphone2Hid.append("|");
                            }
                            if (phone.length() > 6) {
                                custphone3Hid.append("|").append(phone.substring(6));
                            } else {
                                custphone3Hid.append("|");
                            }
                            if (customerDetail.getIsSameAsBillingAddress() == 1) {
                                isShippingAddressSameHid.append("|").append("true");
                            } else {
                                isShippingAddressSameHid.append("|").append("false");
                            }
                            shipcustomerAddressHid.append("|").append(
                                    customerDetail.getShippingaddress() == null ? ""
                                            : customerDetail.getShippingaddress());
                            shipcustomerCountryCodeHid.append("|").append(
                                    customerDetail.getShippingcountry() == null ? ""
                                            : customerDetail.getShippingcountry());
                            shipstateHid.append("|").append(
                                    customerDetail.getShippingState() == null ? "" : customerDetail.getShippingState());
                            if (customerDetail.getGstin() != null && customerDetail.getGstin().length() > 2) {
                                shipstateCodeHid.append("|").append(customerDetail.getGstin().substring(0, 2));
                            } else {
                                shipstateCodeHid.append("|").append(customerDetail.getGstin());
                            }

                            shiptlocationHid.append("|").append(customerDetail.getShippinglocation() == null ? ""
                                    : customerDetail.getShippinglocation());
                            shipcustPhnNoCountryCodeHid.append("|")
                                    .append(customerDetail.getShippingphoneCtryCode() == null ? ""
                                            : customerDetail.getShippingphoneCtryCode());
                            phone = customerDetail.getShippingphone() == null ? "" : customerDetail.getShippingphone();
                            if (phone.length() > 2) {
                                shipcustPhone1Hid.append("|").append(phone.substring(0, 3));
                            } else {
                                shipcustPhone1Hid.append("|");
                            }
                            if (phone.length() > 5) {
                                shipcustPhone2Hid.append("|").append(phone.substring(3, 6));
                            } else {
                                shipcustPhone2Hid.append("|");
                            }
                            if (phone.length() > 6) {
                                shipcustPhone3Hid.append("|").append(phone.substring(6));
                            } else {
                                shipcustPhone3Hid.append("|");
                            }
                        }
                    } else {
                        row.put("shippingSameAsBilling", customerDetail.getIsSameAsBillingAddress());
                        row.put("shippingvendPhnCtryCode", customerDetail.getShippingphoneCtryCode() == null ? ""
                                : customerDetail.getShippingphoneCtryCode());
                        if (customerDetail.getShippingphone() != null) {
                            int k = customerDetail.getShippingphone().indexOf("-");
                            row.put("shippingPhone", customerDetail.getShippingphone().substring(k + 1,
                                    customerDetail.getShippingphone().length()));
                        } else {
                            row.put("shippingPhone", "");
                        }
                        if (customerDetail.getShippingcountry() != null) {
                            row.put("shippingCountry", customerDetail.getShippingcountry());
                        } else {
                            row.put("shippingCountry", "");
                        }
                        if (customerDetail.getShippingState() != null) {
                            row.put("shippingState", customerDetail.getShippingState());
                        } else {
                            row.put("shippingState", "");
                        }
                        if (customerDetail.getShippingaddress() != null) {
                            row.put("shippingAddress", customerDetail.getShippingaddress());
                        } else {
                            row.put("shippingAddress", "");
                        }
                        row.put("shippingLocation", customerDetail.getShippinglocation() == null ? ""
                                : customerDetail.getShippinglocation());
                    }

                }
                results.put("customerDetailIdHid", customerDetailId);
                results.put("customerDetailIdListHid", customerDetailIdHid.toString());
                results.put("gstinCodeHid", gstinCodeHid.toString());
                results.put("customerAddressHid", customerAddressHid.toString());
                results.put("customercountryCodeHid", customercountryCodeHid.toString());
                results.put("custstateHid", custstateHid.toString());
                results.put("custstatecodeHid", custstatecodeHid.toString());
                results.put("custlocationHid", custlocationHid.toString());
                results.put("custPhnNocountryCodeHid", custPhnNocountryCodeHid.toString());
                results.put("custphone1Hid", custphone1Hid.toString());
                results.put("custphone2Hid", custphone2Hid.toString());
                results.put("custphone3Hid", custphone3Hid.toString());
                results.put("isShippingAddressSameHid", isShippingAddressSameHid.toString());
                results.put("shipcustomerAddressHid", shipcustomerAddressHid.toString());
                results.put("shipcustomerCountryCodeHid", shipcustomerCountryCodeHid.toString());
                results.put("shipstateHid", shipstateHid.toString());
                results.put("shipStateCodeHid", shipstateCodeHid.toString());
                results.put("shiptlocationHid", shiptlocationHid.toString());
                results.put("shipcustPhnNoCountryCodeHid", shipcustPhnNoCountryCodeHid.toString());
                results.put("shipcustPhone1Hid", shipcustPhone1Hid.toString());
                results.put("shipcustPhone2Hid", shipcustPhone2Hid.toString());
                results.put("shipcustPhone3Hid", shipcustPhone3Hid.toString());
                vendordetailan.add(row);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "ShowCustomerDetails Email", "ShowCustomerDetails Organization",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>>>>>> end " + results);
        return Results.ok(results).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result searchCustomer(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            JsonNode json = request.body().asJson();
            ArrayNode serachVendorData = result.putArray("vendorListData");
            // String email = json.findValue("usermail").asText();
            // session.adding("email", email);
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            List<UsersRoles> userRoles = user.getUserRoles();
            StringBuilder userRolesStr = new StringBuilder();
            for (UsersRoles roles : userRoles) {
                userRolesStr.append(roles.getRole().getName()).append(",");
            }
            StringBuilder userRoleID = new StringBuilder();
            for (UsersRoles roles : userRoles) {
                userRolesStr.append(roles.getRole().getName()).append(",");
                userRoleID.append(roles.getRole()).append(",");
            }
            result.put("userRole", userRoleID.toString());
            result.put("userRoles", userRolesStr.toString());
            Integer canActivateCustomer;
            if (user.canActivateCustomer() == true) {
                canActivateCustomer = 1;
                result.put("canActivateCustomer", canActivateCustomer);
            }
            if (user.canActivateVendor() == false) {
                canActivateCustomer = 0;
                result.put("canActivateCustomer", canActivateCustomer);
            }
            String enteredCustomerValue = json.findValue("freeTextSearchCustomerVal").asText();
            List<Vendor> vendors = null;
            if (enteredCustomerValue != null && !enteredCustomerValue.equals("")) {
                enteredCustomerValue = enteredCustomerValue.toLowerCase();
                String newsbquery = "select obj from Vendor obj WHERE obj.organization.id =?1 and (lower(obj.name) like ?2 or lower(obj.location) like ?3 or lower(obj.email) like ?4 or lower(obj.phone) like ?5 or lower(obj.address) like ?6) AND obj.type=2 order by obj.createdAt desc";
                ArrayList inparam = new ArrayList(6);
                inparam.add(user.getOrganization().getId());
                inparam.add("%" + enteredCustomerValue + "%");
                inparam.add("%" + enteredCustomerValue + "%");
                inparam.add("%" + enteredCustomerValue + "%");
                inparam.add("%" + enteredCustomerValue + "%");
                inparam.add("%" + enteredCustomerValue + "%");
                vendors = genericDAO.queryWithParams(newsbquery, entityManager, inparam);
            } else {
                vendors = Vendor.findByOrgIdAndType(entityManager, user.getOrganization().getId(), 2);
            }
            // Map<String, String> countries = new TreeMap<String, String>();
            // int count=0;
            // for (String country : currencies.keySet()) {
            // count++;
            // countries.put(String.valueOf(count), country);
            // }
            if (vendors != null && vendors.size() > 0) {
                for (Vendor vendor : vendors) {
                    ObjectNode row = Json.newObject();
                    row.put("id", vendor.getId());
                    row.put("name", vendor.getName());
                    // row.put("code",vendor.getCustomerCode());

                    if (vendor.getAddress() != null && !vendor.getAddress().equals("")) {
                        // row.put("address", countries.get(vendor.getCountry().toString()));
                        row.put("address", vendor.getAddress());
                    } else {
                        row.put("address", "");
                    }
                    row.put("location", vendor.getLocation());
                    row.put("contract", vendor.getContractPoDoc());
                    row.put("email", vendor.getEmail());
                    row.put("phone", vendor.getPhone());
                    row.put("grantAccess", vendor.getGrantAccess());
                    if (vendor.getValidityFrom() != null) {
                        row.put("validFrom", idosdf.format(vendor.getValidityFrom()));
                    }
                    if (vendor.getValidityTo() != null) {
                        row.put("validTo", idosdf.format(vendor.getValidityTo()));
                    }
                    if (userRolesStr.indexOf("MASTER ADMIN") != -1
                            || (userRolesStr.indexOf("APPROVER") != -1 && user.canActivateCustomer())) {
                        row.put("presentStatus", vendor.getPresentStatus());
                    } else {
                        row.put("presentStatus", "-1");
                    }
                    serachVendorData.add(row);
                }
            } else {
                String newsbquery = "select obj from Vendor obj WHERE obj.organization.id =?1 and (obj.vendorGroup IS NOT NULL and obj.vendorGroup.groupName like ?2) AND obj.type=2 and obj.presentStatus=1";
                ArrayList inparam = new ArrayList(2);
                inparam.add(user.getOrganization().getId());
                inparam.add(enteredCustomerValue + "%");
                vendors = genericDAO.queryWithParams(newsbquery, entityManager, inparam);
                if (vendors != null && vendors.size() == 0) {
                    String bnchvendnewsbquery = "select obj from BranchVendors obj where obj.organization.id =?1 and obj.branch.name like ?2 AND obj.vendor.type=2 and obj.presentStatus=1 GROUP BY obj.vendor.id, obj.id";
                    inparam.clear();
                    inparam.add(user.getOrganization().getId());
                    inparam.add(enteredCustomerValue + "%");
                    List<BranchVendors> branchVendors = genericDAO.queryWithParams(bnchvendnewsbquery, entityManager,
                            inparam);
                    if (branchVendors.size() > 0) {
                        for (BranchVendors bnchvendor : branchVendors) {
                            ObjectNode row = Json.newObject();
                            row.put("id", bnchvendor.getVendor().getId());
                            row.put("name", bnchvendor.getVendor().getName());
                            // row.put("name", bnchvendor.getVendor().getCustomerCode());
                            if (bnchvendor.getVendor().getAddress() != null
                                    && !bnchvendor.getVendor().getAddress().equals("")) {
                                // row.put("address",
                                // countries.get(bnchvendor.getVendor().getCountry().toString()));
                                row.put("address", bnchvendor.getVendor().getAddress());
                            } else {
                                row.put("address", "");
                            }
                            row.put("location", bnchvendor.getVendor().getLocation());
                            row.put("contract", bnchvendor.getVendor().getContractPoDoc());
                            row.put("email", bnchvendor.getVendor().getEmail());
                            row.put("phone", bnchvendor.getVendor().getPhone());
                            row.put("grantAccess", bnchvendor.getVendor().getGrantAccess());
                            if (bnchvendor.getVendor().getValidityFrom() != null) {
                                row.put("validFrom", idosdf.format(bnchvendor.getVendor().getValidityFrom()));
                            }
                            if (bnchvendor.getVendor().getValidityTo() != null) {
                                row.put("validTo", idosdf.format(bnchvendor.getVendor().getValidityTo()));
                            }
                            if (userRolesStr.indexOf("MASTER ADMIN") != -1
                                    || (userRolesStr.indexOf("APPROVER") != -1 && user.canActivateCustomer())) {
                                row.put("presentStatus", bnchvendor.getVendor().getPresentStatus());
                            } else {
                                row.put("presentStatus", "-1");
                            }
                            serachVendorData.add(row);
                        }
                    }
                    if (branchVendors.size() == 0) {
                        String specificsvendnewsbquery = ("select obj from VendorSpecific obj where obj.organization.id = ?1 and obj.specificsVendors.name like ?2 AND obj.vendorSpecific.type=2 and obj.presentStatus=1 GROUP BY obj.vendorSpecific.id, obj.id");
                        inparam.clear();
                        inparam.add(user.getOrganization().getId());
                        inparam.add(enteredCustomerValue + "%");
                        List<VendorSpecific> specificsVendors = genericDAO.queryWithParams(specificsvendnewsbquery,
                                entityManager, inparam);
                        if (specificsVendors.size() > 0) {
                            for (VendorSpecific vendSpecifics : specificsVendors) {
                                ObjectNode row = Json.newObject();
                                row.put("id", vendSpecifics.getVendorSpecific().getId());
                                row.put("name", vendSpecifics.getVendorSpecific().getName());
                                // row.put("code", vendSpecifics.getVendorSpecific().getCustomerCode());
                                if (vendSpecifics.getVendorSpecific().getAddress() != null
                                        && !vendSpecifics.getVendorSpecific().getAddress().equals("")) {
                                    // row.put("address",
                                    // countries.get(vendSpecifics.getVendorSpecific().getCountry().toString()));
                                    row.put("address", vendSpecifics.getVendorSpecific().getAddress());
                                } else {
                                    row.put("address", "");
                                }
                                row.put("location", vendSpecifics.getVendorSpecific().getLocation());
                                row.put("contract", vendSpecifics.getVendorSpecific().getContractPoDoc());
                                row.put("email", vendSpecifics.getVendorSpecific().getEmail());
                                row.put("phone", vendSpecifics.getVendorSpecific().getPhone());
                                row.put("grantAccess", vendSpecifics.getVendorSpecific().getGrantAccess());
                                if (vendSpecifics.getVendorSpecific().getValidityFrom() != null) {
                                    row.put("validFrom",
                                            idosdf.format(vendSpecifics.getVendorSpecific().getValidityFrom()));
                                }
                                if (vendSpecifics.getVendorSpecific().getValidityTo() != null) {
                                    row.put("validTo",
                                            idosdf.format(vendSpecifics.getVendorSpecific().getValidityTo()));
                                }

                                if (userRolesStr.indexOf("MASTER ADMIN") != -1
                                        || (userRolesStr.indexOf("APPROVER") != -1 && user.canActivateCustomer())) {
                                    row.put("presentStatus", vendSpecifics.getVendorSpecific().getPresentStatus());
                                } else {
                                    row.put("presentStatus", "-1");
                                }
                                serachVendorData.add(row);
                            }
                        }
                    }
                } else {
                    for (Vendor vendor : vendors) {
                        ObjectNode row = Json.newObject();
                        row.put("id", vendor.getId());
                        row.put("name", vendor.getName());
                        /*
                         * if(vendor.getCustomerCode()!=null && !vendor.getCustomerCode().equals("")){
                         * row.put("code", vendor.getCustomerCode());
                         * }
                         * else{
                         * row.put("code","");
                         * }
                         */
                        if (vendor.getAddress() != null && !vendor.getAddress().equals("")) {
                            // row.put("address", countries.get(vendor.getCountry().toString()));
                            row.put("address", vendor.getAddress());
                        } else {
                            row.put("address", "");
                        }
                        row.put("location", vendor.getLocation());
                        row.put("contract", vendor.getContractPoDoc());
                        row.put("email", vendor.getEmail());
                        row.put("phone", vendor.getPhone());
                        row.put("grantAccess", vendor.getGrantAccess());
                        if (vendor.getValidityFrom() != null) {
                            row.put("validFrom", idosdf.format(vendor.getValidityFrom()));
                        }
                        if (vendor.getValidityTo() != null) {
                            row.put("validTo", idosdf.format(vendor.getValidityTo()));
                        }
                        if (userRolesStr.indexOf("MASTER ADMIN") != -1
                                || (userRolesStr.indexOf("APPROVER") != -1 && user.canActivateCustomer())) {
                            row.put("presentStatus", vendor.getPresentStatus());
                        } else {
                            row.put("presentStatus", "-1");
                        }
                        serachVendorData.add(row);
                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result searchCustomerName(Request request) {

        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            JsonNode json = request.body().asJson();
            ArrayNode serachVendorData = result.putArray("vendorListData");
            // String email = json.findValue("usermail").asText();
            // session.adding("email", email);
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            List<UsersRoles> userRoles = user.getUserRoles();
            StringBuilder userRolesStr = new StringBuilder();
            for (UsersRoles roles : userRoles) {
                userRolesStr.append(roles.getRole().getName()).append(",");
            }
            StringBuilder userRoleID = new StringBuilder();
            for (UsersRoles roles : userRoles) {
                userRolesStr.append(roles.getRole().getName()).append(",");
                userRoleID.append(roles.getRole()).append(",");
            }
            result.put("userRole", userRoleID.toString());
            result.put("userRoles", userRolesStr.toString());
            Integer canActivateCustomer;
            if (user.canActivateCustomer() == true) {
                canActivateCustomer = 1;
                result.put("canActivateCustomer", canActivateCustomer);
            }
            if (user.canActivateVendor() == false) {
                canActivateCustomer = 0;
                result.put("canActivateCustomer", canActivateCustomer);
            }
            String enteredCustomerValue = json.findValue("cName").asText();
            List<Vendor> vendors = null;
            if (!enteredCustomerValue.equals("")) {
                String newsbquery = "select obj from Vendor obj WHERE obj.organization.id =?1 and (obj.name like ?2 or obj.location like ?3 or obj.email like ?4 or obj.phone like ?5 or obj.address like ?6) AND obj.type=2 and obj.presentStatus=1";
                ArrayList inparam = new ArrayList(6);
                inparam.add(user.getOrganization().getId());
                inparam.add(enteredCustomerValue + "%");
                inparam.add(enteredCustomerValue + "%");
                inparam.add(enteredCustomerValue + "%");
                inparam.add(enteredCustomerValue + "%");
                inparam.add(enteredCustomerValue + "%");
                vendors = genericDAO.queryWithParams(newsbquery, entityManager, inparam);
            } else {
                vendors = Vendor.findByOrgIdAndType(entityManager, user.getOrganization().getId(), 2);
            }
            // Map<String, String> countries = new TreeMap<String, String>();
            // int count=0;
            // for (String country : currencies.keySet()) {
            // count++;
            // countries.put(String.valueOf(count), country);
            // }
            if (vendors != null && vendors.size() > 0) {
                for (Vendor vendor : vendors) {
                    ObjectNode row = Json.newObject();
                    row.put("id", vendor.getId());
                    row.put("name", vendor.getName());
                    // row.put("code",vendor.getCustomerCode());

                    if (vendor.getAddress() != null && !vendor.getAddress().equals("")) {
                        // row.put("address", countries.get(vendor.getCountry().toString()));
                        row.put("address", vendor.getAddress());
                    } else {
                        row.put("address", "");
                    }
                    row.put("location", vendor.getLocation());
                    row.put("contract", vendor.getContractPoDoc());
                    row.put("email", vendor.getEmail());
                    row.put("phone", vendor.getPhone());
                    row.put("grantAccess", vendor.getGrantAccess());
                    if (vendor.getValidityFrom() != null) {
                        row.put("validFrom", idosdf.format(vendor.getValidityFrom()));
                    }
                    if (vendor.getValidityTo() != null) {
                        row.put("validTo", idosdf.format(vendor.getValidityTo()));
                    }
                    if (userRolesStr.indexOf("MASTER ADMIN") != -1
                            || (userRolesStr.indexOf("APPROVER") != -1 && user.canActivateCustomer())) {
                        row.put("presentStatus", vendor.getPresentStatus());
                    } else {
                        row.put("presentStatus", "-1");
                    }
                    serachVendorData.add(row);
                }
            } else {
                String newsbquery = "select obj from Vendor obj WHERE obj.organization.id =?1 and (obj.vendorGroup IS NOT NULL and obj.vendorGroup.groupName like ?2) AND obj.type=2 and obj.presentStatus=1";
                ArrayList inparam = new ArrayList(2);
                inparam.add(user.getOrganization().getId());
                inparam.add(enteredCustomerValue + "%");
                vendors = genericDAO.queryWithParams(newsbquery, entityManager, inparam);
                if (vendors != null && vendors.size() == 0) {
                    String bnchvendnewsbquery = "select obj from BranchVendors obj where obj.organization.id =?1 and obj.branch.name like ?2 AND obj.vendor.type=2 and obj.presentStatus=1 GROUP BY obj.vendor.id";
                    inparam.clear();
                    inparam.add(user.getOrganization().getId());
                    inparam.add(enteredCustomerValue + "%");
                    List<BranchVendors> branchVendors = genericDAO.queryWithParams(bnchvendnewsbquery, entityManager,
                            inparam);
                    if (branchVendors.size() > 0) {
                        for (BranchVendors bnchvendor : branchVendors) {
                            ObjectNode row = Json.newObject();
                            row.put("id", bnchvendor.getVendor().getId());
                            row.put("name", bnchvendor.getVendor().getName());
                            // row.put("name", bnchvendor.getVendor().getCustomerCode());
                            if (bnchvendor.getVendor().getAddress() != null
                                    && !bnchvendor.getVendor().getAddress().equals("")) {
                                // row.put("address",
                                // countries.get(bnchvendor.getVendor().getCountry().toString()));
                                row.put("address", bnchvendor.getVendor().getAddress());
                            } else {
                                row.put("address", "");
                            }
                            row.put("location", bnchvendor.getVendor().getLocation());
                            row.put("contract", bnchvendor.getVendor().getContractPoDoc());
                            row.put("email", bnchvendor.getVendor().getEmail());
                            row.put("phone", bnchvendor.getVendor().getPhone());
                            row.put("grantAccess", bnchvendor.getVendor().getGrantAccess());
                            if (bnchvendor.getVendor().getValidityFrom() != null) {
                                row.put("validFrom", idosdf.format(bnchvendor.getVendor().getValidityFrom()));
                            }
                            if (bnchvendor.getVendor().getValidityTo() != null) {
                                row.put("validTo", idosdf.format(bnchvendor.getVendor().getValidityTo()));
                            }
                            if (userRolesStr.indexOf("MASTER ADMIN") != -1
                                    || (userRolesStr.indexOf("APPROVER") != -1 && user.canActivateCustomer())) {
                                row.put("presentStatus", bnchvendor.getVendor().getPresentStatus());
                            } else {
                                row.put("presentStatus", "-1");
                            }
                            serachVendorData.add(row);
                        }
                    }
                    if (branchVendors.size() == 0) {
                        String specificsvendnewsbquery = ("select obj from VendorSpecific obj where obj.organization.id = ?1 and obj.specificsVendors.name like ?2 AND obj.vendorSpecific.type=2 and obj.presentStatus=1 GROUP BY obj.vendorSpecific.id");
                        inparam.clear();
                        inparam.add(user.getOrganization().getId());
                        inparam.add(enteredCustomerValue + "%");
                        List<VendorSpecific> specificsVendors = genericDAO.queryWithParams(specificsvendnewsbquery,
                                entityManager, inparam);
                        if (specificsVendors.size() > 0) {
                            for (VendorSpecific vendSpecifics : specificsVendors) {
                                ObjectNode row = Json.newObject();
                                row.put("id", vendSpecifics.getVendorSpecific().getId());
                                row.put("name", vendSpecifics.getVendorSpecific().getName());
                                // row.put("code", vendSpecifics.getVendorSpecific().getCustomerCode());
                                if (vendSpecifics.getVendorSpecific().getAddress() != null
                                        && !vendSpecifics.getVendorSpecific().getAddress().equals("")) {
                                    // row.put("address",
                                    // countries.get(vendSpecifics.getVendorSpecific().getCountry().toString()));
                                    row.put("address", vendSpecifics.getVendorSpecific().getAddress());
                                } else {
                                    row.put("address", "");
                                }
                                row.put("location", vendSpecifics.getVendorSpecific().getLocation());
                                row.put("contract", vendSpecifics.getVendorSpecific().getContractPoDoc());
                                row.put("email", vendSpecifics.getVendorSpecific().getEmail());
                                row.put("phone", vendSpecifics.getVendorSpecific().getPhone());
                                row.put("grantAccess", vendSpecifics.getVendorSpecific().getGrantAccess());
                                if (vendSpecifics.getVendorSpecific().getValidityFrom() != null) {
                                    row.put("validFrom",
                                            idosdf.format(vendSpecifics.getVendorSpecific().getValidityFrom()));
                                }
                                if (vendSpecifics.getVendorSpecific().getValidityTo() != null) {
                                    row.put("validTo",
                                            idosdf.format(vendSpecifics.getVendorSpecific().getValidityTo()));
                                }

                                if (userRolesStr.indexOf("MASTER ADMIN") != -1
                                        || (userRolesStr.indexOf("APPROVER") != -1 && user.canActivateCustomer())) {
                                    row.put("presentStatus", vendSpecifics.getVendorSpecific().getPresentStatus());
                                } else {
                                    row.put("presentStatus", "-1");
                                }
                                serachVendorData.add(row);
                            }
                        }
                    }
                } else {
                    for (Vendor vendor : vendors) {
                        ObjectNode row = Json.newObject();
                        row.put("id", vendor.getId());
                        row.put("name", vendor.getName());
                        /*
                         * if(vendor.getCustomerCode()!=null && !vendor.getCustomerCode().equals("")){
                         * row.put("code", vendor.getCustomerCode());
                         * }
                         * else{
                         * row.put("code","");
                         * }
                         */
                        if (vendor.getAddress() != null && !vendor.getAddress().equals("")) {
                            // row.put("address", countries.get(vendor.getCountry().toString()));
                            row.put("address", vendor.getAddress());
                        } else {
                            row.put("address", "");
                        }
                        row.put("location", vendor.getLocation());
                        row.put("contract", vendor.getContractPoDoc());
                        row.put("email", vendor.getEmail());
                        row.put("phone", vendor.getPhone());
                        row.put("grantAccess", vendor.getGrantAccess());
                        if (vendor.getValidityFrom() != null) {
                            row.put("validFrom", idosdf.format(vendor.getValidityFrom()));
                        }
                        if (vendor.getValidityTo() != null) {
                            row.put("validTo", idosdf.format(vendor.getValidityTo()));
                        }
                        if (userRolesStr.indexOf("MASTER ADMIN") != -1
                                || (userRolesStr.indexOf("APPROVER") != -1 && user.canActivateCustomer())) {
                            row.put("presentStatus", vendor.getPresentStatus());
                        } else {
                            row.put("presentStatus", "-1");
                        }
                        serachVendorData.add(row);
                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result).withHeader("ContentType", "application/json");

    }

    @Transactional
    public Result getOpenignBalAdvCustomer(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        ObjectNode result = Json.newObject();
        ArrayNode openingBalAdvAn = result.putArray("custVendOpeningBalAdvData");
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            Map<String, Object> criterias = new HashMap<String, Object>();
            JsonNode json = request.body().asJson();
            String useremail = json.findValue("usermail").asText();
            Long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
            String creditCustomer = json.findValue("creditCustomer").asText();
            Long advanceOption = json.findValue("advanceOption").asLong();

            Vendor selectedVendor = Vendor.findById(IdosUtil.convertStringToLong(creditCustomer));
            session.adding("email", useremail);
            user = getUserInfo(request);
            VendorSpecific customerTxnSpecifics = null;
            // Opening Balance paid by customer/vendor from setup screen i.e. account
            // receivables/payables from previous year. It is not per item, but overall
            // paybles by that customer
            if (advanceOption == 2) {
                Double advance = 0.0;
                if (selectedVendor.getTotalOpeningBalanceAdvPaid() != null
                        && selectedVendor.getTotalOpeningBalanceAdvPaid() > 0) {
                    advance = selectedVendor.getTotalOpeningBalanceAdvPaid();
                }
                ObjectNode rowOB = Json.newObject();
                rowOB.put("custVendorAdvance", advance);
                openingBalAdvAn.add(rowOB);
            } else {// 1=advance for specifics for that customer
                if (txnPurposeVal == 5 || txnPurposeVal == 7) {// receive payment from customer / Pay vendor
                    String pendingInvoice = json.findValue("incomeItem").asText();// transaction for which payment from
                                                                                  // customer, gives specifics info
                    Transaction pendingTransaction = Transaction.findById(IdosUtil.convertStringToLong(pendingInvoice));
                    criterias.clear();
                    criterias.put("vendorSpecific.id", selectedVendor.getId());
                    criterias.put("specificsVendors.id", pendingTransaction.getTransactionSpecifics().getId());
                    criterias.put("organization.id", user.getOrganization().getId());
                    criterias.put("presentStatus", 1);
                    customerTxnSpecifics = genericDAO.getByCriteria(VendorSpecific.class, criterias, entityManager);

                } else if (txnPurposeVal == 3 || txnPurposeVal == 4) { // buy on cash/credit
                    String txnforitem = json.findValue("incomeItem").asText();
                    if (txnforitem != null && !txnforitem.equals("")) {
                        Specifics itemSpecifics = genericDAO.getById(Specifics.class,
                                IdosUtil.convertStringToLong(txnforitem), entityManager);
                        criterias.clear();
                        criterias.put("vendorSpecific.id", selectedVendor.getId());
                        criterias.put("specificsVendors.id", itemSpecifics.getId());
                        criterias.put("organization.id", user.getOrganization().getId());
                        criterias.put("presentStatus", 1);
                        customerTxnSpecifics = genericDAO.getByCriteria(VendorSpecific.class, criterias, entityManager);
                    }
                }
                Double advance = 0.0;
                if (customerTxnSpecifics != null && customerTxnSpecifics.getAdvanceMoney() != null) {
                    advance = customerTxnSpecifics.getAdvanceMoney();
                }
                ObjectNode row = Json.newObject();
                row.put("custVendorAdvance", advance);
                openingBalAdvAn.add(row);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }

        return Results.ok(result);
    }

    @Transactional
    public Result getCustomerListForBranch(Request request) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        ArrayNode custListForBranchAn = result.putArray("custListForBranch");
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            JsonNode json = request.body().asJson();
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            String useremail = json.findValue("useremail").asText();
            Long txnPurposeId = json.findValue("txnPurposeId").asLong();
            Long txnPurposeBnchId = json.findValue("txnPurposeBnchId").asLong();
            StringBuilder sbquery = new StringBuilder(
                    "select obj from Vendor obj WHERE obj.organization.id=?1 and obj.presentStatus=1 ");
            if (txnPurposeId == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW) { // purchaseType 0=credit, 1=cash,
                                                                                  // 2=both
                sbquery.append(" and obj.type=2 and (obj.purchaseType=1 or obj.purchaseType=2)");
            } else if (txnPurposeId == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
                    || txnPurposeId == IdosConstants.CREDIT_NOTE_CUSTOMER
                    || txnPurposeId == IdosConstants.DEBIT_NOTE_CUSTOMER
                    || txnPurposeId == IdosConstants.CANCEL_INVOICE) { // purchaseType 0=credit, 1=cash, 2=both
                sbquery.append(" and obj.type=2 and (obj.purchaseType=0 or obj.purchaseType=2)");
            } else if (txnPurposeId == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                    || txnPurposeId == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) { // purchaseType 0=credit, 1=cash,
                                                                                  // 2=both
                sbquery.append(" and obj.type=1 and (obj.purchaseType=1 or obj.purchaseType=2)");
            } else if (txnPurposeId == IdosConstants.BUY_ON_CREDIT_PAY_LATER
                    || txnPurposeId == IdosConstants.CREDIT_NOTE_VENDOR
                    || txnPurposeId == IdosConstants.DEBIT_NOTE_VENDOR) { // purchaseType 0=credit, 1=cash, 2=both
                sbquery.append(" and obj.type=1 and (obj.purchaseType=0 or obj.purchaseType=2)");

            } else if (txnPurposeId == IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER
                    || txnPurposeId == IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER
                    || txnPurposeId == IdosConstants.SALES_RETURNS
                    || txnPurposeId == IdosConstants.REFUND_ADVANCE_RECEIVED
                    || txnPurposeId == IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE) {
                sbquery.append(" and obj.type=2 and (obj.purchaseType=0 or obj.purchaseType=2)");
            } else if (txnPurposeId == IdosConstants.PAY_VENDOR_SUPPLIER
                    || txnPurposeId == IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER
                    || txnPurposeId == IdosConstants.PURCHASE_RETURNS || txnPurposeId == IdosConstants.PURCHASE_ORDER) {
                sbquery.append(" and obj.type=1 and (obj.purchaseType=0 or obj.purchaseType=2)");
            } else if (txnPurposeId == IdosConstants.PREPARE_QUOTATION
                    || txnPurposeId == IdosConstants.PROFORMA_INVOICE) { // "Quotation"
                sbquery.append(" and obj.type=2");
            } else if (txnPurposeId == IdosConstants.CREATE_PURCHASE_ORDER
                    || txnPurposeId == IdosConstants.MATERIAL_ISSUE_NOTE) {
                sbquery.append(" and obj.type=1");
            }
            ArrayList inparam = new ArrayList(1);
            inparam.add(user.getOrganization().getId());
            List<Vendor> customersForTheOrg = genericDAO.queryWithParams(sbquery.toString(), entityManager, inparam);
            Map<String, Object> criterias = new HashMap<String, Object>();
            if (customersForTheOrg != null && customersForTheOrg.size() > 0) {
                for (Vendor custForOrg : customersForTheOrg) {
                    criterias.put("branch.id", txnPurposeBnchId);
                    criterias.put("organization.id", user.getOrganization().getId());
                    criterias.put("vendor.id", custForOrg.getId());
                    criterias.put("presentStatus", 1);
                    BranchVendors customerAvailableForTxnBranch = genericDAO.getByCriteria(BranchVendors.class,
                            criterias, entityManager);
                    if (customerAvailableForTxnBranch != null) {
                        ObjectNode row = Json.newObject();
                        row.put("customerName", customerAvailableForTxnBranch.getVendor().getName());
                        row.put("customerId", customerAvailableForTxnBranch.getVendor().getId());
                        row.put("customerType", customerAvailableForTxnBranch.getVendor().getType());
                        custListForBranchAn.add(row);
                    }
                    criterias.clear();
                }
            }
            if (txnPurposeId == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW) {
                // append list of walk-in customers who have paid advacne using "Receive adv
                // from customers
                String sbqueryHQL = "select obj from VendorSpecific obj WHERE obj.organization.id=?1 and obj.branch.id=?2 and obj.vendorSpecific.type=3 and obj.presentStatus=1 and obj.vendorSpecific.purchaseType=1 and obj.advanceMoney>0 ";

                inparam.clear();
                inparam.add(user.getOrganization().getId());
                inparam.add(user.getBranch().getId());
                List<VendorSpecific> walkincustomersForTheTxnSpecifics = genericDAO.queryWithParams(sbqueryHQL,
                        entityManager, inparam);
                for (VendorSpecific custForTxnSpecf : walkincustomersForTheTxnSpecifics) {
                    ObjectNode row = Json.newObject();
                    row.put("customerName", custForTxnSpecf.getVendorSpecific().getName() + ":Walk-In Customer");
                    row.put("customerId", custForTxnSpecf.getVendorSpecific().getId());
                    row.put("customerType", custForTxnSpecf.getVendorSpecific().getType());
                    custListForBranchAn.add(row);
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> End ");
        return Results.ok(result);
    }

    @Transactional
    public Result getShippingAddress(Long transID, Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized(result);
            }
            Map<String, Object> criterias = new HashMap<String, Object>();
            // JsonNode json = request.body().asJson();
            // Long transID = json.findValue("entityTxnId") == null ? null :
            // json.findValue("entityTxnId").asLong();
            Transaction transaction = genericDAO.getById(Transaction.class, transID, entityManager);
            Vendor customer = transaction.getTransactionVendorCustomer();
            if (customer != null && customer.getType() == IdosConstants.CUSTOMER) {
                CustomerDetail customerDetail = CustomerDetail.findByCustomerID(entityManager, customer.getId());
                result.put("custid", customer.getId());
                result.put("txnid", transaction.getId());
                if (customerDetail != null) {
                    result.put("shippingvendPhnCtryCode", customerDetail.getShippingphoneCtryCode() == null ? ""
                            : customerDetail.getShippingphoneCtryCode());
                    if (customerDetail.getShippingphone() != null) {
                        int k = customerDetail.getShippingphone().indexOf("-");
                        result.put("shippingPhone", customerDetail.getShippingphone().substring(k + 1,
                                customerDetail.getShippingphone().length()));
                    } else {
                        result.put("shippingPhone", "");
                    }
                    if (customerDetail.getShippingcountry() != null) {
                        result.put("shippingCountry", customerDetail.getShippingcountry());
                    } else {
                        result.put("shippingCountry", "");
                    }
                    if (customerDetail.getShippingaddress() != null) {
                        result.put("shippingAddress", customerDetail.getShippingaddress());
                    } else {
                        result.put("shippingAddress", "");
                    }
                    result.put("shippingLocation",
                            customerDetail.getShippinglocation() == null ? "" : customerDetail.getShippinglocation());
                } else {
                    result.put("shippingvendPhnCtryCode",
                            customer.getPhoneCtryCode() == null ? "" : customer.getPhoneCtryCode());
                    if (customer.getPhone() != null) {
                        int k = customer.getPhone().indexOf("-");
                        result.put("shippingPhone", customer.getPhone().substring(k + 1, customer.getPhone().length()));
                    } else {
                        result.put("shippingPhone", "");
                    }
                    if (customer.getCountry() != null) {
                        result.put("shippingCountry", customer.getCountry());
                    } else {
                        result.put("shippingCountry", "");
                    }
                    if (customer.getAddress() != null) {
                        result.put("shippingAddress", customer.getAddress());
                    } else {
                        result.put("shippingAddress", "");
                    }
                    result.put("shippingLocation", customer.getLocation() == null ? "" : customer.getLocation());
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return internalServerError(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result updateCustomerShippingDetail(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            JsonNode json = request.body().asJson();
            transaction.begin();
            customerService.saveCustomerShippingDetail(json, user, entityManager, transaction);
            transaction.commit();
        } catch (Exception ex) {
            reportException(entityManager, transaction, user, ex, result);
        } catch (Throwable th) {
            reportThrowable(entityManager, transaction, user, th, result);
        }
        return Results.ok(result);
    }

    @Transactional
    public Result getCustomerLocations(Request request) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        ArrayNode custGstinList = result.putArray("custGstinList");
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            JsonNode json = request.body().asJson();
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            int txnPurpose = json.findValue("txnPurpose").asInt();
            Long txnCustomerId = json.findValue("txnCustomerId").asLong();
            Vendor customer = Vendor.findById(txnCustomerId);
            if (customer != null) {
                List<CustomerDetail> customerGstinList = customer.getCustomerDetails();
                for (CustomerDetail customerDetail : customerGstinList) {
                    if (customerDetail.getPresentStatus() == 0) {
                        continue;
                    }
                    String shipState = null;
                    String custLocation = null;
                    String gstin = null;
                    String gstinStateCode = null;
                    // if(user.getOrganization().getPlaceOfSupplyType() != null &&
                    // user.getOrganization().getPlaceOfSupplyType() == 2){
                    if (customer.getPlaceOfSupplyType() != null && customer.getPlaceOfSupplyType() == 1) {
                        shipState = customerDetail.getBillingState();
                        custLocation = customerDetail.getBillinglocation();
                        gstinStateCode = customerDetail.getBillingStateCode();
                    } else {
                        shipState = customerDetail.getShippingState();
                        custLocation = customerDetail.getShippinglocation();
                        gstinStateCode = customerDetail.getShippingStateCode();
                    }
                    if (custLocation == null || "".equals(custLocation)) {
                        custLocation = "";
                    }
                    gstin = customerDetail.getGstin();
                    ObjectNode row = Json.newObject();
                    row.put("customerDetailId", customerDetail.getId());
                    if (gstin != null) {
                        custLocation = custLocation + "-" + gstin;
                        row.put("gstin", gstinStateCode);
                    } else {
                        row.put("gstin", "");
                    }
                    if (shipState != null && !"".equals(shipState)) {
                        custLocation = custLocation + "-" + shipState;
                    }
                    row.put("custLocation", custLocation);
                    custGstinList.add(row);
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result checkIfCustomerCreditLimitExceeded(Request request) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        ArrayNode custCreditLimitDetails = result.putArray("custCreditLimitInfo");
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            JsonNode json = request.body().asJson();
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            Long txnCustomerId = json.findValue("customerId").asLong();
            double netAmtOfTran = json.findValue("netAmt").asDouble();
            String txnForItemStr = json.findValue("txnForItem").toString();
            Vendor customer = Vendor.findById(txnCustomerId);
            if (customer != null) {
                Double creditLimit = customer.getCreditLimit();
                Integer processOrStopTransaction = customer.getExceedingCreditProcessStop();
                Integer excludeAdv = customer.getExcludeAdvFromCreLimCheck();
                ObjectNode row = Json.newObject();
                row.put("customerId", txnCustomerId);
                row.put("custCreditLimit", creditLimit);
                row.put("processOrStopTransaction", processOrStopTransaction);
                if (creditLimit != null && creditLimit > 0) {
                    // available credit limit = credit limit set - previous credit he owes(cust
                    // setup)
                    Double creditOpenBal = customer.getTotalOpeningBalance(); // previous credit balance he owes
                    double availableCreditForCust = 0.0;
                    availableCreditForCust = creditLimit;
                    if (creditOpenBal != null) {
                        availableCreditForCust = availableCreditForCust - creditOpenBal;
                    }
                    // Find totalCredit customer owes
                    // totalCreditForCustomer = creditsaletransaction values - sales return - partly
                    // net payment done(rece payment from cust from credit on sales invoices) -
                    // opening balance adv(cust setup) - advance paid (receiv adv from cust for
                    // item)
                    String sbquery = ("select sum(obj.customerNetPayment),sum(obj.salesReturnAmount), sum(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization.id=?1 and obj.transactionVendorCustomer.id=?2 and obj.transactionPurpose.id=2 and obj.transactionStatus ='Accounted' and obj.presentStatus=1 and obj.paymentStatus != 'PAID'");
                    double custNetPayment = 0.0, salesReturnAmt = 0.0, netAmount = 0.0, existingCreditForCustomer = 0.0,
                            totalCreditForCustomer = 0.0;
                    Query query = entityManager.createQuery(sbquery);
                    query.setParameter(1, user.getOrganization().getId());
                    query.setParameter(2, txnCustomerId);
                    List<Object[]> txnLists = query.getResultList();
                    if (txnLists != null) {
                        for (Object[] val : txnLists) {
                            custNetPayment += val[0] != null ? IdosUtil.convertStringToDouble(String.valueOf(val[0]))
                                    : 0.0;
                            salesReturnAmt += val[1] != null ? IdosUtil.convertStringToDouble(String.valueOf(val[1]))
                                    : 0.0;
                            netAmount += val[2] != null ? IdosUtil.convertStringToDouble(String.valueOf(val[2])) : 0.0;
                            existingCreditForCustomer = netAmount - custNetPayment - salesReturnAmt;
                        }
                    }
                    totalCreditForCustomer = existingCreditForCustomer + netAmtOfTran;
                    if (excludeAdv != null && excludeAdv != 1) { // if it is 1, it means exclude advance paid by
                                                                 // customer
                        totalCreditForCustomer = totalCreditForCustomer - customer.getTotalOpeningBalanceAdvPaid();
                    }
                    // Receive adv from cust for all those items
                    String itemSpecIds = "";
                    List<Long> itemList = new ArrayList<Long>();
                    JSONArray arrJSON = new JSONArray(txnForItemStr);
                    for (int i = 0; i < arrJSON.length(); i++) {
                        JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                        Long itemId = rowItemData.getLong("txnItems");
                        itemSpecIds = itemSpecIds + itemId + ",";
                        itemList.add(itemId);
                    }
                    String itemsList = itemSpecIds.substring(0, itemSpecIds.lastIndexOf(","));
                    List<String> lista = Arrays.asList(itemsList);
                    String sbquery1 = "select sum(obj.advanceMoney) from VendorSpecific obj WHERE obj.organization.id=?1 and obj.vendorSpecific.id=?2 and obj.presentStatus=1 and obj.specificsVendors.id in (:itemLongList)";
                    query = entityManager.createQuery(sbquery1);
                    query.setParameter(1, user.getOrganization().getId());
                    query.setParameter(2, txnCustomerId);
                    query.setParameter("itemLongList", itemList);
                    double vendorSpecAdv = 0.0;
                    List<Double[]> advLists = query.getResultList();
                    if (advLists != null) {
                        vendorSpecAdv += advLists.get(0) != null
                                ? IdosUtil.convertStringToDouble(String.valueOf(advLists.get(0)))
                                : 0.0;
                        totalCreditForCustomer = totalCreditForCustomer - vendorSpecAdv;
                    }
                    if (totalCreditForCustomer < availableCreditForCust) {
                        row.put("creditLimitExceeded", "false");
                    } else {
                        row.put("creditLimitExceeded", "true");
                    }
                } else {
                    row.put("creditLimitExceeded", "false");
                }
                custCreditLimitDetails.add(row);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            ex.printStackTrace();
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result getWalkinCustomerLocations(String name, Request request) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> Start " + name);
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        ArrayNode cutomerList = result.putArray("cutomerList");
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            JsonNode json = request.body().asJson();
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            int idx = 1;
            if (name != null) {
                name = name.toUpperCase();
            }
            List<Vendor> customerList = Vendor.findListByOrgIdAndTypeName(entityManager, user.getOrganization().getId(),
                    IdosConstants.WALK_IN_CUSTOMER, name);
            for (Vendor customer : customerList) {
                List<CustomerDetail> customerGstinList = customer.getCustomerDetails();
                if (customerGstinList != null && customerGstinList.size() > 0) {
                    if (customer.getPresentStatus() == 0) {
                        continue;
                    }
                    // row.put("customerDetailId", customer.getId());
                    String shipState = customerGstinList.get(0).getShippingState();
                    String gstin = customer.getGstin();
                    String custLocation = customerGstinList.get(0).getShippinglocation();

                    if (gstin != null && custLocation != null) {
                        custLocation = custLocation + "-" + gstin;
                    } else if (gstin != null && custLocation == null) {
                        custLocation = gstin;
                    } else {
                        gstin = "";
                    }
                    if (shipState != null && !"".equals(shipState) && custLocation != null) {
                        custLocation = custLocation + "-" + shipState;
                    } else if (shipState != null && !"".equals(shipState) && custLocation == null) {
                        custLocation = shipState;
                    }
                    ObjectNode row = Json.newObject();
                    row.put("label", customer.getName());
                    row.put("value", custLocation == null ? "" : custLocation);
                    row.put("idx", gstin);
                    cutomerList.add(row);
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result customerSalesMonthWiseItemsData(Request request) {
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        ArrayNode an = result.putArray("custMonthwiseItemsData");
        ArrayNode anCash = result.putArray("custMonthwiseCashSalesItemsData");
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = getUserInfo(request);
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            String[] arr = DateUtil.getFinancialDate(user);
            String finStartDateStr = arr[0];
            Date findStartDate = IdosConstants.MYSQLDF.parse(finStartDateStr);
            int startMonth = IdosUtil.convertStringToInt(DateUtil.getMonthNumber(findStartDate));
            int startYear = IdosUtil.convertStringToInt(DateUtil.getYear(findStartDate));
            Date currDateTime = IdosConstants.MYSQLDF
                    .parse(IdosConstants.MYSQLDF.format(Calendar.getInstance().getTime()));
            int endMonth = IdosUtil.convertStringToInt(DateUtil.getMonthNumber(currDateTime));
            int endYear = IdosUtil.convertStringToInt(DateUtil.getYear(currDateTime));

            JsonNode json = request.body().asJson();
            user = getUserInfo(request);
            // String branchID=json.findValue("branchID")!=null ?
            // json.findValue("branchID").asText() : null;
            String txnModelFor = json.findValue("txnModelFor") != null ? json.findValue("txnModelFor").asText() : null;
            String custVendId = json.findValue("custVendId") != null ? json.findValue("custVendId").asText() : null;
            if (custVendId != null && !custVendId.equals("")) {
                // Credit sales/credit purchase
                Map<Long, ArrayList<Double>> map = new HashMap<Long, ArrayList<Double>>();
                if (startYear == endYear) {
                    for (int iMonth = startMonth; iMonth < endMonth; iMonth++) {
                        getCreditSalesPurchaseDataForCust(iMonth, startYear, txnModelFor, map, entityManager,
                                custVendId);
                    }
                } else {
                    // last year till dec
                    for (int iMonth = startMonth; iMonth < 12; iMonth++) {
                        getCreditSalesPurchaseDataForCust(iMonth, startYear, txnModelFor, map, entityManager,
                                custVendId);
                    }
                    // current year from jan to currentMonth
                    for (int iMonth = 1; iMonth < endMonth; iMonth++) {
                        getCreditSalesPurchaseDataForCust(iMonth, endYear, txnModelFor, map, entityManager, custVendId);
                    }
                }
                for (Map.Entry<Long, ArrayList<Double>> entry : map.entrySet()) {
                    Long key = entry.getKey();
                    Specifics specifics = Specifics.findById(key);
                    ObjectNode row = Json.newObject();
                    row.put("specificsName", specifics.getName());
                    row.put("specificsId", specifics.getId());
                    ArrayList<Double> arrayList = entry.getValue();
                    row.put("Jan", arrayList.get(0));
                    row.put("Feb", arrayList.get(1));
                    row.put("Mar", arrayList.get(2));
                    row.put("Apr", arrayList.get(3));
                    row.put("May", arrayList.get(4));
                    row.put("Jun", arrayList.get(5));
                    row.put("Jul", arrayList.get(6));
                    row.put("Aug", arrayList.get(7));
                    row.put("Sep", arrayList.get(8));
                    row.put("Oct", arrayList.get(9));
                    row.put("Nov", arrayList.get(10));
                    row.put("Dec", arrayList.get(11));
                    an.add(row);
                }
                // Cash sales/cash purchase
                Map<Long, ArrayList<Double>> cashmap = new HashMap<Long, ArrayList<Double>>();
                if (startYear == endYear) {
                    for (int iMonth = startMonth; iMonth < endMonth; iMonth++) {
                        getCashSalesPurchaseDataForCust(iMonth, startYear, txnModelFor, cashmap, entityManager,
                                custVendId);
                    }
                } else {
                    // last year till dec
                    for (int iMonth = startMonth; iMonth < 12; iMonth++) {
                        getCashSalesPurchaseDataForCust(iMonth, startYear, txnModelFor, cashmap, entityManager,
                                custVendId);
                    }
                    // current year from jan to currentMonth
                    for (int iMonth = 1; iMonth < endMonth; iMonth++) {
                        getCashSalesPurchaseDataForCust(iMonth, endYear, txnModelFor, cashmap, entityManager,
                                custVendId);
                    }
                }
                for (Map.Entry<Long, ArrayList<Double>> cashentry : cashmap.entrySet()) {
                    Long key = cashentry.getKey();
                    Specifics specifics = Specifics.findById(key);
                    ObjectNode row = Json.newObject();
                    row.put("specificsName", specifics.getName());
                    row.put("specificsId", specifics.getId());
                    ArrayList<Double> arrayList = cashentry.getValue();
                    row.put("Jan", arrayList.get(0));
                    row.put("Feb", arrayList.get(1));
                    row.put("Mar", arrayList.get(2));
                    row.put("Apr", arrayList.get(3));
                    row.put("May", arrayList.get(4));
                    row.put("Jun", arrayList.get(5));
                    row.put("Jul", arrayList.get(6));
                    row.put("Aug", arrayList.get(7));
                    row.put("Sep", arrayList.get(8));
                    row.put("Oct", arrayList.get(9));
                    row.put("Nov", arrayList.get(10));
                    row.put("Dec", arrayList.get(11));
                    anCash.add(row);
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            ex.printStackTrace();
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result);
    }

    private static void getCreditSalesPurchaseDataForCust(int month, int year, String txnModelFor,
            Map<Long, ArrayList<Double>> map, EntityManager entityManager, String custVendId) throws Exception {
        if (custVendId == null || "".equals(custVendId)) {
            return;
        }
        Long custVendorId = Long.parseLong(custVendId);
        List<String> listOfdate = DateUtil.returnFirstDayAndLastDayOfGivenMonth(month, year);
        Date fromDate = IdosConstants.MYSQLDF.parse(listOfdate.get(0));
        Date toDate = IdosConstants.MYSQLDF.parse(listOfdate.get(1));
        Query query = entityManager.createQuery(CASH_CREDIT_SALES_PURCHASE_JQL);
        if (txnModelFor.equalsIgnoreCase("vendorPayables")) {
            query.setParameter(1, 4L);
        } else {
            query.setParameter(1, 2L);
        }
        query.setParameter(2, custVendorId);
        query.setParameter(3, fromDate);
        query.setParameter(4, toDate);

        List<Object[]> txnLists = query.getResultList();
        double netAmt = 0.0;
        Specifics spec = null;
        for (Object[] val : txnLists) {
            netAmt = 0.0;
            netAmt += val[0] != null ? Double.parseDouble(String.valueOf(val[0])) : 0.0;
            spec = (Specifics) val[1];
            Long key = spec.getId();
            if (map.containsKey(key)) {
                ArrayList<Double> values = map.get(key);
                values.set(month, netAmt);
            } else {
                ArrayList<Double> netAmtsList = new ArrayList<Double>(
                        Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
                netAmtsList.set(month, netAmt);
                map.put(key, netAmtsList);
            }
        }
    }

    private static void getCashSalesPurchaseDataForCust(int month, int year, String txnModelFor,
            Map<Long, ArrayList<Double>> map, EntityManager entityManager, String custVendId) throws Exception {
        if (custVendId == null || "".equals(custVendId)) {
            return;
        }
        Long custVendorId = Long.parseLong(custVendId);
        List<String> listOfdate = DateUtil.returnFirstDayAndLastDayOfGivenMonth(month, year);
        Date fromDate = IdosConstants.MYSQLDF.parse(listOfdate.get(0));
        Date toDate = IdosConstants.MYSQLDF.parse(listOfdate.get(1));

        Query query = entityManager.createQuery(CASH_CREDIT_SALES_PURCHASE_JQL);
        if (txnModelFor.equalsIgnoreCase("vendorPayables")) {
            query.setParameter(1, 3L);
        } else {
            query.setParameter(1, 1L);
        }
        query.setParameter(2, custVendorId);
        query.setParameter(3, fromDate);
        query.setParameter(4, toDate);

        List<Object[]> txnLists = query.getResultList();
        double netAmt = 0.0;
        Specifics spec = null;
        for (Object[] val : txnLists) {
            netAmt = 0.0;
            netAmt += val[0] != null ? Double.parseDouble(String.valueOf(val[0])) : 0.0;
            spec = (Specifics) val[1];
            Long key = spec.getId();
            if (map.containsKey(key)) {
                ArrayList<Double> values = map.get(key);
                values.set(month, netAmt);
            } else {
                ArrayList<Double> netAmtsList = new ArrayList<Double>(
                        Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
                netAmtsList.set(month, netAmt);
                map.put(key, netAmtsList);
            }
        }
    }

    @Transactional
    public Result getCustomerListForBranchAndTypeOfSuply(Request request) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        ArrayNode custListForBranchAn = result.putArray("custListForBranch");
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            JsonNode json = request.body().asJson();
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            String useremail = json.findValue("useremail").asText();
            Long txnPurposeId = json.findValue("txnPurposeId").asLong();
            Long txnPurposeBnchId = json.findValue("txnPurposeBnchId").asLong();
            Integer typeOfSupply = json.findValue("typeOfSupply") == null ? null
                    : json.findValue("typeOfSupply").asInt();
            StringBuilder sbquery = new StringBuilder(
                    "select obj from Vendor obj WHERE obj.organization.id=?1 and obj.presentStatus=1 ");
            if (typeOfSupply != null && typeOfSupply == 2) {
                sbquery.append("and obj.isRegistered = 0 ");
            } else if (typeOfSupply != null && typeOfSupply == 3) {
                sbquery.append("and obj.isRegistered = 1 ");
            }
            if (txnPurposeId == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                    || txnPurposeId == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) { // purchaseType 0=credit, 1=cash,
                                                                                  // 2=both
                sbquery.append(" and obj.type=1 and (obj.purchaseType=1 or obj.purchaseType=2)");
            } else if (txnPurposeId == IdosConstants.BUY_ON_CREDIT_PAY_LATER
                    || txnPurposeId == IdosConstants.CREDIT_NOTE_VENDOR
                    || txnPurposeId == IdosConstants.DEBIT_NOTE_VENDOR) { // purchaseType 0=credit, 1=cash, 2=both
                sbquery.append(" and obj.type=1 and (obj.purchaseType=0 or obj.purchaseType=2)");
            }
            ArrayList inparam = new ArrayList(1);
            inparam.add(user.getOrganization().getId());
            List<Vendor> customersForTheOrg = genericDAO.queryWithParams(sbquery.toString(), entityManager, inparam);
            Map<String, Object> criterias = new HashMap<String, Object>();
            if (customersForTheOrg != null && customersForTheOrg.size() > 0) {
                for (Vendor custForOrg : customersForTheOrg) {
                    criterias.put("branch.id", txnPurposeBnchId);
                    criterias.put("organization.id", user.getOrganization().getId());
                    criterias.put("vendor.id", custForOrg.getId());
                    criterias.put("presentStatus", 1);
                    BranchVendors customerAvailableForTxnBranch = genericDAO.getByCriteria(BranchVendors.class,
                            criterias, entityManager);
                    if (customerAvailableForTxnBranch != null) {
                        ObjectNode row = Json.newObject();
                        row.put("customerName", customerAvailableForTxnBranch.getVendor().getName());
                        row.put("customerId", customerAvailableForTxnBranch.getVendor().getId());
                        custListForBranchAn.add(row);
                    }
                    criterias.clear();
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> End ");
        return Results.ok(result);
    }

}
