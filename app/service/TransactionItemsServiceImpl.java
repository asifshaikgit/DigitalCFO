package service;

import java.util.*;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import controllers.TransactionController;
import model.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.logging.Level;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;

import pojo.TaxPojo;

/**
 * Created by Sunil Namdev on 05-12-2016.
 */
public class TransactionItemsServiceImpl implements TransactionItemsService {
	@Override
	public void insertMultipleItemsTransactionItems(EntityManager entityManager, Users user, JSONArray arrJSON,
			Transaction transaction, Date txnDate) throws IDOSException {
		try {
			Long txnPurpose = transaction.getTransactionPurpose().getId();
			List<TransactionItems> transactionitems = new ArrayList<TransactionItems>();
			double totalNetAmountVal = 0.0;
			for (int i = 0; i < arrJSON.length(); i++) {
				Double howMuchAdvance = 0.0;
				Double txnTaxAmount = 0.0;
				Double customerAdvance = 0.0;
				Double withholdingAmount = 0.0;
				Double txnInvoiceValue = 0.0;

				JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
				TransactionItems transactionItem = new TransactionItems();
				Long itemId = rowItemData.getLong("txnItems");

				Long txnRcmTaxItemID = (rowItemData.isNull("txnRcmTaxItemID") == true
						|| rowItemData.getString("txnRcmTaxItemID").isEmpty()) ? 0L
								: rowItemData.getLong("txnRcmTaxItemID");
				Specifics txnItem = genericDAO.getById(Specifics.class, itemId, entityManager);
				Double txnPerUnitPrice = rowItemData.getDouble("txnPerUnitPrice");
				Double txnNoOfUnit = rowItemData.getDouble("txnNoOfUnit");
				Double txnGross = rowItemData.getDouble("txnGross");
				String txnTaxDesc = "";
				if (!rowItemData.isNull("txnTaxDesc") && !rowItemData.get("txnTaxDesc").equals("")) {
					txnTaxDesc = rowItemData.getString("txnTaxDesc");
				}
				transactionItem.setTaxDescription(txnTaxDesc);
				TaxPojo taxPojo = null;
				if (txnPurpose == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
						|| txnPurpose == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
						|| IdosConstants.CREDIT_NOTE_CUSTOMER == txnPurpose
						|| IdosConstants.DEBIT_NOTE_CUSTOMER == txnPurpose
						|| (IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER == txnPurpose
								&& transaction.getTypeIdentifier() == 1)
						|| IdosConstants.CANCEL_INVOICE == txnPurpose) {
					if (user.getOrganization().getGstCountryCode() != null
							&& !"".equals(user.getOrganization().getGstCountryCode())) {
						taxPojo = saveTransactionTaxes(transactionItem, rowItemData, transaction);
					} else {
						setSellTaxInfo(transactionItem);
					}
				} else if (txnPurpose == IdosConstants.PREPARE_QUOTATION
						|| txnPurpose == IdosConstants.PROFORMA_INVOICE) {
					setSellTaxInfo(transactionItem);
				} else if (txnPurpose == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
						|| txnPurpose == IdosConstants.BUY_ON_CREDIT_PAY_LATER
						|| txnPurpose == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT
						|| txnPurpose == IdosConstants.CREDIT_NOTE_VENDOR
						|| txnPurpose == IdosConstants.DEBIT_NOTE_VENDOR
						|| (IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER == txnPurpose
								&& transaction.getTypeIdentifier() == 2)) {
					if (user.getOrganization().getGstCountryCode() != null
							&& !"".equals(user.getOrganization().getGstCountryCode())) {
						taxPojo = saveTransactionTaxes(transactionItem, rowItemData, transaction);
					} else {
						setBuyTaxInfo(transactionItem);
					}
				}
				String taxNameOnAdvAdj = "";
				if (!rowItemData.isNull("txnTaxNameOnAdvAdj") && !rowItemData.get("txnTaxNameOnAdvAdj").equals("")) {
					taxNameOnAdvAdj = rowItemData.getString("txnTaxNameOnAdvAdj");
				}
				String taxNameOnAdvAdjArray[] = taxNameOnAdvAdj.split(",");
				Double txnTaxOnAdvAdj = 0d;
				String taxOnAdvAdj = "";
				if (!rowItemData.isNull("txnTaxOnAdvAdj") && !rowItemData.get("txnTaxOnAdvAdj").equals("")) {
					taxOnAdvAdj = rowItemData.getString("txnTaxOnAdvAdj");
				}
				String taxOnAdvAdjArray[] = taxOnAdvAdj.split(",");
				for (int count = 0; count < taxOnAdvAdjArray.length; count++) {
					if (!"".equals(taxOnAdvAdjArray[count])) {
						txnTaxOnAdvAdj = IdosUtil.convertStringToDouble(taxOnAdvAdjArray[count]);
						if (transactionItem.getTaxName1() != null
								&& transactionItem.getTaxName1().indexOf(taxNameOnAdvAdjArray[count]) != -1) {
							transactionItem.setAdvAdjTax1Value(txnTaxOnAdvAdj);
						} else if (transactionItem.getTaxName2() != null
								&& transactionItem.getTaxName2().indexOf(taxNameOnAdvAdjArray[count]) != -1) {
							transactionItem.setAdvAdjTax2Value(txnTaxOnAdvAdj);
						} else if (transactionItem.getTaxName3() != null
								&& transactionItem.getTaxName3().indexOf(taxNameOnAdvAdjArray[count]) != -1) {
							transactionItem.setAdvAdjTax3Value(txnTaxOnAdvAdj);
						} else if (transactionItem.getTaxName4() != null
								&& transactionItem.getTaxName4().indexOf(taxNameOnAdvAdjArray[count]) != -1) {
							transactionItem.setAdvAdjTax4Value(txnTaxOnAdvAdj);
						} else if (transactionItem.getTaxName5() != null
								&& transactionItem.getTaxName5().indexOf(taxNameOnAdvAdjArray[count]) != -1) {
							transactionItem.setAdvAdjTax5Value(txnTaxOnAdvAdj);
						}
					}
				}
				if (!rowItemData.isNull("withholdingAmount") && !rowItemData.get("withholdingAmount").equals("")) {
					withholdingAmount = rowItemData.getDouble("withholdingAmount");
				}
				if (!rowItemData.isNull("customerAdvance") && !rowItemData.get("customerAdvance").equals("")) {
					customerAdvance = rowItemData.getDouble("customerAdvance");
				}
				if (!rowItemData.isNull("howMuchAdvance") && !rowItemData.get("howMuchAdvance").equals("")) {
					howMuchAdvance = rowItemData.getDouble("howMuchAdvance");
				}
				if (!rowItemData.isNull("txnInvoiceValue") && !rowItemData.get("txnInvoiceValue").equals("")) {
					txnInvoiceValue = rowItemData.getDouble("txnInvoiceValue");
				}
				Double netAmountVal = 0d;
				if (txnPurpose == IdosConstants.PREPARE_QUOTATION) {
					netAmountVal = txnGross;
				} else {
					if (!rowItemData.isNull("netAmountVal") && !rowItemData.get("netAmountVal").equals("")) {
						netAmountVal = rowItemData.getDouble("netAmountVal");
					}
				}

				String discountPercent = "0";
				if (!rowItemData.isNull("txnDiscountPercent")) {
					discountPercent = rowItemData.getString("txnDiscountPercent");
				}
				Double discountAmount = 0.0;
				if (!rowItemData.isNull("txnDiscountAmt") && !rowItemData.get("txnDiscountAmt").equals("")) {
					discountAmount = rowItemData.getDouble("txnDiscountAmt");
				}
				// storing budget info for buy transactions
				String userTxnAmountLimitDesc = "";
				String budgetAvailDuringTxn = "";
				String actualAllocatedBudget = "";
				if (!rowItemData.isNull("amountRangeLimitRuleVal")
						&& !rowItemData.get("amountRangeLimitRuleVal").equals("")) {
					userTxnAmountLimitDesc = rowItemData.getString("amountRangeLimitRuleVal");
				}
				if (!rowItemData.isNull("budgetDisplayVal") && !rowItemData.get("budgetDisplayVal").equals("")) {
					budgetAvailDuringTxn = rowItemData.getString("budgetDisplayVal");
				}
				if (!rowItemData.isNull("actualbudgetDisplayVal")
						&& !rowItemData.get("actualbudgetDisplayVal").equals("")) {
					actualAllocatedBudget = rowItemData.getString("actualbudgetDisplayVal");
				}
				transactionItem.setUserTxnLimitDesc(userTxnAmountLimitDesc);
				transactionItem.setActualAllocatedBudget(actualAllocatedBudget);
				transactionItem.setBudgetAvailDuringTxn(budgetAvailDuringTxn);

				if (budgetAvailDuringTxn != null && !budgetAvailDuringTxn.equals("")) {
					if (netAmountVal > IdosUtil.convertStringToDouble(budgetAvailDuringTxn)) {
						transaction.setTransactionExceedingBudget(1);
					}
				}
				transactionItem.setReverseChargeItemId(txnRcmTaxItemID);
				transactionItem.setTransactionId(transaction);
				transactionItem.setBranch(transaction.getTransactionBranch());
				transactionItem.setOrganization(transaction.getTransactionBranchOrganization());
				transactionItem.setTransactionSpecifics(txnItem);
				transactionItem.setTransactionParticulars(txnItem.getParticularsId());
				transactionItem.setPricePerUnit(txnPerUnitPrice);
				transactionItem.setNoOfUnits(txnNoOfUnit);
				transactionItem.setGrossAmount(txnGross);
				transactionItem.setInvoiceValue(txnInvoiceValue);

				// transactionItem.setTotalTax(txnTaxAmount); should be set from
				// saveTransactionTaxes
				if (withholdingAmount > 0.0) {
					transactionItem.setWithholdingAmount(withholdingAmount);
				}
				transactionItem.setAvailableAdvance(customerAdvance);
				transactionItem.setAdjustmentFromAdvance(howMuchAdvance);
				transactionItem.setNetAmount(netAmountVal);
				transactionItem.setDiscountPercent(discountPercent);
				transactionItem.setDiscountAmount(discountAmount);

				// advance adjustment
				if (howMuchAdvance > 0) {
					Double afterAdvRemoved = 0.00;
					Map<String, Object> criterias = new HashMap<String, Object>();
					criterias.put("vendorSpecific.id", transaction.getTransactionVendorCustomer().getId());
					criterias.put("specificsVendors.id", txnItem.getId());
					criterias.put("organization.id", user.getOrganization().getId());
					criterias.put("presentStatus", 1);
					VendorSpecific customerTxnSpecifics = genericDAO.getByCriteria(VendorSpecific.class, criterias,
							entityManager);
					if (customerTxnSpecifics.getAdvanceMoney() != null && customerTxnSpecifics.getAdvanceMoney() > 0) {
						if (customerTxnSpecifics.getAdvanceMoney() > howMuchAdvance) {
							customerTxnSpecifics
									.setAdvanceMoney(customerTxnSpecifics.getAdvanceMoney() - howMuchAdvance);
							transaction.setPaidInvoiceRefNumber(customerTxnSpecifics.getId().toString());
							transaction.setLinkedTxnRef(customerTxnSpecifics.getId().toString());
							transaction
									.setTypeIdentifier(IdosConstants.TXN_TYPE_CREDIT_AND_OPENING_BALANCE_ADV_PAID_VEND);
						} else {
							// TXN_TYPE_CREDIT_AND_OPENING_BALANCE_ADV_PAID_VEND ;
							afterAdvRemoved = howMuchAdvance
									- customerTxnSpecifics.getAdvanceMoney();
							customerTxnSpecifics.setAdvanceMoney(0.00);
							if (afterAdvRemoved > 0) {
								if (transaction.getTransactionVendorCustomer().getType() == 1) {
									VendorDetail vendorDetail = VendorDetail.findByVendorGSTNID(entityManager,
											transaction.getTransactionVendorCustomer().getId(),
											transaction.getDestinationGstin());
									VendorBranchWiseAdvBalance checkForVendorOpeingAdvance = VendorBranchWiseAdvBalance
											.getAdvAmountForItem(entityManager,
													user.getOrganization().getId(),
													transaction.getTransactionVendorCustomer().getId(),
													transaction.getTransactionBranch().getId(),
													transaction.getTypeOfSupply(),
													vendorDetail.getId(), txnItem.getId());

									if (checkForVendorOpeingAdvance != null) {
										checkForVendorOpeingAdvance.setAdvanceAmount(
												checkForVendorOpeingAdvance.getAdvanceAmount() - afterAdvRemoved);
										genericDAO.saveOrUpdate(checkForVendorOpeingAdvance, user,
												entityManager);
									}
								} else {
									CustomerBranchWiseAdvBalance checkForCustomerOpeingAdvance = CustomerBranchWiseAdvBalance
											.getAdvAmountForItem(entityManager,
													user.getOrganization().getId(),
													transaction.getTransactionVendorCustomer().getId(),
													transaction.getTransactionBranch().getId(),
													transaction.getTypeOfSupply(),
													Long.parseLong(transaction.getDestinationGstin()), txnItem.getId());

									if (checkForCustomerOpeingAdvance != null) {
										checkForCustomerOpeingAdvance.setAdvanceAmount(
												checkForCustomerOpeingAdvance.getAdvanceAmount() - afterAdvRemoved);
										genericDAO.saveOrUpdate(checkForCustomerOpeingAdvance, user,
												entityManager);
										// transaction.setLinkedTxnRef(checkForOpeingAdvance.getId().toString());
									}
									/*
									 * else {
									 * BranchVendors branchHasVendor =
									 * BranchVendors.findByVendorBranch(entityManager,
									 * user.getOrganization().getId(), transaction.getTransactionBranch().getId(),
									 * transaction.getTransactionVendorCustomer().getId());
									 * branchHasVendor.setOpeningBalanceAdvPaid(
									 * branchHasVendor.getOpeningBalanceAdvPaid() - afterAdvRemoved);
									 * genericDAO.saveOrUpdate(branchHasVendor, user, entityManager);
									 * }
									 */
								}
							}
						}
						genericDAO.saveOrUpdate(customerTxnSpecifics, user, entityManager);
					} else {
						if (transaction.getTransactionVendorCustomer().getType() == 1) {
							VendorDetail vendorDetail = VendorDetail.findByVendorGSTNID(entityManager,
									transaction.getTransactionVendorCustomer().getId(),
									transaction.getDestinationGstin());
							VendorBranchWiseAdvBalance checkForVendorOpeingAdvance = VendorBranchWiseAdvBalance
									.getAdvAmountForItem(entityManager,
											user.getOrganization().getId(),
											transaction.getTransactionVendorCustomer().getId(),
											transaction.getTransactionBranch().getId(),
											transaction.getTypeOfSupply(),
											vendorDetail.getId(), txnItem.getId());

							if (checkForVendorOpeingAdvance != null) {
								checkForVendorOpeingAdvance.setAdvanceAmount(
										checkForVendorOpeingAdvance.getAdvanceAmount() - howMuchAdvance);
								genericDAO.saveOrUpdate(checkForVendorOpeingAdvance, user,
										entityManager);
								transaction.setPaidInvoiceRefNumber(checkForVendorOpeingAdvance.getId().toString());
								transaction.setTypeIdentifier(
										IdosConstants.TXN_TYPE_OPENING_BALANCE_ADV_PAID_BRACHWISE_VEND);
							}
						} else {
							CustomerBranchWiseAdvBalance checkForCustomerOpeingAdvance = CustomerBranchWiseAdvBalance
									.getAdvAmountForItem(entityManager,
											user.getOrganization().getId(),
											transaction.getTransactionVendorCustomer().getId(),
											transaction.getTransactionBranch().getId(),
											transaction.getTypeOfSupply(),
											Long.parseLong(transaction.getDestinationGstin()), txnItem.getId());

							if (checkForCustomerOpeingAdvance != null) {
								checkForCustomerOpeingAdvance.setAdvanceAmount(
										checkForCustomerOpeingAdvance.getAdvanceAmount() - howMuchAdvance);
								genericDAO.saveOrUpdate(checkForCustomerOpeingAdvance, user,
										entityManager);
								transaction.setPaidInvoiceRefNumber(checkForCustomerOpeingAdvance.getId().toString());
								transaction.setTypeIdentifier(
										IdosConstants.TXN_TYPE_OPENING_BALANCE_ADV_PAID_BRACHWISE_CUST);
							}
							/*
							 * else {
							 * BranchVendors branchHasVendor =
							 * BranchVendors.findByVendorBranch(entityManager,
							 * user.getOrganization().getId(), transaction.getTransactionBranch().getId(),
							 * transaction.getTransactionVendorCustomer().getId());
							 * branchHasVendor.setOpeningBalanceAdvPaid(
							 * branchHasVendor.getOpeningBalanceAdvPaid() - afterAdvRemoved);
							 * genericDAO.saveOrUpdate(branchHasVendor, user, entityManager);
							 * }
							 */
						}
					}
					SELL_TRANSACTION_DAO.saveAdvanceAdjustmentDetail(user, entityManager, txnItem, transactionItem,
							transaction, howMuchAdvance, 0d, txnDate);
				}
				if (!rowItemData.isNull("txnLeftOutWithholdTransIDs")
						&& !rowItemData.get("txnLeftOutWithholdTransIDs").equals("")) {
					String txnLeftOutWithholdTransIDs = rowItemData.getString("txnLeftOutWithholdTransIDs");
					TransactionController.updateWithholdingForLeftOutTrans(txnLeftOutWithholdTransIDs, user,
							entityManager); // call only if withholding for previous transaction was not calculated.
				}
				Double tmpNetAmt = getAccurateNetAndTax(transactionItem, taxPojo);
				// log.log(Level.FINE, "==== tmpNetAmt = " + tmpNetAmt + " net = " +
				// netAmountVal);
				totalNetAmountVal += netAmountVal;
				genericDAO.saveOrUpdate(transactionItem, user, entityManager);
				transactionitems.add(transactionItem);
				if (IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT == txnPurpose
						&& "Accounted".equals(transaction.getTransactionStatus())) {
					BUY_TRANSACTION_DAO.saveUpdateBudget(transaction.getTransactionBranch(), transactionItem, user,
							entityManager);
				}
				if (transaction.getLinkedTxnRef() != null
						&& transaction.getLinkedTxnRef().startsWith(IdosConstants.BOM_TXN_TYPE)) {
					if (txnPurpose == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
							|| txnPurpose == IdosConstants.BUY_ON_CREDIT_PAY_LATER
							|| txnPurpose == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) {
						String bomTxnRef = transaction.getLinkedTxnRef();
						BillOfMaterialTxnModel bomTxn = BillOfMaterialTxnModel.findByBomTxnReference(entityManager,
								user.getOrganization().getId(), bomTxnRef);
						if (bomTxn == null) {
							throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
									"Error on save/update multiitems.", "BOm txn not found for Ref: " + bomTxnRef);
						} else {
							BILL_OF_MATERIAL_TXN_ITEM_DAO.updateFullfilDetail(user, entityManager, bomTxn, txnNoOfUnit,
									transactionItem.getTransactionSpecifics(), transactionItem.getBranch(),
									transaction.getTransactionRefNumber());
						}
					} else if (txnPurpose == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
							|| txnPurpose == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER) {
						String bomTxnRef = transaction.getLinkedTxnRef();
						BillOfMaterialTxnModel bomTxn = BillOfMaterialTxnModel.findByBomTxnReference(entityManager,
								user.getOrganization().getId(), bomTxnRef);
						if (bomTxn == null) {
							throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
									"Error on save/update multiitems.", "Bom txn not found for Ref: " + bomTxnRef);
						} else {
							Double unitsFullfilled = bomTxn.getFulfilledUnits() == null ? 0.0
									: bomTxn.getFulfilledUnits();
							Double unitsToFullfill = bomTxn.getIncomeNoOfUnits() - unitsFullfilled;
							if (unitsToFullfill <= txnNoOfUnit) {
								bomTxn.setIsFulfilled(IdosConstants.FULFILLED_TRANACTION);
								bomTxn.setFulfilledUnits(bomTxn.getIncomeNoOfUnits()); // all fullfilled
							} else if (unitsToFullfill > txnNoOfUnit) {
								unitsFullfilled += txnNoOfUnit;
								bomTxn.setFulfilledUnits(unitsFullfilled);
							}
							if (bomTxn.getLinkedRefNumber() != null) {
								bomTxn.setLinkedRefNumber(
										bomTxn.getLinkedRefNumber() + "," + transaction.getTransactionRefNumber());
							} else {
								bomTxn.setLinkedRefNumber(transaction.getTransactionRefNumber());
							}
						}
					}
				}
			}
			if (txnPurpose == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
					|| txnPurpose == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
					|| txnPurpose == IdosConstants.CREDIT_NOTE_CUSTOMER
					|| txnPurpose == IdosConstants.DEBIT_NOTE_CUSTOMER) {
				double totalNetAmtWithoutAdv = 0.0;
				for (TransactionItems transactionItem : transactionitems) {
					if (transactionItem.getTotalTax() != null) {
						double netAmtWithoutAdv = transactionItem.getGrossAmount() + transactionItem.getTotalTax();
						totalNetAmtWithoutAdv = totalNetAmtWithoutAdv + netAmtWithoutAdv;
					}
				}
				transaction.setInvoiceValue(totalNetAmtWithoutAdv);
				Double roundedCutPartOfNetAmount = transaction.getNetAmount() - totalNetAmountVal;
				transaction.setRoundedCutPartOfNetAmount(roundedCutPartOfNetAmount);
				genericDAO.saveOrUpdate(transaction, user, entityManager);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
					"Error on save/update multiitems.", ex.getMessage());
		}
	}

	private void setSellTaxInfo(TransactionItems transactionItem) {
		// VAT 1(+10.0%):5.0,Net Tax:5.00,
		String txnnetamountdescription = transactionItem.getTaxDescription();
		Double totalTax = 0d;
		if (txnnetamountdescription != null && !txnnetamountdescription.equals("")
				&& !txnnetamountdescription.contains("undefined")) {
			String inputtaxvalarr[] = txnnetamountdescription.split(",");
			for (int i = 0; i < inputtaxvalarr.length; i++) {
				String inputtaxvalwithstrarr[] = inputtaxvalarr[i].split(":");
				String rate = inputtaxvalwithstrarr[0].substring(inputtaxvalwithstrarr[0].indexOf("(") + 1,
						inputtaxvalwithstrarr[0].indexOf(")") - 1);
				String name = inputtaxvalwithstrarr[0].replaceAll("\\(.*?\\) ?", "");
				Double tempTaxAmount = Double.valueOf(inputtaxvalwithstrarr[1]);
				totalTax += tempTaxAmount;
				switch (i) {
					case 0:
						if (inputtaxvalarr.length > i) {
							transactionItem.setTaxName1(name);
							transactionItem.setTaxRate1(Double.valueOf(rate));
							transactionItem.setTaxValue1(tempTaxAmount);
						}
						break;
					case 1:
						if (inputtaxvalarr.length > i) {
							transactionItem.setTaxName2(name);
							transactionItem.setTaxRate2(Double.valueOf(rate));
							transactionItem.setTaxValue2(tempTaxAmount);
						}
						break;
					case 2:
						if (inputtaxvalarr.length > i) {
							transactionItem.setTaxName3(name);
							transactionItem.setTaxRate3(Double.valueOf(rate));
							transactionItem.setTaxValue3(tempTaxAmount);
						}
						break;
					case 3:
						if (inputtaxvalarr.length > i) {
							transactionItem.setTaxName4(name);
							transactionItem.setTaxRate4(Double.valueOf(rate));
							transactionItem.setTaxValue4(tempTaxAmount);
						}
						break;
					case 4:
						if (inputtaxvalarr.length > i) {
							transactionItem.setTaxName5(name);
							transactionItem.setTaxRate5(Double.valueOf(rate));
							transactionItem.setTaxValue5(tempTaxAmount);
						}
						break;
					case 5:
						if (inputtaxvalarr.length > i) {
							transactionItem.setTaxName6(name);
							transactionItem.setTaxRate6(Double.valueOf(rate));
							transactionItem.setTaxValue6(tempTaxAmount);
						}
						break;
					case 6:
						if (inputtaxvalarr.length > i) {
							transactionItem.setTaxName7(name);
							transactionItem.setTaxRate7(Double.valueOf(rate));
							transactionItem.setTaxValue7(tempTaxAmount);
						}
						break;
				}
			}
			transactionItem.setTotalTax(totalTax);
		}
	}

	private void setBuyTaxInfo(TransactionItems transactionItem) {
		// VAT 1(+10.0%):5.0,Net Tax:5.00,
		String txnnetamountdescription = transactionItem.getTaxDescription();
		if (txnnetamountdescription != null && !txnnetamountdescription.equals("")
				&& !txnnetamountdescription.contains("undefined")) {
			String inputtaxvalarr[] = txnnetamountdescription.split(",");
			for (int i = 0; i < inputtaxvalarr.length; i++) {
				String[] taxNameValue = inputtaxvalarr[i].split("=");
				if (taxNameValue.length < 1) {
					continue;
				}
				switch (i) {
					case 0:
						if (inputtaxvalarr.length > i) {
							transactionItem.setTaxName1(taxNameValue[0]);
							if (taxNameValue.length > 1)
								transactionItem.setTaxValue1(Double.valueOf(taxNameValue[1]));
						}
						break;
					case 1:
						if (inputtaxvalarr.length > i) {
							transactionItem.setTaxName2(taxNameValue[0]);
							if (taxNameValue.length > 1)
								transactionItem.setTaxValue2(Double.valueOf(taxNameValue[1]));
						}
						break;
					case 2:
						if (inputtaxvalarr.length > i) {
							transactionItem.setTaxName3(taxNameValue[0]);
							if (taxNameValue.length > 1)
								transactionItem.setTaxValue3(Double.valueOf(taxNameValue[1]));
						}
						break;
					case 3:
						if (inputtaxvalarr.length > i) {
							transactionItem.setTaxName4(taxNameValue[0]);
							if (taxNameValue.length > 1)
								transactionItem.setTaxValue4(Double.valueOf(taxNameValue[1]));
						}
						break;
					case 4:
						if (inputtaxvalarr.length > i) {
							transactionItem.setTaxName5(taxNameValue[0]);
							if (taxNameValue.length > 1)
								transactionItem.setTaxValue5(Double.valueOf(taxNameValue[1]));
						}
						break;
					case 5:
						if (inputtaxvalarr.length > i) {
							transactionItem.setTaxName6(taxNameValue[0]);
							if (taxNameValue.length > 1)
								transactionItem.setTaxValue6(Double.valueOf(taxNameValue[1]));
						}
						break;
					case 6:
						if (inputtaxvalarr.length > i) {
							transactionItem.setTaxName7(taxNameValue[0]);
							if (taxNameValue.length > 1)
								transactionItem.setTaxValue7(Double.valueOf(taxNameValue[1]));
						}
						break;
				}
			}
		}
	}

	@Override
	public void updateMultipleItemsTransactionItems(EntityManager entityManager, Users user, JSONArray arrJSON,
			Transaction transaction) throws IDOSException {
		try {
			Long txnPurpose = transaction.getTransactionPurpose().getId();
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("transaction.id", transaction.getId());
			genericDAO.deleteByCriteria(TransactionItems.class, criterias, entityManager);
			List<TransactionItems> transactionitems = new ArrayList<TransactionItems>();
			double totalNetAmountVal = 0.0;
			for (int i = 0; i < arrJSON.length(); i++) {
				Double howMuchAdvance = 0.0;
				Double txnTaxAmount = 0.0;
				Double customerAdvance = 0.0;
				String txnTaxDesc = "";
				Double withholdingAmount = 0.0;
				JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
				TransactionItems transactionItem = new TransactionItems();
				Long itemId = rowItemData.getLong("txnItems");
				Specifics txnItem = genericDAO.getById(Specifics.class, itemId, entityManager);
				Double txnPerUnitPrice = rowItemData.getDouble("txnPerUnitPrice");
				Double txnNoOfUnit = rowItemData.getDouble("txnNoOfUnit");
				Double txnGross = rowItemData.getDouble("txnGross");
				if (!rowItemData.isNull("txnTaxDesc") && !rowItemData.get("txnTaxDesc").equals("")) {
					txnTaxDesc = rowItemData.getString("txnTaxDesc");
				}
				transactionItem.setTaxDescription(txnTaxDesc);
				if (txnPurpose == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
						|| txnPurpose == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
						|| IdosConstants.CREDIT_NOTE_CUSTOMER == txnPurpose
						|| IdosConstants.DEBIT_NOTE_CUSTOMER == txnPurpose
						|| IdosConstants.CANCEL_INVOICE == txnPurpose) {
					if (user.getOrganization().getGstCountryCode() != null
							&& !"".equals(user.getOrganization().getGstCountryCode())) {
						saveTransactionTaxes(transactionItem, rowItemData, transaction);
					} else {
						setSellTaxInfo(transactionItem);
					}
				} else if (txnPurpose == IdosConstants.PREPARE_QUOTATION
						|| txnPurpose == IdosConstants.PROFORMA_INVOICE) {
					setSellTaxInfo(transactionItem);
				} else {
					setBuyTaxInfo(transactionItem);
				}
				String txnTaxNameOnAdvAdj = "";
				if (!rowItemData.isNull("txnTaxNameOnAdvAdj") && !rowItemData.get("txnTaxNameOnAdvAdj").equals("")) {
					txnTaxNameOnAdvAdj = rowItemData.getString("txnTaxNameOnAdvAdj");
				}
				Double txnTaxOnAdvAdj = 0d;
				if (!rowItemData.isNull("txnTaxOnAdvAdj") && !rowItemData.get("txnTaxOnAdvAdj").equals("")) {
					txnTaxOnAdvAdj = rowItemData.getDouble("txnTaxOnAdvAdj");
				}
				if (transactionItem.getTaxName1() != null
						&& transactionItem.getTaxName1().indexOf(txnTaxNameOnAdvAdj) != -1) {
					transactionItem.setAdvAdjTax1Value(txnTaxOnAdvAdj);
				} else if (transactionItem.getTaxName2() != null
						&& transactionItem.getTaxName2().indexOf(txnTaxNameOnAdvAdj) != -1) {
					transactionItem.setAdvAdjTax2Value(txnTaxOnAdvAdj);
				} else if (transactionItem.getTaxName3() != null
						&& transactionItem.getTaxName3().indexOf(txnTaxNameOnAdvAdj) != -1) {
					transactionItem.setAdvAdjTax3Value(txnTaxOnAdvAdj);
				} else if (transactionItem.getTaxName4() != null
						&& transactionItem.getTaxName4().indexOf(txnTaxNameOnAdvAdj) != -1) {
					transactionItem.setAdvAdjTax4Value(txnTaxOnAdvAdj);
				} else if (transactionItem.getTaxName5() != null
						&& transactionItem.getTaxName5().indexOf(txnTaxNameOnAdvAdj) != -1) {
					transactionItem.setAdvAdjTax5Value(txnTaxOnAdvAdj);
				}

				if (!rowItemData.isNull("txnTaxAmount") && !rowItemData.get("txnTaxAmount").equals("")) {
					txnTaxAmount = rowItemData.getDouble("txnTaxAmount");
				}
				if (!rowItemData.isNull("withholdingAmount") && !rowItemData.get("withholdingAmount").equals("")) {
					withholdingAmount = rowItemData.getDouble("withholdingAmount");
				}
				if (!rowItemData.isNull("customerAdvance") && !rowItemData.get("customerAdvance").equals("")) {
					customerAdvance = rowItemData.getDouble("customerAdvance");
				}
				if (!rowItemData.isNull("howMuchAdvance") && !rowItemData.get("howMuchAdvance").equals("")) {
					howMuchAdvance = rowItemData.getDouble("howMuchAdvance");
				}
				Double txnInvoiceValue = 0.0;
				if (!rowItemData.isNull("txnInvoiceValue") && !rowItemData.get("txnInvoiceValue").equals("")) {
					txnInvoiceValue = rowItemData.getDouble("txnInvoiceValue");
				}

				Double netAmountVal = 0d;
				if (!rowItemData.isNull("netAmountVal") && !rowItemData.get("netAmountVal").equals("")) {
					netAmountVal = rowItemData.getDouble("netAmountVal");
				}
				String discountPercent = "0";
				if (!rowItemData.isNull("txnDiscountPercent")) {
					discountPercent = rowItemData.getString("txnDiscountPercent");
				}
				Double discountAmount = 0.0;
				if (!rowItemData.isNull("txnDiscountAmt") && !rowItemData.get("txnDiscountAmt").equals("")) {
					discountAmount = rowItemData.getDouble("txnDiscountAmt");
				}
				// transactionItem.setTransactionId(transaction.getId());
				transactionItem.setTransactionId(transaction);
				transactionItem.setTransactionSpecifics(txnItem);
				transactionItem.setTransactionParticulars(txnItem.getParticularsId());
				transactionItem.setPricePerUnit(txnPerUnitPrice);
				transactionItem.setNoOfUnits(txnNoOfUnit);
				transactionItem.setGrossAmount(txnGross);
				transactionItem.setInvoiceValue(txnInvoiceValue);

				transactionItem.setTotalTax(txnTaxAmount);
				if (withholdingAmount > 0.0) {
					transactionItem.setWithholdingAmount(withholdingAmount);
				}
				transactionItem.setAvailableAdvance(customerAdvance);
				transactionItem.setAdjustmentFromAdvance(howMuchAdvance);
				transactionItem.setNetAmount(netAmountVal);
				transactionItem.setDiscountPercent(discountPercent);
				transactionItem.setDiscountAmount(discountAmount);
				genericDAO.saveOrUpdate(transactionItem, user, entityManager);

				// advance adjustment
				if (howMuchAdvance > 0.0) {
					Double afterAdvRemoved = 0.00;
					criterias.clear();
					criterias.put("vendorSpecific.id", transaction.getTransactionVendorCustomer().getId());
					criterias.put("specificsVendors.id", txnItem.getId());
					criterias.put("organization.id", user.getOrganization().getId());
					criterias.put("presentStatus", 1);
					VendorSpecific customerTxnSpecifics = genericDAO.getByCriteria(VendorSpecific.class, criterias,
							entityManager);
					if (customerTxnSpecifics.getAdvanceMoney() != null && customerTxnSpecifics.getAdvanceMoney() != 0) {
						if (customerTxnSpecifics.getAdvanceMoney() > howMuchAdvance) {
							customerTxnSpecifics
									.setAdvanceMoney(customerTxnSpecifics.getAdvanceMoney() - howMuchAdvance);

						} else {
							afterAdvRemoved = howMuchAdvance - customerTxnSpecifics.getAdvanceMoney();
							customerTxnSpecifics.setAdvanceMoney(0.00);
							if (afterAdvRemoved > 0) {
								if (transaction.getTransactionVendorCustomer().getType() == 1) {
									VendorDetail vendorDetail = VendorDetail.findByVendorGSTNID(entityManager,
											transaction.getTransactionVendorCustomer().getId(),
											transaction.getDestinationGstin());
									VendorBranchWiseAdvBalance checkForOpeingAdvance = VendorBranchWiseAdvBalance
											.getAdvAmountForItem(entityManager,
													user.getOrganization().getId(),
													transaction.getTransactionVendorCustomer().getId(),
													transaction.getTransactionBranch().getId(),
													transaction.getTypeOfSupply(),
													vendorDetail.getId(), txnItem.getId());

									if (checkForOpeingAdvance != null) {
										checkForOpeingAdvance.setAdvanceAmount(
												checkForOpeingAdvance.getAdvanceAmount() - afterAdvRemoved);
										genericDAO.saveOrUpdate(checkForOpeingAdvance, user,
												entityManager);
									}
								} else {
									CustomerBranchWiseAdvBalance checkForOpeingAdvance = CustomerBranchWiseAdvBalance
											.getAdvAmountForItem(entityManager,
													user.getOrganization().getId(),
													transaction.getTransactionVendorCustomer().getId(),
													transaction.getTransactionBranch().getId(),
													transaction.getTypeOfSupply(),
													Long.parseLong(transaction.getDestinationGstin()), txnItem.getId());

									if (checkForOpeingAdvance != null) {
										checkForOpeingAdvance.setAdvanceAmount(
												checkForOpeingAdvance.getAdvanceAmount() - afterAdvRemoved);
										genericDAO.saveOrUpdate(checkForOpeingAdvance, user,
												entityManager);
									}
									/*
									 * else {
									 * BranchVendors branchHasVendor =
									 * BranchVendors.findByVendorBranch(entityManager,
									 * user.getOrganization().getId(), transaction.getTransactionBranch().getId(),
									 * transaction.getTransactionVendorCustomer().getId());
									 * branchHasVendor.setOpeningBalanceAdvPaid(
									 * branchHasVendor.getOpeningBalanceAdvPaid() - afterAdvRemoved);
									 * genericDAO.saveOrUpdate(branchHasVendor, user, entityManager);
									 * }
									 */
								}
							}
						}
						genericDAO.saveOrUpdate(customerTxnSpecifics, user, entityManager);
					} else {
						if (transaction.getTransactionVendorCustomer().getType() == 1) {
							VendorDetail vendorDetail = VendorDetail.findByVendorGSTNID(entityManager,
									transaction.getTransactionVendorCustomer().getId(),
									transaction.getDestinationGstin());
							VendorBranchWiseAdvBalance checkForOpeingAdvance = VendorBranchWiseAdvBalance
									.getAdvAmountForItem(entityManager,
											user.getOrganization().getId(),
											transaction.getTransactionVendorCustomer().getId(),
											transaction.getTransactionBranch().getId(),
											transaction.getTypeOfSupply(),
											vendorDetail.getId(), txnItem.getId());

							if (checkForOpeingAdvance != null) {
								checkForOpeingAdvance.setAdvanceAmount(
										checkForOpeingAdvance.getAdvanceAmount() - afterAdvRemoved);
								genericDAO.saveOrUpdate(checkForOpeingAdvance, user,
										entityManager);
							}
						} else {
							CustomerBranchWiseAdvBalance checkForOpeingAdvance = CustomerBranchWiseAdvBalance
									.getAdvAmountForItem(entityManager,
											user.getOrganization().getId(),
											transaction.getTransactionVendorCustomer().getId(),
											transaction.getTransactionBranch().getId(),
											transaction.getTypeOfSupply(),
											Long.parseLong(transaction.getDestinationGstin()), txnItem.getId());

							if (checkForOpeingAdvance != null) {
								checkForOpeingAdvance.setAdvanceAmount(
										checkForOpeingAdvance.getAdvanceAmount() - afterAdvRemoved);
								genericDAO.saveOrUpdate(checkForOpeingAdvance, user,
										entityManager);
							}
							/*
							 * else {
							 * BranchVendors branchHasVendor =
							 * BranchVendors.findByVendorBranch(entityManager,
							 * user.getOrganization().getId(), transaction.getTransactionBranch().getId(),
							 * transaction.getTransactionVendorCustomer().getId());
							 * branchHasVendor.setOpeningBalanceAdvPaid(
							 * branchHasVendor.getOpeningBalanceAdvPaid() - afterAdvRemoved);
							 * genericDAO.saveOrUpdate(branchHasVendor, user, entityManager);
							 * }
							 */
						}
					}
				}

				if (!rowItemData.isNull("txnLeftOutWithholdTransIDs")
						&& !rowItemData.get("txnLeftOutWithholdTransIDs").equals("")) {
					String txnLeftOutWithholdTransIDs = rowItemData.getString("txnLeftOutWithholdTransIDs");
					TransactionController.updateWithholdingForLeftOutTrans(txnLeftOutWithholdTransIDs, user,
							entityManager); // call only if withholding for previous transaction was not calculated.
				}
				totalNetAmountVal += netAmountVal;
				transactionitems.add(transactionItem);
			}
			if (txnPurpose == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
					|| txnPurpose == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
					|| (IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER == txnPurpose
							&& transaction.getTypeIdentifier() == 1)) {
				double totalNetAmtWithoutAdv = 0.0;
				for (TransactionItems transactionItem : transactionitems) {
					double netAmtWithoutAdv = transactionItem.getGrossAmount() + transactionItem.getTotalTax();
					totalNetAmtWithoutAdv = totalNetAmtWithoutAdv + netAmtWithoutAdv;
				}
				Double roundedCutPartOfNetAmount = transaction.getNetAmount() - totalNetAmountVal;
				transaction.setRoundedCutPartOfNetAmount(roundedCutPartOfNetAmount);
				transaction.setInvoiceValue(totalNetAmtWithoutAdv);
				genericDAO.saveOrUpdate(transaction, user, entityManager);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
					"Error on save/update multiitems.", ex.getMessage());
		}
	}

	@Override
	public void updateMultipleItemsSalesReturnTransactionItems(EntityManager entityManager, Users user,
			JSONArray arrJSON, Transaction transaction) throws IDOSException {
		try {
			for (int i = 0; i < arrJSON.length(); i++) {
				Double howMuchAdvanceReturned = 0.0;
				Double txnTaxAmountReturned = 0.0;
				Double customerAdvanceReturned = 0.0;
				String txnTaxDescReturned = "";
				Double withholdingAmountReturned = 0.0;
				JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
				Long txnItemTableIdHid = rowItemData.getLong("txnItemTableIdHid"); // get Sell on credit row in
																					// TransactionItems and in that
																					// enter sales retrun values
				if (txnItemTableIdHid != null) {
					TransactionItems transactionItem = TransactionItems.findById(txnItemTableIdHid);
					if (transactionItem != null) {
						Long itemId = rowItemData.getLong("txnItems");
						Specifics txnItem = genericDAO.getById(Specifics.class, itemId, entityManager);
						Double txnNoOfUnitReturned = rowItemData.getDouble("txnNoOfUnit");
						Double txnGrossReturned = rowItemData.getDouble("txnGross");
						if (!rowItemData.isNull("txnTaxDesc") && !rowItemData.get("txnTaxDesc").equals("")) {
							txnTaxDescReturned = rowItemData.getString("txnTaxDesc");
						}
						if (!rowItemData.isNull("txnTaxAmount") && !rowItemData.get("txnTaxAmount").equals("")) {
							txnTaxAmountReturned = rowItemData.getDouble("txnTaxAmount");
						}
						if (!rowItemData.isNull("withholdingAmount")
								&& !rowItemData.get("withholdingAmount").equals("")) {
							withholdingAmountReturned = rowItemData.getDouble("withholdingAmount");
						}
						if (!rowItemData.isNull("customerAdvance") && !rowItemData.get("customerAdvance").equals("")) {
							customerAdvanceReturned = rowItemData.getDouble("customerAdvance");
						}
						if (!rowItemData.isNull("howMuchAdvance") && !rowItemData.get("howMuchAdvance").equals("")) {
							howMuchAdvanceReturned = rowItemData.getDouble("howMuchAdvance");
						}
						Double netAmountValReturned = 0d;
						if (!rowItemData.isNull("netAmountVal") && !rowItemData.get("netAmountVal").equals("")) {
							netAmountValReturned = rowItemData.getDouble("netAmountVal");
						}

						if (transactionItem.getTransactionSpecifics().getId() == txnItem.getId()
								&& txnNoOfUnitReturned != 0) { // to confirm we are modifying right Transaction_Items of
																// Sell on credit
							double prevUnits = 0;
							if (transactionItem.getNoOfUnitsReturned() != null) {
								prevUnits = transactionItem.getNoOfUnitsReturned();
							}
							double prevGross = 0;
							if (transactionItem.getGrossAmounReturned() != null) {
								prevGross = transactionItem.getGrossAmounReturned();
							}
							double prevTax = 0;
							if (transactionItem.getTotalTaxReturned() != null) {
								prevTax = transactionItem.getTotalTaxReturned();
							}
							double prevAdj = 0;
							if (transactionItem.getAdjustmentFromAdvanceReturned() != null) {
								prevAdj = transactionItem.getAdjustmentFromAdvanceReturned();
							}
							double prevWH = 0;
							if (transactionItem.getWithholdingAmountReturned() != null) {
								prevWH = transactionItem.getWithholdingAmountReturned();
							}
							double prevNet = 0;
							if (transactionItem.getNetAmountReturned() != null) {
								prevNet = transactionItem.getNetAmountReturned();
							}
							transactionItem.setNoOfUnitsReturned(txnNoOfUnitReturned + prevUnits); // if previously
																									// anything returned
																									// then add that qty
							transactionItem.setGrossAmounReturned(txnGrossReturned + prevGross);
							transactionItem.setTaxDescriptionReturned(txnTaxDescReturned);
							transactionItem.setTotalTaxReturned(txnTaxAmountReturned + prevTax);
							transactionItem.setAvailableAdvanceForReturned(customerAdvanceReturned);
							transactionItem.setAdjustmentFromAdvanceReturned(howMuchAdvanceReturned + prevAdj);
							transactionItem.setWithholdingAmountReturned(withholdingAmountReturned + prevWH);
							transactionItem.setNetAmountReturned(netAmountValReturned + prevNet);
							genericDAO.saveOrUpdate(transactionItem, user, entityManager);

							// advance adjustment
							if (howMuchAdvanceReturned != 0.0) {
								Map<String, Object> criterias = new HashMap<String, Object>();
								criterias.clear();
								criterias.put("vendorSpecific.id", transaction.getTransactionVendorCustomer().getId());
								criterias.put("specificsVendors.id", txnItem.getId());
								criterias.put("organization.id", user.getOrganization().getId());
								criterias.put("presentStatus", 1);
								VendorSpecific customerTxnSpecifics = genericDAO.getByCriteria(VendorSpecific.class,
										criterias, entityManager);
								customerTxnSpecifics.setAdvanceMoney(
										customerTxnSpecifics.getAdvanceMoney() + howMuchAdvanceReturned);
								genericDAO.saveOrUpdate(customerTxnSpecifics, user, entityManager);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
					"Error on save/update multiitems.", ex.getMessage());
		}
	}

	@Override
	public Long saveMultiItemsTransRecAdvCust(EntityManager entityManager, Users user, JsonNode json,
			Transaction transaction, Vendor customer, ObjectNode result) throws IDOSException {
		Long tdsRecSpecificID = -1L;
		try {
			Double totalAdvanceReceived = 0d, totalWithholdingTax = 0d, totalGross = 0d;
			String txnforunavailablecustomer = json.findValue("txnforunavailablecustomer") != null
					? json.findValue("txnforunavailablecustomer").asText()
					: null;
			String txnForItemStr = json.findValue("txnforitem").toString();
			JSONArray arrJSON = new JSONArray(txnForItemStr);
			for (int i = 0; i < arrJSON.length(); i++) {
				String userTxnLimitDesc = "";
				String budgetAvailDuringTxn = "";
				String actualAllocatedBudget = "";
				JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
				rowItemData.getLong("txnItems");
				Double withholdingAmount = 0.0;
				if (!rowItemData.isNull("withholdingAmount") && !rowItemData.get("withholdingAmount").equals("")) {
					withholdingAmount = rowItemData.getDouble("withholdingAmount");
				}

				if (withholdingAmount != null && withholdingAmount > 0.0) {
					// Is this the account where you classify withholding tax (TDS) on payments
					// received from customers
					Specifics specificsForMapping = CHART_OF_ACCOUNTS_SERVICE.getSpecificsForMapping(user, "8",
							entityManager);
					if (specificsForMapping == null) {
						result.put("tdsReceivableSpecific", 0);
						throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
								"COA mapping is not found for item", "TDS COA mapping not found for, type: 8");
					} else {
						tdsRecSpecificID = specificsForMapping.getId();
					}
				}
				Double customerAdvance = 0.0;
				if (!rowItemData.isNull("customerAdvance") && !rowItemData.get("customerAdvance").equals("")) {
					customerAdvance = rowItemData.getDouble("customerAdvance");
				}
				Long incomeItem = rowItemData.getLong("txnItems");
				Specifics incomeSpecf = Specifics.findById(incomeItem);
				if (txnforunavailablecustomer != null && !"".equals(txnforunavailablecustomer)) {
					// add data into vendorspecifics table too, as cust adv is stored in this table.
					VendorSpecific vennSpecf = new VendorSpecific();
					Specifics newVendSpecfics = Specifics.findById(incomeSpecf.getId());
					Particulars newVendParticulars = newVendSpecfics.getParticularsId();
					vennSpecf.setVendorSpecific(customer);
					vennSpecf.setSpecificsVendors(newVendSpecfics);
					vennSpecf.setBranch(user.getBranch());
					vennSpecf.setOrganization(user.getOrganization());
					vennSpecf.setParticulars(newVendParticulars);
					vennSpecf.setAdvanceMoney(customerAdvance + withholdingAmount);
					genericDAO.saveOrUpdate(vennSpecf, user, entityManager);
				} else {
					ArrayList inparamList = new ArrayList(3);
					inparamList.add(user.getOrganization().getId());
					inparamList.add(incomeSpecf.getId());
					inparamList.add(customer.getId());
					List<VendorSpecific> vendorSpecf = genericDAO.queryWithParams(VENDOR_SPECIFIC_HQL, entityManager,
							inparamList);
					if (vendorSpecf.size() > 0) {
						VendorSpecific vendorSpecficsForAdvance = vendorSpecf.get(0);
						Double addedAdvance = 0d;
						if (vendorSpecficsForAdvance.getAdvanceMoney() != null) {
							addedAdvance = customerAdvance + vendorSpecficsForAdvance.getAdvanceMoney()
									+ withholdingAmount;
						} else {
							addedAdvance = customerAdvance + withholdingAmount;
						}
						vendorSpecficsForAdvance.setAdvanceMoney(addedAdvance);
						genericDAO.saveOrUpdate(vendorSpecficsForAdvance, user, entityManager);
					}
				}

				TransactionItems transactionItem = new TransactionItems();
				Double txnGross = rowItemData.getDouble("txnGross");
				String txnTaxDesc = "";
				if (!rowItemData.isNull("txnTaxDesc") && !rowItemData.get("txnTaxDesc").equals("")) {
					txnTaxDesc = rowItemData.getString("txnTaxDesc");
				}
				transactionItem.setTaxDescription(txnTaxDesc);
				Double txnInvoiceValue = 0.0;
				if (!rowItemData.isNull("txnInvoiceValue") && !rowItemData.get("txnInvoiceValue").equals("")) {
					txnInvoiceValue = rowItemData.getDouble("txnInvoiceValue");
				}

				if (user.getOrganization().getGstCountryCode() != null
						&& !"".equals(user.getOrganization().getGstCountryCode())) {
					saveTransactionTaxes(transactionItem, rowItemData, transaction);
				} else {
					setSellTaxInfo(transactionItem);
				}
				// transactionItem.setTransactionId(transaction.getId());
				transactionItem.setTransactionId(transaction);
				transactionItem.setBranch(transaction.getTransactionBranch());
				transactionItem.setOrganization(transaction.getTransactionBranchOrganization());
				transactionItem.setTransactionSpecifics(incomeSpecf);
				transactionItem.setTransactionParticulars(incomeSpecf.getParticularsId());
				transactionItem.setGrossAmount(txnGross);
				transactionItem.setInvoiceValue(txnInvoiceValue);
				transactionItem.setWithholdingAmount(withholdingAmount);
				transactionItem.setAvailableAdvance(customerAdvance);
				transactionItem.setNetAmount(customerAdvance);
				genericDAO.saveOrUpdate(transactionItem, user, entityManager);
				totalAdvanceReceived += customerAdvance;
				totalWithholdingTax += withholdingAmount;
				totalGross += txnGross;
			}
			transaction.setNetAmount(totalAdvanceReceived);
			transaction.setCustomerNetPayment(totalAdvanceReceived);
			transaction.setWithholdingTax(totalWithholdingTax);
			transaction.setAvailableAdvance(totalAdvanceReceived);
			transaction.setGrossAmount(totalGross);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
					"Error on save/update multiitems.", ex.getMessage());
		}
		return tdsRecSpecificID;
	}

	@Override
	public TaxPojo saveTransactionTaxes(TransactionItems transactionItem, JSONObject rowItemData, Transaction txn)
			throws Exception {
		Double totalTaxOnItem = 0d;
		Double totalTax1 = txn.getTaxValue1() == null ? 0d : txn.getTaxValue1();
		Double totalTax2 = txn.getTaxValue2() == null ? 0d : txn.getTaxValue2();
		Double totalTax3 = txn.getTaxValue3() == null ? 0d : txn.getTaxValue3();
		Double totalTax4 = txn.getTaxValue4() == null ? 0d : txn.getTaxValue4();
		Double totalTax5 = txn.getTaxValue5() == null ? 0d : txn.getTaxValue5();
		try {
			String taxName = "";
			if (!rowItemData.isNull("txnTaxName") && !rowItemData.get("txnTaxName").equals("")) {
				taxName = rowItemData.getString("txnTaxName");
			}
			String taxNameArray[] = taxName.split(",");
			String txnTaxAmount = "";
			if (!rowItemData.isNull("txnTaxAmount") && !rowItemData.get("txnTaxAmount").equals("")) {
				txnTaxAmount = rowItemData.getString("txnTaxAmount");
			}
			String taxAmountArray[] = txnTaxAmount.split(",");
			String txnTaxRate = "";
			if (!rowItemData.isNull("txnTaxRate") && !rowItemData.get("txnTaxRate").equals("")) {
				txnTaxRate = rowItemData.getString("txnTaxRate");
			}
			String taxRateArray[] = txnTaxRate.split(",");
			String txnTaxID = "";
			if (!rowItemData.isNull("txnTaxID") && !rowItemData.get("txnTaxID").equals("")) {
				txnTaxID = rowItemData.getString("txnTaxID");
			}
			String taxIDArray[] = txnTaxID.split(",");

			String txnFormulaIDs = "";
			if (!rowItemData.isNull("txnTaxFormulaId") && !rowItemData.get("txnTaxFormulaId").equals("")) {
				txnFormulaIDs = rowItemData.getString("txnTaxFormulaId");
			}
			String taxFormulaArray[] = txnFormulaIDs.split(",");
			// RCM

			Double txnDutiesAndTaxesAmount = 0d;
			if (!rowItemData.isNull("txnDutiesAndTaxesAmount")
					&& !rowItemData.get("txnDutiesAndTaxesAmount").equals("")) {
				txnDutiesAndTaxesAmount = IdosUtil
						.convertStringToDouble(rowItemData.getString("txnDutiesAndTaxesAmount"));
			}
			TaxPojo taxPojo = new TaxPojo();
			for (int count = 0; count < taxAmountArray.length; count++) {
				if (!"".equals(taxAmountArray[count])) {
					Double txnTax = IdosUtil.convertStringToDouble(taxAmountArray[count]);
					String txnTaxName = taxNameArray[count];
					Double taxRate;
					if (taxRateArray[count].toString().equals("-")) {
						taxRate = 0.0;
					} else {
						taxRate = IdosUtil.convertStringToDouble(taxRateArray[count]);
					}
					Long taxID = null;
					if (taxIDArray[count] != null && !"null".equals(taxIDArray[count])) {
						taxID = IdosUtil.convertStringToLong(taxIDArray[count]);
					}

					Long txnTaxFormulaId = null;
					if (taxFormulaArray.length > count && taxFormulaArray[count] != null
							&& !"null".equals(taxFormulaArray[count]) && !"".equals(taxFormulaArray[count])) {
						txnTaxFormulaId = IdosUtil.convertStringToLong(taxFormulaArray[count]);
					}
					if (txnTaxName.indexOf("SGST") != -1) {
						transactionItem.setTaxValue1(txnTax);
						transactionItem.setTaxName1(txnTaxName);
						transactionItem.setTaxRate1(taxRate);
						transactionItem.setTax1ID(taxID);
						taxPojo.setFormula1(txnTaxFormulaId);
						totalTax1 += txnTax;
					} else if (txnTaxName.indexOf("CGST") != -1) {
						transactionItem.setTaxValue2(txnTax);
						transactionItem.setTaxName2(txnTaxName);
						transactionItem.setTaxRate2(taxRate);
						transactionItem.setTax2ID(taxID);
						taxPojo.setFormula2(txnTaxFormulaId);
						totalTax2 += txnTax;
					} else if (txnTaxName.indexOf("IGST") != -1) {
						transactionItem.setTaxValue3(txnTax);
						transactionItem.setTaxName3(txnTaxName);
						transactionItem.setTaxRate3(taxRate);
						transactionItem.setTax3ID(taxID);
						taxPojo.setFormula3(txnTaxFormulaId);
						totalTax3 += txnTax;
					} else if (txnTaxName.indexOf("CESS") != -1) {
						transactionItem.setTaxValue4(txnTax);
						transactionItem.setTaxName4(txnTaxName);
						transactionItem.setTaxRate4(taxRate);
						transactionItem.setTax4ID(taxID);
						taxPojo.setFormula4(txnTaxFormulaId);
						totalTax4 += txnTax;
					} else {
						transactionItem.setTaxValue5(txnTax);
						transactionItem.setTaxName5(txnTaxName);
						transactionItem.setTaxRate5(taxRate);
						transactionItem.setTax5ID(taxID);
						taxPojo.setFormula5(txnTaxFormulaId);
						totalTax5 += txnTax;
					}
					totalTaxOnItem += txnTax;
				}
			}
			if (txnDutiesAndTaxesAmount != null && txnDutiesAndTaxesAmount > 0) {
				transactionItem.setTaxValue7(txnDutiesAndTaxesAmount);
				transactionItem.setTaxName7("Duties/Taxes");
			}
			transactionItem.setTotalTax(totalTaxOnItem);
			txn.setTaxValue1(totalTax1);
			txn.setTaxValue2(totalTax2);
			txn.setTaxValue3(totalTax3);
			txn.setTaxValue4(totalTax4);
			txn.setTaxValue5(totalTax5);
			return taxPojo;
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			throw ex;
		}
	}

	@Override
	public Long saveMultiItemsTransPayAdvVend(EntityManager entityManager, Users user, JsonNode json,
			Transaction transaction, Vendor customer, ObjectNode result) throws IDOSException {
		Long tdsRecSpecificID = -1L;
		try {
			Double totalAdvanceReceived = 0d, totalWithholdingTax = 0d, totalGross = 0d;
			String txnforunavailablecustomer = json.findValue("txnforunavailablecustomer") != null
					? json.findValue("txnforunavailablecustomer").asText()
					: null;
			String txnForItemStr = json.findValue("txnforitem").toString();
			// Long txnRcmTaxItem = json.findValue("txnRcmTaxItem").asLong();
			// log.log(Level.FINE, "txnRcmTaxItem===="+txnRcmTaxItem);
			Long txnRcmTaxItem = null;
			JSONArray arrJSON = new JSONArray(txnForItemStr);
			for (int i = 0; i < arrJSON.length(); i++) {
				String userTxnLimitDesc = "";
				String budgetAvailDuringTxn = "";
				String actualAllocatedBudget = "";
				JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
				rowItemData.getLong("txnItems");

				if (!rowItemData.isNull("txnRcmTaxItemID") && !rowItemData.get("txnRcmTaxItemID").equals("")) {
					txnRcmTaxItem = rowItemData.getLong("txnRcmTaxItemID");
				}

				/*
				 * if (withholdingAmount != null && withholdingAmount > 0.0) { //instead it will
				 * be from sectionwise mapping from 31-35
				 * //Is this the account where you classify withholding tax (TDS) on payments
				 * received from customers
				 * tdsRecSpecificID = CHART_OF_ACCOUNTS_SERVICE.getSpecificsForMapping(user,
				 * "8", entityManager);
				 * if (tdsRecSpecificID == null || tdsRecSpecificID == 0) {
				 * result.put("tdsReceivableSpecific", 0);
				 * break;
				 * }
				 * }
				 */
				Double customerAdvance = 0.0;
				if (!rowItemData.isNull("customerAdvance") && !rowItemData.get("customerAdvance").equals("")) {
					customerAdvance = rowItemData.getDouble("customerAdvance");
				}
				Long incomeItem = rowItemData.getLong("txnItems");
				Specifics incomeSpecf = Specifics.findById(incomeItem);

				TransactionItems transactionItem = new TransactionItems();
				Double txnGross = rowItemData.getDouble("txnGross");
				String txnTaxDesc = "";
				if (!rowItemData.isNull("txnTaxDesc") && !rowItemData.get("txnTaxDesc").equals("")) {
					txnTaxDesc = rowItemData.getString("txnTaxDesc");
				}
				transactionItem.setTaxDescription(txnTaxDesc);
				Double txnInvoiceValue = 0.0;
				if (!rowItemData.isNull("txnInvoiceValue") && !rowItemData.get("txnInvoiceValue").equals("")) {
					txnInvoiceValue = rowItemData.getDouble("txnInvoiceValue");
				}
				Double withholdingAmount = 0.0;
				Double netAmt = txnGross;
				if (!rowItemData.isNull("withholdingAmount") && !rowItemData.get("withholdingAmount").equals("")) {
					withholdingAmount = rowItemData.getDouble("withholdingAmount");
					netAmt = netAmt - withholdingAmount;
				}
				// transactionItem.setTransactionId(transaction.getId());
				transactionItem.setTransactionId(transaction);
				transactionItem.setBranch(transaction.getTransactionBranch());
				transactionItem.setOrganization(transaction.getTransactionBranchOrganization());
				transactionItem.setTransactionSpecifics(incomeSpecf);
				transactionItem.setTransactionParticulars(incomeSpecf.getParticularsId());
				transactionItem.setGrossAmount(txnGross);
				transactionItem.setInvoiceValue(txnInvoiceValue);
				if (withholdingAmount > 0.0) {
					transactionItem.setWithholdingAmount(withholdingAmount);
				}
				transactionItem.setAvailableAdvance(customerAdvance);
				transactionItem.setNetAmount(netAmt);
				if (txnRcmTaxItem != null) {
					transactionItem.setReverseChargeItemId(txnRcmTaxItem);
				}
				genericDAO.saveOrUpdate(transactionItem, user, entityManager);
				totalAdvanceReceived += customerAdvance;
				totalWithholdingTax += withholdingAmount;
				totalGross += txnGross;
			}
			transaction.setNetAmount(totalAdvanceReceived - totalWithholdingTax);
			transaction.setCustomerNetPayment(totalAdvanceReceived);
			transaction.setWithholdingTax(totalWithholdingTax);
			transaction.setAvailableAdvance(totalAdvanceReceived);
			transaction.setGrossAmount(totalGross);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
					"Error on save/update multiitems.", ex.getMessage());
		}
		return tdsRecSpecificID;
	}

	private Double getAccurateNetAndTax(TransactionItems txnItem, TaxPojo taxPojo) {
		if (taxPojo == null) {
			return 0.0;
		}
		Long sgstFormulaId = taxPojo.getFormula1();
		Double sgstRate = txnItem.getTaxRate1();
		Long cgstFormulaId = taxPojo.getFormula2();
		Double cgstRate = txnItem.getTaxRate2();
		Long igstFormulaId = taxPojo.getFormula3();
		Double igstRate = txnItem.getTaxRate3();
		Long cessFormulaId = taxPojo.getFormula4();
		Double cessRate = txnItem.getTaxRate4();
		double grossAmount = txnItem.getGrossAmount();
		Specifics specifics = txnItem.getTransactionSpecifics();
		Double sgstAmount = 0.0;
		Double invoiceValue1 = 0.0;
		Double cgstAmount = 0.0;
		Double invoiceValue2 = 0.0;
		Double igstAmount = 0.0;
		Double invoiceValue3 = 0.0;
		Double cessAmount = 0.0;
		Double invoiceValue4 = 0.0;
		Double totalTaxAmount = 0.0;

		if (specifics.getIsCombinationSales() == null || specifics.getIsCombinationSales() != 1) {
			BranchSpecificsTaxFormula bnchSpecfTaxFormula = BranchSpecificsTaxFormula.findById(sgstFormulaId);
			if (bnchSpecfTaxFormula != null && sgstFormulaId > 0) {
				if ("GV".equals(bnchSpecfTaxFormula.getAppliedTo())) {
					sgstAmount = grossAmount * (sgstRate / (100.0));
					if (bnchSpecfTaxFormula.getAddDeduct() == 1) {
						invoiceValue1 = grossAmount + sgstAmount;
					} else if (bnchSpecfTaxFormula.getAddDeduct() == 0) {
						invoiceValue1 = grossAmount - sgstAmount;
					}
					totalTaxAmount += sgstAmount;
				}
			}
			bnchSpecfTaxFormula = BranchSpecificsTaxFormula.findById(cgstFormulaId);
			if (bnchSpecfTaxFormula != null && cgstFormulaId > 0) {
				if ("GV".equals(bnchSpecfTaxFormula.getAppliedTo())) {
					cgstAmount = grossAmount * (cgstRate / (100.0));
				} else if ("Tax1".equals(bnchSpecfTaxFormula.getAppliedTo())) {
					cgstAmount = sgstAmount * (cgstRate / (100.0));
				} else if ("IV1".equals(bnchSpecfTaxFormula.getAppliedTo())) {
					cgstAmount = invoiceValue1 * (cgstRate / (100.0));
				}

				if (bnchSpecfTaxFormula.getAddDeduct() == 1) {
					invoiceValue2 = grossAmount + (sgstAmount + cgstAmount);
				} else if (bnchSpecfTaxFormula.getAddDeduct() == 0) {
					invoiceValue2 = grossAmount - (sgstAmount + cgstAmount);
				}
				totalTaxAmount += cgstAmount;
			}

			bnchSpecfTaxFormula = BranchSpecificsTaxFormula.findById(igstFormulaId);
			if (bnchSpecfTaxFormula != null && igstFormulaId > 0) {
				if ("GV".equals(bnchSpecfTaxFormula.getAppliedTo())) {
					igstAmount = grossAmount * (igstRate / (100.0));
				} else if ("Tax1".equals(bnchSpecfTaxFormula.getAppliedTo())) {
					igstAmount = sgstAmount * (igstRate / (100.0));
				} else if ("Tax2".equals(bnchSpecfTaxFormula.getAppliedTo())) {
					igstAmount = cgstAmount * (igstRate / (100.0));
				} else if ("IV2".equals(bnchSpecfTaxFormula.getAppliedTo())) {
					igstAmount = invoiceValue2 * (igstRate / (100.0));
				}

				if (bnchSpecfTaxFormula.getAddDeduct() == 1) {
					invoiceValue3 = grossAmount + (sgstAmount + cgstAmount + igstAmount);
				} else if (bnchSpecfTaxFormula.getAddDeduct() == 0) {
					invoiceValue3 = grossAmount - (sgstAmount + cgstAmount + igstAmount);
				}
				totalTaxAmount += igstAmount;
			}

			bnchSpecfTaxFormula = BranchSpecificsTaxFormula.findById(cessFormulaId);
			if (bnchSpecfTaxFormula != null && cessFormulaId > 0) {
				if ("GV".equals(bnchSpecfTaxFormula.getAppliedTo())) {
					cessAmount = grossAmount * (cessRate / (100.0));
				} else if ("Tax1".equals(bnchSpecfTaxFormula.getAppliedTo())) {
					cessAmount = sgstAmount * (cessRate / (100.0));
				} else if ("Tax2".equals(bnchSpecfTaxFormula.getAppliedTo())) {
					cessAmount = cgstAmount * (cessRate / (100.0));
				} else if ("Tax3".equals(bnchSpecfTaxFormula.getAppliedTo())) {
					cessAmount = igstAmount * (cessRate / (100.0));
				} else if ("IV3".equals(bnchSpecfTaxFormula.getAppliedTo())) {
					cessAmount = invoiceValue3 * (cessRate / (100.0));
				}
				if (bnchSpecfTaxFormula.getAddDeduct() == 1) {
					invoiceValue4 = grossAmount + (sgstAmount + cgstAmount + igstAmount + cessAmount);
				} else if (bnchSpecfTaxFormula.getAddDeduct() == 0) {
					invoiceValue4 = grossAmount - (sgstAmount + cgstAmount + igstAmount + cessAmount);
				}
				totalTaxAmount += cessAmount;
			}
		}
		log.log(Level.FINE, "-- " + totalTaxAmount);
		Double invoiceValue = grossAmount + totalTaxAmount;
		Double netAmount = invoiceValue;
		if (txnItem.getAdjustmentFromAdvance() != null) {
			netAmount = netAmount - txnItem.getAdjustmentFromAdvance();
		}

		/*
		 * txnItem.setNetAmount(netAmount);
		 * txnItem.setInvoiceValue(invoiceValue);
		 * txnItem.setTaxValue1(sgstAmount);
		 * txnItem.setTaxValue2(cgstAmount);
		 * txnItem.setTaxValue3(igstAmount);
		 * txnItem.setTaxValue1(cessAmount);
		 * 
		 */
		txnItem.setTotalTax(totalTaxAmount);
		return netAmount;
	}
}
