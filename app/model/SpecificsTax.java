package model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="SPECIFICS_has_TAX")
public class SpecificsTax extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="specifics_id")
	private Specifics specificsTax;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="specifics_particulars_id")
	private Particulars particulars;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="tax_id")
	private Tax taxSpecifics;

	public Specifics getSpecificsTax() {
		return specificsTax;
	}

	public void setSpecificsTax(Specifics specificsTax) {
		this.specificsTax = specificsTax;
	}

	public Particulars getParticulars() {
		return particulars;
	}

	public void setParticulars(Particulars particulars) {
		this.particulars = particulars;
	}

	public Tax getTaxSpecifics() {
		return taxSpecifics;
	}

	public void setTaxSpecifics(Tax taxSpecifics) {
		this.taxSpecifics = taxSpecifics;
	}

}