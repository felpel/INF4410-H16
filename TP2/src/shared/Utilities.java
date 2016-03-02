package shared;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.Charset;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

public class Utilities {
	public static <T> T readJsonConfiguration(String filename, Class<T> clazz) throws IOException {
		Path configPath = Paths.get(filename);
		
		if (!Files.exists(configPath) || !configPath.toString().endsWith(".json")) {
			throw new FileNotFoundException(filename);
		}
		
		String json = new String(Files.readAllBytes(configPath));			
		System.out.println(json); // DEBUG
		
		T cfg = null;
		try {
			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.create();		
			cfg = gson.fromJson(json, clazz);
		} catch (JsonParseException jpe) {
			System.err.println("Unable to parse correctly JSON of calculation server configuration...");
			jpe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return cfg;	
	}
}
