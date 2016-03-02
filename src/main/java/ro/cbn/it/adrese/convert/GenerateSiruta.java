package ro.cbn.it.adrese.convert;

import com.google.gson.reflect.TypeToken;
import ro.cbn.it.adrese.utils.FileUtils;
import ro.cbn.it.framework.utils.GsonUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class GenerateSiruta {
	
	public static ArrayList<LinkedHashMap<String, String>> json;
	
	public static int main(String[] args) {
		long start = System.nanoTime();
		try {
			if (args.length != 2) {
				System.err.println("Error: no input/destination files given!");
			}
			
			String orase = FileUtils.getFromFile(args[0] + "/siruta_parsat.json");
			
			json = GsonUtils.getGson().fromJson(orase, new TypeToken<ArrayList<LinkedHashMap<String, String>>>() {
			}.getType());
			
			String judeteFile = FileUtils.getFromFile(args[0] + "/judete.json");
			ArrayList<LinkedHashMap<String, String>> judete = GsonUtils.getGson().fromJson(judeteFile, new TypeToken<ArrayList<LinkedHashMap<String, String>>>() {
			}.getType());
			for (LinkedHashMap<String, String> oras : json) {
				for (LinkedHashMap<String, String> judet : judete) {
					if(oras.get("judet").equals(judet.get("cod"))){
						oras.put("nume_judet",judet.get("nume"));
						oras.put("prescurtare_judet",judet.get("prescurtare"));
						break;
					}
				}
				if(oras.get("tip").equals("40")){
					oras.put("nivel","judet");
					oras.put("rank", "0");
					continue;
				}
				for (LinkedHashMap<String, String> oras2 : json) {
					if(oras.get("superior").equals(oras2.get("siruta"))){
						oras.put("nume_superior", oras2.get("nume"));
						if(oras2.get("nivel").equals("judet")){
							oras.put("nivel","localitate superioara");
							oras.put("rank", "1");
						}else if(oras2.get("nivel").equals("localitate superioara")){
							oras.put("nivel","localitate inferioara");
							oras.put("rank", "2");
						}
						break;
					}
				}
			}
			
			String result = GsonUtils.getGsonPrettyPrint().toJson(json);
			PrintWriter printer = new PrintWriter(args[1]);
			printer.println(result);
			printer.close();
			
			long end = System.nanoTime();
			double seconds = (double)(end - start)/ 1000000000.0;
			DecimalFormat df = new DecimalFormat("####0.0000");
			System.out.println("[GenerateSiruta] Finished processing SIRUTA json in "+df.format(seconds));
			
			return 0;
			
		} catch (IOException e) {
			e.printStackTrace();
			return 2;
		}
	}
}
