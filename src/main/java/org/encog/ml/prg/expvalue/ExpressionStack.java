package org.encog.ml.prg.expvalue;

import java.io.Serializable;

import org.encog.EncogError;

public class ExpressionStack implements Serializable {
	private final ExpressionValue[] stack;
	private int position;

	public ExpressionStack(final int theStackSize) {
		this.stack = new ExpressionValue[theStackSize];
		for (int i = 0; i < this.stack.length; i++) {
			this.stack[i] = new ExpressionValue(0);
		}
	}

	private void checkOverflow() {
		if (this.position + 1 >= this.stack.length) {
			throw new EncogError("Stack overflow.");
		}
	}

	public void clear() {
		this.position = 0;
	}

	public boolean isEmpty() {
		return this.position == 0;
	}

	public void operationAdd() {
		final ExpressionValue b = pop();
		final ExpressionValue a = pop();

		if (a.isString() || b.isString()) {
			push(a.toStringValue() + b.toStringValue());
		} else if (a.isInt() && b.isInt()) {
			push(a.toIntValue() + b.toIntValue());
		} else {
			push(a.toFloatValue() + b.toFloatValue());
		}
	}

	public void operationDiv() {
		final ExpressionValue b = pop();
		final ExpressionValue a = pop();

		if (a.isInt() && b.isInt()) {
			push(a.toIntValue() / b.toIntValue());
		} else {
			push(a.toFloatValue() / b.toFloatValue());
		}
	}

	public void operationMul() {
		final ExpressionValue b = pop();
		final ExpressionValue a = pop();

		if (a.isInt() && b.isInt()) {
			push(a.toIntValue() * b.toIntValue());
		} else {
			push(a.toFloatValue() * b.toFloatValue());
		}
	}

	public void operationPow() {
		final ExpressionValue b = pop();
		final ExpressionValue a = pop();

		if (a.isInt() && b.isInt()) {
			push(Math.pow(a.toIntValue(), b.toIntValue()));
		} else {
			push(Math.pow(a.toFloatValue(), b.toFloatValue()));
		}

	}

	public void operationSub() {
		final ExpressionValue b = pop();
		final ExpressionValue a = pop();

		if (a.isInt() && b.isInt()) {
			push(a.toIntValue() - b.toIntValue());
		} else {
			push(a.toFloatValue() - b.toFloatValue());
		}
	}

	public ExpressionValue pop() {
		if (isEmpty()) {
			throw new EncogError(
					"Stack is empty, not enough values to perform that function.");
		}
		this.position--;
		final ExpressionValue result = this.stack[this.position];
		return result;
	}

	public void push(final boolean value) {
		checkOverflow();
		this.stack[this.position++].setValue(value);
	}

	public void push(final double value) {
		checkOverflow();
		this.stack[this.position++].setValue(value);
	}

	public void push(final ExpressionValue value) {
		checkOverflow();
		this.stack[this.position++].setValue(value);
	}

	public void push(final long value) {
		checkOverflow();
		this.stack[this.position++].setValue(value);
	}

	public void push(final String value) {
		checkOverflow();
		this.stack[this.position++].setValue(value);
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append("[ExpressionStack: ");

		for (int i = this.position; i >= 0; i--) {
			result.append(this.stack[i].toStringValue());
			result.append(" ");
		}
		result.append("]");
		return result.toString();
	}
}
