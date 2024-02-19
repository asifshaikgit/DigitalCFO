package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.Date;

import javax.persistence.*;


@SuppressWarnings("serial")
@MappedSuperclass
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractBaseModel implements BaseModel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	public Long id;
	
	@Column(name="CREATED_AT")
	public Date createdAt;
	
	@ManyToOne
	@JoinColumn(name="CREATED_BY")
	@JsonIgnore
	public Users createdBy;
	
	@Column(name="MODIFIED_AT")
	public Date modifiedAt;
	
	@ManyToOne
	@JoinColumn(name="MODIFIED_BY")
	@JsonIgnore
	public Users modifiedBy;
	
	@Column(name="present_status")
	public Integer presentStatus=1;
	
	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Users getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Users createdBy) {
		this.createdBy = createdBy;
	}

	public Date getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(Date modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public Users getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(Users modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Integer getPresentStatus() {
		return presentStatus;
	}

	public void setPresentStatus(Integer presentStatus) {
		this.presentStatus = presentStatus;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractBaseModel other = (AbstractBaseModel) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
