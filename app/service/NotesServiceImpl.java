package service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Branch;
import model.ClaimTransaction;
import model.IDOSNotes;
import model.IdosNotesToTransaction;
import model.Organization;
import model.Project;
import model.Transaction;
import model.Users;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import com.idos.util.DateUtil;
import com.idos.util.PasswordUtil;
import javax.inject.Inject;
import controllers.StaticController;
import play.db.jpa.JPAApi;
import play.libs.Json;

public class NotesServiceImpl implements NotesService {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	private static DashboardService dashboardService = new DashboardServiceImpl();

	@Override
	public ObjectNode getUsers(Organization org, EntityManager entityManager) {
		ObjectNode on = Json.newObject();
		on = getUsers(org, on, entityManager);
		return on;
	}

	@Override
	public ObjectNode getProjects(Organization org, EntityManager entityManager) {
		log.log(Level.FINE, "============ Start");
		ObjectNode on = Json.newObject();
		on = getProjects(org, on, entityManager);
		return on;
	}

	@Override
	public ObjectNode getUsers(Organization org, ObjectNode on, EntityManager entityManager) {
		log.log(Level.FINE, "============ Start");
		on = dashboardService.getBranchesOrProjectsOrOperationalData(org, 7, entityManager);
		return on;
	}

	@Override
	public ObjectNode getProjects(Organization org, ObjectNode on, EntityManager entityManager) {
		log.log(Level.FINE, "============ Start");
		on = dashboardService.getBranchesOrProjectsOrOperationalData(org, 4, entityManager);
		return on;
	}

	@Override
	public ObjectNode getBranches(Organization org, EntityManager entityManager) {
		log.log(Level.FINE, "============ Start");
		ObjectNode on = Json.newObject();
		on = getBranches(org, on, entityManager);
		return on;
	}

	@Override
	public ObjectNode getBranches(Organization org, ObjectNode on, EntityManager entityManager) {
		log.log(Level.FINE, "============ Start");
		on = dashboardService.getBranchesOrProjectsOrOperationalData(org, 2, entityManager);
		return on;
	}

