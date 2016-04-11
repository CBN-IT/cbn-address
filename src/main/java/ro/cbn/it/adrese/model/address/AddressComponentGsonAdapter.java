package ro.cbn.it.adrese.model.address;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

/**
 * Implements a Gson TypeAdapter for efficiently serializing / deserializing instances of AddressComponent.
 */
@SuppressWarnings("UnusedParameters")
public class AddressComponentGsonAdapter implements JsonSerializer<AddressComponent>, JsonDeserializer<AddressComponent> {
	
	/**
	 * The address database to use for optimizing the address object references.
	 */
	private AddressDatabase db;
	
	private boolean skipTags = false;
	
	/**
	 * Constructor with existing database instance.
	 * 
	 * @param db The parent address database. 
	 */
	public AddressComponentGsonAdapter(AddressDatabase db) {
		this.db = db;
	}
	
	/**
	 * Constructor with skip tags flag.
	 * 
	 * @param db The parent address database.
	 * @param skipTags Whether to skip outputting the tags.
	 */
	public AddressComponentGsonAdapter(AddressDatabase db, boolean skipTags) {
		this.skipTags = skipTags;
	}
	
	@Override
	public JsonElement serialize(AddressComponent addr, Type type, JsonSerializationContext context) {
		return serialize(addr, type, context, false);
	}
	
	/**
	 * Serialize variant that skips putting the ancestors.
	 * 
	 * @param addr The address to serialize.
	 * @param type The class / type of the object to serialize.
	 * @param context The serialization context.
	 * @param noAncestors Whether to skip ancestors / tags.
	 * @return The serialized JSON element.
	 */
	public JsonElement serialize(AddressComponent addr, Type type, JsonSerializationContext context, boolean noAncestors) {
		JsonObject obj = new JsonObject();
		obj.add("id", context.serialize(addr.id));
		obj.add("rank", context.serialize(addr.rank));
		obj.add("name", context.serialize(addr.name));
		obj.add("metaData", context.serialize(addr.metaData));
		
		if (!noAncestors) {
			if (!skipTags) {
				obj.add("ownTags", context.serialize(addr.ownTags));
				obj.add("inheritableTags", context.serialize(addr.inheritableTags));
			}
			
			JsonArray ancestorsArr = new JsonArray();
			for (AddressComponent ancestor: addr.ancestors) {
				JsonElement jsonAncestor = serialize(ancestor, type, context, true);
				ancestorsArr.add(jsonAncestor);
			}
			obj.add("ancestors", ancestorsArr);
		}
		
		return obj;
	}
	
	@Override
	public AddressComponent deserialize(JsonElement el, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj = el.getAsJsonObject();
		AddressComponent addr = new AddressComponent();
		
		addr.id = obj.get("id").getAsString();
		AddressComponent existingAddr = db.findById(addr.id);
		if (existingAddr != null) {
			return existingAddr;
		}
		addr.name = obj.get("name").getAsString();
		addr.rank = obj.get("rank").getAsByte();
		
		Type mapType = new TypeToken<Map<String, String>>() {}.getType();
		addr.metaData = context.deserialize(obj.get("metaData"), mapType);
		
		if (obj.has("tags")) {
			String[] tags = deserializeStringArr(obj.get("ownTags").getAsJsonArray());
			addr.ownTags = new HashSet<>();
			Collections.addAll(addr.ownTags, tags);
		}
		if (obj.has("inheritableTags")) {
			String[] inheritableTags = deserializeStringArr(obj.get("inheritableTags").getAsJsonArray());
			addr.inheritableTags = new HashSet<>();
			Collections.addAll(addr.inheritableTags, inheritableTags);
		}
		if (obj.has("ancestors")) {
			JsonArray jsonAncestors = obj.get("ancestors").getAsJsonArray();
			
			ArrayList<AddressComponent> ancestors = new ArrayList<>();
			for (JsonElement jsonAncestor: jsonAncestors) {
				ancestors.add(deserialize(jsonAncestor, type, context));
			}
			addr.ancestors = ancestors;
		}
		
		return addr;
	}
	
	private String[] deserializeStringArr(JsonArray jsonArr) {
		ArrayList<String> arrList = new ArrayList<>();
		
		for (JsonElement el: jsonArr) {
			arrList.add(el.getAsString());
		}
		return arrList.toArray(new String[arrList.size()]);
	}
	
}
