package com.idos.dao;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosDaoConstants;

import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.db.jpa.JPAApi;
import play.libs.Json;
import views.html.errorPage;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;

/**
 * @author Sunil K. Namdev created on 11.09.2019
 */
public class FileUploadDAOImpl implements FileUploadDAO {
    private static EntityManager em;

    public FileUploadDAOImpl() {
        em = EntityManagerProvider.getEntityManager();
    }

    @Override
    public boolean saveUpdateTxnUpload(EntityManager em, JsonNode json, ArrayNode an, Users user) {
        log.log(Level.FINE, ">>>> Start");
        String parentTr = json.findValue("modelId") != null ? json.findValue("modelId").asText() : null;
        String fileUrl = json.findValue("docUrl") != null ? json.findValue("docUrl").asText() : null;
        String fileName = json.findValue("fileName") != null ? json.findValue("fileName").asText() : null;
        String txnRefrenceNo = json.findValue("txnRefrenceNo") != null ? json.findValue("txnRefrenceNo").asText()
                : null;

        if (parentTr != null && !parentTr.equals("") && fileUrl != null && !fileUrl.equals("") && fileName != null
                && !fileName.equals("")) {
            if (txnRefrenceNo != null && txnRefrenceNo.startsWith(IdosConstants.MAIN_TXN_TYPE)) {
                Long id = Long.valueOf(parentTr.substring(17, parentTr.length()));
                Transaction txn = Transaction.findById(id);
                if (txn.getSupportingDocs() != null && !txn.getSupportingDocs().equals("")) {
                    txn.setSupportingDocs(txn.getSupportingDocs() + "," + user.getEmail() + "#" + fileUrl);
                } else {
                    txn.setSupportingDocs(user.getEmail() + "#" + fileUrl);
                }
                genericDao.saveOrUpdate(txn, user, em);
            } else if (parentTr.contains("transactionProvisionEntity")) {
                Long id = Long.valueOf(parentTr.substring(26, parentTr.length()));
                IdosProvisionJournalEntry idosProvJournalEntry = IdosProvisionJournalEntry.findById(id);
                if (idosProvJournalEntry.getSupportingDocuments() != null
                        && !idosProvJournalEntry.getSupportingDocuments().equals("")) {
                    idosProvJournalEntry.setSupportingDocuments(
                            idosProvJournalEntry.getSupportingDocuments() + "," + user.getEmail() + "#" + fileUrl);
                } else {
                    idosProvJournalEntry.setSupportingDocuments(user.getEmail() + "#" + fileUrl);
                }
                genericDao.saveOrUpdate(idosProvJournalEntry, user, em);
            } else if (parentTr.contains("claimsTransactionEntity")) {
                Long id = Long.valueOf(parentTr.substring(23, parentTr.length()));
                ClaimTransaction claimTxn = ClaimTransaction.findById(id);
                if (claimTxn.getSupportingDocuments() != null && !claimTxn.getSupportingDocuments().equals("")) {
                    claimTxn.setSupportingDocuments(
                            claimTxn.getSupportingDocuments() + "," + user.getEmail() + "#" + fileUrl);
                } else {
                    claimTxn.setSupportingDocuments(user.getEmail() + "#" + fileUrl);
                }
                genericDao.saveOrUpdate(claimTxn, user, em);
            } else if (txnRefrenceNo != null && txnRefrenceNo.startsWith(IdosConstants.BOM_TXN_TYPE)) {
                Long id = Long.valueOf(parentTr.substring(17, parentTr.length()));
                BillOfMaterialTxnModel txn = BillOfMaterialTxnModel.findById(id);
                if (txn.getSupportingDocs() != null && !txn.getSupportingDocs().equals("")) {
                    txn.setSupportingDocs(txn.getSupportingDocs() + "," + user.getEmail() + "#" + fileUrl);
                } else {
                    txn.setSupportingDocs(user.getEmail() + "#" + fileUrl);
                }
                genericDao.saveOrUpdate(txn, user, em);
            }

            ObjectNode row = Json.newObject();
            row.put("parentTr", parentTr);
            row.put("fileUrl", fileUrl);
            row.put("fileName", user.getEmail() + "#" + fileName);
            an.add(row);
        }
        return false;
    }

