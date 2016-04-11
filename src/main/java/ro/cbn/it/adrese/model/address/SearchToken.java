package ro.cbn.it.adrese.model.address;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Describes a query token.
 */
public class SearchToken {
	
	public final Pattern keyValPattern = Pattern.compile("^(#([a-z0-9_-]+)=(\\p{Alnum}+))(\\$?)",
		Pattern.UNICODE_CHARACTER_CLASS);
	
	/**
	 * The types that are used as indexes.
	 */
	public final List<String> indexedTypes = Arrays.asList("o", "a", "an");
	
	/**
	 * Token's type.
	 * Empty string for value-only tokens.
	 */
	public String type = "";
	
	/**
	 * Token's value.
	 */
	public String value;
	
	/**
	 * Whether the token must be matched fully or just as prefix.
	 */
	public boolean fullMatch = false;
	
	/**
	 * Constructs a token from a token string.
	 * 
	 * Examples:
	 * - "somename": a default (value only) query token.
	 * - "#a=123": a key-value match token.
	 * 
	 * @param token The token string to build from.
	 */
	public SearchToken(String token) {
		Matcher m = keyValPattern.matcher(token);
		if (m.find()) {
			type = m.group(2);
			value = AddressComponent.normalizeForIndexing(m.group(3));
			fullMatch = !m.group(4).isEmpty();
			
		} else {
			type = "";
			value = AddressComponent.normalizeForIndexing(token);
		}
	}
	
	/**
	 * Returns whether the token is indexed.
	 * @return True if useable for index retrieval.
	 */
	public boolean isIndexed() {
		return type.isEmpty() || indexedTypes.contains(type);
	}
	
	/**
	 * Returns the value to query the database index for.
	 * 
	 * @return The indexed value.
	 */
	public String getIndexedValue() {
		switch (type) {
			case "an": // special case: ancestor name (indexed value is the name)
				return value;
			default: // else: return the string representation of the token
				return toString(false);
		}
	}
	
	/**
	 * Returns the string representation of the token.
	 * 
	 * @return The string token.
	 */
	@Override
	public String toString() {
		return toString(false);
	}
	
	/**
	 * Returns the string representation of the token.
	 * 
	 * @param justPrefix Whether to not include the "$" (if fullMatch is true).
	 * @return The string representation of the token.
	 */
	public String toString(boolean justPrefix) {
		if (type.isEmpty()) {
			return value;
		} else {
			return "#" + type + "=" + value + (!justPrefix && fullMatch ? "$" : "");
		}
	}
	
}
