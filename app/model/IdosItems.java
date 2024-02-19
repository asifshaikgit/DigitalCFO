package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="IDOS_ITEMS")
public class IdosItems extends AbstractBaseModel {

	@Column(name="ITEM_NAME")
	private String itemName;
	
	@Column(name="ITEM_DOMAIN_INDUSTRY")
	private String itemDomainIndustry;
	
	@Column(name="ITEM_CATEGORY")
	private String itemCategory;

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getItemDomainIndustry() {
		return itemDomainIndustry;
	}

	public void setItemDomainIndustry(String itemDomainIndustry) {
		this.itemDomainIndustry = itemDomainIndustry;
	}

	public String getItemCategory() {
		return itemCategory;
	}

	public void setItemCategory(String itemCategory) {
		this.itemCategory = itemCategory;
	}
}
