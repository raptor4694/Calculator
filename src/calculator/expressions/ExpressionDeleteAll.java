package calculator.expressions;

import calculator.Scope;
import calculator.errors.CalculatorError;

public class ExpressionDeleteAll implements Expression {
	
	@Override
	public Object eval(Scope scope) {
		throw new CalculatorError("delete * does not return a value");
	}
	
	@Override
	public Object evalOptionalValue(Scope scope) {
		scope.deleteAllVariables();
		return null;
	}
	
	@Override
	public String toEvalString() {
		return "delete *";
	}
	
	@Override
	public String toCompiledString() {
		return "<DELALL>";
	}
	
}
