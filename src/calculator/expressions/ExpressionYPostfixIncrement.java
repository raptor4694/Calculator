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
public class ExpressionYPostfixIncrement
		implements ExpressionPostfixIncrement, ExpressionVectorElement {
	public final Expression array;
	
	@Override
	public Object eval(Scope scope) {
		Object obj = array.eval(scope);
		
		if (!(obj instanceof Number[]))
			throw new TypeError("not an array");
		
		Number[] array = (Number[]) obj;
		
		if (array.length != 2 && array.length != 3)
			throw new DimensionError();
		
		Number last = array[1];
		array[1] = last.plus(Real.ONE);
		return last;
	}
	
	@Override
	public Object evalReference(Scope scope) {
		return array.eval(scope);
	}
	
	@Override
	public String toEvalString() {
		return getNameString() + "++";
	}
	
	@Override
	public String getNameString() {
		return array.toEvalString() + ".y";
	}
	
	@Override
	public String toCompiledString() {
		return "<<.Y %s> INC>".format((Object) array.toCompiledString());
	}
	
	@Override
	public String toString() {
		return "YPostfixIncrement{array=%s}".format(array);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitPostfixIncrement(this);
	}
	
}
