package distributor;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import shared.*;

public class SecureDistributorWorker extends DistributorWorker {
  private Queue<Task> m_doneTasks = null;

  public SecureDistributorWorker(Queue<Operation> pendingOperations, Queue<Task> doneTasks,
                           ServerInterface serverStub, Queue<Integer> results,
                           int id, AtomicInteger nbTasksTried) {
    super(id, serverStub, results, pendingOperations, nbTasksTried);

    m_doneTasks = doneTasks;
  }

  @Override
  public void run() {
    List<Operation> operations = null;
    while(m_pendingOperations.peek() != null && m_retryCount <= MAX_RETRY) {
      //TODO Create task with perceived server's capacity

      operations = new ArrayList<Operation>();
      for (int i = 0; i < this.m_projectedServerCapacity && this.m_pendingOperations.peek() != null; i++) {
        Operation op = this.m_pendingOperations.poll();
        if (op != null) {
          operations.add(op);
        }
      }

      try {
        if (!operations.isEmpty()) {
          Task t = this.tryAddResultFromServer(operations);
          ++this.m_projectedServerCapacity;
          this.m_doneTasks.add(t);
        }
      }
      catch (ServerTooBusyException stbe) {
  			Utilities.logError(String.format("%sTask was REFUSED!", this.getLogPrefix()));

        // Put tasks in queue if it could not be completed
        for (Operation undoneOperation : operations) {
          this.m_pendingOperations.add(undoneOperation);
        }

        //Reduce server's capacity
        if (this.m_projectedServerCapacity > 1) {
          --this.m_projectedServerCapacity;
        }

        //TODO Check if we want the worker to sleep (ex: 2s) to let the server finish his previous task(s)
        try {
          Thread.sleep(2000);
        } catch (InterruptedException ie) {
          //Do nothing
        }
      }
      catch (RemoteException re) {
        Utilities.logError(re.getMessage());
        ++m_retryCount;
        //TODO Probably notify distributor main of server failure
        //TODO verify if server timed out (check if this is really related to RemoteException)
			}
    }
    this.finish();
  }
}
