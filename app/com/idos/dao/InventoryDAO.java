package com.idos.dao;

import com.idos.util.IDOSException;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.Date;

/**
 * @auther Sunil K. Namdev created on 16.02.2018
 */
public interface InventoryDAO extends BaseDAO {

	StringBuilder sbr = new StringBuilder(
			"select specifics, branch, sum(ou), sum(ob), sum(pou), sum(pob), sum(sou), sum(sob), sum(t.ou+t.pou-t.sou) as cu, sum(t.ob+t.pob-t.sob) as cb, sum(sm) from(")
			.append("select TRANSACTION_SPECIFICS as specifics, BRANCH_ID as branch, date as idate, SUM(NUM_EXPUNITS_CONVERTED_INCUNITS) as ou, SUM(GROSS_VALUE) as ob, 0 as pou, 0 as pob, 0 as sou, 0 as sob, 0 as sm from TRADING_INVENTORY where BRANCH_ORGNIZATION_ID=?1 and TRANSACTION_SPECIFICS=?2 and PRESENT_STATUS = 1 and (TRANSACTION_TYPE='3') group by BRANCH_ID,TRANSACTION_SPECIFICS,date  ")
			.append(" union all ")
			.append("select TRANSACTION_SPECIFICS as specifics, BRANCH_ID as branch, date as idate, SUM(NUM_EXPUNITS_CONVERTED_INCUNITS) as ou, SUM(GROSS_VALUE) as ob, 0 as pou, 0 as pob, 0 as sou, 0 as sob, 0 as sm from TRADING_INVENTORY where BRANCH_ORGNIZATION_ID=?3 and TRANSACTION_SPECIFICS=?4 and PRESENT_STATUS = 1 and TRANSACTION_TYPE in (1, 5, 7) and  date  <= ?5 group by BRANCH_ID, TRANSACTION_SPECIFICS, date ")
			.append(" union all ")
			.append("select BUY_SPECIFICS as specifics, BRANCH_ID as branch, date as idate, (SUM(TOTAL_QUANTITY) *-1) as ou, (SUM(GROSS_VALUE) * -1) as ob, 0 as pou, 0 as pob, 0 as sou, 0 as sob, 0 as sm from TRADING_INVENTORY where  BRANCH_ORGNIZATION_ID=?6 and BUY_SPECIFICS =?7 and PRESENT_STATUS = 1  and TRANSACTION_TYPE in (2, 6, 8) and  date  <= ?8 group by BRANCH_ID, BUY_SPECIFICS, date")
			.append(" union all ")
			.append("select TRANSACTION_SPECIFICS  as specifics, BRANCH_ID as branch,  date as idate, 0 as ou, 0 as ob, SUM(NUM_EXPUNITS_CONVERTED_INCUNITS) as pou, SUM(GROSS_VALUE) as pob, 0 as sou, 0 as sob, 0 as sm from TRADING_INVENTORY where BRANCH_ORGNIZATION_ID=?9 and PRESENT_STATUS = 1  and TRANSACTION_SPECIFICS=?10 and TRANSACTION_TYPE in (1,5,7) and date  between ?11 and ?12 group by BRANCH_ID, TRANSACTION_SPECIFICS, date ")
			.append(" union all ")
			.append("select BUY_SPECIFICS as specifics, BRANCH_ID as branch,  date as idate , 0 as ou, 0 as ob, 0 as pou, 0 as pob, SUM(TOTAL_QUANTITY) sou, SUM(GROSS_VALUE) sob, SUM(TRANSACTION_GROSS) sm from TRADING_INVENTORY where BRANCH_ORGNIZATION_ID=?13 and PRESENT_STATUS = 1 and BUY_SPECIFICS=?14 and TRANSACTION_TYPE in (2,6,8) and date between ?15 and ?16 group by BRANCH_ID, BUY_SPECIFICS, date")
			.append(") as t group by branch, specifics");
	String BRANCH_ITEM_SQL = sbr.toString();

