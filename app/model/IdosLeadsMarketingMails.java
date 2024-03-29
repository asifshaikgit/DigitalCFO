package model;

import java.util.Date;

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
@Table(name = "IDOS_LEADS_MARKETING_MAILS")
public class IdosLeadsMarketingMails extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public IdosLeadsMarketingMails() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final long serialVersionUID = 1L;

	@Column(name = "NAME")
	private String name;

	@Column(name = "EMAIL")
	private String email;

	@Column(name = "DATE")
	private Date date;

	@Column(name = "COMMENTS")
	private String comments;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ASSOCIATED_IDOS_LEADS")
	private IdosLeads idosLeads;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public IdosLeads getIdosLeads() {
		return idosLeads;
	}

	public void setIdosLeads(IdosLeads idosLeads) {
		this.idosLeads = idosLeads;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IdosLeadsMarketingMails [name=");
		builder.append(name);
		builder.append(", email=");
		builder.append(email);
		builder.append(", date=");
		builder.append(date);
		builder.append(", comments=");
		builder.append(comments);
		builder.append("]");
		return builder.toString();
	}

	public static IdosLeadsMarketingMails findById(final long id) {
		return entityManager.find(IdosLeadsMarketingMails.class, id);
	}

}
