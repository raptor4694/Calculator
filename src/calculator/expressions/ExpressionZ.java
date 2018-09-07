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
public class ExpressionZ implements ExpressionReferenceable {
	public final Expression array;
	
	@Override
	public Object eval(Scope scope) {
		Object obj = array.eval(scope);
		
		if (!(obj instanceof Number[]))
			throw new TypeError("not an array");
		
		Number[] array = (Number[]) obj;
		
		if (array.length != 3)
			throw new DimensionError();
		
		return array[2];
	}
	
	@Override
	public String toEvalString() {
		return array.toEvalString() + ".z";
	}
	
	@Override
	public String getNameString() {
		return toEvalString();
	}
	
	@Override
	public String toCompiledString() {
		return "<.Z %s>".format((Object) array.toCompiledString());
	}
	
	@Override
	public String toString() {
		return "Z{array=%s}".format(array);
	}
	
	@Override
	public Expression toAssign(Expression value) {
		return new ExpressionZAssign(array, value);
	}
	
	@Override
	public Expression toAssignOp(EnumOperator operator, Expression value) {
		return new ExpressionZAssignOp(array, operator, value);
	}
	
	@Override
	public Expression toPrefixIncrement() {
		return new ExpressionZPrefixIncrement(array);
	}
	
	@Override
	public Expression toPrefixDecrement() {
		return new ExpressionZPrefixDecrement(array);
	}
	
	@Override
	public Expression toPostfixIncrement() {
		return new ExpressionZPostfixIncrement(array);
	}
	
	@Override
	public Expression toPostfixDecrement() {
		return new ExpressionZPostfixDecrement(array);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitVectorZ(this);
	}
	
}
