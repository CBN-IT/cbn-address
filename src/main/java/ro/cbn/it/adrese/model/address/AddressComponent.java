package ro.cbn.it.adrese.model.address;

import com.google.gson.annotations.Expose;
import ro.cbn.it.adrese.utils.TextPatternUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Stores an address component.
 */
public class AddressComponent implements Serializable {
	
	
	/**
	 * The unique identifier of the address component.
	 */
	@Expose
	public String id;
	
	/**
	 * The component's rank (i.e. hierarchy tree depth).
	 */
	@Expose
	public byte rank;
	
	/**
	 * The name / label of the current component.
	 */
	@Expose
	public String name;
	
	/**
	 * Caches the tags used for indexing / matching the instance with a query.
	 */
	protected Set<String> ownTags = null, inheritableTags = null;
	
	/**
	 * A list of ancestors for the current address item, in reverse order.
	 * 
	 * The first item is the parent, the 2nd is the parent's parent etc.
	 */
	@Expose
	public List<AddressComponent> ancestors = new ArrayList<>();
	
	/**
	 * Stores address metadata.
	 */
	@Expose
	public Map<String, String> metaData = new HashMap<>();
	
	/**
	 * Default constructor.
	 */
	public AddressComponent() {
	}
	
	/**
	 * Address constructor.
	 * @param name The name of the address.
	 * @param ancestors The ancestors.
	 */
	public AddressComponent(String id, byte rank, String name, List<AddressComponent> ancestors) {
		this.id = id;
		this.rank = rank;
		this.name = name;
		this.ancestors = ancestors;
	}
	
	/**
	 * Returns the parent address component.
	 * 
	 * @return The parent component or null if not defined.
	 */
	public AddressComponent getParent() {
		if (ancestors.isEmpty())
			return null;
		return ancestors.get(0);
	}
	
	/**
	 * Returns the component's private tags.
	 * 
	 * @return A string array with the requested tags.
	 */
	public Set<String> getOwnTags() {
		generateTags();
		return this.ownTags;
	}
	
	/**
	 * Returns the component's inheritable tags.
	 * 
	 * @return A string array with the requested tags.
	 */
	public Set<String> getInheritableTags() {
		generateTags();
		return this.inheritableTags;
	}
	
	/**
	 * Returns the set of tags to use for indexing.
	 * 
	 * @return The index tags for the current instance.
	 */
	public Set<String> getIndexedTags() {
		Set<String> indexSet = new HashSet<>();
		indexSet.addAll(getOwnTags());
		indexSet.addAll(getInheritableTags());
		for (AddressComponent ancestor: ancestors) {
			indexSet.addAll(ancestor.getInheritableTags());
		}
		return indexSet;
	}
	
	/**
	 * Generates the tags assigned to the current address instance (used for indexing / searching purposes).
	 * 
	 * The tags include:
	 * - the tokenized name;
	 * - the current instance's id for ancestor lookup;
	 * 
	 * The tags will be normalized (i.e. to lower case, with accents removed).
	 */
	protected void generateTags() {
		if (this.ownTags != null && this.inheritableTags != null)
			return;
		
		this.ownTags = new HashSet<>();
		this.inheritableTags = new HashSet<>();
		
		// add ancestor ID as inheritable
		inheritableTags.add("#a=" + this.id);
		
		// tokenize the current instance
		List<String> names = new ArrayList<>();
		names.add(this.name);
		for (String meta: this.metaData.values()) {
			names.add(meta);
		}
		for (String token: TextPatternUtils.tokenizeWords(names)) {
			ownTags.add("#o=" + normalizeForIndexing(token));
			inheritableTags.add(normalizeForIndexing(token));
		}
	}
	
	/**
	 * Tokenizes a tag for indexing / matching the current address instance.
	 * 
	 * @param text The input token to normalize.
	 * @return The normalized token.
	 */
	protected static String normalizeForIndexing(String text) {
		return TextPatternUtils.normalizeSirutaText(TextPatternUtils.removeAccents(text).toLowerCase());
	}
	
	@Override
	public int hashCode() {
		if (this.id == null) return 0;
		return this.id.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof AddressComponent))
			return false;
		return this.id != null && this.id.equals(((AddressComponent) obj).id);
	}
}
