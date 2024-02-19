package com.idos.dao;

import com.idos.util.DateUtil;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import controllers.StaticController;
import model.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by Sunil K. Namdev on 25-11-2017.
 */
public class AgeingReportDAOImpl implements AgeingReportDAO {

    private static final List<Long> rpfcList = new ArrayList<Long>(1);
    private static final List<Long> ptvList = new ArrayList<Long>(1);
    private static final List<Long> cnfcList = new ArrayList<Long>(1);
    private static final List<Long> dnfvList = new ArrayList<Long>(1);
    private static final List<String> txnStatusList = new ArrayList<String>(2);
    static {
        rpfcList.add(IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER);
        cnfcList.add(IdosConstants.CREDIT_NOTE_CUSTOMER);
        txnStatusList.add(IdosConstants.NOT_PAID);
        txnStatusList.add(IdosConstants.PARTLY_PAID);
        ptvList.add(IdosConstants.PAY_VENDOR_SUPPLIER);
        dnfvList.add(IdosConstants.DEBIT_NOTE_VENDOR);
    }

    @Override
    public List<AgeingReport> getAgeingReportList(String ageingDate, Users user, EntityManager em, boolean isPayable)
            throws IDOSException {
        List<AgeingReport> reportList = null;
        try {
            Date toDate = null;
            if (ageingDate != null && ageingDate != "") {
                // ageingDate =
                // IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(ageingDate));
                toDate = IdosConstants.IDOSDF.parse(ageingDate);
            } else {
                toDate = Calendar.getInstance().getTime();
                // ageingDate = toDate.toString();
            }

            String AGE_JPQL = REC_SQL;
            if (isPayable) {
                AGE_JPQL = PAY_SQL;
            }
            Query query = em.createNativeQuery(AGE_JPQL);
            query.setParameter(1, user.getOrganization().getId());
            if (isPayable) {
                query.setParameter(2, 4l);
            } else {
                query.setParameter(2, 2l);
            }
            query.setParameter(3, toDate);

            /*
             * query.setParameter(4, user.getOrganization().getId());
             * query.setParameter(5, toDate);
             * query.setParameter(6, user.getOrganization().getId());
             * query.setParameter(7, toDate);
             */

            List<Object[]> txnList = query.getResultList();
            if (txnList.size() > 0) {
                reportList = new ArrayList<AgeingReport>();
            }
            double totalAmount = 0.0, totalDue = 0.0, totalRec = 0.0, total0_30 = 0.0, total31_60 = 0.0,
                    total61_90 = 0.0, total91_180 = 0.0;
            double totalOver180 = 0.0, totalOverdue = 0.0;
            int creditDays = 0;
            AgeingReport ageingReport = null;
            for (Object[] txn : txnList) {
                creditDays = 0;

                ageingReport = new AgeingReport();
                Date txnDate = null;
                Vendor vendor = null;
                if (txn[0] != null) {
                    Long custVendID = Long.parseLong(String.valueOf(txn[0]));
                    vendor = Vendor.findById(custVendID);
                    if (vendor != null) {
                        ageingReport.setVendCust(vendor.getName());
                        if (vendor.getDaysForCredit() != null) {
                            ageingReport.setCreditPeriod(vendor.getDaysForCredit());
                            creditDays = vendor.getDaysForCredit();
                        }
                    }
                } else if (txn[1] != null) {
                    ageingReport.setVendCust(String.valueOf(txn[1]));
                }
                Long branchid = 0L;
                if (txn[2] != null) {
                    branchid = Long.parseLong(String.valueOf(txn[2]));
                    Branch branch = Branch.findById(branchid);
                    ageingReport.setBranch(branch.getName());
                }
                if (txn[3] != null) {
                    ageingReport.setInvoice((String) txn[3]);
                }
                Double crdtTxnTotalAmt = 0.0;
                if (txn[4] != null) {
                    String txnRef = (String) txn[4];
                    /*
                     * if(vendor != null) {
                     * List<Long> purposeList = new ArrayList<>(1);
                     * purposeList.add(IdosConstants.CREDIT_NOTE_CUSTOMER);
                     * List<String> paystatuList = new ArrayList<>(1);
                     * paystatuList.add(IdosConstants.NOT_PAID);
                     * List<Transaction> cdrtTxnList =
                     * TRANSACTION_DAO.findByOrgCustVendPaymentStatusLinkedTxn(user.getOrganization(
                     * ).getId(), branchid, vendor.getId(), purposeList, paystatuList,
                     * IdosConstants.TXN_STATUS_ACCOUNTED, txnRef, em);
                     * for(Transaction crdtTxn : cdrtTxnList){
                     * crdtTxnTotalAmt += crdtTxn.getNetAmount();
                     * }
                     * }
                     */
                    ageingReport.setTxnRef(txnRef);
                }
                if (txn[5] != null) {
                    ageingReport.setAccDate(IdosConstants.REPORTDF.format((java.sql.Date) txn[5]));
                    txnDate = IdosConstants.MYSQLDF.parse(String.valueOf(txn[5]));
                }
                Double netAmount = 0.0;
                if (txn[6] != null) {
                    netAmount = (Double) txn[6];
                    ageingReport.setNetAmount(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(netAmount)));
                    totalAmount += netAmount;
                }
                Double netPayment = 0.0;
                if (txn[7] != null) {
                    netPayment = (Double) txn[7];
                    ageingReport.setAmtReceived(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(netPayment)));
                    totalRec += netPayment;
                }
                Double dueAmt = 0.0;
                if (txn[8] != null) {
                    dueAmt = (Double) txn[8];
                }

                if (dueAmt > 0.0) {
                    ageingReport.setAmountDue(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(dueAmt)));
                    totalDue += dueAmt;
                } else {
                    dueAmt = netAmount;
                }
                long txnPurpose = Long.parseLong(String.valueOf(txn[9]));
                if (txnPurpose == 20L) {
                    if (isPayable) {
                        ageingReport.setAmountDue(ageingReport.getNetAmount());
                        totalDue += netAmount;
                    } else {
                        ageingReport.setAmtReceived(ageingReport.getNetAmount());
                        totalRec += netAmount;
                    }
                }

