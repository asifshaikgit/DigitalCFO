package service;

import java.text.ParseException;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface OnlineService extends BaseService {
	String ONLINE_USERS_JPQL = "select obj FROM Users obj WHERE obj.organization.id = ?1 and obj.email = ?2 and obj.presentStatus = 1";

	public ObjectNode getOnlineIdosUsers(ObjectNode result, JsonNode json, Users user, EntityManager entityManager);

	public ObjectNode getOnlineOrgUsers(final Users user, final EntityManager entityManager);

	public ObjectNode getOnlineOrgUsers(final Users user, final EntityManager entityManager, final String search,
			final String skipEmail);

	public ObjectNode sendChatMessage(final Users user, final EntityManager entityManager, final String from,
			final String to, final String message) throws ParseException;

	public ObjectNode getChatHistory(final Users user, final EntityManager entityManager, final int month);
}
