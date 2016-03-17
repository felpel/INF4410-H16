package distributor;

import java.util.List;
import java.util.Queue;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;
import shared.*;

public class NonSecureDistributorWorker extends DistributorWorker {
  private List<Operation> m_operations = null;

  public NonSecureDistributorWorker(List<Operation> operations, ServerInterface serverStub, Queue<Integer> results, int id, AtomicInteger nbTasksTried) {
    super(id, serverStub, results, null, nbTasksTried);

    this.m_operations = operations;
  }

  @Override
  public void run() {

    try {
      if (!this.m_operations.isEmpty()) {
        this.tryAddResultFromServer(this.m_operations);
      }
    }
    catch (ServerTooBusyException stbe) {
      Utilities.logError(String.format("%sTask was REFUSED!", this.getLogPrefix()));
    }
    catch (RemoteException re) {
      Utilities.logError(re.getMessage());
    }

    this.finish();
  }
}
