package com.idos.dao;

import actor.CreatorActor;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import controllers.Karvy.KarvyAuthorization;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import com.typesafe.config.Config;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.WebSocket;
import service.BranchBankService;
import service.BranchBankServiceImpl;
import service.BranchCashService;
import service.BranchCashServiceImpl;
import views.html.creditNote;
import views.html.debitNote;
import views.html.errorPage;
import java.util.logging.Level;
import javax.mail.Session;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.*;
import javax.inject.Inject;
import play.db.jpa.JPAApi;

/**
 * @author Sunil K. Namdev created on 29.01.2019
 */
public class BillOfMaterialDAOImpl implements BillOfMaterialDAO {
    private static JPAApi jpaApi;

    @Override
    public Map<String, String> add(JsonNode json, Users user, EntityManager em) throws IDOSException {
        Map<String, String> bomDetailHash = null;
        try {
            bomDetailHash = save(json, user, em, 0L);
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            log.log(Level.SEVERE, user.getEmail(), ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on save Bill of Material.", ex.getMessage());
        }
        return bomDetailHash;
    }

    @Override
    public Map<String, String> update(JsonNode json, Users user, EntityManager em, long bomId) throws IDOSException {
        Map<String, String> bomDetailHash = null;
        try {
            bomDetailHash = save(json, user, em, bomId);
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            log.log(Level.SEVERE, user.getEmail(), ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on update Bill of Material.", ex.getMessage());
        }
        return bomDetailHash;
    }

    private Map<String, String> save(JsonNode json, Users user, EntityManager em, long bomId)
            throws org.json.JSONException, IDOSException {
        Map<String, String> bomDetailHash = null;
        log.log(Level.FINE, " start " + json);
        long branchId = json.findValue("branchId") == null ? 0L : json.findValue("branchId").asLong();
        long projectId = json.findValue("projectId") == null ? 0L : json.findValue("projectId").asLong();
        long incomeItemId = json.findValue("incomeItemId") == null ? 0L : json.findValue("incomeItemId").asLong();
        String bomRow = json.findValue("dataArray").toString();
        JSONArray dataArray = new JSONArray(bomRow);
        Branch branch = Branch.findById(branchId);
        if (branch == null) {
            throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                    IdosConstants.INVALID_DATA_ERRCODE, "Branch is not found");
        }
        Project project = Project.findById(projectId);
        Specifics specifics = Specifics.findById(incomeItemId);
        BillOfMaterialModel billOfMaterial = null;
        if (bomId > 0L) {
            billOfMaterial = BillOfMaterialModel.findById(bomId);
        } else {
            billOfMaterial = new BillOfMaterialModel();
            billOfMaterial.setOrganization(user.getOrganization());
        }
        billOfMaterial.setBranch(branch);
        billOfMaterial.setProject(project);
        billOfMaterial.setIncome(specifics);
        genericDao.saveOrUpdate(billOfMaterial, user, em);
        bomDetailHash = new HashMap<>(3);
        bomDetailHash.put("entityId", billOfMaterial.getId().toString());
        bomDetailHash.put("branch", branch.getName());
        bomDetailHash.put("income", specifics.getName());
        if (project != null) {
            bomDetailHash.put("project", project.getName());
        } else {
            bomDetailHash.put("project", "");
        }
        List<Long> newList = new ArrayList<Long>(4);
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject rowData = new JSONObject(dataArray.get(i).toString());
            long bomItem = (rowData.isNull("bomItem") || rowData.getString("bomItem").isEmpty()) ? 0L
                    : rowData.getLong("bomItem");
            String bomUnitOfMeasure = rowData.isNull("bomUnitOfMeasure") ? null : rowData.getString("bomUnitOfMeasure");
            Double bomNoOfUnit = rowData.getDouble("bomNoOfUnit");
            long bomVendor = rowData.getLong("bomVendor");
            String bomOem = rowData.isNull("bomOem") ? null : rowData.getString("bomOem");
            String bomTom = rowData.isNull("bomTom") ? null : rowData.getString("bomTom");
            String bomKnowledgeFollowed = rowData.isNull("bomKnowledgeFollowed") ? null
                    : rowData.getString("bomKnowledgeFollowed");
            Specifics expense = Specifics.findById(bomItem);
            Vendor vendor = Vendor.findById(bomVendor);
            BillOfMaterialItemModel billOfMaterialItem = null;
            long bomDetailId = (rowData.isNull("bomDetailId") || rowData.getString("bomDetailId").isEmpty()) ? 0L
                    : rowData.getLong("bomDetailId");
            if (bomDetailId > 0L) {
                billOfMaterialItem = BillOfMaterialItemModel.findById(bomDetailId);
            } else {
                billOfMaterialItem = new BillOfMaterialItemModel();
                billOfMaterialItem.setOrganization(user.getOrganization());
                billOfMaterialItem.setBillOfMaterial(billOfMaterial);
            }
            billOfMaterialItem.setBranch(branch);
            billOfMaterialItem.setVendor(vendor);
            billOfMaterialItem.setExpense(expense);
            billOfMaterialItem.setNoOfUnits(bomNoOfUnit);
            billOfMaterialItem.setMeasureName(bomUnitOfMeasure);
            billOfMaterialItem.setOem(bomOem);
            billOfMaterialItem.setTypeOfMaterial(bomTom);
            billOfMaterialItem.setKnowledgeLib(bomKnowledgeFollowed);
            genericDao.saveOrUpdate(billOfMaterialItem, user, em);
            newList.add(billOfMaterialItem.getId());
        }
        if (billOfMaterial != null && billOfMaterial.getBillOfMaterialItems() != null
                && billOfMaterial.getBillOfMaterialItems().size() > dataArray.length()) {
            for (BillOfMaterialItemModel bomItem : billOfMaterial.getBillOfMaterialItems()) {
                if (!newList.contains(bomItem.getId())) {
                    genericDao.deleteById(BillOfMaterialItemModel.class, bomItem.getId(), em);
                }
            }
        }
        return bomDetailHash;
    }

    @Override
    public List<BillOfMaterialModel> getByOrg(Users user, EntityManager em) throws IDOSException {
        List<BillOfMaterialModel> billOfMaterialModels = null;
        ArrayList inparams = new ArrayList(2);
        inparams.add(user.getOrganization().getId());
        inparams.add(1);
        billOfMaterialModels = genericDao.queryWithParamsName(BOM_BY_ORG_JPQL, em, inparams);
        return billOfMaterialModels;
    }

    @Override
    public List<BillOfMaterialItemModel> getByIncomeAndBranch(Users user, EntityManager em, long branchId,
            long incomeId) throws IDOSException {
        List<BillOfMaterialItemModel> billOfMaterialItemModels = null;
        String JPQL = "from BillOfMaterialItemModel t1 where  t1.organization.id = ?1 and t1.branch.id= ?2 and t1.billOfMaterial.id in (select obj.id from BillOfMaterialModel obj where obj.organization.id = ?3 and obj.branch.id= ?4 and obj.income.id= ?5 and obj.presentStatus = ?6) and t1.presentStatus = ?7";
        ArrayList inparams = new ArrayList(7);
        inparams.add(user.getOrganization().getId());
        inparams.add(branchId);
        inparams.add(user.getOrganization().getId());
        inparams.add(branchId);
        inparams.add(incomeId);
        inparams.add(1);
        inparams.add(1);
        billOfMaterialItemModels = genericDao.queryWithParamsName(JPQL, em, inparams);
        return billOfMaterialItemModels;
    }

}
