package model;

import com.idos.util.IdosUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "BUSINESS_TYPE")
public class BusinessType extends AbstractBaseModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "BUSINESS_TYPE_NAME")
	private String businessName;

	@Column(name = "BUSINESS_TYPE_CODE")
	private String businessType;

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName =  IdosUtil.escapeHtml(businessName);
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType =  IdosUtil.escapeHtml(businessType);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BusinessType [businessName=");
		builder.append(businessName);
		builder.append(", businessType=");
		builder.append(businessType);
		builder.append("]");
		return builder.toString();
	}

}
