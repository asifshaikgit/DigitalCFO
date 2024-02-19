package com.idos.dao;

import java.util.Date;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;

import com.idos.util.IDOSException;

import model.Branch;
import model.Specifics;
import model.Users;
import model.Vendor;

/**
 * Created by Ankush A. Sapkal
 */

public interface VendCustBillWiseOpeningBalanceDAO extends BaseDAO {

	public boolean saveVendorOpeningBalance(String billwiseOpeningBalance, Vendor vendor, Users user,
			EntityManager entityManager) throws IDOSException;

	public boolean saveCustomerOpeningBalance(Object billwiseOpeningBalance, Vendor customer, Users user,
			EntityManager entityManager) throws IDOSException;

	public void getVendorOpeningBalance(ObjectNode result, Users user, Vendor vendor, EntityManager entityManager)
			throws IDOSException;

	public void getCustomerOpeningBalance(ObjectNode result, Users user, Vendor customer, EntityManager entityManager)
			throws IDOSException;

}