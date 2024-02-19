package service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.IdosChatHistory;
import model.Users;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.inject.Inject;
import play.db.jpa.JPAApi;
import play.libs.Json;
import pojo.TransactionViewResponse;
import actor.AdminActor;
import actor.CreatorActor;
import actor.ProjectTransactionActor;
import actor.SpecificsTransactionActor;
import actor.VendorTransactionActor;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import com.idos.dao.OnlineDAO;
import com.idos.dao.OnlineDAOImpl;
import com.idos.util.DateUtil;

public class OnlineServiceImpl implements OnlineService {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	private static OnlineServiceImpl serviceImpl = new OnlineServiceImpl();

	public static final OnlineServiceImpl getInstance() {
		return serviceImpl;
	}

	@Override
	public ObjectNode getOnlineIdosUsers(ObjectNode result, JsonNode json, Users user, EntityManager entityManager) {
		result = onlineDAO.getOnlineIdosUsers(result, json, user, entityManager);
		return result;
	}

	@Override
	public synchronized ObjectNode getOnlineOrgUsers(Users user, EntityManager entityManager) {
		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		if (null == user) {
			result.put("message", "No users online!");
		} else {
			ArrayNode users = result.putArray("users");
			Set<String> adminRegistered = AdminActor.adminRegistered.keySet();
			Set<String> expenseregistrered = CreatorActor.expenseregistrered.keySet();
			Set<String> projectRegistered = ProjectTransactionActor.projectRegistered.keySet();
			Set<String> registrered = SpecificsTransactionActor.registrered.keySet();
			Set<String> vendvendregistrered = VendorTransactionActor.vendvendregistrered.keySet();
			Set<String> onlineUsers = new TreeSet<String>();
			onlineUsers.addAll(adminRegistered);
			onlineUsers.addAll(expenseregistrered);
			onlineUsers.addAll(projectRegistered);
			onlineUsers.addAll(registrered);
			onlineUsers.addAll(vendvendregistrered);

			for (String oUser : onlineUsers) {
				if ((null != oUser && !"".equals(oUser))) {
					ArrayList inparams = new ArrayList(2);
					inparams.add(user.getOrganization().getId());
					inparams.add(oUser);
					List<Users> list = genericDAO.queryWithParamsName(ONLINE_USERS_JPQL, entityManager, inparams);
					if (null != list && list.size() > 0) {
						Users onlineUser = list.get(0);
						ObjectNode row = Json.newObject();
						row.put("email", onlineUser.getEmail());
						row.put("name", onlineUser.getFullName());
						users.add(row);
					}
				}
			}
			result.put("result", true);
			result.remove("message");
		}
		return result;
	}

