package com.idos.dao;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
import model.Users;
import model.GSTR2.AdjustmentofAdvJson;
import model.GSTR2.B2BJson;
import model.GSTR2.B2BURJson;
import model.GSTR2.DCNRegisterVendorJson;
import model.GSTR2.DCNUnregisterVendorJson;
import model.GSTR2.HSNSACSuppliesInwardReturnJson;
import model.GSTR2.ITCReversalJson;
import model.GSTR2.ImportCapitalgoodsJson;
import model.GSTR2.ImportOfServiceJson;
import model.GSTR2.TaxLiabilityAdvPaidJson;

public class GSTR2JsonDAOImpl implements GSTR2JsonDAO {

	@Override
	public String createGSTR2Json(JsonNode json, Users user, EntityManager entityManager) throws Exception {
		String dateMonthAndYearForMonthWise = "";
		String dateMonthAndYearForQuarterWise = "";
		String dateMonthAndYearFromDate = "";
		String dateMonthAndYearToDate = "";
		Integer intervalType = json.findValue("intervalType").asInt();
		Integer jsonType = json.findValue("jsonType").asInt();
		if (intervalType == 1) {
			dateMonthAndYearForMonthWise = json.findValue("txtDate1").asText();
			log.log(Level.FINE, "date1=" + dateMonthAndYearForMonthWise);
		} else if (intervalType == 2) {
			dateMonthAndYearForQuarterWise = json.findValue("txtDate2").asText();
			log.log(Level.FINE, "date2=" + dateMonthAndYearForQuarterWise);
		} else if (intervalType == 3) {
			dateMonthAndYearFromDate = json.findValue("txtDate3").asText();
			dateMonthAndYearToDate = json.findValue("txtDate4").asText();
			log.log(Level.FINE, "date3=" + dateMonthAndYearFromDate);
			log.log(Level.FINE, "date4=" + dateMonthAndYearToDate);
		}
		String gstIn = json.findValue("gstIn").asText();
		String resultJson = "";
		switch (jsonType) {
			case 1:
				resultJson = B2BInvoiceJSON(gstIn, intervalType, dateMonthAndYearForMonthWise,
						dateMonthAndYearForQuarterWise, dateMonthAndYearFromDate, dateMonthAndYearFromDate);
				break;
			case 2:
				resultJson = B2BURInvoiceJSON(gstIn, intervalType, dateMonthAndYearForMonthWise,
						dateMonthAndYearForQuarterWise, dateMonthAndYearFromDate, dateMonthAndYearFromDate);
				break;
			case 3:
				resultJson = creditDebitNoteRegisterJSON(gstIn, intervalType, dateMonthAndYearForMonthWise,
						dateMonthAndYearForQuarterWise, dateMonthAndYearFromDate, dateMonthAndYearFromDate);
				break;
			case 4:
				resultJson = creditDebitNoteUnregisterJSON(gstIn, intervalType, dateMonthAndYearForMonthWise,
						dateMonthAndYearForQuarterWise, dateMonthAndYearFromDate, dateMonthAndYearFromDate);
				break;
			case 5:
				resultJson = importsOfGoodsJSON(gstIn, intervalType, dateMonthAndYearForMonthWise,
						dateMonthAndYearForQuarterWise, dateMonthAndYearFromDate, dateMonthAndYearFromDate);
				break;
			case 6:
				resultJson = importOfServiceJSON(gstIn, intervalType, dateMonthAndYearForMonthWise,
						dateMonthAndYearForQuarterWise, dateMonthAndYearFromDate, dateMonthAndYearFromDate);
				break;
			case 7:
				resultJson = taxLiabilityAdvPaidJSON(gstIn, intervalType, dateMonthAndYearForMonthWise,
						dateMonthAndYearForQuarterWise, dateMonthAndYearFromDate, dateMonthAndYearFromDate);
				break;
			case 8:
				resultJson = hsnSacSuppliesInwardReturnJSON(gstIn, intervalType, dateMonthAndYearForMonthWise,
						dateMonthAndYearForQuarterWise, dateMonthAndYearFromDate, dateMonthAndYearFromDate);
				break;
			case 9:
				resultJson = itcReversalJSON(gstIn, intervalType, dateMonthAndYearForMonthWise,
						dateMonthAndYearForQuarterWise, dateMonthAndYearFromDate, dateMonthAndYearFromDate);
				break;
			case 10:
				resultJson = "Nill Rated Invoices ";
				break;
			case 11:
				resultJson = adjustmentofAdvJSON(gstIn, intervalType, dateMonthAndYearForMonthWise,
						dateMonthAndYearForQuarterWise, dateMonthAndYearFromDate, dateMonthAndYearFromDate);
				break;
			default:
				break;
		}

		return resultJson;
	}

