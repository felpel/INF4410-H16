package distributor;

import java.util.ArrayList;
import java.util.List;

public class DistributorConfiguration
{
	private boolean secure;
	private String dataFilename;
	private List<ServerInformation> servers;

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public boolean getSecure() {
		return this.secure;
	}

	public void setDataFilename(String filename) {
		this.dataFilename = filename;
	}

	public String getDataFilename() {
		return this.dataFilename;
	}

	public void setServers(List<ServerInformation> servers){
		this.servers = servers;
	}

	public List<ServerInformation> getServers() {
		return this.servers;
	}

	public DistributorConfiguration() {
		this(true, "donnees-2317.txt");

		List<ServerInformation> servers = new ArrayList<ServerInformation>();
		servers.add(new ServerInformation(5000));
		servers.add(new ServerInformation(5001));
		servers.add(new ServerInformation(5002));

		setServers(servers);
	}

	public DistributorConfiguration(boolean secure, String filename) {
		setSecure(secure);
		setDataFilename(filename);
	}
}
