package com.idos.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sunil Namdev on 08-07-2016.
 */
final public class IdosConstants {

    public static final Map<String, String> STATE_CODE_MAPPING;
    public static final Map<String, String> TYPE_OF_SUPPLY_MAP;
    public static final Map<String, String> BUY_TYPE_OF_SUPPLY_MAP;

    static {
        Map<String, String> stateMap = new HashMap<String, String>();
        stateMap.put("00", "Other");
        stateMap.put("01", "Jammu & Kashmir");
        stateMap.put("02", "Himachal Pradesh");
        stateMap.put("03", "Punjab");
        stateMap.put("04", "Chandigarh");
        stateMap.put("05", "Uttranchal");
        stateMap.put("06", "Haryana");
        stateMap.put("07", "Delhi");
        stateMap.put("08", "Rajasthan");
        stateMap.put("09", "Uttar Pradesh");
        stateMap.put("10", "Bihar");
        stateMap.put("11", "Sikkim");
        stateMap.put("12", "Arunachal Pradesh");
        stateMap.put("13", "Nagaland");
        stateMap.put("14", "Manipur");
        stateMap.put("15", "Mizoram");
        stateMap.put("16", "Tripura");
        stateMap.put("17", "Meghalaya");
        stateMap.put("18", "Assam");
        stateMap.put("19", "West Bengal");
        stateMap.put("20", "Jharkhand");
        stateMap.put("21", "Odisha (Formerly Orissa");
        stateMap.put("22", "Chhattisgarh");
        stateMap.put("23", "Madhya Pradesh");
        stateMap.put("24", "Gujarat");
        stateMap.put("25", "Daman & Diu");
        stateMap.put("26", "Dadra & Nagar Haveli");
        stateMap.put("27", "Maharashtra");
        stateMap.put("28", "");
        stateMap.put("29", "Karnataka");
        stateMap.put("30", "Goa");
        stateMap.put("31", "Lakshdweep");
        stateMap.put("32", "Kerala");
        stateMap.put("33", "Tamil Nadu");
        stateMap.put("34", "Pondicherry");
        stateMap.put("35", "Andaman & Nicobar Islands");
        stateMap.put("36", "Telangana");
        stateMap.put("37", "Andhra Pradesh");
        STATE_CODE_MAPPING = Collections.unmodifiableMap(stateMap);

        Map<String, String> typeOfSupplyMap = new HashMap<String, String>(7);
        typeOfSupplyMap.put("0", "");
        typeOfSupplyMap.put("1", "Regular Supply");
        typeOfSupplyMap.put("2", "Supply applicable for Reverse Charge");
        typeOfSupplyMap.put("3", "This is an Export Supply");
        typeOfSupplyMap.put("4", "This is supply to SEZ Unit or SEZ Developer");
        typeOfSupplyMap.put("5", "This is deemed Export Supply");
        typeOfSupplyMap.put("6", "Supply made through E-commerce Operator");
        typeOfSupplyMap.put("7", "Bill of Supply");
        TYPE_OF_SUPPLY_MAP = Collections.unmodifiableMap(typeOfSupplyMap);

        Map<String, String> buyTypeOfSupplyMap = new HashMap<String, String>(5);
        buyTypeOfSupplyMap.put("0", "");
        buyTypeOfSupplyMap.put("1", "Regular");
        buyTypeOfSupplyMap.put("2", "Supply on Reverse Charge - Unregistered Vendor");
        buyTypeOfSupplyMap.put("3", "Supply attracting tax on reverse charge - registered vendor");
        buyTypeOfSupplyMap.put("4", "Overseas / SEZ Import Goods - Supply");
        buyTypeOfSupplyMap.put("5", "Overseas / SEZ Import Services - Supply");
        BUY_TYPE_OF_SUPPLY_MAP = Collections.unmodifiableMap(buyTypeOfSupplyMap);
    }

    public static final String HEAD_SPECIFIC = "item";
    public static final String HEAD_CASH = "cash";
    public static final String HEAD_BANK = "bank";
    public static final String HEAD_BANK_ACCOUNT = "bacc";
    public static final String HEAD_PETTY = "pett";
    public static final String HEAD_VENDOR = "vend"; // Under liabilities -Account Payables
    public static final String HEAD_CUSTOMER = "cust"; // Under Assets - Account Receivables
    public static final String HEAD_VENDOR_ADV = "vAdv"; // Under Assets- Vendor Advance
    public static final String HEAD_CUSTOMER_ADV = "cAdv"; // Under liabilities - customer advance
    public static final String HEAD_USER = "user";
    public static final String HEAD_TAXS = "taxs";
    public static final String HEAD_SGST = "sgst";
    public static final String HEAD_CGST = "cgst";
    public static final String HEAD_IGST = "igst";
    public static final String HEAD_CESS = "cess";

