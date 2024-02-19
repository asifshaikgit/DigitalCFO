package model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "IDOS_CHAT_HISTORY")
public class IdosChatHistory extends AbstractBaseModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -7716304056486561275L;

	@Column(name = "SENDER_RECEIVER")
	private String senderReceiver;

	@Column(name = "CHAT_MESSAGE")
	private String chatMessage;

	@Column(name = "CHAT_DATE")
	private Date chatDate;

	@Column(name = "CHAT_ATTACHMENT")
	private String chatAttachment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORGANIZATION_ID")
	private Organization organization;

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public String getSenderReceiver() {
		return senderReceiver;
	}

	public void setSenderReceiver(String senderReceiver) {
		this.senderReceiver = senderReceiver;
	}

	public String getChatMessage() {
		return chatMessage;
	}

	public void setChatMessage(String chatMessage) {
		this.chatMessage = chatMessage;
	}

	public Date getChatDate() {
		return chatDate;
	}

	public void setChatDate(Date chatDate) {
		this.chatDate = chatDate;
	}

	public String getChatAttachment() {
		return chatAttachment;
	}

	public void setChatAttachment(String chatAttachment) {
		this.chatAttachment = chatAttachment;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IdosChatHistory [senderReceiver=");
		builder.append(senderReceiver);
		builder.append(", chatMessage=");
		builder.append(chatMessage);
		builder.append(", chatDate=");
		builder.append(chatDate);
		builder.append(", chatAttachment=");
		builder.append(chatAttachment);
		builder.append(", organization=");
		builder.append(organization);
		builder.append("]");
		return builder.toString();
	}
}