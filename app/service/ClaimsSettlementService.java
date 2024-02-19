/* Project: IDOS 1.0
 * Module: Travel Advance Settlement
 * Filename: ClaimsSettlementService.java
 * Component Realisation: Java Interface
 * Prepared By: Sunil Namdev
 * Description: Modules to advance travel settlement
 * Copyright (c) 2016 IDOS

 * MODIFICATION HISTORY
 * Version		Date		   	Author		      Remarks
 * -------------------------------------------------------------------------
 *  0.1  Aug 30, 2016	                  		  - Initial Version
 * -------------------------------------------------------------------------
 */
package service;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import model.ClaimTransaction;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

/**
 * Created by Sunil Namdev on 31-08-2016.
 */
public interface ClaimsSettlementService extends BaseService {

        String QUERY_USER_NON_SETTLED_TRANSACTIONS = "SELECT obj \n" +
                        "  FROM ClaimTransaction obj\n" +
                        " WHERE obj.createdBy.id = :userId \n" +
                        "   AND obj.transactionPurpose = " + IdosConstants.REQUEST_FOR_TRAVEL_ADVANCE + "\n" +
                        "   AND obj.claimsDueSettlement >= 0.0\n" +
                        "   AND obj.settlementStatus = '" + IdosConstants.SettlementStatus.NOT_SETTLED + "'\n" +
                        "   AND obj.transactionStatus = '" + IdosConstants.ClaimTransactionStatus.ACCOUNTED + "'\n" +
                        "   AND obj.presentStatus = 1\n" +
                        " ORDER BY obj.createdAt desc \n";

        FileUploadService FILE_UPLOAD_SERVICE = new FileUploadServiceImpl();

        public ObjectNode submitForApproval(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
                        EntityTransaction entitytransaction) throws IDOSException;

        public ObjectNode claimSettlementAccountantAction(ObjectNode result, JsonNode json, Users user,
                        EntityManager entityManager, EntityTransaction entitytransaction) throws IDOSException;

        public ObjectNode populateUserUnsettledTravelClaimAdvances(ObjectNode result, JsonNode json, Users user,
                        EntityManager entityManager, EntityTransaction entitytransaction);

        public ObjectNode displayUnsettledAdvancesDetails(ObjectNode result, JsonNode json, Users user,
                        EntityManager entityManager, EntityTransaction entitytransaction);

        public void sendSocketResponeToClientSettlement(ClaimTransaction claimTransaction, Users user,
                        ObjectNode result);
}