    public static final String HEAD_RCM_SGST_IN = "rmsi";
    public static final String HEAD_RCM_CGST_IN = "rmci";
    public static final String HEAD_RCM_IGST_IN = "rmii";
    public static final String HEAD_RCM_CESS_IN = "rmei";

    public static final String HEAD_RCM_SGST_OUTPUT = "rmso";
    public static final String HEAD_RCM_CGST_OUTPUT = "rmco";
    public static final String HEAD_RCM_IGST_OUTPUT = "rmio";
    public static final String HEAD_RCM_CESS_OUTPUT = "rmeo";

    public static final String HEAD_TDS = "tds";
    public static final String HEAD_TDS_INPUT = "tdsi";
    // public static final String HEAD_TDS_OUTPUT = "tdso";
    public static final String HEAD_TDS_192 = "192_";
    public static final String HEAD_TDS_194A = "194a";
    public static final String HEAD_TDS_194C1 = "1941";
    public static final String HEAD_TDS_194C2 = "1942";
    public static final String HEAD_TDS_194H = "194h";
    public static final String HEAD_TDS_194I1 = "1943";
    public static final String HEAD_TDS_194I2 = "1944";
    public static final String HEAD_TDS_194J = "194j";

    public static final String HEAD_INTR_BRANCH = "intb";
    public static final String HEAD_INTR_BRANCH_OUT = "ibto";
    public static final String HEAD_INTR_BRANCH_IN = "ibti";
    public static final String HEAD_TRVL_ADV = "tauser";
    public static final String HEAD_EXP_ADV = "eauser";
    public static final String HEAD_EMP_CLAIM = "eclm";
    public static final String HEAD_PAYROLL_EXPENSE = "pexp";
    public static final String HEAD_PAYROLL_DEDUCTIONS = "pded";

    public static final int HEAD_TYPE_INWORD = 1;
    public static final int HEAD_TYPE_OUTWORD = 2;

    public static final int PROVISION_JOURNAL_ENTRY_CREDIT = 0;
    public static final int PROVISION_JOURNAL_ENTRY_DEBIT = 1;
    public static final short PAYMODE_NONE = 0;
    public static final short PAYMODE_CASH = 1;
    public static final short PAYMODE_BANK = 2;
    public static final short PAYMODE_PETTY_CASH = 3;

    public static final short CASH = 1;
    public static final short PETTY_CASH = 2;

    public static final short VENDOR = 1;
    public static final short CUSTOMER = 2;
    public static final short WALK_IN_CUSTOMER = 3;
    public static final short WALK_IN_VENDOR = 4;
    public static final short CUSTOMER_ADVANCE = 5;
    public static final short VENDOR_ADVANCE = 6;

    public static final short INPUT_TAX = 1;
    public static final short OUTPUT_TAX = 2;
    public static final short RCM_INPUT_TAX = 3;
    public static final short OUTPUT_TDS = 4;
    public static final short RCM_OUTPUT_TAX = 5;
    public static final short INPUT_TDS = 6;// 8 - Withholidng tax on payments received from customers and others

    public static final short OUTPUT_SGST = 20; // here 2 for output
    public static final short OUTPUT_CGST = 21;
    public static final short OUTPUT_IGST = 22;
    public static final short OUTPUT_CESS = 23;

    public static final short INPUT_SGST = 10; // here 1 for input
    public static final short INPUT_CGST = 11;
    public static final short INPUT_IGST = 12;
    public static final short INPUT_CESS = 13;

    public static final short RCM_SGST_IN = 30; // here 3 for RCM input
    public static final short RCM_CGST_IN = 31;
    public static final short RCM_IGST_IN = 32;
    public static final short RCM_CESS_IN = 33;

    public static final short RCM_SGST_OUTPUT = 50; // here 3 for RCM output
    public static final short RCM_CGST_OUTPUT = 51;
    public static final short RCM_IGST_OUTPUT = 52;
    public static final short RCM_CESS_OUTPUT = 53;

    public static final int TDS_192 = 40; // 4 - TDS
    public static final int TDS_194A = 41;
    public static final int TDS_194C1 = 42;
    public static final int TDS_194C2 = 43;
    public static final int TDS_194H = 44;
    public static final int TDS_194I1 = 45;
    public static final int TDS_194I2 = 46;
    public static final int TDS_194J = 47;

    public static final short INCOME = 1;
    public static final short EXPENSE = 2;
    public static final short ASSETS = 3;
    public static final short LIABILITIES = 4;

