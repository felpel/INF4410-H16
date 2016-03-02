package distributor;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import shared.*;

public class Distributor {
	private DistributorConfiguration configuration = null;
	List<ServerInterface> calculationServers = null;
	
	public static void main(String[] args) {
		if (args == null || args[0] == null || args[0].trim().isEmpty)){
			System.err.println("No filename was provided for the data. Program will exit.");
			return;
		}
		
		Distributor self = new Distributor();		
		self.process(args[0]);
	}
	
	public Distributor() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		
		loadConfiguration();
		loadServerStubs();
	}
	
	private void loadConfiguration() {
		this.loadConfiguration("distributor-config.json");
	}
	
	private void loadConfiguration(String filename) throws FileNotFoundException {
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
			ServerInterface stub = this.loadServerStub(serverInfo));
			this.calculationServers.add(stub);
		}
	}
	
	private void loadServerStub(ServerInformation serverInfo) {
		ServerInterface stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(serverInfo.getHost(), serverInfo.getPort());
            stub = (ServerInterface) registry.lookup("server");
        } catch (NotBoundException e) {
            System.out.println("Error: The name '" + e.getMessage() + "' is not defined in the registry.");
        } catch (AccessException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return stub;
	}
	
	private void process(String filename) {
		Task fullTask = this.readOperations(filename);
		
		//When not secured, we need to ask all 3 servers for the results
		//TODO Take full task and divide it in multiple tasks.
		//TODO Send tasks (list of operations) to servers even though 
		//we don't know each server's capacity.
		//TODO Manage servers' failures
		//TODO Show aggregated result
	}
	
	private Task readOperations(String filename) throws IOException {
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
		
		return task;
	}
}