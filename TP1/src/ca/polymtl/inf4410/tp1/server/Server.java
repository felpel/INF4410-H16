package ca.polymtl.inf4410.tp1.server;

import ca.polymtl.inf4410.tp1.shared.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.nio.charset.Charset;
import java.util.Collections;

public class Server implements ServerInterface {
    
    private List<FileInfo> m_files = null;
    private List<UUID> m_ids = null;
    private String m_workingDirectory = null;

    public static void main(String[] args) {
            Server server = new Server();
            server.run();
    }

    public Server() {
        super();

        m_workingDirectory =  this.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
        m_workingDirectory = m_workingDirectory.substring("file:".length(), m_workingDirectory.lastIndexOf("/") + 1);

        m_files = new ArrayList<>();
        m_ids = new ArrayList<>();
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
           throw new RemoteException("Impossible de recuperer la liste des fichiers sur le serveur...");
        }

        Collections.sort(m_files);
        
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
        
        if (latestVersion.getLockedUser() != null && !latestVersion.getLockedUser().equals(clientId)) {
            throw new RemoteException("Operation refusee : le fichier est verrouillé par un autre utilisateur");
        }

        latestVersion.lock(clientId);

        String serverFileChecksum = Utilities.getChecksumFromBytes(latestVersion.getContent());

        return !serverFileChecksum.equals(checksum) ?
            latestVersion : 
            null;
    }

    @Override
    public void push(String filename, byte[] content, UUID clientId) throws RemoteException{
        FileInfo latestVersion = this.getFileOnServer(filename);

        if (latestVersion.getLockedUser() == null) {
            throw new RemoteException("Operation refusee: vous devez d'abord verouiller le fichier");
        }

        if (!latestVersion.getLockedUser().equals(clientId)) {
            throw new RemoteException("Operation refusee: le fichier est deja verouille par un autre utilisateur");
        }

        latestVersion.setContent(content);
        latestVersion.unlock();
    }

    @Override
    public byte[] cat(String filename) throws RemoteException {
        FileInfo file = this.getFileOnServer(filename);
        return file.getContent();
    }
    
    private FileInfo getFileOnServer(String filename) throws RemoteException {
        for (FileInfo file : m_files){
            if (file.getName().equals(filename)){
                return file;
            }
        }

        throw new RemoteException("Le fichier demandé n'existe pas sur le serveur...");
    }
}
