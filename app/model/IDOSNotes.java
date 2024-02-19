package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "IDOS_NOTES")
public class IDOSNotes extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public IDOSNotes() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "TITLE_SUBJECT")
	public String titleSubject;

	@Column(name = "NOTES_REFERENCE_NUMBER")
	public String notesReferenceNumber;

	@Column(name = "NOTES_SHARED_USER_EMAILS")
	public String notesSharedUserEmails;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ASSOCIATED_BRANCH")
	private Branch associatedBranch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ASSOCIATED_ORGANIZATION")
	private Organization associatedOrganization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ASSOCIATED_PROJECT")
	private Project associatedProjects;

	@Column(name = "NOTES_BODY_CONTENTS")
	public String notesBodyContenet;

	@Column(name = "NOTES_REMARKS")
	public String notesRemarks;

	@Column(name = "NOTES_SUPPORTING_DOCUMENTS")
	public String notesSupportingDocuments;

	public String getTitleSubject() {
		return titleSubject;
	}

	public void setTitleSubject(String titleSubject) {
		this.titleSubject = titleSubject;
	}

	public String getNotesReferenceNumber() {
		return notesReferenceNumber;
	}

	public void setNotesReferenceNumber(String notesReferenceNumber) {
		this.notesReferenceNumber = notesReferenceNumber;
	}

	public String getNotesSharedUserEmails() {
		return notesSharedUserEmails;
	}

	public void setNotesSharedUserEmails(String notesSharedUserEmails) {
		this.notesSharedUserEmails = notesSharedUserEmails;
	}

	public Branch getAssociatedBranch() {
		return associatedBranch;
	}

	public void setAssociatedBranch(Branch associatedBranch) {
		this.associatedBranch = associatedBranch;
	}

	public Organization getAssociatedOrganization() {
		return associatedOrganization;
	}

	public void setAssociatedOrganization(Organization associatedOrganization) {
		this.associatedOrganization = associatedOrganization;
	}

	public Project getAssociatedProjects() {
		return associatedProjects;
	}

	public void setAssociatedProjects(Project associatedProjects) {
		this.associatedProjects = associatedProjects;
	}

	public String getNotesBodyContenet() {
		return notesBodyContenet;
	}

	public void setNotesBodyContenet(String notesBodyContenet) {
		this.notesBodyContenet = notesBodyContenet;
	}

	public String getNotesRemarks() {
		return notesRemarks;
	}

	public void setNotesRemarks(String notesRemarks) {
		this.notesRemarks = notesRemarks;
	}

	public String getNotesSupportingDocuments() {
		return notesSupportingDocuments;
	}

	public void setNotesSupportingDocuments(String notesSupportingDocuments) {
		this.notesSupportingDocuments = notesSupportingDocuments;
	}

	public static IDOSNotes findById(final Long id) {
		return entityManager.find(IDOSNotes.class, id);
	}
}
