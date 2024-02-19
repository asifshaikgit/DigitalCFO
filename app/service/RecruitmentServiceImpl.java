package service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.IdosConstants;
import model.Branch;
import model.ProjectLabourPosition;
import model.ProjectLabourPositionLanguageProficiency;
import model.ProjectLabourPositionQualification;
import model.Users;

import java.util.logging.Logger;
import java.util.logging.Level;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.typesafe.config.Config;
import play.libs.Json;
import play.Application;
import javax.inject.Inject;

import com.idos.dao.CashNBankDAOImpl;
import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;

public class RecruitmentServiceImpl implements RecruitmentService {
	private static Application application;

	@Inject
	public RecruitmentServiceImpl(Application application) {
		this.application = application;
	}

	private DynamicReportService dynReportService = new DynamicReportServiceImpl(application);
	private ExceptionService expService = new ExceptionServiceImpl();

	@Override
	public ObjectNode listOpenPositionExcel(ObjectNode result, JsonNode json,
			Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) {
		log.log(Level.FINE, "============ Start");
		try {
			ArrayNode an = result.putArray("openProjectPositionsXLSX");
			Date currentDate = Calendar.getInstance().getTime();
			String curDt = IdosConstants.mysqldf.format(currentDate);
			String fileName = "ProjectPositionList.xlsx";
			String path = application.path().toString() + "/public/ProjectPosition/" + fileName;
			String path1 = application.path().toString() + "/target/scala-2.10/classes/public/ProjectPosition/"
					+ fileName;
			StringBuilder sbr = new StringBuilder("");
			sbr.append("select obj from ProjectLabourPosition obj where obj.positionValidity>='" + curDt
					+ "' and obj.positionValidityTo<='" + curDt
					+ "' and obj.project.allowAccessToRecruitmentServices=1 and obj.presentStatus=1");
			List<ProjectLabourPosition> allOpenProjectPositions = genericDAO.executeSimpleQuery(sbr.toString(),
					entityManager);
			Workbook wb = new XSSFWorkbook();
			CreationHelper createHelper = wb.getCreationHelper();
			CellStyle unlockedCellStyle = wb.createCellStyle();
			unlockedCellStyle.setLocked(false);
			Sheet sheets = wb.createSheet("ProjectPositionList");
			Row row = sheets.createRow((short) 0);
			Cell datacells00 = row.createCell(0);
			datacells00.setCellValue(createHelper.createRichTextString("Organization Name"));
			Cell datacells01 = row.createCell(1);
			datacells01.setCellValue(createHelper.createRichTextString("Project Name"));
			Cell datacells02 = row.createCell(2);
			datacells02.setCellValue(createHelper.createRichTextString("Designation"));
			Cell datacells03 = row.createCell(3);
			datacells03.setCellValue(createHelper.createRichTextString("Validity From"));
			Cell datacells04 = row.createCell(4);
			datacells04.setCellValue(createHelper.createRichTextString("Validity To"));
			Cell datacells05 = row.createCell(5);
			datacells05.setCellValue(createHelper.createRichTextString("Location"));
			Cell datacells06 = row.createCell(6);
			datacells06.setCellValue(createHelper.createRichTextString("Qualifications"));
			Cell datacells07 = row.createCell(7);
			datacells07.setCellValue(createHelper.createRichTextString("Experience Required(In Years)"));
			Cell datacells08 = row.createCell(8);
			datacells08.setCellValue(createHelper.createRichTextString("Language & Proficiency"));
			Cell datacells09 = row.createCell(9);
			datacells09.setCellValue(createHelper.createRichTextString("Job Description"));
			Cell datacells10 = row.createCell(10);
			datacells10.setCellValue(createHelper.createRichTextString("Compensation"));
			int incomeRowCountInt = 1;
			Map<String, Object> criterias = new HashMap<String, Object>();
			for (ProjectLabourPosition pjctLabPos : allOpenProjectPositions) {
				Row datarows = sheets.createRow((short) ++incomeRowCountInt);
				Cell datacells1 = datarows.createCell(0);
				datacells1.setCellValue(createHelper.createRichTextString(pjctLabPos.getOrganization().getName()));
				String projectName = "";
				if (pjctLabPos.getProject() != null) {
					projectName = pjctLabPos.getProject().getName();
				}
				Cell datacells2 = datarows.createCell(1);
				datacells2.setCellValue(createHelper.createRichTextString(projectName));
				String designation = "";
				if (pjctLabPos.getPositionName() != null) {
					designation = pjctLabPos.getPositionName();
				}
				Cell datacells3 = datarows.createCell(2);
				datacells3.setCellValue(createHelper.createRichTextString(designation));
				String validFrom = "";
				String validTo = "";
				if (pjctLabPos.getPositionValidity() != null && !pjctLabPos.getPositionValidity().equals("")) {
					validFrom = IdosConstants.idosdf.format(pjctLabPos.getPositionValidity());
				}
				Cell datacells4 = datarows.createCell(3);
				datacells4.setCellValue(createHelper.createRichTextString(validFrom));
				if (pjctLabPos.getPositionValidityTo() != null && !pjctLabPos.getPositionValidityTo().equals("")) {
					validTo = IdosConstants.idosdf.format(pjctLabPos.getPositionValidityTo());
				}
				Cell datacells5 = datarows.createCell(4);
				datacells5.setCellValue(createHelper.createRichTextString(validTo));
				String location = "";
				if (pjctLabPos.getLocation() != null) {
					criterias.clear();
					criterias.put("name", pjctLabPos.getLocation());
					criterias.put("organization.id", pjctLabPos.getOrganization().getId());
					criterias.put("presentStatus", 1);
					Branch bnch = genericDAO.getByCriteria(Branch.class, criterias, entityManager);
					if (bnch != null) {
						location = pjctLabPos.getLocation() + "(" + bnch.getLocation() + ")";
					}
				}
				Cell datacells6 = datarows.createCell(5);
				datacells6.setCellValue(createHelper.createRichTextString(location));
				List<ProjectLabourPositionQualification> pjctLabQualificationsList = pjctLabPos
						.getPjctLabourpositionQualification();
				StringBuilder qual = new StringBuilder("");
				String qualification = "";
				String expRequired = "";
				String langProficiency = "";
				for (ProjectLabourPositionQualification pjctLabPosQual : pjctLabQualificationsList) {
					switch (Integer.parseInt(pjctLabPosQual.getQualificationName())) {
						case 1:
							qual.append("High School,");
							break;
						case 2:
							qual.append("Under Graduate,");
							break;
						case 3:
							qual.append("Graduate,");
							break;
						case 4:
							qual.append("Post Graduate,");
							break;
						case 5:
							qual.append("Professional Qualification,");
							break;
						case 6:
							qual.append("Doctoral Qualification,");
							break;
						default:
							qual.append("");
					}
					if (pjctLabPosQual.getQualificationDegree() != null) {
						qual.append(pjctLabPosQual.getQualificationDegree() + ",");
					}
					qualification = qual.toString().substring(0, qual.toString().length() - 1);
				}
				Cell datacells7 = datarows.createCell(6);
				datacells7.setCellValue(createHelper.createRichTextString(qualification));
				if (pjctLabPos.getExpRequired() != null) {
					switch (Integer.parseInt(pjctLabPos.getExpRequired())) {
						case 1:
							expRequired = "0 Years";
							break;
						case 2:
							expRequired = "1 Years";
							break;
						case 3:
							expRequired = "2 Years";
							break;
						case 4:
							expRequired = "3-4 Years";
							break;
						case 5:
							expRequired = "5-6 Years";
							break;
						case 6:
							expRequired = "6-7 Years";
							break;
						case 7:
							expRequired = "8-10 Years";
							break;
						case 8:
							expRequired = "11-12 Years";
							break;
						case 9:
							expRequired = "12-15 Years";
							break;
						case 10:
							expRequired = "15-20 Years";
							break;
						case 11:
							expRequired = "21-25 Years";
							break;
						case 12:
							expRequired = "25+ Years";
							break;
					}
				}
				Cell datacells8 = datarows.createCell(7);
				datacells8.setCellValue(createHelper.createRichTextString(expRequired));
				List<ProjectLabourPositionLanguageProficiency> listProjLabPosLanguage = pjctLabPos
						.getPjctLabourpositionLangProf();
				qual.delete(0, qual.length());
				for (ProjectLabourPositionLanguageProficiency projLangProf : listProjLabPosLanguage) {
					qual.append(projLangProf.getLanguaugeProficiency() + ",");
				}
				langProficiency = qual.toString().substring(0, qual.toString().length() - 1);
				Cell datacells9 = datarows.createCell(8);
				datacells9.setCellValue(createHelper.createRichTextString(langProficiency));
				String jobDescription = "";
				String compensation = "";
				if (pjctLabPos.getJobDescription() != null) {
					jobDescription = pjctLabPos.getJobDescription();
				}
				Cell datacells010 = datarows.createCell(9);
				datacells010.setCellValue(createHelper.createRichTextString(jobDescription));
				if (pjctLabPos.getBudget() != null) {
					compensation = IdosConstants.decimalFormat.format(pjctLabPos.getBudget());
				}
				Cell datacells011 = datarows.createCell(10);
				datacells011.setCellValue(createHelper.createRichTextString(compensation));
			}
			sheets.autoSizeColumn(0);
			sheets.autoSizeColumn(1);
			sheets.autoSizeColumn(2);
			sheets.autoSizeColumn(3);
			sheets.autoSizeColumn(4);
			sheets.autoSizeColumn(5);
			sheets.autoSizeColumn(6);
			sheets.autoSizeColumn(7);
			sheets.autoSizeColumn(8);
			sheets.autoSizeColumn(9);
			sheets.autoSizeColumn(10);
			FileOutputStream fileOut = new FileOutputStream(path);
			FileOutputStream fileOut1 = new FileOutputStream(path1);
			wb.write(fileOut);
			wb = new XSSFWorkbook(new FileInputStream(path));
			wb.write(fileOut1);
			fileOut.close();
			fileOut1.close();
			ObjectNode datarow = Json.newObject();
			datarow.put("fileName", fileName);
			an.add(datarow);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
		}
		return result;
	}

