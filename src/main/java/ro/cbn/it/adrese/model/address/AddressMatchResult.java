package ro.cbn.it.adrese.model.address;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the result of an address matches in a database operation.
 */
public class AddressMatchResult {
	
	/**
	 * Stores the position and length of a string match.
	 */
	public static class MatchPosition {
		public int position;
		public int length;
	}
	
	/**
	 * The matched address.
	 */
	public AddressComponent address;
	
	/**
	 * A matrix that stores the positions of the matched terms.
	 */
	public List<MatchPosition> matchPositions;
	
	public AddressMatchResult(AddressComponent address) {
		this.address = address;
		matchPositions = new ArrayList<>();
	}
}
