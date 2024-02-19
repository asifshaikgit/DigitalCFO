package pojo;

import model.Specifics;
import model.Transaction;
import model.TransactionItems;

import java.util.List;

/**
 * Created by Sunil K. Namdev on 20-10-2019.
 */
public class WithHoldingTaxPojo {
    Double taxRate;
    Double taxAmount;
    Transaction transaction;
    TransactionItems transactionItem;
    Specifics specific;
    Integer taxType;

    public Double getTaxRate() {
        return this.taxRate;
    }

    public void setTaxRate(final Double taxRate) {
        this.taxRate = taxRate;
    }

    public Double getTaxAmount() {
        return this.taxAmount;
    }

    public void setTaxAmount(final Double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public Transaction getTransaction() {
        return this.transaction;
    }

    public void setTransaction(final Transaction transaction) {
        this.transaction = transaction;
    }

    public TransactionItems getTransactionItem() {
        return this.transactionItem;
    }

    public void setTransactionItem(final TransactionItems transactionItem) {
        this.transactionItem = transactionItem;
    }

    public Specifics getSpecific() {
        return this.specific;
    }

    public void setSpecific(final Specifics specific) {
        this.specific = specific;
    }

    public Integer getTaxType() {
        return this.taxType;
    }

    public void setTaxType(final Integer taxType) {
        this.taxType = taxType;
    }
}
