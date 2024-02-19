package com.idos.dao;

import com.idos.util.IdosConstants;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @auther Sunil Namdev created on 05.06.2018
 */
public class PLBSCoaMappingDAOImpl implements PLBSCoaMappingDAO {

    @Override
    public ObjectNode getCoaForOrganizationWithAllHeads(ObjectNode result, JsonNode json, Users user,
            EntityManager em) {
        ArrayNode incomeCOAAn = result.putArray("incomeCOAData");
        ArrayNode expenseCOAAn = result.putArray("expenseCOAData");
        ArrayNode assetsCOAAn = result.putArray("assetsCOAData");
        ArrayNode liabilitiesCOAAn = result.putArray("liabilitiesCOAData");

        result.put("incomeresult", false);
        result.put("expenseresult", false);
        result.put("assetsresult", false);
        result.put("liabilitiesresult", false);

        List<Specifics> incomeSpecfList = coaDAO.getCOAChildNodesList(em, user, IdosConstants.INCOME);
        if (!incomeSpecfList.isEmpty()) {
            result.put("incomeresult", true);
            for (Specifics specf : incomeSpecfList) {
                ObjectNode incomesRow = Json.newObject();
                incomesRow.put("id", IdosConstants.HEAD_SPECIFIC + specf.getId());
                incomesRow.put("name", specf.getName());
                incomeCOAAn.add(incomesRow);
            }
        }

        List<Specifics> expenseSpecfList = coaDAO.getCOAChildNodesList(em, user, IdosConstants.EXPENSE);
        if (!expenseSpecfList.isEmpty()) {
            result.put("expenseresult", true);
            for (Specifics specf : expenseSpecfList) {
                boolean isChildNodeAdded = coaDAO.getPayrollItem4CoaNode(expenseCOAAn, 0, em, user, specf);
                if (!isChildNodeAdded) {
                    ObjectNode expenseRow = Json.newObject();
                    expenseRow.put("id", IdosConstants.HEAD_SPECIFIC + specf.getId());
                    expenseRow.put("name", specf.getName());
                    expenseCOAAn.add(expenseRow);
                }
            }
        }
        List<Specifics> assetsSpecfList = coaDAO.getCOAChildNodesList(em, user, IdosConstants.ASSETS);
        if (!assetsSpecfList.isEmpty()) {
            result.put("assetsresult", true);
            for (Specifics specfics : assetsSpecfList) {
                boolean isChildNodeAdded = coaDAO.getAssetsCoaNodes(assetsCOAAn, 0, em, user, specfics);
                if (!isChildNodeAdded) {
                    ObjectNode assetsRow = Json.newObject();
                    assetsRow.put("id", IdosConstants.HEAD_SPECIFIC + specfics.getId());
                    assetsRow.put("name", specfics.getName());
                    assetsCOAAn.add(assetsRow);
                }
            }
        }

        List<Specifics> liabilitiesSpecfList = coaDAO.getCOAChildNodesList(em, user, IdosConstants.LIABILITIES);
        if (!liabilitiesSpecfList.isEmpty()) {
            result.put("liabilitiesresult", true);
            for (Specifics specfics : liabilitiesSpecfList) {
                boolean isChildNodeAdded = coaDAO.getLiabilitiesCoaNodes(liabilitiesCOAAn, 0, em, user, specfics);
                if (!isChildNodeAdded) {
                    ObjectNode liabilitiesRow = Json.newObject();
                    liabilitiesRow.put("id", IdosConstants.HEAD_SPECIFIC + specfics.getId());
                    liabilitiesRow.put("name", specfics.getName());
                    liabilitiesCOAAn.add(liabilitiesRow);
                }
            }
        }
        log.log(Level.FINE, "*********End " + result);
        return result;
    }
}
