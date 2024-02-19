package service;

import com.idos.util.IDOSException;
import model.BOMModel;
import model.BOMItemModel;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;

/**
 * @author Harish Kumar created on 25.04.2023
 */
public interface BOMService extends BaseService{
    Map<String, String> add(JsonNode json , Users user, EntityManager em) throws IDOSException;
    Map<String, String> update(JsonNode json , Users user, EntityManager em, long bomId) throws IDOSException;
    List<BOMModel> getByOrg(Users user, EntityManager em) throws IDOSException;
    
}
