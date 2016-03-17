package distributor;

import java.util.Queue;
import java.rmi.RemoteException;

import shared.*;

public abstract class DistributorWorker implements Runnable {
  public final int MAX_RETRY = 5;

  protected Queue<Integer> m_results = null;
  protected Queue<Task> m_pendingTasks = null;
  protected int m_id = 0;
  protected ServerInterface m_serverStub = null;
  protected int m_retryCount = 0;

  public DistributorWorker(int id, ServerInterface serverStub, Queue<Integer> results, Queue<Task> pendingTasks) {
    m_id = id;
    m_serverStub = serverStub;
    m_results = results;
    m_pendingTasks = pendingTasks;
  }

  public abstract void run();

  public final void tryAddResultFromServer(Task t) throws RemoteException, ServerTooBusyException {
    Utilities.log(String.format("%sTrying to process task...\n%s", this.getLogPrefix(), t.toString(true)));
    int result = m_serverStub.process(t);
    Utilities.log(String.format("%sReceived result from task [%d] -> %d", this.getLogPrefix(), t.getId(), result));
    m_results.add(result);
  }

  public void finish() {
    Utilities.log(String.format("%sFinished", this.getLogPrefix()));
  }

  protected final String getLogPrefix() {
    return String.format("[W%d] ", m_id);
  }

}
