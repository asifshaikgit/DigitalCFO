package controllers;

import java.io.File;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import akka.NotUsed;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.SingletonDBConnection;
import java.util.logging.Level;
import model.Users;
import com.typesafe.config.Config;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.Results;
import views.html.errorPage;
import play.Application;
import javax.inject.Inject;
import akka.stream.javadsl.*;
import akka.actor.*;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.libs.Files.TemporaryFile;
//import play.mvc.Http.Session;

public class TransactionExcelController extends StaticController {
	private final Application application;
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	// private Request request;
	// private Http.Session session = request.session();
	@Inject
	public TransactionExcelController(JPAApi jpaApi, Application application) {
		super(application);
		this.application = application;
		this.jpaApi = jpaApi;
		entityManager = EntityManagerProvider.getEntityManager();
	}

	/**
	 * Added by Firdous on 27-12--2017
	 * Method to download transaction template
	 * 
	 * @return
	 */
	@Transactional
	public Result downloadTransactionTemplate(Request request) {
		log.log(Level.FINE, ">>>> Start inside download transaction Template");
		// EntityManager entityManager = getEntityManager();
		File file = null;
		Users user = null;
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			String orgName = user.getOrganization().getName().replaceAll("\\s", "");
			String fileName = orgName + "_Sell_On_Credit_Template.xlsx";
			String sheetName = "SellOnCreditTransaction";
			String path = application.path().toString() + "/logs/OrgTransactions/";
			File filepath = new File(path);
			if (!filepath.exists()) {
				filepath.mkdir();
			}
			path = path + fileName;
			excelService.createTransactionTemplateExcel(user, entityManager, path, sheetName);
			/* result.withHeader("ContentType","application/xlsx"); */
			file = new File(path);
			return Results.ok(file).withHeader("Content-Disposition", "attachment; filename=" + fileName);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
	}

	/**
	 * Added by Firdous on 27-12--2017
	 * Method to download transaction template
	 * 
	 * @return
	 */
	@Transactional
	public Result downloadSellOnCashTransactionTemplate(Request request) {
		log.log(Level.FINE, ">>>> Start inside download sell on cash  transaction Template");
		// EntityManager entityManager = getEntityManager();
		File file = null;
		Users user = null;
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			String orgName = user.getOrganization().getName().replaceAll("\\s", "");
			String fileName = orgName + "_Sell_On_Cash_Template.xlsx";
			String sheetName = "SellOnCashTransaction";
			String path = application.path().toString() + "/logs/OrgTransactions/";
			File filepath = new File(path);
			if (!filepath.exists()) {
				filepath.mkdir();
			}
			path = path + fileName;
			excelService.createSellOnCashTransactionTemplateExcel(user, entityManager, path, sheetName);
			/* result.withHeader("ContentType","application/xlsx"); */
			file = new File(path);
			return Results.ok(file).withHeader("Content-Disposition", "attachment; filename=" + fileName);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
	}

	/**
	 * Added by Firdous on 03-01-2018
	 * Method to download transaction template
	 * 
	 * @return
	 */

	@Transactional
	public Result downloadRcvPayFromCustomerTransactionTemplate(Request request) {
		log.log(Level.FINE, ">>>> Start inside download receive payment from customer transaction Template");
		// EntityManager entityManager = getEntityManager();
		File file = null;
		Users user = null;
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			String orgName = user.getOrganization().getName().replaceAll("\\s", "");
			String fileName = orgName + "_RecievePayment_From_Customer_Template.xlsx";
			String sheetName = "RecievePaymentFromCustomerTransaction";
			String path = application.path().toString() + "/logs/OrgTransactions/";
			File filepath = new File(path);
			if (!filepath.exists()) {
				filepath.mkdir();
			}
			path = path + fileName;
			excelService.createRecievePaymentFromCustomerTransactionTemplateExcel(user, entityManager, path, sheetName);
			/* result.withHeader("ContentType","application/xlsx"); */
			file = new File(path);
			return Results.ok(file).withHeader("Content-Disposition", "attachment; filename=" + fileName);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
	}

	/**
	 * Added by Firdous on 03-01-2018
	 * Method to download transaction template
	 * 
	 * @return
	 */

	@Transactional
	public Result downloadBuyOnCreditTransactionTemplate(Request request) {
		log.log(Level.FINE, ">>>> Start inside download receive payment from customer transaction Template");
		// EntityManager entityManager = getEntityManager();
		File file = null;
		Users user = null;
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			String orgName = user.getOrganization().getName().replaceAll("\\s", "");
			String fileName = orgName + "_BuyOnCredit_Transaction_Template.xlsx";
			String sheetName = "BuyOnCreditTransaction";
			String path = application.path().toString() + "/logs/OrgTransactions/";
			File filepath = new File(path);
			if (!filepath.exists()) {
				filepath.mkdir();
			}
			path = path + fileName;
			excelService.createBuyOnCreditTransactionTemplateExcel(user, entityManager, path, sheetName);
			/* result.withHeader("ContentType","application/xlsx"); */
			file = new File(path);
			return Results.ok(file).withHeader("Content-Disposition", "attachment; filename=" + fileName);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
	}

	/**
	 * Added by Firdous on 20-03-2018
	 * Method to download transaction template
	 * 
	 * @return
	 */

	@Transactional
	public Result downloadBuyOnCashTransactionTemplate(Request request) {
		log.log(Level.FINE, ">>>> Start inside download receive payment from customer transaction Template");
		// EntityManager entityManager = getEntityManager();
		File file = null;
		Users user = null;
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			String orgName = user.getOrganization().getName().replaceAll("\\s", "");
			String fileName = orgName + "_BuyOnCash_Transaction_Template.xlsx";
			String sheetName = "BuyOnCashTransaction";
			String path = application.path().toString() + "/logs/OrgTransactions/";
			File filepath = new File(path);
			if (!filepath.exists()) {
				filepath.mkdir();
			}
			path = path + fileName;
			excelService.createBuyOnCashTransactionTemplateExcel(user, entityManager, path, sheetName);
			/* result.withHeader("ContentType","application/xlsx"); */
			file = new File(path);
			return Results.ok(file).withHeader("Content-Disposition", "attachment; filename=" + fileName);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
	}

	/**
	 * Added by Firdous on 23-04-2018
	 * Method to download transaction template
	 * 
	 * @return
	 */

	@Transactional
	public Result downloadPayVendorTransactionTemplate(Request request) {
		log.log(Level.FINE, ">>>> Start inside download pay vendor transaction Template");
		// EntityManager entityManager = getEntityManager();
		File file = null;
		Users user = null;
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			String orgName = user.getOrganization().getName().replaceAll("\\s", "");
			String fileName = orgName + "_Pay_Vendor_Template.xlsx";
			String sheetName = "PayVendorTransaction";
			String path = application.path().toString() + "/logs/OrgTransactions/";
			File filepath = new File(path);
			if (!filepath.exists()) {
				filepath.mkdir();
			}
			path = path + fileName;
			excelService.createPayVendorTransactionTemplateExcel(user, entityManager, path, sheetName);
			/* result.withHeader("ContentType","application/xlsx"); */
			file = new File(path);
			return Results.ok(file).withHeader("Content-Disposition", "attachment; filename=" + fileName);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
	}

