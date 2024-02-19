package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="USER_has_RIGHTS_for_CHART_OF_ACCOUNTS")
public class UserRightSpecifics extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="specifics_id")
	private Specifics specifics;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="specifics_particulars_id")
	private Particulars particulars;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="user_right_id")
	private UserRights userRights;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="user_id")
	private Users user;
	
	@Column(name="amount")
	private Double amount;
	
	@Column(name="AMOUNT_TO")
	private Double amountTo;
	
	public Double getAmountTo() {
		return amountTo;
	}

	public void setAmountTo(Double amountTo) {
		this.amountTo = amountTo;
	}

	@Column(name="amount_criteria")
	private Integer amountCriteria;
	
	public Integer getAmountCriteria() {
		return amountCriteria;
	}

	public void setAmountCriteria(Integer amountCriteria) {
		this.amountCriteria = amountCriteria;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Specifics getSpecifics() {
		return specifics;
	}

	public void setSpecifics(Specifics specifics) {
		this.specifics = specifics;
	}

	public Particulars getParticulars() {
		return particulars;
	}

	public void setParticulars(Particulars particulars) {
		this.particulars = particulars;
	}

	public UserRights getUserRights() {
		return userRights;
	}

	public void setUserRights(UserRights userRights) {
		this.userRights = userRights;
	}

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}
	
	public Long getEntityComparableParamId(){
		return getSpecifics().getId();
	}	
}
