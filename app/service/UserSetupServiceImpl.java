package service;

import com.idos.cache.UserTxnRuleSetupCache;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import model.Specifics;
import model.Users;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @auther Sunil Namdev created on 31.07.2018
 */
public class UserSetupServiceImpl implements UserSetupService {

    @Override
    public ObjectNode getAssetsCoaChildNodesWithAllHeads(EntityManager entityManager, Users user) {
        log.log(Level.FINE, "===Start ");
        ObjectNode result = Json.newObject();
        ArrayNode assetsCOAAn = result.putArray("coaItemData");
        List<Specifics> assetsSpecfList = chartsOfAccountsDAO.getCOAChildNodesList(entityManager, user, 3);
        for (Specifics specfics : assetsSpecfList) {
            boolean isChildNodeAdded = USER_SETUP_DAO.getAssetsCoaNodes(assetsCOAAn, entityManager, user, specfics);
            if (!isChildNodeAdded && (specfics.getIdentificationForDataValid() == null
                    || "".equals(specfics.getIdentificationForDataValid()))) {
                ObjectNode assetsRow = Json.newObject();
                assetsRow.put("id", IdosConstants.HEAD_SPECIFIC + specfics.getId());
                assetsRow.put("name", specfics.getName());
                assetsRow.put("headType", IdosConstants.HEAD_SPECIFIC);
                assetsCOAAn.add(assetsRow);
            }
        }
        log.log(Level.FINE, "===End ");
        return result;
    }

    public String getAllCoaItemsList(StringBuilder fromAmount, StringBuilder toAmount, EntityManager em, Users user,
            int particular) throws IDOSException {
        log.log(Level.FINE, "==========Start " + particular);
        String itemIdsList = null;
        String fromAmountTmp = fromAmount.toString();
        if (fromAmountTmp.length() > 0)
            fromAmountTmp = fromAmountTmp.substring(0, fromAmountTmp.length() - 1);
        String toAmountTmp = toAmount.toString();
        if (toAmountTmp.length() > 0)
            toAmountTmp = toAmountTmp.substring(0, toAmountTmp.length() - 1);
        fromAmount.delete(0, fromAmount.length());
        toAmount.delete(0, toAmount.length());
        List<Specifics> itemsList = null;
        ArrayNode coaItemData = null;
        if (particular == UserTxnRuleSetupCache.INCOME_HEAD) {
            itemsList = chartsOfAccountsDAO.getIncomesCoaChildNodes(em, user);
        } else if (particular == UserTxnRuleSetupCache.EXPENSE_HEAD) {
            itemsList = chartsOfAccountsDAO.getExpensesCoaChildNodes(em, user);
        } else if (particular == UserTxnRuleSetupCache.ASSETS_HEAD) {
            ObjectNode result = getAssetsCoaChildNodesWithAllHeads(em, user);
            coaItemData = (ArrayNode) result.get("coaItemData");
        } else if (particular == UserTxnRuleSetupCache.LIABILITES_HEAD) {
            ObjectNode result = chartsOfAccountsDAO.getLiabilitiesCoaLeafNodesWithAllHeads(em, user);
            coaItemData = (ArrayNode) result.get("coaItemData");
        }
        StringBuilder items = new StringBuilder();
        if (particular == UserTxnRuleSetupCache.INCOME_HEAD || particular == UserTxnRuleSetupCache.EXPENSE_HEAD) {
            for (Specifics specf : itemsList) {
                if (specf.getIdentificationForDataValid() != null
                        && !"".equals(specf.getIdentificationForDataValid())) {
                    int mappid = IdosUtil.convertStringToInt(specf.getIdentificationForDataValid());
                    if (mappid != 8 && mappid != 51 && mappid != 64 && mappid != 23 && mappid != 62 && mappid != 63) {
                        continue;
                    }
                }
                items.append(IdosConstants.HEAD_SPECIFIC).append(specf.getId()).append(",");
                fromAmount.append(fromAmountTmp).append(",");
                toAmount.append(toAmountTmp).append(",");
            }
        } else if (particular == UserTxnRuleSetupCache.ASSETS_HEAD
                || particular == UserTxnRuleSetupCache.LIABILITES_HEAD) {
            for (int i = 0; i < coaItemData.size(); i++) {
                ObjectNode row = (ObjectNode) coaItemData.get(i);
                items.append(row.findValue("id").asText()).append(",");
                fromAmount.append(fromAmountTmp).append(",");
                toAmount.append(toAmountTmp).append(",");
            }
        }
        if (fromAmount.length() > 1) {
            fromAmount = fromAmount.replace(fromAmount.length() - 1, fromAmount.length(), "");
        }
        if (toAmount.length() > 1) {
            toAmount = toAmount.replace(toAmount.length() - 1, toAmount.length(), "");
        }
        if (items.length() > 1) {
            items = items.replace(items.length() - 1, items.length(), "");
            itemIdsList = items.toString();
        }
        log.log(Level.FINE, "==========End " + itemIdsList);
        return itemIdsList;
    }

