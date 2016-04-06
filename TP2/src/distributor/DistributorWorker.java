package distributor;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.rmi.RemoteException;

import shared.*;

//Abstract class that defines how our distributor's worker should work (both Secure and NonSecure)

public abstract class DistributorWorker implements Runnable {
  public final int MAX_RETRY = 5;

  protected Queue<ServerResult> m_results = null;
  protected Queue<Operation> m_pendingOperations = null;
  protected int m_id = 0;
  protected ServerInterface m_serverStub = null;
  protected int m_retryCount = 0;
  protected int m_projectedServerCapacity = 1;
  protected AtomicInteger m_nbTasksTried = null;

  public DistributorWorker(int id, ServerInterface serverStub, Queue<ServerResult> results, Queue<Operation> pendingOperations, AtomicInteger nbTasksTried) {
    m_id = id;
    m_serverStub = serverStub;
    m_results = results;
    m_pendingOperations = pendingOperations;
    m_nbTasksTried = nbTasksTried;
  }

  public abstract void run();

  public final Task createTask(List<Operation> operations) {
    Task t = new Task(this.m_nbTasksTried.incrementAndGet());
    for (Operation op : operations) {
      t.addOperation(op);
    }
    return t;
  }

  //Create a new ServerResult and set the result to the value returned by the process function on the stub
  //If there is any error, we add those failures to the ServerResult so the distributor can react accordingly.
  public final ServerResult tryAddResultFromServer(Task t) {
    Utilities.log(String.format("%sTrying to process task...\n%s", this.getLogPrefix(), t.toString(true)));

    ServerResult sResult = new ServerResult();
    try {
      sResult.setServerId(this.m_id);
      sResult.setResult(m_serverStub.process(t));
      
      Utilities.log(String.format("%sReceived result from task [%d] -> %d", this.getLogPrefix(), t.getId(), sResult.getResult()));
    } catch(ServerTooBusyException stbe) {
      sResult.setFailure(stbe);
    } catch(RemoteException re) {
      sResult.setFailure(re);
    }
    

    m_results.add(sResult);
    return sResult;
  }

  public void finish() {
    Utilities.log(String.format("%sFinished", this.getLogPrefix()));
  }

  protected final String getLogPrefix() {
    return String.format("[W%d] ", m_id);
  }
}
