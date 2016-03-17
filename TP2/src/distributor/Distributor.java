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
import java.util.concurrent.atomic.AtomicInteger;

import shared.*;

public abstract class Distributor {
	protected DistributorConfiguration configuration = null;
	protected List<ServerInterface> calculationServers = null;
	protected Queue<Operation> pendingOperations = null;
	protected Queue<Task> doneTasks = null;
	protected Queue<Integer> results = null;
	protected AtomicInteger nbTasksTried = null;
	protected int nbOperations = 0;

	public Distributor() {
		this.calculationServers = new ArrayList<ServerInterface>();
		this.pendingOperations = new ConcurrentLinkedQueue<Operation>();
		this.doneTasks = new ConcurrentLinkedQueue<Task>();
		this.results = new ConcurrentLinkedQueue<Integer>();
		this.nbTasksTried = new AtomicInteger(0);
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

		//TODO Check if we need other pre-conditions
		if (this.calculationServers == null || this.calculationServers.isEmpty()) {
			Utilities.logError("Can't calculate result since no servers were available...");
			return;
		}

		//TODO Fix filename
		try {
			this.readOperations("./donnees/" + this.configuration.getDataFilename());
		}
		catch (IOException ioe){
			Utilities.logError("Unable to read the distributor's configuration...");
			return;
		}
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

		if (port < Constants.SERVER_MIN_PORT || port > Constants.SERVER_MAX_PORT) {
			Utilities.logError("Unable to load server stub since the port is not between the range [5000, 5050]");
			return null;
		}

		ServerInterface stub = null;
		try {
			Registry registry = LocateRegistry.getRegistry(host, Constants.RMI_REGISTRY_PORT);
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

		for (String instruction : instructions) {
			String[] instructionElements = instruction.split(" ");
			String function = instructionElements[0];
			int operand = Integer.parseInt(instructionElements[1]);
			this.pendingOperations.add(new Operation(function, operand));
		}

		if (this.pendingOperations != null) {
			this.nbOperations = this.pendingOperations.size();
		}
	}

	public abstract void process();

	public final void showFinalResult() {
		// !-- All tasks must be completed to get an appropriate result.
		if (this.results == null || this.results.isEmpty()) {
			Utilities.logError("Unable to get the final result since no results were provided.");
			return;
		}

		int doneOperations = 0;
		for (Task t : doneTasks) {
			doneOperations += t.getOperations().size();
		}

		if (!this.pendingOperations.isEmpty() || doneOperations != nbOperations) {
			Utilities.logError("Unable to get the correct results because some operations were not treated.");
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
