package model;

import play.db.jpa.JPAApi;
import javax.inject.Inject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Query;
import java.util.List;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "TRANSACTION_PURPOSE")
public class TransactionPurpose extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public TransactionPurpose() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "TRANSACTION_PURPOSE")
	private String transactionPurpose;

	@Column(name = "NEXT_QUESTION")
	private String nextQuestion;

	@Column(name = "QUESTION_TYPE")
	private Integer questionType;

	public Integer getQuestionType() {
		return questionType;
	}

	public void setQuestionType(Integer questionType) {
		this.questionType = questionType;
	}

	public String getTransactionPurpose() {
		return transactionPurpose;
	}

	public void setTransactionPurpose(String transactionPurpose) {
		this.transactionPurpose = transactionPurpose;
	}

	public String getNextQuestion() {
		return nextQuestion;
	}

	public void setNextQuestion(String nextQuestion) {
		this.nextQuestion = nextQuestion;
	}

	/**
	 * Find a TransactionPurpose by id.
	 */
	public static TransactionPurpose findById(Long id) {
		return entityManager.find(TransactionPurpose.class, id);
	}

	/**
	 * Find a TransactionPurpose by name.
	 */
	public static TransactionPurpose findByName(EntityManager entityManager, String name) {
		TransactionPurpose transactionPurpose = null;
		// StringBuilder sbquery = new StringBuilder();
		// sbquery.append("select obj from TransactionPurpose obj where
		// upper(obj.transactionPurpose) = ?x and obj.presentStatus=1");
		String sqlQuery = "select obj from TransactionPurpose obj where upper(obj.transactionPurpose) = ?1 and obj.presentStatus=1";
		Query query = entityManager.createQuery(sqlQuery);
		query.setParameter(1, name.toUpperCase());
		List<TransactionPurpose> transactionPurposeList = query.getResultList();
		if (!transactionPurposeList.isEmpty() && transactionPurposeList.size() > 0) {
			transactionPurpose = transactionPurposeList.get(0);
		}
		return transactionPurpose;
	}

	public static List<TransactionPurpose> getSingleUserTransactionPurpose(EntityManager entityManager) {
		StringBuilder sbquery = new StringBuilder(
				"select obj from TransactionPurpose obj where obj.presentStatus=1 and obj.questionType = 1 and id not in (9,10,20,21,27,28,34)");
		List<TransactionPurpose> transactionPurposeList = entityManager.createQuery(sbquery.toString()).getResultList();
		return transactionPurposeList;
	}

}
