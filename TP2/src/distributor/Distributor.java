package distributor;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import shared.*;

public abstract class Distributor {
	public final static int RMI_REGISTRY_PORT = 5000;
	
	protected DistributorConfiguration configuration = null;
	protected List<ServerInterface> calculationServers = null;
	protected Queue<Task> pendingTasks = null;
	protected Queue<Task> doneTasks = null;
	protected Queue<int> results = null;
	protected int nbTasks = 0;

	public Distributor() {
		this.calculationServers = new ArrayList<ServerInterface>();
		this.pendingTasks = new ConcurrentLinkedQueue<Task>();
		this.doneTasks = new ConcurrentLinkedQueue<Task>();
		this.results = new ConcurrentLinkedQueue<int>();	
	}
	
	public final void initialize(DistributorConfiguration configuration) throws IllegalArgumentException {
		if (configuration == null) {
			throw new IllegalArgumentException("Configuration should not be null");
		}
		
		this.configuration = configuration;
		
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		
		this.loadServerStubs();
	}

	private final void loadServerStubs() {
		if (this.configuration == null) {
			//TODO Maybe load default configuration?
			return;
		}

		for (ServerInformation serverInfo : this.configuration.getServers()) {
			ServerInterface stub = this.loadServerStub(serverInfo);
			if (stub != null) {
				this.calculationServers.add(stub);
			}
		}
	}

	private final ServerInterface loadServerStub(ServerInformation serverInfo) {
		if (serverInfo == null) {
			throw new IllegalArgumentException();
		}
		
		String host = serverInfo.getHost();
		if (host == null || host.trim().isEmpty()) {
			Utilities.logError("Unable to load server stub since the host is empty");
			return null;
		}
		
		int port = serverInfo.getPort();
		
		if (port < 5000 || port > 5050) {
			Utilities.logError("Unable to load server stub since the port is not between the range [5000, 5050]");
			return null;
		}
		
		ServerInterface stub = null;
		try {
			Registry registry = LocateRegistry.getRegistry(host, RMI_REGISTRY_PORT);
			String uniqueName = String.format("srv-%d", port);
			stub = (ServerInterface) registry.lookup(uniqueName);
		} catch (NotBoundException e) {
			System.out.println("Error: The name '" + e.getMessage() + "' is not defined in the registry.");
		} catch (AccessException e) {
			System.out.println("Error: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Error: " + e.getMessage());
		}

		return stub;
	}

	private final void readOperations(String filename) throws IOException {
		Path filePath = Paths.get(filename);

		if (!Files.exists(filePath)) {
			throw new FileNotFoundException();
		}

		Charset cs = Charset.forName("utf-8");
		List<String> instructions = Files.readAllLines(filePath, cs);

		//TODO Remove when we do benchmarks
		java.util.Collections.shuffle(instructions);

		for (int i = 0; i < instructions.size(); i += configuration.getBatchSize()) {
			Task task = new Task(i / configuration.getBatchSize());
			for(int j = i; j < (this.configuration.getBatchSize() + i) && j < instructions.size(); j++) {
				String instruction = instructions.get(j);
				String[] instructionElements = instruction.split(" ");
				String operation = instructionElements[0];
				int operand = Integer.parseInt(instructionElements[1]);
				task.addSubTask(operation, operand);
			}
			this.pendingTasks.add(task);
		}
	}
	
	public void process() throws IOException {
		//When not secured, we need to ask all 3 servers for the results
		//TODO Take full task and divide it in multiple tasks.
		//TODO Send tasks (list of operations) to servers even though
		//we don't know each server's capacity.
		//TODO Manage servers' failures
		//TODO Show aggregated result

		//TODO Check if we need other pre-conditions
		if (this.calculationServers == null || this.calculationServers.isEmpty()) {
			Utilities.logError("Can't calculate result since no servers were available...");
			return;
		}

		//TODO Fix filename
		this.readOperations("./donnees/" + this.configuration.getDataFilename());
		
		if (this.pendingTasks != null) {
			this.nbTasks = this.pendingTasks.size();
		}
	}
	
	public final void getFinalResult() {
		// !-- All tasks must be completed to get an appropriate result.
		if (this.results == null || this.results.isEmpty()) {
			Utilities.logError("Unable to get the final result since no results were provided.");
			return;
		}
		
		if (!this.pendingTasks.isEmpty() || this.doneTasks.size() != nbTasks) {
			Utilities.logError("Unable to get the correct results because some tasks were not treated.");
			return;
		}

		//Sum all results and apply modulo 5000
		int finalResult = 0;
		for (int result : this.results) {
			finalResult += result;
		}

		Utilities.logInformation(String.format("Result = %d", finalResult % 5000));
	}
}
