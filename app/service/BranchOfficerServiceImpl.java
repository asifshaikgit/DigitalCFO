package service;

import java.util.HashMap;
import java.util.Map;

import model.OrganizationKeyOfficials;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.inject.Inject;
import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import com.idos.util.CountryCurrencyUtil;

import controllers.StaticController;
import play.libs.Json;

public class BranchOfficerServiceImpl implements BranchOfficerService {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Override
	public ObjectNode getDetails(String email) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode on = Json.newObject();
		if (null != email && !"".equals(email)) {
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("email", email);
			criterias.put("presentStatus", 1);
			OrganizationKeyOfficials official = genericDAO.getByCriteria(OrganizationKeyOfficials.class, criterias,
					entityManager);
			if (null != official && null != official.getId()) {
				on.put("id", official.getId());
				if (null != official.getName()) {
					on.put("name", official.getName());
				} else {
					on.put("name", "");
				}
				if (null != official.getDesignation()) {
					on.put("designation", official.getDesignation());
				} else {
					on.put("designation", "");
				}
				if (null != official.getCountry()) {
					on.put("country", CountryCurrencyUtil.getCountryName(official.getCountry().toString()));
				} else {
					on.put("country", "");
				}
				if (null != official.getName()) {
					on.put("city", official.getCity());
				} else {
					on.put("city", "");
				}
				if (null != official.getEmail()) {
					on.put("email", official.getEmail());
				} else {
					on.put("email", "");
				}
				if (null != official.getPhoneNumber()) {
					on.put("officalPhoneNumber", official.getPhoneNumber());
				} else {
					on.put("officalPhoneNumber", "");
				}
				if (null != official.getPersonalPhoneNumber()) {
					on.put("personalPhoneNumber", official.getPersonalPhoneNumber());
				} else {
					on.put("personalPhoneNumber", "");
				}
				if (null != official.getUploadedId()) {
					on.put("uploadId", official.getUploadedId());
				} else {
					on.put("uploadId", "");
				}
				on.put("result", true);
			} else {
				on.put("result", false);
				on.put("message", "Cannot find the user details.");
			}
		}
		return on;
	}

}
