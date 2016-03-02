package server;

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
	public static final int MODULATOR = 5000;
	
	public static ServerConfiguration readJsonConfiguration(String filename) throws IOException {
		Path srvConfig = Paths.get(filename);
		
		if (!Files.exists(srvConfig) || !srvConfig.toString().endsWith(".json")) {
			throw new FileNotFoundException(filename);
		}
		
		String json = new String(Files.readAllBytes(srvConfig));			
		System.out.println(json); // DEBUG
		
		ServerConfiguration cfg = null;
		try {
			Gson gson = new Gson();		
			cfg = gson.fromJson(json, ServerConfiguration.class);
		} catch (JsonParseException jpe) {
			System.err.println("Unable to parse correctly JSON of calculation server configuration...");
			jpe.printStackTrace();
		}
		
		return cfg;	
	}
	
	public static void readOperations(String filename) throws IOException {
		Path filePath = Paths.get(filename);
		
		if (!Files.exists(filePath)) {
			throw new FileNotFoundException();
		}
		
		Charset cs = Charset.forName("utf-8");
		List<String> instructions = Files.readAllLines(filePath, cs);
		
		Task task = new Task();
		for (String instruction : instructions) {
			String[] instructionElements = instruction.split(" ");
			String operation = instructionElements[0];
			int operand = Integer.parseInt(instructionElements[1]);
			task.addSubTask(operation, operand);
		}
		
		//DEBUG
		/*System.out.println("Operations to calculate:");
		for (SubTask st : task.getSubTasks()) {
			System.out.println(String.format("%s(%d)", st.getOperation(), st.getOperand()));
		}*/
		
		//DEBUG
		System.out.println(String.format("Result for task: %d", calculateResultForTask(task)));
	}
	
	public static int fib(int x) {
		if (x == 0)
			return 0;
		if (x == 1)
			return 1;
		return fib(x - 1) + fib(x - 2);
	}
	
	public static int prime(int x) {
		int highestPrime = 0;
		
		for (int i = 1; i <= x; ++i)
		{
			if (isPrime(i) && x % i == 0 && i > highestPrime)
				highestPrime = i;
		}
		
		return highestPrime;
	}
	
	private static boolean isPrime(int x) {
		if (x <= 1)
			return false;

		for (int i = 2; i < x; ++i)
		{
			if (x % i == 0)
				return false;
		}
		
		return true;		
	}
	
	public static int calculateResultForTask(Task task) {
		//TODO Verify if it is actually what we want
		//TODO We can '//' the treatment 
		int tempResult = 0;
		for (SubTask st : task.getSubTasks()) {
			System.out.println(String.format("%s(%d)", st.getOperation(), st.getOperand()));
			switch (st.getOperation()) {
				case "fib":
					tempResult += fib(st.getOperand());
					tempResult %= MODULATOR;
				break;
				case "prime":
					tempResult += prime(st.getOperand());
					tempResult %= MODULATOR;
					break;
				default:
					System.err.println(String.format("Undefined operation '%s'", st.getOperation()));
					break;
			}
		}
		return tempResult;
	}
}
