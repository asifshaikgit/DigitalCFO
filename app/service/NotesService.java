package service;

import model.IDOSNotes;
import model.Organization;
import model.Users;
import javax.persistence.EntityManager;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface NotesService extends  BaseService{

	public ObjectNode getUsers(final Organization org, EntityManager entityManager);
	public ObjectNode getProjects(final Organization org, EntityManager entityManager);
	public ObjectNode getUsers(final Organization org, ObjectNode on, EntityManager entityManager);
	public ObjectNode getProjects(final Organization org, ObjectNode on, EntityManager entityManager);
	public ObjectNode getBranches(final Organization org, EntityManager entityManager);
	public ObjectNode getBranches(final Organization org, ObjectNode on, EntityManager entityManager);
	public ObjectNode saveNote(final Long noteId, final Users creatingUser, final String users, final Long branch, final Long project, final String subject, final String note, final String file, final String transaction);
	public ObjectNode getNote(final IDOSNotes notes);
	public ObjectNode getAllNotes(final Users user);
	public ObjectNode getAllNotes(final Users user, final ObjectNode on);
	public ObjectNode getAllSharedNotes(final Users user);
	public ObjectNode getAllSharedNotes(final Users user, final ObjectNode on);
	public ObjectNode getNoteById(final Long id);
	public ObjectNode addRemark(final Long id, final Users user, final String remark, final String attachment);
	public ObjectNode getNotesNotification(final Users user);
	public ObjectNode getNotesNotification(final Users user, final ObjectNode on);
	public ObjectNode search(final Users user, final String keyword, final int days);
	public ObjectNode getTransactions(final Organization org) throws Exception;
	public ObjectNode getTransactions(final Organization org, ObjectNode on) throws Exception;
	public ObjectNode getClaimTransactions(final Organization org) throws Exception;
	public ObjectNode getClaimTransactions(final Organization org, ObjectNode on) throws Exception;

}
