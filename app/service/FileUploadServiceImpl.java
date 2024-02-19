package service;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import model.IdosUploadFilesLogs;
import model.Organization;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import play.libs.Json;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Sunil K. Namdev created on 24.08.2019
 */
public class FileUploadServiceImpl implements FileUploadService {
    private static String storageConnectionString = "DefaultEndpointsProtocol=https;" + "AccountName=teststoragesunil;"
            + "AccountKey=fqKybfNrHXvsO2hMP1z+6pF1/GyberJe+20CfOIZehaJrelAJAJX2HmgdCPC0Rj+cJaavWzkTaFZPO+jmurymQ==";
    private static String blobContainerGlobal = "test-container";

    @Inject
    public FileUploadServiceImpl() {
    }

    static {
        String blobUrlTmp = ConfigFactory.load().getString("blob.url");
        if (blobUrlTmp != null && !"".equals(blobUrlTmp)) {
            storageConnectionString = blobUrlTmp;
        }
        String blobContainerTmp = ConfigFactory.load().getString("blob.container");
        if (blobContainerTmp != null && !"".equals(blobContainerTmp)) {
            blobContainerGlobal = blobContainerTmp;
        }
    }

    @Override
    public String upload(File sourceFile, String filename, String blobContainerName) throws IDOSException {
        String fileUri = null;
        File downloadedFile = null;
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "start Azure Blob storage :: " + filename);
        if (blobContainerName == null || blobContainerName.equals("")) {
            blobContainerName = blobContainerGlobal;
        }
        CloudStorageAccount storageAccount;
        CloudBlobClient blobClient = null;
        CloudBlobContainer container = null;

