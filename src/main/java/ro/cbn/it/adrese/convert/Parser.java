package ro.cbn.it.adrese.convert;

import com.google.gson.reflect.TypeToken;
import ro.appenigne.web.framework.utils.GsonUtils;
import ro.cbn.it.adrese.utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class Parser {
	public static void main(String[] args){
		try {
			long start = System.nanoTime();
			String stringToSearch = "targovis";
			stringToSearch=stringToSearch.
				replaceAll("[îâ]","[îâ]").
				replaceAll("[a]","[aăîâ]").
				replaceAll("[i]","[iîâ]").
				replaceAll("[t]","[tț]").
				replaceAll("[ţ]","[ț]").
				replaceAll("[s]","[s\u0219]").
				replaceAll("[ş]","[ș]");
			System.out.println(stringToSearch);
			//stringToSearch = StringUtils.removeDiacritics(stringToSearch).toLowerCase();
			String dir = System.getProperty("user.dir")+"\\src\\main\\webapp\\";
			String fromFile = FileUtils.getFromFile(dir+"siruta/siruta_parsat.json");
			ArrayList<LinkedHashMap<String,String>> json = GsonUtils.getGson().fromJson(fromFile, new TypeToken<ArrayList<LinkedHashMap<String,String>>>(){}.getType());
			long start2 = System.nanoTime();
			ArrayList<LinkedHashMap<String,String>> response = new ArrayList<>();
			Pattern p = Pattern.compile(stringToSearch,Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
			for (LinkedHashMap<String, String> stringStringLinkedHashMap : json) {
				String nume = stringStringLinkedHashMap.get("nume");
				if (p.matcher(nume).find()) {
					response.add(stringStringLinkedHashMap);
				}
			}
			String s = GsonUtils.getGson().toJson(response);
			System.out.println(s);
			long end = System.nanoTime();
			double seconds = (double)(end - start)/ 1000000000.0;
			double seconds2 = (double)(end - start2)/ 1000000000.0;
			System.out.println(seconds);
			System.out.println(seconds2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
