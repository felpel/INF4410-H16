package distributor;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import shared.*;

//DistributorWorker for the Secure mode of the application

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


  //The logic for the run in this mode is more complex than the one for NoNSecureDistributorWorker. This is because the workers in the 
  //secure mode share a list of operations and they are doing the polling instead of the Distributor.
  @Override
  public void run() {
    List<Operation> operations = null;
    while(m_pendingOperations.peek() != null) {
      operations = new ArrayList<Operation>();
      //Simply populate a list of operations based on the projectedServerCapacity
      for (int i = 0; i < this.m_projectedServerCapacity && this.m_pendingOperations.peek() != null; i++) {
        Operation op = this.m_pendingOperations.poll();
        operations.add(op);
      }

      if (!operations.isEmpty()) {
        //Create a task with the selected operations and then process on the server
        Task t = createTask(operations);
        ServerResult sr = this.tryAddResultFromServer(t);

        //If we recieved a result and there was no failure, we can increment the projected server capacity
        //and add the task to the doneTask list
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
