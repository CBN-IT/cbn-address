package ro.cbn.it.adrese.test;

import ro.cbn.it.adrese.utils.TextPatternUtils;

/**
 * Experiments regarding unicode accents / diacritics and regex.
 */
public class DiacriticsExperiment {
	
	public static void main(String[] args) {
		String t1 = "șăuț șÂ ȘĂUȚ îÎiaăâșȘțȚ";
		String t2Cedilla = "şsţt";
		System.out.println(t1 + " -> " + TextPatternUtils.removeAccents(t1) );
		System.out.println(t2Cedilla + " -> " + TextPatternUtils.removeAccents(t2Cedilla) );
	}
	
}
