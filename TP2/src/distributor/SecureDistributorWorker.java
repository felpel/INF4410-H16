package distributor;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import shared.*;

public class SecureDistributorWorker extends DistributorWorker {
  private Queue<Task> m_doneTasks = null;
  private int m_projectedServerCapacity = 1;
  private int m_rejectedTasks = 0;

  public SecureDistributorWorker(Queue<Operation> pendingOperations, Queue<Task> doneTasks,
                           ServerInterface serverStub, Queue<ServerResult> results,
                           int id, AtomicInteger nbTasksTried) {
    super(id, serverStub, results, pendingOperations, nbTasksTried);

    m_doneTasks = doneTasks;
  }

  @Override
  public void run() {
    List<Operation> operations = null;
    while(m_pendingOperations.peek() != null) {
      operations = new ArrayList<Operation>();
      for (int i = 0; i < this.m_projectedServerCapacity && this.m_pendingOperations.peek() != null; i++) {
        Operation op = this.m_pendingOperations.poll();
        operations.add(op);
      }

      if (!operations.isEmpty()) {
        Task t = createTask(operations);
        ServerResult sr = this.tryAddResultFromServer(t);
        if (sr.getResult() != null && sr.getFailure() == null) {
          ++this.m_projectedServerCapacity;
          this.m_doneTasks.add(t);
        }
        
        if (sr.getFailure() != null) {
            // Put operations in queue if the task could not be completed
            for (Operation undoneOperation : operations) {
                this.m_pendingOperations.add(undoneOperation);
            }
        
            if (sr.getFailure() instanceof ServerTooBusyException) {
                Utilities.logError(String.format("%sTask was REFUSED!", this.getLogPrefix()));
                this.m_rejectedTasks++;

                //Reduce server's capacity
                if (this.m_projectedServerCapacity > 1) {
                    --this.m_projectedServerCapacity;
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    //Do nothing
                }
            }
            else if (sr.getFailure() instanceof RemoteException) {
                Utilities.logError(sr.getFailure().getMessage());
                break;
            }
            else {
                Utilities.logError(String.format("Unexpected error occured:\n%s", sr.getFailure().getMessage()));
            }
        }
      }
    }
    this.finish();
  }

  public void finish() {
    super.finish();
    Utilities.logInformation(String.format("%s %d task(s) were refused.", this.getLogPrefix(), this.m_rejectedTasks));
  }
}
