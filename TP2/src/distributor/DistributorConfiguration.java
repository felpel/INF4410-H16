package distributor;

import java.util.ArrayList;

public class DistributorConfiguration
{
	private boolean secure;
	
	//TODO Validate parsing of this "complex" object
	private List<ServerInformation> servers;
	
	public void setSecure(boolean secure) {
		this.secure = secure;
	}
	
	public boolean getSecure() {
		return this.secure;
	}
	
	public void setServers(List<ServerInformation> servers){
		this.servers = servers;
	}
	
	public List<ServerInformation> getServers() {
		return this.servers;
	}
	
	public DistributorConfiguration() {
		List<ServerInformation> servers = new ArrayList<ServerInformation>();
		servers.add(new ServerInformation(5000));
		servers.add(new ServerInformation(5001));
		servers.add(new ServerInformation(5002));
		
		this(true, servers);
	}
	
	public DistributorConfiguration(boolean secure, List<ServerInformation> servers) {
		setSecure(secure);
		setServers(servers);
	}
}