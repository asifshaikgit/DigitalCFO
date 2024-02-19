package com.idos.dao;

import com.idos.util.IDOSException;
import model.AgeingReport;
import model.Users;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.List;

/**
 * Created by Sunil K. Namdev on 25-11-2017.
 */
public interface AgeingReportDAO extends BaseDAO {

        String REC_SQL = ("select t.vendcust, t.walkin, t.branch, t.invoice, t.refnumber, t.date, t.amount, t.netpayment, t.dueamount, t.txnpurpose, t.txnid, poref from ( select a.TRANSACTION_VENDOR_CUSTOMER as vendcust, a.TRANSACTION_UNAVAILABLE_VENDOR_CUSTOMER as walkin, a.TRANSACTION_BRANCH as branch, a.INVOICE_NUMBER as invoice, a.TRANSACTION_REF_NUMBER as refnumber, a.TRANSACTION_ACTIONDATE as date, a.NET_AMOUNT as amount, a.CUSTOMER_NET_PAYMENT as netpayment, a.CUSTOMER_DUE_PAYMENT as dueamount, a.TRANSACTION_PURPOSE as txnpurpose, ID as txnid, a.PO_REFERENCE as poref from TRANSACTION a WHERE a.TRANSACTION_BRANCH_ORGANIZATION = ?1 AND a.TRANSACTION_PURPOSE = ?2 and a.TRANSACTION_STATUS = 'Accounted' and a.NET_AMOUNT > 0 and (a.PAYMENT_STATUS = 'NOT-PAID' or a.PAYMENT_STATUS = 'PARTLY-PAID') and a.PRESENT_STATUS = 1 and a.TRANSACTION_ACTIONDATE <= ?3 ) as t order by t.date");
        /*
         * .append(" ( select a.TRANSACTION_VENDOR_CUSTOMER as vendcust, a.TRANSACTION_UNAVAILABLE_VENDOR_CUSTOMER as walkin, a.TRANSACTION_BRANCH as branch, a.INVOICE_NUMBER as invoice, a.TRANSACTION_REF_NUMBER as refnumber, a.TRANSACTION_ACTIONDATE as date, a.NET_AMOUNT as amount, a.CUSTOMER_NET_PAYMENT as netpayment, a.CUSTOMER_DUE_PAYMENT as dueamount, a.TRANSACTION_PURPOSE as txnpurpose, ID as txnid from TRANSACTION a WHERE a.TRANSACTION_BRANCH_ORGANIZATION = =?x AND a.TRANSACTION_PURPOSE = =?x and a.TRANSACTION_STATUS = 'Accounted' and a.NET_AMOUNT > 0 and (a.PAYMENT_STATUS = 'NOT-PAID' or a.PAYMENT_STATUS = 'PARTLY-PAID') and a.TRANSACTION_ACTIONDATE <= =?x union all "
         * )
         * .append(" select t2.HEAD_ID as vendcust, null as walkin, t1.DEBIT_BRANCH as branch, null as invoice, t1.TRANSACTION_REF_NUMBER as refnumber, t1.TRANSACTION_DATE as date, t2.HEAD_AMOUNT as amount, null as netpayment, null as dueamount, t1.TRANSACTION_PURPOSE as txnpurpose, t1.ID as txnid from PROVISION_JOURNAL_ENTRY t1, PROVISION_JOURNAL_ENTRY_DETAIL t2 WHERE t1.BRANCH_ORGANIZATION = =?x and t1.ID = t2.PROVISION_JOURNAL_ENTRY_ID AND t1.TRANSACTION_STATUS = 'Accounted' and t2.IS_DEBIT=1 and t1.TRANSACTION_DATE <= =?x and (t2.HEAD_TYPE='cust' or t2.HEAD_TYPE='cAdv') union all "
         * )
         * .append(" select t2.HEAD_ID as vendcust, null as walkin, t1.CREDIT_BRANCH as branch, null as invoice, t1.TRANSACTION_REF_NUMBER as refnumber, t1.TRANSACTION_DATE as date, t2.HEAD_AMOUNT as amount, null as netpayment, null as dueamount, t1.TRANSACTION_PURPOSE as txnpurpose, t1.ID as txnid from PROVISION_JOURNAL_ENTRY t1, PROVISION_JOURNAL_ENTRY_DETAIL t2 WHERE t1.BRANCH_ORGANIZATION = =?x and t1.ID = t2.PROVISION_JOURNAL_ENTRY_ID AND t1.TRANSACTION_STATUS = 'Accounted' and t2.IS_DEBIT=0 and t1.TRANSACTION_DATE <= =?x and (t2.HEAD_TYPE='cust' or t2.HEAD_TYPE='cAdv')) as t order by t.date"
         * );
         */

        // String REC_SQL = ageRecJPQL.toString();

