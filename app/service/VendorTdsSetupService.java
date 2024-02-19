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

/**
 * Created by Ankush A. Sapkal
 */

public interface VendorTdsSetupService extends BaseService {
	public boolean saveVendorBasicTds(JsonNode json, Users user, EntityManager entityManager) throws IDOSException;

	public void getVendorBasicTdsDetails(ObjectNode result, Users user, EntityManager entityManager)
			throws IDOSException;

	public ObjectNode getVendorBasicRowData(ObjectNode result, Users user, EntityManager entityManager)
			throws IDOSException;

	public boolean saveVendorAdvanceTds(JsonNode json, Users user, EntityManager entityManager) throws IDOSException;

	public ObjectNode getVendorAdvanceRowData(ObjectNode result, Users user, Long ledger, EntityManager entityManager)
			throws IDOSException;

	public ObjectNode calculateTds(ObjectNode result, ArrayNode TdsDetail, Users user, Specifics spec, Vendor vendor,
			Double txnGrossAmt, Double newTxnNetAmount, Long txnPurposeValue, Date txnDate, EntityManager entityManager)
			throws IDOSException;

	public void getVendorAdvanceTdsDetails(ObjectNode result, Users user, EntityManager entityManager)
			throws IDOSException;

	public boolean saveVendorTdsSetup(JsonNode json, Users user, Vendor vendor, EntityManager entityManager)
			throws IDOSException;

	public boolean saveCOATdsSetup(JsonNode json, Users user, Specifics specific, EntityManager entityManager)
			throws IDOSException;
}
