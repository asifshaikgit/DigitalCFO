package service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import model.IdosRegisteredVendor;
import model.Organization;
import model.Vendor;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.db.jpa.JPAApi;
import play.libs.Json;
import javax.inject.Inject;
import com.idos.dao.OrgAnalyticsDAO;
import com.idos.dao.OrgAnalyticsDAOImpl;
import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;

public class AnalyticsServiceImpl implements AnalyticsService {
	private static JPAApi jpaApi;
	private static EntityManager em;

	@Override
	public ObjectNode getVendorSellerAnalytics(String email) throws Exception {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode on = Json.newObject();
		on.put("result", false);
		on.put("message", "Oops! Something went wrong. Please try again later.");
		// EntityManager em = null;
		try {
			if (null != email && !"".equals(email)) {
				Map<String, Object> criterias = new HashMap<String, Object>();
				criterias.put("vendorEmail", email);
				criterias.put("presentStatus", 1);
				// EntityManager em = jpaApi.em();
				IdosRegisteredVendor vendor = genericDAO.getByCriteria(IdosRegisteredVendor.class, criterias, em);
				if (null != vendor) {
					int contacted = 0;
					if (null != vendor.getNumberOfTimesContacted()) {
						contacted = vendor.getNumberOfTimesContacted();
						on.put("contacted", contacted);
					} else {
						on.put("contacted", 0);
					}
					if (null != vendor.getNumberOfTimesSearched()) {
						on.put("searched", vendor.getNumberOfTimesSearched());
					} else {
						on.put("searched", 0);
					}
					if (null != vendor.getId()) {
						List<Organization> orgs = analyticsDAO.getUniqueOrgContacted(vendor.getId());
						if (!orgs.isEmpty()) {
							StringBuilder ids = new StringBuilder();
							for (int i = 0; i < orgs.size(); i++) {
								if (null != orgs.get(i) && null != orgs.get(i).getId()) {
									ids.append(orgs.get(i).getId());
									if (i != orgs.size() - 1) {
										ids.append(",");
									}
								}
							}
							if (ids.length() > 0) {
								List<Vendor> vendors = analyticsDAO.getConverted(email, ids.toString());
								if (!vendors.isEmpty() && vendors.size() > 0) {
									on.put("converted", vendors.size());
									double convertRate = (((double) vendors.size() / (double) contacted) * 100);
									on.put("convertRate", convertRate);
								} else {
									on.put("converted", 0);
									on.put("convertRate", 0);
								}
							}
						}
					} else {
						on.put("converted", 0);
						on.put("convertRate", 0);
					}
					on.put("result", true);
					on.remove("message");
				} else {
					on.put("message", "No analytics found.");
				}
			} else {
				on.put("message", "No analytics found.");
			}
		} catch (Exception e) {
			throw e;
		}
		return on;
	}

}
