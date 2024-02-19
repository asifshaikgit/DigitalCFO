
package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.Session;
import javax.persistence.EntityManager;

import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.*;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import akka.stream.javadsl.*;
import akka.actor.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import play.db.jpa.JPAApi;

import javax.transaction.Transactional;

import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.WebSocket;
import views.html.*;
import actor.AdminActor;
import actor.ProjectTransactionActor;
import com.idos.enumtype.BankAccountEnumType;
import play.Application;
import javax.inject.Inject;
import akka.NotUsed;
import play.mvc.Http.Request;
import play.libs.Files.TemporaryFile;
//import play.mvc.Http.Session;
import java.util.logging.Level;

public class ConfigurationController extends StaticController {
	private static final Class<? extends Action<?>>[] Transactional = null;
	private static final String SPECIFICS_HQL = "Select obj from Specifics obj where obj.presentStatus=1 and obj.organization.id=?1 and obj.presentStatus=1 ORDER BY obj.id desc";
	private static EntityManager entityManager;
	public static Application application;
	private Request request;
	// private Http.Session session = request.session();

	@Inject
	public ConfigurationController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
		this.application = application;
	}

	@Transactional
	public Result updateOrganization(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Users user = getUserInfo(request);
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String chtOfActStatus = json.findValue("chtOfActStatus").asText();
			String orgnId = json.findValue("organizationId").asText();
			Organization orgn = Organization.findById(IdosUtil.convertStringToLong(orgnId));
			orgn.setCompanyHasChartOfAccounts(IdosUtil.convertStringToInt(chtOfActStatus));
			if (IdosUtil.convertStringToInt(chtOfActStatus) == 1) {
				orgcrud.save(user, orgn, entityManager);
			}
			if (IdosUtil.convertStringToInt(chtOfActStatus) == 0) {
				createnewchartofacc(request, json, entityManager);
				orgcrud.save(user, orgn, entityManager);
			}
			transaction.commit();
		} catch (Exception ex) {
			reportException(entityManager, transaction, null, ex, result);//
		} catch (Throwable th) {
			reportThrowable(entityManager, transaction, null, th, result);
		}
		return Results.ok(result);
	}

	public static void createnewchartofacc(Request request, JsonNode json, EntityManager entityManager)
			throws Exception {
		log.log(Level.FINE, ">>>> Start");
		Users user = getUserInfo(request);
		try {
			String orgId = json.findPath("organizationId").asText();
			String parentChAc = json.findPath("parentChartAc").asText();
			String currenttLiabSubCat = json.findPath("currenttLiabSubCat").asText();
			String directtExpSubCat = json.findPath("directtExpSubCat").asText();
			String directtExpUtilSubCat = json.findPath("directtExpUtilSubCat").asText();
			String indirectExpSubCat = json.findPath("indirectExpSubCat").asText();
			String indirectExpPtyCshSubCat = json.findPath("indirectExpPtyCshSubCat").asText();
			String indirectExpBdSubCat = json.findPath("indirectExpBdSubCat").asText();
			int flag = 1;
			String bankAccSubCat[] = null, bankAccNo[] = null, capAccSubCat[] = null, fxdAstSubCat[] = null,
					cntAstSubCat[] = null, cntLiabProvSubCat[] = null;
			String chAcc[] = null, cntLiabSubCat[] = null, dctExpSubCat[] = null, dctExpUtilSubCat[] = null,
					indctExpSubCat[] = null;
			String indctExpPtyCshSubCat[] = null, indctExpBdSubCat[] = null;
			if (parentChAc.length() != 0) {
				chAcc = parentChAc.substring(0, (parentChAc.length()) - 1).split(",");
			}
			if (currenttLiabSubCat.length() != 0) {
				cntLiabSubCat = currenttLiabSubCat.substring(0, (currenttLiabSubCat.length()) - 1).split(",");
			}
			if (directtExpSubCat.length() != 0) {
				dctExpSubCat = directtExpSubCat.substring(0, (directtExpSubCat.length()) - 1).split(",");
			}
			if (directtExpUtilSubCat.length() != 0) {
				dctExpUtilSubCat = directtExpUtilSubCat.substring(0, (directtExpUtilSubCat.length()) - 1).split(",");
			}
			if (indirectExpSubCat.length() != 0) {
				indctExpSubCat = indirectExpSubCat.substring(0, (indirectExpSubCat.length()) - 1).split(",");
			}
			if (indirectExpPtyCshSubCat.length() != 0) {
				indctExpPtyCshSubCat = indirectExpPtyCshSubCat.substring(0, (indirectExpPtyCshSubCat.length()) - 1)
						.split(",");
			}
			if (indirectExpBdSubCat.length() != 0) {
				indctExpBdSubCat = indirectExpBdSubCat.substring(0, (indirectExpBdSubCat.length()) - 1).split(",");
			}
			Organization org = Organization.findById(Long.parseLong(orgId));
			for (int i = 0; i < chAcc.length; i++) {
				Long parentActCode = null;
				Long maxActCode = null;
				Particulars tmpPart = new Particulars();
				tmpPart.setOrganization(org);
				tmpPart.setName(chAcc[i]);
				List maxPartObj = Particulars.findMaxAccountCode(entityManager, org.getId());
				if (maxPartObj.get(0) != null) {
					maxActCode = (Long) maxPartObj.get(0);
					tmpPart.setAccountCode(AccountCodeUtil.generateAccountCode(parentActCode, maxActCode));
				} else {
					maxActCode = 1000000000000000000L;
					tmpPart.setAccountCode(maxActCode);
				}
				tmpPart.setAccountCodeHirarchy("/");
				partcrud.save(user, tmpPart, entityManager);
				if (cntLiabSubCat != null) {
					if (cntLiabSubCat.length != 0 && flag != 0) {
						flag = 0;
						for (int j = 0; j < cntLiabSubCat.length; j++) {
							parentActCode = null;
							maxActCode = null;
							Specifics cntLiab = new Specifics();
							cntLiab.setOrganization(org);
							cntLiab.setParticularsId(tmpPart);
							cntLiab.setName(cntLiabSubCat[j]);
							List maxSpecfObj = Specifics.findMaxAccountCode4Particular(entityManager, org.getId(),
									tmpPart.getId());
							parentActCode = tmpPart.getAccountCode();
							if (maxSpecfObj.get(0) != null) {
								maxActCode = (Long) maxSpecfObj.get(0);
							} else {
								maxActCode = null;
							}
							Long actCode = AccountCodeUtil.generateAccountCode(parentActCode, maxActCode);
							String aCode = String.valueOf(actCode);
							int length = aCode.length();
							int pos = aCode.indexOf("9");
							int itrate = 0;
							String one = "1";
							if (pos != -1) {
								itrate = length - pos - 2;
								for (int m = 0; m < itrate; m++) {
									one += "0";
								}
								if (parentActCode != null) {
									actCode = parentActCode + Long.valueOf(one);
								}
								if (maxActCode != null) {
									actCode = maxActCode + Long.valueOf(one);
								}
							}
							cntLiab.setAccountCode(actCode);
							cntLiab.setAccountCodeHirarchy(
									tmpPart.getAccountCodeHirarchy() + tmpPart.getAccountCode() + "/");
							specfcrud.save(user, cntLiab, entityManager);
						}
						cntLiabSubCat = null;
					}
				}
				if (dctExpSubCat != null) {
					if (dctExpSubCat.length != 0 && flag != 0) {
						flag = 0;
						Specifics parentDctExpUtil = null;
						for (int j = 0; j < dctExpSubCat.length; j++) {
							parentActCode = null;
							maxActCode = null;
							Specifics dctExp = new Specifics();
							dctExp.setOrganization(org);
							dctExp.setParticularsId(tmpPart);
							dctExp.setName(dctExpSubCat[j]);
							List maxSpecfObj = Specifics.findMaxAccountCode4Particular(entityManager, org.getId(),
									tmpPart.getId());
							parentActCode = tmpPart.getAccountCode();
							if (maxSpecfObj.get(0) != null) {
								maxActCode = (Long) maxSpecfObj.get(0);
							} else {
								maxActCode = null;
							}
							Long actCode = AccountCodeUtil.generateAccountCode(parentActCode, maxActCode);
							String aCode = String.valueOf(actCode);
							int length = aCode.length();
							int pos = aCode.indexOf("9");
							int itrate = 0;
							String one = "1";
							if (pos != -1) {
								itrate = length - pos - 2;
								for (int m = 0; m < itrate; m++) {
									one += "0";
								}
								if (parentActCode != null) {
									actCode = parentActCode + Long.valueOf(one);
								}
								if (maxActCode != null) {
									actCode = maxActCode + Long.valueOf(one);
								}
							}
							dctExp.setAccountCode(actCode);
							dctExp.setAccountCodeHirarchy(
									tmpPart.getAccountCodeHirarchy() + tmpPart.getAccountCode() + "/");
							specfcrud.save(user, dctExp, entityManager);
						}
						if (dctExpUtilSubCat != null) {
							for (int j = 0; j < dctExpUtilSubCat.length; j++) {
								parentActCode = null;
								maxActCode = null;
								Specifics dctExpUtil = new Specifics();
								dctExpUtil.setOrganization(org);
								dctExpUtil.setParticularsId(tmpPart);
								dctExpUtil.setName(dctExpUtilSubCat[j]);
								dctExpUtil.setAccountCode(null);
								if (j == 0) {
									parentDctExpUtil = dctExpUtil;
									List maxSpecfObj = Specifics.findMaxAccountCode4Particular(entityManager,
											org.getId(), tmpPart.getId());
									parentActCode = tmpPart.getAccountCode();
									if (maxSpecfObj.get(0) != null) {
										maxActCode = (Long) maxSpecfObj.get(0);
									} else {
										maxActCode = null;
									}
									Long actCode = AccountCodeUtil.generateAccountCode(parentActCode, maxActCode);
									String aCode = String.valueOf(actCode);
									int length = aCode.length();
									int pos = aCode.indexOf("9");
									int itrate = 0;
									String one = "1";
									if (pos != -1) {
										itrate = length - pos - 2;
										for (int m = 0; m < itrate; m++) {
											one += "0";
										}
										if (parentActCode != null) {
											actCode = parentActCode + Long.valueOf(one);
										}
										if (maxActCode != null) {
											actCode = maxActCode + Long.valueOf(one);
										}
									}
									dctExpUtil.setAccountCode(actCode);
									dctExpUtil.setAccountCodeHirarchy(
											tmpPart.getAccountCodeHirarchy() + tmpPart.getAccountCode() + "/");
								}
								if (j > 0) {
									List maxSpecfObj = Specifics.findMaxAccountCode4Specific(entityManager, org.getId(),
											parentDctExpUtil.getId());
									parentActCode = parentDctExpUtil.getAccountCode();
									if (maxSpecfObj.get(0) != null) {
										maxActCode = (Long) maxSpecfObj.get(0);
									} else {
										maxActCode = null;
									}
									Long actCode = AccountCodeUtil.generateAccountCode(parentActCode, maxActCode);
									String aCode = String.valueOf(actCode);
									int length = aCode.length();
									int pos = aCode.indexOf("9");
									int itrate = 0;
									String one = "1";
									if (pos != -1) {
										itrate = length - pos - 2;
										for (int m = 0; m < itrate; m++) {
											one += "0";
										}
										if (parentActCode != null) {
											actCode = parentActCode + Long.valueOf(one);
										}
										if (maxActCode != null) {
											actCode = maxActCode + Long.valueOf(one);
										}
									}
									dctExpUtil.setAccountCode(actCode);
									dctExpUtil.setAccountCodeHirarchy(parentDctExpUtil.getAccountCodeHirarchy()
											+ parentDctExpUtil.getAccountCode() + "/");
									dctExpUtil.setParentSpecifics(parentDctExpUtil);
								}
								specfcrud.save(user, dctExpUtil, entityManager);
							}
						}
						dctExpSubCat = null;
						dctExpUtilSubCat = null;
					}
				}
				if (indctExpSubCat != null) {
					if (indctExpSubCat.length != 0 && flag != 0) {
						flag = 0;
						Specifics parentindctExpPtyCsh = null;
						Specifics parentindctExpBd = null;
						for (int j = 0; j < indctExpSubCat.length; j++) {
							parentActCode = null;
							maxActCode = null;
							Specifics indctdctExp = new Specifics();
							indctdctExp.setOrganization(org);
							indctdctExp.setParticularsId(tmpPart);
							indctdctExp.setName(indctExpSubCat[j]);
							List maxSpecfObj = Specifics.findMaxAccountCode4Particular(entityManager, org.getId(),
									tmpPart.getId());
							parentActCode = tmpPart.getAccountCode();
							if (maxSpecfObj.get(0) != null) {
								maxActCode = (Long) maxSpecfObj.get(0);
							} else {
								maxActCode = null;
							}
							Long actCode = AccountCodeUtil.generateAccountCode(parentActCode, maxActCode);
							String aCode = String.valueOf(actCode);
							int length = aCode.length();
							int pos = aCode.indexOf("9");
							int itrate = 0;
							String one = "1";
							if (pos != -1) {
								itrate = length - pos - 2;
								for (int m = 0; m < itrate; m++) {
									one += "0";
								}
								if (parentActCode != null) {
									actCode = parentActCode + Long.valueOf(one);
								}
								if (maxActCode != null) {
									actCode = maxActCode + Long.valueOf(one);
								}
							}
							indctdctExp.setAccountCode(actCode);
							indctdctExp.setAccountCodeHirarchy(
									tmpPart.getAccountCodeHirarchy() + tmpPart.getAccountCode() + "/");
							specfcrud.save(user, indctdctExp, entityManager);
						}
						if (indctExpPtyCshSubCat != null) {
							for (int j = 0; j < indctExpPtyCshSubCat.length; j++) {
								Specifics indctExpPtyCsh = new Specifics();
								indctExpPtyCsh.setOrganization(org);
								indctExpPtyCsh.setParticularsId(tmpPart);
								indctExpPtyCsh.setName(indctExpPtyCshSubCat[j]);
								indctExpPtyCsh.setAccountCode(null);
								if (j == 0) {
									parentindctExpPtyCsh = indctExpPtyCsh;
									List maxSpecfObj = Specifics.findMaxAccountCode4Particular(entityManager,
											org.getId(), tmpPart.getId());
									parentActCode = tmpPart.getAccountCode();
									if (maxSpecfObj.get(0) != null) {
										maxActCode = (Long) maxSpecfObj.get(0);
									} else {
										maxActCode = null;
									}
									Long actCode = AccountCodeUtil.generateAccountCode(parentActCode, maxActCode);
									String aCode = String.valueOf(actCode);
									int length = aCode.length();
									int pos = aCode.indexOf("9");
									int itrate = 0;
									String one = "1";
									if (pos != -1) {
										itrate = length - pos - 2;
										for (int m = 0; m < itrate; m++) {
											one += "0";
										}
										if (parentActCode != null) {
											actCode = parentActCode + Long.valueOf(one);
										}
										if (maxActCode != null) {
											actCode = maxActCode + Long.valueOf(one);
										}
									}
									indctExpPtyCsh.setAccountCode(actCode);
									indctExpPtyCsh.setAccountCodeHirarchy(
											tmpPart.getAccountCodeHirarchy() + tmpPart.getAccountCode() + "/");
								}
								if (j > 0) {
									List maxSpecfObj = Specifics.findMaxAccountCode4Specific(entityManager, org.getId(),
											parentindctExpPtyCsh.getId());
									parentActCode = parentindctExpPtyCsh.getAccountCode();
									if (maxSpecfObj.get(0) != null) {
										maxActCode = (Long) maxSpecfObj.get(0);
									} else {
										maxActCode = null;
									}
									Long actCode = AccountCodeUtil.generateAccountCode(parentActCode, maxActCode);
									String aCode = String.valueOf(actCode);
									int length = aCode.length();
									int pos = aCode.indexOf("9");
									int itrate = 0;
									String one = "1";
									if (pos != -1) {
										itrate = length - pos - 2;
										for (int m = 0; m < itrate; m++) {
											one += "0";
										}
										if (parentActCode != null) {
											actCode = parentActCode + Long.valueOf(one);
										}
										if (maxActCode != null) {
											actCode = maxActCode + Long.valueOf(one);
										}
									}
									indctExpPtyCsh.setAccountCode(actCode);
									indctExpPtyCsh.setAccountCodeHirarchy(parentindctExpPtyCsh.getAccountCodeHirarchy()
											+ parentindctExpPtyCsh.getAccountCode() + "/");
									indctExpPtyCsh.setParentSpecifics(parentindctExpPtyCsh);
								}
								specfcrud.save(user, indctExpPtyCsh, entityManager);
							}
						}
						if (indctExpBdSubCat != null) {
							for (int j = 0; j < indctExpBdSubCat.length; j++) {
								Specifics indctExpBd = new Specifics();
								indctExpBd.setOrganization(org);
								indctExpBd.setParticularsId(tmpPart);
								indctExpBd.setName(indctExpBdSubCat[j]);
								indctExpBd.setAccountCode(null);
								if (j == 0) {
									parentindctExpBd = indctExpBd;
									List maxSpecfObj = Specifics.findMaxAccountCode4Particular(entityManager,
											org.getId(), tmpPart.getId());
									parentActCode = tmpPart.getAccountCode();
									if (maxSpecfObj.get(0) != null) {
										maxActCode = (Long) maxSpecfObj.get(0);
									} else {
										maxActCode = null;
									}
									Long actCode = AccountCodeUtil.generateAccountCode(parentActCode, maxActCode);
									String aCode = String.valueOf(actCode);
									int length = aCode.length();
									int pos = aCode.indexOf("9");
									int itrate = 0;
									String one = "1";
									if (pos != -1) {
										itrate = length - pos - 2;
										for (int m = 0; m < itrate; m++) {
											one += "0";
										}
										if (parentActCode != null) {
											actCode = parentActCode + Long.valueOf(one);
										}
										if (maxActCode != null) {
											actCode = maxActCode + Long.valueOf(one);
										}
									}
									indctExpBd.setAccountCode(actCode);
									indctExpBd.setAccountCodeHirarchy(
											tmpPart.getAccountCodeHirarchy() + tmpPart.getAccountCode() + "/");
								}
								if (j > 0) {
									List maxSpecfObj = Specifics.findMaxAccountCode4Specific(entityManager, org.getId(),
											parentindctExpBd.getId());
									parentActCode = parentindctExpBd.getAccountCode();
									if (maxSpecfObj.get(0) != null) {
										maxActCode = (Long) maxSpecfObj.get(0);
									} else {
										maxActCode = null;
									}
									Long actCode = AccountCodeUtil.generateAccountCode(parentActCode, maxActCode);
									String aCode = String.valueOf(actCode);
									int length = aCode.length();
									int pos = aCode.indexOf("9");
									int itrate = 0;
									String one = "1";
									if (pos != -1) {
										itrate = length - pos - 2;
										for (int m = 0; m < itrate; m++) {
											one += "0";
										}
										if (parentActCode != null) {
											actCode = parentActCode + Long.valueOf(one);
										}
										if (maxActCode != null) {
											actCode = maxActCode + Long.valueOf(one);
										}
									}
									indctExpBd.setAccountCode(actCode);
									indctExpBd.setAccountCodeHirarchy(parentindctExpBd.getAccountCodeHirarchy()
											+ parentindctExpBd.getAccountCode() + "/");
									indctExpBd.setParentSpecifics(parentindctExpBd);
								}
								specfcrud.save(user, indctExpBd, entityManager);
							}
						}
						indctExpSubCat = null;
						indctExpPtyCshSubCat = null;
						indctExpBdSubCat = null;
					}
				}
				flag = 1;
			}

		} catch (Exception ex) {
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "CreateNewChartOfAcc Email", "CreateNewChartOfAcc Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			throw new Exception();
		}
	}

	@Transactional
	public Result saveOrUpdateOrganizationDetails(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		Users user = null;
		Map<Object, Object> criteria = new HashMap<Object, Object>();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("usermail").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			if (user == null) {
				session.removing();
				throw new IDOSException(IdosConstants.SESSION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
						"user email is not found", IdosConstants.SESSION_EXCEPTION);
			}
			Long orgId = (json.findValue("orgId") == null || "".equals(json.findValue("orgId").asText())) ? 0l
					: json.findValue("orgId").asLong();
			String companyName = json.findPath("companyName").asText();
			String corporateEmail = json.findPath("corpEmail").asText();
			String addr = json.findPath("addr").asText();
			String phNo = json.findPath("phno").asText();
			String phNoctryCode = json.findPath("regphnoccode").asText();
			String webUrl = json.findPath("weburl").asText();
			String GSTApplicable = json.findPath("GSTApplicable").asText();
			String country = json.findPath("orgcountry").asText();
			String currency = json.findPath("orgCurrency").asText();
			String stYr = json.findPath("finstyr") != null ? json.findPath("finstyr").asText() : null;
			String endYr = json.findPath("finendyr") != null ? json.findPath("finendyr").asText() : null;
			/*
			 * String prevYrAcc = json.findPath("prevyracc") != null ?
			 * json.findPath("prevyracc").asText() : null;
			 * String prevYrTaxRtrn = json.findPath("prevyrtaxrtrn") != null ?
			 * json.findPath("prevyrtaxrtrn").asText() : null;
			 * String orgChart = json.findPath("orgchart") != null ?
			 * json.findPath("orgchart").asText() : null;
			 * String accMan = json.findPath("accman") != null ?
			 * json.findPath("accman").asText() : null;
			 * String signatoryList = json.findPath("signatoryList") != null ?
			 * json.findPath("signatoryList").asText() : null;
			 */
			// String compTemplates = json.findPath("compTemplates") != null ?
			// json.findPath("compTemplates").asText() : null;
			String companyLogo = json.findPath("companyLogo") != null ? json.findPath("companyLogo").asText() : null;
			// String orgLogoEncodedData=json.findPath("orgLogoEncodedData") != null ?
			// json.findPath("orgLogoEncodedData").asText() : null;
			String auditedAccountsUploads = json.findPath("auditedAccountsUploads") != null
					? json.findPath("auditedAccountsUploads").asText()
					: null;
			String taxReturnsUploads = json.findPath("taxReturnsUploads") != null
					? json.findPath("taxReturnsUploads").asText()
					: null;
			String organizationChartUploads = json.findPath("organizationChartUploads") != null
					? json.findPath("organizationChartUploads").asText()
					: null;
			String accountingManualUploads = json.findPath("accountingManualUploads") != null
					? json.findPath("accountingManualUploads").asText()
					: null;
			String listOfStatergiesUploads = json.findPath("listOfStatergiesUploads") != null
					? json.findPath("listOfStatergiesUploads").asText()
					: null;
			String templatesForCompanyUploads = json.findPath("templatesForCompanyUploads") != null
					? json.findPath("templatesForCompanyUploads").asText()
					: null;
			String companyProductListings = json.findPath("companyProductListings").asText();
			String isCompositionScheme = json.findValue("isCompositionScheme").asText();
			Integer limitForBackDatedTxn = json.findPath("limitForBackDatedTxn") != null
					|| !"".equals(json.findPath("limitForBackDatedTxn").asText())
							? json.findPath("limitForBackDatedTxn").asInt()
							: null;
			Integer uploadDestination = json.findPath("uploadDestinationId") != null
					|| !"".equals(json.findPath("uploadDestinationId").asText())
							? json.findPath("uploadDestinationId").asInt()
							: 0;

			Organization org = Organization.findById(orgId);
			criteria.put("isHeadQuarter", 1);
			criteria.put("organization.id", org.getId());
			criteria.put("presentStatus", 1);
			Branch headQuarterBranch = genericDAO.getByCriteria(Branch.class, criteria, entityManager);
			Date finStYr = null, finEndYr = null;
			if (stYr != null && stYr != "") {
				finStYr = mysqlmdtdf.parse(mysqlmdtdf.format(idosmdtdf.parse(stYr)));
			}
			if (endYr != null && endYr != "") {
				finEndYr = mysqlmdtdf.parse(mysqlmdtdf.format(idosmdtdf.parse(endYr)));
			}
			org.setName(companyName);
			org.setCorporateMail(corporateEmail);
			org.setRegPhNoCtryCode(phNoctryCode);
			org.setRegisteredPhoneNumber(phNo);
			org.setWebUrl(webUrl);
			if (IdosUtil.convertStringToInt(GSTApplicable) == 1) {// Yes for India
				org.setGstCountryCode("IN");
				result.put("gstcountrycode", "IN");
			}
			org.setAutoItemRegistrationAllowed(IdosUtil.convertStringToInt(companyProductListings));
			org.setIsCompositionScheme(IdosUtil.convertStringToInt(isCompositionScheme));
			if (limitForBackDatedTxn != null) {
				org.setLimitDaysForBackdatedTxn(limitForBackDatedTxn);
			}
			if (country != "") {
				org.setCountry(IdosUtil.convertStringToInt(country));
			}
			org.setAuditedAccountDoc(transactionDao.getAndDeleteSupportingDocument(org.getAuditedAccountDoc(),
					user.getEmail(), auditedAccountsUploads, user, entityManager));
			org.setTaxReturnDoc(transactionDao.getAndDeleteSupportingDocument(org.getTaxReturnDoc(), user.getEmail(),
					taxReturnsUploads, user, entityManager));
			org.setOrganizationChartDoc(transactionDao.getAndDeleteSupportingDocument(org.getOrganizationChartDoc(),
					user.getEmail(), organizationChartUploads, user, entityManager));
			org.setAccountingManualDoc(transactionDao.getAndDeleteSupportingDocument(org.getAccountingManualDoc(),
					user.getEmail(), accountingManualUploads, user, entityManager));
			org.setSignatoryListDoc(transactionDao.getAndDeleteSupportingDocument(org.getSignatoryListDoc(),
					user.getEmail(), listOfStatergiesUploads, user, entityManager));
			org.setCompanyTemplateDoc(transactionDao.getAndDeleteSupportingDocument(org.getCompanyTemplateDoc(),
					user.getEmail(), templatesForCompanyUploads, user, entityManager));
			org.setCurrency(currency);
			org.setCompanyLogo(transactionDao.getAndDeleteSupportingDocument(org.getCompanyLogo(), user.getEmail(),
					companyLogo, user, entityManager));
			org.setRegisteredAddress(addr);
			org.setFinancialStartDate(finStYr);
			org.setFinancialEndDate(finEndYr);
			org.setFileUploadDestination(uploadDestination);
			orgcrud.save(user, org, entityManager);
			FILE_UPLOAD_SERVICE.updateUploadFileLogs(entityManager, user, auditedAccountsUploads, org.getId(),
					IdosConstants.ORG_MODULE);
			FILE_UPLOAD_SERVICE.updateUploadFileLogs(entityManager, user, taxReturnsUploads, org.getId(),
					IdosConstants.ORG_MODULE);
			FILE_UPLOAD_SERVICE.updateUploadFileLogs(entityManager, user, organizationChartUploads, org.getId(),
					IdosConstants.ORG_MODULE);
			FILE_UPLOAD_SERVICE.updateUploadFileLogs(entityManager, user, accountingManualUploads, org.getId(),
					IdosConstants.ORG_MODULE);
			FILE_UPLOAD_SERVICE.updateUploadFileLogs(entityManager, user, listOfStatergiesUploads, org.getId(),
					IdosConstants.ORG_MODULE);
			FILE_UPLOAD_SERVICE.updateUploadFileLogs(entityManager, user, templatesForCompanyUploads, org.getId(),
					IdosConstants.ORG_MODULE);
			FILE_UPLOAD_SERVICE.updateUploadFileLogs(entityManager, user, companyLogo, org.getId(),
					IdosConstants.ORG_MODULE);
			auditDAO.saveAuditLogs("Added/Updated Organization Details", user, org.getId(), Organization.class,
					ipAddress, json.toString(), entityManager);
			transaction.commit();
		} catch (Exception ex) {
			reportException(entityManager, transaction, user, ex, result);//
		} catch (Throwable th) {
			reportThrowable(entityManager, transaction, user, th, result);
		}
		return Results.ok(result);
	}

	@Transactional
	public Result showBranchDetails(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode results = Json.newObject();
		Users user = null;
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			JsonNode json = request.body().asJson();
			ArrayNode branchdetailan = results.putArray("branchdetailsData");
			String branchEntityId = json.findValue("entityPrimaryId").asText();
			ArrayNode branchBankAccountan = results.putArray("branchBankActData");
			ArrayNode branchTaxan = results.putArray("branchTaxData");
			Branch branchDet = Branch.findById(IdosUtil.convertStringToLong(branchEntityId));
			String opnDate = "";
			String country = "";
			if (branchDet != null) {

				ObjectNode row = Json.newObject();
				row.put("id", branchDet.getId());
				row.put("branchName", branchDet.getName());
				if (branchDet.getBranchOpenDate() != null && !branchDet.getBranchOpenDate().equals("")) {
					opnDate = idosdf.format(branchDet.getBranchOpenDate());
				}
				row.put("branchOpenDate", opnDate);
				if (branchDet.getCountry() != null) {
					row.put("country", branchDet.getCountry());
				} else {
					row.put("country", "");
				}
				row.put("branchCurrency", branchDet.getCurrency() == null ? "" : branchDet.getCurrency());
				row.put("branchLocation", branchDet.getLocation() == null ? "" : branchDet.getLocation());
				row.put("branchPhoneNumberCtryCd",
						branchDet.getPhoneNumberCtryCode() == null ? "" : branchDet.getPhoneNumberCtryCode());
				int n = 0;
				if (branchDet.getPhoneNumber() != null && branchDet.getPhoneNumber() != "") {
					n = branchDet.getPhoneNumber().indexOf("-");
					row.put("branchPhoneNumber", branchDet.getPhoneNumber() == null ? ""
							: branchDet.getPhoneNumber().substring(n + 1, branchDet.getPhoneNumber().length()));
				} else {
					row.put("branchPhoneNumber", "");
				}
				row.put("branchAddress", branchDet.getAddress() == null ? "" : branchDet.getAddress());
				row.put("branchStateCode", branchDet.getStateCode() == null ? "" : branchDet.getStateCode());
				if (branchDet.getGstin() != null && !branchDet.getGstin().equals("")) {
					row.put("branchGstinPart1", branchDet.getGstin().substring(0, 2));
					if (branchDet.getGstin().length() > 2) {
						if (branchDet.getGstin().length() > 14) {
							row.put("branchGstinPart2", branchDet.getGstin().substring(2, 15));
						} else {
							row.put("branchGstinPart2", branchDet.getGstin().substring(2));
						}
					} else {
						row.put("branchGstinPart2", "");
					}
				} else {
					row.put("branchGstinPart1", "");
					row.put("branchGstinPart2", "");
				}
				row.put("gstCountryCode", user.getOrganization().getGstCountryCode() == null ? ""
						: user.getOrganization().getGstCountryCode());
				row.put("branchFacility",
						branchDet.getBranchFacility() == null ? "" : branchDet.getBranchFacility().toString());
				if (branchDet.getBranchFacility() != null) {
					if (branchDet.getBranchFacility() == 1 || branchDet.getBranchFacility() == 2) {
						row.put("branchAggreement", branchDet.getAggreement() == null ? "" : branchDet.getAggreement());
						String aggreementValididtyFrom = "", aggreementValididtyTo = "";
						if (branchDet.getAggreementValidFrom() != null
								&& !branchDet.getAggreementValidFrom().equals("")) {
							aggreementValididtyFrom = idosdf.format(branchDet.getAggreementValidFrom());
						}
						row.put("aggreementValididtyFrom", aggreementValididtyFrom);
						if (branchDet.getAggreementValidTo() != null && !branchDet.getAggreementValidTo().equals("")) {
							aggreementValididtyTo = idosdf.format(branchDet.getAggreementValidTo());
						}
						row.put("aggreementValididtyTo", aggreementValididtyTo);
						row.put("periodicityOfPayment", branchDet.getPeriodicityOfPayment() == null ? ""
								: branchDet.getPeriodicityOfPayment().toString());
						row.put("rentPayable",
								branchDet.getRentPayable() == null ? "" : branchDet.getRentPayable().toString());
						row.put("landlordName", branchDet.getLandlordName() == null ? "" : branchDet.getLandlordName());
						row.put("landlordAddress",
								branchDet.getLandlordAddress() == null ? "" : branchDet.getLandlordAddress());
						row.put("bankAccountName",
								branchDet.getBankAccountName() == null ? "" : branchDet.getBankAccountName());
						row.put("bankAccountNumber",
								branchDet.getBankAccountNumber() == null ? "" : branchDet.getBankAccountNumber());
						row.put("bankAccountBranch",
								branchDet.getBankAccountBranch() == null ? "" : branchDet.getBankAccountBranch());
						String rentRevisionDueOn = "";
						if (branchDet.getRentRevisedDueOn() != null && !branchDet.getRentRevisedDueOn().equals("")) {
							rentRevisionDueOn = idosdf.format(branchDet.getRentRevisedDueOn());
						}
						row.put("rentRevisionDueOn", rentRevisionDueOn);
						row.put("alertForActions",
								branchDet.getAlertForAction() == null ? "" : branchDet.getAlertForAction());
						row.put("alertForInformations",
								branchDet.getAlertForInformation() == null ? "" : branchDet.getAlertForInformation());
						row.put("rentRevisionDueRemarks", branchDet.getRemarks() == null ? "" : branchDet.getRemarks());
					}
				}
				DigitalSignatureBranchWise digitalSignatureData = DigitalSignatureBranchWise
						.findByOrgAndBranch(entityManager, user.getOrganization().getId(), branchDet.getId());
				if (digitalSignatureData != null) {
					row.put("digitalSignDocuments", digitalSignatureData.getDigitalSignDocuments());
					row.put("dsPersonName", digitalSignatureData.getPersonName());
					row.put("dsPhoneNo", digitalSignatureData.getPhoneNo());
					row.put("dsEmailId", digitalSignatureData.getEmailId());
					row.put("dsDesignation", digitalSignatureData.getDesignation());
					row.put("dsRefNo", digitalSignatureData.getRefNo());
					row.put("dsKycDetails", digitalSignatureData.getKycDetails());
					row.put("dsValidityFrom", digitalSignatureData.getValidityFrom());
					row.put("dsValidityTo", digitalSignatureData.getValidityTo());
				} else {
					row.put("digitalSignDocuments", "");
					row.put("dsPersonName", "");
					row.put("dsPhoneNo", "");
					row.put("dsEmailId", "");
					row.put("dsDesignation", "");
					row.put("dsRefNo", "");
					row.put("dsKycDetails", "");
				}
				branchdetailan.add(row);
				// brank key off data
				ArrayNode branchkeyoffan = results.putArray("branchKeyOffData");
				String strSql = "select obj from OrganizationKeyOfficials obj where obj.presentStatus=1 and obj.branch.id=?1 and obj.organization.id=?2";
				ArrayList inparam = new ArrayList(2);
				inparam.add(branchDet.getId());
				inparam.add(branchDet.getOrganization().getId());
				List<OrganizationKeyOfficials> bnchKeyOfficialsList = genericDAO.queryWithParams(strSql, entityManager,
						inparam);
				for (OrganizationKeyOfficials bnchKeyOff : bnchKeyOfficialsList) {
					ObjectNode keyoffrow = Json.newObject();
					keyoffrow.put("keyoffId", bnchKeyOff.getId());
					keyoffrow.put("keyoffname", bnchKeyOff.getName() == null ? "" : bnchKeyOff.getName());
					keyoffrow.put("keyoffdesignation",
							bnchKeyOff.getDesignation() == null ? "" : bnchKeyOff.getDesignation());
					String keyoffcountry = "";
					if (bnchKeyOff.getCountry() != null) {
						keyoffrow.put("keyoffcountry", bnchKeyOff.getCountry());
					} else {
						keyoffrow.put("keyoffcountry", "");
					}
					keyoffrow.put("keyoffcity", bnchKeyOff.getCity() == null ? "" : bnchKeyOff.getCity());
					keyoffrow.put("keyoffEmail", bnchKeyOff.getEmail() == null ? "" : bnchKeyOff.getEmail());
					keyoffrow.put("keyoffphnNumberCtryCode", bnchKeyOff.getCtryPhCode());
					if (bnchKeyOff.getPhoneNumber() != null && !bnchKeyOff.getPhoneNumber().equals("")) {
						int m = bnchKeyOff.getPhoneNumber().indexOf("-");
						keyoffrow.put("keyoffphnNumber",
								bnchKeyOff.getPhoneNumber().substring(m + 1, bnchKeyOff.getPhoneNumber().length()));
					} else {
						keyoffrow.put("keyoffphnNumber", "");
					}
					keyoffrow.put("keyoffpersphnNumberCtryCode", bnchKeyOff.getPersonalPhoneCountryCode());
					if (bnchKeyOff.getPersonalPhoneNumber() != null
							&& !bnchKeyOff.getPersonalPhoneNumber().equals("")) {
						int m = bnchKeyOff.getPersonalPhoneNumber().indexOf("-");
						keyoffrow.put("keyoffpersphnNumber", bnchKeyOff.getPersonalPhoneNumber().substring(m + 1,
								bnchKeyOff.getPersonalPhoneNumber().length()));
					} else {
						keyoffrow.put("keyoffpersphnNumber", "");
					}
					keyoffrow.put("keyoffidproof",
							bnchKeyOff.getUploadedId() == null ? "" : bnchKeyOff.getUploadedId());
					branchkeyoffan.add(keyoffrow);
				}
				// branch statutory details
				ArrayNode bnchdynmstatan = results.putArray("branchdynmStatData");
				List<StatutoryDetails> dynmStatutory = branchDet.getBranchStatutoryDetails();
				if (dynmStatutory != null && dynmStatutory.size() > 0) {
					for (StatutoryDetails dynmstatdtls : dynmStatutory) {
						ObjectNode rows = Json.newObject();
						rows.put("dynmStatHidnId", dynmstatdtls.getId());
						rows.put("dynmStatDetails",
								dynmstatdtls.getStatutoryDetails() == null ? "" : dynmstatdtls.getStatutoryDetails());
						rows.put("dynmStatRegNo", dynmstatdtls.getRegistrationNumber() == null ? ""
								: dynmstatdtls.getRegistrationNumber());
						if (dynmstatdtls.getIsStatutoryAvailableForInvoice() != null) {
							rows.put("isStatAvailForInvoice", dynmstatdtls.getIsStatutoryAvailableForInvoice());
						}
						rows.put("dynmStatRegDocUrl",
								dynmstatdtls.getRegistrationDoc() == null ? "" : dynmstatdtls.getRegistrationDoc());
						if (dynmstatdtls.getValidFrom() != null && !dynmstatdtls.getValidFrom().equals("")) {
							rows.put("dynmStatRegValidFrom", idosdf.format(dynmstatdtls.getValidFrom()));
						} else {
							rows.put("dynmStatRegValidFrom", "");
						}
						if (dynmstatdtls.getValidTo() != null && !dynmstatdtls.getValidTo().equals("")) {
							rows.put("dynmStatRegValidTo", idosdf.format(dynmstatdtls.getValidTo()));
						} else {
							rows.put("dynmStatRegValidTo", "");
						}
						rows.put("alertForActions",
								dynmstatdtls.getAlertForAction() == null ? "" : dynmstatdtls.getAlertForAction());
						rows.put("alertForInformations", dynmstatdtls.getAlertForInformation() == null ? ""
								: dynmstatdtls.getAlertForInformation());
						rows.put("nameAddressOfConsultant", dynmstatdtls.getNameAddressOfConsultant() == null ? ""
								: dynmstatdtls.getNameAddressOfConsultant());
						rows.put("alertRemarks", dynmstatdtls.getRemarks() == null ? "" : dynmstatdtls.getRemarks());
						bnchdynmstatan.add(rows);
					}
				}
				// branch operational remainders
				ArrayNode bnchopereman = results.putArray("branchOperRemaindersData");
				List<OrganizationOperationalRemainders> branchOperRem = branchDet.getBranchOperationAlerts();
				if (branchOperRem != null && branchOperRem.size() > 0) {
					for (OrganizationOperationalRemainders bnchOperRem : branchOperRem) {
						ObjectNode operemrows = Json.newObject();
						operemrows.put("operationRemHidIds", bnchOperRem.getId());
						operemrows.put("requirements",
								bnchOperRem.getRequiements() == null ? "" : bnchOperRem.getRequiements());
						if (bnchOperRem.getDueOn() != null && !bnchOperRem.getDueOn().equals("")) {
							operemrows.put("dueOn", idosdf.format(bnchOperRem.getDueOn()));
						} else {
							operemrows.put("dueOn", "");
						}
						if (bnchOperRem.getValidTo() != null && !bnchOperRem.getValidTo().equals("")) {
							operemrows.put("operRemindersValidTo", idosdf.format(bnchOperRem.getValidTo()));
						} else {
							operemrows.put("operRemindersValidTo", "");
						}
						operemrows.put("recurrence",
								bnchOperRem.getRecurrences() == null ? "" : bnchOperRem.getRecurrences().toString());
						operemrows.put("alertforaction",
								bnchOperRem.getAlertForAction() == null ? "" : bnchOperRem.getAlertForAction());
						operemrows.put("alertforinformation", bnchOperRem.getAlertForInformation() == null ? ""
								: bnchOperRem.getAlertForInformation());
						operemrows.put("remarks", bnchOperRem.getRemarks() == null ? "" : bnchOperRem.getRemarks());
						bnchopereman.add(operemrows);
					}
				}

				// branch safe deposit box
				ArrayNode bnchsafedepboxan = results.putArray("branchSafeDepositBoxData");
				List<BranchDepositBoxKey> branchSafeDepBox = branchDet.getBranchDepositKeys();
				if (branchSafeDepBox != null && branchSafeDepBox.size() > 0) {
					for (BranchDepositBoxKey bnchSfeDepBox : branchSafeDepBox) {
						if (bnchSfeDepBox.getPresentStatus() == 0) {
							continue;
						}
						ObjectNode safedepboxrows = Json.newObject();
						safedepboxrows.put("safeDepositBoxHidIds", bnchSfeDepBox.getId());
						safedepboxrows.put("keyCustodianName", bnchSfeDepBox.getName());
						if (bnchSfeDepBox.getOpeningBalance() != null) {
							safedepboxrows.put("keyCustodianOpeningBalance",
									IdosConstants.decimalFormat.format(bnchSfeDepBox.getOpeningBalance()));
						} else {
							safedepboxrows.put("keyCustodianOpeningBalance", "");
						}
						safedepboxrows.put("keyCustodianPhNoCtryCode", bnchSfeDepBox.getCountryPhnCode());
						if (bnchSfeDepBox.getPhoneNumber() != null && !bnchSfeDepBox.getPhoneNumber().equals("")) {
							int m = bnchSfeDepBox.getPhoneNumber().indexOf("-");
							safedepboxrows.put("keyCustphnNumber", bnchSfeDepBox.getPhoneNumber().substring(m + 1,
									bnchSfeDepBox.getPhoneNumber().length()));
						} else {
							safedepboxrows.put("keyCustphnNumber", "");
						}
						safedepboxrows.put("keyCustodianemail",
								bnchSfeDepBox.getEmail() == null ? "" : bnchSfeDepBox.getEmail());
						if (bnchSfeDepBox.getCashierName() != null || bnchSfeDepBox.getCashierEmail() != null) {
							if (bnchSfeDepBox.getCashierName() != null) {
								safedepboxrows.put("safeDepBoxCashier", bnchSfeDepBox.getCashierName());
							} else {
								safedepboxrows.put("safeDepBoxCashier", "");
							}
							safedepboxrows.put("cashierPhNoCtryCode", bnchSfeDepBox.getCashierPhnNoCountryCode());
							if (bnchSfeDepBox.getCashierPhnNo() != null && !bnchSfeDepBox.getCashierPhnNo().equals("")
									&& !bnchSfeDepBox.getCashierPhnNo().equals("-")
									&& bnchSfeDepBox.getPhoneNumber() != null) {
								int m = bnchSfeDepBox.getCashierPhnNo().indexOf("-");
								safedepboxrows.put("cashierphnnumber", bnchSfeDepBox.getCashierPhnNo().substring(m + 1,
										bnchSfeDepBox.getCashierPhnNo().length()));
							} else {
								safedepboxrows.put("cashierphnnumber", "");
							}
							if (bnchSfeDepBox.getCashierEmail() != null) {
								safedepboxrows.put("cashieremail", bnchSfeDepBox.getCashierEmail());
							} else {
								safedepboxrows.put("cashieremail", "");
							}
							if (bnchSfeDepBox.getCashierKnowledgeLibrary() != null) {
								safedepboxrows.put("cashierkl", bnchSfeDepBox.getCashierKnowledgeLibrary());
							} else {
								safedepboxrows.put("cashierkl", "");
							}
							safedepboxrows.put("pettyCashTnApprovalRequired",
									bnchSfeDepBox.getPettyCashTxnApprovalRequired());
							if (bnchSfeDepBox.getPettyCashTxnApprovalRequired() == 1) {
								if (bnchSfeDepBox.getApprovalAmountLimit() != null) {
									safedepboxrows.put("pettyCashTnApprovalAmountLimit",
											IdosConstants.decimalFormat.format(bnchSfeDepBox.getApprovalAmountLimit()));
								} else {
									safedepboxrows.put("pettyCashTnApprovalAmountLimit", "");
								}
							}
						}
						if (bnchSfeDepBox.getPettyCashOpeningBalance() != null) {
							safedepboxrows.put("pettyCashOpeningBalance",
									IdosConstants.decimalFormat.format(bnchSfeDepBox.getPettyCashOpeningBalance()));
						} else {
							safedepboxrows.put("pettyCashOpeningBalance", "");
						}
						bnchsafedepboxan.add(safedepboxrows);
					}
				}
				// branch insurence
				ArrayNode bnchinsurencean = results.putArray("branchInsurenceData");
				List<BranchInsurance> branchInsuranceList = BranchInsurance.getBranchInsuranceList(entityManager,
						user.getOrganization().getId(), branchDet.getId());
				if (branchInsuranceList.size() > 0) {
					for (BranchInsurance bnchIns : branchInsuranceList) {
						if (bnchIns.getPresentStatus() == 0) {
							continue;
						}
						ObjectNode bnchInsrows = Json.newObject();
						bnchInsrows.put("bnchInsId", bnchIns.getId());
						bnchInsrows.put("bnchInsPolType",
								bnchIns.getPolicyType() == null ? "" : bnchIns.getPolicyType());
						bnchInsrows.put("policyNumber",
								bnchIns.getPolicyNumber() == null ? "" : bnchIns.getPolicyNumber());
						bnchInsrows.put("insurenceComp",
								bnchIns.getInsurenceCompany() == null ? "" : bnchIns.getInsurenceCompany());
						bnchInsrows.put("insurenceDoc",
								bnchIns.getInsurancePolicyDocUrl() == null ? "" : bnchIns.getInsurancePolicyDocUrl());
						if (bnchIns.getPolicyValidFrom() != null && !bnchIns.getPolicyValidFrom().equals("")) {
							bnchInsrows.put("policyValidityFrom", idosdf.format(bnchIns.getPolicyValidFrom()));
						} else {
							bnchInsrows.put("policyValidityFrom", "");
						}
						if (bnchIns.getPolicyValidTo() != null && !bnchIns.getPolicyValidTo().equals("")) {
							bnchInsrows.put("policyValidityTo", idosdf.format(bnchIns.getPolicyValidTo()));
						} else {
							bnchInsrows.put("policyValidityTo", "");
						}
						bnchInsrows.put("annualPremium",
								bnchIns.getAnnualPremium() == null ? "" : bnchIns.getAnnualPremium().toString());
						bnchInsrows.put("alertForActions",
								bnchIns.getAlertOfAction() == null ? "" : bnchIns.getAlertOfAction());
						bnchInsrows.put("alertForInformations",
								bnchIns.getAlertOfInformation() == null ? "" : bnchIns.getAlertOfInformation());
						bnchInsrows.put("remarks", bnchIns.getRemarks() == null ? "" : bnchIns.getRemarks());
						bnchinsurencean.add(bnchInsrows);
					}
				}
				// branch bank accounts
				//ArrayNode branchBankAccountan = results.putArray("branchBankActData");
				List<BranchBankAccounts> branchBankAccounts = branchDet.getBranchBankAccounts();
				if (branchBankAccounts.size() > 0) {
					for (BranchBankAccounts bnchBnkAct : branchBankAccounts) {
						if (bnchBnkAct.getPresentStatus() == 0) {
							continue;
						}
						ObjectNode bnchbankActrows = Json.newObject();
						bnchbankActrows.put("branchBankAccounId", bnchBnkAct.getId());
						bnchbankActrows.put("branchBankAccountBankName",
								bnchBnkAct.getBankName() == null ? "" : bnchBnkAct.getBankName());
						bnchbankActrows.put("branchBankAccountType",
								bnchBnkAct.getAccountType() == null ? "" : bnchBnkAct.getAccountType().toString());
						bnchbankActrows.put("branchBankAccountNumber",
								bnchBnkAct.getAccountNumber() == null ? "" : bnchBnkAct.getAccountNumber());
						bnchbankActrows.put("branchBankAccountOpeningBalance", IdosConstants.decimalFormat
								.format(bnchBnkAct.getOpeningBalance() == null ? 0.0 : bnchBnkAct.getOpeningBalance()));
						bnchbankActrows.put("branchBankAccounttAuthSignName",
								bnchBnkAct.getAuthorizedSignatoryName() == null ? ""
										: bnchBnkAct.getAuthorizedSignatoryName());
						bnchbankActrows.put("branchBankAccounttAuthSignEmail",
								bnchBnkAct.getAuthorizedSignatoryEmail() == null ? ""
										: bnchBnkAct.getAuthorizedSignatoryEmail());
						bnchbankActrows.put("branchBankAccounttAddress",
								bnchBnkAct.getBankAddress() == null ? "" : bnchBnkAct.getBankAddress());
						bnchbankActrows.put("branchBankAccounttPhnNoCtryCode",
								bnchBnkAct.getBankNumberPhnCtryCode() == null ? ""
										: bnchBnkAct.getBankNumberPhnCtryCode());
						if (bnchBnkAct.getPhoneNumber() != "" && bnchBnkAct.getPhoneNumber() != null) {
							int k = bnchBnkAct.getPhoneNumber().indexOf("-");
							bnchbankActrows.put("branchBankAccounttPhnNo",
									bnchBnkAct.getPhoneNumber().substring(k + 1, bnchBnkAct.getPhoneNumber().length()));
						} else {
							bnchbankActrows.put("branchBankAccounttPhnNo", bnchBnkAct.getPhoneNumber());
						}
						bnchbankActrows.put("branchBankAccountSwiftCode",
								bnchBnkAct.getSwiftCode() == null ? "" : bnchBnkAct.getSwiftCode());
						bnchbankActrows.put("branchBankAccountRoutingNumber",
								bnchBnkAct.getRoutingNumber() == null ? "" : bnchBnkAct.getRoutingNumber());
						bnchbankActrows.put("branchBankAccountCheckbookCustody",
								bnchBnkAct.getCheckBookCustodtName() == null ? ""
										: bnchBnkAct.getCheckBookCustodtName());
						bnchbankActrows.put("branchBankAccountCheckbookCustodyEmail",
								bnchBnkAct.getCheckBookCustodyEmail() == null ? ""
										: bnchBnkAct.getCheckBookCustodyEmail());
						branchBankAccountan.add(bnchbankActrows);
					}
				}
				// BRANCH TAX
				// ArrayNode branchTaxan = results.putArray("branchTaxData");
				List<BranchTaxes> branchTaxes = branchDet.getBranchTaxes();
				if (branchTaxes.size() > 0) {
					for (BranchTaxes bnchTaxes : branchTaxes) {
						ObjectNode bnchtaxrows = Json.newObject();
						bnchtaxrows.put("branchTaxHidIds", bnchTaxes.getId());
						bnchtaxrows.put("bnchTaxName", bnchTaxes.getTaxName() == null ? "" : bnchTaxes.getTaxName());
						bnchtaxrows.put("bnchTaxRates",
								bnchTaxes.getTaxRate() == null ? "" : bnchTaxes.getTaxRate().toString());
						bnchtaxrows.put("bnchTaxOpeningBal",
								bnchTaxes.getOpeningBalance() == null ? "" : bnchTaxes.getOpeningBalance().toString());
						bnchtaxrows.put("branchTaxType", bnchTaxes.getTaxType());
						branchTaxan.add(bnchtaxrows);
					}
				}
				log.log(Level.FINE, "Branch tax data: " + branchTaxan);

				Role branchAdminRole = Role.findById(9L);
				Map<String, Object> criterias = new HashMap<String, Object>();
				criterias.clear();
				criterias.put("branch.id", branchDet.getId());
				criterias.put("role.id", branchAdminRole.getId());
				criterias.put("presentStatus", 1);
				UsersRoles usrRoles = genericDAO.getByCriteria(UsersRoles.class, criterias, entityManager);
				if (usrRoles != null && null != usrRoles.getUser() && null != usrRoles.getUser().getEmail()) {
					results.put("branchAdmin", usrRoles.getUser().getEmail());
				} else {
					results.put("branchAdmin", "");
				}
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, results);//
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, results);
		}
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result showProjectDetails(Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode results = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			ArrayNode projectdetailan = results.putArray("projectdetailsData");
			String projectEntityId = json.findValue("entityPrimaryId").asText();
			Project projectDet = Project.findById(IdosUtil.convertStringToLong(projectEntityId));
			if (projectDet != null) {
				ObjectNode row = Json.newObject();
				row.put("id", projectDet.getId());
				row.put("projectName", projectDet.getName());
				row.put("projectNumber", projectDet.getNumber());
				if (projectDet.getCountry() != null) {
					row.put("country", projectDet.getCountry());
				} else {
					row.put("country", "");
				}
				row.put("projectLocation", projectDet.getLocation());
				row.put("projectDirectorName", projectDet.getProjectDirectorName());
				row.put("pjctDirPhoneNumberCtryCd", projectDet.getDirPhnNumCtryCode());
				if (projectDet.getDirectorPhoneNumber() != null && !projectDet.getDirectorPhoneNumber().equals("")) {
					int n = projectDet.getDirectorPhoneNumber().indexOf("-");
					row.put("projectDirectorNumber",
							projectDet.getDirectorPhoneNumber() == null ? ""
									: projectDet.getDirectorPhoneNumber().substring(n + 1,
											projectDet.getDirectorPhoneNumber().length()));
				} else {
					row.put("projectDirectorNumber", "");
				}
				row.put("projectManagerName", projectDet.getProjectManagerName());
				row.put("pjctManPhoneNumberCtryCd", projectDet.getDirPhnNumCtryCode());
				if (projectDet.getManagerPhoneNumber() != null && !projectDet.getManagerPhoneNumber().equals("")) {
					int n = projectDet.getManagerPhoneNumber().indexOf("-");
					row.put("projectManagerNumber",
							projectDet.getManagerPhoneNumber() == null ? ""
									: projectDet.getManagerPhoneNumber().substring(n + 1,
											projectDet.getManagerPhoneNumber().length()));
				} else {
					row.put("projectManagerNumber", "");
				}
				if (projectDet.getStartDate() != null) {
					row.put("projectStartDate", idosdf.format(projectDet.getStartDate()));
				} else {
					row.put("projectStartDate", "");
				}
				if (projectDet.getEndDate() != null) {
					row.put("projectEndDate", idosdf.format(projectDet.getEndDate()));
				} else {
					row.put("projectEndDate", "");
				}
				List<ProjectBranches> projectBranch = projectDet.getProjectBranch();
				if (projectBranch != null && projectBranch.size() > 0) {
					String branches = "";
					for (ProjectBranches bnches : projectBranch) {
						Branch branchById = bnches.getProjectBranch();
						branches += branchById.getId() + ",";
					}
					row.put("projectBranches", branches.substring(0, branches.length() - 1));
				} else {
					row.put("projectBranches", "");
				}
				String allowedRecruitmentService = "0";
				if (projectDet.getAllowAccessToRecruitmentServices() != null) {
					if (projectDet.getAllowAccessToRecruitmentServices() == 0) {
						allowedRecruitmentService = "0";
					}
					if (projectDet.getAllowAccessToRecruitmentServices() == 1) {
						allowedRecruitmentService = "1";
					}
				}
				row.put("allowedRecruitmentServices", allowedRecruitmentService);
				projectdetailan.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "ShowProjectDetails Email", "ShowProjectDetails Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> End " + results);
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result createProject(Http.Request request) {

		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		Users usr = null;
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start " + json);
			String projId = json.findPath("projId") != null ? json.findPath("projId").asText() : null;
			String projName = json.findPath("projName").asText();
			String projNumber = json.findPath("projNumber").asText();
			String projStartDate = json.findPath("projStartDate") != null ? json.findPath("projStartDate").asText()
					: null;
			String projEndDate = json.findPath("projEndDate") != null ? json.findPath("projEndDate").asText() : null;
			String projLocation = json.findPath("projLocation").asText();
			String projBranch = json.findPath("projBranch").asText();
			String projCountry = json.findPath("projCountry").asText();
			String projDirName = json.findPath("projDirName").asText();
			String projDirNumber = json.findPath("projDirNumber").asText();
			String projDirCtryCodeText = json.findPath("projDirCtryCodeText").asText();
			String projMangName = json.findPath("projMangName").asText();
			String projManCtryCodeText = json.findPath("projManCtryCodeText").asText();
			String projMangNumber = json.findPath("projMangNumber").asText();
			String usermail = json.findPath("usermail").asText();
			String projectPositionLisingAllowed = json.findPath("projectPositionLisingAllowed") != null
					? json.findPath("projectPositionLisingAllowed").asText()
					: null;
			session.adding("email", usermail);
			usr = getUserInfo(request);
			Organization org = usr.getOrganization();
			String projectBranch[] = null;
			if (projBranch != null && !projBranch.equals("") && projBranch.indexOf(',') != -1) {
				projectBranch = projBranch.split(",");
			} else if (projBranch != null && !projBranch.equals("")) {
				projectBranch = new String[] { projBranch };
			}
			Branch projectBnch;
			Project newProj;
			Project checkDupProj = Project.checkDiplicateProject(entityManager, org.getId(), projName, projNumber);
			if (checkDupProj != null) {
				if (projId == null || projId == "") {
					result.put("ispresent", "true");
					result.put("dataid", checkDupProj.getId());
					if (transaction != null && transaction.isActive()) {
						transaction.rollback();
					}
					return Results.ok(result);
				} else {
					if (IdosUtil.convertStringToLong(projId) != checkDupProj.getId()) {
						result.put("ispresent", "true");
						result.put("dataid", checkDupProj.getId());
						if (transaction != null && transaction.isActive()) {
							transaction.rollback();
						}
						return Results.ok(result);
					}
				}
			}
			if (projId == null || projId == "") {
				newProj = new Project();
			} else {
				newProj = Project.findById(IdosUtil.convertStringToLong(projId));
			}
			Date projectStartDate = null, projectEndDate = null;
			if (projStartDate != null && projStartDate != "") {
				projectStartDate = mysqldf.parse(mysqldf.format(idosdf.parse(projStartDate)));
			}
			if (projEndDate != null && projEndDate != "") {
				projectEndDate = mysqldf.parse(mysqldf.format(idosdf.parse(projEndDate)));
			}
			newProj.setStartDate(projectStartDate);
			newProj.setEndDate(projectEndDate);
			newProj.setName(projName);
			newProj.setNumber(projNumber);
			if (!projCountry.equals("") && projCountry != null) {
				newProj.setCountry(IdosUtil.convertStringToInt(projCountry));
			}
			newProj.setLocation(projLocation);
			newProj.setProjectDirectorName(projDirName);
			newProj.setDirPhnNumCtryCode(projDirCtryCodeText);
			newProj.setDirectorPhoneNumber(projDirNumber);
			newProj.setProjectManagerName(projMangName);
			newProj.setManagerPhnNumCtryCode(projManCtryCodeText);
			newProj.setManagerPhoneNumber(projMangNumber);
			newProj.setOrganization(org);
			if (projectPositionLisingAllowed != null && !projectPositionLisingAllowed.equals("")) {
				newProj.setAllowAccessToRecruitmentServices(IdosUtil.convertStringToInt(projectPositionLisingAllowed));
			}
			projcrud.save(usr, newProj, entityManager);

			if (ConfigParams.getInstance().isDeploymentSingleUser(usr)) {
				// For Single User Deployment Only
				if (newProj.getModifiedAt() == null) {
					singleUserService.updateOnProjectCreation(usr, newProj, entityManager);
				}
			}

			auditDAO.saveAuditLogs("save/updated project", usr, newProj.getId(), Project.class, ipAddress,
					json.toString(), entityManager);
			List<ProjectBranches> oldProjectBranches = newProj.getProjectBranch();
			List<ProjectBranches> newProjectBranches = new ArrayList<ProjectBranches>();
			if (projectBranch != null && !projectBranch.equals("")) {
				for (int i = 0; i < projectBranch.length; i++) {
					ProjectBranches newProjBranch = new ProjectBranches();
					projectBnch = Branch.findById(IdosUtil.convertStringToLong(projectBranch[i]));
					newProjBranch.setProjectBranch(projectBnch);
					newProjBranch.setProject(newProj);
					newProjBranch.setProjectOrganization(org);
					newProjBranch.setBranchOrganization(projectBnch.getOrganization());
					newProjectBranches.add(newProjBranch);
				}
			}
			List<List<ProjectBranches>> businessEntityTransactionList = ListUtility
					.getProjectBranchesTransactionList(oldProjectBranches, newProjectBranches);
			for (int i = 0; i < businessEntityTransactionList.size(); i++) {
				if (i == 0) {
					List<ProjectBranches> oldPjctBnch = businessEntityTransactionList.get(i);
					if (oldPjctBnch != null) {
						for (ProjectBranches Projectbnch : oldPjctBnch) {
							entityManager.remove(Projectbnch);
							auditDAO.saveAuditLogs("removed old project branch", usr, Projectbnch.getId(),
									ProjectBranches.class, ipAddress, json.toString(), entityManager);
						}
					}
				}
				if (i == 1) {
					List<ProjectBranches> newPjctBnch = businessEntityTransactionList.get(i);
					if (newPjctBnch != null) {
						for (ProjectBranches newPjctbnch : newPjctBnch) {
							genericDAO.saveOrUpdate(newPjctbnch, usr, entityManager);
							auditDAO.saveAuditLogs("added new project branch", usr, newPjctbnch.getId(),
									ProjectBranches.class, ipAddress, json.toString(), entityManager);
						}
					}
				}
			}
			// project position logic
			ProjectLabourPosition labPos = null;
			ProjectLabourPositionQualification labPosQual = null;
			String projectPosHiddenIds = json.findPath("projectPosHidIds").asText();
			if (projectPosHiddenIds.equals("")) {
				projectPosHiddenIds = ",";
			}
			String projectPosHidIds[] = projectPosHiddenIds.substring(0, projectPosHiddenIds.length()).split(",");
			String projectPosName[] = json.findPath("projectPosName").asText()
					.substring(0, json.findPath("projectPosName").asText().length()).split(",");
			String pjctpositionvalidity[] = json.findPath("pjctpositionvalidity").asText()
					.substring(0, json.findPath("pjctpositionvalidity").asText().length()).split("@");
			String pjctpositionvalidityto[] = json.findPath("pjctpositionvalidityto").asText()
					.substring(0, json.findPath("pjctpositionvalidityto").asText().length()).split("@");
			String projectPosBranch[] = json.findPath("projectPosBranch").asText()
					.substring(0, json.findPath("projectPosBranch").asText().length()).split(",");
			String projectposlocation[] = json.findPath("projectposlocation").asText()
					.substring(0, json.findPath("projectposlocation").asText().length()).split(",");
			String projPosQual[] = json.findPath("projPosQual").asText()
					.substring(0, json.findPath("projPosQual").asText().length()).split("@");
			String posqualificationDegree[] = json.findPath("posqualificationDegree").asText()
					.substring(0, json.findPath("posqualificationDegree").asText().length()).split("@");
			String posrequiredExp[] = json.findPath("posrequiredExp").asText()
					.substring(0, json.findPath("posrequiredExp").asText().length()).split(",");
			String posreqlanguages[] = json.findPath("posreqlanguages").asText()
					.substring(0, json.findPath("posreqlanguages").asText().length()).split(",");
			String poslangproficiencyspeaking[] = json.findPath("poslangproficiencyspeaking").asText()
					.substring(0, json.findPath("poslangproficiencyspeaking").asText().length()).split(",");
			String poslangproficiencyreading[] = json.findPath("poslangproficiencyreading").asText()
					.substring(0, json.findPath("poslangproficiencyreading").asText().length()).split(",");
			String poslangproficiencywriting[] = json.findPath("poslangproficiencywriting").asText()
					.substring(0, json.findPath("poslangproficiencywriting").asText().length()).split(",");
			String posjobDescription[] = json.findPath("posjobDescription").asText()
					.substring(0, json.findPath("posjobDescription").asText().length()).split(",");
			String posRequiresApproval[] = json.findPath("posRequiresApproval").asText()
					.substring(0, json.findPath("posRequiresApproval").asText().length()).split(",");
			String posplaceOfAdvertisement[] = json.findPath("posplaceOfAdvertisement").asText()
					.substring(0, json.findPath("posplaceOfAdvertisement").asText().length()).split(",");
			String poshiringBudget[] = json.findPath("poshiringBudget").asText()
					.substring(0, json.findPath("poshiringBudget").asText().length()).split(",");
			String posempAggreementDoc[] = json.findPath("posempAggreementDoc").asText()
					.substring(0, json.findPath("posempAggreementDoc").asText().length()).split(",");
			for (int i = 0; i < projectPosHidIds.length; ++i) {
				if (!projectPosHidIds[i].equals("") && projectPosHidIds[i] != null) {
					labPos = ProjectLabourPosition.findById(IdosUtil.convertStringToLong(projectPosHidIds[i]));
					if (projectPosName.length > i) {
						labPos.setPositionName(projectPosName[i].trim());
					}
					if (pjctpositionvalidity.length > i) {
						if (pjctpositionvalidity[i] != null && !pjctpositionvalidity[i].equals("")) {
							labPos.setPositionValidity(
									mysqldf.parse(mysqldf.format(idosdf.parse(pjctpositionvalidity[i]))));
						}
					}
					if (pjctpositionvalidityto.length > i) {
						if (pjctpositionvalidityto[i] != null && !pjctpositionvalidityto[i].equals("")) {
							labPos.setPositionValidityTo(
									mysqldf.parse(mysqldf.format(idosdf.parse(pjctpositionvalidityto[i]))));
						}
					}
					if (projectPosBranch.length > i && projectposlocation.length > i) {
						String projectPositionBranch = "";
						projectPositionBranch = projectposlocation[i].trim();
						if (!projectPosBranch[i].trim().equals("")) {
							Branch bnch = Branch.findById(IdosUtil.convertStringToLong(projectPosBranch[i].trim()));
							projectPositionBranch = bnch.getName();
						}
						labPos.setLocation(projectPositionBranch);
					}
					if (posrequiredExp.length > i) {
						labPos.setExpRequired(posrequiredExp[i].trim());
					}
					if (posjobDescription.length > i) {
						labPos.setJobDescription(posjobDescription[i].trim());
					}
					if (posRequiresApproval.length > i) {
						if (!posRequiresApproval[i].trim().equals("")) {
							labPos.setRequiresApproval(IdosUtil.convertStringToInt(posRequiresApproval[i].trim()));
						}
					}
					if (posplaceOfAdvertisement.length > i) {
						labPos.setPlaceOfAdvertisement(posplaceOfAdvertisement[i].trim());
					}
					if (poshiringBudget.length > i) {
						if (poshiringBudget[i].trim() != null && !poshiringBudget[i].trim().equals("")) {
							labPos.setBudget(IdosUtil.convertStringToDouble(poshiringBudget[i].trim()));
						}
					}
					if (posempAggreementDoc.length > i) {
						labPos.setAgreementTemlateDoc(posempAggreementDoc[i]);
					}
					labPos.setProject(newProj);
					labPos.setOrganization(usr.getOrganization());
					genericDAO.saveOrUpdate(labPos, usr, entityManager);
					auditDAO.saveAuditLogs("updated project labour position", usr, labPos.getId(),
							ProjectLabourPosition.class, ipAddress, json.toString(), entityManager);
					// project position qualification logic
					if (projPosQual.length > i) {
						List<ProjectLabourPositionQualification> oldProjectLabourPositionQualification = labPos
								.getPjctLabourpositionQualification();
						List<ProjectLabourPositionQualification> newProjectLabourPositionQualification = new ArrayList<ProjectLabourPositionQualification>();
						if (projPosQual[i] != null && !projPosQual[i].trim().equals("")) {
							String projectLabPosQual[] = projPosQual[i].substring(0, projPosQual[i].length())
									.split(",");
							String projectLabPosDeg[] = posqualificationDegree[i]
									.substring(0, posqualificationDegree[i].length()).split(",");
							for (int j = 0; j < projectLabPosQual.length; j++) {
								ProjectLabourPositionQualification newProjLabPosQual = new ProjectLabourPositionQualification();
								if (projectLabPosQual.length > j) {
									newProjLabPosQual.setQualificationName(projectLabPosQual[j].trim());
								}
								if (projectLabPosDeg.length > j) {
									newProjLabPosQual.setQualificationDegree(projectLabPosDeg[j].trim());
								}
								newProjLabPosQual.setLabourPosition(labPos);
								newProjLabPosQual.setOrganization(usr.getOrganization());
								newProjectLabourPositionQualification.add(newProjLabPosQual);
							}
						}
						List<List<ProjectLabourPositionQualification>> projPosLabQualTransactionList = ListUtility
								.getProjPosQualificationTransactionList(oldProjectLabourPositionQualification,
										newProjectLabourPositionQualification);
						for (int j = 0; j < projPosLabQualTransactionList.size(); j++) {
							if (j == 0) {
								List<ProjectLabourPositionQualification> oldPjctLabPosQual = projPosLabQualTransactionList
										.get(j);
								if (oldPjctLabPosQual != null) {
									for (ProjectLabourPositionQualification ProjectLabPosQual : oldPjctLabPosQual) {
										entityManager.remove(ProjectLabPosQual);
										auditDAO.saveAuditLogs("removed old project labour position qualification", usr,
												ProjectLabPosQual.getId(), ProjectLabourPositionQualification.class,
												ipAddress, json.toString(), entityManager);
									}
								}
							}
							if (j == 1) {
								List<ProjectLabourPositionQualification> newPjctLabPosQual = projPosLabQualTransactionList
										.get(j);
								if (newPjctLabPosQual != null) {
									for (ProjectLabourPositionQualification NewPjctLabPosQual : newPjctLabPosQual) {
										genericDAO.saveOrUpdate(NewPjctLabPosQual, usr, entityManager);
										auditDAO.saveAuditLogs("added new project labour position qualification", usr,
												NewPjctLabPosQual.getId(), ProjectLabourPositionQualification.class,
												ipAddress, json.toString(), entityManager);
									}
								}
							}
						}
					}
				}
			}
			for (int i = projectPosHidIds.length; (i < projectPosName.length); ++i) {
				labPos = new ProjectLabourPosition();
				if (!projectPosName[i].trim().equals("") || !pjctpositionvalidity[i].trim().equals("")) {
					if (projectPosName.length > i) {
						labPos.setPositionName(projectPosName[i].trim());
					}
					if (pjctpositionvalidity.length > i) {
						if (pjctpositionvalidity[i].trim() != null && !pjctpositionvalidity[i].trim().equals("")) {
							labPos.setPositionValidity(
									mysqldf.parse(mysqldf.format(idosdf.parse(pjctpositionvalidity[i]))));
						}
					}
					if (pjctpositionvalidityto.length > i) {
						if (pjctpositionvalidityto[i] != null && !pjctpositionvalidityto[i].equals("")) {
							labPos.setPositionValidityTo(
									mysqldf.parse(mysqldf.format(idosdf.parse(pjctpositionvalidityto[i]))));
						}
					}
					if (projectPosBranch.length > i && projectposlocation.length > i) {
						String projectPositionBranch = "";
						projectPositionBranch = projectposlocation[i].trim();
						if (!projectPosBranch[i].trim().equals("")) {
							Branch bnch = Branch.findById(IdosUtil.convertStringToLong(projectPosBranch[i].trim()));
							projectPositionBranch = bnch.getName();
						}
						labPos.setLocation(projectPositionBranch);
					}
					if (posrequiredExp.length > i) {
						labPos.setExpRequired(posrequiredExp[i].trim());
					}
					for (int k = 0; k < posreqlanguages.length; k++) {
						ProjectLabourPositionLanguageProficiency pjctLabPosLangProf = new ProjectLabourPositionLanguageProficiency();
						pjctLabPosLangProf.setLabourPosition(labPos);
						String langProf = "";
						if (posreqlanguages.length > k) {
							langProf = posreqlanguages[k].trim() + ":";
						}
						if (poslangproficiencyspeaking.length > k) {
							langProf += "Speaking -" + poslangproficiencyspeaking[k] + ",";
						}
						if (poslangproficiencyreading.length > k) {
							langProf += "Reading -" + poslangproficiencyreading[k] + ",";
						}
						if (poslangproficiencywriting.length > k) {
							langProf += "Writing -" + poslangproficiencywriting[k];
						}
						pjctLabPosLangProf.setLanguaugeProficiency(langProf);
						genericDAO.saveOrUpdate(pjctLabPosLangProf, usr, entityManager);
					}
					if (posjobDescription.length > i) {
						labPos.setJobDescription(posjobDescription[i].trim());
					}
					if (posRequiresApproval.length > i) {
						if (!posRequiresApproval[i].trim().equals("")) {
							labPos.setRequiresApproval(IdosUtil.convertStringToInt(posRequiresApproval[i].trim()));
						}
					}
					if (posplaceOfAdvertisement.length > i) {
						labPos.setPlaceOfAdvertisement(posplaceOfAdvertisement[i].trim());
					}
					if (poshiringBudget.length > i) {
						if (poshiringBudget[i].trim() != null && !poshiringBudget[i].trim().equals("")) {
							labPos.setBudget(IdosUtil.convertStringToDouble(poshiringBudget[i].trim()));
						}
					}
					if (posempAggreementDoc.length > i) {
						labPos.setAgreementTemlateDoc(posempAggreementDoc[i]);
					}
					labPos.setProject(newProj);
					labPos.setOrganization(usr.getOrganization());
					genericDAO.saveOrUpdate(labPos, usr, entityManager);
					auditDAO.saveAuditLogs("Created project labour position", usr, labPos.getId(),
							ProjectLabourPosition.class, ipAddress, json.toString(), entityManager);
					if (projPosQual.length > i) {
						List<ProjectLabourPositionQualification> oldProjectLabourPositionQualification = labPos
								.getPjctLabourpositionQualification();
						List<ProjectLabourPositionQualification> newProjectLabourPositionQualification = new ArrayList<ProjectLabourPositionQualification>();
						if (projPosQual[i] != null && !projPosQual[i].trim().equals("")) {
							String projectLabPosQual[] = projPosQual[i].substring(0, projPosQual[i].length())
									.split(",");
							String projectLabPosDeg[] = posqualificationDegree[i]
									.substring(0, posqualificationDegree[i].length()).split(",");
							for (int j = 0; j < projectLabPosQual.length; j++) {
								ProjectLabourPositionQualification newProjLabPosQual = new ProjectLabourPositionQualification();
								if (projectLabPosQual.length > j) {
									newProjLabPosQual.setQualificationName(projectLabPosQual[j].trim());
								}
								if (projectLabPosDeg.length > j) {
									newProjLabPosQual.setQualificationDegree(projectLabPosDeg[j].trim());
								}
								newProjLabPosQual.setLabourPosition(labPos);
								newProjLabPosQual.setOrganization(usr.getOrganization());
								newProjectLabourPositionQualification.add(newProjLabPosQual);
							}
						}
						List<List<ProjectLabourPositionQualification>> projPosLabQualTransactionList = ListUtility
								.getProjPosQualificationTransactionList(oldProjectLabourPositionQualification,
										newProjectLabourPositionQualification);
						for (int j = 0; j < projPosLabQualTransactionList.size(); j++) {
							if (j == 0) {
								List<ProjectLabourPositionQualification> oldPjctLabPosQual = projPosLabQualTransactionList
										.get(j);
								if (oldPjctLabPosQual != null) {
									for (ProjectLabourPositionQualification ProjectLabPosQual : oldPjctLabPosQual) {
										entityManager.remove(ProjectLabPosQual);
										auditDAO.saveAuditLogs("removed old project labour position qualification", usr,
												ProjectLabPosQual.getId(), ProjectLabourPositionQualification.class,
												ipAddress, json.toString(), entityManager);
									}
								}
							}
							if (j == 1) {
								List<ProjectLabourPositionQualification> newPjctLabPosQual = projPosLabQualTransactionList
										.get(j);
								if (newPjctLabPosQual != null) {
									for (ProjectLabourPositionQualification NewPjctLabPosQual : newPjctLabPosQual) {
										genericDAO.saveOrUpdate(NewPjctLabPosQual, usr, entityManager);
										auditDAO.saveAuditLogs("added new project labour position qualification", usr,
												NewPjctLabPosQual.getId(), ProjectLabourPositionQualification.class,
												ipAddress, json.toString(), entityManager);
									}
								}
							}
						}
					}
				}
			}
			transaction.commit();
			result.put("projectId", newProj.getId());
			// Map<String, ActorRef> orgregistrered = new HashMap<String, ActorRef>();
			// Object[] keyArray =
			// ProjectTransactionActor.projectRegistered.keySet().toArray();
			// for (int i = 0; i < keyArray.length; i++) {
			// List<Users> users = Users.findByEmailActDeact(entityManager, (String)
			// keyArray[i]);
			// if (!users.isEmpty() && users.get(0).getOrganization().getId() ==
			// newProj.getOrganization().getId()) {
			// orgregistrered.put(keyArray[i].toString(),
			// ProjectTransactionActor.projectRegistered.get(keyArray[i]));
			// }
			// }
			String actionText = "";
			if (newProj.getPresentStatus() == 0) {
				actionText = "Activate";
			}
			if (newProj.getPresentStatus() == 1) {
				actionText = "Deactivate";
			}
			result.put("id", newProj.getId());
			result.put("name", newProj.getName());
			result.put("number", projNumber);
			result.put("startDate", projStartDate);
			result.put("endDate", projEndDate);
			result.put("location", projLocation);
			result.put("actionText", actionText);
			// ProjectTransactionActor.addProject(newProj.getId(), newProj.getName(),
			// projNumber, projStartDate,
			// projEndDate, projLocation, orgregistrered, actionText);
		} catch (Exception ex) {
			reportException(entityManager, transaction, usr, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, transaction, usr, th, result);
		}
		return Results.ok(result);
	}

	@Transactional
	public Result addRole(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Users user = getUserInfo(request);
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		try {
			transaction.begin();
			Role newrole = new Role();
			JsonNode json = request.body().asJson();
			ArrayNode rolean = result.putArray("roleData");
			String roleName = json.findValue("role").asText();
			newrole.setName(roleName.toUpperCase());
			rolecrud.save(user, newrole, entityManager);
			Role newRole = Role.findById(newrole.getId());
			if (newrole != null) {
				ObjectNode row = Json.newObject();
				row.put("id", newrole.getId());
				row.put("name", newrole.getName());
				rolean.add(row);
			}
			transaction.commit();
		} catch (Exception ex) {
			reportException(entityManager, transaction, null, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, transaction, null, th, result);
		}
		return Results.ok(result);
	}

	@Transactional
	public Result removeOrganization(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String orgId = json.findValue("orgId").asText();
			Organization org = Organization.findById(IdosUtil.convertStringToLong(orgId));
			orgcrud.delete(org, entityManager);
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "RemoveOrganization Email", "RemoveOrganization Configuration",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result getDatas(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
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
			userEmail = usrinfo.getEmail();
			orgaName = usrinfo.getOrganization().getName();
			Map<String, Object> criterias = new HashMap<String, Object>();
			ArrayNode organ = result.putArray("organizationData");
			ArrayNode branchlistan = result.putArray("branchListData");
			ArrayNode vendorlistan = result.putArray("vendorListData");
			ArrayNode projectlistan = result.putArray("projectListData");
			ArrayNode catan = result.putArray("categoryData");
			// ArrayNode partan = result.putArray("partData");
			ArrayNode iteman = result.putArray("itemData");
			ArrayNode userbnchan = result.putArray("userbranchData");
			ArrayNode userrolean = result.putArray("userroleData");
			ArrayNode bnkactTypean = result.putArray("bnkactTypean");
			ArrayNode languageListan = result.putArray("lanuageListData");
			// ArrayNode incomeItemsArray = result.putArray("incomeItemData");
			// ArrayNode expenseItemsArray = result.putArray("expenseItemData");
			result.put("gstCountryCode", usrinfo.getOrganization().getGstCountryCode() == null ? ""
					: usrinfo.getOrganization().getGstCountryCode());
			result.put("placeOfSupplyType", usrinfo.getOrganization().getPlaceOfSupplyType() == null ? 1
					: usrinfo.getOrganization().getPlaceOfSupplyType());
			criterias.put("organization.id", usrinfo.getOrganization().getId());
			List<Branch> branchList = genericDAO.findByCriteria(Branch.class, criterias, "id", false, entityManager);
			System.out.println("<<<<<branchList>>>>>" + branchList);
			criterias.clear();
			criterias.put("organization.id", usrinfo.getOrganization().getId());
			List<Project> projectList = genericDAO.findByCriteria(Project.class, criterias, "id", true, entityManager);
			System.out.println("<<<<<projectList>>>>>" + projectList);
			// criterias.clear();
			// criterias.put("organization.id", usrinfo.getOrganization().getId());

			// List<Particulars> categories=genericDAO.findByCriteria(Particulars.class,
			// criterias, "id", true, entityManager);
			// ArrayList inparamList = new ArrayList(1);
			// inparamList.add(usrinfo.getOrganization().getId());
			// List<Specifics> itemSpecifics = genericDAO.queryWithParams(SPECIFICS_HQL,
			// entityManager, inparamList);
			// List<Particulars> catList=particular.list(usrinfo.getOrganization());
			// List<Specifics> itemList=specifics.list(usrinfo.getOrganization());

			List<Specifics> incomeItemsList = coaService.getIncomesCoaChildNodes(entityManager, usrinfo);
			List<Specifics> expenseItemsList = coaService.getExpensesCoaChildNodes(entityManager, usrinfo);
			Organization orgn = usrinfo.getOrganization();
			List<Branch> userBnchList = orgn.getBranches();
			List<Role> userRoleList = Role.list(entityManager);

			BankAccountEnumType[] bankActTypes = BankAccountEnumType.class.getEnumConstants();
			if (orgn != null) {
				ObjectNode row = Json.newObject();
				row.put("id", orgn.getId());
				row.put("name", orgn.getName());
				row.put("corporateEmail", orgn.getCorporateMail());
				organ.add(row);
			}
			/*
			 * if(catList.size()>0){
			 * for(Particulars part:catList){
			 * ObjectNode row = Json.newObject();
			 * row.put("id", part.getId());
			 * row.put("name", part.getName());
			 * row.put("accountCode", part.getAccountCode());
			 * catan.add(row);
			 * partan.add(row);
			 * }
			 * }
			 * if(itemList.size()>0){
			 * for(Specifics specf:itemList){
			 * ObjectNode row = Json.newObject();
			 * row.put("id", specf.getId());
			 * row.put("name", specf.getName());
			 * row.put("topLevelAccountcode", specf.getParticularsId().getAccountCode());
			 * iteman.add(row);
			 * catan.add(row);
			 * }
			 * }
			 * 
			 * for(Specifics specf: incomeItemsList){
			 * ObjectNode row = Json.newObject();
			 * row.put("id", specf.getId());
			 * row.put("name", specf.getName());
			 * incomeItemsArray.add(row);
			 * catan.add(row);
			 * }
			 * for (Specifics specf : expenseItemsList) {
			 * ObjectNode row = Json.newObject();
			 * row.put("id", specf.getId());
			 * row.put("name", specf.getName());
			 * row.put("label", specf.getName());
			 * expenseItemsArray.add(row);
			 * catan.add(row);
			 * }
			 */

			if (userBnchList.size() > 0) {
				for (Branch bnch : userBnchList) {
					if (bnch.getPresentStatus() == 0) {
						continue;
					}
					ObjectNode row = Json.newObject();
					row.put("id", bnch.getId());
					row.put("name", bnch.getName());
					row.put("gstin", bnch.getGstin());
					row.put("isHeadQuarters", bnch.getIsHeadQuarter());
					userbnchan.add(row);
				}
			}
			if (userRoleList.size() > 0) {
				for (Role rl : userRoleList) {
					ObjectNode row = Json.newObject();
					row.put("id", rl.getId());
					row.put("name", rl.getName());
					userrolean.add(row);
				}
			}

			/*
			 * Locale[] locales = Locale.getAvailableLocales();
			 * Set<String> langSet=new HashSet<String>();
			 * for (Locale locale : locales) {
			 * langSet.add(locale.getDisplayLanguage());
			 * }
			 * Set<String> treeSet = new TreeSet<String>(langSet);
			 */
			Set<String> treeSet = LanguageUtil.getLanguages();
			Iterator<String> itr = treeSet.iterator();
			int langcount = 0;
			while (itr.hasNext()) {
				langcount++;
				ObjectNode row = Json.newObject();
				row.put("id", langcount);
				row.put("name", itr.next());
				languageListan.add(row);
			}

			for (BankAccountEnumType bankActType : bankActTypes) {
				int actType = bankActType.getId();
				ObjectNode row = Json.newObject();
				row.put("id", String.valueOf(actType));
				row.put("name", bankActType.getName());
				bnkactTypean.add(row);
			}
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
			criterias.clear();
			criterias.put("organization.id", usrinfo.getOrganization().getId());
			List<Vendor> vendorList = genericDAO.findByCriteria(Vendor.class, criterias, "id", true, entityManager);
			for (Vendor vendorTableList : vendorList) {
				ObjectNode row = Json.newObject();
				row.put("id", vendorTableList.getId());
				row.put("name", vendorTableList.getName());

				if (vendorTableList.getType() == 2) {
					row.put("type", "Customer");
				} else if (vendorTableList.getType() == 1) {
					row.put("type", "Vendor");
				}
				if (vendorTableList.getAddress() != null && !vendorTableList.getAddress().equals("")) {
					row.put("address", vendorTableList.getAddress());
				} else {
					row.put("address", "");
				}
				/*
				 * if(vendorTableList.getCustomerCode()!=null &&
				 * !vendorTableList.getCustomerCode().equals("")){
				 * row.put("code", vendorTableList.getCustomerCode());
				 * }
				 * else{
				 * row.put("code","");
				 * }
				 */
				row.put("location", vendorTableList.getLocation() == null ? "" : vendorTableList.getLocation());
				row.put("email", vendorTableList.getEmail() == null ? "" : vendorTableList.getEmail());
				row.put("phone", vendorTableList.getPhone() == null ? "" : vendorTableList.getPhone());
				row.put("grantAccess", vendorTableList.getGrantAccess());
				/*
				 * if(userRolesStr.indexOf("MASTER ADMIN") != -1 ||
				 * (userRolesStr.indexOf("APPROVER") != -1 && user.canActivateVendor())) {
				 * log.log(Level.INFO, "userrole="+userRolesStr);
				 * log.log(Level.INFO, "present status"+vendor.getPresentStatus());
				 * row.put("presentStatus", vendor.getPresentStatus());
				 * }else{
				 * log.log(Level.INFO, "else part");
				 * log.log(Level.INFO, "userrole="+userRolesStr);
				 * log.log(Level.INFO, "present status"+vendor.getPresentStatus());
				 * row.put("presentStatus", "-1");
				 * }
				 */
				Integer canActivateVendor;
				if (usrinfo.canActivateVendor() == true) {
					canActivateVendor = 1;
					result.put("canActivateVendor", canActivateVendor);
				}
				if (usrinfo.canActivateVendor() == false) {
					canActivateVendor = 0;
					result.put("canActivateVendor", canActivateVendor);
				}

				Integer presentStatus;
				if (vendorTableList.getPresentStatus() == 1) {
					presentStatus = 1;
					row.put("presentStatus", presentStatus);
				} else if (vendorTableList.getPresentStatus() == 0) {
					presentStatus = 0;
					row.put("presentStatus", presentStatus);
				} else {
					row.put("presentStatus", "");
				}
				List<BranchVendors> vendorBranches = vendorTableList.getVendorBranches();
				vendorlistan.add(row);
			}

			if (projectList.size() > 0) {
				for (Project projectTableList : projectList) {
					ObjectNode row = Json.newObject();
					row.put("id", projectTableList.getId());
					row.put("name", projectTableList.getName());
					row.put("number", projectTableList.getNumber());
					String startDate = "", endDate = "";
					if (projectTableList.getStartDate() != null) {
						if (projectTableList.getStartDate().toString()
								.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d")) {
							startDate = idosdf.format(mysqldf.parse(projectTableList.getStartDate().toString()));
						} else {
							startDate = idosdf
									.format(projectDateFormat.parse(projectTableList.getStartDate().toString()));
						}
					}
					if (projectTableList.getEndDate() != null) {
						if (projectTableList.getEndDate().toString()
								.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d")) {
							endDate = idosdf.format(mysqldf.parse(projectTableList.getEndDate().toString()));
						} else {
							endDate = idosdf.format(projectDateFormat.parse(projectTableList.getEndDate().toString()));
						}
					}
					row.put("startDate", startDate);
					row.put("endDate", endDate);
					row.put("location", projectTableList.getLocation());
					if (projectTableList.getPresentStatus() == 0) {
						row.put("actionText", "Activate");
					}
					if (projectTableList.getPresentStatus() == 1) {
						row.put("actionText", "Deactivate");
					}
					projectlistan.add(row);
				}
			}
		} catch (Exception ex) {
			reportException(entityManager, null, usrinfo, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, usrinfo, th, result);
		} finally {
			// EntityManagerProvider.close();
		}
		log.log(Level.FINE, ">>>> End");
		return Results.ok(result);
	}

	@Transactional
	public Result addBranch(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users usr = null;
		Http.Session session = request.session();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode branchan = result.putArray("branchData");
			String usermail = json.findPath("usermail").asText();
			session.adding("email", usermail);
			usr = getUserInfo(request);
			if (usr == null) {
				return unauthorized();
			}
			Organization org = usr.getOrganization();
			String bnchId = json.findPath("branchId") != null ? json.findPath("branchId").asText().trim() : null;
			Branch newBranch = null;
			Boolean newBranchFlag = false;
			String oldName = null;
			if (bnchId == null || bnchId.equals("")) {
				newBranch = new Branch();
				newBranchFlag = true;
				newBranch.setIsHeadQuarter(0);
				newBranch.setBudgetDate(Calendar.getInstance().getTime());
				StringBuilder sbr = new StringBuilder("");
				sbr.append("select obj from IdosChannelPartnerAlloteOrganization obj where obj.organizationName='"
						+ org.getName().equalsIgnoreCase(org.getName()) + "' and obj.email='"
						+ org.getCorporateMail().equalsIgnoreCase(org.getCorporateMail())
						+ "' and obj.presentStatus=1");
				List<IdosChannelPartnerAlloteOrganization> idosCpCommitmentList = genericDAO
						.executeSimpleQuery(sbr.toString(), entityManager);
				if (!idosCpCommitmentList.isEmpty() && idosCpCommitmentList.size() > 0) {
					for (IdosChannelPartnerAlloteOrganization idosCpCommitment : idosCpCommitmentList) {
						IdosChannelPartnerCustomerOrganizationBranch newCpOrgBnch = new IdosChannelPartnerCustomerOrganizationBranch();
						newCpOrgBnch.setCustomerOrganizatioNbRANCH(newBranch);
						newCpOrgBnch.setCustomerOrganization(org);
						newCpOrgBnch.setIdosChannelPartner(idosCpCommitment.getChannelPartner());
						genericDAO.saveOrUpdate(newCpOrgBnch, usr, entityManager);
						idosCpCommitment.setCommitmentStatus("ALLOTED");
						genericDAO.saveOrUpdate(idosCpCommitment, usr, entityManager);
						// send mail to channel partner whose prospect organization registered as idos
						// customer and now creating branch for the organization
						String location = "";
						if (newBranch.getLocation() != null) {
							location = newBranch.getLocation();
						}
						String cpBody = channelPartnerProspectConverted
								.render(idosCpCommitment.getChannelPartner().getChannelPartnerName(),
										idosCpCommitment.getChannelPartner().getChannelPartnerEmail(), org.getName(),
										org.getCorporateMail(), newBranch.getName(), location,
										ConfigParams.getInstance())
								.body();
						final String cpusername = ConfigFactory.load().getString("smtpchannelsales.user");
						String cpsubject = "Organization/Branch Registered Into Your Channel Partner Account With Idos";
						Session cpsession = channelSalesSession;
						mailTimer(cpBody, cpusername, cpsession,
								idosCpCommitment.getChannelPartner().getChannelPartnerEmail(), null, cpsubject);
					}
				}
			} else {
				newBranch = Branch.findById(IdosUtil.convertStringToLong(bnchId));
				oldName = newBranch.getName();
			}
			String bnchName = json.findPath("branchName").asText();
			String bnchOpenDate = json.findPath("branchOpenDate") != null ? json.findPath("branchOpenDate").asText()
					: null;
			Date bnchOpenDt = null;
			if (bnchOpenDate != null && bnchOpenDate != "") {
				bnchOpenDt = mysqldf.parse(mysqldf.format(idosdf.parse(bnchOpenDate)));
			}
			String branchCountry = json.findPath("branchCountry").asText();
			if (!branchCountry.equals("") && branchCountry != null) {
				Integer bnchCtry = IdosUtil.convertStringToInt(branchCountry);
				newBranch.setCountry(bnchCtry);
			}
			String bnchCurrency = json.findPath("branchCurrency").asText();
			String bnchLocation = json.findPath("branchLocation").asText();
			String bnchPhNoCtryCd = json.findPath("regphnoccode").asText();
			String bnchPhoneNumber = json.findPath("branchPhoneNumber").asText();
			String branchFacility = json.findPath("bnchFacility") != null ? json.findPath("bnchFacility").asText()
					: null;
			newBranch.setName(bnchName);
			newBranch.setLocation(bnchLocation);
			newBranch.setPhoneNumberCtryCode(bnchPhNoCtryCd);
			newBranch.setPhoneNumber(bnchPhoneNumber);
			newBranch.setBranchOpenDate(bnchOpenDt);
			newBranch.setCurrency(bnchCurrency);

			String branchAddress = json.findValue("branchAddress") != null ? json.findValue("branchAddress").asText()
					: null;
			String branchStateCode = json.findValue("branchStateCode") != null
					? json.findValue("branchStateCode").asText()
					: null;
			String branchGstin = json.findValue("branchGstin") != null ? json.findValue("branchGstin").asText() : "";
			if (usr.getOrganization().getGstCountryCode() != null
					&& usr.getOrganization().getGstCountryCode().equals("IN")) {
				branchGstin = IdosUtil.branchAndMultiGstinValidate(branchGstin, bnchId);
				if (usr.getOrganization().getGstCountryCode() != null && branchGstin != null
						&& (branchGstin.length() < 2 || branchGstin.length() > 15)) {
					throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
							IdosConstants.DATA_FORMAT_EXCEPTION, "GSTIN is wrong.");
				}
				newBranch.setGstin(branchGstin);
			} else {
				newBranch.setGstin("");
			}
			newBranch.setAddress(branchAddress);
			newBranch.setStateCode(branchStateCode);
			int bnchFacility = 0;
			String premiseType = null;
			String branchrentRevisionDueOn = json.findPath("branchrentRevisionDueOn") != null
					? json.findPath("branchrentRevisionDueOn").asText()
					: null;
			String rentRevisionDueOnRemarks = json.findPath("branchrentRevisionDueOnRemarks") != null
					? json.findPath("branchrentRevisionDueOnRemarks").asText()
					: null;
			Date aggreementValidFrom = null, aggreementValidTo = null;
			if (!branchFacility.equals("") && branchFacility != null) {
				bnchFacility = IdosUtil.convertStringToInt(json.findPath("bnchFacility").asText());
				newBranch.setBranchFacility(bnchFacility);
				if (bnchFacility == 1 || bnchFacility == 2) {
					if (bnchFacility == 1) {
						premiseType = "Leased";
					} else {
						premiseType = "Rented";
					}
					String bnchAggreement = json.findPath("branchAggreement") != null
							? json.findPath("branchAggreement").asText()
							: null;
					String branchAggreementValidFrom = json.findPath("branchAggreementValidFrom") != null
							? json.findPath("branchAggreementValidFrom").asText()
							: null;
					String branchAggreementValidTo = json.findPath("branchAggreementValidTo") != null
							? json.findPath("branchAggreementValidTo").asText()
							: null;
					String branchPremiseRentPayable = json.findPath("branchRentPayable").asText();
					String periodOfPayment = json.findPath("periodOfPayment").asText();
					if (!periodOfPayment.equals("") && periodOfPayment != null) {
						newBranch.setPeriodicityOfPayment(IdosUtil.convertStringToInt(periodOfPayment));
					}
					if (!branchPremiseRentPayable.equals("") && branchPremiseRentPayable != null) {
						Double rentPayable = IdosUtil.convertStringToDouble(branchPremiseRentPayable);
						newBranch.setRentPayable(rentPayable);
					}
					String landLordName = json.findPath("branchLandlordName").asText();
					String branchlandLordAddress = json.findPath("branchlandLordAddress").asText();
					String branchbankAccountName = json.findPath("branchbankAccountName").asText();
					String branchbankAccountNumber = json.findPath("branchbankAccountNumber").asText();
					String branchbankAccountBranch = json.findPath("branchbankAccountBranch").asText();
					String branchPremiseAlertForAction = json.findPath("branchPremiseAlertForAction") != null
							? json.findPath("branchPremiseAlertForAction").asText()
							: null;
					String branchPremiseAlertForInformation = json.findPath("branchPremiseAlertForInformation") != null
							? json.findPath("branchPremiseAlertForInformation").asText()
							: null;
					Date rentDueOn = null;
					if (!branchrentRevisionDueOn.equals("") && branchrentRevisionDueOn != null) {
						rentDueOn = mysqldf.parse(mysqldf.format(idosdf.parse(branchrentRevisionDueOn)));
					}
					if (branchAggreementValidFrom != null && !branchAggreementValidFrom.equals("")) {
						aggreementValidFrom = mysqldf.parse(mysqldf.format(idosdf.parse(branchAggreementValidFrom)));
					}
					if (branchAggreementValidTo != null && !branchAggreementValidTo.equals("")) {
						aggreementValidTo = mysqldf.parse(mysqldf.format(idosdf.parse(branchAggreementValidTo)));
					}
					if (branchPremiseAlertForAction != null) {
						newBranch.setAlertForAction(branchPremiseAlertForAction);
					}
					if (branchPremiseAlertForInformation != null) {
						newBranch.setAlertForInformation(branchPremiseAlertForInformation);
					}
					newBranch.setAggreement(bnchAggreement);
					newBranch.setAggreementValidFrom(aggreementValidFrom);
					newBranch.setAggreementValidTo(aggreementValidTo);
					newBranch.setLandlordName(landLordName);
					newBranch.setLandlordAddress(branchlandLordAddress);
					newBranch.setBankAccountName(branchbankAccountName);
					newBranch.setBankAccountNumber(branchbankAccountNumber);
					newBranch.setBankAccountBranch(branchbankAccountBranch);
					newBranch.setRentRevisedDueOn(rentDueOn);
					newBranch.setRemarks(rentRevisionDueOnRemarks);
				}
			}
			String isDigitalSignEnabled = json.findPath("isDigitalSignatureEnabled") != null
					? json.findPath("isDigitalSignatureEnabled").asText()
					: null;

			newBranch.setOrganization(org);
			bnchcrud.save(usr, newBranch, entityManager);
			DigitalSignatureController.saveDigitalSignatureDetails(org, newBranch, usr, json, entityManager);
			if (newBranch.getId() != null && newBranchFlag) {
				INTER_BRANCH_TRANSFER_SERVICE.createInterBranchMapping(usr, entityManager, newBranch);
			}
			if (ConfigParams.getInstance().isDeploymentSingleUser(usr)) {
				// For Single User Deployment Only
				if (newBranch.getModifiedAt() == null) {
					singleUserService.updateOnBranchCreation(usr, newBranch, entityManager);
				}
			}

			auditDAO.saveAuditLogs("added/updated branch basic details", usr, newBranch.getId(), Branch.class,
					ipAddress, json.toString(), entityManager);
			// Map<String, ActorRef> orgregistrered = new HashMap<String, ActorRef>();
			// Object[] keyArray = AdminActor.adminRegistered.keySet().toArray();
			// for (int i = 0; i < keyArray.length; i++) {
			// List<Users> users = Users.findByEmailActDeact(entityManager, (String)
			// keyArray[i]);
			// if (!users.isEmpty() && users.get(0).getOrganization().getId() ==
			// newBranch.getOrganization().getId()) {
			// orgregistrered.put(keyArray[i].toString(),
			// AdminActor.adminRegistered.get(keyArray[i]));
			// }
			// }
			System.out.println("isDigitalSignEnabled" + entityManager);
			Map<String, String> countries = CountryCurrencyUtil.getCountries();
			String country = "";
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
			branchCashService.saveBranchCash(json, newBranch, usr, org, branchrentRevisionDueOn, premiseType,
					entityManager, genericDAO, auditDAO, emailsession, ipAddress);
			saveBranchOtherInformations(json, newBranch, usr, org, bnchFacility, bnchName, branchrentRevisionDueOn,
					premiseType, rentRevisionDueOnRemarks, ipAddress, newBranchFlag);
			result.put("branchId", newBranch.getId());
			result.put("id", newBranch.getId());
			result.put("branchName", newBranch.getName());
			result.put("branchgstin", newBranch.getGstin());
			result.put("country", country);
			result.put("location", newBranch.getLocation());
			result.put("phoneNumber", newBranch.getPhoneNumber());
			result.put("oldName", oldName);
			result.put("actionText", actionText);
		} catch (Exception ex) {
			reportException(entityManager, transaction, usr, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, transaction, usr, th, result);
		}
		log.log(Level.FINE, ">>>> End");
		return Results.ok(result);
	}

	@Transactional
	public Result addUser(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users newuser = null;
		Users sessionuser = null;
		try {
			transaction.begin();
			sessionuser = getUserInfo(request);
			JsonNode json = request.body().asJson();
			ArrayNode useran = result.putArray("userData");
			String userHidpk = json.findValue("userHiddenPrimaryKey").asText();
			String fullName = json.findValue("fullName").asText();
			String email = json.findValue("emailId").asText();
			String password = null;
			String usraddress = json.findValue("usraddress") != null ? json.findValue("usraddress").asText() : null;
			String usrdob = json.findValue("usrdob") != null ? json.findValue("usrdob").asText() : null;
			String usrctrycode = json.findValue("usrphncountrycode") != null
					? json.findValue("usrphncountrycode").asText()
					: null;
			String usrmobile = json.findValue("usrmobile") != null ? json.findValue("usrmobile").asText() : null;
			String usrphoto = json.findValue("usrphoto") != null ? json.findValue("usrphoto").asText() : null;
			String usridproof = json.findValue("usridproof") != null ? json.findValue("usridproof").asText() : null;
			String usraltemail = json.findValue("usraltemail") != null ? json.findValue("usraltemail").asText() : null;
			String userBranch = json.findValue("userBranch").asText();
			Branch newuserBranch = Branch.findById(IdosUtil.convertStringToLong(userBranch));
			String userRole = json.findValue("userRoles").asText();
			String newUserRole = userRole.substring(0, userRole.length() - 1);
			if (userHidpk != "" && userHidpk != null) {
				newuser = Users.findById(IdosUtil.convertStringToLong(userHidpk));
				Map<String, Object> criterias = new HashMap<String, Object>();
				criterias.put("user.id", newuser.getId());
				genericDAO.deleteByCriteria(UsersRoles.class, criterias, entityManager);
				List<UsersRoles> userRoles = newuser.getUserRoles();
				for (UsersRoles usrRole : userRoles) {
					if (usrRole.getRole().getId() == 1L) {
						criterias.clear();
						criterias.put("organization.id", sessionuser.getOrganization().getId());
						criterias.put("isHeadQuarter", 1);
						criterias.put("presentStatus", 1);
						newuserBranch = genericDAO.getByCriteria(Branch.class, criterias, entityManager);
					}
					if (usrRole.getRole().getId() == 8L) {
						newuserBranch = newuser.getBranchSafeDepositBox().getBranch();
					}
					if (usrRole.getRole().getId() == 12L) {
						newuserBranch = newuser.getBranchKeyOff().getBranch();
					}
					if (usrRole.getRole().getId() == 12L) {
						newuserBranch = newuser.getBranchKeyOff().getBranch();
					}
				}
			} else {
				newuser = new Users();
				password = PasswordUtil.gen(10);
				newuser.setPassword(PasswordUtil.encrypt(password));
			}
			newuser.setOrganization(sessionuser.getOrganization());
			newuser.setFullName(fullName);
			newuser.setEmail(email);
			newuser.setAddress(usraddress);
			newuser.setPhoneNumberCountryCode(usrctrycode);
			newuser.setMobile(usrmobile);
			newuser.setPhotograph(usrphoto);
			newuser.setBranch(newuserBranch);
			newuser.setOrganization(newuserBranch.getOrganization());
			newuser.setIdproof(usridproof);
			newuser.setAltEmail(usraltemail);
			if (usrdob != null) {
				Date dob = idosdf.parse(usrdob);
				String dob1 = mysqldf.format(dob);
				Date dateOfBirth = mysqldf.parse(dob1);
				newuser.setDob(dateOfBirth);
			}
			String bnchNames = "";
			usercrud.save(sessionuser, newuser, entityManager);
			String amt = json.findValue("amt") != null ? json.findValue("amt").asText() : null;
			String roleid = json.findValue("roleid") != null ? json.findValue("roleid").asText() : null;
			String amount[] = null;
			String rolesid[] = null;
			if (amt != null) {
				amount = amt.substring(0, amt.length() - 1).split(",");
				rolesid = roleid.substring(0, roleid.length() - 1).split(",");
			}
			int k = 0;
			String newrole[] = newUserRole.split(",");
			String userRolesStr = "";
			for (int i = 0; i < newrole.length; i++) {
				UsersRoles newuserrole = new UsersRoles();
				if (amt != null) {
					if ((k < amount.length) && IdosUtil.convertStringToDouble(rolesid[k]) == IdosUtil
							.convertStringToDouble(newrole[i])) {
						newuserrole.setAmountLimit(IdosUtil.convertStringToDouble(amount[k]));
						k++;
					}
				}
				Role userrole = Role.findById(IdosUtil.convertStringToLong(newrole[i]));
				newuserrole.setRole(userrole);
				newuserrole.setUser(newuser);
				newuserrole.setBranch(newuserBranch);
				newuserrole.setOrganization(newuserBranch.getOrganization());
				userRolesStr += userrole.getName() + ",";
				userrolecrud.save(sessionuser, newuserrole, entityManager);
			}
			Users user = Users.findById(newuser.getId());
			if (user != null) {
				ObjectNode row = Json.newObject();
				row.put("id", user.getId());
				row.put("fullName", user.getFullName());
				row.put("userEmail", user.getEmail());
				row.put("userBranch", user.getBranch().getName());
				row.put("userRole", userRolesStr);
				useran.add(row);
			}
			transaction.commit();
			String body = userAccountCreation.render(newuser.getEmail(), password, ConfigParams.getInstance()).body();
			final String username = ConfigFactory.load().getString("smtp.user");
			Session session = emailsession;
			String subject = "Successfully Created Users for Organization " + newuser.getOrganization().getName();
			if (userHidpk.equals("") || userHidpk == null) {
				mailTimer(body, username, session, newuser.getEmail(), null, subject);
			}
			result.put("userId", newuser.getId());
		} catch (Exception ex) {
			reportException(entityManager, transaction, sessionuser, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, transaction, sessionuser, th, result);
		}
		return Results.ok(result);
	}

	@Transactional
	public Result removeBranch(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String bnchId = json.findValue("bnchId").asText();
			Branch removeBranch = Branch.findById(IdosUtil.convertStringToLong(bnchId));
			List<BranchDepositBoxKey> removebnchsDepKey = removeBranch.getBranchDepositKeys();
			for (BranchDepositBoxKey bnchDep : removebnchsDepKey) {
				bnchdepboxcrud.delete(bnchDep, entityManager);
			}
			bnchcrud.delete(removeBranch, entityManager);
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "RemoveBranch Email", "RemoveBranch Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result removeRole(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String roleId = json.findValue("roleId").asText();
			Role removeRole = Role.findById(IdosUtil.convertStringToLong(roleId));
			rolecrud.delete(removeRole, entityManager);
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "RemoveRole Email", "RemoveRole Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result removeCategory(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String roleId = json.findValue("categoryId").asText();
			Particulars removeCat = Particulars.findById(IdosUtil.convertStringToLong(roleId));
			partcrud.delete(removeCat, entityManager);
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "RemoveCategory Email", "RemoveCategory Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result removeItem(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String specfId = json.findValue("itemId").asText();
			Specifics removeSpecf = Specifics.findById(IdosUtil.convertStringToLong(specfId));
			specfcrud.delete(removeSpecf, entityManager);
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "RemoveItem Email", "RemoveItem Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result removeVendor(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String specfId = json.findValue("vendorId").asText();
			Vendor removeVendor = Vendor.findById(IdosUtil.convertStringToLong(specfId));
			List<VendorSpecific> vendorSpecifics = removeVendor.getVendorsSpecifics();
			for (VendorSpecific vendorSeecf : vendorSpecifics) {
				vendorspecfcrud.delete(vendorSeecf, entityManager);
			}
			vendcrud.delete(removeVendor, entityManager);
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "RemoveVendor Email", "RemoveVendor Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result removeUser(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String userId = json.findValue("userId").asText();
			Users removeuser = Users.findById(IdosUtil.convertStringToLong(userId));
			List<UsersRoles> removeUserRoles = removeuser.getUserRoles();
			for (UsersRoles userroles : removeUserRoles) {
				userrolecrud.delete(userroles, entityManager);
			}
			usercrud.delete(removeuser, entityManager);
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "RemoveUser Email", "RemoveUser Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result getTaxes() {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		ObjectNode row = Json.newObject();
		try {
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "GetTaxes Email", "GetTaxes Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result removeProject(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String projectId = json.findValue("projectId").asText();
			Project removeproject = Project.findById(IdosUtil.convertStringToLong(projectId));
			List<ProjectBranches> removePjctBnchs = removeproject.getProjectBranch();
			for (ProjectBranches pjctbnchs : removePjctBnchs) {
				projBranchcrud.delete(pjctbnchs, entityManager);
			}
			projcrud.delete(removeproject, entityManager);
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "RemoveProject Email", "RemoveProject Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result saveTax() {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		try {
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "SaveTax Email", "SaveTax Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result updateCompLogo(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Users user = getUserInfo(request);
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		try {
			transaction.begin();
			MultipartFormData<File> body = request.body().asMultipartFormData();
			List<FilePart<File>> image = body.getFiles();
			String fileName = image.get(0).getFilename();
			Users usr = getUserInfo(request);
			Organization org = usr.getOrganization();
			String path = application.path().toString() + "/public/images/" + org.getName() + fileName;
			String logo = org.getName() + fileName;
			path = path.replaceAll("\\s", "");
			logo = logo.replaceAll("\\s", "");
			File f2 = new File(path);
			for (FilePart<File> filePart : image) {
				TemporaryFile temporaryFile = (TemporaryFile) filePart.getRef();
				String filePath = temporaryFile.path().toString();
				File file = new File(filePath);
				FileInputStream in = new FileInputStream(file);
				FileOutputStream out = new FileOutputStream(f2);
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
			}
			org.setCompanyLogo(logo);
			orgcrud.save(user, org, entityManager);
			transaction.commit();
		} catch (Exception ex) {
			reportException(entityManager, transaction, null, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, transaction, null, th, result);
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getBranchTaxes(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		JsonNode json = request.body().asJson();
		Users userInfo = getUserInfo(request);
		ArrayNode branchTaxan = result.putArray("brancTaxData");
		try {
			String branches = json.findValue("taxBranch").asText();
			if (branches != "") {
				String itemBranches[] = branches.split(",");
				for (int i = 0; i < itemBranches.length; i++) {
					Branch branch = Branch.findById(IdosUtil.convertStringToLong(itemBranches[i]));
					Map<String, Object> criterias = new HashMap<String, Object>();
					criterias.clear();
					criterias.put("branchTaxes.id", branch.getId());
					criterias.put("organization.id", branch.getOrganization().getId());
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, userInfo.getEmail(), userInfo.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getConfiguration(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		ArrayNode configinitialan = result.putArray("initialConfigData");
		Users userInfo = getUserInfo(request);
		ObjectNode row = Json.newObject();
		String role = session.getOptional("role").orElse("");
		row.put("companyHasCOA", userInfo.getOrganization().getCompanyHasChartOfAccounts());
		row.put("userRole", role);
		configinitialan.add(row);
		return Results.ok(result);
	}

	@Transactional
	public Result removeUserRoleSpec(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Users user = getUserInfo(request);
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String userId = json.findValue("userId").asText();
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.clear();
			criterias.put("user.id", IdosUtil.convertStringToLong(userId));
			criterias.put("presentStatus", 1);
			List<UsersRoles> tmpUsrRole = genericDAO.findByCriteria(UsersRoles.class, criterias, entityManager);
			for (UsersRoles newTmpUsrRole : tmpUsrRole) {
				newTmpUsrRole.setAmountLimit(null);
				userrolecrud.save(user, newTmpUsrRole, entityManager);

			}
			criterias.clear();
			criterias.put("user.id", IdosUtil.convertStringToLong(userId));
			genericDAO.deleteByCriteria(UserRolesSpecifics.class, criterias, entityManager);
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "RemoveUserRoleSpec Email", "RemoveUserRoleSpec Configuration",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result addUserRoleSpec(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Users user = getUserInfo(request);
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		try {
			transaction.begin();
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			String items = json.findValue("items").asText();
			String amount = json.findValue("amount").asText();
			String userId = json.findValue("userId").asText();
			String roleId = json.findValue("roleId").asText();
			criterias.put("user.id", IdosUtil.convertStringToLong(userId));
			criterias.put("presentStatus", 1);
			UserRolesSpecifics tmpusrspec = genericDAO.getByCriteria(UserRolesSpecifics.class, criterias,
					entityManager);
			if (tmpusrspec != null) {
				genericDAO.deleteByCriteria(UserRolesSpecifics.class, criterias, entityManager);
			}
			criterias.clear();
			criterias.put("user.id", IdosUtil.convertStringToLong(userId));
			criterias.put("role.id", IdosUtil.convertStringToLong(roleId));
			criterias.put("presentStatus", 1);
			UsersRoles usrRole = genericDAO.getByCriteria(UsersRoles.class, criterias, entityManager);
			usrRole.setAmountLimit(Double.valueOf(amount));
			userrolecrud.save(user, usrRole, entityManager);
			String item[] = items.split(",");
			for (int i = 0; i < item.length; i++) {
				UserRolesSpecifics usrSpec = new UserRolesSpecifics();
				if (!item[i].equals("Select All")) {
					Specifics userSpec = Specifics.findById(IdosUtil.convertStringToLong(item[i]));
					usrSpec.setUser(Users.findById(IdosUtil.convertStringToLong(userId)));
					usrSpec.setRole(Role.findById(IdosUtil.convertStringToLong(roleId)));
					usrSpec.setSpecifics(userSpec);
					usrSpec.setParticulars(userSpec.getParticularsId());
					usrRoleSpeccrud.save(user, usrSpec, entityManager);
				}
			}
			transaction.commit();
		} catch (Exception ex) {
			reportException(entityManager, transaction, null, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, transaction, null, th, result);
		}
		return Results.ok(result);
	}

	@Transactional
	public Result changePageLabel(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users usr = getUserInfo(request);
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode an = result.putArray("newLabel");
			String labelDispValue = json.findValue("labelDispValue").asText();
			String labelelemId = json.findValue("labelelemId").asText();
			String sbquery = ("select obj from DynamicLabel obj where  obj.organization.id= ?1 and obj.labelDomId= ?2 and obj.presentStatus=1 ");
			DynamicLabel dynmLab = null;
			ArrayList inparam = new ArrayList(2);
			inparam.add(usr.getOrganization().getId());
			inparam.add(labelelemId);
			List<DynamicLabel> entityList = genericDAO.queryWithParams(sbquery, entityManager, inparam);
			if (entityList.size() > 0) {
				dynmLab = entityList.get(0);
			} else {
				dynmLab = new DynamicLabel();
			}
			dynmLab.setLabelDomId(labelelemId);
			dynmLab.setLabelDisplayName(labelDispValue);
			dynmLab.setOrganization(usr.getOrganization());
			genericDAO.saveOrUpdate(dynmLab, usr, entityManager);
			ObjectNode row = Json.newObject();
			row.put("text", dynmLab.getLabelDisplayName());
			an.add(row);
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usr.getEmail(), usr.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	private static Result saveBranchOtherInformations(JsonNode json, Branch newBranch, Users usr, Organization org,
			Integer bnchFacility, String bnchName, String branchrentRevisionDueOn, String premiseType,
			String rentRevisionDueOnRemarks, String ipAddress, boolean newBranchFlag)
			throws ParseException, IDOSException {
		log.log(Level.FINE, ">>>> Start " + json);
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			// branch officer logic;
			OrganizationKeyOfficials newkeyoff = null;
			Users branchOfficerUsers = null;
			UsersRoles branchOfficerUsersRole = null;
			String keyOffHiddenIds = json.findPath("koffhiddIds").asText();
			if (keyOffHiddenIds.equals("")) {
				keyOffHiddenIds = ",";
			}

			String tempKOffName = json.findPath("koffname").asText();
			String tempKOffDesignation = json.findPath("koffDesignation").asText();
			String tempKOffCountry = json.findPath("koffCountry").asText();
			String tempKOffCity = json.findPath("koffCity").asText();
			String tempKPhNoCtryCd = json.findPath("kphnoccode").asText();
			String tempKPhNo = json.findPath("kphno").asText();
			String tempKEmail = json.findPath("kemail").asText();
			String tempKidProof = json.findPath("kidProof").asText();
			String tempKycDoc = json.findPath("kycDoc").asText();
			String tempKOffPersCtryCd = json.findPath("kpersPhNoCtryCd").asText();
			String tempKPersPhNo = json.findPath("kpersPhNo").asText();
			String keyOffHidIds[] = keyOffHiddenIds.substring(0, (keyOffHiddenIds.length())).split(",");
			String keyOffName[] = tempKOffName.substring(0, (tempKOffName.length())).split(",", -1);
			String keyOffDsgn[] = tempKOffDesignation.substring(0, (tempKOffDesignation.length())).split(",", -1);
			String keyOffCtry[] = tempKOffCountry.substring(0, (tempKOffCountry.length())).split(",", -1);
			String keyOffCity[] = tempKOffCity.substring(0, (tempKOffCity.length())).split(",", -1);
			String keyPhNoCtryCd[] = tempKPhNoCtryCd.substring(0, (tempKPhNoCtryCd.length())).split(",", -1);
			String keyPhNo[] = tempKPhNo.substring(0, (tempKPhNo.length())).split(",", -1);
			String keyPersPhNoCtryCd[] = tempKOffPersCtryCd.substring(0, (tempKOffPersCtryCd.length())).split(",", -1);
			String keyPersPhNo[] = tempKPersPhNo.substring(0, (tempKPersPhNo.length())).split(",", -1);
			String keyEmail[] = tempKEmail.substring(0, (tempKEmail.length())).split(",", -1);
			String keyIdProof[] = tempKidProof.substring(0, (tempKidProof.length())).split(",", -1);
			String keyKycDoc[] = null;
			if (tempKycDoc != null) {
				keyKycDoc = tempKycDoc.substring(0, (tempKycDoc.length())).split(",", -1);
			}
			String branchAdmin = null;

			if (ConfigParams.getInstance().isDeploymentSingleUser(usr))
				branchAdmin = json.findPath("branchAdminForSingleUser") != null
						? json.findPath("branchAdminForSingleUser").asText()
						: null;
			else
				branchAdmin = json.findPath("branchAdmin") != null ? json.findPath("branchAdmin").asText() : null;

			Map<String, Object> criterias = new HashMap<String, Object>();
			for (int i = 0; i < keyOffHidIds.length; i++) {
				if (!keyOffHidIds[i].equals("") && keyOffHidIds[i] != null) {
					if (!keyEmail[i].trim().equals("")) {
						newkeyoff = OrganizationKeyOfficials.findById(IdosUtil.convertStringToLong(keyOffHidIds[i]));
						criterias.clear();
						criterias.put("email", keyEmail[i].trim());
						criterias.put("organization.id", org.getId());
						criterias.put("presentStatus", 1);
						Users newBranchOfficerDuringUpdate = genericDAO.getByCriteria(Users.class, criterias,
								entityManager);
						criterias.clear();
						criterias.put("branchKeyOff.id", IdosUtil.convertStringToLong(keyOffHidIds[i]));
						criterias.put("presentStatus", 1);
						branchOfficerUsers = genericDAO.getByCriteria(Users.class, criterias, entityManager);
						if (keyOffName.length > i) {
							newkeyoff.setName(keyOffName[i].trim());
						}
						if (keyOffDsgn.length > i) {
							newkeyoff.setDesignation(keyOffDsgn[i].trim());
						}
						if (keyOffCtry.length > i) {
							if (!keyOffCtry[i].trim().equals("") && keyOffCtry[i].trim() != null) {
								newkeyoff.setCountry(IdosUtil.convertStringToInt(keyOffCtry[i]));
							}
						}
						if (keyOffDsgn.length > i) {
							newkeyoff.setDesignation(keyOffDsgn[i].trim());
						}
						if (keyOffCity.length > i) {
							newkeyoff.setCity(keyOffCity[i].trim());
						}
						if (keyPhNoCtryCd.length > i) {
							newkeyoff.setCtryPhCode(keyPhNoCtryCd[i].trim());
						}
						if (keyPhNo.length > i) {
							newkeyoff.setPhoneNumber(keyPhNo[i].trim());
						}
						if (keyPersPhNoCtryCd.length > i) {
							newkeyoff.setPersonalPhoneCountryCode(keyPersPhNoCtryCd[i].trim());
						}
						if (keyPersPhNo.length > i) {
							newkeyoff.setPersonalPhoneNumber(keyPersPhNo[i].trim());
						}
						if (keyEmail.length > i) {
							newkeyoff.setEmail(keyEmail[i].trim());
						}
						if (keyIdProof.length > i) {
							newkeyoff.setUploadedId(keyIdProof[i].trim());
						}
						if (keyKycDoc != null && keyKycDoc.length > i) {
							newkeyoff.setUploadedKycId(keyKycDoc[i].trim());
						}
						newkeyoff.setBranch(newBranch);
						newkeyoff.setOrganization(org);
						genericDAO.saveOrUpdate(newkeyoff, usr, entityManager);
						auditDAO.saveAuditLogs("updated Branch officers details", usr, newkeyoff.getId(), Users.class,
								ipAddress, json.toString(), entityManager);
						if (branchOfficerUsers != null && newBranchOfficerDuringUpdate != null) {
							branchOfficerUsers.setFullName(keyOffName[i].trim());
							branchOfficerUsers.setEmail(keyEmail[i].trim());
							branchOfficerUsers.setBranchKeyOff(newkeyoff);
							branchOfficerUsers.setOrganization(org);
							branchOfficerUsers.setBranch(newBranch);
							branchOfficerUsers.setBranchKeyOff(newkeyoff);
							if (keyOffDsgn.length > i) {
								branchOfficerUsers.setDesignation(keyOffDsgn[i].trim());
							}
							genericDAO.saveOrUpdate(branchOfficerUsers, usr, entityManager);
							auditDAO.saveAuditLogs("updated Branch officers user details", usr,
									branchOfficerUsers.getId(), Users.class, ipAddress, json.toString(), entityManager);
						} else {
							if (branchOfficerUsers != null) {
								branchOfficerUsers.setBranchKeyOff(null);
								genericDAO.saveOrUpdate(branchOfficerUsers, usr, entityManager);
							}
							branchOfficerUsers = new Users();
							branchOfficerUsers.setFullName(keyOffName[i].trim());
							branchOfficerUsers.setEmail(keyEmail[i].trim());
							branchOfficerUsers.setBranchKeyOff(newkeyoff);
							branchOfficerUsers.setOrganization(org);
							branchOfficerUsers.setBranch(newBranch);
							branchOfficerUsers.setBranchKeyOff(newkeyoff);
							if (keyOffDsgn.length > i) {
								branchOfficerUsers.setDesignation(keyOffDsgn[i].trim());
							}
							genericDAO.saveOrUpdate(branchOfficerUsers, usr, entityManager);
							auditDAO.saveAuditLogs("created Branch officers user details", usr,
									branchOfficerUsers.getId(), Users.class, ipAddress, json.toString(), entityManager);
							String password = PasswordUtil.gen(10);
							String body = userAccountCreation
									.render(branchOfficerUsers.getEmail(), password, ConfigParams.getInstance()).body();
							final String username = ConfigFactory.load().getString("smtp.user");
							Session session = emailsession;
							String subject = "Successfully Created Users for Organization " + org.getName();
							mailTimer1(body, username, session, branchOfficerUsers.getEmail(), null, subject);
						}
						/*
						 * no need to set officer role for officer confirmed by Srikanth on 17th March
						 * 2017
						 * instead of that set as CREATOR
						 */
						criterias.clear();
						criterias.put("id", 3L);
						criterias.put("presentStatus", 1);
						Role usrRole = genericDAO.getByCriteria(Role.class, criterias, entityManager);
						criterias.clear();
						criterias.put("branchKeyOff.id", IdosUtil.convertStringToLong(keyOffHidIds[i]));
						criterias.put("presentStatus", 1);
						branchOfficerUsersRole = genericDAO.getByCriteria(UsersRoles.class, criterias, entityManager);
						if (branchOfficerUsersRole != null) {
							branchOfficerUsersRole.setRole(usrRole);
							branchOfficerUsersRole.setUser(branchOfficerUsers);
							branchOfficerUsersRole.setOrganization(org);
							branchOfficerUsersRole.setBranch(newBranch);
							branchOfficerUsersRole.setBranchKeyOff(newkeyoff);
							genericDAO.saveOrUpdate(branchOfficerUsersRole, usr, entityManager);
							auditDAO.saveAuditLogs("updated Branch officers user roles details", usr,
									branchOfficerUsersRole.getId(), UsersRoles.class, ipAddress, json.toString(),
									entityManager);
							if (log.isLoggable(Level.FINE))
								log.log(Level.FINE, "updated Branch officers user roles details 1");
						} else if (branchOfficerUsersRole == null) {
							branchOfficerUsersRole = new UsersRoles();
							branchOfficerUsersRole.setRole(usrRole);
							branchOfficerUsersRole.setUser(branchOfficerUsers);
							branchOfficerUsersRole.setOrganization(org);
							branchOfficerUsersRole.setBranch(newBranch);
							branchOfficerUsersRole.setBranchKeyOff(newkeyoff);
							genericDAO.saveOrUpdate(branchOfficerUsersRole, usr, entityManager);
							auditDAO.saveAuditLogs("updated Branch officers user roles details", usr,
									branchOfficerUsersRole.getId(), UsersRoles.class, ipAddress, json.toString(),
									entityManager);
							if (log.isLoggable(Level.FINE))
								log.log(Level.FINE, "updated Branch officers user roles details 2");
						}
					}
				}
			}
			for (int i = keyOffHidIds.length; (i < keyOffName.length || i < keyOffDsgn.length
					|| i < keyPhNoCtryCd.length || i < keyPhNo.length || i < keyEmail.length); i++) {
				newkeyoff = new OrganizationKeyOfficials();
				branchOfficerUsers = new Users();
				branchOfficerUsersRole = new UsersRoles();
				// if(log.isLoggable(Level.FINE)) log.log(Level.FINE, ">>>>>>>> " +
				// keyEmail[i]);

				if (keyEmail.length > i && !keyEmail[i].trim().equals("")) {
					if (keyOffName.length > i) {
						newkeyoff.setName(keyOffName[i].trim());
					}
					if (keyOffDsgn.length > i) {
						newkeyoff.setDesignation(keyOffDsgn[i].trim());
					}
					if (keyOffCtry.length > i) {
						if (!keyOffCtry[i].trim().equals("") && keyOffCtry[i].trim() != null) {
							newkeyoff.setCountry(IdosUtil.convertStringToInt(keyOffCtry[i]));
						}
					}
					if (keyOffDsgn.length > i) {
						newkeyoff.setDesignation(keyOffDsgn[i].trim());
					}
					if (keyOffCity.length > i) {
						newkeyoff.setCity(keyOffCity[i].trim());
					}
					if (keyPhNoCtryCd.length > i) {
						newkeyoff.setCtryPhCode(keyPhNoCtryCd[i].trim());
					}
					if (keyPhNo.length > i) {
						newkeyoff.setPhoneNumber(keyPhNo[i].trim());
					}
					if (keyPersPhNoCtryCd.length > i) {
						newkeyoff.setPersonalPhoneCountryCode(keyPersPhNoCtryCd[i].trim());
					}
					if (keyPersPhNo.length > i) {
						newkeyoff.setPersonalPhoneNumber(keyPersPhNo[i].trim());
					}
					if (keyEmail.length > i) {
						newkeyoff.setEmail(keyEmail[i].trim());
					}
					if (keyIdProof.length > i) {
						newkeyoff.setUploadedId(keyIdProof[i].trim());
					}
					if (keyKycDoc.length > i) {

						newkeyoff.setUploadedKycId(keyKycDoc[i].trim());

					}
					newkeyoff.setBranch(newBranch);
					newkeyoff.setOrganization(org);
					genericDAO.saveOrUpdate(newkeyoff, usr, entityManager);

					branchOfficerUsers.setFullName(keyOffName[i].trim());
					criterias.clear();
					criterias.put("email", keyEmail[i].trim());
					criterias.put("organization.id", org.getId());
					criterias.put("presentStatus", 1);
					Users existingusr = genericDAO.getByCriteria(Users.class, criterias, entityManager);
					if (existingusr != null) {
						criterias.clear();
						criterias.put("role.id", 3L);
						criterias.put("user.id", existingusr.getId());
						criterias.put("presentStatus", 1);
						UsersRoles existingUserRoles = genericDAO.getByCriteria(UsersRoles.class, criterias,
								entityManager);
						branchOfficerUsers = existingusr;
						branchOfficerUsersRole = existingUserRoles;
					}
					if (branchOfficerUsersRole == null) {
						branchOfficerUsersRole = new UsersRoles();
					}
					branchOfficerUsers.setEmail(keyEmail[i].trim());
					branchOfficerUsers.setBranchKeyOff(newkeyoff);
					String password = PasswordUtil.gen(10);
					branchOfficerUsers.setPassword(PasswordUtil.encrypt(password));
					branchOfficerUsers.setOrganization(org);
					branchOfficerUsers.setBranch(newBranch);
					branchOfficerUsers.setBranchKeyOff(newkeyoff);
					genericDAO.saveOrUpdate(branchOfficerUsers, usr, entityManager);
					auditDAO.saveAuditLogs("added Branch officers user details", usr, branchOfficerUsers.getId(),
							Users.class, ipAddress, json.toString(), entityManager);
					criterias.clear();
					criterias.put("id", 3L);
					criterias.put("presentStatus", 1);
					Role usrRole = genericDAO.getByCriteria(Role.class, criterias, entityManager);
					branchOfficerUsersRole.setRole(usrRole);
					branchOfficerUsersRole.setUser(branchOfficerUsers);
					branchOfficerUsersRole.setOrganization(org);
					branchOfficerUsersRole.setBranch(newBranch);
					branchOfficerUsersRole.setBranchKeyOff(newkeyoff);
					genericDAO.saveOrUpdate(branchOfficerUsersRole, usr, entityManager);
					auditDAO.saveAuditLogs("added Branch officers user roles details", usr,
							branchOfficerUsersRole.getId(), UsersRoles.class, ipAddress, json.toString(),
							entityManager);
					if (log.isLoggable(Level.FINE))
						log.log(Level.FINE, "added Branch officers user roles details");
					String body = userAccountCreation
							.render(branchOfficerUsers.getEmail(), password, ConfigParams.getInstance()).body();
					final String username = ConfigFactory.load().getString("smtp.user");
					Session session = emailsession;
					String subject = "Successfully Created Users for Organization " + org.getName();
					mailTimer1(body, username, session, branchOfficerUsers.getEmail(), null, subject);
				}
			}
			// branch statutory logic
			StatutoryDetails newOrgStatDetails = null;
			String tempStatHiddenIds = json.findPath("stathidIds").asText();
			if (tempStatHiddenIds.equals("")) {
				tempStatHiddenIds = ",";
			}
			String tempStatDeatils = json.findPath("statdetails").asText();
			String tempStatRegNo = json.findPath("statRegNo").asText();
			String tempStatForInv = json.findPath("statForInv").asText();
			String tempStatRegDoc = json.findPath("statRegDoc").asText();
			String tempStatRegValidFrom = json.findPath("statRegValidFrom").asText();
			String tempStatRegValidTo = json.findPath("statRegValidTo").asText();
			String tempStatAltForActn = json.findPath("statAlertForAction").asText();
			String tempStatAltForInf = json.findPath("statAlertForInformation").asText();
			String tempstatNameAddConsultant = json.findPath("statNameAddConsultant").asText();
			String tempstatAlertRemarks = json.findPath("statAlertRemarks").asText();
			String statHiddenIds[] = tempStatHiddenIds.substring(0, (tempStatHiddenIds.length())).split(",");
			String statDetails[] = tempStatDeatils.substring(0, (tempStatDeatils.length())).split(",", -1);
			String statRegNo[] = tempStatRegNo.substring(0, (tempStatRegNo.length())).split(",", -1);
			String statForInv[] = tempStatForInv.substring(0, (tempStatForInv.length())).split(",", -1);
			String statRegDoc[] = tempStatRegDoc.substring(0, (tempStatRegDoc.length())).split(",", -1);
			String statRegValidFrom[] = tempStatRegValidFrom.substring(0, (tempStatRegValidFrom.length())).split("@",
					-1);
			String statRegValidTo[] = tempStatRegValidTo.substring(0, (tempStatRegValidTo.length())).split("@", -1);
			String statAltForActn[] = tempStatAltForActn.substring(0, (tempStatAltForActn.length())).split(",", -1);
			String statAltForInf[] = tempStatAltForInf.substring(0, (tempStatAltForInf.length())).split(",", -1);
			String statNameAddConsultant[] = tempstatNameAddConsultant
					.substring(0, (tempstatNameAddConsultant.length())).split("}", -1);
			String statAlertRemarks[] = tempstatAlertRemarks.substring(0, (tempstatAlertRemarks.length())).split("}",
					-1);
			for (int i = 0; i < statHiddenIds.length; i++) {
				if (!statHiddenIds[i].equals("") && statHiddenIds[i] != null) {
					newOrgStatDetails = StatutoryDetails.findById(IdosUtil.convertStringToLong(statHiddenIds[i]));
					if (statDetails.length > i) {
						newOrgStatDetails.setStatutoryDetails(statDetails[i].trim());
					}
					if (statRegNo.length > i) {
						newOrgStatDetails.setRegistrationNumber(statRegNo[i].trim());
					}
					if (statForInv.length > i) {
						if (!statForInv[i].trim().equals("")) {
							newOrgStatDetails.setIsStatutoryAvailableForInvoice(1);
						} else {
							newOrgStatDetails.setIsStatutoryAvailableForInvoice(0);
						}
					}
					/*
					 * if(statRegDoc.length>i){
					 * newOrgStatDetails.setRegistrationDoc(statRegDoc[i].trim());
					 * }
					 */
					newOrgStatDetails.setRegistrationDoc(
							transactionDao.getAndDeleteSupportingDocument(newOrgStatDetails.getRegistrationDoc(),
									usr.getEmail(), tempStatRegDoc, usr, entityManager));

					if (statRegValidFrom.length > i) {
						if (!statRegValidFrom[i].trim().equals("") && statRegValidFrom[i].trim() != null) {
							newOrgStatDetails
									.setValidFrom(mysqldf.parse(mysqldf.format(idosdf.parse(statRegValidFrom[i]))));
						}
					}
					if (statRegValidTo.length > i) {
						if (!statRegValidTo[i].trim().equals("") && statRegValidTo[i].trim() != null) {
							newOrgStatDetails
									.setValidTo(mysqldf.parse(mysqldf.format(idosdf.parse(statRegValidTo[i]))));
						}
					}
					if (statAltForActn.length > i) {
						newOrgStatDetails.setAlertForAction(statAltForActn[i]);
					}
					if (statAltForInf.length > i) {
						newOrgStatDetails.setAlertForInformation(statAltForInf[i]);
					}
					if (statNameAddConsultant.length > i) {
						newOrgStatDetails.setNameAddressOfConsultant(statNameAddConsultant[i]);
					}
					if (statAlertRemarks.length > i) {
						newOrgStatDetails.setRemarks(statAlertRemarks[i]);
					}
					newOrgStatDetails.setBranch(newBranch);
					newOrgStatDetails.setOrganization(org);
					orgstatdtlscrud.save(usr, newOrgStatDetails, entityManager);
					FILE_UPLOAD_SERVICE.updateUploadFileLogs(entityManager, usr, newOrgStatDetails.getRegistrationDoc(),
							newBranch.getId(), IdosConstants.BRANCH_MODULE);
					auditDAO.saveAuditLogs("updated branch statutory details", usr, newOrgStatDetails.getId(),
							StatutoryDetails.class, ipAddress, json.toString(), entityManager);
				}
			}
			for (int i = statHiddenIds.length; (i < statDetails.length || i < statRegNo.length || i < statRegDoc.length
					|| i < statRegValidFrom.length || i < statRegValidTo.length); i++) {
				newOrgStatDetails = new StatutoryDetails();
				if ((statDetails.length > i && !statDetails[i].trim().equals(""))
						|| (statDetails.length > i && !statRegNo[i].trim().equals(""))) {
					if (statDetails.length > i) {
						newOrgStatDetails.setStatutoryDetails(statDetails[i].trim());
					}
					if (statRegNo.length > i) {
						newOrgStatDetails.setRegistrationNumber(statRegNo[i].trim());
					}
					if (statForInv.length > i) {
						if (!statForInv[i].trim().equals("")) {
							newOrgStatDetails.setIsStatutoryAvailableForInvoice(1);
						} else {
							newOrgStatDetails.setIsStatutoryAvailableForInvoice(0);
						}
					}
					/*
					 * if(statRegDoc.length>i){
					 * newOrgStatDetails.setRegistrationDoc(statRegDoc[i].trim());
					 * }
					 */
					newOrgStatDetails.setRegistrationDoc(
							transactionDao.getAndDeleteSupportingDocument(newOrgStatDetails.getRegistrationDoc(),
									usr.getEmail(), tempStatRegDoc, usr, entityManager));

					if (statRegValidFrom.length > i) {
						if (!statRegValidFrom[i].trim().equals("") && statRegValidFrom[i].trim() != null) {
							newOrgStatDetails
									.setValidFrom(mysqldf.parse(mysqldf.format(idosdf.parse(statRegValidFrom[i]))));
						}
					}
					if (statRegValidTo.length > i) {
						if (!statRegValidTo[i].trim().equals("") && statRegValidTo[i].trim() != null) {
							newOrgStatDetails
									.setValidTo(mysqldf.parse(mysqldf.format(idosdf.parse(statRegValidTo[i]))));
						}
					}
					if (statAltForActn.length > i) {
						newOrgStatDetails.setAlertForAction(statAltForActn[i]);
					}
					if (statAltForInf.length > i) {
						newOrgStatDetails.setAlertForInformation(statAltForInf[i]);
					}
					if (statNameAddConsultant.length > i) {
						newOrgStatDetails.setNameAddressOfConsultant(statNameAddConsultant[i]);
					}
					if (statAlertRemarks.length > i) {
						newOrgStatDetails.setRemarks(statAlertRemarks[i]);
					}
					newOrgStatDetails.setBranch(newBranch);
					newOrgStatDetails.setOrganization(org);
					orgstatdtlscrud.save(usr, newOrgStatDetails, entityManager);
					FILE_UPLOAD_SERVICE.updateUploadFileLogs(entityManager, usr, newOrgStatDetails.getRegistrationDoc(),
							newBranch.getId(), IdosConstants.BRANCH_MODULE);
					auditDAO.saveAuditLogs("added branch statutory details", usr, newOrgStatDetails.getId(),
							StatutoryDetails.class, ipAddress, json.toString(), entityManager);
				}
			}
			// start operational remainder logic
			OrganizationOperationalRemainders opeRem = null;
			String tempopeRemhidIds = json.findPath("opeRemhidIds").asText();
			if (tempopeRemhidIds.equals("")) {
				tempopeRemhidIds += ",";
			}
			String tempopeRemRequirements = json.findPath("opeRemRequirements").asText();
			String tempopeRemRecurrences = json.findPath("opeRemRecurrences").asText();
			String tempopeRemDueOn = json.findPath("opeRemDueOn").asText();
			String tempoperRemindersValidTo = json.findPath("operRemindersValidTo").asText();
			String tempopeRemAltForAction = json.findPath("opeRemAltForAction").asText();
			String tempopeRemAltForInformation = json.findPath("opeRemAltForInformation").asText();
			String tempopeRemAltRemarks = json.findPath("opeRemAltRemarks").asText();
			String opeRemhidIds[] = tempopeRemhidIds.substring(0, (tempopeRemhidIds.length())).split(",");
			String opeRemRequirements[] = tempopeRemRequirements.substring(0, (tempopeRemRequirements.length()))
					.split(",", -1);
			String opeRemRecurrences[] = tempopeRemRecurrences.substring(0, (tempopeRemRecurrences.length())).split(",",
					-1);
			String opeRemDueOn[] = tempopeRemDueOn.substring(0, (tempopeRemDueOn.length())).split("@", -1);
			String operRemindersValidTo[] = tempoperRemindersValidTo.substring(0, (tempoperRemindersValidTo.length()))
					.split("@", -1);
			String opeRemAltForAction[] = tempopeRemAltForAction.substring(0, (tempopeRemAltForAction.length()))
					.split(",", -1);
			String opeRemAltForInformation[] = tempopeRemAltForInformation
					.substring(0, (tempopeRemAltForInformation.length())).split(",", -1);
			String opeRemAltRemarks[] = tempopeRemAltRemarks.substring(0, (tempopeRemAltRemarks.length())).split("}",
					-1);
			for (int i = 0; i < opeRemhidIds.length; i++) {
				if (!opeRemhidIds[i].equals("") && opeRemhidIds[i] != null) {
					opeRem = OrganizationOperationalRemainders.findById(IdosUtil.convertStringToLong(opeRemhidIds[i]));
					if (opeRemRequirements.length > i) {
						opeRem.setRequiements(opeRemRequirements[i].trim());
					}
					if (opeRemRecurrences.length > i) {
						if (!opeRemRecurrences[i].trim().equals("") && opeRemRecurrences[i].trim() != null) {
							opeRem.setRecurrences(IdosUtil.convertStringToInt(opeRemRecurrences[i]));
						}
					}
					if (opeRemDueOn.length > i) {
						if (!opeRemDueOn[i].trim().equals("") && opeRemDueOn[i].trim() != null) {
							opeRem.setDueOn(mysqldf.parse(mysqldf.format(idosdf.parse(opeRemDueOn[i]))));
						}
					}
					if (operRemindersValidTo.length > i) {
						if (!operRemindersValidTo[i].trim().equals("") && operRemindersValidTo[i].trim() != null) {
							opeRem.setValidTo(mysqldf.parse(mysqldf.format(idosdf.parse(operRemindersValidTo[i]))));
						}
					}
					if (opeRemAltForAction.length > i) {
						opeRem.setAlertForAction(opeRemAltForAction[i].trim());
					}
					if (opeRemAltForInformation.length > i) {
						opeRem.setAlertForInformation(opeRemAltForInformation[i].trim());
					}
					if (opeRemAltRemarks.length > i) {
						opeRem.setRemarks(opeRemAltRemarks[i].trim());
					}
					opeRem.setBranch(newBranch);
					opeRem.setOrganization(org);
					orgOpeRemCrud.save(usr, opeRem, entityManager);
					auditDAO.saveAuditLogs("updated branch operational remainders details", usr, opeRem.getId(),
							OrganizationOperationalRemainders.class, ipAddress, json.toString(), entityManager);
				}
			}
			for (int i = opeRemhidIds.length; (i < opeRemRequirements.length || i < opeRemRecurrences.length
					|| i < opeRemDueOn.length || i < opeRemAltForAction.length
					|| i < opeRemAltForInformation.length); i++) {
				opeRem = new OrganizationOperationalRemainders();
				if ((opeRemRequirements.length > i && !opeRemRequirements[i].trim().equals(""))
						|| (opeRemDueOn.length > i && !opeRemDueOn[i].trim().equals(""))) {
					if (opeRemRequirements.length > i) {
						opeRem.setRequiements(opeRemRequirements[i].trim());
					}
					if (opeRemRecurrences.length > i) {
						if (!opeRemRecurrences[i].trim().equals("") && opeRemRecurrences[i].trim() != null) {
							opeRem.setRecurrences(IdosUtil.convertStringToInt(opeRemRecurrences[i]));
						}
					}
					if (opeRemDueOn.length > i) {
						if (!opeRemDueOn[i].trim().equals("") && opeRemDueOn[i].trim() != null) {
							opeRem.setDueOn(mysqldf.parse(mysqldf.format(idosdf.parse(opeRemDueOn[i]))));
						}
					}
					if (operRemindersValidTo.length > i) {
						if (!operRemindersValidTo[i].trim().equals("") && operRemindersValidTo[i].trim() != null) {
							opeRem.setValidTo(mysqldf.parse(mysqldf.format(idosdf.parse(operRemindersValidTo[i]))));
						}
					}
					if (opeRemAltForAction.length > i) {
						opeRem.setAlertForAction(opeRemAltForAction[i].trim());
					}
					if (opeRemAltForInformation.length > i) {
						opeRem.setAlertForInformation(opeRemAltForInformation[i].trim());
					}
					if (opeRemAltRemarks.length > i) {
						opeRem.setRemarks(opeRemAltRemarks[i].trim());
					}
					opeRem.setBranch(newBranch);
					opeRem.setOrganization(org);
					orgOpeRemCrud.save(usr, opeRem, entityManager);
					auditDAO.saveAuditLogs("added branch operational remainders details", usr, opeRem.getId(),
							OrganizationOperationalRemainders.class, ipAddress, json.toString(), entityManager);
				}
			}
			// start branch safe deposit box logic
			/*
			 * BranchDepositBoxKey newBranchDepBoxKey=null;
			 * String keyDepositHiddenId= json.findPath("keyDepHidIds") == null ? null :
			 * json.findPath("keyDepHidIds").asText().trim();
			 * String keyDepositName = json.findPath("keyDepName") == null ? null :
			 * json.findPath("keyDepName").asText().trim();
			 * String keyDepositOpeningBalance = json.findPath("keyDepositOpeningBalance")
			 * == null ? null : json.findPath("keyDepositOpeningBalance").asText().trim();
			 * String keyDepositPhNoCtryCd = json.findPath("kPhNoCtryCd") == null ? null :
			 * json.findPath("kPhNoCtryCd").asText().trim();
			 * String keyDepositPhNo = json.findPath("keyDepPhNo") == null ? null :
			 * json.findPath("keyDepPhNo").asText().trim();
			 * String keyDepositEmail = json.findPath("keyDepEmail") == null ? null :
			 * json.findPath("keyDepEmail").asText().trim();
			 * String cashierName = json.findPath("cashName") == null ? null :
			 * json.findPath("cashName").asText().trim();
			 * String cashierPhNoCtryCd = json.findPath("cashierCtryCode") == null ? null :
			 * json.findPath("cashierCtryCode").asText().trim();
			 * String cashierPhNo = json.findPath("cashierNo") == null ? null :
			 * json.findPath("cashierNo").asText().trim();
			 * String cashierMail = json.findPath("cashierMail") == null ? null :
			 * json.findPath("cashierMail").asText().trim();
			 * String cashierKL = json.findPath("cashierKL") == null ? null :
			 * json.findPath("cashierKL").asText().trim();
			 * String cashierPettyTxnApprReqd = json.findPath("cashierPettyTxnApprReqd") ==
			 * null ? null : json.findPath("cashierPettyTxnApprReqd").asText().trim();
			 * String cashierPettyTxnApprAmtLimit =
			 * json.findPath("cashierPettyTxnApprAmtLimit") == null ? null :
			 * json.findPath("cashierPettyTxnApprAmtLimit").asText().trim();
			 * String pettyCashOpeningBalance = json.findPath("pettyCashOpeningBalance") ==
			 * null ? null : json.findPath("pettyCashOpeningBalance").asText().trim();
			 * 
			 * if(keyDepositHiddenId != null && !keyDepositHiddenId.equals("")){
			 * newBranchDepBoxKey =
			 * bnchDepBoxKey.findById(Long.parseLong(keyDepositHiddenId));
			 * }else {
			 * newBranchDepBoxKey = new BranchDepositBoxKey();
			 * }
			 * 
			 * if(keyDepositName != null ){
			 * newBranchDepBoxKey.setName(keyDepositName);
			 * }else{
			 * newBranchDepBoxKey.setName(null);
			 * }
			 * if(keyDepositOpeningBalance != null && !"".equals(keyDepositOpeningBalance)){
			 * newBranchDepBoxKey.setOpeningBalance(Double.parseDouble(
			 * keyDepositOpeningBalance));
			 * }else{
			 * newBranchDepBoxKey.setOpeningBalance(null);
			 * }
			 * if(keyDepositPhNoCtryCd != null){
			 * newBranchDepBoxKey.setCountryPhnCode(keyDepositPhNoCtryCd);
			 * }else{
			 * newBranchDepBoxKey.setCountryPhnCode(null);
			 * }
			 * if(keyDepositPhNo != null){
			 * newBranchDepBoxKey.setPhoneNumber(keyDepositPhNo);
			 * }else{
			 * newBranchDepBoxKey.setPhoneNumber(null);
			 * }
			 * if(keyDepositEmail != null ){
			 * newBranchDepBoxKey.setEmail(keyDepositEmail);
			 * }else{
			 * newBranchDepBoxKey.setEmail(null);
			 * }
			 * newBranchDepBoxKey.setOrganization(org);
			 * newBranchDepBoxKey.setBranch(newBranch);
			 * 
			 * if(pettyCashOpeningBalance!=null && !pettyCashOpeningBalance.equals("")){
			 * newBranchDepBoxKey.setPettyCashOpeningBalance(Double.parseDouble(
			 * pettyCashOpeningBalance));
			 * }else{
			 * newBranchDepBoxKey.setPettyCashOpeningBalance(null);
			 * }
			 * bnchdepboxcrud.save(user, newBranchDepBoxKey, entityManager);
			 * 
			 * if(keyDepositHiddenId != null && !keyDepositHiddenId.equals("")){
			 * //creates cashier user
			 * if (cashierMail != null && !cashierMail.equals("")) {
			 * newBranchDepBoxKey.setCashierName(cashierName);
			 * newBranchDepBoxKey.setCashierPhnNoCountryCode(cashierPhNoCtryCd);
			 * newBranchDepBoxKey.setCashierPhnNo(cashierPhNo);
			 * newBranchDepBoxKey.setCashierEmail(cashierMail);
			 * newBranchDepBoxKey.setCashierKnowledgeLibrary(cashierKL);
			 * bnchdepboxcrud.save(user, newBranchDepBoxKey, entityManager);
			 * 
			 * criterias.clear();
			 * criterias.put("email", cashierMail);
			 * criterias.put("organization.id", org.getId());
			 * Users newCashierDuringUpdate = genericDAO.getByCriteria(Users.class,
			 * criterias, entityManager);
			 * 
			 * criterias.clear();
			 * criterias.put("branchSafeDepositBox.id", Long.parseLong(keyDepositHiddenId));
			 * Users branchCashierUser = genericDAO.getByCriteria(Users.class, criterias,
			 * entityManager);
			 * 
			 * criterias.clear();
			 * criterias.put("id", 8L);
			 * Role role = genericDAO.getByCriteria(Role.class, criterias, entityManager);
			 * 
			 * if (branchCashierUser != null && newCashierDuringUpdate != null) {
			 * branchCashierUser.setFullName(cashierName);
			 * branchCashierUser.setEmail(cashierMail);
			 * branchCashierUser.setOrganization(org);
			 * branchCashierUser.setBranch(newBranch);
			 * branchCashierUser.setBranchSafeDepositBox(newBranchDepBoxKey);
			 * genericDAO.saveOrUpdate(branchCashierUser, usr, entityManager);
			 * auditDAO.
			 * saveAuditLogs("updated cashier user for the branch cash/safe deposit box",
			 * usr, branchCashierUser.getId(), Users.class, ipAddress, json.toString(),
			 * entityManager);
			 * } else {
			 * if (branchCashierUser != null) {
			 * branchCashierUser.setBranchSafeDepositBox(null);
			 * genericDAO.saveOrUpdate(branchCashierUser, usr, entityManager);
			 * }
			 * branchCashierUser = new Users();
			 * branchCashierUser.setFullName(cashierName);
			 * branchCashierUser.setEmail(cashierMail);
			 * branchCashierUser.setOrganization(org);
			 * branchCashierUser.setBranch(newBranch);
			 * branchCashierUser.setBranchSafeDepositBox(newBranchDepBoxKey);
			 * genericDAO.saveOrUpdate(branchCashierUser, usr, entityManager);
			 * String password = PasswordUtil.gen(10);
			 * String body = userAccountCreation.render(branchCashierUser.getEmail(),
			 * password).body();
			 * final String username = ConfigFactory.load().getString("smtp.user");
			 * Session session = emailsession;
			 * String subject = "Successfully Created Cashier Users for Organization " +
			 * org.getName();
			 * mailTimer1(body, username, session, branchCashierUser.getEmail(), null,
			 * subject);
			 * }
			 * criterias.clear();
			 * criterias.put("branchSafeDepositBox.id", Long.parseLong(keyDepositHiddenId));
			 * UsersRoles branchCashierUserRoles =
			 * genericDAO.getByCriteria(UsersRoles.class, criterias, entityManager);
			 * if (branchCashierUserRoles != null) {
			 * branchCashierUserRoles.setRole(role);
			 * branchCashierUserRoles.setUser(branchCashierUser);
			 * branchCashierUserRoles.setOrganization(org);
			 * branchCashierUserRoles.setBranch(newBranch);
			 * branchCashierUserRoles.setBranchSafeDepositBox(newBranchDepBoxKey);
			 * genericDAO.saveOrUpdate(branchCashierUserRoles, usr, entityManager);
			 * auditDAO.
			 * saveAuditLogs("updated cashier user for the branch cash/safe deposit box",
			 * usr, branchCashierUserRoles.getId(), UsersRoles.class, ipAddress,
			 * json.toString(), entityManager);
			 * } else if (branchCashierUserRoles == null) {
			 * branchCashierUserRoles = new UsersRoles();
			 * branchCashierUserRoles.setRole(role);
			 * branchCashierUserRoles.setUser(branchCashierUser);
			 * branchCashierUserRoles.setOrganization(org);
			 * branchCashierUserRoles.setBranch(newBranch);
			 * branchCashierUserRoles.setBranchSafeDepositBox(newBranchDepBoxKey);
			 * genericDAO.saveOrUpdate(branchCashierUserRoles, usr, entityManager);
			 * auditDAO.
			 * saveAuditLogs("Created cashier user roles for the branch cash/safe deposit box"
			 * , usr, branchCashierUserRoles.getId(), UsersRoles.class, ipAddress,
			 * json.toString(), entityManager);
			 * }
			 * }
			 * }else {
			 * //creates cashier user!
			 * if (cashierMail != null && !cashierMail.equals("")) {
			 * newBranchDepBoxKey.setCashierName(cashierName);
			 * newBranchDepBoxKey.setCashierPhnNoCountryCode(cashierPhNoCtryCd);
			 * newBranchDepBoxKey.setCashierPhnNo(cashierPhNo);
			 * newBranchDepBoxKey.setCashierEmail(cashierMail);
			 * newBranchDepBoxKey.setCashierKnowledgeLibrary(cashierKL);
			 * bnchdepboxcrud.save(user, newBranchDepBoxKey, entityManager);
			 * 
			 * Users branchCashierUser = new Users();
			 * UsersRoles branchCashierUserRoles = new UsersRoles();
			 * criterias.clear();
			 * criterias.put("email", cashierMail);
			 * criterias.put("organization.id", org.getId());
			 * Users existingusr = genericDAO.getByCriteria(Users.class, criterias,
			 * entityManager);
			 * if (existingusr != null) {
			 * criterias.clear();
			 * criterias.put("role.id", 8L);
			 * criterias.put("user.id", existingusr.getId());
			 * UsersRoles existingUserRoles = genericDAO.getByCriteria(UsersRoles.class,
			 * criterias, entityManager);
			 * branchCashierUser = existingusr;
			 * branchCashierUserRoles = existingUserRoles;
			 * }
			 * if (branchCashierUserRoles == null) {
			 * branchCashierUserRoles = new UsersRoles();
			 * }
			 * branchCashierUser.setFullName(cashierName);
			 * branchCashierUser.setEmail(cashierMail);
			 * branchCashierUser.setOrganization(org);
			 * branchCashierUser.setBranch(newBranch);
			 * String password = PasswordUtil.gen(10);
			 * branchCashierUser.setPassword(PasswordUtil.encrypt(password));
			 * branchCashierUser.setBranchSafeDepositBox(newBranchDepBoxKey);
			 * genericDAO.saveOrUpdate(branchCashierUser, usr, entityManager);
			 * auditDAO.saveAuditLogs("created cashier user for the branch safe/deposit box"
			 * , usr, branchCashierUser.getId(), Users.class, ipAddress, json.toString(),
			 * entityManager);
			 * criterias.clear();
			 * criterias.put("id", 8L);
			 * Role role = genericDAO.getByCriteria(Role.class, criterias, entityManager);
			 * branchCashierUserRoles.setRole(role);
			 * branchCashierUserRoles.setUser(branchCashierUser);
			 * branchCashierUserRoles.setOrganization(org);
			 * branchCashierUserRoles.setBranch(newBranch);
			 * branchCashierUserRoles.setBranchSafeDepositBox(newBranchDepBoxKey);
			 * genericDAO.saveOrUpdate(branchCashierUserRoles, usr, entityManager);
			 * auditDAO.
			 * saveAuditLogs("created cashier user role for the branch safe/deposit box",
			 * usr, branchCashierUserRoles.getId(), UsersRoles.class, ipAddress,
			 * json.toString(), entityManager);
			 * String body = userAccountCreation.render(branchCashierUser.getEmail(),
			 * password).body();
			 * final String username = ConfigFactory.load().getString("smtp.user");
			 * Session session = emailsession;
			 * String subject = "Successfully Created Cashier Users for Organization " +
			 * org.getName();
			 * mailTimer1(body, username, session, branchCashierUser.getEmail(), null,
			 * subject);
			 * }
			 * }
			 * if(cashierPettyTxnApprReqd!=null && !cashierPettyTxnApprReqd.equals("")){
			 * newBranchDepBoxKey.setPettyCashTxnApprovalRequired(Integer.parseInt(
			 * cashierPettyTxnApprReqd));
			 * if(cashierPettyTxnApprReqd.equals("1")){
			 * if(cashierPettyTxnApprAmtLimit!=null &&
			 * !cashierPettyTxnApprAmtLimit.equals("")){
			 * newBranchDepBoxKey.setApprovalAmountLimit(Double.parseDouble(
			 * cashierPettyTxnApprAmtLimit));
			 * }
			 * }
			 * }
			 */
			// start with branch insurence logic
			BranchInsurance newBnchInsurance = null;
			String bnchInsHiddenIds = json.findPath("bnchInsHiddenIds").asText();
			if (bnchInsHiddenIds == "") {
				bnchInsHiddenIds = ",";
			}
			String bnchInsPolicyType = json.findPath("bnchInsPolicyType").asText();
			String bnchInsPolNum = json.findPath("bnchInsPolNum").asText();
			String bnchInsPolComp = json.findPath("bnchInsPolComp").asText();
			String bnchInsPolicyDoc = json.findPath("bnchInsPolicyDoc").asText();
			String bnchInsPolValidityFm = json.findPath("bnchInsPolValidityFm").asText();
			String bnchInsPolValidityTo = json.findPath("bnchInsPolValidityTo").asText();
			String bnchInsPolYearlyPremium = json.findPath("bnchInsPolYearlyPremium").asText();
			String bnchinsalertforActn = json.findPath("bnchinsalertforActn").asText();
			String bnchinsalertforInfn = json.findPath("bnchinsalertforInfn").asText();
			String bnchInsAltRmrks = json.findPath("bnchInsAltRmrks").asText();
			String bnchInsuranceHidIds[] = bnchInsHiddenIds.substring(0, (bnchInsHiddenIds.length())).split(",");
			String bnchInsurancePolicyType[] = bnchInsPolicyType.substring(0, (bnchInsPolicyType.length())).split(",",
					-1);
			String bnchInsPolNums[] = bnchInsPolNum.substring(0, (bnchInsPolNum.length())).split(",", -1);
			String bnchInsPolCompanies[] = bnchInsPolComp.substring(0, (bnchInsPolComp.length())).split(",", -1);
			String bnchInsurancePolicyDoc[] = bnchInsPolicyDoc.substring(0, (bnchInsPolicyDoc.length())).split(",", -1);
			String bnchInsurancePolicyValidityFm[] = bnchInsPolValidityFm.substring(0, (bnchInsPolValidityFm.length()))
					.split("@", -1);
			String bnchInsurancePolicyValidityTo[] = bnchInsPolValidityTo.substring(0, (bnchInsPolValidityTo.length()))
					.split("@", -1);
			String bnchInsurancePolicyYearlyPremium[] = bnchInsPolYearlyPremium
					.substring(0, (bnchInsPolYearlyPremium.length())).split(",", -1);
			String bnchInsAltForActions[] = bnchinsalertforActn.substring(0, (bnchinsalertforActn.length())).split(",",
					-1);
			String bnchInsAltForInformations[] = bnchinsalertforInfn.substring(0, (bnchinsalertforInfn.length()))
					.split(",", -1);
			String bnchInsAltRemarks[] = bnchInsAltRmrks.substring(0, (bnchInsAltRmrks.length())).split("}", -1);
			for (int i = 0; i < bnchInsuranceHidIds.length; i++) {
				if (bnchInsuranceHidIds[i] != null && !"".equals(bnchInsuranceHidIds[i])) {
					newBnchInsurance = BranchInsurance.findById(IdosUtil.convertStringToLong(bnchInsuranceHidIds[i]));
					if (bnchInsurancePolicyType.length > i) {
						newBnchInsurance.setPolicyType(bnchInsurancePolicyType[i].trim());
					}
					if (bnchInsPolNums.length > i) {
						newBnchInsurance.setPolicyNumber(bnchInsPolNums[i].trim());
					}
					if (bnchInsPolCompanies.length > i) {
						newBnchInsurance.setInsurenceCompany(bnchInsPolCompanies[i].trim());
					}
					/*
					 * if(bnchInsurancePolicyDoc.length>i){
					 * newBnchInsurance.setInsurancePolicyDocUrl(bnchInsurancePolicyDoc[i].trim());
					 * bnchInsPolicyDoc}
					 */
					newBnchInsurance.setInsurancePolicyDocUrl(
							transactionDao.getAndDeleteSupportingDocument(newBnchInsurance.getInsurancePolicyDocUrl(),
									usr.getEmail(), bnchInsPolicyDoc, usr, entityManager));
					if (bnchInsurancePolicyYearlyPremium.length > i) {
						if (!bnchInsurancePolicyYearlyPremium[i].trim().equals("")
								&& bnchInsurancePolicyYearlyPremium[i].trim() != null) {
							newBnchInsurance.setAnnualPremium(
									IdosUtil.convertStringToDouble(bnchInsurancePolicyYearlyPremium[i]));
						}
					}
					if (bnchInsurancePolicyValidityFm.length > i) {
						if (bnchInsurancePolicyValidityFm[i].length() > 2) {
							newBnchInsurance.setPolicyValidFrom(
									mysqldf.parse(mysqldf.format(idosdf.parse(bnchInsurancePolicyValidityFm[i]))));
						}
					}
					if (bnchInsurancePolicyValidityTo.length > i) {
						if (bnchInsurancePolicyValidityTo[i].length() > 2) {
							newBnchInsurance.setPolicyValidTo(
									mysqldf.parse(mysqldf.format(idosdf.parse(bnchInsurancePolicyValidityTo[i]))));
						}
					}
					if (bnchInsAltForActions.length > i) {
						newBnchInsurance.setAlertOfAction(bnchInsAltForActions[i].trim());
					}
					if (bnchInsAltForInformations.length > i) {
						newBnchInsurance.setAlertOfInformation(bnchInsAltForInformations[i].trim());
					}
					if (bnchInsAltRemarks.length > i) {
						newBnchInsurance.setRemarks(bnchInsAltRemarks[i].trim());
					}
					newBnchInsurance.setOrganization(org);
					newBnchInsurance.setBranch(newBranch);
					bnchInscrud.save(usr, newBnchInsurance, entityManager);
					FILE_UPLOAD_SERVICE.updateUploadFileLogs(entityManager, usr,
							newBnchInsurance.getInsurancePolicyDocUrl(), newBranch.getId(),
							IdosConstants.BRANCH_MODULE);
					auditDAO.saveAuditLogs("updated branch insurence", usr, newBnchInsurance.getId(),
							BranchInsurance.class, ipAddress, json.toString(), entityManager);
				}
			}
			for (int i = bnchInsuranceHidIds.length; (i < bnchInsurancePolicyDoc.length || i < bnchInsPolNums.length
					|| i < bnchInsPolCompanies.length || i < bnchInsurancePolicyType.length); i++) {
				newBnchInsurance = new BranchInsurance();
				if ((bnchInsurancePolicyType.length > i && !bnchInsurancePolicyType[i].trim().equals(""))
						|| (bnchInsPolNums.length > i && !bnchInsPolNums[i].trim().equals(""))) {
					if (bnchInsurancePolicyType.length > i) {
						newBnchInsurance.setPolicyType(bnchInsurancePolicyType[i].trim());
					}
					if (bnchInsPolNums.length > i) {
						newBnchInsurance.setPolicyNumber(bnchInsPolNums[i].trim());
					}
					if (bnchInsPolCompanies.length > i) {
						newBnchInsurance.setInsurenceCompany(bnchInsPolCompanies[i].trim());
					}
					/*
					 * if(bnchInsurancePolicyDoc.length>i){
					 * newBnchInsurance.setInsurancePolicyDocUrl(bnchInsurancePolicyDoc[i].trim());
					 * }
					 */
					newBnchInsurance.setInsurancePolicyDocUrl(
							transactionDao.getAndDeleteSupportingDocument(newBnchInsurance.getInsurancePolicyDocUrl(),
									usr.getEmail(), bnchInsPolicyDoc, usr, entityManager));
					if (bnchInsurancePolicyValidityFm.length > i) {
						if (bnchInsurancePolicyValidityFm[i].length() > 2) {
							newBnchInsurance.setPolicyValidFrom(
									mysqldf.parse(mysqldf.format(idosdf.parse(bnchInsurancePolicyValidityFm[i]))));
						}
					}
					if (bnchInsurancePolicyValidityTo.length > i) {
						if (bnchInsurancePolicyValidityTo[i].length() > 2) {
							newBnchInsurance.setPolicyValidTo(
									mysqldf.parse(mysqldf.format(idosdf.parse(bnchInsurancePolicyValidityTo[i]))));
						}
					}
					if (bnchInsurancePolicyYearlyPremium.length > i) {
						if (!bnchInsurancePolicyYearlyPremium[i].trim().equals("")
								&& bnchInsurancePolicyYearlyPremium[i].trim() != null) {
							newBnchInsurance.setAnnualPremium(
									IdosUtil.convertStringToDouble(bnchInsurancePolicyYearlyPremium[i]));
						}
					}
					if (bnchInsAltForActions.length > i) {
						newBnchInsurance.setAlertOfAction(bnchInsAltForActions[i].trim());
					}
					if (bnchInsAltForInformations.length > i) {
						newBnchInsurance.setAlertOfInformation(bnchInsAltForInformations[i].trim());
					}
					if (bnchInsAltRemarks.length > i) {
						newBnchInsurance.setRemarks(bnchInsAltRemarks[i].trim());
					}
					newBnchInsurance.setOrganization(org);
					newBnchInsurance.setBranch(newBranch);
					bnchInscrud.save(usr, newBnchInsurance, entityManager);
					FILE_UPLOAD_SERVICE.updateUploadFileLogs(entityManager, usr, bnchInsPolicyDoc, newBranch.getId(),
							IdosConstants.BRANCH_MODULE);
					auditDAO.saveAuditLogs("created branch insurence", usr, newBnchInsurance.getId(),
							BranchInsurance.class, ipAddress, json.toString(), entityManager);
				}
			}
			// started branch bank account logic
			BranchBankAccounts newBnchBnkAct = null;
			Specifics bankAccountSpcifics = null;
			String branchBankAccountHidId = json.findPath("branchBankAccountHidId").asText();
			if (branchBankAccountHidId == "") {
				branchBankAccountHidId = ",";
			}
			String branchBankAccountBankName = json.findPath("branchBankAccountBankName").asText();
			String branchBankAccountType = json.findPath("branchBankAccountType").asText();
			String branchBankAccountNumber = json.findPath("branchBankAccountNumber").asText();
			String branchBankAccountOpeningBalance = json.findPath("branchBankAccountOpeningBalance").asText();
			String branchBankAccounttAuthSignName = json.findPath("branchBankAccounttAuthSignName").asText();
			String branchBankAccounttAuthSignEmail = json.findPath("branchBankAccounttAuthSignEmail").asText();
			String branchBankAccounttAddress = json.findPath("branchBankAccounttAddress").asText();
			String branchBankAccounttPhnNoCtryCode = json.findPath("branchBankAccounttPhnNoCtryCode").asText();
			String branchBankAccountPhnNo = json.findPath("branchBankAccountPhnNo").asText();
			String branchBankAccountSwiftCode = json.findPath("branchBankAccountSwiftCode").asText();
			String branchBankAccountRoutingNumber = json.findPath("branchBankAccountRoutingNumber").asText();
			String branchBankAccountCheckbookCustody = json.findPath("branchBankAccountCheckbookCustody").asText();
			String branchBankAccountCheckbookCustodyEmail = json.findPath("branchBankAccountCheckbookCustodyEmail")
					.asText();
			String bnchBankAccountHidId[] = branchBankAccountHidId.substring(0, (branchBankAccountHidId.length()))
					.split(",");
			String bnchBankAccountBankName[] = branchBankAccountBankName
					.substring(0, (branchBankAccountBankName.length())).split(",", -1);
			String bnchBankAccountType[] = branchBankAccountType.substring(0, (branchBankAccountType.length()))
					.split(",", -1);
			String bnchBankAccountNumber[] = branchBankAccountNumber.substring(0, (branchBankAccountNumber.length()))
					.split(",", -1);
			String bnchBankOpeningBalance[] = branchBankAccountOpeningBalance
					.substring(0, (branchBankAccountOpeningBalance.length())).split(",", -1);
			String bnchBankAccounttAuthSignName[] = branchBankAccounttAuthSignName
					.substring(0, (branchBankAccounttAuthSignName.length())).split(",", -1);
			String bnchBankAccounttAuthSignEmail[] = branchBankAccounttAuthSignEmail
					.substring(0, (branchBankAccounttAuthSignEmail.length())).split(",", -1);
			String bnchBankAccounttAddress[] = branchBankAccounttAddress
					.substring(0, (branchBankAccounttAddress.length())).split("}", -1);
			String bnchBankAccounttPhnNoCtryCode[] = branchBankAccounttPhnNoCtryCode
					.substring(0, (branchBankAccounttPhnNoCtryCode.length())).split(",", -1);
			String bnchBankAccountPhnNo[] = branchBankAccountPhnNo.substring(0, (branchBankAccountPhnNo.length()))
					.split(",", -1);
			String bnchBankAccountSwiftCode[] = branchBankAccountSwiftCode
					.substring(0, (branchBankAccountSwiftCode.length())).split(",", -1);
			String bnchBankAccountRoutingNumber[] = branchBankAccountRoutingNumber
					.substring(0, (branchBankAccountRoutingNumber.length())).split(",", -1);
			String bnchBankAccountCheckbookCustody[] = branchBankAccountCheckbookCustody
					.substring(0, (branchBankAccountCheckbookCustody.length())).split(",", -1);
			String bnchBankAccountCheckbookCustodyEmail[] = branchBankAccountCheckbookCustodyEmail
					.substring(0, (branchBankAccountCheckbookCustodyEmail.length())).split(",", -1);
			for (int i = 0; i < bnchBankAccountHidId.length; i++) {
				if (bnchBankAccountHidId[i] != null && !"".equals(bnchBankAccountHidId[i])) {
					newBnchBnkAct = BranchBankAccounts.findById(IdosUtil.convertStringToLong(bnchBankAccountHidId[i]));
					criterias.clear();
					criterias.put("branchBankAccounts.id", IdosUtil.convertStringToLong(bnchBankAccountHidId[i]));
					criterias.put("presentStatus", 1);
					bankAccountSpcifics = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
					if (bnchBankAccountBankName.length > i) {
						newBnchBnkAct.setBankName(bnchBankAccountBankName[i].trim());
					}
					if (bnchBankAccountNumber.length > i) {
						newBnchBnkAct.setAccountNumber(bnchBankAccountNumber[i].trim());
					}

					// if(newBnchBnkAct.getOpeningBalance() == null && bnchBankOpeningBalance[i]
					// !=null && !bnchBankOpeningBalance[i].trim().equals("")){
					if (!BranchDepositBoxKey.isCashOrPettyCashInvolve(usr.getOrganization().getId(),
							newBnchBnkAct.getBranch().getId(), "Cash", newBnchBnkAct.getId(), genericDAO, entityManager)
							&& bnchBankOpeningBalance[i] != null && !bnchBankOpeningBalance[i].trim().equals("")) {

						String newsbquery = "select obj from BranchBankAccountBalance obj WHERE obj.branch.id=?1 AND obj.organization.id=?2 and obj.branchBankAccounts.id=?3 and obj.presentStatus=1 ORDER BY obj.date desc";
						ArrayList inparam = new ArrayList(3);
						inparam.add(newBnchBnkAct.getBranch().getId());
						inparam.add(newBnchBnkAct.getOrganization().getId());
						inparam.add(newBnchBnkAct.getId());
						// List<BranchBankAccountBalance> branchBankAccountBalList =
						// genericDAO.executeSimpleQueryWithLimit(newsbquery.toString(),entityManager,1);
						List<BranchBankAccountBalance> branchBankAccountBalList = genericDAO.queryWithParams(newsbquery,
								entityManager, inparam);
						if (branchBankAccountBalList.size() > 0) {
							BranchBankAccountBalance branchBankAccountBal = branchBankAccountBalList.get(0);
							if (branchBankAccountBal.getAmountBalance() != null) {
								Double amountBalanceTmp = branchBankAccountBal.getAmountBalance();
								// remove old
								if (newBnchBnkAct.getOpeningBalance() != null) {
									amountBalanceTmp -= newBnchBnkAct.getOpeningBalance();
								}

								amountBalanceTmp += IdosUtil.convertStringToDouble(bnchBankOpeningBalance[i].trim());
								branchBankAccountBal.setAmountBalance(amountBalanceTmp);

							} else {
								branchBankAccountBal.setAmountBalance(
										IdosUtil.convertStringToDouble(bnchBankOpeningBalance[i].trim()));
							}
							if (branchBankAccountBal.getResultantCash() != null) {
								Double resultantAmountTmp = branchBankAccountBal.getResultantCash();
								// Remove old
								if (newBnchBnkAct.getOpeningBalance() != null) {
									resultantAmountTmp -= newBnchBnkAct.getOpeningBalance();
								}
								resultantAmountTmp += IdosUtil.convertStringToDouble(bnchBankOpeningBalance[i].trim());
								branchBankAccountBal.setResultantCash(resultantAmountTmp);
							} else {
								branchBankAccountBal.setResultantCash(
										IdosUtil.convertStringToDouble(bnchBankOpeningBalance[i].trim()));
							}
							genericDAO.saveOrUpdate(branchBankAccountBal, usr, entityManager);
						}
						// Set after less old
						newBnchBnkAct
								.setOpeningBalance(IdosUtil.convertStringToDouble(bnchBankOpeningBalance[i].trim()));
					}

					if (bnchBankAccounttAuthSignName.length > i) {
						newBnchBnkAct.setAuthorizedSignatoryName(bnchBankAccounttAuthSignName[i].trim());
					}
					if (bnchBankAccounttAuthSignEmail.length > i) {
						newBnchBnkAct.setAuthorizedSignatoryEmail(bnchBankAccounttAuthSignEmail[i].trim());
					}
					if (bnchBankAccounttAddress.length > i) {
						newBnchBnkAct.setBankAddress(bnchBankAccounttAddress[i].trim());
					}
					if (bnchBankAccounttPhnNoCtryCode.length > i) {
						newBnchBnkAct.setBankNumberPhnCtryCode(bnchBankAccounttPhnNoCtryCode[i].trim());
					}
					if (bnchBankAccountPhnNo.length > i) {
						newBnchBnkAct.setPhoneNumber(bnchBankAccountPhnNo[i].trim());
					}
					if (bnchBankAccountSwiftCode.length > i) {
						newBnchBnkAct.setSwiftCode(bnchBankAccountSwiftCode[i]);
					}
					if (bnchBankAccountRoutingNumber.length > i) {
						newBnchBnkAct.setRoutingNumber(bnchBankAccountRoutingNumber[i].trim());
					}
					if (bnchBankAccountCheckbookCustody.length > i) {
						newBnchBnkAct.setCheckBookCustodtName(bnchBankAccountCheckbookCustody[i].trim());
					}
					if (bnchBankAccountCheckbookCustodyEmail.length > i) {
						newBnchBnkAct.setCheckBookCustodyEmail(bnchBankAccountCheckbookCustodyEmail[i].trim());
					}
					newBnchBnkAct.setBranch(newBranch);
					newBnchBnkAct.setOrganization(org);
					newBnchBnkAct.setAccountType(IdosUtil.convertStringToInt(bnchBankAccountType[i]));
					bnchBankActcrud.save(usr, newBnchBnkAct, entityManager);
					auditDAO.saveAuditLogs("update branch bank account details", usr, newBnchBnkAct.getId(),
							BranchBankAccounts.class, ipAddress, json.toString(), entityManager);
				}
			}
			for (int i = bnchBankAccountHidId.length; (i < bnchBankAccountBankName.length
					|| i < bnchBankAccountType.length || i < bnchBankAccountNumber.length); i++) {
				newBnchBnkAct = new BranchBankAccounts();
				if (!bnchBankAccountBankName[i].trim().equals("") || !bnchBankAccountNumber[i].trim().equals("")) {
					bankAccountSpcifics = new Specifics();
					if (bnchBankAccountBankName.length > i) {
						newBnchBnkAct.setBankName(bnchBankAccountBankName[i].trim());
					}
					if (bnchBankAccountNumber.length > i) {
						newBnchBnkAct.setAccountNumber(bnchBankAccountNumber[i].trim());
					}
					if (bnchBankAccounttAuthSignName.length > i) {
						newBnchBnkAct.setAuthorizedSignatoryName(bnchBankAccounttAuthSignName[i].trim());
					}
					if (bnchBankAccounttAuthSignEmail.length > i) {
						newBnchBnkAct.setAuthorizedSignatoryEmail(bnchBankAccounttAuthSignEmail[i].trim());
					}
					if (bnchBankAccounttAddress.length > i) {
						newBnchBnkAct.setBankAddress(bnchBankAccounttAddress[i].trim());
					}
					if (bnchBankAccounttPhnNoCtryCode.length > i) {
						newBnchBnkAct.setBankNumberPhnCtryCode(bnchBankAccounttPhnNoCtryCode[i].trim());
					}
					if (bnchBankAccountPhnNo.length > i) {
						newBnchBnkAct.setPhoneNumber(bnchBankAccountPhnNo[i].trim());
					}
					if (bnchBankAccountSwiftCode.length > i) {
						newBnchBnkAct.setSwiftCode(bnchBankAccountSwiftCode[i].trim());
					}
					if (bnchBankAccountRoutingNumber.length > i) {
						newBnchBnkAct.setRoutingNumber(bnchBankAccountRoutingNumber[i].trim());
					}
					if (bnchBankAccountCheckbookCustody.length > i) {
						newBnchBnkAct.setCheckBookCustodtName(bnchBankAccountCheckbookCustody[i].trim());
					}
					if (bnchBankAccountCheckbookCustodyEmail.length > i) {
						newBnchBnkAct.setCheckBookCustodyEmail(bnchBankAccountCheckbookCustodyEmail[i].trim());
					}
					newBnchBnkAct.setBranch(newBranch);
					newBnchBnkAct.setOrganization(org);
					newBnchBnkAct.setAccountType(IdosUtil.convertStringToInt(bnchBankAccountType[i]));
					bnchBankActcrud.save(usr, newBnchBnkAct, entityManager);
					BranchBankAccountBalance branchBankAccountBal = new BranchBankAccountBalance();
					branchBankAccountBal.setBranchBankAccounts(newBnchBnkAct);
					branchBankAccountBal.setBranch(newBranch);
					branchBankAccountBal.setOrganization(org);
					branchBankAccountBal.setDate(Calendar.getInstance().getTime());

					if (bnchBankOpeningBalance[i] != null && !bnchBankOpeningBalance[i].trim().equals("")) {
						newBnchBnkAct
								.setOpeningBalance(IdosUtil.convertStringToDouble(bnchBankOpeningBalance[i].trim()));
						branchBankAccountBal
								.setAmountBalance(IdosUtil.convertStringToDouble(bnchBankOpeningBalance[i].trim()));
						branchBankAccountBal
								.setResultantCash(IdosUtil.convertStringToDouble(bnchBankOpeningBalance[i].trim()));
					}
					genericDAO.saveOrUpdate(branchBankAccountBal, usr, entityManager);
					genericDAO.saveOrUpdate(newBnchBnkAct, usr, entityManager);
					auditDAO.saveAuditLogs("created branch bank account details", usr, newBnchBnkAct.getId(),
							BranchBankAccounts.class, ipAddress, json.toString(), entityManager);
				}
			}
			// start with branch output tax logic
			String branchTaxHidIds = json.findPath("bnchTaxesHidIds").asText();
			String branchTaxName = json.findPath("bnchTaxesNames").asText();
			String branchTaxRate = json.findPath("bnchTaxesRates").asText();
			String salesTaxOpeningBalance = json.findPath("salesTaxOpeningBalances").asText();
			saveUpdateBranchTaxes(branchTaxHidIds, branchTaxName, branchTaxRate, salesTaxOpeningBalance, 2, usr, json,
					newBranch, org, entityManager, ipAddress);
			// start with branch input tax logic
			String branchInputTaxHidIds = json.findPath("bnchInputTaxesHidIds").asText();
			String branchInputTaxName = json.findPath("bnchInputTaxesNames").asText();
			String branchInputTaxRate = json.findPath("bnchInputTaxesRates").asText();
			String buyingTaxOpeningBalance = json.findPath("buyingTaxOpeningBalances").asText();
			saveUpdateBranchTaxes(branchInputTaxHidIds, branchInputTaxName, branchInputTaxRate, buyingTaxOpeningBalance,
					1, usr, json, newBranch, org, entityManager, ipAddress);

			if (bnchFacility == 1 || bnchFacility == 2) {
				String bnchOff = "";
				List<OrganizationKeyOfficials> branchOfficers = newBranch.getBranchOfficers();
				if (branchOfficers != null) {
					if (branchOfficers.size() > 0) {
						for (OrganizationKeyOfficials bnchOfficers : branchOfficers) {
							if (bnchOfficers.getEmail() != null && !bnchOfficers.equals("")) {
								bnchOff += bnchOfficers.getEmail();
							}
						}
					}
				}
				String body = branchPremiseAlert.render(branchrentRevisionDueOn, bnchName, org.getName(), premiseType,
						bnchOff, rentRevisionDueOnRemarks, ConfigParams.getInstance()).body();
				final String username = ConfigFactory.load().getString("smtp.user");
				Session session = emailsession;
				String subject = org.getName() + "Branch" + bnchName + "Premise Alert";
			}
			String statbody = branchStatutoryAlert
					.render(org.getName(), bnchName, tempStatDeatils, tempStatRegNo, tempStatRegValidTo,
							tempStatAltForActn, tempStatAltForInf, tempstatAlertRemarks, ConfigParams.getInstance())
					.body();
			final String statusername = ConfigFactory.load().getString("smtp.user");
			Session statsession = emailsession;
			String statsubject = org.getName() + " Branch " + bnchName + " Statutory Alert";
			String operrembody = branchOperationalAlert
					.render(org.getName(), bnchName, tempopeRemRequirements, tempopeRemDueOn, tempopeRemAltForAction,
							tempopeRemAltForInformation, tempopeRemAltRemarks, ConfigParams.getInstance())
					.body();
			final String operremusername = ConfigFactory.load().getString("smtp.user");
			Session operremsession = emailsession;
			String operremsubject = org.getName() + " Branch " + bnchName + " Operational remainder Alert";
			String insbody = branchInsurenceAlert.render(org.getName(), bnchName, bnchInsPolicyType, bnchInsPolNum,
					bnchInsPolComp, bnchInsPolYearlyPremium, bnchInsPolValidityTo, bnchinsalertforActn,
					bnchinsalertforInfn, bnchInsAltRmrks, ConfigParams.getInstance()).body();
			final String insusername = ConfigFactory.load().getString("smtp.user");
			Session inssession = emailsession;
			String inssubject = org.getName() + " Branch " + bnchName + " Insurences Alert";
			/* only creator role is allowed as confirmed by Srikanth on 17th March 17 */
			if (branchAdmin != null && !branchAdmin.equals("")) {
				criterias.clear();
				criterias.put("email", branchAdmin);
				criterias.put("presentStatus", 1);
				Users user = genericDAO.getByCriteria(Users.class, criterias, entityManager);
				if (user != null) {
					Role branchAdminRole = Role.findById(9L);
					criterias.clear();
					criterias.put("branch.id", newBranch.getId());
					criterias.put("role.id", branchAdminRole.getId());
					criterias.put("presentStatus", 1);
					UsersRoles usrRoles = genericDAO.getByCriteria(UsersRoles.class, criterias, entityManager);
					if (usrRoles == null) {
						usrRoles = new UsersRoles();
					}
					usrRoles.setUser(user);
					usrRoles.setRole(branchAdminRole);
					usrRoles.setBranch(newBranch);
					usrRoles.setOrganization(org);
					genericDAO.saveOrUpdate(usrRoles, usr, entityManager);
				}
			}
			transaction.commit();
		} catch (Exception ex) {
			throw ex;
		}
		log.log(Level.FINE, ">>>>> End");
		return Results.ok();
	}

	@Transactional
	public Result getChartOfAccount(Request request) {

		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		// EntityTransaction entityTransaction=entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users usrinfo = null;
		try {
			// entityTransaction.begin();
			usrinfo = getUserInfo(request);
			if (usrinfo == null) {
				return unauthorized();
			}
			Map<String, Object> criterias = new HashMap<String, Object>();
			ArrayNode catan = result.putArray("categoryData");
			ArrayNode partan = result.putArray("partData");
			ArrayNode iteman = result.putArray("itemData");
			criterias.put("presentStatus", 1);
			criterias.put("organization.id", usrinfo.getOrganization().getId());
			List<Specifics> itemSpecifics = genericDAO.findByCriteria(Specifics.class, criterias, "id", true,
					entityManager);
			// Organization orgn=usrinfo.getOrganization();

			List<Particulars> catList = Particulars.list(entityManager, usrinfo.getOrganization());
			List<Specifics> itemList = Specifics.list(entityManager, usrinfo.getOrganization());
			if (catList.size() > 0) {
				for (Particulars part : catList) {
					ObjectNode row = Json.newObject();
					row.put("id", part.getId());
					row.put("name", part.getName());
					row.put("accountCode", part.getAccountCode());
					catan.add(row);
					partan.add(row);
				}
			}
			if (itemList.size() > 0) {
				for (Specifics specf : itemList) {
					ObjectNode row = Json.newObject();
					row.put("id", specf.getId());
					row.put("name", specf.getName());
					row.put("topLevelAccountcode", specf.getParticularsId().getAccountCode());
					iteman.add(row);
					catan.add(row);
				}
			}
			// entityTransaction.commit();
			result.put("invoiceSerialNo", usrinfo.getOrganization().getInvoiceSerial() == null ? 1
					: usrinfo.getOrganization().getInvoiceSerial() + 1);
			result.put("proformaSerialNo", usrinfo.getOrganization().getProformaSerial() == null ? 1
					: usrinfo.getOrganization().getProformaSerial() + 1);
			result.put("quotationSerialNo", usrinfo.getOrganization().getQuotationSerial() == null ? 1
					: usrinfo.getOrganization().getQuotationSerial() + 1);
			result.put("receiptSerialNo", usrinfo.getOrganization().getReceiptSerial() == null ? 1
					: usrinfo.getOrganization().getReceiptSerial() + 1);
			result.put("advanceReceiptSerialNo", usrinfo.getOrganization().getAdvanceReceiptSerial() == null ? 1
					: usrinfo.getOrganization().getAdvanceReceiptSerial() + 1);
			result.put("debitNoteCustSerialNo", usrinfo.getOrganization().getDebitNoteCustomerSerial() == null ? 1
					: usrinfo.getOrganization().getDebitNoteCustomerSerial() + 1);
			result.put("creditNoteCustSerialNo", usrinfo.getOrganization().getCreditNoteCustomerSerial() == null ? 1
					: usrinfo.getOrganization().getCreditNoteCustomerSerial() + 1);
			result.put("purchaseOrderSerialNo", usrinfo.getOrganization().getPurchaseOrderSerial() == null ? 1
					: usrinfo.getOrganization().getPurchaseOrderSerial() + 1);
			result.put("refundAdvReceiptSerialNo", usrinfo.getOrganization().getRefundAdvanceReceiptSerial() == null ? 1
					: usrinfo.getOrganization().getRefundAdvanceReceiptSerial() + 1);
			result.put("refundAmtAgainstInvoiceReceiptSerialNo",
					usrinfo.getOrganization().getRefundAmounteReceiptSerial() == null ? 1
							: usrinfo.getOrganization().getRefundAmounteReceiptSerial() + 1);
			result.put("deliveryChallanReceiptSerialNo",
					usrinfo.getOrganization().getDeliveryChallanReceiptSerial() == null ? 1
							: usrinfo.getOrganization().getDeliveryChallanReceiptSerial() + 1);
			result.put("paymentVoucherSerialNo", usrinfo.getOrganization().getPaymentVoucherSerial() == null ? 1
					: usrinfo.getOrganization().getPaymentVoucherSerial() + 1);
			result.put("selfInvoiceSerialNo", usrinfo.getOrganization().getSelfInvoice() == null ? 1
					: usrinfo.getOrganization().getSelfInvoice() + 1);
			result.put("createpurchaseOrderSerialNo",
					usrinfo.getOrganization().getCreatePurchaseOrderSerial() == null ? 1
							: usrinfo.getOrganization().getCreatePurchaseOrderSerial() + 1);

			result.put("invoiceInterval", usrinfo.getOrganization().getInvoiceInterval() == null ? 1
					: usrinfo.getOrganization().getInvoiceInterval());
			result.put("proformaInterval", usrinfo.getOrganization().getProformaInterval() == null ? 1
					: usrinfo.getOrganization().getProformaInterval());
			result.put("quotationInterval", usrinfo.getOrganization().getQuotationInterval() == null ? 1
					: usrinfo.getOrganization().getQuotationInterval());
			result.put("receiptInterval", usrinfo.getOrganization().getReceiptInterval() == null ? 1
					: usrinfo.getOrganization().getReceiptInterval());
			result.put("advanceReceiptInterval", usrinfo.getOrganization().getAdvanceReceiptInterval() == null ? 1
					: usrinfo.getOrganization().getAdvanceReceiptInterval());
			result.put("debitNoteCustInterval", usrinfo.getOrganization().getDebitNoteCustomerInterval() == null ? 1
					: usrinfo.getOrganization().getDebitNoteCustomerInterval());
			result.put("creditNoteCustInterval", usrinfo.getOrganization().getCreditNoteCustomerInterval() == null ? 1
					: usrinfo.getOrganization().getCreditNoteCustomerInterval());
			result.put("purchaseOrderInterval", usrinfo.getOrganization().getPurchaseOrderInterval() == null ? 1
					: usrinfo.getOrganization().getPurchaseOrderInterval());
			result.put("refundAdvReceiptInterval",
					usrinfo.getOrganization().getRefundAdvanceReceiptInterval() == null ? 1
							: usrinfo.getOrganization().getRefundAdvanceReceiptInterval());
			result.put("refundAmtAgainstInvoiceReceiptInterval",
					usrinfo.getOrganization().getRefundAmountReceiptInterval() == null ? 1
							: usrinfo.getOrganization().getRefundAmountReceiptInterval());
			result.put("deliveryChallanReceiptInterval",
					usrinfo.getOrganization().getDeliverChallanReceiptInterval() == null ? 1
							: usrinfo.getOrganization().getDeliverChallanReceiptInterval());
			result.put("paymentVoucherInterval", usrinfo.getOrganization().getPaymentVoucherInterval() == null ? 1
					: usrinfo.getOrganization().getPaymentVoucherInterval());
			result.put("selfInvoiceInterval", usrinfo.getOrganization().getSelfInvoiceInterval() == null ? 1
					: usrinfo.getOrganization().getSelfInvoiceInterval());
			result.put("createpurchaseOrderInterval",
					usrinfo.getOrganization().getCreatePurchaseOrderInterval() == null ? 1
							: usrinfo.getOrganization().getCreatePurchaseOrderInterval());
			result.put("serialNoCategory", usrinfo.getOrganization().getOrgSerialGenrationType() == null ? 1
					: usrinfo.getOrganization().getOrgSerialGenrationType());
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usrinfo.getEmail(), usrinfo.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	/**
	 * To savve or update tax for the branch
	 * 
	 * @param branchTaxHidIds
	 * @param branchTaxName
	 * @param branchTaxRate
	 * @param taxType
	 * @param usr
	 * @param json
	 * @param newBranch
	 * @param entityManager
	 * @param ipAddress
	 * @throws IDOSException
	 */
	private static void saveUpdateBranchTaxes(String branchTaxHidIds, String branchTaxName, String branchTaxRate,
			String taxOpeningBalance,
			int taxType, Users usr, JsonNode json, Branch newBranch, Organization org, EntityManager entityManager,
			String ipAddress) throws IDOSException {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> Start branchTaxHidIds: " + branchTaxHidIds + " branchTaxName: " + branchTaxName
					+ " taxType: " + taxType + " branchTaxRate: " + branchTaxRate);

		// start with branch tax logic
		if ("".equals(branchTaxHidIds)) {
			branchTaxHidIds = ",";
		}
		BranchTaxes branchTax = null;
		String taxArrHidIds[] = branchTaxHidIds.substring(0, (branchTaxHidIds.length())).split(",");
		String branchTaxNames[] = branchTaxName.substring(0, (branchTaxName.length())).split(",", -1);
		String branchTaxRates[] = branchTaxRate.substring(0, (branchTaxRate.length())).split(",", -1);
		String salesTaxOpeningBalances[] = null;
		if (taxOpeningBalance != null && !"".equals(taxOpeningBalance)) {
			salesTaxOpeningBalances = taxOpeningBalance.substring(0, (taxOpeningBalance.length())).split(",", -1);
		}
		String strMsg = null;
		if (taxType == 1) {
			strMsg = "updated branch input tax details";
		} else {
			strMsg = "updated branch output tax details";
		}

		for (int i = 0; i < taxArrHidIds.length; i++) {
			if (taxArrHidIds[i] != null && !"".equals(taxArrHidIds[i].trim())) {
				branchTax = BranchTaxes.findById(IdosUtil.convertStringToLong(taxArrHidIds[i]));
				if (branchTaxNames.length > i) {
					branchTax.setTaxName(branchTaxNames[i].trim());
				}
				if (branchTaxRates.length > i) {
					if (!branchTaxRates[i].trim().equals("") && branchTaxRates[i].trim() != null) {
						branchTax.setTaxRate(IdosUtil.convertStringToDouble(branchTaxRates[i].trim()));
					}
				}
				if (salesTaxOpeningBalances != null && salesTaxOpeningBalances.length > i) {
					if (!salesTaxOpeningBalances[i].trim().equals("") && salesTaxOpeningBalances[i].trim() != null) {
						branchTax.setOpeningBalance(IdosUtil.convertStringToDouble(salesTaxOpeningBalances[i].trim()));
					}
				}
				branchTax.setOrganization(org);
				branchTax.setBranch(newBranch);
				branchTax.setTaxType(taxType);
				genericDAO.saveOrUpdate(branchTax, usr, entityManager);
				auditDAO.saveAuditLogs("updated branch tax details", usr, branchTax.getId(), TaxBranches.class,
						ipAddress, json.toString(), entityManager);
			}
		}
		if (taxType == 1) {
			strMsg = "CREATED branch input tax details";
		} else {
			strMsg = "CREATED branch output tax details";
		}
		for (int i = taxArrHidIds.length; (i < branchTaxNames.length || i < branchTaxRates.length); i++) {
			branchTax = new BranchTaxes();
			if (!branchTaxNames[i].trim().equals("") || !branchTaxRates[i].trim().equals("")) {
				if (branchTaxNames.length > i) {
					branchTax.setTaxName(branchTaxNames[i].trim());
				}
				if (branchTaxRates.length > i) {
					if (!branchTaxRates[i].trim().equals("") && branchTaxRates[i].trim() != null) {
						branchTax.setTaxRate(IdosUtil.convertStringToDouble(branchTaxRates[i].trim()));
					}
				}
				if (salesTaxOpeningBalances != null && salesTaxOpeningBalances.length > i) {
					if (!salesTaxOpeningBalances[i].trim().equals("") && salesTaxOpeningBalances[i].trim() != null) {
						branchTax.setOpeningBalance(IdosUtil.convertStringToDouble(salesTaxOpeningBalances[i].trim()));
					}
				}
				branchTax.setOrganization(org);
				branchTax.setBranch(newBranch);
				branchTax.setTaxType(taxType);
				genericDAO.saveOrUpdate(branchTax, usr, entityManager);
				auditDAO.saveAuditLogs(strMsg, usr, branchTax.getId(), TaxBranches.class, ipAddress, json.toString(),
						entityManager);
			}
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> End");
	}

	@Transactional
	public Result getCountryCurrencyData(Request request) {
		log.log(Level.FINE, ">>>> Start");
		System.out.println(">>>>>><<<<<<>>>>>>>>>>>>>>>>>>>>getcountrycurencyData" + entityManager);
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode phonecodean = result.putArray("phoneCodeData");
		ArrayNode currencyAndCountryData = result.putArray("currencyAndCountryData");
		Users usrinfo = null;
		String userEmail = null;
		String orgaName = null;
		try {
			usrinfo = getUserInfo(request);
			System.out.println(">>>>>><<<<<<>>>>>>>>>>>>>>>>>>>>" + usrinfo);
			if (usrinfo == null) {
				return unauthorized();
			}

			Map<String, Object> criterias = new HashMap<String, Object>(1);
			criterias.put("presentStatus", 1);
			List<CountryPhoneCode> countryTelephoneCodes = genericDAO.findByCriteria(CountryPhoneCode.class, criterias,
					"countryWithCode", false, entityManager);
			for (CountryPhoneCode countryCode : countryTelephoneCodes) {
				ObjectNode row = Json.newObject();
				row.put("id", countryCode.getAreaCode());
				row.put("name", countryCode.getCountryWithCode());
				phonecodean.add(row);
			}
			List<IDOSCountry> findAll = IDOSCountry.findAll();
			for (IDOSCountry idosCountry : findAll) {
				ObjectNode row = Json.newObject();
				row.put("id", idosCountry.getId().toString());
				row.put("name", idosCountry.getName());
				row.put("currency", idosCountry.getName() + " ==> (" + idosCountry.getCurrencyCode() + ")("
						+ idosCountry.getCurrencySymbol() + ")");
				currencyAndCountryData.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, userEmail, ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, userEmail, orgaName,
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		} finally {
			// EntityManagerProvider.close();
		}
		log.log(Level.FINE, ">>>> End");
		return Results.ok(result);
	}

	@Transactional
	public Result validateBankCashAccount(Request request) {
		Users user = getUserInfo(request);
		if (user == null) {
			return unauthorized();
		}
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			String type = json.findValue("type").asText() == "" ? null : json.findValue("type").asText();
			String accountId = json.findValue("accountId").asText() == "" ? null : json.findValue("accountId").asText();
			String branchId = json.findValue("branchId").asText() == "" ? null : json.findValue("branchId").asText();
			if (branchId != null && type != null && accountId != null && !"".equals(type) && !"".equals(accountId)
					&& !"".equals(branchId)) {
				if (type.equals("Cash") || type.equals("PettyCash")) {
					boolean cashOrPettyCashInvolve = BranchDepositBoxKey.isCashOrPettyCashInvolve(
							user.getOrganization().getId(), IdosUtil.convertStringToLong(branchId), type,
							IdosUtil.convertStringToLong(accountId), genericDAO, entityManager);
					result.put("status", cashOrPettyCashInvolve);
				} else if (type.equals("Bank")) {
					boolean bankAccountInvolve = BranchBankAccounts.isBankAccountInvolve(user.getOrganization().getId(),
							IdosUtil.convertStringToLong(branchId), type, IdosUtil.convertStringToLong(accountId),
							genericDAO, entityManager);
					result.put("status", bankAccountInvolve);
				} else {
					result.put("status", false);
				}
			} else {
				result.put("status", false);
			}

		} catch (Throwable ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			result.put("status", "failed");
		}
		return Results.ok(result);
	}

	@Transactional
	public static boolean correctCountriesWithNewIDS() {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, ">>>>>>>>>>>>> Started Countries Updation <<<<<<<<<<<<<<<<<<<");
		}
		// EntityManager em = getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		PrintWriter writer = null;
		try {
			File file = new File("/home/user/IDOS/DATA_QUERY.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			writer = new PrintWriter(new File("/home/user/IDOS/DATA_QUERY.txt"));
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error", e);
		}
		try {
			Map<String, String> countriesOLD = CountryCurrencyUtil.getOLD();
			List<IDOSCountry> newCountries = IDOSCountry.findAll();
			for (String key : countriesOLD.keySet()) {
				for (IDOSCountry idosCountry : newCountries) {
					if (countriesOLD.get(key).toUpperCase().equals(idosCountry.getName().toUpperCase())) {
						int oldId = Integer.parseInt(key);
						int newId = idosCountry.getId().intValue();
						log.log(Level.FINE, ">>> " + idosCountry.getName() + " >>>" + oldId + ">>>>" + newId);
						// log.log(Level.FINE, ">>>>>>>>>>>>> Started ORGANIZATION Updation
						// <<<<<<<<<<<<<<<<<<<");

						String ORG_COUNTRIES_SQL = "UPDATE ORGANIZATION SET COUNTRY = " + newId + ", CURRENCY = '"
								+ newId + "' ,MODIFIED_AT = now() where COUNTRY = " + oldId
								+ " and MODIFIED_AT < NOW() - INTERVAL 15 MINUTE;";
						String BRANCH_COUNTRIES_SQL = "UPDATE BRANCH SET COUNTRY = " + newId + ", CURRENCY = '" + newId
								+ "' ,MODIFIED_AT = now()  where COUNTRY = " + oldId
								+ " and MODIFIED_AT < NOW() - INTERVAL 15 MINUTE;";
						String CUST_COUNTRIES_SQL_1 = "UPDATE CUSTOMER_DETAIL SET SHIPPING_COUNTRY = " + newId
								+ ",MODIFIED_AT = now() where SHIPPING_COUNTRY = " + oldId
								+ " and MODIFIED_AT < NOW() - INTERVAL 15 MINUTE;";
						String CUST_COUNTRIES_SQL_2 = "UPDATE CUSTOMER_DETAIL SET BILLING_COUNTRY = " + newId
								+ ",MODIFIED_AT = now() where BILLING_COUNTRY = " + oldId
								+ " and MODIFIED_AT < NOW() - INTERVAL 15 MINUTE;";
						String CUST_WALK_COUNTRIES_SQL_1 = "UPDATE CUSTOMER_WALKIN_DETAIL SET SHIPPING_COUNTRY = "
								+ newId + ",MODIFIED_AT = now() where SHIPPING_COUNTRY = " + oldId
								+ " and MODIFIED_AT < NOW() - INTERVAL 15 MINUTE;";
						String CUST_WALK_COUNTRIES_SQL_2 = "UPDATE CUSTOMER_WALKIN_DETAIL SET BILLING_COUNTRY = "
								+ newId + ",MODIFIED_AT = now() where BILLING_COUNTRY = " + oldId
								+ " and MODIFIED_AT < NOW() - INTERVAL 15 MINUTE;";
						String VEND_COUNTRIES_SQL = "UPDATE VENDOR SET COUNTRY = " + newId
								+ ",MODIFIED_AT = now() where COUNTRY = " + oldId
								+ " and MODIFIED_AT < NOW() - INTERVAL 15 MINUTE;";
						String VEND_DETAILS_COUNTRIES_SQL = "UPDATE VENDOR_DETAIL SET COUNTRY = " + newId
								+ ",MODIFIED_AT = now()  where COUNTRY = " + oldId
								+ " and MODIFIED_AT < NOW() - INTERVAL 15 MINUTE;";
						String PROJECT_COUNTRIES_SQL = "UPDATE PROJECT SET COUNTRY = " + newId
								+ ",MODIFIED_AT = now()   where COUNTRY = " + oldId
								+ " and MODIFIED_AT < NOW() - INTERVAL 15 MINUTE;";
						writer.println(ORG_COUNTRIES_SQL);
						writer.println(BRANCH_COUNTRIES_SQL);
						writer.println(CUST_COUNTRIES_SQL_1);
						writer.println(CUST_COUNTRIES_SQL_2);
						writer.println(CUST_WALK_COUNTRIES_SQL_1);
						writer.println(CUST_WALK_COUNTRIES_SQL_2);
						writer.println(VEND_COUNTRIES_SQL);
						writer.println(VEND_DETAILS_COUNTRIES_SQL);
						writer.println(PROJECT_COUNTRIES_SQL);
					}
				}
			}

		} catch (Throwable ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
		}
		writer.close();
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, ">>>>>>>>>>>>> Ended Countries Updation <<<<<<<<<<<<<<<<<<<");
		}
		return true;
	}

}
