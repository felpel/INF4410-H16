package shared;

import java.util.ArrayList;

public class Task {
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
			sb.append(String.format("Task #[%d] - %d operation(s)\n", this.id, this.getOperations().size()));
		}
		return sb.toString();
	}
}
