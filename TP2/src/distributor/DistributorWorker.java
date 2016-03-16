package distributor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.rmi.RemoteException;

import shared.*;

public class DistributorWorker implements Runnable {
  public final int MAX_RETRY = 5;

  private int m_id = 0;
  private Queue<int> m_results = null;
  private ArrayList<Task> m_acquiredTasks = new ArrayList<Task>();
  private Queue<Task> m_pendingTasks = null;
  private Queue<Task> m_doneTasks = null;
  private ServerInterface m_serverStub = null;
  private int m_retryCount = 0;

  public DistributorWorker(Queue<Task> pendingTasks, Queue<Task> doneTasks,
                           ServerInterface serverStub, Queue<int> results,
                           int id) {
    m_pendingTasks = pendingTasks;
    m_doneTasks = doneTasks;
    m_serverStub = serverStub;
    m_results = results;
    m_id = id;
  }

  @Override
  public void run() {
    Task t;
    while((t = m_pendingTasks.poll()) != null) {
      m_acquiredTasks.add(t);
      try {
        Utilities.log(String.format("%sTrying to process task...\n%s", this.getLogPrefix(), t.toString(true)));
        int result = m_serverStub.process(t);
        Utilities.log(String.format("%sReceived result from task [%d] -> %d", this.getLogPrefix(), t.getId(), result));
        m_results.add(result);
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
        //re.printStackTrace();
        //TODO Probably notify distributor main of server failure
        //TODO verify if server timed out (check if this is really related to RemoteException)
        if (++m_retryCount > MAX_RETRY) {
          this.finish();
          return;
        }
			}
      finally {
        m_acquiredTasks.remove(t);
      }
    }
  }

  public void finish() {
    Utilities.log(String.format("%sFinished", this.getLogPrefix()));
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

  private String getLogPrefix() {
    return String.format("[W%d] ", m_id);
  }
}
