package calculator.expressions;

import calculator.DimensionError;
import calculator.Scope;
import calculator.TypeError;
import calculator.Visitor;
import calculator.values.EnumOperator;
import calculator.values.Number;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionY implements ExpressionReferenceable {
	public final Expression array;
	
	@Override
	public Object eval(Scope scope) {
		Object obj = array.eval(scope);
		
		if (!(obj instanceof Number[]))
			throw new TypeError("not an array");
		
		Number[] array = (Number[]) obj;
		
		if (array.length != 2 && array.length != 3)
			throw new DimensionError();
		
		return array[1];
	}
	
	@Override
	public String toEvalString() {
		return array.toEvalString() + ".y";
	}
	
	@Override
	public String getNameString() {
		return toEvalString();
	}
	
	@Override
	public String toCompiledString() {
		return "<.Y %s>".format((Object) array.toCompiledString());
	}
	
	@Override
	public String toString() {
		return "Y{array=%s}".format(array);
	}
	
	@Override
	public Expression toAssign(Expression value) {
		return new ExpressionYAssign(array, value);
	}
	
	@Override
	public Expression toAssignOp(EnumOperator operator, Expression value) {
		return new ExpressionYAssignOp(array, operator, value);
	}
	
	@Override
	public Expression toPrefixIncrement() {
		return new ExpressionYPrefixIncrement(array);
	}
	
	@Override
	public Expression toPrefixDecrement() {
		return new ExpressionYPrefixDecrement(array);
	}
	
	@Override
	public Expression toPostfixIncrement() {
		return new ExpressionYPostfixIncrement(array);
	}
	
	@Override
	public Expression toPostfixDecrement() {
		return new ExpressionYPostfixDecrement(array);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitVectorY(this);
	}
}
