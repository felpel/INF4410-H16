package distributor;

import com.google.gson.annotations.Expose;

public class ServerInformation {
        @Expose
	private String host;
	
	@Expose
	private int port;
	
	public void setHost(String host) {
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
	}
}