	@Override
	public ObjectNode listOpenPositionJSON(ObjectNode result, JsonNode json,
			Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) {
		log.log(Level.FINE, "============ Start");
		try {
			result.put("result", false);
			ArrayNode an = result.putArray("openProjectPositionsJSON");
			Date currentDate = Calendar.getInstance().getTime();
			String curDt = IdosConstants.mysqldf.format(currentDate);
			StringBuilder sbr = new StringBuilder("");
			sbr.append("select obj from ProjectLabourPosition obj where obj.positionValidity>='" + curDt
					+ "' and obj.positionValidityTo<='" + curDt
					+ "' and obj.project.allowAccessToRecruitmentServices=1 and obj.presentStatus=1");
			List<ProjectLabourPosition> allOpenProjectPositions = genericDAO.executeSimpleQuery(sbr.toString(),
					entityManager);
			for (ProjectLabourPosition pjctLabPos : allOpenProjectPositions) {
				result.put("result", true);
				ObjectNode row = Json.newObject();
				row.put("Organization Name", pjctLabPos.getOrganization().getName());
				String projectName = "";
				if (pjctLabPos.getProject() != null) {
					projectName = pjctLabPos.getProject().getName();
				}
				row.put("Project Name", projectName);
				String designation = "";
				if (pjctLabPos.getPositionName() != null) {
					designation = pjctLabPos.getPositionName();
				}
				row.put("Designation", designation);
				String validFrom = "";
				String validTo = "";
				if (pjctLabPos.getPositionValidity() != null && !pjctLabPos.getPositionValidity().equals("")) {
					validFrom = IdosConstants.idosdf.format(pjctLabPos.getPositionValidity());
				}
				row.put("Validity From", validFrom);
				if (pjctLabPos.getPositionValidityTo() != null && !pjctLabPos.getPositionValidityTo().equals("")) {
					validTo = IdosConstants.idosdf.format(pjctLabPos.getPositionValidityTo());
				}
				row.put("Validity To", validTo);
				String location = "";
				if (pjctLabPos.getLocation() != null) {
					location = pjctLabPos.getLocation();
				}
				row.put("Location", location);
				List<ProjectLabourPositionQualification> pjctLabQualificationsList = pjctLabPos
						.getPjctLabourpositionQualification();
				StringBuilder qual = new StringBuilder("");
				String qualification = "";
				String expRequired = "";
				String langProficiency = "";
				for (ProjectLabourPositionQualification pjctLabPosQual : pjctLabQualificationsList) {
					switch (Integer.parseInt(pjctLabPosQual.getQualificationName())) {
						case 1:
							qual.append("High School,");
							break;
						case 2:
							qual.append("Under Graduate,");
							break;
						case 3:
							qual.append("Graduate,");
							break;
						case 4:
							qual.append("Post Graduate,");
							break;
						case 5:
							qual.append("Professional Qualification,");
							break;
						case 6:
							qual.append("Doctoral Qualification,");
							break;
						default:
							qual.append("");
					}
					if (pjctLabPosQual.getQualificationDegree() != null) {
						qual.append(pjctLabPosQual.getQualificationDegree() + ",");
					}
					qualification = qual.toString().substring(0, qual.toString().length() - 1);
				}
				row.put("Qualifications", qualification);
				if (pjctLabPos.getExpRequired() != null) {
					switch (Integer.parseInt(pjctLabPos.getExpRequired())) {
						case 1:
							expRequired = "0 Years";
							break;
						case 2:
							expRequired = "1 Years";
							break;
						case 3:
							expRequired = "2 Years";
							break;
						case 4:
							expRequired = "3-4 Years";
							break;
						case 5:
							expRequired = "5-6 Years";
							break;
						case 6:
							expRequired = "6-7 Years";
							break;
						case 7:
							expRequired = "8-10 Years";
							break;
						case 8:
							expRequired = "11-12 Years";
							break;
						case 9:
							expRequired = "12-15 Years";
							break;
						case 10:
							expRequired = "15-20 Years";
							break;
						case 11:
							expRequired = "21-25 Years";
							break;
						case 12:
							expRequired = "25+ Years";
							break;
					}
				}
				row.put("Experience Required(In Years)", expRequired);
				List<ProjectLabourPositionLanguageProficiency> listProjLabPosLanguage = pjctLabPos
						.getPjctLabourpositionLangProf();
				qual.delete(0, qual.length());
				for (ProjectLabourPositionLanguageProficiency projLangProf : listProjLabPosLanguage) {
					qual.append(projLangProf.getLanguaugeProficiency() + ",");
				}
				langProficiency = qual.toString().substring(0, qual.toString().length() - 1);
				row.put("Language & Proficiency", langProficiency);
				String jobDescription = "";
				String compensation = "";
				if (pjctLabPos.getJobDescription() != null) {
					jobDescription = pjctLabPos.getJobDescription();
				}
				row.put("Job Description", jobDescription);
				if (pjctLabPos.getBudget() != null) {
					compensation = IdosConstants.decimalFormat.format(pjctLabPos.getBudget());
				}
				row.put("Compensation", compensation);
				an.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
		}
		return result;
	}

}
