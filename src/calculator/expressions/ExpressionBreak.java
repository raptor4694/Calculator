package calculator.expressions;

import calculator.Scope;
import calculator.Visitor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionBreak extends RuntimeException
		implements Expression {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5849243680513397843L;
	
	@Override
	public Object eval(Scope scope) {
		throw this;
	}
	
	@Override
	public String toEvalString() {
		return "break";
	}
	
	@Override
	public String toCompiledString() {
		return "<BREAK>";
	}
	
	@Override
	public String toString() {
		return "Break{}";
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitBreak(this);
	}
	
}
