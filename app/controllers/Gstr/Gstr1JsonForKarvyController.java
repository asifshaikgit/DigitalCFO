package controllers.Gstr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import play.mvc.Results;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;

import controllers.StaticController;
import model.Branch;
import model.BranchBankAccounts;
import model.ConfigParams;
import model.Transaction;
import model.TransactionInvoice;
import model.TransactionItems;
import model.TrialBalanceCOAItems;
import model.Users;
import model.karvy.GSTFiling;
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

public class Gstr1JsonForKarvyController extends StaticController {
    private static final String B2B_B2C_QUERY = "select obj from GSTFiling obj where obj.organizationId.id=?1 and obj.branchId.gstin=?2 and  (obj.transactionPurpose.id=?3 or obj.transactionPurpose.id=?4 or obj.transactionPurpose.id=?5)  and obj.agentName=?6 and obj.presentStatus=1 and obj.transactionDate>=?7 and obj.transactionDate<=?8 and obj.transactionId.transactionStatus=?9";
    private static final String CR_DR_QUERY = "select obj from GSTFiling obj where obj.organizationId.id=?1 and obj.branchId.gstin=?2 and  (obj.transactionPurpose.id=?3 or obj.transactionPurpose.id=?4)  and obj.agentName=?5 and obj.presentStatus=1 and  obj.transactionDate>=?6 and obj.transactionDate<=?7 and obj.transactionId.transactionStatus=?8 and obj.transactionId.transactionVendorCustomer.isRegistered =?9";
    private static final String EXPORT_INVOICE_QUERY = "select obj from GSTFiling obj where obj.organizationId.id=?1 and obj.branchId.gstin=?2 and (obj.transactionPurpose.id=?3 or obj.transactionPurpose.id=?4) and obj.agentName=?5 and obj.presentStatus=1 and obj.transactionDate>=?6 and obj.transactionDate<=?7 and obj.transactionId.typeOfSupply=?8 and obj.transactionId.transactionStatus=?9";
    private static final String B2B_QUERY = "select taxRate1,taxRate2,taxRate3,sum(taxValue1),sum(taxValue2),sum(taxValue3),sum(grossAmount),sum(taxValue4) from TransactionItems where transaction.id=?1 and transaction.transactionStatus='Accounted' and presentStatus=1 group by taxRate1,taxRate2,taxRate3";
    private static final String B2C_AND_EXPORTINVOICE_QUERY = "select taxRate3,sum(taxValue3),sum(grossAmount),sum(taxValue4) from TransactionItems where transaction.id=?1 and transaction.transactionStatus='Accounted' and obj.presentStatus=1 group by taxRate3";
    private static final String B2C_SMALLER_QUERY = "select distinct SUBSTRING(destinationGstin,1,2) from Transaction where transactionBranchOrganization.id=?1 and transactionBranch.gstin=?2 and presentStatus=1 and  (transactionPurpose.id=1 or transactionPurpose.id=2)  and invoiceValue<=250000  and typeOfSupply=1 and (walkinCustomerType=0  or walkinCustomerType=3  or walkinCustomerType=4 or walkinCustomerType=5 or walkinCustomerType=6) and substring(transactionDate,1,10)=?3 and transactionStatus='Accounted' and transactionVendorCustomer.isRegistered=?4";
    private static final String B2C_SMALLER_QUERY2 = "select distinct SUBSTRING(destinationGstin,1,2) from Transaction where transactionBranchOrganization.id=?1 and transactionBranch.gstin=?2 and presentStatus=1 and  (transactionPurpose.id=1 or transactionPurpose.id=2) and invoiceValue<=250000  and typeOfSupply=1 and (walkinCustomerType=0  or walkinCustomerType=3  or walkinCustomerType=4 or walkinCustomerType=5 or walkinCustomerType=6) and transactionDate>=?3 and transactionDate<=?4 and transactionStatus='Accounted' and transactionVendorCustomer.isRegistered=?5";
    private static final String B2C_SMALLER_QUERY3 = "select obj.taxRate1,obj.taxRate2,obj.taxRate3,sum(obj.taxValue1),sum(obj.taxValue2),sum(obj.taxValue3),sum(obj.grossAmount),sum(obj.taxValue4),obj.transaction.transactionRefNumber from TransactionItems obj where SUBSTRING(obj.transaction.destinationGstin,1,2)=?1 and obj.presentStatus=1 and obj.presentStatus=1 and obj.transaction.transactionBranchOrganization.id=?2 and obj.transaction.sourceGstin=?3 and  (obj.transaction.transactionPurpose.id=1 or obj.transaction.transactionPurpose.id=2 or obj.transaction.transactionPurpose.id=30)  and obj.transaction.invoiceValue<=250000  and obj.transaction.typeOfSupply=1 and (obj.transaction.walkinCustomerType=0 or obj.transaction.walkinCustomerType=3 or obj.transaction.walkinCustomerType=4 or obj.transaction.walkinCustomerType=5 or obj.transaction.walkinCustomerType=6) and obj.transaction.transactionDate>=?4 and obj.transaction.transactionDate<=?5 and obj.transaction.transactionStatus='Accounted' and obj.transaction.transactionVendorCustomer.isRegistered=?6 group by obj.taxRate1,obj.taxRate2,obj.taxRate3";
    private static final String B2C_SMALLER_QUERY_DEBIT = "select obj.taxRate1,obj.taxRate2,obj.taxRate3,sum(obj.taxValue1),sum(obj.taxValue2),sum(obj.taxValue3),sum(obj.grossAmount),sum(obj.taxValue4) from TransactionItems obj where SUBSTRING(obj.transaction.destinationGstin,1,2)=?1 and obj.presentStatus=1 and obj.transaction.transactionBranchOrganization.id=?2 and obj.transaction.sourceGstin=?3 and (obj.transaction.transactionPurpose.id=31) and obj.transaction.invoiceValue<=250000  and obj.transaction.typeOfSupply=1 and (obj.transaction.walkinCustomerType=0 or obj.transaction.walkinCustomerType=3 or obj.transaction.walkinCustomerType=4 or obj.transaction.walkinCustomerType=5 or obj.transaction.walkinCustomerType=6) and obj.transaction.transactionDate>=?4 and obj.transaction.transactionDate<=?5 and obj.transaction.transactionStatus='Accounted' and obj.transaction.transactionVendorCustomer.isRegistered=?6 group by obj.taxRate1,obj.taxRate2,obj.taxRate3";
    private static final String ADVANCES_RECEIVED_QUERY1 = "select distinct SUBSTRING(destinationGstin,1,2) from Transaction where transactionBranchOrganization.id=?1 and transactionBranch.gstin=?2 and  transactionPurpose.id=6 and substring(transactionDate,1,10)=?3 and transactionStatus='Accounted' and presentStatus=1";
    private static final String ADVANCES_RECEIVED_QUERY2 = "select distinct SUBSTRING(destinationGstin,1,2) from Transaction where transactionBranchOrganization.id=?1 and transactionBranch.gstin=?2 and  transactionPurpose.id=6 and transactionDate>=?3 and transactionDate<=?4 and transactionStatus='Accounted' and presentStatus=1";
    private static final String ADVANCES_RECEIVED_QUERY3 = "select obj.taxRate1,obj.taxRate2,obj.taxRate3,sum(obj.taxValue1),sum(obj.taxValue2),sum(obj.taxValue3),sum(obj.grossAmount),sum(obj.taxValue4) from TransactionItems obj where SUBSTRING(obj.transaction.destinationGstin,1,2)=?1 and obj.transaction.transactionBranchOrganization.id=?2 and obj.transaction.transactionBranch.gstin=?3 and obj.presentStatus=1 and  obj.transaction.transactionPurpose.id=6  and obj.transaction.transactionDate>=?4 and obj.transaction.transactionDate<=?5 and obj.transaction.transactionStatus='Accounted' group by obj.taxRate1,obj.taxRate2,obj.taxRate3";
    private static final String ADVANCE_ADJ_QUERY1 = "select distinct SUBSTRING(obj.transaction.destinationGstin,1,2) from  AdvanceAdjustmentDetail obj where obj.organization.id=?1 and obj.branch.gstin=?2 and  (obj.transaction.transactionPurpose.id=1 or obj.transaction.transactionPurpose.id=2) and substring(obj.transaction.transactionDate,1,10)=?3 and obj.transaction.transactionStatus='Accounted' and obj.presentStatus=1";
    private static final String ADVANCE_ADJ_QUERY2 = "select distinct SUBSTRING(obj.transaction.destinationGstin,1,2) from  AdvanceAdjustmentDetail obj where obj.organization.id=?1 and obj.branch.gstin=?2 and  (obj.transaction.transactionPurpose.id=1 or obj.transaction.transactionPurpose.id=2) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and obj.transaction.transactionStatus='Accounted' and obj.presentStatus=1";
    private static final String ADVANCE_ADJ_QUERY3 = "select obj.taxRate1,obj.taxRate2,obj.taxRate3,sum(obj.advAdjTax1Value),sum(obj.advAdjTax2Value),sum(obj.advAdjTax3Value),sum(obj.adjustmentFromAdvance),sum(obj.advAdjTax4Value),obj.transaction.transactionRefNumber from TransactionItems obj where SUBSTRING(obj.transaction.destinationGstin,1,2)=?1 and obj.transaction.transactionBranchOrganization.id=?2 and obj.transaction.transactionBranch.gstin=?3 and obj.presentStatus=1 and  (obj.transaction.transactionPurpose=1 or obj.transaction.transactionPurpose=2)  and obj.transaction.transactionDate>=?4 and obj.transaction.transactionDate<=?5 and obj.transaction.transactionStatus='Accounted' group by obj.taxRate1,obj.taxRate2,obj.taxRate3";
    private static final String HSN_QUERY = "select distinct obj.transactionSpecifics.gstItemCode,obj.transactionSpecifics.gstDesc from  TransactionItems obj where obj.organization.id=?1 and obj.branch.gstin=?2 and  obj.transaction.transactionPurpose.id in(1,2,6,30,31,25,35) and obj.transaction.transactionDate>=?3 and obj.transaction.transactionDate<=?4 and obj.transaction.transactionStatus='Accounted' and obj.presentStatus=1";
    private static final String HSN_DATA_QUERY1 = "select obj.transactionSpecifics.incomeUnitsMeasure,sum(obj.noOfUnits),sum(obj.totalTax),sum(obj.grossAmount),sum(obj.taxValue1),sum(obj.taxValue2),sum(obj.taxValue3),sum(obj.taxValue4),obj.transaction.transactionRefNumber from TransactionItems obj where obj.transactionSpecifics.gstItemCode=?1 and obj.transaction.transactionBranchOrganization.id=?2 and obj.transaction.transactionBranch.gstin=?3 and obj.presentStatus=1 and  obj.transaction.transactionPurpose.id in(1,2,6,31,25) and obj.transaction.transactionDate>=?4 and obj.transaction.transactionDate<=?5 and obj.transaction.transactionStatus='Accounted' group by obj.transactionSpecifics.incomeUnitsMeasure";
    private static final String HSN_DATA_QUERY2 = "select obj.transactionSpecifics.incomeUnitsMeasure,sum(obj.noOfUnits),sum(obj.totalTax),sum(obj.grossAmount),sum(obj.taxValue1),sum(obj.taxValue2),sum(obj.taxValue3),sum(obj.taxValue4),obj.transaction.transactionRefNumber from TransactionItems obj where obj.transactionSpecifics.gstItemCode=?1 and obj.transaction.transactionBranchOrganization.id=?2 and obj.transaction.transactionBranch.gstin=?3 and obj.presentStatus=1 and  obj.transaction.transactionPurpose.id in(30,35) and obj.transaction.transactionDate>=?4 and obj.transaction.transactionDate<=?5 and obj.transaction.transactionStatus='Accounted' group by obj.transactionSpecifics.incomeUnitsMeasure";
    private static final String HSN_UNITS_QUERY = "select sum(obj.noOfUnits) from TransactionItems obj where obj.transactionSpecifics.gstItemCode=?1 and obj.transaction.transactionBranchOrganization.id=?2 and obj.transaction.transactionBranch.gstin=?3 and  obj.transaction.transactionPurpose.id in(30,35) and obj.transaction.transactionDate>=?4 and obj.transaction.transactionDate<=?5 and obj.transaction.typeIdentifier=2 and obj.transaction.transactionStatus='Accounted' and obj.presentStatus=1 group by obj.transactionSpecifics.incomeUnitsMeasure";
    private static final String B2B_QUERY1 = "select obj.grossAmount,obj.transaction.transactionRefNumber from TransactionItems obj where SUBSTRING(obj.transaction.destinationGstin,1,2)!=SUBSTRING(obj.transaction.sourceGstin,1,2) and obj.organization.id=?1 and obj.branch.gstin=?2 and  (obj.transaction.transactionPurpose.id=1 or obj.transaction.transactionPurpose.id=2 or obj.transaction.transactionPurpose.id=30 or obj.transaction.transactionPurpose.id=31) and obj.transaction.transactionDate>=?3 and obj.presentStatus=1 and obj.transaction.transactionDate<=?4 and obj.transactionSpecifics.gstItemCategory=?5 and obj.transaction.transactionVendorCustomer.isRegistered=?6 and obj.transaction.transactionStatus='Accounted'";
    private static final String B2B_QUERY2 = "select obj.grossAmount,obj.transaction.transactionRefNumber from TransactionItems obj where SUBSTRING(obj.transaction.destinationGstin,1,2)=SUBSTRING(obj.transaction.sourceGstin,1,2) and obj.organization.id=?1 and obj.branch.gstin=?2 and  (obj.transaction.transactionPurpose.id=1 or obj.transaction.transactionPurpose.id=2 or obj.transaction.transactionPurpose.id=30 or obj.transaction.transactionPurpose.id=31) and obj.transaction.transactionDate>=?3 and obj.presentStatus=1 and obj.transaction.transactionDate<=?4 and obj.transactionSpecifics.gstItemCategory=?5 and obj.transaction.transactionVendorCustomer.isRegistered=?6 and obj.transaction.transactionStatus='Accounted'";
    private static final String B2C_QUERY1 = "select obj.grossAmount,obj.transaction.transactionRefNumber from TransactionItems obj where SUBSTRING(obj.transaction.destinationGstin,1,2)=SUBSTRING(obj.transaction.sourceGstin,1,2) and obj.organization.id=?1 and obj.branch.gstin=?2 and  (obj.transaction.transactionPurpose.id=1 or obj.transaction.transactionPurpose.id=2 or obj.transaction.transactionPurpose.id=30 or obj.transaction.transactionPurpose.id=31) and obj.transaction.transactionDate>=?3 and obj.presentStatus=1 and obj.transaction.transactionDate<=?4 and obj.transactionSpecifics.gstItemCategory=?5 and obj.transaction.transactionVendorCustomer.isRegistered=?6 and obj.transaction.transactionStatus='Accounted'";
    private static final String B2C_QUERY2 = "select obj.grossAmount,obj.transaction.transactionRefNumber from TransactionItems obj where SUBSTRING(obj.transaction.destinationGstin,1,2)!=SUBSTRING(obj.transaction.sourceGstin,1,2) and obj.organization.id=?1 and obj.branch.gstin=?2 and  (obj.transaction.transactionPurpose.id=1 or obj.transaction.transactionPurpose.id=2 or obj.transaction.transactionPurpose.id=30 or obj.transaction.transactionPurpose.id=31) and obj.transaction.transactionDate>=?3 and obj.presentStatus=1 and obj.transaction.transactionDate<=?4 and obj.transactionSpecifics.gstItemCategory=?5 and obj.transaction.transactionVendorCustomer.isRegistered=?6 and obj.transaction.transactionStatus='Accounted'";
    private static final String DOCUMENT_QUERY1 = "select obj from Transaction obj where obj.transactionBranchOrganization.id=?1 and obj.transactionBranch.gstin=?2 and  (obj.transactionPurpose.id=1 or obj.transactionPurpose.id=2) and obj.transactionDate<=?3 and obj.transactionStatus='Accounted' and obj.presentStatus=1 ORDER BY obj.transactionDate desc";
    private static final String DOCUMENT_QUERY2 = "select obj from Transaction obj where obj.transactionBranchOrganization.id=?1 and obj.transactionBranch.gstin=?2 and  (obj.transactionPurpose.id=1 or obj.transactionPurpose.id=2) and obj.transactionDate>=?3 and obj.transactionStatus='Accounted' and obj.presentStatus=1 ORDER BY obj.transactionDate asc";
    private static final String DOCUMENT_QUERY3 = "select count(invoiceNumber) from Transaction where transactionBranchOrganization.id=?1 and transactionBranch.gstin=?2 and  (transactionPurpose.id=1 or transactionPurpose.id=2) and transactionDate>=?3 and transactionDate<=?4 and transactionStatus='Accounted' and obj.presentStatus=1";
    private static final String DOCUMENT_QUERY4 = "select obj from Transaction obj where obj.transactionBranchOrganization.id=?1 and obj.transactionBranch.gstin=?2 and  (obj.transactionPurpose.id=?3) and obj.transactionDate<=?4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 ORDER BY obj.transactionDate desc";
    private static final String DOCUMENT_QUERY5 = "select obj from Transaction obj where obj.transactionBranchOrganization.id=?1 and obj.transactionBranch.gstin=?2 and  (obj.transactionPurpose.id=?3) and obj.transactionDate>=?4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 ORDER BY obj.transactionDate asc";
    private static final String DOCUMENT_QUERY6 = "select count(invoiceNumber) from Transaction where transactionBranchOrganization.id=?1 and transactionBranch.gstin=?2 and  (transactionPurpose.id=?3) and transactionDate>=?4 and transactionDate<=?5 and transactionStatus='Accounted' and obj.presentStatus=1";
    private static final String DOCUMENT_DCQUERY1 = "select obj from Transaction obj where obj.transactionBranchOrganization.id=?1 and obj.transactionBranch.gstin=?2 and   (obj.transactionPurpose.id=?3 and obj.typeIdentifier=?4) and obj.transactionDate<=?5 and obj.transactionStatus='Accounted' and obj.presentStatus=1 ORDER BY obj.transactionDate desc";
    private static final String DOCUMENT_DCQUERY2 = "select obj from Transaction obj where obj.transactionBranchOrganization.id=?1 and obj.transactionBranch.gstin=?2 and  (obj.transactionPurpose.id=?3 and obj.typeIdentifier=?4) and obj.transactionDate>=?5 and obj.transactionStatus='Accounted' and obj.presentStatus=1 ORDER BY obj.transactionDate asc";
    private static final String DOCUMENT_DCQUERY3 = "select count(invoiceNumber) from Transaction where transactionBranchOrganization.id=?1 and transactionBranch.gstin=?2 and  transactionPurpose.id=?3 and transactionDate>=?4 and transactionDate<=?5 and transactionStatus='Accounted' and obj.presentStatus=1";
    private static final String CANCELLED_INVOICES_QUERY = "select count(id) from Transaction where transactionBranchOrganization.id=?1 and transactionBranch.gstin=?2 and  (transactionPurpose.id=38) and transactionDate>=?3 and transactionDate<=?4 and transactionStatus='Accounted' and obj.presentStatus=1";
    private final Application application;
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    private Request request;
    // private Http.Session session = request.session();

