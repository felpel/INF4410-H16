package server;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.ConnectException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import shared.*;

public class Server implements ServerInterface {
	private final int MODULATOR = 5000;
	private ServerConfiguration configuration = null;
	private List<Task> pendingTasks = new ArrayList<Task>();
	private final Random RAND = new Random();

	public static void main(String[] args) {
		Server self = new Server();
	}

	public Server() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			this.loadConfiguration();
			this.loadServerInRegistry();
		} catch(IOException ioe) {

		}
		catch(Exception e) {}

	}

	private void loadConfiguration() throws IOException {
		this.loadConfiguration("srv-config.json");
	}

	private void loadConfiguration(String filename) throws IOException {
		this.configuration =
			Utilities.<ServerConfiguration>readJsonConfiguration(filename, ServerConfiguration.class);
	}

	private void loadServerInRegistry() {
		if (this.configuration == null) {
			//Maybe load default configuration instead?
			return;
		}

		ServerInterface stub = null;
		Registry registry = null;
		try {
        stub = (ServerInterface) UnicastRemoteObject
                        .exportObject(this, configuration.getPort());
        registry = LocateRegistry.getRegistry("127.0.0.1", 5000);
				String uniqueName = String.format("srv-%d", configuration.getPort());
        registry.rebind(uniqueName, stub);
        Utilities.logInformation("Server ready.");
				this.logServerInformation(uniqueName);

    } catch (ConnectException e) {
        System.err.println("Impossible to connect to the RMI registry." +
                           "Has rmiregistry [" + configuration.getPort() + "] been started ?");
        System.err.println();
        System.err.println("Error: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
    }
	}

	private void logServerInformation(String name) {
		Utilities.log(String.format("Name:\t\t%s", name));
		if (this.configuration != null) {
			Utilities.log(String.format("Port:\t\t%s", this.configuration.getPort()));
			if (this.configuration.getMischievious() == 0) {
					Utilities.log("Server is not mischievous (0%).");
			} else {
				Utilities.log(String.format("Mischievous:\t%d %%", this.configuration.getMischievious()));
			}

			Utilities.log(String.format("Capacity:\t%d task(s)", this.configuration.getCapacity()));
		}
	}

	//VERY IMPORTANT TO HANDLE REMOTE EXCEPTION ON CLIENT SIDE ("Pannes intempestives")
	public int process(Task task) throws RemoteException, ServerTooBusyException
	{
		if (!accepts(task)) {
			throw new ServerTooBusyException("Unable to treat task, server is too busy.");
		}

		if (isMischievious()) {
				//TODO Verify if we actually want to do this even when distributor is in secure mode
				//or if we simply set the mischievous rate to 0
				return RAND.nextInt(Integer.MAX_VALUE % 5000);
		}

		Utilities.logInformation("Task should be treated!");
		this.pendingTasks.add(task);

		//TODO Verify if it is actually what we want
		//TODO We can '//' the treatment between each servers
		int tempResult = 0;
		for (SubTask st : task.getSubTasks()) {
			Utilities.log(String.format("%s(%d)", st.getOperation(), st.getOperand())); //DEBUG
			switch (st.getOperation()) {
				case "fib":
					tempResult += Operations.fib(st.getOperand());
					tempResult %= this.MODULATOR;
				break;
				case "prime":
					tempResult += Operations.prime(st.getOperand());
					tempResult %= this.MODULATOR;
					break;
				default:
					Utilities.logError(String.format("Undefined operation '%s'", st.getOperation()));
					break;
			}
		}

		Utilities.logInformation("Task was treated succesfully! :)");
		Utilities.log(String.format("Result: %d", tempResult));

		this.pendingTasks.remove(task);

		return tempResult;
	}

	private boolean accepts(Task task) {
			int nbPendingSubTasks = 0;

			for (Task t : this.pendingTasks) {
				nbPendingSubTasks += t.getSubTasks().size();
			}

			int newSubTasks = task.getSubTasks().size();

			int serverCapacity = this.configuration.getCapacity();

			int serverLoad = 100 * nbPendingSubTasks / serverCapacity;

			System.out.println("========== NEW TASK ==========");
			String info = String.format("Pending subtasks:\t%d\n" +
																	"New subtasks:\t%d\nServer load:\t%d%%",
																	nbPendingSubTasks, newSubTasks, serverLoad);
			System.out.println(info);
			if (nbPendingSubTasks + newSubTasks <= serverCapacity)
				return true;

			//Verify if we have enough resources to treat the request (See "Simulation des ressources")
			int refusalRate = (newSubTasks - serverCapacity) * 100 / (9 * serverCapacity);
			int randomNumber = RAND.nextInt(101);

			Utilities.log(String.format("Refusal rate (T): %d %%\tRandom percent: %d %%", refusalRate, randomNumber));
			return randomNumber > refusalRate;
	}

	private boolean isMischievious() {
		//Take in account the mischievous property of the server (See "Serveur de calculs malicieux")
		int mischieviousRate = this.configuration.getMischievious();
		if (mischieviousRate == 0) {
			return false;
		}

		int randomNumber = RAND.nextInt(100);
		Utilities.log(String.format("Mischievious rate: %d %%\tRandom percent: %d %%", mischieviousRate, randomNumber));

		return randomNumber <= mischieviousRate;
	}
}
