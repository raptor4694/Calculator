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
public class ExpressionX implements ExpressionReferenceable {
	public final Expression array;
	
	@Override
	public Object eval(Scope scope) {
		Object obj = array.eval(scope);
		
		if (!(obj instanceof Number[]))
			throw new TypeError("not an array");
		
		Number[] array = (Number[]) obj;
		
		if (array.length != 2 && array.length != 3)
			throw new DimensionError();
		
		return array[0];
	}
	
	@Override
	public String toEvalString() {
		return array.toEvalString() + ".x";
	}
	
	@Override
	public String getNameString() {
		return toEvalString();
	}
	
	@Override
	public String toCompiledString() {
		return "<.X %s>".format((Object) array.toCompiledString());
	}
	
	@Override
	public String toString() {
		return "X{array=%s}".format(array);
	}
	
	@Override
	public Expression toAssign(Expression value) {
		return new ExpressionXAssign(array, value);
	}
	
	@Override
	public Expression toAssignOp(EnumOperator operator, Expression value) {
		return new ExpressionXAssignOp(array, operator, value);
	}
	
	@Override
	public Expression toPrefixIncrement() {
		return new ExpressionXPrefixIncrement(array);
	}
	
	@Override
	public Expression toPrefixDecrement() {
		return new ExpressionXPrefixDecrement(array);
	}
	
	@Override
	public Expression toPostfixIncrement() {
		return new ExpressionXPostfixIncrement(array);
	}
	
	@Override
	public Expression toPostfixDecrement() {
		return new ExpressionXPostfixDecrement(array);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitVectorX(this);
	}
}
