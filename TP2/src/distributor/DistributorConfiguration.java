package distributor;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

public class DistributorConfiguration
{
        @Expose
	private boolean secure;
        @Expose
	private String dataFilename;
        @Expose
	private ArrayList<ServerInformation> servers;

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

	public void setServers(ArrayList<ServerInformation> servers){
		this.servers = servers;
	}

	public List<ServerInformation> getServers() {
		return this.servers;
	}

	public DistributorConfiguration() {
	}
	
        @Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this, DistributorConfiguration.class);
	}
}
