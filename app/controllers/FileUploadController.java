package controllers;

import com.idos.util.*;

import model.*;
import model.payroll.PayrollTransaction;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;
import play.mvc.Result;
import play.mvc.Results;
import play.libs.Files.TemporaryFile;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import org.apache.commons.codec.binary.Base64;
import play.Application;
import javax.inject.Inject;

public class FileUploadController extends StaticController {
	private final Application application;
	private static EntityManager entityManager;
	private Request request;

	@Inject
	public FileUploadController(Application application) {
		super(application);
		this.application = application;
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static String httpUrl = "http://localhost:8080/scanupload";
	private static int httpTimeout = 3000;
	static {
		String httpUrlTmp = ConfigFactory.load().getString("http.vscan.url");
		if (httpUrlTmp != null && !"".equals(httpUrlTmp)) {
			httpUrl = httpUrlTmp;
		}
		String httpTo = ConfigFactory.load().getString("http.vscan.timeout");
		if (httpTo != null && !"".equals(httpTo)) {
			httpTimeout = Integer.parseInt(httpTo);
		}
	}
	private static final RequestConfig httpParams = RequestConfig.custom().setConnectTimeout(httpTimeout)
			.setSocketTimeout(httpTimeout).build();

	@Transactional
	public Result fileUploadLogs(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		result.put("result", false);
		ArrayNode fileUploadResultan = result.putArray("fileUploadResultData");
		try {
			user = getUserInfo(request);
			if (null == user) {
				log.log(Level.SEVERE, "unauthorized");
				return unauthorized();
			}
			// response().setHeader("Access-Control-Allow-Origin", "*");
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			Integer uploadDestinationId = json.findValue("uploadDestinationId") != null
					? json.findValue("uploadDestinationId").asInt()
					: null;
			String fileName = json.findValue("fileName") != null ? json.findValue("fileName").asText() : null;
			Double fileSize = json.findValue("fileSize") != null ? json.findValue("fileSize").asDouble() : null;
			String fileUrl = json.findValue("fileUrl") != null ? json.findValue("fileUrl").asText() : null;
			String uploadModule = json.findValue("uploadModule") != null ? json.findValue("uploadModule").asText()
					: null;
			String uploadModuleElemName = json.findValue("uploadModuleElemName") != null
					? json.findValue("uploadModuleElemName").asText()
					: null;
			Long referenceId = json.findValue("referenceId") != null ? json.findValue("referenceId").asLong() : null;
			session.adding("email", email);
			IdosUploadFilesLogs fileLogs = new IdosUploadFilesLogs();
			fileLogs.setOrganization(user.getOrganization());
			if (uploadDestinationId != null) {
				fileLogs.setDestination(uploadDestinationId);
			}
			if (fileName != null && !fileName.equals("")) {
				fileLogs.setFileName(fileName);
			}
			if (fileSize != null) {
				fileLogs.setFileSize(fileSize);
			}
			if (fileUrl != null && !fileUrl.equals("")) {
				fileLogs.setFileUrl(fileUrl);
			}
			if (referenceId != null) {
				fileLogs.setReferenceId(referenceId);
			} else if (referenceId == null) { // for newly created transaction/branch/customer/vendor
				fileLogs.setPresentStatus(0);
			}
			if (uploadModule != null && !uploadModule.equals("")) {
				fileLogs.setUploadModule(uploadModule);
				if (uploadModule.equals(IdosConstants.ORG_MODULE) || uploadModule.equals(IdosConstants.BRANCH_MODULE)
						|| uploadModule.equals(IdosConstants.CUSTOMER_MODULE)
						|| uploadModule.equals(IdosConstants.VENDOR_MODULE)) {
					fileLogs.setPresentStatus(0);
				}
			} else if (uploadModule == null) {
				fileLogs.setPresentStatus(0);
			}
			if (uploadModuleElemName != null && !uploadModuleElemName.equals("")) {
				fileLogs.setUploadModuleElement(uploadModuleElemName);
			} else {
				throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
						IdosConstants.INVALID_DATA_EXCEPTION, "Module element is not defined.");
			}
			entitytransaction.begin();
			genericDAO.saveOrUpdate(fileLogs, user, entityManager);
			entitytransaction.commit();
			result.put("result", true);
		} catch (Exception ex) {
			result.put("result", false);
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result scanVirus1(Request request) {
		Users user = getUserInfo(request);
		if (user == null) {
			return unauthorized();
		}
		ObjectNode result = Json.newObject();
		result.put("status", "failed");
		try {
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>>>>> Start + " + json);
			// Http.MultipartFormData formData = request.body().asMultipartFormData();
			// if(formData != null){
			// List<Http.MultipartFormData.FilePart> filePartList = formData.getFiles();
			// for (Http.MultipartFormData.FilePart logoFilePart : filePartList){
			// if (logoFilePart != null) {
			String logoFilePart = json.findValue("fileUrl") != null ? json.findValue("fileUrl").asText() : null;
			File file = new File(logoFilePart);
			InputStream is = new java.io.FileInputStream(file);
			URL url = new URL("http://127.0.0.1:8080/scan");
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
			int responseCode = httpURLConnection.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			System.out.println(response.toString());
			in.close();
			is.close();
			// }
			// }
			// }
		} catch (Exception ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			result.put("status", "failed");
		}
		log.log(Level.FINE, ">>>>>>> End" + result);
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result scanVirus(Request request) {
		Users user = getUserInfo(request);
		if (user == null) {
			return unauthorized();
		}
		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>>>>> Start");
			Http.MultipartFormData<File> formData = request.body().asMultipartFormData();
			if (formData != null) {
				List<Http.MultipartFormData.FilePart<File>> filePartList = formData.getFiles();
				for (Http.MultipartFormData.FilePart<File> logoFilePart : filePartList) {
					if (logoFilePart != null) {
						TemporaryFile temporaryFile = (TemporaryFile) logoFilePart.getRef();
						String filePath = temporaryFile.path().toString();
						File uploadFile = new File(filePath);
						if (log.isLoggable(Level.FINE))
							log.log(Level.FINE, " =====>>>> " + logoFilePart.getFilename());
						String filename = FileUtil.sanitizeFileName(logoFilePart.getFilename());

						// Opens input stream of the file for reading data
						FileInputStream inputStream = new FileInputStream(uploadFile);
						HttpClient httpclient = HttpClientBuilder.create().build();

						HttpPost httppost = new HttpPost(httpUrl);
						httppost.setConfig(httpParams);

						// MultipartEntity entity = new MultipartEntity();
						MultipartEntityBuilder builder = MultipartEntityBuilder.create();
						/* example for setting a HttpMultipartMode */
						builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

						/* for adding an file part */
						FileBody fileBody = new FileBody(uploadFile);
						builder.addPart("file", fileBody);
						HttpEntity entity = builder.build();

						// set the file input stream and file name as arguments
						// entity.addPart("file", new InputStreamBody(inputStream,
						// uploadFile.getName()));
						httppost.setEntity(entity);

						// execute the request
						HttpResponse response = httpclient.execute(httppost);
						int statusCode = response.getStatusLine().getStatusCode();
						HttpEntity responseEntity = response.getEntity();
						String responseString = EntityUtils.toString(responseEntity, "UTF-8");
						String blobContainerName = IdosUtil.getOrganizationName4Blob(user.getOrganization().getName())
								+ "-" + user.getOrganization().getId();
						blobContainerName = blobContainerName.toLowerCase();
						if (statusCode == 200 && user.getOrganization().getFileUploadDestination() != null
								&& user.getOrganization().getFileUploadDestination() == 1) {
							String uri = FILE_UPLOAD_SERVICE.upload(uploadFile, filename, blobContainerName);
							if (uri == null) {
								statusCode = 901;
							} else {
								result.put("fileUri", uri);
								result.put("fileName", filename);
							}
						}
						log.log(Level.FINE, "[" + statusCode + "] " + responseString);
						inputStream.close();
						httppost.releaseConnection();
						result.put("statusCode", statusCode);
						result.put("statusMsg", responseString);
					}
				}
			}
		} catch (java.net.BindException ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
			result.put("statusMsg", "failed- Upload is not available, please contact support.");
		} catch (IDOSException ex) {
			log.log(Level.SEVERE, user.getEmail() + " " + ex.getErrorText());
			result.put("statusMsg", "failed - " + ex.getErrorText());
		} catch (Exception ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			result.put("statusMsg", "failed - " + ex.getMessage());
		}
		log.log(Level.FINE, ">>>>>>> End" + result);
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result delete(Request request) {
		Users user = getUserInfo(request);
		if (user == null) {
			log.log(Level.SEVERE, "unauthorized");
			return unauthorized();
		}
		ObjectNode result = Json.newObject();
		result.put("status", "failed");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		try {
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>>>>> Start " + json);
			String fileName = json.findValue("fileName") != null ? json.findValue("fileName").asText() : null;
			String fileUrl = json.findValue("fileUrl") != null ? json.findValue("fileUrl").asText() : null;
			String uploadModule = json.findValue("uploadModule") != null ? json.findValue("uploadModule").asText()
					: null;
			Long referenceId = json.findValue("referenceId") != null ? json.findValue("referenceId").asLong() : null;
			if (uploadModule != null && referenceId != null) {
				entitytransaction.begin();
				if (IdosConstants.BOM_TXN_TYPE.indexOf(uploadModule) != -1) {
					BillOfMaterialTxnModel txn = BillOfMaterialTxnModel.findById(referenceId);
					txn.setSupportingDocs(UploadUtil.removeSpecificUrl(txn.getSupportingDocs(), fileUrl));
					genericDAO.saveOrUpdate(txn, user, entityManager);
				} else if (IdosConstants.MAIN_TXN_TYPE.indexOf(uploadModule) != -1) {
					Transaction txn = Transaction.findById(referenceId);
					txn.setSupportingDocs(UploadUtil.removeSpecificUrl(txn.getSupportingDocs(), fileUrl));
					genericDAO.saveOrUpdate(txn, user, entityManager);
				} else if (IdosConstants.PJE_TXN_TYPE.indexOf(uploadModule) != -1) {
					IdosProvisionJournalEntry txn = IdosProvisionJournalEntry.findById(referenceId);
					txn.setSupportingDocuments(UploadUtil.removeSpecificUrl(txn.getSupportingDocuments(), fileUrl));
					genericDAO.saveOrUpdate(txn, user, entityManager);
				} else if (IdosConstants.CLAIM_TXN_TYPE.indexOf(uploadModule) != -1) {
					ClaimTransaction txn = ClaimTransaction.findById(referenceId);
					txn.setSupportingDocuments(UploadUtil.removeSpecificUrl(txn.getSupportingDocuments(), fileUrl));
					genericDAO.saveOrUpdate(txn, user, entityManager);
				} else if (IdosConstants.OUTPUT_TAX_OTHER.indexOf(uploadModule) != -1) {
					PayrollTransaction txn = PayrollTransaction.findById(referenceId);
					txn.setSupportingDocs(UploadUtil.removeSpecificUrl(txn.getSupportingDocs(), fileUrl));
					genericDAO.saveOrUpdate(txn, user, entityManager);
				}
				List<IdosUploadFilesLogs> filesLogs = IdosUploadFilesLogs.findByFileUrl(entityManager,
						user.getOrganization().getId(), fileUrl);
				if (filesLogs.size() > 0) {
					filesLogs.get(0).setPresentStatus(0);
					genericDAO.saveOrUpdate(filesLogs.get(0), user, entityManager);
				}
				entitytransaction.commit();
			}
			if (fileUrl != null && fileName != null) {
				String url = FILE_UPLOAD_SERVICE.delete(fileUrl, fileName, user.getOrganization());
				if (url != null) {
					result.put("status", "success");
				}
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>>>>> End" + result);
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result saveUpdateFileUpload(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			user = getUserInfo(request);
			if (null == user) {
				return unauthorized(result);
			}
			JsonNode json = request.body().asJson();
			ArrayNode an = result.putArray("resultTxn");
			entitytransaction.begin();
			FILE_UPLOAD_SERVICE.saveUpdateTxnUpload(entityManager, json, an, user);
			result.put("result", true);
			entitytransaction.commit();
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			result.put("result", false);
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result deleteUncommittedFiles(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			user = getUserInfo(request);
			if (null == user) {
				return unauthorized(result);
			}
			JsonNode json = request.body().asJson();
			ArrayNode an = result.putArray("resultTxn");
			entitytransaction.begin();
			FILE_UPLOAD_SERVICE.deleteUncommittedFiles(entityManager, json, an, user);
			result.put("result", true);
			entitytransaction.commit();
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			result.put("result", false);
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result serveFile(Request request, String fileName) {
		log.log(Level.FINE, ">>>> Start serveFile : " + fileName);
		Users user = getUserInfo(request);
		if (user == null) {
			log.log(Level.SEVERE, "unauthorized access");
			return unauthorized();
		}

		String filePath = application.path().toString() + "/public/images/companylogo/" + fileName;
		File file = new File(filePath);

		try {
			if (file.exists()) {
				Path path = Paths.get(file.getAbsolutePath());
				byte[] fileContent = Files.readAllBytes(path);
				Base64 codec = new Base64();
				String encoded = codec.encodeBase64String(fileContent);
	
				return Results.ok(encoded).as("image/png");
			} else {
				return notFound("File not found");
			}
		} catch (IOException e) {
				log.log(Level.SEVERE, "Error reading file: " + fileName, e);
				return internalServerError("Error reading file");
		}

	}
}
