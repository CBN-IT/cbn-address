package ro.cbn.it.adrese.core;

import ro.cbn.it.adrese.model.siruta.SirutaProvider;
import ro.cbn.it.framework.annotation.UrlPattern;
import ro.cbn.it.framework.utils.Log;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@UrlPattern("/_ah/warmup")
public class AhWarmup extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			SirutaProvider.getDatabase();
		} catch (Exception e) {
			Log.s("Exception while reading database", e);
		}
		resp.setContentType("text/plain; charset=UTF-8");
		resp.getWriter().print("ok");
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
}
