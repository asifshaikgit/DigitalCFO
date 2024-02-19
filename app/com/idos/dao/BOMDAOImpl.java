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
import javax.persistence.EntityTransaction;
import java.util.*;

/**
 * @author Harish Kumar created on 25.04.2023
 */
public class BOMDAOImpl implements BOMDAO {
    @Override
    public Map<String, String> add(JsonNode json , Users user, EntityManager em) throws IDOSException {
        Map<String, String> bomDetailHash = null;
        try {
            bomDetailHash = save(json , user, em, 0L);
        }catch (Exception ex){
            if(em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            log.log(Level.SEVERE, user.getEmail(), ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION, "Error on save Bill of Material.", ex.getMessage());
        }
        return bomDetailHash;
    }

    @Override
    public Map<String, String> update(JsonNode json , Users user, EntityManager em, long bomId) throws IDOSException {
        Map<String, String> bomDetailHash = null;
        try {
            bomDetailHash = save(json , user, em, bomId);
        }catch (Exception ex){
            if(em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            log.log(Level.SEVERE, user.getEmail(), ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION, "Error on update Bill of Material.", ex.getMessage());
        }
        return bomDetailHash;
    }

    private  Map<String, String> save(JsonNode json , Users user, EntityManager em, long bomId) throws org.json.JSONException, IDOSException{
        Map<String, String> bomDetailHash = null;
        log.log(Level.FINE, " start " + json);
        String bomName = json.findValue("bomName").asText();
        String bomRow = json.findValue("dataArray").toString();
        JSONArray dataArray = new JSONArray(bomRow);
        
        BOMModel bom = null;
        if(bomId > 0L) {
            bom = BOMModel.findById(bomId);
        }else{
            bom = new BOMModel();
            bom.setOrganization(user.getOrganization());
        }
        bom.setBomName(bomName);
        genericDao.saveOrUpdate(bom, user, em);
        bomDetailHash = new HashMap<>(3);
        bomDetailHash.put("entityId", bom.getId().toString());
        List  <Long>newList  = new ArrayList<Long>(4);
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject rowData = new JSONObject(dataArray.get(i).toString());
            long bomExpItem = (rowData.isNull("bomItem") || rowData.getString("bomItem").isEmpty()) ? 0L : rowData.getLong("bomItem");
            Double bomNoOfUnit = rowData.getDouble("bomNoOfUnit");
            Specifics expense = Specifics.findById(bomExpItem);
            BOMItemModel bomItem = null;
            long bomDetailId = (rowData.isNull("bomDetailId") || rowData.getString("bomDetailId").isEmpty()) ? 0L : rowData.getLong("bomDetailId");
            if(bomDetailId > 0L) {
                bomItem = BOMItemModel.findById(bomDetailId);
            }else{
                bomItem = new BOMItemModel();
                bomItem.setBOM(bom);
            }
            bomItem.setExpense(expense);
            bomItem.setNoOfUnits(bomNoOfUnit);
            genericDao.saveOrUpdate(bomItem, user, em);
            newList.add(bomItem.getId());
            
        }
        if(bom != null && bom.getBomItems() != null && bom.getBomItems().size()  > dataArray.length()){
            for(BOMItemModel bomItem : bom.getBomItems()){
                if(!newList.contains(bomItem.getId())){
                        genericDao.deleteById(BOMItemModel.class, bomItem.getId(), em);
                }
            }
        }
        return bomDetailHash;
    }

    @Override
    public List<BOMModel> getByOrg(Users user, EntityManager em) throws IDOSException {
        List<BOMModel> bomModels = null;
        ArrayList inparams = new ArrayList(2);
        inparams.add(user.getOrganization().getId());
        //inparams.add(1);
        bomModels = genericDao.queryWithParamsName(BOM_BY_ORG_JPQL, em, inparams);
        return bomModels;
    }

    

}