    public static final short CREDIT_SALES_PURCHASE = 1;
    public static final short CUSTOMER_VENDOR_ADVANCE = 2;
    public static final int TRAVEL_ADVANCE = 1;
    public static final int EXPENSE_ADVANCE = 2;

    public static final boolean IS_CREDIT = true;

    public static final String TRAVEL_EXPENSES = "Travel Expenses";
    public static final String BOARDING_LODGING = "Boarding & Lodging";
    public static final String OTHER_EXPENSES = "Other Expenses";
    public static final String FIXED_PER_DIAM = "Fixed Per Diam";
    public static final String INCURRED_EXPENCES = "Incurred Expenses";
    public static final String REIMBURSEMENT_EXPENSES = "Reimbursment Expenses";

    public static final String TRAVEL_EXPENSES_MAPPING_ID = "24";
    public static final String BOARDING_LODGING_MAPPING_ID = "25";
    public static final String OTHER_EXPENSES_MAPPING_ID = "26";
    public static final String FIXED_PER_DIAM_MAPPING_ID = "27";

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("######.00");

    public static final SimpleDateFormat IDOSDF = new SimpleDateFormat("MMM dd,yyyy");
    public static final SimpleDateFormat IDOSDTF = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
    public static final SimpleDateFormat idosdf = new SimpleDateFormat("MMM dd,yyyy");
    public static final SimpleDateFormat IDOSMDTDF = new SimpleDateFormat("MMM dd");
    public static final SimpleDateFormat SDF = new SimpleDateFormat("MM-dd-yyyy");
    public static final SimpleDateFormat MYSQLDF = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat mysqldf = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat MYSQLMDTDF = new SimpleDateFormat("MM-dd");
    public static final SimpleDateFormat REPORTDF = new SimpleDateFormat("dd-MM-yyyy");
    public static final SimpleDateFormat MYSQLDTF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat TIMEFMT = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat IDOSSDFTIME = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
    public static final SimpleDateFormat IDOSSDFDATE = new SimpleDateFormat("dd-MMM-yyyy");
    public static final SimpleDateFormat IDOSJSONDATE = new SimpleDateFormat(" dd MMMM yyyy");
    public static final SimpleDateFormat IDOSJSONDATEGSTR1 = new SimpleDateFormat(" dd-MM-yyyy");
    public static final SimpleDateFormat REPORTDF2 = new SimpleDateFormat("dd/MM/yyyy");
    public static final String UNKNOWN_EXCEPTION_ERRCODE = "IDOS_0000";
    public static final String TB_EXCEPTION_ERRCODE = "IDOS_0001";
    public static final String RECORD_NOT_FOUND = "IDOS_0002";
    public static final String NULL_KEY_EXC_ESMF = "IDOS_100";
    public static final String NULL_KEY_EXC_ESMF_MSG = "Encountered Null Values";
    public static final String BUSINESS_EXCEPTION = "Business";
    public static final String TECHNICAL_EXCEPTION = "Technical";
    public static final String DATA_FORMAT_ERRCODE = "IDOS_0003";
    public static final String DATA_FORMAT_EXCEPTION = "Dataformat";

    public static final String COA_MAPPING_ERRCODE = "IDOS_0004";
    public static final String COA_MAPPING_EXCEPTION = "COA MAPPING MISSING";

    public static final String SESSION_ERRCODE = "IDOS_0005";
    public static final String SESSION_EXCEPTION = "SESSION EXPIRED";

    public static final String INVALID_DATA_ERRCODE = "IDOS_0006";
    public static final String INVALID_DATA_EXCEPTION = "INVALID DATA";

    public static final String INVALID_INVENTORY_ERRCODE = "IDOS_0007";
    public static final String INVALID_INVENTORY_EXCEPTION = "Invalid quantity";

    public static final String INVALID_STORAGE_ERRCODE = "IDOS_0008";
    public static final String INVALID_STORAGE_EXCEPTION = "Invalid connection or storage";
    public static final String INVALID_FILE_EXIST_ERRCODE = "IDOS_0009";

    public static final String INSUFFICIENT_INVENTORY_ERRCODE = "IDOS_0009";
    public static final String INSUFFICIENT_INVENTORY_EXCEPTION = "insufficient inventory balance";

    public static final String INSUFFICIENT_BALANCE_ERRCODE = "IDOS_0010";
    public static final String INSUFFICIENT_BALANCE_EXCEPTION = "insufficient balance";

    public static final String XLSX_TYPE = "xlsx";
    public static final String PDF_TYPE = "pdf";

