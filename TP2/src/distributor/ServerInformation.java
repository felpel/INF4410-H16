package distributor;

public class ServerInformation {
	private String host;
	private int port;
	
	public void setHost(String host) {
		// Maybe validate host (localhost, 127.0.0.1 or something IPv4)
		this.host = host;
	}
	
	public String getHost() {
		return host;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public ServerInformation() {
		this(5000);
	}
	
	public ServerInformation(int port) {
		this("127.0.0.1", port);
	}
	
	public ServerInformation(String host, int port) {
		setHost(host);
		setPort(port);
	}
}