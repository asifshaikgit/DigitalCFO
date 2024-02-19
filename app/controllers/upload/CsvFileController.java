package controllers.upload;

/*import com.download.GenericCsvWriter;*/

import com.idos.util.IdosConstants;
import com.typesafe.config.ConfigFactory;
import controllers.StaticController;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.dao.upload.UploadDataDao;
import com.idos.dao.upload.UploadDataDaoImpl;
import com.idos.upload.GenericUpload;
import com.idos.util.IDOSException;
import model.upload.IdosFileLog;
import model.upload.IdosUploadLog;
import com.typesafe.config.Config;
import play.mvc.Results;
import java.util.logging.Level;
import model.Users;

import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;
import javax.inject.Inject;
import play.Application;
import play.mvc.Http.Request;

/**
 * @author Sunil K Namdev
 * @version 1.0
 *          Date: 31.05.2020
 */
public class CsvFileController extends StaticController {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    public static Application application;
    private Request request;

    /*
     * @Inject
     * public CsvFileController(JPAApi jpaApi, Application application){
     * super(application);
     * this.jpaApi = jpaApi;
     * entityManager = EntityManagerProvider.getEntityManager();
     * }
     */
    @Inject
    public CsvFileController(Application application) {
        super(application);
    }

    private static GenericUpload genericupload = new GenericUpload();
    private static UploadDataDao uploadDao = new UploadDataDaoImpl();
    private static Config config = ConfigFactory.parseFile(new File("conf/application.conf"));
    private static String TXN_TEMP_URL = "logs/templates/Transaction_Template.xlsx";

