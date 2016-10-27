package ro.cbn.it.adrese.model.address;

import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharSequenceNodeFactory;
import ro.cbn.it.framework.utils.Log;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Implements a searchable address database.
 */
public class AddressDatabase implements Serializable {
	
	private final Pattern keyValPattern = Pattern.compile("^(#([a-z]+)=(\\p{Alnum}+))(\\$?)",
		Pattern.UNICODE_CHARACTER_CLASS);
	
	/**
	 * Stores the full list of addresses, indexed by their ID.
	 */
	protected Map<String, AddressComponent> addresses = new LinkedHashMap<>();
	
	/**
	 * Stores the prefix index used for searching the database.
	 */
	protected ConcurrentRadixTree<AddressComponent> index = new ConcurrentRadixTree<>(new DefaultCharSequenceNodeFactory());
	
	/**
	 * Constructs an empty database.
	 */
	public AddressDatabase() {
	}
	
	/**
	 * Adds a new address component to the database (both to the storage and to the index).
	 * 
	 * @param address The address to add.
	 */
	public void add(AddressComponent address) {
		addresses.put(address.id, address);
		
		// add it to the index
		Set<String> indexTags = address.getIndexedTags();
		for (String t: indexTags) {
			// the index doesn't accept multiple values for the same key (token), so we append its unique identifier
			// (the matching will be done in a prefix-based manner)
			index.put(t + "$" + address.id, address);
		}
	}
	
	/**
	 * Finds an address component by its unique ID.
	 * 
	 * @param id The ID.
	 * @return The address object or null if not found.
	 */
	public AddressComponent findById(String id) {
		return addresses.get(id);
	}
	
	/**
	 * Searches into the address database for the specified prefixes.
	 * 
	 * @param queryTokens The query tokens.
	 * @param limit The maximum number of results (to stop at, optional).
	 * @return A collection of matching addresses.
	 */
	public Collection<AddressComponent> search(Collection<String> queryTokens, int limit) {
		// sort the prefixes by their length, descending
		// we use the longest ones for retrieval from the radix tree to restrict the results
		List<SearchToken> tokens = normalizeQueryTokens(queryTokens);
		List<SearchToken> indexTokens = computeIndexTokens(tokens);
		
		// we use an identity set to exclude duplicate address instances from the results
		Set<AddressComponent> results = new LinkedHashSet<>();
		SearchToken indexTerm = (!indexTokens.isEmpty() ? indexTokens.get(0) : null );
		Iterator<AddressComponent> indexIterator;
		if (indexTerm == null) {
			Log.i("No indexes for query, doing a full scan...");
			indexIterator = this.addresses.values().iterator();
		} else {
			Log.i("Using index", indexTerm.getIndexedValue());
			indexIterator = this.index.getValuesForKeysStartingWith(indexTerm.getIndexedValue()).iterator();
		}
		
		int i = 0;
		while (indexIterator.hasNext()) {
			if (limit > -1 && i >= limit) {
				break;
			}
			AddressComponent address = indexIterator.next();
			boolean matches = matchesQuery(address, tokens);
			if (matches) {
				results.add(address);
				i++;
			}
		}
		
		return results;
	}
	
	/**
	 * Normalizes an input query into separate patterns / index terms to search for.
	 * 
	 * @param queryTokens The input query tokens to normalize.
	 * @return The query token objects.
	 */
	protected List<SearchToken> normalizeQueryTokens(Collection<String> queryTokens) {
		List<SearchToken> tokens = new ArrayList<>();
		if (queryTokens.isEmpty()) return tokens;
		
		for (String sToken: queryTokens) {
			SearchToken token = new SearchToken(sToken);
			if (!token.value.isEmpty())
				tokens.add(token);
		}
		
		return tokens;
	}
	
	/**
	 * Returns a list of index tokens from the requested search tokens.
	 * Sorts them by their length, descending.
	 * 
	 * @param tokens The tokens list to sort.
	 * @return The tokens to use for index retrieval.
	 */
	protected List<SearchToken> computeIndexTokens(List<SearchToken> tokens) {
		List<SearchToken> indexTokens = new ArrayList<>();
		for (SearchToken token: tokens) {
			if (token.isIndexed()) {
				indexTokens.add(token);
			}
		}
		
		Collections.sort(indexTokens, new Comparator<SearchToken>() {
			@Override
			public int compare(SearchToken o1, SearchToken o2) {
				return o2.value.length() - o1.value.length();
			}
		});
		
		return indexTokens;
	}
	
	/**
	 * Returns whether the given address matches all the tokens of a query.
	 * 
	 * @param address The address to check.
	 * @param queryTokens The query tokens to match.
	 * @return True if the match succeeds.
	 */
	protected boolean matchesQuery(AddressComponent address, List<SearchToken> queryTokens) {
		for (SearchToken query: queryTokens) {
			if (!matchesQuery(address, query)) {
				// Log.d("Address does not match", query, address.getTags(true));
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns whether the given address matches a query token.
	 * 
	 * @param address The address to check.
	 * @param query The query tokens to match.
	 * @return True if the match succeeds.
	 */
	public boolean matchesQuery(AddressComponent address, SearchToken query) {
		try {
			switch (query.type) {
				case "r": // rank
					return address.rank == Byte.parseByte(query.value);
				case "id":
					return address.id.equals(query.value);
				case "rmin": // minimum rank
					return address.rank >= Byte.parseByte(query.value);
				case "rmax": // maximum rank
					return address.rank <= Byte.parseByte(query.value);
				
				case "a": // query by ancestor name
					for (AddressComponent ancestor: address.ancestors) {
						if (ancestor.id.equals(query.value))
							return true;
					}
					return false;
				case "an": // query by ancestor name
					for (AddressComponent ancestor: address.ancestors) {
						if (matchesQuery(ancestor, new SearchToken(query.value)))
							return true;
					}
					return false;
				
				default: // search inside the address's indexed tags array
					boolean found = false;
					for (String addrToken : address.getIndexedTags()) {
						if (query.fullMatch) { // must be an exact match
							found = (query.toString(true).equals(addrToken));
						} else { // do a prefix search
							found = addrToken.startsWith(query.toString(true));
						}
						if (found) break;
					}
					return found;
			}
			
		} catch (NumberFormatException ex) {
			Log.s("Invalid query token", query.toString());
		}
		return false;
	}
	
	public Map<String, AddressComponent> getAddresses() {
		return this.addresses;
	}
	
	public long getIndexCount() {
		return this.index.size();
	}
	
}
