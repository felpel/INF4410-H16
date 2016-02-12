package ca.polymtl.inf4410.tp1.client;

import ca.polymtl.inf4410.tp1.shared.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Client {
    public final String CLIENT_ID = "clientId";
    
    private ServerInterface m_distantServerStub = null;
    private UUID m_clientId = null;
    private String m_workingDirectory = null;

    public static void main(String[] args) {
        Client client = new Client();
        Scanner scanner = new Scanner(System.in);
        String command = "";
        String[] tokens = null;
        boolean exit = false;
        
        client.showOptions();
        
        while(!exit) {
            System.out.print("> ");
            command = scanner.nextLine();
            tokens = command.split(" ");
            
            switch (tokens.length) {
                case 1:
                {
                    if (tokens[0].equals("list")) {
                        client.list();
                    }
                    else if (tokens[0].equals("syncLocalDir")) {
                        client.syncLocalDir();
                    }
                    else if (tokens[0].equals("options")) {
                        client.showOptions();
                    }
                    else if (tokens[0].equals("clear")) {
                        System.out.print("\033[H\033[2J");
                    }
                    else if (tokens[0].equals("exit")) {
                        System.out.println("Au revoir! @ la prochaine");
                        exit = true;
                    }
                }
                break;
                case 2:
                {
                    if (tokens[0].equals("create")) {
                        client.create(tokens[1]);
                    }
                    else if (tokens[0].equals("get")) {
                        client.get(tokens[1]);
                    }
                    else if (tokens[0].equals("lock")) {
                        client.lock(tokens[1]);
                    }
                    else if (tokens[0].equals("push")) {
                        client.push(tokens[1]);
                    }
                    else if (tokens[0].equals("cat")) {
                        client.showContent(tokens[1]);
                    }
                }
                break;
                case 3:
                {
                    if (tokens[0].equals("concat")) {
                        client.concat(tokens[1], tokens[2]);
                    }
                }
                break;
                default:
                    System.err.println("Les arguments fournis au programme sont invalides.");
                    break;
            }
            System.out.println();
        }
        scanner.close();
    }

    public Client() {
        super();

        m_workingDirectory = this.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
        m_workingDirectory = m_workingDirectory.substring("file:".length(), m_workingDirectory.lastIndexOf("/") + 1);

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        m_distantServerStub = loadServerStub("127.0.0.1");

        generateClientId();
    }

    private void showOptions() {
        System.out.println("Bonjour, voici la liste des options : ");
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("*\toptions : Affiche la liste des commandes disponibles");
        System.out.println("*\tget (filename) : recupere un fichier sur le serveur");
        System.out.println("*\tcreate (filename) : creer un fichier sur le serveur");
        System.out.println("*\tlist : Retourne la liste des fichiers sur le serveur");
        System.out.println("*\tsyncLocalDir : Synchronisation des fichiers locaux avec ceux du serveur");
        System.out.println("*\tlock (filename) : Verrouille le fichier en vue d'une modification future");
        System.out.println("*\tpush (filename) : Envoie une nouvelle version du fichier sur le serveur");
        System.out.println("*\tclear : Vide l'affichage de la console");
        System.out.println("*\texit : Quitte l'application");
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

    private void printError(Exception e) {
        if (e.getCause() == null) {
            System.out.println(e.getMessage());
            return;
        }
    
        String errorMessage = e.getCause().toString();
        System.out.println(errorMessage.substring(errorMessage.indexOf(":") + 1).trim());
    }
    
    private void generateClientId() {
        try {
            m_clientId = UUID.fromString(m_distantServerStub.generateClientId());
            
            System.out.println(String.format("*** Nouveau client enregistré, votre identifiant: %s ***", m_clientId));
            
            Path clientDir = Paths.get(m_workingDirectory, m_clientId.toString());
          
            if (!Files.exists(clientDir)) {
                Files.createDirectory(clientDir);
            }
            
            m_workingDirectory = clientDir.toString();
            
            //Save client id to local directory
            Path filePath = Paths.get(m_workingDirectory, CLIENT_ID);
            
            if (!Files.exists(filePath)) { 
                Files.createFile(filePath);
            }
            
            Files.write(filePath, m_clientId.toString().getBytes(), StandardOpenOption.WRITE); 
        } catch (Exception e) {
            e.printStackTrace();
            this.printError(e);
        }
    }

    private void create(String filename) {
        try {
            m_distantServerStub.create(filename);
            System.out.println(String.format("%s ajouté.", filename));
        } catch (Exception e) {
            this.printError(e);
        }
    }

    private void list() {
        try {
            List<FileInfo> files = m_distantServerStub.list();

            for(FileInfo file : files){                
                String status = "non vérouillé";
                
                if (file.getLockedUser() != null) {
                    status = String.format("vérouillé par %s", file.getLockedUser().equals(m_clientId) 
                                            ? "(moi)"
                                            : file.getLockedUser().toString());
                }
                
                System.out.println(String.format("* %s\t%s", file.getName(), status));
            }

            System.out.println(String.format("%d fichier(s)", files.size()));
        } catch (Exception e) {
            this.printError(e);
        }
    }

    private void syncLocalDir() {
        try {
            List<FileInfo> files = m_distantServerStub.syncLocalDir();

            File dir = new File(m_workingDirectory);

            File[] allFiles = dir.listFiles();
            if (allFiles != null) {
                for (File file : allFiles) {
                    if (!file.getPath().contains(".jar") && !file.getName().endsWith(CLIENT_ID))
                    {
                        file.delete();
                    }
                }
            }
            final byte[] noData = new byte[0];
            for (FileInfo file : files){
                byte[] data = file.getContent() != null ? file.getContent() : noData;
                Files.write(Paths.get(m_workingDirectory, file.getName()), data);
            }
            System.out.println("Repertoire local synchronise");
        } catch (Exception e) {
            this.printError(e);
        }
    }

    private void get(String filename) {
        try {
            Path filePath = Paths.get(m_workingDirectory, filename);
            String checksum = Utilities.getChecksumFromFile(filePath.toString());
            
            FileInfo file = m_distantServerStub.get(filename, checksum);
            if (file != null) {
                
                if (!Files.exists(filePath)) {
                    Files.createFile(filePath);
                } 
                
                Files.write(filePath, file.getContent(), StandardOpenOption.WRITE);
                System.out.println(String.format("%s synchronisé.", filename));
            } else {
                System.out.println("Fichier déjà synchronisé");
            }
        } catch (Exception e) {
            this.printError(e);
        }
    }

    private void lock(String filename) {
        try {
            Path filePath = Paths.get(m_workingDirectory, filename);
            String checksum = Utilities.getChecksumFromFile(filePath.toString());
            
            FileInfo file = m_distantServerStub.lock(filename, m_clientId, checksum);
            if (file != null) {
                Files.write(filePath, file.getContent());
                System.out.println(String.format("%s vérouillé.", filename));
            } else {
                System.out.println("Le fichier est déjà vérouillé et à jour.");
            }
        } catch (Exception e) {
            this.printError(e);
        }
    }

    private void push(String filename) {
        try {
            Path filePath = Paths.get(m_workingDirectory, filename);
            
            if (!Files.exists(filePath)) {
                System.out.println("Operation refusee : le fichier est introuvable sur le client.");
                return;
            }
            
            byte[] data = Files.readAllBytes(filePath);
            m_distantServerStub.push(filename, data, m_clientId);
            System.out.println(String.format("%s a été envoyé au serveur", filename));
        } catch (Exception e) {
            this.printError(e);
        }
    }

    // Debugging purpose
    private void showContent(String filename) {
        try {            
            byte[] data = m_distantServerStub.cat(filename);

            System.out.println(String.format("* %s (côté serveur)", filename));
            System.out.println("********************************************************");

            if (data != null && data.length != 0) {
                System.out.println(new String(data, "UTF-8"));   
            } else {
                System.out.println("(Fichier vide)");
            }
        } catch (Exception e) {
            this.printError(e);
        }
    }

    // Debugging purpose
    private void concat(String filename, String content) {
        if (filename == null || content == null || content.isEmpty()) {
            return;
        }

        try {
            Files.write(Paths.get(m_workingDirectory, filename), content.getBytes(), StandardOpenOption.APPEND);
            System.out.println(filename + " mis a jour.");
        } catch (Exception e) {
            this.printError(e);
        }
    }
}
