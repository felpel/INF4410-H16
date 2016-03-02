package server;

import com.google.gson.Gson;

//TODO Try to validate parsed values ... does not work at the moment
public class ServerConfiguration {	
	private int capacity; // voir p.5 ("Sim. des ressources")
	private int malicious; // voir p.5 ("Serveur de calcul malicieux")
	private int port; // voir p.8 ("Conseil pour le travail")
	
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	public int getCapacity() {
		return this.capacity;
	}
	
	public void setMalicious(int malicious) {
		this.malicious = malicious;
	}
	
	public int getMalicious() {
		return this.malicious;
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
	
	public ServerConfiguration() {
		this(10, 0, 5000);
	}
	
	public ServerConfiguration(int capacity, int malicious, int port) {
		setCapacity(capacity);
		setMalicious(malicious);
		setPort(port);
	}
	
	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this, ServerConfiguration.class);
	}
}
