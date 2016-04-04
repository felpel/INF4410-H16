package distributor;

import java.util.List;
import java.util.Queue;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;
import shared.*;

//DistributorWorker for the NonSecure mode of the application

public class NonSecureDistributorWorker extends DistributorWorker {
  private List<Operation> m_operations = null;

  public NonSecureDistributorWorker(List<Operation> operations, ServerInterface serverStub, Queue<ServerResult> results, int id, AtomicInteger nbTasksTried) {
    super(id, serverStub, results, null, nbTasksTried);

    this.m_operations = operations;
  }

  //Create a task with the current operations and try to process them
  @Override
  public void run() {
    if (!this.m_operations.isEmpty()) {
      Task t = this.createTask(this.m_operations);
      this.tryAddResultFromServer(t);
    }
    this.finish();
  }
}
