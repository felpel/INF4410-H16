package ca.polymtl.inf4410.tp1.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ca.polymtl.inf4410.tp1.shared.FileInfo;
import ca.polymtl.inf4410.tp1.shared.ServerInterface;

public class Server implements ServerInterface {

    private List<FileInfo> m_files = null;

	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	public Server() {
		super();

        m_files = new ArrayList<FileInfo>();
        //TODO Maybe load dynamically the files in the server directory?
	}

	private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			ServerInterface stub = (ServerInterface) UnicastRemoteObject
					.exportObject(this, 0);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("server", stub);
			System.out.println("Server ready.");
		} catch (ConnectException e) {
			System.err
					.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

    @Override
    public String generateClientId() throws RemoteException {
        UUID newClientId = UUID.randomUUID();
        return newClientId.toString();
    }

    @Override
    public void create(String filename) throws RemoteException {
        for (FileInfo file : m_files){
            if (file.getName().equals(filename)){
                throw new RemoteException(
                        String.format("%s existe déjà sur le serveur.", filename)
                );
            }
        }

        m_files.add(new FileInfo(filename));
    }

    //TODO Verify that list() & syncLocalDir() REALLY have the same logic
    @Override
    public List<FileInfo> list() throws RemoteException {
        if (m_files == null) {
           throw new RemoteException("Impossible de recuperer la liste des fichiers " +
                                     "sur le serveur...");
        }

        return m_files;
    }

    @Override
    public List<FileInfo> syncLocalDir() throws RemoteException {
        if (m_files == null) {
            throw new RemoteException("Impossible d'effectuer une synchronisation " +
                                      "puisqu'il n'y a aucun fichier sur le serveur...");
        }

        return m_files;
    }
}
