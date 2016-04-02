package distributor;

import java.io.IOException;

import shared.*;

public class Client {
	public static final String DEFAULT_CONFIGURATION = "distributor-config.json";

	public static void main(String[] args) {
		/*if (args[0].equals("--benchmark") || args[0].equals("-b")) {
			doBenchmark
		}

		return;*/

		DistributorConfiguration configuration = null;
		Distributor distributor = null;

		try {
			configuration = Client.loadConfiguration();
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
			try {
				distributor.initialize(configuration);
				if (distributor.calculationServers != null &&
						!distributor.calculationServers.isEmpty() &&
						distributor.pendingOperations != null &&
						!distributor.pendingOperations.isEmpty())
				{
					long startTime = System.nanoTime();
					distributor.process();
					long endTime = System.nanoTime();
					Utilities.logInformation(String.format("Start time: %s ns\tEnd time: %s ns", Long.toString(startTime), Long.toString(endTime)));
					distributor.showFinalResult(endTime - startTime);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static DistributorConfiguration loadConfiguration() throws IOException {
		return Client.loadConfiguration(DEFAULT_CONFIGURATION);
	}

	private static DistributorConfiguration loadConfiguration(String filename) throws IOException {
		return Utilities.<DistributorConfiguration>readJsonConfiguration(filename, DistributorConfiguration.class);
	}
}
