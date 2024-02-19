package service.plbscoamapperservice;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import model.BaseModel;
import model.Users;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.node.ObjectNode;
import service.BaseService;

public interface PLBSCOAMapperService extends BaseService {
	<T extends BaseModel> void savePLBSCOAMapping(final Long orgId, final int plBsHead, final String coaIds,
			final Users user, EntityManager entityManager);

	public ObjectNode fetchPLBSCOAMapping(Users user, EntityManager entityManager);
}
