package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.BranchSpecifics;
import model.BranchSpecificsTaxFormula;
import model.BranchTaxes;
import model.Specifics;
import model.Users;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;

import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import javax.inject.Inject;
import views.html.*;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;
import play.Application;

public class TaxController extends StaticController {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	public static Application application;
	private Request request;
	// private Http.Session session = request.session();

	@Inject
	public TaxController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result taxFormulaValidation(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users users = null;
		try {
			JsonNode json = request.body().asJson();
			ArrayNode an = result.putArray("taxFormulaOutcomeData");
			String useremail = json.findValue("usermail").asText();
			String aValue = json.findValue("aValue").asText();
			String taxFormulas = json.findValue("taxFormulas").asText();
			String taxRates = json.findValue("taxRates").asText();
			String addDeducts = json.findValue("addDeducts").asText();
			String applyTos = json.findValue("applyTos").asText();
			session.adding("email", useremail);
			users = getUserInfo(request);
			String[] formulaArr = taxFormulas.split(",");
			String[] ratesArr = taxRates.split(",");
			String[] addDeductsArr = addDeducts.split(",");
			String[] applyTosArr = applyTos.split(",");
			Double taxTotalAmount = 0.0;
			Double B = null;
			Double C = null;
			Double D = null;
			Double E = null;
			Double F = null;
			Double G = null;
			Double Tax1 = 0.0;
			Double IV1 = 0.0;
			Double Tax2 = 0.0;
			Double IV2 = 0.0;
			Double Tax3 = 0.0;
			Double IV3 = 0.0;
			Double Tax4 = 0.0;
			Double IV4 = 0.0;
			Double Tax5 = 0.0;
			Double IV5 = 0.0;

			// Double A=Double.parseDouble(aValue);
			Double GV = Double.parseDouble(aValue);
			ObjectNode arow = Json.newObject();
			arow.put("A", "100.00");
			an.add(arow);
			for (int i = 0; i < formulaArr.length; i++) {
				ObjectNode row = Json.newObject();

				if (i == 0) {
					String appliedOn = applyTosArr[i];
					if (appliedOn.equals("Gross Value(GV)")) {
						Tax1 = GV * (Double.parseDouble(ratesArr[i]) / (100.0));
						if (Integer.parseInt(addDeductsArr[i]) == 1) {
							IV1 = GV + Tax1;
						}
						if (Integer.parseInt(addDeductsArr[i]) == 0) {
							IV1 = GV - Tax1;
						}
						row.put("Tax1", decimalFormat.format(Tax1));
						// row.put("IV1",decimalFormat.format(IV1));
						taxTotalAmount += Tax1;
					}
				}
				if (i == 1) {
					String appliedOn = applyTosArr[i];
					if (appliedOn.equals("Gross Value(GV)")) {
						Tax2 = GV * (Double.parseDouble(ratesArr[i]) / (100.0));
					} else if (appliedOn.equals("Tax1")) {
						Tax2 = Tax1 * (Double.parseDouble(ratesArr[i]) / (100.0));
					} else if (appliedOn.equals("IV1")) {
						Tax2 = IV1 * (Double.parseDouble(ratesArr[i]) / (100.0));
					}
					if (Integer.parseInt(addDeductsArr[i]) == 1) {
						IV2 = GV + Tax1 + Tax2;
					}
					if (Integer.parseInt(addDeductsArr[i]) == 0) {
						IV2 = GV - (Tax1 + Tax2);
					}
					row.put("Tax2", decimalFormat.format(Tax2));
					// row.put("IV2",decimalFormat.format(IV2));
					taxTotalAmount += Tax2;

				}
				if (i == 2) {
					String appliedOn = applyTosArr[i];
					if (appliedOn.equals("Gross Value(GV)")) {
						Tax3 = GV * (Double.parseDouble(ratesArr[i]) / (100.0));
					} else if (appliedOn.equals("Tax1")) {
						Tax3 = Tax1 * (Double.parseDouble(ratesArr[i]) / (100.0));
					} else if (appliedOn.equals("Tax2")) {
						Tax3 = Tax2 * (Double.parseDouble(ratesArr[i]) / (100.0));
					} else if (appliedOn.equals("IV2")) {
						Tax3 = IV2 * (Double.parseDouble(ratesArr[i]) / (100.0));
					}
					if (Integer.parseInt(addDeductsArr[i]) == 1) {
						IV3 = GV + Tax1 + Tax2 + Tax3;
					}
					if (Integer.parseInt(addDeductsArr[i]) == 0) {
						IV3 = GV - (Tax1 + Tax2 + Tax3);
					}
					row.put("Tax3", decimalFormat.format(Tax3));
					// row.put("IV3",decimalFormat.format(IV3));
					taxTotalAmount += Tax3;
				}
				if (i == 3) {
					String appliedOn = applyTosArr[i];
					if (appliedOn.equals("Gross Value(GV)")) {
						Tax4 = GV * (Double.parseDouble(ratesArr[i]) / (100.0));
					} else if (appliedOn.equals("Tax1")) {
						Tax4 = Tax1 * (Double.parseDouble(ratesArr[i]) / (100.0));
					} else if (appliedOn.equals("Tax2")) {
						Tax4 = Tax2 * (Double.parseDouble(ratesArr[i]) / (100.0));
					} else if (appliedOn.equals("Tax3")) {
						Tax4 = Tax3 * (Double.parseDouble(ratesArr[i]) / (100.0));
					} else if (appliedOn.equals("IV3")) {
						Tax4 = IV3 * (Double.parseDouble(ratesArr[i]) / (100.0));
					}
					if (Integer.parseInt(addDeductsArr[i]) == 1) {
						IV4 = GV + Tax1 + Tax2 + Tax3 + Tax4;
					}
					if (Integer.parseInt(addDeductsArr[i]) == 0) {
						IV4 = GV - (Tax1 + Tax2 + Tax3 + Tax4);
					}
					row.put("Tax4", decimalFormat.format(Tax4));
					taxTotalAmount += Tax4;
				}
				if (i == 4) {
					String appliedOn = applyTosArr[i];
					if (appliedOn.equals("Gross Value(GV)")) {
						Tax5 = GV * (Double.parseDouble(ratesArr[i]) / (100.0));
					} else if (appliedOn.equals("Tax1")) {
						Tax5 = Tax1 * (Double.parseDouble(ratesArr[i]) / (100.0));
					} else if (appliedOn.equals("Tax2")) {
						Tax5 = Tax2 * (Double.parseDouble(ratesArr[i]) / (100.0));
					} else if (appliedOn.equals("Tax3")) {
						Tax5 = Tax3 * (Double.parseDouble(ratesArr[i]) / (100.0));
					} else if (appliedOn.equals("Tax4")) {
						Tax5 = Tax4 * (Double.parseDouble(ratesArr[i]) / (100.0));
					} else if (appliedOn.equals("IV4")) {
						Tax5 = IV4 * (Double.parseDouble(ratesArr[i]) / (100.0));
					}
					if (Integer.parseInt(addDeductsArr[i]) == 1) {
						IV5 = GV + Tax1 + Tax2 + Tax3 + Tax4 + Tax5;
					}
					if (Integer.parseInt(addDeductsArr[i]) == 0) {
						IV5 = GV - (Tax1 + Tax2 + Tax3 + Tax4 + Tax5);
					}
					row.put("Tax5", decimalFormat.format(Tax5));
					taxTotalAmount += Tax5;
				}
				an.add(row);
			}
			ObjectNode totalRow = Json.newObject();
			totalRow.put("totalTaxOnGross", decimalFormat.format(taxTotalAmount));
			an.add(totalRow);
			ObjectNode outputRow = Json.newObject();
			outputRow.put("net", decimalFormat.format(GV + (taxTotalAmount)));
			an.add(outputRow);
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
	public Result getBranchTaxes(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode branchtaxesan = result.putArray("branchTaxes");
		ArrayNode branchspecftaxesformulaan = result.putArray("branchSpecfTaxesFormula");
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
			// String useremail=json.findValue("usermail").asText();
			String branchId = json.findValue("branchPrimaryId").asText();
			String specificsId = json.findValue("specificsPrimId").asText();
			Specifics specificsentity = Specifics.findById(IdosUtil.convertStringToLong(specificsId));

			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("presentStatus", 1);
			criterias.put("taxType", 2);
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchId));
			criterias.put("organization.id", user.getOrganization().getId());
			List<BranchTaxes> branchTaxesList = genericDAO.findByCriteria(BranchTaxes.class, criterias, entityManager);
			for (BranchTaxes bnchTaxes : branchTaxesList) {
				ObjectNode row = Json.newObject();
				row.put("id", bnchTaxes.getId());
				row.put("name", bnchTaxes.getTaxName());
				row.put("rate", bnchTaxes.getTaxRate());
				row.put("specificsname", specificsentity.getName());
				branchtaxesan.add(row);
			}
			criterias.clear();
			// criterias.put("presentStatus", 1);
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchId));
			criterias.put("specifics.id", IdosUtil.convertStringToLong(specificsId));
			criterias.put("presentStatus", 1);
			BranchSpecifics bnchSpecf = genericDAO.getByCriteria(BranchSpecifics.class, criterias, entityManager);
			if (bnchSpecf != null) {
				List<BranchSpecificsTaxFormula> bnchSpecfTaxFormulaList = bnchSpecf.getBranchSpecificsTaxesFormulas();
				for (BranchSpecificsTaxFormula bnchSpecfTaxForm : bnchSpecfTaxFormulaList) {
					if (bnchSpecfTaxForm.getBranchTaxes().getTaxType() == IdosConstants.OUTPUT_TAX) {
						ObjectNode row = Json.newObject();
						row.put("id", bnchSpecfTaxForm.getId());
						row.put("branchTaxId", bnchSpecfTaxForm.getBranchTaxes().getId());
						row.put("appliedTo", bnchSpecfTaxForm.getAppliedTo());
						row.put("addDeduct", bnchSpecfTaxForm.getAddDeduct());
						row.put("formula", bnchSpecfTaxForm.getFormula());
						row.put("invoiceValue", bnchSpecfTaxForm.getInvoiceValue());
						row.put("status", bnchSpecfTaxForm.getPresentStatus());
						branchspecftaxesformulaan.add(row);
					}
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
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result applyTaxRulesToBranchSpecifics(Request request) {
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start " + json);
			transaction.begin();
			String useremail = json.findValue("usermail").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			String branchId = json.findValue("branchId").asText();
			String specificsId = json.findValue("specificsId").asText();
			String multiItemsSpecificsId = json.findValue("applyRulesToMultiItemsList").asText();
			String[] itemsList = null;
			if (multiItemsSpecificsId != null && multiItemsSpecificsId != "") {
				itemsList = multiItemsSpecificsId.split(",");
			}
			if (itemsList != null) { // if multiple items are selected to apply same tax rules
				for (int x = 0; x < itemsList.length; x++) {
					specificsId = itemsList[x];
					applyTaxRulesToEachBranchSpecifics(specificsId, branchId, json, entityManager, request);
				}
			} else { // single item
				applyTaxRulesToEachBranchSpecifics(specificsId, branchId, json, entityManager, request);
			}
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	private static void applyTaxRulesToEachBranchSpecifics(String specificsId, String branchId, JsonNode json,
			EntityManager entityManager, Request request) {
		Users user = getUserInfo(request);
		Map<String, Object> criterias = new HashMap<String, Object>();
		criterias.put("branch.id", Long.parseLong(branchId));
		criterias.put("specifics.id", Long.parseLong(specificsId));
		criterias.put("presentStatus", 1);
		BranchSpecifics bnchSpecf = genericDAO.getByCriteria(BranchSpecifics.class, criterias, entityManager);
		String bnchTaxFmHid = json.findPath("bnchTaxFmHid").asText();
		if (bnchTaxFmHid == "") {
			bnchTaxFmHid = ",";
		}
		String taxTypes = json.findPath("taxTypes").asText();
		String addsDeducts = json.findPath("addsDeducts").asText();
		String appliedTos = json.findPath("appliedTos").asText();
		String formulas = json.findPath("formulas").asText();
		String invoiceValues = json.findPath("invoiceValues").asText();
		String bnchTaxFmHidArr[] = bnchTaxFmHid.substring(0, bnchTaxFmHid.length()).split(",");
		String taxTypesArr[] = taxTypes.substring(0, taxTypes.length()).split(",");
		String addsDeductsArr[] = addsDeducts.substring(0, addsDeducts.length()).split(",");
		String appliedTosArr[] = appliedTos.substring(0, appliedTos.length()).split(",");
		String formulasArr[] = formulas.substring(0, formulas.length()).split(",");
		String invoiceValuesArr[] = invoiceValues.substring(0, invoiceValues.length()).split(",");
		BranchSpecificsTaxFormula bnchTaxFormula = null;
		for (int i = 0; i < bnchTaxFmHidArr.length; i++) {
			if (taxTypesArr.length > i && (taxTypesArr[i] != null && !"".equals(taxTypesArr[i]))) {
				bnchTaxFormula = BranchSpecificsTaxFormula.findById(Long.parseLong(bnchTaxFmHidArr[i]));
				BranchTaxes bnchTaxes = BranchTaxes.findById(Long.parseLong(taxTypesArr[i]));
				bnchTaxFormula.setBranchTaxes(bnchTaxes);
				bnchTaxFormula.setBranch(bnchTaxes.getBranch());
				bnchTaxFormula.setOrganization(bnchTaxes.getOrganization());
				bnchTaxFormula.setSpecifics(bnchSpecf.getSpecifics());
				bnchTaxFormula.setParticular(bnchSpecf.getParticular());
				bnchTaxFormula.setBranchSpecifics(bnchSpecf);
				bnchTaxFormula.setAddDeduct(Integer.parseInt(addsDeductsArr[i]));
				bnchTaxFormula.setAppliedTo(appliedTosArr[i]);
				bnchTaxFormula.setFormula(formulasArr[i]);
				bnchTaxFormula.setInvoiceValue(invoiceValuesArr[i]);
				bnchTaxFormula.setPresentStatus(1);
				log.log(Level.FINE, "===========================>" + bnchTaxFormula.getPresentStatus());
				genericDAO.saveOrUpdate(bnchTaxFormula, user, entityManager);
			} else {
				// bnchTaxFormula=BranchSpecificsTaxFormula.findById(Long.parseLong(bnchTaxFmHidArr[i]));
				if (bnchTaxFmHidArr[i] != null) {
					// genericDAO.deleteById(BranchSpecificsTaxFormula.class,
					// Long.parseLong(bnchTaxFmHidArr[i]), entityManager);

					String taxID = bnchTaxFmHidArr[i];
					bnchTaxFormula = BranchSpecificsTaxFormula.findById(Long.parseLong(taxID));
					if (bnchTaxFormula != null) {
						bnchTaxFormula.setPresentStatus(0);
						genericDAO.saveOrUpdate(bnchTaxFormula, user, entityManager);
					}
				}
			}
		}
		for (int i = bnchTaxFmHidArr.length; (i < taxTypesArr.length || i < formulasArr.length); i++) {
			if (taxTypesArr[i] == null || "".equals(taxTypesArr[i])) {
				continue;
			}
			bnchTaxFormula = new BranchSpecificsTaxFormula();
			BranchTaxes bnchTaxes = BranchTaxes.findById(Long.parseLong(taxTypesArr[i]));
			bnchTaxFormula.setBranchTaxes(bnchTaxes);
			bnchTaxFormula.setBranch(bnchTaxes.getBranch());
			bnchTaxFormula.setOrganization(bnchTaxes.getOrganization());
			bnchTaxFormula.setSpecifics(bnchSpecf.getSpecifics());
			bnchTaxFormula.setParticular(bnchSpecf.getParticular());
			bnchTaxFormula.setBranchSpecifics(bnchSpecf);
			bnchTaxFormula.setAddDeduct(Integer.parseInt(addsDeductsArr[i]));
			bnchTaxFormula.setAppliedTo(appliedTosArr[i]);
			bnchTaxFormula.setFormula(formulasArr[i]);
			bnchTaxFormula.setInvoiceValue(invoiceValuesArr[i]);
			genericDAO.saveOrUpdate(bnchTaxFormula, user, entityManager);
		}
	}

	@Transactional
	public Result applyTaxRulesToBranchSpecificsTmp(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start: " + json);
			user = getUserInfo(request);
			String branchId = json.findValue("branchId").asText();
			String specificsId = json.findValue("specificsId").asText();
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("branch.id", Long.parseLong(branchId));
			criterias.put("specifics.id", Long.parseLong(specificsId));
			criterias.put("presentStatus", 1);
			BranchSpecifics bnchSpecf = genericDAO.getByCriteria(BranchSpecifics.class, criterias, entityManager);
			String bnchTaxFmHid = json.findPath("bnchTaxFmHid").asText();
			if (bnchTaxFmHid == "") {
				bnchTaxFmHid = ",";
			}
			String taxTypes = json.findPath("taxTypes").asText();
			String addsDeducts = json.findPath("addsDeducts").asText();
			String appliedTos = json.findPath("appliedTos").asText();
			String formulas = json.findPath("formulas").asText();
			String invoiceValues = json.findPath("invoiceValues").asText();
			JsonNode checkedTaxIDjn = json.get("checkedTaxIDList");
			JsonNode uncheckedTaxIDjn = json.get("uncheckedTaxIDList");

			String bnchTaxFmHidArr[] = bnchTaxFmHid.substring(0, bnchTaxFmHid.length()).split(",");
			String taxTypesArr[] = taxTypes.substring(0, taxTypes.length()).split(",");
			String addsDeductsArr[] = addsDeducts.substring(0, addsDeducts.length()).split(",");
			String appliedTosArr[] = appliedTos.substring(0, appliedTos.length()).split(",");
			String formulasArr[] = formulas.substring(0, formulas.length()).split(",");
			String invoiceValuesArr[] = invoiceValues.substring(0, invoiceValues.length()).split(",");
			BranchSpecificsTaxFormula bnchTaxFormula = null;

			for (int i = 0; i < checkedTaxIDjn.size(); i++) {
				String taxID = checkedTaxIDjn.get(i).asText();
				if (taxTypesArr.length > i && !"".equals(taxTypesArr[i])) {
					bnchTaxFormula = BranchSpecificsTaxFormula.findById(Long.parseLong(taxID));
					BranchTaxes bnchTaxes = BranchTaxes.findById(Long.parseLong(taxTypesArr[i]));
					bnchTaxFormula.setBranchTaxes(bnchTaxes);
					bnchTaxFormula.setBranch(bnchTaxes.getBranch());
					bnchTaxFormula.setOrganization(bnchTaxes.getOrganization());
					bnchTaxFormula.setSpecifics(bnchSpecf.getSpecifics());
					bnchTaxFormula.setParticular(bnchSpecf.getParticular());
					bnchTaxFormula.setBranchSpecifics(bnchSpecf);
					bnchTaxFormula.setAddDeduct(Integer.parseInt(addsDeductsArr[i]));
					bnchTaxFormula.setAppliedTo(appliedTosArr[i]);
					bnchTaxFormula.setFormula(formulasArr[i]);
					bnchTaxFormula.setInvoiceValue(invoiceValuesArr[i]);
					bnchTaxFormula.setPresentStatus(1);
					genericDAO.saveOrUpdate(bnchTaxFormula, user, entityManager);
				}
			}

			for (int i = 0; i < uncheckedTaxIDjn.size(); i++) {
				String taxID = uncheckedTaxIDjn.get(i).asText();
				bnchTaxFormula = BranchSpecificsTaxFormula.findById(Long.parseLong(taxID));
				if (bnchTaxFormula != null) {
					bnchTaxFormula.setPresentStatus(0);
					genericDAO.saveOrUpdate(bnchTaxFormula, user, entityManager);
				}

				/*
				 * if(bnchTaxFmHidArr[i] != null) {
				 * genericDAO.deleteById(BranchSpecificsTaxFormula.class,
				 * Long.parseLong(bnchTaxFmHidArr[i]), entityManager);
				 * }
				 */
			}

			for (int i = bnchTaxFmHidArr.length; (i < taxTypesArr.length || i < formulasArr.length); i++) {
				bnchTaxFormula = new BranchSpecificsTaxFormula();
				BranchTaxes bnchTaxes = BranchTaxes.findById(Long.parseLong(taxTypesArr[i]));
				bnchTaxFormula.setBranchTaxes(bnchTaxes);
				bnchTaxFormula.setBranch(bnchTaxes.getBranch());
				bnchTaxFormula.setOrganization(bnchTaxes.getOrganization());
				bnchTaxFormula.setSpecifics(bnchSpecf.getSpecifics());
				bnchTaxFormula.setParticular(bnchSpecf.getParticular());
				bnchTaxFormula.setBranchSpecifics(bnchSpecf);
				bnchTaxFormula.setAddDeduct(Integer.parseInt(addsDeductsArr[i]));
				bnchTaxFormula.setAppliedTo(appliedTosArr[i]);
				bnchTaxFormula.setFormula(formulasArr[i]);
				bnchTaxFormula.setInvoiceValue(invoiceValuesArr[i]);
				genericDAO.saveOrUpdate(bnchTaxFormula, user, entityManager);
			}
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
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
	public Result saveTaxableItemsForCompositionScheme(Http.Request request) {
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start " + json);
			transaction.begin();
			String useremail = json.findValue("usermail").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			taxService.saveTaxableItemsForCompositionScheme(json, entityManager, user);
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
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
