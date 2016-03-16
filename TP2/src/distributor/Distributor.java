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
import java.util.Queue;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import java.io.IOException;
import java.nio.charset.Charset;

import shared.*;

public class Distributor {
	//private static final int NB_WORKERS = 3;
	private DistributorConfiguration configuration = null;
	private List<ServerInterface> calculationServers = null;
	private Queue<Task> pendingTasks = null;
	private Queue<Task> doneTasks = null;
	private AtomicInteger result = null;

	public static void main(String[] args) {
		Distributor self = new Distributor();

		try {
			self.process();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public Distributor() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}


		try {
			loadConfiguration();
			loadServerStubs();
			this.pendingTasks = new ConcurrentLinkedQueue<Task>();
			this.doneTasks = new ConcurrentLinkedQueue<Task>();
			this.result = new AtomicInteger(0);
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
			if (stub != null) {
				this.calculationServers.add(stub);
			}
		}
	}

	private ServerInterface loadServerStub(ServerInformation serverInfo) {
		ServerInterface stub = null;
    try {
        Registry registry = LocateRegistry.getRegistry(serverInfo.getHost(), 5000);
        String uniqueName = String.format("srv-%d", serverInfo.getPort());
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

	private void process() throws IOException {
		//When not secured, we need to ask all 3 servers for the results
		//TODO Take full task and divide it in multiple tasks.
		//TODO Send tasks (list of operations) to servers even though
		//we don't know each server's capacity.
		//TODO Manage servers' failures
		//TODO Show aggregated result

		//TODO Check if we need other pre-conditions
		if (this.calculationServers == null || this.calculationServers.isEmpty()) {
			return;
		}

		//TODO Fix filename
		this.readOperations("./donnees/" + this.configuration.getDataFilename());
		int nbTasks = this.pendingTasks.size();

		ExecutorService executor = Executors.newFixedThreadPool(this.calculationServers.size());

		int workerCounter = 0;
		for (ServerInterface serverStub : this.calculationServers) {
			DistributorWorker worker = new DistributorWorker(this.pendingTasks, this.doneTasks, serverStub, this.result, workerCounter++);
			executor.execute(worker);
		}

		//Wait for executor's full shutdown
		executor.shutdown();
		while(!executor.isTerminated()) {
		}

		// All tasks must be completed to get an appropriate result.
		if (!this.pendingTasks.isEmpty() || this.doneTasks.size() != nbTasks) {
			Utilities.log("Unable to get the correct results because some tasks were not treated.");
			//TODO Log error
			return;
		}

		this.result.set(this.result.get() % 5000);

		Utilities.logInformation(String.format("Result = %d", this.result.get()));
	}

	private void readOperations(String filename) throws IOException {
		Path filePath = Paths.get(filename);

		if (!Files.exists(filePath)) {
			throw new FileNotFoundException();
		}

		Charset cs = Charset.forName("utf-8");
		List<String> instructions = Files.readAllLines(filePath, cs);

		//TODO Remove when we do benchmarks
		Collections.shuffle(instructions);

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
}
