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
	//private static final int NB_WORKERS = 3;
	private DistributorConfiguration configuration = null;
	private List<ServerInterface> calculationServers = null;
	//private ConcurrentLinkedQueue<Task> pendingTasks = null;

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
		//TODO Fix filename
		List<Task> tasks = this.readOperations("./donnees/" + this.configuration.getDataFilename());
		int result = this.processRecursive(tasks);
		Utilities.logInformation(String.format("Result: \t%d", result % 5000));
	}

	private int processRecursive(List<Task> tasks) {
		int result = 0;
		if (tasks == null || tasks.isEmpty()) {
			return result;
		}

		//ExecutorService executor = Executors.newFixedThreadPool(NB_WORKERS);

		//www.vogella.com/tutorials/JavaConcurrency/article.html#gainandissues
		//Executors javadoc
		//List<Future<int>>

		//When not secured, we need to ask all 3 servers for the results
		//TODO Take full task and divide it in multiple tasks.
		//TODO Send tasks (list of operations) to servers even though
		//we don't know each server's capacity.
		//TODO Manage servers' failures
		//TODO Show aggregated result

		List<Task> refusedTasks = new ArrayList<Task>();

		for (Task t : tasks) {
			try {
				result += this.calculationServers.get(0).process(t);
				Utilities.log(String.format("Partial result:\t%d", result));
			}
			catch (ServerTooBusyException stbe){
				Utilities.logError("Task was REFUSED!");
				refusedTasks.add(t);
			}
			catch (RemoteException re) {
				//TODO Probably notify distributor main of server failure
				//TODO verify if server timed out
			}
		}

		Utilities.logInformation(String.format("Processing [%d] refused task(s), if there are any...", refusedTasks.size()));
		return result + processRecursive(refusedTasks);
	}

	private List<Task> readOperations(String filename) throws IOException {
		List<Task> tasks = new ArrayList<Task>();

		Path filePath = Paths.get(filename);

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
