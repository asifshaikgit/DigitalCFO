package controllers;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

import javax.mail.Session;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.AlertMailLogs;
import model.Branch;
import model.BranchBankAccounts;
import model.BranchInsurance;
import model.ConfigParams;
import model.CountryPhoneCode;
import model.IdosChannelPartnerAlloteOrganization;
import model.IdosChannelPartnerCustomerOrganizationBranch;
import model.Organization;
import model.OrganizationKeyOfficials;
import model.OrganizationOperationalRemainders;
import model.Role;
import model.StatutoryDetails;
import model.Transaction;
import model.Users;
import model.UsersRoles;
import java.util.logging.Level;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.enumtype.BankAccountEnumType;
import com.idos.util.CountryCurrencyUtil;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import com.idos.util.PasswordUtil;

import actor.AdminActor;
import com.typesafe.config.ConfigFactory;
import play.db.jpa.JPAApi;
import play.mvc.Http.Request;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.WebSocket;
import play.mvc.Http;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.Application;
import play.libs.Files.TemporaryFile;
import javax.inject.Inject;
import akka.stream.javadsl.*;
import akka.actor.*;
import akka.NotUsed;
import views.html.*;

public class BranchController extends StaticController {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	public static Application application;
	private Request request;

	@Inject
	public BranchController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
		this.application = application;
	}

	@Transactional
	public Result branchPremiseEntityInformation(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// // EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			ArrayNode branchPremiseEntityInfo = result.putArray("branchPremiseEntityInfo");
			String organizationEntityId = json.findValue("organizationEntityId").asText();
			String branchEntityId = json.findValue("branchEntityId").asText();
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			Branch branch = genericDAO.getByCriteria(Branch.class, criterias, entityManager);
			if (branch != null) {
				ObjectNode row = Json.newObject();
				row.put("branchName", branch.getName());
				if (branch.getAggreement() != null) {
					row.put("premiseaggreement", branch.getAggreement());
				} else {
					row.put("premiseaggreement", "");
				}
				if (branch.getAggreementValidFrom() != null) {
					row.put("premiseAgreementValidityFrom", idosdf.format(branch.getAggreementValidFrom()));
				} else {
					row.put("premiseAgreementValidityFrom", "");
				}
				if (branch.getAggreementValidTo() != null) {
					row.put("premiseAgreementValidityTo", idosdf.format(branch.getAggreementValidTo()));
				} else {
					row.put("premiseAgreementValidityTo", "");
				}
				if (branch.getLastUpdatedValidityDate() != null) {
					row.put("premisePremiseValidityLastUpdated", "Branch Premise Validity Last Updated:"
							+ idosdf.format(branch.getLastUpdatedValidityDate()));
				} else {
					row.put("premisePremiseValidityLastUpdated", "");
				}
				if (branch.getPeriodicityOfPayment() != null) {
					row.put("premisePaymentPeriodicity", branch.getPeriodicityOfPayment());
				} else {
					row.put("premisePaymentPeriodicity", "");
				}
				if (branch.getRentPayable() != null) {
					row.put("premiseRentPayable", branch.getRentPayable());
				} else {
					row.put("premiseRentPayable", "");
				}
				if (branch.getLandlordName() != null) {
					row.put("premiseLandlordname", branch.getLandlordName());
				} else {
					row.put("premiseLandlordname", "");
				}
				if (branch.getLandlordAddress() != null) {
					row.put("premiseLandlordaddress", branch.getLandlordAddress());
				} else {
					row.put("premiseLandlordaddress", "");
				}
				if (branch.getBankAccountName() != null) {
					row.put("premiseLandlordbankaccountname", branch.getBankAccountName());
				} else {
					row.put("premiseLandlordbankaccountname", "");
				}
				if (branch.getBankAccountNumber() != null) {
					row.put("premiseLandlordbankaccountnumber", branch.getBankAccountNumber());
				} else {
					row.put("premiseLandlordbankaccountnumber", "");
				}
				if (branch.getBankAccountBranch() != null) {
					row.put("premiseLandlordbankaccountbranch", branch.getBankAccountBranch());
				} else {
					row.put("premiseLandlordbankaccountbranch", "");
				}
				if (branch.getLastUpdatedRentRevisionDate() != null) {
					row.put("premisePremiseLastRentRenewable", "Branch Premise Last Rent Renewable Date:"
							+ idosdf.format(branch.getLastUpdatedRentRevisionDate()));
				} else {
					row.put("premisePremiseLastRentRenewable", "");
				}
				if (branch.getLastRentPaidDueDated() != null) {
					row.put("branchPremiseLastPaymentDueDated",
							"Branch Premise Last Payment Due Date:" + idosdf.format(branch.getLastRentPaidDueDated()));
				} else {
					row.put("branchPremiseLastPaymentDueDated", "");
				}
				if (branch.getLastRentPaidDate() != null) {
					row.put("branchPremiseLastPaymentPaidDate",
							"Branch Premise Last Payment Paid Date:" + idosdf.format(branch.getLastRentPaidDate()));
				} else {
					row.put("branchPremiseLastPaymentPaidDate", "");
				}
				if (branch.getRentRevisedDueOn() != null) {
					row.put("premiseRentRevisionDueOn", idosdf.format(branch.getRentRevisedDueOn()));
				} else {
					row.put("premiseRentRevisionDueOn", "");
				}
				if (branch.getRemarks() != null) {
					row.put("premiseRemarks", branch.getRemarks());
				} else {
					row.put("premiseRemarks", "");
				}
				branchPremiseEntityInfo.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "BranchPremiseEntityInformation Email",
					"BranchPremiseEntityInformation Configuration",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result updateBranchPremiseValidity(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// // EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode alertmsg = result.putArray("alertmsg");
			String userEmail = json.findValue("userEmail").asText();
			userEmail = userEmail.replace("%40", "@");
			String organizationEntityId = json.findValue("organizationEntityId").asText();
			String branchEntityId = json.findValue("branchEntityId").asText();
			String branchtaskAlertGroupingDate = json.findValue("branchtaskAlertGroupingDate").asText();
			branchtaskAlertGroupingDate = branchtaskAlertGroupingDate.replace("%20", " ");
			branchtaskAlertGroupingDate = branchtaskAlertGroupingDate.replace("%2c", ",");
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("taskAlertGroupingDate",
					mysqldf.parse(mysqldf.format(idosdf.parse(branchtaskAlertGroupingDate))));
			criterias.put("alertType", 1);
			criterias.put("presentStatus", 1);
			List<AlertMailLogs> allAlertToBeConfirmed = genericDAO.findByCriteria(AlertMailLogs.class, criterias,
					entityManager);
			if (allAlertToBeConfirmed.size() > 0) {
				for (AlertMailLogs altMailLog : allAlertToBeConfirmed) {
					altMailLog.setConfirmationStatus("completed");
					genericDAO.saveOrUpdate(altMailLog, null, entityManager);
				}
			}
			criterias.clear();
			criterias.put("id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			Branch branch = genericDAO.getByCriteria(Branch.class, criterias, entityManager);
			criterias.clear();
			criterias.put("email", userEmail);
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			user = genericDAO.getByCriteria(Users.class, criterias, entityManager);
			String branchValidityFrom = json.findValue("branchValidityFrom").asText();
			String branchValidityTo = json.findValue("branchValidityTo").asText();
			String branchAgreemnet = json.findValue("branchAgreemnet").asText();
			if (branch != null) {
				// SEND VALIDITY UPDATE MAIL TO ALERT FOR INFORMATION EMAIL
				if (branch.getAlertForInformation() != null && !branch.getAlertForInformation().equals("")) {
					AlertMailLogs alertMailLogs = new AlertMailLogs();
					String body = branchPremiseUpdatedValidityInfAlert
							.render(branch.getName(), userEmail, branchValidityFrom, branchValidityTo,
									branch.getAlertForInformation(), branch.getOrganization().getId().toString(),
									branch.getId().toString(), ConfigParams.getInstance())
							.body();
					final String username = ConfigFactory.load().getString("smtp.user");
					Session session = emailsession;
					String subject = "Branch Premise Validity Update Information Alert For Organization: "
							+ branch.getOrganization().getName() + "";
					alertMailLogs.setMailFrom("beta@myidos.com");
					alertMailLogs.setMailTo(branch.getAlertForInformation());
					alertMailLogs.setMailSubject(subject);
					alertMailLogs.setBranch(branch);
					alertMailLogs.setOrganization(branch.getOrganization());
					genericDAO.saveOrUpdate(alertMailLogs, null, entityManager);
					mailTimer(body, username, session, branch.getAlertForInformation(), null, subject);
				}
				branch.setAggreementValidFrom(mysqldf.parse(mysqldf.format(idosdf.parse(branchValidityFrom))));
				branch.setAggreementValidTo(mysqldf.parse(mysqldf.format(idosdf.parse(branchValidityTo))));
				branch.setLastUpdatedValidityDate(mysqldf.parse(mysqldf.format(Calendar.getInstance().getTime())));
				if (branchAgreemnet != null && !branchAgreemnet.equals("")) {
					branch.setAggreement(branchAgreemnet);
				}
				genericDAO.saveOrUpdate(branch, user, entityManager);
				ObjectNode row = Json.newObject();
				row.put("success", branch.getName() + " Branch Premise Validity Updated Successfully");
				alertmsg.add(row);
				transaction.commit();
			}
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
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result updateBranchPremiseRentRenewalDate(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// // EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode alertmsg = result.putArray("alertmsg");
			String userEmail = json.findValue("userEmail").asText();
			userEmail = userEmail.replace("%40", "@");
			String organizationEntityId = json.findValue("organizationEntityId").asText();
			String branchEntityId = json.findValue("branchEntityId").asText();
			String branchtaskAlertGroupingDate = json.findValue("branchtaskAlertGroupingDate").asText();
			branchtaskAlertGroupingDate = branchtaskAlertGroupingDate.replace("%20", " ");
			branchtaskAlertGroupingDate = branchtaskAlertGroupingDate.replace("%2c", ",");
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("taskAlertGroupingDate",
					mysqldf.parse(mysqldf.format(idosdf.parse(branchtaskAlertGroupingDate))));
			criterias.put("alertType", 2);
			criterias.put("presentStatus", 1);
			List<AlertMailLogs> allAlertToBeConfirmed = genericDAO.findByCriteria(AlertMailLogs.class, criterias,
					entityManager);
			if (allAlertToBeConfirmed.size() > 0) {
				for (AlertMailLogs altMailLog : allAlertToBeConfirmed) {
					altMailLog.setConfirmationStatus("completed");
					genericDAO.saveOrUpdate(altMailLog, null, entityManager);
				}
			}
			criterias.clear();
			criterias.put("id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			Branch branch = genericDAO.getByCriteria(Branch.class, criterias, entityManager);
			criterias.clear();
			criterias.put("email", userEmail);
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			user = genericDAO.getByCriteria(Users.class, criterias, entityManager);
			String branchrentRenewalDue = json.findValue("branchrentRenewalDue").asText();
			String branchAgreemnet = json.findValue("branchAgreemnet").asText();
			if (branch != null) {
				// SEND VALIDITY UPDATE MAIL TO ALERT FOR INFORMATION EMAIL
				if (branch.getAlertForInformation() != null && !branch.getAlertForInformation().equals("")) {
					AlertMailLogs alertMailLogs = new AlertMailLogs();
					String body = branchPremiseUpdatedRentRenewalInfAlert
							.render(branch.getName(), userEmail, branchrentRenewalDue, branch.getAlertForInformation(),
									branch.getOrganization().getId().toString(), branch.getId().toString(),
									ConfigParams.getInstance())
							.body();
					final String username = ConfigFactory.load().getString("smtp.user");
					Session session = emailsession;
					String subject = "Branch Premise Rent Renewal Date Update Information Alert For Organization: "
							+ branch.getOrganization().getName() + "";
					alertMailLogs.setMailFrom("beta@myidos.com");
					alertMailLogs.setMailTo(branch.getAlertForInformation());
					alertMailLogs.setMailSubject(subject);
					alertMailLogs.setBranch(branch);
					alertMailLogs.setOrganization(branch.getOrganization());
					genericDAO.saveOrUpdate(alertMailLogs, null, entityManager);
					mailTimer(body, username, session, branch.getAlertForInformation(), null, subject);
				}
				branch.setRentRevisedDueOn(mysqldf.parse(mysqldf.format(idosdf.parse(branchrentRenewalDue))));
				branch.setLastUpdatedRentRevisionDate(mysqldf.parse(mysqldf.format(Calendar.getInstance().getTime())));
				if (branchAgreemnet != null && !branchAgreemnet.equals("")) {
					branch.setAggreement(branchAgreemnet);
				}
				genericDAO.saveOrUpdate(branch, user, entityManager);
				ObjectNode row = Json.newObject();
				row.put("success", branch.getName() + " Branch Premise Rent Renewal Date Updated Successfully");
				alertmsg.add(row);
				transaction.commit();
			}
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
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result confirmBranchPremiseRentPayment(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// // EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode alertmsg = result.putArray("alertmsg");
			String userEmail = json.findValue("userEmail").asText();
			userEmail = userEmail.replace("%40", "@");
			String organizationEntityId = json.findValue("organizationEntityId").asText();
			String branchEntityId = json.findValue("branchEntityId").asText();
			String branchtaskAlertGroupingDate = json.findValue("branchtaskAlertGroupingDate").asText();
			branchtaskAlertGroupingDate = branchtaskAlertGroupingDate.replace("%20", " ");
			branchtaskAlertGroupingDate = branchtaskAlertGroupingDate.replace("%2c", ",");
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("taskAlertGroupingDate",
					mysqldf.parse(mysqldf.format(idosdf.parse(branchtaskAlertGroupingDate))));
			criterias.put("alertType", 3);
			criterias.put("presentStatus", 1);
			List<AlertMailLogs> allAlertToBeConfirmed = genericDAO.findByCriteria(AlertMailLogs.class, criterias,
					entityManager);
			if (allAlertToBeConfirmed.size() > 0) {
				for (AlertMailLogs altMailLog : allAlertToBeConfirmed) {
					altMailLog.setConfirmationStatus("completed");
					genericDAO.saveOrUpdate(altMailLog, null, entityManager);
				}
			}
			criterias.clear();
			criterias.put("id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			Branch branch = genericDAO.getByCriteria(Branch.class, criterias, entityManager);
			criterias.clear();
			criterias.put("email", userEmail);
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			user = genericDAO.getByCriteria(Users.class, criterias, entityManager);
			String branchrentDueDated = json.findValue("branchrentDueDated").asText();
			branchrentDueDated = branchrentDueDated.replace("%20", " ");
			branchrentDueDated = branchrentDueDated.replace("%2c", ",");
			String branchAgreemnet = json.findValue("branchAgreemnet").asText();
			if (branch != null) {
				// SEND VALIDITY UPDATE MAIL TO ALERT FOR INFORMATION EMAIL
				if (branch.getAlertForInformation() != null && !branch.getAlertForInformation().equals("")) {
					AlertMailLogs alertMailLogs = new AlertMailLogs();
					String body = branchPremiseConfirmRentPaymentInfAlert
							.render(branch.getName(), userEmail, branchrentDueDated, branch.getAlertForInformation(),
									branch.getOrganization().getId().toString(), branch.getId().toString(),
									ConfigParams.getInstance())
							.body();
					final String username = ConfigFactory.load().getString("smtp.user");
					Session session = emailsession;
					String subject = "Branch Premise Rent Payment Confirmaton Alert For Organization: "
							+ branch.getOrganization().getName() + "";
					alertMailLogs.setMailFrom("beta@myidos.com");
					alertMailLogs.setMailTo(branch.getAlertForInformation());
					alertMailLogs.setMailSubject(subject);
					alertMailLogs.setBranch(branch);
					alertMailLogs.setOrganization(branch.getOrganization());
					genericDAO.saveOrUpdate(alertMailLogs, null, entityManager);
					mailTimer(body, username, session, branch.getAlertForInformation(), null, subject);
				}
				if (branchAgreemnet != null && !branchAgreemnet.equals("")) {
					branch.setAggreement(branchAgreemnet);
				}
				branch.setLastRentPaidDate(mysqldf.parse(mysqldf.format(Calendar.getInstance().getTime())));
				branch.setLastRentPaidDueDated(mysqldf.parse(mysqldf.format(idosdf.parse(branchrentDueDated))));
				genericDAO.saveOrUpdate(branch, user, entityManager);
				ObjectNode row = Json.newObject();
				row.put("success", branch.getName() + " Branch Premise Rent Payment Confirmed Successfully");
				alertmsg.add(row);
				transaction.commit();
			}
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
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result branchIndividualStatutoryInfo(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// // EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			ArrayNode branchStatutoryEntityInfo = result.putArray("branchStatutoryEntityInfo");
			String organizationEntityId = json.findValue("organizationEntityId").asText();
			String branchEntityId = json.findValue("branchEntityId").asText();
			String branchStatutoryEntityId = json.findValue("branchStatutoryEntityId").asText();
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("id", IdosUtil.convertStringToLong(branchStatutoryEntityId));
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			StatutoryDetails branchStatutory = genericDAO.getByCriteria(StatutoryDetails.class, criterias,
					entityManager);
			if (branchStatutory != null) {
				ObjectNode row = Json.newObject();
				row.put("branchName", branchStatutory.getBranch().getName());
				if (branchStatutory.getStatutoryDetails() != null) {
					row.put("statutoryDetails", branchStatutory.getStatutoryDetails());
				} else {
					row.put("statutoryDetails", "");
				}
				if (branchStatutory.getRegistrationNumber() != null) {
					row.put("statutoryRegistrationNumber", branchStatutory.getRegistrationNumber());
				} else {
					row.put("statutoryRegistrationNumber", "");
				}
				if (branchStatutory.getValidTo() != null) {
					row.put("branchStatutoryEndsOn", idosdf.format(branchStatutory.getValidTo()));
				} else {
					row.put("branchStatutoryEndsOn", "");
				}
				if (branchStatutory.getLastUpdatedValidityDate() != null) {
					row.put("branchStatutoryLastUpdatedDate", "Branch Statutory Validity Last Updated Date:"
							+ idosdf.format(branchStatutory.getLastUpdatedValidityDate()));
				} else {
					row.put("branchStatutoryLastUpdatedDate", "");
				}
				if (branchStatutory.getAlertForAction() != null) {
					row.put("statAltForAction", branchStatutory.getAlertForAction());
				} else {
					row.put("statAltForAction", "");
				}
				if (branchStatutory.getAlertForInformation() != null) {
					row.put("statAltForInformation", branchStatutory.getAlertForInformation());
				} else {
					row.put("statAltForInformation", "");
				}
				if (branchStatutory.getNameAddressOfConsultant() != null) {
					row.put("statConsultantNameAddress", branchStatutory.getNameAddressOfConsultant());
				} else {
					row.put("statConsultantNameAddress", "");
				}
				if (branchStatutory.getRemarks() != null) {
					row.put("statRemarks", branchStatutory.getRemarks());
				} else {
					row.put("statRemarks", "");
				}
				branchStatutoryEntityInfo.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "BranchIndividualStatutoryInfo Email",
					"BranchIndividualStatutoryInfo Configuration",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result updateBranchStatutoryValidity(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// // EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode alertmsg = result.putArray("alertmsg");
			String userEmail = json.findValue("userEmail").asText();
			userEmail = userEmail.replace("%40", "@");
			String organizationEntityId = json.findValue("organizationEntityId").asText();
			String branchEntityId = json.findValue("branchEntityId").asText();
			String branchStatutoryEntityId = json.findValue("branchStatutoryEntityId").asText();
			String branchtaskAlertGroupingDate = json.findValue("branchtaskAlertGroupingDate").asText();
			branchtaskAlertGroupingDate = branchtaskAlertGroupingDate.replace("%20", " ");
			branchtaskAlertGroupingDate = branchtaskAlertGroupingDate.replace("%2c", ",");
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("branchStatutory.id", IdosUtil.convertStringToLong(branchStatutoryEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("taskAlertGroupingDate",
					mysqldf.parse(mysqldf.format(idosdf.parse(branchtaskAlertGroupingDate))));
			criterias.put("alertType", 4);
			criterias.put("presentStatus", 1);
			List<AlertMailLogs> allAlertToBeConfirmed = genericDAO.findByCriteria(AlertMailLogs.class, criterias,
					entityManager);
			if (allAlertToBeConfirmed.size() > 0) {
				for (AlertMailLogs altMailLog : allAlertToBeConfirmed) {
					altMailLog.setConfirmationStatus("completed");
					genericDAO.saveOrUpdate(altMailLog, null, entityManager);
				}
			}
			criterias.clear();
			criterias.put("id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			Branch branch = genericDAO.getByCriteria(Branch.class, criterias, entityManager);
			criterias.clear();
			criterias.put("email", userEmail);
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			user = genericDAO.getByCriteria(Users.class, criterias, entityManager);
			criterias.clear();
			criterias.put("id", IdosUtil.convertStringToLong(branchStatutoryEntityId));
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			StatutoryDetails branchStatutory = genericDAO.getByCriteria(StatutoryDetails.class, criterias,
					entityManager);
			String branchStatutoryValidityFrom = json.findValue("branchStatutoryValidityFrom").asText();
			String branchStatutoryValidityTo = json.findValue("branchStatutoryValidityTo").asText();
			String branchStatutorySuppDocUrl = json.findValue("branchStatutorySuppDocUrl").asText();
			if (branchStatutory != null) {
				// SEND VALIDITY UPDATE MAIL TO ALERT FOR INFORMATION EMAIL
				if (branchStatutory.getAlertForInformation() != null
						&& !branchStatutory.getAlertForInformation().equals("")) {
					AlertMailLogs alertMailLogs = new AlertMailLogs();
					String body = branchStatutoryUpdatedValidityInfAlert.render(branch.getName(), userEmail,
							branchStatutory.getStatutoryDetails(), branchStatutoryValidityFrom,
							branchStatutoryValidityTo, branchStatutory.getAlertForInformation(),
							branch.getOrganization().getId().toString(), branch.getId().toString(),
							branchStatutory.getId().toString(), ConfigParams.getInstance()).body();
					final String username = ConfigFactory.load().getString("smtp.user");
					Session session = emailsession;
					String subject = "Branch Statutory Validity Update Information Alert For Organization: "
							+ branch.getOrganization().getName() + "";
					alertMailLogs.setMailFrom("beta@myidos.com");
					alertMailLogs.setMailTo(branchStatutory.getAlertForInformation());
					alertMailLogs.setMailSubject(subject);
					alertMailLogs.setBranch(branch);
					alertMailLogs.setOrganization(branch.getOrganization());
					genericDAO.saveOrUpdate(alertMailLogs, null, entityManager);
					mailTimer(body, username, session, branchStatutory.getAlertForInformation(), null, subject);
				}
				branchStatutory.setValidFrom(mysqldf.parse(mysqldf.format(idosdf.parse(branchStatutoryValidityFrom))));
				branchStatutory.setValidTo(mysqldf.parse(mysqldf.format(idosdf.parse(branchStatutoryValidityTo))));
				branchStatutory
						.setLastUpdatedValidityDate(mysqldf.parse(mysqldf.format(Calendar.getInstance().getTime())));
				if (branchStatutorySuppDocUrl != null && !branchStatutorySuppDocUrl.equals("")) {
					branchStatutory.setStatutoryDetails(branchStatutorySuppDocUrl);
				}
				genericDAO.saveOrUpdate(branchStatutory, user, entityManager);
				ObjectNode row = Json.newObject();
				row.put("success", branch.getName() + " Branch Statutory " + branchStatutory.getStatutoryDetails()
						+ " Validity Updated Successfully");
				alertmsg.add(row);
				transaction.commit();
			}
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
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result branchIndividualOperationalRemainderInfo(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// // EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			ArrayNode branchOperationalRemEntityInfo = result.putArray("branchOperationalRemEntityInfo");
			String organizationEntityId = json.findValue("organizationEntityId").asText();
			String branchEntityId = json.findValue("branchEntityId").asText();
			String branchOperationalRemainderEntityId = json.findValue("branchOperationalRemainderEntityId").asText();
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("id", IdosUtil.convertStringToLong(branchOperationalRemainderEntityId));
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			OrganizationOperationalRemainders orgOperRem = genericDAO
					.getByCriteria(OrganizationOperationalRemainders.class, criterias, entityManager);
			if (orgOperRem != null) {
				ObjectNode row = Json.newObject();
				row.put("branchName", orgOperRem.getBranch().getName());
				if (orgOperRem.getRequiements() != null) {
					row.put("operationalRequirements", orgOperRem.getRequiements());
				} else {
					row.put("operationalRequirements", "");
				}
				if (orgOperRem.getDueOn() != null) {
					row.put("operationalRemDueOn", idosdf.format(orgOperRem.getDueOn()));
				} else {
					row.put("operationalRemDueOn", "");
				}
				if (orgOperRem.getLastOperationalRemActionDueDated() != null) {
					row.put("branchOperationalRemainderLastActionDueDated",
							"Branch Operational Remainder Last Time Action Due Dated:"
									+ idosdf.format(orgOperRem.getLastOperationalRemActionDueDated()));
				} else {
					row.put("branchOperationalRemainderLastActionDueDated", "");
				}
				if (orgOperRem.getLastOperationalRemActionDate() != null) {
					row.put("branchOperationalRemainderLastActionDate",
							"Branch Premise Operational Remainder Last Time Action Date:"
									+ idosdf.format(orgOperRem.getLastOperationalRemActionDate()));
				} else {
					row.put("branchOperationalRemainderLastActionDate", "");
				}
				if (orgOperRem.getRecurrences() != null) {
					row.put("operationalRemRecurrence", orgOperRem.getRecurrences());
				} else {
					row.put("operationalRemRecurrence", "");
				}
				if (orgOperRem.getAlertForAction() != null) {
					row.put("operationalRemAlertForAction", orgOperRem.getAlertForAction());
				} else {
					row.put("operationalRemAlertForAction", "");
				}
				if (orgOperRem.getAlertForInformation() != null) {
					row.put("operationalRemAlertForInformation", orgOperRem.getAlertForInformation());
				} else {
					row.put("operationalRemAlertForInformation", "");
				}
				if (orgOperRem.getRemarks() != null) {
					row.put("operationalRemRemarks", orgOperRem.getRemarks());
				} else {
					row.put("operationalRemRemarks", "");
				}
				branchOperationalRemEntityInfo.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "BranchIndividualOperationalRemainderInfo Email",
					"BranchIndividualOperationalRemainderInfo Configuration",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result confirmBranchOperationalRemAction(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// // EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode alertmsg = result.putArray("alertmsg");
			String userEmail = json.findValue("userEmail").asText();
			userEmail = userEmail.replace("%40", "@");
			String organizationEntityId = json.findValue("organizationEntityId").asText();
			String branchEntityId = json.findValue("branchEntityId").asText();
			String branchOperationalRemainderEntityId = json.findValue("branchOperationalRemainderEntityId").asText();
			String branchtaskAlertGroupingDate = json.findValue("branchtaskAlertGroupingDate").asText();
			branchtaskAlertGroupingDate = branchtaskAlertGroupingDate.replace("%20", " ");
			branchtaskAlertGroupingDate = branchtaskAlertGroupingDate.replace("%2c", ",");
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("branchOperationalRemainder.id",
					IdosUtil.convertStringToLong(branchOperationalRemainderEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("taskAlertGroupingDate",
					mysqldf.parse(mysqldf.format(idosdf.parse(branchtaskAlertGroupingDate))));
			criterias.put("alertType", 5);
			criterias.put("presentStatus", 1);
			List<AlertMailLogs> allAlertToBeConfirmed = genericDAO.findByCriteria(AlertMailLogs.class, criterias,
					entityManager);
			if (allAlertToBeConfirmed.size() > 0) {
				for (AlertMailLogs altMailLog : allAlertToBeConfirmed) {
					altMailLog.setConfirmationStatus("completed");
					genericDAO.saveOrUpdate(altMailLog, null, entityManager);
				}
			}
			criterias.clear();
			criterias.put("id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			Branch branch = genericDAO.getByCriteria(Branch.class, criterias, entityManager);
			criterias.clear();
			criterias.put("email", userEmail);
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			user = genericDAO.getByCriteria(Users.class, criterias, entityManager);
			criterias.clear();
			criterias.put("id", IdosUtil.convertStringToLong(branchOperationalRemainderEntityId));
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			OrganizationOperationalRemainders orgOperRem = genericDAO
					.getByCriteria(OrganizationOperationalRemainders.class, criterias, entityManager);
			String branchOperRemGoingDate = json.findValue("branchOperRemGoingDate").asText();
			branchOperRemGoingDate = branchOperRemGoingDate.replace("%20", " ");
			branchOperRemGoingDate = branchOperRemGoingDate.replace("%2c", ",");
			if (orgOperRem != null) {
				// SEND VALIDITY UPDATE MAIL TO ALERT FOR INFORMATION EMAIL
				if (orgOperRem.getAlertForInformation() != null && !orgOperRem.getAlertForInformation().equals("")) {
					AlertMailLogs alertMailLogs = new AlertMailLogs();
					String body = branchConfirmOperationalRemInfAlert.render(branch.getName(), userEmail,
							orgOperRem.getRequiements(), branchOperRemGoingDate, orgOperRem.getAlertForInformation(),
							branch.getOrganization().getId().toString(), branch.getId().toString(),
							orgOperRem.getId().toString(), ConfigParams.getInstance()).body();
					final String username = ConfigFactory.load().getString("smtp.user");
					Session session = emailsession;
					String subject = "Branch Operational Remainder Confirmaton Alert For Organization: "
							+ branch.getOrganization().getName() + "";
					alertMailLogs.setMailFrom("beta@myidos.com");
					alertMailLogs.setMailTo(orgOperRem.getAlertForInformation());
					alertMailLogs.setMailSubject(subject);
					alertMailLogs.setBranch(branch);
					alertMailLogs.setOrganization(branch.getOrganization());
					genericDAO.saveOrUpdate(alertMailLogs, null, entityManager);
					mailTimer(body, username, session, orgOperRem.getAlertForInformation(), null, subject);
				}
				orgOperRem.setLastOperationalRemActionDate(
						mysqldf.parse(mysqldf.format(Calendar.getInstance().getTime())));
				orgOperRem.setLastOperationalRemActionDueDated(
						mysqldf.parse(mysqldf.format(idosdf.parse(branchOperRemGoingDate))));
				genericDAO.saveOrUpdate(orgOperRem, user, entityManager);
				ObjectNode row = Json.newObject();
				row.put("success", branch.getName() + " Branch Operational Remainder Confirmed Successfully");
				alertmsg.add(row);
				transaction.commit();
			}
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
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result branchIndividualPolicyInfo(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// // EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			ArrayNode branchInsurencePolicyEntityInfo = result.putArray("branchInsurencePolicyEntityInfo");
			String organizationEntityId = json.findValue("organizationEntityId").asText();
			String branchEntityId = json.findValue("branchEntityId").asText();
			String branchInsurenceEntityId = json.findValue("branchInsurenceEntityId").asText();
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("id", IdosUtil.convertStringToLong(branchInsurenceEntityId));
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			BranchInsurance bnchIns = genericDAO.getByCriteria(BranchInsurance.class, criterias, entityManager);
			if (bnchIns != null) {
				ObjectNode row = Json.newObject();
				row.put("branchName", bnchIns.getBranch().getName());
				if (bnchIns.getPolicyType() != null) {
					row.put("policyType", bnchIns.getPolicyType());
				} else {
					row.put("policyType", "");
				}
				if (bnchIns.getPolicyNumber() != null) {
					row.put("policyNumber", bnchIns.getPolicyNumber());
				} else {
					row.put("policyNumber", "");
				}
				if (bnchIns.getInsurenceCompany() != null) {
					row.put("insurenceCompany", bnchIns.getInsurenceCompany());
				} else {
					row.put("insurenceCompany", "");
				}
				if (bnchIns.getInsurancePolicyDocUrl() != null) {
					row.put("insurencePolicyDocUrl", bnchIns.getInsurancePolicyDocUrl());
				} else {
					row.put("insurencePolicyDocUrl", "");
				}
				if (bnchIns.getPolicyValidFrom() != null) {
					row.put("policyValidFrom", idosdf.format(bnchIns.getPolicyValidFrom()));
				} else {
					row.put("policyValidFrom", "");
				}
				if (bnchIns.getPolicyValidTo() != null) {
					row.put("policyValidTo", idosdf.format(bnchIns.getPolicyValidTo()));
				} else {
					row.put("policyValidTo", "");
				}
				if (bnchIns.getLastInsurenceVlidityUpdatedDate() != null) {
					row.put("branchInsuranceLastUpdatedValidityDate", "Branch Insurance Validity Last Updated Date:"
							+ idosdf.format(bnchIns.getLastInsurenceVlidityUpdatedDate()));
				} else {
					row.put("branchInsuranceLastUpdatedValidityDate", "");
				}
				if (bnchIns.getLastInsurenceAnnualPremiumDueDated() != null) {
					row.put("branchInsuranceAnnualPremiumDueDated", "Branch Insurance Last Annual Premium Due Dated:"
							+ idosdf.format(bnchIns.getLastInsurenceAnnualPremiumDueDated()));
				} else {
					row.put("branchInsuranceAnnualPremiumDueDated", "");
				}
				if (bnchIns.getLastInsurenceAnnualPremiumPaidDate() != null) {
					row.put("branchInsuranceAnnualPremiumLastPaidDate",
							"Branch Insurance Last Annual Premium Paid Date:"
									+ idosdf.format(bnchIns.getLastInsurenceAnnualPremiumPaidDate()));
				} else {
					row.put("branchInsuranceAnnualPremiumLastPaidDate", "");
				}
				if (bnchIns.getAnnualPremium() != null) {
					row.put("policyAnnualPremium", bnchIns.getAnnualPremium());
				} else {
					row.put("policyAnnualPremium", "");
				}
				if (bnchIns.getAlertOfAction() != null) {
					row.put("policyAlertForAction", bnchIns.getAlertOfAction());
				} else {
					row.put("policyAlertForAction", "");
				}
				if (bnchIns.getAlertOfInformation() != null) {
					row.put("policyAlertForInformation", bnchIns.getAlertOfInformation());
				} else {
					row.put("policyAlertForInformation", "");
				}
				if (bnchIns.getRemarks() != null) {
					row.put("policyRemarks", bnchIns.getRemarks());
				} else {
					row.put("policyRemarks", "");
				}
				branchInsurencePolicyEntityInfo.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "BranchIndividualPolicyInfo Email",
					"BranchIndividualPolicyInfo Configuration",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result updateBranchInsurenceValidity(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// // EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode alertmsg = result.putArray("alertmsg");
			String userEmail = json.findValue("userEmail").asText();
			userEmail = userEmail.replace("%40", "@");
			String organizationEntityId = json.findValue("organizationEntityId").asText();
			String branchEntityId = json.findValue("branchEntityId").asText();
			String branchInsurenceEntityId = json.findValue("branchInsurenceEntityId").asText();
			String branchtaskAlertGroupingDate = json.findValue("branchtaskAlertGroupingDate").asText();
			branchtaskAlertGroupingDate = branchtaskAlertGroupingDate.replace("%20", " ");
			branchtaskAlertGroupingDate = branchtaskAlertGroupingDate.replace("%2c", ",");
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("branchInsurence.id", IdosUtil.convertStringToLong(branchInsurenceEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("taskAlertGroupingDate",
					mysqldf.parse(mysqldf.format(idosdf.parse(branchtaskAlertGroupingDate))));
			criterias.put("alertType", 6);
			criterias.put("presentStatus", 1);
			List<AlertMailLogs> allAlertToBeConfirmed = genericDAO.findByCriteria(AlertMailLogs.class, criterias,
					entityManager);
			if (allAlertToBeConfirmed.size() > 0) {
				for (AlertMailLogs altMailLog : allAlertToBeConfirmed) {
					altMailLog.setConfirmationStatus("completed");
					genericDAO.saveOrUpdate(altMailLog, null, entityManager);
				}
			}
			criterias.clear();
			criterias.put("id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			Branch branch = genericDAO.getByCriteria(Branch.class, criterias, entityManager);
			criterias.clear();
			criterias.put("email", userEmail);
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			user = genericDAO.getByCriteria(Users.class, criterias, entityManager);
			criterias.clear();
			criterias.put("id", IdosUtil.convertStringToLong(branchInsurenceEntityId));
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			BranchInsurance bnchIns = genericDAO.getByCriteria(BranchInsurance.class, criterias, entityManager);
			String branchInsurenceValidityFrom = json.findValue("branchInsurenceValidityFrom").asText();
			String branchInsurenceValidityTo = json.findValue("branchInsurenceValidityTo").asText();
			String branchInsurencePolicyDocUrl = json.findValue("branchInsurencePolicyDocUrl").asText();
			if (bnchIns != null) {
				// SEND VALIDITY UPDATE MAIL TO ALERT FOR INFORMATION EMAIL
				if (bnchIns.getAlertOfInformation() != null && !bnchIns.getAlertOfInformation().equals("")) {
					AlertMailLogs alertMailLogs = new AlertMailLogs();
					String body = branchInsurencePolicyValidityInfAlert.render(branch.getName(), userEmail,
							bnchIns.getPolicyNumber(), branchInsurenceValidityFrom, branchInsurenceValidityTo,
							bnchIns.getAlertOfInformation(), branch.getOrganization().getId().toString(),
							branch.getId().toString(), bnchIns.getId().toString(), ConfigParams.getInstance()).body();
					final String username = ConfigFactory.load().getString("smtp.user");
					Session session = emailsession;
					String subject = "Branch Insurence Policy Validity Update Information Alert For Organization: "
							+ branch.getOrganization().getName() + "";
					alertMailLogs.setMailFrom("beta@myidos.com");
					alertMailLogs.setMailTo(bnchIns.getAlertOfInformation());
					alertMailLogs.setMailSubject(subject);
					alertMailLogs.setBranch(branch);
					alertMailLogs.setOrganization(branch.getOrganization());
					genericDAO.saveOrUpdate(alertMailLogs, null, entityManager);
					mailTimer(body, username, session, bnchIns.getAlertOfInformation(), null, subject);
				}
				bnchIns.setPolicyValidFrom(mysqldf.parse(mysqldf.format(idosdf.parse(branchInsurenceValidityFrom))));
				bnchIns.setPolicyValidTo(mysqldf.parse(mysqldf.format(idosdf.parse(branchInsurenceValidityTo))));
				bnchIns.setLastInsurenceVlidityUpdatedDate(
						mysqldf.parse(mysqldf.format(Calendar.getInstance().getTime())));
				if (branchInsurencePolicyDocUrl != null && !branchInsurencePolicyDocUrl.equals("")) {
					bnchIns.setInsurancePolicyDocUrl(branchInsurencePolicyDocUrl);
				}
				genericDAO.saveOrUpdate(bnchIns, user, entityManager);
				ObjectNode row = Json.newObject();
				row.put("success", branch.getName() + " Branch Insurence Policy Validity Updated Successfully");
				alertmsg.add(row);
				transaction.commit();
			}
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
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result confirmBranchInsurencePremiumPayment(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// // EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode alertmsg = result.putArray("alertmsg");
			String userEmail = json.findValue("userEmail").asText();
			userEmail = userEmail.replace("%40", "@");
			String organizationEntityId = json.findValue("organizationEntityId").asText();
			String branchEntityId = json.findValue("branchEntityId").asText();
			String branchInsurenceEntityId = json.findValue("branchInsurenceEntityId").asText();
			String branchtaskAlertGroupingDate = json.findValue("branchtaskAlertGroupingDate").asText();
			branchtaskAlertGroupingDate = branchtaskAlertGroupingDate.replace("%20", " ");
			branchtaskAlertGroupingDate = branchtaskAlertGroupingDate.replace("%2c", ",");
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("branchInsurence.id", IdosUtil.convertStringToLong(branchInsurenceEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("taskAlertGroupingDate",
					mysqldf.parse(mysqldf.format(idosdf.parse(branchtaskAlertGroupingDate))));
			criterias.put("alertType", 7);
			criterias.put("presentStatus", 1);
			List<AlertMailLogs> allAlertToBeConfirmed = genericDAO.findByCriteria(AlertMailLogs.class, criterias,
					entityManager);
			if (allAlertToBeConfirmed.size() > 0) {
				for (AlertMailLogs altMailLog : allAlertToBeConfirmed) {
					altMailLog.setConfirmationStatus("completed");
					genericDAO.saveOrUpdate(altMailLog, null, entityManager);
				}
			}
			criterias.clear();
			criterias.put("id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			Branch branch = genericDAO.getByCriteria(Branch.class, criterias, entityManager);
			criterias.clear();
			criterias.put("email", userEmail);
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			user = genericDAO.getByCriteria(Users.class, criterias, entityManager);
			criterias.clear();
			criterias.put("id", IdosUtil.convertStringToLong(branchInsurenceEntityId));
			criterias.put("branch.id", IdosUtil.convertStringToLong(branchEntityId));
			criterias.put("organization.id", IdosUtil.convertStringToLong(organizationEntityId));
			criterias.put("presentStatus", 1);
			BranchInsurance bnchIns = genericDAO.getByCriteria(BranchInsurance.class, criterias, entityManager);
			String branchpremiumGoingDt = json.findValue("branchpremiumGoingDt").asText();
			branchpremiumGoingDt = branchpremiumGoingDt.replace("%20", " ");
			branchpremiumGoingDt = branchpremiumGoingDt.replace("%2c", ",");
			String branchInsurencePolicyDocUrl = json.findValue("branchInsurencePolicyDocUrl").asText();
			if (bnchIns != null) {
				// SEND VALIDITY UPDATE MAIL TO ALERT FOR INFORMATION EMAIL
				if (bnchIns.getAlertOfInformation() != null && !bnchIns.getAlertOfInformation().equals("")) {
					AlertMailLogs alertMailLogs = new AlertMailLogs();
					String body = branchInsurenceConfirmAnnualPremiumInfAlert
							.render(branch.getName(), userEmail, bnchIns.getPolicyNumber(), branchpremiumGoingDt,
									bnchIns.getAlertOfInformation(), branch.getOrganization().getId().toString(),
									branch.getId().toString(), bnchIns.getId().toString(), ConfigParams.getInstance())
							.body();
					final String username = ConfigFactory.load().getString("smtp.user");
					Session session = emailsession;
					String subject = "Branch Insurence Policy Annual Premium Confirmaton Alert For Organization: "
							+ branch.getOrganization().getName() + "";
					alertMailLogs.setMailFrom("beta@myidos.com");
					alertMailLogs.setMailTo(branch.getAlertForInformation());
					alertMailLogs.setMailSubject(subject);
					alertMailLogs.setBranch(branch);
					alertMailLogs.setOrganization(branch.getOrganization());
					genericDAO.saveOrUpdate(alertMailLogs, null, entityManager);
					mailTimer(body, username, session, branch.getAlertForInformation(), null, subject);
				}
				if (branchInsurencePolicyDocUrl != null && !branchInsurencePolicyDocUrl.equals("")) {
					bnchIns.setInsurancePolicyDocUrl(branchInsurencePolicyDocUrl);
				}
				bnchIns.setLastInsurenceAnnualPremiumPaidDate(
						mysqldf.parse(mysqldf.format(Calendar.getInstance().getTime())));
				bnchIns.setLastInsurenceAnnualPremiumDueDated(
						mysqldf.parse(mysqldf.format(idosdf.parse(branchpremiumGoingDt))));
				genericDAO.saveOrUpdate(branch, user, entityManager);
				ObjectNode row = Json.newObject();
				row.put("success",
						branch.getName() + " Branch Insurence Policy Premium Payment Confirmed Successfully");
				alertmsg.add(row);
				transaction.commit();
			}
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
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result bankAccountsForPayment(Request request) {

		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			ArrayNode an = result.putArray("availableBranchBankData");
			// String useremail=json.findValue("usermail").asText();
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			Branch txnBnch = null;
			log.log(Level.FINE, ">>>> Start : " + json);
			String txnPurpose = json.findValue("txnPurpose").asText();
			String txnBranch = null;

			if (txnPurpose.equals("1") || txnPurpose.equals("3") || txnPurpose.equals("7") || txnPurpose.equals("8")) {
				txnBranch = json.findValue("txnBranch").asText();
				txnBnch = Branch.findById(Long.parseLong(txnBranch));
			} else if (txnPurpose.equals("5")) {
				txnBranch = json.findValue("txnBranch").asText();
				String pendingInvoice = json.findValue("pendingInvoice").asText();
				Transaction txn = Transaction.findById(Long.parseLong(pendingInvoice));
				if (txn != null) {
					txnBnch = txn.getTransactionBranch();
				} else {
					txnBnch = Branch.findById(Long.parseLong(txnBranch));
				}

			} else if (txnPurpose.equals("6") || txnPurpose.equals("9") || txnPurpose.equals("35")
					|| txnPurpose.equals("36") || txnPurpose.equals("34")) {

				// txnBnch=user.getBranch(); Sunil
				txnBranch = json.findValue("txnBranch").asText();
				txnBnch = Branch.findById(Long.parseLong(txnBranch));
			} else if (txnPurpose.equals("")) {
				String txnEntityId = json.findValue("txnEntityId").asText();
				Transaction txn = Transaction.findById(Long.parseLong(txnEntityId));
				if (txn != null) {
					txnBnch = txn.getTransactionBranch();
				} else {
					// Transaction not present. ideally this condition is not valid
				}
			}

			if (txnBnch != null) { // Sunil: Added to verify null
				List<BranchBankAccounts> bnchBankAccounts = txnBnch.getBranchBankAccounts();
				for (BranchBankAccounts bnchBnkAct : bnchBankAccounts) {
					ObjectNode objNode = Json.newObject();
					objNode.put("bnchBankAccountsId", bnchBnkAct.getId());
					objNode.put("bnchBankAccountsName", bnchBnkAct.getBankName());
					an.add(objNode);
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return internalServerError(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getBranchAdministratorData(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// // EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = null;
		String userRoles = "";
		try {
			ArrayNode branchlistan = result.putArray("branchListData");
			ArrayNode currencyData = result.putArray("currencyData");
			ArrayNode countryData = result.putArray("countryData");
			ArrayNode bnkactTypean = result.putArray("bnkactTypean");
			ArrayNode phonecodean = result.putArray("phoneCodeData");
			ArrayNode organ = result.putArray("organizationData");
			BankAccountEnumType[] bankActTypes = BankAccountEnumType.class.getEnumConstants();
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("presentStatus", 1);
			List<CountryPhoneCode> countryTelephoneCodes = genericDAO.findByCriteria(CountryPhoneCode.class, criterias,
					"countryWithCode", false, entityManager);
			user = getUserInfo(request);
			userRoles = getUserRolesIds(user);
			int count = 0;
			Organization orgn = user.getOrganization();
			criterias.clear();
			criterias.put("organization.id", user.getOrganization().getId());
			if (orgn != null) {
				ObjectNode row = Json.newObject();
				row.put("id", orgn.getId());
				row.put("name", orgn.getName());
				row.put("corporateEmail", orgn.getCorporateMail());
				organ.add(row);
			}
			if (userRoles.contains("9")) {
				Branch branchTableList = user.getBranchKeyOff().getBranch();
				ObjectNode row = Json.newObject();
				row.put("id", branchTableList.getId());
				row.put("name", branchTableList.getName());
				String country = "";
				if (branchTableList.getCountry() != null) {
					country = String.valueOf(branchTableList.getCountry());
				}
				row.put("country", country);
				row.put("location", branchTableList.getLocation());
				row.put("phone", branchTableList.getPhoneNumber());
				if (branchTableList.getPresentStatus() == 0) {
					row.put("actionText", "Activate");
				}
				if (branchTableList.getPresentStatus() == 1) {
					row.put("actionText", "Deactivate");
				}
				branchlistan.add(row);
			}

			Map<String, String> currencies = CountryCurrencyUtil.getAvailableCurrenciesList();
			for (String currency : currencies.keySet()) {
				ObjectNode row = Json.newObject();
				row.put("id", currency);
				row.put("name", currencies.get(currency));
				currencyData.add(row);
			}
			Map<String, String> countries = CountryCurrencyUtil.getCountries();
			for (String country : countries.keySet()) {
				ObjectNode row = Json.newObject();
				row.put("id", country);
				row.put("name", countries.get(country));
				countryData.add(row);
			}
			for (BankAccountEnumType bankActType : bankActTypes) {
				int actType = bankActType.getId();
				ObjectNode row = Json.newObject();
				row.put("id", String.valueOf(actType));
				row.put("name", bankActType.getName());
				bnkactTypean.add(row);
			}
			for (CountryPhoneCode countryCode : countryTelephoneCodes) {
				ObjectNode row = Json.newObject();
				row.put("id", countryCode.getAreaCode());
				row.put("name", countryCode.getCountryWithCode());
				phonecodean.add(row);
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
		log.log(Level.FINE, ">>>> end " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result deactivateBranch(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// // EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		Users user = null;
		Branch branch = null;
		try {
			entitytransaction.begin();
			ArrayNode branchactionan = result.putArray("branchActionData");
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				if (user != null) {
					String entityPrimaryId = json.findValue("entityPrimaryId").asText();
					String branchActionText = json.findValue("branchActionText").asText();
					if (branchActionText != null && !branchActionText.equals("")) {
						if (branchActionText.equals("Deactivate")) {
							branch = Branch.findById(Long.parseLong(entityPrimaryId));
							branch.setPresentStatus(0);
							genericDAO.saveOrUpdate(branch, user, entityManager);
							ObjectNode row = Json.newObject();
							row.put("id", entityPrimaryId);
							row.put("successmsg", "Subscribed");
							row.put("actionText", "Activate");
							branchactionan.add(row);
						} else if (branchActionText.equals("Activate")) {
							branch = Branch.findById(Long.parseLong(entityPrimaryId));
							branch.setPresentStatus(1);
							genericDAO.saveOrUpdate(branch, user, entityManager);
							ObjectNode row = Json.newObject();
							row.put("id", entityPrimaryId);
							row.put("successmsg", "Subscribed");
							row.put("actionText", "Deactivate");
							branchactionan.add(row);
						}
					}
				}
			}
			entitytransaction.commit();
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	/**
	 * Added by Firdous on 18-12--2017
	 * Method to download Branch template
	 * 
	 * @return
	 */
	@Transactional
	public Result downloadOrgBranchTemplate(Request request) {
		log.log(Level.FINE, ">>>> Start inside download Branch Template");
		// EntityManager entityManager = getEntityManager();
		File file = null;
		Users user = null;
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			String orgName = user.getOrganization().getName().replaceAll("\\s", "");
			String fileName = orgName + "_Branch_Template.xlsx";
			String sheetName = "Branches";
			String path = application.path().toString() + "/logs/OrgBranches/";
			File filepath = new File(path);
			if (!filepath.exists()) {
				filepath.mkdir();
			}
			path = path + fileName;
			excelService.createOrgBranchTemplateExcel(user, entityManager, path, sheetName);
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
	 * Added By Firdous on 21-12-2017
	 * Method to Upload Branch details using xlsx template file
	 *
	 * @return
	 */

	@Transactional
	public Result uploadBranches(Request request) {
		log.log(Level.FINE, ">>>> Start uploading branches");
		Map<String, Object> criterias = new HashMap<String, Object>();
		// // EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		ArrayNode an = result.putArray("successUploading");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		JsonNode json = request.body().asJson();
		DataFormatter df = new DataFormatter();
		Users user = null;
		try {
			transaction.begin();
			MultipartFormData<File> body = request.body().asMultipartFormData();
			user = getUserInfo(request);
			if (user == null) {
				log.log(Level.SEVERE, " unauthorized access");
				return unauthorized();
			}
			Organization organization = user.getOrganization();
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
						if (!"Branches".equalsIgnoreCase(sheet.getSheetName())) {
							log.log(Level.FINE, "no sheets available");
							continue;
						}
						Iterator rows = sheet.rowIterator();
						while (rows.hasNext()) {

							XSSFRow row = (XSSFRow) rows.next();
							log.log(Level.INFO, "row number=" + row.getRowNum());
							if (row.getRowNum() == 0 || row.getRowNum() == 1 || row.getRowNum() == 2) {
								continue;
							}

							String organizationName = row.getCell(0) != null ? row.getCell(0).toString() : null;
							String branchName = row.getCell(1) != null ? row.getCell(1).toString() : null;
							String openedDate = row.getCell(2) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(2)));
							String gstinStateCode1 = row.getCell(3) != null ? row.getCell(3).toString() : null;
							String gstinNumber1 = row.getCell(4) != null ? row.getCell(4).toString() : null;
							String gstinStateCode2 = row.getCell(5) == null ? null
									: ((XSSFCell) row.getCell(5)).toString();
							String gstinNumber2 = row.getCell(6) == null ? null
									: ((XSSFCell) row.getCell(6)).toString();
							String branchAddress = row.getCell(7) == null ? null
									: ((XSSFCell) row.getCell(7)).toString();
							String country = row.getCell(8) == null ? null : ((XSSFCell) row.getCell(8)).toString();
							String currency = row.getCell(9) == null ? null : ((XSSFCell) row.getCell(9)).toString();
							String state = row.getCell(10) == null ? null : ((XSSFCell) row.getCell(10)).toString();
							String city = row.getCell(11) == null ? null : ((XSSFCell) row.getCell(11)).toString();
							String phoneCountryCode = row.getCell(12) == null ? null
									: ((XSSFCell) row.getCell(12)).toString();
							String phoneNo = row.getCell(13) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(13)));
							String premise = row.getCell(14) != null ? row.getCell(14).toString() : null;

							String premiseValidityFrom = row.getCell(15) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(15)));
							String premiseValidityTo = row.getCell(16) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(16)));
							String periodicityOfPayment = row.getCell(17) != null ? row.getCell(17).toString() : null;
							String rentPayable = row.getCell(18) != null ? row.getCell(18).toString() : null;
							String landLordName = row.getCell(19) != null ? row.getCell(19).toString() : null;
							String landLordAddress = row.getCell(20) == null ? null
									: ((XSSFCell) row.getCell(20)).toString();
							String landLordBankName = row.getCell(21) == null ? null
									: ((XSSFCell) row.getCell(21)).toString();
							String landLordBankAccountNumber = row.getCell(22) == null ? null
									: ((XSSFCell) row.getCell(22)).toString();
							String landLordBankAddress = row.getCell(23) == null ? null
									: ((XSSFCell) row.getCell(23)).toString();
							String rentRevisionDueOn = row.getCell(24) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(24)));
							String alertForActionLandLord = row.getCell(25) == null ? null
									: ((XSSFCell) row.getCell(25)).toString();
							String alertForInformationLandLord = row.getCell(26) == null ? null
									: ((XSSFCell) row.getCell(26)).toString();
							String remarksLandLOrd = row.getCell(27) == null ? null
									: ((XSSFCell) row.getCell(27)).toString();

							String officerName = row.getCell(28) == null ? null
									: ((XSSFCell) row.getCell(28)).toString();
							String designation = row.getCell(29) == null ? null
									: ((XSSFCell) row.getCell(29)).toString();
							String officerCountry = row.getCell(30) == null ? null
									: ((XSSFCell) row.getCell(30)).toString();
							String officerCity = row.getCell(31) == null ? null
									: ((XSSFCell) row.getCell(31)).toString();
							String officerEmail = row.getCell(32) == null ? null
									: ((XSSFCell) row.getCell(32)).toString();
							String countryCode1 = row.getCell(33) == null ? null
									: ((XSSFCell) row.getCell(20)).toString();
							String officialPhoneNumber = row.getCell(34) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(21)));
							String countryCode2 = row.getCell(35) == null ? null
									: ((XSSFCell) row.getCell(22)).toString();
							String personalPhoneNumber = row.getCell(36) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(23)));

							String statutaryDetails = row.getCell(37) == null ? null
									: ((XSSFCell) row.getCell(37)).toString();
							String registrationNumber = row.getCell(38) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(38)));
							String validityFrom = row.getCell(39) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(39)));
							String validityTo = row.getCell(40) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(40)));
							String alertForAction = row.getCell(41) == null ? null
									: ((XSSFCell) row.getCell(41)).toString();
							String alertForInformation = row.getCell(42) == null ? null
									: ((XSSFCell) row.getCell(42)).toString();
							String nameAndAddressOfConsultant = row.getCell(43) == null ? null
									: ((XSSFCell) row.getCell(43)).toString();
							String remarks = row.getCell(44) == null ? null : ((XSSFCell) row.getCell(44)).toString();

							String requirements = row.getCell(45) == null ? null
									: ((XSSFCell) row.getCell(45)).toString();
							String validityFrom1 = row.getCell(46) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(46)));
							String validityTo1 = row.getCell(47) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(47)));
							String recurrance = row.getCell(48) == null ? null
									: ((XSSFCell) row.getCell(48)).toString();
							String alertForAction1 = row.getCell(49) == null ? null
									: ((XSSFCell) row.getCell(49)).toString();
							String alertForInformation1 = row.getCell(50) == null ? null
									: ((XSSFCell) row.getCell(50)).toString();
							String remarks1 = row.getCell(51) == null ? null : ((XSSFCell) row.getCell(51)).toString();

							String custodianForSafeDeposit = row.getCell(52) == null ? null
									: ((XSSFCell) row.getCell(52)).toString();
							String openingBalance = row.getCell(53) == null ? null
									: ((XSSFCell) row.getCell(53)).toString();
							String phoneCountryCode3 = row.getCell(54) == null ? null
									: ((XSSFCell) row.getCell(54)).toString();
							String cutodianPhoneNumber = row.getCell(55) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(55)));
							String custodianEmail = row.getCell(56) == null ? null
									: ((XSSFCell) row.getCell(56)).toString();
							String cashierName = row.getCell(57) == null ? null
									: ((XSSFCell) row.getCell(57)).toString();
							String phoneCountryCode4 = row.getCell(58) == null ? null
									: ((XSSFCell) row.getCell(58)).toString();
							String cashierPhoneNumber = row.getCell(59) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(59)));
							String cashierEmail = row.getCell(60) == null ? null
									: ((XSSFCell) row.getCell(60)).toString();
							String cashierKnowledgeLibrary = row.getCell(61) == null ? null
									: ((XSSFCell) row.getCell(61)).toString();
							String pettyCashTransactionApprovalRequired = row.getCell(62) == null ? null
									: ((XSSFCell) row.getCell(62)).toString();
							String approvalLimit = row.getCell(63) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(63)));
							String pettyCashOpeningBalance = row.getCell(64) == null ? null
									: ((XSSFCell) row.getCell(64)).toString();

							String policyType = row.getCell(65) == null ? null
									: ((XSSFCell) row.getCell(65)).toString();
							String policyNumber = row.getCell(66) == null ? null
									: ((XSSFCell) row.getCell(66)).toString();
							String insuranceCompany = row.getCell(67) == null ? null
									: ((XSSFCell) row.getCell(67)).toString();
							String validityFrom2 = row.getCell(68) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(68)));
							String validityTo2 = row.getCell(69) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(69)));
							String annualPremier = row.getCell(70) == null ? null
									: ((XSSFCell) row.getCell(70)).toString();
							String alertForAction2 = row.getCell(71) == null ? null
									: ((XSSFCell) row.getCell(71)).toString();
							String alertForInformation2 = row.getCell(72) == null ? null
									: ((XSSFCell) row.getCell(72)).toString();
							String remarks2 = row.getCell(73) == null ? null : ((XSSFCell) row.getCell(73)).toString();

							String bankName = row.getCell(74) == null ? null : ((XSSFCell) row.getCell(74)).toString();
							String accountNumber = row.getCell(75) == null ? null
									: ((XSSFCell) row.getCell(75)).toString();
							String bankOpeningBalance = row.getCell(76) == null ? null
									: ((XSSFCell) row.getCell(76)).toString();
							String accountType = row.getCell(77) == null ? null
									: ((XSSFCell) row.getCell(77)).toString();
							String routingNumber = row.getCell(78) == null ? null
									: ((XSSFCell) row.getCell(78)).toString();
							String swiftCode = row.getCell(79) == null ? null : ((XSSFCell) row.getCell(79)).toString();
							String phoneCountryCode5 = row.getCell(80) == null ? null
									: ((XSSFCell) row.getCell(80)).toString();
							String bankPhoneNumber = row.getCell(81) == null ? null
									: ((XSSFCell) row.getCell(81)).toString();
							String bankAddress = row.getCell(82) == null ? null
									: ((XSSFCell) row.getCell(82)).toString();
							String bankInstrumentCustodianName = row.getCell(83) == null ? null
									: ((XSSFCell) row.getCell(83)).toString();
							String bankInstrumentCustodianEmail = row.getCell(84) == null ? null
									: ((XSSFCell) row.getCell(84)).toString();
							String authorizedSignatoryName = row.getCell(85) == null ? null
									: ((XSSFCell) row.getCell(85)).toString();
							String authorizedSignatoryEmail = row.getCell(86) == null ? null
									: ((XSSFCell) row.getCell(86)).toString();

							Branch newBranch = null;
							Branch oldBranch = null;
							String oldName = null;
							criterias.clear();
							criterias.put("name", branchName);
							criterias.put("organization.id", organization.getId());
							criterias.put("presentStatus", 1);
							List<Branch> existBranch = genericDAO.findByCriteria(Branch.class, criterias,
									entityManager);
							if (existBranch.size() == 0) {
								newBranch = new Branch();
								oldName = "";
							} else {
								newBranch = existBranch.get(0);
								oldName = newBranch.getName();
							}
							if (existBranch.equals(null)) {
								newBranch.setIsHeadQuarter(0);
								newBranch.setBudgetDate(Calendar.getInstance().getTime());
								StringBuilder sbr = new StringBuilder("");
								sbr.append(
										"select obj from IdosChannelPartnerAlloteOrganization obj where obj.organizationName='"
												+ user.getOrganization().getName()
														.equalsIgnoreCase(user.getOrganization().getName())
												+ "' and obj.email='"
												+ user.getOrganization().getCorporateMail()
														.equalsIgnoreCase(user.getOrganization().getCorporateMail())
												+ "' and obj.presentStatus=1");
								List<IdosChannelPartnerAlloteOrganization> idosCpCommitmentList = genericDAO
										.executeSimpleQuery(sbr.toString(), entityManager);
								if (!idosCpCommitmentList.isEmpty() && idosCpCommitmentList.size() > 0) {
									for (IdosChannelPartnerAlloteOrganization idosCpCommitment : idosCpCommitmentList) {
										IdosChannelPartnerCustomerOrganizationBranch newCpOrgBnch = new IdosChannelPartnerCustomerOrganizationBranch();
										newCpOrgBnch.setCustomerOrganizatioNbRANCH(newBranch);
										newCpOrgBnch.setCustomerOrganization(user.getOrganization());
										newCpOrgBnch.setIdosChannelPartner(idosCpCommitment.getChannelPartner());
										genericDAO.saveOrUpdate(newCpOrgBnch, user, entityManager);
										idosCpCommitment.setCommitmentStatus("ALLOTED");
										genericDAO.saveOrUpdate(idosCpCommitment, user, entityManager);
										String location = "";
										if (newBranch.getLocation() != null) {
											location = newBranch.getLocation();
										}
										String cpBody = channelPartnerProspectConverted
												.render(idosCpCommitment.getChannelPartner().getChannelPartnerName(),
														idosCpCommitment.getChannelPartner().getChannelPartnerEmail(),
														user.getOrganization().getName(),
														user.getOrganization().getCorporateMail(), newBranch.getName(),
														location, ConfigParams.getInstance())
												.body();
										final String cpusername = ConfigFactory.load()
												.getString("smtpchannelsales.user");
										String cpsubject = "Organization/Branch Registered Into Your Channel Partner Account With Idos";
										Session cpsession = channelSalesSession;
										mailTimer(cpBody, cpusername, cpsession,
												idosCpCommitment.getChannelPartner().getChannelPartnerEmail(), null,
												cpsubject);

									}
								}
							}
							newBranch.setName(branchName);
							newBranch.setPhoneNumberCtryCode(phoneCountryCode);
							newBranch.setPhoneNumber(phoneNo);
							newBranch.setLocation(city);
							newBranch.setAddress(branchAddress);
							newBranch.setOrganization(organization);
							try {
								if (openedDate != null && !openedDate.equals("")) {
									newBranch.setBranchOpenDate(IdosConstants.mysqldf.parse(
											IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(openedDate))));
								}
							} catch (java.text.ParseException ex) {
								log.log(Level.SEVERE, "Date cannot be parsed", ex);
								throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
										IdosConstants.TECHNICAL_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
										"Date cannot be parsed");
							}
							newBranch.setCurrency(currency);
							if (user.getOrganization().getGstCountryCode() == null && gstinStateCode1 == null
									&& (gstinNumber1 == null
											|| (gstinNumber1.length() < 13 || gstinNumber1.length() > 13))) {
								throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE,
										IdosConstants.BUSINESS_EXCEPTION, IdosConstants.DATA_FORMAT_EXCEPTION,
										"GSTIN is wrong.");
							}
							newBranch.setAddress(branchAddress);
							newBranch.setStateCode(gstinStateCode1);
							newBranch.setGstin(gstinNumber1);
							int countryId = 0;
							if (country != null) {
								String counrtyId = CountryCurrencyUtil.getCountryId(country);
								if (counrtyId != null) {
									countryId = IdosUtil.convertStringToInt(counrtyId);
									newBranch.setCountry(countryId);
								}
							}
							Date aggreementValidFrom = null, aggreementValidTo = null;

							if (premise != null && !premise.equals("")) {
								int bnchFacility = 0;
								if (premise != null) {
									if (premise.equals("Rented")) {
										bnchFacility = 1;
										newBranch.setBranchFacility(bnchFacility);
									} else if (premise.equals("Leased")) {
										bnchFacility = 2;
										newBranch.setBranchFacility(bnchFacility);
									}
									if (bnchFacility == 1 || bnchFacility == 2) {
										if (periodicityOfPayment != null) {
											if (!periodicityOfPayment.equals("")) {
												if (periodicityOfPayment.equals("Weekly")) {
													newBranch.setPeriodicityOfPayment(1);
												} else if (periodicityOfPayment.equals("Monthly")) {
													newBranch.setPeriodicityOfPayment(2);
												} else if (periodicityOfPayment.equals("Quarterly")) {
													newBranch.setPeriodicityOfPayment(3);
												}
												if (periodicityOfPayment.equals("Half Yearly")) {
													newBranch.setPeriodicityOfPayment(4);
												}
												if (periodicityOfPayment.equals("Annually")) {
													newBranch.setPeriodicityOfPayment(5);
												}
												if (periodicityOfPayment.equals("Once In 2 Years")) {
													newBranch.setPeriodicityOfPayment(6);
												}
												if (periodicityOfPayment.equals("Once In 3 years")) {
													newBranch.setPeriodicityOfPayment(7);
												}
												if (periodicityOfPayment.equals("One Time")) {
													newBranch.setPeriodicityOfPayment(8);
												}
											}
										}
										if (!rentPayable.equals("") && rentPayable != null) {
											Double rentPayable1 = IdosUtil.convertStringToDouble(rentPayable);
											newBranch.setRentPayable(rentPayable1);
										}

										Date rentDueOn = null;
										if (!rentRevisionDueOn.equals("") && rentRevisionDueOn != null) {
											rentDueOn = mysqldf.parse(mysqldf.format(idosdf.parse(rentRevisionDueOn)));
										}
										if (premiseValidityFrom != null && !premiseValidityFrom.equals("")) {
											aggreementValidFrom = mysqldf
													.parse(mysqldf.format(idosdf.parse(premiseValidityFrom)));
										}
										if (premiseValidityTo != null && !premiseValidityTo.equals("")) {
											aggreementValidTo = mysqldf
													.parse(mysqldf.format(idosdf.parse(premiseValidityFrom)));
										}
										if (alertForActionLandLord != null) {
											newBranch.setAlertForAction(alertForActionLandLord);
										}
										if (alertForInformationLandLord != null) {
											newBranch.setAlertForInformation(alertForInformationLandLord);
										}

										newBranch.setAggreementValidFrom(aggreementValidFrom);
										newBranch.setAggreementValidTo(aggreementValidTo);
										newBranch.setLandlordName(landLordName);
										newBranch.setLandlordAddress(landLordAddress);
										newBranch.setBankAccountName(landLordBankName);
										newBranch.setBankAccountNumber(landLordBankAccountNumber);
										newBranch.setBankAccountBranch(landLordBankAddress);
										newBranch.setRentRevisedDueOn(rentDueOn);
										newBranch.setRemarks(remarks);
									}
								}
							}
							bnchcrud.save(user, newBranch, entityManager);
							auditDAO.saveAuditLogs("added/updated branch basic details", user, newBranch.getId(),
									Branch.class, ipAddress, "", entityManager);
							// Map<String, ActorRef> orgregistrered = new HashMap<String, ActorRef>();
							// Object[] keyArray = AdminActor.adminRegistered.keySet().toArray();
							// for (int j = 0; j < keyArray.length; j++) {
							// StringBuilder sbquery = new StringBuilder("");
							// sbquery.append("select obj from Users obj WHERE obj.email ='" + keyArray[j]
							// + "' and obj.presentStatus=1");
							// List<Users> users = genericDAO.executeSimpleQuery(sbquery.toString(),
							// entityManager);
							// if (!users.isEmpty() && users.get(0).getOrganization().getId() == newBranch
							// .getOrganization().getId()) {
							// orgregistrered.put(keyArray[i].toString(),
							// AdminActor.adminRegistered.get(keyArray[i]));
							// }
							// }
							Map<String, String> countries = CountryCurrencyUtil.getCountries();
							String country1 = "";
							if (newBranch.getCountry() != null) {
								country = String.valueOf(newBranch.getCountry());
							}
							String actionText = "";
							if (newBranch.getPresentStatus() == 0) {
								actionText = "Activate";
							}
							if (newBranch.getPresentStatus() == 1) {
								actionText = "Deactivate";
							}
							// AdminActor.addBranch(newBranch.getId(), newBranch.getName(),
							// newBranch.getGstin(), country,
							// newBranch.getLocation(), newBranch.getPhoneNumber(), orgregistrered, oldName,
							// actionText);
							result.put("id", newBranch.getId());
							result.put("branchName", newBranch.getName());
							result.put("branchgstin", newBranch.getGstin());
							result.put("country", country);
							result.put("location", newBranch.getLocation());
							result.put("phoneNumber", newBranch.getPhoneNumber());
							result.put("oldName", oldName);
							result.put("actionText", actionText);
							// branchCashService.saveBranchCash(json, newBranch,user,
							// organization,rentRevisionDueOn,premise, entityManager, genericDAO, auditDAO,
							// emailsession, ipAddress);

							OrganizationKeyOfficials newkeyoff = new OrganizationKeyOfficials();
							Users branchOfficerUsers = new Users();
							UsersRoles branchOfficerUsersRole = new UsersRoles();

							criterias.clear();
							criterias.put("email", officerEmail);
							criterias.put("organization.id", organization.getId());
							criterias.put("presentStatus", 1);
							Users newBranchOfficerDuringUpdate = genericDAO.getByCriteria(Users.class, criterias,
									entityManager);
							newkeyoff.setBranch(newBranch);
							newkeyoff.setName(officerName);
							newkeyoff.setDesignation(designation);
							newkeyoff.setCity(officerCity);
							int countryId1 = 0;
							if (officerCountry != null) {
								String countryId2 = CountryCurrencyUtil.getCountryId(officerCountry);
								if (countryId2 != null) {
									countryId1 = IdosUtil.convertStringToInt(countryId2);
									newkeyoff.setCountry(countryId1);
								}
							}

							String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
									"[a-zA-Z0-9_+&*-]+)*@" +
									"(?:[a-zA-Z0-9-]+\\.)+[a-z" +
									"A-Z]{2,7}$";

							Pattern emailPattern = Pattern.compile(emailRegex);
							if (officerEmail != null && !officerEmail.equals("")
									&& emailPattern.matcher(officerEmail).matches()) {
								newkeyoff.setEmail(officerEmail);
							} else {
								throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
										IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
										"incorrect email id " + officerEmail);
							}
							newkeyoff.setCtryPhCode(countryCode1);
							newkeyoff.setPhoneNumber(officialPhoneNumber);
							newkeyoff.setPersonalPhoneCountryCode(countryCode2);
							newkeyoff.setPersonalPhoneNumber(personalPhoneNumber);
							newkeyoff.setOrganization(organization);
							genericDAO.saveOrUpdate(newkeyoff, user, entityManager);
							auditDAO.saveAuditLogs("updated Branch officers details", user, newkeyoff.getId(),
									Users.class, ipAddress, "", entityManager);

							branchOfficerUsers.setFullName(officerName);
							branchOfficerUsers.setEmail(officerEmail);
							branchOfficerUsers.setBranchKeyOff(newkeyoff);
							branchOfficerUsers.setOrganization(organization);
							branchOfficerUsers.setBranch(newBranch);
							branchOfficerUsers.setDesignation(designation);
							genericDAO.saveOrUpdate(branchOfficerUsers, user, entityManager);
							auditDAO.saveAuditLogs("updated Branch officers user details", user,
									branchOfficerUsers.getId(), Users.class, ipAddress, "", entityManager);

							String password = PasswordUtil.gen(10);
							String body1 = userAccountCreation
									.render(branchOfficerUsers.getEmail(), password, ConfigParams.getInstance()).body();
							final String username = ConfigFactory.load().getString("smtp.user");
							Session session = emailsession;
							String subject = "Successfully Created Users for Organization " + organization.getName();
							mailTimer1(body1, username, session, branchOfficerUsers.getEmail(), null, subject);

							criterias.clear();
							criterias.put("id", 3L);
							criterias.put("presentStatus", 1);
							Role usrRole = genericDAO.getByCriteria(Role.class, criterias, entityManager);
							criterias.clear();
							branchOfficerUsersRole.setRole(usrRole);
							branchOfficerUsersRole.setUser(branchOfficerUsers);
							branchOfficerUsersRole.setOrganization(organization);
							branchOfficerUsersRole.setBranch(newBranch);
							branchOfficerUsersRole.setBranchKeyOff(newkeyoff);
							genericDAO.saveOrUpdate(branchOfficerUsersRole, user, entityManager);
							auditDAO.saveAuditLogs("updated Branch officers user roles details", user,
									branchOfficerUsersRole.getId(), UsersRoles.class, ipAddress, "", entityManager);
							if (log.isLoggable(Level.FINE))
								log.log(Level.FINE, "updated Branch officers user roles details 1");

							StatutoryDetails newOrgStatDetails = new StatutoryDetails();
							newOrgStatDetails.setBranch(newBranch);
							newOrgStatDetails.setAlertForAction(alertForAction);
							newOrgStatDetails.setAlertForInformation(alertForInformation);
							newOrgStatDetails.setStatutoryDetails(statutaryDetails);
							newOrgStatDetails.setNameAddressOfConsultant(nameAndAddressOfConsultant);
							newOrgStatDetails.setOrganization(organization);
							if (!validityFrom.trim().equals("") && validityFrom.trim() != null) {
								newOrgStatDetails
										.setValidFrom(mysqldf.parse(mysqldf.format(idosdf.parse(validityFrom))));
							}
							if (!validityTo.trim().equals("") && validityTo.trim() != null) {
								newOrgStatDetails.setValidTo(mysqldf.parse(mysqldf.format(idosdf.parse(validityTo))));
							}
							newOrgStatDetails.setRegistrationNumber(registrationNumber);
							newOrgStatDetails.setRemarks(remarks1);
							orgstatdtlscrud.save(user, newOrgStatDetails, entityManager);
							auditDAO.saveAuditLogs("updated branch statutory details", user, newOrgStatDetails.getId(),
									StatutoryDetails.class, ipAddress, "", entityManager);

							OrganizationOperationalRemainders opeRem = new OrganizationOperationalRemainders();
							opeRem.setAlertForAction(alertForAction1);
							opeRem.setAlertForInformation(alertForInformation1);
							opeRem.setBranch(newBranch);
							opeRem.setOrganization(organization);
							if (!recurrance.equals("") && recurrance != null) {
								if (recurrance.equals("Weekly")) {
									opeRem.setRecurrences(1);
								} else if (recurrance.equals("Monthly")) {
									opeRem.setRecurrences(2);
								} else if (recurrance.equals("Quarterly")) {
									opeRem.setRecurrences(3);
								} else if (recurrance.equals("Half Yearly")) {
									opeRem.setRecurrences(4);
								} else if (recurrance.equals("Annually")) {
									opeRem.setRecurrences(5);
								} else if (recurrance.equals("Once In 2 Years")) {
									opeRem.setRecurrences(6);
								} else if (recurrance.equals("Once In 3 years")) {
									opeRem.setRecurrences(7);
								} else if (recurrance.equals("One Time")) {
									opeRem.setRecurrences(8);
								}
							}

							opeRem.setRequiements(requirements);
							if (!validityTo1.trim().equals("") && validityTo1.trim() != null) {
								opeRem.setValidTo(mysqldf.parse(mysqldf.format(idosdf.parse(validityTo1))));
							}
							orgOpeRemCrud.save(user, opeRem, entityManager);
							auditDAO.saveAuditLogs("updated branch operational remainders details", user,
									opeRem.getId(), OrganizationOperationalRemainders.class, ipAddress, "",
									entityManager);

							/*
							 * BranchDepositBoxKey newBranchDepBoxKey=new BranchDepositBoxKey();
							 * newBranchDepBoxKey.setApprovalAmountLimit(Double.parseDouble(approvalLimit));
							 * newBranchDepBoxKey.setEmail(custodianEmail);
							 * newBranchDepBoxKey.setBranch(newBranch);
							 * newBranchDepBoxKey.setCashierEmail(cashierEmail);
							 * newBranchDepBoxKey.setCashierKnowledgeLibrary(cashierKnowledgeLibrary);
							 * newBranchDepBoxKey.setCashierName(cashierName);
							 * newBranchDepBoxKey.setCashierPhnNo(cashierPhoneNumber);
							 * newBranchDepBoxKey.setCashierPhnNoCountryCode(phoneCountryCode4);
							 * newBranchDepBoxKey.setEmail(bankInstrumentCustodianEmail);
							 * newBranchDepBoxKey.setName(bankInstrumentCustodianName);
							 * newBranchDepBoxKey.setPhoneNumber(cutodianPhoneNumber);
							 * newBranchDepBoxKey.setCountryPhnCode(phoneCountryCode3);
							 * newBranchDepBoxKey.setOpeningBalance(Double.parseDouble(openingBalance));
							 * if(pettyCashTransactionApprovalRequired.equals("Yes")){
							 * Integer approvalRequired=1;
							 * newBranchDepBoxKey.setPettyCashTxnApprovalRequired(approvalRequired);
							 * }
							 * 
							 * else if(pettyCashTransactionApprovalRequired.equals("No")){
							 * Integer approvalRequired=0;
							 * newBranchDepBoxKey.setPettyCashTxnApprovalRequired(approvalRequired);
							 * }
							 * newBranchDepBoxKey.setPettyCashOpeningBalance(Double.parseDouble(
							 * pettyCashOpeningBalance));
							 * bnchdepboxcrud.save(user, newBranchDepBoxKey, entityManager);
							 */

							/*
							 * criterias.clear();
							 * criterias.put("email", cashierEmail);
							 * criterias.put("organization.id", organization.getId());
							 * Users newCashierDuringUpdate = genericDAO.getByCriteria(Users.class,
							 * criterias, entityManager);
							 * Users branchCashierUser = new Users();
							 * criterias.clear();
							 * criterias.put("id", 8L);
							 * Role role = genericDAO.getByCriteria(Role.class, criterias, entityManager);
							 * branchCashierUser.setFullName(cashierName);
							 * branchCashierUser.setEmail(cashierEmail);
							 * branchCashierUser.setOrganization(organization);
							 * branchCashierUser.setBranch(newBranch);
							 * branchCashierUser.setBranchSafeDepositBox(newBranchDepBoxKey);
							 * genericDAO.saveOrUpdate(branchCashierUser, user, entityManager);
							 * auditDAO.
							 * saveAuditLogs("updated cashier user for the branch cash/safe deposit box",
							 * user, branchCashierUser.getId(), Users.class, ipAddress,"", entityManager);
							 * 
							 * 
							 * String password1 = PasswordUtil.gen(10);
							 * String body2 = userAccountCreation.render(branchCashierUser.getEmail(),
							 * password1).body();
							 * final String username2 = ConfigFactory.load().getString("smtp.user");
							 * Session session2 = emailsession;
							 * String subject2 = "Successfully Created Cashier Users for Organization " +
							 * organization.getName();
							 * StaticController.mailTimer1(body2, username2, session2,
							 * branchCashierUser.getEmail(), null, subject2);
							 * 
							 * 
							 * UsersRoles branchCashierUserRoles = new UsersRoles();
							 * branchCashierUserRoles.setRole(role);
							 * branchCashierUserRoles.setUser(branchCashierUser);
							 * branchCashierUserRoles.setOrganization(organization);
							 * branchCashierUserRoles.setBranch(newBranch);
							 * branchCashierUserRoles.setBranchSafeDepositBox(newBranchDepBoxKey);
							 * genericDAO.saveOrUpdate(branchCashierUserRoles, user, entityManager);
							 * auditDAO.
							 * saveAuditLogs("Created cashier user roles for the branch cash/safe deposit box"
							 * , user, branchCashierUserRoles.getId(), UsersRoles.class, ipAddress,"",
							 * entityManager);
							 */

							BranchInsurance newBnchInsurance = new BranchInsurance();
							newBnchInsurance.setAlertOfAction(alertForAction2);
							newBnchInsurance.setAlertOfInformation(alertForInformation2);
							newBnchInsurance.setBranch(newBranch);
							newBnchInsurance.setOrganization(organization);
							if (!validityFrom2.trim().equals("") && validityFrom2.trim() != null) {
								newBnchInsurance
										.setPolicyValidFrom(mysqldf.parse(mysqldf.format(idosdf.parse(validityFrom2))));
							}
							if (!validityTo.trim().equals("") && validityTo.trim() != null) {
								newBnchInsurance
										.setPolicyValidTo(mysqldf.parse(mysqldf.format(idosdf.parse(validityTo2))));
							}
							newBnchInsurance.setPolicyType(policyType);
							newBnchInsurance.setPolicyNumber(policyNumber);
							newBnchInsurance.setRemarks(remarks2);
							newBnchInsurance.setInsurenceCompany(insuranceCompany);
							if (annualPremier != null && !annualPremier.equals("")) {
								newBnchInsurance.setAnnualPremium(IdosUtil.convertStringToDouble(annualPremier));
							}
							bnchInscrud.save(user, newBnchInsurance, entityManager);
							auditDAO.saveAuditLogs("updated branch insurence", user, newBnchInsurance.getId(),
									BranchInsurance.class, ipAddress, "", entityManager);

							/*
							 * BranchBankAccounts newBnchBnkAct=new BranchBankAccounts();
							 * newBnchBnkAct.setAccountNumber(accountNumber);
							 * newBnchBnkAct.setBankName(bankName);
							 * newBnchBnkAct.setBankAddress(bankAddress);
							 * //newBnchBnkAct.setAccountType(accountType);
							 * newBnchBnkAct.setBankNumberPhnCtryCode(phoneCountryCode5);
							 * 
							 * newBnchBnkAct.setAuthorizedSignatoryName(authorizedSignatoryName);
							 * newBnchBnkAct.setAuthorizedSignatoryEmail(authorizedSignatoryEmail);
							 * newBnchBnkAct.setBranch(newBranch);
							 * newBnchBnkAct.setOpeningBalance(Double.parseDouble(bankOpeningBalance));
							 * newBnchBnkAct.setOrganization(organization);
							 * newBnchBnkAct.setPhoneNumber(bankPhoneNumber);
							 * newBnchBnkAct.setRoutingNumber(routingNumber);
							 * newBnchBnkAct.setSwiftCode(swiftCode);
							 * BankAccountEnumType bankAccountType=BankAccountEnumType.valueOf(accountType);
							 * Integer accountTypeId=bankAccountType.getId();
							 * newBnchBnkAct.setAccountType(accountTypeId);
							 * bnchBankActcrud.save(user, newBnchBnkAct, entityManager);
							 * BranchBankAccountBalance branchBankAccountBal = new
							 * BranchBankAccountBalance();
							 * branchBankAccountBal.setBranchBankAccounts(newBnchBnkAct);
							 * branchBankAccountBal.setBranch(newBranch);
							 * branchBankAccountBal.setOrganization(organization);
							 * branchBankAccountBal.setDate(Calendar.getInstance().getTime());
							 * 
							 * if(bankOpeningBalance!=null && !bankOpeningBalance.trim().equals("")){
							 * newBnchBnkAct.setOpeningBalance(Double.parseDouble(bankOpeningBalance.trim())
							 * );
							 * branchBankAccountBal.setAmountBalance(Double.parseDouble(bankOpeningBalance.
							 * trim()));
							 * branchBankAccountBal.setResultantCash(Double.parseDouble(bankOpeningBalance.
							 * trim()));
							 * }
							 * genericDAO.saveOrUpdate(branchBankAccountBal, user, entityManager);
							 * genericDAO.saveOrUpdate(newBnchBnkAct, user, entityManager);
							 * auditDAO.saveAuditLogs("created branch bank account details", user,
							 * newBnchBnkAct.getId(), BranchBankAccounts.class, ipAddress,"",
							 * entityManager);
							 */

							String statbody = branchStatutoryAlert.render(organization.getName(), branchName,
									statutaryDetails, registrationNumber, validityTo, alertForAction,
									alertForInformation, remarks, ConfigParams.getInstance()).body();
							final String statusername = ConfigFactory.load().getString("smtp.user");
							Session statsession = emailsession;
							String statsubject = organization.getName() + " Branch " + branchName + " Statutory Alert";
							String operrembody = branchOperationalAlert
									.render(organization.getName(), branchName, requirements, validityTo1,
											alertForAction1, alertForInformation1, remarks1, ConfigParams.getInstance())
									.body();
							final String operremusername = ConfigFactory.load().getString("smtp.user");
							Session operremsession = emailsession;
							String operremsubject = organization.getName() + " Branch " + branchName
									+ " Operational remainder Alert";
							String insbody = branchInsurenceAlert.render(organization.getName(), branchName, policyType,
									policyNumber, insuranceCompany, annualPremier, validityTo2, alertForAction2,
									alertForInformation2, remarks2, ConfigParams.getInstance()).body();
							final String insusername = ConfigFactory.load().getString("smtp.user");
							Session inssession = emailsession;
							String inssubject = organization.getName() + " Branch " + branchName + " Insurences Alert";
							/* only creator role is allowed as confirmed by Srikanth on 17th March 17 */
							/*
							 * if(branchAdmin!=null && !branchAdmin.equals("")){
							 * criterias.clear();
							 * criterias.put("email", branchAdmin);
							 * Users user1=genericDAO.getByCriteria(Users.class, criterias, entityManager);
							 * if(user1!=null){
							 * Role branchAdminRole=Role.findById(9L);
							 * criterias.clear();
							 * criterias.put("branch.id", newBranch.getId());
							 * criterias.put("role.id", branchAdminRole.getId());
							 * UsersRoles usrRoles=genericDAO.getByCriteria(UsersRoles.class, criterias,
							 * entityManager);
							 * if(usrRoles==null){
							 * usrRoles=new UsersRoles();
							 * }
							 * usrRoles.setUser(user1);
							 * usrRoles.setRole(branchAdminRole);
							 * usrRoles.setBranch(newBranch);
							 * usrRoles.setOrganization(organization);
							 * genericDAO.saveOrUpdate(usrRoles, user1, entityManager);
							 * }
							 * }
							 */
						}
						transaction.commit();
					}
				} catch (Exception ex) {
					log.log(Level.SEVERE, "Error", ex);
				}

			}
			ObjectNode row = Json.newObject();
			row.put("message", "Uploaded Successfully");
			an.add(row);
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
		return Results.ok(result);
	}

	@Transactional
	public Result checkForDuplicacy(String name, Request request) {
		System.out.println("checkforDuplicacy" + name);
		Users user = getUserInfo(request);
		if (user == null) {
			return unauthorized();
		}
		ObjectNode result = Json.newObject();
		try {
			List<Branch> entityList = Branch.findByName(entityManager, user.getOrganization(), name);
			if (entityList.size() > 0) {
				result.put("ispresent", "true");
				result.put("dataid", entityList.get(0).getId());
			} else {
				result.put("ispresent", "false");
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}
	@Transactional
	public Result getAllBranchDetails(Request request) {
		log.log(Level.FINE, ">>>> Start");
		EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		Users usrinfo = null;
		String userEmail = null;
		String orgaName = null;
		try {
			usrinfo = getUserInfo(request);
			if (usrinfo == null) {
				session.removing();
				throw new IDOSException(IdosConstants.SESSION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
						"user email is not found in session", IdosConstants.SESSION_EXCEPTION);
			}
			// userEmail = usrinfo.getEmail();
			// orgaName = usrinfo.getOrganization().getName();
			Map<String, Object> criterias = new HashMap<String, Object>();
			//ArrayNode organ = result.putArray("organizationData");
			ArrayNode branchlistan = result.putArray("branchListData");
			criterias.put("organization.id", usrinfo.getOrganization().getId());
			List<Branch> branchList = genericDAO.findByCriteria(Branch.class, criterias, "id", false, entityManager);
			System.out.println("<<<<<branchList>>>>>" + branchList);
			if (branchList.size() > 0) {
				for (Branch branchTableList : branchList) {
					ObjectNode row = Json.newObject();
					row.put("id", branchTableList.getId());
					row.put("name", branchTableList.getName());
					String country = "";
					if (branchTableList.getCountry() != null) {
						country = branchTableList.getCountry().toString();
					}
					row.put("country", country);
					row.put("location", branchTableList.getLocation());
					row.put("phone", branchTableList.getPhoneNumber());
					if (branchTableList.getPresentStatus() == 0) {
						row.put("actionText", "Activate");
					} else if (branchTableList.getPresentStatus() == 1) {
						row.put("actionText", "Deactivate");
					}
					if (branchTableList.getGstin() != null) {
						row.put("branchgstin", branchTableList.getGstin());
					}

					branchlistan.add(row);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return Results.ok(result);	
	}

}
