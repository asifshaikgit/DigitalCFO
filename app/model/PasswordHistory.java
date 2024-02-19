package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "PASSWORD_HISTORY")
public class PasswordHistory extends AbstractBaseModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public PasswordHistory() {}
	
	public PasswordHistory(final String userEmail, final Users userId, final Organization orgId, final String password) {
		this.userEmail = userEmail;
		this.user = userId;
		this.organization = orgId;
		this.password = password;
	}
	
	@Column(name = "EMAIL")
	private String userEmail;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USER_ID")
	private Users user;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ORGANIZATION_ID")
	private Organization organization;
	
	@Column(name = "PASSWORD")
	private String password;

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
