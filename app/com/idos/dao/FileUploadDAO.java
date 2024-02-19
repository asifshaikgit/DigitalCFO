package com.idos.dao;

import model.ClaimTransaction;
import model.IdosProvisionJournalEntry;
import model.Transaction;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.IDOSException;

import play.libs.Json;
import play.mvc.Result;
import service.FileUploadService;
import service.FileUploadServiceImpl;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

/**
 * @author Sunil K. Namdev created on 11.09.2019
 */
public interface FileUploadDAO extends BaseDAO {
        String UPDATE_UPLOAD_FILE_LOGS_QUERY = "update IdosUploadFilesLogs obj set obj.referenceId=?1, obj.uploadModule=?2, obj.presentStatus=?3 where obj.organization.id=?4 and obj.fileUrl=?5";
        String DELETE_UPLOAD_FILE_LOGS_QUERY = "delete from IdosUploadFilesLogs obj where obj.organization.id=?1 and obj.referenceId is null";
        String UNCOMMITTED_FILE_LIST_QUERY = "select obj.id, obj.fileUrl from IdosUploadFilesLogs obj where obj.organization.id=?1 and obj.referenceId is null";

        boolean saveUpdateTxnUpload(EntityManager entityManager, JsonNode json, ArrayNode an, Users user);

        void updateUploadFileLogs(EntityManager entityManager, Users user, String fileUrls, Long referenceId,
                        String uploadModule) throws IDOSException;

        public void deleteUncommittedFiles(EntityManager entityManager, JsonNode json, ArrayNode an, Users user)
                        throws IDOSException;
}
