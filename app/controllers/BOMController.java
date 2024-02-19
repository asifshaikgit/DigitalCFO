package controllers;


import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;

import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import service.EntityManagerProvider;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import play.Application;
import play.mvc.Http;
import play.mvc.Http.Request;
import javax.inject.Inject;

/**
 * @author Harish Kumar created on 25.04.2023
 */
public class BOMController extends StaticController {
    private static EntityManager em;

    @Inject
    public BOMController(Application application) {
        super(application);
        em = EntityManagerProvider.getEntityManager();
    }

    @Transactional
    public Result save(Request request){
        Users user = getUserInfo(request);
        if(user == null){
            log.log(Level.SEVERE, "unauthorized access");
            return unauthorized();
        }
        //EntityManager em = getEntityManager();
        ObjectNode result = Json.newObject();
        EntityTransaction entityTransaction = em.getTransaction();
        try{
            JsonNode json = request.body().asJson();
            Map<String, String> bomDetailMap = null;
            long bomId = json.findValue("bomId") == null ? 0L : json.findValue("bomId").asLong();
            entityTransaction.begin();
            if(bomId <= 0L) {
                bomDetailMap = BOM_SERVICE.add(json, user, em);
                result.put("status", "added");
            }else{
                bomDetailMap = BOM_SERVICE.update(json, user, em, bomId);
                result.put("status", "updated");
            }
            entityTransaction.commit();
            if(bomDetailMap != null) {
                ObjectMapper mapper = new ObjectMapper();
                final JsonNode jsonRes = mapper.convertValue(bomDetailMap, JsonNode.class);
                result.put("bomlist", jsonRes);
            }else{
                result.put("status", "failed");
            }
        }catch (Exception ex){
            if(entityTransaction.isActive()){
                entityTransaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            String strBuff=getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(), Thread.currentThread().getStackTrace()[1].getMethodName());
            result.put("status", "failed");
        }
        return ok(result);
    }



    @Transactional
    public Result getByOrg(Request request){
        Users user = getUserInfo(request);
        if(user == null){
            log.log(Level.SEVERE, "unauthorized access");
            return unauthorized();
        }
        ObjectNode result = Json.newObject();
        try {
            //EntityManager em = getEntityManager();
            List<BOMModel> billOfMaterialModelList = BOM_SERVICE.getByOrg(user, em);
            ArrayNode bomlist = result.putArray("bomlist");
            for(BOMModel billOfMaterial: billOfMaterialModelList){
                ObjectNode row = Json.newObject();
                row.put("entityId", billOfMaterial.getId());
                row.put("status", billOfMaterial.getPresentStatus());
                row.put("bomName", billOfMaterial.getBomName());
                bomlist.add(row);
            }
            result.put("status", "success");
        }catch (Exception ex){
            result.put("status", "failed");
            log.log(Level.SEVERE, "Error", ex);
            String strBuff=getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(), Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        return ok(result);
    }

    @Transactional
    public Result getDetail(Request request, final long entityid){
        Users user = getUserInfo(request);
        if(user == null){
            log.log(Level.SEVERE, "unauthorized access");
            return unauthorized();
        }
        ObjectNode result = Json.newObject();
        try {
            //EntityManager em = getEntityManager();
            BOMModel  billOfMaterial  = BOMModel.findById(entityid);
            
            result.put("bomId", billOfMaterial.getId());
            result.put("bomName", billOfMaterial.getBomName());
            ArrayNode bomlist = result.putArray("bomlist");
            for(BOMItemModel billOfMaterialItem: billOfMaterial.getBomItems()){
                ObjectNode row = Json.newObject();
                row.put("entityId", billOfMaterialItem.getId());
                row.put("expenseId", billOfMaterialItem.getExpense().getId());
                if(billOfMaterialItem.getNoOfUnits() != null) {
                    row.put("noOfUnit", billOfMaterialItem.getNoOfUnits());
                }else{
                    row.put("noOfUnit", "");
                }
                bomlist.add(row);
            }
            result.put("status", "success");
        }catch (Exception ex){
            result.put("status", "failed");
            log.log(Level.SEVERE, "Error", ex);
            String strBuff=getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(), Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        return ok(result);
    }

    

    
}