	@Override
	public ObjectNode saveNote(Long noteId, Users creatingUser, String users,
			Long branch, Long project, String subject, String note, String file, String pTransaction) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		if (null != creatingUser && null != noteId && !"".equals(noteId)) {
			IDOSNotes idosNote = IDOSNotes.findById(noteId);
			if (null == idosNote) {
				idosNote = new IDOSNotes();
				String refNumber = PasswordUtil.gen(10);
				idosNote.setNotesReferenceNumber(refNumber);
			}
			idosNote.setNotesBodyContenet(note);
			idosNote.setNotesSharedUserEmails(users);
			idosNote.setNotesSupportingDocuments(file);
			idosNote.setTitleSubject(subject);
			if (null != branch) {
				idosNote.setAssociatedBranch(Branch.findById(branch));
			}
			if (null != project) {
				idosNote.setAssociatedProjects(Project.findById(project));
			}
			if (null != creatingUser.getOrganization()) {
				idosNote.setAssociatedOrganization(creatingUser.getOrganization());
			}
			EntityTransaction transaction = entityManager.getTransaction();
			transaction.begin();
			genericDAO.saveOrUpdate(idosNote, creatingUser, entityManager);
			if (null != pTransaction) {
				String[] arr = pTransaction.split("_");
				if (arr.length == 2) {
					IdosNotesToTransaction toTransaction = null;
					Map<String, Object> criterias = new HashMap<String, Object>();
					criterias.put("idosNotes.id", idosNote.getId());
					criterias.put("presentStatus", 1);
					toTransaction = genericDAO.getByCriteria(IdosNotesToTransaction.class, criterias, entityManager);
					if (null == toTransaction) {
						toTransaction = new IdosNotesToTransaction();
					}
					toTransaction.setIdosNotes(idosNote);
					long fetchId = Long.parseLong(arr[1]);
					if ("txn".equalsIgnoreCase(arr[0])) {
						toTransaction.setIncomeExpenseTransaction(Transaction.findById(fetchId));
						toTransaction.setClaimsTransaction(null);
					} else if ("clm".equalsIgnoreCase(arr[0])) {
						toTransaction.setClaimsTransaction(ClaimTransaction.findById(fetchId));
						toTransaction.setIncomeExpenseTransaction(null);
					}
					if (null != toTransaction.getClaimsTransaction()
							|| null != toTransaction.getIncomeExpenseTransaction()) {
						genericDAO.saveOrUpdate(toTransaction, creatingUser, entityManager);
					}
				}
			}
			transaction.commit();
			result = getNote(idosNote);
			result.put("result", true);
		}
		return result;
	}

	@Override
	public ObjectNode getNote(IDOSNotes notes) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = null;
		if (null != notes && null != notes.getId()) {
			result = Json.newObject();
			ObjectNode row = null;
			ArrayNode an = null;
			result.put("id", notes.getId());
			if (null != notes.getNotesBodyContenet()) {
				result.put("note", notes.getNotesBodyContenet());
			} else {
				result.put("note", "");
			}
			if (null != notes.getNotesReferenceNumber()) {
				result.put("refNumber", notes.getNotesReferenceNumber());
			} else {
				result.put("refNumber", "");
			}
			an = result.putArray("remarks");
			if (null != notes.getNotesRemarks()) {
				getRemarks(notes, result);
			} else {
				result.put("remarks", "");
			}
			if (null != notes.getTitleSubject()) {
				result.put("subject", notes.getTitleSubject());
			} else {
				result.put("subject", "");
			}
			if (null != notes.getCreatedAt()) {
				result.put("created", StaticController.idosdf.format(notes.getCreatedAt()));
			} else {
				result.put("created", "");
			}
			if (null != notes.getCreatedBy()) {
				result.put("createdBy", notes.getCreatedBy().getEmail());
			} else {
				result.put("createdBy", "");
			}
			if (null != notes.getModifiedAt()) {
				result.put("modified", StaticController.idosdf.format(notes.getModifiedAt()));
			} else {
				result.put("modified", "");
			}
			if (null != notes.getModifiedBy()) {
				result.put("modifiedBy", notes.getModifiedBy().getEmail());
			} else {
				result.put("modifiedBy", "");
			}
			an = result.putArray("users");
			if (null != notes.getNotesSharedUserEmails()) {
				String[] arr = notes.getNotesSharedUserEmails().split(",");
				for (String s : arr) {
					if (null != s && !"".equals(s)) {
						row = Json.newObject();
						row.put("email", s);
						an.add(row);
					}
				}
			}
			an = result.putArray("files");
			if (null != notes.getNotesSupportingDocuments()) {
				String[] arr = notes.getNotesSupportingDocuments().split(",");
				for (String s : arr) {
					if (null != s && !"".equals(s)) {
						row = Json.newObject();
						row.put("file", s);
						an.add(row);
					}
				}
			}
			if (null != notes.getAssociatedBranch()) {
				if (null != notes.getAssociatedBranch().getId()) {
					result.put("branchId", notes.getAssociatedBranch().getId());
					if (null != notes.getAssociatedBranch().getName()) {
						result.put("branchName", notes.getAssociatedBranch().getName());
					} else {
						result.put("branchName", "");
					}
				} else {
					result.put("branchId", "");
				}
			} else {
				result.put("branchId", "");
				result.put("branchName", "");
			}
			if (null != notes.getAssociatedProjects()) {
				if (null != notes.getAssociatedProjects().getId()) {
					result.put("projectId", notes.getAssociatedProjects().getId());
					if (null != notes.getAssociatedProjects().getName()) {
						result.put("projectName", notes.getAssociatedProjects().getName());
					} else {
						result.put("projectName", "");
					}
				} else {
					result.put("projectId", "");
				}
			} else {
				result.put("projectId", "");
				result.put("projectName", "");
			}
			if (null != notes.getAssociatedOrganization()) {
				if (null != notes.getAssociatedOrganization().getId()) {
					result.put("orgId", notes.getAssociatedOrganization().getId());
					if (null != notes.getAssociatedOrganization().getName()) {
						result.put("orgName", notes.getAssociatedOrganization().getName());
					} else {
						result.put("orgName", "");
					}
				} else {
					result.put("orgId", "");
				}
			} else {
				result.put("orgId", "");
				result.put("orgName", "");
			}
			if (null != notes.getCreatedBy()) {
				Users user = notes.getCreatedBy();
				String roles = StaticController.getUserRoles(user);
				if (roles.contains("ACCOUNTANT") || roles.contains("AUDITOR")) {
					Map<String, Object> criterias = new HashMap<String, Object>();
					criterias.put("idosNotes.id", notes.getId());
					criterias.put("presentStatus", 1);
					IdosNotesToTransaction transaction = genericDAO.getByCriteria(IdosNotesToTransaction.class,
							criterias, entityManager);
					if (null != transaction && null != transaction.getId()) {
						result.put("notesTxn", transaction.getId());
						if (null != transaction.getClaimsTransaction()
								&& null != transaction.getClaimsTransaction().getId()) {
							result.put("transaction", "clm_" + transaction.getClaimsTransaction().getId());
							result.put("transactionRef", transaction.getClaimsTransaction().getTransactionRefNumber());
						} else if (null != transaction.getIncomeExpenseTransaction()
								&& null != transaction.getIncomeExpenseTransaction().getId()) {
							result.put("transaction", "txn_" + transaction.getIncomeExpenseTransaction().getId());
							result.put("transactionRef",
									transaction.getIncomeExpenseTransaction().getTransactionRefNumber());
						} else {
							result.put("transaction", "");
							result.put("transactionRef", "");
						}
					} else {
						result.put("transaction", "");
						result.put("transactionRef", "");
					}
				} else {
					result.put("transaction", "");
					result.put("transactionRef", "");
				}
			} else {
				result.put("transaction", "");
				result.put("transactionRef", "");
			}
		}
		return result;

	}

	@Override
	public ObjectNode getAllNotes(Users user) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result = getAllNotes(user, result);
		return result;
	}

	@Override
	public ObjectNode getAllSharedNotes(Users user) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result = getAllSharedNotes(user, result);
		return result;
	}

	@Override
	public ObjectNode getAllNotes(Users user, ObjectNode on) {
		log.log(Level.FINE, "============ Start");
		if (null != user && null != user.getId()) {
			List<IDOSNotes> notes = listAllNotes(user);
			if (!notes.isEmpty() && notes.size() > 0) {
				ArrayNode an = on.putArray("createdNotes");
				for (IDOSNotes note : notes) {
					if (null != note) {
						an.add(getNote(note));
					}
				}
			}
		}
		return on;
	}

	private static List<IDOSNotes> listAllNotes(final Users user) {
		log.log(Level.FINE, "============ Start");
		Map<String, Object> criterias = new LinkedHashMap<String, Object>();
		criterias.put("createdBy.id", user.getId());
		criterias.put("associatedOrganization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		return genericDAO.findByCriteria(IDOSNotes.class, criterias, entityManager);
	}

	private static List<IDOSNotes> listAllSharedNotes(final Users user) {
		log.log(Level.FINE, "============ Start");
		StringBuilder query = new StringBuilder();
		query.append("SELECT obj FROM IDOSNotes obj WHERE obj.presentStatus = 1");
		query.append(" AND LOCATE('").append(user.getEmail()).append("', obj.notesSharedUserEmails) > 0");
		return genericDAO.executeSimpleQuery(query.toString(), entityManager);
	}

	@Override
	public ObjectNode getAllSharedNotes(Users user, ObjectNode on) {
		log.log(Level.FINE, "============ Start");
		if (null != user && null != user.getId()) {
			List<IDOSNotes> notes = listAllSharedNotes(user);
			if (!notes.isEmpty() && notes.size() > 0) {
				ArrayNode an = on.putArray("sharedNotes");
				for (IDOSNotes note : notes) {
					if (null != note) {
						an.add(getNote(note));
					}
				}
			}
		}
		return on;
	}

	@Override
	public ObjectNode getNoteById(Long id) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = null;
		if (null != id && id > 0) {
			IDOSNotes note = IDOSNotes.findById(id);
			result = getNote(note);
		}
		return result;
	}

	@Override
	public ObjectNode addRemark(Long id, Users user, String remark,
			String attachment) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Something went wrong. Please try again later.");
		if (null != id && !"".equals(id)) {
			IDOSNotes note = IDOSNotes.findById(id);
			if (null != note) {
				StringBuilder newRemark = null;
				if (null != note.getNotesRemarks()) {
					newRemark = new StringBuilder(note.getNotesRemarks());
				} else {
					newRemark = new StringBuilder();
				}
				newRemark.append(user.getEmail()).append("@-rmk-@");
				newRemark.append(remark).append("@-rmkAtt-@");
				newRemark.append(attachment).append("@-dt-@");
				newRemark.append(StaticController.mysqldtf.format(Calendar.getInstance().getTime())).append("@-nxt-@");
				note.setNotesRemarks(newRemark.toString());
				EntityTransaction transaction = entityManager.getTransaction();
				transaction.begin();
				genericDAO.saveOrUpdate(note, user, entityManager);
				transaction.commit();
				getRemarks(note, result);
				result.put("result", true);
				result.remove("message");
			}
		}
		return result;
	}

	private static void getRemarks(final IDOSNotes note, ObjectNode result) {
		log.log(Level.FINE, "============ Start");
		ObjectNode row = null;
		ArrayNode an = result.putArray("remarks");
		String remarks = note.getNotesRemarks();
		String[] individualRemark = remarks.split("@-nxt-@");
		if (individualRemark.length > 0) {
			for (String ir : individualRemark) {
				if (null != ir && !"".equals(ir)) {
					row = Json.newObject();
					String[] arr = ir.split("@-rmk-@");
					if (null != arr[0]) {
						row.put("email", arr[0]);
					} else {
						row.put("email", "");
					}
					arr = arr[1].split("@-rmkAtt-@");
					if (null != arr[0]) {
						row.put("remark", arr[0]);
					} else {
						row.put("remark", "");
					}
					arr = arr[1].split("@-dt-@");
					if (null != arr[0]) {
						row.put("remarkAttachment", arr[0]);
					} else {
						row.put("remarkAttachment", "");
					}
					arr = arr[1].split("@-nxt-@");
					if (null != arr[0]) {
						row.put("created", arr[0]);
					} else {
						row.put("created", "");
					}
					an.add(row);
				}
			}
		}
	}

	@Override
	public ObjectNode getNotesNotification(Users user) {
		log.log(Level.FINE, "============ Start 453");
		ObjectNode result = Json.newObject();
		result = getNotesNotification(user, result);
		return result;
	}

	@Override
	public ObjectNode getNotesNotification(Users user, ObjectNode on) {
		log.log(Level.FINE, "============ Start 461");
		List<IDOSNotes> notes = listAllNotes(user);
		long total = notes.size();
		notes = listAllSharedNotes(user);
		on.put("sharedNotes", notes.size());
		total += notes.size();
		on.put("totalNotes", total);
		String query = createSearchQuery(user, null, 7, true);
		notes = genericDAO.executeSimpleQuery(query, entityManager);
		total = notes.size();
		query = createSearchQuery(user, null, 7, false);
		notes = genericDAO.executeSimpleQuery(query, entityManager);
		total += notes.size();
		on.put("newNotes", total);
		return on;
	}

	@Override
	public ObjectNode search(Users user, String keyword, int days) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Something went wrong. Please try again later");
		if (null != user) {
			ArrayNode an = result.putArray("notes");
			String query = createSearchQuery(user, keyword, days, true);
			List<IDOSNotes> notes = genericDAO.executeSimpleQuery(query, entityManager);
			if (!notes.isEmpty() && notes.size() > 0) {
				for (IDOSNotes note : notes) {
					if (null != note) {
						an.add(getNote(note));
					}
				}
			}
			query = createSearchQuery(user, keyword, days, false);
			notes = genericDAO.executeSimpleQuery(query, entityManager);
			if (!notes.isEmpty() && notes.size() > 0) {
				for (IDOSNotes note : notes) {
					if (null != note) {
						an.add(getNote(note));
					}
				}
			}
			result.put("result", true);
			result.remove("message");
		}
		return result;
	}

	private String createSearchQuery(Users user, String keyword, int days, boolean inOrg) {
		log.log(Level.FINE, "============ Start");
		StringBuilder query = new StringBuilder();
		query.append("SELECT obj FROM IDOSNotes obj WHERE obj.presentStatus = 1");
		if (inOrg) {
			query.append(" AND obj.createdBy.id = ").append(user.getId());
			query.append(" AND obj.associatedOrganization.id = ").append(user.getOrganization().getId());
		} else {
			query.append(" AND LOCATE('").append(user.getEmail()).append("', obj.notesSharedUserEmails) > 0");
		}
		if (null != keyword && !"".equals(keyword.trim())) {
			query.append(" AND (obj.titleSubject LIKE '%").append(keyword).append("%'");
			query.append(" OR obj.notesReferenceNumber LIKE '%").append(keyword).append("%')");
		}
		if (days > 0) {
			Calendar calendar = Calendar.getInstance();
			String currentDate = StaticController.mysqldf.format(calendar.getTime());
			currentDate += " 23:59:59";
			calendar.add(Calendar.DATE, -+days);
			String previousDate = StaticController.mysqldf.format(calendar.getTime());
			previousDate += " 00:00:00";
			query.append(" AND obj.createdAt <= '" + currentDate + "' AND obj.createdAt >= '" + previousDate + "'");
		}
		return query.toString();
	}

	@Override
	public ObjectNode getTransactions(Organization org) throws Exception {
		log.log(Level.FINE, "============ Start");
		ObjectNode on = Json.newObject();
		on = getTransactions(org, on);
		return on;
	}

	@Override
	public ObjectNode getTransactions(Organization org, ObjectNode on) throws Exception {
		log.log(Level.FINE, "============ Start");
		try {
			if (null == on) {
				on = Json.newObject();
			}
			if (null != org && null != org.getId()) {
				ObjectNode sr = null;
				ArrayNode an = on.putArray("result");
				Map<String, Object> criterias = new HashMap<String, Object>();
				criterias.put("transactionBranchOrganization.id", org.getId());
				criterias.put("presentStatus", 1);
				String oldDate = DateUtil.returnTwoYearBackDate();
				String curDate = StaticController.mysqldf.format(Calendar.getInstance().getTime());
				StringBuilder query = new StringBuilder();
				query.append("SELECT obj FROM Transaction obj WHERE obj.presentStatus = 1");
				query.append(" AND obj.createdAt <= '" + curDate + "' AND obj.createdAt >= '" + oldDate + "'");
				List<Transaction> transactions = genericDAO.executeSimpleQuery(query.toString(), entityManager);
				if (null != transactions && !transactions.isEmpty() && transactions.size() > 0) {
					for (Transaction transaction : transactions) {
						if (null != transaction && null != transaction.getId()) {
							sr = Json.newObject();
							sr.put("itemId", transaction.getId());
							if (null != transaction.getTransactionRefNumber()) {
								sr.put("itemName", transaction.getTransactionRefNumber());
							} else {
								sr.put("itemName", "");
							}
						}
						an.add(sr);
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return on;
	}

	@Override
	public ObjectNode getClaimTransactions(Organization org) throws Exception {
		log.log(Level.FINE, "============ Start");
		ObjectNode on = Json.newObject();
		on = getClaimTransactions(org, on);
		return on;
	}

	@Override
	public ObjectNode getClaimTransactions(Organization org, ObjectNode on)
			throws Exception {
		log.log(Level.FINE, "============ Start");

		try {
			if (null == on) {
				on = Json.newObject();
			}
			if (null != org && null != org.getId()) {
				ObjectNode sr = null;
				ArrayNode an = on.putArray("result");
				Map<String, Object> criterias = new HashMap<String, Object>();
				criterias.put("transactionBranchOrganization.id", org.getId());
				criterias.put("presentStatus", 1);
				String oldDate = DateUtil.returnTwoYearBackDate();
				String curDate = StaticController.mysqldf.format(Calendar.getInstance().getTime());
				StringBuilder query = new StringBuilder();
				query.append("SELECT obj FROM ClaimTransaction obj WHERE obj.presentStatus = 1");
				query.append(" AND obj.createdAt <= '" + curDate + "' AND obj.createdAt >= '" + oldDate + "'");
				List<ClaimTransaction> transactions = genericDAO.executeSimpleQuery(query.toString(), entityManager);
				if (null != transactions && !transactions.isEmpty() && transactions.size() > 0) {
					for (ClaimTransaction transaction : transactions) {
						if (null != transaction && null != transaction.getId()) {
							sr = Json.newObject();
							sr.put("itemId", transaction.getId());
							if (null != transaction.getTransactionRefNumber()) {
								sr.put("itemName", transaction.getTransactionRefNumber());
							} else {
								sr.put("itemName", "");
							}
						}
						an.add(sr);
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return on;
	}

}
