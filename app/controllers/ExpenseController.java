package controllers;

import java.time.chrono.JapaneseChronology;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Session;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.IdosConstants;
import model.AuditLogs;
import model.Branch;
import model.ConfigParams;
import model.Expense;
import model.Organization;
import model.Particulars;
import model.Specifics;
import model.Users;
import model.UsersRoles;
import model.Vendor;
import model.VendorSpecific;
import akka.NotUsed;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.HtmlEmail;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.WebSocket;
import views.html.*;
import actor.CreatorActor;
import actor.VendorTransactionActor;
import akka.stream.javadsl.*;
import akka.actor.*;
import com.idos.util.CodeHelper;
import com.idos.util.IdosUtil;
import javax.inject.Inject;
import play.Application;
import java.util.logging.Level;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;
import pojo.TransactionViewResponse;

public class ExpenseController extends StaticController {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	public static Application application;
	private Request request;
	// private Http.Session session = request.session();

	@Inject
	public ExpenseController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();

	}

	@Transactional
	public Result getExpenseDetails(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			user = getUserInfo(request);
			ArrayNode san = result.putArray("specificsData");
			ArrayNode van = result.putArray("vendorData");
			Specifics specific = null;
			List<VendorSpecific> vendorsSpecifics = new ArrayList<VendorSpecific>();
			List<Specifics> specificList = Specifics.list(entityManager, user.getOrganization());
			if (specificList.size() > 0) {
				specific = Specifics.findById(specificList.get(0).getId());
				vendorsSpecifics = specific.getSpecificsVendors();
				for (Specifics specifics : specificList) {
					ObjectNode row = Json.newObject();
					row.put("id", specifics.getId());
					row.put("name", specifics.getName());
					row.put("noData", "Data");
					san.add(row);
				}
			} else {
				ObjectNode row = Json.newObject();
				row.put("noData", "No Data");
				san.add(row);
			}
			if (vendorsSpecifics.size() > 0) {
				for (VendorSpecific vendorSpecific : vendorsSpecifics) {
					ObjectNode row = Json.newObject();
					row.put("id", vendorSpecific.getVendorSpecific().getId());
					row.put("name", vendorSpecific.getVendorSpecific().getName());
					row.put("unitcost", vendorSpecific.getUnitPrice());
					row.put("noData", "Data");
					van.add(row);
				}
			} else {
				ObjectNode row = Json.newObject();
				row.put("noData", "No Data");
				van.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getTransactionList(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			ArrayNode san = result.putArray("specificsData");
			ArrayNode van = result.putArray("vendorData");
			String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
			if (ipAddress == null) {
				ipAddress = request.remoteAddress();
			}
			String useremail = json.findValue("usermail").asText();
			String transactionPurpose = json.findValue("transactionPurpose").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			Specifics specific = null;
			List<VendorSpecific> vendorsSpecifics = new ArrayList<VendorSpecific>();
			List<Specifics> specificList = null;
			if (transactionPurpose.equals("1")) {
				StringBuilder newsbquery = new StringBuilder("");
				newsbquery.append(
						"select obj from Specifics obj WHERE obj.accountCodeHirarchy like '%2000000%' AND obj.organization.id ='"
								+ user.getOrganization().getId() + "' and obj.presentStatus=1");
				specificList = genericDAO.executeSimpleQuery(newsbquery.toString(), entityManager);
			}
			if (transactionPurpose.equals("2")) {
				StringBuilder newsbquery = new StringBuilder("");
				newsbquery.append(
						"select obj from Specifics obj WHERE obj.accountCodeHirarchy not like '%2000000%' AND obj.organization.id ='"
								+ user.getOrganization().getId() + "' and obj.presentStatus=1");
				specificList = genericDAO.executeSimpleQuery(newsbquery.toString(), entityManager);
			}
			if (specificList.size() > 0) {
				specific = Specifics.findById(specificList.get(0).getId());
				vendorsSpecifics = specific.getSpecificsVendors();
				for (Specifics specifics : specificList) {
					ObjectNode row = Json.newObject();
					row.put("id", specifics.getId());
					row.put("name", specifics.getName());
					row.put("noData", "Data");
					san.add(row);
				}
			} else {
				ObjectNode row = Json.newObject();
				row.put("noData", "No Data");
				san.add(row);
			}
			if (vendorsSpecifics.size() > 0) {
				for (VendorSpecific vendorSpecific : vendorsSpecifics) {
					ObjectNode row = Json.newObject();
					row.put("id", vendorSpecific.getVendorSpecific().getId());
					row.put("name", vendorSpecific.getVendorSpecific().getName());
					row.put("unitcost", vendorSpecific.getUnitPrice());
					row.put("noData", "Data");
					van.add(row);
				}
			} else {
				ObjectNode row = Json.newObject();
				row.put("noData", "No Data");
				van.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result showExpenseDetails(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users userInfo = null;
		try {
			ArrayNode expan = result.putArray("expensedetailsData");
			ArrayNode approverUser = result.putArray("additionalUsers");
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			session.adding("email", useremail);
			userInfo = getUserInfo(request);
			String expenseId = json.findValue("entityPrimaryId").asText();
			Expense expenses = Expense.findById(IdosUtil.convertStringToLong(expenseId));
			if (expenses != null) {
				ObjectNode row = Json.newObject();
				row.put("id", expenses.getId());
				row.put("txnPurpose", expenses.getTransactionPurpose());
				switch (expenses.getTransactionPurpose()) {
					case 1:
						row.put("txnPurposeText", "Buy");
						break;
					case 2:
						row.put("txnPurposeText", "Pay");
						break;
					case 3:
						row.put("txnPurposeText", "Deposit");
						break;
					case 4:
						row.put("txnPurposeText", "Withdrawal");
						break;
					case 5:
						row.put("txnPurposeText", "Provision");
						break;
					case 6:
						row.put("txnPurposeText", "Sell");
						break;
					case 7:
						row.put("txnPurposeText", "Receive");
						break;
					case 8:
						row.put("txnPurposeText", "Hire");
						break;
					default:
						row.put("txnPurposeText", "Buy");
						break;
				}
				row.put("item", expenses.getSpecifics().getId());
				row.put("itemName", expenses.getSpecifics().getName());
				row.put("payType", expenses.getPaymentType());
				switch (expenses.getPaymentType()) {
					case 1:
						row.put("payTypeText", "Credit Purchase");
						break;
					case 2:
						row.put("payTypeText", "Cash Purchase");
						break;
					default:
						row.put("payTypeText", "Credit Purchase");
						break;
				}
				row.put("vendor", expenses.getVendor().getId());
				row.put("vendorName", expenses.getVendor().getName());
				row.put("perunitcost", expenses.getUnitCost());
				row.put("noofunits", expenses.getNoOfItems());
				row.put("tamount", expenses.getTotalCost());
				row.put("docuploadurl", expenses.getFileUrl());
				expan.add(row);
			}
			StringBuilder sbquery = new StringBuilder("");
			sbquery.append("select obj from UsersRoles obj WHERE (obj.role.id=4) and obj.organization.id='"
					+ expenses.getUsers().getOrganization().getId() + "' and obj.presentStatus=1");
			List<UsersRoles> usersroles = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
			Map<Long, String> addUser = new HashMap<Long, String>();
			for (UsersRoles usrRoles : usersroles) {
				if (!(expenses.getUsers().getId().equals(usrRoles.getUser().getId()))
						&& (!usrRoles.getUser().getId().equals(userInfo.getId()))) {
					if (expenses.getNextLabelForwadedBy() != null) {
						String emailArr[] = expenses.getNextLabelForwadedBy().split(",");
						for (int i = 0; i < emailArr.length; i++) {
							if (!emailArr[i].equals(usrRoles.getUser().getEmail())) {
								addUser.put(usrRoles.getUser().getId(), usrRoles.getUser().getEmail());
							}
						}
					} else {
						addUser.put(usrRoles.getUser().getId(), usrRoles.getUser().getEmail());
					}
				}
			}
			for (Long addUserId : addUser.keySet()) {
				ObjectNode row = Json.newObject();
				row.put("id", addUserId);
				row.put("email", addUser.get(addUserId));
				approverUser.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, userInfo.getEmail(), userInfo.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getCost(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode results = Json.newObject();
		try {
			entityManager.getTransaction().begin();
			ArrayNode costan = results.putArray("unitcostData");
			JsonNode json = request.body().asJson();
			String cost = "";
			String specificsId = json.findValue("specifics").asText();
			String vendorId = json.findValue("vendor").asText();
			Vendor vend = Vendor.findById(IdosUtil.convertStringToLong(vendorId));
			List<VendorSpecific> vendSpecs = vend.getVendorsSpecifics();
			for (VendorSpecific vendSpec : vendSpecs) {
				if (vendSpec.getSpecificsVendors().getId() == IdosUtil.convertStringToLong(specificsId)) {
					cost = String.valueOf(vendSpec.getUnitPrice());
				}
			}
			ObjectNode row = Json.newObject();
			row.put("unitcost", cost);
			costan.add(row);
			entityManager.getTransaction().commit();
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "GetCost Email", "GetCost Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result partBasedDetails(Request request) {
		log.log(Level.FINE, ">>>> Start");

		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			ArrayNode san = result.putArray("specificsData");
			ArrayNode van = result.putArray("vendorData");
			String id = json.findValue("id").asText();
			int flag = 0;
			Particulars part = Particulars.findById(IdosUtil.convertStringToLong(id));
			List<Specifics> specificList = part.getSpecifics();
			if (specificList.size() > 0) {
				for (Specifics specifics : specificList) {
					ObjectNode row = Json.newObject();
					row.put("id", specifics.getId());
					row.put("name", specifics.getName());
					san.add(row);
				}
				ObjectNode row = Json.newObject();
				Specifics specific = Specifics.findById(specificList.get(0).getId());
				List<VendorSpecific> vendorsSpecifics = specific.getSpecificsVendors();
				for (VendorSpecific vendorSpecific : vendorsSpecifics) {
					flag = 1;
					row.put("id", vendorSpecific.getVendorSpecific().getId());
					row.put("name", vendorSpecific.getVendorSpecific().getName());
					row.put("unitcost", vendorSpecific.getUnitPrice());
					van.add(row);
				}
				if (flag == 0) {
					row.put("id", "");
					row.put("name", "");
					row.put("unitcost", "");
					van.add(row);
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "PartBasedDetails Email", "PartBasedDetails Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getParticulars(Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			user = getUserInfo(request);
			ArrayNode an = result.putArray("particularsData");
			List<Particulars> particulars = Particulars.list(entityManager, user.getOrganization());
			for (Particulars particularlist : particulars) {
				ObjectNode row = Json.newObject();
				row.put("id", particularlist.getId());
				row.put("name", particularlist.getName());
				an.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getSpecifics(Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			ArrayNode an = result.putArray("specificsData");
			String id = json.findValue("id").asText();
			Particulars part = Particulars.findById(Long.parseLong(id));
			List<Specifics> specificList = part.getSpecifics();
			for (Specifics specifics : specificList) {
				ObjectNode row = Json.newObject();
				row.put("id", specifics.getId());
				row.put("name", specifics.getName());
				an.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "GetSpecifics Email", "GetSpecifics Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getAllSpecifics(Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			ArrayNode an = result.putArray("specificallData");
			List<Specifics> specificList = Specifics.list(entityManager, user.getOrganization());
			for (Specifics specifics : specificList) {
				ObjectNode row = Json.newObject();
				row.put("id", specifics.getId());
				row.put("name", specifics.getName());
				an.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getVendors(Request request) {
		log.log(Level.FINE, ">>>> Start");

		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			ArrayNode an = result.putArray("vendorData");
			String id = json.findValue("id").asText();
			int flag = 0;
			Specifics specific = Specifics.findById(Long.parseLong(id));
			List<VendorSpecific> vendorsSpecifics = specific.getSpecificsVendors();
			for (VendorSpecific vendorSpecific : vendorsSpecifics) {
				ObjectNode row = Json.newObject();
				flag = 1;
				row.put("id", vendorSpecific.getVendorSpecific().getId());
				row.put("name", vendorSpecific.getVendorSpecific().getName());
				row.put("unitcost", vendorSpecific.getUnitPrice());
				an.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "GetVendors Email", "GetVendors Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result submitForApproval(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		Users usr = null;
		try {
			transaction.begin();
			Expense expenses = null;
			ObjectNode exprow = Json.newObject();
			String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
			if (ipAddress == null) {
				ipAddress = request.remoteAddress();
			}
			JsonNode json = request.body().asJson();
			String txnPurpose = json.findPath("transactionPurpose").asText();
			String txnPurposeText = json.findPath("transactionPurposeText").asText();
			String useremail = json.findValue("useremail").asText();
			session.adding("email", useremail);
			usr = getUserInfo(request);
			String role = "";
			List<UsersRoles> userRoles = UsersRoles.getUserRoleList(entityManager, usr.getOrganization().getId(),
					usr.getId(),
					usr.getBranch().getId());
			for (UsersRoles roles : userRoles) {
				role += roles.getRole().getName() + ",";
			}
			role = role.substring(0, role.length() - 1);
			results.put("role", role);
			String payType = json.findPath("payType").asText();
			String payTypeText = json.findPath("payTypeText").asText();
			String specificId = json.findPath("specifics").asText();
			String vendorId = json.findPath("vendor").asText();
			Branch bnch = usr.getBranch();
			Organization orgn = usr.getOrganization();
			Vendor ven = null;
			if (vendorId.matches("[a-zA-Z]+")) {
				ven = new Vendor();
				ven.setName(vendorId);
				ven.setBranch(bnch);
				ven.setOrganization(orgn);
				genericDAO.saveOrUpdate(ven, usr, entityManager);
			} else {
				ven = Vendor.findById(Long.parseLong(vendorId));
			}
			String itemNumbers = json.findPath("itemNumbers").asText();
			String unitCost = json.findPath("perunitCost").asText();
			String totalCost = json.findPath("tCost").asText();
			String fileUrl = json.findPath("fileUrl").asText();
			String hiddenExpenseId = json.findPath("hidExpenseId").asText();
			String buttonActionId = json.findPath("buttonactionId").asText();
			String txnRemarks = json.findPath("txnRemarks").asText();
			Specifics spec = Specifics.findById(Long.parseLong(specificId));
			int noOfItems = Integer.parseInt(itemNumbers);
			Double tCost = Double.parseDouble(totalCost);
			if (hiddenExpenseId == "") {
				expenses = new Expense();
				expenses.setActionDate(Calendar.getInstance().getTime());
				if (fileUrl == "") {
					expenses.setFileUrl(null);
				} else {
					expenses.setFileUrl(fileUrl);
				}
				String transactionNumber = CodeHelper.getForeverUniqueID("MTXN", null);
				expenses.setTxnNumber(transactionNumber);
				expenses.setUsers(usr);
				expenses.setRemarks(useremail + "@" + txnRemarks);
			} else {
				expenses = Expense.findById(Long.parseLong(hiddenExpenseId));
				expenses.setModifiedAt(Calendar.getInstance().getTime());
				if (fileUrl == "") {
					if (expenses.getFileUrl() == null) {
						expenses.setFileUrl(null);
					}
				} else {
					if (expenses.getFileUrl() == null) {
						expenses.setFileUrl(fileUrl);
					} else {
						expenses.setFileUrl(expenses.getFileUrl() + "," + fileUrl);
					}
					expenses.setRemarks(expenses.getRemarks() + "," + useremail + "@" + txnRemarks);
				}
			}
			expenses.setUnitCost(Double.parseDouble(unitCost));
			expenses.setTransactionPurpose(Integer.parseInt(txnPurpose));
			expenses.setPaymentType(Integer.parseInt(payType));
			expenses.setSpecifics(spec);
			expenses.setParticulars(spec.getParticularsId());
			expenses.setVendor(ven);
			expenses.setNoOfItems(noOfItems);
			expenses.setTotalCost(tCost);
			expenses.setBranch(bnch);
			expenses.setOrganization(orgn);
			String action = "";
			if (buttonActionId.equals("submitApprovalButton")) {
				expenses.setStatus("Initiated");
				exprow.put("status", "Initiated");
				action = "Submit Expense For Approval";
			}
			if (buttonActionId.equals("submitAccountingButton")) {
				expenses.setStatus("Accounted");
				exprow.put("status", "Accounted");
				action = "Submit Expense For Accounting";
			}
			cntr.save(usr, expenses, entityManager);
			exprow.put("transactionPurpose", txnPurposeText);
			exprow.put("expenseSpecifics", spec.getName());
			exprow.put("paymentType", payTypeText);
			exprow.put("vendor", ven.getName());
			exprow.put("noOfItems", itemNumbers);
			exprow.put("unitCost", unitCost);
			exprow.put("totalCost", totalCost);
			String expenseSupportingDoc = "";
			;
			if (expenses.getFileUrl() != null && expenses.getFileUrl() != "") {
				int lastCommaOccurence = expenses.getFileUrl().lastIndexOf(',');
				if (lastCommaOccurence != -1) {
					expenseSupportingDoc = expenses.getFileUrl().substring(lastCommaOccurence + 1,
							expenses.getFileUrl().length());
				} else {
					expenseSupportingDoc = expenses.getFileUrl();
				}
			}
			exprow.put("expenseSupportingDoc", expenseSupportingDoc);
			exprow.put("expenseTransactionUser", useremail);
			exprow.put("expenseRemarks", useremail + "@" + txnRemarks);
			exprow.put("expenseTransactionNumber", expenses.getTxnNumber());
			exprow.put("expenseTransactionDate", mysqldtf.format(Calendar.getInstance().getTime()));
			auditDAO.saveAuditLogs(action, usr, expenses.getId(), Expense.class, ipAddress, exprow.toString(),
					entityManager);
			results.put("status", "OK");
			transaction.commit();
			TransactionViewResponse.add(expenses.getId(), useremail, expenses.getSpecifics().getName(),
					expenses.getVendor().getName(), expenses.getNoOfItems(), expenses.getTotalCost(),
					idosdf.format(expenses.getActionDate()), expenses.getStatus(), expenses.getFileUrl(), txnRemarks,
					results);
			if (vendorId.matches("[a-zA-Z]+")) {
				// Map<String, ActorRef> orgvendvendregistrered = new HashMap<String,
				// ActorRef>();
				// Object[] keyArray =
				// VendorTransactionActor.vendvendregistrered.keySet().toArray();
				// for (int i = 0; i < keyArray.length; i++) {
				// List<Users> orgusers = Users.findByEmailActDeact(entityManager, (String)
				// keyArray[i]);
				// if (!orgusers.isEmpty()
				// && orgusers.get(0).getOrganization().getId() ==
				// ven.getOrganization().getId()) {
				// orgvendvendregistrered.put(keyArray[i].toString(),
				// VendorTransactionActor.vendvendregistrered.get(keyArray[i]));
				// }
				// }
				// VendorTransactionActor.add(ven.getId(), orgvendvendregistrered,
				// ven.getName(), null, null, null, null,
				// null, null, null, ven.getPresentStatus());
				results.put("info", "vendorAdded");
				results.put("id", ven.getId());
				results.put("name", ven.getName());
				results.put("address", ven.getAddress());
				results.put("location", ven.getLocation());
				results.put("email", ven.getEmail());
				results.put("grantAccess", ven.getGrantAccess());
				results.put("phone", ven.getPhone());
				results.put("type", ven.getType());
				results.put("entityType", "vendorCustomer");
				results.put("presentStatus", ven.getPresentStatus());
				results.put("canCreateCustomer", usr.canCreateCustomer());
				results.put("canActivateCustomer", usr.canActivateCustomer());
				results.put("canActivateVendor", usr.canActivateVendor());
				results.put("canCreateVendor", usr.canCreateVendor());
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usr.getEmail(), usr.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result approveStatus(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		Users usr = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode an = results.putArray("expenseAction");
			AuditLogs expenselogs = new AuditLogs();
			ObjectNode exprow = Json.newObject();
			String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
			if (ipAddress == null) {
				ipAddress = request.remoteAddress();
			}
			String useremail = json.findValue("useremail").asText();
			session.adding("email", useremail);
			usr = getUserInfo(request);
			String expenseId = json.findPath("expense").asText();
			String txnRemarks = json.findPath("txnRemarks").asText();
			Expense expenses = Expense.findById(Long.parseLong(expenseId));
			expenses.setStatus("Approved");
			expenses.setRemarks(expenses.getRemarks() + "," + useremail + "@" + txnRemarks);
			expenses.setActionDate(Calendar.getInstance().getTime());
			CRUDController<Expense> cntr = new CRUDController<Expense>(application);
			cntr.save(usr, expenses, entityManager);
			exprow.put("status", "Approved");
			exprow.put("expenseRemarks", useremail + "@" + txnRemarks);
			String action = "Approval of Expense";
			switch (expenses.getTransactionPurpose()) {
				case 1:
					exprow.put("transactionPurpose", "Buy");
					break;
				case 2:
					exprow.put("transactionPurpose", "Pay");
					break;
				case 3:
					exprow.put("transactionPurpose", "Deposit");
					break;
				case 4:
					exprow.put("transactionPurpose", "Withdrawal");
					break;
				case 5:
					exprow.put("transactionPurpose", "Provision");
					break;
				case 6:
					exprow.put("transactionPurpose", "Sell");
					break;
				case 7:
					exprow.put("transactionPurpose", "Receive");
					break;
				case 8:
					exprow.put("transactionPurpose", "Hire");
					break;
				default:
					exprow.put("transactionPurpose", "Buy");
					break;
			}
			exprow.put("expenseSpecifics", expenses.getSpecifics().getName());
			switch (expenses.getPaymentType()) {
				case 1:
					exprow.put("paymentType", "Credit Purchase");
					break;
				case 2:
					exprow.put("paymentType", "Cash Purchase");
					break;
				default:
					exprow.put("paymentType", "Credit Purchase");
					break;
			}
			exprow.put("vendor", expenses.getVendor().getName());
			exprow.put("noOfItems", String.valueOf(expenses.getNoOfItems()));
			exprow.put("unitCost", String.valueOf(expenses.getUnitCost()));
			exprow.put("totalCost", String.valueOf(expenses.getTotalCost()));
			exprow.put("expenseSupportingDoc", "");
			exprow.put("expenseTransactionUser", useremail);
			exprow.put("expenseTransactionNumber", expenses.getTxnNumber());
			exprow.put("expenseTransactionDate", mysqldtf.format(Calendar.getInstance().getTime()));
			auditDAO.saveAuditLogs(action, usr, expenses.getId(), Expense.class, ipAddress, exprow.toString(),
					entityManager);
			transaction.commit();
			TransactionViewResponse.action(expenses.getId(), "Approved", null, null, txnRemarks, results);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usr.getEmail(), usr.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result rejectStatus(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		Users usr = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode an = results.putArray("expenseAction");
			AuditLogs expenselogs = new AuditLogs();
			ObjectNode exprow = Json.newObject();
			String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
			if (ipAddress == null) {
				ipAddress = request.remoteAddress();
			}
			String useremail = json.findValue("useremail").asText();
			session.adding("email", useremail);
			usr = getUserInfo(request);
			String expenseId = json.findPath("expense").asText();
			String txnRemarks = json.findPath("txnRemarks").asText();
			Expense expenses = Expense.findById(Long.parseLong(expenseId));
			expenses.setStatus("Rejected");
			expenses.setRemarks(expenses.getRemarks() + "," + useremail + "@" + txnRemarks);
			expenses.setActionDate(Calendar.getInstance().getTime());
			CRUDController<Expense> cntr = new CRUDController<Expense>(application);
			cntr.save(usr, expenses, entityManager);
			exprow.put("status", "Rejected");
			String action = "Rejection of Expense";
			switch (expenses.getTransactionPurpose()) {
				case 1:
					exprow.put("transactionPurpose", "Buy");
					break;
				case 2:
					exprow.put("transactionPurpose", "Pay");
					break;
				case 3:
					exprow.put("transactionPurpose", "Deposit");
					break;
				case 4:
					exprow.put("transactionPurpose", "Withdrawal");
					break;
				case 5:
					exprow.put("transactionPurpose", "Provision");
					break;
				case 6:
					exprow.put("transactionPurpose", "Sell");
					break;
				case 7:
					exprow.put("transactionPurpose", "Receive");
					break;
				case 8:
					exprow.put("transactionPurpose", "Hire");
					break;
				default:
					exprow.put("transactionPurpose", "Buy");
					break;
			}
			exprow.put("expenseSpecifics", expenses.getSpecifics().getName());
			switch (expenses.getPaymentType()) {
				case 1:
					exprow.put("paymentType", "Credit Purchase");
					break;
				case 2:
					exprow.put("paymentType", "Cash Purchase");
					break;
				default:
					exprow.put("paymentType", "Credit Purchase");
					break;
			}
			exprow.put("vendor", expenses.getVendor().getName());
			exprow.put("noOfItems", String.valueOf(expenses.getNoOfItems()));
			exprow.put("unitCost", String.valueOf(expenses.getUnitCost()));
			exprow.put("totalCost", String.valueOf(expenses.getTotalCost()));
			exprow.put("expenseRemarks", useremail + "@" + txnRemarks);
			String expenseSupportingDoc = "";
			;
			if (expenses.getFileUrl() != null && expenses.getFileUrl() != "") {
				int lastCommaOccurence = expenses.getFileUrl().lastIndexOf(',');
				if (lastCommaOccurence != -1) {
					expenseSupportingDoc = expenses.getFileUrl().substring(lastCommaOccurence + 1,
							expenses.getFileUrl().length());
				} else {
					expenseSupportingDoc = expenses.getFileUrl();
				}
			}
			exprow.put("expenseSupportingDoc", expenseSupportingDoc);
			exprow.put("expenseTransactionUser", useremail);
			exprow.put("expenseTransactionNumber", expenses.getTxnNumber());
			exprow.put("expenseTransactionDate", mysqldtf.format(Calendar.getInstance().getTime()));
			auditDAO.saveAuditLogs(action, usr, expenses.getId(), Expense.class, ipAddress, exprow.toString(),
					entityManager);
			transaction.commit();
			TransactionViewResponse.action(expenses.getId(), "Rejected", null, null, txnRemarks, results);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			log.log(Level.INFO, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usr.getEmail(), usr.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result completeAccounting(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		Users usr = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode an = results.putArray("expenseAction");
			AuditLogs expenselogs = new AuditLogs();
			ObjectNode exprow = Json.newObject();
			String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
			if (ipAddress == null) {
				ipAddress = request.remoteAddress();
			}
			String useremail = json.findValue("useremail").asText();
			session.adding("email", useremail);
			usr = getUserInfo(request);
			String expenseId = json.findPath("expense").asText();
			String txnRemarks = json.findPath("txnRemarks").asText();
			Expense expenses = Expense.findById(Long.parseLong(expenseId));
			expenses.setStatus("Accounted");
			expenses.setRemarks(expenses.getRemarks() + "," + useremail + "@" + txnRemarks);
			expenses.setActionDate(Calendar.getInstance().getTime());
			CRUDController<Expense> cntr = new CRUDController<Expense>(application);
			cntr.save(usr, expenses, entityManager);
			exprow.put("status", "Accounted");
			String action = "Accounting of Expense";
			switch (expenses.getTransactionPurpose()) {
				case 1:
					exprow.put("transactionPurpose", "Buy");
					break;
				case 2:
					exprow.put("transactionPurpose", "Pay");
					break;
				case 3:
					exprow.put("transactionPurpose", "Deposit");
					break;
				case 4:
					exprow.put("transactionPurpose", "Withdrawal");
					break;
				case 5:
					exprow.put("transactionPurpose", "Provision");
					break;
				case 6:
					exprow.put("transactionPurpose", "Sell");
					break;
				case 7:
					exprow.put("transactionPurpose", "Receive");
					break;
				case 8:
					exprow.put("transactionPurpose", "Hire");
					break;
				default:
					exprow.put("transactionPurpose", "Buy");
					break;
			}
			exprow.put("expenseSpecifics", expenses.getSpecifics().getName());
			switch (expenses.getPaymentType()) {
				case 1:
					exprow.put("paymentType", "Credit Purchase");
					break;
				case 2:
					exprow.put("paymentType", "Cash Purchase");
					break;
				default:
					exprow.put("paymentType", "Credit Purchase");
					break;
			}
			exprow.put("vendor", expenses.getVendor().getName());
			exprow.put("noOfItems", String.valueOf(expenses.getNoOfItems()));
			exprow.put("unitCost", String.valueOf(expenses.getUnitCost()));
			exprow.put("totalCost", String.valueOf(expenses.getTotalCost()));
			exprow.put("expenseSupportingDoc", "");
			exprow.put("expenseTransactionUser", useremail);
			exprow.put("expenseTransactionNumber", expenses.getTxnNumber());
			exprow.put("expenseTransactionDate", mysqldtf.format(Calendar.getInstance().getTime()));
			exprow.put("expenseRemarks", useremail + "@" + txnRemarks);
			auditDAO.saveAuditLogs(action, usr, expenses.getId(), Expense.class, ipAddress, exprow.toString(),
					entityManager);
			transaction.commit();
			TransactionViewResponse.action(expenses.getId(), "Accounted", null, null, txnRemarks, results);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usr.getEmail(), usr.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result additionalApproval(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		Users usr = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode an = results.putArray("expenseAction");
			AuditLogs expenselogs = new AuditLogs();
			ObjectNode exprow = Json.newObject();
			String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
			if (ipAddress == null) {
				ipAddress = request.remoteAddress();
			}
			String useremail = json.findValue("useremail").asText();
			session.adding("email", useremail);
			usr = getUserInfo(request);
			String nextApprover = json.findValue("nextApprover").asText();
			String uploadedDoc = json.findValue("uploadedDoc").asText();
			session.adding("email", useremail);
			String expenseId = json.findPath("expense").asText();
			String txnRemarks = json.findPath("txnRemarks").asText();
			Expense expenses = Expense.findById(Long.parseLong(expenseId));
			if (uploadedDoc == "") {
				if (expenses.getFileUrl() == null) {
					expenses.setFileUrl(null);
				}
			} else {
				if (expenses.getFileUrl() == null) {
					expenses.setFileUrl(uploadedDoc);
				} else {
					expenses.setFileUrl(expenses.getFileUrl() + "," + uploadedDoc);
				}
			}
			expenses.setStatus("Initiated Additional Approval");
			expenses.setAdditionalApproverEmail(nextApprover);
			if (expenses.getNextLabelForwadedBy() != null) {
				expenses.setNextLabelForwadedBy(expenses.getNextLabelForwadedBy() + "," + useremail);
			} else {
				expenses.setNextLabelForwadedBy(useremail);
			}
			expenses.setRemarks(expenses.getRemarks() + "," + useremail + "@" + txnRemarks);
			expenses.setActionDate(Calendar.getInstance().getTime());
			CRUDController<Expense> cntr = new CRUDController<Expense>(application);
			cntr.save(usr, expenses, entityManager);
			exprow.put("status", "Initiated Additional Approval");
			String action = "Initiated Additional Approval For Expense";
			switch (expenses.getTransactionPurpose()) {
				case 1:
					exprow.put("transactionPurpose", "Buy");
					break;
				case 2:
					exprow.put("transactionPurpose", "Pay");
					break;
				case 3:
					exprow.put("transactionPurpose", "Deposit");
					break;
				case 4:
					exprow.put("transactionPurpose", "Withdrawal");
					break;
				case 5:
					exprow.put("transactionPurpose", "Provision");
					break;
				case 6:
					exprow.put("transactionPurpose", "Sell");
					break;
				case 7:
					exprow.put("transactionPurpose", "Receive");
					break;
				case 8:
					exprow.put("transactionPurpose", "Hire");
					break;
				default:
					exprow.put("transactionPurpose", "Buy");
					break;
			}
			exprow.put("expenseSpecifics", expenses.getSpecifics().getName());
			switch (expenses.getPaymentType()) {
				case 1:
					exprow.put("paymentType", "Credit Purchase");
					break;
				case 2:
					exprow.put("paymentType", "Cash Purchase");
					break;
				default:
					exprow.put("paymentType", "Credit Purchase");
					break;
			}
			exprow.put("vendor", expenses.getVendor().getName());
			exprow.put("noOfItems", String.valueOf(expenses.getNoOfItems()));
			exprow.put("unitCost", String.valueOf(expenses.getUnitCost()));
			exprow.put("totalCost", String.valueOf(expenses.getTotalCost()));
			exprow.put("expenseSupportingDoc", "");
			exprow.put("expenseTransactionUser", useremail);
			exprow.put("expenseTransactionNumber", expenses.getTxnNumber());
			exprow.put("expenseTransactionDate", mysqldtf.format(Calendar.getInstance().getTime()));
			exprow.put("expenseRemarks", useremail + "@" + txnRemarks);
			auditDAO.saveAuditLogs(action, usr, expenses.getId(), Expense.class, ipAddress, exprow.toString(),
					entityManager);
			transaction.commit();
			TransactionViewResponse.action(expenses.getId(), "Initiated Additional Approval", nextApprover, uploadedDoc,
					txnRemarks, results);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			log.log(Level.INFO, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usr.getEmail(), usr.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result sendMail(Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode results = Json.newObject();
		if (ConfigParams.getInstance().getIsMailOff(IdosConstants.MAIL_SYSTEM_OFF_KEY)) {
			return Results.ok(results);
		}
		JsonNode json = request.body().asJson();
		String expenseId = json.findPath("expense").asText();
		Expense expenses = Expense.findById(Long.parseLong(expenseId));
		try {
			String body = expenseMail
					.render(expenses.getUsers().getFullName(), expenses.getParticulars().getName(),
							expenses.getSpecifics().getName(), String.valueOf(expenses.getNoOfItems()),
							String.valueOf(expenses.getTotalCost()), expenses.getStatus(), ConfigParams.getInstance())
					.body();
			final String username = ConfigFactory.load().getString("smtp.user");
			Session session = emailsession;
			Email mail = new HtmlEmail();
			mail.setMailSession(session);
			mail.setFrom(username);
			mail.addTo(expenses.getUsers().getEmail());
			mail.setSubject(expenses.getStatus() + " Expense");
			mail.setSentDate(new Date());
			mail.setMsg(body);
			mail.send();
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "SendMail Email", "SendMail Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result getExpense(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = getUserInfo(request);
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode pan = result.putArray("particularsData");
			ArrayNode san = result.putArray("specificsData");
			ArrayNode van = result.putArray("vendorData");
			ArrayNode otheran = result.putArray("otherData");
			String expenseId = json.findValue("expense").asText();
			Expense expenses = Expense.findById(Long.parseLong(expenseId));
			List<Particulars> particulars = Particulars.list(entityManager, user.getOrganization());
			for (Particulars particularlist : particulars) {
				ObjectNode row = Json.newObject();
				row.put("id", particularlist.getId());
				row.put("name", particularlist.getName());
				row.put("partselcted", expenses.getParticulars().getId());
				row.put("partselctedName", expenses.getParticulars().getName());
				pan.add(row);
			}
			Particulars part = expenses.getParticulars();
			List<Specifics> specificList = part.getSpecifics();
			for (Specifics specifics : specificList) {
				ObjectNode row = Json.newObject();
				row.put("id", specifics.getId());
				row.put("name", specifics.getName());
				row.put("specselcted", expenses.getSpecifics().getId());
				san.add(row);
			}
			Specifics specific = expenses.getSpecifics();
			List<VendorSpecific> vendorsSpecifics = specific.getSpecificsVendors();
			for (VendorSpecific vendorSpecific : vendorsSpecifics) {
				ObjectNode row = Json.newObject();
				row.put("id", vendorSpecific.getVendorSpecific().getId());

				row.put("name", vendorSpecific.getVendorSpecific().getName());
				row.put("venselcted", expenses.getVendor().getId());
				van.add(row);
			}
			Double UnitCost = getunitCost(expenses.getParticulars(), expenses.getSpecifics(), expenses.getVendor(),
					entityManager);
			ObjectNode rows = Json.newObject();
			rows.put("unitCost", UnitCost);
			rows.put("noOfItems", String.valueOf(expenses.getNoOfItems()));
			rows.put("totalCost", String.valueOf(expenses.getTotalCost()));
			if (expenses.getFileUrl() != null) {
				rows.put("fileUrl", expenses.getFileUrl());
			} else {
				rows.put("fileUrl", "");
			}
			otheran.add(rows);
			transaction.commit();
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public static Double getunitCost(Particulars part, Specifics spec, Vendor ven, EntityManager entityManager) {
		log.log(Level.FINE, ">>>> Start");
		Double unitCost = 0.0;
		StringBuilder sbquery = new StringBuilder("");
		sbquery.append(
				"select obj from VendorSpecific obj where vendorSpecific='" + ven.getId() + "' and specificsVendors='"
						+ spec.getId() + "' and particulars='" + part.getId() + "' and obj.presentStatus=1");
		List<VendorSpecific> vendorSpecifics = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
		for (VendorSpecific vendorSpecific : vendorSpecifics) {
			unitCost = vendorSpecific.getUnitPrice();
		}
		return unitCost;
	}

	@Transactional
	public Result deleteExpenses(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String expenseIds = json.findValue("expenseIds").asText();
			String expenses[] = expenseIds.split(",");
			for (int i = 0; i < expenses.length; i++) {
				Expense exp = Expense.findById(Long.parseLong(expenses[i]));
				cntr.delete(exp, entityManager);
			}
			transaction.commit();
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "DeleteExpenses Email", "DeleteExpenses Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result expenses(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		Http.Session session = request.session();
		Users usrinfo = getUserInfo(request);
		String role = session.getOptional("role").orElse("");
		String allowedProcurement = String.valueOf(usrinfo.getAllowedProcurementRequest());
		return Results.ok(config.render(usrinfo.getFullName(), usrinfo.getEmail(), role, usrinfo.getOrganization(),
				allowedProcurement, usrinfo, ConfigParams.getInstance()));
	}

	@Transactional
	public Result expensesList(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Map<String, Object> criterias = new HashMap<String, Object>();
		Users usrinfo = null;
		try {
			usrinfo = getUserInfo(request);
			String role = session.getOptional("role").orElse("");
			List<Expense> expensedata = null;
			if (role.equals("CREATOR")) {
				criterias.clear();
				criterias.put("users.id", usrinfo.getId());
				criterias.put("createdBy.id", usrinfo.getId());
				criterias.put("branch.id", usrinfo.getBranch().getId());
				criterias.put("organization.id", usrinfo.getOrganization().getId());
				criterias.put("presentStatus", 1);
				expensedata = genericDAO.findByCriteria(Expense.class, criterias, "id", true, entityManager);
			}
			if (role.equals("APPROVER")) {
				criterias.clear();
				criterias.put("organization.id", usrinfo.getOrganization().getId());
				criterias.put("presentStatus", 1);
				expensedata = genericDAO.findByCriteria(Expense.class, criterias, "id", true, entityManager);
			}
			if (role.equals("ACCOUNTANT")) {
				criterias.clear();
				criterias.put("branch.id", usrinfo.getBranch().getId());
				criterias.put("organization.id", usrinfo.getOrganization().getId());
				criterias.put("presentStatus", 1);
				expensedata = genericDAO.findByCriteria(Expense.class, criterias, "id", true, entityManager);
			}
			JsonNode json = request.body().asJson();
			ArrayNode an = result.putArray("expenseData");
			if (expensedata != null) {
				if (expensedata.size() > 0) {
					for (Expense expenses : expensedata) {
						String expenseTxnRemarks = expenses.getRemarks()
								.substring(expenses.getRemarks().lastIndexOf('@') + 1, expenses.getRemarks().length());
						ObjectNode row = Json.newObject();
						row.put("id", expenses.getId());
						row.put("expenseCreator", expenses.getUsers().getEmail());
						row.put("expenseItem", expenses.getSpecifics().getName());
						row.put("expenseVendor", expenses.getVendor().getName());
						row.put("expenseQty", expenses.getNoOfItems());
						row.put("totalAmount", expenses.getTotalCost());
						row.put("expenseActionDate", idosdf.format(expenses.getActionDate()));
						row.put("expenseStatus", expenses.getStatus());
						row.put("expenseTxnRemarks", expenseTxnRemarks);
						if (role.equals("APPROVER")) {
							if (expenses.getStatus().equals("Initiated")
									|| expenses.getStatus().equals("Initiated Additional Approval")) {
								if (expenses.getStatus().equals("Initiated Additional Approval")) {
									row.put("nextApprover", expenses.getAdditionalApproverEmail());
									row.put("action", "AdditionalApproval");
								} else {
									row.put("action", "Approve,Reject");
								}
							} else {
								row.put("action", expenses.getStatus());
							}
							if (expenses.getFileUrl() != "" && expenses.getFileUrl() != null) {
								row.put("expensedocUrl", expenses.getFileUrl());
							} else {
								row.put("expensedocUrl", "");
							}
						}
						if (role.equals("ACCOUNTANT")) {
							if (expenses.getStatus().equals("Accounted")) {
								row.put("action", "Complete Accounting");
							} else {
								row.put("action", expenses.getStatus());
							}
							if (expenses.getFileUrl() != "" && expenses.getFileUrl() != null) {
								row.put("expensedocUrl", expenses.getFileUrl());
							} else {
								row.put("expensedocUrl", "");
							}
						}
						an.add(row);
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usrinfo.getEmail(), usrinfo.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}
}
