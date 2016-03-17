package distributor;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import shared.*;

public final class NonSecureDistributor extends Distributor {

	private int m_projectedServerCapacity = 1;
	private ExecutorService m_executor = null;

	@Override
	public final void process() {
		List<Operation> operations = null;
		ExecutorService executor = null;

		while(this.pendingOperations.peek() != null) {

			operations = new ArrayList<Operation>();
			for (int i = 0; i < this.m_projectedServerCapacity; i++) {
				Operation op = this.pendingOperations.poll();
				if (op != null) {
					operations.add(op);
				}
			}

			int finalResult = 0;
			boolean majorityDetermined = false;

			while(!majorityDetermined) {
				Queue<Integer> resultsForTask = new ConcurrentLinkedQueue<Integer>();

				executor = Executors.newFixedThreadPool(this.calculationServers.size());

				int workerCounter = 0;
				for (ServerInterface serverStub : this.calculationServers) {
					NonSecureDistributorWorker worker = new NonSecureDistributorWorker(operations, serverStub, resultsForTask, ++workerCounter, this.nbTasksTried);
					executor.execute(worker);
				}

				//Wait for executor's full shutdown
				executor.shutdown();
				while(!executor.isTerminated()) {
				}

				if (resultsForTask.size() == this.calculationServers.size()) {
					this.m_projectedServerCapacity++;
					majorityDetermined = true;
					Map<Integer, Integer> occurencesForResult = new HashMap<Integer, Integer>();

					for (Integer result : resultsForTask) {
						if (!occurencesForResult.containsKey(result)) {
							occurencesForResult.put(result, 1);
						} else {
							Integer count = occurencesForResult.get(result);
							occurencesForResult.put(result, ++count);
						}
					}

					finalResult = Collections.max(
                        occurencesForResult.entrySet(),
                        new Comparator<Entry<Integer,Integer>>(){
                            @Override
                            public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
                                return o1.getValue() > o2.getValue()? 1 : -1;
                            }
                        }).getKey();
				}
			}

			if (majorityDetermined) {
				this.results.add(finalResult);

				Task t = new Task(0);

				for(Operation op : operations) {
					t.addOperation(op);
				}

				this.doneTasks.add(t);
			}
		}
	}
}
