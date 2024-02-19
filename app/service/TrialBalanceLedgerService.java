package service;

import com.idos.util.IDOSException;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.io.ByteArrayOutputStream;
import play.Application;

/**
 * Created by Sunil Namdev on 17-10-2016.
 */
public interface TrialBalanceLedgerService extends BaseService {
    ByteArrayOutputStream exportTrialBalanceLedger(Users user, EntityManager entityManager, JsonNode json,
            Application application)
            throws IDOSException;

    ObjectNode getTransactionForHead(Users user, EntityManager entityManager, JsonNode json) throws IDOSException;
}
