package calculator;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(String.class)
@AllArgsConstructor
public class ExpressionZPrefixDecrement
		implements ExpressionPrefixDecrement, ExpressionVectorElement {
	Expression array;
	
	@Override
	public Object eval(Scope scope) {
		Object obj = array.eval(scope);
		
		if (!(obj instanceof Number[]))
			throw new TypeError("not an array");
		
		Number[] array = (Number[]) obj;
		
		if (array.length != 3)
			throw new DimensionError();
		
		return array[2] = array[2].minus(Real.ONE);
	}
	
	@Override
	public Object evalReference(Scope scope) {
		return array.eval(scope);
	}
	
	@Override
	public String toEvalString() {
		return "--" + getNameString();
	}
	
	@Override
	public String getNameString() {
		return array.toEvalString() + ".z";
	}
	
	@Override
	public String toCompiledString() {
		return "<DEC <.Z " + array.toCompiledString() + ">>";
	}
	
	@Override
	public String toString() {
		return "ZPrefixDecrement{array=%s}".format(array);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visitPrefixDecrement(this);
	}
	
}
