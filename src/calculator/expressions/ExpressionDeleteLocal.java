package calculator.expressions;

import calculator.Scope;
import calculator.errors.CalculatorError;

public class ExpressionDeleteLocal implements Expression {
	@Override
	public Object eval(Scope scope) {
		throw new CalculatorError("delete local does not return a value");
	}
	
	@Override
	public Object evalOptionalValue(Scope scope) {
		scope.deleteLocalVariables();
		return null;
	}
	
	@Override
	public String toEvalString() {
		return "delete local";
	}
	
	@Override
	public String toCompiledString() {
		return "<DELLOCAL>";
	}
}
