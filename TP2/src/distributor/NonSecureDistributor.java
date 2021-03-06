package distributor;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Map;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import shared.*;

//Distributor for the NonSecure mode of the application

public final class NonSecureDistributor extends Distributor {

	//We send the same task to every server so the tasks need to have a common number of operations
	private int m_projectedServerCapacity = 1;
	private ExecutorService m_executor = null;

	@Override
	public final void process() {
		List<Operation> operations = null;
		ExecutorService executor = null;

		//While there are pending operations and we have atleast 2 calculation servers
		while(this.pendingOperations.peek() != null && this.calculationServers.size() > 1) {

			//Retrieve from the list the appropriate amount of operations
			operations = new ArrayList<Operation>();
			for (int i = 0; i < this.m_projectedServerCapacity; i++) {
				Operation op = this.pendingOperations.poll();
				if (op != null) {
					operations.add(op);
				}
			}

			int finalResult = 0;
			boolean majorityDetermined = false;
			//This map will contain all the results. That enables us to check for a majority in past results if 
			//its not possible to have a majority (ex : only two servers answered with two diff answers)
			Map<Integer, Integer> pastResults = new HashMap<Integer, Integer>();

			while(!majorityDetermined) {
				Queue<ServerResult> resultsForTask = new ConcurrentLinkedQueue<ServerResult>();

				executor = Executors.newFixedThreadPool(this.calculationServers.size());

				for (Entry<Integer, ServerInterface> calculationServer : this.calculationServers.entrySet()) {
					NonSecureDistributorWorker worker = new NonSecureDistributorWorker(operations, calculationServer.getValue(), resultsForTask, calculationServer.getKey(), this.nbTasksTried);
					executor.execute(worker);
				}

				//Wait for executor's full shutdown
				executor.shutdown();
				while(!executor.isTerminated()) {
				}

				int resultsCount = 0;
				int threshold = 0;
				boolean loadTooBig = false;

				//Process the server results
				for(ServerResult sr : resultsForTask) {
						if(sr.getResult() != null) {
							resultsCount++;
						}

						Exception failure = sr.getFailure();
						if (!loadTooBig && failure != null && failure instanceof ServerTooBusyException) {
							Utilities.log(String.format("Server [%d] was too busy", sr.getServerId()));
							loadTooBig = true;
						}

						if (failure != null && failure instanceof RemoteException) {
                                                        int serverId = sr.getServerId();
							Utilities.log(String.format("RemoteException from server [%d], it will no longer be used.", serverId));
							this.calculationServers.remove(serverId);
						}
				}

				//Threshold for establishing a majority
				threshold = resultsCount/2 + 1;

				if (resultsCount == this.calculationServers.size()) {
					this.m_projectedServerCapacity++;
				}

				if (loadTooBig && this.m_projectedServerCapacity > 1) {
					this.m_projectedServerCapacity--;
				}

				//Try to establish a majority with the current results, if it fails, try with the past results too
				if (resultsCount > 1) {
					Map<Integer, Integer> occurencesForResult = new HashMap<Integer, Integer>();

					for (ServerResult sr : resultsForTask) {
						if (sr.getResult() == null) {
							continue;
						}

						if (!occurencesForResult.containsKey(sr.getResult())) {
							occurencesForResult.put(sr.getResult(), 1);
						} else {
							Integer count = occurencesForResult.get(sr.getResult());
							occurencesForResult.put(sr.getResult(), ++count);
						}
					}

					Entry<Integer, Integer> potentialResult = Collections.max(
                        occurencesForResult.entrySet(),
                        new Comparator<Entry<Integer,Integer>>(){
                            @Override
                            public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
                                return o1.getValue() > o2.getValue()? 1 : -1;
                            }
                        });

					if (potentialResult.getValue() >= threshold) {
						majorityDetermined = true;
						finalResult = potentialResult.getKey();
					}
					else {
						boolean hasPastResults = pastResults.size() != 0;

						for(Entry<Integer, Integer> newResult : occurencesForResult.entrySet()) {
							Integer updatedCount = newResult.getValue();
							if (pastResults.containsKey(newResult.getKey())) {
								updatedCount += pastResults.get(newResult.getKey());
							}
							pastResults.put(newResult.getKey(), updatedCount);
						}

						if (hasPastResults) {
							Entry<Integer, Integer> potentialEntry = Collections.max(
		                        pastResults.entrySet(),
		                        new Comparator<Entry<Integer,Integer>>(){
		                            @Override
		                            public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
		                                return o1.getValue() > o2.getValue()? 1 : -1;
		                            }
		                        });
							if (potentialEntry.getValue() > 1) {
								finalResult = potentialEntry.getKey();
								majorityDetermined = true;
							}
						}
					}
				}
			}

			//Majority has been determined so we can add the tasks to the donTasks list
			if (majorityDetermined) {
				ServerResult sr = new ServerResult();
				sr.setResult(finalResult);
				this.results.add(sr);
				Utilities.logInformation(String.format("Majority result is : %d\n", finalResult));
				Task t = new Task(0);

				for(Operation op : operations) {
					t.addOperation(op);
				}

				this.doneTasks.add(t);
			}
		}
	}
}
