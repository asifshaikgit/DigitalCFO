package service;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import com.idos.dao.SellTransactionDAO;
import com.idos.dao.SellTransactionDAOImpl;
import com.idos.util.IDOSException;
import model.Transaction;
import model.TransactionItems;
import model.Users;
import model.Vendor;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import pojo.TaxPojo;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * Created by Sunil Namdev on 05-12-2016.
 */
public interface TransactionItemsService extends BaseService {
    String VENDOR_SPECIFIC_HQL = "select obj from VendorSpecific obj WHERE obj.organization.id=?1 and obj.specificsVendors.id=?2 and obj.vendorSpecific.id=?3 and obj.presentStatus=1";
    ChartOfAccountsService CHART_OF_ACCOUNTS_SERVICE = new ChartOfAccountsServiceImpl();

    public void insertMultipleItemsTransactionItems(EntityManager entityManager, Users user, JSONArray arrJSON,
            Transaction transaction, Date txnDate) throws IDOSException;

    public void updateMultipleItemsTransactionItems(EntityManager entityManager, Users user, JSONArray arrJSON,
            Transaction transaction) throws IDOSException;

    public void updateMultipleItemsSalesReturnTransactionItems(EntityManager entityManager, Users user,
            JSONArray arrJSON, Transaction transaction) throws IDOSException;

    Long saveMultiItemsTransRecAdvCust(EntityManager entityManager, Users user, JsonNode json, Transaction transaction,
            Vendor customer, ObjectNode result) throws IDOSException;

    TaxPojo saveTransactionTaxes(TransactionItems transactionItem, JSONObject rowItemData, Transaction txn)
            throws Exception;

    Long saveMultiItemsTransPayAdvVend(EntityManager entityManager, Users user, JsonNode json, Transaction transaction,
            Vendor customer, ObjectNode result) throws IDOSException;
}
