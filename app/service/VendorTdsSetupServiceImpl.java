package service;

import java.util.Date;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.IDOSException;

import model.Specifics;
import model.Users;
import model.Vendor;

public class VendorTdsSetupServiceImpl implements VendorTdsSetupService {

	@Override
	public boolean saveVendorBasicTds(JsonNode json, Users user, EntityManager entityManager) throws IDOSException {
		return VENDOR_TDS_DAO.saveVendorBasicTds(json, user, entityManager);
	}

	@Override
	public void getVendorBasicTdsDetails(ObjectNode result, Users user, EntityManager entityManager)
			throws IDOSException {
		VENDOR_TDS_DAO.getVendorBasicTdsDetails(result, user, entityManager);
	}

	@Override
	public ObjectNode getVendorBasicRowData(ObjectNode result, Users user, EntityManager entityManager)
			throws IDOSException {
		return VENDOR_TDS_DAO.getVendorBasicRowData(result, user, entityManager);
	}

	@Override
	public boolean saveVendorAdvanceTds(JsonNode json, Users user, EntityManager entityManager) throws IDOSException {
		return VENDOR_TDS_DAO.saveVendorAdvanceTds(json, user, entityManager);
	}

	@Override
	public ObjectNode getVendorAdvanceRowData(ObjectNode result, Users user, Long ledger, EntityManager entityManager)
			throws IDOSException {
		return VENDOR_TDS_DAO.getVendorAdvanceRowData(result, user, ledger, entityManager);
	}

	@Override
	public ObjectNode calculateTds(ObjectNode result, ArrayNode TdsDetail, Users user, Specifics spec, Vendor vendor,
			Double txnGrossAmt, Double newTxnNetAmount, Long txnPurposeValue, Date txnDate, EntityManager entityManager)
			throws IDOSException {
		VENDOR_TDS_DAO.calculateTds(result, TdsDetail, user, spec, vendor, txnGrossAmt, newTxnNetAmount,
				txnPurposeValue, txnDate, entityManager, false);
		return result;
	}

	@Override
	public void getVendorAdvanceTdsDetails(ObjectNode result, Users user, EntityManager entityManager)
			throws IDOSException {
		VENDOR_TDS_DAO.getVendorAdvanceTdsDetails(result, user, entityManager);

	}

	@Override
	public boolean saveVendorTdsSetup(JsonNode json, Users user, Vendor vendor, EntityManager entityManager)
			throws IDOSException {
		return VENDOR_TDS_DAO.saveVendorTdsSetup(json, user, vendor, entityManager);
	}

	@Override
	public boolean saveCOATdsSetup(JsonNode json, Users user, Specifics specific, EntityManager entityManager)
			throws IDOSException {
		return VENDOR_TDS_DAO.saveCOATdsSetup(json, user, specific, entityManager);
	}

}
