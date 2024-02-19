package com.idos.dao;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import com.idos.util.ListUtility;
import model.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @auther Sunil Namdev created on 09.07.2018
 */
public class UserSetupDAOImpl implements UserSetupDAO {

    @Override
    public void saveUpdateTxnRule4Specific(List<Long> items, String fromAmounts, String toAmounts, EntityManager em,
            Users newUser, Integer particular, Long usrRight, boolean isNew) throws IDOSException {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "Start items:" + items + " " + fromAmounts + " " + toAmounts);
        String sql = null;
        ArrayList inparams = new ArrayList<>(3);
        inparams.add(newUser.getId());
        inparams.add(usrRight);
        if (!isNew) {
            Long accountCode = 1000000000000000000L;
            if (particular == 2) {
                accountCode = 2000000000000000000L;
            } else if (particular == 3) {
                accountCode = 3000000000000000000L;
            } else if (particular == 4) {
                accountCode = 4000000000000000000L;
            }
            inparams.add(accountCode);
            sql = SPECIFIC_HQL;
        } else {
            sql = ALL_SPECIFIC_HQL;
        }
        List<UserRightSpecifics> olduserCreationRightForCOA = genericDao.queryWithParams(sql, em, inparams);
        List<UserRightSpecifics> newuserCreationRightForCOA = new ArrayList<UserRightSpecifics>();
        String fromAmountArray[] = fromAmounts.split(",");
        String toAmountArray[] = toAmounts.split(",");
        int count = 0;
        UserRights usrRights = UserRights.findById(usrRight);
        for (Long specificID : items) {
            UserRightSpecifics newUserCreationRight = new UserRightSpecifics();
            Specifics usrRightForCOA = Specifics.findById(specificID);
            newUserCreationRight.setUser(newUser);
            newUserCreationRight.setUserRights(usrRights);
            newUserCreationRight.setSpecifics(usrRightForCOA);
            newUserCreationRight.setParticulars(usrRightForCOA.getParticularsId());
            if (IdosConstants.AUDITOR_RIGHTS != usrRight) {
                if (count < fromAmountArray.length && fromAmountArray.length > 0
                        && !fromAmountArray[count].trim().equals("")) {
                    newUserCreationRight.setAmount(IdosUtil.convertStringToDouble(fromAmountArray[count]));
                } else {
                    newUserCreationRight.setAmount(0.0);
                }
                if (count < toAmountArray.length && toAmountArray.length > 0
                        && !toAmountArray[count].trim().equals("")) {
                    newUserCreationRight.setAmountTo(IdosUtil.convertStringToDouble(toAmountArray[count]));
                } else {
                    newUserCreationRight.setAmountTo(0.0);
                }
            }
            newuserCreationRightForCOA.add(newUserCreationRight);
            count = count + 1;
        }

