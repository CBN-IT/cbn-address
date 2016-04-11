package ro.cbn.it.adrese.utils;

import java.text.Normalizer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains utility methods for working with text and string patterns, tokenization, accents normalization etc.
 */
public class TextPatternUtils {
	
	public final static Pattern tokenizerPattern = Pattern.compile("(#[a-z0-a_-]+=\\p{Alnum}*|\\p{Alnum}+)", Pattern.UNICODE_CHARACTER_CLASS);
	
	public final static Pattern[] altSirutaTokenizerPatterns = {
		Pattern.compile("i") /* replaced with 'a' */
	};
	public final static String[] altSirutaTokenizerReplacements = {
		"a"
	};
	
	/**
	 * Removes the accent marks (e.g. diacritics) from an UTF-8 string.
	 * 
	 * Example: "Țâță de mâță" => "Tata de mata"
	 * 
	 * @param text The input text.
	 * @return The normalized text.
	 */
	public static String removeAccents(String text) {
		return text == null ? null :
			Normalizer.normalize(text, Normalizer.Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}
	
	/**
	 * Normalizes SIRUTA names for indexing  / searching.
	 */
	public static String normalizeSirutaText(String text) {
		for (int i=0; i<altSirutaTokenizerPatterns.length; i++) {
			Matcher m = altSirutaTokenizerPatterns[i].matcher(text);
			text = m.replaceAll(altSirutaTokenizerReplacements[i]);
		}
		return text;
	}
	
	/**
	 * Tokenizes a string, extracting the words of the given text.
	 * 
	 * @param text The text to tokenize.
	 * @return The set of the found tokens (separate words).
	 */
	public static Collection<String> tokenizeWords(String text) {
		Set<String> tokens = new HashSet<>();
		
		Matcher m = tokenizerPattern.matcher(text);
		while (m.find()) {
			tokens.add(m.group(1));
		}
		
		return tokens;
	}
	
	/**
	 * Tokenizes a collection of strings, extracting the words of the given text.
	 *
	 * @param texts The strings to tokenize.
	 * @return The set of the found tokens (separate words).
	 */
	public static Collection<String> tokenizeWords(Collection<String> texts) {
		Set<String> tokens = new HashSet<>();
		for (String s: texts) {
			tokens.addAll(tokenizeWords(s));
		}
		return tokens;
	}
	
}