	@Transactional
	public Result uploadSellTrans(Request request) {
		log.log(Level.FINE, ">>>> Start");
		Map<String, Object> criterias = new HashMap<String, Object>();
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		ArrayNode an = result.putArray("successUploading");
		DataFormatter df = new DataFormatter();
		Connection conn = null;
		CallableStatement stmt = null;
		Users user = null;
		long totalRowsInserted = 0;
		ResultSet rs = null;
		try {
			transaction.begin();
			MultipartFormData<File> body = request.body().asMultipartFormData();
			user = getUserInfo(request);
			List<FilePart<File>> chartofaccount = body.getFiles();
			for (FilePart<File> filePart : chartofaccount) {
				String fileName = filePart.getFilename();
				String contentType = filePart.getContentType();
				//File file = filePart.getRef();
				TemporaryFile temporaryFile = (TemporaryFile) filePart.getRef();
				String filePath = temporaryFile.path().toString();
				File file = new File(filePath);

				String path1 = file.getAbsolutePath();
				String path2 = file.getCanonicalPath();
				log.log(Level.INFO, "path1=" + path1);
				log.log(Level.INFO, "path2=" + path2);
				// conn =DatabaseConnection.getConnection();
				conn = SingletonDBConnection.getInstance().getConnInst();
				Statement statement1 = conn.createStatement();
				statement1.execute("truncate table temp_sell_on_credit_trans");
				// Statement statement = conn.createStatement();
				String path = application.path().toString() + "/logs/UploadExcel/";

				File filepath = new File(path);
				if (!filepath.exists()) {
					filepath.mkdir();
				}
				path = path + fileName;
				File file2 = new File(path);
				FileUtils.copyFile(file, file2);
				String rightPath = path.replace("\\", "/");
				String sql = "LOAD DATA LOCAL INFILE ? INTO TABLE temp_sell_on_credit_trans FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' IGNORE 1 LINES";
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setString(1, rightPath);
				statement.execute();
				// statement1.execute("select * from dbname.tablelog_tb");
				// ResultSet rs = statement1.getResultSet();
				// System.out.println("Row hostname and timestamp");
				// while(rs.next()) {
				// System.out.println(rs.getRow());}

				log.log(Level.FINE, "Inside insert_sellOnCredit_transactions, Got connection" + conn);
				String query = "{CALL insert_sellOnCredit_transactions(?1,?2)}";
				stmt = conn.prepareCall(query);
				stmt.setLong(1, user.getId());
				Long debugStoredPorc = 1l; // 1 means true, stored proc data will be logged into table LOG_STORED_PROC
				stmt.setLong(2, debugStoredPorc);

				rs = stmt.executeQuery();

				log.log(Level.FINE, "Inside Search,executed stored proce query");
				// InputStream is = new java.io.FileInputStream(file);
				result.put("totalRowsInserted", totalRowsInserted - 1);

				if (transaction != null && transaction.isActive()) {
					transaction.rollback();
				}

				/*
				 * try{
				 * XSSFWorkbook wb = new XSSFWorkbook(is);
				 * }catch (RuntimeException e) {
				 * if ( transaction != null && transaction.isActive()) {
				 * transaction.rollback();
				 * }
				 * throw e;
				 * } catch(Exception ex){
				 * log.log(Level.SEVERE, "Error", ex);
				 * ex.printStackTrace();
				 * }finally{
				 * result.put("totalRowsInserted",totalRowsInserted-1);
				 * log.log(Level.INFO, "Total rows inserted "+ totalRowsInserted);
				 * if ( transaction != null && transaction.isActive()) {
				 * transaction.rollback();
				 * }
				 * }
				 */
			}
			ObjectNode row = Json.newObject();
			row.put("message", "Uploaded Successfully");
			an.add(row);
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error", e);
			}
			;
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error", e);
			}
			;
			try {
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error", e);
			}
			;
		}
		return Results.ok(result);
	}

	@Transactional
	public Result uploadSellOnCashTrans(Request request) {
		log.log(Level.FINE, ">>>> Start");
		Map<String, Object> criterias = new HashMap<String, Object>();
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		ArrayNode an = result.putArray("successUploading");
		DataFormatter df = new DataFormatter();
		Connection conn = null;
		CallableStatement stmt = null;
		Users user = null;
		long totalRowsInserted = 0;
		ResultSet rs = null;
		try {
			transaction.begin();
			MultipartFormData<File> body = request.body().asMultipartFormData();
			user = getUserInfo(request);
			List<FilePart<File>> chartofaccount = body.getFiles();
			for (FilePart<File> filePart : chartofaccount) {
				String fileName = filePart.getFilename();
				String contentType = filePart.getContentType();
				//File file = filePart.getRef();
				TemporaryFile temporaryFile = (TemporaryFile) filePart.getRef();
				String filePath = temporaryFile.path().toString();
				File file = new File(filePath);

				String path1 = file.getAbsolutePath();
				String path2 = file.getCanonicalPath();
				log.log(Level.INFO, "path1=" + path1);
				log.log(Level.INFO, "path2=" + path2);
				// conn =DatabaseConnection.getConnection();
				conn = SingletonDBConnection.getInstance().getConnInst();
				Statement statement1 = conn.createStatement();
				statement1.execute("truncate table temp_sell_on_cash_trans");
				// Statement statement = conn.createStatement();
				String path = application.path().toString() + "/logs/UploadExcel/";

				File filepath = new File(path);
				if (!filepath.exists()) {
					filepath.mkdir();
				}
				path = path + fileName;
				File file2 = new File(path);
				FileUtils.copyFile(file, file2);
				String rightPath = path.replace("\\", "/");
				String sql = "LOAD DATA LOCAL INFILE ? INTO TABLE  temp_sell_on_cash_trans FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' IGNORE 1 LINES";
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setString(1, rightPath);
				statement.execute();
				// statement1.execute("select * from dbname.tablelog_tb");
				// ResultSet rs = statement1.getResultSet();
				// System.out.println("Row hostname and timestamp");
				// while(rs.next()) {
				// System.out.println(rs.getRow());}

				log.log(Level.FINE, "Inside insert_sellOnCash_transactions, Got connection" + conn);
				String query = "{CALL insert_sellOnCash_transactions(?1,?2)}";
				stmt = conn.prepareCall(query);
				stmt.setLong(1, user.getId());
				Long debugStoredPorc = 1l; // 1 means true, stored proc data will be logged into table LOG_STORED_PROC
				stmt.setLong(2, debugStoredPorc);

				rs = stmt.executeQuery();

				log.log(Level.FINE, "Inside Search,executed stored proce query");
				// InputStream is = new java.io.FileInputStream(file);
				result.put("totalRowsInserted", totalRowsInserted - 1);

				if (transaction != null && transaction.isActive()) {
					transaction.rollback();
				}

				/*
				 * try{
				 * XSSFWorkbook wb = new XSSFWorkbook(is);
				 * }catch (RuntimeException e) {
				 * if ( transaction != null && transaction.isActive()) {
				 * transaction.rollback();
				 * }
				 * throw e;
				 * } catch(Exception ex){
				 * log.log(Level.SEVERE, "Error", ex);
				 * ex.printStackTrace();
				 * }finally{
				 * result.put("totalRowsInserted",totalRowsInserted-1);
				 * log.log(Level.INFO, "Total rows inserted "+ totalRowsInserted);
				 * if ( transaction != null && transaction.isActive()) {
				 * transaction.rollback();
				 * }
				 * }
				 */
			}
			ObjectNode row = Json.newObject();
			row.put("message", "Uploaded Successfully");
			an.add(row);
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error", e);
			}
			;
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error", e);
			}
			;
			try {
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error", e);
			}
			;
		}
		return Results.ok(result);
	}

	@Transactional
	public Result uploadPayVendorTrans(Request request) {

		log.log(Level.INFO, ">>> inside upload pay vendor transaction");
		Map<String, Object> criterias = new HashMap<String, Object>();
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();

		ObjectNode result = Json.newObject();
		ArrayNode an = result.putArray("successUploading");
		DataFormatter df = new DataFormatter();
		Connection conn = null;
		CallableStatement stmt = null;
		Users user = null;
		long totalRowsInserted = 0;
		ResultSet rs = null;
		try {
			transaction.begin();
			MultipartFormData<File> body = request.body().asMultipartFormData();
			user = getUserInfo(request);
			List<FilePart<File>> chartofaccount = body.getFiles();
			for (FilePart<File> filePart : chartofaccount) {
				String fileName = filePart.getFilename();
				String contentType = filePart.getContentType();
				//File file = filePart.getRef();
				TemporaryFile temporaryFile = (TemporaryFile) filePart.getRef();
				String filePath = temporaryFile.path().toString();
				File file = new File(filePath);
				conn = SingletonDBConnection.getInstance().getConnInst();
				Statement statement1 = conn.createStatement();
				statement1.execute("truncate table temp_pay_vend_trans");
				// Statement statement = conn.createStatement();
				// String path=file.getPath();
				// String path=application.path().toString()+"/logs/UploadExcel/"+fileName;
				// log.log(Level.INFO, "path="+path);
				String path = application.path().toString() + "/logs/UploadExcel/";

				File filepath = new File(path);
				if (!filepath.exists()) {
					filepath.mkdir();
				}
				path = path + fileName;
				File file2 = new File(path);
				FileUtils.copyFile(file, file2);
				String rightPath = path.replace("\\", "/");
				// statement.execute( "LOAD DATA LOCAL INFILE 'F:/temp/"+fileName+"' INTO TABLE
				// temp_rec_payment_trans FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n'
				// IGNORE 1 LINES");
				String sql = "LOAD DATA LOCAL INFILE ? INTO TABLE  temp_pay_vend_trans FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' IGNORE 1 LINES";
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setString(1, rightPath);
				statement.execute();
				// stmt.execute("LOAD DATA LOCAL INFILE 'C:\\\\Documents and
				// Settings\\\\tofarmer\\\\My Documents\\\\testloadjava.txt' INTO TABLE
				// customerArticlePrices CHARACTER SET 'utf8' FIELDS TERMINATED BY ';' ENCLOSED
				// BY '\"' LINES TERMINATED BY '\\r\\n' (customerNumber, articleNumber,
				// customerPrice, changeDate, inputDate)" );
				// statement1.execute("select * from dbname.tablelog_tb");
				// ResultSet rs = statement1.getResultSet();
				// System.out.println("Row hostname and timestamp");
				// while(rs.next()) {
				// System.out.println(rs.getRow());}

				log.log(Level.FINE, "Inside insert_PayVendor_transactions, Got connection" + conn);
				String query = "{CALL insert_PayVendor_transactions(?1,?2)}";
				stmt = conn.prepareCall(query);
				stmt.setLong(1, user.getId());
				Long debugStoredPorc = 0l; // 1 means true, stored proc data will be logged into table LOG_STORED_PROC
				stmt.setLong(2, debugStoredPorc);

				rs = stmt.executeQuery();
				log.log(Level.FINE, "Inside Search,executed stored proce query");
				InputStream is = new java.io.FileInputStream(file);

				/*
				 * try{
				 * XSSFWorkbook wb = new XSSFWorkbook(is);
				 * }catch (RuntimeException e) {
				 * if ( transaction != null && transaction.isActive()) {
				 * transaction.rollback();
				 * }
				 * throw e;
				 * } catch(Exception ex){
				 * log.log(Level.SEVERE, "Error", ex);
				 * ex.printStackTrace();
				 * }finally{
				 * result.put("totalRowsInserted",totalRowsInserted-1);
				 * log.log(Level.INFO, "Total rows inserted "+ totalRowsInserted);
				 * if ( transaction != null && transaction.isActive()) {
				 * transaction.rollback();
				 * }
				 * }
				 */
			}
			ObjectNode row = Json.newObject();
			row.put("message", "Uploaded Successfully");
			an.add(row);
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error", e);
			}
			;
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error", e);
			}
			;
			try {
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error", e);
			}
			;
		}
		return Results.ok(result);
	}

	@Transactional
	public Result uploadBuyOnCreditTrans(Request request) {
		log.log(Level.FINE, ">>>> Start");
		Map<String, Object> criterias = new HashMap<String, Object>();
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		ArrayNode an = result.putArray("successUploading");
		DataFormatter df = new DataFormatter();
		Connection conn = null;
		CallableStatement stmt = null;
		Users user = null;
		long totalRowsInserted = 0;
		ResultSet rs = null;
		try {
			transaction.begin();
			MultipartFormData<File> body = request.body().asMultipartFormData();
			user = getUserInfo(request);
			List<FilePart<File>> chartofaccount = body.getFiles();
			for (FilePart<File> filePart : chartofaccount) {
				String fileName = filePart.getFilename();
				String contentType = filePart.getContentType();
				//File file = filePart.getRef();
				TemporaryFile temporaryFile = (TemporaryFile) filePart.getRef();
				String filePath = temporaryFile.path().toString();
				File file = new File(filePath);

				String path1 = file.getAbsolutePath();
				String path2 = file.getCanonicalPath();
				log.log(Level.INFO, "path1=" + path1);
				log.log(Level.INFO, "path2=" + path2);
				// conn =DatabaseConnection.getConnection();
				conn = SingletonDBConnection.getInstance().getConnInst();
				Statement statement1 = conn.createStatement();
				statement1.execute("truncate table temp_buy_on_credit_trans");
				// Statement statement = conn.createStatement();
				String path = application.path().toString() + "/logs/UploadExcel/";

				File filepath = new File(path);
				if (!filepath.exists()) {
					filepath.mkdir();
				}
				path = path + fileName;
				File file2 = new File(path);
				FileUtils.copyFile(file, file2);
				String rightPath = path.replace("\\", "/");
				String sql = "LOAD DATA LOCAL INFILE ? INTO TABLE  temp_buy_on_credit_trans FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' IGNORE 1 LINES";
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setString(1, rightPath);
				statement.execute();
				// statement1.execute("select * from dbname.tablelog_tb");
				// ResultSet rs = statement1.getResultSet();
				// System.out.println("Row hostname and timestamp");
				// while(rs.next()) {
				// System.out.println(rs.getRow());}

				log.log(Level.FINE, "Inside insert_buyOnCredit_transactions, Got connection" + conn);
				String query = "{CALL insert_buyOnCredit_transactions(?2,?3)}";
				stmt = conn.prepareCall(query);
				stmt.setLong(1, user.getId());
				Long debugStoredPorc = 1l; // 1 means true, stored proc data will be logged into table LOG_STORED_PROC
				stmt.setLong(2, debugStoredPorc);

				rs = stmt.executeQuery();

				log.log(Level.FINE, "Inside Search,executed stored proce query");
				// InputStream is = new java.io.FileInputStream(file);
				result.put("totalRowsInserted", totalRowsInserted - 1);

				if (transaction != null && transaction.isActive()) {
					transaction.rollback();
				}

				/*
				 * try{
				 * XSSFWorkbook wb = new XSSFWorkbook(is);
				 * }catch (RuntimeException e) {
				 * if ( transaction != null && transaction.isActive()) {
				 * transaction.rollback();
				 * }
				 * throw e;
				 * } catch(Exception ex){
				 * log.log(Level.SEVERE, "Error", ex);
				 * ex.printStackTrace();
				 * }finally{
				 * result.put("totalRowsInserted",totalRowsInserted-1);
				 * log.log(Level.INFO, "Total rows inserted "+ totalRowsInserted);
				 * if ( transaction != null && transaction.isActive()) {
				 * transaction.rollback();
				 * }
				 * }
				 */
			}
			ObjectNode row = Json.newObject();
			row.put("message", "Uploaded Successfully");
			an.add(row);
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error", e);
			}
			;
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error", e);
			}
			;
			try {
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error", e);
			}
			;
		}
		return Results.ok(result);
	}

	@Transactional
	public Result uploadBuyOnCashTrans(Request request) {
		log.log(Level.FINE, ">>>> Start");
		Map<String, Object> criterias = new HashMap<String, Object>();
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		ArrayNode an = result.putArray("successUploading");
		DataFormatter df = new DataFormatter();
		Connection conn = null;
		CallableStatement stmt = null;
		Users user = null;
		long totalRowsInserted = 0;
		ResultSet rs = null;
		try {
			transaction.begin();
			MultipartFormData<File> body = request.body().asMultipartFormData();
			user = getUserInfo(request);
			List<FilePart<File>> chartofaccount = body.getFiles();
			for (FilePart<File> filePart : chartofaccount) {
				String fileName = filePart.getFilename();
				String contentType = filePart.getContentType();
				//File file = filePart.getRef();
				TemporaryFile temporaryFile = (TemporaryFile) filePart.getRef();
				String filePath = temporaryFile.path().toString();
				File file = new File(filePath);

				String path1 = file.getAbsolutePath();
				String path2 = file.getCanonicalPath();
				log.log(Level.INFO, "path1=" + path1);
				log.log(Level.INFO, "path2=" + path2);
				// conn =DatabaseConnection.getConnection();
				conn = SingletonDBConnection.getInstance().getConnInst();
				Statement statement1 = conn.createStatement();
				statement1.execute("truncate table temp_buy_on_cash_trans");
				// Statement statement = conn.createStatement();
				String path = application.path().toString() + "/logs/UploadExcel/";

				File filepath = new File(path);
				if (!filepath.exists()) {
					filepath.mkdir();
				}
				path = path + fileName;
				File file2 = new File(path);
				FileUtils.copyFile(file, file2);
				String rightPath = path.replace("\\", "/");
				String sql = "LOAD DATA LOCAL INFILE ? INTO TABLE  temp_buy_on_cash_trans FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' IGNORE 1 LINES";
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setString(1, rightPath);
				statement.execute();
				// statement1.execute("select * from dbname.tablelog_tb");
				// ResultSet rs = statement1.getResultSet();
				// System.out.println("Row hostname and timestamp");
				// while(rs.next()) {
				// System.out.println(rs.getRow());}

				log.log(Level.FINE, "Inside insert_buyOnCash_transactions, Got connection" + conn);
				String query = "{CALL insert_buyOnCash_transactions(?1,?2)}";
				stmt = conn.prepareCall(query);
				stmt.setLong(1, user.getId());
				Long debugStoredPorc = 1l; // 1 means true, stored proc data will be logged into table LOG_STORED_PROC
				stmt.setLong(2, debugStoredPorc);

				rs = stmt.executeQuery();

				log.log(Level.FINE, "Inside Search,executed stored proce query");
				// InputStream is = new java.io.FileInputStream(file);
				result.put("totalRowsInserted", totalRowsInserted - 1);

				if (transaction != null && transaction.isActive()) {
					transaction.rollback();
				}

				/*
				 * try{
				 * XSSFWorkbook wb = new XSSFWorkbook(is);
				 * }catch (RuntimeException e) {
				 * if ( transaction != null && transaction.isActive()) {
				 * transaction.rollback();
				 * }
				 * throw e;
				 * } catch(Exception ex){
				 * log.log(Level.SEVERE, "Error", ex);
				 * ex.printStackTrace();
				 * }finally{
				 * result.put("totalRowsInserted",totalRowsInserted-1);
				 * log.log(Level.INFO, "Total rows inserted "+ totalRowsInserted);
				 * if ( transaction != null && transaction.isActive()) {
				 * transaction.rollback();
				 * }
				 * }
				 */
			}
			ObjectNode row = Json.newObject();
			row.put("message", "Uploaded Successfully");
			an.add(row);
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error", e);
			}
			;
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error", e);
			}
			;
			try {
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error", e);
			}
			;
		}
		return Results.ok(result);
	}

	@Transactional
	public Result uploadRecPayTrans(Request request) {
		log.log(Level.FINE, ">>>> Start");
		Map<String, Object> criterias = new HashMap<String, Object>();
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();

		ObjectNode result = Json.newObject();
		ArrayNode an = result.putArray("successUploading");
		DataFormatter df = new DataFormatter();
		Connection conn = null;
		CallableStatement stmt = null;
		Users user = null;
		long totalRowsInserted = 0;
		ResultSet rs = null;
		try {
			transaction.begin();
			MultipartFormData<File> body = request.body().asMultipartFormData();
			user = getUserInfo(request);
			List<FilePart<File>> chartofaccount = body.getFiles();
			for (FilePart<File> filePart : chartofaccount) {
				String fileName = filePart.getFilename();
				String contentType = filePart.getContentType();
				//File file = filePart.getRef();
				TemporaryFile temporaryFile = (TemporaryFile) filePart.getRef();
				String filePath = temporaryFile.path().toString();
				File file = new File(filePath);
				conn = SingletonDBConnection.getInstance().getConnInst();
				Statement statement1 = conn.createStatement();
				statement1.execute("truncate table temp_rec_payment_trans");
				// Statement statement = conn.createStatement();
				// String path=file.getPath();
				// String path=application.path().toString()+"/logs/UploadExcel/"+fileName;
				// log.log(Level.INFO, "path="+path);
				String path = application.path().toString() + "/logs/UploadExcel/";

				File filepath = new File(path);
				if (!filepath.exists()) {
					filepath.mkdir();
				}
				path = path + fileName;
				File file2 = new File(path);
				FileUtils.copyFile(file, file2);
				String rightPath = path.replace("\\", "/");
				// statement.execute( "LOAD DATA LOCAL INFILE 'F:/temp/"+fileName+"' INTO TABLE
				// temp_rec_payment_trans FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n'
				// IGNORE 1 LINES");
				String sql = "LOAD DATA LOCAL INFILE ? INTO TABLE  temp_rec_payment_trans FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' IGNORE 1 LINES";
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setString(1, rightPath);
				statement.execute();
				// stmt.execute("LOAD DATA LOCAL INFILE 'C:\\\\Documents and
				// Settings\\\\tofarmer\\\\My Documents\\\\testloadjava.txt' INTO TABLE
				// customerArticlePrices CHARACTER SET 'utf8' FIELDS TERMINATED BY ';' ENCLOSED
				// BY '\"' LINES TERMINATED BY '\\r\\n' (customerNumber, articleNumber,
				// customerPrice, changeDate, inputDate)" );
				// statement1.execute("select * from dbname.tablelog_tb");
				// ResultSet rs = statement1.getResultSet();
				// System.out.println("Row hostname and timestamp");
				// while(rs.next()) {
				// System.out.println(rs.getRow());}

				log.log(Level.FINE, "Inside insert_receivePay_transactions, Got connection" + conn);
				String query = "{CALL insert_receivePay_transactions(?1,?2)}";
				stmt = conn.prepareCall(query);
				stmt.setLong(1, user.getId());
				Long debugStoredPorc = 0l; // 1 means true, stored proc data will be logged into table LOG_STORED_PROC
				stmt.setLong(2, debugStoredPorc);

				rs = stmt.executeQuery();
				log.log(Level.FINE, "Inside Search,executed stored proce query");
				InputStream is = new java.io.FileInputStream(file);

				/*
				 * try{
				 * XSSFWorkbook wb = new XSSFWorkbook(is);
				 * }catch (RuntimeException e) {
				 * if ( transaction != null && transaction.isActive()) {
				 * transaction.rollback();
				 * }
				 * throw e;
				 * } catch(Exception ex){
				 * log.log(Level.SEVERE, "Error", ex);
				 * ex.printStackTrace();
				 * }finally{
				 * result.put("totalRowsInserted",totalRowsInserted-1);
				 * log.log(Level.INFO, "Total rows inserted "+ totalRowsInserted);
				 * if ( transaction != null && transaction.isActive()) {
				 * transaction.rollback();
				 * }
				 * }
				 */
			}
			ObjectNode row = Json.newObject();
			row.put("message", "Uploaded Successfully");
			an.add(row);
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error", e);
			}
			;
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error", e);
			}
			;
			try {
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error", e);
			}
			;
		}
		return Results.ok(result);
	}
	/*
	 * @Transactional
	 * public Result downloadTransactionDataInExcel(){
	 * log.log(Level.FINE, ">>>> Start inside download transaction data");
	 * // EntityManager entityManager = getEntityManager();
	 * ObjectNode result = Json.newObject();
	 * File file = null;
	 * 
	 * String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
	 * Users user = null;
	 * if (ipAddress == null) {
	 * ipAddress = request.remoteAddress();
	 * }
	 * try {
	 * Map<String, Object> criterias = new HashMap<String, Object>();
	 * JsonNode json = request.body().asJson();
	 * String useremail = json.findValue("useremail").asText();
	 * String fromDate=json.findValue("fromDate").asText();
	 * String toDate=json.findValue("toDate").asText();
	 * 
	 * session.adding("email", useremail);
	 * DataFormatter df = new DataFormatter();
	 * 
	 * user=getUserInfo(request);
	 * if(user == null){
	 * return unauthorized();
	 * }
	 * Date fromTransDate=null;
	 * Date toTransDate=null;
	 * 
	 * String orgName = user.getOrganization().getName().replaceAll("\\s", "");
	 * String fileName = orgName + "_Transaction_Data.xlsx";
	 * String sheetName ="Outwards Supply";
	 * try {
	 * if (fromDate != null && !fromDate.equals("")) {
	 * fromTransDate=IdosConstants.mysqldf.parse(IdosConstants.mysqldf.format(
	 * IdosConstants.idosdf.parse(fromDate)));
	 * }
	 * if (toDate != null && !toDate.equals("")) {
	 * toTransDate=IdosConstants.mysqldf.parse(IdosConstants.mysqldf.format(
	 * IdosConstants.idosdf.parse(toDate)));
	 * }
	 * }catch (java.text.ParseException ex){
	 * log.log(Level.SEVERE, "Date cannot be parsed", ex);
	 * throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
	 * IdosConstants.TECHNICAL_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
	 * "Date cannot be parsed");
	 * }
	 * String path = application.path().toString() + "/logs/OrgTransData/";
	 * File filepath = new File(path);
	 * if (!filepath.exists()) {
	 * filepath.mkdir();
	 * }
	 * path = path + fileName;
	 * excelService.createOrgTransactionTemplateExcel(user,entityManager, path,
	 * sheetName,fromTransDate,toTransDate);
	 * result.withHeader("ContentType","application/xlsx");
	 * response().setHeader("Content-Disposition",
	 * "attachment; filename="+fileName);
	 * file = new File(path);
	 * }catch(Exception ex){
	 * log.log(Level.SEVERE, "Error", ex);
	 * String strBuff=getStackTraceMessage(ex);
	 * expService.sendExceptionReport(strBuff,user.getEmail(),
	 * user.getOrganization().getName(),
	 * Thread.currentThread().getStackTrace()[1].getMethodName());
	 * List<String> errorList=getStackTrace(ex);
	 * return Results.ok(errorPage.render(ex,errorList));
	 * }
	 * return Results.ok(result);
	 * }
	 */
	/*
	 * @Transactional
	 * public Result uploadTransactions(){
	 * log.log(Level.FINE, ">>>> Start uploading transactions");
	 * Map<String, Object> criterias=new HashMap<String, Object>();
	 * // EntityManager entityManager=getEntityManager();
	 * EntityTransaction transaction=entityManager.getTransaction();
	 * ObjectNode result = Json.newObject();
	 * ArrayNode an = result.putArray("successUploading");
	 * DataFormatter df = new DataFormatter();
	 * Users user=null;
	 * int batchSize = 25;
	 * int rowCount=0;
	 * long totalRowsInserted=0;
	 * int lastrownum=0;
	 * try{
	 * transaction.begin();
	 * MultipartFormData body = request.body().asMultipartFormData();
	 * user=getUserInfo(request);
	 * List<FilePart> bulkTransaction = body.getFiles();
	 * for(FilePart filePart:bulkTransaction){
	 * String fileName = filePart.getFilename();
	 * String contentType = filePart.getContentType();
	 * File file = filePart.getFile();
	 * InputStream is = new java.io.FileInputStream(file);
	 * try{
	 * XSSFWorkbook wb = new XSSFWorkbook(is);
	 * int numOfSheets = wb.getNumberOfSheets();
	 * TransactionBulk transactionBulk=new TransactionBulk();
	 * genericDAO.save(transactionBulk,user,entityManager);
	 * for (int i = 0; i < numOfSheets; i++) {
	 * XSSFSheet sheet = wb.getSheetAt(i);
	 * if(!"SellOnCreditTransaction".equalsIgnoreCase(sheet.getSheetName())) {
	 * log.log(Level.FINE, "no sheets available");
	 * continue;
	 * }
	 * lastrownum=sheet.getLastRowNum();
	 * result.put("totalRowsInXls",lastrownum);
	 * Iterator rows = sheet.rowIterator();
	 * while(rows.hasNext()) {
	 * rowCount++;totalRowsInserted++;
	 * if ( rowCount > 0 && rowCount % batchSize == 0 ) { //batch commit of 25
	 * entityManager.flush();
	 * entityManager.clear();
	 * transaction.commit();
	 * transaction.begin();
	 * rowCount=0;
	 * }
	 * XSSFRow row = (XSSFRow) rows.next();
	 * if (row.getRowNum() == 0) {
	 * continue;
	 * }
	 * String transactionSlNo=row.getCell(0)!=null?row.getCell(0).toString():null;
	 * String transactionDate=row.getCell(1) == null ? null :
	 * df.formatCellValue(((XSSFCell) row.getCell(1)));
	 * String txnBranchName=row.getCell(2)!=null?row.getCell(2).toString():null;
	 * String projectName=row.getCell(3)!=null?row.getCell(3).toString():null;
	 * String typeOfSupply=row.getCell(4)!=null?row.getCell(4).toString():null;
	 * String withOrWithoutGST = row.getCell(5) == null ? null : ((XSSFCell)
	 * row.getCell(5)).toString();
	 * String customerCode = row.getCell(6) == null ? null :
	 * df.formatCellValue(((XSSFCell) row.getCell(6)));
	 * String placeOfSupply = row.getCell(7) == null ? null : ((XSSFCell)
	 * row.getCell(7)).toString();
	 * String poReference = row.getCell(8) == null ? null : ((XSSFCell)
	 * row.getCell(8)).toString();
	 * String itemCode =row.getCell(9) == null ? null :
	 * df.formatCellValue(((XSSFCell) row.getCell(9)));
	 * String price = row.getCell(10) == null ? null :
	 * df.formatCellValue(((XSSFCell) row.getCell(10)));
	 * String units = row.getCell(11) == null ? null :
	 * df.formatCellValue(((XSSFCell) row.getCell(11)));
	 * String discountPercent = row.getCell(12) == null ? null :
	 * df.formatCellValue(((XSSFCell) row.getCell(12)));
	 * String advanceAdjustment = row.getCell(13) == null ? null :
	 * df.formatCellValue(((XSSFCell) row.getCell(13)));
	 * String transactionNotes = row.getCell(14) == null ? null : ((XSSFCell)
	 * row.getCell(14)).toString();
	 * Long txnPurposeVal=IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER;
	 * TransactionPurpose usertxnPurpose =
	 * TransactionPurpose.findById(txnPurposeVal);
	 * Transaction myTransaction = new Transaction();
	 * if (IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER == txnPurposeVal) {
	 * myTransaction.setPaymentStatus("NOT-PAID");
	 * transaction.setPerformaInvoice(performaInvoice);
	 * transaction.setCustomerDuePayment(txnnetamount);
	 * }
	 * 
	 * 
	 * 
	 * // JSONArray arrJSON = new JSONArray(itemCode);
	 * // JSONObject firstRowItemData = new JSONObject(itemCode);
	 * // Long itemIdRow0 = firstRowItemData.getLong("txnItems");
	 * // Specifics txnSpecificItem = genericDAO.getById(Specifics.class,
	 * itemIdRow0, entityManager);
	 * // Double txnPerUnitPriceRow0 =
	 * firstRowItemData.getDouble("txnPerUnitPrice");
	 * // Double txnNoOfUniRow0t = firstRowItemData.getDouble("txnNoOfUnit");
	 * // Double txnGrossRow0 = firstRowItemData.getDouble("txnGross");
	 * criterias.put("name", itemCode);
	 * Specifics txnSpecificItem=genericDAO.getByCriteria(Specifics.class,
	 * criterias, entityManager);
	 * myTransaction.setTransactionParticulars(txnSpecificItem.getParticularsId());
	 * myTransaction.setNoOfUnits(Double.parseDouble(units));
	 * myTransaction.setPricePerUnit(Double.parseDouble(price));
	 * myTransaction.setGrossAmount(txnGrossRow0);
	 * transaction.setSourceGstin(txnSourceGstin);
	 * transaction.setDestinationGstin(txnDestinGstin);
	 * criterias.put("name",txnBranchName);
	 * Branch txnBranch = genericDAO.getByCriteria(Branch.class, criterias,
	 * entityManager);
	 * criterias.clear();
	 * criterias.put("name",projectName);
	 * Project txnProject = genericDAO.getByCriteria(Project.class, criterias,
	 * entityManager);
	 * criterias.clear();
	 * criterias.put("name",customerCode);
	 * Vendor txnCustomer = genericDAO.getByCriteria(Vendor.class, criterias,
	 * entityManager);
	 * if(typeOfSupply.equals("Regular Supply")){
	 * myTransaction.setTypeOfSupply(1);
	 * }
	 * else if(typeOfSupply.equals("Supply applicable for Reverse Charge")){
	 * myTransaction.setTypeOfSupply(2);
	 * }
	 * else if(typeOfSupply.equals("This is an Export Supply")){
	 * myTransaction.setTypeOfSupply(3);
	 * }
	 * else if(typeOfSupply.equals("This is supply to SEZ Unit or SEZ Developer")){
	 * myTransaction.setTypeOfSupply(4);
	 * }
	 * else if(typeOfSupply.equals("This is deemed Export Supply")){
	 * myTransaction.setTypeOfSupply(5);
	 * }
	 * else if(typeOfSupply.equals("Supply made through E-commerce Operator")){
	 * myTransaction.setTypeOfSupply(6);
	 * }
	 * else if(typeOfSupply.equals("Bill of Supply")){
	 * myTransaction.setTypeOfSupply(7);
	 * }
	 * if(withOrWithoutGST.equals("On payment of IGST")){
	 * myTransaction.setWithWithoutTax(1);
	 * }
	 * else if(withOrWithoutGST.equals("Under Bond / LUT without payment of IGST")){
	 * myTransaction.setWithWithoutTax(2);
	 * }
	 * transaction.setWithWithoutTax(txnWithWithoutTax);
	 * transaction.setWalkinCustomerType(txnWalkinCustomerType);
	 * myTransaction.setTransactionPurpose(usertxnPurpose);
	 * myTransaction.setTransactionBranch(txnBranch);
	 * myTransaction.setTransactionBranchOrganization(txnBranch.getOrganization());
	 * myTransaction.setTransactionProject(txnProject);
	 * myTransaction.setTransactionVendorCustomer(txnCustomer);
	 * //transaction.setTransactionUnavailableVendorCustomer(
	 * txnforunavailablecustomer);
	 * 
	 * myTransaction.setPoReference(poReference);
	 * 
	 * Specifics specifics = Specifics.findById(txnSpecificItem.getId());
	 * boolean isGstTaxApplicable = true;
	 * if("1".equals(specifics.getGstItemCategory()) ||
	 * "2".equals(specifics.getGstItemCategory()) ||
	 * "3".equals(specifics.getGstItemCategory())){
	 * isGstTaxApplicable = false;
	 * 
	 * }
	 * 
	 * // transaction.setNetAmount(txnnetamount);
	 * // Double roundedCutPartOfNetAmount = txnnetamount -
	 * netAmountTotalWithDecimalValue;
	 * // transaction.setRoundedCutPartOfNetAmount(roundedCutPartOfNetAmount);
	 * try {
	 * if (transactionDate != null && !transactionDate.equals("")) {
	 * myTransaction.setTransactionDate(IdosConstants.mysqldf.parse(IdosConstants.
	 * mysqldf.format(IdosConstants.idosdf.parse(transactionDate))));
	 * }
	 * 
	 * }catch (java.text.ParseException ex){
	 * log.log(Level.SEVERE, "Date cannot be parsed", ex);
	 * throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
	 * IdosConstants.TECHNICAL_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
	 * "Date cannot be parsed");
	 * }
	 * 
	 * // myTransaction.setTransactionDate();
	 * // transaction.setReceiptDetailsType(txnreceiptdetails);
	 * // transaction.setReceiptDetailsDescription(txnreceipttypebankdetails);
	 * // if (IdosConstants.PAYMODE_CASH == txnreceiptdetails) {
	 * // paymentMode = "CASH";
	 * // } else if (IdosConstants.PAYMODE_BANK == txnreceiptdetails) {
	 * // paymentMode = "BANK";
	 * // }
	 * // if (!txnremarks.equals("") && txnremarks != null) {
	 * // txnRemarks = user.getEmail() + "#" + txnremarks;
	 * // transaction.setRemarks(txnRemarks);
	 * // txnRemarks = transaction.getRemarks();
	 * // }
	 * if (!supportingdoc.equals("") && supportingdoc != null) {
	 * String suppdocarr[] = supportingdoc.split(",");
	 * for (int i = 0; i < suppdocarr.length; i++) {
	 * if (txnDocument.equals("")) {
	 * txnDocument += user.getEmail() + "#" + suppdocarr[i];
	 * } else {
	 * txnDocument += "," + user.getEmail() + "#" + suppdocarr[i];
	 * }
	 * }
	 * transaction.setSupportingDocs(txnDocument);
	 * }
	 * myTransaction.setTransactionStatus("Accounted");
	 * String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
	 * myTransaction.setTransactionRefNumber(transactionNumber);
	 * Double netAmount=Double.parseDouble(units)*Double.parseDouble(price);
	 * myTransaction.setNetAmount(netAmount);
	 * DecimalFormat df1 = new DecimalFormat("###.##");
	 * if(specifics.getIsCombinationSales()!=null &&
	 * specifics.getIsCombinationSales()==1){
	 * Long coaId = specifics.getId();
	 * StringBuilder newsbquery = new StringBuilder(); //specificId = laptop and
	 * combSpecificId = RAM, Monitor etc
	 * newsbquery.
	 * append("select obj from SpecificsCombinationSales obj WHERE obj.specificsId.id = '"
	 * +coaId+"' and obj.organization.id ='"+user.getOrganization().getId()+"'");
	 * List<SpecificsCombinationSales>
	 * specificsList=genericDAO.executeSimpleQuery(newsbquery.toString(),
	 * entityManager);
	 * ArrayList<Double> taxamt = new ArrayList<Double>();
	 * taxamt.add(0, 0.0); //SGST
	 * taxamt.add(1, 0.0);
	 * taxamt.add(2, 0.0);
	 * for(SpecificsCombinationSales combSpec:specificsList){
	 * double openBalUnits = Integer.parseInt(units) *
	 * combSpec.getOpeningBalUnits();
	 * double grossAmtForThisItem = openBalUnits * combSpec.getOpeningBalRate();
	 * criterias.clear();
	 * criterias.put("branch.id",txnBranch.getId());
	 * criterias.put("organization.id", user.getOrganization().getId());
	 * criterias.put("specifics.id", combSpec.getCombSpecificsId().getId());
	 * criterias.put("presentStatus", 1);
	 * List<BranchSpecificsTaxFormula> bnchSpecfTaxFormula =
	 * genericDAO.findByCriteria(BranchSpecificsTaxFormula.class, criterias,
	 * entityManager);
	 * Specifics specificChildItem = combSpec.getCombSpecificsId();
	 * ObjectNode row1 = Json.newObject();
	 * getNetAmountTaxComponentForCombinationSell(txnSourceGstinCode,
	 * txnDestGstinCode,txnTypeOfSupply,txnWithWithoutTax,specificChildItem,
	 * bnchSpecfTaxFormula,grossAmtForThisItem,row,taxamt);
	 * }
	 * //get branchTaxes to get taxId
	 * Long sgstTaxId=null,cgstTaxId=null,igstTaxId=null;
	 * StringBuilder strSQL = new
	 * StringBuilder("select obj from BranchTaxes obj where obj.organization=").
	 * append(user.getOrganization().getId());
	 * strSQL.append(" and obj.branch=").append(txnBranch.getId());
	 * strSQL.append(" and obj.taxType in (20,21,22)");
	 * List<BranchTaxes> taxList = genericDAO.executeSimpleQuery(strSQL.toString(),
	 * entityManager);
	 * for(BranchTaxes bnchTax: taxList) {
	 * if(bnchTax.getTaxName().contains("SGST")){
	 * sgstTaxId = bnchTax.getId();
	 * }else if(bnchTax.getTaxName().contains("CGST")){
	 * cgstTaxId = bnchTax.getId();
	 * }else if(bnchTax.getTaxName().contains("IGST")){
	 * igstTaxId = bnchTax.getId();
	 * }
	 * }
	 * 
	 * double SGSTTaxAmt =taxamt.get(0)==null? 0.0:taxamt.get(0).doubleValue();
	 * if(SGSTTaxAmt>0){
	 * ObjectNode row1 = Json.newObject();
	 * row1.put("taxName", "SGST"); //SGST added for all subitems of combination
	 * sales
	 * row1.put("taxRate", 0.0);
	 * row1.put("taxid", sgstTaxId);
	 * row1.put("individualTax", "SGST(0.0%):" + df1.format(SGSTTaxAmt));
	 * row1.put("taxAmount", df1.format(SGSTTaxAmt));
	 * branchSpecificsTaxComponentan.add(row1);
	 * ObjectNode formularow = Json.newObject();
	 * formularow.put("individualTax", "SGST(0.0%):" + df1.format(SGSTTaxAmt));
	 * branchSpecificsTaxFormulaComponentan.add(formularow);
	 * 
	 * }
	 * double CGSTTaxAmt =taxamt.get(1)==null? 0.0:taxamt.get(1).doubleValue();
	 * if(CGSTTaxAmt>0){
	 * ObjectNode row2= Json.newObject();
	 * row2.put("taxName", "CGST");
	 * row2.put("taxRate", 0.0);
	 * row2.put("taxid", cgstTaxId);
	 * row2.put("individualTax", "CGST(0.0%):" + df1.format(CGSTTaxAmt));
	 * row2.put("taxAmount", df1.format(CGSTTaxAmt));
	 * branchSpecificsTaxComponentan.add(row2);
	 * ObjectNode formularow = Json.newObject();
	 * formularow.put("individualTax", "CGST(0.0%):" + df1.format(CGSTTaxAmt));
	 * branchSpecificsTaxFormulaComponentan.add(formularow);
	 * }
	 * double IGSTTaxAmt =taxamt.get(2)==null? 0.0:taxamt.get(2).doubleValue();
	 * if(IGSTTaxAmt>0){
	 * ObjectNode row3= Json.newObject();
	 * row3.put("taxName", "IGST");
	 * row3.put("taxRate", 0.0);
	 * row3.put("taxid", igstTaxId);
	 * row3.put("individualTax", "IGST(0.0%):" + df1.format(IGSTTaxAmt));
	 * row3.put("taxAmount", df.format(IGSTTaxAmt));
	 * branchSpecificsTaxComponentan.add(row3);
	 * ObjectNode formularow = Json.newObject();
	 * formularow.put("individualTax", "IGST(0.0%):" + df1.format(IGSTTaxAmt));
	 * branchSpecificsTaxFormulaComponentan.add(formularow);
	 * }
	 * double taxTotalAmount=SGSTTaxAmt+CGSTTaxAmt+IGSTTaxAmt;
	 * ObjectNode row = Json.newObject();
	 * row.put("taxTotalAmount", taxTotalAmount);
	 * branchSpecificsTaxResultAmountan.add(row);
	 * }else{
	 * criterias.clear();
	 * criterias.put("branch.id", Long.parseLong(txnBranchId));
	 * criterias.put("organization.id", user.getOrganization().getId());
	 * criterias.put("specifics.id", txnSpecificsId);
	 * criterias.put("presentStatus", 1);
	 * Double GV = Double.parseDouble(txnGrossAmt);
	 * List<BranchSpecificsTaxFormula> bnchSpecfTaxFormula =
	 * genericDAO.findByCriteria(BranchSpecificsTaxFormula.class, criterias,
	 * entityManager);
	 * Double Tax1 = 0.0;
	 * Double IV1 = 0.0;
	 * Double Tax2 = 0.0;
	 * Double IV2 = 0.0;
	 * Double Tax3 = 0.0;
	 * Double IV3 = 0.0;
	 * Double Tax4 = 0.0;
	 * Double IV4 = 0.0;
	 * Double Tax5 = 0.0;
	 * Double IV5 = 0.0;
	 * Double taxTotalAmount = 0.0;
	 * for (int i = 0; i < bnchSpecfTaxFormula.size(); i++) {
	 * int taxType = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxType();
	 * if (taxType == IdosConstants.OUTPUT_TAX || taxType ==
	 * IdosConstants.OUTPUT_SGST || taxType == IdosConstants.OUTPUT_CGST
	 * || taxType == IdosConstants.OUTPUT_IGST || taxType ==
	 * IdosConstants.OUTPUT_CESS) {
	 * ObjectNode row = Json.newObject();
	 * ObjectNode formularow = Json.newObject();
	 * String taxName = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxName();
	 * if(taxName != null) {
	 * if ((!isGstTaxApplicable) && (taxName.startsWith("SGST") ||
	 * taxName.startsWith("CGST") || taxName.startsWith("IGST"))) {
	 * continue;
	 * }
	 * if (isGstTaxApplicable && txnWithWithoutTax == 1 &&
	 * (taxName.startsWith("SGST") || taxName.startsWith("CGST"))) {
	 * continue;
	 * }
	 * if (txnTypeOfSupply != 3 ){
	 * if (((txnSrcGstinStateCode == 0 || txnDstnGstinStateCode == 0) &&
	 * ((taxName.startsWith("SGST") || taxName.startsWith("CGST"))))
	 * || (txnSrcGstinStateCode != txnDstnGstinStateCode &&
	 * ((taxName.startsWith("SGST") || taxName.startsWith("CGST"))))) {
	 * continue;
	 * }
	 * if ((txnSrcGstinStateCode == 0 || txnDstnGstinStateCode == 0) &&
	 * taxName.startsWith("IGST") || (txnSrcGstinStateCode == txnDstnGstinStateCode
	 * && (taxName.startsWith("IGST") && txnWithWithoutTax != 1))) {
	 * continue;
	 * }
	 * }
	 * }
	 * Double taxRate = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxRate();
	 * if(taxName != null && taxName.length() > 4) {
	 * row.put("taxName", taxName.substring(0, 4));
	 * }else{
	 * row.put("taxName", taxName);
	 * }
	 * if(taxRate != null) {
	 * row.put("taxRate", taxRate);
	 * }else{
	 * row.put("taxRate", 0.0);
	 * }
	 * row.put("taxid", bnchSpecfTaxFormula.get(i).getBranchTaxes().getId());
	 * formularow.put("individualTaxFormula",
	 * bnchSpecfTaxFormula.get(i).getFormula());
	 * String appliedOn = bnchSpecfTaxFormula.get(i).getAppliedTo();
	 * //Tax 1 = GV*(Rate/100) & IV 1 = GV + Tax 1
	 * if (i == 0) {
	 * if (appliedOn.equals("GV")) {
	 * Tax1 = GV * (taxRate / (100.0));
	 * if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 1) {
	 * IV1 = GV + Tax1;
	 * row.put("individualTax", taxName + "(+" + taxRate + "%):" + df.format(Tax1));
	 * }else if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 0) {
	 * IV1 = GV - Tax1;
	 * row.put("individualTax", taxName + "(-" + taxRate + "%):" + df.format(Tax1));
	 * }
	 * taxTotalAmount += Tax1;
	 * }
	 * row.put("taxAmount", df.format(Tax1));
	 * }
	 * Based on selection
	 * If GV is selected: Tax 2 = GV*(Rate/100)
	 * If Tax 1 is selected: Tax 2 = Tax 1*(Rate/100)
	 * If IV 1 is selected Tax 2 = IV 1( Rate/100)
	 * IV 2 = GV + Tax 1 + Tax 2
	 * 
	 * else if (i == 1) {
	 * if (appliedOn.equals("GV")) {
	 * Tax2 = GV * (taxRate / (100.0));
	 * } else if (appliedOn.equals("Tax1")) {
	 * Tax2 = Tax1 * (taxRate / (100.0));
	 * } else if (appliedOn.equals("IV1")) {
	 * Tax2 = IV1 * (taxRate / (100.0));
	 * }
	 * 
	 * if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 1) {
	 * IV2 = GV + (Tax1 + Tax2);
	 * row.put("individualTax", taxName + "(+" + taxRate + "%):" + df.format(Tax2));
	 * }else if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 0) {
	 * IV2 = GV - (Tax1 + Tax2);
	 * row.put("individualTax", taxName + "(-" + taxRate + "%):" + df.format(Tax2));
	 * }
	 * taxTotalAmount += Tax2;
	 * row.put("taxAmount", df.format(Tax2));
	 * }
	 * Based on selection
	 * If GV is selected: Tax 3 = GV*(Rate/100)
	 * If Tax 1 is selected: Tax 3 = Tax 1*(Rate/100)
	 * If Tax 2 is selected: Tax 3 = Tax 2*(Rate/100)
	 * if IV 2 is selected Tax 3 = IV 2*(Rate/100)
	 * IV 3 = GV + Tax 1 + Tax 2 + Tax 3
	 * 
	 * else if (i == 2) {
	 * if (appliedOn.equals("GV")) {
	 * Tax3 = GV * (taxRate / (100.0));
	 * } else if (appliedOn.equals("Tax1")) {
	 * Tax3 = Tax1 * (taxRate / (100.0));
	 * } else if (appliedOn.equals("Tax2")) {
	 * Tax3 = Tax2 * (taxRate / (100.0));
	 * } else if (appliedOn.equals("IV2")) {
	 * Tax3 = IV2 * (taxRate / (100.0));
	 * }
	 * 
	 * if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 1) {
	 * IV3 = GV + (Tax1 + Tax2 + Tax3);
	 * row.put("individualTax", taxName + "(+" + taxRate + "%):" + df.format(Tax3));
	 * }else if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 0) {
	 * IV3 = GV - (Tax1 + Tax2 + Tax3);
	 * row.put("individualTax", taxName + "(-" + taxRate + "%):" + df.format(Tax3));
	 * }
	 * taxTotalAmount += Tax3;
	 * row.put("taxAmount", df.format(Tax3));
	 * }else if (i == 3) {
	 * if (appliedOn.equals("GV")) {
	 * Tax4 = GV * (taxRate / (100.0));
	 * } else if (appliedOn.equals("Tax1")) {
	 * Tax4 = Tax1 * (taxRate / (100.0));
	 * } else if (appliedOn.equals("Tax2")) {
	 * Tax4 = Tax2 * (taxRate / (100.0));
	 * } else if (appliedOn.equals("Tax3")) {
	 * Tax4 = Tax3 * (taxRate / (100.0));
	 * } else if (appliedOn.equals("IV3")) {
	 * Tax4 = IV3 * (taxRate / (100.0));
	 * }
	 * 
	 * if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 1) {
	 * IV4 = GV + (Tax1 + Tax2 + Tax3 + Tax4);
	 * row.put("individualTax", taxName + "(+" + taxRate + "%):" + df.format(Tax4));
	 * }else if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 0) {
	 * IV4 = GV - (Tax1 + Tax2 + Tax3 + Tax4);
	 * row.put("individualTax", taxName + "(-" + taxRate + "%):" + df.format(Tax4));
	 * }
	 * taxTotalAmount += Tax4;
	 * row.put("taxAmount", df.format(Tax4));
	 * }else if (i == 4) {
	 * if (appliedOn.equals("GV")) {
	 * Tax5 = GV * (taxRate / (100.0));
	 * } else if (appliedOn.equals("Tax1")) {
	 * Tax5 = Tax1 * (taxRate / (100.0));
	 * } else if (appliedOn.equals("Tax2")) {
	 * Tax5 = Tax2 * (taxRate / (100.0));
	 * } else if (appliedOn.equals("Tax3")) {
	 * Tax5 = Tax3 * (taxRate / (100.0));
	 * } else if (appliedOn.equals("Tax4")) {
	 * Tax5 = Tax4 * (taxRate / (100.0));
	 * } else if (appliedOn.equals("IV2")) {
	 * Tax5 = IV2 * (taxRate / (100.0));
	 * }
	 * if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 1) {
	 * IV5 = GV + (Tax1 + Tax2 + Tax3 + Tax4 + Tax5);
	 * row.put("individualTax", taxName + "(+" + taxRate + "%):" + df.format(Tax5));
	 * }else if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 0) {
	 * IV5 = GV - (Tax1 + Tax2 + Tax3 + Tax4 + Tax5);
	 * row.put("individualTax", taxName + "(-" + taxRate + "%):" + df.format(Tax5));
	 * }
	 * taxTotalAmount += Tax5;
	 * row.put("taxAmount", df.format(Tax5));
	 * }
	 * 
	 * if(i < 5) {
	 * branchSpecificsTaxComponentan.add(row);
	 * branchSpecificsTaxFormulaComponentan.add(formularow);
	 * }
	 * }
	 * }
	 * ObjectNode row = Json.newObject();
	 * row.put("taxTotalAmount", taxTotalAmount);
	 * branchSpecificsTaxResultAmountan.add(row);
	 * }
	 * transactionDao.setInvoiceQuotProfSerial(user, entityManager,myTransaction);
	 * genericDAO.saveOrUpdate(myTransaction, user, entityManager);
	 * 
	 * 
	 * // SellTransactionServiceImpl.transactionItemsService.
	 * insertMultipleItemsTransactionItems(entityManager, user,"", myTransaction);
	 * try{
	 * 
	 * Double howMuchAdvance=0.0;Double txnTaxAmount=0.0;Double
	 * customerAdvance=0.0;Double withholdingAmount=0.0;
	 * Double txnInvoiceValue = 0.0;
	 * 
	 * 
	 * TransactionItems transactionItem = new TransactionItems();
	 * Long itemId =rowItemData.getLong("txnItems");
	 * Specifics txnItem=genericDAO.getById(Specifics.class, itemId, entityManager);
	 * Double txnPerUnitPrice=rowItemData.getDouble("txnPerUnitPrice");
	 * Double txnNoOfUnit=rowItemData.getDouble("txnNoOfUnit");
	 * Double txnGross=rowItemData.getDouble("txnGross");
	 * String txnTaxDesc="";
	 * if(!rowItemData.isNull("txnTaxDesc") &&
	 * !rowItemData.get("txnTaxDesc").equals("")){
	 * txnTaxDesc=rowItemData.getString("txnTaxDesc");
	 * }
	 * transactionItem.setTaxDescription(txnTaxDesc);
	 * if(myTransaction.getTransactionPurpose().getId() ==
	 * IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW ||
	 * transaction.getTransactionPurpose().getId() ==
	 * IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
	 * if(user.getOrganization().getGstCountryCode() != null &&
	 * !"".equals(user.getOrganization().getGstCountryCode())) {
	 * saveTransactionTaxes(transactionItem, rowItemData, transaction);
	 * }else{
	 * setSellTaxInfo(transactionItem);
	 * }
	 * }else if(transaction.getTransactionPurpose().getId() ==
	 * IdosConstants.PREPARE_QUOTATION ||
	 * transaction.getTransactionPurpose().getId() == IdosConstants.PROFORMA_INVOICE
	 * ){
	 * setSellTaxInfo(transactionItem);
	 * }else if((transaction.getTransactionPurpose().getId() ==
	 * IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY ||
	 * transaction.getTransactionPurpose().getId() ==
	 * IdosConstants.BUY_ON_CREDIT_PAY_LATER ||
	 * transaction.getTransactionPurpose().getId() ==
	 * IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) &&
	 * user.getOrganization().getGstCountryCode() != null &&
	 * !"".equals(user.getOrganization().getGstCountryCode())) {
	 * saveTransactionTaxes(transactionItem, rowItemData, transaction);
	 * }else{
	 * setBuyTaxInfo(transactionItem);
	 * }
	 * 
	 * String taxNameOnAdvAdj = "";
	 * if(!rowItemData.isNull("txnTaxNameOnAdvAdj") &&
	 * !rowItemData.get("txnTaxNameOnAdvAdj").equals("")){
	 * taxNameOnAdvAdj=rowItemData.getString("txnTaxNameOnAdvAdj");
	 * }
	 * String taxNameOnAdvAdjArray[] = taxNameOnAdvAdj.split(",");
	 * Double txnTaxOnAdvAdj = 0d; String taxOnAdvAdj = "";
	 * if(!rowItemData.isNull("txnTaxOnAdvAdj") &&
	 * !rowItemData.get("txnTaxOnAdvAdj").equals("")){
	 * taxOnAdvAdj = rowItemData.getString("txnTaxOnAdvAdj");
	 * }
	 * String taxOnAdvAdjArray[] = taxOnAdvAdj.split(",");
	 * for (int count = 0; count < taxOnAdvAdjArray.length; count++) {
	 * if(!"".equals(taxOnAdvAdjArray[count])){
	 * txnTaxOnAdvAdj = Double.parseDouble(taxOnAdvAdjArray[count]);
	 * if (transactionItem.getTaxName1() != null &&
	 * transactionItem.getTaxName1().indexOf(taxNameOnAdvAdjArray[count]) != -1) {
	 * transactionItem.setAdvAdjTax1Value(txnTaxOnAdvAdj);
	 * } else if (transactionItem.getTaxName2() != null &&
	 * transactionItem.getTaxName2().indexOf(taxNameOnAdvAdjArray[count]) != -1) {
	 * transactionItem.setAdvAdjTax2Value(txnTaxOnAdvAdj);
	 * } else if (transactionItem.getTaxName3() != null &&
	 * transactionItem.getTaxName3().indexOf(taxNameOnAdvAdjArray[count]) != -1) {
	 * transactionItem.setAdvAdjTax3Value(txnTaxOnAdvAdj);
	 * } else if (transactionItem.getTaxName4() != null &&
	 * transactionItem.getTaxName4().indexOf(taxNameOnAdvAdjArray[count]) != -1) {
	 * transactionItem.setAdvAdjTax4Value(txnTaxOnAdvAdj);
	 * } else if (transactionItem.getTaxName5() != null &&
	 * transactionItem.getTaxName5().indexOf(taxNameOnAdvAdjArray[count]) != -1) {
	 * transactionItem.setAdvAdjTax5Value(txnTaxOnAdvAdj);
	 * }
	 * }
	 * }
	 * if(!rowItemData.isNull("withholdingAmount") &&
	 * !rowItemData.get("withholdingAmount").equals("")){
	 * withholdingAmount=rowItemData.getDouble("withholdingAmount");
	 * }
	 * if(!rowItemData.isNull("customerAdvance") &&
	 * !rowItemData.get("customerAdvance").equals("")){
	 * customerAdvance=rowItemData.getDouble("customerAdvance");
	 * }
	 * if(!rowItemData.isNull("howMuchAdvance") &&
	 * !rowItemData.get("howMuchAdvance").equals("")){
	 * howMuchAdvance=rowItemData.getDouble("howMuchAdvance");
	 * }
	 * if(!rowItemData.isNull("txnInvoiceValue") &&
	 * !rowItemData.get("txnInvoiceValue").equals("")){
	 * txnInvoiceValue=rowItemData.getDouble("txnInvoiceValue");
	 * }
	 * Double netAmountVal=0d;
	 * if(transaction.getTransactionPurpose().getId() ==
	 * IdosConstants.PREPARE_QUOTATION){
	 * netAmountVal = txnGross;
	 * } else {
	 * if (!rowItemData.isNull("netAmountVal") &&
	 * !rowItemData.get("netAmountVal").equals("")) {
	 * netAmountVal = rowItemData.getDouble("netAmountVal");
	 * }
	 * }
	 * 
	 * String discountPercent = "0";
	 * if(!rowItemData.isNull("txnDiscountPercent")){
	 * discountPercent = rowItemData.getString("txnDiscountPercent");
	 * }
	 * Double discountAmount = 0.0;
	 * if(!rowItemData.isNull("txnDiscountAmt") &&
	 * !rowItemData.get("txnDiscountAmt").equals("")){
	 * discountAmount = rowItemData.getDouble("txnDiscountAmt");
	 * }
	 * //storing budget info for buy transactions
	 * String userTxnAmountLimitDesc="";String budgetAvailDuringTxn=""; String
	 * actualAllocatedBudget="";
	 * if(!rowItemData.isNull("amountRangeLimitRuleVal") &&
	 * !rowItemData.get("amountRangeLimitRuleVal").equals("")){
	 * userTxnAmountLimitDesc=rowItemData.getString("amountRangeLimitRuleVal");
	 * }
	 * if(!rowItemData.isNull("budgetDisplayVal") &&
	 * !rowItemData.get("budgetDisplayVal").equals("")){
	 * budgetAvailDuringTxn=rowItemData.getString("budgetDisplayVal");
	 * }
	 * if(!rowItemData.isNull("actualbudgetDisplayVal") &&
	 * !rowItemData.get("actualbudgetDisplayVal").equals("")){
	 * actualAllocatedBudget=rowItemData.getString("actualbudgetDisplayVal");
	 * }
	 * transactionItem.setUserTxnLimitDesc(userTxnAmountLimitDesc);
	 * transactionItem.setActualAllocatedBudget(actualAllocatedBudget);
	 * transactionItem.setBudgetAvailDuringTxn(budgetAvailDuringTxn);
	 * if (budgetAvailDuringTxn != null && !budgetAvailDuringTxn.equals("")) {
	 * if (netAmountVal > Double.parseDouble(budgetAvailDuringTxn)) {
	 * myTransaction.setTransactionExceedingBudget(1);
	 * }
	 * }
	 * //transactionItem.setTransactionId(transaction.getId());
	 * transactionItem.setTransactionId(myTransaction);
	 * transactionItem.setBranch(myTransaction.getTransactionBranch());
	 * transactionItem.setOrganization(myTransaction.
	 * getTransactionBranchOrganization());
	 * transactionItem.setTransactionSpecifics(specifics);
	 * transactionItem.setTransactionParticulars(specifics.getParticularsId());
	 * transactionItem.setPricePerUnit(Double.parseDouble(price));
	 * transactionItem.setNoOfUnits(Double.parseDouble(units));
	 * // transactionItem.setGrossAmount(txnGross);
	 * transactionItem.setInvoiceValue(txnInvoiceValue);
	 * 
	 * //transactionItem.setTotalTax(txnTaxAmount); should be set from
	 * saveTransactionTaxes
	 * transactionItem.setWithholdingAmount(withholdingAmount);
	 * transactionItem.setAvailableAdvance(customerAdvance);
	 * transactionItem.setAdjustmentFromAdvance(howMuchAdvance);
	 * // transactionItem.setNetAmount(netAmountVal);
	 * transactionItem.setDiscountPercent(discountPercent);
	 * //transactionItem.setDiscountAmount(discountAmount);
	 * 
	 * //advance adjustment
	 * if(howMuchAdvance != 0.0){
	 * Map<String, Object> criterias1 = new HashMap<String, Object>();
	 * criterias1.put("vendorSpecific.id",
	 * myTransaction.getTransactionVendorCustomer().getId());
	 * criterias1.put("specificsVendors.id", specifics.getId());
	 * criterias1.put("organization.id", user.getOrganization().getId());
	 * VendorSpecific
	 * customerTxnSpecifics=genericDAO.getByCriteria(VendorSpecific.class,
	 * criterias, entityManager);
	 * customerTxnSpecifics.setAdvanceMoney(customerTxnSpecifics.getAdvanceMoney()-
	 * howMuchAdvance);
	 * genericDAO.saveOrUpdate(customerTxnSpecifics, user, entityManager);
	 * SELL_TRANSACTION_DAO.saveAdvanceAdjustmentDetail(user,
	 * entityManager,specifics, transactionItem, transaction, howMuchAdvance);
	 * }
	 * if(!rowItemData.isNull("txnLeftOutWithholdTransIDs") &&
	 * !rowItemData.get("txnLeftOutWithholdTransIDs").equals("")){
	 * String txnLeftOutWithholdTransIDs =
	 * rowItemData.getString("txnLeftOutWithholdTransIDs");
	 * TransactionController.updateWithholdingForLeftOutTrans(
	 * txnLeftOutWithholdTransIDs, user, entityManager); // call only if withholding
	 * for previous transaction was not calculated.
	 * }
	 * genericDAO.saveOrUpdate(transactionItem, user, entityManager);
	 * }
	 * }
	 * catch(Exception ex){
	 * log.log(Level.SEVERE, "Error", ex);
	 * throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE,
	 * IdosConstants.TECHNICAL_EXCEPTION, "Error on save/update multiitems.",
	 * ex.getMessage());
	 * }
	 * genericDAO.saveOrUpdate(myTransaction, user, entityManager); // need to
	 * update becuase of desgin issue
	 * //Trial balance entries
	 * tralBalanceService.insertTrialBalance(myTransaction, user, entityManager);
	 * 
	 * //if trading inventory then need this for INVENTORY_REPORT
	 * stockService.insertTradingInventory(myTransaction, user, entityManager);
	 * 
	 * //call Karvy API to submit Sell data for GST Filing
	 * KarvyAuthorization karvyAPICall = new KarvyAuthorization(jpaApi,
	 * application);
	 * karvyAPICall.saveGSTFilingData(user,myTransaction,entityManager);
	 * //karvyAPICall.sendSellTranDataToKarvy(transaction,entityManager);
	 * 
	 * Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
	 * Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
	 * for (int k = 0; k < keyArray.length; k++) {
	 * StringBuilder sbquery = new StringBuilder("");
	 * sbquery.append("select obj from Users obj WHERE obj.email ='" + keyArray[i] +
	 * "'");
	 * List<Users> orgusers = genericDAO.executeSimpleQuery(sbquery.toString(),
	 * entityManager);
	 * if (!orgusers.isEmpty() && orgusers.get(0).getOrganization().getId() ==
	 * user.getOrganization().getId()) {
	 * orgtxnregistereduser.put(keyArray[i].toString(),
	 * CreatorActor.expenseregistrered.get(keyArray[i]));
	 * }
	 * }
	 * transaction.commit();
	 * 
	 * String invoiceDate = "";
	 * String invoiceDateLabel = "";
	 * if (myTransaction.getTransactionInvoiceDate() != null) {
	 * invoiceDateLabel = "INVOICE DATE:";
	 * invoiceDate =
	 * IdosConstants.idosdf.format(myTransaction.getTransactionInvoiceDate());
	 * }
	 * String itemParentName = "";
	 * if (txnSpecificItem.getParentSpecifics() != null &&
	 * !txnSpecificItem.getParentSpecifics().equals("")) {
	 * itemParentName = txnSpecificItem.getParentSpecifics().getName();
	 * } else {
	 * itemParentName = txnSpecificItem.getParticularsId().getName();
	 * }
	 * String txnSpecialStatus = "";
	 * if (myTransaction.getTransactionExceedingBudget() != null &&
	 * myTransaction.getKlFollowStatus() != null) {
	 * if (myTransaction.getTransactionExceedingBudget() == 1 &&
	 * myTransaction.getKlFollowStatus() == 0) {
	 * txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
	 * }
	 * if (myTransaction.getTransactionExceedingBudget() == 1 &&
	 * myTransaction.getKlFollowStatus() == 1) {
	 * txnSpecialStatus = "Transaction Exceeding Budget";
	 * }
	 * }
	 * if (myTransaction.getTransactionExceedingBudget() == null &&
	 * myTransaction.getKlFollowStatus() != null) {
	 * if (myTransaction.getKlFollowStatus() == 0) {
	 * txnSpecialStatus = "Rules Not Followed";
	 * }
	 * }
	 * if (myTransaction.getTransactionExceedingBudget() != null &&
	 * myTransaction.getKlFollowStatus() == null) {
	 * txnSpecialStatus = "Transaction Exceeding Budget";
	 * }
	 * String txnResultDesc = "";
	 * if (myTransaction.getNetAmountResultDescription() != null &&
	 * !myTransaction.getNetAmountResultDescription().equals("null")) {
	 * txnResultDesc = myTransaction.getNetAmountResultDescription();
	 * }
	 * String tranInvoice = myTransaction.getInvoiceNumber();
	 * CreatorActor.addTxn(myTransaction.getId(), txnBranchName, projectName,
	 * itemCode, itemParentName,
	 * customerCode,myTransaction.getTransactionPurpose().getTransactionPurpose(),
	 * IdosConstants.idosdf.format(myTransaction.getTransactionDate()),
	 * invoiceDateLabel, invoiceDate,"",myTransaction.getNoOfUnits(),
	 * myTransaction.getPricePerUnit(), myTransaction.getGrossAmount(),
	 * myTransaction.getNetAmount(), txnResultDesc, "",
	 * myTransaction.getTransactionStatus(),
	 * myTransaction.getCreatedBy().getEmail(), "", "", "","","",
	 * orgtxnregistereduser, txnSpecialStatus, myTransaction.getFrieghtCharges(),
	 * myTransaction.getPoReference(),"","",
	 * myTransaction.getTransactionPurpose().getId(), tranInvoice);
	 * }
	 * 
	 * 
	 * }
	 * 
	 * transaction.commit();
	 * 
	 * }catch (RuntimeException e) {
	 * if ( transaction != null && transaction.isActive()) {
	 * transaction.rollback();
	 * }
	 * throw e;
	 * }
	 * catch(Exception ex){
	 * log.log(Level.SEVERE, "Error", ex);
	 * ex.printStackTrace();
	 * }finally{
	 * log.log(Level.INFO, "Total rows inserted "+ totalRowsInserted);
	 * result.put("totalRowsInserted",totalRowsInserted-1);
	 * if ( transaction != null && transaction.isActive()) {
	 * transaction.rollback();
	 * }
	 * }
	 * 
	 * }
	 * ObjectNode row = Json.newObject();
	 * row.put("message", "Uploaded Successfully");
	 * an.add(row);
	 * 
	 * }catch(Exception ex){
	 * if(transaction.isActive()){
	 * transaction.rollback();
	 * }
	 * log.log(Level.SEVERE, "Error", ex);
	 * // log.log(Level.SEVERE, ex.getMessage());
	 * String strBuff=getStackTraceMessage(ex);
	 * expService.sendExceptionReport(strBuff,user.getEmail(),
	 * user.getOrganization().getName(),
	 * Thread.currentThread().getStackTrace()[1].getMethodName());
	 * List<String> errorList=getStackTrace(ex);
	 * return Results.ok(errorPage.render(ex,errorList));
	 * }
	 * return Results.ok(result);
	 * }
	 */

}
