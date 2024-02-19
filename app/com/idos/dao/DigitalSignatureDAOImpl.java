package com.idos.dao;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.fasterxml.jackson.databind.JsonNode;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import java.util.logging.Level;
import model.Branch;
import model.DigitalSignatureBranchWise;
import model.Organization;
import model.Users;
import play.mvc.Result;
import play.db.jpa.JPAApi;
import javax.inject.Inject;

public class DigitalSignatureDAOImpl implements DigitalSignatureDAO {
	private static JPAApi jpaApi;

	@Override
	public boolean digitalSignatureDAO(Organization org, Branch branch, Users user, JsonNode json,
			EntityManager entityManager) throws IDOSException {
		try {
			DigitalSignatureBranchWise digitalSignBranchWise = new DigitalSignatureBranchWise();
			String dsPersonName = json.findPath("dsPersonName") != null ? json.findPath("dsPersonName").asText() : null;
			String dsPersonDesignation = json.findPath("dsPersonDesignation") != null
					? json.findPath("dsPersonDesignation").asText()
					: null;
			String dsPersonPhoneNo = json.findPath("dsPersonPhoneNo") != null
					? json.findPath("dsPersonPhoneNo").asText()
					: null;
			String dsPersonEmailId = json.findPath("dsPersonEmailId") != null
					? json.findPath("dsPersonEmailId").asText()
					: null;
			String dsRefNo = json.findPath("dsRefNo") != null ? json.findPath("dsRefNo").asText() : null;
			String digitalSignDocumentsList = json.findPath("digitalSignDocumentsList") != null
					? json.findPath("digitalSignDocumentsList").asText()
					: null;
			String dsKYC = json.findPath("dsKYC") != null ? json.findPath("dsKYC").asText() : null;
			String dsValidityFrom = json.findPath("dsValidityFrom") != null ? json.findPath("dsValidityFrom").asText()
					: null;
			String dsValidityTo = json.findPath("dsValidityTo") != null ? json.findPath("dsValidityTo").asText() : null;

			digitalSignBranchWise.setOrganization(org);
			digitalSignBranchWise.setBranch(branch);
			if (digitalSignDocumentsList != null)
				digitalSignBranchWise.setDigitalSignDocuments(digitalSignDocumentsList);
			if (dsPersonName != null)
				digitalSignBranchWise.setPersonName(dsPersonName);
			if (dsPersonDesignation != null)
				digitalSignBranchWise.setDesignation(dsPersonDesignation);
			if (dsPersonPhoneNo != null)
				digitalSignBranchWise.setPhoneNo(dsPersonPhoneNo);
			if (dsPersonEmailId != null)
				digitalSignBranchWise.setEmailId(dsPersonEmailId);
			if (dsRefNo != null)
				digitalSignBranchWise.setRefNo(dsRefNo);
			if (dsKYC != null)
				digitalSignBranchWise.setKycDetails(dsKYC);
			if (dsValidityFrom != null)
				digitalSignBranchWise.setValidityFrom(dsValidityFrom);
			if (dsValidityTo != null)
				digitalSignBranchWise.setValidityTo(dsValidityTo);

			genericDao.saveOrUpdate(digitalSignBranchWise, user, entityManager);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
					"Error on fetching unfilfulled units", ex.getMessage());
		}
		return true;
	}

}
