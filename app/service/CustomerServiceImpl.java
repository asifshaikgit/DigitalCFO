package service;

import com.idos.util.IDOSException;
import com.idos.util.ListUtility;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import play.db.jpa.JPAApi;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sunil Namdev on 02-02-2017.
 */
public class CustomerServiceImpl implements CustomerService {
    @Override
    public Vendor saveCustomer(JsonNode json, Users user, EntityManager em, EntityTransaction transaction,
            int customerType) throws IDOSException {
        return customerDao.saveCustomer(json, user, em, transaction, customerType, 0);
    }

    @Override
    public boolean saveCustomerShippingDetail(JsonNode json, Users user, EntityManager em,
            EntityTransaction transaction) throws IDOSException {
        return customerDao.saveCustomerShippingDetail(json, user, em, transaction);
    }

    @Override
    public boolean saveWalkinCustomerDetail(JsonNode json, String walkinCustomerName, Users user,
            EntityManager entityManager) throws IDOSException {
        return customerDao.saveWalkinCustomerDetail(json, walkinCustomerName, user, entityManager);
    }
}
