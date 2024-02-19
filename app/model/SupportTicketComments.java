package model;

import com.idos.util.IdosUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="SUPPORT_TICKET_COMMENT")
public class SupportTicketComments extends AbstractBaseModel {
	
	@Column(name="COMMENTS")
	private String comments;
	
	@Column(name="ATTACHMENTS")
	private String attchements;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SUPPORT_TICKED_ID")
	private SupportTicket supportTicket;

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments =  IdosUtil.escapeHtml(comments);
	}

	public SupportTicket getSupportTicket() {
		return supportTicket;
	}

	public void setSupportTicket(SupportTicket supportTicket) {
		this.supportTicket = supportTicket;
	}

	public String getAttchements() {
		return attchements;
	}

	public void setAttchements(String attchements) {
		this.attchements = attchements;
	}
}
