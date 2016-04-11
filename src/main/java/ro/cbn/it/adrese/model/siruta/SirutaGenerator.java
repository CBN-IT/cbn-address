package ro.cbn.it.adrese.model.siruta;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.github.jamm.MemoryMeter;
import ro.cbn.it.adrese.model.address.AddressComponent;
import ro.cbn.it.adrese.model.address.AddressDatabase;
import ro.cbn.it.adrese.utils.FileUtils;
import ro.cbn.it.framework.utils.GsonUtils;
import ro.cbn.it.framework.utils.StringUtils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Generates a SIRUTA indexed database and stores it in a serialized file.
 * <p>
 * To be invoked at build time (using the maven exec plugin).
 */
public class SirutaGenerator {
	
	/**
	 * Generates the searchable address database from the input file.
	 * 
	 * @param databaseFile The source database JSON file.
	 * @param countiesMapFile The counties map JSON file.
	 */
	private static AddressDatabase generateDatabase(String databaseFile, String countiesMapFile) throws IOException {
		String rawText = FileUtils.readTextFile(countiesMapFile);
		ArrayList<LinkedHashMap<String, String>> rawCounties = GsonUtils.getGson().fromJson(rawText,
			new TypeToken<ArrayList<LinkedHashMap<String, String>>>() {
			}.getType());
		
		Map<String, LinkedHashMap<String, String>> countiesMap = new HashMap<>();
		for (LinkedHashMap<String, String> county: rawCounties) {
			countiesMap.put(county.get("cod"), county);
		}
		
		rawText = FileUtils.readTextFile(databaseFile);
		ArrayList<LinkedHashMap<String, String>> siruta = GsonUtils.getGson().fromJson(rawText,
			new TypeToken<ArrayList<LinkedHashMap<String, String>>>() {}.getType());
		
		AddressDatabase database = new AddressDatabase();
		for (LinkedHashMap<String, String> obj : siruta) {
			Map<String, String> metaData = new HashMap<>();
			List<AddressComponent> ancestors = new ArrayList<>();
			
			int sirutaType = Integer.parseInt(obj.get("tip"));
			String name = StringUtils.capitalizeWords(obj.get("nume"));
			byte rank = 2; // default to city
			
			if (sirutaType == 40) {
				if (countiesMap.containsKey(obj.get("judet"))) {
					LinkedHashMap<String,String> county = countiesMap.get(obj.get("judet"));
					metaData.put("shortCountyName", county.get("prescurtare"));
				}
				rank = 0;
				
			} else if (sirutaType == 6) {
				name = name.replaceAll("(?i)Bucuresti ", "");
				name = name.replaceAll("(?i)Sectorul", "Sector");
				
			} else if (obj.get("tip").equals("3")) {
				name = "Comuna " + name;
				
			} else if (sirutaType == 22 || sirutaType == 23 ||
				sirutaType == 11 || sirutaType == 19) {
				name = "Sat " + name;
			}
			
			if (rank > 0) {
				// fill out the ancestors
				String superior_loc = obj.get("superior");
				if (superior_loc != null && !superior_loc.isEmpty()) {
					AddressComponent ancestor = database.findById(superior_loc);
					ancestors.add(ancestor);
					for (AddressComponent a2 : ancestor.ancestors) {
						ancestors.add(a2);
					}
					rank = (byte)(ancestor.rank + 1);
				}
			}
			
			AddressComponent address = new AddressComponent(obj.get("siruta"), rank, name, ancestors);
			address.metaData = metaData;
			
			database.add(address);
		}
		
		return database;
	}
	
	/**
	 * Main code for generating an indexed database from SIRUTA source.
	 * 
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		try {
			long started = System.nanoTime();
			MemoryMeter meter = new MemoryMeter();
			
			AddressDatabase database = generateDatabase(args[0], args[1]);
			
			long ended = System.nanoTime();
			double seconds = (double)(ended - started)/ 1000000000.0;
			DecimalFormat timeDiffFormat = new DecimalFormat("####0.0000");
			System.out.println("[SirutaGenerator] Finished generating the database in " + 
				timeDiffFormat.format(seconds) + " s");
			System.out.println("[SirutaGenerator] Total addresses: " + database.getAddresses().size() + 
				"; Total indexed prefixes: " + database.getIndexCount());
			System.out.println("[SirutaGenerator] Estimated memory usage: " + 
				meter.measureDeep(database) + " B");
			
			Gson gson = SirutaProvider.getAddressDatabaseGson();
			FileUtils.writeTextFile(args[2] + ".json", gson.toJson(database));
			FileUtils.writeSerializedObjToFile(args[2] + ".bin", database);
			
			AddressDatabase loadedDb = SirutaProvider.getDatabase();
			
			ended = System.nanoTime();
			seconds = (double)(ended - started)/ 1000000000.0;
			System.out.println("[SirutaGenerator] Finished everything in " + 
				timeDiffFormat.format(seconds) + " s");
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