    @Inject
    public Gstr1JsonForKarvyController(Application application, JPAApi jpaApi) {
        super(application);
        this.application = application;
        this.jpaApi = jpaApi;
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @Transactional
    public Result downloadGSTR1JSONSFileForKarvy(Request request) {
        log.log(Level.FINE, ">>>> Start inside download GSTR1 data");
        // EntityManager entityManager = getEntityManager();
        Users user = null;
        user = getUserInfo(request);
        if (user == null) {
            return unauthorized();
        }
        ObjectNode result = Json.newObject();
        File file = null;
        String path = application.path().toString() + "/logs/KarvyJSONData/";
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);

        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {

            String dateMonthAndYearForMonthWise = "";
            String dateMonthAndYearForQuarterWise = "";
            String dateMonthAndYearFromDate = "";
            String dateMonthAndYearToDate = "";
            List<GSTFiling> gstFilingsForRegular = null;
            List<GSTFiling> gstFilingsForExport = null;
            List<GSTFiling> gstFilingsForCreditAndDebitNoteRegistered = null;
            List<GSTFiling> gstFilingsForCreditAndDebitNoteUnregistered = null;
            JsonNode json = request.body().asJson();
            String useremail = json.findValue("useremail").asText();
            String type = json.findValue("type").asText();
            Integer intervalType = json.findValue("intervalType").asInt();
            String selectedValues = json.findValues("selectedValues").toString();
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
            List<Transaction> regularTransactions = new ArrayList<Transaction>();
            List<Transaction> creditDebitNoteTransactions = new ArrayList<Transaction>();
            List<Transaction> creditDebitNoteUnregTransactions = new ArrayList<Transaction>();
            List<Transaction> b2bTransactions = new ArrayList<Transaction>();
            List<Transaction> b2cTransactions = new ArrayList<Transaction>();

            if (selectedValues.contains("2") || selectedValues.contains("3")) {
                ArrayList<Object> inparam1 = new ArrayList<Object>(9);
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW);
                inparam1.add(IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER);
                inparam1.add(IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER);
                inparam1.add(ConfigParams.getInstance().getCompanyOwner());
                inparam1.add(fromTransDate);
                inparam1.add(toTransDate);
                inparam1.add("Accounted");
                gstFilingsForRegular = genericDAO.queryWithParamsName(B2B_B2C_QUERY.toString(), entityManager,
                        inparam1);
                for (GSTFiling gstFiling : gstFilingsForRegular) {
                    Transaction isCancelled = Transaction.findCancelledSellInvoice(entityManager,
                            user.getOrganization().getId(), gstFiling.getTransactionId().getTransactionRefNumber());
                    if (isCancelled == null)
                        regularTransactions.add(gstFiling.getTransactionId());
                }
                for (Transaction transaction : regularTransactions) {
                    double totalNetAmtWithoutAdv = 0.0;
                    List<TransactionItems> transactionItems = transaction.getTransactionItems();
                    for (TransactionItems transactionItem : transactionItems) {
                        double netAmtWithoutAdv = transactionItem.getGrossAmount() + transactionItem.getTotalTax();
                        totalNetAmtWithoutAdv = totalNetAmtWithoutAdv + netAmtWithoutAdv;
                    }
                    // if(transaction.getSourceGstin()!=null &&
                    // !transaction.getSourceGstin().equals("") &&
                    // transaction.getDestinationGstin()!=null &&
                    // !transaction.getDestinationGstin().equals("") &&
                    // transaction.getSourceGstin().length()>=2 &&
                    // transaction.getDestinationGstin().length()>=2 ){
                    if (transaction.getTransactionPurpose()
                            .getId() != IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER
                            && totalNetAmtWithoutAdv > 250000.00
                            && (transaction.getWalkinCustomerType() == 0 || transaction.getWalkinCustomerType() == 3
                                    || transaction.getWalkinCustomerType() == 4
                                    || transaction.getWalkinCustomerType() == 5
                                    || transaction.getWalkinCustomerType() == 6)
                            && !transaction.getSourceGstin().substring(0, 2)
                                    .equals(transaction.getDestinationGstin().substring(0, 2))
                            && transaction.getTransactionVendorCustomer().getIsRegistered() == 0) {
                        b2cTransactions.add(transaction);
                    } else if ((transaction.getTypeOfSupply() == 1 || transaction.getTypeOfSupply() == 2
                            || transaction.getTypeOfSupply() == 4 || transaction.getTypeOfSupply() == 5)
                            && (transaction.getWalkinCustomerType() == 0 || transaction.getWalkinCustomerType() == 1
                                    || transaction.getWalkinCustomerType() == 2)) {
                        if (transaction.getTransactionPurpose()
                                .getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                            if (transaction.getTypeIdentifier() == 1
                                    && transaction.getTransactionStatus().equals("Accounted")) {
                                b2bTransactions.add(transaction);
                            }
                        } else {
                            b2bTransactions.add(transaction);
                        }
                    }

                }
            }

            if (selectedValues.contains("4")) {
                ArrayList<Object> inparam1 = new ArrayList<Object>(8);
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(IdosConstants.DEBIT_NOTE_CUSTOMER);
                inparam1.add(IdosConstants.CREDIT_NOTE_CUSTOMER);
                inparam1.add(ConfigParams.getInstance().getCompanyOwner());
                inparam1.add(fromTransDate);
                inparam1.add(toTransDate);
                inparam1.add("Accounted");
                inparam1.add(0);
                gstFilingsForCreditAndDebitNoteRegistered = genericDAO.queryWithParamsName(CR_DR_QUERY.toString(),
                        entityManager, inparam1);
            }

            if (selectedValues.contains("6")) {
                ArrayList<Object> inparam1 = new ArrayList<Object>(8);
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(IdosConstants.DEBIT_NOTE_CUSTOMER);
                inparam1.add(IdosConstants.CREDIT_NOTE_CUSTOMER);
                inparam1.add(ConfigParams.getInstance().getCompanyOwner());
                inparam1.add(fromTransDate);
                inparam1.add(toTransDate);
                inparam1.add("Accounted");
                inparam1.add(0);
                gstFilingsForCreditAndDebitNoteUnregistered = genericDAO.queryWithParamsName(CR_DR_QUERY.toString(),
                        entityManager, inparam1);

            }

            if (selectedValues.contains("5")) {

                ArrayList<Object> inparam2 = new ArrayList<Object>(9);
                inparam2.add(user.getOrganization().getId());
                inparam2.add(gstIn);
                inparam2.add(IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW);
                inparam2.add(IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER);
                inparam2.add(ConfigParams.getInstance().getCompanyOwner());
                inparam2.add(fromTransDate);
                inparam2.add(toTransDate);
                inparam2.add(Integer.parseInt("3"));
                inparam2.add("Accounted");
                gstFilingsForExport = genericDAO.queryWithParamsName(EXPORT_INVOICE_QUERY.toString(), entityManager,
                        inparam2);
            }
            JSONObject outerObject = new JSONObject();
            JSONObject outerObjectItms = new JSONObject();
            JSONObject outerObjectitmsDet = new JSONObject();
            JSONObject outerObjectitmsInv = new JSONObject();

            List<Transaction> exportTransactions = new ArrayList<Transaction>();

            JSONArray outerArray1 = new JSONArray();
            JSONArray outerArray2 = new JSONArray();
            JSONArray outerArray3 = new JSONArray();
            JSONArray outerArray4 = new JSONArray();

            int i = 0;
            if (selectedValues.contains("2")) {
                if (b2bTransactions != null) {
                    JSONObject[] innerObjectForRegular = new JSONObject[b2bTransactions.size()];

                    for (Transaction transaction : b2bTransactions)// change to list1.size()
                    {
                        double totalNetAmtWithoutAdv = 0.0;
                        List<TransactionItems> transactionItems = transaction.getTransactionItems();
                        for (TransactionItems transactionItem : transactionItems) {
                            double netAmtWithoutAdv = transactionItem.getGrossAmount() + transactionItem.getTotalTax();
                            totalNetAmtWithoutAdv = totalNetAmtWithoutAdv + netAmtWithoutAdv;
                        }
                        innerObjectForRegular[i] = new JSONObject();
                        if (transaction.getTransactionPurpose()
                                .getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                            innerObjectForRegular[i].put("ctin", transaction.getTransactionToBranch().getGstin());
                        } else {
                            innerObjectForRegular[i].put("ctin", transaction.getTransactionVendorCustomer().getGstin());
                        }

                        JSONArray innerArrayInv = new JSONArray();

                        int j = 0;
                        JSONArray innerArray = new JSONArray();
                        JSONArray innerArrayItms = new JSONArray();
                        JSONArray innerArrayItmsDet = new JSONArray();
                        ArrayList<Object> inparam1 = new ArrayList<Object>(1);
                        inparam1.add(transaction.getId());
                        List<Object[]> quotationNetAmountListPrev = genericDAO
                                .queryWithParamsNameGeneric(B2B_QUERY.toString(), entityManager, inparam1);
                        JSONObject[] innerObjectForInv = new JSONObject[b2bTransactions.size()];
                        JSONObject[] innerObjectForItemsDet = new JSONObject[quotationNetAmountListPrev.size()];
                        JSONObject[] innerObjectForItemsNum = new JSONObject[quotationNetAmountListPrev.size()];

                        for (Object[] custData : quotationNetAmountListPrev) {
                            innerObjectForItemsNum[j] = new JSONObject();

                            Double rate1 = 0.0;
                            Double rate2 = 0.0;
                            Double rate3 = 0.0;
                            Double rate = 0.0;
                            innerObjectForItemsDet[j] = new JSONObject();

                            if (custData[6] != null) {

                                innerObjectForItemsDet[j].put("txval", Double.parseDouble(String.valueOf(custData[6])));
                            } else {
                                innerObjectForItemsDet[j].put("txval", "");
                            }
                            if (custData[0] != null) {
                                rate1 = Double.parseDouble(String.valueOf(custData[0]));
                            }
                            if (custData[1] != null) {
                                rate2 = Double.parseDouble(String.valueOf(custData[1]));
                            }
                            if (custData[2] != null) {
                                rate3 = Double.parseDouble(String.valueOf(custData[2]));
                            }
                            rate = rate1 + rate2 + rate3;
                            innerObjectForItemsDet[j].put("rt", rate);
                            if (custData[4] != null) {
                                innerObjectForItemsDet[j].put("camt", Double.parseDouble(String.valueOf(custData[4])));
                            } /*
                               * else {
                               * innerObjectForItemsDet[j].put("camt", "");
                               * }
                               */
                            if (custData[3] != null) {
                                innerObjectForItemsDet[j].put("samt", Double.parseDouble(String.valueOf(custData[3])));
                            } /*
                               * else {
                               * innerObjectForItemsDet[j].put("samt", "");
                               * }
                               */
                            if (custData[5] != null) {
                                innerObjectForItemsDet[j].put("iamt", Double.parseDouble(String.valueOf(custData[5])));
                            } /*
                               * else {
                               * innerObjectForItemsDet[j].put("iamt", "");
                               * }
                               */
                            if (custData[7] != null) {
                                innerObjectForItemsDet[j].put("csamt", Double.parseDouble(String.valueOf(custData[7])));
                            } /*
                               * else {
                               * innerObjectForItemsDet[j].put("csamt", "");
                               * }
                               */

                            Integer rateInt = 1;
                            if (rate != null)
                                rateInt = (int) (rate * 100 + 1);
                            String numPrepend = "01";
                            innerObjectForItemsNum[j].put("num", rateInt);
                            innerObjectForItemsNum[j].put("itm_det", innerObjectForItemsDet[j]);
                            innerObjectForInv[j] = new JSONObject();

                            innerObjectForInv[j].put("inum", transaction.getInvoiceNumber());
                            innerObjectForInv[j].put("idt",
                                    IdosConstants.IDOSJSONDATEGSTR1.format(transaction.getTransactionDate()));
                            innerObjectForInv[j].put("val", totalNetAmtWithoutAdv);
                            innerObjectForInv[j].put("pos", transaction.getDestinationGstin().subSequence(0, 2));
                            if (transaction.getTypeOfSupply() == 2) {
                                innerObjectForInv[j].put("rchrg", "Y");
                            } else {
                                innerObjectForInv[j].put("rchrg", "N");
                            }
                            // innerObjectForInv[j].put("diff_percent", " ");
                            if (transaction.getTypeOfSupply() == 4) {
                                if (transaction.getWithWithoutTax() == 1) {
                                    innerObjectForInv[j].put("inv_typ", "SEWP");
                                } else if (transaction.getWithWithoutTax() == 2) {
                                    innerObjectForInv[j].put("inv_typ", "SEWOP");
                                }
                            } else if (transaction.getTypeOfSupply() == 5) {
                                innerObjectForInv[j].put("inv_typ", "DE");
                            } else {
                                innerObjectForInv[j].put("inv_typ", "R");
                            }
                            innerArrayItms.put(innerObjectForItemsNum[j]);
                            innerObjectForInv[j].put("itms", innerArrayItms);

                            innerArrayInv.put(innerObjectForInv[j]);
                            j++;
                        }
                        innerObjectForRegular[i].put("inv", innerArrayInv);
                        outerArray1.put(innerObjectForRegular[i]);
                        i++;
                    }
                }
                outerObject.put("b2b", outerArray1);
            }
            i = 0;
            if (selectedValues.contains("3")) {
                if (b2cTransactions != null) {
                    JSONObject[] innerObjectForB2C = new JSONObject[b2cTransactions.size()];
                    for (Transaction transaction : b2cTransactions)// change to list1.size()
                    {
                        double totalNetAmtWithoutAdv = 0.0;
                        List<TransactionItems> transactionItems = transaction.getTransactionItems();
                        for (TransactionItems transactionItem : transactionItems) {
                            double netAmtWithoutAdv = transactionItem.getGrossAmount() + transactionItem.getTotalTax();
                            totalNetAmtWithoutAdv = totalNetAmtWithoutAdv + netAmtWithoutAdv;
                        }
                        innerObjectForB2C[i] = new JSONObject();
                        innerObjectForB2C[i].put("pos", transaction.getDestinationGstin().substring(0, 2));
                        innerObjectForB2C[i].put("inv__inum", transaction.getInvoiceNumber());
                        innerObjectForB2C[i].put("inv__idt",
                                IdosConstants.IDOSJSONDATE.format(transaction.getTransactionDate()));
                        innerObjectForB2C[i].put("inv__val", totalNetAmtWithoutAdv);

                        innerObjectForB2C[i].put("inv__diff_percent", "");
                        innerObjectForB2C[i].put("inv__typ", "");
                        int j = 0;
                        JSONArray innerArray = new JSONArray();

                        ArrayList<Object> inparam1 = new ArrayList<Object>(1);
                        inparam1.add(transaction.getId());

                        List<Object[]> quotationNetAmountListPrev = genericDAO.queryWithParamsNameGeneric(
                                B2C_AND_EXPORTINVOICE_QUERY.toString(), entityManager, inparam1);
                        JSONObject[] innerObjectForItems = new JSONObject[quotationNetAmountListPrev.size()];
                        innerObjectForB2C[i].put("inv__itms__num", "");
                        for (Object[] custData : quotationNetAmountListPrev) {

                            Double rate = 0.0;
                            innerObjectForItems[j] = new JSONObject();
                            if (custData[2] != null) {

                                innerObjectForItems[j].put("txval", Double.parseDouble(String.valueOf(custData[2])));
                            } else {
                                innerObjectForItems[j].put("txval", "");
                            }
                            if (custData[0] != null) {
                                rate = Double.parseDouble(String.valueOf(custData[0]));
                            }

                            innerObjectForItems[j].put("rt", rate);

                            if (custData[1] != null) {
                                innerObjectForItems[j].put("iamt", Double.parseDouble(String.valueOf(custData[1])));
                            } else {
                                innerObjectForItems[j].put("iamt", "");
                            }
                            if (custData[3] != null) {
                                innerObjectForItems[j].put("csamt", Double.parseDouble(String.valueOf(custData[3])));
                            } else {
                                innerObjectForItems[j].put("csamt", "");
                            }
                            innerArray.put(innerObjectForItems[j]);
                            j++;

                        }
                        innerObjectForB2C[i].put("inv__itms__itm_det", innerArray);

                        outerArray2.put(innerObjectForB2C[i]);
                        i++;
                    }
                }
                outerObject.put("b2cl", outerArray2);
            }

            i = 0;
            if (selectedValues.contains("4")) {
                if (gstFilingsForCreditAndDebitNoteRegistered != null) {
                    JSONObject[] innerObjectForCreditDebitNote = new JSONObject[gstFilingsForCreditAndDebitNoteRegistered
                            .size()];
                    for (GSTFiling gstFiling : gstFilingsForCreditAndDebitNoteRegistered) {
                        creditDebitNoteTransactions.add(gstFiling.getTransactionId());
                    }
                    for (Transaction transaction : creditDebitNoteTransactions) {
                        innerObjectForCreditDebitNote[i] = new JSONObject();
                        innerObjectForCreditDebitNote[i].put("ctin",
                                transaction.getTransactionVendorCustomer().getGstin());
                        innerObjectForCreditDebitNote[i].put("nt__nt_num", transaction.getInvoiceNumber());
                        innerObjectForCreditDebitNote[i].put("nt__nt_dt",
                                IdosConstants.IDOSJSONDATE.format(transaction.getTransactionDate()));
                        if (transaction.getTransactionPurpose().getId().equals(IdosConstants.DEBIT_NOTE_CUSTOMER)) {
                            innerObjectForCreditDebitNote[i].put("nt__ntty", "D");
                        }
                        if (transaction.getTransactionPurpose().getId().equals(IdosConstants.CREDIT_NOTE_CUSTOMER)) {
                            innerObjectForCreditDebitNote[i].put("nt__ntty", "C");
                        }
                        double totalNetAmtWithoutAdv = 0.0;
                        List<TransactionItems> transactionItems = transaction.getTransactionItems();
                        for (TransactionItems transactionItem : transactionItems) {
                            double netAmtWithoutAdv = transactionItem.getGrossAmount() + transactionItem.getTotalTax();
                            totalNetAmtWithoutAdv = totalNetAmtWithoutAdv + netAmtWithoutAdv;
                        }
                        innerObjectForCreditDebitNote[i].put("nt__val", totalNetAmtWithoutAdv);
                        if (transaction.getLinkedTxnRef() != null) {
                            List<Transaction> findByTxnReference = Transaction.findByTxnReference(entityManager,
                                    user.getOrganization().getId(), transaction.getLinkedTxnRef());
                            if (findByTxnReference != null && !findByTxnReference.isEmpty()) {
                                Transaction transactionRef = findByTxnReference.get(0);
                                innerObjectForCreditDebitNote[i].put("nt__inum", transactionRef.getInvoiceNumber());
                                innerObjectForCreditDebitNote[i].put("nt__idt",
                                        IdosConstants.IDOSJSONDATE.format(transactionRef.getTransactionDate()));
                            }
                        } else {
                            innerObjectForCreditDebitNote[i].put("nt_inum", "");
                            innerObjectForCreditDebitNote[i].put("nt_idt", "");
                        }
                        innerObjectForCreditDebitNote[i].put("nt__p_gst", "N");
                        int j = 0;
                        innerObjectForCreditDebitNote[i].put("nt__itms__num", "");
                        JSONArray innerArray = new JSONArray();

                        JSONObject[] innerObjectForItems = new JSONObject[transaction.getTransactionItems().size()];

                        for (TransactionItems transactionItem : transactionItems) {
                            innerObjectForItems[j] = new JSONObject();
                            // innerObjectForItems[j].put("num" ,"");
                            innerObjectForItems[j].put("txval", transactionItem.getGrossAmount());
                            if (transactionItem.getTransactionSpecifics().getGstTaxRate() == null) {
                                innerObjectForItems[j].put("rt", "");

                            } else {
                                if (transactionItem.getTaxValue1() != null && transactionItem.getTaxValue2() != null) {
                                    if (transactionItem.getTaxValue1().equals("")
                                            && transactionItem.getTaxValue2().equals(""))
                                        innerObjectForItems[j].put("rt", "");
                                    else
                                        innerObjectForItems[j].put("rt",
                                                transactionItem.getTransactionSpecifics().getGstTaxRate());
                                }
                                if (transactionItem.getTaxValue3() != null) {
                                    if (transactionItem.getTaxValue3().equals(""))
                                        innerObjectForItems[j].put("rt", "");
                                    else
                                        innerObjectForItems[j].put("rt",
                                                transactionItem.getTransactionSpecifics().getGstTaxRate());
                                }
                            }
                            innerObjectForItems[j].put("camt", transactionItem.getTaxValue1());
                            innerObjectForItems[j].put("samt", transactionItem.getTaxValue2());

                            innerObjectForItems[j].put("iamt", transactionItem.getTaxValue3());
                            innerObjectForItems[j].put("csamt", transactionItem.getTaxValue4());
                            innerArray.put(innerObjectForItems[j]);
                            j++;
                        }
                        innerObjectForCreditDebitNote[i].put("nt__itms__itm_det", innerArray);

                        outerArray3.put(innerObjectForCreditDebitNote[i]);
                        i++;
                    }
                }
                outerObject.put("cdnr", outerArray3);
            }

            i = 0;

            if (selectedValues.contains("6")) {
                if (gstFilingsForCreditAndDebitNoteUnregistered != null) {
                    JSONObject[] innerObjectForCreditDebitNote = new JSONObject[gstFilingsForCreditAndDebitNoteUnregistered
                            .size()];
                    for (GSTFiling gstFiling : gstFilingsForCreditAndDebitNoteUnregistered) {
                        creditDebitNoteUnregTransactions.add(gstFiling.getTransactionId());
                    }
                    for (Transaction transaction : creditDebitNoteUnregTransactions) {
                        innerObjectForCreditDebitNote[i] = new JSONObject();
                        innerObjectForCreditDebitNote[i].put("nt_num", transaction.getInvoiceNumber());
                        innerObjectForCreditDebitNote[i].put("nt_dt",
                                IdosConstants.IDOSJSONDATE.format(transaction.getTransactionDate()));
                        if (transaction.getTransactionPurpose().getId().equals(IdosConstants.DEBIT_NOTE_CUSTOMER)) {
                            innerObjectForCreditDebitNote[i].put("ntty", "D");
                        }
                        if (transaction.getTransactionPurpose().getId().equals(IdosConstants.CREDIT_NOTE_CUSTOMER)) {
                            innerObjectForCreditDebitNote[i].put("ntty", "C");
                        }
                        double totalNetAmtWithoutAdv = 0.0;
                        List<TransactionItems> transactionItems = transaction.getTransactionItems();
                        for (TransactionItems transactionItem : transactionItems) {
                            double netAmtWithoutAdv = transactionItem.getGrossAmount() + transactionItem.getTotalTax();
                            totalNetAmtWithoutAdv = totalNetAmtWithoutAdv + netAmtWithoutAdv;
                        }
                        innerObjectForCreditDebitNote[i].put("val", totalNetAmtWithoutAdv);
                        if (transaction.getLinkedTxnRef() != null) {
                            List<Transaction> findByTxnReference = Transaction.findByTxnReference(entityManager,
                                    user.getOrganization().getId(), transaction.getLinkedTxnRef());
                            if (findByTxnReference != null && !findByTxnReference.isEmpty()) {
                                Transaction transactionRef = findByTxnReference.get(0);
                                innerObjectForCreditDebitNote[i].put("inum", transactionRef.getInvoiceNumber());
                                innerObjectForCreditDebitNote[i].put("idt",
                                        IdosConstants.IDOSJSONDATE.format(transactionRef.getTransactionDate()));
                            }
                        } else {
                            innerObjectForCreditDebitNote[i].put("inum", "");
                            innerObjectForCreditDebitNote[i].put("idt", "");
                        }
                        innerObjectForCreditDebitNote[i].put("p_gst", "N");
                        int j = 0;
                        innerObjectForCreditDebitNote[i].put("itms__num", "");
                        JSONArray innerArray = new JSONArray();
                        List<Transaction> parentTransaction = Transaction.findByTxnReference(entityManager,
                                user.getOrganization().getId(), transaction.getLinkedTxnRef());
                        if (parentTransaction.size() >= 1) {
                            if (parentTransaction.get(0).getInvoiceValue() > 250000)
                                innerObjectForCreditDebitNote[i].put("typ", "B2CL");
                            else if (parentTransaction.get(0).getTypeOfSupply() == 3
                                    && parentTransaction.get(0).getWithWithoutTax() == 1)
                                innerObjectForCreditDebitNote[i].put("typ", "EXPWP");
                            else if (parentTransaction.get(0).getTypeOfSupply() == 3
                                    && parentTransaction.get(0).getWithWithoutTax() == 2)
                                innerObjectForCreditDebitNote[i].put("typ", "EXPWOP");
                            else
                                innerObjectForCreditDebitNote[i].put("typ", "");
                        } else
                            innerObjectForCreditDebitNote[i].put("typ", "");

                        JSONObject[] innerObjectForItems = new JSONObject[transaction.getTransactionItems().size()];

                        for (TransactionItems transactionItem : transactionItems) {
                            innerObjectForItems[j] = new JSONObject();
                            // innerObjectForItems[j].put("num" ,"");
                            innerObjectForItems[j].put("txval", transactionItem.getGrossAmount());
                            if (transactionItem.getTransactionSpecifics().getGstTaxRate() == null) {
                                innerObjectForItems[j].put("rt", "");

                            } else {
                                if (transactionItem.getTaxValue1() != null && transactionItem.getTaxValue2() != null) {
                                    if (transactionItem.getTaxValue1().equals("")
                                            && transactionItem.getTaxValue2().equals(""))
                                        innerObjectForItems[j].put("rt", "");
                                    else
                                        innerObjectForItems[j].put("rt",
                                                transactionItem.getTransactionSpecifics().getGstTaxRate());
                                }
                                if (transactionItem.getTaxValue3() != null) {
                                    if (transactionItem.getTaxValue3().equals(""))
                                        innerObjectForItems[j].put("rt", "");
                                    else
                                        innerObjectForItems[j].put("rt",
                                                transactionItem.getTransactionSpecifics().getGstTaxRate());
                                }
                            }
                            innerObjectForItems[j].put("camt", transactionItem.getTaxValue1());
                            innerObjectForItems[j].put("samt", transactionItem.getTaxValue2());

                            innerObjectForItems[j].put("iamt", transactionItem.getTaxValue3());
                            innerObjectForItems[j].put("csamt", transactionItem.getTaxValue4());
                            innerArray.put(innerObjectForItems[j]);
                            j++;
                        }
                        innerObjectForCreditDebitNote[i].put("itms__itm_det", innerArray);

                        outerArray3.put(innerObjectForCreditDebitNote[i]);
                        i++;
                    }
                }
                outerObject.put("cdnur", outerArray3);
            }

            i = 0;
            if (selectedValues.contains("5")) {
                if (gstFilingsForExport != null) {
                    JSONObject[] innerObjectForExport = new JSONObject[gstFilingsForExport.size()];
                    for (GSTFiling gstFiling : gstFilingsForExport) {
                        Transaction isCancelled = Transaction.findCancelledSellInvoice(entityManager,
                                user.getOrganization().getId(), gstFiling.getTransactionId().getTransactionRefNumber());
                        if (isCancelled == null)
                            exportTransactions.add(gstFiling.getTransactionId());
                    }
                    for (Transaction transaction : exportTransactions)// change to list1.size()
                    {
                        innerObjectForExport[i] = new JSONObject();
                        if (transaction.getWithWithoutTax() == 2) {
                            innerObjectForExport[i].put("exp_typ", "WOPAY");
                        } else {
                            innerObjectForExport[i].put("exp_typ", "WPAY");
                        }
                        innerObjectForExport[i].put("inv__inum", transaction.getInvoiceNumber());
                        innerObjectForExport[i].put("inv__idt",
                                IdosConstants.IDOSJSONDATE.format(transaction.getTransactionDate()));
                        innerObjectForExport[i].put("inv__val", transaction.getNetAmount());
                        String invoiceNo = "";
                        String invoiceDate = "";
                        Map<String, Object> criteria = new HashMap<String, Object>();
                        criteria.put("organization.id", transaction.getTransactionBranchOrganization().getId());
                        criteria.put("transaction.id", transaction.getId());
                        criteria.put("presentStatus", 1);
                        TransactionInvoice transationInvoice = genericDAO.getByCriteria(TransactionInvoice.class,
                                criteria, entityManager);
                        if (transationInvoice != null) {
                            if (transationInvoice.getPortCode() != null) {
                                innerObjectForExport[i].put("inv__sbpcode", transationInvoice.getPortCode());
                            } else {
                                innerObjectForExport[i].put("inv__sbpcode", "");
                            }
                            if (transationInvoice.getApplNumberGoodsRemoval() != null) {
                                innerObjectForExport[i].put("inv__sbnum",
                                        transationInvoice.getApplNumberGoodsRemoval());
                            } else {
                                innerObjectForExport[i].put("inv__sbnum", "");
                            }
                            if (transationInvoice.getDateRemovalGoods() != null) {
                                innerObjectForExport[i].put("inv__sbdt",
                                        IdosConstants.IDOSJSONDATE.format(transationInvoice.getDateRemovalGoods()));
                            } else {
                                innerObjectForExport[i].put("inv__sbdt", "");
                            }
                        } else {
                            innerObjectForExport[i].put("inv__sbpcode", "");
                            innerObjectForExport[i].put("inv__sbnum", "");
                            innerObjectForExport[i].put("inv__sbdt", "");
                        }
                        innerObjectForExport[i].put("inv__diff_percent", "");
                        int j = 0;
                        JSONArray innerArray = new JSONArray();

                        ArrayList<Object> inparam1 = new ArrayList<Object>(1);
                        inparam1.add(transaction.getId());
                        List<Object[]> quotationNetAmountListPrev = genericDAO.queryWithParamsNameGeneric(
                                B2C_AND_EXPORTINVOICE_QUERY.toString(), entityManager, inparam1);
                        JSONObject[] innerObjectForItems = new JSONObject[quotationNetAmountListPrev.size()];
                        for (Object[] custData : quotationNetAmountListPrev) {

                            Double rate = 0.0;
                            innerObjectForItems[j] = new JSONObject();
                            if (custData[2] != null) {

                                innerObjectForItems[j].put("txval", Double.parseDouble(String.valueOf(custData[2])));
                            } else {
                                innerObjectForItems[j].put("txval", "");
                            }
                            if (custData[0] != null) {
                                rate = Double.parseDouble(String.valueOf(custData[0]));
                            }

                            innerObjectForItems[j].put("rt", rate);

                            if (custData[1] != null) {
                                innerObjectForItems[j].put("iamt", Double.parseDouble(String.valueOf(custData[1])));
                            } else {
                                innerObjectForItems[j].put("iamt", "");
                            }
                            if (custData[3] != null) {
                                innerObjectForItems[j].put("csamt", Double.parseDouble(String.valueOf(custData[3])));
                            } else {
                                innerObjectForItems[j].put("csamt", "");
                            }
                            innerArray.put(innerObjectForItems[j]);
                            j++;
                        }
                        innerObjectForExport[i].put("inv__itms", innerArray);

                        outerArray4.put(innerObjectForExport[i]);

                        i++;
                    }
                }
                outerObject.put("exp", outerArray4);
            }

            outerObject.put("fp", gstr1period);
            outerObject.put("gstin", gstIn);
            outerObject.put("hash", "hash");
            FileWriter fw = null;
            BufferedWriter writer = null;

            File fileDir = new File(path);
            if (!fileDir.exists()) {
                fileDir.mkdir();
            }
            String fileName = user.getOrganization().getName() + "_" + gstr1period + "_GSTR1.json";
            path = path + fileName;
            file = new File(path);
            try {

                fw = new FileWriter(file);
                writer = new BufferedWriter(fw);
                writer.append(outerObject.toString());
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
            log.log(Level.INFO, "inside error");
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }

    }

