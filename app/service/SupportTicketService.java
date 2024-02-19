package service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.ConfigParams;
import model.IdosRegisteredVendor;
import model.SupportTicket;
import model.SupportTicketComments;
import model.SupportTicketReply;
import model.Users;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import play.db.jpa.JPAApi;
import play.libs.Json;
import views.html.ticketCommentMail;
import views.html.ticketUpdateMail;

import com.idos.dao.GenericDAO;
import com.idos.util.PasswordUtil;
import javax.inject.Inject;
import controllers.Application;
import controllers.StaticController;

public class SupportTicketService implements BaseService {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Inject
	public SupportTicketService(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
		entityManager = EntityManagerProvider.getEntityManager();
	}

	public static ObjectNode createSupportTicket(final Users user, final String sub, final String msg,
			final String attachments, final IdosRegisteredVendor vendor, final String filePart,
			EntityManager entityManager) {
		log.log(Level.FINE, "============ Start");
		// EntityManager entityManager = entityManager;
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Unable to locate the user.");
		result.put("caseId", "");
		try {
			// String caseId = generateCaseId();
			String caseId = PasswordUtil.getResetPasswordToken().substring(0, 10);
			SupportTicket ticket = new SupportTicket();
			if (null != user) {
				ticket.setBranch(user.getBranch());
				ticket.setOrganization(user.getOrganization());
			} else {
				ticket.setVendor(vendor);
			}
			ticket.setMessage(msg);
			ticket.setSubject(sub);
			ticket.setSupportingAttachment(attachments);
			ticket.setCaseStatus(1);
			ticket.setCaseId(caseId);
			if (null != filePart && !"".equals(filePart)) {
				ticket.setAttachment(filePart);
			}
			transaction.begin();
			genericDAO.saveOrUpdate(ticket, user, entityManager);
			transaction.commit();
			result.put("result", true);
			result.put("message", "Your Email has been recieved. Your Case ID is " + caseId);
			result.put("caseId", caseId);
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
		return result;
	}

	public static ObjectNode getSupportTicketsById(final Users user, final String filter,
			final String filterValue, final IdosRegisteredVendor vendor, EntityManager entityManager) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		ArrayNode an = result.putArray("results");
		if (null != user || null != vendor) {
			StringBuilder query = new StringBuilder();
			query.append("SELECT obj FROM SupportTicket obj");
			if (null != user) {
				query.append(" WHERE obj.createdBy = ").append(user.getId()).append(" AND obj.branch.id = ")
						.append(user.getBranch().getId());
			} else if (null != vendor) {
				query.append(" WHERE obj.vendor = ").append(vendor.getId());
			}
			if (null != filterValue && !"".equals(filterValue)) {
				String[] arr = filterValue.split("/");
				if (arr[0].contains("7") || arr[0].contains("30")) {
					int value = Integer.parseInt(arr[0]);
					Calendar calendar = Calendar.getInstance();
					String currentDate = StaticController.mysqldf.format(calendar.getTime());
					currentDate += " 23:59:59";
					calendar.add(Calendar.DATE, -+value);
					String previousDate = StaticController.mysqldf.format(calendar.getTime());
					previousDate += " 00:00:00";
					query.append(
							" AND obj.createdAt <= '" + currentDate + "' AND obj.createdAt >= '" + previousDate + "'");
				}
				if (filterValue.contains("active")) {
					query.append(" AND obj.caseStatus = " + 1);
				}
				if ("searchtext".equalsIgnoreCase(filter)) {
					if (arr.length == 3) {
						query.append(
								" AND (obj.caseId LIKE '%" + arr[2] + "%' OR obj.subject LIKE '%" + arr[2] + "%')");
					}
				}
			}
			List<SupportTicket> supportTickets = genericDAO.executeSimpleQuery(query.toString(), entityManager);
			if (!supportTickets.isEmpty() && supportTickets.size() > 0) {
				for (SupportTicket ticket : supportTickets) {
					if (null != ticket) {
						ObjectNode row = Json.newObject();
						if (null != ticket.getId()) {
							row.put("ticketId", ticket.getId());
						} else {
							row.put("ticketId", "");
						}
						if (null != ticket.getCaseId()) {
							row.put("caseId", ticket.getCaseId());
						} else {
							row.put("caseId", "");
						}
						if (null != ticket.getSubject()) {
							row.put("subject", ticket.getSubject());
						} else {
							row.put("subject", "");
						}
						if (null != ticket.getAttachment()) {
							row.put("attachmentFile", ticket.getAttachment());
						} else {
							row.put("attachmentFile", "");
						}
						if (null != ticket.getModifiedAt()) {
							row.put("updated", StaticController.idosdf.format(ticket.getModifiedAt()));
						} else {
							row.put("updated", "");
						}
						if (null != ticket.getModifiedBy()) {
							row.put("updatedBy", ticket.getModifiedBy().getFullName());
						} else {
							row.put("updatedBy", "");
						}
						if (null != ticket.getCreatedAt()) {
							row.put("created", StaticController.idosdf.format(ticket.getCreatedAt()));
						} else {
							row.put("created", "");
						}
						if (null != ticket.getCreatedBy()) {
							row.put("createdBy", ticket.getCreatedBy().getFullName());
						} else if (null != ticket.getVendor()) {
							row.put("createdBy", ticket.getVendor().getVendorEmail());
						} else {
							row.put("createdBy", "");
						}
						if (null != ticket.getMessage()) {
							row.put("message", ticket.getMessage());
						} else {
							row.put("message", "");
						}
						if (null != ticket.getHelpful()) {
							row.put("helpful", ticket.getHelpful());
						} else {
							row.put("helpful", "");
						}
						if (null != ticket.getRating()) {
							row.put("rating", ticket.getRating());
						} else {
							row.put("rating", "");
						}
						if (null != ticket.getSupportingAttachment()) {
							row.put("attachment", ticket.getSupportingAttachment());
						} else {
							row.put("attachment", "");
						}
						if (null != ticket.getCaseStatus()) {
							row.put("statusNumber", ticket.getCaseStatus());
							if (ticket.getCaseStatus().equals(1)) {
								row.put("status", "Open");
							} else {
								row.put("status", "Closed");
							}
						} else {
							row.put("status", "");
						}
						if (null != ticket.getCaseAssignedTo()) {
							row.put("assigned", ticket.getCaseAssignedTo().getEmail());
							row.put("assignedName", ticket.getCaseAssignedTo().getFullName());
						} else {
							row.put("assigned", "");
							row.put("assignedName", "");
						}
						if (null != ticket.getCaseAttendedBy()) {
							row.put("attended", ticket.getCaseAttendedBy().getEmail());
							row.put("attendedName", ticket.getCaseAttendedBy().getFullName());
						} else {
							row.put("attended", "");
							row.put("attendedName", "");
						}
						ArrayNode subArray = null;
						ObjectNode subRow = null;
						if (null != ticket.getComments() && ticket.getComments().size() > 0) {
							subArray = row.putArray("comments");
							for (SupportTicketComments comment : ticket.getComments()) {
								if (null != comment) {
									subRow = Json.newObject();
									if (null != comment.getId()) {
										subRow.put("id", comment.getId());
									}
									if (null != comment.getComments()) {
										subRow.put("comment", comment.getComments());
									} else {
										subRow.put("comment", "");
									}
									if (null != comment.getCreatedAt()) {
										subRow.put("created", StaticController.mysqldtf.format(comment.getCreatedAt()));
									} else {
										subRow.put("created", "");
									}
									if (null != comment.getModifiedAt()) {
										subRow.put("modified",
												StaticController.mysqldtf.format(comment.getModifiedAt()));
									} else {
										subRow.put("modified", "");
									}
									if (null != comment.getCreatedBy()) {
										subRow.put("createdBy", comment.getCreatedBy().getEmail());
									} else if (null != comment.getSupportTicket().getVendor()) {
										subRow.put("createdBy",
												comment.getSupportTicket().getVendor().getVendorEmail());
									} else {
										subRow.put("createdBy", "");
									}
									if (null != comment.getModifiedBy()) {
										subRow.put("modifiedBy", comment.getModifiedBy().getEmail());
									} else {
										subRow.put("modifiedBy", "");
									}
									if (null != comment.getAttchements()) {
										subRow.put("attachment", comment.getAttchements());
									} else {
										subRow.put("attachment", "");
									}
									subArray.add(subRow);
								}
							}
						}
						if (null != ticket.getReplies() && ticket.getReplies().size() > 0) {
							subArray = row.putArray("replies");
							for (SupportTicketReply reply : ticket.getReplies()) {
								if (null != reply) {
									subRow = Json.newObject();
									if (null != reply.getId()) {
										subRow.put("id", reply.getId());
									}
									if (null != reply.getCreatedAt()) {
										subRow.put("created", StaticController.mysqldtf.format(reply.getCreatedAt()));
									} else {
										subRow.put("created", "");
									}
									if (null != reply.getModifiedAt()) {
										subRow.put("modified", StaticController.mysqldtf.format(reply.getModifiedAt()));
									} else {
										subRow.put("modified", "");
									}
									if (null != reply.getCreatedBy()) {
										subRow.put("createdBy", reply.getCreatedBy().getEmail());
									} else if (null != reply.getSupportTicket().getVendor()) {
										subRow.put("createdBy", reply.getSupportTicket().getVendor().getVendorEmail());
									} else {
										subRow.put("createdBy", "");
									}
									if (null != reply.getModifiedBy()) {
										subRow.put("modifiedBy", reply.getModifiedBy().getEmail());
									} else {
										subRow.put("modifiedBy", "");
									}
									if (null != reply.getEmail()) {
										subRow.put("to", reply.getEmail());
									} else {
										subRow.put("to", "");
									}
									if (null != reply.getMessage()) {
										subRow.put("message", reply.getMessage());
									} else {
										subRow.put("message", "");
									}
									if (null != reply.getSubject()) {
										subRow.put("subject", reply.getSubject());
									} else {
										subRow.put("subject", "");
									}
									if (null != reply.getAttachmentFileName()) {
										subRow.put("attachment", reply.getAttachmentFileName());
									} else {
										subRow.put("attachment", "");
									}
									subArray.add(subRow);
								}
							}
						}
						an.add(row);
					}
				}
			}
		}
		return result;
	}

