package shared;

import java.io.Serializable;
import java.util.ArrayList;

public class Task implements Serializable {
	public ArrayList<SubTask> subTasks;
	
	public ArrayList<SubTask> getSubTasks() {
		return this.subTasks;
	}
	
	public Task() {
		this.subTasks = new ArrayList<SubTask>();
	}
	
	public void addSubTask(String operation, int operand) {
		SubTask st = new SubTask(operation, operand);
		subTasks.add(st);
	}
}