    public final static String AUTH_TOKEN_HEADER = "X-AUTH-TOKEN";
    public final static String AUTH_TOKEN_HEADER0 = "X-AUTH-TOKEN0";
    public final static String AUTH_TOKEN_HEADER1 = "X-AUTH-TOKEN1";
    public static final String AUTH_TOKEN = "authToken";
    public static final String AUTH_TOKEN0 = "authToken0";
    public static final String AUTH_TOKEN1 = "authToken1";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String VENDOR_EMAIL = "vendoremail";
    public static final String USER_EMAIL = "email";
    public static final String SELLER_EMAIL = "selleremail";

    public static final String DECIMAL_FORMAT_STR = "######.00";
    public static final DecimalFormat decimalFormat = new DecimalFormat("#####0.00");
    public static final DecimalFormat DECIMAL_FORMAT2 = new DecimalFormat("######.00");
    public static final DecimalFormat DECIMAL_FORMAT_MAIN = new DecimalFormat("#,##0.00#");
    public static final DecimalFormat DECIMAL_FORMAT_MAIN_2DEC = new DecimalFormat("#,##0.00");

    public static final int IN_REVENUE_FROM_OPERATIONS = 100;
    public static final int IN_OTHER_INCOME = 101;

    public static final int EX_COST_OF_MATERIALS_CONSUMED = 200;
    public static final int EX_PURCHASES_OF_STOCK_IN_TRADE = 201;
    public static final int EX_CIIOFGIP_AND_STOCK_IN_TRADE = 202; // Changes In Inventories Of Finished Goods
                                                                  // Work-In-Progress And Stock-In-Trade
    public static final int EX_EMPLOYEE_BENEFITS_EXPENSE = 203; // EMPLOYEE_BENEFITS_EXPENSE
    public static final int EX_FINANCE_COSTS = 204;
    public static final int EX_DEPRECIATION_AND_AMORTIZATION = 205;
    public static final int EX_OTHER_EXPENSES = 206;
    public static final int EX_EXCEPTIONAL_ITEMS = 207;
    public static final int EX_EXTRAORDINARY_ITEMS = 208;
    public static final int EX_CURRENT_TAX = 209;
    public static final int EX_DEFERRED_TAX = 210;

    public static final int AS_TANGIBLE_ASSETS = 300;
    public static final int AS_INTANGIBLE_ASSETS = 301;
    public static final int AS_CAPITAL_WORK_IN_PROGRESS = 302;
    public static final int AS_INTANGIBLE_ASSETS_UNDER_DEV = 303;
    public static final int AS_NON_CURRENT_INVESTMENTS = 304;
    public static final int AS_DEFERRED_TAX_ASSETS_NET = 305;
    public static final int AS_LONG_TERM_LOANS_AND_ADVANCES = 306;
    public static final int AS_OTHER_NON_CURRENT_ASSETS = 307;
    public static final int AS_CURRENT_INVESTMENTS = 308;
    public static final int AS_INVENTORIES = 309;
    public static final int AS_TRADE_RECEIVABLES = 310;
    public static final int AS_CASH_AND_CASH_EQUIVALENTS = 311;
    public static final int AS_SHORT_TERM_LOANS_AND_ADVANCES = 312;
    public static final int AS_OTHER_CURRENT_ASSETS = 313;

    public static final int LI_SHARE_CAPITAL = 400;
    public static final int LI_RESERVES_AND_SURPLUS = 401;
    public static final int LI_MONEY_RECEIVED_AGAINST_SHARE_WARRANTS = 402;
    public static final int LI_SHARE_APPLICATION_MONEY_PENDING_ALLOTMENT = 403;
    public static final int LI_LONG_TERM_BORROWINGS = 404;
    public static final int LI_DEFERRED_TAX_LIABILITIES_NET = 405;
    public static final int LI_OTHER_LONG_TERM_LIABILITIES = 406;
    public static final int LI_LONG_TERM_PROVISIONS = 407;
    public static final int LI_SHORT_TERM_BORROWINGS = 408;
    public static final int LI_TRADE_PAYABLES = 409;
    public static final int LI_OTHER_CURRENT_LIABILITIES = 410;
    public static final int LI_SHORT_TERM_PROVISIONS = 411;

    public static final int RAW_MATERIAL = 1;
    public static final int CONSUMABLES = 2;
    public static final int FINISHED_GOODS = 3;
    public static final int WORK_IN_PROGRESS = 4;
    public static final int STOCK_IN_TRADE = 5;

