package service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.logging.Level;
import actor.CreatorActor;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.inject.Inject;
import model.Branch;
import model.IdosProvisionJournalEntry;
import model.Users;
import model.Vendor;
import play.db.jpa.JPAApi;
import play.mvc.WebSocket;
import pojo.ProvisionJournalEntryDetailPojo;
import pojo.TransactionViewResponse;
import akka.stream.javadsl.*;
import akka.actor.*;
import akka.NotUsed;

/**
 * Created by Sunil K. Namdev on 13.6.16.
 */
public class ProvisionJournalEntryServiceImpl implements ProvisionJournalEntryService {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    @Override
    public void saveProvisionJournalEntryBRSDate(Users user, EntityManager entityManager,
            EntityTransaction entitytransaction, String transactionRef, String brsBankDate) throws Exception {
        provisionJournalDAO.saveProvisionJournalEntryBRSDate(user, entityManager, entitytransaction, transactionRef,
                brsBankDate);
    }

    @Override
    public ObjectNode provisionJournalEntry(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
            EntityTransaction entitytransaction, IdosProvisionJournalEntry newProvJournalEntry) throws Exception {
        result = provisionJournalDAO.provisionJournalEntry(result, json, user, entityManager, entitytransaction,
                newProvJournalEntry);
        return result;
    }

    @Override
    public int provisionApproverAction(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
            EntityTransaction entitytransaction, IdosProvisionJournalEntry newProvJournalEntry) throws IDOSException {
        return provisionJournalDAO.provisionApproverAction(result, json, user, entityManager, entitytransaction,
                newProvJournalEntry);
    }

    @Override
    public void getProvisionJournalEntryList(Users user, String roles, ArrayNode recordsArrayNode,
            EntityManager entityManager) {
        provisionJournalDAO.getProvisionJournalEntryList(user, roles, recordsArrayNode, entityManager);
    }

    @Override
    public void searchProvisionJournalEntry(Users user, String roles, ArrayNode an, JsonNode json,
            EntityManager entityManager) {
        provisionJournalDAO.searchProvisionJournalEntry(user, roles, an, json, entityManager);
    }

    @Override
    public List<ProvisionJournalEntryDetailPojo> getProvisionJournalEntryDetail(EntityManager entityManager,
            IdosProvisionJournalEntry provisionJournalEntry, StringBuilder itemParentName,
            StringBuilder creditItemsName, StringBuilder debitItemsName) {
        return provisionJournalDAO.getProvisionJournalEntryDetail(entityManager, provisionJournalEntry, itemParentName,
                creditItemsName, debitItemsName);
    }

    @Override
    public ObjectNode getDashboardProvisionEntriesDataBranchWise(String startDate, String endDate,
            Map totalProvJourEntForExpIncMap, Map allSpecifcsAmtData, Map vendorPayablesData, Map custReceivablesData,
            Users user, EntityManager entityManager) throws Exception {
        return provisionJournalDAO.getDashboardProvisionEntriesDataBranchWise(startDate, endDate,
                totalProvJourEntForExpIncMap, allSpecifcsAmtData, vendorPayablesData, custReceivablesData, user,
                entityManager);
    }

    @Override
    public Map getDashboardProvisionEntriesDataForBranch(String startDate, String endDate, Users user,
            Map allSpecifcsAmtData, Map vendorPayablesData, Map custReceivablesData, Branch branch,
            EntityManager entityManager) throws Exception {
        return provisionJournalDAO.getDashboardProvisionEntriesDataForBranch(startDate, endDate, user,
                allSpecifcsAmtData, vendorPayablesData, custReceivablesData, branch, entityManager);
    }

    @Override
    public List getDetailProvisionEntriesForCustVen(EntityManager entityManager, String startDate, String endDate,
            Users user, Branch branch, Vendor vendor, String vendorType) {
        return provisionJournalDAO.getDetailProvisionEntriesForCustVen(entityManager, startDate, endDate, user, branch,
                vendor, vendorType);
    }

