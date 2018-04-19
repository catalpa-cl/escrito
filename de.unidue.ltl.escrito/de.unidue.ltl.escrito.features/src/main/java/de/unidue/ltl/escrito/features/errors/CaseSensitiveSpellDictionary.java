package de.unidue.ltl.escrito.features.errors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class CaseSensitiveSpellDictionary {

	private List<String> dictionary;

	public CaseSensitiveSpellDictionary(File dictFile) throws IOException {
		this.dictionary = getDict(dictFile);
	}

	private List<String> getDict(File dictFile) throws IOException {
		List<String> list = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dictFile)));
		String line = "";
		while((line=br.readLine())!=null){
			list.add(line);
		}
		br.close();
		return list;
	}

	/**
	 * Returns true if the word is correctly spelled against the current word
	 * list.
	 */
	public boolean isCorrect(String word, boolean caseSensitiveCheck) {
		if (!caseSensitiveCheck) {
			if (dictionary.contains(word))
				return true;
		} else {
			if (dictionary.contains(word)
					|| dictionary.contains(StringUtils.uncapitalize(word)))
				return true;
		}
		return false;
	}
}
