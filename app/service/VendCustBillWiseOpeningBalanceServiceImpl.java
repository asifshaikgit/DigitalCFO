package service;

import com.idos.util.DateUtil;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import controllers.StaticController;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;

import com.typesafe.config.Config;
import play.db.jpa.JPAApi;
import play.libs.Json;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * Created by Ankush A. Sapkal
 */

public class VendCustBillWiseOpeningBalanceServiceImpl implements VendCustBillWiseOpeningBalanceService {

	@Override
	public boolean saveVendorOpeningBalance(String billwiseOpeningBalance, Vendor vendor, Users user,
			EntityManager entityManager) throws IDOSException {
		return BILLWISE_OPENING_BAL_DAO.saveVendorOpeningBalance(billwiseOpeningBalance, vendor, user, entityManager);
	}

	@Override
	public boolean saveCustomerOpeningBalance(String billwiseOpeningBalance, Vendor customer, Users user,
			EntityManager entityManager) throws IDOSException {
		// TODO Auto-generated method stub
		return BILLWISE_OPENING_BAL_DAO.saveCustomerOpeningBalance(billwiseOpeningBalance, customer, user,
				entityManager);
	}

	@Override
	public void getVendorOpeningBalance(ObjectNode result, Users user, Vendor vendor, EntityManager entityManager)
			throws IDOSException {
		BILLWISE_OPENING_BAL_DAO.getVendorOpeningBalance(result, user, vendor, entityManager);
	}

	@Override
	public void getCustomerOpeningBalance(ObjectNode result, Users user, Vendor customer, EntityManager entityManager)
			throws IDOSException {
		BILLWISE_OPENING_BAL_DAO.getCustomerOpeningBalance(result, user, customer, entityManager);
	}

}