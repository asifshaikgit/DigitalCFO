package controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.Session;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.*;
import model.*;
import akka.NotUsed;
import play.mvc.Results;
import java.util.logging.Level;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.type.TypeReference;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http.Cookie;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.WebSocket;
import views.html.*;
import actor.VendorTransactionActor;
import play.Application;
import javax.inject.Inject;
import akka.stream.javadsl.*;
import akka.actor.*;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.libs.Files.TemporaryFile;
//import play.mvc.Http.Session;

public class VendorController extends StaticController {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static Application application;
	// private static JPAApi jpaApi;
	private static EntityManager entityManager;
	// private Request request;

	// private Http.Session session = request.session();
	@Inject
	public VendorController(Application application) {
		super(application);
		this.application = application;
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	// @BodyParser.Of(value = BodyParser.Raw.class, maxLength = 2048 * 1024)
	@BodyParser.Of(value = BodyParser.Raw.class)
	public Result saveVendor(Request request) {
		Users user = getUserInfo(request);
		if (user == null) {
			return unauthorized();
		}

		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();

		try {
			String rawBody = new String(request.body().asBytes().toArray());
			JsonNode json = objectMapper.readValue(rawBody, new TypeReference<JsonNode>() {
			});

			log.log(Level.FINE, ">>>> Start " + json);

			transaction.begin();
			Vendor vendor = VENDOR_SERVICE.saveVendor(json, user, entityManager);
			transaction.commit();
			String role = "";
			List<UsersRoles> userRoles = UsersRoles.getUserRoleList(entityManager, user.getOrganization().getId(),
					user.getId(),
					user.getBranch().getId());
			for (UsersRoles roles : userRoles) {
				role += roles.getRole().getName() + ",";
			}
			role = role.substring(0, role.length() - 1);
			result.put("role", role);
			result.put("vendorId", vendor.getId());
			result.put("info", "vendorAdded");
			result.put("id", vendor.getId());
			result.put("name", vendor.getName());
			result.put("address", vendor.getAddress());
			result.put("location", vendor.getLocation());
			result.put("email", vendor.getEmail());
			result.put("grantAccess", vendor.getGrantAccess());
			result.put("phone", vendor.getPhone());
			result.put("type", vendor.getType());
			result.put("entityType", "vendorCustomer");
			result.put("presentStatus", vendor.getPresentStatus());
			result.put("canCreateCustomer", user.canCreateCustomer());
			result.put("canActivateCustomer", user.canActivateCustomer());
			result.put("canActivateVendor", user.canActivateVendor());
			result.put("canCreateVendor", user.canCreateVendor());
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error parsing JSON", e);
			return badRequest("Invalid JSON format");
		} catch (Exception ex) {
			reportException(entityManager, transaction, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, transaction, user, th, result);
		}

		return Results.ok(result).as("application/json");
	}

	@Transactional
	public Result saveVendorGroup(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users users = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode an = result.putArray("newvendorGroupData");
			String useremail = json.findValue("useremail").asText();
			String vendorGroupId = json.findValue("vendGrpId") != null ? json.findValue("vendGrpId").asText() : null;
			String vendGroupName = json.findValue("vendGroupName").asText();
			String vendGroupKl = json.findValue("vendGroupKl").asText();
			session.adding("email", useremail);
			users = getUserInfo(request);
			VendorGroup vendGroup;
			if (vendorGroupId == null) {
				vendGroup = new VendorGroup();
			} else {
				vendGroup = VendorGroup.findById(IdosUtil.convertStringToLong(vendorGroupId));
			}
			vendGroup.setGroupName(vendGroupName);
			vendGroup.setKnowledgeLibrary(vendGroupKl);
			vendGroup.setGroupType(1);
			vendGroup.setOrganization(users.getOrganization());
			genericDAO.saveOrUpdate(vendGroup, users, entityManager);
			// Map<String, ActorRef> orgvendvendregistrered = new HashMap<String,
			// ActorRef>();
			// Object[] keyArray =
			// VendorTransactionActor.vendvendregistrered.keySet().toArray();
			// for (int i = 0; i < keyArray.length; i++) {
			// List<Users> orgusers = Users.findByEmailActDeact(entityManager, (String)
			// keyArray[i]);
			// if (!orgusers.isEmpty()
			// && orgusers.get(0).getOrganization().getId() ==
			// vendGroup.getOrganization().getId()) {
			// orgvendvendregistrered.put(keyArray[i].toString(),
			// VendorTransactionActor.vendvendregistrered.get(keyArray[i]));
			// }
			// }
			// VendorTransactionActor.addGroup(vendGroup.getId(), orgvendvendregistrered,
			// vendGroup.getGroupName(), 1,
			// "vendorCustomerGroup");
			String role = "";
			List<UsersRoles> userRoles = UsersRoles.getUserRoleList(entityManager, users.getOrganization().getId(),
					users.getId(),
					users.getBranch().getId());
			for (UsersRoles roles : userRoles) {
				role += roles.getRole().getName() + ",";
			}
			role = role.substring(0, role.length() - 1);
			result.put("role", role);
			result.put("canCreateCustomer", users.canCreateCustomer());
			result.put("canActivateCustomer", users.canActivateCustomer());
			result.put("canActivateVendor", users.canActivateVendor());
			result.put("canCreateVendor", users.canCreateVendor());
			result.put("id", vendGroup.getId());
			result.put("groupname", vendGroup.getGroupName());
			result.put("type", 1);
			result.put("entityType", "vendorCustomerGroup");
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, users.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, users.getEmail(), users.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result listVendorGroup(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users users = null;
		try {
			JsonNode json = request.body().asJson();
			ArrayNode an = result.putArray("vendorGroupList");
			String useremail = json.findValue("useremail").asText();
			session.adding("email", useremail);
			users = getUserInfo(request);
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("groupType", 1);
			criterias.put("presentStatus", 1);
			List<VendorGroup> vendorGroupList = genericDAO.findByCriteria(VendorGroup.class, criterias, entityManager);
			for (VendorGroup vendGrp : vendorGroupList) {
				ObjectNode row = Json.newObject();
				row.put("id", vendGrp.getId());
				row.put("vendGroupName", vendGrp.getGroupName());
				an.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, users.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, users.getEmail(), users.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result acceptTransaction(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode an = result.putArray("vendorGroupList");
			String txnEntityId = json.findValue("txnEntityId").asText();
			String txnvendorEmail = json.findValue("txnvendorEmail").asText();
			String txnvendUploadedDocUrl = json.findValue("txnvendUploadedDocUrl").asText();
			String txnvendorCustRemarks = json.findValue("txnvendorCustRemarks").asText();
			Transaction txn = Transaction.findById(IdosUtil.convertStringToLong(txnEntityId));
			String vendCust = "";
			if (txn.getTransactionVendorCustomer().getType() == 1) {
				vendCust = "vendor";
			}
			if (txn.getTransactionVendorCustomer().getType() == 2) {
				vendCust = "customer";
			}
			txn.setSupportingDocs(transactionDao.getAndDeleteSupportingDocument(txn.getSupportingDocs(), txnvendorEmail,
					txnvendUploadedDocUrl, txn.getCreatedBy(), entityManager));
			if (txnvendorCustRemarks != null && !txnvendorCustRemarks.equals("")) {
				if (txn.getRemarks() != null) {
					txn.setRemarks(
							txn.getRemarks() + "|" + vendCust + "-" + txnvendorEmail + "#" + txnvendorCustRemarks);
					txn.setVendCustRemarks(txnvendorCustRemarks);
				}
				if (txn.getRemarks() == null) {
					txn.setRemarks(vendCust + "-" + txnvendorEmail + "#" + txnvendorCustRemarks);
					txn.setVendCustRemarks(txnvendorCustRemarks);
				}
			}
			txn.setVendCustAcceptence(1);
			genericDAO.saveOrUpdate(txn, null, entityManager);
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "AcceptTransaction Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "AcceptTransaction Email", "AcceptTransaction Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result vendorGroupDetails(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users users = null;
		try {
			JsonNode json = request.body().asJson();
			ArrayNode an = result.putArray("vendorGroupDetails");
			String useremail = json.findValue("useremail").asText();
			String entityPrimaryId = json.findValue("entityPrimaryId").asText();
			session.adding("email", useremail);
			users = getUserInfo(request);
			VendorGroup vendGroup = VendorGroup.findById(IdosUtil.convertStringToLong(entityPrimaryId));
			ObjectNode row = Json.newObject();
			row.put("id", vendGroup.getId());
			row.put("groupName", vendGroup.getGroupName());
			row.put("groupKl", vendGroup.getKnowledgeLibrary());
			an.add(row);
		} catch (Exception ex) {
			log.log(Level.SEVERE, users.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, users.getEmail(), users.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result showVendorDetails(Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode results = Json.newObject();
		// EntityManager entityManager=getEntityManager();
		Users user = null;
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			JsonNode json = request.body().asJson();
			ArrayNode vendordetailan = results.putArray("vendordetailsData");
			ArrayNode vendGstinState = results.putArray("vendGstinState");
			ArrayNode tdsItemDetails = results.putArray("tdsItemDetails");
			String vendorEntityId = json.findValue("entityPrimaryId").asText();
			Vendor vendorDet = Vendor.findById(IdosUtil.convertStringToLong(vendorEntityId));

			if (vendorDet != null) {
				ObjectNode row = Json.newObject();
				row.put("id", vendorDet.getId());
				row.put("vendorName", vendorDet.getName());
				row.put("vendorEmail", vendorDet.getEmail());
				row.put("vendPhnCtryCode", vendorDet.getPhoneCtryCode());

				if (vendorDet != null && vendorDet.getGstin() != null) {
					if (vendorDet.getGstin().length() > 1) {
						row.put("gstinPart1", vendorDet.getGstin().substring(0, 2));
						if (vendorDet.getGstin().length() > 14) {
							row.put("gstinPart2", vendorDet.getGstin().substring(2));
						} else {
							row.put("gstinPart2", "");
						}
					} else {
						row.put("gstinPart1", "");
						row.put("gstinPart2", "");
					}
				} else {
					row.put("gstinPart1", "");
					row.put("gstinPart2", "");
				}
				row.put("businessIndividual", vendorDet.getIsBusiness());
				row.put("registeredOrUnReg", vendorDet.getIsRegistered());

				if (vendorDet.getPhone() != null) {
					int k = vendorDet.getPhone().indexOf("-");
					row.put("vendorPhone", vendorDet.getPhone().substring(k + 1, vendorDet.getPhone().length()));
				} else {
					row.put("vendorPhone", "");
				}
				if (vendorDet.getCountry() != null) {
					row.put("vendorCountryCode", vendorDet.getCountry());
				} else {
					row.put("vendorCountryCode", "");
				}
				if (vendorDet.getAddress() != null) {
					row.put("vendAddress", vendorDet.getAddress());
				} else {
					row.put("vendAddress", "");
				}

				if (vendorDet.getCountryState() != null) {
					row.put("vendorState", vendorDet.getCountryState());
				} else {
					row.put("vendorState", "");
				}

				if (vendorDet.getPanNo() != null) {
					row.put("vendPanNo", vendorDet.getPanNo());
				} else {
					row.put("vendPanNo", "");
				}

				if (vendorDet.getNatureOfVendor() != null) {
					row.put("natureOfVend", vendorDet.getNatureOfVendor().toString());
				} else {
					row.put("natureOfVend", "");
				}
				row.put("vendorFutPayAlwd", vendorDet.getPurchaseType());
				if (vendorDet.getDaysForCredit() != null) {
					row.put("daysOfCredit", vendorDet.getDaysForCredit());
				} else {
					row.put("daysOfCredit", "");
				}
				if (vendorDet.getTotalOriginalOpeningBalance() != null) {
					row.put("openingBalance",
							IdosConstants.decimalFormat.format(vendorDet.getTotalOriginalOpeningBalance()));
				} else {
					row.put("openingBalance", "");
				}
				if (vendorDet.getTotalOpeningBalanceAdvPaid() != null) {
					row.put("openingBalanceAdvPaid",
							IdosConstants.decimalFormat.format(vendorDet.getTotalOpeningBalanceAdvPaid()));
				} else {
					row.put("openingBalanceAdvPaid", "");
				}
				if (vendorDet.getVendorGroup() != null) {
					row.put("vendorGroup", vendorDet.getVendorGroup().getId());
				} else {
					row.put("vendorGroup", "");
				}
				if (vendorDet.getValidityFrom() != null) {
					row.put("validFrom", idosdf.format(vendorDet.getValidityFrom()));
				} else {
					row.put("validFrom", "");
				}
				if (vendorDet.getValidityTo() != null) {
					row.put("validTo", idosdf.format(vendorDet.getValidityTo()));
				} else {
					row.put("validTo", "");
				}
				if (vendorDet.getStatutoryName1() != null) {
					row.put("vendorStatutoryName1", vendorDet.getStatutoryName1());
				} else {
					row.put("vendorStatutoryName1", "");
				}
				if (vendorDet.getStatutoryNumber1() != null) {
					row.put("vendorStatutoryNumber1", vendorDet.getStatutoryNumber1());
				} else {
					row.put("vendorStatutoryNumber1", "");
				}
				if (vendorDet.getStatutoryName2() != null) {
					row.put("vendorStatutoryName2", vendorDet.getStatutoryName2());
				} else {
					row.put("vendorStatutoryName2", "");
				}
				if (vendorDet.getStatutoryNumber2() != null) {
					row.put("vendorStatutoryNumber2", vendorDet.getStatutoryNumber2());
				} else {
					row.put("vendorStatutoryNumber2", "");
				}
				if (vendorDet.getStatutoryName3() != null) {
					row.put("vendorStatutoryName3", vendorDet.getStatutoryName3());
				} else {
					row.put("vendorStatutoryName3", "");
				}
				if (vendorDet.getStatutoryNumber3() != null) {
					row.put("vendorStatutoryNumber3", vendorDet.getStatutoryNumber3());
				} else {
					row.put("vendorStatutoryNumber3", "");
				}
				if (vendorDet.getStatutoryName4() != null) {
					row.put("vendorStatutoryName4", vendorDet.getStatutoryName4());
				} else {
					row.put("vendorStatutoryName4", "");
				}
				if (vendorDet.getStatutoryNumber4() != null) {
					row.put("vendorStatutoryNumber4", vendorDet.getStatutoryNumber4());
				} else {
					row.put("vendorStatutoryNumber4", "");
				}
				row.put("vendorLocation", vendorDet.getLocation());
				row.put("getContractPoDoc", vendorDet.getContractPoDoc());
				String vendAllowedForAdjustments = null;
				if (vendorDet.getAdjustmentsAllowed() != null) {
					row.put("vendAllowedForAdjustments", vendorDet.getAdjustmentsAllowed());
				} else {
					row.put("vendAllowedForAdjustments", vendAllowedForAdjustments);
				}
				String vendAdjustmentsName = "";
				if (vendorDet.getAdjustmentsName() != null) {
					vendAdjustmentsName = vendorDet.getAdjustmentsName();
				}
				row.put("vendAdjustmentsName", vendAdjustmentsName);
				String vendAdjustmentsBasis = null;
				if (vendorDet.getAdjustmentsBasis() != null) {
					row.put("vendAdjustmentsBasis", vendorDet.getAdjustmentsBasis());
				} else {
					row.put("vendAdjustmentsBasis", vendAdjustmentsBasis);
				}
				String vendAdjustmentsBasisRateForEachInvoice = "";
				if (vendorDet.getAdjustmentsRate() != null) {
					vendAdjustmentsBasisRateForEachInvoice = vendorDet.getAdjustmentsRate().toString();
				}
				row.put("vendAdjustmentsBasisRateForEachInvoice", vendAdjustmentsBasisRateForEachInvoice);
				String Specifications = "";
				String vendspecfunitPrice = "";
				String vendSpecfRcmRate = "";
				String vendSpecfCessRate = "";
				String vendSpecfApplicableDate = "";
				if (vendorDet.getVendorSpecifics() != null) {
					Specifications = vendorDet.getVendorSpecifics();
					vendspecfunitPrice = vendorDet.getVendorSpecificsUnitPrice();
					vendSpecfRcmRate = vendorDet.getVendorSpecificsRcmTaxRate();
					vendSpecfCessRate = vendorDet.getVendorSpecificsRcmCessRate();
					vendSpecfApplicableDate = vendorDet.getVendorSpecificsRcmApplicableDate();
					String vendorSpecifics = vendorDet.getVendorSpecifics();
					vendorSpecifics = IdosUtil.removeLastChar(vendorSpecifics);
					String[] split = vendorSpecifics.split(",");
					for (String specific : split) {
						if (specific != null && !specific.equals("")) {
							VendorTDSTaxes tdsSpecific = VendorTDSTaxes.isTdsSeupForSpecific(entityManager,
									user.getOrganization().getId(), Long.parseLong(specific));
							if (tdsSpecific != null) {
								ObjectNode tdsRow = Json.newObject();
								tdsRow.put("specificId", specific);
								tdsRow.put("tdsWhType", tdsSpecific.getTdsSection().getId());
								tdsRow.put("tdsTaxRate", tdsSpecific.getTaxRate());
								tdsRow.put("tdsTaxTransLimit", tdsSpecific.getTransLimit());
								tdsRow.put("tdsTaxOverallLimitApply", tdsSpecific.getOverAllLimitApply());
								if (tdsSpecific.getOverAllLimit() != null) {
									tdsRow.put("overallLimit", tdsSpecific.getOverAllLimit());
								} else {
									tdsRow.put("overallLimit", "");
								}
								tdsRow.put("tdsFromDate", IdosConstants.IDOSDF.format(tdsSpecific.getFromDate()));
								tdsRow.put("tdsToDate", IdosConstants.IDOSDF.format(tdsSpecific.getToDate()));
								tdsItemDetails.add(tdsRow);
							}
						}
					}
				}
				row.put("vendspecfunitPrice", vendspecfunitPrice);
				row.put("Specifications", Specifications);
				row.put("vendSpecfRcmRate", vendSpecfRcmRate);
				row.put("vendSpecfCessRate", vendSpecfCessRate);
				row.put("vendSpecfApplicableDate", vendSpecfApplicableDate);
				List<BranchVendors> vendorBranches = vendorDet.getVendorBranches();
				String vendBranches = "";
				String openingBalance = "";
				String openingBalanceAP = "";
				for (BranchVendors bnchVendors : vendorBranches) {
					if (bnchVendors.getPresentStatus() == 1) {
						vendBranches += bnchVendors.getBranch().getId() + ",";
						openingBalance += bnchVendors.getOriginalOpeningBalance() == null ? ""
								: bnchVendors.getOriginalOpeningBalance() + ",";
						openingBalanceAP += bnchVendors.getOriginalOpeningBalanceAdvPaid() == null ? ""
								: bnchVendors.getOriginalOpeningBalanceAdvPaid() + ",";
					}
				}
				if (!vendBranches.equals("")) {
					vendBranches = vendBranches.substring(0, vendBranches.length() - 1);
				}
				if (!openingBalance.equals("")) {
					openingBalance = openingBalance.substring(0, openingBalance.length() - 1);
				}
				if (!openingBalanceAP.equals("")) {
					openingBalanceAP = openingBalanceAP.substring(0, openingBalanceAP.length() - 1);
				}
				row.put("vendBranches", vendBranches);
				row.put("branchOpeningBalance", openingBalance);
				row.put("branchopeningBalanceAP", openingBalanceAP);

				// Vendor Branch and Billwise Opening Balance
				BILLWISE_OPENING_BALANCE_SERVICE.getVendorOpeningBalance(results, user, vendorDet, entityManager);
				// BRANCHWISE_ADVANCE_BALANCE_SERVICE.getVendorAdvanceBalance(results, user,
				// vendorDet, entityManager);
				// TDS
				List<VendorTDSTaxes> tdsHistory = VendorTDSTaxes.findVendorTdsHistory(entityManager,
						vendorDet.getOrganization().getId(), vendorDet.getId());
				String tdsHistoryList = "";
				if (tdsHistory != null) {
					for (VendorTDSTaxes tdsObj : tdsHistory) {
						if (tdsObj.getTaxRate() != null) {
							StringBuilder history = new StringBuilder();
							Specifics spec = null;
							if (tdsObj.getSpecifics() != null) {
								spec = Specifics.findById(tdsObj.getSpecifics());
								if (spec != null) {
									history.append(spec.getName());
									history.append(" - ");
								}
							}
							if (spec != null) {
								if (tdsObj.getTaxRate() != null) {
									history.append("(TDS Rate :").append(tdsObj.getTaxRate()).append(" %)");
								}
								if (tdsObj.getFromDate() != null) {
									history.append("-(from ").append(IdosConstants.IDOSDF.format(tdsObj.getFromDate()));
									history.append(" to ");
								}
								if (tdsObj.getToDate() != null) {
									history.append(IdosConstants.IDOSDF.format(tdsObj.getToDate()));
									history.append(")");
								}
								tdsHistoryList += history.toString() + "|";
							}
						}
					}
				}
				row.put("tdsHistoryList", tdsHistoryList);
				/************ Start Detail ***********************************/
				StringBuilder vendorDetailIdHid = new StringBuilder();
				StringBuilder gstinCodeHid = new StringBuilder();
				StringBuilder vendorAddressHid = new StringBuilder();
				StringBuilder vendorCountryCodeHid = new StringBuilder();
				StringBuilder vendorStateHid = new StringBuilder();
				StringBuilder vendorStateCodeHid = new StringBuilder();
				StringBuilder vendorLocationHid = new StringBuilder();
				StringBuilder vendorPhnNocountryCodeHid = new StringBuilder();
				StringBuilder vendorPhone1Hid = new StringBuilder();
				StringBuilder vendorPhone2Hid = new StringBuilder();
				StringBuilder vendorPhone3Hid = new StringBuilder();
				String vendorDetailId = "0";
				int sizeOfVendor = vendorDet.getVendorDetails().size();
				for (VendorDetail vendorDetail : VendorDetail.findGstByVendorID(entityManager, vendorDet.getId())) {
					System.out.println(vendorDetail.toString());
					// IDOSWORK-169: Now GSTIN and Location will uniquely identify row in
					// VENDOR_DETAIL, as we can enter GSTIN=1234, Location=PUne and with same
					// GSTIN=1234, location=Mumbai is valid
					if (vendorDet.getIsRegistered() == 1) {
						if (vendorDetail.getGstin() != null && vendorDet.getGstin() != null
								&& vendorDet.getLocation() != null
								&& vendorDet.getGstin().equals(vendorDetail.getGstin())
								&& vendorDet.getLocation().equals(vendorDetail.getLocation())) {

							row.put("vendPhnCtryCode",
									vendorDetail.getPhoneCtryCode() == null ? "" : vendorDetail.getPhoneCtryCode());
							if (vendorDetail.getPhone() != null) {
								int k = vendorDetail.getPhone().indexOf("-");
								row.put("vendorPhone",
										vendorDetail.getPhone().substring(k + 1, vendorDetail.getPhone().length()));
							} else {
								row.put("vendorPhone", "");
							}
							if (vendorDetail.getCountry() != null) {
								row.put("vendorCountry", vendorDetail.getCountry());
							} else {
								row.put("vendorCountry", "");
							}
							if (vendorDetail.getCountryState() != null) {
								row.put("vendorState", vendorDetail.getCountryState());
							} else {
								row.put("vendorState", "");
							}
							if (vendorDetail.getAddress() != null) {
								row.put("vendorAddress", vendorDetail.getAddress());
							} else {
								row.put("vendorAddress", "");
							}
							row.put("vendorLocation",
									vendorDetail.getLocation() == null ? "" : vendorDetail.getLocation());
						} else {
							ObjectNode row1 = Json.newObject();
							if (vendorDetail.getGstin() != null && vendorDetail.getGstin().length() == 15) {
								row1.put("gstinCode", vendorDetail.getGstin());
							} else {
								row1.put("gstinCode", "");
							}
							if (vendorDetail.getCountryState() != null) {
								row1.put("gstState", vendorDetail.getCountryState());
							} else {
								row1.put("gstState", "");
							}
							row1.put("vendorID", vendorDetail.getId());
							row1.put("vendorStatus", vendorDetail.getPresentStatus());
							row1.put("vendorLocation",
									vendorDetail.getLocation() == null ? "" : vendorDetail.getLocation());
							vendGstinState.add(row1);

							vendorDetailIdHid.append("|").append(vendorDetail.getId());
							vendorDetailId = String.valueOf(vendorDetail.getId());
							gstinCodeHid.append("|")
									.append(vendorDetail.getGstin() == null ? "" : vendorDetail.getGstin());
							vendorAddressHid.append("|")
									.append(vendorDetail.getAddress() == null ? "" : vendorDetail.getAddress());
							vendorCountryCodeHid.append("|")
									.append(vendorDetail.getCountry() == null ? "" : vendorDetail.getCountry());
							vendorStateHid.append("|")
									.append(vendorDetail.getCountryState() == null ? ""
											: vendorDetail.getCountryState());
							vendorStateCodeHid.append("|")
									.append(vendorDetail.getStateCode() == null ? "" : vendorDetail.getStateCode());
							vendorLocationHid.append("|")
									.append(vendorDetail.getLocation() == null ? "" : vendorDetail.getLocation());
							vendorPhnNocountryCodeHid.append("|")
									.append(vendorDetail.getPhoneCtryCode() == null ? ""
											: vendorDetail.getPhoneCtryCode());
							log.log(Level.INFO, "vendor phone country code=" + vendorDetail.getPhoneCtryCode());
							String phone = vendorDetail.getPhone() == null ? "" : vendorDetail.getPhone();
							if (phone.length() > 2) {
								vendorPhone1Hid.append("|").append(phone.substring(0, 3));
							} else {
								vendorPhone1Hid.append("|");
							}
							if (phone.length() > 5) {
								vendorPhone2Hid.append("|").append(phone.substring(3, 6));
							} else {
								vendorPhone2Hid.append("|");
							}
							if (phone.length() > 6) {
								vendorPhone3Hid.append("|").append(phone.substring(6));
							} else {
								vendorPhone3Hid.append("|");
							}

						}
					} else {
						row.put("vendPhnCtryCode",
								vendorDetail.getPhoneCtryCode() == null ? "" : vendorDetail.getPhoneCtryCode());
						if (vendorDetail.getPhone() != null) {
							int k = vendorDetail.getPhone().indexOf("-");
							row.put("vendorPhone",
									vendorDetail.getPhone().substring(k + 1, vendorDetail.getPhone().length()));
						} else {
							row.put("vendorPhone", "");
						}
						if (vendorDetail.getCountry() != null) {
							row.put("vendorCountry", vendorDetail.getCountry());
						} else {
							row.put("vendorCountry", "");
						}
						if (vendorDetail.getCountryState() != null) {
							row.put("vendorState", vendorDetail.getCountryState());
						} else {
							row.put("vendorState", "");
						}
						if (vendorDetail.getAddress() != null) {
							row.put("vendorAddress", vendorDetail.getAddress());
						} else {
							row.put("vendorAddress", "");
						}
						row.put("vendorLocation",
								vendorDetail.getLocation() == null ? "" : vendorDetail.getLocation());
					}

				}
				results.put("vendorDetailIdHid", vendorDetailId);
				results.put("vendorDetailIdListHid", vendorDetailIdHid.toString());
				results.put("vendorGstinCodeHid", gstinCodeHid.toString());
				results.put("vendorAddressHid", vendorAddressHid.toString());
				results.put("vendorcountryCodeHid", vendorCountryCodeHid.toString());
				results.put("vendorstateHid", vendorStateHid.toString());
				results.put("vendorStateCodeHid", vendorStateCodeHid.toString());
				results.put("vendorlocationHid", vendorLocationHid.toString());
				results.put("vendorPhnNocountryCodeHid", vendorPhnNocountryCodeHid.toString());
				results.put("vendorphone1Hid", vendorPhone1Hid.toString());
				results.put("vendorphone2Hid", vendorPhone2Hid.toString());
				results.put("vendorphone3Hid", vendorPhone3Hid.toString());
				vendordetailan.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			ex.printStackTrace();
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "ShowVendorDetails Email", "ShowVendorDetails Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> end " + results);
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result listVendor(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			entityTransaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode childCOA = result.putArray("vendorData");
			String email = json.findValue("usermail").asText();
			session.adding("email", email);
			user = getUserInfo(request);
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.clear();
			criterias.put("branch.id", user.getBranch().getId());
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("presentStatus", 1);
			List<Vendor> vendors = genericDAO.findByCriteria(Vendor.class, criterias, entityManager);
			if (vendors.size() > 0) {
				for (Vendor vendor : vendors) {
					ObjectNode row = Json.newObject();
					row.put("id", vendor.getId());
					row.put("name", vendor.getName());
					if (vendor.getType() == 2 || vendor.getType() == 3) {
						row.put("type", "Customer");
					}
					if (vendor.getType() == 1) {
						row.put("type", "Vendor");
					}
					if (vendor.getAddress() != null && !vendor.getAddress().equals("")) {
						// row.put("address", countries.get(vendor.getCountry().toString()));
						row.put("address", vendor.getAddress());
					} else {
						row.put("address", "");
					}
					row.put("location", vendor.getLocation());
					row.put("contract", vendor.getContractPoDoc());
					row.put("email", vendor.getEmail());
					row.put("phone", vendor.getPhone());
					if (vendor.getValidityFrom() != null) {
						row.put("validFrom", idosdf.format(vendor.getValidityFrom()));
					}
					if (vendor.getValidityTo() != null) {
						row.put("validTo", idosdf.format(vendor.getValidityTo()));
					}
					childCOA.add(row);
				}
			}
			entityTransaction.commit();
		} catch (Exception ex) {
			if (entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result searchVendor(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			ArrayNode serachVendorData = result.putArray("vendorListData");
			// String email = json.findValue("usermail").asText();
			// session.adding("email", email);
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			List<UsersRoles> userRoles = user.getUserRoles();
			StringBuilder userRolesStr = new StringBuilder();
			StringBuilder userRoleID = new StringBuilder();
			for (UsersRoles roles : userRoles) {
				userRolesStr.append(roles.getRole().getName()).append(",");
				userRoleID.append(roles.getRole()).append(",");
			}
			Integer canActivateVendor;
			if (user.canActivateVendor() == true) {
				canActivateVendor = 1;
				result.put("canActivateVendor", canActivateVendor);
			}
			if (user.canActivateVendor() == false) {
				canActivateVendor = 0;
				result.put("canActivateVendor", canActivateVendor);
			}
			result.put("userRole", userRoleID.toString());
			result.put("userRoles", userRolesStr.toString());
			String enteredVendorValue = json.findValue("freeTextSearchVendorVal").asText();
			List<Vendor> vendors = null;
			if (enteredVendorValue != null && !enteredVendorValue.equals("")) {
				enteredVendorValue = enteredVendorValue.toLowerCase();
				String newsbquery = ("select obj from Vendor obj WHERE obj.organization.id =?1 and obj.type=1 and (lower(obj.name) like ?2 or lower(obj.location) like ?3 or lower(obj.email) like ?4 or lower(obj.phone) like ?5 or lower(obj.address) like ?6) order by obj.createdAt desc");
				ArrayList inparam = new ArrayList(6);
				inparam.add(user.getOrganization().getId());
				inparam.add("%" + enteredVendorValue + "%");
				inparam.add("%" + enteredVendorValue + "%");
				inparam.add("%" + enteredVendorValue + "%");
				inparam.add("%" + enteredVendorValue + "%");
				inparam.add("%" + enteredVendorValue + "%");
				vendors = genericDAO.queryWithParams(newsbquery, entityManager, inparam);
			} else {
				vendors = Vendor.findByOrgIdAndType(entityManager, user.getOrganization().getId(), 1);
			}
			if (vendors.size() > 0) {
				for (Vendor vendor : vendors) {
					ObjectNode row = Json.newObject();
					row.put("id", vendor.getId());
					row.put("name", vendor.getName());
					if (vendor.getAddress() != null && !vendor.getAddress().equals("")) {
						// row.put("address", countries.get(vendor.getCountry().toString()));
						row.put("address", vendor.getAddress());
					} else {
						row.put("address", "");
					}
					row.put("location", vendor.getLocation());
					row.put("contract", vendor.getContractPoDoc());
					row.put("email", vendor.getEmail());
					row.put("phone", vendor.getPhone());
					row.put("grantAccess", vendor.getGrantAccess());
					if (vendor.getValidityFrom() != null) {
						row.put("validFrom", idosdf.format(vendor.getValidityFrom()));
					}
					if (vendor.getValidityTo() != null) {
						row.put("validTo", idosdf.format(vendor.getValidityTo()));
					}
					row.put("grantAccess", vendor.getGrantAccess());
					if (userRolesStr.indexOf("MASTER ADMIN") != -1
							|| (userRolesStr.indexOf("APPROVER") != -1 && user.canActivateVendor())) {

						row.put("presentStatus", vendor.getPresentStatus());
					} else {

						row.put("presentStatus", "-1");
					}
					serachVendorData.add(row);
				}
			}
			if (vendors.size() == 0) {
				String newsbquery = ("select obj from Vendor obj WHERE obj.organization.id = ?1 and obj.type=1 and (obj.vendorGroup IS NOT NULL and obj.vendorGroup.groupName like ?2) and obj.presentStatus=1");
				ArrayList inparam = new ArrayList(2);
				inparam.add(user.getOrganization().getId());
				inparam.add(enteredVendorValue + "%");
				vendors = genericDAO.queryWithParams(newsbquery, entityManager, inparam);
				if (vendors.size() == 0) {
					String bnchvendnewsbquery = ("select obj from BranchVendors obj where obj.organization.id = ?1 and obj.vendor.type = 1 and obj.branch.name like ?2 and obj.presentStatus = 1 GROUP BY obj.vendor.id, obj.id");
					inparam.clear();
					inparam.add(user.getOrganization().getId());
					inparam.add(enteredVendorValue + "%");
					List<BranchVendors> branchVendors = genericDAO.queryWithParams(bnchvendnewsbquery, entityManager,
							inparam);
					if (branchVendors.size() > 0) {
						for (BranchVendors bnchvendor : branchVendors) {
							ObjectNode row = Json.newObject();
							row.put("id", bnchvendor.getVendor().getId());
							row.put("name", bnchvendor.getVendor().getName());
							if (bnchvendor.getVendor().getAddress() != null
									&& !bnchvendor.getVendor().getAddress().equals("")) {
								// row.put("address",
								// countries.get(bnchvendor.getVendor().getCountry().toString()));
								row.put("address", bnchvendor.getVendor().getAddress());
							} else {
								row.put("address", "");
							}
							row.put("location", bnchvendor.getVendor().getLocation());
							row.put("contract", bnchvendor.getVendor().getContractPoDoc());
							row.put("email", bnchvendor.getVendor().getEmail());
							row.put("phone", bnchvendor.getVendor().getPhone());
							row.put("grantAccess", bnchvendor.getVendor().getGrantAccess());
							if (bnchvendor.getVendor().getValidityFrom() != null) {
								row.put("validFrom", idosdf.format(bnchvendor.getVendor().getValidityFrom()));
							}
							if (bnchvendor.getVendor().getValidityTo() != null) {
								row.put("validTo", idosdf.format(bnchvendor.getVendor().getValidityTo()));
							}
							if (userRolesStr.indexOf("MASTER ADMIN") != -1
									|| (userRolesStr.indexOf("APPROVER") != -1 && user.canActivateVendor())) {
								row.put("presentStatus", bnchvendor.getVendor().getPresentStatus());
							} else {
								row.put("presentStatus", "-1");
							}
							serachVendorData.add(row);
						}
					}
					if (branchVendors.size() == 0) {
						String specificsvendnewsbquery = ("select obj from VendorSpecific obj where obj.organization.id =?1 AND obj.vendorSpecific.type=1 and obj.specificsVendors.name like ?2 and obj.presentStatus=1 GROUP BY obj.vendorSpecific.id, obj.id");
						inparam.clear();
						inparam.add(user.getOrganization().getId());
						inparam.add(enteredVendorValue + "%");
						List<VendorSpecific> specificsVendors = genericDAO.queryWithParams(specificsvendnewsbquery,
								entityManager, inparam);
						if (specificsVendors.size() > 0) {
							for (VendorSpecific vendSpecifics : specificsVendors) {
								ObjectNode row = Json.newObject();
								row.put("id", vendSpecifics.getVendorSpecific().getId());
								row.put("name", vendSpecifics.getVendorSpecific().getName());
								if (vendSpecifics.getVendorSpecific().getAddress() != null
										&& !vendSpecifics.getVendorSpecific().getAddress().equals("")) {
									// row.put("address",
									// countries.get(vendSpecifics.getVendorSpecific().getCountry().toString()));
									row.put("address", vendSpecifics.getVendorSpecific().getAddress());
									row.put("address", "");
								}
								row.put("location", vendSpecifics.getVendorSpecific().getLocation());
								row.put("contract", vendSpecifics.getVendorSpecific().getContractPoDoc());
								row.put("email", vendSpecifics.getVendorSpecific().getEmail());
								row.put("phone", vendSpecifics.getVendorSpecific().getPhone());
								row.put("grantAccess", vendSpecifics.getVendorSpecific().getGrantAccess());
								if (vendSpecifics.getVendorSpecific().getValidityFrom() != null) {
									row.put("validFrom",
											idosdf.format(vendSpecifics.getVendorSpecific().getValidityFrom()));
								}
								if (vendSpecifics.getVendorSpecific().getValidityTo() != null) {
									row.put("validTo",
											idosdf.format(vendSpecifics.getVendorSpecific().getValidityTo()));
								}
								if (userRolesStr.indexOf("MASTER ADMIN") != -1
										|| (userRolesStr.indexOf("APPROVER") != -1 && user.canActivateVendor())) {
									row.put("presentStatus", vendSpecifics.getVendorSpecific().getPresentStatus());
								} else {
									row.put("presentStatus", "-1");
								}
								serachVendorData.add(row);
							}
						}
					}
				} else {
					for (Vendor vendor : vendors) {
						ObjectNode row = Json.newObject();
						row.put("id", vendor.getId());
						row.put("name", vendor.getName());
						if (vendor.getAddress() != null && !vendor.getAddress().equals("")) {
							// row.put("address", countries.get(vendor.getCountry().toString()));
							row.put("address", vendor.getAddress());
						} else {
							row.put("address", "");
						}
						row.put("location", vendor.getLocation());
						row.put("contract", vendor.getContractPoDoc());
						row.put("email", vendor.getEmail());
						row.put("phone", vendor.getPhone());
						row.put("grantAccess", vendor.getGrantAccess());
						if (vendor.getValidityFrom() != null) {
							row.put("validFrom", idosdf.format(vendor.getValidityFrom()));
						}
						if (vendor.getValidityTo() != null) {
							row.put("validTo", idosdf.format(vendor.getValidityTo()));
						}
						if (userRolesStr.indexOf("MASTER ADMIN") != -1
								|| (userRolesStr.indexOf("APPROVER") != -1 && user.canActivateVendor())) {
							row.put("presentStatus", vendor.getPresentStatus());
						} else {
							row.put("presentStatus", "-1");
						}
						serachVendorData.add(row);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result searchVendorName(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			ArrayNode serachVendorData = result.putArray("vendorListData");
			// String email = json.findValue("usermail").asText();
			// session.adding("email", email);
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			List<UsersRoles> userRoles = user.getUserRoles();
			StringBuilder userRolesStr = new StringBuilder();
			StringBuilder userRoleID = new StringBuilder();
			for (UsersRoles roles : userRoles) {
				userRolesStr.append(roles.getRole().getName()).append(",");
				userRoleID.append(roles.getRole()).append(",");
			}
			Integer canActivateVendor;
			if (user.canActivateVendor() == true) {
				canActivateVendor = 1;
				result.put("canActivateVendor", canActivateVendor);
			}
			if (user.canActivateVendor() == false) {
				canActivateVendor = 0;
				result.put("canActivateVendor", canActivateVendor);
			}
			result.put("userRole", userRoleID.toString());
			result.put("userRoles", userRolesStr.toString());
			/// name coming in myData#####
			String enteredVendorValue = json.findValue("vName").asText();
			List<Vendor> vendors = null;
			if (!enteredVendorValue.equals("")) {
				String newsbquery = ("select obj from Vendor obj WHERE obj.organization.id =?1 and obj.type=1 and (obj.name like ?2 or obj.location like ?3 or obj.email like ?4 or obj.phone like ?5 or obj.address like ?6) and obj.presentStatus=1");
				ArrayList inparam = new ArrayList(6);
				inparam.add(user.getOrganization().getId());
				inparam.add(enteredVendorValue + "%");
				inparam.add(enteredVendorValue + "%");
				inparam.add(enteredVendorValue + "%");
				inparam.add(enteredVendorValue + "%");
				inparam.add(enteredVendorValue + "%");
				vendors = genericDAO.queryWithParams(newsbquery, entityManager, inparam);
			} else {
				vendors = Vendor.findByOrgIdAndType(entityManager, user.getOrganization().getId(), 1);
			}
			if (vendors.size() > 0) {
				for (Vendor vendor : vendors) {
					ObjectNode row = Json.newObject();
					row.put("id", vendor.getId());
					row.put("name", vendor.getName());
					if (vendor.getAddress() != null && !vendor.getAddress().equals("")) {
						// row.put("address", countries.get(vendor.getCountry().toString()));
						row.put("address", vendor.getAddress());
					} else {
						row.put("address", "");
					}
					row.put("location", vendor.getLocation());
					row.put("contract", vendor.getContractPoDoc());
					row.put("email", vendor.getEmail());
					row.put("phone", vendor.getPhone());
					row.put("grantAccess", vendor.getGrantAccess());
					if (vendor.getValidityFrom() != null) {
						row.put("validFrom", idosdf.format(vendor.getValidityFrom()));
					}
					if (vendor.getValidityTo() != null) {
						row.put("validTo", idosdf.format(vendor.getValidityTo()));
					}
					row.put("grantAccess", vendor.getGrantAccess());
					if (userRolesStr.indexOf("MASTER ADMIN") != -1
							|| (userRolesStr.indexOf("APPROVER") != -1 && user.canActivateVendor())) {

						row.put("presentStatus", vendor.getPresentStatus());
					} else {

						row.put("presentStatus", "-1");
					}
					serachVendorData.add(row);
				}
			}
			if (vendors.size() == 0) {
				String newsbquery = ("select obj from Vendor obj WHERE obj.organization.id = ?1 and obj.type=1 and (obj.vendorGroup IS NOT NULL and obj.vendorGroup.groupName like ?2) and obj.presentStatus=1");
				ArrayList inparam = new ArrayList(2);
				inparam.add(user.getOrganization().getId());
				inparam.add(enteredVendorValue + "%");
				vendors = genericDAO.queryWithParams(newsbquery, entityManager, inparam);
				if (vendors.size() == 0) {
					String bnchvendnewsbquery = ("select obj from BranchVendors obj where obj.organization.id =?1 and obj.vendor.type=1 and obj.branch.name like ?2 and obj.presentStatus=1 GROUP BY obj.vendor.id");
					inparam.clear();
					inparam.add(user.getOrganization().getId());
					inparam.add(enteredVendorValue + "%");
					List<BranchVendors> branchVendors = genericDAO.queryWithParams(bnchvendnewsbquery, entityManager,
							inparam);
					if (branchVendors.size() > 0) {
						for (BranchVendors bnchvendor : branchVendors) {
							ObjectNode row = Json.newObject();
							row.put("id", bnchvendor.getVendor().getId());
							row.put("name", bnchvendor.getVendor().getName());
							if (bnchvendor.getVendor().getAddress() != null
									&& !bnchvendor.getVendor().getAddress().equals("")) {
								// row.put("address",
								// countries.get(bnchvendor.getVendor().getCountry().toString()));
								row.put("address", bnchvendor.getVendor().getAddress());
							} else {
								row.put("address", "");
							}
							row.put("location", bnchvendor.getVendor().getLocation());
							row.put("contract", bnchvendor.getVendor().getContractPoDoc());
							row.put("email", bnchvendor.getVendor().getEmail());
							row.put("phone", bnchvendor.getVendor().getPhone());
							row.put("grantAccess", bnchvendor.getVendor().getGrantAccess());
							if (bnchvendor.getVendor().getValidityFrom() != null) {
								row.put("validFrom", idosdf.format(bnchvendor.getVendor().getValidityFrom()));
							}
							if (bnchvendor.getVendor().getValidityTo() != null) {
								row.put("validTo", idosdf.format(bnchvendor.getVendor().getValidityTo()));
							}
							if (userRolesStr.indexOf("MASTER ADMIN") != -1
									|| (userRolesStr.indexOf("APPROVER") != -1 && user.canActivateVendor())) {
								row.put("presentStatus", bnchvendor.getVendor().getPresentStatus());
							} else {
								row.put("presentStatus", "-1");
							}
							serachVendorData.add(row);
						}
					}
					if (branchVendors.size() == 0) {
						String specificsvendnewsbquery = ("select obj from VendorSpecific obj where obj.organization.id =?1 AND obj.vendorSpecific.type=1 and obj.specificsVendors.name like ?2 and obj.presentStatus=1 GROUP BY obj.vendorSpecific.id");
						inparam.clear();
						inparam.add(user.getOrganization().getId());
						inparam.add(enteredVendorValue + "%");
						List<VendorSpecific> specificsVendors = genericDAO.queryWithParams(specificsvendnewsbquery,
								entityManager, inparam);
						if (specificsVendors.size() > 0) {
							for (VendorSpecific vendSpecifics : specificsVendors) {
								ObjectNode row = Json.newObject();
								row.put("id", vendSpecifics.getVendorSpecific().getId());
								row.put("name", vendSpecifics.getVendorSpecific().getName());
								if (vendSpecifics.getVendorSpecific().getAddress() != null
										&& !vendSpecifics.getVendorSpecific().getAddress().equals("")) {
									// row.put("address",
									// countries.get(vendSpecifics.getVendorSpecific().getCountry().toString()));
									row.put("address", vendSpecifics.getVendorSpecific().getAddress());
									row.put("address", "");
								}
								row.put("location", vendSpecifics.getVendorSpecific().getLocation());
								row.put("contract", vendSpecifics.getVendorSpecific().getContractPoDoc());
								row.put("email", vendSpecifics.getVendorSpecific().getEmail());
								row.put("phone", vendSpecifics.getVendorSpecific().getPhone());
								row.put("grantAccess", vendSpecifics.getVendorSpecific().getGrantAccess());
								if (vendSpecifics.getVendorSpecific().getValidityFrom() != null) {
									row.put("validFrom",
											idosdf.format(vendSpecifics.getVendorSpecific().getValidityFrom()));
								}
								if (vendSpecifics.getVendorSpecific().getValidityTo() != null) {
									row.put("validTo",
											idosdf.format(vendSpecifics.getVendorSpecific().getValidityTo()));
								}
								if (userRolesStr.indexOf("MASTER ADMIN") != -1
										|| (userRolesStr.indexOf("APPROVER") != -1 && user.canActivateVendor())) {
									row.put("presentStatus", vendSpecifics.getVendorSpecific().getPresentStatus());
								} else {
									row.put("presentStatus", "-1");
								}
								serachVendorData.add(row);
							}
						}
					}
				} else {
					for (Vendor vendor : vendors) {
						ObjectNode row = Json.newObject();
						row.put("id", vendor.getId());
						row.put("name", vendor.getName());
						if (vendor.getAddress() != null && !vendor.getAddress().equals("")) {
							// row.put("address", countries.get(vendor.getCountry().toString()));
							row.put("address", vendor.getAddress());
						} else {
							row.put("address", "");
						}
						row.put("location", vendor.getLocation());
						row.put("contract", vendor.getContractPoDoc());
						row.put("email", vendor.getEmail());
						row.put("phone", vendor.getPhone());
						row.put("grantAccess", vendor.getGrantAccess());
						if (vendor.getValidityFrom() != null) {
							row.put("validFrom", idosdf.format(vendor.getValidityFrom()));
						}
						if (vendor.getValidityTo() != null) {
							row.put("validTo", idosdf.format(vendor.getValidityTo()));
						}
						if (userRolesStr.indexOf("MASTER ADMIN") != -1
								|| (userRolesStr.indexOf("APPROVER") != -1 && user.canActivateVendor())) {
							row.put("presentStatus", vendor.getPresentStatus());
						} else {
							row.put("presentStatus", "-1");
						}
						serachVendorData.add(row);
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	/*
	 * @Transactional
	 * public Result uploadVendors(){
	 * log.log(Level.FINE, ">>>> Start");
	 * ObjectNode result = Json.newObject();
	 * ArrayNode an = result.putArray("successUploading");
	 * Users user=null;
	 * EntityManager entityManager=jpaApi.em();
	 * EntityTransaction transaction=entityManager.getTransaction();
	 * try{
	 * transaction.begin();
	 * MultipartFormData body = request.body().asMultipartFormData();
	 * user=getUserInfo(request);
	 * List<FilePart> chartofaccount = body.getFiles();
	 * Map<String, Object> criterias = new HashMap<String, Object>();
	 * for(FilePart filePart:chartofaccount){
	 * String fileName = filePart.getFilename();
	 * String contentType = filePart.getContentType();
	 * File file = filePart.getFile();
	 * InputStream is = new java.io.FileInputStream(file);
	 * try{
	 * XSSFWorkbook wb = new XSSFWorkbook(is);
	 * int numOfSheets = wb.getNumberOfSheets();
	 * for (int i = 0; i < numOfSheets; i++) {
	 * XSSFSheet sheet = wb.getSheetAt(i);
	 * Iterator rows = sheet.rowIterator();
	 * while(rows.hasNext()) {
	 * XSSFRow row = (XSSFRow) rows.next();
	 * String name=row.getCell(0)!=null?row.getCell(0).toString():null;
	 * String location=row.getCell(1)!=null?row.getCell(1).toString():null;
	 * String email=row.getCell(2)!=null?row.getCell(2).toString():null;
	 * if(row.getCell(3)!=null){
	 * row.getCell(3).setCellType(Cell.CELL_TYPE_STRING);
	 * }
	 * String phoneNumber=row.getCell(3)!=null?row.getCell(3).toString():null;
	 * String address=row.getCell(4)!=null?row.getCell(4).toString():null;
	 * log.log(Level.INFO, address);
	 * criterias.clear();
	 * criterias.put("name", name);
	 * criterias.put("organization.id", user.getOrganization().getId());
	 * criterias.put("type", 1);
	 * List<Vendor> existVend=genericDAO.findByCriteria(Vendor.class, criterias,
	 * entityManager);
	 * if(existVend.size()==0){
	 * if(name !=null && !name.equals("")){
	 * Vendor newVendor=new Vendor();
	 * newVendor.setName(name);
	 * newVendor.setLocation(location);
	 * newVendor.setEmail(email);
	 * newVendor.setPhone(phoneNumber);
	 * newVendor.setAddress(address);
	 * newVendor.setBranch(user.getBranch());
	 * newVendor.setOrganization(user.getOrganization());
	 * newVendor.setType(1);
	 * if(!user.getUserRolesName().contains("MASTER ADMIN")){
	 * newVendor.setPresentStatus(0);
	 * }else{
	 * newVendor.setPresentStatus(1);
	 * }
	 * genericDAO.saveOrUpdate(newVendor, user, entityManager);
	 * }
	 * }
	 * }
	 * }
	 * transaction.commit();
	 * }catch(Exception ex){
	 * if(transaction.isActive()){
	 * transaction.rollback();
	 * }
	 * log.log(Level.SEVERE, "Error", ex);
	 * String strBuff=getStackTraceMessage(ex);
	 * expService.sendExceptionReport(strBuff,user.getEmail(),
	 * user.getOrganization().getName(),
	 * Thread.currentThread().getStackTrace()[1].getMethodName());
	 * }
	 * }
	 * ObjectNode row = Json.newObject();
	 * row.put("message", "Uploaded Successfully");
	 * an.add(row);
	 * }catch(Exception ex){
	 * if(transaction.isActive()){
	 * transaction.rollback();
	 * }
	 * log.log(Level.SEVERE, user.getEmail(), ex);
	 * String strBuff=getStackTraceMessage(ex);
	 * expService.sendExceptionReport(strBuff,user.getEmail(),
	 * user.getOrganization().getName(),
	 * Thread.currentThread().getStackTrace()[1].getMethodName());
	 * List<String> errorList=getStackTrace(ex);
	 * return Results.ok(errorPage.render(ex,errorList));
	 * }
	 * log.log(Level.FINE, ">>>> End " + result);
	 * return Results.ok(result);
	 * }
	 */
	@Transactional
	public Result vendorCustomerAccountLogin(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode vendorCustActan = result.putArray("vendorCustActData");
			String email = json.findValue("usermail").asText();
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			String selectedCustVend = json.findValue("selectedCustVend").asText();
			Vendor entityVendor = Vendor.findById(IdosUtil.convertStringToLong(selectedCustVend));
			String elemText = json.findValue("elemText").asText();
			String vendCustPassword = null;
			if (elemText.equals("Grant Access")) {
				// send mail to customer vendor with the access link containing vendor customer
				// emailand organization parameters
				entityVendor.setGrantAccess(1);
				vendCustPassword = PasswordUtil.gen(10);
				entityVendor.setAccessPassword(PasswordUtil.encrypt(vendCustPassword));
			} else if (elemText.equals("Remove Access")) {
				entityVendor.setGrantAccess(0);
				entityVendor.setAccessPassword(vendCustPassword);
			}
			genericDAO.saveOrUpdate(entityVendor, user, entityManager);
			transaction.commit();
			if (elemText.equals("Grant Access")) {
				String type = "";
				if (entityVendor.getType() == 1) {
					type = "Vendor";
				} else if (entityVendor.getType() == 2) {
					type = "Customer";
				}
				String body = vendorCustomerAccountAccessLink
						.render(vendCustPassword, entityVendor.getEmail(), user.getOrganization().getName(),
								user.getOrganization().getId().toString(), type, ConfigParams.getInstance())
						.body();
				final String username = ConfigFactory.load().getString("smtp.user");
				Session session = emailsession;
				String subject = "Vendor/Customer Account Access Link for Organization: "
						+ user.getOrganization().getName() + "";
				mailTimer(body, username, session, entityVendor.getEmail(), null, subject);
			}
			ObjectNode row = Json.newObject();
			row.put("grantAccess", entityVendor.getGrantAccess());
			vendorCustActan.add(row);
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getVendorCustomerTransactions(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode userTxnData = result.putArray("userTxnData");
			ArrayNode totalOutstandingsData = result.putArray("totalOutstandingsData");
			String vendcustaccountOrganization = json.findPath("vendcustaccountOrganization").asText();
			String vendcustaccountEmail = json.findPath("vendcustaccountEmail").asText();
			String vendcustentityType = json.findPath("vendcustentityType").asText();
			Organization org = Organization.findById(IdosUtil.convertStringToLong(vendcustaccountOrganization));
			Integer type = null;
			if (vendcustentityType.equals("Vendor")) {
				type = 1;
			}
			if (vendcustentityType.equals("Customer")) {
				type = 2;
			}
			Map<Object, Object> criteria = new HashMap<Object, Object>();
			criteria.clear();
			criteria.put("email", vendcustaccountEmail);
			criteria.put("organization.id", org.getId());
			criteria.put("type", type);
			criteria.put("presentStatus", 1);
			List<Vendor> custvendors = genericDAO.findByCriteria(Vendor.class, criteria, entityManager);
			Vendor vendorCustomer = null;
			if (custvendors != null && custvendors.size() > 0) {
				vendorCustomer = custvendors.get(0);
			}
			List<Transaction> vendorCustomerTransactions = vendorCustomer.getVendorCustomerTransactions();
			Double currentOutstandings = 0.0;
			Double outstandingVendorSpecialAdjustments = 0.0;
			Double receivedSpecialAdjustments = 0.0;
			Double paidSpecialAdjustments = 0.0;
			for (Transaction vendCustTxn : vendorCustomerTransactions) {
				if (vendCustTxn.getTransactionStatus().equals("Accounted")) {
					ObjectNode event = Json.newObject();
					event.put("id", vendCustTxn.getId());
					if (vendCustTxn.getTransactionBranch() != null) {
						event.put("branchName", vendCustTxn.getTransactionBranch().getName());
					} else {
						event.put("branchName", "");
					}
					if (null != vendCustTxn.getCreatedBy()) {
						event.put("email", vendCustTxn.getCreatedBy().getEmail());
					}
					if (vendCustTxn.getTransactionProject() != null) {
						event.put("projectName", vendCustTxn.getTransactionProject().getName());
					} else {
						event.put("projectName", "");
					}
					if (vendCustTxn.getTransactionSpecifics() != null) {
						event.put("itemName", vendCustTxn.getTransactionSpecifics().getName());
					} else {
						event.put("itemName", "");
					}
					String transactionVendCustOutstandings = "";
					if (vendCustTxn.getTransactionPurpose().getId() == 1L) {
						if (vendCustTxn.getCustomerNetPayment() != null) {
							transactionVendCustOutstandings += "Amount Received On This Invoice:"
									+ vendCustTxn.getCustomerNetPayment();
						} else {
							transactionVendCustOutstandings += "Amount Received On This Invoice:"
									+ vendCustTxn.getNetAmount();
						}
						if (vendCustTxn.getCustomerDuePayment() != null) {
							currentOutstandings += vendCustTxn.getCustomerDuePayment();
							transactionVendCustOutstandings += ",Amount Due On This Invoice:"
									+ vendCustTxn.getCustomerDuePayment();
						} else {
							currentOutstandings += 0.0;
							transactionVendCustOutstandings += ",Amount Due On This Invoice:" + 0.0;
						}
					}
					if (vendCustTxn.getTransactionPurpose().getId() == 2L) {
						if (vendCustTxn.getCustomerNetPayment() != null) {
							transactionVendCustOutstandings += "Amount Received On This Invoice:"
									+ vendCustTxn.getCustomerNetPayment();
						} else {
							transactionVendCustOutstandings += "Amount Received On This Invoice:" + 0.0;
						}
						if (vendCustTxn.getCustomerDuePayment() != null) {
							currentOutstandings += vendCustTxn.getCustomerDuePayment();
							transactionVendCustOutstandings += ",Amount Due On This Invoice:"
									+ vendCustTxn.getCustomerDuePayment();
						} else {
							currentOutstandings += vendCustTxn.getNetAmount();
							transactionVendCustOutstandings += ",Amount Due On This Invoice:"
									+ vendCustTxn.getNetAmount();
						}
					}
					if (vendCustTxn.getTransactionPurpose().getId() == 3L) {
						if (vendCustTxn.getVendorNetPayment() != null) {
							transactionVendCustOutstandings += "Amount Paid On This Invoice:"
									+ vendCustTxn.getVendorNetPayment();
						} else {
							transactionVendCustOutstandings += "Amount Paid On This Invoice:"
									+ vendCustTxn.getNetAmount();
						}
						if (vendCustTxn.getVendorDuePayment() != null) {
							currentOutstandings += vendCustTxn.getVendorDuePayment();
							transactionVendCustOutstandings += ",Amount Due On This Invoice:"
									+ vendCustTxn.getVendorDuePayment();
						} else {
							currentOutstandings += 0.0;
							transactionVendCustOutstandings += ",Amount Due On This Invoice:" + 0.0;
						}
					}
					if (vendCustTxn.getTransactionPurpose().getId() == 4L) {
						if (vendCustTxn.getVendorNetPayment() != null) {
							transactionVendCustOutstandings += "Amount Paid On This Invoice:"
									+ vendCustTxn.getVendorNetPayment();
						} else {
							transactionVendCustOutstandings += "Amount Paid On This Invoice:" + 0.0;
						}
						if (vendCustTxn.getVendorDuePayment() != null) {
							currentOutstandings += vendCustTxn.getVendorDuePayment();
							transactionVendCustOutstandings += ",Amount Due On This Invoice:"
									+ vendCustTxn.getVendorDuePayment();
						} else {
							currentOutstandings += vendCustTxn.getNetAmount();
							transactionVendCustOutstandings += ",Amount Due On This Invoice:"
									+ vendCustTxn.getNetAmount();
						}
					}
					if (vendCustTxn.getTransactionPurpose().getId() == 5L) {
						if (vendCustTxn.getCustomerNetPayment() != null) {
							transactionVendCustOutstandings += "Amount Received On This Invoice:"
									+ vendCustTxn.getCustomerNetPayment();
						} else {
							transactionVendCustOutstandings += "Amount Received On This Invoice:"
									+ vendCustTxn.getNetAmount();
						}
						if (vendCustTxn.getCustomerDuePayment() != null) {
							currentOutstandings += vendCustTxn.getCustomerDuePayment();
							transactionVendCustOutstandings += ",Amount Due On This Invoice:"
									+ vendCustTxn.getCustomerDuePayment();
						} else {
							currentOutstandings += 0.0;
							transactionVendCustOutstandings += ",Amount Due On This Invoice:" + 0.0;
						}
					}
					if (vendCustTxn.getTransactionPurpose().getId() == 6L) {
						if (vendCustTxn.getCustomerNetPayment() != null) {
							transactionVendCustOutstandings += "Amount Received On This Invoice:"
									+ vendCustTxn.getCustomerNetPayment();
						} else {
							transactionVendCustOutstandings += "Amount Received On This Invoice:"
									+ vendCustTxn.getNetAmount();
						}
						if (vendCustTxn.getCustomerDuePayment() != null) {
							currentOutstandings += vendCustTxn.getCustomerDuePayment();
							transactionVendCustOutstandings += ",Amount Due On This Invoice:"
									+ vendCustTxn.getCustomerDuePayment();
						} else {
							currentOutstandings += 0.0;
							transactionVendCustOutstandings += ",Amount Due On This Invoice:" + 0.0;
						}
					}
					if (vendCustTxn.getTransactionPurpose().getId() == 7L) {
						if (vendCustTxn.getVendorNetPayment() != null) {
							transactionVendCustOutstandings += "Amount Paid On This Invoice:"
									+ vendCustTxn.getVendorNetPayment();
						} else {
							transactionVendCustOutstandings += "Amount Paid On This Invoice:"
									+ vendCustTxn.getNetAmount();
						}
						if (vendCustTxn.getVendorDuePayment() != null) {
							currentOutstandings += vendCustTxn.getVendorDuePayment();
							transactionVendCustOutstandings += ",Amount Due On This Invoice:"
									+ vendCustTxn.getVendorDuePayment();
						} else {
							currentOutstandings += 0.0;
							transactionVendCustOutstandings += ",Amount Due On This Invoice:" + 0.0;
						}
					}
					if (vendCustTxn.getTransactionPurpose().getId() == 8L) {
						if (vendCustTxn.getVendorNetPayment() != null) {
							transactionVendCustOutstandings += "Amount Paid On This Invoice:"
									+ vendCustTxn.getVendorNetPayment();
						} else {
							transactionVendCustOutstandings += "Amount Paid On This Invoice:"
									+ vendCustTxn.getNetAmount();
						}
						if (vendCustTxn.getVendorDuePayment() != null) {
							currentOutstandings += vendCustTxn.getVendorDuePayment();
							transactionVendCustOutstandings += ",Amount Due On This Invoice:"
									+ vendCustTxn.getVendorDuePayment();
						} else {
							currentOutstandings += 0.0;
							transactionVendCustOutstandings += ",Amount Due On This Invoice:" + 0.0;
						}
					}
					if (vendCustTxn.getTransactionPurpose().getId() == 9L) {
						if (vendCustTxn.getNetAmount() != null) {
							receivedSpecialAdjustments = vendCustTxn.getNetAmount();
						}
					}
					if (vendCustTxn.getTransactionPurpose().getId() == 10L
							&& !vendCustTxn.getTransactionStatus().equals("Rejected")) {
						if (vendCustTxn.getNetAmount() != null) {
							paidSpecialAdjustments = vendCustTxn.getNetAmount();
						}
					}
					if (vendCustTxn.getTransactionPurpose().getId() == 12L) {
						if (vendCustTxn.getCustomerNetPayment() != null) {
							transactionVendCustOutstandings += "Amount Received On This Invoice:"
									+ vendCustTxn.getCustomerNetPayment();
						} else {
							transactionVendCustOutstandings += "Amount Received On This Invoice:"
									+ vendCustTxn.getNetAmount();
						}
						if (vendCustTxn.getCustomerDuePayment() != null) {
							currentOutstandings += vendCustTxn.getCustomerDuePayment();
							transactionVendCustOutstandings += ",Amount Due On This Invoice:"
									+ vendCustTxn.getCustomerDuePayment();
						} else {
							currentOutstandings += 0.0;
							transactionVendCustOutstandings += ",Amount Due On This Invoice:" + 0.0;
						}
					}
					if (vendCustTxn.getTransactionPurpose().getId() == 13L) {
						if (vendCustTxn.getVendorNetPayment() != null) {
							transactionVendCustOutstandings += "Amount Paid On This Invoice:"
									+ vendCustTxn.getVendorNetPayment();
						} else {
							transactionVendCustOutstandings += "Amount Paid On This Invoice:"
									+ vendCustTxn.getNetAmount();
						}
						if (vendCustTxn.getVendorDuePayment() != null) {
							currentOutstandings += vendCustTxn.getVendorDuePayment();
							transactionVendCustOutstandings += ",Amount Due On This Invoice:"
									+ vendCustTxn.getVendorDuePayment();
						} else {
							currentOutstandings += 0.0;
							transactionVendCustOutstandings += ",Amount Due On This Invoice:" + 0.0;
						}
					}
					event.put("transactionVendCustOutstandings", transactionVendCustOutstandings);
					event.put("transactionPurpose", vendCustTxn.getTransactionPurpose().getTransactionPurpose());
					event.put("transactionPurposeID", vendCustTxn.getTransactionPurpose().getId());
					event.put("txnDate", idosdf.format(vendCustTxn.getTransactionDate()));
					String invoiceDate = "";
					String invoiceDateLabel = "";
					if (vendCustTxn.getTransactionInvoiceDate() != null) {
						invoiceDateLabel = "INVOICE DATE:";
						invoiceDate = idosdf.format(vendCustTxn.getTransactionInvoiceDate());
					}
					event.put("invoiceDateLabel", invoiceDateLabel);
					event.put("invoiceDate", invoiceDate);
					if (vendCustTxn.getReceiptDetailsType() != null) {
						if (vendCustTxn.getReceiptDetailsType() == 1) {
							event.put("paymentMode", "CASH");
						}
						if (vendCustTxn.getReceiptDetailsType() == 2) {
							event.put("paymentMode", "BANK");
						}
					} else {
						event.put("paymentMode", "");
					}
					if (vendCustTxn.getNoOfUnits() != null) {
						event.put("noOfUnit", vendCustTxn.getNoOfUnits());
					} else {
						event.put("noOfUnit", "");
					}
					if (vendCustTxn.getPricePerUnit() != null) {
						event.put("unitPrice", vendCustTxn.getPricePerUnit());
					} else {
						event.put("unitPrice", "");
					}
					if (vendCustTxn.getGrossAmount() != null) {
						event.put("grossAmount", vendCustTxn.getGrossAmount());
					} else {
						event.put("grossAmount", "");
					}
					if (vendCustTxn.getVendCustRemarks() != null) {
						event.put("vendCustRemarks", vendCustTxn.getVendCustRemarks());
					} else {
						event.put("vendCustRemarks", "");
					}
					event.put("netAmount", vendCustTxn.getNetAmount());
					if (vendCustTxn.getNetAmountResultDescription() != null
							&& !vendCustTxn.getNetAmountResultDescription().equals("null")) {
						event.put("netAmtDesc", vendCustTxn.getNetAmountResultDescription());
					} else {
						event.put("netAmtDesc", "");
					}
					event.put("status", vendCustTxn.getTransactionStatus());
					event.put("vendCustAcceptence", vendCustTxn.getVendCustAcceptence());
					userTxnData.add(event);
				}
			}
			outstandingVendorSpecialAdjustments = receivedSpecialAdjustments - paidSpecialAdjustments;
			ObjectNode outstandings = Json.newObject();
			outstandings.put("type", vendorCustomer.getType());
			outstandings.put("currentOutstandings", currentOutstandings);
			outstandings.put("outstandingVendorSpecialAdjustments", outstandingVendorSpecialAdjustments);
			totalOutstandingsData.add(outstandings);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "GetVendorCustomerTransactions Email",
					"GetVendorCustomerTransactions Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getBranchProject(Request request, final Long orgId) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		try {
			transaction.begin();
			Cookie cookie = request.cookie("vendorCustomer");
			if (null == cookie) {
				result.put("message", "Request Denied!");
			} else {
				String email = cookie.value();
				if (null == email || "".equals(email)) {
					result.put("message", "Request Denied!");
				} else {
					if (null == orgId || orgId.equals(0)) {
						result.put("message", "Cannot find the organization!");
					} else {
						Organization organization = Organization.findById(orgId);
						if (null == organization) {
							result.put("message", "Cannot find the organization!");
						} else {
							ArrayNode datas = result.putArray("branch");
							ObjectNode data = dashboardService.getBranchesOrProjectsOrOperationalData(organization, 2,
									entityManager);
							datas.add(data);
							data = dashboardService.getBranchesOrProjectsOrOperationalData(organization, 4,
									entityManager);
							datas = result.putArray("project");
							datas.add(data);
							result.put("result", true);
							result.remove("message");
						}
					}
				}
			}
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "VendorController BranchProject", "VendorController BranchProject",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getSpecifics(Request request, final Long orgId, final Integer type) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		try {
			Cookie cookie = request.cookie("vendorCustomer");
			if (null == cookie) {
				result.put("message", "Request Denied!");
			} else {
				String email = cookie.value();
				if (null == email || "".equals(email)) {
					result.put("message", "Request Denied!");
				} else {
					result = VENDOR_SERVICE.getSpecifics(orgId, type);
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "VendorController BranchProject", "VendorController BranchProject",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result search(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		// EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = null;
		try {
			// transaction.begin();
			JsonNode json = request.body().asJson();
			long id = json.findValue("id").asLong();
			int type = json.findValue("id").asInt();
			double fromAmount = json.findValue("fromAmount").asLong();
			double toAmount = json.findValue("toAmount").asLong();
			long txnType = json.findValue("txnType").asLong();
			long category = json.findValue("category").asLong();
			long item = json.findValue("item").asLong();
			long branch = json.findValue("branch").asLong();
			long project = json.findValue("project").asLong();
			String txnRefNumber = json.findValue("txnRefNumber").asText().trim();
			String fromDate = json.findValue("fromDate").asText().trim();
			String toDate = json.findValue("toDate").asText().trim();
			String status = json.findValue("status").asText().trim();
			result = VENDOR_SERVICE.search(id, type, txnRefNumber, txnType, category, item, fromDate, toDate, branch,
					project, fromAmount, toAmount, status);
			// transaction.commit();
		} catch (Exception ex) {
			// if (transaction.isActive()) {
			// transaction.rollback();
			// }
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "VendorController BranchProject", "VendorController BranchProject",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result statements(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		// EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = null;
		try {
			// transaction.begin();
			JsonNode json = request.body().asJson();
			long id = json.findValue("id").asLong();
			String type = json.findValue("type").asText();
			long org = json.findValue("org").asLong();
			long branch = json.findValue("branch").asLong();
			String from = json.findValue("from").asText();
			String to = json.findValue("to").asText();
			int getType = (null == json.findValue("getType")) ? 1 : json.findValue("getType").asInt();
			if (2 == getType || 3 == getType) {
				// Download
				String fileType = (2 == getType) ? "xlsx" : "pdf";
				String branchName = (null == json.findValue("branchName")) ? "" : json.findValue("branchName").asText();
				result = VENDOR_SERVICE.exportStatements(id, type, org, branch, from, to, getType, branchName,
						fileType);
			} else {
				// Search
				result = VENDOR_SERVICE.getStatements(id, type, org, branch, from, to, getType);
			}
			// transaction.commit();
		} catch (Exception ex) {
			// if (transaction.isActive()) {
			// transaction.rollback();
			// }
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "VendorController BranchProject", "VendorController BranchProject",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result changeStatus(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		Users user = getUserInfo(request);
		if (user == null) {
			return unauthorized();
		}
		try {
			transaction.begin();
			StringBuilder sbr = new StringBuilder("");
			JsonNode json = request.body().asJson();
			long elementId = json.findValue("origEntityId").asLong();
			int presentStatus = json.findValue("presentStatus") == null ? 0 : json.findValue("presentStatus").asInt();

			Vendor vendor = Vendor.findById(elementId);
			vendor.setPresentStatus(presentStatus);
			vendcrud.save(user, vendor, entityManager);
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result vendorLogout(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		String fullName = null;
		String role = null;
		String email = null;
		String pwdchangemsg = null;
		String allowedProcurement = null;
		Organization orgn = null;
		ObjectNode results = Json.newObject();
		try {
			String accountEmail = session.getOptional("vendoremail").orElse("");
			String entityType = session.getOptional("entityType").orElse("");
			int type = 1;
			if (entityType.equals("Vendor")) {
				type = 1;
			}
			if (entityType.equals("Customer")) {
				type = 2;
			}
			Map<Object, Object> criteria = new HashMap<Object, Object>();
			criteria.clear();
			criteria.put("email", accountEmail);
			criteria.put("type", type);
			criteria.put("presentStatus", 1);
			List<Vendor> custvendors = genericDAO.findByCriteria(Vendor.class, criteria, entityManager);
			if (custvendors != null && custvendors.size() > 0) {
				entitytransaction.begin();
				Vendor vend = custvendors.get(0);
				vend.setRegVendInSession(0);
				vend.setAuthToken(null);
				genericDAO.saveOrUpdate(vend, null, entityManager);
				entitytransaction.commit();
				results.put("logout", vend.getEmail());
			}
			CookieUtils.discardCookie("user");
			CookieUtils.discardCookie(IdosConstants.AUTH_TOKEN);
			session.removing();
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "sellerLogout Email", "sellerLogout Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End");
		// return
		// Results.ok(logoutsllr.render(fullName,email,role,orgn,allowedProcurement));

		// log.log(Level.FINE, ">>>> End");
		return Results.ok(results);
	}

	@Transactional
	public Result getVendorLocations(Request request) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode custGstinList = result.putArray("custGstinList");
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
			Long txnvendorId = json.findValue("txnVendorId").asLong();
			Vendor vendor = Vendor.findById(txnvendorId);
			if (vendor != null) {
				if (vendor.getIsBusiness() != null) {
					result.put("vendorBusinessType", vendor.getIsBusiness());
				} else {
					result.put("vendorBusinessType", "");
				}
				List<VendorDetail> vendorGstinList = vendor.getVendorDetails();
				for (VendorDetail vendorGstin : vendorGstinList) {
					if (vendorGstin.getPresentStatus() == 0) {
						continue;
					}
					ObjectNode row = Json.newObject();
					row.put("vendorDetailId", vendorGstin.getId());

					String shipState = vendorGstin.getCountryState();
					String gstin = vendorGstin.getGstin();
					String custLocation = vendorGstin.getLocation();
					if (custLocation == null || "".equals(custLocation)) {
						custLocation = "";
					}
					if (gstin != null) {
						custLocation = custLocation + "-" + gstin;
						row.put("gstin", gstin);
					} else {
						row.put("gstin", "");
					}
					if (shipState != null && !"".equals(shipState)) {
						custLocation = custLocation + "-" + shipState;
					}
					row.put("custLocation", custLocation);
					custGstinList.add(row);
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getWalkinVendorLocations(String name, Request request) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode cutomerList = result.putArray("cutomerList");
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
			int idx = 1;
			if (name != null) {
				name = name.toUpperCase();
			}
			List<Vendor> customerList = Vendor.findListByOrgIdAndTypeName(entityManager, user.getOrganization().getId(),
					IdosConstants.WALK_IN_VENDOR, name);
			for (Vendor customer : customerList) {
				List<VendorDetail> customerGstinList = customer.getVendorDetails();
				if (customerGstinList != null && customerGstinList.size() > 0) {
					if (customer.getPresentStatus() == 0) {
						continue;
					}
					ObjectNode row = Json.newObject();
					row.put("label", customer.getName());

					// row.put("customerDetailId", customer.getId());
					String shipState = customerGstinList.get(0).getCountryState();
					String gstin = customer.getGstin();
					String custLocation = customerGstinList.get(0).getLocation();
					if (custLocation == null || "".equals(custLocation)) {
						custLocation = "";
					}
					if (gstin != null) {
						custLocation = custLocation + "-" + gstin;
					} else {
						gstin = "";
					}
					if (shipState != null && !"".equals(shipState)) {
						custLocation = custLocation + "-" + shipState;
					}
					// row.put("custLocation", custLocation);
					row.put("value", custLocation);
					row.put("idx", gstin);
					cutomerList.add(row);
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	/**
	 * Added by Firdous on 27-11-2017
	 * Method to download vendor template
	 * 
	 * @return
	 */
	@Transactional
	public Result downloadOrgVendorTemplate(Request request) {
		log.log(Level.FINE, ">>>> Start inside download org vendor");
		DataFormatter df = new DataFormatter();
		// EntityManager entityManager = getEntityManager();
		File file = null;
		Users user = null;
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			String orgName = user.getOrganization().getName().replaceAll("\\s", "");
			String fileName = orgName + "_Vendor_Template.xlsx";
			String sheetName = "Vendors";
			String path = application.path().toString() + "/logs/OrgVendors/";
			File filepath = new File(path);
			if (!filepath.exists()) {
				filepath.mkdir();
			}
			path = path + fileName;
			excelService.createOrgVendorTemplateExcel(user, entityManager, path, sheetName);
			file = new File(path);
			return Results.ok(file).withHeader("ContentType", "application/xlsx").withHeader("Content-Disposition",
					"attachment; filename=" + fileName);
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
	 * Added By Firdous on 07-12-2017
	 * Method to Upload Vendor details using xlsx template file
	 *
	 * @return
	 */

	@Transactional
	public Result uploadVendors(Request request) {
		log.log(Level.FINE, ">>>> Start uploading vendor");
		Map<String, Object> criterias = new HashMap<String, Object>();
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		ArrayNode an = result.putArray("successUploading");
		DataFormatter df = new DataFormatter();
		Users user = null;
		int batchSize = 50;
		int rowCount = 0;
		long totalRowsInserted = 0;
		int lastrownum = 0;
		StringBuilder itemsNotFound = new StringBuilder();
		StringBuilder branchNotFound = new StringBuilder();
		try {
			transaction.begin();
			MultipartFormData<File> body = request.body().asMultipartFormData();
			user = getUserInfo(request);
			List<FilePart<File>> chartofaccount = body.getFiles();

			for (FilePart<File> filePart : chartofaccount) {
				String fileName = filePart.getFilename();
				String contentType = filePart.getContentType();
				// File file = filePart.getRef();
				TemporaryFile temporaryFile = (TemporaryFile) filePart.getRef();
				String filePath = temporaryFile.path().toString();
				File file = new File(filePath);
				InputStream is = new java.io.FileInputStream(file);
				try {
					XSSFWorkbook wb = new XSSFWorkbook(is);
					int numOfSheets = wb.getNumberOfSheets();
					for (int i = 0; i < numOfSheets; i++) {
						XSSFSheet sheet = wb.getSheetAt(i);
						if (!"Vendors".equalsIgnoreCase(sheet.getSheetName())) {
							log.log(Level.FINE, "no sheets available");
							continue;
						}
						lastrownum = sheet.getLastRowNum();
						result.put("totalRowsInXls", lastrownum);
						Iterator rows = sheet.rowIterator();
						while (rows.hasNext()) {
							rowCount++;
							totalRowsInserted++;
							if (rowCount > 0 && rowCount % batchSize == 0) { // batch commit of 25
								entityManager.flush();
								entityManager.clear();

								transaction.commit();
								transaction.begin();
								rowCount = 0;
							}
							XSSFRow row = (XSSFRow) rows.next();
							if (row.getRowNum() == 0) {
								continue;
							}

							String branchList = row.getCell(0) != null ? row.getCell(0).toString() : null;
							String branchOpeningBalanceList = row.getCell(1) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(1)));
							String branchAdvanceOpeningBalanceList = row.getCell(2) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(2)));
							String vendorGroup = row.getCell(3) != null ? row.getCell(3).toString() : null;
							String name = row.getCell(4) != null ? row.getCell(4).toString() : null;
							String email = row.getCell(5) != null ? row.getCell(5).toString() : null;
							String isGstRegistered = row.getCell(6) != null ? row.getCell(6).toString() : null;
							String GSTINStateCode = row.getCell(7) == null ? null
									: ((XSSFCell) row.getCell(7)).toString();
							String GSTINCode = row.getCell(8) == null ? null : ((XSSFCell) row.getCell(8)).toString();
							String vendType = row.getCell(9) == null ? null : ((XSSFCell) row.getCell(9)).toString();
							String address = row.getCell(10) == null ? null : ((XSSFCell) row.getCell(10)).toString();
							String country = row.getCell(11) == null ? null : ((XSSFCell) row.getCell(11)).toString();
							String state = row.getCell(12) == null ? null : ((XSSFCell) row.getCell(12)).toString();
							String location = row.getCell(13) == null ? null : ((XSSFCell) row.getCell(13)).toString();
							String phoneCountryCode = row.getCell(14) == null ? null
									: ((XSSFCell) row.getCell(14)).toString();
							String phoneNo = row.getCell(15) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(15)));
							String purchaseItemsList = row.getCell(16) == null ? null
									: ((XSSFCell) row.getCell(16)).toString();
							String statutoryId1 = row.getCell(17) == null ? null
									: ((XSSFCell) row.getCell(17)).toString();
							String statutoryNo1 = row.getCell(18) == null ? null
									: ((XSSFCell) row.getCell(18)).toString();
							String statutoryId2 = row.getCell(19) == null ? null
									: ((XSSFCell) row.getCell(19)).toString();
							String statutoryNo2 = row.getCell(20) == null ? null
									: ((XSSFCell) row.getCell(20)).toString();
							String statutoryId3 = row.getCell(21) == null ? null
									: ((XSSFCell) row.getCell(21)).toString();
							String statutoryNo3 = row.getCell(22) == null ? null
									: ((XSSFCell) row.getCell(22)).toString();
							String statutoryId4 = row.getCell(23) == null ? null
									: ((XSSFCell) row.getCell(23)).toString();
							String statutoryNo4 = row.getCell(24) == null ? null
									: ((XSSFCell) row.getCell(24)).toString();
							String cashCredit = row.getCell(25) == null ? null
									: ((XSSFCell) row.getCell(25)).toString();
							String daysOfCredit = row.getCell(26) == null ? null
									: ((XSSFCell) row.getCell(26)).toString();
							/*
							 * String openingBal = row.getCell(27) == null ? null : ((XSSFCell)
							 * row.getCell(27)).toString();
							 * String openingBalOfAdv = row.getCell(28) == null ? null : ((XSSFCell)
							 * row.getCell(28)).toString();
							 */
							String GSTINStateCode2 = row.getCell(29) == null ? null
									: ((XSSFCell) row.getCell(29)).toString();
							String GSTINCode2 = row.getCell(30) == null ? null
									: ((XSSFCell) row.getCell(30)).toString();
							String address2 = row.getCell(31) == null ? null : ((XSSFCell) row.getCell(31)).toString();
							String country2 = row.getCell(32) == null ? null : ((XSSFCell) row.getCell(32)).toString();
							String state2 = row.getCell(33) == null ? null : ((XSSFCell) row.getCell(33)).toString();
							String location2 = row.getCell(34) == null ? null : ((XSSFCell) row.getCell(34)).toString();
							String phoneCountryCode2 = row.getCell(35) == null ? null
									: ((XSSFCell) row.getCell(35)).toString();
							String phoneNo2 = row.getCell(36) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(36)));
							String GSTINStateCode3 = row.getCell(37) == null ? null
									: ((XSSFCell) row.getCell(37)).toString();
							String GSTINCode3 = row.getCell(38) == null ? null
									: ((XSSFCell) row.getCell(38)).toString();
							String address3 = row.getCell(39) == null ? null : ((XSSFCell) row.getCell(39)).toString();
							String country3 = row.getCell(40) == null ? null : ((XSSFCell) row.getCell(40)).toString();
							String state3 = row.getCell(41) == null ? null : ((XSSFCell) row.getCell(41)).toString();
							String location3 = row.getCell(42) == null ? null : ((XSSFCell) row.getCell(42)).toString();
							String phoneCountryCode3 = row.getCell(43) == null ? null
									: ((XSSFCell) row.getCell(43)).toString();
							String phoneNo3 = row.getCell(44) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(44)));
							DataFormatter dataFormatter = new DataFormatter();
							String validityFrom = dataFormatter.formatCellValue(row.getCell(27));
							String validityTo = dataFormatter.formatCellValue(row.getCell(28));

							if (name == null || name.equals("") || name.equals("null")) {
								continue;
							}
							String GSTIN = "";
							String stateCode = IdosUtil.getStateCode(state);
							if (GSTINStateCode != null) {
								if (state == null || state == "null" || stateCode == null
										|| !stateCode.equals(GSTINStateCode)) {
									throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
											IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
											"Invalid GSTIN state. for vendor: " + name);
								}
								GSTIN = GSTINStateCode;
							} else if (state != null && stateCode != null) {
								GSTIN = stateCode;
							}

							if (GSTINCode != null) {
								GSTIN = GSTIN + GSTINCode;
							}

							Vendor vend = null;
							criterias.clear();
							criterias.put("name", name);
							criterias.put("organization.id", user.getOrganization().getId());
							criterias.put("type", 1);
							criterias.put("presentStatus", 1);
							List<Vendor> existVend = genericDAO.findByCriteria(Vendor.class, criterias, entityManager);
							if (existVend.size() == 0) {
								vend = new Vendor();
							} else {
								vend = existVend.get(0);
							}

							if (vendorGroup != null && !vendorGroup.equals("") && !vendorGroup.equals("null")) {
								List<VendorGroup> vendGroup = VendorGroup.findListByOrgIdAndName(entityManager,
										user.getOrganization().getId(), vendorGroup);
								if (vendGroup != null && vendGroup.size() > 0) {
									vend.setVendorGroup(vendGroup.get(0));
								}
							}
							vend.setType(1); // customer type
							vend.setName(name);
							vend.setEmail(email);
							vend.setAddress(address);
							vend.setLocation(location);
							vend.setPhoneCtryCode(phoneCountryCode);
							vend.setPhone(phoneNo);
							if (isGstRegistered != null && isGstRegistered != "") {
								if (isGstRegistered.equalsIgnoreCase("Yes")) {
									vend.setIsRegistered(1);
								} else {
									vend.setIsRegistered(0);
								}
							}
							if (vendType != null) {
								if (vendType.equalsIgnoreCase("Vendor is a business Establishment")) {
									vend.setIsBusiness(1);
								} else if (vendType.equalsIgnoreCase("Vendor is a individual Consumer")) {
									vend.setIsBusiness(2);
								}
							}
							vend.setGstin(GSTIN);
							int countryId = 0;
							if (country != null) {
								String counrtyId = CountryCurrencyUtil.getCountryId(country);
								if (counrtyId != null) {
									countryId = IdosUtil.convertStringToInt(counrtyId);
									vend.setCountry(countryId);
								}
							}

							vend.setCountryState(state);
							vend.setContractPoDoc("");

							if (statutoryId1 != null && !statutoryId1.equals("")) {
								vend.setStatutoryName1(statutoryId1);
							}
							if (statutoryNo1 != null && !statutoryNo1.equals("")) {
								vend.setStatutoryNumber1(statutoryNo1);
							}
							if (statutoryId2 != null && !statutoryId2.equals("")) {
								vend.setStatutoryName2(statutoryId2);
							}
							if (statutoryNo2 != null && !statutoryNo2.equals("")) {
								vend.setStatutoryNumber2(statutoryNo2);
							}
							if (statutoryId3 != null && !statutoryId3.equals("")) {
								vend.setStatutoryName3(statutoryId3);
							}
							if (statutoryNo3 != null && !statutoryNo3.equals("")) {
								vend.setStatutoryNumber3(statutoryNo3);
							}
							if (statutoryId4 != null && !statutoryId4.equals("")) {
								vend.setStatutoryName4(statutoryId4);
							}
							if (statutoryNo4 != null && !statutoryNo4.equals("")) {
								vend.setStatutoryNumber4(statutoryNo4);
							}

							if (daysOfCredit != null && !daysOfCredit.equals("null")) {
								Integer daysOfCreditInt = (int) IdosUtil.convertStringToDouble(daysOfCredit);
								vend.setDaysForCredit(daysOfCreditInt);
							}
							if (cashCredit != null && cashCredit != "null") {
								if (cashCredit.equalsIgnoreCase("Cash")) {
									vend.setPurchaseType(1);
								} else if (cashCredit.equalsIgnoreCase("Credit")) {
									vend.setPurchaseType(0);
								} else if (cashCredit.equalsIgnoreCase("Both")) {
									vend.setPurchaseType(2);
								}
							}

							Double totalOpeningBalance = 0.0;
							Double totalAdvanceOpeningBalance = 0.0;

							String branchListNames[] = branchList.split(",");
							if (branchOpeningBalanceList != null && !branchOpeningBalanceList.equals("null")) {
								String branchOpeningBalances[] = branchOpeningBalanceList.split(",");
								for (int j = 0; j < branchListNames.length; j++) {
									if (branchOpeningBalances[i] != null && !branchOpeningBalances[i].equals("")) {
										totalOpeningBalance += IdosUtil.convertStringToDouble(branchOpeningBalances[i]);
									}
								}
								vend.setTotalOriginalOpeningBalance(totalOpeningBalance);
								vend.setTotalOpeningBalance(totalOpeningBalance);
							} else {
								vend.setTotalOriginalOpeningBalance(0.0);
								vend.setTotalOpeningBalance(0.0);
							}
							if (branchAdvanceOpeningBalanceList != null && branchAdvanceOpeningBalanceList != ""
									&& !branchAdvanceOpeningBalanceList.equals("null")) {
								String branchAdvanceOpeningBalances[] = branchAdvanceOpeningBalanceList.split(",");
								for (int j = 0; j < branchListNames.length; j++) {
									totalAdvanceOpeningBalance += IdosUtil
											.convertStringToDouble(branchAdvanceOpeningBalances[i]);
								}
								vend.setTotalOpeningBalanceAdvPaid(totalAdvanceOpeningBalance);
								vend.setTotalOriginalOpeningBalanceAdvPaid(totalAdvanceOpeningBalance);
							} else {
								vend.setTotalOpeningBalanceAdvPaid(0.0);
								vend.setTotalOriginalOpeningBalanceAdvPaid(0.0);
							}

							try {
								if (validityFrom != null && !validityFrom.equals("")) {
									vend.setValidityFrom(IdosConstants.mysqldf.parse(
											IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(validityFrom))));
								}
								if (validityTo != null && !validityTo.equals("")) {
									vend.setValidityTo(IdosConstants.mysqldf.parse(
											IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(validityTo))));
								}
							} catch (java.text.ParseException ex) {
								log.log(Level.SEVERE, "Date cannot be parsed", ex);
								throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
										IdosConstants.TECHNICAL_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
										"Date cannot be parsed");
							}

							vend.setBranch(user.getBranch());
							vend.setOrganization(user.getOrganization());
							String specifics = "";
							if (purchaseItemsList != null && purchaseItemsList != ""
									&& !purchaseItemsList.equals("null")) {
								String[] purchaseItems = purchaseItemsList.split(",");
								for (int y = 0; y < purchaseItems.length; y++) {
									List<Specifics> specific = Specifics.findByNameAndHeadType(entityManager,
											user.getOrganization(), purchaseItems[y], "2");

									if (specific != null && specific.size() > 0) {
										if (y == purchaseItems.length - 1) {
											specifics = specifics + specific.get(0).getId();
										} else {
											specifics = specifics + specific.get(0).getId() + ",";
										}
									}
								}
								vend.setVendorSpecifics(specifics);

							}
							if (user.getUserRolesName().contains("MASTER ADMIN") || IdosConstants.SOLV
									.equalsIgnoreCase(ConfigParams.getInstance().getCompanyOwner())) {
								vend.setPresentStatus(1);
							} else {
								vend.setPresentStatus(0);
							}
							genericDAO.saveOrUpdate(vend, user, entityManager);

							// save CustomerDetail for Main GSTIN
							VendorDetail vendorDetail = VendorDetail.findByVendorGSTNID(entityManager, vend.getId(),
									GSTIN);
							if (vendorDetail == null) {
								vendorDetail = new VendorDetail();
							}
							vendorDetail.setVendor(vend);
							vendorDetail.setGstin(GSTIN);
							vendorDetail.setOrganization(user.getOrganization());
							vendorDetail.setCountry(countryId);
							vendorDetail.setLocation(location);
							vendorDetail.setPhoneCtryCode(phoneCountryCode);
							vendorDetail.setPhone(phoneNo);
							vendorDetail.setAddress(address);
							vendorDetail.setStateCode(stateCode);
							vendorDetail.setCountryState(state);

							genericDAO.saveOrUpdate(vendorDetail, user, entityManager);
							if (GSTINStateCode2 != null && GSTINCode2 != null) {
								String stateCode2 = IdosUtil.getStateCode(state2);
								if (state2 == null || state2 == "null" || !stateCode2.equals(GSTINStateCode2)) {
									throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
											IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
											"Invalid GSTIN state.");
								}
								String GSTIN2 = GSTINStateCode2 + GSTINCode2;
								VendorDetail vendorDetail2 = VendorDetail.findByVendorGSTNID(entityManager,
										vend.getId(), GSTIN2);
								if (vendorDetail2 == null) {
									vendorDetail2 = new VendorDetail();
								}
								vendorDetail2.setVendor(vend);
								vendorDetail2.setOrganization(user.getOrganization());
								vendorDetail2.setGstin(GSTIN2);
								vendorDetail2.setAddress(address2);
								vendorDetail2.setLocation(location2);
								vendorDetail2.setCountryState(state2);
								vendorDetail2.setStateCode(stateCode2);
								int countryId2 = 0;
								if (country2 != null) {
									String counrtyId2 = CountryCurrencyUtil.getCountryId(country2);
									if (counrtyId2 != null) {
										countryId2 = IdosUtil.convertStringToInt(counrtyId2);
										vendorDetail2.setCountry(countryId2);
									}
								}

								vendorDetail2.setPhoneCtryCode(phoneCountryCode2);

								vendorDetail2.setPhone(phoneNo2);
								genericDAO.saveOrUpdate(vendorDetail2, user, entityManager);
							}
							if (GSTINStateCode3 != null && GSTINCode3 != null) {
								String stateCode3 = IdosUtil.getStateCode(state3);
								if (state3 == null || state3 == "null" || !stateCode3.equals(GSTINStateCode3)) {
									throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
											IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
											"Invalid GSTIN state.");
								}
								String GSTIN3 = GSTINStateCode3 + GSTINCode3;
								VendorDetail vendorDetail3 = VendorDetail.findByVendorGSTNID(entityManager,
										vend.getId(), GSTIN3);
								if (vendorDetail3 == null) {
									vendorDetail3 = new VendorDetail();
								}
								vendorDetail3.setVendor(vend);
								vendorDetail3.setOrganization(user.getOrganization());
								vendorDetail3.setGstin(GSTIN3);
								vendorDetail3.setAddress(address3);
								vendorDetail3.setLocation(location3);
								vendorDetail3.setCountryState(state3);
								vendorDetail3.setStateCode(stateCode3);
								int countryId3 = 0;
								if (country3 != null) {
									String counrtyId3 = CountryCurrencyUtil.getCountryId(country3);
									if (counrtyId3 != null) {
										countryId3 = IdosUtil.convertStringToInt(counrtyId3);
										vendorDetail3.setCountry(countryId3);
									}
								}

								vendorDetail3.setPhoneCtryCode(phoneCountryCode3);

								vendorDetail3.setPhone(phoneNo3);
								genericDAO.saveOrUpdate(vendorDetail3, user, entityManager);
							}
							// customer other information start
							// branches
							String newVendBnchs[] = branchList.split(",");
							String branchOpeningBalances[] = null;
							String branchAdvanceOpeningBalances[] = null;

							if (branchOpeningBalanceList != null) {
								branchOpeningBalances = branchOpeningBalanceList.split(",");
								branchAdvanceOpeningBalances = branchAdvanceOpeningBalanceList.split(",");
							}
							for (int x = 0; x < newVendBnchs.length; x++) {
								if (!newVendBnchs[x].equals("")) {
									List<Branch> bnch = Branch.findListByOrgIdAndName(entityManager,
											user.getOrganization().getId(), newVendBnchs[x]);
									if (bnch != null && bnch.size() > 0) {
										BranchVendors newBnchVend = new BranchVendors();
										Branch branch = bnch.get(0);
										newBnchVend.setVendor(vend);
										newBnchVend.setBranch(branch);
										newBnchVend.setOrganization(branch.getOrganization());
										/*
										 * newBnchVend.setOpeningBalance(Double.parseDouble(branchOpeningBalances[i]));
										 * newBnchVend.setOpeningBalanceAdvPaid(Double.parseDouble(
										 * branchAdvanceOpeningBalances[i]));
										 */
										if (branchOpeningBalanceList != null) {
											if (i < branchOpeningBalances.length && branchOpeningBalances[i] != null
													&& !"".equals(branchOpeningBalances[i])) {
												newBnchVend.setOriginalOpeningBalance(
														IdosUtil.convertStringToDouble(branchOpeningBalances[x]));
												newBnchVend.setOpeningBalance(
														IdosUtil.convertStringToDouble(branchOpeningBalances[x]));
											} else {
												newBnchVend.setOriginalOpeningBalance(0.0);
												newBnchVend.setOpeningBalance(0.0);
											}
										} else {
											newBnchVend.setOriginalOpeningBalance(0.0);
											newBnchVend.setOpeningBalance(0.0);
										}
										if (branchAdvanceOpeningBalanceList != null) {
											if (i < branchAdvanceOpeningBalances.length
													&& branchAdvanceOpeningBalances[i] != null
													&& !"".equals(branchAdvanceOpeningBalances[i])) {
												newBnchVend.setOriginalOpeningBalanceAdvPaid(IdosUtil
														.convertStringToDouble(branchAdvanceOpeningBalances[x]));
												newBnchVend.setOpeningBalanceAdvPaid(IdosUtil
														.convertStringToDouble(branchAdvanceOpeningBalances[x]));
											} else {
												newBnchVend.setOriginalOpeningBalanceAdvPaid(0.0);
												newBnchVend.setOpeningBalanceAdvPaid(0.0);
											}
										} else {
											newBnchVend.setOriginalOpeningBalanceAdvPaid(0.0);
											newBnchVend.setOpeningBalanceAdvPaid(0.0);
										}
										genericDAO.saveOrUpdate(newBnchVend, user, entityManager);
									} else {
										branchNotFound.append(newVendBnchs[x]).append(", ");
									}
								}
							}
							// vendor specifics
							if (purchaseItemsList != null && purchaseItemsList != ""
									&& !purchaseItemsList.equals("null")) {
								String[] purchaseItems = purchaseItemsList.split(",");
								for (int y = 0; y < purchaseItems.length; y++) {
									List<Specifics> specific = Specifics.findByNameAndHeadType(entityManager,
											user.getOrganization(), purchaseItems[y], "2");
									if (specific != null && specific.size() > 0) {
										Specifics itemSpec = specific.get(0);
										VendorSpecific vendSpecf = new VendorSpecific();
										Particulars newVendParticulars = itemSpec.getParticularsId();
										vendSpecf.setVendorSpecific(vend);
										vendSpecf.setSpecificsVendors(itemSpec);
										vendSpecf.setBranch(user.getBranch());
										vendSpecf.setOrganization(user.getOrganization());
										vendSpecf.setParticulars(newVendParticulars);
										genericDAO.saveOrUpdate(vendSpecf, user, entityManager);
									} else {
										itemsNotFound.append(purchaseItems[y]).append(", ");
									}
								}
							}
						}
					}
					transaction.commit();
				} catch (RuntimeException e) {
					if (transaction != null && transaction.isActive()) {
						transaction.rollback();
					}
					throw e;
				} catch (Exception ex) {
					log.log(Level.SEVERE, "Error", ex);
					if (transaction != null && transaction.isActive()) {
						transaction.rollback();
					}
					throw ex;
				} finally {
					log.log(Level.INFO, "Total rows inserted " + totalRowsInserted);
					result.put("totalRowsInserted", (totalRowsInserted - 1));
					result.put("branchNotFound", branchNotFound.toString());
					result.put("itemsNotFound", itemsNotFound.toString());

				}
			}
			ObjectNode row = Json.newObject();
			an.add(row);
		} catch (Exception ex) {
			reportException(entityManager, transaction, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, transaction, user, th, result);
		}
		return Results.ok(result);
	}
}
