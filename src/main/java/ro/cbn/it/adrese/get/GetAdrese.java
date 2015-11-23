package ro.cbn.it.adrese.get;

import ro.appenigne.web.framework.annotation.UrlPattern;
import ro.appenigne.web.framework.utils.Log;
import ro.cbn.it.adrese.core.AhWarmup;
import ro.cbn.it.adrese.core.IController;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

@UrlPattern("/get/Adrese")
public class GetAdrese extends IController {
	public static ArrayList<LinkedHashMap<String, String>> json;
	private static final int ABSOLUTE_MAX = 2000;

	@Override
	public void execute() throws Exception {
		// set CORS headers
		this.resp.addHeader("Access-Control-Allow-Origin", "*");
		
		ArrayList<LinkedHashMap<String, String>> response = new ArrayList<>();
		long start = System.nanoTime();
		String search = req.getParameter("search");
		String judet = req.getParameter("nume_judet");
		String cod_judet = req.getParameter("cod_judet");
		String prescurtare_judet = req.getParameter("prescurtare_judet");
		String superior = req.getParameter("nume_superior");
		String prettyPrint = req.getParameter("prettyPrint");
		String maxCountS = req.getParameter("maxCount");
		int maxCount = 100;
		try {
			maxCount = Integer.parseInt(maxCountS, 10);
			if (maxCount > ABSOLUTE_MAX) {
				maxCount = ABSOLUTE_MAX;
			}
		} catch (NumberFormatException ignored) {
		}
		
		Pattern pSearch = getSearchableString(search);
		Pattern pJudet = getSearchableString(judet);
		Log.d(search, pSearch == null ? null : pSearch.toString(), judet, pJudet == null ? null : pJudet.toString(), cod_judet, prescurtare_judet);

		AhWarmup.readFromFile();
		for (LinkedHashMap<String, String> oras : json) {
			String nume = oras.get("nume");
			if (cod_judet == null || cod_judet.isEmpty() || cod_judet.equals(oras.get("judet"))) {
				if (prescurtare_judet == null || prescurtare_judet.isEmpty() || prescurtare_judet.equalsIgnoreCase(oras.get("prescurtare_judet"))) {
					if (pJudet == null || pJudet.matcher(oras.get("nume_judet")).find()) {
						if (superior == null || superior.isEmpty() || superior.equals(oras.get("nume_superior"))) {
							if (pSearch == null || pSearch.matcher(nume).find()) {
								response.add(oras);
								maxCount--;
								if (maxCount == 0) {
									break;
								}
							}
						}
					}
				}
			}
		}
		DecimalFormat df = new DecimalFormat("####0.0000");
		long end = System.nanoTime();
		double seconds = (double) (end - start) / 1000000000.0;
		Log.d("The search took " + df.format(seconds) + " seconds and found " + response.size() + " results.");
		if ("true".equals(prettyPrint)) {
			jsonResponsePrettyPrint(response);
		} else {
			jsonResponse(response);
		}
	}

	private Pattern getSearchableString(String s) {
		if (s == null || s.isEmpty()) {
			return null;
		}
		s = s.toLowerCase().replaceAll("[îâ]", "[îâ]")
			.replaceAll("[a]", "[aăîâ]")
			.replaceAll("[i]", "[iîâ]")
			.replaceAll("[t]", "[tț]")
			.replaceAll("[ţ]", "[ț]")
			.replaceAll("[s]", "[sș]")
			.replaceAll("[ş]", "[ș]");
		return Pattern.compile(s, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	}

}
