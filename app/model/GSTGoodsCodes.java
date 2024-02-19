package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
@Table(name="GST_GOODS_CODES")
public class GSTGoodsCodes extends AbstractBaseModel{
	@Column(name="GOODS_DESCRIPTION")
	private String goodsDescription;
	
	@Column(name="GST_COUNTRY_CODE")
	private String GSTCountryCode;
	
	@Column(name="TAX_RATE")
	private Double taxRate;
	
	@Column(name="GROUP_ID")
	private Integer groupId;
	
	@Column(name="GOODS_CODE")
	private String goodsCode;

	public String getGoodsDescription() {
		return goodsDescription;
	}

	public void setGoodsDescription(String goodsDescription) {
		this.goodsDescription = goodsDescription;
	}

	public String getGSTCountryCode() {
		return GSTCountryCode;
	}

	public void setGSTCountryCode(String gSTCountryCode) {
		GSTCountryCode = gSTCountryCode;
	}

	public Double getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(Double taxRate) {
		this.taxRate = taxRate;
	}

	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	public String getGoodsCode() {
		return goodsCode;
	}

	public void setGoodsCode(String goodsCode) {
		this.goodsCode = goodsCode;
	}
	   
	
}
