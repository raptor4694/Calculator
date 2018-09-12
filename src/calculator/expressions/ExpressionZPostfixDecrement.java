package calculator.expressions;

import calculator.Scope;
import calculator.Visitor;
import calculator.errors.DimensionError;
import calculator.errors.TypeError;
import calculator.values.Number;
import calculator.values.Real;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionZPostfixDecrement
		implements ExpressionPostfixDecrement, ExpressionVectorElement {
	public final Expression array;
	
	@Override
	public Object eval(Scope scope) {
		Object obj = array.eval(scope);
		
		if (!(obj instanceof Number[]))
			throw new TypeError("not an array");
		
		Number[] array = (Number[]) obj;
		
		if (array.length != 3)
			throw new DimensionError();
		
		Number last = array[2];
		array[2] = last.minus(Real.ONE);
		return last;
	}
	
	@Override
	public Object evalReference(Scope scope) {
		return array.eval(scope);
	}
	
	@Override
	public String toEvalString() {
		return getNameString() + "--";
	}
	
	@Override
	public String getNameString() {
		return array.toEvalString() + ".z";
	}
	
	@Override
	public String toCompiledString() {
		return "<<.Z " + array.toCompiledString() + "> DEC>";
	}
	
	@Override
	public String toString() {
		return "ZPostfixDecrement{array=%s}".format(array);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitPostfixDecrement(this);
	}
}
