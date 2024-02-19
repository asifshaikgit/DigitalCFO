package com.idos.dao;

import com.idos.util.IDOSException;
import model.Specifics;
import model.Users;
import com.fasterxml.jackson.databind.node.ArrayNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.List;

/**
 * @auther Sunil Namdev created on 09.07.2018
 */
public interface UserSetupDAO extends BaseDAO {
    String SPECIFIC_HQL = "select obj from UserRightSpecifics obj where  user.id=?1 and userRights.id=?2 and particulars.accountCode = ?3 and obj.presentStatus=1";
    String ALL_SPECIFIC_HQL = "select obj from UserRightSpecifics obj where user.id=?1 and userRights.id=?2 and obj.presentStatus=1";

    String ALL_CASH_HQL = "select obj from UserRightCash obj where organization.id=?1 and user.id=?2 and userRights.id=?3 and cashType=?4 and particular=?5 and obj.presentStatus=1";
    String ALL_BANK_HQL = "select obj from UserRightBank obj where organization.id=?1 and user.id=?2 and userRights.id=?3 and particular=?4 and obj.presentStatus=1";
    String ALL_CUST_VEND_HQL = "select obj from UserRightCustomer obj where organization.id=?1 and user.id=?2 and userRights.id=?3 and particular=?4 and obj.presentStatus=1";
    String ALL_TAX_HQL = "select obj from UserRightTax obj where organization.id=?1 and user.id=?2 and userRights.id=?3 and particular=?4 and obj.presentStatus=1";
    String ALL_INTERBRANCH_HQL = "select obj from UserRightInterBranch obj where organization.id=?1 and user.id=?2 and userRights.id=?3 and particular=?4 and obj.presentStatus=1";
    String ALL_USER_HQL = "select obj from UserRightUsers obj where organization.id=?1 and user.id=?2 and userRights.id=?3 and particular=?4 and obj.presentStatus=1";

    boolean getAssetsCoaNodes(ArrayNode assetsCOAAn, EntityManager em, Users user, Specifics specifics);

    boolean getLiabilitiesCoaNodes(ArrayNode liabilitiesCoaAn, EntityManager em, Users user, Specifics specifics);

    void saveUpdateTxnRule4Specific(List<Long> items, String fromAmounts, String toAmounts, EntityManager em,
            Users newUser, Integer particular, Long userRight, boolean isNew) throws IDOSException;

    void saveUpdateTxnRule4Cash(List<Long> items, String fromAmounts, String toAmounts, EntityManager em, Users newUser,
            Integer particular, Long userRight, boolean isNew, int headType) throws IDOSException;

    void saveUpdateTxnRule4Bank(List<Long> items, String fromAmounts, String toAmounts, EntityManager em, Users newUser,
            Integer particular, Long usrRight, boolean isNew, int headType) throws IDOSException;

    void saveUpdateTxnRule4Customer(List<Long> items, String fromAmounts, String toAmounts, EntityManager em,
            Users newUser, Integer particular, Long userRight, boolean isNew, int headType) throws IDOSException;

    void saveUpdateTxnRule4Taxes(List<Long> items, String fromAmounts, String toAmounts, EntityManager em,
            Users newUser, Integer particular, Long usrRight, boolean isNew, int headType) throws IDOSException;

    void saveUpdateTxnRule4InterBranch(List<Long> items, String fromAmounts, String toAmounts, EntityManager em,
            Users newUser, Integer particular, Long usrRight, boolean isNew, int headType) throws IDOSException;

    void saveUpdateTxnRule4User(List<Long> items, String fromAmounts, String toAmounts, EntityManager em, Users newUser,
            Integer particular, Long usrRight, boolean isNew, int headType) throws IDOSException;
}
