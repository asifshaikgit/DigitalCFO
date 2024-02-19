package com.idos.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.inject.Inject;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;

import model.Branch;
import model.CustomerBillwiseOpBalance;
import model.Transaction;
import model.Users;
import model.Vendor;
import model.VendorBillwiseOpBalance;
import play.libs.Json;

/**
 * Created by Ankush A. Sapkal
 */

public class VendCustBillwiseOpeningBalanceDAOImpl implements VendCustBillWiseOpeningBalanceDAO {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Override
	public boolean saveVendorOpeningBalance(String billwiseOpeningBalance, Vendor vendor, Users user,
			EntityManager entityManager) throws IDOSException {
		try {
			if (billwiseOpeningBalance != null && !"".equals(billwiseOpeningBalance) && billwiseOpeningBalance != "") {
				JSONArray arrJSON = new JSONArray(billwiseOpeningBalance);
				for (int i = 0; i < arrJSON.length(); i++) {

					JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
					Long branchId = rowItemData.get("branch") == null || "".equals(rowItemData.getString("branch"))
							? null
							: rowItemData.getLong("branch");
					JSONArray arrInnerJSON = rowItemData.getJSONArray("billDetails");
					if (arrInnerJSON.length() > 0 && branchId != null) {
						for (int j = 0; j < arrInnerJSON.length(); j++) {
							JSONObject rowItemInnerData = new JSONObject(arrInnerJSON.get(j).toString());
							Long id = rowItemInnerData.get("id") == null || "".equals(rowItemInnerData.getString("id"))
									? null
									: rowItemInnerData.getLong("id");
							String billDate = rowItemInnerData.get("billDate") == null
									|| "".equals(rowItemInnerData.getString("billDate")) ? null
											: rowItemInnerData.getString("billDate");
							String billNo = rowItemInnerData.get("billNumber") == null
									|| "".equals(rowItemInnerData.getString("billNumber")) ? null
											: rowItemInnerData.getString("billNumber");
							Double billAmt = rowItemInnerData.get("billAmount") == null
									|| "".equals(rowItemInnerData.getString("billAmount")) ? null
											: rowItemInnerData.getDouble("billAmount");
							String status = rowItemInnerData.get("status") == null
									|| "".equals(rowItemInnerData.getString("status")) ? null
											: rowItemInnerData.getString("status");
							VendorBillwiseOpBalance vendOpeningBalance = new VendorBillwiseOpBalance();
							vendOpeningBalance.setOrganization(user.getOrganization());
							vendOpeningBalance.setVendor(vendor);
							if (branchId != null) {
								Branch branch = Branch.findById(branchId);
								vendOpeningBalance.setBranch(branch);
							}
							if (id != null) {
								VendorBillwiseOpBalance opBalObj = VendorBillwiseOpBalance.findById(id);
								if (opBalObj != null) {
									vendOpeningBalance = opBalObj;
								}

							}
							if (status != null && status.equals("DEL")) {
								vendOpeningBalance.setPresentStatus(0);
							} else {
								if (billDate != null) {
									vendOpeningBalance.setBillDate(IdosConstants.IDOSDF.parse(billDate));
								}

								if (billNo != null) {
									vendOpeningBalance.setBillNo(billNo);
								}

								if (billAmt != null) {
									vendOpeningBalance.setBillAmount(billAmt);
									vendOpeningBalance.setOpeningBalance(billAmt);
								}
							}
							genericDao.saveOrUpdate(vendOpeningBalance, user, entityManager);
						}
					}
				}
			}

		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
					"Error on save/update multiitems.", ex.getMessage());
		}
		return true;

	}

	@Override
	public void getVendorOpeningBalance(ObjectNode result, Users user, Vendor vendor, EntityManager entityManager)
			throws IDOSException {
		ArrayNode billwiseOpBalanceData = result.putArray("billwiseOpBalanceData");
		Map<String, Object> criterias = new HashMap<String, Object>();
		List<Branch> branchConfigForVendor = VendorBillwiseOpBalance.findBranchWithOpeningBalance(entityManager,
				user.getOrganization().getId(), vendor.getId());
		if (branchConfigForVendor != null && branchConfigForVendor.size() > 0) {
			for (Branch branch : branchConfigForVendor) {
				criterias.clear();
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("branch.id", branch.getId());
				criterias.put("vendor.id", vendor.getId());
				criterias.put("presentStatus", 1);
				List<VendorBillwiseOpBalance> openingBalanceVend = genericDao
						.findByCriteria(VendorBillwiseOpBalance.class, criterias, entityManager);
				if (openingBalanceVend != null && openingBalanceVend.size() > 0) {
					ObjectNode row = Json.newObject();
					row.put("branchId", branch.getId());
					ArrayNode billDetails = row.putArray("billDetails");
					for (VendorBillwiseOpBalance openBalBillWise : openingBalanceVend) {
						ObjectNode innerRow = Json.newObject();
						innerRow.put("id", openBalBillWise.getId());
						innerRow.put("billDate", IdosConstants.IDOSDF.format(openBalBillWise.getBillDate()));
						innerRow.put("billNo", openBalBillWise.getBillNo());
						innerRow.put("billAmt", IdosConstants.decimalFormat.format(openBalBillWise.getBillAmount()));
						innerRow.put("openingBalance",
								IdosConstants.decimalFormat.format(openBalBillWise.getOpeningBalance()));
						innerRow.put("editStatus",
								Transaction.isTxnLinkedWithBillwiseOpeningBal(entityManager,
										user.getOrganization().getId(), openBalBillWise.getId().toString(),
										IdosConstants.TXN_TYPE_OPENING_BALANCE_BILLWISE_VEND));
						billDetails.add(innerRow);
					}
					billwiseOpBalanceData.add(row);
				}
			}
		}

	}

	@Override
	public boolean saveCustomerOpeningBalance(Object billwiseOpeningBalance, Vendor customer, Users user,
			EntityManager entityManager) throws IDOSException {
		try {
			if (billwiseOpeningBalance != null && !"".equals(billwiseOpeningBalance) && billwiseOpeningBalance != "") {
				JSONArray arrJSON = new JSONArray(billwiseOpeningBalance.toString());
				for (int i = 0; i < arrJSON.length(); i++) {
					JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
					Long branchId = rowItemData.get("branch") == null || "".equals(rowItemData.getString("branch"))
							? null
							: rowItemData.getLong("branch");
					JSONArray arrInnerJSON = rowItemData.getJSONArray("billDetails");
					if (arrInnerJSON.length() > 0 && branchId != null) {
						for (int j = 0; j < arrInnerJSON.length(); j++) {
							JSONObject rowItemInnerData = new JSONObject(arrInnerJSON.get(j).toString());
							Long id = rowItemInnerData.get("id") == null || "".equals(rowItemInnerData.getString("id"))
									? null
									: rowItemInnerData.getLong("id");
							String billDate = rowItemInnerData.get("billDate") == null
									|| "".equals(rowItemInnerData.getString("billDate")) ? null
											: rowItemInnerData.getString("billDate");
							String billNo = rowItemInnerData.get("billNumber") == null
									|| "".equals(rowItemInnerData.getString("billNumber")) ? null
											: rowItemInnerData.getString("billNumber");
							Double billAmt = rowItemInnerData.get("billAmount") == null
									|| "".equals(rowItemInnerData.getString("billAmount")) ? null
											: rowItemInnerData.getDouble("billAmount");
							String status = rowItemInnerData.get("status") == null
									|| "".equals(rowItemInnerData.getString("status")) ? null
											: rowItemInnerData.getString("status");
							CustomerBillwiseOpBalance custOpeningBalance = new CustomerBillwiseOpBalance();
							custOpeningBalance.setOrganization(user.getOrganization());
							custOpeningBalance.setCustomer(customer);
							if (branchId != null) {
								Branch branch = Branch.findById(branchId);
								custOpeningBalance.setBranch(branch);
							}
							if (id != null) {
								CustomerBillwiseOpBalance opBalObj = CustomerBillwiseOpBalance.findById(id);
								if (opBalObj != null) {
									custOpeningBalance = opBalObj;
								}
							}
							if (status != null && status.equals("DEL")) {
								custOpeningBalance.setPresentStatus(0);
							} else {
								if (billDate != null) {
									custOpeningBalance.setBillDate(IdosConstants.IDOSDF.parse(billDate));
								}

								if (billNo != null) {
									custOpeningBalance.setBillNo(billNo);
								}

								if (billAmt != null) {
									custOpeningBalance.setBillAmount(billAmt);
									custOpeningBalance.setOpeningBalance(billAmt);
								}
							}
							genericDao.saveOrUpdate(custOpeningBalance, user, entityManager);
						}
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
					"Error on save/update multiitems.", ex.getMessage());
		}
		return true;
	}

	@Override
	public void getCustomerOpeningBalance(ObjectNode result, Users user, Vendor customer, EntityManager entityManager)
			throws IDOSException {
		ArrayNode billwiseOpBalanceData = result.putArray("billwiseOpBalanceData");
		Map<String, Object> criterias = new HashMap<String, Object>();
		List<Branch> branchConfigForCust = CustomerBillwiseOpBalance.findBranchWithOpeningBalance(entityManager,
				user.getOrganization().getId(), customer.getId());
		if (branchConfigForCust != null && branchConfigForCust.size() > 0) {
			for (Branch branch : branchConfigForCust) {
				criterias.clear();
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("branch.id", branch.getId());
				criterias.put("customer.id", customer.getId());
				criterias.put("presentStatus", 1);
				List<CustomerBillwiseOpBalance> openingBalanceCust = genericDao
						.findByCriteria(CustomerBillwiseOpBalance.class, criterias, entityManager);
				if (openingBalanceCust != null && openingBalanceCust.size() > 0) {
					ObjectNode row = Json.newObject();
					row.put("branchId", branch.getId());
					ArrayNode billDetails = row.putArray("billDetails");
					for (CustomerBillwiseOpBalance openBalBillWise : openingBalanceCust) {
						ObjectNode innerRow = Json.newObject();
						innerRow.put("id", openBalBillWise.getId());
						if (openBalBillWise.getBillDate() != null) {
							innerRow.put("billDate", IdosConstants.IDOSDF.format(openBalBillWise.getBillDate()));
						}
						innerRow.put("billNo", openBalBillWise.getBillNo());
						innerRow.put("billAmt", openBalBillWise.getBillAmount());
						innerRow.put("openingBalance", openBalBillWise.getOpeningBalance());
						innerRow.put("editStatus",
								Transaction.isTxnLinkedWithBillwiseOpeningBal(entityManager,
										user.getOrganization().getId(), openBalBillWise.getId().toString(),
										IdosConstants.TXN_TYPE_OPENING_BALANCE_BILLWISE_CUST));
						billDetails.add(innerRow);
					}
					billwiseOpBalanceData.add(row);
				}
			}
		}

	}
}