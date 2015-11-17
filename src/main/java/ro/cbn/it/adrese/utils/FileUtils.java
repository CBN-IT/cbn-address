package ro.cbn.it.adrese.utils;

import java.io.*;

public class FileUtils {
	public static String getFromFile(String file) throws IOException {
		StringBuilder str = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(file)),
			"UTF-8"));
		String line;
		while ((line = br.readLine()) != null) {
			str.append(line);
		}
		br.close();
		return str.toString();
	}
}
