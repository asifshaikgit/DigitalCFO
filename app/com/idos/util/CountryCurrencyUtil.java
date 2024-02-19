package com.idos.util;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;

import java.util.logging.Logger;
import java.util.logging.Level;

import model.IDOSCountry;

public class CountryCurrencyUtil {
	private static EntityManager entityManager;

	protected static Logger log = Logger.getLogger("controllers");

	public CountryCurrencyUtil() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	public static Map<String, String> getAvailableCurrencies() {
		Map<String, String> currencies = new TreeMap<String, String>();
		List<IDOSCountry> findAll = IDOSCountry.findAll();
		for (IDOSCountry idosCountry : findAll) {
			currencies.put(idosCountry.getName(),
					"(" + idosCountry.getCurrencyCode() + ")(" + idosCountry.getCurrencySymbol() + ")");
		}
		return currencies;
	}

	public static Map<String, String> getOLD() {
		Map<String, String> country1 = new TreeMap<String, String>();
		int count = 0;
		Map<String, String> currencies = getAvailableCurrenciesOLD();
		for (String country : currencies.keySet()) {
			count++;
			country1.put("" + count, country);
		}
		return country1;
	}

	public static Map<String, String> getAvailableCurrenciesOLD() {
		Locale[] locales = Locale.getAvailableLocales();
		Map<String, String> currencies = new TreeMap<String, String>();
		for (Locale locale : locales) {
			try {
				Currency currency = Currency.getInstance(locale);
				currencies.put(locale.getDisplayCountry(),
						"(" + currency.getCurrencyCode() + ")" + "(" + currency.getSymbol(locale) + ")");
			} catch (Exception e) {
			}
		}
		return currencies;
	}

	public static Map<String, String> getAvailableCurrenciesList() {
		Map<String, String> currencies = new TreeMap<String, String>();
		List<IDOSCountry> findAll = IDOSCountry.findAll();
		for (IDOSCountry idosCountry : findAll) {
			currencies.put(idosCountry.getId().toString(), idosCountry.getName() + " ==> ("
					+ idosCountry.getCurrencyCode() + ")(" + idosCountry.getCurrencySymbol() + ")");
		}
		return currencies;
	}

	public static List<String> getAvailableCurrenciesListOLD() {
		Locale[] locales = Locale.getAvailableLocales();
		List<String> currencies = new ArrayList<String>();
		for (Locale locale : locales) {
			try {
				Currency currency = Currency.getInstance(locale);
				currencies.add("(" + currency.getCurrencyCode() + ")" + "(" + currency.getSymbol(locale) + ")");
			} catch (Exception e) {
			}
		}
		return currencies;
	}

	public static Map<String, String> getCountries() {
		Map<String, String> countries = new TreeMap<String, String>();
		// System.out.println("__________null" + entityManager);
		List<IDOSCountry> findAll = IDOSCountry.findAll();
		for (IDOSCountry idosCountry : findAll) {
			countries.put(idosCountry.getId().toString(), idosCountry.getName());
		}
		return countries;
	}

	public static Map<String, String> getCountriesOLD() {
		Map<String, String> countries = new TreeMap<String, String>();
		Set<String> countrySet = new TreeSet<String>();
		int i = 1;
		Locale[] locales = Locale.getAvailableLocales();
		for (Locale locale : locales) {
			try {
				if (!locale.getDisplayCountry().equals("") && locale.getDisplayCountry() != null) {
					// countrySet.add(locale.getDisplayCountry());
					countrySet.add(locale.getCountry());
				}
			} catch (Exception e) {
			}
		}
		for (String country : countrySet) {
			countries.put(String.valueOf(i), country);
			i++;
		}
		return countries;
	}

	public static List<String> getCountriesList() {
		List<String> countries = new ArrayList<String>();
		Locale[] locales = Locale.getAvailableLocales();
		for (Locale locale : locales) {
			try {
				if (!locale.getDisplayCountry().equals("") && locale.getDisplayCountry() != null) {
					countries.add(locale.getDisplayCountry());
				}
			} catch (Exception e) {
			}
		}
		return countries;
	}

	public static String getCountryName(final String id) {
		String res = null;
		if (null != id && !"".equals(id)) {
			Map<String, String> countries = getCountries();
			res = countries.get(id);
		}
		return res;
	}

	public static String getCountryId(final String name) {
		String res = null;
		if (null != name && !"".equals(name)) {
			Map<String, String> countries = getCountries();
			for (String key : countries.keySet()) {
				if (countries.get(key).equals(name)) {
					return key;
				}
			}
		}
		return res;
	}

}
