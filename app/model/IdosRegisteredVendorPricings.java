package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "IDOS_REGISTERED_VENDOR_PRICINGS")
public class IdosRegisteredVendorPricings extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public IdosRegisteredVendorPricings() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "VENDOR_NAME")
	private String vendorName;

	@Column(name = "VENDOR_EMAIL")
	private String vendorEmail;

	public String getVendorEmail() {
		return vendorEmail;
	}

	public void setVendorEmail(String vendorEmail) {
		this.vendorEmail = vendorEmail;
	}

	public String getVendorRegistrationNumber() {
		return vendorRegistrationNumber;
	}

	public void setVendorRegistrationNumber(String vendorRegistrationNumber) {
		this.vendorRegistrationNumber = vendorRegistrationNumber;
	}

	public Double getVendorRetailerUnitPrice() {
		return vendorRetailerUnitPrice;
	}

	public void setVendorRetailerUnitPrice(Double vendorRetailerUnitPrice) {
		this.vendorRetailerUnitPrice = vendorRetailerUnitPrice;
	}

	public Double getVendorWholesaleUnitPrice() {
		return vendorWholesaleUnitPrice;
	}

	public void setVendorWholesaleUnitPrice(Double vendorWholesaleUnitPrice) {
		this.vendorWholesaleUnitPrice = vendorWholesaleUnitPrice;
	}

	public Double getVendorSpecialUnitPrice() {
		return vendorSpecialUnitPrice;
	}

	public void setVendorSpecialUnitPrice(Double vendorSpecialUnitPrice) {
		this.vendorSpecialUnitPrice = vendorSpecialUnitPrice;
	}

	public String getVendorSpecialPriceRequirements() {
		return vendorSpecialPriceRequirements;
	}

	public void setVendorSpecialPriceRequirements(
			String vendorSpecialPriceRequirements) {
		this.vendorSpecialPriceRequirements = vendorSpecialPriceRequirements;
	}

	@Column(name = "VENDOR_PHONE_NUMBER")
	private String vendorPhoneNumber;

	public String getVendorPhoneNumber() {
		return vendorPhoneNumber;
	}

	public void setVendorPhoneNumber(String vendorPhoneNumber) {
		this.vendorPhoneNumber = vendorPhoneNumber;
	}

	@Column(name = "VENDOR_REGISTRATION_NUMBER")
	private String vendorRegistrationNumber;

	@Column(name = "VENDOR_ITEMS")
	private String vendorItems;

	@Column(name = "VENDOR_ITEMS_DESCRIPTION")
	private String vendorItemsDescription;

	public String getVendorItemsDescription() {
		return vendorItemsDescription;
	}

	public void setVendorItemsDescription(String vendorItemsDescription) {
		this.vendorItemsDescription = vendorItemsDescription;
	}

	@Column(name = "VENDOR_LOCATIONS")
	private String vendorLocations;

	@Column(name = "VENDOR_RETAILER_UNIT_PRICE")
	private Double vendorRetailerUnitPrice;

	@Column(name = "VENDOR_WHOLESALE_UNIT_PRICE")
	private Double vendorWholesaleUnitPrice;

	@Column(name = "VENDOR_SPECIAL_UNIT_PRICE")
	private Double vendorSpecialUnitPrice;

	@Column(name = "VENDOR_SPECIAL_PRICE_REQUIREMENTS")
	private String vendorSpecialPriceRequirements;

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getVendorItems() {
		return vendorItems;
	}

	public void setVendorItems(String vendorItems) {
		this.vendorItems = vendorItems;
	}

	public String getVendorLocations() {
		return vendorLocations;
	}

	public void setVendorLocations(String vendorLocations) {
		this.vendorLocations = vendorLocations;
	}

	/**
	 * Find a IdosRegisteredVendorPricings by id.
	 */
	public static IdosRegisteredVendorPricings findById(Long id) {
		return entityManager.find(IdosRegisteredVendorPricings.class, id);
	}

}
