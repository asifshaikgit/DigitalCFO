package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="IDOS_SUBSCRIPTION_TAX")
public class IdosSubscriptionTax extends AbstractBaseModel {
	
	@Column(name="IDOS_SUBSCRIPTION_TAX_NAME")
	private String subscriptionTacName;
	
	@Column(name="IDOS_SUBSCRIPTION_TAX_TYPE")
	private Integer subscriptionTaxType;

	public String getSubscriptionTacName() {
		return subscriptionTacName;
	}

	public void setSubscriptionTacName(String subscriptionTacName) {
		this.subscriptionTacName = subscriptionTacName;
	}

	public Integer getSubscriptionTaxType() {
		return subscriptionTaxType;
	}

	public void setSubscriptionTaxType(Integer subscriptionTaxType) {
		this.subscriptionTaxType = subscriptionTaxType;
	}
	
}