	@Override
	public synchronized ObjectNode getOnlineOrgUsers(Users user, EntityManager entityManager, String search,
			String skipEmail) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		search = search.trim();
		if ((null == user || (null == skipEmail || "".equals(skipEmail))) && search.length() < 2) {
			result.put("message", "No users online!");
		} else {
			ArrayNode users = result.putArray("users");
			Set<String> adminRegistered = AdminActor.adminRegistered.keySet();
			Set<String> expenseregistrered = CreatorActor.expenseregistrered.keySet();
			Set<String> projectRegistered = ProjectTransactionActor.projectRegistered.keySet();
			Set<String> registrered = SpecificsTransactionActor.registrered.keySet();
			Set<String> vendvendregistrered = VendorTransactionActor.vendvendregistrered.keySet();
			Set<String> onlineUsers = new TreeSet<String>();
			onlineUsers.addAll(adminRegistered);
			onlineUsers.addAll(expenseregistrered);
			onlineUsers.addAll(projectRegistered);
			onlineUsers.addAll(registrered);
			onlineUsers.addAll(vendvendregistrered);
			StringBuilder query = new StringBuilder();
			for (String oUser : onlineUsers) {
				if ((null != oUser && !"".equals(oUser)) && !oUser.equals(user.getEmail())) {
					query.delete(0, query.length());
					query.append("SELECT obj FROM Users obj WHERE obj.presentStatus = 1");
					query.append(" AND obj.email = '").append(oUser).append("'");
					query.append(" AND obj.email != '").append(skipEmail).append("'");
					query.append(" AND obj.organization.id = ").append(user.getOrganization().getId());
					query.append(" AND (obj.email LIKE '%").append(search.trim()).append("%'");
					query.append(" OR obj.fullName LIKE '%").append(search.trim()).append("%')");
					List<Users> list = genericDAO.executeSimpleQueryWithLimit(query.toString(), entityManager, 1);
					String email = null, name = null;
					if (null != list && list.size() > 0) {
						Users onlineUser = list.get(0);
						email = (null == onlineUser.getEmail() || "".equals(onlineUser.getEmail())) ? ""
								: onlineUser.getEmail();
						name = (null == onlineUser.getFullName() || "".equals(onlineUser.getFullName())) ? ""
								: onlineUser.getFullName();
						if ("" != email) {
							query.delete(0, query.length());
							query = query.append(name).append("-").append(email);
							users.add(query.toString());
						}
					}
				}
			}
			result.put("result", true);
			result.remove("message");
		}
		return result;
	}

	@Override
	public ObjectNode sendChatMessage(Users user, EntityManager entityManager, String from, String to, String message)
			throws ParseException {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		if (null == user || null == user.getOrganization()) {
			result.put("message", "No users online!");
		} else {
			// Save a copy to DB!
			Calendar calendar = Calendar.getInstance();
			String chatDate = DateUtil.mysqldf.format(calendar.getTime());
			StringBuilder query = new StringBuilder();
			String[] receivers = to.split(",");
			query.append("SELECT obj FROM IdosChatHistory obj WHERE obj.presentStatus = 1");
			query.append(" AND obj.chatDate = '").append(chatDate).append("'");
			if (receivers.length > 1) {
				query.append(" AND (LOCATE('").append(from).append("',obj.senderReceiver) > 0");
				for (String receiver : receivers) {
					query.append(" AND LOCATE('").append(receiver).append("',obj.senderReceiver) > 0");
				}
			} else {
				query.append(" AND (obj.senderReceiver = '").append(from).append("-").append(to).append("'");
				query.append(" OR obj.senderReceiver = '").append(to).append("-").append(from).append("'");
			}
			query.append(")");
			List<IdosChatHistory> histories = genericDAO.executeSimpleQueryWithLimit(query.toString(), entityManager,
					1);
			IdosChatHistory history = null;
			StringBuilder msg = new StringBuilder(), senderReceiver = new StringBuilder();
			if (null == histories || 0 == histories.size()) {
				history = new IdosChatHistory();
			} else {
				history = histories.get(0);
				if (null != history.getChatMessage()) {
					msg.append(history.getChatMessage());
				}
			}
			if (msg.length() > 0) {
				msg.append("@-next-@");
			}
			msg.append(from).append("@-message-@").append(message);
			if (receivers.length > 1) {
				senderReceiver.append(from).append(",").append(to);
			} else {
				senderReceiver.append(from).append("-").append(to);
			}
			history.setChatDate(DateUtil.mysqldf.parse(chatDate));
			history.setChatMessage(msg.toString());
			history.setOrganization(user.getOrganization());
			history.setSenderReceiver(senderReceiver.toString());
			genericDAO.saveOrUpdate(history, user, entityManager);
			String newFrom = null;
			for (String receiver : receivers) {
				newFrom = getReceievers(receivers, receiver, from);
				// AdminActor.chat(newFrom.toString(), receiver, message, user.getFullName());
				result.put("type", "chat");
				result.put("from", newFrom.toString());
				result.put("to", receiver);
				result.put("message", message);
				result.put("name", user.getFullName());
				TransactionViewResponse.chat(newFrom.toString(), receiver, message, user.getFullName(), result);
			}
			result.put("name", user.getFullName());
			result.put("result", true);
			result.remove("message");
		}
		return result;
	}

	private String getReceievers(final String[] receivers, final String remove, final String from) {
		log.log(Level.FINE, "============ Start");
		String[] newRecs = receivers;
		List<String> recStrs = new ArrayList<String>(Arrays.asList(newRecs));
		recStrs.remove(recStrs.indexOf(remove));
		StringBuilder result = new StringBuilder();
		for (String rec : recStrs) {
			result.append(rec).append(",");
		}
		result.append(from);
		return result.toString();
	}

	@Override
	public ObjectNode getChatHistory(Users user, EntityManager entityManager, int month) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		if (null == user || (null == user.getEmail() || "".equals(user.getEmail()))) {
			result.put("message", "Please login again to continue.");
		} else {
			if (null == entityManager) {
				entityManager = EntityManagerProvider.getEntityManager();
			}
			Date curDate = Calendar.getInstance().getTime();
			String today = DateUtil.mysqldf.format(curDate);
			if (0 == month) {
				month = 1;
			}
			String previous = DateUtil.returnMonthsDate(month);
			StringBuilder query = new StringBuilder();
			query.append("SELECT obj FROM IdosChatHistory obj WHERE obj.presentStatus = 1");
			query.append(" AND LOCATE('").append(user.getEmail()).append("',obj.senderReceiver) > 0");
			query.append(" AND (obj.chatDate <= '").append(today).append("'");
			query.append(" AND obj.chatDate >= '").append(previous).append("')");
			query.append(" ORDER BY obj.chatDate DESC");
			List<IdosChatHistory> histories = genericDAO.executeSimpleQuery(query.toString(), entityManager);
			ObjectNode row = null, sr = null;
			ArrayNode datas = result.putArray("datas"), chats = null;
			String email = null, chat = null, chatDate = null;
			String[] emailMsg, chatsArr;
			if (null != histories && histories.size() > 0) {
				for (IdosChatHistory history : histories) {
					if (null != history) {
						row = Json.newObject();
						email = (null == history.getSenderReceiver()) ? "" : history.getSenderReceiver();
						if (email.contains(user.getEmail())) {
							email = email.replace(user.getEmail(), "");
							email = email.replace("-", "");
							email = email.replaceFirst(",", "");
							if (email.endsWith(",")) {
								email = email.substring(0, email.length() - 1);
							}
						}
						row.put("email", email);
						chatDate = (null == history.getChatDate()) ? "" : DateUtil.idosdf.format(history.getChatDate());
						row.put("chatDate", chatDate);
						chat = (null == history.getChatMessage()) ? "" : history.getChatMessage();
						emailMsg = chat.split("@-next-@");
						if (emailMsg.length > 0) {
							chats = row.putArray("messages");
							for (String s : emailMsg) {
								chatsArr = s.split("@-message-@");
								sr = Json.newObject();
								email = (null == chatsArr[0]) ? "" : chatsArr[0];
								chat = (null == chatsArr[1]) ? "" : chatsArr[1];
								sr.put("email", email);
								sr.put("chat", chat);
								chats.add(sr);
							}
						}
						datas.add(row);
					}
				}
			}
			result.put("result", true);
			result.remove("message");
		}
		return result;
	}

}
