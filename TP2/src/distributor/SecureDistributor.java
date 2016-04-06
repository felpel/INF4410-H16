package distributor;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import shared.*;

//Distributor for the Secure mode of the application

public final class SecureDistributor extends Distributor {
	
	public final void process() {
		//Create a thread pool with X threads where X is the number of calculation servers
		ExecutorService executor = Executors.newFixedThreadPool(this.calculationServers.size());

		//Iterate over our calculation servers and create a SecureDistributorWorker for each of them
		for (Entry<Integer, ServerInterface> calculationServer : this.calculationServers.entrySet()) {
			DistributorWorker worker = new SecureDistributorWorker(this.pendingOperations, this.doneTasks, calculationServer.getValue(), this.results, calculationServer.getKey(), this.nbTasksTried);
			//Make the SecureDistributorWorker do its thing
			executor.execute(worker);
		}

		//Wait for executor's full shutdown
		executor.shutdown();
		while(!executor.isTerminated()) {
		}
	}
}
