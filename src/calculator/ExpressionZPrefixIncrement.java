package calculator;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionZPrefixIncrement
		implements ExpressionPrefixIncrement, ExpressionVectorElement {
	Expression array;
	
	@Override
	public Object eval(Scope scope) {
		Object obj = array.eval(scope);
		
		if (!(obj instanceof Number[]))
			throw new TypeError("not an array");
		
		Number[] array = (Number[]) obj;
		
		if (array.length != 3)
			throw new DimensionError();
		
		return array[2] = array[2].plus(Real.ONE);
	}
	
	@Override
	public Object evalReference(Scope scope) {
		return array.eval(scope);
	}
	
	@Override
	public String toEvalString() {
		return "++" + getNameString();
	}
	
	@Override
	public String toCompiledString() {
		return "<INC <.Z " + array.toCompiledString() + ">>";
	}
	
	@Override
	public String toString() {
		return "ZPrefixIncrement{array=%s}".format(array);
	}
	
	@Override
	public String getNameString() {
		return array.toEvalString() + ".z";
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitPrefixIncrement(this);
	}
	
}
