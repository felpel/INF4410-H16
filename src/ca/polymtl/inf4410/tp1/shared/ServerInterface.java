package ca.polymtl.inf4410.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public interface ServerInterface extends Remote {
    String generateClientId() throws RemoteException;
    List<FileInfo> list() throws RemoteException;
    List<FileInfo> syncLocalDir() throws RemoteException;
    void create(String filename) throws RemoteException;
    FileInfo get(String filename, String checksum) throws RemoteException;
    FileInfo lock(String filename, UUID clientId, String checksum) throws RemoteException;
    void push(String filename, byte[] content, UUID clientId) throws RemoteException;
}
