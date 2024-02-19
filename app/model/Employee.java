package model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "EMPLOYEE")
public class Employee extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public Employee() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "fname")
	private String fname;

	@Column(name = "lname")
	private String lname;

	@Column(name = "mname")
	private String mname;

	@Column(name = "address")
	private String address;

	@Column(name = "phone")
	private Integer phone;

	@Column(name = "email")
	private String email;

	@Column(name = "ssn")
	private String ssn;

	@Column(name = "salary")
	private Double salary;

	@Column(name = "hire_date")
	private Date hireDate;

	@Column(name = "social_costs")
	private Integer socialCosts;

	@Column(name = "years_experience")
	private Integer yearsExperience;

	@Column(name = "education")
	private String education;

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public String getLname() {
		return lname;
	}

	public void setLname(String lname) {
		this.lname = lname;
	}

	public String getMname() {
		return mname;
	}

	public void setMname(String mname) {
		this.mname = mname;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Integer getPhone() {
		return phone;
	}

	public void setPhone(Integer phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSsn() {
		return ssn;
	}

	public void setSsn(String ssn) {
		this.ssn = ssn;
	}

	public Double getSalary() {
		return salary;
	}

	public void setSalary(Double salary) {
		this.salary = salary;
	}

	public Date getHireDate() {
		return hireDate;
	}

	public void setHireDate(Date hireDate) {
		this.hireDate = hireDate;
	}

	public Integer getSocialCosts() {
		return socialCosts;
	}

	public void setSocialCosts(Integer socialCosts) {
		this.socialCosts = socialCosts;
	}

	public Integer getYearsExperience() {
		return yearsExperience;
	}

	public void setYearsExperience(Integer yearsExperience) {
		this.yearsExperience = yearsExperience;
	}

	public String getEducation() {
		return education;
	}

	public void setEducation(String education) {
		this.education = education;
	}

	/**
	 * Find a Employee by id.
	 */
	public static Employee findById(Long id) {
		return entityManager.find(Employee.class, id);
	}

	/**
	 * Update this employee.
	 */
	public void update(Long id) {
		this.id = id;
		entityManager.merge(this);
	}

	/**
	 * Insert this employee.
	 */
	public void save() {
		this.id = id;
		entityManager.persist(this);
	}

	/**
	 * Delete this employee.
	 */
	public void delete() {
		entityManager.remove(this);
	}

	/**
	 * Return a page of employee
	 *
	 * @param page     Page to display
	 * @param pageSize Number of employees per page
	 * @param sortBy   Employee property used for sorting
	 * @param order    Sort order (either or asc or desc)
	 * @param filter   Filter applied on the name column
	 */
	public static Page page(EntityManager entityManager, int page, int pageSize, String sortBy, String order,
			String filter) {
		if (page < 1)
			page = 1;
		Long total = (Long) entityManager
				.createQuery("select count(c) from Employee e where lower(e.fname) like ? and e.presentStatus=1")
				.setParameter(1, "%" + filter.toLowerCase() + "%")
				.getSingleResult();
		List<Employee> data = entityManager
				.createQuery("from Employee e where lower(e.fname) like ? order by c." + sortBy + " " + order)
				.setParameter(1, "%" + filter.toLowerCase() + "%")
				.setFirstResult((page - 1) * pageSize)
				.setMaxResults(pageSize)
				.getResultList();
		return new Page(entityManager, data, total, page, pageSize);
	}

	/**
	 * Used to represent a employees page.
	 */
	public static class Page {

		private final int pageSize;
		private final long totalRowCount;
		private final int pageIndex;
		private final List<Employee> list;

		public Page(EntityManager entityManager, List<Employee> data, long total, int page, int pageSize) {
			this.list = data;
			this.totalRowCount = total;
			this.pageIndex = page;
			this.pageSize = pageSize;
		}

		public long getTotalRowCount() {
			return totalRowCount;
		}

		public int getPageIndex() {
			return pageIndex;
		}

		public List<Employee> getList() {
			return list;
		}

		public boolean hasPrev() {
			return pageIndex > 1;
		}

		public boolean hasNext() {
			return (totalRowCount / pageSize) >= pageIndex;
		}

		public String getDisplayXtoYofZ() {
			int start = ((pageIndex - 1) * pageSize + 1);
			int end = start + Math.min(pageSize, list.size()) - 1;
			return start + " to " + end + " of " + totalRowCount;
		}

	}

}