    @Transactional
    public Result downloadKarvyGSTR1JSONSFileForOtherTransactionsKarvy(Request request) {
        log.log(Level.FINE, ">>>> Start inside download GSTR1 data");
        // EntityManager entityManager = getEntityManager();
        Users user = null;
        user = getUserInfo(request);
        if (user == null) {
            return unauthorized();
        }
        ObjectNode result = Json.newObject();
        File file = null;
        String path = application.path().toString() + "/logs/KarvyJSONData/";
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);

        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {

            String dateMonthAndYearForMonthWise = "";
            String dateMonthAndYearForQuarterWise = "";
            String dateMonthAndYearFromDate = "";
            String dateMonthAndYearToDate = "";

            JsonNode json = request.body().asJson();
            String useremail = json.findValue("useremail").asText();
            String type = json.findValue("type").asText();
            Integer intervalType = json.findValue("intervalType").asInt();
            String selectedValues = json.findValues("selectedValues").toString();
            if (intervalType == 1) {
                dateMonthAndYearForMonthWise = json.findValue("txtDate1").asText();
            } else if (intervalType == 2) {
                dateMonthAndYearForQuarterWise = json.findValue("txtDate2").asText();
            } else if (intervalType == 3) {
                dateMonthAndYearFromDate = json.findValue("txtDate3").asText();
                dateMonthAndYearToDate = json.findValue("txtDate4").asText();
            }
            String gstIn = json.findValue("gstIn").asText();

            /*
             * Date fromTransDate=null;
             * Date toTransDate=null;
             * String gstr1period="";
             * if(intervalType==1){
             * 
             * String
             * year=dateMonthAndYearForMonthWise.substring(dateMonthAndYearForMonthWise.
             * length()-4, dateMonthAndYearForMonthWise.length());
             * String month
             * =dateMonthAndYearForMonthWise.substring(0,dateMonthAndYearForMonthWise.length
             * ()-5);
             * //January 1,2018
             * String dateinjava=month+" 1,"+year;
             * 
             * fromTransDate=IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(
             * IdosConstants.IDOSDF.parse(dateinjava)));
             * 
             * Calendar cal = Calendar.getInstance();
             * toTransDate=cal.getTime();
             * cal.setTime(fromTransDate);
             * Integer monthInInt = cal.get(Calendar.MONTH)+1;
             * gstr1period = monthInInt.toString()+year;
             * SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
             * 
             * Date date =
             * sdf.parse(year+"-"+(monthInInt<10?("0"+monthInInt):monthInInt)+"-01");
             * 
             * Calendar calendar = Calendar.getInstance();
             * calendar.setTime(date);
             * 
             * calendar.add(Calendar.MONTH, 1);
             * calendar.set(Calendar.DAY_OF_MONTH, 1);
             * calendar.add(Calendar.DATE, -1);
             * 
             * toTransDate =
             * IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(calendar.getTime()))
             * ;
             * }
             * if(intervalType==2){
             * String
             * year=dateMonthAndYearForQuarterWise.substring(dateMonthAndYearForQuarterWise.
             * length()-4, dateMonthAndYearForQuarterWise.length());
             * String month
             * =dateMonthAndYearForQuarterWise.substring(0,dateMonthAndYearForQuarterWise.
             * length()-5);
             * //January 1,2018
             * String dateinjava=month+" 1,"+year;
             * 
             * Date fromTransDate1=IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(
             * IdosConstants.IDOSDF.parse(dateinjava)));
             * Calendar cal = Calendar.getInstance();
             * cal.setTime(fromTransDate1);
             * cal.set(Calendar.MONTH, cal.get(Calendar.MONTH)/3 * 3);
             * cal.set(Calendar.DAY_OF_MONTH, 1);
             * fromTransDate=IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(cal.
             * getTime()));
             * Integer monthInInt = cal.get(Calendar.MONTH)+1;
             * gstr1period = monthInInt.toString()+year;
             * Calendar cal1 = Calendar.getInstance();
             * cal1.setTime(fromTransDate1);
             * cal1.set(Calendar.MONTH, cal.get(Calendar.MONTH)/3 * 3 + 2);
             * cal1.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
             * toTransDate
             * =IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(cal1.getTime()));
             * }
             * if(intervalType==3){
             * fromTransDate =
             * MYSQLDF.parse(MYSQLDF.format(IDOSDF.parse(dateMonthAndYearFromDate)));
             * 
             * //String
             * year=dateMonthAndYearForQuarterWise.substring(dateMonthAndYearFromDate.length
             * ()-4, dateMonthAndYearFromDate.length());
             * Calendar cal = Calendar.getInstance();
             * cal.setTime(fromTransDate);
             * Integer monthInInt = cal.get(Calendar.MONTH)+1;
             * Integer year=cal.get(Calendar.YEAR);
             * gstr1period = monthInInt.toString()+year;
             * toTransDate =
             * MYSQLDF.parse(MYSQLDF.format(IDOSDF.parse(dateMonthAndYearToDate)));
             * }
             */
            String fromTransDate = null;
            String toTransDate = null;
            Date fromTransDate1 = null;
            Date toTransDate1 = null;
            String gstr1period = "";
            if (intervalType == 1) {

                String year = dateMonthAndYearForMonthWise.substring(dateMonthAndYearForMonthWise.length() - 4,
                        dateMonthAndYearForMonthWise.length());
                String month = dateMonthAndYearForMonthWise.substring(0, dateMonthAndYearForMonthWise.length() - 5);
                // January 1,2018
                String dateinjava = month + " 1," + year;

                fromTransDate1 = IdosConstants.MYSQLDF
                        .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateinjava)));
                fromTransDate = IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateinjava));
                Calendar cal = Calendar.getInstance();
                // Date toTransDate1=cal.getTime();
                cal.setTime(fromTransDate1);
                Integer monthInInt = cal.get(Calendar.MONTH) + 1;
                gstr1period = monthInInt.toString() + year;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                Date date = sdf.parse(year + "-" + (monthInInt < 10 ? ("0" + monthInInt) : monthInInt) + "-01");

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                calendar.add(Calendar.MONTH, 1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.DATE, -1);

                toTransDate = IdosConstants.MYSQLDF.format(calendar.getTime());
                toTransDate1 = IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(calendar.getTime()));
            }
            if (intervalType == 2) {
                String year = dateMonthAndYearForQuarterWise.substring(dateMonthAndYearForQuarterWise.length() - 4,
                        dateMonthAndYearForQuarterWise.length());
                String month = dateMonthAndYearForQuarterWise.substring(0, dateMonthAndYearForQuarterWise.length() - 5);
                // January 1,2018
                String dateinjava = month + " 1," + year;

                fromTransDate1 = IdosConstants.MYSQLDF
                        .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateinjava)));
                Calendar cal = Calendar.getInstance();
                cal.setTime(fromTransDate1);
                cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) / 3 * 3);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                fromTransDate = IdosConstants.MYSQLDF.format(cal.getTime());
                Integer monthInInt = cal.get(Calendar.MONTH) + 1;
                gstr1period = monthInInt.toString() + year;
                Calendar cal1 = Calendar.getInstance();
                cal1.setTime(fromTransDate1);
                cal1.set(Calendar.MONTH, cal.get(Calendar.MONTH) / 3 * 3 + 2);
                cal1.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                toTransDate = IdosConstants.MYSQLDF.format(cal1.getTime());
                toTransDate1 = IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(cal1.getTime()));
            }
            if (intervalType == 3) {
                fromTransDate = IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateMonthAndYearFromDate));
                fromTransDate1 = IdosConstants.MYSQLDF
                        .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateMonthAndYearFromDate)));
                toTransDate = IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateMonthAndYearToDate));
                toTransDate1 = IdosConstants.MYSQLDF
                        .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateMonthAndYearToDate)));
            }
            JSONArray outerArray1 = new JSONArray();
            JSONArray outerArray2 = new JSONArray();
            JSONArray outerArray3 = new JSONArray();
            JSONArray outerArray4 = new JSONArray();
            JSONArray outerArray5 = new JSONArray();
            JSONArray outerArray6 = new JSONArray();
            // JSONArray outerArray4= new JSONArray();
            JSONObject outerObject = new JSONObject();
            int i = 0;
            if (selectedValues.contains("7")) {
                String placeOfSuppliesQuery = null;
                ArrayList<Object> inparam1 = new ArrayList<Object>(1);
                if (fromTransDate1.equals(toTransDate1)) {
                    // placeOfSuppliesQuery = new StringBuilder("select distinct
                    // SUBSTRING(destinationGstin,1,2) from Transaction where
                    // transactionBranchOrganization.id="+user.getOrganization().getId()+" and
                    // transactionBranch.gstin='"+gstIn+"' and (transactionPurpose.id=1 or
                    // transactionPurpose.id=2) and invoiceValue<=250000 and typeOfSupply=1 and
                    // (walkinCustomerType=0 or walkinCustomerType=3 or walkinCustomerType=4 or
                    // walkinCustomerType=5 or walkinCustomerType=6) and
                    // substring(transactionDate,1,10)='"+fromTransDate+"' and
                    // transactionStatus='Accounted'");
                    placeOfSuppliesQuery = B2C_SMALLER_QUERY;
                    inparam1.add(user.getOrganization().getId());
                    inparam1.add(gstIn);
                    inparam1.add(fromTransDate1);
                    inparam1.add(0);
                } else {
                    placeOfSuppliesQuery = B2C_SMALLER_QUERY2;
                    inparam1.add(user.getOrganization().getId());
                    inparam1.add(gstIn);
                    inparam1.add(fromTransDate1);
                    inparam1.add(toTransDate1);
                    inparam1.add(0);
                }
                List<Object[]> placeOfSupplyList = genericDAO
                        .queryWithParamsNameGeneric(placeOfSuppliesQuery.toString(), entityManager, inparam1);

                if (placeOfSupplyList != null && placeOfSupplyList.size() > 0) {

                    JSONObject[] innerObjectForB2C = new JSONObject[placeOfSupplyList.size()];
                    for (Object placeOfSupply : placeOfSupplyList) {
                        innerObjectForB2C[i] = new JSONObject();
                        if (placeOfSupply.equals(gstIn.substring(0, 2))) {
                            innerObjectForB2C[i].put("sply_ty", "INTRA");
                        } else {
                            innerObjectForB2C[i].put("sply_ty", "INTER");
                        }
                        innerObjectForB2C[i].put("pos", placeOfSupply);
                        innerObjectForB2C[i].put("typ", "OE");
                        inparam1.clear();
                        inparam1.add(placeOfSupply);
                        inparam1.add(user.getOrganization().getId());
                        inparam1.add(gstIn);
                        inparam1.add(fromTransDate1);
                        inparam1.add(toTransDate1);
                        inparam1.add(0);
                        List<Object[]> quotationNetAmountListPrev1 = genericDAO
                                .queryWithParamsNameGeneric(B2C_SMALLER_QUERY3.toString(), entityManager, inparam1);
                        List<Object[]> quotationNetAmountListPrevDebit = genericDAO.queryWithParamsNameGeneric(
                                B2C_SMALLER_QUERY_DEBIT.toString(), entityManager, inparam1);
                        int j = 0;
                        JSONArray innerArray = new JSONArray();
                        JSONObject[] innerObjectForItems = new JSONObject[quotationNetAmountListPrev1.size()];
                        for (Object[] custData : quotationNetAmountListPrev1) {
                            Transaction isCancelled = Transaction.findCancelledSellInvoice(entityManager,
                                    user.getOrganization().getId(), custData[8].toString());
                            if (isCancelled == null) {
                                Double rate1 = 0.0;
                                Double rate2 = 0.0;
                                Double rate3 = 0.0;
                                Double rate = 0.0;
                                innerObjectForItems[j] = new JSONObject();
                                if (custData[6] != null) {
                                    if (quotationNetAmountListPrevDebit.size() > j
                                            && quotationNetAmountListPrevDebit.get(j)[6] != null)
                                        innerObjectForItems[j].put("txval",
                                                Double.parseDouble(String.valueOf(custData[6])) - Double.parseDouble(
                                                        String.valueOf(quotationNetAmountListPrevDebit.get(j)[6])));
                                    else
                                        innerObjectForItems[j].put("txval",
                                                Double.parseDouble(String.valueOf(custData[6])));
                                } else {
                                    innerObjectForItems[j].put("txval", "");
                                }
                                if (custData[0] != null) {
                                    rate1 = Double.parseDouble(String.valueOf(custData[0]));
                                }
                                if (custData[1] != null) {
                                    rate2 = Double.parseDouble(String.valueOf(custData[1]));
                                }
                                if (custData[2] != null) {
                                    rate3 = Double.parseDouble(String.valueOf(custData[2]));
                                }
                                rate = rate1 + rate2 + rate3;
                                innerObjectForItems[j].put("rt", rate);
                                if (custData[4] != null) {
                                    if (quotationNetAmountListPrevDebit.size() > j
                                            && quotationNetAmountListPrevDebit.get(j)[4] != null)
                                        innerObjectForItems[j].put("camt",
                                                Double.parseDouble(String.valueOf(custData[4])) - Double.parseDouble(
                                                        String.valueOf(quotationNetAmountListPrevDebit.get(j)[4])));
                                    else
                                        innerObjectForItems[j].put("camt",
                                                Double.parseDouble(String.valueOf(custData[4])));
                                } else {
                                    innerObjectForItems[j].put("camt", "");
                                }
                                if (custData[3] != null) {
                                    if (quotationNetAmountListPrevDebit.size() > j
                                            && quotationNetAmountListPrevDebit.get(j)[3] != null)
                                        innerObjectForItems[j].put("samt",
                                                Double.parseDouble(String.valueOf(custData[3])) - Double.parseDouble(
                                                        String.valueOf(quotationNetAmountListPrevDebit.get(j)[3])));
                                    else
                                        innerObjectForItems[j].put("samt",
                                                Double.parseDouble(String.valueOf(custData[3])));
                                } else {
                                    innerObjectForItems[j].put("samt", "");
                                }
                                if (custData[5] != null) {
                                    if (quotationNetAmountListPrevDebit.size() > j
                                            && quotationNetAmountListPrevDebit.get(j)[5] != null)
                                        innerObjectForItems[j].put("iamt",
                                                Double.parseDouble(String.valueOf(custData[5])) - Double.parseDouble(
                                                        String.valueOf(quotationNetAmountListPrevDebit.get(j)[5])));
                                    else
                                        innerObjectForItems[j].put("iamt",
                                                Double.parseDouble(String.valueOf(custData[5])));
                                } else {
                                    innerObjectForItems[j].put("iamt", "");
                                }
                                if (custData[7] != null) {
                                    if (quotationNetAmountListPrevDebit.size() > j
                                            && quotationNetAmountListPrevDebit.get(j)[7] != null)
                                        innerObjectForItems[j].put("csamt",
                                                Double.parseDouble(String.valueOf(custData[7])) - Double.parseDouble(
                                                        String.valueOf(quotationNetAmountListPrevDebit.get(j)[7])));
                                    else
                                        innerObjectForItems[j].put("csamt",
                                                Double.parseDouble(String.valueOf(custData[7])));
                                } else {
                                    innerObjectForItems[j].put("csamt", "");
                                }
                                innerArray.put(innerObjectForItems[j]);
                                j++;
                            }
                        }
                        outerArray1.put(innerObjectForB2C[i]);
                        outerArray1.put(innerArray);
                        i++;
                    }

                    outerObject.put("b2cs", outerArray1);

                }
            }
            i = 0;

            if (selectedValues.contains("8")) {

                String placeOfSuppliesQuery = null;
                ArrayList<Object> inparam1 = new ArrayList<Object>(4);
                if (fromTransDate1.equals(toTransDate1)) {
                    placeOfSuppliesQuery = ADVANCES_RECEIVED_QUERY1;
                    inparam1.add(user.getOrganization().getId());
                    inparam1.add(gstIn);
                    inparam1.add(fromTransDate1);
                } else {
                    placeOfSuppliesQuery = ADVANCES_RECEIVED_QUERY2;
                    inparam1.add(user.getOrganization().getId());
                    inparam1.add(gstIn);
                    inparam1.add(fromTransDate1);
                    inparam1.add(toTransDate1);
                }
                List<Object[]> placeOfSupplyList = genericDAO
                        .queryWithParamsNameGeneric(placeOfSuppliesQuery.toString(), entityManager, inparam1);
                log.log(Level.INFO, "size of query" + placeOfSupplyList.size());
                if (placeOfSupplyList != null && placeOfSupplyList.size() > 0) {

                    JSONObject[] innerObjectForAdvanceReceived = new JSONObject[placeOfSupplyList.size()];
                    for (Object placeOfSupply : placeOfSupplyList) {
                        innerObjectForAdvanceReceived[i] = new JSONObject();

                        innerObjectForAdvanceReceived[i].put("pos", placeOfSupply);

                        inparam1.clear();
                        inparam1.add(placeOfSupply);
                        inparam1.add(user.getOrganization().getId());
                        inparam1.add(gstIn);
                        inparam1.add(fromTransDate1);
                        inparam1.add(toTransDate1);
                        List<Object[]> quotationNetAmountListPrev1 = genericDAO.queryWithParamsNameGeneric(
                                ADVANCES_RECEIVED_QUERY3.toString(), entityManager, inparam1);
                        int j = 0;
                        JSONArray innerArray = new JSONArray();
                        JSONObject[] innerObjectForItems = new JSONObject[quotationNetAmountListPrev1.size()];
                        for (Object[] custData : quotationNetAmountListPrev1) {
                            Double rate1 = 0.0;
                            Double rate2 = 0.0;
                            Double rate3 = 0.0;
                            Double rate = 0.0;
                            innerObjectForItems[j] = new JSONObject();

                            if (custData[0] != null) {
                                rate1 = Double.parseDouble(String.valueOf(custData[0]));
                            }
                            if (custData[1] != null) {
                                rate2 = Double.parseDouble(String.valueOf(custData[1]));
                            }
                            if (custData[2] != null) {
                                rate3 = Double.parseDouble(String.valueOf(custData[2]));
                            }
                            rate = rate1 + rate2 + rate3;
                            innerObjectForItems[j].put("rt", rate);
                            if (custData[6] != null) {

                                innerObjectForItems[j].put("ad_amt", Double.parseDouble(String.valueOf(custData[6])));
                            } else {
                                innerObjectForItems[j].put("ad_amt", "");
                            }
                            if (custData[4] != null) {
                                innerObjectForItems[j].put("camt", Double.parseDouble(String.valueOf(custData[4])));
                            }
                            if (custData[3] != null) {
                                innerObjectForItems[j].put("samt", Double.parseDouble(String.valueOf(custData[3])));
                            }
                            if (custData[5] != null) {
                                innerObjectForItems[j].put("iamt", Double.parseDouble(String.valueOf(custData[5])));
                            }
                            if (custData[7] != null) {
                                innerObjectForItems[j].put("csamt", Double.parseDouble(String.valueOf(custData[7])));
                            } else {
                                innerObjectForItems[j].put("csamt", "");
                            }
                            innerArray.put(innerObjectForItems[j]);
                            j++;
                        }
                        if (placeOfSupply.equals(gstIn.substring(0, 2))) {
                            innerObjectForAdvanceReceived[i].put("sply_ty", "INTRA");
                        } else {
                            innerObjectForAdvanceReceived[i].put("sply_ty", "INTER");
                        }
                        innerObjectForAdvanceReceived[i].put("itms", innerArray);
                        outerArray2.put(innerObjectForAdvanceReceived[i]);
                        i++;
                    }

                    outerObject.put("at", outerArray2);

                }
            }
            i = 0;
            if (selectedValues.contains("9")) {
                String placeOfSuppliesQuery = null;
                ArrayList<Object> inparam1 = new ArrayList<Object>(4);
                if (fromTransDate.equals(toTransDate)) {
                    placeOfSuppliesQuery = ADVANCE_ADJ_QUERY1;
                    inparam1.add(user.getOrganization().getId());
                    inparam1.add(gstIn);
                    inparam1.add(fromTransDate1);
                } else {
                    placeOfSuppliesQuery = ADVANCE_ADJ_QUERY2;
                    inparam1.add(user.getOrganization().getId());
                    inparam1.add(gstIn);
                    inparam1.add(fromTransDate1);
                    inparam1.add(toTransDate1);
                }
                List<Object[]> placeOfSupplyList = genericDAO
                        .queryWithParamsNameGeneric(placeOfSuppliesQuery.toString(), entityManager, inparam1);
                if (placeOfSupplyList != null && placeOfSupplyList.size() > 0) {

                    JSONObject[] innerObjectForAdvanceAdjusted = new JSONObject[placeOfSupplyList.size()];
                    for (Object placeOfSupply : placeOfSupplyList) {
                        innerObjectForAdvanceAdjusted[i] = new JSONObject();

                        innerObjectForAdvanceAdjusted[i].put("pos", placeOfSupply);

                        inparam1.clear();
                        inparam1.add(placeOfSupply);
                        inparam1.add(user.getOrganization().getId());
                        inparam1.add(gstIn);
                        inparam1.add(fromTransDate1);
                        inparam1.add(toTransDate1);
                        List<Object[]> quotationNetAmountListPrev1 = genericDAO
                                .queryWithParamsNameGeneric(ADVANCE_ADJ_QUERY3.toString(), entityManager, inparam1);
                        int j = 0;
                        JSONArray innerArray = new JSONArray();
                        JSONObject[] innerObjectForItems = new JSONObject[quotationNetAmountListPrev1.size()];
                        for (Object[] custData : quotationNetAmountListPrev1) {
                            Transaction isCancelled = Transaction.findCancelledSellInvoice(entityManager,
                                    user.getOrganization().getId(), custData[8].toString());
                            if (isCancelled == null) {
                                Double rate1 = 0.0;
                                Double rate2 = 0.0;
                                Double rate3 = 0.0;
                                Double rate = 0.0;
                                innerObjectForItems[j] = new JSONObject();

                                if (custData[0] != null) {
                                    rate1 = Double.parseDouble(String.valueOf(custData[0]));
                                }
                                if (custData[1] != null) {
                                    rate2 = Double.parseDouble(String.valueOf(custData[1]));
                                }
                                if (custData[2] != null) {
                                    rate3 = Double.parseDouble(String.valueOf(custData[2]));
                                }
                                rate = rate1 + rate2 + rate3;
                                innerObjectForItems[j].put("rt", rate);
                                if (custData[6] != null) {

                                    innerObjectForItems[j].put("ad_amt",
                                            Double.parseDouble(String.valueOf(custData[6])));
                                } else {
                                    innerObjectForItems[j].put("ad_amt", "");
                                }
                                if (custData[4] != null) {
                                    innerObjectForItems[j].put("camt", Double.parseDouble(String.valueOf(custData[4])));
                                }
                                if (custData[3] != null) {
                                    innerObjectForItems[j].put("samt", Double.parseDouble(String.valueOf(custData[3])));
                                }
                                if (custData[5] != null) {
                                    innerObjectForItems[j].put("iamt", Double.parseDouble(String.valueOf(custData[5])));
                                }
                                if (custData[7] != null) {
                                    innerObjectForItems[j].put("csamt",
                                            Double.parseDouble(String.valueOf(custData[7])));
                                } else {
                                    innerObjectForItems[j].put("csamt", "");
                                }
                                innerArray.put(innerObjectForItems[j]);
                                j++;
                            }
                        }
                        if (placeOfSupply.equals(gstIn.substring(0, 2))) {
                            innerObjectForAdvanceAdjusted[i].put("sply_ty", "INTRA");
                        } else {
                            innerObjectForAdvanceAdjusted[i].put("sply_ty", "INTER");
                        }
                        innerObjectForAdvanceAdjusted[i].put("itms", innerArray);
                        outerArray3.put(innerObjectForAdvanceAdjusted[i]);
                        i++;

                    }
                    outerObject.put("txpd", outerArray3);

                }
            }
            i = 0;
            if (selectedValues.contains("10")) {

                // StringBuilder placeOfSupplyQuery= new StringBuilder("select distinct
                // SUBSTRING(destinationGstin,1,2) from Transaction where
                // transactionBranchOrganization.id="+user.getOrganization().getId()+"and
                // transactionBranch.gstin='"+gstIn+"'and transactionPurpose.id=6 and
                // transactionDate>='2018-07-09' and invoiceValue<=250000 and typeOfSupply=1 and
                // transactionVendorCustomer.isRegistered=0 and transactionDate between
                // '"+fromTransDate+"' and '"+toTransDate+"'");

                /*
                 * StringBuilder hsnQuery = new
                 * StringBuilder("select distinct obj.transactionSpecifics.gstItemCode,  obj.transactionSpecifics.gstDesc from  TransactionItems obj where obj.organization.id="
                 * +user.getOrganization().getId()+" and obj.branch.gstin='"
                 * +gstIn+"'and  obj.transaction.transactionPurpose.id in(1,2,6,30,31,25,35) and obj.transaction.transactionDate>='"
                 * +fromTransDate+"' and obj.transaction.transactionDate<='"+toTransDate+"'");
                 * 
                 * //List<String> hsnList =
                 * entityManager.createQuery(hsnQuery.toString()).getResultList();
                 * List<Object[]> hsnList =
                 * entityManager.createQuery(hsnQuery.toString()).getResultList();
                 */
                /*
                 * StringBuilder hsnQuery = new
                 * StringBuilder("select distinct obj.transactionSpecifics.gstItemCode, obj.transactionSpecifics.gstDesc from  TransactionItems obj where obj.organization.id="
                 * +user.getOrganization().getId()+" and obj.branch.gstin='"
                 * +gstIn+"'and  obj.transaction.transactionPurpose.id in(1,2,6,30,31,25,35) and obj.transaction.transactionDate>='"
                 * +fromTransDate+"' and obj.transaction.transactionDate<='"+toTransDate+"'");
                 * log.log(Level.INFO, "hsn query="+hsnQuery.toString());
                 * List<Object[]> hsnList =
                 * entityManager.createQuery(hsnQuery.toString()).getResultList();
                 */
                ArrayList<Object> inparam1 = new ArrayList<Object>(5);
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                List<Object[]> hsnList = genericDAO.queryWithParamsNameGeneric(HSN_QUERY.toString(), entityManager,
                        inparam1);
                if (hsnList != null && hsnList.size() > 0) {

                    JSONObject[] innerObjectForHsnWise = new JSONObject[hsnList.size()];
                    for (Object[] hsn : hsnList) {

                        innerObjectForHsnWise[i] = new JSONObject();
                        innerObjectForHsnWise[i].put("data__num", i + 1);

                        innerObjectForHsnWise[i].put("data__hsn_sc", hsn[0]);
                        if (hsn[1] == null && hsn[1] != "") {
                            innerObjectForHsnWise[i].put("data__desc", "");
                        } else {
                            innerObjectForHsnWise[i].put("data__desc", hsn[1]);
                        }
                        // innerObjectForHsnWise[i].put("data__hsn_sc",hsn[0]);
                        /*
                         * StringBuilder hsndescquery = new
                         * StringBuilder("select obj.invoiceItemDescription1 from Specifics obj where obj.gstItemCode="
                         * +hsn);
                         * List<String> hsndesclist =
                         * entityManager.createQuery(hsndescquery.toString()).getResultList();
                         * String description="";
                         * if(hsndesclist!=null&&hsndesclist.size()>0){
                         * for(String desc:hsndesclist){
                         * description=description.concat(desc);
                         * }
                         * }
                         */
                        JSONArray innerArray = new JSONArray();

                        // innerObjectForHsnWise[i].put("data__desc","");

                        int j = 0;

                        StringBuilder transactionDataQuery = new StringBuilder(
                                "select obj.transactionSpecifics.incomeUnitsMeasure,sum(obj.noOfUnits),sum(obj.totalTax),sum(obj.grossAmount),sum(obj.taxValue1),sum(obj.taxValue2),sum(obj.taxValue3),sum(obj.taxValue4) from TransactionItems obj where obj.transactionSpecifics.gstItemCode='"
                                        + hsn[0] + "' and obj.transaction.transactionBranchOrganization.id="
                                        + user.getOrganization().getId()
                                        + " and obj.transaction.transactionBranch.gstin='" + gstIn
                                        + "' and  obj.transaction.transactionPurpose.id in(1,2,6,31,25) and obj.transaction.transactionDate>='"
                                        + fromTransDate + "' and obj.transaction.transactionDate<='" + toTransDate
                                        + "' and obj.transaction.transactionStatus='Accounted' group by obj.transactionSpecifics.incomeUnitsMeasure");
                        inparam1.clear();
                        inparam1.add(hsn[0]);
                        inparam1.add(user.getOrganization().getId());
                        inparam1.add(gstIn);
                        inparam1.add(fromTransDate1);
                        inparam1.add(toTransDate1);
                        List<Object[]> transactionData = genericDAO
                                .queryWithParamsNameGeneric(HSN_DATA_QUERY1.toString(), entityManager, inparam1);

                        JSONObject[] innerObjectForUqc = new JSONObject[transactionData.size()];
                        inparam1.clear();
                        inparam1.add(hsn[0]);
                        inparam1.add(user.getOrganization().getId());
                        inparam1.add(gstIn);
                        inparam1.add(fromTransDate1);
                        inparam1.add(toTransDate1);
                        StringBuilder transactionDataQuery2 = new StringBuilder(
                                "select obj.transactionSpecifics.incomeUnitsMeasure,sum(obj.noOfUnits),sum(obj.totalTax),sum(obj.grossAmount),sum(obj.taxValue1),sum(obj.taxValue2),sum(obj.taxValue3),sum(obj.taxValue4) from TransactionItems obj where obj.transactionSpecifics.gstItemCode='"
                                        + hsn[0] + "' and obj.transaction.transactionBranchOrganization.id="
                                        + user.getOrganization().getId()
                                        + " and obj.transaction.transactionBranch.gstin='" + gstIn
                                        + "' and  obj.transaction.transactionPurpose.id in(30,35) and obj.transaction.transactionDate>='"
                                        + fromTransDate + "' and obj.transaction.transactionDate<='" + toTransDate
                                        + "' and obj.transaction.transactionStatus='Accounted' group by obj.transactionSpecifics.incomeUnitsMeasure");
                        List<Object[]> transactionData2 = genericDAO
                                .queryWithParamsNameGeneric(HSN_DATA_QUERY2.toString(), entityManager, inparam1);
                        StringBuilder transactionDataQueryForUnits = new StringBuilder(
                                "select sum(obj.noOfUnits) from TransactionItems obj where obj.transactionSpecifics.gstItemCode='"
                                        + hsn[0] + "' and obj.transaction.transactionBranchOrganization.id="
                                        + user.getOrganization().getId()
                                        + " and obj.transaction.transactionBranch.gstin='" + gstIn
                                        + "' and  obj.transaction.transactionPurpose.id in(30,35) and obj.transaction.transactionDate>='"
                                        + fromTransDate + "' and obj.transaction.transactionDate<='" + toTransDate
                                        + "' and obj.transaction.typeIdentifier=2 and obj.transaction.transactionStatus='Accounted' group by obj.transactionSpecifics.incomeUnitsMeasure");
                        inparam1.clear();
                        inparam1.add(hsn[0]);
                        inparam1.add(user.getOrganization().getId());
                        inparam1.add(gstIn);
                        inparam1.add(fromTransDate1);
                        inparam1.add(toTransDate1);
                        List<Object[]> transactionDataForUnits = genericDAO
                                .queryWithParamsNameGeneric(HSN_UNITS_QUERY.toString(), entityManager, inparam1);
                        JSONObject[] innerObjectForUqc2 = new JSONObject[transactionData2.size()];
                        if (transactionData != null && transactionData.size() > 0) {
                            for (Object[] transaction : transactionData) {
                                Transaction isCancelledTransaction = Transaction.findCancelledSellInvoice(entityManager,
                                        user.getOrganization().getId(), transaction[8].toString());
                                if (isCancelledTransaction == null) {
                                    innerObjectForUqc[j] = new JSONObject();
                                    if (transaction[0] != null) {
                                        innerObjectForUqc[j].put("uqc", String.valueOf(transaction[0]));
                                    }
                                    if (transaction[1] != null) {
                                        if (transactionDataForUnits.toString() != ""
                                                && transactionDataForUnits.size() > 0) {
                                            innerObjectForUqc[j].put("qty",
                                                    Double.parseDouble(String.valueOf(transaction[1]))
                                                            - Double.parseDouble(
                                                                    String.valueOf(transactionDataForUnits.get(j))));
                                        } else
                                            innerObjectForUqc[j].put("qty",
                                                    Double.parseDouble(String.valueOf(transaction[1])));
                                    }
                                    Double value1 = 0.0;
                                    Double value2 = 0.0;
                                    if (transaction[2] != null && transaction[3] != null) {
                                        value1 = Double.parseDouble(String.valueOf(transaction[2]))
                                                + Double.parseDouble(String.valueOf(transaction[3]));

                                    } else if (transaction[3] != null) {
                                        value1 = Double.parseDouble(String.valueOf(transaction[3]));
                                    }
                                    Double taxvalue1 = 0.0;
                                    Double taxvalue2 = 0.0;
                                    if (transactionData2.toString() != "" && transactionData2.size() > 0) {
                                        Transaction isCancelledTransaction2 = Transaction.findCancelledSellInvoice(
                                                entityManager, user.getOrganization().getId(),
                                                transactionData2.get(j)[8].toString());
                                        if (isCancelledTransaction2 == null) {
                                            if (transactionData2.get(j)[2] != null
                                                    && transactionData2.get(j)[3] != null) {
                                                value2 = Double.parseDouble(String.valueOf(transactionData2.get(j)[2]))
                                                        + Double.parseDouble(
                                                                String.valueOf(transactionData2.get(j)[3]));
                                            } else if (transactionData2.get(j)[3] != null) {
                                                value2 = Double.parseDouble(String.valueOf(transactionData2.get(j)[3]));
                                            }

                                            if (transactionData2 != null && transactionData2.size() > 0) {
                                                if (transactionData2.get(j)[2] != null) {
                                                    taxvalue2 = Double
                                                            .parseDouble(String.valueOf(transactionData2.get(j)[2]));
                                                }
                                            }
                                        }
                                    }
                                    Double value = value1 - value2;
                                    /*
                                     * if(transaction[2] != null && transaction[3]!=null){
                                     * innerObjectForUqc[j].put("data_val",Double.parseDouble(String.valueOf(
                                     * transaction[2]))+Double.parseDouble(String.valueOf(transaction[3])));
                                     * }else if(transaction[3]!=null){
                                     * innerObjectForUqc[j].put("data_val",Double.parseDouble(String.valueOf(
                                     * transaction[3])));
                                     * }
                                     */

                                    if (transaction[2] != null) {
                                        taxvalue1 = Double.parseDouble(String.valueOf(transaction[2]));
                                    }

                                    Double taxvalue = taxvalue1 - taxvalue2;
                                    innerObjectForUqc[j].put("val", value);

                                    if (transaction[3] != null) {
                                        innerObjectForUqc[j].put("txval", taxvalue);
                                    }
                                    if (transaction[6] != null) {
                                        innerObjectForUqc[j].put("iamt",
                                                Double.parseDouble(String.valueOf(transaction[6])));
                                    }
                                    if (transaction[5] != null) {
                                        innerObjectForUqc[j].put("camt",
                                                Double.parseDouble(String.valueOf(transaction[5])));
                                    }
                                    if (transaction[4] != null) {
                                        innerObjectForUqc[j].put("samt",
                                                Double.parseDouble(String.valueOf(transaction[4])));
                                    }
                                    if (transaction[7] != null) {
                                        innerObjectForUqc[j].put("csamt",
                                                Double.parseDouble(String.valueOf(transaction[7])));
                                    }

                                    innerArray.put(innerObjectForUqc[j]);
                                    j++;
                                }

                            }
                        }
                        innerObjectForHsnWise[i].put("data", innerArray);
                        outerArray4.put(innerObjectForHsnWise[i]);
                        i++;
                    }

                    outerObject.put("hsn", outerArray4);

                }

            }

            if (selectedValues.contains("11")) {
                ArrayList<Object> inparam1 = new ArrayList<Object>(8);
                JSONObject[] innerObjectForNillRatedSupply = new JSONObject[4];
                JSONArray innerArray = new JSONArray();

                JSONObject[] innerObjectForItems1 = new JSONObject[4];
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                inparam1.add("1");
                inparam1.add(1);
                List<Object[]> b2bAmount1Array = genericDAO.queryWithParamsNameGeneric(B2B_QUERY1.toString(),
                        entityManager, inparam1);
                Double b2bAmount1Val = 0.0;

                for (Object[] txn : b2bAmount1Array) {
                    Transaction isCancelled = Transaction.findCancelledSellInvoice(entityManager,
                            user.getOrganization().getId(), txn[1].toString());
                    if (isCancelled == null)
                        b2bAmount1Val += Double.parseDouble(txn[0].toString());
                }

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                inparam1.add("2");
                inparam1.add(1);
                List<Object[]> b2bAmount2Array = genericDAO.queryWithParamsNameGeneric(B2B_QUERY1.toString(),
                        entityManager, inparam1);
                Double b2bAmount2Val = 0.0;

                for (Object[] txn : b2bAmount2Array) {
                    Transaction isCancelled = Transaction.findCancelledSellInvoice(entityManager,
                            user.getOrganization().getId(), txn[1].toString());
                    if (isCancelled == null)
                        b2bAmount2Val += Double.parseDouble(txn[0].toString());
                }

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                inparam1.add("3");
                inparam1.add(1);
                List<Object[]> b2bAmount3Array = genericDAO.queryWithParamsNameGeneric(B2B_QUERY1.toString(),
                        entityManager, inparam1);

                Double b2bAmount3Val = 0.0;

                for (Object[] txn : b2bAmount3Array) {
                    Transaction isCancelled = Transaction.findCancelledSellInvoice(entityManager,
                            user.getOrganization().getId(), txn[1].toString());
                    if (isCancelled == null)
                        b2bAmount3Val += Double.parseDouble(txn[0].toString());
                }

                innerObjectForItems1[0] = new JSONObject();
                innerObjectForItems1[0].put("sply_ty", "INTRAB2B");
                innerObjectForItems1[0].put("expt_amt", IdosConstants.decimalFormat.format(b2bAmount1Val));
                innerObjectForItems1[0].put("nil_amt", IdosConstants.decimalFormat.format(b2bAmount2Val));
                innerObjectForItems1[0].put("ngsup_amt", IdosConstants.decimalFormat.format(b2bAmount3Val));
                innerArray.put(innerObjectForItems1[0]);

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                inparam1.add("1");
                inparam1.add(1);
                List<Object[]> b2bAmount4Array = genericDAO.queryWithParamsNameGeneric(B2B_QUERY2.toString(),
                        entityManager, inparam1);
                Double b2bAmount4Val = 0.0;

                for (Object[] txn : b2bAmount4Array) {
                    Transaction isCancelled = Transaction.findCancelledSellInvoice(entityManager,
                            user.getOrganization().getId(), txn[1].toString());
                    if (isCancelled == null)
                        b2bAmount4Val += Double.parseDouble(txn[0].toString());
                }

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                inparam1.add("2");
                inparam1.add(1);
                List<Object[]> b2bAmount5Array = genericDAO.queryWithParamsNameGeneric(B2B_QUERY2.toString(),
                        entityManager, inparam1);
                Double b2bAmount5Val = 0.0;

                for (Object[] txn : b2bAmount5Array) {
                    Transaction isCancelled = Transaction.findCancelledSellInvoice(entityManager,
                            user.getOrganization().getId(), txn[1].toString());
                    if (isCancelled == null)
                        b2bAmount5Val += Double.parseDouble(txn[0].toString());
                }

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                inparam1.add("3");
                inparam1.add(1);
                List<Object[]> b2bAmount6Array = genericDAO.queryWithParamsNameGeneric(B2B_QUERY2.toString(),
                        entityManager, inparam1);
                Double b2bAmount6Val = 0.0;

                for (Object[] txn : b2bAmount6Array) {
                    Transaction isCancelled = Transaction.findCancelledSellInvoice(entityManager,
                            user.getOrganization().getId(), txn[1].toString());
                    if (isCancelled == null)
                        b2bAmount6Val += Double.parseDouble(txn[0].toString());
                }

                innerObjectForItems1[1] = new JSONObject();
                innerObjectForItems1[1].put("sply_ty", "INTRB2B");
                innerObjectForItems1[1].put("expt_amt", IdosConstants.decimalFormat.format(b2bAmount4Val));
                innerObjectForItems1[1].put("nil_amt", IdosConstants.decimalFormat.format(b2bAmount5Val));
                innerObjectForItems1[1].put("ngsup_amt", IdosConstants.decimalFormat.format(b2bAmount6Val));
                innerArray.put(innerObjectForItems1[1]);

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                inparam1.add("1");
                inparam1.add(0);
                List<Object[]> b2cAmount1Array = genericDAO.queryWithParamsNameGeneric(B2C_QUERY1.toString(),
                        entityManager, inparam1);
                Double b2cAmount1Val = 0.0;

                for (Object[] txn : b2cAmount1Array) {
                    Transaction isCancelled = Transaction.findCancelledSellInvoice(entityManager,
                            user.getOrganization().getId(), txn[1].toString());
                    if (isCancelled == null)
                        b2cAmount1Val += Double.parseDouble(txn[0].toString());
                }

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                inparam1.add("2");
                inparam1.add(0);
                List<Object[]> b2cAmount2Array = genericDAO.queryWithParamsNameGeneric(B2C_QUERY1.toString(),
                        entityManager, inparam1);
                Double b2cAmount2Val = 0.0;

                for (Object[] txn : b2cAmount2Array) {
                    Transaction isCancelled = Transaction.findCancelledSellInvoice(entityManager,
                            user.getOrganization().getId(), txn[1].toString());
                    if (isCancelled == null)
                        b2cAmount2Val += Double.parseDouble(txn[0].toString());
                }

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                inparam1.add("3");
                inparam1.add(0);
                List<Object[]> b2cAmount3Array = genericDAO.queryWithParamsNameGeneric(B2C_QUERY1.toString(),
                        entityManager, inparam1);
                Double b2cAmount3Val = 0.0;

                for (Object[] txn : b2cAmount3Array) {
                    Transaction isCancelled = Transaction.findCancelledSellInvoice(entityManager,
                            user.getOrganization().getId(), txn[1].toString());
                    if (isCancelled == null)
                        b2cAmount3Val += Double.parseDouble(txn[0].toString());
                }

                innerObjectForItems1[2] = new JSONObject();
                innerObjectForItems1[2].put("sply_ty", "INTRAB2C");
                innerObjectForItems1[2].put("expt_amt", IdosConstants.decimalFormat.format(b2cAmount3Val));
                innerObjectForItems1[2].put("nil_amt", IdosConstants.decimalFormat.format(b2cAmount3Val));
                innerObjectForItems1[2].put("ngsup_amt", IdosConstants.decimalFormat.format(b2cAmount3Val));
                innerArray.put(innerObjectForItems1[2]);

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                inparam1.add("1");
                inparam1.add(0);
                List<Object[]> b2cAmount4Array = genericDAO.queryWithParamsNameGeneric(B2C_QUERY2.toString(),
                        entityManager, inparam1);
                Double b2cAmount4Val = 0.0;

                for (Object[] txn : b2cAmount4Array) {
                    Transaction isCancelled = Transaction.findCancelledSellInvoice(entityManager,
                            user.getOrganization().getId(), txn[1].toString());
                    if (isCancelled == null)
                        b2cAmount4Val += Double.parseDouble(txn[0].toString());
                }

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                inparam1.add("2");
                inparam1.add(0);
                List<Object[]> b2cAmount5Array = genericDAO.queryWithParamsNameGeneric(B2C_QUERY2.toString(),
                        entityManager, inparam1);
                Double b2cAmount5Val = 0.0;
                for (Object[] txn : b2cAmount5Array) {
                    Transaction isCancelled = Transaction.findCancelledSellInvoice(entityManager,
                            user.getOrganization().getId(), txn[1].toString());
                    if (isCancelled == null)
                        b2cAmount5Val += Double.parseDouble(txn[0].toString());
                }

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                inparam1.add("3");
                inparam1.add(0);
                List<Object[]> b2cAmount6Array = genericDAO.queryWithParamsNameGeneric(B2C_QUERY2.toString(),
                        entityManager, inparam1);
                Double b2cAmount6Val = 0.0;
                for (Object[] txn : b2cAmount6Array) {
                    Transaction isCancelled = Transaction.findCancelledSellInvoice(entityManager,
                            user.getOrganization().getId(), txn[1].toString());
                    if (isCancelled == null)
                        b2cAmount6Val += Double.parseDouble(txn[0].toString());
                }

                innerObjectForItems1[3] = new JSONObject();
                innerObjectForItems1[3].put("sply_ty", "INTRB2C");
                innerObjectForItems1[3].put("expt_amt", IdosConstants.decimalFormat.format(b2cAmount4Val));
                innerObjectForItems1[3].put("nil_amt", IdosConstants.decimalFormat.format(b2cAmount5Val));
                innerObjectForItems1[3].put("ngsup_amt", IdosConstants.decimalFormat.format(b2cAmount6Val));
                innerArray.put(innerObjectForItems1[3]);

                innerObjectForNillRatedSupply[0] = new JSONObject();
                innerObjectForNillRatedSupply[0].put("inv", innerArray);
                outerArray5.put(innerObjectForNillRatedSupply[0]);
                outerObject.put("nil", outerArray5);
            }
            i = 0;
            if (selectedValues.contains("12")) {
                ArrayList<Object> inparam1 = new ArrayList<Object>(8);
                JSONObject[] innerObjectForDocsIssued = new JSONObject[6];
                int noOfCancelledTxns = 0;
                JSONObject[] innerObjectForItems1 = new JSONObject[8];
                innerObjectForItems1[0] = new JSONObject();
                innerObjectForItems1[1] = new JSONObject();
                innerObjectForItems1[2] = new JSONObject();
                innerObjectForItems1[3] = new JSONObject();
                innerObjectForItems1[4] = new JSONObject();
                innerObjectForItems1[5] = new JSONObject();
                innerObjectForItems1[6] = new JSONObject();
                innerObjectForItems1[7] = new JSONObject();

                String invoiceNumber1 = "";

                String invoiceNumber2 = "";

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(toTransDate1);
                List<Transaction> transactions = genericDAO.queryWithParamsName(DOCUMENT_QUERY1.toString(),
                        entityManager, inparam1);
                if (transactions.size() > 0) {
                    Transaction isCancelled = Transaction.findCancelledSellInvoice(entityManager,
                            user.getOrganization().getId(), transactions.get(0).getTransactionRefNumber());
                    if (isCancelled == null)
                        invoiceNumber1 = transactions.get(0).getInvoiceNumber();
                    else
                        noOfCancelledTxns++;
                }
                JSONArray innerArray = new JSONArray();

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(fromTransDate1);
                List<Transaction> transactions2 = genericDAO.queryWithParamsName(DOCUMENT_QUERY2.toString(),
                        entityManager, inparam1);
                if (transactions2.size() > 0) {
                    Transaction isCancelled = Transaction.findCancelledSellInvoice(entityManager,
                            user.getOrganization().getId(), transactions2.get(0).getTransactionRefNumber());
                    if (isCancelled == null)
                        invoiceNumber2 = transactions2.get(0).getInvoiceNumber();
                    else
                        noOfCancelledTxns++;
                }

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                List<Object[]> numberOfInvoices = genericDAO.queryWithParamsNameGeneric(DOCUMENT_QUERY3.toString(),
                        entityManager, inparam1);
                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                List<Object[]> cancelledInvoicesNo = genericDAO
                        .queryWithParamsNameGeneric(CANCELLED_INVOICES_QUERY.toString(), entityManager, inparam1);

                innerObjectForItems1[0].put("doc_det__doc_typ", "Invoices for outward supply");
                innerObjectForItems1[0].put("doc_det__docs__num", 1);
                innerObjectForItems1[0].put("doc_det__docs__from", invoiceNumber2);
                innerObjectForItems1[0].put("doc_det__docs__to", invoiceNumber1);
                if (numberOfInvoices.size() > 0) {
                    innerObjectForItems1[0].put("doc_det__docs__totnum", numberOfInvoices.get(0));
                } else {
                    innerObjectForItems1[0].put("doc_det__docs__totnum", "");
                }
                innerObjectForItems1[0].put("doc_det__docs__cancel", noOfCancelledTxns);
                if (numberOfInvoices.size() > 0) {
                    innerObjectForItems1[0].put("doc_det__docs__net_issue",
                            Long.parseLong(String.valueOf(numberOfInvoices.get(0)))
                                    - Long.parseLong(String.valueOf(cancelledInvoicesNo.get(0))));
                } else {
                    innerObjectForItems1[0].put("doc_det__docs__net_issue", "");
                }
                innerArray.put(innerObjectForItems1[0]);

                String debitNoteTo = "";
                String debitNoteFrom = "";

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(31l);
                inparam1.add(toTransDate1);
                List<Transaction> transactions3 = genericDAO.queryWithParamsName(DOCUMENT_QUERY4.toString(),
                        entityManager, inparam1);

                if (transactions3.size() > 0) {
                    debitNoteTo = transactions3.get(0).getInvoiceNumber();
                }

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(31l);
                inparam1.add(fromTransDate1);
                List<Transaction> transactions4 = genericDAO.queryWithParamsName(DOCUMENT_QUERY5.toString(),
                        entityManager, inparam1);
                if (transactions4.size() > 0) {
                    debitNoteFrom = transactions4.get(0).getInvoiceNumber();
                }

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(31l);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                List<Object[]> numberOfDebitNotes = genericDAO.queryWithParamsNameGeneric(DOCUMENT_QUERY6.toString(),
                        entityManager, inparam1);

                innerObjectForItems1[1].put("doc_det__doc_typ", "Debit Note");
                innerObjectForItems1[1].put("doc_det__docs__num", 1);
                innerObjectForItems1[1].put("doc_det__docs__from", debitNoteFrom);
                innerObjectForItems1[1].put("doc_det__docs__to", debitNoteTo);
                if (numberOfDebitNotes.size() > 0) {
                    innerObjectForItems1[1].put("doc_det__docs__totnum", numberOfDebitNotes.get(0));
                } else {
                    innerObjectForItems1[1].put("doc_det__docs__totnum", "");
                }
                innerObjectForItems1[1].put("doc_det__docs__cancel", 0);
                if (numberOfDebitNotes.size() > 0) {
                    innerObjectForItems1[1].put("doc_det__docs__net_issue", numberOfDebitNotes.get(0));
                } else {
                    innerObjectForItems1[1].put("doc_det__docs__net_issue", "");
                }
                innerArray.put(innerObjectForItems1[1]);

                String creditNoteTo = "";
                String creditNoteFrom = "";

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(30l);
                inparam1.add(toTransDate1);
                List<Transaction> transactions5 = genericDAO.queryWithParamsName(DOCUMENT_QUERY4.toString(),
                        entityManager, inparam1);

                if (transactions5.size() > 0) {
                    creditNoteTo = transactions5.get(0).getInvoiceNumber();
                }

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(30l);
                inparam1.add(fromTransDate1);
                List<Transaction> transactions6 = genericDAO.queryWithParamsName(DOCUMENT_QUERY5.toString(),
                        entityManager, inparam1);

                if (transactions6.size() > 0) {
                    creditNoteFrom = transactions6.get(0).getInvoiceNumber();
                }

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(30l);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                List<Object[]> numberOfCreditNotes = genericDAO.queryWithParamsNameGeneric(DOCUMENT_QUERY6.toString(),
                        entityManager, inparam1);

                innerObjectForItems1[2].put("doc_det__doc_typ", "Credit Note");
                innerObjectForItems1[2].put("doc_det__docs__num", 1);
                innerObjectForItems1[2].put("doc_det__docs__from", creditNoteFrom);
                innerObjectForItems1[2].put("doc_det__docs__to", creditNoteTo);
                if (numberOfCreditNotes.size() > 0) {
                    innerObjectForItems1[2].put("doc_det__docs__totnum", numberOfCreditNotes.get(0));
                } else {
                    innerObjectForItems1[2].put("doc_det__docs__totnum", "");
                }
                innerObjectForItems1[2].put("doc_det__docs__cancel", 0);
                if (numberOfCreditNotes.size() > 0) {
                    innerObjectForItems1[2].put("doc_det__docs__net_issue", numberOfCreditNotes.get(0));
                } else {
                    innerObjectForItems1[2].put("doc_det__docs__net_issue", "");
                }
                innerArray.put(innerObjectForItems1[2]);

                String advRcvdVoucherTo = "";

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(6l);
                inparam1.add(toTransDate1);
                List<Transaction> transactions7 = genericDAO.queryWithParamsName(DOCUMENT_QUERY4.toString(),
                        entityManager, inparam1);

                if (transactions7.size() > 0) {
                    advRcvdVoucherTo = transactions7.get(0).getInvoiceNumber();
                }

                String advRcvdVoucherFrom = "";

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(6l);
                inparam1.add(fromTransDate1);
                List<Transaction> transactions8 = genericDAO.queryWithParamsName(DOCUMENT_QUERY5.toString(),
                        entityManager, inparam1);

                if (transactions8.size() > 0) {
                    advRcvdVoucherFrom = transactions8.get(0).getInvoiceNumber();
                }

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(6l);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                List<Object[]> numberOfAdvanceReceivedVoucher = genericDAO
                        .queryWithParamsNameGeneric(DOCUMENT_QUERY6.toString(), entityManager, inparam1);

                // innerObjectForItems1[3].put("doc_num",6);
                innerObjectForItems1[3].put("doc_det__doc_typ", "Receipt Voucher");
                innerObjectForItems1[3].put("doc_det__docs__num", 1);
                innerObjectForItems1[3].put("doc_det__docs__from", advRcvdVoucherFrom);
                innerObjectForItems1[3].put("doc_det__docs__to", advRcvdVoucherTo);
                if (numberOfAdvanceReceivedVoucher.size() > 0) {
                    innerObjectForItems1[3].put("doc_det__docs__totnum", numberOfAdvanceReceivedVoucher.get(0));
                } else {
                    innerObjectForItems1[3].put("doc_det__docs__totnum", "");
                }
                innerObjectForItems1[3].put("doc_det__docs__cancel", 0);
                if (numberOfAdvanceReceivedVoucher.size() > 0) {
                    innerObjectForItems1[3].put("doc_det__docs__net_issue", numberOfAdvanceReceivedVoucher.get(0));
                } else {
                    innerObjectForItems1[3].put("doc_det__docs__net_issue", "");
                }
                innerArray.put(innerObjectForItems1[3]);

                String refundVoucherTo = "";
                String refundVoucherFrom = "";

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(35l);
                inparam1.add(toTransDate1);
                List<Transaction> transactions9 = genericDAO.queryWithParamsName(DOCUMENT_QUERY4.toString(),
                        entityManager, inparam1);
                if (transactions9.size() > 0) {
                    refundVoucherTo = transactions9.get(0).getInvoiceNumber();
                }

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(35l);
                inparam1.add(fromTransDate1);
                List<Transaction> transactions10 = genericDAO.queryWithParamsName(DOCUMENT_QUERY5.toString(),
                        entityManager, inparam1);

                if (transactions10.size() > 0) {
                    refundVoucherFrom = transactions10.get(0).getInvoiceNumber();
                }

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(35l);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                List<Object[]> numberOfRefundVoucher = genericDAO.queryWithParamsNameGeneric(DOCUMENT_QUERY6.toString(),
                        entityManager, inparam1);

                innerObjectForItems1[4].put("doc_det__doc_typ", "Refund Voucher");
                innerObjectForItems1[4].put("doc_det__docs__num", 1);
                innerObjectForItems1[4].put("doc_det__docs__from", refundVoucherFrom);
                innerObjectForItems1[4].put("doc_det__docs__to", refundVoucherTo);
                if (numberOfRefundVoucher.size() > 0) {
                    innerObjectForItems1[4].put("doc_det__docs__totnum", numberOfRefundVoucher.get(0));
                } else {
                    innerObjectForItems1[4].put("doc_det__docs__totnum", "");
                }
                innerObjectForItems1[4].put("doc_det__docs__cancel", 0);
                if (numberOfRefundVoucher.size() > 0) {
                    innerObjectForItems1[4].put("doc_det__docs__net_issue", numberOfRefundVoucher.get(0));
                } else {
                    innerObjectForItems1[4].put("doc_det__docs__net_issue", "");
                }
                innerArray.put(innerObjectForItems1[4]);

                String deliveryChallanTo = "";
                String deliveryChallanFrom = "";

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(25l);
                inparam1.add(2);
                inparam1.add(toTransDate1);
                List<Transaction> transactions11 = genericDAO.queryWithParamsName(DOCUMENT_DCQUERY1.toString(),
                        entityManager, inparam1);

                if (transactions11.size() > 0) {
                    deliveryChallanTo = transactions11.get(0).getInvoiceNumber();
                }

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(25l);
                inparam1.add(2);
                inparam1.add(fromTransDate1);
                List<Transaction> transactions12 = genericDAO.queryWithParamsName(DOCUMENT_DCQUERY2.toString(),
                        entityManager, inparam1);

                if (transactions12.size() > 0) {
                    deliveryChallanFrom = transactions12.get(0).getInvoiceNumber();
                }

                inparam1.clear();
                inparam1.add(user.getOrganization().getId());
                inparam1.add(gstIn);
                inparam1.add(25l);
                inparam1.add(fromTransDate1);
                inparam1.add(toTransDate1);
                List<Object[]> numberOfDeliveryChallan = genericDAO
                        .queryWithParamsNameGeneric(DOCUMENT_DCQUERY3.toString(), entityManager, inparam1);
                // innerObjectForItems1[5].put("doc_num",12);
                innerObjectForItems1[5].put("doc_det__doc_typ", "Delivery Challan");
                innerObjectForItems1[5].put("doc_det__docs__num", 1);
                innerObjectForItems1[5].put("doc_det__docs__from", deliveryChallanFrom);
                innerObjectForItems1[5].put("doc_det__docs__to", deliveryChallanTo);
                if (numberOfDeliveryChallan.size() > 0) {
                    innerObjectForItems1[5].put("doc_det__docs__totnum", numberOfDeliveryChallan.get(0));
                } else {
                    innerObjectForItems1[5].put("doc_det__docs__totnum", "");
                }
                innerObjectForItems1[5].put("doc_det__docs__cancel", 0);
                if (numberOfDeliveryChallan.size() > 0) {
                    innerObjectForItems1[5].put("doc_det__docs__net_issue", numberOfDeliveryChallan.get(0));
                } else {
                    innerObjectForItems1[5].put("doc_det__docs__net_issue", "");
                }
                innerArray.put(innerObjectForItems1[5]);
                innerObjectForDocsIssued[0] = new JSONObject();
                // innerObjectForDocsIssued[0].put("doc", innerArray);
                outerArray6.put(innerObjectForDocsIssued[0]);
                outerArray6.put(innerArray);

                outerObject.put("doc_issue", outerArray6);
            }
            outerObject.put("fp", gstr1period);
            outerObject.put("GSTIN", gstIn);
            FileWriter fw = null;
            BufferedWriter writer = null;

            File fileDir = new File(path);
            if (!fileDir.exists()) {
                fileDir.mkdir();
            }
            String fileName = user.getOrganization().getName() + "_" + gstr1period + "_GSTR1.json";
            path = path + fileName;
            file = new File(path);
            try {
                fw = new FileWriter(file);
                writer = new BufferedWriter(fw);
                writer.append(outerObject.toString());
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
            log.log(Level.INFO, "inside error");
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
    }

    @Transactional
    public Result downloadKarvyGSTR1JSONSFile(Http.Request request) {
        log.log(Level.FINE, ">>>> Start inside download buy transaction data");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        ObjectNode result = Json.newObject();
        File file = null;

        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            JsonNode json = request.body().asJson();
            String useremail = json.findValue("useremail").asText();
            String type = json.findValue("type").asText();
            String dateMonthAndYear = json.findValue("txtDate").asText();
            session.adding("email", useremail);
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            String year = dateMonthAndYear.substring(dateMonthAndYear.length() - 4, dateMonthAndYear.length());
            String month = dateMonthAndYear.substring(0, dateMonthAndYear.length() - 5);
            // January 1,2018
            String dateinjava = month + " 1," + year;
            Date fromTransDate = IdosConstants.MYSQLDF
                    .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateinjava)));
            Calendar cal = Calendar.getInstance();
            cal.setTime(fromTransDate);
            Integer monthInInt = cal.get(Calendar.MONTH) + 1;
            String gstr1period = monthInInt.toString() + year;
            Long branchId = user.getBranch().getId();
            System.out.println("branchId = " + branchId);
            Branch branch = Branch.findById(branchId);
            String fileName = null;
            if (branch != null) {
                String branchgstin = branch.getGstin();
                System.out.println("branchgstin " + branchgstin);
                String path = application.path().toString() + "/logs/KarvyJSONData/";
                String karvyurl = null;
                String orgName = user.getOrganization().getName().replaceAll("\\s", "");
                fileName = orgName;
                if (type != null) {
                    if (type.equalsIgnoreCase("GSTR1")) {
                        karvyurl = "http://api.karvygst.com/GetData/GenerateGSTR1?Returnperiod=" + gstr1period
                                + "&GSTIN=" + branchgstin; // 36GSPTN0129G1ZK";
                        fileName = fileName + "_" + gstr1period + "_GSTR1.json";
                    } else if (type.equalsIgnoreCase("GSTR3B")) {
                        karvyurl = "http://api.karvygst.com/GetData/GenerateGSTR3B?Returnperiod=" + gstr1period
                                + "&GSTIN" + branchgstin;
                        fileName = fileName + "_" + gstr1period + "_GSTR3B.json";
                    }
                }
                System.out.println("karvyURL = " + karvyurl);
                log.log(Level.FINE, "karvyURL = " + karvyurl);
                file = createKarvyGSTR1JSONSFile(user, entityManager, path, fileName, karvyurl);
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
    public static File createKarvyGSTR1JSONSFile(Users user, EntityManager entityManager, String path, String fileName,
            String karvyURL) {
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(karvyURL); // new
                                         // URL("http://api2.karvygst.com/GetData/GenerateGSTR1?Returnperiod=102017&GSTIN=29AADCI5569G1ZK");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("clientid", "KarvyGst");
            con.setRequestProperty("clientsecret", "KarvyGst@123");
            con.setRequestProperty("emailid", "idos@gmail.com");
            con.setRequestProperty("password", "Idos@01");
            con.setRequestProperty("accept", "application/json");
            con.setRequestProperty("type", "Postman");

            int HttpResult = con.getResponseCode();
            if (HttpResult == 200) {
                BufferedReader buffer = new BufferedReader(
                        new java.io.InputStreamReader(con.getInputStream(), "utf-8"));

                String line = null;
                while ((line = buffer.readLine()) != null) {
                    sb.append(line + "\n");
                }
                System.out.println("SB output : " + sb);
                log.log(Level.FINE, "SB output : " + sb);
                buffer.close();
                JSONObject jObject = new JSONObject(sb.toString());
            } else {
                System.out.println("ERROR " + con.getResponseMessage());
                log.log(Level.FINE, "ERROR " + con.getResponseMessage());
            }
            con.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FileWriter fw = null;
        BufferedWriter writer = null;
        File file = null;

        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        path = path + fileName;
        file = new File(path);
        try {
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            writer.append(sb).append('|');
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
        return file;
    }
}
