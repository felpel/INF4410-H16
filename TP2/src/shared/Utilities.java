package shared;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.Charset;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

public class Utilities {

	//Function used to read a Json config file, deserialize it to an object of type clazz and return that object
	public static <T> T readJsonConfiguration(String filename, Class<T> clazz) throws IOException {
		Path configPath = Paths.get(filename);

		if (!Files.exists(configPath) || !configPath.toString().endsWith(".json")) {
			throw new FileNotFoundException(filename);
		}

		Utilities.log(String.format("Loading configuration [%s]", clazz.toString()));
		String json = new String(Files.readAllBytes(configPath));

		T cfg = null;
		try {
			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.create();
			cfg = gson.fromJson(json, clazz);
			Utilities.log(cfg.toString());
		} catch (JsonParseException jpe) {
			Utilities.logError("Unable to parse correctly JSON of calculation server configuration...");
			jpe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cfg;
	}

	public static void log(String message) {
		log(System.out, message);
	}

	public static void log(PrintStream ps, String message) {
		log(ps, "> ", message);
	}

	public static void logInformation(String message) {
		log(System.out, "---> ", message);
	}

	public static void logError(String message) {
		log(System.err, "*** ", message);
	}

	public static void log(PrintStream ps, String prefix, String message) {
		ps.println(String.format("%s%s", prefix, message));
	}
}
