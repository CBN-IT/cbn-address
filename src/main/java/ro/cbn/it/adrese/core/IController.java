package ro.cbn.it.adrese.core;


import com.google.gson.Gson;
import ro.cbn.it.framework.servlet.AbstractIController;
import ro.cbn.it.framework.utils.GsonUtils;

import java.io.IOException;

/**
 * The public abstract class that all controllers must implement.
 */
public abstract class IController extends AbstractIController {
	public Gson gson = GsonUtils.getGson();
	public void jsonResponsePrettyPrint(Object o) throws IOException {
		resp.setContentType("application/json; charset=UTF-8");
		if (o instanceof String) {
			resp.getWriter().print(o);
		} else {
			resp.getWriter().print(GsonUtils.getGsonPrettyPrint().toJson(o));
		}
	}
}
