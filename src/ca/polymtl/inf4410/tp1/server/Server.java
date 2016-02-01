package ca.polymtl.inf4410.tp1.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ca.polymtl.inf4410.tp1.shared.*;

public class Server implements ServerInterface {

    private List<FileInfo> m_files = null;
    private List<UUID> m_ids = null;

    public static void main(String[] args) {
            Server server = new Server();
            server.run();
    }

    public Server() {
        super();

        m_files = new ArrayList<FileInfo>();
        //TODO Maybe load dynamically the files in the server directory?
        //TODO Load client IDs
    }
    
    protected void finalize() {
        super.finalize();
        
        //TODO Save files (ids and file infos)
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
            System.err.println("Impossible de se connecter au registre RMI." + 
                               "Est-ce que rmiregistry est lancé ?");
            System.err.println();
            System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    @Override
    public String generateClientId() throws RemoteException {
        UUID newClientId = UUID.randomUUID();
        m_ids.add(newClientId);
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
        return this.list();
    }
    
    @Override
    public FileInfo get(String filename, String checksum) throws RemoteException {
        FileInfo serverFile = this.getFileOnServer(filename);
        
        String serverFileChecksum = Utilities.getChecksumFromBytes(serverFile.getContent());
        
        return !serverFileChecksum.equals(checksum) ?
            serverFile : 
            null;
    }
    
    @Override
    public FileInfo lock(String filename, UUID clientId, String checksum) throws RemoteException {
        FileInfo latestVersion = this.getFileOnServer(filename);
        
        String serverFileChecksum = Utilities.getChecksumFromBytes(latestVersion.getContent());
        
        if (latestVersion != null && 
            latestVersion.getLockedUser() != null && 
            !latestVersion.getLockedUser().equals(clientId))) {
            throw new RemoteException("Le fichier est verrouillé par un autre utilisateur");
        }
        
        //TODO Verify if same reference in m_files
        latestVersion.lock(clientId);
        
        return !serverFileChecksum.equals(checksum) ?
            latestVersion : 
            null;
    }
    
    private FileInfo getFileOnServer(String filename) throws RemoteException {
        FileInfo serverFile = null;
        for (FileInfo file : m_files){
            if (file.getName().equals(filename)){
                serverFile = file;
                break;
            }
        }
        
        if (serverFile == null) {
            throw new RemoteException("Le fichier demandé n'existe pas sur le serveur...");
        }
        
        return serverFile;
    }
}