    /*
     * @Transactional
     * public Result uploadCsv(String uploadType) {
     * if (log.isLoggable(Level.FINE))
     * log.log(Level.FINE, "START :: uploadCsv() type: " + uploadType);
     * String operation = "append";
     * // EntityManager em = getEntityManager();
     * EntityTransaction emtxn = entityManager.getTransaction();
     * ObjectNode result = Json.newObject();
     * ArrayNode an = result.putArray("successUploading");
     * long totalRowsInserted = 0;
     * Users user = null;
     * IdosUploadLog idosUploadLog = null;
     * try {
     * user = getUserInfo(request);
     * if (user == null) {
     * return unauthorized();
     * }
     * String tableName = ConfigFactory.load().getString(uploadType + ".tableName");
     * String uploadDate = IdosConstants.REPORTDF.format(new java.util.Date());
     * Http.MultipartFormData<File> body = request.body().asMultipartFormData();
     * List<Http.MultipartFormData.FilePart<File>> files = body.getFiles();
     * File file = null;
     * String fileName = null;
     * for (Http.MultipartFormData.FilePart<File> filePart : files) {
     * fileName = filePart.getFilename();
     * Pattern regex = Pattern.compile("[$&+,:;=\\\\?@#|/'<>^*()%!-]");
     * if (regex.matcher(fileName).find() || fileName.contains(" ")) {
     * throw new IDOSException("CSV-100", "Filename", "Wrong Filename",
     * "file name should not contain space or special character other than underscore"
     * );
     * }
     * if (operation.equals("append")) {
     * int fileNameLen = fileName.lastIndexOf(".");
     * if (fileNameLen != -1) {
     * long orgId = user.getOrganization().getId();
     * String[] fileArray = fileName.split("_");
     * if (fileArray.length < 3 || orgId != (Long.parseLong(fileArray[1]))) {
     * throw new IDOSException("CSV-100", "Filename", "Wrong Filename",
     * "file name is wrong if want to upload then rename the file like start trans_<organization id>_<ddMMYYYY>, Or please download a new  template by clicking on download template button for uploading data "
     * );
     * }
     * }
     * }
     * fileName = fileName.toLowerCase();
     * if (log.isLoggable(Level.FINE)) log.log(Level.FINE, "Fie to upload: " +
     * fileName);
     * int found = UPLOAD_DATA_SERVICE.isCsvFileExist(fileName, entityManager,
     * tableName);
     * if (found > 0 && operation.equals("append")) {
     * throw new IDOSException("CSV-100", "Data", "Already present",
     * "file is already uploaded if want to upload then rename the file like start name + _ + Access ID + _ + access name  + _ + ddMMYYYY, Or please download a new template for uploading data "
     * );
     * } else if (operation.equals("update") && found > 0) {
     * emtxn.begin();
     * int disabled = UPLOAD_DATA_SERVICE.disableCsvFile(fileName, entityManager,
     * tableName);
     * emtxn.commit();
     * } else if (operation.equals("update") && found <= 0) {
     * throw new IDOSException("CSV-200", "Data", "Not found",
     * "no file available with this name: " + fileName);
     * }
     * file = (File) filePart.getRef();
     * //totalRowsInserted = genericupload.getLineCount(file);
     * OutputStream out = null;
     * InputStream filecontent = null;
     * String path = ConfigFactory.load().getString("UploadConfig.inFileDir");
     * File filepath = new File(path);
     * if (!filepath.exists()) {
     * filepath.mkdir();
     * }
     * out = new FileOutputStream(new File(path + File.separator + fileName));
     * filecontent = new java.io.FileInputStream(file);
     * int read = 0;
     * final byte[] bytes = new byte[1024];
     * while ((read = filecontent.read(bytes)) != -1) {
     * out.write(bytes, 0, read);
     * }
     * filecontent.close();
     * out.flush();
     * out.close();
     * emtxn.begin();
     * boolean status = genericupload.uploadData(uploadType, operation);
     * if (status) {
     * IdosFileLog idosFileLog = uploadDao.saveUploadedFileDeatils(entityManager,
     * fileName, user, operation, uploadDate);
     * emtxn.commit();
     * result.put("status", "Uploaded Successfully");
     * } else {
     * result.put("message", "Fail to upload, please contact support team.");
     * }
     * }
     * idosUploadLog = UPLOAD_DATA_SERVICE.findByFileName(entityManager, fileName);
     * if (idosUploadLog != null) {
     * if ("append".equals(uploadType)) {
     * result.put("totalRowsInserted", idosUploadLog.getRecordsInserted());
     * } else {
     * result.put("totalRowsInserted", idosUploadLog.getRecordsUpdated());
     * }
     * } else {
     * result.put("totalRowsInserted", totalRowsInserted);
     * }
     * if (log.isLoggable(Level.FINE)) log.log(Level.FINE, "END :: uploadCsv()" +
     * result);
     * return Results.ok(file);
     * } catch (Exception ex) {
     * reportException(entityManager, emtxn, user, ex, result);
     * return null;
     * } catch (Throwable th) {
     * reportThrowable(entityManager, emtxn, user, th, result);
     * return null;
     * }
     * }
     * 
     * @Transactional
     * public Result downloadTxnTemplate() {
     * log.log(Level.FINE, ">>>> Start inside download transaction Template");
     * // EntityManager entityManager = getEntityManager();
     * File file = null;
     * Users user = null;
     * try {
     * user = getUserInfo(request);
     * if (user == null) {
     * return unauthorized();
     * }
     * String orgName = user.getOrganization().getName().replaceAll("\\s", "");
     * String path = TXN_TEMP_URL;
     * File filepath = new File(path);
     * String fileName = orgName + "_Transaction_Template.xlsx";
     * if (!filepath.exists()) {
     * filepath.mkdir();
     * }
     * file = new File(path);
     * return
     * Results.ok(file).withHeader("ContentType","application/xlsx").withHeader(
     * "Content-Disposition", "attachment; filename=" + fileName);
     * } catch (Exception ex) {
     * log.log(Level.SEVERE, "Error", ex);
     * String strBuff = getStackTraceMessage(ex);
     * expService.sendExceptionReport(strBuff, user.getEmail(),
     * user.getOrganization().getName(),
     * Thread.currentThread().getStackTrace()[1].getMethodName());
     * List<String> errorList = getStackTrace(ex);
     * return null;
     * }
     * }
     */
}
