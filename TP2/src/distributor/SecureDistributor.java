package distributor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import shared.*;

public final class SecureDistributor extends Distributor {
	//TODO Test and validate
	public final void process() {
		ExecutorService executor = Executors.newFixedThreadPool(this.calculationServers.size());

		int workerCounter = 0;
		for (ServerInterface serverStub : this.calculationServers) {
			DistributorWorker worker = new SecureDistributorWorker(this.pendingOperations, this.doneTasks, serverStub, this.results, ++workerCounter);
			executor.execute(worker);
		}

		//Wait for executor's full shutdown
		executor.shutdown();
		while(!executor.isTerminated()) {
		}
	}
}