    public static final long SELL_ON_CASH_COLLECT_PAYMENT_NOW = 1l;
    public static final long SELL_ON_CREDIT_COLLECT_PAYMENT_LATER = 2l;
    public static final long BUY_ON_CASH_PAY_RIGHT_AWAY = 3l;
    public static final long BUY_ON_CREDIT_PAY_LATER = 4l;
    public static final long RECEIVE_PAYMENT_FROM_CUSTOMER = 5l;
    public static final long RECEIVE_ADVANCE_FROM_CUSTOMER = 6l;
    public static final long PAY_VENDOR_SUPPLIER = 7l;
    public static final long PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER = 8l;
    public static final long RECEIVE_SPECIAL_ADJUSTMENTS_AMOUNT_FROM_VENDORS = 9l;
    public static final long PAY_SPECIAL_ADJUSTMENTS_AMOUNT_TO_VENDORS = 10l;
    public static final long BUY_ON_PETTY_CASH_ACCOUNT = 11l;
    public static final long SALES_RETURNS = 12l;
    public static final long PURCHASE_RETURNS = 13l;
    public static final long TRANSFER_MAIN_CASH_TO_PETTY_CASH = 14l;
    public static final long REQUEST_FOR_TRAVEL_ADVANCE = 15l;
    public static final long SETTLE_TRAVEL_ADVANCE = 16l;
    public static final long REQUEST_ADVANCE_FOR_EXPENSE = 17l;
    public static final long SETTLE_ADVANCE_FOR_EXPENSE = 18l;
    public static final long REQUEST_FOR_EXPENSE_REIMBURSEMENT = 19l;
    public static final long MAKE_PROVISION_JOURNAL_ENTRY = 20l;
    public static final long JOURNAL_ENTRY = 21l;
    public static final long WITHDRAW_CASH_FROM_BANK = 22l;
    public static final long DEPOSIT_CASH_IN_BANK = 23l;
    public static final long TRANSFER_FUNDS_FROM_ONE_BANK_TO_ANOTHER = 24l;
    public static final long TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER = 25l;
    public static final long INVENTORY_OPENING_BALANCE = 26l;
    public static final long PREPARE_QUOTATION = 27l;
    public static final long PROFORMA_INVOICE = 28l;
    public static final long PURCHASE_ORDER = 29l;
    public static final long CREDIT_NOTE_CUSTOMER = 30L;
    public static final long DEBIT_NOTE_CUSTOMER = 31L;
    public static final long CREDIT_NOTE_VENDOR = 32L;
    public static final long DEBIT_NOTE_VENDOR = 33L;
    public static final long PROCESS_PAYROLL = 34L;
    public static final long REFUND_ADVANCE_RECEIVED = 35L;
    public static final long REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE = 36L;
    public static final long REVERSAL_OF_ITC = 37L;
    public static final long CANCEL_INVOICE = 38L;
    public static final long BILL_OF_MATERIAL = 39L;
    public static final long CREATE_PURCHASE_REQUISITION = 40L;
    public static final long CREATE_PURCHASE_ORDER = 41L;
    public static final long MATERIAL_ISSUE_NOTE = 42L;
    /*
     * Could be replace with enum below, but could be dangerous, because
     * many changes are required.
     * 
     * public enum TransactionsPurpose {
     * SELL_ON_CASH_COLLECT_PAYMENT_NOW(1, "Sell on cash & collect payment now"),
     * SELL_ON_CREDIT_COLLECT_PAYMENT_LATER(2,
     * "Sell on credit & collect payment later"),
     * BUY_ON_CASH_PAY_RIGHT_AWAY(3, "Buy on cash & pay right away"),
     * BUY_ON_CREDIT_PAY_LATER(4, "Buy on credit & pay later"),
     * RECEIVE_PAYMENT_FROM_CUSTOMER(5, "Receive payment from customer"),
     * RECEIVE_ADVANCE_FROM_CUSTOMER(6, "Receive advance from customer"),
     * PAY_VENDOR_SUPPLIER(7, "Pay vendor/supplier"),
     * PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER(8, "Pay advance to vendor or supplier"),
     * RECEIVE_SPECIAL_ADJUSTMENTS_AMOUNT_FROM_VENDORS(9,
     * "Receive special adjustments amount from vendors"),
     * PAY_SPECIAL_ADJUSTMENTS_AMOUNT_TO_VENDORS(10,
     * "Pay special adjustments amount to vendors"),
     * BUY_ON_PETTY_CASH_ACCOUNT(11, "Buy on Petty Cash Account"),
     * SALES_RETURNS(12, "Sales returns"),
     * PURCHASE_RETURNS(13, "Purchase returns"),
     * TRANSFER_MAIN_CASH_TO_PETTY_CASH(14, "Transfer main cash to petty cash"),
     * REQUEST_FOR_TRAVEL_ADVANCE(15, "Request For Travel Advance"),
     * SETTLE_TRAVEL_ADVANCE(16, "Settle Travel Advance"),
     * REQUEST_ADVANCE_FOR_EXPENSE(17, "Request Advance For Expense"),
     * SETTLE_ADVANCE_FOR_EXPENSE(18, "Settle Advance For Expense"),
     * REQUEST_FOR_EXPENSE_REIMBURSEMENT(19, "Request For Expense Reimbursement"),
     * MAKE_PROVISION_JOURNAL_ENTRY(20, "Make Provision/Journal Entry"),
     * JOURNAL_ENTRY(21, "Journal Entry"),
     * WITHDRAW_CASH_FROM_BANK(22, "Withdraw Cash From Bank"),
     * DEPOSIT_CASH_IN_BANK(23, "Deposit Cash In Bank"),
     * TRANSFER_FUNDS_FROM_ONE_BANK_TO_ANOTHER(24,
     * "Transfer Funds From One Bank To Another"),
     * TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER(25,
     * "Transfer Inventory Item From One Branch To Another"),
     * INVENTORY_OPENING_BALANCE(26, "Inventory Opening Balance"),
     * PREPARE_QUOTATION(27, "Prepare Quotation"),
     * PROFORMA_INVOICE(28, "Prepare Proforma Invoice"),
     * PURCHASE_ORDER(29, "Create a Purchase Order or Requisition"),
     * CREDIT_NOTE_CUSTOMER(30, "Credit Note for customer"),
     * DEBIT_NOTE_CUSTOMER(31, "Debit Note for customer"),
     * CREDIT_NOTE_VENDOR(32, "Credit Note for vendor"),
     * DEBIT_NOTE_VENDOR(33, "Debit Note for vendor"),
     * PROCESS_PAYROLL(34, "Process Payroll"),
     * REFUND_ADVANCE_RECEIVED(35, "Refund Advance Received"),
     * REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE(36,
     * "Refund Amount Received Against Invoice"),
     * REVERSAL_OF_ITC(37, "Reversal of ITC"),
     * CANCEL_INVOICE(38, "Cancellation/Voiding Invoice"),
     * BILL_OF_MATERIAL(39, "Create Sales Order/Bill Of Material"),
     * CREATE_PURCHASE_REQUISITION(40, "Create Purchase Requisition"),
     * CREATE_PURCHASE_ORDER(41, "Create Purchase Order"),
     * MATERIAL_ISSUE_NOTE(42, "Material Issue Note");
     * 
     * public final long id;
     * public final String purpose;
     * 
     * TransactionsPurpose(long id, String purpose) {
     * this.id = id;
     * this.purpose = purpose;
     * }
     * }
     * 
     */

