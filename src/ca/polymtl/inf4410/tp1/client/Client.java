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
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Client {

    public static void main(String[] args) {
        Client client = new Client();

	switch (args.length) {
	  case 1:
	    {
		if (args[0].equals("list")) {
		    client.list();
		}
		if (args[0].equals("syncLocalDir")) {
		    client.syncLocalDir();
		}
	    }
	    break;
	  case 2:
	    {
		if (args[0].equals("create")) {
		    client.create(args[1]);
		}

		if (args[0].equals("get")) {
		    client.get(args[1]);
		}

		if (args[0].equals("lock")) {
		    client.lock(args[1]);
		}

		if (args[0].equals("push")) {
		    client.push(args[1]);
		}
	    }
	    break;
	  default:
	    System.err.println("Les arguments fournis au programme sont invalides.");
	    break;
	}
    }

    private ServerInterface m_distantServerStub = null;
    private UUID m_clientId = null;
    private String m_workingDirectory = null;

    public Client() {
        super();

        //TODO verify this
        try {
            m_workingDirectory = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().toString();
            m_workingDirectory = m_workingDirectory.substring("file:".length(), m_workingDirectory.lastIndexOf("/"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        //TODO Verify how we setup the host (maybe add another command?)
        m_distantServerStub = loadServerStub("127.0.0.1");

        Scanner scanner = null;
        //Generate client ID if it doesn't exist locally
        try {
            //Load client id from file
            File clientIdFile = new File(String.format("%s/%s", m_workingDirectory, "clientId"));
            scanner = new Scanner(clientIdFile);
            m_clientId = UUID.fromString(scanner.nextLine());
            
        } catch (IOException mue) {
            System.err.println("Impossible de trouver le fichier contenant l'identifiant du client...");
            mue.printStackTrace();
        } catch (Exception ex) {
	    ex.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        if (m_clientId == null){
            generateClientId();
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

    /*  generateclientid()

        Génère un identifiant unique pour le client. Celui-ci
        est sauvegardé dans un fichier local et est
        retransmis au serveur lors de l'appel à lock() ou
        push(). Cette méthode est destinée à être
        appelée par l'application client lorsque nécessaire
        (il n'y a pas de commande generateclientid
        visible à l'utilisateur).
    */
    private void generateClientId() {
        PrintWriter writer = null;
        try {
            m_clientId = UUID.fromString(m_distantServerStub.generateClientId());
            
            System.out.println(String.format("*** Nouveau client enregistré, votre identifiant: %s ***", m_clientId));
            //Save client id to local directory
            File clientIdFile = new File(String.format("%s/%s", m_workingDirectory, "clientId"));
            writer = new PrintWriter(clientIdFile);
            writer.print(m_clientId.toString());

        } catch (RemoteException e) {
            System.err.println("Echec pour recuperer l'identifiant du client sur le serveur");
            e.printStackTrace();
        } catch (FileNotFoundException fnfe) {
            System.err.println("Impossible d'ouvrir le fichier clientId...");
            fnfe.printStackTrace();
        } catch (Exception e) {
	    e.printStackTrace();
        }  
        finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /*  create(nom)

        Crée un fichier vide sur le serveur avec le nom
        spécifié. Si un fichier portant ce nom existe déjà,
        l'opération échoue.
    */
    private void create(String filename) {
        try {
            m_distantServerStub.create(filename);
            System.out.println(String.format("%s ajouté.", filename));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /*  list()
        Retourne la liste des fichiers présents sur le
        serveur. Pour chaque fichier, le nom et l'identifiant
        du client possédant le verrou (le cas échéant) est
        retourné.
    */
    private void list() {
        try {
            List<FileInfo> files = m_distantServerStub.list();

            for(FileInfo file : files){                
                String status = file.getLockedUser() != null ?
                    String.format("vérouillé par %s", file.getLockedUser().toString()) :
                    "non vérouillé";

                System.out.println(String.format("* %s\t%s", file.getName(), status));
            }

            System.out.println(String.format("%d fichier(s)", files.size()));
        } catch (RemoteException e){
            e.printStackTrace();
        }
    }

    /*  syncLocalDir()

        Permet de récupérer les noms et les contenus de
        tous les fichiers du serveur.
        Le client appelle cette fonction pour synchroniser
        son répertoire local avec celui du serveur. Les
        fichiers existants seront écrasés et remplacés par
        les versions du le serveur.
    */
    private void syncLocalDir() {
        try {
            List<FileInfo> files = m_distantServerStub.syncLocalDir();

            File dir = new File(m_workingDirectory);

            File[] allFiles = dir.listFiles();
            if (allFiles != null) {
                for (File file : allFiles) {
                    if (!file.getPath().contains(".jar"))
                    {
                        file.delete();
                    }
                }
            }

            for (FileInfo file : files){
                Files.write(Paths.get(m_workingDirectory + file.getName()), file.getContent());
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /*  get(nom, checksum)

        Demande au serveur d'envoyer la dernière
        version du fichier spécifié. Le client passe
        également la somme de contrôle du fichier qu'il
        possède. Si le client possède la même somme de
        contrôle que celle qui est présente sur le serveur,
        celui-ci doit retourner une valeur nulle au client
        pour lui indiquer que son fichier est à jour et éviter
        un transfert inutile. Si le client ne possède pas
        encore ce fichier, il doit spécifier une somme de
        contrôle de -1 pour forcer le serveur à lui envoyer
        le fichier.

        Le fichier est écrit dans le répertoire local courant.
    */
    private void get(String filename) {
        try {
            String filepath = m_workingDirectory + filename;
            String checksum = Utilities.getChecksumFromFile(filepath);
            
            FileInfo file = m_distantServerStub.get(filename, checksum);
            if (file != null) {
                Path lastVersion = Paths.get(filepath);
                Files.write(lastVersion, file.getContent());
                System.out.println(String.format("%s synchronisé.", filename));
            } else {
                System.out.println("Fichier déjà synchronisé");
            }
        } catch (RemoteException re) {
            re.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /*  lock(nom, clientid, checksum)

        Demande au serveur de verrouiller le fichier
        spécifié. La dernière version du fichier est écrite
        dans le répertoire local courant ( la somme de
        contrôle est aussi utilisée pour éviter un transfert
        inutile) . L'opération échoue si le fichier est déjà
        verrouillé par un autre client.
    */
    private void lock(String filename) {
        try {
            String filepath = m_workingDirectory + filename;
            String checksum = Utilities.getChecksumFromFile(filepath);
            
            FileInfo file = m_distantServerStub.lock(filename, m_clientId, checksum);
            if (file != null) {
                Path lastVersion = Paths.get(filepath);
                Files.write(lastVersion, file.getContent());
                System.out.println(String.format("%s vérouillé.", filename));
            } else {
                System.out.println("Le fichier est déjà vérouillé et à jour.");
            }
        } catch (RemoteException re) {
            re.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /*  push(nom, contenu, clientid)

        Envoie une nouvelle version du fichier spécifié au
        serveur. L'opération échoue si le fichier n'avait pas
        été verrouillé par le client préalablement. Si le
        push réussit, le contenu envoyé par le client
        remplace le contenu qui était sur le serveur
        auparavant et le fichier est déverrouillé.
    */
    private void push(String filename) {
        try {
            byte[] data = Files.readAllBytes(Paths.get(m_workingDirectory, filename));
            m_distantServerStub.push(filename, data, m_clientId);
            System.out.println(String.format("%s a été envoyé au serveur", filename));
        } catch (RemoteException re) {
	    System.out.println("lol");
	    re.getMessage();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
	    e.printStackTrace();
        }
    }
}
