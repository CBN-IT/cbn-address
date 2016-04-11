package ro.cbn.it.adrese.model.address;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Implements a Gson TypeAdapter for serializing / deserializing instances of AddressDatabase.
 */
public class AddressDatabaseGsonAdapter implements JsonSerializer<AddressDatabase>, JsonDeserializer<AddressDatabase> {
	
	@Override
	public JsonElement serialize(AddressDatabase db, Type type, JsonSerializationContext context) {
		JsonObject obj = new JsonObject();
		JsonArray addresses = new JsonArray();
		AddressComponentGsonAdapter addressAdapter = new AddressComponentGsonAdapter(db);
		Type addressComponentType = new TypeToken<AddressComponent>() {}.getType();
		for (AddressComponent addr: db.addresses.values()) {
			addresses.add(addressAdapter.serialize(addr, addressComponentType, context));
		}
		obj.add("addresses", addresses);
		return obj;
	}
	
	@Override
	public AddressDatabase deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj = jsonElement.getAsJsonObject();
		JsonArray addresses = obj.get("addresses").getAsJsonArray();
		AddressDatabase db = new AddressDatabase();
		AddressComponentGsonAdapter addressAdapter = new AddressComponentGsonAdapter(db);
		Type addressComponentType = new TypeToken<AddressComponent>() {}.getType();
		
		for (JsonElement el: addresses) {
			AddressComponent address = addressAdapter.deserialize(el, addressComponentType, context);
			db.add(address);
		}
		
		return db;
	}
	
}