	StringBuilder sbr1 = new StringBuilder(
			"select itemid, sum(ou), sum(ob)/sum(ou), sum(ob), sum(pou), prate, sum(pob), sum(sou), srate, sum(sob), sum(t.ou+t.pou-t.sou) as cu,(sum(t.ob+t.pob-t.sob)/sum(t.ou+t.pou-t.sou)) as crate, sum(t.ob+t.pob-t.sob) as cb, sum(sm) from( ")
			.append(" select TRANSACTION_SPECIFICS as itemid,  SUM(NUM_EXPUNITS_CONVERTED_INCUNITS) as ou, CALCULATED_RATE as orate, SUM(GROSS_VALUE) as ob, 0 as pou, 0 as prate, 0 as pob, 0 as sou, 0 as srate, 0 as sob, 0 as sm")
			.append(" from TRADING_INVENTORY where BRANCH_ORGNIZATION_ID=?1 and BRANCH_ID=?2 and TRANSACTION_SPECIFICS=?3 and PRESENT_STATUS = 1  and (TRANSACTION_TYPE=3) group by TRANSACTION_SPECIFICS ")
			.append(" union all ")
			.append(" select TRANSACTION_SPECIFICS as itemid,   SUM(NUM_EXPUNITS_CONVERTED_INCUNITS) as ou, CALCULATED_RATE as orate, SUM(GROSS_VALUE) as ob, 0 as pou, 0 as prate, 0 as pob, 0 as sou, 0 as srate, 0 as sob, 0 as sm ")
			.append(" from TRADING_INVENTORY where  BRANCH_ORGNIZATION_ID=?1 and BRANCH_ID=?2 and TRANSACTION_SPECIFICS=?3 and PRESENT_STATUS = 1 and TRANSACTION_TYPE in (1, 5, 7) and  DATE  <= ?4 group by TRANSACTION_SPECIFICS ")
			.append(" union all ")
			.append(" select BUY_SPECIFICS as itemid, (SUM(TOTAL_QUANTITY) *-1) as ou, CALCULATED_RATE as orate, (SUM(GROSS_VALUE) * -1) as ob, 0 as pou, 0 as prate, 0 as pob, 0 as sou, 0 as srate, 0 as sob, 0 as sm ")
			.append(" from TRADING_INVENTORY where BRANCH_ORGNIZATION_ID=?1 and BRANCH_ID=?2 and BUY_SPECIFICS=?3 and PRESENT_STATUS = 1 and TRANSACTION_TYPE in (2, 6, 8) and  DATE  <= ?4 group by BUY_SPECIFICS ) as t group by t.itemid");
	String BRANCH_TXN_OPEN_SQL = sbr1.toString();

	StringBuilder sbr2 = new StringBuilder(
			"select txnid, idate, sum(ou), orate, sum(ob), sum(pou), prate, sum(pob), sum(sou), srate, sum(sob), sum(t.ou+t.pou-t.sou) as cu, (sum(t.ob+t.pob-t.sob)/sum(t.ou+t.pou-t.sou)) as crate, sum(t.ob+t.pob-t.sob) as cb, sum(sm), txnType, ID, PRICE_CHANGED_TXN from( ")
			.append(" select TRANSACTION_ID as txnid,  date as idate, 0 as ou, 0 as orate, 0 as ob, SUM(TOTAL_QUANTITY) as pou, CALCULATED_RATE as prate, SUM(GROSS_VALUE) as pob, 0 as sou, CALCULATED_RATE as srate, 0 as sob, null as sm, TRANSACTION_TYPE as txnType, ID, PRICE_CHANGED_TXN ")
			.append(" from TRADING_INVENTORY where TRANSACTION_TYPE in (1,5,7,9) and BRANCH_ORGNIZATION_ID=?1 and BRANCH_ID=?2 and PRESENT_STATUS = 1  and (TRANSACTION_SPECIFICS=?3 or BUY_SPECIFICS=?4) and ")
			.append(" (DATE >=?5 and DATE <= ?6) group by TRANSACTION_ID ")
			.append(" union all  ")
			.append(" select TRANSACTION_ID as txnid,  date as idate, 0 as ou, 0 as orate, 0 as ob, 0 as pou, 0 as prate, 0 as pob, SUM(TOTAL_QUANTITY) sou, CALCULATED_RATE as srate, SUM(GROSS_VALUE) sob, SUM(TRANSACTION_GROSS) sm, TRANSACTION_TYPE as txnType, ID, PRICE_CHANGED_TXN ")
			.append(" from TRADING_INVENTORY where BRANCH_ORGNIZATION_ID=?7 and BRANCH_ID=?8 and PRESENT_STATUS = 1  and (TRANSACTION_SPECIFICS=?9 or BUY_SPECIFICS=?10) and TRANSACTION_TYPE in (2,6,8,10) and DATE ")
			.append(" between ?11 and ?12 group by TRANSACTION_ID) as t ")
			.append(" group by t.txnid order by t.idate,t.ID");

	String BRANCH_TXN_SQL = sbr2.toString();