    @Override
    public void updateUploadFileLogs(EntityManager entityManager, Users user, String fileUrls, Long referenceId,
            String uploadModule) throws IDOSException {
        try {
            if (fileUrls != null && !fileUrls.equals("")) {
                String suppdocarr[] = fileUrls.split(",");
                for (int i = 0; i < suppdocarr.length; i++) {
                    String fileUrl = suppdocarr[i].substring(suppdocarr[i].indexOf('#') + 1, suppdocarr[i].length());
                    Query query = entityManager.createQuery(UPDATE_UPLOAD_FILE_LOGS_QUERY);
                    query.setParameter(1, referenceId);
                    query.setParameter(2, uploadModule);
                    query.setParameter(3, 1);
                    query.setParameter(4, user.getOrganization().getId());
                    query.setParameter(5, fileUrl);
                    query.executeUpdate();
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on Bill of Material for approval", ex.getMessage());
        }
    }

    // @Override
    /*
     * public void getUploadLog(EntityManager em, Users user, String fileUrl, Long
     * referenceId, String uploadModule, boolean isUpdatePresentStatus) throws
     * IDOSException{
     * try{
     * fileUrl = fileUrl.substring(fileUrl.indexOf('#')+1, fileUrl.length());
     * String sql =
     * "select obj from IdosUploadFilesLogs obj where obj.organization.id = ?1 and obj.referenceId= ?2 and obj.fileUrl = ?3"
     * ;
     * ArrayList inparams = new ArrayList(3);
     * inparams.add(user.getOrganization().getId());
     * inparams.add(referenceId);
     * inparams.add(fileUrl);
     * List<IdosUploadFilesLogs> list = genericDao.queryWithParamsName(sql, em,
     * inparams);
     * for(IdosUploadFilesLogs fileLog : list){
     * fileLog.setPresentStatus(0);
     * String module = fileLog.getUploadModule();
     * if(IdosConstants.ORG_MODULE.equalsIgnoreCase(module)){
     * Organization org = Organization.findById(referenceId);
     * } else if(IdosConstants.BRANCH_MODULE.equalsIgnoreCase(module)){
     * Branch branch = Branch.findById(referenceId);
     * } else if(IdosConstants.CUSTOMER_MODULE.equalsIgnoreCase(module)){
     * 
     * }
     * }
     * } catch (Exception ex) {
     * log.log(Level.SEVERE, "Error", ex);
     * throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE,
     * IdosConstants.TECHNICAL_EXCEPTION, "Error on Bill of Material for approval",
     * ex.getMessage());
     * }
     * }
     */

    @Override
    public void deleteUncommittedFiles(EntityManager entityManager, JsonNode json, ArrayNode an, Users user)
            throws IDOSException {
        ArrayList params = new ArrayList(1);
        params.add(user.getOrganization().getId());
        List<Object[]> uncommittedFilesList = genericDao.queryWithParamsNameGeneric(UNCOMMITTED_FILE_LIST_QUERY,
                entityManager, params);
        String urls = "";
        for (Object[] uncommittedFile : uncommittedFilesList) {
            if (uncommittedFile[1].toString() != null && !uncommittedFile[1].toString().equals("")) {
                if (urls.equals("")) {
                    urls += uncommittedFile[1].toString();
                } else {
                    urls += ("," + uncommittedFile[1].toString());
                }
            }
        }
        if (urls != null && !urls.equals("")) {
            FILE_UPLOAD_SERVICE.deleteBlobsList(urls, user.getOrganization(), user, true, entityManager);
        }

        genericDao.deleteByParamName(DELETE_UPLOAD_FILE_LOGS_QUERY, entityManager, params);
    }
}
