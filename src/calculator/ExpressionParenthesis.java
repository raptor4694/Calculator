package calculator;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionParenthesis implements Expression {
	Expression expr;
	
	@Override
	public Object eval(Scope scope) {
		return expr.eval(scope);
	}
	
	@Override
	public Object evalOptionalValue(Scope scope) {
		return expr.evalOptionalValue(scope);
	}
	
	@Override
	public String toEvalString() {
		return "(" + expr.toEvalString() + ")";
	}
	
	@Override
	public Expression skipParens() {
		return expr.skipParens();
	}
	
	@Override
	public String toCompiledString() {
		return "<%s>".format(
				(Object) expr.skipParens().toCompiledString());
	}
	
	@Override
	public String toString() {
		return "Parenthesis{expr=%s}".format(expr);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitParens(this);
	}
}