	StringBuilder sbr3 = new StringBuilder("select sum(ou), rate, sum(GROSS_VALUE) from ( ")
			.append(" select sum(TOTAL_QUANTITY) as ou, CALCULATED_RATE as rate, TRANSACTION_SPECIFICS as item, GROSS_VALUE ")
			.append(" from TRADING_INVENTORY where BRANCH_ORGNIZATION_ID=?1 and BRANCH_ID=?2 and PRESENT_STATUS = 1 and TRANSACTION_SPECIFICS=?3 ")
			.append(" and TRANSACTION_TYPE=3 group by CALCULATED_RATE,TRANSACTION_SPECIFICS,GROSS_VALUE")
			.append(" union all ")
			.append(" select sum(TOTAL_QUANTITY) as ou, CALCULATED_RATE as rate, TRANSACTION_SPECIFICS as item, sum(GROSS_VALUE) ")
			.append(" from TRADING_INVENTORY where BRANCH_ORGNIZATION_ID=?4 and BRANCH_ID=?5 and PRESENT_STATUS = 1 and TRANSACTION_SPECIFICS=?6 ")
			.append(" and TRANSACTION_TYPE in (1,5,7,9) and date <= ?7 group by TRANSACTION_SPECIFICS,CALCULATED_RATE ")
			// .append(" union all ")
			// .append(" select sum(TOTAL_QUANTITY *-1) as ou, CALCULATED_RATE as rate,
			// BUY_SPECIFICS as item, sum(GROSS_VALUE * -1) from TRADING_INVENTORY where
			// BRANCH_ORGNIZATION_ID=?x and BRANCH_ID=?x and BUY_SPECIFICS=?x and
			// TRANSACTION_TYPE=6 and date <= ?x group by BUY_SPECIFICS")
			.append(" union all ")
			.append(" select sum(TOTAL_QUANTITY) as ou, CALCULATED_RATE as rate, BUY_SPECIFICS as item, sum(GROSS_VALUE) ")
			.append(" from TRADING_INVENTORY where BRANCH_ORGNIZATION_ID=?8 and BRANCH_ID=?9 and PRESENT_STATUS = 1 and BUY_SPECIFICS=?10 ")
			.append(" and TRANSACTION_TYPE in (2,6,8,10) and  date  <= ?11 group by BUY_SPECIFICS,CALCULATED_RATE ")
			// .append(" union all ")
			// .append(" select sum(TOTAL_QUANTITY * -1) as ou, CALCULATED_RATE as rate,
			// BUY_SPECIFICS as item, sum(GROSS_VALUE * -1) ")
			// .append(" from TRADING_INVENTORY where BRANCH_ORGNIZATION_ID=?x and
			// BRANCH_ID=?x and TRANSACTION_SPECIFICS=?x and TRANSACTION_TYPE=5 and date <=
			// ?x group by TRANSACTION_SPECIFICS")
			.append(") t group by rate");

	String FIFO_OB_TXN_SQL = sbr3.toString();

	String FIFO_TXN_JPQL = "select obj from TradingInventory obj where obj.organization.id=?1 and  obj.branch.id=?2 and obj.presentStatus=1 and obj.transactionType in (1,2,5,6,7,8,9,10) and(obj.transactionSpecifics.id=?3 or obj.buySpecifics.id=?4) and obj.date  between ?5 and ?6 order by obj.date";

	// StringBuffer sbr1 = new StringBuffer("select obj from TradingInventory obj
	// where obj.transactionType in (1,2,5,6,7,8,9,10) and
	// (obj.transactionSpecifics.id='" + buySpecific.getId() + "' or
	// obj.buySpecifics.id='" + buySpecific.getId() + "') and obj.organization='" +
	// user.getOrganization().getId() + "' and obj.branch='" + branch.getId() + "'
	// and obj.date > '" + transactionDate + "' group by obj.transactionId order by
	// date asc,obj.createdAt asc");
	StringBuilder sbrBackDatedFifo = new StringBuilder(
			"select TRANSACTION_ID, date ,sum(TOTAL_QUANTITY), SUM(NUM_EXPUNITS_CONVERTED_INCUNITS), CALCULATED_RATE, SUM(GROSS_VALUE), TRANSACTION_TYPE, ID, TRANSACTION_SPECIFICS, TRANSACTION_GROSS ")
			.append(" from TRADING_INVENTORY where TRANSACTION_TYPE in (1,2,5,6,7,8,9,10) and BRANCH_ORGNIZATION_ID=?1 and BRANCH_ID=?2 and PRESENT_STATUS = 1 and (TRANSACTION_SPECIFICS=?3 or BUY_SPECIFICS=?4) and ")
			.append(" DATE > ?5 group by TRANSACTION_ID order by date asc,CREATED_AT asc");
	String BACKDATED_FIFO_TXN_SQL = sbrBackDatedFifo.toString();

	void getMidInventory(ObjectNode result, JsonNode json, Users user, EntityManager em) throws IDOSException;

	void getTxnLevelInventory(ObjectNode result, JsonNode json, Users user, EntityManager em) throws IDOSException;

	TradingInventory getClosingInventory(long orgid, long branchid, long specificid, Date date, EntityManager em);
}
