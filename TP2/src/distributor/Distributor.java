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
import java.io.IOException;
import java.nio.charset.Charset;

import shared.*;

public class Distributor {
	private DistributorConfiguration configuration = null;
	List<ServerInterface> calculationServers = null;
	
	public static void main(String[] args) {
		Distributor self = new Distributor();	

		try {
			self.process();
		} catch(IOException ioe) {

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
	
	private void process() throws IOException {
		Task fullTask = this.readOperations(this.configuration.getDataFilename());
		
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