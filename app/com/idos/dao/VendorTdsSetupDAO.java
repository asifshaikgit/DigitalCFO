package com.idos.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.IDOSException;

import model.Specifics;
import model.Users;
import model.Vendor;
import pojo.WithHoldingTaxPojo;

/**
 * Created by Ankush A. Sapkal
 */

public interface VendorTdsSetupDAO extends BaseDAO {

	public boolean saveVendorBasicTds(JsonNode json, Users user, EntityManager entityManager) throws IDOSException;

	public void getVendorBasicTdsDetails(ObjectNode result, Users user, EntityManager entityManager)
			throws IDOSException;

	public ObjectNode getVendorBasicRowData(ObjectNode result, Users user, EntityManager entityManager)
			throws IDOSException;

	public boolean saveVendorAdvanceTds(JsonNode json, Users user, EntityManager entityManager) throws IDOSException;

	public ObjectNode getVendorAdvanceRowData(ObjectNode result, Users user, Long ledger, EntityManager entityManager)
			throws IDOSException;

	public List<WithHoldingTaxPojo> calculateTds(ObjectNode result, ArrayNode TdsDetail, Users user, Specifics spec,
			Vendor vendor, Double txnGrossAmt, Double newTxnNetAmount, Long txnPurposeValue, Date txnDate,
			EntityManager entityManager, boolean isCalledFromTxn) throws IDOSException;

	public void getVendorAdvanceTdsDetails(ObjectNode result, Users user, EntityManager entityManager)
			throws IDOSException;

	public boolean saveVendorTdsSetup(JsonNode json, Users user, Vendor vendor, EntityManager entityManager)
			throws IDOSException;

	public boolean saveCOATdsSetup(JsonNode json, Users user, Specifics specific, EntityManager entityManager)
			throws IDOSException;
}
