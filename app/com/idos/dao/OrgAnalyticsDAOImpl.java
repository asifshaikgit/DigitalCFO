package com.idos.dao;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import model.Organization;
import model.Vendor;
import play.db.jpa.JPAApi;
import javax.inject.Inject;

@SuppressWarnings("unchecked")
public class OrgAnalyticsDAOImpl implements OrgAnalyticsDAO {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Override
	public List<Organization> getUniqueOrgContacted(long id)
			throws Exception {
		List<Organization> result = Collections.emptyList();
		try {
			if (id > 0) {
				result = entityManager.createQuery(
						"SELECT DISTINCT(obj.organization) FROM IdosRegisteredVendorContacted obj WHERE obj.idosRegisteredVendor.id = :vid")
						.setParameter("vid", id).getResultList();
			}
		} catch (Exception e) {
			throw e;
		}
		return result;
	}

	@Override
	public List<Vendor> getConverted(String email, String inParameterOrgIds)
			throws Exception {
		List<Vendor> result = Collections.emptyList();
		try {
			if (null != email && !"".equals(email) && null != inParameterOrgIds && !"".equals(inParameterOrgIds)) {
				// EntityManager em = jpaApi.em();
				result = entityManager
						.createQuery("FROM Vendor WHERE email = :email AND branch_organization_id IN(:ids)")
						.setParameter("email", email).setParameter("ids", inParameterOrgIds).getResultList();
			}
		} catch (Exception e) {
			throw e;
		}
		return result;
	}

}
