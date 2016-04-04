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
	private final Random RAND = new Random();

	public static void main(String[] args) {
		if (args.length == 0) {
			Utilities.logError("Server ID was not provided.");
			return;
		}

		Server self = new Server(Integer.parseInt(args[0]));
	}

	public Server(int id) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			//We try to load the configuration based on the ID
			this.loadConfiguration(id);
			this.loadServerInRegistry();
		} catch(IOException ioe) {

		}
		catch(Exception e) {}

	}

	//Format the ID into the string for the filename
	private void loadConfiguration(int id) throws IOException {
		this.loadConfiguration(String.format("srv-config-%d.json", id));
	}

	//Assign the config returned by ReadJsonConfiguration
	private void loadConfiguration(String filename) throws IOException {
		this.configuration =
			Utilities.<ServerConfiguration>readJsonConfiguration(filename, ServerConfiguration.class);
	}

	//Locate the registry then export this server
	private void loadServerInRegistry() {
		if (this.configuration == null) {
			return;
		}

		ServerInterface stub = null;
		Registry registry = null;
		try {
      stub = (ServerInterface) UnicastRemoteObject
                      .exportObject(this, configuration.getPort());
      registry = LocateRegistry.getRegistry(configuration.getHost(), Constants.RMI_REGISTRY_PORT);
      String uniqueName = String.format("srv-%d", configuration.getPort());
      registry.rebind(uniqueName, stub);
      Utilities.logInformation("Server ready.");
      this.logServerInformation(uniqueName);

	  } catch (ConnectException e) {
	      System.err.println("Impossible to connect to the RMI registry." +
	                      "Has rmiregistry [" + Constants.RMI_REGISTRY_PORT + "] been started ?");
	      System.err.println();
	      System.err.println("Error: " + e.getMessage());
	  } catch (Exception e) {
	      System.err.println("Error: " + e.getMessage());
	  }
	}

	private void logServerInformation(String name) {
		Utilities.log("=============================================");
		Utilities.log(String.format("Name:\t\t%s", name));
		if (this.configuration != null) {
                        Utilities.log(String.format("Host:\t\t%s", this.configuration.getHost()));
			Utilities.log(String.format("Port:\t\t%s", this.configuration.getPort()));
			if (this.configuration.getMischievious() == 0) {
					Utilities.log("Server is not mischievous (0%).");
			} else {
				Utilities.log(String.format("Mischievous:\t%d %%", this.configuration.getMischievious()));
			}

			Utilities.log(String.format("Capacity:\t%d task(s)", this.configuration.getCapacity()));
		} else {
			Utilities.logError("Configuration is missing.");
		}
		Utilities.log("=============================================\n");
	}

	//Main function for processing a task
	public int process(Task task) throws RemoteException, ServerTooBusyException
	{
		Utilities.log("Received new task!\n" + task.toString());
		if (!accepts(task)) {
			Utilities.logInformation("Task was refused :(");
			throw new ServerTooBusyException("Unable to treat task, server is too busy.");
		}

		if (isMischievious()) {
				Utilities.logInformation("Mischievous activity detected :>\n");
				int rndm = RAND.nextInt(Integer.MAX_VALUE % 5000);
				Utilities.logInformation("Mischievous result is  : " + rndm + "\n");
				return rndm;
		}

		Utilities.logInformation("Processing task!");

		//Calculate the actual answers to the operations in the task passed by param
		int tempResult = 0;
		for (Operation op : task.getOperations()) {
			Utilities.logInformation(op.toString());
			switch (op.getFunction()) {
				case "fib":
					tempResult += Operations.fib(op.getOperand());
					tempResult %= this.MODULATOR;
				break;
				case "prime":
					tempResult += Operations.prime(op.getOperand());
					tempResult %= this.MODULATOR;
					break;
				default:
					Utilities.logError(String.format("Undefined function '%s'", op.getFunction()));
					break;
			}
		}

		Utilities.logInformation("Task done :)");
		Utilities.log(String.format("Result: %d\n", tempResult));

		return tempResult;
	}

	//Function to simulate refusal if the task's size is greater than the server's capacity
	private boolean accepts(Task task) {
			int newOperations = task.getOperations().size();

			if (newOperations > Constants.MAX_OPERATIONS_PER_TASK) {
				Utilities.logError(String.format("%s -> Impossible to treat task with > 100 operations", task.toString()));
				return false;
			}

			int serverCapacity = this.configuration.getCapacity();

			if (newOperations <= serverCapacity)
				return true;

			//Verify if we have enough resources to treat the request (See "Simulation des ressources")
			int refusalRate = (newOperations - serverCapacity) * 100 / (9 * serverCapacity);
			int randomNumber = RAND.nextInt(101);

			Utilities.log(String.format("Refusal rate (T): %d %%\tRandom percent: %d %%", refusalRate, randomNumber));
			return randomNumber > refusalRate;
	}

	//Function to simulate malicious activity if the config is appropriate
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
