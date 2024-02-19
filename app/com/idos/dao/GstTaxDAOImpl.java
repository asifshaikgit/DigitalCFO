package com.idos.dao;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;

import model.Branch;
import model.BranchSpecifics;
import model.BranchSpecificsTaxFormula;
import model.BranchTaxes;
import model.Specifics;
import model.Users;
import model.Vendor;
import model.VendorSpecific;

import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;

import play.libs.Json;
import java.util.logging.Level;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosDaoConstants;
import com.idos.util.IdosUtil;
import play.db.jpa.JPAApi;
import javax.inject.Inject;

/**
 * Created by Sunil K Namdev on 05-07-2017.
 */
public class GstTaxDAOImpl implements GstTaxDAO {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    @Override
    public void saveUpdateBranchTax(String branchId, JsonNode json, Integer taxCategory,
            EntityManager entityManager,
            Users user) throws IDOSException {
        try {
            Branch branch = Branch.findById(IdosUtil.convertStringToLong(branchId));
            String gstin = branch.getGstin() == null ? "" : branch.getGstin();
            String branchName = branch.getName();
            if (branchName != null && branchName.length() > 3) {
                branchName = branch.getName().replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "").substring(0, 3);
            }
            if (branchName != null) {
                branchName = branchName.toUpperCase();
            }
            String taxRatesList = json.findPath("taxRates").asText();

            String taxRateArray[] = taxRatesList.substring(0, taxRatesList.length()).split(",");

            Map<String, Object> criterias = new HashMap<String, Object>(2);
            String taxNameList = json.findPath("taxNames").asText();
            String taxNameArray[] = taxNameList.substring(0, taxNameList.length()).split(",");
            int taxType = taxCategory;
            for (int i = 0; i < taxNameArray.length; i++) {
                if (taxNameArray[i] == null || "".equals(taxNameArray[i])) {
                    continue;
                }
                if (taxCategory == IdosConstants.OUTPUT_TAX) {
                    if (taxNameArray[i].startsWith("SGST")) {
                        taxType = IdosConstants.OUTPUT_SGST;
                    } else if (taxNameArray[i].startsWith("CGST")) {
                        taxType = IdosConstants.OUTPUT_CGST;
                    } else if (taxNameArray[i].startsWith("IGST")) {
                        taxType = IdosConstants.OUTPUT_IGST;
                    } else if (taxNameArray[i].startsWith("CESS")) {
                        taxType = IdosConstants.OUTPUT_CESS;
                    }
                } else if (taxCategory == IdosConstants.INPUT_TAX) {
                    if (taxNameArray[i].startsWith("SGST")) {
                        taxType = IdosConstants.INPUT_SGST;
                    } else if (taxNameArray[i].startsWith("CGST")) {
                        taxType = IdosConstants.INPUT_CGST;
                    } else if (taxNameArray[i].startsWith("IGST")) {
                        taxType = IdosConstants.INPUT_IGST;
                    } else if (taxNameArray[i].startsWith("CESS")) {
                        taxType = IdosConstants.INPUT_CESS;
                    }
                } else if (taxCategory == IdosConstants.RCM_INPUT_TAX) {
                    if (taxNameArray[i].startsWith("SGST")) {
                        taxType = IdosConstants.RCM_SGST_IN;
                    } else if (taxNameArray[i].startsWith("CGST")) {
                        taxType = IdosConstants.RCM_CGST_IN;
                    } else if (taxNameArray[i].startsWith("IGST")) {
                        taxType = IdosConstants.RCM_IGST_IN;
                    } else if (taxNameArray[i].startsWith("CESS")) {
                        taxType = IdosConstants.RCM_CESS_IN;
                    }
                } else if (taxCategory == IdosConstants.RCM_OUTPUT_TAX) {
                    if (taxNameArray[i].startsWith("SGST")) {
                        taxType = IdosConstants.RCM_SGST_OUTPUT;
                    } else if (taxNameArray[i].startsWith("CGST")) {
                        taxType = IdosConstants.RCM_CGST_OUTPUT;
                    } else if (taxNameArray[i].startsWith("IGST")) {
                        taxType = IdosConstants.RCM_IGST_OUTPUT;
                    } else if (taxNameArray[i].startsWith("CESS")) {
                        taxType = IdosConstants.RCM_CESS_OUTPUT;
                    }
                }

                if (i >= taxRateArray.length) { // user has not defined rate so no need to store.
                    break;
                }
                Double taxRate = IdosUtil.convertStringToDouble(taxRateArray[i]);
                StringBuilder taxName = new StringBuilder();
                if (taxCategory == IdosConstants.RCM_INPUT_TAX || taxCategory == IdosConstants.RCM_OUTPUT_TAX) {
                    taxName.append(taxNameArray[i]).append(" ").append(branchName).append("-").append(gstin).append("-")
                            .append(taxRate).append("%").append("-").append("RCM");
                    if (taxType >= 30 && taxType <= 33) {
                        taxName.append("-Input");
                    } else {
                        taxName.append("-Output");
                    }
                } else {
                    taxName.append(taxNameArray[i]).append(" ").append(branchName).append("-").append(gstin).append("-")
                            .append(taxRate).append("%");
                    if (taxType >= 10 && taxType <= 19) {
                        taxName.append("-Input");
                    } else {
                        taxName.append("-Output");
                    }
                }

                criterias.put("branch.id", branch.getId());
                criterias.put("taxName", taxName.toString());
                criterias.put("taxType", taxType);
                BranchTaxes branchTaxes = genericDao.getByCriteria(BranchTaxes.class, criterias, entityManager);
                if (branchTaxes == null) {
                    branchTaxes = new BranchTaxes();
                    branchTaxes.setBranch(branch);
                    branchTaxes.setTaxName(taxName.toString());
                    branchTaxes.setOrganization(user.getOrganization());
                    branchTaxes.setTaxRate(taxRate);
                    branchTaxes.setTaxType(taxType);
                    genericDao.saveOrUpdate(branchTaxes, user, entityManager);
                }
                criterias.clear();
                ;
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on fetching taxes.", ex.getMessage());
        }
    }

    @Override
    public void applyTaxRulesToEachBranchSpecifics(String specificsId, String branchId, JsonNode json,
            EntityManager entityManager, Users user) throws IDOSException {
        Branch branch = Branch.findById(IdosUtil.convertStringToLong(branchId));
        String gstin = branch.getGstin() == null ? "" : branch.getGstin();
        String branchName = branch.getName();
        if (branchName != null && branchName.length() > 3) {
            branchName = branch.getName().replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "").substring(0, 3);
        }
        if (branchName != null) {
            branchName = branchName.toUpperCase();
        }

