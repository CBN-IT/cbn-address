package ro.cbn.it.adrese.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ro.cbn.it.adrese.core.IController;
import ro.cbn.it.adrese.model.address.AddressComponent;
import ro.cbn.it.adrese.model.address.AddressComponentGsonAdapter;
import ro.cbn.it.adrese.model.address.AddressDatabase;
import ro.cbn.it.adrese.model.siruta.SirutaProvider;
import ro.cbn.it.adrese.utils.TextPatternUtils;
import ro.cbn.it.framework.annotation.UrlPattern;
import ro.cbn.it.framework.utils.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Endpoint for querying the address database.
 */
@UrlPattern("/searchAddress")
public class SearchAddress extends IController {
	
	private static final int RESULTS_LIMIT_DEFAULT = 100;
	private static final int RESULTS_LIMIT_MAX = 1000;
	
	@Override
	public void execute() throws Exception {
		// Allow cross-origin requests
		String origin = this.req.getHeader("Origin");
		if (origin != null && !origin.isEmpty()) {
			this.resp.setHeader("Access-Control-Allow-Origin", origin);
		}
		
		if ("OPTIONS".equalsIgnoreCase(this.req.getMethod())) {
			this.resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
			return;
		}
		
		long started = System.nanoTime();
		
		// parse parameters
		String search = req.getParameter("query");
		if (search == null)
			search = ""; // the query must not be null
		
		Collection<String> queryTokens = TextPatternUtils.tokenizeWords(search);
		
		String ancestor = req.getParameter("ancestor");
		Pattern numericAncestorPat = Pattern.compile("^[0-9]+$");
		if (ancestor != null && !ancestor.isEmpty()) {
			Collection<String> ancestorTokens = TextPatternUtils.tokenizeWords(ancestor);
			for (String ancToken: ancestorTokens) {
				if (numericAncestorPat.matcher(ancToken).find()) {
					queryTokens.add("#a=" + ancToken + "$");
				} else { // search ancestor names
					queryTokens.add("#an=" + ancToken);
				}
			}
		}
		
		if (req.getParameter("maxRank") != null && !req.getParameter("maxRank").isEmpty()) {
			queryTokens.add("#rmax=" + req.getParameter("maxRank") + "$");
		}
		
		boolean onlyOwnNames = false;
		if (req.getParameter("onlyOwnNames")!=null) {
			onlyOwnNames = true;
		}
		
		
		String maxCountS = req.getParameter("maxCount");
		int maxCount = RESULTS_LIMIT_DEFAULT;
		if (maxCountS != null && !maxCountS.isEmpty()) {
			maxCount = Integer.parseInt(maxCountS);
			if (maxCount > RESULTS_LIMIT_MAX) {
				maxCount = RESULTS_LIMIT_MAX;
			}
		}
		
		String prettyPrint = req.getParameter("prettyPrint");
		
		AddressDatabase addrDb = SirutaProvider.getDatabase();
		
		Log.i("Query: search: '" + search + "'; ancestors: " + ancestor + "; tokens: ", queryTokens);
		LinkedHashSet<AddressComponent> results = new LinkedHashSet<>();
		
		// first, query only the own names
		List<String> ownNameQueryTokens = new ArrayList<>();
		for (String token: queryTokens) {
			if (token.startsWith("#")) {
				ownNameQueryTokens.add(token);
			} else {
				ownNameQueryTokens.add("#o=" + token);
			}
		}
		
		results.addAll(addrDb.search(ownNameQueryTokens, maxCount));
		if (!onlyOwnNames) {
			int remaining = maxCount - results.size();
			if (remaining < 0) remaining = 0;
			// fill the remaining results with the ancestor
			results.addAll(addrDb.search(queryTokens, remaining));
		}
		
		DecimalFormat df = new DecimalFormat("####0.0000");
		long ended = System.nanoTime();
		double seconds = (double) (ended - started) / 1000000000.0;
		
		Log.i("The search took " + df.format(seconds) + " seconds and found " + results.size() + " results.");
		this.resp.setContentType("application/json; charset=UTF-8");
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(AddressComponent.class, new AddressComponentGsonAdapter(addrDb, true));
		if ("true".equals(prettyPrint)) {
			gsonBuilder.setPrettyPrinting();
		}
		
		Gson gson = gsonBuilder.create();
		this.resp.getWriter().print(gson.toJson(results));
	}

}
