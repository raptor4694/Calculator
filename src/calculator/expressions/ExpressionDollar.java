package calculator.expressions;

import calculator.CalculatorError;
import calculator.Scope;
import calculator.Visitor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionDollar implements Expression {
	
	@Override
	public Object eval(Scope scope) {
		if (!scope.hasVariable("$"))
			throw new CalculatorError("'$' outside of index");
		return scope.getVariable("$");
	}
	
	@Override
	public String toEvalString() {
		return "$";
	}
	
	@Override
	public String toCompiledString() {
		return "<SIZE>";
	}
	
	@Override
	public String toString() {
		return "Dollar{}";
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitDollar(this);
	}
	
}
