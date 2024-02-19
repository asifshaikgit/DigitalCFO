package controllers.Gstr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import play.mvc.Results;

import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;

import controllers.StaticController;
import model.Branch;
import model.BranchTaxes;
import model.TransactionItems;
import model.TrialBalanceCOAItems;
import model.Users;
import model.Vendor;
import com.typesafe.config.Config;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.html.errorPage;
import play.Application;
import javax.inject.Inject;
import play.mvc.Http;
import play.mvc.Http.Request;

public class Gstr3bJsonController extends StaticController {

    // private static final String OUTWARD_TAXABLE_SUPPLY="select
    // obj.grossAmount,obj.taxRate1,obj.taxRate2,obj.taxRate3 ,obj.taxRate4 from
    // TransactionItems obj where obj.transactionBranchOrganization.id=?1 and
    // obj.transactionBranch.gstin=?2 and (obj.transaction.transactionPurpose.id=1
    // or obj.transaction.transactionPurpose.id=2 or
    // obj.transaction.transactionPurpose.id=30 or
    // obj.transaction.transactionPurpose.id=31 or
    // obj.transaction.transactionPurpose.id=6
    // obj.transaction.transactionPurpose.id=35 or
    // obj.transaction.transactionPurpose.id=25) and
    // obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4
    // and (obj.transaction.typeOfSupply=1 or obj.transaction.typeOfSupply=2 or
    // obj.transaction.typeOfSupply=6)";
    private static final String OUTWARD_TAXABLE_SUPPLY = "select obj from TransactionItems obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2  and  (obj.transaction.transactionPurpose.id=1 or obj.transaction.transactionPurpose.id=2 or obj.transaction.transactionPurpose.id=30 or obj.transaction.transactionPurpose.id=31 or obj.transaction.transactionPurpose.id=6 or obj.transaction.transactionPurpose.id=35 or obj.transaction.transactionPurpose.id=25) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and (obj.transaction.typeOfSupply=1 or obj.transaction.typeOfSupply=2 or obj.transaction.typeOfSupply=6 or obj.transaction.typeOfSupply=0) and obj.presentStatus=1 and obj.transactionSpecifics.gstItemCategory is null";
    private static final String OUTWARD_TAXABLE_SUPPLY_ZERO_RATED = "select obj from TransactionItems obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2  and  (obj.transaction.transactionPurpose.id=1 or obj.transaction.transactionPurpose.id=2 or obj.transaction.transactionPurpose.id=30 or obj.transaction.transactionPurpose.id=31 or obj.transaction.transactionPurpose.id=6 or obj.transaction.transactionPurpose.id=35) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and (obj.transaction.typeOfSupply=3 or obj.transaction.typeOfSupply=4 or obj.transaction.typeOfSupply=5)";
    private static final String OTHER_OUTWARD_TAXABLE_SUPPLY_QUERY = "select obj from TransactionItems obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2  and  (obj.transaction.transactionPurpose.id=1 or obj.transaction.transactionPurpose.id=2 or obj.transaction.transactionPurpose.id=30 or obj.transaction.transactionPurpose.id=31 or obj.transaction.transactionPurpose.id=6 or obj.transaction.transactionPurpose.id=35 or obj.transaction.transactionPurpose.id=25) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and (obj.transaction.typeOfSupply=1 or obj.transaction.typeOfSupply=2 or obj.transaction.typeOfSupply=6 or obj.transaction.typeOfSupply=7) and (obj.transactionSpecifics.gstItemCategory=1 or obj.transactionSpecifics.gstItemCategory=2) and obj.presentStatus=1";
    private static final String INWARD_SUPPLIES_QUERY = "select obj from TransactionItems obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2  and  (obj.transaction.transactionPurpose.id IN(3,4,11,25,33,32)) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and (obj.transaction.typeOfSupply=2 or obj.transaction.typeOfSupply=3 or obj.transaction.typeOfSupply=4 or obj.transaction.typeOfSupply=5) and obj.presentStatus=1";
    private static final String NON_GST_OUTWARD_SUPPLIES_QUERY = "select obj from TransactionItems obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2  and  (obj.transaction.transactionPurpose.id=1 or obj.transaction.transactionPurpose.id=2 or obj.transaction.transactionPurpose.id=30 or obj.transaction.transactionPurpose.id=31 or obj.transaction.transactionPurpose.id=6 or obj.transaction.transactionPurpose.id=35 or obj.transaction.transactionPurpose.id=25) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and (obj.transaction.typeOfSupply=1 or obj.transaction.typeOfSupply=2 or obj.transaction.typeOfSupply=6 or obj.transaction.typeOfSupply=7) and obj.transactionSpecifics.gstItemCategory=3 and obj.presentStatus=1";

    private static final String ITC_AVAILABLE_IMPORT_OF_GOODS_QUERY = "select obj from TransactionItems obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2  and  (obj.transaction.transactionPurpose.id IN(3,4,25,32,33)) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and obj.transaction.typeOfSupply=4 and obj.transactionSpecifics.isEligibleInputTaxCredit=1 and obj.presentStatus=1";
    private static final String ITC_AVAILABLE_IMPORT_OF_SERVICES_QUERY = "select obj from TransactionItems obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2  and  (obj.transaction.transactionPurpose.id IN(3,4,25,32,33)) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and obj.transaction.typeOfSupply=5 and obj.transactionSpecifics.isEligibleInputTaxCredit=1 and obj.presentStatus=1";
    private static final String ITC_AVAILABLE_INWARD_SUPPLIES_QUERY = "select obj from TransactionItems obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2  and  (obj.transaction.transactionPurpose.id IN(3,4,11,25,32,33)) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and (obj.transaction.typeOfSupply=2 or obj.transaction.typeOfSupply=3) and obj.transactionSpecifics.isEligibleInputTaxCredit=1 and obj.presentStatus=1";
    private static final String ALL_OTHER_ITC_QUERY = "select obj from TransactionItems obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2  and  (obj.transaction.transactionPurpose.id IN(3,4,11,16,18,19,25,32,33)) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and obj.transaction.typeOfSupply=1 and obj.transactionSpecifics.isEligibleInputTaxCredit=1 and obj.presentStatus=1";
    private static final String ALL_OTHER_ITC_CLAIMS_QUERY = "select obj from ClaimsSettlement obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2  and  (obj.transaction.transactionPurpose.id=16 or obj.transaction.transactionPurpose.id=18 or obj.transaction.transactionPurpose.id=19) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and obj.presentStatus=1";
    private static final String INTELIGIBLE_ITC_QUERY = "select obj from TransactionItems obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2  and  (obj.transaction.transactionPurpose.id IN(3,4,11,16,18,19,25,32,33)) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4  and (obj.transactionSpecifics.isEligibleInputTaxCredit=0 or obj.transactionSpecifics.isEligibleInputTaxCredit is null) and obj.presentStatus=1";
    private static final String INTELIGIBLE_ITC_CLAIMS_QUERY = "select obj from ClaimsSettlement obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2  and  (obj.transaction.transactionPurpose.id=16 or obj.transaction.transactionPurpose.id=18 or obj.transaction.transactionPurpose.id=19) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and obj.presentStatus=1";
    private static final String REVERSAL_ITC_QUERY = "select obj from TransactionItems obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2 and obj.transaction.transactionPurpose.id= ?3 and obj.transaction.transactionDate>=?4 and obj.transaction.transactionDate<=?5 and obj.presentStatus=1";
    private static final String SUPPLIER_INTER_QUERY = "select obj from TransactionItems obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2 and SUBSTRING(obj.transaction.destinationGstin,1,2)!=SUBSTRING(obj.transaction.sourceGstin,1,2) and  (obj.transaction.transactionPurpose.id IN(3,4,11,16,18,19,25,32,33)) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and obj.transaction.typeOfSupply=1 and (obj.transactionSpecifics.gstItemCategory IN(1,2)) and obj.presentStatus=1";
    private static final String SUPPLIER_INTRA_QUERY = "select obj from TransactionItems obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2 and SUBSTRING(obj.transaction.destinationGstin,1,2)=SUBSTRING(obj.transaction.sourceGstin,1,2) and  (obj.transaction.transactionPurpose.id IN(3,4,11,16,18,19,25,32,33)) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and obj.transaction.typeOfSupply=1 and (obj.transactionSpecifics.gstItemCategory IN(1,2)) and obj.presentStatus=1";
    private static final String NON_GST_SUPPLY_INTER_QUERY = "select obj from TransactionItems obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2 and SUBSTRING(obj.transaction.destinationGstin,1,2)!=SUBSTRING(obj.transaction.sourceGstin,1,2) and  (obj.transaction.transactionPurpose.id IN(3,4,11,16,18,19,25,32,33)) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and obj.transaction.typeOfSupply=1 and obj.transactionSpecifics.gstItemCategory=3 and obj.presentStatus=1";
    private static final String NON_GST_SUPPLY_INTRA_QUERY = "select obj from TransactionItems obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2 and SUBSTRING(obj.transaction.destinationGstin,1,2)=SUBSTRING(obj.transaction.sourceGstin,1,2) and  (obj.transaction.transactionPurpose.id IN(3,4,11,16,18,19,25,32,33)) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and obj.transaction.typeOfSupply=1 and obj.transactionSpecifics.gstItemCategory=3 and obj.presentStatus=1";

    private static final String UNREGISTERED_SUPPLY_QUERY = "select obj from TransactionItems obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2 and (obj.transaction.transactionPurpose IN(1,2,6,30,31,35)) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and (obj.transaction.typeOfSupply=1 or obj.transaction.typeOfSupply=2 or obj.transaction.typeOfSupply=6) and (obj.transaction.walkinCustomerType=0  or obj.transaction.walkinCustomerType=3  or obj.transaction.walkinCustomerType=4 or obj.transaction.walkinCustomerType=5 or obj.transaction.walkinCustomerType=6) and SUBSTRING(obj.transaction.destinationGstin,1,2)=?5 and obj.presentStatus=1";
    private static final String COMPOSITION_DEALER_SUPPLY_QUERY = "select obj from TransactionItems obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2  and  (obj.transaction.transactionPurpose.id=1 or obj.transaction.transactionPurpose.id=2 or obj.transaction.transactionPurpose.id=30 or obj.transaction.transactionPurpose.id=31 or obj.transaction.transactionPurpose.id=6 or obj.transaction.transactionPurpose.id=35) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and (obj.transaction.typeOfSupply=1 or obj.transaction.typeOfSupply=2 or obj.transaction.typeOfSupply=6) and obj.transaction.transactionVendorCustomer.isBusiness=4 and SUBSTRING(obj.transaction.destinationGstin,1,2)=?5 and obj.presentStatus=1";
    private static final String UIN_HOLDERS_SUPPLY_QUERY = "select obj from TransactionItems obj where obj.transaction.transactionBranchOrganization.id=?1 and obj.transaction.transactionBranch.gstin=?2  and  (obj.transaction.transactionPurpose.id=1 or obj.transaction.transactionPurpose.id=2 or obj.transaction.transactionPurpose.id=30 or obj.transaction.transactionPurpose.id=31 or obj.transaction.transactionPurpose.id=6 or obj.transaction.transactionPurpose.id=35) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and (obj.transaction.typeOfSupply=1 or obj.transaction.typeOfSupply=2 or obj.transaction.typeOfSupply=6) and obj.transaction.transactionVendorCustomer.isBusiness=3 and SUBSTRING(obj.transaction.destinationGstin,1,2)=?5 and obj.presentStatus=1";

