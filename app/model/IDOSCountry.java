package model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Query;
import javax.persistence.Table;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "COUNTRY")
public class IDOSCountry {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public IDOSCountry() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	public Long id;

	@Column(name = "CODE")
	private String code;

	@Column(name = "DIAL_CODE")
	private Integer dialCode;

	@Column(name = "NAME")
	private String name;

	@Column(name = "CURRENCY_NAME")
	private String currencyName;

	@Column(name = "CURRENCY_SYMBOL")
	private String currencySymbol;

	@Column(name = "CURRENCY_CODE")
	private String currencyCode;

	public Long getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getDialCode() {
		return dialCode;
	}

	public void setDialCode(Integer dialCode) {
		this.dialCode = dialCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCurrencyName() {
		return currencyName;
	}

	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}

	public String getCurrencySymbol() {
		return currencySymbol;
	}

	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public static IDOSCountry findById(Long id) {
		return entityManager.find(IDOSCountry.class, id);
	}

	public static List<IDOSCountry> findAll() {
		System.out.println("null" + entityManager);
		Query query = entityManager.createQuery("select obj from IDOSCountry obj order by obj.name asc");
		List<IDOSCountry> countryList = query.getResultList();
		return countryList;
	}

}
