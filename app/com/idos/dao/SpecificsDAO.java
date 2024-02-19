package com.idos.dao;

import model.Specifics;
import model.Users;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.IDOSException;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * Created by Sunil K. Namdev on 06-07-2017.
 */
public interface SpecificsDAO extends BaseDAO {
        String TAX_JQL = "select distinct obj from BranchTaxes obj where obj.organization.id = ?1 and obj.taxType = ?2 and obj.presentStatus=1";

        String TAX_BRANCH_JQL = "select distinct obj from BranchTaxes obj where obj.organization.id = ?1 and obj.branch.id  = ?2 and obj.taxType = ?3 and obj.presentStatus=1";

        String COA_57_JPQL = "select t1.fromBranch.id, t1.toBranch.id, t1.fromBranch.name, t1.toBranch.name, t1.id from InterBranchMapping t1 where t1.organization.id=?1 and t1.presentStatus=1 order by t1.fromBranch.name";

        String COA_57_BRANCH_JPQL = "select t1.fromBranch.id, t1.toBranch.id, t1.fromBranch.name, t1.toBranch.name, t1.id from InterBranchMapping t1 where t1.organization.id=?1 and t1.toBranch.id=?2 and t1.presentStatus=1 order by t1.fromBranch.name";

        String COA_BY_ACCOUNT_CODE_JPQL = "from Specifics obj WHERE obj.organization.id = ?1 and obj.accountCodeHirarchy like ?2 and obj.presentStatus=1";

        public void getChildCOA(JsonNode json, Users user, EntityManager entityManager, ObjectNode results)
                        throws IDOSException;

        public boolean getInterBranchAccountsWithHead(EntityManager em, Users user, ArrayNode an);

        public boolean getInterBranchAccountsWithHead4Branch(EntityManager em, Users user, ArrayNode an, long branchId);

        public boolean getInterBranchMappingAccountsWithHead(EntityManager em, Users user, ArrayNode an);

        public boolean getInterBranchWithIdHead(EntityManager em, Users user, ArrayNode an);

        public void getTaxCOAChilds(int coaIdentForDataValid, EntityManager entityManager, Users user,
                        String coaActCode,
                        ArrayNode an);

        public void getTaxCOAChildsForBranch(int coaIdentForDataValid, EntityManager entityManager, Users user,
                        String coaActCode, Long branchId, ArrayNode an);

        public Boolean isSpecificHasTxnsAndOpeningBal(Specifics specifics, EntityManager entityManager)
                        throws IDOSException;
}
