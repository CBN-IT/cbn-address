package ro.cbn.it.adrese.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileUtils {
	
	/**
	 * Reads a text file and returns its contents as string.
	 * Newlines are normalized to "\n"!
	 * 
	 * @param file The input file.
	 * @return The file's contents.
	 * @throws IOException
	 */
	public static String readTextFile(String file) throws IOException {
		StringBuilder str = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(
			new FileInputStream(file), StandardCharsets.UTF_8));
		String line;
		while ((line = br.readLine()) != null) {
			str.append(line).append("\n");
		}
		br.close();
		return str.toString();
	}
	
	/**
	 * Writes a string to a text file.
	 * 
	 * @param file The output file.
	 * @param data The file's contents.
	 * @throws IOException The exception.
	 */
	@SuppressWarnings("AppEngineForbiddenCode")
	public static void writeTextFile(String file, String data) throws IOException {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(file), StandardCharsets.UTF_8));
			bw.write(data);
			
		} finally {
			try {
				if (bw != null)
					bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Reads a file containing a Java serialized object and returns the unserialized instance.
	 * 
	 * @param filename The path of the file to read.
	 * @param clazz The name class to deserialize.
	 * @return The serialized object.
	 * @throws IOException If the file could not be read.
	 * @throws ClassNotFoundException If the serialized class is invalid.
	 */
	public static Object readSerializedObjFromFile(String filename, Class<?> clazz) throws IOException, ClassNotFoundException {
		Kryo kryo = new Kryo();
		kryo.setReferences(false);
		FileInputStream fileIn = new FileInputStream(filename);
		Input in = new Input(fileIn);
		Object obj = kryo.readObject(in, clazz);
		in.close();
		return obj;
	}
	
	/**
	 * Serializes a Java object and writes it to a file.
	 * 
	 * @param filename Path to the destination file.
	 * @param obj The object to serialize.
	 * @throws IOException If the file could not be written / object cannot be serialized.
	 */
	@SuppressWarnings("AppEngineForbiddenCode")
	public static void writeSerializedObjToFile(String filename, Object obj) throws IOException {
		Kryo kryo = new Kryo();
		kryo.setReferences(false);
		FileOutputStream fileOut = new FileOutputStream(filename);
		Output out = new Output(fileOut);
		kryo.writeObject(out, obj.getClass());
		out.close();
		fileOut.close();
	}
	
}
