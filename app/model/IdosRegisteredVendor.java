package model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.*;

import com.idos.util.PasswordUtil;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "IDOS_REGISTERED_VENDOR")
public class IdosRegisteredVendor extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public IdosRegisteredVendor() {
		super();
		entityManager = EntityManagerProvider.getEntityManager();
	}

	public IdosRegisteredVendor(String vendorName, String vendorEmail,
			String vendorAccountPassword) {
		super();
		this.vendorName = vendorName;
		this.vendorEmail = vendorEmail;
		this.vendorAccountPassword = vendorAccountPassword;
	}

	@Column(name = "VENDOR_NAME")
	private String vendorName;

	@Column(name = "VENDOR_EMAIL")
	private String vendorEmail;

	@Column(name = "VENDOR_PHONE_NUMBER")
	private String vendorPhoneNumber;

	@Column(name = "VENDOR_REGISTRATION_NUMBER")
	private Double vendorRegistrationNumber;

	@Column(name = "VENDOR_ACCOUNT_PASSWORD")
	private String vendorAccountPassword;

	@Column(name = "LAST_UPDATED_PASSWORD")
	private Date lastUpdatedPassword;

	@Column(name = "NUMBER_OF_TIMES_SEARCHED")
	private Integer numberOfTimesSearched = 0;

	@Column(name = "NUMBER_OF_TIMES_CONTACTED")
	private Integer numberOfTimesContacted = 0;

	public Date getLastUpdatedPassword() {
		return lastUpdatedPassword;
	}

	public void setLastUpdatedPassword(Date lastUpdatedPassword) {
		this.lastUpdatedPassword = lastUpdatedPassword;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public Integer getRegVendInSession() {
		return regVendInSession;
	}

	public void setRegVendInSession(Integer regVendInSession) {
		this.regVendInSession = regVendInSession;
	}

	@Column(name = "LAST_LOGIN_DATE")
	private Date lastLoginDate;

	@Column(name = "REGISTERED_VENDOR_IN_SESSION")
	private Integer regVendInSession = 0;

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getVendorEmail() {
		return vendorEmail;
	}

	public void setVendorEmail(String vendorEmail) {
		this.vendorEmail = vendorEmail;
	}

	public String getVendorPhoneNumber() {
		return vendorPhoneNumber;
	}

	public void setVendorPhoneNumber(String vendorPhoneNumber) {
		this.vendorPhoneNumber = vendorPhoneNumber;
	}

	public Double getVendorRegistrationNumber() {
		return vendorRegistrationNumber;
	}

	public void setVendorRegistrationNumber(Double vendorRegistrationNumber) {
		this.vendorRegistrationNumber = vendorRegistrationNumber;
	}

	public String getVendorAccountPassword() {
		return vendorAccountPassword;
	}

	public void setVendorAccountPassword(String vendorAccountPassword) {
		this.vendorAccountPassword = vendorAccountPassword;
	}

	public Integer getNumberOfTimesSearched() {
		return numberOfTimesSearched;
	}

	public void setNumberOfTimesSearched(Integer numberOfTimesSearched) {
		this.numberOfTimesSearched = numberOfTimesSearched;
	}

	public Integer getNumberOfTimesContacted() {
		return numberOfTimesContacted;
	}

	public void setNumberOfTimesContacted(Integer numberOfTimesContacted) {
		this.numberOfTimesContacted = numberOfTimesContacted;
	}

	public String getAuthToken() {
		return this.authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	@Column(name = "auth_token")
	private String authToken;

	public String createToken() {
		authToken = UUID.randomUUID().toString();
		authToken = "se11" + authToken;
		return authToken;
	}

	public static IdosRegisteredVendor findByAuthToken(EntityManager entityManager, String authToken) {
		IdosRegisteredVendor user = null;
		if (authToken == null) {
			return user;
		}
		try {
			Query query = entityManager.createQuery(
					"select obj from IdosRegisteredVendor obj WHERE obj.authToken=?1 and obj.presentStatus=1");
			query.setParameter(1, authToken);
			user = (IdosRegisteredVendor) query.getSingleResult();
		} catch (NoResultException ex) {
		}
		return user;
	}

	public static IdosRegisteredVendor findByEmailAddressAndPassword(EntityManager entityManager, String emailAddress,
			String password) throws Exception {
		IdosRegisteredVendor user = null;
		if (emailAddress == null || password == null) {
			return user;
		}
		try {
			password = PasswordUtil.encrypt(password);
			Query query = entityManager.createQuery(
					"select obj from IdosRegisteredVendor obj WHERE obj.vendorEmail=?1  AND obj.vendorAccountPassword =?2 and obj.presentStatus=1");
			query.setParameter(1, emailAddress);
			query.setParameter(2, password);
			user = (IdosRegisteredVendor) query.getSingleResult();
		} catch (NoResultException ex) {

		}
		return user;
	}

	public static IdosRegisteredVendor findByEmailAddress(EntityManager entityManager, String emailAddress)
			throws Exception {
		IdosRegisteredVendor user = null;
		if (emailAddress == null) {
			return user;
		}
		try {
			Query query = entityManager.createQuery(
					"select obj from IdosRegisteredVendor obj WHERE obj.vendorEmail=?1 and obj.presentStatus=1");
			query.setParameter(1, emailAddress);
			user = (IdosRegisteredVendor) query.getSingleResult();
		} catch (NoResultException ex) {

		}
		return user;
	}

}
