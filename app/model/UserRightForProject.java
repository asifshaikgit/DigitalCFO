package model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="USER_has_RIGHTS_for_PROJECT")
public class UserRightForProject extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USER_RIGHT_ID")
	private UserRights userRights;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USER_ID")
	private Users user;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USER_PROJECT_ID")
	private Project project;

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

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
	
	public Long getEntityComparableParamId(){
		return getProject().getId();
	}	
}
