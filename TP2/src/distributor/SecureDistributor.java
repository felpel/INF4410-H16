package distributor;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import shared.*;

public final class SecureDistributor extends Distributor {
	public final void process() {
		ExecutorService executor = Executors.newFixedThreadPool(this.calculationServers.size());

		for (Entry<Integer, ServerInterface> calculationServer : this.calculationServers.entrySet()) {
			DistributorWorker worker = new SecureDistributorWorker(this.pendingOperations, this.doneTasks, calculationServer.getValue(), this.results, calculationServer.getKey(), this.nbTasksTried);
			executor.execute(worker);
		}

		//Wait for executor's full shutdown
		executor.shutdown();
		while(!executor.isTerminated()) {
		}
	}
}
