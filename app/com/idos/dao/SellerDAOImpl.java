package com.idos.dao;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import play.mvc.Http;

import javax.mail.Session;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.logging.Level;
import javax.inject.Inject;
import play.db.jpa.JPAApi;
import model.IdosRegisteredVendor;
import model.IdosRegisteredVendorPricings;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import controllers.BaseController;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.libs.Files.TemporaryFile;

public class SellerDAOImpl implements SellerDAO {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	private static Http.Request request;
	// private static Http.Session session = request.session();

	@Override
	public ObjectNode uploadSupplierItems(Request request, ObjectNode result, JsonNode json,
			MultipartFormData<File> body, EntityManager entityManager,
			EntityTransaction entitytransaction) {
		final List<FilePart<File>> chartOfAccount = body.getFiles();
		Http.Session session = request.session();

		for (FilePart<File> filePart : chartOfAccount) {
			try {
				entitytransaction.begin();
				// String email = StaticController.session("selleremail");
				String email = session.getOptional("selleremail").orElse("");
				Map<String, Object> criterias = new HashMap<String, Object>();
				criterias.clear();
				criterias.put("vendorEmail", email);
				criterias.put("presentStatus", 1);
				IdosRegisteredVendor idosRegVendor = genericDao.getByCriteria(IdosRegisteredVendor.class, criterias,
						entityManager);
				String fileName = filePart.getFilename();
				final Session mailsession = BaseController.mailSession();
				final String subject = "Supplier Items Uploaded";
				//File file = filePart.getRef();
				TemporaryFile temporaryFile = (TemporaryFile) filePart.getRef();
				String filePath = temporaryFile.path().toString();
				File file = new File(filePath);
				InputStream is = new java.io.FileInputStream(file);
				XSSFWorkbook wb = new XSSFWorkbook(is);
				int numOfSheets = wb.getNumberOfSheets();
				for (int i = 0; i < numOfSheets; i++) {
					XSSFSheet sheet = wb.getSheetAt(i);
					Iterator rows = sheet.rowIterator();
					while (rows.hasNext()) {
						XSSFRow row = (XSSFRow) rows.next();
						IdosRegisteredVendorPricings idosRegVendorPricings = new IdosRegisteredVendorPricings();
						if (row.getCell(0) != null) {
							Cell cell = row.getCell(0);
							cell.setCellType(CellType.STRING);
						}
						String supplierVendorItem = row.getCell(0) != null ? row.getCell(0).toString() : null;
						if (supplierVendorItem != null) {
							idosRegVendorPricings.setVendorItems(supplierVendorItem);
						}
						if (row.getCell(1) != null) {
							Cell cell = row.getCell(1);
							cell.setCellType(CellType.STRING);
						}
						String itemDescription = row.getCell(1) != null ? row.getCell(1).toString() : null;
						if (itemDescription != null) {
							idosRegVendorPricings.setVendorItemsDescription(itemDescription);
						}
						if (row.getCell(2) != null) {
							Cell cell = row.getCell(2);
							cell.setCellType(CellType.STRING);
						}
						String suppliedInbranches = row.getCell(2) != null ? row.getCell(2).toString() : null;
						if (suppliedInbranches != null) {
							idosRegVendorPricings.setVendorLocations(suppliedInbranches);
						}
						if (row.getCell(3) != null) {
							Cell cell = row.getCell(3);
							cell.setCellType(CellType.STRING);
						}
						String retailerPrice = row.getCell(3) != null ? row.getCell(3).toString() : null;
						if (retailerPrice != null) {
							idosRegVendorPricings.setVendorRetailerUnitPrice(Double.parseDouble(retailerPrice));
						}
						if (row.getCell(4) != null) {
							Cell cell = row.getCell(4);
							cell.setCellType(CellType.STRING);
						}
						String wholeSellerPrice = row.getCell(4) != null ? row.getCell(4).toString() : null;
						if (wholeSellerPrice != null) {
							idosRegVendorPricings.setVendorWholesaleUnitPrice(Double.parseDouble(wholeSellerPrice));
						}
						if (row.getCell(5) != null) {
							Cell cell = row.getCell(5);
							cell.setCellType(CellType.STRING);
						}
						String specialOfferedPrice = row.getCell(5) != null ? row.getCell(5).toString() : null;
						if (specialOfferedPrice != null) {
							idosRegVendorPricings.setVendorSpecialUnitPrice(Double.parseDouble(specialOfferedPrice));
						}
						if (row.getCell(6) != null) {
							Cell cell = row.getCell(6);
							cell.setCellType(CellType.STRING);
						}
						String specialPriceRequirements = row.getCell(6) != null ? row.getCell(6).toString() : null;
						if (specialPriceRequirements != null) {
							idosRegVendorPricings.setVendorSpecialPriceRequirements(specialPriceRequirements);
						}
						idosRegVendorPricings.setVendorEmail(idosRegVendor.getVendorEmail());
						idosRegVendorPricings.setVendorPhoneNumber(idosRegVendor.getVendorPhoneNumber());
						idosRegVendorPricings.setVendorName(idosRegVendor.getVendorName());
						genericDao.saveOrUpdate(idosRegVendorPricings, null, entityManager);
					}
				}
				entitytransaction.commit();
				BaseController.sendMailWithAttachment(mailsession, subject, fileName, file, "allusers@myidos.com",
						"alerts@myidos.com");
			} catch (Exception ex) {
				log.log(Level.SEVERE, "Error", ex);
			}
		}
		return result;
	}

}
