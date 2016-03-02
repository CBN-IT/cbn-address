package ro.cbn.it.adrese.core;

import com.google.gson.reflect.TypeToken;
import ro.cbn.it.adrese.get.GetAdrese;
import ro.cbn.it.adrese.utils.FileUtils;
import ro.cbn.it.framework.annotation.UrlPattern;
import ro.cbn.it.framework.utils.GsonUtils;
import ro.cbn.it.framework.utils.Log;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@UrlPattern("/_ah/warmup")
public class AhWarmup extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		readFromFile();
		resp.setContentType("text/plain; charset=UTF-8");
		resp.getWriter().print("ok");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	public static void readFromFile() throws IOException {
		long start = System.nanoTime();
		if (GetAdrese.json == null) {
			String orase = FileUtils.getFromFile("siruta/processed.json");
			GetAdrese.json = GsonUtils.getGson().fromJson(orase, new TypeToken<ArrayList<LinkedHashMap<String, String>>>() {
			}.getType());
		}
		long end = System.nanoTime();
		double seconds = (double)(end - start)/ 1000000000.0;
		DecimalFormat df = new DecimalFormat("####0.0000");
		Log.d("Loaded json into memory in "+df.format(seconds));
	}
	
}