    @Override
    public void sendProvisionWebSocketResponse(IdosProvisionJournalEntry newProvJournalEntry, Users user,
            EntityManager entityManager, ObjectNode result) {
        log.log(Level.FINE, ">>>> Start");
        String branchName = "";
        String projectName = "";
        String customerVendorName = "";
        if (newProvJournalEntry.getDebitBranch() != null) {
            branchName = newProvJournalEntry.getDebitBranch().getName();
        }
        StringBuilder itemParentName = new StringBuilder();
        StringBuilder creditItemsName = new StringBuilder();
        StringBuilder debitItemsName = new StringBuilder();
        getProvisionJournalEntryDetail(entityManager, newProvJournalEntry, itemParentName, creditItemsName,
                debitItemsName);
        String itemName = IdosUtil.removeLastChar(debitItemsName.toString()) + "#"
                + IdosUtil.removeLastChar(creditItemsName.toString());
        String approverEmail = "";
        String approverLabel = "";
        String txnRemarks = "";
        if (newProvJournalEntry.getApproverActionBy() != null) {
            approverLabel = "APPROVER:";
            approverEmail = newProvJournalEntry.getApproverActionBy().getEmail();
        }
        String invoiceDate = "";
        String invoiceDateLabel = "";
        if (newProvJournalEntry.getReversalDate() != null) {
            invoiceDateLabel = "REVERSAL DATE:";
            invoiceDate = IdosConstants.idosdf.format(newProvJournalEntry.getReversalDate());
        }
        String txnResultDesc = "";
        if (newProvJournalEntry.getPurpose() != null && !newProvJournalEntry.getPurpose().equals("null")) {
            txnResultDesc = newProvJournalEntry.getPurpose();
        }
        if (newProvJournalEntry.getTxnRemarks() != null) {
            txnRemarks = newProvJournalEntry.getTxnRemarks();
        }
        String approverEmails = "";
        String additionalApprovarUsers = "";
        String selectedAdditionalApproval = "";
        String txnSpecialStatus = "";
        if (newProvJournalEntry.getApproverEmails() != null) {
            approverEmails = newProvJournalEntry.getApproverEmails();
        }
        if (newProvJournalEntry.getAdditionalApproverUserEmails() != null) {
            additionalApprovarUsers = newProvJournalEntry.getAdditionalApproverUserEmails();
        }
        if (newProvJournalEntry.getSelectedAdditionalApprover() != null) {
            selectedAdditionalApproval = newProvJournalEntry.getSelectedAdditionalApprover();
        }
        // Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
        // Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
        // for (int i = 0; i < keyArray.length; i++) {
        // StringBuilder sbquery = new StringBuilder("");
        // sbquery.append("select obj from Users obj WHERE obj.email ='" + keyArray[i] +
        // "' and obj.presentStatus=1");
        // List<Users> orgusers = genericDAO.executeSimpleQuery(sbquery.toString(),
        // entityManager);
        // if (!orgusers.isEmpty() && orgusers.get(0).getOrganization().getId() ==
        // user.getOrganization().getId()) {
        // orgtxnregistereduser.put(keyArray[i].toString(),
        // CreatorActor.expenseregistrered.get(keyArray[i]));
        // }
        // }
        String instrumentNumber = newProvJournalEntry.getInstrumentNumber() == null ? ""
                : newProvJournalEntry.getInstrumentNumber();
        String instrumentDate = newProvJournalEntry.getInstrumentDate() == null ? ""
                : newProvJournalEntry.getInstrumentDate();
        String txnDocument = newProvJournalEntry.getSupportingDocuments() == null ? ""
                : newProvJournalEntry.getSupportingDocuments();
        TransactionViewResponse.addActionTxn(newProvJournalEntry.getId(), branchName, projectName, itemName,
                itemParentName.toString(), "", "", "", "", customerVendorName,
                newProvJournalEntry.getTransactionPurpose().getTransactionPurpose(),
                IdosConstants.idosdf.format(newProvJournalEntry.getTransactionDate()), invoiceDateLabel, invoiceDate,
                "", 0d, 0.0, newProvJournalEntry.getTotalDebitAmount(), newProvJournalEntry.getTotalDebitAmount(),
                txnResultDesc, "", newProvJournalEntry.getTransactionStatus(),
                newProvJournalEntry.getCreatedBy().getEmail(), approverLabel, approverEmail, txnDocument, txnRemarks,
                "", approverEmails, additionalApprovarUsers, selectedAdditionalApproval,
                txnSpecialStatus, 0.0, "", instrumentNumber, instrumentDate,
                newProvJournalEntry.getTransactionPurpose().getId(), "", "", 0,
                newProvJournalEntry.getTransactionRefNumber(), 0l, 0.0, 0, 0, result);
    }
}
