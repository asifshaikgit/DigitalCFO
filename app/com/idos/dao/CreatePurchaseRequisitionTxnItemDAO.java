package com.idos.dao;

import com.idos.dao.BaseDAO;
import com.idos.util.IDOSException;
import model.PurchaseRequisitionTxnModel;
import model.Branch;
import model.Specifics;
import model.Users;
import com.fasterxml.jackson.databind.node.ArrayNode;

import javax.persistence.EntityManager;

/**
 * @author Harish Kumar created on 05.05.2023
 */
public interface CreatePurchaseRequisitionTxnItemDAO extends BaseDAO {
    void getListOfTxnItems(Users user, EntityManager em, long txnId, ArrayNode txnItemsAn) throws IDOSException;
    
}

