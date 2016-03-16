package distributor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import shared.*;

public final class SecureDistributor extends Distributor {
	//TODO Test and validate
	public final void process() {
		super.process();
		
		ExecutorService executor = Executors.newFixedThreadPool(this.calculationServers.size());

		int workerCounter = 0;
		for (ServerInterface serverStub : this.calculationServers) {
			DistributorWorker worker = new DistributorWorker(this.pendingTasks, this.doneTasks, serverStub, this.result, workerCounter++);
			executor.execute(worker);
		}

		//Wait for executor's full shutdown
		executor.shutdown();
		while(!executor.isTerminated()) {
		}

		// All tasks must be completed to get an appropriate result.
		if (!this.pendingTasks.isEmpty() || this.doneTasks.size() != nbTasks) {
			Utilities.log("Unable to get the correct results because some tasks were not treated.");
			//TODO Log error
			return;
		}

		this.result.set(this.result.get() % 5000);

		Utilities.logInformation(String.format("Result = %d", this.result.get()));
	}
}