    public static final int GST_GOODS = 1;
    public static final int GST_SERVICES = 2;
    public static final String GST_GOODS_TEXT = "GOODS";
    public static final String GST_SERVICES_TEXT = "SERVICES";

    public static final int GST_EXEMPT_SUPPLY = 1;
    public static final int GST_NIL_RATE_SUPPLY = 2;
    public static final int GST_NON_RATE_SUPPLY = 3;
    public static final String GST_EXEMPT_SUPPLY_TEXT = "GST Exempt Goods/Services";
    public static final String GST_NIL_RATE_SUPPLY_TEXT = "Nil Rate Goods /Services";
    public static final String GST_NON_RATE_SUPPLY_TEXT = "Non GST Goods/ Services";

    // StockServiceImpl.java - buy=1, sell=2, opening=3, closing=4
    public static final long TRADING_INV_BUY = 1;
    public static final long TRADING_INV_SELL = 2;
    public static final long TRADING_INV_OPENING_BAL = 3;
    public static final long TRADING_INV_CLOSING_BAL = 4;
    public static final long TRADING_INV_PURCHASE_RET = 5;
    public static final long TRADING_INV_SALES_RET = 6;
    public static final long TRADING_INV_PJE_EXP = 7; // buy
    public static final long TRADING_INV_PJE_INC = 8;
    public static final long TRADING_INV_PJE_EXP_CREDIT = 9;
    public static final long TRADING_INV_PJE_INC_DEBIT = 10; // returns

    public static String PUBLICK = null;
    public static String PRIVATEK = null;

