package distributor;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import shared.*;

public final class NonSecureDistributor extends Distributor {

	private ExecutorService m_executor = null;

	public void process() {
		int workerCounter = 0;

		Queue<Integer> resultsForTask = new ConcurrentLinkedQueue<Integer>();
		List<NonSecureDistributorWorker> workers = new ArrayList<NonSecureDistributorWorker>();

		this.m_executor = Executors.newFixedThreadPool(this.calculationServers.size());

		for (ServerInterface serverStub : this.calculationServers) {
			NonSecureDistributorWorker w = new NonSecureDistributorWorker(this.pendingTasks, serverStub, resultsForTask, ++workerCounter);
			workers.add(w);
			this.m_executor.execute(w);
		}

		for (Task t : this.pendingTasks) {
			this.treatTask(t, workers, resultsForTask);
		}
	}

	private final void treatTask(Task t, List<NonSecureDistributorWorker> workers, Queue<Integer> resultsForTask) {
		for (NonSecureDistributorWorker worker : workers) {
			worker.assignTask(t);
		}

		// Wait for results
		while (resultsForTask.size() != workers.size()) {
			try {
					Thread.sleep(1000);
			}
			catch (InterruptedException ie) { // Do nothing
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("occurrences:\n");
		HashMap<Integer, Integer> occurences = new HashMap<Integer, Integer>();

		// Count occurrences of each result, we take the one who appeared the most
		for (Integer result : resultsForTask) {
			sb.append(result + ", ");
			if (!occurences.containsKey(result)) {
				occurences.put(result, 1);
			} else {
				Integer occurencesForResult = occurences.get(result);
				occurencesForResult++;
			}
		}

		Utilities.log(sb.substring(0, sb.length() - 2));

		Integer finalResult = Collections.max(
			occurences.entrySet(),
			new Comparator<Entry<Integer, Integer>>() {
				@Override
				public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
					return o1.getValue() > o2.getValue() ?
						1 : -1;
				}
			}).getKey();

		Utilities.log(String.format("Final result:\t%d", finalResult));

		this.results.add(finalResult);
		resultsForTask.clear();

		/*//Wait for executor's full shutdown
		executor.shutdownNow();
		while(!executor.isTerminated()) {
			System.out.println("Shutdown...");
		}*/
	}
}
