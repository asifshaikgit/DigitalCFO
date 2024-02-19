package controllers;

import actor.SpecificsTransactionActor;
import com.idos.util.*;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import play.db.jpa.JPAApi;

import javax.transaction.Transactional;
import akka.stream.javadsl.*;
import akka.actor.*;

import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.WebSocket;
import akka.NotUsed;
import views.html.errorPage;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.io.File;
import java.text.ParseException;
import java.util.*;
import play.Application;
import javax.inject.Inject;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

public class SpecificsController extends StaticController {
    private final static String GST_SERVICE_CODE_DESC_JQL = "select obj from GSTServicesCodes obj where obj.serviceCode = ?1 and obj.presentStatus=1";
    private final static String GST_GOODS_CODE_DESC_JQL = "select obj from GSTGoodsCodes obj where obj.goodsCode = ?1 and obj.presentStatus=1";
    private final static String BARCODE_JQL = "select obj.barcode from Specifics obj where obj.barcode = ?1 and obj.accountCodeHirarchy like ?2 and obj.organization.id = ?3 and obj.presentStatus=1";
    private final static String SPECIFICS_CHILD_HQL = "select obj from Specifics obj WHERE obj.organization.id = ?1 and obj.parentSpecifics.id = ?2 and obj.presentStatus=1";
    private final static String USER_SPECIFICS_HQL = "update UserRightSpecifics obj set obj.presentStatus=0 where obj.specifics.id = ?1";
    private final static String FETCH_BRANCH_TAXES = "select obj.branchTaxes.id, obj.formula, obj.appliedTo, obj.addDeduct, obj.invoiceValue, obj.gstTaxRate from BranchSpecificsTaxFormula obj WHERE obj.branch.id=?1 and obj.organization.id=?2 and obj.specifics.id=?3 and obj.presentStatus=1";
    private final static String FETCH_BRANCHS = "select obj.branch from BranchSpecifics obj WHERE obj.specifics.id=?1 and obj.organization.id=?2 and obj.presentStatus=1";
    // public static LRUCacheUsingLinkedHashMap<Long, LRUCacheUsingLinkedHashMap>
    // cacheOrgCoaMap = new LRUCacheUsingLinkedHashMap<Long,
    // LRUCacheUsingLinkedHashMap>(10);
    // public static HashMap<Long, String> cacheCoa = new HashMap<Long,
    // newspecificsobj >();
    // public static HashMap<Long, String> cacheCoa = new HashMap<Long, String >();
    private static Application application;
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    // private Request request;
    // private Http.Session session = request.session();
    @Inject
    public SpecificsController(Application application) {
        super(application);
        this.application = application;
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @Transactional
    public Result checkForDuplicacy(String name, String headType, Request request) {
        Users user = getUserInfo(request);
        if (user == null) {
            return unauthorized();
        }
        ObjectNode result = Json.newObject();
        try {
            List<Specifics> entityList = null;

            if (headType != null && (headType.trim().equals("1")) || headType.trim().equals("2")) {
                entityList = Specifics.findByNameAndHeadType(entityManager, user.getOrganization(), name,
                        headType.trim());
            } else {
                entityList = Specifics.findByName(entityManager, user.getOrganization(), name);
            }

            if (entityList != null && entityList.size() > 0) {
                result.put("ispresent", "true");
                result.put("dataid", entityList.get(0).getId());
            } else {
                result.put("ispresent", "false");
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result);
    }

    @Transactional
    public Result saveSpecifics(Request request) {
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        Users user = null;
        EntityTransaction entityTransaction = entityManager.getTransaction();
        try {
            Long parentActCode = null;
            Long maxActCode = null;
            String hirarchy = null;
            JsonNode json = request.body().asJson();
            log.log(Level.FINE, ">>>> Start " + json);
            Specifics newSpec = null;
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
            if (ipAddress == null) {
                ipAddress = request.remoteAddress();
            }
            entityTransaction.begin();
            ArrayNode an = result.putArray("newspecificsData");
            String itemHidpk = json.findValue("itemHiddenPrimaryKey").asText();
            String topMostParentCode = json.findValue("topMostParentCode").asText();
            String specName = json.findValue("specificsName").asText();
            String newSpecParentId = json.findValue("specificsParentId").asText();
            String newSpecParentText = json.findValue("specificsParentText").asText();
            newSpecParentText = IdosUtil.escapeHtml(newSpecParentText);
            String isEmployeeClaimItem = json.findValue("isEmployeeClaimItem") != null
                    ? json.findValue("isEmployeeClaimItem").asText()
                    : null;
            String expUnitMeasure = json.findValue("expUnitMeasure") != null ? json.findValue("expUnitMeasure").asText()
                    : null;
            String expNoOfOpeningBalUnits = json.findValue("expNoOfOpeningBalUnits") != null
                    ? json.findValue("expNoOfOpeningBalUnits").asText()
                    : null;
            String expRateOpeningBalUnits = json.findValue("expRateOpeningBalUnits") != null
                    ? json.findValue("expRateOpeningBalUnits").asText()
                    : null;
            String expOpeningBal = json.findValue("expOpeningBal") != null ? json.findValue("expOpeningBal").asText()
                    : null;
            String incometoexpensemapping = json.findValue("incometoexpensemapping") != null
                    ? json.findValue("incometoexpensemapping").asText()
                    : null;
            String noOfExpUnit = json.findValue("noOfExpUnit") != null ? json.findValue("noOfExpUnit").asText() : null;
            String noOfIncUnit = json.findValue("noOfIncUnit") != null ? json.findValue("noOfIncUnit").asText() : null;
            String incUnitMeasure = json.findValue("incUnitMeasure") != null ? json.findValue("incUnitMeasure").asText()
                    : null;
            String tradingInvCalcMethod = json.findValue("tradingInvCalcMethod") != null
                    ? json.findValue("tradingInvCalcMethod").asText()
                    : null;
            String invoiceItemDescription1 = json.findValue("invoiceItemDescription1") != null
                    ? json.findValue("invoiceItemDescription1").asText().trim()
                    : null;
            String invoiceItemDescription2 = json.findValue("invoiceItemDescription2") != null
                    ? json.findValue("invoiceItemDescription2").asText().trim()
                    : null;
            String itemBarcodeNo = json.findValue("itemBarcodeNo") != null
                    ? json.findValue("itemBarcodeNo").asText().trim()
                    : null;
            Integer itemInvoiceDesc1Check = json.findValue("itemInvoiceDesc1Check") != null
                    ? json.findValue("itemInvoiceDesc1Check").asInt()
                    : null;
            Integer itemInvoiceDesc2Check = json.findValue("itemInvoiceDesc2Check") != null
                    ? json.findValue("itemInvoiceDesc2Check").asInt()
                    : null;
            Integer isTranEditableCheck = json.findValue("isTranEditableCheck") != null
                    ? json.findValue("isTranEditableCheck").asInt()
                    : null;
            Integer itemPriceInclusive = json.findValue("itemPriceInclusive") != null
                    ? json.findValue("itemPriceInclusive").asInt()
                    : null;
            String GSTDesc = json.findValue("GSTDesc") != null ? json.findValue("GSTDesc").asText() : null;
            String GSTItemSelected = json.findValue("GSTItemSelected") != null
                    ? json.findValue("GSTItemSelected").asText()
                    : null;
            String GSTCode = json.findValue("GSTCode") != null ? json.findValue("GSTCode").asText() : null;
            String GSTTaxRate = json.findValue("GSTTaxRate") != null ? json.findValue("GSTTaxRate").asText() : null;
            Double cessTaxRate = (json.findValue("cessTaxRate") != null
                    && !"".equals(json.findValue("cessTaxRate").asText())) ? json.findValue("cessTaxRate").asDouble()
                            : null;
            String GSTItemCategory = json.findValue("GSTItemCategory") != null
                    ? json.findValue("GSTItemCategory").asText()
                    : null;
            String GSTtypeOfSupply = json.findValue("GSTtypeOfSupply") != null
                    ? json.findValue("GSTtypeOfSupply").asText()
                    : null;
            //String GSTApplicable = (json.findValue("GSTApplicable") != null
            //        && !"".equals(json.findValue("GSTApplicable").asText()))
            //                ? json.findValue("GSTApplicable").asText()
            //                : null;
            Integer isInputTaxCreditItem = json.findValue("isInputTaxCreditItem") != null
                    || !"".equals(json.findValue("isInputTaxCreditItem").asText())
                            ? json.findValue("isInputTaxCreditItem").asInt()
                            : null;
            String gstTaxRateSelected = (json.findValue("gstTaxRateSelected") != null
                    && !"".equals(json.findValue("gstTaxRateSelected").asText()))
                            ? json.findValue("gstTaxRateSelected").asText()
                            : null;
            String cessTaxRateSelected = (json.findValue("cessTaxRateSelected") != null
                    && !"".equals(json.findValue("cessTaxRateSelected").asText()))
                            ? json.findValue("cessTaxRateSelected").asText()
                            : null;

            Map<String, Object> criterias = new HashMap<String, Object>(4);
            Specifics newSpecParentspec = null;
            Particulars newSpecPart = null;
            Particulars newSpecParentpart = null;
            criterias.put("id", Long.parseLong(newSpecParentId));
            criterias.put("name", newSpecParentText);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            newSpecParentpart = genericDAO.getByCriteria(Particulars.class, criterias, entityManager);
            if (newSpecParentpart != null) {
                parentActCode = newSpecParentpart.getAccountCode();
                newSpecPart = newSpecParentpart;
                StringBuilder sbquery = new StringBuilder(
                        "select MAX(obj.accountCode) from Specifics obj where obj.particularsId.id = ");
                sbquery.append(newSpecParentpart.getId())
                        .append(" and obj.parentSpecifics IS NULL and organization.id = ")
                        .append(user.getOrganization().getId()).append(" and obj.presentStatus=1");
                List maxSpecfObj = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
                if (maxSpecfObj.get(0) != null) {
                    maxActCode = (Long) maxSpecfObj.get(0);
                } else {
                    maxActCode = null;
                }
                hirarchy = newSpecParentpart.getAccountCodeHirarchy() + newSpecParentpart.getAccountCode() + "/";
            }
            if (newSpecParentpart == null) {
                criterias.clear();
                criterias.put("id", Long.parseLong(newSpecParentId));
                criterias.put("name", newSpecParentText);
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("presentStatus", 1);
                newSpecParentspec = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
                parentActCode = newSpecParentspec.getAccountCode();
                List maxSpecfObj = Specifics.findMaxAccountCode4Specific(entityManager, user.getOrganization().getId(),
                        newSpecParentspec.getId());
                if (maxSpecfObj.get(0) != null) {
                    maxActCode = (Long) maxSpecfObj.get(0);
                } else {
                    maxActCode = null;
                }
                hirarchy = newSpecParentspec.getAccountCodeHirarchy() + newSpecParentspec.getAccountCode() + "/";
                newSpecPart = newSpecParentspec.getParticularsId();
            }
            String btnName = json.findValue("btnName").asText();
            String itemBranchValues = json.findPath("itemBchValues") != null ? json.findPath("itemBchValues").asText()
                    : null;
            String walkinCustDiscount = json.findPath("walkinCustDiscount") != null
                    ? json.findPath("walkinCustDiscount").asText()
                    : null;
            String itemBranches[] = null;
            Boolean isNewSpecific = false;
            if (itemBranchValues != null) {
                itemBranches = itemBranchValues.split(",");
            }
            if (itemHidpk != null && !"".equals(itemHidpk)) {
                newSpec = Specifics.findById(Long.parseLong(itemHidpk));
            } else {
                newSpec = new Specifics();
                isNewSpecific = true;
            }
            Long actCode = AccountCodeUtil.generateAccountCode(parentActCode, maxActCode);
            criterias.clear();
            criterias.put("accountCode", actCode);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            Specifics foundSpecf = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
            if (foundSpecf != null) {
                actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
            }
            String aCode = String.valueOf(actCode);
            int length = aCode.length();
            int pos = aCode.indexOf("9");
            int itrate = 0;
            String one = "1";
            if (pos != -1) {
                itrate = length - pos - 3;
                for (int m = 0; m < itrate; m++) {
                    one += "0";
                }
                if (parentActCode != null) {
                    actCode = parentActCode + Long.valueOf(one);
                }
                if (maxActCode != null) {
                    actCode = maxActCode + Long.valueOf(one);
                }
            }
            criterias.clear();
            criterias.put("accountCode", actCode);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            Specifics againfoundSpecf = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
            if (againfoundSpecf != null) {
                actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
            }
            criterias.clear();
            criterias.put("accountCode", actCode);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            Specifics againfoundSpecf1 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
            if (againfoundSpecf1 != null) {
                actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
            }
            criterias.clear();
            criterias.put("accountCode", actCode);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            Specifics againfoundSpecf2 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
            if (againfoundSpecf2 != null) {
                actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
            }
            criterias.clear();
            criterias.put("accountCode", actCode);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            Specifics againfoundSpecf3 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
            if (againfoundSpecf3 != null) {
                actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
            }
            criterias.clear();
            criterias.put("accountCode", actCode);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            Specifics againfoundSpecf4 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
            if (againfoundSpecf4 != null) {
                actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
            }
            criterias.clear();
            criterias.put("accountCode", actCode);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            Specifics againfoundSpecf5 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
            if (againfoundSpecf5 != null) {
                actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
            }
            criterias.clear();
            criterias.put("accountCode", actCode);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            Specifics againfoundSpecf6 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
            if (againfoundSpecf6 != null) {
                actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
            }
            criterias.clear();
            criterias.put("accountCode", actCode);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            Specifics againfoundSpecf7 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
            if (againfoundSpecf7 != null) {
                actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
            }
            criterias.clear();
            criterias.put("accountCode", actCode);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            Specifics againfoundSpecf8 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
            if (againfoundSpecf8 != null) {
                actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
            }
            criterias.clear();
            criterias.put("accountCode", actCode);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            Specifics againfoundSpecf9 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
            if (againfoundSpecf9 != null) {
                actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
            }
            criterias.clear();
            criterias.put("accountCode", actCode);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            Specifics againfoundSpecf10 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
            if (againfoundSpecf10 != null) {
                actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
            }
            if (newSpec.getAccountCode() == null) {
                newSpec.setAccountCode(actCode);
            }
            newSpec.setAccountCodeHirarchy(hirarchy);
            newSpec.setName(specName);
            newSpec.setPresentStatus(1);
            if (newSpecParentId != null) {
                Specifics parent = Specifics.findById(Long.parseLong(newSpecParentId));
                if (parent != null && parent.getIdentificationForDataValid() != null
                        && !"".equals(parent.getIdentificationForDataValid())) {
                    int identificationForDataValid = Integer.parseInt(parent.getIdentificationForDataValid());
                    if (identificationForDataValid >= 24 && identificationForDataValid <= 27) {
                        if (isInputTaxCreditItem != null) {
                            newSpec.setIsEligibleInputTaxCredit(isInputTaxCreditItem);
                        } else {
                            newSpec.setIsEligibleInputTaxCredit(0);
                        }
                    }
                }
            }
            if (isEmployeeClaimItem != null) {
                newSpec.setEmployeeClaimItem(Integer.parseInt(isEmployeeClaimItem));
            }
            if (expUnitMeasure != null) {
                newSpec.setExpenseUnitsMeasure(expUnitMeasure);
            }
            if (expNoOfOpeningBalUnits != null && expNoOfOpeningBalUnits != "") {
                newSpec.setTotalInvOpeningBalUnits(Double.parseDouble(expNoOfOpeningBalUnits));
            } else {
                newSpec.setTotalInvOpeningBalUnits(0.0);
            }
            if (expRateOpeningBalUnits != null && expRateOpeningBalUnits != "") {
                newSpec.setTotalInvOpeningBalRate(Double.parseDouble(expRateOpeningBalUnits));
            } else {
                newSpec.setTotalInvOpeningBalRate(0.0);
            }
            if (expOpeningBal != null && expOpeningBal != "") {
                newSpec.setTotalInvOpeningBalance(Double.parseDouble(expOpeningBal));
            } else {
                newSpec.setTotalInvOpeningBalance(0.0);
            }
            /*
             * if(itemPurchaseFromLoanAccount!= null){
             * newSpec.setIsLoanAccountItem(Integer.parseInt(itemPurchaseFromLoanAccount));
             * }
             */
            // StringBuilder sbquery = new StringBuilder("");
            // sbquery.append("select obj.barcode from Specifics obj where obj.barcode =
            // '"+itemBarcodeNo+"' and obj.accountCodeHirarchy like '/10%' ");
            // List checkBarcodeExistsInc =
            // genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
            String accHier = "'/10%'";
            ArrayList inparam = new ArrayList(3);
            inparam.add(itemBarcodeNo);
            inparam.add(accHier);
            inparam.add(user.getOrganization().getId());
            List<Specifics> checkBarcodeExistsInc = genericDAO.queryWithParams(BARCODE_JQL, entityManager, inparam);
            if (topMostParentCode.equals("1000000000000000000")) {
                newSpec.setInvoiceItemDescription1(invoiceItemDescription1);
                newSpec.setInvoiceItemDescription2(invoiceItemDescription2);
                if (checkBarcodeExistsInc.size() == 0) {
                    newSpec.setBarcode(itemBarcodeNo);
                }
                newSpec.setIsInvoiceDescription1(itemInvoiceDesc1Check);
                newSpec.setIsInvoiceDescription2(itemInvoiceDesc2Check);
                if (ConfigParams.getInstance().getCompanyOwner().equals("PWC")) {
                    newSpec.setIsTransactionEditable(1);
                } else {
                    newSpec.setIsTransactionEditable(isTranEditableCheck);
                }
                newSpec.setIsPriceInclusive(itemPriceInclusive);
            }
            newSpec.setGstDesc(GSTDesc);
            // StringBuilder sbquery1 = new StringBuilder("");
            // sbquery1.append("select obj.barcode from Specifics obj where obj.barcode =
            // '"+itemBarcodeNo+"' and obj.accountCodeHirarchy like '/20%' ");
            // List checkBarcodeExistsExp =
            // genericDAO.executeSimpleQuery(sbquery1.toString(), entityManager);
            accHier = "'/20%'";
            ArrayList inparam1 = new ArrayList(3);
            inparam1.add(itemBarcodeNo);
            inparam1.add(accHier);
            inparam1.add(user.getOrganization().getId());
            List<Specifics> checkBarcodeExistsExp = genericDAO.queryWithParams(BARCODE_JQL, entityManager, inparam1);
            if (topMostParentCode.equals("1000000000000000000") || topMostParentCode.equals("2000000000000000000")) { // GST
                                                                                                                      // specific
                                                                                                                      // info
                newSpec.setGstItemCode(GSTCode);
                if (GSTtypeOfSupply != null && !GSTtypeOfSupply.equals("")
                        && !GSTtypeOfSupply.equalsIgnoreCase("Please Select..")) {
                    int GSTtypeOfGoodsOrSerInt = Integer.parseInt(GSTtypeOfSupply);
                    if (GSTtypeOfGoodsOrSerInt == IdosConstants.GST_GOODS) {
                        newSpec.setGstTypeOfSupply(IdosConstants.GST_GOODS_TEXT);
                    } else if (GSTtypeOfGoodsOrSerInt == IdosConstants.GST_SERVICES) {
                        newSpec.setGstTypeOfSupply(IdosConstants.GST_SERVICES_TEXT);
                    }
                } else {
                    newSpec.setGstTypeOfSupply(null);
                }
                /*if (topMostParentCode.equals("1000000000000000000")) {
                    newSpec.setGSTApplicable(GSTApplicable);
                }*/
                if (GSTItemCategory != null && !GSTItemCategory.equals("")
                        && !GSTItemCategory.equalsIgnoreCase("Please Select..")) {
                    newSpec.setGstItemCategory(GSTItemCategory);
                } else {
                    newSpec.setGstItemCategory(null);
                }
                if (GSTTaxRate != null && !"".equals(GSTTaxRate)) {
                    newSpec.setGstTaxRate(Double.parseDouble(GSTTaxRate));
                }
                if (cessTaxRate != null) {
                    newSpec.setCessTaxRate(cessTaxRate);
                } else {
                    newSpec.setCessTaxRate(null);
                }

                if (topMostParentCode.equals("2000000000000000000")) {
                    if (gstTaxRateSelected != null && !"".equals(gstTaxRateSelected)) {
                        newSpec.setGstTaxRateSelected(gstTaxRateSelected);
                    }
                    if (cessTaxRateSelected != null) {
                        newSpec.setCessTaxRateSelected(cessTaxRateSelected);
                    }
                }

                if (topMostParentCode.equals("2000000000000000000")) {
                    if (checkBarcodeExistsExp.size() == 0) {
                        newSpec.setBarcode(itemBarcodeNo);
                    }
                    if (isInputTaxCreditItem != null) {
                        newSpec.setIsEligibleInputTaxCredit(isInputTaxCreditItem);
                    } else {
                        newSpec.setIsEligibleInputTaxCredit(0);
                    }

                    Integer isTdsVendSpecific = json.findValue("isTdsVendSpecific") != null
                            || !"".equals(json.findValue("isTdsVendSpecific").asText())
                                    ? json.findValue("isTdsVendSpecific").asInt()
                                    : null;
                    if (isTdsVendSpecific != null) {
                        newSpec.setIsTdsVendorSpecific(isTdsVendSpecific);
                    } else {
                        newSpec.setIsTdsVendorSpecific(0);
                    }

                    Integer isCompositionItem = json.findValue("isCompositionItem") != null
                            || !"".equals(json.findValue("isCompositionItem").asText())
                                    ? json.findValue("isCompositionItem").asInt()
                                    : null;
                    if (isCompositionItem != null) {
                        newSpec.setIsCompositionScheme(isCompositionItem);
                    } else {
                        newSpec.setIsCompositionScheme(0);
                    }
                }
            }
            if (newSpecParentpart != null) {
                newSpec.setParticularsId(newSpecParentpart);
                newSpec.setParentSpecifics(null);
            }
            if (newSpecParentpart == null) {
                newSpec.setParticularsId(newSpecParentspec.getParticularsId());
                newSpec.setParentSpecifics(newSpecParentspec);
                if (newSpecParentspec.getIdentificationForDataValid() != null
                        && newSpecParentspec.getIdentificationForDataValid().equals("52")) { // if parent is combination
                                                                                             // sales
                    newSpec.setIsCombinationSales(1); // it is combination sale item, so show combItemslist for sell on
                                                      // cash/credit when this item is selected in dropdown
                }
            }
            String incomeSpecificsPerUnitPrice = json.findValue("incomeSpecificPerUnitPrice") != null
                    ? json.findValue("incomeSpecificPerUnitPrice").asText()
                    : null;
            if (incomeSpecificsPerUnitPrice != null && !incomeSpecificsPerUnitPrice.equals("")) {
                newSpec.setIncomeSpecfPerUnitPrice(Double.parseDouble(incomeSpecificsPerUnitPrice));
            } else {
                newSpec.setIncomeSpecfPerUnitPrice(null);
            }

            String incomeexpense = json.findPath("incomeexpense") != null ? json.findPath("incomeexpense").asText()
                    : null;
            if (incomeexpense != null) {
                if (!incomeexpense.equals("")) {
                    newSpec.setIncomeOrExpenseType(Integer.parseInt(incomeexpense));
                }
            }
            String expenseSpecfWithholdingApplicable = json.findValue("expenseSpecfWithholdingApplicable") != null
                    ? json.findValue("expenseSpecfWithholdingApplicable").asText()
                    : null;
            if (expenseSpecfWithholdingApplicable != null) {
                if (expenseSpecfWithholdingApplicable != null && !expenseSpecfWithholdingApplicable.equals("")) {
                    newSpec.setIsWithholdingApplicable(Integer.parseInt(expenseSpecfWithholdingApplicable));
                }
            }
            String expenseSpecfWithholdingType = json.findValue("expenseSpecfWithholdingType") != null
                    ? json.findValue("expenseSpecfWithholdingType").asText()
                    : null;
            if (expenseSpecfWithholdingType != null) {
                if (expenseSpecfWithholdingType != null && !expenseSpecfWithholdingType.equals("")) {
                    newSpec.setWithholdingType(Integer.parseInt(expenseSpecfWithholdingType));
                }
            }
            String expenseSpecfCaptureInputtaxes = json.findValue("expenseSpecfCaptureInputTaxes") != null
                    ? json.findValue("expenseSpecfCaptureInputTaxes").asText()
                    : null;
            if (expenseSpecfCaptureInputtaxes != null) {
                if (!expenseSpecfCaptureInputtaxes.equals("")) {
                    newSpec.setIsCaptureInputTaxes(Integer.parseInt(expenseSpecfCaptureInputtaxes));
                }
            }
            String expenseSpecfWithholdingRate = json.findValue("expenseSpecfWithholdingRate") != null
                    ? json.findValue("expenseSpecfWithholdingRate").asText()
                    : null;
            if (expenseSpecfWithholdingRate != null && !expenseSpecfWithholdingRate.equals("")) {
                newSpec.setWithHoldingRate(Double.parseDouble(expenseSpecfWithholdingRate));
            } else {
                newSpec.setWithHoldingRate(null);
            }
            String expenseSpecfWithholdingLimit = json.findValue("expenseSpecfWithholdingLimit") != null
                    ? json.findValue("expenseSpecfWithholdingLimit").asText()
                    : null;
            if (expenseSpecfWithholdingLimit != null && !expenseSpecfWithholdingLimit.equals("")) {
                newSpec.setWithHoldingLimit(Double.parseDouble(expenseSpecfWithholdingLimit));
            } else {
                newSpec.setWithHoldingLimit(null);
            }
            String expenseSpecfWithholdingMonetoryLimit = json.findValue("expenseSpecfWithholdingMonetoryLimit") != null
                    ? json.findValue("expenseSpecfWithholdingMonetoryLimit").asText()
                    : null;
            if (expenseSpecfWithholdingMonetoryLimit != null && !expenseSpecfWithholdingMonetoryLimit.equals("")) {
                newSpec.setWithholdingMonetoryLimit(Double.parseDouble(expenseSpecfWithholdingMonetoryLimit));
            } else {
                newSpec.setWithholdingMonetoryLimit(null);
            }

            newSpec.setOrganization(user.getOrganization());
            String datavalidationall = json.findValue("datavalidationall") == null ? ""
                    : json.findValue("datavalidationall").asText();
            if (datavalidationall != null) {
                if (datavalidationall.equals("")) {
                    newSpec.setIdentificationForDataValid(null);
                } else {
                    newSpec.setIdentificationForDataValid(datavalidationall);
                }
            }

            String datavalidation_pl_bs = json.findValue("datavalidation_pl_bs") == null ? null
                    : json.findValue("datavalidation_pl_bs").asText();
            if (datavalidation_pl_bs != null) {
                if (datavalidation_pl_bs != null && !datavalidation_pl_bs.equals("")) {
                    newSpec.setIdentificationForDataValidPLorBS(datavalidation_pl_bs);
                }
            }

            String openingBalance = json.findValue("openingBalance") == null ? null
                    : json.findValue("openingBalance").asText();
            if (openingBalance != null) {
                if (openingBalance != null && !openingBalance.equals("")) {
                    newSpec.setTotalOpeningBalance(Double.parseDouble(openingBalance));
                } else {
                    newSpec.setTotalOpeningBalance(null);
                }
            }
            /* Sunil: new chart of account */
            if (topMostParentCode.equals("1000000000000000000")) {
                String taxDateApplicable = "";
                if (json.has("taxApplicableDate")) {
                    taxDateApplicable = json.findValue("taxApplicableDate") != null
                            || !"".equals(json.findValue("taxApplicableDate"))
                                    ? json.findValue("taxApplicableDate").asText()
                                    : null;
                }
                if (taxDateApplicable != null && !taxDateApplicable.equals("")) {
                    try {
                        Date date = new Date();
                        String dateString = IdosConstants.IDOSDF.format(IdosConstants.IDOSDF.parse(taxDateApplicable));
                        String timeString = IdosConstants.TIMEFMT.format(date);
                        String applicableDateString = dateString + " " + timeString;
                        newSpec.setTaxApplicableDate(IdosConstants.IDOSDTF.parse(applicableDateString));
                    } catch (ParseException e) {
                        throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION,
                                "Data Format Exception for Date!", IdosConstants.DATA_FORMAT_ERRCODE);
                    }
                } else {
                    if (GSTTaxRate != null || cessTaxRate != null) {
                        newSpec.setTaxApplicableDate(new Date());
                    }
                }
            }
            specfcrud.save(user, newSpec, entityManager);

            if (topMostParentCode.equals("1000000000000000000") && newSpec.getIsCombinationSales() != null
                    && newSpec.getIsCombinationSales() == 1) {
                saveCombinationSalesItems(newSpec, json, user, request);
            }

            if (newSpec.getId() != null && topMostParentCode.equals("2000000000000000000")) {
                VENDOR_TDS_SERVICE.saveCOATdsSetup(json, user, newSpec, entityManager);
            }
            // First BranchSpecifics should be saved before inserting into TRADING_INVENTORY
            // table
            Specifics mapSpecf = null;
            Specifics incomeSpecificLinkedToExpense = null;
            if (incometoexpensemapping != null && !incometoexpensemapping.equals("")) { // In income specifics if it is
                                                                                        // mapped as inventory item
                mapSpecf = Specifics.findById(Long.parseLong(incometoexpensemapping)); // expense buy specific
            } else if (topMostParentCode.equals("2000000000000000000")) {
                criterias.clear();
                criterias.put("linkIncomeExpenseSpecifics.id", newSpec.getId());
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("presentStatus", 1);
                incomeSpecificLinkedToExpense = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
            }
            String itemBranch = "";
            String itemBranchesView = "";
            List<Long> newBranchList = null;
            List<Long> oldBranchList = null;
            if (!isNewSpecific) {
                oldBranchList = new ArrayList<>();
                newBranchList = new ArrayList<>();
            }
            List<BranchSpecifics> specificsBranchOldList = newSpec.getSpecificsBranch();
            List<BranchSpecifics> oldBranchSpecifics = new ArrayList<BranchSpecifics>();
            if (specificsBranchOldList != null) {
                for (BranchSpecifics branchSpecifics : specificsBranchOldList) {
                    if (branchSpecifics.getPresentStatus() == 1)
                        oldBranchSpecifics.add(branchSpecifics);
                    if (!isNewSpecific && oldBranchList != null) {
                        oldBranchList.add(branchSpecifics.getBranch().getId());
                    }
                }
            }

            List<BranchSpecifics> newBranchSpecifics = new ArrayList<BranchSpecifics>();
            if (itemBranchValues != null && !itemBranchValues.equals("")) {
                String[] walkinCustDiscountArr = null;
                if (null != walkinCustDiscount && !"".equals(walkinCustDiscount)) {
                    walkinCustDiscountArr = walkinCustDiscount.split(",");
                }
                String branchOpeningBalance = json.findPath("branchOpeningBalance") != null
                        ? json.findPath("branchOpeningBalance").asText()
                        : null;
                String[] branchOpeningBalanceArr = null;
                if (branchOpeningBalance != null) {
                    branchOpeningBalanceArr = branchOpeningBalance.split(",");
                }
                String branchInvNoOfUnit = json.findPath("branchInvNoOfUnit") != null
                        ? json.findPath("branchInvNoOfUnit").asText()
                        : null;
                String[] branchInvNoOfUnitArr = null;
                if (branchInvNoOfUnit != null) {
                    branchInvNoOfUnitArr = branchInvNoOfUnit.split(",");
                }
                String branchInvRate = json.findPath("branchInvRate") != null ? json.findPath("branchInvRate").asText()
                        : null;
                String[] branchInvRateArr = null;
                if (branchInvRate != null) {
                    branchInvRateArr = branchInvRate.split(",");
                }
                String branchInvOpeningBalance = json.findPath("branchInvOpeningBalance") != null
                        ? json.findPath("branchInvOpeningBalance").asText()
                        : null;
                String[] branchInvOpeningBalanceArr = null;
                if (branchInvOpeningBalance != null) {
                    branchInvOpeningBalanceArr = branchInvOpeningBalance.split(",");
                }

                for (int i = 0; i < itemBranches.length; i++) {
                    BranchSpecifics newBnchSpecf = new BranchSpecifics();
                    Branch selectedBranch = Branch.findById(IdosUtil.convertStringToLong(itemBranches[i]));
                    if (selectedBranch == null) {
                        throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
                                IdosConstants.INVALID_DATA_EXCEPTION, "Invalid Branch",
                                "Invalid Branch: " + itemBranches[i]);
                    }
                    itemBranch += selectedBranch.getName() + ",";
                    newBnchSpecf.setBranch(selectedBranch);
                    newBnchSpecf.setOrganization(selectedBranch.getOrganization());
                    newBnchSpecf.setSpecifics(newSpec);
                    newBnchSpecf.setParticular(newSpec.getParticularsId());
                    if (!isNewSpecific) {
                        if (oldBranchList == null || (!oldBranchList.contains(Long.parseLong(itemBranches[i])))
                                && newBranchList != null) {
                            newBranchList.add(Long.parseLong(itemBranches[i]));
                        }
                    }
                    if (walkinCustDiscountArr != null && walkinCustDiscountArr.length > i
                            && null != walkinCustDiscountArr[i] && !"".equals(walkinCustDiscountArr[i])) {
                        newBnchSpecf.setWalkinCustomerMaxDiscount(Double.parseDouble(walkinCustDiscountArr[i]));
                    }
                    if (branchOpeningBalanceArr != null && branchOpeningBalanceArr.length > i
                            && branchOpeningBalanceArr[i] != null && !"".equals(branchOpeningBalanceArr[i])) {
                        newBnchSpecf.setOpeningBalance(Double.parseDouble(branchOpeningBalanceArr[i]));
                    }
                    if (branchInvNoOfUnitArr != null && branchInvNoOfUnitArr.length > i
                            && branchInvNoOfUnitArr[i] != null && !"".equals(branchInvNoOfUnitArr[i])) {
                        newBnchSpecf.setInvOpeningBalUnits(Double.parseDouble(branchInvNoOfUnitArr[i]));
                    }
                    if (branchInvRateArr != null && branchInvRateArr.length > i && branchInvRateArr[i] != null
                            && !"".equals(branchInvRateArr[i])) {
                        newBnchSpecf.setInvOpeningBalRate(Double.parseDouble(branchInvRateArr[i]));
                    }
                    if (branchInvOpeningBalanceArr != null && branchInvOpeningBalanceArr.length > i
                            && branchInvOpeningBalanceArr[i] != null && !"".equals(branchInvOpeningBalanceArr[i])) {
                        newBnchSpecf.setInvOpeningBalance(Double.parseDouble(branchInvOpeningBalanceArr[i]));
                    }
                    newBranchSpecifics.add(newBnchSpecf);

                    if (newSpec.getId() != null && topMostParentCode.equals("2000000000000000000")) {
                        tradingInvCalcMethod = newSpec.getTradingInventoryCalcMethod(); // Sunil: In expense case this
                                                                                        // value will not come from UI.
                        Specifics specForIn = newSpec;
                        // Specifics SpecForIn = Specifics.findById(newSpec.getId()); // expence
                        /*
                         * Sunil: why need to make DB call in loop when same object need to fetch?, thus
                         * above is commented
                         * variable names in java should start with small letter, infact it is not
                         * needed but keeping.
                         **/
                        boolean isTxnPresent = isTransactionPresentBranch(user.getOrganization().getId(),
                                selectedBranch.getId(), specForIn.getId());// which income/expence
                        if (!isTxnPresent) {
                            /*
                             * Sunil: this will fetch income item if newSpec is expense else always
                             * empty/null. so why need to call
                             * when newSpec is income/assets/liabilities? added the check
                             * topMostParentCode.equals("2000000000000000000")
                             */
                            /*
                             * Sunil: not need to call this again and again in loop as all conditions are
                             * same?
                             * moved the code out of loop
                             * criterias.clear();
                             * criterias.put("linkIncomeExpenseSpecifics.id", newSpec.getId());
                             * criterias.put("organization.id", user.getOrganization().getId());
                             * foundSpecf = genericDAO.getByCriteria(Specifics.class, criterias,
                             * entityManager); // Income
                             */
                            if (incomeSpecificLinkedToExpense != null
                                    && incomeSpecificLinkedToExpense.getLinkIncomeExpenseSpecifics() != null) {
                                double conversionUnit = 1.0;
                                if (incomeSpecificLinkedToExpense.getNoOfExpenseUnits() != null
                                        && incomeSpecificLinkedToExpense.getNoOfIncomeUnits() != null
                                        && incomeSpecificLinkedToExpense.getNoOfIncomeUnits() > 0.0) {
                                    conversionUnit = incomeSpecificLinkedToExpense.getNoOfExpenseUnits()
                                            / incomeSpecificLinkedToExpense.getNoOfIncomeUnits();
                                }
                                TradingInventory tradingInv = TradingInventory.getTradingInventory(entityManager,
                                        user.getOrganization().getId(), selectedBranch.getId(), specForIn.getId(),
                                        IdosConstants.TRADING_INV_OPENING_BAL);
                                if (tradingInv == null) {
                                    tradingInv = new TradingInventory();
                                }
                                tradingInv.setDate(specForIn.getCreatedAt());
                                tradingInv.setBranch(selectedBranch);
                                tradingInv.setOrganization(user.getOrganization());
                                tradingInv.setUser(user);
                                tradingInv.setTransactionType(IdosConstants.TRADING_INV_OPENING_BAL);
                                tradingInv.setTransactionSpecifics(specForIn);
                                tradingInv.setTotalQuantity(newBnchSpecf.getInvOpeningBalUnits());
                                tradingInv.setCalcualtedRate(newBnchSpecf.getInvOpeningBalRate());
                                tradingInv.setGrossValue(newBnchSpecf.getInvOpeningBalance());
                                double qtyConvertedToIncomeUnit = 0.0;
                                double buyRate = 0.0;
                                if (newBnchSpecf.getInvOpeningBalUnits() != null
                                        && newBnchSpecf.getInvOpeningBalance() != null
                                        && newBnchSpecf.getInvOpeningBalUnits() > 0) {
                                    qtyConvertedToIncomeUnit = newBnchSpecf.getInvOpeningBalUnits() * conversionUnit; // so
                                                                                                                      // buyqty
                                                                                                                      // =
                                                                                                                      // 5bag,
                                                                                                                      // but
                                                                                                                      // 1bag
                                                                                                                      // =
                                                                                                                      // 100chocolate
                                                                                                                      // pieces
                                                                                                                      // when
                                                                                                                      // selling
                                                                                                                      // chocolate,
                                                                                                                      // then
                                                                                                                      // put
                                                                                                                      // 5oo
                                                                                                                      // for
                                                                                                                      // qty
                                    buyRate = newBnchSpecf.getInvOpeningBalance() / qtyConvertedToIncomeUnit;
                                    tradingInv.setNoOfExpUnitsConvertedToIncUnits(qtyConvertedToIncomeUnit);
                                    tradingInv.setCalcualtedRate(buyRate);
                                } else {
                                    tradingInv.setNoOfExpUnitsConvertedToIncUnits(0.0);
                                    tradingInv.setCalcualtedRate(0.0);
                                }
                                genericDAO.saveOrUpdate(tradingInv, user, entityManager); // save TradingInventory first
                                                                                          // entry for this item will be
                                                                                          // opening bal, when this item
                                                                                          // is created. Later buy/sell
                                                                                          // entries will be added in
                                                                                          // transacitoncontroller

                                if (tradingInvCalcMethod != null && tradingInvCalcMethod.equalsIgnoreCase("WAC")) {
                                    TradingInventory tradingInvclosing = TradingInventory.getTradingInventory(
                                            entityManager, user.getOrganization().getId(), selectedBranch.getId(),
                                            newSpec.getId(), IdosConstants.TRADING_INV_CLOSING_BAL);
                                    if (tradingInvclosing == null) {
                                        tradingInvclosing = new TradingInventory();
                                    }
                                    tradingInvclosing.setDate(newSpec.getCreatedAt());
                                    tradingInvclosing.setBranch(selectedBranch);
                                    tradingInvclosing.setOrganization(user.getOrganization());
                                    tradingInvclosing.setUser(user);
                                    tradingInvclosing.setTransactionSpecifics(newSpec); // for closing trade always use
                                                                                        // buyspecific because we might
                                                                                        // need to fetch this closing
                                                                                        // trade for buy transaction and
                                                                                        // at that time we don't get
                                                                                        // sellspecific from buy trade
                                    tradingInvclosing.setTransactionType(IdosConstants.TRADING_INV_CLOSING_BAL); // buy
                                                                                                                 // = 1,
                                                                                                                 // sell
                                                                                                                 // = 2,
                                                                                                                 // opening
                                                                                                                 // = 3,
                                                                                                                 // closing
                                                                                                                 // = 4
                                    tradingInvclosing.setTotalQuantity(newBnchSpecf.getInvOpeningBalUnits());
                                    tradingInvclosing.setGrossValue(newBnchSpecf.getInvOpeningBalance());
                                    tradingInvclosing.setNoOfExpUnitsConvertedToIncUnits(qtyConvertedToIncomeUnit);
                                    tradingInvclosing.setCalcualtedRate(buyRate);
                                    genericDAO.saveOrUpdate(tradingInvclosing, user, entityManager); // save closing
                                                                                                     // entry for
                                                                                                     // openingbal buy
                                                                                                     // trade
                                }
                            }
                        }
                    }
                }
                itemBranchesView = itemBranch.substring(0, itemBranch.length() - 1);
            }
            List<List<BranchSpecifics>> businessEntityTransactionList = ListUtility
                    .getBranchSpecificsTransactionList1(oldBranchSpecifics, newBranchSpecifics);
            for (int i = 0; i < businessEntityTransactionList.size(); i++) {
                if (i == 0) {
                    List<BranchSpecifics> oldBnchSpecf = businessEntityTransactionList.get(i);
                    if (oldBnchSpecf != null) {
                        for (BranchSpecifics bnchSpecifics : oldBnchSpecf) {
                            // entityManager.remove(bnchSpecifics); // need soft delete
                            bnchSpecifics.setPresentStatus(0); // deactivate by setting presentStatus to 0
                            genericDAO.saveOrUpdate(bnchSpecifics, user, entityManager);
                        }
                    }
                } else if (i == 1) {
                    List<BranchSpecifics> newBnchSpecf = businessEntityTransactionList.get(i);
                    if (newBnchSpecf != null) {
                        for (BranchSpecifics newbnchSpecifics : newBnchSpecf) {
                            newbnchSpecifics.setBudgetDate(Calendar.getInstance().getTime());
                            newbnchSpecifics.setPresentStatus(1);// Activate BranchSpecifics by setting presentStatus to
                                                                 // 1
                            genericDAO.saveOrUpdate(newbnchSpecifics, user, entityManager);
                        }
                    }
                }
            }

            if (mapSpecf != null) {
                double conversionUnit = 1;
                newSpec.setLinkIncomeExpenseSpecifics(mapSpecf);
                if (noOfExpUnit != null && noOfIncUnit != null && noOfIncUnit != "" && noOfExpUnit != "") {
                    newSpec.setNoOfExpenseUnits(Double.parseDouble(noOfExpUnit)); // Income specifics
                    newSpec.setNoOfIncomeUnits(Double.parseDouble(noOfIncUnit));
                    conversionUnit = Double.parseDouble(noOfIncUnit) / Double.parseDouble(noOfExpUnit);// 1 bag expense
                                                                                                       // unit = 200
                                                                                                       // peices
                                                                                                       // chocolate
                                                                                                       // (income unit)
                } else {
                    newSpec.setNoOfExpenseUnits(0.0);
                    newSpec.setNoOfIncomeUnits(0.0);
                }
                newSpec.setExpenseToIncomeConverstionRate(conversionUnit);
                newSpec.setIncomeUnitsMeasure(incUnitMeasure);
                newSpec.setExpenseUnitsMeasure(mapSpecf.getExpenseUnitsMeasure());
                newSpec.setTradingInventoryCalcMethod(tradingInvCalcMethod);
                newSpec.setIsTradingInvenotryItem(1);// so it will be inserted into TRADING_INVENTORY table too when
                                                     // buy/sell transaction for showing in inventory Report
                boolean isTxnPresent = isTransactionPresent(user.getOrganization().getId(), mapSpecf.getId());
                if (!isTxnPresent) {
                    List<BranchSpecifics> branchSpecifics = mapSpecf.getSpecificsBranch();
                    mapSpecf.setNoOfExpenseUnits(newSpec.getNoOfExpenseUnits());
                    mapSpecf.setNoOfIncomeUnits(newSpec.getNoOfIncomeUnits());
                    mapSpecf.setExpenseToIncomeConverstionRate(conversionUnit);
                    mapSpecf.setIsTradingInvenotryItem(1);
                    mapSpecf.setIncomeUnitsMeasure(incUnitMeasure);
                    mapSpecf.setTradingInventoryCalcMethod(tradingInvCalcMethod);
                    genericDAO.saveOrUpdate(mapSpecf, user, entityManager); // save mapped Expense buy specifics by
                                                                            // setting inventory_item = 1
                    for (BranchSpecifics branchSpecific : branchSpecifics) {
                        // if (branchSpecific.getInvOpeningBalUnits() != null &&
                        // branchSpecific.getInvOpeningBalUnits() > 0) {
                        TradingInventory tradingInv = TradingInventory.getTradingInventory(entityManager,
                                user.getOrganization().getId(), branchSpecific.getBranch().getId(), mapSpecf.getId(),
                                IdosConstants.TRADING_INV_OPENING_BAL);
                        if (tradingInv == null) {
                            tradingInv = new TradingInventory();
                        }
                        tradingInv.setDate(mapSpecf.getCreatedAt());
                        tradingInv.setBranch(branchSpecific.getBranch());
                        tradingInv.setOrganization(user.getOrganization());
                        tradingInv.setUser(user);
                        tradingInv.setTransactionType(IdosConstants.TRADING_INV_OPENING_BAL); // buy trade opening bal =
                                                                                              // 3, buy = 1, sell = 2,
                                                                                              // closing = 4
                        tradingInv.setTransactionSpecifics(mapSpecf);
                        tradingInv.setTotalQuantity(branchSpecific.getInvOpeningBalUnits());
                        tradingInv.setCalcualtedRate(branchSpecific.getInvOpeningBalRate());
                        tradingInv.setGrossValue(branchSpecific.getInvOpeningBalance());
                        double qtyConvertedToIncomeUnit = 0.0;
                        double buyRate = 0.0;
                        if (branchSpecific.getInvOpeningBalUnits() != null
                                && branchSpecific.getInvOpeningBalance() != null
                                && branchSpecific.getInvOpeningBalUnits() > 0) {
                            qtyConvertedToIncomeUnit = branchSpecific.getInvOpeningBalUnits() * conversionUnit; // so
                                                                                                                // buyqty
                                                                                                                // =
                                                                                                                // 5bag,
                                                                                                                // but
                                                                                                                // 1bag
                                                                                                                // =
                                                                                                                // 100chocolate
                                                                                                                // pieces
                                                                                                                // when
                                                                                                                // selling
                                                                                                                // chocolate,
                                                                                                                // then
                                                                                                                // put
                                                                                                                // 5oo
                                                                                                                // for
                                                                                                                // qty
                            buyRate = branchSpecific.getInvOpeningBalance() / qtyConvertedToIncomeUnit;
                            tradingInv.setNoOfExpUnitsConvertedToIncUnits(qtyConvertedToIncomeUnit);
                            tradingInv.setCalcualtedRate(buyRate);
                        }
                        genericDAO.saveOrUpdate(tradingInv, user, entityManager); // save TradingInventory first entry
                                                                                  // for this item will be opening bal,
                                                                                  // when this item is created. Later
                                                                                  // buy/sell entries will be added in
                                                                                  // transacitoncontroller

                        if (tradingInvCalcMethod != null && tradingInvCalcMethod.equalsIgnoreCase("WAC")) {
                            TradingInventory tradingInvclosing = TradingInventory.getTradingInventory(entityManager,
                                    user.getOrganization().getId(), branchSpecific.getBranch().getId(),
                                    mapSpecf.getId(), IdosConstants.TRADING_INV_CLOSING_BAL);
                            if (tradingInvclosing == null) {
                                tradingInvclosing = new TradingInventory();
                            }
                            tradingInvclosing.setDate(mapSpecf.getCreatedAt());
                            tradingInvclosing.setBranch(branchSpecific.getBranch());
                            tradingInvclosing.setOrganization(user.getOrganization());
                            tradingInvclosing.setUser(user);
                            tradingInvclosing.setTransactionSpecifics(mapSpecf); // for closing trade always use
                                                                                 // buyspecific because we might need to
                                                                                 // fetch this closing trade for buy
                                                                                 // transaction and at that time we
                                                                                 // don't get sellspecific from buy
                                                                                 // trade
                            tradingInvclosing.setTransactionType(IdosConstants.TRADING_INV_CLOSING_BAL); // buy = 1,
                                                                                                         // sell = 2,
                                                                                                         // opening = 3,
                                                                                                         // closing = 4
                            tradingInvclosing.setTotalQuantity(branchSpecific.getInvOpeningBalUnits());
                            tradingInvclosing.setGrossValue(branchSpecific.getInvOpeningBalance());
                            tradingInvclosing.setNoOfExpUnitsConvertedToIncUnits(qtyConvertedToIncomeUnit);
                            tradingInvclosing.setCalcualtedRate(buyRate);
                            genericDAO.saveOrUpdate(tradingInvclosing, user, entityManager); // save closing entry for
                                                                                             // openingbal buy trade
                        }
                        // }
                    }
                }

                // reorder level save logic start
                String reorderLevelbranchIds = json.findValue("reorderLevelbranchIds") != null
                        ? json.findValue("reorderLevelbranchIds").asText()
                        : null;
                String branchAlertUserForReorderLevels = json.findValue("branchAlertUserForReorderLevels") != null
                        ? json.findValue("branchAlertUserForReorderLevels").asText()
                        : null;
                String branchReorderLevels = json.findValue("branchReorderLevels") != null
                        ? json.findValue("branchReorderLevels").asText()
                        : null;
                if (reorderLevelbranchIds != null && !reorderLevelbranchIds.equals("")) {
                    String branchIds[] = reorderLevelbranchIds.split(",");
                    String alertUserForReorderLevels[] = branchAlertUserForReorderLevels.split(",");
                    String reorderLevels[] = branchReorderLevels.split(",");
                    List<WarehouseItemStockReorderLevel> oldItemWarehouseStockReorderLevel = newSpec
                            .getItemWarehouseStockReorderLevels();
                    List<WarehouseItemStockReorderLevel> newItemWarehouseStockReorderLevel = new ArrayList<WarehouseItemStockReorderLevel>();
                    for (int w = 0; w < branchIds.length; w++) {
                        if (alertUserForReorderLevels.length > 0)
                            if ((branchIds[w] != null && !branchIds[w].equals(""))
                                    && (alertUserForReorderLevels[w] != null)
                                    && (reorderLevels[w] != null)) {
                                WarehouseItemStockReorderLevel warehouseItemStockReorderLevel = new WarehouseItemStockReorderLevel();
                                Branch setBnch = Branch.findById(Long.parseLong(branchIds[w]));
                                warehouseItemStockReorderLevel.setBranch(setBnch);
                                warehouseItemStockReorderLevel.setOrganization(setBnch.getOrganization());
                                warehouseItemStockReorderLevel.setSpecifics(newSpec);
                                warehouseItemStockReorderLevel.setParticular(newSpec.getParticularsId());
                                if (!alertUserForReorderLevels[w].equals("")) {
                                    warehouseItemStockReorderLevel
                                            .setReorderLevelAlertUser(alertUserForReorderLevels[w]);
                                } else {
                                    warehouseItemStockReorderLevel.setReorderLevelAlertUser(null);
                                }
                                if (!reorderLevels[w].equals("")) {
                                    warehouseItemStockReorderLevel.setReorderLevel(Integer.parseInt(reorderLevels[w]));
                                } else {
                                    warehouseItemStockReorderLevel.setReorderLevel(0);
                                }
                                newItemWarehouseStockReorderLevel.add(warehouseItemStockReorderLevel);
                            }
                    }
                    List<List<WarehouseItemStockReorderLevel>> businessEntityWarehouseItemStockReorderLevelList = ListUtility
                            .getWarehouseItemStockReorderLevelList(oldItemWarehouseStockReorderLevel,
                                    newItemWarehouseStockReorderLevel);
                    for (int i = 0; i < businessEntityWarehouseItemStockReorderLevelList.size(); i++) {
                        if (i == 0) {
                            List<WarehouseItemStockReorderLevel> oldWarehouseItemStockReorderLevel = businessEntityWarehouseItemStockReorderLevelList
                                    .get(i);
                            if (oldWarehouseItemStockReorderLevel != null) {
                                for (WarehouseItemStockReorderLevel warehouseItemStockReorderLevel : oldWarehouseItemStockReorderLevel) {
                                    entityManager.remove(warehouseItemStockReorderLevel);
                                }
                            }
                        }
                        if (i == 1) {
                            List<WarehouseItemStockReorderLevel> newWarehouseItemStockReorderLevel = businessEntityWarehouseItemStockReorderLevelList
                                    .get(i);
                            if (newWarehouseItemStockReorderLevel != null) {
                                for (WarehouseItemStockReorderLevel newWarehouseItemStockReorderLevelObj : newWarehouseItemStockReorderLevel) {
                                    genericDAO.saveOrUpdate(newWarehouseItemStockReorderLevelObj, user, entityManager);
                                }
                            }
                        }
                    }
                }
                // reorder level save logic end
            } else {
                newSpec.setIsTradingInvenotryItem(null);
                newSpec.setLinkIncomeExpenseSpecifics(null);
            }

            // Start specifics transaction purpose logic,
            // Sunil: it now applicable for all 4 types of COA
            String specificsTransactionPurpose = json.findPath("itemTransactionPurpose") != null
                    ? json.findPath("itemTransactionPurpose").asText()
                    : null;
            if (specificsTransactionPurpose != null && !specificsTransactionPurpose.equals("")) {
                String[] itemTxnPurposes = specificsTransactionPurpose.split(",");
                List<SpecificsTransactionPurpose> oldSpecificsTransacion = newSpec.getSpecificsTransactionPurposes();
                List<SpecificsTransactionPurpose> newSpecificsTransaction = new ArrayList<SpecificsTransactionPurpose>();
                if (specificsTransactionPurpose != null && !specificsTransactionPurpose.equals("")) {
                    for (int i = 0; i < itemTxnPurposes.length; i++) {
                        if (itemTxnPurposes[i] != null && !itemTxnPurposes[i].equals("")) {
                            SpecificsTransactionPurpose newSpecfTxnPurpose = new SpecificsTransactionPurpose();
                            Branch newBnch = user.getBranch();
                            newSpecfTxnPurpose.setBranch(newBnch);
                            newSpecfTxnPurpose.setOrganization(newBnch.getOrganization());
                            newSpecfTxnPurpose.setSpecifics(newSpec);
                            newSpecfTxnPurpose.setParticulars(newSpec.getParticularsId());
                            TransactionPurpose txnPurpose = TransactionPurpose
                                    .findById(Long.parseLong(itemTxnPurposes[i]));
                            newSpecfTxnPurpose.setTransactionPurpose(txnPurpose);
                            newSpecificsTransaction.add(newSpecfTxnPurpose);
                        }
                    }
                }
                List<List<SpecificsTransactionPurpose>> specfTxnPurposeTransactionList = ListUtility
                        .getSpecificsTransactionPurposeTransactionList(oldSpecificsTransacion, newSpecificsTransaction);
                for (int i = 0; i < specfTxnPurposeTransactionList.size(); i++) {
                    if (i == 0) {
                        List<SpecificsTransactionPurpose> oldSpecftxnpurpose = specfTxnPurposeTransactionList.get(i);
                        if (oldSpecftxnpurpose != null) {
                            for (SpecificsTransactionPurpose txnPurpSpecf : oldSpecftxnpurpose) {
                                entityManager.remove(txnPurpSpecf);
                            }
                        }
                    }
                    if (i == 1) {
                        List<SpecificsTransactionPurpose> newSpecfTxnPurpose = specfTxnPurposeTransactionList.get(i);
                        if (newSpecfTxnPurpose != null) {
                            for (SpecificsTransactionPurpose newSpecificsTxnPurpose : newSpecfTxnPurpose) {
                                genericDAO.saveOrUpdate(newSpecificsTxnPurpose, user, entityManager);
                            }
                        }
                    }
                }
            }

            if (newSpec.getId() != null && topMostParentCode.equals("1000000000000000000")) {
                Boolean flag = false;
                Integer taxCategory = 2;
                Boolean isNewBranch = false;
                List<BranchSpecifics> findBranchBySpecific = BranchSpecifics.findBranchBySpecific(entityManager,
                        user.getOrganization().getId(), newSpec.getId());
                for (BranchSpecifics branchSpecifics : findBranchBySpecific) {
                    if (newBranchList != null && newBranchList.size() > 0) {
                        if (newBranchList.contains(branchSpecifics.getBranch().getId())) {
                            isNewBranch = true;
                        }
                    }
                    // saveUpdateBranchTax(branchSpecifics.getBranch().getId(), json, taxCategory,
                    // entityManager, user);
                    Boolean successFlag = applyTaxRulesToEachBranchSpecifics(newSpec, branchSpecifics, taxCategory,
                            json, entityManager, user, isNewBranch);
                    if (!flag) {
                        flag = successFlag;
                    }
                }

                if (flag && newSpec.getId() != null) {
                    if (SpecificTaxHistory.validate(user.getOrganization().getId(), newSpec.getId(),
                            newSpec.getGstTaxRate(), newSpec.getCessTaxRate())) {
                        if (newSpec.getGstTaxRate() != null || newSpec.getCessTaxRate() != null) {
                            SpecificTaxHistory newTaxHistory = new SpecificTaxHistory();
                            newTaxHistory.setOrganization(user.getOrganization());
                            newTaxHistory.setSpecifics(newSpec);
                            newTaxHistory.setTaxRate(newSpec.getGstTaxRate());
                            newTaxHistory.setCessRate(newSpec.getCessTaxRate());
                            newTaxHistory.setApplicableDate(newSpec.getTaxApplicableDate());
                            genericDAO.saveOrUpdate(newTaxHistory, user, entityManager);
                        }
                    }
                }
            } else if (newSpec.getId() != null && topMostParentCode.equals("2000000000000000000")) {
                Integer taxCategory = 1;
                List<BranchSpecifics> findBranchBySpecific = BranchSpecifics.findBranchBySpecific(entityManager,
                        user.getOrganization().getId(), newSpec.getId());
                for (BranchSpecifics branchSpecifics : findBranchBySpecific) {
                    taxService.saveInputTaxCOA(newSpec, branchSpecifics.getBranch(), entityManager, user);
                }
            }

            // End Transaction Purpose
            /* Sunil: as per new GUI vendor entry is not applicable */

            //// Start Branch bank Accounts logic
            //if ((topMostParentCode.equals("3000000000000000000") && (datavalidationall.equals("4")))
            //        || ((topMostParentCode.equals("4000000000000000000") && datavalidationall.equals("5")))) {
            //    BranchBankAccounts newBnchBnkAct = null;
            //    String branchBankAccountHidId = json.findPath("branchBankAccountHidId").asText();
            //    /*
            //     * if (branchBankAccountHidId == "") {
            //     * branchBankAccountHidId = ",";
            //     * }
            //     */
            //    String branchBankAccountBankName = json.findPath("branchBankAccountBankName").asText();
            //    String branchBankAccountType = json.findPath("branchBankAccountType").asText();
            //    String branchBankAccountNumber = json.findPath("branchBankAccountNumber").asText();
            //    String branchBankAccountOpeningBalance = json.findPath("branchBankAccountOpeningBalance").asText();
            //    String branchBankAccounttAuthSignName = json.findPath("branchBankAccounttAuthSignName").asText();
            //    String branchBankAccounttAuthSignEmail = json.findPath("branchBankAccounttAuthSignEmail").asText();
            //    String branchBankAccounttAddress = json.findPath("branchBankAccounttAddress").asText();
            //    String branchBankAccounttPhnNoCtryCode = json.findPath("branchBankAccounttPhnNoCtryCode").asText();
            //    String branchBankAccountPhnNo = json.findPath("branchBankAccountPhnNo").asText();
            //    String branchBankAccountSwiftCode = json.findPath("branchBankAccountSwiftCode").asText();
            //    String branchBankAccountIfscCode = json.findPath("branchBankAccounttIfscCode").asText();
            //    String branchBankAccountRoutingNumber = json.findPath("branchBankAccountRoutingNumber").asText();
            //    String branchBankOpeningBalance = json.findPath("branchOpeningBalance").asText();
            //    String bnchBankAccountHidId = branchBankAccountHidId;
            //    String bnchBankAccountBankName = branchBankAccountBankName;
            //    String bnchBankAccountType = branchBankAccountType;
            //    String bnchBankAccountNumber = branchBankAccountNumber;
            //    String bnchBankOpeningBalance = branchBankAccountOpeningBalance;
            //    String bnchBankAccounttAuthSignName = branchBankAccounttAuthSignName;
            //    String bnchBankAccounttAuthSignEmail = branchBankAccounttAuthSignEmail;
            //    String bnchBankAccounttAddress = branchBankAccounttAddress;
            //    String bnchBankAccounttPhnNoCtryCode = branchBankAccounttPhnNoCtryCode;
            //    String bnchBankAccountPhnNo = branchBankAccountPhnNo;
            //    String bnchBankAccountSwiftCode = branchBankAccountSwiftCode;
            //    String bnchBankAccountRoutingNumber = branchBankAccountRoutingNumber;
            //    newBnchBnkAct = BranchBankAccounts.findById(IdosUtil.convertStringToLong(bnchBankAccountHidId));
            //    if (newBnchBnkAct == null) {
            //        newBnchBnkAct = new BranchBankAccounts();
            //    }
            //    criterias.clear();
            //    criterias.put("branchBankAccounts.id", IdosUtil.convertStringToLong(bnchBankAccountHidId));
            //    criterias.put("presentStatus", 1);
            //    newBnchBnkAct.setBankName(bnchBankAccountBankName.trim());
            //    newBnchBnkAct.setAccountNumber(bnchBankAccountNumber.trim());
            //    String bankOpeningBal[] = null;
            //    if (branchBankOpeningBalance != null || branchBankOpeningBalance != ",") {
            //        bankOpeningBal = branchBankOpeningBalance.split(",");
            //    }

            //    for (int i = 0; i < itemBranches.length; i++) {
            //        BranchSpecifics newBnchSpecf = new BranchSpecifics();
            //        Branch selectedBranch = Branch.findById(IdosUtil.convertStringToLong(itemBranches[i]));
            //        if (selectedBranch == null) {
            //            throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
            //                    IdosConstants.INVALID_DATA_EXCEPTION, "Invalid Branch",
            //                    "Invalid Branch: " + itemBranches[i]);
            //        }
            //        // if(newBnchBnkAct.getOpeningBalance() == null && bnchBankOpeningBalance[i]
            //        // !=null && !bnchBankOpeningBalance[i].trim().equals("")){

            //        newBnchBnkAct.setBankAddress(bnchBankAccounttAddress.trim());
            //        newBnchBnkAct.setBankNumberPhnCtryCode(bnchBankAccounttPhnNoCtryCode.trim());
            //        newBnchBnkAct.setPhoneNumber(bnchBankAccountPhnNo.trim());
            //        newBnchBnkAct.setSwiftCode(bnchBankAccountSwiftCode);
            //        newBnchBnkAct.setRoutingNumber(bnchBankAccountRoutingNumber.trim());
            //        newBnchBnkAct.setAuthorizedSignatoryEmail(bnchBankAccounttAuthSignEmail);
            //        newBnchBnkAct.setAuthorizedSignatoryName(bnchBankAccounttAuthSignName);
            //        newBnchBnkAct.setBankAddress(bnchBankAccounttAddress);
            //        newBnchBnkAct.setIfscCode(branchBankAccountIfscCode);
            //        newBnchBnkAct.setBranch(selectedBranch);
            //        newBnchBnkAct.setOrganization(user.getOrganization());
            //        newBnchBnkAct.setAccountType(IdosUtil.convertStringToInt(bnchBankAccountType));

            //        bnchBankActcrud.save(user, newBnchBnkAct, entityManager);

            //        BranchBankAccountBalance branchBankAccountBal = null;
            //        if ( bankOpeningBal[i] != null && !bankOpeningBal[i].trim().equals("")) {

            //            String newsbquery = "select obj from BranchBankAccountBalance obj WHERE obj.branch.id=?1 AND obj.organization.id=?2 and obj.branchBankAccounts.id=?3 and obj.presentStatus=1 ORDER BY obj.date desc";
            //            ArrayList inparam2 = new ArrayList(3);
            //            inparam2.add(IdosUtil.convertStringToLong(itemBranches[i]));
            //            inparam2.add(newBnchBnkAct.getOrganization().getId());
            //            inparam2.add(newBnchBnkAct.getId());
            //            // List<BranchBankAccountBalance> branchBankAccountBalList =
            //            // genericDAO.executeSimpleQueryWithLimit(newsbquery.toString(),entityManager,1);
            //            List<BranchBankAccountBalance> branchBankAccountBalList = genericDAO.queryWithParams(newsbquery,
            //                    entityManager, inparam2);
            //            if (branchBankAccountBalList.size() > 0) {
            //                branchBankAccountBal = branchBankAccountBalList.get(0);
            //                if (branchBankAccountBal.getAmountBalance() != null) {
            //                    Double amountBalanceTmp = branchBankAccountBal.getAmountBalance();
            //                    // remove old
            //                    if (newBnchBnkAct.getOpeningBalance() != null) {
            //                        amountBalanceTmp -= newBnchBnkAct.getOpeningBalance();
            //                    }

            //                    amountBalanceTmp += IdosUtil.convertStringToDouble(bankOpeningBal[i].trim());
            //                    branchBankAccountBal.setAmountBalance(amountBalanceTmp);

            //                } else {
            //                    branchBankAccountBal.setAmountBalance(
            //                            IdosUtil.convertStringToDouble(bankOpeningBal[i].trim()));
            //                }
            //                if (branchBankAccountBal.getResultantCash() != null) {
            //                    Double resultantAmountTmp = branchBankAccountBal.getResultantCash();
            //                    // Remove old
            //                    if (newBnchBnkAct.getOpeningBalance() != null) {
            //                        resultantAmountTmp -= newBnchBnkAct.getOpeningBalance();
            //                    }
            //                    resultantAmountTmp += IdosUtil.convertStringToDouble(bankOpeningBal[i].trim());
            //                    branchBankAccountBal.setResultantCash(resultantAmountTmp);
            //                } else {
            //                    branchBankAccountBal.setResultantCash(
            //                            IdosUtil.convertStringToDouble(bankOpeningBal[i].trim()));
            //                }
            //                genericDAO.saveOrUpdate(branchBankAccountBal, user, entityManager);
            //                // Set after less old
            //                newBnchBnkAct
            //                        .setOpeningBalance(IdosUtil.convertStringToDouble(bankOpeningBal[i].trim()));
            //            }else{
            //                branchBankAccountBal = new BranchBankAccountBalance();
            //                branchBankAccountBal.setBranchBankAccounts(newBnchBnkAct);
            //                branchBankAccountBal.setBranch(selectedBranch);
            //                branchBankAccountBal.setOrganization(user.getOrganization());
            //                branchBankAccountBal.setDate(Calendar.getInstance().getTime());
            //                newBnchBnkAct
            //                        .setOpeningBalance(IdosUtil.convertStringToDouble(bankOpeningBal[i].trim()));
            //                branchBankAccountBal
            //                        .setAmountBalance(IdosUtil.convertStringToDouble(bankOpeningBal[i].trim()));
            //                branchBankAccountBal
            //                        .setResultantCash(IdosUtil.convertStringToDouble(bankOpeningBal[i].trim()));
            //                genericDAO.saveOrUpdate(branchBankAccountBal, user, entityManager);
            //            }
            //        }

            //        genericDAO.saveOrUpdate(newBnchBnkAct, user, entityManager);
            //        auditDAO.saveAuditLogs("created branch bank account details", user, newBnchBnkAct.getId(),
            //                BranchBankAccounts.class, ipAddress, json.toString(), entityManager);

            //        BranchBankAccountMapping branchBankAccountMapping = new BranchBankAccountMapping();
            //        branchBankAccountMapping.setBranchBankAccounts(newBnchBnkAct);
            //        branchBankAccountMapping.setOrganization(user.getOrganization());
            //        branchBankAccountMapping.setBranch(selectedBranch);
            //        branchBankAccountMapping.setSpecifics(newSpec);
            //        branchBankAccountMapping.setBranchBankAccountBalance(branchBankAccountBal);
            //        BranchBankAccountMapping branchBankAccountMappings = branchBankAccountMapping.findByBranchOrgAndBankId(entityManager, user.getOrganization(), selectedBranch, newBnchBnkAct, newSpec);
            //        if(branchBankAccountMappings == null){
            //            genericDAO.save(branchBankAccountMapping, user, entityManager);
            //        } else {
            //            branchBankAccountMapping.setId(branchBankAccountMappings.getId());
            //            genericDAO.saveOrUpdate(branchBankAccountMapping, user, entityManager);
            //        }

            //        auditDAO.saveAuditLogs("update branch bank account details", user, newBnchBnkAct.getId(),
            //                BranchBankAccounts.class, ipAddress, json.toString(), entityManager);
            //    }
            //}
            //// End Branch bank Accounts logic

            // start knowledge library transaction
            String knowledgeLibraryHiddenIds = json.findPath("knowledgeLibraryHiddenIds").asText();
            String knowledgeLibraryHiddenIdsArr[] = knowledgeLibraryHiddenIds.split(",");
            String knowledgeLibraryContent = json.findPath("knowledgeLibraryContent").asText();
            String knowledgeLibraryContentArr[] = knowledgeLibraryContent.split(",");
            String isknowledgeLibraryMandatory = json.findPath("isknowledgeLibraryMandatory").asText();
            String isknowledgeLibraryMandatoryArr[] = isknowledgeLibraryMandatory.split(",");
            String knowledgeLibraryForBranches = json.findPath("knowledgeLibraryForBranches").asText();
            String knowledgeLibraryForBranchesArr[] = knowledgeLibraryForBranches.split("#");
            SpecificsKnowledgeLibrary specificsKl = null;
            for (int i = 0; (i < knowledgeLibraryHiddenIdsArr.length || i < knowledgeLibraryContentArr.length); i++) {
                if (knowledgeLibraryHiddenIdsArr.length > i) {
                    if (!knowledgeLibraryHiddenIdsArr[i].trim().equals("")
                            && knowledgeLibraryHiddenIdsArr[i].trim() != null) {
                        specificsKl = SpecificsKnowledgeLibrary
                                .findById(Long.parseLong(knowledgeLibraryHiddenIdsArr[i].trim()));
                    } else {
                        specificsKl = new SpecificsKnowledgeLibrary();
                    }
                    if (knowledgeLibraryContentArr.length > i) {
                        specificsKl.setKnowledgeLibraryContent(knowledgeLibraryContentArr[i].trim());
                    }
                    if (isknowledgeLibraryMandatoryArr.length > i) {
                        if (!"".equals(isknowledgeLibraryMandatoryArr[i]))
                            specificsKl.setIsMandatory(Integer.parseInt(isknowledgeLibraryMandatoryArr[i]));
                    }
                    specificsKl.setSpecifics(newSpec);
                    specificsKl.setParticulars(newSpec.getParticularsId());
                    genericDAO.saveOrUpdate(specificsKl, user, entityManager);
                    List<SpecificsKnowledgeLibraryForBranch> oldSpecificskl = specificsKl.getSpecificsKlibrary();
                    List<SpecificsKnowledgeLibraryForBranch> newSpecificskl = new ArrayList<SpecificsKnowledgeLibraryForBranch>();
                    if (knowledgeLibraryForBranchesArr[i].trim() != null
                            && !knowledgeLibraryForBranchesArr[i].trim().equals("")) {
                        String specfklBranchArray[] = knowledgeLibraryForBranchesArr[i].split(",");
                        for (int j = 0; j < specfklBranchArray.length; j++) {
                            SpecificsKnowledgeLibraryForBranch newSpecfKlforBranches = new SpecificsKnowledgeLibraryForBranch();
                            Branch bnch = Branch.findById(Long.parseLong(specfklBranchArray[j]));
                            newSpecfKlforBranches.setBranch(bnch);
                            newSpecfKlforBranches.setOrganization(bnch.getOrganization());
                            newSpecfKlforBranches.setSpecifics(newSpec);
                            newSpecfKlforBranches.setParticulars(newSpec.getParticularsId());
                            newSpecfKlforBranches.setSpecificsKl(specificsKl);
                            newSpecificskl.add(newSpecfKlforBranches);
                        }
                    }
                    List<List<SpecificsKnowledgeLibraryForBranch>> specificsklTransactionList = ListUtility
                            .getSpecificsKnowledgeLibraryTransactionList(oldSpecificskl, newSpecificskl);
                    for (int j = 0; j < specificsklTransactionList.size(); j++) {
                        if (j == 0) {
                            List<SpecificsKnowledgeLibraryForBranch> oldSpecfkl = specificsklTransactionList.get(j);
                            if (oldSpecfkl != null) {
                                for (SpecificsKnowledgeLibraryForBranch klSpecf : oldSpecfkl) {
                                    entityManager.remove(klSpecf);
                                }
                            }
                        }
                        if (j == 1) {
                            List<SpecificsKnowledgeLibraryForBranch> newSpecfkl = specificsklTransactionList.get(j);
                            if (newSpecfkl != null) {
                                for (SpecificsKnowledgeLibraryForBranch newklSpecifics : newSpecfkl) {
                                    genericDAO.saveOrUpdate(newklSpecifics, user, entityManager);
                                }
                            }
                        }
                    }
                }
            }
            // end knowledge library transaction
            if (topMostParentCode.equals("2000000000000000000")) {
                String docUploadRuleExpenseItemBranchesStr = json
                        .findValue("docUploadRuleExpenseItemBranchesStr") != null
                                ? json.findValue("docUploadRuleExpenseItemBranchesStr").asText()
                                : null;
                String docUploadRuleMonetoryLimitsForExpenseItemInIndividualBranches = json
                        .findValue("docUploadRuleMonetoryLimitsForExpenseItemInIndividualBranches") != null
                                ? json.findValue("docUploadRuleMonetoryLimitsForExpenseItemInIndividualBranches")
                                        .asText()
                                : null;
                criterias.clear();
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("specifics.id", newSpec.getId());
                criterias.put("presentStatus", 1);
                List<SpecificsDocUploadMonetoryRuleForBranch> oldspecificsDocUploadMonLimitRuleForBranches = genericDAO
                        .findByCriteria(SpecificsDocUploadMonetoryRuleForBranch.class, criterias, entityManager);
                List<SpecificsDocUploadMonetoryRuleForBranch> newspecificsDocUploadMonLimitRuleForBranches = new ArrayList<SpecificsDocUploadMonetoryRuleForBranch>();
                if (docUploadRuleExpenseItemBranchesStr != null && !docUploadRuleExpenseItemBranchesStr.equals("")) {
                    String docUploadRuleExpenseItemBranchesStrArr[] = docUploadRuleExpenseItemBranchesStr.split(",");
                    String docUploadRuleMonetoryLimitsForExpenseItemInIndividualBranchesArr[] = docUploadRuleMonetoryLimitsForExpenseItemInIndividualBranches
                            .split(",");
                    for (int i = 0; i < docUploadRuleExpenseItemBranchesStrArr.length; i++) {
                        SpecificsDocUploadMonetoryRuleForBranch newSpecRuleBnch = new SpecificsDocUploadMonetoryRuleForBranch();
                        if (!docUploadRuleMonetoryLimitsForExpenseItemInIndividualBranchesArr[i].trim().equals("")) {
                            newSpecRuleBnch.setMonetoryLimit(Double
                                    .parseDouble(docUploadRuleMonetoryLimitsForExpenseItemInIndividualBranchesArr[i]));
                        } else {
                            newSpecRuleBnch.setMonetoryLimit(0.0);
                        }
                        Branch existingBnch = Branch
                                .findById(Long.parseLong(docUploadRuleExpenseItemBranchesStrArr[i]));
                        newSpecRuleBnch.setBranch(existingBnch);
                        newSpecRuleBnch.setOrganization(existingBnch.getOrganization());
                        newSpecRuleBnch.setSpecifics(newSpec);
                        newSpecRuleBnch.setParticulars(newSpec.getParticularsId());
                        newspecificsDocUploadMonLimitRuleForBranches.add(newSpecRuleBnch);
                    }
                }
                List<List<SpecificsDocUploadMonetoryRuleForBranch>> specfDocUploadRuleList = ListUtility
                        .getSpecDUMLRFBnch(oldspecificsDocUploadMonLimitRuleForBranches,
                                newspecificsDocUploadMonLimitRuleForBranches);
                for (int i = 0; i < specfDocUploadRuleList.size(); i++) {
                    if (i == 0) {
                        List<SpecificsDocUploadMonetoryRuleForBranch> oldspecfDocUploadRule = specfDocUploadRuleList
                                .get(i);
                        if (oldspecfDocUploadRule != null) {
                            for (SpecificsDocUploadMonetoryRuleForBranch specfDocUploadRule : oldspecfDocUploadRule) {
                                entityManager.remove(specfDocUploadRule);
                            }
                        }
                    }
                    if (i == 1) {
                        List<SpecificsDocUploadMonetoryRuleForBranch> newspecfDocUploadRule = specfDocUploadRuleList
                                .get(i);
                        if (newspecfDocUploadRule != null) {
                            for (SpecificsDocUploadMonetoryRuleForBranch specfDocUploadRule : newspecfDocUploadRule) {
                                genericDAO.saveOrUpdate(specfDocUploadRule, user, entityManager);
                            }
                        }
                    }
                    if (i == 2) {
                        List<SpecificsDocUploadMonetoryRuleForBranch> updatespecfDocUploadRule = specfDocUploadRuleList
                                .get(i);
                        if (updatespecfDocUploadRule != null) {
                            for (SpecificsDocUploadMonetoryRuleForBranch specfDocUploadRule : updatespecfDocUploadRule) {
                                genericDAO.saveOrUpdate(specfDocUploadRule, user, entityManager);
                            }
                        }
                    }
                }
            }
            // entityManager.getTransaction().commit();
            String specificName = specName != null ? specName.toLowerCase() : null;
            specificName = IdosUtil.escapeHtml(specificName);
            String stbuf = ("select obj from Specifics obj where obj.id = ?1 and obj.organization.id = ?2 and obj.particularsId.id = ?3 and lower(obj.name) = ?4 and obj.presentStatus=1");
            ArrayList inparams = new ArrayList(4);
            inparams.add(newSpec.getId());
            inparams.add(user.getOrganization().getId());
            inparams.add(newSpecPart.getId());
            inparams.add(specificName);
            List<Specifics> newSpecOption = genericDAO.queryWithParams(stbuf, entityManager, inparams);
            Specifics enteredSpecifics = null;
            for (Specifics specf : newSpecOption) {
                enteredSpecifics = specf;
            }
            Map<String, ActorRef> orgregistrered = new HashMap<String, ActorRef>();
            System.out.println("expenseSpecfWithholdingMonetoryLimit" + orgregistrered);
            // Object[] keyArray = SpecificsTransactionActor.registrered.keySet().toArray();
            // System.out.println("expenseSpecfWithholdingMonetoryLimit....." + keyArray);
            // for (int i = 0; i < keyArray.length; i++) {
            // List<Users> users = Users.findByEmailActDeact(entityManager, (String)
            // keyArray[i]);
            // if (!users.isEmpty()) {
            // Long usrOrgID = 0l;
            // Long tmpOrgID = 0l;
            // if (users.get(0) != null && users.get(0).getOrganization() != null
            // && users.get(0).getOrganization().getId() != null) {
            // usrOrgID = users.get(0).getOrganization().getId();
            // }
            // if (enteredSpecifics != null && enteredSpecifics.getOrganization() != null
            // && enteredSpecifics.getOrganization().getId() != null) {
            // tmpOrgID = enteredSpecifics.getOrganization().getId();
            // }
            // if (usrOrgID != 0 && tmpOrgID != null && tmpOrgID == usrOrgID) {
            // orgregistrered.put(keyArray[i].toString(),
            // SpecificsTransactionActor.registrered.get(keyArray[i]));
            // }
            // }
            // }
            String newsbquery = ("select obj from Specifics obj WHERE obj.organization.id  = ?1 and obj.accountCodeHirarchy like ?2 and obj.presentStatus=1");
            inparam.clear();
            inparam.add(user.getOrganization().getId());
            inparam.add("%" + parentActCode + "%");
            List<Specifics> childspecifics = genericDAO.queryWithParams(newsbquery, entityManager, inparam);
            Long particularAccountCodeTmp = 0l;
            if (enteredSpecifics != null) {
                if (enteredSpecifics.getParticularsId() != null) {
                    particularAccountCodeTmp = enteredSpecifics.getParticularsId().getAccountCode();
                }
                Long particularIdTmp = 0l;
                if (enteredSpecifics.getParticularsId() != null) {
                    particularIdTmp = enteredSpecifics.getParticularsId().getId();
                }
                String identDataValid = enteredSpecifics.getIdentificationForDataValid();
                // SpecificsTransactionActor.add(enteredSpecifics.getId(), parentActCode,
                // particularAccountCodeTmp,
                // childspecifics, enteredSpecifics.getName(), particularIdTmp,
                // enteredSpecifics.getAccountCode(),
                // orgregistrered, enteredSpecifics.getOrganization().getName(),
                // itemBranchesView, btnName,
                // itemHidpk, identDataValid);
                String role = "";
                List<UsersRoles> userRoles = UsersRoles.getUserRoleList(entityManager, user.getOrganization().getId(),
                        user.getId(),
                        user.getBranch().getId());
                for (UsersRoles roles : userRoles) {
                    role += roles.getRole().getName() + ",";
                }
                role = role.substring(0, role.length() - 1);
                result.put("role", role);
                ArrayNode coaChildData = result.putArray("coaChildData");
                result.put("id", enteredSpecifics.getId());
                result.put("name", enteredSpecifics.getName());
                result.put("catId", particularIdTmp);
                result.put("accountCode", enteredSpecifics.getAccountCode());
                result.put("organization", enteredSpecifics.getOrganization().getName());
                result.put("itemBranches", itemBranchesView);
                result.put("topLevelAccountCode", particularAccountCodeTmp);
                result.put("parentAccountCode", String.valueOf(parentActCode));
                result.put("btnName", btnName);
                result.put("itemHidpk", itemHidpk);
                result.put("identDataValid", identDataValid);
                if (childspecifics.size() > 0) {
                    for (Specifics specf : childspecifics) {
                        ObjectNode row = Json.newObject();
                        row.put("id", specf.getId());
                        row.put("name", specf.getName());
                        row.put("accountCode", specf.getAccountCode());
                        coaChildData.add(row);
                    }
                }
            }
            if (ConfigParams.getInstance().isDeploymentSingleUser(user)) { // #SingleUser
                // For Single User Deployment Only
                if (newSpec.getModifiedAt() == null) {
                    singleUserService.updateOnCOACreation(user, newSpec, entityManager);
                }
            }
            entityTransaction.commit();
            result.put("specificId", newSpec.getId());
        } catch (Exception ex) {
            ex.printStackTrace();
            reportException(entityManager, entityTransaction, user, ex, result);
        } catch (Throwable th) {
            th.printStackTrace();
            reportThrowable(entityManager, entityTransaction, user, th, result);
        }
        return Results.ok(result);
    }

