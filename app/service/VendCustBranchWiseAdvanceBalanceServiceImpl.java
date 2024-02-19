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

public class VendCustBranchWiseAdvanceBalanceServiceImpl implements VendCustBranchWiseAdvanceBalanceService {
    @Override
    public boolean saveVendorAdvanceBalance(String branchwiseAdvanceBalance, Vendor vendor, Users user,
            EntityManager entityManager) throws IDOSException {
        // return
        // BRANCHWISE_ADVANCE_BAL_DAO.saveVendorAdvanceBalance(branchwiseAdvanceBalance,
        // vendor, user,
        // entityManager);
        return false;
    }

    @Override
    public boolean saveCustomeAdvanceBalance(String branchwiseAdvanceBalance, Vendor customer, Users user,
            EntityManager entityManager) throws IDOSException {
        // TODO Auto-generated method stub
        // return
        // BRANCHWISE_ADVANCE_BAL_DAO.saveCustomeAdvanceBalance(branchwiseAdvanceBalance,
        // customer, user,
        // entityManager);
        return false;
    }

    @Override
    public void getVendorAdvanceBalance(ObjectNode result, Users user, Vendor vendor, EntityManager entityManager)
            throws IDOSException {
        // BRANCHWISE_ADVANCE_BAL_DAO.getVendorAdvanceBalance(result, user, vendor,
        // entityManager);
    }

    @Override
    public void getCustomerAdvanceBalance(ObjectNode result, Users user, Vendor customer, EntityManager entityManager)
            throws IDOSException {
        // BRANCHWISE_ADVANCE_BAL_DAO.getCustomerAdvanceBalance(result, user, customer,
        // entityManager);
    }
}
