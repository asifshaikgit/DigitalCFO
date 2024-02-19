package model;

import play.db.jpa.JPAApi;

import java.util.Date;
import java.util.List;

import javax.persistence.*;
import service.EntityManagerProvider;
import javax.inject.Inject;

@Entity
@Table(name = "TRADING_INVENTORY")
public class TradingInventory extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public TradingInventory() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "TRANSACTION_ID")
	private Long transactionId;

	// 1= buy & 2= sell
	@Column(name = "TRANSACTION_TYPE")
	private Long transactionType;

	// this could be buy/sell specific
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_SPECIFICS")
	private Specifics transactionSpecifics;

	// For sell inventory item corresponding buy item, need this for inventory
	// report as many sell items mapped to one buy item
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BUY_SPECIFICS")
	private Specifics buySpecifics;

	@Column(name = "DATE")
	private Date date;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BRANCH_ID")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BRANCH_ORGNIZATION_ID")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID")
	private Users user;

	@Column(name = "TOTAL_QUANTITY")
	private Double totalQuantity = 0.0;

	@Column(name = "NUM_EXPUNITS_CONVERTED_INCUNITS")
	private Double noOfExpUnitsConvertedToIncUnits;

	@Column(name = "QUANTITY_MATCHED_WITH_SELL") // BASED ON FIFO/Weighted avg use Buy quantities to match with sell
													// quantities and calcualte gross for sell
	private Double quantityMatchedWithSell = 0.0;

	@Column(name = "CALCULATED_RATE")
	private Double calcualtedRate = 0.0;

	@Column(name = "GROSS_VALUE") // for buy it will come from transaction, for sell it will be calculated
	private Double grossValue = 0.0;

	@Column(name = "LINKED_BUY_IDS") // for sell, corresponding buy ids using which rate for this sell transaction is
										// calculated
	private String linkedBuyIds;

	@Column(name = "TRANSACTION_GROSS")
	private Double transactionGorss;

	@Column(name = "IS_BACKDATED_TRANSACTION") // 0=normal transaction 1=backdated transaction, so will have effect on
												// inventory
	private Integer isBackdatedTransaction;

	@Column(name = "INVENTORY_TYPE") // 1- FIFO , 2 - WAC
	private Integer inventoryType;

	@Column(name = "PRICE_CHANGED_TXN")
	private String priceChangedTxn;

	public Long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	public Long getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(Long transactionType) {
		this.transactionType = transactionType;
	}

	public Specifics getTransactionSpecifics() {
		return transactionSpecifics;
	}

	public void setTransactionSpecifics(Specifics transactionSpecifics) {
		this.transactionSpecifics = transactionSpecifics;
	}

	public Specifics getBuySpecifics() {
		return buySpecifics;
	}

	public void setBuySpecifics(Specifics buySpecifics) {
		this.buySpecifics = buySpecifics;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

	public Double getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(Double totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public Double getQuantityMatchedWithSell() {
		return quantityMatchedWithSell;
	}

	public void setQuantityMatchedWithSell(Double quantityMatchedWithSell) {
		this.quantityMatchedWithSell = quantityMatchedWithSell;
	}

	public Double getCalcualtedRate() {
		return calcualtedRate;
	}

	public void setCalcualtedRate(Double calcualtedRate) {
		this.calcualtedRate = calcualtedRate;
	}

	public Double getGrossValue() {
		return grossValue;
	}

	public void setGrossValue(Double grossValue) {
		this.grossValue = grossValue;
	}

	public String getLinkedBuyIds() {
		return linkedBuyIds;
	}

	public void setLinkedBuyIds(String linkedBuyIds) {
		this.linkedBuyIds = linkedBuyIds;
	}

	public Double getNoOfExpUnitsConvertedToIncUnits() {
		return noOfExpUnitsConvertedToIncUnits;
	}

	public void setNoOfExpUnitsConvertedToIncUnits(
			Double noOfExpUnitsConvertedToIncUnits) {
		this.noOfExpUnitsConvertedToIncUnits = noOfExpUnitsConvertedToIncUnits;
	}

	public Double getTransactionGorss() {
		return this.transactionGorss;
	}

	public void setTransactionGorss(Double transactionGorss) {
		this.transactionGorss = transactionGorss;
	}

	public Integer getIsBackdatedTransaction() {
		return isBackdatedTransaction;
	}

	public void setIsBackdatedTransaction(Integer isBackdatedTransaction) {
		this.isBackdatedTransaction = isBackdatedTransaction;
	}

	public Integer getInventoryType() {
		return this.inventoryType;
	}

	public void setInventoryType(Integer inventoryType) {
		this.inventoryType = inventoryType;
	}

	public String getPriceChangedTxn() {
		return this.priceChangedTxn;
	}

	public void setPriceChangedTxn(String priceChangedTxn) {
		this.priceChangedTxn = priceChangedTxn;
	}

	/**
	 * Find a TradingInventory by id.
	 */
	public static TradingInventory findById(Long id) {
		return entityManager.find(TradingInventory.class, id);
	}

	public static TradingInventory getTradingInventory(EntityManager entityManager, Long orgid, Long branchid,
			Long itemid, Long inventoryType) {
		TradingInventory ti = null;
		Query query = entityManager.createQuery(
				"select a from TradingInventory a where a.organization.id=?1 and a.branch.id=?2 and a.transactionSpecifics.id=?3 and a.transactionType=?4 and a.transactionId is null and a.presentStatus=1");
		query.setParameter(1, orgid);
		query.setParameter(2, branchid);
		query.setParameter(3, itemid);
		query.setParameter(4, inventoryType);
		List<TradingInventory> list = query.getResultList();
		if (list.size() > 0) {
			ti = list.get(0);
		}
		return ti;
	}

	public static List<TradingInventory> findTradingInventory(EntityManager entityManager, Long orgid, Long branchid,
			Long itemid, Long txnType, long txnId) {
		Query query = entityManager.createQuery(
				"select a from TradingInventory a where a.organization.id=?1 and a.branch.id=?2 and a.transactionSpecifics.id=?3 and a.transactionType=?4 and a.transactionId =?5 and a.presentStatus=1");
		query.setParameter(1, orgid);
		query.setParameter(2, branchid);
		query.setParameter(3, itemid);
		query.setParameter(4, txnType);
		query.setParameter(5, txnId);
		List<TradingInventory> list = query.getResultList();
		return list;
	}

	public static List<TradingInventory> findSellInventory(EntityManager entityManager, Long orgid, Long branchid,
			Long itemid, Long txnType, long txnId) {
		Query query = entityManager.createQuery(
				"select a from TradingInventory a where a.organization.id=?1 and a.branch.id=?2 and a.buySpecifics.id=?3 and a.transactionType=?4 and a.transactionId =?5 and a.presentStatus=1");
		query.setParameter(1, orgid);
		query.setParameter(2, branchid);
		query.setParameter(3, itemid);
		query.setParameter(4, txnType);
		query.setParameter(5, txnId);
		List<TradingInventory> list = query.getResultList();
		return list;
	}

	public static List<TradingInventory> getListAfterMarkedInventory(EntityManager entityManager, long orgid,
			long branchid, long itemid, long inventoryId) {
		Query query = entityManager.createQuery(
				"select a from TradingInventory a where a.organization.id=?1 and a.branch.id=?2 and(a.transactionSpecifics.id=?3 or a.buySpecifics.id=?4) and a.transactionType != 4 and a.id > ?5 and a.presentStatus=1 order by a.id");
		query.setParameter(1, orgid);
		query.setParameter(2, branchid);
		query.setParameter(3, itemid);
		query.setParameter(4, itemid);
		query.setParameter(5, inventoryId);
		List<TradingInventory> list = query.getResultList();
		return list;
	}
}
