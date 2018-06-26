package calculator;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionUnaryOperator implements Expression {
	EnumOperator operator;
	Expression expr;
	
	public ExpressionUnaryOperator(EnumOperator operator,
			Expression expr) {
		if (operator.cardinality != 1)
			throw new IllegalArgumentException(
					"Operator cardinality is not 1");
		this.operator = operator;
		this.expr = expr;
	}
	
	@Override
	public String toEvalString() {
		String exprStr = expr.toEvalString();
		String opSym = operator.getSymbol();
		return operator.isPostfix()? exprStr + opSym : opSym + exprStr;
	}
	
	@Override
	public String toCompiledString() {
		return "<%s %s>".format(operator, expr.toCompiledString());
	}
	
	@Override
	public String toString() {
		return "UnaryOperator{operator=%s,expr=%s}".format(operator,
				expr);
	}
	
	@Override
	public Object eval(Scope scope) {
		return operator.call(scope, expr.eval(scope));
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitUnary(this);
	}
}
