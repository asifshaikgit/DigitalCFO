package com.idos.enumtype;

import java.io.Serializable;

public enum BankAccountEnumType implements Serializable {

	CASH_CREDIT(1),CHECKING(2),CURRENT(3),DEPOSIT(4),LINE_OF_CREDIT(5),LOAN(6),OVER_DRAFT(7),SAVINGS(8),CREDIT_CARD(9),PACKING_CREDIT(10),LETTER_OF_CREDIT(11),BANK_GUARANTEE(12);

	private int id;

	private BankAccountEnumType(int value) {
		this.id = value;
	}

	public int getValue() {
		return id;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		switch (this) {
		case CASH_CREDIT:
			return "CASH_CREDIT";
		case CHECKING:
			return "CHECKING";
		case CURRENT:
			return "CURRENT";
		case DEPOSIT:
			return "DEPOSIT";
		case LINE_OF_CREDIT:
			return "LINE_OF_CREDIT";
		case LOAN:
			return "LOAN";
		case OVER_DRAFT:
			return "OVER_DRAFT";
		case SAVINGS:
			return "SAVINGS";
		case CREDIT_CARD:
			return "CREDIT_CARD";
		case PACKING_CREDIT:
			return "PACKING_CREDIT";
		case LETTER_OF_CREDIT:
			return "LETTER_OF_CREDIT";
		case BANK_GUARANTEE:
			return "BANK_GUARANTEE";
		default:
			return "--";
		}
	}

	// the identifierMethod
	public int toInt() {
		return id;
	}

	// the valueOfMethod
	public static BankAccountEnumType fromInt(int value) {
		switch (value) {
		case 1:
			return CASH_CREDIT;
		case 2:
			return CHECKING;
		case 3:
			return CURRENT;
		case 4:
			return DEPOSIT;
		case 5:
			return LINE_OF_CREDIT;
		case 6:
			return LOAN;
		case 7:
			return OVER_DRAFT;
		case 8:
			return SAVINGS;
		case 9:
			return CREDIT_CARD;
		case 10:
			return PACKING_CREDIT;
		case 11:
			return LETTER_OF_CREDIT;
		case 12:
			return BANK_GUARANTEE;
		default:
			return CASH_CREDIT;
		}
	}

}
