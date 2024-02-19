package controllers;

import static controllers.Karvy.ExternalUserLoginController.entityManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.idos.util.IdosUtil;
import model.*;
import model.payroll.PayrollSetup;
import model.payroll.PayrollUserPayslip;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.IdosConstants;
import java.util.logging.Level;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import javax.inject.Inject;
import views.html.errorPage;
import play.Application;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

public class TransactionItemsController extends StaticController {
	public static Application application;
	private static JPAApi jpaApi;
	public static EntityManager em;
	public Request request;
	// private Http.Session session = request.session();

	@Inject
	public TransactionItemsController(Application application) {
		super(application);
		em = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result getListOfMultipleItems(Request request) {
		ObjectNode results = Json.newObject();
		// EntityManager entityManager=getEntityManager();
		Users user = getUserInfo(request);
		try {
			JsonNode json = request.body().asJson();
			ArrayNode txnItemsan = results.putArray("transactionItemdetailsData");
			long transactionEntityId = json.findValue("transactionEntityId").asLong();
			String txnReferenceNo = json.findValue("txnReferenceNo") != null ? json.findValue("txnReferenceNo").asText()
					: null;
			if (txnReferenceNo != null && txnReferenceNo.startsWith(IdosConstants.BOM_TXN_TYPE)) {
				BILL_OF_MATERIAL_TXN_SERVICE.getListOfTxnItems(user, em, transactionEntityId, txnItemsan);
				if (log.isLoggable(Level.FINE))
					log.log(Level.FINE, ">>>> End " + results);
				return Results.ok(results);
			}

			if (txnReferenceNo != null && txnReferenceNo.startsWith(IdosConstants.PR_TXN_TYPE)) {
				PURCHASE_REQUISITION_TXN_SERVICE.getListOfTxnItems(user, entityManager, transactionEntityId,
						txnItemsan);
				if (log.isLoggable(Level.FINE))
					log.log(Level.FINE, ">>>> End " + results);
				return ok(results);
			}

			if (txnReferenceNo != null && txnReferenceNo.startsWith(IdosConstants.PO_TXN_TYPE)) {
				purchaseOrderService.getListOfTxnItems(user, entityManager, transactionEntityId, txnItemsan);
				if (log.isLoggable(Level.FINE))
					log.log(Level.FINE, ">>>> End " + results);
				return ok(results);
			}

			Map<String, Object> criterias = new HashMap<String, Object>();
			// criterias.put("transactionId", Long.parseLong(transactionEntityId));
			criterias.put("transaction.id", transactionEntityId);
			criterias.put("presentStatus", 1);
			List<TransactionItems> listTransactionItems = genericDAO.findByCriteria(TransactionItems.class, criterias,
					em);
			for (TransactionItems txnItemrow : listTransactionItems) {
				ObjectNode row = Json.newObject();
				row.put("id", txnItemrow.getId());
				row.put("noOfUnits", IdosConstants.DECIMAL_FORMAT2
						.format((txnItemrow.getNoOfUnits() == null ? 0 : txnItemrow.getNoOfUnits())));
				row.put("pricePerUnit", IdosConstants.decimalFormat
						.format(txnItemrow.getPricePerUnit() == null ? 0.0 : txnItemrow.getPricePerUnit()));
				row.put("discountPer",
						txnItemrow.getDiscountPercent() == null ? "0.00" : txnItemrow.getDiscountPercent());
				row.put("discountAmt", IdosConstants.decimalFormat
						.format(txnItemrow.getDiscountAmount() == null ? 0.0 : txnItemrow.getDiscountAmount()));
				if (txnItemrow.getGrossAmount() != null) {
					row.put("grossAmount", IdosConstants.decimalFormat.format(txnItemrow.getGrossAmount()));
				} else {
					row.put("grossAmount", "");
				}
				if (txnItemrow.getTransactionSpecifics() != null) {
					row.put("itemName", txnItemrow.getTransactionSpecifics().getName());
					row.put("itemId", txnItemrow.getTransactionSpecifics().getId());
				} else {
					row.put("itemName", "");
					row.put("itemId", "");
				}
				double totalTax = 0.0;
				if (txnItemrow.getTotalTax() == null || txnItemrow.getTotalTax() == 0.0) {
					totalTax = (txnItemrow.getTaxValue1() == null ? 0.0 : txnItemrow.getTaxValue1()) +
							(txnItemrow.getTaxValue2() == null ? 0.0 : txnItemrow.getTaxValue2()) +
							(txnItemrow.getTaxValue3() == null ? 0.0 : txnItemrow.getTaxValue3()) +
							(txnItemrow.getTaxValue4() == null ? 0.0 : txnItemrow.getTaxValue4()) +
							(txnItemrow.getTaxValue5() == null ? 0.0 : txnItemrow.getTaxValue5()) +
							(txnItemrow.getTaxValue6() == null ? 0.0 : txnItemrow.getTaxValue6()) +
							(txnItemrow.getTaxValue7() == null ? 0.0 : txnItemrow.getTaxValue7());
				} else {
					totalTax = txnItemrow.getTotalTax();
				}
				row.put("totalInputTax", IdosConstants.decimalFormat.format(totalTax));
				if (IdosUtil.isNull(txnItemrow.getTaxDescription()) && totalTax > 0.0) {
					StringBuilder desc = new StringBuilder();
					BranchTaxes branchTax = null;
					if (txnItemrow.getTax1ID() != null) {
						branchTax = BranchTaxes.findById(txnItemrow.getTax1ID());
						desc.append(branchTax.getTaxName()).append(": ").append(txnItemrow.getTaxValue1());
					}
					if (txnItemrow.getTax2ID() != null) {
						branchTax = BranchTaxes.findById(txnItemrow.getTax2ID());
						desc.append(" ").append(branchTax.getTaxName()).append(": ").append(txnItemrow.getTaxValue2());
					}
					if (txnItemrow.getTax3ID() != null) {
						branchTax = BranchTaxes.findById(txnItemrow.getTax3ID());
						desc.append(" ").append(branchTax.getTaxName()).append(": ").append(txnItemrow.getTaxValue3());
					}
					if (txnItemrow.getTax4ID() != null) {
						branchTax = BranchTaxes.findById(txnItemrow.getTax4ID());
						desc.append(" ").append(branchTax.getTaxName()).append(": ").append(txnItemrow.getTaxValue4());
					}
					if (txnItemrow.getTax5ID() != null) {
						branchTax = BranchTaxes.findById(txnItemrow.getTax5ID());
						desc.append(" ").append(branchTax.getTaxName()).append(": ").append(txnItemrow.getTaxValue5());
					}
					if (txnItemrow.getTax6ID() != null) {
						branchTax = BranchTaxes.findById(txnItemrow.getTax6ID());
						desc.append(" ").append(branchTax.getTaxName()).append(": ").append(txnItemrow.getTaxValue6());
					}
					if (txnItemrow.getTax7ID() != null) {
						branchTax = BranchTaxes.findById(txnItemrow.getTax7ID());
						desc.append(" ").append(branchTax.getTaxName()).append(": ").append(txnItemrow.getTaxValue7());
					}
					row.put("taxDescription", desc.toString());
				} else {
					row.put("taxDescription",
							txnItemrow.getTaxDescription() == null ? "" : txnItemrow.getTaxDescription());
				}

				row.put("withholdingAmount", IdosConstants.decimalFormat
						.format(txnItemrow.getWithholdingAmount() == null ? 0.0 : txnItemrow.getWithholdingAmount()));
				row.put("availableAdvance", IdosConstants.decimalFormat
						.format(txnItemrow.getAvailableAdvance() == null ? 0.0 : txnItemrow.getAvailableAdvance()));
				row.put("adjFromAdvance", IdosConstants.decimalFormat.format(
						txnItemrow.getAdjustmentFromAdvance() == null ? 0.0 : txnItemrow.getAdjustmentFromAdvance()));
				row.put("netAmount", IdosConstants.decimalFormat
						.format(txnItemrow.getNetAmount() == null ? 0.0 : txnItemrow.getNetAmount()));
				txnItemsan.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getPayrollItems(Request request) {
		ObjectNode results = Json.newObject();
		// EntityManager entityManager=getEntityManager();
		try {
			Users user = getUserInfo(request);
			if (user == null) {
				return unauthorized(results);
			}
			JsonNode json = request.body().asJson();
			ArrayNode payrollItemsan = results.putArray("payrollDetailsData");
			ArrayNode payrollEarnHeadsDataan = results.putArray("payrollEarnHeadsData");
			ArrayNode payrollDeduHeadsDataan = results.putArray("payrollDeduHeadsData");
			String transactionEntityId = json.findValue("transactionEntityId").asText();
			// Map<String, Object> criterias = new HashMap<String, Object>();
			// criterias.put("transactionId", Long.parseLong(transactionEntityId));
			// criterias.put("transaction.id", Long.parseLong(transactionEntityId));
			// List<TransactionItems>
			// listTransactionItems=genericDAO.findByCriteria(TransactionItems.class,
			// criterias, entityManager);
			String sbquery = "select obj from PayrollUserPayslip obj where obj.transactionRefNumber = (select o.transactionRefNumber from PayrollTransaction o where o.id = ?1 and o.presentStatus=1) and obj.presentStatus=1";
			ArrayList inparam = new ArrayList(1);
			inparam.add(Long.parseLong(transactionEntityId));
			List<PayrollUserPayslip> payrollItemList = genericDAO.queryWithParams(sbquery, em, inparam);
			String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September",
					"October", "November", "December" };
			for (PayrollUserPayslip payrollRow : payrollItemList) {
				ObjectNode row = Json.newObject();
				row.put("id", payrollRow.getId());
				row.put("empName", payrollRow.getUser().getFullName());
				String payrollMonth = months[payrollRow.getPayslipMonth() - 1];
				row.put("payrollMonth", payrollMonth);
				row.put("payrollYear", payrollRow.getPayslipYear());
				row.put("eligibleDays", payrollRow.getEligibleDays());
				if (payrollRow.getEarning1() != null)
					row.put("income1", payrollRow.getEarning1());
				else
					row.put("income1", "");

				if (payrollRow.getEarning2() != null)
					row.put("income2", payrollRow.getEarning2());
				else
					row.put("income2", "");

				if (payrollRow.getEarning3() != null)
					row.put("income3", payrollRow.getEarning3());
				else
					row.put("income3", "");

				if (payrollRow.getEarning4() != null)
					row.put("income4", payrollRow.getEarning4());
				else
					row.put("income4", "");

				if (payrollRow.getEarning5() != null)
					row.put("income5", payrollRow.getEarning5());
				else
					row.put("income5", "");

				if (payrollRow.getEarning6() != null)
					row.put("income6", payrollRow.getEarning6());
				else
					row.put("income6", "");

				if (payrollRow.getEarning7() != null)
					row.put("income7", payrollRow.getEarning7());
				else
					row.put("income7", "");

				if (payrollRow.getDeduction1() != null)
					row.put("deduction1", payrollRow.getDeduction1());
				else
					row.put("deduction1", "");

				if (payrollRow.getDeduction2() != null)
					row.put("deduction2", payrollRow.getDeduction2());
				else
					row.put("deduction2", "");

				if (payrollRow.getDeduction3() != null)
					row.put("deduction3", payrollRow.getDeduction3());
				else
					row.put("deduction3", "");

				if (payrollRow.getDeduction4() != null)
					row.put("deduction4", payrollRow.getDeduction4());
				else
					row.put("deduction4", "");

				if (payrollRow.getDeduction5() != null)
					row.put("deduction5", payrollRow.getDeduction5());
				else
					row.put("deduction5", "");

				if (payrollRow.getDeduction6() != null)
					row.put("deduction6", payrollRow.getDeduction6());
				else
					row.put("deduction6", "");

				if (payrollRow.getDeduction7() != null)
					row.put("deduction7", payrollRow.getDeduction7());
				else
					row.put("deduction7", "");

				if (payrollRow.getTotalEarning() != null)
					row.put("totalIncome", payrollRow.getTotalEarning());
				else
					row.put("totalIncome", "");

				if (payrollRow.getTotalDeduction() != null)
					row.put("totalDeduction", payrollRow.getTotalDeduction());
				else
					row.put("totalDeduction", "");

				if (payrollRow.getNetPay() != null)
					row.put("netPay", payrollRow.getNetPay());
				else
					row.put("netPay", "");

				payrollItemsan.add(row);
			}
			String sbquery1 = "select obj from PayrollSetup obj where obj.organization.id=?1 and obj.payrollType=?2 and obj.inForce = 1 and obj.presentStatus=1";
			ArrayList inparam1 = new ArrayList(2);
			inparam1.add(user.getOrganization().getId());
			inparam1.add(IdosConstants.PAYROLL_TYPE_EARNINGS);
			List<PayrollSetup> payrollSetupEarnList = genericDAO.queryWithParams(sbquery1, em, inparam1);
			for (PayrollSetup payrollEarnHead : payrollSetupEarnList) {
				ObjectNode row = Json.newObject();
				row.put("id", payrollEarnHead.getId());
				row.put("headName", payrollEarnHead.getPayrollHeadName());
				payrollEarnHeadsDataan.add(row);
			}

			inparam1.clear();
			inparam1.add(user.getOrganization().getId());
			inparam1.add(IdosConstants.PAYROLL_TYPE_DEDUCTIONS);
			List<PayrollSetup> payrollSetupDeduList = genericDAO.queryWithParams(sbquery1, em, inparam1);
			for (PayrollSetup payrollDeduHead : payrollSetupDeduList) {
				ObjectNode row = Json.newObject();
				row.put("id", payrollDeduHead.getId());
				row.put("headName", payrollDeduHead.getPayrollHeadName());
				payrollDeduHeadsDataan.add(row);
			}

		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getInvoiceData(Request request) {
		ObjectNode results = Json.newObject();
		// EntityManager entityManager=getEntityManager();
		Users user = getUserInfo(request);
		if (user == null) {
			return unauthorized(results);
		}
		try {
			ArrayNode txnItemsan = results.putArray("txnItemData");
			ArrayNode txnan = results.putArray("txnDetailsData");
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			// String useremail = json.findValue("useremail").asText();
			// String txnPurpose = json.findValue("txnPurposeText").asText();
			int txnPurposeVal = json.findValue("txnPurposeId").asInt();
			Long transactionEntityId = json.findValue("invTxnEntityId").asLong();
			// Long openingBalAdvId = (json.findValue("openingBalAdvId") == null
			// || "".equals(json.findValue("openingBalAdvId").asText())) ? null
			// : json.findValue("openingBalAdvId").asLong();
			// Long branchId = json.findValue("branchId").asLong();
			// Long custvendid = json.findValue("branchId").asLong();
			// if (transactionEntityId == -1) {
			// if (openingBalAdvId != null) {
			// ObjectNode row = Json.newObject();
			// ArrayNode txnTaxList = row.putArray("taxData");
			// ArrayNode txnAdvTaxList = row.putArray("advAdjTaxData");
			// CustomerBranchWiseAdvBalance openBal = CustomerBranchWiseAdvBalance
			// .findById(openingBalAdvId);
			// row.put("id", "-1");
			// row.put("openingBalAdvId", openBal.getId());
			// row.put("typeOfSupply", openBal.getTypeOfSupply());
			// row.put("placeOfSupply", openBal.getCustomerDetail().getId());
			// row.put("itemName", openBal.getSpecifics().getName());
			// row.put("itemId", openBal.getSpecifics().getId());
			// row.put("availableAdvance", openBal.getAdvanceAmount());
			// row.put("withholdingAmount", "");
			// row.put("netAmount", openBal.getOpeningBalance());
			// txnItemsan.add(row);

			// ObjectNode openBalDetails = Json.newObject();
			// openBalDetails.put("typeOfSupply", "");
			// openBalDetails.put("placeOfSupply", "");
			// openBalDetails.put("typeOfSupplyNo", openBal.getTypeOfSupply());
			// openBalDetails.put("placeOfSupplyGstin",
			// openBal.getCustomerDetail().getId());
			// openBalDetails.put("totalNetAmount", openBal.getAdvanceAmount());
			// openBalDetails.put("totalInvoiceValue", 0.00);
			// txnan.add(openBalDetails);
			// } else {
			// Branch selectedBranch = Branch.findById(branchId);
			// Vendor custoVendor = Vendor.findById(custvendid);
			// if (selectedBranch != null) {
			// criterias.clear();
			// criterias.put("organization.id", user.getOrganization().getId());
			// criterias.put("branch.id", selectedBranch.getId());
			// criterias.put("vendor.id", custoVendor.getId());
			// criterias.put("presentStatus", 1);
			// BranchVendors branchVendors = genericDAO.getByCriteria(BranchVendors.class,
			// criterias,
			// entityManager);
			// if (branchVendors != null) {
			// ObjectNode rowBranchOpBal = Json.newObject();
			// rowBranchOpBal.put("id", "-1");
			// rowBranchOpBal.put("openingBalAdvId", "");
			// rowBranchOpBal.put("typeOfSupply", "");
			// rowBranchOpBal.put("placeOfSupply", "");
			// rowBranchOpBal.put("itemName", "");
			// rowBranchOpBal.put("itemId", "-1");
			// rowBranchOpBal.put("availableAdvance",
			// branchVendors.getOpeningBalanceAdvPaid());
			// rowBranchOpBal.put("withholdingAmount", "");
			// rowBranchOpBal.put("netAmount",
			// branchVendors.getOriginalOpeningBalanceAdvPaid());
			// txnItemsan.add(rowBranchOpBal);

			// ObjectNode rowBrnOpBalDetailData = Json.newObject();
			// rowBranchOpBal.put("typeOfSupply", "");
			// rowBranchOpBal.put("placeOfSupply", "");
			// rowBranchOpBal.put("typeOfSupplyNo", "");
			// rowBranchOpBal.put("placeOfSupplyGstin", "");
			// rowBranchOpBal.put("totalNetAmount",
			// branchVendors.getOpeningBalanceAdvPaid());
			// rowBranchOpBal.put("totalInvoiceValue", 0.00);
			// txnan.add(rowBranchOpBal);
			// }
			// }
			// }
			// } else {
			Transaction txn = Transaction.findById(transactionEntityId);
			if (txn == null) {
				return Results.ok(results);
			}
			List<TransactionItems> listTransactionItems = TransactionItems.finfByTxnId(entityManager,
					transactionEntityId);
			double totalInvoiceValue = 0.0;
			boolean placeOfSupplyEntered = false;

			if (txnPurposeVal == 36) {
				// Transaction
				// transaction=genericDAO.getById(Transaction.class,transactionEntityId,
				// entityManager);

				ObjectNode txnRow = Json.newObject();

				txnRow.put("totalNetAmount", txn.getNetAmount());
				txnRow.put("totalInvoiceValue", totalInvoiceValue);
				txnan.add(txnRow);
				if (txn.getWithholdingTax() != null) {
					txnRow.put("withholdingAmount", txn.getWithholdingTax());
				} else {
					txnRow.put("withholdingAmount", "");
				}
				if (txn.getNetAmount() != null) {
					txnRow.put("netAmount", txn.getNetAmount());
				} else {
					txnRow.put("netAmount", "");
				}

				txnItemsan.add(txnRow);
				log.log(Level.INFO, "transaction details=" + txnItemsan.asText());
			} else {
				for (TransactionItems txnItemrow : listTransactionItems) {
					ObjectNode row = Json.newObject();
					ArrayNode txnTaxList = row.putArray("taxData");
					ArrayNode txnAdvTaxList = row.putArray("advAdjTaxData");
					log.log(Level.INFO, "transaction item=" + txnItemrow.getId());

					row.put("id", txnItemrow.getId());
					Transaction transaction = txnItemrow.getTransactionId();
					if (transaction.getTransactionPurpose().getId() == IdosConstants.PURCHASE_ORDER) {
						row.put("noOfUnits", txnItemrow.getNoOfUnits());
						row.put("pricePerUnit", IdosConstants.decimalFormat.format(txnItemrow.getPricePerUnit()));
						row.put("discountPer",
								txnItemrow.getDiscountPercent() == null ? "0.00" : txnItemrow.getDiscountPercent());
						row.put("discountAmt", IdosConstants.decimalFormat
								.format(txnItemrow.getDiscountAmount() == null ? 0.0
										: txnItemrow.getDiscountAmount()));
						row.put("grossAmount", IdosConstants.decimalFormat.format(txnItemrow.getGrossAmount()));
						if (txnItemrow.getTransactionSpecifics() != null) {
							row.put("itemName", txnItemrow.getTransactionSpecifics().getName());
							row.put("itemId", txnItemrow.getTransactionSpecifics().getId());
						} else {
							row.put("itemName", "");
							row.put("itemId", "");
						}
						row.put("taxDescription", txnItemrow.getTaxDescription());
						row.put("itemTotalTax", txnItemrow.getTotalTax());
						row.put("withholdingAmount",
								IdosConstants.decimalFormat.format(txnItemrow.getWithholdingAmount()));
						row.put("availableAdvance",
								IdosConstants.decimalFormat.format(txnItemrow.getAvailableAdvance()));
						row.put("adjFromAdvance",
								IdosConstants.decimalFormat.format(txnItemrow.getAdjustmentFromAdvance()));
						row.put("netAmount", IdosConstants.decimalFormat.format(txnItemrow.getNetAmount()));

						if (placeOfSupplyEntered == false) {
							String destGstinId = transaction.getDestinationGstin() != null
									? transaction.getDestinationGstin()
									: "";
							row.put("placeOfSupply", destGstinId);
							placeOfSupplyEntered = true;
						}

					} else if ((IdosConstants.CREDIT_NOTE_CUSTOMER == txnPurposeVal
							|| IdosConstants.DEBIT_NOTE_CUSTOMER == txnPurposeVal)
							&& (IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER == transaction
									.getTransactionPurpose().getId())
							|| IdosConstants.CANCEL_INVOICE == txnPurposeVal) {
						double noOfUnits = txnItemrow.getNoOfUnits();
						row.put("noOfUnits", noOfUnits);
						row.put("pricePerUnit", IdosConstants.decimalFormat.format(txnItemrow.getPricePerUnit()));
						row.put("discountPer",
								txnItemrow.getDiscountPercent() == null ? "0.00" : txnItemrow.getDiscountPercent());
						row.put("discountAmt", IdosConstants.decimalFormat
								.format(txnItemrow.getDiscountAmount() == null ? 0.0
										: txnItemrow.getDiscountAmount()));
						double gross = txnItemrow.getGrossAmount();
						row.put("grossAmount", IdosConstants.decimalFormat.format(gross));
						if (txnItemrow.getTotalTax() != null && !"".equals(txnItemrow.getTotalTax())) {
							row.put("itemTotalTax", IdosConstants.decimalFormat.format(txnItemrow.getTotalTax()));
						}
						if (txnItemrow.getTransactionSpecifics() != null) {
							row.put("itemName", txnItemrow.getTransactionSpecifics().getName());
							row.put("itemId", txnItemrow.getTransactionSpecifics().getId());
						} else {
							row.put("itemName", "");
							row.put("itemId", "");
						}
						row.put("taxDescription", txnItemrow.getTaxDescription());

						if (txnItemrow.getTaxName1() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName1());
							if (txnItemrow.getTransactionSpecifics().getIsCombinationSales() != null
									&& txnItemrow.getTransactionSpecifics().getIsCombinationSales() == 1) {
								taxRow.put("taxRate", "-");
							} else {
								taxRow.put("taxRate", IdosConstants.decimalFormat.format(txnItemrow.getTaxRate1()));
							}
							taxRow.put("taxid", txnItemrow.getTax1ID());
							taxRow.put("taxAmount", IdosConstants.decimalFormat.format(txnItemrow.getTaxValue1()));
							txnTaxList.add(taxRow);
						}
						if (txnItemrow.getTaxName2() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName2());
							if (txnItemrow.getTransactionSpecifics().getIsCombinationSales() != null
									&& txnItemrow.getTransactionSpecifics().getIsCombinationSales() == 1) {
								taxRow.put("taxRate", "-");
							} else {
								taxRow.put("taxRate", txnItemrow.getTaxRate2());
							}
							taxRow.put("taxid", txnItemrow.getTax2ID());
							taxRow.put("taxAmount", IdosConstants.decimalFormat.format(txnItemrow.getTaxValue2()));
							txnTaxList.add(taxRow);
						}
						if (txnItemrow.getTaxName3() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName3());
							if (txnItemrow.getTransactionSpecifics().getIsCombinationSales() != null
									&& txnItemrow.getTransactionSpecifics().getIsCombinationSales() == 1) {
								taxRow.put("taxRate", "-");
							} else {
								taxRow.put("taxRate", txnItemrow.getTaxRate3());
							}
							taxRow.put("taxid", txnItemrow.getTax3ID());
							taxRow.put("taxAmount", IdosConstants.decimalFormat.format(txnItemrow.getTaxValue3()));
							txnTaxList.add(taxRow);
						}
						if (txnItemrow.getTaxName4() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName4());
							if (txnItemrow.getTransactionSpecifics().getIsCombinationSales() != null
									&& txnItemrow.getTransactionSpecifics().getIsCombinationSales() == 1) {
								taxRow.put("taxRate", "-");
							} else {
								taxRow.put("taxRate", txnItemrow.getTaxRate4());
							}
							taxRow.put("taxid", txnItemrow.getTax4ID());
							taxRow.put("taxAmount", IdosConstants.decimalFormat.format(txnItemrow.getTaxValue4()));
							txnTaxList.add(taxRow);
						}
						if (txnItemrow.getAdvAdjTax1Value() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName1());
							taxRow.put("taxAmount",
									IdosConstants.decimalFormat.format(txnItemrow.getAdvAdjTax1Value()));
							txnAdvTaxList.add(taxRow);
						}
						if (txnItemrow.getAdvAdjTax2Value() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName2());
							taxRow.put("taxAmount",
									IdosConstants.decimalFormat.format(txnItemrow.getAdvAdjTax2Value()));
							txnAdvTaxList.add(taxRow);
						}
						if (txnItemrow.getAdvAdjTax3Value() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName3());
							taxRow.put("taxAmount",
									IdosConstants.decimalFormat.format(txnItemrow.getAdvAdjTax3Value()));
							txnAdvTaxList.add(taxRow);
						}
						if (txnItemrow.getAdvAdjTax4Value() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName4());
							taxRow.put("taxAmount",
									IdosConstants.decimalFormat.format(txnItemrow.getAdvAdjTax4Value()));
							txnAdvTaxList.add(taxRow);
						}
						row.put("availableAdvance", txnItemrow.getAvailableAdvance());
						row.put("adjFromAdvance", txnItemrow.getAdjustmentFromAdvance());
						row.put("netAmount", IdosConstants.decimalFormat.format(txnItemrow.getNetAmount()));

						if (txnItemrow.getInvoiceValue() != null) {
							row.put("invoiceValue",
									IdosConstants.decimalFormat.format(txnItemrow.getInvoiceValue()));
							totalInvoiceValue += txnItemrow.getInvoiceValue();
						} else {
							row.put("invoiceValue", 0.0);
						}
					} else if ((IdosConstants.CREDIT_NOTE_VENDOR == txnPurposeVal
							|| IdosConstants.DEBIT_NOTE_VENDOR == txnPurposeVal)
							&& (IdosConstants.BUY_ON_CREDIT_PAY_LATER == transaction.getTransactionPurpose()
									.getId())) {
						double noOfUnits = txnItemrow.getNoOfUnits();
						row.put("noOfUnits", noOfUnits);
						row.put("pricePerUnit", IdosConstants.decimalFormat.format(txnItemrow.getPricePerUnit()));
						row.put("discountPer",
								txnItemrow.getDiscountPercent() == null ? "0.00" : txnItemrow.getDiscountPercent());
						row.put("discountAmt", IdosConstants.decimalFormat
								.format(txnItemrow.getDiscountAmount() == null ? 0.0
										: txnItemrow.getDiscountAmount()));
						row.put("dutiesAndTaxes",
								txnItemrow.getTaxValue7() == null ? "" : txnItemrow.getTaxValue7().toString());
						row.put("DutiesTaxName", txnItemrow.getTaxName7() == null ? "" : txnItemrow.getTaxName7());
						row.put("rcmTaxId", txnItemrow.getReverseChargeItemId() == null ? ""
								: txnItemrow.getReverseChargeItemId().toString());
						if (txnItemrow.getReverseChargeItemId() != null) {
							BranchSpecificsTaxFormula formula = BranchSpecificsTaxFormula
									.findById(txnItemrow.getReverseChargeItemId());
							if (formula != null) {
								row.put("rcmTaxName", formula.getHsnDesc() == null ? "" : formula.getHsnDesc());
							} else {
								row.put("rcmTaxName", "");
							}
						} else {
							row.put("rcmTaxName", "");
						}

						double gross = txnItemrow.getGrossAmount();
						row.put("grossAmount", IdosConstants.decimalFormat.format(gross));
						if (txnItemrow.getTotalTax() != null) {
							row.put("itemTotalTax", IdosConstants.decimalFormat.format(txnItemrow.getTotalTax()));
						} else {
							row.put("itemTotalTax", "0.00");
						}
						if (txnItemrow.getTransactionSpecifics() != null) {
							row.put("itemName", txnItemrow.getTransactionSpecifics().getName());
							row.put("itemId", txnItemrow.getTransactionSpecifics().getId());
						} else {
							row.put("itemName", "");
							row.put("itemId", "");
						}

						row.put("taxDescription", txnItemrow.getTaxDescription());
						Double sgstCgstRate = 0.0;
						if (txnItemrow.getTaxName1() != null && txnItemrow.getTaxValue1() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName1());
							taxRow.put("taxRate", txnItemrow.getTaxRate1());
							taxRow.put("taxid", txnItemrow.getTax1ID());
							taxRow.put("taxAmount", IdosConstants.decimalFormat.format(txnItemrow.getTaxValue1()));
							sgstCgstRate = txnItemrow.getTaxRate1();
							txnTaxList.add(taxRow);
						}
						if (txnItemrow.getTaxName2() != null && txnItemrow.getTaxValue2() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName2());
							taxRow.put("taxRate", txnItemrow.getTaxRate2());
							taxRow.put("taxid", txnItemrow.getTax2ID());
							taxRow.put("taxAmount", IdosConstants.decimalFormat.format(txnItemrow.getTaxValue2()));
							sgstCgstRate += txnItemrow.getTaxRate2();
							txnTaxList.add(taxRow);
						}
						if (txnItemrow.getTaxName3() != null && txnItemrow.getTaxValue3() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName3());
							taxRow.put("taxRate", txnItemrow.getTaxRate3());
							taxRow.put("taxid", txnItemrow.getTax3ID());
							taxRow.put("taxAmount", IdosConstants.decimalFormat.format(txnItemrow.getTaxValue3()));
							sgstCgstRate = txnItemrow.getTaxRate3();
							txnTaxList.add(taxRow);
						}
						if (txnItemrow.getTaxName4() != null && txnItemrow.getTaxValue4() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName4());
							taxRow.put("taxRate", txnItemrow.getTaxRate4());
							taxRow.put("taxid", txnItemrow.getTax4ID());
							taxRow.put("taxAmount", IdosConstants.decimalFormat.format(txnItemrow.getTaxValue4()));
							txnTaxList.add(taxRow);
						}
						if (txnItemrow.getAdvAdjTax1Value() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName1());
							taxRow.put("taxAmount",
									IdosConstants.decimalFormat.format(txnItemrow.getAdvAdjTax1Value()));
							txnAdvTaxList.add(taxRow);
						}
						if (txnItemrow.getAdvAdjTax2Value() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName2());
							taxRow.put("taxAmount",
									IdosConstants.decimalFormat.format(txnItemrow.getAdvAdjTax2Value()));
							txnAdvTaxList.add(taxRow);
						}
						if (txnItemrow.getAdvAdjTax3Value() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName3());
							taxRow.put("taxAmount",
									IdosConstants.decimalFormat.format(txnItemrow.getAdvAdjTax3Value()));
							txnAdvTaxList.add(taxRow);
						}
						if (txnItemrow.getAdvAdjTax4Value() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName4());
							taxRow.put("taxAmount",
									IdosConstants.decimalFormat.format(txnItemrow.getAdvAdjTax4Value()));
							txnAdvTaxList.add(taxRow);
						}
						row.put("sgstCgstRate", sgstCgstRate);
						row.put("availableAdvance", txnItemrow.getAvailableAdvance() == null ? "0.00"
								: IdosConstants.decimalFormat.format(txnItemrow.getAvailableAdvance()));
						row.put("adjFromAdvance", txnItemrow.getAdjustmentFromAdvance() == null ? "0.00"
								: IdosConstants.decimalFormat.format(txnItemrow.getAdjustmentFromAdvance()));
						row.put("netAmount", txnItemrow.getNetAmount() == null ? "0.00"
								: IdosConstants.decimalFormat.format(txnItemrow.getNetAmount()));
						row.put("withholdingAmount", txnItemrow.getWithholdingAmount() == null ? "0.00"
								: IdosConstants.decimalFormat.format(txnItemrow.getWithholdingAmount()));
						if (txnItemrow.getInvoiceValue() != null) {
							row.put("invoiceValue",
									IdosConstants.decimalFormat.format(txnItemrow.getInvoiceValue()));
							totalInvoiceValue += txnItemrow.getInvoiceValue();
						} else {
							row.put("invoiceValue", 0.0);
						}

						VendorTDSTaxes vendTdsBasic = null;
						if (txnItemrow.getTransactionId().getTransactionVendorCustomer() != null
								&& txnItemrow.getTransactionSpecifics() != null) {
							vendTdsBasic = VendorTDSTaxes.findByOrgVend(em, user.getOrganization().getId(),
									txnItemrow.getTransactionId().getTransactionVendorCustomer().getId(),
									txnItemrow.getTransactionSpecifics(), new Date());
						}
						if (vendTdsBasic != null) {
							row.put("modeOfTdsCompute", vendTdsBasic.getModeOfComputation());
						} else {
							row.put("modeOfTdsCompute", "");
						}
					} else if (IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER == txnPurposeVal) {
						boolean isInventoryItemFound = false;
						double noOfUnits = txnItemrow.getNoOfUnits();
						row.put("noOfUnits", noOfUnits);
						row.put("pricePerUnit", txnItemrow.getPricePerUnit());
						row.put("discountPer",
								txnItemrow.getDiscountPercent() == null ? "0.00" : txnItemrow.getDiscountPercent());
						row.put("discountAmt",
								txnItemrow.getDiscountAmount() == null ? 0.0 : txnItemrow.getDiscountAmount());
						double gross = txnItemrow.getGrossAmount();
						row.put("grossAmount", gross);
						row.put("itemTotalTax", txnItemrow.getTotalTax());
						if (txnItemrow.getTransactionSpecifics() != null) {
							if (txnItemrow.getTransactionSpecifics().getLinkIncomeExpenseSpecifics() != null) {
								row.put("itemName",
										txnItemrow.getTransactionSpecifics().getLinkIncomeExpenseSpecifics()
												.getName());
								row.put("itemId",
										txnItemrow.getTransactionSpecifics().getLinkIncomeExpenseSpecifics()
												.getId());
								isInventoryItemFound = true;
							} else {
								row.put("itemName", txnItemrow.getTransactionSpecifics().getName());
								row.put("itemId", txnItemrow.getTransactionSpecifics().getId());
							}
						} else {
							isInventoryItemFound = true;
							row.put("itemName", "");
							row.put("itemId", "");
						}
						row.put("taxDescription", txnItemrow.getTaxDescription());
						Double sgstCgstRate = 0.0;
						if (txnItemrow.getTaxName1() != null && txnItemrow.getTaxValue1() != null) {
							BranchTaxes inputTax = BranchTaxes.findByRateType(em, txnItemrow.getBranch().getId(),
									txnItemrow.getOrganization().getId(), IdosConstants.INPUT_SGST,
									txnItemrow.getTaxRate1());
							if (inputTax != null) {
								ObjectNode taxRow = Json.newObject();
								taxRow.put("taxName", txnItemrow.getTaxName1());
								taxRow.put("taxRate", txnItemrow.getTaxRate1());
								taxRow.put("taxid", inputTax.getId());
								taxRow.put("taxAmount", txnItemrow.getTaxValue1());
								sgstCgstRate = txnItemrow.getTaxRate1();
								txnTaxList.add(taxRow);
							} else {
								isInventoryItemFound = false;
							}
						}
						if (txnItemrow.getTaxName2() != null && txnItemrow.getTaxValue2() != null) {
							BranchTaxes inputTax = BranchTaxes.findByRateType(em, txnItemrow.getBranch().getId(),
									txnItemrow.getOrganization().getId(), IdosConstants.INPUT_CGST,
									txnItemrow.getTaxRate2());
							if (inputTax != null) {
								ObjectNode taxRow = Json.newObject();
								taxRow.put("taxName", txnItemrow.getTaxName2());
								taxRow.put("taxRate", txnItemrow.getTaxRate2());
								taxRow.put("taxid", inputTax.getId());
								taxRow.put("taxAmount", txnItemrow.getTaxValue2());
								sgstCgstRate += txnItemrow.getTaxRate2();
								txnTaxList.add(taxRow);
							} else {
								isInventoryItemFound = false;
							}
						}
						if (txnItemrow.getTaxName3() != null && txnItemrow.getTaxValue3() != null) {
							BranchTaxes inputTax = BranchTaxes.findByRateType(em, txnItemrow.getBranch().getId(),
									txnItemrow.getOrganization().getId(), IdosConstants.INPUT_IGST,
									txnItemrow.getTaxRate3());
							if (inputTax != null) {
								ObjectNode taxRow = Json.newObject();
								taxRow.put("taxName", txnItemrow.getTaxName3());
								taxRow.put("taxRate", txnItemrow.getTaxRate3());
								taxRow.put("taxid", inputTax.getId());
								taxRow.put("taxAmount", txnItemrow.getTaxValue3());
								txnTaxList.add(taxRow);
							} else {
								isInventoryItemFound = false;
							}
						}
						if (txnItemrow.getTaxName4() != null && txnItemrow.getTaxValue4() != null) {
							BranchTaxes inputTax = BranchTaxes.findByRateType(em, txnItemrow.getBranch().getId(),
									txnItemrow.getOrganization().getId(), IdosConstants.INPUT_CESS,
									txnItemrow.getTaxRate4());
							if (inputTax != null) {
								ObjectNode taxRow = Json.newObject();
								taxRow.put("taxName", txnItemrow.getTaxName4());
								taxRow.put("taxRate", txnItemrow.getTaxRate4());
								taxRow.put("taxid", inputTax.getId());
								taxRow.put("taxAmount", txnItemrow.getTaxValue4());
								txnTaxList.add(taxRow);
							} else {
								isInventoryItemFound = false;
							}
						}
						if (txnItemrow.getAdvAdjTax1Value() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName1());
							taxRow.put("taxAmount", txnItemrow.getAdvAdjTax1Value());
							txnAdvTaxList.add(taxRow);
						}
						if (txnItemrow.getAdvAdjTax2Value() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName2());
							taxRow.put("taxAmount", txnItemrow.getAdvAdjTax2Value());
							txnAdvTaxList.add(taxRow);
						}
						if (txnItemrow.getAdvAdjTax3Value() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName3());
							taxRow.put("taxAmount", txnItemrow.getAdvAdjTax3Value());
							txnAdvTaxList.add(taxRow);
						}
						if (txnItemrow.getAdvAdjTax4Value() != null) {
							ObjectNode taxRow = Json.newObject();
							taxRow.put("taxName", txnItemrow.getTaxName4());
							taxRow.put("taxAmount", txnItemrow.getAdvAdjTax4Value());
							txnAdvTaxList.add(taxRow);
						}
						row.put("sgstCgstRate", sgstCgstRate);
						row.put("availableAdvance", txnItemrow.getAvailableAdvance());
						row.put("adjFromAdvance", txnItemrow.getAdjustmentFromAdvance());
						row.put("netAmount", txnItemrow.getNetAmount());
						row.put("withholdingAmount", txnItemrow.getWithholdingAmount());
						if (txnItemrow.getInvoiceValue() != null) {
							row.put("invoiceValue", txnItemrow.getInvoiceValue());
							totalInvoiceValue += txnItemrow.getInvoiceValue();
						} else {
							row.put("invoiceValue", 0.0);
						}
						if (isInventoryItemFound) {
							txnItemsan.add(row);
						}
					} else if (IdosConstants.SALES_RETURNS == transaction.getTransactionPurpose().getId()) {
						double noOfUnitsLeftAfterReturn = txnItemrow.getNoOfUnits();
						if (txnItemrow.getNoOfUnitsReturned() != null) {
							noOfUnitsLeftAfterReturn = noOfUnitsLeftAfterReturn - txnItemrow.getNoOfUnitsReturned();
						}
						row.put("noOfUnits", noOfUnitsLeftAfterReturn);
						row.put("pricePerUnit", txnItemrow.getPricePerUnit());
						row.put("discountPer",
								txnItemrow.getDiscountPercent() == null ? "0.00" : txnItemrow.getDiscountPercent());
						row.put("discountAmt",
								txnItemrow.getDiscountAmount() == null ? 0.0 : txnItemrow.getDiscountAmount());
						double grossAfterReturn = txnItemrow.getGrossAmount();
						if (txnItemrow.getGrossAmounReturned() != null) {
							grossAfterReturn = grossAfterReturn - txnItemrow.getGrossAmounReturned();
						}
						row.put("grossAmount", grossAfterReturn);
						if (txnItemrow.getTransactionSpecifics() != null) {
							row.put("itemName", txnItemrow.getTransactionSpecifics().getName());
							row.put("itemId", txnItemrow.getTransactionSpecifics().getId());
						} else {
							row.put("itemName", "");
							row.put("itemId", "");
						}
						row.put("taxDescription", txnItemrow.getTaxDescription());
						double taxAfterReturn = txnItemrow.getTotalTax();
						if (txnItemrow.getTotalTaxReturned() != null) {
							taxAfterReturn = taxAfterReturn - txnItemrow.getTotalTaxReturned();
						}
						row.put("itemTotalTax", taxAfterReturn);
						row.put("withholdingAmount", txnItemrow.getWithholdingAmount());
						row.put("availableAdvance", txnItemrow.getAvailableAdvance());
						row.put("adjFromAdvance", txnItemrow.getAdjustmentFromAdvance());
						double netAmtReturn = txnItemrow.getNetAmount();
						if (txnItemrow.getNetAmountReturned() != null) {
							netAmtReturn = netAmtReturn - txnItemrow.getNetAmountReturned();
						}
						row.put("netAmount", netAmtReturn);
					} else if (IdosConstants.REFUND_ADVANCE_RECEIVED == txnPurposeVal) {
						row.put("typeOfSupply", transaction.getTypeOfSupply());

						if (placeOfSupplyEntered == false) {
							String destGstinId = transaction.getDestinationGstin() != null
									? transaction.getDestinationGstin()
									: "";

							row.put("placeOfSupply", destGstinId);
							placeOfSupplyEntered = true;
						}

						if (txnItemrow.getTransactionSpecifics() != null) {
							row.put("itemName", txnItemrow.getTransactionSpecifics().getName());
							row.put("itemId", txnItemrow.getTransactionSpecifics().getId());
						} else {
							row.put("itemName", "");
							row.put("itemId", "");
						}
						Double availableAdvance = txnItemrow.getAvailableAdvance();
						Double adjustAdvance = txnItemrow.getAdjustmentFromAdvance();
						Double diff = 0d;
						Double withholdingAmt = txnItemrow.getWithholdingAmount();
						Double withholdingAmtReturned = txnItemrow.getWithholdingAmountReturned();
						Double tdsDiff = 0d;

						if (availableAdvance != null && availableAdvance > 0) {
							diff += availableAdvance;
						}
						if (adjustAdvance != null && adjustAdvance > 0) {
							diff -= adjustAdvance;
						}

						if (diff >= 0) {
							row.put("availableAdvance", IdosConstants.decimalFormat.format(diff));
						} else {
							row.put("availableAdvance", 0);
						}

						if (withholdingAmt != null && withholdingAmt > 0) {
							tdsDiff += withholdingAmt;
						}
						if (withholdingAmtReturned != null && withholdingAmtReturned > 0) {
							tdsDiff -= withholdingAmtReturned;
						}

						if (tdsDiff >= 0) {
							row.put("withholdingAmount", IdosConstants.decimalFormat.format(tdsDiff));
						} else {
							row.put("withholdingAmount", 0);
						}
						if (txnItemrow.getNetAmount() != null) {
							row.put("netAmount", IdosConstants.decimalFormat.format(txnItemrow.getNetAmount()));
						} else {
							row.put("netAmount", "");
						}
					}
					if (txnPurposeVal != IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
						txnItemsan.add(row);
					} else if (IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE == txnPurposeVal) {
						if (txnItemrow.getTransactionSpecifics() != null) {
							row.put("itemName", txnItemrow.getTransactionSpecifics().getName());
							row.put("itemId", txnItemrow.getTransactionSpecifics().getId());
						} else {
							row.put("itemName", "");
							row.put("itemId", "");
						}
						/*
						 * Double availableAdvance = txnItemrow.get
						 * Double adjustAdvance = txnItemrow.getAdjustmentFromAdvance();
						 * Double advanceRefund = txnItemrow.getAdjustmentFromAdvanceReturned();
						 */
						// criterias.clear();
						// criterias.put("vendorSpecific.id",
						// transaction.getTransactionVendorCustomer().getId());
						// criterias.put("specificsVendors.id",
						// txnItemrow.getTransactionSpecifics().getId());
						// criterias.put("organization.id", user.getOrganization().getId());
						// VendorSpecific customerTxnSpecifics =
						// genericDAO.getByCriteria(VendorSpecific.class, criterias, entityManager);
						// Double totaladvanceMoneyAvailable = customerTxnSpecifics.getAdvanceMoney();

						// criterias.clear();
						// criterias.put("organization.id",
						// transaction.getTransactionBranchOrganization().getId());
						// criterias.put("branch.id",transaction.getTransactionBranch().getId());
						// criterias.put("advTransaction.id", transaction.id);
						Double diff = 0d;

						/*
						 * if(availableAdvance != null && availableAdvance > 0) {
						 * diff += availableAdvance;
						 * }
						 * if(adjustAdvance != null && adjustAdvance > 0) {
						 * diff -= adjustAdvance;
						 * }
						 * if(advanceRefund != null && advanceRefund > 0) {
						 * diff -= advanceRefund;
						 * }
						 * if(diff >= 0) {
						 * row.put("availableAdvance", diff);
						 * }else {
						 * row.put("availableAdvance", 0);
						 * }
						 */

						if (txnItemrow.getWithholdingAmount() != null) {
							row.put("withholdingAmount", txnItemrow.getWithholdingAmount());
						} else {
							row.put("withholdingAmount", "");
						}
						if (txnItemrow.getNetAmount() != null) {
							row.put("netAmount", txnItemrow.getNetAmount());
						} else {
							row.put("netAmount", "");
						}

						txnItemsan.add(row);
						log.log(Level.INFO, "transaction details=" + txnItemsan.asText());
					}
				}
				ObjectNode txnRow = Json.newObject();
				if (txnPurposeVal != IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
					TRIAL_BALANCE_SERVICE.fetchTxnGSTDetails(em, txn, user, txnRow);
				}
				txnRow.put("totalNetAmount", txn.getNetAmount());
				txnRow.put("totalInvoiceValue", totalInvoiceValue);
				txnan.add(txnRow);
			}
			// }
		} catch (

		Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "========= End" + results);
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	/*
	 * @Transactional
	 * public static void insertListOfMultipleItemsIntoTransactionItems(JSONArray
	 * arrJSON,Transaction transaction) throws IDOSException{
	 * // EntityManager entityManager=getEntityManager();
	 * try{
	 * Users user=getUserInfo(request);
	 * for(int i=0;i<arrJSON.length();i++){
	 * Double soccpnhowmuchfromadvance=0.0;Double txnTaxAmount=0.0;Double
	 * customerAdvance=0.0;String txnTaxDesc="";Double withholdingAmount=0.0;
	 * TransactionItems transactionItem = new TransactionItems();
	 * JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
	 * Long itemId =rowItemData.getLong("txnItems");
	 * Specifics txnItem=genericDAO.getById(Specifics.class, itemId, entityManager);
	 * Double txnPerUnitPrice=rowItemData.getDouble("txnPerUnitPrice");
	 * Double txnNoOfUnit=rowItemData.getDouble("txnNoOfUnit");
	 * Double txnGross=rowItemData.getDouble("txnGross");
	 * if(!rowItemData.isNull("txnTaxDesc") &&
	 * !rowItemData.get("txnTaxDesc").equals("")){
	 * txnTaxDesc=rowItemData.getString("txnTaxDesc");
	 * }
	 * if(!rowItemData.isNull("txnTaxAmount") &&
	 * !rowItemData.get("txnTaxAmount").equals("")){
	 * txnTaxAmount=rowItemData.getDouble("txnTaxAmount");
	 * }
	 * if(!rowItemData.isNull("withholdingAmount") &&
	 * !rowItemData.get("withholdingAmount").equals("")){
	 * withholdingAmount=rowItemData.getDouble("withholdingAmount");
	 * }
	 * if(!rowItemData.isNull("customerAdvance") &&
	 * !rowItemData.get("customerAdvance").equals("")){
	 * customerAdvance=rowItemData.getDouble("customerAdvance");
	 * }
	 * if(!rowItemData.isNull("soccpnhowmuchfromadvance") &&
	 * !rowItemData.get("soccpnhowmuchfromadvance").equals("")){
	 * soccpnhowmuchfromadvance=rowItemData.getDouble("soccpnhowmuchfromadvance");
	 * }
	 * Double soccpnnetamnt=0d;
	 * if(!rowItemData.isNull("soccpnhowmuchfromadvance") &&
	 * !rowItemData.get("soccpnhowmuchfromadvance").equals("")){
	 * soccpnnetamnt=rowItemData.getDouble("soccpnnetamnt");
	 * }
	 * //transactionItem.setTransactionId(transaction.getId());
	 * transactionItem.setTransactionId(transaction);
	 * transactionItem.setTransactionSpecifics(txnItem);
	 * transactionItem.setTransactionParticulars(txnItem.getParticularsId());
	 * transactionItem.setOrganization(transaction.getTransactionBranchOrganization(
	 * ));
	 * transactionItem.setBranch(transaction.getTransactionBranch());
	 * transactionItem.setPricePerUnit(txnPerUnitPrice);
	 * transactionItem.setNoOfUnits(txnNoOfUnit);
	 * transactionItem.setGrossAmount(txnGross);
	 * transactionItem.setTaxDescription(txnTaxDesc);
	 * transactionItem.setTotalTax(txnTaxAmount);
	 * transactionItem.setWithholdingAmount(withholdingAmount);
	 * transactionItem.setAvailableAdvance(customerAdvance);
	 * transactionItem.setAdjustmentFromAdvance(soccpnhowmuchfromadvance);
	 * transactionItem.setNetAmount(soccpnnetamnt);
	 * genericDAO.saveOrUpdate(transactionItem, user, entityManager);
	 * 
	 * //advance adjustment
	 * if(soccpnhowmuchfromadvance != 0.0){
	 * Map<String, Object> criterias = new HashMap<String, Object>();
	 * criterias.put("vendorSpecific.id",
	 * transaction.getTransactionVendorCustomer().getId());
	 * criterias.put("specificsVendors.id", txnItem.getId());
	 * criterias.put("organization.id", user.getOrganization().getId());
	 * VendorSpecific
	 * customerTxnSpecifics=genericDAO.getByCriteria(VendorSpecific.class,
	 * criterias, entityManager);
	 * customerTxnSpecifics.setAdvanceMoney(customerTxnSpecifics.getAdvanceMoney()-
	 * soccpnhowmuchfromadvance);
	 * genericDAO.saveOrUpdate(customerTxnSpecifics, user, entityManager);
	 * }
	 * }
	 * }
	 * catch(Exception ex){
	 * log.log(Level.SEVERE, "Error", ex);
	 * throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE,
	 * IdosConstants.TECHNICAL_EXCEPTION, "Error on save/update multiitems.",
	 * ex.getMessage());
	 * }
	 * }
	 */
}
