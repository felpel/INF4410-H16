package server;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class Server {
	public static void main(String[] args) {
		try {
			//DEBUG
			ServerConfiguration cfg = Utilities.readJsonConfiguration("srv-config.json");
			System.out.println(cfg.toString());
			//Utilities.readOperations("./donnees/donnees-4172.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
