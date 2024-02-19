package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table(name="IDOS_SUBSCRIPTION_PACKAGE_TYPE")
public class IdosSubscriptionPackageType extends AbstractBaseModel {
	
	@Column(name="SUBSCRIPTION_NAME")
	private String subscriptionName;
	
	@Column(name="CURRENCY")
	private String currency;
	
	@Column(name="SUBSCRIPTION_AMOUNT")
	private Double subscriptionAmount;

	public String getSubscriptionName() {
		return subscriptionName;
	}

	public void setSubscriptionName(String subscriptionName) {
		this.subscriptionName = subscriptionName;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Double getSubscriptionAmount() {
		return subscriptionAmount;
	}

	public void setSubscriptionAmount(Double subscriptionAmount) {
		this.subscriptionAmount = subscriptionAmount;
	}
}