        List<BranchTaxes> branchTaxesList = branch.getBranchTaxes();

        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("branch.id", branch.getId());
        criterias.put("specifics.id", IdosUtil.convertStringToLong(specificsId));
        criterias.put("presentStatus", 1);
        BranchSpecifics branchSpecifics = genericDao.getByCriteria(BranchSpecifics.class, criterias, entityManager);
        if (branchSpecifics == null) {
            throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                    "Mapping between Branch and Item is missing!", IdosConstants.RECORD_NOT_FOUND);
        }

        String taxNameList = json.findPath("taxNames").asText();
        String taxRatesList = json.findPath("taxRates").asText();
        String addsDeducts = json.findPath("addsDeducts").asText();
        String appliedTos = json.findPath("appliedTos").asText();
        String formulas = json.findPath("formulas").asText();
        String invoiceValues = json.findPath("invoiceValues").asText();
        // Below three are for validation and allow to add new taxes if these values
        // changed from COA.
        String itemCategory = json.findPath("itemCategory").asText();
        String itemGstCode = json.findPath("itemGstCode").asText();
        Double itemGstRate = json.findValue("itemGstRate") == null ? 0.0 : json.findValue("itemGstRate").asDouble();
        String applicableDate = json.findValue("applicableDate") == null
                || "".equals(json.findValue("applicableDate").asText()) ? null
                        : json.findValue("applicableDate").asText();

        String taxRateArray[] = taxRatesList.substring(0, taxRatesList.length()).split(",");
        String taxNameArray[] = taxNameList.substring(0, taxNameList.length()).split(",");
        String addsDeductsArr[] = addsDeducts.substring(0, addsDeducts.length()).split(",");
        String appliedTosArr[] = appliedTos.substring(0, appliedTos.length()).split(",");
        String formulasArr[] = formulas.substring(0, formulas.length()).split(",");
        String invoiceValuesArr[] = invoiceValues.substring(0, invoiceValues.length()).split(",");

        // criterias.clear();
        // criterias.put("branch.id", branch.getId());
        // criterias.put("organization.id", user.getOrganization().getId());
        // criterias.put("specifics.id", branchSpecifics.getSpecifics().getId());
        // criterias.put("presentStatus", 1);
        // List<BranchSpecificsTaxFormula> bnchSpecfTaxFormulaList =
        // genericDao.findByCriteria(BranchSpecificsTaxFormula.class, criterias,
        // entityManager);
        // for(BranchSpecificsTaxFormula branchSpecificsTaxFormula :
        // bnchSpecfTaxFormulaList){
        // branchSpecificsTaxFormula.setPresentStatus(0);
        // genericDao.saveOrUpdate(branchSpecificsTaxFormula, user, entityManager);
        // }

        BranchSpecificsTaxFormula bnchTaxFormula = null;

        for (int i = 0; i < taxNameArray.length; i++) {
            if (taxNameArray[i] == null || "".equals(taxNameArray[i])) {
                continue;
            }
            Double taxRate = IdosUtil.convertStringToDouble(taxRateArray[i]);
            int taxCategory = json.findValue("taxCategory").asInt();
            String taxName = taxNameArray[i] + " " + branchName + "-" + gstin + "-" + taxRate + "%";
            int taxType = taxCategory;
            if (taxCategory == IdosConstants.RCM_INPUT_TAX) {
                String rcmInputTaxName = taxName + "-RCM-Input";
                String rcmOutputTaxName = taxName + "-RCM-Output";
                BranchTaxes branchTaxes1 = null;
                BranchTaxes branchTaxes2 = null;
                // Input RCM TAX
                for (BranchTaxes branchTax : branchTaxesList) {
                    if (rcmInputTaxName.equalsIgnoreCase(branchTax.getTaxName())
                            && (branchTax.getTaxType() == IdosConstants.RCM_SGST_IN
                                    || branchTax.getTaxType() == IdosConstants.RCM_CGST_IN
                                    || branchTax.getTaxType() == IdosConstants.RCM_IGST_IN
                                    || branchTax.getTaxType() == IdosConstants.RCM_CESS_IN)) {
                        branchTaxes1 = branchTax;
                        break;
                    }
                }

                if (branchTaxes1 != null) {
                    bnchTaxFormula = new BranchSpecificsTaxFormula();
                    bnchTaxFormula.setBranchTaxes(branchTaxes1);
                    bnchTaxFormula.setBranch(branchTaxes1.getBranch());
                    bnchTaxFormula.setOrganization(branchTaxes1.getOrganization());
                    bnchTaxFormula.setSpecifics(branchSpecifics.getSpecifics());
                    bnchTaxFormula.setParticular(branchSpecifics.getParticular());
                    bnchTaxFormula.setBranchSpecifics(branchSpecifics);
                    bnchTaxFormula.setAddDeduct(Integer.parseInt(addsDeductsArr[i]));
                    bnchTaxFormula.setAppliedTo(appliedTosArr[i]);
                    bnchTaxFormula.setFormula(formulasArr[i]);
                    bnchTaxFormula.setInvoiceValue(invoiceValuesArr[i]);
                    bnchTaxFormula.setGstItemCategory(itemCategory);
                    bnchTaxFormula.setGstItemCode(itemGstCode);
                    bnchTaxFormula.setGstTaxRate(branchTaxes1.getTaxRate());
                    if (applicableDate != null) {
                        try {
                            Date date = new Date();
                            String dateString = IdosConstants.IDOSDF.format(IdosConstants.IDOSDF.parse(applicableDate));
                            String timeString = IdosConstants.TIMEFMT.format(date);
                            applicableDate = dateString + " " + timeString;
                            bnchTaxFormula.setApplicableFrom(IdosConstants.IDOSDTF.parse(applicableDate));
                        } catch (ParseException e) {
                            throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE,
                                    IdosConstants.DATA_FORMAT_EXCEPTION, "Data Format Exception for Date!",
                                    IdosConstants.DATA_FORMAT_ERRCODE);
                        }
                    }
                    genericDao.saveOrUpdate(bnchTaxFormula, user, entityManager);
                }
                // Output RCM TAX
                for (BranchTaxes branchTax : branchTaxesList) {
                    if (rcmOutputTaxName.equalsIgnoreCase(branchTax.getTaxName())
                            && (branchTax.getTaxType() == IdosConstants.RCM_SGST_OUTPUT
                                    || branchTax.getTaxType() == IdosConstants.RCM_CGST_OUTPUT
                                    || branchTax.getTaxType() == IdosConstants.RCM_IGST_OUTPUT
                                    || branchTax.getTaxType() == IdosConstants.RCM_CESS_OUTPUT)) {
                        branchTaxes2 = branchTax;
                        break;
                    }
                }
                if (branchTaxes2 != null) {
                    bnchTaxFormula = new BranchSpecificsTaxFormula();
                    bnchTaxFormula.setBranchTaxes(branchTaxes2);
                    bnchTaxFormula.setBranch(branchTaxes2.getBranch());
                    bnchTaxFormula.setOrganization(branchTaxes2.getOrganization());
                    bnchTaxFormula.setSpecifics(branchSpecifics.getSpecifics());
                    bnchTaxFormula.setParticular(branchSpecifics.getParticular());
                    bnchTaxFormula.setBranchSpecifics(branchSpecifics);
                    bnchTaxFormula.setAddDeduct(IdosUtil.convertStringToInt(addsDeductsArr[i]));
                    bnchTaxFormula.setAppliedTo(appliedTosArr[i]);
                    bnchTaxFormula.setFormula(formulasArr[i]);
                    bnchTaxFormula.setInvoiceValue(invoiceValuesArr[i]);
                    bnchTaxFormula.setGstItemCategory(itemCategory);
                    bnchTaxFormula.setGstItemCode(itemGstCode);
                    bnchTaxFormula.setGstTaxRate(branchTaxes2.getTaxRate());
                    if (applicableDate != null) {
                        try {
                            Date date = new Date();
                            String dateString = IdosConstants.IDOSDF.format(IdosConstants.IDOSDF.parse(applicableDate));
                            String timeString = IdosConstants.TIMEFMT.format(date);
                            applicableDate = dateString + " " + timeString;
                            bnchTaxFormula.setApplicableFrom(IdosConstants.IDOSDTF.parse(applicableDate));
                        } catch (ParseException e) {
                            throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE,
                                    IdosConstants.DATA_FORMAT_EXCEPTION, "Data Format Exception for Date!",
                                    IdosConstants.DATA_FORMAT_ERRCODE);
                        }
                    }
                    genericDao.saveOrUpdate(bnchTaxFormula, user, entityManager);
                }

            } else {
                if (taxType >= 10 && taxType <= 19) {
                    taxName += "-Input";
                } else {
                    taxName += "-Output";
                }

                BranchTaxes branchTaxes = null;
                for (BranchTaxes branchTax : branchTaxesList) {
                    if (taxName.equalsIgnoreCase(branchTax.getTaxName())
                            && (branchTax.getTaxType() == IdosConstants.OUTPUT_SGST
                                    || branchTax.getTaxType() == IdosConstants.OUTPUT_CGST
                                    || branchTax.getTaxType() == IdosConstants.OUTPUT_IGST
                                    || branchTax.getTaxType() == IdosConstants.OUTPUT_CESS)) {
                        branchTaxes = branchTax;
                        break;
                    }
                }
                if (branchTaxes != null) {
                    bnchTaxFormula = new BranchSpecificsTaxFormula();
                    bnchTaxFormula.setBranchTaxes(branchTaxes);
                    bnchTaxFormula.setBranch(branchTaxes.getBranch());
                    bnchTaxFormula.setOrganization(branchTaxes.getOrganization());
                    bnchTaxFormula.setSpecifics(branchSpecifics.getSpecifics());
                    bnchTaxFormula.setParticular(branchSpecifics.getParticular());
                    bnchTaxFormula.setBranchSpecifics(branchSpecifics);
                    bnchTaxFormula.setAddDeduct(IdosUtil.convertStringToInt(addsDeductsArr[i]));
                    bnchTaxFormula.setAppliedTo(appliedTosArr[i]);
                    bnchTaxFormula.setFormula(formulasArr[i]);
                    bnchTaxFormula.setInvoiceValue(invoiceValuesArr[i]);
                    bnchTaxFormula.setGstItemCategory(itemCategory);
                    bnchTaxFormula.setGstItemCode(itemGstCode);
                    bnchTaxFormula.setGstTaxRate(branchTaxes.getTaxRate());
                    if (applicableDate != null) {
                        try {
                            Date date = new Date();
                            String dateString = IdosConstants.IDOSDF.format(IdosConstants.IDOSDF.parse(applicableDate));
                            String timeString = IdosConstants.TIMEFMT.format(date);
                            applicableDate = dateString + " " + timeString;
                            bnchTaxFormula.setApplicableFrom(IdosConstants.IDOSDTF.parse(applicableDate));
                        } catch (ParseException e) {
                            throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE,
                                    IdosConstants.DATA_FORMAT_EXCEPTION, "Data Format Exception for Date!",
                                    IdosConstants.DATA_FORMAT_ERRCODE);
                        }
                    }
                    genericDao.saveOrUpdate(bnchTaxFormula, user, entityManager);
                }
            }

        }
    }

    @Override
    public void applyTaxRulesToMultipleBranchSpecifics(String specificsId, String branchId, JsonNode json,
            EntityManager entityManager, Users user) throws IDOSException {
        String multiItemsSpecificsId = json.findValue("applyRulesToMultiItemsList").asText();
        String taxNameList = json.findPath("taxNames").asText();
        String addsDeducts = json.findPath("addsDeducts").asText();
        String appliedTos = json.findPath("appliedTos").asText();
        String formulas = json.findPath("formulas").asText();
        String invoiceValues = json.findPath("invoiceValues").asText();
        String applicableDate = json.findValue("applicableDate") == null
                || "".equals(json.findValue("applicableDate").asText()) ? null
                        : json.findValue("applicableDate").asText();

        String taxNameArray[] = taxNameList.substring(0, taxNameList.length()).split(",");
        String addsDeductsArr[] = addsDeducts.substring(0, addsDeducts.length()).split(",");
        String appliedTosArr[] = appliedTos.substring(0, appliedTos.length()).split(",");
        String formulasArr[] = formulas.substring(0, formulas.length()).split(",");
        String invoiceValuesArr[] = invoiceValues.substring(0, invoiceValues.length()).split(",");

        Map<String, Object> criterias = new HashMap<String, Object>(2);
        BranchSpecificsTaxFormula bnchTaxFormula = null;
        String[] itemsList = null;
        if (multiItemsSpecificsId != null && multiItemsSpecificsId != "") {
            itemsList = multiItemsSpecificsId.split(",");
        }
        Branch branch = Branch.findById(IdosUtil.convertStringToLong(branchId));
        String gstin = branch.getGstin() == null ? "" : branch.getGstin();
        String branchName = branch.getName();
        if (branchName != null && branchName.length() > 3) {
            branchName = branch.getName().replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "").substring(0, 3);
        }
        if (branchName != null) {
            branchName = branchName.toUpperCase();
        }
        if (itemsList != null) { // if multiple items are selected to apply same tax rules
            for (int x = 0; x < itemsList.length; x++) {
                if (!specificsId.equals(itemsList[x])) { // specificsId is original item, for which rules are set which
                                                         // is already inserted in above function from GstController
                    Specifics specificsentity = Specifics.findById(IdosUtil.convertStringToLong(itemsList[x]));
                    String itemCategory = specificsentity.getGstTypeOfSupply();
                    String hsnSacCode = specificsentity.getGstItemCode();
                    double sgstRate = 0, cgstRate = 0.0, igstRate = 0.0, cessRate = 0.0;
                    if (specificsentity.getGstTaxRate() != null) {
                        Double otherRates = specificsentity.getGstTaxRate() / 2;
                        sgstRate = otherRates;
                        cgstRate = otherRates;
                        igstRate = specificsentity.getGstTaxRate();
                    }
                    if (specificsentity.getCessTaxRate() != null) {
                        cessRate = specificsentity.getCessTaxRate();
                    }
                    criterias.clear();
                    criterias.put("branch.id", branch.getId());
                    criterias.put("specifics.id", IdosUtil.convertStringToLong(itemsList[x]));
                    criterias.put("presentStatus", 1);
                    BranchSpecifics branchSpecifics = genericDao.getByCriteria(BranchSpecifics.class, criterias,
                            entityManager);
                    if (branchSpecifics == null) {
                        throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                                "Mapping between Branch and Item is missing!", IdosConstants.RECORD_NOT_FOUND);
                    }

                    // //invalidate previous taxes by making its present_status=0;
                    // criterias.clear();
                    // criterias.put("branch.id", branch.getId());
                    // criterias.put("organization.id", user.getOrganization().getId());
                    // criterias.put("specifics.id", branchSpecifics.getSpecifics().getId());
                    // criterias.put("presentStatus", 1);
                    // List<BranchSpecificsTaxFormula> bnchSpecfTaxFormulaList =
                    // genericDao.findByCriteria(BranchSpecificsTaxFormula.class, criterias,
                    // entityManager);
                    // for(BranchSpecificsTaxFormula branchSpecificsTaxFormula :
                    // bnchSpecfTaxFormulaList){
                    // branchSpecificsTaxFormula.setPresentStatus(0);
                    // genericDao.saveOrUpdate(branchSpecificsTaxFormula, user, entityManager);
                    // }
                    // Insert into Branch Taxes to show it on TB & store formula in
                    // BranchSpecificaTaxFormula
                    int taxType = 0;
                    double taxRate = 0.0;
                    for (int i = 0; i < taxNameArray.length; i++) {
                        if (taxNameArray[i].startsWith("SGST")) {
                            taxType = IdosConstants.OUTPUT_SGST;
                            taxRate = sgstRate;
                        } else if (taxNameArray[i].startsWith("CGST")) {
                            taxType = IdosConstants.OUTPUT_CGST;
                            taxRate = cgstRate;
                        } else if (taxNameArray[i].startsWith("IGST")) {
                            taxType = IdosConstants.OUTPUT_IGST;
                            taxRate = igstRate;
                        } else if (taxNameArray[i].startsWith("CESS")) {
                            taxType = IdosConstants.OUTPUT_CESS;
                            taxRate = cessRate;
                        }
                        String taxName = taxNameArray[i] + " " + branchName + "-" + gstin + "-" + taxRate + "%";
                        criterias.clear();
                        criterias.put("branch.id", branch.getId());
                        criterias.put("taxName", taxName);
                        criterias.put("taxType", taxType);
                        criterias.put("presentStatus", 1);
                        BranchTaxes branchTaxes = genericDao.getByCriteria(BranchTaxes.class, criterias, entityManager);
                        if (branchTaxes == null) {
                            branchTaxes = new BranchTaxes();
                            branchTaxes.setBranch(branch);
                            branchTaxes.setTaxName(taxName);
                            branchTaxes.setOrganization(user.getOrganization());
                            branchTaxes.setTaxRate(taxRate);
                            branchTaxes.setTaxType(taxType);
                            genericDao.saveOrUpdate(branchTaxes, user, entityManager);
                        }

                        bnchTaxFormula = new BranchSpecificsTaxFormula();
                        bnchTaxFormula.setBranchTaxes(branchTaxes);
                        bnchTaxFormula.setBranch(branchTaxes.getBranch());
                        bnchTaxFormula.setOrganization(branchTaxes.getOrganization());
                        bnchTaxFormula.setSpecifics(branchSpecifics.getSpecifics());
                        bnchTaxFormula.setParticular(branchSpecifics.getParticular());
                        bnchTaxFormula.setBranchSpecifics(branchSpecifics);
                        bnchTaxFormula.setAddDeduct(IdosUtil.convertStringToInt(addsDeductsArr[i]));
                        bnchTaxFormula.setAppliedTo(appliedTosArr[i]);
                        bnchTaxFormula.setFormula(formulasArr[i]);
                        bnchTaxFormula.setInvoiceValue(invoiceValuesArr[i]);
                        bnchTaxFormula.setGstItemCategory(itemCategory);
                        bnchTaxFormula.setGstItemCode(hsnSacCode);
                        bnchTaxFormula.setGstTaxRate(branchTaxes.getTaxRate());
                        if (applicableDate != null) {
                            try {
                                Date date = new Date();
                                String dateString = IdosConstants.IDOSDF
                                        .format(IdosConstants.IDOSDF.parse(applicableDate));
                                String timeString = IdosConstants.TIMEFMT.format(date);
                                applicableDate = dateString + " " + timeString;
                                bnchTaxFormula.setApplicableFrom(IdosConstants.IDOSDTF.parse(applicableDate));
                            } catch (ParseException e) {
                                throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE,
                                        IdosConstants.DATA_FORMAT_EXCEPTION, "Data Format Exception for Date!",
                                        IdosConstants.DATA_FORMAT_ERRCODE);
                            }
                        }
                        genericDao.saveOrUpdate(bnchTaxFormula, user, entityManager);
                    }
                }
            }
        }
    }

    @Override
    public void saveInputTaxBranch(JsonNode json, EntityManager entityManager, Users user) throws IDOSException {
        try {
            String inputTaxData = json.findValue("inputTaxData").toString();
            JSONArray inTaxDataArray = new JSONArray(inputTaxData);
            for (int i = 0; i < inTaxDataArray.length(); i++) {
                JSONObject taxDataRecord = new JSONObject(inTaxDataArray.get(i).toString());
                String branches = taxDataRecord.getString("branches");
                String[] branchArray = branches.split(",");
                String gstTaxRateTmp = taxDataRecord.getString("gstTaxRates");
                Double gstTaxRate = -1d;
                if (gstTaxRateTmp != null && !"".equals(gstTaxRateTmp)) {
                    gstTaxRate = taxDataRecord.getDouble("gstTaxRates");
                }
                String cessRateTmp = taxDataRecord.getString("cessTaxRates");
                Double cessRate = -1d;
                if (cessRateTmp != null && !"".equals(cessRateTmp)) {
                    cessRate = taxDataRecord.getDouble("cessTaxRates");
                }

                // req Branch List
                for (String branchid : branchArray) {
                    Long branchId = branchid == null ? 0 : IdosUtil.convertStringToLong(branchid);
                    Branch branch = Branch.findById(branchId);
                    if (gstTaxRate != -1d && gstTaxRate >= 0.0) {
                        Double rate = gstTaxRate / 2;
                        saveInputTax(branch, rate, IdosConstants.INPUT_SGST, user, "SGST", entityManager);
                        saveInputTax(branch, rate, IdosConstants.INPUT_CGST, user, "CGST", entityManager);
                        saveInputTax(branch, gstTaxRate, IdosConstants.INPUT_IGST, user, "IGST", entityManager);
                    }
                    if (cessRate != -1d && cessRate >= 0.0) {
                        saveInputTax(branch, cessRate, IdosConstants.INPUT_CESS, user, "CESS", entityManager);
                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on save/update input taxes.", ex.getMessage());
        }
    }

    private void saveInputTax(Branch branch, Double rate, int type, Users user, String name,
            EntityManager entityManager) {
        String gstin = branch.getGstin() == null ? "" : branch.getGstin();
        String branchName = branch.getName();
        if (branchName != null && branchName.length() > 3) {
            branchName = branch.getName().replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "").substring(0, 3);
        }
        if (branchName != null) {
            branchName = branchName.toUpperCase();
        }
        StringBuilder taxName = new StringBuilder(name).append(" ").append(branchName).append("-").append(gstin)
                .append("-").append(rate).append("%");
        if (type >= 10 && type <= 19) {
            taxName.append("-Input");
        } else {
            taxName.append("-Output");
        }
        BranchTaxes branchTaxes = BranchTaxes.findByRateType(entityManager, branch.getId(),
                user.getOrganization().getId(), type, rate);
        // if(branchTaxes != null){
        // branchTaxes.setPresentStatus(0);
        // genericDao.saveOrUpdate(branchTaxes, user, em);
        // }
        if (branchTaxes == null) {
            branchTaxes = new BranchTaxes();
            branchTaxes.setBranch(branch);
            branchTaxes.setTaxName(taxName.toString());
            branchTaxes.setOrganization(user.getOrganization());
            branchTaxes.setTaxRate(rate);
            branchTaxes.setTaxType(type);
            genericDao.saveOrUpdate(branchTaxes, user, entityManager);
        }

    }

    @Override
    public void saveRcmTaxBranch(JsonNode json, EntityManager entityManager, Users user) throws IDOSException {
        try {
            String rcmTaxData = json.findValue("rcmTaxData").toString();
            JSONArray rcmTaxDataArray = new JSONArray(rcmTaxData);
            for (int i = 0; i < rcmTaxDataArray.length(); i++) {
                JSONObject taxDataRecord = new JSONObject(rcmTaxDataArray.get(i).toString());
                Long branchId = taxDataRecord.getLong("branch");
                Long vendorId = taxDataRecord.getLong("vendor");
                String typeOfSupply = taxDataRecord.getString("typeOfSupply");
                String description = taxDataRecord.getString("description");
                String hsnSacCode = taxDataRecord.getString("hsnSacCode");
                Double gstTaxRate = taxDataRecord.getDouble("gstTaxRates");
                String cessRate = taxDataRecord.getString("cessTaxRates");
                Long itemId = taxDataRecord.getLong("itemId");
                Branch branch = Branch.findById(branchId);
                Vendor vendor = Vendor.findById(vendorId);
                Specifics specifics = Specifics.findById(itemId);

                Double rate = gstTaxRate / 2;
                saveRcmTax(branch, rate, IdosConstants.RCM_SGST_IN, user, "SGST", entityManager, specifics,
                        typeOfSupply, hsnSacCode, vendor, description, new Date());
                saveRcmTax(branch, rate, IdosConstants.RCM_CGST_IN, user, "CGST", entityManager, specifics,
                        typeOfSupply, hsnSacCode, vendor, description, new Date());
                saveRcmTax(branch, gstTaxRate, IdosConstants.RCM_IGST_IN, user, "IGST", entityManager, specifics,
                        typeOfSupply, hsnSacCode, vendor, description, new Date());
                saveRcmTax(branch, rate, IdosConstants.RCM_SGST_OUTPUT, user, "SGST", entityManager, specifics,
                        typeOfSupply, hsnSacCode, vendor, description, new Date());
                saveRcmTax(branch, rate, IdosConstants.RCM_CGST_OUTPUT, user, "CGST", entityManager, specifics,
                        typeOfSupply, hsnSacCode, vendor, description, new Date());
                saveRcmTax(branch, gstTaxRate, IdosConstants.RCM_IGST_OUTPUT, user, "IGST", entityManager, specifics,
                        typeOfSupply, hsnSacCode, vendor, description, new Date());
                if (!cessRate.equals("")) {
                    saveRcmTax(branch, Double.valueOf(cessRate), IdosConstants.RCM_CESS_IN, user, "CESS", entityManager,
                            specifics, typeOfSupply, hsnSacCode, vendor, description, new Date());
                    saveRcmTax(branch, Double.valueOf(cessRate), IdosConstants.RCM_CESS_OUTPUT, user, "CESS",
                            entityManager, specifics, typeOfSupply, hsnSacCode, vendor, description, new Date());
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on save/update RCM taxes.", ex.getMessage());
        }
    }

    private void saveRcmTax(Branch branch, Double rate, int type, Users user, String name, EntityManager entityManager,
            Specifics specifics, String itemCategory, String hsnCode, Vendor vendor, String desc, Date applicableDate)
            throws IDOSException {
        String gstin = branch.getGstin() == null ? "" : branch.getGstin();
        String branchName = branch.getName();
        if (branchName != null && branchName.length() > 3) {
            branchName = branch.getName().replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "").substring(0, 3);
        }
        if (branchName != null) {
            branchName = branchName.toUpperCase();
        }
        StringBuilder taxName = new StringBuilder(name).append(" ").append(branchName).append("-").append(gstin)
                .append("-").append(rate).append("%").append("-").append("RCM");
        if (type >= 30 && type <= 33) {
            taxName.append("-Input");
        } else {
            taxName.append("-Output");
        }
        BranchTaxes branchTaxes = BranchTaxes.findByRateType(entityManager, branch.getId(),
                user.getOrganization().getId(), type, rate);
        if (branchTaxes == null) {
            branchTaxes = new BranchTaxes();
        }
        branchTaxes.setBranch(branch);
        branchTaxes.setTaxName(taxName.toString());
        branchTaxes.setOrganization(user.getOrganization());
        branchTaxes.setTaxRate(rate);
        branchTaxes.setTaxType(type);
        genericDao.saveOrUpdate(branchTaxes, user, entityManager);

        Integer addDeduct = 1;
        String appliedTo = "GV";
        String formula = null;
        String invoiceValue = null;

        if (name.equals("SGST")) {
            formula = "sgst=GV*(Rate/100)";
            invoiceValue = "IV1=GV+(sgst)";
        }
        if (name.equals("CGST")) {
            formula = "cgst=GV*(Rate/100)";
            invoiceValue = "IV2=GV+(sgst+cgst)";
        }
        if (name.equals("IGST")) {
            formula = "igst=GV*(Rate/100)";
            invoiceValue = "IV3=GV+(sgst+cgst+igst)";
        }
        if (name.equals("CESS")) {
            formula = "cess=GV*(Rate/100)";
            invoiceValue = "IV4=GV+(sgst+cgst+igst+cess)";
        }

        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("branch.id", branch.getId());
        criterias.put("specifics.id", specifics.getId());
        criterias.put("presentStatus", 1);
        BranchSpecifics branchSpecifics = genericDao.getByCriteria(BranchSpecifics.class, criterias, entityManager);
        if (branchSpecifics == null) {
            throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                    IdosConstants.RECORD_NOT_FOUND,
                    "Mapping between Branch (" + branch.getName() + ") and Item (" + specifics.getName()
                            + ") is missing! Please create mapping from COA setup.");
        }
        // BranchSpecificsTaxFormula bnchTaxFormula =
        // BranchSpecificsTaxFormula.findByTaxBranch(branch.getId(),
        // user.getOrganization().getId(), branchTaxes.getId());
        // BranchSpecificsTaxFormula bnchTaxFormula =
        // BranchSpecificsTaxFormula.findByTaxBranchAndSpecific(user.getOrganization().getId(),branch.getId(),
        // branchTaxes.getId(),specifics.getId(),vendor.getId(), desc);
        criterias.clear();
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("branch.id", branch.getId());
        criterias.put("branchTaxes.id", branchTaxes.getId());
        criterias.put("specifics.id", specifics.getId());
        criterias.put("vendor.id", vendor.getId());
        criterias.put("hsnDesc", desc);
        criterias.put("presentStatus", 1);
        BranchSpecificsTaxFormula bnchTaxFormula = genericDao.getByCriteria(BranchSpecificsTaxFormula.class, criterias,
                entityManager);
        if (bnchTaxFormula == null) {
            bnchTaxFormula = new BranchSpecificsTaxFormula();
            bnchTaxFormula.setBranchTaxes(branchTaxes);
            bnchTaxFormula.setBranch(branchTaxes.getBranch());
            bnchTaxFormula.setOrganization(branchTaxes.getOrganization());
            bnchTaxFormula.setSpecifics(specifics);
            bnchTaxFormula.setParticular(specifics.getParticularsId());
            bnchTaxFormula.setBranchSpecifics(branchSpecifics);
            bnchTaxFormula.setAddDeduct(addDeduct);
            bnchTaxFormula.setAppliedTo(appliedTo);
            bnchTaxFormula.setFormula(formula);
            bnchTaxFormula.setInvoiceValue(invoiceValue);
            bnchTaxFormula.setGstItemCategory(itemCategory);
            bnchTaxFormula.setGstItemCode(hsnCode);
            bnchTaxFormula.setGstTaxRate(rate);
            bnchTaxFormula.setVendor(vendor);
            bnchTaxFormula.setVendorType(vendor.getType());
            bnchTaxFormula.setHsnDesc(desc);
            if (applicableDate != null) {
                bnchTaxFormula.setApplicableFrom(applicableDate);
            }
            genericDao.saveOrUpdate(bnchTaxFormula, user, entityManager);
        }
    }

    @Override
    public ObjectNode getGstInTaxesCess4Branch(Long branchId, EntityManager entityManager, Users user) {
        ObjectNode result = Json.newObject();
        ArrayNode inTaxRateList = result.putArray("inTaxRateList");
        ArrayList inparamList = new ArrayList(2);
        inparamList.add(user.getOrganization().getId());
        inparamList.add(branchId);
        List<BranchTaxes> branchTaxesList = genericDao.queryWithParams(BRANCH_TAX_HQL, entityManager, inparamList);
        for (BranchTaxes branchTaxes : branchTaxesList) {
            ObjectNode row = Json.newObject();
            row.put("id", branchTaxes.getId());
            row.put("taxname", branchTaxes.getTaxName());
            row.put("rate", branchTaxes.getTaxRate());
            row.put("taxtype", branchTaxes.getTaxType());
            inTaxRateList.add(row);
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "******* End " + result);
        return result;
    }

    /*
     * @Override
     * public ObjectNode getRcmTaxes4BranchVendor(Long branchId, Long vendorid,
     * EntityManager entityManager, Users user) throws IDOSException{
     * ObjectNode result = Json.newObject();
     * ArrayNode inTaxRateList = result.putArray("inTaxRateList");
     * try {
     * ArrayList inparamList = new ArrayList(2);
     * inparamList.add(branchId);
     * inparamList.add(user.getOrganization().getId());
     * List<BranchTaxes> branchTaxesList =
     * genericDao.queryWithParams(BRANCH_TAX_HQL, entityManager, inparamList);
     * for(BranchTaxes branchTaxes: branchTaxesList){
     * ObjectNode row = Json.newObject();
     * row.put("id", branchTaxes.getId());
     * row.put("taxname", branchTaxes.getTaxName());
     * row.put("rate", branchTaxes.getTaxRate());
     * row.put("taxtype", branchTaxes.getTaxType());
     * inTaxRateList.add(row);
     * }
     * }catch (Exception ex) {
     * log.log(Level.SEVERE, "error", ex);
     * throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE,
     * IdosConstants.TECHNICAL_EXCEPTION, "Error on save/update RCM taxes.",
     * ex.getMessage());
     * }
     * return result;
     * }
     */
    @Override
    public ObjectNode getRcmTaxesForSpecific(JsonNode json, EntityManager entityManager, Users user)
            throws IDOSException {
        ObjectNode result = Json.newObject();
        ArrayNode reverseChargeData = result.putArray("reverseChargeData");
        int txnPurposeVal = json.findValue("txnPurposeVal").asInt();
        ArrayNode branchTdsDetail = result.putArray("branchTdsDetail");

        String txnVendCustId = json.findValue("txnVendCustId").asText();

        Long itemId = json.findValue("itemId") != null ? json.findValue("itemId").asLong() : 0L;

        Integer txnTypeOfSupply = json.findValue("txnTypeOfSupply") != null ? json.findValue("txnTypeOfSupply").asInt()
                : 0;

        Long branchId = json.findValue("txnBranchId").asLong();
        String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
        Date txnDate = IdosUtil.getFormatedDateWithTime(selectedTxnDate);

        try {
            txnDate = IdosConstants.MYSQLDTF.parse(IdosConstants.MYSQLDTF.format(txnDate));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            log.log(Level.SEVERE, "Date Parse Exception >>>>>>" + e);
        }

        if ((txnVendCustId != null && !txnVendCustId.equals(""))
                && txnPurposeVal == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                || txnPurposeVal == IdosConstants.BUY_ON_CREDIT_PAY_LATER
                || txnPurposeVal == IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER
                || txnPurposeVal == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) {

            List<BranchSpecificsTaxFormula> branchSpecificsTaxFormulaList = null;
            if (txnTypeOfSupply == 4 || txnTypeOfSupply == 5) {
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, ">>>>>>>>>>>>> QQQQQ >>>>" + SPECIFIC_BRANCH_RCM_HQL.toString());
                }

                Query q = entityManager.createQuery(SPECIFIC_BRANCH_RCM_HQL);
                q.setParameter(1, user.getOrganization().getId());
                q.setParameter(2, branchId);
                q.setParameter(3, itemId);
                q.setParameter(4, user.getOrganization().getId());
                q.setParameter(5, itemId);
                q.setParameter(6, Long.parseLong(txnVendCustId));
                q.setParameter(7, 32);
                if (txnTypeOfSupply == 4) {
                    q.setParameter(8, "GOODS");
                }
                if (txnTypeOfSupply == 5) {
                    q.setParameter(8, "SERVICES");
                }
                q.setParameter(9, txnDate);

                branchSpecificsTaxFormulaList = q.getResultList();
                // branchSpecificsTaxFormulaList =
                // genericDao.queryWithParamsName(SPECIFIC_BRANCH_RCM_HQL, entityManager,
                // inparamList);

            } else {
                Query q = entityManager.createQuery(SPECIFIC_BRANCH_RCM_ALL_HQL);
                q.setParameter(1, user.getOrganization().getId());
                q.setParameter(2, branchId);
                q.setParameter(3, itemId);
                q.setParameter(4, user.getOrganization().getId());
                q.setParameter(5, itemId);
                q.setParameter(6, Long.parseLong(txnVendCustId));
                q.setParameter(7, 32);
                q.setParameter(8, txnDate);
                branchSpecificsTaxFormulaList = q.getResultList();
                // branchSpecificsTaxFormulaList =
                // genericDao.queryWithParamsName(SPECIFIC_BRANCH_RCM_ALL_HQL, entityManager,
                // inparamList);
            }

            if (branchSpecificsTaxFormulaList != null) {
                Double taxRate = 0d;
                Double cessRate = 0d;
                for (BranchSpecificsTaxFormula branchSpecificsTaxFormula : branchSpecificsTaxFormulaList) {
                    ObjectNode row = Json.newObject();
                    BranchTaxes branchTaxes = branchSpecificsTaxFormula.getBranchTaxes();
                    VendorSpecific vendorSpecific = VendorSpecific.findByVendorAndSpecific(entityManager,
                            user.getOrganization().getId(), user.getBranch().getId(), Long.parseLong(txnVendCustId),
                            branchSpecificsTaxFormula.getSpecifics().getId());
                    if (vendorSpecific != null && vendorSpecific.getRcmTaxRateVend() != null) {
                        taxRate = branchTaxes.getTaxRate();
                        String desc = branchSpecificsTaxFormula.getSpecifics().getName() + "-(" + taxRate + "%)" + "-"
                                + branchSpecificsTaxFormula.getSpecifics().getGstDesc();
                        row.put("taxRateName", branchTaxes.getTaxName());
                        row.put("taxNameId", branchTaxes.getId());
                        row.put("id", branchSpecificsTaxFormula.getId());
                        row.put("description", desc);
                        row.put("taxRate", taxRate);

                    }

                    if (vendorSpecific != null && vendorSpecific.getCessTaxRateVend() != null) {

                        Query query = entityManager.createQuery(CESS_RATE_HQL);
                        query.setParameter(1, user.getOrganization().getId());
                        query.setParameter(2, branchSpecificsTaxFormula.getBranch().getId());
                        query.setParameter(3, branchSpecificsTaxFormula.getSpecifics().getId());
                        query.setParameter(4, 33);
                        query.setParameter(5, vendorSpecific.getCessTaxRateVend());
                        query.setParameter(6, Long.parseLong(txnVendCustId));
                        BranchSpecificsTaxFormula bnchTaxFormula = (BranchSpecificsTaxFormula) query.getSingleResult();

                        if (bnchTaxFormula != null) {
                            row.put("cessRateName", bnchTaxFormula.getBranchTaxes().getTaxName());
                            row.put("cessNameId", bnchTaxFormula.getBranchTaxes().getId());
                            if (branchTaxes.getTaxRate() != null) {
                                cessRate = bnchTaxFormula.getBranchTaxes().getTaxRate();
                                row.put("cessRateName", bnchTaxFormula.getBranchTaxes().getTaxName());
                                row.put("cessNameId", bnchTaxFormula.getBranchTaxes().getId());
                                row.put("cessRate", cessRate);
                            } else {
                                row.put("cessRate", "");
                            }
                        }
                    }
                    if (row.size() > 0) {
                        reverseChargeData.add(row);
                    }

                }
            }
            // Start TDS
            Specifics specf = Specifics.findById(itemId);
            Vendor vendor = Vendor.findById(Long.valueOf(txnVendCustId));
            if (specf != null && vendor != null) {
                // Passing Od because we required only TDS rate and Limts
                VENDOR_TDS_DAO.calculateTds(result, branchTdsDetail, user, specf, vendor, 0d, 0d,
                        Long.valueOf(txnPurposeVal), IdosUtil.getFormatedDateWithTime(selectedTxnDate), entityManager,
                        false);
            }
        } else {
            log.log(Level.SEVERE, "Specific details not found for id " + itemId);
        }

        return result;
    }

    @Override
    public void saveTaxableItemsForCompositionScheme(JsonNode json, EntityManager entityManager, Users user) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "******* Save Taxable Items START");
        String coaSelectedIds = json.findValue("coaSelectedIds") != null
                || !"".equals(json.findValue("coaSelectedIds").asText())
                        ? json.findValue("coaSelectedIds").asText().trim()
                        : null;
        String coaDeselectedIds = json.findValue("coaDeselectedIds") != null
                || !"".equals(json.findValue("coaDeselectedIds").asText())
                        ? json.findValue("coaDeselectedIds").asText().trim()
                        : null;

        if (coaSelectedIds != null) {
            String[] coaSelectedIdsArr = StringUtils.split(coaSelectedIds, ",");
            for (int i = 0; i < coaSelectedIdsArr.length; i++) {
                Specifics spec = Specifics.findById(Long.valueOf(coaSelectedIdsArr[i]));
                if (spec != null) {
                    spec.setIsCompositionScheme(1);
                    genericDao.saveOrUpdate(spec, user, entityManager);
                }
            }
        }

        if (coaDeselectedIds != null) {
            String[] coaDeselectedIdsArr = StringUtils.split(coaDeselectedIds, ",");
            for (int i = 0; i < coaDeselectedIdsArr.length; i++) {
                Specifics spec = Specifics.findById(Long.valueOf(coaDeselectedIdsArr[i]));
                if (spec != null) {
                    spec.setIsCompositionScheme(0);
                    genericDao.saveOrUpdate(spec, user, entityManager);
                }
            }
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "******** Save Taxable Items END");
    }

    @Override
    public ObjectNode getRcmTaxesForSpecificTypeOfSupply(JsonNode json, EntityManager entityManager, Users user)
            throws IDOSException {
        ObjectNode result = Json.newObject();
        ArrayNode sgstData = result.putArray("sgstData");
        ArrayNode cgstData = result.putArray("cgstData");
        Long taxIgstId = json.findValue("taxIgstId").asLong();
        result.put("status", false);
        Map<String, Object> criterias = new HashMap<String, Object>();
        BranchTaxes branchIgstTax = BranchTaxes.findById(taxIgstId);
        if (branchIgstTax != null) {
            criterias.clear();
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("branch.id", branchIgstTax.getBranch().getId());
            criterias.put("taxType", 30);
            criterias.put("taxRate", (branchIgstTax.getTaxRate() / 2));
            criterias.put("presentStatus", 1);
            BranchTaxes branchSgstTax = genericDao.getByCriteria(BranchTaxes.class, criterias, entityManager);
            if (branchSgstTax != null) {
                ObjectNode row = Json.newObject();
                row.put("taxRateNameSgst", branchSgstTax.getTaxName());
                row.put("taxNameIdSgst", branchSgstTax.getId());
                row.put("taxRateSgst", branchSgstTax.getTaxRate());
                sgstData.add(row);
                result.put("status", true);
            }

            criterias.clear();
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("branch.id", branchIgstTax.getBranch().getId());
            criterias.put("taxType", 31);
            criterias.put("taxRate", (branchIgstTax.getTaxRate() / 2));
            criterias.put("presentStatus", 1);
            BranchTaxes branchCgstTax = genericDao.getByCriteria(BranchTaxes.class, criterias, entityManager);
            if (branchCgstTax != null) {
                ObjectNode row = Json.newObject();
                row.put("taxRateNameCgst", branchCgstTax.getTaxName());
                row.put("taxNameIdCgst", branchCgstTax.getId());
                row.put("taxRateCgst", branchCgstTax.getTaxRate());
                cgstData.add(row);
                result.put("status", true);
            }
        }
        return result;
    }

    @Override
    public void saveInputTaxCOA(Specifics specific, Branch branch, EntityManager entityManager, Users user)
            throws IDOSException {
        // TODO Auto-generated method stub
        try {
            String gstTaxRateSelected = specific.getGstTaxRateSelected();
            if (gstTaxRateSelected != null) {
                String[] rates = gstTaxRateSelected.split(",");
                for (String gstRate : rates) {
                    Double gstTaxRate = -1d;
                    if (gstRate.equals(IdosConstants.OUTPUT_TAX_OTHER)) {
                        if (specific.getGstTaxRate() != null) {
                            gstTaxRate = specific.getGstTaxRate();
                        }
                    } else {
                        gstTaxRate = Double.parseDouble(gstRate);
                    }
                    if (gstTaxRate != -1d && gstTaxRate >= 0.0) {
                        Double rate = gstTaxRate / 2;
                        saveInputTax(branch, rate, IdosConstants.INPUT_SGST, user, "SGST", entityManager);
                        saveInputTax(branch, rate, IdosConstants.INPUT_CGST, user, "CGST", entityManager);
                        saveInputTax(branch, gstTaxRate, IdosConstants.INPUT_IGST, user, "IGST", entityManager);
                    }
                }
            }

            String cessTaxRateSelected = specific.getCessTaxRateSelected();
            if (cessTaxRateSelected != null) {
                String[] cessRates = cessTaxRateSelected.split(",");
                for (String cessRate : cessRates) {
                    Double cessTaxRate = -1d;
                    if (cessRate.equals(IdosConstants.OUTPUT_TAX_OTHER)) {
                        if (specific.getCessTaxRate() != null) {
                            cessTaxRate = specific.getCessTaxRate();
                        }
                    } else {
                        cessTaxRate = Double.parseDouble(cessRate);
                    }
                    if (cessTaxRate != -1d && cessTaxRate >= 0.0) {
                        saveInputTax(branch, cessTaxRate, IdosConstants.INPUT_CESS, user, "CESS", entityManager);
                    }
                }
            }

        } catch (Exception ex) {
            log.log(Level.SEVERE, "error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on save/update input taxes.", ex.getMessage());
        }
    }

    @Override
    public void saveRcmTaxBranchVendor(Branch branch, Vendor vendor, Specifics specific, Double gstTaxRate,
            Double cessRate, Date applicableDate, EntityManager entityManager, Users user) throws IDOSException {
        try {
            String typeOfSupply = "";
            String description = "";
            String hsnSacCode = "";
            if (specific != null) {
                if (specific.getGstTypeOfSupply() != null) {
                    typeOfSupply = specific.getGstTypeOfSupply();
                }

                if (specific.getGstItemCode() != null) {
                    hsnSacCode = specific.getGstItemCode();
                }

                if (specific.getGstDesc() != null) {
                    description = specific.getGstDesc();
                }

            }

            if (gstTaxRate != null) {
                Double rate = gstTaxRate / 2;
                saveRcmTax(branch, rate, IdosConstants.RCM_SGST_IN, user, "SGST", entityManager, specific, typeOfSupply,
                        hsnSacCode, vendor, description, applicableDate);
                saveRcmTax(branch, rate, IdosConstants.RCM_CGST_IN, user, "CGST", entityManager, specific, typeOfSupply,
                        hsnSacCode, vendor, description, applicableDate);
                saveRcmTax(branch, gstTaxRate, IdosConstants.RCM_IGST_IN, user, "IGST", entityManager, specific,
                        typeOfSupply, hsnSacCode, vendor, description, applicableDate);
                saveRcmTax(branch, rate, IdosConstants.RCM_SGST_OUTPUT, user, "SGST", entityManager, specific,
                        typeOfSupply, hsnSacCode, vendor, description, applicableDate);
                saveRcmTax(branch, rate, IdosConstants.RCM_CGST_OUTPUT, user, "CGST", entityManager, specific,
                        typeOfSupply, hsnSacCode, vendor, description, applicableDate);
                saveRcmTax(branch, gstTaxRate, IdosConstants.RCM_IGST_OUTPUT, user, "IGST", entityManager, specific,
                        typeOfSupply, hsnSacCode, vendor, description, applicableDate);

            }
            if (cessRate != null) {
                saveRcmTax(branch, Double.valueOf(cessRate), IdosConstants.RCM_CESS_IN, user, "CESS", entityManager,
                        specific, typeOfSupply, hsnSacCode, vendor, description, applicableDate);
                saveRcmTax(branch, Double.valueOf(cessRate), IdosConstants.RCM_CESS_OUTPUT, user, "CESS", entityManager,
                        specific, typeOfSupply, hsnSacCode, vendor, description, applicableDate);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
            log.log(Level.SEVERE, "error", ex);
            if (ex instanceof IDOSException) {
                throw ex;
            } else {
                throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                        "Error on save/update input taxes.", ex.getMessage());
            }
        }
    }

}
