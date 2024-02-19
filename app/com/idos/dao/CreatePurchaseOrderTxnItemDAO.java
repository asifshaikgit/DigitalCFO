package com.idos.dao;

import com.idos.dao.BaseDAO;
import com.idos.util.IDOSException;
import model.PurchaseOrderTxnModel;
import model.Branch;
import model.PurchaseOrderTxnItemModel;
import model.Specifics;
import model.Users;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

import javax.persistence.EntityManager;

/**
 * @author Harish Kumar created on 30.05.2023
 */
public interface CreatePurchaseOrderTxnItemDAO extends BaseDAO {
    void getListOfTxnItems(Users user, EntityManager em, long txnId, ArrayNode txnItemsAn) throws IDOSException;
    List<PurchaseOrderTxnItemModel> getAllItemsOfPO(Users user, EntityManager em, long txnId) throws IDOSException;    
}




