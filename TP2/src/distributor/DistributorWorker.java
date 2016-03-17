package distributor;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.rmi.RemoteException;

import shared.*;

public abstract class DistributorWorker implements Runnable {
  public final int MAX_RETRY = 5;

  protected Queue<Integer> m_results = null;
  protected Queue<Operation> m_pendingOperations = null;
  protected int m_id = 0;
  protected ServerInterface m_serverStub = null;
  protected int m_retryCount = 0;
  protected int m_projectedServerCapacity = 1;
  protected AtomicInteger m_nbTasksTried = null;

  public DistributorWorker(int id, ServerInterface serverStub, Queue<Integer> results, Queue<Operation> pendingOperations, AtomicInteger nbTasksTried) {
    m_id = id;
    m_serverStub = serverStub;
    m_results = results;
    m_pendingOperations = pendingOperations;
    m_nbTasksTried = nbTasksTried;
  }

  public abstract void run();

  public final Task tryAddResultFromServer(List<Operation> operations) throws RemoteException, ServerTooBusyException {
    Task t = new Task(this.m_nbTasksTried.incrementAndGet());
    for (Operation op : operations) {
      t.addOperation(op);
    }
    this.tryAddResultFromServer(t);
    return t;
  }

  private void tryAddResultFromServer(Task t) throws RemoteException, ServerTooBusyException {
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
