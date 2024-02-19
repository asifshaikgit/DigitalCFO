package service;

import com.idos.util.IDOSException;
import model.Organization;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ser;
import com.fasterxml.jackson.databind.node.ArrayNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.io.File;
import java.util.List;

/**
 * @author Sunil K. Namdev created on 24.08.2019
 */
public interface FileUploadService extends BaseService {
    String upload(File sourceFile, String filename, String blobContainerName) throws IDOSException;

    String delete(String fileUri, String filename, Organization org) throws IDOSException;

    boolean saveUpdateTxnUpload(EntityManager em, JsonNode json, ArrayNode an, Users user);

    boolean deleteBlobsList(String urls, Organization org, Users user, boolean ifDeleteUncommitedFiles,
            EntityManager em) throws IDOSException;

    void updateUploadFileLogs(EntityManager entityManager, Users user, String fileUrls, Long referenceId,
            String uploadModule) throws IDOSException;

    void deleteUncommittedFiles(EntityManager entityManager, JsonNode json, ArrayNode an, Users user)
            throws IDOSException;

    boolean validUserPermissionForUpload(EntityManager entityManager, String txnRefrenceNo, Users user)
            throws IDOSException;

    boolean validUserPermissionForDelDoc(EntityManager entityManager, String txnStatus, Users user)
            throws IDOSException;
}
