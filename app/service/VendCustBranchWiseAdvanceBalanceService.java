package service;

import com.idos.util.IDOSException;

import model.Users;
import model.Vendor;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;

import model.Branch;
import model.Specifics;

public interface VendCustBranchWiseAdvanceBalanceService extends BaseService {
        public boolean saveVendorAdvanceBalance(String branchwiseAdvanceBalance, Vendor vendor, Users user,
                        EntityManager entityManager) throws IDOSException;

        public boolean saveCustomeAdvanceBalance(String branchwiseAdvanceBalance, Vendor customer, Users user,
                        EntityManager entityManager) throws IDOSException;

        public void getVendorAdvanceBalance(ObjectNode result, Users user, Vendor vendor, EntityManager entityManager)
                        throws IDOSException;

        public void getCustomerAdvanceBalance(ObjectNode result, Users user, Vendor customer,
                        EntityManager entityManager)
                        throws IDOSException;
}
