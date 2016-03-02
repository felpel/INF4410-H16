package server;

public class SubTask {
	private String operation;
	private int operand;
	
	public String getOperation() {
		return this.operation;
	}
	
	public void setOperation(String op) {
		this.operation = op;
	}
	
	public int getOperand() {
		return this.operand;
	}
	
	public void setOperand(int op) {
		this.operand = op;
	}
	
	public SubTask(String operation, int operand) {
		this.operation = operation;
		this.operand = operand;
	}
}
