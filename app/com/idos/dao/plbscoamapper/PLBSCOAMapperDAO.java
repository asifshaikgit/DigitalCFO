package com.idos.dao.plbscoamapper;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.idos.dao.BaseDAO;
import model.BaseModel;
import model.Users;

public interface PLBSCOAMapperDAO extends BaseDAO {

	/**
	 * Method savePLBSCOAMapping - Saves PL BS mapping with COA in database.
	 * 
	 * @param orgId
	 * @param plBsHead
	 * @param coaIds
	 * @param user
	 * @param entityManager
	 */
	<T extends BaseModel> void savePLBSCOAMapping(final Long orgId, final int plBsHead, final String coaIds,
			final Users user, EntityManager entityManager);

}
