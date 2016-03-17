package distributor;

import java.util.Queue;
import java.rmi.RemoteException;
import shared.*;

public class NonSecureDistributorWorker extends DistributorWorker {
  private Task m_task = null;

  public NonSecureDistributorWorker(Queue<Operation> pendingOperations, ServerInterface serverStub, Queue<Integer> results, int id) {
    super(id, serverStub, results, pendingOperations);
  }

  public final void assignTask(Task task) {
    if (task == null) {
      throw new IllegalArgumentException(this.getLogPrefix() + "Cannot assign a null task");
    }

    m_task = task;
  }

  @Override
  public void run() {
    boolean taskCompleted = false;

    while (/*!m_pendingTasks.isEmpty() &&*/ m_retryCount < MAX_RETRY) {
      try {
        while (m_task == null) {}
        this.tryAddResultFromServer(m_task);
        m_task = null;
        m_retryCount = 0;
      }
      catch (ServerTooBusyException | RemoteException e) {
        Utilities.log(String.format("%sRetry count: %d", this.getLogPrefix(), ++m_retryCount));
      }
    }

    this.finish();
  }
}
