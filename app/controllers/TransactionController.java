package controllers;

import actor.CreatorActor;
import com.idos.util.*;
import controllers.Karvy.KarvyAuthorization;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import akka.stream.javadsl.*;
import akka.actor.*;
import java.util.logging.Level;
import com.typesafe.config.ConfigFactory;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.WebSocket;
import pojo.TransactionViewResponse;
import service.BranchBankService;
import service.BranchBankServiceImpl;
import service.BranchCashService;
import service.BranchCashServiceImpl;
import akka.NotUsed;
import views.html.creditNote;
import views.html.debitNote;

import javax.mail.Session;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import play.Application;
import javax.inject.Inject;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

public class TransactionController extends StaticController {

	private static final String WALKIN_ITEMS_HQL = "select a from BranchSpecifics a, SpecificsTransactionPurpose c where a.organization = c.organization and a.organization.id = ?1 and c.organization.id=?2 and a.branch.id= ?3 and c.transactionPurpose.id=?4 and a.specifics = c.specifics and a.specifics.accountCodeHirarchy like ?5 and a.specifics in (select b.specifics FROM UserRightSpecifics b where b.user.id=?6 and b.userRights.id=1) and a.presentStatus=1";

	private static final String CREDIT_DEBIT_TXN = "select obj from Transaction obj WHERE obj.transactionBranchOrganization.id= ?1 and obj.transactionBranch.id= ?2 and obj.transactionVendorCustomer.id= ?3 and obj.transactionPurpose.id in (?4) and obj.transactionRefNumber not in (select t1.linkedTxnRef from Transaction t1 where t1.transactionBranchOrganization.id= ?5 and t1.transactionBranch.id= ?6 and t1.transactionVendorCustomer.id= ?7 and t1.transactionPurpose.id=38 and t1.transactionStatus ='Accounted' and obj.presentStatus=1) and obj.transactionStatus ='Accounted' and obj.presentStatus=1 and obj.transactionDate between ?8 and ?9";

	private static final String INV_TRANSFER_TXN = "select obj from Transaction obj WHERE obj.transactionBranchOrganization.id= ?1 and obj.transactionBranch.id=?2 and obj.transactionToBranch.id= ?3 and obj.transactionPurpose.id=25 and obj.transactionStatus ='Accounted' and obj.presentStatus=1 and obj.typeIdentifier=1 and obj.transactionRefNumber not in (select t.linkedTxnRef from Transaction t WHERE t.transactionBranchOrganization.id= ?4 and t.transactionPurpose.id=25 and t.typeIdentifier=2 and t.linkedTxnRef is not null)";

	private static final String PENDING_INVOICES_HQL = "select obj from Transaction obj WHERE obj.transactionBranchOrganization.id = ?1 and obj.transactionBranch.id = ?2 and obj.transactionVendorCustomer.id = ?3 and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.transactionDate <= ?4 and obj.presentStatus=1 and obj.transactionRefNumber not in (select t1.linkedTxnRef from Transaction t1 where t1.transactionBranchOrganization.id = ?5 and obj.transactionBranch.id = ?6 and t1.transactionPurpose.id = 38 and t1.presentStatus = 1)";

	private static final String PENDING_VEND_PAYMENT_HQL = "select obj from Transaction obj WHERE obj.transactionBranchOrganization.id = ?1 and obj.transactionBranch.id = ?2 and obj.transactionVendorCustomer.id = ?3 and obj.linkedTxnRef = ?4 and obj.presentStatus=1 and obj.transactionStatus not in ('Rejected','Accounted')";

	private static final String ITEMS_BASED_ON_TYPE_JPQL = "select obj from VendorSpecific obj where vendorSpecific.id= ?1 and organization.id= ?2 and particulars.accountCode= ?3 and specificsVendors.gstTypeOfSupply= ?4 and presentStatus=1";

	private static final String ITEMS_JPQL = "select obj from VendorSpecific obj where vendorSpecific.id= ?1 and organization.id= ?2 and particulars.accountCode= ?3 and presentStatus=1";

	private static final String BRANCH_SPECIFIC_JPQL = "select DISTINCT obj from BranchSpecifics obj where organization.id= ?1 and specifics.id= ?2 and branch.id= ?3 and presentStatus=1";

	private static final String VENDOR_SPECIFIC_JPQL = "select obj from VendorSpecific obj where particulars.accountCode= ?1 and organization.id= ?2 and vendorSpecific.id= ?3 and presentStatus=1";
	private static Application application;
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	private Request request;

	// private Http.Session session = request.session();
	@Inject
	public TransactionController(Application application) {
		super(application);
		this.application = application;
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result getTxnItems(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		ArrayNode gettxnbnchan = result.putArray("allTxnBranchPurposeData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			String txnPurposeId = json.findValue("txnPurposeId").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			criterias.clear();
			criterias.put("user.id", user.getId());
			criterias.put("userRights.id", 1L);
			criterias.put("branch.presentStatus", 1);
			List<UserRightInBranch> userRightInBranches = genericDAO.findByCriteria(UserRightInBranch.class, criterias,
					entityManager);
			for (UserRightInBranch usrRgtBnch : userRightInBranches) {
				ObjectNode row = Json.newObject();
				row.put("id", usrRgtBnch.getBranch().getId());
				row.put("name", usrRgtBnch.getBranch().getName());
				row.put("gstin", usrRgtBnch.getBranch().getGstin() == null ? "" : usrRgtBnch.getBranch().getGstin());
				gettxnbnchan.add(row);
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getCurrentPettyCashAccount(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		ArrayNode bnchPtyCashAccountan = result.putArray("currentBranchPettyCashAccountData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			String transactionPrimId = json.findValue("transactionPrimId").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			Transaction transaction = Transaction.findById(IdosUtil.convertStringToLong(transactionPrimId));
			StringBuilder newsbquery = new StringBuilder("");
			newsbquery.append("select obj from BranchCashCount obj WHERE obj.branch.id='"
					+ transaction.getTransactionBranch().getId() + "' AND obj.organization.id='"
					+ transaction.getTransactionBranchOrganization().getId()
					+ "' and obj.presentStatus=1 ORDER BY obj.date desc");

			List<BranchCashCount> currentBranchCashCount = genericDAO.executeSimpleQueryWithLimit(newsbquery.toString(),
					entityManager, 1);
			if (currentBranchCashCount.size() > 0) {
				ObjectNode row = Json.newObject();
				row.put("currentBranchResultantPettyCashAmount", currentBranchCashCount.get(0).getResultantPettyCash());
				row.put("currentTransactionNetAmount", transaction.getNetAmount());
				bnchPtyCashAccountan.add(row);
			} else {
				ObjectNode row = Json.newObject();
				row.put("currentBranchResultantPettyCashAmount", "0.0");
				row.put("currentTransactionNetAmount", transaction.getNetAmount());
				bnchPtyCashAccountan.add(row);
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		return Results.ok(result);
	}

	@Transactional
	public Result vendorSpecialAdjustmentsProjects(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		ArrayNode vendSpecAdjustan = result.putArray("vendSpecAdjustData");
		ArrayNode projSpecAdjustan = result.putArray("projSpecAdjustData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			String txnPurposeId = json.findValue("txnPurposeId").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			criterias.clear();
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("adjustmentsAllowed", 1);
			criterias.put("presentStatus", 1);
			List<Vendor> adjustmentsAllowedVendor = genericDAO.findByCriteria(Vendor.class, criterias, entityManager);
			for (Vendor vend : adjustmentsAllowedVendor) {
				ObjectNode row = Json.newObject();
				row.put("id", vend.getId());
				row.put("name", vend.getName());
				vendSpecAdjustan.add(row);
			}
			criterias.clear();
			criterias.put("user.id", user.getId());
			criterias.put("userRights.id", 1L);
			criterias.put("project.presentStatus", 1);
			criterias.put("project.organization.id", user.getOrganization().getId());
			criterias.put("presentStatus", 1);
			List<UserRightForProject> userRightForProjects = genericDAO.findByCriteria(UserRightForProject.class,
					criterias, entityManager);
			for (UserRightForProject usrRgtPjct : userRightForProjects) {
				ObjectNode row = Json.newObject();
				row.put("id", usrRgtPjct.getProject().getId());
				row.put("name", usrRgtPjct.getProject().getName());
				projSpecAdjustan.add(row);
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		return Results.ok(result);
	}

	@Transactional
	public Result amountName(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		ArrayNode amountNamean = result.putArray("amountNameData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			String txnPurposeId = json.findValue("txnPurposeId").asText();
			String txnVendorId = json.findValue("txnVendorId").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			Vendor vend = Vendor.findById(IdosUtil.convertStringToLong(txnVendorId));
			ObjectNode row = Json.newObject();
			if (vend.getAdjustmentsAllowed() != null) {
				if (vend.getAdjustmentsAllowed() == 1) {
					String adjustmentName = "";
					if (vend.getAdjustmentsName() != null) {
						adjustmentName = vend.getAdjustmentsName();
					}
					row.put("adjustmentName", adjustmentName);
				}
			}
			Double adjustmentAmount = 0.0;
			if (vend.getAvailableSpecAdjAmount() != null) {
				adjustmentAmount = vend.getAvailableSpecAdjAmount();
			}
			row.put("adjustmentAmount", String.valueOf(adjustmentAmount));
			String kLibrary = "";
			if (vend.getVendorGroup() != null) {
				if (vend.getVendorGroup().getKnowledgeLibrary() != null) {
					kLibrary = vend.getVendorGroup().getKnowledgeLibrary();
				}
			}
			row.put("kLibrary", kLibrary);
			amountNamean.add(row);
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getProcurementRequest(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");

		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		ArrayNode procurementan = result.putArray("procurementData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("usermail").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			// give bach procurement request count and procurementrequest details for the
			// user who is having right in branch as well as specifics
			criterias.clear();
			criterias.put("procBranch.id", user.getBranch().getId());
			criterias.put("procOrganization.id", user.getOrganization().getId());
			criterias.put("procurementStatus", "Procurement Request");
			criterias.put("presentStatus", 1);
			List<ProcurementRequest> procRequestForUser = genericDAO.findByCriteria(ProcurementRequest.class, criterias,
					entityManager);
			int count = 0;
			if (procRequestForUser.size() > 0) {
				for (ProcurementRequest procRequest : procRequestForUser) {
					// check for if user has creator right in procurement request branch
					criterias.clear();
					criterias.put("user.id", user.getId());
					criterias.put("userRights.id", 1L);
					criterias.put("branch.id", procRequest.getProcBranch().getId());
					criterias.put("organization.id", procRequest.getProcOrganization().getId());
					criterias.put("presentStatus", 1);
					UserRightInBranch doesuserRghtInBranch = genericDAO.getByCriteria(UserRightInBranch.class,
							criterias, entityManager);
					String procurementcount = "";
					if (doesuserRghtInBranch != null) {
						// check for if user has creator right in procurement request specifics
						criterias.clear();
						criterias.put("user.id", user.getId());
						criterias.put("userRights.id", 1L);
						criterias.put("specifics.id", procRequest.getSpecifics().getId());
						criterias.put("particulars.id", procRequest.getParticular().getId());
						criterias.put("presentStatus", 1);
						UserRightSpecifics doesUserHasRightInSpecifics = genericDAO
								.getByCriteria(UserRightSpecifics.class, criterias, entityManager);
						if (doesUserHasRightInSpecifics != null) {
							// add json data
							count++;
							procurementcount = String.valueOf(count);
							ObjectNode row = Json.newObject();
							row.put("procurementRequestId", procRequest.getId());
							row.put("procurementDetails", "ProcuredBy:" + procRequest.getCreatedBy().getEmail()
									+ "#for:" + procRequest.getSpecifics().getName());
							row.put("count", procurementcount);
							procurementan.add(row);
						}
					}
				}
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result userAuditorRemarks(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		ArrayNode txnremarksan = result.putArray("txnRemarks");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("usermail").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			String transactionPrimaryId = json.findValue("transactionPrimaryId").asText();
			Transaction txn = Transaction.findById(IdosUtil.convertStringToLong(transactionPrimaryId));
			String txnRmarks = json.findValue("txnRmarks").asText();
			if (txnRmarks != null && !txnRmarks.equals("")) {
				if (txn.getRemarks() != null) {
					txn.setRemarks(txn.getRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
				} else {
					txn.setRemarks(user.getEmail() + "#" + txnRmarks);
				}
			}
			if (txn.getAuditorUserRemarksDate() != null) {
				txn.setAuditorUserRemarksDate(
						txn.getAuditorUserRemarksDate() + "#" + idosdf.format(Calendar.getInstance().getTime()));
			} else {
				txn.setAuditorUserRemarksDate(idosdf.format(Calendar.getInstance().getTime()));
			}
			entityManager.merge(txn);
			transaction.commit();
			ObjectNode row = Json.newObject();
			row.put("remarks", txn.getRemarks());
			txnremarksan.add(row);
		} catch (Exception ex) {
			reportException(entityManager, transaction, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, transaction, user, th, result);
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getTxnItemParent(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		ArrayNode advanceTxnItemParentData = result.putArray("advanceTxnItemParentData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			String txnAdvanceItemId = (json.findValue("txnAdvanceItemId") == null
					|| "".equals(json.findValue("txnAdvanceItemId").asText())) ? null
							: json.findValue("txnAdvanceItemId").asText();
			Specifics specf = Specifics.findById(IdosUtil.convertStringToLong(txnAdvanceItemId));
			String txnVendCustEntityId = (json.findValue("txnCustVendId") == null
					|| "".equals(json.findValue("txnCustVendId").asText())) ? null
							: json.findValue("txnCustVendId").asText();
			int txnTypeOfSupply = json.findValue("txnTypeOfSupply") == null ? 0
					: json.findValue("txnTypeOfSupply").asInt();
			Long brachId = json.findValue("brachId").asLong();
			String txnDestinGstin = json.findValue("txnDestGstinCls").asText();
			Long txnPlaceOfSupplyId = json.findValue("destinGstinId") == null ? 0
					: json.findValue("destinGstinId").asLong();
			transaction.commit();
			ObjectNode row = Json.newObject();
			if (specf.getParentSpecifics() != null && !specf.getParentSpecifics().equals("")) {
				row.put("itemParentName", "Immediate Parent: " + specf.getParentSpecifics().getName());
			} else {
				row.put("itemParentName", "Immediate Parent: " + specf.getParticularsId().getName());
			}
			if (txnVendCustEntityId != null && txnVendCustEntityId != "") { // it means customer is selected from drop
																			// downlist
				StringBuilder sbqueryvedspecf = new StringBuilder("");
				Vendor vendCust = Vendor.findById(IdosUtil.convertStringToLong(txnVendCustEntityId));
				sbqueryvedspecf.append("select obj from VendorSpecific obj WHERE obj.organization='"
						+ user.getOrganization().getId() + "' and obj.specificsVendors='" + specf.getId()
						+ "' and obj.vendorSpecific='" + vendCust.getId() + "' and obj.presentStatus=1");
				List<VendorSpecific> vendorSpecf = genericDAO.executeSimpleQuery(sbqueryvedspecf.toString(),
						entityManager);
				Double existingAdvance = 0.0;
				if (vendorSpecf.size() > 0) {
					VendorSpecific vendorSpecficsForAdvance = vendorSpecf.get(0);
					if (vendorSpecficsForAdvance.getAdvanceMoney() != null) {
						existingAdvance = vendorSpecficsForAdvance.getAdvanceMoney();
					}
				}
				if (vendCust.getType() == 1) {
					VendorBranchWiseAdvBalance itemBrachWiseAdv = VendorBranchWiseAdvBalance
							.getAdvAmountForItem(
									entityManager, user.getOrganization().getId(), Long.parseLong(txnVendCustEntityId),
									brachId, txnTypeOfSupply, txnPlaceOfSupplyId,
									Long.parseLong(txnAdvanceItemId));
					if (itemBrachWiseAdv != null) {
						existingAdvance += itemBrachWiseAdv.getAdvanceAmount();
					}
				} else {
					CustomerBranchWiseAdvBalance itemBrachWiseAdv = CustomerBranchWiseAdvBalance
							.getAdvAmountForItem(
									entityManager, user.getOrganization().getId(), Long.parseLong(txnVendCustEntityId),
									brachId, txnTypeOfSupply, Long.parseLong(txnDestinGstin),
									Long.parseLong(txnAdvanceItemId));
					if (itemBrachWiseAdv != null) {
						existingAdvance += itemBrachWiseAdv.getAdvanceAmount();
					}
				}
				row.put("vendCustExistingAdvance",
						String.valueOf(IdosConstants.decimalFormat.format(existingAdvance)));
			} else { // if walk-in customer its exisitng adv is null
				row.put("vendCustExistingAdvance", 0.0);
			}
			advanceTxnItemParentData.add(row);
		} catch (Exception ex) {
			reportException(entityManager, transaction, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, transaction, user, th, result);
		}
		return Results.ok(result);
	}

	@Transactional
	public Result procureRequest(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		ArrayNode txnremarksan = result.putArray("txnRemarks");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("usermail").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			String procureItem = json.findValue("procureItem").asText();
			String itemToProcureUnit = json.findValue("procureItemUnit").asText();

			String itemToProcureRemarks = json.findValue("procureItemRemarks").asText();
			ProcurementRequest procRequest = new ProcurementRequest();
			Specifics procSpecf = Specifics.findById(IdosUtil.convertStringToLong(procureItem));
			procRequest.setSpecifics(procSpecf);
			procRequest.setParticular(procSpecf.getParticularsId());
			procRequest.setProcBranch(user.getBranch());
			procRequest.setProcOrganization(user.getOrganization());
			procRequest.setNoOfUnits(IdosUtil.convertStringToInt(itemToProcureUnit));
			if (itemToProcureRemarks != null && !itemToProcureRemarks.equals("")) {
				procRequest.setProcurementRemarks(user.getEmail() + "#" + itemToProcureRemarks);
			}
			procRequest.setProcurementStatus("Procurement Request");
			genericDAO.saveOrUpdate(procRequest, user, entityManager);
			transaction.commit();
			// Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
			// Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
			// for (int i = 0; i < keyArray.length; i++) {
			// StringBuilder sbquery = new StringBuilder("");
			// sbquery.append(
			// "select obj from Users obj WHERE obj.email ='" + keyArray[i] + "' and
			// obj.presentStatus=1");
			// List<Users> orgusers = genericDAO.executeSimpleQuery(sbquery.toString(),
			// entityManager);
			// if (!orgusers.isEmpty()
			// && orgusers.get(0).getOrganization().getId() ==
			// user.getOrganization().getId()) {
			// orgtxnregistereduser.put(keyArray[i].toString(),
			// CreatorActor.expenseregistrered.get(keyArray[i]));
			// }
			// }
			// CreatorActor.addProcurementRequest(procRequest.getProcurementStatus());
			result.put("txnType", "sellExpenseTxn");
			result.put("procurementStatus", procRequest.getProcurementStatus());
		} catch (Exception ex) {
			reportException(entityManager, transaction, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, transaction, user, th, result);
		}
		return Results.ok(result);
	}

	@Transactional
	public Result gettxnOutstandings(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode getinvoiceoutstandingan = result.putArray("invoiceOutstandingsData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			Map<String, Object> criterias = new HashMap<String, Object>(4);
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			String txnEntityId = json.findValue("txnEntityId").asText();
			String txnVendCust = json.findValue("txnVendCust").asText();
			long txnPurposeId = json.findValue("txnPurposeId").asLong();
			Long openingBalBillId = (json.findValue("openingBalBillId") == null
					|| "".equals(json.findValue("openingBalBillId").asText())) ? null
							: json.findValue("openingBalBillId").asLong();
			Long txnBranchId = (json.findValue("txnBranchId") == null
					|| "".equals(json.findValue("txnBranchId").asText())) ? null
							: json.findValue("txnBranchId").asLong();
			user = getUserInfo(request);
			Vendor txnVendCustEntity = null;
			Branch txnBranch = null;
			if (txnBranchId != null) {
				txnBranch = Branch.findById(txnBranchId);
			}
			txnVendCustEntity = Vendor.findById(IdosUtil.convertStringToLong(txnVendCust));
			if (txnEntityId.equals("-1")) { // It is OB entered on customer/vendor screen, which need to be adjusted
				if (openingBalBillId != null) {
					if (txnVendCustEntity.getType() == IdosConstants.VENDOR) {
						VendorBillwiseOpBalance openBal = VendorBillwiseOpBalance.findById(openingBalBillId);
						ObjectNode row = Json.newObject();
						row.put("openingBalBillId", openBal.getId());
						row.put("gross", openBal.getBillAmount());
						row.put("net", IdosConstants.decimalFormat.format(openBal.getBillAmount()));
						row.put("netDesc", openBal.getBillNo());
						row.put("custAdvanceMoney", "");
						row.put("amountPaid", (openBal.getBillAmount() - openBal.getOpeningBalance()));
						row.put("amountNotPaid", openBal.getOpeningBalance());// OB is amount which is carried forward
																				// from previous year, it is not for
																				// particular item, it is credit given
																				// to that customer/vendor
						row.put("returns", 0.00);
						row.put("txnRefFlowInProgress", "Amount To Be Approved: 0.00");
						getinvoiceoutstandingan.add(row);
					} else {
						CustomerBillwiseOpBalance openBal = CustomerBillwiseOpBalance.findById(openingBalBillId);

						ObjectNode row = Json.newObject();
						row.put("openingBalBillId", openBal.getId());
						row.put("gross", openBal.getBillAmount());
						row.put("net", openBal.getBillAmount());
						row.put("netDesc", openBal.getBillNo());
						row.put("custAdvanceMoney", "");
						row.put("amountPaid", (openBal.getBillAmount() - openBal.getOpeningBalance()));
						row.put("amountNotPaid", openBal.getOpeningBalance()); // OB is amount which is carried forward
																				// from previous year, it is not for
																				// particular item, it is credit given
																				// to that customer/vendor
						row.put("returns", 0);
						row.put("txnRefFlowInProgress", "Amount To Be Approved:" + 0);
						getinvoiceoutstandingan.add(row);
					}
				} else {
					/// Set Pending amount branch wise
					if (txnBranch != null) {
						criterias.clear();
						criterias.put("organization.id", user.getOrganization().getId());
						criterias.put("branch.id", txnBranch.getId());
						criterias.put("vendor.id", txnVendCustEntity.getId());
						criterias.put("presentStatus", 1);
						BranchVendors branchVendors = genericDAO.getByCriteria(BranchVendors.class, criterias,
								entityManager);
						if (branchVendors != null) {
							if (branchVendors.getOpeningBalance() != null && branchVendors.getOpeningBalance() > 0) {
								double previousPaidAmount = VendorBillwiseOpBalance.getPaidOpeningBalanceAmount(
										entityManager, txnBranch.getOrganization().getId(), txnVendCustEntity.getId(),
										txnBranch.getId(), IdosConstants.PAY_VENDOR_SUPPLIER, genericDAO,
										IdosConstants.TXN_TYPE_OPENING_BALANCE_VEND);
								ObjectNode row = Json.newObject();
								row.put("gross", branchVendors.getOpeningBalance());
								row.put("net",
										IdosConstants.decimalFormat.format(branchVendors.getOriginalOpeningBalance()));
								row.put("netDesc", "Opening Balance");
								row.put("custAdvanceMoney", "");
								row.put("amountPaid", IdosConstants.decimalFormat.format(previousPaidAmount));
								row.put("amountNotPaid", IdosConstants.decimalFormat
										.format(branchVendors.getOriginalOpeningBalance() - previousPaidAmount)); // OB
																													// is
																													// amount
																													// which
																													// is
																													// carried
																													// forward
																													// from
																													// previous
																													// year,
																													// it
																													// is
																													// not
																													// for
																													// particular
																													// item,
																													// it
																													// is
																													// credit
																													// given
																													// to
																													// that
																													// customer/vendor
								row.put("returns", 0.00);
								row.put("txnRefFlowInProgress", "Amount To Be Approved: 0.00");
								getinvoiceoutstandingan.add(row);
							}
						}
					}
				}
			} else {
				ObjectNode row = Json.newObject();
				Transaction txnOutstandings = Transaction.findById(IdosUtil.convertStringToLong(txnEntityId));
				Double amountPaid = null;
				Double amountNotPaid = null;
				Double returns = null;
				String txnRefFlowInProgress = "";
				String txnRefDiscountInProgress = "";
				double netAmount = 0.0;
				if (txnOutstandings.getTransactionPurpose().getId() == IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER) {
					Double amountRefund = TransactionItems.findByAmountRefund(entityManager,
							user.getOrganization().getId(), txnOutstandings.getTransactionBranch().getId(),
							txnOutstandings.getTransactionRefNumber());
					Double taxReturned = TransactionItems.findByTDSRefund(entityManager, user.getOrganization().getId(),
							txnOutstandings.getTransactionBranch().getId(), txnOutstandings.getTransactionRefNumber());
					if (txnOutstandings.getWithholdingTax() != null
							&& (txnOutstandings.getWithholdingTax() - taxReturned) > 0) {
						row.put("withholdingAmount",
								IdosConstants.decimalFormat.format(txnOutstandings.getWithholdingTax() - taxReturned));
					} else {
						row.put("withholdingAmount", "");
					}
					netAmount = txnOutstandings.getNetAmount() == null ? 0.0 : txnOutstandings.getNetAmount();

					if ((netAmount - amountRefund) > 0) {
						row.put("netAmount", IdosConstants.decimalFormat.format(netAmount - amountRefund));
					} else {
						row.put("netAmount", "");
					}
					amountPaid = 0.0;
					amountNotPaid = 0.0;
					returns = 0.0;
					txnRefFlowInProgress = "";
				} else {
					criterias.clear();
					criterias.put("vendorSpecific.id", txnVendCustEntity.getId());
					criterias.put("specificsVendors.id", txnOutstandings.getTransactionSpecifics().getId());
					criterias.put("organization.id", user.getOrganization().getId());
					criterias.put("presentStatus", 1);
					VendorSpecific customerTxnSpecifics = genericDAO.getByCriteria(VendorSpecific.class, criterias,
							entityManager);

					row.put("gross", txnOutstandings.getGrossAmount());
					row.put("net", IdosConstants.decimalFormat.format(txnOutstandings.getNetAmount()));
					row.put("netDesc", txnOutstandings.getNetAmountResultDescription() == null ? ""
							: txnOutstandings.getNetAmountResultDescription());
					if (customerTxnSpecifics != null) {
						if (customerTxnSpecifics.getAdvanceMoney() != null) {
							row.put("custAdvanceMoney",
									IdosConstants.decimalFormat.format(customerTxnSpecifics.getAdvanceMoney()));
						} else {
							row.put("custAdvanceMoney", "");
						}
					} else {
						row.put("custAdvanceMoney", "");
					}
					if (txnOutstandings.getTransactionPurpose()
							.getId() == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER) {
						if (txnOutstandings.getCustomerNetPayment() != null) {
							amountPaid = txnOutstandings.getCustomerNetPayment();
							amountNotPaid = txnOutstandings.getNetAmount() - txnOutstandings.getCustomerNetPayment();
						} else {
							amountPaid = 0.0;
							amountNotPaid = txnOutstandings.getNetAmount();
						}
						if (txnOutstandings.getSalesReturnAmount() != null) {
							returns = txnOutstandings.getSalesReturnAmount();
							amountNotPaid = amountNotPaid - returns;
						} else if (txnOutstandings.getSalesReturnAmount() == null) {
							returns = 0.0;
						}
					} else if (txnOutstandings.getTransactionPurpose()
							.getId() == IdosConstants.BUY_ON_CREDIT_PAY_LATER) {
						if (txnOutstandings.getVendorNetPayment() != null) {
							amountPaid = txnOutstandings.getVendorNetPayment();
							amountNotPaid = txnOutstandings.getNetAmount() - txnOutstandings.getVendorNetPayment();
						} else {
							amountPaid = 0.0;
							amountNotPaid = txnOutstandings.getNetAmount();
						}
						if (txnOutstandings.getPurchaseReturnAmount() != null) {
							returns = txnOutstandings.getPurchaseReturnAmount();
							amountNotPaid = amountNotPaid - returns;
						}
						if (txnOutstandings.getPurchaseReturnAmount() == null) {
							returns = 0.0;
						}
						ArrayList inparams = new ArrayList(4);
						inparams.add(txnOutstandings.getTransactionBranchOrganization().getId());
						inparams.add(txnOutstandings.getTransactionRefNumber());

						String cssbquery = "select SUM(obj.netAmount), sum(obj.availableDiscountAmountForTxn) from Transaction obj WHERE obj.transactionBranchOrganization.id = ?1 AND obj.linkedTxnRef= ?2 AND obj.transactionStatus!='Accounted' AND obj.transactionStatus!='Rejected' and obj.presentStatus=1";
						List<Object[]> txnInProgress = genericDAO.queryWithParamsNameGeneric(cssbquery, entityManager,
								inparams);
						if (txnInProgress.size() > 0) {
							Object obj = txnInProgress.get(0)[0];
							if (obj != null) {
								txnRefFlowInProgress = "Amount To Be Approved:" + obj.toString();
							}
							obj = txnInProgress.get(0)[1];
							if (obj != null) {
								txnRefDiscountInProgress = "Discount To Be Approved:" + obj.toString();
							}
						}

						/*
						 * ArrayList inparams = new ArrayList(4);
						 * inparams.add(txnOutstandings.getTransactionBranchOrganization().getId());
						 * inparams.add(txnOutstandings.getTransactionBranch().getId());
						 * inparams.add(txnOutstandings.getTransactionVendorCustomer().getId());
						 * inparams.add(txnOutstandings.getTransactionRefNumber()); List<Transaction>
						 * pendingPayVendorTxnList =
						 * genericDAO.queryWithParams(PENDING_VEND_PAYMENT_HQL, entityManager,
						 * inparams); Double pendingNetAmount =0.0; for (Transaction pendingPayVendorTxn
						 * : pendingPayVendorTxnList) { pendingNetAmount -=
						 * pendingPayVendorTxn.getNetAmount() == null ? 0.0 :
						 * pendingPayVendorTxn.getNetAmount();
						 * if(pendingPayVendorTxn.getAvailableDiscountAmountForTxn() != null){
						 * pendingNetAmount -= pendingPayVendorTxn.getAvailableDiscountAmountForTxn(); }
						 * } txnRefFlowInProgress = "Amount To Be Approved: " + pendingNetAmount;
						 */
					}
				}

				row.put("amountPaid", IdosConstants.decimalFormat.format(amountPaid));
				row.put("amountNotPaid", IdosConstants.decimalFormat.format(amountNotPaid));
				row.put("returns", IdosConstants.decimalFormat.format(returns));
				row.put("txnRefFlowInProgress", txnRefFlowInProgress);
				row.put("txnRefDiscountInProgress", txnRefDiscountInProgress);
				getinvoiceoutstandingan.add(row);
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getAllIncomeItems(Request request) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode getcreditcustvenditemsan = result.putArray("txnItemsCustomerVendorsData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			long txnPurposeId = json.findValue("txnPurposeId").asLong();
			String txnPurposeText = json.findValue("txnPurposeText").asText();
			Long txnBranchId = json.findValue("branchid") == null ? null : json.findValue("branchid").asLong();
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			if (txnBranchId == null) {
				txnBranchId = user.getBranch().getId();
			}
			Query query = entityManager.createQuery(WALKIN_ITEMS_HQL);
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, user.getOrganization().getId());
			query.setParameter(3, txnBranchId);
			query.setParameter(4, txnPurposeId);
			query.setParameter(5, "/1%");
			query.setParameter(6, user.getId());
			List<BranchSpecifics> specificsList = query.getResultList();
			for (BranchSpecifics userHasRightForSpecf : specificsList) {
				ObjectNode row = Json.newObject();
				row.put("id", userHasRightForSpecf.getSpecifics().getId());
				row.put("name", userHasRightForSpecf.getSpecifics().getName());
				row.put("accountCode", userHasRightForSpecf.getSpecifics().getParticularsId().getAccountCode());
				if (userHasRightForSpecf.getSpecifics().getGstItemCategory() != null) {
					row.put("category", userHasRightForSpecf.getSpecifics().getGstItemCategory());
				} else {
					row.put("category", "");
				}
				row.put("iseditable", userHasRightForSpecf.getSpecifics().getIsTransactionEditable() == null ? 0
						: userHasRightForSpecf.getSpecifics().getIsTransactionEditable());
				row.put("isCombinationSales", userHasRightForSpecf.getSpecifics().getIsCombinationSales() == null ? 0
						: userHasRightForSpecf.getSpecifics().getIsCombinationSales());
				getcreditcustvenditemsan.add(row);
			}

		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> End ");
		return Results.ok(result);
	}

	/* Not in use */
	@Transactional
	public Result getAllIncomeItemsOld(Request request) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode getcreditcustvenditemsan = result.putArray("txnItemsCustomerVendorsData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			// String useremail = json.findValue("useremail").asText();
			String txnPurposeId = json.findValue("txnPurposeId").asText();
			String txnPurposeText = json.findValue("txnPurposeText").asText();
			// session.adding("email", useremail);
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("transactionPurpose.id", Long.parseLong(txnPurposeId));
			criterias.put("presentStatus", 1);
			List<SpecificsTransactionPurpose> txnPurposeSpecifics = genericDAO
					.findByCriteria(SpecificsTransactionPurpose.class, criterias, entityManager);
			for (SpecificsTransactionPurpose txnPurposeSpecf : txnPurposeSpecifics) {
				ObjectNode row = Json.newObject();
				criterias.clear();
				criterias.put("user.id", user.getId());
				criterias.put("userRights.id", 1L);
				criterias.put("specifics.id", txnPurposeSpecf.getSpecifics().getId());
				criterias.put("particulars.id", txnPurposeSpecf.getSpecifics().getParticularsId().getId());
				criterias.put("presentStatus", 1);
				UserRightSpecifics userHasRightForSpecf = genericDAO.getByCriteria(UserRightSpecifics.class, criterias,
						entityManager);
				if (userHasRightForSpecf != null) {
					criterias.clear();
					criterias.put("branch.id", user.getBranch().getId());
					criterias.put("organization.id", userHasRightForSpecf.getUser().getOrganization().getId());
					criterias.put("specifics.id", userHasRightForSpecf.getSpecifics().getId());
					criterias.put("presentStatus", 1);
					BranchSpecifics bnchSpecf = genericDAO.getByCriteria(BranchSpecifics.class, criterias,
							entityManager);
					if (bnchSpecf != null) {
						row.put("id", userHasRightForSpecf.getSpecifics().getId());
						row.put("name", userHasRightForSpecf.getSpecifics().getName());
						row.put("accountCode", userHasRightForSpecf.getSpecifics().getParticularsId().getAccountCode());
						if (userHasRightForSpecf.getSpecifics().getGstItemCategory() != null) {
							row.put("category", userHasRightForSpecf.getSpecifics().getGstItemCategory());
						} else {
							row.put("category", "");
						}
						row.put("iseditable", userHasRightForSpecf.getSpecifics().getIsTransactionEditable() == null ? 0
								: userHasRightForSpecf.getSpecifics().getIsTransactionEditable());
						row.put("isCombinationSales",
								userHasRightForSpecf.getSpecifics().getIsCombinationSales() == null ? 0
										: userHasRightForSpecf.getSpecifics().getIsCombinationSales());
						getcreditcustvenditemsan.add(row);
					}
				}
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> End ");
		return Results.ok(result);
	}

	@Transactional
	public Result getCreditCustomerVendorItems(Request request) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode getcreditcustvenditemsan = result.putArray("txnItemsCustomerVendorsData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			long txnPurposeId = json.findValue("txnPurposeId").asLong();
			// String txnPurposeText=json.findValue("txnPurposeText").asText();
			String custVendEntityId = json.findValue("custVendEntityId") == null ? "0"
					: json.findValue("custVendEntityId").asText();
			Long txnBranchID = json.findValue("txnBranchID") == null ? 0l : json.findValue("txnBranchID").asLong();
			int itemType = json.findValue("typeOfSupplyForItemType") == null ? 0
					: json.findValue("typeOfSupplyForItemType").asInt();
			log.log(Level.INFO, "branch id=" + txnBranchID);
			// session.adding("email", useremail);
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized(result);
			}
			Vendor custVend = Vendor.findById(IdosUtil.convertStringToLong(custVendEntityId));
			Branch branch = Branch.findById(txnBranchID);
			if (custVend == null || branch == null) {
				return Results.ok(result);
			}
			if ((IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER == txnPurposeId
					|| IdosConstants.SALES_RETURNS == txnPurposeId
					|| IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW == txnPurposeId
					|| IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER == txnPurposeId
					|| IdosConstants.PREPARE_QUOTATION == txnPurposeId || IdosConstants.PROFORMA_INVOICE == txnPurposeId
					|| IdosConstants.CREDIT_NOTE_CUSTOMER == txnPurposeId
					|| IdosConstants.DEBIT_NOTE_CUSTOMER == txnPurposeId || IdosConstants.CANCEL_INVOICE == txnPurposeId
					|| IdosConstants.REFUND_ADVANCE_RECEIVED == txnPurposeId) && (custVend != null)) {
				ArrayList params = new ArrayList();
				params.add(1000000000000000000L);
				params.add(user.getOrganization().getId());
				params.add(custVend.getId());
				List<VendorSpecific> creditCustomesItems = genericDAO.queryWithParamsName(VENDOR_SPECIFIC_JPQL,
						entityManager, params);
				for (VendorSpecific creditcustitems : creditCustomesItems) {
					ArrayList paramBrchSpecific = new ArrayList();
					paramBrchSpecific.add(user.getOrganization().getId());
					paramBrchSpecific.add(creditcustitems.getSpecificsVendors().getId());
					paramBrchSpecific.add(branch.getId());
					List<BranchSpecifics> branchSpecificList = genericDAO.queryWithParamsName(BRANCH_SPECIFIC_JPQL,
							entityManager, paramBrchSpecific);
					if (branchSpecificList != null && branchSpecificList.size() > 0) {
						ObjectNode row = Json.newObject();
						row.put("id", creditcustitems.getSpecificsVendors().getId());
						String description = creditcustitems.getSpecificsVendors().getName();
						if (creditcustitems.getSpecificsVendors().getInvoiceItemDescription1() != null) {
							description += " " + creditcustitems.getSpecificsVendors().getInvoiceItemDescription1();
						}
						if (creditcustitems.getSpecificsVendors().getInvoiceItemDescription2() != null) {
							description += " " + creditcustitems.getSpecificsVendors().getInvoiceItemDescription2();
						}
						if (creditcustitems.getSpecificsVendors().getGstItemCategory() != null) {
							row.put("category", creditcustitems.getSpecificsVendors().getGstItemCategory());
						} else {
							row.put("category", "");
						}
						row.put("name", description);
						row.put("iseditable",
								creditcustitems.getSpecificsVendors().getIsTransactionEditable() == null ? 0
										: creditcustitems.getSpecificsVendors().getIsTransactionEditable());
						row.put("isCombinationSales",
								creditcustitems.getSpecificsVendors().getIsCombinationSales() == null ? 0
										: creditcustitems.getSpecificsVendors().getIsCombinationSales());
						getcreditcustvenditemsan.add(row);
					}
				}
			} else if (IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER == txnPurposeId
					|| IdosConstants.PURCHASE_RETURNS == txnPurposeId
					|| IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY == txnPurposeId
					|| IdosConstants.BUY_ON_CREDIT_PAY_LATER == txnPurposeId
					|| IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT == txnPurposeId
					|| IdosConstants.PURCHASE_ORDER == txnPurposeId || IdosConstants.CREDIT_NOTE_VENDOR == txnPurposeId
					|| IdosConstants.DEBIT_NOTE_VENDOR == txnPurposeId
					|| IdosConstants.CREATE_PURCHASE_ORDER == txnPurposeId) {
				String query = null;
				ArrayList params = null;
				if (itemType == 4 || itemType == 5) {
					params = new ArrayList();
					params.add(custVend.getId());
					params.add(user.getOrganization().getId());
					params.add(2000000000000000000L);
					if (itemType == 4) {
						params.add(IdosConstants.GST_GOODS_TEXT);
					} else if (itemType == 5) {
						params.add(IdosConstants.GST_SERVICES_TEXT);
					}
					query = ITEMS_BASED_ON_TYPE_JPQL;
				} else {
					params = new ArrayList();
					params.add(custVend.getId());
					params.add(user.getOrganization().getId());
					params.add(2000000000000000000L);
					query = ITEMS_JPQL;
				}
				List<VendorSpecific> creditCustomesItems = genericDAO.queryWithParamsName(query, entityManager, params);
				for (VendorSpecific creditcustitems : creditCustomesItems) {
					ArrayList paramList = new ArrayList();
					paramList.add(user.getOrganization().getId());
					paramList.add(creditcustitems.getSpecificsVendors().getId());
					paramList.add(branch.getId());
					List<BranchSpecifics> branchSpecificList = genericDAO.queryWithParamsName(BRANCH_SPECIFIC_JPQL,
							entityManager, paramList);
					if (branchSpecificList != null && branchSpecificList.size() > 0) {
						ObjectNode row = Json.newObject();
						row.put("id", creditcustitems.getSpecificsVendors().getId());
						row.put("name", creditcustitems.getSpecificsVendors().getName());
						row.put("iseditable",
								creditcustitems.getSpecificsVendors().getIsTransactionEditable() == null ? 0
										: creditcustitems.getSpecificsVendors().getIsTransactionEditable());
						row.put("isCombinationSales",
								creditcustitems.getSpecificsVendors().getIsCombinationSales() == null ? 0
										: creditcustitems.getSpecificsVendors().getIsCombinationSales());
						VendorTDSTaxes tdsSpecific = VendorTDSTaxes.isTdsSeupForSpecific(entityManager,
								user.getOrganization().getId(), creditcustitems.getSpecificsVendors().getId());
						if (tdsSpecific != null) {
							row.put("isTdsSpecific", true);
						} else {
							row.put("isTdsSpecific", false);
						}
						getcreditcustvenditemsan.add(row);
					}
				}
			} else if (txnPurposeId == IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE) {
				StringBuilder sbquery = new StringBuilder(
						"select obj from Transaction obj WHERE obj.transactionBranchOrganization=")
						.append(user.getOrganization().getId());
				sbquery.append(" and obj.transactionBranch.id=").append(txnBranchID);
				sbquery.append(" and obj.transactionVendorCustomer.id=").append(custVendEntityId);
				sbquery.append(
						" and obj.transactionPurpose=5  and obj.transactionStatus ='Accounted' and obj.presentStatus=1");
				// get Receive Advance from Customer
				List<Transaction> customerReceivePaymentTxn = genericDAO.executeSimpleQuery(sbquery.toString(),
						entityManager);
				for (Transaction txnCustRcvPaymnt : customerReceivePaymentTxn) {

					Double amountRefund = TransactionItems.findByAmountRefund(entityManager,
							user.getOrganization().getId(), txnCustRcvPaymnt.getTransactionBranch().getId(),
							txnCustRcvPaymnt.getTransactionRefNumber());
					Double taxReturned = TransactionItems.findByTDSRefund(entityManager, user.getOrganization().getId(),
							txnCustRcvPaymnt.getTransactionBranch().getId(),
							txnCustRcvPaymnt.getTransactionRefNumber());
					if ((txnCustRcvPaymnt.getNetAmount() != null
							&& (txnCustRcvPaymnt.getNetAmount() - amountRefund) > 0)
							|| (txnCustRcvPaymnt.getWithholdingTax() != null
									&& (txnCustRcvPaymnt.getWithholdingTax() - taxReturned) > 0)) {
						ObjectNode row = Json.newObject();
						row.put("id", txnCustRcvPaymnt.getId());
						row.put("name",
								idosdf.format(txnCustRcvPaymnt.getTransactionDate()) + "-"
										+ txnCustRcvPaymnt.getInvoiceNumber() + "-"
										+ txnCustRcvPaymnt.getTransactionRefNumber());
						getcreditcustvenditemsan.add(row);
					}
				}
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> End ");
		return Results.ok(result);
	}

	@Transactional
	public Result getklInvoices(Request request) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode txnSalesExpenseInvoicesan = result.putArray("txnSalesExpenseInvoicesData");
		// ArrayNode txnklan = result.putArray("txnkldata");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			int txnPurposeId = json.findValue("txnPurposeId").asInt();
			Long custVendEntityId = json.findValue("custVendEntityId") == null ? 0L
					: json.findValue("custVendEntityId").asLong();
			Long txnBranch = json.findValue("txnBranch") == null ? 0L : json.findValue("txnBranch").asLong();
			Vendor custVend = Vendor.findById(custVendEntityId);
			if (txnPurposeId == IdosConstants.CREDIT_NOTE_CUSTOMER || txnPurposeId == IdosConstants.DEBIT_NOTE_CUSTOMER
					|| txnPurposeId == IdosConstants.CREDIT_NOTE_VENDOR
					|| txnPurposeId == IdosConstants.DEBIT_NOTE_VENDOR
					|| txnPurposeId == IdosConstants.CANCEL_INVOICE) {
				String fmDate = json.findValue("fromDate") != null ? json.findValue("fromDate").asText() : null;
				String tDate = json.findValue("toDate") != null ? json.findValue("toDate").asText() : null;
				Date fromDate = IdosConstants.IDOSDF.parse(fmDate);
				Date toDate = IdosConstants.IDOSDF.parse(tDate);
				ArrayList inparams = new ArrayList(9);
				inparams.add(user.getOrganization().getId());
				inparams.add(txnBranch);
				inparams.add(custVendEntityId);
				Collection<Long> selectTxnList = null;
				if (txnPurposeId == IdosConstants.CREDIT_NOTE_CUSTOMER
						|| txnPurposeId == IdosConstants.DEBIT_NOTE_CUSTOMER) {
					selectTxnList = new ArrayList<Long>(1);
					selectTxnList.add(IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER);
					inparams.add(selectTxnList);
				} else if (txnPurposeId == IdosConstants.CREDIT_NOTE_VENDOR
						|| txnPurposeId == IdosConstants.DEBIT_NOTE_VENDOR) {
					selectTxnList = new ArrayList<Long>(1);
					selectTxnList.add(IdosConstants.BUY_ON_CREDIT_PAY_LATER);
					inparams.add(selectTxnList);
				} else if (txnPurposeId == IdosConstants.CANCEL_INVOICE) {
					selectTxnList = new ArrayList<Long>(2);
					selectTxnList.add(IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW);
					selectTxnList.add(IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER);
					inparams.add(selectTxnList);
				}
				inparams.add(user.getOrganization().getId());
				inparams.add(txnBranch);
				inparams.add(custVendEntityId);
				inparams.add(fromDate);
				inparams.add(toDate);
				List<Transaction> txnList = genericDAO.queryWithParamsName(CREDIT_DEBIT_TXN, entityManager, inparams);
				for (Transaction txn : txnList) {
					if (txnPurposeId == IdosConstants.CANCEL_INVOICE) {
						ObjectNode row = Json.newObject();
						row.put("id", txn.getId());
						row.put("name", idosdf.format(txn.getTransactionDate()) + "(" + txn.getNetAmount() + ")"
								+ txn.getTransactionRefNumber());
						txnSalesExpenseInvoicesan.add(row);
					} else {
						if (txn.getSalesReturnAmount() != null) {
							Double totalAdjusted = txn.getSalesReturnAmount();
							if (txn.getCustomerNetPayment() != null) {
								totalAdjusted += txn.getCustomerNetPayment();
							}
							if (totalAdjusted < txn.getNetAmount()) {
								String currentDate = idosdf.format(Calendar.getInstance().getTime());
								long daysBetween = DateUtil.calculateDays(currentDate,
										idosdf.format(txn.getTransactionDate()));
								if (daysBetween < 183) {
									ObjectNode row = Json.newObject();
									row.put("id", txn.getId());
									if (txnPurposeId == IdosConstants.CREDIT_NOTE_CUSTOMER
											|| txnPurposeId == IdosConstants.DEBIT_NOTE_CUSTOMER)
										row.put("name", idosdf.format(txn.getTransactionDate()) + "("
												+ txn.getNetAmount() + ")" + txn.getInvoiceNumber());
									else
										row.put("name", idosdf.format(txn.getTransactionDate()) + "("
												+ txn.getNetAmount() + ")" + txn.getTransactionRefNumber());
									txnSalesExpenseInvoicesan.add(row);
								}
							}
						} else if ((txn.getSalesReturnAmount() == null && txn.getCustomerNetPayment() == null)
								|| (txn.getCustomerNetPayment() < txn.getNetAmount())) {
							String currentDate = idosdf.format(Calendar.getInstance().getTime());
							long daysBetween = DateUtil.calculateDays(currentDate,
									idosdf.format(txn.getTransactionDate()));
							if (daysBetween < 183) {
								ObjectNode row = Json.newObject();
								row.put("id", txn.getId());
								if (txnPurposeId == IdosConstants.CREDIT_NOTE_CUSTOMER
										|| txnPurposeId == IdosConstants.DEBIT_NOTE_CUSTOMER)
									row.put("name", idosdf.format(txn.getTransactionDate()) + "(" + txn.getNetAmount()
											+ ")" + txn.getInvoiceNumber());
								else
									row.put("name", idosdf.format(txn.getTransactionDate()) + "(" + txn.getNetAmount()
											+ ")" + txn.getTransactionRefNumber());
								txnSalesExpenseInvoicesan.add(row);
							}
						}
					}
				}
			} else if (txnPurposeId == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
				Long txnToBranch = json.findValue("txnToBranch") == null ? 0L : json.findValue("txnToBranch").asLong();
				ArrayList inparams = new ArrayList();
				inparams.add(user.getOrganization().getId());
				inparams.add(txnBranch);
				inparams.add(txnToBranch);
				inparams.add(user.getOrganization().getId());
				List<Transaction> txnList = genericDAO.queryWithParams(INV_TRANSFER_TXN, entityManager, inparams);
				for (Transaction txn : txnList) {
					ObjectNode row = Json.newObject();
					row.put("id", txn.getId());
					row.put("name", idosdf.format(txn.getTransactionDate()) + "(" + txn.getNetAmount() + ")"
							+ txn.getTransactionRefNumber());
					txnSalesExpenseInvoicesan.add(row);
				}
			} else if (txnPurposeId == IdosConstants.SALES_RETURNS) {
				StringBuilder sbquery = new StringBuilder(
						"select obj from Transaction obj WHERE obj.transactionBranchOrganization.id=");
				sbquery.append(user.getOrganization().getId()).append(" and obj.transactionVendorCustomer.id=")
						.append(custVendEntityId);
				sbquery.append(
						" and obj.transactionPurpose.id=2 and obj.transactionStatus ='Accounted' and obj.presentStatus=1");

				List<Transaction> creditCustomesInvoices = genericDAO.executeSimpleQuery(sbquery.toString(),
						entityManager);
				for (Transaction txnSalesExpenseInvoices : creditCustomesInvoices) {
					if (txnSalesExpenseInvoices.getSalesReturnAmount() != null) {
						Double totalAdjusted = txnSalesExpenseInvoices.getSalesReturnAmount();
						if (txnSalesExpenseInvoices.getCustomerNetPayment() != null) {
							totalAdjusted += txnSalesExpenseInvoices.getCustomerNetPayment();
						}
						if (totalAdjusted < txnSalesExpenseInvoices.getNetAmount()) {
							String currentDate = idosdf.format(Calendar.getInstance().getTime());
							long daysBetween = DateUtil.calculateDays(currentDate,
									idosdf.format(txnSalesExpenseInvoices.getTransactionDate()));
							if (daysBetween < 183) {
								ObjectNode row = Json.newObject();
								row.put("id", txnSalesExpenseInvoices.getId());
								row.put("name",
										idosdf.format(txnSalesExpenseInvoices.getTransactionDate()) + "("
												+ txnSalesExpenseInvoices.getNetAmount() + ")"
												+ txnSalesExpenseInvoices.getTransactionRefNumber());
								txnSalesExpenseInvoicesan.add(row);
							}
						}
					} else if ((txnSalesExpenseInvoices.getSalesReturnAmount() == null
							&& txnSalesExpenseInvoices.getCustomerNetPayment() == null)
							|| (txnSalesExpenseInvoices.getCustomerNetPayment() < txnSalesExpenseInvoices
									.getNetAmount())) {
						String currentDate = idosdf.format(Calendar.getInstance().getTime());
						long daysBetween = DateUtil.calculateDays(currentDate,
								idosdf.format(txnSalesExpenseInvoices.getTransactionDate()));
						if (daysBetween < 183) {
							ObjectNode row = Json.newObject();
							row.put("id", txnSalesExpenseInvoices.getId());
							row.put("name",
									idosdf.format(txnSalesExpenseInvoices.getTransactionDate()) + "("
											+ txnSalesExpenseInvoices.getNetAmount() + ")"
											+ txnSalesExpenseInvoices.getTransactionRefNumber());
							txnSalesExpenseInvoicesan.add(row);
						}
					}
				}
			} else if (txnPurposeId == IdosConstants.PURCHASE_RETURNS) {
				StringBuilder sbquery = new StringBuilder(
						"select obj from Transaction obj WHERE obj.transactionBranchOrganization=")
						.append(user.getOrganization().getId());
				sbquery.append(" and obj.transactionVendorCustomer.id=").append(custVendEntityId);
				// .append(" and obj.transactionSpecifics=");
				// sbquery.append(Long.parseLong(incomeExpenseEntityId)).append(" and
				// obj.transactionParticulars=").append(incomeExpSpecf.getParticularsId().getId());
				sbquery.append(
						" and obj.transactionPurpose=4 and obj.transactionStatus ='Accounted' and obj.presentStatus=1");
				List<Transaction> creditVendorInvoices = genericDAO.executeSimpleQuery(sbquery.toString(),
						entityManager);
				for (Transaction txnSalesExpenseInvoices : creditVendorInvoices) {
					if (txnSalesExpenseInvoices.getPurchaseReturnAmount() != null) {
						if (txnSalesExpenseInvoices.getPurchaseReturnAmount() < txnSalesExpenseInvoices
								.getNetAmount()) {
							String currentDate = idosdf.format(Calendar.getInstance().getTime());
							long daysBetween = DateUtil.calculateDays(currentDate,
									idosdf.format(txnSalesExpenseInvoices.getTransactionDate()));
							if (daysBetween < 183) {
								ObjectNode row = Json.newObject();
								row.put("id", txnSalesExpenseInvoices.getId());
								row.put("name",
										idosdf.format(txnSalesExpenseInvoices.getTransactionDate()) + "("
												+ txnSalesExpenseInvoices.getNetAmount() + ")"
												+ txnSalesExpenseInvoices.getTransactionRefNumber());
								txnSalesExpenseInvoicesan.add(row);
							}
						}
					}
					if (txnSalesExpenseInvoices.getPurchaseReturnAmount() == null) {
						String currentDate = idosdf.format(Calendar.getInstance().getTime());
						long daysBetween = DateUtil.calculateDays(currentDate,
								idosdf.format(txnSalesExpenseInvoices.getTransactionDate()));
						if (daysBetween < 183) {
							ObjectNode row = Json.newObject();
							row.put("id", txnSalesExpenseInvoices.getId());
							row.put("name",
									idosdf.format(txnSalesExpenseInvoices.getTransactionDate()) + "("
											+ txnSalesExpenseInvoices.getNetAmount() + ")"
											+ txnSalesExpenseInvoices.getTransactionRefNumber());
							txnSalesExpenseInvoicesan.add(row);
						}
					}
				}
			} else if (txnPurposeId == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
					|| txnPurposeId == IdosConstants.BUY_ON_CREDIT_PAY_LATER) {
				StringBuilder sbquery = new StringBuilder("from BillOfMaterialTxnModel obj WHERE obj.organization.id=")
						.append(user.getOrganization().getId());
				sbquery.append(" and obj.branch.id=").append(txnBranch);
				sbquery.append(
						" and obj.transactionPurpose=41 and obj.transactionStatus ='Accounted' and (obj.isFulfilled != 1 or obj.isFulfilled is null)");

				StringBuilder sbquery1 = new StringBuilder(sbquery);
				sbquery1.append(
						" and obj.id in(select t1.billOfMaterialTxn.id from BillOfMaterialTxnItemModel t1 where t1.organization.id=")
						.append(user.getOrganization().getId());
				sbquery1.append(" and t1.branch.id=").append(txnBranch);
				sbquery1.append(" and t1.vendor.id=").append(custVendEntityId).append(")");

				List<BillOfMaterialTxnModel> creditVendorInvoices = genericDAO.executeSimpleQuery(sbquery1.toString(),
						entityManager);
				for (BillOfMaterialTxnModel txnPOInvoices : creditVendorInvoices) {
					ObjectNode row = Json.newObject();
					row.put("id", txnPOInvoices.getId());
					row.put("name", idosdf.format(txnPOInvoices.getActionDate()) + "("
							+ txnPOInvoices.getTotalNetAmount() + ")" + txnPOInvoices.getTransactionRefNumber());
					row.put("txnRefNo", txnPOInvoices.getTransactionRefNumber());
					txnSalesExpenseInvoicesan.add(row);
				}
				if (log.isLoggable(Level.FINE))
					log.log(Level.FINE, ">>>>>>>>" + result);
				sbquery.append(" and obj.customerVendor.id=").append(custVendEntityId);
				creditVendorInvoices = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
				for (BillOfMaterialTxnModel txnPOInvoices : creditVendorInvoices) {
					ObjectNode row = Json.newObject();
					row.put("id", txnPOInvoices.getId());
					row.put("name", idosdf.format(txnPOInvoices.getActionDate()) + "("
							+ txnPOInvoices.getTotalNetAmount() + ")" + txnPOInvoices.getTransactionRefNumber());
					row.put("txnRefNo", txnPOInvoices.getTransactionRefNumber());
					txnSalesExpenseInvoicesan.add(row);
				}

			} else if (txnPurposeId == IdosConstants.REFUND_ADVANCE_RECEIVED) {
				Branch branch = Branch.findById(txnBranch);
				if (custVend.getTotalOpeningBalance() != null && custVend.getTotalOpeningBalanceAdvPaid() > 0.0) {
					Date currentDate = new Date();
					SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
					String formattedDate = formatter.format(currentDate);
					Date txnDate = IdosUtil.getFormatedDateWithTime(formattedDate);
					List<CustomerBranchWiseAdvBalance> billwiseOpeningBalance = CustomerBranchWiseAdvBalance
							.findOpeningBalance(entityManager, user.getOrganization().getId(),
									custVend.getId(),
									branch.getId(), txnDate);
					if (billwiseOpeningBalance != null && billwiseOpeningBalance.size() > 0) {
						for (CustomerBranchWiseAdvBalance OpBalance : billwiseOpeningBalance) {
							ObjectNode rowOB = Json.newObject();
							rowOB.put("id", -1);
							rowOB.put("openingAdvId", OpBalance.getId().toString());
							rowOB.put("name",
									custVend.getName() + ":Advance Opening Balance("
											+ idosdf.format(OpBalance.getReceiptDate()) + ")("
											+ OpBalance.getReceiptNo()
											+ ")(" + OpBalance.getSpecifics().getName() + ")("
											+ OpBalance.getOpeningBalance()
											+ ")");
							txnSalesExpenseInvoicesan.add(rowOB);
						}
					}
				}
				String txnListForRefundAdvReceived = "select obj from Transaction obj where obj.transactionBranchOrganization.id=?1 and obj.transactionBranch.id=?2 and obj.transactionVendorCustomer.id=?3 and obj.transactionPurpose.id=?4 and obj.transactionStatus ='Accounted' and obj.presentStatus=1";
				ArrayList<Object> inparam1 = new ArrayList<Object>(5);
				inparam1.add(user.getOrganization().getId());
				inparam1.add(txnBranch);
				inparam1.add(custVendEntityId);
				inparam1.add(6l);
				List<Transaction> customerAdvanceTxn = genericDAO.queryWithParamsName(txnListForRefundAdvReceived,
						entityManager, inparam1);
				for (Transaction txnCustAdv : customerAdvanceTxn) {
					Double advanceRefund = TransactionItems.findByAdvanceRefund(entityManager,
							user.getOrganization().getId(), txnCustAdv.getTransactionBranch().getId(),
							txnCustAdv.getTransactionRefNumber());
					Double tdsRefund = TransactionItems.findByTDSAdvanceRefund(entityManager,
							user.getOrganization().getId(), txnCustAdv.getTransactionBranch().getId(),
							txnCustAdv.getTransactionRefNumber());

					if (txnCustAdv.getAdjustmentFromAdvance() != null)
						advanceRefund = advanceRefund + txnCustAdv.getAdjustmentFromAdvance();
					if ((txnCustAdv.getNetAmount() != null && (txnCustAdv.getNetAmount() - advanceRefund) > 0)
							|| (txnCustAdv.getWithholdingTax() != null
									&& (txnCustAdv.getWithholdingTax() - tdsRefund) > 0)) {
						ObjectNode row = Json.newObject();
						row.put("id", txnCustAdv.getId());
						row.put("name", idosdf.format(txnCustAdv.getTransactionDate()) + "-"
								+ txnCustAdv.getInvoiceNumber() + "-" + txnCustAdv.getTransactionRefNumber());
						txnSalesExpenseInvoicesan.add(row);
					}
				}
			} else if (txnPurposeId == IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE) {
				StringBuilder sbquery = new StringBuilder(
						"select obj from Transaction obj WHERE obj.transactionBranchOrganization=")
						.append(user.getOrganization().getId());
				sbquery.append(" and obj.transactionBranch.id=").append(txnBranch);
				sbquery.append(" and obj.transactionVendorCustomer.id=").append(custVendEntityId);
				sbquery.append(
						" and obj.transactionPurpose=5  and obj.transactionStatus ='Accounted' and obj.presentStatus=1");
				List<Transaction> customerReceivePaymentTxn = genericDAO.executeSimpleQuery(sbquery.toString(),
						entityManager);
				for (Transaction txnCustRcvPaymnt : customerReceivePaymentTxn) {
					Double amountRefund = TransactionItems.findByAmountRefund(entityManager,
							user.getOrganization().getId(), txnCustRcvPaymnt.getTransactionBranch().getId(),
							txnCustRcvPaymnt.getTransactionRefNumber());
					Double taxReturned = TransactionItems.findByTDSRefund(entityManager, user.getOrganization().getId(),
							txnCustRcvPaymnt.getTransactionBranch().getId(),
							txnCustRcvPaymnt.getTransactionRefNumber());
					if ((txnCustRcvPaymnt.getNetAmount() != null
							&& (txnCustRcvPaymnt.getNetAmount() - amountRefund) > 0)
							|| (txnCustRcvPaymnt.getWithholdingTax() != null
									&& (txnCustRcvPaymnt.getWithholdingTax() - taxReturned) > 0)) {
						ObjectNode row = Json.newObject();
						row.put("id", txnCustRcvPaymnt.getId());
						row.put("name",
								idosdf.format(txnCustRcvPaymnt.getTransactionDate()) + "-"
										+ txnCustRcvPaymnt.getInvoiceNumber() + "-"
										+ txnCustRcvPaymnt.getTransactionRefNumber());
						row.put("openingAdvId", "");
						txnSalesExpenseInvoicesan.add(row);
					}
				}
			} else if (txnPurposeId == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
					|| txnPurposeId == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER) {
				BILL_OF_MATERIAL_TXN_SERVICE.getPurchaseOrSalesOrderUnfulfillTxns(user, entityManager, txnBranch,
						custVendEntityId, IdosConstants.BILL_OF_MATERIAL, txnSalesExpenseInvoicesan);
			}
		} catch (Exception ex) {
			/*
			 * log.log(Level.SEVERE, user.getEmail(), ex); String strBuff =
			 * getStackTraceMessage(ex); expService.sendExceptionReport(strBuff,
			 * user.getEmail(), user.getOrganization().getName(),
			 * Thread.currentThread().getStackTrace()[1].getMethodName()); List<String>
			 * errorList = getStackTrace(ex); return Results.ok(errorPage.render(ex,
			 * errorList));
			 */
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, " End>>>>>>>>" + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getCustomerVendorPendingInvoices(Request request) {
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode getcustvendpendinginvoicesan = result.putArray("allCustomerVendorsPendingInvoicesData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			Map<String, Object> criterias = new HashMap<String, Object>(5);
			JsonNode json = request.body().asJson();
			if (log.isLoggable(Level.FINE))
				log.log(Level.FINE, ">>>> Start " + json);
			long selectedCustVend = json.findValue("selectedCustVend").asLong();
			long txnBranchId = json.findValue("txnBranchId").asLong();
			Vendor selectedVendor = Vendor.findById(selectedCustVend);
			Branch branch = Branch.findById(txnBranchId);
			String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
			Date txnDate = IdosUtil.getFormatedDateWithTime(selectedTxnDate);

			if (selectedVendor.getTotalOpeningBalance() != null && selectedVendor.getTotalOpeningBalance() > 0.0) {
				if (selectedVendor.getType() == IdosConstants.VENDOR) {
					List<VendorBillwiseOpBalance> bwobList = VendorBillwiseOpBalance.getPendingOpeningBalance(
							entityManager, user.getOrganization().getId(), selectedVendor.getId(), branch.getId(),
							IdosConstants.PAY_VENDOR_SUPPLIER, genericDAO);
					if (bwobList != null && bwobList.size() > 0) {
						for (VendorBillwiseOpBalance opBalance : bwobList) {
							ObjectNode rowOB = Json.newObject();
							rowOB.put("id", -1);
							rowOB.put("opId", opBalance.getId().toString());// for OB transaction id is -1 as it is not
																			// transaction
							rowOB.put("dateNetAmount",
									selectedVendor.getName() + ":Billwise Opening Balance("
											+ idosdf.format(opBalance.getBillDate()) + ")(" + opBalance.getBillNo()
											+ ")(" + opBalance.getOpeningBalance() + ")");
							rowOB.put("vendorCustomerGroupKl", "");
							getcustvendpendinginvoicesan.add(rowOB);
						}
					} else if (branch != null) {
						List<BranchVendors> branchVendorsList = BranchVendors.getPendingOpeningBalance(entityManager,
								user.getOrganization().getId(), selectedVendor.getId(), branch.getId(),
								IdosConstants.PAY_VENDOR_SUPPLIER);
						for (BranchVendors branchVendor : branchVendorsList) {
							ObjectNode rowOB = Json.newObject();
							rowOB.put("id", -1); // for OB transaction id is -1 as it is not transaction
							rowOB.put("opId", "");
							rowOB.put("dateNetAmount",
									selectedVendor.getName() + ":Opening Balance("
											+ idosdf.format(selectedVendor.getCreatedAt()) + ")("
											+ branchVendor.getOpeningBalance() + ")");
							rowOB.put("vendorCustomerGroupKl", "");
							getcustvendpendinginvoicesan.add(rowOB);
						}
					}
				} else {
					List<CustomerBillwiseOpBalance> billwiseOpeningBalance = CustomerBillwiseOpBalance
							.findOpeningBalance(entityManager, user.getOrganization().getId(), selectedVendor.getId(),
									branch.getId(), txnDate);
					if (billwiseOpeningBalance != null && billwiseOpeningBalance.size() > 0) {
						for (CustomerBillwiseOpBalance OpBalance : billwiseOpeningBalance) {
							ObjectNode rowOB = Json.newObject();
							rowOB.put("id", -1);
							rowOB.put("opId", OpBalance.getId().toString());// for OB transaction id is -1 as it is not
																			// transaction
							rowOB.put("dateNetAmount",
									selectedVendor.getName() + ":Opening Balance("
											+ idosdf.format(OpBalance.getBillDate()) + ")(" + OpBalance.getBillNo()
											+ ")(" + OpBalance.getOpeningBalance() + ")");
							rowOB.put("vendorCustomerGroupKl", "");
							getcustvendpendinginvoicesan.add(rowOB);
						}
					} else if (branch != null) {
						criterias.clear();
						criterias.put("organization.id", user.getOrganization().getId());
						criterias.put("branch.id", branch.getId());
						criterias.put("vendor.id", selectedVendor.getId());
						criterias.put("presentStatus", 1);
						BranchVendors branchVendors = genericDAO.getByCriteria(BranchVendors.class, criterias,
								entityManager);
						if (branchVendors != null) {
							if (branchVendors.getOpeningBalance() != null && branchVendors.getOpeningBalance() > 0) {
								ObjectNode rowOB = Json.newObject();
								rowOB.put("id", -1); // for OB transaction id is -1 as it is not transaction
								rowOB.put("opId", "");
								rowOB.put("dateNetAmount",
										selectedVendor.getName() + ":Opening Balance("
												+ idosdf.format(selectedVendor.getCreatedAt()) + ")("
												+ branchVendors.getOpeningBalance() + ")");
								rowOB.put("vendorCustomerGroupKl", "");
								getcustvendpendinginvoicesan.add(rowOB);
							}
						}
					}
				}
			}
			// Query query = entityManager.createQuery(PENDING_INVOICES_HQL);
			ArrayList inparams = new ArrayList(6);
			inparams.add(user.getOrganization().getId());
			inparams.add(branch.getId());
			inparams.add(selectedVendor.getId());
			inparams.add(txnDate);
			inparams.add(user.getOrganization().getId());
			inparams.add(branch.getId());
			List<Transaction> creditCustVendPendingInvoices = genericDAO.queryWithParams(PENDING_INVOICES_HQL,
					entityManager, inparams);
			for (Transaction pendingTxn : creditCustVendPendingInvoices) {
				StringBuilder pendingPayVendors = null;
				Double netPayment = 0.0;
				Double returns = 0.0;
				if (pendingTxn.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER) {
					if (pendingTxn.getCustomerNetPayment() != null) {
						netPayment = pendingTxn.getNetAmount() - pendingTxn.getCustomerNetPayment();
					} else {
						netPayment = pendingTxn.getNetAmount();
					}
					if (pendingTxn.getSalesReturnAmount() != null) {
						returns = pendingTxn.getSalesReturnAmount();
						netPayment = netPayment - returns;
					}
				} else if (pendingTxn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CREDIT_PAY_LATER) {
					if (pendingTxn.getVendorNetPayment() != null) {
						netPayment = pendingTxn.getNetAmount() - pendingTxn.getVendorNetPayment();
					} else {
						netPayment = pendingTxn.getNetAmount();
					}
					if (pendingTxn.getPurchaseReturnAmount() != null) { // if item is returned to vendor for which
																		// payment is made, then that much less you owe
																		// to vendor
						returns = pendingTxn.getPurchaseReturnAmount();
						netPayment = netPayment - returns;
					}

					inparams.clear();
					inparams.add(user.getOrganization().getId());
					inparams.add(branch.getId());
					inparams.add(selectedVendor.getId());
					inparams.add(pendingTxn.getTransactionRefNumber());
					List<Transaction> pendingPayVendorTxnList = genericDAO.queryWithParams(PENDING_VEND_PAYMENT_HQL,
							entityManager, inparams);
					if (pendingPayVendorTxnList.size() > 0) {
						pendingPayVendors = new StringBuilder();
						for (Transaction pendingPayVendorTxn : pendingPayVendorTxnList) {
							netPayment -= pendingPayVendorTxn.getNetAmount() == null ? 0.0
									: pendingPayVendorTxn.getNetAmount();
							if (pendingPayVendorTxn.getAvailableDiscountAmountForTxn() != null) {
								netPayment -= pendingPayVendorTxn.getAvailableDiscountAmountForTxn();
							}
							pendingPayVendors.append(pendingPayVendorTxn.getTransactionRefNumber()).append(": ")
									.append(pendingPayVendorTxn.getNetAmount()).append(", ");
						}
					}
				}
				if (netPayment != null && netPayment > 0) { // if it is -ve it means due to purchase/sales return,
															// vendor has got excessive balance and you don't need to
															// pay anything
					String performaInvoice = "";
					if (pendingTxn.getPerformaInvoice() != null && pendingTxn.getPerformaInvoice() == true) {
						performaInvoice = ",PerformaInvoice";
					}
					ObjectNode row = Json.newObject();
					row.put("id", pendingTxn.getId());
					row.put("opId", "");
					row.put("dateNetAmount",
							pendingTxn.getTransactionSpecifics().getName() + "("
									+ idosdf.format(pendingTxn.getTransactionDate()) + ")(" + netPayment + ")"
									+ pendingTxn.getTransactionRefNumber() + performaInvoice);
					if (selectedVendor.getVendorGroup() != null) {
						if (selectedVendor.getVendorGroup().getKnowledgeLibrary() != null) {
							row.put("vendorCustomerGroupKl", selectedVendor.getVendorGroup().getKnowledgeLibrary());
						} else {
							row.put("vendorCustomerGroupKl", "");
						}
					} else {
						row.put("vendorCustomerGroupKl", "");
					}
					if (pendingPayVendors != null) {
						row.put("pendingPayVendors", pendingPayVendors.toString());
					}
					getcustvendpendinginvoicesan.add(row);
				}
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getTxnPjctItemsOnBranch(Request request) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> Start ");
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		// ArrayNode gettxnan = result.putArray("allTxnPurposeItemsData");
		ArrayNode gettxnpjctan = result.putArray("allTxnProjectPurposeData");
		ArrayNode getbnchcashptyan = result.putArray("branchCashPettyAccountData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			long txnPurposeId = json.findValue("txnPurposeId").asLong();
			long txnPurposeBnchId = json.findValue("txnPurposeBnchId").asLong();
			// session.adding("email", useremail);

			criterias.put("user.id", user.getId());
			criterias.put("userRights.id", 1L);
			criterias.put("presentStatus", 1);
			List<UserRightForProject> userRightForProject = genericDAO.findByCriteria(UserRightForProject.class,
					criterias, entityManager);
			for (UserRightForProject usrRgtPjct : userRightForProject) {
				criterias.clear();
				criterias.put("project.id", usrRgtPjct.getProject().getId());
				criterias.put("projectBranch.id", txnPurposeBnchId);
				criterias.put("projectOrganization.id", usrRgtPjct.getUser().getOrganization().getId());
				criterias.put("presentStatus", 1);
				ProjectBranches pjctBnch = genericDAO.getByCriteria(ProjectBranches.class, criterias, entityManager);
				if (pjctBnch != null) {
					ObjectNode row = Json.newObject();
					row.put("id", usrRgtPjct.getProject().getId());
					row.put("name", usrRgtPjct.getProject().getName());
					gettxnpjctan.add(row);
				}
			}
			if (txnPurposeId == IdosConstants.BILL_OF_MATERIAL) {
				gettxnpjctan.removeAll();
				criterias.clear();
				criterias.put("projectBranch.id", txnPurposeBnchId);
				criterias.put("projectOrganization.id", user.getOrganization().getId());
				criterias.put("presentStatus", 1);
				List<ProjectBranches> projectBranches = genericDAO.findByCriteria(ProjectBranches.class, criterias,
						entityManager);
				for (ProjectBranches projectBranches2 : projectBranches) {
					ObjectNode row = Json.newObject();
					row.put("id", projectBranches2.getProject().getId());
					row.put("name", projectBranches2.getProject().getName());
					gettxnpjctan.add(row);
				}
			}
			/*
			 * if(txnPurposeId != IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW &&
			 * txnPurposeId != IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
			 * criterias.clear(); criterias.put("branch.id", txnPurposeBnchId);
			 * criterias.put("transactionPurpose.id", txnPurposeId);
			 * criterias.put("organization.id", user.getOrganization().getId());
			 * criterias.put("presentStatus", 1); List<SpecificsTransactionPurpose>
			 * txnPurposeSpecifics=genericDAO.findByCriteria(SpecificsTransactionPurpose.
			 * class, criterias, entityManager); for(SpecificsTransactionPurpose
			 * txnPurposeSpecf:txnPurposeSpecifics){ ObjectNode row = Json.newObject();
			 * criterias.clear(); criterias.put("user.id", user.getId());
			 * criterias.put("userRights.id", 1L); criterias.put("specifics.id",
			 * txnPurposeSpecf.getSpecifics().getId()); criterias.put("particulars.id",
			 * txnPurposeSpecf.getSpecifics().getParticularsId().getId());
			 * criterias.put("presentStatus", 1); UserRightSpecifics
			 * userHasRightForSpecf=genericDAO.getByCriteria(UserRightSpecifics.class,
			 * criterias, entityManager); if(userHasRightForSpecf!=null){ criterias.clear();
			 * criterias.put("branch.id", txnPurposeBnchId); criterias.put("specifics.id",
			 * userHasRightForSpecf.getSpecifics().getId());
			 * criterias.put("organization.id",
			 * userHasRightForSpecf.getUser().getOrganization().getId());
			 * criterias.put("presentStatus", 1); BranchSpecifics
			 * bnchSpecf=genericDAO.getByCriteria(BranchSpecifics.class, criterias,
			 * entityManager); if(bnchSpecf!=null){ row.put("id",
			 * userHasRightForSpecf.getSpecifics().getId()); row.put("name",
			 * userHasRightForSpecf.getSpecifics().getName()); row.put("accountCode",
			 * userHasRightForSpecf.getSpecifics().getParticularsId().getAccountCode());
			 * gettxnan.add(row); } } }
			 */

			if (txnPurposeId == IdosConstants.TRANSFER_MAIN_CASH_TO_PETTY_CASH
					|| txnPurposeId == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) {
				StringBuilder newsbquery = new StringBuilder(
						"select obj from BranchCashCount obj WHERE obj.branch.id=?1 AND obj.organization.id=?2 and obj.presentStatus=1 ORDER BY obj.date desc");
				ArrayList inparam = new ArrayList(2);
				inparam.add(txnPurposeBnchId);
				inparam.add(user.getOrganization().getId());
				List<BranchCashCount> prevBranchCashCount = genericDAO.queryWithParams(newsbquery.toString(),
						entityManager, inparam);
				if (prevBranchCashCount.size() > 0) {
					ObjectNode row = Json.newObject();
					if (prevBranchCashCount.get(0).getResultantCash() != null) {
						row.put("resultantCash",
								IdosConstants.decimalFormat.format(prevBranchCashCount.get(0).getResultantCash()));
					} else {
						row.put("resultantCash", "0.00");
					}
					if (prevBranchCashCount.get(0).getResultantPettyCash() != null) {
						row.put("resultantPettyCash",
								IdosConstants.decimalFormat.format(prevBranchCashCount.get(0).getResultantPettyCash()));
					} else {
						row.put("resultantPettyCash", "0.00");
					}
					getbnchcashptyan.add(row);
				}
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>>>> End ");
		return Results.ok(result);
	}

	@Transactional
	public Result submitForAccounting(Request request) {
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			entitytransaction.begin();
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			if (log.isLoggable(Level.FINE))
				log.log(Level.FINE, ">>>> Start " + json);
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			result.put("useremail", user.getEmail());
			String role = "";
			List<UsersRoles> userRoles = UsersRoles.getUserRoleList(entityManager, user.getOrganization().getId(),
					user.getId(),
					user.getBranch().getId());
			for (UsersRoles roles : userRoles) {
				role += roles.getRole().getName() + ",";
			}
			role = role.substring(0, role.length() - 1);
			result.put("role", role);
			String txnPurpose = json.findValue("txnPurpose").asText();
			long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
			TransactionPurpose usertxnPurpose = TransactionPurpose.findById(txnPurposeVal);
			String txnInstrumentNumber = "";
			String txnInstrumentDate = "";
			Transaction txn = null;
			if (IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW == txnPurposeVal
					|| IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER == txnPurposeVal) {
				txn = sellTransactionService.submitForAccounting(user, json, entityManager, entitytransaction, result);
				if (txn == null) {
					return Results.ok(result);
				}
			} else if (IdosConstants.CANCEL_INVOICE == txnPurposeVal) {
				txn = sellTransactionService.submitForCancellation(user, json, entityManager, entitytransaction,
						result);
				if (txn == null) {
					return Results.ok(result);
				}
			} else if (IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER == txnPurposeVal) {
				Double txnwithHoldingTaxReceipt = json.findValue("txnRCPFCCwithHoldingTaxReceipt") == null
						|| json.findValue("txnRCPFCCwithHoldingTaxReceipt").asText().trim().equals("") ? 0.0
								: json.findValue("txnRCPFCCwithHoldingTaxReceipt").asDouble();
				if (txnwithHoldingTaxReceipt != null && txnwithHoldingTaxReceipt > 0.0) {
					// Is this the account where you classify withholding tax (TDS) on payments
					// received from customers
					Specifics specificsMap = coaService.getSpecificsForMapping(user, "8", entityManager);
					if (specificsMap == null) {
						throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
								"COA is not found for mapping id 8",
								"TDS COA mapping is not found for, type: 8- withholding tax (TDS) on payments received from customers and others");
					}
					result.put("tdsReceivableSpecific", specificsMap.getId());
				}
				String txnforbranch = json.findValue("txnforbranch").asText();
				String creditRCPFCCCustomer = json.findValue("creditRCPFCCCustomer").asText();// credit customer
				String txnRCPFCCoutstandings = json.findValue("txnRCPFCCoutstandings").asText();// net amount
																								// description which is
																								// outstandings here
				String txnRCPFCCInvoice = json.findValue("txnRCPFCCInvoice").asText();// transaction for which payment
																						// from customer
				Integer customeradvanceType = json.findValue("customeradvanceType").asInt();
				String txnRCPFCCCustomerAdvanceIfAny = json.findValue("txnRCPFCCCustomerAdvanceIfAny").asText();// advance
																												// if
																												// any
				String txnRCPFCCCustomerAdvanceAdjustment = json.findValue("txnRCPFCCCustomerAdvanceAdjustment")
						.asText(); // Advance to Adjust
				String txnRCPFCCpaymentReceivedStr = json.findValue("txnRCPFCCpaymentReceived") == null ? null
						: json.findValue("txnRCPFCCpaymentReceived").asText();
				Double txnRCPFCCpaymentReceived = 0.0;
				if (txnRCPFCCpaymentReceivedStr == null || "".equals(txnRCPFCCpaymentReceivedStr.trim())) {
					throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
							IdosConstants.INVALID_DATA_EXCEPTION, "Invalid Recevied amount: empty");
				} else {
					txnRCPFCCpaymentReceived = IdosUtil.convertStringToDouble(txnRCPFCCpaymentReceivedStr);
				}
				Double txnRCPFCCdiscountAllowed = json.findValue("txnRCPFCCdiscountAllowed") == null ? 0.0
						: json.findValue("txnRCPFCCdiscountAllowed").asDouble();
				String txnRCPFCCpaymentDue = json.findValue("txnRCPFCCpaymentDue").asText();
				int txnreceiptdetails = json.findValue("txnReceiptDetails") == null ? 0
						: json.findValue("txnReceiptDetails").asInt();
				String txnreceipttypebankdetails = json.findValue("txnReceiptDescription").asText();
				String supportingdoc = json.findValue("supportingdoc").asText();
				String txnremarks = json.findValue("txnremarks").asText();
				Long openingBalBillId = (json.findValue("openingBalBillId") == null
						|| "".equals(json.findValue("openingBalBillId").asText())) ? null
								: json.findValue("openingBalBillId").asLong();
				String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
				Date txnDate = IdosUtil.getFormatedDateWithTime(selectedTxnDate);

				String klfollowednotfollowed = json.findValue("klfollowednotfollowed").asText();
				// start with new transaction
				String txnForItemStr = json.findValue("txnForItem").toString();
				txn = new Transaction();
				Vendor txnCustomer = Vendor.findById(IdosUtil.convertStringToLong(creditRCPFCCCustomer));
				Double advanceAdjustment = 0.0;
				String paymentMode = "";
				String projectName = "";
				Branch transactionBranch = null;
				String branchName = "";
				if (txnforbranch != null && !txnforbranch.equals("")) {
					transactionBranch = genericDAO.getById(Branch.class, IdosUtil.convertStringToLong(txnforbranch),
							entityManager);
					branchName = transactionBranch.getName();
				}
				JSONArray arrJSON = new JSONArray(txnForItemStr);
				if (txnRCPFCCInvoice.equals("-1")) { // When customer pays OB for account receivables from last year etc
					// transactionBranch=user.getBranch();
					Double recivedPayment = txnRCPFCCpaymentReceived;
					if (txnwithHoldingTaxReceipt != null) {
						txn.setWithholdingTax(txnwithHoldingTaxReceipt);
						recivedPayment += txnwithHoldingTaxReceipt;
					} else {
						txn.setWithholdingTax(0.0);
					}

					if (openingBalBillId != null) {
						CustomerBillwiseOpBalance openingBalanceBillTrans = CustomerBillwiseOpBalance
								.findById(openingBalBillId);
						txn.setGrossAmount(openingBalanceBillTrans.getOpeningBalance());
						txn.setPaidInvoiceRefNumber(openingBalanceBillTrans.getId().toString());
						txn.setTypeIdentifier(IdosConstants.TXN_TYPE_OPENING_BALANCE_BILLWISE_CUST);
						// Set BILLWISE Opening Balance
						openingBalanceBillTrans.setOpeningBalance(IdosUtil.convertStringToDouble(txnRCPFCCpaymentDue));
						genericDAO.saveOrUpdate(openingBalanceBillTrans, user, entityManager);
						// Set Branch Opening Balance
						criterias.clear();
						criterias.put("organization.id", user.getOrganization().getId());
						criterias.put("branch.id", transactionBranch.getId());
						criterias.put("vendor.id", txnCustomer.getId());
						criterias.put("presentStatus", 1);
						BranchVendors branchCustomer = genericDAO.getByCriteria(BranchVendors.class, criterias,
								entityManager);
						if (branchCustomer.getOpeningBalance() != null && recivedPayment != null) {
							double openingBalanceBranch = branchCustomer.getOpeningBalance() - recivedPayment;
							branchCustomer.setOpeningBalance(openingBalanceBranch);
							genericDAO.saveOrUpdate(branchCustomer, user, entityManager);
						}
						// customer Opening balance
						double totalOpBal = txnCustomer.getTotalOpeningBalance() - recivedPayment;
						txnCustomer.setTotalOpeningBalance(totalOpBal);

					} else {
						criterias.clear();
						criterias.put("organization.id", user.getOrganization().getId());
						criterias.put("branch.id", transactionBranch.getId());
						criterias.put("vendor.id", txnCustomer.getId());
						criterias.put("presentStatus", 1);
						BranchVendors branchCustomer = genericDAO.getByCriteria(BranchVendors.class, criterias,
								entityManager);
						if (branchCustomer != null) {
							txn.setGrossAmount(branchCustomer.getOpeningBalance());
							branchCustomer.setOpeningBalance(IdosUtil.convertStringToDouble(txnRCPFCCpaymentDue));
							genericDAO.saveOrUpdate(branchCustomer, user, entityManager);
						}
						txn.setPaidInvoiceRefNumber("-1"); // it has no pending transaction invoice, so setting -1
						txn.setTypeIdentifier(IdosConstants.TXN_TYPE_OPENING_BALANCE_CUST);

						// customer Opening balance
						double openingBalanceBranch = txnCustomer.getTotalOpeningBalance() - recivedPayment;
						txnCustomer.setTotalOpeningBalance(openingBalanceBranch);
					}
				} else {
					txn.setTypeIdentifier(IdosConstants.TXN_TYPE_OTHER_TRANSACTIONS_CUST);
					Double advanceIfAnyForAdjustment = null;
					for (int i = 0; i < arrJSON.length(); i++) {
						JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
						Double taxWH = 0.0, amountReceived = 0.0, dueBal = 0.0, discAllowed = 0.0;
						if (rowItemData.getString("taxWH") != null && !rowItemData.getString("taxWH").equals(""))
							taxWH = rowItemData.getDouble("taxWH");

						if (rowItemData.getString("amountReceived") != null
								&& !rowItemData.getString("amountReceived").equals(""))
							amountReceived = rowItemData.getDouble("amountReceived");
						else
							throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
									IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
									"Invalid Recevied amount: " + rowItemData.getDouble("amountReceived"));

						if (rowItemData.getString("dueBal") != null && !rowItemData.getString("dueBal").equals(""))
							dueBal = rowItemData.getDouble("dueBal");

						if (rowItemData.getString("discAllowed") != null
								&& !rowItemData.getString("discAllowed").equals(""))
							discAllowed = rowItemData.getDouble("discAllowed");

						Transaction pendingTransaction = Transaction.findById(rowItemData.getLong("pendingTxn"));
						// effect on the pendingTransaction for which company sell on credit and collect
						// payment later
						Double customerNetPayment = null;

						Double availableAdvanceAdjustment = null;

						if (!txnRCPFCCCustomerAdvanceIfAny.equals("")
								&& !txnRCPFCCCustomerAdvanceAdjustment.equals("")) {
							advanceIfAnyForAdjustment = IdosUtil.convertStringToDouble(txnRCPFCCCustomerAdvanceIfAny);
							advanceAdjustment = IdosUtil.convertStringToDouble(txnRCPFCCCustomerAdvanceAdjustment);
						} else {
							advanceIfAnyForAdjustment = 0.0;
							advanceAdjustment = 0.0;
						}
						if (pendingTransaction.getCustomerNetPayment() != null) {
							customerNetPayment = pendingTransaction.getCustomerNetPayment() + amountReceived
									+ advanceAdjustment;
						}
						if (pendingTransaction.getCustomerNetPayment() == null) {
							customerNetPayment = amountReceived + advanceAdjustment;
						}
						customerNetPayment = customerNetPayment + taxWH + discAllowed;
						if (pendingTransaction.getWithholdingTax() != null) {
							pendingTransaction.setWithholdingTax(pendingTransaction.getWithholdingTax() + taxWH);
						} else if (pendingTransaction.getWithholdingTax() == null) {
							pendingTransaction.setWithholdingTax(taxWH);
						}
						pendingTransaction.setAvailableAdvance(advanceIfAnyForAdjustment);
						if (pendingTransaction.getAdjustmentFromAdvance() != null) {
							availableAdvanceAdjustment = pendingTransaction.getAdjustmentFromAdvance()
									+ advanceAdjustment;
						}
						if (pendingTransaction.getAdjustmentFromAdvance() == null) {
							availableAdvanceAdjustment = advanceAdjustment;
						}
						pendingTransaction.setAdjustmentFromAdvance(availableAdvanceAdjustment);
						pendingTransaction.setCustomerNetPayment(customerNetPayment);
						pendingTransaction.setCustomerDuePayment(dueBal);
						if (dueBal > 0) {
							pendingTransaction.setPaymentStatus("PARTLY-PAID");
						}
						if ((dueBal < 0 || dueBal == 0) && amountReceived > 0) {
							pendingTransaction.setPaymentStatus("PAID");
						}
						// transactionBranch = pendingTransaction.getTransactionBranch();
						genericDAO.saveOrUpdate(pendingTransaction, user, entityManager);

						// start Receive payment new transaction
					}
					/*
					 * if (pendingTransaction.getTransactionProject() != null) { projectName =
					 * pendingTransaction.getTransactionProject().getName(); }
					 * txn.setWithholdingTax(txnwithHoldingTaxReceipt);
					 * txn.setTransactionProject(pendingTransaction.getTransactionProject());
					 * txn.setTransactionSpecifics(pendingTransaction.getTransactionSpecifics());
					 * txn.setTransactionParticulars(pendingTransaction.getTransactionSpecifics().
					 * getParticularsId()); txn.setNoOfUnits(pendingTransaction.getNoOfUnits());
					 * txn.setPricePerUnit(pendingTransaction.getPricePerUnit());
					 * txn.setGrossAmount(pendingTransaction.getGrossAmount());
					 * txn.setPaidInvoiceRefNumber(pendingTransaction.getTransactionRefNumber());
					 */
					// advance adjustment
					txn.setAdvanceType(customeradvanceType);
					txn.setAvailableAdvance(advanceIfAnyForAdjustment);
					if (customeradvanceType == 2) {
						if (txnCustomer.getTotalOpeningBalanceAdvPaid() != null) {
							txnCustomer.setTotalOpeningBalanceAdvPaid(
									txnCustomer.getTotalOpeningBalanceAdvPaid() - advanceAdjustment);
							genericDAO.saveOrUpdate(txnCustomer, user, entityManager);
						}
					} else {
						if (!txnRCPFCCCustomerAdvanceIfAny.equals("") && !txnRCPFCCCustomerAdvanceAdjustment.equals("")
								&& advanceAdjustment > 0.0) {
							txn.setAdjustmentFromAdvance(advanceAdjustment);
							criterias.clear();
							criterias.put("vendorSpecific.id", txnCustomer.getId());
							// criterias.put("specificsVendors.id",
							// pendingTransaction.getTransactionSpecifics().getId());
							criterias.put("organization.id", user.getOrganization().getId());
							criterias.put("presentStatus", 1);
							VendorSpecific customerTxnSpecifics = genericDAO.getByCriteria(VendorSpecific.class,
									criterias, entityManager);
							customerTxnSpecifics
									.setAdvanceMoney(customerTxnSpecifics.getAdvanceMoney() - advanceAdjustment);
							genericDAO.saveOrUpdate(customerTxnSpecifics, user, entityManager);
						}
					}
				}
				// applicable fields for OB invoice as well for credit sell invoices
				txn.setTransactionPurpose(usertxnPurpose);
				if (transactionBranch != null && !transactionBranch.equals("")) {
					txn.setTransactionBranch(transactionBranch);
					txn.setTransactionBranchOrganization(transactionBranch.getOrganization());
				}
				if (klfollowednotfollowed != null && !klfollowednotfollowed.equals("")) {
					txn.setKlFollowStatus(IdosUtil.convertStringToInt(klfollowednotfollowed));
				}
				if (DateUtil.isBackDate(txnDate)) {
					txn.setIsBackdatedTransaction(IdosConstants.BACK_DATED_TXN);
				}
				txn.setTransactionVendorCustomer(txnCustomer);
				txn.setPaymentStatus("PAID");
				txn.setNetAmount(txnRCPFCCpaymentReceived);
				txn.setWithholdingTax(txnwithHoldingTaxReceipt);
				txn.setAvailableDiscountAmountForTxn(txnRCPFCCdiscountAllowed);
				String netDesc = "Payment Received:" + txnRCPFCCpaymentReceived + ",Advance Adjustment:"
						+ advanceAdjustment + ",Withholding Adjustment:" + txnwithHoldingTaxReceipt + ",Due Balance:"
						+ txnRCPFCCpaymentDue;
				txn.setNetAmountResultDescription(netDesc);
				txn.setTransactionDate(txnDate);
				txn.setCustomerDuePayment(0.0);
				txn.setCustomerNetPayment(txnRCPFCCpaymentReceived);
				txn.setReceiptDetailsType(txnreceiptdetails);

				txn.setReceiptDetailsDescription(txnreceipttypebankdetails);
				String txnRemarks = "";
				if (IdosConstants.PAYMODE_CASH == txnreceiptdetails) {
					paymentMode = "CASH";
				} else if (IdosConstants.PAYMODE_BANK == txnreceiptdetails) {
					paymentMode = "BANK";
				}
				if (txnremarks != null && !txnremarks.equals("")) {
					txnRemarks = user.getEmail() + "#" + txnremarks;
					txn.setRemarks(txnRemarks);
					txnRemarks = txn.getRemarks();
				}
				txn.setSupportingDocs(transactionDao.getAndDeleteSupportingDocument(txn.getSupportingDocs(),
						user.getEmail(), supportingdoc, user, entityManager));
				txn.setTransactionStatus("Accounted");
				String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
				txn.setTransactionRefNumber(transactionNumber);
				transactionDao.setInvoiceQuotProfSerial(user, entityManager, txn);
				genericDAO.saveOrUpdate(txn, user, entityManager);
				FILE_UPLOAD_SERVICE.updateUploadFileLogs(entityManager, user, supportingdoc, txn.getId(),
						IdosConstants.MAIN_TXN_TYPE);

				// to save in transaction_items table
				for (int i = 0; i < arrJSON.length(); i++) {
					JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
					if (!rowItemData.getString("pendingTxn").equals("")) {
						if (!rowItemData.getString("pendingTxn").equals("-1")) {
							Transaction pendingTransaction = Transaction.findById(rowItemData.getLong("pendingTxn"));
							TransactionItems saveSelectedTxns = new TransactionItems();
							saveSelectedTxns.setTransactionId(txn);
							saveSelectedTxns.setOrganization(user.getOrganization());
							saveSelectedTxns.setTransactionRefNumber(pendingTransaction.getTransactionRefNumber());
							if (rowItemData.getString("amountReceived") != null
									&& !rowItemData.getString("amountReceived").equals(""))
								saveSelectedTxns.setNetAmount(rowItemData.getDouble("amountReceived"));
							else
								throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
										IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
										"Invalid Recevied amount: " + rowItemData.getDouble("amountReceived"));
							if (!rowItemData.getString("taxWH").equals(""))
								saveSelectedTxns.setWithholdingAmount(rowItemData.getDouble("taxWH"));
							if (!rowItemData.getString("discAllowed").equals(""))
								saveSelectedTxns.setDiscountAmount(rowItemData.getDouble("discAllowed"));
							genericDAO.saveOrUpdate(saveSelectedTxns, user, entityManager);
						} else {
							criterias.clear();
							criterias.put("organization.id", user.getOrganization().getId());
							criterias.put("branch.id", transactionBranch.getId());
							criterias.put("vendor.id", txnCustomer.getId());
							criterias.put("presentStatus", 1);
							BranchVendors branchCustomer = genericDAO.getByCriteria(BranchVendors.class, criterias,
									entityManager);
							Double receivedPayment = 0.0;
							TransactionItems saveSelectedTxns = new TransactionItems();
							saveSelectedTxns.setTransactionId(txn);
							saveSelectedTxns.setOrganization(user.getOrganization());
							if (!rowItemData.getString("amountReceived").equals("")) {
								saveSelectedTxns.setNetAmount(rowItemData.getDouble("amountReceived"));
								receivedPayment += rowItemData.getDouble("amountReceived");
							}
							if (!rowItemData.getString("taxWH").equals("")) {
								saveSelectedTxns.setWithholdingAmount(rowItemData.getDouble("taxWH"));
								receivedPayment += rowItemData.getDouble("taxWH");
							}
							if (!rowItemData.getString("discAllowed").equals("")) {
								saveSelectedTxns.setDiscountAmount(rowItemData.getDouble("discAllowed"));
								receivedPayment += rowItemData.getDouble("discAllowed");
							}

							if (branchCustomer != null) {
								saveSelectedTxns.setGrossAmount(branchCustomer.getOpeningBalance());
								if (!rowItemData.getString("dueBal").equals(""))
									branchCustomer.setOpeningBalance(rowItemData.getDouble("dueBal"));
								genericDAO.saveOrUpdate(branchCustomer, user, entityManager);
							}
							saveSelectedTxns.setTransactionRefNumber("-1"); // it has no pending transaction invoice, so
																			// setting -1
							genericDAO.saveOrUpdate(saveSelectedTxns, user, entityManager);
							// saveSelectedTxns.setTypeIdentifier(IdosConstants.TXN_TYPE_OPENING_BALANCE_CUST);
							// customer Opening balance
							double openingBalanceBranch = txnCustomer.getTotalOpeningBalance() - receivedPayment;
							txnCustomer.setTotalOpeningBalance(openingBalanceBranch);
						}
					}
				}

				Long txnreceiptPaymentBank = json.findValue("txnReceiptPaymentBank") != null
						? json.findValue("txnReceiptPaymentBank").asLong()
						: null;
				txnInstrumentNumber = json.findValue("txnInstrumentNum") != null
						? json.findValue("txnInstrumentNum").asText()
						: "";
				txnInstrumentDate = json.findValue("txnInstrumentDate") != null
						? json.findValue("txnInstrumentDate").asText()
						: "";
				sellTransactionService.setTxnPaymentDetail(user, entityManager, txn, txnreceiptdetails,
						txnreceiptPaymentBank, txnInstrumentNumber, txnInstrumentDate, result);
				TRIAL_BALANCE_SERVICE.insertTrialBalance(txn, user, entityManager);
				/***************************
				 * TRIAL BALANCE TABLES
				 ******************************/

				if (txn.getRoundedCutPartOfNetAmount() != null && txn.getRoundedCutPartOfNetAmount() != 0.0) {
					Boolean roundupMappingFound = null;
					if (txn.getRoundedCutPartOfNetAmount() > 0) {
						roundupMappingFound = TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
								txn.getTransactionBranchOrganization(), txn.getTransactionBranch(), txn.getId(),
								txn.getTransactionPurpose(), txn.getTransactionDate(),
								txn.getRoundedCutPartOfNetAmount(), user, entityManager, true);
					} else {
						roundupMappingFound = TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
								txn.getTransactionBranchOrganization(), txn.getTransactionBranch(), txn.getId(),
								txn.getTransactionPurpose(), txn.getTransactionDate(),
								txn.getRoundedCutPartOfNetAmount(), user, entityManager, false);
					}
					result.put("roundupMappingFound", roundupMappingFound);
					if (roundupMappingFound == null || !roundupMappingFound) {
						return Results.ok(result);
					}
				}

				/*******************************************
				 * END TRIAL BALANCE ENTRIES
				 ******************************/

				// Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
				// Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
				// for (int i = 0; i < keyArray.length; i++) {
				// StringBuilder sbquery = new StringBuilder("");
				// sbquery.append(
				// "select obj from Users obj WHERE obj.email ='" + keyArray[i] + "' and
				// obj.presentStatus=1");
				// List<Users> orgusers = genericDAO.executeSimpleQuery(sbquery.toString(),
				// entityManager);
				// if (!orgusers.isEmpty()
				// && orgusers.get(0).getOrganization().getId() ==
				// user.getOrganization().getId()) {
				// orgtxnregistereduser.put(keyArray[i].toString(),
				// CreatorActor.expenseregistrered.get(keyArray[i]));
				// }
				// }
				String debitCredit = "Credit";
				entitytransaction.commit();
				String invoiceDate = "";
				String invoiceDateLabel = "";
				if (txn.getTransactionInvoiceDate() != null) {
					invoiceDateLabel = "INVOICE DATE:";
					invoiceDate = idosdf.format(txn.getTransactionInvoiceDate());
				}
				String itemParentName = "";
				String itemName = "";
				if (txn.getTransactionSpecifics() != null) {
					itemName = txn.getTransactionSpecifics().getName();
					if (txn.getTransactionSpecifics().getParentSpecifics() != null
							&& !txn.getTransactionSpecifics().getParentSpecifics().equals("")) {
						itemParentName = txn.getTransactionSpecifics().getParentSpecifics().getName();
					} else {
						itemParentName = txn.getTransactionSpecifics().getParticularsId().getName();
					}
				}
				String txnSpecialStatus = "";
				if (txn.getTransactionExceedingBudget() != null && txn.getKlFollowStatus() != null) {
					if (txn.getTransactionExceedingBudget() == 1 && txn.getKlFollowStatus() == 0) {
						txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
					}
					if (txn.getTransactionExceedingBudget() == 1 && txn.getKlFollowStatus() == 1) {
						txnSpecialStatus = "Transaction Exceeding Budget";
					}
				}
				if (txn.getTransactionExceedingBudget() == null && txn.getKlFollowStatus() != null) {
					if (txn.getKlFollowStatus() == 0) {
						txnSpecialStatus = "Rules Not Followed";
					}
				}
				if (txn.getTransactionExceedingBudget() != null && txn.getKlFollowStatus() == null) {
					txnSpecialStatus = "Transaction Exceeding Budget";
				}
				String txnResultDesc = "";
				if (txn.getNetAmountResultDescription() != null
						&& !txn.getNetAmountResultDescription().equals("null")) {
					txnResultDesc = txn.getNetAmountResultDescription();
				}
				String invoiceNumber = txn.getInvoiceNumber() == null ? "" : txn.getInvoiceNumber();
				Integer typeOfSupply = txn.getTypeOfSupply() == null ? 0 : txn.getTypeOfSupply();
				String txnDocument = txn.getSupportingDocs() == null ? "" : txn.getSupportingDocs();
				TransactionViewResponse.addTxn(txn.getId(), txn.getTransactionBranch().getName(), projectName, itemName,
						itemParentName, txnCustomer.getName(), txn.getTransactionPurpose().getTransactionPurpose(),
						idosdf.format(txn.getTransactionDate()), invoiceDateLabel, invoiceDate, paymentMode,
						txn.getNoOfUnits(), txn.getPricePerUnit(), txn.getGrossAmount(), txn.getNetAmount(),
						txnResultDesc, "", txn.getTransactionStatus(), txn.getCreatedBy().getEmail(), "", "",
						txnDocument, txnRemarks, debitCredit, txnSpecialStatus, 0.0, "",
						txnInstrumentNumber, txnInstrumentDate, txn.getTransactionPurpose().getId(), invoiceNumber,
						txn.getTransactionRefNumber(), typeOfSupply, result);
				transactionService.sendStockWebSocketResponse(entityManager, txn, user, result);
			} else if (IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER == txnPurposeVal) {
				// in COA user need to do the mapping "is this where you classify advance
				// received from customer" before passing any transaction of that type else we
				// have issue showing it in TB
				Specifics specificsForMapping = coaService.getSpecificsForMapping(user, "6", entityManager);
				if (specificsForMapping == null) {
					throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
							"COA mapping is not found",
							"COA mapping not found for, type 6: advance received from customers / debtors");
				}
				result.put("recAdvFromCustCOAType", specificsForMapping.getId());
				if (specificsForMapping == null) {
					return Results.ok(result);
				}
				txn = transactionService.submitForAccountingRecCust(user, json, entityManager, entitytransaction,
						result);
			} else if (IdosConstants.REVERSAL_OF_ITC == txnPurposeVal) {
				txn = REVERSAL_ITC_DAO.submitForAccounting(user, json, entityManager, entitytransaction, usertxnPurpose,
						result);
				if (txn != null) {
					entitytransaction.commit();
				}
			} else if (IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT == txnPurposeVal) {
				txn = buyTransactionService.submit4AccoutingBuyOnPetty(user, entityManager, json, usertxnPurpose,
						result);
				if (txn != null) {
					INVOICE_SERVICE.saveInvoiceLog(user, entityManager, txn, null, json);
				}
				entitytransaction.commit();
			}
			if (txn != null) {
				result.put(TRANSACTION_ID, txn.getId());
				result.put(TRANSACTION_REF_NO, txn.getTransactionRefNumber());
			}
		} catch (Exception ex) {
			reportException(entityManager, entitytransaction, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, entitytransaction, user, th, result);
		}
		log.log(Level.FINE, ">>>> End");
		return Results.ok(result);
	}

	@Transactional
	public Result submitForApproval(Request request) {
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		ArrayNode singleUserAccounting = result.putArray("singleUserAccounting");
		Users user = null;
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			entitytransaction.begin();
			Map<String, Object> criterias = new HashMap<String, Object>(3);
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start " + json);
			String useremail = json.findValue("useremail").asText();
			// session.adding("email", useremail);
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			result.put("useremail", user.getEmail());
			String role = "";
			List<UsersRoles> userRoles = UsersRoles.getUserRoleList(entityManager, user.getOrganization().getId(),
					user.getId(),
					user.getBranch().getId());
			for (UsersRoles roles : userRoles) {
				role += roles.getRole().getName() + ",";
			}
			role = role.substring(0, role.length() - 1);
			result.put("role", role);
			String txnPurpose = json.findValue("txnPurpose").asText();
			long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
			TransactionPurpose usertxnPurpose = TransactionPurpose.findById(txnPurposeVal);
			String txnInstrumentNumber = "";
			String txnInstrumentDate = "";
			Transaction txn = null;
			BillOfMaterialTxnModel billOfMaterialTxn = null;
			PurchaseRequisitionTxnModel purchaseRequisitionTxn = null;
			PurchaseOrderTxnModel purchaseOrderTxn = null;
			if (IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY == txnPurposeVal
					|| IdosConstants.BUY_ON_CREDIT_PAY_LATER == txnPurposeVal) {
				txn = buyTransactionService.submitForApproval(user, json, entityManager, entitytransaction,
						usertxnPurpose, result);
				if (txn != null) {
					INVOICE_SERVICE.saveInvoiceLog(user, entityManager, txn, null, json);
				}
				entitytransaction.commit();
				// Single User Submit
				if (ConfigParams.getInstance().isDeploymentSingleUser(user)) {
					ObjectNode createSingleuserJson = singleUserService.createSingleuserJson(txn, json, user);
					singleUserAccounting.add(createSingleuserJson);
				}
			} else if (IdosConstants.PAY_VENDOR_SUPPLIER == txnPurposeVal) {
				txn = transactionService.submitForApprovalPayVendor(user, json, entityManager, usertxnPurpose, result);
				entitytransaction.commit();
				// Single User Submit
				if (ConfigParams.getInstance().isDeploymentSingleUser(user)) {
					ObjectNode createSingleuserJson = singleUserService.createSingleuserJson(txn, json, user);
					singleUserAccounting.add(createSingleuserJson);
				}
			} else if (IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER == txnPurposeVal) {
				// in COA user need to do the mapping "is this where you classify advance
				// received from vendor" before passing any transaction of that type else we
				// have issue showing it in TB
				Specifics specificsForMapping = coaService.getSpecificsForMapping(user, "7", entityManager);
				if (specificsForMapping == null) {
					throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
							"COA is not found for mapping id 7",
							"TDS COA mapping not found for: advance paid to vendors / creditors");
				}
				txn = transactionService.submitForApprovalPayAdvToVend(user, json, entityManager, entitytransaction,
						result);
			} else if (IdosConstants.TRANSFER_MAIN_CASH_TO_PETTY_CASH == txnPurposeVal) {
				txn = transactionService.submitForApprovalTransferCashToPetty(user, json, entityManager, usertxnPurpose,
						result);
				entitytransaction.commit();
				// Single User
				if (ConfigParams.getInstance().isDeploymentSingleUser(user)) {
					ObjectNode createSingleuserJson = singleUserService.createSingleuserJson(txn, json, user);
					singleUserAccounting.add(createSingleuserJson);
				}
			} else if (txnPurpose.equals("Pay special adjustments amount to vendors")) {
				String txnPSAATVCreditVendor = json.findValue("txnPSAATVCreditVendor").asText();
				String txnPSAATVAmountPaid = json.findValue("txnPSAATVAmountPaid").asText();
				String txnPSAATVForProject = json.findValue("txnPSAATVForProject") != null
						? json.findValue("txnPSAATVForProject").asText()
						: null;
				String supportingdoc = json.findValue("supportingdoc").asText();
				String txnremarks = json.findValue("txnremarks").asText();
				String klfollowednotfollowed = json.findValue("klfollowednotfollowed").asText();
				Transaction transaction = transactionDao.paySpecialAdjustmentsToVendors(txnPurpose, usertxnPurpose,
						user, txnPSAATVCreditVendor, txnPSAATVAmountPaid, txnPSAATVForProject, supportingdoc,
						txnremarks, klfollowednotfollowed, entitytransaction, entityManager);
				String branchName = "";
				String projectName = "";
				String itemName = "";
				String itemParentName = "";
				String paymentMode = "";
				String invoiceDate = "";
				String invoiceDateLabel = "";
				if (txn.getTransactionInvoiceDate() != null) {
					invoiceDateLabel = "INVOICE DATE:";
					invoiceDate = idosdf.format(txn.getTransactionInvoiceDate());
				}
				String approverEmails = "";
				String additionalApprovarUsers = "";
				String selectedAdditionalApproval = "";
				String txnRemarks = "";
				String debitCredit = "Credit";
				String txnSpecialStatus = "";
				String approverEmail = "";
				String approverLabel = "";
				if (txn.getApproverActionBy() != null) {
					approverLabel = "APPROVER:";
					approverEmail = txn.getApproverActionBy().getEmail();
				}
				if (txn.getTransactionExceedingBudget() != null && txn.getKlFollowStatus() != null) {
					if (txn.getTransactionExceedingBudget() == 1 && txn.getKlFollowStatus() == 0) {
						txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
					}
					if (txn.getTransactionExceedingBudget() == 1 && txn.getKlFollowStatus() == 1) {
						txnSpecialStatus = "Transaction Exceeding Budget";
					}
				}
				if (txn.getTransactionExceedingBudget() == null && txn.getKlFollowStatus() != null) {
					if (txn.getKlFollowStatus() == 0) {
						txnSpecialStatus = "Rules Not Followed";
					}
				}
				if (txn.getTransactionExceedingBudget() != null && txn.getKlFollowStatus() == null) {
					txnSpecialStatus = "Transaction Exceeding Budget";
				}
				if (txn.getTransactionBranch() != null) {
					branchName = txn.getTransactionBranch().getName();
				}
				if (txn.getTransactionProject() != null) {
					projectName = txn.getTransactionProject().getName();
				}
				if (txn.getApproverActionBy() != null) {
					approverEmail = txn.getApproverActionBy().getEmail();
				}
				if (txn.getAdditionalApproverEmails() != null) {
					additionalApprovarUsers = txn.getAdditionalApproverEmails();
				}
				if (txn.getRemarks() != null) {
					txnRemarks = txn.getRemarks();
				}
				if (!ConfigParams.getInstance().isDeploymentSingleUser(user)) {
					String txnResultDesc = "";
					if (txn.getNetAmountResultDescription() != null
							&& !txn.getNetAmountResultDescription().equals("null")) {
						txnResultDesc = txn.getNetAmountResultDescription();
					}
					Integer typeOfSupply = txn.getTypeOfSupply() == null ? 0 : txn.getTypeOfSupply();
					String txnDocument = txn.getSupportingDocs() == null ? "" : txn.getSupportingDocs();
					TransactionViewResponse.addActionTxn(txn.getId(), branchName, projectName, itemName, itemParentName,
							"", "",
							"", "", txn.getTransactionVendorCustomer().getName(),
							txn.getTransactionPurpose().getTransactionPurpose(),
							idosdf.format(txn.getTransactionDate()), invoiceDateLabel, invoiceDate, paymentMode, 0.0,
							0.0, 0.0, txn.getNetAmount(), txnResultDesc, "", txn.getTransactionStatus(),
							txn.getCreatedBy().getEmail(), approverLabel, approverEmail, txnDocument, txnRemarks,
							debitCredit, approverEmails, additionalApprovarUsers, selectedAdditionalApproval,
							txnSpecialStatus, 0.0, "", txnInstrumentNumber, txnInstrumentDate,
							txn.getTransactionPurpose().getId(), "", "", 0, txn.getTransactionRefNumber(), 0l, 0.0, 0,
							typeOfSupply, result);
				}
			} else if (txnPurpose.equals("Sales returns")) {
				txn = sellTransactionService.submitForApprovalSalesReturns(user, json, entityManager,
						entitytransaction, result);
			} else if (txnPurpose.equals("Purchase returns")) {
				txn = buyTransactionService.submitForApprovalPurchaseReturn(user, json, entityManager,
						entitytransaction, usertxnPurpose, result);
			} else if (IdosConstants.MAKE_PROVISION_JOURNAL_ENTRY == txnPurposeVal
					|| txnPurpose.equals("Journal Entry")) {
				IdosProvisionJournalEntry newProvJournalEntry = new IdosProvisionJournalEntry();
				result = provisionJournalEntryService.provisionJournalEntry(result, json, user, entityManager,
						entitytransaction, newProvJournalEntry);
				if (result.findValue("validTransactionDate") != null
						&& result.findValue("validTransactionDate").asInt() == 1) {
					if (!ConfigParams.getInstance().isDeploymentSingleUser(user)) {
						provisionJournalEntryService.sendProvisionWebSocketResponse(newProvJournalEntry, user,
								entityManager, result);
					}
				}
				if (newProvJournalEntry != null) {
					result.put(TRANSACTION_ID, newProvJournalEntry.getId());
					result.put(TRANSACTION_REF_NO, newProvJournalEntry.getTransactionRefNumber());
				}
			} else if (txnPurpose.equals("Withdraw Cash From Bank") || txnPurpose.equals("Deposit Cash In Bank")
					|| txnPurpose.equals("Transfer Funds From One Bank To Another")) {
				txn = transactionService.bankServices(result, json, user, entityManager, entitytransaction);
				if (!ConfigParams.getInstance().isDeploymentSingleUser(user)) {
					transactionService.sendBankWebSocketResponse(entityManager, txn, user, result);
				}
			} else if (txnPurpose.equals("Inventory Opening Balance")) {
				txn = stockService.openingStockInventoryItem(result, json, user, entityManager, entitytransaction);
				if (!ConfigParams.getInstance().isDeploymentSingleUser(user)) {
					transactionService.sendStockWebSocketResponse(entityManager, txn, user, result);
				}
			} else if (IdosConstants.PREPARE_QUOTATION == txnPurposeVal) {
				txn = quotationProformaService.submitForApprovalQuotation(user, json, entityManager, entitytransaction,
						result);
			} else if (IdosConstants.PROFORMA_INVOICE == txnPurposeVal) {
				txn = quotationProformaService.submitForApprovalProforma(user, json, entityManager, entitytransaction,
						result);
			} else if (IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW == txnPurposeVal
					|| IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER == txnPurposeVal) {
				txn = sellTransactionService.submitForApproval(user, json, entityManager, entitytransaction, result);
			} else if (IdosConstants.CREDIT_NOTE_CUSTOMER == txnPurposeVal
					|| IdosConstants.DEBIT_NOTE_CUSTOMER == txnPurposeVal) {
				txn = transactionService.submitForApprovalNote(user, json, entityManager, entitytransaction, result);
			} else if (IdosConstants.CREDIT_NOTE_VENDOR == txnPurposeVal
					|| IdosConstants.DEBIT_NOTE_VENDOR == txnPurposeVal) {
				txn = transactionService.submitForApprovalVendorNote(user, json, entityManager, entitytransaction,
						usertxnPurpose, result);
			} else if (IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER == txnPurposeVal) {
				txn = transactionService.submitForApprovalInterBranchTransfer(user, json, entityManager,
						entitytransaction, usertxnPurpose, result);
			} else if (IdosConstants.REFUND_ADVANCE_RECEIVED == txnPurposeVal) {
				txn = transactionService.submitForApprovalRefundAdvanceRecived(user, json, entityManager,
						entitytransaction, usertxnPurpose, result);
			} else if (IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE == txnPurposeVal) {
				txn = transactionService.submitForApprovalRefundAmountRecivedAgainstInvoice(user, json, entityManager,
						entitytransaction, usertxnPurpose, result);
			} else if (IdosConstants.BILL_OF_MATERIAL == txnPurposeVal) {
				billOfMaterialTxn = BILL_OF_MATERIAL_TXN_SERVICE.submitForApproval(user, json, entityManager, result);
				entitytransaction.commit();
			} else if (IdosConstants.CREATE_PURCHASE_ORDER == txnPurposeVal) {
				// billOfMaterialTxn = BILL_OF_MATERIAL_TXN_SERVICE.submitForApprovalPo(user,
				// json, entityManager, result);
				purchaseOrderTxn = purchaseOrderService.submitForApprovalPurchaseOrder(user, json, entityManager,
						result);
				entitytransaction.commit();
			} else if (IdosConstants.CREATE_PURCHASE_REQUISITION == txnPurposeVal) {
				// billOfMaterialTxn =
				// BILL_OF_MATERIAL_TXN_SERVICE.submitForApprovalPurchaseRequisition(user, json,
				// entityManager, result);
				// create purchaseRequisitionTxnService
				// create purchaseRequisitionTxnServiceImpl
				// create purchaseRequisitionTxnModel
				// create purchaseRequisitionTxnItemModel
				purchaseRequisitionTxn = PURCHASE_REQUISITION_TXN_SERVICE.submitForApprovalPurchaseRequisition(user,
						json, entityManager, result);
				entitytransaction.commit();
			} else if (IdosConstants.CANCEL_INVOICE == txnPurposeVal) {
				txn = sellTransactionService.submitForCancellation(user, json, entityManager, entitytransaction,
						result);
			}

			if (txn != null) {
				result.put(TRANSACTION_ID, txn.getId());
				result.put(TRANSACTION_REF_NO, txn.getTransactionRefNumber());
			} else if (billOfMaterialTxn != null) {
				result.put(TRANSACTION_ID, billOfMaterialTxn.getId());
				result.put(TRANSACTION_REF_NO, billOfMaterialTxn.getTransactionRefNumber());
			} else if (purchaseRequisitionTxn != null) {
				result.put(TRANSACTION_ID, purchaseRequisitionTxn.getId());
				result.put(TRANSACTION_REF_NO, purchaseRequisitionTxn.getTransactionRefNumber());
			} else if (purchaseOrderTxn != null) {
				result.put(TRANSACTION_ID, purchaseOrderTxn.getId());
				result.put(TRANSACTION_REF_NO, purchaseOrderTxn.getTransactionRefNumber());
			}
		} catch (Exception ex) {
			reportException(entityManager, entitytransaction, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, entitytransaction, user, th, result);
		}
		log.log(Level.FINE, ">>>> End");
		return Results.ok(result);
	}

	@Transactional
	public Result getTxnBnchSpecfKL(Request request) {

		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode getbanchpettycashtxnan = result.putArray("branchPettyCashTxnData");
		ArrayNode gettxnspecfinputtaxesan = result.putArray("txnSpecificsInputTaxesData");
		ArrayNode gettxnbnchspecfklan = result.putArray("txnBranchSpecificsKLData");
		ArrayNode gettxnbnchspecfcustomeran = result.putArray("txnBranchSpecificsCustomerData");
		ArrayNode getbudgetan = result.putArray("txnBudgetData");
		ArrayNode incomeStockan = result.putArray("incomeStockData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start " + json);
			String userTxnPurposeText = json.findValue("userTxnPurposeText").asText();
			long userTxnPurposeId = json.findValue("txnPurposeValue").asLong();
			long txnBranchId = json.findValue("txnBranchId").asLong();
			String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
			Date txnDate = IdosUtil.getFormatedDateWithTime(selectedTxnDate);
			// Branch bnch = Branch.findById(txnBranchId);
			String txnSpecificsId = json.findValue("txnSpecificsId") == null ? "0"
					: json.findValue("txnSpecificsId").asText();
			Branch txnBranch = Branch.findById(txnBranchId);
			Branch bnch = txnBranch;
			List<BranchDepositBoxKey> branchSafeDepositBoxes = txnBranch.getBranchDepositKeys();
			for (BranchDepositBoxKey bnchSafeDepBox : branchSafeDepositBoxes) {
				if (bnchSafeDepBox.getCashierEmail() != null && !bnchSafeDepBox.getCashierEmail().equals("")) {
					ObjectNode pettyrow = Json.newObject();
					pettyrow.put("approvalRequired", bnchSafeDepBox.getPettyCashTxnApprovalRequired());
					if (bnchSafeDepBox.getPettyCashTxnApprovalRequired() == 1) {
						if (bnchSafeDepBox.getApprovalAmountLimit() != null) {
							pettyrow.put("approvalAmountLimit", bnchSafeDepBox.getApprovalAmountLimit());
						} else {
							pettyrow.put("approvalAmountLimit", "0.0");
						}
					}
					StringBuilder newsbquery = new StringBuilder(
							"select obj from BranchCashCount obj WHERE obj.branch.id='" + txnBranch.getId()
									+ "' AND obj.organization.id='" + txnBranch.getOrganization().getId()
									+ "' and obj.presentStatus=1 ORDER BY obj.date desc");
					List<BranchCashCount> prevBranchCashCount = genericDAO
							.executeSimpleQueryWithLimit(newsbquery.toString(), entityManager, 1);
					if (prevBranchCashCount.size() > 0) {
						if (prevBranchCashCount.get(0).getResultantPettyCash() != null) {
							pettyrow.put("resultantPettyCash", prevBranchCashCount.get(0).getResultantPettyCash());
						} else {
							pettyrow.put("resultantPettyCash", "0.0");
						}
					} else {
						pettyrow.put("resultantPettyCash", "0.0");
					}
					getbanchpettycashtxnan.add(pettyrow);
				}
			}
			if (branchSafeDepositBoxes.size() <= 0) {
				ObjectNode pettyrow = Json.newObject();
				pettyrow.put("approvalRequired", "0");
				pettyrow.put("resultantPettyCash", "0.0");
				getbanchpettycashtxnan.add(pettyrow);
			}

			criterias.clear();
			criterias.put("branch.id", txnBranchId);
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("specifics.id", IdosUtil.convertStringToLong(txnSpecificsId));
			Specifics specf = Specifics.findById(IdosUtil.convertStringToLong(txnSpecificsId));
			if (specf != null && specf.getIsPriceInclusive() != null)
				result.put("isItemTaxInclusive", specf.getIsPriceInclusive());
			else
				result.put("isItemTaxInclusive", "");

			if (specf.getGstTaxRateSelected() != null) {
				if (specf.getGstTaxRateSelected().contains(IdosConstants.OUTPUT_TAX_OTHER)) {
					String rates = specf.getGstTaxRateSelected();
					if (specf.getGstTaxRate() != null) {
						rates = rates.replace(IdosConstants.OUTPUT_TAX_OTHER, specf.getGstTaxRate().toString());
					} else {
						rates = rates.replace("," + IdosConstants.OUTPUT_TAX_OTHER, "");
					}
					result.put("gstRateSelected", rates);
				} else {
					result.put("gstRateSelected", specf.getGstTaxRateSelected());
				}
			} else {
				result.put("gstRateSelected", "");
			}

			if (specf.getCessTaxRateSelected() != null) {
				if (specf.getCessTaxRateSelected().contains(IdosConstants.OUTPUT_TAX_OTHER)) {
					String rates = specf.getCessTaxRateSelected();
					if (specf.getCessTaxRate() != null) {
						rates = rates.replace(IdosConstants.OUTPUT_TAX_OTHER, specf.getCessTaxRate().toString());
					} else {
						rates = rates.replace("," + IdosConstants.OUTPUT_TAX_OTHER, "");
					}
					result.put("cessRateSelected", rates);
				} else {
					result.put("cessRateSelected", specf.getCessTaxRateSelected());
				}
			} else {
				result.put("cessRateSelected", "");
			}

			ObjectNode inputtaxesrow = Json.newObject();
			if (specf.getIsCaptureInputTaxes() != null)
				inputtaxesrow.put("isitemallowsinputtaxes", specf.getIsCaptureInputTaxes());
			else
				inputtaxesrow.put("isitemallowsinputtaxes", "");
			gettxnspecfinputtaxesan.add(inputtaxesrow);
			inputtaxesrow.put("presentStatus", 1);
			List<SpecificsKnowledgeLibraryForBranch> specfKlForBranch = genericDAO
					.findByCriteria(SpecificsKnowledgeLibraryForBranch.class, criterias, entityManager);
			if (specfKlForBranch.size() > 0) {
				for (SpecificsKnowledgeLibraryForBranch specificsBnchkl : specfKlForBranch) {
					ObjectNode row = Json.newObject();
					row.put("klContent", specificsBnchkl.getSpecificsKl().getKnowledgeLibraryContent());
					row.put("klIsMandatory", specificsBnchkl.getSpecificsKl().getIsMandatory());
					String specificsunitpriceforcustomers = "";
					if (specf.getIncomeSpecfPerUnitPrice() != null) {
						row.put("specificsunitpriceforcustomers",
								specificsBnchkl.getSpecifics().getIncomeSpecfPerUnitPrice());
					} else {
						row.put("specificsunitpriceforcustomers", specificsunitpriceforcustomers);
					}
					if (specf.getParentSpecifics() != null && !specf.getParentSpecifics().equals("")) {
						row.put("itemParentName", "Immediate Parent: " + specf.getParentSpecifics().getName());
					} else {
						row.put("itemParentName", "Immediate Parent: " + specf.getParticularsId().getName());
					}
					gettxnbnchspecfklan.add(row);
				}
			} else {
				ObjectNode row = Json.newObject();
				String specificsunitpriceforcustomers = "";
				if (specf.getIncomeSpecfPerUnitPrice() != null) {
					row.put("specificsunitpriceforcustomers", specf.getIncomeSpecfPerUnitPrice());
				} else {
					row.put("specificsunitpriceforcustomers", specificsunitpriceforcustomers);
				}
				if (specf.getParentSpecifics() != null && !specf.getParentSpecifics().equals("")) {
					row.put("itemParentName", "Immediate Parent: " + specf.getParentSpecifics().getName());
				} else {
					row.put("itemParentName", "Immediate Parent: " + specf.getParticularsId().getName());
				}
				gettxnbnchspecfklan.add(row);
			}

			if (userTxnPurposeText.equals("Sell on cash & collect payment now")) {
				// get all the cusomers which is mapped with this item and available for the
				// branch
				if (!txnSpecificsId.equals("") && txnSpecificsId != null) {
					Specifics txnSpecifics = Specifics.findById(IdosUtil.convertStringToLong(txnSpecificsId));
					StringBuilder sbquery = new StringBuilder("");
					sbquery.append("select obj from VendorSpecific obj WHERE obj.organization='"
							+ user.getOrganization().getId() + "' and obj.specificsVendors='" + txnSpecifics.getId()
							+ "' and obj.vendorSpecific.type=2 and (obj.vendorSpecific.purchaseType=1 or obj.vendorSpecific.purchaseType=2) and obj.presentStatus=1");
					List<VendorSpecific> customersForTheTxnSpecifics = genericDAO.executeSimpleQuery(sbquery.toString(),
							entityManager);
					for (VendorSpecific custForTxnSpecf : customersForTheTxnSpecifics) {
						criterias.clear();
						criterias.put("branch.id", txnBranchId);
						criterias.put("organization.id", user.getOrganization().getId());
						criterias.put("vendor.id", custForTxnSpecf.getVendorSpecific().getId());
						criterias.put("presentStatus", 1);
						BranchVendors customerAvailableForTxnBranch = genericDAO.getByCriteria(BranchVendors.class,
								criterias, entityManager);
						if (customerAvailableForTxnBranch != null) {
							ObjectNode row = Json.newObject();
							row.put("customerName", customerAvailableForTxnBranch.getVendor().getName());
							row.put("customerId", customerAvailableForTxnBranch.getVendor().getId());
							gettxnbnchspecfcustomeran.add(row);
						}
					}
					// append list of walk-in customers who have paid advacne using "Receive adv
					// from customers
					sbquery = new StringBuilder("");
					sbquery.append("select obj from VendorSpecific obj WHERE obj.organization='"
							+ user.getOrganization().getId() + "' and obj.branch='" + user.getBranch().getId()
							+ "' and obj.specificsVendors='" + txnSpecifics.getId()
							+ "' and obj.vendorSpecific.type=3 and obj.presentStatus=1 and obj.vendorSpecific.purchaseType=1 and obj.advanceMoney>0");
					List<VendorSpecific> walkincustomersForTheTxnSpecifics = genericDAO
							.executeSimpleQuery(sbquery.toString(), entityManager);
					for (VendorSpecific custForTxnSpecf : walkincustomersForTheTxnSpecifics) {
						ObjectNode row = Json.newObject();
						row.put("customerName", custForTxnSpecf.getVendorSpecific().getName() + ":Walk-In Customer");
						row.put("customerId", custForTxnSpecf.getVendorSpecific().getId());
						gettxnbnchspecfcustomeran.add(row);
					}
				}
				if (specf.getLinkIncomeExpenseSpecifics() != null) {
					double branchItemPresentStock = stockService.getbranchSellStockAvailableCombSales(Json.newObject(),
							specf.getId(), bnch.getId(), selectedTxnDate, 0, null, user, entityManager);
					ObjectNode row = Json.newObject();
					row.put("inventory", "InventoryItem");
					row.put("stockAvailable", branchItemPresentStock);
					incomeStockan.add(row);
				} else {
					ObjectNode row = Json.newObject();
					row.put("inventory", "notInventoryItem");
					incomeStockan.add(row);
				}
			} else if (userTxnPurposeText.equals("Sell on credit & collect payment later")) {
				// get all the cusomers which is mapped with this item and available for the
				// branch
				if (!txnSpecificsId.equals("") && txnSpecificsId != null) {
					Specifics txnSpecifics = Specifics.findById(IdosUtil.convertStringToLong(txnSpecificsId));
					StringBuilder sbquery = new StringBuilder("");
					sbquery.append("select obj from VendorSpecific obj WHERE obj.organization='"
							+ user.getOrganization().getId() + "' and obj.specificsVendors='" + txnSpecifics.getId()
							+ "' and obj.vendorSpecific.type=2 and obj.presentStatus=1 and (obj.vendorSpecific.purchaseType=0 or obj.vendorSpecific.purchaseType=2)");
					List<VendorSpecific> customersForTheTxnSpecifics = genericDAO.executeSimpleQuery(sbquery.toString(),
							entityManager);
					for (VendorSpecific custForTxnSpecf : customersForTheTxnSpecifics) {
						criterias.clear();
						criterias.put("branch.id", txnBranchId);
						criterias.put("organization.id", user.getOrganization().getId());
						criterias.put("vendor.id", custForTxnSpecf.getVendorSpecific().getId());
						criterias.put("presentStatus", 1);
						BranchVendors customerAvailableForTxnBranch = genericDAO.getByCriteria(BranchVendors.class,
								criterias, entityManager);
						if (customerAvailableForTxnBranch != null) {
							ObjectNode row = Json.newObject();
							row.put("customerName", customerAvailableForTxnBranch.getVendor().getName());
							row.put("customerId", customerAvailableForTxnBranch.getVendor().getId());
							gettxnbnchspecfcustomeran.add(row);
						}
					}
				}
				if (specf.getLinkIncomeExpenseSpecifics() != null) {
					double branchItemPresentStock = stockService.getbranchSellStockAvailableCombSales(Json.newObject(),
							specf.getId(), bnch.getId(), selectedTxnDate, 0, null, user, entityManager);
					ObjectNode row = Json.newObject();
					row.put("inventory", "InventoryItem");
					row.put("stockAvailable", branchItemPresentStock);
					incomeStockan.add(row);
				} else {
					ObjectNode row = Json.newObject();
					row.put("inventory", "notInventoryItem");
					incomeStockan.add(row);
				}
			} else if (userTxnPurposeText.equals("Buy on cash & pay right away")
					|| userTxnPurposeText.equals("Buy on Petty Cash Account")) {
				// get all the vendors which is mapped with this item and avaible for the
				// selected branch
				if (!txnSpecificsId.equals("") && txnSpecificsId != null) {
					Specifics txnSpecifics = Specifics.findById(IdosUtil.convertStringToLong(txnSpecificsId));
					StringBuilder sbquery = new StringBuilder("");
					sbquery.append("select obj from VendorSpecific obj WHERE obj.organization='"
							+ user.getOrganization().getId() + "' and obj.specificsVendors='" + txnSpecifics.getId()
							+ "' and obj.vendorSpecific.type=1 and obj.presentStatus=1 and (obj.vendorSpecific.purchaseType=1 or obj.vendorSpecific.purchaseType=2)");
					List<VendorSpecific> customersForTheTxnSpecifics = genericDAO.executeSimpleQuery(sbquery.toString(),
							entityManager);
					for (VendorSpecific custForTxnSpecf : customersForTheTxnSpecifics) {
						criterias.clear();
						criterias.put("branch.id", txnBranchId);
						criterias.put("organization.id", user.getOrganization().getId());
						criterias.put("vendor.id", custForTxnSpecf.getVendorSpecific().getId());
						criterias.put("presentStatus", 1);
						BranchVendors customerAvailableForTxnBranch = genericDAO.getByCriteria(BranchVendors.class,
								criterias, entityManager);
						if (customerAvailableForTxnBranch != null) {
							ObjectNode row = Json.newObject();
							row.put("customerName", customerAvailableForTxnBranch.getVendor().getName());
							row.put("customerId", customerAvailableForTxnBranch.getVendor().getId());
							gettxnbnchspecfcustomeran.add(row);
						}
					}
					// give back the budget of the branch specifics
					int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
					criterias.clear();
					criterias.put("branch.id", txnBranchId);
					criterias.put("specifics.id", txnSpecifics.getId());
					criterias.put("organization.id", user.getOrganization().getId());
					criterias.put("particular.id", txnSpecifics.getParticularsId().getId());
					criterias.put("presentStatus", 1);
					BranchSpecifics txnBnchSpef = genericDAO.getByCriteria(BranchSpecifics.class, criterias,
							entityManager);
					Double monthBudgetForBranchTxnSpecific = null;
					Double monthBudgetAvailableForBranchTxnSpecific = null;
					if (txnBnchSpef != null) {
						switch (currentMonth) {
							case 1:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountJan() != null
										? txnBnchSpef.getBudgetAmountJan()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountJan() != null) {
									monthBudgetAvailableForBranchTxnSpecific = monthBudgetForBranchTxnSpecific
											- txnBnchSpef.getBudgetDeductedAmountJan();
								}
								break;
							case 2:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountFeb() != null
										? txnBnchSpef.getBudgetAmountFeb()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountFeb() != null) {
									monthBudgetAvailableForBranchTxnSpecific = monthBudgetForBranchTxnSpecific
											- txnBnchSpef.getBudgetDeductedAmountFeb();
								}
								break;
							case 3:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountMar() != null
										? txnBnchSpef.getBudgetAmountMar()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountMar() != null) {
									monthBudgetAvailableForBranchTxnSpecific = monthBudgetForBranchTxnSpecific
											- txnBnchSpef.getBudgetDeductedAmountMar();
								}
								break;
							case 4:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountApr() != null
										? txnBnchSpef.getBudgetAmountApr()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountApr() != null) {
									monthBudgetAvailableForBranchTxnSpecific = monthBudgetForBranchTxnSpecific
											- txnBnchSpef.getBudgetDeductedAmountApr();
								}
								break;
							case 5:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountMay() != null
										? txnBnchSpef.getBudgetAmountMay()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountMay() != null) {
									monthBudgetAvailableForBranchTxnSpecific = monthBudgetForBranchTxnSpecific
											- txnBnchSpef.getBudgetDeductedAmountMay();
								}
								break;
							case 6:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountJune() != null
										? txnBnchSpef.getBudgetAmountJune()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountJune() != null) {
									monthBudgetAvailableForBranchTxnSpecific = monthBudgetForBranchTxnSpecific
											- txnBnchSpef.getBudgetDeductedAmountJune();
								}
								break;
							case 7:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountJuly() != null
										? txnBnchSpef.getBudgetAmountJuly()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountJuly() != null) {
									monthBudgetAvailableForBranchTxnSpecific = monthBudgetForBranchTxnSpecific
											- txnBnchSpef.getBudgetDeductedAmountJuly();
								}
								break;
							case 8:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountAug() != null
										? txnBnchSpef.getBudgetAmountAug()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountAug() != null) {
									monthBudgetAvailableForBranchTxnSpecific = monthBudgetForBranchTxnSpecific
											- txnBnchSpef.getBudgetDeductedAmountAug();
								}
								break;
							case 9:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountSep() != null
										? txnBnchSpef.getBudgetAmountSep()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountSep() != null) {
									monthBudgetAvailableForBranchTxnSpecific = monthBudgetForBranchTxnSpecific
											- txnBnchSpef.getBudgetDeductedAmountSep();
								}
								break;
							case 10:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountOct() != null
										? txnBnchSpef.getBudgetAmountOct()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountOct() != null) {
									monthBudgetAvailableForBranchTxnSpecific = monthBudgetForBranchTxnSpecific
											- txnBnchSpef.getBudgetDeductedAmountOct();
								}
								break;
							case 11:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountNov() != null
										? txnBnchSpef.getBudgetAmountNov()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountNov() != null) {
									monthBudgetAvailableForBranchTxnSpecific = monthBudgetForBranchTxnSpecific
											- txnBnchSpef.getBudgetDeductedAmountNov();
								}
								break;
							case 12:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountDec() != null
										? txnBnchSpef.getBudgetAmountDec()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountDec() != null) {
									monthBudgetAvailableForBranchTxnSpecific = monthBudgetForBranchTxnSpecific
											- txnBnchSpef.getBudgetDeductedAmountDec();
								}
								break;
						}
					}
					ObjectNode budgetrow = Json.newObject();
					if (monthBudgetForBranchTxnSpecific != null) {
						budgetrow.put("monthBudgetForBranchTxnSpecific", monthBudgetForBranchTxnSpecific);
					} else {
						budgetrow.put("monthBudgetForBranchTxnSpecific", "0.0");
					}
					if (monthBudgetAvailableForBranchTxnSpecific != null) {
						budgetrow.put("monthBudgetAvailableForBranchTxnSpecific",
								monthBudgetAvailableForBranchTxnSpecific);
					} else {
						if (monthBudgetForBranchTxnSpecific != null) {
							budgetrow.put("monthBudgetAvailableForBranchTxnSpecific", monthBudgetForBranchTxnSpecific);
						} else {
							budgetrow.put("monthBudgetAvailableForBranchTxnSpecific", "0.0");
						}
					}
					criterias.clear();
					criterias.put("user.id", user.getId());
					criterias.put("userRights.id", 1L);
					criterias.put("user.organization.id", user.getOrganization().getId());
					criterias.put("specifics.id", specf.getId());
					criterias.put("particulars.id", specf.getParticularsId().getId());
					UserRightSpecifics userRightSpecf = genericDAO.getByCriteria(UserRightSpecifics.class, criterias,
							entityManager);
					if (userRightSpecf != null) {
						if (userRightSpecf.getAmount() != null) {
							budgetrow.put("userTxnAmountFrom", userRightSpecf.getAmount());
						} else {
							budgetrow.put("userTxnAmountFrom", "0.0");
						}
						if (userRightSpecf.getAmountTo() != null) {
							budgetrow.put("userTxnAmountTo", userRightSpecf.getAmountTo());
						} else {
							budgetrow.put("userTxnAmountTo", "0.0");
						}
					} else {
						budgetrow.put("userTxnAmountFrom", "0.0");
						budgetrow.put("userTxnAmountTo", "0.0");
					}
					getbudgetan.add(budgetrow);
				}
				if (specf != null && specf.getIsTradingInvenotryItem() != null
						&& specf.getIsTradingInvenotryItem() == 1) {
					double purchaseStockForInventoryItem = stockService.getPurchaseStockForInventoryItem(bnch, txnDate,
							specf, user, entityManager);
					double sellStockForInventoryItem = stockService.getSellStockForInventoryItem(bnch, txnDate, specf,
							user, entityManager);
					ObjectNode row = Json.newObject();
					row.put("inventory", "BuyInventoryItem");
					row.put("stockAvailable", (purchaseStockForInventoryItem - sellStockForInventoryItem));
					incomeStockan.add(row);
				} else {
					ObjectNode row = Json.newObject();
					row.put("inventory", "notInventoryItem");
					incomeStockan.add(row);
				}
			} else if (userTxnPurposeText.equals("Buy on credit & pay later")) {
				// get all the vendors which is mapped with this item and avaible for the
				// selected branch
				if (!txnSpecificsId.equals("") && txnSpecificsId != null) {
					Specifics txnSpecifics = Specifics.findById(IdosUtil.convertStringToLong(txnSpecificsId));
					StringBuilder sbquery = new StringBuilder("");
					sbquery.append("select obj from VendorSpecific obj WHERE obj.organization='"
							+ user.getOrganization().getId() + "' and obj.specificsVendors='" + txnSpecifics.getId()
							+ "' and obj.vendorSpecific.type=1 and obj.presentStatus=1 and (obj.vendorSpecific.purchaseType=0 or obj.vendorSpecific.purchaseType=2)");
					List<VendorSpecific> customersForTheTxnSpecifics = genericDAO.executeSimpleQuery(sbquery.toString(),
							entityManager);
					for (VendorSpecific custForTxnSpecf : customersForTheTxnSpecifics) {
						criterias.clear();
						criterias.put("branch.id", txnBranchId);
						criterias.put("organization.id", user.getOrganization().getId());
						criterias.put("vendor.id", custForTxnSpecf.getVendorSpecific().getId());
						BranchVendors customerAvailableForTxnBranch = genericDAO.getByCriteria(BranchVendors.class,
								criterias, entityManager);
						if (customerAvailableForTxnBranch != null) {
							ObjectNode row = Json.newObject();
							row.put("customerName", customerAvailableForTxnBranch.getVendor().getName());
							row.put("customerId", customerAvailableForTxnBranch.getVendor().getId());
							gettxnbnchspecfcustomeran.add(row);
						}
					}
					// give back the budget of the branch specifics
					int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
					criterias.clear();
					criterias.put("branch.id", txnBranchId);
					criterias.put("specifics.id", txnSpecifics.getId());
					criterias.put("organization.id", user.getOrganization().getId());
					criterias.put("particular.id", txnSpecifics.getParticularsId().getId());
					criterias.put("presentStatus", 1);
					BranchSpecifics txnBnchSpef = genericDAO.getByCriteria(BranchSpecifics.class, criterias,
							entityManager);
					Double monthBudgetForBranchTxnSpecific = null;
					Double monthBudgetAvailableForBranchTxnSpecific = null;
					if (txnBnchSpef != null) {
						switch (currentMonth) {
							case 1:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountJan() != null
										? txnBnchSpef.getBudgetAmountJan()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountJan() != null) {
									monthBudgetAvailableForBranchTxnSpecific = Double.parseDouble(decimalFormat.format(
											monthBudgetForBranchTxnSpecific
													- txnBnchSpef.getBudgetDeductedAmountJan()));
								}
								break;
							case 2:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountFeb() != null
										? txnBnchSpef.getBudgetAmountFeb()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountFeb() != null) {
									monthBudgetAvailableForBranchTxnSpecific = Double.parseDouble(decimalFormat.format(
											monthBudgetForBranchTxnSpecific
													- txnBnchSpef.getBudgetDeductedAmountFeb()));
								}
								break;
							case 3:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountMar() != null
										? txnBnchSpef.getBudgetAmountMar()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountMar() != null) {
									monthBudgetAvailableForBranchTxnSpecific = Double.parseDouble(decimalFormat.format(
											monthBudgetForBranchTxnSpecific
													- txnBnchSpef.getBudgetDeductedAmountMar()));
								}
								break;
							case 4:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountApr() != null
										? txnBnchSpef.getBudgetAmountApr()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountApr() != null) {
									monthBudgetAvailableForBranchTxnSpecific = Double.parseDouble(decimalFormat.format(
											monthBudgetForBranchTxnSpecific
													- txnBnchSpef.getBudgetDeductedAmountApr()));
								}
								break;
							case 5:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountMay() != null
										? txnBnchSpef.getBudgetAmountMay()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountMay() != null) {
									monthBudgetAvailableForBranchTxnSpecific = Double.parseDouble(decimalFormat.format(
											monthBudgetForBranchTxnSpecific
													- txnBnchSpef.getBudgetDeductedAmountMay()));
								}
								break;
							case 6:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountJune() != null
										? txnBnchSpef.getBudgetAmountJune()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountJune() != null) {
									monthBudgetAvailableForBranchTxnSpecific = Double.parseDouble(decimalFormat.format(
											monthBudgetForBranchTxnSpecific
													- txnBnchSpef.getBudgetDeductedAmountJune()));
								}
								break;
							case 7:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountJuly() != null
										? txnBnchSpef.getBudgetAmountJuly()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountJuly() != null) {
									monthBudgetAvailableForBranchTxnSpecific = Double.parseDouble(decimalFormat.format(
											monthBudgetForBranchTxnSpecific
													- txnBnchSpef.getBudgetDeductedAmountJuly()));
								}
								break;
							case 8:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountAug() != null
										? txnBnchSpef.getBudgetAmountAug()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountAug() != null) {
									monthBudgetAvailableForBranchTxnSpecific = Double.parseDouble(decimalFormat.format(
											monthBudgetForBranchTxnSpecific
													- txnBnchSpef.getBudgetDeductedAmountAug()));
								}
								break;
							case 9:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountSep() != null
										? txnBnchSpef.getBudgetAmountSep()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountSep() != null) {
									monthBudgetAvailableForBranchTxnSpecific = Double.parseDouble(decimalFormat.format(
											monthBudgetForBranchTxnSpecific
													- txnBnchSpef.getBudgetDeductedAmountSep()));
								}
								break;
							case 10:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountOct() != null
										? txnBnchSpef.getBudgetAmountOct()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountOct() != null) {
									monthBudgetAvailableForBranchTxnSpecific = Double.parseDouble(decimalFormat.format(
											monthBudgetForBranchTxnSpecific
													- txnBnchSpef.getBudgetDeductedAmountOct()));
								}
								break;
							case 11:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountNov() != null
										? txnBnchSpef.getBudgetAmountNov()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountNov() != null) {
									monthBudgetAvailableForBranchTxnSpecific = Double.parseDouble(decimalFormat.format(
											monthBudgetForBranchTxnSpecific
													- txnBnchSpef.getBudgetDeductedAmountNov()));
								}
								break;
							case 12:
								monthBudgetForBranchTxnSpecific = txnBnchSpef.getBudgetAmountDec() != null
										? txnBnchSpef.getBudgetAmountDec()
										: 0.0;
								if (txnBnchSpef.getBudgetDeductedAmountDec() != null) {
									monthBudgetAvailableForBranchTxnSpecific = Double.parseDouble(decimalFormat.format(
											monthBudgetForBranchTxnSpecific
													- txnBnchSpef.getBudgetDeductedAmountDec()));
								}
								break;
						}
					}
					ObjectNode budgetrow = Json.newObject();
					if (monthBudgetForBranchTxnSpecific != null) {
						budgetrow.put("monthBudgetForBranchTxnSpecific", monthBudgetForBranchTxnSpecific);
					} else {
						budgetrow.put("monthBudgetForBranchTxnSpecific", "0.0");
					}
					if (monthBudgetAvailableForBranchTxnSpecific != null) {
						budgetrow.put("monthBudgetAvailableForBranchTxnSpecific",
								monthBudgetAvailableForBranchTxnSpecific);
					} else {
						if (monthBudgetForBranchTxnSpecific != null) {
							budgetrow.put("monthBudgetAvailableForBranchTxnSpecific", monthBudgetForBranchTxnSpecific);
						} else {
							budgetrow.put("monthBudgetAvailableForBranchTxnSpecific", "0.0");
						}
					}
					criterias.clear();
					criterias.put("user.id", user.getId());
					criterias.put("userRights.id", 1L);
					criterias.put("user.organization.id", user.getOrganization().getId());
					criterias.put("specifics.id", specf.getId());
					criterias.put("particulars.id", specf.getParticularsId().getId());
					criterias.put("presentStatus", 1);
					UserRightSpecifics userRightSpecf = genericDAO.getByCriteria(UserRightSpecifics.class, criterias,
							entityManager);
					if (userRightSpecf != null) {
						if (userRightSpecf.getAmount() != null) {
							budgetrow.put("userTxnAmountFrom", userRightSpecf.getAmount());
						} else {
							budgetrow.put("userTxnAmountFrom", "0.0");
						}
						if (userRightSpecf.getAmountTo() != null) {
							budgetrow.put("userTxnAmountTo", userRightSpecf.getAmountTo());
						} else {
							budgetrow.put("userTxnAmountTo", "0.0");
						}
					} else {
						budgetrow.put("userTxnAmountFrom", "0.0");
						budgetrow.put("userTxnAmountTo", "0.0");
					}
					getbudgetan.add(budgetrow);
				}
				if (specf != null && specf.getIsTradingInvenotryItem() != null
						&& specf.getIsTradingInvenotryItem() == 1) {
					double purchaseStockForInventoryItem = stockService.getPurchaseStockForInventoryItem(bnch, txnDate,
							specf, user, entityManager);
					double sellStockForInventoryItem = stockService.getSellStockForInventoryItem(bnch, txnDate, specf,
							user, entityManager);
					ObjectNode row = Json.newObject();
					row.put("inventory", "BuyInventoryItem");
					row.put("stockAvailable", (purchaseStockForInventoryItem - sellStockForInventoryItem));
					incomeStockan.add(row);
				} else {
					ObjectNode row = Json.newObject();
					row.put("inventory", "notInventoryItem");
					incomeStockan.add(row);
				}
			} else if (userTxnPurposeId == IdosConstants.PREPARE_QUOTATION
					|| userTxnPurposeId == IdosConstants.PROFORMA_INVOICE) {
				if (!txnSpecificsId.equals("") && txnSpecificsId != null) {
					Specifics txnSpecifics = Specifics.findById(IdosUtil.convertStringToLong(txnSpecificsId));
					StringBuilder sbquery = new StringBuilder(
							"select obj from VendorSpecific obj WHERE obj.organization='"
									+ user.getOrganization().getId() + "' and obj.specificsVendors='"
									+ txnSpecifics.getId() + "' and obj.vendorSpecific.type=2 and obj.presentStatus=1");
					List<VendorSpecific> customersForTheTxnSpecifics = genericDAO.executeSimpleQuery(sbquery.toString(),
							entityManager);
					for (VendorSpecific custForTxnSpecf : customersForTheTxnSpecifics) {
						criterias.clear();
						criterias.put("branch.id", txnBranchId);
						criterias.put("organization.id", user.getOrganization().getId());
						criterias.put("vendor.id", custForTxnSpecf.getVendorSpecific().getId());
						criterias.put("presentStatus", 1);
						BranchVendors customerAvailableForTxnBranch = genericDAO.getByCriteria(BranchVendors.class,
								criterias, entityManager);
						if (customerAvailableForTxnBranch != null) {
							ObjectNode row = Json.newObject();
							row.put("customerName", customerAvailableForTxnBranch.getVendor().getName());
							row.put("customerId", customerAvailableForTxnBranch.getVendor().getId());
							gettxnbnchspecfcustomeran.add(row);
						}
					}
				}
				if (specf.getLinkIncomeExpenseSpecifics() != null) {
					Double purchaseStock = stockService.getPurchaseStockForThisIncomeLinkedExpenseItem(bnch, specf,
							user, entityManager);
					Double sellStock = stockService.getSellStockForThisIncomeItem(bnch, specf, user, entityManager);
					Double stockTranferred = stockService.getPurchaseStockTransferredItem(bnch,
							specf.getLinkIncomeExpenseSpecifics(), user, entityManager);
					Double sellStockInProgress = stockService.getPurchaseStockTransferredInProgressItem(bnch,
							specf.getLinkIncomeExpenseSpecifics(), user, entityManager);
					Double availableStock = purchaseStock - sellStock - stockTranferred - sellStockInProgress;
					ObjectNode row = Json.newObject();
					row.put("inventory", "InventoryItem");
					row.put("stockAvailable", availableStock);
					incomeStockan.add(row);
				} else {
					ObjectNode row = Json.newObject();
					row.put("inventory", "notInventoryItem");
					incomeStockan.add(row);
				}
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, ">>>> End " + result);
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getAdvanceDiscount(Request request) {
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		ArrayNode vendorAdvanceUnitPricean = result.putArray("vendorAdvanceUnitPriceData");
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start " + json);
			String useremail = json.findValue("useremail").asText();
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			int txnPurposeVal = json.findValue("txnPurposeVal").asInt();
			String txnVendCustId = json.findValue("txnVendCustId") == null ? null
					: json.findValue("txnVendCustId").asText();
			String txnVendCustItemId = json.findValue("txnVendCustItemId") == null ? null
					: json.findValue("txnVendCustItemId").asText();
			String txnTypeOfSupply = json.findValue("txnTypeOfSupply") == null ? null
					: json.findValue("txnTypeOfSupply").asText();
			Map<String, Object> criterias = new HashMap<String, Object>();
			if (txnPurposeVal == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
					|| txnPurposeVal == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
					|| txnPurposeVal == IdosConstants.CREDIT_NOTE_CUSTOMER
					|| txnPurposeVal == IdosConstants.DEBIT_NOTE_CUSTOMER
					|| txnPurposeVal == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
				sellTransactionService.getAdvanceDiscount(user, entityManager, json, result);
			} else if (txnPurposeVal == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
					|| txnPurposeVal == IdosConstants.BUY_ON_CREDIT_PAY_LATER
					|| txnPurposeVal == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT
					|| txnPurposeVal == IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER) {
				buyTransactionService.getAdvanceDiscount(user, entityManager, json, result);
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		log.log(Level.FINE, ">>>> End: " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getNetAmountTaxComponent(Request request) {
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		ArrayNode branchSpecificsTaxComponentan = result.putArray("branchSpecificsTaxComponentData");
		ArrayNode branchSpecificsTaxFormulaComponentan = result.putArray("branchSpecificsTaxFormulaComponentData");
		ArrayNode branchSpecificsTaxResultAmountan = result.putArray("branchSpecificsTaxResultAmountData");
		ArrayNode branchSpecificsTaxComponentPurchasean = result.putArray("branchSpecificsTaxComponentPurchaseData");
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start " + json);
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}

			long txnBranchId = json.findValue("txnBranchId") == null ? 0L : json.findValue("txnBranchId").asLong();
			long txnSpecificsId = json.findValue("txnSpecificsId").asLong();
			String txnGrossAmt = json.findValue("txnGrossAmt") == null ? "0" : json.findValue("txnGrossAmt").asText();
			long txnPurposeValue = json.findValue("txnPurposeValue").asLong();
			String txnSourceGstin = json.findValue("txnSourceGstinCode") == null ? ""
					: json.findValue("txnSourceGstinCode").asText();
			String txnDestGstin = json.findValue("txnDestGstinCode") == null ? ""
					: json.findValue("txnDestGstinCode").asText();
			Double noOfUnits = json.findValue("noOfUnits") == null ? 0.0 : json.findValue("noOfUnits").asDouble();
			String txnSelectedVendorCustomer = json.findValue("txnSelectedVendorCustomer") == null ? ""
					: json.findValue("txnSelectedVendorCustomer").asText();
			String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
			Date txnDate = IdosUtil.getFormatedDateWithTime(selectedTxnDate);

			txnDate = IdosConstants.MYSQLDTF.parse(IdosConstants.MYSQLDTF.format(txnDate));
			String txnSourceGstinCode = txnSourceGstin;
			String txnDestGstinCode = txnDestGstin;
			int txnSrcGstinStateCode = 0;
			int txnDstnGstinStateCode = 0;
			if (txnSourceGstinCode != null && !"".equals(txnSourceGstinCode) && txnSourceGstinCode.length() > 1) {
				txnSourceGstinCode = txnSourceGstinCode.substring(0, 2);
				txnSrcGstinStateCode = IdosUtil.convertStringToInt(txnSourceGstinCode);
			}
			if (txnDestGstinCode != null && !"null".equalsIgnoreCase(txnDestGstinCode) && !"".equals(txnDestGstinCode)
					&& txnDestGstinCode.length() > 1) {
				txnDestGstinCode = txnDestGstinCode.substring(0, 2);
				txnDstnGstinStateCode = IdosUtil.convertStringToInt(txnDestGstinCode);
			}

			Specifics specifics = Specifics.findById(txnSpecificsId);
			if (specifics == null) {
				result.put("status", "-1");
				result.put("statusMsg", "Item is not found to calculate tax");
				return Results.ok(result);
			}
			boolean isGstTaxApplicable = true;
			if ("1".equals(specifics.getGstItemCategory()) || "2".equals(specifics.getGstItemCategory())
					|| "3".equals(specifics.getGstItemCategory())) {
				isGstTaxApplicable = false;
				result.put("isGstTaxApplicable", "false");
			}
			if (IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER == txnPurposeValue
					&& txnSourceGstin != null && txnSourceGstin.equalsIgnoreCase(txnDestGstin)) {
				txnDstnGstinStateCode = 0;
				return Results.ok(result);
			}
			if (IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW == txnPurposeValue
					|| IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER == txnPurposeValue) {
				if (user.getOrganization().getIsCompositionScheme() != null
						&& user.getOrganization().getIsCompositionScheme() == 1) {
					return Results.ok(result);
				}
			}
			if (IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW == txnPurposeValue
					|| IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER == txnPurposeValue
					|| IdosConstants.PROFORMA_INVOICE == txnPurposeValue
					|| IdosConstants.SALES_RETURNS == txnPurposeValue
					|| IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER == txnPurposeValue
					|| IdosConstants.CREDIT_NOTE_CUSTOMER == txnPurposeValue
					|| IdosConstants.DEBIT_NOTE_CUSTOMER == txnPurposeValue
					|| IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER == txnPurposeValue) {
				int txnTypeOfSupply = json.findValue("txnTypeOfSupply") == null ? 0
						: json.findValue("txnTypeOfSupply").asInt();
				Integer txnWithWithoutTax = json.findValue("txnWithWithoutTax") == null ? null
						: json.findValue("txnWithWithoutTax").asInt();
				if (IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW == txnPurposeValue
						|| IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER == txnPurposeValue
						|| IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER == txnPurposeValue
						|| IdosConstants.CREDIT_NOTE_CUSTOMER == txnPurposeValue
						|| IdosConstants.DEBIT_NOTE_CUSTOMER == txnPurposeValue) {
					if (txnTypeOfSupply != 1 && txnTypeOfSupply != 3 && txnTypeOfSupply != 4 && txnTypeOfSupply != 5
							&& txnTypeOfSupply != 6) {
						return Results.ok(result);
					}
					if ((txnTypeOfSupply == 3 || txnTypeOfSupply == 4 || txnTypeOfSupply == 5)
							&& txnWithWithoutTax != null && txnWithWithoutTax == 2) {
						return Results.ok(result);
					}
				}
				DecimalFormat df = new DecimalFormat("###.00");
				if (specifics.getIsCombinationSales() != null && specifics.getIsCombinationSales() == 1) {
					Long coaId = specifics.getId();
					StringBuilder newsbquery = new StringBuilder(
							"select obj from SpecificsCombinationSales obj WHERE obj.specificsId.id = '" + coaId
									+ "' and obj.organization.id ='" + user.getOrganization().getId()
									+ "' and obj.presentStatus=1");
					List<SpecificsCombinationSales> specificsList = genericDAO.executeSimpleQuery(newsbquery.toString(),
							entityManager);
					ArrayList<Double> taxamt = new ArrayList<Double>();
					taxamt.add(0, 0.0); // SGST
					taxamt.add(1, 0.0);
					taxamt.add(2, 0.0);
					for (SpecificsCombinationSales combSpec : specificsList) {
						double openBalUnits = noOfUnits * combSpec.getOpeningBalUnits();
						double grossAmtForThisItem = openBalUnits * combSpec.getOpeningBalRate();
						criterias.clear();
						criterias.put("branch.id", txnBranchId);
						criterias.put("organization.id", user.getOrganization().getId());
						criterias.put("specifics.id", combSpec.getCombSpecificsId().getId());
						criterias.put("presentStatus", 1);
						List<BranchSpecificsTaxFormula> bnchSpecfTaxFormula = genericDAO
								.findByCriteria(BranchSpecificsTaxFormula.class, criterias, entityManager);
						Specifics specificChildItem = combSpec.getCombSpecificsId();
						ObjectNode row = Json.newObject();
						getNetAmountTaxComponentForCombinationSell(txnSourceGstinCode, txnDestGstinCode,
								txnTypeOfSupply, txnWithWithoutTax, specificChildItem, bnchSpecfTaxFormula,
								grossAmtForThisItem, row, taxamt);
					}
					// get branchTaxes to get taxId
					Long sgstTaxId = null, cgstTaxId = null, igstTaxId = null;
					StringBuilder strSQL = new StringBuilder("select obj from BranchTaxes obj where obj.organization=")
							.append(user.getOrganization().getId());
					strSQL.append(" and obj.branch=").append(txnBranchId);
					strSQL.append(" and obj.presentStatus=1 and obj.taxType in (20,21,22)");
					List<BranchTaxes> taxList = genericDAO.executeSimpleQuery(strSQL.toString(), entityManager);
					for (BranchTaxes bnchTax : taxList) {
						if (bnchTax.getTaxName().contains("SGST")) {
							sgstTaxId = bnchTax.getId();
						} else if (bnchTax.getTaxName().contains("CGST")) {
							cgstTaxId = bnchTax.getId();
						} else if (bnchTax.getTaxName().contains("IGST")) {
							igstTaxId = bnchTax.getId();
						}
					}

					double SGSTTaxAmt = taxamt.get(0) == null ? 0.0 : taxamt.get(0).doubleValue();
					if (SGSTTaxAmt > 0) {
						ObjectNode row = Json.newObject();
						row.put("taxName", "SGST"); // SGST added for all subitems of combination sales
						row.put("taxRate", "-");
						row.put("taxid", sgstTaxId);
						row.put("individualTax", "SGST(0.0%):" + IdosConstants.decimalFormat.format(SGSTTaxAmt));
						row.put("taxAmount", IdosConstants.decimalFormat.format(SGSTTaxAmt));
						row.put("taxFormulaId", "-1");
						branchSpecificsTaxComponentan.add(row);
						ObjectNode formularow = Json.newObject();
						formularow.put("individualTax", "SGST(0.0%):" + IdosConstants.decimalFormat.format(SGSTTaxAmt));
						branchSpecificsTaxFormulaComponentan.add(formularow);
					}
					double CGSTTaxAmt = taxamt.get(1) == null ? 0.0 : taxamt.get(1).doubleValue();
					if (CGSTTaxAmt > 0) {
						ObjectNode row = Json.newObject();
						row.put("taxName", "CGST");
						row.put("taxRate", "-");
						row.put("taxid", cgstTaxId);
						row.put("individualTax", "CGST(0.0%):" + IdosConstants.decimalFormat.format(CGSTTaxAmt));
						row.put("taxAmount", IdosConstants.decimalFormat.format(CGSTTaxAmt));
						row.put("taxFormulaId", "-1");
						branchSpecificsTaxComponentan.add(row);
						ObjectNode formularow = Json.newObject();
						formularow.put("individualTax", "CGST(0.0%):" + IdosConstants.decimalFormat.format(CGSTTaxAmt));
						branchSpecificsTaxFormulaComponentan.add(formularow);
					}
					double IGSTTaxAmt = taxamt.get(2) == null ? 0.0 : taxamt.get(2).doubleValue();
					if (IGSTTaxAmt > 0) {
						ObjectNode row = Json.newObject();
						row.put("taxName", "IGST");
						row.put("taxRate", "-");
						row.put("taxid", igstTaxId);
						row.put("individualTax", "IGST(0.0%):" + IdosConstants.decimalFormat.format(IGSTTaxAmt));
						row.put("taxAmount", IdosConstants.decimalFormat.format(IGSTTaxAmt));
						row.put("taxFormulaId", "-1");
						branchSpecificsTaxComponentan.add(row);
						ObjectNode formularow = Json.newObject();
						formularow.put("individualTax", "IGST(0.0%):" + IdosConstants.decimalFormat.format(IGSTTaxAmt));
						branchSpecificsTaxFormulaComponentan.add(formularow);
					}
					double totalTaxAmount = SGSTTaxAmt + CGSTTaxAmt + IGSTTaxAmt;
					ObjectNode rowResult = Json.newObject();
					rowResult.put("taxTotalAmount", IdosConstants.decimalFormat.format(totalTaxAmount));
					result.put("taxTotalAmount", IdosConstants.decimalFormat.format(totalTaxAmount));
					branchSpecificsTaxResultAmountan.add(rowResult);
				} else {
					List<Integer> taxTypeList = new ArrayList<Integer>(3);
					Double GV = IdosUtil.convertStringToDouble(txnGrossAmt);
					List<BranchSpecificsTaxFormula> bnchSpecfTaxFormula = null;
					if (specifics.getGstTaxRate() != null || DateUtil.isBackDate(txnDate)) {
						taxTypeList.add(new Integer(IdosConstants.OUTPUT_SGST));
						taxTypeList.add(new Integer(IdosConstants.OUTPUT_CGST));
						taxTypeList.add(new Integer(IdosConstants.OUTPUT_IGST));
					}

					if (!taxTypeList.isEmpty()) {
						if (log.isLoggable(Level.FINE))
							log.log(Level.FINE, "inparams " + taxTypeList);
						List<BranchSpecificsTaxFormula> gstTaxRateList = BranchSpecificsTaxFormula
								.findTaxOnSpecificDate(entityManager, user.getOrganization().getId(), txnBranchId,
										txnSpecificsId, txnDate, taxTypeList);
						if (gstTaxRateList != null) {
							if (gstTaxRateList.size() > 3) {
								bnchSpecfTaxFormula = new ArrayList<BranchSpecificsTaxFormula>(3);
								bnchSpecfTaxFormula.add(gstTaxRateList.get(0));
								bnchSpecfTaxFormula.add(gstTaxRateList.get(1));
								bnchSpecfTaxFormula.add(gstTaxRateList.get(2));
							} else {
								bnchSpecfTaxFormula = gstTaxRateList;
							}
						}
					}
					taxTypeList.clear();
					if (specifics.getCessTaxRate() != null || DateUtil.isBackDate(txnDate)) {
						taxTypeList.add((int) IdosConstants.OUTPUT_CESS);
					}
					if (!taxTypeList.isEmpty()) {
						List<BranchSpecificsTaxFormula> cessTaxRateList = BranchSpecificsTaxFormula
								.findTaxOnSpecificDate(entityManager, user.getOrganization().getId(), txnBranchId,
										txnSpecificsId, txnDate, taxTypeList);
						if (cessTaxRateList != null && cessTaxRateList.size() > 0) {
							if (bnchSpecfTaxFormula == null) {
								bnchSpecfTaxFormula = cessTaxRateList;
							} else {
								bnchSpecfTaxFormula.add(cessTaxRateList.get(0));
							}
						}
					}

					Double Tax1 = 0.0;
					Double IV1 = 0.0;
					Double Tax2 = 0.0;
					Double IV2 = 0.0;
					Double Tax3 = 0.0;
					Double IV3 = 0.0;
					Double Tax4 = 0.0;
					Double IV4 = 0.0;
					Double Tax5 = 0.0;
					Double IV5 = 0.0;
					Double taxTotalAmount = 0.0;
					boolean isCessCalculated = false;
					boolean isSgstCalculated = false;
					boolean isCgstCalculated = false;
					boolean isIgstCalculated = false;
					if (bnchSpecfTaxFormula != null) {
						for (int i = 0; i < bnchSpecfTaxFormula.size(); i++) {
							int taxType = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxType();
							if (taxType == IdosConstants.OUTPUT_TAX || taxType == IdosConstants.OUTPUT_SGST
									|| taxType == IdosConstants.OUTPUT_CGST || taxType == IdosConstants.OUTPUT_IGST
									|| taxType == IdosConstants.OUTPUT_CESS) {
								ObjectNode row = Json.newObject();
								ObjectNode formularow = Json.newObject();
								String taxName = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxName();
								if (taxName != null) {
									if ((!isGstTaxApplicable)
											&& (taxName.startsWith("SGST") || taxName.startsWith("CGST")
													|| taxName.startsWith("IGST") || taxName.startsWith("CESS"))) {
										continue;
									}
									if (isGstTaxApplicable && txnWithWithoutTax != null && txnWithWithoutTax == 1
											&& (taxName.startsWith("SGST") || taxName.startsWith("CGST"))) {
										continue;
									}
									if (txnTypeOfSupply != 3) {
										if (((txnSrcGstinStateCode == 0 || txnDstnGstinStateCode == 0)
												&& ((taxName.startsWith("SGST") || taxName.startsWith("CGST"))))
												|| (txnSrcGstinStateCode != txnDstnGstinStateCode
														&& ((taxName.startsWith("SGST")
																|| taxName.startsWith("CGST"))))) {
											continue;
										}
										if ((txnSrcGstinStateCode == 0 || txnDstnGstinStateCode == 0)
												&& taxName.startsWith("IGST")
												|| (txnSrcGstinStateCode == txnDstnGstinStateCode
														&& (taxName.startsWith("IGST") && txnWithWithoutTax != null
																&& txnWithWithoutTax != 1))) {
											continue;
										}
									}
								}
								if (taxName.startsWith("CESS") && isCessCalculated) {
									continue;
								}
								if (taxName.startsWith("SGST") && isSgstCalculated) {
									continue;
								}
								if (taxName.startsWith("CGST") && isCgstCalculated) {
									continue;
								}
								if (taxName.startsWith("IGST") && isIgstCalculated) {
									continue;
								}

								Double taxRate = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxRate();
								if (taxName != null && taxName.length() > 4) {
									row.put("taxName", taxName.substring(0, 4));
									if (taxName.startsWith("CESS")) {
										isCessCalculated = true;
									} else if (taxName.startsWith("SGST")) {
										isSgstCalculated = true;
									} else if (taxName.startsWith("CGST")) {
										isCgstCalculated = true;
									} else if (taxName.startsWith("IGST")) {
										isIgstCalculated = true;
									}
								} else {
									row.put("taxName", taxName);
								}
								if (taxRate != null) {
									row.put("taxRate", taxRate);
								} else {
									row.put("taxRate", 0.0);
								}
								row.put("taxid", bnchSpecfTaxFormula.get(i).getBranchTaxes().getId());
								row.put("taxFormulaId", bnchSpecfTaxFormula.get(i).getId());
								formularow.put("individualTaxFormula", bnchSpecfTaxFormula.get(i).getFormula());
								String appliedOn = bnchSpecfTaxFormula.get(i).getAppliedTo();
								// Tax 1 = GV*(Rate/100) & IV 1 = GV + Tax 1
								if (i == 0) {
									if (appliedOn.equals("GV")) {
										Tax1 = GV * (taxRate / (100.0));
										if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 1) {
											IV1 = GV + Tax1;
											row.put("individualTax",
													taxName + "(+" + taxRate + "%):" + df.format(Tax1));
										} else if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 0) {
											IV1 = GV - Tax1;
											row.put("individualTax",
													taxName + "(-" + taxRate + "%):" + df.format(Tax1));
										}
										taxTotalAmount += Tax1;

									}
									row.put("taxAmount", df.format(Tax1));
								}
								/*
								 * Based on selection If GV is selected: Tax 2 = GV*(Rate/100) If Tax 1 is
								 * selected: Tax 2 = Tax 1*(Rate/100) If IV 1 is selected Tax 2 = IV 1(
								 * Rate/100) IV 2 = GV + Tax 1 + Tax 2
								 */
								else if (i == 1) {
									if (appliedOn.equals("GV")) {
										Tax2 = GV * (taxRate / (100.0));
									} else if (appliedOn.equals("Tax1")) {
										Tax2 = Tax1 * (taxRate / (100.0));
									} else if (appliedOn.equals("IV1")) {
										Tax2 = IV1 * (taxRate / (100.0));
									}

									if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 1) {
										IV2 = GV + (Tax1 + Tax2);
										row.put("individualTax", taxName + "(+" + taxRate + "%):" + df.format(Tax2));
									} else if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 0) {
										IV2 = GV - (Tax1 + Tax2);
										row.put("individualTax", taxName + "(-" + taxRate + "%):" + df.format(Tax2));
									}
									taxTotalAmount += Tax2;
									row.put("taxAmount", df.format(Tax2));
								}
								/*
								 * Based on selection If GV is selected: Tax 3 = GV*(Rate/100) If Tax 1 is
								 * selected: Tax 3 = Tax 1*(Rate/100) If Tax 2 is selected: Tax 3 = Tax
								 * 2*(Rate/100) if IV 2 is selected Tax 3 = IV 2*(Rate/100) IV 3 = GV + Tax 1 +
								 * Tax 2 + Tax 3
								 */
								else if (i == 2) {
									if (appliedOn.equals("GV")) {
										Tax3 = GV * (taxRate / (100.0));
									} else if (appliedOn.equals("Tax1")) {
										Tax3 = Tax1 * (taxRate / (100.0));
									} else if (appliedOn.equals("Tax2")) {
										Tax3 = Tax2 * (taxRate / (100.0));
									} else if (appliedOn.equals("IV2")) {
										Tax3 = IV2 * (taxRate / (100.0));
									}

									if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 1) {
										IV3 = GV + (Tax1 + Tax2 + Tax3);
										row.put("individualTax", taxName + "(+" + taxRate + "%):" + df.format(Tax3));
									} else if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 0) {
										IV3 = GV - (Tax1 + Tax2 + Tax3);
										row.put("individualTax", taxName + "(-" + taxRate + "%):" + df.format(Tax3));
									}
									taxTotalAmount += Tax3;
									row.put("taxAmount", df.format(Tax3));
								} else if (i == 3) {
									if (appliedOn.equals("GV")) {
										Tax4 = GV * (taxRate / (100.0));
									} else if (appliedOn.equals("Tax1")) {
										Tax4 = Tax1 * (taxRate / (100.0));
									} else if (appliedOn.equals("Tax2")) {
										Tax4 = Tax2 * (taxRate / (100.0));
									} else if (appliedOn.equals("Tax3")) {
										Tax4 = Tax3 * (taxRate / (100.0));
									} else if (appliedOn.equals("IV3")) {
										Tax4 = IV3 * (taxRate / (100.0));
									}
									if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 1) {
										IV4 = GV + (Tax1 + Tax2 + Tax3 + Tax4);
										row.put("individualTax", taxName + "(+" + taxRate + "%):" + df.format(Tax4));
									} else if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 0) {
										IV4 = GV - (Tax1 + Tax2 + Tax3 + Tax4);
										row.put("individualTax", taxName + "(-" + taxRate + "%):" + df.format(Tax4));
									}
									taxTotalAmount += Tax4;
									row.put("taxAmount", df.format(Tax4));
									row.put("netTaxAmount", Tax4);
								} else if (i == 4) {
									if (appliedOn.equals("GV")) {
										Tax5 = GV * (taxRate / (100.0));
									} else if (appliedOn.equals("Tax1")) {
										Tax5 = Tax1 * (taxRate / (100.0));
									} else if (appliedOn.equals("Tax2")) {
										Tax5 = Tax2 * (taxRate / (100.0));
									} else if (appliedOn.equals("Tax3")) {
										Tax5 = Tax3 * (taxRate / (100.0));
									} else if (appliedOn.equals("Tax4")) {
										Tax5 = Tax4 * (taxRate / (100.0));
									} else if (appliedOn.equals("IV2")) {
										Tax5 = IV2 * (taxRate / (100.0));
									}
									if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 1) {
										IV5 = GV + (Tax1 + Tax2 + Tax3 + Tax4 + Tax5);
										row.put("individualTax", taxName + "(+" + taxRate + "%):" + df.format(Tax5));
									} else if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 0) {
										IV5 = GV - (Tax1 + Tax2 + Tax3 + Tax4 + Tax5);
										row.put("individualTax", taxName + "(-" + taxRate + "%):" + df.format(Tax5));
									}
									taxTotalAmount += Tax5;
									row.put("taxAmount", df.format(Tax5));
								}

								if (i < 5) {
									branchSpecificsTaxComponentan.add(row);
									branchSpecificsTaxFormulaComponentan.add(formularow);
								}
							}
						}
					}
					ObjectNode row = Json.newObject();
					row.put("taxTotalAmount", taxTotalAmount);
					branchSpecificsTaxResultAmountan.add(row);
				}
			} else if (txnPurposeValue == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
					|| txnPurposeValue == IdosConstants.BUY_ON_CREDIT_PAY_LATER
					|| txnPurposeValue == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT
					|| txnPurposeValue == IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER
					|| IdosConstants.PURCHASE_RETURNS == txnPurposeValue) {
				String txnAdjustmentAmount = json.findValue("txnAdjustmentAmount") == null ? null
						: json.findValue("txnAdjustmentAmount").asText();
				String txnTotalInputTaxes = json.findValue("txnTotalInputTaxes") == null ? null
						: json.findValue("txnTotalInputTaxes").asText();

				Double adjustmentAmount = 0.0;
				Double grossAmount = IdosUtil.convertStringToDouble(txnGrossAmt);
				Double newTxnNetAmount = IdosUtil.convertStringToDouble(txnGrossAmt);
				if (txnAdjustmentAmount != null && !"".equals(txnAdjustmentAmount)) {
					adjustmentAmount = IdosUtil.convertStringToDouble(txnAdjustmentAmount);
					newTxnNetAmount = newTxnNetAmount - adjustmentAmount;
				}
				Double totalInputTaxes = 0.0;
				if (txnTotalInputTaxes != null && !"".equals(txnTotalInputTaxes)) {
					totalInputTaxes = IdosUtil.convertStringToDouble(txnTotalInputTaxes);
					newTxnNetAmount = newTxnNetAmount + totalInputTaxes;
				}

				criterias.clear();
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("id", txnSpecificsId);
				criterias.put("presentStatus", 1);
				Specifics specf = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
				Vendor vendor = null;
				if (txnSelectedVendorCustomer != null && !"".equals(txnSelectedVendorCustomer)) {
					vendor = Vendor.findById(Long.valueOf(txnSelectedVendorCustomer));
				}
				if (specf != null && vendor != null) {
					VENDOR_TDS_SERVICE.calculateTds(result, branchSpecificsTaxComponentPurchasean, user, specf, vendor,
							grossAmount, newTxnNetAmount, txnPurposeValue,
							IdosUtil.getFormatedDateWithTime(selectedTxnDate), entityManager);
				} else {
					log.log(Level.SEVERE, "Specific details not found for id " + txnSpecificsId);
				}
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result barcodeItemFetch(Request request) {
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		ArrayNode itemBarcodeId = result.putArray("itemBarcodeId");
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			// Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			String itemBarcode = json.findValue("barcode") == null ? "" : json.findValue("barcode").asText();
			String txnType = json.findValue("txnType") == null ? "" : json.findValue("txnType").asText();
			// StringBuilder sb = new StringBuilder();
			String sb = "";
			if (txnType.startsWith("soc")) {
				// sb.append("select obj.id from Specifics obj where obj.barcode='" +
				// itemBarcode + "'and obj.accountCodeHirarchy like '/10%' ");
				sb = "select obj.id from Specifics obj where obj.barcode= ?1 and obj.accountCodeHirarchy like '/10%' and obj.presentStatus=1 ";
			}
			if (txnType.startsWith("boc")) {
				// sb.append("select obj.id from Specifics obj where obj.barcode='" +
				// itemBarcode + "'and obj.accountCodeHirarchy like '/20%' ");
				sb = "select obj.id from Specifics obj where obj.barcode= ?1 and obj.accountCodeHirarchy like '/20%' and obj.presentStatus=1 ";
			}
			ArrayList inparam = new ArrayList(1);
			inparam.add(itemBarcode);
			List<Specifics> barcodeItemId = genericDAO.queryWithParams(sb, entityManager, inparam);
			// List<Specifics> barcodeItemId= genericDAO.executeSimpleQuery(sb.toString(),
			// entityManager);
			ObjectNode row = Json.newObject();
			if (barcodeItemId.size() > 0) {
				Object val = barcodeItemId.get(0);
				if (val != null) {
					row.put("barcodeItemId", String.valueOf(val));
				} else {
					row.put("barcodeItemId", "");
				}
			} else {
				row.put("barcodeItemId", "");
			}
			itemBarcodeId.add(row);

		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		return Results.ok(result);
	}

	@Transactional
	public Result barcodeFetch(Request request) {
		log.log(Level.FINE, "inside barcodeFetch");
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode itemIdan = result.putArray("itemId");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			long itemId = json.findValue("itemId") == null ? 0L : json.findValue("itemId").asLong();
			// StringBuilder sb = new StringBuilder();
			// sb.append("select obj.barcode from Specifics obj where obj.id='" + itemId +
			// "'");
			// List<Specifics> itemBarcode= genericDAO.executeSimpleQuery(sb.toString(),
			// entityManager);
			String sb = "select obj.barcode from Specifics obj where obj.id=?1 and obj.presentStatus=1";
			ArrayList inparam = new ArrayList(1);
			inparam.add(itemId);
			List<Specifics> itemBarcode = genericDAO.queryWithParams(sb, entityManager, inparam);
			ObjectNode row = Json.newObject();
			if (itemBarcode.size() > 0) {
				Object val = itemBarcode.get(0);
				if (val != null) {
					row.put("itemBarcode", String.valueOf(val));
				} else {
					row.put("itemBarcode", "");
				}
			} else {
				row.put("itemBarcode", "");
			}
			itemIdan.add(row);

		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result showTranCombSalesItemsWithTaxes(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			ArrayNode combSalesList = result.putArray("combSalesListData");
			String txnBranchId = json.findValue("txnBranchId") == null ? "" : json.findValue("txnBranchId").asText();
			Double noOfUnits = json.findValue("noOfUnits") == null ? 0.0 : json.findValue("noOfUnits").asDouble();
			Long coaId = json.findValue("txnSpecificsId") != null ? json.findValue("txnSpecificsId").asLong() : 0l;
			String txnSourceGstinCode = json.findValue("txnSourceGstinCode") == null ? ""
					: json.findValue("txnSourceGstinCode").asText();
			String txnDestGstinCode = json.findValue("txnDestGstinCode") == null ? ""
					: json.findValue("txnDestGstinCode").asText();
			int txnTypeOfSupply = json.findValue("txnTypeOfSupply") == null ? 0
					: json.findValue("txnTypeOfSupply").asInt();
			Integer txnWithWithoutTax = json.findValue("txnWithWithoutTax") == null ? null
					: json.findValue("txnWithWithoutTax").asInt();
			Specifics incomeSpecifics = Specifics.findById(coaId);
			user = getUserInfo(request);
			if (incomeSpecifics != null) {
				String newsbquery = ("select obj from SpecificsCombinationSales obj WHERE obj.organization.id =?1 and obj.specificsId.id = ?2 and obj.presentStatus=1");
				ArrayList inparam = new ArrayList(2);
				inparam.add(user.getOrganization().getId());
				inparam.add(coaId);
				List<SpecificsCombinationSales> specificsList = genericDAO.queryWithParams(newsbquery, entityManager,
						inparam);
				ArrayList<Double> taxamt = new ArrayList<Double>();
				taxamt.add(0, 0.0); // SGST
				taxamt.add(1, 0.0);
				taxamt.add(2, 0.0);
				for (SpecificsCombinationSales combSpec : specificsList) {
					ObjectNode row = Json.newObject();
					row.put("specificsId", combSpec.getCombSpecificsId().getId());
					row.put("itemName", combSpec.getCombSpecificsId().getName());
					double openBalUnits = noOfUnits * combSpec.getOpeningBalUnits();
					double grossAmtForThisItem = openBalUnits * combSpec.getOpeningBalRate();
					if (combSpec.getOpeningBalUnits() != null) {
						row.put("openBalUnits", openBalUnits);
					} else {
						row.put("openBalUnits", 0);
					}
					if (combSpec.getOpeningBalUnits() != null) {
						row.put("openingBalRate", combSpec.getOpeningBalRate());
					} else {
						row.put("openingBalRate", 0);
					}
					row.put("grossAmt", grossAmtForThisItem);
					Specifics specificChildItem = combSpec.getCombSpecificsId();

					Map<String, Object> criterias = new HashMap<String, Object>();
					criterias.put("branch.id", IdosUtil.convertStringToLong(txnBranchId));
					criterias.put("organization.id", user.getOrganization().getId());
					criterias.put("specifics.id", combSpec.getCombSpecificsId().getId());
					criterias.put("presentStatus", 1);

					List<BranchSpecificsTaxFormula> bnchSpecfTaxFormula = genericDAO
							.findByCriteria(BranchSpecificsTaxFormula.class, criterias, entityManager);
					getNetAmountTaxComponentForCombinationSell(txnSourceGstinCode, txnDestGstinCode, txnTypeOfSupply,
							txnWithWithoutTax, specificChildItem, bnchSpecfTaxFormula, grossAmtForThisItem, row,
							taxamt);
					combSalesList.add(row);
				}
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	public static void getNetAmountTaxComponentForCombinationSell(String txnSourceGstinCode, String txnDestGstinCode,
			int txnTypeOfSupply, Integer txnWithWithoutTax, Specifics specificChildItem,
			List<BranchSpecificsTaxFormula> bnchSpecfTaxFormula, Double GV, ObjectNode row, ArrayList<Double> taxAmt)
			throws IDOSException {
		int txnSrcGstinStateCode = 0;
		int txnDstnGstinStateCode = 0;
		if (txnSourceGstinCode != null && !"".equals(txnSourceGstinCode) && txnSourceGstinCode.length() > 1) {
			txnSourceGstinCode = txnSourceGstinCode.substring(0, 2);
			txnSrcGstinStateCode = IdosUtil.convertStringToInt(txnSourceGstinCode);
		}
		if (txnDestGstinCode != null && !"".equals(txnDestGstinCode) && txnDestGstinCode.length() > 1) {
			txnDestGstinCode = txnDestGstinCode.substring(0, 2);
			txnDstnGstinStateCode = IdosUtil.convertStringToInt(txnDestGstinCode);
		}
		String gstItemCate = specificChildItem.getGstItemCategory();
		boolean isGstTaxApplicable = true;
		if (gstItemCate != null && ("1".equals(gstItemCate) || "2".equals(gstItemCate) || "3".equals(gstItemCate))) {
			isGstTaxApplicable = false;
		}
		DecimalFormat df = new DecimalFormat("###.##");
		Double Tax1 = 0.0;
		Double IV1 = 0.0;
		Double Tax2 = 0.0;
		Double IV2 = 0.0;
		Double Tax3 = 0.0;
		Double IV3 = 0.0;
		Double Tax4 = 0.0;
		Double IV4 = 0.0;
		Double Tax5 = 0.0;
		Double IV5 = 0.0;
		Double taxTotalAmount = 0.0;
		Double SGSTTaxAmt = taxAmt.get(0);
		Double CGSTTaxAmt = taxAmt.get(1);
		Double IGSTTaxAmt = taxAmt.get(2);
		for (int i = 0; i < bnchSpecfTaxFormula.size(); i++) {
			int taxType = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxType();
			if (taxType == IdosConstants.OUTPUT_TAX || taxType == IdosConstants.OUTPUT_SGST
					|| taxType == IdosConstants.OUTPUT_CGST || taxType == IdosConstants.OUTPUT_IGST
					|| taxType == IdosConstants.OUTPUT_CESS) {

				String taxName = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxName();
				if (taxName != null) {
					if ((!isGstTaxApplicable) && (taxName.startsWith("SGST") || taxName.startsWith("CGST")
							|| taxName.startsWith("IGST"))) {
						continue;
					}
					if (isGstTaxApplicable && txnWithWithoutTax != null && txnWithWithoutTax == 1
							&& (taxName.startsWith("SGST") || taxName.startsWith("CGST"))) {
						continue;
					}
					if (txnTypeOfSupply != 3) {
						if (((txnSrcGstinStateCode == 0 || txnDstnGstinStateCode == 0)
								&& ((taxName.startsWith("SGST") || taxName.startsWith("CGST"))))
								|| (txnSrcGstinStateCode != txnDstnGstinStateCode
										&& ((taxName.startsWith("SGST") || taxName.startsWith("CGST"))))) {
							continue;
						}
						if ((txnSrcGstinStateCode == 0 || txnDstnGstinStateCode == 0) && taxName.startsWith("IGST")
								|| (txnSrcGstinStateCode == txnDstnGstinStateCode && (taxName.startsWith("IGST")
										&& txnWithWithoutTax != null && txnWithWithoutTax != 1))) {
							continue;
						}
					}
				}
				Double taxRate = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxRate();
				if (taxName != null && taxName.length() > 4) {
					row.put("taxName" + i, taxName.substring(0, 4));
				} else {
					row.put("taxName" + i, taxName);
				}
				if (taxRate != null) {
					row.put("taxRate" + i, taxRate);
				} else {
					row.put("taxRate" + i, 0.0);
				}
				String appliedOn = bnchSpecfTaxFormula.get(i).getAppliedTo();
				// Tax 1 = GV*(Rate/100) & IV 1 = GV + Tax 1
				if (i == 0) {
					if (appliedOn.equals("GV")) {
						Tax1 = GV * (taxRate / (100.0));
						if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 1) {
							IV1 = GV + Tax1;
							row.put("individualTax" + i, taxName + "(+" + taxRate + "%):" + df.format(Tax1));
						} else if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 0) {
							IV1 = GV - Tax1;
							row.put("individualTax" + i, taxName + "(-" + taxRate + "%):" + df.format(Tax1));
						}
						taxTotalAmount += Tax1;
					}
					row.put("taxAmount" + i, df.format(Tax1));
					if (taxName.startsWith("SGST")) {
						SGSTTaxAmt += Tax1;
					} else if (taxName.startsWith("CGST")) {
						CGSTTaxAmt += Tax1;
					} else if (taxName.startsWith("IGST")) {
						IGSTTaxAmt += Tax1;
					}
				}
				/*
				 * Based on selection If GV is selected: Tax 2 = GV*(Rate/100) If Tax 1 is
				 * selected: Tax 2 = Tax 1*(Rate/100) If IV 1 is selected Tax 2 = IV 1(
				 * Rate/100) IV 2 = GV + Tax 1 + Tax 2
				 */
				else if (i == 1) {
					if (appliedOn.equals("GV")) {
						Tax2 = GV * (taxRate / (100.0));
					} else if (appliedOn.equals("Tax1")) {
						Tax2 = Tax1 * (taxRate / (100.0));
					} else if (appliedOn.equals("IV1")) {
						Tax2 = IV1 * (taxRate / (100.0));
					}

					if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 1) {
						IV2 = GV + (Tax1 + Tax2);
						row.put("individualTax" + i, taxName + "(+" + taxRate + "%):" + df.format(Tax2));
					} else if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 0) {
						IV2 = GV - (Tax1 + Tax2);
						row.put("individualTax" + i, taxName + "(-" + taxRate + "%):" + df.format(Tax2));
					}
					taxTotalAmount += Tax2;
					row.put("taxAmount" + i, df.format(Tax2));
					if (taxName.startsWith("SGST")) {
						SGSTTaxAmt += Tax2;
					} else if (taxName.startsWith("CGST")) {
						CGSTTaxAmt += Tax2;
					} else if (taxName.startsWith("IGST")) {
						IGSTTaxAmt += Tax2;
					}
				}
				/*
				 * Based on selection If GV is selected: Tax 3 = GV*(Rate/100) If Tax 1 is
				 * selected: Tax 3 = Tax 1*(Rate/100) If Tax 2 is selected: Tax 3 = Tax
				 * 2*(Rate/100) if IV 2 is selected Tax 3 = IV 2*(Rate/100) IV 3 = GV + Tax 1 +
				 * Tax 2 + Tax 3
				 */
				else if (i == 2) {
					if (appliedOn.equals("GV")) {
						Tax3 = GV * (taxRate / (100.0));
					} else if (appliedOn.equals("Tax1")) {
						Tax3 = Tax1 * (taxRate / (100.0));
					} else if (appliedOn.equals("Tax2")) {
						Tax3 = Tax2 * (taxRate / (100.0));
					} else if (appliedOn.equals("IV2")) {
						Tax3 = IV2 * (taxRate / (100.0));
					}

					if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 1) {
						IV3 = GV + (Tax1 + Tax2 + Tax3);
						row.put("individualTax" + i, taxName + "(+" + taxRate + "%):" + df.format(Tax3));
					} else if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 0) {
						IV3 = GV - (Tax1 + Tax2 + Tax3);
						row.put("individualTax" + i, taxName + "(-" + taxRate + "%):" + df.format(Tax3));
					}
					taxTotalAmount += Tax3;
					row.put("taxAmount" + i, df.format(Tax3));
					if (taxName.startsWith("SGST")) {
						SGSTTaxAmt += Tax3;
					} else if (taxName.startsWith("CGST")) {
						CGSTTaxAmt += Tax3;
					} else if (taxName.startsWith("IGST")) {
						IGSTTaxAmt += Tax3;
					}
				} else if (i == 3) {
					if (appliedOn.equals("GV")) {
						Tax4 = GV * (taxRate / (100.0));
					} else if (appliedOn.equals("Tax1")) {
						Tax4 = Tax1 * (taxRate / (100.0));
					} else if (appliedOn.equals("Tax2")) {
						Tax4 = Tax2 * (taxRate / (100.0));
					} else if (appliedOn.equals("Tax3")) {
						Tax4 = Tax3 * (taxRate / (100.0));
					} else if (appliedOn.equals("IV3")) {
						Tax4 = IV3 * (taxRate / (100.0));
					}

					if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 1) {
						IV4 = GV + (Tax1 + Tax2 + Tax3 + Tax4);
						row.put("individualTax" + i, taxName + "(+" + taxRate + "%):" + df.format(Tax4));
					} else if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 0) {
						IV4 = GV - (Tax1 + Tax2 + Tax3 + Tax4);
						row.put("individualTax" + i, taxName + "(-" + taxRate + "%):" + df.format(Tax4));
					}
					taxTotalAmount += Tax4;
					row.put("taxAmount" + i, df.format(Tax4));
					if (taxName.startsWith("SGST")) {
						SGSTTaxAmt += Tax4;
					} else if (taxName.startsWith("CGST")) {
						CGSTTaxAmt += Tax4;
					} else if (taxName.startsWith("IGST")) {
						IGSTTaxAmt += Tax4;
					}
				} else if (i == 4) {
					if (appliedOn.equals("GV")) {
						Tax5 = GV * (taxRate / (100.0));
					} else if (appliedOn.equals("Tax1")) {
						Tax5 = Tax1 * (taxRate / (100.0));
					} else if (appliedOn.equals("Tax2")) {
						Tax5 = Tax2 * (taxRate / (100.0));
					} else if (appliedOn.equals("Tax3")) {
						Tax5 = Tax3 * (taxRate / (100.0));
					} else if (appliedOn.equals("Tax4")) {
						Tax5 = Tax4 * (taxRate / (100.0));
					} else if (appliedOn.equals("IV2")) {
						Tax5 = IV2 * (taxRate / (100.0));
					}
					if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 1) {
						IV5 = GV + (Tax1 + Tax2 + Tax3 + Tax4 + Tax5);
						row.put("individualTax" + i, taxName + "(+" + taxRate + "%):" + df.format(Tax5));
					} else if (bnchSpecfTaxFormula.get(i).getAddDeduct() == 0) {
						IV5 = GV - (Tax1 + Tax2 + Tax3 + Tax4 + Tax5);
						row.put("individualTax" + i, taxName + "(-" + taxRate + "%):" + df.format(Tax5));
					}
					taxTotalAmount += Tax5;
					row.put("taxAmount" + i, df.format(Tax5));
					if (taxName.startsWith("SGST")) {
						SGSTTaxAmt += Tax5;
					} else if (taxName.startsWith("CGST")) {
						CGSTTaxAmt += Tax5;
					} else if (taxName.startsWith("IGST")) {
						IGSTTaxAmt += Tax5;
					}
				}
			}
		}
		taxAmt.set(0, SGSTTaxAmt);
		taxAmt.set(1, CGSTTaxAmt);
		taxAmt.set(2, IGSTTaxAmt);
		row.put("taxTotalAmount", taxTotalAmount);
		row.put("totalAmount", taxTotalAmount + GV);
	}

	@Transactional
	public Result vendorSupplierWithholdingData(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			ArrayNode branchSpecificsTaxComponentPurchasean = result
					.putArray("branchSpecificsTaxComponentPurchaseData");
			String email = json.findValue("useremail").asText();
			session.adding("email", email);
			user = getUserInfo(request);
			Long txnSelectedVendorCustomer = json.findValue("txnVendorId").asLong();
			String pendingTxnEntityId = json.findValue("pendingTxnEntityId").asText();
			Transaction orgTxn = Transaction.findById(IdosUtil.convertStringToLong(pendingTxnEntityId));
			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			int nextYear = currentYear + 1;
			String finStartDate = "01-04-" + currentYear + "";
			String finEndDate = "01-03-" + nextYear + "";
			String finStDt = mysqldf.format(reportdf.parse(finStartDate));
			String finEndDt = mysqldf.format(reportdf.parse(finEndDate));
			String cssbquery = ("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization='"
					+ user.getOrganization().getId() + "' AND obj.transactionSpecifics='"
					+ orgTxn.getTransactionSpecifics().getId() + "' AND obj.transactionVendorCustomer.id='"
					+ txnSelectedVendorCustomer
					+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=4) and obj.presentStatus=1 and obj.transactionDate  between '"
					+ finStDt + "' and '" + finEndDt + "' and obj.transactionInvoiceDate between '" + finStDt
					+ "' and '" + finEndDt + "'");

			List<Transaction> totalVendNetForFinYearForWithholding = genericDAO.executeSimpleQuery(cssbquery,
					entityManager);
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("id", orgTxn.getTransactionSpecifics().getId());
			criterias.put("presentStatus", 1);
			Specifics specf = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
			if (specf != null) {
				if (specf.getIsWithholdingApplicable() != null) {
					if (specf.getIsWithholdingApplicable() == 1) {
						int isWithholdingApplicableOrNot = specf.getIsWithholdingApplicable();
						Double withgholdingrate = null;
						Double grossAmount = orgTxn.getNetAmount();
						if (specf.getWithHoldingRate() != null) {
							withgholdingrate = specf.getWithHoldingRate();
						}
						Double withHoldingLimit = null;
						if (specf.getWithHoldingLimit() != null) {
							withHoldingLimit = specf.getWithHoldingLimit();
						}
						Double withHoldingMonetoryLimit = null;
						if (specf.getWithholdingMonetoryLimit() != null) {
							withHoldingMonetoryLimit = specf.getWithholdingMonetoryLimit();
						}
						if (isWithholdingApplicableOrNot == 1 && withgholdingrate != null && withHoldingLimit != null) {
							if (grossAmount >= withHoldingLimit) {
								Double withHoldingTaxAmount = (withgholdingrate / 100.0) * (grossAmount);
								ObjectNode row = Json.newObject();
								if (totalVendNetForFinYearForWithholding.size() > 0) {
									Object val = totalVendNetForFinYearForWithholding.get(0);
									if (val != null) {
										row.put("totalVendNetForFinYearForWithholding", String.valueOf(val));
									} else {
										row.put("totalVendNetForFinYearForWithholding", "");
									}
								} else {
									row.put("totalVendNetForFinYearForWithholding", "");
								}
								row.put("withholdingtaxRate", withgholdingrate + "(%)");
								if (withHoldingLimit != null) {
									row.put("withholdingtaxLimit", withHoldingLimit);
								} else {
									row.put("withholdingtaxLimit", "");
								}
								if (withHoldingMonetoryLimit != null) {
									row.put("withHoldingMonetoryLimit", withHoldingMonetoryLimit);
								} else {
									row.put("withHoldingMonetoryLimit", "");
								}
								row.put("withholdingtaxTotalAmount", withHoldingTaxAmount + "(-)");
								branchSpecificsTaxComponentPurchasean.add(row);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result approverActions(Request request) {
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		Users user = getUserInfo(request);
		if (user == null) {
			return unauthorized();
		}
		try {
			Boolean isSingleUserDeploy = ConfigParams.getInstance().isDeploymentSingleUser(user);
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start " + json);
			String txnReferenceNo = json.findValue("txnReferenceNo") != null ? json.findValue("txnReferenceNo").asText()
					: null;
			results.put("useremail", user.getEmail());
			String userRolesAsString = "";
			List<UsersRoles> userRolesList = UsersRoles.getUserRoleList(entityManager, user.getOrganization().getId(),
					user.getId(),
					user.getBranch().getId());
			for (UsersRoles userRole : userRolesList) {
				userRolesAsString += userRole.getRole().getName() + ",";
			}
			userRolesAsString = userRolesAsString.substring(0, userRolesAsString.length() - 1);
			results.put("role", userRolesAsString);
			if (txnReferenceNo != null && txnReferenceNo.startsWith(IdosConstants.BOM_TXN_TYPE)) {
				entityTransaction.begin();
				BillOfMaterialTxnModel txn = BILL_OF_MATERIAL_TXN_SERVICE.approverAction(user, entityManager, json,
						results);
				entityTransaction.commit();
				if (txn != null) {
					results.put(TRANSACTION_ID, txn.getId());
					results.put(TRANSACTION_REF_NO, txn.getTransactionRefNumber());
				}
				log.log(Level.FINE, ">>>> End " + results);
				return Results.ok(results);
			}

			if (txnReferenceNo != null && txnReferenceNo.startsWith(IdosConstants.PR_TXN_TYPE)) {
				entityTransaction.begin();
				PurchaseRequisitionTxnModel txn = PURCHASE_REQUISITION_TXN_SERVICE.approverAction(user, entityManager,
						json, results);
				entityTransaction.commit();
				if (txn != null) {
					results.put(TRANSACTION_ID, txn.getId());
					results.put(TRANSACTION_REF_NO, txn.getTransactionRefNumber());
				}
				log.log(Level.FINE, ">>>> End " + results);
				return ok(results);
			}

			if (txnReferenceNo != null && txnReferenceNo.startsWith(IdosConstants.PO_TXN_TYPE)) {
				entityTransaction.begin();
				PurchaseOrderTxnModel txn = purchaseOrderService.approverAction(user, entityManager, json, results);
				entityTransaction.commit();
				if (txn != null) {
					results.put(TRANSACTION_ID, txn.getId());
					results.put(TRANSACTION_REF_NO, txn.getTransactionRefNumber());
				}
				log.log(Level.FINE, ">>>> End " + results);
				return ok(results);
			}

			String selectedApproverAction = json.findValue("selectedApproverAction").asText();
			Long transactionPrimId = json.findValue("transactionPrimId").asLong();
			String suppDoc = json.findValue("suppDoc") != null ? json.findValue("suppDoc").asText() : null;
			String txnRmarks = json.findValue("txnRmarks").asText() != null ? json.findValue("txnRmarks").asText()
					: null;
			Transaction txn = Transaction.findById(transactionPrimId);
			String txnInstrumentNumber = "";
			String txnInstrumentDate = "";
			long transactionPurposeId = txn.getTransactionPurpose().getId();
			entityTransaction.begin();
			do {
				BranchCashService branchCashService = new BranchCashServiceImpl();
				BranchBankService branchBankService = new BranchBankServiceImpl();
				Map<String, Object> criterias = new HashMap<String, Object>();
				if (!txn.getTransactionStatus().equals("Approved") && selectedApproverAction.equals("1")) {
					// approved action perform transaction operation
					txn.setTransactionStatus("Approved");
					txn.setModifiedBy(user);
					txn.setApproverActionBy(user);
					txn.setSupportingDocs(transactionDao.getAndDeleteSupportingDocument(txn.getSupportingDocs(),
							user.getEmail(), suppDoc, user, entityManager));
					if (txnRmarks != null && !txnRmarks.equals("")) {
						if (txn.getRemarks() != null) {
							txn.setRemarks(txn.getRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
						} else {
							txn.setRemarks(user.getEmail() + "#" + txnRmarks);
						}
					}
				}
				if (selectedApproverAction.equals("5")) {
					txn.setTransactionStatus("Require Clarification");
					txn.setModifiedBy(user);
					txn.setSupportingDocs(transactionDao.getAndDeleteSupportingDocument(txn.getSupportingDocs(),
							user.getEmail(), suppDoc, user, entityManager));
					if (txnRmarks != null && !txnRmarks.equals("")) {
						if (txn.getRemarks() != null) {
							txn.setRemarks(txn.getRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
						} else {
							txn.setRemarks(user.getEmail() + "#" + txnRmarks);
						}
					}
				}
				if (selectedApproverAction.equals("6")) {
					txn.setTransactionStatus("Clarified");
					txn.setModifiedBy(user);
					txn.setSupportingDocs(transactionDao.getAndDeleteSupportingDocument(txn.getSupportingDocs(),
							user.getEmail(), suppDoc, user, entityManager));
					if (txnRmarks != null && !txnRmarks.equals("")) {
						if (txn.getRemarks() != null) {
							txn.setRemarks(txn.getRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
						} else {
							txn.setRemarks(user.getEmail() + "#" + txnRmarks);
						}
					}
				}
				if (selectedApproverAction.equals("7")) {
					// get user roles for this id and if he is auditor user then for his remark add
					// "Auditor"
					StringBuilder sb = new StringBuilder();
					sb.append("select obj from UsersRoles obj where obj.user='" + user.getId()
							+ "' and obj.role NOT IN(1,2,9) and obj.presentStatus=1 ORDER BY obj.role.id");
					List<UsersRoles> userRoles = genericDAO.executeSimpleQuery(sb.toString(), entityManager);
					String roles = "";
					for (UsersRoles role : userRoles) {
						if (!role.getRole().getName().equals("OFFICERS")) {
							roles += role.getRole().getName() + ",";
						}
					}
					roles = roles.substring(0, roles.length() - 1);

					txn.setModifiedBy(user);
					txn.setSupportingDocs(transactionDao.getAndDeleteSupportingDocument(txn.getSupportingDocs(),
							user.getEmail(), suppDoc, user, entityManager));
					if (txnRmarks != null && !txnRmarks.equals("")) {
						String finalTxnRemark;
						if (roles.equals("AUDITOR")) {
							finalTxnRemark = user.getEmail() + "(Auditor)#" + txnRmarks;
						} else {
							finalTxnRemark = user.getEmail() + "#" + txnRmarks;
						}
						if (txn.getRemarks() != null) {
							txn.setRemarks(txn.getRemarks() + "|" + finalTxnRemark);
						} else {
							txn.setRemarks(finalTxnRemark);
						}
					}
				}
				if (selectedApproverAction.equals("8")) {
					txn.setModifiedBy(user);
				}
				if (!txn.getTransactionStatus().equals("Rejected")) {
					if (selectedApproverAction.equals("2")) {
						// reject action
						txn.setTransactionStatus("Rejected");
						txn.setModifiedBy(user);
						txn.setApproverActionBy(user);
						if (transactionPurposeId == 10L) {
							Double addedSpecialAdjustmentAmount = txn.getNetAmount();
							if (txn.getTransactionVendorCustomer() != null) {
								Vendor venor = txn.getTransactionVendorCustomer();
								if (venor.getAvailableSpecAdjAmount() != null) {
									txn.getTransactionVendorCustomer().setAvailableSpecAdjAmount(
											venor.getAvailableSpecAdjAmount() + addedSpecialAdjustmentAmount);
								}
								if (venor.getAvailableSpecAdjAmount() == null) {
									txn.getTransactionVendorCustomer()
											.setAvailableSpecAdjAmount(addedSpecialAdjustmentAmount);
								}
								genericDAO.saveOrUpdate(venor, user, entityManager);
							}
						}
						txn.setSupportingDocs(transactionDao.getAndDeleteSupportingDocument(txn.getSupportingDocs(),
								user.getEmail(), suppDoc, user, entityManager));
						if (txnRmarks != null && !txnRmarks.equals("")) {
							if (txn.getRemarks() != null) {
								txn.setRemarks(txn.getRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
							} else {
								txn.setRemarks(user.getEmail() + "#" + txnRmarks);
							}
						}
						if (txn.getAdjustmentFromAdvance() != null && txn.getAdjustmentFromAdvance() > 0) {
							if (txn.getTypeIdentifier() == IdosConstants.TXN_TYPE_CREDIT_AND_OPENING_BALANCE_ADV_PAID_VEND) {
								criterias.clear();
								criterias.put("vendorSpecific.id", txn.getTransactionVendorCustomer().getId());
								criterias.put("specificsVendors.id", txn.getTransactionSpecifics().getId());
								criterias.put("organization.id", user.getOrganization().getId());
								criterias.put("presentStatus", 1);
								VendorSpecific customerTxnSpecifics = genericDAO.getByCriteria(VendorSpecific.class,
										criterias,
										entityManager);
								customerTxnSpecifics.setAdvanceMoney(
										customerTxnSpecifics.getAdvanceMoney() + txn.getAdjustmentFromAdvance());
								genericDAO.saveOrUpdate(customerTxnSpecifics, user, entityManager);
							} else if (txn
									.getTypeIdentifier() == IdosConstants.TXN_TYPE_OPENING_BALANCE_ADV_PAID_BRACHWISE_VEND) {
								VendorDetail vendorDetail = VendorDetail.findByVendorGSTNID(entityManager,
										txn.getTransactionVendorCustomer().getId(),
										txn.getDestinationGstin());
								VendorBranchWiseAdvBalance checkForVendorOpeingAdvance = VendorBranchWiseAdvBalance
										.getAdvAmountForItem(entityManager,
												user.getOrganization().getId(),
												txn.getTransactionVendorCustomer().getId(),
												txn.getTransactionBranch().getId(),
												txn.getTypeOfSupply(),
												vendorDetail.getId(), txn.getTransactionSpecifics().getId());
								checkForVendorOpeingAdvance.setAdvanceAmount(
										checkForVendorOpeingAdvance.getAdvanceAmount()
												+ txn.getAdjustmentFromAdvance());
								genericDAO.saveOrUpdate(checkForVendorOpeingAdvance, user,
										entityManager);
							} else if (txn
									.getTypeIdentifier() == IdosConstants.TXN_TYPE_OPENING_BALANCE_ADV_PAID_BRACHWISE_CUST) {
								CustomerDetail customerDetails = CustomerDetail.findByCustomerGSTNID(entityManager,
										txn.getTransactionVendorCustomer().getId(),
										txn.getDestinationGstin());
								CustomerBranchWiseAdvBalance checkForCustomerOpeingAdvance = CustomerBranchWiseAdvBalance
										.getAdvAmountForItem(entityManager,
												user.getOrganization().getId(),
												txn.getTransactionVendorCustomer().getId(),
												txn.getTransactionBranch().getId(),
												txn.getTypeOfSupply(),
												customerDetails.getId(),
												txn.getTransactionSpecifics().getId());
								checkForCustomerOpeingAdvance.setAdvanceAmount(
										checkForCustomerOpeingAdvance.getAdvanceAmount()
												+ txn.getAdjustmentFromAdvance());
								genericDAO.saveOrUpdate(checkForCustomerOpeingAdvance, user,
										entityManager);
							}
						}

					}
				}
				if (selectedApproverAction.equals("3")) {
					// additional approval action
					txn.setTransactionStatus("Require Additional Approval");
					txn.setModifiedBy(user);
					txn.setApproverActionBy(user);
					String selectedAddApproverEmail = json.findValue("selectedAddApproverEmail").asText();
					txn.setSelectedAdditionalApprover(selectedAddApproverEmail);
					txn.setSupportingDocs(transactionDao.getAndDeleteSupportingDocument(txn.getSupportingDocs(),
							user.getEmail(), suppDoc, user, entityManager));
					if (txnRmarks != null && !txnRmarks.equals("")) {
						if (txn.getRemarks() != null) {
							txn.setRemarks(txn.getRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
						} else {
							txn.setRemarks(user.getEmail() + "#" + txnRmarks);
						}
					}
				}
				if (selectedApproverAction.equals("4")) {
					if (!txn.getTransactionStatus().equals("Accounted")) {
						Long tdsPayableSpecificID = 0L;
						if (txn.getIsBackdatedTransaction() != null
								&& txn.getIsBackdatedTransaction() != IdosConstants.BACK_DATED_TXN) {
							txn.setTransactionDate(new Date());
						}
						int txnreceiptdetails = json.findValue("txnReceiptDetails") != null
								? json.findValue("txnReceiptDetails").asInt()
								: 0;
						String txnreceipttypebankdetails = json.findValue("txnReceiptDescription") != null
								? json.findValue("txnReceiptDescription").asText()
								: null;
						String txnInvDate = json.findValue("txnInvDate") != null ? json.findValue("txnInvDate").asText()
								: null;
						if (txnInvDate != null && !txnInvDate.equals("")) {
							txn.setTransactionInvoiceDate(mysqldf.parse(mysqldf.format(idosdf.parse(txnInvDate))));
						}
						if (transactionPurposeId == IdosConstants.TRANSFER_FUNDS_FROM_ONE_BANK_TO_ANOTHER) {
							txn.setReceiptDetailsType(2);
						} else if (transactionPurposeId == IdosConstants.BUY_ON_CREDIT_PAY_LATER
								|| transactionPurposeId == IdosConstants.PURCHASE_ORDER
								|| transactionPurposeId == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
							txn.setReceiptDetailsType(null);
							txnreceiptdetails = 0;
						} else if (transactionPurposeId == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) {
							txn.setReceiptDetailsType(3);
							txnreceiptdetails = 3;
						} else if (transactionPurposeId != IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
								&& transactionPurposeId != IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
								&& transactionPurposeId != IdosConstants.PAY_VENDOR_SUPPLIER
								&& transactionPurposeId != IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER
								&& transactionPurposeId != IdosConstants.TRANSFER_MAIN_CASH_TO_PETTY_CASH
								&& transactionPurposeId != IdosConstants.REFUND_ADVANCE_RECEIVED
								&& transactionPurposeId != IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE) {
							txn.setReceiptDetailsType(txnreceiptdetails);
						}

						if (transactionPurposeId == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
								|| transactionPurposeId == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
								|| (transactionPurposeId == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER
										&& txn.getTypeIdentifier() == 1)) {
							sellTransactionService.verifyItemInvetory(user, entityManager, txn, results);
							sellTransactionService.completeAccounting(user, json, entityManager, entityTransaction, txn,
									results);
							transactionService.sendStockWebSocketResponse(entityManager, txn, user, results);
							entityTransaction.commit();
							log.log(Level.FINE, ">>>> End " + results);
							return Results.ok(results);
						} else if (transactionPurposeId == IdosConstants.CREDIT_NOTE_CUSTOMER
								|| transactionPurposeId == IdosConstants.DEBIT_NOTE_CUSTOMER) {
							sellTransactionService.completeAccounting(user, json, entityManager, entityTransaction, txn,
									results);
							transactionService.sendStockWebSocketResponse(entityManager, txn, user, results);
							results.put("isvalidstock", "1");
							entityTransaction.commit();
							log.log(Level.FINE, ">>>> End " + results);
							return Results.ok(results);
						} else if (transactionPurposeId == IdosConstants.SALES_RETURNS) {
							sellTransactionService.completeAccounting(user, json, entityManager, entityTransaction, txn,
									results);
						} else if (transactionPurposeId == IdosConstants.PREPARE_QUOTATION
								|| transactionPurposeId == IdosConstants.PROFORMA_INVOICE) {
							transactionDao.setInvoiceQuotProfSerial(user, entityManager, txn);
						} else if (transactionPurposeId == IdosConstants.PURCHASE_ORDER) {
							transactionDao.setInvoiceQuotProfSerial(user, entityManager, txn);
							// purchaseOrderService.createPurchaseOrderJSON(txn, user,entityManager,
							// transaction); //this is to send Purchase Order to Kaizala
						} else if (transactionPurposeId == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
								|| transactionPurposeId == IdosConstants.BUY_ON_CREDIT_PAY_LATER
								|| transactionPurposeId == IdosConstants.CREDIT_NOTE_VENDOR
								|| transactionPurposeId == IdosConstants.DEBIT_NOTE_VENDOR
								|| transactionPurposeId == IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER
								|| (transactionPurposeId == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER
										&& txn.getTypeIdentifier() == 2)
								|| transactionPurposeId == IdosConstants.REFUND_ADVANCE_RECEIVED
								|| transactionPurposeId == IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE) {
							if (transactionPurposeId == IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE) {
								if (txn.getWithholdingTax() != null && txn.getWithholdingTax() > 0.0) {
									Specifics specificsMap = coaService.getSpecificsForMapping(user, "8",
											entityManager);
									if (specificsMap == null) {
										throw new IDOSException(IdosConstants.RECORD_NOT_FOUND,
												IdosConstants.BUSINESS_EXCEPTION, "COA is not found for mapping id 8",
												"TDS COA mapping is not found for, type: 8- withholding tax (TDS) on payments received from customers and others");
									}
									tdsPayableSpecificID = specificsMap.getId();
								} else {
									tdsPayableSpecificID = -1l; // means withhelding tax not applicable
								}
							} else {
								transactionDao.setInvoiceQuotProfSerial(user, entityManager, txn);
								VendorTDSTaxes findByOrgVendSpecific = null;
								if (txn.getTransactionVendorCustomer() != null
										&& txn.getTransactionSpecifics() != null) {
									findByOrgVendSpecific = VendorTDSTaxes.findByOrgVend(entityManager,
											user.getOrganization().getId(), txn.getTransactionVendorCustomer().getId(),
											txn.getTransactionSpecifics(), txn.getTransactionDate());
								}
								Long withheldingTaxType = null;
								if (findByOrgVendSpecific != null && findByOrgVendSpecific.getTdsSection() != null) {
									withheldingTaxType = findByOrgVendSpecific.getTdsSection().getId();
								}
								// Integer withheldingTaxType = specf.getWithholdingType();
								if (withheldingTaxType != null) {
									Specifics specificsForMapping = coaService.getSpecificsForMapping(user,
											withheldingTaxType.toString(), entityManager);
									if (specificsForMapping == null) {
										throw new IDOSException(IdosConstants.RECORD_NOT_FOUND,
												IdosConstants.BUSINESS_EXCEPTION, "COA mapping is not found",
												"TDS COA mapping not found for, type: " + withheldingTaxType);
									}
									tdsPayableSpecificID = specificsForMapping.getId();
								} else {
									tdsPayableSpecificID = -1l; // means withhelding tax not applicable
								}
							}
							results.put("tdsPayableSpecific", tdsPayableSpecificID);
							if (tdsPayableSpecificID == null || tdsPayableSpecificID == 0) {
								return Results.ok(results);
							}
							if (transactionPurposeId == IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER) {
								txn.setPaymentStatus("PAID");
								txn.setVendorDuePayment(0.0);
								txn.setVendorNetPayment(txn.getNetAmount());
								boolean isSuccess = transactionService.submitForAccountPayAdvToVendAdv(txn,
										entityManager, user);
							}
							if (transactionPurposeId == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
									|| transactionPurposeId == IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER
									|| transactionPurposeId == IdosConstants.REFUND_ADVANCE_RECEIVED
									|| transactionPurposeId == IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE) {
								txn.setPaymentStatus("PAID");
								txn.setVendorDuePayment(0.0);
								txn.setVendorNetPayment(txn.getNetAmount());
								long txnReceiptPaymentBank = 0L;
								if (txn.getReceiptDetailsType() == null || txn.getReceiptDetailsType() == 0) {
									txnReceiptPaymentBank = json.findValue("txnReceiptPaymentBank") != null
											? json.findValue("txnReceiptPaymentBank").asLong()
											: 0L;
									txnInstrumentNumber = json.findValue("txnInstrumentNum") != null
											? json.findValue("txnInstrumentNum").asText()
											: "";
									txnInstrumentDate = json.findValue("txnInstrumentDate") != null
											? json.findValue("txnInstrumentDate").asText()
											: "";
								} else {
									txnreceiptdetails = txn.getReceiptDetailsType();
									if (txnreceiptdetails == IdosConstants.PAYMODE_BANK) {
										if (txn.getTransactionBranchBankAccount() == null) {
											throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
													IdosConstants.BUSINESS_EXCEPTION,
													IdosConstants.INVALID_DATA_EXCEPTION,
													"Bank is not selected in transaction when payment mode is Bank.");
										}
										txnReceiptPaymentBank = txn.getTransactionBranchBankAccount().getId();
										txnInstrumentNumber = txn.getInstrumentNumber();
										txnInstrumentDate = txn.getInstrumentDate();
									}
									if (txn.getReceiptDetailsDescription() != null) {
										txnreceipttypebankdetails = txn.getReceiptDetailsDescription();
									}
								}
								boolean isValid = buyTransactionService.setTxnPaymentDetail(user, entityManager, txn,
										txnreceiptdetails, txnReceiptPaymentBank, txnInstrumentNumber,
										txnInstrumentDate, results);
								if (!isValid) {
									return Results.ok(results);
								}
							} else if (transactionPurposeId == IdosConstants.BUY_ON_CREDIT_PAY_LATER
									|| transactionPurposeId == IdosConstants.CREDIT_NOTE_VENDOR
									|| transactionPurposeId == IdosConstants.DEBIT_NOTE_VENDOR
									|| transactionPurposeId == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
								txn.setPaymentStatus("NOT-PAID");
								txn.setVendorDuePayment(txn.getNetAmount());
							}
							if (transactionPurposeId == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
									|| transactionPurposeId == IdosConstants.BUY_ON_CREDIT_PAY_LATER
									|| transactionPurposeId == IdosConstants.CREDIT_NOTE_VENDOR
									|| transactionPurposeId == IdosConstants.DEBIT_NOTE_VENDOR
									|| transactionPurposeId == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
								stockService.insertTradingInventory(txn, user, entityManager);
								// get purchase order transaction to kaizala that processing done in IDOS
								if (txn.getPaidInvoiceRefNumber() != null
										&& !txn.getPaidInvoiceRefNumber().equals("")) {
									String poTransactionId = txn.getPaidInvoiceRefNumber();
									Transaction poTransaction = Transaction
											.findById(IdosUtil.convertStringToLong(poTransactionId));
									poTransaction.setAdvanceType(2); /*
																		 * PO has been processed through Buy
																		 * transaction
																		 * too, getting PO with status=1 only in buy
																		 * transaction
																		 */
									genericDAO.saveOrUpdate(poTransaction, user, entityManager);
									// purchaseOrderService.sendPurchaseOrderProcessingDone(poTransaction,
									// entityManager, entityTransaction);
									// for approval check which method from the service will call here with required
									// parameters
									// the two methods are submitForApprovalPurchaseOrder and approverAction
								}
								// call Karvy API to submit Buy data for GST Filing
								KarvyAuthorization karvyAPICall = new KarvyAuthorization(application);
								karvyAPICall.saveGSTFilingData(user, txn, entityManager);
							}

							if (IdosConstants.CREDIT_NOTE_VENDOR == txn.getTransactionPurpose().getId()
									|| IdosConstants.DEBIT_NOTE_VENDOR == txn.getTransactionPurpose().getId()) {
								List<Transaction> buyTxnList = Transaction.findByTxnReference(entityManager,
										txn.getTransactionBranchOrganization().getId(), txn.getLinkedTxnRef());
								Transaction buyTxn = null;
								if (buyTxnList != null && !buyTxnList.isEmpty()) {
									buyTxn = buyTxnList.get(0);
								}
								if (buyTxn != null) {
									Double purchaseReturn = buyTxn.getPurchaseReturnAmount() == null ? 0.0
											: buyTxn.getPurchaseReturnAmount();
									purchaseReturn += txn.getNetAmount();
									buyTxn.setPurchaseReturnAmount(purchaseReturn);
									genericDAO.saveOrUpdate(buyTxn, user, entityManager);
								}
							}

						} else if (transactionPurposeId == IdosConstants.PAY_VENDOR_SUPPLIER) {
							// advance adjustment for buy on cash/credit/pay vendor
							// advance adjustment
							Double advanceAdjustment1 = 0.0;
							if (txn.getAdjustmentFromAdvance() != null) {
								advanceAdjustment1 = txn.getAdjustmentFromAdvance();
							}
							// On vendor Screen two advances: 1. Vendor credit sale
							// advance:getOpeningBalance (ie. we need to pay this money to vendor) and
							// 2. Vendor advance (given extra money to vendor for buy):
							// getOpeningBalanceAdvPaid
							Vendor vendor = txn.getTransactionVendorCustomer();
							Branch branch = txn.getTransactionBranch();
							String queryOpBalTxn = "select obj from TransactionItems obj where obj.organization.id=?1 and obj.branch.id=?2 and obj.transaction.id= ?3 and obj.transactionRefNumber = ?4 and obj.presentStatus=1";
							ArrayList<Object> inparam1 = new ArrayList<Object>(4);
							inparam1.add(user.getOrganization().getId());
							inparam1.add(branch.getId());
							inparam1.add(txn.getId());
							inparam1.add("-1");
							List<TransactionItems> opBalTxn = genericDAO.queryWithParamsName(queryOpBalTxn,
									entityManager, inparam1);
							if (opBalTxn.size() > 0 && opBalTxn.get(0) != null) {
								// If -1 then Opening Bal vendor adjustment invoice, no pending transaction
								// exist
								double totalPaidAmount = 0.0;
								if (opBalTxn.get(0).getNetAmount() != null)
									totalPaidAmount += opBalTxn.get(0).getNetAmount();
								if (opBalTxn.get(0).getDiscountAmount() != null)
									totalPaidAmount += opBalTxn.get(0).getDiscountAmount();
								double openingBalance = vendor.getTotalOpeningBalance() - totalPaidAmount; // reduce
																											// opening
																											// balance
																											// by the
																											// amount
																											// which is
																											// paid to
																											// vendor
								vendor.setTotalOpeningBalance(openingBalance); // If Opening balance invoice then ref no
																				// = -1, in that case we don't have
																				// specifics id etc, so don't do adv
																				// adjustment
								genericDAO.saveOrUpdate(vendor, user, entityManager);

								// Branch Opening balance decuction for Branch
								criterias.clear();
								criterias.put("organization.id", user.getOrganization().getId());
								criterias.put("branch.id", branch.getId());
								criterias.put("vendor.id", vendor.getId());
								criterias.put("presentStatus", 1);
								BranchVendors branchVendors = genericDAO.getByCriteria(BranchVendors.class, criterias,
										entityManager);
								if (branchVendors.getOpeningBalance() != null && txn.getNetAmount() != null) {
									double openingBalanceBranch = branchVendors.getOpeningBalance()
											- txn.getNetAmount();
									branchVendors.setOpeningBalance(openingBalanceBranch);
									genericDAO.saveOrUpdate(branchVendors, user, entityManager);
								}
							} else if (txn.getTypeIdentifier() != null && txn
									.getTypeIdentifier() == IdosConstants.TXN_TYPE_OPENING_BALANCE_BILLWISE_VEND) {
								// If -1 then Opening Bal vendor adjustment invoice, no pending transaction
								// exist
								// BILLWISE OPENING BALANCE CALCULATIONS
								double openingBalance = vendor.getTotalOpeningBalance() - txn.getNetAmount();// reduce
																												// opening
																												// balance
																												// by
																												// the
																												// amount
																												// which
																												// is
																												// paid
																												// to
																												// vendor
								vendor.setTotalOpeningBalance(openingBalance); // If Opening balance invoice then ref no
																				// = -1, in that case we don't have
																				// specifics id etc, so don't do adv
																				// adjustment
								genericDAO.saveOrUpdate(vendor, user, entityManager);
								Long billId = IdosUtil.convertStringToLong(txn.getPaidInvoiceRefNumber());
								if (vendor.getType() == 1 && billId != null) {
									VendorBillwiseOpBalance billwiseOpeningBalance = VendorBillwiseOpBalance
											.findById(billId);
									if (billwiseOpeningBalance.getOpeningBalance() != null
											&& txn.getNetAmount() != null) {
										double opBal = billwiseOpeningBalance.getOpeningBalance() - txn.getNetAmount();
										billwiseOpeningBalance.setOpeningBalance(opBal);
										genericDAO.saveOrUpdate(billwiseOpeningBalance, user, entityManager);
									}
								}
								// Branch Opening balance decuction for Branch
								criterias.clear();
								criterias.put("organization.id", user.getOrganization().getId());
								criterias.put("branch.id", branch.getId());
								criterias.put("vendor.id", vendor.getId());
								criterias.put("presentStatus", 1);
								BranchVendors branchVendors = genericDAO.getByCriteria(BranchVendors.class, criterias,
										entityManager);
								if (branchVendors.getOpeningBalance() != null && txn.getNetAmount() != null) {
									double openingBalanceBranch = branchVendors.getOpeningBalance()
											- txn.getNetAmount();
									branchVendors.setOpeningBalance(openingBalanceBranch);
									genericDAO.saveOrUpdate(branchVendors, user, entityManager);
								}
							} else {
								if (txn.getAdvanceType() == 2) { // OB for advance paid through Vendor screen
									if (vendor.getTotalOpeningBalanceAdvPaid() != null
											&& txn.getAdjustmentFromAdvance() != null) {
										vendor.setTotalOpeningBalanceAdvPaid(vendor.getTotalOpeningBalanceAdvPaid()
												- txn.getAdjustmentFromAdvance());
										genericDAO.saveOrUpdate(vendor, user, entityManager);
									}
								} else {
									if (txn.getAdjustmentFromAdvance() != null
											&& !txn.getAdjustmentFromAdvance().equals("") && advanceAdjustment1 > 0.0) {
										criterias.clear();
										criterias.put("vendorSpecific.id", txn.getTransactionVendorCustomer().getId());
										criterias.put("specificsVendors.id", txn.getTransactionSpecifics().getId());
										criterias.put("organization.id", user.getOrganization().getId());
										criterias.put("presentStatus", 1);
										VendorSpecific vendorTxnSpecifics = genericDAO
												.getByCriteria(VendorSpecific.class, criterias, entityManager);
										vendorTxnSpecifics.setAdvanceMoney(
												vendorTxnSpecifics.getAdvanceMoney() - txn.getAdjustmentFromAdvance());
										genericDAO.saveOrUpdate(vendorTxnSpecifics, user, entityManager);
									}
								}
							}

							// pay vendor/supplier effect on previous transaction invoice
							txn.setVendorDuePayment(0.0);
							Double txnVendNetAmt = txn.getNetAmount();
							if (txn.getWithholdingTax() != null) {
								txnVendNetAmt = txnVendNetAmt - txn.getWithholdingTax();
							}
							txn.setVendorNetPayment(txnVendNetAmt);
							txn.setPaymentStatus("PAID");
							if (txn.getTypeIdentifier() != null
									&& txn.getTypeIdentifier() == IdosConstants.TXN_TYPE_OTHER_TRANSACTIONS_VEND) { // if
																													// not
																													// Opening
																													// balance
																													// invoice
																													// transaction
								List<TransactionItems> selectedTxnsList = null;
								String selectedTxnsListQuery = "select obj from TransactionItems obj where obj.organization.id =?1 and obj.transaction.id=?2 and obj.presentStatus=1";
								inparam1.clear();
								inparam1.add(user.getOrganization().getId());
								inparam1.add(txn.getId());
								selectedTxnsList = genericDAO.queryWithParamsName(selectedTxnsListQuery.toString(),
										entityManager, inparam1);
								for (TransactionItems txnItem : selectedTxnsList) {
									criterias.clear();
									criterias.put("transactionRefNumber", txnItem.getTransactionRefNumber());
									criterias.put("presentStatus", 1);
									Transaction pendingTransaction = genericDAO.getByCriteria(Transaction.class,
											criterias, entityManager);
									Double vendorNetPayment = null;
									Double advanceIfAnyForAdjustment = null;
									Double advanceAdjustment = null;
									Double availableAdvanceAdjustment = null;
									Double withHoldingAmount = null;
									Double discountReceived = null;
									if ((!txn.getAvailableAdvance().equals("") && txn.getAvailableAdvance() != null)
											&& (!txn.getAdjustmentFromAdvance().equals("")
													&& txn.getAdjustmentFromAdvance() != null)) {
										advanceIfAnyForAdjustment = txn.getAvailableAdvance();
										advanceAdjustment = txn.getAdjustmentFromAdvance();
									} else {
										advanceIfAnyForAdjustment = 0.0;
										advanceAdjustment = 0.0;
									}
									if (txnItem.getWithholdingAmount() != null) {
										withHoldingAmount = txnItem.getWithholdingAmount();
									}
									if (txnItem.getDiscountAmount() != null) {
										discountReceived = txnItem.getDiscountAmount();
									}
									if (pendingTransaction.getVendorNetPayment() != null) {
										vendorNetPayment = pendingTransaction.getVendorNetPayment()
												+ txnItem.getNetAmount() + advanceAdjustment;
									}
									if (pendingTransaction.getVendorNetPayment() == null) {
										vendorNetPayment = txnItem.getNetAmount() + advanceAdjustment;
									}
									if (withHoldingAmount != null) {
										vendorNetPayment = vendorNetPayment - withHoldingAmount;
									}
									if (discountReceived != null) {
										vendorNetPayment = vendorNetPayment + discountReceived;
									}
									pendingTransaction.setWithholdingTax(txnItem.getWithholdingAmount());
									pendingTransaction.setAvailableAdvance(advanceIfAnyForAdjustment);
									if (pendingTransaction.getAdjustmentFromAdvance() != null) {
										availableAdvanceAdjustment = pendingTransaction.getAdjustmentFromAdvance()
												+ advanceAdjustment;
									}
									if (pendingTransaction.getAdjustmentFromAdvance() == null) {
										availableAdvanceAdjustment = advanceAdjustment;
									}
									pendingTransaction.setAdjustmentFromAdvance(availableAdvanceAdjustment);
									pendingTransaction.setVendorNetPayment(vendorNetPayment);
									String dueArr[] = txn.getNetAmountResultDescription()
											.substring(0, txn.getNetAmountResultDescription().length()).split(",");
									String dueString = dueArr[2];
									String dueStringArr[] = dueString.substring(0, dueString.length()).split(":");
									String due = dueStringArr[1];
									Double duePayment = pendingTransaction.getNetAmount() - vendorNetPayment;
									if (duePayment > 0) {
										pendingTransaction.setVendorDuePayment(duePayment);
										pendingTransaction.setPaymentStatus("PARTLY-PAID");
									} else {
										pendingTransaction.setPaymentStatus("PAID");
										pendingTransaction.setVendorDuePayment(0d);
										pendingTransaction.setVendorDuePayment(pendingTransaction.getNetAmount());
									}
									genericDAO.saveOrUpdate(pendingTransaction, user, entityManager);
								}
							}
							// For opening balance invoice and vendor invoices do bank/cash adjustment
							long txnReceiptPaymentBank = 0L;
							if (txn.getReceiptDetailsType() == null || txn.getReceiptDetailsType() == 0) {
								txnReceiptPaymentBank = json.findValue("txnReceiptPaymentBank") != null
										? json.findValue("txnReceiptPaymentBank").asLong()
										: 0L;
								txnInstrumentNumber = json.findValue("txnInstrumentNum") != null
										? json.findValue("txnInstrumentNum").asText()
										: "";
								txnInstrumentDate = json.findValue("txnInstrumentDate") != null
										? json.findValue("txnInstrumentDate").asText()
										: "";
							} else {
								txnreceiptdetails = txn.getReceiptDetailsType();
								if (txnreceiptdetails == IdosConstants.PAYMODE_BANK) {
									txnReceiptPaymentBank = txn.getTransactionBranchBankAccount().getId();
									txnInstrumentNumber = txn.getInstrumentNumber();
									txnInstrumentDate = txn.getInstrumentDate();
								}
								txnreceipttypebankdetails = txn.getReceiptDetailsDescription();
							}
							boolean isValid = buyTransactionService.setTxnPaymentDetail(user, entityManager, txn,
									txnreceiptdetails, txnReceiptPaymentBank, txnInstrumentNumber, txnInstrumentDate,
									results);
							if (!isValid) {
								return Results.ok(results);
							}
							TRIAL_BALANCE_SERVICE.insertTrialBalance(txn, user, entityManager);
						} else if (transactionPurposeId == IdosConstants.TRANSFER_MAIN_CASH_TO_PETTY_CASH) {
							txn.setPaymentStatus("PAID");
							Branch branch = txn.getTransactionBranch();
							if (txn.getReceiptDetailsType() != null && txn.getReceiptDetailsType() > 0) {
								txnreceiptdetails = txn.getReceiptDetailsType();
								txnreceipttypebankdetails = txn.getReceiptDetailsDescription();
							}
							if (txnreceiptdetails == IdosConstants.PAYMODE_CASH) {
								Double resultantCashTmp = null;
								StringBuilder newsbquery = new StringBuilder(
										"select obj from BranchCashCount obj WHERE obj.branch.id='");
								newsbquery.append(branch.getId()).append("' AND obj.organization.id='");
								newsbquery.append(branch.getOrganization().getId())
										.append("' and obj.presentStatus=1 ORDER BY obj.date desc");
								List<BranchCashCount> prevBranchCashCount = genericDAO
										.executeSimpleQueryWithLimit(newsbquery.toString(), entityManager, 1);
								if (prevBranchCashCount.size() > 0) {
									BranchCashCount branchCashCount = prevBranchCashCount.get(0);
									resultantCashTmp = branchCashCount.getResultantCash() - txn.getNetAmount();
									results.put("resultantCash", resultantCashTmp);
									if (resultantCashTmp >= 0) {
										branchCashCount.setResultantCash(resultantCashTmp);
										if (branchCashCount.getTotalMainCashToPettyCash() != null) {
											branchCashCount.setTotalMainCashToPettyCash(
													branchCashCount.getTotalMainCashToPettyCash() + txn.getNetAmount());
										} else {
											branchCashCount.setTotalMainCashToPettyCash(txn.getNetAmount());
										}
										if (branchCashCount.getResultantPettyCash() != null) {
											branchCashCount.setResultantPettyCash(
													branchCashCount.getResultantPettyCash() + txn.getNetAmount());
										} else {
											branchCashCount.setResultantPettyCash(txn.getNetAmount());
										}

										if (branchCashCount.getCreditAmount() != null) {
											branchCashCount.setCreditAmount(
													branchCashCount.getCreditAmount() + txn.getNetAmount());
										} else {
											branchCashCount.setCreditAmount(txn.getNetAmount());
										}

										genericDAO.saveOrUpdate(branchCashCount, user, entityManager);
									} else {
										return Results.ok(results);
									}
								}
							}
							TRIAL_BALANCE_SERVICE.insertTrialBalance(txn, user, entityManager);
						} else if (transactionPurposeId == IdosConstants.PURCHASE_RETURNS) {
							txn.setPaymentStatus("PAID");
							txn.setVendorDuePayment(0.0);
							txn.setVendorNetPayment(txn.getNetAmount());
							criterias.clear();
							criterias.put("transactionRefNumber", txn.getPaidInvoiceRefNumber());
							criterias.put("presentStatus", 1);
							Transaction previousTransaction = genericDAO.getByCriteria(Transaction.class, criterias,
									entityManager);
							Double purchaseReturnValue = 0.0;
							if (previousTransaction.getPurchaseReturnAmount() != null) {
								purchaseReturnValue = previousTransaction.getPurchaseReturnAmount()
										+ txn.getNetAmount();
							}
							if (previousTransaction.getSalesReturnAmount() == null) {
								purchaseReturnValue = txn.getNetAmount();
							}
							previousTransaction.setPurchaseReturnAmount(purchaseReturnValue);
							genericDAO.saveOrUpdate(previousTransaction, user, entityManager);
							TRIAL_BALANCE_SERVICE.insertTrialBalance(txn, user, entityManager);
							stockService.insertTradingInventory(txn, user, entityManager);
						} else if (transactionPurposeId == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) {
							double resultantCash = branchCashService.updateBranchCashDetail(entityManager, user,
									txn.getTransactionBranch(), txn.getNetAmount(), true, txn.getTransactionDate(),
									results);
							if (resultantCash < 0.0) {
								throw new IDOSException(IdosConstants.INSUFFICIENT_BALANCE_ERRCODE,
										IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INSUFFICIENT_BALANCE_EXCEPTION,
										"Pettycash balance is insufficient.");
							}
							txn.setPaymentStatus("PAID");
							txn.setVendorDuePayment(0.0);
							txn.setVendorNetPayment(txn.getNetAmount());
							genericDAO.saveOrUpdate(txn, user, entityManager);
							// Trial balance entries
							TRIAL_BALANCE_SERVICE.insertTrialBalance(txn, user, entityManager);
							if (txn.getRoundedCutPartOfNetAmount() != null
									&& txn.getRoundedCutPartOfNetAmount() != 0.0) {
								Boolean roundupMappingFound = null;
								if (txn.getRoundedCutPartOfNetAmount() > 0) {
									roundupMappingFound = TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
											txn.getTransactionBranchOrganization(), txn.getTransactionBranch(),
											txn.getId(), txn.getTransactionPurpose(), txn.getTransactionDate(),
											txn.getRoundedCutPartOfNetAmount(), user, entityManager, false);
								} else {
									roundupMappingFound = TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
											txn.getTransactionBranchOrganization(), txn.getTransactionBranch(),
											txn.getId(), txn.getTransactionPurpose(), txn.getTransactionDate(),
											txn.getRoundedCutPartOfNetAmount(), user, entityManager, true);
								}
								results.put("roundupMappingFound", roundupMappingFound);
								if (roundupMappingFound == null || !roundupMappingFound) {
									return Results.ok(results);
								}
							}
							stockService.insertTradingInventory(txn, user, entityManager);
						}
						txn.setReceiptDetailsDescription(txnreceipttypebankdetails);
						txn.setTransactionStatus("Accounted");
						/***************************
						 * TRIAL BALANCE TABLES
						 ******************************/
						if ((transactionPurposeId == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER
								&& txn.getTypeIdentifier() == 2)
								|| transactionPurposeId == IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER) {
							TRIAL_BALANCE_SERVICE.insertTrialBalance(txn, user, entityManager); // TrailBalance entries
						} else if (transactionPurposeId == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
								|| transactionPurposeId == IdosConstants.BUY_ON_CREDIT_PAY_LATER
								|| transactionPurposeId == IdosConstants.CREDIT_NOTE_VENDOR) {
							TRIAL_BALANCE_SERVICE.insertTrialBalance(txn, user, entityManager); // TrailBalance entries
							if (txn.getRoundedCutPartOfNetAmount() != null
									&& txn.getRoundedCutPartOfNetAmount() != 0.0) {
								Boolean roundupMappingFound = null;
								if (txn.getRoundedCutPartOfNetAmount() > 0) {
									roundupMappingFound = TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
											txn.getTransactionBranchOrganization(), txn.getTransactionBranch(),
											txn.getId(), txn.getTransactionPurpose(), txn.getTransactionDate(),
											txn.getRoundedCutPartOfNetAmount(), user, entityManager, false);
								} else {
									roundupMappingFound = TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
											txn.getTransactionBranchOrganization(), txn.getTransactionBranch(),
											txn.getId(), txn.getTransactionPurpose(), txn.getTransactionDate(),
											txn.getRoundedCutPartOfNetAmount(), user, entityManager, true);
								}
								results.put("roundupMappingFound", roundupMappingFound);
								if (roundupMappingFound == null || !roundupMappingFound) {
									return Results.ok(results);
								}
							}
						} else if (transactionPurposeId == IdosConstants.DEBIT_NOTE_VENDOR) {
							TRIAL_BALANCE_SERVICE.insertTrialBalance(txn, user, entityManager); // TrailBalance entries
							if (txn.getRoundedCutPartOfNetAmount() != null
									&& txn.getRoundedCutPartOfNetAmount() != 0.0) {
								Boolean roundupMappingFound = null;
								if (txn.getRoundedCutPartOfNetAmount() > 0) {
									roundupMappingFound = TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
											txn.getTransactionBranchOrganization(), txn.getTransactionBranch(),
											txn.getId(), txn.getTransactionPurpose(), txn.getTransactionDate(),
											txn.getRoundedCutPartOfNetAmount(), user, entityManager, true);
								} else {
									roundupMappingFound = TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
											txn.getTransactionBranchOrganization(), txn.getTransactionBranch(),
											txn.getId(), txn.getTransactionPurpose(), txn.getTransactionDate(),
											txn.getRoundedCutPartOfNetAmount(), user, entityManager, false);
								}
								results.put("roundupMappingFound", roundupMappingFound);
								if (roundupMappingFound == null || !roundupMappingFound) {
									return Results.ok(results);
								}
							}
						}

						// ************CUST-VENDOR ADVANCE************//
						if (transactionPurposeId != IdosConstants.CREDIT_NOTE_VENDOR
								&& transactionPurposeId != IdosConstants.DEBIT_NOTE_VENDOR) {
							// ADVANCE IS CREDITED for Buy on cash(3), buy on credit(4), buy on petty
							// cash(11)
							if (txn.getAdjustmentFromAdvance() != null && txn.getAdjustmentFromAdvance() > 0.0
									&& !txn.getAdjustmentFromAdvance().equals("") && txn.getAdvanceType() != 2) {
								// Out of sell transaction of 1000/- if adjusting 400/- from customer advance
								// then 400 will go as Debit for sell transaction
								if (transactionPurposeId == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT
										|| (transactionPurposeId == IdosConstants.PAY_VENDOR_SUPPLIER
												&& !txn.getPaidInvoiceRefNumber().equals("-1"))) {
									TrialBalanceVendorAdvance trialBalVenAdv = new TrialBalanceVendorAdvance(); // adv
																												// in
																												// case
																												// of
																												// sell
																												// on
																												// cash
																												// and
																												// credit
																												// both
									trialBalVenAdv.setTransactionId(txn.getId());
									trialBalVenAdv.setTransactionPurpose(txn.getTransactionPurpose());
									trialBalVenAdv.setTransactionSpecifics(txn.getTransactionSpecifics());
									trialBalVenAdv.setTransactionParticulars(txn.getTransactionParticulars());
									trialBalVenAdv.setDate(txn.getTransactionDate());
									trialBalVenAdv.setBranch(txn.getTransactionBranch());
									trialBalVenAdv.setOrganization(txn.getTransactionBranchOrganization());
									trialBalVenAdv.setVendorType(1); // vendor type=2 means customer and =1 means vendor
									trialBalVenAdv.setVendor(txn.getTransactionVendorCustomer());
									trialBalVenAdv.setCreditAmount(txn.getAdjustmentFromAdvance());
									genericDAO.saveOrUpdate(trialBalVenAdv, user, entityManager);
								}
							}
							if (IdosConstants.PAYMODE_CASH == txnreceiptdetails) {
								/*
								 * if (transactionPurposeId == IdosConstants.PAY_VENDOR_SUPPLIER) { // PENDING
								 * TB EDFFECT FOR BILLWISE TrialBalanceBranchCash trialBalCash = new
								 * TrialBalanceBranchCash(); //will affect only for sell on cash
								 * trialBalCash.setTransactionId(txn.getId());
								 * trialBalCash.setTransactionPurpose(txn.getTransactionPurpose());
								 * trialBalCash.setDate(txn.getTransactionDate());
								 * trialBalCash.setBranch(txn.getTransactionBranch());
								 * trialBalCash.setCashType(new Integer(IdosConstants.CASH)); //1=normal cash
								 * and 2=petty cash
								 * trialBalCash.setOrganization(txn.getTransactionBranchOrganization());
								 * trialBalCash.setCreditAmount(txn.getNetAmount()); if
								 * (!txn.getTransactionBranch().getBranchDepositKeys().isEmpty()) {
								 * trialBalCash.setBranchDepositBoxKey(txn.getTransactionBranch().
								 * getBranchDepositKeys().get(0)); } genericDAO.saveOrUpdate(trialBalCash, user,
								 * entityManager); }
								 */
								if (transactionPurposeId == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) {
									TrialBalanceBranchCash trialBalCash = new TrialBalanceBranchCash(); // will affect
																										// only for sell
																										// on cash
									trialBalCash.setTransactionId(txn.getId());
									trialBalCash.setTransactionPurpose(txn.getTransactionPurpose());
									trialBalCash.setDate(txn.getTransactionDate());
									trialBalCash.setBranch(txn.getTransactionBranch());
									trialBalCash.setCashType(new Integer(IdosConstants.PETTY_CASH)); // 1=normal cash
																										// and 2=petty
																										// cash
									trialBalCash.setOrganization(txn.getTransactionBranchOrganization());
									trialBalCash.setDebitAmount(txn.getNetAmount());
									if (!txn.getTransactionBranch().getBranchDepositKeys().isEmpty()) {
										trialBalCash.setBranchDepositBoxKey(
												txn.getTransactionBranch().getBranchDepositKeys().get(0));
									}
									genericDAO.saveOrUpdate(trialBalCash, user, entityManager);
								}
							}

							// **************** BANK *************************/
							/*
							 * if (IdosConstants.PAYMODE_BANK == txnreceiptdetails) { if
							 * (transactionPurposeId == IdosConstants.PAY_VENDOR_SUPPLIER) { // PENDING TB
							 * EDFFECT FOR BILLWISE if (txn.getTransactionBranchBankAccount() == null) {
							 * throw new IDOSException(IdosConstants.RECORD_NOT_FOUND,
							 * IdosConstants.BUSINESS_EXCEPTION, IdosConstants.NULL_KEY_EXC_ESMF_MSG,
							 * "Bank detail are not found in transaction"); } TrialBalanceBranchBank
							 * trialBalBank = new TrialBalanceBranchBank();
							 * trialBalBank.setTransactionId(txn.getId());
							 * trialBalBank.setTransactionPurpose(txn.getTransactionPurpose());
							 * trialBalBank.setDate(txn.getTransactionDate());
							 * trialBalBank.setBranchBankAccounts(txn.getTransactionBranchBankAccount());
							 * //added by Sunil trialBalBank.setBranch(txn.getTransactionBranch());
							 * trialBalBank.setOrganization(txn.getTransactionBranchOrganization());
							 * trialBalBank.setCreditAmount(txn.getNetAmount());
							 * genericDAO.saveOrUpdate(trialBalBank, user, entityManager); } }
							 */
						}
						// ***************************TAXES*************************/
						if (transactionPurposeId == IdosConstants.PAY_VENDOR_SUPPLIER
								|| transactionPurposeId == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) {
							// get all taxes for this transaction
							String taxNames[] = new String[10];
							double taxValues[] = new double[10];
							if (txn.getTaxName1() != null) {
								taxNames[0] = txn.getTaxName1();
								taxValues[0] = txn.getTaxValue1();
							}
							if (txn.getTaxName2() != null) {
								taxNames[1] = txn.getTaxName2();
								taxValues[1] = txn.getTaxValue2();
							}
							if (txn.getTaxName3() != null) {
								taxNames[2] = txn.getTaxName3();
								taxValues[2] = txn.getTaxValue3();
							}
							if (txn.getTaxName4() != null) {
								taxNames[3] = txn.getTaxName4();
								taxValues[3] = txn.getTaxValue4();
							}
							if (txn.getTaxName5() != null) {
								taxNames[4] = txn.getTaxName5();
								taxValues[4] = txn.getTaxValue5();
							}
							if (txn.getTaxName6() != null) {
								taxNames[5] = txn.getTaxName6();
								taxValues[5] = txn.getTaxValue6();
							}
							if (txn.getTaxName7() != null) {
								taxNames[6] = txn.getTaxName7();
								taxValues[6] = txn.getTaxValue7();
							}

							// get all taxes for this branch and organization
							criterias.clear();
							criterias.put("branch.id", txn.getTransactionBranch().getId());
							criterias.put("organization.id", user.getOrganization().getId());
							criterias.put("taxType", 1); // input taxes=1, output taxes=2, 3=Withholding tax payment
															// received from customer, 4=Withholding tax payment made to
															// vendor
							List<BranchTaxes> branchTaxesList = genericDAO.findByCriteria(BranchTaxes.class, criterias,
									entityManager);
							for (int i = 0; i < taxNames.length; i++) {
								if (taxNames[i] != null) {
									for (BranchTaxes bnchTaxes : branchTaxesList) {
										if (taxNames[i].equalsIgnoreCase(bnchTaxes.getTaxName())) {
											TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
											trialBalTaxes.setTransactionId(txn.getId());
											trialBalTaxes.setTransactionPurpose(txn.getTransactionPurpose());
											trialBalTaxes.setDate(txn.getTransactionDate());
											trialBalTaxes.setBranchTaxes(bnchTaxes);
											trialBalTaxes.setTaxType((int) IdosConstants.INPUT_TAX); // input taxes
											trialBalTaxes.setBranch(txn.getTransactionBranch());
											trialBalTaxes.setOrganization(txn.getTransactionBranchOrganization());
											if (transactionPurposeId == IdosConstants.PAY_VENDOR_SUPPLIER) {
												trialBalTaxes.setCreditAmount(taxValues[i]);
											} else {
												trialBalTaxes.setDebitAmount(taxValues[i]);
											}
											trialBalTaxes.setTransactionSpecifics(txn.getTransactionSpecifics());
											trialBalTaxes.setTransactionParticulars(txn.getTransactionParticulars());
											genericDAO.saveOrUpdate(trialBalTaxes, user, entityManager);
										}
									}
								}
							}
						}

						if (transactionPurposeId == IdosConstants.REFUND_ADVANCE_RECEIVED) {
							transactionDao.setInvoiceQuotProfSerial(user, entityManager, txn);
							TRIAL_BALANCE_SERVICE.insertTrialBalance(txn, user, entityManager);
							KarvyAuthorization karvyAPICall = new KarvyAuthorization(application);
							karvyAPICall.saveGSTFilingData(user, txn, entityManager);
						} else if (transactionPurposeId == IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE) {
							transactionDao.setInvoiceQuotProfSerial(user, entityManager, txn);
							TRIAL_BALANCE_SERVICE.insertTrialBalance(txn, user, entityManager);
						} else if (transactionPurposeId == IdosConstants.CANCEL_INVOICE) {
							transactionDao.setInvoiceQuotProfSerial(user, entityManager, txn);
							INVOICE_SERVICE.saveInvoiceLog(user, entityManager, txn, null, json);
							TRIAL_BALANCE_SERVICE.insertTrialBalance(txn, user, entityManager);
							if (txn.getRoundedCutPartOfNetAmount() != null
									&& txn.getRoundedCutPartOfNetAmount() != 0.0) {
								Boolean roundupMappingFound = null;
								if (txn.getRoundedCutPartOfNetAmount() > 0) {
									TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
											txn.getTransactionBranchOrganization(), txn.getTransactionBranch(),
											txn.getId(), txn.getTransactionPurpose(), txn.getTransactionDate(),
											txn.getRoundedCutPartOfNetAmount(), user, entityManager, false);
								} else {
									TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
											txn.getTransactionBranchOrganization(), txn.getTransactionBranch(),
											txn.getId(), txn.getTransactionPurpose(), txn.getTransactionDate(),
											txn.getRoundedCutPartOfNetAmount(), user, entityManager, true);
								}
							}
							stockService.insertTradingInventory(txn, user, entityManager);
						}

						/********************************
						 * END TRIAL BALANCE ENTRIES
						 **********************************/
						if (transactionPurposeId == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
								|| transactionPurposeId == IdosConstants.BUY_ON_CREDIT_PAY_LATER) {
							buyTransactionService.saveUpdateBudget4Items(txn, user, entityManager);
						}
						buyTransactionService.calculateAndSaveTds(entityManager, user, txn);
					}
					txn.setSupportingDocs(transactionDao.getAndDeleteSupportingDocument(txn.getSupportingDocs(),
							user.getEmail(), suppDoc, user, entityManager));
					if (txnRmarks != null && !txnRmarks.equals("")) {
						if (txn.getRemarks() != null) {
							txn.setRemarks(txn.getRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
						} else {
							txn.setRemarks(user.getEmail() + "#" + txnRmarks);
						}
					}
					if (transactionPurposeId == IdosConstants.WITHDRAW_CASH_FROM_BANK
							|| transactionPurposeId == IdosConstants.DEPOSIT_CASH_IN_BANK
							|| transactionPurposeId == IdosConstants.TRANSFER_FUNDS_FROM_ONE_BANK_TO_ANOTHER) {
						txn.setTransactionStatus("Accounted");
						txn.setTxnDone(1);
						if (transactionPurposeId == IdosConstants.WITHDRAW_CASH_FROM_BANK) {
							boolean branchBankDetailEntered = branchBankService.updateBranchBankDetailTransaction(
									entityManager, user, txn.getTransactionBranchBankAccount(), txn.getNetAmount(),
									true, results, txn.getTransactionDate(), txn.getTransactionBranch());
							if (!branchBankDetailEntered) {
								return Results.ok(results); // since balance is in -ve don't make any changes in DB
							}
							Double resultantCash = branchCashService.updateBranchCashDetail(entityManager, user,
									txn.getTransactionBranch(), txn.getNetAmount(), false, txn.getTransactionDate(),
									results);
							results.put("resultantCash", resultantCash);
						} else if (transactionPurposeId == IdosConstants.DEPOSIT_CASH_IN_BANK) {
							Double resultantCash = branchCashService.updateBranchCashDetail(entityManager, user,
									txn.getTransactionBranch(), txn.getNetAmount(), true, txn.getTransactionDate(),
									results);
							results.put("resultantCash", resultantCash);
							if (resultantCash < 0) {
								return Results.ok(results);
							}
							Double resultantAmount = branchBankService.updateBranchBankDetail(entityManager, user,
									txn.getTransactionBranchBankAccount(), txn.getNetAmount(), false);
							results.put("resultantAmount", resultantAmount);
						} else if (transactionPurposeId == IdosConstants.TRANSFER_FUNDS_FROM_ONE_BANK_TO_ANOTHER) {
							boolean branchBankDetailEntered = branchBankService.updateBranchBankDetailTransaction(
									entityManager, user, txn.getTransactionBranchBankAccount(), txn.getNetAmount(),
									true, results, txn.getTransactionDate(), txn.getTransactionBranch());
							if (!branchBankDetailEntered) {
								return Results.ok(results);
							}
							branchBankDetailEntered = branchBankService.updateBranchBankDetailTransaction(entityManager,
									user, txn.getTransactionToBranchBankAccount(), txn.getNetAmount(), false, results,
									txn.getTransactionDate(), txn.getTransactionBranch());
							if (!branchBankDetailEntered) {
								return Results.ok(results); // since balance is in -ve don't make any changes in DB
							}
							if (txn.getTransactionBranch().getId() != txn.getTransactionToBranch().getId()) {
								TRIAL_BALANCE_SERVICE.saveTrialBalInterBranch(txn, user,
										IdosConstants.HEAD_TYPE_OUTWORD, entityManager, true);
								TRIAL_BALANCE_SERVICE.saveTrialBalInterBranch(txn, user, IdosConstants.HEAD_TYPE_INWORD,
										entityManager, false);
							}
						}
						txn.setSupportingDocs(transactionDao.getAndDeleteSupportingDocument(txn.getSupportingDocs(),
								user.getEmail(), suppDoc, user, entityManager));
						if (txnRmarks != null && !txnRmarks.equals("")) {
							if (txn.getRemarks() != null) {
								txn.setRemarks(txn.getRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
							} else {
								txn.setRemarks(user.getEmail() + "#" + txnRmarks);
							}
						}
						TRIAL_BALANCE_SERVICE.insertTrialBalance(txn, user, entityManager);
					}
					genericDAO.saveOrUpdate(txn, user, entityManager);
				} else {
					genericDAO.saveOrUpdate(txn, user, entityManager);
				}
				// Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
				// Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
				// for (int i = 0; i < keyArray.length; i++) {
				// StringBuilder sbquery = new StringBuilder("");
				// sbquery.append(
				// "select obj from Users obj WHERE obj.email ='" + keyArray[i] + "' and
				// obj.presentStatus=1");
				// List<Users> orgusers = genericDAO.executeSimpleQuery(sbquery.toString(),
				// entityManager);
				// if (orgusers != null && !orgusers.isEmpty()
				// && orgusers.get(0).getOrganization().getId() ==
				// user.getOrganization().getId()) {
				// orgtxnregistereduser.put(keyArray[i].toString(),
				// CreatorActor.expenseregistrered.get(keyArray[i]));
				// }
				// }
				entityTransaction.commit();
				String branchName = "";
				String projectName = "";
				String itemName = "";
				String budgetAllocated = "";
				String budgetAvailable = "";
				String budgetAllocatedAmt = "";
				String budgetAvailableAmt = "";
				String customerVendorName = "";
				String txnDate = "";
				String paymentMode = "";
				Double noOfUnit = 0d;
				Double unitPrice = 0.0;
				Double grossAmount = 0.0;
				Double netAmount = 0.0;
				String netAmountDesc = "";
				String status = "";
				String createdBy = "";
				String txnRemarks = "";
				String useremail = "";
				String approverEmails = "";
				String additionalapproverEmails = "";
				String selectedAdditionalApproval = "";
				Double frieghtCharges = 0.0;
				String poReference = "";
				if (txn.getTransactionBranch() != null) {
					branchName = txn.getTransactionBranch().getName();
				}

				if (txn.getTransactionProject() != null) {
					projectName = txn.getTransactionProject().getName();
				}
				if (txn.getTransactionSpecifics() != null) {
					itemName = txn.getTransactionSpecifics().getName();
				}
				if (txn.getActualAllocatedBudget() != null) {
					budgetAllocated = txn.getActualAllocatedBudget();
				}
				if (txn.getBudgetAvailDuringTxn() != null) {
					budgetAvailable = String.valueOf(txn.getBudgetAvailDuringTxn());
				}
				if (txn.getTransactionVendorCustomer() != null) {
					customerVendorName = txn.getTransactionVendorCustomer().getName();
				} else {
					if (txn.getTransactionUnavailableVendorCustomer() != null) {
						customerVendorName = txn.getTransactionUnavailableVendorCustomer();
					}
				}
				if (txn.getNoOfUnits() != null) {
					noOfUnit = txn.getNoOfUnits();
				} else {
					noOfUnit = 0d;
				}
				if (txn.getPricePerUnit() != null) {
					unitPrice = txn.getPricePerUnit();
				} else {
					unitPrice = 0.0;
				}

				if (txn.getFrieghtCharges() != null) {
					frieghtCharges = txn.getFrieghtCharges();
				}
				if (txn.getGrossAmount() != null) {
					grossAmount = txn.getGrossAmount();
				} else {
					grossAmount = 0.0;
				}

				if (txn.getPoReference() != null) {
					poReference = txn.getPoReference();
				}
				txnDate = idosdf.format(txn.getTransactionDate());
				if (txn.getReceiptDetailsType() != null) {
					if (txn.getReceiptDetailsType() == IdosConstants.PAYMODE_CASH) {
						paymentMode = "CASH";
					} else if (txn.getReceiptDetailsType() == IdosConstants.PAYMODE_BANK) {
						paymentMode = "BANK";
					}
				}
				if (txn.getNetAmount() != null) {
					netAmount = txn.getNetAmount();
				}
				if (txn.getNetAmountResultDescription() != null
						&& !txn.getNetAmountResultDescription().equals("null")) {
					netAmountDesc = txn.getNetAmountResultDescription();
				}
				status = txn.getTransactionStatus();
				createdBy = txn.getCreatedBy().getEmail();
				if (txn.getRemarks() != null) {
					txnRemarks = txn.getRemarks();
				}
				useremail = user.getEmail();
				approverEmails = txn.getApproverEmails();
				additionalapproverEmails = txn.getAdditionalApproverEmails();
				if (txn.getSelectedAdditionalApprover() != null) {
					selectedAdditionalApproval = txn.getSelectedAdditionalApprover();
				}
				String invDate = "";
				String invoiceDateLabel = "";
				if (txn.getTransactionInvoiceDate() != null) {
					invoiceDateLabel = "INVOICE DATE:";
					invDate = idosdf.format(txn.getTransactionInvoiceDate());
				}
				String itemParentName = "";
				if (txn.getTransactionSpecifics() != null) {
					if (txn.getTransactionSpecifics().getParentSpecifics() != null
							&& !txn.getTransactionSpecifics().getParentSpecifics().equals("")) {
						itemParentName = txn.getTransactionSpecifics().getParentSpecifics().getName();
					} else {
						itemParentName = txn.getTransactionSpecifics().getParticularsId().getName();
					}
				}
				String approverEmail = "";
				String approverLabel = "";
				if (txn.getApproverActionBy() != null) {
					approverLabel = "APPROVER:";
					approverEmail = txn.getApproverActionBy().getEmail();
				}
				String txnSpecialStatus = "";
				if (txn.getTransactionExceedingBudget() != null && txn.getKlFollowStatus() != null) {
					if (txn.getTransactionExceedingBudget() == 1 && txn.getKlFollowStatus() == 0) {
						txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
					}
					if (txn.getTransactionExceedingBudget() == 1 && txn.getKlFollowStatus() == 1) {
						txnSpecialStatus = "Transaction Exceeding Budget";
					}
				}
				if (txn.getTransactionExceedingBudget() == null && txn.getKlFollowStatus() != null) {
					if (txn.getKlFollowStatus() == 0) {
						txnSpecialStatus = "Rules Not Followed";
					}
				}
				if (txn.getTransactionExceedingBudget() != null && txn.getKlFollowStatus() == null) {
					txnSpecialStatus = "Transaction Exceeding Budget";
				}
				String invoiceNumber = txn.getInvoiceNumber() == null ? "" : txn.getInvoiceNumber();
				String txnPurpose = txn.getTransactionPurpose().getTransactionPurpose();
				int txnIdentifier = 0;
				if (transactionPurposeId == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
					branchName += " to " + txn.getTransactionToBranch().getName();
					if (txn.getTypeIdentifier() == 1) {
						txnPurpose += "(Outward)";
						txnIdentifier = 1;
					} else {
						txnPurpose += "(Inward)";
						txnIdentifier = 2;
					}
				}

				int typeOfSupply = 0;
				if (txn.getTypeOfSupply() != null) {
					typeOfSupply = txn.getTypeOfSupply();
				}
				String txnDocument = txn.getSupportingDocs() == null ? "" : txn.getSupportingDocs();
				TransactionViewResponse.addActionTxn(txn.getId(), branchName, projectName, itemName, itemParentName,
						budgetAllocated, budgetAllocatedAmt, budgetAvailable, budgetAvailableAmt, customerVendorName,
						txnPurpose, txnDate, invoiceDateLabel, invDate, paymentMode, noOfUnit, unitPrice, grossAmount,
						netAmount, netAmountDesc, "", status, createdBy, approverLabel, approverEmail, txnDocument,
						txnRemarks, "", approverEmails, additionalapproverEmails, selectedAdditionalApproval,
						txnSpecialStatus, frieghtCharges, poReference, txnInstrumentNumber,
						txnInstrumentDate, transactionPurposeId, "", invoiceNumber, txnIdentifier,
						txn.getTransactionRefNumber(), 0l, 0.0, 0, typeOfSupply, results);

				if (transactionPurposeId == 12L && selectedApproverAction.equals("4")) {
					criterias.clear();
					criterias.put("organization.id", txn.getTransactionBranchOrganization().getId());
					criterias.put("vendorSpecific.id", txn.getTransactionVendorCustomer().getId());
					criterias.put("specificsVendors.id", txn.getTransactionSpecifics().getId());
					criterias.put("presentStatus", 1);
					VendorSpecific vendSpecf = genericDAO.getByCriteria(VendorSpecific.class, criterias, entityManager);
					if (vendSpecf.getAdvanceMoney() != null) {
						vendSpecf.setAdvanceMoney(vendSpecf.getAdvanceMoney() + txn.getNetAmount());
					} else {
						vendSpecf.setAdvanceMoney(txn.getNetAmount());
					}
					genericDAO.saveOrUpdate(vendSpecf, user, entityManager);
					criterias.clear();
					criterias.put("transactionRefNumber", txn.getPaidInvoiceRefNumber());
					criterias.put("presentStatus", 1);
					Transaction previousTransaction = genericDAO.getByCriteria(Transaction.class, criterias,
							entityManager);
					String currentDate = idosdf.format(Calendar.getInstance().getTime());
					String invoiceDate = "";
					if (previousTransaction.getTransactionDate() != null) {
						invoiceDate = idosdf.format(previousTransaction.getTransactionDate());
					}
					if (previousTransaction.getTransactionInvoiceDate() != null) {
						invoiceDate = idosdf.format(previousTransaction.getTransactionInvoiceDate());
					}
					String body = creditNote
							.render(txn, currentDate, previousTransaction, invoiceDate, ConfigParams.getInstance())
							.body();
					final String username = ConfigFactory.load().getString("smtp.user");
					Session session = emailsession;
					String subject = "Credit Note";
					mailTimer(body, username, session, txn.getTransactionVendorCustomer().getEmail(), null, subject);
				}
				if (transactionPurposeId == 13L && selectedApproverAction.equals("4")) {
					criterias.clear();
					criterias.put("organization.id", txn.getTransactionBranchOrganization().getId());
					criterias.put("vendorSpecific.id", txn.getTransactionVendorCustomer().getId());
					criterias.put("specificsVendors.id", txn.getTransactionSpecifics().getId());
					criterias.put("presentStatus", 1);
					VendorSpecific vendSpecf = genericDAO.getByCriteria(VendorSpecific.class, criterias, entityManager);
					if (vendSpecf.getAdvanceMoney() != null) {
						vendSpecf.setAdvanceMoney(vendSpecf.getAdvanceMoney() + txn.getNetAmount());
					} else {
						vendSpecf.setAdvanceMoney(txn.getNetAmount());
					}
					genericDAO.saveOrUpdate(vendSpecf, user, entityManager);
					criterias.clear();
					criterias.put("transactionRefNumber", txn.getPaidInvoiceRefNumber());
					criterias.put("presentStatus", 1);
					Transaction previousTransaction = genericDAO.getByCriteria(Transaction.class, criterias,
							entityManager);
					String currentDate = idosdf.format(Calendar.getInstance().getTime());
					String invoiceDate = "";
					if (previousTransaction.getTransactionDate() != null) {
						invoiceDate = idosdf.format(previousTransaction.getTransactionDate());
					}
					if (previousTransaction.getTransactionInvoiceDate() != null) {
						invoiceDate = idosdf.format(previousTransaction.getTransactionInvoiceDate());
					}
					String body = debitNote
							.render(txn, currentDate, previousTransaction, invoiceDate, ConfigParams.getInstance())
							.body();
					final String username = ConfigFactory.load().getString("smtp.user");
					Session session = emailsession;
					String subject = "Debit Note";
					mailTimer(body, username, session, txn.getTransactionVendorCustomer().getEmail(), null, subject);
				}
				// Single User
				if (isSingleUserDeploy && selectedApproverAction.equals("1")) {
					selectedApproverAction = "4"; // for Complete accounting
				} else {
					selectedApproverAction = "1"; // for break point
				}
			} while (isSingleUserDeploy && selectedApproverAction.equals("4"));
			if (txn != null) {
				results.put(TRANSACTION_ID, txn.getId());
				results.put(TRANSACTION_REF_NO, txn.getTransactionRefNumber());
			}
		} catch (Exception ex) {
			reportException(entityManager, entityTransaction, user, ex, results);
		} catch (Throwable th) {
			reportThrowable(entityManager, entityTransaction, user, th, results);
		}
		log.log(Level.FINE, ">>>> End ");
		return Results.ok(results);
	}

	@Transactional
	public Result approverCashBankReceivablePayables(Http.Request request) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode cashBankRecivablesPayablesan = result.putArray("cashBankRecivablesPayablesData");
		JsonNode json = request.body().asJson();
		// String useremail=json.findValue("usermail").asText();
		// session.adding("email", useremail);
		Users user = getUserInfo(request);
		if (user == null) {
			return unauthorized();
		}
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			ObjectNode row = transactionService.approverCashBankReceivablePayables(user, entityManager);
			cashBankRecivablesPayablesan.add(row);
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> end " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result branchWiseApproverCashBankReceivablePayables(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		JsonNode json = request.body().asJson();
		String tabElement = json.findValue("tabElement").asText();
		String useremail = json.findValue("usermail").asText();
		session.adding("email", useremail);
		Users user = getUserInfo(request);
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			result = transactionService.branchWiseApproverCashBankReceivablePayables(user, entityManager, tabElement);
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result wightedAverageForTransaction(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		JsonNode json = request.body().asJson();
		String transactionId = json.findValue("transactionId").asText();
		String period = json.findValue("period").asText();
		Transaction transaction = Transaction.findById(Long.parseLong(transactionId));
		String useremail = json.findValue("usermail").asText();
		session.adding("email", useremail);
		Users user = getUserInfo(request);
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			result = transactionService.wightedAverageForTransaction(user, transaction, period, entityManager);
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		return Results.ok(result);
	}

	@Transactional
	public Result documentRule(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		JsonNode json = request.body().asJson();
		String useremail = json.findValue("useremail").asText();
		session.adding("email", useremail);
		String txnForExpItem = json.findValue("txnForExpItem").asText();
		String txnForExpBranch = json.findValue("txnForExpBranch").asText();
		String txnForExpNetAmount = json.findValue("txnForExpNetAmount").asText();
		Users user = getUserInfo(request);
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			result = transactionService.documentRule(user, txnForExpItem, txnForExpBranch, txnForExpNetAmount);
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		return Results.ok(result);
	}

	@Transactional
	public Result documentRulePVS(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		JsonNode json = request.body().asJson();
		String useremail = json.findValue("useremail").asText();
		session.adding("email", useremail);
		String txnInv = json.findValue("txnInv").asText();
		String txnpaymentReceived = json.findValue("txnpaymentReceived").asText();
		Users user = getUserInfo(request);
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			result = transactionService.documentRulePVS(user, txnInv, txnpaymentReceived, entityManager);
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		return Results.ok(result);
	}

	@Transactional
	public Result accountHeadTransactions(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			entitytransaction.begin();
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result = transactionService.accountHeadTransactions(result, json, user, entityManager,
						entitytransaction);
			}
			entitytransaction.commit();
		} catch (Exception ex) {
			reportException(entityManager, entitytransaction, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, entitytransaction, user, th, result);
		}
		return Results.ok(result);
	}

	@Transactional
	public Result checkMaxDiscountForWalkinCust(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			entitytransaction.begin();
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result = transactionService.checkMaxDiscountForWalkinCust(result, json, user, entityManager);
			}
			entitytransaction.commit();
		} catch (Exception ex) {
			reportException(entityManager, entitytransaction, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, entitytransaction, user, th, result);
		}
		return Results.ok(result);
	}

	@Transactional
	public Result showTransactionDetails(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		ObjectNode results = Json.newObject();
		Transaction txn = null;
		try {
			JsonNode json = request.body().asJson();

			ArrayNode txndetailan = results.putArray("transactiondetailsData");
			ArrayNode txnItemsan = results.putArray("transactionItemdetailsData");

			String transactionEntityId = json.findValue("transactionEntityId").asText();
			txn = Transaction.findById(IdosUtil.convertStringToLong(transactionEntityId));
			if (txn != null) {
				ObjectNode row = Json.newObject();
				row.put("id", txn.getId());
				row.put("transactionPurpose", txn.getTransactionPurpose().getTransactionPurpose());
				row.put("transactionPurposeVal", txn.getTransactionPurpose().getId());
				row.put("noOfUnits", txn.getNoOfUnits());
				row.put("pricePerUnit", txn.getPricePerUnit());
				if (txn.getFrieghtCharges() != null)
					row.put("frieghtCharges", txn.getFrieghtCharges());
				else
					row.put("frieghtCharges", "");
				if (txn.getGrossAmount() != null)
					row.put("grossAmount", IdosConstants.DECIMAL_FORMAT.format(txn.getGrossAmount()));
				else
					row.put("grossAmount", "");
				row.put("transactionDate", idosdf.format(txn.getTransactionDate()));
				if (txn.getTransactionBranch() != null) {
					row.put("branchName", txn.getTransactionBranch().getName());
					row.put("branchId", txn.getTransactionBranch().getId());
				} else {
					row.put("branchName", "");
					row.put("branchId", "");
				}
				if (txn.getTransactionProject() != null) {
					row.put("projectName", txn.getTransactionProject().getName());
				} else {
					row.put("projectName", "");
				}
				if (txn.getTransactionProject() != null) {
					row.put("projectID", txn.getTransactionProject().getId());
				} else {
					row.put("projectID", "");
				}

				row.put("poReference", txn.getPoReference() == null ? "" : txn.getPoReference());
				row.put("advanceType", txn.getAdvanceType() == null ? 0 : txn.getAdvanceType());
				row.put("netAmount",
						txn.getNetAmount() == null ? "" : IdosConstants.decimalFormat.format(txn.getNetAmount()));
				row.put("remarks", txn.getRemarks() == null ? "" : txn.getRemarks());
				row.put("remarksPrivate", txn.getRemarksPrivate() == null ? "" : txn.getRemarksPrivate());
				row.put("supportingDocs", txn.getSupportingDocs() == null ? "" : txn.getSupportingDocs());

				row.put("createdBy", txn.getCreatedBy().getEmail());
				if (txn.getTransactionSpecifics() != null) {
					row.put("itemName", txn.getTransactionSpecifics().getName());
					row.put("itemId", txn.getTransactionSpecifics().getId());
				} else {
					row.put("itemName", "");
					row.put("itemId", "");
				}
				if (txn.getTransactionSpecifics() != null) {
					if (txn.getTransactionSpecifics().getParentSpecifics() != null
							&& !txn.getTransactionSpecifics().getParentSpecifics().equals("")) {
						row.put("itemParentName", txn.getTransactionSpecifics().getParentSpecifics().getName());
					} else {
						row.put("itemParentName", txn.getTransactionSpecifics().getParticularsId().getName());
					}
				} else {
					row.put("itemParentName", "");
				}
				if (txn.getTransactionVendorCustomer() != null) {
					row.put("customerVendorName", txn.getTransactionVendorCustomer().getName());
					row.put("customerVendorId", txn.getTransactionVendorCustomer().getId());
				} else {
					if (txn.getTransactionUnavailableVendorCustomer() != null) {
						row.put("customerVendorName", txn.getTransactionUnavailableVendorCustomer());
						row.put("customerVendorId", "");
					} else {
						row.put("customerVendorName", "");
						row.put("customerVendorId", "");
					}
				}

				row.put("adjustmentFromAdvance",
						txn.getAdjustmentFromAdvance() == null ? 0.0 : txn.getAdjustmentFromAdvance());
				row.put("netAmountResultDescription",
						txn.getNetAmountResultDescription() == null ? "" : txn.getNetAmountResultDescription());
				row.put("withholdingTax", txn.getWithholdingTax() == null ? 0.0 : txn.getWithholdingTax());

				if (txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) {
					row.put("isitemallowsinputtaxes", 1);
					row.put("taxName1", txn.getTaxName1() == null ? "" : txn.getTaxName1());
					row.put("taxValue1", txn.getTaxValue1());
					row.put("taxName2", txn.getTaxName2() == null ? "" : txn.getTaxName2());
					row.put("taxValue2", txn.getTaxValue2());
					row.put("taxName3", txn.getTaxName3() == null ? "" : txn.getTaxName3());
					row.put("taxValue3", txn.getTaxValue3());
					row.put("taxName4", txn.getTaxName4() == null ? "" : txn.getTaxName4());
					row.put("taxValue4", txn.getTaxValue4());
					row.put("taxName5", txn.getTaxName5() == null ? "" : txn.getTaxName5());
					row.put("taxValue5", txn.getTaxValue5());
					row.put("taxName6", txn.getTaxName6() == null ? "" : txn.getTaxName6());
					row.put("taxValue6", txn.getTaxValue6());
					row.put("taxName7", txn.getTaxName7() == null ? "" : txn.getTaxName7());
					row.put("taxValue7", txn.getTaxValue7());
					BigDecimal totalInputTaxes = BigDecimal
							.valueOf((txn.getTaxValue1() == null ? 0.0 : txn.getTaxValue1())
									+ (txn.getTaxValue2() == null ? 0.0 : txn.getTaxValue2())
									+ (txn.getTaxValue3() == null ? 0.0 : txn.getTaxValue3())
									+ (txn.getTaxValue4() == null ? 0.0 : txn.getTaxValue4())
									+ (txn.getTaxValue5() == null ? 0.0 : txn.getTaxValue5())
									+ (txn.getTaxValue6() == null ? 0.0 : txn.getTaxValue6())
									+ (txn.getTaxValue7() == null ? 0.0 : txn.getTaxValue7()));

					row.put("totalInputTax", totalInputTaxes);
				} else if (txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
						|| txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CREDIT_PAY_LATER
						|| IdosConstants.PREPARE_QUOTATION == txn.getTransactionPurpose().getId()
						|| IdosConstants.PROFORMA_INVOICE == txn.getTransactionPurpose().getId()
						|| IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW == txn.getTransactionPurpose().getId()
						|| IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER == txn.getTransactionPurpose().getId()) {
					Map<String, Object> criterias = new HashMap<String, Object>();
					criterias.put("transaction.id", IdosUtil.convertStringToLong(transactionEntityId));
					criterias.put("presentStatus", 1);
					List<TransactionItems> listTransactionItems = genericDAO.findByCriteria(TransactionItems.class,
							criterias, entityManager);
					for (TransactionItems txnItemrow : listTransactionItems) {
						ObjectNode itemRow = Json.newObject();
						itemRow.put("id", txnItemrow.getId());
						itemRow.put("noOfUnits", txnItemrow.getNoOfUnits());
						itemRow.put("pricePerUnit", txnItemrow.getPricePerUnit());
						itemRow.put("discountPer", txnItemrow.getDiscountPercent());
						itemRow.put("discountAmt", txnItemrow.getDiscountAmount());
						itemRow.put("grossAmount", IdosConstants.DECIMAL_FORMAT.format(txnItemrow.getGrossAmount()));
						if (txnItemrow.getTransactionSpecifics() != null) {
							itemRow.put("itemName", txnItemrow.getTransactionSpecifics().getName());
							itemRow.put("itemId", txnItemrow.getTransactionSpecifics().getId());
						} else {
							itemRow.put("itemName", "");
							itemRow.put("itemId", "");
						}
						itemRow.put("taxDescription", txnItemrow.getTaxDescription());
						itemRow.put("totalInputTax", txnItemrow.getTotalTax());
						itemRow.put("withholdingAmount", txnItemrow.getWithholdingAmount());
						itemRow.put("availableAdvance", txnItemrow.getAvailableAdvance());
						itemRow.put("adjFromAdvance", txnItemrow.getAdjustmentFromAdvance());
						itemRow.put("netAmount", txnItemrow.getNetAmount() == null ? ""
								: IdosConstants.DECIMAL_FORMAT.format(txnItemrow.getNetAmount()));
						txnItemsan.add(itemRow);
					}
				}
				txndetailan.add(row);
			}
		} catch (Exception ex) {
			reportException(entityManager, null, null, ex, results);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, null, th, results);
		}
		log.log(Level.FINE, ">>>> End " + results);
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	// Resubmit Editted transaction using "Edit Transaction". This is available only
	// for creator where status="approved"/"Require Approval"
	// allowed for Buy on cash,Buy on credit,Pay Vendor / supplier,Pay advance to
	// Vendor / supplier
	// This function is used only for Buy on cash and buy on credit. Here we can not
	// use submitForApproval() as it needs lot of details of transaction which are
	// not available with us
	// For pay vendor/ pay advance to vendor using submitForApproval() only..
	@Transactional
	public Result resubmitForApproval(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		Users user = null;
		try {
			entitytransaction.begin();
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start " + json);
			String useremail = json.findValue("useremail").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			results.put("useremail", user.getEmail());
			String role = "";
			List<UsersRoles> userRoles = UsersRoles.getUserRoleList(entityManager, user.getOrganization().getId(),
					user.getId(),
					user.getBranch().getId());
			for (UsersRoles roles : userRoles) {
				role += roles.getRole().getName() + ",";
			}
			role = role.substring(0, role.length() - 1);
			results.put("role", role);
			String txnPurpose = json.findValue("txnPurpose").asText();
			String txnPurposeVal = json.findValue("txnPurposeVal").asText();
			String txnwithholdingTaxValues = json.findValue("txnwithholdingTaxValues") == null ? null
					: json.findValue("txnwithholdingTaxValues").asText();
			/*
			 * if(txnPurpose.equals("Buy on cash & pay right away") ||
			 * txnPurpose.equals("Buy on credit & pay later")){ String transactionEntityId =
			 * json.findValue("transactionEntityId").asText(); String
			 * txnforcustomer=json.findValue("txnForCustomer").asText(); String
			 * txnNoOfUnits=json.findValue("txnnoofunits").asText(); String
			 * txnpriceperunit=json.findValue("txnpriceperunit").asText(); String
			 * txnFrieghtCharges=json.findValue("txnFrieghtCharges") == null ? null :
			 * json.findValue("txnFrieghtCharges").asText(); String
			 * txngross=json.findValue("txngross").asText(); String
			 * txnremarks=json.findValue("txnRemarks").asText(); String
			 * txnnetamount=json.findValue("txnnetamount").asText(); String
			 * txnnetamountdescription=json.findValue("txnnetamountdescription").asText();
			 * String
			 * txncustomeradvanceifany=json.findValue("txnCustomerAdvanceIfAny").asText();
			 * String
			 * txncustomeradvanceadjustment=json.findValue("txnCustomerAdvanceAdjustment").
			 * asText(); String
			 * txnInputTaxesNames=json.findValue("txnInputTaxesNames").asText(); String
			 * txnInputTaxesValues=json.findValue("txnInputTaxesValues").asText();
			 * Transaction txn=Transaction.findById(Long.parseLong(transactionEntityId));
			 * if(txn!=null){ Vendor txncustomer=null; String customerVendorName=null;
			 * if(txnforcustomer!=null && !txnforcustomer.equals("")){
			 * txncustomer=genericDAO.getById(Vendor.class, Long.parseLong(txnforcustomer),
			 * entityManager); customerVendorName=txncustomer.getName(); }
			 * txn.setTransactionVendorCustomer(txncustomer);
			 * txn.setNoOfUnits(Double.parseDouble(txnNoOfUnits)); if(txnFrieghtCharges !=
			 * null) txn.setFrieghtCharges(Double.parseDouble(txnFrieghtCharges)); else
			 * txn.setFrieghtCharges(null); if(txnpriceperunit != null &&
			 * !"".equals(txnpriceperunit)) {
			 * txn.setPricePerUnit(Double.parseDouble(txnpriceperunit)); } if(txngross !=
			 * null && !"".equals(txngross)) {
			 * txn.setGrossAmount(Double.parseDouble(txngross)); } if(!txnremarks.equals("")
			 * && txnremarks!=null){ String txnRemarks=txnremarks+
			 * " This transaction is edited on "+mysqldtf.format(Calendar.getInstance().
			 * getTime()); txn.setRemarks(txnRemarks); }
			 * txn.setNetAmount(Double.parseDouble(txnnetamount));
			 * txn.setNetAmountResultDescription(txnnetamountdescription); //advance
			 * adjustment, sometimes when editing initial value for advance is set but new
			 * is null then don't go and deduct adv money
			 * if(!txncustomeradvanceifany.equals("") && txncustomeradvanceifany!=null){
			 * txn.setAvailableAdvance(Double.parseDouble(txncustomeradvanceifany)); }
			 * if(!txncustomeradvanceadjustment.equals("") &&
			 * txncustomeradvanceadjustment!=null){
			 * txn.setAdjustmentFromAdvance(Double.parseDouble(txncustomeradvanceadjustment)
			 * ); } else{ txn.setAdjustmentFromAdvance(null); } String
			 * netAmtInputTaxDesc=""; if(txnInputTaxesValues!=null &&
			 * !txnInputTaxesValues.equals("")){ String
			 * inputtaxnamearr[]=txnInputTaxesNames.split(","); String
			 * inputtaxvalarr[]=txnInputTaxesValues.split(","); for(int
			 * i=0;i<inputtaxvalarr.length;i++){ switch(i){ case
			 * 0:if(inputtaxvalarr.length>i){
			 * netAmtInputTaxDesc+=","+inputtaxnamearr[i]+":"+inputtaxvalarr[i];
			 * txn.setTaxName1(inputtaxnamearr[i]);
			 * txn.setTaxValue1(Double.parseDouble(inputtaxvalarr[i])); } break; case
			 * 1:if(inputtaxvalarr.length>i){
			 * netAmtInputTaxDesc+=","+inputtaxnamearr[i]+":"+inputtaxvalarr[i];
			 * txn.setTaxName2(inputtaxnamearr[i]);
			 * txn.setTaxValue2(Double.parseDouble(inputtaxvalarr[i])); } break; case
			 * 2:if(inputtaxvalarr.length>i){
			 * netAmtInputTaxDesc+=","+inputtaxnamearr[i]+":"+inputtaxvalarr[i];
			 * txn.setTaxName3(inputtaxnamearr[i]);
			 * txn.setTaxValue3(Double.parseDouble(inputtaxvalarr[i])); } break; case
			 * 3:if(inputtaxvalarr.length>i){
			 * netAmtInputTaxDesc+=","+inputtaxnamearr[i]+":"+inputtaxvalarr[i];
			 * txn.setTaxName4(inputtaxnamearr[i]);
			 * txn.setTaxValue4(Double.parseDouble(inputtaxvalarr[i])); } break; case
			 * 4:if(inputtaxvalarr.length>i){
			 * netAmtInputTaxDesc+=","+inputtaxnamearr[i]+":"+inputtaxvalarr[i];
			 * txn.setTaxName5(inputtaxnamearr[i]);
			 * txn.setTaxValue5(Double.parseDouble(inputtaxvalarr[i])); } break; case
			 * 5:if(inputtaxvalarr.length>i){
			 * netAmtInputTaxDesc+=","+inputtaxnamearr[i]+":"+inputtaxvalarr[i];
			 * txn.setTaxName6(inputtaxnamearr[i]);
			 * txn.setTaxValue6(Double.parseDouble(inputtaxvalarr[i])); } break; case
			 * 6:if(inputtaxvalarr.length>i){
			 * netAmtInputTaxDesc+=","+inputtaxnamearr[i]+":"+inputtaxvalarr[i];
			 * txn.setTaxName7(inputtaxnamearr[i]);
			 * txn.setTaxValue7(Double.parseDouble(inputtaxvalarr[i])); } break; } } }
			 * txn.setNetAmountResultDescription(txn.getNetAmountResultDescription()+
			 * netAmtInputTaxDesc); if(txnwithholdingTaxValues != null &&
			 * !"".equals(txnwithholdingTaxValues))
			 * txn.setWithholdingTax(Double.parseDouble(txnwithholdingTaxValues)); else
			 * txn.setWithholdingTax(null); genericDAO.saveOrUpdate(txn, user,
			 * entityManager);
			 * 
			 * } }
			 */
			if (txnPurpose.equals("Pay vendor/supplier")) {
				String transactionEntityId = json.findValue("transactionEntityId").asText();
				String creditMCPFCVVendor = json.findValue("creditMCPFCVVendor").asText();// credit vendor
				String txnMCPFCVoutstandings = json.findValue("txnMCPFCVoutstandings").asText();// net amount
																								// description which is
																								// outstandings here
				String txnMCPFCVInvoice = json.findValue("txnMCPFCVInvoice").asText();// transaction for which payment
																						// to vendor
				String txnMCPFCVVendorAdvanceIfAny = json.findValue("txnMCPFCVVendorAdvanceIfAny").asText();// advance
																											// if any
				String txnMCPFCVVendorAdvanceAdjustment = json.findValue("txnMCPFCVVendorAdvanceAdjustment").asText();
				String txnMCPFCVpaymentReceived = json.findValue("txnMCPFCVpaymentReceived").asText();
				String txnMCPFCVpaymentDue = json.findValue("txnMCPFCVpaymentDue").asText();
				String txnactPayToVend = json.findValue("txnactPayToVend").asText();
				String txnremarks = json.findValue("txnremarks").asText();
				Transaction transaction = Transaction.findById(IdosUtil.convertStringToLong(transactionEntityId));
				if (transaction != null) {
					Transaction pendingTransaction = Transaction
							.findById(IdosUtil.convertStringToLong(txnMCPFCVInvoice)); // we are paying vendor for this
																						// transaction, so need to copy
																						// branch/bank etc of this
																						// original transaction
					Vendor txnVendor = Vendor.findById(IdosUtil.convertStringToLong(creditMCPFCVVendor));
					String branchName = "";
					String paymentMode = "";
					String projectName = "";
					String itemName = "";
					String customerVendorName = "";
					String debitCredit = "Credit";
					String txnRemarks = "";
					branchName = pendingTransaction.getTransactionBranch().getName();
					transaction.setTransactionBranch(pendingTransaction.getTransactionBranch());
					if (pendingTransaction.getTransactionProject() != null) {
						projectName = pendingTransaction.getTransactionProject().getName();
						transaction.setTransactionProject(pendingTransaction.getTransactionProject());
					}
					itemName = pendingTransaction.getTransactionSpecifics().getName();
					transaction.setTransactionSpecifics(pendingTransaction.getTransactionSpecifics());
					transaction.setBudgetAvailDuringTxn(pendingTransaction.getBudgetAvailDuringTxn());
					transaction.setActualAllocatedBudget(pendingTransaction.getActualAllocatedBudget());
					transaction.setUserTxnLimitDesc(pendingTransaction.getUserTxnLimitDesc());
					customerVendorName = txnVendor.getName();
					transaction.setTransactionVendorCustomer(txnVendor);
					transaction.setTransactionBranchOrganization(
							pendingTransaction.getTransactionBranch().getOrganization());
					transaction
							.setTransactionParticulars(pendingTransaction.getTransactionSpecifics().getParticularsId());
					transaction.setNoOfUnits(pendingTransaction.getNoOfUnits());
					transaction.setPricePerUnit(pendingTransaction.getPricePerUnit());
					transaction.setGrossAmount(pendingTransaction.getGrossAmount());
					transaction.setPaidInvoiceRefNumber(pendingTransaction.getTransactionRefNumber());
					Double advanceIfAnyForAdjustment = null;
					Double advanceAdjustment = null;
					if (!txnMCPFCVVendorAdvanceIfAny.equals("") && !txnMCPFCVVendorAdvanceAdjustment.equals("")) {
						advanceIfAnyForAdjustment = IdosUtil.convertStringToDouble(txnMCPFCVVendorAdvanceIfAny);
						advanceAdjustment = IdosUtil.convertStringToDouble(txnMCPFCVVendorAdvanceAdjustment);
					} else {
						advanceIfAnyForAdjustment = 0.0;
						advanceAdjustment = 0.0;
					}
					transaction.setAvailableAdvance(advanceIfAnyForAdjustment);
					transaction.setAdjustmentFromAdvance(advanceAdjustment);
					transaction.setNetAmount(IdosUtil.convertStringToDouble(txnMCPFCVpaymentReceived));
					String netDesc = "Payment Made:" + txnMCPFCVpaymentReceived + ",Advance Adjustment:"
							+ advanceAdjustment + ",Due Balance:" + txnMCPFCVpaymentDue;
					transaction.setNetAmountResultDescription(netDesc);
					if (txnactPayToVend != null && !txnactPayToVend.equals("")) {
						transaction.setNetAmountResultDescription(
								transaction.getNetAmountResultDescription() + "," + txnactPayToVend);
						String txnActPayVend[] = txnactPayToVend.split(",");
						if (txnActPayVend.length > 1) {
							String txnActPayVendwithcolon[] = txnActPayVend[1].split(":");
							transaction.setWithholdingTax(IdosUtil.convertStringToDouble(txnActPayVendwithcolon[1]));
						}
					}
					transaction.setTransactionDate(Calendar.getInstance().getTime());
					if (!txnremarks.equals("") && txnremarks != null) {
						txnRemarks = txnremarks + " This transaction is edited on " + Calendar.getInstance().getTime();
						transaction.setRemarks(txnRemarks);
						txnRemarks = transaction.getRemarks();
					}
					genericDAO.saveOrUpdate(transaction, user, entityManager);
				}
			}
			if (txnPurpose.equals("Pay advance to vendor or supplier")) {
				String transactionEntityId = json.findValue("transactionEntityId").asText();
				String txnPCAFCVExpenseItem = json.findValue("txnPCAFCVExpenseItem").asText();
				String txnPCAFCVCreditVendor = json.findValue("txnPCAFCVCreditVendor").asText();
				String txnPCAFCVPurposeOfAdvance = json.findValue("txnPCAFCVPurposeOfAdvance").asText();
				String txnPCAFCVAmountOfAdvance = json.findValue("txnPCAFCVAmountOfAdvance").asText();
				String txnremarks = json.findValue("txnremarks").asText();
				Transaction transaction = Transaction.findById(IdosUtil.convertStringToLong(transactionEntityId));
				if (transaction != null) {
					String txnRemarks = "";
					String itemName = "";
					String customerVendorName = "";
					Specifics expenseSpecf = Specifics.findById(IdosUtil.convertStringToLong(txnPCAFCVExpenseItem));
					Vendor vendorForAdvance = Vendor.findById(IdosUtil.convertStringToLong(txnPCAFCVCreditVendor));
					itemName = expenseSpecf.getName();
					customerVendorName = vendorForAdvance.getName();
					transaction.setTransactionSpecifics(expenseSpecf);
					transaction.setTransactionParticulars(expenseSpecf.getParticularsId());
					transaction.setTransactionVendorCustomer(vendorForAdvance);
					transaction.setNetAmount(IdosUtil.convertStringToDouble(txnPCAFCVAmountOfAdvance));
					String purpOfAdvance = "Advance Paid For Purpose";
					if (!txnPCAFCVPurposeOfAdvance.equals("") && txnPCAFCVPurposeOfAdvance != null) {
						transaction.setNetAmountResultDescription(purpOfAdvance + ":" + txnPCAFCVPurposeOfAdvance);
					}
					if (!txnremarks.equals("") && txnremarks != null) {
						txnRemarks = txnremarks + "# This transaction is edited on " + Calendar.getInstance().getTime();
						transaction.setRemarks(txnRemarks);
						txnRemarks = transaction.getRemarks();
					}

					if (txnwithholdingTaxValues != null && !"".equals(txnwithholdingTaxValues))
						transaction.setWithholdingTax(IdosUtil.convertStringToDouble(txnwithholdingTaxValues));
					else
						transaction.setWithholdingTax(null);

					genericDAO.saveOrUpdate(transaction, user, entityManager);
				}
			}
			entitytransaction.commit();
		} catch (Exception ex) {
			reportException(entityManager, entitytransaction, user, ex, results);
		} catch (Throwable th) {
			reportThrowable(entityManager, entitytransaction, user, th, results);
		}
		log.log(Level.FINE, ">>>> End " + results);
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result branchCustomerVendorReceivablePayables(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			if (user != null) {
				result = transactionService.branchCustomerVendorReceivablePayables(result, json, user, entityManager);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result overUnderOneEightyReceivablePayablesTxn(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			entitytransaction.begin();
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			if (null != user) {
				result = transactionService.overUnderOneEightyReceivablePayablesTxn(result, json, user, entityManager,
						entitytransaction);
			}
			entitytransaction.commit();
		} catch (Exception ex) {
			reportException(entityManager, entitytransaction, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, entitytransaction, user, th, result);
		}
		log.log(Level.FINE, ">>>> End");
		return Results.ok(result);
	}

	@Transactional
	public Result downloadOverUnderOneEightyDayaTxnExcel(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		File file = null;
		String fname;
		try {
			entitytransaction.begin();
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			user = getUserInfo(request);
			if (null == user) {
				return unauthorized();
			}
			String path = application.path().toString() + "/logs/BudgetExcel/";
			File filePath = new File(path);
			if (!filePath.exists()) {
				filePath.mkdir();
			}
			fname = transactionService.downloadOverUnderOneEightyDayaTxnExcel(result, json, user, entityManager,
					entitytransaction, path);
			if (fname != null) {
				file = new File(path + fname);
			}

			entitytransaction.commit();
			return Results.ok(file).withHeader("ContentType", "application/xlsx").withHeader("Content-Disposition",
					"attachment; filename=" + fname);

		} catch (Exception ex) {
			reportException(entityManager, entitytransaction, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, entitytransaction, user, th, result);
		}
		return null;
	}

	@Transactional
	public Result getInputTaxesListOnBranch(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		ArrayNode branchInputTaxList = result.putArray("inputTaxList");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			String txnPurposeId = json.findValue("txnPurposeId").asText();
			String txnBranchId = json.findValue("txnBranchId").asText();
			Long txnItemTableIdHid = (json.findValue("txnItemTableIdHid") != null
					|| !json.findValue("txnItemTableIdHid").equals("")) ? json.findValue("txnItemTableIdHid").asLong()
							: 0l;
			session.adding("email", useremail);
			user = getUserInfo(request);

			if (txnItemTableIdHid != 0) { // for purchase returns show taxes already paid when buy on credit
				TransactionItems txnItemrow = TransactionItems.findById(txnItemTableIdHid);
				if (txnItemrow != null) {
					String taxDesc = txnItemrow.getTaxDescription();
					if (taxDesc != null && !taxDesc.equals("")) {
						String[] taxes = taxDesc.split(",");
						for (int i = 0; i < taxes.length; i++) {
							String individualTax[] = taxes[i].split("=");
							ObjectNode row = Json.newObject();
							row.put("inputTaxID", individualTax[0]);
							row.put("inputTaxName", individualTax[0]);
							row.put("inputTaxValue", individualTax[1]);
							branchInputTaxList.add(row);
						}
					}
				}
			} else {
				criterias.clear();
				criterias.put("branch.id", IdosUtil.convertStringToLong(txnBranchId));
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("taxType", 1);
				criterias.put("presentStatus", 1);
				List<BranchTaxes> inputTaxesList = genericDAO.findByCriteria(BranchTaxes.class, criterias,
						entityManager);
				for (BranchTaxes inputTax : inputTaxesList) {
					ObjectNode row = Json.newObject();
					if (inputTax != null) {
						row.put("inputTaxID", inputTax.getId());
						row.put("inputTaxName", inputTax.getTaxName());
						row.put("inputTaxValue", 0);
						branchInputTaxList.add(row);
					}
				}
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		log.log(Level.FINE, ">>>> end " + result);
		return Results.ok(result);
	}

	public static void updateWithholdingForLeftOutTrans(String txnLeftOutWithholdTransIDs, Users user,
			EntityManager entityManager) throws IDOSException {
		Map<String, Object> criterias = new HashMap<String, Object>(1);
		String transIDarr[] = txnLeftOutWithholdTransIDs.split(",");
		for (int i = 0; i < transIDarr.length; i++) {
			String transID = transIDarr[i];
			if (transID != null && !"".equals(transID)) {
				criterias.clear();
				criterias.put("id", IdosUtil.convertStringToLong(transID));
				criterias.put("presentStatus", 1);
				Transaction transaction = genericDAO.getByCriteria(Transaction.class, criterias, entityManager);
				transaction.setWithholdingTax(0.0);
				genericDAO.saveOrUpdate(transaction, user, entityManager);
			} else {
				continue;
			}
		}
	}

	@Transactional
	public Result fetchItemsForBranchProject(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode gettxnan = result.putArray("allTxnPurposeItemsData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			long txnPurposeId = json.findValue("txnPurposeId").asLong();
			long txnPurposeBnchId = json.findValue("txnPurposeBnchId").asLong();
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			// criterias.put("branch.id", txnPurposeBnchId);
			criterias.put("transactionPurpose.id", txnPurposeId);
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("presentStatus", 1);
			List<SpecificsTransactionPurpose> txnPurposeSpecifics = genericDAO
					.findByCriteria(SpecificsTransactionPurpose.class, criterias, entityManager);
			for (SpecificsTransactionPurpose txnPurposeSpecf : txnPurposeSpecifics) {
				ObjectNode row = Json.newObject();
				criterias.clear();
				criterias.put("user.id", user.getId());
				criterias.put("userRights.id", 1L);
				criterias.put("specifics.id", txnPurposeSpecf.getSpecifics().getId());
				criterias.put("particulars.id", txnPurposeSpecf.getSpecifics().getParticularsId().getId());
				criterias.put("presentStatus", 1);
				UserRightSpecifics userHasRightForSpecf = genericDAO.getByCriteria(UserRightSpecifics.class, criterias,
						entityManager);
				if (userHasRightForSpecf != null) {
					criterias.clear();
					criterias.put("branch.id", txnPurposeBnchId);
					criterias.put("specifics.id", userHasRightForSpecf.getSpecifics().getId());
					criterias.put("organization.id", userHasRightForSpecf.getUser().getOrganization().getId());
					criterias.put("presentStatus", 1);
					BranchSpecifics bnchSpecf = genericDAO.getByCriteria(BranchSpecifics.class, criterias,
							entityManager);
					if (bnchSpecf != null) {
						row.put("id", userHasRightForSpecf.getSpecifics().getId());
						row.put("name", userHasRightForSpecf.getSpecifics().getName());
						row.put("accountCode", userHasRightForSpecf.getSpecifics().getParticularsId().getAccountCode());
						gettxnan.add(row);
					}
				}
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>>>>> " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getTaxesOnAdvanceOrAfterAdvAdj(Request request) {
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start " + json);
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			transactionService.getTaxOnAdvOrAdj(json, entityManager, user, result);
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getPriceForTaxInclusive(Request request) {
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start " + json);
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			Double priceOld = json.findValue("txnAdjustmentAmount") == null ? 0.0
					: json.findValue("txnAdjustmentAmount").asDouble();
			transactionService.getInclusiveTaxCalculated(json, entityManager, user, result);
			JsonNode findValue = result.findValue("advAdjTaxData");
			for (JsonNode jsonNode : findValue) {
				if (jsonNode.has("taxAmountWithoutRoundup")) {
					Double tax = jsonNode.findValue("taxAmountWithoutRoundup") == null ? 0.0
							: jsonNode.findValue("taxAmountWithoutRoundup").asDouble();
					priceOld -= tax;
				}
			}
			result = Json.newObject();
			result.put("price", priceOld);
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result validateTxnRefNo(Http.Request request) {
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start " + json);
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			String useremail = json.findValue("useremail").asText();
			String txnRefNo = json.findValue("txnRefNo") != null && !"".equals(json.findValue("txnRefNo"))
					? json.findValue("txnRefNo").asText()
					: null;
			List<Transaction> list = Transaction.findByInvoiceNumber(entityManager, user.getOrganization(), txnRefNo);
			if (list != null && list.size() > 0) {
				result.put("valid", false);
			} else {
				result.put("valid", true);
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		return Results.ok(result);
	}

	/*
	 * @Transactional public Result checkforlasttarnsactiondate(){
	 * log.log(Level.INFO, "inside checkforlasttarnsactiondate"); // EntityManager
	 * entityManager = getEntityManager(); ObjectNode result = Json.newObject();
	 * String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
	 * Users user = null; if (ipAddress == null) { ipAddress =
	 * request.remoteAddress(); } try { JsonNode json = request.body().asJson();
	 * log.log(Level.FINE, ">>>> Start " + json); user =
	 * getUserInfo(request); if (user == null) { return
	 * unauthorized(); } String useremail = json.findValue("useremail").asText();
	 * long txnPurposeId = json.findValue("txnPurposeId").asLong(); long
	 * txnPurposeBnchId = json.findValue("txnPurposeBnchId").asLong(); String
	 * transactionDate =
	 * json.findValue("transactionDate")!=null?json.findValue("transactionDate").
	 * asText():null; Date maxDate=null; Date transDate =
	 * IdosConstants.IDOSDF.parse(transactionDate); //StringBuilder sbquery = new
	 * StringBuilder("select MAX(obj.transactionDate) from Transaction obj where obj.transactionBranch="
	 * ); StringBuilder sbquery = new
	 * StringBuilder("select MAX(obj.transactionDate) from Transaction obj WHERE obj.transactionBranchOrganization="
	 * ).append(user.getOrganization().getId());
	 * sbquery.append(" and obj.transactionBranch.id=").append(txnPurposeBnchId);
	 * sbquery.append(" and (obj.transactionPurpose="+IdosConstants.
	 * SELL_ON_CASH_COLLECT_PAYMENT_NOW);
	 * sbquery.append(" or obj.transactionPurpose="+IdosConstants.
	 * SELL_ON_CREDIT_COLLECT_PAYMENT_LATER);
	 * sbquery.append(" or obj.transactionPurpose="+IdosConstants.
	 * RECEIVE_ADVANCE_FROM_CUSTOMER+")"); List
	 * maxDateObj=genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
	 * if(maxDateObj.get(0)!=null){ maxDate=(Date) maxDateObj.get(0); }else{
	 * maxDate=null; } if(transDate.before(maxDate)){ result = Json.newObject();
	 * result.put("value", "valid date"); throw new
	 * IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
	 * IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
	 * "BackDated date should not be less than previous transaction date"); }else{
	 * log.log(Level.INFO, "max date="+maxDate); log.log(Level.INFO,
	 * "transdate="+transDate); result = Json.newObject(); result.put("value",
	 * "valid date"); } } catch (Exception ex) { log.log(Level.SEVERE, "Error", ex);
	 * String strBuff = getStackTraceMessage(ex);
	 * expService.sendExceptionReport(strBuff, user.getEmail(),
	 * user.getOrganization().getName(),
	 * Thread.currentThread().getStackTrace()[1].getMethodName()); List<String>
	 * errorList = getStackTrace(ex); return Results.ok(errorPage.render(ex,
	 * errorList)); } return Results.ok(result); }
	 */

	@Transactional
	public Result getPurchaseRequisitionList(Http.Request request) {
		ObjectNode result = Json.newObject();
		Users user = null;
		Long orgId = null;
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			orgId = user.getOrganization().getId();
			List<PurchaseRequisitionTxnModel> purReqList = PurchaseRequisitionTxnModel
					.getAllActivePurchaseRequisitionTxnByOrg(orgId);
			if (null == orgId || orgId.equals(0)) {
				result.put("message", "Cannot find the organization!");
			} else {
				if (null == purReqList || purReqList.isEmpty() || purReqList.size() == 0) {
					result.put("message", "Cannot find the items!");
				} else {
					ArrayNode datas = result.putArray("items");
					ObjectNode row = null;
					for (PurchaseRequisitionTxnModel purReq : purReqList) {
						if (null != purReq && null != purReq.getId() && null != purReq.getTransactionRefNumber()
								&& !"".equals(purReq.getTransactionRefNumber())) {
							row = Json.newObject();
							row.put("id", purReq.getId());
							row.put("purReqRefNo", purReq.getTransactionRefNumber());
							datas.add(row);
						}
						result.put("result", true);
						result.remove("message");
					}
				}
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		log.log(Level.FINE, ">>>> End " + result);
		return ok(result);
	}

	@Transactional
	public Result getPurchaseRequisitionItemList(Http.Request request, Long purReqId) {
		ObjectNode result = Json.newObject();
		Users user = null;
		Long orgId = null;
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			orgId = user.getOrganization().getId();
			List<PurchaseRequisitionTxnItemModel> purReqItemsList = PurchaseRequisitionTxnItemModel
					.getPurchaseRequisitionTxnItemByPRId(orgId, purReqId);
			if (null == orgId || orgId.equals(0)) {
				result.put("message", "Cannot find the organization!");
			} else {
				if (null == purReqItemsList || purReqItemsList.isEmpty() || purReqItemsList.size() == 0) {
					result.put("message", "Cannot find the items!");
				} else {
					ArrayNode datas = result.putArray("items");
					ObjectNode row = null;
					for (PurchaseRequisitionTxnItemModel purReqItem : purReqItemsList) {
						if (null != purReqItem) {
							row = Json.newObject();
							row.put("id", purReqItem.getId());
							row.put("expense_id", purReqItem.getExpense().getId());
							row.put("no_of_unit", purReqItem.getNoOfUnits());
							row.put("measure_name", purReqItem.getMeasureName());
							row.put("vendor_id", purReqItem.getVendor().getId());
							row.put("oem", purReqItem.getOem());
							row.put("expected_date", purReqItem.getExpectedDatetime().toString());
							row.put("type_of_material", purReqItem.getTypeOfMaterial());
							datas.add(row);
						}
						result.put("result", true);
						result.remove("message");
					}
				}
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		log.log(Level.FINE, ">>>> End " + result);
		return ok(result);
	}

	@Transactional
	public Result getPurchaseRequisitionByBranchAndProject(Http.Request request, Long branchId, Long projectId) {
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = null;
		Long orgId = null;
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			orgId = user.getOrganization().getId();
			List<PurchaseRequisitionTxnModel> purReqList = PurchaseRequisitionTxnModel
					.findPRByOrgIdAndBranchIdAndProjectId(orgId, branchId, projectId);
			if (null == orgId || orgId.equals(0)) {
				result.put("message", "Cannot find the organization!");
			} else {
				if (null == purReqList || purReqList.isEmpty() || purReqList.size() == 0) {
					result.put("message", "Cannot find the items!");
				} else {
					ArrayNode datas = result.putArray("items");
					ObjectNode row = null;
					for (PurchaseRequisitionTxnModel purReq : purReqList) {
						if (null != purReq && null != purReq.getId() && null != purReq.getTransactionRefNumber()
								&& !"".equals(purReq.getTransactionRefNumber())) {
							row = Json.newObject();
							row.put("id", purReq.getId());
							row.put("purReqRefNo", purReq.getTransactionRefNumber());
							datas.add(row);
						}
						result.put("result", true);
						result.remove("message");
					}
				}
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		log.log(Level.FINE, ">>>> End " + result);
		return ok(result);
	}

	@Transactional
	public Result getPurchaseOrderByBranchAndProject(Http.Request request, Long branchId, Long projectId) {
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = null;
		Long orgId = null;
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			orgId = user.getOrganization().getId();
			List<PurchaseOrderTxnModel> purOrdList = PurchaseOrderTxnModel
					.findSubPObyOrgIdAndBranchIdAndProjectId(orgId, branchId, projectId);
			if (null == orgId || orgId.equals(0)) {
				result.put("message", "Cannot find the organization!");
			} else {
				if (null == purOrdList || purOrdList.isEmpty() || purOrdList.size() == 0) {
					result.put("message", "Cannot find the items!");
				} else {
					ArrayNode datas = result.putArray("items");
					ObjectNode row = null;
					for (PurchaseOrderTxnModel purOrd : purOrdList) {
						if (null != purOrd && null != purOrd.getId() && null != purOrd.getTransactionRefNumber()
								&& !"".equals(purOrd.getTransactionRefNumber())) {
							row = Json.newObject();
							row.put("id", purOrd.getId());
							row.put("purOrdRefNo", purOrd.getTransactionRefNumber());
							datas.add(row);
						}
						result.put("result", true);
						result.remove("message");
					}
				}
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		log.log(Level.FINE, ">>>> End " + result);
		return ok(result);
	}

}