    @Transactional
    public Result checkBarcode(Request request) {
        log.log(Level.FINE, ">>>> Start");
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            // EntityManager entityManager = getEntityManager();
            JsonNode json = request.body().asJson();
            ArrayNode an = result.putArray("barcodeExistsData");
            String barcodeNo = json.findValue("barcodeNo").asText();
            String barcodeElemId = json.findValue("barcodeElemId").asText();
            String accHier = "";
            user = getUserInfo(request);
            ObjectNode row = Json.newObject();
            if (barcodeElemId.substring(11).equals("Inc")) {
                // StringBuilder sbquery = new StringBuilder("");
                // sbquery.append("select obj.barcode from Specifics obj where obj.barcode =
                // '"+barcodeNo+"' and obj.accountCodeHirarchy like '/10%' ");
                // List checkBarcodeExistsInc =
                // genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
                accHier = "'/10%'";
                ArrayList inparam = new ArrayList(3);
                inparam.add(barcodeNo);
                inparam.add(accHier);
                inparam.add(user.getOrganization().getId());
                List<Specifics> checkBarcodeExistsInc = genericDAO.queryWithParams(BARCODE_JQL, entityManager, inparam);
                if (checkBarcodeExistsInc.size() > 0) {
                    row.put("barcodeMessage", "Barcode is present");
                    an.add(row);
                } else {
                    row.put("barcodeMessage", "Barcode is not present");
                    an.add(row);
                }
            }

            if (barcodeElemId.substring(11).equals("Exp")) {
                // StringBuilder sbquery1 = new StringBuilder("");
                // sbquery1.append("select obj.barcode from Specifics obj where obj.barcode =
                // '"+barcodeNo+"' and obj.accountCodeHirarchy like '/20%' ");
                // List checkBarcodeExistsExp =
                // genericDAO.executeSimpleQuery(sbquery1.toString(), entityManager);
                accHier = "'/20%'";
                ArrayList inparam1 = new ArrayList(3);
                inparam1.add(barcodeNo);
                inparam1.add(accHier);
                inparam1.add(user.getOrganization().getId());
                List<Specifics> checkBarcodeExistsExp = genericDAO.queryWithParams(BARCODE_JQL, entityManager,
                        inparam1);
                if (checkBarcodeExistsExp.size() > 0) {
                    row.put("barcodeMessage", "Barcode is present");
                    an.add(row);
                } else {
                    row.put("barcodeMessage", "Barcode is not present");
                    an.add(row);
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "CheckEmail Email", "CheckEmail Organization",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result);
    }

    @Transactional
    public Result getSelectedExpenseItemUnit(Request request) {
        ObjectNode results = Json.newObject();
        Users usrinfo = null;
        try {
            JsonNode json = request.body().asJson();
            ArrayNode expSpecificsan = results.putArray("expenseSepecificsData");
            usrinfo = getUserInfo(request);
            Long specificsEntityId = json.findValue("incometoexpensemapping") != null
                    ? json.findValue("incometoexpensemapping").asLong()
                    : 0l;
            Specifics expenseSpecifics = Specifics.findById(specificsEntityId);
            if (expenseSpecifics != null) {
                ObjectNode row = Json.newObject();
                row.put("id", expenseSpecifics.getId());
                row.put("name", expenseSpecifics.getName());
                row.put("expUnitMeasure", expenseSpecifics.getExpenseUnitsMeasure());
                if (expenseSpecifics.getNoOfExpenseUnits() != null) {
                    row.put("noOfExpUnit", expenseSpecifics.getNoOfExpenseUnits());
                }
                if (expenseSpecifics.getIncomeUnitsMeasure() != null
                        || expenseSpecifics.getIncomeUnitsMeasure() != "") {
                    row.put("incUnitMeasure", expenseSpecifics.getIncomeUnitsMeasure());
                }
                if (expenseSpecifics.getNoOfIncomeUnits() != null) {
                    row.put("noOfIncUnit", expenseSpecifics.getNoOfIncomeUnits());
                }
                if (expenseSpecifics.getTradingInventoryCalcMethod() != null) {
                    row.put("calcMethod", expenseSpecifics.getTradingInventoryCalcMethod());
                }
                expSpecificsan.add(row);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, usrinfo.getEmail(), usrinfo.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(results).withHeader("ContentType", "application/json");

    }

    @Transactional
    public Result showItemsDetails(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        ObjectNode results = Json.newObject();
        try {
            JsonNode json = request.body().asJson();
            ArrayNode itemdetailan = results.putArray("itemdetailsData");
            ArrayNode itemKnowledgeLibraryan = results.putArray("itemKlData");
            ArrayNode itemDocUploadRulean = results.putArray("itemDocUploadRuleData");
            String specificsEntityId = json.findValue("entityPrimaryId").asText();
            Specifics specfs = Specifics.findById(IdosUtil.convertStringToLong(specificsEntityId));
            List<SpecificsKnowledgeLibrary> specfKlList = SpecificsKnowledgeLibrary.findBySpecific(entityManager,
                    specfs.getId());
            if (specfs != null) {
                ObjectNode row = Json.newObject();
                row.put("id", specfs.getId());
                row.put("name", IdosUtil.unescapeHtml(specfs.getName()));
                if (specfs.getParentSpecifics() == null) {
                    row.put("itemparent", specfs.getParticularsId().getId());
                    row.put("itemparenttext", specfs.getParticularsId().getName());
                }
                if (specfs.getParentSpecifics() != null) {
                    row.put("itemparent", specfs.getParentSpecifics().getId());
                    row.put("itemparenttext", specfs.getParentSpecifics().getName());
                    row.put("parentIndentForDataValid", specfs.getParentSpecifics().getIdentificationForDataValid());
                }
                String incomeSpecfPerUnitPrice = "";
                if (specfs.getIncomeSpecfPerUnitPrice() != null) {
                    row.put("incomeSpecfPerUnitPrice", specfs.getIncomeSpecfPerUnitPrice());
                } else {
                    row.put("incomeSpecfPerUnitPrice", incomeSpecfPerUnitPrice);
                }
                if (specfs.getEmployeeClaimItem() != null) {
                    row.put("isEmployeeClaimItem", specfs.getEmployeeClaimItem());
                } else {
                    row.put("isEmployeeClaimItem", "0");
                }
                if (specfs.getExpenseUnitsMeasure() != null) {
                    row.put("expUnitMeasure", specfs.getExpenseUnitsMeasure());
                } else {
                    row.put("expUnitMeasure", "");
                }
                if (specfs.getTotalInvOpeningBalUnits() != null) {
                    row.put("expNoOfOpeningBalUnits", specfs.getTotalInvOpeningBalUnits());
                } else {
                    row.put("expNoOfOpeningBalUnits", "0");
                }
                if (specfs.getTotalInvOpeningBalRate() != null) {
                    row.put("expRateOpeningBalUnits", specfs.getTotalInvOpeningBalRate());
                } else {
                    row.put("expRateOpeningBalUnits", "0");
                }
                if (specfs.getTotalInvOpeningBalance() != null) {
                    row.put("expOpeningBal", specfs.getTotalInvOpeningBalance());
                } else {
                    row.put("expOpeningBal", "0");
                }

                row.put("invoiceItemDescription1",
                        specfs.getInvoiceItemDescription1() == null ? "" : specfs.getInvoiceItemDescription1());
                row.put("invoiceItemDescription2",
                        specfs.getInvoiceItemDescription2() == null ? "" : specfs.getInvoiceItemDescription2());
                row.put("itemBarcodeNo", specfs.getBarcode() == null ? "" : specfs.getBarcode());
                row.put("itemInvoiceDesc1Check",
                        specfs.getIsInvoiceDescription1() == null ? 0 : specfs.getIsInvoiceDescription1());
                row.put("itemPriceInclusive", specfs.getIsPriceInclusive() == null ? 0 : specfs.getIsPriceInclusive());
                row.put("itemInvoiceDesc2Check",
                        specfs.getIsInvoiceDescription2() == null ? 0 : specfs.getIsInvoiceDescription2());
                row.put("isTranEditableCheck",
                        specfs.getIsTransactionEditable() == null ? 0 : specfs.getIsTransactionEditable());
                row.put("GstItemDesc", specfs.getGstDesc() == null ? "" : specfs.getGstDesc());
                // get GST specifics data
                if (specfs.getGstItemCode() != null) {
                    row.put("GSTItemCode", specfs.getGstItemCode());
                } else {
                    row.put("GSTItemCode", "");
                }
                if (specfs.getTaxApplicableDate() != null) {
                    row.put("taxApplicableDate", IdosConstants.IDOSDTF.format(specfs.getTaxApplicableDate()));
                } else {
                    row.put("taxApplicableDate", "");
                }

                if (specfs.getGstTaxRate() != null) {
                    row.put("GSTTaxRate", specfs.getGstTaxRate());
                } else {
                    row.put("GSTTaxRate", "");
                }
                if (specfs.getCessTaxRate() != null) {
                    row.put("cessTaxRate", specfs.getCessTaxRate());
                } else {
                    row.put("cessTaxRate", "");
                }

                if (specfs.getGstTaxRateSelected() != null) {
                    row.put("gstTaxRateSelected", specfs.getGstTaxRateSelected());
                } else {
                    row.put("gstTaxRateSelected", "");
                }

                if (specfs.getCessTaxRateSelected() != null) {
                    row.put("cessTaxRateSelected", specfs.getCessTaxRateSelected());
                } else {
                    row.put("cessTaxRateSelected", "");
                }

                if (specfs.getGstItemCategory() != null) {
                    row.put("GstItemCategory", specfs.getGstItemCategory());
                } else {
                    row.put("GstItemCategory", "");
                }
                if (specfs.getGstTypeOfSupply() != null) {
                    String typeOfSupply = specfs.getGstTypeOfSupply();
                    int gstTypeOfGoodsOrSerInt = 0;
                    if (typeOfSupply.equalsIgnoreCase(IdosConstants.GST_GOODS_TEXT)) {
                        gstTypeOfGoodsOrSerInt = IdosConstants.GST_GOODS;
                    } else if (typeOfSupply.equalsIgnoreCase(IdosConstants.GST_SERVICES_TEXT)) {
                        gstTypeOfGoodsOrSerInt = IdosConstants.GST_SERVICES;
                    }
                    row.put("GSTtypeOfSupply", Integer.toString(gstTypeOfGoodsOrSerInt));
                } else {
                    row.put("GSTtypeOfSupply", "");
                }
                if (specfs.getGstTypeOfSupply() != null
                        && specfs.getGstTypeOfSupply().equalsIgnoreCase(IdosConstants.GST_SERVICES_TEXT)) {
                    ArrayList inparamList = new ArrayList(1);
                    inparamList.add(specfs.getGstItemCode());
                    List<GSTServicesCodes> gstCodes = genericDAO.queryWithParams(GST_SERVICE_CODE_DESC_JQL,
                            entityManager, inparamList);
                    /*
                     * if (gstCodes != null && gstCodes.size() > 0) {
                     * GSTServicesCodes gstcode = gstCodes.get(0);
                     * row.put("GstItemDesc", gstcode.getServiceDescription());
                     * } else {
                     * row.put("GstItemDesc", "");
                     * }
                     */
                } else {
                    ArrayList inparamList = new ArrayList(1);
                    inparamList.add(specfs.getGstItemCode());
                    List<GSTGoodsCodes> gstCodes = genericDAO.queryWithParams(GST_GOODS_CODE_DESC_JQL, entityManager,
                            inparamList);
                    /*
                     * if(gstCodes!= null && gstCodes.size() > 0 ){
                     * GSTGoodsCodes gstcode = gstCodes.get(0);
                     * //row.put("GstItemDesc",gstcode.getGoodsDescription());
                     * }else{
                     * ///row.put("GstItemDesc","");
                     * }
                     */
                }
                String expenseSpecfWitholdingApplicable = "";
                if (specfs.getIsWithholdingApplicable() != null) {
                    row.put("isWithholdingApplicable", specfs.getIsWithholdingApplicable());
                } else {
                    row.put("isWithholdingApplicable", expenseSpecfWitholdingApplicable);
                }
                String expenseSpecfWitholdingType = "";
                if (specfs.getIsWithholdingApplicable() != null) {
                    row.put("withHoldingType", specfs.getWithholdingType());
                } else {
                    row.put("withHoldingType", expenseSpecfWitholdingType);
                }
                String captureInputTaxes = "";
                if (specfs.getIsCaptureInputTaxes() != null) {
                    row.put("captureInputTaxes", specfs.getIsCaptureInputTaxes());
                } else {
                    row.put("captureInputTaxes", captureInputTaxes);
                }
                String expenseSpecfWitholdingRate = "";
                if (specfs.getWithHoldingRate() != null) {
                    row.put("withholdingRate", specfs.getWithHoldingRate());
                } else {
                    row.put("withholdingRate", expenseSpecfWitholdingRate);
                }
                String expenseSpecfWitholdingLimit = "";
                if (specfs.getWithHoldingLimit() != null) {
                    row.put("withholdingLimit", specfs.getWithHoldingLimit());
                } else {
                    row.put("withholdingLimit", expenseSpecfWitholdingLimit);
                }
                if (specfs.getWithholdingMonetoryLimit() != null) {
                    row.put("withholdingMonetoryLimit", specfs.getWithholdingMonetoryLimit());
                } else {
                    row.put("withholdingMonetoryLimit", "");
                }
                if (specfs.getIsFixedAsset() != null) {
                    row.put("isFixedAssetsSelectValue", specfs.getIsFixedAsset().toString());
                } else {
                    row.put("isFixedAssetsSelectValue", "");
                }
                if (specfs.getCapitalizaAmount() != null) {
                    row.put("isFixedAssetsCapitalizaAmountInput", decimalFormat.format(specfs.getCapitalizaAmount()));
                } else {
                    row.put("isFixedAssetsCapitalizaAmountInput", "");
                }
                if (specfs.getThresholdLimit() != null) {
                    row.put("isFixedAssetsThresholdLimitInput", decimalFormat.format(specfs.getThresholdLimit()));
                } else {
                    row.put("isFixedAssetsThresholdLimitInput", "");
                }
                if (specfs.getCapitalizaLifeSpan() != null) {
                    row.put("isFixedAssetsLifeSpanInput", decimalFormat.format(specfs.getCapitalizaLifeSpan()));
                } else {
                    row.put("isFixedAssetsLifeSpanInput", "");
                }
                if (specfs.getIsMovableImmovableAsset() != null) {
                    row.put("isMovableImmovableSelectValue", specfs.getIsMovableImmovableAsset().toString());
                } else {
                    row.put("isMovableImmovableSelectValue", "");
                }
                if (specfs.getIsTaggableAsset() != null) {
                    row.put("isTaggableSelectValue", specfs.getIsTaggableAsset().toString());
                } else {
                    row.put("isTaggableSelectValue", "");
                }
                if (specfs.getIsEligibleInputTaxCredit() != null) {
                    row.put("isEligibleInputTaxCredit", specfs.getIsEligibleInputTaxCredit().toString());
                } else {
                    row.put("isEligibleInputTaxCredit", "");
                }
                if (specfs.getIsTdsVendorSpecific() != null) {
                    row.put("isTDsVendSpecfic", specfs.getIsTdsVendorSpecific());
                } else {
                    row.put("isTDsVendSpecfic", 0);
                }

                if (specfs.getIsCompositionScheme() != null) {
                    row.put("isCompositionItem", specfs.getIsCompositionScheme());
                } else {
                    row.put("isCompositionItem", 0);
                }
                String taggableCode = "";
                if (specfs.getTaggableCode() != null && !specfs.getTaggableCode().equals("")) {
                    taggableCode = specfs.getTaggableCode();
                }
                row.put("taggableCode", taggableCode);
                List<SpecificTaxHistory> findSpecificHistory = SpecificTaxHistory.findSpecificHistory(entityManager,
                        specfs.getOrganization().getId(), specfs.getId());
                String taxHistoryList = "";
                for (SpecificTaxHistory specificHistory : findSpecificHistory) {
                    if (specificHistory.getApplicableDate() != null) {
                        StringBuilder history = new StringBuilder();
                        history.append(IdosConstants.IDOSDF.format(specificHistory.getApplicableDate()));
                        if (specificHistory.getTaxRate() != null) {
                            history.append("-(Tax Rate :").append(specificHistory.getTaxRate()).append(" %)");
                        }
                        if (specificHistory.getCessRate() != null) {
                            history.append("-(Cess Rate :").append(specificHistory.getCessRate()).append(" %)");
                        }
                        taxHistoryList += history.toString() + "|";
                    }
                }
                row.put("taxHistoryList", taxHistoryList);
                Boolean validate = SpecificTaxHistory.validate(specfs.getOrganization().getId(), specfs.getId(),
                        specfs.getGstTaxRate(), specfs.getCessTaxRate());
                row.put("currentTaxRulesAvailable", validate);
                List<VendorTDSTaxes> tdsHistory = VendorTDSTaxes.findSpecificTdsHistory(entityManager,
                        specfs.getOrganization().getId(), specfs.getId());
                String tdsHistoryList = "";
                if (tdsHistory != null) {
                    for (VendorTDSTaxes tdsObj : tdsHistory) {
                        if (tdsObj.getTaxRate() != null) {
                            StringBuilder history = new StringBuilder();
                            if (tdsObj.getTaxRate() != null) {
                                history.append("(TDS Rate :").append(tdsObj.getTaxRate()).append(" %)");
                            }
                            if (tdsObj.getFromDate() != null) {
                                history.append("-(from ").append(IdosConstants.IDOSDF.format(tdsObj.getFromDate()));
                                history.append(" to ");
                            }
                            if (tdsObj.getToDate() != null) {
                                history.append(IdosConstants.IDOSDF.format(tdsObj.getToDate()));
                                history.append(")");
                            }
                            tdsHistoryList += history.toString() + "|";
                        }
                    }
                }
                row.put("tdsHistoryList", tdsHistoryList);
                // retrieve item warehouse stock reorder level values start
                List<WarehouseItemStockReorderLevel> itemWarehouseStockReorderLevelList = specfs
                        .getItemWarehouseStockReorderLevels();
                String branchIds = "", alertUserEmails = "", reorderLevels = "";
                if (itemWarehouseStockReorderLevelList != null && !itemWarehouseStockReorderLevelList.isEmpty()
                        && itemWarehouseStockReorderLevelList.size() > 0) {
                    for (WarehouseItemStockReorderLevel itemReorderLevels : itemWarehouseStockReorderLevelList) {
                        branchIds += itemReorderLevels.getBranch().getId() + ",";
                        if (itemReorderLevels.getReorderLevelAlertUser() != null) {
                            alertUserEmails += itemReorderLevels.getReorderLevelAlertUser() + ",";
                        } else {
                            alertUserEmails += "" + ",";
                        }
                        if (itemReorderLevels.getReorderLevel() != null) {
                            reorderLevels += itemReorderLevels.getReorderLevel() + ",";
                        } else {
                            reorderLevels += "0" + ",";
                        }
                    }
                    row.put("reorderlevelbranchIds", branchIds.substring(0, branchIds.length() - 1));
                    row.put("reorderlevelalertUserEmails", alertUserEmails.substring(0, alertUserEmails.length() - 1));
                    row.put("reorderlevelreorderLevels", reorderLevels.substring(0, reorderLevels.length() - 1));
                } else {
                    row.put("reorderlevelbranchIds", branchIds);
                    row.put("reorderlevelalertUserEmails", alertUserEmails);
                    row.put("reorderlevelreorderLevels", reorderLevels);
                }

                // retrieve item warehouse stock reorder level values end
                // List<BranchSpecifics> bnchSpecifics = specfs.getSpecificsBranch();
                List<BranchSpecifics> bnchSpecifics = BranchSpecifics.findBranchBySpecific(entityManager,
                        specfs.getOrganization().getId(), specfs.getId());

                if (bnchSpecifics.size() > 0) {
                    String itemBranches = "", itemBranchesPresentStatus = "", itemValues = "",
                            branchOpeningBalance = "";
                    String branchInvOpeningBalance = "", branchInvUnits = "", branchInvRate = "";
                    for (BranchSpecifics bnchSpecf : bnchSpecifics) {
                        if (bnchSpecf.getPresentStatus() == 0 || bnchSpecf.getBranch().getPresentStatus() == 0)
                            continue;

                        itemBranches += bnchSpecf.getBranch().getId() + ",";
                        itemBranchesPresentStatus += bnchSpecf.getPresentStatus() + ",";
                        if (null != bnchSpecf.getWalkinCustomerMaxDiscount()) {
                            itemValues += bnchSpecf.getWalkinCustomerMaxDiscount() + ",";
                        }
                        if (bnchSpecf.getOpeningBalance() != null) {
                            branchOpeningBalance += bnchSpecf.getOpeningBalance() + ",";
                        }
                        if (bnchSpecf.getInvOpeningBalance() != null) {
                            branchInvOpeningBalance += bnchSpecf.getInvOpeningBalance() + ",";
                        }
                        if (bnchSpecf.getInvOpeningBalUnits() != null) {
                            branchInvUnits += bnchSpecf.getInvOpeningBalUnits() + ",";
                        }
                        if (bnchSpecf.getInvOpeningBalRate() != null) {
                            branchInvRate += bnchSpecf.getInvOpeningBalRate() + ",";
                        }
                    }
                    if (itemBranches.length() > 0) {
                        row.put("itemBranches", itemBranches.substring(0, itemBranches.length() - 1));
                    } else {
                        row.put("itemBranches", itemBranches);
                    }
                    if (itemBranchesPresentStatus.length() > 0) {
                        row.put("itemBranchesPresentStatus",
                                itemBranchesPresentStatus.substring(0, itemBranchesPresentStatus.length() - 1));
                    } else {
                        row.put("itemBranchesPresentStatus", itemBranchesPresentStatus);
                    }
                    if (!"".equals(itemValues)) {
                        row.put("walkinCustDiscount", itemValues.substring(0, itemValues.length() - 1));
                    } else {
                        row.put("walkinCustDiscount", "");
                    }
                    if (!"".equals(branchOpeningBalance)) {
                        row.put("branchOpeningBalance",
                                branchOpeningBalance.substring(0, branchOpeningBalance.length() - 1));
                    } else {
                        row.put("branchOpeningBalance", "");
                    }
                    if (!"".equals(branchInvOpeningBalance)) {
                        row.put("branchInvOpeningBalance",
                                branchInvOpeningBalance.substring(0, branchInvOpeningBalance.length() - 1));
                    } else {
                        row.put("branchInvOpeningBalance", "");
                    }
                    if (!"".equals(branchInvUnits)) {
                        row.put("branchInvUnits", branchInvUnits.substring(0, branchInvUnits.length() - 1));
                    } else {
                        row.put("branchInvUnits", "");
                    }
                    if (!"".equals(branchInvRate)) {
                        row.put("branchInvRate", branchInvRate.substring(0, branchInvRate.length() - 1));
                    } else {
                        row.put("branchInvRate", "");
                    }
                } else {
                    row.put("itemBranches", "");
                    row.put("itemBranchesPresentStatus", "");
                    row.put("walkinCustDiscount", "");
                    row.put("branchOpeningBalance", "");
                    row.put("branchInvOpeningBalance", "");
                    row.put("branchInvUnits", "");
                    row.put("branchInvRate", "");
                }
                // List<SpecificsTransactionPurpose> specfTxnPurpose =
                // specfs.getSpecificsTransactionPurposes();
                List<SpecificsTransactionPurpose> specfTxnPurpose = SpecificsTransactionPurpose
                        .findSpecificsTransactionPurpose(entityManager, specfs.getOrganization().getId(),
                                specfs.getId());
                if (specfTxnPurpose.size() > 0) {
                    String specfTxnPurposes = "";
                    for (SpecificsTransactionPurpose specfTxnpurp : specfTxnPurpose) {
                        if (specfTxnpurp.getTransactionPurpose() != null)
                            specfTxnPurposes += specfTxnpurp.getTransactionPurpose().getId() + ",";
                    }
                    row.put("specfTxnPurpose", specfTxnPurposes.substring(0, specfTxnPurposes.length() - 1));
                } else {
                    row.put("specfTxnPurpose", "");
                }
                if (specfs.getIncomeOrExpenseType() != null) {
                    row.put("incomeExpense", specfs.getIncomeOrExpenseType());
                } else {
                    row.put("incomeExpense", "");
                }
                if (specfs.getLinkIncomeExpenseSpecifics() != null) {
                    row.put("linkincomeExpense", specfs.getLinkIncomeExpenseSpecifics().getId());
                } else {
                    row.put("linkincomeExpense", "");
                }
                if (specfs.getNoOfExpenseUnits() != null) {
                    row.put("noOfExpUnit", specfs.getNoOfExpenseUnits());
                } else {
                    row.put("noOfExpUnit", "");
                }
                if (specfs.getNoOfIncomeUnits() != null) {
                    row.put("noOfIncUnit", specfs.getNoOfIncomeUnits());
                } else {
                    row.put("noOfIncUnit", "");
                }
                if (specfs.getIncomeUnitsMeasure() != null) {
                    row.put("incUnitMeasure", specfs.getIncomeUnitsMeasure());
                } else {
                    row.put("incUnitMeasure", "");
                }
                if (specfs.getTradingInventoryCalcMethod() != null) {
                    row.put("tradingInvCalcMethod", specfs.getTradingInventoryCalcMethod());
                } else {
                    row.put("tradingInvCalcMethod", "");
                }
                // Start Add by Sunil for mapping and openingBalance
                if (specfs.getIdentificationForDataValid() != null) {
                    row.put("identificationForDataValid", specfs.getIdentificationForDataValid());
                } else {
                    row.put("identificationForDataValid", "");
                }

                //if ((specfs.getIdentificationForDataValid() != null)
                //        && (specfs.getIdentificationForDataValid().equals("4")
                //                || specfs.getIdentificationForDataValid().equals("5"))) {
                //    // branch bank accounts
                //    ArrayNode branchBankAccountan = results.putArray("branchBankActData");
                //    BranchBankAccountMapping branchBankAccountMapping = BranchBankAccountMapping
                //            .findBySpecific(entityManager, specfs);
                //    if (branchBankAccountMapping != null) {
                //        ObjectNode bnchbankActrows = Json.newObject();
                //        BranchBankAccounts bnchBnkAct = branchBankAccountMapping.getBranchBankAccounts();

                //        bnchbankActrows.put("branchBankAccounId", bnchBnkAct.getId());
                //        bnchbankActrows.put("branchBankAccountBankName",
                //                bnchBnkAct.getBankName() == null ? "" : bnchBnkAct.getBankName());
                //        bnchbankActrows.put("branchBankAccountType",
                //                bnchBnkAct.getAccountType() == null ? "" : bnchBnkAct.getAccountType().toString());
                //        bnchbankActrows.put("branchBankAccountNumber",
                //                bnchBnkAct.getAccountNumber() == null ? "" : bnchBnkAct.getAccountNumber());
                //        bnchbankActrows.put("branchBankAccountOpeningBalance", IdosConstants.decimalFormat
                //                .format(bnchBnkAct.getOpeningBalance() == null ? 0.0 : bnchBnkAct.getOpeningBalance()));
                //        bnchbankActrows.put("branchBankAccounttAuthSignName",
                //                bnchBnkAct.getAuthorizedSignatoryName() == null ? ""
                //                        : bnchBnkAct.getAuthorizedSignatoryName());
                //        bnchbankActrows.put("branchBankAccounttAuthSignEmail",
                //                bnchBnkAct.getAuthorizedSignatoryEmail() == null ? ""
                //                        : bnchBnkAct.getAuthorizedSignatoryEmail());
                //        bnchbankActrows.put("branchBankAccounttAddress",
                //                bnchBnkAct.getBankAddress() == null ? "" : bnchBnkAct.getBankAddress());
                //        bnchbankActrows.put("branchBankAccounttPhnNoCtryCode",
                //                bnchBnkAct.getBankNumberPhnCtryCode() == null ? ""
                //                        : bnchBnkAct.getBankNumberPhnCtryCode());
                //        if (bnchBnkAct.getPhoneNumber() != "" && bnchBnkAct.getPhoneNumber() != null) {
                //            int k = bnchBnkAct.getPhoneNumber().indexOf("-");
                //            bnchbankActrows.put("branchBankAccounttPhnNo",
                //                    bnchBnkAct.getPhoneNumber().substring(k + 1, bnchBnkAct.getPhoneNumber().length()));
                //        } else {
                //            bnchbankActrows.put("branchBankAccounttPhnNo", bnchBnkAct.getPhoneNumber());
                //        }
                //        bnchbankActrows.put("branchBankAccountSwiftCode",
                //                bnchBnkAct.getSwiftCode() == null ? "" : bnchBnkAct.getSwiftCode());
                //        bnchbankActrows.put("branchBankAccountIfscCode",
                //                bnchBnkAct.getIfscCode() == null ? "" : bnchBnkAct.getIfscCode());
                //        bnchbankActrows.put("branchBankAccountRoutingNumber",
                //                bnchBnkAct.getRoutingNumber() == null ? "" : bnchBnkAct.getRoutingNumber());
                //        branchBankAccountan.add(bnchbankActrows);
                //        bnchbankActrows.put("branchBankAccountIfscCode",
                //                bnchBnkAct.getIfscCode() == null ? "" : bnchBnkAct.getIfscCode());
                //    }
                //}

                if (specfs.getIdentificationForDataValidPLorBS() != null) {
                    row.put("identificationForDataValidPLorBS", specfs.getIdentificationForDataValidPLorBS());
                } else {
                    row.put("identificationForDataValidPLorBS", "");
                }

                if (specfs.getTotalOpeningBalance() != null) {
                    row.put("openingBalance", IdosConstants.decimalFormat.format(specfs.getTotalOpeningBalance()));
                } else {
                    row.put("openingBalance", "");
                }
                // end added by Sunil for mapping

                row.put("isSpecificUsedInTransactions", isSpecificUsedInTransactions(specfs, entityManager));
                itemdetailan.add(row);

            }
            if (specfKlList.size() > 0) {
                for (SpecificsKnowledgeLibrary specKl : specfKlList) {
                    ObjectNode row = Json.newObject();
                    row.put("klPrimKeyId", specKl.getId());
                    row.put("klContent", specKl.getKnowledgeLibraryContent());
                    if (specKl.getIsMandatory() != null && specKl.getIsMandatory() == 1) {
                        row.put("mandatory", "Yes");
                    } else {
                        row.put("mandatory", "No");
                    }
                    List<SpecificsKnowledgeLibraryForBranch> klForBranchesList = SpecificsKnowledgeLibraryForBranch
                            .findBranchBySpecific(entityManager, specfs.getOrganization().getId(), specfs.getId());
                    if (klForBranchesList.size() > 0) {
                        String klisforbnch = "";
                        for (SpecificsKnowledgeLibraryForBranch specfKlBnch : klForBranchesList) {
                            klisforbnch += specfKlBnch.getBranch().getId() + ",";
                        }
                        row.put("klforbnch", klisforbnch.substring(0, klisforbnch.length() - 1));
                    } else {
                        row.put("klforbnch", "");
                    }
                    itemKnowledgeLibraryan.add(row);
                }
            }
            List<SpecificsDocUploadMonetoryRuleForBranch> oldspecificsDocUploadMonLimitRuleForBranches = SpecificsDocUploadMonetoryRuleForBranch
                    .findBranchBySpecific(entityManager, specfs.getOrganization().getId(), specfs.getId());
            String specItemIndividualBranches = "";
            String specItemIndividualBranchesMonetoryLimit = "";
            if (oldspecificsDocUploadMonLimitRuleForBranches.size() > 0) {
                ObjectNode row = Json.newObject();
                for (SpecificsDocUploadMonetoryRuleForBranch specfBnchsDocRule : oldspecificsDocUploadMonLimitRuleForBranches) {

                    if (specfBnchsDocRule.getBranch() != null) {
                        specItemIndividualBranches += specfBnchsDocRule.getBranch().getId() + ",";
                    }
                    if (specfBnchsDocRule.getMonetoryLimit() != null) {
                        specItemIndividualBranchesMonetoryLimit += specfBnchsDocRule.getMonetoryLimit() + ",";
                    } else {
                        specItemIndividualBranchesMonetoryLimit += 0.0 + ",";
                    }
                }
                row.put("specItemIndividualBranches",
                        specItemIndividualBranches.substring(0, specItemIndividualBranches.length() - 1));
                row.put("specItemIndividualBranchesMonetoryLimit", specItemIndividualBranchesMonetoryLimit.substring(0,
                        specItemIndividualBranchesMonetoryLimit.length() - 1));
                itemDocUploadRulean.add(row);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "ShowItemsDetails Email", "ShowItemsDetails Organization",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + results);
        return Results.ok(results).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result getItemKnowledgelibrary(Request request) {
        log.log(Level.FINE, ">>>> Start");
        ObjectNode result = Json.newObject();
        try {
            JsonNode json = request.body().asJson();
            ArrayNode itemklan = result.putArray("itemkLibrary");
            String itemId = json.findValue("specfId").asText();
            Specifics specifics = Specifics.findById(IdosUtil.convertStringToLong(itemId));
            if (specifics != null) {
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "GetItemKnowledgelibrary Email",
                    "GetItemKnowledgelibrary Organization", Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result getChildCOA(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        ObjectNode results = Json.newObject();
        Users user = null;
        try {
            JsonNode json = request.body().asJson();
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, ">>>> Start " + json);
            // String email = json.findValue("usermail").asText();
            // session.adding("email", email);
            user = getUserInfo(request);
            if (user == null) {
                log.log(Level.SEVERE, "unauthorized access");
                return unauthorized();
            }
            SPECIFICS_SERVICE.getChildCOA(json, user, entityManager, results);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> end " + results);
        return Results.ok(results).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result getParticularsForOrg(Request request) {
        ObjectNode results = Json.newObject();
        Users usrinfo = null;
        try {
            ArrayNode partan = results.putArray("partData");
            usrinfo = getUserInfo(request);
            List<Particulars> catList = Particulars.list(entityManager, usrinfo.getOrganization());
            if (catList.size() > 0) {
                for (Particulars part : catList) {
                    ObjectNode row = Json.newObject();
                    row.put("id", part.getId());
                    row.put("name", part.getName());
                    row.put("accountCode", part.getAccountCode());
                    partan.add(row);
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, usrinfo.getEmail(), usrinfo.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + results);
        return Results.ok(results).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result getBranchIncomesCoa(Http.Request request) {

        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            JsonNode json = request.body().asJson();
            ArrayNode branchIncomecoaChildDataan = result.putArray("branchIncomecoaChildData");
            String email = json.findValue("usermail").asText();
            String branchId = json.findValue("branchId").asText();
            session.adding("email", email);
            user = getUserInfo(request);
            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("accountCode", 1000000000000000000L);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            Particulars parts = genericDAO.getByCriteria(Particulars.class, criterias, entityManager);
            criterias.clear();
            criterias.put("particular.id", parts.getId());
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("branch.id", IdosUtil.convertStringToLong(branchId));
            criterias.put("presentStatus", 1);
            List<BranchSpecifics> branchSpecificsList = genericDAO.findByCriteria(BranchSpecifics.class, criterias,
                    entityManager);
            if (branchSpecificsList.size() > 0) {
                for (BranchSpecifics bnchspecf : branchSpecificsList) {
                    ObjectNode row = Json.newObject();
                    row.put("id", bnchspecf.getSpecifics().getId());
                    row.put("name", bnchspecf.getSpecifics().getName());
                    branchIncomecoaChildDataan.add(row);
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
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result getAllChartOfAccounts(Request request) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> Start");
        ObjectNode result = Json.newObject();
        ArrayNode branchan = result.putArray("allCoaData");
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

            List<Specifics> orgSpecificsList = user.getOrganization().getSpecifics();
            /*
             * ObjectMapper mapper = new ObjectMapper();
             * mapper.enableDefaultTyping();
             * String str = mapper.writeValueAsString(orgSpecificsList);
             * branchan.add(str);
             */
            // StringBuilder sb = new StringBuilder();
            for (Specifics specifics : orgSpecificsList) {
                if (specifics.getPresentStatus() == 0) {
                    continue;
                }
                ObjectNode row = Json.newObject();
                row.put("id", specifics.getId());
                row.put("name", specifics.getName());
                row.put("accountCode", specifics.getParticularsId().getAccountCode());
                branchan.add(row);
                /*
                 * sb.append("{").append("id").append(":").append(specifics.getId()).append(",")
                 * ;
                 * sb.append("name").append(":").append(specifics.getName()).append(",");
                 * sb.append("accountCode").append(":").append(specifics.getParticularsId().
                 * getAccountCode()).append("},");
                 */
            }
            // branchan.add(sb.toString());
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
    public Result getAllChartOfAccountsLRUCache(Request request) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> Start");
        ObjectNode result = Json.newObject();
        ArrayNode branchan = result.putArray("allCoaData");
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
            List<Long> coaList = new ArrayList<>();
            // if(cacheOrgCoaMap == null || cacheOrgCoaMap.isEmpty()){
            List<Specifics> orgSpecificsList = user.getOrganization().getSpecifics();
            for (Specifics specifics : orgSpecificsList) {
                if (specifics.getPresentStatus() == 0) {
                    continue;
                }
                // cacheCoa.put(specifics.getId(), specifics.getName());
                coaList.add(specifics.getId());
                ObjectNode row = Json.newObject();
                row.put("id", specifics.getId());
                row.put("name", specifics.getName());
                branchan.add(row);
            }
            // UserSetupCache.addCratorCOA(user.getId(), coaList);
            /*
             * cacheOrgCoaMap.set(user.getOrganization().getId(), cacheCoa);
             * }else{
             * cacheCoa = cacheOrgCoaMap.find(user.getOrganization().getId());
             * if(cacheCoa == null || cacheCoa.isEmpty()){
             * List<Specifics> orgSpecificsList = user.getOrganization().getSpecifics();
             * for(Specifics specifics: orgSpecificsList){
             * if(specifics.getPresentStatus() == 0){
             * continue;
             * }
             * cacheCoa.set(specifics.getId(), specifics.getName());
             * ObjectNode row = Json.newObject();
             * row.put("id", specifics.getId());
             * row.put("name", specifics.getName());
             * branchan.add(row);
             * }
             * cacheOrgCoaMap.set(user.getOrganization().getId(), cacheCoa);
             * }else{
             * Set<Long> keys = cacheCoa.keySet();
             * for(Long k:keys){
             * ObjectNode row = Json.newObject();
             * row.put("id", k);
             * row.put("name", cacheCoa.find(k));
             * branchan.add(row);
             * }
             * }
             * }
             */
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
    public Result getAllChartOfAccountsLRUCacheStr(Request request) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> Start");
        ObjectNode result = Json.newObject();
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
            Long inParticularId = null;
            Long exParticularId = null;
            Long asParticularId = null;
            Long liParticularId = null;
            List<Particulars> particulars = Particulars.list(entityManager, user.getOrganization());
            for (Particulars particular : particulars) {
                if (particular.getAccountCode().equals(1000000000000000000L)) {
                    inParticularId = particular.getId();
                } else if (particular.getAccountCode().equals(2000000000000000000L)) {
                    exParticularId = particular.getId();
                } else if (particular.getAccountCode().equals(3000000000000000000L)) {
                    asParticularId = particular.getId();
                } else if (particular.getAccountCode().equals(4000000000000000000L)) {
                    liParticularId = particular.getId();
                }
            }
            StringBuilder income = new StringBuilder();
            StringBuilder expense = new StringBuilder();
            StringBuilder asset = new StringBuilder();
            StringBuilder liabilities = new StringBuilder();
            // if(cacheOrgCoaMap == null || cacheOrgCoaMap.isEmpty()){
            List<Specifics> orgSpecificsList = user.getOrganization().getSpecifics();
            for (Specifics specifics : orgSpecificsList) {
                if (specifics.getPresentStatus() == 0) {
                    continue;
                }
                // cacheCoa.put(specifics.getId(), specifics.getName());
                // ObjectNode row = Json.newObject();
                // row.put("id", specifics.getId());
                // row.put("name", specifics.getName());
                if (specifics.getParticularsId().getId() == inParticularId) {
                    income.append(specifics.getId()).append(specifics.getName());
                } else if (specifics.getParticularsId().getId() == exParticularId) {
                    expense.append(specifics.getId()).append(specifics.getName());
                } else if (specifics.getParticularsId().getId() == asParticularId) {
                    asset.append(specifics.getId()).append(specifics.getName());
                } else if (specifics.getParticularsId().getId() == liParticularId) {
                    liabilities.append(specifics.getId()).append(specifics.getName());
                }
            }
            result.put("income", income.toString());
            result.put("expense", expense.toString());
            result.put("asset", asset.toString());
            result.put("liabilities", liabilities.toString());
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        // if(log.isLoggable(Level.FINE)) log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result getAllChartOfAccountsLRUCache2(Request request) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> Start");
        ObjectNode result = Json.newObject();
        ArrayNode inCoaData = result.putArray("inCoaData");
        ArrayNode exCoaData = result.putArray("exCoaData");
        ArrayNode asCoaData = result.putArray("asCoaData");
        ArrayNode liCoaData = result.putArray("liCoaData");
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
            Long inParticularId = null;
            Long exParticularId = null;
            Long asParticularId = null;
            Long liParticularId = null;
            List<Particulars> particulars = Particulars.list(entityManager, user.getOrganization());
            for (Particulars particular : particulars) {
                if (particular.getAccountCode().equals(1000000000000000000L)) {
                    inParticularId = particular.getId();
                } else if (particular.getAccountCode().equals(2000000000000000000L)) {
                    exParticularId = particular.getId();
                } else if (particular.getAccountCode().equals(3000000000000000000L)) {
                    asParticularId = particular.getId();
                } else if (particular.getAccountCode().equals(4000000000000000000L)) {
                    liParticularId = particular.getId();
                }
            }

            // if(cacheOrgCoaMap == null || cacheOrgCoaMap.isEmpty()){
            List<Specifics> orgSpecificsList = user.getOrganization().getSpecifics();
            for (Specifics specifics : orgSpecificsList) {
                if (specifics.getPresentStatus() == 0) {
                    continue;
                }
                // cacheCoa.put(specifics.getId(), specifics.getName());
                ObjectNode row = Json.newObject();
                row.put("id", specifics.getId());
                row.put("name", specifics.getName());
                if (specifics.getParticularsId().getId() == inParticularId) {
                    inCoaData.add(row);
                } else if (specifics.getParticularsId().getId() == exParticularId) {
                    exCoaData.add(row);
                } else if (specifics.getParticularsId().getId() == asParticularId) {
                    asCoaData.add(row);
                } else if (specifics.getParticularsId().getId() == liParticularId) {
                    liCoaData.add(row);
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
        // if(log.isLoggable(Level.FINE)) log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result searchCoa(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            JsonNode json = request.body().asJson();
            ArrayNode coaParaList = result.putArray("coaParaListData");
            String email = json.findValue("usermail").asText();
            Map<String, Object> criterias = new HashMap<String, Object>();
            session.adding("email", email);
            user = getUserInfo(request);
            String enteredCoaValue = json.findValue("freeTextSearchCoaVal").asText();
            String newsbquery = ("select obj from Specifics obj WHERE obj.organization.id  = ?1 and obj.name like ?2 and obj.presentStatus=1");
            ArrayList inparam = new ArrayList(2);
            inparam.add(user.getOrganization().getId());
            inparam.add("%" + enteredCoaValue + "%");

            List<Specifics> specificsList = genericDAO.queryWithParams(newsbquery, entityManager, inparam);
            if (specificsList.size() > 0) {
                for (Specifics specf : specificsList) {
                    ObjectNode row = Json.newObject();
                    String para = "";
                    if (specf.getAccountCodeHirarchy() != null) {
                        String exceptStartEndSleash = specf.getAccountCodeHirarchy().substring(1,
                                specf.getAccountCodeHirarchy().length() - 1);
                        String[] accCodeArray = exceptStartEndSleash.split("/");
                        for (int i = 0; i < accCodeArray.length; i++) {
                            Long actCode = Long.valueOf(accCodeArray[i]);
                            if (i == 0) {
                                criterias.clear();
                                criterias.put("accountCode", actCode);
                                criterias.put("organization.id", user.getOrganization().getId());
                                criterias.put("presentStatus", 1);
                                Particulars foundParticular = genericDAO.getByCriteria(Particulars.class, criterias,
                                        entityManager);
                                if (foundParticular != null) {
                                    para += foundParticular.getName();
                                }
                            }
                            if (i > 0) {
                                String sbquery = ("select obj from Specifics obj WHERE obj.organization.id  = ?1 and obj.accountCode = ?2 and obj.presentStatus=1");
                                inparam.clear();
                                inparam.add(user.getOrganization().getId());
                                inparam.add(actCode);
                                List<Specifics> foundSpecific = genericDAO.queryWithParams(sbquery, entityManager,
                                        inparam);
                                if (foundSpecific.size() > 0) {
                                    para += " ----> " + foundSpecific.get(0).getName();
                                }
                            }
                        }
                        para += " ----> " + specf.getName();
                    }
                    row.put("para", para);
                    coaParaList.add(row);
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
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result downloadOrgChartOfAccounts(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        ObjectNode results = Json.newObject();
        ArrayNode an = results.putArray("orgCOAFileCred");
        Users user = null;
        File file = null;
        try {
            JsonNode json = request.body().asJson();
            String useremail = json.findValue("useremail").asText();
            // session.adding("email", useremail);
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            String sheetName = user.getOrganization().getName().replaceAll("\\s", "") + "COA";
            String fileName = sheetName + "COA.xlsx";
            String path = application.path().toString() + "/logs/OrgChartOfAccounts/";

            File filepath = new File(path);
            if (!filepath.exists()) {
                filepath.mkdir();
            }
            path = path + fileName;

            excelService.createOrgCOAExcel(user.getOrganization(), entityManager, path, sheetName);
            // ObjectNode row = Json.newObject();
            /// row.put("fileName", fileName);
            file = new File(path);

            // an.add(row);
            log.log(Level.FINE, ">>>> end");
            return Results.ok(file).withHeader("ContentType", "application/xlsx").withHeader("Content-Disposition",
                    "attachment; filename = " + fileName);
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
    public Result isSpecificExists(int idDataValidation, String parentaccountCode, Request request) {
        log.log(Level.FINE, ">>>> Start " + idDataValidation);
        // EntityManager entityManager = getEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users usrinfo = null;
        int idDataValidationTemp = 0;
        try {
            usrinfo = getUserInfo(request);
            idDataValidationTemp = Specifics.checkIfSpecificExists(entityManager, usrinfo.getOrganization(),
                    idDataValidation, parentaccountCode);
            if (idDataValidationTemp > 0)
                result.put("isSpecificExists", idDataValidationTemp);
            else
                result.put("isSpecificExists", 0);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, usrinfo.getEmail(), usrinfo.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result checkIfCombinationSalesIncomeItem(Request request) {
        ObjectNode results = Json.newObject();
        Users usrinfo = null;
        try {
            JsonNode json = request.body().asJson();
            ArrayNode isCombinationSalesItem = results.putArray("combinationSalesData");
            ArrayNode incSpecificsan = results.putArray("incomeSepecificsData");
            usrinfo = getUserInfo(request);
            Long coaId = json.findValue("coaId") != null ? json.findValue("coaId").asLong() : 0l;
            Specifics incomeSpecifics = Specifics.findById(coaId);
            boolean isCombSalesItem = false;

            if (incomeSpecifics != null && incomeSpecifics.getIdentificationForDataValid() != null
                    && incomeSpecifics.getIdentificationForDataValid().equalsIgnoreCase("52")) {
                isCombSalesItem = true; // when new item is entered under combination sales using createCOA
            } else if (incomeSpecifics != null && incomeSpecifics.getParentSpecifics() != null) { // this is when item
                                                                                                  // is clicked to
                                                                                                  // showEntityDetails
                Specifics parentSpecifics = Specifics.findById(incomeSpecifics.getParentSpecifics().getId());
                if (parentSpecifics != null && parentSpecifics.getIdentificationForDataValid() != null
                        && parentSpecifics.getIdentificationForDataValid().equalsIgnoreCase("52")) {
                    isCombSalesItem = true;
                }
            }
            if (isCombSalesItem) {
                ObjectNode row1 = Json.newObject();
                row1.put("isCombinationSales", "1");
                isCombinationSalesItem.add(row1);
                Users user = getUserInfo(request);
                if (user != null) {
                    // EntityManager entityManager = getEntityManager();
                    List<Specifics> itemsList = coaService.getIncomesCoaChildNodes(entityManager, user);
                    for (Specifics specf : itemsList) { // get all income child nodes which can be part of combination
                                                        // item
                        ObjectNode row = Json.newObject();
                        row.put("id", specf.getId());
                        row.put("name", specf.getName());
                        /*
                         * if(specf.getOpeningBalance()!= null){
                         * row.put("openBalUnits", specf.getOpeningBalance());
                         * }else{
                         */
                        row.put("openBalUnits", 0);
                        // }
                        if (specf.getIncomeSpecfPerUnitPrice() != null) {
                            row.put("openBalRate", specf.getIncomeSpecfPerUnitPrice());
                        } else {
                            row.put("openBalRate", 0);
                        }
                        incSpecificsan.add(row);
                    }
                } else {
                    log.log(Level.SEVERE, "Error: unauthorized access.");
                    return unauthorized();
                }
            } else {
                ObjectNode row1 = Json.newObject();
                row1.put("isCombinationSales", "0"); // false
                isCombinationSalesItem.add(row1);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, usrinfo.getEmail(), usrinfo.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(results).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result getListOfCombinationSalesItems(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            JsonNode json = request.body().asJson();
            ArrayNode combSalesList = result.putArray("combSalesListData");
            Long coaId = json.findValue("incomeSpecificsId") != null ? json.findValue("incomeSpecificsId").asLong()
                    : 0l;
            Specifics incomeSpecifics = Specifics.findById(coaId);
            user = getUserInfo(request);
            if (incomeSpecifics != null) {
                StringBuilder newsbquery = new StringBuilder();
                newsbquery.append(
                        "select obj from SpecificsCombinationSales obj WHERE obj.specificsId.id = ?1 and obj.organization.id  = ?2 and obj.presentStatus=1");
                ArrayList inparam = new ArrayList(2);
                inparam.add(coaId);
                inparam.add(user.getOrganization().getId());
                List<SpecificsCombinationSales> specificsList = genericDAO.queryWithParams(newsbquery.toString(),
                        entityManager, inparam);
                for (SpecificsCombinationSales spec : specificsList) {
                    ObjectNode row = Json.newObject();
                    row.put("specificsId", spec.getCombSpecificsId().getId());
                    row.put("itemName", spec.getCombSpecificsId().getName());
                    if (spec.getOpeningBalUnits() != null) {
                        row.put("openBalUnits", spec.getOpeningBalUnits());
                    } else {
                        row.put("openBalUnits", 0);
                    }
                    if (spec.getOpeningBalUnits() != null) {
                        row.put("openingBalRate", spec.getOpeningBalRate());
                    } else {
                        row.put("openingBalRate", 0);
                    }
                    combSalesList.add(row);
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
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public static void saveCombinationSalesItems(Specifics newSpec, JsonNode json, Users user, Request request) {
        // Applicable only for combination sales units
        // EntityManager entityManager = getEntityManager();
        Map criterias = new HashMap<String, Object>();
        criterias.put("specificsId.id", newSpec.getId());
        criterias.put("presentStatus", 1);
        List<SpecificsCombinationSales> specCombList = genericDAO.findByCriteria(SpecificsCombinationSales.class,
                criterias, entityManager);
        if (specCombList != null && specCombList.size() > 0) {
            genericDAO.deleteByCriteria(SpecificsCombinationSales.class, criterias, entityManager); // delete existing
                                                                                                    // combination sales
                                                                                                    // data if it
                                                                                                    // already exist
        }
        String combinationSalesItems = json.findPath("combinationSalesItems") != null
                ? json.findPath("combinationSalesItems").asText()
                : null;
        String combinationSalesItemsArr[] = null;
        if (combinationSalesItems != null && !combinationSalesItems.equals("")) {
            combinationSalesItemsArr = combinationSalesItems.split(",");
            String combinationSalesUnits = json.findPath("combinationSalesUnits").asText();
            String combinationSalesUnitsArr[] = combinationSalesUnits.split(",");
            String combinationSalesRates = json.findPath("combinationSalesRates").asText();
            String combinationSalesRatesArr[] = combinationSalesRates.split(",");
            for (int i = 0; i < combinationSalesItemsArr.length; i++) {
                SpecificsCombinationSales specCombSales = new SpecificsCombinationSales();
                specCombSales.setSpecificsId(newSpec); // newly added combination sales specifics e.g. LAPTOP
                specCombSales.setOrganization(newSpec.getOrganization());
                Specifics combSpecifics = null;
                double units = 0.0;
                double rate = 0.0;// consisting of different basic parts specifics e.g. RAM, monitor
                if (!combinationSalesItemsArr[i].trim().equals("") && combinationSalesItemsArr[i].trim() != null) {
                    combSpecifics = Specifics.findById(Long.parseLong(combinationSalesItemsArr[i].trim()));
                }
                if (combinationSalesUnitsArr.length > i) {
                    units = Double.parseDouble(combinationSalesUnitsArr[i].trim());
                }
                if (combinationSalesRatesArr.length > i) {
                    if (!"".equals(combinationSalesRatesArr[i]))
                        rate = Double.parseDouble(combinationSalesRatesArr[i].trim());
                }
                specCombSales.setCombSpecificsId(combSpecifics);
                specCombSales.setOpeningBalUnits(units);
                specCombSales.setOpeningBalRate(rate);
                genericDAO.saveOrUpdate(specCombSales, user, entityManager);
            }
        }
    }

    @Transactional
    public Result downloadOrgCOATemplate(int particularValue, Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        ObjectNode results = Json.newObject();
        ArrayNode an = results.putArray("orgCOAFileCred");
        File file = null;
        Users user = null;
        try {
            JsonNode json = request.body().asJson();
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            String orgName = user.getOrganization().getName().replaceAll("\\s", "");
            String fileName = orgName + "_COA_Template.xlsx";
            String sheetName = null;
            if (particularValue == 1) {
                fileName = orgName + "_COA_Income_Template.xlsx";
                sheetName = "Income";
            } else if (particularValue == 2) {
                fileName = orgName + "_COA_Expense_Template.xlsx";
                sheetName = "Expenses";
            } else if (particularValue == 3) {
                fileName = orgName + "_COA_Assets_Template.xlsx";
                sheetName = "Assets";
            } else if (particularValue == 4) {
                fileName = orgName + "_COA_Liabilities_Template.xlsx";
                sheetName = "Liabilities";
            }

            String path = application.path().toString() + "/logs/OrgChartOfAccounts/";
            File filepath = new File(path);
            if (!filepath.exists()) {
                filepath.mkdir();
            }
            path = path + fileName;
            excelService.createOrgCOATemplateExcel(user, entityManager, particularValue, path, sheetName);
            // ObjectNode row = Json.newObject();
            // row.put("fileName", fileName);
            // an.add(row);
            file = new File(path);
            return Results.ok(file).withHeader("ContentType", "application/xlsx").withHeader("Content-Disposition",
                    "attachment; filename = " + fileName);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
    }

    private static boolean isTransactionPresent(Long orgid, Long itemid) {
        boolean returnVal = Transaction.isTxnPresentForItem(entityManager, orgid,
                IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW, itemid);
        if (!returnVal) {
            returnVal = Transaction.isTxnPresentForItem(entityManager, orgid,
                    IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER, itemid);
        }
        if (!returnVal) {
            returnVal = Transaction.isTxnPresentForItem(entityManager, orgid, IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY,
                    itemid);
        }
        if (!returnVal) {
            returnVal = Transaction.isTxnPresentForItem(entityManager, orgid, IdosConstants.BUY_ON_CREDIT_PAY_LATER,
                    itemid);
        }
        if (!returnVal) {
            returnVal = Transaction.isTxnPresentForItem(entityManager, orgid, IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT,
                    itemid);
        }
        if (!returnVal) {
            returnVal = IdosProvisionJournalEntry.isTxnPresentForItem(entityManager, orgid, itemid);
        }
        return returnVal;
    }

    private static boolean isTransactionPresentBranch(Long orgid, Long branchId, Long itemid) {
        boolean returnVal = Transaction.isTxnPresentForItemBranch(entityManager, orgid,
                IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW, branchId, itemid);
        if (!returnVal) {
            returnVal = Transaction.isTxnPresentForItemBranch(entityManager, orgid,
                    IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER, branchId, itemid);
        }
        if (!returnVal) {
            returnVal = Transaction.isTxnPresentForItemBranch(entityManager, orgid,
                    IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY, branchId, itemid);
        }
        if (!returnVal) {
            returnVal = Transaction.isTxnPresentForItemBranch(entityManager, orgid,
                    IdosConstants.BUY_ON_CREDIT_PAY_LATER, branchId, itemid);
        }
        if (!returnVal) {
            returnVal = Transaction.isTxnPresentForItemBranch(entityManager, orgid,
                    IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT, branchId, itemid);
        }
        if (!returnVal) {
            returnVal = IdosProvisionJournalEntry.isTxnPresentForItemBranch(entityManager, orgid, branchId, itemid);
        }
        return returnVal;
    }

    @Transactional
    public Result getSupportDocLimit(Request request) {
        // EntityManager entityManager = getEntityManager();
        ObjectNode results = Json.newObject();
        Users user = null;
        try {
            JsonNode json = request.body().asJson();
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            Long txnSpecific = json.findValue("txnSpecific") != null ? json.findValue("txnSpecific").asLong() : null;
            Long txnBranch = json.findValue("txnBranch") != null ? json.findValue("txnBranch").asLong() : null;
            Double grossTotal = json.findValue("grossTotal") != null ? json.findValue("grossTotal").asDouble() : null;
            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("branch.id", txnBranch);
            criterias.put("specifics.id", txnSpecific);
            criterias.put("presentStatus", 1);
            SpecificsDocUploadMonetoryRuleForBranch monetoryRuleForBranch = genericDAO
                    .getByCriteria(SpecificsDocUploadMonetoryRuleForBranch.class, criterias, entityManager);
            if (monetoryRuleForBranch != null) {
                if (monetoryRuleForBranch.getMonetoryLimit() != null) {
                    results.put("limitAmt", monetoryRuleForBranch.getMonetoryLimit().toString());
                } else {
                    results.put("limitAmt", "");
                }
            } else {
                results.put("limitAmt", "");
            }

        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(results).withHeader("ContentType", "application/json");
    }

    public static Boolean applyTaxRulesToEachBranchSpecifics(Specifics specifics, BranchSpecifics branchSpecific,
            Integer taxCategory, JsonNode json, EntityManager entityManager, Users user, Boolean isNewBranch)
            throws IDOSException {
        Boolean flag = false;

        if (isNewBranch) {

            flag = applyTaxRulesToNewBranch(specifics, branchSpecific, taxCategory, json, entityManager, user);
            return flag;

        } else {

            if (branchSpecific == null) {
                throw new IDOSException(IdosConstants.COA_MAPPING_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                        IdosConstants.COA_MAPPING_EXCEPTION, "Mapping is missing between Branch and Item!");
            }
            Branch branch = branchSpecific.getBranch();
            String gstin = branch.getGstin() == null ? "" : branch.getGstin();
            String branchName = branch.getName();
            if (branchName != null) {
                branchName = branchName.replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "");
                if (branchName.length() > 5) {
                    branchName = branchName.substring(0, 5);
                } else if (branchName.length() > 3) {
                    branchName = branchName.substring(0, 3);
                }
                branchName = branchName.toUpperCase();
            }
            String taxNameList = json.findPath("taxNames").asText();
            String taxRatesList = json.findPath("taxRates").asText();
            String addsDeducts = json.findPath("addsDeducts").asText();
            String appliedTos = json.findPath("appliedTos").asText();
            String formulas = json.findPath("formulas").asText();
            String invoiceValues = json.findPath("invoiceValues").asText();
            // Below three are for validation and allow to add new taxes if these values
            // changed from COA.
            String itemCategory = specifics.getGstItemCategory();
            String itemGstCode = specifics.getGstItemCode();
            Double itemGstRate = specifics.getGstTaxRate();
            Date applicableDate = specifics.getTaxApplicableDate();
            String taxRateArray[] = taxRatesList.substring(0, taxRatesList.length()).split(",");
            String taxNameArray[] = taxNameList.substring(0, taxNameList.length()).split(",");
            String addsDeductsArr[] = addsDeducts.substring(0, addsDeducts.length()).split(",");
            String appliedTosArr[] = appliedTos.substring(0, appliedTos.length()).split(",");
            String formulasArr[] = formulas.substring(0, formulas.length()).split(",");
            String invoiceValuesArr[] = invoiceValues.substring(0, invoiceValues.length()).split(",");
            BranchSpecificsTaxFormula bnchTaxFormula = null;
            int taxType = taxCategory;
            int rateLength = taxRateArray.length;
            for (int i = 0; i < taxNameArray.length; i++) {
                if (taxNameArray[i] == null || "".equals(taxNameArray[i])) {
                    continue;
                }
                if (i > rateLength || (taxRateArray[i] == null || "".equals(taxRateArray[i]))) {
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
                Double taxRate = Double.parseDouble(taxRateArray[i]);
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
                Map<String, Object> criterias = new HashMap<String, Object>(5);
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("branch.id", branch.getId());
                criterias.put("taxName", taxName.toString());
                criterias.put("taxType", taxType);
                criterias.put("presentStatus", 1);
                BranchTaxes branchTaxes = genericDAO.getByCriteria(BranchTaxes.class, criterias, entityManager);
                if (branchTaxes == null) {
                    branchTaxes = new BranchTaxes();
                    branchTaxes.setBranch(branch);
                    branchTaxes.setTaxName(taxName.toString());
                    branchTaxes.setOrganization(user.getOrganization());
                    branchTaxes.setTaxRate(taxRate);
                    branchTaxes.setTaxType(taxType);
                    genericDAO.saveOrUpdate(branchTaxes, user, entityManager);
                }
                if (branchTaxes != null) {
                    bnchTaxFormula = new BranchSpecificsTaxFormula();
                    bnchTaxFormula.setBranchTaxes(branchTaxes);
                    bnchTaxFormula.setBranch(branchTaxes.getBranch());
                    bnchTaxFormula.setOrganization(branchTaxes.getOrganization());
                    bnchTaxFormula.setSpecifics(branchSpecific.getSpecifics());
                    bnchTaxFormula.setParticular(branchSpecific.getParticular());
                    bnchTaxFormula.setBranchSpecifics(branchSpecific);
                    bnchTaxFormula.setAddDeduct(Integer.parseInt(addsDeductsArr[i]));
                    bnchTaxFormula.setAppliedTo(appliedTosArr[i]);
                    bnchTaxFormula.setFormula(formulasArr[i]);
                    bnchTaxFormula.setInvoiceValue(invoiceValuesArr[i]);
                    bnchTaxFormula.setGstItemCategory(itemCategory);
                    bnchTaxFormula.setGstItemCode(itemGstCode);
                    bnchTaxFormula.setGstTaxRate(branchTaxes.getTaxRate());
                    bnchTaxFormula.setApplicableFrom(applicableDate);
                    genericDAO.saveOrUpdate(bnchTaxFormula, user, entityManager);
                    flag = true;
                }
            }

        }
        return flag;
    }

    public static void saveUpdateBranchTax(Long branchId, JsonNode json, Integer taxCategory,
            EntityManager entityManager, Users user) throws IDOSException {
        try {
            Branch branch = Branch.findById(branchId);
            String gstin = branch.getGstin() == null ? "" : branch.getGstin();
            String branchName = branch.getName();
            if (branchName != null && branchName.length() > 3) {
                branchName = branch.getName().replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "").substring(0, 3);
            }
            if (branchName != null) {
                branchName = branchName.toUpperCase();
            }
            String taxNameList = json.findPath("taxNames").asText();
            String taxRatesList = json.findPath("taxRates").asText();
            // Below three are for validation and allow to add new taxes if these values
            // changed from COA.

            String taxRateArray[] = taxRatesList.substring(0, taxRatesList.length()).split(",");
            String taxNameArray[] = taxNameList.substring(0, taxNameList.length()).split(",");

            Map<String, Object> criterias = new HashMap<String, Object>(2);
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
                Double taxRate = Double.parseDouble(taxRateArray[i]);
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

                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("branch.id", branch.getId());
                criterias.put("taxName", taxName.toString());
                criterias.put("taxType", taxType);
                criterias.put("presentStatus", 1);
                BranchTaxes branchTaxes = genericDAO.getByCriteria(BranchTaxes.class, criterias, entityManager);
                // if(branchTaxes != null){
                // branchTaxes.setPresentStatus(0);
                // genericDAO.saveOrUpdate(branchTaxes, user, entityManager);
                // }
                if (branchTaxes == null) {
                    branchTaxes = new BranchTaxes();
                    branchTaxes.setBranch(branch);
                    branchTaxes.setTaxName(taxName.toString());
                    branchTaxes.setOrganization(user.getOrganization());
                    branchTaxes.setTaxRate(taxRate);
                    branchTaxes.setTaxType(taxType);
                    genericDAO.saveOrUpdate(branchTaxes, user, entityManager);
                }
                criterias.clear();
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on fetching taxes.", ex.getMessage());
        }
    }

    public static Boolean isSpecificUsedInTransactions(Specifics specifics, EntityManager entityManager)
            throws IDOSException {
        Boolean flag = false;
        if (!flag) {
            // Mapped Ledger
            if (specifics.getIdentificationForDataValid() != null) {
                flag = true;
            }
        }

        if (!flag) {
            // is childs available
            Query query = entityManager.createQuery(SPECIFICS_CHILD_HQL);
            query.setParameter(1, specifics.getOrganization().getId());
            query.setParameter(2, specifics.getId());
            List<Specifics> specificsList = query.getResultList();
            if (specificsList != null && specificsList.size() > 0) {
                flag = true;
            }
        }

        if (!flag) {
            flag = SPECIFICS_SERVICE.isSpecificHasTxnsAndOpeningBal(specifics, entityManager);
        }

        return flag;
    }

    @Transactional
    public Result disableSpecifics(Request request) {
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        Users user = null;
        EntityTransaction entityTransaction = entityManager.getTransaction();
        try {
            JsonNode json = request.body().asJson();
            log.log(Level.FINE, ">>>> Start " + json);
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            Long specId = json.findValue("specificId") != null ? json.findValue("specificId").asLong() : null;
            Specifics specific = null;
            if (specId != null) {

                // Specific disable
                specific = Specifics.findById(specId);
                if (specific != null) {
                    entityTransaction.begin();
                    // user specific disable
                    Query query = entityManager.createQuery(USER_SPECIFICS_HQL);
                    query.setParameter(1, specific.getId());
                    query.executeUpdate();

                    // branch specific disable
                    List<BranchSpecifics> branchSpecifics = BranchSpecifics.findBranchBySpecific(entityManager,
                            user.getOrganization().getId(), specId);
                    if (branchSpecifics != null) {
                        for (BranchSpecifics branchSpecific : branchSpecifics) {
                            branchSpecific.setPresentStatus(0);
                            genericDAO.saveOrUpdate(branchSpecific, user, entityManager);
                        }
                    }
                    // vendor/ customer specific disable
                    List<VendorSpecific> vendorSpec = VendorSpecific.findBySpecific(entityManager,
                            user.getOrganization().getId(), specId);
                    if (vendorSpec != null) {
                        for (VendorSpecific vendorSpecific : vendorSpec) {
                            vendorSpecific.setPresentStatus(0);
                            genericDAO.saveOrUpdate(vendorSpecific, user, entityManager);
                        }
                    }
                    result.put("status", true);
                    result.put("accountCode", specific.getAccountCode());
                    if (specific.getParentSpecifics() != null) {
                        result.put("parentAccountCode", specific.getParentSpecifics().getAccountCode());
                    } else {
                        result.put("parentAccountCode", "");
                    }
                    specific.setPresentStatus(0);
                    if (specific.getParentSpecifics() != null) {
                        if (specific.getInvoiceItemDescription2() != null) {
                            specific.setInvoiceItemDescription2(specific.getInvoiceItemDescription2() + " "
                                    + specific.getParentSpecifics().getId());
                        } else {
                            specific.setInvoiceItemDescription2(specific.getParentSpecifics().getId() + "");
                        }
                        specific.setParentSpecifics(null);
                    }
                    genericDAO.saveOrUpdate(specific, user, entityManager);
                    entityTransaction.commit();
                } else {
                    result.put("status", false);
                }
            }
        } catch (Exception ex) {
            if (entityTransaction.isActive()) {
                entityTransaction.rollback();
                entityManager.flush();
            }
            log.log(Level.SEVERE, user.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result);
    }

    public static Boolean applyTaxRulesToNewBranch(Specifics specifics, BranchSpecifics branchSpecific,
            Integer taxCategory, JsonNode json, EntityManager entityManager, Users user) throws IDOSException {
        Boolean flag = false;
        if (branchSpecific == null) {
            throw new IDOSException(IdosConstants.COA_MAPPING_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                    IdosConstants.COA_MAPPING_EXCEPTION, "Mapping is missing between Branch and Item!");
        }
        Branch branch = branchSpecific.getBranch();
        String gstin = branch.getGstin() == null ? "" : branch.getGstin();
        String branchName = branch.getName();
        if (branchName != null) {
            if (branchName.length() > 5) {
                branchName = branch.getName().replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "").substring(0, 5);
            } else if (branchName.length() > 3) {
                branchName = branch.getName().replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "").substring(0, 3);
            }
        }
        if (branchName != null) {
            branchName = branchName.toUpperCase();
        }

        ArrayList paramsList = new ArrayList<>();
        paramsList.add(specifics.getId());
        paramsList.add(user.getOrganization().getId());
        List<Branch> taxConfigBranch = genericDAO.queryWithParamsName(FETCH_BRANCHS, entityManager, paramsList);

        Long branchId = 0L;
        if (taxConfigBranch != null) {
            branchId = taxConfigBranch.get(0).getId();
        }

        ArrayList params = new ArrayList<>();
        params.add(branchId);
        params.add(user.getOrganization().getId());
        params.add(specifics.getId());

        List<Object[]> brachTaxInfo = genericDAO.queryWithParamsNameGeneric(FETCH_BRANCH_TAXES, entityManager, params);

        String taxNameList = "";
        String taxRatesList = "";
        String addsDeducts = "";
        String appliedTos = "";
        String formulas = "";
        String invoiceValues = "";

        for (Object[] taxRow : brachTaxInfo) {

            if (taxRow[0] != null) {
                Long branchTaxID = Long.parseLong(taxRow[0].toString());
                BranchTaxes branchTaxes = BranchTaxes.findById(branchTaxID);
                taxNameList += branchTaxes.getTaxName().substring(0, 4) + ",";
            }
            if (taxRow[1] != null) {
                formulas += taxRow[1].toString() + ",";
            }
            if (taxRow[2] != null) {
                appliedTos += taxRow[2].toString() + ",";
            }
            if (taxRow[3] != null) {
                addsDeducts += taxRow[3].toString() + ",";
            }
            if (taxRow[4] != null) {
                invoiceValues += taxRow[4].toString() + ",";
            }
            if (taxRow[5] != null) {
                taxRatesList += taxRow[5].toString() + ",";
            }
        }

        // Below three are for validation and allow to add new taxes if these values
        // changed from COA.
        String itemCategory = specifics.getGstItemCategory();
        String itemGstCode = specifics.getGstItemCode();
        Double itemGstRate = specifics.getGstTaxRate();
        Date applicableDate = specifics.getTaxApplicableDate();

        String taxRateArray[] = taxRatesList.substring(0, taxRatesList.length()).split(",");
        String taxNameArray[] = taxNameList.substring(0, taxNameList.length()).split(",");
        String addsDeductsArr[] = addsDeducts.substring(0, addsDeducts.length()).split(",");
        String appliedTosArr[] = appliedTos.substring(0, appliedTos.length()).split(",");
        String formulasArr[] = formulas.substring(0, formulas.length()).split(",");
        String invoiceValuesArr[] = invoiceValues.substring(0, invoiceValues.length()).split(",");

        BranchSpecificsTaxFormula bnchTaxFormula = null;
        int taxType = taxCategory;
        int rateLength = taxRateArray.length;
        for (int i = 0; i < taxNameArray.length; i++) {
            if (taxNameArray[i] == null || "".equals(taxNameArray[i])) {
                continue;
            }
            if (i > rateLength || (taxRateArray[i] == null || "".equals(taxRateArray[i]))) {
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
            Double taxRate = Double.parseDouble(taxRateArray[i]);
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
            Map<String, Object> criterias = new HashMap<String, Object>(5);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("branch.id", branch.getId());
            criterias.put("taxName", taxName.toString());
            criterias.put("taxType", taxType);
            criterias.put("presentStatus", 1);
            BranchTaxes branchTaxes = genericDAO.getByCriteria(BranchTaxes.class, criterias, entityManager);
            if (branchTaxes == null) {
                branchTaxes = new BranchTaxes();
                branchTaxes.setBranch(branch);
                branchTaxes.setTaxName(taxName.toString());
                branchTaxes.setOrganization(user.getOrganization());
                branchTaxes.setTaxRate(taxRate);
                branchTaxes.setTaxType(taxType);
                genericDAO.saveOrUpdate(branchTaxes, user, entityManager);
            }
            if (branchTaxes != null) {
                bnchTaxFormula = new BranchSpecificsTaxFormula();
                bnchTaxFormula.setBranchTaxes(branchTaxes);
                bnchTaxFormula.setBranch(branchTaxes.getBranch());
                bnchTaxFormula.setOrganization(branchTaxes.getOrganization());
                bnchTaxFormula.setSpecifics(branchSpecific.getSpecifics());
                bnchTaxFormula.setParticular(branchSpecific.getParticular());
                bnchTaxFormula.setBranchSpecifics(branchSpecific);
                bnchTaxFormula.setAddDeduct(Integer.parseInt(addsDeductsArr[i]));
                bnchTaxFormula.setAppliedTo(appliedTosArr[i]);
                bnchTaxFormula.setFormula(formulasArr[i]);
                bnchTaxFormula.setInvoiceValue(invoiceValuesArr[i]);
                bnchTaxFormula.setGstItemCategory(itemCategory);
                bnchTaxFormula.setGstItemCode(itemGstCode);
                bnchTaxFormula.setGstTaxRate(branchTaxes.getTaxRate());
                bnchTaxFormula.setApplicableFrom(applicableDate);
                genericDAO.saveOrUpdate(bnchTaxFormula, user, entityManager);
                flag = true;
            }
        }
        return flag;
    }

}
