package service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.logging.Level;
import model.AdvanceAdjustmentDetail;
import model.Branch;
import model.BranchBankAccounts;
import model.BranchCashCount;
import model.BranchDepositBoxKey;
import model.BranchSpecifics;
import model.BranchTaxes;
import model.ClaimItemDetails;
import model.ClaimTransaction;
import model.ClaimsSettlement;
import model.CoaTemplateAccounts;
import model.CoaValidationMapping;
import model.CustomerDetail;
import model.IDOSCountry;
import model.IdosProvisionJournalEntry;
import model.Organization;
import model.Particulars;
import model.Project;
import model.ProvisionJournalEntryDetail;
import model.Specifics;
import model.Transaction;
import model.TransactionInvoice;
import model.TransactionItems;
import model.TransactionPurpose;
import model.Users;
import model.Vendor;
import model.VendorDetail;
import model.VendorGroup;
import model.karvy.GSTFiling;

import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.JsonNode;

import com.idos.enumtype.BankAccountEnumType;
import com.idos.util.CountryCurrencyUtil;
import com.idos.util.CountryTelephoneCodeUtil;
import com.idos.util.DateUtil;
import com.idos.util.ExcelUtil;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;

import controllers.StaticController;
import play.Application;
import play.db.jpa.JPAApi;
import javax.inject.Inject;

public class CreateExcelServiceImpl implements CreateExcelService {
    private final Application application;
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    @Inject
    public CreateExcelServiceImpl(Application application) {
        this.application = application;
    }

    @SuppressWarnings("deprecation")
    @Override
    public String createbudgetexcel(Organization org, EntityManager em, String path, String sheetName)
            throws Exception {
        log.log(Level.FINE, "============ Start");
        List<Branch> branchList = org.getBranches();
        String fileName = sheetName + ".xlsx";
        // String path=application.path().toString()+"/logs/BudgetExcel/"+fileName;
        // String
        // path1=application.path().toString()+"/target/scala-2.10/classes/public/BudgetExcel/"+fileName;

        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        path = path.concat(fileName);
        Workbook wb = new XSSFWorkbook();
        CreationHelper createHelper = wb.getCreationHelper();
        CellStyle unlockedCellStyle = wb.createCellStyle();
        unlockedCellStyle.setLocked(false);
        Map<String, Object> criterias = new HashMap<String, Object>();
        for (Branch orgBranches : branchList) {
            criterias.clear();
            criterias.put("organization.id", org.getId());
            criterias.put("accountCode", 2000000000000000000L);
            criterias.put("presentStatus", 1);
            Particulars part = genericDAO.getByCriteria(Particulars.class, criterias, em);
            criterias.clear();
            criterias.put("branch.id", orgBranches.getId());
            criterias.put("organization.id", org.getId());
            criterias.put("particular.id", part.getId());
            criterias.put("presentStatus", 1);
            List<BranchSpecifics> branchSpecifics = genericDAO.findByCriteria(BranchSpecifics.class, criterias, em);
            Sheet sheets = wb.createSheet(orgBranches.getName());
            // Create a row and put some cells in it. Rows are 0 based.
            Row row = sheets.createRow((short) 0);
            sheets.protectSheet("");
            // Create a cell and put a value in it.
            row.createCell(0).setCellValue(createHelper.createRichTextString("Name"));
            row.createCell(1).setCellValue(createHelper.createRichTextString("Jan"));
            row.createCell(2).setCellValue(createHelper.createRichTextString("Feb"));
            row.createCell(3).setCellValue(createHelper.createRichTextString("Mar"));
            row.createCell(4).setCellValue(createHelper.createRichTextString("Apr"));
            row.createCell(5).setCellValue(createHelper.createRichTextString("May"));
            row.createCell(6).setCellValue(createHelper.createRichTextString("Jun"));
            row.createCell(7).setCellValue(createHelper.createRichTextString("July"));
            row.createCell(8).setCellValue(createHelper.createRichTextString("Aug"));
            row.createCell(9).setCellValue(createHelper.createRichTextString("Sep"));
            row.createCell(10).setCellValue(createHelper.createRichTextString("Oct"));
            row.createCell(11).setCellValue(createHelper.createRichTextString("Nov"));
            row.createCell(12).setCellValue(createHelper.createRichTextString("Dec"));
            row.createCell(13).setCellValue(createHelper.createRichTextString("Total"));
            Row datarow = sheets.createRow((short) 1);
            datarow.createCell(0).setCellValue(createHelper.createRichTextString(orgBranches.getName()));
            Cell datacell2 = datarow.createCell(1);
            datacell2.setCellValue(createHelper.createRichTextString(""));
            String colFormCell2 = "SUM(B3:B" + (branchSpecifics.size() + 2) + ")";
            datacell2.setCellFormula(colFormCell2);
            // datacell2.setCellType(CellType.FORMULA);
            Cell datacell3 = datarow.createCell(2);
            datacell3.setCellValue(createHelper.createRichTextString(""));
            String colFormCell3 = "SUM(C3:C" + (branchSpecifics.size() + 2) + ")";
            datacell3.setCellFormula(colFormCell3);
            // datacell3.setCellType(CellType.FORMULA);
            Cell datacell4 = datarow.createCell(3);
            datacell4.setCellValue(createHelper.createRichTextString(""));
            String colFormCell4 = "SUM(D3:D" + (branchSpecifics.size() + 2) + ")";
            datacell4.setCellFormula(colFormCell4);
            // datacell4.setCellType(CellType.FORMULA);
            Cell datacell5 = datarow.createCell(4);
            datacell5.setCellValue(createHelper.createRichTextString(""));
            String colFormCell5 = "SUM(E3:E" + (branchSpecifics.size() + 2) + ")";
            datacell5.setCellFormula(colFormCell5);
            // datacell5.setCellType(CellType.FORMULA);
            Cell datacell6 = datarow.createCell(5);
            datacell6.setCellValue(createHelper.createRichTextString(""));
            String colFormCell6 = "SUM(F3:F" + (branchSpecifics.size() + 2) + ")";
            datacell6.setCellFormula(colFormCell6);
            // datacell6.setCellType(CellType.FORMULA);
            Cell datacell7 = datarow.createCell(6);
            datacell7.setCellValue(createHelper.createRichTextString(""));
            String colFormCell7 = "SUM(G3:G" + (branchSpecifics.size() + 2) + ")";
            datacell7.setCellFormula(colFormCell7);
            // datacell7.setCellType(CellType.FORMULA);
            Cell datacell8 = datarow.createCell(7);
            datacell8.setCellValue(createHelper.createRichTextString(""));
            String colFormCell8 = "SUM(H3:H" + (branchSpecifics.size() + 2) + ")";
            datacell8.setCellFormula(colFormCell8);
            // datacell8.setCellType(CellType.FORMULA);
            Cell datacell9 = datarow.createCell(8);
            datacell9.setCellValue(createHelper.createRichTextString(""));
            String colFormCell9 = "SUM(I3:I" + (branchSpecifics.size() + 2) + ")";
            datacell9.setCellFormula(colFormCell9);
            // datacell9.setCellType(CellType.FORMULA);
            Cell datacell10 = datarow.createCell(9);
            datacell10.setCellValue(createHelper.createRichTextString(""));
            String colFormCell10 = "SUM(J3:J" + (branchSpecifics.size() + 2) + ")";
            datacell10.setCellFormula(colFormCell10);
            // datacell10.setCellType(CellType.FORMULA);
            Cell datacell11 = datarow.createCell(10);
            datacell11.setCellValue(createHelper.createRichTextString(""));
            String colFormCell11 = "SUM(K3:K" + (branchSpecifics.size() + 2) + ")";
            datacell11.setCellFormula(colFormCell11);
            // datacell11.setCellType(CellType.FORMULA);
            Cell datacell12 = datarow.createCell(11);
            datacell12.setCellValue(createHelper.createRichTextString(""));
            String colFormCell12 = "SUM(L3:L" + (branchSpecifics.size() + 2) + ")";
            datacell12.setCellFormula(colFormCell12);
            // datacell12.setCellType(CellType.FORMULA);
            Cell datacell13 = datarow.createCell(12);
            datacell13.setCellValue(createHelper.createRichTextString(""));
            String colFormCell13 = "SUM(M3:M" + (branchSpecifics.size() + 2) + ")";
            datacell13.setCellFormula(colFormCell13);
            // datacell13.setCellType(CellType.FORMULA);
            Cell datacell14 = datarow.createCell(13);
            datacell14.setCellValue(createHelper.createRichTextString(""));
            String StrFormula = "SUM(B2:M2)";
            datacell14.setCellFormula(StrFormula);
            // datacell14.setCellType(CellType.FORMULA);
            for (int i = 0; i < branchSpecifics.size(); i++) {
                String hasChildorNoQuery = "select obj from Specifics obj where obj.organization.id= ?1 and obj.parentSpecifics.id= ?2 and obj.presentStatus=1";
                ArrayList<Object> inparam1 = new ArrayList<Object>(3);
                inparam1.add(branchSpecifics.get(i).getOrganization().getId());
                inparam1.add(branchSpecifics.get(i).getSpecifics().getId());
                List<Specifics> hasChildorNo = genericDAO.queryWithParamsName(hasChildorNoQuery, em, inparam1);
                if (hasChildorNo.size() == 0) {
                    int val = i + 3;
                    Row datarows = sheets.createRow((short) i + 2);
                    String specificsName = branchSpecifics.get(i).getSpecifics().getName().replaceAll("&amp;", "&");
                    datarows.createCell(0).setCellValue(createHelper.createRichTextString(specificsName));
                    Cell datacells2 = datarows.createCell(1);
                    datacells2.setCellValue(createHelper.createRichTextString(""));
                    datacells2.setCellStyle(unlockedCellStyle);
                    Cell datacells3 = datarows.createCell(2);
                    datacells3.setCellValue(createHelper.createRichTextString(""));
                    datacells3.setCellStyle(unlockedCellStyle);
                    Cell datacells4 = datarows.createCell(3);
                    datacells4.setCellValue(createHelper.createRichTextString(""));
                    datacells4.setCellStyle(unlockedCellStyle);
                    Cell datacells5 = datarows.createCell(4);
                    datacells5.setCellValue(createHelper.createRichTextString(""));
                    datacells5.setCellStyle(unlockedCellStyle);
                    Cell datacells6 = datarows.createCell(5);
                    datacells6.setCellValue(createHelper.createRichTextString(""));
                    datacells6.setCellStyle(unlockedCellStyle);
                    Cell datacells7 = datarows.createCell(6);
                    datacells7.setCellValue(createHelper.createRichTextString(""));
                    datacells7.setCellStyle(unlockedCellStyle);
                    Cell datacells8 = datarows.createCell(7);
                    datacells8.setCellValue(createHelper.createRichTextString(""));
                    datacells8.setCellStyle(unlockedCellStyle);
                    Cell datacells9 = datarows.createCell(8);
                    datacells9.setCellValue(createHelper.createRichTextString(""));
                    datacells9.setCellStyle(unlockedCellStyle);
                    Cell datacells10 = datarows.createCell(9);
                    datacells10.setCellValue(createHelper.createRichTextString(""));
                    datacells10.setCellStyle(unlockedCellStyle);
                    Cell datacells11 = datarows.createCell(10);
                    datacells11.setCellValue(createHelper.createRichTextString(""));
                    datacells11.setCellStyle(unlockedCellStyle);
                    Cell datacells12 = datarows.createCell(11);
                    datacells12.setCellValue(createHelper.createRichTextString(""));
                    datacells12.setCellStyle(unlockedCellStyle);
                    Cell datacells13 = datarows.createCell(12);
                    datacells13.setCellValue(createHelper.createRichTextString(""));
                    datacells13.setCellStyle(unlockedCellStyle);
                    Cell datacells14 = datarows.createCell(13);
                    datacells14.setCellValue(createHelper.createRichTextString(""));
                    String StrCellFormula = "SUM(B" + val + ":M" + val + ")";
                    // datacells14.setCellType(CellType.FORMULA);
                    datacells14.setCellFormula(StrCellFormula);

                    sheets.autoSizeColumn(0);
                }
            }
        }
        FileOutputStream fileOut = new FileOutputStream(path);
        // FileOutputStream fileOut1 = new FileOutputStream(path1);
        wb.write(fileOut);
        // wb = new XSSFWorkbook(new FileInputStream(path));
        // wb.write(fileOut1);
        fileOut.close();
        // fileOut1.close();
        return fileName;
    }

    private List<IdosProvisionJournalEntry> getJournalEntriesToDownload(Organization org, Date fromDate, Date toDate,
            String searchCategory, String searchTransactionRefNumber, String searchTxnPurpose, String searchUserType,
            String searchItems, String searchTxnStatus, String searchTxnBranch, String searchTxnProjects,
            String searchVendors, String searchCustomers, String searchTxnWithWithoutDoc, String searchTxnPyMode,
            String searchTxnWithWithoutRemarks, String searchTxnException, String searchAmountRanseLimitFrom,
            String searchAmountRanseLimitTo, EntityManager em) throws Exception {
        // ArrayList inparamList = new ArrayList();
        StringBuilder pjeJPQL = new StringBuilder(
                "select obj from IdosProvisionJournalEntry obj where obj.provisionMadeForOrganization.id='"
                        + org.getId() + "' and obj.presentStatus=1");
        // inparamList.add(org.getId());
        if (fromDate != null && toDate == null) {
            // pjeJPQL.append(" and obj.transactionDate >=?");
            // inparamList.add(IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(fromDate)));
            pjeJPQL.append(" and obj.transactionDate >='" + IdosConstants.MYSQLDF.format(fromDate) + "'");
        }
        if (fromDate == null && toDate != null) {
            // pjeJPQL.append(" and obj.transactionDate <=?");
            // inparamList.add(IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(toDate)));
            pjeJPQL.append(" and obj.transactionDate <='" + IdosConstants.MYSQLDF.format(toDate) + "'");
        }
        if (fromDate != null && toDate != null) {
            pjeJPQL.append(" and obj.transactionDate between '" + IdosConstants.MYSQLDF.format(fromDate) + "' and '"
                    + IdosConstants.MYSQLDF.format(toDate) + "'");
        }
        if (searchTxnPurpose != null && !searchTxnPurpose.equals("")) {
            pjeJPQL.append(" and obj.transactionPurpose.id IN (" + searchTxnPurpose + ")");
        }

        if (searchTxnStatus != null && !searchTxnStatus.equals("") && !searchTxnStatus.equals("''")) {
            pjeJPQL.append(" and obj.transactionStatus IN(" + searchTxnStatus + ")");
        }
        if (searchTransactionRefNumber != null && !searchTransactionRefNumber.equals("")) {
            pjeJPQL.append(" and obj.transactionRefNumber='" + searchTransactionRefNumber + "'");
        }
        if (searchTxnBranch != null && !searchTxnBranch.equals("")) {
            pjeJPQL.append(" and ( obj.debitBranch.id IN (" + searchTxnBranch + ") or obj.creditBranch.id IN ("
                    + searchTxnBranch + "))");
        }
        if (searchAmountRanseLimitFrom != null && searchAmountRanseLimitTo != null) {
            pjeJPQL.append(" and (obj.totalDebitAmount>='" + searchAmountRanseLimitFrom
                    + "' and obj.totalDebitAmount <='" + searchAmountRanseLimitTo + "') and (obj.totalCreditAmount>= '"
                    + searchAmountRanseLimitFrom + "' and obj.totalCreditAmount <='" + searchAmountRanseLimitTo + "')");
        } else if (searchAmountRanseLimitFrom != null && searchAmountRanseLimitTo == null) {
            pjeJPQL.append(" and (obj.totalDebitAmount>='" + searchAmountRanseLimitFrom
                    + "' or obj.totalCreditAmount>='" + searchAmountRanseLimitFrom + "')");
        } else if (searchAmountRanseLimitFrom == null && searchAmountRanseLimitTo != null) {
            pjeJPQL.append(" and (obj.totalDebitAmount>='" + searchAmountRanseLimitTo + "' or obj.totalCreditAmount>='"
                    + searchAmountRanseLimitTo + "')");
        }

        if (searchTxnWithWithoutDoc != null && !searchTxnWithWithoutDoc.equals("")) {
            if (searchTxnWithWithoutDoc.equals("1")) {
                pjeJPQL.append(" and obj.supportingDocs!=null");
            }
            if (searchTxnWithWithoutDoc.equals("0")) {
                pjeJPQL.append(" and obj.supportingDocs==null");
            }
        }

        if (searchTxnWithWithoutRemarks != null && !searchTxnWithWithoutRemarks.equals("")) {
            if (searchTxnWithWithoutRemarks.equals("1")) {
                pjeJPQL.append(" and obj.remarks!=null");
            }
            if (searchTxnWithWithoutRemarks.equals("0")) {
                pjeJPQL.append(" and obj.remarks!=null");
            }
        }
        pjeJPQL.append(" order by obj.transactionDate");
        // List<IdosProvisionJournalEntry> pjeList =
        // genericDAO.queryWithParams(pjeJPQL.toString(), em, inparamList);
        List<IdosProvisionJournalEntry> pjeList = genericDAO.executeSimpleQuery(pjeJPQL.toString(), em);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "JPQL: " + pjeJPQL);
            log.log(Level.FINE, "Total fetch: " + pjeList.size());
        }
        return pjeList;
    }

    // Not in use
    private List<Transaction> getTxnToDownload(Organization org, Date fromDate, Date toDate, String searchCategory,
            String searchTransactionRefNumber, String searchTxnPurpose, String searchUserType, String searchItems,
            String searchTxnStatus, String searchTxnBranch, String searchTxnProjects, String searchVendors,
            String searchCustomers, String searchTxnWithWithoutDoc, String searchTxnPyMode,
            String searchTxnWithWithoutRemarks, String searchTxnException, String searchAmountRanseLimitFrom,
            String searchAmountRanseLimitTo, EntityManager em) throws Exception {
        // ArrayList inparamList = new ArrayList();
        StringBuilder sb = new StringBuilder(
                "select obj from Transaction obj where obj.transactionBranchOrganization.id='" + org.getId()
                        + "' and obj.presentStatus=1");
        // inparamList.add(org.getId());
        if (fromDate != null && toDate == null) {
            // sb.append(" and obj.transactionDate=?");
            // inparamList.add(IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(fromDate)));
            sb.append(" and obj.transactionDate='" + IdosConstants.MYSQLDF.format(fromDate) + "'");
        }
        if (fromDate == null && toDate != null) {
            // sb.append(" and obj.transactionDate=?");
            // inparamList.add(IdosConstants.MYSQLDF.parse(IdosConstants.MYSQLDF.format(toDate)));
            sb.append(" and obj.transactionDate='" + IdosConstants.MYSQLDF.format(toDate) + "'");
        }
        if (fromDate != null && toDate != null) {
            sb.append(" and obj.transactionDate between'" + IdosConstants.MYSQLDF.format(fromDate) + "' and'"
                    + IdosConstants.MYSQLDF.format(toDate) + "'");
        }
        if (searchTxnPurpose != null && !searchTxnPurpose.equals("")) {
            sb.append(" and obj.transactionPurpose.id IN (" + searchTxnPurpose + ")");
        }

        if (searchCategory != null && !searchCategory.equals("")) {
            sb.append(" and obj.transactionParticulars.id='" + searchCategory + "'");
        }
        if (searchItems != null && !searchItems.equals("")) {
            sb.append(" and obj.transactionSpecifics.id IN(" + searchItems + ")");
        }
        if (searchTxnStatus != null && !searchTxnStatus.equals("") && !searchTxnStatus.equals("''")) {
            sb.append(" and obj.transactionStatus IN(" + searchTxnStatus + ")");
        }
        if (searchTransactionRefNumber != null && !searchTransactionRefNumber.equals("")) {
            sb.append(" and obj.transactionRefNumber='" + searchTransactionRefNumber + "'");
        }
        if (searchTxnBranch != null && !searchTxnBranch.equals("")) {
            sb.append(" and obj.transactionBranch.id IN(" + searchTxnBranch + ")");
        }
        if (searchTxnProjects != null && !searchTxnProjects.equals("")) {
            sb.append(" and obj.transactionProject.id IN" + searchTxnProjects + ")");
        }
        if (searchAmountRanseLimitFrom != null && searchAmountRanseLimitTo == null) {
            sb.append(" and obj.netAmount>='" + Double.parseDouble(searchAmountRanseLimitFrom) + "'");

        }
        if (searchAmountRanseLimitFrom == null && searchAmountRanseLimitTo != null) {
            sb.append(" and obj.netAmount<='" + Double.parseDouble(searchAmountRanseLimitTo) + "'");

        }
        if (searchAmountRanseLimitFrom != null && searchAmountRanseLimitTo != null) {
            sb.append(" and obj.netAmount>='" + Double.parseDouble(searchAmountRanseLimitFrom)
                    + "' and obj.netAmount<='" + Double.parseDouble(searchAmountRanseLimitTo) + "'");
        }
        if (searchTxnWithWithoutDoc != null && !searchTxnWithWithoutDoc.equals("")) {
            if (searchTxnWithWithoutDoc.equals("1")) {
                sb.append(" and obj.supportingDocs!=null");
            }
            if (searchTxnWithWithoutDoc.equals("0")) {
                sb.append(" and obj.supportingDocs==null");
            }
        }
        if (searchTxnPyMode != null && !searchTxnPyMode.equals("")) {
            sb.append(" and obj.receiptDetailsType IN(" + searchTxnPyMode + ")");
        }
        if (searchTxnWithWithoutRemarks != null && !searchTxnWithWithoutRemarks.equals("")) {
            if (searchTxnWithWithoutRemarks.equals("1")) {
                sb.append(" and obj.remarks!=null");
            }
            if (searchTxnWithWithoutRemarks.equals("0")) {
                sb.append(" and obj.remarks!=null");
            }
        }
        if (searchTxnException != null && !searchTxnException.equals("")) {
            if (searchTxnException.equals("1")) {
                sb.append(" and obj.transactionExceedingBudget=1 and obj.klFollowStatus IN(null,1)");
            }
            if (searchTxnException.equals("2")) {
                sb.append(" and obj.klFollowStatus=0 and obj.transactionExceedingBudget IN(null,0)");
            }
            if (searchTxnException.equals("3")) {
                sb.append(" and obj.transactionExceedingBudget=1 and obj.klFollowStatus=0");
            }
            if (searchTxnException.equals("1,2")) {
                sb.append(
                        " and ((obj.transactionExceedingBudget=1 and (obj.klFollowStatus is null or obj.klFollowStatus!=0)) or (obj.klFollowStatus=0 and obj.transactionExceedingBudget is null or obj.transactionExceedingBudget!=1))");
            }
            if (searchTxnException.equals("1,3")) {
                sb.append(
                        " and ((obj.transactionExceedingBudget=1 and (obj.klFollowStatus is null or obj.klFollowStatus!=0)) or (obj.transactionExceedingBudget=1 and obj.klFollowStatus=0))");
            }
            if (searchTxnException.equals("2,3")) {
                sb.append(
                        " and ((obj.klFollowStatus=0 and obj.transactionExceedingBudget is null or obj.transactionExceedingBudget!=1) or (obj.transactionExceedingBudget=1 and obj.klFollowStatus=0))");
            }
            if (searchTxnException.equals("1,2,3")) {
                sb.append(" and (obj.transactionExceedingBudget in(null,1) or obj.klFollowStatus=0)");
            }
        }
        if ((searchVendors != null && !searchVendors.equals(""))
                && (searchCustomers != null && !searchCustomers.equals(""))) {
            sb.append("and (obj.transactionVendorCustomer.id IN(" + searchVendors
                    + ") OR obj.transactionVendorCustomer.id IN(" + searchCustomers + "))");
        } else if (searchVendors != null && !searchVendors.equals("")) {
            sb.append("and obj.transactionVendorCustomer.id IN(" + searchVendors + ")");
        } else if (searchCustomers != null && !searchCustomers.equals("")) {
            sb.append("and obj.transactionVendorCustomer.id IN(" + searchCustomers + ")");
        }
        sb.append(" order by obj.transactionDate");
        List<Transaction> transactionList = genericDAO.executeSimpleQuery(sb.toString(), em);
        // List<Transaction> transactionList = genericDAO.queryWithParams(sb.toString(),
        // em, inparamList);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "JPQL: " + sb);
            log.log(Level.FINE, "Total fetch: " + transactionList.size());
        }
        return transactionList;
    }

    @Override
    public String createtransactionexcel(Users user, JsonNode json, EntityManager em, String path, String sheetName,
            List<Transaction> sellTxnList, List<Transaction> buyTxnList, List<Transaction> otherTxnList,
            List<IdosProvisionJournalEntry> pjeTxnList) throws Exception {
        String searchItems = json.findValue("searchItems") != null ? json.findValue("searchItems").asText() : null;
        String searchVendors = json.findValue("searchVendors") != null ? json.findValue("searchVendors").asText()
                : null;
        String searchCustomers = json.findValue("searchCustomers") != null ? json.findValue("searchCustomers").asText()
                : null;

        String fileName = sheetName + ".xlsx";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        path = path.concat(fileName);

        Workbook wb = new XSSFWorkbook();
        CreationHelper createHelper = wb.getCreationHelper();
        CellStyle unlockedCellStyle = wb.createCellStyle();
        final Integer MAX_COLUMN = 70;
        Cell cell[] = new Cell[MAX_COLUMN];
        String[] header = null;
        Row row = null;
        int noOfRows = 1;
        List<Transaction> transactionList = null;
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "sellTxnList: " + sellTxnList.size());
        if (sellTxnList.size() > 0) {
            String saleTxnSheetName = IdosUtil.getOrganizationName4Report(user) + "_Sale";
            Sheet saleTxnSheet = wb.createSheet(saleTxnSheetName);
            row = saleTxnSheet.createRow((short) 0);
            createHelper = wb.getCreationHelper();
            unlockedCellStyle = wb.createCellStyle();
            unlockedCellStyle.setLocked(false);
            // Set Header for Sell
            header = new String[MAX_COLUMN];
            header[0] = "Transaction Reference Number";
            header[1] = "Date";
            header[2] = "Creator";
            header[3] = "Approver";
            header[4] = "Transaction Purpose";
            header[5] = "Branch";
            header[6] = "Project";
            header[7] = "Customer";
            header[8] = "GSTIN";
            header[9] = "Place of supply";
            header[10] = "Doc. S No.";
            header[11] = "Type of Supply";
            header[12] = "PO Reference No.";
            header[13] = "Line Item";
            header[14] = "Goods/Services";
            header[15] = "Item";
            header[16] = "HSN/SAC";
            header[17] = "Type of Goods/Services";
            header[18] = "Units Of Measure";
            header[19] = "No. of Units";
            header[20] = "Price per unit";
            header[21] = "Discount Rate";
            header[22] = "Discount Amount";
            header[23] = "Gross Amount";
            header[24] = "Withholding Tax";
            header[25] = "SGST Rate";
            header[26] = "SGST Amount";
            header[27] = "CGST Rate";
            header[28] = "CGST Amount";
            header[29] = "IGST Rate";
            header[30] = "IGST Amount";
            header[31] = "CESS Rate";
            header[32] = "CESS Amount";
            header[33] = "Invoice Value";
            header[34] = "Advance Adjustment";
            header[35] = "SGST On Advance Adjusted";
            header[36] = "CGST On Advance Adjusted";
            header[37] = "IGST On Advance Adjusted";
            header[38] = "CESS On Advance Adjusted";
            header[39] = "Net Amount";
            header[40] = "Mode of Receipt";
            header[41] = "Bank Name";
            header[42] = "Instrument No.";
            header[43] = "Instrument Date";
            header[44] = "Pay Mode Description";
            header[45] = "Date & Time of Supply";
            header[46] = "Transport Mode";
            header[47] = "Vehicle Details";
            header[48] = "Date of Application for Removal of Goods";
            header[49] = "Number of Application for Removal of Goods";
            header[50] = "GSTIN of e-Commerce Operator";
            header[51] = "Destination Country";
            header[52] = "Destination Currency";
            header[53] = "Conversion Rate";
            header[54] = "Port Code";
            header[55] = "Invoice Date";
            header[56] = "Original Document No.";
            header[57] = "Original Document Date";
            header[58] = "Remarks1";
            header[59] = "Remarks2";
            header[60] = "Remarks3";
            header[61] = "Status";
            header[62] = "";
            header[63] = "";
            header[64] = "";
            header[65] = "";
            header[66] = "";
            header[67] = "";
            header[68] = "";
            header[69] = "";
            //
            // Cell Creation and Formating
            for (int i = 0; i < MAX_COLUMN; i++) {
                cell[i] = row.createCell(i);
                cell[i].setCellValue(createHelper.createRichTextString(header[i]));
                cell[i].setCellStyle(unlockedCellStyle);
            }

            noOfRows = 1;
            for (Transaction txn : sellTxnList) {
                List<TransactionItems> listTransactionItems = TRANSACTION_ITEM_DAO.findByTransaction(em,
                        txn.getTransactionBranchOrganization().getId(), txn.getId());
                if (listTransactionItems != null && listTransactionItems.size() > 0) {
                    noOfRows = writeMultiItemsTrans(listTransactionItems, txn, createHelper, saleTxnSheet, noOfRows, 2,
                            null, em, 2);
                } else {
                    noOfRows = writeTransaction(listTransactionItems, txn, createHelper, saleTxnSheet, noOfRows, 2,
                            null, em, 2);
                }
            }
            for (int i = 0; i < MAX_COLUMN; i++) {
                saleTxnSheet.autoSizeColumn(i);
            }
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "buyTxnList: " + buyTxnList.size());
        if (buyTxnList.size() > 0) { // buy txns
            String sheetBuyTxnName = IdosUtil.getOrganizationName4Report(user) + "_Buy";
            Sheet sheetBuyTxn = wb.createSheet(sheetBuyTxnName);
            row = sheetBuyTxn.createRow((short) 0);
            createHelper = wb.getCreationHelper();
            unlockedCellStyle = wb.createCellStyle();
            unlockedCellStyle.setLocked(false);
            header = new String[MAX_COLUMN];
            // Set Header for Buy
            header[0] = "Transaction Reference Number";
            header[1] = "Date";
            header[2] = "Creator";
            header[3] = "Approver";
            header[4] = "Transaction Purpose";
            header[5] = "Branch";
            header[6] = "Project";
            header[7] = "Vendor";
            header[8] = "GSTIN";
            header[9] = "Place of supply";
            header[10] = "Type of supply";
            header[11] = "Purchase Order";
            header[12] = "Line Item";
            header[13] = "Goods/Services";
            header[14] = "Item";
            header[15] = "HSN/SAC";
            header[16] = "Units Of Measure";
            header[17] = "Reverse Charge Item";
            header[18] = "No. of Units";
            header[19] = "Price per unit";
            header[20] = "Gross Amount";
            header[21] = "SGST Rate";
            header[22] = "SGST Amount";
            header[23] = "CGST Rate";
            header[24] = "CGST Amount";
            header[25] = "IGST Rate";
            header[26] = "IGST Amount";
            header[27] = "CESS Rate";
            header[28] = "CESS Amount";
            header[29] = "Eligibilty of ITC";
            header[30] = "ITC IGST (Amt)";
            header[31] = "ITC CGST (Amt)";
            header[32] = "ITC SGST (Amt)";
            header[33] = "ITC Cess (Amt)";
            header[34] = "Withholding Tax";
            header[35] = "Advance Adjustment";
            header[36] = "Net Payment Due";
            header[37] = "Mode of Receipt";
            header[38] = "Bank Name";
            header[39] = "Instrument No.";
            header[40] = "Instrument Date";
            header[41] = "Pay Mode Description";
            header[42] = "Import from Country";
            header[43] = "Foreign Currency";
            header[44] = "Foreign Currency Amount";
            header[45] = "Inv/Reference Date";
            header[46] = "Inv / Reference - Number";
            header[47] = "DC / GRN / Reference Date";
            header[48] = "DC / GRN / Reference Number";
            header[49] = "Import Reference Date";
            header[50] = "Import Reference Number";
            header[51] = "Port Code";
            header[52] = "Remarks1";
            header[53] = "Remarks2";
            header[54] = "Remarks3";
            header[55] = "Status";
            header[56] = "";
            header[57] = "";
            header[58] = "";
            header[59] = "";
            header[60] = "";
            header[61] = "";
            header[62] = "";
            header[63] = "";
            header[64] = "";
            header[65] = "";
            header[66] = "";
            header[67] = "";
            header[68] = "";
            header[69] = "";
            // Cell Creation and Formating
            for (int i = 0; i < MAX_COLUMN; i++) {
                cell[i] = row.createCell(i);
                cell[i].setCellValue(createHelper.createRichTextString(header[i]));
                cell[i].setCellStyle(unlockedCellStyle);
            }
            noOfRows = 1;
            transactionList = buyTxnList;
            for (Transaction txn : transactionList) {
                List<TransactionItems> listTransactionItems = TRANSACTION_ITEM_DAO.findByTransaction(em,
                        txn.getTransactionBranchOrganization().getId(), txn.getId());
                if (listTransactionItems != null && listTransactionItems.size() > 0) {
                    noOfRows = writeMultiItemsTrans(listTransactionItems, txn, createHelper, sheetBuyTxn, noOfRows, 2,
                            null, em, 3);
                } else {
                    noOfRows = writeTransaction(listTransactionItems, txn, createHelper, sheetBuyTxn, noOfRows, 2, null,
                            em, 3);
                }
            }

            for (int i = 0; i < MAX_COLUMN; i++) {
                sheetBuyTxn.autoSizeColumn(i);
            }
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "pjeTxnList: " + pjeTxnList.size());
        if (pjeTxnList.size() > 0) { // Provision/Journal Entry txns
            String sheetProvTxnName = IdosUtil.getOrganizationName4Report(user) + "_JournalEntries";
            Sheet sheetProvTxn = wb.createSheet(sheetProvTxnName);
            row = sheetProvTxn.createRow((short) 0);
            createHelper = wb.getCreationHelper();
            unlockedCellStyle = wb.createCellStyle();
            unlockedCellStyle.setLocked(false);
            header = new String[MAX_COLUMN];
            // Set Header for Buy
            header[0] = "Transaction Reference Number";
            header[1] = "Date";
            header[2] = "Creator";
            header[3] = "Approver";
            header[4] = "Debit Branch";
            header[5] = "Dr- Project";
            header[6] = "Dr- ledger";
            header[7] = "Dr-No. Of Units";
            header[8] = "Dr-Price per unit";
            header[9] = "Dr-Amount";
            header[10] = "Credit Branch";
            header[11] = "Cr- project";
            header[12] = "Cr- Ledger";
            header[13] = "Cr- No. of units";
            header[14] = "Cr-Price per unit";
            header[15] = "Cr-Amount";
            header[16] = "Total Amount";
            header[17] = "Instrument number";
            header[18] = "Instrument date";
            header[19] = "Purpose";
            header[20] = "Alert for Reversal";
            header[21] = "Date of Alert for Reversal";
            header[22] = "Remarks";
            header[23] = "System Date";
            header[24] = "Status";
            header[25] = "";
            header[26] = "";
            header[27] = "";
            header[28] = "";
            header[29] = "";
            header[30] = "";
            header[31] = "";
            header[32] = "";
            header[33] = "";
            header[34] = "";
            header[35] = "";
            header[36] = "";
            header[37] = "";
            header[38] = "";
            header[39] = "";
            header[40] = "";
            header[41] = "";
            header[42] = "";
            header[43] = "";
            header[44] = "";
            header[45] = "";
            header[46] = "";
            header[47] = "";
            header[48] = "";
            header[49] = "";
            header[50] = "";
            header[51] = "";
            header[52] = "";
            header[53] = "";
            header[54] = "";
            header[55] = "";
            header[56] = "";
            header[57] = "";
            header[58] = "";
            header[59] = "";
            header[60] = "";
            header[61] = "";
            header[62] = "";
            header[63] = "";
            header[64] = "";
            header[65] = "";
            header[66] = "";
            header[67] = "";
            header[68] = "";
            header[69] = "";
            // Cell Creation and Formating
            for (int i = 0; i < MAX_COLUMN; i++) {
                cell[i] = row.createCell(i);
                cell[i].setCellValue(createHelper.createRichTextString(header[i]));
                cell[i].setCellStyle(unlockedCellStyle);
            }
            noOfRows = 1;
            noOfRows = writeJournalEntries(user.getOrganization(), pjeTxnList, searchItems, searchVendors,
                    searchCustomers, createHelper, sheetProvTxn, noOfRows, 2, null, em);
            for (int i = 0; i < MAX_COLUMN; i++) {
                sheetProvTxn.autoSizeColumn(i);
            }
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "pjeObjectList: " + otherTxnList.size());
        if (otherTxnList.size() > 0) {
            String sheetNameTxn = IdosUtil.getOrganizationName4Report(user) + "_other";
            Sheet sheetTxn = wb.createSheet(sheetNameTxn);
            row = sheetTxn.createRow((short) 0);
            createHelper = wb.getCreationHelper();
            unlockedCellStyle = wb.createCellStyle();
            unlockedCellStyle.setLocked(false);
            header = new String[MAX_COLUMN];

            header[0] = "Transaction Reference Number";
            header[1] = "Branch";
            header[2] = "Project";
            header[3] = "Transaction Purpose";
            header[4] = "Specifics";
            header[5] = "Customer/Vendor";
            header[6] = "Date";
            header[7] = "Payment Mode";
            header[8] = "Payment Mode Description";
            header[9] = "No Of Units";
            header[10] = "Price per Unit";
            header[11] = "Gross Amount";
            header[12] = "Net Amount";
            header[13] = "Net Amount Description";
            header[14] = "Created By";
            header[15] = "Approved By";
            header[16] = "Remarks";
            header[17] = "Status";
            header[18] = "Payment Status";
            header[19] = "Tax Name 1";
            header[20] = "Tax Value 1";
            header[21] = "Tax Rate 1";
            header[22] = "Tax Name 2";
            header[23] = "Tax Value 2";
            header[24] = "Tax Rate 2";
            header[25] = "Tax Name 3";
            header[26] = "Tax Value 3";
            header[27] = "Tax Rate 3";
            header[28] = "Tax Name 4";
            header[29] = "Tax Value 4";
            header[30] = "Tax Rate 4";
            header[31] = "Tax Name 5";
            header[32] = "Tax Value 5";
            header[33] = "Tax Rate 5";
            header[34] = "Tax Name 6";
            header[35] = "Tax Value 6";
            header[36] = "Tax Rate 6";
            header[37] = "Tax Name 7";
            header[38] = "Tax Value 7";
            header[39] = "Tax Rate 7";
            header[40] = "Withholding Tax";
            header[41] = "Customer GST No";
            header[42] = "Vendor GST No";
            header[43] = "Invoice Number";
            header[44] = "Invoice Date";
            header[45] = "Status";
            header[46] = "";
            header[47] = "";
            header[48] = "";
            header[49] = "";
            header[50] = "";
            header[51] = "";
            header[52] = "";
            header[53] = "";
            header[54] = "";
            header[55] = "";
            header[56] = "";
            header[57] = "";
            header[58] = "";
            header[59] = "";
            header[60] = "";
            header[61] = "";
            header[62] = "";
            header[63] = "";
            header[64] = "";
            header[65] = "";
            header[66] = "";
            header[67] = "";
            header[68] = "";
            header[69] = "";

            for (int i = 0; i < MAX_COLUMN; i++) {
                cell[i] = row.createCell(i);
                cell[i].setCellValue(createHelper.createRichTextString(header[i]));
                cell[i].setCellStyle(unlockedCellStyle);
            }
            noOfRows = 1;
            transactionList = otherTxnList;
            for (Transaction txn : transactionList) {
                List<TransactionItems> listTransactionItems = TRANSACTION_ITEM_DAO.findByTransaction(em,
                        txn.getTransactionBranchOrganization().getId(), txn.getId());
                if (listTransactionItems != null && listTransactionItems.size() > 0) {
                    noOfRows = writeMultiItemsTrans(listTransactionItems, txn, createHelper, sheetTxn, noOfRows, 2,
                            null, em, 1);
                } else {
                    noOfRows = writeTransaction(listTransactionItems, txn, createHelper, sheetTxn, noOfRows, 2, null,
                            em, 1);
                }
            }

            // int size=noOfRows; //transactionList.size()+1;
            // sheetTxn.setAutoFilter(CellRangeAddress.valueOf("A1:AH"+size+""));
            for (int i = 0; i < MAX_COLUMN; i++) {
                sheetTxn.autoSizeColumn(i);
            }
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "ENd: ");
        FileOutputStream fileOut = new FileOutputStream(path);
        wb.write(fileOut);
        fileOut.close();
        return fileName;
    }

    @Override
    public File createtransactioncsv(Users user, JsonNode json, EntityManager em, String path, final String fileName,
            final List<Transaction> txnList, final List<IdosProvisionJournalEntry> pjeTxnList) throws Exception {
        log.log(Level.FINE, "============ Start");
        String searchItems = json.findValue("searchItems") != null ? json.findValue("searchItems").asText() : null;
        String searchVendors = json.findValue("searchVendors") != null ? json.findValue("searchVendors").asText()
                : null;
        String searchCustomers = json.findValue("searchCustomers") != null ? json.findValue("searchCustomers").asText()
                : null;

        FileWriter writer = null;
        File file = null;

        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        path = path + fileName;
        file = new File(path);
        writer = new FileWriter(file);
        writer.append("Transaction Reference Number").append('|');
        writer.append("Branch").append('|');
        writer.append("Project").append('|');
        writer.append("Transaction Purpose").append('|');
        writer.append("Specifics").append('|');
        writer.append("Customer/Vendor").append('|');
        writer.append("Date").append('|');
        writer.append("Payment Mode").append('|');
        writer.append("Payment Mode Description").append('|');
        writer.append("No Of Units").append('|');
        writer.append("Price per Unit").append('|');
        writer.append("Gross Amount").append('|');
        writer.append("Net Amount").append('|');
        writer.append("Net Amount Description").append('|');
        writer.append("Created By").append('|');
        writer.append("Approved By").append('|');
        writer.append("Remarks").append('|');
        writer.append("Status").append('|');
        writer.append("Payment Status").append('|');
        writer.append("Tax Name 1").append('|');
        writer.append("Tax Value 1").append('|');
        writer.append("Tax Rate 1").append('|');
        writer.append("Tax Name 2").append('|');
        writer.append("Tax Value 2").append('|');
        writer.append("Tax Rate 2").append('|');
        writer.append("Tax Name 3").append('|');
        writer.append("Tax Value 3").append('|');
        writer.append("Tax Rate 3").append('|');
        writer.append("Tax Name 4").append('|');
        writer.append("Tax Value 4").append('|');
        writer.append("Tax Rate 4").append('|');
        writer.append("Tax Name 5").append('|');
        writer.append("Tax Value 5").append('|');
        writer.append("Tax Rate 5").append('|');
        writer.append("Tax Name 6").append('|');
        writer.append("Tax Value 6").append('|');
        writer.append("Tax Rate 6").append('|');
        writer.append("Tax Name 7").append('|');
        writer.append("Tax Value 7").append('|');
        writer.append("Tax Rate 7").append('|');
        writer.append("Customer Gst No").append('|');
        writer.append("Vendor Gst No").append('|');
        writer.append("Invoice No").append('|');
        writer.append("Invoice Date").append('\n');
        int noOfRows = 0;

        for (Transaction txn : txnList) {
            List<TransactionItems> listTransactionItems = TRANSACTION_ITEM_DAO.findByTransaction(em,
                    txn.getTransactionBranchOrganization().getId(), txn.getId());
            if (listTransactionItems != null && listTransactionItems.size() > 0) {
                noOfRows = writeMultiItemsTrans(listTransactionItems, txn, null, null, noOfRows, 1, writer, em, 1);
            } else {
                noOfRows = writeTransaction(listTransactionItems, txn, null, null, noOfRows, 1, writer, em, 1);
            }
        }
        noOfRows = writeJournalEntries(user.getOrganization(), pjeTxnList, searchItems, searchVendors, searchCustomers,
                null, null, noOfRows, 1, writer, em);
        writer.flush();
        writer.close();
        return file;
    }

    @Override
    public String createOrgCOAExcel(Organization org, EntityManager em, String path, String sheetName)
            throws Exception {
        log.log(Level.FINE, "============ Start");
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("organization.id", org.getId());
        criterias.put("presentStatus", 1);
        List<Specifics> specfList = genericDAO.findByCriteria(Specifics.class, criterias, em);
        // String orgName=org.getName().replaceAll("\\s", "");
        // String sheetName=orgName+"COA";
        // String
        // path=application.path().toString()+"/logs/OrgChartOfAccounts/"+fileName;
        // String
        // path1=application.path().toString()+"/target/scala-2.10/classes/public/OrgChartOfAccounts/"+fileName;
        Workbook wb = new XSSFWorkbook();
        Sheet sheets = wb.createSheet(sheetName);
        CreationHelper createHelper = wb.getCreationHelper();
        for (int i = 0; i < specfList.size(); i++) {
            Row row = sheets.createRow((short) i);
            if (specfList.get(i).getAccountCodeHirarchy() != null) {
                String exceptStartEndSleash = specfList.get(i).getAccountCodeHirarchy().substring(1,
                        specfList.get(i).getAccountCodeHirarchy().length() - 1);
                String[] accCodeArray = exceptStartEndSleash.split("/");
                for (int j = 0; j < accCodeArray.length; j++) {
                    Long actCode = Long.valueOf(accCodeArray[j]);
                    if (j == 0) {
                        criterias.clear();
                        criterias.put("accountCode", actCode);
                        criterias.put("organization.id", org.getId());
                        criterias.put("presentStatus", 1);
                        Particulars foundParticular = genericDAO.getByCriteria(Particulars.class, criterias, em);
                        if (foundParticular != null) {
                            row.createCell(j)
                                    .setCellValue(createHelper.createRichTextString(foundParticular.getName()));
                        }
                    } else if (j > 0) {
                        StringBuilder sbquery = new StringBuilder("");
                        sbquery.append("select obj from Specifics obj WHERE (obj.accountCode='" + actCode
                                + "') and obj.organization.id ='" + org.getId() + "' and obj.presentStatus=1");
                        List<Specifics> foundSpecific = genericDAO.executeSimpleQuery(sbquery.toString(), em);
                        if (foundSpecific.size() > 0) {
                            row.createCell(j)
                                    .setCellValue(createHelper.createRichTextString(foundSpecific.get(0).getName()));
                        }
                    }
                }
                row.createCell(accCodeArray.length)
                        .setCellValue(createHelper.createRichTextString(specfList.get(i).getName()));
            }
        }

        FileOutputStream fileOut = new FileOutputStream(path);
        // FileOutputStream fileOut1 = new FileOutputStream(path1);
        wb.write(fileOut);
        // wb = new XSSFWorkbook(new FileInputStream(path));
        // wb.write(fileOut1);
        fileOut.close();
        // fileOut1.close();
        return sheetName;
    }

    @Override
    public String createOrgCOATemplateExcel(Users user, EntityManager em, int coaType, String path, String sheetName)
            throws Exception {
        log.log(Level.FINE, "============ Start");
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("presentStatus", 1);
        List<Branch> branchList = genericDAO.findByCriteria(Branch.class, criterias, em);
        String branchName = "";
        for (Branch branch : branchList) {
            branchName += branch.getName() + ",";
        }
        branchName = branchName.substring(0, branchName.length() - 1);

        criterias.clear();
        criterias.put("questionType", 1);
        criterias.put("presentStatus", 1);
        String transPurposeNames = "";
        List<TransactionPurpose> listOftxnPurpose = genericDAO.findByCriteria(TransactionPurpose.class, criterias, em);
        for (TransactionPurpose txnPurpose : listOftxnPurpose) {
            if (txnPurpose.getId() == 1 || txnPurpose.getId() == 2 || txnPurpose.getId() == 5 || txnPurpose.getId() == 6
                    || txnPurpose.getId() == 12) {
                if (coaType == 1 || coaType == 3) {
                    transPurposeNames += txnPurpose.getTransactionPurpose() + ",";
                }
            }
            if (txnPurpose.getId() == 3 || txnPurpose.getId() == 4 || txnPurpose.getId() == 11
                    || txnPurpose.getId() == 7 || txnPurpose.getId() == 8 || txnPurpose.getId() == 13) {
                if (coaType == 2 || coaType == 3) {
                    transPurposeNames += txnPurpose.getTransactionPurpose() + ",";
                }
            }
            if (txnPurpose.getId() == 14 || txnPurpose.getId() == 22 || txnPurpose.getId() == 23
                    || txnPurpose.getId() == 24 || txnPurpose.getId() == 25) {
                if (coaType == 3) {
                    transPurposeNames += txnPurpose.getTransactionPurpose() + ",";
                }
            }
            if (txnPurpose.getId() == 7 || txnPurpose.getId() == 8 || txnPurpose.getId() == 13
                    || txnPurpose.getId() == 24) {
                if (coaType == 4) {
                    transPurposeNames += txnPurpose.getTransactionPurpose() + ",";
                }
            }
            if (txnPurpose.getId() == 27 || txnPurpose.getId() == 28) { // prepare quotation and performa invoice
                if (coaType == 1) {
                    transPurposeNames += txnPurpose.getTransactionPurpose() + ",";
                }
            }
            if (txnPurpose.getId() == 29 || txnPurpose.getId() == 25) { // purchase order
                if (coaType == 2) {
                    transPurposeNames += txnPurpose.getTransactionPurpose() + ",";
                }
            }
        }
        transPurposeNames = transPurposeNames.substring(0, transPurposeNames.length() - 1);

        criterias.clear();
        criterias.put("presentStatus", 1);
        List<CoaValidationMapping> coaValidationMappingList = genericDAO.findByCriteria(CoaValidationMapping.class,
                criterias, em);

        /*
         * String sbquery = null;
         * if(coaType == 1 || coaType == 2){
         * sbquery = new
         * String("select obj from CoaValidationPLBS obj where obj.presentStatus=1 and obj.id <= 9"
         * );
         * }else{
         * sbquery = new
         * String("select obj from CoaValidationPLBS obj where obj.presentStatus=1 and obj.id >=10"
         * );
         * }
         * List<CoaValidationPLBS> coaValidationPLBSList =
         * genericDAO.executeSimpleQuery(sbquery, em);
         */

        List<Specifics> orgSpecificsList = null;
        List<String> withholdingTypeList = null;
        if (coaType == 1) { // In Income, map to expense specifics for inventory purpose
            // orgSpecificsList = user.getOrganization().getSpecifics();
            ChartOfAccountsService coaService = new ChartOfAccountsServiceImpl();
            orgSpecificsList = coaService.getExpensesCoaChildNodes(em, user);
        }
        if (coaType == 2) {
            withholdingTypeList = new ArrayList<>(Arrays.asList("Sec192-Payment of Salary",
                    "Sec194A-Income by way of Interest other than Interest on Securities",
                    "Sec194C-Payment to Contractors/SubContractors - Individuals / HUF",
                    "Sec194C-Payment to Contractors/SubContractors - Others", "Sec194H-Commission or Brokerage",
                    "Sec194-I-Rent-(a) Plant and Machinery",
                    "Sec194-I-Rent-(b)-Land or building or furniture or fitting",
                    "Sec-194J-Fees for Professional/Technical Service etc."));
        }
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheets = workbook.createSheet(sheetName);

        criterias.clear();
        criterias.put("presentStatus", 1);
        criterias.put("particularType", coaType);
        List<CoaTemplateAccounts> coaTemplateAccountsList = genericDAO.findByCriteria(CoaTemplateAccounts.class,
                criterias, em);

        // String path1 = application.path().toString() +
        // "/target/scala-2.10/classes/public/OrgChartOfAccounts/" + fileName;

        CreationHelper createHelper = workbook.getCreationHelper();

        XSSFSheet hiddenMappingSheet = workbook.createSheet("hiddenMapping");
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheets);
        int totalHiddenRows = 0;
        if (coaValidationMappingList.size() > totalHiddenRows) {
            totalHiddenRows = coaValidationMappingList.size();
        }
        /*
         * else {
         * totalHiddenRows = coaValidationPLBSList.size();
         * }
         */
        if (coaType == 1) {
            if (orgSpecificsList.size() > totalHiddenRows)
                totalHiddenRows = orgSpecificsList.size();
        }
        if (coaType == 2) {
            if (withholdingTypeList.size() > totalHiddenRows)
                totalHiddenRows = withholdingTypeList.size();
        }

        for (int i = 0; i < totalHiddenRows; i++) {
            XSSFRow row = hiddenMappingSheet.createRow(i);
            if (i < coaValidationMappingList.size()) {
                XSSFCell cell = row.createCell(0);
                cell.setCellValue(coaValidationMappingList.get(i).getMappingName());
            }
            /*
             * if (i < coaValidationPLBSList.size()) {
             * XSSFCell cellPLBS = row.createCell(1);
             * cellPLBS.setCellValue(coaValidationPLBSList.get(i).getValidationName());
             * }
             */

            if (coaType == 1) {
                if (i < orgSpecificsList.size()) {
                    XSSFCell cell = row.createCell(2);
                    cell.setCellValue(orgSpecificsList.get(i).getName());
                }
            }

            if (coaType == 2) {
                if (i < withholdingTypeList.size()) {
                    XSSFCell cell = row.createCell(2);
                    cell.setCellValue(withholdingTypeList.get(i));
                }
            }
        }
        Name namedCell = workbook.createName();
        namedCell.setNameName("mappingList");
        namedCell.setRefersToFormula("hiddenMapping!$A$1:$A$" + coaValidationMappingList.size());
        XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("mappingList");

        /*
         * namedCell = workbook.createName();
         * namedCell.setNameName("plbsList");
         * namedCell.setRefersToFormula("hiddenMapping!$B$1:$B$" +
         * coaValidationPLBSList.size());
         * XSSFDataValidationConstraint dvConstraintPLBS =
         * (XSSFDataValidationConstraint)
         * dvHelper.createFormulaListConstraint("plbsList");
         */

        XSSFDataValidationConstraint orgSpecificsConstraint = null;
        // XSSFDataValidationConstraint locationConstraint = null;
        XSSFDataValidationConstraint withholdingTypeConstraint = null;
        XSSFDataValidationConstraint GSTGoodServicesConstraint = null;
        XSSFDataValidationConstraint GSTTypesOfGoodServicesConstraint = null;
        XSSFDataValidationConstraint inventoryValuationMethodConstraint = null;
        String userEmailList = "";
        if (coaType == 1) {
            // locationConstraint = (XSSFDataValidationConstraint)
            // dvHelper.createExplicitListConstraint(new String[]{"Domestic",
            // "International", "Both"});
            inventoryValuationMethodConstraint = (XSSFDataValidationConstraint) dvHelper
                    .createExplicitListConstraint(new String[] { "FIFO", "WAC" });
            if (orgSpecificsList.size() > 0) {
                namedCell = workbook.createName();
                namedCell.setNameName("orgSpecificsList");
                namedCell.setRefersToFormula("hiddenMapping!$C$1:$C$" + orgSpecificsList.size());
                orgSpecificsConstraint = (XSSFDataValidationConstraint) dvHelper
                        .createFormulaListConstraint("orgSpecificsList");
            }

            criterias.clear();
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            List<Users> usersList = genericDAO.findByCriteria(Users.class, criterias, em);

            for (Users usr : usersList) {
                userEmailList += usr.getEmail() + ",";
            }
            userEmailList = userEmailList.substring(0, userEmailList.length() - 1);

        } else if (coaType == 2) {
            namedCell = workbook.createName();
            namedCell.setNameName("withholdingTypeList");
            namedCell.setRefersToFormula("hiddenMapping!$C$1:$C$" + withholdingTypeList.size());
            withholdingTypeConstraint = (XSSFDataValidationConstraint) dvHelper
                    .createFormulaListConstraint("withholdingTypeList");
        }

        if (coaType == 1 || coaType == 2) {
            GSTGoodServicesConstraint = (XSSFDataValidationConstraint) dvHelper
                    .createExplicitListConstraint(new String[] { "Goods", "Services" });
            GSTTypesOfGoodServicesConstraint = (XSSFDataValidationConstraint) dvHelper
                    .createExplicitListConstraint(new String[] { "GST Exempt Goods/Services",
                            "Nil Rate Goods /Services", "Non GST Goods/ Services" });
        }

        workbook.setSheetHidden(1, true);
        XSSFDataValidationConstraint yesNoConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "Yes", "No" });
        XSSFDataValidationConstraint yesConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "Yes" });
        Row headerRow = sheets.createRow(0);
        headerRow.createCell(0).setCellValue(createHelper.createRichTextString("Account Name"));
        headerRow.createCell(1).setCellValue(createHelper.createRichTextString("Sub Account Name"));
        headerRow.createCell(2).setCellValue(createHelper.createRichTextString("Branch Name                   "));
        headerRow.createCell(3).setCellValue(createHelper.createRichTextString("Opening Balances              "));

        if (coaType == 1) {
            headerRow.createCell(4)
                    .setCellValue(createHelper.createRichTextString("Max. Discount Percentage For Walkin Customer "));
            headerRow.createCell(5).setCellValue(
                    createHelper.createRichTextString("Transaction Type                                         "));
            headerRow.createCell(6)
                    .setCellValue(createHelper.createRichTextString("Identification for Data Validation"));
        } else if (coaType == 2) {

            headerRow.createCell(4).setCellValue(createHelper.createRichTextString("No of unit"));
            headerRow.createCell(5).setCellValue(createHelper.createRichTextString("Rate "));
            headerRow.createCell(6).setCellValue(createHelper.createRichTextString("Inventory Value"));
            headerRow.createCell(7).setCellValue(
                    createHelper.createRichTextString("Transaction Type                                         "));
            headerRow.createCell(8)
                    .setCellValue(createHelper.createRichTextString("Identification for Data Validation"));
        } else {
            headerRow.createCell(4).setCellValue(
                    createHelper.createRichTextString("Transaction Type                                         "));
            headerRow.createCell(5)
                    .setCellValue(createHelper.createRichTextString("Identification for Data Validation"));
            headerRow.createCell(6).setCellValue(createHelper.createRichTextString("Is Transaction Editable?"));
        }

        /*
         * if (coaType == 1 || coaType == 2) {
         * headerRow.createCell(5).setCellValue(createHelper.
         * createRichTextString("Identification for PL"));
         * }else
         */

        if (coaType == 2) {
            // headerRow.createCell(6).setCellValue(createHelper.createRichTextString("Capital/Revenue"));
            headerRow.createCell(9).setCellValue(createHelper.createRichTextString("GST Goods/Services"));
            headerRow.createCell(10).setCellValue(createHelper.createRichTextString("HSN/SAC Code"));
            headerRow.createCell(11).setCellValue(createHelper.createRichTextString("Type Of Goods/Services"));
            headerRow.createCell(12).setCellValue(createHelper.createRichTextString("Buy Unit OF Measure"));
            headerRow.createCell(13).setCellValue(createHelper.createRichTextString("Is Withholding Applicable?"));
            headerRow.createCell(14).setCellValue(createHelper.createRichTextString("Withholding Tax Type"));
            headerRow.createCell(15).setCellValue(createHelper.createRichTextString("Withholding Rate"));
            headerRow.createCell(16).setCellValue(createHelper.createRichTextString("Withholding Transaction limits"));
            headerRow.createCell(17).setCellValue(createHelper.createRichTextString("Withholding Monetary limits"));
            headerRow.createCell(18).setCellValue(createHelper.createRichTextString("Capture Input Taxes?"));
            headerRow.createCell(19)
                    .setCellValue(createHelper.createRichTextString("Is this eligible for expense claims?"));
            // headerRow.createCell(13).setCellValue(createHelper.createRichTextString("This
            // Item Can Be Purchased On Loan Account?"));
            headerRow.createCell(20).setCellValue(createHelper.createRichTextString("Knowledge Library Instructions"));
            headerRow.createCell(21).setCellValue(createHelper.createRichTextString("Is Knowledge Library Mandatory?"));
            headerRow.createCell(22).setCellValue(
                    createHelper.createRichTextString("Knowledge Library Applicable In Branches				"));
        } else if (coaType == 1) {
            headerRow.createCell(7).setCellValue(createHelper.createRichTextString("Invoice Description-1"));
            headerRow.createCell(8).setCellValue(createHelper.createRichTextString("Invoice Description-2"));
            headerRow.createCell(9).setCellValue(createHelper.createRichTextString("Price Per Unit"));
            headerRow.createCell(10).setCellValue(createHelper.createRichTextString("GST Goods/Services"));
            headerRow.createCell(11).setCellValue(createHelper.createRichTextString("HSN/SAC Code"));
            headerRow.createCell(12).setCellValue(createHelper.createRichTextString("Type Of Goods/Services"));
            headerRow.createCell(13).setCellValue(createHelper.createRichTextString("Tax Rate(%)"));
            headerRow.createCell(14).setCellValue(
                    createHelper.createRichTextString("Map Income To Expense Item(NOTE:FIRST UPLOAD EXPENSE ITEMS)"));
            headerRow.createCell(15).setCellValue(
                    createHelper.createRichTextString("Valuation Method(NOTE:EXPENSE ITEM MAPPING MANDATORY)"));
            headerRow.createCell(16).setCellValue(createHelper.createRichTextString("Buy Units in Number"));
            headerRow.createCell(17).setCellValue(createHelper.createRichTextString("Equivalent Sell Units In Number"));
            headerRow.createCell(18).setCellValue(createHelper.createRichTextString("Sell Unit Of Measure"));
            headerRow.createCell(19).setCellValue(createHelper.createRichTextString("Reorder Level Branch"));
            headerRow.createCell(20).setCellValue(createHelper.createRichTextString("Reorder Level email"));
            headerRow.createCell(21).setCellValue(createHelper.createRichTextString("Reorder Level Item Inventory"));
            headerRow.createCell(22).setCellValue(createHelper.createRichTextString("Is Transaction Editable?"));
            headerRow.createCell(23).setCellValue(createHelper.createRichTextString("Knowledge Library Instructions"));
            headerRow.createCell(24).setCellValue(createHelper.createRichTextString("Is Knowledge Library Mandatory?"));
            headerRow.createCell(25).setCellValue(
                    createHelper.createRichTextString("Knowledge Library Applicable In Branches				"));
            headerRow.createCell(26)
                    .setCellValue(createHelper.createRichTextString("Is Price Inclusive of Tax			"));

            // headerRow.createCell(8).setCellValue(createHelper.createRichTextString("Domestic/International"));
        } else if (coaType == 3 || coaType == 4) {
            headerRow.createCell(7).setCellValue(createHelper.createRichTextString("Knowledge Library Instructions"));
            headerRow.createCell(8).setCellValue(createHelper.createRichTextString("Is Knowledge Library Mandatory?"));
            headerRow.createCell(9).setCellValue(
                    createHelper.createRichTextString("Knowledge Library Applicable In Branches				"));
        }
        XSSFCellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        for (int b = 0; b < headerRow.getLastCellNum(); b++) {
            sheets.autoSizeColumn(b);
            headerRow.getCell(b).setCellStyle(style);
        }
        sheets.createFreezePane(0, 1, 0, 1);
        int rowCount = 1;
        for (CoaTemplateAccounts coaTemplateAccounts : coaTemplateAccountsList) {
            Row mainRow = sheets.createRow((short) rowCount);
            mainRow.createCell(0).setCellValue(createHelper.createRichTextString(coaTemplateAccounts.getAccountName()));
            mainRow.createCell(1)
                    .setCellValue(createHelper.createRichTextString(coaTemplateAccounts.getSubAccountName()));
            mainRow.createCell(2).setCellValue(createHelper.createRichTextString(branchName));
            CellRangeAddressList GSTGoodsList = null;
            CellRangeAddressList GSTTypeOfGoodsList = null;
            CellRangeAddressList KLMandatoryList = null;
            if (coaType == 1) {
                mainRow.createCell(3).setCellValue(createHelper.createRichTextString(null));
                mainRow.createCell(4).setCellValue(createHelper.createRichTextString(null));
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setWrapText(true);
                Cell cell = mainRow.createCell(5);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(createHelper.createRichTextString(transPurposeNames));

                CellRangeAddressList addressList = new CellRangeAddressList(rowCount, rowCount, 6, 6);
                XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint,
                        addressList);
                validation.setShowErrorBox(true);
                sheets.addValidationData(validation);

                /*
                 * CellRangeAddressList locationList = new CellRangeAddressList(rowCount,
                 * rowCount, 8, 8);
                 * XSSFDataValidation locationValidation = (XSSFDataValidation)
                 * dvHelper.createValidation(locationConstraint, locationList);
                 * validationPLBS.setShowErrorBox(true);
                 * sheets.addValidationData(locationValidation);
                 */

                GSTGoodsList = new CellRangeAddressList(rowCount, rowCount, 10, 10);
                GSTTypeOfGoodsList = new CellRangeAddressList(rowCount, rowCount, 12, 12);
                KLMandatoryList = new CellRangeAddressList(rowCount, rowCount, 24, 24);

                if (orgSpecificsConstraint != null) { // map income to expense units
                    CellRangeAddressList orgSpecificsAddressList = new CellRangeAddressList(rowCount, rowCount, 14, 14);
                    XSSFDataValidation orgSpecificsValidation = (XSSFDataValidation) dvHelper
                            .createValidation(orgSpecificsConstraint, orgSpecificsAddressList);
                    orgSpecificsValidation.setShowErrorBox(true);
                    sheets.addValidationData(orgSpecificsValidation);
                }
                CellRangeAddressList inventoryValuationList = new CellRangeAddressList(rowCount, rowCount, 15, 15); // valuation
                                                                                                                    // method
                                                                                                                    // WAC/FIFO
                XSSFDataValidation invValuationValidation = (XSSFDataValidation) dvHelper
                        .createValidation(inventoryValuationMethodConstraint, inventoryValuationList);
                invValuationValidation.setShowErrorBox(true);
                sheets.addValidationData(invValuationValidation);

                Cell cell10 = mainRow.createCell(19); // Recorder level branch
                cell10.setCellStyle(cellStyle);
                cell10.setCellValue(createHelper.createRichTextString(branchName));
                Cell cell11 = mainRow.createCell(20); // recorder level email
                cell11.setCellStyle(cellStyle);
                cell11.setCellValue(createHelper.createRichTextString(userEmailList));

                mainRow.createCell(21).setCellValue(createHelper.createRichTextString("100,200"));

                CellRangeAddressList isTranEditableList = new CellRangeAddressList(rowCount, rowCount, 22, 22);
                XSSFDataValidation isTranEditableValidation = (XSSFDataValidation) dvHelper
                        .createValidation(yesNoConstraint, isTranEditableList);
                isTranEditableValidation.setShowErrorBox(true);
                sheets.addValidationData(isTranEditableValidation);

                mainRow.createCell(25).setCellValue(createHelper.createRichTextString(branchName));
                CellRangeAddressList isPriceInclusiveOfTax = new CellRangeAddressList(rowCount, rowCount, 26, 26);
                XSSFDataValidation isPriceInclusiveOfTaxValidation = (XSSFDataValidation) dvHelper
                        .createValidation(yesConstraint, isPriceInclusiveOfTax);
                isPriceInclusiveOfTaxValidation.setShowErrorBox(true);
                sheets.addValidationData(isPriceInclusiveOfTaxValidation);
            } else if (coaType == 2) {
                mainRow.createCell(3).setCellValue(createHelper.createRichTextString(null));
                mainRow.createCell(4).setCellValue(createHelper.createRichTextString(null));
                mainRow.createCell(5).setCellValue(createHelper.createRichTextString(null));
                mainRow.createCell(6).setCellValue(createHelper.createRichTextString(null));

                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setWrapText(true);
                Cell cell = mainRow.createCell(7);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(createHelper.createRichTextString(transPurposeNames));

                CellRangeAddressList addressList = new CellRangeAddressList(rowCount, rowCount, 8, 8);
                XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint,
                        addressList);
                validation.setShowErrorBox(true);
                sheets.addValidationData(validation);
                GSTGoodsList = new CellRangeAddressList(rowCount, rowCount, 9, 9);
                GSTTypeOfGoodsList = new CellRangeAddressList(rowCount, rowCount, 11, 11);
                KLMandatoryList = new CellRangeAddressList(rowCount, rowCount, 23, 23);

                CellRangeAddressList withholdingApplicableList = new CellRangeAddressList(rowCount, rowCount, 13, 13);
                XSSFDataValidation withholdingApplicableValidation = (XSSFDataValidation) dvHelper
                        .createValidation(yesNoConstraint, withholdingApplicableList);
                withholdingApplicableValidation.setShowErrorBox(true);
                // validation.setSuppressDropDownArrow(false);
                withholdingApplicableValidation.setShowErrorBox(true);
                sheets.addValidationData(withholdingApplicableValidation);

                CellRangeAddressList withTypeList = new CellRangeAddressList(rowCount, rowCount, 14, 14);
                XSSFDataValidation validationwithType = (XSSFDataValidation) dvHelper
                        .createValidation(withholdingTypeConstraint, withTypeList);
                validationwithType.setShowErrorBox(true);
                sheets.addValidationData(validationwithType);

                CellRangeAddressList inputTaxList = new CellRangeAddressList(rowCount, rowCount, 18, 18);
                XSSFDataValidation inputTaxValidation = (XSSFDataValidation) dvHelper.createValidation(yesNoConstraint,
                        inputTaxList);
                inputTaxValidation.setShowErrorBox(true);
                sheets.addValidationData(inputTaxValidation);

                CellRangeAddressList expenseClaimsList = new CellRangeAddressList(rowCount, rowCount, 19, 19);
                XSSFDataValidation expenseClaimsValidation = (XSSFDataValidation) dvHelper
                        .createValidation(yesNoConstraint, expenseClaimsList);
                expenseClaimsValidation.setShowErrorBox(true);
                sheets.addValidationData(expenseClaimsValidation);

                /*
                 * CellRangeAddressList purchasedOnLoanList = new CellRangeAddressList(rowCount,
                 * rowCount, 14, 14);
                 * XSSFDataValidation purchasedOnLoanValidation =
                 * (XSSFDataValidation)dvHelper.createValidation(yesNoConstraint,
                 * purchasedOnLoanList);
                 * purchasedOnLoanValidation.setShowErrorBox(true);
                 * purchasedOnLoanValidation.setSuppressDropDownArrow(true);
                 * purchasedOnLoanValidation.setShowErrorBox(true);
                 * sheets.addValidationData(purchasedOnLoanValidation);
                 */

                mainRow.createCell(22).setCellValue(createHelper.createRichTextString(branchName));
            }

            if (coaType == 1 || coaType == 2) {
                XSSFDataValidation GSTGoodsValidation = (XSSFDataValidation) dvHelper
                        .createValidation(GSTGoodServicesConstraint, GSTGoodsList);
                GSTGoodsValidation.setShowErrorBox(true);
                GSTGoodsValidation.setSuppressDropDownArrow(true);
                GSTGoodsValidation.setShowErrorBox(true);
                sheets.addValidationData(GSTGoodsValidation);

                XSSFDataValidation GSTTypeOfValidation = (XSSFDataValidation) dvHelper
                        .createValidation(GSTTypesOfGoodServicesConstraint, GSTTypeOfGoodsList);
                GSTTypeOfValidation.setShowErrorBox(true);
                GSTTypeOfValidation.setSuppressDropDownArrow(true);
                GSTTypeOfValidation.setShowErrorBox(true);
                sheets.addValidationData(GSTTypeOfValidation);

                XSSFDataValidation KLMandatoryValidation = (XSSFDataValidation) dvHelper
                        .createValidation(yesNoConstraint, KLMandatoryList);
                KLMandatoryValidation.setShowErrorBox(true);
                KLMandatoryValidation.setSuppressDropDownArrow(true);
                KLMandatoryValidation.setShowErrorBox(true);
                sheets.addValidationData(KLMandatoryValidation);
            } else if (coaType == 3 || coaType == 4) {
                mainRow.createCell(3).setCellValue(createHelper.createRichTextString(null));

                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setWrapText(true);
                Cell cell = mainRow.createCell(4);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(createHelper.createRichTextString(transPurposeNames));

                CellRangeAddressList addressList = new CellRangeAddressList(rowCount, rowCount, 5, 5);
                XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint,
                        addressList);
                validation.setShowErrorBox(true);
                sheets.addValidationData(validation);
                CellRangeAddressList addressPLBSList = new CellRangeAddressList(rowCount, rowCount, 6, 6); // is
                                                                                                           // transaction
                                                                                                           // Editable?
                // XSSFDataValidation validationPLBS =
                // (XSSFDataValidation)dvHelper.createValidation(dvConstraintPLBS,
                // addressPLBSList);
                XSSFDataValidation validationPLBS = (XSSFDataValidation) dvHelper.createValidation(yesNoConstraint,
                        addressPLBSList);
                validationPLBS.setShowErrorBox(true);
                validationPLBS.setShowErrorBox(true);
                sheets.addValidationData(validationPLBS);

                mainRow.createCell(7).setCellValue(createHelper.createRichTextString(""));

                CellRangeAddressList KLMandatoryList1 = new CellRangeAddressList(rowCount, rowCount, 8, 8);
                XSSFDataValidation KLMandatoryValidation1 = (XSSFDataValidation) dvHelper
                        .createValidation(yesNoConstraint, KLMandatoryList1);
                KLMandatoryValidation1.setShowErrorBox(true);
                KLMandatoryValidation1.setSuppressDropDownArrow(true);
                KLMandatoryValidation1.setShowErrorBox(true);
                sheets.addValidationData(KLMandatoryValidation1);

                mainRow.createCell(9).setCellValue(createHelper.createRichTextString(branchName));
            }
            rowCount++;
        }

        FileOutputStream fileOut = new FileOutputStream(path);
        // FileOutputStream fileOut1 = new FileOutputStream(path1);
        workbook.write(fileOut);
        // workbook = new XSSFWorkbook(new FileInputStream(path));
        // workbook.write(fileOut1);
        fileOut.close();
        // fileOut1.close();
        log.log(Level.FINE, "===============End ");
        return sheetName;
    }

    @Override
    public String createOrgCustomerTemplateExcel(Users user, EntityManager em, String path, String sheetName)
            throws Exception {
        log.log(Level.FINE, "============ Start");
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("presentStatus", 1);
        List<Branch> branchList = genericDAO.findByCriteria(Branch.class, criterias, em);
        String branchName = "";
        for (Branch branch : branchList) {
            branchName += branch.getName() + ",";
        }
        branchName = branchName.substring(0, branchName.length() - 1);
        criterias.clear();
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("groupType", 2);
        List<VendorGroup> custGroupList = genericDAO.findByCriteria(VendorGroup.class, criterias, em);

        ChartOfAccountsService coaService = new ChartOfAccountsServiceImpl();
        List<Specifics> orgSpecificsList = coaService.getIncomesCoaChildNodes(em, user);
        String specificsStr = "";
        for (Specifics spec : orgSpecificsList) {
            specificsStr = specificsStr + spec.getName() + ",";
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheets = workbook.createSheet(sheetName);
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheets);

        // hidden mapping start
        // countries list
        List<String> countries = CountryCurrencyUtil.getCountriesList();
        List<String> phoneCounrtyCodes = CountryTelephoneCodeUtil.getPhoneCountryCodes();
        List<String> gstinStateCodes = IdosUtil.getStateCodes();
        List<String> states = IdosUtil.getStateNames();

        XSSFSheet hiddenMappingSheet = workbook.createSheet("hiddenMapping");

        int totalHiddenRows = 0; // set it to max list size
        if (countries.size() > totalHiddenRows) {
            totalHiddenRows = countries.size();
        }
        if (gstinStateCodes.size() > totalHiddenRows) {
            totalHiddenRows = gstinStateCodes.size();
        }
        if (phoneCounrtyCodes.size() > totalHiddenRows) {
            totalHiddenRows = phoneCounrtyCodes.size();
        }
        if (states.size() > totalHiddenRows) {
            totalHiddenRows = states.size();
        }
        if (custGroupList.size() > totalHiddenRows) {
            totalHiddenRows = custGroupList.size();
        }

        for (int i = 0; i < totalHiddenRows; i++) {
            XSSFRow row = hiddenMappingSheet.createRow(i);
            if (i < countries.size()) {
                XSSFCell cell = row.createCell(0);
                cell.setCellValue(countries.get(i));
            }

            if (i < gstinStateCodes.size()) {
                XSSFCell cell = row.createCell(1);
                cell.setCellValue(gstinStateCodes.get(i));
            }

            if (i < phoneCounrtyCodes.size()) {
                XSSFCell cell = row.createCell(2);
                cell.setCellValue(phoneCounrtyCodes.get(i));
            }

            if (i < states.size()) {
                XSSFCell cell = row.createCell(3);
                cell.setCellValue(states.get(i));
            }
            if (i < custGroupList.size()) {
                XSSFCell cell = row.createCell(4);
                cell.setCellValue(custGroupList.get(i).getGroupName());
            }
        }

        // hidden mapping ends
        XSSFDataValidationConstraint yesNoConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "Yes", "No" });
        workbook.setSheetHidden(1, true);
        Row headerRow = sheets.createRow(0);
        headerRow.createCell(0).setCellValue(createHelper.createRichTextString("Branches 							"));
        headerRow.createCell(1).setCellValue(createHelper.createRichTextString("Branch Opening Balances             "));
        headerRow.createCell(2)
                .setCellValue(createHelper.createRichTextString("Branch Opening Balance Of Advance          "));
        headerRow.createCell(3).setCellValue(createHelper.createRichTextString("Customer Group"));
        headerRow.createCell(4).setCellValue(createHelper.createRichTextString("Customer Name"));
        headerRow.createCell(5).setCellValue(createHelper.createRichTextString("Customer Email"));
        headerRow.createCell(6).setCellValue(createHelper.createRichTextString("Is Customer GST Registered?"));
        headerRow.createCell(7).setCellValue(createHelper.createRichTextString("GSTIN State Code"));
        headerRow.createCell(8).setCellValue(createHelper.createRichTextString("GSTIN Code"));
        headerRow.createCell(9).setCellValue(createHelper.createRichTextString("Customer Type"));
        headerRow.createCell(10).setCellValue(createHelper.createRichTextString("Billing Address"));
        headerRow.createCell(11).setCellValue(createHelper.createRichTextString("Billing Country"));
        headerRow.createCell(12).setCellValue(createHelper.createRichTextString("Billing State"));
        headerRow.createCell(13).setCellValue(createHelper.createRichTextString("Billing Locaiton"));
        headerRow.createCell(14).setCellValue(createHelper.createRichTextString("Billing Country Code"));
        headerRow.createCell(15).setCellValue(createHelper.createRichTextString("Billing Phone No."));
        headerRow.createCell(16).setCellValue(createHelper.createRichTextString("Shipping Address same As Billing?"));
        headerRow.createCell(17).setCellValue(createHelper.createRichTextString("Shipping Address"));
        headerRow.createCell(18).setCellValue(createHelper.createRichTextString("Shipping Country"));
        headerRow.createCell(19).setCellValue(createHelper.createRichTextString("Shipping State"));
        headerRow.createCell(20).setCellValue(createHelper.createRichTextString("Shipping Locaiton"));
        headerRow.createCell(21).setCellValue(createHelper.createRichTextString("Shipping Country Code"));
        headerRow.createCell(22).setCellValue(createHelper.createRichTextString("Shipping Phone No."));
        headerRow.createCell(23).setCellValue(createHelper.createRichTextString("Items Of Sale"));
        headerRow.createCell(24).setCellValue(createHelper.createRichTextString("Statutory Id1"));
        headerRow.createCell(25).setCellValue(createHelper.createRichTextString("ID Number1"));
        headerRow.createCell(26).setCellValue(createHelper.createRichTextString("Statutory Id2"));
        headerRow.createCell(27).setCellValue(createHelper.createRichTextString("ID Number2"));
        headerRow.createCell(28).setCellValue(createHelper.createRichTextString("Statutory Id3"));
        headerRow.createCell(29).setCellValue(createHelper.createRichTextString("ID Number3"));
        headerRow.createCell(30).setCellValue(createHelper.createRichTextString("Cash/Credit?"));
        headerRow.createCell(31).setCellValue(createHelper.createRichTextString("Customer Days Of Credit"));
        headerRow.createCell(32).setCellValue(createHelper.createRichTextString("Credit Limit"));
        headerRow.createCell(33).setCellValue(createHelper.createRichTextString("Transaction Exceeding Credit Limit"));
        headerRow.createCell(34)
                .setCellValue(createHelper.createRichTextString("Exclude Advance for calculating Credit Limit?"));
        /*
         * headerRow.createCell(33).setCellValue(createHelper.
         * createRichTextString("Opening Balance"));
         * headerRow.createCell(34).setCellValue(createHelper.
         * createRichTextString("Opening Balance Of Advance"));
         */

        headerRow.createCell(35).setCellValue(createHelper.createRichTextString("GSTIN State Code2"));
        headerRow.createCell(36).setCellValue(createHelper.createRichTextString("GSTIN Code2"));
        headerRow.createCell(37).setCellValue(createHelper.createRichTextString("Billing Address2"));
        headerRow.createCell(38).setCellValue(createHelper.createRichTextString("Billing Country2"));
        headerRow.createCell(39).setCellValue(createHelper.createRichTextString("Billing State2"));
        headerRow.createCell(40).setCellValue(createHelper.createRichTextString("Billing Locaiton2"));
        headerRow.createCell(41).setCellValue(createHelper.createRichTextString("Billing Country Code2"));
        headerRow.createCell(42).setCellValue(createHelper.createRichTextString("Billing Phone No.2"));
        headerRow.createCell(43).setCellValue(createHelper.createRichTextString("Shipping Address same As Billing?"));
        headerRow.createCell(44).setCellValue(createHelper.createRichTextString("Shipping Address2"));
        headerRow.createCell(45).setCellValue(createHelper.createRichTextString("Shipping Country2"));
        headerRow.createCell(46).setCellValue(createHelper.createRichTextString("Shipping State2"));
        headerRow.createCell(47).setCellValue(createHelper.createRichTextString("Shipping Locaiton2"));
        headerRow.createCell(48).setCellValue(createHelper.createRichTextString("Shipping Country Code2"));
        headerRow.createCell(49).setCellValue(createHelper.createRichTextString("Shipping Phone No.2"));

        headerRow.createCell(50).setCellValue(createHelper.createRichTextString("GSTIN State Code3"));
        headerRow.createCell(51).setCellValue(createHelper.createRichTextString("GSTIN Code3"));
        headerRow.createCell(52).setCellValue(createHelper.createRichTextString("Billing Address3"));
        headerRow.createCell(53).setCellValue(createHelper.createRichTextString("Billing Country3"));
        headerRow.createCell(54).setCellValue(createHelper.createRichTextString("Billing State3"));
        headerRow.createCell(55).setCellValue(createHelper.createRichTextString("Billing Locaiton3"));
        headerRow.createCell(56).setCellValue(createHelper.createRichTextString("Billing Country Code3"));
        headerRow.createCell(57).setCellValue(createHelper.createRichTextString("Billing Phone No.3"));
        headerRow.createCell(58).setCellValue(createHelper.createRichTextString("Shipping Address same As Billing?"));
        headerRow.createCell(59).setCellValue(createHelper.createRichTextString("Shipping Address3"));
        headerRow.createCell(60).setCellValue(createHelper.createRichTextString("Shipping Country3"));
        headerRow.createCell(61).setCellValue(createHelper.createRichTextString("Shipping State3"));
        headerRow.createCell(62).setCellValue(createHelper.createRichTextString("Shipping Locaiton3"));
        headerRow.createCell(63).setCellValue(createHelper.createRichTextString("Shipping Country Code3"));
        headerRow.createCell(64).setCellValue(createHelper.createRichTextString("Shipping Phone No.3"));

        XSSFCellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        for (int b = 0; b < headerRow.getLastCellNum(); b++) {
            sheets.autoSizeColumn(b);
            headerRow.getCell(b).setCellStyle(style);
        }
        sheets.createFreezePane(0, 1, 0, 1);
        int rowCount = 1;
        // for (CoaTemplateAccounts coaTemplateAccounts: coaTemplateAccountsList ){
        Row mainRow = sheets.createRow((short) rowCount);
        mainRow.createCell(0).setCellValue(createHelper.createRichTextString(branchName));

        mainRow.createCell(1).setCellValue(createHelper.createRichTextString(null));
        mainRow.createCell(2).setCellValue(createHelper.createRichTextString(null));

        Name namedCell = workbook.createName();
        namedCell.setNameName("custGroupList");
        namedCell.setRefersToFormula("hiddenMapping!$E$1:$E$" + custGroupList.size() + 2);
        XSSFDataValidationConstraint custGroupConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("custGroupList");

        CellRangeAddressList customerGroupList = new CellRangeAddressList(rowCount, rowCount, 3, 3);
        XSSFDataValidation custGrpValidation = (XSSFDataValidation) dvHelper.createValidation(custGroupConstraint,
                customerGroupList);
        custGrpValidation.setShowErrorBox(true);
        sheets.addValidationData(custGrpValidation);

        mainRow.createCell(4).setCellValue(createHelper.createRichTextString("Manasi"));
        mainRow.createCell(5).setCellValue(createHelper.createRichTextString("manasi@ibm.com"));

        CellRangeAddressList addressList = new CellRangeAddressList(rowCount, rowCount, 6, 6);// cust GST registered
        XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(yesNoConstraint, addressList);
        validation.setShowErrorBox(true);
        sheets.addValidationData(validation);
        // gstin state codes
        namedCell = workbook.createName();
        namedCell.setNameName("gstinStateCodes");
        namedCell.setRefersToFormula("hiddenMapping!$B$1:$B$" + gstinStateCodes.size());
        XSSFDataValidationConstraint gstinStateCodeConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("gstinStateCodes");

        CellRangeAddressList steteCodeList = new CellRangeAddressList(rowCount, rowCount, 7, 7);
        XSSFDataValidation stateCodeValidation = (XSSFDataValidation) dvHelper
                .createValidation(gstinStateCodeConstraint, steteCodeList);
        stateCodeValidation.setShowErrorBox(true);
        sheets.addValidationData(stateCodeValidation);

        mainRow.createCell(8).setCellValue(createHelper.createRichTextString("ABCDEF1234527"));

        XSSFDataValidationConstraint GSTCustTypeConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(
                        new String[] { "Customer is a business Establishment", "Customer is a individual Consumer" });
        CellRangeAddressList custTypeList = new CellRangeAddressList(rowCount, rowCount, 9, 9);// GST customer Type
        XSSFDataValidation custTypeValidation = (XSSFDataValidation) dvHelper.createValidation(GSTCustTypeConstraint,
                custTypeList);
        custTypeValidation.setShowErrorBox(true);
        sheets.addValidationData(custTypeValidation);

        mainRow.createCell(10).setCellValue(createHelper.createRichTextString("Nariman Point"));
        // countries
        namedCell = workbook.createName();
        namedCell.setNameName("countries");
        namedCell.setRefersToFormula("hiddenMapping!$A$1:$A$" + countries.size());
        XSSFDataValidationConstraint countryConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("countries");

        CellRangeAddressList countryList = new CellRangeAddressList(rowCount, rowCount, 11, 11);
        XSSFDataValidation countryValidation = (XSSFDataValidation) dvHelper.createValidation(countryConstraint,
                countryList);
        countryValidation.setShowErrorBox(true);
        sheets.addValidationData(countryValidation);

        // state list
        namedCell = workbook.createName();
        namedCell.setNameName("states");
        namedCell.setRefersToFormula("hiddenMapping!$D$1:$D$" + states.size());
        XSSFDataValidationConstraint statesConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("states");

        CellRangeAddressList steteNamesList = new CellRangeAddressList(rowCount, rowCount, 12, 12);
        XSSFDataValidation stateNameValidation = (XSSFDataValidation) dvHelper.createValidation(statesConstraint,
                steteNamesList);
        stateNameValidation.setShowErrorBox(true);
        sheets.addValidationData(stateNameValidation);

        mainRow.createCell(13).setCellValue(createHelper.createRichTextString("Maharashtra"));

        // country code
        namedCell = workbook.createName();
        namedCell.setNameName("phoneCounrtyCodes");
        namedCell.setRefersToFormula("hiddenMapping!$C$1:$C$" + phoneCounrtyCodes.size());
        XSSFDataValidationConstraint phoneCountryConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("phoneCounrtyCodes");
        CellRangeAddressList countryCodeList = new CellRangeAddressList(rowCount, rowCount, 14, 14);
        XSSFDataValidation phcountryValidation = (XSSFDataValidation) dvHelper.createValidation(phoneCountryConstraint,
                countryCodeList);
        phcountryValidation.setShowErrorBox(true);
        sheets.addValidationData(phcountryValidation);

        mainRow.createCell(15).setCellValue(createHelper.createRichTextString("9932345678"));

        CellRangeAddressList isSameAsBillingList = new CellRangeAddressList(rowCount, rowCount, 16, 16);// shipping same
                                                                                                        // as billing
        XSSFDataValidation shippingValidation = (XSSFDataValidation) dvHelper.createValidation(yesNoConstraint,
                isSameAsBillingList);
        shippingValidation.setShowErrorBox(true);
        sheets.addValidationData(shippingValidation);

        // shipping address
        mainRow.createCell(17).setCellValue(createHelper.createRichTextString("Shivaji Nagar"));
        CellRangeAddressList shippingCountryList = new CellRangeAddressList(rowCount, rowCount, 18, 18);
        XSSFDataValidation shippingCountryValidation = (XSSFDataValidation) dvHelper.createValidation(countryConstraint,
                shippingCountryList);
        shippingCountryValidation.setShowErrorBox(true);
        sheets.addValidationData(shippingCountryValidation);

        // state list
        CellRangeAddressList shistateNamesList = new CellRangeAddressList(rowCount, rowCount, 19, 19);
        XSSFDataValidation shipstateNameValidation = (XSSFDataValidation) dvHelper.createValidation(statesConstraint,
                shistateNamesList);
        shipstateNameValidation.setShowErrorBox(true);
        sheets.addValidationData(shipstateNameValidation);

        mainRow.createCell(20).setCellValue(createHelper.createRichTextString("Pune"));
        // country code
        CellRangeAddressList shippingcountryCodeList = new CellRangeAddressList(rowCount, rowCount, 21, 21);
        XSSFDataValidation shipppingphcountryValidation = (XSSFDataValidation) dvHelper
                .createValidation(phoneCountryConstraint, shippingcountryCodeList);
        shipppingphcountryValidation.setShowErrorBox(true);
        sheets.addValidationData(shipppingphcountryValidation);

        mainRow.createCell(22).setCellValue(createHelper.createRichTextString("9932345123"));

        // sale items
        mainRow.createCell(23).setCellValue(createHelper.createRichTextString(specificsStr));

        // statutory details
        mainRow.createCell(24).setCellValue(createHelper.createRichTextString("ID1"));
        mainRow.createCell(25).setCellValue(createHelper.createRichTextString("123"));
        mainRow.createCell(26).setCellValue(createHelper.createRichTextString("ID2"));
        mainRow.createCell(27).setCellValue(createHelper.createRichTextString("543"));
        mainRow.createCell(28).setCellValue(createHelper.createRichTextString("ID3"));
        mainRow.createCell(29).setCellValue(createHelper.createRichTextString("3534"));

        XSSFDataValidationConstraint cashConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "Cash", "Credit", "Both" });
        CellRangeAddressList cashList = new CellRangeAddressList(rowCount, rowCount, 30, 30);// cash/credit
        XSSFDataValidation cashValidation = (XSSFDataValidation) dvHelper.createValidation(cashConstraint, cashList);
        cashValidation.setShowErrorBox(true);
        sheets.addValidationData(cashValidation);

        mainRow.createCell(31).setCellValue(createHelper.createRichTextString("30"));
        mainRow.createCell(32).setCellValue(createHelper.createRichTextString("10000"));

        XSSFDataValidationConstraint tranExceedingConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "Process", "Stop" });
        CellRangeAddressList tranExceedList = new CellRangeAddressList(rowCount, rowCount, 33, 33);// Tran exceeding
                                                                                                   // credit limit
        XSSFDataValidation tranExceedValidation = (XSSFDataValidation) dvHelper
                .createValidation(tranExceedingConstraint, tranExceedList);
        tranExceedValidation.setShowErrorBox(true);
        sheets.addValidationData(tranExceedValidation);

        CellRangeAddressList considerList = new CellRangeAddressList(rowCount, rowCount, 34, 34);// consider open bal
        XSSFDataValidation conValidation = (XSSFDataValidation) dvHelper.createValidation(yesNoConstraint,
                considerList);
        conValidation.setShowErrorBox(true);
        sheets.addValidationData(conValidation);

        CellRangeAddressList steteCodeList2 = new CellRangeAddressList(rowCount, rowCount, 35, 35);
        XSSFDataValidation stateCodeValidation2 = (XSSFDataValidation) dvHelper
                .createValidation(gstinStateCodeConstraint, steteCodeList2);
        stateCodeValidation2.setShowErrorBox(true);
        sheets.addValidationData(stateCodeValidation2);

        mainRow.createCell(36).setCellValue(createHelper.createRichTextString("ABCDEF1234527"));

        mainRow.createCell(37).setCellValue(createHelper.createRichTextString("Nariman Point"));
        // countries

        CellRangeAddressList countryList2 = new CellRangeAddressList(rowCount, rowCount, 38, 38);
        XSSFDataValidation countryValidation2 = (XSSFDataValidation) dvHelper.createValidation(countryConstraint,
                countryList2);
        countryValidation2.setShowErrorBox(true);
        sheets.addValidationData(countryValidation2);

        // state list

        CellRangeAddressList steteNamesList2 = new CellRangeAddressList(rowCount, rowCount, 39, 39);
        XSSFDataValidation stateNameValidation2 = (XSSFDataValidation) dvHelper.createValidation(statesConstraint,
                steteNamesList2);
        stateNameValidation2.setShowErrorBox(true);
        sheets.addValidationData(stateNameValidation2);

        mainRow.createCell(40).setCellValue(createHelper.createRichTextString("Mumbai"));
        // country code

        CellRangeAddressList countryCodeList2 = new CellRangeAddressList(rowCount, rowCount, 41, 41);
        XSSFDataValidation phcountryValidation2 = (XSSFDataValidation) dvHelper.createValidation(phoneCountryConstraint,
                countryCodeList2);
        phcountryValidation2.setShowErrorBox(true);
        sheets.addValidationData(phcountryValidation2);

        mainRow.createCell(42).setCellValue(createHelper.createRichTextString("9932345678"));

        CellRangeAddressList isSameAsBillingList2 = new CellRangeAddressList(rowCount, rowCount, 43, 43);// shipping
                                                                                                         // same as
                                                                                                         // billing
        XSSFDataValidation shippingValidation2 = (XSSFDataValidation) dvHelper.createValidation(yesNoConstraint,
                isSameAsBillingList2);
        shippingValidation2.setShowErrorBox(true);
        sheets.addValidationData(shippingValidation2);

        // shipping address
        mainRow.createCell(44).setCellValue(createHelper.createRichTextString("Shivaji Nagar"));
        CellRangeAddressList shippingCountryList2 = new CellRangeAddressList(rowCount, rowCount, 45, 45);
        XSSFDataValidation shippingCountryValidation2 = (XSSFDataValidation) dvHelper
                .createValidation(countryConstraint, shippingCountryList2);
        shippingCountryValidation2.setShowErrorBox(true);
        sheets.addValidationData(shippingCountryValidation2);

        // state list
        CellRangeAddressList shistateNamesList2 = new CellRangeAddressList(rowCount, rowCount, 46, 46);
        XSSFDataValidation shipstateNameValidation2 = (XSSFDataValidation) dvHelper.createValidation(statesConstraint,
                shistateNamesList2);
        shipstateNameValidation2.setShowErrorBox(true);
        sheets.addValidationData(shipstateNameValidation2);

        mainRow.createCell(47).setCellValue(createHelper.createRichTextString("Pune"));
        // country code
        CellRangeAddressList shippingcountryCodeList2 = new CellRangeAddressList(rowCount, rowCount, 48, 48);
        XSSFDataValidation shipppingphcountryValidation2 = (XSSFDataValidation) dvHelper
                .createValidation(phoneCountryConstraint, shippingcountryCodeList2);
        shipppingphcountryValidation2.setShowErrorBox(true);
        sheets.addValidationData(shipppingphcountryValidation2);

        mainRow.createCell(49).setCellValue(createHelper.createRichTextString("9932345123"));

        CellRangeAddressList steteCodeList3 = new CellRangeAddressList(rowCount, rowCount, 50, 50);
        XSSFDataValidation stateCodeValidation3 = (XSSFDataValidation) dvHelper
                .createValidation(gstinStateCodeConstraint, steteCodeList3);
        stateCodeValidation3.setShowErrorBox(true);
        sheets.addValidationData(stateCodeValidation3);

        mainRow.createCell(51).setCellValue(createHelper.createRichTextString("ABCDEF1234527"));

        mainRow.createCell(52).setCellValue(createHelper.createRichTextString("Nariman Point"));
        // countries

        CellRangeAddressList countryList3 = new CellRangeAddressList(rowCount, rowCount, 53, 53);
        XSSFDataValidation countryValidation3 = (XSSFDataValidation) dvHelper.createValidation(countryConstraint,
                countryList3);
        countryValidation3.setShowErrorBox(true);
        sheets.addValidationData(countryValidation3);

        // state list

        CellRangeAddressList steteNamesList3 = new CellRangeAddressList(rowCount, rowCount, 54, 54);
        XSSFDataValidation stateNameValidation3 = (XSSFDataValidation) dvHelper.createValidation(statesConstraint,
                steteNamesList3);
        stateNameValidation3.setShowErrorBox(true);
        sheets.addValidationData(stateNameValidation3);

        mainRow.createCell(55).setCellValue(createHelper.createRichTextString("Mumbai"));
        // country code

        CellRangeAddressList countryCodeList3 = new CellRangeAddressList(rowCount, rowCount, 56, 56);
        XSSFDataValidation phcountryValidation3 = (XSSFDataValidation) dvHelper.createValidation(phoneCountryConstraint,
                countryCodeList3);
        phcountryValidation3.setShowErrorBox(true);
        sheets.addValidationData(phcountryValidation3);

        mainRow.createCell(57).setCellValue(createHelper.createRichTextString("9932345678"));

        CellRangeAddressList isSameAsBillingList3 = new CellRangeAddressList(rowCount, rowCount, 58, 58);// shipping
                                                                                                         // same as
                                                                                                         // billing
        XSSFDataValidation shippingValidation3 = (XSSFDataValidation) dvHelper.createValidation(yesNoConstraint,
                isSameAsBillingList3);
        shippingValidation3.setShowErrorBox(true);
        sheets.addValidationData(shippingValidation3);

        // shipping address
        mainRow.createCell(59).setCellValue(createHelper.createRichTextString("Shivaji Nagar"));
        CellRangeAddressList shippingCountryList3 = new CellRangeAddressList(rowCount, rowCount, 60, 60);
        XSSFDataValidation shippingCountryValidation3 = (XSSFDataValidation) dvHelper
                .createValidation(countryConstraint, shippingCountryList3);
        shippingCountryValidation3.setShowErrorBox(true);
        sheets.addValidationData(shippingCountryValidation3);

        // state list
        CellRangeAddressList shistateNamesList3 = new CellRangeAddressList(rowCount, rowCount, 61, 61);
        XSSFDataValidation shipstateNameValidation3 = (XSSFDataValidation) dvHelper.createValidation(statesConstraint,
                shistateNamesList3);
        shipstateNameValidation3.setShowErrorBox(true);
        sheets.addValidationData(shipstateNameValidation3);

        mainRow.createCell(62).setCellValue(createHelper.createRichTextString("Pune"));
        // country code
        CellRangeAddressList shippingcountryCodeList3 = new CellRangeAddressList(rowCount, rowCount, 63, 63);
        XSSFDataValidation shipppingphcountryValidation3 = (XSSFDataValidation) dvHelper
                .createValidation(phoneCountryConstraint, shippingcountryCodeList3);
        shipppingphcountryValidation3.setShowErrorBox(true);
        sheets.addValidationData(shipppingphcountryValidation3);

        mainRow.createCell(64).setCellValue(createHelper.createRichTextString("9932345123"));

        // rowCount++;

        FileOutputStream fileOut = new FileOutputStream(path);
        workbook.write(fileOut);
        fileOut.close();
        log.log(Level.FINE, "===============End ");
        return sheetName;
    }

    @Override
    public String createOrgVendorCustomerExcel(Organization org, int type, String path, String sheetName, EntityManager entityManager)
            throws Exception {
        log.log(Level.FINE, "============ Start");
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("type", type);
        criterias.put("organization.id", org.getId());
        criterias.put("presentStatus", 1);
        List<Vendor> vendors = genericDAO.findByCriteria(Vendor.class, criterias, entityManager);
        Collections.reverse(vendors);
        String fileName = sheetName + ".xlsx";
        if (!vendors.isEmpty()) {

            // String
            // path1=application.path().toString()+"/target/scala-2.10/classes/public/OrgVendorCustomer/";
            File file = new File(path);
            if (!file.exists()) {
                file.mkdir();
            }
            path = path.concat(fileName);
            // path1 = path1.concat(fileName);
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet(sheetName);
            Cell cell = null;
            Row row = null;
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setFontName("Calibri");
            font.setFontHeightInPoints((short) 12);
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            // headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
            // headerStyle.setBorderLeft(CellStyle.BORDER_THIN);
            // headerStyle.setBorderRight(CellStyle.BORDER_THIN);
            // headerStyle.setBorderTop(CellStyle.BORDER_THIN);
            headerStyle.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
            headerStyle.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
            headerStyle.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
            headerStyle.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
            row = sheet.createRow(0);
            /* Set Header */
            for (byte b = 0; b < 5; b++) {
                cell = row.createCell(b);
                cell.setCellStyle(headerStyle);
                if (0 == b) {
                    cell.setCellValue("Vendor Name");
                } else if (1 == b) {
                    cell.setCellValue("Location");
                } else if (2 == b) {
                    cell.setCellValue("E-mail");
                } else if (3 == b) {
                    cell.setCellValue("Phone");
                } else if (4 == b) {
                    cell.setCellValue("Address");
                }
            }

            headerStyle = workbook.createCellStyle();
            font = workbook.createFont();
            font.setFontName("Calibri");
            font.setFontHeightInPoints((short) 11);
            font.setBold(true);
            headerStyle.setFont(font);
            for (int i = 0; i < vendors.size(); i++) {
                row = sheet.createRow(i + 1);
                for (byte b = 0; b < 5; b++) {
                    cell = row.createCell(b);
                    cell.setCellStyle(headerStyle);
                    if (0 == b) {
                        cell.setCellValue(vendors.get(i).getName());
                    } else if (1 == b) {
                        cell.setCellValue(vendors.get(i).getLocation());
                    } else if (2 == b) {
                        cell.setCellValue(vendors.get(i).getEmail());
                    } else if (3 == b) {
                        cell.setCellValue(vendors.get(i).getPhone());
                    } else if (4 == b) {
                        cell.setCellValue(vendors.get(i).getAddress());
                    }
                }
            }
            sheet.setAutoFilter(new CellRangeAddress(sheet.getFirstRowNum(), sheet.getLastRowNum(),
                    sheet.getRow(0).getFirstCellNum(), sheet.getRow(0).getLastCellNum() - 1));
            OutputStream outputStream = new FileOutputStream(path);
            workbook.write(outputStream);
            outputStream.close();
            // workbook = new XSSFWorkbook(new FileInputStream(path));
            // outputStream = new FileOutputStream(path1);
            // workbook.write(outputStream);
            outputStream.close();
        }
        return fileName;
    }

    @Override
    public String createBudgetDetails(Organization org, String path, String sheetName) throws IOException {
        log.log(Level.FINE, "============ Start");
        String fileName = null;
        if (null != org) {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = null;
            Row row = null;
            fileName = sheetName + ".xlsx";
            // String
            // path1=application.path().toString()+"/target/scala-2.10/classes/public/OrgVendorCustomer/";
            File file = new File(path);
            if (!file.exists()) {
                file.mkdir();
            }
            /*
             * file = new File(path1);
             * if (!file.exists()) {
             * file.mkdir();
             * }
             */
            List<BranchSpecifics> branchSpecifics = Collections.emptyList();
            List<Branch> branches = org.getBranches();
            if (!branches.isEmpty()) {
                for (Branch branch : branches) {
                    int rowCount = 2; // start from the 3rd line for filling the specifics
                    sheet = workbook.createSheet(branch.getName());
                    branchSpecifics = branch.getBranchSpecifics();
                    if (!branchSpecifics.isEmpty()) {
                        row = sheet.createRow(0);
                        row.createCell(0).setCellValue("Name");
                        row.createCell(1).setCellValue("Jan");
                        row.createCell(2).setCellValue("Feb");
                        row.createCell(3).setCellValue("Mar");
                        row.createCell(4).setCellValue("Apr");
                        row.createCell(5).setCellValue("May");
                        row.createCell(6).setCellValue("Jun");
                        row.createCell(7).setCellValue("Jul");
                        row.createCell(8).setCellValue("Aug");
                        row.createCell(9).setCellValue("Sep");
                        row.createCell(10).setCellValue("Oct");
                        row.createCell(11).setCellValue("Nov");
                        row.createCell(12).setCellValue("Dec");
                        row.createCell(13).setCellValue("Total");
                        row = sheet.createRow(1);
                        row.createCell(0).setCellValue(branch.getName());
                        row.createCell(1).setCellValue(branch.getBudgetAmountJan());
                        row.createCell(2).setCellValue(branch.getBudgetAmountFeb());
                        row.createCell(3).setCellValue(branch.getBudgetAmountMar());
                        row.createCell(4).setCellValue(branch.getBudgetAmountApr());
                        row.createCell(5).setCellValue(branch.getBudgetAmountMay());
                        row.createCell(6).setCellValue(branch.getBudgetAmountJune());
                        row.createCell(7).setCellValue(branch.getBudgetAmountJuly());
                        row.createCell(8).setCellValue(branch.getBudgetAmountAug());
                        row.createCell(9).setCellValue(branch.getBudgetAmountSep());
                        row.createCell(10).setCellValue(branch.getBudgetAmountOct());
                        row.createCell(11).setCellValue(branch.getBudgetAmountNov());
                        row.createCell(12).setCellValue(branch.getBudgetAmountDec());
                        row.createCell(13).setCellValue(branch.getBudgetTotal());
                        for (BranchSpecifics branchSpecific : branchSpecifics) {
                            row = sheet.createRow(rowCount);
                            if (null != branchSpecific && null != branchSpecific.getSpecifics() && branchSpecific
                                    .getSpecifics().getAccountCodeHirarchy().contains("2000000000000000000")) {
                                if (null != branchSpecific.getSpecifics().getName()) {
                                    row.createCell(0).setCellValue(branchSpecific.getSpecifics().getName());
                                } else {
                                    row.createCell(0).setCellValue("");
                                }
                                if (null != branchSpecific.getBudgetAmountJan()) {
                                    row.createCell(1).setCellValue(branchSpecific.getBudgetAmountJan());
                                } else {
                                    row.createCell(1).setCellValue("");
                                }
                                if (null != branchSpecific.getBudgetAmountFeb()) {
                                    row.createCell(2).setCellValue(branchSpecific.getBudgetAmountFeb());
                                } else {
                                    row.createCell(2).setCellValue("");
                                }
                                if (null != branchSpecific.getBudgetAmountMar()) {
                                    row.createCell(3).setCellValue(branchSpecific.getBudgetAmountMar());
                                } else {
                                    row.createCell(3).setCellValue("");
                                }
                                if (null != branchSpecific.getBudgetAmountApr()) {
                                    row.createCell(4).setCellValue(branchSpecific.getBudgetAmountApr());
                                } else {
                                    row.createCell(4).setCellValue("");
                                }
                                if (null != branchSpecific.getBudgetAmountMay()) {
                                    row.createCell(5).setCellValue(branchSpecific.getBudgetAmountMay());
                                } else {
                                    row.createCell(5).setCellValue("");
                                }
                                if (null != branchSpecific.getBudgetAmountJune()) {
                                    row.createCell(6).setCellValue(branchSpecific.getBudgetAmountJune());
                                } else {
                                    row.createCell(6).setCellValue("");
                                }
                                if (null != branchSpecific.getBudgetAmountJuly()) {
                                    row.createCell(7).setCellValue(branchSpecific.getBudgetAmountJuly());
                                } else {
                                    row.createCell(7).setCellValue("");
                                }
                                if (null != branchSpecific.getBudgetAmountAug()) {
                                    row.createCell(8).setCellValue(branchSpecific.getBudgetAmountAug());
                                } else {
                                    row.createCell(8).setCellValue("");
                                }
                                if (null != branchSpecific.getBudgetAmountSep()) {
                                    row.createCell(9).setCellValue(branchSpecific.getBudgetAmountSep());
                                } else {
                                    row.createCell(9).setCellValue("");
                                }
                                if (null != branchSpecific.getBudgetAmountOct()) {
                                    row.createCell(10).setCellValue(branchSpecific.getBudgetAmountOct());
                                } else {
                                    row.createCell(10).setCellValue("");
                                }
                                if (null != branchSpecific.getBudgetAmountNov()) {
                                    row.createCell(11).setCellValue(branchSpecific.getBudgetAmountNov());
                                } else {
                                    row.createCell(11).setCellValue("");
                                }
                                if (null != branchSpecific.getBudgetAmountDec()) {
                                    row.createCell(12).setCellValue(branchSpecific.getBudgetAmountDec());
                                } else {
                                    row.createCell(12).setCellValue("");
                                }
                                if (null != branchSpecific.getBudgetTotal()) {
                                    row.createCell(13).setCellValue(branchSpecific.getBudgetTotal());
                                } else {
                                    row.createCell(13).setCellValue("");
                                }
                                rowCount++;
                            }
                        }
                    }
                }
            }
            path = path.concat(fileName);
            // path1 = path1.concat(fileName);
            OutputStream outputStream = new FileOutputStream(path);
            workbook.write(outputStream);
            outputStream.close();
            // workbook = new XSSFWorkbook(new FileInputStream(path));
            // outputStream = new FileOutputStream(path1);
            // workbook.write(outputStream);
            // outputStream.close();
        }
        return fileName;
    }

    private static int writeMultiItemsTrans(List<TransactionItems> txnItemsList, Transaction txn,
            CreationHelper createHelper, Sheet sheets, int noOfRows, int fileType, FileWriter writer, EntityManager em,
            Integer setNo) throws IOException {
        String transactionRefNumber = "";
        String paymentStatus = "";
        String branchName = "";
        String projectName = "";
        String txnPurpose = "";
        String itemName = "";
        String customerVendor = "";
        String txnDate = "";
        String paymentMode = "";
        String paymentModeDescription = "";
        String noOfUnit = "";
        String pricePerUnit = "";
        String grossAmount = "";
        String netAmount = "";
        String netAmountDescription = "";
        String createdBy = "";
        String approvedBy = "";
        String remarks = "";
        String status = "";
        String taxName1 = "";
        String taxValue1 = "";
        String taxRate1 = "";
        String taxName2 = "";
        String taxValue2 = "";
        String taxRate2 = "";
        String taxName3 = "";
        String taxValue3 = "";
        String taxRate3 = "";
        String taxName4 = "";
        String taxValue4 = "";
        String taxRate4 = "";
        String taxName5 = "";
        String taxValue5 = "";
        String taxRate5 = "";
        String taxName6 = "";
        String taxValue6 = "";
        String taxRate6 = "";
        String taxName7 = "";
        String taxValue7 = "";
        String taxRate7 = "";
        String withholdingtax = "";
        String vendorGstin = "";
        String customerGstin = "";
        String invoiceNo = "";
        String invoiceDate = "";
        String docSNo = "";
        String poRefNo = "";
        String uOM = "";
        String discRate = "";
        String discAmt = "";
        String advAdj = "";
        String sGSTonAdv = "";
        String cGSTonAdv = "";
        String iGSTonAdv = "";
        String cessOnAdv = "";
        String modeOfReceipt = "";
        String receiptDetails = "";
        String bankName = "";
        String instNo = "";
        String instDate = "";
        String transportMode = "";
        String vehicleDetails = "";
        String doafrg = "";
        String noafrg = "";
        String gstIN = "";
        String destCountry = "";
        String destCurrency = "";
        String currConvRate = "";
        String portCode = "";
        String docNo = "";
        String docDate = "";
        String reverseChargeItem = "";
        String importCountry = "";
        String invRefDate = "";
        String invRefNo = "";
        String grnDate = "";
        String grnRefNo = "";
        String importRefDate = "";
        String importRefNo = "";
        String typeOfSupply = "";
        String dateTimeofSupply = "";
        String invoiceValue = "";
        String remarks1 = "";
        String remarks2 = "";
        String remarks3 = "";
        String placeOfSupply = "";
        String originalDocNo = "";
        String originalDocDate = "";
        String gstIn = "";
        String placeOfSupplyLocation = "";
        String lineItem = "";
        String goodsOrService = "";
        String hsnSac = "";
        String typesOfGoodsOrService = "";
        String eligibiltyOfItc = "";
        String itcIgst = "";
        String itcCgst = "";
        String itcSgst = "";
        String itcCess = "";

        TransactionInvoice invoiceDetails = TransactionInvoice.findByTransactionID(entityManager,
                txn.getTransactionBranchOrganization().getId(), txn.getId());
        Integer count = 1;

        for (TransactionItems txnItemrow : txnItemsList) {
            lineItem = count.toString();
            if (txn.getTransactionRefNumber() != null) {
                transactionRefNumber = txn.getTransactionRefNumber();
            }
            if (txn.getTransactionBranch() != null) {
                branchName = txn.getTransactionBranch().getName();
            }
            if (txn.getTransactionProject() != null) {
                projectName = txn.getTransactionProject().getName();
            }
            if (txn.getTransactionPurpose() != null) {
                txnPurpose = txn.getTransactionPurpose().getTransactionPurpose();
            }
            if (txnItemrow.getTransactionSpecifics() != null) {
                itemName = txnItemrow.getTransactionSpecifics().getName();

                if (txnItemrow.getTransactionSpecifics().getIsEligibleInputTaxCredit() != null
                        && txnItemrow.getTransactionSpecifics().getIsEligibleInputTaxCredit() == 0) {
                    eligibiltyOfItc = "no";
                }
                if (txnItemrow.getTransactionSpecifics().getGstTypeOfSupply() != null) {
                    if (txnItemrow.getTransactionSpecifics().getGstTypeOfSupply().equals("GOODS")) {
                        goodsOrService = "G"; // Goods
                        if (txnItemrow.getTransactionSpecifics().getIsEligibleInputTaxCredit() != null
                                && txnItemrow.getTransactionSpecifics().getIsEligibleInputTaxCredit() == 1) {
                            eligibiltyOfItc = "ip";
                            if (txnItemrow.getTaxValue1() != null) {
                                itcSgst = txnItemrow.getTaxValue1().toString();
                            }
                            if (txnItemrow.getTaxValue2() != null) {
                                itcCgst = txnItemrow.getTaxValue2().toString();
                            }
                            if (txnItemrow.getTaxValue3() != null) {
                                itcIgst = txnItemrow.getTaxValue3().toString();
                            }
                            if (txnItemrow.getTaxValue4() != null) {
                                itcCess = txnItemrow.getTaxValue4().toString();
                            }
                        }
                    } else if (txnItemrow.getTransactionSpecifics().getGstTypeOfSupply().equals("SERVICES")) {
                        goodsOrService = "S"; // Service
                        if (txnItemrow.getTransactionSpecifics().getIsEligibleInputTaxCredit() != null
                                && txnItemrow.getTransactionSpecifics().getIsEligibleInputTaxCredit() == 1) {
                            eligibiltyOfItc = "is";
                            if (txnItemrow.getTaxValue1() != null) {
                                itcSgst = txnItemrow.getTaxValue1().toString();
                            }
                            if (txnItemrow.getTaxValue2() != null) {
                                itcCgst = txnItemrow.getTaxValue2().toString();
                            }
                            if (txnItemrow.getTaxValue3() != null) {
                                itcIgst = txnItemrow.getTaxValue3().toString();
                            }
                            if (txnItemrow.getTaxValue4() != null) {
                                itcCess = txnItemrow.getTaxValue4().toString();
                            }
                        }
                    } else {
                        goodsOrService = "";
                    }

                }

                if (txnItemrow.getTransactionSpecifics().getGstItemCode() != null) {
                    hsnSac = txnItemrow.getTransactionSpecifics().getGstItemCode();
                }

                if (txnItemrow.getTransactionSpecifics().getGstItemCategory() != null) {
                    if (txnItemrow.getTransactionSpecifics().getGstItemCategory().equals("1")) {
                        typesOfGoodsOrService = "GST Exempt Goods/Services";
                    } else if (txnItemrow.getTransactionSpecifics().getGstItemCategory().equals("2")) {
                        typesOfGoodsOrService = "Nil Rate Goods /Services";
                    } else if (txnItemrow.getTransactionSpecifics().getGstItemCategory().equals("3")) {
                        typesOfGoodsOrService = "Non GST Goods/ Services";
                    }
                }

                if (setNo == 3) {
                    if (txnItemrow.getTransactionSpecifics() != null
                            && txnItemrow.getTransactionSpecifics().getExpenseUnitsMeasure() != null) {
                        uOM = txnItemrow.getTransactionSpecifics().getExpenseUnitsMeasure();
                    }
                } else if (setNo == 2) {
                    if (txnItemrow.getTransactionSpecifics() != null
                            && txnItemrow.getTransactionSpecifics().getIncomeUnitsMeasure() != null) {
                        uOM = txnItemrow.getTransactionSpecifics().getIncomeUnitsMeasure();
                    }
                }

            }

            if (txn.getTransactionVendorCustomer() != null) {
                customerVendor = txn.getTransactionVendorCustomer().getName();
            }
            if (txn.getTransactionUnavailableVendorCustomer() != null
                    && !txn.getTransactionUnavailableVendorCustomer().equals("")) {
                customerVendor = txn.getTransactionUnavailableVendorCustomer();
            }
            if (txn.getTransactionDate() != null) {
                txnDate = IdosConstants.IDOSDF.format(txn.getTransactionDate());
            }
            if (txn.getReceiptDetailsType() != null) {
                if (txn.getReceiptDetailsType() == 1) {
                    paymentMode = "CASH";
                }
                if (txn.getReceiptDetailsType() == 2) {
                    paymentMode = "BANK";
                }
            }
            if (txn.getReceiptDetailsDescription() != null) {
                paymentModeDescription = txn.getReceiptDetailsDescription();
            }

            if (txnItemrow.getNoOfUnits() != null) {
                noOfUnit = String.valueOf(txnItemrow.getNoOfUnits());
            }
            if (txnItemrow.getPricePerUnit() != null) {
                pricePerUnit = String.valueOf(txnItemrow.getPricePerUnit());
            }
            if (txnItemrow.getGrossAmount() != null) {
                grossAmount = String.valueOf(txnItemrow.getGrossAmount());
            }
            if (txnItemrow.getNetAmount() != null) {
                netAmount = String.valueOf(txnItemrow.getNetAmount());
            }
            if (txn.getNetAmountResultDescription() != null) {
                netAmountDescription = txn.getNetAmountResultDescription();
            }
            if (txn.getCreatedBy() != null) {
                createdBy = txn.getCreatedBy().getEmail();
            }
            if (txn.getApproverActionBy() != null) {
                approvedBy = txn.getApproverActionBy().getEmail();
            }
            if (txn.getRemarks() != null) {
                remarks = txn.getRemarks();
            }
            if (txn.getTransactionStatus() != null) {
                status = txn.getTransactionStatus();
            }
            if (txn.getPaymentStatus() != null) {
                paymentStatus = txn.getPaymentStatus();
            }

            if (txnItemrow != null) {
                if (txnItemrow.getTaxName1() != null && txnItemrow.getTaxName1().length() >= 4) {
                    taxName1 = txnItemrow.getTaxName1().substring(0, 4);
                }
                if (txnItemrow.getTaxValue1() != null) {
                    taxValue1 = txnItemrow.getTaxValue1().toString();
                }
                if (txnItemrow.getTaxRate1() != null) {
                    taxRate1 = txnItemrow.getTaxRate1().toString();
                }
                if (txnItemrow.getTaxName2() != null && txnItemrow.getTaxName2().length() >= 4) {
                    taxName2 = txnItemrow.getTaxName2().substring(0, 4);
                }
                if (txnItemrow.getTaxValue2() != null) {
                    taxValue2 = txnItemrow.getTaxValue2().toString();
                }
                if (txnItemrow.getTaxRate2() != null) {
                    taxRate2 = txnItemrow.getTaxRate2().toString();
                }
                if (txnItemrow.getTaxName3() != null && txnItemrow.getTaxName3().length() >= 4) {
                    taxName3 = txnItemrow.getTaxName3().substring(0, 4);
                }
                if (txnItemrow.getTaxValue3() != null) {
                    taxValue3 = txnItemrow.getTaxValue3().toString();
                }
                if (txnItemrow.getTaxRate3() != null) {
                    taxRate3 = txnItemrow.getTaxRate3().toString();
                }
                if (txnItemrow.getTaxName4() != null && txnItemrow.getTaxName4().length() >= 4) {
                    taxName4 = txnItemrow.getTaxName4().substring(0, 4);
                }
                if (txnItemrow.getTaxValue4() != null) {
                    taxValue4 = txnItemrow.getTaxValue4().toString();
                }
                if (txnItemrow.getTaxRate4() != null) {
                    taxRate4 = txnItemrow.getTaxRate4().toString();
                }
                if (txnItemrow.getTaxName5() != null && txnItemrow.getTaxName5().length() >= 4) {
                    taxName5 = txnItemrow.getTaxName5().substring(0, 4);
                }
                if (txnItemrow.getTaxValue5() != null) {
                    taxValue5 = txnItemrow.getTaxValue5().toString();
                }
                if (txnItemrow.getTaxRate5() != null) {
                    taxRate5 = txnItemrow.getTaxRate5().toString();
                }
                if (txnItemrow.getTaxName6() != null && txnItemrow.getTaxName6().length() >= 4) {
                    taxName6 = txnItemrow.getTaxName6().substring(0, 4);
                }
                if (txnItemrow.getTaxValue6() != null) {
                    taxValue6 = txnItemrow.getTaxValue6().toString();
                }
                if (txnItemrow.getTaxRate6() != null) {
                    taxRate6 = txnItemrow.getTaxRate6().toString();
                }
                if (txnItemrow.getTaxName7() != null && txnItemrow.getTaxName7().length() >= 4) {
                    taxName7 = txnItemrow.getTaxName7().substring(0, 4);
                }
                if (txnItemrow.getTaxValue7() != null) {
                    taxValue7 = txnItemrow.getTaxValue7().toString();
                }
                if (txnItemrow.getTaxRate7() != null) {
                    taxRate7 = txnItemrow.getTaxRate7().toString();
                }
                if (txnItemrow.getWithholdingAmount() != null) {
                    withholdingtax = txnItemrow.getWithholdingAmount().toString();
                }
            }
            if (txn.getTransactionVendorCustomer() != null) {

                if (txn.getTransactionVendorCustomer().getType() == IdosConstants.VENDOR) {
                    if (txn.getTransactionVendorCustomer().getGstin() != null) {
                        vendorGstin = txn.getTransactionVendorCustomer().getGstin();
                    } else {
                        VendorDetail vendorDetail = VendorDetail.findByVendorID(em,
                                txn.getTransactionVendorCustomer().getId());
                        if (vendorDetail != null) {
                            if (vendorDetail.getStateCode() != null) {
                                vendorGstin = vendorDetail.getStateCode();
                            }
                        }
                    }
                } else if (txn.getTransactionVendorCustomer().getType() == IdosConstants.CUSTOMER) {
                    if (txn.getTransactionVendorCustomer().getGstin() != null) {
                        customerGstin = txn.getTransactionVendorCustomer().getGstin();
                    } else {
                        CustomerDetail customerDetail = CustomerDetail.findByCustomerID(em,
                                txn.getTransactionVendorCustomer().getId());
                        if (customerDetail != null) {
                            if (customerDetail.getBillingState() != null) {
                                customerGstin = customerDetail.getBillingState();
                            }
                        }
                    }
                }
            }

            if (txn.getTransactionVendorCustomer() != null) {

                CustomerDetail customerDetail = CustomerDetail.findByCustomerBillingState(em,
                        txn.getTransactionVendorCustomer().getId(), txn.getDestinationGstin());
                if (customerDetail != null) {
                    if (customerDetail.getBillinglocation() != null) {
                        placeOfSupplyLocation = customerDetail.getBillinglocation();
                    }
                } else {
                    if (txn.getTransactionVendorCustomer().getLocation() != null) {
                        placeOfSupplyLocation = txn.getTransactionVendorCustomer().getLocation();
                    }
                }

                if (txn.getDestinationGstin() != null && txn.getDestinationGstin().length() > 2) {
                    gstIn = txn.getDestinationGstin();
                } else {
                    if (customerDetail != null) {
                        gstIn = customerDetail.getBillingStateCode().toString();
                    } else {
                        gstIn = txn.getTransactionVendorCustomer().getCountryState();
                    }
                }
            } else if (txn.getTransactionUnavailableVendorCustomer() != null
                    && (txn.getWalkinCustomerType() == 1 || txn.getWalkinCustomerType() == 2)) {
                Vendor customer = Vendor.findByOrgIdTypeName(em, txn.getTransactionBranchOrganization().getId(),
                        IdosConstants.WALK_IN_CUSTOMER, txn.getTransactionUnavailableVendorCustomer().toUpperCase());
                CustomerDetail customerDetail = null;
                if (customer != null) {
                    customerDetail = CustomerDetail.findByCustomerID(em, customer.getId());
                }
                if (customerDetail != null) {
                    if (customerDetail.getBillinglocation() != null) {
                        placeOfSupplyLocation = customerDetail.getBillinglocation();
                    }
                } else {
                    if (txn.getTransactionVendorCustomer().getLocation() != null) {
                        placeOfSupplyLocation = customer.getLocation();
                    }
                }
                if (txn.getDestinationGstin() != null && txn.getDestinationGstin().length() > 2) {
                    gstIn = txn.getDestinationGstin();
                } else {
                    if (customerDetail != null) {
                        gstIn = customerDetail.getBillingStateCode().toString();
                    } else {
                        gstIn = customer.getCountryState();
                    }
                }
            }

            if (txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                    || txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CREDIT_PAY_LATER) {
                TransactionInvoice transationInvoice = TransactionInvoice.findByTransactionID(entityManager,
                        txn.getTransactionBranchOrganization().getId(), txn.getId());
                if (transationInvoice != null) {
                    if (transationInvoice.getInvRefNumber() != null) {
                        invoiceNo = transationInvoice.getInvRefNumber();
                    }
                    if (transationInvoice.getInvRefDate() != null) {
                        invoiceDate = IdosConstants.IDOSDF.format(transationInvoice.getInvRefDate());
                    }
                }
                if (txn.getTypeOfSupply() != null) {
                    if (txn.getTypeOfSupply() == 1)
                        typeOfSupply = "Regular Supply";
                    else if (txn.getTypeOfSupply() == 2)
                        typeOfSupply = "Supply on Reverse Charge - Unregistered Vendor";
                    else if (txn.getTypeOfSupply() == 3)
                        typeOfSupply = "Supply attracting tax on reverse charge - registered vendor";
                    else if (txn.getTypeOfSupply() == 4)
                        typeOfSupply = "Overseas / SEZ Import Goods - Supply";
                    else if (txn.getTypeOfSupply() == 5)
                        typeOfSupply = "Overseas / SEZ Import Services - Supply";
                }

            } else {
                if (txn.getInvoiceNumber() != null) {
                    invoiceNo = txn.getInvoiceNumber();
                }
                if (txn.getTransactionInvoiceDate() != null) {
                    invoiceDate = IdosConstants.IDOSDF.format(txn.getTransactionInvoiceDate());
                }
                if (txn.getTypeOfSupply() != null) {
                    if (txn.getTypeOfSupply() == 1)
                        typeOfSupply = "Regular Supply";
                    else if (txn.getTypeOfSupply() == 2)
                        typeOfSupply = "Supply applicable for Reverse Charge";
                    else if (txn.getTypeOfSupply() == 3)
                        typeOfSupply = "This is an Export Supply";
                    else if (txn.getTypeOfSupply() == 4)
                        typeOfSupply = "This is supply to SEZ Unit or SEZ Developer";
                    else if (txn.getTypeOfSupply() == 5)
                        typeOfSupply = "This is deemed Export Supply";
                    else if (txn.getTypeOfSupply() == 6)
                        typeOfSupply = "Supply made through E-commerce Operator";
                    else if (txn.getTypeOfSupply() == 7)
                        typeOfSupply = "Bill of Supply";
                }

            }
            discRate = txnItemrow.getDiscountPercent() != null ? txnItemrow.getDiscountPercent() : "";
            discAmt = txnItemrow.getDiscountAmount() != null ? txnItemrow.getDiscountAmount().toString() : "";
            advAdj = txnItemrow.getAdjustmentFromAdvance() != null ? txnItemrow.getAdjustmentFromAdvance().toString()
                    : "";
            sGSTonAdv = txnItemrow.getAdvAdjTax1Value() != null ? txnItemrow.getAdvAdjTax1Value().toString() : "";
            cGSTonAdv = txnItemrow.getAdvAdjTax2Value() != null ? txnItemrow.getAdvAdjTax2Value().toString() : "";
            iGSTonAdv = txnItemrow.getAdvAdjTax3Value() != null ? txnItemrow.getAdvAdjTax3Value().toString() : "";
            cessOnAdv = txnItemrow.getAdvAdjTax4Value() != null ? txnItemrow.getAdvAdjTax4Value().toString() : "";
            modeOfReceipt = txn.getReceiptDetailsType() != null ? txn.getReceiptDetailsType().toString() : "";
            receiptDetails = txn.getReceiptDetailsDescription() != null ? txn.getReceiptDetailsDescription() : "";
            invoiceValue = txn.getInvoiceValue() != null ? txn.getInvoiceValue().toString() : "";
            if (txn.getTransactionBranchBankAccount() != null)
                bankName = txn.getTransactionBranchBankAccount().getBankName() != null
                        ? txn.getTransactionBranchBankAccount().getBankName()
                        : "";
            instNo = txn.getInstrumentNumber() != null ? txn.getInstrumentNumber() : "";
            instDate = txn.getInstrumentDate() != null ? txn.getInstrumentDate() : "";
            Specifics reverseChargeItems = null;
            if (txnItemrow.getReverseChargeItemId() != null)
                reverseChargeItems = Specifics.findById(txnItemrow.getReverseChargeItemId());
            if (reverseChargeItems != null)
                reverseChargeItem = reverseChargeItems.getName() != null ? reverseChargeItems.getName() : "";

            poRefNo = txn.getPoReference() != null ? txn.getPoReference() : "";
            docSNo = txn.getInvoiceNumber() != null ? txn.getInvoiceNumber() : "";

            if (remarks != null) {
                String[] remarksSet = remarks.split("[|]");
                int n = remarksSet.length;
                if (remarksSet[n - 1] != null)
                    remarks1 = remarksSet[n - 1];
                if (n >= 2) {
                    if (remarksSet[n - 2] != null)
                        remarks2 = remarksSet[n - 2];
                }
                if (n >= 3) {
                    if (remarksSet[n - 3] != null)
                        remarks3 = remarksSet[n - 3];
                }
            }
            if (txn.getTransactionPurpose() != null && (txn.getTransactionPurpose().getId() == 3
                    || txn.getTransactionPurpose().getId() == 4 || txn.getTransactionPurpose().getId() == 7
                    || txn.getTransactionPurpose().getId() == 8 || txn.getTransactionPurpose().getId() == 29
                    || txn.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_VENDOR
                    || txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT)) {
                if (txn.getDestinationGstin() != null && !txn.getDestinationGstin().equals("")
                        && txn.getTransactionVendorCustomer() != null) {
                    VendorDetail vendorDetails = VendorDetail.findByVendorGSTNID(em,
                            txn.getTransactionVendorCustomer().getId(), txn.getDestinationGstin());
                    if (vendorDetails != null)
                        placeOfSupply = vendorDetails.getLocation();
                }
            }
            if (txn.getTransactionPurpose() != null
                    && (txn.getTransactionPurpose().getId() == 30 || txn.getTransactionPurpose().getId() == 31)
                    || txn.getTransactionPurpose().getId() == 5 || txn.getTransactionPurpose().getId() == 35
                    || txn.getTransactionPurpose().getId() == 36) {
                List<Transaction> findByTxnReference = Transaction.findByTxnReference(em,
                        txn.getTransactionBranchOrganization().getId(), txn.getLinkedTxnRef());
                if (findByTxnReference != null && findByTxnReference.size() > 0) {
                    originalDocNo = findByTxnReference.get(0).getInvoiceNumber();
                    if (findByTxnReference.get(0).getTransactionDate() != null) {
                        originalDocDate = IdosConstants.IDOSDF.format(findByTxnReference.get(0).getTransactionDate());
                    }
                }
            }
            if (invoiceDetails != null) {
                if (docSNo.equals(""))
                    docSNo = invoiceDetails.getInvoiceNumber() != null ? invoiceDetails.getInvoiceNumber() : "";
                transportMode = invoiceDetails.getTranportationMode() != null ? invoiceDetails.getTranportationMode()
                        : "";
                vehicleDetails = invoiceDetails.getVehicleDetail() != null ? invoiceDetails.getVehicleDetail() : "";
                doafrg = invoiceDetails.getDateRemovalGoods() != null ? invoiceDetails.getDateRemovalGoods().toString()
                        : "";
                noafrg = invoiceDetails.getApplNumberGoodsRemoval() != null ? invoiceDetails.getApplNumberGoodsRemoval()
                        : "";
                gstIN = invoiceDetails.getGstinEcomOperator() != null ? invoiceDetails.getGstinEcomOperator() : "";
                destCountry = invoiceDetails.getCountryName() != null ? invoiceDetails.getCountryName() : "";
                destCurrency = invoiceDetails.getCurrencyCode() != null ? invoiceDetails.getCurrencyCode() : "";
                currConvRate = invoiceDetails.getCurrencyConvRate() != null
                        ? invoiceDetails.getCurrencyConvRate().toString()
                        : "";
                portCode = invoiceDetails.getPortCode() != null ? invoiceDetails.getPortCode() : "";
                invRefDate = invoiceDetails.getInvRefDate() != null ? invoiceDetails.getInvRefDate().toString() : "";
                invRefNo = invoiceDetails.getInvRefNumber() != null ? invoiceDetails.getInvRefNumber() : "";
                grnDate = invoiceDetails.getGrnDate() != null ? invoiceDetails.getGrnDate().toString() : "";
                grnRefNo = invoiceDetails.getGrnRefNumber() != null ? invoiceDetails.getGrnRefNumber() : "";
                importRefDate = invoiceDetails.getImportDate() != null ? invoiceDetails.getImportDate().toString() : "";
                importRefNo = invoiceDetails.getImportRefNumber() != null ? invoiceDetails.getImportRefNumber() : "";
                dateTimeofSupply = invoiceDetails.getDatetimeOfSupply() != null
                        ? invoiceDetails.getDatetimeOfSupply().toString()
                        : "";
            }

            if (fileType == 1) {
                writeTxnToCsvFile(writer, transactionRefNumber, paymentStatus, branchName, projectName, txnPurpose,
                        itemName, customerVendor, txnDate, paymentMode, paymentModeDescription, noOfUnit, pricePerUnit,
                        grossAmount, netAmount, netAmountDescription, createdBy, approvedBy, remarks, status, taxName1,
                        taxValue1, taxRate1, taxName2, taxValue2, taxRate2, taxName3, taxValue3, taxRate3, taxName4,
                        taxValue4, taxRate4, taxName5, taxValue5, taxRate5, taxName6, taxValue6, taxRate6, taxName7,
                        taxValue7, taxRate7, withholdingtax, vendorGstin, customerGstin, invoiceNo, invoiceDate);
            } else {
                noOfRows = writeTxnToExcel(noOfRows, createHelper, sheets, transactionRefNumber, paymentStatus,
                        branchName, projectName, txnPurpose, itemName, customerVendor, txnDate, paymentMode,
                        paymentModeDescription, noOfUnit, pricePerUnit, grossAmount, netAmount, netAmountDescription,
                        createdBy, approvedBy, remarks, status, taxName1, taxValue1, taxRate1, taxName2, taxValue2,
                        taxRate2, taxName3, taxValue3, taxRate3, taxName4, taxValue4, taxRate4, taxName5, taxValue5,
                        taxRate5, taxName6, taxValue6, taxRate6, taxName7, taxValue7, taxRate7, withholdingtax,
                        vendorGstin, customerGstin, invoiceNo, invoiceDate, discRate, discAmt, advAdj, sGSTonAdv,
                        cGSTonAdv, iGSTonAdv, cessOnAdv, modeOfReceipt, receiptDetails, bankName, instNo, instDate,
                        docSNo, transportMode, vehicleDetails, doafrg, noafrg, gstIN, destCountry, destCurrency,
                        currConvRate, portCode, uOM, reverseChargeItem, invRefDate, invRefNo, grnDate, grnRefNo,
                        importRefDate, importRefNo, poRefNo, typeOfSupply, dateTimeofSupply, invoiceValue, remarks1,
                        remarks2, remarks3, placeOfSupply, originalDocNo, originalDocDate, setNo, gstIn,
                        placeOfSupplyLocation, lineItem, goodsOrService, hsnSac, typesOfGoodsOrService, eligibiltyOfItc,
                        itcIgst, itcCgst, itcSgst, itcCess, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
            }

            count++;
        }
        return noOfRows;
    }

    private static int writeTransaction(List<TransactionItems> listTransactionItems, Transaction txn,
            CreationHelper createHelper, Sheet sheets, int noOfRows, int fileType, FileWriter writer, EntityManager em,
            Integer setNo) throws IOException {
        String transactionRefNumber = "";
        String paymentStatus = "";
        String branchName = "";
        String projectName = "";
        String txnPurpose = "";
        String itemName = "";
        String customerVendor = "";
        String txnDate = "";
        String paymentMode = "";
        String paymentModeDescription = "";
        String noOfUnit = "";
        String pricePerUnit = "";
        String grossAmount = "";
        String netAmount = "";
        String netAmountDescription = "";
        String createdBy = "";
        String approvedBy = "";
        String remarks = "";
        String status = "";
        String taxName1 = "";
        String taxValue1 = "";
        String taxRate1 = "";
        String taxName2 = "";
        String taxValue2 = "";
        String taxRate2 = "";
        String taxName3 = "";
        String taxValue3 = "";
        String taxRate3 = "";
        String taxName4 = "";
        String taxValue4 = "";
        String taxRate4 = "";
        String taxName5 = "";
        String taxValue5 = "";
        String taxRate5 = "";
        String taxName6 = "";
        String taxValue6 = "";
        String taxRate6 = "";
        String taxName7 = "";
        String taxValue7 = "";
        String taxRate7 = "";
        String withholdingtax = "";
        String vendorGstin = "";
        String customerGstin = "";
        String invoiceDate = "";
        String invoiceNo = "";
        String docSNo = "";
        String poRefNo = "";
        String uOM = "";
        String discRate = "";
        String discAmt = "";
        String advAdj = "";
        String sGSTonAdv = "";
        String cGSTonAdv = "";
        String iGSTonAdv = "";
        String cessOnAdv = "";
        String modeOfReceipt = "";
        String receiptDetails = "";
        String bankName = "";
        String instNo = "";
        String instDate = "";
        String transportMode = "";
        String vehicleDetails = "";
        String doafrg = "";
        String noafrg = "";
        String gstIN = "";
        String destCountry = "";
        String destCurrency = "";
        String currConvRate = "";
        String portCode = "";
        String docNo = "";
        String docDate = "";
        String purchaseOrder = "";
        String reverseChargeItem = "";
        String importCountry = "";
        String invRefDate = "";
        String invRefNo = "";
        String grnDate = "";
        String grnRefNo = "";
        String importRefDate = "";
        String importRefNo = "";
        String typeOfSupply = "";
        String dateTimeofSupply = "";
        String invoiceValue = "";
        String remarks1 = "";
        String remarks2 = "";
        String remarks3 = "";
        String placeOfSupply = "";
        String originalDocDate = "";
        String originalDocNo = "";
        String gstIn = "";
        String placeOfSupplyLocation = "";
        String goodsOrService = "";
        String hsnSac = "";
        String typesOfGoodsOrService = "";
        String eligibiltyOfItc = "";
        String itcIgst = "";
        String itcCgst = "";
        String itcSgst = "";
        String itcCess = "";
        String lineItem = "1"; // single Transaction Item
        TransactionInvoice invoiceDetails = TransactionInvoice.findByTransactionID(entityManager,
                txn.getTransactionBranchOrganization().getId(), txn.getId());
        // List<TransactionItems> listTransactionItems = txn.getTransactionItems();
        TransactionItems txnItem = null;
        if (listTransactionItems != null && listTransactionItems.size() > 0) {
            if (listTransactionItems.get(0) != null)
                txnItem = listTransactionItems.get(0);
        }
        if (txn.getTransactionRefNumber() != null) {
            transactionRefNumber = txn.getTransactionRefNumber();
        }
        if (txn.getTransactionBranch() != null) {
            branchName = txn.getTransactionBranch().getName();
        }
        if (txn.getTransactionProject() != null) {
            projectName = txn.getTransactionProject().getName();
        }
        if (txn.getTransactionPurpose() != null) {
            txnPurpose = txn.getTransactionPurpose().getTransactionPurpose();
        }
        if (txn.getTransactionSpecifics() != null) {
            itemName = txn.getTransactionSpecifics().getName();
        }
        if (txn.getTransactionVendorCustomer() != null) {
            customerVendor = txn.getTransactionVendorCustomer().getName();
        }
        if (txn.getTransactionUnavailableVendorCustomer() != null
                && !txn.getTransactionUnavailableVendorCustomer().equals("")) {
            customerVendor = txn.getTransactionUnavailableVendorCustomer();
        }
        if (txn.getTransactionDate() != null) {
            txnDate = IdosConstants.IDOSDF.format(txn.getTransactionDate());
        }
        if (txn.getReceiptDetailsType() != null) {
            if (txn.getReceiptDetailsType() == 1) {
                paymentMode = "CASH";
            }
            if (txn.getReceiptDetailsType() == 2) {
                paymentMode = "BANK";
            }
        }
        if (txn.getReceiptDetailsDescription() != null) {
            paymentModeDescription = txn.getReceiptDetailsDescription();
        }
        if (txn.getNoOfUnits() != null) {
            noOfUnit = String.valueOf(txn.getNoOfUnits());
        }
        if (txn.getPricePerUnit() != null) {
            pricePerUnit = String.valueOf(txn.getPricePerUnit());
        }
        if (txn.getGrossAmount() != null) {
            grossAmount = String.valueOf(txn.getGrossAmount());
        }
        if (txn.getNetAmount() != null) {
            netAmount = String.valueOf(txn.getNetAmount());
        }
        if (txn.getNetAmountResultDescription() != null) {
            netAmountDescription = txn.getNetAmountResultDescription();
        }
        if (txn.getCreatedBy() != null) {
            createdBy = txn.getCreatedBy().getEmail();
        }
        if (txn.getApproverActionBy() != null) {
            approvedBy = txn.getApproverActionBy().getEmail();
        }
        if (txn.getRemarks() != null) {
            remarks = txn.getRemarks();
        }
        if (txn.getTransactionStatus() != null) {
            status = txn.getTransactionStatus();
        }
        if (txn.getPaymentStatus() != null) {
            paymentStatus = txn.getPaymentStatus();
        }
        if (txn.getTaxName1() != null) {
            taxName1 = txn.getTaxName1().substring(0, 4);
        }
        if (txn.getTaxValue1() != null) {
            taxValue1 = txn.getTaxValue1().toString();
        }
        if (txn.getTaxName2() != null) {
            taxName2 = txn.getTaxName2().substring(0, 4);
            ;
        }
        if (txn.getTaxValue2() != null) {
            taxValue2 = txn.getTaxValue2().toString();
        }
        if (txn.getTaxName3() != null) {
            taxName3 = txn.getTaxName3().substring(0, 4);
            ;
        }
        if (txn.getTaxValue3() != null) {
            taxValue3 = txn.getTaxValue3().toString();
        }
        if (txn.getTaxName4() != null) {
            taxName4 = txn.getTaxName4().substring(0, 4);
            ;
        }
        if (txn.getTaxValue4() != null) {
            taxValue4 = txn.getTaxValue4().toString();
        }
        if (txn.getTaxName5() != null) {
            taxName5 = txn.getTaxName5().substring(0, 4);
            ;
        }
        if (txn.getTaxValue5() != null) {
            taxValue5 = txn.getTaxValue5().toString();
        }
        if (txn.getTaxName6() != null) {
            taxName6 = txn.getTaxName6().substring(0, 4);
            ;
        }
        if (txn.getTaxValue6() != null) {
            taxValue6 = txn.getTaxValue6().toString();
        }
        if (txn.getTaxName7() != null) {
            taxName7 = txn.getTaxName7().substring(0, 4);
        }
        if (txn.getTaxValue7() != null) {
            taxValue7 = txn.getTaxValue7().toString();
        }
        if (txn.getWithholdingTax() != null) {
            withholdingtax = txn.getWithholdingTax().toString();
        }
        if (txn.getTransactionVendorCustomer() != null) {

            if (txn.getTransactionVendorCustomer().getType() == IdosConstants.VENDOR) {
                if (txn.getTransactionVendorCustomer().getGstin() != null) {
                    vendorGstin = txn.getTransactionVendorCustomer().getGstin();
                } else {
                    VendorDetail vendorDetail = VendorDetail.findByVendorID(em,
                            txn.getTransactionVendorCustomer().getId());
                    if (vendorDetail != null) {
                        if (vendorDetail.getStateCode() != null) {
                            vendorGstin = vendorDetail.getStateCode();
                        }
                    }
                }
            } else if (txn.getTransactionVendorCustomer().getType() == IdosConstants.CUSTOMER) {
                if (txn.getTransactionVendorCustomer().getGstin() != null) {
                    customerGstin = txn.getTransactionVendorCustomer().getGstin();
                } else {
                    CustomerDetail customerDetail = CustomerDetail.findByCustomerID(em,
                            txn.getTransactionVendorCustomer().getId());
                    if (customerDetail != null) {
                        if (customerDetail.getBillingState() != null) {
                            customerGstin = customerDetail.getBillingState();
                        }
                    }
                }
            }
        }

        if (txn.getTransactionVendorCustomer() != null) {

            CustomerDetail customerDetail = CustomerDetail.findByCustomerBillingState(em,
                    txn.getTransactionVendorCustomer().getId(), txn.getDestinationGstin());
            if (customerDetail != null) {
                if (customerDetail.getBillinglocation() != null) {
                    placeOfSupplyLocation = customerDetail.getBillinglocation();
                }
            } else {
                if (txn.getTransactionVendorCustomer().getLocation() != null) {
                    placeOfSupplyLocation = txn.getTransactionVendorCustomer().getLocation();
                }
            }

            if (txn.getDestinationGstin() != null && txn.getDestinationGstin().length() > 1) {
                gstIn = txn.getDestinationGstin();
            } else {
                if (customerDetail != null) {
                    gstIn = customerDetail.getBillingStateCode().toString();
                } else {
                    gstIn = txn.getTransactionVendorCustomer().getCountryState();
                }

            }
        } else if (txn.getTransactionUnavailableVendorCustomer() != null
                && (txn.getWalkinCustomerType() == 1 || txn.getWalkinCustomerType() == 2)) {
            Vendor customer = Vendor.findByOrgIdTypeName(em, txn.getTransactionBranchOrganization().getId(),
                    IdosConstants.WALK_IN_CUSTOMER, txn.getTransactionUnavailableVendorCustomer().toUpperCase());
            CustomerDetail customerDetail = CustomerDetail.findByCustomerID(em, customer.getId());
            if (customerDetail != null) {
                if (customerDetail.getBillinglocation() != null) {
                    placeOfSupplyLocation = customerDetail.getBillinglocation();
                }
            } else {
                if (txn.getTransactionVendorCustomer().getLocation() != null) {
                    placeOfSupplyLocation = customer.getLocation();
                }
            }
            if (txn.getDestinationGstin() != null && txn.getDestinationGstin().length() > 2) {
                gstIn = txn.getDestinationGstin();
            } else {
                if (customerDetail != null) {
                    gstIn = customerDetail.getBillingStateCode().toString();
                } else {
                    gstIn = customer.getCountryState();
                }
            }
        }
        if (txnItem != null) {

            if (txnItem.getTransactionSpecifics() != null) {
                itemName = txnItem.getTransactionSpecifics().getName();

                if (txnItem.getTransactionSpecifics().getIsEligibleInputTaxCredit() != null
                        && txnItem.getTransactionSpecifics().getIsEligibleInputTaxCredit() == 0) {
                    eligibiltyOfItc = "no";
                }
                if (txnItem.getTransactionSpecifics().getGstTypeOfSupply() != null) {
                    if (txnItem.getTransactionSpecifics().getGstTypeOfSupply().equals("1")) {
                        goodsOrService = "G"; // Goods
                        if (txnItem.getTransactionSpecifics().getIsEligibleInputTaxCredit() != null
                                && txnItem.getTransactionSpecifics().getIsEligibleInputTaxCredit() == 1) {
                            eligibiltyOfItc = "ip";
                            if (txnItem.getTaxValue1() != null) {
                                itcSgst = txnItem.getTaxValue1().toString();
                            }
                            if (txnItem.getTaxValue2() != null) {
                                itcCgst = txnItem.getTaxValue2().toString();
                            }
                            if (txnItem.getTaxValue3() != null) {
                                itcIgst = txnItem.getTaxValue3().toString();
                            }
                            if (txnItem.getTaxValue4() != null) {
                                itcCess = txnItem.getTaxValue4().toString();
                            }
                        }
                    } else if (txnItem.getTransactionSpecifics().getGstTypeOfSupply().equals("2")) {
                        goodsOrService = "S"; // Service
                        if (txnItem.getTransactionSpecifics().getIsEligibleInputTaxCredit() != null
                                && txnItem.getTransactionSpecifics().getIsEligibleInputTaxCredit() == 1) {
                            eligibiltyOfItc = "is";
                            if (txnItem.getTaxValue1() != null) {
                                itcSgst = txnItem.getTaxValue1().toString();
                            }
                            if (txnItem.getTaxValue2() != null) {
                                itcCgst = txnItem.getTaxValue2().toString();
                            }
                            if (txnItem.getTaxValue3() != null) {
                                itcIgst = txnItem.getTaxValue3().toString();
                            }
                            if (txnItem.getTaxValue4() != null) {
                                itcCess = txnItem.getTaxValue4().toString();
                            }
                        }
                    }

                }

                if (txnItem.getTransactionSpecifics().getGstItemCode() != null) {
                    hsnSac = txnItem.getTransactionSpecifics().getGstItemCode();
                }

                if (txnItem.getTransactionSpecifics().getGstItemCategory() != null) {
                    if (txnItem.getTransactionSpecifics().getGstItemCategory().equals("1")) {
                        typesOfGoodsOrService = "GST Exempt Goods/Services";
                    } else if (txnItem.getTransactionSpecifics().getGstItemCategory().equals("2")) {
                        typesOfGoodsOrService = "Nil Rate Goods /Services";
                    } else if (txnItem.getTransactionSpecifics().getGstItemCategory().equals("3")) {
                        typesOfGoodsOrService = "Non GST Goods/ Services";
                    }
                }

                if (setNo == 3) {
                    if (txnItem.getTransactionSpecifics() != null
                            && txnItem.getTransactionSpecifics().getExpenseUnitsMeasure() != null) {
                        uOM = txnItem.getTransactionSpecifics().getExpenseUnitsMeasure();
                    }
                } else if (setNo == 2) {
                    if (txnItem.getTransactionSpecifics() != null
                            && txnItem.getTransactionSpecifics().getIncomeUnitsMeasure() != null) {
                        uOM = txnItem.getTransactionSpecifics().getIncomeUnitsMeasure();
                    }
                }

            }
        } else {
            if (txn.getTransactionSpecifics() != null) {
                if (txn.getTransactionSpecifics().getIsEligibleInputTaxCredit() != null
                        && txn.getTransactionSpecifics().getIsEligibleInputTaxCredit() == 0) {
                    eligibiltyOfItc = "no";
                }
                if (txn.getTransactionSpecifics().getGstTypeOfSupply() != null) {
                    if (txn.getTransactionSpecifics().getGstTypeOfSupply().equals("1")) {
                        goodsOrService = "G"; // Goods
                        if (txn.getTransactionSpecifics().getIsEligibleInputTaxCredit() != null
                                && txn.getTransactionSpecifics().getIsEligibleInputTaxCredit() == 1) {
                            eligibiltyOfItc = "ip";
                        }
                    } else if (txn.getTransactionSpecifics().getGstTypeOfSupply().equals("2")) {
                        goodsOrService = "S"; // Service
                        if (txn.getTransactionSpecifics().getIsEligibleInputTaxCredit() != null
                                && txn.getTransactionSpecifics().getIsEligibleInputTaxCredit() == 1) {
                            eligibiltyOfItc = "is";
                        }
                    }
                }

                if (txn.getTransactionSpecifics().getGstItemCode() != null) {
                    hsnSac = txn.getTransactionSpecifics().getGstItemCode();
                }

                if (txn.getTransactionSpecifics().getGstItemCategory() != null) {
                    if (txn.getTransactionSpecifics().getGstItemCategory().equals("1")) {
                        typesOfGoodsOrService = "GST Exempt Goods/Services";
                    } else if (txn.getTransactionSpecifics().getGstItemCategory().equals("2")) {
                        typesOfGoodsOrService = "Nil Rate Goods /Services";
                    } else if (txn.getTransactionSpecifics().getGstItemCategory().equals("3")) {
                        typesOfGoodsOrService = "Non GST Goods/ Services";
                    }
                }

                if (setNo == 3) {
                    if (txn.getTransactionSpecifics() != null
                            && txn.getTransactionSpecifics().getExpenseUnitsMeasure() != null) {
                        uOM = txn.getTransactionSpecifics().getExpenseUnitsMeasure();
                    }
                } else if (setNo == 2) {
                    if (txn.getTransactionSpecifics() != null
                            && txn.getTransactionSpecifics().getIncomeUnitsMeasure() != null) {
                        uOM = txn.getTransactionSpecifics().getIncomeUnitsMeasure();
                    }
                }

            }
        }

        //
        if (txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                || txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CREDIT_PAY_LATER) {
            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("organization.id", txn.getTransactionBranchOrganization().getId());
            criterias.put("transaction.id", txn.getId());
            criterias.put("presentStatus", 1);
            // criterias.put("", txn.getInvoiceNumber());
            TransactionInvoice transationInvoice = genericDAO.getByCriteria(TransactionInvoice.class, criterias, em);
            if (transationInvoice != null) {
                if (transationInvoice.getInvRefNumber() != null) {
                    invoiceNo = transationInvoice.getInvRefNumber();
                }
                if (transationInvoice.getInvRefDate() != null) {
                    invoiceDate = IdosConstants.IDOSDF.format(transationInvoice.getInvRefDate());
                }
            }
            if (txn.getTypeOfSupply() != null) {
                if (txn.getTypeOfSupply() == 1)
                    typeOfSupply = "Regular Supply";
                else if (txn.getTypeOfSupply() == 2)
                    typeOfSupply = "Supply on Reverse Charge - Unregistered Vendor";
                else if (txn.getTypeOfSupply() == 3)
                    typeOfSupply = "Supply attracting tax on reverse charge - registered vendor";
                else if (txn.getTypeOfSupply() == 4)
                    typeOfSupply = "Overseas / SEZ Import Goods - Supply";
                else if (txn.getTypeOfSupply() == 5)
                    typeOfSupply = "Overseas / SEZ Import Services - Supply";
            }
        } else {
            if (txn.getInvoiceNumber() != null) {
                invoiceNo = txn.getInvoiceNumber();
            }
            if (txn.getTransactionInvoiceDate() != null) {
                invoiceDate = IdosConstants.IDOSDF.format(txn.getTransactionInvoiceDate());
            }
            if (txn.getTypeOfSupply() != null) {
                if (txn.getTypeOfSupply() == 1)
                    typeOfSupply = "Regular Supply";
                else if (txn.getTypeOfSupply() == 2)
                    typeOfSupply = "Supply applicable for Reverse Charge";
                else if (txn.getTypeOfSupply() == 3)
                    typeOfSupply = "This is an Export Supply";
                else if (txn.getTypeOfSupply() == 4)
                    typeOfSupply = "This is supply to SEZ Unit or SEZ Developer";
                else if (txn.getTypeOfSupply() == 5)
                    typeOfSupply = "This is deemed Export Supply";
                else if (txn.getTypeOfSupply() == 6)
                    typeOfSupply = "Supply made through E-commerce Operator";
                else if (txn.getTypeOfSupply() == 7)
                    typeOfSupply = "Bill of Supply";
            }

        }
        if (txnItem != null) {
            discRate = txnItem.getDiscountPercent() != null ? txnItem.getDiscountPercent() : "";
            discAmt = txnItem.getDiscountAmount() != null ? txnItem.getDiscountAmount().toString() : "";
            advAdj = txnItem.getAdjustmentFromAdvance() != null ? txnItem.getAdjustmentFromAdvance().toString() : "";
            sGSTonAdv = txnItem.getAdvAdjTax1Value() != null ? txnItem.getAdvAdjTax1Value().toString() : "";
            cGSTonAdv = txnItem.getAdvAdjTax2Value() != null ? txnItem.getAdvAdjTax2Value().toString() : "";
            iGSTonAdv = txnItem.getAdvAdjTax3Value() != null ? txnItem.getAdvAdjTax3Value().toString() : "";
            cessOnAdv = txnItem.getAdvAdjTax4Value() != null ? txnItem.getAdvAdjTax4Value().toString() : "";
        }
        modeOfReceipt = txn.getReceiptDetailsType() != null ? txn.getReceiptDetailsType().toString() : "";
        receiptDetails = txn.getReceiptDetailsDescription() != null ? txn.getReceiptDetailsDescription() : "";
        if (txn.getTransactionBranchBankAccount() != null)
            bankName = txn.getTransactionBranchBankAccount().getBankName() != null
                    ? txn.getTransactionBranchBankAccount().getBankName()
                    : "";
        docSNo = txn.getInvoiceNumber() != null ? txn.getInvoiceNumber() : "";
        instNo = txn.getInstrumentNumber() != null ? txn.getInstrumentNumber() : "";
        instDate = txn.getInstrumentDate() != null ? txn.getInstrumentDate() : "";
        invoiceValue = txn.getInvoiceValue() != null ? txn.getInvoiceValue().toString() : "";
        if (txnItem != null) {
            Specifics reverseChargeItems = null;
            if (txnItem.getReverseChargeItemId() != null)
                reverseChargeItems = Specifics.findById(txnItem.getReverseChargeItemId());
            if (reverseChargeItems != null)
                reverseChargeItem = reverseChargeItems.getName() != null ? reverseChargeItems.getName() : "";

            Specifics itemSpecifics = Specifics.findById(txnItem.getId());
            if (setNo == 1)
                uOM = itemSpecifics.getExpenseUnitsMeasure();
            else if (setNo == 2)
                uOM = itemSpecifics.getIncomeUnitsMeasure();
        }
        poRefNo = txn.getPoReference() != null ? txn.getPoReference() : "";
        if (remarks != null) {
            String[] remarksSet = remarks.split("[|]");
            int n = remarksSet.length;
            if (remarksSet[n - 1] != null)
                remarks1 = remarksSet[n - 1];
            if (n >= 2) {
                if (remarksSet[n - 2] != null)
                    remarks2 = remarksSet[n - 2];
            }
            if (n >= 3) {
                if (remarksSet[n - 3] != null)
                    remarks3 = remarksSet[n - 3];
            }
        }
        if (txn.getTransactionPurpose() != null
                && (txn.getTransactionPurpose().getId() == 30 || txn.getTransactionPurpose().getId() == 31)
                || txn.getTransactionPurpose().getId() == 5
                || txn.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_VENDOR
                || txn.getTransactionPurpose().getId() == 35 || txn.getTransactionPurpose().getId() == 36
                || txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) {
            VendorDetail vendorDetails = VendorDetail.findByVendorGSTNID(em, txn.getTransactionVendorCustomer().getId(),
                    txn.getDestinationGstin());
            if (vendorDetails != null)
                placeOfSupply = vendorDetails.getLocation();
        }

        if (txn.getTransactionPurpose() != null
                && (txn.getTransactionPurpose().getId() == 30 || txn.getTransactionPurpose().getId() == 31)) {
            List<Transaction> findByTxnReference = Transaction.findByTxnReference(em,
                    txn.getTransactionBranchOrganization().getId(), txn.getLinkedTxnRef());
            if (findByTxnReference != null && findByTxnReference.size() > 0) {
                originalDocNo = findByTxnReference.get(0).getInvoiceNumber();
                if (findByTxnReference.get(0).getTransactionDate() != null) {
                    originalDocDate = IdosConstants.IDOSDF.format(findByTxnReference.get(0).getTransactionDate());
                }
            }
        }

        if (invoiceDetails != null) {
            if (docSNo == "")
                docSNo = invoiceDetails.getInvoiceNumber() != null ? invoiceDetails.getInvoiceNumber() : "";
            transportMode = invoiceDetails.getTranportationMode() != null ? invoiceDetails.getTranportationMode() : "";
            vehicleDetails = invoiceDetails.getVehicleDetail() != null ? invoiceDetails.getVehicleDetail() : "";
            doafrg = invoiceDetails.getDateRemovalGoods() != null ? invoiceDetails.getDateRemovalGoods().toString()
                    : "";
            noafrg = invoiceDetails.getApplNumberGoodsRemoval() != null ? invoiceDetails.getApplNumberGoodsRemoval()
                    : "";
            gstIN = invoiceDetails.getGstinEcomOperator() != null ? invoiceDetails.getGstinEcomOperator() : "";
            destCountry = invoiceDetails.getCountryName() != null ? invoiceDetails.getCountryName() : "";
            destCurrency = invoiceDetails.getCurrencyCode() != null ? invoiceDetails.getCurrencyCode() : "";
            currConvRate = invoiceDetails.getCurrencyConvRate() != null
                    ? invoiceDetails.getCurrencyConvRate().toString()
                    : "";
            portCode = invoiceDetails.getPortCode() != null ? invoiceDetails.getPortCode() : "";
            invRefDate = invoiceDetails.getInvRefDate() != null ? invoiceDetails.getInvRefDate().toString() : "";
            invRefNo = invoiceDetails.getInvRefNumber() != null ? invoiceDetails.getInvRefNumber() : "";
            ;
            grnDate = invoiceDetails.getGrnDate() != null ? invoiceDetails.getGrnDate().toString() : "";
            ;
            grnRefNo = invoiceDetails.getGrnRefNumber() != null ? invoiceDetails.getGrnRefNumber() : "";
            ;
            importRefDate = invoiceDetails.getImportDate() != null ? invoiceDetails.getImportDate().toString() : "";
            ;
            importRefNo = invoiceDetails.getImportRefNumber() != null ? invoiceDetails.getPortCode() : "";
            dateTimeofSupply = invoiceDetails.getDatetimeOfSupply() != null
                    ? invoiceDetails.getDatetimeOfSupply().toString()
                    : "";
        }
        if (fileType == 1) {
            writeTxnToCsvFile(writer, transactionRefNumber, paymentStatus, branchName, projectName, txnPurpose,
                    itemName, customerVendor, txnDate, paymentMode, paymentModeDescription, noOfUnit, pricePerUnit,
                    grossAmount, netAmount, netAmountDescription, createdBy, approvedBy, remarks, status, taxName1,
                    taxValue1, taxRate1, taxName2, taxValue2, taxRate2, taxName3, taxValue3, taxRate3, taxName4,
                    taxValue4, taxRate4, taxName5, taxValue5, taxRate5, taxName6, taxValue6, taxRate6, taxName7,
                    taxValue7, taxRate7, withholdingtax, withholdingtax, withholdingtax, withholdingtax,
                    withholdingtax);
        } else {
            noOfRows = writeTxnToExcel(noOfRows, createHelper, sheets, transactionRefNumber, paymentStatus, branchName,
                    projectName, txnPurpose, itemName, customerVendor, txnDate, paymentMode, paymentModeDescription,
                    noOfUnit, pricePerUnit, grossAmount, netAmount, netAmountDescription, createdBy, approvedBy,
                    remarks, status, taxName1, taxValue1, taxRate1, taxName2, taxValue2, taxRate2, taxName3, taxValue3,
                    taxRate3, taxName4, taxValue4, taxRate4, taxName5, taxValue5, taxRate5, taxName6, taxValue6,
                    taxRate6, taxName7, taxValue7, taxRate7, withholdingtax, vendorGstin, customerGstin, invoiceNo,
                    invoiceDate, discRate, discAmt, advAdj, sGSTonAdv, cGSTonAdv, iGSTonAdv, cessOnAdv, modeOfReceipt,
                    receiptDetails, bankName, instNo, instDate, docSNo, transportMode, vehicleDetails, doafrg, noafrg,
                    gstIN, destCountry, destCurrency, currConvRate, portCode, uOM, reverseChargeItem, invRefDate,
                    invRefNo, grnDate, grnRefNo, importRefDate, importRefNo, poRefNo, typeOfSupply, dateTimeofSupply,
                    invoiceValue, remarks1, remarks2, remarks3, placeOfSupply, originalDocNo, originalDocDate, setNo,
                    gstIn, placeOfSupplyLocation, lineItem, goodsOrService, hsnSac, typesOfGoodsOrService,
                    eligibiltyOfItc, itcIgst, itcCgst, itcSgst, itcCess, "", "", "", "", "", "", "", "", "", "", "", "",
                    "", "", "");
        }
        return noOfRows;
    }

    /**
     * @param org
     * @param txnList
     * @param srchItem
     * @param srchVendor
     * @param srchCustomer
     * @param createHelper
     * @param sheets
     * @param noOfRows
     * @param fileType     1-csv, 2 -excel
     * @return
     */

    private static int writeJournalEntries(Organization org, List<IdosProvisionJournalEntry> pjeItemList,
            String srchItem, String srchVendor, String srchCustomer, CreationHelper createHelper, Sheet sheets,
            int noOfRows, int fileType, FileWriter writer, EntityManager em) throws IOException {

        for (IdosProvisionJournalEntry pjeItem : pjeItemList) {
            String transactionRefNumber = "";
            String paymentStatus = "";
            String branchName = "";
            String txnPurpose = "";
            String customerVendor = "";
            String txnDate = "";
            String paymentMode = "";
            String paymentModeDescription = "";
            String netAmount = "";
            String netAmountDescription = "";
            String createdBy = "";
            String approvedBy = "";
            String remarks = "";
            String status = "";
            String taxName1 = "";
            String taxValue1 = "";
            String taxRate1 = "";
            String taxName2 = "";
            String taxValue2 = "";
            String taxRate2 = "";
            String taxName3 = "";
            String taxValue3 = "";
            String taxRate3 = "";
            String taxName4 = "";
            String taxValue4 = "";
            String taxRate4 = "";
            String taxName5 = "";
            String taxValue5 = "";
            String taxRate5 = "";
            String taxName6 = "";
            String taxValue6 = "";
            String taxRate6 = "";
            String taxName7 = "";
            String taxValue7 = "";
            String taxRate7 = "";
            String withholdingtax = "";
            Double totalTDS = 0.0;
            String customerGstin = "";
            String vendorGstin = "";
            String invoiceNo = "";
            String invoiceDate = "";
            String projectName = "";
            String itemName = "";
            String noOfUnit = "";
            String pricePerUnit = "";
            String grossAmount = "";
            String gstIn = "";
            String placeOfSupplyLocation = "";
            String lineItem = "";
            String goodsOrService = "";
            String hsnSac = "";
            String typesOfGoodsOrService = "";
            String eligibiltyOfItc = "";
            String itcIgst = "";
            String itcCgst = "";
            String itcSgst = "";
            String itcCess = "";
            String instrumentNo = "";
            String instrumentDate = "";
            String alertForReversal = "";
            String dateOfAlertForReversal = "";
            String systemdate = "";
            // IdosProvisionJournalEntry pjeTxn =
            // IdosProvisionJournalEntry.findById(pjeItemDetail.getProvisionJournalEntry().getId());
            ArrayList params = new ArrayList<>(2);
            params.add(pjeItem.getId());
            params.add(1);

            List<ProvisionJournalEntryDetail> pjeDebitItemsDetailsQueryList = genericDAO
                    .queryWithParamsName(PJE_DEBIT_CREDIT_ITEMS_DETAILS_QUERY, em, params);
            params.clear();
            params.add(pjeItem.getId());
            params.add(0);
            List<ProvisionJournalEntryDetail> pjeCreditItemsDetailsQueryList = genericDAO
                    .queryWithParamsName(PJE_DEBIT_CREDIT_ITEMS_DETAILS_QUERY, em, params);
            if (pjeDebitItemsDetailsQueryList.size() > 0 && pjeCreditItemsDetailsQueryList.size() > 0) {
                int loopSize = Math.max(pjeDebitItemsDetailsQueryList.size(), pjeCreditItemsDetailsQueryList.size());
                for (int i = 0; i < loopSize; i++) {
                    String debitBranchNameForPje = "";
                    String creditBranchNameForPje = "";
                    String debitProjectNameForPje = "";
                    String creditProjectNameForPje = "";
                    String debitItemNameForPje = "";
                    String creditItemNameForPje = "";
                    String debitNoOfUnitsForPje = "";
                    String creditNoOfUnitsForPje = "";
                    String debitPricePerUnitForPje = "";
                    String creditPricePerUnitForPje = "";
                    String debitHeadAmountForPje = "";
                    String creditHeadAmountForPje = "";
                    if (pjeItem.getTransactionRefNumber() != null) {
                        transactionRefNumber = pjeItem.getTransactionRefNumber();
                    }

                    if (pjeItem.getTransactionPurpose() != null) {
                        txnPurpose = pjeItem.getPurpose();
                    }

                    if (pjeItem.getTransactionDate() != null) {
                        txnDate = IdosConstants.IDOSDF.format(pjeItem.getTransactionDate());
                    }

                    /*
                     * if(pjeItem.getTotalDebitAmount()!=null){
                     * grossAmount=String.valueOf(pjeItem.getTotalDebitAmount());
                     * }
                     */
                    if (pjeItem.getTotalCreditAmount() != null) {
                        netAmount = String.valueOf(IdosConstants.decimalFormat.format(pjeItem.getTotalCreditAmount()));
                    }
                    if (pjeItem.getPurpose() != null) {
                        netAmountDescription = pjeItem.getPurpose();
                    }
                    if (pjeItem.getCreatedBy() != null) {
                        createdBy = pjeItem.getCreatedBy().getEmail();
                    }
                    if (pjeItem.getApproverActionBy() != null) {
                        approvedBy = pjeItem.getApproverActionBy().getEmail();
                    } else if (pjeItem.getApproverEmails() != null) {
                        approvedBy = pjeItem.getApproverEmails();
                    }
                    if (pjeItem.getTxnRemarks() != null) {
                        remarks = pjeItem.getTxnRemarks();
                    }
                    if (pjeItem.getTransactionStatus() != null) {
                        status = pjeItem.getTransactionStatus();
                    }
                    if (pjeItem.getInstrumentNumber() != null)
                        instrumentNo = pjeItem.getInstrumentNumber();

                    if (pjeItem.getInstrumentDate() != null)
                        instrumentDate = pjeItem.getInstrumentDate();

                    if (pjeItem.getAllowedReversal() != null)
                        if (pjeItem.getAllowedReversal() == 1)
                            alertForReversal = "Yes";
                        else
                            alertForReversal = "No";
                    if (pjeItem.getReversalDate() != null)
                        dateOfAlertForReversal = pjeItem.getReversalDate().toString();

                    systemdate = IdosConstants.IDOSDF.format(pjeItem.getCreatedAt());

                    if (pjeDebitItemsDetailsQueryList.size() > i) {
                        if (pjeDebitItemsDetailsQueryList.get(i) != null) {

                            if (pjeItem.getDebitBranch() != null)
                                debitBranchNameForPje = pjeItem.getDebitBranch().getName();

                            if (pjeDebitItemsDetailsQueryList.get(i).getProject() != null)
                                debitProjectNameForPje = pjeDebitItemsDetailsQueryList.get(i).getProject().getName();

                            if (pjeDebitItemsDetailsQueryList.get(i).getHeadID() != null) {
                                Map<String, Object> criterias = new HashMap<String, Object>();
                                if (IdosConstants.HEAD_CASH
                                        .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())) { // cash
                                    criterias.clear();
                                    criterias.put("id", pjeDebitItemsDetailsQueryList.get(i).getHeadID());
                                    criterias.put("presentStatus", 1);
                                    List<BranchDepositBoxKey> branchCashCountList = genericDAO
                                            .findByCriteria(BranchDepositBoxKey.class, criterias, em);
                                    if (branchCashCountList.size() > 0) {
                                        BranchDepositBoxKey branchCashCount = branchCashCountList.get(0);
                                        debitItemNameForPje = branchCashCount.getBranch().getName() + " Cash";
                                    }
                                } else if (IdosConstants.HEAD_BANK
                                        .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())) { // Bank
                                    criterias.clear();
                                    criterias.put("id", pjeDebitItemsDetailsQueryList.get(i).getHeadID());
                                    List<BranchBankAccounts> branchBankAccountsList = genericDAO
                                            .findByCriteria(BranchBankAccounts.class, criterias, em);
                                    if (branchBankAccountsList.size() > 0) {
                                        BranchBankAccounts branchBankAccounts = branchBankAccountsList.get(0);
                                        debitItemNameForPje = branchBankAccounts.getBranch().getName()
                                                + branchBankAccounts.getBankName();
                                    }
                                } else if (IdosConstants.HEAD_PETTY
                                        .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())) { // petty cash
                                                                                                       // currently not
                                                                                                       // getting used
                                    criterias.clear();
                                    criterias.put("id", pjeDebitItemsDetailsQueryList.get(i).getHeadID());
                                    criterias.put("presentStatus", 1);
                                    List<BranchDepositBoxKey> branchCashCountList = genericDAO
                                            .findByCriteria(BranchDepositBoxKey.class, criterias, em);
                                    if (branchCashCountList.size() > 0) {
                                        BranchDepositBoxKey branchCashCount = branchCashCountList.get(0);
                                        debitItemNameForPje = branchCashCount.getBranch().getName() + " Pettycash";
                                    }
                                } else if (IdosConstants.HEAD_VENDOR
                                        .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_CUSTOMER
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())) {
                                    criterias.clear();
                                    criterias.put("id", pjeDebitItemsDetailsQueryList.get(i).getHeadID());
                                    criterias.put("presentStatus", 1);
                                    List<Vendor> vendorList = genericDAO.findByCriteria(Vendor.class, criterias, em);
                                    if (vendorList.size() > 0) {
                                        Vendor vendor = vendorList.get(0);
                                        debitItemNameForPje = vendor.getName();
                                    }
                                } else if (IdosConstants.HEAD_VENDOR_ADV
                                        .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_CUSTOMER_ADV
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())) {
                                    criterias.clear();
                                    criterias.put("id", pjeDebitItemsDetailsQueryList.get(i).getHeadID());
                                    criterias.put("presentStatus", 1);
                                    List<Vendor> vendorList = genericDAO.findByCriteria(Vendor.class, criterias, em);
                                    if (!vendorList.isEmpty()) {
                                        Vendor vendor = vendorList.get(0);
                                        debitItemNameForPje = vendor.getName() + "_Adv";
                                    }
                                } else if (IdosConstants.HEAD_USER
                                        .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())) {
                                    criterias.clear();
                                    criterias.put("id", pjeDebitItemsDetailsQueryList.get(i).getHeadID());
                                    criterias.put("presentStatus", 1);
                                    List<Users> usersList = genericDAO.findByCriteria(Users.class, criterias, em);
                                    if (usersList.size() > 0) {
                                        Users users = usersList.get(0);
                                        debitItemNameForPje = users.getFullName();
                                    }
                                } else if (IdosConstants.HEAD_TAXS
                                        .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_SGST
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_CGST
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_IGST
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_CESS
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_RCM_CESS_IN
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_RCM_SGST_IN
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_RCM_CGST_IN
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_RCM_IGST_IN
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_RCM_CESS_OUTPUT
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_RCM_SGST_OUTPUT
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_RCM_CGST_OUTPUT
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_RCM_IGST_OUTPUT
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())) {
                                    criterias.clear();
                                    criterias.put("id", pjeDebitItemsDetailsQueryList.get(i).getHeadID());
                                    criterias.put("presentStatus", 1);
                                    List<BranchTaxes> branchTaxesList = genericDAO.findByCriteria(BranchTaxes.class,
                                            criterias, em);
                                    if (branchTaxesList.size() > 0) {
                                        BranchTaxes branchTaxes = branchTaxesList.get(0);
                                        debitItemNameForPje = branchTaxes.getTaxName();
                                    }
                                } else if (IdosConstants.HEAD_SPECIFIC
                                        .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_TDS_INPUT
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_TDS_192
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_TDS_194A
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_TDS_194C1
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_TDS_194C2
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_TDS_194H
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_TDS_194I1
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_TDS_194I2
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_TDS_194J
                                                .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())) {
                                    criterias.clear();
                                    criterias.put("id", pjeDebitItemsDetailsQueryList.get(i).getHeadID());
                                    criterias.put("presentStatus", 1);
                                    List<Specifics> specificsList = genericDAO.findByCriteria(Specifics.class,
                                            criterias, em);
                                    if (specificsList.size() > 0) {
                                        Specifics specifics = specificsList.get(0);
                                        debitItemNameForPje = specifics.getName();
                                    }
                                } else if (IdosConstants.HEAD_INTR_BRANCH
                                        .equals(pjeDebitItemsDetailsQueryList.get(i).getHeadType())) {
                                    Branch branch = Branch.findById(pjeDebitItemsDetailsQueryList.get(i).getHeadID());
                                    Branch branch2 = Branch.findById(pjeDebitItemsDetailsQueryList.get(i).getHeadID2());
                                    debitItemNameForPje = branch.getName() + "-" + branch2.getName();
                                }
                            }

                            if (pjeDebitItemsDetailsQueryList.get(i).getUnits() != null) {
                                if (pjeDebitItemsDetailsQueryList.get(i).getUnits() > 0)
                                    debitNoOfUnitsForPje = pjeDebitItemsDetailsQueryList.get(i).getUnits().toString();
                            }

                            if (pjeDebitItemsDetailsQueryList.get(i).getUnitPrice() != null)
                                if (pjeDebitItemsDetailsQueryList.get(i).getUnitPrice() > 0)
                                    debitPricePerUnitForPje = IdosConstants.decimalFormat
                                            .format(pjeDebitItemsDetailsQueryList.get(i).getUnitPrice());

                            if (pjeDebitItemsDetailsQueryList.get(i).getHeadAmount() != null)
                                if (pjeDebitItemsDetailsQueryList.get(i).getHeadAmount() > 0)
                                    debitHeadAmountForPje = IdosConstants.decimalFormat
                                            .format(pjeDebitItemsDetailsQueryList.get(i).getHeadAmount());
                        }
                    }

                    if (pjeCreditItemsDetailsQueryList.size() > i) {
                        if (pjeCreditItemsDetailsQueryList.get(i) != null) {

                            if (pjeItem.getCreditBranch() != null)
                                creditBranchNameForPje = pjeItem.getCreditBranch().getName();

                            if (pjeCreditItemsDetailsQueryList.get(i).getProject() != null)
                                creditProjectNameForPje = pjeCreditItemsDetailsQueryList.get(i).getProject().getName();

                            if (pjeCreditItemsDetailsQueryList.get(i).getHeadID() != null) {
                                Map<String, Object> criterias = new HashMap<String, Object>();
                                if (IdosConstants.HEAD_CASH
                                        .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())) { // cash
                                    criterias.clear();
                                    criterias.put("id", pjeCreditItemsDetailsQueryList.get(i).getHeadID());
                                    criterias.put("presentStatus", 1);
                                    List<BranchDepositBoxKey> branchCashCountList = genericDAO
                                            .findByCriteria(BranchDepositBoxKey.class, criterias, em);
                                    if (branchCashCountList.size() > 0) {
                                        BranchDepositBoxKey branchCashCount = branchCashCountList.get(0);
                                        creditItemNameForPje = branchCashCount.getBranch().getName() + " Cash";
                                    }
                                } else if (IdosConstants.HEAD_BANK
                                        .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())) { // Bank
                                    criterias.clear();
                                    criterias.put("id", pjeCreditItemsDetailsQueryList.get(i).getHeadID());
                                    criterias.put("presentStatus", 1);
                                    List<BranchBankAccounts> branchBankAccountsList = genericDAO
                                            .findByCriteria(BranchBankAccounts.class, criterias, em);
                                    if (branchBankAccountsList.size() > 0) {
                                        BranchBankAccounts branchBankAccounts = branchBankAccountsList.get(0);
                                        creditItemNameForPje = branchBankAccounts.getBranch().getName()
                                                + branchBankAccounts.getBankName();
                                    }
                                } else if (IdosConstants.HEAD_PETTY
                                        .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())) { // petty cash
                                                                                                        // currently not
                                                                                                        // getting used
                                    criterias.clear();
                                    criterias.put("id", pjeCreditItemsDetailsQueryList.get(i).getHeadID());
                                    criterias.put("presentStatus", 1);
                                    List<BranchDepositBoxKey> branchCashCountList = genericDAO
                                            .findByCriteria(BranchDepositBoxKey.class, criterias, em);
                                    if (branchCashCountList.size() > 0) {
                                        BranchDepositBoxKey branchCashCount = branchCashCountList.get(0);
                                        creditItemNameForPje = branchCashCount.getBranch().getName() + " Pettycash";
                                    }
                                } else if (IdosConstants.HEAD_VENDOR
                                        .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_CUSTOMER
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())) {
                                    criterias.clear();
                                    criterias.put("id", pjeCreditItemsDetailsQueryList.get(i).getHeadID());
                                    criterias.put("presentStatus", 1);
                                    List<Vendor> vendorList = genericDAO.findByCriteria(Vendor.class, criterias, em);
                                    if (vendorList.size() > 0) {
                                        Vendor vendor = vendorList.get(0);
                                        creditItemNameForPje = vendor.getName();
                                    }
                                } else if (IdosConstants.HEAD_VENDOR_ADV
                                        .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_CUSTOMER_ADV
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())) {
                                    criterias.clear();
                                    criterias.put("id", pjeCreditItemsDetailsQueryList.get(i).getHeadID());
                                    criterias.put("presentStatus", 1);
                                    List<Vendor> vendorList = genericDAO.findByCriteria(Vendor.class, criterias, em);
                                    if (!vendorList.isEmpty()) {
                                        Vendor vendor = vendorList.get(0);
                                        creditItemNameForPje = vendor.getName() + "_Adv";
                                    }
                                } else if (IdosConstants.HEAD_USER
                                        .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())) {
                                    criterias.clear();
                                    criterias.put("id", pjeCreditItemsDetailsQueryList.get(i).getHeadID());
                                    criterias.put("presentStatus", 1);
                                    List<Users> usersList = genericDAO.findByCriteria(Users.class, criterias, em);
                                    if (usersList.size() > 0) {
                                        Users users = usersList.get(0);
                                        creditItemNameForPje = users.getFullName();
                                    }
                                } else if (IdosConstants.HEAD_TAXS
                                        .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_SGST
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_CGST
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_IGST
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_CESS
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_CESS
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_RCM_CESS_IN
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_RCM_SGST_IN
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_RCM_CGST_IN
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_RCM_IGST_IN
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_RCM_CESS_OUTPUT
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_RCM_SGST_OUTPUT
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_RCM_CGST_OUTPUT
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_RCM_IGST_OUTPUT
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())) {
                                    criterias.clear();
                                    criterias.put("id", pjeCreditItemsDetailsQueryList.get(i).getHeadID());
                                    criterias.put("presentStatus", 1);
                                    List<BranchTaxes> branchTaxesList = genericDAO.findByCriteria(BranchTaxes.class,
                                            criterias, em);
                                    if (branchTaxesList.size() > 0) {
                                        BranchTaxes branchTaxes = branchTaxesList.get(0);
                                        creditItemNameForPje = branchTaxes.getTaxName();
                                    }
                                } else if (IdosConstants.HEAD_SPECIFIC
                                        .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_TDS_INPUT
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_TDS_192
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_TDS_194A
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_TDS_194C1
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_TDS_194C2
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_TDS_194H
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_TDS_194I1
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_TDS_194I2
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())
                                        || IdosConstants.HEAD_TDS_194J
                                                .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())) {
                                    criterias.clear();
                                    criterias.put("id", pjeCreditItemsDetailsQueryList.get(i).getHeadID());
                                    criterias.put("presentStatus", 1);
                                    List<Specifics> specificsList = genericDAO.findByCriteria(Specifics.class,
                                            criterias, em);
                                    if (specificsList.size() > 0) {
                                        Specifics specifics = specificsList.get(0);
                                        creditItemNameForPje = specifics.getName();
                                    }
                                } else if (IdosConstants.HEAD_INTR_BRANCH
                                        .equals(pjeCreditItemsDetailsQueryList.get(i).getHeadType())) {
                                    Branch branch = Branch.findById(pjeCreditItemsDetailsQueryList.get(i).getHeadID());
                                    Branch branch2 = Branch
                                            .findById(pjeCreditItemsDetailsQueryList.get(i).getHeadID2());
                                    creditItemNameForPje = branch.getName() + "-" + branch2.getName();
                                }
                            }

                            if (pjeCreditItemsDetailsQueryList.get(i).getUnits() != null) {
                                if (pjeCreditItemsDetailsQueryList.get(i).getUnits() > 0)
                                    creditNoOfUnitsForPje = pjeCreditItemsDetailsQueryList.get(i).getUnits().toString();
                            }

                            if (pjeCreditItemsDetailsQueryList.get(i).getUnitPrice() != null) {
                                if (pjeCreditItemsDetailsQueryList.get(i).getUnitPrice() > 0)
                                    creditPricePerUnitForPje = IdosConstants.decimalFormat
                                            .format(pjeCreditItemsDetailsQueryList.get(i).getUnitPrice());
                            }

                            if (pjeCreditItemsDetailsQueryList.get(i).getHeadAmount() != null) {
                                if (pjeCreditItemsDetailsQueryList.get(i).getHeadAmount() > 0)
                                    creditHeadAmountForPje = IdosConstants.decimalFormat
                                            .format(pjeCreditItemsDetailsQueryList.get(i).getHeadAmount());
                            }
                        }
                    }

                    withholdingtax = totalTDS.toString();
                    if (itemName.length() > 0)
                        itemName = itemName.substring(0, itemName.length() - 1);
                    if (customerVendor.length() > 0)
                        customerVendor = customerVendor.substring(0, customerVendor.length() - 1);
                    if (taxName1.length() > 0)
                        taxName1 = taxName1.substring(0, taxName1.length() - 1);
                    if (taxName2.length() > 0)
                        taxName2 = taxName2.substring(0, taxName2.length() - 1);
                    if (taxName3.length() > 0)
                        taxName3 = taxName3.substring(0, taxName3.length() - 1);
                    if (taxName4.length() > 0)
                        taxName4 = taxName4.substring(0, taxName4.length() - 1);
                    if (taxValue1.length() > 0)
                        taxValue1 = taxValue1.substring(0, taxValue1.length() - 1);
                    if (taxValue2.length() > 0)
                        taxValue2 = taxValue2.substring(0, taxValue2.length() - 1);
                    if (taxValue3.length() > 0)
                        taxValue3 = taxValue3.substring(0, taxValue3.length() - 1);
                    if (taxValue4.length() > 0)
                        taxValue4 = taxValue4.substring(0, taxValue4.length() - 1);
                    if (taxRate1.length() > 0)
                        taxRate1 = taxRate1.substring(0, taxRate1.length() - 1);
                    if (taxRate2.length() > 0)
                        taxRate2 = taxRate2.substring(0, taxRate2.length() - 1);
                    if (taxRate3.length() > 0)
                        taxRate3 = taxRate3.substring(0, taxRate3.length() - 1);
                    if (taxRate4.length() > 0)
                        taxRate4 = taxRate4.substring(0, taxRate4.length() - 1);

                    if (fileType == 1) {
                        writeTxnToCsvFile(writer, transactionRefNumber, paymentStatus, branchName, projectName,
                                txnPurpose, itemName, customerVendor, txnDate, paymentMode, paymentModeDescription,
                                noOfUnit, pricePerUnit, grossAmount, netAmount, netAmountDescription, createdBy,
                                approvedBy, remarks, status, taxName1, taxValue1, taxRate1, taxName2, taxValue2,
                                taxRate2, taxName3, taxValue3, taxRate3, taxName4, taxValue4, taxRate4, taxName5,
                                taxValue5, taxRate5, taxName6, taxValue6, taxRate6, taxName7, taxValue7, taxRate7,
                                withholdingtax, vendorGstin, customerGstin, invoiceNo, invoiceDate);
                    } else {
                        noOfRows = writeTxnToExcel(noOfRows, createHelper, sheets, transactionRefNumber, paymentStatus,
                                branchName, projectName, txnPurpose, itemName, customerVendor, txnDate, paymentMode,
                                paymentModeDescription, noOfUnit, pricePerUnit, grossAmount, netAmount,
                                netAmountDescription, createdBy, approvedBy, remarks, status, taxName1, taxValue1,
                                taxRate1, taxName2, taxValue2, taxRate3, taxName3, taxValue3, taxRate3, taxName4,
                                taxValue4, taxRate4, taxName5, taxValue5, taxRate5, taxName6, taxValue6, taxRate6,
                                taxName7, taxValue7, taxRate7, withholdingtax, vendorGstin, customerGstin, invoiceNo,
                                invoiceDate, "", "", "", "", "", "", "", "", "", "", instrumentNo, instrumentDate, "",
                                "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                                "", "", "", "", "", 4, gstIn, placeOfSupplyLocation, lineItem, goodsOrService, hsnSac,
                                typesOfGoodsOrService, eligibiltyOfItc, itcIgst, itcCgst, itcSgst, itcCess,
                                alertForReversal, dateOfAlertForReversal, systemdate, debitBranchNameForPje,
                                creditBranchNameForPje, debitProjectNameForPje, creditProjectNameForPje,
                                debitItemNameForPje, creditItemNameForPje, debitNoOfUnitsForPje, creditNoOfUnitsForPje,
                                debitPricePerUnitForPje, creditPricePerUnitForPje, debitHeadAmountForPje,
                                creditHeadAmountForPje);
                    }
                }
            }
        }
        return noOfRows;
    }

    private static int writeTxnToExcel(int noOfRows, CreationHelper createHelper, Sheet sheets,
            String transactionRefNumber, String paymentStatus, String branchName, String projectName, String txnPurpose,
            String itemName, String customerVendor, String txnDate, String paymentMode, String paymentModeDescription,
            String noOfUnit, String pricePerUnit, String grossAmount, String netAmount, String netAmountDescription,
            String createdBy, String approvedBy, String remarks, String status, String taxName1, String taxValue1,
            String taxRate1, String taxName2, String taxValue2, String taxRate2, String taxName3, String taxValue3,
            String taxRate3, String taxName4, String taxValue4, String taxRate4, String taxName5, String taxValue5,
            String taxRate5, String taxName6, String taxValue6, String taxRate6, String taxName7, String taxValue7,
            String taxRate7, String withholdingtax, String vendorGstin, String customerGstin, String invoiceNo,
            String invoiceDate, String discRate, String discAmt, String advAdj, String sGSTonAdv, String cGSTonAdv,
            String iGSTonAdv, String cessOnAdv, String modeOfReceipt, String receiptDetails, String bankName,
            String instNo, String instDate, String docSNo, String transportMode, String vehicleDetails, String doafrg,
            String noafrg, String gstIN, String destCountry, String destCurrency, String currConvRate, String portCode,
            String uOM, String reverseChargeItem, String invRefDate, String invRefNo, String grnDate, String grnRefNo,
            String importRefDate, String importRefNo, String poRefNo, String typeOfSupply, String dateTimeofSupply,
            String invoiceValue, String remarks1, String remarks2, String remarks3, String placeOfSupply,
            String originalDocNo, String originalDocDate, Integer setNo, String gstIn, String placeOfSupplyLocation,
            String lineItem, String goodsOrService, String hsnSac, String typesOfGoodsOrService, String eligibiltyOfItc,
            String itcIgst, String itcCgst, String itcSgst, String itcCess, String alertForReversal,
            String dateOfAlertForReversal, String systemdate, String debitBranchNameForPje,
            String creditBranchNameForPje, String debitProjectNameForPje, String creditProjectNameForPje,
            String debitItemNameForPje, String creditItemNameForPje, String debitNoOfUnitsForPje,
            String creditNoOfUnitsForPje, String debitPricePerUnitForPje, String creditPricePerUnitForPje,
            String debitHeadAmountForPje, String creditHeadAmountForPje) {
        Row datarows = sheets.createRow((short) noOfRows);
        final Integer MAX_COLUMN = 70;
        Cell dataCell[] = new Cell[MAX_COLUMN];
        String[] data = new String[MAX_COLUMN];
        for (int i = 0; i < 70; i++) {
            dataCell[i] = datarows.createCell(i);
        }
        noOfRows++;
        if (setNo == 1) {// other transactions
            Cell datacells1 = datarows.createCell(0);
            datacells1.setCellValue(createHelper.createRichTextString(transactionRefNumber));
            Cell datacells2 = datarows.createCell(1);
            datacells2.setCellValue(createHelper.createRichTextString(branchName));
            Cell datacells3 = datarows.createCell(2);
            datacells3.setCellValue(createHelper.createRichTextString(projectName));
            Cell datacells4 = datarows.createCell(3);
            datacells4.setCellValue(createHelper.createRichTextString(txnPurpose));
            Cell datacells5 = datarows.createCell(4);
            datacells5.setCellValue(createHelper.createRichTextString(itemName));
            Cell datacells6 = datarows.createCell(5);
            datacells6.setCellValue(createHelper.createRichTextString(customerVendor));
            Cell datacells7 = datarows.createCell(6);
            datacells7.setCellValue(createHelper.createRichTextString(txnDate));
            Cell datacells8 = datarows.createCell(7);
            datacells8.setCellValue(createHelper.createRichTextString(paymentMode));
            Cell datacells9 = datarows.createCell(8);
            datacells9.setCellValue(createHelper.createRichTextString(paymentModeDescription));
            Cell datacells10 = datarows.createCell(9);
            datacells10.setCellValue(createHelper.createRichTextString(noOfUnit));
            Cell datacells11 = datarows.createCell(10);
            datacells11.setCellValue(createHelper.createRichTextString(pricePerUnit));
            Cell datacells12 = datarows.createCell(11);
            datacells12.setCellValue(createHelper.createRichTextString(grossAmount));
            Cell datacells13 = datarows.createCell(12);
            datacells13.setCellValue(createHelper.createRichTextString(netAmount));
            Cell datacells14 = datarows.createCell(13);
            datacells14.setCellValue(createHelper.createRichTextString(netAmountDescription));
            Cell datacells15 = datarows.createCell(14);
            datacells15.setCellValue(createHelper.createRichTextString(createdBy));
            Cell datacells16 = datarows.createCell(15);
            datacells16.setCellValue(createHelper.createRichTextString(approvedBy));
            Cell datacells17 = datarows.createCell(16);
            datacells17.setCellValue(createHelper.createRichTextString(remarks));
            Cell datacells18 = datarows.createCell(17);
            datacells18.setCellValue(createHelper.createRichTextString(status));
            Cell datacells19 = datarows.createCell(18);
            datacells19.setCellValue(createHelper.createRichTextString(paymentStatus));
            Cell datacells20 = datarows.createCell(19);
            datacells20.setCellValue(createHelper.createRichTextString(taxName1));
            Cell datacells21 = datarows.createCell(20);
            datacells21.setCellValue(createHelper.createRichTextString(taxValue1));
            Cell datacells22 = datarows.createCell(21);
            datacells22.setCellValue(createHelper.createRichTextString(taxRate1));
            Cell datacells23 = datarows.createCell(22);
            datacells23.setCellValue(createHelper.createRichTextString(taxName2));
            Cell datacells24 = datarows.createCell(23);
            datacells24.setCellValue(createHelper.createRichTextString(taxValue2));
            Cell datacells25 = datarows.createCell(24);
            datacells25.setCellValue(createHelper.createRichTextString(taxRate2));
            Cell datacells26 = datarows.createCell(25);
            datacells26.setCellValue(createHelper.createRichTextString(taxName3));
            Cell datacells27 = datarows.createCell(26);
            datacells27.setCellValue(createHelper.createRichTextString(taxValue3));
            Cell datacells28 = datarows.createCell(27);
            datacells28.setCellValue(createHelper.createRichTextString(taxRate3));
            Cell datacells29 = datarows.createCell(28);
            datacells29.setCellValue(createHelper.createRichTextString(taxName4));
            Cell datacells30 = datarows.createCell(29);
            datacells30.setCellValue(createHelper.createRichTextString(taxValue4));
            Cell datacells31 = datarows.createCell(30);
            datacells31.setCellValue(createHelper.createRichTextString(taxRate4));
            Cell datacells32 = datarows.createCell(31);
            datacells32.setCellValue(createHelper.createRichTextString(taxName5));
            Cell datacells33 = datarows.createCell(32);
            datacells33.setCellValue(createHelper.createRichTextString(taxValue5));
            Cell datacells34 = datarows.createCell(33);
            datacells34.setCellValue(createHelper.createRichTextString(taxRate5));
            Cell datacells35 = datarows.createCell(34);
            datacells35.setCellValue(createHelper.createRichTextString(taxName6));
            Cell datacells36 = datarows.createCell(35);
            datacells36.setCellValue(createHelper.createRichTextString(taxValue6));
            Cell datacells37 = datarows.createCell(36);
            datacells37.setCellValue(createHelper.createRichTextString(taxRate6));
            Cell datacells38 = datarows.createCell(37);
            datacells38.setCellValue(createHelper.createRichTextString(taxName7));
            Cell datacells39 = datarows.createCell(38);
            datacells39.setCellValue(createHelper.createRichTextString(taxValue7));
            Cell datacells40 = datarows.createCell(39);
            datacells40.setCellValue(createHelper.createRichTextString(taxRate7));
            Cell datacells41 = datarows.createCell(40);
            datacells41.setCellValue(createHelper.createRichTextString(withholdingtax));
            Cell datacells42 = datarows.createCell(41);
            datacells42.setCellValue(createHelper.createRichTextString(customerGstin));
            Cell datacells43 = datarows.createCell(42);
            datacells43.setCellValue(createHelper.createRichTextString(vendorGstin));
            Cell datacells44 = datarows.createCell(43);
            datacells44.setCellValue(createHelper.createRichTextString(invoiceNo));
            Cell datacells45 = datarows.createCell(44);
            datacells45.setCellValue(createHelper.createRichTextString(invoiceDate));
        } else if (setNo == 2) { // sell side
            data[0] = transactionRefNumber;
            data[1] = txnDate;
            data[2] = createdBy;
            data[3] = approvedBy;
            data[4] = txnPurpose;
            data[5] = branchName;
            data[6] = projectName;
            data[7] = customerVendor;
            data[8] = customerGstin;
            data[9] = placeOfSupplyLocation;
            data[10] = docSNo;
            data[11] = typeOfSupply;
            data[12] = poRefNo;
            data[13] = lineItem;
            data[14] = goodsOrService;
            data[15] = itemName;
            data[16] = hsnSac;
            data[17] = typesOfGoodsOrService;
            data[18] = uOM;
            data[19] = noOfUnit;
            data[20] = pricePerUnit;
            data[21] = discRate;
            data[22] = discAmt;
            data[23] = grossAmount;
            data[24] = withholdingtax;
            data[25] = taxRate1;
            data[26] = taxValue1;
            data[27] = taxRate2;
            data[28] = taxValue2;
            data[29] = taxRate3;
            data[30] = taxValue3;
            data[31] = taxRate4;
            data[32] = taxValue4;
            data[33] = invoiceValue;
            data[34] = advAdj;
            data[35] = sGSTonAdv;
            data[36] = cGSTonAdv;
            data[37] = iGSTonAdv;
            data[38] = cessOnAdv;
            data[39] = netAmount;
            data[40] = paymentMode;
            data[41] = bankName;
            data[42] = instNo;
            data[43] = instDate;
            data[44] = paymentModeDescription;
            data[45] = dateTimeofSupply;
            data[46] = transportMode;
            data[47] = vehicleDetails;
            data[48] = doafrg;
            data[49] = noafrg;
            data[50] = gstIN;
            data[51] = destCountry;
            data[52] = destCurrency;
            data[53] = currConvRate;
            data[54] = portCode;
            data[55] = invoiceDate;
            data[56] = originalDocNo;
            data[57] = originalDocDate;
            data[58] = remarks1;
            data[59] = remarks2;
            data[60] = remarks3;
            data[61] = status;
            data[62] = "";
            data[63] = "";
            data[64] = "";
            data[65] = "";
            data[66] = "";
            data[67] = "";
            data[68] = "";
            data[69] = "";

            for (int i = 0; i < MAX_COLUMN; i++) {
                dataCell[i] = datarows.createCell(i);
                dataCell[i].setCellValue(createHelper.createRichTextString(data[i]));
            }
        } else if (setNo == 3) { // buy side

            data = new String[MAX_COLUMN];
            data[0] = transactionRefNumber;
            data[1] = txnDate;
            data[2] = createdBy;
            data[3] = approvedBy;
            data[4] = txnPurpose;
            data[5] = branchName;
            data[6] = projectName;
            data[7] = customerVendor;
            data[8] = vendorGstin;
            data[9] = placeOfSupply;
            data[10] = typeOfSupply;
            data[11] = poRefNo;
            data[12] = lineItem;
            data[13] = goodsOrService;
            data[14] = itemName;
            data[15] = hsnSac;
            data[16] = uOM;
            data[17] = reverseChargeItem;
            data[18] = noOfUnit;
            data[19] = pricePerUnit;
            data[20] = grossAmount;
            data[21] = taxRate1;
            data[22] = taxValue1;
            data[23] = taxRate2;
            data[24] = taxValue2;
            data[25] = taxRate3;
            data[26] = taxValue3;
            data[27] = taxRate4;
            data[28] = taxValue4;
            data[29] = eligibiltyOfItc;
            data[30] = itcIgst;
            data[31] = itcCgst;
            data[32] = itcSgst;
            data[33] = itcCess;
            data[34] = withholdingtax;
            data[35] = advAdj;
            data[36] = netAmount;
            data[37] = paymentMode;
            data[38] = bankName;
            data[39] = instNo;
            data[40] = instDate;
            data[41] = paymentModeDescription;
            data[42] = destCountry;
            data[43] = destCurrency;
            data[44] = currConvRate;
            data[45] = invRefDate;
            data[46] = invRefNo;
            data[47] = grnDate;
            data[48] = grnRefNo;
            data[49] = importRefDate;
            data[50] = importRefNo;
            data[51] = portCode;
            data[52] = remarks1;
            data[53] = remarks2;
            data[54] = remarks3;
            data[55] = status;
            data[56] = "";
            data[57] = "";
            data[58] = "";
            data[59] = "";
            data[60] = "";
            data[61] = "";
            data[62] = "";
            data[63] = "";
            data[64] = "";
            data[65] = "";
            data[66] = "";
            data[67] = "";
            data[68] = "";
            data[69] = "";

            for (int i = 0; i < MAX_COLUMN; i++) {
                dataCell[i] = datarows.createCell(i);
                dataCell[i].setCellValue(createHelper.createRichTextString(data[i]));
            }
        } else if (setNo == 4) { // Provision/Journal Entry txns
            data = new String[MAX_COLUMN];
            data[0] = transactionRefNumber;
            data[1] = txnDate;
            data[2] = createdBy;
            data[3] = approvedBy;
            data[4] = debitBranchNameForPje;
            data[5] = debitProjectNameForPje;
            data[6] = debitItemNameForPje;
            data[7] = debitNoOfUnitsForPje;
            data[8] = debitPricePerUnitForPje;
            data[9] = debitHeadAmountForPje;
            data[10] = creditBranchNameForPje;
            data[11] = creditProjectNameForPje;
            data[12] = creditItemNameForPje;
            data[13] = creditNoOfUnitsForPje;
            data[14] = creditPricePerUnitForPje;
            data[15] = creditHeadAmountForPje;
            data[16] = netAmount;
            data[17] = instNo;
            data[18] = instDate;
            data[19] = txnPurpose;
            data[20] = alertForReversal;
            data[21] = dateOfAlertForReversal;
            data[22] = remarks;
            data[23] = systemdate;
            data[24] = status;
            data[25] = "";
            data[26] = "";
            data[27] = "";
            data[28] = "";
            data[29] = "";
            data[30] = "";
            data[31] = "";
            data[32] = "";
            data[33] = "";
            data[34] = "";
            data[35] = "";
            data[36] = "";
            data[37] = "";
            data[38] = "";
            data[39] = "";
            data[40] = "";
            data[41] = "";
            data[42] = "";
            data[43] = "";
            data[44] = "";
            data[45] = "";
            data[46] = "";
            data[47] = "";
            data[48] = "";
            data[49] = "";
            data[50] = "";
            data[51] = "";
            data[52] = "";
            data[53] = "";
            data[54] = "";
            data[55] = "";
            data[56] = "";
            data[57] = "";
            data[58] = "";
            data[59] = "";
            data[60] = "";
            data[61] = "";
            data[62] = "";
            data[63] = "";
            data[64] = "";
            data[65] = "";
            data[66] = "";
            data[67] = "";
            data[68] = "";
            data[69] = "";

            for (int i = 0; i < MAX_COLUMN; i++) {
                dataCell[i] = datarows.createCell(i);
                dataCell[i].setCellValue(createHelper.createRichTextString(data[i]));
            }
        }
        return noOfRows;

    }

    private static void writeTxnToCsvFile(FileWriter writer, String transactionRefNumber, String paymentStatus,
            String branchName, String projectName, String txnPurpose, String itemName, String customerVendor,
            String txnDate, String paymentMode, String paymentModeDescription, String noOfUnit, String pricePerUnit,
            String grossAmount, String netAmount, String netAmountDescription, String createdBy, String approvedBy,
            String remarks, String status, String taxName1, String taxValue1, String taxRate1, String taxName2,
            String taxValue2, String taxRate2, String taxName3, String taxValue3, String taxRate3, String taxName4,
            String taxValue4, String taxRate4, String taxName5, String taxValue5, String taxRate5, String taxName6,
            String taxValue6, String taxRate6, String taxName7, String taxValue7, String taxRate7,
            String withholdingtax, String vendorGstin, String customerGstin, String invoiceNo, String invoiceDate)
            throws IOException {
        writer.append(transactionRefNumber).append('|');
        writer.append(branchName).append('|');
        writer.append(projectName).append('|');
        writer.append(txnPurpose).append('|');
        writer.append(itemName).append('|');
        writer.append(customerVendor).append('|');
        writer.append(txnDate).append('|');
        writer.append(paymentMode).append('|');
        writer.append(paymentModeDescription).append('|');
        writer.append(noOfUnit).append('|');
        writer.append(pricePerUnit).append('|');
        writer.append(grossAmount).append('|');
        writer.append(netAmount).append('|');
        writer.append(netAmountDescription).append('|');
        writer.append(createdBy).append('|');
        writer.append(approvedBy).append('|');
        writer.append(remarks).append('|');
        writer.append(status).append('|');
        writer.append(paymentStatus).append('|');
        writer.append(taxName1).append('|');
        writer.append(taxValue1).append('|');
        writer.append(taxRate1).append('|');
        writer.append(taxName2).append('|');
        writer.append(taxValue2).append('|');
        writer.append(taxRate2).append('|');
        writer.append(taxName3).append('|');
        writer.append(taxValue3).append('|');
        writer.append(taxRate3).append('|');
        writer.append(taxName4).append('|');
        writer.append(taxValue4).append('|');
        writer.append(taxRate4).append('|');
        writer.append(taxName5).append('|');
        writer.append(taxValue5).append('|');
        writer.append(taxRate5).append('|');
        writer.append(taxName6).append('|');
        writer.append(taxValue6).append('|');
        writer.append(taxRate6).append('|');
        writer.append(taxName7).append('|');
        writer.append(taxValue7).append('|');
        writer.append(taxRate7).append('|');
        writer.append(withholdingtax).append('|');
        writer.append(customerGstin).append('|');
        writer.append(vendorGstin).append('|');
        writer.append(invoiceNo).append('|');
        writer.append(invoiceDate).append('|');
        writer.append(System.lineSeparator());
    }

    /**
     * Added by Firdous on 28-11-2017
     * Method to create xlsx template file for adding vendor details
     */
    public String createOrgVendorTemplateExcel(Users user, EntityManager em, String path, String sheetName)
            throws Exception {

        log.log(Level.FINE, "inside createOrgVendorExcel()");

        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("presentStatus", 1);
        List<Branch> branchList = genericDAO.findByCriteria(Branch.class, criterias, em);
        String branchName = "";
        for (Branch branch : branchList) {
            branchName += branch.getName() + ",";
        }
        branchName = branchName.substring(0, branchName.length() - 1);
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("groupType", 1);
        criterias.put("presentStatus", 1);
        List<VendorGroup> vendorGroupList = genericDAO.findByCriteria(VendorGroup.class, criterias, em);
        ChartOfAccountsService coaService = new ChartOfAccountsServiceImpl();
        List<Specifics> orgSpecificsList = coaService.getExpensesCoaChildNodes(em, user);
        String specificsStr = "";
        for (Specifics spec : orgSpecificsList) {
            specificsStr = specificsStr + spec.getName() + ",";
        }

        // creating workbook and xlsx sheet
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheets = workbook.createSheet(sheetName);
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheets);

        List<String> countries = CountryCurrencyUtil.getCountriesList();
        List<String> listOfCountryCodes = CountryTelephoneCodeUtil.getPhoneCountryCodes();
        List<String> gstinStateCodes = IdosUtil.getStateCodes();
        List<String> listOfStates = IdosUtil.getStateNames();

        // hidden mapping start
        XSSFSheet hiddenMappingSheet = workbook.createSheet("hiddenMapping");

        int totalHiddenRows = 0; // set it to max list size
        if (vendorGroupList.size() > totalHiddenRows) {
            totalHiddenRows = vendorGroupList.size();
        }
        if (gstinStateCodes.size() > totalHiddenRows) {
            totalHiddenRows = gstinStateCodes.size();
        }
        if (countries.size() > totalHiddenRows) {
            totalHiddenRows = countries.size();
        }
        if (listOfStates.size() > totalHiddenRows) {
            totalHiddenRows = listOfStates.size();
        }
        if (listOfCountryCodes.size() > totalHiddenRows) {
            totalHiddenRows = listOfCountryCodes.size();
        }

        for (int i = 0; i < totalHiddenRows; i++) {
            XSSFRow row = hiddenMappingSheet.createRow(i);

            if (i < vendorGroupList.size()) {
                XSSFCell cell = row.createCell(0);
                cell.setCellValue(vendorGroupList.get(i).getGroupName());
            }

            if (i < listOfStates.size()) {
                XSSFCell cell = row.createCell(1);
                cell.setCellValue(gstinStateCodes.get(i));
            }
            if (i < countries.size()) {
                XSSFCell cell = row.createCell(2);
                cell.setCellValue(countries.get(i));
            }

            if (i < listOfStates.size()) {
                XSSFCell cell = row.createCell(3);
                cell.setCellValue(listOfStates.get(i));
            }

            if (i < listOfCountryCodes.size()) {
                XSSFCell cell = row.createCell(4);
                cell.setCellValue(listOfCountryCodes.get(i));
            }

        }

        XSSFDataValidationConstraint yesNoConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "Yes", "No" });
        Row headerRow = sheets.createRow(0);
        headerRow.createCell(0).setCellValue(createHelper.createRichTextString("Branches 							"));
        headerRow.createCell(1)
                .setCellValue(createHelper.createRichTextString("Branches Opening Balances						"));
        headerRow.createCell(2).setCellValue(
                createHelper.createRichTextString("Branches Opening Balances Advance Paid 							"));
        headerRow.createCell(3).setCellValue(createHelper.createRichTextString("Vendor Group"));
        headerRow.createCell(4).setCellValue(createHelper.createRichTextString("Vendor Name"));
        headerRow.createCell(5).setCellValue(createHelper.createRichTextString("Vendor Email"));
        headerRow.createCell(6).setCellValue(createHelper.createRichTextString("Is Vendor GST Registered?"));
        headerRow.createCell(7).setCellValue(createHelper.createRichTextString("GSTIN State Code"));
        headerRow.createCell(8).setCellValue(createHelper.createRichTextString("GSTIN Code"));
        headerRow.createCell(9).setCellValue(createHelper.createRichTextString("Vendor Type"));
        headerRow.createCell(10).setCellValue(createHelper.createRichTextString("Address"));
        headerRow.createCell(11).setCellValue(createHelper.createRichTextString("Country"));
        headerRow.createCell(12).setCellValue(createHelper.createRichTextString("State"));
        headerRow.createCell(13).setCellValue(createHelper.createRichTextString("Locaiton"));
        headerRow.createCell(14).setCellValue(createHelper.createRichTextString("Country Code"));
        headerRow.createCell(15).setCellValue(createHelper.createRichTextString("Phone No."));
        headerRow.createCell(16).setCellValue(createHelper.createRichTextString("Items Of Purchase"));
        headerRow.createCell(17).setCellValue(createHelper.createRichTextString("Statutory Id1"));
        headerRow.createCell(18).setCellValue(createHelper.createRichTextString("ID Number1"));
        headerRow.createCell(19).setCellValue(createHelper.createRichTextString("Statutory Id2"));
        headerRow.createCell(20).setCellValue(createHelper.createRichTextString("ID Number2"));
        headerRow.createCell(21).setCellValue(createHelper.createRichTextString("Statutory Id3"));
        headerRow.createCell(22).setCellValue(createHelper.createRichTextString("ID Number3"));
        headerRow.createCell(23).setCellValue(createHelper.createRichTextString("Statutory Id4"));
        headerRow.createCell(24).setCellValue(createHelper.createRichTextString("ID Number4"));
        headerRow.createCell(25).setCellValue(createHelper.createRichTextString("Cash/Credit?"));
        headerRow.createCell(26).setCellValue(createHelper.createRichTextString("Vendor Days Of Credit"));
        /*
         * headerRow.createCell(27).setCellValue(createHelper.
         * createRichTextString("Opening Balance"));
         * headerRow.createCell(28).setCellValue(createHelper.
         * createRichTextString("Opening Balance Of Advance"));
         */
        headerRow.createCell(27).setCellValue(createHelper.createRichTextString("Validity From"));
        headerRow.createCell(28).setCellValue(createHelper.createRichTextString("Validity To"));
        headerRow.createCell(29).setCellValue(createHelper.createRichTextString("GSTIN State Code2"));
        headerRow.createCell(30).setCellValue(createHelper.createRichTextString("GSTIN Code2"));
        headerRow.createCell(31).setCellValue(createHelper.createRichTextString("Address2"));
        headerRow.createCell(32).setCellValue(createHelper.createRichTextString("Country2"));
        headerRow.createCell(33).setCellValue(createHelper.createRichTextString("State2"));
        headerRow.createCell(34).setCellValue(createHelper.createRichTextString("Locaiton2"));
        headerRow.createCell(35).setCellValue(createHelper.createRichTextString("Country Code2"));
        headerRow.createCell(36).setCellValue(createHelper.createRichTextString("Phone No. 2"));
        headerRow.createCell(37).setCellValue(createHelper.createRichTextString("GSTIN State Code 3"));
        headerRow.createCell(38).setCellValue(createHelper.createRichTextString("GSTIN Code 3"));
        headerRow.createCell(39).setCellValue(createHelper.createRichTextString("Address 3"));
        headerRow.createCell(40).setCellValue(createHelper.createRichTextString("Country 3"));
        headerRow.createCell(41).setCellValue(createHelper.createRichTextString("State 3"));
        headerRow.createCell(42).setCellValue(createHelper.createRichTextString("Locaiton 3"));
        headerRow.createCell(43).setCellValue(createHelper.createRichTextString("Country Code 3"));
        headerRow.createCell(44).setCellValue(createHelper.createRichTextString("Phone No. 3"));

        workbook.setSheetHidden(1, true);
        XSSFCellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        for (int b = 0; b < headerRow.getLastCellNum(); b++) {
            sheets.autoSizeColumn(b);
            headerRow.getCell(b).setCellStyle(style);
            sheets.lockAutoFilter(true);
        }
        sheets.createFreezePane(0, 1, 0, 1);
        int rowCount = 1;

        Row mainRow = sheets.createRow((short) rowCount);
        mainRow.createCell(0).setCellValue(createHelper.createRichTextString(branchName));
        mainRow.createCell(1).setCellValue(createHelper.createRichTextString(null));
        mainRow.createCell(2).setCellValue(createHelper.createRichTextString(null));
        Name namedCell = workbook.createName();
        namedCell.setNameName("vendor_group");
        namedCell.setRefersToFormula("hiddenMapping!$A$1:$A$" + vendorGroupList.size() + 5);
        XSSFDataValidationConstraint vendorConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("vendor_group");
        CellRangeAddressList vendorgroupList = new CellRangeAddressList(rowCount, rowCount, 3, 3);
        XSSFDataValidation vendorGroupValidation = (XSSFDataValidation) dvHelper.createValidation(vendorConstraint,
                vendorgroupList);
        vendorGroupValidation.setShowErrorBox(true);
        sheets.addValidationData(vendorGroupValidation);

        mainRow.createCell(4).setCellValue(createHelper.createRichTextString("vendor-11"));

        mainRow.createCell(5).setCellValue(createHelper.createRichTextString("vendor11@myidos.com"));

        CellRangeAddressList addressList = new CellRangeAddressList(rowCount, rowCount, 6, 6);// cust GST registered
        XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(yesNoConstraint, addressList);
        validation.setEmptyCellAllowed(false);
        validation.setShowErrorBox(true);
        sheets.addValidationData(validation);

        namedCell = workbook.createName();
        namedCell.setNameName("stateCodes");
        namedCell.setRefersToFormula("hiddenMapping!$B$1:$B$" + gstinStateCodes.size());
        XSSFDataValidationConstraint stateCodesConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("stateCodes");
        CellRangeAddressList stateCodesList = new CellRangeAddressList(rowCount, rowCount, 7, 7);
        XSSFDataValidation stateCodesValidation = (XSSFDataValidation) dvHelper.createValidation(stateCodesConstraint,
                stateCodesList);
        stateCodesValidation.setShowErrorBox(true);
        stateCodesValidation.setEmptyCellAllowed(false);
        sheets.addValidationData(stateCodesValidation);

        /*
         * XSSFDataValidationConstraint restrictConstraint=
         * (XSSFDataValidationConstraint)
         * dvHelper.createCustomConstraint("$E$2=\"Yes\"");
         * 
         * CellRangeAddressList addressList1=new
         * CellRangeAddressList(rowCount,rowCount,5,6);
         * XSSFDataValidation restrictValidation =
         * (XSSFDataValidation)dvHelper.createValidation(restrictConstraint,addressList1
         * );
         * restrictValidation.createErrorBox("Not allowed"
         * ,"Please select Yes to eneter gst details");
         * restrictValidation.setShowErrorBox(true);
         * sheets.addValidationData(restrictValidation);
         */

        mainRow.createCell(8).setCellValue(createHelper.createRichTextString("123"));

        XSSFDataValidationConstraint GSTVendorTypeConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(
                        new String[] { "Vendor is a business Establishment", "Vendor is a individual Consumer" });
        CellRangeAddressList vendorTypeList = new CellRangeAddressList(rowCount, rowCount, 9, 9);// GST customer Type
        XSSFDataValidation vendorTypeValidation = (XSSFDataValidation) dvHelper
                .createValidation(GSTVendorTypeConstraint, vendorTypeList);
        vendorTypeValidation.setShowErrorBox(true);
        vendorTypeValidation.setEmptyCellAllowed(false);
        sheets.addValidationData(vendorTypeValidation);
        mainRow.createCell(10).setCellValue(createHelper.createRichTextString("Nariman Point"));

        // countries
        namedCell = workbook.createName();
        namedCell.setNameName("countries");
        namedCell.setRefersToFormula("hiddenMapping!$C$1:$C$" + countries.size());
        XSSFDataValidationConstraint countryConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("countries");
        CellRangeAddressList countryList = new CellRangeAddressList(rowCount, rowCount, 11, 11);
        XSSFDataValidation countryValidation = (XSSFDataValidation) dvHelper.createValidation(countryConstraint,
                countryList);
        countryValidation.setShowErrorBox(true);
        countryValidation.setEmptyCellAllowed(false);
        sheets.addValidationData(countryValidation);

        // states
        namedCell = workbook.createName();
        namedCell.setNameName("states");
        namedCell.setRefersToFormula("hiddenMapping!$D$1:$D$" + listOfStates.size());
        XSSFDataValidationConstraint stateConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("states");
        CellRangeAddressList stateList = new CellRangeAddressList(rowCount, rowCount, 12, 12);
        XSSFDataValidation stateValidation = (XSSFDataValidation) dvHelper.createValidation(stateConstraint, stateList);
        stateValidation.setShowErrorBox(true);
        stateValidation.setEmptyCellAllowed(false);
        sheets.addValidationData(stateValidation);

        mainRow.createCell(13).setCellValue(createHelper.createRichTextString("Mumbai"));

        namedCell = workbook.createName();
        namedCell.setNameName("telephoneCodes");
        namedCell.setRefersToFormula("hiddenMapping!$E$1:$E$" + listOfCountryCodes.size());
        XSSFDataValidationConstraint telephoneCodeConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("telephoneCodes");
        CellRangeAddressList telephoneCodesList = new CellRangeAddressList(rowCount, rowCount, 14, 14);
        XSSFDataValidation codeValidation = (XSSFDataValidation) dvHelper.createValidation(telephoneCodeConstraint,
                telephoneCodesList);
        codeValidation.setShowErrorBox(true);
        sheets.addValidationData(codeValidation);

        mainRow.createCell(15).setCellValue(createHelper.createRichTextString("9932345678"));

        mainRow.createCell(16).setCellValue(createHelper.createRichTextString(specificsStr));

        mainRow.createCell(17).setCellValue(createHelper.createRichTextString("ID1"));
        mainRow.createCell(18).setCellValue(createHelper.createRichTextString("123"));
        mainRow.createCell(19).setCellValue(createHelper.createRichTextString("ID2"));
        mainRow.createCell(20).setCellValue(createHelper.createRichTextString("543"));
        mainRow.createCell(21).setCellValue(createHelper.createRichTextString("ID3"));
        mainRow.createCell(22).setCellValue(createHelper.createRichTextString("3534"));
        mainRow.createCell(23).setCellValue(createHelper.createRichTextString("ID4"));
        mainRow.createCell(24).setCellValue(createHelper.createRichTextString("5476"));

        XSSFDataValidationConstraint cashConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "Cash", "Credit", "Both" });
        CellRangeAddressList cashList = new CellRangeAddressList(rowCount, rowCount, 25, 25);// cash/credit
        XSSFDataValidation cashValidation = (XSSFDataValidation) dvHelper.createValidation(cashConstraint, cashList);
        cashValidation.setShowErrorBox(true);
        sheets.addValidationData(cashValidation);

        /*
         * XSSFDataValidationConstraint restrictConstraint2=
         * (XSSFDataValidationConstraint)
         * dvHelper.createCustomConstraint("$X$2=\"Credit\"");
         * CellRangeAddressList addressList2=new
         * CellRangeAddressList(rowCount,rowCount,24,24);
         * XSSFDataValidation restrictValidation2 =
         * (XSSFDataValidation)dvHelper.createValidation(restrictConstraint2,
         * addressList2);
         * restrictValidation2.createErrorBox("Not allowed"
         * ,"THIS OPTION IS FOR CREDIT TYPE");
         * restrictValidation2.setShowErrorBox(true);
         * restrictValidation2.setEmptyCellAllowed(false);
         * sheets.addValidationData(restrictValidation2);
         */
        mainRow.createCell(26).setCellValue(createHelper.createRichTextString("5476"));

        /*
         * mainRow.createCell(27).setCellValue(createHelper.createRichTextString("3000")
         * );
         * 
         * mainRow.createCell(28).setCellValue(createHelper.createRichTextString("11000"
         * ));
         */

        String excelFormatPattern = DateFormatConverter.convert(Locale.ENGLISH, DateUtil.mysqldf);
        CellStyle dateStyle = workbook.createCellStyle();
        DataFormat poiFormat = workbook.createDataFormat();
        dateStyle.setDataFormat(poiFormat.getFormat(excelFormatPattern));
        mainRow.createCell(27).setCellStyle(dateStyle);
        mainRow.createCell(28).setCellStyle(dateStyle);

        /*
         * mainRow.createCell(27);
         * mainRow.createCell(28);
         */

        CellRangeAddressList stateCodesList2 = new CellRangeAddressList(rowCount, rowCount, 29, 29);
        XSSFDataValidation stateCodesValidation2 = (XSSFDataValidation) dvHelper.createValidation(stateCodesConstraint,
                stateCodesList2);
        stateCodesValidation2.setShowErrorBox(true);
        sheets.addValidationData(stateCodesValidation2);

        mainRow.createCell(30).setCellValue(createHelper.createRichTextString("ABCDEF1234527"));

        mainRow.createCell(31).setCellValue(createHelper.createRichTextString("Nariman Point"));

        CellRangeAddressList countryList2 = new CellRangeAddressList(rowCount, rowCount, 32, 32);
        XSSFDataValidation countryValidation2 = (XSSFDataValidation) dvHelper.createValidation(countryConstraint,
                countryList2);
        countryValidation2.setShowErrorBox(true);
        sheets.addValidationData(countryValidation2);

        CellRangeAddressList stateList2 = new CellRangeAddressList(rowCount, rowCount, 33, 33);
        XSSFDataValidation stateValidation2 = (XSSFDataValidation) dvHelper.createValidation(stateConstraint,
                stateList2);
        stateValidation2.setShowErrorBox(true);
        sheets.addValidationData(stateValidation2);

        mainRow.createCell(34).setCellValue(createHelper.createRichTextString("Mumbai"));

        CellRangeAddressList telephoneCodesList2 = new CellRangeAddressList(rowCount, rowCount, 35, 35);
        XSSFDataValidation codeValidation2 = (XSSFDataValidation) dvHelper.createValidation(telephoneCodeConstraint,
                telephoneCodesList2);
        codeValidation2.setShowErrorBox(true);
        sheets.addValidationData(codeValidation2);

        mainRow.createCell(36).setCellValue(createHelper.createRichTextString("9932345678"));

        CellRangeAddressList stateCodesList3 = new CellRangeAddressList(rowCount, rowCount, 37, 37);
        XSSFDataValidation stateCodesValidation3 = (XSSFDataValidation) dvHelper.createValidation(stateCodesConstraint,
                stateCodesList3);
        stateCodesValidation3.setShowErrorBox(true);
        stateCodesValidation3.setEmptyCellAllowed(false);
        sheets.addValidationData(stateCodesValidation3);

        mainRow.createCell(38).setCellValue(createHelper.createRichTextString("ABCDEF1234527"));

        mainRow.createCell(39).setCellValue(createHelper.createRichTextString("Nariman Point"));

        // countries
        CellRangeAddressList countryList3 = new CellRangeAddressList(rowCount, rowCount, 40, 40);
        XSSFDataValidation countryValidation3 = (XSSFDataValidation) dvHelper.createValidation(countryConstraint,
                countryList3);
        countryValidation3.setShowErrorBox(true);
        sheets.addValidationData(countryValidation3);

        // state list
        CellRangeAddressList stateList3 = new CellRangeAddressList(rowCount, rowCount, 41, 41);
        XSSFDataValidation stateValidation3 = (XSSFDataValidation) dvHelper.createValidation(stateConstraint,
                stateList3);
        stateValidation3.setShowErrorBox(true);
        stateValidation3.setEmptyCellAllowed(false);
        sheets.addValidationData(stateValidation3);

        mainRow.createCell(42).setCellValue(createHelper.createRichTextString("Mumbai"));

        CellRangeAddressList telephoneCodesList3 = new CellRangeAddressList(rowCount, rowCount, 43, 43);
        XSSFDataValidation codeValidation3 = (XSSFDataValidation) dvHelper.createValidation(telephoneCodeConstraint,
                telephoneCodesList3);
        codeValidation3.setShowErrorBox(true);
        sheets.addValidationData(codeValidation3);

        mainRow.createCell(44).setCellValue(createHelper.createRichTextString("9932345678"));
        /*
         * sheets.setAutoFilter(new CellRangeAddress(sheets.getFirstRowNum(),
         * sheets.getLastRowNum(), sheets.getRow(0).getFirstCellNum(),
         * sheets.getRow(0).getLastCellNum() - 1));
         */

        for (int b = 0; b < mainRow.getLastCellNum(); b++) {
            sheets.autoSizeColumn(b);
        }
        FileOutputStream fileOut = new FileOutputStream(path);
        workbook.write(fileOut);
        fileOut.close();
        log.log(Level.FINE, "===============End ");
        return sheetName;
    }

    /**
     * Created by Firdous on 18-12-2017
     * Method to Create excel sheet template for branch
     *
     * @throws IOException
     */

    @Override
    public String createOrgBranchTemplateExcel(Users user, EntityManager em, String path, String sheetName)
            throws IOException {
        log.log(Level.INFO, ">>>>> inside create barnch excel template");
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("organization.id", user.getOrganization().getId());
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheets = workbook.createSheet(sheetName);
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheets);

        List<String> countries = CountryCurrencyUtil.getCountriesList();
        List<String> listOfCountryCodes = CountryTelephoneCodeUtil.getPhoneCountryCodes();
        List<String> gstinStateCodes = IdosUtil.getStateCodes();
        List<String> listOfStates = IdosUtil.getStateNames();

        Map<String, String> countryCurrencies1 = CountryCurrencyUtil.getAvailableCurrencies();
        /* Set<String> countryCurrencies1=countryCurrencie */
        /*
         * for(int i=0;i<countryCurrencies.size();i++){
         * countryCurrencies.keySet();
         * }
         */
        Set<Entry<String, String>> set = countryCurrencies1.entrySet();

        List<Entry<String, String>> countryCurrencies = new ArrayList<Entry<String, String>>(set);
        // List<String> countryCurrencies=new
        // ArrayList<String>(Map.Entry(countryCurrencies1.entrySet()));
        List<String> bankAccountType = new ArrayList<String>();
        BankAccountEnumType[] bankActTypes = BankAccountEnumType.class.getEnumConstants();
        for (BankAccountEnumType bankActType : bankActTypes) {
            bankAccountType.add(bankActType.getName());
        }
        /*
         * List<String> currencies=new ArrayList<String>();
         * for(int i=0;i<countryCurrencies.size();i++){
         * currencies.set(i,countries.get(i)+"==>"+countryCurrencies.get(i));
         * }
         */

        // hidden mapping start
        XSSFSheet hiddenMappingSheet = workbook.createSheet("hiddenMapping");

        int totalHiddenRows = 0; // set it to max list size

        if (gstinStateCodes.size() > totalHiddenRows) {
            totalHiddenRows = gstinStateCodes.size();
        }
        if (countries.size() > totalHiddenRows) {
            totalHiddenRows = countries.size();
        }
        if (listOfStates.size() > totalHiddenRows) {
            totalHiddenRows = listOfStates.size();
        }
        if (listOfCountryCodes.size() > totalHiddenRows) {
            totalHiddenRows = listOfCountryCodes.size();
        }
        if (countryCurrencies.size() > totalHiddenRows) {
            totalHiddenRows = countryCurrencies.size();
        }
        if (bankAccountType.size() > totalHiddenRows) {
            totalHiddenRows = bankAccountType.size();
        }

        for (int i = 0; i < totalHiddenRows; i++) {
            XSSFRow row = hiddenMappingSheet.createRow(i);

            if (i < gstinStateCodes.size()) {
                XSSFCell cell = row.createCell(0);
                cell.setCellValue(gstinStateCodes.get(i));
            }
            if (i < countries.size()) {
                XSSFCell cell = row.createCell(1);
                cell.setCellValue(countries.get(i));
            }

            if (i < countryCurrencies.size()) {
                XSSFCell cell = row.createCell(2);
                cell.setCellValue(countryCurrencies.get(i).getKey() + " => " + countryCurrencies.get(i).getValue());
            }

            if (i < listOfStates.size()) {
                XSSFCell cell = row.createCell(3);
                cell.setCellValue(listOfStates.get(i));
            }

            if (i < listOfCountryCodes.size()) {
                XSSFCell cell = row.createCell(4);
                cell.setCellValue(listOfCountryCodes.get(i));
            }

            if (i < bankAccountType.size()) {
                XSSFCell cell = row.createCell(5);
                cell.setCellValue(bankAccountType.get(i));
            }

        }

        Row headerRow1 = sheets.createRow(0);
        sheets.addMergedRegion(CellRangeAddress.valueOf("A1:O2"));

        sheets.addMergedRegion(CellRangeAddress.valueOf("P1:AB2"));

        sheets.addMergedRegion(CellRangeAddress.valueOf("AC1:AK2"));

        sheets.addMergedRegion(CellRangeAddress.valueOf("AL1:AS2"));

        sheets.addMergedRegion(CellRangeAddress.valueOf("AT1:AZ2"));

        sheets.addMergedRegion(CellRangeAddress.valueOf("BA1:BM2"));

        sheets.addMergedRegion(CellRangeAddress.valueOf("BN1:BV2"));

        sheets.addMergedRegion(CellRangeAddress.valueOf("BW1:CI2"));

        Cell cell = headerRow1.createCell(0);
        cell.setCellValue(createHelper.createRichTextString("Branch Setup"));

        Cell cell1 = headerRow1.createCell(15);
        cell1.setCellValue(createHelper.createRichTextString("Branch Premise Details"));

        Cell cell2 = headerRow1.createCell(28);
        cell2.setCellValue(createHelper.createRichTextString("Branch Officers And Contact Details"));

        Cell cell3 = headerRow1.createCell(37);
        cell3.setCellValue(createHelper.createRichTextString("Branch Statutory Details"));

        Cell cell4 = headerRow1.createCell(45);
        cell4.setCellValue(createHelper.createRichTextString("Operational Reminders"));

        Cell cell5 = headerRow1.createCell(52);
        cell5.setCellValue(createHelper.createRichTextString("Branch Cash Account"));

        Cell cell6 = headerRow1.createCell(65);
        cell6.setCellValue(createHelper.createRichTextString("Branch Insurance"));

        Cell cell7 = headerRow1.createCell(74);
        cell7.setCellValue(createHelper.createRichTextString("Bank Account Details"));

        XSSFCellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);

        style.setFont(font);
        XSSFCellStyle style2 = ExcelUtil.getCellStyleHeader(workbook);
        headerRow1.getCell(0).setCellStyle(style);
        headerRow1.getCell(15).setCellStyle(style);
        headerRow1.getCell(28).setCellStyle(style);
        headerRow1.getCell(37).setCellStyle(style);
        headerRow1.getCell(45).setCellStyle(style);
        headerRow1.getCell(52).setCellStyle(style);
        headerRow1.getCell(65).setCellStyle(style);
        headerRow1.getCell(74).setCellStyle(style);

        XSSFDataValidationConstraint yesNoConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "Yes", "No" });
        Row headerRow = sheets.createRow(2);

        // Branch Details
        headerRow.createCell(0).setCellValue(createHelper.createRichTextString("Organization"));
        headerRow.createCell(1).setCellValue(createHelper.createRichTextString("Branch Name"));
        headerRow.createCell(2).setCellValue(createHelper.createRichTextString("Opened Date"));
        headerRow.createCell(3).setCellValue(createHelper.createRichTextString("Gst state code"));
        headerRow.createCell(4).setCellValue(createHelper.createRichTextString("Gstin number"));
        headerRow.createCell(5).setCellValue(createHelper.createRichTextString("Gst state code"));
        headerRow.createCell(6).setCellValue(createHelper.createRichTextString("Gstin number"));
        headerRow.createCell(7).setCellValue(createHelper.createRichTextString("Branch Address"));
        headerRow.createCell(8).setCellValue(createHelper.createRichTextString("Country"));
        headerRow.createCell(9).setCellValue(createHelper.createRichTextString("Currency"));
        headerRow.createCell(10).setCellValue(createHelper.createRichTextString("State"));
        headerRow.createCell(11).setCellValue(createHelper.createRichTextString("City"));
        headerRow.createCell(12).setCellValue(createHelper.createRichTextString("Country Code"));
        headerRow.createCell(13).setCellValue(createHelper.createRichTextString("Phone No."));
        headerRow.createCell(14).setCellValue(createHelper.createRichTextString("Premise"));

        // Branch Premise Details
        headerRow.createCell(15).setCellValue(createHelper.createRichTextString("Validity From"));
        headerRow.createCell(16).setCellValue(createHelper.createRichTextString("Validity To"));
        headerRow.createCell(17).setCellValue(createHelper.createRichTextString("Periodicity Of Payment"));
        headerRow.createCell(18).setCellValue(createHelper.createRichTextString("Rent Payable"));
        headerRow.createCell(19).setCellValue(createHelper.createRichTextString("LandLord Name"));
        headerRow.createCell(20).setCellValue(createHelper.createRichTextString("LandLord Address"));
        headerRow.createCell(21).setCellValue(createHelper.createRichTextString("LandLord bank name"));
        headerRow.createCell(22).setCellValue(createHelper.createRichTextString("LandLord Bank Account Number"));
        headerRow.createCell(23).setCellValue(createHelper.createRichTextString("LandLord Bank Address"));
        headerRow.createCell(24).setCellValue(createHelper.createRichTextString("Rent Revision due"));
        headerRow.createCell(25).setCellValue(createHelper.createRichTextString("Alert For Action"));
        headerRow.createCell(26).setCellValue(createHelper.createRichTextString("Alert For Information"));
        headerRow.createCell(27).setCellValue(createHelper.createRichTextString("Remarks"));

        // Branch Officers and Contact Details
        headerRow.createCell(28).setCellValue(createHelper.createRichTextString("Officer Name"));
        headerRow.createCell(29).setCellValue(createHelper.createRichTextString("Designation"));
        headerRow.createCell(30).setCellValue(createHelper.createRichTextString("Country"));
        headerRow.createCell(31).setCellValue(createHelper.createRichTextString("City"));
        headerRow.createCell(32).setCellValue(createHelper.createRichTextString("Email"));
        headerRow.createCell(33).setCellValue(createHelper.createRichTextString("Country Code"));
        headerRow.createCell(34).setCellValue(createHelper.createRichTextString("Official Phone number"));
        headerRow.createCell(35).setCellValue(createHelper.createRichTextString("Country Code"));
        headerRow.createCell(36).setCellValue(createHelper.createRichTextString("Personal Phone Number"));

        // Branch Statutary Details
        headerRow.createCell(37).setCellValue(createHelper.createRichTextString("Statutary Details"));
        headerRow.createCell(38).setCellValue(createHelper.createRichTextString("Registration Number"));
        headerRow.createCell(39).setCellValue(createHelper.createRichTextString("Validity From"));
        headerRow.createCell(40).setCellValue(createHelper.createRichTextString("Validity To"));
        headerRow.createCell(41).setCellValue(createHelper.createRichTextString("Alert For Action"));
        headerRow.createCell(42).setCellValue(createHelper.createRichTextString("Alert For Information"));
        headerRow.createCell(43).setCellValue(createHelper.createRichTextString("Name and Address of Consultant"));
        headerRow.createCell(44).setCellValue(createHelper.createRichTextString("Remarks"));

        // Operational Reminders
        headerRow.createCell(45).setCellValue(createHelper.createRichTextString("Requirements"));
        headerRow.createCell(46).setCellValue(createHelper.createRichTextString("Validity From"));
        headerRow.createCell(47).setCellValue(createHelper.createRichTextString("Validity To"));
        headerRow.createCell(48).setCellValue(createHelper.createRichTextString("Recurrance"));
        headerRow.createCell(49).setCellValue(createHelper.createRichTextString("Alert For Action"));
        headerRow.createCell(50).setCellValue(createHelper.createRichTextString("Alert For Information"));
        headerRow.createCell(51).setCellValue(createHelper.createRichTextString("Remarks"));

        // Branch Cash Account
        headerRow.createCell(52).setCellValue(createHelper.createRichTextString("Custodian For Safe Deposit"));
        headerRow.createCell(53).setCellValue(createHelper.createRichTextString("Opening Balance"));
        headerRow.createCell(54).setCellValue(createHelper.createRichTextString("Country Code"));
        headerRow.createCell(55).setCellValue(createHelper.createRichTextString("Custodain Phone Number"));
        headerRow.createCell(56).setCellValue(createHelper.createRichTextString("Email"));
        headerRow.createCell(57).setCellValue(createHelper.createRichTextString("Cashier Name"));
        headerRow.createCell(58).setCellValue(createHelper.createRichTextString("Country Code"));
        headerRow.createCell(59).setCellValue(createHelper.createRichTextString("Cashier Phone Number"));
        headerRow.createCell(60).setCellValue(createHelper.createRichTextString("Cashier Email"));
        headerRow.createCell(61).setCellValue(createHelper.createRichTextString("Cashier Knowledge Library"));
        headerRow.createCell(62)
                .setCellValue(createHelper.createRichTextString("Petty Cash Transaction's Approval Required"));
        headerRow.createCell(63).setCellValue(createHelper.createRichTextString("Approval Limit"));
        headerRow.createCell(64).setCellValue(createHelper.createRichTextString("Petty Cash Opening balance"));

        // Branch Insurance
        headerRow.createCell(65).setCellValue(createHelper.createRichTextString("Policy Type"));
        headerRow.createCell(66).setCellValue(createHelper.createRichTextString("Policy Number"));
        headerRow.createCell(67).setCellValue(createHelper.createRichTextString("Insurance Company"));
        headerRow.createCell(68).setCellValue(createHelper.createRichTextString("Validity From"));
        headerRow.createCell(69).setCellValue(createHelper.createRichTextString("Validity To"));
        headerRow.createCell(70).setCellValue(createHelper.createRichTextString("Annual Premier"));
        headerRow.createCell(71).setCellValue(createHelper.createRichTextString("Alert For Action"));
        headerRow.createCell(72).setCellValue(createHelper.createRichTextString("Alert for Information"));
        headerRow.createCell(73).setCellValue(createHelper.createRichTextString("Remarks"));

        // Bank Account Details
        headerRow.createCell(74).setCellValue(createHelper.createRichTextString("Bank Name"));
        headerRow.createCell(75).setCellValue(createHelper.createRichTextString("Account Number"));
        headerRow.createCell(76).setCellValue(createHelper.createRichTextString("Opening Balance"));
        headerRow.createCell(77).setCellValue(createHelper.createRichTextString("Account Type"));
        headerRow.createCell(78).setCellValue(createHelper.createRichTextString("Routing Number"));
        headerRow.createCell(79).setCellValue(createHelper.createRichTextString("Swift Code"));
        headerRow.createCell(80).setCellValue(createHelper.createRichTextString("Country Code"));
        headerRow.createCell(81).setCellValue(createHelper.createRichTextString("Bank Phone Number"));
        headerRow.createCell(82).setCellValue(createHelper.createRichTextString("Bank Address"));
        headerRow.createCell(83).setCellValue(createHelper.createRichTextString("Bank Instrument custodian Name"));
        headerRow.createCell(84).setCellValue(createHelper.createRichTextString("Bank Instrument custodian Email"));
        headerRow.createCell(85).setCellValue(createHelper.createRichTextString("Authorized Signatory Name"));
        headerRow.createCell(86).setCellValue(createHelper.createRichTextString("Authorized Signatory Email"));

        /*
         * XSSFDataValidationConstraint cashConstraint = (XSSFDataValidationConstraint)
         * dvHelper.createExplicitListConstraint(new String[]{"Leased", "Rented",
         * "Owned"});
         * CellRangeAddressList cashList = new CellRangeAddressList(rowCount, rowCount,
         * 23, 23);//cash/credit
         * XSSFDataValidation cashValidation =
         * (XSSFDataValidation)dvHelper.createValidation(cashConstraint, cashList);
         * cashValidation.setShowErrorBox(true);
         * sheets.addValidationData(cashValidation);
         */

        XSSFCellStyle style1 = workbook.createCellStyle();
        Font font1 = workbook.createFont();
        font1.setBold(true);
        style1.setFont(font1);

        for (int b = 0; b < headerRow.getLastCellNum(); b++) {
            sheets.autoSizeColumn(b);
            headerRow.getCell(b).setCellStyle(style1);
            sheets.lockAutoFilter(true);
        }
        int rowCount = 3;

        Row mainRow = sheets.createRow((short) rowCount);
        // Input fields

        // Branch Setup
        mainRow.createCell(0).setCellValue(createHelper.createRichTextString(user.getOrganization().getName()));

        mainRow.createCell(1).setCellValue(createHelper.createRichTextString("HSR"));

        String excelFormatPattern = DateFormatConverter.convert(Locale.ENGLISH, DateUtil.idosdf);
        CellStyle dateStyle = workbook.createCellStyle();
        DataFormat poiFormat = workbook.createDataFormat();
        dateStyle.setDataFormat(poiFormat.getFormat(excelFormatPattern));
        mainRow.createCell(2).setCellStyle(dateStyle);

        Name namedCell = workbook.createName();
        namedCell = workbook.createName();
        namedCell.setNameName("stateCodes");
        namedCell.setRefersToFormula("hiddenMapping!$A1:$A$" + gstinStateCodes.size());
        XSSFDataValidationConstraint stateCodesConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("stateCodes");
        CellRangeAddressList stateCodesList = new CellRangeAddressList(rowCount, rowCount, 3, 3);
        XSSFDataValidation stateCodesValidation = (XSSFDataValidation) dvHelper.createValidation(stateCodesConstraint,
                stateCodesList);
        stateCodesValidation.setShowErrorBox(true);
        stateCodesValidation.setEmptyCellAllowed(false);
        sheets.addValidationData(stateCodesValidation);

        mainRow.createCell(4).setCellValue(createHelper.createRichTextString("ABCEDEF1234567"));

        CellRangeAddressList stateCodesList1 = new CellRangeAddressList(rowCount, rowCount, 5, 5);
        XSSFDataValidation stateCodesValidation1 = (XSSFDataValidation) dvHelper.createValidation(stateCodesConstraint,
                stateCodesList1);
        stateCodesValidation1.setShowErrorBox(true);
        stateCodesValidation1.setEmptyCellAllowed(false);
        sheets.addValidationData(stateCodesValidation1);

        mainRow.createCell(6).setCellValue(createHelper.createRichTextString("ABCEDEF1234567"));

        mainRow.createCell(7).setCellValue(createHelper.createRichTextString("address"));

        namedCell = workbook.createName();
        namedCell.setNameName("countries");
        namedCell.setRefersToFormula("hiddenMapping!$B$1:$B$" + countries.size());
        XSSFDataValidationConstraint countryConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("countries");
        CellRangeAddressList countryList = new CellRangeAddressList(rowCount, rowCount, 8, 8);
        XSSFDataValidation countryValidation = (XSSFDataValidation) dvHelper.createValidation(countryConstraint,
                countryList);
        countryValidation.setShowErrorBox(true);
        countryValidation.setEmptyCellAllowed(false);
        sheets.addValidationData(countryValidation);

        namedCell = workbook.createName();
        namedCell.setNameName("currencies");
        namedCell.setRefersToFormula("hiddenMapping!$C$1:$C$" + countryCurrencies.size());
        XSSFDataValidationConstraint countryCurrencyConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("currencies");
        CellRangeAddressList countryCurrencyList = new CellRangeAddressList(rowCount, rowCount, 9, 9);
        XSSFDataValidation countryCurrencyValidation = (XSSFDataValidation) dvHelper
                .createValidation(countryCurrencyConstraint, countryCurrencyList);
        countryCurrencyValidation.setShowErrorBox(true);
        countryCurrencyValidation.setEmptyCellAllowed(false);
        sheets.addValidationData(countryCurrencyValidation);

        namedCell = workbook.createName();
        namedCell.setNameName("states");
        namedCell.setRefersToFormula("hiddenMapping!$D$1:$D$" + listOfStates.size());
        XSSFDataValidationConstraint stateConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("states");
        CellRangeAddressList stateList = new CellRangeAddressList(rowCount, rowCount, 10, 10);
        XSSFDataValidation stateValidation = (XSSFDataValidation) dvHelper.createValidation(stateConstraint, stateList);
        stateValidation.setShowErrorBox(true);
        stateValidation.setEmptyCellAllowed(false);
        sheets.addValidationData(stateValidation);

        mainRow.createCell(11).setCellValue(createHelper.createRichTextString("Mumbai"));

        namedCell = workbook.createName();
        namedCell.setNameName("telephoneCodes");
        namedCell.setRefersToFormula("hiddenMapping!$E$1:$E$" + listOfCountryCodes.size());
        XSSFDataValidationConstraint telephoneCodeConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("telephoneCodes");
        CellRangeAddressList telephoneCodesList = new CellRangeAddressList(rowCount, rowCount, 12, 12);
        XSSFDataValidation codeValidation = (XSSFDataValidation) dvHelper.createValidation(telephoneCodeConstraint,
                telephoneCodesList);
        codeValidation.setShowErrorBox(true);
        sheets.addValidationData(codeValidation);

        mainRow.createCell(13).setCellValue(createHelper.createRichTextString("9932345678"));

        XSSFDataValidationConstraint premiseTypeConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "Rented", "Leased", "Owned" });
        CellRangeAddressList premiseTypeList = new CellRangeAddressList(rowCount, rowCount, 14, 14);// GST customer Type
        XSSFDataValidation premiseTypeValidation = (XSSFDataValidation) dvHelper.createValidation(premiseTypeConstraint,
                premiseTypeList);
        premiseTypeValidation.setShowErrorBox(true);
        premiseTypeValidation.setEmptyCellAllowed(false);
        sheets.addValidationData(premiseTypeValidation);

        // Branch Premise Detail
        mainRow.createCell(15).setCellStyle(dateStyle);

        mainRow.createCell(16).setCellStyle(dateStyle);

        XSSFDataValidationConstraint recurrenceConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "Weekely", "Monthly", "Quarterly", "Half Yearly",
                        "Annually", "Once in 2 Years", "Once in 3 years", "One Time" });

        CellRangeAddressList recurrenceTypeList = new CellRangeAddressList(rowCount, rowCount, 17, 17);// GST customer
                                                                                                       // Type
        XSSFDataValidation recurrenceTypeValidation = (XSSFDataValidation) dvHelper
                .createValidation(recurrenceConstraint, recurrenceTypeList);
        recurrenceTypeValidation.setShowErrorBox(true);
        recurrenceTypeValidation.setEmptyCellAllowed(false);
        sheets.addValidationData(recurrenceTypeValidation);

        mainRow.createCell(18).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(19).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(20).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(21).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(22).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(23).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(24).setCellStyle(dateStyle);

        mainRow.createCell(25).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(26).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(27).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        // Branch Officers and contact details
        mainRow.createCell(28).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(29).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        CellRangeAddressList countryList1 = new CellRangeAddressList(rowCount, rowCount, 30, 30);
        XSSFDataValidation countryValidation1 = (XSSFDataValidation) dvHelper.createValidation(countryConstraint,
                countryList1);
        countryValidation1.setShowErrorBox(true);
        countryValidation1.setEmptyCellAllowed(false);
        sheets.addValidationData(countryValidation1);

        mainRow.createCell(31).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(32).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        CellRangeAddressList telephoneCodesList1 = new CellRangeAddressList(rowCount, rowCount, 33, 33);
        XSSFDataValidation codeValidation1 = (XSSFDataValidation) dvHelper.createValidation(telephoneCodeConstraint,
                telephoneCodesList1);
        codeValidation1.setShowErrorBox(true);
        sheets.addValidationData(codeValidation1);

        mainRow.createCell(34).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        CellRangeAddressList telephoneCodesList2 = new CellRangeAddressList(rowCount, rowCount, 35, 35);
        XSSFDataValidation codeValidation2 = (XSSFDataValidation) dvHelper.createValidation(telephoneCodeConstraint,
                telephoneCodesList2);
        codeValidation2.setShowErrorBox(true);
        sheets.addValidationData(codeValidation2);

        mainRow.createCell(36).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        // Statutary Details
        mainRow.createCell(37).setCellValue(createHelper.createRichTextString("ABCEDEF"));
        mainRow.createCell(38).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(39).setCellStyle(dateStyle);
        mainRow.createCell(40).setCellStyle(dateStyle);

        mainRow.createCell(41).setCellValue(createHelper.createRichTextString("ABCEDEF"));
        mainRow.createCell(42).setCellValue(createHelper.createRichTextString("ABCEDEF"));
        mainRow.createCell(43).setCellValue(createHelper.createRichTextString("ABCEDEF"));
        mainRow.createCell(44).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        // Operational Reminders

        mainRow.createCell(45).setCellValue(createHelper.createRichTextString("ABCEDEF"));
        mainRow.createCell(46).setCellStyle(dateStyle);
        mainRow.createCell(47).setCellStyle(dateStyle);

        CellRangeAddressList recurrenceTypeList1 = new CellRangeAddressList(rowCount, rowCount, 48, 48);// GST customer
                                                                                                        // Type
        XSSFDataValidation recurrenceTypeValidation1 = (XSSFDataValidation) dvHelper
                .createValidation(recurrenceConstraint, recurrenceTypeList1);
        recurrenceTypeValidation1.setShowErrorBox(true);
        recurrenceTypeValidation1.setEmptyCellAllowed(false);
        sheets.addValidationData(recurrenceTypeValidation1);

        mainRow.createCell(49).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(50).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(51).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        // Branch Cash account
        mainRow.createCell(52).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(53).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        // Branch Cash Account
        CellRangeAddressList telephoneCodesList3 = new CellRangeAddressList(rowCount, rowCount, 54, 54);
        XSSFDataValidation codeValidation3 = (XSSFDataValidation) dvHelper.createValidation(telephoneCodeConstraint,
                telephoneCodesList3);
        codeValidation3.setShowErrorBox(true);
        sheets.addValidationData(codeValidation3);

        mainRow.createCell(55).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(56).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(57).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        CellRangeAddressList telephoneCodesList4 = new CellRangeAddressList(rowCount, rowCount, 58, 58);
        XSSFDataValidation codeValidation4 = (XSSFDataValidation) dvHelper.createValidation(telephoneCodeConstraint,
                telephoneCodesList4);
        codeValidation4.setShowErrorBox(true);
        sheets.addValidationData(codeValidation4);

        mainRow.createCell(59).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(60).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(61).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        CellRangeAddressList addressList = new CellRangeAddressList(rowCount, rowCount, 62, 62);// cust GST registered
        XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(yesNoConstraint, addressList);
        validation.setEmptyCellAllowed(false);
        validation.setShowErrorBox(true);
        sheets.addValidationData(validation);

        mainRow.createCell(63).setCellValue(createHelper.createRichTextString("1234"));

        mainRow.createCell(64).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        // Branch Insurance
        mainRow.createCell(65).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(66).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(67).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(68).setCellStyle(dateStyle);

        mainRow.createCell(69).setCellStyle(dateStyle);

        mainRow.createCell(70).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(71).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(72).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(73).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        // Bank account Details
        mainRow.createCell(74).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(75).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(76).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        namedCell = workbook.createName();
        namedCell.setNameName("accountType");
        namedCell.setRefersToFormula("hiddenMapping!$F$1:$F$" + bankAccountType.size());
        XSSFDataValidationConstraint accountTypeConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("accountType");
        CellRangeAddressList accountTypeList = new CellRangeAddressList(rowCount, rowCount, 77, 77);
        XSSFDataValidation accountTypeValidation = (XSSFDataValidation) dvHelper.createValidation(accountTypeConstraint,
                accountTypeList);
        accountTypeValidation.setShowErrorBox(true);
        accountTypeValidation.setEmptyCellAllowed(false);
        sheets.addValidationData(accountTypeValidation);

        mainRow.createCell(78).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(79).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        CellRangeAddressList telephoneCodesList5 = new CellRangeAddressList(rowCount, rowCount, 80, 80);
        XSSFDataValidation codeValidation5 = (XSSFDataValidation) dvHelper.createValidation(telephoneCodeConstraint,
                telephoneCodesList5);
        codeValidation5.setShowErrorBox(true);
        sheets.addValidationData(codeValidation5);

        mainRow.createCell(81).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(82).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(83).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(84).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(85).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(86).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        for (int b = 0; b < mainRow.getLastCellNum(); b++) {
            sheets.autoSizeColumn(b);
        }
        FileOutputStream fileOut = new FileOutputStream(path);
        workbook.write(fileOut);
        fileOut.close();
        log.log(Level.FINE, "===============End ");
        return sheetName;
    }

    @Override
    public String createTransactionTemplateExcel(Users user, EntityManager em, String path, String sheetName)
            throws FileNotFoundException, IOException {
        log.log(Level.INFO, ">>>>> inside create transaction excel template");
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("presentStatus", 1);
        List<Branch> branchList = genericDAO.findByCriteria(Branch.class, criterias, em);
        List<Project> projectList = genericDAO.findByCriteria(Project.class, criterias, em);
        ChartOfAccountsService coaService = new ChartOfAccountsServiceImpl();
        List<Specifics> orgSpecificsList = coaService.getIncomesCoaChildNodes(em, user);

        criterias.put("type", 2);
        log.log(Level.INFO, "criterias=" + criterias);
        List<Vendor> customerList = genericDAO.findByCriteria(Vendor.class, criterias, em);
        log.log(Level.INFO, "customer size=" + customerList.size());

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheets = workbook.createSheet(sheetName);
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheets);

        // hidden mapping start
        XSSFSheet hiddenMappingSheet = workbook.createSheet("hiddenMapping");

        int totalHiddenRows = 0; // set it to max list size
        if (branchList.size() > totalHiddenRows) {
            totalHiddenRows = branchList.size();
        }
        if (projectList.size() > totalHiddenRows) {
            totalHiddenRows = projectList.size();
        }
        if (customerList.size() > totalHiddenRows) {
            totalHiddenRows = customerList.size();
        }
        if (orgSpecificsList.size() > totalHiddenRows) {
            totalHiddenRows = orgSpecificsList.size();
        }
        for (int i = 0; i < totalHiddenRows; i++) {
            XSSFRow row = hiddenMappingSheet.createRow(i);

            if (i < branchList.size()) {
                XSSFCell cell = row.createCell(0);
                cell.setCellValue(branchList.get(i).getName());
            }

            if (i < projectList.size()) {
                XSSFCell cell = row.createCell(1);
                cell.setCellValue(projectList.get(i).getName());
            }
            if (i < customerList.size()) {
                XSSFCell cell = row.createCell(2);
                cell.setCellValue(customerList.get(i).getName());
            }
            if (i < orgSpecificsList.size()) {
                XSSFCell cell = row.createCell(3);
                cell.setCellValue(orgSpecificsList.get(i).getName());
            }

        }

        XSSFDataValidationConstraint withOrWithoutIGstConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(
                        new String[] { "OnPaymentOfIGST", "Under BOND/LUT without payment of IGST" });
        XSSFDataValidationConstraint typeOfSupplyConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "Regular Supply", "Supply Applicable For Reverse Charge",
                        "This is an Export Supply", "This is supply to SEZ Unit SEZ Developer",
                        "This is Deemed Export Supply", "Supply Made Through Ecommerce Operator", "Bill Of Supply" });
        Row headerRow = sheets.createRow(0);
        headerRow.createCell(0).setCellValue(createHelper.createRichTextString("Transaction Serial Number*"));
        headerRow.createCell(1).setCellValue(createHelper.createRichTextString("Transaction Date*"));
        headerRow.createCell(2).setCellValue(createHelper.createRichTextString("Branch*"));
        headerRow.createCell(3).setCellValue(createHelper.createRichTextString("Project"));
        headerRow.createCell(4).setCellValue(createHelper.createRichTextString("Type Of Supply*"));
        headerRow.createCell(5).setCellValue(createHelper.createRichTextString("With or without GST"));
        headerRow.createCell(6).setCellValue(createHelper.createRichTextString("Customer ID*"));
        // headerRow.createCell(7).setCellValue(createHelper.createRichTextString("Place
        // Of Supply(Location)*"));
        headerRow.createCell(7).setCellValue(createHelper.createRichTextString("PO Reference"));
        headerRow.createCell(8).setCellValue(createHelper.createRichTextString("Item Code*"));
        headerRow.createCell(9).setCellValue(createHelper.createRichTextString("Price*"));
        headerRow.createCell(10).setCellValue(createHelper.createRichTextString("Units*"));
        headerRow.createCell(11).setCellValue(createHelper.createRichTextString("Discount %"));
        headerRow.createCell(12).setCellValue(createHelper.createRichTextString("Advance Adjustment"));
        headerRow.createCell(13).setCellValue(createHelper.createRichTextString("Transaction Notes"));

        workbook.setSheetHidden(1, true);
        XSSFCellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        for (int b = 0; b < headerRow.getLastCellNum(); b++) {
            sheets.autoSizeColumn(b);
            headerRow.getCell(b).setCellStyle(style);
            sheets.lockAutoFilter(true);
        }

        int rowCount = 1;
        Row mainRow = sheets.createRow((short) rowCount);
        mainRow.createCell(0).setCellValue(createHelper.createRichTextString("1"));

        String excelFormatPattern = DateFormatConverter.convert(Locale.ENGLISH, DateUtil.mysqldf);
        CellStyle dateStyle = workbook.createCellStyle();
        DataFormat poiFormat = workbook.createDataFormat();
        dateStyle.setDataFormat(poiFormat.getFormat(excelFormatPattern));
        mainRow.createCell(1).setCellStyle(dateStyle);

        Name namedCell = workbook.createName();
        namedCell.setNameName("branch_list");
        namedCell.setRefersToFormula("hiddenMapping!$A$1:$A$" + branchList.size());
        XSSFDataValidationConstraint branchConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("branch_list");
        CellRangeAddressList branchNameList = new CellRangeAddressList(rowCount, rowCount, 2, 2);
        XSSFDataValidation branchNameValidation = (XSSFDataValidation) dvHelper.createValidation(branchConstraint,
                branchNameList);
        branchNameValidation.setShowErrorBox(true);
        sheets.addValidationData(branchNameValidation);

        if (projectList.size() > 0) {
            Name namedCell1 = workbook.createName();
            namedCell1.setNameName("project_list");
            namedCell1.setRefersToFormula("hiddenMapping!$B$1:$B$" + projectList.size());
            XSSFDataValidationConstraint projectListConstraint = (XSSFDataValidationConstraint) dvHelper
                    .createFormulaListConstraint("project_list");
            CellRangeAddressList projectNameList = new CellRangeAddressList(rowCount, rowCount, 3, 3);
            XSSFDataValidation projectNameListValidation = (XSSFDataValidation) dvHelper
                    .createValidation(projectListConstraint, projectNameList);
            projectNameListValidation.setShowErrorBox(true);
            sheets.addValidationData(projectNameListValidation);
        } else {
            mainRow.createCell(3).setCellValue(createHelper.createRichTextString(""));
        }
        CellRangeAddressList typeOfSupplyList = new CellRangeAddressList(rowCount, rowCount, 4, 4);// cash/credit
        XSSFDataValidation typeOfSupplyValidation = (XSSFDataValidation) dvHelper
                .createValidation(typeOfSupplyConstraint, typeOfSupplyList);
        typeOfSupplyValidation.setShowErrorBox(true);
        sheets.addValidationData(typeOfSupplyValidation);
        mainRow.createCell(4).setCellValue("Regular Supply");

        CellRangeAddressList withOrWithoutIGSTList = new CellRangeAddressList(rowCount, rowCount, 5, 5);// cash/credit
        XSSFDataValidation withOrWithoutIGSTValidation = (XSSFDataValidation) dvHelper
                .createValidation(withOrWithoutIGstConstraint, withOrWithoutIGSTList);
        withOrWithoutIGSTValidation.setShowErrorBox(true);
        sheets.addValidationData(withOrWithoutIGSTValidation);

        Name namedCell1 = workbook.createName();
        namedCell1.setNameName("customer_list");
        namedCell1.setRefersToFormula("hiddenMapping!$C$1:$C$" + customerList.size());
        XSSFDataValidationConstraint customerListConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("customer_list");
        CellRangeAddressList customerNameList = new CellRangeAddressList(rowCount, rowCount, 6, 6);
        XSSFDataValidation customerNameListValidation = (XSSFDataValidation) dvHelper
                .createValidation(customerListConstraint, customerNameList);
        customerNameListValidation.setShowErrorBox(true);
        sheets.addValidationData(customerNameListValidation);
        // mainRow.createCell(6).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        // mainRow.createCell(7).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(7).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        Name namedCell2 = workbook.createName();
        namedCell2.setNameName("item_list");
        namedCell2.setRefersToFormula("hiddenMapping!$D$1:$D$" + orgSpecificsList.size());
        XSSFDataValidationConstraint itemNameConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("item_list");
        CellRangeAddressList itemNameList = new CellRangeAddressList(rowCount, rowCount, 8, 8);
        XSSFDataValidation itemNameValidation = (XSSFDataValidation) dvHelper.createValidation(itemNameConstraint,
                itemNameList);
        itemNameValidation.setShowErrorBox(true);
        sheets.addValidationData(itemNameValidation);

        // mainRow.createCell(9).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(9).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(10).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(11).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(12).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(13).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        for (int b = 0; b < mainRow.getLastCellNum(); b++) {
            sheets.autoSizeColumn(b);
        }

        FileOutputStream fileOut = new FileOutputStream(path);
        workbook.write(fileOut);
        fileOut.close();
        log.log(Level.FINE, "===============End ");
        return sheetName;
    }

    @Override
    public String createRecievePaymentFromCustomerTransactionTemplateExcel(Users user, EntityManager em,
            String path, String sheetName) throws Exception {
        log.log(Level.INFO, ">>>>> inside create receive payment from customer transaction excel template");

        String stbuf = ("select obj from Branch obj where obj.organization.id=?1 and obj.presentStatus=1");
        ArrayList inparam = new ArrayList(1);
        inparam.add(user.getOrganization().getId());
        List<Branch> branchList = genericDAO.queryWithParams(stbuf.toString(), em, inparam);

        String stbuf1 = ("select obj from Vendor obj where obj.organization.id=?1 and presentStatus=?2 and obj.type=?3 and (obj.purchaseType=?4 or obj.purchaseType=?5)");
        ArrayList inparam1 = new ArrayList(5);
        inparam1.add(user.getOrganization().getId());
        inparam1.add(Integer.parseInt("1"));
        inparam1.add(Integer.parseInt("2"));
        inparam1.add(Integer.parseInt("0"));
        inparam1.add(Integer.parseInt("2"));
        List<Vendor> customerList = genericDAO.queryWithParams(stbuf1.toString(), em, inparam1);
        String stbuf2 = ("select obj from Transaction obj where obj.transactionBranchOrganization.id=?1 and (obj.paymentStatus=?2 or obj.paymentStatus=?3)  and obj.transactionPurpose.transactionPurpose=?4 and obj.presentStatus=1");
        ArrayList inparam2 = new ArrayList(3);
        inparam2.add(user.getOrganization().getId());
        inparam2.add("NOT-PAID");
        inparam2.add("PARTLY-PAID");
        inparam2.add("Sell On credit & collect payment later");

        List<Transaction> creditCustVendPendingInvoices = genericDAO.queryWithParams(stbuf2.toString(), em, inparam2);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheets = workbook.createSheet(sheetName);
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheets);

        // hidden mapping start
        XSSFSheet hiddenMappingSheet = workbook.createSheet("hiddenMapping");

        int totalHiddenRows = 0; // set it to max list size
        if (branchList.size() > totalHiddenRows) {
            totalHiddenRows = branchList.size();
        }

        if (customerList.size() > totalHiddenRows) {
            totalHiddenRows = customerList.size();
        }

        if (creditCustVendPendingInvoices.size() > totalHiddenRows) {
            totalHiddenRows = creditCustVendPendingInvoices.size();
        }
        for (int i = 0; i < totalHiddenRows; i++) {
            XSSFRow row = hiddenMappingSheet.createRow(i);

            if (i < branchList.size()) {
                XSSFCell cell = row.createCell(0);
                cell.setCellValue(branchList.get(i).getName());
            }

            if (i < customerList.size()) {
                XSSFCell cell = row.createCell(1);
                cell.setCellValue(customerList.get(i).getName());
            }

            if (i < creditCustVendPendingInvoices.size()) {
                XSSFCell cell = row.createCell(2);
                try {
                    // cell.setCellValue(creditCustVendPendingInvoices.get(i).getTransactionSpecifics().getName()
                    // + "(" +
                    // DateUtil.idosdf.format(creditCustVendPendingInvoices.get(i).getTransactionDate())
                    // + ")(" + creditCustVendPendingInvoices.get(i).getNetAmount() + ")"
                    // +creditCustVendPendingInvoices.get(i).getTransactionRefNumber());
                    cell.setCellValue(creditCustVendPendingInvoices.get(i).getTransactionRefNumber() + "-"
                            + creditCustVendPendingInvoices.get(i).getTransactionVendorCustomer().getName() + "-"
                            + creditCustVendPendingInvoices.get(i).getInvoiceNumber() + "-"
                            + creditCustVendPendingInvoices.get(i).getCustomerDuePayment());
                } catch (NullPointerException e) {
                    cell.setCellValue("");
                }

            }
            if (i < branchList.size()) {
                XSSFCell cell = row.createCell(3);
                try {
                    cell.setCellValue(branchList.get(i).getBranchBankAccounts().get(0).getBankName());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
            }
        }
        XSSFRow row = hiddenMappingSheet.createRow(creditCustVendPendingInvoices.size() + 1);
        XSSFCell cell = row.createCell(2);
        cell.setCellValue("Opening Balance");

        XSSFDataValidationConstraint typeOfPayment = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "CASH", "BANK" });
        Row headerRow = sheets.createRow(0);
        headerRow.createCell(0).setCellValue(createHelper.createRichTextString("Transaction Serial Number*"));
        headerRow.createCell(1).setCellValue(createHelper.createRichTextString("Transaction Date*"));
        headerRow.createCell(2).setCellValue(createHelper.createRichTextString("Branch*"));
        headerRow.createCell(3).setCellValue(createHelper.createRichTextString("Customer Name*"));
        headerRow.createCell(4).setCellValue(createHelper.createRichTextString("Select Invoice Transaction ID*"));
        headerRow.createCell(5).setCellValue(createHelper.createRichTextString("Payment Recieved"));
        headerRow.createCell(6).setCellValue(createHelper.createRichTextString("Tax Withheld"));
        headerRow.createCell(7).setCellValue(createHelper.createRichTextString("Reciept By"));
        headerRow.createCell(8).setCellValue(createHelper.createRichTextString("Bank"));
        headerRow.createCell(9).setCellValue(createHelper.createRichTextString("Instrument Number"));
        headerRow.createCell(10).setCellValue(createHelper.createRichTextString("Instrument Date"));
        headerRow.createCell(11).setCellValue(createHelper.createRichTextString("Input Reciept Details"));
        headerRow.createCell(12).setCellValue(createHelper.createRichTextString("Transaction Notes"));

        workbook.setSheetHidden(1, true);
        XSSFCellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        for (int b = 0; b < headerRow.getLastCellNum(); b++) {
            sheets.autoSizeColumn(b);
            headerRow.getCell(b).setCellStyle(style);
            sheets.lockAutoFilter(true);
        }

        int rowCount = 1;
        Row mainRow = sheets.createRow((short) rowCount);
        mainRow.createCell(0).setCellValue(createHelper.createRichTextString("1"));

        String excelFormatPattern = DateFormatConverter.convert(Locale.ENGLISH, DateUtil.mysqldf);
        CellStyle dateStyle = workbook.createCellStyle();
        DataFormat poiFormat = workbook.createDataFormat();
        dateStyle.setDataFormat(poiFormat.getFormat(excelFormatPattern));
        mainRow.createCell(1).setCellStyle(dateStyle);

        Name namedCell = workbook.createName();
        namedCell.setNameName("branch_list");
        namedCell.setRefersToFormula("hiddenMapping!$A$1:$A$" + branchList.size());
        XSSFDataValidationConstraint branchConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("branch_list");
        CellRangeAddressList branchNameList = new CellRangeAddressList(rowCount, rowCount, 2, 2);
        XSSFDataValidation branchNameValidation = (XSSFDataValidation) dvHelper.createValidation(branchConstraint,
                branchNameList);
        branchNameValidation.setShowErrorBox(true);
        sheets.addValidationData(branchNameValidation);

        Name namedCell1 = workbook.createName();
        namedCell1.setNameName("customer_list");
        namedCell1.setRefersToFormula("hiddenMapping!$B$1:$B$" + customerList.size());
        XSSFDataValidationConstraint customerListConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("customer_list");
        CellRangeAddressList customerNameList = new CellRangeAddressList(rowCount, rowCount, 3, 3);
        XSSFDataValidation customerNameListValidation = (XSSFDataValidation) dvHelper
                .createValidation(customerListConstraint, customerNameList);
        customerNameListValidation.setShowErrorBox(true);
        sheets.addValidationData(customerNameListValidation);

        Name namedCell2 = workbook.createName();
        namedCell2.setNameName("invoice_list");
        namedCell2.setRefersToFormula("hiddenMapping!$C$1:$C$" + creditCustVendPendingInvoices.size());
        XSSFDataValidationConstraint invoiceListConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("invoice_list");
        CellRangeAddressList invoicesList = new CellRangeAddressList(rowCount, rowCount, 4, 4);
        XSSFDataValidation invoicesListValidation = (XSSFDataValidation) dvHelper
                .createValidation(invoiceListConstraint, invoicesList);
        invoicesListValidation.setShowErrorBox(true);
        sheets.addValidationData(invoicesListValidation);

        mainRow.createCell(5).setCellValue(createHelper.createRichTextString("1234"));

        mainRow.createCell(6).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        CellRangeAddressList typeOfPaymentList = new CellRangeAddressList(rowCount, rowCount, 7, 7);// cash/credit
        XSSFDataValidation typeOfPaymentValidation = (XSSFDataValidation) dvHelper.createValidation(typeOfPayment,
                typeOfPaymentList);
        typeOfPaymentValidation.setShowErrorBox(true);
        sheets.addValidationData(typeOfPaymentValidation);

        Name namedCell3 = workbook.createName();
        namedCell3.setNameName("branch_bank_list");
        namedCell3.setRefersToFormula("hiddenMapping!$D$1:$D$" + branchList.size());
        XSSFDataValidationConstraint branchBankConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("branch_bank_list");
        CellRangeAddressList branchBankNameList = new CellRangeAddressList(rowCount, rowCount, 8, 8);
        XSSFDataValidation branchBankNameValidation = (XSSFDataValidation) dvHelper
                .createValidation(branchBankConstraint, branchBankNameList);
        branchBankNameValidation.setShowErrorBox(true);
        sheets.addValidationData(branchBankNameValidation);

        mainRow.createCell(9).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        dateStyle.setDataFormat(poiFormat.getFormat(excelFormatPattern));
        mainRow.createCell(10).setCellStyle(dateStyle);

        mainRow.createCell(11).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(12).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        for (int b = 0; b < mainRow.getLastCellNum(); b++) {
            sheets.autoSizeColumn(b);
        }

        FileOutputStream fileOut = new FileOutputStream(path);
        workbook.write(fileOut);
        fileOut.close();
        log.log(Level.FINE, "===============End ");
        return sheetName;

    }

    /*
     * @Override
     * public String createRecievePaymentFromCustomerTransactionTemplateExcel(Users
     * user, EntityManager em,
     * String path, String sheetName) throws Exception {
     * log.log(Level.INFO,
     * ">>>>> inside create receive payment from customer transaction excel template"
     * );
     * 
     * 
     * 
     * String stbuf1 =
     * ("select obj from Vendor obj where obj.organization.id=?x and presentStatus=?x and obj.type=?x and (obj.purchaseType=?x or obj.purchaseType=?x)"
     * );
     * ArrayList inparam1 = new ArrayList(5);
     * inparam1.add(user.getOrganization().getId());
     * inparam1.add(Integer.parseInt("1"));
     * inparam1.add(Integer.parseInt("2"));
     * inparam1.add(Integer.parseInt("0"));
     * inparam1.add(Integer.parseInt("2"));
     * 
     * List<Vendor> customerList=genericDAO.queryWithParams(stbuf1.toString(),
     * em,inparam1);
     * String stbuf2 =
     * ("select obj from Transaction obj where obj.transactionBranchOrganization.id=?x and (obj.paymentStatus=?x or obj.paymentStatus=?x)"
     * );
     * ArrayList inparam2 = new ArrayList(3);
     * inparam2.add(user.getOrganization().getId());
     * inparam2.add("NOT-PAID");
     * inparam2.add("PARTLY-PAID");
     * List<Transaction> creditCustVendPendingInvoices =
     * genericDAO.queryWithParams(stbuf2.toString(), em,inparam2);
     * 
     * XSSFWorkbook workbook = new XSSFWorkbook();
     * XSSFSheet sheets = workbook.createSheet(sheetName);
     * CreationHelper createHelper = workbook.getCreationHelper();
     * XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheets);
     * 
     * //hidden mapping start
     * XSSFSheet hiddenMappingSheet = workbook.createSheet("hiddenMapping");
     * 
     * int totalHiddenRows = 0; //set it to max list size
     * if (branchList.size() > totalHiddenRows){
     * totalHiddenRows = branchList.size();
     * }
     * 
     * if (customerList.size() > totalHiddenRows){
     * totalHiddenRows = customerList.size();
     * }
     * 
     * if(creditCustVendPendingInvoices.size() > totalHiddenRows){
     * totalHiddenRows = creditCustVendPendingInvoices.size();
     * }
     * for (int i = 0; i < totalHiddenRows; i++) {
     * XSSFRow row = hiddenMappingSheet.createRow(i);
     * 
     * if (i < branchList.size()) {
     * XSSFCell cell = row.createCell(0);
     * cell.setCellValue(branchList.get(i).getName());
     * }
     * 
     * if( i < customerList.size()) {
     * XSSFCell cell = row.createCell(1);
     * cell.setCellValue(customerList.get(i).getCustomerCode());
     * }
     * 
     * if( i < creditCustVendPendingInvoices.size()){
     * XSSFCell cell = row.createCell(2);
     * try{
     * //cell.setCellValue(creditCustVendPendingInvoices.get(i).
     * getTransactionSpecifics().getName() + "(" +
     * DateUtil.idosdf.format(creditCustVendPendingInvoices.get(i).
     * getTransactionDate()) + ")(" +
     * creditCustVendPendingInvoices.get(i).getNetAmount() + ")"
     * +creditCustVendPendingInvoices.get(i).getTransactionRefNumber());
     * cell.setCellValue(creditCustVendPendingInvoices.get(i).
     * getTransactionRefNumber());
     * }
     * catch(NullPointerException e){
     * cell.setCellValue("");
     * }
     * 
     * }
     * if (i < branchList.size()) {
     * XSSFCell cell = row.createCell(3);
     * try{
     * cell.setCellValue(branchList.get(i).getBranchBankAccounts().get(0).
     * getBankName());
     * }
     * catch(Exception e){
     * cell.setCellValue("");
     * }
     * }
     * }
     * XSSFRow row =
     * hiddenMappingSheet.createRow(creditCustVendPendingInvoices.size()+1);
     * XSSFCell cell = row.createCell(2);
     * cell.setCellValue("Opening Balance");
     * 
     * XSSFDataValidationConstraint typeOfPayment =
     * (XSSFDataValidationConstraint)dvHelper.createExplicitListConstraint(new
     * String[]{"CASH", "BANK"});
     * Row headerRow = sheets.createRow(0);
     * headerRow.createCell(0).setCellValue(createHelper.
     * createRichTextString("Transaction Serial Number*"));
     * headerRow.createCell(1).setCellValue(createHelper.
     * createRichTextString("Transaction Date*"));
     * headerRow.createCell(2).setCellValue(createHelper.createRichTextString(
     * "Branch*"));
     * headerRow.createCell(3).setCellValue(createHelper.
     * createRichTextString("Customer Name*"));
     * headerRow.createCell(4).setCellValue(createHelper.
     * createRichTextString("Select Invoice Transaction ID*"));
     * headerRow.createCell(5).setCellValue(createHelper.
     * createRichTextString("Payment Recieved"));
     * headerRow.createCell(6).setCellValue(createHelper.
     * createRichTextString("Tax Withheld"));
     * headerRow.createCell(7).setCellValue(createHelper.
     * createRichTextString("Reciept By"));
     * headerRow.createCell(8).setCellValue(createHelper.createRichTextString("Bank"
     * ));
     * headerRow.createCell(9).setCellValue(createHelper.
     * createRichTextString("Instrument Number"));
     * headerRow.createCell(10).setCellValue(createHelper.
     * createRichTextString("Instrument Date"));
     * headerRow.createCell(11).setCellValue(createHelper.
     * createRichTextString("Input Reciept Details"));
     * headerRow.createCell(12).setCellValue(createHelper.
     * createRichTextString("Transaction Notes"));
     * 
     * workbook.setSheetHidden(1, true);
     * XSSFCellStyle style = workbook.createCellStyle();
     * Font font = workbook.createFont();
     * font.setBold(true);
     * style.setFont(font);
     * 
     * 
     * for(int b = 0; b < headerRow.getLastCellNum(); b++){
     * sheets.autoSizeColumn(b);
     * headerRow.getCell(b).setCellStyle(style);
     * sheets.lockAutoFilter(true);
     * }
     * 
     * int rowCount = 1;
     * String stbuf = ("select obj from Branch obj where obj.organization.id=?x");
     * ArrayList inparam = new ArrayList(1);
     * inparam.add(user.getOrganization().getId());
     * List<Branch> branchList =genericDAO.queryWithParams(stbuf.toString(),em,
     * inparam);
     * 
     * 
     * Row mainRow = sheets.createRow((short)rowCount);
     * mainRow.createCell(0).setCellValue(createHelper.createRichTextString("1"));
     * 
     * String excelFormatPattern = DateFormatConverter.convert(Locale.ENGLISH,
     * DateUtil.idosdf);
     * CellStyle dateStyle = workbook.createCellStyle();
     * DataFormat poiFormat = workbook.createDataFormat();
     * dateStyle.setDataFormat(poiFormat.getFormat(excelFormatPattern));
     * mainRow.createCell(1).setCellStyle(dateStyle);
     * 
     * Name namedCell = workbook.createName();
     * namedCell.setNameName("branch_list");
     * namedCell.setRefersToFormula("hiddenMapping!$A$1:$A$"+branchList.size());
     * XSSFDataValidationConstraint branchConstraint =
     * (XSSFDataValidationConstraint)
     * dvHelper.createFormulaListConstraint("branch_list");
     * CellRangeAddressList branchNameList = new CellRangeAddressList(rowCount,
     * rowCount,2, 2);
     * XSSFDataValidation branchNameValidation =
     * (XSSFDataValidation)dvHelper.createValidation(branchConstraint,branchNameList
     * );
     * branchNameValidation.setShowErrorBox(true);
     * sheets.addValidationData(branchNameValidation);
     * 
     * Name namedCell1 = workbook.createName();
     * namedCell1.setNameName("customer_list");
     * namedCell1.setRefersToFormula("hiddenMapping!$B$1:$B$"+customerList.size());
     * XSSFDataValidationConstraint customerListConstraint =
     * (XSSFDataValidationConstraint)
     * dvHelper.createFormulaListConstraint("customer_list");
     * CellRangeAddressList customerNameList = new CellRangeAddressList(rowCount,
     * rowCount,3,3);
     * XSSFDataValidation customerNameListValidation =
     * (XSSFDataValidation)dvHelper.createValidation(customerListConstraint,
     * customerNameList);
     * customerNameListValidation.setShowErrorBox(true);
     * sheets.addValidationData(customerNameListValidation);
     * 
     * Name namedCell2 = workbook.createName();
     * namedCell2.setNameName("invoice_list");
     * namedCell2.setRefersToFormula("hiddenMapping!$C$1:$C$"+
     * creditCustVendPendingInvoices.size());
     * XSSFDataValidationConstraint invoiceListConstraint =
     * (XSSFDataValidationConstraint)dvHelper.createFormulaListConstraint(
     * "invoice_list");
     * CellRangeAddressList invoicesList = new CellRangeAddressList(rowCount,
     * rowCount,4,4);
     * XSSFDataValidation invoicesListValidation =
     * (XSSFDataValidation)dvHelper.createValidation(invoiceListConstraint,
     * invoicesList);
     * invoicesListValidation.setShowErrorBox(true);
     * sheets.addValidationData(invoicesListValidation);
     * 
     * mainRow.createCell(5).setCellValue(createHelper.createRichTextString("1234"))
     * ;
     * 
     * mainRow.createCell(6).setCellValue(createHelper.createRichTextString(
     * "ABCEDEF"));
     * 
     * CellRangeAddressList typeOfPaymentList = new CellRangeAddressList(rowCount,
     * rowCount,7,7);//cash/credit
     * XSSFDataValidation typeOfPaymentValidation =
     * (XSSFDataValidation)dvHelper.createValidation(typeOfPayment,typeOfPaymentList
     * );
     * typeOfPaymentValidation.setShowErrorBox(true);
     * sheets.addValidationData(typeOfPaymentValidation);
     * 
     * Name namedCell3 = workbook.createName();
     * namedCell3.setNameName("branch_bank_list");
     * namedCell3.setRefersToFormula("hiddenMapping!$D$1:$D$"+branchList.size());
     * XSSFDataValidationConstraint branchBankConstraint =
     * (XSSFDataValidationConstraint)
     * dvHelper.createFormulaListConstraint("branch_bank_list");
     * CellRangeAddressList branchBankNameList = new CellRangeAddressList(rowCount,
     * rowCount,8, 8);
     * XSSFDataValidation branchBankNameValidation =
     * (XSSFDataValidation)dvHelper.createValidation(branchBankConstraint,
     * branchBankNameList);
     * branchBankNameValidation.setShowErrorBox(true);
     * sheets.addValidationData(branchBankNameValidation);
     * 
     * mainRow.createCell(9).setCellValue(createHelper.createRichTextString(
     * "ABCEDEF"));
     * 
     * dateStyle.setDataFormat(poiFormat.getFormat(excelFormatPattern));
     * mainRow.createCell(10).setCellStyle(dateStyle);
     * 
     * 
     * mainRow.createCell(11).setCellValue(createHelper.createRichTextString(
     * "ABCEDEF"));
     * 
     * mainRow.createCell(12).setCellValue(createHelper.createRichTextString(
     * "ABCEDEF"));
     * 
     * for(int b = 0; b <mainRow.getLastCellNum(); b++){
     * sheets.autoSizeColumn(b);
     * }
     * 
     * FileOutputStream fileOut = new FileOutputStream(path);
     * workbook.write(fileOut);
     * fileOut.close();
     * log.log(Level.FINE, "===============End " );
     * return sheetName;
     * 
     * }
     */

    @Override
    public File createOrgBuySideTransactionData(Users user, EntityManager em, String path, String fileName,
            Date fromTransDate, Date toTransDate) throws Exception {
        FileWriter fw = null;
        BufferedWriter writer = null;
        File file = null;

        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        path = path + fileName;
        file = new File(path);
        List<GSTFiling> gstFilings = new ArrayList<GSTFiling>();
        List<GSTFiling> gstFilingsForTranscations = null;
        List<GSTFiling> gstFilingsForClaimTransactions = null;
        try {
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            writer.append("Comapny Code").append('|');
            writer.append("Company Name").append('|');
            writer.append("State").append('|');
            writer.append("GSTIN").append('|');
            writer.append("Year").append('|');
            writer.append("Month").append('|');
            writer.append("Accounting Document No").append('|');
            writer.append("Accounting Document Date").append('|');
            writer.append("Transaction Count").append('|');
            writer.append("Currency").append('|');
            writer.append("GL Account").append('|');
            writer.append("Document Type").append('|');
            writer.append("Taxability").append('|');
            writer.append("Supply Type").append('|');
            writer.append("Supplier Code").append('|');
            writer.append("Nature Of Supplier").append('|');
            writer.append("GSTIN Of Supplier").append('|');
            writer.append("Supplier State").append('|');
            writer.append("Name Of The Supplier").append('|');
            writer.append("Invoice/Dr Note/Cr Note/Receipt Voucher/Refund Voucher(No)").append('|');
            writer.append("Invoice/Dr Note/Cr Note/Receipt Voucher/Refund Voucher(Date)").append('|');
            writer.append("Invoice/Dr Note/Cr Note/Receipt Voucher/Refund Voucher(Value)").append('|');
            writer.append("Supply attract reverse charge").append('|');
            writer.append("POS (State)").append('|');
            writer.append("Line Item").append('|');
            writer.append("Item Code").append('|');
            writer.append("Category").append('|');
            writer.append("HSN/SAC").append('|');
            writer.append("Product/Service Description").append('|');
            writer.append("UQC").append('|');
            writer.append("Quantity").append('|');
            writer.append("Sale price (Before discount)").append('|');
            writer.append("Discount").append('|');
            writer.append("Net sale price (after discount)").append('|');
            writer.append("VAT").append('|');
            writer.append("Central Excise").append('|');
            writer.append("State Excise").append('|');
            writer.append("Taxable value").append('|');
            writer.append("Total GST Rate").append('|');
            writer.append("IGST (Rate)").append('|');
            writer.append("IGST (Amt)").append('|');
            writer.append("CGST (Rate)").append('|');
            writer.append("CGST (Amt)").append('|');
            writer.append("SGST/UTGST (Rate)").append('|');
            writer.append("SGST/UTGST (Amt)").append('|');
            writer.append("Cess (Rate)").append('|');
            writer.append("Cess (Amount)").append('|');
            writer.append("Eligibilty of ITC").append('|');
            writer.append("ITC IGST (Amt)").append('|');
            writer.append("ITC CGST (Amt)").append('|');
            writer.append("ITC SGST (Amt)").append('|');
            writer.append("ITC Cess (Amt)").append('|');
            writer.append("Nature Of expense").append('|');
            writer.append("Ship From (State)").append('|');
            writer.append("Ship To (State)").append('|');
            writer.append("Way Bill No").append('|');
            writer.append("Transporter name").append('|');
            writer.append("Lorry Receipt number").append('|');
            writer.append("Lorry Receipt date").append('|');
            writer.append("Credit/Debit Note(Original  Invoice No.)").append('|');
            writer.append("Credit/Debit Note(Original  Invoice Date)").append('|');
            writer.append("Reason for issuing Debit Note/ Credit Note").append('|');
            writer.append("Assessable value before BCD").append('|');
            writer.append("Basic Custom Duty").append('|');
            writer.append("Port Code").append('|');
            writer.append("Is advance adjustment").append('|');
            writer.append("Advance Adjustment (Invoice No)").append('|');
            writer.append("Advance Adjustment (Invoice Date)").append('|');
            writer.append("Is Amendment").append('|');
            writer.append("Amendment (Original Year)").append('|');
            writer.append("Amendment (Original Month)").append('|');
            writer.append("Amendment (Original  GSTIN of Supplier)").append('|');
            writer.append("Amendment (Original Document No)").append('|');
            writer.append("Amendment (Original Document Date)").append('|');
            writer.append("Is ISD").append('|');
            writer.append("ISD (Location Details)");
            writer.newLine();

            // String stbuf1 = ("select obj from GSTFiling obj where
            // obj.organizationId.id=?x and (obj.transactionPurpose.id=?x or
            // obj.transactionPurpose.id=?x or obj.transactionPurpose.id=?x or
            // obj.transactionPurpose.id=?x or obj.transactionPurpose.id=?x) and
            // obj.gstFilingStatus=?x and obj.agentName=?x ");
            String stbuf1 = ("select obj from GSTFiling obj where obj.organizationId.id=?1 and  obj.transactionPurpose.id in (?2,?3,?4,?5,?6,?7) and  obj.agentName=?8 and obj.presentStatus=1 and obj.transactionDate>=?9 and obj.transactionDate<=?10");
            ArrayList inparam1 = new ArrayList(10);
            inparam1.add(user.getOrganization().getId());
            inparam1.add(IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY);
            inparam1.add(IdosConstants.BUY_ON_CREDIT_PAY_LATER);
            inparam1.add(IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT);
            inparam1.add(IdosConstants.CREDIT_NOTE_VENDOR);
            inparam1.add(IdosConstants.DEBIT_NOTE_VENDOR);
            inparam1.add(IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER);
            // inparam1.add(Integer.parseInt("0"));
            inparam1.add("PWC");
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);
            gstFilingsForTranscations = genericDAO.queryWithParams(stbuf1.toString(), em, inparam1);
            List<Transaction> transactions = new ArrayList<Transaction>();
            for (GSTFiling gstFiling : gstFilingsForTranscations) {
                if (gstFiling.getTransactionId().getTransactionPurpose()
                        .getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                    if (gstFiling.getTransactionId().getTypeIdentifier() == 2) {
                        transactions.add(gstFiling.getTransactionId());
                    }
                } else {
                    transactions.add(gstFiling.getTransactionId());
                }
            }
            // String stbuf2 = ("select obj from GSTFiling obj where
            // obj.organizationId.id=?x and (obj.transactionPurpose.id=?x or
            // obj.transactionPurpose.id=?x or obj.transactionPurpose.id=?x) and
            // obj.gstFilingStatus=?x and obj.agentName=?x");
            String stbuf2 = ("select obj from GSTFiling obj where obj.organizationId.id=?1 and  (obj.transactionPurpose.id=?2 or obj.transactionPurpose.id=?3 or obj.transactionPurpose.id=?4) and  obj.agentName=?5 and obj.presentStatus=1 and obj.transactionDate>=?6 and obj.transactionDate<=?7");
            ArrayList inparam2 = new ArrayList(7);
            inparam2.add(user.getOrganization().getId());
            inparam2.add(IdosConstants.SETTLE_TRAVEL_ADVANCE);
            inparam2.add(IdosConstants.SETTLE_ADVANCE_FOR_EXPENSE);
            inparam2.add(IdosConstants.REQUEST_FOR_EXPENSE_REIMBURSEMENT);
            // inparam2.add(Integer.parseInt("0"));
            inparam2.add("PWC");
            inparam2.add(fromTransDate);
            inparam2.add(toTransDate);
            gstFilingsForClaimTransactions = genericDAO.queryWithParams(stbuf2.toString(), em, inparam2);
            List<ClaimTransaction> claimtransactions = new ArrayList<ClaimTransaction>();
            for (GSTFiling gstFiling : gstFilingsForClaimTransactions) {
                claimtransactions.add(gstFiling.getClaimTransactionId());
            }
            if (gstFilingsForTranscations != null) {
                gstFilings.addAll(gstFilingsForTranscations);
            }
            if (gstFilingsForClaimTransactions != null) {
                gstFilings.addAll(gstFilingsForClaimTransactions);
            }
            log.log(Level.INFO, "claim size=" + claimtransactions.size());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM");
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("YYYY");
            SimpleDateFormat reportdf = new SimpleDateFormat("yyyyMMdd");
            Integer lineItem = 0;

            for (Transaction transaction : transactions) {
                double totalNetAmtWithoutAdv = 0.0;
                List<TransactionItems> transactionItems = transaction.getTransactionItems();
                for (TransactionItems transactionItem : transactionItems) {
                    double netAmtWithoutAdv = transactionItem.getGrossAmount() + transactionItem.getTotalTax();
                    totalNetAmtWithoutAdv = totalNetAmtWithoutAdv + netAmtWithoutAdv;
                }
                for (TransactionItems transactionItem : transactionItems) {
                    lineItem++;
                    String branchTostate = "";
                    Specifics spec = transactionItem.getTransactionSpecifics();
                    TransactionPurpose tranPur = transaction.getTransactionPurpose();
                    writer.append(user.getOrganization().getId().toString()).append('|');
                    writer.append(user.getOrganization().getName()).append('|');
                    if (tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        branchTostate = IdosConstants.STATE_CODE_MAPPING
                                .get(transaction.getTransactionToBranch().getStateCode());
                        writer.append(branchTostate).append("|");
                        writer.append(transaction.getTransactionToBranch().getGstin()).append('|');
                    } else {
                        branchTostate = IdosConstants.STATE_CODE_MAPPING
                                .get(transaction.getTransactionBranch().getStateCode());
                        writer.append(branchTostate).append("|");
                        writer.append(transaction.getTransactionBranch().getGstin()).append('|');
                    }

                    String month = simpleDateFormat.format(transaction.getTransactionDate()).toUpperCase();
                    String year = simpleDateFormat2.format(transaction.getTransactionDate()).toUpperCase();
                    writer.append(year).append('|');
                    writer.append(month).append('|');
                    writer.append(transaction.getTransactionRefNumber()).append('|');
                    writer.append(reportdf.format(transaction.getTransactionDate())).append('|');
                    writer.append("1").append('|');
                    if (transaction.getTransactionBranch().getCountry() != null
                            && !transaction.getTransactionBranch().getCountry().equals("")) {
                        IDOSCountry country = IDOSCountry
                                .findById(transaction.getTransactionBranch().getCountry().longValue());
                        if (country != null) {
                            String currINR = country.getCurrencyCode();
                            writer.append(currINR).append('|');
                        } else {
                            writer.append("").append('|');
                        }

                    } else if (tranPur.getId() == IdosConstants.CREDIT_NOTE_VENDOR
                            || tranPur.getId() == IdosConstants.DEBIT_NOTE_VENDOR
                            || tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        writer.append("INR").append('|');
                    } else {
                        writer.append("").append('|');
                    }

                    writer.append(spec.getAccountCode().toString()).append('|');

                    if (tranPur.getId() == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT
                            || tranPur.getId() == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                            || tranPur.getId() == IdosConstants.BUY_ON_CREDIT_PAY_LATER
                            || tranPur.getId() == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) {
                        writer.append("Invoice").append('|');
                    } else if (tranPur.getId() == IdosConstants.CREDIT_NOTE_VENDOR) {
                        writer.append("Credit Note").append('|');
                    } else if (tranPur.getId() == IdosConstants.DEBIT_NOTE_VENDOR) {
                        writer.append("Debit Note").append('|');
                    } else if (tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        writer.append("invoice").append('|');
                    }
                    if (spec.getGstItemCategory() == null) {
                        writer.append("Taxable").append('|');
                    } else {
                        writer.append("Not Taxable").append('|');
                    }

                    if (transaction.getSourceGstin() != null && transaction.getDestinationGstin() != null) {

                        if (transaction.getSourceGstin().substring(0, 2)
                                .equals(transaction.getDestinationGstin().substring(0, 2))) {
                            writer.append("intra-state").append('|');
                        } else {
                            writer.append("inter-state").append('|');
                        }

                    } else {
                        writer.append("").append('|');
                    }

                    if (tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        writer.append(transaction.getTransactionBranch().getName()).append('|');
                        if (transaction.getTransactionBranch().getGstin() != null) {
                            writer.append("Registered").append('|');
                            writer.append(transaction.getTransactionBranch().getGstin()).append('|');
                        } else {
                            writer.append("Un-Registered").append('|');// Unregistered
                            writer.append("").append('|');

                        }

                        writer.append(
                                IdosConstants.STATE_CODE_MAPPING.get(transaction.getTransactionBranch().getStateCode()))
                                .append('|');
                        writer.append(transaction.getTransactionBranch().getName()).append('|');
                    } else if (transaction.getTransactionUnavailableVendorCustomer() != ""
                            && transaction.getTransactionUnavailableVendorCustomer() != null) {
                        writer.append(transaction.getTransactionUnavailableVendorCustomer()).append('|');
                        if (transaction.getWalkinCustomerType() == 1 || transaction.getWalkinCustomerType() == 2) {
                            writer.append("Registered").append('|');
                            writer.append(transaction.getDestinationGstin()).append('|');
                        } else {
                            writer.append("Un-Registered").append('|'); // Unregistered
                            writer.append("").append('|');
                        }
                        if (transaction.getDestinationGstin() != "") {
                            writer.append(IdosConstants.STATE_CODE_MAPPING
                                    .get(transaction.getDestinationGstin().substring(0, 2))).append('|');
                        } else {
                            writer.append(IdosConstants.STATE_CODE_MAPPING
                                    .get(transaction.getTransactionBranch().getGstin().substring(0, 2))).append('|');
                        }
                        writer.append(transaction.getTransactionUnavailableVendorCustomer()).append('|');
                    } else if (transaction.getTransactionVendorCustomer() != null) {
                        writer.append(transaction.getTransactionVendorCustomer().getName()).append('|');
                        if (transaction.getTransactionVendorCustomer().getIsRegistered() != null) {
                            if (transaction.getTransactionVendorCustomer().getIsRegistered() == 1) {
                                writer.append("Registered").append('|');
                                writer.append(transaction.getTransactionVendorCustomer().getGstin()).append('|');
                            } else {
                                writer.append("Un-Registered").append('|'); // Unregistered
                                writer.append("").append('|'); // blank gstin
                            }
                        }
                        if (transaction.getDestinationGstin() != "") {
                            writer.append(IdosConstants.STATE_CODE_MAPPING
                                    .get(transaction.getDestinationGstin().substring(0, 2))).append('|');
                        } else {
                            writer.append(IdosConstants.STATE_CODE_MAPPING
                                    .get(transaction.getTransactionBranch().getGstin().substring(0, 2))).append('|');
                        }
                        writer.append(transaction.getTransactionVendorCustomer().getName()).append('|');
                    } else {
                        writer.append("").append('|');
                        writer.append("").append('|');
                        writer.append("").append('|');
                        writer.append("").append('|');
                        writer.append("").append('|');
                    }
                    String invoiceNo = "";
                    String invoiceDate = "";
                    Map<String, Object> criteria = new HashMap<String, Object>();
                    criteria.put("organization.id", transaction.getTransactionBranchOrganization().getId());
                    criteria.put("transaction.id", transaction.getId());
                    criteria.put("presentStatus", 1);
                    // criterias.put("invoiceNumber", txn.getInvoiceNumber());
                    TransactionInvoice transationInvoice = genericDAO.getByCriteria(TransactionInvoice.class, criteria,
                            em);
                    if (transationInvoice != null) {
                        if (transationInvoice.getInvRefNumber() != null && transationInvoice.getInvRefNumber() != "") {
                            invoiceNo = transationInvoice.getInvRefNumber();
                            writer.append(invoiceNo).append('|');
                        } else {
                            writer.append("").append('|');
                        }
                        if (transationInvoice.getInvRefDate() != null) {
                            invoiceDate = IdosConstants.IDOSDF.format(transationInvoice.getInvRefDate());
                            writer.append(invoiceDate).append('|');
                        } else {

                            writer.append("").append('|');
                        }
                    } else {
                        writer.append("").append('|');
                        writer.append("").append('|');
                    }

                    writer.append(String.valueOf(totalNetAmtWithoutAdv)).append('|');

                    if (transaction.getTypeOfSupply() == null
                            && (transaction.getTypeOfSupply() == 1 || transaction.getTypeOfSupply() == 0)) { // Supply
                                                                                                             // applicable
                                                                                                             // for
                                                                                                             // Reverse
                                                                                                             // Charge
                        writer.append("No").append('|');
                    } else {
                        writer.append("Yes").append('|');
                    }
                    String sourceGSTIN = transaction.getSourceGstin();
                    String vendorstate = null;
                    if (sourceGSTIN != null) {
                        String sourceGSTINStateCode = sourceGSTIN.substring(0, 2);
                        vendorstate = IdosConstants.STATE_CODE_MAPPING.get(sourceGSTINStateCode);
                        writer.append(vendorstate).append('|');
                    } else {
                        writer.append("").append('|');
                    }
                    writer.append(lineItem.toString()).append('|');
                    writer.append(spec.getId().toString()).append('|');
                    if (spec.getGstTypeOfSupply() != null) {
                        if (spec.getGstTypeOfSupply().equalsIgnoreCase("GOODS")) {
                            writer.append("G").append('|');
                        } else {
                            writer.append("S").append('|');
                        }
                    } else {
                        writer.append("").append('|');
                    }
                    if (spec.getGstItemCode() != null) {
                        writer.append(spec.getGstItemCode()).append('|');
                    } else {
                        writer.append("").append('|');
                    }
                    // writer.append("").append('|');
                    if (spec.getGstDesc() != null) {
                        writer.append(spec.getGstDesc()).append('|');
                    } else {
                        writer.append("").append('|');
                    }
                    // ***********not storing item desc
                    if (spec.getExpenseUnitsMeasure() != null) {
                        writer.append(spec.getExpenseUnitsMeasure()).append('|');
                    } else {
                        writer.append("").append('|');
                    }
                    writer.append(transactionItem.getNoOfUnits().toString()).append('|');
                    writer.append(transactionItem.getPricePerUnit().toString()).append('|');
                    writer.append("").append('|');
                    writer.append(transactionItem.getPricePerUnit().toString()).append('|');

                    writer.append("0").append('|');
                    writer.append("0").append('|');
                    writer.append("0").append('|');

                    writer.append(transactionItem.getGrossAmount().toString()).append('|');

                    if (transactionItem.getTaxRate2() != null && transactionItem.getTaxRate1() != null) {

                        Double tax = transactionItem.getTaxRate2() + transactionItem.getTaxRate1();
                        writer.append(tax.toString()).append('|');
                    } else if (transactionItem.getTaxRate3() != null) {
                        Double tax = transactionItem.getTaxRate3();
                        writer.append(tax.toString()).append('|');
                    } else {
                        writer.append("").append('|');
                    }
                    if (transactionItem.getTaxName3() != null && transactionItem.getTaxName3().indexOf("IGST") != -1) {
                        writer.append(transactionItem.getTaxRate3().toString()).append('|');
                        writer.append(transactionItem.getTaxValue3().toString()).append('|');
                    } else {
                        writer.append("").append('|');
                        writer.append("").append('|');
                    }
                    if (transactionItem.getTaxName2() != null && transactionItem.getTaxName2().indexOf("CGST") != -1) {
                        writer.append(transactionItem.getTaxRate2().toString()).append('|');
                        writer.append(transactionItem.getTaxValue2().toString()).append('|');
                    } else {
                        writer.append("").append('|');
                        writer.append("").append('|');
                    }
                    if (transactionItem.getTaxName1() != null && transactionItem.getTaxName1().indexOf("SGST") != -1) {
                        writer.append(transactionItem.getTaxRate1().toString()).append('|');
                        writer.append(transactionItem.getTaxValue1().toString()).append('|');
                    } else {
                        writer.append("").append('|');
                        writer.append("").append('|');
                    }
                    if (transactionItem.getTaxName4() != null && transactionItem.getTaxName4().indexOf("CESS") != -1) {
                        writer.append(transactionItem.getTaxRate4().toString()).append('|');
                        writer.append(transactionItem.getTaxValue4().toString()).append('|');
                    } else {
                        writer.append("").append('|');
                        writer.append("").append('|');
                    }
                    writer.append("IP").append('|');
                    if (transactionItem.getTaxName3() != null && transactionItem.getTaxName3().indexOf("IGST") != -1) {
                        writer.append(transactionItem.getTaxValue3().toString()).append('|');
                    } else {
                        writer.append("").append('|');
                    }
                    if (transactionItem.getTaxName2() != null && transactionItem.getTaxName2().indexOf("CGST") != -1) {
                        writer.append(transactionItem.getTaxValue2().toString()).append('|');
                    } else {
                        writer.append("").append('|');
                    }
                    if (transactionItem.getTaxName1() != null && transactionItem.getTaxName1().indexOf("SGST") != -1) {
                        writer.append(transactionItem.getTaxValue1().toString()).append('|');
                    } else {
                        writer.append("").append('|');
                    }
                    if (transactionItem.getTaxName4() != null && transactionItem.getTaxName4().indexOf("CESS") != -1) {
                        writer.append(transactionItem.getTaxValue4().toString()).append('|');
                    } else {
                        writer.append("").append('|');
                    }
                    writer.append("Exclusively For Taxable/Zero Rated Supplies").append('|');
                    if (tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        writer.append(vendorstate).append('|');
                    } else if (null != transaction.getTransactionVendorCustomer()) {
                        writer.append(transaction.getTransactionVendorCustomer().getCountryState()).append('|');
                    } else {
                        writer.append("").append('|');
                    }
                    writer.append(branchTostate).append('|');
                    writer.append("").append('|');
                    writer.append("").append('|');
                    writer.append("").append('|');
                    writer.append("").append('|');
                    if (tranPur.getId() == IdosConstants.CREDIT_NOTE_VENDOR
                            || tranPur.getId() == IdosConstants.DEBIT_NOTE_VENDOR) {

                        if (transaction.getLinkedTxnRef() != null) {
                            List<Transaction> findByTxnReference = Transaction.findByTxnReference(em,
                                    user.getOrganization().getId(), transaction.getLinkedTxnRef());
                            if (findByTxnReference != null && !findByTxnReference.isEmpty()) {
                                Transaction transactionRef = findByTxnReference.get(0);
                                String invoiceNo1 = "";
                                String invoiceDate1 = "";
                                Map<String, Object> criteria1 = new HashMap<String, Object>();
                                criteria1.put("organization.id",
                                        transaction.getTransactionBranchOrganization().getId());
                                criteria1.put("transaction.id", transactionRef.getId());
                                criteria1.put("presentStatus", 1);
                                // criterias.put("invoiceNumber", txn.getInvoiceNumber());
                                TransactionInvoice transationInvoice1 = genericDAO
                                        .getByCriteria(TransactionInvoice.class, criteria1, em);
                                if (transationInvoice1 != null) {
                                    if (transationInvoice1.getInvRefNumber() != null
                                            && transationInvoice1.getInvRefNumber() != "") {
                                        invoiceNo1 = transationInvoice1.getInvRefNumber();
                                        writer.append(invoiceNo1).append('|');
                                    } else {
                                        writer.append("").append('|');
                                    }
                                    if (transationInvoice1.getInvRefDate() != null) {
                                        invoiceDate1 = IdosConstants.IDOSDF.format(transationInvoice1.getInvRefDate());
                                        writer.append(invoiceDate1).append('|');
                                    } else {

                                        writer.append("").append('|');
                                    }
                                } else {
                                    writer.append("").append('|');
                                    writer.append("").append('|');
                                }

                                /*
                                 * writer.append(transactionRef.getInvoiceNumber()).append('|');
                                 * writer.append(reportdf.format(transactionRef.getTransactionDate())).append('|
                                 * ');
                                 */
                            } else {
                                writer.append("").append('|');
                                writer.append("").append('|');
                            }
                        } else {
                            writer.append("").append('|');
                            writer.append("").append('|');
                        }
                    } else {
                        writer.append("").append('|');
                        writer.append("").append('|');
                    }
                    writer.append("").append('|');

                    // *********shipping bill no
                    if (transaction.getTypeOfSupply() != null
                            && (transaction.getTypeOfSupply() != 1 && transaction.getTypeOfSupply() != 0)) {
                        writer.append(transactionItem.getGrossAmount().toString()).append('|');
                    } else {
                        writer.append("").append('|');
                    } // *********shipping bill no
                    if (transaction.getTypeOfSupply() != null
                            && (transaction.getTypeOfSupply() != 1 && transactionItem.getTaxValue7() != null)) {
                        writer.append(transactionItem.getTaxValue7().toString()).append('|');
                    } else {
                        writer.append("").append('|');
                    }
                    if (transationInvoice.getPortCode() != null) {
                        writer.append(transationInvoice.getPortCode()).append('|');
                    } else {
                        writer.append("").append('|');
                    }

                    writer.append("No").append('|'); // No removed
                    writer.append("").append('|');
                    writer.append("").append('|');
                    writer.append("").append('|');
                    writer.append("").append('|');
                    writer.append("").append('|');
                    writer.append("").append('|');
                    writer.append("").append('|');
                    writer.append("").append('|');
                    writer.append("").append('|');
                    writer.append("").append('|');

                    writer.newLine();
                }
                lineItem = 0;
            }
            if (claimtransactions.size() > 0) {
                for (ClaimTransaction transaction : claimtransactions) {

                    if (transaction != null) {

                        String stbuf3 = ("select obj from ClaimItemDetails obj where obj.organization.id=?1 and obj.branch.id=?2 and obj.transaction.id=?3 and obj.itemCategory in(?4,?5,?6,?7,?8,?9) and obj.presentStatus=1");
                        ArrayList inparam3 = new ArrayList(9);
                        inparam3.add(user.getOrganization().getId());
                        inparam3.add(user.getBranch().getId());
                        inparam3.add(transaction.getId());
                        inparam3.add(IdosConstants.TRAVEL_EXPENSES);
                        inparam3.add(IdosConstants.BOARDING_LODGING);
                        inparam3.add(IdosConstants.OTHER_EXPENSES);
                        inparam3.add(IdosConstants.FIXED_PER_DIAM);
                        inparam3.add(IdosConstants.INCURRED_EXPENCES);
                        inparam3.add(IdosConstants.REIMBURSEMENT_EXPENSES);
                        List<ClaimItemDetails> claimItemDetailsList = genericDAO.queryWithParams(stbuf3.toString(), em,
                                inparam3);
                        // List<ClaimItemDetails> claimItemDetailsList =
                        // genericDAO.findByCriteria(ClaimItemDetails.class, criterias1, em);
                        if (claimItemDetailsList != null && !claimItemDetailsList.isEmpty()) {
                            for (ClaimItemDetails claimItemDetails : claimItemDetailsList) {
                                lineItem++;
                                Specifics spec = transaction.getAdvanceForExpenseItems();
                                TransactionPurpose tranPur = transaction.getTransactionPurpose();
                                writer.append(user.getOrganization().getId().toString()).append('|');
                                writer.append(user.getOrganization().getName()).append('|');
                                String branchstate = IdosConstants.STATE_CODE_MAPPING
                                        .get(transaction.getTransactionBranch().getStateCode());
                                writer.append(branchstate).append("|");
                                writer.append(transaction.getTransactionBranch().getGstin()).append('|');
                                String month = simpleDateFormat.format(transaction.getTransactionDate()).toUpperCase();
                                String year = simpleDateFormat2.format(transaction.getTransactionDate()).toUpperCase();
                                writer.append(year).append('|');
                                writer.append(month).append('|');
                                writer.append(transaction.getTransactionRefNumber()).append('|');
                                writer.append(reportdf.format(transaction.getTransactionDate())).append('|');
                                writer.append("1").append('|');
                                writer.append("INR").append('|');
                                writer.append("").append('|');
                                writer.append("invoice").append('|');
                                StringBuilder sbuffer = new StringBuilder("");
                                if (claimItemDetails.getClaimSettlementId() != null) {
                                    sbuffer = new StringBuilder("select obj from ClaimsSettlement obj where obj.id='"
                                            + claimItemDetails.getClaimSettlementId() + "' and obj.presentStatus=1");
                                    List<ClaimsSettlement> travelExpenses = genericDAO
                                            .executeSimpleQuery(sbuffer.toString(), em);
                                    if (travelExpenses.get(0).getItemTax() != null) {
                                        writer.append("Taxable").append('|');
                                    } else {
                                        writer.append("Not Taxable").append('|');
                                    }
                                } else if (transaction.getClaimsNetTax() != null) {
                                    writer.append("Taxable").append('|');
                                } else {
                                    writer.append("Not Taxable").append('|');
                                }
                                if (transaction.getTransactionBranch() != null
                                        || claimItemDetails.getVendorName() != null) {
                                    if (transaction.getTransactionBranch().getStateCode() != null) {
                                        if (IdosConstants.STATE_CODE_MAPPING
                                                .get(transaction.getTransactionBranch().getStateCode())
                                                .equals(claimItemDetails.getVendorState())) {
                                            writer.append("inter-state").append('|');
                                        } else {
                                            writer.append("intra-state").append('|');
                                        }
                                    } else {
                                        writer.append("").append('|');
                                    }
                                } else {
                                    writer.append("").append('|');
                                }
                                // supplier code
                                if (claimItemDetails.getVendorName() != null) {
                                    writer.append(claimItemDetails.getVendorName()).append('|');
                                } else {
                                    writer.append("").append('|');
                                }

                                if (claimItemDetails.getIsRegistered() != null) {
                                    if (claimItemDetails.getIsRegistered() == 1) {
                                        writer.append("Registered").append('|');
                                        if (claimItemDetails.getVendorGstin() != null) {
                                            writer.append(claimItemDetails.getVendorGstin()).append('|');
                                        } else {
                                            writer.append("").append('|');
                                        }
                                    } else {
                                        writer.append("Un-Registered").append('|');// Unregistered
                                        writer.append("").append('|');
                                    }
                                } else {
                                    writer.append("Un-Registered").append('|');// Unregistered
                                    writer.append("").append('|');
                                }
                                if (claimItemDetails.getVendorState() != null) {
                                    writer.append(claimItemDetails.getVendorState()).append('|');
                                } else {
                                    writer.append("").append('|');
                                }
                                if (claimItemDetails.getVendorName() != null) {
                                    writer.append(claimItemDetails.getVendorName()).append('|');
                                } else {
                                    writer.append("").append('|');
                                }

                                if (claimItemDetails.getInvoiceBillRefNo() != null) {
                                    writer.append(claimItemDetails.getInvoiceBillRefNo()).append('|');
                                } else {
                                    writer.append("").append('|');
                                }
                                if (claimItemDetails.getInvoiceBillRefDate() != null) {
                                    writer.append(reportdf.format(claimItemDetails.getInvoiceBillRefDate()))
                                            .append('|');
                                } else {
                                    writer.append("").append('|');
                                }
                                writer.append(claimItemDetails.getTransaction().getClaimsNetSettlement().toString())
                                        .append('|');

                                writer.append("No").append('|');
                                if (claimItemDetails.getVendorState() != null) {
                                    writer.append(claimItemDetails.getVendorState()).append('|');
                                } else {
                                    writer.append("").append('|');
                                }
                                writer.append(lineItem.toString()).append('|');
                                writer.append("").append('|');
                                writer.append("").append('|');
                                if (claimItemDetails.getHsnOrSacCode() != null) {
                                    writer.append(claimItemDetails.getHsnOrSacCode()).append('|');
                                } else {
                                    writer.append("").append('|');
                                }
                                if (claimItemDetails.getProductServiceDesc() != null) {
                                    writer.append(claimItemDetails.getProductServiceDesc()).append('|');
                                } else {
                                    writer.append("").append('|');
                                }

                                if (claimItemDetails.getUqc() != null) {
                                    writer.append(claimItemDetails.getUqc()).append('|');
                                } else {
                                    writer.append("").append('|');
                                }
                                writer.append(claimItemDetails.getQuantity().toString()).append('|');
                                writer.append(claimItemDetails.getRate().toString()).append('|');
                                writer.append("").append('|');
                                writer.append(claimItemDetails.getRate().toString()).append('|');
                                writer.append("").append('|');
                                writer.append("").append('|');
                                writer.append("").append('|');

                                writer.append(claimItemDetails.getGrossAmt().toString()).append('|');

                                if (claimItemDetails.getSgstRate() != null || claimItemDetails.getCgstRate() != null
                                        || claimItemDetails.getIgstAmt() != null) {
                                    if (claimItemDetails.getSgstRate() != null
                                            || claimItemDetails.getCgstRate() != null) {
                                        Double amount = claimItemDetails.getSgstRate() + claimItemDetails.getCgstRate();
                                        writer.append(amount.toString()).append('|');
                                    } else {
                                        writer.append(claimItemDetails.getIgstRate().toString()).append('|');
                                    }
                                } else {
                                    writer.append("").append('|');
                                }
                                if (claimItemDetails.getIgstId() != null && claimItemDetails.getIgstId() != -1) {
                                    writer.append(claimItemDetails.getIgstRate().toString()).append('|');
                                    writer.append(claimItemDetails.getIgstAmt().toString()).append('|');
                                } else {
                                    writer.append("").append('|');
                                    writer.append("").append('|');
                                }

                                if (claimItemDetails.getCgstId() != null && claimItemDetails.getCgstId() != -1) {
                                    writer.append(claimItemDetails.getCgstRate().toString()).append('|');
                                    writer.append(claimItemDetails.getCgstAmt().toString()).append('|');
                                } else {
                                    writer.append("").append('|');
                                    writer.append("").append('|');
                                }
                                if (claimItemDetails.getSgstId() != null && claimItemDetails.getSgstId() != -1) {
                                    writer.append(claimItemDetails.getSgstRate().toString()).append('|');
                                    writer.append(claimItemDetails.getSgstAmt().toString()).append('|');
                                } else {
                                    writer.append("").append('|');
                                    writer.append("").append('|');
                                }
                                if (claimItemDetails.getCessId() != null && claimItemDetails.getCessId() != -1) {
                                    writer.append(claimItemDetails.getCessRate().toString()).append('|');
                                    writer.append(claimItemDetails.getCessAmt().toString()).append('|');
                                } else {
                                    writer.append("").append('|');
                                    writer.append("").append('|');
                                }
                                writer.append("IP").append('|');
                                if (claimItemDetails.getIgstId() != null && claimItemDetails.getIgstId() != -1) {
                                    writer.append(claimItemDetails.getIgstAmt().toString()).append('|');
                                } else {
                                    writer.append("").append('|');
                                }
                                if (claimItemDetails.getCgstId() != null && claimItemDetails.getCgstId() != -1) {
                                    writer.append(claimItemDetails.getCgstAmt().toString()).append('|');
                                } else {
                                    writer.append("").append('|');
                                }
                                if (claimItemDetails.getSgstId() != null && claimItemDetails.getSgstId() != -1) {
                                    writer.append(claimItemDetails.getSgstAmt().toString()).append('|');
                                } else {
                                    writer.append("").append('|');
                                }
                                if (claimItemDetails.getCessId() != null && claimItemDetails.getCessId() != -1) {
                                    writer.append(claimItemDetails.getCessAmt().toString()).append('|');
                                } else {
                                    writer.append("").append('|');
                                }

                                writer.append("").append('|');
                                writer.append("").append('|');
                                writer.append("").append('|');
                                writer.append("").append('|');
                                writer.append("").append('|');
                                writer.append("").append('|');
                                writer.append("").append('|'); // *********shipping bill no
                                writer.append("").append('|'); // *********shipping bill no
                                writer.append("").append('|');
                                writer.append("").append('|');
                                writer.append("").append('|'); // No removed
                                writer.append("").append('|');
                                writer.append("").append('|');
                                writer.append("").append('|');
                                writer.append("").append('|');
                                writer.append("").append('|');
                                writer.append("").append('|');
                                writer.append("").append('|');
                                writer.append("").append('|');
                                writer.append("").append('|');
                                writer.append("").append('|');
                                writer.append("").append('|');
                                writer.append("").append('|');

                                writer.newLine();
                            }

                            lineItem = 0;
                        }
                    }
                }

            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error", e);

        } finally {

            try {

                if (writer != null)
                    writer.close();

                if (fw != null)
                    fw.close();

            } catch (IOException ex) {

                log.log(Level.SEVERE, "Error", ex);

            }

        }

        try {
            log.log(Level.INFO, "gst filing=" + gstFilings.size());
            if (gstFilings.size() != 0) {
                for (GSTFiling gstFiling : gstFilings) {
                    gstFiling.setGstFilingStatus(1);
                    log.log(Level.INFO, " inside set filing status");
                    log.log(Level.INFO, "get the status=" + gstFiling.getGstFilingStatus());
                    genericDAO.saveOrUpdate(gstFiling, user, em);
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            log.log(Level.SEVERE, "not able to upload", e);
            log.log(Level.INFO, "inside catch");
        }
        return file;

    }

    @Override
    public File createOrgTransactionSellAndRecieveAdvanceData(Users user, EntityManager em, String path,
            String fileName, Date fromTransDate, Date toTransDate) {

        FileWriter fw = null;
        BufferedWriter writer = null;
        File file = null;

        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        path = path + fileName;
        file = new File(path);
        List<GSTFiling> gstFilings = null;
        try {

            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            writer.append("Comapny Code").append('|');
            writer.append("Company Name").append('|');
            writer.append("State").append('|');
            writer.append("GSTIN").append('|');
            writer.append("Year").append('|');
            writer.append("Month").append('|');
            writer.append("Accounting Document No").append('|');
            writer.append("Accounting Document Date").append('|');
            writer.append("Transaction Count").append('|');
            writer.append("Currency").append('|');
            writer.append("GL Account").append('|');
            writer.append("Document Type").append('|');
            writer.append("Taxability").append('|');
            writer.append("Nature of Exemption").append('|');
            writer.append("Supply Type").append('|');
            writer.append("Customer Code").append('|');
            writer.append("Nature Of Recipient").append('|');
            writer.append("GSTIN Of Recipient").append('|');
            writer.append("Recipient State").append('|');
            writer.append("Name Of The Recipient").append('|');
            writer.append("Invoice/Dr Note/Cr Note/Receipt Voucher/Refund Voucher(No)").append('|');
            writer.append("Invoice/Dr Note/Cr Note/Receipt Voucher/Refund Voucher(Date)").append('|');
            writer.append("Invoice/Dr Note/Cr Note/Receipt Voucher/Refund Voucher(Value)").append('|');
            writer.append("Supply attract reverse charge").append('|');
            writer.append("POS (State)").append('|');
            writer.append("GSTIN of e-commerce portal").append('|');
            writer.append("Line Item").append('|');
            writer.append("Item Code").append('|');
            writer.append("Category").append('|');
            writer.append("HSN/SAC").append('|');
            writer.append("Product/Service Description").append('|');
            writer.append("UQC").append('|');
            writer.append("Quantity").append('|');
            writer.append("Sale price (Before discount)").append('|');
            writer.append("Discount").append('|');
            writer.append("Net sale price (after discount)").append('|');
            writer.append("VAT").append('|');
            writer.append("Central Excise").append('|');
            writer.append("State Excise").append('|');
            writer.append("Taxable value").append('|');
            writer.append("Total GST Rate").append('|');
            writer.append("IGST (Rate)").append('|');
            writer.append("IGST (Amt)").append('|');
            writer.append("CGST (Rate)").append('|');
            writer.append("CGST (Amt)").append('|');
            writer.append("SGST/UTGST (Rate)").append('|');
            writer.append("SGST/UTGST (Amt)").append('|');
            writer.append("Cess (Rate)").append('|');
            writer.append("Cess (Amount)").append('|');
            writer.append("Ship From (State)").append('|');
            writer.append("Ship To (State)").append('|');
            writer.append("Way Bill No").append('|');
            writer.append("Transporter name").append('|');
            writer.append("Lorry Receipt number").append('|');
            writer.append("Lorry Receipt date").append('|');
            writer.append("Credit Note/Debit Note/Refund Voucher(Original Document No)").append('|');
            writer.append("Credit Note/Debit Note/Refund Voucher(Original Document Date)").append('|');
            writer.append("Reason for issuing Debit Note/ Credit Note/ Refund Voucher").append('|');
            writer.append("Shipping Bill/ Bill of Export (No)").append('|');
            writer.append("Shipping Bill/ Bill of Export (Date)").append('|');
            writer.append("Port Code").append('|');
            writer.append("Export Duty (If any)").append('|');
            writer.append("Is advance adjustment").append('|');
            writer.append("Advance Adjustment (Invoice No)").append('|');
            writer.append("Advance Adjustment (Invoice Date)").append('|');
            writer.append("Is Amendment").append('|');
            writer.append("Amendment (Original Year)").append('|');
            writer.append("Amendment (Original Month)").append('|');
            writer.append("Amendment (Original Recipients GSTIN/ UIN)").append('|');
            writer.append("Amendment (Original Document No)").append('|');
            writer.append("Amendment (Original Document Date)");
            writer.newLine();

            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            List<Branch> branchList = genericDAO.findByCriteria(Branch.class, criterias, em);

            // String stbuf1 = ("select obj from GSTFiling obj where
            // obj.organizationId.id=?x and obj.transactionPurpose.id
            // in(?x,?x,?x,?x,?x,?x,?x,?x) and obj.gstFilingStatus=?x and
            // obj.agentName=?x");
            // original design based on date range select transactions
            String stbuf1 = ("select obj from GSTFiling obj where obj.organizationId.id=?1 and obj.transactionPurpose.id in(?2,?3,?4,?5,?6,?7,?8,?9) and  obj.agentName=?10 and obj.presentStatus=1 and obj.transactionDate>=?11 and obj.transactionDate<=?12");
            ArrayList inparam1 = new ArrayList(10);
            inparam1.add(user.getOrganization().getId());
            inparam1.add(IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW);
            inparam1.add(IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER);
            inparam1.add(IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER);
            inparam1.add(IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER);
            inparam1.add(IdosConstants.CREDIT_NOTE_CUSTOMER);
            inparam1.add(IdosConstants.DEBIT_NOTE_CUSTOMER);
            inparam1.add(IdosConstants.REFUND_ADVANCE_RECEIVED);
            inparam1.add(IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER);
            // inparam1.add(Integer.parseInt("0"));
            inparam1.add("PWC");
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);
            gstFilings = genericDAO.queryWithParams(stbuf1.toString(), em, inparam1);
            List<Transaction> transactions = new ArrayList<Transaction>();
            for (GSTFiling gstFiling : gstFilings) {
                if (gstFiling.getTransactionId().getTransactionPurpose()
                        .getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                    if (gstFiling.getTransactionId().getTypeIdentifier() == 1) {
                        transactions.add(gstFiling.getTransactionId());
                    }
                } else {
                    transactions.add(gstFiling.getTransactionId());
                }
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM");
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("YYYY");
            SimpleDateFormat reportdf = new SimpleDateFormat("yyyyMMdd");
            Integer lineItem = 0;

            for (Transaction transaction : transactions) {
                double totalNetAmtWithoutAdv = 0.0;
                List<TransactionItems> transactionItems = transaction.getTransactionItems();
                for (TransactionItems transactionItem : transactionItems) {
                    double netAmtWithoutAdv = transactionItem.getGrossAmount() + transactionItem.getTotalTax();
                    totalNetAmtWithoutAdv = totalNetAmtWithoutAdv + netAmtWithoutAdv;
                }
                for (TransactionItems transactionItem : transactionItems) {
                    lineItem++;
                    Specifics spec = transactionItem.getTransactionSpecifics();
                    TransactionPurpose tranPur = transaction.getTransactionPurpose();
                    writer.append(user.getOrganization().getId().toString()).append('|');
                    writer.append(user.getOrganization().getName()).append('|');
                    // if(tranPur.getTransactionPurpose().getId==IdosConstants.)
                    String branchstate = IdosConstants.STATE_CODE_MAPPING
                            .get(transaction.getTransactionBranch().getStateCode());
                    writer.append(branchstate).append("|");
                    writer.append(transaction.getTransactionBranch().getGstin()).append('|');
                    String month = simpleDateFormat.format(transaction.getTransactionDate()).toUpperCase();
                    String year = simpleDateFormat2.format(transaction.getTransactionDate()).toUpperCase();
                    writer.append(year).append('|');
                    writer.append(month).append('|');

                    writer.append(transaction.getTransactionRefNumber()).append('|');

                    if (transaction.getTransactionDate() != null) {
                        writer.append(reportdf.format(transaction.getTransactionDate())).append('|');
                    } else {
                        writer.append("").append('|');
                    }
                    writer.append("1").append('|');
                    Integer currency = transaction.getTransactionBranch().getCountry();
                    if (currency != null && !currency.equals("")) {
                        IDOSCountry country = IDOSCountry.findById(currency.longValue());
                        if (country != null) {
                            String currINR = country.getCurrencyCode();
                            writer.append(currINR).append('|');
                        } else {
                            writer.append("").append('|');
                        }

                    } else if (tranPur.getId() == IdosConstants.CREDIT_NOTE_CUSTOMER
                            || tranPur.getId() == IdosConstants.DEBIT_NOTE_CUSTOMER
                            || tranPur.getId() == IdosConstants.REFUND_ADVANCE_RECEIVED
                            || tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        writer.append("INR").append('|');
                    } else {
                        writer.append("").append('|');
                    }
                    writer.append(spec.getAccountCode().toString()).append('|');
                    if (tranPur.getId() == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                            || tranPur.getId() == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER) {
                        writer.append("Invoice").append('|');
                    } else if (tranPur.getId() == IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER
                            || tranPur.getId() == IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER) {
                        writer.append("Advance Receipt Voucher").append('|');
                    } else if (tranPur.getId() == IdosConstants.CREDIT_NOTE_CUSTOMER) {
                        writer.append("Credit Note").append('|');
                    } else if (tranPur.getId() == IdosConstants.DEBIT_NOTE_CUSTOMER) {
                        writer.append("Debit Note").append('|');
                    } else if (tranPur.getId() == IdosConstants.REFUND_ADVANCE_RECEIVED) {
                        writer.append("Refund Voucher").append('|');
                    } else if (tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        writer.append("invoice").append('|');
                    }
                    if (transaction.getWithWithoutTax() != null) {
                        if (transaction.getWithWithoutTax() == 2) {
                            writer.append("Not Taxable").append('|');
                        } else if (transaction.getWithWithoutTax() == 1) {
                            writer.append("Taxable").append('|');
                        }
                    } else if (spec.getGstItemCategory() == null) {
                        writer.append("Taxable").append('|');
                    } else {
                        writer.append("Not Taxable").append('|');
                    }
                    writer.append("").append('|');

                    if (tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        if (transaction.getTransactionBranch() != null
                                && transaction.getTransactionToBranch() != null) {
                            if (transaction.getTransactionBranch().getStateCode() != null
                                    && transaction.getTransactionToBranch() != null) {
                                if (transaction.getTransactionBranch().getStateCode()
                                        .equals(transaction.getTransactionToBranch().getStateCode())) {
                                    writer.append("intra-state").append('|');
                                } else {
                                    writer.append("inter-state").append('|');
                                }
                            } else {
                                writer.append("").append('|');
                            }
                        } else {
                            writer.append("").append('|');
                        }
                    } else if (transaction.getSourceGstin() != null && transaction.getDestinationGstin() != null
                            && transaction.getSourceGstin() != "" && transaction.getDestinationGstin() != "") {

                        if (transaction.getSourceGstin().substring(0, 2)
                                .equals(transaction.getDestinationGstin().substring(0, 2))) {
                            writer.append("intra-state").append('|');
                        } else {
                            writer.append("inter-state").append('|');
                        }

                    } else {
                        writer.append("").append('|');
                    }
                    if (tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        writer.append(transaction.getTransactionToBranch().getName()).append('|');
                        if (transaction.getTransactionToBranch().getGstin() != null) {
                            writer.append("Registered").append('|');
                            writer.append(transaction.getTransactionToBranch().getGstin()).append('|');
                        } else {
                            writer.append("Un-Registered").append('|');// Unregistered
                            writer.append("").append('|');

                        }
                        writer.append(IdosConstants.STATE_CODE_MAPPING
                                .get(transaction.getTransactionToBranch().getStateCode())).append('|');
                        writer.append(transaction.getTransactionToBranch().getName()).append('|');
                    } else if (transaction.getTransactionVendorCustomer() != null) {
                        writer.append(transaction.getTransactionVendorCustomer().getName()).append('|');
                        if (transaction.getTransactionVendorCustomer().getIsRegistered() != null) {
                            if (transaction.getTransactionVendorCustomer().getIsRegistered() == 1) {
                                writer.append("Registered").append('|');
                                writer.append(transaction.getTransactionVendorCustomer().getGstin()).append('|');
                            } else {
                                writer.append("Un-Registered").append('|'); // Unregistered
                                writer.append("").append('|'); // blank gstin
                            }
                        }
                        if (transaction.getDestinationGstin() != "") {
                            writer.append(IdosConstants.STATE_CODE_MAPPING
                                    .get(transaction.getDestinationGstin().substring(0, 2))).append('|');
                        } else {
                            writer.append(IdosConstants.STATE_CODE_MAPPING
                                    .get(transaction.getTransactionBranch().getGstin().substring(0, 2))).append('|');
                        }
                        writer.append(transaction.getTransactionVendorCustomer().getName()).append('|');
                    } else {
                        writer.append("").append('|'); // Unregistered
                        writer.append("").append('|');
                        writer.append("").append('|');
                        writer.append("").append('|');
                        writer.append("").append('|');
                    }
                    if (transaction.getInvoiceNumber() != null) {
                        writer.append(transaction.getInvoiceNumber()).append('|');
                    } else {
                        writer.append("").append('|');
                    }
                    if (transaction.getTransactionDate() != null) {
                        writer.append(reportdf.format(transaction.getTransactionDate())).append('|');
                    } else {
                        writer.append("").append('|');
                    }
                    if (tranPur.getId().equals(IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER)) {
                        writer.append(IdosConstants.decimalFormat.format(transaction.getGrossAmount())).append('|');
                    } else {
                        writer.append(IdosConstants.decimalFormat.format(totalNetAmtWithoutAdv)).append('|');
                    }
                    if (transaction.getTypeOfSupply() != null && transaction.getTypeOfSupply() == 2) { // Supply
                                                                                                       // applicable for
                                                                                                       // Reverse Charge
                        writer.append("Yes").append('|');
                    } else {
                        writer.append("No").append('|');
                    }
                    String destGSTIN = transaction.getDestinationGstin();
                    String vendorstate = null;
                    if (destGSTIN != null && destGSTIN != "") {
                        String destGSTINStateCode = destGSTIN.substring(0, 2);
                        vendorstate = IdosConstants.STATE_CODE_MAPPING.get(destGSTINStateCode);
                        writer.append(vendorstate).append('|');
                    } else {
                        writer.append("").append('|');
                    }
                    writer.append("").append('|'); // *************GSTIN of ecommerce
                    writer.append(lineItem.toString()).append('|');
                    writer.append(spec.getId().toString()).append('|');
                    if (spec.getGstTypeOfSupply() != null) {
                        if (spec.getGstTypeOfSupply().equalsIgnoreCase("GOODS")) {
                            writer.append("G").append('|');
                        } else {
                            writer.append("S").append('|');
                        }
                    } else {
                        writer.append("").append('|');
                    }
                    if (spec.getGstItemCode() != null) {
                        writer.append(spec.getGstItemCode()).append('|');
                    } else {
                        writer.append("").append('|');
                    }
                    if (spec.getGstDesc() == null) {
                        writer.append("").append('|'); // ***********not storing item desc
                    } else {
                        writer.append(spec.getGstDesc()).append('|');
                    }
                    if (spec.getIncomeUnitsMeasure() != null) {
                        writer.append(spec.getIncomeUnitsMeasure()).append('|');
                    } else {
                        writer.append("").append('|');
                    }
                    if (tranPur.getTransactionPurpose().equals("Receive advance from customer")) {
                        writer.append("1").append('|');
                        writer.append(transactionItem.getGrossAmount().toString()).append('|');
                        writer.append("").append('|');
                        writer.append(transactionItem.getGrossAmount().toString()).append('|');
                    } else if (tranPur.getTransactionPurpose().equals("Refund Advance Received")) {
                        writer.append("").append('|');
                        writer.append("").append('|');
                        writer.append("").append('|');
                        writer.append("").append('|');

                    } else {
                        writer.append(transactionItem.getNoOfUnits().toString()).append('|');
                        writer.append(transactionItem.getPricePerUnit().toString()).append('|');
                        Double discountPerUnit = transactionItem.getDiscountAmount() / transactionItem.getNoOfUnits();
                        writer.append(String.valueOf(discountPerUnit)).append('|');
                        Double amount = transactionItem.getPricePerUnit() - discountPerUnit;
                        writer.append(amount.toString()).append('|');
                    }

                    writer.append("0").append('|');
                    writer.append("0").append('|');
                    writer.append("0").append('|');
                    writer.append(transactionItem.getGrossAmount().toString()).append('|');
                    if (transactionItem.getTaxName1() != null || transactionItem.getTaxName2() != null
                            || transactionItem.getTaxName3() != null) {
                        if (spec.getGstTaxRate() != null) {
                            writer.append(spec.getGstTaxRate().toString()).append('|');
                        } else {
                            writer.append("").append('|');
                        }
                    } else {
                        writer.append("").append('|');
                    }
                    if (transactionItem.getTaxName3() != null && transactionItem.getTaxName3().indexOf("IGST") != -1) {
                        writer.append(transactionItem.getTaxRate3().toString()).append('|');
                        writer.append(transactionItem.getTaxValue3().toString()).append('|');
                    } else {
                        writer.append("").append('|');
                        writer.append("").append('|');
                    }
                    if (transactionItem.getTaxName2() != null && transactionItem.getTaxName2().indexOf("CGST") != -1) {
                        writer.append(transactionItem.getTaxRate2().toString()).append('|');
                        writer.append(transactionItem.getTaxValue2().toString()).append('|');
                    } else {
                        writer.append("").append('|');
                        writer.append("").append('|');
                    }
                    if (transactionItem.getTaxName1() != null && transactionItem.getTaxName1().indexOf("SGST") != -1) {
                        writer.append(transactionItem.getTaxRate1().toString()).append('|');
                        writer.append(transactionItem.getTaxValue1().toString()).append('|');
                    } else {
                        writer.append("").append('|');
                        writer.append("").append('|');
                    }
                    if (transactionItem.getTaxName4() != null && transactionItem.getTaxName4().indexOf("CESS") != -1) {
                        writer.append(transactionItem.getTaxRate4().toString()).append('|');
                        writer.append(transactionItem.getTaxValue4().toString()).append('|');
                    } else {
                        writer.append("").append('|');
                        writer.append("").append('|');
                    }
                    if (tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        writer.append(vendorstate).append('|');
                        writer.append(branchstate).append('|');
                    } else {
                        if (branchstate != null) {
                            writer.append(branchstate).append('|');
                        } else {
                            writer.append("").append('|');
                        }
                        if (vendorstate != null) {
                            writer.append(vendorstate).append('|');
                        } else {
                            writer.append("").append('|');
                        }
                    }
                    writer.append("").append('|');
                    writer.append("").append('|');
                    writer.append("").append('|');
                    writer.append("").append('|');
                    if (tranPur.getTransactionPurpose().equals("Refund Advance Received")
                            || tranPur.getId().equals(IdosConstants.DEBIT_NOTE_CUSTOMER)
                            || tranPur.getId().equals(IdosConstants.CREDIT_NOTE_CUSTOMER)) {

                        if (transaction.getLinkedTxnRef() != null) {
                            List<Transaction> findByTxnReference = Transaction.findByTxnReference(em,
                                    user.getOrganization().getId(), transaction.getLinkedTxnRef());
                            if (findByTxnReference != null && !findByTxnReference.isEmpty()) {
                                Transaction transactionRef = findByTxnReference.get(0);
                                writer.append(transactionRef.getInvoiceNumber()).append('|');
                                writer.append(reportdf.format(transactionRef.getTransactionDate())).append('|');
                            }
                        } else {
                            writer.append("").append('|');
                            writer.append("").append('|');
                        }
                    } else {
                        writer.append("").append('|');
                        writer.append("").append('|');
                    }
                    writer.append("").append('|');
                    writer.append("").append('|'); // *********shipping bill no
                    writer.append("").append('|'); // *********shipping bill no

                    writer.append("").append('|');
                    writer.append("").append('|');
                    if (tranPur.getId().equals(IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW)
                            || tranPur.getId().equals(IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER)) {
                        String advanceVoucherNumber = "";
                        Date voucherDate = null;
                        List<AdvanceAdjustmentDetail> advanceAdjustmentDetails = AdvanceAdjustmentDetail
                                .findListByTxn(entityManager, transaction.getId());
                        if (advanceAdjustmentDetails != null && advanceAdjustmentDetails.size() != 0) {
                            for (AdvanceAdjustmentDetail advanceAdjustmentDetail : advanceAdjustmentDetails) {
                                if (advanceAdjustmentDetail.getAdjustedAmount() != 0) {
                                    advanceVoucherNumber = advanceAdjustmentDetail.getAdvTransaction()
                                            .getInvoiceNumber();
                                    voucherDate = advanceAdjustmentDetail.getAdvTransaction().getTransactionDate();
                                }
                            }
                            writer.append("Yes").append('|');
                            writer.append(advanceVoucherNumber).append('|');
                            if (voucherDate != null) {
                                writer.append(reportdf.format(voucherDate)).append('|');
                            } else {
                                writer.append("").append('|');
                            }
                        } else {
                            writer.append("").append('|');
                            writer.append("").append('|');
                            writer.append("").append('|');
                        }
                    } else {
                        writer.append("").append('|');
                        writer.append("").append('|');
                        writer.append("").append('|');
                    }
                    writer.append("").append('|');
                    writer.append("").append('|');
                    writer.append("").append('|');
                    writer.append("").append('|');
                    writer.append("").append('|');
                    writer.append("").append('|');

                    writer.newLine();
                }
                lineItem = 0;
            }

        } catch (IOException e) {
            log.log(Level.SEVERE, "Error", e);

        } finally {

            try {

                if (writer != null)
                    writer.close();

                if (fw != null)
                    fw.close();

            } catch (IOException ex) {

                log.log(Level.SEVERE, "Error", ex);

            }

        }
        log.log(Level.INFO, "entity manager=" + em.isOpen());
        try {
            log.log(Level.INFO, "gst filing=" + gstFilings.size());
            if (gstFilings.size() != 0) {
                for (GSTFiling gstFiling : gstFilings) {
                    gstFiling.setGstFilingStatus(1);
                    log.log(Level.INFO, " inside set filing status");
                    log.log(Level.INFO, "get the status=" + gstFiling.getGstFilingStatus());
                    genericDAO.saveOrUpdate(gstFiling, user, em);
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            log.log(Level.SEVERE, "not able to upload", e);
            log.log(Level.INFO, "inside catch");
        }
        return file;

    }

    /*
     * Created by Firdous on 06-03-2018
     * 
     * @see
     * service.CreateExcelService#createSellOnCashTransactionTemplateExcel(model.
     * Users, javax.persistence.EntityManager, java.lang.String, java.lang.String)
     */
    @Override
    public String createSellOnCashTransactionTemplateExcel(Users user, EntityManager em, String path,
            String sheetName) throws FileNotFoundException, IOException {
        log.log(Level.INFO, ">>>>> inside create Sell On Cash transaction excel template");
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("presentStatus", 1);
        List<Branch> branchList = genericDAO.findByCriteria(Branch.class, criterias, em);
        List<Project> projectList = genericDAO.findByCriteria(Project.class, criterias, em);
        ChartOfAccountsService coaService = new ChartOfAccountsServiceImpl();
        List<Specifics> orgSpecificsList = coaService.getIncomesCoaChildNodes(em, user);

        criterias.put("type", 2);
        log.log(Level.INFO, "criterias=" + criterias);
        List<Vendor> customerList = genericDAO.findByCriteria(Vendor.class, criterias, em);
        log.log(Level.INFO, "customer size=" + customerList.size());

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheets = workbook.createSheet(sheetName);
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheets);

        // hidden mapping start
        XSSFSheet hiddenMappingSheet = workbook.createSheet("hiddenMapping");

        int totalHiddenRows = 0; // set it to max list size
        if (branchList.size() > totalHiddenRows) {
            totalHiddenRows = branchList.size();
        }
        if (projectList.size() > totalHiddenRows) {
            totalHiddenRows = projectList.size();
        }
        if (customerList.size() > totalHiddenRows) {
            totalHiddenRows = customerList.size();
        }
        if (orgSpecificsList.size() > totalHiddenRows) {
            totalHiddenRows = orgSpecificsList.size();
        }
        for (int i = 0; i < totalHiddenRows; i++) {
            XSSFRow row = hiddenMappingSheet.createRow(i);

            if (i < branchList.size()) {
                XSSFCell cell = row.createCell(0);
                cell.setCellValue(branchList.get(i).getName());
            }

            if (i < projectList.size()) {
                XSSFCell cell = row.createCell(1);
                cell.setCellValue(projectList.get(i).getName());
            }
            if (i < customerList.size()) {
                XSSFCell cell = row.createCell(2);
                cell.setCellValue(customerList.get(i).getName());
            }
            if (i < orgSpecificsList.size()) {
                XSSFCell cell = row.createCell(3);
                cell.setCellValue(orgSpecificsList.get(i).getName());
            }
            if (i < branchList.size()) {
                XSSFCell cell = row.createCell(4);
                try {
                    cell.setCellValue(branchList.get(i).getBranchBankAccounts().get(0).getBankName());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
            }

        }

        XSSFDataValidationConstraint withOrWithoutIGstConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(
                        new String[] { "OnPaymentOfIGST", "Under BOND/LUT without payment of IGST" });
        XSSFDataValidationConstraint typeOfSupplyConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "Regular Supply", "Supply Applicable For Reverse Charge",
                        "This is an Export Supply", "This is supply to SEZ Unit SEZ Developer",
                        "This is Deemed Export Supply", "Supply Made Through Ecommerce Operator", "Bill Of Supply" });
        XSSFDataValidationConstraint typeOfPayment = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "CASH", "BANK" });
        Row headerRow = sheets.createRow(0);
        headerRow.createCell(0).setCellValue(createHelper.createRichTextString("Transaction Serial Number*"));
        headerRow.createCell(1).setCellValue(createHelper.createRichTextString("Transaction Date*"));
        headerRow.createCell(2).setCellValue(createHelper.createRichTextString("Branch*"));
        headerRow.createCell(3).setCellValue(createHelper.createRichTextString("Project"));
        headerRow.createCell(4).setCellValue(createHelper.createRichTextString("Type Of Supply*"));
        headerRow.createCell(5).setCellValue(createHelper.createRichTextString("With or without GST"));
        headerRow.createCell(6).setCellValue(createHelper.createRichTextString("Customer ID*"));
        // headerRow.createCell(7).setCellValue(createHelper.createRichTextString("Place
        // Of Supply(Location)*"));
        headerRow.createCell(7).setCellValue(createHelper.createRichTextString("PO Reference"));
        headerRow.createCell(8).setCellValue(createHelper.createRichTextString("Item Code*"));
        headerRow.createCell(9).setCellValue(createHelper.createRichTextString("Price*"));
        headerRow.createCell(10).setCellValue(createHelper.createRichTextString("Units*"));
        headerRow.createCell(11).setCellValue(createHelper.createRichTextString("Discount %"));
        headerRow.createCell(12).setCellValue(createHelper.createRichTextString("Advance Adjustment"));
        headerRow.createCell(13).setCellValue(createHelper.createRichTextString("Reciept By"));
        headerRow.createCell(14).setCellValue(createHelper.createRichTextString("Bank"));
        headerRow.createCell(15).setCellValue(createHelper.createRichTextString("Instrument Number"));
        headerRow.createCell(16).setCellValue(createHelper.createRichTextString("Instrument Date"));
        headerRow.createCell(17).setCellValue(createHelper.createRichTextString("Input Reciept Details"));
        headerRow.createCell(18).setCellValue(createHelper.createRichTextString("Transaction Notes"));

        workbook.setSheetHidden(1, true);
        XSSFCellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        for (int b = 0; b < headerRow.getLastCellNum(); b++) {
            sheets.autoSizeColumn(b);
            headerRow.getCell(b).setCellStyle(style);
            sheets.lockAutoFilter(true);
        }

        int rowCount = 1;
        Row mainRow = sheets.createRow((short) rowCount);
        mainRow.createCell(0).setCellValue(createHelper.createRichTextString("1"));

        String excelFormatPattern = DateFormatConverter.convert(Locale.ENGLISH, DateUtil.mysqldf);
        CellStyle dateStyle = workbook.createCellStyle();
        DataFormat poiFormat = workbook.createDataFormat();
        dateStyle.setDataFormat(poiFormat.getFormat(excelFormatPattern));
        mainRow.createCell(1).setCellStyle(dateStyle);

        Name namedCell = workbook.createName();
        namedCell.setNameName("branch_list");
        namedCell.setRefersToFormula("hiddenMapping!$A$1:$A$" + branchList.size());
        XSSFDataValidationConstraint branchConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("branch_list");
        CellRangeAddressList branchNameList = new CellRangeAddressList(rowCount, rowCount, 2, 2);
        XSSFDataValidation branchNameValidation = (XSSFDataValidation) dvHelper.createValidation(branchConstraint,
                branchNameList);
        branchNameValidation.setShowErrorBox(true);
        sheets.addValidationData(branchNameValidation);

        if (projectList.size() > 0) {
            Name namedCell1 = workbook.createName();
            namedCell1.setNameName("project_list");
            namedCell1.setRefersToFormula("hiddenMapping!$B$1:$B$" + projectList.size());
            XSSFDataValidationConstraint projectListConstraint = (XSSFDataValidationConstraint) dvHelper
                    .createFormulaListConstraint("project_list");
            CellRangeAddressList projectNameList = new CellRangeAddressList(rowCount, rowCount, 3, 3);
            XSSFDataValidation projectNameListValidation = (XSSFDataValidation) dvHelper
                    .createValidation(projectListConstraint, projectNameList);
            projectNameListValidation.setShowErrorBox(true);
            sheets.addValidationData(projectNameListValidation);
        } else {
            mainRow.createCell(3).setCellValue(createHelper.createRichTextString(""));
        }
        CellRangeAddressList typeOfSupplyList = new CellRangeAddressList(rowCount, rowCount, 4, 4);// cash/credit
        XSSFDataValidation typeOfSupplyValidation = (XSSFDataValidation) dvHelper
                .createValidation(typeOfSupplyConstraint, typeOfSupplyList);
        typeOfSupplyValidation.setShowErrorBox(true);
        sheets.addValidationData(typeOfSupplyValidation);
        mainRow.createCell(4).setCellValue("Regular Supply");

        CellRangeAddressList withOrWithoutIGSTList = new CellRangeAddressList(rowCount, rowCount, 5, 5);// cash/credit
        XSSFDataValidation withOrWithoutIGSTValidation = (XSSFDataValidation) dvHelper
                .createValidation(withOrWithoutIGstConstraint, withOrWithoutIGSTList);
        withOrWithoutIGSTValidation.setShowErrorBox(true);
        sheets.addValidationData(withOrWithoutIGSTValidation);

        Name namedCell1 = workbook.createName();
        namedCell1.setNameName("customer_list");
        namedCell1.setRefersToFormula("hiddenMapping!$C$1:$C$" + customerList.size());
        XSSFDataValidationConstraint customerListConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("customer_list");
        CellRangeAddressList customerNameList = new CellRangeAddressList(rowCount, rowCount, 6, 6);
        XSSFDataValidation customerNameListValidation = (XSSFDataValidation) dvHelper
                .createValidation(customerListConstraint, customerNameList);
        customerNameListValidation.setShowErrorBox(true);
        sheets.addValidationData(customerNameListValidation);
        // mainRow.createCell(6).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        // mainRow.createCell(7).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(7).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        Name namedCell2 = workbook.createName();
        namedCell2.setNameName("item_list");
        namedCell2.setRefersToFormula("hiddenMapping!$D$1:$D$" + orgSpecificsList.size());
        XSSFDataValidationConstraint itemNameConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("item_list");
        CellRangeAddressList itemNameList = new CellRangeAddressList(rowCount, rowCount, 8, 8);
        XSSFDataValidation itemNameValidation = (XSSFDataValidation) dvHelper.createValidation(itemNameConstraint,
                itemNameList);
        itemNameValidation.setShowErrorBox(true);
        sheets.addValidationData(itemNameValidation);

        // mainRow.createCell(9).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(9).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(10).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(11).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(12).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        CellRangeAddressList typeOfPaymentList = new CellRangeAddressList(rowCount, rowCount, 13, 13);// cash/credit
        XSSFDataValidation typeOfPaymentValidation = (XSSFDataValidation) dvHelper.createValidation(typeOfPayment,
                typeOfPaymentList);
        typeOfPaymentValidation.setShowErrorBox(true);
        sheets.addValidationData(typeOfPaymentValidation);

        Name namedCell3 = workbook.createName();
        namedCell3.setNameName("branch_bank_list");
        namedCell3.setRefersToFormula("hiddenMapping!$E$1:$E$" + branchList.size());
        XSSFDataValidationConstraint branchBankConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("branch_bank_list");
        CellRangeAddressList branchBankNameList = new CellRangeAddressList(rowCount, rowCount, 14, 14);
        XSSFDataValidation branchBankNameValidation = (XSSFDataValidation) dvHelper
                .createValidation(branchBankConstraint, branchBankNameList);
        branchBankNameValidation.setShowErrorBox(true);
        sheets.addValidationData(branchBankNameValidation);

        mainRow.createCell(15).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(16).setCellStyle(dateStyle);

        mainRow.createCell(17).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(18).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        for (int b = 0; b < mainRow.getLastCellNum(); b++) {
            sheets.autoSizeColumn(b);
        }

        FileOutputStream fileOut = new FileOutputStream(path);
        workbook.write(fileOut);
        fileOut.close();
        log.log(Level.FINE, "===============End ");
        return sheetName;
    }

    @Override
    public String createBuyOnCreditTransactionTemplateExcel(Users user, EntityManager em, String path,
            String sheetName) throws IOException {
        // TODO Auto-generated method stub
        log.log(Level.INFO, ">>>>> inside create buy on credit transaction excel template");
        log.log(Level.INFO, ">>>>> inside create buy on cash transaction excel template");
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("presentStatus", 1);
        List<BranchTaxes> branchTaxesList = new ArrayList<BranchTaxes>();
        List<BranchTaxes> branchCESSTaxesList = new ArrayList<BranchTaxes>();

        List<Branch> branchList = genericDAO.findByCriteria(Branch.class, criterias, em);
        List<Project> projectList = genericDAO.findByCriteria(Project.class, criterias, em);
        ChartOfAccountsService coaService = new ChartOfAccountsServiceImpl();
        List<Specifics> orgSpecificsList = coaService.getExpensesCoaChildNodes(em, user);
        // for(Branch branch:branchList){
        /*
         * criterias.put("branch.id", branch.getId());
         * criterias.put("taxType", 10);
         * List<BranchTaxes> branchTaxes=genericDAO.findByCriteria(BranchTaxes.class,
         * criterias, em);
         * branchTaxesList.addAll(branchTaxes);
         * }
         * for(Branch branch:branchList){
         * criterias.put("branch.id", branch.getId());
         * criterias.put("taxType", 11);
         * List<BranchTaxes> branchTaxes=genericDAO.findByCriteria(BranchTaxes.class,
         * criterias, em);
         * branchTaxesList.addAll(branchTaxes);
         * }
         */

        for (Branch branch : branchList) {
            criterias.put("branch.id", branch.getId());
            criterias.put("taxType", 12);
            criterias.put("presentStatus", 1);
            List<BranchTaxes> branchTaxes = genericDAO.findByCriteria(BranchTaxes.class, criterias, em);
            branchTaxesList.addAll(branchTaxes);
        }
        for (Branch branch : branchList) {
            criterias.put("branch.id", branch.getId());
            criterias.put("taxType", 13);
            criterias.put("presentStatus", 1);
            List<BranchTaxes> branchTaxes = genericDAO.findByCriteria(BranchTaxes.class, criterias, em);
            branchCESSTaxesList.addAll(branchTaxes);
        }
        criterias.clear();
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("type", 1);
        criterias.put("presentStatus", 1);
        log.log(Level.INFO, "criterias=" + criterias);
        List<Vendor> vendorList = genericDAO.findByCriteria(Vendor.class, criterias, em);
        log.log(Level.INFO, "customer size=" + vendorList.size());

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheets = workbook.createSheet(sheetName);
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheets);

        // hidden mapping start
        XSSFSheet hiddenMappingSheet = workbook.createSheet("hiddenMapping");

        int totalHiddenRows = 0; // set it to max list size
        if (branchList.size() > totalHiddenRows) {
            totalHiddenRows = branchList.size();
        }
        if (projectList.size() > totalHiddenRows) {
            totalHiddenRows = projectList.size();
        }
        if (vendorList.size() > totalHiddenRows) {
            totalHiddenRows = vendorList.size();
        }
        if (orgSpecificsList.size() > totalHiddenRows) {
            totalHiddenRows = orgSpecificsList.size();
        }
        if (branchTaxesList.size() > totalHiddenRows) {
            totalHiddenRows = branchTaxesList.size();
        }
        if (branchCESSTaxesList.size() > totalHiddenRows) {
            totalHiddenRows = branchCESSTaxesList.size();
        }
        for (int i = 0; i < totalHiddenRows; i++) {
            XSSFRow row = hiddenMappingSheet.createRow(i);

            if (i < branchList.size()) {
                XSSFCell cell = row.createCell(0);
                cell.setCellValue(branchList.get(i).getName());
            }

            if (i < projectList.size()) {
                XSSFCell cell = row.createCell(1);
                cell.setCellValue(projectList.get(i).getName());
            }
            if (i < vendorList.size()) {
                XSSFCell cell = row.createCell(2);
                cell.setCellValue(vendorList.get(i).getName());
            }
            if (i < orgSpecificsList.size()) {
                XSSFCell cell = row.createCell(3);
                cell.setCellValue(orgSpecificsList.get(i).getName());
            }
            if (i < branchTaxesList.size()) {
                XSSFCell cell = row.createCell(4);
                cell.setCellValue(branchTaxesList.get(i).getTaxRate());
            }
            if (i < branchCESSTaxesList.size()) {
                XSSFCell cell = row.createCell(5);
                cell.setCellValue(branchCESSTaxesList.get(i).getTaxRate());
            }
            if (i < branchList.size()) {
                XSSFCell cell = row.createCell(6);
                try {
                    cell.setCellValue(branchList.get(i).getBranchBankAccounts().get(0).getBankName());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
            }

        }

        XSSFDataValidationConstraint withOrWithoutIGstConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(
                        new String[] { "OnPaymentOfIGST", "Under BOND/LUT without payment of IGST" });
        XSSFDataValidationConstraint typeOfSupplyConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "Regular Supply", "Supply Applicable For Reverse Charge",
                        "This is an Export Supply", "This is supply to SEZ Unit SEZ Developer",
                        "This is Deemed Export Supply", "Supply Made Through Ecommerce Operator", "Bill Of Supply" });
        XSSFDataValidationConstraint gstRateConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "5", "12", "18", "28" });
        Row headerRow = sheets.createRow(0);
        headerRow.createCell(0).setCellValue(createHelper.createRichTextString("Transaction Serial Number*"));
        headerRow.createCell(1).setCellValue(createHelper.createRichTextString("Transaction Date*"));
        headerRow.createCell(2).setCellValue(createHelper.createRichTextString("Branch*"));
        headerRow.createCell(3).setCellValue(createHelper.createRichTextString("Project"));
        headerRow.createCell(4).setCellValue(createHelper.createRichTextString("Type Of Supply*"));
        headerRow.createCell(5).setCellValue(createHelper.createRichTextString("Vendor ID*"));
        headerRow.createCell(6).setCellValue(createHelper.createRichTextString("PO Reference"));
        headerRow.createCell(7).setCellValue(createHelper.createRichTextString("Item Code*"));
        headerRow.createCell(8).setCellValue(createHelper.createRichTextString("Price*"));
        headerRow.createCell(9).setCellValue(createHelper.createRichTextString("Units*"));
        headerRow.createCell(10).setCellValue(createHelper.createRichTextString("GST Rate"));
        headerRow.createCell(11).setCellValue(createHelper.createRichTextString("CESS Rate"));
        headerRow.createCell(12).setCellValue(createHelper.createRichTextString("Advance Adjustment"));
        headerRow.createCell(13).setCellValue(createHelper.createRichTextString("Transaction Notes"));
        headerRow.createCell(14).setCellValue(createHelper.createRichTextString("Invoice/Reference Date"));
        headerRow.createCell(15).setCellValue(createHelper.createRichTextString("Invoice/Reference Number"));
        headerRow.createCell(16).setCellValue(createHelper.createRichTextString("DC / GRN / Reference  Date"));
        headerRow.createCell(17).setCellValue(createHelper.createRichTextString("DC / GRN / Reference  Number"));
        headerRow.createCell(18).setCellValue(createHelper.createRichTextString("Way Bill No"));
        headerRow.createCell(19).setCellValue(createHelper.createRichTextString("Transporter Name"));
        headerRow.createCell(20).setCellValue(createHelper.createRichTextString("Lorry Receipt Number"));
        headerRow.createCell(21).setCellValue(createHelper.createRichTextString("Lorry Receipt Date"));
        headerRow.createCell(22).setCellValue(createHelper.createRichTextString("Import Reference Date"));
        headerRow.createCell(23).setCellValue(createHelper.createRichTextString("Import Reference Number"));
        headerRow.createCell(24).setCellValue(createHelper.createRichTextString("Port Code"));

        workbook.setSheetHidden(1, true);
        XSSFCellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        for (int b = 0; b < headerRow.getLastCellNum(); b++) {
            sheets.autoSizeColumn(b);
            headerRow.getCell(b).setCellStyle(style);
            sheets.lockAutoFilter(true);
        }

        int rowCount = 1;
        Row mainRow = sheets.createRow((short) rowCount);
        mainRow.createCell(0).setCellValue(createHelper.createRichTextString("1"));

        String excelFormatPattern = DateFormatConverter.convert(Locale.ENGLISH, DateUtil.mysqldf);
        CellStyle dateStyle = workbook.createCellStyle();
        DataFormat poiFormat = workbook.createDataFormat();
        dateStyle.setDataFormat(poiFormat.getFormat(excelFormatPattern));
        mainRow.createCell(1).setCellStyle(dateStyle);

        Name namedCell = workbook.createName();
        namedCell.setNameName("branch_list");
        namedCell.setRefersToFormula("hiddenMapping!$A$1:$A$" + branchList.size());
        XSSFDataValidationConstraint branchConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("branch_list");
        CellRangeAddressList branchNameList = new CellRangeAddressList(rowCount, rowCount, 2, 2);
        XSSFDataValidation branchNameValidation = (XSSFDataValidation) dvHelper.createValidation(branchConstraint,
                branchNameList);
        branchNameValidation.setShowErrorBox(true);
        sheets.addValidationData(branchNameValidation);

        if (projectList.size() > 0) {
            Name namedCell1 = workbook.createName();
            namedCell1.setNameName("project_list");
            namedCell1.setRefersToFormula("hiddenMapping!$B$1:$B$" + projectList.size());
            XSSFDataValidationConstraint projectListConstraint = (XSSFDataValidationConstraint) dvHelper
                    .createFormulaListConstraint("project_list");
            CellRangeAddressList projectNameList = new CellRangeAddressList(rowCount, rowCount, 3, 3);
            XSSFDataValidation projectNameListValidation = (XSSFDataValidation) dvHelper
                    .createValidation(projectListConstraint, projectNameList);
            projectNameListValidation.setShowErrorBox(true);
            sheets.addValidationData(projectNameListValidation);
        } else {
            mainRow.createCell(3).setCellValue(createHelper.createRichTextString(""));
        }
        CellRangeAddressList typeOfSupplyList = new CellRangeAddressList(rowCount, rowCount, 4, 4);// cash/credit
        XSSFDataValidation typeOfSupplyValidation = (XSSFDataValidation) dvHelper
                .createValidation(typeOfSupplyConstraint, typeOfSupplyList);
        typeOfSupplyValidation.setShowErrorBox(true);
        sheets.addValidationData(typeOfSupplyValidation);
        mainRow.createCell(4).setCellValue("Regular Supply");

        Name namedCell1 = workbook.createName();
        namedCell1.setNameName("vendor_list");
        namedCell1.setRefersToFormula("hiddenMapping!$C$1:$C$" + vendorList.size());
        XSSFDataValidationConstraint vendorListConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("vendor_list");
        CellRangeAddressList vendorNameList = new CellRangeAddressList(rowCount, rowCount, 5, 5);
        XSSFDataValidation vendorNameListValidation = (XSSFDataValidation) dvHelper
                .createValidation(vendorListConstraint, vendorNameList);
        vendorNameListValidation.setShowErrorBox(true);
        sheets.addValidationData(vendorNameListValidation);
        // mainRow.createCell(6).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        // mainRow.createCell(7).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(6).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        Name namedCell2 = workbook.createName();
        namedCell2.setNameName("item_list");
        namedCell2.setRefersToFormula("hiddenMapping!$D$1:$D$" + orgSpecificsList.size());
        XSSFDataValidationConstraint itemNameConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("item_list");
        CellRangeAddressList itemNameList = new CellRangeAddressList(rowCount, rowCount, 7, 7);
        XSSFDataValidation itemNameValidation = (XSSFDataValidation) dvHelper.createValidation(itemNameConstraint,
                itemNameList);
        itemNameValidation.setShowErrorBox(true);
        sheets.addValidationData(itemNameValidation);

        // mainRow.createCell(9).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(8).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(9).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        if (branchTaxesList.isEmpty() || branchTaxesList.size() == 0) {
            mainRow.createCell(10).setCellValue(createHelper.createRichTextString("0"));
        } else {
            Name namedCell5 = workbook.createName();
            namedCell5.setNameName("branch_tax_list");
            namedCell5.setRefersToFormula("hiddenMapping!$E$1:$E$" + branchTaxesList.size());
            XSSFDataValidationConstraint branchtaxListConstraint = (XSSFDataValidationConstraint) dvHelper
                    .createFormulaListConstraint("branch_tax_list");
            CellRangeAddressList branchTaxList = new CellRangeAddressList(rowCount, rowCount, 10, 10);
            XSSFDataValidation branchTaxesValidation = (XSSFDataValidation) dvHelper
                    .createValidation(branchtaxListConstraint, branchTaxList);
            branchTaxesValidation.setShowErrorBox(true);
            sheets.addValidationData(branchTaxesValidation);
        }

        if (branchCESSTaxesList.isEmpty() || branchCESSTaxesList.size() == 0) {
            mainRow.createCell(11).setCellValue(createHelper.createRichTextString("0"));
        } else {
            Name namedCell6 = workbook.createName();
            namedCell6.setNameName("branch__cess_tax_list");
            namedCell6.setRefersToFormula("hiddenMapping!$F$1:$F$" + branchCESSTaxesList.size());
            XSSFDataValidationConstraint branchtaxListConstraint = (XSSFDataValidationConstraint) dvHelper
                    .createFormulaListConstraint("branch__cess_tax_list");
            CellRangeAddressList branchTaxList = new CellRangeAddressList(rowCount, rowCount, 11, 11);
            XSSFDataValidation branchTaxesValidation = (XSSFDataValidation) dvHelper
                    .createValidation(branchtaxListConstraint, branchTaxList);
            branchTaxesValidation.setShowErrorBox(true);
            sheets.addValidationData(branchTaxesValidation);
        }

        mainRow.createCell(12).setCellValue(createHelper.createRichTextString(""));

        mainRow.createCell(13).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(14).setCellStyle(dateStyle);

        mainRow.createCell(15).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(16).setCellStyle(dateStyle);

        mainRow.createCell(17).setCellValue(createHelper.createRichTextString("1234"));

        mainRow.createCell(18).setCellValue(createHelper.createRichTextString("1234"));

        mainRow.createCell(19).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(20).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(21).setCellStyle(dateStyle);

        mainRow.createCell(22).setCellStyle(dateStyle);

        mainRow.createCell(23).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(24).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        for (int b = 0; b < mainRow.getLastCellNum(); b++) {
            sheets.autoSizeColumn(b);
        }

        FileOutputStream fileOut = new FileOutputStream(path);
        workbook.write(fileOut);
        fileOut.close();
        log.log(Level.FINE, "===============End ");
        return sheetName;
    }

    @Override
    public String createBuyOnCashTransactionTemplateExcel(Users user, EntityManager em, String path,
            String sheetName) throws FileNotFoundException, IOException {
        // TODO Auto-generated method stub
        log.log(Level.INFO, ">>>>> inside create buy on cash transaction excel template");
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("presentStatus", 1);
        List<BranchTaxes> branchTaxesList = new ArrayList<BranchTaxes>();
        List<BranchTaxes> branchCESSTaxesList = new ArrayList<BranchTaxes>();

        List<Branch> branchList = genericDAO.findByCriteria(Branch.class, criterias, em);
        List<Project> projectList = genericDAO.findByCriteria(Project.class, criterias, em);
        ChartOfAccountsService coaService = new ChartOfAccountsServiceImpl();
        List<Specifics> orgSpecificsList = coaService.getExpensesCoaChildNodes(em, user);
        /*
         * for(Branch branch:branchList){
         * criterias.put("branch.id", branch.getId());
         * criterias.put("taxType", 10);
         * List<BranchTaxes> branchTaxes=genericDAO.findByCriteria(BranchTaxes.class,
         * criterias, em);
         * branchTaxesList.addAll(branchTaxes);
         * }
         * for(Branch branch:branchList){
         * criterias.put("branch.id", branch.getId());
         * criterias.put("taxType", 11);
         * List<BranchTaxes> branchTaxes=genericDAO.findByCriteria(BranchTaxes.class,
         * criterias, em);
         * branchTaxesList.addAll(branchTaxes);
         * }
         */

        for (Branch branch : branchList) {
            criterias.put("branch.id", branch.getId());
            criterias.put("taxType", 12);
            criterias.put("presentStatus", 1);
            List<BranchTaxes> branchTaxes = genericDAO.findByCriteria(BranchTaxes.class, criterias, em);
            branchTaxesList.addAll(branchTaxes);
        }
        for (Branch branch : branchList) {
            criterias.put("branch.id", branch.getId());
            criterias.put("taxType", 13);
            criterias.put("presentStatus", 1);
            List<BranchTaxes> branchTaxes = genericDAO.findByCriteria(BranchTaxes.class, criterias, em);
            branchCESSTaxesList.addAll(branchTaxes);
        }
        criterias.clear();
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("type", 1);
        criterias.put("presentStatus", 1);
        log.log(Level.INFO, "criterias=" + criterias);
        List<Vendor> vendorList = genericDAO.findByCriteria(Vendor.class, criterias, em);
        log.log(Level.INFO, "customer size=" + vendorList.size());

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheets = workbook.createSheet(sheetName);
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheets);

        // hidden mapping start
        XSSFSheet hiddenMappingSheet = workbook.createSheet("hiddenMapping");

        int totalHiddenRows = 0; // set it to max list size
        if (branchList.size() > totalHiddenRows) {
            totalHiddenRows = branchList.size();
        }
        if (projectList.size() > totalHiddenRows) {
            totalHiddenRows = projectList.size();
        }
        if (vendorList.size() > totalHiddenRows) {
            totalHiddenRows = vendorList.size();
        }
        if (orgSpecificsList.size() > totalHiddenRows) {
            totalHiddenRows = orgSpecificsList.size();
        }
        if (branchTaxesList.size() > totalHiddenRows) {
            totalHiddenRows = branchTaxesList.size();
        }
        if (branchCESSTaxesList.size() > totalHiddenRows) {
            totalHiddenRows = branchCESSTaxesList.size();
        }
        for (int i = 0; i < totalHiddenRows; i++) {
            XSSFRow row = hiddenMappingSheet.createRow(i);

            if (i < branchList.size()) {
                XSSFCell cell = row.createCell(0);
                cell.setCellValue(branchList.get(i).getName());
            }

            if (i < projectList.size()) {
                XSSFCell cell = row.createCell(1);
                cell.setCellValue(projectList.get(i).getName());
            }
            if (i < vendorList.size()) {
                XSSFCell cell = row.createCell(2);
                cell.setCellValue(vendorList.get(i).getName());
            }
            if (i < orgSpecificsList.size()) {
                XSSFCell cell = row.createCell(3);
                cell.setCellValue(orgSpecificsList.get(i).getName());
            }
            if (i < branchTaxesList.size()) {
                XSSFCell cell = row.createCell(4);
                cell.setCellValue(branchTaxesList.get(i).getTaxRate());
            }
            if (i < branchCESSTaxesList.size()) {
                XSSFCell cell = row.createCell(5);
                cell.setCellValue(branchCESSTaxesList.get(i).getTaxRate());
            }
            if (i < branchList.size()) {
                XSSFCell cell = row.createCell(6);
                try {
                    cell.setCellValue(branchList.get(i).getBranchBankAccounts().get(0).getBankName());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
            }

        }

        XSSFDataValidationConstraint withOrWithoutIGstConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(
                        new String[] { "OnPaymentOfIGST", "Under BOND/LUT without payment of IGST" });
        XSSFDataValidationConstraint typeOfSupplyConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "Regular Supply", "Supply Applicable For Reverse Charge",
                        "This is an Export Supply", "This is supply to SEZ Unit SEZ Developer",
                        "This is Deemed Export Supply", "Supply Made Through Ecommerce Operator", "Bill Of Supply" });
        XSSFDataValidationConstraint gstRateConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "5", "12", "18", "28" });
        XSSFDataValidationConstraint typeOfPayment = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "CASH", "BANK" });
        Row headerRow = sheets.createRow(0);

        headerRow.createCell(0).setCellValue(createHelper.createRichTextString("Transaction Serial Number*"));
        headerRow.createCell(1).setCellValue(createHelper.createRichTextString("Transaction Date*"));
        headerRow.createCell(2).setCellValue(createHelper.createRichTextString("Branch*"));
        headerRow.createCell(3).setCellValue(createHelper.createRichTextString("Project"));
        headerRow.createCell(4).setCellValue(createHelper.createRichTextString("Type Of Supply*"));
        headerRow.createCell(5).setCellValue(createHelper.createRichTextString("Vendor ID*"));
        headerRow.createCell(6).setCellValue(createHelper.createRichTextString("PO Reference"));
        headerRow.createCell(7).setCellValue(createHelper.createRichTextString("Item Code*"));
        headerRow.createCell(8).setCellValue(createHelper.createRichTextString("Price*"));
        headerRow.createCell(9).setCellValue(createHelper.createRichTextString("Units*"));
        headerRow.createCell(10).setCellValue(createHelper.createRichTextString("GST Rate"));
        headerRow.createCell(11).setCellValue(createHelper.createRichTextString("CESS Rate"));
        headerRow.createCell(12).setCellValue(createHelper.createRichTextString("Advance Adjustment"));
        headerRow.createCell(13).setCellValue(createHelper.createRichTextString("Transaction Notes"));
        headerRow.createCell(14).setCellValue(createHelper.createRichTextString("Invoice/Reference Date"));
        headerRow.createCell(15).setCellValue(createHelper.createRichTextString("Invoice/Reference Number"));
        headerRow.createCell(16).setCellValue(createHelper.createRichTextString("DC / GRN / Reference  Date"));
        headerRow.createCell(17).setCellValue(createHelper.createRichTextString("DC / GRN / Reference  Number"));
        headerRow.createCell(18).setCellValue(createHelper.createRichTextString("Way Bill No"));
        headerRow.createCell(19).setCellValue(createHelper.createRichTextString("Transporter Name"));
        headerRow.createCell(20).setCellValue(createHelper.createRichTextString("Lorry Receipt Number"));
        headerRow.createCell(21).setCellValue(createHelper.createRichTextString("Lorry Receipt Date"));
        headerRow.createCell(22).setCellValue(createHelper.createRichTextString("Import Reference Date"));
        headerRow.createCell(23).setCellValue(createHelper.createRichTextString("Import Reference Number"));
        headerRow.createCell(24).setCellValue(createHelper.createRichTextString("Port Code"));

        headerRow.createCell(25).setCellValue(createHelper.createRichTextString("Reciept By"));
        headerRow.createCell(26).setCellValue(createHelper.createRichTextString("Bank"));
        headerRow.createCell(27).setCellValue(createHelper.createRichTextString("Instrument Number"));
        headerRow.createCell(28).setCellValue(createHelper.createRichTextString("Instrument Date"));
        headerRow.createCell(29).setCellValue(createHelper.createRichTextString("Input Reciept Details"));

        workbook.setSheetHidden(1, true);
        XSSFCellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        for (int b = 0; b < headerRow.getLastCellNum(); b++) {
            sheets.autoSizeColumn(b);
            headerRow.getCell(b).setCellStyle(style);
            sheets.lockAutoFilter(true);
        }

        int rowCount = 1;
        Row mainRow = sheets.createRow((short) rowCount);
        mainRow.createCell(0).setCellValue(createHelper.createRichTextString("1"));

        String excelFormatPattern = DateFormatConverter.convert(Locale.ENGLISH, DateUtil.mysqldf);
        CellStyle dateStyle = workbook.createCellStyle();
        DataFormat poiFormat = workbook.createDataFormat();
        dateStyle.setDataFormat(poiFormat.getFormat(excelFormatPattern));
        mainRow.createCell(1).setCellStyle(dateStyle);

        Name namedCell = workbook.createName();
        namedCell.setNameName("branch_list");
        namedCell.setRefersToFormula("hiddenMapping!$A$1:$A$" + branchList.size());
        XSSFDataValidationConstraint branchConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("branch_list");
        CellRangeAddressList branchNameList = new CellRangeAddressList(rowCount, rowCount, 2, 2);
        XSSFDataValidation branchNameValidation = (XSSFDataValidation) dvHelper.createValidation(branchConstraint,
                branchNameList);
        branchNameValidation.setShowErrorBox(true);
        sheets.addValidationData(branchNameValidation);

        if (projectList.size() > 0) {
            Name namedCell1 = workbook.createName();
            namedCell1.setNameName("project_list");
            namedCell1.setRefersToFormula("hiddenMapping!$B$1:$B$" + projectList.size());
            XSSFDataValidationConstraint projectListConstraint = (XSSFDataValidationConstraint) dvHelper
                    .createFormulaListConstraint("project_list");
            CellRangeAddressList projectNameList = new CellRangeAddressList(rowCount, rowCount, 3, 3);
            XSSFDataValidation projectNameListValidation = (XSSFDataValidation) dvHelper
                    .createValidation(projectListConstraint, projectNameList);
            projectNameListValidation.setShowErrorBox(true);
            sheets.addValidationData(projectNameListValidation);
        } else {
            mainRow.createCell(3).setCellValue(createHelper.createRichTextString(""));
        }
        CellRangeAddressList typeOfSupplyList = new CellRangeAddressList(rowCount, rowCount, 4, 4);// cash/credit
        XSSFDataValidation typeOfSupplyValidation = (XSSFDataValidation) dvHelper
                .createValidation(typeOfSupplyConstraint, typeOfSupplyList);
        typeOfSupplyValidation.setShowErrorBox(true);
        sheets.addValidationData(typeOfSupplyValidation);
        mainRow.createCell(4).setCellValue("Regular Supply");

        Name namedCell1 = workbook.createName();
        namedCell1.setNameName("vendor_list");
        namedCell1.setRefersToFormula("hiddenMapping!$C$1:$C$" + vendorList.size());
        XSSFDataValidationConstraint vendorListConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("vendor_list");
        CellRangeAddressList vendorNameList = new CellRangeAddressList(rowCount, rowCount, 5, 5);
        XSSFDataValidation vendorNameListValidation = (XSSFDataValidation) dvHelper
                .createValidation(vendorListConstraint, vendorNameList);
        vendorNameListValidation.setShowErrorBox(true);
        sheets.addValidationData(vendorNameListValidation);
        // mainRow.createCell(6).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        // mainRow.createCell(7).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(6).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        Name namedCell2 = workbook.createName();
        namedCell2.setNameName("item_list");
        namedCell2.setRefersToFormula("hiddenMapping!$D$1:$D$" + orgSpecificsList.size());
        XSSFDataValidationConstraint itemNameConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("item_list");
        CellRangeAddressList itemNameList = new CellRangeAddressList(rowCount, rowCount, 7, 7);
        XSSFDataValidation itemNameValidation = (XSSFDataValidation) dvHelper.createValidation(itemNameConstraint,
                itemNameList);
        itemNameValidation.setShowErrorBox(true);
        sheets.addValidationData(itemNameValidation);

        // mainRow.createCell(9).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(8).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(9).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        if (branchTaxesList.isEmpty() || branchTaxesList.size() == 0) {
            mainRow.createCell(10).setCellValue(createHelper.createRichTextString("0"));
        } else {
            Name namedCell5 = workbook.createName();
            namedCell5.setNameName("branch_tax_list");
            namedCell5.setRefersToFormula("hiddenMapping!$E$1:$E$" + branchTaxesList.size());
            XSSFDataValidationConstraint branchtaxListConstraint = (XSSFDataValidationConstraint) dvHelper
                    .createFormulaListConstraint("branch_tax_list");
            CellRangeAddressList branchTaxList = new CellRangeAddressList(rowCount, rowCount, 10, 10);
            XSSFDataValidation branchTaxesValidation = (XSSFDataValidation) dvHelper
                    .createValidation(branchtaxListConstraint, branchTaxList);
            branchTaxesValidation.setShowErrorBox(true);
            sheets.addValidationData(branchTaxesValidation);
        }

        if (branchCESSTaxesList.isEmpty() || branchCESSTaxesList.size() == 0) {
            mainRow.createCell(11).setCellValue(createHelper.createRichTextString("0"));
        } else {
            Name namedCell6 = workbook.createName();
            namedCell6.setNameName("branch__cess_tax_list");
            namedCell6.setRefersToFormula("hiddenMapping!$F$1:$F$" + branchCESSTaxesList.size());
            XSSFDataValidationConstraint branchtaxListConstraint = (XSSFDataValidationConstraint) dvHelper
                    .createFormulaListConstraint("branch__cess_tax_list");
            CellRangeAddressList branchTaxList = new CellRangeAddressList(rowCount, rowCount, 11, 11);
            XSSFDataValidation branchTaxesValidation = (XSSFDataValidation) dvHelper
                    .createValidation(branchtaxListConstraint, branchTaxList);
            branchTaxesValidation.setShowErrorBox(true);
            sheets.addValidationData(branchTaxesValidation);
        }

        mainRow.createCell(12).setCellValue(createHelper.createRichTextString(""));

        mainRow.createCell(13).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(14).setCellStyle(dateStyle);

        mainRow.createCell(15).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(16).setCellStyle(dateStyle);

        mainRow.createCell(17).setCellValue(createHelper.createRichTextString("1234"));

        mainRow.createCell(18).setCellValue(createHelper.createRichTextString("1234"));

        mainRow.createCell(19).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(20).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(21).setCellStyle(dateStyle);

        mainRow.createCell(22).setCellStyle(dateStyle);

        mainRow.createCell(23).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(24).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        CellRangeAddressList typeOfPaymentList = new CellRangeAddressList(rowCount, rowCount, 25, 25);// cash/credit
        XSSFDataValidation typeOfPaymentValidation = (XSSFDataValidation) dvHelper.createValidation(typeOfPayment,
                typeOfPaymentList);
        typeOfPaymentValidation.setShowErrorBox(true);
        sheets.addValidationData(typeOfPaymentValidation);

        Name namedCell3 = workbook.createName();
        namedCell3.setNameName("branch_bank_list");
        namedCell3.setRefersToFormula("hiddenMapping!$G$1:$G$" + branchList.size());
        XSSFDataValidationConstraint branchBankConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("branch_bank_list");
        CellRangeAddressList branchBankNameList = new CellRangeAddressList(rowCount, rowCount, 26, 26);
        XSSFDataValidation branchBankNameValidation = (XSSFDataValidation) dvHelper
                .createValidation(branchBankConstraint, branchBankNameList);
        branchBankNameValidation.setShowErrorBox(true);
        sheets.addValidationData(branchBankNameValidation);

        mainRow.createCell(27).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(28).setCellStyle(dateStyle);

        mainRow.createCell(29).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        for (int b = 0; b < mainRow.getLastCellNum(); b++) {
            sheets.autoSizeColumn(b);
        }

        FileOutputStream fileOut = new FileOutputStream(path);
        workbook.write(fileOut);
        fileOut.close();
        log.log(Level.FINE, "===============End ");
        return sheetName;

    }

    @Override
    public String createPayVendorTransactionTemplateExcel(Users user, EntityManager em, String path,
            String sheetName) throws FileNotFoundException, IOException {
        // TODO Auto-generated method stub
        log.log(Level.INFO, ">>>>> inside create receive payment from customer transaction excel template");

        String stbuf = ("select obj from Branch obj where obj.organization.id=?1 and obj.presentStatus=1");
        ArrayList inparam = new ArrayList(1);
        inparam.add(user.getOrganization().getId());
        List<Branch> branchList = genericDAO.queryWithParams(stbuf.toString(), em, inparam);

        String stbuf1 = ("select obj from Vendor obj where obj.organization.id=?1 and presentStatus=?2 and obj.type=?3 and (obj.purchaseType=?4 or obj.purchaseType=?5)");
        ArrayList inparam1 = new ArrayList(5);
        inparam1.add(user.getOrganization().getId());
        inparam1.add(Integer.parseInt("1"));
        inparam1.add(Integer.parseInt("1"));
        inparam1.add(Integer.parseInt("0"));
        inparam1.add(Integer.parseInt("2"));
        List<Vendor> customerList = genericDAO.queryWithParams(stbuf1.toString(), em, inparam1);
        String stbuf2 = ("select obj from Transaction obj where obj.transactionBranchOrganization.id=?1 and (obj.paymentStatus=?2 or obj.paymentStatus=?3)  and obj.transactionPurpose.transactionPurpose=?4 and obj.presentStatus=1");
        ArrayList inparam2 = new ArrayList(3);
        inparam2.add(user.getOrganization().getId());
        inparam2.add("NOT-PAID");
        inparam2.add("PARTLY-PAID");
        inparam2.add("Buy On credit & pay later");

        List<Transaction> creditCustVendPendingInvoices = genericDAO.queryWithParams(stbuf2.toString(), em, inparam2);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheets = workbook.createSheet(sheetName);
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheets);

        // hidden mapping start
        XSSFSheet hiddenMappingSheet = workbook.createSheet("hiddenMapping");

        int totalHiddenRows = 0; // set it to max list size
        if (branchList.size() > totalHiddenRows) {
            totalHiddenRows = branchList.size();
        }

        if (customerList.size() > totalHiddenRows) {
            totalHiddenRows = customerList.size();
        }

        if (creditCustVendPendingInvoices.size() > totalHiddenRows) {
            totalHiddenRows = creditCustVendPendingInvoices.size();
        }
        for (int i = 0; i < totalHiddenRows; i++) {
            XSSFRow row = hiddenMappingSheet.createRow(i);

            if (i < branchList.size()) {
                XSSFCell cell = row.createCell(0);
                cell.setCellValue(branchList.get(i).getName());
            }

            if (i < customerList.size()) {
                XSSFCell cell = row.createCell(1);
                cell.setCellValue(customerList.get(i).getName());
            }

            if (i < creditCustVendPendingInvoices.size()) {
                XSSFCell cell = row.createCell(2);
                try {
                    // cell.setCellValue(creditCustVendPendingInvoices.get(i).getTransactionSpecifics().getName()
                    // + "(" +
                    // DateUtil.idosdf.format(creditCustVendPendingInvoices.get(i).getTransactionDate())
                    // + ")(" + creditCustVendPendingInvoices.get(i).getNetAmount() + ")"
                    // +creditCustVendPendingInvoices.get(i).getTransactionRefNumber());
                    cell.setCellValue(creditCustVendPendingInvoices.get(i).getTransactionRefNumber() + "-"
                            + creditCustVendPendingInvoices.get(i).getTransactionVendorCustomer().getName() + "-"
                            + creditCustVendPendingInvoices.get(i).getInvoiceNumber() + "-"
                            + creditCustVendPendingInvoices.get(i).getVendorDuePayment());
                } catch (NullPointerException e) {
                    cell.setCellValue("");
                }

            }
            if (i < branchList.size()) {
                XSSFCell cell = row.createCell(3);
                try {
                    cell.setCellValue(branchList.get(i).getBranchBankAccounts().get(0).getBankName());
                } catch (Exception e) {
                    cell.setCellValue("");
                }
            }
        }
        XSSFRow row = hiddenMappingSheet.createRow(creditCustVendPendingInvoices.size() + 1);
        XSSFCell cell = row.createCell(2);
        cell.setCellValue("Opening Balance");

        XSSFDataValidationConstraint typeOfPayment = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(new String[] { "CASH", "BANK" });
        Row headerRow = sheets.createRow(0);
        headerRow.createCell(0).setCellValue(createHelper.createRichTextString("Transaction Serial Number*"));
        headerRow.createCell(1).setCellValue(createHelper.createRichTextString("Transaction Date*"));
        headerRow.createCell(2).setCellValue(createHelper.createRichTextString("Branch*"));
        headerRow.createCell(3).setCellValue(createHelper.createRichTextString("Vendor Name*"));
        headerRow.createCell(4).setCellValue(createHelper.createRichTextString("Select Invoice Transaction ID*"));
        headerRow.createCell(5).setCellValue(createHelper.createRichTextString("Payment Recieved"));

        headerRow.createCell(6).setCellValue(createHelper.createRichTextString("Reciept By"));
        headerRow.createCell(7).setCellValue(createHelper.createRichTextString("Bank"));
        headerRow.createCell(8).setCellValue(createHelper.createRichTextString("Instrument Number"));
        headerRow.createCell(9).setCellValue(createHelper.createRichTextString("Instrument Date"));
        headerRow.createCell(10).setCellValue(createHelper.createRichTextString("Input Reciept Details"));
        headerRow.createCell(11).setCellValue(createHelper.createRichTextString("Transaction Notes"));

        workbook.setSheetHidden(1, true);
        XSSFCellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        for (int b = 0; b < headerRow.getLastCellNum(); b++) {
            sheets.autoSizeColumn(b);
            headerRow.getCell(b).setCellStyle(style);
            sheets.lockAutoFilter(true);
        }

        int rowCount = 1;
        Row mainRow = sheets.createRow((short) rowCount);
        mainRow.createCell(0).setCellValue(createHelper.createRichTextString("1"));

        String excelFormatPattern = DateFormatConverter.convert(Locale.ENGLISH, DateUtil.mysqldf);
        CellStyle dateStyle = workbook.createCellStyle();
        DataFormat poiFormat = workbook.createDataFormat();
        dateStyle.setDataFormat(poiFormat.getFormat(excelFormatPattern));
        mainRow.createCell(1).setCellStyle(dateStyle);

        Name namedCell = workbook.createName();
        namedCell.setNameName("branch_list");
        namedCell.setRefersToFormula("hiddenMapping!$A$1:$A$" + branchList.size());
        XSSFDataValidationConstraint branchConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("branch_list");
        CellRangeAddressList branchNameList = new CellRangeAddressList(rowCount, rowCount, 2, 2);
        XSSFDataValidation branchNameValidation = (XSSFDataValidation) dvHelper.createValidation(branchConstraint,
                branchNameList);
        branchNameValidation.setShowErrorBox(true);
        sheets.addValidationData(branchNameValidation);

        Name namedCell1 = workbook.createName();
        namedCell1.setNameName("vendor_list");
        namedCell1.setRefersToFormula("hiddenMapping!$B$1:$B$" + customerList.size());
        XSSFDataValidationConstraint customerListConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("vendor_list");
        CellRangeAddressList customerNameList = new CellRangeAddressList(rowCount, rowCount, 3, 3);
        XSSFDataValidation customerNameListValidation = (XSSFDataValidation) dvHelper
                .createValidation(customerListConstraint, customerNameList);
        customerNameListValidation.setShowErrorBox(true);
        sheets.addValidationData(customerNameListValidation);

        Name namedCell2 = workbook.createName();
        namedCell2.setNameName("invoice_list");
        namedCell2.setRefersToFormula("hiddenMapping!$C$1:$C$" + creditCustVendPendingInvoices.size());
        XSSFDataValidationConstraint invoiceListConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("invoice_list");
        CellRangeAddressList invoicesList = new CellRangeAddressList(rowCount, rowCount, 4, 4);
        XSSFDataValidation invoicesListValidation = (XSSFDataValidation) dvHelper
                .createValidation(invoiceListConstraint, invoicesList);
        invoicesListValidation.setShowErrorBox(true);
        sheets.addValidationData(invoicesListValidation);

        mainRow.createCell(5).setCellValue(createHelper.createRichTextString("1234"));

        CellRangeAddressList typeOfPaymentList = new CellRangeAddressList(rowCount, rowCount, 6, 6);// cash/credit
        XSSFDataValidation typeOfPaymentValidation = (XSSFDataValidation) dvHelper.createValidation(typeOfPayment,
                typeOfPaymentList);
        typeOfPaymentValidation.setShowErrorBox(true);
        sheets.addValidationData(typeOfPaymentValidation);

        Name namedCell3 = workbook.createName();
        namedCell3.setNameName("branch_bank_list");
        namedCell3.setRefersToFormula("hiddenMapping!$D$1:$D$" + branchList.size());
        XSSFDataValidationConstraint branchBankConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("branch_bank_list");
        CellRangeAddressList branchBankNameList = new CellRangeAddressList(rowCount, rowCount, 7, 7);
        XSSFDataValidation branchBankNameValidation = (XSSFDataValidation) dvHelper
                .createValidation(branchBankConstraint, branchBankNameList);
        branchBankNameValidation.setShowErrorBox(true);
        sheets.addValidationData(branchBankNameValidation);

        mainRow.createCell(8).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        dateStyle.setDataFormat(poiFormat.getFormat(excelFormatPattern));
        mainRow.createCell(9).setCellStyle(dateStyle);

        mainRow.createCell(10).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        mainRow.createCell(11).setCellValue(createHelper.createRichTextString("ABCEDEF"));

        for (int b = 0; b < mainRow.getLastCellNum(); b++) {
            sheets.autoSizeColumn(b);
        }

        FileOutputStream fileOut = new FileOutputStream(path);
        workbook.write(fileOut);
        fileOut.close();
        log.log(Level.FINE, "===============End ");
        return sheetName;

    }

    @Override
    public String createOrgTransactionSellAndRecieveAdvanceDataXlsx(Users user, EntityManager em, String path,
            String sheetName, Date fromTransDate, Date toTransDate) {

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheets = workbook.createSheet(sheetName);
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheets);
        final Integer MAX_COLUMN = 70;
        Cell dataCell[] = new Cell[MAX_COLUMN];
        CellStyle unlockedCellStyle = workbook.createCellStyle();
        Row headerRow = sheets.createRow(0);
        createHelper = workbook.getCreationHelper();
        unlockedCellStyle.setLocked(false);
        // Set Header for Sell
        String[] header = null;
        for (int i = 0; i < MAX_COLUMN; i++) {
            dataCell[i] = headerRow.createCell(i);
        }
        header = new String[MAX_COLUMN];
        header[0] = "Comapny Code";
        header[1] = "Company Name";
        header[2] = "State";
        header[3] = "GSTIN";
        header[4] = "Year";
        header[5] = "Month";
        header[6] = "Accounting Document No";
        header[7] = "Accounting Document Date";
        header[8] = "Transaction Count";
        header[9] = "Currency";
        header[10] = "GL Account";
        header[11] = "Document Type";
        header[12] = "Taxability";
        header[13] = "Nature of Exemption";
        header[14] = "Supply Type";
        header[15] = "Customer Code";
        header[16] = "Nature Of Recipient";
        header[17] = "GSTIN Of Recipient";
        header[18] = "Recipient State";
        header[19] = "Name Of The Recipient";
        header[20] = "Invoice/Dr Note/Cr Note/Receipt Voucher/Refund Voucher(No)";
        header[21] = "Invoice/Dr Note/Cr Note/Receipt Voucher/Refund Voucher(Date)";
        header[22] = "Invoice/Dr Note/Cr Note/Receipt Voucher/Refund Voucher(Value)";
        header[23] = "Supply attract reverse charge";
        header[24] = "POS (State)";
        header[25] = "GSTIN of e-commerce portal";
        header[26] = "Line Item";
        header[27] = "Item Code";
        header[28] = "Category";
        header[29] = "HSN/SAC";
        header[30] = "Product/Service Description";
        header[31] = "UQC";
        header[32] = "Quantity";
        header[33] = "Sale price (Before discount)";
        header[34] = "Discount";
        header[35] = "Net sale price (after discount)";
        header[36] = "VAT";
        header[37] = "Central Excise";
        header[38] = "State Excise";
        header[39] = "Taxable value";
        header[40] = "Total GST Rate";
        header[41] = "IGST (Rate)";
        header[42] = "IGST (Amt)";
        header[43] = "CGST (Rate)";
        header[44] = "CGST (Amt)";
        header[45] = "SGST/UTGST (Rate)";
        header[46] = "SGST/UTGST (Amt)";
        header[47] = "Cess (Rate)";
        header[48] = "Cess (Amount)";
        header[49] = "Ship From (State)";
        header[50] = "Ship To (State)";
        header[51] = "Way Bill No";
        header[52] = "Transporter name";
        header[53] = "Lorry Receipt number";
        header[54] = "Lorry Receipt date";
        header[55] = "Credit Note/Debit Note/Refund Voucher(Original Document No)";
        header[58] = "Credit Note/Debit Note/Refund Voucher(Original Document Date)";
        header[57] = "Reason for issuing Debit Note/ Credit Note/ Refund Voucher";
        header[58] = "Shipping Bill/ Bill of Export (No)";
        header[59] = "Shipping Bill/ Bill of Export (Date)";
        header[60] = "Port Code";
        header[61] = "Export Duty (If any)";
        header[62] = "Is advance adjustment";
        header[63] = "Advance Adjustment (Invoice No)";
        header[64] = "Advance Adjustment (Invoice Date)";
        header[65] = "Amendment (Original Year)";
        header[66] = "Amendment (Original Month)";
        header[67] = "Amendment (Original Recipients GSTIN/ UIN)";
        header[68] = "Amendment (Original Document No)";
        header[69] = "Amendment (Original Document Date)";

        //
        // Cell Creation and Formating
        for (int i = 0; i < MAX_COLUMN; i++) {
            dataCell[i] = headerRow.createCell(i);
            dataCell[i].setCellValue(createHelper.createRichTextString(header[i]));
            dataCell[i].setCellStyle(unlockedCellStyle);
        }
        XSSFCellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        for (int b = 0; b < headerRow.getLastCellNum(); b++) {
            sheets.autoSizeColumn(b);
            headerRow.getCell(b).setCellStyle(style);
            sheets.lockAutoFilter(true);
        }
        List<GSTFiling> gstFilings = null;
        try {
            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            List<Branch> branchList = genericDAO.findByCriteria(Branch.class, criterias, em);
            String stbuf1 = ("select obj from GSTFiling obj where obj.organizationId.id=?1 and obj.transactionPurpose.id in(?2,?3,?4,?5,?6,?7,?8,?9) and  obj.agentName=?10 and obj.presentStatus=1 and obj.transactionDate>=?11 and obj.transactionDate<=?12");
            ArrayList inparam1 = new ArrayList(10);
            inparam1.add(user.getOrganization().getId());
            inparam1.add(IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW);
            inparam1.add(IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER);
            inparam1.add(IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER);
            inparam1.add(IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER);
            inparam1.add(IdosConstants.CREDIT_NOTE_CUSTOMER);
            inparam1.add(IdosConstants.DEBIT_NOTE_CUSTOMER);
            inparam1.add(IdosConstants.REFUND_ADVANCE_RECEIVED);
            inparam1.add(IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER);
            // inparam1.add(Integer.parseInt("0"));
            inparam1.add("PWC");
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);
            gstFilings = genericDAO.queryWithParams(stbuf1.toString(), em, inparam1);
            List<Transaction> transactions = new ArrayList<Transaction>();
            for (GSTFiling gstFiling : gstFilings) {
                if (gstFiling.getTransactionId().getTransactionPurpose()
                        .getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                    if (gstFiling.getTransactionId().getTypeIdentifier() == 1) {
                        transactions.add(gstFiling.getTransactionId());
                    }
                } else {
                    transactions.add(gstFiling.getTransactionId());
                }
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM");
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("YYYY");
            SimpleDateFormat reportdf = new SimpleDateFormat("yyyyMMdd");
            Integer lineItem = 0;
            int rowCount = 1;
            for (Transaction transaction : transactions) {

                double totalNetAmtWithoutAdv = 0.0;
                List<TransactionItems> transactionItems = transaction.getTransactionItems();
                for (TransactionItems transactionItem : transactionItems) {
                    double netAmtWithoutAdv = transactionItem.getGrossAmount() + transactionItem.getTotalTax();
                    totalNetAmtWithoutAdv = totalNetAmtWithoutAdv + netAmtWithoutAdv;
                }

                for (TransactionItems transactionItem : transactionItems) {
                    lineItem++;
                    Row row = sheets.createRow(rowCount++);
                    String[] data = new String[MAX_COLUMN];
                    for (int i = 0; i < MAX_COLUMN; i++) {
                        dataCell[i] = row.createCell(i);
                    }
                    for (int i = 0; i < MAX_COLUMN; i++) {
                        data[i] = "";
                    }

                    Specifics spec = transactionItem.getTransactionSpecifics();
                    TransactionPurpose tranPur = transaction.getTransactionPurpose();
                    data[0] = user.getOrganization().getId().toString();
                    data[1] = user.getOrganization().getName();
                    // if(tranPur.getTransactionPurpose().getId==IdosConstants.)
                    String branchstate = IdosConstants.STATE_CODE_MAPPING
                            .get(transaction.getTransactionBranch().getStateCode());
                    data[2] = branchstate;
                    data[3] = transaction.getTransactionBranch().getGstin();
                    String month = simpleDateFormat.format(transaction.getTransactionDate()).toUpperCase();
                    String year = simpleDateFormat2.format(transaction.getTransactionDate()).toUpperCase();
                    data[4] = year;
                    data[5] = month;
                    data[6] = transaction.getTransactionRefNumber();

                    if (transaction.getTransactionDate() != null) {
                        data[7] = reportdf.format(transaction.getTransactionDate());
                    }

                    data[8] = "1";
                    // 9
                    Integer currency = transaction.getTransactionBranch().getCountry();
                    if (currency != null && !currency.equals("")) {
                        IDOSCountry country = IDOSCountry.findById(currency.longValue());
                        if (country != null) {
                            String currINR = country.getCurrencyCode();
                            data[9] = "currINR";
                        }
                    } else if (tranPur.getId() == IdosConstants.CREDIT_NOTE_CUSTOMER
                            || tranPur.getId() == IdosConstants.DEBIT_NOTE_CUSTOMER
                            || tranPur.getId() == IdosConstants.REFUND_ADVANCE_RECEIVED
                            || tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        data[9] = "INR";
                    }
                    // 10
                    data[10] = spec.getAccountCode().toString();
                    // 11
                    String documentType = "";
                    if (tranPur.getId() == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                            || tranPur.getId() == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER) {
                        documentType = "Invoice";
                    } else if (tranPur.getId() == IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER
                            || tranPur.getId() == IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER) {
                        documentType = "Advance Receipt Voucher\"";
                    } else if (tranPur.getId() == IdosConstants.CREDIT_NOTE_CUSTOMER) {
                        documentType = "Credit Note";
                    } else if (tranPur.getId() == IdosConstants.DEBIT_NOTE_CUSTOMER) {
                        documentType = "Debit Note";
                    } else if (tranPur.getId() == IdosConstants.REFUND_ADVANCE_RECEIVED) {
                        documentType = "Refund Voucher";
                    } else if (tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        documentType = "Invoice";
                    }
                    if (transaction.getWithWithoutTax() != null) {
                        if (transaction.getWithWithoutTax() == 2) {
                            documentType = "Not Taxable";
                        } else if (transaction.getWithWithoutTax() == 1) {
                            documentType = "Taxable";
                        }
                    } else if (spec.getGstItemCategory() == null) {
                        documentType = "Taxable";
                    } else {
                        documentType = "Not Taxable";
                    }
                    data[11] = documentType;
                    // 12
                    String taxability = "";
                    if (tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        if (transaction.getTransactionBranch() != null
                                && transaction.getTransactionToBranch() != null) {
                            if (transaction.getTransactionBranch().getStateCode() != null
                                    && transaction.getTransactionToBranch() != null) {
                                if (transaction.getTransactionBranch().getStateCode()
                                        .equals(transaction.getTransactionToBranch().getStateCode())) {
                                    taxability = "intra-state";
                                } else {
                                    taxability = "inter-state";
                                }
                            }
                        }
                    } else if (transaction.getSourceGstin() != null && transaction.getDestinationGstin() != null
                            && transaction.getSourceGstin() != "" && transaction.getDestinationGstin() != "") {
                        if (transaction.getSourceGstin().substring(0, 2)
                                .equals(transaction.getDestinationGstin().substring(0, 2))) {
                            taxability = "intra-state";
                        } else {
                            taxability = "inter-state";
                        }
                    }
                    data[12] = taxability;
                    // 1
                    if (tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        data[15] = transaction.getTransactionToBranch().getId().toString();
                        if (transaction.getTransactionToBranch().getGstin() != null) {
                            data[13] = "Registered";
                            data[17] = transaction.getTransactionToBranch().getGstin();
                        } else {
                            data[13] = "Un-Registered";// Unregistered
                        }
                        data[18] = IdosConstants.STATE_CODE_MAPPING
                                .get(transaction.getTransactionToBranch().getStateCode());
                        data[19] = transaction.getTransactionToBranch().getName();
                    } else if (transaction.getTransactionVendorCustomer() != null) {
                        data[15] = transaction.getTransactionVendorCustomer().getId().toString();
                        if (transaction.getTransactionVendorCustomer().getIsRegistered() != null) {
                            if (transaction.getTransactionVendorCustomer().getIsRegistered() == 1) {
                                data[13] = "Registered";
                                data[17] = transaction.getTransactionVendorCustomer().getGstin();
                            } else {
                                data[13] = "Un-Registered"; // Unregistered
                                row.createCell(15).setCellValue(""); // blank gstin
                            }
                        }
                        if (transaction.getDestinationGstin() != "") {
                            data[18] = IdosConstants.STATE_CODE_MAPPING
                                    .get(transaction.getDestinationGstin().substring(0, 2));
                        } else {
                            data[18] = IdosConstants.STATE_CODE_MAPPING
                                    .get(transaction.getTransactionBranch().getGstin().substring(0, 2));
                        }
                        data[19] = transaction.getTransactionVendorCustomer().getName();
                    } else {
                        // Unregistered
                    }

                    // 20
                    if (transaction.getInvoiceNumber() != null) {
                        data[20] = transaction.getInvoiceNumber();
                    }
                    // 21
                    if (transaction.getTransactionDate() != null) {
                        data[21] = reportdf.format(transaction.getTransactionDate());
                    }
                    // 22
                    if (tranPur.getId().equals(IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER)) {
                        data[22] = IdosConstants.decimalFormat.format(transaction.getGrossAmount());
                    } else {
                        data[22] = IdosConstants.decimalFormat.format(totalNetAmtWithoutAdv);
                    }
                    // 23
                    if (transaction.getTypeOfSupply() != null && transaction.getTypeOfSupply() == 2) { // Supply
                                                                                                       // applicable for
                                                                                                       // Reverse Charge
                        data[23] = "Yes";
                    } else {
                        data[23] = "No";
                    }
                    // 24
                    String destGSTIN = transaction.getDestinationGstin();
                    String vendorstate = null;
                    if (destGSTIN != null && destGSTIN != "") {
                        String destGSTINStateCode = destGSTIN.substring(0, 2);
                        vendorstate = IdosConstants.STATE_CODE_MAPPING.get(destGSTINStateCode);
                        data[24] = vendorstate;
                    }
                    // 25
                    // *************GSTIN of ecommerce
                    // 26
                    data[26] = lineItem.toString();
                    // 27
                    data[27] = spec.getId().toString();
                    // 28
                    if (spec.getGstTypeOfSupply() != null) {
                        if (spec.getGstTypeOfSupply().equalsIgnoreCase("GOODS")) {
                            data[28] = "G";
                        } else {
                            data[28] = "S";
                        }
                    }
                    // 29
                    if (spec.getGstItemCode() != null) {
                        data[29] = spec.getGstItemCode();
                    }
                    // 30
                    if (spec.getGstDesc() != null) {
                        data[30] = spec.getGstDesc();
                    }
                    // 31
                    if (spec.getIncomeUnitsMeasure() != null) {
                        data[31] = spec.getIncomeUnitsMeasure();
                    }
                    // 32,33,34,35
                    if (tranPur.getTransactionPurpose().equals("Receive advance from customer")) {
                        data[32] = "1";
                        data[33] = transactionItem.getGrossAmount().toString();
                        data[34] = "";
                        data[35] = transactionItem.getGrossAmount().toString();
                    } else if (tranPur.getTransactionPurpose().equals("Refund Advance Received")) {
                        data[32] = "";
                        data[33] = "";
                        data[34] = "";
                        data[35] = "";
                    } else {
                        data[32] = transactionItem.getNoOfUnits().toString();
                        data[33] = transactionItem.getPricePerUnit().toString();
                        Double discountPerUnit = transactionItem.getDiscountAmount() / transactionItem.getNoOfUnits();
                        data[34] = String.valueOf(discountPerUnit);
                        Double amount = transactionItem.getPricePerUnit() - discountPerUnit;
                        data[35] = amount.toString();
                    }
                    // 36
                    data[36] = "0";
                    // 37
                    data[37] = "0";
                    // 38
                    data[38] = "0";
                    // 39
                    data[39] = "0";
                    transactionItem.getGrossAmount().toString();
                    // 40
                    if (transactionItem.getTaxName1() != null || transactionItem.getTaxName2() != null
                            || transactionItem.getTaxName3() != null) {
                        if (spec.getGstTaxRate() != null) {
                            data[40] = spec.getGstTaxRate().toString();
                        }
                    }
                    // 41,42
                    if (transactionItem.getTaxName3() != null && transactionItem.getTaxName3().indexOf("IGST") != -1) {
                        data[41] = transactionItem.getTaxRate3().toString();
                        data[42] = transactionItem.getTaxValue3().toString();
                    }
                    // 43,44
                    if (transactionItem.getTaxName2() != null && transactionItem.getTaxName2().indexOf("CGST") != -1) {
                        data[43] = transactionItem.getTaxRate2().toString();
                        data[44] = transactionItem.getTaxValue2().toString();
                    }
                    // 45,46
                    if (transactionItem.getTaxName1() != null && transactionItem.getTaxName1().indexOf("SGST") != -1) {
                        data[45] = transactionItem.getTaxRate1().toString();
                        data[46] = transactionItem.getTaxValue1().toString();
                    }
                    // 47,48
                    if (transactionItem.getTaxName4() != null && transactionItem.getTaxName4().indexOf("CESS") != -1) {
                        data[47] = transactionItem.getTaxRate4().toString();
                        data[48] = transactionItem.getTaxValue4().toString();
                    }
                    // 49,50
                    if (tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        data[49] = vendorstate;
                        data[50] = branchstate;
                    } else {
                        if (vendorstate != null) {
                            data[49] = vendorstate;
                        }
                        if (branchstate != null) {
                            data[50] = branchstate;
                        }
                    }
                    // 51 to 54
                    // NOT SET

                    // 55 56
                    if (tranPur.getTransactionPurpose().equals("Refund Advance Received")
                            || tranPur.getId().equals(IdosConstants.DEBIT_NOTE_CUSTOMER)
                            || tranPur.getId().equals(IdosConstants.CREDIT_NOTE_CUSTOMER)) {
                        if (transaction.getLinkedTxnRef() != null) {
                            List<Transaction> findByTxnReference = Transaction.findByTxnReference(em,
                                    user.getOrganization().getId(), transaction.getLinkedTxnRef());
                            if (findByTxnReference != null && !findByTxnReference.isEmpty()) {
                                Transaction transactionRef = findByTxnReference.get(0);
                                data[55] = transactionRef.getInvoiceNumber();
                                data[56] = reportdf.format(transactionRef.getTransactionDate());
                            }
                        }
                    }
                    // 57,58,59,60,61
                    // no data set
                    // 62,63,64
                    if (tranPur.getId().equals(IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW)
                            || tranPur.getId().equals(IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER)) {
                        String advanceVoucherNumber = "";
                        Date voucherDate = null;
                        List<AdvanceAdjustmentDetail> advanceAdjustmentDetails = AdvanceAdjustmentDetail
                                .findListByTxn(entityManager, transaction.getId());
                        if (advanceAdjustmentDetails != null && advanceAdjustmentDetails.size() != 0) {
                            for (AdvanceAdjustmentDetail advanceAdjustmentDetail : advanceAdjustmentDetails) {
                                if (advanceAdjustmentDetail.getAdjustedAmount() != 0) {
                                    advanceVoucherNumber = advanceAdjustmentDetail.getAdvTransaction()
                                            .getInvoiceNumber();
                                    voucherDate = advanceAdjustmentDetail.getAdvTransaction().getTransactionDate();
                                }
                            }
                            data[62] = "Yes";
                            data[63] = advanceVoucherNumber;
                            if (voucherDate != null) {
                                data[64] = reportdf.format(voucherDate);
                            }
                        }
                    } // path = path + fileName;
                      // Not set till 70
                    for (int i = 0; i < MAX_COLUMN; i++) {
                        dataCell[i] = row.createCell(i);
                        dataCell[i].setCellValue(createHelper.createRichTextString(data[i]));
                    }
                }
                lineItem = 0;
            }

            FileOutputStream fileOut = new FileOutputStream(path);
            workbook.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error", e);
        }

        try {
            log.log(Level.INFO, "gst filing=" + gstFilings.size());
            if (gstFilings.size() != 0) {
                for (GSTFiling gstFiling : gstFilings) {
                    gstFiling.setGstFilingStatus(1);
                    log.log(Level.INFO, " inside set filing status");
                    log.log(Level.INFO, "get the status=" + gstFiling.getGstFilingStatus());
                    genericDAO.saveOrUpdate(gstFiling, user, em);
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "not able to upload", e);
            log.log(Level.INFO, "inside catch");
        }
        return sheetName;
    }

    @Override
    public String createOrgBuySideTransactionDataXlsx(Users user, EntityManager em, String path,
            String sheetName, Date fromTransDate, Date toTransDate) throws Exception {

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheets = workbook.createSheet(sheetName);
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheets);
        final Integer MAX_COLUMN = 78;
        Cell dataCell[] = new Cell[MAX_COLUMN];
        CellStyle unlockedCellStyle = workbook.createCellStyle();
        Row headerRow = sheets.createRow(0);
        createHelper = workbook.getCreationHelper();
        unlockedCellStyle.setLocked(false);
        // Set Header for Sell
        String[] header = null;
        for (int i = 0; i < MAX_COLUMN; i++) {
            dataCell[i] = headerRow.createCell(i);
        }
        header = new String[MAX_COLUMN];
        List<GSTFiling> gstFilings = new ArrayList<GSTFiling>();
        List<GSTFiling> gstFilingsForTranscations = null;
        List<GSTFiling> gstFilingsForClaimTransactions = null;
        try {
            header[0] = "Comapny Code";
            header[1] = "Company Name";
            header[2] = "State";
            header[3] = "GSTIN";
            header[4] = "Year";
            header[5] = "Month";
            header[6] = "Accounting Document No";
            header[7] = "Accounting Document Date";
            header[8] = "Transaction Count";
            header[9] = "Currency";
            header[10] = "GL Account";
            header[11] = "Document Type";
            header[12] = "Taxability";
            header[13] = "Supply Type";
            header[14] = "Supplier Code";
            header[15] = "Nature Of Supplier";
            header[16] = "GSTIN Of Supplier";
            header[17] = "Supplier State";
            header[18] = "Name Of The Supplier";
            header[19] = "Invoice/Dr Note/Cr Note/Receipt Voucher/Refund Voucher(No)";
            header[20] = "Invoice/Dr Note/Cr Note/Receipt Voucher/Refund Voucher(Date)";
            header[21] = "Invoice/Dr Note/Cr Note/Receipt Voucher/Refund Voucher(Value)";
            header[22] = "Supply attract reverse charge";
            header[23] = "POS (State)";
            header[24] = "Line Item";
            header[25] = "Item Code";
            header[26] = "Category";
            header[27] = "HSN/SAC";
            header[28] = "Product/Service Description";
            header[29] = "UQC";
            header[30] = "Quantity";
            header[31] = "Sale price (Before discount)";
            header[32] = "Discount";
            header[33] = "Net sale price (after discount)";
            header[34] = "VAT";
            header[35] = "Central Excise";
            header[36] = "State Excise";
            header[37] = "Taxable value";
            header[38] = "Total GST Rate";
            header[39] = "IGST (Rate)";
            header[40] = "IGST (Amt)";
            header[41] = "CGST (Rate)";
            header[42] = "CGST (Amt)";
            header[43] = "SGST/UTGST (Rate)";
            header[44] = "SGST/UTGST (Amt)";
            header[45] = "Cess (Rate)";
            header[46] = "Cess (Amount)";
            header[47] = "Eligibilty of ITC";
            header[48] = "ITC IGST (Amt)";
            header[49] = "ITC CGST (Amt)";
            header[50] = "ITC SGST (Amt)";
            header[51] = "ITC Cess (Amt)";
            header[52] = "Nature Of expense";
            header[53] = "Ship From (State)";
            header[54] = "Ship To (State)";
            header[55] = "Way Bill No";
            header[56] = "Transporter name";
            header[57] = "Lorry Receipt number";
            header[58] = "Lorry Receipt date";
            header[59] = "Credit/Debit Note(Original  Invoice No.)";
            header[60] = "Credit/Debit Note(Original  Invoice Date)";
            header[61] = "Reason for issuing Debit Note/ Credit Note";
            header[62] = "Assessable value before BCD";
            header[63] = "Basic Custom Duty";
            header[64] = "Port Code";
            header[65] = "Is advance adjustment";
            header[66] = "Advance Adjustment (Invoice No)";
            header[67] = "Advance Adjustment (Invoice Date)";
            header[68] = "Is Amendment";
            header[69] = "Amendment (Original Year)";
            header[70] = "Amendment (Original Month)";
            header[71] = "Amendment (Original  GSTIN of Supplier)";
            header[72] = "Amendment (Original Document No)";
            header[73] = "Amendment (Original Document Date)";
            header[74] = "Is ISD";
            header[75] = "ISD (Location Details)";
            // Cell Creation and Formating
            for (int i = 0; i < MAX_COLUMN; i++) {
                dataCell[i] = headerRow.createCell(i);
                dataCell[i].setCellValue(createHelper.createRichTextString(header[i]));
                dataCell[i].setCellStyle(unlockedCellStyle);
            }
            XSSFCellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            for (int b = 0; b < headerRow.getLastCellNum(); b++) {
                sheets.autoSizeColumn(b);
                headerRow.getCell(b).setCellStyle(style);
                sheets.lockAutoFilter(true);
            }

            String stbuf1 = ("select obj from GSTFiling obj where obj.organizationId.id=?1 and  obj.transactionPurpose.id in (?2,?3,?4,?5,?6,?7) and  obj.agentName=?8 and obj.presentStatus=1 and obj.transactionDate>=?9 and obj.transactionDate<=?10");
            ArrayList inparam1 = new ArrayList(10);
            inparam1.add(user.getOrganization().getId());
            inparam1.add(IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY);
            inparam1.add(IdosConstants.BUY_ON_CREDIT_PAY_LATER);
            inparam1.add(IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT);
            inparam1.add(IdosConstants.CREDIT_NOTE_VENDOR);
            inparam1.add(IdosConstants.DEBIT_NOTE_VENDOR);
            inparam1.add(IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER);
            // inparam1.add(Integer.parseInt("0"));
            inparam1.add("PWC");
            inparam1.add(fromTransDate);
            inparam1.add(toTransDate);
            gstFilingsForTranscations = genericDAO.queryWithParams(stbuf1.toString(), em, inparam1);
            List<Transaction> transactions = new ArrayList<Transaction>();
            for (GSTFiling gstFiling : gstFilingsForTranscations) {
                if (gstFiling.getTransactionId().getTransactionPurpose()
                        .getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                    if (gstFiling.getTransactionId().getTypeIdentifier() == 2) {
                        transactions.add(gstFiling.getTransactionId());
                    }
                } else {
                    transactions.add(gstFiling.getTransactionId());
                }
            }
            String stbuf2 = ("select obj from GSTFiling obj where obj.organizationId.id=?1 and  (obj.transactionPurpose.id=?2 or obj.transactionPurpose.id=?3 or obj.transactionPurpose.id=?4) and  obj.agentName=?5 and obj.presentStatus=1 and obj.transactionDate>=?6 and obj.transactionDate<=?7");
            ArrayList inparam2 = new ArrayList(7);
            inparam2.add(user.getOrganization().getId());
            inparam2.add(IdosConstants.SETTLE_TRAVEL_ADVANCE);
            inparam2.add(IdosConstants.SETTLE_ADVANCE_FOR_EXPENSE);
            inparam2.add(IdosConstants.REQUEST_FOR_EXPENSE_REIMBURSEMENT);
            // inparam2.add(Integer.parseInt("0"));
            inparam2.add("PWC");
            inparam2.add(fromTransDate);
            inparam2.add(toTransDate);
            gstFilingsForClaimTransactions = genericDAO.queryWithParams(stbuf2.toString(), em, inparam2);
            List<ClaimTransaction> claimtransactions = new ArrayList<ClaimTransaction>();
            for (GSTFiling gstFiling : gstFilingsForClaimTransactions) {
                claimtransactions.add(gstFiling.getClaimTransactionId());
            }
            if (gstFilingsForTranscations != null) {
                gstFilings.addAll(gstFilingsForTranscations);
            }
            if (gstFilingsForClaimTransactions != null) {
                gstFilings.addAll(gstFilingsForClaimTransactions);
            }
            log.log(Level.INFO, "claim size=" + claimtransactions.size());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM");
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("YYYY");
            SimpleDateFormat reportdf = new SimpleDateFormat("yyyyMMdd");
            Integer lineItem = 0;
            Integer rowCount = 1;
            for (Transaction transaction : transactions) {
                double totalNetAmtWithoutAdv = 0.0;
                List<TransactionItems> transactionItems = transaction.getTransactionItems();
                for (TransactionItems transactionItem : transactionItems) {
                    double netAmtWithoutAdv = transactionItem.getGrossAmount() + transactionItem.getTotalTax();
                    totalNetAmtWithoutAdv = totalNetAmtWithoutAdv + netAmtWithoutAdv;
                }
                for (TransactionItems transactionItem : transactionItems) {
                    lineItem++;
                    Row row = sheets.createRow(rowCount++);
                    String[] data = new String[MAX_COLUMN];
                    for (int i = 0; i < MAX_COLUMN; i++) {
                        dataCell[i] = row.createCell(i);
                    }
                    for (int i = 0; i < MAX_COLUMN; i++) {
                        data[i] = "";
                    }

                    String branchTostate = "";
                    Specifics spec = transactionItem.getTransactionSpecifics();
                    TransactionPurpose tranPur = transaction.getTransactionPurpose();
                    data[0] = user.getOrganization().getId().toString();
                    data[1] = user.getOrganization().getName();
                    // 2,3
                    if (tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        branchTostate = IdosConstants.STATE_CODE_MAPPING
                                .get(transaction.getTransactionToBranch().getStateCode());
                        data[2] = branchTostate;
                        data[3] = transaction.getTransactionToBranch().getGstin();
                    } else {
                        branchTostate = IdosConstants.STATE_CODE_MAPPING
                                .get(transaction.getTransactionBranch().getStateCode());
                        data[2] = branchTostate;
                        data[3] = transaction.getTransactionBranch().getGstin();
                    }
                    // 4,5,6,7,8
                    String month = simpleDateFormat.format(transaction.getTransactionDate()).toUpperCase();
                    String year = simpleDateFormat2.format(transaction.getTransactionDate()).toUpperCase();
                    data[4] = year;
                    data[5] = month;
                    data[6] = transaction.getTransactionRefNumber();
                    data[7] = reportdf.format(transaction.getTransactionDate());
                    data[8] = "1";
                    // 9
                    if (transaction.getTransactionBranch().getCountry() != null
                            && !transaction.getTransactionBranch().getCountry().equals("")) {
                        IDOSCountry country = IDOSCountry
                                .findById(transaction.getTransactionBranch().getCountry().longValue());
                        if (country != null) {
                            String currINR = country.getCurrencyCode();
                            data[9] = currINR;
                        }
                    } else if (tranPur.getId() == IdosConstants.CREDIT_NOTE_VENDOR
                            || tranPur.getId() == IdosConstants.DEBIT_NOTE_VENDOR
                            || tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        data[9] = "INR";
                    }
                    // 10

                    data[10] = spec.getAccountCode().toString();

                    // 11
                    String documentType = "";
                    if (tranPur.getId() == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT
                            || tranPur.getId() == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                            || tranPur.getId() == IdosConstants.BUY_ON_CREDIT_PAY_LATER
                            || tranPur.getId() == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) {
                        documentType = "Invoice";
                    } else if (tranPur.getId() == IdosConstants.CREDIT_NOTE_VENDOR) {
                        documentType = "Credit Note";
                    } else if (tranPur.getId() == IdosConstants.DEBIT_NOTE_VENDOR) {
                        documentType = "Debit Note";
                    } else if (tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        documentType = "Invoice";
                    }
                    if (spec.getGstItemCategory() == null) {
                        documentType = "Taxable";
                    } else {
                        documentType = "Not Taxable";
                    }
                    data[11] = documentType;
                    // 12
                    if (transaction.getSourceGstin() != null && transaction.getDestinationGstin() != null) {
                        if (transaction.getSourceGstin().substring(0, 2)
                                .equals(transaction.getDestinationGstin().substring(0, 2))) {
                            data[12] = "intra-state";
                        } else {
                            data[12] = "inter-state";
                        }
                    }
                    // 13,14,15,16,17,18
                    if (tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        data[13] = transaction.getTransactionBranch().getName();
                        data[14] = transaction.getTransactionBranch().getId().toString();
                        if (transaction.getTransactionBranch().getGstin() != null) {
                            data[15] = "Registered";
                            data[16] = transaction.getTransactionBranch().getGstin();
                        } else {
                            data[15] = "Un-Registered";// Unregistered
                        }

                        data[17] = IdosConstants.STATE_CODE_MAPPING
                                .get(transaction.getTransactionBranch().getStateCode());
                        data[18] = transaction.getTransactionBranch().getName();
                    } else if (transaction.getTransactionUnavailableVendorCustomer() != ""
                            && transaction.getTransactionUnavailableVendorCustomer() != null) {
                        data[13] = transaction.getTransactionUnavailableVendorCustomer();
                        if (transaction.getWalkinCustomerType() == 1 || transaction.getWalkinCustomerType() == 2) {
                            data[15] = "Registered";
                            data[16] = transaction.getDestinationGstin();
                        } else {
                            data[15] = "Un-Registered"; // Unregistered
                        }
                        if (transaction.getDestinationGstin() != "") {
                            data[17] = IdosConstants.STATE_CODE_MAPPING
                                    .get(transaction.getDestinationGstin().substring(0, 2));
                        } else {
                            data[17] = IdosConstants.STATE_CODE_MAPPING
                                    .get(transaction.getTransactionBranch().getGstin().substring(0, 2));
                        }
                        data[18] = transaction.getTransactionUnavailableVendorCustomer();
                    } else if (transaction.getTransactionVendorCustomer() != null) {
                        data[13] = transaction.getTransactionVendorCustomer().getName();
                        data[14] = transaction.getTransactionVendorCustomer().getId().toString();
                        if (transaction.getTransactionVendorCustomer().getIsRegistered() != null) {
                            if (transaction.getTransactionVendorCustomer().getIsRegistered() == 1) {
                                data[15] = "Registered";
                                data[16] = transaction.getTransactionVendorCustomer().getGstin();
                            } else {
                                data[15] = "Un-Registered"; // Unregistered
                            }
                        }
                        if (transaction.getDestinationGstin() != "") {
                            data[17] = IdosConstants.STATE_CODE_MAPPING
                                    .get(transaction.getDestinationGstin().substring(0, 2));
                        } else {
                            data[17] = IdosConstants.STATE_CODE_MAPPING
                                    .get(transaction.getTransactionBranch().getGstin().substring(0, 2));
                        }
                        data[18] = transaction.getTransactionVendorCustomer().getName();
                    }
                    // 19,20
                    String invoiceNo = "";
                    String invoiceDate = "";
                    Map<String, Object> criteria = new HashMap<String, Object>();
                    criteria.put("organization.id", transaction.getTransactionBranchOrganization().getId());
                    criteria.put("transaction.id", transaction.getId());
                    criteria.put("presentStatus", 1);
                    // criterias.put("invoiceNumber", txn.getInvoiceNumber());
                    TransactionInvoice transationInvoice = genericDAO.getByCriteria(TransactionInvoice.class, criteria,
                            em);
                    if (transationInvoice != null) {
                        if (transationInvoice.getInvRefNumber() != null && transationInvoice.getInvRefNumber() != "") {
                            invoiceNo = transationInvoice.getInvRefNumber();
                            data[19] = invoiceNo;
                        }
                        if (transationInvoice.getInvRefDate() != null) {
                            invoiceDate = IdosConstants.IDOSDF.format(transationInvoice.getInvRefDate());
                            data[20] = invoiceDate;
                        }
                    }
                    // 21
                    data[21] = String.valueOf(totalNetAmtWithoutAdv);
                    // 22
                    if (transaction.getTypeOfSupply() != null) {
                        if (transaction.getTypeOfSupply() == 1 || transaction.getTypeOfSupply() == 0) { // Supply
                                                                                                        // applicable
                                                                                                        // for Reverse
                                                                                                        // Charge
                            data[22] = "No";
                        } else {
                            data[22] = "Yes";
                        }
                    }

                    // 23,24,25
                    String sourceGSTIN = transaction.getSourceGstin();
                    String vendorstate = null;
                    if (sourceGSTIN != null) {
                        String sourceGSTINStateCode = sourceGSTIN.substring(0, 2);
                        vendorstate = IdosConstants.STATE_CODE_MAPPING.get(sourceGSTINStateCode);
                        data[23] = vendorstate;
                    }
                    data[24] = lineItem.toString();
                    data[25] = spec.getId().toString();
                    // 26,27,28
                    if (spec.getGstTypeOfSupply() != null) {
                        if (spec.getGstTypeOfSupply().equalsIgnoreCase("GOODS")) {
                            data[26] = "G";
                        } else {
                            data[26] = "S";
                        }
                    }
                    if (spec.getGstItemCode() != null) {
                        data[27] = spec.getGstItemCode();
                    }
                    if (spec.getGstDesc() != null) {
                        data[28] = spec.getGstDesc();
                    }
                    // 29,30,31,32,33
                    // ***********not storing item desc
                    if (spec.getExpenseUnitsMeasure() != null) {
                        data[29] = spec.getExpenseUnitsMeasure();
                    }
                    data[30] = transactionItem.getNoOfUnits().toString();
                    data[31] = transactionItem.getPricePerUnit().toString();
                    data[32] = "";
                    data[33] = transactionItem.getPricePerUnit().toString();
                    // 34,35,36,
                    // No data

                    // 37
                    data[37] = transactionItem.getGrossAmount().toString();
                    // 38
                    if (transactionItem.getTaxRate2() != null && transactionItem.getTaxRate1() != null) {
                        Double tax = transactionItem.getTaxRate2() + transactionItem.getTaxRate1();
                        data[38] = tax.toString();
                    } else if (transactionItem.getTaxRate3() != null) {
                        Double tax = transactionItem.getTaxRate3();
                        data[38] = tax.toString();
                    }
                    // 39,40
                    if (transactionItem.getTaxName3() != null && transactionItem.getTaxName3().indexOf("IGST") != -1) {
                        data[39] = transactionItem.getTaxRate3().toString();
                        data[40] = transactionItem.getTaxValue3().toString();
                    }
                    // 41,42
                    if (transactionItem.getTaxName2() != null && transactionItem.getTaxName2().indexOf("CGST") != -1) {
                        data[41] = transactionItem.getTaxRate2().toString();
                        data[42] = transactionItem.getTaxValue2().toString();
                    }
                    // 43,44
                    if (transactionItem.getTaxName1() != null && transactionItem.getTaxName1().indexOf("SGST") != -1) {
                        data[43] = transactionItem.getTaxRate1().toString();
                        data[44] = transactionItem.getTaxValue1().toString();
                    }
                    // 45,46
                    if (transactionItem.getTaxName4() != null && transactionItem.getTaxName4().indexOf("CESS") != -1) {
                        data[45] = transactionItem.getTaxRate4().toString();
                        data[46] = transactionItem.getTaxValue4().toString();
                    }
                    // 47,48,49,50,51
                    data[47] = "IP";
                    if (transactionItem.getTaxName3() != null && transactionItem.getTaxName3().indexOf("IGST") != -1) {
                        data[48] = transactionItem.getTaxValue3().toString();
                    }
                    if (transactionItem.getTaxName2() != null && transactionItem.getTaxName2().indexOf("CGST") != -1) {
                        data[49] = transactionItem.getTaxValue2().toString();
                    }
                    if (transactionItem.getTaxName1() != null && transactionItem.getTaxName1().indexOf("SGST") != -1) {
                        data[50] = transactionItem.getTaxValue1().toString();
                    }
                    if (transactionItem.getTaxName4() != null && transactionItem.getTaxName4().indexOf("CESS") != -1) {
                        data[51] = transactionItem.getTaxValue4().toString();
                    }
                    // 52,53,54
                    data[52] = "Exclusively For Taxable/Zero Rated Supplies";
                    if (tranPur.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        data[53] = vendorstate;
                    } else if (null != transaction.getTransactionVendorCustomer()) {
                        data[53] = transaction.getTransactionVendorCustomer().getCountryState();
                    }
                    data[54] = branchTostate;
                    // 55,56,57,58
                    // No data
                    // 59,60
                    if (tranPur.getId() == IdosConstants.CREDIT_NOTE_VENDOR
                            || tranPur.getId() == IdosConstants.DEBIT_NOTE_VENDOR) {

                        if (transaction.getLinkedTxnRef() != null) {
                            List<Transaction> findByTxnReference = Transaction
                                    .findByTxnReference(entityManager, user.getOrganization().getId(),
                                            transaction.getLinkedTxnRef());
                            if (findByTxnReference != null && !findByTxnReference.isEmpty()) {
                                Transaction transactionRef = findByTxnReference.get(0);
                                String invoiceNo1 = "";
                                String invoiceDate1 = "";
                                Map<String, Object> criteria1 = new HashMap<String, Object>();
                                criteria1.put("organization.id",
                                        transaction.getTransactionBranchOrganization().getId());
                                criteria1.put("transaction.id", transactionRef.getId());
                                criteria1.put("presentStatus", 1);
                                // criterias.put("invoiceNumber", txn.getInvoiceNumber());
                                TransactionInvoice transationInvoice1 = genericDAO
                                        .getByCriteria(TransactionInvoice.class, criteria1, em);
                                if (transationInvoice1 != null) {
                                    if (transationInvoice1.getInvRefNumber() != null
                                            && transationInvoice1.getInvRefNumber() != "") {
                                        invoiceNo1 = transationInvoice1.getInvRefNumber();
                                        data[59] = invoiceNo1;
                                    }
                                    if (transationInvoice1.getInvRefDate() != null) {
                                        invoiceDate1 = IdosConstants.IDOSDF.format(transationInvoice1.getInvRefDate());
                                        data[60] = invoiceDate1;
                                    }
                                }
                            }
                        }
                    }
                    // 61,62,63,64

                    // *********shipping bill no
                    if (transaction.getTypeOfSupply() != null) {
                        if (transaction.getTypeOfSupply() != 1 && transaction.getTypeOfSupply() != 0) {
                            data[62] = transactionItem.getGrossAmount().toString();
                        }
                        if (transaction.getTypeOfSupply() != 1 && transactionItem.getTaxValue7() != null) {
                            data[63] = transactionItem.getTaxValue7().toString();
                        }
                    }

                    // *********shipping bill no

                    if (transationInvoice.getPortCode() != null) {
                        data[64] = transationInvoice.getPortCode();
                    }
                    // 65
                    data[65] = "No"; // No removed
                    // 66-75
                    // No data available
                    for (int i = 0; i < MAX_COLUMN; i++) {
                        dataCell[i] = row.createCell(i);
                        dataCell[i].setCellValue(createHelper.createRichTextString(data[i]));
                    }
                }
                lineItem = 0;
            }
            if (claimtransactions.size() > 0) {
                for (ClaimTransaction transaction : claimtransactions) {

                    if (transaction != null) {

                        String stbuf3 = ("select obj from ClaimItemDetails obj where obj.organization.id=?1 and obj.branch.id=?2 and obj.transaction.id=?3 and obj.presentStatus=1 and obj.itemCategory in(?4,?5,?6,?7,?8,?9)");
                        ArrayList inparam3 = new ArrayList(9);
                        inparam3.add(user.getOrganization().getId());
                        inparam3.add(user.getBranch().getId());
                        inparam3.add(transaction.getId());
                        inparam3.add(IdosConstants.TRAVEL_EXPENSES);
                        inparam3.add(IdosConstants.BOARDING_LODGING);
                        inparam3.add(IdosConstants.OTHER_EXPENSES);
                        inparam3.add(IdosConstants.FIXED_PER_DIAM);
                        inparam3.add(IdosConstants.INCURRED_EXPENCES);
                        inparam3.add(IdosConstants.REIMBURSEMENT_EXPENSES);
                        List<ClaimItemDetails> claimItemDetailsList = genericDAO.queryWithParams(stbuf3.toString(),
                                em, inparam3);
                        // List<ClaimItemDetails> claimItemDetailsList =
                        // genericDAO.findByCriteria(ClaimItemDetails.class, criterias1, em);
                        if (claimItemDetailsList != null && !claimItemDetailsList.isEmpty()) {
                            for (ClaimItemDetails claimItemDetails : claimItemDetailsList) {
                                lineItem++;
                                Row row = sheets.createRow(rowCount++);
                                String[] data = new String[MAX_COLUMN];
                                for (int i = 0; i < MAX_COLUMN; i++) {
                                    dataCell[i] = row.createCell(i);
                                }
                                for (int i = 0; i < MAX_COLUMN; i++) {
                                    data[i] = "";
                                }

                                Specifics spec = transaction.getAdvanceForExpenseItems();
                                TransactionPurpose tranPur = transaction.getTransactionPurpose();
                                // 1,2,3,4
                                data[0] = user.getOrganization().getId().toString();
                                data[1] = user.getOrganization().getName();
                                String branchstate = IdosConstants.STATE_CODE_MAPPING
                                        .get(transaction.getTransactionBranch().getStateCode());
                                data[2] = branchstate;
                                data[3] = transaction.getTransactionBranch().getGstin();
                                // 4,5,6,7,8,9,10.11
                                String month = simpleDateFormat.format(transaction.getTransactionDate()).toUpperCase();
                                String year = simpleDateFormat2.format(transaction.getTransactionDate()).toUpperCase();
                                data[4] = year;
                                data[5] = month;
                                data[6] = transaction.getTransactionRefNumber();
                                data[7] = reportdf.format(transaction.getTransactionDate());
                                data[8] = "1";
                                data[9] = "INR";
                                data[10] = "";
                                data[11] = "invoice";
                                // 12
                                StringBuilder sbuffer = new StringBuilder("");
                                if (claimItemDetails.getClaimSettlementId() != null) {
                                    sbuffer = new StringBuilder("select obj from ClaimsSettlement obj where obj.id='"
                                            + claimItemDetails.getClaimSettlementId() + "' and obj.presentStatus=1");
                                    List<ClaimsSettlement> travelExpenses = genericDAO
                                            .executeSimpleQuery(sbuffer.toString(), em);
                                    if (travelExpenses.get(0).getItemTax() != null) {
                                        data[12] = "Taxable";
                                    } else if (transaction.getClaimsNetTax() != null) {
                                        data[12] = "Taxable";
                                    } else {
                                        data[12] = "Not Taxable";
                                    }
                                    // 13 , 14
                                    if (transaction.getTransactionBranch() != null
                                            || claimItemDetails.getVendorName() != null) {
                                        if (transaction.getTransactionBranch().getStateCode() != null) {
                                            if (IdosConstants.STATE_CODE_MAPPING
                                                    .get(transaction.getTransactionBranch().getStateCode())
                                                    .equals(claimItemDetails.getVendorState())) {
                                                data[13] = "inter-state";
                                            } else {
                                                data[13] = "intra-state";
                                            }
                                        }
                                    }
                                    // supplier code
                                    if (claimItemDetails.getVendorName() != null) {
                                        data[14] = claimItemDetails.getVendorName();
                                    }

                                    // 15, 16
                                    if (claimItemDetails.getIsRegistered() != null) {
                                        if (claimItemDetails.getIsRegistered() == 1) {
                                            data[15] = "Registered";
                                            if (claimItemDetails.getVendorGstin() != null) {
                                                data[16] = claimItemDetails.getVendorGstin();
                                            }
                                        }
                                    } else {
                                        data[15] = "Un-Registered";// Unregistered
                                    }
                                    // 17, 18
                                    if (claimItemDetails.getVendorState() != null) {
                                        data[17] = claimItemDetails.getVendorState();
                                    }

                                    if (claimItemDetails.getVendorName() != null) {
                                        data[18] = claimItemDetails.getVendorName();
                                    }

                                    // 19,20,21
                                    if (claimItemDetails.getInvoiceBillRefNo() != null) {
                                        data[19] = claimItemDetails.getInvoiceBillRefNo();
                                    }
                                    if (claimItemDetails.getInvoiceBillRefDate() != null) {
                                        data[20] = reportdf.format(claimItemDetails.getInvoiceBillRefDate());
                                    }
                                    data[21] = claimItemDetails.getTransaction().getClaimsNetSettlement().toString();

                                    // 22, 23 ,24,25,26
                                    data[22] = "No";
                                    if (claimItemDetails.getVendorState() != null) {
                                        data[23] = claimItemDetails.getVendorState();
                                    }
                                    data[24] = lineItem.toString();

                                    // 27,28,29
                                    if (claimItemDetails.getHsnOrSacCode() != null) {
                                        data[27] = claimItemDetails.getHsnOrSacCode();
                                    }
                                    if (claimItemDetails.getProductServiceDesc() != null) {
                                        data[28] = claimItemDetails.getProductServiceDesc();
                                    }
                                    if (claimItemDetails.getUqc() != null) {
                                        data[29] = claimItemDetails.getUqc();
                                    }
                                    // 30 to 37
                                    data[30] = claimItemDetails.getQuantity().toString();
                                    data[31] = claimItemDetails.getRate().toString();
                                    data[32] = "";
                                    data[33] = claimItemDetails.getRate().toString();
                                    data[34] = "";
                                    data[35] = "";
                                    data[36] = "";
                                    data[37] = claimItemDetails.getGrossAmt().toString();
                                    // 38
                                    if (claimItemDetails.getSgstRate() != null || claimItemDetails.getCgstRate() != null
                                            || claimItemDetails.getIgstAmt() != null) {
                                        if (claimItemDetails.getSgstRate() != null
                                                || claimItemDetails.getCgstRate() != null) {
                                            Double amount = claimItemDetails.getSgstRate()
                                                    + claimItemDetails.getCgstRate();
                                            data[38] = amount.toString();
                                        } else {
                                            data[38] = claimItemDetails.getIgstRate().toString();
                                        }
                                    }
                                    // 39,40
                                    if (claimItemDetails.getIgstId() != null && claimItemDetails.getIgstId() != -1) {
                                        data[39] = claimItemDetails.getIgstRate().toString();
                                        data[40] = claimItemDetails.getIgstAmt().toString();
                                    }
                                    // 41,42
                                    if (claimItemDetails.getCgstId() != null && claimItemDetails.getCgstId() != -1) {
                                        data[41] = claimItemDetails.getCgstRate().toString();
                                        data[42] = claimItemDetails.getCgstAmt().toString();
                                    }
                                    // 43, 44
                                    if (claimItemDetails.getSgstId() != null && claimItemDetails.getSgstId() != -1) {
                                        data[43] = claimItemDetails.getSgstRate().toString();
                                        data[44] = claimItemDetails.getSgstAmt().toString();
                                    }
                                    // 45, 46
                                    if (claimItemDetails.getCessId() != null && claimItemDetails.getCessId() != -1) {
                                        data[45] = claimItemDetails.getCessRate().toString();
                                        data[46] = claimItemDetails.getCessAmt().toString();
                                    }
                                    // 47,48 ,49, 50, 51
                                    data[47] = "IP";
                                    if (claimItemDetails.getIgstId() != null && claimItemDetails.getIgstId() != -1) {
                                        data[48] = claimItemDetails.getIgstAmt().toString();
                                    }
                                    if (claimItemDetails.getCgstId() != null && claimItemDetails.getCgstId() != -1) {
                                        data[49] = claimItemDetails.getCgstAmt().toString();
                                    }
                                    if (claimItemDetails.getSgstId() != null && claimItemDetails.getSgstId() != -1) {
                                        data[50] = claimItemDetails.getSgstAmt().toString();
                                    }
                                    if (claimItemDetails.getCessId() != null && claimItemDetails.getCessId() != -1) {
                                        data[51] = claimItemDetails.getCessAmt().toString();
                                    }
                                    // 52 t0 75
                                    // No data available
                                    for (int i = 0; i < MAX_COLUMN; i++) {
                                        dataCell[i] = row.createCell(i);
                                        dataCell[i].setCellValue(createHelper.createRichTextString(data[i]));
                                    }
                                }

                                lineItem = 0;
                            }
                        }
                    }

                }
            }

            FileOutputStream fileOut = new FileOutputStream(path);
            workbook.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error", e);
        }
        try {
            log.log(Level.INFO, "gst filing=" + gstFilings.size());
            if (gstFilings.size() != 0) {
                for (GSTFiling gstFiling : gstFilings) {
                    gstFiling.setGstFilingStatus(1);
                    log.log(Level.INFO, " inside set filing status");
                    log.log(Level.INFO, "get the status=" + gstFiling.getGstFilingStatus());
                    genericDAO.saveOrUpdate(gstFiling, user, em);
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "not able to upload", e);
            log.log(Level.INFO, "inside catch");
        }
        return sheetName;
    }

}
