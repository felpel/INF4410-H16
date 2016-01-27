package ca.polymtl.inf4410.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServerInterface extends Remote {
    String generateClientId() throws RemoteException;
    void create(String filename) throws RemoteException;
    List<FileInfo> list() throws RemoteException;
    List<FileInfo> syncLocalDir() throws RemoteException;
}
