package com.idos.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public class LanguageUtil {

	private static Set<String> languages = Collections.emptySet();
	private Set<String> googleLanguages = Collections.emptySet();
	private LanguageUtil() {}
	private static LanguageUtil languageUtil = null;
	public static LanguageUtil getLanguageUtil() {
		if (null == languageUtil) {
			languageUtil = new LanguageUtil();
		}
		return languageUtil;
	}
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return getLanguageUtil();
	}

	public static Set<String> getLanguages() {
		if (languages.isEmpty()) {
			languages = new TreeSet<String>();
			Locale[] locales = Locale.getAvailableLocales();
			for (Locale locale : locales) {
				languages.add(locale.getDisplayLanguage());
	        }
			getLanguageUtil().addGoogleLanguages();
		}

		return languages;
	}

	private void addGoogleLanguages() {
		if (googleLanguages.isEmpty()) {
			googleLanguages = new HashSet<String>();
			googleLanguages.add("Afrikaans");
			googleLanguages.add("Armenian");
			googleLanguages.add("Azerbaijani");
			googleLanguages.add("Basque");
			googleLanguages.add("Bengali");
			googleLanguages.add("Bosnian");
			googleLanguages.add("Cebuano");
			googleLanguages.add("Esperanto");
			googleLanguages.add("Filipino");
			googleLanguages.add("Galician");
			googleLanguages.add("Georgian");
			googleLanguages.add("Gujarti");
			googleLanguages.add("Haitian Creole");
			googleLanguages.add("Hausa");
			googleLanguages.add("Hmong");
			googleLanguages.add("Igbo");
			googleLanguages.add("Javanese");
			googleLanguages.add("Kannada");
			googleLanguages.add("Khmer");
			googleLanguages.add("Lao");
			googleLanguages.add("Latin");
			googleLanguages.add("Maori");
			googleLanguages.add("Marathi");
			googleLanguages.add("Mongolian");
			googleLanguages.add("Nepali");
			googleLanguages.add("Persian");
			googleLanguages.add("Punjabi");
			googleLanguages.add("Somali");
			googleLanguages.add("Swahili");
			googleLanguages.add("Tamil");
			googleLanguages.add("Telugu");
			googleLanguages.add("Urdu");
			googleLanguages.add("Welsh");
			googleLanguages.add("Yiddish");
			googleLanguages.add("Yoruba");
			googleLanguages.add("Zulu");
		}
		if (!languages.isEmpty()) {
			languages.addAll(googleLanguages);
		}
	}

}
