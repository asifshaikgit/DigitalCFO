package model;

import com.idos.util.IdosUtil;
import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="SUPPORT_TICKET_REPLY")
public class SupportTicketReply extends AbstractBaseModel {
	
	@Column(name="EMAIL", length = 256, unique = true, nullable = false)
	@Constraints.MaxLength(256)
	@Constraints.Required
	@Constraints.Email
	private String email;
	
	@Column(name="SUBJECT")
	private String subject;
	
	@Column(name="MESSAGE")
	private String message;
	
	@Column(name="ATTACHMENT_FILE_NAME")
	private String attachmentFileName;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SUPPORT_TICKED_ID")
	private SupportTicket supportTicket;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject =  IdosUtil.escapeHtml(subject);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message =  IdosUtil.escapeHtml(message);
	}

	public String getAttachmentFileName() {
		return attachmentFileName;
	}

	public void setAttachmentFileName(String attachmentFileName) {
		this.attachmentFileName = attachmentFileName;
	}

	public SupportTicket getSupportTicket() {
		return supportTicket;
	}

	public void setSupportTicket(SupportTicket supportTicket) {
		this.supportTicket = supportTicket;
	}
}
