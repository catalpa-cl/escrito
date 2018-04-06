package de.unidue.ltl.edu.scoring.features.essay.spelling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class SpellCheckingFilter {

	//chars that shouldn't pass the filter
	private List<String> charToIgnore = new ArrayList<String>(Arrays.asList(
			".", "?", "&", ",", "-", "<", ">", "!", "„", "“", "(", ")", ";",
			"'", "/", "\"", "]", "[", "§", "$", "%", "`", "´", ":", "ZZ", "quot"));

	public boolean passFilter(String tokenText) {
		// ignore special chars
		if (charToIgnore.contains(tokenText)) {
			return false;
		}

		// ignore numbers
		if (StringUtils.isNumeric(tokenText)
				|| isCharSeparatedNumber(tokenText, ".")
				|| isCharSeparatedNumber(tokenText, ",")
				|| isCharSeparatedNumber(tokenText, "-")
				|| isCharSeparatedNumber(tokenText, "%")) {
			return false;
		}
		return true;
	}

	/**
	 * checks if the string splitted by dots contains only numbers
	 * 
	 * @param tokenText
	 * @return
	 */
	private boolean isCharSeparatedNumber(String tokenText, String character) {
		if (!tokenText.contains(character))
			return false;
		for (String substring : tokenText.split(character)) {
			if (!StringUtils.isNumeric(substring))
				return false;
		}
		return true;
	}
}