    // OrganizationGstSerials.java For GST category Save in DB
    public static final int GSTIN_SERIAL_FOR_SALES_INVOICE = 1;
    public static final int GSTIN_SERIAL_FOR_PROFORMA = 2;
    public static final int GSTIN_SERIAL_FOR_QUOTATION = 3;
    public static final int GSTIN_SERIAL_FOR_RECEIPT = 4;
    public static final int GSTIN_SERIAL_FOR_ADVANCE_RECEIPT = 5;
    public static final int GSTIN_SERIAL_FOR_DEBIT_NOTE_TO_CUST = 6;
    public static final int GSTIN_SERIAL_FOR_CREDIT_NOTE_TO_CUST = 7;
    public static final int GSTIN_SERIAL_FOR_PURCHASE_ORDER = 8;
    public static final int GSTIN_SERIAL_FOR_REFUND_ADVANCE_RECEIVED = 9;
    public static final int GSTIN_SERIAL_FOR_REFUND_AMOUNT_AGAINST_INVOICE_RECEIVED = 10;
    public static final int GSTIN_SERIAL_FOR_PAYMENT_VOUCHER = 11;
    public static final int GSTIN_SERIAL_FOR_SELF_INVOICE = 12;
    public static final int GSTIN_SERIAL_FOR_CREATE_PURCHASE_ORDER = 13;
    // IDOS Payroll for karvy
    public static final int PAYROLL_TYPE_EARNINGS = 1;
    public static final int PAYROLL_TYPE_DEDUCTIONS = 2;

    // External User Status....
    public static final int EXTERNAL_USER_REGISTERED = 4;
    public static final int EXTERNAL_USER_ORG_ADD = 5;
    public static final int EXTERNAL_USER_ACCESS_CODE_SENT = 0;
    public static final int EXTERNAL_USER_SUBMIT_FOR_APPROVAL = 2;
    public static final int EXTERNAL_USER_ACCESS_TO_ORG = 1;
    public static final int EXTERNAL_USER_DEACTIVATED = 3;

    public static final double MAX_DOUBLE_VALUE = 99999999999d;

    public static final long CREATOR_RIGHTS = 1L;
    public static final long APPROVER_RIGHTS = 2L;
    public static final long AUDITOR_RIGHTS = 3L;

    // Constatnts for PAY_VENDOR_SUPPLIER and RECEIVE_PAYMENT_FROM_CUSTOMER
    public static final int TXN_TYPE_OPENING_BALANCE_VEND = 701;
    public static final int TXN_TYPE_OPENING_BALANCE_BILLWISE_VEND = 702;
    public static final int TXN_TYPE_OTHER_TRANSACTIONS_VEND = 703;
    public static final int TXN_TYPE_OPENING_BALANCE_ADV_PAID_BRACHWISE_VEND = 704;
    public static final int TXN_TYPE_CREDIT_AND_OPENING_BALANCE_ADV_PAID_VEND = 706;
    // Constatnts for RECEIVE_PAYMENT_FROM_CUSTOMER
    public static final int TXN_TYPE_OPENING_BALANCE_CUST = 501;
    public static final int TXN_TYPE_OPENING_BALANCE_BILLWISE_CUST = 502;
    public static final int TXN_TYPE_OTHER_TRANSACTIONS_CUST = 503;
    public static final int TXN_TYPE_OPENING_BALANCE_ADV_PAID_BRACHWISE_CUST = 504;

    public static final String VEND_CUST_ORG_HQL = "select a from Vendor a where a.organization.id=?1 and a.presentStatus=1 and a.type in (?2,?3)";

    public static final String FIFO_METHOD = "FIFO";
    public static final String WAC_METHOD = "WAC";
    public static final int FIFO_INVENTORY = 1;
    public static final int WAC_INVENTORY = 2;

    public enum DeployMode {
        SINGLE,
        MULTI,
        MIX
    }

    public static final String NOT_PAID = "NOT-PAID";
    public static final String PARTLY_PAID = "PARTLY-PAID";

    public static final String TXN_STATUS_REQUIRE_APPROVAL = "Require Approval";
    public static final String TXN_STATUS_ACCOUNTED = "Accounted";
    public static final String TXN_STATUS_APPROVED = "Approved";
    public static final String TXN_STATUS_REQUIRE_CLARIFICATION = "Require Clarification";
    public static final String TXN_STATUS_CLARIFIED = "Clarified";
    public static final String TXN_STATUS_REJECTED = "Rejected";
    public static final String TXN_STATUS_REQUIRE_ADDITIONAL_APPROVAL = "Require Additional Approval";
    public static final String TXN_STATUS_NO_DUE_FOR_SETTLEMENT = "No Due For Settlement";
    public static final String TXN_STATUS_PAYMENT_DUE_FROM_STAFF = "Payment Due From Staff";
    public static final String TXN_STATUS_PAYMENT_DUE_TO_STAFF = "Payment Due To Staff";

    public static final int PRICE_CHANGE = 1;
    public static final int QUANTITY_CHANGE = 2;

    public static final int BACK_DATED_TXN = 1;

    public static final String IDOS_START_DATE = "2010-01-01";

    public static final String PWC = "PWC";