	private String B2BInvoiceJSON(String gstIn, Integer intervalType, String dateStr1, String dateStr2, String dateStr3,
			String dateStr4) throws Exception {
		B2BJson jsonObj = new B2BJson();

		return OBJ_MAPPER.writeValueAsString(jsonObj);
	}

	private String B2BURInvoiceJSON(String gstIn, Integer intervalType, String dateStr1, String dateStr2,
			String dateStr3, String dateStr4) throws Exception {
		B2BURJson jsonObj = new B2BURJson();

		return OBJ_MAPPER.writeValueAsString(jsonObj);
	}

	private String creditDebitNoteRegisterJSON(String gstIn, Integer intervalType, String dateStr1, String dateStr2,
			String dateStr3, String dateStr4) throws Exception {
		DCNRegisterVendorJson jsonObj = new DCNRegisterVendorJson();

		return OBJ_MAPPER.writeValueAsString(jsonObj);
	}

	private String creditDebitNoteUnregisterJSON(String gstIn, Integer intervalType, String dateStr1, String dateStr2,
			String dateStr3, String dateStr4) throws Exception {
		DCNUnregisterVendorJson jsonObj = new DCNUnregisterVendorJson();

		return OBJ_MAPPER.writeValueAsString(jsonObj);
	}

	private String importsOfGoodsJSON(String gstIn, Integer intervalType, String dateStr1, String dateStr2,
			String dateStr3, String dateStr4) throws Exception {
		ImportCapitalgoodsJson jsonObj = new ImportCapitalgoodsJson();

		return OBJ_MAPPER.writeValueAsString(jsonObj);
	}

	private String importOfServiceJSON(String gstIn, Integer intervalType, String dateStr1, String dateStr2,
			String dateStr3, String dateStr4) throws Exception {
		ImportOfServiceJson jsonObj = new ImportOfServiceJson();

		return OBJ_MAPPER.writeValueAsString(jsonObj);
	}

	private String taxLiabilityAdvPaidJSON(String gstIn, Integer intervalType, String dateStr1, String dateStr2,
			String dateStr3, String dateStr4) throws Exception {
		TaxLiabilityAdvPaidJson jsonObj = new TaxLiabilityAdvPaidJson();

		return OBJ_MAPPER.writeValueAsString(jsonObj);
	}

	private String hsnSacSuppliesInwardReturnJSON(String gstIn, Integer intervalType, String dateStr1, String dateStr2,
			String dateStr3, String dateStr4) throws Exception {
		HSNSACSuppliesInwardReturnJson jsonObj = new HSNSACSuppliesInwardReturnJson();

		return OBJ_MAPPER.writeValueAsString(jsonObj);
	}

	private String itcReversalJSON(String gstIn, Integer intervalType, String dateStr1, String dateStr2,
			String dateStr3, String dateStr4) throws Exception {
		ITCReversalJson jsonObj = new ITCReversalJson();

		return OBJ_MAPPER.writeValueAsString(jsonObj);
	}

	private String adjustmentofAdvJSON(String gstIn, Integer intervalType, String dateStr1, String dateStr2,
			String dateStr3, String dateStr4) throws Exception {
		AdjustmentofAdvJson jsonObj = new AdjustmentofAdvJson();

		return OBJ_MAPPER.writeValueAsString(jsonObj);
	}
}
