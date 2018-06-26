package calculator;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
public class ExpressionElementwiseBinaryOperator
		extends ExpressionBinaryOperator {
	
	public ExpressionElementwiseBinaryOperator(Expression lhs,
			EnumOperator operator, Expression rhs) {
		super(lhs, operator, rhs);
	}
	
	@Override
	public String toCompiledString() {
		return "<%s .%s %s>".format((Object) lhs.toCompiledString(),
				operator, rhs.toCompiledString());
	}
	
	@Override
	public Object eval(Scope scope) {
		return operator.call(/*elementWise*/true, lhs.eval(scope),
				rhs.eval(scope));
	}
	
	@Override
	public String toEvalString() {
		return lhs.toEvalString() + " ." + operator.getSymbol() + " "
				+ rhs.toEvalString();
	}
	
	@Override
	public String toString() {
		return "ElementwiseBinaryOperator{lhs=%s,operator=%s,rhs=%s}".format(
				lhs, operator, rhs);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitBinary(this);
	}
	
}
