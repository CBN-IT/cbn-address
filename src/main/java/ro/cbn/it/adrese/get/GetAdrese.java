package ro.cbn.it.adrese.get;

import ro.appenigne.web.framework.annotation.UrlPattern;
import ro.appenigne.web.framework.utils.GsonUtils;
import ro.appenigne.web.framework.utils.Log;
import ro.cbn.it.adrese.core.AhWarmup;
import ro.cbn.it.adrese.core.IController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

@UrlPattern("/get/Adrese")
public class GetAdrese extends IController {
	public static ArrayList<LinkedHashMap<String, String>> json;
	private static final int ABSOLUTE_MAX = 2000;

	@Override
	public void execute() throws Exception {
		ArrayList<LinkedHashMap<String, String>> response = new ArrayList<>();
		long start = System.nanoTime();
		String stringToSearch = req.getParameter("term");
		String maxCountS = req.getParameter("maxCount");
		int maxCount = 100;
		try {
			maxCount = Integer.parseInt(maxCountS, 10);
			if (maxCount > ABSOLUTE_MAX) {
				maxCount = ABSOLUTE_MAX;
			}
		} catch (NumberFormatException ignored) {
		}
		if (stringToSearch == null || stringToSearch.isEmpty()) {
			jsonResponse(response);
			return;
		}
		stringToSearch = stringToSearch.
			replaceAll("[îâ]", "[îâ]").
			replaceAll("[a]", "[aăîâ]").
			replaceAll("[i]", "[iîâ]").
			replaceAll("[t]", "[tț]").
			replaceAll("[ţ]", "[ț]").
			replaceAll("[s]", "[sș]").
			replaceAll("[ş]", "[ș]");
		Log.d(req.getParameter("term"), stringToSearch);

		Pattern p = Pattern.compile(stringToSearch, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		AhWarmup.readFromFile();
		for (LinkedHashMap<String, String> stringStringLinkedHashMap : json) {
			String nume = stringStringLinkedHashMap.get("nume");
			if (p.matcher(nume).find()) {
				response.add(stringStringLinkedHashMap);
				maxCount--;
				if (maxCount == 0) {
					break;
				}
			}

		}
		String s = GsonUtils.getGson().toJson(response);
		long end = System.nanoTime();
		double seconds = (double) (end - start) / 1000000000.0;
		Log.d("The search took " + seconds + " seconds and found " + response.size() + " results.");

		jsonResponse(response);
	}

}
