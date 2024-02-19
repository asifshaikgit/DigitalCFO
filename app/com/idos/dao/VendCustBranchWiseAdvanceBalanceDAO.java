package com.idos.dao;

import com.idos.util.IDOSException;
import javax.persistence.EntityManager;
import model.Users;
import model.Vendor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;

public interface VendCustBranchWiseAdvanceBalanceDAO extends BaseDAO {

        public boolean saveVendorAdvanceBalance(String branchWiseAdvanceBalance, Vendor vendor, Users user,
                        EntityManager entityManager) throws IDOSException;

        public boolean saveCustomeAdvanceBalance(Object branchWiseAdvanceBalance, Vendor customer, Users user,
                        EntityManager entityManager) throws IDOSException;

        public void getVendorAdvanceBalance(ObjectNode result, Users user, Vendor vendor, EntityManager entityManager)
                        throws IDOSException;

        public void getCustomerAdvanceBalance(ObjectNode result, Users user, Vendor customer,
                        EntityManager entityManager)
                        throws IDOSException;

}
