package com.idos.dao;

import com.idos.dao.BaseDAO;
import com.idos.util.IDOSException;
import model.BillOfMaterialTxnModel;
import model.Branch;
import model.Specifics;
import model.Users;
import com.fasterxml.jackson.databind.node.ArrayNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * @author Sunil K. Namdev created on 20.07.2019
 */
public interface BillOfMaterialTxnItemDAO extends BaseDAO {
    void getListOfTxnItems(Users user, EntityManager em, long txnId, ArrayNode txnItemsAn) throws IDOSException;

    boolean updateFullfilDetail(Users user, EntityManager em, BillOfMaterialTxnModel bomTxn, double unitsToFullfil,
            Specifics specific, Branch branch, String mainTxnRefNo) throws IDOSException;

    boolean isAllItemsFullfilled(Users user, EntityManager em, BillOfMaterialTxnModel bomTxn) throws IDOSException;
}
