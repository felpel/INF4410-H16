package shared;

import java.io.Serializable;

//Custom Class used to represent an operation in a data file
//For exemple, fib 5
//The member function will have the value "fib"
//And the member operand will be the int value of 5

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
