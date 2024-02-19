package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="USER_USABILITY")
public class UserUsability extends AbstractBaseModel {
	
	@Column(name="ELEMENT")
	private String documentElement;
	
	@Column(name="USER")
	private String userEmail;
	
	@Column(name="IP_ADDRESS")
	private String ipAddress;

	public String getDocumentElement() {
		return documentElement;
	}

	public void setDocumentElement(String documentElement) {
		this.documentElement = documentElement;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
}