        String PAY_SQL = ("select t.vendcust, t.walkin, t.branch, t.invoice, t.refnumber, t.date, t.amount, t.netpayment, t.dueamount, t.txnpurpose, t.txnid, poref from ( select a.TRANSACTION_VENDOR_CUSTOMER as vendcust, a.TRANSACTION_UNAVAILABLE_VENDOR_CUSTOMER as walkin, a.TRANSACTION_BRANCH as branch, a.INVOICE_NUMBER as invoice, a.TRANSACTION_REF_NUMBER as refnumber, a.TRANSACTION_ACTIONDATE as date, a.NET_AMOUNT as amount, a.VENDOR_NET_PAYMENT as netpayment, a.VENDOR_DUE_PAYMENT as dueamount, a.TRANSACTION_PURPOSE as txnpurpose, ID as txnid, a.PO_REFERENCE as poref from TRANSACTION a WHERE a.TRANSACTION_BRANCH_ORGANIZATION = ?1 AND a.TRANSACTION_PURPOSE = ?2 and a.TRANSACTION_STATUS = 'Accounted' and a.NET_AMOUNT > 0 and (a.PAYMENT_STATUS = 'NOT-PAID' or a.PAYMENT_STATUS = 'PARTLY-PAID') and a.PRESENT_STATUS = 1 and a.TRANSACTION_ACTIONDATE <= ?3)  as t order by t.date");
        /*
         * .append(" ( select a.TRANSACTION_VENDOR_CUSTOMER as vendcust, a.TRANSACTION_UNAVAILABLE_VENDOR_CUSTOMER as walkin, a.TRANSACTION_BRANCH as branch, a.INVOICE_NUMBER as invoice, a.TRANSACTION_REF_NUMBER as refnumber, a.TRANSACTION_ACTIONDATE as date, a.NET_AMOUNT as amount, a.VENDOR_NET_PAYMENT as netpayment, a.VENDOR_DUE_PAYMENT as dueamount, a.TRANSACTION_PURPOSE as txnpurpose, ID as txnid from TRANSACTION a WHERE a.TRANSACTION_BRANCH_ORGANIZATION = =?x AND a.TRANSACTION_PURPOSE = =?x and a.TRANSACTION_STATUS = 'Accounted' and a.NET_AMOUNT > 0 and (a.PAYMENT_STATUS = 'NOT-PAID' or a.PAYMENT_STATUS = 'PARTLY-PAID') and a.TRANSACTION_ACTIONDATE <= =?x union all "
         * )
         * .append(" select t2.HEAD_ID as vendcust, null as walkin, t1.DEBIT_BRANCH as branch, null as invoice, t1.TRANSACTION_REF_NUMBER as refnumber, t1.TRANSACTION_DATE as date, t2.HEAD_AMOUNT as amount, null as netpayment, null as dueamount, t1.TRANSACTION_PURPOSE as txnpurpose, t1.ID as txnid from PROVISION_JOURNAL_ENTRY t1, PROVISION_JOURNAL_ENTRY_DETAIL t2 WHERE t1.BRANCH_ORGANIZATION = ?x and t1.ID = t2.PROVISION_JOURNAL_ENTRY_ID AND t1.TRANSACTION_STATUS = 'Accounted' and t2.IS_DEBIT=1 and t1.TRANSACTION_DATE <= ?x and (t2.HEAD_TYPE= 'vend' or t2.HEAD_TYPE='vAdv') union all "
         * )
         * .append(" select t2.HEAD_ID as vendcust, null as walkin, t1.CREDIT_BRANCH as branch, null as invoice, t1.TRANSACTION_REF_NUMBER as refnumber, t1.TRANSACTION_DATE as date, t2.HEAD_AMOUNT as amount, null as netpayment, null as dueamount, t1.TRANSACTION_PURPOSE as txnpurpose, t1.ID as txnid from PROVISION_JOURNAL_ENTRY t1, PROVISION_JOURNAL_ENTRY_DETAIL t2 WHERE t1.BRANCH_ORGANIZATION = ?x  and t1.ID = t2.PROVISION_JOURNAL_ENTRY_ID AND t1.TRANSACTION_STATUS = 'Accounted' and t2.IS_DEBIT=0 and t1.TRANSACTION_DATE <= ?x and (t2.HEAD_TYPE= 'vend' or t2.HEAD_TYPE='vAdv')) as t order by t.date"
         * );
         */
        // String PAY_SQL = agePayJPQL.toString();

        public List<AgeingReport> getAgeingReportList(String ageingDate, Users user, EntityManager entityManager,
                        boolean isPayable) throws IDOSException;

        String TEMPLATE_JPQL = "from Transaction a WHERE a.transactionBranchOrganization.id = ?1 AND a.transactionPurpose.id = ?2 and a.transactionDate <= ?3 and a.transactionStatus = 'Accounted' and a.netAmount > 0 and (a.paymentStatus = 'NOT-PAID' or a.paymentStatus = 'PARTLY-PAID') and a.presentStatus = 1 order by a.transactionDate";

        public List<AgeingReport> getAgeingReport2List(String ageingDate, Users user, EntityManager entityManager,
                        boolean isPayable) throws IDOSException;

        List<AgeingReport> getBrsTempList(String ageingDate, long bankid, Users user, EntityManager em,
                        boolean isPayable) throws IDOSException;

        String BRS_JPQL = "from Transaction a WHERE a.transactionBranchOrganization.id = ?1 AND a.transactionPurpose.id in (5,7) and a.transactionBranchBankAccount.id = ?2  and a.transactionDate <= ?3 and a.transactionStatus = 'Accounted' and a.netAmount > 0 and a.presentStatus = 1 order by a.transactionDate";
}
