package service;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.IDOSException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import model.Branch;
import model.IdosProvisionJournalEntry;
import model.Users;
import model.Vendor;
import pojo.ProvisionJournalEntryDetailPojo;

/**
 * Created by snamdev on 13.6.16.
 */
public interface ProvisionJournalEntryService extends BaseService {
        void saveProvisionJournalEntryBRSDate(Users user, EntityManager entityManager,
                        EntityTransaction entitytransaction,
                        String transactionRef, String brsBankDate) throws Exception;

        public void getProvisionJournalEntryList(Users user, String roles, ArrayNode recordsArrayNode,
                        EntityManager entityManager);

        public void searchProvisionJournalEntry(Users user, String roles, ArrayNode an, JsonNode json,
                        EntityManager entityManager);

        public List<ProvisionJournalEntryDetailPojo> getProvisionJournalEntryDetail(EntityManager entityManager,
                        IdosProvisionJournalEntry provisionJournalEntry, StringBuilder itemParentName,
                        StringBuilder creditItemsName, StringBuilder debitItemsName);

        public ObjectNode provisionJournalEntry(ObjectNode result, JsonNode json, Users user,
                        EntityManager entityManager,
                        EntityTransaction entitytransaction, IdosProvisionJournalEntry newProvJournalEntry)
                        throws Exception;

        public int provisionApproverAction(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
                        EntityTransaction entitytransaction, IdosProvisionJournalEntry newProvJournalEntry)
                        throws IDOSException;

        public ObjectNode getDashboardProvisionEntriesDataBranchWise(String startDate, String endDate,
                        Map<String, Double> totalProvJourEntForExpIncMap, Map<String, Double> allSpecifcsAmtData,
                        Map<String, Double> vendorPayablesData, Map<String, Double> custReceivablesData, Users user,
                        EntityManager entityManager) throws Exception;

        public Map<String, Double> getDashboardProvisionEntriesDataForBranch(String startDate, String endDate,
                        Users user,
                        Map<String, Double> allSpecifcsAmtData, Map<String, Double> vendorPayablesData,
                        Map<String, Double> custReceivablesData, Branch branch, EntityManager entityManager)
                        throws Exception;

        public List getDetailProvisionEntriesForCustVen(EntityManager entityManager, String startDate, String endDate,
                        Users user, Branch branch, Vendor vendor, String vendorType);

        void sendProvisionWebSocketResponse(IdosProvisionJournalEntry newProvJournalEntry, Users user,
                        EntityManager entityManager, ObjectNode result);
}
