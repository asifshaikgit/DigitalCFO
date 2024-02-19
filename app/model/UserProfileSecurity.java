package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="USER_SECURITY_PROFILE")
public class UserProfileSecurity extends AbstractBaseModel{
	
	@Column(name="SECURITY_QUESTION")
	private String securityQuestion;
	
	@Column(name="SECURED_ANSWER")
	private String securedAnswer;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USERS_ID")
	private Users user;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USERS_ORGANIZATION_ID")
	private Organization organization;

	public String getSecurityQuestion() {
		return securityQuestion;
	}

	public void setSecurityQuestion(String securityQuestion) {
		this.securityQuestion = securityQuestion;
	}

	public String getSecuredAnswer() {
		return securedAnswer;
	}

	public void setSecuredAnswer(String securedAnswer) {
		this.securedAnswer = securedAnswer;
	}

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
}