        List<List<UserRightSpecifics>> userCreationRightCOATransactionList = ListUtility
                .getUserRightsInCOAList(olduserCreationRightForCOA, newuserCreationRightForCOA);
        for (int i = 0; i < userCreationRightCOATransactionList.size(); i++) {
            if (i == 0) {
                List<UserRightSpecifics> oldRightsOnItems = userCreationRightCOATransactionList.get(i);
                if (oldRightsOnItems != null) {
                    for (UserRightSpecifics userRightObj : oldRightsOnItems) {
                        em.remove(userRightObj);
                    }
                }
            }
            if (i == 1) {
                List<UserRightSpecifics> newRightsOnItems = userCreationRightCOATransactionList.get(i);
                if (newRightsOnItems != null) {
                    for (UserRightSpecifics userRightObj : newRightsOnItems) {
                        genericDao.saveOrUpdate(userRightObj, newUser, em);
                    }
                }
            }
            if (i == 2) {
                List<UserRightSpecifics> updateRightsOnItems = userCreationRightCOATransactionList.get(i);
                if (updateRightsOnItems != null) {
                    for (UserRightSpecifics userRightObj : updateRightsOnItems) {
                        genericDao.saveOrUpdate(userRightObj, newUser, em);
                    }
                }
            }
        }
    }

    @Override
    public void saveUpdateTxnRule4Cash(List<Long> items, String fromAmounts, String toAmounts, EntityManager em,
            Users newUser, Integer particular, Long usrRight, boolean isNew, int headType) throws IDOSException {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "Start " + fromAmounts + " " + toAmounts + " " + items);
        String sql = null;
        ArrayList inparams = new ArrayList<>(5);
        inparams.add(newUser.getOrganization().getId());
        inparams.add(newUser.getId());
        inparams.add(usrRight);
        inparams.add(headType);
        inparams.add(particular);
        sql = ALL_CASH_HQL;

        List<UserRightCash> oldItemList = genericDao.queryWithParams(sql, em, inparams);
        List<UserRightCash> newItemList = new ArrayList<UserRightCash>();
        String fromAmountArray[] = fromAmounts.split(",");
        String toAmountArray[] = toAmounts.split(",");
        int count = 0;
        UserRights usrRights = UserRights.findById(usrRight);
        for (Long cashid : items) {
            BranchDepositBoxKey branchDepositBoxKey = BranchDepositBoxKey.findById(cashid);
            UserRightCash userRightCash = new UserRightCash();
            userRightCash.setUser(newUser);
            userRightCash.setUserRights(usrRights);
            userRightCash.setCash(branchDepositBoxKey);
            userRightCash.setOrganization(newUser.getOrganization());
            userRightCash.setParticular(particular);
            if (IdosConstants.AUDITOR_RIGHTS != usrRight) {
                if (count < fromAmountArray.length && fromAmountArray.length > 0
                        && !fromAmountArray[count].trim().equals("")) {
                    userRightCash.setFromAmount(IdosUtil.convertStringToDouble(fromAmountArray[count]));
                } else {
                    userRightCash.setFromAmount(0.0);
                }
                if (count < toAmountArray.length && toAmountArray.length > 0
                        && !toAmountArray[count].trim().equals("")) {
                    userRightCash.setToAmount(IdosUtil.convertStringToDouble(toAmountArray[count]));
                } else {
                    userRightCash.setToAmount(0.0);
                }
            }
            userRightCash.setCashType(headType);
            newItemList.add(userRightCash);
            count = count + 1;
        }

        List<List<UserRightCash>> userRightOnCash = ListUtility.getUserRightItems(oldItemList, newItemList);
        for (int i = 0; i < userRightOnCash.size(); i++) {
            if (i == 0) {
                List<UserRightCash> oldRightsOnItems = userRightOnCash.get(i);
                if (oldRightsOnItems != null) {
                    for (UserRightCash userRightObj : oldRightsOnItems) {
                        em.remove(userRightObj);
                    }
                }
            } else if (i == 1) {
                List<UserRightCash> newRightsOnItems = userRightOnCash.get(i);
                if (newRightsOnItems != null) {
                    for (UserRightCash userRightObj : newRightsOnItems) {
                        genericDao.saveOrUpdate(userRightObj, newUser, em);
                    }
                }
            } else if (i == 2) {
                List<UserRightCash> updateRightsOnItems = userRightOnCash.get(i);
                if (updateRightsOnItems != null) {
                    for (UserRightCash userRightObj : updateRightsOnItems) {
                        genericDao.saveOrUpdate(userRightObj, newUser, em);
                    }
                }
            }
        }
    }

    @Override
    public void saveUpdateTxnRule4Bank(List<Long> items, String fromAmounts, String toAmounts, EntityManager em,
            Users newUser, Integer particular, Long usrRight, boolean isNew, int headType) throws IDOSException {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "Start " + fromAmounts + " " + toAmounts + " " + items);
        String sql = null;
        ArrayList inparams = new ArrayList<>(4);
        inparams.add(newUser.getOrganization().getId());
        inparams.add(newUser.getId());
        inparams.add(usrRight);
        inparams.add(particular);
        sql = ALL_BANK_HQL;

        List<UserRightBank> oldItemList = genericDao.queryWithParams(sql, em, inparams);
        List<UserRightBank> newItemList = new ArrayList<UserRightBank>();
        String fromAmountArray[] = fromAmounts.split(",");
        String toAmountArray[] = toAmounts.split(",");
        int count = 0;
        UserRights usrRights = UserRights.findById(usrRight);
        for (Long itemid : items) {
            BranchBankAccounts branchBankAccount = BranchBankAccounts.findById(itemid);
            if (branchBankAccount == null) {
                throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                        IdosConstants.INVALID_DATA_EXCEPTION, "Bank is not provided.");
            }
            UserRightBank userRightBank = new UserRightBank();
            userRightBank.setUser(newUser);
            userRightBank.setUserRights(usrRights);
            userRightBank.setBank(branchBankAccount);
            userRightBank.setOrganization(newUser.getOrganization());
            userRightBank.setParticular(particular);
            if (IdosConstants.AUDITOR_RIGHTS != usrRight) {
                if (count < fromAmountArray.length && fromAmountArray.length > 0
                        && !fromAmountArray[count].trim().equals("")) {
                    userRightBank.setFromAmount(IdosUtil.convertStringToDouble(fromAmountArray[count]));
                } else {
                    userRightBank.setFromAmount(0.0);
                }
                if (count < toAmountArray.length && toAmountArray.length > 0
                        && !toAmountArray[count].trim().equals("")) {
                    userRightBank.setToAmount(IdosUtil.convertStringToDouble(toAmountArray[count]));
                } else {
                    userRightBank.setToAmount(0.0);
                }
            }
            newItemList.add(userRightBank);
            count = count + 1;
        }

        List<List<UserRightBank>> userRightOnItems = ListUtility.getUserRightItems(oldItemList, newItemList);
        for (int i = 0; i < userRightOnItems.size(); i++) {
            if (i == 0) {
                List<UserRightBank> oldRightsOnItems = userRightOnItems.get(i);
                if (oldRightsOnItems != null) {
                    for (UserRightBank userRightObj : oldRightsOnItems) {
                        em.remove(userRightObj);
                    }
                }
            } else if (i == 1) {
                List<UserRightBank> newRightsOnItems = userRightOnItems.get(i);
                if (newRightsOnItems != null) {
                    for (UserRightBank userRightObj : newRightsOnItems) {
                        genericDao.saveOrUpdate(userRightObj, newUser, em);
                    }
                }
            } else if (i == 2) {
                List<UserRightBank> updateRightsOnItems = userRightOnItems.get(i);
                if (updateRightsOnItems != null) {
                    for (UserRightBank userRightObj : updateRightsOnItems) {
                        genericDao.saveOrUpdate(userRightObj, newUser, em);
                    }
                }
            }
        }
    }

    @Override
    public void saveUpdateTxnRule4Customer(List<Long> items, String fromAmounts, String toAmounts, EntityManager em,
            Users newUser, Integer particular, Long usrRight, boolean isNew, int headType) throws IDOSException {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "Start " + fromAmounts + " " + toAmounts + " " + items + " head: " + headType);
        String sql = null;
        ArrayList inparams = new ArrayList<>(4);
        inparams.add(newUser.getOrganization().getId());
        inparams.add(newUser.getId());
        inparams.add(usrRight);
        inparams.add(particular);
        sql = ALL_CUST_VEND_HQL;

        List<UserRightCustomer> oldList = genericDao.queryWithParams(sql, em, inparams);
        List<UserRightCustomer> newList = new ArrayList<UserRightCustomer>();
        String fromAmountArray[] = fromAmounts.split(",");
        String toAmountArray[] = toAmounts.split(",");
        int count = 0;
        UserRights usrRights = UserRights.findById(usrRight);
        for (Long itemid : items) {
            Vendor customer = Vendor.findById(itemid);
            UserRightCustomer userRightCustomer = new UserRightCustomer();
            userRightCustomer.setUser(newUser);
            userRightCustomer.setUserRights(usrRights);
            userRightCustomer.setCustomer(customer);
            userRightCustomer.setOrganization(newUser.getOrganization());
            userRightCustomer.setType(customer.getType());
            userRightCustomer.setParticular(particular);
            if (IdosConstants.AUDITOR_RIGHTS != usrRight) {
                if (count < fromAmountArray.length && fromAmountArray.length > 0
                        && !fromAmountArray[count].trim().equals("")) {
                    userRightCustomer.setFromAmount(IdosUtil.convertStringToDouble(fromAmountArray[count]));
                } else {
                    userRightCustomer.setFromAmount(0.0);
                }
                if (count < toAmountArray.length && toAmountArray.length > 0
                        && !toAmountArray[count].trim().equals("")) {
                    userRightCustomer.setToAmount(IdosUtil.convertStringToDouble(toAmountArray[count]));
                } else {
                    userRightCustomer.setToAmount(0.0);
                }
            }
            newList.add(userRightCustomer);
            count = count + 1;
        }

        List<List<UserRightCustomer>> userRightsOnItems = ListUtility.getUserRightItems(oldList, newList);
        for (int i = 0; i < userRightsOnItems.size(); i++) {
            if (i == 0) {
                List<UserRightCustomer> oldRightsOnItems = userRightsOnItems.get(i);
                if (oldRightsOnItems != null) {
                    for (UserRightCustomer userRightObj : oldRightsOnItems) {
                        em.remove(userRightObj);
                    }
                }
            } else if (i == 1) {
                List<UserRightCustomer> newRightsOnItems = userRightsOnItems.get(i);
                if (newRightsOnItems != null) {
                    for (UserRightCustomer userRightObj : newRightsOnItems) {
                        genericDao.saveOrUpdate(userRightObj, newUser, em);
                    }
                }
            } else if (i == 2) {
                List<UserRightCustomer> updateRightsOnItems = userRightsOnItems.get(i);
                if (updateRightsOnItems != null) {
                    for (UserRightCustomer userRightObj : updateRightsOnItems) {
                        genericDao.saveOrUpdate(userRightObj, newUser, em);
                    }
                }
            }
        }
    }

    @Override
    public void saveUpdateTxnRule4Taxes(List<Long> items, String fromAmounts, String toAmounts, EntityManager em,
            Users newUser, Integer particular, Long usrRight, boolean isNew, int headType) throws IDOSException {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "Start " + fromAmounts + " " + toAmounts + " " + items + " head: " + headType);
        String sql = null;
        ArrayList inparams = new ArrayList<>(4);
        inparams.add(newUser.getOrganization().getId());
        inparams.add(newUser.getId());
        inparams.add(usrRight);
        inparams.add(particular);
        sql = ALL_TAX_HQL;
        List<UserRightTax> oldList = genericDao.queryWithParams(sql, em, inparams);
        List<UserRightTax> newList = new ArrayList<UserRightTax>();
        String fromAmountArray[] = fromAmounts.split(",");
        String toAmountArray[] = toAmounts.split(",");
        int count = 0;
        UserRights usrRights = UserRights.findById(usrRight);
        for (Long itemid : items) {
            BranchTaxes branchTax = BranchTaxes.findById(itemid);
            UserRightTax userRightObject = new UserRightTax();
            userRightObject.setUser(newUser);
            userRightObject.setUserRights(usrRights);
            userRightObject.setTax(branchTax);
            userRightObject.setOrganization(newUser.getOrganization());
            userRightObject.setTaxType(branchTax.getTaxType());
            userRightObject.setParticular(particular);
            if (IdosConstants.AUDITOR_RIGHTS != usrRight) {
                if (count < fromAmountArray.length && fromAmountArray.length > 0
                        && !fromAmountArray[count].trim().equals("")) {
                    userRightObject.setFromAmount(IdosUtil.convertStringToDouble(fromAmountArray[count]));
                } else {
                    userRightObject.setFromAmount(0.0);
                }
                if (count < toAmountArray.length && toAmountArray.length > 0
                        && !toAmountArray[count].trim().equals("")) {
                    userRightObject.setToAmount(IdosUtil.convertStringToDouble(toAmountArray[count]));
                } else {
                    userRightObject.setToAmount(0.0);
                }
            }
            newList.add(userRightObject);
            count = count + 1;
        }

        List<List<UserRightTax>> userRightsOnItems = ListUtility.getUserRightItems(oldList, newList);
        for (int i = 0; i < userRightsOnItems.size(); i++) {
            if (i == 0) {
                List<UserRightTax> oldRightsOnItems = userRightsOnItems.get(i);
                if (oldRightsOnItems != null) {
                    for (UserRightTax userRightObj : oldRightsOnItems) {
                        em.remove(userRightObj);
                    }
                }
            } else if (i == 1) {
                List<UserRightTax> newRightsOnItems = userRightsOnItems.get(i);
                if (newRightsOnItems != null) {
                    for (UserRightTax userRightObj : newRightsOnItems) {
                        genericDao.saveOrUpdate(userRightObj, newUser, em);
                    }
                }
            } else if (i == 2) {
                List<UserRightTax> updateRightsOnItems = userRightsOnItems.get(i);
                if (updateRightsOnItems != null) {
                    for (UserRightTax userRightObj : updateRightsOnItems) {
                        genericDao.saveOrUpdate(userRightObj, newUser, em);
                    }
                }
            }
        }
    }

    @Override
    public void saveUpdateTxnRule4InterBranch(List<Long> items, String fromAmounts, String toAmounts, EntityManager em,
            Users newUser, Integer particular, Long usrRight, boolean isNew, int headType) throws IDOSException {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "Start " + fromAmounts + " " + toAmounts + " " + items + " head: " + headType);
        String sql = null;
        ArrayList inparams = new ArrayList<>(4);
        inparams.add(newUser.getOrganization().getId());
        inparams.add(newUser.getId());
        inparams.add(usrRight);
        inparams.add(particular);
        sql = ALL_INTERBRANCH_HQL;

        List<UserRightInterBranch> oldList = genericDao.queryWithParams(sql, em, inparams);
        List<UserRightInterBranch> newList = new ArrayList<UserRightInterBranch>();
        String fromAmountArray[] = fromAmounts.split(",");
        String toAmountArray[] = toAmounts.split(",");
        int count = 0;
        UserRights usrRights = UserRights.findById(usrRight);
        for (Long itemid : items) {
            InterBranchMapping interBranchMapping = InterBranchMapping.findById(itemid);
            UserRightInterBranch userRightObject = new UserRightInterBranch();
            userRightObject.setUser(newUser);
            userRightObject.setUserRights(usrRights);
            userRightObject.setInterBranch(interBranchMapping);
            userRightObject.setOrganization(newUser.getOrganization());
            userRightObject.setParticular(particular);
            if (IdosConstants.AUDITOR_RIGHTS != usrRight) {
                if (count < fromAmountArray.length && fromAmountArray.length > 0
                        && !fromAmountArray[count].trim().equals("")) {
                    userRightObject.setFromAmount(IdosUtil.convertStringToDouble(fromAmountArray[count]));
                } else {
                    userRightObject.setFromAmount(0.0);
                }
                if (count < toAmountArray.length && toAmountArray.length > 0
                        && !toAmountArray[count].trim().equals("")) {
                    userRightObject.setToAmount(IdosUtil.convertStringToDouble(toAmountArray[count]));
                } else {
                    userRightObject.setToAmount(0.0);
                }
            }
            newList.add(userRightObject);
            count = count + 1;
        }

        List<List<UserRightInterBranch>> userRightsOnItems = ListUtility.getUserRightItems(oldList, newList);
        for (int i = 0; i < userRightsOnItems.size(); i++) {
            if (i == 0) {
                List<UserRightInterBranch> oldRightsOnItems = userRightsOnItems.get(i);
                if (oldRightsOnItems != null) {
                    for (UserRightInterBranch userRightObj : oldRightsOnItems) {
                        em.remove(userRightObj);
                    }
                }
            } else if (i == 1) {
                List<UserRightInterBranch> newRightsOnItems = userRightsOnItems.get(i);
                if (newRightsOnItems != null) {
                    for (UserRightInterBranch userRightObj : newRightsOnItems) {
                        genericDao.saveOrUpdate(userRightObj, newUser, em);
                    }
                }
            } else if (i == 2) {
                List<UserRightInterBranch> updateRightsOnItems = userRightsOnItems.get(i);
                if (updateRightsOnItems != null) {
                    for (UserRightInterBranch userRightObj : updateRightsOnItems) {
                        genericDao.saveOrUpdate(userRightObj, newUser, em);
                    }
                }
            }
        }
    }

    @Override
    public void saveUpdateTxnRule4User(List<Long> items, String fromAmounts, String toAmounts, EntityManager em,
            Users newUser, Integer particular, Long usrRight, boolean isNew, int headType) throws IDOSException {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "Start " + fromAmounts + " " + toAmounts + " " + items + " head: " + headType);
        String sql = null;
        ArrayList inparams = new ArrayList<>(4);
        inparams.add(newUser.getOrganization().getId());
        inparams.add(newUser.getId());
        inparams.add(usrRight);
        inparams.add(particular);
        sql = ALL_USER_HQL;

        List<UserRightUsers> oldList = genericDao.queryWithParams(sql, em, inparams);
        List<UserRightUsers> newList = new ArrayList<UserRightUsers>();
        String fromAmountArray[] = fromAmounts.split(",");
        String toAmountArray[] = toAmounts.split(",");
        int count = 0;
        UserRights usrRights = UserRights.findById(usrRight);
        for (Long itemid : items) {
            Users user = Users.findById(itemid);
            UserRightUsers userRightObject = new UserRightUsers();
            userRightObject.setUser(newUser);
            userRightObject.setUserRights(usrRights);
            userRightObject.setOnUser(user);
            userRightObject.setOrganization(newUser.getOrganization());
            userRightObject.setParticular(particular);
            if (IdosConstants.AUDITOR_RIGHTS != usrRight) {
                if (count < fromAmountArray.length && fromAmountArray.length > 0
                        && !fromAmountArray[count].trim().equals("")) {
                    userRightObject.setFromAmount(IdosUtil.convertStringToDouble(fromAmountArray[count]));
                } else {
                    userRightObject.setFromAmount(0.0);
                }
                if (count < toAmountArray.length && toAmountArray.length > 0
                        && !toAmountArray[count].trim().equals("")) {
                    userRightObject.setToAmount(IdosUtil.convertStringToDouble(toAmountArray[count]));
                } else {
                    userRightObject.setToAmount(0.0);
                }
            }
            newList.add(userRightObject);
            count = count + 1;
        }

        List<List<UserRightUsers>> userRightsOnItems = ListUtility.getUserRightItems(oldList, newList);
        for (int i = 0; i < userRightsOnItems.size(); i++) {
            if (i == 0) {
                List<UserRightUsers> oldRightsOnItems = userRightsOnItems.get(i);
                if (oldRightsOnItems != null) {
                    for (UserRightUsers userRightObj : oldRightsOnItems) {
                        em.remove(userRightObj);
                    }
                }
            } else if (i == 1) {
                List<UserRightUsers> newRightsOnItems = userRightsOnItems.get(i);
                if (newRightsOnItems != null) {
                    for (UserRightUsers userRightObj : newRightsOnItems) {
                        genericDao.saveOrUpdate(userRightObj, newUser, em);
                    }
                }
            } else if (i == 2) {
                List<UserRightUsers> updateRightsOnItems = userRightsOnItems.get(i);
                if (updateRightsOnItems != null) {
                    for (UserRightUsers userRightObj : updateRightsOnItems) {
                        genericDao.saveOrUpdate(userRightObj, newUser, em);
                    }
                }
            }
        }
    }

    @Override
    public boolean getAssetsCoaNodes(ArrayNode assetsCOAAn, EntityManager entityManager, Users user,
            Specifics specifics) {
        Map<String, Object> criterias = new HashMap<String, Object>();
        boolean isChildNodeAdded = false;
        if (specifics.getIdentificationForDataValid() != null
                && specifics.getIdentificationForDataValid().equals("3")) {
            criterias.clear();
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            List<BranchDepositBoxKey> bnchCashCountList = genericDao.findByCriteria(BranchDepositBoxKey.class,
                    criterias, entityManager);
            if (!bnchCashCountList.isEmpty()) {
                isChildNodeAdded = true;
                for (BranchDepositBoxKey bnchCashCount : bnchCashCountList) {
                    ObjectNode assetsCashRow = Json.newObject();
                    assetsCashRow.put("id", IdosConstants.HEAD_CASH + bnchCashCount.getId());
                    assetsCashRow.put("name", bnchCashCount.getBranch().getName() + " Cash");
                    assetsCashRow.put("headType", IdosConstants.HEAD_CASH);
                    assetsCOAAn.add(assetsCashRow);
                }
            }
        } else if ("30".equals(specifics.getIdentificationForDataValid())) {
            criterias.clear();
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            List<BranchDepositBoxKey> bnchCashCountList = genericDao.findByCriteria(BranchDepositBoxKey.class,
                    criterias, entityManager);
            if (!bnchCashCountList.isEmpty()) {
                isChildNodeAdded = true;
                for (BranchDepositBoxKey bnchCashCount : bnchCashCountList) {
                    ObjectNode assetsCashRow = Json.newObject();
                    assetsCashRow.put("id", IdosConstants.HEAD_PETTY + bnchCashCount.getId());
                    assetsCashRow.put("name", bnchCashCount.getBranch().getName() + " Petty Cash");
                    assetsCashRow.put("headType", IdosConstants.HEAD_PETTY);
                    assetsCOAAn.add(assetsCashRow);
                }
            }
        } else if (specifics.getIdentificationForDataValid() != null
                && specifics.getIdentificationForDataValid().equals("4")) {
            criterias.clear();
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            List<BranchBankAccounts> bnchBankAccounts = genericDao.findByCriteria(BranchBankAccounts.class, criterias,
                    entityManager);
            if (!bnchBankAccounts.isEmpty()) {
                isChildNodeAdded = true;
                for (BranchBankAccounts indBnchBankAccounts : bnchBankAccounts) {
                    ObjectNode assetsBankRow = Json.newObject();
                    assetsBankRow.put("id", IdosConstants.HEAD_BANK + indBnchBankAccounts.getId());
                    assetsBankRow.put("name",
                            indBnchBankAccounts.getBranch().getName() + " " + indBnchBankAccounts.getBankName());
                    assetsBankRow.put("headType", IdosConstants.HEAD_BANK);
                    assetsCOAAn.add(assetsBankRow);
                }
            }
        } else if ("1".equals(specifics.getIdentificationForDataValid())) {
            // <option value="1">Is this customer account / debtors</option> ==
            // Assets-Account Receivables , cust credit sales
            // all customers of the organization is also assets for the company
            ArrayList inparms = new ArrayList<>(3);
            inparms.add(user.getOrganization().getId());
            inparms.add(2);
            inparms.add(3);
            List<Vendor> customersList = genericDao.queryWithParams(IdosConstants.VEND_CUST_ORG_HQL, entityManager,
                    inparms);
            if (!customersList.isEmpty()) {
                isChildNodeAdded = true;
                for (Vendor cust : customersList) {
                    ObjectNode assetsCustRow = Json.newObject();
                    assetsCustRow.put("id", IdosConstants.HEAD_CUSTOMER + cust.getId());
                    assetsCustRow.put("name", cust.getName());
                    assetsCustRow.put("headType", IdosConstants.HEAD_CUSTOMER);
                    assetsCOAAn.add(assetsCustRow);
                }
            }
        } else if ("7".equals(specifics.getIdentificationForDataValid())) {
            // 7- Is this the account where you classify advance paid to vendors / creditors
            ArrayList inparms = new ArrayList<>(3);
            inparms.add(user.getOrganization().getId());
            inparms.add(1);
            inparms.add(4);
            List<Vendor> customersList = genericDao.queryWithParams(IdosConstants.VEND_CUST_ORG_HQL, entityManager,
                    inparms);
            if (!customersList.isEmpty()) {
                isChildNodeAdded = true;
                for (Vendor vendor : customersList) {
                    ObjectNode assetsCustRow = Json.newObject();
                    assetsCustRow.put("id", IdosConstants.HEAD_VENDOR_ADV + vendor.getId()); // In TB shown Under
                                                                                             // Assets: Vendor advance
                    assetsCustRow.put("name", vendor.getName() + "_Adv");
                    assetsCustRow.put("headType", IdosConstants.HEAD_VENDOR_ADV);
                    assetsCOAAn.add(assetsCustRow);
                }
            }
        } else if ("12".equals(specifics.getIdentificationForDataValid())) {
            // 12 - Is this the account where you classify travel advances paid
            StringBuilder newsbquery = new StringBuilder("select obj from Users obj where obj.organization.id=")
                    .append(user.getOrganization().getId());
            newsbquery.append(
                    " and id in (select distinct createdBy from ClaimTransaction claim where claim.transactionBranchOrganization.id = ")
                    .append(user.getOrganization().getId());
            newsbquery.append(" and claim.presentStatus=1 and claim.transactionStatus='Accounted'")
                    .append(" and claim.transactionPurpose.id = 15)");

            List<Users> travelAdvUserList = genericDao.executeSimpleQuery(newsbquery.toString(), entityManager);
            if (travelAdvUserList.size() > 0) {
                for (Users userTmp : travelAdvUserList) {
                    ObjectNode assetsUsersRow = Json.newObject();
                    assetsUsersRow.put("id", IdosConstants.HEAD_USER + userTmp.getId());
                    assetsUsersRow.put("name", userTmp.getFullName());
                    assetsUsersRow.put("headType", IdosConstants.HEAD_USER);
                    assetsCOAAn.add(assetsUsersRow);
                }
                isChildNodeAdded = true;
            }
        } else if ("13".equals(specifics.getIdentificationForDataValid())) {
            // 13">Is this the account where you classify advance paid to staff for expenses
            StringBuilder newsbquery = new StringBuilder("select obj from Users obj where obj.organization.id=")
                    .append(user.getOrganization().getId());
            newsbquery.append(
                    " and id in (select distinct createdBy from ClaimTransaction claim where claim.transactionBranchOrganization.id = ")
                    .append(user.getOrganization().getId());
            List<Users> travelAdvUserList = genericDao.executeSimpleQuery(newsbquery.toString(), entityManager);
            if (travelAdvUserList.size() > 0) {
                for (Users userTmp : travelAdvUserList) {
                    ObjectNode assetsUsersRow = Json.newObject();
                    assetsUsersRow.put("id", IdosConstants.HEAD_USER + userTmp.getId());
                    assetsUsersRow.put("name", userTmp.getFullName());
                    assetsUsersRow.put("headType", IdosConstants.HEAD_USER);
                    assetsCOAAn.add(assetsUsersRow);
                }
                isChildNodeAdded = true;
            }
        } else if ("57".equals(specifics.getIdentificationForDataValid())) {
            isChildNodeAdded = SPECIFICS_DAO.getInterBranchWithIdHead(entityManager, user, assetsCOAAn);
        } else if (specifics.getIdentificationForDataValid() != null) {
            int taxType = 0;
            String headType = IdosConstants.HEAD_TAXS;
            if (specifics.getIdentificationForDataValid().equals("8")) {
                headType = IdosConstants.HEAD_TDS_INPUT;
                isChildNodeAdded = true;
                ObjectNode assetsUsersRow = Json.newObject();
                assetsUsersRow.put("id", headType + specifics.getId());
                assetsUsersRow.put("name", specifics.getName());
                assetsUsersRow.put("headType", headType);
                assetsCOAAn.add(assetsUsersRow);
            } else if (specifics.getIdentificationForDataValid().equals("14")) {
                taxType = IdosConstants.INPUT_TAX;
                headType = IdosConstants.HEAD_TAXS;
            } else if (specifics.getIdentificationForDataValid().equals("39")) {
                taxType = IdosConstants.INPUT_SGST;
                headType = IdosConstants.HEAD_SGST;
            } else if (specifics.getIdentificationForDataValid().equals("40")) {
                taxType = IdosConstants.INPUT_CGST;
                headType = IdosConstants.HEAD_CGST;
            } else if (specifics.getIdentificationForDataValid().equals("41")) {
                taxType = IdosConstants.INPUT_IGST;
                headType = IdosConstants.HEAD_IGST;
            } else if (specifics.getIdentificationForDataValid().equals("42")) {
                taxType = IdosConstants.INPUT_CESS;
                headType = IdosConstants.HEAD_CESS;
            } else if (specifics.getIdentificationForDataValid().equals("53")) {
                taxType = IdosConstants.RCM_SGST_IN;
                headType = IdosConstants.HEAD_RCM_SGST_IN;
            } else if (specifics.getIdentificationForDataValid().equals("54")) {
                taxType = IdosConstants.RCM_CGST_IN;
                headType = IdosConstants.HEAD_RCM_CGST_IN;
            } else if (specifics.getIdentificationForDataValid().equals("55")) {
                taxType = IdosConstants.RCM_IGST_IN;
                headType = IdosConstants.HEAD_RCM_IGST_IN;
            } else if (specifics.getIdentificationForDataValid().equals("56")) {
                taxType = IdosConstants.RCM_CESS_IN;
                headType = IdosConstants.HEAD_RCM_CESS_IN;
            }
            if (taxType != 0) {
                criterias.clear();
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("taxType", taxType);
                criterias.put("presentStatus", 1);
                List<BranchTaxes> branchTaxesList = genericDao.findByCriteria(BranchTaxes.class, criterias,
                        entityManager);
                if (!branchTaxesList.isEmpty()) {
                    isChildNodeAdded = true;
                }
                for (BranchTaxes branchTaxes : branchTaxesList) {
                    ObjectNode assetsUsersRow = Json.newObject();
                    assetsUsersRow.put("id", headType + branchTaxes.getId());
                    assetsUsersRow.put("name", branchTaxes.getTaxName());
                    assetsUsersRow.put("headType", headType);
                    assetsCOAAn.add(assetsUsersRow);
                }
            }
        }
        return isChildNodeAdded;
    }

    @Override
    public boolean getLiabilitiesCoaNodes(ArrayNode liabilitiesCoaAn, EntityManager em, Users user,
            Specifics specifics) {
        Map<String, Object> criterias = new HashMap<String, Object>();
        boolean isChildNodeAdded = false;
        if (specifics.getIdentificationForDataValid() != null
                && specifics.getIdentificationForDataValid().equals("2")) {
            // <option value="2">Is this vendor account / creditors</option> == Liabilities
            // - Acct payables(purchase on credit from vendors)
            // all vendors of the organization is liabilities for the company
            ArrayList inparms = new ArrayList<>(3);
            inparms.add(user.getOrganization().getId());
            inparms.add(1);
            inparms.add(4);
            List<Vendor> vendorsList = genericDao.queryWithParams(IdosConstants.VEND_CUST_ORG_HQL, em, inparms);
            if (!vendorsList.isEmpty()) {
                isChildNodeAdded = true;
            }
            for (Vendor vend : vendorsList) {
                ObjectNode liabilitiesVendRow = Json.newObject();
                liabilitiesVendRow.put("id", IdosConstants.HEAD_VENDOR + vend.getId());// Shown under TB-liabilities -
                                                                                       // Accounts Payables
                liabilitiesVendRow.put("name", vend.getName());
                liabilitiesVendRow.put("headType", IdosConstants.HEAD_VENDOR);
                liabilitiesCoaAn.add(liabilitiesVendRow);
            }
        } else if (specifics.getIdentificationForDataValid() != null
                && specifics.getIdentificationForDataValid().equals("5")) {
            criterias.clear();
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            List<BranchBankAccounts> bnchBankAccounts = genericDao.findByCriteria(BranchBankAccounts.class, criterias,
                    em);
            if (!bnchBankAccounts.isEmpty()) {
                isChildNodeAdded = true;
            }
            for (BranchBankAccounts indBnchBankAccounts : bnchBankAccounts) {
                ObjectNode liabilitiesBankRow = Json.newObject();
                liabilitiesBankRow.put("id", IdosConstants.HEAD_BANK + indBnchBankAccounts.getId());
                liabilitiesBankRow.put("name",
                        indBnchBankAccounts.getBranch().getName() + " " + indBnchBankAccounts.getBankName());
                liabilitiesBankRow.put("headType", IdosConstants.HEAD_BANK);
                liabilitiesCoaAn.add(liabilitiesBankRow);
            }
        } else if (specifics.getIdentificationForDataValid() != null
                && specifics.getIdentificationForDataValid().equals("6")) {
            ArrayList inparms = new ArrayList<>(3);
            inparms.add(user.getOrganization().getId());
            inparms.add(2);
            inparms.add(3);
            List<Vendor> customersList = genericDao.queryWithParams(IdosConstants.VEND_CUST_ORG_HQL, em, inparms);
            if (!customersList.isEmpty()) {
                isChildNodeAdded = true;
            }
            for (Vendor cust : customersList) {
                ObjectNode liabilityCustRow = Json.newObject();
                liabilityCustRow.put("id", IdosConstants.HEAD_CUSTOMER_ADV + cust.getId()); // In TB:showne under
                                                                                            // Liabilities - Customer
                                                                                            // advance
                liabilityCustRow.put("name", cust.getName() + "_Adv");
                liabilityCustRow.put("headType", IdosConstants.HEAD_CUSTOMER_ADV);
                liabilitiesCoaAn.add(liabilityCustRow);
            }
        } else if (specifics.getIdentificationForDataValid() != null
                && !specifics.getIdentificationForDataValid().equalsIgnoreCase("")) {
            int taxType = 0;
            String headType = IdosConstants.HEAD_TAXS;
            if (specifics.getIdentificationForDataValid().equals("31")) {
                headType = IdosConstants.HEAD_TDS_192;
            } else if (specifics.getIdentificationForDataValid().equals("32")) {
                headType = IdosConstants.HEAD_TDS_194A;
            } else if (specifics.getIdentificationForDataValid().equals("33")) {
                headType = IdosConstants.HEAD_TDS_194C1;
            } else if (specifics.getIdentificationForDataValid().equals("34")) {
                headType = IdosConstants.HEAD_TDS_194C2;
            } else if (specifics.getIdentificationForDataValid().equals("35")) {
                headType = IdosConstants.HEAD_TDS_194H;
            } else if (specifics.getIdentificationForDataValid().equals("36")) {
                headType = IdosConstants.HEAD_TDS_194I1;
            } else if (specifics.getIdentificationForDataValid().equals("37")) {
                headType = IdosConstants.HEAD_TDS_194I2;
            } else if (specifics.getIdentificationForDataValid().equals("38")) {
                headType = IdosConstants.HEAD_TDS_194J;
            } else if (specifics.getIdentificationForDataValid().equals("15")) {
                taxType = IdosConstants.OUTPUT_TAX;
                headType = IdosConstants.HEAD_TAXS;
            } else if (specifics.getIdentificationForDataValid().equals("43")) {
                taxType = IdosConstants.OUTPUT_SGST;
                headType = IdosConstants.HEAD_SGST;
            } else if (specifics.getIdentificationForDataValid().equals("44")) {
                taxType = IdosConstants.OUTPUT_CGST;
                headType = IdosConstants.HEAD_CGST;
            } else if (specifics.getIdentificationForDataValid().equals("45")) {
                taxType = IdosConstants.OUTPUT_IGST;
                headType = IdosConstants.HEAD_IGST;
            } else if (specifics.getIdentificationForDataValid().equals("46")) {
                taxType = IdosConstants.OUTPUT_CESS;
                headType = IdosConstants.HEAD_CESS;
            } else if (specifics.getIdentificationForDataValid().equals("47")) {
                taxType = IdosConstants.RCM_SGST_OUTPUT;
                headType = IdosConstants.HEAD_RCM_SGST_OUTPUT;
            } else if (specifics.getIdentificationForDataValid().equals("48")) {
                taxType = IdosConstants.RCM_CGST_OUTPUT;
                headType = IdosConstants.HEAD_RCM_CGST_OUTPUT;
            } else if (specifics.getIdentificationForDataValid().equals("49")) {
                taxType = IdosConstants.RCM_IGST_OUTPUT;
                headType = IdosConstants.HEAD_RCM_IGST_OUTPUT;
            } else if (specifics.getIdentificationForDataValid().equals("50")) {
                taxType = IdosConstants.RCM_CESS_OUTPUT;
                headType = IdosConstants.HEAD_RCM_CESS_OUTPUT;
            }
            if (taxType != 0) {
                criterias.clear();
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("taxType", taxType);
                criterias.put("presentStatus", 1);
                List<BranchTaxes> branchTaxList = genericDao.findByCriteria(BranchTaxes.class, criterias, em);
                if (!branchTaxList.isEmpty()) {
                    isChildNodeAdded = true;
                }
                for (BranchTaxes branchTaxes : branchTaxList) {
                    ObjectNode liabilityTaxRow = Json.newObject();
                    liabilityTaxRow.put("id", headType + branchTaxes.getId());
                    liabilityTaxRow.put("name", branchTaxes.getTaxName());
                    liabilityTaxRow.put("headType", headType);
                    liabilitiesCoaAn.add(liabilityTaxRow);
                }
            } else {
                isChildNodeAdded = true;
                ObjectNode liabilityTaxRow = Json.newObject();
                liabilityTaxRow.put("id", headType + specifics.getId());
                liabilityTaxRow.put("name", specifics.getName());
                liabilityTaxRow.put("headType", headType);
                liabilitiesCoaAn.add(liabilityTaxRow);
            }
        }
        return isChildNodeAdded;
    }
}