    private static final String ITC_AVAILABLE_ALL_OTHER_ITC_QUERY = "";
    private Application application;
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    private Request request;
    // private Http.Session session = request.session();

    @Inject
    public Gstr3bJsonController(Application application) {
        super(application);
        this.application = application;
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @Transactional
    public Result getBrnachGstinList(Http.Request request) {
        log.log(Level.FINE, ">>>> Start inside download buy transaction data");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        // EntityTransaction transaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            // transaction.begin();
            JsonNode json = request.body().asJson();
            ArrayNode branchListArray = result.putArray("branchlist");
            ObjectNode node = result.putObject("composition");
            String useremail = json.findValue("userEmail").asText();
            session.adding("email", useremail);
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }

            /*
             * Map<String, Object> criterias = new HashMap<String, Object>();
             * criterias.put("organization.id", user.getOrganization().getId());
             */
            StringBuilder customerHql = new StringBuilder("select a from Branch a where a.organization.id=")
                    .append(user.getOrganization().getId());

            customerHql.append(" group by a.gstin, a.id");

            List<Branch> branchList = genericDAO.executeSimpleQuery(customerHql.toString(), entityManager);
            for (Branch branch : branchList) {
                ObjectNode objNode = Json.newObject();
                objNode.put("bnchId", branch.getId());
                objNode.put("bnchName", branch.getName());
                objNode.put("bnchGST", branch.getGstin());

                branchListArray.add(objNode);
            }

            if (user.getOrganization().getIsCompositionScheme() == 1) {
                node.put("composition", 1);
            } else {
                node.put("composition", 0);
            }

        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result populateValues(Http.Request request) {
        log.log(Level.INFO, "get turnover value");
        Http.Session session = request.session();
        ObjectNode result = Json.newObject();

        ArrayNode dashboardan = result.putArray("dashBoardData");
        ObjectNode dashboardrow = Json.newObject();

        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {

            JsonNode json = request.body().asJson();
            String useremail = json.findValue("useremail").asText();
            String dateMonthAndYear = json.findValue("txtDate").asText();
            String gstIn = json.findValue("gstIn").asText();
            Double taxRate = json.findValue("taxRate").asDouble();
            log.log(Level.INFO, "taxRate=" + taxRate);
            Double cgst = 0.0;
            Double sgst = 0.0;
            session.adding("email", useremail);
            user = getUserInfo(request);

            log.log(Level.INFO, " dateMonthAndYear=" + dateMonthAndYear);
            String year = dateMonthAndYear.substring(dateMonthAndYear.length() - 4, dateMonthAndYear.length());
            String month = dateMonthAndYear.substring(0, dateMonthAndYear.length() - 5);
            // January 1,2018
            String dateinjava = month + " 1," + year;
            log.log(Level.INFO, "date in java=" + dateinjava);

            Date fromTransDate = IdosConstants.MYSQLDF
                    .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateinjava)));
            Calendar cal = Calendar.getInstance();
            cal.setTime(fromTransDate);
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) / 3 * 3);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            // Date fromTransDate1=cal.getTime();
            Date fromTransDate1 = IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(cal.getTime()));

            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(fromTransDate);
            cal1.set(Calendar.MONTH, cal.get(Calendar.MONTH) / 3 * 3 + 2);
            cal1.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            // Date toTransDate1=cal1.getTime();
            Date toTransDate1 = IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(cal1.getTime()));

            log.log(Level.INFO, "from TransDate=" + fromTransDate1);
            // Date fromTransDate1=IdosConstants.IDOSDF.parse(dateinjava);
            /*
             * Calendar cal = Calendar.getInstance();
             * Date toTransDate=cal.getTime();
             * cal.setTime(fromTransDate);
             * Integer monthInInt = cal.get(Calendar.MONTH)+1;
             * String gstr1period = monthInInt.toString()+year;
             * log.log(Level.INFO, "period="+gstr1period);
             * SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
             * 
             * Date date =
             * sdf.parse(year+"-"+(monthInInt<10?("0"+monthInInt):monthInInt)+"-01");
             * 
             * Calendar calendar = Calendar.getInstance();
             * calendar.setTime(date);
             * 
             * calendar.set(Calendar.MONTH, cal.get(Calendar.MONTH)/3 * 3 + 2);
             * calendar.set(Calendar.DAY_OF_MONTH,
             * cal.getActualMaximum(Calendar.DAY_OF_MONTH));
             * toTransDate
             * =IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(calendar.getTime())
             * );
             */
            log.log(Level.INFO, "toTransDate=" + toTransDate1);
            log.log(Level.INFO, "gstin=" + gstIn);
            Double intraGSTTournover = TrialBalanceCOAItems.findTournOverIntraStateForKarvy(
                    user.getOrganization().getId(), gstIn, fromTransDate1, toTransDate1);
            log.log(Level.INFO, "gst turnover=" + intraGSTTournover);
            sgst = (intraGSTTournover * taxRate / 100) / 2;
            cgst = sgst;
            dashboardrow.put("intraGstTurnover", IdosConstants.decimalFormat.format(intraGSTTournover));
            dashboardrow.put("cgst", IdosConstants.decimalFormat.format(cgst));
            dashboardrow.put("sgst", IdosConstants.decimalFormat.format(sgst));
            dashboardan.add(dashboardrow);
        } catch (Exception ex) {
            log.log(Level.INFO, "inside error");
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());

        }
        return Results.ok(result);
    }

    // Below method add by Sunil
    @Transactional
    public Result downloadGSTR3BJSONSFile(Request request) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> Start inside download GSTR3b data");
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        File file = null;
        String path = application.path().toString() + "/logs/KarvyJSONData/";
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            JsonNode json = request.body().asJson();
            String dateMonthAndYear = json.findValue("txtDate").asText();
            String gstin = json.findValue("gstin").asText();
            String table31 = json.findValue("table31").toString();
            Integer intervalType = json.findValue("intervalType").asInt();
            JSONArray table31JsonArr = new JSONArray(table31);
            String year = dateMonthAndYear.substring(dateMonthAndYear.length() - 4, dateMonthAndYear.length());
            String month = dateMonthAndYear.substring(0, dateMonthAndYear.length() - 5);
            String dateinjava = month + " 1," + year;
            Date fromTransDate;
            if (intervalType == 3) {
                fromTransDate = IdosConstants.MYSQLDF
                        .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateMonthAndYear)));
            } else {
                fromTransDate = IdosConstants.MYSQLDF
                        .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateinjava)));
            }
            Calendar cal = Calendar.getInstance();
            Date toTransDate = cal.getTime();
            cal.setTime(fromTransDate);
            Integer monthInInt = cal.get(Calendar.MONTH) + 1;
            String gstr1period = monthInInt.toString() + year;

            JSONObject mainJson = new JSONObject();
            if (gstr1period.length() < 6) {
                gstr1period = "0" + gstr1period;
            }
            mainJson.put("ret_period", gstr1period);
            mainJson.put("gstin", gstin);

            JSONObject sup_details = new JSONObject();
            for (int i = 0; i < table31JsonArr.length(); i++) {
                JSONObject rowData = new JSONObject(table31JsonArr.get(i).toString());
                JSONObject row = new JSONObject();
                row.put("txval", rowData.getString("totalTaxable"));
                row.put("iamt", rowData.getString("integratedTax"));
                row.put("camt", rowData.getString("centralTax"));
                row.put("samt", rowData.getString("stateTax"));
                row.put("csamt", rowData.getString("cessTax"));
                if (i == 0) {
                    sup_details.put("osup_det", row);
                } else if (i == 1) {
                    sup_details.put("osup_zero", row);
                } else if (i == 2) {
                    sup_details.put("osup_nil_exmp", row);
                } else if (i == 3) {
                    sup_details.put("isup_rev", row);
                } else if (i == 4) {
                    sup_details.put("osup_nongst", row);
                }
            }
            mainJson.put("sup_details", sup_details);

            String table4 = json.findValue("table4").toString();
            JSONArray table4JsonArr = new JSONArray(table4);
            JSONObject itc_elg = new JSONObject();
            JSONObject itc_net = new JSONObject();
            JSONArray itc_elgArray = new JSONArray();
            JSONArray itc_revArray = new JSONArray();
            JSONArray itc_inelgArray = new JSONArray();
            for (int i = 0; i < table4JsonArr.length(); i++) {
                JSONObject rowData = new JSONObject(table4JsonArr.get(i).toString());
                JSONObject row = new JSONObject();
                String trid = rowData.getString("trid");
                if (trid != null && !"".equals(trid)) {
                    row.put("ty", trid);
                }
                row.put("iamt", rowData.getString("integratedTax"));
                row.put("camt", rowData.getString("centralTax"));
                row.put("samt", rowData.getString("stateTax"));
                row.put("csamt", rowData.getString("cessTax"));

                if (i < 5) {
                    itc_elgArray.put(row);
                } else if (i > 4 && i < 7) {
                    itc_revArray.put(row);
                } else if (i == 7) {
                    itc_net = row;
                } else {
                    itc_inelgArray.put(row);
                }
            }
            itc_elg.put("itc_avl", itc_elgArray);
            itc_elg.put("itc_rev", itc_revArray);
            itc_elg.put("itc_net", itc_net);
            itc_elg.put("itc_inelg", itc_inelgArray);
            mainJson.put("itc_elg", itc_elg);

            String table5 = json.findValue("table5").toString();
            JSONArray table5JsonArr = new JSONArray(table5);
            JSONObject inward_sup = new JSONObject();
            JSONArray isup_detailsArray = new JSONArray();
            for (int i = 0; i < table5JsonArr.length(); i++) {
                JSONObject rowData = new JSONObject(table5JsonArr.get(i).toString());
                JSONObject row = new JSONObject();
                String trid = rowData.getString("trid");
                if (trid != null && !"".equals(trid)) {
                    row.put("ty", trid);
                }
                row.put("inter", rowData.getString("integratedTax"));
                row.put("intra", rowData.getString("cessTax"));
                isup_detailsArray.put(row);
            }
            inward_sup.put("isup_details", isup_detailsArray);
            mainJson.put("inward_sup", inward_sup);

            String table51 = json.findValue("table51").toString();
            JSONArray table51JsonArr = new JSONArray(table51);
            JSONObject intr_ltfee = new JSONObject();
            for (int i = 0; i < table51JsonArr.length(); i++) {
                JSONObject rowData = new JSONObject(table51JsonArr.get(i).toString());
                JSONObject row = new JSONObject();
                row.put("iamt", rowData.getString("integratedTax"));
                row.put("camt", rowData.getString("centralTax"));
                row.put("samt", rowData.getString("stateTax"));
                row.put("csamt", rowData.getString("cessTax"));
                if (i == 0) {
                    intr_ltfee.put("intr_details", row);
                } else if (i == 1) {
                    intr_ltfee.put("ltfee_details", row);
                }
            }
            mainJson.put("intr_ltfee", intr_ltfee);

            String table52 = json.findValue("table52").toString();
            JSONArray table52JsonArr = new JSONArray(table52);
            JSONObject inter_sup = new JSONObject();
            JSONArray unreg_detailsArray = new JSONArray();
            JSONArray comp_detailsArray = new JSONArray();
            JSONArray uin_detailsArray = new JSONArray();

            for (int i = 0; i < table52JsonArr.length(); i++) {
                JSONObject rowData = new JSONObject(table52JsonArr.get(i).toString());
                String stateCode = rowData.getString("stateCode");

                JSONObject rowUnReg = new JSONObject();
                rowUnReg.put("pos", stateCode);
                rowUnReg.put("txval", rowData.getString("field1"));
                rowUnReg.put("iamt", rowData.getString("field2"));
                unreg_detailsArray.put(rowUnReg);

                JSONObject rowComp = new JSONObject();
                rowComp.put("pos", stateCode);
                rowComp.put("txval", rowData.getString("field3"));
                rowComp.put("iamt", rowData.getString("field4"));
                comp_detailsArray.put(rowComp);

                JSONObject rowUin = new JSONObject();
                rowUin.put("pos", stateCode);
                rowUin.put("txval", rowData.getString("field5"));
                rowUin.put("iamt", rowData.getString("field6"));
                uin_detailsArray.put(rowUin);

            }
            inter_sup.put("unreg_details", unreg_detailsArray);
            inter_sup.put("comp_details", comp_detailsArray);
            inter_sup.put("uin_details", uin_detailsArray);
            mainJson.put("inter_sup", inter_sup);

            FileWriter fw = null;
            BufferedWriter writer = null;
            File fileDir = new File(path);
            if (!fileDir.exists()) {
                fileDir.mkdir();
            }
            String orgName = IdosUtil.getOrganizationName4Report(user);
            String fileName = orgName + "_" + gstr1period + "_" + gstin + "_GSTR_3B.json";
            path = path + fileName;
            file = new File(path);
            try {
                fw = new FileWriter(file);
                writer = new BufferedWriter(fw);
                writer.append(mainJson.toString());
                writer.newLine();
            } catch (IOException e) {
                log.log(Level.SEVERE, "Error", e);

            } finally {
                try {
                    if (writer != null)
                        writer.close();

                    if (fw != null)
                        fw.close();
                } catch (IOException ex) {
                    log.log(Level.SEVERE, "Error", ex);
                }
            }
            return Results.ok(result).withHeader("ContentType", "application/json").withHeader("Content-Disposition",
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
    public Result getKarvyGSTR3BSDataForTransactionsKarvy(Request request) {
        log.log(Level.FINE, ">>>> Start inside download GSTR3B Json data");
        // EntityManager entityManager = getEntityManager();
        Users user = null;
        user = getUserInfo(request);
        if (user == null) {
            return unauthorized();
        }
        ObjectNode result = Json.newObject();
        ArrayNode outTaxSuppliesAn = result.putArray("outwardTaxableSupplies");
        ArrayNode outTaxSuppliesZeroRatedAn = result.putArray("outwardTaxableSuppliesZeroRated");
        ArrayNode otherOutTaxSuppliesZeroRatedAn = result.putArray("otherOutwardTaxableSuppliesZeroRated");
        ArrayNode inwardSuppliesAn = result.putArray("inwardSupplies");
        ArrayNode nonGstOutwardSuppliesAn = result.putArray("nonGstOutwardSupplies");

        ArrayNode itcImportOfGoodsAn = result.putArray("itcImportOfGoodsList");
        ArrayNode itcImportOfServiceAn = result.putArray("itcImportOfServiceList");
        ArrayNode itcInwardSuppliesAn = result.putArray("itcInwardSuppliesList");
        ArrayNode itcinwardSuppliesAllAn = result.putArray("itcinwardAllSuppliesList");
        ArrayNode allOtherITCAn = result.putArray("allOtherITCList");
        ArrayNode reversalOfITCAn = result.putArray("reversalOfITCList");

        ArrayNode exemptGSTSupplyAn = result.putArray("exemptGSTSupplyData");
        ArrayNode exemptNONGSTSupplyAn = result.putArray("exemptNONGSTSupplyData");
        ArrayNode multipleStatesDataAn = result.putArray("multipleStatesData");

        // ArrayNode projectBranchData = result.putArray("projectData");
        try {

            String dateMonthAndYearForMonthWise = "";
            String dateMonthAndYearForQuarterWise = "";
            String dateMonthAndYearFromDate = "";
            String dateMonthAndYearToDate = "";
            List<TransactionItems> outwardTaxableSupplies = null;
            List<TransactionItems> outwardTaxableSuppliesZeroRated = null;
            List<TransactionItems> otherOutwardTaxableSuppliesZeroRated = null;
            List<TransactionItems> inwardSupplies = null;
            List<TransactionItems> nonGstOutwardSupplies = null;

            List<TransactionItems> itcImportOfGoodsList = null;
            List<TransactionItems> itcImportOfServiceList = null;
            List<TransactionItems> itcInwardSuppliesList = null;
            List<TransactionItems> itcinwardSuppliesISDList = null;
            List<TransactionItems> allOtherITCList = null;
            List<TransactionItems> reversalOfITCList = null;

            List<TransactionItems> exemptGSTSuppliesList = null;
            List<TransactionItems> exemptNONGSTSuplliesList = null;

            List<TransactionItems> exemptGSTINTRASuppliesList = null;
            List<TransactionItems> exemptNONGSTINTRASuplliesList = null;

            JsonNode json = request.body().asJson();
            String useremail = json.findValue("useremail").asText();
            // String type = json.findValue("type").asText();
            Integer intervalType = json.findValue("intervalType").asInt();
            // String selectedValues=json.findValues("selectedValues").toString();
            if (intervalType == 1) {
                dateMonthAndYearForMonthWise = json.findValue("txtDate1").asText();
            } else if (intervalType == 2) {
                dateMonthAndYearForQuarterWise = json.findValue("txtDate2").asText();
            } else if (intervalType == 3) {
                dateMonthAndYearFromDate = json.findValue("txtDate3").asText();
                dateMonthAndYearToDate = json.findValue("txtDate4").asText();
            }
            String gstIn = json.findValue("gstIn").asText();

            Date fromTransDate = null;
            Date toTransDate = null;
            String gstr1period = "";
            if (intervalType == 1) {

                String year = dateMonthAndYearForMonthWise.substring(dateMonthAndYearForMonthWise.length() - 4,
                        dateMonthAndYearForMonthWise.length());
                String month = dateMonthAndYearForMonthWise.substring(0, dateMonthAndYearForMonthWise.length() - 5);
                // January 1,2018
                String dateinjava = month + " 1," + year;

                fromTransDate = IdosConstants.MYSQLDF
                        .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateinjava)));
                Calendar cal = Calendar.getInstance();
                toTransDate = cal.getTime();
                cal.setTime(fromTransDate);
                Integer monthInInt = cal.get(Calendar.MONTH) + 1;
                gstr1period = monthInInt.toString() + year;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                Date date = sdf.parse(year + "-" + (monthInInt < 10 ? ("0" + monthInInt) : monthInInt) + "-01");

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                calendar.add(Calendar.MONTH, 1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.DATE, -1);

                toTransDate = IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(calendar.getTime()));
            }
            if (intervalType == 2) {
                String year = dateMonthAndYearForQuarterWise.substring(dateMonthAndYearForQuarterWise.length() - 4,
                        dateMonthAndYearForQuarterWise.length());
                String month = dateMonthAndYearForQuarterWise.substring(0, dateMonthAndYearForQuarterWise.length() - 5);
                // January 1,2018
                String dateinjava = month + " 1," + year;

                Date fromTransDate1 = IdosConstants.MYSQLDF
                        .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateinjava)));
                Calendar cal = Calendar.getInstance();
                cal.setTime(fromTransDate1);
                cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) / 3 * 3);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                fromTransDate = IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(cal.getTime()));
                Integer monthInInt = cal.get(Calendar.MONTH) + 1;
                gstr1period = monthInInt.toString() + year;
                Calendar cal1 = Calendar.getInstance();
                cal1.setTime(fromTransDate1);
                cal1.set(Calendar.MONTH, cal.get(Calendar.MONTH) / 3 * 3 + 2);
                cal1.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                toTransDate = IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(cal1.getTime()));
            }
            if (intervalType == 3) {
                fromTransDate = IdosConstants.MYSQLDF
                        .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateMonthAndYearFromDate)));
                Calendar cal = Calendar.getInstance();
                cal.setTime(fromTransDate);
                Integer monthInInt = cal.get(Calendar.MONTH) + 1;
                Integer year = cal.get(Calendar.YEAR);
                gstr1period = monthInInt.toString() + year;
                toTransDate = IdosConstants.MYSQLDF
                        .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateMonthAndYearToDate)));
            }
            ArrayList<Object> inparam1 = new ArrayList<Object>(4);

            inparam1.clear();
            inparam1.add(user.getOrganization().getId());
            inparam1.add(gstIn);
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);

            outwardTaxableSupplies = genericDAO.queryWithParamsName(OUTWARD_TAXABLE_SUPPLY.toString(), entityManager,
                    inparam1);
            inparam1.clear();
            inparam1.add(user.getOrganization().getId());
            inparam1.add(gstIn);
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);

            outwardTaxableSuppliesZeroRated = genericDAO
                    .queryWithParamsName(OUTWARD_TAXABLE_SUPPLY_ZERO_RATED.toString(), entityManager, inparam1);
            inparam1.clear();
            inparam1.add(user.getOrganization().getId());
            inparam1.add(gstIn);
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);

            otherOutwardTaxableSuppliesZeroRated = genericDAO
                    .queryWithParamsName(OTHER_OUTWARD_TAXABLE_SUPPLY_QUERY.toString(), entityManager, inparam1);

            inparam1.clear();
            inparam1.add(user.getOrganization().getId());
            inparam1.add(gstIn);
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);

            inwardSupplies = genericDAO.queryWithParamsName(INWARD_SUPPLIES_QUERY.toString(), entityManager, inparam1);

            inparam1.clear();
            inparam1.add(user.getOrganization().getId());
            inparam1.add(gstIn);
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);
            nonGstOutwardSupplies = genericDAO.queryWithParamsName(NON_GST_OUTWARD_SUPPLIES_QUERY.toString(),
                    entityManager, inparam1);

            Double grossAmount = 0.0;
            Double sgst = 0.0;
            Double cgst = 0.0;
            Double igst = 0.0;
            Double cess = 0.0;

            Double grossAmount1 = 0.0;
            Double sgst1 = 0.0;
            Double cgst1 = 0.0;
            Double igst1 = 0.0;
            Double cess1 = 0.0;

            Double grossAmount2 = 0.0;
            Double sgst2 = 0.0;
            Double cgst2 = 0.0;
            Double igst2 = 0.0;
            Double cess2 = 0.0;

            Double grossAmount3 = 0.0;
            Double sgst3 = 0.0;
            Double cgst3 = 0.0;
            Double igst3 = 0.0;
            Double cess3 = 0.0;

            Double grossAmount4 = 0.0;
            Double sgst4 = 0.0;
            Double cgst4 = 0.0;
            Double igst4 = 0.0;
            Double cess4 = 0.0;
            ObjectNode outTaxSupplyRow = Json.newObject();
            for (TransactionItems outwardTaxableSupply : outwardTaxableSupplies) {

                if (outwardTaxableSupply.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER)) {
                    grossAmount = grossAmount + outwardTaxableSupply.getGrossAmount()
                            + outwardTaxableSupply.getWithholdingAmount() - outwardTaxableSupply.getTaxValue1()
                            - outwardTaxableSupply.getTaxValue2() - outwardTaxableSupply.getTaxValue3()
                            - outwardTaxableSupply.getTaxValue4();
                    if (outwardTaxableSupply.getTaxValue1() != null) {
                        sgst = sgst + outwardTaxableSupply.getTaxValue1();
                    }
                    if (outwardTaxableSupply.getTaxValue2() != null) {
                        cgst = cgst + outwardTaxableSupply.getTaxValue2();
                    }
                    if (outwardTaxableSupply.getTaxValue3() != null) {
                        igst = igst + outwardTaxableSupply.getTaxValue3();
                    }
                    if (outwardTaxableSupply.getTaxValue4() != null) {
                        cess = cess + outwardTaxableSupply.getTaxValue4();
                    }
                } else if (outwardTaxableSupply.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.REFUND_ADVANCE_RECEIVED)) {
                    grossAmount = grossAmount - (outwardTaxableSupply.getGrossAmounReturned()
                            + outwardTaxableSupply.getWithholdingAmountReturned() - outwardTaxableSupply.getTaxValue1()
                            - outwardTaxableSupply.getTaxValue2() - outwardTaxableSupply.getTaxValue3()
                            - outwardTaxableSupply.getTaxValue4());
                    if (outwardTaxableSupply.getTaxValue1() != null) {
                        sgst = sgst - outwardTaxableSupply.getTaxValue1();
                    }
                    if (outwardTaxableSupply.getTaxValue2() != null) {
                        cgst = cgst - outwardTaxableSupply.getTaxValue2();
                    }
                    if (outwardTaxableSupply.getTaxValue3() != null) {
                        igst = igst - outwardTaxableSupply.getTaxValue3();
                    }
                    if (outwardTaxableSupply.getTaxValue4() != null) {
                        cess = cess - outwardTaxableSupply.getTaxValue4();
                    }
                } else if (outwardTaxableSupply.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.CREDIT_NOTE_CUSTOMER)) {
                    grossAmount = grossAmount - outwardTaxableSupply.getGrossAmount();
                    if (outwardTaxableSupply.getTaxValue1() != null) {
                        sgst = sgst - outwardTaxableSupply.getTaxValue1();
                    }
                    if (outwardTaxableSupply.getTaxValue2() != null) {
                        cgst = cgst - outwardTaxableSupply.getTaxValue2();
                    }
                    if (outwardTaxableSupply.getTaxValue3() != null) {
                        igst = igst - outwardTaxableSupply.getTaxValue3();
                    }
                    if (outwardTaxableSupply.getTaxValue4() != null) {
                        cess = cess - outwardTaxableSupply.getTaxValue4();
                    }
                } else if (outwardTaxableSupply.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER)
                        && outwardTaxableSupply.getTransactionId().getTypeIdentifier() == 1) {
                    if (outwardTaxableSupply.getTaxValue1() != null || outwardTaxableSupply.getTaxValue2() != null
                            || outwardTaxableSupply.getTaxValue3() != null
                            || outwardTaxableSupply.getTaxValue4() != null) {
                        grossAmount = grossAmount + outwardTaxableSupply.getGrossAmount();
                        if (outwardTaxableSupply.getTaxValue1() != null) {
                            sgst = sgst + outwardTaxableSupply.getTaxValue1();
                        }
                        if (outwardTaxableSupply.getTaxValue2() != null) {
                            cgst = cgst + outwardTaxableSupply.getTaxValue2();
                        }
                        if (outwardTaxableSupply.getTaxValue3() != null) {
                            igst = igst + outwardTaxableSupply.getTaxValue3();
                        }
                        if (outwardTaxableSupply.getTaxValue4() != null) {
                            cess = cess + outwardTaxableSupply.getTaxValue4();
                        }
                    }
                } else {
                    grossAmount = grossAmount + outwardTaxableSupply.getGrossAmount();
                    if (outwardTaxableSupply.getTaxValue1() != null) {
                        sgst = sgst + outwardTaxableSupply.getTaxValue1();
                    }
                    if (outwardTaxableSupply.getTaxValue2() != null) {
                        cgst = cgst + outwardTaxableSupply.getTaxValue2();
                    }
                    if (outwardTaxableSupply.getTaxValue3() != null) {
                        igst = igst + outwardTaxableSupply.getTaxValue3();
                    }
                    if (outwardTaxableSupply.getTaxValue4() != null) {
                        cess = cess + outwardTaxableSupply.getTaxValue4();
                    }
                }
            }
            outTaxSupplyRow.put("grossAmount", grossAmount);
            outTaxSupplyRow.put("sgst", sgst);
            outTaxSupplyRow.put("cgst", cgst);
            outTaxSupplyRow.put("igst", igst);
            outTaxSupplyRow.put("cess", cess);
            outTaxSuppliesAn.add(outTaxSupplyRow);
            ObjectNode outTaxSupplyZeroRatedRow = Json.newObject();
            for (TransactionItems outwardTaxableSupplyZeroRated : outwardTaxableSuppliesZeroRated) {
                if (outwardTaxableSupplyZeroRated.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER)) {
                    grossAmount1 = grossAmount1 + outwardTaxableSupplyZeroRated.getGrossAmount()
                            + outwardTaxableSupplyZeroRated.getWithholdingAmount()
                            - outwardTaxableSupplyZeroRated.getTaxValue7()
                            - outwardTaxableSupplyZeroRated.getTaxValue3()
                            - outwardTaxableSupplyZeroRated.getTaxValue4();
                    if (outwardTaxableSupplyZeroRated.getTaxValue3() != null) {
                        igst1 = igst1 + outwardTaxableSupplyZeroRated.getTaxValue3();
                    }
                    if (outwardTaxableSupplyZeroRated.getTaxValue4() != null) {
                        cess1 = cess1 + outwardTaxableSupplyZeroRated.getTaxValue4();
                    }
                } else if (outwardTaxableSupplyZeroRated.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.REFUND_ADVANCE_RECEIVED)) {
                    grossAmount1 = grossAmount1 - (outwardTaxableSupplyZeroRated.getGrossAmount()
                            + outwardTaxableSupplyZeroRated.getWithholdingAmountReturned()
                            - outwardTaxableSupplyZeroRated.getTaxValue7()
                            - outwardTaxableSupplyZeroRated.getTaxValue3()
                            - outwardTaxableSupplyZeroRated.getTaxValue4());
                    if (outwardTaxableSupplyZeroRated.getTaxValue3() != null) {
                        igst1 = igst1 - outwardTaxableSupplyZeroRated.getTaxValue3();
                    }
                    if (outwardTaxableSupplyZeroRated.getTaxValue4() != null) {
                        cess1 = cess1 - outwardTaxableSupplyZeroRated.getTaxValue4();
                    }
                } else if (outwardTaxableSupplyZeroRated.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.CREDIT_NOTE_CUSTOMER)) {
                    grossAmount1 = grossAmount1 - (outwardTaxableSupplyZeroRated.getGrossAmount()
                            - outwardTaxableSupplyZeroRated.getTaxValue3()
                            - outwardTaxableSupplyZeroRated.getTaxValue4());
                    if (outwardTaxableSupplyZeroRated.getTaxValue3() != null) {
                        igst1 = igst1 - outwardTaxableSupplyZeroRated.getTaxValue3();
                    }
                    if (outwardTaxableSupplyZeroRated.getTaxValue4() != null) {
                        cess1 = cess1 - outwardTaxableSupplyZeroRated.getTaxValue4();
                    }
                } else {
                    grossAmount1 = grossAmount1 + outwardTaxableSupplyZeroRated.getGrossAmount();
                    if (outwardTaxableSupplyZeroRated.getTaxValue3() != null) {
                        igst1 = igst1 + outwardTaxableSupplyZeroRated.getTaxValue3();
                    }
                    if (outwardTaxableSupplyZeroRated.getTaxValue4() != null) {
                        cess1 = cess1 + outwardTaxableSupplyZeroRated.getTaxValue4();
                    }
                }
            }
            outTaxSupplyZeroRatedRow.put("grossAmount", grossAmount1);
            outTaxSupplyZeroRatedRow.put("sgst", sgst1);
            outTaxSupplyZeroRatedRow.put("cgst", cgst1);
            outTaxSupplyZeroRatedRow.put("igst", igst1);
            outTaxSupplyZeroRatedRow.put("cess", cess1);
            outTaxSuppliesZeroRatedAn.add(outTaxSupplyZeroRatedRow);
            ObjectNode otherOutTaxSupplyZeroRatedRow = Json.newObject();
            for (TransactionItems otherOutwardTaxableSupplyZeroRated : otherOutwardTaxableSuppliesZeroRated) {
                if (otherOutwardTaxableSupplyZeroRated.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER)) {
                    grossAmount2 = grossAmount2 + otherOutwardTaxableSupplyZeroRated.getAvailableAdvance()
                            + otherOutwardTaxableSupplyZeroRated.getWithholdingAmount();
                } else if (otherOutwardTaxableSupplyZeroRated.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.REFUND_ADVANCE_RECEIVED)) {
                    grossAmount2 = grossAmount2 - (otherOutwardTaxableSupplyZeroRated.getGrossAmounReturned()
                            + otherOutwardTaxableSupplyZeroRated.getWithholdingAmountReturned());
                } else if (otherOutwardTaxableSupplyZeroRated.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.CREDIT_NOTE_CUSTOMER)) {
                    grossAmount2 = grossAmount2 - otherOutwardTaxableSupplyZeroRated.getGrossAmount();
                } else {
                    grossAmount2 = grossAmount2 + otherOutwardTaxableSupplyZeroRated.getGrossAmount();
                }
            }
            otherOutTaxSupplyZeroRatedRow.put("grossAmount", grossAmount2);
            otherOutTaxSuppliesZeroRatedAn.add(otherOutTaxSupplyZeroRatedRow);

            ObjectNode inwardSupplyRow = Json.newObject();
            for (TransactionItems inwardSupply : inwardSupplies) {
                if (inwardSupply.getTransactionId().getTransactionPurpose().equals(IdosConstants.DEBIT_NOTE_VENDOR)) {

                    if (inwardSupply.getTaxValue7() != null)
                        grossAmount3 = grossAmount3 - (inwardSupply.getGrossAmount() + inwardSupply.getTaxValue7());
                    else
                        grossAmount3 = grossAmount3 - (inwardSupply.getGrossAmount());
                    if (inwardSupply.getTaxValue3() != null) {
                        igst3 = igst3 - inwardSupply.getTaxValue3();
                    }
                    if (inwardSupply.getTaxValue1() != null) {
                        sgst3 = sgst3 - inwardSupply.getTaxValue1();
                    }
                    if (inwardSupply.getTaxValue2() != null) {
                        cgst3 = cgst3 - inwardSupply.getTaxValue2();
                    }
                    if (inwardSupply.getTaxValue4() != null) {
                        cess3 = cess3 - inwardSupply.getTaxValue4();
                    }
                } else {
                    if (!(inwardSupply.getTransactionId().getTransactionPurpose()
                            .equals(IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER)
                            && inwardSupply.getTransactionId().getTypeIdentifier() == 1)) {
                        if (inwardSupply.getTaxValue7() != null)
                            grossAmount3 = grossAmount3 + inwardSupply.getGrossAmount() + inwardSupply.getTaxValue7();
                        else
                            grossAmount3 = grossAmount3 + inwardSupply.getGrossAmount();
                        if (inwardSupply.getTaxValue3() != null) {
                            igst3 = igst3 + inwardSupply.getTaxValue3();
                        }
                        if (inwardSupply.getTaxValue1() != null) {
                            sgst3 = sgst3 + inwardSupply.getTaxValue1();
                        }
                        if (inwardSupply.getTaxValue2() != null) {
                            cgst3 = cgst3 + inwardSupply.getTaxValue2();
                        }
                        if (inwardSupply.getTaxValue4() != null) {
                            cess3 = cess3 + inwardSupply.getTaxValue4();
                        }
                    }
                }
            }
            inwardSupplyRow.put("grossAmount", grossAmount3);
            inwardSupplyRow.put("sgst", sgst3);
            inwardSupplyRow.put("cgst", cgst3);
            inwardSupplyRow.put("igst", igst3);
            inwardSupplyRow.put("cess", cess3);
            inwardSuppliesAn.add(inwardSupplyRow);
            ObjectNode nonGstOutwardSupplyRow = Json.newObject();
            for (TransactionItems nonGstOutwardSupply : nonGstOutwardSupplies) {
                if (nonGstOutwardSupply.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER)) {
                    grossAmount4 = grossAmount4 + nonGstOutwardSupply.getAvailableAdvance()
                            + nonGstOutwardSupply.getWithholdingAmount();
                } else if (nonGstOutwardSupply.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.REFUND_ADVANCE_RECEIVED)) {
                    grossAmount4 = grossAmount4 - (nonGstOutwardSupply.getGrossAmounReturned()
                            + nonGstOutwardSupply.getWithholdingAmount());
                } else if (nonGstOutwardSupply.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.CREDIT_NOTE_CUSTOMER)) {
                    grossAmount4 = grossAmount4 - nonGstOutwardSupply.getGrossAmount();
                } else {
                    grossAmount4 = grossAmount4 + nonGstOutwardSupply.getGrossAmount();
                }
            }
            nonGstOutwardSupplyRow.put("grossAmount", grossAmount4);
            // nonGstOutwardSuppliesAn.add(nonGstOutwardSupplyRow);

            Double totalGross1 = 0.0;
            Double totalcgst1 = 0.0;
            Double totalsgst1 = 0.0;
            Double totaligst1 = 0.0;
            Double totalCess1 = 0.0;
            totalGross1 = grossAmount1 + grossAmount2 + grossAmount3 + grossAmount4 + grossAmount;
            totalcgst1 = cgst + cgst1 + cgst2 + cgst3 + cgst4;
            totaligst1 = igst + igst1 + igst2 + igst3 + igst4;
            totalsgst1 = sgst + sgst1 + sgst2 + sgst3 + sgst4;
            totalCess1 = cess + cess1 + cess2 + cess3 + cess4;
            ObjectNode totalSupplyRow = Json.newObject();
            nonGstOutwardSupplyRow.put("totalGross1", totalGross1);
            nonGstOutwardSupplyRow.put("totalcgst1", totalcgst1);
            nonGstOutwardSupplyRow.put("totaligst1", totaligst1);
            nonGstOutwardSupplyRow.put("totalsgst1", totalsgst1);
            nonGstOutwardSupplyRow.put("totalCess1", totalCess1);
            nonGstOutwardSuppliesAn.add(nonGstOutwardSupplyRow);

            Double sgst5 = 0.0;
            Double cgst5 = 0.0;
            Double igst5 = 0.0;
            Double cess5 = 0.0;

            Double sgst6 = 0.0;
            Double cgst6 = 0.0;
            Double igst6 = 0.0;
            Double cess6 = 0.0;

            Double sgst7 = 0.0;
            Double cgst7 = 0.0;
            Double igst7 = 0.0;
            Double cess7 = 0.0;

            Double sgst8 = 0.0;
            Double cgst8 = 0.0;
            Double igst8 = 0.0;
            Double cess8 = 0.0;

            Double sgst9 = 0.0;
            Double cgst9 = 0.0;
            Double igst9 = 0.0;
            Double cess9 = 0.0;

            inparam1.clear();
            inparam1.add(user.getOrganization().getId());
            inparam1.add(gstIn);
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);

            itcImportOfGoodsList = genericDAO.queryWithParamsName(ITC_AVAILABLE_IMPORT_OF_GOODS_QUERY.toString(),
                    entityManager, inparam1);
            inparam1.clear();
            inparam1.add(user.getOrganization().getId());
            inparam1.add(gstIn);
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);

            itcImportOfServiceList = genericDAO.queryWithParamsName(ITC_AVAILABLE_IMPORT_OF_SERVICES_QUERY.toString(),
                    entityManager, inparam1);
            inparam1.clear();
            inparam1.add(user.getOrganization().getId());
            inparam1.add(gstIn);
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);

            itcInwardSuppliesList = genericDAO.queryWithParamsName(ITC_AVAILABLE_INWARD_SUPPLIES_QUERY.toString(),
                    entityManager, inparam1);

            inparam1.clear();
            inparam1.add(user.getOrganization().getId());
            inparam1.add(gstIn);
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);

            itcinwardSuppliesISDList = genericDAO.queryWithParamsName(ALL_OTHER_ITC_QUERY.toString(), entityManager,
                    inparam1);

            inparam1.clear();
            inparam1.add(user.getOrganization().getId());
            inparam1.add(gstIn);
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);
            allOtherITCList = genericDAO.queryWithParamsName(INTELIGIBLE_ITC_QUERY.toString(), entityManager, inparam1);

            inparam1.clear();
            inparam1.add(user.getOrganization().getId());
            inparam1.add(gstIn);
            inparam1.add(IdosConstants.REVERSAL_OF_ITC);
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);
            reversalOfITCList = genericDAO.queryWithParamsName(REVERSAL_ITC_QUERY.toString(), entityManager, inparam1);

            ObjectNode itcImportGoodsRow = Json.newObject();
            for (TransactionItems itcImportGoods : itcImportOfGoodsList) {
                if (itcImportGoods.getTransactionId().getId().equals(IdosConstants.DEBIT_NOTE_VENDOR)) {
                    if (itcImportGoods.getTaxValue3() != null) {
                        igst5 = igst5 - itcImportGoods.getTaxValue3();
                    }

                    if (itcImportGoods.getTaxValue4() != null) {
                        cess5 = cess5 - itcImportGoods.getTaxValue4();
                    }
                } else if (itcImportGoods.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER)
                        && itcImportGoods.getTransactionId().getTypeIdentifier() == 1) {
                    if (itcImportGoods.getTaxValue1() != null || itcImportGoods.getTaxValue2() != null
                            || itcImportGoods.getTaxValue3() != null || itcImportGoods.getTaxValue4() != null) {
                        grossAmount = grossAmount + itcImportGoods.getGrossAmount();

                        if (itcImportGoods.getTaxValue3() != null) {
                            igst = igst + itcImportGoods.getTaxValue3();
                        }
                        if (itcImportGoods.getTaxValue4() != null) {
                            cess = cess + itcImportGoods.getTaxValue4();
                        }
                    }
                } else {

                    if (itcImportGoods.getTaxValue3() != null) {
                        igst5 = igst5 + itcImportGoods.getTaxValue3();
                    }

                    if (itcImportGoods.getTaxValue4() != null) {
                        cess5 = cess5 + itcImportGoods.getTaxValue4();
                    }
                }
            }

            itcImportGoodsRow.put("igst", igst5);
            itcImportGoodsRow.put("cess", cess5);
            itcImportOfGoodsAn.add(itcImportGoodsRow);

            ObjectNode itcImportServicesRow = Json.newObject();
            for (TransactionItems itcImportService : itcImportOfServiceList) {
                if (itcImportService.getTransactionId().getId().equals(IdosConstants.DEBIT_NOTE_VENDOR)) {
                    if (itcImportService.getTaxValue3() != null) {
                        igst6 = igst6 - itcImportService.getTaxValue3();
                    }

                    if (itcImportService.getTaxValue4() != null) {
                        cess6 = cess6 - itcImportService.getTaxValue4();
                    }
                } else if (itcImportService.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER)
                        && itcImportService.getTransactionId().getTypeIdentifier() == 1) {
                    if (itcImportService.getTaxValue1() != null || itcImportService.getTaxValue2() != null
                            || itcImportService.getTaxValue3() != null || itcImportService.getTaxValue4() != null) {
                        grossAmount = grossAmount + itcImportService.getGrossAmount();
                        if (itcImportService.getTaxValue1() != null) {
                            sgst = sgst + itcImportService.getTaxValue1();
                        }
                        if (itcImportService.getTaxValue2() != null) {
                            cgst = cgst + itcImportService.getTaxValue2();
                        }
                        if (itcImportService.getTaxValue3() != null) {
                            igst = igst + itcImportService.getTaxValue3();
                        }
                        if (itcImportService.getTaxValue4() != null) {
                            cess = cess + itcImportService.getTaxValue4();
                        }
                    }
                } else {
                    if (itcImportService.getTaxValue3() != null) {
                        igst6 = igst6 + itcImportService.getTaxValue3();
                    }

                    if (itcImportService.getTaxValue4() != null) {
                        cess6 = cess6 + itcImportService.getTaxValue4();
                    }

                }
            }

            itcImportServicesRow.put("igst", igst6);
            itcImportServicesRow.put("cess", cess6);
            itcImportOfServiceAn.add(itcImportServicesRow);

            ObjectNode itcinwardSupplyRow = Json.newObject();
            for (TransactionItems itcInwardSupply : itcInwardSuppliesList) {

                if (itcInwardSupply.getTransactionId().getId().equals(IdosConstants.DEBIT_NOTE_VENDOR)) {
                    if (itcInwardSupply.getTaxValue3() != null) {
                        igst7 = igst7 - itcInwardSupply.getTaxValue3();
                    }

                    if (itcInwardSupply.getTaxValue4() != null) {
                        cess7 = cess7 - itcInwardSupply.getTaxValue4();
                    }

                    if (itcInwardSupply.getTaxValue1() != null) {
                        sgst7 = sgst7 - itcInwardSupply.getTaxValue1();
                    }

                    if (itcInwardSupply.getTaxValue2() != null) {
                        cgst7 = cgst7 - itcInwardSupply.getTaxValue2();
                    }
                } else if (itcInwardSupply.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER)
                        && itcInwardSupply.getTransactionId().getTypeIdentifier() == 1) {
                    if (itcInwardSupply.getTaxValue1() != null || itcInwardSupply.getTaxValue2() != null
                            || itcInwardSupply.getTaxValue3() != null || itcInwardSupply.getTaxValue4() != null) {
                        grossAmount = grossAmount + itcInwardSupply.getGrossAmount();
                        if (itcInwardSupply.getTaxValue1() != null) {
                            sgst = sgst + itcInwardSupply.getTaxValue1();
                        }
                        if (itcInwardSupply.getTaxValue2() != null) {
                            cgst = cgst + itcInwardSupply.getTaxValue2();
                        }
                        if (itcInwardSupply.getTaxValue3() != null) {
                            igst = igst + itcInwardSupply.getTaxValue3();
                        }
                        if (itcInwardSupply.getTaxValue4() != null) {
                            cess = cess + itcInwardSupply.getTaxValue4();
                        }
                    }
                } else {
                    if (itcInwardSupply.getTaxValue3() != null) {
                        igst7 = igst7 + itcInwardSupply.getTaxValue3();
                    }

                    if (itcInwardSupply.getTaxValue4() != null) {
                        cess7 = cess7 + itcInwardSupply.getTaxValue4();
                    }

                    if (itcInwardSupply.getTaxValue1() != null) {
                        sgst7 = sgst7 + itcInwardSupply.getTaxValue1();
                    }

                    if (itcInwardSupply.getTaxValue2() != null) {
                        cgst7 = cgst7 + itcInwardSupply.getTaxValue2();
                    }
                }
            }

            itcinwardSupplyRow.put("sgst", sgst7);
            itcinwardSupplyRow.put("cgst", cgst7);
            itcinwardSupplyRow.put("igst", igst7);
            itcinwardSupplyRow.put("cess", cess7);
            itcInwardSuppliesAn.add(itcinwardSupplyRow);

            ObjectNode allOtherRow = Json.newObject();
            for (TransactionItems itcInwardSupply : itcinwardSuppliesISDList) {
                if (itcInwardSupply.getTransactionId().getId().equals(IdosConstants.DEBIT_NOTE_VENDOR)) {
                    if (itcInwardSupply.getTaxValue3() != null) {
                        igst8 = igst8 - itcInwardSupply.getTaxValue3();
                    }

                    if (itcInwardSupply.getTaxValue4() != null) {
                        cess8 = cess8 - itcInwardSupply.getTaxValue4();
                    }

                    if (itcInwardSupply.getTaxValue1() != null) {
                        sgst8 = sgst8 - itcInwardSupply.getTaxValue1();
                    }

                    if (itcInwardSupply.getTaxValue2() != null) {
                        cgst8 = cgst8 - itcInwardSupply.getTaxValue2();
                    }
                } else if (itcInwardSupply.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER)
                        && itcInwardSupply.getTransactionId().getTypeIdentifier() == 1) {
                    if (itcInwardSupply.getTaxValue1() != null || itcInwardSupply.getTaxValue2() != null
                            || itcInwardSupply.getTaxValue3() != null || itcInwardSupply.getTaxValue4() != null) {
                        grossAmount = grossAmount + itcInwardSupply.getGrossAmount();
                        if (itcInwardSupply.getTaxValue1() != null) {
                            sgst = sgst + itcInwardSupply.getTaxValue1();
                        }
                        if (itcInwardSupply.getTaxValue2() != null) {
                            cgst = cgst + itcInwardSupply.getTaxValue2();
                        }
                        if (itcInwardSupply.getTaxValue3() != null) {
                            igst = igst + itcInwardSupply.getTaxValue3();
                        }
                        if (itcInwardSupply.getTaxValue4() != null) {
                            cess = cess + itcInwardSupply.getTaxValue4();
                        }
                    }
                } else {
                    if (itcInwardSupply.getTaxValue3() != null) {
                        igst8 = igst8 + itcInwardSupply.getTaxValue3();
                    }

                    if (itcInwardSupply.getTaxValue4() != null) {
                        cess8 = cess8 + itcInwardSupply.getTaxValue4();
                    }

                    if (itcInwardSupply.getTaxValue1() != null) {
                        sgst8 = sgst8 + itcInwardSupply.getTaxValue1();
                    }

                    if (itcInwardSupply.getTaxValue2() != null) {
                        cgst8 = cgst8 + itcInwardSupply.getTaxValue2();
                    }
                }
            }

            allOtherRow.put("sgst", sgst8);
            allOtherRow.put("cgst", cgst8);
            allOtherRow.put("igst", igst8);
            allOtherRow.put("cess", cess8);
            Double totalcgst2 = 0.0;
            Double totalsgst2 = 0.0;
            Double totaligst2 = 0.0;
            Double totalCess2 = 0.0;

            totalcgst2 = cgst5 + cgst6 + cgst7 + cgst8;
            totaligst2 = igst5 + igst6 + igst7 + igst8;
            totalsgst2 = sgst5 + sgst6 + sgst7 + sgst8;
            totalCess2 = cess5 + cess6 + cess7 + cess8;
            allOtherRow.put("totalcgst2", totalcgst2);
            allOtherRow.put("totaligst2", totaligst2);
            allOtherRow.put("totalsgst2", totalsgst2);
            allOtherRow.put("totalCess2", totalCess2);
            itcinwardSuppliesAllAn.add(allOtherRow);

            ObjectNode otherITCRow = Json.newObject();
            for (TransactionItems inwardSupply : allOtherITCList) {
                if (inwardSupply.getTransactionId().getId().equals(IdosConstants.DEBIT_NOTE_VENDOR)) {
                    if (inwardSupply.getTaxValue3() != null) {
                        igst9 = igst9 - inwardSupply.getTaxValue3();
                    }

                    if (inwardSupply.getTaxValue4() != null) {
                        cess9 = cess9 - inwardSupply.getTaxValue4();
                    }

                    if (inwardSupply.getTaxValue1() != null) {
                        sgst9 = sgst9 - inwardSupply.getTaxValue1();
                    }

                    if (inwardSupply.getTaxValue2() != null) {
                        cgst9 = cgst9 - inwardSupply.getTaxValue2();
                    }
                } else if (inwardSupply.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER)
                        && inwardSupply.getTransactionId().getTypeIdentifier() == 1) {
                    if (inwardSupply.getTaxValue1() != null || inwardSupply.getTaxValue2() != null
                            || inwardSupply.getTaxValue3() != null || inwardSupply.getTaxValue4() != null) {
                        grossAmount = grossAmount + inwardSupply.getGrossAmount();
                        if (inwardSupply.getTaxValue1() != null) {
                            sgst = sgst + inwardSupply.getTaxValue1();
                        }
                        if (inwardSupply.getTaxValue2() != null) {
                            cgst = cgst + inwardSupply.getTaxValue2();
                        }
                        if (inwardSupply.getTaxValue3() != null) {
                            igst = igst + inwardSupply.getTaxValue3();
                        }
                        if (inwardSupply.getTaxValue4() != null) {
                            cess = cess + inwardSupply.getTaxValue4();
                        }
                    }
                } else {
                    if (inwardSupply.getTaxValue3() != null) {
                        igst9 = igst9 + inwardSupply.getTaxValue3();
                    }

                    if (inwardSupply.getTaxValue4() != null) {
                        cess9 = cess9 + inwardSupply.getTaxValue4();
                    }

                    if (inwardSupply.getTaxValue1() != null) {
                        sgst9 = sgst9 + inwardSupply.getTaxValue1();
                    }

                    if (inwardSupply.getTaxValue2() != null) {
                        cgst9 = cgst9 + inwardSupply.getTaxValue2();
                    }
                }
            }
            otherITCRow.put("sgst", sgst9);
            otherITCRow.put("cgst", cgst9);
            otherITCRow.put("igst", igst9);
            otherITCRow.put("cess", cess9);
            allOtherITCAn.add(otherITCRow);

            Double SGSTFor4243 = 0.0, CGSTFor4243 = 0.0, IGSTFor4243 = 0.0, CESSFor4243 = 0.0;
            Double SGSTForOther = 0.0, CGSTForOther = 0.0, IGSTForOther = 0.0, CESSForOther = 0.0;

            ObjectNode reversalOfItcRow = Json.newObject();
            for (TransactionItems txnItem : reversalOfITCList) {
                BranchTaxes branchTaxes = BranchTaxes.findById(txnItem.getTax1ID());
                if (txnItem.getReasonForReturn() == IdosConstants.ITC_REASON_AMOUNT_IN_TERMS_OF_RULE_42_1_M
                        || txnItem.getReasonForReturn() == IdosConstants.ITC_REASON_AMOUNT_IN_TERMS_OF_RULE_42_2_A
                        || txnItem.getReasonForReturn() == IdosConstants.ITC_REASON_AMOUNT_IN_TERMS_OF_RULE_42_2_B
                        || txnItem.getReasonForReturn() == IdosConstants.ITC_REASON_AMOUNT_IN_TERMS_OF_RULE_43_1_H) {
                    if (branchTaxes != null) {
                        if (branchTaxes.getTaxName() != null) {
                            if (branchTaxes.getTaxName().startsWith("SGST"))
                                SGSTFor4243 += txnItem.getGrossAmounReturned();
                            else if (branchTaxes.getTaxName().startsWith("CGST"))
                                CGSTFor4243 += txnItem.getGrossAmounReturned();
                            else if (branchTaxes.getTaxName().startsWith("IGST"))
                                IGSTFor4243 += txnItem.getGrossAmounReturned();
                            else if (branchTaxes.getTaxName().startsWith("CESS"))
                                CESSFor4243 += txnItem.getGrossAmounReturned();
                        }
                    }
                } else {
                    if (branchTaxes != null) {
                        if (branchTaxes.getTaxName() != null) {
                            if (branchTaxes.getTaxName().startsWith("SGST"))
                                SGSTForOther += txnItem.getGrossAmounReturned();
                            else if (branchTaxes.getTaxName().startsWith("CGST"))
                                CGSTForOther += txnItem.getGrossAmounReturned();
                            else if (branchTaxes.getTaxName().startsWith("IGST"))
                                IGSTForOther += txnItem.getGrossAmounReturned();
                            else if (branchTaxes.getTaxName().startsWith("CESS"))
                                CESSForOther += txnItem.getGrossAmounReturned();
                        }
                    }
                }
            }
            reversalOfItcRow.put("SGSTFor4243", SGSTFor4243);
            reversalOfItcRow.put("CGSTFor4243", CGSTFor4243);
            reversalOfItcRow.put("IGSTFor4243", IGSTFor4243);
            reversalOfItcRow.put("CESSFor4243", CESSFor4243);
            reversalOfItcRow.put("SGSTForOther", SGSTForOther);
            reversalOfItcRow.put("CGSTForOther", CGSTForOther);
            reversalOfItcRow.put("IGSTForOther", IGSTForOther);
            reversalOfItcRow.put("CESSForOther", CESSForOther);
            reversalOfITCAn.add(reversalOfItcRow);

            inparam1.clear();
            inparam1.add(user.getOrganization().getId());
            inparam1.add(gstIn);
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);

            exemptGSTSuppliesList = genericDAO.queryWithParamsName(SUPPLIER_INTER_QUERY.toString(), entityManager,
                    inparam1);
            inparam1.clear();
            inparam1.add(user.getOrganization().getId());
            inparam1.add(gstIn);
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);

            exemptNONGSTSuplliesList = genericDAO.queryWithParamsName(NON_GST_SUPPLY_INTER_QUERY.toString(),
                    entityManager, inparam1);

            inparam1.clear();
            inparam1.add(user.getOrganization().getId());
            inparam1.add(gstIn);
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);

            exemptGSTINTRASuppliesList = genericDAO.queryWithParamsName(SUPPLIER_INTRA_QUERY.toString(), entityManager,
                    inparam1);
            inparam1.clear();
            inparam1.add(user.getOrganization().getId());
            inparam1.add(gstIn);
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);

            exemptNONGSTINTRASuplliesList = genericDAO.queryWithParamsName(NON_GST_SUPPLY_INTRA_QUERY.toString(),
                    entityManager, inparam1);
            Double interStateGSTSuppliesVal = 0.0;
            Double intraStateGSTSuppliesVal = 0.0;
            Double interStateNONGSTSuppliesVal = 0.0;
            Double intraStateNONGSTSuppliesVal = 0.0;

            for (TransactionItems exepmtGst : exemptGSTSuppliesList) {
                if (exepmtGst.getTransactionId().getTransactionPurpose().getId()
                        .equals(IdosConstants.DEBIT_NOTE_VENDOR)) {
                    interStateGSTSuppliesVal = interStateGSTSuppliesVal - exepmtGst.getGrossAmount();
                } else if (exepmtGst.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER)
                        && exepmtGst.getTransactionId().getTypeIdentifier() == 1) {
                    if (exepmtGst.getTaxValue1() != null || exepmtGst.getTaxValue2() != null
                            || exepmtGst.getTaxValue3() != null || exepmtGst.getTaxValue4() != null) {
                        interStateGSTSuppliesVal = interStateGSTSuppliesVal + exepmtGst.getGrossAmount();
                    }
                } else {
                    interStateGSTSuppliesVal = interStateGSTSuppliesVal + exepmtGst.getGrossAmount();
                }
            }

            for (TransactionItems exepmtGst : exemptGSTINTRASuppliesList) {
                if (exepmtGst.getTransactionId().getTransactionPurpose().getId()
                        .equals(IdosConstants.DEBIT_NOTE_VENDOR)) {
                    intraStateGSTSuppliesVal = intraStateGSTSuppliesVal - exepmtGst.getGrossAmount();
                } else if (exepmtGst.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER)
                        && exepmtGst.getTransactionId().getTypeIdentifier() == 1) {
                    if (exepmtGst.getTaxValue1() != null || exepmtGst.getTaxValue2() != null
                            || exepmtGst.getTaxValue3() != null || exepmtGst.getTaxValue4() != null) {
                        intraStateGSTSuppliesVal = interStateGSTSuppliesVal + exepmtGst.getGrossAmount();
                    }
                } else {
                    intraStateGSTSuppliesVal = intraStateGSTSuppliesVal + exepmtGst.getGrossAmount();
                }
            }

            for (TransactionItems exepmtGst : exemptNONGSTSuplliesList) {
                if (exepmtGst.getTransactionId().getTransactionPurpose().getId()
                        .equals(IdosConstants.DEBIT_NOTE_VENDOR)) {
                    interStateNONGSTSuppliesVal = interStateNONGSTSuppliesVal - exepmtGst.getGrossAmount();
                } else if (exepmtGst.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER)
                        && exepmtGst.getTransactionId().getTypeIdentifier() == 1) {
                    if (exepmtGst.getTaxValue1() != null || exepmtGst.getTaxValue2() != null
                            || exepmtGst.getTaxValue3() != null || exepmtGst.getTaxValue4() != null) {
                        interStateNONGSTSuppliesVal = interStateNONGSTSuppliesVal + exepmtGst.getGrossAmount();
                    }
                } else {
                    interStateNONGSTSuppliesVal = interStateNONGSTSuppliesVal + exepmtGst.getGrossAmount();
                }
            }

            for (TransactionItems exepmtGst : exemptNONGSTINTRASuplliesList) {
                if (exepmtGst.getTransactionId().getTransactionPurpose().getId()
                        .equals(IdosConstants.DEBIT_NOTE_VENDOR)) {
                    intraStateNONGSTSuppliesVal = intraStateNONGSTSuppliesVal - exepmtGst.getGrossAmount();
                } else if (exepmtGst.getTransactionId().getTransactionPurpose()
                        .equals(IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER)
                        && exepmtGst.getTransactionId().getTypeIdentifier() == 1) {
                    if (exepmtGst.getTaxValue1() != null || exepmtGst.getTaxValue2() != null
                            || exepmtGst.getTaxValue3() != null || exepmtGst.getTaxValue4() != null) {
                        intraStateNONGSTSuppliesVal = intraStateNONGSTSuppliesVal + exepmtGst.getGrossAmount();
                    }
                } else {
                    intraStateNONGSTSuppliesVal = intraStateNONGSTSuppliesVal + exepmtGst.getGrossAmount();
                }
            }

            ObjectNode exemptRow = Json.newObject();
            // ObjectNode totalexemptRow = Json.newObject();
            exemptRow.put("interStateGSTSupplies", interStateGSTSuppliesVal);
            exemptRow.put("intraStateGSTSupplies", intraStateGSTSuppliesVal);
            exemptRow.put("interStateNONGSTSupplies", interStateNONGSTSuppliesVal);
            exemptRow.put("intraStateNONGSTSupplies", intraStateNONGSTSuppliesVal);

            Double totalExemptINTER = 0.0;
            Double totalExemptINTRA = 0.0;
            totalExemptINTER = interStateGSTSuppliesVal + interStateNONGSTSuppliesVal;
            totalExemptINTRA = intraStateGSTSuppliesVal + intraStateNONGSTSuppliesVal;
            exemptRow.put("totalExemptInter", totalExemptINTER);
            exemptRow.put("totalExemptIntra", totalExemptINTRA);
            exemptGSTSupplyAn.add(exemptRow);
            // exemptGSTSupplyAn.add(totalexemptRow);

            // multiple states data
            ArrayNode unregisteredSupplyData = result.putArray("unregisteredSupplyData");
            for (int stateCode = 0; stateCode <= 37; stateCode++) {
                List<TransactionItems> unregisterredSupplies = null;
                List<TransactionItems> compositionTaxableSupplies = null;
                List<TransactionItems> uinSupplies = null;
                String destinationStateCode = String.format("%02d", stateCode);
                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(fromTransDate);
                inparam1.add(toTransDate);
                inparam1.add(destinationStateCode);

                unregisterredSupplies = genericDAO.queryWithParamsName(UNREGISTERED_SUPPLY_QUERY.toString(),
                        entityManager, inparam1);
                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(fromTransDate);
                inparam1.add(toTransDate);
                inparam1.add(destinationStateCode);

                compositionTaxableSupplies = genericDAO.queryWithParamsName(COMPOSITION_DEALER_SUPPLY_QUERY.toString(),
                        entityManager, inparam1);
                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(fromTransDate);
                inparam1.add(toTransDate);
                inparam1.add(destinationStateCode);
                uinSupplies = genericDAO.queryWithParamsName(UIN_HOLDERS_SUPPLY_QUERY.toString(), entityManager,
                        inparam1);
                grossAmount = 0.0;
                igst = 0.0;
                for (TransactionItems unregisterredSupply : unregisterredSupplies) {
                    if (unregisterredSupply.getTransactionId().getTransactionVendorCustomer() != null) {
                        Vendor customer = Vendor.findById(
                                unregisterredSupply.getTransactionId().getTransactionVendorCustomer().getId());
                        if (customer.getIsRegistered() == 0) {
                            if (unregisterredSupply.getTransactionId().getTransactionPurpose()
                                    .equals(IdosConstants.CREDIT_NOTE_CUSTOMER)) {
                                grossAmount = grossAmount - unregisterredSupply.getGrossAmount();
                                if (unregisterredSupply.getTaxValue3() != null) {
                                    igst = igst - unregisterredSupply.getTaxValue3();
                                }
                            } else if (unregisterredSupply.getTransactionId().getTransactionPurpose()
                                    .equals(IdosConstants.REFUND_ADVANCE_RECEIVED)) {
                                grossAmount = grossAmount + unregisterredSupply.getGrossAmount()
                                        + unregisterredSupply.getWithholdingAmountReturned()
                                        - unregisterredSupply.getTaxValue3() - unregisterredSupply.getTaxValue4();
                                if (unregisterredSupply.getTaxValue3() != null) {
                                    igst = igst - unregisterredSupply.getTaxValue3();
                                }
                            } else {
                                if (unregisterredSupply.getGrossAmount() != null)
                                    grossAmount = grossAmount + unregisterredSupply.getGrossAmount();
                                if (unregisterredSupply.getTaxValue3() != null) {
                                    igst = igst + unregisterredSupply.getTaxValue3();
                                }
                            }
                        }
                    }
                }
                grossAmount1 = 0.0;
                igst1 = 0.0;
                for (TransactionItems unregisterredSupply : compositionTaxableSupplies) {
                    if (unregisterredSupply.getTransactionId().getTransactionVendorCustomer() != null) {
                        Vendor customer = Vendor.findById(
                                unregisterredSupply.getTransactionId().getTransactionVendorCustomer().getId());
                        if (customer.getIsBusiness() == 4) {
                            if (unregisterredSupply.getTransactionId().getTransactionPurpose()
                                    .equals(IdosConstants.CREDIT_NOTE_CUSTOMER)) {
                                grossAmount1 = grossAmount1 - unregisterredSupply.getGrossAmount();
                                if (unregisterredSupply.getTaxValue3() != null) {
                                    igst1 = igst1 - unregisterredSupply.getTaxValue3();
                                }
                            } else if (unregisterredSupply.getTransactionId().getTransactionPurpose()
                                    .equals(IdosConstants.REFUND_ADVANCE_RECEIVED)) {
                                grossAmount1 = grossAmount1 + unregisterredSupply.getGrossAmount()
                                        + unregisterredSupply.getWithholdingAmountReturned()
                                        - unregisterredSupply.getTaxValue3() - unregisterredSupply.getTaxValue4();
                                if (unregisterredSupply.getTaxValue3() != null) {
                                    igst1 = igst1 - unregisterredSupply.getTaxValue3();
                                }
                            } else {
                                grossAmount1 = grossAmount1 + unregisterredSupply.getGrossAmount();
                                if (unregisterredSupply.getTaxValue3() != null) {
                                    igst1 = igst1 + unregisterredSupply.getTaxValue3();
                                }
                            }
                        }
                    }
                }
                grossAmount2 = 0.0;
                igst2 = 0.0;
                for (TransactionItems unregisterredSupply : uinSupplies) {
                    if (unregisterredSupply.getTransactionId().getTransactionVendorCustomer() != null) {
                        Vendor customer = Vendor.findById(
                                unregisterredSupply.getTransactionId().getTransactionVendorCustomer().getId());
                        if (customer.getIsBusiness() == 3) {
                            if (unregisterredSupply.getTransactionId().getTransactionPurpose()
                                    .equals(IdosConstants.CREDIT_NOTE_CUSTOMER)) {
                                grossAmount2 = grossAmount2 - unregisterredSupply.getGrossAmount();
                                if (unregisterredSupply.getTaxValue3() != null) {
                                    igst2 = igst2 - unregisterredSupply.getTaxValue3();
                                }
                            } else if (unregisterredSupply.getTransactionId().getTransactionPurpose()
                                    .equals(IdosConstants.REFUND_ADVANCE_RECEIVED)) {
                                grossAmount2 = grossAmount2 + unregisterredSupply.getGrossAmount()
                                        + unregisterredSupply.getWithholdingAmountReturned()
                                        - unregisterredSupply.getTaxValue3() - unregisterredSupply.getTaxValue4();
                                if (unregisterredSupply.getTaxValue3() != null) {
                                    igst2 = igst2 - unregisterredSupply.getTaxValue3();
                                }
                            } else {
                                grossAmount2 = grossAmount2 + unregisterredSupply.getGrossAmount();
                                if (unregisterredSupply.getTaxValue3() != null) {
                                    igst2 = igst2 + unregisterredSupply.getTaxValue3();
                                }
                            }
                        }
                    }
                }
                if (grossAmount != 0.00 || igst != 0.00 || grossAmount1 != 0.00 || igst1 != 0.00 || grossAmount2 != 0.00
                        || igst2 != 0.00) {
                    ObjectNode unregisteredData = Json.newObject();
                    unregisteredData.put("destinationStateCode", destinationStateCode);
                    unregisteredData.put("grossAmount", IdosConstants.decimalFormat.format(grossAmount));
                    unregisteredData.put("igst", IdosConstants.decimalFormat.format(igst));
                    unregisteredData.put("grossAmount1", IdosConstants.decimalFormat.format(grossAmount1));
                    unregisteredData.put("igst1", IdosConstants.decimalFormat.format(igst1));
                    unregisteredData.put("grossAmount2", IdosConstants.decimalFormat.format(grossAmount2));
                    unregisteredData.put("igst2", IdosConstants.decimalFormat.format(igst2));
                    unregisteredSupplyData.add(unregisteredData);
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
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result getGSTR3BTableThreePointOneDataForKarvy(Request request) {
        // EntityManager entityManager = getEntityManager();
        Users user = null;
        user = getUserInfo(request);
        if (user == null) {
            return unauthorized();
        }
        ObjectNode result = Json.newObject();
        try {
            List<TransactionItems> unregisterredSupplies = null;
            List<TransactionItems> compositionTaxableSupplies = null;
            List<TransactionItems> uinSupplies = null;

            String dateMonthAndYearForMonthWise = "";
            String dateMonthAndYearForQuarterWise = "";
            String dateMonthAndYearFromDate = "";
            String dateMonthAndYearToDate = "";
            JsonNode json = request.body().asJson();
            String useremail = json.findValue("useremail").asText();
            // String type = json.findValue("type").asText();
            Integer intervalType = json.findValue("intervalType").asInt();
            // String selectedValues=json.findValues("selectedValues").toString();
            if (intervalType == 1) {
                dateMonthAndYearForMonthWise = json.findValue("txtDate1").asText();
            } else if (intervalType == 2) {
                dateMonthAndYearForQuarterWise = json.findValue("txtDate2").asText();
            } else if (intervalType == 3) {
                dateMonthAndYearFromDate = json.findValue("txtDate3").asText();
                dateMonthAndYearToDate = json.findValue("txtDate4").asText();
            }
            String gstIn = json.findValue("gstIn").asText();
            String destinationStateCode = json.findValue("stateCode").asText();

            Date fromTransDate = null;
            Date toTransDate = null;
            String gstr1period = "";
            if (intervalType == 1) {

                String year = dateMonthAndYearForMonthWise.substring(dateMonthAndYearForMonthWise.length() - 4,
                        dateMonthAndYearForMonthWise.length());
                String month = dateMonthAndYearForMonthWise.substring(0, dateMonthAndYearForMonthWise.length() - 5);
                // January 1,2018
                String dateinjava = month + " 1," + year;

                fromTransDate = IdosConstants.MYSQLDF
                        .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateinjava)));
                Calendar cal = Calendar.getInstance();
                toTransDate = cal.getTime();
                cal.setTime(fromTransDate);
                Integer monthInInt = cal.get(Calendar.MONTH) + 1;
                gstr1period = monthInInt.toString() + year;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                Date date = sdf.parse(year + "-" + (monthInInt < 10 ? ("0" + monthInInt) : monthInInt) + "-01");

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                calendar.add(Calendar.MONTH, 1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.DATE, -1);

                toTransDate = IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(calendar.getTime()));
            }
            if (intervalType == 2) {
                String year = dateMonthAndYearForQuarterWise.substring(dateMonthAndYearForQuarterWise.length() - 4,
                        dateMonthAndYearForQuarterWise.length());
                String month = dateMonthAndYearForQuarterWise.substring(0, dateMonthAndYearForQuarterWise.length() - 5);
                // January 1,2018
                String dateinjava = month + " 1," + year;

                Date fromTransDate1 = IdosConstants.MYSQLDF
                        .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateinjava)));
                Calendar cal = Calendar.getInstance();
                cal.setTime(fromTransDate1);
                cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) / 3 * 3);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                fromTransDate = IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(cal.getTime()));
                Integer monthInInt = cal.get(Calendar.MONTH) + 1;
                gstr1period = monthInInt.toString() + year;
                Calendar cal1 = Calendar.getInstance();
                cal1.setTime(fromTransDate1);
                cal1.set(Calendar.MONTH, cal.get(Calendar.MONTH) / 3 * 3 + 2);
                cal1.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                toTransDate = IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(cal1.getTime()));
            }
            if (intervalType == 3) {
                fromTransDate = IdosConstants.MYSQLDF
                        .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateMonthAndYearFromDate)));
                Calendar cal = Calendar.getInstance();
                cal.setTime(fromTransDate);
                Integer monthInInt = cal.get(Calendar.MONTH) + 1;
                Integer year = cal.get(Calendar.YEAR);
                gstr1period = monthInInt.toString() + year;
                toTransDate = IdosConstants.MYSQLDF
                        .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateMonthAndYearToDate)));
            }
            ArrayList<Object> inparam1 = new ArrayList<Object>(5);

            inparam1.clear();
            inparam1.add(user.getOrganization().getId());
            inparam1.add(gstIn);
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);
            inparam1.add(destinationStateCode);

            unregisterredSupplies = genericDAO.queryWithParamsName(UNREGISTERED_SUPPLY_QUERY.toString(), entityManager,
                    inparam1);
            inparam1.clear();
            inparam1.add(user.getOrganization().getId());
            inparam1.add(gstIn);
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);
            inparam1.add(destinationStateCode);

            compositionTaxableSupplies = genericDAO.queryWithParamsName(COMPOSITION_DEALER_SUPPLY_QUERY.toString(),
                    entityManager, inparam1);
            inparam1.clear();
            inparam1.add(user.getOrganization().getId());
            inparam1.add(gstIn);
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);
            inparam1.add(destinationStateCode);
            uinSupplies = genericDAO.queryWithParamsName(UIN_HOLDERS_SUPPLY_QUERY.toString(), entityManager, inparam1);
            Double grossAmount = 0.0;
            Double igst = 0.0;
            for (TransactionItems unregisterredSupply : unregisterredSupplies) {
                if (unregisterredSupply.getTransactionId().getTransactionVendorCustomer() != null) {
                    Vendor customer = Vendor
                            .findById(unregisterredSupply.getTransactionId().getTransactionVendorCustomer().getId());
                    if (customer.getIsRegistered() == 2) {
                        if (unregisterredSupply.getTransactionId().getTransactionPurpose()
                                .equals(IdosConstants.CREDIT_NOTE_CUSTOMER)) {
                            grossAmount = grossAmount - unregisterredSupply.getGrossAmount();
                            if (unregisterredSupply.getTaxValue3() != null) {
                                igst = igst - unregisterredSupply.getTaxValue3();
                            }
                        } else if (unregisterredSupply.getTransactionId().getTransactionPurpose()
                                .equals(IdosConstants.REFUND_ADVANCE_RECEIVED)) {
                            grossAmount = grossAmount + unregisterredSupply.getGrossAmount()
                                    + unregisterredSupply.getWithholdingAmountReturned()
                                    - unregisterredSupply.getTaxValue3() - unregisterredSupply.getTaxValue4();
                            if (unregisterredSupply.getTaxValue3() != null) {
                                igst = igst - unregisterredSupply.getTaxValue3();
                            }
                        } else {
                            grossAmount = grossAmount + unregisterredSupply.getGrossAmount();
                            if (unregisterredSupply.getTaxValue3() != null) {
                                igst = igst + unregisterredSupply.getTaxValue3();
                            }
                        }
                    }
                }
            }
            Double grossAmount1 = 0.0;
            Double igst1 = 0.0;
            for (TransactionItems unregisterredSupply : compositionTaxableSupplies) {
                if (unregisterredSupply.getVendCustID() != null) {
                    Vendor customer = Vendor
                            .findById(unregisterredSupply.getTransactionId().getTransactionVendorCustomer().getId());
                    if (customer.getType() == 4) {
                        if (unregisterredSupply.getTransactionId().getTransactionPurpose()
                                .equals(IdosConstants.CREDIT_NOTE_CUSTOMER)) {
                            grossAmount1 = grossAmount1 - unregisterredSupply.getGrossAmount();
                            if (unregisterredSupply.getTaxValue3() != null) {
                                igst1 = igst1 - unregisterredSupply.getTaxValue3();
                            }
                        } else if (unregisterredSupply.getTransactionId().getTransactionPurpose()
                                .equals(IdosConstants.REFUND_ADVANCE_RECEIVED)) {
                            grossAmount1 = grossAmount1 + unregisterredSupply.getGrossAmount()
                                    + unregisterredSupply.getWithholdingAmountReturned()
                                    - unregisterredSupply.getTaxValue3() - unregisterredSupply.getTaxValue4();
                            if (unregisterredSupply.getTaxValue3() != null) {
                                igst1 = igst1 - unregisterredSupply.getTaxValue3();
                            }
                        } else {
                            grossAmount1 = grossAmount1 + unregisterredSupply.getGrossAmount();
                            if (unregisterredSupply.getTaxValue3() != null) {
                                igst1 = igst1 + unregisterredSupply.getTaxValue3();
                            }
                        }
                    }
                }
            }
            Double grossAmount2 = 0.0;
            Double igst2 = 0.0;
            for (TransactionItems unregisterredSupply : uinSupplies) {
                if (unregisterredSupply.getVendCustID() != null) {
                    Vendor customer = Vendor
                            .findById(unregisterredSupply.getTransactionId().getTransactionVendorCustomer().getId());
                    if (customer.getType() == 3) {
                        if (unregisterredSupply.getTransactionId().getTransactionPurpose()
                                .equals(IdosConstants.CREDIT_NOTE_CUSTOMER)) {
                            grossAmount2 = grossAmount2 - unregisterredSupply.getGrossAmount();
                            if (unregisterredSupply.getTaxValue3() != null) {
                                igst2 = igst2 - unregisterredSupply.getTaxValue3();
                            }
                        } else if (unregisterredSupply.getTransactionId().getTransactionPurpose()
                                .equals(IdosConstants.REFUND_ADVANCE_RECEIVED)) {
                            grossAmount2 = grossAmount2 + unregisterredSupply.getGrossAmount()
                                    + unregisterredSupply.getWithholdingAmountReturned()
                                    - unregisterredSupply.getTaxValue3() - unregisterredSupply.getTaxValue4();
                            if (unregisterredSupply.getTaxValue3() != null) {
                                igst2 = igst2 - unregisterredSupply.getTaxValue3();
                            }
                        } else {
                            grossAmount2 = grossAmount2 + unregisterredSupply.getGrossAmount();
                            if (unregisterredSupply.getTaxValue3() != null) {
                                igst2 = igst2 + unregisterredSupply.getTaxValue3();
                            }
                        }
                    }
                }
            }
            ArrayNode unregisteredSupplyData = result.putArray("unregisteredSupplyData");
            ObjectNode unregisteredData = Json.newObject();
            unregisteredData.put("grossAmount", grossAmount);
            unregisteredData.put("igst", igst);
            unregisteredData.put("grossAmount1", grossAmount1);
            unregisteredData.put("igst1", igst1);
            unregisteredData.put("grossAmount2", grossAmount2);
            unregisteredData.put("igst2", igst2);
            unregisteredSupplyData.add(unregisteredData);

        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }
}