    public static final int ITC_REASON_AMOUNT_IN_TERMS_OF_RULE_37_2 = 1;
    public static final int ITC_REASON_AMOUNT_IN_TERMS_OF_RULE_42_1_M = 2;
    public static final int ITC_REASON_AMOUNT_IN_TERMS_OF_RULE_42_2_A = 3;
    public static final int ITC_REASON_AMOUNT_IN_TERMS_OF_RULE_42_2_B = 4;
    public static final int ITC_REASON_AMOUNT_IN_TERMS_OF_RULE_43_1_H = 5;
    public static final int ON_ACCOUNT_OF_AMOUNT_PAID_SUBSEQUENT_TO_REVERSAL_OF_ITC = 6;
    public static final int ANY_OTHER_REVERSAL_OR_RECLAIM = 7;

    public static final int FULFILLED_TRANACTION = 1;
    public static final int UN_FULFILLED_TRANACTION = 0;

    public static final int PURCHASE_REQUISITION_NORMAL = 1;
    public static final int PURCHASE_REQUISITION_AGAINST_BOM = 2;
    public static final int PURCHASE_REQUISITION_AGAINST_SALES_ORDER = 3;

    public static final int PURCHASE_ORDER_NORMAL = 1;
    public static final int PURCHASE_ORDER_AGAINST_REQUISITION = 2;

    public static final String BOM_TXN_TYPE = "BOM";
    public static final String MAIN_TXN_TYPE = "TXN";
    public static final String PJE_TXN_TYPE = "PJE";
    public static final String CLAIM_TXN_TYPE = "CLM";
    public static final String OUTPUT_TAX_OTHER = "OTHER";
    public static final String PR_TXN_TYPE = "PR";
    public static final String PO_TXN_TYPE = "PO";

    // Created for IdosFileUploadLogs
    public static final String ORG_MODULE = "ORG";
    public static final String BRANCH_MODULE = "BRN";
    public static final String CUSTOMER_MODULE = "CUS";
    public static final String VENDOR_MODULE = "VEN";

    public static final String PAYMENT_MODE_CASH = "CASH";
    public static final String PAYMENT_MODE_BANK = "BANK";
    public static final String PAYMENT_MODE_PETTYCASH = "PETTYCASH";
    public static final int AGREE = 1;

    public static final int INSTANCE_API_MODE = 1;
    public static final int INSTANCE_REGULAR_MODE = 2;
    public static final int INSTANCE_BOTH_MODE = 3;

    public static final String UNAUTHUSER_UPLOADDOC_ERRCODE = "IDOS_0010";
    public static final String UNAUTHUSER_UPLOADDOC_EXCEPTION = "Invalid user type with transaction status";
    public static final String UNAUTHUSER_DELETEDOC_ERRCODE = "IDOS_0011";
    public static final String UNAUTHUSER_DELETEDOC_EXCEPTION = "Invalid user type with transaction status";
    public static final String UNAUTHUSER_DELDOC_ERRCODE = "IDOS_0011";
    public static final String UNAUTHUSER_DELDOC_EXCEPTION = "Unauthorise delete of document !!! ";

    public static final String MAIL_SYSTEM_OFF_KEY = "EMAIL.SYSTEM.OFF";

    public static final String SOLV = "SOLV";
    public static final String INCOME_CODE = "1000000000000000000";
    public static final String EXPENSE_CODE = "2000000000000000000";
    public static final String ASSETS_CODE = "3000000000000000000";
    public static final String LIABILITIES_CODE = "4000000000000000000";

    public enum SettlementStatus {
        SETTLED("SETTLED"),
        NOT_SETTLED("NOT-SETTLED");

        @Override
        public String toString() {
            return status;
        }

        public final String status;

        SettlementStatus(String status) {
            this.status = status;
        }
    }

    public enum ClaimTransactionStatus {
        REQUIRE_APPROVAL(TXN_STATUS_REQUIRE_APPROVAL),
        APPROVED(TXN_STATUS_APPROVED),
        REJECTED(TXN_STATUS_REJECTED),
        ACCOUNTED(TXN_STATUS_ACCOUNTED),
        NO_DUE_FOR_SETTLEMENT(TXN_STATUS_NO_DUE_FOR_SETTLEMENT),
        PAYMENT_DUE_FROM_STAFF(TXN_STATUS_PAYMENT_DUE_FROM_STAFF),
        PAYMENT_DUE_TO_STAFF(TXN_STATUS_PAYMENT_DUE_TO_STAFF);

        public final String status;

        @Override
        public String toString() {
            return status;
        }

        ClaimTransactionStatus(String status) {
            this.status = status;
        }
    }
}