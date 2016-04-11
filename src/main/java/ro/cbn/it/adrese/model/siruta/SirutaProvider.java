package ro.cbn.it.adrese.model.siruta;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ro.cbn.it.adrese.model.address.AddressComponent;
import ro.cbn.it.adrese.model.address.AddressComponentGsonAdapter;
import ro.cbn.it.adrese.model.address.AddressDatabase;
import ro.cbn.it.adrese.model.address.AddressDatabaseGsonAdapter;
import ro.cbn.it.adrese.utils.FileUtils;
import ro.cbn.it.framework.utils.Log;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Singleton class that provides an in-memory address database read from a serialized file.
 */
public class SirutaProvider {
	
	/**
	 * The name of the serialized database file to load from.
	 */
	public static final String DATABASE_FILE = "siruta/database.json";
	
	/**
	 * Lock object used for thread synchronization.
	 */
	protected static final Object lock = new Object();
	
	/**
	 * Reference to the opened database.
	 */
	protected static AddressDatabase database;
	
	/**
	 * Returns a SIRUTA database reference.
	 * 
	 * @return A searchable address database.
	 */
	public static AddressDatabase getDatabase() throws IOException, ClassNotFoundException {
		synchronized (lock) {
			if (database == null) {
				long startedLoading = System.nanoTime();
				long ended; double seconds;
				DecimalFormat timeDiffFormat = new DecimalFormat("####0.0000");
				
				Gson gson = getAddressDatabaseGson();
				String contents = FileUtils.readTextFile(DATABASE_FILE);
				
				ended = System.nanoTime();
				seconds = (double)(ended - startedLoading)/ 1000000000.0;
				Log.i("Finished reading the SIRUTA database in " + timeDiffFormat.format(seconds) + " s");
				
				database = gson.fromJson(contents, AddressDatabase.class);
				//database = (AddressDatabase) FileUtils.readSerializedObjFromFile(DATABASE_FILE, AddressDatabase.class);
				
				ended = System.nanoTime();
				seconds = (double)(ended - startedLoading)/ 1000000000.0;
				Log.i("Finished loading the SIRUTA database in " + timeDiffFormat.format(seconds) + " s");
				
				int mb = 1024 * 1024;
				Runtime runtime = Runtime.getRuntime();
				Log.i("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb + " MB");
			}
			return database;
		}
	}
	
	
	/**
	 * Returns the Gson builder to use for serializing / deserializing the address database.
	 * 
	 * @return A Gson instance to use for reading/writing the address database
	 */
	public static Gson getAddressDatabaseGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(AddressDatabase.class, new AddressDatabaseGsonAdapter());
		return gsonBuilder.create();
	}
	
}
