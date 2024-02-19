package service;

import com.idos.dao.AuditDAO;
import com.idos.dao.GenericDAO;
import com.idos.util.IDOSException;
import model.Branch;
import model.Organization;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.mail.Session;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.Date;

/**
 * Created by Sunil Namdev on 14-08-2016.
 */
public interface BranchCashService extends BaseService {
    public boolean saveBranchCash(JsonNode json, Branch newBranch, Users usr, Organization org,
            String branchrentRevisionDueOn, String premiseType, EntityManager entityManager, GenericDAO genericDAO,
            AuditDAO auditDAO, Session emailsession, String ipAddress) throws Exception;

    public Double updateBranchCashDetail(EntityManager entityManager, Users user, Branch txnBranch, Double txnNetAmount,
            boolean isCredit, Date txnDate, ObjectNode result) throws IDOSException;

    public Double updateBranchPettyCashDetail(EntityManager entityManager, GenericDAO genericDAO, Users user,
            Branch txnBranch, Double txnNetAmount, boolean isCredit, Date txnDate, ObjectNode result)
            throws IDOSException;

    public double getBranchCashBalanceOnDate(Date txnDate, Users user, long branchId, EntityManager em, short cashType)
            throws IDOSException;
}
