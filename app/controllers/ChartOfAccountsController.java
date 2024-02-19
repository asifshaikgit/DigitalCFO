package controllers;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.Session;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.logging.Level;
import model.*;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.Http.Request;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.libs.Files.TemporaryFile;
import views.html.errorPage;
import play.Application;
import com.idos.util.AccountCodeUtil;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import com.idos.util.ListUtility;
import javax.inject.Inject;

public class ChartOfAccountsController extends StaticController {
	public static Application application;
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	public Request request;

	@Inject
	public ChartOfAccountsController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result uploadChartOfAccount(int selectedCoaUploadType, Request request) {
		log.log(Level.FINE, ">>>> Start " + selectedCoaUploadType);
		ObjectNode result = Json.newObject();
		if (selectedCoaUploadType < 5) {
			return uploadChartOfAccountTemplate(selectedCoaUploadType, request);
		} else {
			// EntityManager entityManager = getEntityManager();
			EntityTransaction transaction = entityManager.getTransaction();

			Users user = null;
			ArrayNode an = result.putArray("successUploading");
			try {
				transaction.begin();
				Http.MultipartFormData<File> body = request.body().asMultipartFormData();
				user = getUserInfo(request);
				List<Http.MultipartFormData.FilePart<File>> chartofaccount = body.getFiles();
				for (Http.MultipartFormData.FilePart<File> filePart : chartofaccount) {
					String fileName = filePart.getFilename();
					final Session session = mailSession();
					final String subject = "Chart Of Account Upload By " + user.getEmail() + " who belongs to Company "
							+ user.getOrganization().getName();
					String contentType = filePart.getContentType();
					//File file = filePart.getRef();
					TemporaryFile temporaryFile = (TemporaryFile) filePart.getRef();
					String filePath = temporaryFile.path().toString();
					File file = new File(filePath);
					InputStream is = new java.io.FileInputStream(file);
					try {
						XSSFWorkbook wb = new XSSFWorkbook(is);
						int numOfSheets = wb.getNumberOfSheets();
						for (int i = 0; i < numOfSheets; i++) {
							XSSFSheet sheet = wb.getSheetAt(i);
							Iterator rows = sheet.rowIterator();
							while (rows.hasNext()) {
								XSSFRow row = (XSSFRow) rows.next();
								Iterator cellIter = row.cellIterator();
								String prevCellValue = null;
								while (cellIter.hasNext()) {
									XSSFCell myCell = (XSSFCell) cellIter.next();
									String cellValue = myCell.toString();
									// look into both particular and specifics table for cell value
									// if found in any one of the both particular or specifics table then no
									// transaction
									StringBuilder sbquery = new StringBuilder("");
									cellValue = cellValue.replaceAll("\\'", "");
									if (cellValue == null || "".equals(cellValue.trim())) {
										continue;
									}
									sbquery.append("select obj from Particulars obj WHERE UPPER(obj.name) LIKE UPPER('"
											+ cellValue + "%') and obj.organization.id='"
											+ user.getOrganization().getId() + "' and obj.presentStatus=1");
									List<Particulars> particulars = genericDAO.executeSimpleQuery(sbquery.toString(),
											entityManager);
									if (particulars.size() > 0) {
										prevCellValue = cellValue;
									}
									StringBuilder specsbquery = new StringBuilder("");
									// cellValue = cellValue.replaceAll("\\'", "");
									// log.log(Level.INFO, cellValue);
									specsbquery.append("select obj from Specifics obj WHERE UPPER(obj.name)=UPPER('"
											+ cellValue + "') and obj.organization.id='"
											+ user.getOrganization().getId() + "' and obj.presentStatus=1");
									List<Specifics> specifics = genericDAO.executeSimpleQuery(specsbquery.toString(),
											entityManager);
									if (specifics.size() > 0) {
										prevCellValue = cellValue;
									}
									// if not found in any of the two particular or specifics table
									if (!(particulars.size() > 0) && !(specifics.size() > 0)) {
										Long parentActCode = null;
										Long maxActCode = null;
										String hirarchy = null;
										Specifics newSpecf = new Specifics();
										StringBuilder nfsbquery = new StringBuilder("");
										if (prevCellValue != null) {
											prevCellValue = prevCellValue.replaceAll("\\'", "");
										}
										nfsbquery.append(
												"select obj from Particulars obj WHERE UPPER(obj.name) LIKE UPPER('"
														+ prevCellValue + "%') and obj.organization.id='"
														+ user.getOrganization().getId() + "' and obj.presentStatus=1");
										List<Particulars> nfparticulars = genericDAO
												.executeSimpleQuery(nfsbquery.toString(), entityManager);
										if (nfparticulars.size() > 0) {
											parentActCode = nfparticulars.get(0).getAccountCode();
											StringBuilder sbquerys = new StringBuilder("");
											sbquerys.append(
													"select MAX(obj.accountCode) from Specifics obj where obj.particularsId.id='"
															+ nfparticulars.get(0).getId()
															+ "' and obj.parentSpecifics IS NULL and organization.id='"
															+ user.getOrganization().getId()
															+ "' and obj.presentStatus=1");
											List maxSpecfObj = genericDAO.executeSimpleQuery(sbquerys.toString(),
													entityManager);
											if (maxSpecfObj.get(0) != null) {
												maxActCode = (Long) maxSpecfObj.get(0);
											} else {
												maxActCode = null;
											}
											hirarchy = nfparticulars.get(0).getAccountCodeHirarchy()
													+ nfparticulars.get(0).getAccountCode() + "/";
											newSpecf.setParticularsId(nfparticulars.get(0));
											newSpecf.setParentSpecifics(null);
										}
										StringBuilder nfspecsbquery = new StringBuilder("");
										if (prevCellValue != null) {
											prevCellValue = prevCellValue.replaceAll("\\'", "");
										}
										nfspecsbquery
												.append("select obj from Specifics obj WHERE UPPER(obj.name)=UPPER('"
														+ prevCellValue + "') and obj.organization.id='"
														+ user.getOrganization().getId() + "' and obj.presentStatus=1");
										List<Specifics> nfspecifics = genericDAO
												.executeSimpleQuery(nfspecsbquery.toString(), entityManager);
										if (nfspecifics.size() > 0) {
											parentActCode = nfspecifics.get(0).getAccountCode();
											StringBuilder sbqueries = new StringBuilder("");
											sbqueries.append(
													"select MAX(obj.accountCode) from Specifics obj where obj.parentSpecifics.id='"
															+ nfspecifics.get(0).getId() + "' and organization.id='"
															+ user.getOrganization().getId()
															+ "' and obj.presentStatus=1");
											List maxSpecfObj = genericDAO.executeSimpleQuery(sbqueries.toString(),
													entityManager);
											if (maxSpecfObj.get(0) != null) {
												maxActCode = (Long) maxSpecfObj.get(0);
											} else {
												maxActCode = null;
											}
											hirarchy = nfspecifics.get(0).getAccountCodeHirarchy()
													+ nfspecifics.get(0).getAccountCode() + "/";
											newSpecf.setParticularsId(nfspecifics.get(0).getParticularsId());
											newSpecf.setParentSpecifics(nfspecifics.get(0));
										}
										//
										if ((nfparticulars.size() > 0) || (nfspecifics.size() > 0)) {
											Long actCode = AccountCodeUtil.generateAccountCode(parentActCode,
													maxActCode);
											Map<String, Object> criterias = new HashMap<String, Object>();
											criterias.put("accountCode", actCode);
											criterias.put("organization.id", user.getOrganization().getId());
											criterias.put("presentStatus", 1);
											Specifics foundSpecf = genericDAO.getByCriteria(Specifics.class, criterias,
													entityManager);
											if (foundSpecf != null) {
												actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
											}
											String aCode = String.valueOf(actCode);
											int length = aCode.length();
											int pos = aCode.indexOf("9");
											int itrate = 0;
											String one = "1";
											if (pos != -1) {
												itrate = length - pos - 3;
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
											criterias.clear();
											criterias.put("accountCode", actCode);
											criterias.put("organization.id", user.getOrganization().getId());
											criterias.put("presentStatus", 1);
											Specifics againfoundSpecf = genericDAO.getByCriteria(Specifics.class,
													criterias, entityManager);
											if (againfoundSpecf != null) {
												actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
											}
											criterias.clear();
											criterias.put("accountCode", actCode);
											criterias.put("organization.id", user.getOrganization().getId());
											criterias.put("presentStatus", 1);
											Specifics againfoundSpecf1 = genericDAO.getByCriteria(Specifics.class,
													criterias, entityManager);
											if (againfoundSpecf1 != null) {
												actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
											}
											criterias.clear();
											criterias.put("accountCode", actCode);
											criterias.put("organization.id", user.getOrganization().getId());
											criterias.put("presentStatus", 1);
											Specifics againfoundSpecf2 = genericDAO.getByCriteria(Specifics.class,
													criterias, entityManager);
											if (againfoundSpecf2 != null) {
												actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
											}
											criterias.clear();
											criterias.put("accountCode", actCode);
											criterias.put("organization.id", user.getOrganization().getId());
											criterias.put("presentStatus", 1);
											Specifics againfoundSpecf3 = genericDAO.getByCriteria(Specifics.class,
													criterias, entityManager);
											if (againfoundSpecf3 != null) {
												actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
											}
											criterias.clear();
											criterias.put("accountCode", actCode);
											criterias.put("organization.id", user.getOrganization().getId());
											criterias.put("presentStatus", 1);
											Specifics againfoundSpecf4 = genericDAO.getByCriteria(Specifics.class,
													criterias, entityManager);
											if (againfoundSpecf4 != null) {
												actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
											}
											criterias.clear();
											criterias.put("accountCode", actCode);
											criterias.put("organization.id", user.getOrganization().getId());
											criterias.put("presentStatus", 1);
											Specifics againfoundSpecf5 = genericDAO.getByCriteria(Specifics.class,
													criterias, entityManager);
											if (againfoundSpecf5 != null) {
												actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
											}
											criterias.clear();
											criterias.put("accountCode", actCode);
											criterias.put("organization.id", user.getOrganization().getId());
											criterias.put("presentStatus", 1);
											Specifics againfoundSpecf6 = genericDAO.getByCriteria(Specifics.class,
													criterias, entityManager);
											if (againfoundSpecf6 != null) {
												actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
											}
											criterias.clear();
											criterias.put("accountCode", actCode);
											criterias.put("organization.id", user.getOrganization().getId());
											criterias.put("presentStatus", 1);
											Specifics againfoundSpecf7 = genericDAO.getByCriteria(Specifics.class,
													criterias, entityManager);
											if (againfoundSpecf7 != null) {
												actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
											}
											criterias.clear();
											criterias.put("accountCode", actCode);
											criterias.put("organization.id", user.getOrganization().getId());
											criterias.put("presentStatus", 1);
											Specifics againfoundSpecf8 = genericDAO.getByCriteria(Specifics.class,
													criterias, entityManager);
											if (againfoundSpecf8 != null) {
												actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
											}
											criterias.clear();
											criterias.put("accountCode", actCode);
											criterias.put("organization.id", user.getOrganization().getId());
											criterias.put("presentStatus", 1);
											Specifics againfoundSpecf9 = genericDAO.getByCriteria(Specifics.class,
													criterias, entityManager);
											if (againfoundSpecf9 != null) {
												actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
											}
											criterias.clear();
											criterias.put("accountCode", actCode);
											criterias.put("organization.id", user.getOrganization().getId());
											criterias.put("presentStatus", 1);
											Specifics againfoundSpecf10 = genericDAO.getByCriteria(Specifics.class,
													criterias, entityManager);
											if (againfoundSpecf10 != null) {
												actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
											}
											newSpecf.setAccountCode(actCode);
											newSpecf.setAccountCodeHirarchy(hirarchy);
											newSpecf.setName(cellValue);
											newSpecf.setOrganization(user.getOrganization());
											genericDAO.saveOrUpdate(newSpecf, user, entityManager);
											// Single User
											if (ConfigParams.getInstance().isDeploymentSingleUser(user)) {
												// For Single User Deployment Only
												if (newSpecf.getModifiedAt() == null) {
													singleUserService.updateOnCOACreation(user, newSpecf,
															entityManager);
												}
											}
											prevCellValue = cellValue;
										}
									}
								}
							}
						}
					} catch (Exception ex) {
						log.log(Level.SEVERE, "Error", ex);
					}
					if (user != null) {
						user.getOrganization().setChartOfAccountUploadStatus(1);
					}
					sendMailWithAttachment(session, subject, fileName, file, "allusers@myidos.com",
							"alerts@myidos.com");
				}
				ObjectNode row = Json.newObject();
				row.put("message", "Uploaded Successfully");
				an.add(row);
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
		}
		return Results.ok(result);
	}

	@Transactional
	public Result uploadChartOfAccountTemplate(int selectedCoaUploadType, Request request) {
		log.log(Level.FINE, ">>>>>>> Start");
		// EntityManager entityManager = getEntityManager();
		Users user = null;
		ObjectNode result = Json.newObject();
		ArrayNode an = result.putArray("successUploading");
		DataFormatter df = new DataFormatter();

		int batchSize = 25;
		int rowCount = 0;
		int lastrownum = 0;
		long totalRowsInserted = 0;
		String uploadIssue = "";
		try {
			entityManager.getTransaction().begin();
			Http.MultipartFormData<File> body = request.body().asMultipartFormData();
			user = getUserInfo(request);
			List<Http.MultipartFormData.FilePart<File>> chartofaccount = body.getFiles();
			for (Http.MultipartFormData.FilePart<File> filePart : chartofaccount) {
				String fileName = filePart.getFilename();
				final Session session = mailSession();
				final String subject = "Chart Of Account " + selectedCoaUploadType + " Upload By " + user.getEmail()
						+ " who belongs to Company " + user.getOrganization().getName();
				String contentType = filePart.getContentType();
				//File file = filePart.getRef();
				TemporaryFile temporaryFile = (TemporaryFile) filePart.getRef();
				String filePath = temporaryFile.path().toString();
				File file = new File(filePath);
				InputStream is = new java.io.FileInputStream(file);
				try {
					XSSFWorkbook workbook = new XSSFWorkbook(is);
					int numOfSheets = workbook.getNumberOfSheets();
					log.log(Level.FINE, "No of sheets: " + numOfSheets);
					for (int i = 0; i < numOfSheets; i++) {
						XSSFSheet sheet = workbook.getSheetAt(i);
						log.log(Level.FINE, "Name of sheet: " + sheet.getSheetName());
						if (!"Assets".equalsIgnoreCase(sheet.getSheetName())
								&& !"Liabilities".equalsIgnoreCase(sheet.getSheetName())
								&& !"Income".equalsIgnoreCase(sheet.getSheetName())
								&& !"Expenses".equalsIgnoreCase(sheet.getSheetName())) {
							continue;
						}
						lastrownum = sheet.getLastRowNum();
						result.put("totalRowsInXls", lastrownum);
						Iterator rows = sheet.rowIterator();
						while (rows.hasNext()) {
							rowCount++;
							if (rowCount > 0 && rowCount % batchSize == 0) { // batch commit of 25
								entityManager.flush();
								entityManager.clear();
								entityManager.getTransaction().commit();
								entityManager.getTransaction().begin();
								rowCount = 0;
							}
							XSSFRow row = (XSSFRow) rows.next();
							if (row.getRowNum() == 0) {
								String row6ColHead = row.getCell(6) == null ? null
										: ((XSSFCell) row.getCell(6)).toString();
								if (selectedCoaUploadType == 1
										&& !row6ColHead.equalsIgnoreCase("Identification for Data Validation")) {
									uploadIssue = "Though it is called Income template, columns are NOT matching to that of INCOME templates.";
									break;
								} else if (selectedCoaUploadType == 2
										&& !row6ColHead.equalsIgnoreCase("Inventory Value")) {
									uploadIssue = "Though it is called Expense template, columns are NOT matching to that of EXPENSE templates.";
									break;
								} else if ((selectedCoaUploadType == 3 || selectedCoaUploadType == 4)
										&& !row6ColHead.equalsIgnoreCase("Is Transaction Editable?")) {
									uploadIssue = "Though it is called Assets/Liabilities template, columns are NOT matching to that templates.";
									break;
									// throw new IDOSException(IdosConstants.RECORD_NOT_FOUND,
									// IdosConstants.BUSINESS_EXCEPTION, uploadIssue,
									// IdosConstants.RECORD_NOT_FOUND);
								}
								continue;
							}
							String accountName = row.getCell(0) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(0)));
							// String phoneNo = row.getCell(15) == null ? null :
							// df.formatCellValue(((XSSFCell) row.getCell(15)));
							if (accountName != null) {
								accountName = accountName.replaceAll("\\'", "");
							}
							if (accountName == null || "".equals(accountName.trim())) {
								continue;
							}

							String subAccountName = row.getCell(1) == null ? null
									: df.formatCellValue(((XSSFCell) row.getCell(1)));
							// String phoneNo = row.getCell(15) == null ? null :
							// df.formatCellValue(((XSSFCell) row.getCell(15)));
							log.log(Level.INFO, "sub account name=" + subAccountName);
							if (subAccountName != null) {
								subAccountName = subAccountName.replaceAll("\\'", "");
							}
							/*
							 * if(subAccountName == null){ //it means that it has no child accounts e.g. in
							 * Income xls- acctName=sales,subacctName=null is possible
							 * continue;
							 * }
							 */
							Map<String, Object> criterias = new HashMap<String, Object>();
							String escapedAccName = IdosUtil.escapeHtml(accountName); // In db Housing & BF stored as
																						// Hosing &amp; BF as all
																						// entries in DB for name are
																						// esacped using
																						// Org.setName(),so to do
																						// comparison we too need to
																						// escape all names
							criterias.put("name", escapedAccName);
							criterias.put("organization.id", user.getOrganization().getId());
							criterias.put("presentStatus", 1);
							Specifics parentSpecifics = genericDAO.getByCriteria(Specifics.class, criterias,
									entityManager);
							// check if item already exist ie. subAccountName already present
							if (parentSpecifics != null) {
								Long parentSpecificsActCode = parentSpecifics.getAccountCode();
								String escapedSubAccName = IdosUtil.escapeHtml(subAccountName); // In db Housing & BF
																								// stored as Hosing
																								// &amp; BF as all
																								// entries in DB for
																								// name are esacped
																								// using
																								// Org.setName(),so to
																								// do comparison we too
																								// need to escape all
																								// names
								criterias.clear();
								criterias.put("name", escapedSubAccName);
								criterias.put("organization.id", user.getOrganization().getId());
								criterias.put("particularsId.id", parentSpecifics.getParticularsId().getId()); // check
																												// if
																												// subaccount
																												// present
																												// in
																												// same
																												// income
																												// category
								criterias.put("presentStatus", 1);
								Specifics itemSpecifics = genericDAO.getByCriteria(Specifics.class, criterias,
										entityManager);
								if (itemSpecifics != null) { // it means item already present, so don't insert, go to
																// next item
									continue;
								}
							}
							totalRowsInserted++;
							String branchList = row.getCell(2) == null ? null : ((XSSFCell) row.getCell(2)).toString();
							String openingBalanceList = row.getCell(3) == null ? null
									: ((XSSFCell) row.getCell(3)).toString();
							String branchNameArray[] = null;
							String branchItemDiscount[] = null;
							String branchOpeningBalance[] = null;
							String branchopeningBalExpUnits[] = null;
							if (branchList != null) {
								branchNameArray = branchList.split(",");
							}
							if (openingBalanceList != null && !("").equals(openingBalanceList)) {
								branchOpeningBalance = openingBalanceList.split(",");
							}

							// String transactionPurposeList = row.getCell(3) == null ? null : ((XSSFCell)
							// row.getCell(3)).toString();
							// String validationMappingName = row.getCell(4) == null ? null : ((XSSFCell)
							// row.getCell(4)).toString();
							// String validationPLBSName = row.getCell(5) == null ? null : ((XSSFCell)
							// row.getCell(5)).toString();
							// String openingBalance = row.getCell(6) == null ? null : ((XSSFCell)
							// row.getCell(6)).toString();
							String particularName = null;
							String openingBalInventory = null;
							String transactionPurposeList = null;
							String validationMappingName = null;
							String walkinCustDiscount = null;
							String openingBalance = null;
							String invDesc1 = null;
							String invDesc2 = null;
							String incomeexpense = null;
							String incomeToExpenseMappedItemName = null;
							String isWithholdingApplicable = null;
							String withholdingTaxType = null;
							String withholdingRate = null;
							String withholdingTransLimit = null;
							String withholdingMoneyLimit = null;
							String isInputTaxApplicable = null;
							String isEligibleExpenseClaims = null;
							String purchaseOnLoanAccount = null;
							String incomeSpecificsPerUnitPrice = null;
							String reorderLevelBranchs = null;
							String reorderLevelEmail = null;
							String reorderLevelInvetoryValue = null;
							String isTransactionEditable = null;
							String tradingInvCalcMethod = null;
							String noOfExpUnit = null;
							String noOfIncUnit = null;
							String incUnitMeasure = null;
							String expUnitMeasure = null;
							String openingBalExpUnits = null;
							String openingBalExpRate = null;
							String GSTGoodServices = null;
							String HSNCode = null;
							String typeOfGood = null;
							String outputTaxOnGoods = null;
							String KLInst = null;
							String KLMandatory = null;
							String KLBranches = null;
							String isPriceInclusiveOfTax = null;
							Integer isPriceInclusive = 0;
							if (selectedCoaUploadType == 1) {
								particularName = "Incomes";
								walkinCustDiscount = row.getCell(4) == null ? null
										: ((XSSFCell) row.getCell(4)).toString();
								transactionPurposeList = row.getCell(5) == null ? null
										: ((XSSFCell) row.getCell(5)).toString();
								validationMappingName = row.getCell(6) == null ? null
										: ((XSSFCell) row.getCell(6)).toString();
								invDesc1 = row.getCell(7) == null ? null : ((XSSFCell) row.getCell(7)).toString();
								invDesc2 = row.getCell(8) == null ? null : ((XSSFCell) row.getCell(8)).toString();
								incomeSpecificsPerUnitPrice = row.getCell(9) == null ? null
										: ((XSSFCell) row.getCell(9)).toString();
								GSTGoodServices = row.getCell(10) == null ? null
										: ((XSSFCell) row.getCell(10)).toString();
								HSNCode = row.getCell(11) == null ? null
										: df.formatCellValue(((XSSFCell) row.getCell(11)));
								typeOfGood = row.getCell(12) == null ? null : ((XSSFCell) row.getCell(12)).toString();
								outputTaxOnGoods = row.getCell(13) == null ? null
										: ((XSSFCell) row.getCell(13)).toString();
								// String localtionDetail = row.getCell(8) == null ? null : ((XSSFCell)
								// row.getCell(8)).toString();
								incomeToExpenseMappedItemName = row.getCell(14) == null ? null
										: ((XSSFCell) row.getCell(14)).toString();
								tradingInvCalcMethod = row.getCell(15) == null ? null
										: ((XSSFCell) row.getCell(15)).toString();
								noOfExpUnit = row.getCell(16) == null ? null : ((XSSFCell) row.getCell(16)).toString();
								noOfIncUnit = row.getCell(17) == null ? null : ((XSSFCell) row.getCell(17)).toString();
								incUnitMeasure = row.getCell(18) == null ? null
										: ((XSSFCell) row.getCell(18)).toString();
								reorderLevelBranchs = row.getCell(19) == null ? null
										: ((XSSFCell) row.getCell(19)).toString();
								reorderLevelEmail = row.getCell(20) == null ? null
										: ((XSSFCell) row.getCell(20)).toString();
								reorderLevelInvetoryValue = row.getCell(21) == null ? null
										: ((XSSFCell) row.getCell(21)).toString();
								// incomeexpense = localtionDetail;
								isTransactionEditable = row.getCell(22) == null ? null
										: ((XSSFCell) row.getCell(22)).toString();
								KLInst = row.getCell(23) == null ? null : ((XSSFCell) row.getCell(23)).toString();
								KLMandatory = row.getCell(24) == null ? null : ((XSSFCell) row.getCell(24)).toString();
								KLBranches = row.getCell(25) == null ? null : ((XSSFCell) row.getCell(25)).toString();
								isPriceInclusiveOfTax = row.getCell(26) == null ? null
										: ((XSSFCell) row.getCell(26)).toString();
								if (walkinCustDiscount != null && !("").equals(walkinCustDiscount)) {
									branchItemDiscount = walkinCustDiscount.split(",");
								}
								if (isPriceInclusiveOfTax != null) {
									isPriceInclusive = 1;
								} else {
									isPriceInclusive = 0;
								}
							} else if (selectedCoaUploadType == 2) {
								particularName = "Expenses";
								openingBalExpUnits = row.getCell(4) == null ? null
										: ((XSSFCell) row.getCell(4)).toString();
								openingBalExpRate = row.getCell(5) == null ? null
										: ((XSSFCell) row.getCell(5)).toString();
								openingBalInventory = row.getCell(6) == null ? null
										: ((XSSFCell) row.getCell(6)).toString();
								transactionPurposeList = row.getCell(7) == null ? null
										: ((XSSFCell) row.getCell(7)).toString();
								validationMappingName = row.getCell(8) == null ? null
										: ((XSSFCell) row.getCell(8)).toString();
								GSTGoodServices = row.getCell(9) == null ? null
										: ((XSSFCell) row.getCell(9)).toString();
								HSNCode = row.getCell(10) == null ? null
										: df.formatCellValue(((XSSFCell) row.getCell(10)));
								typeOfGood = row.getCell(11) == null ? null : ((XSSFCell) row.getCell(11)).toString();
								expUnitMeasure = row.getCell(12) == null ? null
										: ((XSSFCell) row.getCell(12)).toString();

								// String capitalOrRevenue = row.getCell(7) == null ? null : ((XSSFCell)
								// row.getCell(7)).toString();
								isWithholdingApplicable = row.getCell(13) == null ? null
										: ((XSSFCell) row.getCell(13)).toString();
								withholdingTaxType = row.getCell(14) == null ? null
										: ((XSSFCell) row.getCell(14)).toString();
								withholdingRate = row.getCell(15) == null ? null
										: ((XSSFCell) row.getCell(15)).toString();
								withholdingTransLimit = row.getCell(16) == null ? null
										: ((XSSFCell) row.getCell(16)).toString();
								withholdingMoneyLimit = row.getCell(17) == null ? null
										: ((XSSFCell) row.getCell(17)).toString();
								isInputTaxApplicable = row.getCell(18) == null ? null
										: ((XSSFCell) row.getCell(18)).toString();
								isEligibleExpenseClaims = row.getCell(19) == null ? null
										: ((XSSFCell) row.getCell(19)).toString();
								// purchaseOnLoanAccount = row.getCell(14) == null ? null : ((XSSFCell)
								// row.getCell(14)).toString();
								KLInst = row.getCell(20) == null ? null : ((XSSFCell) row.getCell(20)).toString();
								KLMandatory = row.getCell(21) == null ? null : ((XSSFCell) row.getCell(21)).toString();
								KLBranches = row.getCell(22) == null ? null : ((XSSFCell) row.getCell(22)).toString();
								// incomeexpense = capitalOrRevenue;
							} else if (selectedCoaUploadType == 3) {
								particularName = "Assets";
							} else if (selectedCoaUploadType == 4) {
								particularName = "Liabilities";
							}
							if (selectedCoaUploadType == 3 || selectedCoaUploadType == 4) {
								transactionPurposeList = row.getCell(4) == null ? null
										: ((XSSFCell) row.getCell(4)).toString();
								validationMappingName = row.getCell(5) == null ? null
										: ((XSSFCell) row.getCell(5)).toString();
								isTransactionEditable = row.getCell(6) == null ? null
										: ((XSSFCell) row.getCell(6)).toString();
								KLInst = row.getCell(7) == null ? null : ((XSSFCell) row.getCell(7)).toString();
								KLMandatory = row.getCell(8) == null ? null : ((XSSFCell) row.getCell(8)).toString();
								KLBranches = row.getCell(9) == null ? null : ((XSSFCell) row.getCell(9)).toString();
							}

							Specifics newSpecifics = null;
							Particulars newParticular = null;
							/*
							 * Map<String, Object> criterias=new HashMap<String, Object>();
							 * String escapedAccName = IdosUtil.escapeHtml(accountName); //In db Housing &
							 * BF stored as Hosing &amp; BF as all entries in DB for name are esacped using
							 * Org.setName(),so to do comparison we too need to escape all names
							 * criterias.put("name", escapedAccName);
							 * criterias.put("organization.id", user.getOrganization().getId());
							 * Specifics parentSpecifics = genericDAO.getByCriteria(Specifics.class,
							 * criterias, entityManager);
							 */
							Particulars parentParticular = null;

							String hierarchy = null;
							Long maxActCode = null;
							Long parentActCode = null;

							// Search parent account specifics first
							if (parentSpecifics != null) {
								Long parentSpecificsActCode = parentSpecifics.getAccountCode();
								StringBuilder sbquery = new StringBuilder(
										"select MAX(obj.accountCode) from Specifics obj where obj.parentSpecifics.id=");
								sbquery.append(parentSpecifics.getId()).append(" and organization.id=")
										.append(user.getOrganization().getId()).append(" and obj.presentStatus=1");

								List maxSpecfObj = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
								if (maxSpecfObj.get(0) != null) {
									maxActCode = (Long) maxSpecfObj.get(0);
								} else {
									maxActCode = null;
								}
								log.log(Level.FINE, "maxActCode: " + maxActCode);
								log.log(Level.FINE, "parentSpecificsActCode: " + parentSpecificsActCode);
								hierarchy = parentSpecifics.getAccountCodeHirarchy() + parentSpecificsActCode + "/";
								parentActCode = parentSpecificsActCode;
							} else if (parentSpecifics == null) {
								// parent specific not found then use particular as parent
								Long parentParticularActCode = null;
								criterias.clear();
								criterias.put("name", particularName);
								criterias.put("organization.id", user.getOrganization().getId());
								criterias.put("presentStatus", 1);
								parentParticular = genericDAO.getByCriteria(Particulars.class, criterias,
										entityManager);
								if (parentParticular != null) {
									parentParticularActCode = parentParticular.getAccountCode();
									StringBuilder sbquery = new StringBuilder(
											"select MAX(obj.accountCode) from Specifics obj where obj.particularsId.id=");
									sbquery.append(parentParticular.getId())
											.append(" and obj.parentSpecifics IS NULL and organization.id=");
									sbquery.append(user.getOrganization().getId()).append(" and obj.presentStatus=1");
									List maxSpecfObj = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
									if (maxSpecfObj.get(0) != null) {
										maxActCode = (Long) maxSpecfObj.get(0);
									} else {
										maxActCode = null;
									}
									log.log(Level.FINE, "maxActCode: " + maxActCode);
									log.log(Level.FINE, "parentParticularActCode: " + parentParticularActCode);
									hierarchy = parentParticular.getAccountCodeHirarchy() + parentParticularActCode
											+ "/";
								}
								parentActCode = parentParticularActCode;
							}
							Long actCode = AccountCodeUtil.generateAccountCode(parentActCode, maxActCode);
							criterias.clear();
							criterias.put("accountCode", actCode);
							criterias.put("organization.id", user.getOrganization().getId());
							criterias.put("presentStatus", 1);
							Specifics foundSpecf = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
							if (foundSpecf != null) {
								actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
							}
							String aCode = String.valueOf(actCode);
							int length = aCode.length();
							int pos = aCode.indexOf("9");
							int itrate = 0;
							String one = "1";
							if (pos != -1) {
								itrate = length - pos - 3;
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
							actCode = getValidatedAccountCode(actCode, user, parentActCode, entityManager);
							if (parentSpecifics != null) {
								if (subAccountName != null && !"".equals(subAccountName.trim())) {
									newSpecifics = prepareSpecifics(entityManager, parentParticular, parentSpecifics,
											user, actCode, hierarchy, subAccountName, isEligibleExpenseClaims,
											purchaseOnLoanAccount, selectedCoaUploadType, incomeSpecificsPerUnitPrice,
											isWithholdingApplicable, isInputTaxApplicable, withholdingRate,
											withholdingTransLimit, withholdingMoneyLimit, validationMappingName,
											branchOpeningBalance, incomeToExpenseMappedItemName, reorderLevelBranchs,
											reorderLevelEmail, reorderLevelInvetoryValue, branchNameArray,
											branchItemDiscount, transactionPurposeList, isTransactionEditable,
											GSTGoodServices, HSNCode, typeOfGood, outputTaxOnGoods, KLInst, KLMandatory,
											KLBranches, tradingInvCalcMethod, noOfExpUnit, noOfIncUnit, incUnitMeasure,
											expUnitMeasure, openingBalExpUnits, openingBalExpRate, openingBalInventory,
											invDesc1, invDesc2, withholdingTaxType, isPriceInclusive);
								}
							} else if (parentSpecifics == null) {
								Specifics newParentSpecifics = prepareSpecifics(entityManager, parentParticular,
										parentSpecifics, user, actCode, hierarchy, accountName, isEligibleExpenseClaims,
										purchaseOnLoanAccount, selectedCoaUploadType, incomeSpecificsPerUnitPrice,
										isWithholdingApplicable, isInputTaxApplicable, withholdingRate,
										withholdingTransLimit, withholdingMoneyLimit, null, branchOpeningBalance,
										incomeToExpenseMappedItemName, reorderLevelBranchs, reorderLevelEmail,
										reorderLevelInvetoryValue, branchNameArray, branchItemDiscount,
										transactionPurposeList, isTransactionEditable, GSTGoodServices, HSNCode,
										typeOfGood, outputTaxOnGoods, KLInst, KLMandatory, KLBranches,
										tradingInvCalcMethod, noOfExpUnit, noOfIncUnit, incUnitMeasure, expUnitMeasure,
										openingBalExpUnits, openingBalExpRate, openingBalInventory, invDesc1, invDesc2,
										withholdingTaxType, isPriceInclusive);

								if (subAccountName != null && !"".equals(subAccountName)) {
									log.log(Level.FINE, "Start sub account entry for parent: "
											+ newParentSpecifics.getAccountCode());
									if (newParentSpecifics != null) {
										Long parentSpecificsActCode = newParentSpecifics.getAccountCode();
										StringBuilder sbquery = new StringBuilder(
												"select MAX(obj.accountCode) from Specifics obj where obj.parentSpecifics.id=");
										sbquery.append(newParentSpecifics.getId()).append(" and organization.id=")
												.append(user.getOrganization().getId())
												.append(" and obj.presentStatus=1");

										List maxSpecfObj = genericDAO.executeSimpleQuery(sbquery.toString(),
												entityManager);
										if (maxSpecfObj.get(0) != null) {
											maxActCode = (Long) maxSpecfObj.get(0);
										} else {
											maxActCode = null;
										}
										log.log(Level.FINE, "maxActCode: " + maxActCode);
										log.log(Level.FINE, "parentSpecificsActCode: " + parentSpecificsActCode);
										hierarchy = newParentSpecifics.getAccountCodeHirarchy() + parentSpecificsActCode
												+ "/";
										parentActCode = parentSpecificsActCode;
									}

									actCode = AccountCodeUtil.generateAccountCode(parentActCode, maxActCode);
									criterias.clear();
									criterias.put("accountCode", actCode);
									criterias.put("organization.id", user.getOrganization().getId());
									criterias.put("presentStatus", 1);
									foundSpecf = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
									if (foundSpecf != null) {
										actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
									}
									aCode = String.valueOf(actCode);
									length = aCode.length();
									pos = aCode.indexOf("9");
									itrate = 0;
									one = "1";
									if (pos != -1) {
										itrate = length - pos - 3;
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
									actCode = getValidatedAccountCode(actCode, user, parentActCode, entityManager);
									newSpecifics = prepareSpecifics(entityManager, parentParticular, newParentSpecifics,
											user, actCode, hierarchy, subAccountName, isEligibleExpenseClaims,
											purchaseOnLoanAccount, selectedCoaUploadType, incomeSpecificsPerUnitPrice,
											isWithholdingApplicable, isInputTaxApplicable, withholdingRate,
											withholdingTransLimit, withholdingMoneyLimit, validationMappingName,
											branchOpeningBalance, incomeToExpenseMappedItemName, reorderLevelBranchs,
											reorderLevelEmail, reorderLevelInvetoryValue, branchNameArray,
											branchItemDiscount, transactionPurposeList, isTransactionEditable,
											GSTGoodServices, HSNCode, typeOfGood, outputTaxOnGoods, KLInst, KLMandatory,
											KLBranches, tradingInvCalcMethod, noOfExpUnit, noOfIncUnit, incUnitMeasure,
											expUnitMeasure, openingBalExpUnits, openingBalExpRate, openingBalInventory,
											invDesc1, invDesc2, withholdingTaxType, isPriceInclusive);

								}

							}
							if (ConfigParams.getInstance().isDeploymentSingleUser(user)) {
								// For Single User Deployment Only
								if (null != newSpecifics) {
									if (newSpecifics.getModifiedAt() == null) {
										singleUserService.updateOnCOACreation(user, newSpecifics, entityManager);
									}
								}
							}

						} // end while
					}
				} catch (Exception ex) {
					log.log(Level.INFO, "Total rows inserted " + totalRowsInserted);
					log.log(Level.SEVERE, "Error", ex);
					throw ex;
				}
				if (user != null) {
					user.getOrganization().setChartOfAccountUploadStatus(1);
				}
				// sendMailWithAttachment(session, subject, fileName, file,
				// "allusers@myidos.com", "alerts@myidos.com");
			} // end for

			ObjectNode row = Json.newObject();
			row.put("message", "Uploaded Successfully");
			an.add(row);
			entityManager.getTransaction().commit();
		} catch (RuntimeException e) {
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			throw e;
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			ex.printStackTrace();
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		} finally {
			result.put("uploadIssue", uploadIssue);
			result.put("totalRowsInserted", totalRowsInserted - 1);
			log.log(Level.INFO, "uploadIssue " + uploadIssue);
			log.log(Level.INFO, "Total rows inserted " + totalRowsInserted);
			if (entityManager.getTransaction() != null && entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
		}
		log.log(Level.FINE, ">>>>>>> End");
		return Results.ok(result);
	}

	private static Specifics prepareSpecifics(EntityManager entityManager, Particulars parentParticular,
			Specifics parentSpecifics, Users user, Long actCode, String hierarchy, String specificsName,
			String isEligibleExpenseClaims, String purchaseOnLoanAccount, int selectedCoaUploadType,
			String incomeSpecificsPerUnitPrice, String isWithholdingApplicable, String isInputTaxApplicable,
			String withholdingRate, String withholdingTransLimit, String withholdingMoneyLimit,
			String validationMappingName, String[] branchOpeningBalance, String incomeToExpenseMappedItemName,
			String reorderLevelBranchs, String reorderLevelEmail, String reorderLevelInvetoryValue,
			String branchNameArray[], String branchItemDiscount[], String transactionPurposeList,
			String isTransactionEditable,
			String GSTGoodServices, String HSNCode, String typeOfGood, String outputTaxOnGoods, String KLInst,
			String KLMandatory, String KLBranches,
			String tradingInvCalcMethod, String noOfExpUnit, String noOfIncUnit, String incUnitMeasure,
			String expUnitMeasure, String openingBalExpUnits, String openingBalExpRate, String openingBalInventory,
			String invDesc1, String invDesc2, String withholdingTaxType, Integer isPriceInclusive) throws Exception {
		log.log(Level.FINE, ">>>>>>> Start name = " + specificsName);
		Specifics newSpecifics = new Specifics();

		newSpecifics.setAccountCode(actCode);
		newSpecifics.setAccountCodeHirarchy(hierarchy);

		newSpecifics.setName(specificsName);
		if (isEligibleExpenseClaims != null && !"".equals(isEligibleExpenseClaims)) {
			if ("Yes".equalsIgnoreCase(isEligibleExpenseClaims))
				newSpecifics.setEmployeeClaimItem(1);
			else
				newSpecifics.setEmployeeClaimItem(0);
		}

		/*
		 * if(purchaseOnLoanAccount!=null){
		 * if("Yes".equalsIgnoreCase(purchaseOnLoanAccount))
		 * newSpecifics.setIsLoanAccountItem(1);
		 * else
		 * newSpecifics.setIsLoanAccountItem(0);
		 * }
		 */

		if (parentParticular != null) {
			newSpecifics.setParticularsId(parentParticular);
			newSpecifics.setParentSpecifics(null);
		}
		if (parentSpecifics != null) {
			newSpecifics.setParticularsId(parentSpecifics.getParticularsId());
			newSpecifics.setParentSpecifics(parentSpecifics);
		}

		if (isTransactionEditable != null && !"".equals(isTransactionEditable)) {
			if ("Yes".equalsIgnoreCase(isTransactionEditable))
				newSpecifics.setIsTransactionEditable(1);
			else
				newSpecifics.setIsTransactionEditable(0);
		}

		if (selectedCoaUploadType == 1) {
			if (invDesc1 != null && !invDesc1.equals("")) {
				newSpecifics.setInvoiceItemDescription1(invDesc1);
				newSpecifics.setIsInvoiceDescription1(1);

			}
			if (invDesc2 != null && !invDesc2.equals("")) {
				newSpecifics.setInvoiceItemDescription2(invDesc2);
				newSpecifics.setIsInvoiceDescription2(1);
			}
			if (incomeSpecificsPerUnitPrice != null && !incomeSpecificsPerUnitPrice.equals("")) {
				newSpecifics.setIncomeSpecfPerUnitPrice(Double.parseDouble(incomeSpecificsPerUnitPrice));
			} else {
				newSpecifics.setIncomeSpecfPerUnitPrice(null);

			}
			if (isPriceInclusive == 1) {
				newSpecifics.setIsPriceInclusive(1);
			} else {
				newSpecifics.setIsPriceInclusive(null);
			}
			/*
			 * if(incomeExpense!=null){
			 * if (incomeExpense.equalsIgnoreCase("Domestic")) {
			 * newSpecifics.setIncomeOrExpenseType(1);
			 * } else if (incomeExpense.equalsIgnoreCase("International")) {
			 * newSpecifics.setIncomeOrExpenseType(2);
			 * } else if (incomeExpense.equalsIgnoreCase("Both")) {
			 * newSpecifics.setIncomeOrExpenseType(3);
			 * }
			 * }
			 */

		} else if (selectedCoaUploadType == 2) {
			/*
			 * if(incomeExpense!=null){
			 * if(incomeExpense.equalsIgnoreCase("Capital")){
			 * newSpecifics.setIncomeOrExpenseType(1);
			 * }else if(incomeExpense.equalsIgnoreCase("Revenue")){
			 * newSpecifics.setIncomeOrExpenseType(2);
			 * }
			 * }
			 */
			if (isWithholdingApplicable != null) {
				if (isWithholdingApplicable.equals("Yes")) {
					newSpecifics.setIsWithholdingApplicable(1);
				}
				if (isWithholdingApplicable.equals("No")) {
					newSpecifics.setIsWithholdingApplicable(0);
				}
			}
			if (isInputTaxApplicable != null) {
				if (isInputTaxApplicable.equals("Yes")) {
					newSpecifics.setIsCaptureInputTaxes(1);
				} else if (isInputTaxApplicable.equals("Yes")) {
					newSpecifics.setIsCaptureInputTaxes(2);
				}
			}
			if (withholdingRate != null && !"".equals(withholdingRate)) {
				newSpecifics.setWithHoldingRate(Double.parseDouble(withholdingRate));
			}
			if (withholdingTransLimit != null && !withholdingTransLimit.equals("")) {
				newSpecifics.setWithHoldingLimit(Double.parseDouble(withholdingTransLimit));
			}
			if (withholdingMoneyLimit != null && !withholdingMoneyLimit.equals("")) {
				newSpecifics.setWithholdingMonetoryLimit(Double.parseDouble(withholdingMoneyLimit));
			}

			if (withholdingTaxType != null && withholdingTaxType != "") {
				if (withholdingTaxType.equalsIgnoreCase("Sec192-Payment of Salary")) {
					newSpecifics.setWithholdingType(31);
				} else if (withholdingTaxType
						.equalsIgnoreCase("Sec194A-Income by way of Interest other than Interest on Securities")) {
					newSpecifics.setWithholdingType(32);
				} else if (withholdingTaxType
						.equalsIgnoreCase("Sec194C-Payment to Contractors/SubContractors - Individuals / HUF")) {
					newSpecifics.setWithholdingType(33);
				} else if (withholdingTaxType
						.equalsIgnoreCase("Sec194C-Payment to Contractors/SubContractors - Others")) {
					newSpecifics.setWithholdingType(34);
				} else if (withholdingTaxType.equalsIgnoreCase("Sec194H-Commission or Brokerage")) {
					newSpecifics.setWithholdingType(35);
				} else if (withholdingTaxType.equalsIgnoreCase("Sec194-I-Rent-(a) Plant and Machinery")) {
					newSpecifics.setWithholdingType(36);
				} else if (withholdingTaxType
						.equalsIgnoreCase("Sec194-I-Rent-(b)-Land or building or furniture or fitting")) {
					newSpecifics.setWithholdingType(37);
				} else if (withholdingTaxType
						.equalsIgnoreCase("Sec-194J-Fees for Professional/Technical Service etc.")) {
					newSpecifics.setWithholdingType(38);
				}
			}
			// inventory opening bal and rate for buy item
			if (expUnitMeasure != null && expUnitMeasure != "") {
				newSpecifics.setExpenseUnitsMeasure(expUnitMeasure);
			}
		}
		newSpecifics.setOrganization(user.getOrganization());

		if (selectedCoaUploadType == 1 || selectedCoaUploadType == 2) { // GST specific info
			newSpecifics.setGstItemCode(HSNCode);
			/*
			 * if(HSNCode!=null && HSNCode!="" && !HSNCode.equalsIgnoreCase("null")){
			 * int p=HSNCode.lastIndexOf("."); //HSNCODE= ab123.0 is appearing, by default
			 * all cellvalue in xls is giving decimal output
			 * if(p > 0){
			 * String HSNCodeDecimalPartRemoved=HSNCode.substring(0,p);
			 * newSpecifics.setGstItemCode(HSNCodeDecimalPartRemoved);
			 * }
			 * }
			 */
			if (GSTGoodServices != null && !GSTGoodServices.equals("")) {
				newSpecifics.setGstTypeOfSupply(GSTGoodServices);
			} else {
				newSpecifics.setGstTypeOfSupply(null);
			}
			if (typeOfGood != null && !typeOfGood.equals("")) {
				if (typeOfGood.equalsIgnoreCase(IdosConstants.GST_EXEMPT_SUPPLY_TEXT)) {
					newSpecifics.setGstItemCategory("1");
				} else if (typeOfGood.equalsIgnoreCase(IdosConstants.GST_NIL_RATE_SUPPLY_TEXT)) {
					newSpecifics.setGstItemCategory("2");
				} else if (typeOfGood.equalsIgnoreCase(IdosConstants.GST_NON_RATE_SUPPLY_TEXT)) {
					newSpecifics.setGstItemCategory("3");
				}
			} else {
				newSpecifics.setGstItemCategory(null);
			}
			if (outputTaxOnGoods != null && !"".equals(outputTaxOnGoods)) {
				newSpecifics.setGstTaxRate(Double.parseDouble(outputTaxOnGoods));
			}
		}

		if (validationMappingName != null && !"".equals(validationMappingName)) {
			CoaValidationMapping mapping = CoaValidationMapping.findByName(entityManager, validationMappingName);
			if (mapping != null) {
				Map<String, Object> criterias = new HashMap<String, Object>();
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("identificationForDataValid", "" + mapping.getId());
				criterias.put("presentStatus", 1);
				Specifics tmpSpecifics = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
				// only once a mapping is allowed for an oraganization.
				if (tmpSpecifics == null && mapping != null) {
					newSpecifics.setIdentificationForDataValid("" + mapping.getId());
				}
			} else {
				if (log.isLoggable(Level.FINE)) {
					log.log(Level.FINE, " mapping not found For == " + newSpecifics.getName());
				}
			}
		}

		// This logic is used when INCOME ITEM(sell item) is inserted, so we will
		// already have data in Branchspecifics inventory OB inserted through Expense
		// item.
		Specifics mapSpecf = null;
		if (incomeToExpenseMappedItemName != null && !incomeToExpenseMappedItemName.equals("")) {// In income specifics
																									// if it is mapped
																									// as inventory item
			List<Specifics> foundSpecificsList = Specifics.findByNameAndHeadType(entityManager, user.getOrganization(),
					incomeToExpenseMappedItemName, String.valueOf(selectedCoaUploadType));
			if (foundSpecificsList != null && foundSpecificsList.size() > 0) {
				mapSpecf = foundSpecificsList.get(0);// expense buy specific
			}
		}
		if (mapSpecf != null) {
			double conversionUnit = 1;
			newSpecifics.setLinkIncomeExpenseSpecifics(mapSpecf);
			if (noOfExpUnit != null && noOfIncUnit != null && noOfIncUnit != "" && noOfExpUnit != "") {
				newSpecifics.setNoOfExpenseUnits(Double.parseDouble(noOfExpUnit)); // Income sepecifics
				newSpecifics.setNoOfIncomeUnits(Double.parseDouble(noOfIncUnit));
				conversionUnit = Double.parseDouble(noOfIncUnit) / Double.parseDouble(noOfExpUnit);// 1 bag expense unit
																									// = 200 peices
																									// chocolate (income
																									// unit)
			} else {
				newSpecifics.setNoOfExpenseUnits(0.0);
				newSpecifics.setNoOfIncomeUnits(0.0);
			}
			newSpecifics.setExpenseToIncomeConverstionRate(conversionUnit);
			newSpecifics.setIncomeUnitsMeasure(incUnitMeasure);
			newSpecifics.setExpenseUnitsMeasure(mapSpecf.getExpenseUnitsMeasure());
			newSpecifics.setTradingInventoryCalcMethod(tradingInvCalcMethod);
			newSpecifics.setIsTradingInvenotryItem(1);// so it will be inserted into TRADING_INVENTORY table too when
														// buy/sell transaction for showing in inventory Report

			List<BranchSpecifics> branchSpecifics = mapSpecf.getSpecificsBranch();
			mapSpecf.setNoOfExpenseUnits(newSpecifics.getNoOfExpenseUnits());
			mapSpecf.setNoOfIncomeUnits(newSpecifics.getNoOfIncomeUnits());
			mapSpecf.setExpenseToIncomeConverstionRate(conversionUnit);
			mapSpecf.setIsTradingInvenotryItem(1);
			mapSpecf.setIncomeUnitsMeasure(incUnitMeasure);
			mapSpecf.setTradingInventoryCalcMethod(tradingInvCalcMethod);
			genericDAO.saveOrUpdate(mapSpecf, user, entityManager); // save mapped Expense buy specifics by setting
																	// inventory_item=1
			for (BranchSpecifics branchSpecific : branchSpecifics) {
				// if (branchSpecific.getInvOpeningBalUnits() != null &&
				// branchSpecific.getInvOpeningBalUnits() > 0) {
				TradingInventory tradingInv = TradingInventory.getTradingInventory(entityManager,
						user.getOrganization().getId(), branchSpecific.getBranch().getId(), mapSpecf.getId(),
						IdosConstants.TRADING_INV_OPENING_BAL);
				if (tradingInv == null) {
					tradingInv = new TradingInventory();
				}
				log.log(Level.FINE, "==------>>>>>" + mapSpecf.getCreatedAt());
				tradingInv.setDate(mapSpecf.getCreatedAt());
				tradingInv.setBranch(branchSpecific.getBranch());
				tradingInv.setOrganization(user.getOrganization());
				tradingInv.setUser(user);
				tradingInv.setTransactionType(IdosConstants.TRADING_INV_OPENING_BAL); // buy trade opening bal=3, buy
																						// =1, sell=2, closing=4
				tradingInv.setTransactionSpecifics(mapSpecf);
				tradingInv.setTotalQuantity(branchSpecific.getInvOpeningBalUnits());
				tradingInv.setCalcualtedRate(branchSpecific.getInvOpeningBalRate());
				tradingInv.setGrossValue(branchSpecific.getInvOpeningBalance());
				double qtyConvertedToIncomeUnit = 0.0;
				double buyRate = 0.0;
				if (branchSpecific.getInvOpeningBalUnits() != null && branchSpecific.getInvOpeningBalance() != null
						&& branchSpecific.getInvOpeningBalUnits() > 0) {
					qtyConvertedToIncomeUnit = branchSpecific.getInvOpeningBalUnits() * conversionUnit; // so
																										// buyqty=5bag,
																										// but
																										// 1bag=100chocolate
																										// pieces when
																										// selling
																										// chocolate,
																										// then put 5oo
																										// for qty
					buyRate = branchSpecific.getInvOpeningBalance() / qtyConvertedToIncomeUnit;
					tradingInv.setNoOfExpUnitsConvertedToIncUnits(qtyConvertedToIncomeUnit);
					tradingInv.setCalcualtedRate(buyRate);
				}
				genericDAO.saveOrUpdate(tradingInv, user, entityManager); // save TradingInventory first entry for this
																			// item will be opening bal, when this item
																			// is created. Later buy/sell entries will
																			// be added in transacitoncontroller

				if (tradingInvCalcMethod != null && tradingInvCalcMethod.equalsIgnoreCase("WAC")) {
					TradingInventory tradingInvclosing = TradingInventory.getTradingInventory(entityManager,
							user.getOrganization().getId(), branchSpecific.getBranch().getId(), mapSpecf.getId(),
							IdosConstants.TRADING_INV_CLOSING_BAL);
					if (tradingInvclosing == null) {
						tradingInvclosing = new TradingInventory();
					}
					tradingInvclosing.setDate(mapSpecf.getCreatedAt());
					tradingInvclosing.setBranch(branchSpecific.getBranch());
					tradingInvclosing.setOrganization(user.getOrganization());
					tradingInvclosing.setUser(user);
					tradingInvclosing.setTransactionSpecifics(mapSpecf); // for closing trade always use buyspecific
																			// because we might need to fetch this
																			// closing trade for buy transaction and at
																			// that time we don't get sellspecific from
																			// buy trade
					tradingInvclosing.setTransactionType(IdosConstants.TRADING_INV_CLOSING_BAL); // buy=1, sell=2,
																									// opening=3,
																									// closing=4
					tradingInvclosing.setTotalQuantity(branchSpecific.getInvOpeningBalUnits());
					tradingInvclosing.setGrossValue(branchSpecific.getInvOpeningBalance());
					tradingInvclosing.setNoOfExpUnitsConvertedToIncUnits(qtyConvertedToIncomeUnit);
					tradingInvclosing.setCalcualtedRate(buyRate);
					genericDAO.saveOrUpdate(tradingInvclosing, user, entityManager); // save closing entry for
																						// openingbal buy trade
				}
				// }
			}
		} else {
			newSpecifics.setLinkIncomeExpenseSpecifics(null);
		}

		specfcrud.save(user, newSpecifics, entityManager);

		if (incomeToExpenseMappedItemName != null && !incomeToExpenseMappedItemName.equals("")) {
			// reorder level save logic start
			List<WarehouseItemStockReorderLevel> oldItemWarehouseStockReorderLevel = newSpecifics
					.getItemWarehouseStockReorderLevels();
			List<WarehouseItemStockReorderLevel> newItemWarehouseStockReorderLevel = new ArrayList<WarehouseItemStockReorderLevel>();
			if (reorderLevelBranchs != null && reorderLevelEmail != null) {
				String reorderLevelbranchNameList[] = reorderLevelBranchs.split(",");
				String reorderLevelEmailList[] = reorderLevelEmail.split(",");
				String reorderLevelInventoryList[] = reorderLevelInvetoryValue != null
						? reorderLevelInvetoryValue.split(",")
						: null;
				for (int cnt = 0; cnt < reorderLevelbranchNameList.length; cnt++) {
					Branch branchInfo = getBranch(reorderLevelbranchNameList[cnt], user);
					if (branchInfo != null) {
						Long reorderLevelbranchId = branchInfo.getId();
						String reorderLevelbranchUser = null;
						if (reorderLevelEmailList != null && reorderLevelEmailList.length > cnt) {
							reorderLevelbranchUser = reorderLevelEmailList[cnt];
						}
						String reorderLevelInventory = null;
						if (reorderLevelInventoryList != null && reorderLevelInventoryList.length > cnt) {
							reorderLevelInventory = reorderLevelInventoryList[cnt];
						}
						if (reorderLevelbranchId != null && reorderLevelbranchUser != null
								&& reorderLevelInventory != null) {
							WarehouseItemStockReorderLevel warehouseItemStockReorderLevel = new WarehouseItemStockReorderLevel();
							warehouseItemStockReorderLevel.setBranch(branchInfo);
							warehouseItemStockReorderLevel.setOrganization(user.getOrganization());
							warehouseItemStockReorderLevel.setSpecifics(newSpecifics);
							warehouseItemStockReorderLevel.setParticular(newSpecifics.getParticularsId());
							if (!reorderLevelbranchUser.equals("")) {
								warehouseItemStockReorderLevel.setReorderLevelAlertUser(reorderLevelbranchUser);
							} else {
								warehouseItemStockReorderLevel.setReorderLevelAlertUser(null);
							}
							if (!reorderLevelInventory.equals("")) {
								Integer recordLevel = (int) Double.parseDouble(reorderLevelInventory);
								warehouseItemStockReorderLevel.setReorderLevel(recordLevel);
							} else {
								warehouseItemStockReorderLevel.setReorderLevel(0);
							}
							newItemWarehouseStockReorderLevel.add(warehouseItemStockReorderLevel);
						}
					}
				}
			}
			List<List<WarehouseItemStockReorderLevel>> businessEntityWarehouseItemStockReorderLevelList = ListUtility
					.getWarehouseItemStockReorderLevelList(oldItemWarehouseStockReorderLevel,
							newItemWarehouseStockReorderLevel);
			for (int b = 0; b < businessEntityWarehouseItemStockReorderLevelList.size(); b++) {
				if (b == 0) {
					List<WarehouseItemStockReorderLevel> oldWarehouseItemStockReorderLevel = businessEntityWarehouseItemStockReorderLevelList
							.get(b);
					if (oldWarehouseItemStockReorderLevel != null) {
						for (WarehouseItemStockReorderLevel warehouseItemStockReorderLevel : oldWarehouseItemStockReorderLevel) {
							entityManager.remove(warehouseItemStockReorderLevel);
						}
					}
				}
				if (b == 1) {
					List<WarehouseItemStockReorderLevel> newWarehouseItemStockReorderLevel = businessEntityWarehouseItemStockReorderLevelList
							.get(b);
					if (newWarehouseItemStockReorderLevel != null) {
						for (WarehouseItemStockReorderLevel newWarehouseItemStockReorderLevelObj : newWarehouseItemStockReorderLevel) {
							genericDAO.saveOrUpdate(newWarehouseItemStockReorderLevelObj, user, entityManager);
						}
					}
				}
			}
		}
		// reorder level save logic end

		List<BranchSpecifics> oldBranchSpecifics = newSpecifics.getSpecificsBranch();
		List<BranchSpecifics> newBranchSpecifics = new ArrayList<BranchSpecifics>();
		String[] branchInvNoOfUnitArr = null;
		if (openingBalExpUnits != null) {
			branchInvNoOfUnitArr = openingBalExpUnits.split(",");
		}

		String[] branchInvRateArr = null;
		if (openingBalExpRate != null) {
			branchInvRateArr = openingBalExpRate.split(",");
		}

		String[] branchInvOpeningBalanceArr = null;
		if (openingBalInventory != null) {
			branchInvOpeningBalanceArr = openingBalInventory.split(",");
		}
		double totalOpeningBalance = 0.0;
		double totalExpOpeningBalUnits = 0.0;
		double totalExpOpeningBalRate = 0.0;
		double totalExpOpeningBalInvetory = 0.0;
		if (branchNameArray != null) {
			for (int b = 0; b < branchNameArray.length; b++) {
				BranchSpecifics newBnchSpecf = new BranchSpecifics();
				Branch newBnch = getBranch(branchNameArray[b], user);
				if (newBnch != null) {
					newBnchSpecf.setBranch(newBnch);
					newBnchSpecf.setOrganization(user.getOrganization());
					newBnchSpecf.setSpecifics(newSpecifics);
					newBnchSpecf.setParticular(newSpecifics.getParticularsId());
					if (branchOpeningBalance != null && !"".equals(branchOpeningBalance)
							&& !branchOpeningBalance.equals("null")) {
						if (branchOpeningBalance[b] != null && !"".equals(branchOpeningBalance[b])) {
							newBnchSpecf.setOpeningBalance(Double.parseDouble(branchOpeningBalance[b].trim()));
							totalOpeningBalance = totalOpeningBalance
									+ Double.parseDouble(branchOpeningBalance[b].trim());
						}
					} else {
						newBnchSpecf.setOpeningBalance(0.0);
					}
					if (branchItemDiscount != null && !"".equals(branchItemDiscount)) {
						if (branchItemDiscount[b] != null && !"".equals(branchItemDiscount[b])) {
							newBnchSpecf.setWalkinCustomerMaxDiscount(Double.parseDouble(branchItemDiscount[b].trim()));
						}
					}
					if (openingBalExpUnits != null && !"".equals(openingBalExpUnits)) {
						if (branchInvNoOfUnitArr[b] != null && !"".equals(branchInvNoOfUnitArr[b])) {
							newBnchSpecf.setInvOpeningBalUnits(Double.parseDouble(branchInvNoOfUnitArr[b].trim()));
							totalExpOpeningBalUnits = totalExpOpeningBalUnits
									+ Double.parseDouble(branchInvNoOfUnitArr[b].trim());
						}
					}
					if (openingBalExpRate != null && !"".equals(openingBalExpRate)) {
						if (branchInvRateArr[b] != null && !"".equals(branchInvRateArr[b])) {
							newBnchSpecf.setInvOpeningBalRate(Double.parseDouble(branchInvRateArr[b].trim()));
							totalExpOpeningBalRate = totalExpOpeningBalRate
									+ Double.parseDouble(branchInvRateArr[b].trim());
						}
					}
					if (openingBalInventory != null && !"".equals(openingBalInventory)) {
						if (branchInvOpeningBalanceArr[b] != null && !"".equals(branchInvOpeningBalanceArr[b])) {
							if (branchInvOpeningBalanceArr[b].trim().matches("-?\\d+(\\.\\d+)?")) {
								newBnchSpecf
										.setInvOpeningBalance(Double.parseDouble(branchInvOpeningBalanceArr[b].trim()));
								totalExpOpeningBalInvetory = totalExpOpeningBalInvetory
										+ Double.parseDouble(branchInvOpeningBalanceArr[b].trim());
							}
						}
					}
					newBranchSpecifics.add(newBnchSpecf);
					if (newSpecifics.getId() != null) { // For expense item, this code will get triggered
						double conversionUnit = 1;
						Specifics SpecForIn = Specifics.findById(newSpecifics.getId()); // expence
						Map<String, Object> criterias = new HashMap<String, Object>();
						criterias.clear();
						criterias.put("linkIncomeExpenseSpecifics.id", newSpecifics.getId());
						criterias.put("organization.id", user.getOrganization().getId());
						criterias.put("presentStatus", 1);
						Specifics foundSpecf = genericDAO.getByCriteria(Specifics.class, criterias, entityManager); // Income
						if (foundSpecf != null && foundSpecf.getLinkIncomeExpenseSpecifics() != null) {
							if (foundSpecf.getNoOfExpenseUnits() != null && foundSpecf.getNoOfIncomeUnits() != null
									&& foundSpecf.getNoOfIncomeUnits() > 0) {
								conversionUnit = foundSpecf.getNoOfExpenseUnits() / foundSpecf.getNoOfIncomeUnits();
							}

							TradingInventory tradingInv = TradingInventory.getTradingInventory(entityManager,
									user.getOrganization().getId(), newBnch.getId(), SpecForIn.getId(),
									IdosConstants.TRADING_INV_OPENING_BAL);
							if (tradingInv == null) {
								tradingInv = new TradingInventory();
							}
							tradingInv.setDate(SpecForIn.getCreatedAt());
							tradingInv.setBranch(newBnch);
							tradingInv.setOrganization(user.getOrganization());
							tradingInv.setUser(user);
							tradingInv.setTransactionType(IdosConstants.TRADING_INV_OPENING_BAL); // buy trade opening
																									// bal=3, buy =1,
																									// sell=2, closing=4
							tradingInv.setTransactionSpecifics(SpecForIn);
							tradingInv.setTotalQuantity(newBnchSpecf.getInvOpeningBalUnits());
							tradingInv.setCalcualtedRate(newBnchSpecf.getInvOpeningBalRate());
							tradingInv.setGrossValue(Double.parseDouble(branchInvOpeningBalanceArr[b]));
							double qtyConvertedToIncomeUnit = 0.0;
							double buyRate = 0.0;
							if (newBnchSpecf.getInvOpeningBalUnits() != null
									&& newBnchSpecf.getInvOpeningBalance() != null
									&& newBnchSpecf.getInvOpeningBalUnits() > 0) {
								qtyConvertedToIncomeUnit = newBnchSpecf.getInvOpeningBalUnits() * conversionUnit; // so
																													// buyqty=5bag,
																													// but
																													// 1bag=100chocolate
																													// pieces
																													// when
																													// selling
																													// chocolate,
																													// then
																													// put
																													// 5oo
																													// for
																													// qty
								buyRate = newBnchSpecf.getInvOpeningBalance() / qtyConvertedToIncomeUnit;
								tradingInv.setNoOfExpUnitsConvertedToIncUnits(qtyConvertedToIncomeUnit);
								tradingInv.setCalcualtedRate(buyRate);
							}
							genericDAO.saveOrUpdate(tradingInv, user, entityManager); // save TradingInventory first
																						// entry for this item will be
																						// opening bal, when this item
																						// is created. Later buy/sell
																						// entries will be added in
																						// transacitoncontroller

							if (tradingInvCalcMethod != null && tradingInvCalcMethod.equalsIgnoreCase("WAC")) {
								TradingInventory tradingInvclosing = TradingInventory.getTradingInventory(entityManager,
										user.getOrganization().getId(), newBnch.getId(), mapSpecf.getId(),
										IdosConstants.TRADING_INV_CLOSING_BAL);
								if (tradingInvclosing == null) {
									tradingInvclosing = new TradingInventory();
								}
								tradingInvclosing.setDate(mapSpecf.getCreatedAt());
								tradingInvclosing.setBranch(newBnch);
								tradingInvclosing.setOrganization(user.getOrganization());
								tradingInvclosing.setUser(user);
								tradingInvclosing.setTransactionSpecifics(mapSpecf); // for closing trade always use
																						// buyspecific because we might
																						// need to fetch this closing
																						// trade for buy transaction and
																						// at that time we don't get
																						// sellspecific from buy trade
								tradingInvclosing.setTransactionType(IdosConstants.TRADING_INV_CLOSING_BAL); // buy=1,
																												// sell=2,
																												// opening=3,
																												// closing=4
								tradingInvclosing.setTotalQuantity(newBnchSpecf.getInvOpeningBalUnits());
								tradingInvclosing.setGrossValue(newBnchSpecf.getInvOpeningBalance());
								tradingInvclosing.setNoOfExpUnitsConvertedToIncUnits(qtyConvertedToIncomeUnit);
								tradingInvclosing.setCalcualtedRate(buyRate);
								genericDAO.saveOrUpdate(tradingInvclosing, user, entityManager); // save closing entry
																									// for openingbal
																									// buy trade
							}
						}
					}
				}
			}
			newSpecifics.setTotalOpeningBalance(totalOpeningBalance);
			newSpecifics.setTotalInvOpeningBalRate(totalExpOpeningBalRate);
			newSpecifics.setTotalInvOpeningBalUnits(totalExpOpeningBalUnits);
			newSpecifics.setTotalInvOpeningBalance(totalExpOpeningBalInvetory);

			List<List<BranchSpecifics>> businessEntityTransactionList = ListUtility
					.getBranchSpecificsTransactionList1(oldBranchSpecifics, newBranchSpecifics);
			for (int b = 0; b < businessEntityTransactionList.size(); b++) {
				if (b == 0) {
					List<BranchSpecifics> oldBnchSpecf = businessEntityTransactionList.get(b);
					if (oldBnchSpecf != null) {
						for (BranchSpecifics bnchSpecifics : oldBnchSpecf) {
							// entityManager.remove(bnchSpecifics); // need soft delete
							bnchSpecifics.setPresentStatus(0); // deactivate by setting presentStatus to 0
							genericDAO.saveOrUpdate(bnchSpecifics, user, entityManager);
						}
					}
				}
				if (b == 1) {
					List<BranchSpecifics> newBnchSpecf = businessEntityTransactionList.get(b);
					if (newBnchSpecf != null) {
						for (BranchSpecifics newbnchSpecifics : newBnchSpecf) {
							newbnchSpecifics.setBudgetDate(Calendar.getInstance().getTime());
							newbnchSpecifics.setPresentStatus(1);// Activate BranchSpecifics by setting presentStatus to
																	// 1
							genericDAO.saveOrUpdate(newbnchSpecifics, user, entityManager);
						}
					}
				}
			}
		}

		// knowledge library start
		if (KLInst != null) {
			SpecificsKnowledgeLibrary specificsKl = new SpecificsKnowledgeLibrary();
			specificsKl.setKnowledgeLibraryContent(KLInst);
			if (KLMandatory != null) {
				if (KLMandatory.equalsIgnoreCase("Yes")) {
					specificsKl.setIsMandatory(1);
				} else {
					specificsKl.setIsMandatory(0);
				}
			}
			specificsKl.setSpecifics(newSpecifics);
			specificsKl.setParticulars(newSpecifics.getParticularsId());
			genericDAO.saveOrUpdate(specificsKl, user, entityManager);

			if (KLBranches != null && !KLBranches.equals("")) {
				String specfklBranchArray[] = KLBranches.split(",");
				for (int j = 0; j < specfklBranchArray.length; j++) {
					SpecificsKnowledgeLibraryForBranch newSpecfKlforBranches = new SpecificsKnowledgeLibraryForBranch();
					Map<String, Object> criterias = new HashMap<String, Object>();
					criterias.put("organization.id", user.getOrganization().getId());
					criterias.put("name", specfklBranchArray[j]);
					criterias.put("presentStatus", 1);
					List<Branch> branches = genericDAO.findByCriteria(Branch.class, criterias, entityManager);
					if (branches != null && branches.size() > 0) {
						Branch bnch = branches.get(0);
						newSpecfKlforBranches.setBranch(bnch);
						newSpecfKlforBranches.setOrganization(bnch.getOrganization());
						newSpecfKlforBranches.setSpecifics(newSpecifics);
						newSpecfKlforBranches.setParticulars(newSpecifics.getParticularsId());
						newSpecfKlforBranches.setSpecificsKl(specificsKl);
						genericDAO.saveOrUpdate(newSpecfKlforBranches, user, entityManager);
					}
				}
			}
		}
		// Knowledge library end

		// Start specifics transaction purpose logic,
		// Sunil: it now applicable for all 4 types of COA
		if (transactionPurposeList != null && !transactionPurposeList.equals("")) {
			String[] itemTxnPurposes = transactionPurposeList.split(",");
			List<SpecificsTransactionPurpose> oldSpecificsTransacion = newSpecifics.getSpecificsTransactionPurposes();
			List<SpecificsTransactionPurpose> newSpecificsTransaction = new ArrayList<SpecificsTransactionPurpose>();
			if (itemTxnPurposes != null) {
				for (int c = 0; c < itemTxnPurposes.length; c++) {
					SpecificsTransactionPurpose newSpecfTxnPurpose = new SpecificsTransactionPurpose();
					Branch newBnch = user.getBranch();
					newSpecfTxnPurpose.setBranch(newBnch);
					newSpecfTxnPurpose.setOrganization(newBnch.getOrganization());
					newSpecfTxnPurpose.setSpecifics(newSpecifics);
					newSpecfTxnPurpose.setParticulars(newSpecifics.getParticularsId());
					TransactionPurpose txnPurpose = TransactionPurpose.findByName(entityManager, itemTxnPurposes[c]);
					newSpecfTxnPurpose.setTransactionPurpose(txnPurpose);
					newSpecificsTransaction.add(newSpecfTxnPurpose);
				}
			}
			List<List<SpecificsTransactionPurpose>> specfTxnPurposeTransactionList = ListUtility
					.getSpecificsTransactionPurposeTransactionList(oldSpecificsTransacion, newSpecificsTransaction);
			for (int b = 0; b < specfTxnPurposeTransactionList.size(); b++) {
				if (b == 0) {
					List<SpecificsTransactionPurpose> oldSpecftxnpurpose = specfTxnPurposeTransactionList.get(b);
					if (oldSpecftxnpurpose != null) {
						for (SpecificsTransactionPurpose txnPurpSpecf : oldSpecftxnpurpose) {
							entityManager.remove(txnPurpSpecf);
						}
					}
				}
				if (b == 1) {
					List<SpecificsTransactionPurpose> newSpecfTxnPurpose = specfTxnPurposeTransactionList.get(b);
					if (newSpecfTxnPurpose != null) {
						for (SpecificsTransactionPurpose newSpecificsTxnPurpose : newSpecfTxnPurpose) {
							genericDAO.saveOrUpdate(newSpecificsTxnPurpose, user, entityManager);
						}
					}
				}
			}
		} // End Transaction Purpose
		if (ConfigParams.getInstance().isDeploymentSingleUser(user)) {
			// For Single User Deployment Only
			singleUserService.updateOnCOACreation(user, newSpecifics, entityManager);
		}
		log.log(Level.FINE, ">>>>>>> End");
		return newSpecifics;
	}

	private static Long getValidatedAccountCode(Long actCode, Users user, Long parentActCode,
			EntityManager entityManager) {
		Map<String, Object> criterias = new HashMap<String, Object>();
		criterias.put("accountCode", actCode);
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		Specifics againfoundSpecf = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
		if (againfoundSpecf != null) {
			actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
		}
		criterias.clear();
		criterias.put("accountCode", actCode);
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		Specifics againfoundSpecf1 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
		if (againfoundSpecf1 != null) {
			actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
		}
		criterias.clear();
		criterias.put("accountCode", actCode);
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		Specifics againfoundSpecf2 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
		if (againfoundSpecf2 != null) {
			actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
		}
		criterias.clear();
		criterias.put("accountCode", actCode);
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		Specifics againfoundSpecf3 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
		if (againfoundSpecf3 != null) {
			actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
		}
		criterias.clear();
		criterias.put("accountCode", actCode);
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		Specifics againfoundSpecf4 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
		if (againfoundSpecf4 != null) {
			actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
		}
		criterias.clear();
		criterias.put("accountCode", actCode);
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		Specifics againfoundSpecf5 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
		if (againfoundSpecf5 != null) {
			actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
		}
		criterias.clear();
		criterias.put("accountCode", actCode);
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		Specifics againfoundSpecf6 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
		if (againfoundSpecf6 != null) {
			actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
		}
		criterias.clear();
		criterias.put("accountCode", actCode);
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		Specifics againfoundSpecf7 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
		if (againfoundSpecf7 != null) {
			actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
		}
		criterias.clear();
		criterias.put("accountCode", actCode);
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		Specifics againfoundSpecf8 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
		if (againfoundSpecf8 != null) {
			actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
		}
		criterias.clear();
		criterias.put("accountCode", actCode);
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		Specifics againfoundSpecf9 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
		if (againfoundSpecf9 != null) {
			actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
		}
		criterias.clear();
		criterias.put("accountCode", actCode);
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		Specifics againfoundSpecf10 = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
		if (againfoundSpecf10 != null) {
			actCode = AccountCodeUtil.generateAccountCode(parentActCode, actCode);
		}
		return actCode;
	}

	@Transactional
	public Result getCoaForBranchWithAllHeads(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		// EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			if (null != user) {
				result = coaService.getCoaForBranchWithAllHeads(result, json, user, entityManager);
			} else {
				log.log(Level.SEVERE, "unauthorized");
				return unauthorized();
			}
		} catch (Exception ex) {
			// if (entitytransaction.isActive()) {
			// entitytransaction.rollback();
			// }
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getCoaForOrganizationWithAllHeads(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		// EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			if (null != user) {
				result = coaService.getCoaForOrganizationWithAllHeads(result, json, user, entityManager);
			} else {
				return unauthorized();
			}
		} catch (Exception ex) {
			// if (entitytransaction.isActive()) {
			// entitytransaction.rollback();
			// }
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result categoryBasedChartOfAccounts(Request request) {
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode branchan = result.putArray("categoryBasedCoaData");
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
			JsonNode json = request.body().asJson();
			String categoryid = json.findValue("categoryId").asText();
			log.log(Level.FINE, ">>>> Start" + json);
			Particulars particulars = Particulars.findById(IdosUtil.convertStringToLong(categoryid));
			List<Specifics> orgSpecificsList = null;
			if (particulars.getAccountCode() == 1000000000000000000L) {
				orgSpecificsList = coaService.getIncomesCoaChildNodes(entityManager, user);
			} else if (particulars.getAccountCode() == 2000000000000000000L) {
				orgSpecificsList = coaService.getExpensesCoaChildNodes(entityManager, user);
			} else if (particulars.getAccountCode() == 3000000000000000000L) {
				orgSpecificsList = coaService.getAssetsCoaChildNodes(entityManager, user);
			} else if (particulars.getAccountCode() == 4000000000000000000L) {
				orgSpecificsList = coaService.getLiabilitiesCoaLeafNodes(entityManager, user);
			}

			/*
			 * criterias.clear();
			 * criterias.put("particularsId.id", particulars.getId());
			 * criterias.put("organization.id", user.getOrganization().getId());
			 * List<Specifics> orgSpecificsList=genericDAO.findByCriteria(Specifics.class,
			 * criterias, entityManager);
			 */
			for (Specifics specifics : orgSpecificsList) {
				ObjectNode row = Json.newObject();
				row.put("id", specifics.getId());
				row.put("name", specifics.getName());
				branchan.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getChartOfAccountsExpenseItems(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode expenseItemsArray = result.putArray("coaItemData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			if (user != null) {
				List<Specifics> expenseItemsList = coaService.getExpensesCoaChildNodes(entityManager, user);
				for (Specifics specf : expenseItemsList) {
					ObjectNode row = Json.newObject();
					ArrayNode tdsItemDetails = row.putArray("tdsItemDetails");
					row.put("id", specf.getId());
					row.put("name", specf.getName() != null ? specf.getName() : "");
					row.put("label", specf.getName() != null ? specf.getName() : "");
					VendorTDSTaxes tdsSpecific = VendorTDSTaxes.isTdsSeupForSpecific(entityManager,
							user.getOrganization().getId(), specf.getId());
					if (tdsSpecific != null) {
						row.put("isTdsSpecific", true);
						ObjectNode tdsRow = Json.newObject();
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
					} else {
						row.put("isTdsSpecific", false);
					}
					expenseItemsArray.add(row);
				}
			} else {
				log.log(Level.SEVERE, "Error: unauthorized access.");
				return unauthorized();
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getChartOfAccountsIncomeItems(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode incomeItemsArray = result.putArray("coaItemData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			if (user != null) {
				List<Specifics> itemsList = coaService.getIncomesCoaChildNodes(entityManager, user);
				for (Specifics specf : itemsList) {
					ObjectNode row = Json.newObject();
					row.put("id", specf.getId());
					row.put("name", specf.getName());
					/*
					 * if(specf.getOpeningBalance()!=null){
					 * row.put("openBalUnits", specf.getOpeningBalance());
					 * }else{
					 */
					row.put("openBalUnits", 0);
					// }
					if (specf.getIncomeSpecfPerUnitPrice() != null) {
						row.put("openBalRate", specf.getIncomeSpecfPerUnitPrice());
					} else {
						row.put("openBalRate", 0);
					}
					if (specf.getIsCompositionScheme() != null) {
						row.put("taxableForComposition", specf.getIsCompositionScheme());
					} else {
						row.put("taxableForComposition", 0);
					}
					incomeItemsArray.add(row);
				}
			} else {
				log.log(Level.SEVERE, "Error: unauthorized access.");
				return unauthorized();
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getChartOfAccountsLiabilitiesItems(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		// ObjectNode result = Json.newObject();
		// ArrayNode incomeItemsArray = result.putArray("coaItemData");
		ObjectNode result = null;
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			if (user != null) {
				result = coaService.getLiabilitiesCoaLeafNodesWithAllHeads(entityManager, user);
				/*
				 * for (Specifics specf : itemsList) {
				 * ObjectNode row = Json.newObject();
				 * row.put("id", specf.getId());
				 * row.put("name", specf.getName());
				 * incomeItemsArray.add(row);
				 * }
				 */
			} else {
				log.log(Level.SEVERE, "Error: unauthorized access.");
				return unauthorized();
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getChartOfAccountsIncomeItemsWithTaxRules(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode incomeItemsArray = result.putArray("incomeItemData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Map<String, Object> criterias = new HashMap<String, Object>(2);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			JsonNode json = request.body().asJson();
			Long branchId = json.findValue("branchId") != null ? json.findValue("branchId").asLong() : null;
			user = getUserInfo(request);
			if (user != null) {
				List<Specifics> parentList = new ArrayList<Specifics>();
				List<Specifics> childList = new ArrayList<Specifics>();
				List<Specifics> itemsList = null;
				if (branchId != null) {
					itemsList = coaService.getSpecificsByBranchAndHeadType(entityManager, user, branchId, 1);
					// to get the Parent List...
					for (Specifics specf : itemsList) {
						if (specf.getParentSpecifics() == null)
							parentList.add(specf);
					}
					for (Specifics specf : itemsList) {
						if (specf.getParentSpecifics() != null) {
							parentList.remove(specf.getParentSpecifics());
							childList.add(specf);
						}
					}
					itemsList.clear();
					itemsList.addAll(parentList);
					itemsList.addAll(childList);
				} else {
					itemsList = coaService.getIncomesCoaChildNodes(entityManager, user);
				}

				for (Specifics specf : itemsList) {
					ObjectNode row = Json.newObject();
					row.put("id", specf.getId());
					row.put("name", specf.getName());
					criterias.clear();
					criterias.put("branch.id", branchId);
					criterias.put("organization.id", user.getOrganization().getId());
					criterias.put("specifics.id", specf.getId());
					criterias.put("presentStatus", 1);
					criterias.put("gstTaxRate", specf.getGstTaxRate());
					List<BranchSpecificsTaxFormula> bnchSpecfTaxFormulaList = genericDAO
							.findByCriteria(BranchSpecificsTaxFormula.class, criterias, entityManager);
					if (bnchSpecfTaxFormulaList != null && bnchSpecfTaxFormulaList.size() > 0) {
						row.put("isTaxSetup", "Yes");
					} else {
						row.put("isTaxSetup", "No");
					}
					incomeItemsArray.add(row);
				}
			} else {
				log.log(Level.SEVERE, "Error: unauthorized access.");
				return unauthorized();
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getCoa4UserByBranch(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = getUserInfo(request);
		if (null == user) {
			return unauthorized();
		}
		try {
			ArrayNode incomeCOAAn = result.putArray("incomeCOAData");
			ArrayNode expenseCOAAn = result.putArray("expenseCOAData");
			ArrayNode assetsCOAAn = result.putArray("assetsCOAData");
			ArrayNode liabilitiesCOAAn = result.putArray("liabilitiesCOAData");
			ArrayNode tableRowIdAn = result.putArray("tableRowIdData");
			ArrayNode projectBranchData = result.putArray("projectData");
			JsonNode json = request.body().asJson();
			String tableRowId = json.findValue("tableRowId").asText();
			Long branchId = json.findValue("txnBranchID") != null ? json.findValue("txnBranchID").asLong() : 0l;
			ObjectNode tableRowIdRow = Json.newObject();
			tableRowIdRow.put("tableRowId", tableRowId);
			tableRowIdAn.add(tableRowIdRow);
			Boolean isProjectReq = false;
			if (json.has("projectReq")) {
				isProjectReq = (json.findValue("projectReq").asText() == null) ? false
						: json.findValue("projectReq").asBoolean();
			}

			if (isProjectReq && branchId != 0l) {
				Map<String, Object> criterias = new HashMap<String, Object>(3);
				criterias.put("projectOrganization.id", user.getOrganization().getId());
				criterias.put("projectBranch.id", branchId);
				criterias.put("presentStatus", 1);
				List<ProjectBranches> projectBranches = genericDAO.findByCriteria(ProjectBranches.class, criterias,
						entityManager);
				if (projectBranches != null && !projectBranches.isEmpty()) {
					result.put("projectresult", true);
					for (ProjectBranches projectBranch : projectBranches) {
						ObjectNode projectRow = Json.newObject();
						projectRow.put("id", projectBranch.getProject().getId());
						projectRow.put("name", projectBranch.getProject().getName());
						projectBranchData.add(projectRow);
					}
				}
			}

			List<Specifics> list = coaService.getIncomeExpenseSpecifics4UserByBranch(entityManager, user, branchId);
			for (Specifics specf : list) {
				ObjectNode row = Json.newObject();
				row.put("id", IdosConstants.HEAD_SPECIFIC + specf.getId());
				row.put("name", specf.getName());
				row.put("isinventory", specf.getIsTradingInvenotryItem());
				row.put("headType", IdosConstants.HEAD_SPECIFIC);
				if (specf.getAccountCodeHirarchy().startsWith("/1")) {
					incomeCOAAn.add(row);
				} else if (specf.getAccountCodeHirarchy().startsWith("/2")) {
					expenseCOAAn.add(row);
				}
			}
			List<Specifics> assetsList = coaService.getAssetsCoaChildNodes(entityManager, user);
			for (Specifics specfics : assetsList) {
				boolean isChildNodeAdded = coaService.getAssetsCoaNodes(assetsCOAAn, branchId, entityManager, user,
						specfics);
				if (!isChildNodeAdded && coaService.checkToAddMappedItem(specfics)) {
					ObjectNode assetsRow = Json.newObject();
					assetsRow.put("id", IdosConstants.HEAD_SPECIFIC + specfics.getId());
					assetsRow.put("name", specfics.getName());
					assetsRow.put("headType", IdosConstants.HEAD_SPECIFIC);
					assetsCOAAn.add(assetsRow);
				}
			}

			List<Specifics> liabilitiesList = coaService.getLiabilitiesCoaLeafNodes(entityManager, user);
			for (Specifics specfics : liabilitiesList) {
				boolean isChildNodeAdded = coaService.getLiabilitiesCoaNodes(liabilitiesCOAAn, branchId, entityManager,
						user, specfics);
				if (!isChildNodeAdded && coaService.checkToAddMappedItem(specfics)) {
					ObjectNode row = Json.newObject();
					row.put("id", IdosConstants.HEAD_SPECIFIC + specfics.getId());
					row.put("name", specfics.getName());
					row.put("headType", IdosConstants.HEAD_SPECIFIC);
					liabilitiesCOAAn.add(row);
				}
			}

		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getCoaItems4UserByBranch(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = getUserInfo(request);
		if (null == user) {
			return unauthorized();
		}
		try {
			ArrayNode incomeCOAAn = result.putArray("incomeCOAData");
			ArrayNode expenseCOAAn = result.putArray("expenseCOAData");
			ArrayNode assetsCOAAn = result.putArray("assetsCOAData");
			ArrayNode liabilitiesCOAAn = result.putArray("liabilitiesCOAData");
			// ArrayNode tableRowIdAn = result.putArray("tableRowIdData");
			// ArrayNode projectBranchData = result.putArray("projectData");
			JsonNode json = request.body().asJson();
			Long branchId = json.findValue("branchid") != null ? json.findValue("branchid").asLong() : 0l;
			String itemTypeFlag = json.findValue("itemType") != null ? json.findValue("itemType").asText() : "";
			char[] flagsArr = itemTypeFlag.toCharArray();
			if (flagsArr.length > 0 && flagsArr[0] == '1') {
				List<Specifics> list = coaService.getIncomeOrExpenseSpecifics4UserByBranch(entityManager, user,
						branchId, true);
				for (Specifics specf : list) {
					ObjectNode row = Json.newObject();
					row.put("id", specf.getId());
					row.put("name", specf.getName());
					if (specf.getIsTradingInvenotryItem() != null)
						row.put("isinventory", specf.getIsTradingInvenotryItem());
					else
						row.put("isinventory", "");
					row.put("accountCode", specf.getParticularsId().getAccountCode());
					if (specf.getGstItemCategory() != null) {
						row.put("category", specf.getGstItemCategory());
					} else {
						row.put("category", "");
					}
					row.put("iseditable",
							specf.getIsTransactionEditable() == null ? 0 : specf.getIsTransactionEditable());
					row.put("isCombinationSales",
							specf.getIsCombinationSales() == null ? 0 : specf.getIsCombinationSales());
					incomeCOAAn.add(row);
				}
			}
			if (flagsArr.length > 1 && flagsArr[1] == '1') {
				List<Specifics> list = coaService.getIncomeOrExpenseSpecifics4UserByBranch(entityManager, user,
						branchId, false);
				for (Specifics specf : list) {
					ObjectNode row = Json.newObject();
					row.put("id", specf.getId());
					row.put("name", specf.getName());
					if (specf.getIsTradingInvenotryItem() != null)
						row.put("isinventory", specf.getIsTradingInvenotryItem());
					else
						row.put("isinventory", "");
					row.put("accountCode", specf.getParticularsId().getAccountCode());
					if (specf.getGstItemCategory() != null) {
						row.put("category", specf.getGstItemCategory());
					} else {
						row.put("category", "");
					}
					row.put("iseditable",
							specf.getIsTransactionEditable() == null ? 0 : specf.getIsTransactionEditable());
					row.put("isCombinationSales",
							specf.getIsCombinationSales() == null ? 0 : specf.getIsCombinationSales());
					expenseCOAAn.add(row);
				}
			}

			if (flagsArr.length > 2 && flagsArr[2] == '1') {
				List<Specifics> assetsList = coaService.getAssetsCoaChildNodes(entityManager, user);
				for (Specifics specfics : assetsList) {
					boolean isChildNodeAdded = coaService.getAssetsCoaNodes(assetsCOAAn, branchId, entityManager, user,
							specfics);
					if (!isChildNodeAdded) {
						ObjectNode assetsRow = Json.newObject();
						assetsRow.put("id", specfics.getId());
						assetsRow.put("name", specfics.getName());
						assetsCOAAn.add(assetsRow);
					}
				}
			}
			if (flagsArr.length > 3 && flagsArr[3] == '1') {
				List<Specifics> liabilitiesList = coaService.getLiabilitiesCoaLeafNodes(entityManager, user);
				for (Specifics specfics : liabilitiesList) {
					boolean isChildNodeAdded = coaService.getLiabilitiesCoaNodes(liabilitiesCOAAn, branchId,
							entityManager, user, specfics);
					if (!isChildNodeAdded) {
						ObjectNode row = Json.newObject();
						row.put("id", specfics.getId());
						row.put("name", specfics.getName());
						liabilitiesCOAAn.add(row);
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getChartOfAccountsExpenceItemsBranchwise(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode expenceItemsArray = result.putArray("expenceItemData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Map<String, Object> criterias = new HashMap<String, Object>(2);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			JsonNode json = request.body().asJson();
			Long branchId = json.findValue("branchId").asLong();
			user = getUserInfo(request);
			if (user != null) {
				List<Specifics> parentList = new ArrayList<Specifics>();
				List<Specifics> childList = new ArrayList<Specifics>();
				List<Specifics> itemsList = coaService.getSpecificsByBranchAndHeadType(entityManager, user, branchId,
						2);
				// to get the Parent List...
				for (Specifics specf : itemsList) {
					if (specf.getParentSpecifics() == null)
						parentList.add(specf);
				}
				for (Specifics specf : itemsList) {
					if (specf.getParentSpecifics() != null) {
						parentList.remove(specf.getParentSpecifics());
						childList.add(specf);
					}
				}
				itemsList.clear();
				itemsList.addAll(parentList);
				itemsList.addAll(childList);

				for (Specifics specf : itemsList) {
					if (specf.getIdentificationForDataValid() == null) {
						ObjectNode row = Json.newObject();
						row.put("id", specf.getId());
						row.put("name", specf.getName());
						expenceItemsArray.add(row);
					}
				}
			} else {
				log.log(Level.SEVERE, "Error: unauthorized access.");
				return unauthorized();
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getAllCoaUnitsofOrg(Request request) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> Start");
		Users user = getUserInfo(request);
		if (user == null) {
			log.log(Level.SEVERE, "Error: unauthorized access.");
			return unauthorized();
		}
		// EntityManager em =getEntityManager();
		ObjectNode result = Json.newObject();
		try {
			ArrayNode coaArrayNode = result.putArray("coaUnitsList");
			coaService.getAllCoaUnitForOrg(entityManager, user, (int) IdosConstants.EXPENSE, coaArrayNode);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "VendorController BranchProject", "VendorController BranchProject",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getChartOfAccountsExpenceItemsWithTaxRules(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode expenceItemsArray = result.putArray("expenceItemData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Map<String, Object> criterias = new HashMap<String, Object>(2);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			JsonNode json = request.body().asJson();
			Long branchId = json.findValue("branchId").asLong();
			user = getUserInfo(request);
			if (user != null) {
				List<Specifics> parentList = new ArrayList<Specifics>();
				List<Specifics> childList = new ArrayList<Specifics>();
				List<Specifics> itemsList = coaService.getSpecificsByBranchAndHeadType(entityManager, user, branchId,
						2);
				// to get the Parent List...
				for (Specifics specf : itemsList) {
					if (specf.getParentSpecifics() == null)
						parentList.add(specf);
				}
				for (Specifics specf : itemsList) {
					if (specf.getParentSpecifics() != null) {
						parentList.remove(specf.getParentSpecifics());
						childList.add(specf);
					}
				}
				itemsList.clear();
				itemsList.addAll(parentList);
				itemsList.addAll(childList);

				for (Specifics specf : itemsList) {
					ObjectNode row = Json.newObject();
					row.put("id", specf.getId());
					row.put("name", specf.getName());
					criterias.clear();
					criterias.put("branch.id", branchId);
					criterias.put("organization.id", user.getOrganization().getId());
					criterias.put("specifics.id", specf.getId());
					criterias.put("presentStatus", 1);
					criterias.put("gstTaxRate", specf.getGstTaxRate());
					List<BranchSpecificsTaxFormula> bnchSpecfTaxFormulaList = genericDAO
							.findByCriteria(BranchSpecificsTaxFormula.class, criterias, entityManager);
					if (bnchSpecfTaxFormulaList != null && bnchSpecfTaxFormulaList.size() > 0) {
						row.put("isTaxSetup", "Yes");
					} else {
						row.put("isTaxSetup", "No");
					}
					expenceItemsArray.add(row);
				}
			} else {
				log.log(Level.SEVERE, "Error: unauthorized access.");
				return unauthorized();
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

}
