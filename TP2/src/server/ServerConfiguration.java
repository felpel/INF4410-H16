package server;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

//Custom class that represents the configuration of a calculation server.

public class ServerConfiguration {
  
  //We need to expose these because it is required for the deserialization made by the Gson Builder
  @Expose
	private int capacity; // see p.5 ("Sim. des ressources")
	@Expose
	private int mischievious; // see p.5 ("Serveur de calcul malicieux")
	@Expose
	private int port; // see p.8 ("Conseil pour le travail")
	@Expose
	private String host;
	
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	public int getCapacity() {
		return this.capacity;
	}
	
	public void setMischievious(int mischievious) {
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
	}
	
	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this, ServerConfiguration.class);
	}
}
