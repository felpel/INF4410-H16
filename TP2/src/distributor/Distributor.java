package distributor;

import java.io.FileNotFoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.AccessException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.io.IOException;
import java.nio.charset.Charset;

import shared.*;

public class Distributor {
	private static final int NB_WORKERS = 3;
	private DistributorConfiguration configuration = null;
	private List<ServerInterface> calculationServers = null;
	//private ConcurrentLinkedQueue<Task> pendingTasks = null;
	
	public static void main(String[] args) {
		Distributor self = new Distributor();	

		try {
			self.process();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} catch(Exception e) {}
	}
	
	public Distributor() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		

		try {
			loadConfiguration();
			loadServerStubs();
		} catch(IOException ioe) { 

		}
		catch(Exception e) {}
	}
	
	private void loadConfiguration() throws IOException {
		this.loadConfiguration("distributor-config.json");
	}
	
	private void loadConfiguration(String filename) throws IOException {
		this.configuration = 
			Utilities.<DistributorConfiguration>readJsonConfiguration(filename, DistributorConfiguration.class);
	}
	
	private void loadServerStubs() {
		if (this.configuration == null) {
			//TODO Maybe load default configuration?
			return;
		}
		
		this.calculationServers = new ArrayList<ServerInterface>();
		for (ServerInformation serverInfo : this.configuration.getServers()) {
			ServerInterface stub = this.loadServerStub(serverInfo);
			this.calculationServers.add(stub);
		}
	}
	
	private ServerInterface loadServerStub(ServerInformation serverInfo) {
		ServerInterface stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(serverInfo.getHost(), 5000);
            Integer fuckyoujava = new Integer(serverInfo.getPort());
            stub = (ServerInterface) registry.lookup("server" + fuckyoujava.toString());
        } catch (NotBoundException e) {
            System.out.println("Error: The name '" + e.getMessage() + "' is not defined in the registry.");
        } catch (AccessException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return stub;
	}
	
	private void process() throws IOException {
		ExecutorService executor = Executors.newFixedThreadPool(NB_WORKERS);
		
		int result = 0;
		//www.vogella.com/tutorials/JavaConcurrency/article.html#gainandissues
		//Executors javadoc
		//List<Future<int>> 
		List<Task> tasks = this.readOperations("./donnees/" + this.configuration.getDataFilename());
		for (Task t : tasks) {
			result += this.calculationServers.get(0).process(t).uccess;
		}

		System.out.println(String.format("Result: \t%d", result % 5000));	
		
		//When not secured, we need to ask all 3 servers for the results
		//TODO Take full task and divide it in multiple tasks.
		//TODO Send tasks (list of operations) to servers even though 
		//we don't know each server's capacity.
		//TODO Manage servers' failures
		//TODO Show aggregated result
	}
	
	private List<Task> readOperations(String filename) throws IOException {
		List<Task> tasks = new ArrayList<Task>();

		Path filePath = Paths.get(filename);
		
		System.out.println(filePath.toString());
		if (!Files.exists(filePath)) {
			throw new FileNotFoundException();
		}
		
		Charset cs = Charset.forName("utf-8");
		List<String> instructions = Files.readAllLines(filePath, cs);
		
		for (int i = 0; i < instructions.size(); i += configuration.getBatchSize()) {
			Task task = new Task();
			for(int j = i; j < (this.configuration.getBatchSize() + i) && j < instructions.size(); j++) {
				String instruction = instructions.get(j);
				String[] instructionElements = instruction.split(" ");
				String operation = instructionElements[0];
				int operand = Integer.parseInt(instructionElements[1]);
				task.addSubTask(operation, operand);
			}
			tasks.add(task);
		}
		
		return tasks;
	}
}