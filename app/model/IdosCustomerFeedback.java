package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="IDOS_CUSTOMER_FEEDBACK")
public class IdosCustomerFeedback extends AbstractBaseModel {
	
	public IdosCustomerFeedback() {
		super();
	}

	public IdosCustomerFeedback(String name, String number,String subject,String sentFrom,String feedbackText) {
		super();
		this.name = name;
		this.number = number;
		this.subject = subject;
		this.sentFrom = sentFrom;
		this.feedbackText = feedbackText;
	}

	@Column(name="NAME")
	private String name;
	
	@Column(name="NUMBER")
	private String number;
	
	@Column(name="SUBJECT")
	private String subject;
	
	@Column(name="SENT_FROM")
	private String sentFrom;
	
	@Column(name="FEEDBACK_TEXT")
	private String feedbackText;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSentFrom() {
		return sentFrom;
	}

	public void setSentFrom(String sentFrom) {
		this.sentFrom = sentFrom;
	}

	public String getFeedbackText() {
		return feedbackText;
	}

	public void setFeedbackText(String feedbackText) {
		this.feedbackText = feedbackText;
	}
}
