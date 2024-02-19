package model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.idos.util.IdosUtil;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "BUSINESS_ACTION")
public class BusinessAction extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public BusinessAction() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "ACTION_NAME")
	private String actionName;

	@Column(name = "REQUIRE_APPROVAL")
	private Integer requireApproval;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APPROVAL_BY")
	private Role role;

	@Column(name = "IS_BUSINESS_ACTION_FUNCTION")
	private Integer isBusinessActionFunction;

	public Integer getIsBusinessActionFunction() {
		return isBusinessActionFunction;
	}

	public void setIsBusinessActionFunction(Integer isBusinessActionFunction) {
		this.isBusinessActionFunction = isBusinessActionFunction;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = IdosUtil.escapeHtml(actionName);
	}

	public Integer getRequireApproval() {
		return requireApproval;
	}

	public void setRequireApproval(Integer requireApproval) {
		this.requireApproval = requireApproval;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	/**
	 * list of BusinessAction.
	 */
	public List<BusinessAction> list(EntityManager entityManager) {
		String returnMsg = "";
		StringBuilder sbquery = new StringBuilder("");
		sbquery.append(
				"select obj from BusinessAction obj where obj.isBusinessActionFunction=1 and obj.presentStatus=1");
		List<BusinessAction> businessActions = entityManager.createQuery(sbquery.toString()).getResultList();
		return businessActions;
	}
}
