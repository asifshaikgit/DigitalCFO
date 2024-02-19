package service;

import com.idos.dao.CustomerDAO;
import com.idos.dao.CustomerDAOImpl;
import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import com.idos.util.IDOSException;
import model.Users;
import model.Vendor;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Result;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

/**
 * Created by Sunil Namdev on 02-02-2017.
 */
public interface CustomerService extends BaseService {

    Vendor saveCustomer(JsonNode json, Users user, EntityManager em, EntityTransaction transaction, int customerType)
            throws IDOSException;

    boolean saveCustomerShippingDetail(JsonNode json, Users user, EntityManager em, EntityTransaction transaction)
            throws IDOSException;

    boolean saveWalkinCustomerDetail(JsonNode json, String walkinCustomerName, Users user, EntityManager entityManager)
            throws IDOSException;
}
