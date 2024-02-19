package controllers;

import java.io.File;
import play.libs.Files.TemporaryFile;
import java.io.InputStream;
import java.util.*;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Branch;
import model.BranchSpecifics;
import model.Organization;
import model.Specifics;
import model.Users;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.logging.Level;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.IdosUtil;

import com.typesafe.config.Config;
import play.mvc.Http.Request;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.Application;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import views.html.*;

public class BudgetController extends StaticController {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	public static Application application;
	public Request request;

	@Inject
	public BudgetController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
		this.application = application;
	}

	@Transactional
	public Result budgetTemplate(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		ObjectNode results = Json.newObject();
		ArrayNode an = results.putArray("budgetFileCred");
		Users user = null;
		File file = null;
		try {
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			// session.adding("email", useremail);
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			String sheetName = user.getOrganization().getName().replaceAll("\\s", "") + "Budget";
			String path = application.path().toString() + "/logs/BudgetExcel/";
			String fileName = excelService.createbudgetexcel(user.getOrganization(), entityManager, path, sheetName);
			/*
			 * ObjectNode row = Json.newObject();
			 * row.put("fileName", fileName);
			 * an.add(row);
			 */

			file = new File(path + fileName);
			return Results.ok(file).withHeader("ContentType", "application/xlsx").withHeader("Content-Disposition",
					"attachment; filename=" + fileName);

		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
	}

	@Transactional
	public Result uploadBudget(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		ObjectNode results = Json.newObject();
		ArrayNode an = results.putArray("budgetFileCred");
		Users user = getUserInfo(request);
		try {
			transaction.begin();
			MultipartFormData<File> body = request.body().asMultipartFormData();
			List<FilePart<File>> chartofaccount = body.getFiles();
			for (FilePart<File> filePart : chartofaccount) {
				String fileName = filePart.getFilename();
				String contentType = filePart.getContentType();
				// File file = filePart.getRef().toFile();
				TemporaryFile temporaryFile = (TemporaryFile) filePart.getRef();
				String filePath = temporaryFile.path().toString();
				File file = new File(filePath);
				InputStream is = new java.io.FileInputStream(file);
				try {
					Organization org = user.getOrganization();
					int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
					int finStartMonth = 4;
					int finEndMonth = 3;
					String finStartDate = null;
					String finStDt = null;
					StringBuilder startYear = null;
					String finEndDate = null;
					String finEndDt = null;
					StringBuilder endYear = null;
					if (org.getFinancialStartDate() != null) {
						finStartMonth = org.getFinancialStartDate().getMonth() + 1;
					}
					if (org.getFinancialEndDate() != null) {
						finEndMonth = org.getFinancialEndDate().getMonth() + 1;
					}
					if (currentMonth < finStartMonth) {
						startYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - 1));
						endYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
					} else {
						startYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
						endYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) + 1));
					}
					if (org.getFinancialStartDate() != null && !org.getFinancialStartDate().equals("")) {
						finStDt = StaticController.idosmdtdf.format(org.getFinancialStartDate()) + "," + startYear;
						finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
					} else {
						finStDt = "Apr 01" + "," + startYear;
						finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
					}
					if (org.getFinancialEndDate() != null && !org.getFinancialEndDate().equals("")) {
						finEndDt = StaticController.idosmdtdf.format(org.getFinancialEndDate()) + "," + endYear;
						finEndDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
					} else {
						finEndDt = "Mar 31" + "," + endYear;
						finEndDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
					}
					XSSFWorkbook wb = new XSSFWorkbook(is);
					int numOfSheets = wb.getNumberOfSheets();
					for (int i = 0; i < numOfSheets; i++) {
						XSSFSheet sheet = wb.getSheetAt(i);
						Iterator rows = sheet.rowIterator();
						rows.next();
						int rowCount = 0;
						Branch rowBranch = null;
						while (rows.hasNext()) {
							rowCount++;
							if (rowCount == 1) {
								// save budget in branch table i.e budget is for branch
								XSSFRow row = (XSSFRow) rows.next();
								Iterator cellIter = row.cellIterator();
								int cellCount = 1;
								while (cellIter.hasNext()) {
									XSSFCell myCell = (XSSFCell) cellIter.next();
									String cellValue = myCell.toString();
									switch (cellCount) {
										case 1:
											Map<String, Object> criterias = new HashMap<String, Object>();
											// int p=cellValue.lastIndexOf("-");
											// String branchName=cellValue.substring(0,p);
											String branchName = cellValue;
											criterias.put("name", branchName);
											criterias.put("organization.id", user.getOrganization().getId());
											criterias.put("presentStatus", 1);
											rowBranch = genericDAO.getByCriteria(Branch.class, criterias,
													entityManager);
											rowBranch.setBudgetDate(Calendar.getInstance().getTime());
											break;
										case 2:
											FormulaEvaluator formulaEval2 = wb.getCreationHelper()
													.createFormulaEvaluator();
											cellValue = formulaEval2.evaluate(myCell).formatAsString();
											if (rowBranch.getBudgetDate()
													.compareTo(StaticController.mysqldf.parse(finStartDate)) <= 0
													&& rowBranch.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													rowBranch.setBudgetAmountJan(
															IdosUtil.convertStringToDouble(cellValue));
													rowBranch.setBudgetDeductedAmountJan(null);
												} else {
													rowBranch.setBudgetAmountJan(null);
													rowBranch.setBudgetDeductedAmountJan(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (rowBranch.getBudgetAmountJan() != null) {
														rowBranch.setBudgetAmountJan(IdosUtil.convertStringToDouble(
																decimalFormat.format(rowBranch.getBudgetAmountJan()
																		+ IdosUtil.convertStringToDouble(cellValue))));
													} else {
														rowBranch.setBudgetAmountJan(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 3:
											FormulaEvaluator formulaEval3 = wb.getCreationHelper()
													.createFormulaEvaluator();
											cellValue = formulaEval3.evaluate(myCell).formatAsString();
											if (rowBranch.getBudgetDate()
													.compareTo(StaticController.mysqldf.parse(finStartDate)) <= 0
													&& rowBranch.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													rowBranch.setBudgetAmountFeb(
															IdosUtil.convertStringToDouble(cellValue));
													rowBranch.setBudgetDeductedAmountFeb(null);
												} else {
													rowBranch.setBudgetAmountFeb(null);
													rowBranch.setBudgetDeductedAmountFeb(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (rowBranch.getBudgetAmountFeb() != null) {
														rowBranch.setBudgetAmountFeb(IdosUtil.convertStringToDouble(
																decimalFormat.format(rowBranch.getBudgetAmountFeb()
																		+ IdosUtil.convertStringToDouble(cellValue))));
													} else {
														rowBranch.setBudgetAmountFeb(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 4:
											FormulaEvaluator formulaEval4 = wb.getCreationHelper()
													.createFormulaEvaluator();
											cellValue = formulaEval4.evaluate(myCell).formatAsString();
											if (rowBranch.getBudgetDate()
													.compareTo(StaticController.mysqldf.parse(finStartDate)) <= 0
													&& rowBranch.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													rowBranch.setBudgetAmountMar(
															IdosUtil.convertStringToDouble(cellValue));
													rowBranch.setBudgetDeductedAmountMar(null);
												} else {
													rowBranch.setBudgetAmountMar(null);
													rowBranch.setBudgetDeductedAmountMar(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (rowBranch.getBudgetAmountMar() != null) {
														rowBranch.setBudgetAmountMar(IdosUtil.convertStringToDouble(
																decimalFormat.format(rowBranch.getBudgetAmountMar()
																		+ IdosUtil.convertStringToDouble(cellValue))));
													} else {
														rowBranch.setBudgetAmountMar(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 5:
											FormulaEvaluator formulaEval5 = wb.getCreationHelper()
													.createFormulaEvaluator();
											cellValue = formulaEval5.evaluate(myCell).formatAsString();
											if (rowBranch.getBudgetDate()
													.compareTo(StaticController.mysqldf.parse(finStartDate)) <= 0
													&& rowBranch.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													rowBranch.setBudgetAmountApr(
															IdosUtil.convertStringToDouble(cellValue));
													rowBranch.setBudgetDeductedAmountApr(null);
												} else {
													rowBranch.setBudgetAmountApr(null);
													rowBranch.setBudgetDeductedAmountApr(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (rowBranch.getBudgetAmountApr() != null) {
														rowBranch.setBudgetAmountApr(IdosUtil.convertStringToDouble(
																decimalFormat.format(rowBranch.getBudgetAmountApr()
																		+ IdosUtil.convertStringToDouble(cellValue))));
													} else {
														rowBranch.setBudgetAmountApr(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 6:
											FormulaEvaluator formulaEval6 = wb.getCreationHelper()
													.createFormulaEvaluator();
											cellValue = formulaEval6.evaluate(myCell).formatAsString();
											if (rowBranch.getBudgetDate()
													.compareTo(StaticController.mysqldf.parse(finStartDate)) <= 0
													&& rowBranch.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													rowBranch.setBudgetAmountMay(
															IdosUtil.convertStringToDouble(cellValue));
													rowBranch.setBudgetDeductedAmountMay(null);
												} else {
													rowBranch.setBudgetAmountMay(null);
													rowBranch.setBudgetDeductedAmountMay(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (rowBranch.getBudgetAmountMay() != null) {
														rowBranch.setBudgetAmountMay(IdosUtil.convertStringToDouble(
																decimalFormat.format(rowBranch.getBudgetAmountMay()
																		+ IdosUtil.convertStringToDouble(cellValue))));
													} else {
														rowBranch.setBudgetAmountMay(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 7:
											FormulaEvaluator formulaEval7 = wb.getCreationHelper()
													.createFormulaEvaluator();
											cellValue = formulaEval7.evaluate(myCell).formatAsString();
											if (rowBranch.getBudgetDate()
													.compareTo(StaticController.mysqldf.parse(finStartDate)) <= 0
													&& rowBranch.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													rowBranch.setBudgetAmountJune(
															IdosUtil.convertStringToDouble(cellValue));
													rowBranch.setBudgetDeductedAmountJune(null);
												} else {
													rowBranch.setBudgetAmountJune(null);
													rowBranch.setBudgetDeductedAmountJune(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (rowBranch.getBudgetAmountJune() != null) {
														rowBranch.setBudgetAmountJune(IdosUtil.convertStringToDouble(
																decimalFormat.format(rowBranch.getBudgetAmountJune()
																		+ IdosUtil.convertStringToDouble(cellValue))));
													} else {
														rowBranch.setBudgetAmountJune(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 8:
											FormulaEvaluator formulaEval8 = wb.getCreationHelper()
													.createFormulaEvaluator();
											cellValue = formulaEval8.evaluate(myCell).formatAsString();
											if (rowBranch.getBudgetDate()
													.compareTo(StaticController.mysqldf.parse(finStartDate)) <= 0
													&& rowBranch.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													rowBranch.setBudgetAmountJuly(
															IdosUtil.convertStringToDouble(cellValue));
													rowBranch.setBudgetDeductedAmountJuly(null);
												} else {
													rowBranch.setBudgetAmountJuly(null);
													rowBranch.setBudgetDeductedAmountJuly(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (rowBranch.getBudgetAmountJuly() != null) {
														rowBranch.setBudgetAmountJuly(IdosUtil.convertStringToDouble(
																decimalFormat.format(rowBranch.getBudgetAmountJuly()
																		+ IdosUtil.convertStringToDouble(cellValue))));
													} else {
														rowBranch.setBudgetAmountJuly(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 9:
											FormulaEvaluator formulaEval9 = wb.getCreationHelper()
													.createFormulaEvaluator();
											cellValue = formulaEval9.evaluate(myCell).formatAsString();
											if (rowBranch.getBudgetDate()
													.compareTo(StaticController.mysqldf.parse(finStartDate)) <= 0
													&& rowBranch.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													rowBranch.setBudgetAmountAug(
															IdosUtil.convertStringToDouble(cellValue));
													rowBranch.setBudgetDeductedAmountAug(null);
												} else {
													rowBranch.setBudgetAmountAug(null);
													rowBranch.setBudgetDeductedAmountAug(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (rowBranch.getBudgetAmountAug() != null) {
														rowBranch.setBudgetAmountAug(IdosUtil.convertStringToDouble(
																decimalFormat.format(rowBranch.getBudgetAmountAug()
																		+ IdosUtil.convertStringToDouble(cellValue))));
													} else {
														rowBranch.setBudgetAmountAug(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 10:
											FormulaEvaluator formulaEval10 = wb.getCreationHelper()
													.createFormulaEvaluator();
											cellValue = formulaEval10.evaluate(myCell).formatAsString();
											if (rowBranch.getBudgetDate()
													.compareTo(StaticController.mysqldf.parse(finStartDate)) <= 0
													&& rowBranch.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													rowBranch.setBudgetAmountSep(
															IdosUtil.convertStringToDouble(cellValue));
													rowBranch.setBudgetDeductedAmountSep(null);
												} else {
													rowBranch.setBudgetAmountSep(null);
													rowBranch.setBudgetDeductedAmountSep(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (rowBranch.getBudgetAmountSep() != null) {
														rowBranch.setBudgetAmountSep(IdosUtil.convertStringToDouble(
																decimalFormat.format(rowBranch.getBudgetAmountSep()
																		+ IdosUtil.convertStringToDouble(cellValue))));
													} else {
														rowBranch.setBudgetAmountSep(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 11:
											FormulaEvaluator formulaEval11 = wb.getCreationHelper()
													.createFormulaEvaluator();
											cellValue = formulaEval11.evaluate(myCell).formatAsString();
											if (rowBranch.getBudgetDate()
													.compareTo(StaticController.mysqldf.parse(finStartDate)) <= 0
													&& rowBranch.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													rowBranch.setBudgetAmountOct(
															IdosUtil.convertStringToDouble(cellValue));
													rowBranch.setBudgetDeductedAmountOct(null);
												} else {
													rowBranch.setBudgetAmountOct(null);
													rowBranch.setBudgetDeductedAmountOct(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (rowBranch.getBudgetAmountOct() != null) {
														rowBranch.setBudgetAmountOct(IdosUtil.convertStringToDouble(
																decimalFormat.format(rowBranch.getBudgetAmountOct()
																		+ IdosUtil.convertStringToDouble(cellValue))));
													} else {
														rowBranch.setBudgetAmountOct(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 12:
											FormulaEvaluator formulaEval12 = wb.getCreationHelper()
													.createFormulaEvaluator();
											cellValue = formulaEval12.evaluate(myCell).formatAsString();
											if (rowBranch.getBudgetDate()
													.compareTo(StaticController.mysqldf.parse(finStartDate)) <= 0
													&& rowBranch.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													rowBranch.setBudgetAmountNov(
															IdosUtil.convertStringToDouble(cellValue));
													rowBranch.setBudgetDeductedAmountNov(null);
												} else {
													rowBranch.setBudgetAmountNov(null);
													rowBranch.setBudgetDeductedAmountNov(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (rowBranch.getBudgetAmountNov() != null) {
														rowBranch.setBudgetAmountNov(IdosUtil.convertStringToDouble(
																decimalFormat.format(rowBranch.getBudgetAmountNov()
																		+ IdosUtil.convertStringToDouble(cellValue))));
													} else {
														rowBranch.setBudgetAmountNov(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 13:
											FormulaEvaluator formulaEval13 = wb.getCreationHelper()
													.createFormulaEvaluator();
											cellValue = formulaEval13.evaluate(myCell).formatAsString();
											if (rowBranch.getBudgetDate()
													.compareTo(StaticController.mysqldf.parse(finStartDate)) <= 0
													&& rowBranch.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													rowBranch.setBudgetAmountDec(
															IdosUtil.convertStringToDouble(cellValue));
													rowBranch.setBudgetDeductedAmountDec(null);
												} else {
													rowBranch.setBudgetAmountDec(null);
													rowBranch.setBudgetDeductedAmountDec(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (rowBranch.getBudgetAmountDec() != null) {
														rowBranch.setBudgetAmountDec(IdosUtil.convertStringToDouble(
																decimalFormat.format(rowBranch.getBudgetAmountDec()
																		+ IdosUtil.convertStringToDouble(cellValue))));
													} else {
														rowBranch.setBudgetAmountDec(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 14:
											FormulaEvaluator formulaEval14 = wb.getCreationHelper()
													.createFormulaEvaluator();
											cellValue = formulaEval14.evaluate(myCell).formatAsString();
											if (rowBranch.getBudgetDate()
													.compareTo(StaticController.mysqldf.parse(finStartDate)) <= 0
													&& rowBranch.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													rowBranch.setBudgetTotal(IdosUtil.convertStringToDouble(cellValue));
													rowBranch.setBudgetDeductedTotal(null);
												} else {
													rowBranch.setBudgetTotal(null);
													rowBranch.setBudgetDeductedTotal(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (rowBranch.getBudgetTotal() != null) {
														rowBranch.setBudgetTotal(IdosUtil.convertStringToDouble(
																decimalFormat.format(rowBranch.getBudgetTotal()
																		+ IdosUtil.convertStringToDouble(cellValue))));
													} else {
														rowBranch.setBudgetTotal(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
									}
									genericDAO.saveOrUpdate(rowBranch, user, entityManager);
									cellCount++;
								}
							} else {
								// save budget in branchspecifics table i.e budget is for branch specifics
								XSSFRow row = (XSSFRow) rows.next();
								Iterator cellIter = row.cellIterator();
								int cellCount = 1;
								Specifics specifics = null;
								BranchSpecifics branchSpecifics = null;
								while (cellIter.hasNext()) {
									XSSFCell myCell = (XSSFCell) cellIter.next();
									String cellValue = myCell.toString();
									switch (cellCount) {
										case 1:
											Map<String, Object> criterias = new HashMap<String, Object>();
											cellValue = cellValue.replaceAll("&", "&amp;");
											criterias.put("name", cellValue);
											criterias.put("organization.id", user.getOrganization().getId());
											criterias.put("presentStatus", 1);
											specifics = genericDAO.getByCriteria(Specifics.class, criterias,
													entityManager);
											criterias.clear();
											criterias.put("branch.id", rowBranch.getId());
											criterias.put("organization.id", user.getOrganization().getId());
											if (specifics != null) {
												criterias.put("specifics.id", specifics.getId());
												if (specifics.getParticularsId() != null)
													criterias.put("particular.id",
															specifics.getParticularsId().getId());
											}
											criterias.put("presentStatus", 1);
											branchSpecifics = genericDAO.getByCriteria(BranchSpecifics.class, criterias,
													entityManager);
											if (branchSpecifics != null) {
												branchSpecifics.setBudgetDate(Calendar.getInstance().getTime());
											}
											break;
										case 2:
											if (branchSpecifics != null
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finStartDate)) <= 0
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													branchSpecifics.setBudgetAmountJan(
															IdosUtil.convertStringToDouble(cellValue));
													branchSpecifics.setBudgetDeductedAmountJan(null);
												} else {
													branchSpecifics.setBudgetAmountJan(null);
													branchSpecifics.setBudgetDeductedAmountJan(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (branchSpecifics.getBudgetAmountJan() != null) {
														branchSpecifics.setBudgetAmountJan(
																IdosUtil.convertStringToDouble(decimalFormat.format(
																		branchSpecifics.getBudgetAmountJan() + IdosUtil
																				.convertStringToDouble(cellValue))));
													} else {
														branchSpecifics.setBudgetAmountJan(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 3:
											if (branchSpecifics != null
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finStartDate)) <= 0
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													branchSpecifics.setBudgetAmountFeb(
															IdosUtil.convertStringToDouble(cellValue));
													branchSpecifics.setBudgetDeductedAmountFeb(null);
												} else {
													branchSpecifics.setBudgetAmountFeb(null);
													branchSpecifics.setBudgetDeductedAmountFeb(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (branchSpecifics.getBudgetAmountFeb() != null) {
														branchSpecifics.setBudgetAmountFeb(
																IdosUtil.convertStringToDouble(decimalFormat.format(
																		branchSpecifics.getBudgetAmountFeb() + IdosUtil
																				.convertStringToDouble(cellValue))));
													} else {
														branchSpecifics.setBudgetAmountFeb(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 4:
											if (branchSpecifics != null
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finStartDate)) <= 0
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													branchSpecifics.setBudgetAmountMar(
															IdosUtil.convertStringToDouble(cellValue));
													branchSpecifics.setBudgetDeductedAmountMar(null);
												} else {
													branchSpecifics.setBudgetAmountMar(null);
													branchSpecifics.setBudgetDeductedAmountMar(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (branchSpecifics.getBudgetAmountMar() != null) {
														branchSpecifics.setBudgetAmountMar(
																IdosUtil.convertStringToDouble(decimalFormat.format(
																		branchSpecifics.getBudgetAmountMar() + IdosUtil
																				.convertStringToDouble(cellValue))));
													} else {
														branchSpecifics.setBudgetAmountMar(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 5:
											if (branchSpecifics != null
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finStartDate)) <= 0
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													branchSpecifics.setBudgetAmountApr(
															IdosUtil.convertStringToDouble(cellValue));
													branchSpecifics.setBudgetDeductedAmountApr(null);
												} else {
													branchSpecifics.setBudgetAmountApr(null);
													branchSpecifics.setBudgetDeductedAmountApr(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (branchSpecifics.getBudgetAmountApr() != null) {
														branchSpecifics.setBudgetAmountApr(
																IdosUtil.convertStringToDouble(decimalFormat.format(
																		branchSpecifics.getBudgetAmountApr() + IdosUtil
																				.convertStringToDouble(cellValue))));
													} else {
														branchSpecifics.setBudgetAmountApr(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 6:
											if (branchSpecifics != null
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finStartDate)) <= 0
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													branchSpecifics.setBudgetAmountMay(
															IdosUtil.convertStringToDouble(cellValue));
													branchSpecifics.setBudgetDeductedAmountMay(null);
												} else {
													branchSpecifics.setBudgetAmountMay(null);
													branchSpecifics.setBudgetDeductedAmountMay(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (branchSpecifics.getBudgetAmountMay() != null) {
														branchSpecifics.setBudgetAmountMay(
																IdosUtil.convertStringToDouble(decimalFormat.format(
																		branchSpecifics.getBudgetAmountMay() + IdosUtil
																				.convertStringToDouble(cellValue))));
													} else {
														branchSpecifics.setBudgetAmountMay(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 7:
											if (branchSpecifics != null
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finStartDate)) <= 0
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													branchSpecifics.setBudgetAmountJune(
															IdosUtil.convertStringToDouble(cellValue));
													branchSpecifics.setBudgetDeductedAmountJune(null);
												} else {
													branchSpecifics.setBudgetAmountJune(null);
													branchSpecifics.setBudgetDeductedAmountJune(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (branchSpecifics.getBudgetAmountJune() != null) {
														branchSpecifics.setBudgetAmountJune(
																IdosUtil.convertStringToDouble(decimalFormat.format(
																		branchSpecifics.getBudgetAmountJune() + IdosUtil
																				.convertStringToDouble(cellValue))));
													} else {
														branchSpecifics.setBudgetAmountJune(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 8:
											if (branchSpecifics != null
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finStartDate)) <= 0
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													branchSpecifics.setBudgetAmountJuly(
															IdosUtil.convertStringToDouble(cellValue));
													branchSpecifics.setBudgetDeductedAmountJuly(null);
												} else {
													branchSpecifics.setBudgetAmountJuly(null);
													branchSpecifics.setBudgetDeductedAmountJuly(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (branchSpecifics.getBudgetAmountJuly() != null) {
														branchSpecifics.setBudgetAmountJuly(
																IdosUtil.convertStringToDouble(decimalFormat.format(
																		branchSpecifics.getBudgetAmountJuly() + IdosUtil
																				.convertStringToDouble(cellValue))));
													} else {
														branchSpecifics.setBudgetAmountJuly(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 9:
											if (branchSpecifics != null
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finStartDate)) <= 0
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													branchSpecifics.setBudgetAmountAug(
															IdosUtil.convertStringToDouble(cellValue));
													branchSpecifics.setBudgetDeductedAmountAug(null);
												} else {
													branchSpecifics.setBudgetAmountAug(null);
													branchSpecifics.setBudgetDeductedAmountAug(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (branchSpecifics.getBudgetAmountAug() != null) {
														branchSpecifics.setBudgetAmountAug(
																IdosUtil.convertStringToDouble(decimalFormat.format(
																		branchSpecifics.getBudgetAmountAug() + IdosUtil
																				.convertStringToDouble(cellValue))));
													} else {
														branchSpecifics.setBudgetAmountAug(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 10:
											if (branchSpecifics != null
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finStartDate)) <= 0
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													branchSpecifics.setBudgetAmountSep(
															IdosUtil.convertStringToDouble(cellValue));
													branchSpecifics.setBudgetDeductedAmountSep(null);
												} else {
													branchSpecifics.setBudgetAmountSep(null);
													branchSpecifics.setBudgetDeductedAmountSep(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (branchSpecifics.getBudgetAmountSep() != null) {
														branchSpecifics.setBudgetAmountSep(
																IdosUtil.convertStringToDouble(decimalFormat.format(
																		branchSpecifics.getBudgetAmountSep() + IdosUtil
																				.convertStringToDouble(cellValue))));
													} else {
														branchSpecifics.setBudgetAmountSep(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 11:
											if (branchSpecifics != null
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finStartDate)) <= 0
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													branchSpecifics.setBudgetAmountOct(
															IdosUtil.convertStringToDouble(cellValue));
													branchSpecifics.setBudgetDeductedAmountOct(null);
												} else {
													branchSpecifics.setBudgetAmountOct(null);
													branchSpecifics.setBudgetDeductedAmountOct(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (branchSpecifics.getBudgetAmountOct() != null) {
														branchSpecifics.setBudgetAmountOct(
																IdosUtil.convertStringToDouble(decimalFormat.format(
																		branchSpecifics.getBudgetAmountOct() + IdosUtil
																				.convertStringToDouble(cellValue))));
													} else {
														branchSpecifics.setBudgetAmountOct(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 12:
											if (branchSpecifics != null
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finStartDate)) <= 0
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													branchSpecifics.setBudgetAmountNov(
															IdosUtil.convertStringToDouble(cellValue));
													branchSpecifics.setBudgetDeductedAmountNov(null);
												} else {
													branchSpecifics.setBudgetAmountNov(null);
													branchSpecifics.setBudgetDeductedAmountNov(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (branchSpecifics.getBudgetAmountNov() != null) {
														branchSpecifics.setBudgetAmountNov(
																IdosUtil.convertStringToDouble(decimalFormat.format(
																		branchSpecifics.getBudgetAmountNov() + IdosUtil
																				.convertStringToDouble(cellValue))));
													} else {
														branchSpecifics.setBudgetAmountNov(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 13:
											if (branchSpecifics != null
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finStartDate)) <= 0
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													branchSpecifics.setBudgetAmountDec(
															IdosUtil.convertStringToDouble(cellValue));
													branchSpecifics.setBudgetDeductedAmountDec(null);
												} else {
													branchSpecifics.setBudgetAmountDec(null);
													branchSpecifics.setBudgetDeductedAmountDec(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (branchSpecifics.getBudgetAmountDec() != null) {
														branchSpecifics.setBudgetAmountDec(
																IdosUtil.convertStringToDouble(decimalFormat.format(
																		branchSpecifics.getBudgetAmountDec() + IdosUtil
																				.convertStringToDouble(cellValue))));
													} else {
														branchSpecifics.setBudgetAmountDec(
																IdosUtil.convertStringToDouble(cellValue));
													}
												}
											}
											break;
										case 14:
											FormulaEvaluator formulaEval14 = wb.getCreationHelper()
													.createFormulaEvaluator();
											cellValue = formulaEval14.evaluate(myCell) != null
													? formulaEval14.evaluate(myCell).formatAsString()
													: "";
											if (branchSpecifics != null
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finStartDate)) <= 0
													&& branchSpecifics.getBudgetDate().compareTo(
															StaticController.mysqldf.parse(finEndDate)) <= 0) {
												if (!cellValue.equals("") && cellValue != null) {
													branchSpecifics
															.setBudgetTotal(IdosUtil.convertStringToDouble(cellValue));
													branchSpecifics.setBudgetDeductedTotal(null);
												} else {
													branchSpecifics.setBudgetTotal(null);
													branchSpecifics.setBudgetDeductedTotal(null);
												}
											} else {
												if (!cellValue.equals("") && cellValue != null) {
													if (branchSpecifics != null) {
														if (branchSpecifics.getBudgetTotal() != null) {
															branchSpecifics.setBudgetTotal(
																	IdosUtil.convertStringToDouble(decimalFormat
																			.format(branchSpecifics.getBudgetTotal()
																					+ IdosUtil.convertStringToDouble(
																							cellValue))));
														} else {
															branchSpecifics.setBudgetTotal(
																	IdosUtil.convertStringToDouble(cellValue));
														}
													}
												}
											}
											break;
									}
									if (branchSpecifics != null) {
										genericDAO.saveOrUpdate(branchSpecifics, user, entityManager);
									}
									cellCount++;
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
					ex.printStackTrace();
					String strBuff = getStackTraceMessage(ex);
					expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
							Thread.currentThread().getStackTrace()[1].getMethodName());
					List<String> errorList = getStackTrace(ex);
					return Results.ok(errorPage.render(ex, errorList));
				}
			}
			ObjectNode row = Json.newObject();
			row.put("success", "Uploading done");
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
		log.log(Level.FINE, ">>>> End " + results);
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getBudgetDetails(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		// EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode result = null;
		Users user = null;
		try {
			// entityTransaction.begin();
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			// String email = json.findValue("email").asText();
			if (user != null) {
				result = BUDGET_SERVICE.getBudgetDetails(user);
			} else {
				result = Json.newObject();
				result.put("result", false);
				result.put("message", "Cannot find the user details.");
			}
			// entityTransaction.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
			result = Json.newObject();
			result.put("result", false);
			result.put("message", "Something went wrong. Please try again later.");
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

}
