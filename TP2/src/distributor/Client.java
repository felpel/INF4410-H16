package distributor;

import java.io.IOException;

import shared.*;

public class Client {
	public static final String DEFAULT_CONFIGURATION = "distributor-config.json";
	
	public static void main(String[] args) {
		DistributorConfiguration configuration = null;
		Distributor distributor = null;
		
		try {
			configuration = this.loadConfiguration();
		} 
		catch (IOException ioe) {
			Utilities.logError(ioe.getMessage());
		}
		finally {
			if (configuration == null) {
				Utilities.logError("Unable to retrieve properly the distributor's configuration");
				return;
			}
			
			distributor = configuration.getSecure() ?
										new SecureDistributor() :
										new NonSecureDistributor();
		}
		
		if (distributor != null) {
			distributor.initialize(configuration);
			distributor.process();
		}
	}
	
	private static DistributorConfiguration loadConfiguration() throws IOException {
		return this.loadConfiguration(DEFAULT_CONFIGURATION);
	}
	
	private static DistributorConfiguration loadConfiguration(String filename) throws IOException {
		return Utilities.<DistributorConfiguration>readJsonConfiguration(filename, DistributorConfiguration.class);
	}
}