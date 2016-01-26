package ca.polymtl.inf4410.tp1.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ca.polymtl.inf4410.tp1.shared.ServerInterface;

public class Client {
	public static void main(String[] args) {
		String distantHostname = null;
		int exponent = 1;

		if (args.length > 0) {
                    try {
                        exponent = Integer.parseInt(args[0]);
                        
                        if (exponent < 1 || exponent > 7) {
                            System.out.println("Exponent must be between 1 and 7, actual value: " + exponent);
                            return;
                        }
                        
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        return;
                    }
            
                    if (args.length == 2) {  
                        distantHostname = args[1];
                    }
		}
		else {
                    System.out.println("Must at least provide one argument (exponent)");
                    return;
		}

		Client client = new Client(exponent, distantHostname);
		client.run();
	}

	FakeServer localServer = null; // Pour tester la latence d'un appel de
                                       // fonction normal.
	private ServerInterface localServerStub = null;
	private ServerInterface distantServerStub = null;
	private byte[] data = null;

	public Client(int exponent, String distantServerHostname) {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		localServer = new FakeServer();
		localServerStub = loadServerStub("127.0.0.1");

		if (distantServerHostname != null) {
			distantServerStub = loadServerStub(distantServerHostname);
		}
		
		int size = (int)Math.pow(10,exponent);
		data = new byte[size];
	}

	private void run() {
		appelNormal();

		if (localServerStub != null) {
			appelRMILocal();
		}

		if (distantServerStub != null) {
			appelRMIDistant();
		}
	}

	private ServerInterface loadServerStub(String hostname) {
		ServerInterface stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (ServerInterface) registry.lookup("server");
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage()
					+ "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	}

	private void appelNormal() {
		long start = System.nanoTime();
		localServer.execute(data);
		long end = System.nanoTime();

		System.out.println("Temps écoulé appel normal: " + (end - start)
				+ " ns");
	}

	private void appelRMILocal() {
		try {
			long start = System.nanoTime();
			localServerStub.execute(data);
			long end = System.nanoTime();

			System.out.println("Temps écoulé appel RMI local: " + (end - start)
					+ " ns");
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}
	}

	private void appelRMIDistant() {
		try {
			long start = System.nanoTime();
			distantServerStub.execute(data);
			long end = System.nanoTime();

			System.out.println("Temps écoulé appel RMI distant: "
					+ (end - start) + " ns");
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}
	}
}
