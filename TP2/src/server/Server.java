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

import shared.*;

public class Server implements ServerInterface {
	private final int MODULATOR = 5000;	
	private ServerConfiguration serverConfiguration = null;
	
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
		this.serverConfiguration = 
			Utilities.<ServerConfiguration>readJsonConfiguration(filename, ServerConfiguration.class);
	}
	
	private void loadServerInRegistry() {
		if (this.serverConfiguration == null) {
			//Maybe load default configuration instead?
			return;
		}
		
		try {
            ServerInterface stub = (ServerInterface) UnicastRemoteObject
                            .exportObject(this, serverConfiguration.getPort());

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("server", stub);
            System.out.println("Server ready.");
        } catch (ConnectException e) {
            System.err.println("Impossible to connect to the RMI registry." + 
                               "Has rmiregistry [" + serverConfiguration.getPort() + "] been started ?");
            System.err.println();
            System.err.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
	}
	
	//VERY IMPORTANT TO HANDLE REMOTE EXCEPTION ON CLIENT SIDE ("Pannes intempestives")
	public int process(Task task) throws RemoteException 
	{
		//TODO Verify if we have enough resources to treat the request (See "Simulation des ressources")
		//TODO Take in account the mischievous property of the server (See "Serveur de calculs malicieux")
		//TODO Verify if it is actually what we want
		//TODO We can '//' the treatment between each servers
		int tempResult = 0;
		for (SubTask st : task.getSubTasks()) {
			System.out.println(String.format("%s(%d)", st.getOperation(), st.getOperand())); //DEBUG
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
					System.err.println(String.format("Undefined operation '%s'", st.getOperation()));
					break;
			}
		}
		
		return tempResult;
	}
}