                if (txn[11] != null) {
                    String poRefNo = String.valueOf(txn[11]);
                    if (poRefNo != null) {
                        ageingReport.setPoReference(poRefNo);
                    }
                }

                if (toDate.getTime() >= txnDate.getTime()) {
                    long daysBetween = DateUtil.calculateDays(IdosConstants.IDOSDF.format(txnDate), ageingDate);
                    ageingReport.setDaysO_s(daysBetween);
                    if (dueAmt > 0.0) {
                        if (daysBetween <= 30) {
                            ageingReport.setDays0_30(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(dueAmt)));
                            total0_30 += dueAmt;
                        } else if (daysBetween > 30 && daysBetween <= 60) {
                            ageingReport.setDays31_60(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(dueAmt)));
                            total31_60 += dueAmt;
                        } else if (daysBetween > 60 && daysBetween <= 90) {
                            ageingReport.setDays61_90(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(dueAmt)));
                            total61_90 += dueAmt;
                        } else if (daysBetween > 90 && daysBetween <= 180) {
                            ageingReport.setDays91_180(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(dueAmt)));
                            total91_180 += dueAmt;
                        } else if (daysBetween > 180) {
                            ageingReport
                                    .setOver180Days(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(dueAmt)));
                            totalOver180 += dueAmt;
                        }
                        if (daysBetween > creditDays) {
                            ageingReport.setOverdue(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(dueAmt)));
                            totalOverdue += dueAmt;
                        }
                    }
                }
                if (isPayable) {
                    ageingReport.setVendCustHead("Vendor");
                    ageingReport.setRecInvHead("Receipt");
                    ageingReport.setRecPaidHead("Amount Paid");
                    ageingReport.setCommissionHead("SOLV Commission");
                    ageingReport.setPaymentDateHead("Seller Payment Date");
                    ageingReport.setTotalAmtHead("Amount Paid to Seller");
                } else {
                    ageingReport.setVendCustHead("Customer");
                    ageingReport.setRecInvHead("Invoice No.");
                    ageingReport.setRecPaidHead("Received");
                    ageingReport.setPaymentDateHead("Payment Collection Date");
                    ageingReport.setTotalAmtHead("Total Amount Collected");
                }
                ageingReport.setPayMethodHead("Cheque No./ UPI UTR/ NEFT/RTGS UTR");
                reportList.add(ageingReport);
            }
            if (ageingReport != null) {
                ageingReport.setTotalAmount(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(totalAmount)));
                ageingReport.setTotalDue(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(totalDue)));
                ageingReport.setTotalRec(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(totalRec)));
                ageingReport.setTotal0_30(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(total0_30)));
                ageingReport.setTotal31_60(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(total31_60)));
                ageingReport.setTotal61_90(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(total61_90)));
                ageingReport.setTotal91_180(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(total91_180)));
                ageingReport.setTotalOver180(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(totalOver180)));
                ageingReport.setTotalOverdue(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(totalOverdue)));
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "error", ex);
            throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION,
                    "Data format error", ex.getMessage());
        }
        return reportList;
    }

    @Override
    public List<AgeingReport> getAgeingReport2List(String ageingDate, Users user, EntityManager em, boolean isPayable)
            throws IDOSException {
        List<AgeingReport> reportList = null;
        try {
            Date toDate = null;
            if (ageingDate != null && ageingDate != "") {
                // ageingDate =
                // IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(ageingDate));
                toDate = IdosConstants.IDOSDF.parse(ageingDate);
            } else {
                toDate = Calendar.getInstance().getTime();
                // ageingDate = toDate.toString();
            }

            ArrayList inparams = new ArrayList(3);
            inparams.add(user.getOrganization().getId());
            if (isPayable) {
                inparams.add(IdosConstants.BUY_ON_CREDIT_PAY_LATER);
            } else {
                inparams.add(IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER);
            }
            inparams.add(toDate);

            List<Transaction> txnList = genericDao.queryWithParamsName(TEMPLATE_JPQL, em, inparams);
            if (txnList.size() > 0) {
                reportList = new ArrayList<AgeingReport>();
            }
            double totalAmount = 0.0, totalDue = 0.0, totalRec = 0.0, total0_30 = 0.0, total31_60 = 0.0,
                    total61_90 = 0.0, total91_180 = 0.0;
            double totalOver180 = 0.0, totalOverdue = 0.0;
            int creditDays = 0;
            AgeingReport ageingReport = null;
            for (Transaction txn : txnList) {
                creditDays = 0;
                ageingReport = new AgeingReport();
                Vendor vendor = null;
                if (txn.getTransactionVendorCustomer() != null) {
                    vendor = txn.getTransactionVendorCustomer();
                    if (vendor != null) {
                        ageingReport.setVendCust(vendor.getName());
                        if (vendor.getDaysForCredit() != null) {
                            ageingReport.setCreditPeriod(vendor.getDaysForCredit());
                            creditDays = vendor.getDaysForCredit();
                        }
                    }
                } else {
                    ageingReport.setVendCust(txn.getTransactionUnavailableVendorCustomer());
                }

                ageingReport.setBranch(txn.getTransactionBranch().getName());
                ageingReport.setInvoice(txn.getInvoiceNumber());
                ageingReport.setTxnRef(txn.getTransactionRefNumber());
                ageingReport.setPoReference(txn.getPoReference());
                ageingReport.setAccDate(IdosConstants.REPORTDF2.format(txn.getTransactionDate()));
                ageingReport.setCreditPeriod(creditDays);
                Double noteTxnAmt = 0.0;
                Double netAmount = 0.0;
                Double netPayment = 0.0;
                Double dueAmt = 0.0;
                Double finalDueAmt = 0.0;
                if (isPayable) {
                    netAmount = txn.getNetAmount() == null ? 0.0 : txn.getNetAmount();
                    netPayment = txn.getVendorNetPayment() == null ? 0.0 : txn.getVendorNetPayment();
                    dueAmt = txn.getVendorDuePayment() == null ? 0.0 : txn.getVendorDuePayment();
                    noteTxnAmt = txn.getPurchaseReturnAmount() == null ? 0.0 : txn.getPurchaseReturnAmount();
                    Double discount = txn.getAvailableDiscountAmountForTxn() == null ? 0.0
                            : txn.getAvailableDiscountAmountForTxn();
                    netPayment = netPayment + discount;
                    ageingReport.setVendCustHead("Vendor");
                    ageingReport.setRecInvHead("Receipt");
                    ageingReport.setRecPaidHead("Amount Paid");
                    ageingReport.setCommissionHead("SOLV Commission");
                    ageingReport.setPaymentDateHead("Seller Payment Date");
                    ageingReport.setTotalAmtHead("Amount Paid to Seller");
                    ageingReport.setCrdtDbtTxnHead("Debit Note");
                } else {
                    netAmount = txn.getNetAmount() == null ? 0.0 : txn.getNetAmount();
                    if (txn.getAdjustmentFromAdvance() != null) {
                        netAmount = netAmount - txn.getAdjustmentFromAdvance();
                    }
                    netPayment = txn.getCustomerNetPayment() == null ? 0.0 : txn.getCustomerNetPayment();
                    dueAmt = txn.getCustomerDuePayment() == null ? 0.0 : txn.getCustomerDuePayment();
                    noteTxnAmt = txn.getSalesReturnAmount() == null ? 0.0 : txn.getSalesReturnAmount();

                    ageingReport.setVendCustHead("Customer");
                    ageingReport.setRecInvHead("Invoice No.");
                    ageingReport.setRecPaidHead("Received");
                    ageingReport.setPaymentDateHead("Payment Collection Date");
                    ageingReport.setTotalAmtHead("Total Amount Collected");
                    ageingReport.setCrdtDbtTxnHead("Credit Note");
                }
                finalDueAmt = dueAmt - netPayment - noteTxnAmt;
                if (finalDueAmt <= 0.0) {
                    continue;
                }
                ageingReport.setNetAmount(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(netAmount)));
                ageingReport.setAmtReceived(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(netPayment)));
                ageingReport.setAmountDue(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(finalDueAmt)));
                ageingReport.setCrdtDbtTxnAmt(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(noteTxnAmt)));
                totalAmount += netAmount;
                totalRec += netPayment;
                totalDue += dueAmt;
                ageingReport.setPayMethodHead("Cheque No./ UPI UTR/ NEFT/RTGS UTR");
                reportList.add(ageingReport);
            }
            if (ageingReport != null) {
                ageingReport.setTotalAmount(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(totalAmount)));
                ageingReport.setTotalDue(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(totalDue)));
                ageingReport.setTotalRec(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(totalRec)));
                ageingReport.setTotal0_30(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(total0_30)));
                ageingReport.setTotal31_60(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(total31_60)));
                ageingReport.setTotal61_90(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(total61_90)));
                ageingReport.setTotal91_180(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(total91_180)));
                ageingReport.setTotalOver180(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(totalOver180)));
                ageingReport.setTotalOverdue(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(totalOverdue)));
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "error", ex);
            throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION,
                    "Data format error", ex.getMessage());
        }
        return reportList;
    }

    @Override
    public List<AgeingReport> getBrsTempList(String ageingDate, long bankid, Users user, EntityManager em,
            boolean isPayable) throws IDOSException {
        List<AgeingReport> reportList = null;
        try {
            Date toDate = null;
            if (ageingDate != null && ageingDate != "") {
                toDate = IdosConstants.IDOSDF.parse(ageingDate);
            } else {
                toDate = Calendar.getInstance().getTime();
            }
            ArrayList inparams = new ArrayList(3);
            inparams.add(user.getOrganization().getId());
            inparams.add(bankid);
            inparams.add(toDate);

            List<Transaction> txnList = genericDao.queryWithParamsName(BRS_JPQL, em, inparams);
            if (txnList.size() > 0) {
                reportList = new ArrayList<AgeingReport>();
            }

            AgeingReport brsReport = null;
            for (Transaction txn : txnList) {
                if (txn.getNetAmount() == null)
                    continue;
                brsReport = new AgeingReport();
                if (txn.getTransactionVendorCustomer() != null) {
                    Vendor vendor = txn.getTransactionVendorCustomer();
                    if (vendor != null) {
                        brsReport.setVendCust(vendor.getName());
                    }
                } else {
                    brsReport.setVendCust(txn.getTransactionUnavailableVendorCustomer());
                }
                brsReport.setBranch(txn.getTransactionBranch().getName());
                brsReport.setInvoice(txn.getInvoiceNumber());
                brsReport.setTxnRef(txn.getTransactionRefNumber());
                brsReport.setPoReference(txn.getPoReference());
                brsReport.setAccDate(IdosConstants.REPORTDF2.format(txn.getTransactionDate()));
                brsReport.setTxnPurpose(txn.getTransactionPurpose().getTransactionPurpose());
                Double netAmount = txn.getNetAmount();
                brsReport.setAmtReceived(Double.parseDouble(IdosConstants.DECIMAL_FORMAT.format(netAmount)));
                brsReport.setBranch(txn.getTransactionBranchBankAccount().getBankName());
                reportList.add(brsReport);
                if (reportList.size() == 1) {
                    brsReport.setRecInvHead("Invoice/Receipt No.");
                    brsReport.setRecPaidHead("Amount Received/Paid");
                    brsReport.setVendCustHead("Customer/Vendor");
                }
            }

        } catch (Exception ex) {
            log.log(Level.SEVERE, "error", ex);
            throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION,
                    "Data format error", ex.getMessage());
        }
        return reportList;
    }

}
