package server;

import com.google.gson.Gson;

public class ServerConfiguration {
	private int capacity; // voir p.5 ("Sim. des ressources")
	private int mischievious; // voir p.5 ("Serveur de calcul malicieux")
	private int port; // voir p.8 ("Conseil pour le travail")
	private String host;
	
	public void setCapacity(int capacity) {
		//TODO Maybe verify if between 0 and 10 (see specs)
		this.capacity = capacity;
	}
	
	public int getCapacity() {
		return this.capacity;
	}
	
	public void setMischievious(int mischievious) {
		//TODO Maybe verify if between 0 and 100 (%)
		this.mischievious = mischievious;
	}
	
	public int getMischievious() {
		return this.mischievious;
	}
	
	public void setPort(int port) {
		if (port < 5000 || port > 5500) {
			throw new IllegalArgumentException("Port should be between 5000 and 5500");
		}
		
		this.port = port;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public void setHost(String host) {
                if (host == null || host.trim().isEmpty()) {
                        throw new IllegalArgumentException("Host must not be empty.");
                }
                
                this.host = host;
	}
	
	public String getHost() {
                return this.host;
	}
	
	public ServerConfiguration() {
		this(10, 0, "127.0.0.1", 5000);
	}
	
	public ServerConfiguration(int capacity, int mischievious, String host, int port) {
		setCapacity(capacity);
		setMischievious(mischievious);
		setHost(host);
		setPort(port);
	}
	
	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this, ServerConfiguration.class);
	}
}
