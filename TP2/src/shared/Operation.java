package shared;

import java.io.Serializable;

//@SuppressWarnings("serial")
public class Operation implements Serializable {
	private String function;
	private int operand;

	public String getFunction() {
		return this.function;
	}

	public void setFunction(String fn) {
		this.function = fn;
	}

	public int getOperand() {
		return this.operand;
	}

	public void setOperand(int op) {
		this.operand = op;
	}

	public Operation(String function, int operand) {
		setFunction(function);
		setOperand(operand);
	}

  @Override
	public String toString() {
		return String.format("%s(%d)", this.getFunction(), this.getOperand());
	}
}
