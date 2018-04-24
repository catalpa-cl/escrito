package de.unidue.ltl.escrito.core.normalization;

public class SpellingUtils {

	public static boolean doNotSpellcheck(String tokenText){
		
		
		// Do not correct punctuation marks, i.e. tokens without any letter or character
		if (tokenText.matches("[^a-zA-Z0-9]+")) {
			//		System.out.println("1 Do not correct token "+tokenText);
			return true;
		}
		// Do not correct tokens that consist only of numbers
		if (tokenText.matches("[\\d]+")) {
			//		System.out.println("2 Do not correct token "+tokenText);
			return true;
		}

		// some tokenization artifacts are not in our dictionaries, but should not be treated as errors
		// TODO: read that in from a separate file
		if (tokenText.equals("'re")
				|| tokenText.equals("'m")
				|| tokenText.equals("'s")
				|| tokenText.equals("'d")
				|| tokenText.equals("'ll")
				|| tokenText.equals("n't")
				|| tokenText.equals("'ve")
				|| tokenText.equals("wo")
				|| tokenText.equals("ca")){ 
			return true;
		}

		// we also want to ignore bullet point markers
		if (tokenText.equals("a.")
				|| tokenText.equals("b.")
				|| tokenText.equals("c.")
				|| tokenText.equals("d.")
				|| tokenText.equals("(a)")
				|| tokenText.equals("(b)")
				|| tokenText.equals("(c)")
				|| tokenText.equals("(d)")){ 
			return true;
		}
		
		
		// Do not correct tokens that consist only of numbers followed by at most 2 letters to acount for "20mm" etc
		if (tokenText.matches("[\\d]+[A-Za-z]{1,2}")) {
			//System.out.println("3 Do not correct token "+tokenText);
			return true;
		}

		// Do not correct tokens that consist only of numbers followed by at most 2 letters to acount for "20mm" etc
		if (tokenText.matches("[\\d]+\\.[\\d][A-Za-z]{0,2}")) {
			//System.out.println("4 Do not correct token "+tokenText);
			return true;
		}
		if (tokenText.matches("[\\d]+,[\\d][A-Za-z]{0,2}")) {
			//System.out.println("4 Do not correct token "+tokenText);
			return true;
		}
		return false;
		
	}
	
	
	
	
}
