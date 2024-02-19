package com.idos.dao.plbscoamapper;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import org.apache.commons.lang3.StringUtils;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;

import model.BaseModel;
import model.Users;
import model.PLBSCOAMap.PLBSCOAMap;

public class PLBSCOAMapperDAOImpl implements PLBSCOAMapperDAO {

	@Override
	public <T extends BaseModel> void savePLBSCOAMapping(final Long orgId, final int plBsHead, final String coaIds,
			final Users user, EntityManager entityManager) {

		// Get COA Id Arr
		final String[] coaIdArr = StringUtils.split(coaIds, ",");

		final GenericDAO genericDao = new GenericJpaDAO();

		// The variable used inside loop.
		PLBSCOAMap plbsCoaMap = null;
		Map<String, Object> criterias = new HashMap<String, Object>(2);
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("plbsHead", plBsHead);
		criterias.put("presentStatus", 1);
		genericDao.deleteByCriteria(PLBSCOAMap.class, criterias, entityManager);

		// Save in db.
		for (int i = 0; i < coaIdArr.length; i++) {
			if ("multiselect-all".equalsIgnoreCase(coaIdArr[i])) {
				continue;
			}
			plbsCoaMap = new PLBSCOAMap();

			// Default values.
			plbsCoaMap.setCreatedAt(Calendar.getInstance().getTime());
			plbsCoaMap.setCreatedBy(user.getCreatedBy());

			// Non null values.
			plbsCoaMap.setOrganizationId(user.getOrganization());
			plbsCoaMap.setPlbsHead(plBsHead);
			plbsCoaMap.setCoaId(coaIdArr[i]);

			genericDao.saveOrUpdate(plbsCoaMap, user, entityManager);
		}
	}
}
