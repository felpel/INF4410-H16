package shared;

import java.io.Serializable;
import java.util.ArrayList;

public class Task implements Serializable {
	private int id;
	private ArrayList<SubTask> subTasks;

	public int getId () {
		return this.id;
	}

	public ArrayList<SubTask> getSubTasks() {
		return this.subTasks;
	}

	public Task(int id) {
		this.id = id;
		this.subTasks = new ArrayList<SubTask>();
	}

	public void addSubTask(String operation, int operand) {
		SubTask st = new SubTask(operation, operand);
		subTasks.add(st);
	}

  @Override
	public String toString() {
		return this.toString(false);
	}

	public String toString(boolean verbose) {
		StringBuilder sb = new StringBuilder();
		if (verbose) {
			sb.append(String.format("Task [#%d]\n----------------------\n", this.id));
			for(SubTask st : this.getSubTasks()) {
				sb.append(String.format("-\t%s\n", st.toString()));
			}
		}
		else {
			sb.append(String.format("Task #[%d] - %d operation(s)\n", this.id, this.getSubTasks().size()));
		}
		return sb.toString();
	}
}