	public static ObjectNode updateHelpFulRating(final Users user, final long id, final String value,
			final boolean isRating, final IdosRegisteredVendor vendor) {
		log.log(Level.FINE, "============ Start");
		// EntityManager entityManager = entityManager;
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		result.put("result", false);
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT obj FROM SupportTicket obj");
			if (null != user) {
				query.append(" WHERE obj.createdBy = ").append(user.getId());
			} else if (null != vendor) {
				query.append(" WHERE obj.vendor = ").append(vendor.getId());
			}
			query.append(" AND obj.id = ").append(id);
			List<SupportTicket> tickets = genericDAO.executeSimpleQueryWithLimit(query.toString(), entityManager, 1);
			if (!tickets.isEmpty() && tickets.size() > 0) {
				SupportTicket ticket = tickets.get(0);
				if (null != ticket) {
					if (!isRating) {
						ticket.setHelpful(Integer.valueOf(value));
					} else {
						ticket.setRating(value);
					}
					transaction.begin();
					genericDAO.saveOrUpdate(ticket, user, entityManager);
					transaction.commit();
					result.put("result", true);
					result.put("id", ticket.getId());
					if (!isRating) {
						result.put("help", ticket.getHelpful());
					} else {
						result.put("rate", ticket.getRating());
					}
				}
			}
		} catch (Exception ex) {
			if (transaction.isActive() && null != transaction) {
				transaction.rollback();
			}
		}
		return result;
	}

	public static ObjectNode addComment(final String ticketId, final String ticketNumber, final String comments,
			final String attachment, final Users user, final IdosRegisteredVendor vendor) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		EntityManager manager = entityManager;
		EntityTransaction transaction = manager.getTransaction();
		try {
			if ((null != ticketId || !"".equals(ticketId)) && (null != ticketNumber || !"".equals(ticketNumber))
					&& (null != comments || !"".equals(comments))) {
				StringBuilder query = new StringBuilder();
				query.append("SELECT obj FROM SupportTicket obj WHERE obj.id = ").append(ticketId);
				query.append(" AND obj.caseId = '").append(ticketNumber).append("'");
				List<SupportTicket> tickets = genericDAO.executeSimpleQueryWithLimit(query.toString(), entityManager,
						1);
				if (!tickets.isEmpty()) {
					SupportTicket ticket = tickets.get(0);
					log.log(Level.FINE, "Info " + ticket.getId());
					if (null != ticket) {
						SupportTicketComments comment = new SupportTicketComments();
						comment.setComments(comments);
						comment.setSupportTicket(ticket);
						comment.setAttchements(attachment);
						transaction.begin();
						genericDAO.saveOrUpdate(comment, user, manager);
						transaction.commit();
						String[] ccArray = new String[3];
						ccArray[0] = "supportadmin@myidos.com";
						if (null != ticket.getCreatedBy()) {
							ccArray[1] = ticket.getCreatedBy().getEmail();
						} else if (null != ticket.getVendor()) {
							ccArray[1] = ticket.getVendor().getVendorEmail();
						}
						String assigned = null, closed = null, modifiedName = "", sub = "", created = "";
						if (null != ticket.getCaseAssignedTo()) {
							assigned = ccArray[2] = ticket.getCaseAssignedTo().getEmail();
						}
						if (null != ticket.getCaseAttendedBy()) {
							closed = ticket.getCaseAttendedBy().getEmail();
						}
						if (null != ticket.getModifiedBy()) {
							modifiedName = ticket.getModifiedBy().getFullName();
						}
						if (null != ticket.getSubject()) {
							sub = ticket.getSubject();
						}
						if (null != ticket.getCreatedBy()) {
							created = ticket.getCreatedBy().getEmail();
						} else if (null != ticket.getVendor()) {
							created = ticket.getVendor().getVendorEmail();
						}
						List<InternetAddress> cc = new ArrayList<InternetAddress>(ccArray.length);
						for (String s : ccArray) {
							if (null != s && !"".equals(s)) {
								cc.add(new InternetAddress(s));
							}
						}
						String body = ticketCommentMail.render(modifiedName, ticket.getCaseId(), sub, comments, created,
								assigned, closed, ConfigParams.getInstance()).body();
						final String username = ConfigFactory.load().getString("smtp.user");
						// StaticController.mailTimerMultiple(body, username,
						// StaticController.alertSession(), ticket.getModifiedBy().getEmail(), cc,
						// ticket.getSubject());
						result.put("result", true);
						result.put("message", "Thank you for your comment!");
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			if (transaction.isActive() && null != transaction) {
				transaction.rollback();
			}
		}
		return result;
	}

	public static ObjectNode openOrCloseIssue(final String ticketId, final String ticketNumber, final Integer status,
			final Users user, final IdosRegisteredVendor vendor) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		EntityManager manager = entityManager;
		EntityTransaction transaction = manager.getTransaction();
		try {
			if ((null != ticketId && !"".equals(ticketId)) && (null != ticketNumber && !"".equals(ticketNumber))
					&& (null != status && !"".equals(status))) {
				StringBuilder query = new StringBuilder();
				query.append("SELECT obj FROM SupportTicket obj WHERE obj.id = ").append(ticketId);
				query.append(" AND obj.caseId = '").append(ticketNumber).append("'");
				List<SupportTicket> tickets = genericDAO.executeSimpleQueryWithLimit(query.toString(), entityManager,
						1);
				if (!tickets.isEmpty() && tickets.size() > 0) {
					SupportTicket ticket = tickets.get(0);
					if (null != ticket) {
						ticket.setCaseStatus(status);
						transaction.begin();
						genericDAO.saveOrUpdate(ticket, user, manager);
						transaction.commit();
						result.put("result", true);
						result.put("id", ticket.getId());
						result.put("status", ticket.getCaseStatus());
						String content = null;
						String[] ccArray = new String[2];
						if (ticket.getCaseStatus().equals(1)) {
							content = " has re-opened the issue.";
						} else {
							content = " has closed the issue.";
						}
						ccArray[0] = "supportadmin@myidos.com";
						if (null != ticket.getCreatedBy()) {
							ccArray[1] = ticket.getCreatedBy().getEmail();
						} else if (null != ticket.getVendor()) {
							ccArray[1] = ticket.getVendor().getVendorEmail();
						}
						List<InternetAddress> cc = new ArrayList<InternetAddress>(ccArray.length);
						for (String s : ccArray) {
							cc.add(new InternetAddress(s));
						}
						String assigned = null, closed = null, modifiedName = "", subject = "", created = "";
						if (null != ticket.getCaseAssignedTo()) {
							assigned = ticket.getCaseAssignedTo().getEmail();
						}
						if (null != ticket.getCaseAttendedBy()) {
							closed = ticket.getCaseAttendedBy().getEmail();
						}
						if (null != ticket.getModifiedBy()) {
							modifiedName = ticket.getModifiedBy().getFullName();
						}
						if (null != ticket.getCreatedBy()) {
							created = ticket.getCreatedBy().getEmail();
						} else if (null != ticket.getVendor()) {
							created = ticket.getVendor().getVendorEmail();
						}
						if (null != ticket.getSubject()) {
							subject = ticket.getSubject();
						}
						String body = ticketUpdateMail.render(modifiedName, ticket.getCaseId(), subject, content,
								created, assigned, closed, ConfigParams.getInstance()).body();
						final String username = ConfigFactory.load().getString("smtp.user");
						// StaticController.mailTimerMultiple(body, username,
						// StaticController.alertSession(), ticket.getModifiedBy().getEmail(), cc,
						// ticket.getSubject());
					}
				}
			}
		} catch (Exception ex) {
			if (transaction.isActive() && null != transaction) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
		}
		return result;
	}

	public static IdosRegisteredVendor getRegisteredUser(final String email) {
		log.log(Level.FINE, "============ Start");
		IdosRegisteredVendor vendor = null;
		if (null != email && !"".equals(email)) {
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("vendorEmail", email);
			criterias.put("presentStatus", 1);
			vendor = genericDAO.getByCriteria(IdosRegisteredVendor.class, criterias, entityManager);
		}
		return vendor;
	}
}