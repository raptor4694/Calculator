package calculator.expressions;

import calculator.DimensionError;
import calculator.Scope;
import calculator.TypeError;
import calculator.Visitor;
import calculator.values.Number;
import calculator.values.Real;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionXPostfixDecrement
		implements ExpressionPostfixDecrement, ExpressionVectorElement {
	public final Expression array;
	
	@Override
	public Object eval(Scope scope) {
		Object obj = array.eval(scope);
		
		if (!(obj instanceof Number[]))
			throw new TypeError("not an array");
		
		Number[] array = (Number[]) obj;
		
		if (array.length != 2 && array.length != 3)
			throw new DimensionError();
		
		Number last = array[0];
		array[0] = last.minus(Real.ONE);
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
		return array.toEvalString() + ".x";
	}
	
	@Override
	public String toCompiledString() {
		return "<<.X %s> DEC>".format((Object) array.toCompiledString());
	}
	
	@Override
	public String toString() {
		return "XPostfixDecrement{array=%s}".format(array);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitPostfixDecrement(this);
	}
}