        try {
            // Parse the connection string and create a blob client to interact with Blob
            // storage
            storageAccount = CloudStorageAccount.parse(storageConnectionString);
            blobClient = storageAccount.createCloudBlobClient();
            container = blobClient.getContainerReference(blobContainerName);

            // Create the container if it does not exist with public access.
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, "Creating container: " + container.getName());
            container.createIfNotExists(BlobContainerPublicAccessType.BLOB, new BlobRequestOptions(),
                    new OperationContext());
            // Getting a blob reference
            CloudBlockBlob blob = container.getBlockBlobReference(filename);
            if (blob.exists()) {
                throw new IDOSException(IdosConstants.INVALID_STORAGE_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                        IdosConstants.INVALID_STORAGE_EXCEPTION, "File already present: " + filename);
            }
            // Creating blob and uploading file to it
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, "Uploading the sample file " + sourceFile.getAbsolutePath());
            blob.uploadFromFile(sourceFile.getAbsolutePath());
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, "Uploaded file path is: " + blob.getUri());
            if (blob.getUri() != null)
                fileUri = blob.getUri().toString();
        } catch (StorageException ex) {
            log.log(Level.SEVERE, "error", ex);
            throw new IDOSException(IdosConstants.INVALID_STORAGE_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                    IdosConstants.INVALID_STORAGE_EXCEPTION,
                    String.format("Error returned from the service. Http code: %d and error code: %s",
                            ex.getHttpStatusCode(), ex.getErrorCode()));
        } catch (Exception ex) {
            if (ex instanceof IDOSException) {
                log.log(Level.SEVERE, ((IDOSException) ex).getErrorText() + " " + filename);
                throw (IDOSException) ex;
            } else {
                log.log(Level.SEVERE, "error", ex);
                throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                        IdosConstants.INVALID_STORAGE_EXCEPTION, ex.getMessage());
            }
        } finally {
            if (sourceFile != null) {
                sourceFile.deleteOnExit();
            }
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ":: Azure Blob storage End :: " + fileUri);
        return fileUri;
    }

    @Override
    public String delete(String fileUri, String filename, Organization org) throws IDOSException {
        if (fileUri == null || filename == null || "".equals(fileUri) || "".equals(filename)) {
            return null;
        }
        String blobContainerName = IdosUtil.getOrganizationName4Blob(org.getName()) + "-" + org.getId();
        blobContainerName = blobContainerName.toLowerCase();
        File downloadedFile = null;
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "Delete Blob storage start :: " + filename);
        if (blobContainerName == null || blobContainerName.equals("")) {
            blobContainerName = blobContainerGlobal;
        }
        CloudStorageAccount storageAccount;
        CloudBlobClient blobClient = null;
        CloudBlobContainer container = null;
        try {
            // Parse the connection string and create a blob client to interact with Blob
            // storage
            storageAccount = CloudStorageAccount.parse(storageConnectionString);
            blobClient = storageAccount.createCloudBlobClient();
            container = blobClient.getContainerReference(blobContainerName);
            // Create the container if it does not exist with public access.
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, "container: " + container.getName());
            if (container.exists()) {
                // Getting a blob reference
                CloudBlockBlob blob = container.getBlockBlobReference(filename);
                if (log.isLoggable(Level.FINE))
                    log.log(Level.FINE, "===== " + blob.getUri());
                if (blob.exists()) {
                    blob.delete();
                } /*
                   * else{
                   * throw new IDOSException(IdosConstants.INVALID_STORAGE_ERRCODE,
                   * IdosConstants.TECHNICAL_EXCEPTION,
                   * IdosConstants.INVALID_STORAGE_EXCEPTION,"File is not present");
                   * }
                   */
            }
        } catch (StorageException ex) {
            log.log(Level.SEVERE, "error", ex);
            throw new IDOSException(IdosConstants.INVALID_DATA_EXCEPTION, IdosConstants.BUSINESS_EXCEPTION,
                    IdosConstants.INVALID_STORAGE_EXCEPTION,
                    String.format("Error returned from the service. Http code: %d and error code: %s",
                            ex.getHttpStatusCode(), ex.getErrorCode()));
        } catch (Exception ex) {
            log.log(Level.SEVERE, "error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    IdosConstants.INVALID_STORAGE_EXCEPTION, ex.getMessage());
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "Delete Blob storage End :: " + fileUri);
        return fileUri;
    }

    @Override
    public boolean saveUpdateTxnUpload(EntityManager em, JsonNode json, ArrayNode an, Users user) {
        return FILE_UPLOAD_DAO.saveUpdateTxnUpload(em, json, an, user);
    }

    @Override
    public void updateUploadFileLogs(EntityManager entityManager, Users user, String fileUrls, Long referenceId,
            String uploadModule) throws IDOSException {
        FILE_UPLOAD_DAO.updateUploadFileLogs(entityManager, user, fileUrls, referenceId, uploadModule);
    }

    @Override
    public boolean deleteBlobsList(String urls, Organization org, Users user, boolean ifDeleteUncommitedFiles,
            EntityManager em) throws IDOSException {
        if (urls == null || urls.equals("")) {
            return false;
        }
        String[] arr = urls.split(",");
        int arrLength = arr.length;
        for (int i = 0; i < arrLength; i++) {
            String url = null;
            if (arr[i] != null) {
                String[] emailNurl = arr[i].split("#");
                if (emailNurl.length > 1) {
                    url = emailNurl[1];
                } else {
                    url = emailNurl[0];
                }
            }
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            delete(url, fileName, org);
            if (!ifDeleteUncommitedFiles) {
                List<IdosUploadFilesLogs> filesLogs = IdosUploadFilesLogs.findByFileUrl(em,
                        user.getOrganization().getId(), url);
                if (filesLogs.size() > 0) {
                    filesLogs.get(0).setPresentStatus(0);
                    genericDAO.saveOrUpdate(filesLogs.get(0), user, em);
                }
            }
        }
        return true;
    }

    @Override
    public void deleteUncommittedFiles(EntityManager entityManager, JsonNode json, ArrayNode an, Users user)
            throws IDOSException {
        FILE_UPLOAD_DAO.deleteUncommittedFiles(entityManager, json, an, user);
    }

    public boolean validUserPermissionForUpload(EntityManager em, String txnRefrenceNo, Users user)
            throws IDOSException {
        EntityTransaction entitytransaction = em.getTransaction();
        String rolestr = user.getUserRolesName();
        String userRoleType = rolestr.replaceAll(",", "");
        Long userId = user.getId();
        ObjectNode result = Json.newObject();

        if (txnRefrenceNo.equals("default") && (userRoleType.equals("CREATOR") || userRoleType.equals("ACCOUNTANT"))) {
            return true;
        } else if (txnRefrenceNo.equals("default")
                && (!userRoleType.equals("CREATOR") || !userRoleType.equals("ACCOUNTANT"))) {
            return false;
        } else {
            String txnQuery = "select obj from Transaction obj where obj.transactionRefNumber=?";
            ArrayList txnparam = new ArrayList(1);
            txnparam.add(txnRefrenceNo);
            List<model.Transaction> txnRoles = genericDAO.queryWithParams(txnQuery, em, txnparam);
            model.Transaction transaction = txnRoles.get(0);
            String txnStatus = transaction.getTransactionStatus();
            if ((userRoleType.equals("CREATOR") || userRoleType.equals("ACCOUNTANT"))
                    && (txnStatus.equals("Require Approval") || txnStatus.equals("Require Additional Approval")
                            || txnStatus.equals("Accounted"))) {
                throw new IDOSException(IdosConstants.UNAUTHUSER_UPLOADDOC_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                        IdosConstants.UNAUTHUSER_UPLOADDOC_EXCEPTION,
                        "Unauthorise upload of document !!!As user type is invalid with transaction status ");
            } else if (userRoleType.equals("APPROVER")
                    && (txnStatus.equals("Accounted") || txnStatus.equals("Require Clarification"))) {
                throw new IDOSException(IdosConstants.UNAUTHUSER_UPLOADDOC_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                        IdosConstants.UNAUTHUSER_UPLOADDOC_EXCEPTION,
                        "Unauthorise upload of document !!!As user type is invalid with transaction status ");
            } else if (txnStatus.equals("Accounted")) {
                throw new IDOSException(IdosConstants.UNAUTHUSER_UPLOADDOC_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                        IdosConstants.UNAUTHUSER_UPLOADDOC_EXCEPTION,
                        "Unauthorise delete of document !!!As transaction is Accounted ");
            } else if (txnStatus.equals("Require Additional Approval")
                    && (userRoleType.equals("CREATOR") || userRoleType.equals("ACCOUNTANT"))) {
                throw new IDOSException(IdosConstants.UNAUTHUSER_UPLOADDOC_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                        IdosConstants.UNAUTHUSER_UPLOADDOC_EXCEPTION,
                        "Unauthorise upload of document !!!As user type is invalid with transaction status ");
            } else {
                return true;
            }
        }
    }

    @Override
    public boolean validUserPermissionForDelDoc(EntityManager entityManager, String txnStatus, Users user)
            throws IDOSException {
        String rolestr = user.getUserRolesName();
        String userRoleType = rolestr.replaceAll(",", "");
        if (txnStatus.equals("Accounted")) {
            throw new IDOSException(IdosConstants.UNAUTHUSER_DELDOC_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                    IdosConstants.UNAUTHUSER_DELDOC_EXCEPTION,
                    "Unauthorise delete of document !!!As transaction is Accounted ");
        } else if (txnStatus.equals("Require Clarification")) {
            throw new IDOSException(IdosConstants.UNAUTHUSER_DELDOC_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                    IdosConstants.UNAUTHUSER_DELDOC_EXCEPTION,
                    "Unauthorise delete of document !!!As transaction is Accounted ");
        } else if ((userRoleType.equals("CREATOR") || userRoleType.equals("ACCOUNTANT"))
                && (txnStatus.equals("Require Approval") || txnStatus.equals("Require Additional Approval")
                        || txnStatus.equals("Accounted"))) {
            throw new IDOSException(IdosConstants.UNAUTHUSER_DELDOC_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                    IdosConstants.UNAUTHUSER_DELDOC_EXCEPTION,
                    "Unauthorise delete of document !!!As user type is invalid with transaction status ");
        } else
            return true;
    }
}
