package com.idos.dao;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;

import model.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.logging.Level;
import play.db.jpa.JPAApi;
import play.libs.Json;
import javax.inject.Inject;
import com.idos.util.FileUtil;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import com.idos.util.NumberToWordsInt;

import controllers.TransactionController;

public class InvoiceDAOImpl implements InvoiceDAO {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public Map<String, Object> getInvoiceParams(Transaction transaction, JsonNode json, EntityManager entityManager)
            throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        Map<String, Object> criterias = new HashMap<String, Object>();
        if (transaction.getTransactionVendorCustomer() != null) {
            if (transaction.getTransactionVendorCustomer().getName() != null) {
                params.put("customerName", transaction.getTransactionVendorCustomer().getName());
            }
            if (transaction.getTransactionVendorCustomer().getAddress() != null) {
                params.put("customerAddress", transaction.getTransactionVendorCustomer().getAddress());
            }
            if (transaction.getTransactionVendorCustomer().getEmail() != null) {
                params.put("customerEmail", transaction.getTransactionVendorCustomer().getEmail());
            }
            if (transaction.getTransactionVendorCustomer().getPhone() != null) {
                params.put("customerPhNo", transaction.getTransactionVendorCustomer().getPhone());
            }

            if ((transaction.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                    || transaction.getTransactionPurpose()
                            .getId() == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER)) {
                CustomerDetail customerDetail = CustomerDetail.findByCustomerID(entityManager,
                        transaction.getTransactionVendorCustomer().getId());
                if (customerDetail != null) {
                    if (customerDetail.getShippingaddress() != null) {
                        params.put("shippingAddress", customerDetail.getShippingaddress());
                    }
                    if (customerDetail.getShippingphone() != null) {
                        params.put("shippingPhNo", customerDetail.getShippingphone());
                    }
                } else {
                    if (transaction.getTransactionVendorCustomer().getAddress() != null) {
                        params.put("shippingAddress", transaction.getTransactionVendorCustomer().getAddress());
                    }
                    if (transaction.getTransactionVendorCustomer().getPhone() != null) {
                        params.put("shippingPhNo", transaction.getTransactionVendorCustomer().getPhone());
                    }
                }
            }
            if (transaction.getPoReference() != null) {
                params.put("POReference", transaction.getPoReference());
            } else {
                params.put("POReference", "");
            }
            if (transaction.getTransactionDate() != null) {
                params.put("invoiceDate", IdosConstants.IDOSDF.format(transaction.getTransactionDate()));
            } else {
                params.put("invoiceDate", "");
            }

            StringBuilder remarks = new StringBuilder("");
            if (transaction.getRemarks() != null) {
                if (transaction.getRemarks().indexOf("|") != -1) {
                    String rem[] = transaction.getRemarks().split("\\|");
                    for (int j = 0; j < rem.length; j++) {
                        if (rem[j].indexOf("#") != -1) {
                            String strPart[] = rem[j].split("#");
                            if (strPart.length > 1)
                                remarks.append(strPart[1]);
                        } else {
                            remarks.append(rem[j]);
                        }
                        remarks.append(",");
                    }
                } else {
                    if (transaction.getRemarks().indexOf("#") != -1) {
                        String strPart[] = transaction.getRemarks().split("#");
                        if (strPart.length > 1)
                            remarks.append(strPart[1]);
                    }
                }
            }
            params.put("publicRemarks", remarks.toString());

            // tax ids
            if (transaction.getTransactionVendorCustomer().getStatutoryNumber1() != null) {
                params.put("customerTaxId1", transaction.getTransactionVendorCustomer().getStatutoryName1() + ": "
                        + transaction.getTransactionVendorCustomer().getStatutoryNumber1());
            } else {
                params.put("customerTaxId1", "");
            }
            if (transaction.getTransactionVendorCustomer().getStatutoryNumber2() != null) {
                params.put("customerTaxId2", transaction.getTransactionVendorCustomer().getStatutoryName2() + ": "
                        + transaction.getTransactionVendorCustomer().getStatutoryNumber2());
            } else {
                params.put("customerTaxId2", "");
            }
            if (transaction.getTransactionVendorCustomer().getStatutoryNumber3() != null) {
                params.put("customerTaxId3", transaction.getTransactionVendorCustomer().getStatutoryName3() + ": "
                        + transaction.getTransactionVendorCustomer().getStatutoryNumber3());
            } else {
                params.put("customerTaxId3", "");
            }
            if (transaction.getTransactionVendorCustomer().getStatutoryNumber4() != null) {
                params.put("customerTaxId4", transaction.getTransactionVendorCustomer().getStatutoryName4() + ": "
                        + transaction.getTransactionVendorCustomer().getStatutoryNumber4());
            } else {
                params.put("customerTaxId4", "");
            }
            params.put("netInvValueAfterTax", IdosConstants.decimalFormat.format(transaction.getNetAmount())); // this
                                                                                                               // is
                                                                                                               // after
                                                                                                               // advance
                                                                                                               // adjustments,
                                                                                                               // so we
                                                                                                               // are
                                                                                                               // calculating
                                                                                                               // without
                                                                                                               // advance
                                                                                                               // in
                                                                                                               // below
                                                                                                               // function
                                                                                                               // and
                                                                                                               // setting
                                                                                                               // correct
                                                                                                               // value
                                                                                                               // without
                                                                                                               // advance
                                                                                                               // in
                                                                                                               // InvoiceVoucherController
                                                                                                               // again
            criterias.clear();
            criterias.put("branch.id", transaction.getTransactionBranch().getId());
            criterias.put("organization.id", transaction.getTransactionBranchOrganization().getId());
            criterias.put("isStatutoryAvailableForInvoice", 1);
            criterias.put("presentStatus", 1);
            List<StatutoryDetails> branchStatutories = genericDao.findByCriteria(StatutoryDetails.class, criterias,
                    entityManager);
            for (int i = 0; i < branchStatutories.size(); i++) {
                if (branchStatutories.get(i) != null) {
                    if (i == 0) {
                        params.put("statutory1", branchStatutories.get(i).getStatutoryDetails() + ":"
                                + branchStatutories.get(i).getRegistrationNumber());
                    }
                    if (i == 1) {
                        params.put("statutory2", branchStatutories.get(i).getStatutoryDetails() + ":"
                                + branchStatutories.get(i).getRegistrationNumber());
                    }
                    if (i == 2) {
                        params.put("statutory3", branchStatutories.get(i).getStatutoryDetails() + ":"
                                + branchStatutories.get(i).getRegistrationNumber());
                    }
                    if (i == 3) {
                        params.put("statutory4", branchStatutories.get(i).getStatutoryDetails() + ":"
                                + branchStatutories.get(i).getRegistrationNumber());
                    }
                }
            }
        } else {
            params.put("customerName", transaction.getTransactionUnavailableVendorCustomer());
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

            params.put("shippingAddress", shipcustomerAddress + ", " + shipVendLocation);
            params.put("shippingPhNo", shipVendPhone);
        }
        if (transaction.getTransactionBranchOrganization() != null) {
            String companyLogo = FileUtil.getCompanyLogo(transaction.getTransactionBranchOrganization());
            if (companyLogo != null && !"".equals(companyLogo)) {
                params.put("companyLogo", companyLogo);
            }
            if (transaction.getTransactionBranchOrganization().getName() != null) {
                params.put("companyName", transaction.getTransactionBranchOrganization().getName());
            }
            if (transaction.getTransactionBranchOrganization().getRegisteredAddress() != null) {
                String address = transaction.getTransactionBranchOrganization().getRegisteredAddress()
                        .replaceAll("\\r\\n|\\r|\\n", " ");
                params.put("companyAddress", address);
            }
            if (transaction.getTransactionBranchOrganization().getCorporateMail() != null) {
                params.put("companyEmail", transaction.getTransactionBranchOrganization().getCorporateMail());
            }
            if (transaction.getTransactionBranchOrganization().getRegisteredPhoneNumber() != null) {
                params.put("companyPhNo", transaction.getTransactionBranchOrganization().getRegisteredPhoneNumber());
            }
        }
        if (transaction.getInvoiceNumber() != null) {
            params.put("invoiceNumber", transaction.getInvoiceNumber());
        } else {
            params.put("invoiceNumber", "");
        }
        params.put("poweredBy", ConfigParams.getInstance().getPoweredBy());
        return params;
    }

    @Override
    public List<InvoiceReportModel> getSellTxnInvoice(Organization org, Transaction transaction,
            EntityManager entityManager) {
        List<InvoiceReportModel> listInvoiceReport = new ArrayList<InvoiceReportModel>();
        Map<String, Object> criterias = new HashMap<String, Object>();
        if (transaction != null) {
            criterias.put("transaction.id", transaction.getId());
            criterias.put("presentStatus", 1);
            List<TransactionItems> listTransactionItems = genericDao.findByCriteria(TransactionItems.class, criterias,
                    entityManager);
            if (listTransactionItems != null && listTransactionItems.size() > 0) {
                String taxName1 = "Tax1";
                String taxName2 = "Tax2";
                String taxName3 = "Tax3";
                String taxName4 = "Tax4";
                String taxName5 = "Tax5";
                String taxNames[] = new String[5];
                // get all branch taxes
                criterias.clear();
                criterias.put("taxType", 2);
                criterias.put("branch.id", transaction.getTransactionBranch().getId());
                criterias.put("organization.id", org.getId());
                criterias.put("presentStatus", 1);
                List<BranchTaxes> branchTaxesList = genericDao.findByCriteria(BranchTaxes.class, criterias,
                        entityManager);
                for (int i = 0; i < 5; i++) {
                    if (branchTaxesList.size() > i) {
                        taxNames[i] = branchTaxesList.get(i).getTaxName();
                    } else {
                        int j = i + 1;
                        taxNames[i] = "Tax" + j;
                    }
                }
                double totalNetAmtWithoutAdv = 0.0;
                for (TransactionItems txnItemrow : listTransactionItems) {
                    InvoiceReportModel invRepTransactionItem = new InvoiceReportModel();
                    if (txnItemrow.getNoOfUnits() != null) {
                        invRepTransactionItem.setUnits(String.valueOf(txnItemrow.getNoOfUnits()));
                    }
                    if (txnItemrow.getPricePerUnit() != null) {
                        invRepTransactionItem
                                .setPricePerUnit(IdosConstants.decimalFormat.format(txnItemrow.getPricePerUnit()));
                    } else {
                        invRepTransactionItem.setPricePerUnit("00.00");
                    }
                    if (txnItemrow.getGrossAmount() != null) {
                        invRepTransactionItem.setTotal(IdosConstants.decimalFormat.format(txnItemrow.getGrossAmount()));
                    } else {
                        invRepTransactionItem.setTotal("00.00");
                    }
                    String description1 = null;
                    if (txnItemrow.getTransactionSpecifics() != null
                            && txnItemrow.getTransactionSpecifics().getIsInvoiceDescription1() != null
                            && txnItemrow.getTransactionSpecifics().getIsInvoiceDescription1() == 1) {
                        description1 = txnItemrow.getTransactionSpecifics().getInvoiceItemDescription1();
                    }
                    description1 = description1 == null ? "" : description1;
                    String description2 = null;
                    if (txnItemrow.getTransactionSpecifics() != null
                            && txnItemrow.getTransactionSpecifics().getIsInvoiceDescription2() != null
                            && txnItemrow.getTransactionSpecifics().getIsInvoiceDescription2() == 1) {
                        description2 = txnItemrow.getTransactionSpecifics().getInvoiceItemDescription2();
                    }
                    description2 = description2 == null ? "" : description2;
                    if (txnItemrow.getTransactionSpecifics() != null
                            && txnItemrow.getTransactionSpecifics().getName() != null) {
                        invRepTransactionItem.setItemName(txnItemrow.getTransactionSpecifics().getName() + " "
                                + description1 + " " + description2);
                    } else {
                        invRepTransactionItem.setItemName(description1 + " " + description2);
                    }
                    if (txnItemrow.getDiscountPercent() != null && !"".equals(txnItemrow.getDiscountPercent())) {
                        invRepTransactionItem.setDiscountPer(IdosConstants.decimalFormat
                                .format(Double.parseDouble(txnItemrow.getDiscountPercent())));
                    }
                    if (txnItemrow.getDiscountAmount() != null && !"".equals(txnItemrow.getDiscountAmount())) {
                        invRepTransactionItem
                                .setDiscountAmt(IdosConstants.decimalFormat.format(txnItemrow.getDiscountAmount()));
                    }
                    double netAmtWithoutAdv = 0.0;
                    if (txnItemrow.getGrossAmount() != null) {
                        netAmtWithoutAdv += txnItemrow.getGrossAmount();
                    }
                    if (txnItemrow.getTotalTax() != null) {
                        netAmtWithoutAdv += txnItemrow.getTotalTax();
                    }
                    totalNetAmtWithoutAdv = totalNetAmtWithoutAdv + netAmtWithoutAdv;
                    invRepTransactionItem.setGrossInvoiceValue(IdosConstants.decimalFormat.format(netAmtWithoutAdv));
                    // VAT 1(+10.0%):5.0,Net Tax:5.00,
                    int k = 0;
                    String val = null;
                    String txnnetamountdescription = txnItemrow.getTaxDescription();
                    if (txnnetamountdescription != null && !txnnetamountdescription.equals("")
                            && !txnnetamountdescription.contains("undefined")) {
                        String inputtaxvalarr[] = txnnetamountdescription.split(",");
                        for (int i = 0; i < inputtaxvalarr.length; i++) {
                            if (inputtaxvalarr[i] == null || "".equals(inputtaxvalarr[i].trim())) {
                                continue;
                            }
                            String inputtaxvalwithstrarr[] = inputtaxvalarr[i].split(":");
                            String str = inputtaxvalwithstrarr[0];
                            str = str.replaceAll("\\(.*?\\) ?", "");
                            val = inputtaxvalwithstrarr[1] == null ? "0" : inputtaxvalwithstrarr[1];
                            for (int j = 0; j < taxNames.length; j++) {
                                if (str.equalsIgnoreCase(taxNames[j])) {
                                    k = j;
                                }
                            }
                            switch (k) {
                                case 0:
                                    invRepTransactionItem
                                            .setTaxValue1(IdosConstants.decimalFormat.format(Double.parseDouble(val)));
                                    break;
                                case 1:
                                    invRepTransactionItem
                                            .setTaxValue2(IdosConstants.decimalFormat.format(Double.parseDouble(val)));
                                    break;
                                case 2:
                                    invRepTransactionItem
                                            .setTaxValue3(IdosConstants.decimalFormat.format(Double.parseDouble(val)));
                                    break;
                                case 3:
                                    invRepTransactionItem
                                            .setTaxValue4(IdosConstants.decimalFormat.format(Double.parseDouble(val)));
                                    break;
                                case 4:
                                    invRepTransactionItem
                                            .setTaxValue5(IdosConstants.decimalFormat.format(Double.parseDouble(val)));
                                    break;
                            }
                        }
                    }
                    if (txnItemrow.getTransactionSpecifics() != null
                            && txnItemrow.getTransactionSpecifics().getIncomeUnitsMeasure() != null) {
                        invRepTransactionItem
                                .setUnitOfMeasure(txnItemrow.getTransactionSpecifics().getIncomeUnitsMeasure());
                    } else {
                        invRepTransactionItem.setUnitOfMeasure("");
                    }
                    listInvoiceReport.add(invRepTransactionItem);
                }
                // set tax names in first record, as it appears as label. So even if first sell
                // item is freight outward which has no taxes,
                // but third record has 4 taxes set, then in lable we should see names of those
                // 4 taxes
                InvoiceReportModel taxNameInvoiceModel = listInvoiceReport.get(0);
                taxNameInvoiceModel.setTaxName1(taxNames[0]);
                taxNameInvoiceModel.setTaxName2(taxNames[1]);
                taxNameInvoiceModel.setTaxName3(taxNames[2]);
                taxNameInvoiceModel.setTaxName4(taxNames[3]);
                taxNameInvoiceModel.setTaxName5(taxNames[4]);

                long totalNetAmtWithoutAdvLong = Math.round(totalNetAmtWithoutAdv);
                taxNameInvoiceModel.setNetAmt(IdosConstants.decimalFormat.format(totalNetAmtWithoutAdvLong));
            }
        }
        return listInvoiceReport;
    }

    @Override
    public List<InvoiceReportModel> getSellTxnGstInvoice(Organization org, Transaction transaction,
            EntityManager entityManager, JsonNode json, TransactionInvoice invoiceLog) {
        log.log(Level.FINE, "****** Start");
        List<InvoiceReportModel> listInvoiceReport = new ArrayList<InvoiceReportModel>();
        Map<String, Object> criterias = new HashMap<String, Object>();
        Double txnTotalTaxAmount = 0.0;
        try {
            if (transaction != null) {
                Double currencyCovRate = 1.0;
                if (invoiceLog != null) {
                    currencyCovRate = invoiceLog.getCurrencyConvRate();
                } else {
                    String currencyRate = json.findValue("currencyRate") == null ? null
                            : json.findValue("currencyRate").asText();
                    if (currencyRate != null && !"".equals(currencyRate)) {
                        currencyCovRate = Double.parseDouble(currencyRate);
                    }
                }

                criterias.put("transaction.id", transaction.getId());
                criterias.put("presentStatus", 1);
                List<TransactionItems> listTransactionItems = genericDao.findByCriteria(TransactionItems.class,
                        criterias, entityManager);
                if (listTransactionItems != null && listTransactionItems.size() > 0) {
                    double totalNetAmtWithoutAdv = 0.0;
                    int counter = 1;
                    double totalTaxableValue = 0.0, totalInvValAftrTax = 0d;
                    Double totalTax1 = 0d, totalTax2 = 0d, totalTax3 = 0d, totalTax4 = 0d;
                    Double dueAmtBfrRound = 0d;
                    for (TransactionItems txnItemrow : listTransactionItems) {
                        Double totalTaxForItem = 0d;
                        // if combination sales item, then we need to show all child items e.g. Laptop=
                        // RAM + Monitor then show only RAM and Monitor and not Laptop in the invoice
                        if (txnItemrow.getTransactionSpecifics() != null
                                && txnItemrow.getTransactionSpecifics().getIsCombinationSales() != null
                                && txnItemrow.getTransactionSpecifics().getIsCombinationSales() == 1) {
                            StringBuilder newsbquery = new StringBuilder(); // specificId = laptop and combSpecificId =
                                                                            // RAM, Monitor etc
                            newsbquery
                                    .append("select obj from SpecificsCombinationSales obj WHERE obj.specificsId.id = '"
                                            + txnItemrow.getTransactionSpecifics().getId()
                                            + "' and obj.organization.id ='"
                                            + transaction.getTransactionBranchOrganization().getId()
                                            + "' and obj.presentStatus=1");
                            List<SpecificsCombinationSales> specificsList = genericDao
                                    .executeSimpleQuery(newsbquery.toString(), entityManager);
                            InvoiceReportModel invoiceRecord;
                            for (SpecificsCombinationSales combSpec : specificsList) {
                                invoiceRecord = new InvoiceReportModel();
                                invoiceRecord.setItemNo(String.valueOf(counter));
                                counter++;
                                createInvoiceRecordForCombinationSellTran(combSpec, combSpec.getCombSpecificsId(),
                                        invoiceRecord, txnItemrow, transaction, entityManager);
                                listInvoiceReport.add(invoiceRecord);
                                dueAmtBfrRound += Double.valueOf(invoiceRecord.getNetAmt());
                                if (invoiceRecord.getTaxValue1() != null || !invoiceRecord.getTaxValue1().equals("")) {
                                    totalTax1 += Double.valueOf(invoiceRecord.getTaxValue1()); // sgst
                                }
                                if (invoiceRecord.getTaxValue2() != null || !invoiceRecord.getTaxValue2().equals("")) {
                                    totalTax2 += Double.valueOf(invoiceRecord.getTaxValue2()); // cgst
                                }
                                if (invoiceRecord.getTaxValue3() != null || !invoiceRecord.getTaxValue3().equals("")) {
                                    totalTax3 += Double.valueOf(invoiceRecord.getTaxValue3()); // igst
                                }
                                if (invoiceRecord.getTaxableAmount() != null
                                        || !invoiceRecord.getTaxableAmount().equals("")) {
                                    totalTaxableValue += Double.valueOf(invoiceRecord.getTaxableAmount());
                                }
                            }
                            InvoiceReportModel taxNameInvoiceModel0 = listInvoiceReport.get(0);
                            taxNameInvoiceModel0.setTaxName1("SGST");
                            taxNameInvoiceModel0.setTaxName2("CGST");
                            taxNameInvoiceModel0.setTaxName3("IGST");
                            taxNameInvoiceModel0.setTaxName4("CESS");
                            taxNameInvoiceModel0.setTaxName5("Tax5");
                            taxNameInvoiceModel0
                                    .setTotalDueAmtBfrRound(IdosConstants.decimalFormat.format(dueAmtBfrRound));
                        } else {
                            InvoiceReportModel invoiceRecord = new InvoiceReportModel();
                            invoiceRecord.setItemNo(String.valueOf(counter));
                            counter++;
                            String description1 = null;
                            if (txnItemrow.getTransactionSpecifics() != null
                                    && txnItemrow.getTransactionSpecifics().getIsInvoiceDescription1() != null
                                    && txnItemrow.getTransactionSpecifics().getIsInvoiceDescription1() == 1) {
                                description1 = txnItemrow.getTransactionSpecifics().getInvoiceItemDescription1();
                            }
                            description1 = description1 == null ? "" : description1;
                            String description2 = null;
                            if (txnItemrow.getTransactionSpecifics() != null
                                    && txnItemrow.getTransactionSpecifics().getIsInvoiceDescription2() != null
                                    && txnItemrow.getTransactionSpecifics().getIsInvoiceDescription2() == 1) {
                                description2 = txnItemrow.getTransactionSpecifics().getInvoiceItemDescription2();
                            }
                            description2 = description2 == null ? "" : description2;
                            if (txnItemrow.getTransactionSpecifics() != null
                                    && txnItemrow.getTransactionSpecifics().getName() != null) {
                                invoiceRecord.setItemName(txnItemrow.getTransactionSpecifics().getName() + " "
                                        + description1 + " " + description2);
                            } else {
                                invoiceRecord.setItemName(description1 + " " + description2);
                            }
                            String tmpItemName = IdosUtil.unescapeHtml(invoiceRecord.getItemName());
                            invoiceRecord.setItemName(tmpItemName);
                            if (txnItemrow.getTransactionSpecifics() != null) {
                                invoiceRecord.setHsnCode(txnItemrow.getTransactionSpecifics().getGstItemCode());
                            }
                            invoiceRecord.setUnits(String.valueOf(txnItemrow.getNoOfUnits()));
                            invoiceRecord
                                    .setUnitOfMeasure(txnItemrow.getTransactionSpecifics().getIncomeUnitsMeasure());
                            if (txnItemrow.getPricePerUnit() != null) {
                                invoiceRecord.setPricePerUnit(
                                        IdosConstants.decimalFormat.format(txnItemrow.getPricePerUnit()));
                            }
                            // invoiceRecord.setTotal(IdosConstants.decimalFormat.format(txnItemrow.getGrossAmount()));

                            if (txnItemrow.getDiscountPercent() != null
                                    && !"".equals(txnItemrow.getDiscountPercent())) {
                                invoiceRecord.setDiscountPer(IdosConstants.decimalFormat
                                        .format(Double.parseDouble(txnItemrow.getDiscountPercent())));
                            }
                            if (txnItemrow.getDiscountAmount() != null) {
                                invoiceRecord.setDiscountAmt(
                                        IdosConstants.decimalFormat.format(txnItemrow.getDiscountAmount()));
                            }
                            if (txnItemrow.getGrossAmount() != null) {
                                invoiceRecord.setTaxableAmount(
                                        IdosConstants.decimalFormat.format(txnItemrow.getGrossAmount()));
                            }
                            totalTaxableValue += txnItemrow.getGrossAmount();
                            double netAmtWithoutAdv = 0.0;
                            if (txnItemrow.getGrossAmount() != null) {
                                netAmtWithoutAdv += txnItemrow.getGrossAmount();
                            }
                            if (txnItemrow.getTotalTax() != null) {
                                netAmtWithoutAdv += txnItemrow.getTotalTax();
                            }
                            totalNetAmtWithoutAdv = totalNetAmtWithoutAdv + netAmtWithoutAdv;
                            invoiceRecord.setGrossInvoiceValue(IdosConstants.decimalFormat.format(netAmtWithoutAdv));
                            for (int count = 1; count < 5; count++) {
                                Method method = TransactionItems.class.getMethod("getTaxName" + count);
                                String taxNameTmp = (String) method.invoke(txnItemrow);
                                if (taxNameTmp != null && taxNameTmp.indexOf("SGST") != -1) {
                                    Method taxValueMth = TransactionItems.class.getMethod("getTaxValue" + count);
                                    Double taxValueTmp = (Double) taxValueMth.invoke(txnItemrow);
                                    Method taxRateMth = TransactionItems.class.getMethod("getTaxRate" + count);
                                    Double taxRateTmp = (Double) taxRateMth.invoke(txnItemrow);
                                    if (taxValueTmp != null) {
                                        invoiceRecord.setTaxValue1(IdosConstants.decimalFormat.format(taxValueTmp));
                                        totalTax1 += taxValueTmp;
                                        totalTaxForItem += taxValueTmp;
                                    }
                                    if (taxRateTmp != null) {
                                        invoiceRecord.setTaxRate1(IdosConstants.decimalFormat.format(taxRateTmp));
                                    }
                                } else if (taxNameTmp != null && taxNameTmp.indexOf("CGST") != -1) {
                                    Method taxValueMth = TransactionItems.class.getMethod("getTaxValue" + count);
                                    Double taxValueTmp = (Double) taxValueMth.invoke(txnItemrow);
                                    Method taxRateMth = TransactionItems.class.getMethod("getTaxRate" + count);
                                    Double taxRateTmp = (Double) taxRateMth.invoke(txnItemrow);
                                    if (taxValueTmp != null) {
                                        invoiceRecord.setTaxValue2(IdosConstants.decimalFormat.format(taxValueTmp));
                                        totalTax2 += taxValueTmp;
                                        totalTaxForItem += taxValueTmp;
                                    }
                                    if (taxRateTmp != null) {
                                        invoiceRecord.setTaxRate2(IdosConstants.decimalFormat.format(taxRateTmp));
                                    }
                                } else if (taxNameTmp != null && taxNameTmp.indexOf("IGST") != -1) {
                                    Method taxValueMth = TransactionItems.class.getMethod("getTaxValue" + count);
                                    Double taxValueTmp = (Double) taxValueMth.invoke(txnItemrow);
                                    Method taxRateMth = TransactionItems.class.getMethod("getTaxRate" + count);
                                    Double taxRateTmp = (Double) taxRateMth.invoke(txnItemrow);
                                    if (taxValueTmp != null) {
                                        invoiceRecord.setTaxValue3(IdosConstants.decimalFormat.format(taxValueTmp));
                                        totalTax3 += taxValueTmp;
                                        totalTaxForItem += taxValueTmp;
                                    }
                                    if (taxRateTmp != null) {
                                        invoiceRecord.setTaxRate3(IdosConstants.decimalFormat.format(taxRateTmp));
                                    }
                                } else if (taxNameTmp != null && taxNameTmp.indexOf("CESS") != -1) {
                                    Method taxValueMth = TransactionItems.class.getMethod("getTaxValue" + count);
                                    Double taxValueTmp = (Double) taxValueMth.invoke(txnItemrow);
                                    Method taxRateMth = TransactionItems.class.getMethod("getTaxRate" + count);
                                    Double taxRateTmp = (Double) taxRateMth.invoke(txnItemrow);
                                    if (taxValueTmp != null) {
                                        invoiceRecord.setTaxValue4(IdosConstants.decimalFormat.format(taxValueTmp));
                                        totalTax4 += taxValueTmp;
                                        totalTaxForItem += taxValueTmp;
                                    }
                                    if (taxRateTmp != null) {
                                        invoiceRecord.setTaxRate4(IdosConstants.decimalFormat.format(taxRateTmp));
                                    }
                                }
                            }
                            if (txnItemrow.getTaxValue5() != null) {
                                invoiceRecord
                                        .setTaxValue5(IdosConstants.decimalFormat.format(txnItemrow.getTaxValue5()));
                            }
                            if (txnItemrow.getTaxRate5() != null) {
                                invoiceRecord.setTaxRate5(IdosConstants.decimalFormat.format(txnItemrow.getTaxRate5()));
                            }
                            if (transaction.getTransactionPurpose()
                                    .getId() == IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER
                                    || transaction.getTransactionPurpose()
                                            .getId() == IdosConstants.REFUND_ADVANCE_RECEIVED) {
                                if (transaction.getTransactionPurpose()
                                        .getId() == IdosConstants.REFUND_ADVANCE_RECEIVED) {
                                    invoiceRecord.setNetAmt(IdosConstants.decimalFormat
                                            .format(txnItemrow.getGrossAmount() - totalTaxForItem)); // With tds Amount
                                } else {
                                    invoiceRecord.setNetAmt(IdosConstants.decimalFormat
                                            .format(txnItemrow.getNetAmount() - totalTaxForItem));
                                }
                            } else {
                                invoiceRecord.setNetAmt(IdosConstants.decimalFormat.format(txnItemrow.getNetAmount()));
                            }
                            // invoiceRecord.setTaxName1("SGST");
                            invoiceRecord.setTaxName1("SGST");
                            invoiceRecord.setTaxName2("CGST");
                            invoiceRecord.setTaxName3("IGST");
                            invoiceRecord.setTaxName4("CESS");
                            invoiceRecord.setTaxName5("Tax5");
                            dueAmtBfrRound += txnItemrow.getNetAmount();
                            invoiceRecord.setTotalDueAmtBfrRound(IdosConstants.decimalFormat.format(dueAmtBfrRound));
                            listInvoiceReport.add(invoiceRecord);
                        }
                    }
                    // set tax names in first record, as it appears as label. So even if first sell
                    // item is freight outward which has no taxes,
                    // but third record has 4 taxes set, then in lable we should see names of those
                    // 4 taxes
                    /*
                     * InvoiceReportModel taxNameInvoiceModel0 = listInvoiceReport.get(0);
                     * taxNameInvoiceModel0.setTaxName1("SGST");
                     * taxNameInvoiceModel0.setTaxName2("CGST");
                     * taxNameInvoiceModel0.setTaxName3("IGST");
                     * taxNameInvoiceModel0.setTaxName4("CESS");
                     * taxNameInvoiceModel0.setTaxName5("Tax5");
                     * taxNameInvoiceModel0.setTotalDueAmtBfrRound(IdosConstants.decimalFormat.
                     * format(dueAmtBfrRound));
                     */
                    InvoiceReportModel taxNameInvoiceModel = listInvoiceReport.get(listInvoiceReport.size() - 1);
                    if (totalTax1 > 0) {
                        taxNameInvoiceModel.setTotalTaxAmt1(IdosConstants.decimalFormat.format(totalTax1));
                        totalInvValAftrTax += totalTax1;
                    }
                    if (totalTax2 > 0) {
                        taxNameInvoiceModel.setTotalTaxAmt2(IdosConstants.decimalFormat.format(totalTax2));
                        totalInvValAftrTax += totalTax2;
                    }
                    if (totalTax3 > 0) {
                        taxNameInvoiceModel.setTotalTaxAmt3(IdosConstants.decimalFormat.format(totalTax3));
                        totalInvValAftrTax += totalTax3;
                    }
                    if (totalTax4 > 0) {
                        taxNameInvoiceModel.setTotalTaxAmt4(IdosConstants.decimalFormat.format(totalTax4));
                        totalInvValAftrTax += totalTax4;
                    }
                    // long totalTaxableVal = Math.round(totalTaxableValue);
                    taxNameInvoiceModel.setTotalTaxableAmount(IdosConstants.decimalFormat.format(totalTaxableValue));
                    if (transaction.getTransactionPurpose().getId() == IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER
                            || transaction.getTransactionPurpose().getId() == IdosConstants.REFUND_ADVANCE_RECEIVED) {
                        Double totalNetAmtTmp = 0d;
                        // long totalNetAmtWithoutAdvLong = Math.round(totalNetAmtTmp);
                        if (transaction.getTransactionPurpose().getId() == IdosConstants.REFUND_ADVANCE_RECEIVED) {
                            totalNetAmtTmp = transaction.getGrossAmount() - totalTax1 - totalTax2 - totalTax3
                                    - totalTax4; // With TDS Amount
                        } else {
                            totalNetAmtTmp = transaction.getNetAmount() - totalTax1 - totalTax2 - totalTax3 - totalTax4;
                        }
                        taxNameInvoiceModel.setTotalNetAmount(IdosConstants.decimalFormat.format(totalNetAmtTmp));
                        totalInvValAftrTax = transaction.getGrossAmount();
                    } else {
                        long totalNetAmtWithoutAdvLong = Math.round(totalNetAmtWithoutAdv);
                        taxNameInvoiceModel.setNetAmt(IdosConstants.decimalFormat.format(totalNetAmtWithoutAdvLong));
                        totalInvValAftrTax += totalTaxableValue;
                        if (transaction.getTypeOfSupply() != null && transaction.getTypeOfSupply() == 3) {
                            if (currencyCovRate != null && currencyCovRate > 1.0) {
                                totalInvValAftrTax = totalInvValAftrTax / currencyCovRate;
                            } else if (currencyCovRate != null && currencyCovRate < 1.0) {
                                totalInvValAftrTax = totalInvValAftrTax * currencyCovRate;
                            }
                            String destCurrencyCode = null;
                            if (invoiceLog != null) {
                                if (invoiceLog.getCurrencyCode() != null) {
                                    destCurrencyCode = invoiceLog.getCurrencyCode();
                                }
                            } else {
                                destCurrencyCode = json.findValue("destCurrencyCode") == null ? ""
                                        : json.findValue("destCurrencyCode").asText();
                            }
                            taxNameInvoiceModel.setTotalInvoiceLbl("Total Invoice Value(" + destCurrencyCode + "):");
                        }
                    }
                    long totalInvValAftrTaxLong = Math.round(totalInvValAftrTax);
                    taxNameInvoiceModel.setNetInvValAftrTax(IdosConstants.decimalFormat.format(totalInvValAftrTaxLong));
                    taxNameInvoiceModel
                            .setNetInvValAftrTaxWord(NumberToWordsInt.convert(totalInvValAftrTaxLong) + " Only.");
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        log.log(Level.FINE, "****** End " + listInvoiceReport);
        return listInvoiceReport;
    }

    private void createInvoiceRecordForCombinationSellTran(SpecificsCombinationSales combSpec, Specifics sellSpecific,
            InvoiceReportModel invoiceRecord, TransactionItems txnItemrow, Transaction transaction,
            EntityManager entityManager) throws IDOSException {
        String description1 = null;
        if (sellSpecific != null && sellSpecific.getIsInvoiceDescription1() != null
                && sellSpecific.getIsInvoiceDescription1() == 1) {
            description1 = sellSpecific.getInvoiceItemDescription1();
        }
        description1 = description1 == null ? "" : description1;
        String description2 = null;
        if (sellSpecific != null && sellSpecific.getIsInvoiceDescription2() != null
                && sellSpecific.getIsInvoiceDescription2() == 1) {
            description2 = sellSpecific.getInvoiceItemDescription2();
        }
        description2 = description2 == null ? "" : description2;
        if (sellSpecific != null && sellSpecific.getName() != null) {
            invoiceRecord.setItemName(sellSpecific.getName() + " " + description1 + " " + description2);
        } else {
            invoiceRecord.setItemName(description1 + " " + description2);
        }
        String tmpItemName = IdosUtil.unescapeHtml(invoiceRecord.getItemName());
        invoiceRecord.setItemName(tmpItemName);
        if (sellSpecific != null && sellSpecific.getGstItemCode() != null) {
            invoiceRecord.setHsnCode(sellSpecific.getGstItemCode());
        }
        double openBalUnits = txnItemrow.getNoOfUnits() * combSpec.getOpeningBalUnits();
        double grossAmtForThisItem = openBalUnits * combSpec.getOpeningBalRate();
        invoiceRecord.setUnits(String.valueOf(openBalUnits));
        invoiceRecord.setUnitOfMeasure(sellSpecific.getIncomeUnitsMeasure());
        if (txnItemrow.getPricePerUnit() != null) {
            invoiceRecord.setPricePerUnit(IdosConstants.decimalFormat.format(combSpec.getOpeningBalRate()));
        }
        // invoiceRecord.setTotal(IdosConstants.decimalFormat.format(txnItemrow.getGrossAmount()));

        if (txnItemrow.getDiscountPercent() != null && !"".equals(txnItemrow.getDiscountPercent())) {
            invoiceRecord.setDiscountPer(
                    IdosConstants.decimalFormat.format(Double.parseDouble(txnItemrow.getDiscountPercent())));
        }
        if (txnItemrow.getDiscountAmount() != null && !"".equals(txnItemrow.getDiscountAmount())) {
            invoiceRecord.setDiscountAmt(IdosConstants.decimalFormat.format(txnItemrow.getDiscountAmount()));
        }
        invoiceRecord.setTaxableAmount(IdosConstants.decimalFormat.format(grossAmtForThisItem));

        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("branch.id", transaction.getTransactionBranch().getId());
        criterias.put("organization.id", transaction.getTransactionBranchOrganization().getId());
        criterias.put("specifics.id", sellSpecific.getId());
        criterias.put("presentStatus", 1);
        List<BranchSpecificsTaxFormula> bnchSpecfTaxFormula = genericDao.findByCriteria(BranchSpecificsTaxFormula.class,
                criterias, entityManager);
        ObjectNode row = Json.newObject();
        String txnSourceGstinCode = transaction.getSourceGstin() == null ? "" : transaction.getSourceGstin();
        String txnDestGstinCode = transaction.getDestinationGstin() == null ? "" : transaction.getDestinationGstin();
        int txnTypeOfSupply = transaction.getTypeOfSupply() == null ? 0 : transaction.getTypeOfSupply();
        int txnWithWithoutTax = transaction.getWithWithoutTax() == null ? null : transaction.getWithWithoutTax();
        ArrayList<Double> taxamt = new ArrayList<Double>();
        taxamt.add(0, 0.0); // SGST
        taxamt.add(1, 0.0);
        taxamt.add(2, 0.0);
        TransactionController.getNetAmountTaxComponentForCombinationSell(txnSourceGstinCode, txnDestGstinCode,
                txnTypeOfSupply, txnWithWithoutTax, sellSpecific, bnchSpecfTaxFormula, grossAmtForThisItem, row,
                taxamt);
        invoiceRecord.setTaxValue1(IdosConstants.decimalFormat.format(taxamt.get(0)));
        invoiceRecord.setTaxRate1(IdosConstants.decimalFormat.format(0.0));
        invoiceRecord.setTaxValue2(IdosConstants.decimalFormat.format(taxamt.get(1)));
        invoiceRecord.setTaxValue3(IdosConstants.decimalFormat.format(taxamt.get(2)));
        double taxNetAmtForThisItem = taxamt.get(0) + taxamt.get(1) + taxamt.get(2);
        double netAmtWithoutAdv = grossAmtForThisItem + taxNetAmtForThisItem;
        invoiceRecord.setGrossInvoiceValue(IdosConstants.decimalFormat.format(netAmtWithoutAdv));
        invoiceRecord.setTotalTaxableAmount(IdosConstants.decimalFormat.format(taxNetAmtForThisItem));
        invoiceRecord.setNetAmt(IdosConstants.decimalFormat.format(netAmtWithoutAdv));
    }

    @Override
    public List<InvoiceAdvanceModel> getAdvTxnGstInvoice(Organization org, Transaction transaction,
            EntityManager entityManager) throws IDOSException {
        log.log(Level.FINE, "****** Start");
        if (transaction == null) {
            return null;
        }
        String ADVADJHQL = "select sum(adjustedAmount), sum(advAdjTax1Value), sum(advAdjTax2Value), sum(advAdjTax3Value), sum(advAdjTax4Value), advTransaction.id from AdvanceAdjustmentDetail obj where obj.transaction.id=?1 and obj.presentStatus=1 group by advTransaction.id";

        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "HQL: " + ADVADJHQL);
        List<InvoiceAdvanceModel> advInvoiceList = new ArrayList<InvoiceAdvanceModel>();
        try {
            Query query = entityManager.createQuery(ADVADJHQL);
            query.setParameter(1, transaction.getId());
            List<Object[]> aadTxnLists = query.getResultList();
            for (Object[] aad : aadTxnLists) {
                Double advAdjAmount = Double.parseDouble(String.valueOf(aad[0] == null ? 0.0 : aad[0]));
                Double tax1 = Double.parseDouble(String.valueOf(aad[1] == null ? 0.0 : aad[1]));
                Double tax2 = Double.parseDouble(String.valueOf(aad[2] == null ? 0.0 : aad[2]));
                Double tax3 = Double.parseDouble(String.valueOf(aad[3] == null ? 0.0 : aad[3]));
                Double tax4 = Double.parseDouble(String.valueOf(aad[4] == null ? 0.0 : aad[4]));
                long advRecTaxID = Long.parseLong(String.valueOf(aad[5] == null ? 0.0 : aad[5]));
                Transaction advRecTxn = Transaction.findById(advRecTaxID);
                Double taxableValue = advAdjAmount - (tax1 + tax2 + tax3 + tax4);
                InvoiceAdvanceModel invoiceRecord = new InvoiceAdvanceModel();
                invoiceRecord.setArvNo(advRecTxn.getInvoiceNumber());
                invoiceRecord.setAdvDate(IdosConstants.IDOSDF.format(advRecTxn.getTransactionDate()));
                invoiceRecord.setAdvanceAdjusted(IdosConstants.decimalFormat.format(advAdjAmount));
                invoiceRecord.setAdvTaxableValue(IdosConstants.decimalFormat.format(taxableValue));
                if (tax1 > 0.0)
                    invoiceRecord.setAdvAdjTax1(IdosConstants.decimalFormat.format(tax1));
                if (tax2 > 0.0)
                    invoiceRecord.setAdvAdjTax2(IdosConstants.decimalFormat.format(tax2));
                if (tax3 > 0.0)
                    invoiceRecord.setAdvAdjTax3(IdosConstants.decimalFormat.format(tax3));
                if (tax4 > 0.0)
                    invoiceRecord.setAdvAdjTax4(IdosConstants.decimalFormat.format(tax4));
                advInvoiceList.add(invoiceRecord);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF, IdosConstants.TECHNICAL_EXCEPTION,
                    IdosConstants.NULL_KEY_EXC_ESMF_MSG, "Error on fetching advance for invoice.");
        }
        log.log(Level.FINE, "****** End " + advInvoiceList);
        return advInvoiceList;
    }

    @Override
    public Map<String, Object> getGstInvoiceParams(Transaction transaction, JsonNode json,
            List<InvoiceReportModel> dataList, TransactionInvoice invoiceLog, Users user, EntityManager entityManager)
            throws Exception {
        if (transaction == null) {
            return null;
        }
        Integer invoiceHeading = null;
        String titleUnregisteredSupplier = "SELF INVOICE FOR PURCHASE FROM UNREGISTERED SUPPLIER";
        String titleRegisteredSupplier = "SELF INVOICE FOR PURCHASE FROM REGISTERED SUPPLIER";

        long txnPurpose = transaction.getTransactionPurpose().getId();
        String dateofgoodsremove = null;
        if (invoiceLog != null && invoiceLog.getDateRemovalGoods() != null) {
            dateofgoodsremove = IdosConstants.IDOSDF.format(invoiceLog.getDateRemovalGoods());
        } else {
            dateofgoodsremove = json.findValue("dateofgoodsremove") == null ? null
                    : json.findValue("dateofgoodsremove").asText();
        }
        String numgoodsremove = null;
        if (invoiceLog != null && invoiceLog.getApplNumberGoodsRemoval() != null) {
            numgoodsremove = invoiceLog.getApplNumberGoodsRemoval();
        } else {
            numgoodsremove = json.findValue("numgoodsremove") == null ? null
                    : json.findValue("numgoodsremove").asText();
        }
        String currencyRate = null;
        if (invoiceLog != null && invoiceLog.getCurrencyConvRate() != null) {
            currencyRate = invoiceLog.getCurrencyConvRate().toString();
        } else {
            currencyRate = json.findValue("currencyRate") == null ? null : json.findValue("currencyRate").asText();
        }
        String destCurrencyCode = null;
        if (invoiceLog != null) {
            destCurrencyCode = invoiceLog.getCurrencyCode();
        } else {
            destCurrencyCode = json.findValue("destCurrencyCode") == null ? null
                    : json.findValue("destCurrencyCode").asText();
        }
        Map<String, Object> params = new HashMap<String, Object>();
        Branch branch = Branch.findById(transaction.getTransactionBranch().getId());
        StringBuilder branchDetail = new StringBuilder("");
        if (branch != null) {
            if (branch.getAddress() != null)
                branchDetail.append(branch.getAddress());
            if (branch.getLocation() != null)
                branchDetail.append(", ").append(branch.getLocation());
            if (branch.getStateCode() != null)
                branchDetail.append(", ").append(IdosConstants.STATE_CODE_MAPPING.get(branch.getStateCode()));
            params.put("branchAddress", IdosUtil.replaceFormatingChar(branchDetail.toString()));
            params.put("branchGstin", branch.getGstin());
        }
        if (transaction.getTransactionDate() != null) {
            params.put("invoiceDate", IdosConstants.IDOSDF.format(transaction.getTransactionDate()));
        } else {
            params.put("invoiceDate", "");
        }

        if (transaction.getPoReference() != null) {
            params.put("POReference", transaction.getPoReference());
        } else {
            params.put("POReference", "");
        }
        if (transaction.getInvoiceNumber() != null) {
            params.put("invoiceNumber", transaction.getInvoiceNumber());
        } else {
            params.put("invoiceNumber", "");
        }

        String ecomGstin = null;
        if (txnPurpose == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                || txnPurpose == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
                || txnPurpose == IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER
                || txnPurpose == IdosConstants.CREDIT_NOTE_CUSTOMER || txnPurpose == IdosConstants.DEBIT_NOTE_CUSTOMER
                || txnPurpose == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER
                || txnPurpose == IdosConstants.CANCEL_INVOICE) {
            Double totalTaxAmount = 0.0;
            for (TransactionItems txnItem : transaction.getTransactionItems()) {
                totalTaxAmount = txnItem.getTotalTax() == null ? 0.0 : txnItem.getTotalTax();
            }

            String datetimeOfShipping = null;
            if (invoiceLog != null && invoiceLog.getDatetimeOfSupply() != null) {
                datetimeOfShipping = IdosConstants.IDOSDTF.format(invoiceLog.getDatetimeOfSupply());
            } else {
                datetimeOfShipping = json.findValue("datetimeOfShipping") == null ? null
                        : json.findValue("datetimeOfShipping").asText();
            }
            String transportMode = null;
            if (invoiceLog != null) {
                transportMode = invoiceLog.getTranportationMode();
            } else {
                transportMode = json.findValue("transportMode") == null ? null
                        : json.findValue("transportMode").asText();
            }
            String invoiceVehicleDetail = null;
            if (invoiceLog != null && invoiceLog.getVehicleDetail() != null) {
                invoiceVehicleDetail = invoiceLog.getVehicleDetail();
            } else {
                invoiceVehicleDetail = json.findValue("invoiceVehicleDetail") == null ? null
                        : json.findValue("invoiceVehicleDetail").asText();
            }
            String invoiceTerms = null;
            if (invoiceLog != null) {
                invoiceTerms = invoiceLog.getTerms();
            } else {
                invoiceTerms = json.findValue("invoiceTerms") == null ? null : json.findValue("invoiceTerms").asText();
            }
            if (invoiceLog != null && invoiceLog.getGstinEcomOperator() != null) {
                ecomGstin = invoiceLog.getGstinEcomOperator();
            } else {
                String ecomGstin1 = json.findValue("ecomGstin1") == null ? null : json.findValue("ecomGstin1").asText();
                String ecomGstin2 = json.findValue("ecomGstin2") == null ? null : json.findValue("ecomGstin2").asText();
                ecomGstin = ecomGstin1 + ecomGstin2;
            }

            String destCountry = null;
            if (invoiceLog != null) {
                destCountry = invoiceLog.getCountryName();
            } else {
                destCountry = json.findValue("destCountry") == null ? null : json.findValue("destCountry").asText();
            }
            if (destCurrencyCode == null || "".equals(destCurrencyCode)) {
                destCurrencyCode = "INR";
            }
            if (transaction.getTypeOfSupply() != null && transaction.getTypeOfSupply() != 3) {
                destCurrencyCode = "INR";
            }
            invoiceHeading = json.findValue("invoiceHeading") == null ? 0 : json.findValue("invoiceHeading").asInt();

            params.put("datetimeOfShipping", datetimeOfShipping);
            params.put("transportMode", transportMode);
            params.put("invoiceVehicleDetail", invoiceVehicleDetail);
            params.put("invoiceTerms", invoiceTerms);
            DigitalSignatureBranchWise digitalSignData = DigitalSignatureBranchWise.findByOrgAndBranch(entityManager,
                    user.getOrganization().getId(), branch.getId());
            if (digitalSignData != null && digitalSignData.getDigitalSignDocuments() != null
                    && !digitalSignData.getDigitalSignDocuments().equals("")) {
                if (invoiceLog != null && invoiceLog.getDigitalSignatureContent() != null) {
                    if ((txnPurpose == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                            || txnPurpose == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER)
                            && digitalSignData.getDigitalSignDocuments().substring(0, 1).equals("1"))
                        params.put("digitalSignature", invoiceLog.getDigitalSignatureContent());
                    else if (txnPurpose == IdosConstants.DEBIT_NOTE_CUSTOMER
                            && digitalSignData.getDigitalSignDocuments().contains("8"))
                        params.put("digitalSignature", invoiceLog.getDigitalSignatureContent());
                    else if (txnPurpose == IdosConstants.CREDIT_NOTE_CUSTOMER
                            && digitalSignData.getDigitalSignDocuments().contains("9"))
                        params.put("digitalSignature", invoiceLog.getDigitalSignatureContent());
                    else if (txnPurpose == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER
                            && digitalSignData.getDigitalSignDocuments().contains("6"))
                        params.put("digitalSignature", invoiceLog.getDigitalSignatureContent());
                }
                if (txnPurpose == IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER
                        && digitalSignData.getDigitalSignDocuments().contains("3")) {
                    String digitalSignatureContentRecAdv = "";
                    digitalSignatureContentRecAdv = json.findValue("digitalSignatureContent") == null ? null
                            : json.findValue("digitalSignatureContent").asText();
                    params.put("digitalSignatureRecAdv", digitalSignatureContentRecAdv);
                }
            }
            if (transaction.getTypeOfSupply() != null && transaction.getTypeOfSupply() == 2) {
                params.put("isTaxPayableRevChrg", "Yes");
            } else {
                params.put("isTaxPayableRevChrg", "No");
            }

            params.put("numgoodsremove", numgoodsremove);
            params.put("ecomGstin", ecomGstin);
            params.put("destinationCntry", destCountry);
            params.put("destCurrencyCode", destCurrencyCode);
            params.put("conversionRate", currencyRate);

            Double roundoffamt = transaction.getRoundedCutPartOfNetAmount() == null ? 0.0
                    : transaction.getRoundedCutPartOfNetAmount();
            params.put("roundoffamt",
                    "(" + destCurrencyCode + ") " + IdosConstants.DECIMAL_FORMAT_MAIN_2DEC.format(roundoffamt));

            Double totalDueAmt = transaction.getNetAmount() == null ? 0.0 : transaction.getNetAmount();
            Double totalGross = transaction.getGrossAmount() == null ? 0.0 : transaction.getGrossAmount();
            Double totalDueAmtBfrRound = totalGross + totalTaxAmount;
            Double advanceAdjustedBeforeRound = totalDueAmt - totalDueAmtBfrRound;
            Double advanceAdjusted = (double) Math.round(advanceAdjustedBeforeRound);

            String totalDueAmtBfrRoundStr = null;
            if (transaction.getTypeOfSupply() != null && transaction.getTypeOfSupply() == 3) {
                Double currencyCovRate = 1.0;
                if (currencyRate != null && !"".equals(currencyRate)) {
                    currencyCovRate = Double.parseDouble(currencyRate);
                }
                if (currencyCovRate > 1.0) {
                    totalDueAmtBfrRound = totalDueAmtBfrRound / currencyCovRate;
                    advanceAdjusted = advanceAdjusted / currencyCovRate;
                    totalDueAmt = totalDueAmt / currencyCovRate;
                } else if (currencyCovRate < 1.0) {
                    totalDueAmtBfrRound = totalDueAmtBfrRound * currencyCovRate;
                    advanceAdjusted = advanceAdjusted * currencyCovRate;
                    totalDueAmt = totalDueAmt * currencyCovRate;
                }
            }
            totalDueAmtBfrRoundStr = IdosConstants.DECIMAL_FORMAT_MAIN_2DEC.format(totalDueAmtBfrRound);
            params.put("totalDueAmtBfrRound", "(" + destCurrencyCode + ") " + totalDueAmtBfrRoundStr);

            if (advanceAdjusted != null) {
                params.put("advanceAdjusted",
                        "(" + destCurrencyCode + ") " + IdosConstants.DECIMAL_FORMAT_MAIN_2DEC.format(advanceAdjusted));
            }
            params.put("totalDueAmt",
                    "(" + destCurrencyCode + ") " + IdosConstants.DECIMAL_FORMAT_MAIN_2DEC.format(totalDueAmt));
            long totalDueAmtLong = Math.round(totalDueAmt);
            params.put("totalDueAmtWord",
                    NumberToWordsInt.convert(totalDueAmtLong) + " Only. (" + destCurrencyCode + ") ");

        } else if (txnPurpose == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                || txnPurpose == IdosConstants.BUY_ON_CREDIT_PAY_LATER) {
            String CGSTTax = "0";
            String SGSTTax = "0";
            String totalTax = "0";
            String totalTaxInWords = "";
            Double totalTaxDouble = 0.0;

            if (dataList != null && dataList.size() > 0) {
                CGSTTax = dataList.get(0).getTaxValue6(); // total of all CGST taxes
                SGSTTax = dataList.get(0).getTaxValue7();
                totalTaxDouble = Double.parseDouble(CGSTTax) + Double.parseDouble(SGSTTax);
                totalTax = IdosConstants.decimalFormat.format(totalTaxDouble);
            }
            params.put("invoiceNumber", transaction.getInvoiceNumber() == null ? "" : transaction.getInvoiceNumber());
            double totalAmount = 0.0;
            // if cgst and sgst =0 igst !=0 cess can or cannot be zero
            if (Double.parseDouble(CGSTTax) == 0 && Double.parseDouble(SGSTTax) == 0) {
                params.put("totalTax1", " ");
                params.put("totalTax2", " ");
                params.put("totalTax3",
                        Double.toString(transaction.getTaxValue3() == null ? 0.0 : transaction.getTaxValue3()));
                params.put("totalTax4",
                        Double.toString(transaction.getTaxValue4() == null ? 0.0 : transaction.getTaxValue4()));
                if (transaction.getTaxValue4() == 0) {
                    params.put("totalTax4", " ");
                }
                if (transaction.getTaxValue3() != null) {
                    totalAmount += transaction.getTaxValue3();
                }
                if (transaction.getTaxValue4() != null) {
                    totalAmount += transaction.getTaxValue4();
                }
                params.put("totalTax", Double.toString(totalAmount));
            }

            if (Double.parseDouble(CGSTTax) != 0 && Double.parseDouble(SGSTTax) != 0) {
                params.put("totalTax1", CGSTTax);
                params.put("totalTax2", SGSTTax);
                params.put("totalTax3", " ");
                params.put("totalTax4", Double.toString(transaction.getTaxValue4()));
                if (transaction.getTaxValue4() == 0) {
                    params.put("totalTax4", " ");
                }
                totalAmount = Double.parseDouble(CGSTTax) + Double.parseDouble(SGSTTax) + transaction.getTaxValue4();
                params.put("totalTax", Double.toString(totalAmount));
            }
            if (Double.parseDouble(CGSTTax) == 0 && Double.parseDouble(SGSTTax) == 0 && transaction.getTaxValue3() == 0
                    && transaction.getTaxValue4() == 0) {
                params.put("totalTax1", " ");
                params.put("totalTax2", " ");
                params.put("totalTax3", " ");
                params.put("totalTax4", " ");
                params.put("totalTax", " ");
            }
            if (transaction.getGrossAmount() != null) {
                params.put("totalTaxableValue", Double.toString(transaction.getGrossAmount()));
            } else {
                params.put("totalTaxableValue", "0.00");
            }
            if (transaction.getTypeOfSupply() != null && transaction.getTypeOfSupply() == 2) {
                params.put("title", titleUnregisteredSupplier);
            } else if (transaction.getTypeOfSupply() == 3) {
                params.put("title", titleRegisteredSupplier);
            }
            params.put("totalTaxInWords", NumberToWordsInt.convert(totalTaxDouble.longValue()));
            if (transaction.getTransactionVendorCustomer() != null
                    && transaction.getTransactionVendorCustomer().getName() != null) {
                params.put("customerName", transaction.getTransactionVendorCustomer().getName());
            }
        }

        String title = "TAXABLE INVOICE";
        String subTitle = null;
        String invoiceName = "invoicegst";
        String ecomGstinLabel = null;
        if (transaction.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                || transaction.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER) {
            if (invoiceHeading == 1 || invoiceHeading == 0 || invoiceHeading == null)
                title = "Tax Invoice";
            else if (invoiceHeading == 2) {
                title = "Bill of Supply";
                invoiceName = "invoicegstComposition";
            } else if (invoiceHeading == 3)
                title = "Tax Invoice cum Bill of Supply";

        }
        if (transaction.getTransactionBranchOrganization().getIsCompositionScheme() != null
                && transaction.getTransactionBranchOrganization().getIsCompositionScheme() == 1) {
            if (transaction.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                    || transaction.getTransactionPurpose()
                            .getId() == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER) {
                title = "Bill of Supply";
                invoiceName = "invoicegstComposition";
                subTitle = "Composition taxable person, not eligible to collect tax on supplies";
            }
        }

        if (transaction.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                || transaction.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CREDIT_PAY_LATER) {
            if (transaction.getTypeOfSupply() != null && transaction.getTypeOfSupply() == 2) {
                title = titleUnregisteredSupplier;
                params.put("detailOfSupplier", "Detail Of Unregistered Suppplier");
                if (branch != null && branch.getGstin() != null) {
                    params.put("billingStateCode", branch.getGstin().substring(0, 2));
                } else {
                    params.put("billingStateCode", "");
                }
            } else if (transaction.getTypeOfSupply() != null && transaction.getTypeOfSupply() == 3) {
                title = titleRegisteredSupplier;
                params.put("detailOfSupplier", "Detail Of Registered Suppplier");
                if (branch != null && branch.getGstin() != null) {
                    params.put("billingStateCode", branch.getGstin().substring(0, 2));
                } else {
                    params.put("billingStateCode", "");
                }

            }
            invoiceName = "buySelfInvoiceGst";
        }
        if (txnPurpose == IdosConstants.CREDIT_NOTE_CUSTOMER || txnPurpose == IdosConstants.DEBIT_NOTE_CUSTOMER) {
            params.put("againstInvoiceNoLabel", "Against Invoice Number:");
            params.put("againstInvoiceDateLabel", "Against Invoice Dated:");
            String linkedTxnRef = transaction.getLinkedTxnRef();
            if (linkedTxnRef != null && !"".equals(linkedTxnRef)) {
                List<Transaction> txns = Transaction.findByTxnReference(entityManager, user.getOrganization().getId(),
                        linkedTxnRef);
                if (txns != null && txns.size() > 0) {
                    Transaction transaction2 = txns.get(0);
                    String invoiceNumber = transaction2.getInvoiceNumber();
                    Date transactionDate = transaction2.getTransactionDate();
                    if (transactionDate != null) {
                        params.put("againstInvoiceDate", IdosConstants.IDOSDF.format(transactionDate));
                    } else {
                        params.put("againstInvoiceDate", "");
                    }
                    if (invoiceNumber != null) {
                        params.put("againstInvoiceNo", invoiceNumber);
                    } else {
                        params.put("againstInvoiceNo", "");
                    }
                } else {
                    params.put("againstInvoiceDate", "");
                    params.put("againstInvoiceNo", "");
                }
            } else {
                params.put("againstInvoiceDate", "");
                params.put("againstInvoiceNo", "");
            }
        }

        if (txnPurpose == IdosConstants.CREDIT_NOTE_CUSTOMER) {
            title = "CREDIT NOTE";
            params.put("invoiceSrNoLable", "Against Invoice Number:");
            params.put("invoiceDateLable", "Against Invoice Dated:");
            params.put("headerForNoteNumber", "Credit Note Serial Number:");
            params.put("dateHeaderForNote", "Credit Note Date:");

            params.put("valueforNoteNumber", transaction.getInvoiceNumber());
            if (transaction.getTransactionDate() != null) {
                params.put("dateValueForNote", IdosConstants.IDOSDF.format(transaction.getTransactionDate()));
            } else {
                params.put("dateValueForNote", "");
            }
        } else if (txnPurpose == IdosConstants.DEBIT_NOTE_CUSTOMER) {
            title = "DEBIT NOTE";
            params.put("invoiceSrNoLable", "Against Invoice Number:");
            params.put("invoiceDateLable", "Against Invoice Dated:");
            params.put("headerForNoteNumber", "Debit Note Serial Number:");
            params.put("dateHeaderForNote", "Debit Note Date:");
            params.put("valueforNoteNumber", transaction.getInvoiceNumber());
            if (transaction.getTransactionDate() != null) {
                params.put("dateValueForNote", IdosConstants.IDOSDF.format(transaction.getTransactionDate()));
            } else {
                params.put("dateValueForNote", "");
            }
        } else {
            params.put("invoiceSrNoLable", "Invoice Serial Number:");
            params.put("invoiceDateLable", "Invoice Date:");
        }
        if (txnPurpose != IdosConstants.CREDIT_NOTE_CUSTOMER && txnPurpose != IdosConstants.DEBIT_NOTE_CUSTOMER
                && txnPurpose != IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER
                && txnPurpose != IdosConstants.CANCEL_INVOICE && txnPurpose != IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                && txnPurpose != IdosConstants.BUY_ON_CREDIT_PAY_LATER) {
            int typeOfSupply = transaction.getTypeOfSupply() == null ? 0 : transaction.getTypeOfSupply();
            switch (typeOfSupply) {
                case 2:
                    title = "INVOICE";
                    subTitle = "GST is payable under reverse charge";
                    invoiceName = "invwithouttax";
                    break;
                case 3:
                    title = "EXPORT INVOICE";
                    if (transaction.getWithWithoutTax() != null && transaction.getWithWithoutTax() == 1) {
                        subTitle = "Supply meant for Export on payment of Integrated Tax";
                        invoiceName = "invexpwithtax";
                    } else {
                        subTitle = "Supply meant for Export under Bond or Letter of Undertaking without payment of Intergrated Tax";
                        invoiceName = "invexpwithouttax";
                    }
                    break;
                case 4:
                    title = "SEZ EXPORT INVOICE";
                    if (transaction.getWithWithoutTax() != null && transaction.getWithWithoutTax() == 1) {
                        subTitle = "Supply to SEZ unit for Authorised Operations on Payment of Integrated Tax";
                        invoiceName = "invwithtax";
                    } else {
                        subTitle = "Supply meant for SEZ Export under Bond or Letter of Undertaking without payment of Intergrated Tax";
                        invoiceName = "invwithouttax";
                    }
                    break;
                case 5:
                    title = "DEEMED EXPORT INVOICE";
                    if (transaction.getWithWithoutTax() != null && transaction.getWithWithoutTax() == 1) {
                        subTitle = "Supply meant for Export on payment of Integrated Tax";
                        invoiceName = "invwithtax";
                    } else {
                        subTitle = "Supply meant for Export under Bond or Letter of Undertaking without payment of Integrated Tax";
                        invoiceName = "invwithouttax";
                    }
                    break;
                case 6:
                    title = "INVOICE";
                    subTitle = "Supply made through E-commerce Operator ";
                    ecomGstinLabel = "E-commerce operator GSTIN:";
                    break;
                case 7:
                    title = "BILL OF SUPPLY";
                    invoiceName = "invwithouttax";
                    break;
            }
        } else if (txnPurpose == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
            invoiceName = "transferInvoicegst";
            title = "DELIVERY CHALLAN";
            params.put("invoiceSrNoLable", "DC Serial Number:");
            params.put("invoiceDateLable", "DC Date:");
            subTitle = "Inventory Transfer";

            if (transaction.getTransactionToBranch() != null) {
                if (transaction.getTransactionToBranch().getName() != null) {
                    params.put("customerName", transaction.getTransactionToBranch().getName());
                }
                if (transaction.getTransactionToBranch().getAddress() != null) {
                    params.put("customerAddress",
                            IdosUtil.replaceFormatingChar(transaction.getTransactionToBranch().getAddress()));
                }
                params.put("customerEmail", "");
                if (transaction.getTransactionToBranch().getPhoneNumber() != null
                        && transaction.getTransactionToBranch().getPhoneNumber() != null) {
                    params.put("customerPhNo", transaction.getTransactionToBranch().getPhoneNumber());
                }

                if (transaction.getTransactionToBranch().getGstin() != null
                        && transaction.getTransactionToBranch().getGstin().length() > 2) {
                    params.put("customerGstin", transaction.getTransactionToBranch().getGstin());
                    params.put("billingStateCode", transaction.getTransactionToBranch().getGstin().substring(0, 2));
                }
            }
        }
        params.put("title", title);
        params.put("subTitle", subTitle);
        params.put("invoiceName", invoiceName);
        params.put("ecomGstin", ecomGstin);
        params.put("ecomGstinLabel", ecomGstinLabel);

        Map<String, Object> criterias = new HashMap<String, Object>();
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
            if (transaction.getTransactionVendorCustomer().getName() != null) {
                params.put("customerName", transaction.getTransactionVendorCustomer().getName());
            }
            StringBuilder billingAddress = new StringBuilder();
            if (customerDetail != null) {
                if (customerDetail.getBillingaddress() != null) {
                    billingAddress.append(customerDetail.getBillingaddress());
                }
                if (customerDetail.getBillinglocation() != null) {
                    if (transaction.getWalkinCustomerType() != null
                            && (transaction.getWalkinCustomerType() == 3 || transaction.getWalkinCustomerType() == 4)) {
                        billingAddress.append(IdosConstants.STATE_CODE_MAPPING
                                .get(transaction.getTransactionBranch().getStateCode()));
                    } else {
                        billingAddress.append(", ").append(customerDetail.getBillinglocation());
                    }
                }
                if (customerDetail.getBillingState() != null) {
                    billingAddress.append(", ").append(customerDetail.getBillingState());
                }
            } else {
                if (transaction.getTransactionVendorCustomer().getAddress() != null) {
                    billingAddress.append(transaction.getTransactionVendorCustomer().getAddress());
                }
                if (transaction.getTransactionVendorCustomer().getLocation() != null) {
                    billingAddress.append(", ").append(transaction.getTransactionVendorCustomer().getLocation());
                }
                if (transaction.getTransactionVendorCustomer().getCountryState() != null) {
                    billingAddress.append(", ").append(transaction.getTransactionVendorCustomer().getCountryState());
                }
            }
            params.put("customerAddress", IdosUtil.replaceFormatingChar(billingAddress.toString()));
            if (transaction.getTransactionVendorCustomer().getEmail() != null) {
                params.put("customerEmail", transaction.getTransactionVendorCustomer().getEmail());
            }
            if (customerDetail != null && customerDetail.getBillingphone() != null) {
                params.put("customerPhNo", customerDetail.getBillingphone());
            } else {
                params.put("customerPhNo", transaction.getTransactionVendorCustomer().getPhone());
            }

            if (customerDetail != null && customerDetail.getGstin() != null) {
                params.put("billingStateCode", customerDetail.getGstin().substring(0, 2));
                if (customerDetail.getGstin().length() > 2) {
                    params.put("customerGstin", customerDetail.getGstin());
                }
            }

            if (txnPurpose == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                    || txnPurpose == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
                    || txnPurpose == IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER
                    || txnPurpose == IdosConstants.CREDIT_NOTE_CUSTOMER
                    || txnPurpose == IdosConstants.DEBIT_NOTE_CUSTOMER
                    || txnPurpose == IdosConstants.REFUND_ADVANCE_RECEIVED
                    || txnPurpose == IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE
                    || txnPurpose == IdosConstants.CANCEL_INVOICE) {
                StringBuilder shippingAddress = new StringBuilder();
                if (customerDetail != null) {
                    if (customerDetail.getShippingaddress() != null) {
                        shippingAddress.append(customerDetail.getShippingaddress());
                    }
                    if (customerDetail.getShippinglocation() != null) {
                        if (transaction.getWalkinCustomerType() == 3 || transaction.getWalkinCustomerType() == 4) {
                            shippingAddress.append(IdosConstants.STATE_CODE_MAPPING
                                    .get(transaction.getTransactionBranch().getStateCode()));
                            params.put("shippingLocation", IdosConstants.STATE_CODE_MAPPING
                                    .get(transaction.getTransactionBranch().getStateCode()));
                        } else {
                            shippingAddress.append(", ").append(customerDetail.getShippinglocation());
                            params.put("shippingLocation", customerDetail.getShippinglocation());
                        }
                    }
                    if (customerDetail.getShippingStateCode() != null) {
                        params.put("shippingStateCode", customerDetail.getShippingStateCode());
                    } else {
                        String gstinStateCode = "";
                        if (transaction.getDestinationGstin() != null
                                && transaction.getDestinationGstin().length() > 1) {
                            gstinStateCode = transaction.getDestinationGstin().substring(0, 2);
                        }
                        params.put("shippingStateCode", gstinStateCode);
                    }
                    if (customerDetail.getShippingState() != null) {
                        shippingAddress.append(", ").append(customerDetail.getShippingState());
                        params.put("shippingState", customerDetail.getShippingState());
                    } else {
                        params.put("shippingState",
                                IdosConstants.STATE_CODE_MAPPING.get(customerDetail.getShippingStateCode()));
                    }
                    if (customerDetail.getShippingphone() != null) {
                        params.put("shippingPhNo", customerDetail.getShippingphone());
                    }
                } else {
                    if (transaction.getTransactionVendorCustomer().getAddress() != null) {
                        shippingAddress.append(transaction.getTransactionVendorCustomer().getAddress());
                    }
                    if (transaction.getTransactionVendorCustomer().getLocation() != null) {
                        shippingAddress.append(", ").append(transaction.getTransactionVendorCustomer().getLocation());
                    }
                    if (transaction.getTransactionVendorCustomer().getCountryState() != null) {
                        shippingAddress.append(", ")
                                .append(transaction.getTransactionVendorCustomer().getCountryState());
                    }
                    if (transaction.getTransactionVendorCustomer().getPhone() != null) {
                        params.put("shippingPhNo", transaction.getTransactionVendorCustomer().getPhone());
                    }
                    if (transaction.getDestinationGstin() != null && transaction.getDestinationGstin().length() > 1) {
                        params.put("shippingStateCode", transaction.getDestinationGstin().substring(0, 2));
                    }
                    String gstinStateCode = "";
                    if (transaction.getDestinationGstin() != null && transaction.getDestinationGstin().length() > 1) {
                        gstinStateCode = transaction.getDestinationGstin().substring(0, 2);
                    }
                    params.put("shippingLocation", IdosConstants.STATE_CODE_MAPPING.get(gstinStateCode));
                    params.put("shippingState", IdosConstants.STATE_CODE_MAPPING.get(gstinStateCode));
                }
                params.put("shippingAddress", IdosUtil.replaceFormatingChar(shippingAddress.toString()));
                DigitalSignatureBranchWise digitalSignData = DigitalSignatureBranchWise
                        .findByOrgAndBranch(entityManager, user.getOrganization().getId(), branch.getId());
                if (digitalSignData != null && digitalSignData.getDigitalSignDocuments() != null
                        && !digitalSignData.getDigitalSignDocuments().equals("")) {
                    if (txnPurpose == IdosConstants.REFUND_ADVANCE_RECEIVED
                            && digitalSignData.getDigitalSignDocuments().contains("5"))
                        params.put("digitalSignRefAdvRec", digitalSignData.getPersonName());
                    if (txnPurpose == IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE
                            && digitalSignData.getDigitalSignDocuments().contains("10"))
                        params.put("digiSignContentRefPayment", digitalSignData.getPersonName());
                }
            }
        } else if (transaction.getTransactionUnavailableVendorCustomer() != null
                && (transaction.getWalkinCustomerType() == 1 || transaction.getWalkinCustomerType() == 2)) {
            Vendor customer = Vendor.findByOrgIdTypeName(entityManager,
                    transaction.getTransactionBranchOrganization().getId(), IdosConstants.WALK_IN_CUSTOMER,
                    transaction.getTransactionUnavailableVendorCustomer().toUpperCase());
            CustomerDetail customerDetail = CustomerDetail.findByCustomerID(entityManager, customer.getId());
            params.put("customerName", transaction.getTransactionUnavailableVendorCustomer());
            StringBuilder billingAddress = new StringBuilder();
            if (customerDetail != null) {
                if (customerDetail.getBillingaddress() != null) {
                    billingAddress.append(customerDetail.getBillingaddress());
                }
                if (customerDetail.getBillinglocation() != null) {
                    billingAddress.append(", ").append(customerDetail.getBillinglocation());
                }
                if (customerDetail.getBillingState() != null) {
                    billingAddress.append(", ").append(customerDetail.getBillingState());
                }
            } else {
                if (transaction.getTransactionVendorCustomer().getAddress() != null) {
                    billingAddress.append(transaction.getTransactionVendorCustomer().getAddress());
                }
                if (transaction.getTransactionVendorCustomer().getLocation() != null) {
                    billingAddress.append(", ").append(transaction.getTransactionVendorCustomer().getLocation());
                }
                if (transaction.getTransactionVendorCustomer().getCountryState() != null) {
                    billingAddress.append(", ").append(transaction.getTransactionVendorCustomer().getCountryState());
                }
            }
            params.put("customerAddress", billingAddress.toString());
            if (transaction.getTransactionVendorCustomer() != null
                    && transaction.getTransactionVendorCustomer().getEmail() != null) {
                params.put("customerEmail", transaction.getTransactionVendorCustomer().getEmail());
            }
            if (customerDetail != null && customerDetail.getBillingphone() != null) {
                params.put("customerPhNo", customerDetail.getBillingphone());
            } else {
                params.put("customerPhNo", transaction.getTransactionVendorCustomer().getPhone());
            }

            if (customerDetail != null && customerDetail.getGstin() != null) {
                params.put("billingStateCode", customerDetail.getGstin().substring(0, 2));
                if (customerDetail.getGstin().length() > 2) {
                    params.put("customerGstin", customerDetail.getGstin());
                }
            }

            if (txnPurpose == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                    || txnPurpose == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
                    || txnPurpose == IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER
                    || txnPurpose == IdosConstants.REFUND_ADVANCE_RECEIVED
                    || txnPurpose == IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE) {
                StringBuilder shippingAddress = new StringBuilder();
                if (customerDetail != null) {

                    if (customerDetail.getShippingaddress() != null) {
                        shippingAddress.append(customerDetail.getShippingaddress());
                    }
                    if (customerDetail.getShippinglocation() != null) {
                        shippingAddress.append(", ").append(customerDetail.getShippinglocation());
                        params.put("shippingLocation", customerDetail.getShippinglocation());
                    }
                    if (customerDetail.getShippingState() != null) {
                        shippingAddress.append(", ").append(customerDetail.getShippingState());
                        params.put("shippingState", customerDetail.getShippingState());
                    } else {
                        params.put("shippingState",
                                IdosConstants.STATE_CODE_MAPPING.get(customerDetail.getShippingStateCode()));
                    }
                    if (customerDetail.getShippingphone() != null) {
                        params.put("shippingPhNo", customerDetail.getShippingphone());
                    }
                    if (customerDetail.getShippingStateCode() != null) {
                        params.put("shippingStateCode", customerDetail.getShippingStateCode());
                    }

                } else {
                    if (transaction.getTransactionVendorCustomer().getAddress() != null) {
                        shippingAddress.append(transaction.getTransactionVendorCustomer().getAddress());
                    }

                    if (transaction.getTransactionVendorCustomer().getLocation() != null) {
                        shippingAddress.append(", ").append(transaction.getTransactionVendorCustomer().getLocation());
                    }
                    if (transaction.getTransactionVendorCustomer().getCountryState() != null) {
                        shippingAddress.append(", ")
                                .append(transaction.getTransactionVendorCustomer().getCountryState());
                    }
                    if (transaction.getTransactionVendorCustomer().getPhone() != null) {
                        params.put("shippingPhNo", transaction.getTransactionVendorCustomer().getPhone());
                    }
                    if (transaction.getDestinationGstin() != null && transaction.getDestinationGstin().length() > 1) {
                        params.put("shippingStateCode", transaction.getDestinationGstin().substring(0, 2));
                    }
                    String gstinStateCode = "";
                    if (transaction.getDestinationGstin() != null && transaction.getDestinationGstin().length() > 1) {
                        gstinStateCode = transaction.getDestinationGstin().substring(0, 2);
                    }
                    params.put("shippingLocation", IdosConstants.STATE_CODE_MAPPING.get(gstinStateCode));
                    params.put("shippingState", IdosConstants.STATE_CODE_MAPPING.get(gstinStateCode));
                }
                params.put("shippingAddress", shippingAddress.toString());
            }
        } else if (transaction.getWalkinCustomerType() != null
                && (transaction.getWalkinCustomerType() == 3 || transaction.getWalkinCustomerType() == 4)) {
            params.put("customerName", transaction.getTransactionUnavailableVendorCustomer());
            params.put("customerAddress", "");
            params.put("customerEmail", "");
            params.put("customerPhNo", "");
            params.put("customerPhNo", "");
            params.put("billingStateCode", "");
            params.put("customerGstin", "");
            // params.put("shippingLocation",
            // transaction.getTransactionBranch().getLocation());
            String state = IdosConstants.STATE_CODE_MAPPING.get(transaction.getTransactionBranch().getStateCode());
            params.put("shippingLocation", state);
            params.put("shippingPhNo", "");
            String gstinStateCode = null;
            if (transaction.getDestinationGstin() != null && transaction.getDestinationGstin().length() > 1) {
                gstinStateCode = transaction.getDestinationGstin().substring(0, 2);
            }
            if (gstinStateCode != null) {
                params.put("shippingStateCode", gstinStateCode);
                params.put("shippingState", IdosConstants.STATE_CODE_MAPPING.get(gstinStateCode));
                // params.put("shippingLocation",
                // IdosConstants.STATE_CODE_MAPPING.get(gstinStateCode));
            } else {
                params.put("shippingStateCode", null);
                params.put("shippingState", null);
                // params.put("shippingLocation", null);
            }
            params.put("shippingPhNo", "");
            params.put("shippingAddress", "");
        } else if (transaction.getWalkinCustomerType() != null
                && (transaction.getWalkinCustomerType() == 5 || transaction.getWalkinCustomerType() == 6)) {
            params.put("customerName", transaction.getTransactionUnavailableVendorCustomer());
            params.put("customerAddress", "");
            params.put("customerEmail", "");
            params.put("customerPhNo", "");
            params.put("customerPhNo", "");
            params.put("billingStateCode", "");
            params.put("customerGstin", "");
            String gstinStateCode = null;
            if (transaction.getDestinationGstin() != null && transaction.getDestinationGstin().length() > 1) {
                gstinStateCode = transaction.getDestinationGstin().substring(0, 2);
            }
            if (gstinStateCode != null) {
                params.put("shippingStateCode", gstinStateCode);
                params.put("shippingState", IdosConstants.STATE_CODE_MAPPING.get(gstinStateCode));
                params.put("shippingLocation", IdosConstants.STATE_CODE_MAPPING.get(gstinStateCode));
            } else {
                params.put("shippingStateCode", null);
                params.put("shippingState", null);
                params.put("shippingLocation", null);
            }

            params.put("shippingPhNo", "");
            params.put("shippingPhNo", "");
            params.put("shippingAddress", "");
        }

        if (transaction.getTransactionBranchOrganization() != null) {
            String companyLogo = FileUtil.getCompanyLogo(transaction.getTransactionBranchOrganization());
            if (companyLogo != null && !"".equals(companyLogo)) {
                params.put("companyLogo", companyLogo);
            }
            if (transaction.getTransactionBranchOrganization().getName() != null) {
                params.put("companyName", transaction.getTransactionBranchOrganization().getName());
            }
            if (transaction.getTransactionBranchOrganization().getRegisteredAddress() != null) {
                String address = transaction.getTransactionBranchOrganization().getRegisteredAddress()
                        .replaceAll("\\r\\n|\\r|\\n", " ");
                params.put("companyAddress", address);
            }
            if (transaction.getTransactionBranchOrganization().getCorporateMail() != null) {
                params.put("companyEmail", transaction.getTransactionBranchOrganization().getCorporateMail());
            }
            if (transaction.getTransactionBranchOrganization().getRegisteredPhoneNumber() != null) {
                params.put("companyPhNo", transaction.getTransactionBranchOrganization().getRegisteredPhoneNumber());
            }

            if (transaction.getTransactionBranchOrganization().getWebUrl() != null) {
                params.put("companyURL", transaction.getTransactionBranchOrganization().getWebUrl());
            }
        }

        if (txnPurpose == IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER) {
            title = "ADVANCE RECEIPT VOUCHER";
            params.put("title", title);
            params.put("amountRecDate", dateofgoodsremove);
            params.put("POReference", numgoodsremove);
            if (transaction.getTypeOfSupply() != null && transaction.getTypeOfSupply() != 3) {
                params.put("totalDueAmtBfrRound", null);
            } else {
                if (currencyRate != null && !"".equals(currencyRate)) {
                    // params.put("totalDueAmtBfrRound", "Foreign Currency Amount Received.");
                    params.put("totalDueAmtBfrRound",
                            "Foreign Currency Amount Received: (" + destCurrencyCode + ") " + currencyRate);
                } else {
                    params.put("totalDueAmtBfrRound", "Foreign Currency Amount Received: (" + destCurrencyCode + ")");
                }
            }
        }
        if (txnPurpose == IdosConstants.REFUND_ADVANCE_RECEIVED
                || txnPurpose == IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE) {
            title = "REFUND VOUCHER";
            params.put("title", title);
            if (transaction.getRemarks() != null && !"".equals(transaction.getRemarks())) {
                String string = transaction.getRemarks();
                String remarks[] = string.split("#");
                params.put("remark", remarks[remarks.length - 1]);
            } else {
                params.put("remark", "");
            }
            if (txnPurpose == IdosConstants.REFUND_ADVANCE_RECEIVED) {
                String linkedTxnRef = transaction.getLinkedTxnRef();
                if (linkedTxnRef != null && !linkedTxnRef.equals("")) {
                    List<Transaction> findByTxnReference = Transaction.findByTxnReference(entityManager,
                            user.getOrganization().getId(), linkedTxnRef);
                    if (findByTxnReference != null && findByTxnReference.size() > 0) {
                        if (findByTxnReference.get(0).getTransactionDate() != null) {
                            params.put("PODate",
                                    IdosConstants.IDOSDF.format(findByTxnReference.get(0).getTransactionDate()));
                        } else {
                            params.put("PODate", "");
                        }
                        if (findByTxnReference.get(0).getInvoiceNumber() != null) {
                            params.put("POReference", findByTxnReference.get(0).getInvoiceNumber());
                        } else {
                            params.put("POReference", "");
                        }
                    } else {
                        params.put("PODate", "");
                        params.put("POReference", "");
                    }
                } else {
                    params.put("PODate", "");
                    params.put("POReference", "");
                }
            }
        }

        String shippingAddress = json.findValue("shipingAddress") == null ? null
                : json.findValue("shipingAddress").asText();
        if (shippingAddress != null && txnPurpose == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                || txnPurpose == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
                || txnPurpose == IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER) {
            String shippingLocation = json.findValue("shipingLocation") == null ? null
                    : json.findValue("shipingLocation").asText();
            String shippingCountry = json.findValue("shipingCountry") == null ? null
                    : json.findValue("shipingCountry").asText();
            String shippingState = json.findValue("shipingState") == null ? null
                    : json.findValue("shipingState").asText();

            StringBuilder completeShippingAddress = new StringBuilder();
            if (shippingAddress != null) {
                completeShippingAddress.append(shippingAddress);
            }

            if (shippingLocation != null) {
                if (!completeShippingAddress.toString().toUpperCase().contains(", " + shippingLocation.toUpperCase())) {
                    completeShippingAddress.append(", ").append(shippingLocation);
                }
            }

            if (shippingState != null) {
                if (!completeShippingAddress.toString().toUpperCase().contains(", " + shippingState.toUpperCase())) {
                    completeShippingAddress.append(", ").append(shippingState);
                }
                Map<String, String> stateCodeMapping = IdosConstants.STATE_CODE_MAPPING;
                String code = "";
                for (Map.Entry<String, String> entry : stateCodeMapping.entrySet()) {
                    if (entry.getValue().toUpperCase().equals(shippingState.toUpperCase())) {
                        code = entry.getKey();
                    }
                }
                params.put("shippingStateCode", code);
            }

            if (shippingCountry != null) {
                completeShippingAddress.append(", ").append(shippingCountry);
            }
            params.put("shippingAddress", completeShippingAddress.toString());
        }
        if (txnPurpose == IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE) {
            if (transaction.getTransactionVendorCustomer() != null
                    && transaction.getTransactionVendorCustomer().getGstin() != null) {
                if (transaction.getTransactionBranch().getGstin().length() > 2) {
                    String gstinStateCode = transaction.getTransactionVendorCustomer().getGstin().substring(0, 2);
                    if (gstinStateCode != null) {
                        params.put("shippingStateCode", gstinStateCode);
                        params.put("shippingState", IdosConstants.STATE_CODE_MAPPING.get(gstinStateCode));
                    }
                    params.put("customerGstin", transaction.getTransactionVendorCustomer().getGstin());
                }
            }
        }
        params.put("poweredBy", ConfigParams.getInstance().getPoweredBy());
        log.log(Level.FINE, "********** End " + params);
        return params;
    }

    @Override
    public int saveInvoiceLog(Users user, EntityManager entityManager, Transaction txn, String fileName, JsonNode json)
            throws IDOSException {
        try {
            Long txnPurpose = txn.getTransactionPurpose().getId();
            String datetimeOfShipping = json.findValue("datetimeOfShipping") == null ? null
                    : json.findValue("datetimeOfShipping").asText();
            String destCountry = json.findValue("destCountry") == null ? null : json.findValue("destCountry").asText();
            String destCurrencyCode = json.findValue("destCurrencyCode") == null ? null
                    : json.findValue("destCurrencyCode").asText();
            String portCode = json.findValue("portCode") == null ? null : json.findValue("portCode").asText();
            TransactionInvoice invoiceLog = TransactionInvoice.findByTransactionID(entityManager,
                    user.getOrganization().getId(), txn.getId());
            if (invoiceLog == null) {
                invoiceLog = new TransactionInvoice();
            }
            invoiceLog.setOrganization(txn.getTransactionBranchOrganization());
            invoiceLog.setBranch(txn.getTransactionBranch());
            invoiceLog.setTransaction(txn);
            if (txn.getInvoiceNumber() == null && !"Accounted".equals(txn.getTransactionStatus())) {
                invoiceLog.setInvoiceNumber(txn.getTransactionRefNumber());
            } else {
                invoiceLog.setInvoiceNumber(txn.getInvoiceNumber());
            }
            if (txnPurpose == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                    || txnPurpose == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
                    || txnPurpose == IdosConstants.CREDIT_NOTE_CUSTOMER
                    || txnPurpose == IdosConstants.DEBIT_NOTE_CUSTOMER || IdosConstants.CANCEL_INVOICE == txnPurpose
                    || (IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER == txnPurpose
                            && txn.getTypeIdentifier() == 1)) {

                String transportMode = json.findValue("transportMode") == null ? null
                        : json.findValue("transportMode").asText();
                String invoiceVehicleDetail = json.findValue("invoiceVehicleDetail") == null ? null
                        : json.findValue("invoiceVehicleDetail").asText();
                String invoiceTerms = json.findValue("invoiceTerms") == null ? null
                        : json.findValue("invoiceTerms").asText();
                String dateofgoodsremove = json.findValue("dateofgoodsremove") == null ? null
                        : json.findValue("dateofgoodsremove").asText();
                String numgoodsremove = json.findValue("numgoodsremove") == null ? null
                        : json.findValue("numgoodsremove").asText();
                String ecomGstin1 = json.findValue("ecomGstin1") == null ? null : json.findValue("ecomGstin1").asText();
                String ecomGstin2 = json.findValue("ecomGstin2") == null ? null : json.findValue("ecomGstin2").asText();
                Integer invoiceHeading = json.findValue("invoiceHeading") == null ? 0
                        : json.findValue("invoiceHeading").asInt();
                String digiSignContent = json.findValue("digiSignContent") == null ? null
                        : json.findValue("digiSignContent").asText();
                invoiceLog.setCountryName(destCountry);
                invoiceLog.setCurrencyCode(destCurrencyCode);
                if (datetimeOfShipping != null && !datetimeOfShipping.equals("")) {
                    Date dos = IdosConstants.MYSQLDTF
                            .parse(IdosConstants.MYSQLDTF.format(IdosConstants.IDOSSDFTIME.parse(datetimeOfShipping)));
                    invoiceLog.setDatetimeOfSupply(dos);
                }
                invoiceLog.setFileName(fileName);
                invoiceLog.setTranportationMode(transportMode);
                invoiceLog.setVehicleDetail(invoiceVehicleDetail);
                invoiceLog.setTerms(invoiceTerms);
                if (portCode != null) {
                    invoiceLog.setPortCode(portCode);
                }
                if (dateofgoodsremove != null && !dateofgoodsremove.equals("")) {
                    Date dogr = IdosConstants.MYSQLDF
                            .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSSDFDATE.parse(dateofgoodsremove)));
                    invoiceLog.setDateRemovalGoods(dogr);
                }
                invoiceLog.setApplNumberGoodsRemoval(numgoodsremove);
                invoiceLog.setGstinEcomOperator(ecomGstin1 + ecomGstin2);
                String currencyRate = json.findValue("currencyRate") == null ? null
                        : json.findValue("currencyRate").asText();
                Double currencyCovRate = 1.0;
                if (currencyRate != null && !"".equals(currencyRate)) {
                    currencyCovRate = IdosUtil.convertStringToDouble(currencyRate);
                    invoiceLog.setCurrencyConvRate(currencyCovRate);
                }
                if (invoiceHeading != null) {
                    invoiceLog.setInvoiceHeading(invoiceHeading);
                }
                if (digiSignContent != null) {
                    invoiceLog.setDigitalSignatureContent(digiSignContent);
                }
            } else if (txnPurpose == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                    || txnPurpose == IdosConstants.BUY_ON_CREDIT_PAY_LATER
                    || txnPurpose == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT
                    || txnPurpose == IdosConstants.CREDIT_NOTE_VENDOR || txnPurpose == IdosConstants.DEBIT_NOTE_VENDOR
                    || (txnPurpose == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER
                            && txn.getTypeIdentifier() == 2)) {
                Double amount = json.findValue("amount") == null ? null : json.findValue("amount").asDouble();
                String invRefNumber = json.findValue("invRefNumber") == null ? null
                        : json.findValue("invRefNumber").asText();
                String grnRefDate = json.findValue("grnRefDate") == null ? null : json.findValue("grnRefDate").asText();
                String grnRefNumber = json.findValue("grnRefNumber") == null ? null
                        : json.findValue("grnRefNumber").asText();
                String impRefDate = json.findValue("impRefDate") == null ? null : json.findValue("impRefDate").asText();
                String impRefNumber = json.findValue("grnRefNumber") == null ? null
                        : json.findValue("impRefNumber").asText();
                String remarksAddDetailsBuy = json.findValue("remarksAddDetailsBuy") == null ? null
                        : json.findValue("remarksAddDetailsBuy").asText();
                if (txnPurpose == IdosConstants.CREDIT_NOTE_VENDOR || txnPurpose == IdosConstants.DEBIT_NOTE_VENDOR) {
                    Integer reasonForReturn = json.findValue("reasonForReturn") == null
                            || "".equals(json.findValue("reasonForReturn").asText()) ? null
                                    : json.findValue("reasonForReturn").asInt();
                    invoiceLog.setReasonForReturn(reasonForReturn);
                }

                if (datetimeOfShipping != null && !datetimeOfShipping.equals("")) {
                    Date dos = IdosConstants.MYSQLDF
                            .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSSDFDATE.parse(datetimeOfShipping)));
                    invoiceLog.setInvRefDate(dos);
                }
                invoiceLog.setInvRefNumber(invRefNumber);
                if (portCode != null) {
                    invoiceLog.setPortCode(portCode);
                }

                if (invoiceLog.getInvoiceNumber() == null && invRefNumber != null) {
                    invoiceLog.setInvoiceNumber(invRefNumber);
                } else {
                    invoiceLog.setInvoiceNumber(txn.getTransactionRefNumber());
                }
                if (amount == null && amount > 0) {
                    invoiceLog.setAmount(amount);
                    invoiceLog.setCountryName(destCountry);
                    invoiceLog.setCurrencyCode(destCurrencyCode);
                }
                if (grnRefDate != null && !grnRefDate.equals("")) {
                    Date grnRefDt = IdosConstants.MYSQLDF
                            .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSSDFDATE.parse(grnRefDate)));
                    invoiceLog.setGrnDate(grnRefDt);
                }
                invoiceLog.setGrnRefNumber(grnRefNumber);
                if (impRefDate != null && !impRefDate.equals("")) {
                    Date impRefDt = IdosConstants.MYSQLDF
                            .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSSDFDATE.parse(impRefDate)));
                    invoiceLog.setImportDate(impRefDt);
                }
                invoiceLog.setImportRefNumber(impRefNumber);
                if (!remarksAddDetailsBuy.equals("") && remarksAddDetailsBuy != null) {
                    invoiceLog.setRemarksAddDetails(remarksAddDetailsBuy);
                }
            }
            genericDao.saveOrUpdate(invoiceLog, user, entityManager);
        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
            throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF, IdosConstants.TECHNICAL_EXCEPTION,
                    IdosConstants.NULL_KEY_EXC_ESMF_MSG, "Error on saving invoice log.");
        }
        return 1;
    }

    @Override
    public List<InvoiceReportModel> getBuyTxnGstInvoice(Organization org, Transaction transaction,
            EntityManager entityManager) {
        log.log(Level.FINE, "****** Start");
        List<InvoiceReportModel> listInvoiceReport = new ArrayList<InvoiceReportModel>();
        Map<String, Object> criterias = new HashMap<String, Object>();
        if (transaction != null) {
            criterias.put("transaction.id", transaction.getId());
            criterias.put("presentStatus", 1);
            List<TransactionItems> listTransactionItems = genericDao.findByCriteria(TransactionItems.class, criterias,
                    entityManager);
            if (listTransactionItems != null && listTransactionItems.size() > 0) {
                double totalNetAmtWithoutAdv = 0.0;
                int counter = 1;
                double totalTaxableValue = 0.0;
                double CGSTTax = 0.0;
                double SGSTTax = 0.0;
                for (TransactionItems txnItemrow : listTransactionItems) {
                    InvoiceReportModel invoiceRecord = new InvoiceReportModel();
                    invoiceRecord.setItemNo(String.valueOf(counter));
                    counter++;
                    String description1 = null;
                    if (txnItemrow.getTransactionSpecifics() != null
                            && txnItemrow.getTransactionSpecifics().getIsInvoiceDescription1() != null
                            && txnItemrow.getTransactionSpecifics().getIsInvoiceDescription1() == 1) {
                        description1 = txnItemrow.getTransactionSpecifics().getInvoiceItemDescription1();
                    }
                    description1 = description1 == null ? "" : description1;
                    String description2 = null;
                    if (txnItemrow.getTransactionSpecifics() != null
                            && txnItemrow.getTransactionSpecifics().getIsInvoiceDescription2() != null
                            && txnItemrow.getTransactionSpecifics().getIsInvoiceDescription2() == 1) {
                        description2 = txnItemrow.getTransactionSpecifics().getInvoiceItemDescription2();
                    }
                    description2 = description2 == null ? "" : description2;
                    if (txnItemrow.getTransactionSpecifics() != null
                            && txnItemrow.getTransactionSpecifics().getName() != null) {
                        invoiceRecord.setItemName(txnItemrow.getTransactionSpecifics().getName() + " " + description1
                                + " " + description2);
                    } else {
                        invoiceRecord.setItemName(description1 + " " + description2);
                    }
                    if (txnItemrow.getTransactionSpecifics() != null
                            && txnItemrow.getTransactionSpecifics().getGstItemCode() != null) {
                        invoiceRecord.setHsnCode(txnItemrow.getTransactionSpecifics().getGstItemCode());
                    }
                    if (txnItemrow.getNoOfUnits() != null) {
                        invoiceRecord.setUnits(String.valueOf(txnItemrow.getNoOfUnits()));
                    }
                    if (txnItemrow.getTransactionSpecifics() != null
                            && txnItemrow.getTransactionSpecifics().getIncomeUnitsMeasure() != null) {
                        invoiceRecord.setUnitOfMeasure(txnItemrow.getTransactionSpecifics().getIncomeUnitsMeasure());
                    }
                    invoiceRecord.setPricePerUnit(IdosConstants.decimalFormat
                            .format(txnItemrow.getPricePerUnit() == null ? 0.0 : txnItemrow.getPricePerUnit()));
                    invoiceRecord.setTotal(IdosConstants.decimalFormat
                            .format(txnItemrow.getGrossAmount() == null ? 0.0 : txnItemrow.getGrossAmount()));
                    if (txnItemrow.getDiscountPercent() != null && !""
                            .equals(txnItemrow.getDiscountPercent() == null ? 0.0 : txnItemrow.getDiscountPercent())) {
                        invoiceRecord.setDiscountPer(IdosConstants.decimalFormat
                                .format(Double.parseDouble(txnItemrow.getDiscountPercent())));
                    }
                    if (txnItemrow.getDiscountAmount() != null && !"".equals(txnItemrow.getDiscountAmount())) {
                        invoiceRecord
                                .setDiscountAmt(IdosConstants.decimalFormat.format(txnItemrow.getDiscountAmount()));
                    }
                    invoiceRecord.setTaxableAmount(IdosConstants.decimalFormat
                            .format(txnItemrow.getGrossAmount() != null ? 0.0 : txnItemrow.getGrossAmount()));
                    double netAmtWithoutAdv = 0.0;
                    if (txnItemrow.getGrossAmount() != null) {
                        netAmtWithoutAdv += txnItemrow.getGrossAmount();
                    }
                    if (txnItemrow.getTotalTax() != null) {
                        netAmtWithoutAdv += txnItemrow.getTotalTax();
                    }
                    totalNetAmtWithoutAdv = totalNetAmtWithoutAdv + netAmtWithoutAdv;
                    invoiceRecord.setGrossInvoiceValue(IdosConstants.decimalFormat.format(netAmtWithoutAdv));
                    if (txnItemrow.getTaxValue1() != null) {
                        invoiceRecord.setTaxValue1(IdosConstants.decimalFormat.format(txnItemrow.getTaxValue1()));
                        CGSTTax = CGSTTax + txnItemrow.getTaxValue1();
                    }
                    if (txnItemrow.getTaxValue2() != null) {
                        invoiceRecord.setTaxValue2(IdosConstants.decimalFormat.format(txnItemrow.getTaxValue2()));
                        SGSTTax = SGSTTax + txnItemrow.getTaxValue2();
                    }
                    if (txnItemrow.getTaxValue3() != null) {
                        invoiceRecord.setTaxValue3(IdosConstants.decimalFormat.format(txnItemrow.getTaxValue3()));
                    }
                    if (txnItemrow.getTaxValue4() != null) {
                        invoiceRecord.setTaxValue4(IdosConstants.decimalFormat.format(txnItemrow.getTaxValue4()));
                    }
                    if (txnItemrow.getTaxValue5() != null) {
                        invoiceRecord.setTaxValue5(IdosConstants.decimalFormat.format(txnItemrow.getTaxValue5()));
                    }

                    if (txnItemrow.getTaxRate4() != null) {
                        invoiceRecord.setTaxRate4(IdosConstants.decimalFormat.format(txnItemrow.getTaxRate4()));
                    }

                    if (txnItemrow.getTaxRate3() != null) {
                        invoiceRecord.setTaxRate3(IdosConstants.decimalFormat.format(txnItemrow.getTaxRate3()));
                    }
                    /*
                     * //Temp hardcoding for kaizla demo
                     * if(counter == 2){ //Row 1
                     * invoiceRecord.setTaxRate1("2.5%");//CGST
                     * invoiceRecord.setTaxRate2("2.5%");//SGST
                     * }else if(counter == 3){ //Row 2 taxes
                     * invoiceRecord.setTaxRate1("6%");
                     * invoiceRecord.setTaxRate2("6%");
                     * }else if(counter == 4){ //Row 3
                     * invoiceRecord.setTaxRate1("9%");
                     * invoiceRecord.setTaxRate2("9%");
                     * }
                     */
                    if (txnItemrow.getTaxRate1() != null && txnItemrow.getTaxRate2() != null) {
                        invoiceRecord.setTaxRate1(Double.toString(txnItemrow.getTaxRate1()));
                        invoiceRecord.setTaxRate2(Double.toString(txnItemrow.getTaxRate2()));
                    }
                    if (txnItemrow.getTransactionSpecifics() != null
                            && txnItemrow.getTransactionSpecifics().getOrganization() != null) {
                        if (txnItemrow.getTransactionSpecifics().getOrganization().getName() != null) {
                            invoiceRecord
                                    .setCompanyName(txnItemrow.getTransactionSpecifics().getOrganization().getName());
                        }
                        if (txnItemrow.getTransactionSpecifics().getOrganization().getRegisteredAddress() != null) {
                            invoiceRecord.setCompanyAddress(
                                    txnItemrow.getTransactionSpecifics().getOrganization().getRegisteredAddress());
                        }
                        if (txnItemrow.getTransactionSpecifics().getOrganization().getRegisteredAddress() != null) {
                            invoiceRecord.setCompanyAddress(
                                    txnItemrow.getTransactionSpecifics().getOrganization().getRegisteredAddress());
                        }
                        if (txnItemrow.getTransactionSpecifics().getOrganization().getCorporateMail() != null) {
                            invoiceRecord.setCompanyEmail(
                                    txnItemrow.getTransactionSpecifics().getOrganization().getCorporateMail());
                        }
                        if (txnItemrow.getTransactionSpecifics().getOrganization().getRegisteredPhoneNumber() != null) {
                            invoiceRecord.setCompanyPhNo(
                                    txnItemrow.getTransactionSpecifics().getOrganization().getRegisteredPhoneNumber());
                        }
                    }

                    if (transaction.getTransactionVendorCustomer() != null
                            && transaction.getTransactionVendorCustomer().getName() != null) {
                        invoiceRecord.setCustomerName((transaction.getTransactionVendorCustomer().getName()));
                    }
                    listInvoiceReport.add(invoiceRecord);
                }
                // set tax names in first record, as it appears as label. So even if first sell
                // item is freight outward which has no taxes,
                // but third record has 4 taxes set, then in lable we should see names of those
                // 4 taxes
                InvoiceReportModel taxNameInvoiceModel = listInvoiceReport.get(0);
                taxNameInvoiceModel.setTaxName2("CGST");
                taxNameInvoiceModel.setTaxName1("SGST");
                taxNameInvoiceModel.setTaxName3("IGST");
                taxNameInvoiceModel.setTaxName4("CESS");
                taxNameInvoiceModel.setTaxName5("Tax5");
                taxNameInvoiceModel.setTaxValue6(IdosConstants.decimalFormat.format(CGSTTax)); // CSGST total
                taxNameInvoiceModel.setTaxValue7(IdosConstants.decimalFormat.format(SGSTTax)); // SGST total
                long totalNetAmtWithoutAdvLong = Math.round(totalNetAmtWithoutAdv);
                taxNameInvoiceModel.setNetAmt(IdosConstants.decimalFormat.format(totalNetAmtWithoutAdvLong));
                long totalTaxableVal = Math.round(totalTaxableValue);
                taxNameInvoiceModel.setTotalTaxableAmount(IdosConstants.decimalFormat.format(totalTaxableVal));
            }
        }
        log.log(Level.FINE, "****** End " + listInvoiceReport);
        return listInvoiceReport;
    }

    @Override
    public Map<String, Object> getGRNParams(Transaction transaction, JsonNode json, List<GRNoteModel> dataList,
            TransactionInvoice invoiceLog) throws Exception {
        if (transaction == null) {
            return null;
        }
        long txnPurpose = transaction.getTransactionPurpose().getId();
        Map<String, Object> params = new HashMap<String, Object>();
        Branch branch = Branch.findById(transaction.getTransactionBranch().getId());
        StringBuilder branchDetail = new StringBuilder("");
        if (branch != null) {
            if (branch.getAddress() != null)
                branchDetail.append(branch.getAddress());
            if (branch.getLocation() != null)
                branchDetail.append(", ").append(branch.getLocation());
            if (branch.getStateCode() != null)
                branchDetail.append(", ").append(IdosConstants.STATE_CODE_MAPPING.get(branch.getStateCode()));
        }
        if (transaction.getTransactionBranchOrganization() != null) {
            String companyLogo = FileUtil.getCompanyLogo(transaction.getTransactionBranchOrganization());
            if (companyLogo != null && !"".equals(companyLogo)) {
                params.put("companyLogo", companyLogo);
            }
            if (transaction.getTransactionBranchOrganization().getName() != null)
                params.put("companyName", transaction.getTransactionBranchOrganization().getName());
        }

        params.put("branchAddress", IdosUtil.replaceFormatingChar(branchDetail.toString()));
        // params.put("branchGstin", branch.getGstin());

        if (transaction.getTransactionVendorCustomer() != null
                && transaction.getTransactionVendorCustomer().getName() != null) {
            params.put("vendorName", transaction.getTransactionVendorCustomer().getName());
        } else if (transaction.getTransactionUnavailableVendorCustomer() != null) {
            params.put("vendorName", transaction.getTransactionUnavailableVendorCustomer());
        }
        if (transaction.getTransactionDate() != null) {
            params.put("date", transaction.getTransactionDate());
        } else {
            params.put("date", "");
        }
        if (transaction.getTransactionRefNumber() != null) {
            params.put("serialNo", transaction.getTransactionRefNumber());
        } else {
            params.put("serialNo", "");
        }
        if ((txnPurpose == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                || txnPurpose == IdosConstants.BUY_ON_CREDIT_PAY_LATER
                || txnPurpose == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER)) {
            if (invoiceLog != null) {
                if (invoiceLog.getInvRefNumber() != null && !invoiceLog.getInvRefNumber().equals("")) {
                    params.put("invRefNo", invoiceLog.getInvRefNumber());
                } else {
                    params.put("invRefNo", "");
                }
                if (invoiceLog.getInvRefDate() != null) {
                    params.put("invRefDate", IdosConstants.IDOSDF.format(invoiceLog.getInvRefDate()));
                } else {
                    params.put("invRefDate", "");
                }
                if (invoiceLog.getRemarksAddDetails() != null) {
                    params.put("addDetailsRemarks", invoiceLog.getRemarksAddDetails());
                } else {
                    params.put("addDetailsRemarks", "");
                }
                String invoiceName = "grnreport";
                params.put("invoiceName", invoiceName);
            }
        }
        return params;
    }

    @Override
    public List<GRNoteModel> generateGRNDatas(Organization org, Transaction transaction, EntityManager entityManager) {
        log.log(Level.FINE, "****** Start");
        List<GRNoteModel> listGRNote = new ArrayList<GRNoteModel>();
        Map<String, Object> criterias = new HashMap<String, Object>();
        if (transaction != null) {
            criterias.put("transaction.id", transaction.getId());
            criterias.put("presentStatus", 1);
            List<TransactionItems> listTransactionItems = genericDao.findByCriteria(TransactionItems.class, criterias,
                    entityManager);
            if (listTransactionItems != null && listTransactionItems.size() > 0) {
                int counter = 1;
                for (TransactionItems txnItemrow : listTransactionItems) {
                    GRNoteModel GRNote = new GRNoteModel();
                    GRNote.setItemNo(String.valueOf(counter));
                    counter++;
                    String description1 = null;
                    if (txnItemrow.getTransactionSpecifics() != null
                            && txnItemrow.getTransactionSpecifics().getIsInvoiceDescription1() != null
                            && txnItemrow.getTransactionSpecifics().getIsInvoiceDescription1() == 1) {
                        description1 = txnItemrow.getTransactionSpecifics().getInvoiceItemDescription1();
                    }
                    description1 = description1 == null ? "" : description1;
                    String description2 = null;
                    if (txnItemrow.getTransactionSpecifics() != null
                            && txnItemrow.getTransactionSpecifics().getIsInvoiceDescription2() != null
                            && txnItemrow.getTransactionSpecifics().getIsInvoiceDescription2() == 1) {
                        description2 = txnItemrow.getTransactionSpecifics().getInvoiceItemDescription2();
                    }
                    description2 = description2 == null ? "" : description2;
                    if (txnItemrow.getTransactionSpecifics() != null
                            && txnItemrow.getTransactionSpecifics().getName() != null) {
                        GRNote.setItemName(txnItemrow.getTransactionSpecifics().getName() + " " + description1 + " "
                                + description2);
                    } else {
                        GRNote.setItemName(description1 + " " + description2);
                    }

                    if (txnItemrow.getNoOfUnits() != null) {
                        GRNote.setUnits(String.valueOf(txnItemrow.getNoOfUnits()));
                    } else {
                        GRNote.setUnits("");
                    }
                    if (txnItemrow.getTransactionSpecifics() != null
                            && txnItemrow.getTransactionSpecifics().getExpenseUnitsMeasure() != null) {
                        GRNote.setUnitOfMeasure(txnItemrow.getTransactionSpecifics().getExpenseUnitsMeasure());
                    } else {
                        GRNote.setUnitOfMeasure("");
                    }
                    if (txnItemrow.getPricePerUnit() != null) {
                        GRNote.setPricePerUnit(IdosConstants.decimalFormat.format(txnItemrow.getPricePerUnit()));
                    } else {
                        GRNote.setPricePerUnit("");
                    }
                    if (txnItemrow.getGrossAmount() != null) {
                        GRNote.setTotal(IdosConstants.decimalFormat.format(txnItemrow.getGrossAmount()));
                    } else {
                        GRNote.setTotal("");
                    }
                    listGRNote.add(GRNote);
                }
            }
        }
        log.log(Level.FINE, "****** End " + listGRNote);
        return listGRNote;
    }

    @Override
    public List<createPurchaseOrderModel> generateDataForCreatePO(Organization org, BillOfMaterialTxnModel transaction,
            EntityManager entityManager, JsonNode json, TransactionInvoice invoiceLog) throws Exception {
        List<createPurchaseOrderModel> datasList = new ArrayList<createPurchaseOrderModel>();
        List<BillOfMaterialTxnItemModel> listOfBomTxnItems = new ArrayList<BillOfMaterialTxnItemModel>();
        String bomTxnItemsQuery = "select obj from BillOfMaterialTxnItemModel obj where obj.billOfMaterialTxn.id = ?1 and obj.presentStatus=1";
        ArrayList<Object> inparam = new ArrayList<Object>(1);
        inparam.add(transaction.getId());
        listOfBomTxnItems = genericDao.queryWithParamsName(bomTxnItemsQuery, entityManager, inparam);
        if (listOfBomTxnItems != null && listOfBomTxnItems.size() > 0) {
            int counter = 1;
            for (BillOfMaterialTxnItemModel item : listOfBomTxnItems) {
                createPurchaseOrderModel datas = new createPurchaseOrderModel();
                datas.setsNo(counter);
                String itemDescription = "";
                if (item.getExpense() != null) {
                    datas.setItemName(item.getExpense().getName());
                    String description1 = null;
                    if (item.getExpense() != null && item.getExpense().getIsInvoiceDescription1() != null
                            && item.getExpense().getIsInvoiceDescription1() == 1) {
                        description1 = item.getExpense().getInvoiceItemDescription1();
                    }
                    description1 = description1 == null ? "" : description1;
                    String description2 = null;
                    if (item.getExpense() != null && item.getExpense().getIsInvoiceDescription2() != null
                            && item.getExpense().getIsInvoiceDescription2() == 1) {
                        description2 = item.getExpense().getInvoiceItemDescription2();
                    }

                    description2 = description2 == null ? "" : description2;
                    itemDescription = description1 + description2;
                }
                datas.setItemDescription(itemDescription);
                datas.setuOM(item.getMeasureName());
                datas.setNoOfUnits(item.getNoOfUnits());
                datas.setPricePerUnit(item.getPricePerUnit());
                datas.setGrossAmt(item.getTotalPrice());
                datasList.add(datas);
                counter++;
            }
        }
        return datasList;
    }

    @Override
    public Map<String, Object> getCreatePOParams(BillOfMaterialTxnModel transaction, JsonNode json,
            List<createPurchaseOrderModel> datas, TransactionInvoice invoiceLog, Users user) throws Exception {
        if (transaction == null) {
            return null;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        Branch branch = Branch.findById(transaction.getBranch().getId());
        StringBuilder branchDetail = new StringBuilder("");
        if (branch != null) {
            if (branch.getAddress() != null)
                branchDetail.append(branch.getAddress());
            if (branch.getLocation() != null)
                branchDetail.append(", ").append(branch.getLocation());
            if (branch.getStateCode() != null)
                branchDetail.append(", ").append(IdosConstants.STATE_CODE_MAPPING.get(branch.getStateCode()));

            params.put("branchAddress", IdosUtil.replaceFormatingChar(branchDetail.toString()));
            if (transaction.getOrganization() != null) {
                params.put("organizationName", transaction.getOrganization().getName());
                DigitalSignatureBranchWise digitalSignature = DigitalSignatureBranchWise
                        .findByOrgAndBranch(entityManager, transaction.getOrganization().getId(), branch.getId());
                if (digitalSignature != null) {
                    if (digitalSignature.getPersonName() != null
                            && digitalSignature.getDigitalSignDocuments().contains("4"))
                        params.put("digitalSignature", digitalSignature.getPersonName());
                }
            }
            if (branch.getOrganization() != null) {
                String companyLogo = FileUtil.getCompanyLogo(branch.getOrganization());
                if (companyLogo != null && !"".equals(companyLogo)) {
                    params.put("companyLogo", companyLogo);
                    if (transaction.getBranch().getName() != null)
                        params.put("companyName", branch.getOrganization().getName());
                }
            }
        }

        if (transaction.getCustomerVendor() != null) {
            params.put("vendorName", transaction.getCustomerVendor().getName());
            params.put("vendorAddress", transaction.getCustomerVendor().getAddress());
        } else {
            params.put("vendorName", "");
            params.put("vendorAddress", "");
        }
        if (transaction.getActionDate() != null) {
            params.put("poDate", IdosConstants.IDOSDF.format(transaction.getActionDate()));
        } else {
            params.put("poDate", "");
        }
        if (transaction.getInvoiceNumber() != null) {
            params.put("poNumber", transaction.getInvoiceNumber());
        } else {
            params.put("poNumber", "");
        }
        params.put("remarks", "Taxes are to be charged over and above this gross at applicable rates");
        return params;
    }
}

