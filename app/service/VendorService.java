package service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.idos.dao.VendorDAO;
import com.idos.dao.VendorDAOImpl;
import com.idos.util.IDOSException;
import model.*;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import play.Application;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

public interface VendorService extends BaseService {
	Application application = null; // Initialize to null initially
	/*
	 * default void setApplication(Application applicationInstance) {
	 * application = applicationInstance;
	 * }
	 */
	DynamicReportService dynReportService = new DynamicReportServiceImpl(application);

	ObjectNode getSpecifics(final Long orgId, final Integer type);

	ObjectNode search(final Long custVendId, final int type, final String txnRefNumber, final Long txnType,
			final Long category, final Long item, final String fromDate, final String toDate, final Long branch,
			final Long project, final Double fromAmount, final Double toAmount, final String status)
			throws ParseException;

	ObjectNode getStatements(final long id, final String type, final long org, final long branch, final String fromDate,
			final String toDate, final int getType) throws ParseException;

	ObjectNode exportStatements(final long id, final String type, final long org, final long branch,
			final String fromDate, final String toDate, final int getType, final String branchName,
			final String fileType) throws ParseException, IOException;

	Double getOpeningBalance(final Vendor vendor, final long branch, final long org, final String fromDate,
			final String toDate, final String type) throws ParseException;

	Double getOpeningBalance(final long id, final long branch, final long org, final String fromDate,
			final String toDate, final String type) throws ParseException;

	Vendor saveVendor(JsonNode json, Users user, EntityManager em) throws IDOSException;
}