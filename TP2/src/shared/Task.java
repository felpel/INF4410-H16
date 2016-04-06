package shared;

import java.io.Serializable;

import java.util.ArrayList;

//Custom class to regroup operations before sending to calc. servers
//A task has an ID and an list of Operations

//@SuppressWarnings("serial")
public class Task implements Serializable {
	private int id;
	private ArrayList<Operation> operations;

	public int getId () {
		return this.id;
	}

	public ArrayList<Operation> getOperations() {
		return this.operations;
	}

	public Task(int id) {
		this.id = id;
		this.operations = new ArrayList<Operation>();
	}

	public void addOperation(Operation op) {
		this.addOperation(op.getFunction(), op.getOperand());
	}

	public void addOperation(String function, int operand) {
		Operation op = new Operation(function, operand);
		this.operations.add(op);
	}

  @Override
	public String toString() {
		return this.toString(false);
	}

	public String toString(boolean verbose) {
		StringBuilder sb = new StringBuilder();
		if (verbose) {
			sb.append(String.format("Task [#%d]\n----------------------\n", this.id));
			for(Operation op : this.getOperations()) {
				sb.append(String.format("-\t%s\n", op.toString()));
			}
		}
		else {
			sb.append(String.format("Task #[%d] - %d operation(s)", this.id, this.getOperations().size()));
		}
		return sb.toString();
	}
}