/*
 * not in use
 * 
 * @Override
 * public int saveInvoiceLog(Users user, EntityManager entityManager,
 * Transaction txn, String fileName) throws IDOSException {
 * try {
 * InvoiceLog invoiceLog =
 * InvoiceLog.findByTransactionID(entityManager,txn.getId());
 * if (invoiceLog == null) {
 * invoiceLog = new InvoiceLog();
 * }
 * Vendor customer = txn.getTransactionVendorCustomer();
 * CustomerDetail customerDetail = null;
 * if(customer != null) {
 * customerDetail = CustomerDetail.findByCustomerID(customer.getId());
 * 
 * if (customerDetail == null) {
 * invoiceLog.setShippingPhoneCountryCode(customer.getPhoneCtryCode());
 * if (customer.getPhone() != null) {
 * int k = customer.getPhone().indexOf("-");
 * invoiceLog.setShippingPhone(customer.getPhone().substring(k + 1,
 * customer.getPhone().length()));
 * }
 * invoiceLog.setShippingCountry(customer.getCountry());
 * invoiceLog.setShippingAddress(customer.getAddress());
 * invoiceLog.setShippingLocation(customer.getLocation());
 * } else {
 * invoiceLog.setShippingAddress(customerDetail.getShippingaddress());
 * if (customerDetail.getShippingphone() != null) {
 * int k = customerDetail.getShippingphone().indexOf("-");
 * invoiceLog.setShippingPhone(customerDetail.getShippingphone().substring(k +
 * 1, customerDetail.getShippingphone().length()));
 * }
 * invoiceLog.setShippingPhoneCountryCode(customerDetail.
 * getShippingphoneCtryCode());
 * invoiceLog.setShippingCountry(customerDetail.getShippingcountry());
 * invoiceLog.setShippingLocation(customerDetail.getShippinglocation());
 * }
 * invoiceLog.setOrganization(txn.getTransactionBranchOrganization());
 * invoiceLog.setBranch(txn.getTransactionBranch());
 * invoiceLog.setTransaction(txn);
 * invoiceLog.setFileName(fileName);
 * genericDao.saveOrUpdate(invoiceLog, user, entityManager);
 * }
 * }catch (Exception ex){
 * log.log(Level.SEVERE, user.getEmail(), ex);
 * throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF,
 * IdosConstants.TECHNICAL_EXCEPTION, IdosConstants.NULL_KEY_EXC_ESMF_MSG,
 * "Error on saving invoice log." );
 * }
 * return 1;
 * }
 */
