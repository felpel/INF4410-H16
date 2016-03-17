package distributor;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import shared.*;

public class SecureDistributorWorker extends DistributorWorker {
  private Queue<Task> m_doneTasks = null;

  public SecureDistributorWorker(Queue<Operation> pendingOperations, Queue<Task> doneTasks,
                           ServerInterface serverStub, Queue<Integer> results,
                           int id) {
    super(id, serverStub, results, pendingOperations);

    m_doneTasks = doneTasks;
  }

  @Override
  public void run() {
    Task t = null;
    while(/*TODO Modify this(t = m_pendingOperations.poll()) != null &&*/ m_retryCount <= MAX_RETRY) {
      //TODO Create task with perceived server's capacity
      //m_acquiredTasks.add(t);
      try {
        tryAddResultFromServer(t);
        m_doneTasks.add(t);
      }
      catch (ServerTooBusyException stbe) {
  			Utilities.logError(String.format("%sTask was REFUSED!", this.getLogPrefix()));
        //TODO Check if we want the worker to sleep (ex: 2s) to let the server finish his previous task(s)

        // Put tasks in queue if it could not be completed
        for (Operation undoneOperation : t.getOperations()) {
          m_pendingOperations.add(undoneOperation);
        }

        //TODO Reduce server's capacity
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
