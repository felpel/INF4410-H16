package distributor;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import shared.*;

public class SecureDistributorWorker extends DistributorWorker {
  private int m_id = 0;
  private List<Task> m_acquiredTasks = null;
  private Queue<Task> m_pendingTasks = null;
  private Queue<Task> m_doneTasks = null;

  public SecureDistributorWorker(Queue<Task> pendingTasks, Queue<Task> doneTasks,
                           ServerInterface serverStub, Queue<Integer> results,
                           int id) {
    super(id, serverStub, results, pendingTasks);

    m_doneTasks = doneTasks;
    m_acquiredTasks = new ArrayList<Task>();
  }

  @Override
  public void run() {
    Task t;
    while((t = m_pendingTasks.poll()) != null && m_retryCount <= MAX_RETRY) {
      m_acquiredTasks.add(t);
      try {
        tryAddResultFromServer(t);
        m_doneTasks.add(t);
      }
      catch (ServerTooBusyException stbe) {
  			Utilities.logError(String.format("%sTask was REFUSED!", this.getLogPrefix()));
        //TODO Check if we want the worker to sleep (ex: 2s) to let the server finish his previous task(s)

        // Put tasks in queue if it could not be completed
        m_pendingTasks.add(t);
      }
      catch (RemoteException re) {
        Utilities.logError(re.getMessage());
        ++m_retryCount;
        //TODO Probably notify distributor main of server failure
        //TODO verify if server timed out (check if this is really related to RemoteException)
			}
      finally {
        m_acquiredTasks.remove(t);
      }
    }
    this.finish();
  }

  public void finish() {
    super.finish();

    if (m_acquiredTasks.isEmpty()) {
      return;
    }

    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%sUnfinished tasks:\n", this.getLogPrefix()));
    //If the worker dies, we still need to get the tasks that were not completed.
    for (Task unfinishedTask : m_acquiredTasks) {
        sb.append(String.format("%d, ", unfinishedTask.getId()));
        m_pendingTasks.add(unfinishedTask);
    }
    String tasks = sb.substring(0, sb.length() - 1);
    Utilities.log(tasks);
  }
}
