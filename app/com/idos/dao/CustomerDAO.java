package com.idos.dao;

import com.idos.util.IDOSException;
import model.Users;
import model.Vendor;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import service.CustomerService;
import service.FileUploadService;
import service.FileUploadServiceImpl;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

/**
 * Created by Sunil Namdev on 02-02-2017.
 */
public interface CustomerDAO extends BaseDAO {
    // final String CUSTOMR_JQL = "select obj from Vendor obj where obj.name= ?1 and
    // obj.id=?2 and obj.presentStatus=1";
    String CUSTOMR_JQL_MASTER = "select obj from Vendor obj where obj.name=?1 and obj.id=?2 and obj.presentStatus=1";
    String CUSTOMR_JQL_OTHERS = "select obj from Vendor obj where obj.name=?1 and obj.id=?2 and obj.presentStatus=0";

    Vendor saveCustomer(JsonNode json, Users users, EntityManager entityManager, EntityTransaction transaction,
            int customerType, int txnWalkinCustomerType) throws IDOSException;

    boolean saveCustomerShippingDetail(JsonNode json, Users users, EntityManager entityManager,
            EntityTransaction transaction) throws IDOSException;

    public boolean saveWalkinCustomerDetail(JsonNode json, String walkinCustomerName, Users user,
            EntityManager entityManager) throws IDOSException;
}
