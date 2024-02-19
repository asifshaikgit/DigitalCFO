package com.idos.util;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sunil Namdev on 12.11.2018.
 */
public final class CoaMappingConstants {
    public static final int  CUSTOMER_ACCOUNT_DEBTORS = 1;
    public static final int  VENDOR_ACCOUNT_CREDITORS = 2;
    public static final int  CASH_BALANCES = 3;
    public static final int  BANK_BALANCES = 4;
    public static final int  OVERDRAFT_OR_CASH_CREDIT = 5;
    public static final int  ADVANCE_RECEIVED_FROM_CUSTOMERS_DEBTORS = 6;
    public static final int  ADVANCE_PAID_TO_VENDORS_CREDITORS = 7;
    public static final int  TDS_ON_PAYMENTS_RECEIVED_FROM_CUSTOMERS_AND_OTHERS = 8;
    public static final int  TDS_ON_PAYMENTS_MADE_TO_VENDORS_CREDITORS_AND_OTHERS = 9;
    public static final int  PURCHASE_RETURNS = 10;
    public static final int  SALES_RETURNS = 11;
    public static final int  TRAVEL_ADVANCES_PAID = 12;
    public static final int  ADVANCE_PAID_TO_STAFF_FOR_EXPENSES = 13;
    public static final int  INPUT_TAX_RECEIVABLE = 14;
    public static final int  OUTPUT_TAX_PAYABLE = 15;
    public static final int  YOUR_RAW_MATERIAL_USED_IN_MANUFACTURING = 16;
    public static final int  YOUR_WORK_IN_PROGRESS_IN_MANUFACTURING = 17;
    public static final int  YOUR_FINISHED_GOODS_IN_MANUFACTURING = 18;
    public static final int  YOUR_STOCK_IN_TRADE = 19;
    public static final int  ADVANCE_PAID_TO_OTHERS = 20;
    public static final int  ADVANCES_RECEIVED_FROM_OTHERS = 21;
    public static final int  FIXED_ASSETS = 22;
    public static final int  DEPRECIATION_AMORTIZATION = 23;
    public static final int  TRAVEL_EXPENSES = 24;
    public static final int  BOARDING_AND_LODGING_EXPENSES = 25;
    public static final int  OTHER_TRAVEL_EXPENSES = 26;
    public static final int  FIXED_TRAVEL_ALLOWANCES = 27;
    public static final int  FREIGHT_OUTWARDSAMOUNT_TRANSPORTATION_OF_ITEMS_YOU_SELL = 28;
    public static final int  FREIGHT_INWARDSAMOUNT_TRANSPORTATION_OF_ITEMS_YOU_PURCHASE = 29;
    public static final int  PETTY_CASH = 30;
    public static final int  TDS_192_PAYMENT_OF_SALARY = 31;
    public static final int  TDS_194A_INCOME_BY_WAY_OF_INTEREST = 32;
    public static final int  TDS_194C_INDIVIDUALS_HUF = 33;
    public static final int  TDS_194C_OTHERS = 34;
    public static final int  TDS_194H_COMMISSION = 35;
    public static final int  TDS_194I_RENT_MACHINERY = 36;
    public static final int  TDS_194I_RENT_LAND = 37;
    public static final int  TDS_194J = 38;
    public static final int  SGST_INPUT_RECEIVABLE = 39;
    public static final int  CGST_INPUT_RECEIVABLE = 40;
    public static final int  IGST_INPUT_RECEIVABLE = 41;
    public static final int  CESS_INPUT_RECEIVABLE = 42;
    public static final int  SGST_OUTPUT_PAYABLE = 43;
    public static final int  CGST_OUTPUT_PAYABLE = 44;
    public static final int  IGST_OUTPUT_PAYABLE = 45;
    public static final int  CESS_OUTPUT_PAYABLE = 46;
    public static final int  SGST_RCM_OUT = 47;
    public static final int  CGST_RCM_OUT = 48;
    public static final int  IGST_RCM_OUT = 49;
    public static final int  CESS_RCM_OUT = 50;
    public static final int  ROUNDING_OFF_INCOMES = 51;
    public static final int  COMBINATION_SALES_USING_INCOME_ITEMS = 52;
    public static final int  SGST_RCM_INPUT = 53;
    public static final int  CGST_RCM_INPUT = 54;
    public static final int  IGST_RCM_INPUT = 55;
    public static final int  CESS_RCM_INPUT = 56;
    public static final int INTER_BRANCH_ACCOUNTS = 57;
    public static final int PAYROLL_SALARIES_EXPENSES = 58;
    public static final int PAYROLL_SALARIES_DEDUCTIONS = 59;
    public static final int EMPLOYEE_ADVANCES_PAID = 60;
    public static final int EMPLOYEE_CLAIMS_PAYABLE = 61;
    public static final int DISCOUNT_ALLOWED_MAPPING_ID = 63;
    public static final int DISCOUNT_RECEIVED_MAPPING_ID = 64;
    public static final ArrayList <Integer> ONLY_CHILD_COA = new ArrayList<>();
    static {
        ONLY_CHILD_COA.add(CUSTOMER_ACCOUNT_DEBTORS);
        ONLY_CHILD_COA.add(VENDOR_ACCOUNT_CREDITORS);
        ONLY_CHILD_COA.add(CASH_BALANCES);
        ONLY_CHILD_COA.add(BANK_BALANCES);
        ONLY_CHILD_COA.add(ADVANCE_RECEIVED_FROM_CUSTOMERS_DEBTORS);
        ONLY_CHILD_COA.add(ADVANCE_PAID_TO_VENDORS_CREDITORS);
        ONLY_CHILD_COA.add(TRAVEL_EXPENSES);
        ONLY_CHILD_COA.add(BOARDING_AND_LODGING_EXPENSES);
        ONLY_CHILD_COA.add(OTHER_TRAVEL_EXPENSES);
        ONLY_CHILD_COA.add(FIXED_TRAVEL_ALLOWANCES);
        ONLY_CHILD_COA.add(PETTY_CASH);
        ONLY_CHILD_COA.add(SGST_INPUT_RECEIVABLE);
        ONLY_CHILD_COA.add(CGST_INPUT_RECEIVABLE);
        ONLY_CHILD_COA.add(IGST_INPUT_RECEIVABLE);
        ONLY_CHILD_COA.add(CESS_INPUT_RECEIVABLE);
        ONLY_CHILD_COA.add(SGST_OUTPUT_PAYABLE);
        ONLY_CHILD_COA.add(CGST_OUTPUT_PAYABLE);
        ONLY_CHILD_COA.add(IGST_OUTPUT_PAYABLE);
        ONLY_CHILD_COA.add(CESS_OUTPUT_PAYABLE);
        ONLY_CHILD_COA.add(SGST_RCM_OUT);
        ONLY_CHILD_COA.add(CGST_RCM_OUT);
        ONLY_CHILD_COA.add(IGST_RCM_OUT);
        ONLY_CHILD_COA.add(CESS_RCM_OUT);
        ONLY_CHILD_COA.add(SGST_RCM_INPUT);
        ONLY_CHILD_COA.add(CGST_RCM_INPUT);
        ONLY_CHILD_COA.add(IGST_RCM_INPUT);
        ONLY_CHILD_COA.add(CESS_RCM_INPUT);
        ONLY_CHILD_COA.add(INTER_BRANCH_ACCOUNTS);
        ONLY_CHILD_COA.add(PAYROLL_SALARIES_EXPENSES);
        ONLY_CHILD_COA.add(EMPLOYEE_CLAIMS_PAYABLE);
    }
}