    public void saveUpdateTransactionRule(String items, String fromAmount, String toAmount, EntityManager em,
            Users newUser, Integer particular, Long userRight, boolean isNew) throws IDOSException {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "Start ");
        log.log(Level.FINE, " items: " + items);
        StringTokenizer strtkn = new StringTokenizer(items, ",");
        List<Long> specificList = new ArrayList<Long>();
        List<Long> cashList = new ArrayList<Long>();
        List<Long> pettycashList = new ArrayList<Long>();
        List<Long> bankList = new ArrayList<Long>();
        List<Long> vendorList = new ArrayList<Long>();
        List<Long> vendorAdvList = new ArrayList<Long>();
        List<Long> customerList = new ArrayList<Long>();
        List<Long> customerAdvList = new ArrayList<Long>();
        List<Long> taxList = new ArrayList<Long>();
        List<Long> interbranchList = new ArrayList<Long>();
        List<Long> inputTdsList = new ArrayList<Long>();
        List<Long> outputTdsList = new ArrayList<Long>();
        List<Long> usersList = new ArrayList<Long>();
        while (strtkn.hasMoreElements()) {
            String item = (String) strtkn.nextElement();
            String headType = item.substring(0, 4);
            String headid = item.substring(4);
            if (headid != null && !"".equals(headid)) {
                switch (headType) {
                    case IdosConstants.HEAD_SPECIFIC:
                        specificList.add(IdosUtil.convertStringToLong(headid));
                        break;
                    case IdosConstants.HEAD_CASH:
                        cashList.add(IdosUtil.convertStringToLong(headid));
                        break;
                    case IdosConstants.HEAD_PETTY:
                        pettycashList.add(IdosUtil.convertStringToLong(headid));
                        break;
                    case IdosConstants.HEAD_BANK:
                        bankList.add(IdosUtil.convertStringToLong(headid));
                        break;
                    case IdosConstants.HEAD_CUSTOMER:
                        customerList.add(IdosUtil.convertStringToLong(headid));
                        break;
                    case IdosConstants.HEAD_CUSTOMER_ADV:
                        customerAdvList.add(IdosUtil.convertStringToLong(headid));
                        break;
                    case IdosConstants.HEAD_VENDOR:
                        vendorList.add(IdosUtil.convertStringToLong(headid));
                        break;
                    case IdosConstants.HEAD_VENDOR_ADV:
                        vendorAdvList.add(IdosUtil.convertStringToLong(headid));
                        break;
                    case IdosConstants.HEAD_TAXS:
                    case IdosConstants.HEAD_SGST:
                    case IdosConstants.HEAD_CGST:
                    case IdosConstants.HEAD_IGST:
                    case IdosConstants.HEAD_CESS:
                    case IdosConstants.HEAD_RCM_SGST_IN:
                    case IdosConstants.HEAD_RCM_CGST_IN:
                    case IdosConstants.HEAD_RCM_IGST_IN:
                    case IdosConstants.HEAD_RCM_CESS_IN:
                    case IdosConstants.HEAD_RCM_SGST_OUTPUT:
                    case IdosConstants.HEAD_RCM_CGST_OUTPUT:
                    case IdosConstants.HEAD_RCM_IGST_OUTPUT:
                    case IdosConstants.HEAD_RCM_CESS_OUTPUT:
                        taxList.add(IdosUtil.convertStringToLong(headid));
                        break;
                    case IdosConstants.HEAD_INTR_BRANCH:
                        interbranchList.add(IdosUtil.convertStringToLong(headid));
                        break;
                    case IdosConstants.HEAD_TDS_INPUT:
                        inputTdsList.add(IdosUtil.convertStringToLong(headid));
                        break;
                    case IdosConstants.HEAD_TDS_192:
                    case IdosConstants.HEAD_TDS_194A:
                    case IdosConstants.HEAD_TDS_194C1:
                    case IdosConstants.HEAD_TDS_194C2:
                    case IdosConstants.HEAD_TDS_194H:
                    case IdosConstants.HEAD_TDS_194I1:
                    case IdosConstants.HEAD_TDS_194I2:
                    case IdosConstants.HEAD_TDS_194J:
                        outputTdsList.add(IdosUtil.convertStringToLong(headid));
                        break;
                    case IdosConstants.HEAD_USER:
                        usersList.add(IdosUtil.convertStringToLong(headid));
                        break;
                }
            }
        }
        if (!specificList.isEmpty()) {
            USER_SETUP_DAO.saveUpdateTxnRule4Specific(specificList, fromAmount, toAmount, em, newUser, particular,
                    userRight, isNew);
        }
        if (!cashList.isEmpty()) {
            USER_SETUP_DAO.saveUpdateTxnRule4Cash(cashList, fromAmount, toAmount, em, newUser, particular, userRight,
                    isNew, 1);
        }
        if (!pettycashList.isEmpty()) {
            USER_SETUP_DAO.saveUpdateTxnRule4Cash(pettycashList, fromAmount, toAmount, em, newUser, particular,
                    userRight, isNew, 2);
        }
        if (!bankList.isEmpty()) {
            USER_SETUP_DAO.saveUpdateTxnRule4Bank(bankList, fromAmount, toAmount, em, newUser, particular, userRight,
                    isNew, 0);
        }
        if (!vendorList.isEmpty()) {
            USER_SETUP_DAO.saveUpdateTxnRule4Customer(vendorList, fromAmount, toAmount, em, newUser, particular,
                    userRight, isNew, IdosConstants.VENDOR);
        }
        if (!customerList.isEmpty()) {
            USER_SETUP_DAO.saveUpdateTxnRule4Customer(customerList, fromAmount, toAmount, em, newUser, particular,
                    userRight, isNew, IdosConstants.CUSTOMER);
        }
        if (!vendorAdvList.isEmpty()) {
            USER_SETUP_DAO.saveUpdateTxnRule4Customer(vendorAdvList, fromAmount, toAmount, em, newUser, particular,
                    userRight, isNew, IdosConstants.VENDOR_ADVANCE);
        }
        if (!customerAdvList.isEmpty()) {
            USER_SETUP_DAO.saveUpdateTxnRule4Customer(customerAdvList, fromAmount, toAmount, em, newUser, particular,
                    userRight, isNew, IdosConstants.CUSTOMER_ADVANCE);
        }
        if (!taxList.isEmpty()) {
            USER_SETUP_DAO.saveUpdateTxnRule4Taxes(taxList, fromAmount, toAmount, em, newUser, particular, userRight,
                    isNew, 0);
        }
        if (!interbranchList.isEmpty()) {
            USER_SETUP_DAO.saveUpdateTxnRule4InterBranch(interbranchList, fromAmount, toAmount, em, newUser, particular,
                    userRight, isNew, 0);
        }
        if (!inputTdsList.isEmpty()) {
            USER_SETUP_DAO.saveUpdateTxnRule4Specific(inputTdsList, fromAmount, toAmount, em, newUser, particular,
                    userRight, isNew);
        }
        if (!outputTdsList.isEmpty()) {
            USER_SETUP_DAO.saveUpdateTxnRule4Specific(outputTdsList, fromAmount, toAmount, em, newUser, particular,
                    userRight, isNew);
        }
        if (!usersList.isEmpty()) {
            USER_SETUP_DAO.saveUpdateTxnRule4User(usersList, fromAmount, toAmount, em, newUser, particular, userRight,
                    isNew, 0);
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "End ");
    }
}
